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

import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.build.connect.messages.InitMessage;
import org.codehaus.plexus.build.connect.messages.Message;
import org.codehaus.plexus.build.connect.messages.MojoMessage;
import org.codehaus.plexus.build.connect.messages.ProjectMessage;
import org.codehaus.plexus.build.connect.messages.ProjectsMessage;
import org.codehaus.plexus.build.connect.messages.SessionMessage;

/**
 * Listen to all maven events and forward them to the endpoint
 */
@Named
@Singleton
public class EventListener implements EventSpy {

    private BuildConnection connection;
    private Configuration configuration;

    /**
     * Creates endpoint for the given connection
     *
     * @param connection injected
     */
    @Inject
    public EventListener(BuildConnection connection) {
        this.connection = connection;
    }

    @Override
    public void init(Context context) throws Exception {
        Message message = connection.send(new InitMessage(context), null);
        if (message != null) {
            configuration = Configuration.of(message);
        }
    }

    @Override
    public void onEvent(Object event) throws Exception {
        if (configuration == null) {
            return;
        }
        if (event instanceof ExecutionEvent) {
            handleExecutionEvent((ExecutionEvent) event);
        }
    }

    private void handleExecutionEvent(ExecutionEvent event) {
        MavenSession session = event.getSession();
        Type type = event.getType();
        switch (type) {
            case SessionStarted:
                connection.send(new SessionMessage(session, true), session);
                if (configuration.isSendProjects()) {
                    connection.send(new ProjectsMessage(session.getProjects()), session);
                }
                break;
            case SessionEnded:
                connection.send(new SessionMessage(session, false), session);
                break;
            case ProjectStarted:
            case ProjectFailed:
            case ProjectSkipped:
            case ProjectSucceeded:
                connection.send(new ProjectMessage(event.getProject(), type), session);
                break;
            case MojoStarted:
            case MojoFailed:
            case MojoSkipped:
            case MojoSucceeded:
                connection.send(new MojoMessage(event.getMojoExecution(), type), session);
                break;
            default:
                break;
        }
    }

    @Override
    public void close() throws Exception {}
}
