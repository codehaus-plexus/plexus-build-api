package org.codehaus.plexus.build.connect;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.codehaus.plexus.build.connect.messages.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Drives the server side of the IPC protocol with a raw socket: each frame is an {@code int} length followed by a
 * serialized {@link Message}, and a zero length closes the connection.
 */
@Timeout(30)
class TcpBuildConnectionTest {

    private static final String PORT_PROPERTY = "plexus.build.ipc.port";

    @Test
    void disabledWithoutPortProperty() throws IOException {
        assumeTrue(System.getProperty(PORT_PROPERTY) == null, "the IPC port is set for this JVM");
        TcpBuildConnection connection = new TcpBuildConnection();

        assertFalse(connection.isEnabled());
        assertNull(connection.send(message("s", 1L, Collections.emptyMap()), null));
    }

    @Test
    void serverRepliesOnTheSameSessionAndThread() throws Exception {
        List<Message> received = new CopyOnWriteArrayList<>();
        try (TcpBuildConnection.ServerConnection server = TcpBuildConnection.createServer(message -> {
            received.add(message);
            return Collections.singletonMap("echo", message.getProperty("ping"));
        })) {
            try (Client client = new Client(port(server))) {
                Message request = message("session-a", 5L, Collections.singletonMap("ping", "pong"));

                Message reply = client.exchange(request);

                assertEquals("session-a", reply.getSessionId());
                assertEquals("pong", reply.getProperty("echo"));
                assertEquals(Message.replyTo(request, Collections.singletonMap("echo", "pong")), reply);
                assertEquals(Collections.singletonList(request), received);

                Message second = client.exchange(message("session-a", 5L, Collections.singletonMap("ping", "again")));
                assertEquals("again", second.getProperty("echo"));
            }
        }
    }

    @Test
    void zeroLengthFrameClosesTheConnection() throws Exception {
        try (TcpBuildConnection.ServerConnection server =
                        TcpBuildConnection.createServer(message -> Collections.emptyMap());
                Client client = new Client(port(server))) {
            client.out.writeInt(0);
            client.out.flush();

            assertEquals(0, client.in.readInt());
        }
    }

    @Test
    void failingConsumerClosesTheConnection() throws Exception {
        try (TcpBuildConnection.ServerConnection server = TcpBuildConnection.createServer(message -> {
                    throw new IllegalStateException("boom");
                });
                Client client = new Client(port(server))) {
            client.send(message("s", 1L, Collections.emptyMap()));

            assertEquals(0, client.in.readInt());
        }
    }

    @Test
    void setupProcessPublishesThePort() throws Exception {
        try (TcpBuildConnection.ServerConnection server =
                TcpBuildConnection.createServer(message -> Collections.emptyMap())) {
            Map<String, String> environment = new HashMap<>();
            server.setupProcess(environment::put);

            assertEquals(Collections.singleton(PORT_PROPERTY), environment.keySet());
            assertEquals(port(server), Integer.parseInt(environment.get(PORT_PROPERTY)));
        }
    }

    /** Builds a remote message through the wire format, since its constructor is not public. */
    private static Message message(String sessionId, long threadId, Map<String, String> payload) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        writeString(out, sessionId);
        out.writeLong(threadId);
        writeString(out, "Message");
        out.writeInt(payload.size());
        for (Map.Entry<String, String> entry : payload.entrySet()) {
            writeString(out, entry.getKey());
            writeString(out, entry.getValue());
        }
        return Message.decode(bytes.toByteArray());
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static int port(TcpBuildConnection.ServerConnection server) {
        int[] port = new int[1];
        server.setupProcess((key, value) -> port[0] = Integer.parseInt(value));
        return port[0];
    }

    private static final class Client implements AutoCloseable {
        private final Socket socket;
        private final DataInputStream in;
        private final DataOutputStream out;

        Client(int port) throws IOException {
            socket = new Socket("localhost", port);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }

        void send(Message message) throws IOException {
            byte[] bytes = message.serialize();
            out.writeInt(bytes.length);
            out.write(bytes);
            out.flush();
        }

        Message exchange(Message message) throws IOException {
            send(message);
            byte[] reply = new byte[in.readInt()];
            in.readFully(reply);
            return Message.decode(reply);
        }

        @Override
        public void close() throws IOException {
            socket.close();
        }
    }
}
