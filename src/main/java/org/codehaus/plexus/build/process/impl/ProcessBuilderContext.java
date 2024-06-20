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
package org.codehaus.plexus.build.process.impl;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.build.process.BuildProcess;
import org.codehaus.plexus.build.process.BuildProcessContext;
import org.codehaus.plexus.build.process.ProcessCallback;

/**
 * A simple implementation that uses a {@link ProcessBuilder} to fork a
 * childprocess and informs the {@link BuildProcess} about its execution and
 * outcome.
 */
public class ProcessBuilderContext implements ProcessCallback, BuildProcessContext {

    private String name;
    private boolean allowTermination;
    private ProcessBuilder builder;

    public ProcessBuilderContext(String name, boolean allowTermination, ProcessBuilder builder) {
        this.name = name;
        this.allowTermination = allowTermination;
        this.builder = builder;
    }

    @Override
    public boolean setEnvironmentVariable(String variable, String value) {
        builder.environment().put(variable, value);
        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canTerminate() {
        return allowTermination;
    }

    /**
     * Starts a new {@link Process} using the {@link ProcessBuilder} and notify the
     * given {@link BuildProcess}.
     *
     * @param buildProcess the build process to notify or <code>null</code> if no
     *                     further notification is desired.
     * @return the process started.
     * @throws IOException if starting the process failed.
     */
    public Process start(BuildProcess buildProcess) throws IOException {
        Process process = builder.start();
        if (buildProcess != null) {
            buildProcess.notifyStarted();
            Thread watcher = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (allowTermination) {
                            do {
                                if (buildProcess.isTerminated()) {
                                    // request termination
                                    process.destroy();
                                    break;
                                }
                            } while (!process.waitFor(200, TimeUnit.MILLISECONDS));
                        }
                        buildProcess.notifyFinished(process.waitFor());
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            });
            watcher.setName("Watcher thread for build process " + buildProcess.getName());
            watcher.setDaemon(true);
            watcher.start();
        }
        return process;
    }
}
