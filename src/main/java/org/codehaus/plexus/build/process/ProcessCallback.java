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
 * A {@link ProcessCallback} can be implemented by a context if it is able to
 * accept additional environment variables to be set on a process they create.
 */
public interface ProcessCallback {

    /**
     * Request to set an additional environment variable, the context provider is
     * free to reject the request
     *
     * @param variable
     * @param value
     * @return <code>true</code> if the context accepted the request and will add
     *         the environment variable to the launch, false otherwise.
     */
    boolean setEnvironmentVariable(String variable, String value);
}
