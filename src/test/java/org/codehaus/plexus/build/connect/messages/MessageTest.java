package org.codehaus.plexus.build.connect.messages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageTest {

    @Test
    void roundTripKeepsSessionThreadAndPayload() {
        Map<String, String> payload = new LinkedHashMap<>();
        payload.put("key", "value");
        payload.put("empty", "");
        payload.put("nullValue", null);
        payload.put("unicode", "zażółć ☃");
        Message message = new Message("session-1", 42L, payload);

        Message decoded = Message.decode(message.serialize());

        assertEquals(message, decoded);
        assertEquals(message.hashCode(), decoded.hashCode());
        assertEquals("session-1", decoded.getSessionId());
        assertEquals("", decoded.getProperty("empty"));
        assertNull(decoded.getProperty("nullValue"));
        assertEquals("zażółć ☃", decoded.getProperty("unicode"));
    }

    @Test
    void roundTripOfAnEmptyPayload() {
        Message message = new Message("s", 1L, Collections.emptyMap());

        Message decoded = Message.decode(message.serialize());

        assertEquals(message, decoded);
        assertEquals(0L, decoded.keys().count());
    }

    @Test
    void decodeRestoresTheMessageSubclass() {
        Map<String, String> payload = Collections.singletonMap("k", "v");

        assertInstanceOf(SessionMessage.class, roundTrip(new SessionMessage("s", 1, payload)));
        assertInstanceOf(ProjectsMessage.class, roundTrip(new ProjectsMessage("s", 1, payload)));
        assertInstanceOf(RefreshMessage.class, roundTrip(new RefreshMessage("s", 1, payload)));
        assertInstanceOf(InitMessage.class, roundTrip(new InitMessage("s", 1, payload)));
        assertInstanceOf(ProjectMessage.class, roundTrip(new ProjectMessage("s", 1, payload)));
        assertInstanceOf(MojoMessage.class, roundTrip(new MojoMessage("s", 1, payload)));
        assertEquals(Message.class, roundTrip(new Message("s", 1, payload)).getClass());
    }

    @Test
    void serializeUsesTheGivenSessionId() {
        Message local = new RefreshMessage(Paths.get("a"));

        Message decoded = Message.decode(local.serialize("remote"));

        assertEquals("remote", decoded.getSessionId());
    }

    @Test
    void localMessageHasNoSessionId() {
        Message local = new RefreshMessage(Paths.get("a"));

        assertThrows(IllegalStateException.class, local::getSessionId);
        assertThrows(IllegalStateException.class, local::serialize);
    }

    @Test
    void replyKeepsSessionAndThread() {
        Message request = new Message("s", 7L, Collections.singletonMap("q", "1"));

        Message reply = Message.replyTo(request, Collections.singletonMap("a", "2"));
        Message emptyReply = Message.replyTo(request, null);

        assertEquals(request, Message.replyTo(request, Collections.singletonMap("q", "1")));
        assertEquals("s", reply.getSessionId());
        assertEquals("2", reply.getProperty("a"));
        assertNotEquals(request, reply);
        assertEquals(0L, emptyReply.keys().count());
    }

    @Test
    void messagesFromOneThreadShareTheThreadId() {
        Message first = Message.decode(new RefreshMessage(Paths.get("a")).serialize("s"));
        Message second = Message.decode(new RefreshMessage(Paths.get("b")).serialize("s"));

        assertEquals(
                Message.replyTo(first, null),
                Message.replyTo(second, null),
                "replies differ only by payload, so equal empty replies mean equal thread ids");
    }

    @Test
    void propertyDefaults() {
        Message message = new Message("s", 1L, Collections.singletonMap("flag", "true"));

        assertEquals("fallback", message.getProperty("missing", "fallback"));
        assertTrue(message.getBooleanProperty("flag"));
        assertFalse(message.getBooleanProperty("missing"));
        assertTrue(message.getBooleanProperty("missing", true));
        assertTrue(message.getBooleanProperty("flag", false));
    }

    @Test
    void refreshMessageCarriesTheAbsolutePath() {
        Path relative = Paths.get("target", "x.txt");

        RefreshMessage message = (RefreshMessage) roundTrip(new RefreshMessage(relative), "s");

        assertEquals(relative.toAbsolutePath(), message.getPath());
    }

    @Test
    void projectMessageCarriesCoordinatesAndType() {
        MavenProject project = new MavenProject();
        project.setGroupId("g");
        project.setArtifactId("a");
        project.setVersion("1");
        project.setFile(Paths.get("module", "pom.xml").toAbsolutePath().toFile());

        ProjectMessage message =
                (ProjectMessage) roundTrip(new ProjectMessage(project, ExecutionEvent.Type.ProjectFailed), "s");

        assertEquals("g", message.getGroupId());
        assertEquals("a", message.getArtifactId());
        assertEquals("1", message.getVersion());
        assertEquals(Paths.get("module").toAbsolutePath(), message.getBaseDir());
        assertEquals(ProjectMessage.EventType.ProjectFailed, message.getType());
    }

    @Test
    void unknownEventTypeMapsToUnknown() {
        Map<String, String> payload = Collections.singletonMap("eventType", "SomethingNew");

        assertEquals(ProjectMessage.EventType.Unknown, new ProjectMessage("s", 1, payload).getType());
        assertEquals(MojoMessage.EventType.Unknown, new MojoMessage("s", 1, payload).getType());
        assertEquals(MojoMessage.EventType.Unknown, new MojoMessage("s", 1, Collections.emptyMap()).getType());
    }

    @Test
    void mojoMessageCarriesTheExecution() {
        Plugin plugin = new Plugin();
        plugin.setGroupId("org.example");
        plugin.setArtifactId("example-maven-plugin");
        plugin.setVersion("2.0");
        MojoExecution execution = new MojoExecution(plugin, "compile", "default-compile");
        execution.setLifecyclePhase("compile");

        MojoMessage message =
                (MojoMessage) roundTrip(new MojoMessage(execution, ExecutionEvent.Type.MojoSucceeded), "s");

        assertEquals("org.example", message.getGroupId());
        assertEquals("example-maven-plugin", message.getArtifactId());
        assertEquals("2.0", message.getVersion());
        assertEquals("default-compile", message.getExecutionId());
        assertEquals("compile", message.getLifecyclePhase());
        assertEquals("compile", message.getGoal());
        assertEquals(MojoMessage.EventType.MojoSucceeded, message.getType());
    }

    @Test
    void initMessageFlattensStringsAndProperties() {
        Properties properties = new Properties();
        properties.setProperty("p1", "v1");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("plain", "value");
        data.put("props", properties);
        data.put("ignored", 42);
        EventSpy.Context context = () -> data;

        Message message = roundTrip(new InitMessage(context), "s");

        assertEquals("value", message.getProperty("plain"));
        assertEquals("v1", message.getProperty("props.p1"));
        assertNull(message.getProperty("ignored"));
        assertEquals(2L, message.keys().count());
    }

    private static Message roundTrip(Message message) {
        return Message.decode(message.serialize());
    }

    private static Message roundTrip(Message message, String sessionId) {
        return Message.decode(message.serialize(sessionId));
    }
}
