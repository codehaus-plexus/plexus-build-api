package org.codehaus.plexus.build;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.build.connect.BuildConnection;
import org.codehaus.plexus.build.connect.messages.RefreshMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultBuildContextTest {

    @TempDir
    Path dir;

    private final RecordingProxy<BuildConnection> connection = RecordingProxy.of(BuildConnection.class);
    private final RecordingProxy<LegacySupport> legacySupport = RecordingProxy.of(LegacySupport.class);

    /** With the legacy default implementation, values live in the context itself. */
    @Test
    void valuesAreKeptLocallyForTheDefaultLegacyContext() {
        DefaultBuildContext context = withDefaultLegacy();
        Object value = new Object();

        context.setValue("key", value);

        assertSame(value, context.getValue("key"));
        assertNull(context.getValue("other"));
    }

    @Test
    void unchangedContentDoesNotTouchTheFile() throws IOException {
        DefaultBuildContext context = withDefaultLegacy();
        File file = dir.resolve("out.txt").toFile();
        Files.write(file.toPath(), "same".getBytes(StandardCharsets.UTF_8));
        FileTime old = FileTime.fromMillis(1_000_000_000L);
        Files.setLastModifiedTime(file.toPath(), old);

        try (OutputStream out = context.newFileOutputStream(file)) {
            out.write("same".getBytes(StandardCharsets.UTF_8));
        }

        assertEquals(old, Files.getLastModifiedTime(file.toPath()));
    }

    @Test
    void changedContentIsWritten() throws IOException {
        DefaultBuildContext context = withDefaultLegacy();
        File file = dir.resolve("out.txt").toFile();
        Files.write(file.toPath(), "old".getBytes(StandardCharsets.UTF_8));

        try (OutputStream out = context.newFileOutputStream(file)) {
            out.write("new".getBytes(StandardCharsets.UTF_8));
        }

        assertArrayEquals("new".getBytes(StandardCharsets.UTF_8), Files.readAllBytes(file.toPath()));
    }

    @Test
    void refreshNotifiesTheConnectionWithTheCurrentSession() {
        MavenSession session = RecordingProxySession.SESSION;
        legacySupport.answer("getSession", session);
        DefaultBuildContext context = withDefaultLegacy();
        File file = dir.resolve("changed.txt").toFile();

        context.refresh(file);

        RecordingProxy.Call call = connection.calls().get(0);
        assertEquals("send", call.method);
        assertEquals(file.toPath(), ((RefreshMessage) call.args.get(0)).getPath());
        assertSame(session, call.args.get(1));
    }

    @Test
    void customLegacyContextReceivesEveryCall() throws IOException {
        RecordingProxy<org.sonatype.plexus.build.incremental.BuildContext> legacy =
                RecordingProxy.of(org.sonatype.plexus.build.incremental.BuildContext.class);
        legacy.answer("getValue", "from-legacy").answer("hasDelta", true).answer("isIncremental", true);
        DefaultBuildContext context = new DefaultBuildContext(legacy.get(), connection.get(), legacySupport.get());
        File file = new File("f");
        Throwable cause = new RuntimeException();

        context.setValue("k", "v");
        String value = context.getValue("k");
        context.addMessage(file, 1, 2, "m", BuildContext.SEVERITY_WARNING, cause);
        context.addError(file, 3, 4, "e", null);
        context.removeMessages(file);
        context.newFileOutputStream(file);
        boolean delta = context.hasDelta("path");
        boolean incremental = context.isIncremental();
        context.isUptodate(file, file);
        context.newScanner(file);
        context.newScanner(file, true);
        context.newDeleteScanner(file);

        assertEquals("from-legacy", value);
        assertTrue(delta);
        assertTrue(incremental);
        assertEquals(
                Arrays.asList(
                        "setValue",
                        "getValue",
                        "addMessage",
                        "addMessage",
                        "removeMessages",
                        "newFileOutputStream",
                        "hasDelta",
                        "isIncremental",
                        "isUptodate",
                        "newScanner",
                        "newScanner",
                        "newDeleteScanner"),
                legacy.methods());
        assertEquals(
                Arrays.asList(file, 1, 2, "m", BuildContext.SEVERITY_WARNING, cause),
                legacy.calls().get(2).args);
        assertEquals(BuildContext.SEVERITY_ERROR, legacy.calls().get(3).args.get(4));
        assertEquals(Collections.emptyList(), connection.calls());
    }

    private DefaultBuildContext withDefaultLegacy() {
        return new DefaultBuildContext(
                new org.sonatype.plexus.build.incremental.DefaultBuildContext(), connection.get(), legacySupport.get());
    }

    /** A session the refresh test can identify by reference; nothing reads from it. */
    private static final class RecordingProxySession {
        @SuppressWarnings("deprecation")
        static final MavenSession SESSION = new MavenSession(
                null,
                new org.apache.maven.execution.DefaultMavenExecutionRequest(),
                new org.apache.maven.execution.DefaultMavenExecutionResult(),
                Collections.emptyList());
    }
}
