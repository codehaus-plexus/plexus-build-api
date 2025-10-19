/*
Copyright (c) 2008 Sonatype, Inc. All rights reserved.

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

/**
 * Represents a message with all its parameters.
 * This class holds the collected parameters for a message that can be created through the MessageBuilder.
 */
public class Message {
    private final MessageType type;
    private final Path path;
    private final int line;
    private final int column;
    private final String message;
    private final Throwable cause;

    /**
     * Creates a new message with the specified parameters.
     *
     * @param type the message type
     * @param path the file path associated with this message
     * @param line the line number (1-based, 0 for unknown)
     * @param column the column number (1-based, 0 for unknown)
     * @param message the message text
     * @param cause the exception cause, can be null
     */
    public Message(MessageType type, Path path, int line, int column, String message, Throwable cause) {
        this.type = type;
        this.path = path;
        this.line = line;
        this.column = column;
        this.message = message;
        this.cause = cause;
    }

    /**
     * @return the message type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * @return the file path
     */
    public Path getPath() {
        return path;
    }

    /**
     * @return the line number (1-based, 0 for unknown)
     */
    public int getLine() {
        return line;
    }

    /**
     * @return the column number (1-based, 0 for unknown)
     */
    public int getColumn() {
        return column;
    }

    /**
     * @return the message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the exception cause, or null if none
     */
    public Throwable getCause() {
        return cause;
    }
}
