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

/**
 * <p>Messages interface.</p>
 * <p>
 * This API provides a modern, flexible way to create and manage build messages/markers
 * that inform users in an IDE about issues in their files. It uses a builder pattern
 * for constructing messages in a more convenient and extensible way compared to the
 * legacy BuildContext message methods.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * messages.error(Paths.get("/path/to/file.java"))
 *     .line(42)
 *     .column(10)
 *     .create("Syntax error");
 * </pre>
 */
public interface Messages {

    /**
     * Clears all messages.
     * This removes all messages that were previously created through this API.
     */
    void clearAll();

    /**
     * Clears messages associated with a specific path.
     *
     * @param path the file path for which to clear messages
     */
    void clear(Path path);

    /**
     * Creates a builder for an error message.
     *
     * @param path the file path for which the error message should be created
     * @return a MessageBuilder for constructing the error message
     */
    MessageBuilder error(Path path);

    /**
     * Creates a builder for a warning message.
     *
     * @param path the file path for which the warning message should be created
     * @return a MessageBuilder for constructing the warning message
     */
    MessageBuilder warning(Path path);

    /**
     * Creates a builder for an informational message.
     *
     * @param path the file path for which the info message should be created
     * @return a MessageBuilder for constructing the info message
     */
    MessageBuilder info(Path path);

    /**
     * Creates a builder for a message of a specific type.
     * This is the generic method that the other build methods delegate to.
     *
     * @param type the type of message to build
     * @param path the file path for which the message should be created
     * @return a MessageBuilder for constructing the message
     */
    MessageBuilder build(MessageType type, Path path);
}
