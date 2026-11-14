package org.codehaus.plexus.build.connect;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.build.connect.messages.InitMessage;
import org.codehaus.plexus.build.connect.messages.Message;
import org.codehaus.plexus.build.connect.messages.MojoMessage;
import org.codehaus.plexus.build.connect.messages.ProjectMessage;
import org.codehaus.plexus.build.connect.messages.ProjectsMessage;
import org.codehaus.plexus.build.connect.messages.SessionMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventListenerTest {

    @TempDir
    Path basedir;

    @Test
    void ignoresEventsWithoutAnInitReply() throws Exception {
        RecordingConnection connection = new RecordingConnection(null);
        EventListener listener = new EventListener(connection);

        listener.init(Collections::emptyMap);
        listener.onEvent(event(ExecutionEvent.Type.SessionStarted, session(), null, null));

        assertEquals(Collections.singletonList(InitMessage.class), connection.types());
    }

    @Test
    void sessionEventsWithoutProjectInfos() throws Exception {
        RecordingConnection connection = new RecordingConnection(Collections.emptyMap());
        EventListener listener = new EventListener(connection);
        MavenSession session = session();

        listener.init(Collections::emptyMap);
        listener.onEvent(event(ExecutionEvent.Type.SessionStarted, session, null, null));
        listener.onEvent(event(ExecutionEvent.Type.SessionEnded, session, null, null));

        assertEquals(Arrays.asList(InitMessage.class, SessionMessage.class, SessionMessage.class), connection.types());
        SessionMessage started = (SessionMessage) connection.sent.get(1);
        SessionMessage ended = (SessionMessage) connection.sent.get(2);
        assertTrue(started.isSessionStart());
        assertFalse(ended.isSessionStart());
        assertEquals(basedir.toString(), started.getExecutionRootDirectory());
        assertEquals(session, connection.sessions.get(1));
    }

    @Test
    void sessionStartSendsProjectInfosWhenAsked() throws Exception {
        RecordingConnection connection =
                new RecordingConnection(Collections.singletonMap(Configuration.CONFIG_SEND_PROJECTS, "true"));
        EventListener listener = new EventListener(connection);
        MavenSession session = session();

        listener.init(Collections::emptyMap);
        listener.onEvent(event(ExecutionEvent.Type.SessionStarted, session, null, null));

        assertEquals(Arrays.asList(InitMessage.class, SessionMessage.class, ProjectsMessage.class), connection.types());
        List<ProjectsMessage.ProjectInfo> projects =
                ((ProjectsMessage) connection.sent.get(2)).projects().collect(Collectors.toList());
        assertEquals(1, projects.size());
        assertEquals("g", projects.get(0).getGroupId());
        assertEquals("a", projects.get(0).getArtifactId());
        assertEquals("1", projects.get(0).getVersion());
        assertEquals(basedir, projects.get(0).getBaseDir());
        assertTrue(projects.get(0).getModel().contains("<artifactId>a</artifactId>"));
    }

    @Test
    void projectAndMojoEventsAreForwarded() throws Exception {
        RecordingConnection connection = new RecordingConnection(Collections.emptyMap());
        EventListener listener = new EventListener(connection);
        MavenSession session = session();
        MojoExecution execution = new MojoExecution(new Plugin(), "goal", "id");

        listener.init(Collections::emptyMap);
        listener.onEvent(event(ExecutionEvent.Type.ProjectStarted, session, session.getCurrentProject(), null));
        listener.onEvent(event(ExecutionEvent.Type.MojoFailed, session, null, execution));
        listener.onEvent(event(ExecutionEvent.Type.ForkStarted, session, null, null));
        listener.onEvent("not an execution event");

        assertEquals(Arrays.asList(InitMessage.class, ProjectMessage.class, MojoMessage.class), connection.types());
        assertEquals(ProjectMessage.EventType.ProjectStarted, ((ProjectMessage) connection.sent.get(1)).getType());
        assertEquals(MojoMessage.EventType.MojoFailed, ((MojoMessage) connection.sent.get(2)).getType());
        assertNull(connection.sessions.get(0), "init has no session yet");
    }

    private MavenSession session() {
        MavenProject project = new MavenProject();
        project.setGroupId("g");
        project.setArtifactId("a");
        project.setVersion("1");
        project.setFile(new File(basedir.toFile(), "pom.xml"));
        MavenExecutionRequest request = new DefaultMavenExecutionRequest();
        request.setBaseDirectory(basedir.toFile());
        @SuppressWarnings("deprecation")
        MavenSession session =
                new MavenSession(null, request, new DefaultMavenExecutionResult(), Collections.singletonList(project));
        session.setCurrentProject(project);
        return session;
    }

    private static ExecutionEvent event(
            ExecutionEvent.Type type, MavenSession session, MavenProject project, MojoExecution execution) {
        return new ExecutionEvent() {
            @Override
            public Type getType() {
                return type;
            }

            @Override
            public MavenSession getSession() {
                return session;
            }

            @Override
            public MavenProject getProject() {
                return project;
            }

            @Override
            public MojoExecution getMojoExecution() {
                return execution;
            }

            @Override
            public Exception getException() {
                return null;
            }
        };
    }

    /** Records what is sent and answers the init message with a fixed configuration, or with nothing. */
    private static final class RecordingConnection implements BuildConnection {
        private final Map<String, String> initReply;
        private final List<Message> sent = new ArrayList<>();
        private final List<MavenSession> sessions = new ArrayList<>();

        RecordingConnection(Map<String, String> initReply) {
            this.initReply = initReply;
        }

        @Override
        public Message send(Message message, MavenSession mavenSession) {
            sent.add(message);
            sessions.add(mavenSession);
            if (message instanceof InitMessage && initReply != null) {
                return Message.replyTo(message, initReply);
            }
            return null;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        List<Class<?>> types() {
            return sent.stream().map(Object::getClass).collect(Collectors.toList());
        }
    }
}
