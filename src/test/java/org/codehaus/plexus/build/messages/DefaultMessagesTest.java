package org.codehaus.plexus.build.messages;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.build.BuildContext;
import org.codehaus.plexus.build.RecordingProxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultMessagesTest {

    private final RecordingProxy<BuildContext> context = RecordingProxy.of(BuildContext.class);
    private final DefaultMessages messages = new DefaultMessages(context.get());

    @Test
    void errorIsForwardedWithErrorSeverity() {
        Path path = Paths.get("src", "A.java");
        Throwable cause = new IllegalStateException();

        messages.error(path).line(3).column(7).cause(cause).create("broken");

        RecordingProxy.Call call = context.calls().get(0);
        assertEquals("addMessage", call.method);
        assertEquals(Arrays.asList(path.toFile(), 3, 7, "broken", BuildContext.SEVERITY_ERROR, cause), call.args);
    }

    @Test
    void severityFollowsTheMessageType() {
        Path path = Paths.get("x");

        messages.warning(path).create("w");
        messages.info(path).create("i");
        messages.build(MessageType.ERROR, path).create("e");

        List<Object> severities = new ArrayList<>();
        context.calls().forEach(call -> severities.add(call.args.get(4)));
        assertEquals(Arrays.asList(BuildContext.SEVERITY_WARNING, 0, BuildContext.SEVERITY_ERROR), severities);
    }

    @Test
    void messageWithoutPathIsOnlyLogged() {
        messages.error(null).create("no file");

        assertEquals(Collections.emptyList(), context.calls());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "\t"})
    void blankTextIsRejected(String text) {
        MessageBuilder builder = messages.error(Paths.get("x"));

        assertThrows(IllegalArgumentException.class, () -> builder.create(text));
        assertThrows(IllegalArgumentException.class, () -> builder.create(null));
        assertEquals(Collections.emptyList(), context.calls());
    }

    @Test
    void clearRemovesTheMessagesOfOneFile() {
        Path path = Paths.get("x");

        messages.clear(path);
        messages.clear(null);
        messages.clearAll();

        assertEquals(Collections.singletonList("removeMessages"), context.methods());
        assertEquals(
                Collections.singletonList((Object) path.toFile()),
                context.calls().get(0).args);
    }

    @Test
    void builderPassesEveryFieldToTheConsumer() {
        List<Message> created = new ArrayList<>();
        Path path = Paths.get("p");
        Throwable cause = new RuntimeException();

        new MessageBuilder(MessageType.WARNING, path, created::add)
                .line(1)
                .column(2)
                .cause(cause)
                .create("text");

        Message message = created.get(0);
        assertEquals(MessageType.WARNING, message.getType());
        assertSame(path, message.getPath());
        assertEquals(1, message.getLine());
        assertEquals(2, message.getColumn());
        assertEquals("text", message.getMessage());
        assertSame(cause, message.getCause());
    }

    @Test
    void builderDefaultsToNoPosition() {
        List<Message> created = new ArrayList<>();

        new MessageBuilder(MessageType.INFO, null, created::add).create("text");

        assertEquals(0, created.get(0).getLine());
        assertEquals(0, created.get(0).getColumn());
        assertNull(created.get(0).getCause());
    }

    @Test
    void toStringShowsTheKnownPosition() {
        Path path = Paths.get("f.txt");
        String absolute = path.toAbsolutePath().toString();

        assertEquals(absolute + " [2:5]: m", message(path, 2, 5, "m").toString());
        assertEquals(absolute + " [2]: m", message(path, 2, 0, "m").toString());
        assertEquals(absolute + " [:5]: m", message(path, 0, 5, "m").toString());
        assertEquals(absolute + ": m", message(path, 0, 0, "m").toString());
        assertEquals("m", message(null, 0, 0, "m").toString());
        assertEquals(absolute, message(path, 0, 0, null).toString());
    }

    private static Message message(Path path, int line, int column, String text) {
        return new Message(MessageType.ERROR, path, line, column, text, null);
    }
}
