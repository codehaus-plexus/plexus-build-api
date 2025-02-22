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

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.build.connect.messages.Message;
import org.codehaus.plexus.build.connect.messages.ProjectsReadMessage;
import org.codehaus.plexus.build.connect.messages.SessionMessage;

/**
 * Listen to session events and send them to the connection
 */
@Named
@Singleton
public class SessionListener extends AbstractMavenLifecycleParticipant {

    @Inject
    private BuildConnection connection;

    private boolean sendProjects;
    private boolean started;

    @Override
    public void afterSessionStart(MavenSession session) throws MavenExecutionException {
        started = true;
        Message reply = connection.send(new SessionMessage(session, true));
        if (reply != null) {
            sendProjects = Configuration.of(reply).isSendProjects();
        }
    }

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        if (connection.isEnabled()) {
            if (!started) {
                afterSessionStart(session);
            }
            if (sendProjects) {
                connection.send(new ProjectsReadMessage(session.getAllProjects()));
            }
        }
    }

    @Override
    public void afterSessionEnd(MavenSession session) throws MavenExecutionException {
        connection.send(new SessionMessage(session, false));
        started = false;
    }
}
