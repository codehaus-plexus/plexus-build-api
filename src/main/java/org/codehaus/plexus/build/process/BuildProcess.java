/*
 * Copyright 2024 Christoph Läubrich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.plexus.build.process;

/**
 * A {@link BuildProcess} represents a process created inside a maven execution
 * that performs a specialized task and can be represented as something other
 * than a mojo executions, for example a mojo that executes tests in a forked VM
 * can handle multiple forks in one invocation. Also a process allows to be
 * controlled from the outside, e.g. the user can request to terminate it.
 */
public interface BuildProcess {

    /**
     * @return the name of the process
     */
    String getName();

    /**
     * Notify the manager that the given process was started
     */
    void notifyStarted();

    /**
     * @return true if the process is requested to terminate or has already
     *         terminated.
     */
    boolean isTerminated();

    /**
     * Notify the manager that the given process has finished with the given exit
     * code where an exit code of zero usually indicates a successful termination
     *
     * @param exitcode
     */
    void notifyFinished(int exitcode);
}
