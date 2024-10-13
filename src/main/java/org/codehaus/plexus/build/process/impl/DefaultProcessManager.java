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

import javax.inject.Named;

import java.util.Objects;

import org.codehaus.plexus.build.process.BuildProcess;
import org.codehaus.plexus.build.process.BuildProcessContext;
import org.codehaus.plexus.build.process.ProcessManager;

/**
 * The default implementation of a {@link ProcessManager} always return
 * <code>null</code> from its {@link #newProcess(BuildProcessContext)} method
 * regardless of what is passed in and only checks for <code>null</code>.
 */
@Named
public final class DefaultProcessManager implements ProcessManager {

    @Override
    public BuildProcess newProcess(BuildProcessContext context) {
        Objects.requireNonNull(context);
        return null;
    }
}
