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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;

/**
 * Event that is received / send when a session starts/end
 */
public class SessionMessage extends Message {

    private static final String SESSION_EXECUTION_ROOT_DIRECTORY = "sessionExecutionRootDirectory";
    private static final String SESSION_START = "sessionStart";
    private static final String SESSION_ID = "sessionId";
    private static final Map<MavenExecutionRequest, String> ID_MAP = new WeakHashMap<>();

    /**
     * Creates a new session message
     *
     * @param session the session to use
     * @param start   <code>true</code> if it is a start of the session or
     *                <code>false</code> if it is the end of a session
     */
    public SessionMessage(MavenSession session, boolean start) {
        super(buildMap(session, start));
    }

    SessionMessage(String sessionId, long threadId, Map<String, String> payload) {
        super(sessionId, threadId, payload);
    }

    public String getSessionId() {
        return getProperty(SESSION_ID);
    }

    /**
     * @return <code>true</code> if this is a session start event
     */
    public boolean isSessionStart() {
        return getBooleanProperty(SESSION_START);
    }

    /**
     * @return the value of the ExecutionRootDirectory of this session
     */
    public String getExecutionRootDirectory() {
        return getProperty(SESSION_EXECUTION_ROOT_DIRECTORY);
    }

    /**
     * Returns the unique ID for a session
     *
     * @param session the session to get an Id for
     * @return the id of the session or the name of the current thread if the
     *         session is <code>null</code>
     */
    public static synchronized String getId(MavenSession session) {
        if (session == null) {
            return Thread.currentThread().getName();
        }
        // we can't use the session itself as a key, because sessions might be cloned,
        // but the execution request should (hopefully) stay constant...
        return ID_MAP.computeIfAbsent(
                session.getRequest(), x -> UUID.randomUUID().toString());
    }

    private static Map<String, String> buildMap(MavenSession session, boolean start) {
        Map<String, String> map = new HashMap<>(2);
        map.put(SESSION_ID, getId(session));
        map.put(SESSION_START, Boolean.toString(start));
        map.put(SESSION_EXECUTION_ROOT_DIRECTORY, session.getExecutionRootDirectory());
        return map;
    }
}
