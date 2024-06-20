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
 * The {@link ProcessManager} is responsible for connecting a process created by
 * a mojo (usually a forked one but thats not required) to the IDE and allows
 * some visual representation but also some customization using callbacks.
 */
public interface ProcessManager {

    /**
     * Create a new {@link BuildProcess} given the {@link BuildProcessContext}
     * calling any supported callbacks.
     *
     * @param context the context to use
     * @return the created {@link BuildProcess} that can be used to further control
     *         the process e.g inform about termination or <code>null</code> if no
     *         process can be created, for example this can happen because required
     *         callbacks are not supported for this kind of launch.
     */
    BuildProcess newProcess(BuildProcessContext context);
}
