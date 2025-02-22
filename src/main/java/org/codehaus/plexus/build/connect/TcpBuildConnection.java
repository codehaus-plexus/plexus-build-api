/*
Copyright (c) 2025 Christoph Läubrich All rights reserved.

This program is licensed to you under the Apache License Version 2.0,
and you may not use this file except in compliance with the Apache License Version 2.0.
You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing,
software distributed under the Apache License Version 2.0 is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
*/
package org.codehaus.plexus.build.connect;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.build.connect.messages.Message;
import org.codehaus.plexus.build.connect.messages.SessionMessage;

/**
 * Default implementation using the system property
 * <code>plexus.build.ipc.port</code> to communicate with an endpoint to
 * exchange messages
 */
@Named("default")
@Singleton
public class TcpBuildConnection implements BuildConnection {
    private static final String PLEXUS_BUILD_IPC_PORT = "plexus.build.ipc.port";

    private static final int PORT = Integer.getInteger(PLEXUS_BUILD_IPC_PORT, 0);

    @Inject
    private LegacySupport support;

    private Map<String, Configuration> configMap = new ConcurrentHashMap<>();

    private final ThreadLocal<TcpClientConnection> connections =
            ThreadLocal.withInitial(() -> new TcpClientConnection());

    @Override
    public boolean isEnabled() {
        return PORT > 0;
    }

    @Override
    public Message send(Message message) {
        if (isEnabled()) {
            String sessionId;
            boolean sessionStart;
            if (message instanceof SessionMessage) {
                sessionId = message.getSessionId();
                sessionStart = ((SessionMessage) message).isSessionStart();
            } else {
                sessionId = getThreadSessionId();
                sessionStart = false;
            }
            byte[] messageBytes = message.serialize(sessionId);
            byte[] replyBytes = connections.get().send(messageBytes);
            if (replyBytes.length > 0) {
                Message reply = Message.decode(replyBytes);
                if (reply != null && sessionStart) {
                    configMap.put(sessionId, Configuration.of(reply));
                }
                return reply;
            }
        }
        return null;
    }

    private String getThreadSessionId() {
        // We must use LegacySupport here to get the currents threads session (what
        // might be cloned)
        return SessionMessage.getId(support.getSession());
    }

    @Override
    public Configuration getConfiguration() {
        String id = getThreadSessionId();
        if (id == null) {
            throw new IllegalStateException("No session attached to current thread!");
        }
        Configuration configuration = configMap.get(id);
        if (configuration == null) {
            throw new IllegalStateException("No configuration active for session " + id + "!");
        }
        return configuration;
    }

    /**
     * Creates a new server that will receive messages from a remote endpoint and
     * inform the consumer
     *
     * @param consumer the consumer of messages, might be called by different
     *                 threads, if the consumer throws an exception while handling a
     *                 message it will maybe no longer receive some messages. The
     *                 returned map is used as a payload for the reply to the
     *                 server, if <code>null</code> is returned a simple
     *                 acknowledgement without any payload will be send to the
     *                 endpoint. If the consumer performs blocking operations the
     *                 further execution of the maven process might be halted
     *                 depending on the message type, if that is not desired work
     *                 should be offloaded by the consumer to a different thread.
     * @return a {@link ServerConnection} that can be used to shutdown the server
     *         and get properties that needs to be passed to the maven process
     * @throws IOException if no local socket can be opened
     */
    public static ServerConnection createServer(Function<Message, Map<String, String>> consumer) throws IOException {
        return new ServerConnection(new ServerSocket(0), consumer);
    }

    /**
     * Represents a server connection that must be created to communicate with the
     * maven process using the {@link TcpBuildConnection}
     */
    public static final class ServerConnection implements AutoCloseable {

        private ServerSocket socket;
        private ExecutorService executor = Executors.newCachedThreadPool();
        private List<TcpServerConnection> connections = new ArrayList<>();

        ServerConnection(ServerSocket socket, Function<Message, Map<String, String>> consumer) {
            this.socket = socket;
            executor.execute(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        TcpServerConnection connection = new TcpServerConnection(socket.accept(), consumer);
                        connections.add(connection);
                        executor.execute(connection);
                    } catch (IOException e) {
                        return;
                    }
                }
            });
        }

        @Override
        public void close() {
            executor.shutdownNow();
            for (TcpServerConnection connection : connections) {
                connection.close();
            }
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        /**
         * Given a consumer publishes required properties for a process to launch
         *
         * @param consumer the consumer for system properties
         */
        public void setupProcess(BiConsumer<String, String> consumer) {
            // currently only one but might become more later (e.g. timeout, reconnects,
            // ...)
            consumer.accept(PLEXUS_BUILD_IPC_PORT, Integer.toString(socket.getLocalPort()));
        }
    }

    private static final class TcpServerConnection implements Runnable, Closeable {

        private Socket socket;
        private Function<Message, Map<String, String>> consumer;
        private DataInputStream in;
        private DataOutputStream out;
        private AtomicBoolean closed = new AtomicBoolean();

        public TcpServerConnection(Socket socket, Function<Message, Map<String, String>> consumer) throws IOException {
            this.socket = socket;
            this.consumer = consumer;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }

        @Override
        public void run() {
            try {
                while (!closed.get() && !Thread.currentThread().isInterrupted()) {
                    try {
                        int length = in.readInt();
                        if (length == 0) {
                            return;
                        }
                        byte[] bytes = new byte[length];
                        in.readFully(bytes);
                        Message message = Message.decode(bytes);
                        Map<String, String> payload = consumer.apply(message);
                        Message reply = Message.replyTo(message, payload);
                        byte[] responseBytes = reply.serialize();
                        synchronized (out) {
                            out.writeInt(responseBytes.length);
                            out.write(responseBytes);
                            out.flush();
                        }
                    } catch (Exception e) {
                        return;
                    }
                }
            } finally {
                close();
            }
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                try {
                    synchronized (out) {
                        out.writeInt(0);
                        out.flush();
                    }
                } catch (IOException e) {
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static final class TcpClientConnection {

        private Socket socket;
        private boolean closed;
        private DataInputStream in;
        private DataOutputStream out;

        public byte[] send(byte[] messageBytes) {
            if (!closed) {
                try {
                    if (socket == null) {
                        socket = new Socket("localhost", PORT);
                        in = new DataInputStream(socket.getInputStream());
                        out = new DataOutputStream(socket.getOutputStream());
                    }
                    out.writeInt(messageBytes.length);
                    out.write(messageBytes);
                    out.flush();
                    int length = in.readInt();
                    if (length == 0) {
                        socket.close();
                        closed = true;
                    } else {
                        byte[] bytes = new byte[length];
                        in.readFully(bytes);
                        return bytes;
                    }
                } catch (IOException e) {
                    closed = true;
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e1) {
                        }
                    }
                }
            }
            return new byte[0];
        }
    }
}
