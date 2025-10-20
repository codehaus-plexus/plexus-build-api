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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.nio.file.Path;

import org.codehaus.plexus.build.BuildContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of the Messages interface.
 * <p>
 * This implementation delegates to the BuildContext for compatibility with existing
 * message handling infrastructure. It logs messages and calls the legacy BuildContext
 * message API.
 * </p>
 */
@Named("default")
@Singleton
public class DefaultMessages implements Messages {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMessages.class);

    private final BuildContext buildContext;

    /**
     * Creates a new DefaultMessages instance.
     *
     * @param buildContext the BuildContext to which messages will be delegated
     */
    @Inject
    public DefaultMessages(BuildContext buildContext) {
        this.buildContext = buildContext;
    }

    @Override
    public void clearAll() {
        // This is a no-op in the default implementation
        // Custom implementations may provide actual clearing functionality
    }

    @Override
    public void clear(Path path) {
        if (path != null) {
            buildContext.removeMessages(path.toFile());
        }
    }

    @Override
    public MessageBuilder error(Path path) {
        return build(MessageType.ERROR, path);
    }

    @Override
    public MessageBuilder warning(Path path) {
        return build(MessageType.WARNING, path);
    }

    @Override
    public MessageBuilder info(Path path) {
        return build(MessageType.INFO, path);
    }

    @Override
    public MessageBuilder build(MessageType type, Path path) {
        return new MessageBuilder(type, path, this::handleMessage);
    }

    /**
     * Handles a message by logging it and delegating to the BuildContext.
     *
     * @param message the message to handle
     */
    private void handleMessage(Message message) {
        // Log the message
        String logMessage = message.toString();

        switch (message.getType()) {
            case ERROR:
                logger.error(logMessage, message.getCause());
                break;
            case WARNING:
                logger.warn(logMessage, message.getCause());
                break;
            case INFO:
                logger.info(logMessage, message.getCause());
                break;
        }

        // Delegate to BuildContext for compatibility
        if (message.getPath() != null) {
            int severity = mapTypeToSeverity(message.getType());
            buildContext.addMessage(
                    message.getPath().toFile(),
                    message.getLine(),
                    message.getColumn(),
                    message.getMessage(),
                    severity,
                    message.getCause());
        }
    }

    /**
     * Maps a MessageType to a BuildContext severity level.
     *
     * @param type the message type
     * @return the corresponding BuildContext severity
     */
    private int mapTypeToSeverity(MessageType type) {
        switch (type) {
            case ERROR:
                return BuildContext.SEVERITY_ERROR;
            case WARNING:
                return BuildContext.SEVERITY_WARNING;
            case INFO:
            default:
                // BuildContext supports 0 as an info severity level (undocumented)
                return 0;
        }
    }
}
