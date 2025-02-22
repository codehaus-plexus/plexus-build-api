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

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.project.MavenProject;

/**
 * Message send to inform about reactor project in the build and their effective
 * model
 */
public class ProjectsReadMessage extends Message {

    private static final DefaultModelWriter MODEL_WRITER = new DefaultModelWriter();

    ProjectsReadMessage(String sessionId, long threadId, Map<String, String> payload) {
        super(sessionId, threadId, payload);
    }

    /**
     * @param projects the projects to send
     */
    public ProjectsReadMessage(Collection<MavenProject> projects) {
        super(buildMap(projects));
    }

    private static Map<String, String> buildMap(Collection<MavenProject> projects) {
        Map<String, String> map = new HashMap<>();
        for (MavenProject project : projects) {
            String key = project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();
            map.put(key, getEffectiveModel(project));
        }
        return map;
    }

    private static String getEffectiveModel(MavenProject project) {
        Model model = project.getModel();
        StringWriter writer = new StringWriter();
        try {
            MODEL_WRITER.write(writer, null, model);
        } catch (IOException e) {
        }
        String string = writer.toString();
        return string;
    }
}
