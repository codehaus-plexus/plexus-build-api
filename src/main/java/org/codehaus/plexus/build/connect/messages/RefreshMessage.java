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
import java.util.Collections;
import java.util.Map;

/**
 * A message that indicates a path should be refreshed (e.g. because new files
 * are placed in a generated folder)
 */
public class RefreshMessage extends Message {

    private static final String PATH_KEY = "path";

    /**
     * Create a new message to refresh a path
     *
     * @param path the path to refresh
     */
    public RefreshMessage(Path path) {
        super(Collections.singletonMap(PATH_KEY, path.toFile().getAbsolutePath()));
    }

    /**
     * @return the path to refresh
     */
    public Path getPath() {
        return new File(getProperty(PATH_KEY)).toPath();
    }

    RefreshMessage(String sessionId, long threadId, Map<String, String> payload) {
        super(sessionId, threadId, payload);
    }
}
