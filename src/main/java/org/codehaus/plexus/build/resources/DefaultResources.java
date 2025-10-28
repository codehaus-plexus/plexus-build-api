/*
Copyright (c) 2008 Sonatype, Inc. All rights reserved.

This program is licensed to you under the Apache License Version 2.0,
and you may not use this file except in compliance with the Apache License Version 2.0.
You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing,
software distributed under the Apache License Version 2.0 is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
*/
package org.codehaus.plexus.build.resources;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.codehaus.plexus.build.BuildContext;

/**
 * Default implementation of the Resources interface.
 * <p>
 * This implementation delegates to the BuildContext for compatibility with existing
 * build infrastructure. It provides a transition path from the File-based API to the
 * modern Path-based API.
 * </p>
 */
@Named("default")
@Singleton
public class DefaultResources implements Resources {

    private final BuildContext buildContext;

    /**
     * Creates a new DefaultResources instance.
     *
     * @param buildContext the BuildContext to which operations will be delegated
     */
    @Inject
    public DefaultResources(BuildContext buildContext) {
        this.buildContext = buildContext;
    }

    @Override
    public boolean hasDelta(Path file) {
        if (file == null) {
            return false;
        }
        return buildContext.hasDelta(file.toFile());
    }

    @Override
    public void refresh(Path file) {
        if (file != null) {
            buildContext.refresh(file.toFile());
        }
    }

    @Override
    public OutputStream newOutputStream(Path file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file cannot be null");
        }
        return buildContext.newFileOutputStream(file.toFile());
    }

    @Override
    public OutputStream newOutputStream(Path file, boolean derived) throws IOException {
        OutputStream outputStream = newOutputStream(file);
        if (derived) {
            // Mark the file as derived after creating the output stream
            // The marking happens here so that implementations can wrap the stream
            // if needed to defer the marking until after successful write
            markDerived(file);
        }
        return outputStream;
    }

    @Override
    public boolean isUptodate(Path target, Path source) {
        if (target == null || source == null) {
            return false;
        }
        return buildContext.isUptodate(target.toFile(), source.toFile());
    }

    @Override
    public Path getPath(String relpath) {
        if (relpath == null) {
            throw new IllegalArgumentException("relpath cannot be null");
        }
        // Get the basedir from the build context by checking a known file
        // The BuildContext API doesn't expose basedir directly, so we need to work around this
        // For the default implementation, we'll resolve against the current working directory
        // Custom implementations should override this to use the actual basedir
        return java.nio.file.Paths.get(relpath);
    }

    @Override
    public void markDerived(Path file) {
        // No-op in the default implementation
        // Custom implementations (e.g., IDE integrations) can override this
        // to provide actual derived file tracking
    }

    @Override
    public void copy(Path source, Path target) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("target cannot be null");
        }

        // Only copy if the target is not up-to-date with the source
        if (!isUptodate(target, source)) {
            // Ensure parent directory exists
            Path parentDir = target.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            // Copy using Files API and newOutputStream to ensure proper change detection
            try (InputStream in = Files.newInputStream(source);
                    OutputStream out = newOutputStream(target)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
