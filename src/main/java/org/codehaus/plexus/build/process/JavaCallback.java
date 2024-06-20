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

import java.nio.file.Path;

/**
 * A callback for a context that is working around java processes to be started,
 * a {@link BuildProcessContext} should implement this if it allows to further
 * customize a process that is running a java VM. Depending on the launch type
 * (e.g embedded or forked) some options might not be possible and can maybe
 * rejected.
 */
public interface JavaCallback {

    /**
     * Request to add a system property to the launch.
     *
     * @param key   the key
     * @param value the value
     * @return <code>true</code> if the request was accepted, false otherwise.
     */
    boolean setSystemProperty(String key, String value);

    /**
     * Request to add a VM option to the launch.
     *
     * @param option the VM option to add
     * @param value  the value or <code>null</code> if this option has no value
     * @return <code>true</code> if the request was accepted, false otherwise.
     */
    boolean setVMOption(String option, String value);

    /**
     * Request to add a new classpath entry to the launch
     *
     * @param extraClasspath the extra classpath element
     *
     * @return <code>true</code> if the request was accepted, false otherwise.
     */
    boolean addClasspathEntry(Path extraClasspath);
}
