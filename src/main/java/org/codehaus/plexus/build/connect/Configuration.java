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

import org.codehaus.plexus.build.connect.messages.Message;
import org.codehaus.plexus.build.connect.messages.ProjectsReadMessage;

/**
 * Provides access to the configuration provided by the server
 */
public interface Configuration {

    /**
     * If this property is set to <code>true</code> in reply to a session start, a
     * {@link ProjectsReadMessage} will be send to the endpoint containing all
     * projects with their effective model
     */
    public static final String CONFIG_SEND_AFTER_PROJECTS_READ = "afterProjectsRead";

    /**
     * @return <code>true</code> if {@link #CONFIG_SEND_AFTER_PROJECTS_READ} is
     *         provided
     */
    public boolean isSendProjects();

    /**
     * Creates a Configuration from a message
     *
     * @param message
     * @return the configuration backed by the message payload
     */
    public static Configuration of(Message message) {
        return new Configuration() {

            @Override
            public boolean isSendProjects() {
                return message.getBooleanProperty(CONFIG_SEND_AFTER_PROJECTS_READ, false);
            }
        };
    }
}
