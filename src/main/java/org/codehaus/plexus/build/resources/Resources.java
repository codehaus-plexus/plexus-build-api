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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * A {@link Path}-based API for the resource operations of a build context.
 *
 * <p>This API is deliberately separate from {@link org.codehaus.plexus.build.BuildContext}, which also carries
 * logging and scanner concerns. It keeps only the operations that have a clear input/output relationship and makes
 * their contract explicit.</p>
 *
 * <p>{@link #hasDelta(Path)} is a best effort hint: it is useful for user-editable sources, but a target that was
 * deleted or modified outside the build may not be noticed. Where there is an input and an output, prefer
 * {@link #isUptodate(Path, Path)} and {@link #newOutputStream(Path)}, which only update a file when its content
 * actually changes.</p>
 */
public interface Resources {

    /**
     * Returns whether the file has changed since the last build. This is a hint, not a guarantee; see the class
     * documentation for when to use it.
     *
     * @param file the file to check
     * @return {@code true} if the file has changed, or if it is outside the build context basedir
     */
    boolean hasDelta(Path file);

    /**
     * Signals that the file was modified during the build.
     *
     * @param file the file to refresh
     */
    void refresh(Path file);

    /**
     * Opens a stream that writes to the file, updating it only when the content changes.
     *
     * @param file the file to write
     * @return the output stream
     * @throws IOException if the stream cannot be opened
     */
    OutputStream newOutputStream(Path file) throws IOException;

    /**
     * Opens a stream that writes to the file and marks the file as derived through {@link #markDerived(Path)}, which
     * does nothing in the default implementation.
     *
     * @param file     the file to write
     * @param derived  whether the result is generated and may be overwritten by the next build
     * @return the output stream
     * @throws IOException if the stream cannot be opened
     */
    OutputStream newOutputStream(Path file, boolean derived) throws IOException;

    /**
     * Returns whether the target exists and is newer than the source, with no pending changes to either.
     *
     * @param target the output file
     * @param source the input file
     * @return {@code true} when the target is up to date
     */
    boolean isUptodate(Path target, Path source);

    /**
     * Resolves a build-context-relative path against the project basedir.
     *
     * @param relpath the relative path
     * @return the resolved path, absolute when a project basedir is available
     */
    Path getPath(String relpath);

    /**
     * Marks a file as generated, so an IDE can warn before a user edits it. The default implementation does nothing;
     * IDE integrations may override it.
     *
     * @param file the generated file
     */
    void markDerived(Path file);

    /**
     * Copies the source to the target when the target is not up to date. The parent directory is created if needed,
     * and the copy goes through {@link #newOutputStream(Path)} so implementations can keep their change tracking.
     *
     * @param source the source file
     * @param target the target file
     * @throws IOException if the copy fails
     */
    void copy(Path source, Path target) throws IOException;
}
