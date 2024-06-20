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
 * A {@link BuildProcessContext} is used to create a {@link BuildProcess}, it
 * can implement any number of specialized callbacks that allow to further
 * customize the about to executed process, that allows IDEs to install
 * additional code to track progress or getting notified about results.
 */
public interface BuildProcessContext {

    /**
     * Provides the name of process that is created, this is something that might be
     * presented to the user in an IDE
     *
     * @return the name
     */
    String getName();

    /**
     * Determines if termination of this process is possible, an IDE will possibly
     * present a way to terminate an individual process. A request to terminate the
     * process is then reflected in the process but it is the responsibility of the
     * implementation to act on it.
     *
     * @return <code>true</code>
     */
    boolean canTerminate();
}
