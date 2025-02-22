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

import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.plugin.MojoExecution;

/**
 * Mesaage generated when a mojo is executed
 */
public class MojoMessage extends Message {

    private static final String GOAL = "goal";
    private static final String LIFECYCLE_PHASE = "lifecyclePhase";
    private static final String EXECUTION_ID = "executionId";
    private static final String VERSION = "version";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String GROUP_ID = "groupId";
    private static final String EVENT_TYPE = "eventType";

    MojoMessage(String sessionId, long threadId, Map<String, String> payload) {
        super(sessionId, threadId, payload);
    }

    /**
     * Creates a Mojo message from execution and type
     *
     * @param mojoExecution
     * @param type
     */
    public MojoMessage(MojoExecution mojoExecution, Type type) {
        super(toMap(mojoExecution, type));
    }

    /**
     * @return the group id
     */
    public String getGroupId() {
        return getProperty(GROUP_ID);
    }

    /**
     * @return the artifact id
     */
    public String getArtifactId() {
        return getProperty(ARTIFACT_ID);
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return getProperty(VERSION);
    }

    /**
     * @return the execution id
     */
    public String getExecutionId() {
        return getProperty(EXECUTION_ID);
    }

    /**
     * @return the lifecycle phase
     */
    public String getLifecyclePhase() {
        return getProperty(LIFECYCLE_PHASE);
    }

    /**
     * @return the lifecycle phase
     */
    public String getGoal() {
        return getProperty(GOAL);
    }

    /**
     * @return the type of event
     */
    public EventType getType() {
        try {
            return EventType.valueOf(getProperty(EVENT_TYPE));
        } catch (RuntimeException e) {
            return EventType.Unknown;
        }
    }

    private static Map<String, String> toMap(MojoExecution mojoExecution, Type type) {
        Map<String, String> map = new HashMap<>();
        map.put(EVENT_TYPE, type.name());
        map.put(GROUP_ID, mojoExecution.getGroupId());
        map.put(ARTIFACT_ID, mojoExecution.getArtifactId());
        map.put(VERSION, mojoExecution.getVersion());
        map.put(EXECUTION_ID, mojoExecution.getExecutionId());
        map.put(LIFECYCLE_PHASE, mojoExecution.getLifecyclePhase());
        map.put(GOAL, mojoExecution.getGoal());
        return map;
    }

    /**
     * create the event type
     */
    public static enum EventType {
        /**
         * the mojo was started
         */
        MojoStarted,
        /**
         * The mojo failed
         */
        MojoFailed,
        /**
         * The mojo was skipped
         */
        MojoSkipped,
        /**
         * The mojo succeed
         */
        MojoSucceeded,
        /**
         * the type is unknown
         */
        Unknown;
    }
}
