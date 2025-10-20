/*
This program is licensed to you under the Apache License Version 2.0,
and you may not use this file except in compliance with the Apache License Version 2.0.
You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing,
software distributed under the Apache License Version 2.0 is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
*/
package org.codehaus.plexus.build.messages;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Builder class for constructing messages.
 * <p>
 * This class implements the builder pattern for creating messages with various parameters.
 * It is typically not called directly by client code, but is used internally by the Messages API
 * implementations.
 * </p>
 */
public class MessageBuilder {
    private final MessageType type;
    private final Path path;
    private final Consumer<Message> consumer;

    private int line = 0;
    private int column = 0;
    private Throwable cause;

    /**
     * Creates a new MessageBuilder.
     * <p>
     * Note: This constructor is usually not called by client code. Use the builder methods
     * provided by the {@link Messages} interface instead (e.g., buildError, buildWarning, buildInfo).
     * </p>
     *
     * @param type the type of message to build
     * @param path the file path for which the message should be created
     * @param consumer the consumer that will receive the constructed message
     */
    public MessageBuilder(MessageType type, Path path, Consumer<Message> consumer) {
        this.type = type;
        this.path = path;
        this.consumer = consumer;
    }

    /**
     * Sets the line number for the message.
     *
     * @param line the line number (1-based, use 0 for unknown)
     * @return this builder for method chaining
     */
    public MessageBuilder line(int line) {
        this.line = line;
        return this;
    }

    /**
     * Sets the column number for the message.
     *
     * @param column the column number (1-based, use 0 for unknown)
     * @return this builder for method chaining
     */
    public MessageBuilder column(int column) {
        this.column = column;
        return this;
    }

    /**
     * Sets the exception cause for the message.
     *
     * @param cause the exception that caused this message
     * @return this builder for method chaining
     */
    public MessageBuilder cause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    /**
     * Creates the message object with all collected parameters and informs the consumer.
     * This method finalizes the builder and creates the message.
     *
     * @param message the message text (must not be null or blank)
     */
    public void create(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message text must not be null or blank");
        }
        Message msg = new Message(type, path, line, column, message, cause);
        consumer.accept(msg);
    }
}
