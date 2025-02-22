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
package org.codehaus.plexus.build.connect.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A message exchanged between two endpoints, usually an IDE and a maven build
 */
public class Message {
    private static final ThreadLocal<Long> ID = new ThreadLocal<Long>() {
        private final AtomicLong generator = new AtomicLong();

        @Override
        protected Long initialValue() {
            return generator.getAndIncrement();
        }
    };
    private final long threadId;
    private final Map<String, String> properties;
    private final String sessionId;

    Message(Map<String, String> payload) {
        this(null, ID.get(), payload);
    }

    Message(String sessionId, long threadId, Map<String, String> payload) {
        this.sessionId = sessionId;
        this.properties = Objects.requireNonNull(payload);
        this.threadId = threadId;
    }

    /**
     * Get a String property from the payload
     *
     * @param key the key to fetch
     * @return the value
     */
    public String getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Get a String property from the payload
     *
     * @param key          the key to fetch
     * @param defaultValue default value to use when no value is present
     * @return the value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    /**
     * Get a boolean property from the payload
     *
     * @param key the key to fetch
     * @return the value
     */
    public boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(properties.get(key));
    }

    /**
     * Get a boolean property from the payload
     *
     * @param key          the key to fetch
     * @param defaultValue the value to use if not value is present
     * @return the value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String property = getProperty(key);
        if (property == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(property);
    }

    /**
     * @return the remote session id for this message, only valid for messages not
     *         created locally
     */
    public String getSessionId() {
        if (sessionId == null) {
            throw new IllegalStateException("can not be called on a local message!");
        }
        return sessionId;
    }

    /**
     * @return the bytes using the message session id
     */
    public byte[] serialize() {
        return serialize(getSessionId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + sessionId + "][" + threadId + "] " + properties;
    }

    /**
     * Creates bytes for this message using the session id
     *
     * @param sessionId
     * @return the bytes using the supplied message id
     */
    public byte[] serialize(String sessionId) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
            writeString(sessionId, out);
            out.writeLong(threadId);
            writeString(getClass().getSimpleName(), out);
            if (properties.isEmpty()) {
                out.writeInt(0);
            } else {
                Set<Entry<String, String>> set = properties.entrySet();
                out.writeInt(set.size());
                for (Entry<String, String> entry : set) {
                    writeString(entry.getKey(), out);
                    writeString(entry.getValue(), out);
                }
            }
        } catch (IOException e) {
            // should never happen, but if it happens something is wrong!
            throw new RuntimeException("Internal Error: Write data failed", e);
        }
        return stream.toByteArray();
    }

    /**
     * Creates a reply to a message using the thread id and session id from the
     * original but with the provided payload
     *
     * @param message the reply message to inherit from
     * @param payload the new payload
     * @return the message
     */
    public static Message replyTo(Message message, Map<String, String> payload) {
        if (payload == null) {
            payload = Collections.emptyMap();
        }
        return new Message(message.sessionId, message.threadId, payload);
    }

    /**
     * Decodes a message from its bytes
     *
     * @param bytes the bytes to decode
     * @return the message or <code>null</code> if decoding failed
     */
    public static Message decode(byte[] bytes) {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(stream);
        try {
            String sessionId = readString(in);
            long threadId = in.readLong();
            String messageType = readString(in);
            int size = in.readInt();
            Map<String, String> payload = new LinkedHashMap<>(size);
            for (int i = 0; i < size; i++) {
                payload.put(readString(in), readString(in));
            }
            if ("SessionMessage".equals(messageType)) {
                return new SessionMessage(sessionId, threadId, payload);
            }
            if ("ProjectsReadMessage".equals(messageType)) {
                return new ProjectsReadMessage(sessionId, threadId, payload);
            }
            if ("RefreshMessage".equals(messageType)) {
                return new RefreshMessage(sessionId, threadId, payload);
            }
            return new Message(sessionId, threadId, payload);
        } catch (IOException e) {
            // should never happen, but if it happens something is wrong!
            System.err.println("Internal Error: Message decoding failed: " + e);
        }
        return null;
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 0) {
            return null;
        }
        if (length == 0) {
            return "";
        }
        byte[] bs = new byte[length];
        in.readFully(bs);
        return new String(bs, StandardCharsets.UTF_8);
    }

    private static void writeString(String string, DataOutputStream stream) throws IOException {
        if (string == null) {
            stream.writeInt(-1);
        } else {
            byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
            stream.writeInt(bytes.length);
            stream.write(bytes);
        }
    }
}
