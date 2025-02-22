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

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.project.MavenProject;

/**
 * Send to inform about project changes
 */
public class ProjectMessage extends Message {

    private static final String BASE_DIR = "baseDir";
    private static final String VERSION = "version";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String GROUP_ID = "groupId";
    private static final String EVENT_TYPE = "eventType";

    ProjectMessage(String sessionId, long threadId, Map<String, String> payload) {
        super(sessionId, threadId, payload);
    }

    /**
     * Constructs a new event based on project and type
     *
     * @param mavenProject
     * @param eventtype
     */
    public ProjectMessage(MavenProject mavenProject, Type eventtype) {
        super(toMap(mavenProject, eventtype));
    }

    /**
     * @return the group id of the project
     */
    public String getGroupId() {
        return getProperty(GROUP_ID);
    }

    /**
     * @return the artifact id of the project
     */
    public String getArtifactId() {
        return getProperty(ARTIFACT_ID);
    }

    /**
     * @return the version of the project
     */
    public String getVersion() {
        return getProperty(VERSION);
    }

    /**
     * @return the basedir of the project
     */
    public Path getBaseDir() {
        return new File(getProperty(BASE_DIR)).toPath();
    }

    /**
     * @return the type of the event
     */
    public EventType getType() {
        try {
            return EventType.valueOf(getProperty(EVENT_TYPE));
        } catch (RuntimeException e) {
            return EventType.Unknown;
        }
    }

    private static Map<String, String> toMap(MavenProject mavenProject, Type eventtype) {
        Map<String, String> map = new HashMap<>();
        map.put(EVENT_TYPE, eventtype.name());
        map.put(GROUP_ID, mavenProject.getGroupId());
        map.put(ARTIFACT_ID, mavenProject.getArtifactId());
        map.put(VERSION, mavenProject.getVersion());
        map.put(BASE_DIR, mavenProject.getBasedir().getAbsolutePath());
        return map;
    }

    /**
     * Describe the type of the event
     */
    public static enum EventType {
        /**
         * The project was started
         */
        ProjectStarted,
        /**
         * The project failed
         */
        ProjectFailed,
        /**
         * The project was skipped
         */
        ProjectSkipped,
        /**
         * the project succeed
         */
        ProjectSucceeded,
        /**
         * the type of event is unknown
         */
        Unknown;
    }
}
