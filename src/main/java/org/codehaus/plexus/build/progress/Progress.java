package org.codehaus.plexus.build.progress;

/**
 * The {@link Progress} allows a mojo to report its current state and check if
 * the user has requested cancellation.
 */
public interface Progress {

    /**
     * Reports that the mojo has started the given task with the given amount of
     * work.
     *
     * @param task
     * @param work the amount of work that the mojo plan to report, if a value
     *             <code>&lt;= 0</code> is given the amount of work is assumed to be
     *             unknown.
     */
    void startTask(String task, int work);

    /**
     * Reports a given amount of work was processed
     *
     * @param work
     */
    void worked(int work);

    /**
     * Reports one unit of work to be processed
     */
    default void worked() {
        worked(1);
    }

    /**
     * Notifies the remaining amount of work that will be reported
     *
     * @param work
     */
    void setRemaining(int work);

    /**
     * This method should be used to check if the user has requested to finish the
     * current work and break out early.
     *
     * @return <code>true</code> if a cancel request is currently pending.
     */
    boolean isCancelRequested();
}
