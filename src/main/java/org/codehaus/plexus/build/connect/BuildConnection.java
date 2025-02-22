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

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.build.connect.messages.Message;

/**
 * A {@link BuildConnection} allow communication between a an IDE and a maven
 * build to observe the state of the build and act on certain events. This is
 * usually not used directly by mojos but invoked internally by other APIs.
 */
public interface BuildConnection {

    /**
     * Send a message and returns the reply from the other endpoint, should only be
     * called from a maven thread!
     *
     * @param message      the message to send
     * @param mavenSession the maven session to reference
     * @return the reply message or <code>null</code> if this connection is not
     *         enabled and the message was discarded.
     */
    Message send(Message message, MavenSession mavenSession);

    /**
     * This method allows code to perform an eager check if a buildconnection is
     * present to send messages. This can be used to guard operations to prevent
     * allocate resources or objects if the message will be dropped.
     *
     * @return <code>true</code> if the connection can be used to send messages or
     *         if they will be discarded
     */
    boolean isEnabled();
}
