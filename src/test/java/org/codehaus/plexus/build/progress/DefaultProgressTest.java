package org.codehaus.plexus.build.progress;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecution;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultProgressTest {

    private final DefaultProgress progress = new DefaultProgress(new MojoExecution(new Plugin(), "goal", "id"));

    @AfterEach
    void clearInterrupt() {
        Thread.interrupted();
    }

    @Test
    void cancelFollowsTheThreadInterruptFlag() {
        assertFalse(progress.isCancelRequested());

        Thread.currentThread().interrupt();

        assertTrue(progress.isCancelRequested());
        assertTrue(Thread.currentThread().isInterrupted(), "checking must not clear the flag");
    }

    @Test
    void miscountedWorkIsTolerated() {
        assertDoesNotThrow(() -> {
            progress.startTask("task", 2);
            progress.worked();
            progress.worked(0);
            progress.worked(5);
            progress.worked(-1);
            progress.setRemaining(0);
            progress.worked();
        });
    }
}
