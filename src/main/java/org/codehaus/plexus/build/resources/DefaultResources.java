/*
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
import java.nio.file.Paths;

import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.build.BuildContext;

/**
 * A {@link Resources} implementation that delegates to the existing {@link BuildContext}, so plugins can adopt the
 * new API without waiting for the build tools that provide it.
 *
 * @since 1.1.0
 */
@Named("default")
@Singleton
public class DefaultResources implements Resources {

    private final BuildContext buildContext;

    private final LegacySupport legacySupport;

    @Inject
    public DefaultResources(BuildContext buildContext, LegacySupport legacySupport) {
        this.buildContext = buildContext;
        this.legacySupport = legacySupport;
    }

    @Override
    public boolean hasDelta(Path file) {
        return file != null && buildContext.hasDelta(file.toFile());
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
        OutputStream stream = newOutputStream(file);
        if (derived) {
            markDerived(file);
        }
        return stream;
    }

    @Override
    public boolean isUptodate(Path target, Path source) {
        return target != null && source != null && buildContext.isUptodate(target.toFile(), source.toFile());
    }

    @Override
    public Path getPath(String relpath) {
        if (relpath == null) {
            throw new IllegalArgumentException("relpath cannot be null");
        }
        if (legacySupport.getSession() != null) {
            MavenProject project = legacySupport.getSession().getCurrentProject();
            if (project != null && project.getBasedir() != null) {
                return project.getBasedir()
                        .toPath()
                        .resolve(relpath)
                        .toAbsolutePath()
                        .normalize();
            }
        }
        return Paths.get(relpath);
    }

    @Override
    public void markDerived(Path file) {
        // The default build context does not track derived files; IDE integrations override this.
    }

    @Override
    public void copy(Path source, Path target) throws IOException {
        if (source == null) {
            throw new IllegalArgumentException("source cannot be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("target cannot be null");
        }
        if (isUptodate(target, source)) {
            return;
        }
        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (InputStream in = Files.newInputStream(source);
                OutputStream out = newOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
