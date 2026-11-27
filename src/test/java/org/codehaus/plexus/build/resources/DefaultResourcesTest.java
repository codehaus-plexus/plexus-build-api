/*
This program is licensed to you under the Apache License Version 2.0,
and you may not use this file except in compliance with the Apache License Version 2.0.
You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing,
software distributed under the Apache License Version 2.0 is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
*/
package org.codehaus.plexus.build.resources;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.build.BuildContext;
import org.codehaus.plexus.build.RecordingProxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultResourcesTest {

    @TempDir
    Path tempDir;

    private final RecordingProxy<BuildContext> buildContext = RecordingProxy.of(BuildContext.class);

    private final RecordingProxy<LegacySupport> legacySupport = RecordingProxy.of(LegacySupport.class);

    private DefaultResources resources() {
        return new DefaultResources(buildContext.get(), legacySupport.get());
    }

    @Test
    void delegatesDeltaAndRefresh() {
        Path file = tempDir.resolve("file.txt");
        buildContext.answer("hasDelta", true);

        assertTrue(resources().hasDelta(file));
        resources().refresh(file);

        assertEquals(Arrays.asList("hasDelta", "refresh"), buildContext.methods());
        assertEquals(file.toFile(), buildContext.calls().get(1).args.get(0));
    }

    @Test
    void newOutputStreamDelegatesAndDerivedIsANoop() throws Exception {
        Path file = tempDir.resolve("out.txt");
        resources().newOutputStream(file, true);

        assertEquals(Collections.singletonList("newFileOutputStream"), buildContext.methods());

        RecordingProxy<BuildContext> plain = RecordingProxy.of(BuildContext.class);
        new DefaultResources(plain.get(), legacySupport.get()).newOutputStream(file);
        assertEquals(Collections.singletonList("newFileOutputStream"), plain.methods());
    }

    @Test
    void getPathResolvesAgainstTheProjectBasedir() {
        MavenProject project = new MavenProject();
        project.setFile(tempDir.resolve("pom.xml").toFile());
        MavenSession session = new MavenSession(
                null, new DefaultMavenExecutionRequest(), new DefaultMavenExecutionResult(), Collections.emptyList());
        session.setCurrentProject(project);
        legacySupport.answer("getSession", session);

        assertEquals(tempDir.resolve("src/main/java/A.java"), resources().getPath("src/main/java/A.java"));
    }

    @Test
    void getPathFallsBackToARelativePathWithoutASession() {
        assertEquals(Paths.get("src/main/java/A.java"), resources().getPath("src/main/java/A.java"));
    }

    @Test
    void copyWritesWhenTargetIsStaleAndCreatesParentDirectories() throws Exception {
        Path source = Files.write(tempDir.resolve("source.txt"), "content".getBytes(StandardCharsets.UTF_8));
        Path target = tempDir.resolve("nested/out.txt");

        // delegate stream creation to a real stream, leaving every other call to the recording proxy
        DefaultResources resources = new DefaultResources(new OutputStreamBuildContext(), legacySupport.get());
        resources.copy(source, target);

        assertArrayEquals(Files.readAllBytes(source), Files.readAllBytes(target));
    }

    @Test
    void copyDoesNothingWhenTargetIsUpToDate() throws Exception {
        Path source = tempDir.resolve("source.txt");
        Path target = tempDir.resolve("out.txt");
        buildContext.answer("isUptodate", true);

        resources().copy(source, target);

        assertFalse(Files.exists(target));
        assertEquals(Collections.singletonList("isUptodate"), buildContext.methods());
    }

    /** Records calls but returns a real stream for newFileOutputStream, so the copy can be observed. */
    private static final class OutputStreamBuildContext implements BuildContext {
        public boolean hasDelta(String relpath) {
            return false;
        }

        public boolean hasDelta(File file) {
            return false;
        }

        public boolean hasDelta(List<String> relpaths) {
            return false;
        }

        public void refresh(File file) {}

        public OutputStream newFileOutputStream(File file) throws java.io.IOException {
            return Files.newOutputStream(file.toPath());
        }

        public org.codehaus.plexus.util.Scanner newScanner(File basedir) {
            return null;
        }

        public org.codehaus.plexus.util.Scanner newDeleteScanner(File basedir) {
            return null;
        }

        public org.codehaus.plexus.util.Scanner newScanner(File basedir, boolean ignoreDelta) {
            return null;
        }

        public boolean isIncremental() {
            return false;
        }

        public void setValue(String key, Object value) {}

        public <T> T getValue(String key) {
            return null;
        }

        public void addWarning(File file, int line, int column, String message, Throwable cause) {}

        public void addError(File file, int line, int column, String message, Throwable cause) {}

        public void addMessage(File file, int line, int column, String message, int severity, Throwable cause) {}

        public void removeMessages(File file) {}

        public boolean isUptodate(File target, File source) {
            return false;
        }
    }
}
