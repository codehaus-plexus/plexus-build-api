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
 * Callback targeting a JUnit execution.
 */
public interface JUnitCallback {

    enum JUnitVersion {
        JUNIT3,
        JUNIT4,
        JUNIT5;
    }

    /**
     * Request to add a new classpath entry to the junit execution, this might
     * contain additional classes needed to perform some actions.
     *
     * @param extraTestClasspath the extra classpath element
     *
     * @return <code>true</code> if the request was accepted, <code>false</code>
     *         otherwise.
     */
    boolean addTestClasspathEntry(Path extraTestClasspath);

    /**
     * Request to add a new TestExecutionListener specified by the full qualified
     * class name, this must either be a default one or one that can be loaded by
     * the extra test classpath added before.
     *
     * @param fqcn full qualified classname of the listener implementation
     * @return <code>true</code> if the listener was accepted, <code>false</code>
     *         otherwise.
     */
    boolean addTestExecutionListener(String fqcn);

    /**
     * @return the version of JUnit that will be executed
     */
    JUnitVersion getJUnitVersion();
}
