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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * <p>Resources interface.</p>
 * <p>
 * This API provides a modern, flexible way to manage build resources using Path-based methods.
 * It separates resource management concerns from logging/messaging functionality and provides
 * better control over file operations during the build process.
 * </p>
 * <p>
 * Example usage:
 * </p>
 * <pre>
 * // Convert relative path to absolute Path
 * Path sourceFile = resources.getPath("src/main/java/Example.java");
 * Path targetFile = resources.getPath("target/classes/Example.class");
 *
 * // Copy source to target only if needed
 * resources.copy(sourceFile, targetFile);
 *
 * // Mark generated file as derived
 * resources.markDerived(targetFile);
 * </pre>
 */
public interface Resources {

    /**
     * Returns <code>true</code> if the file has changed since last build.
     * <p>
     * <strong>Important:</strong> This is a "best effort" hint and should primarily be used
     * for user-editable source files where you want to detect changes. For generated
     * files or when there is a clear input/output relationship, prefer using
     * {@link #isUptodate(Path, Path)} instead, as it handles cases where target files
     * may be deleted or modified outside of the build process.
     * </p>
     *
     * @param file the file to check for changes
     * @return <code>true</code> if the file has changed or if the file is not under basedir
     */
    boolean hasDelta(Path file);

    /**
     * Indicates that the file or folder content has been modified during the build.
     * <p>
     * This method should be called when file content is modified through means other
     * than {@link #newOutputStream(Path)} or {@link #newOutputStream(Path, boolean)}.
     * Files changed using OutputStreams returned by those methods do not need to be
     * explicitly refreshed.
     * </p>
     *
     * @param file the file that was modified
     */
    void refresh(Path file);

    /**
     * Returns a new OutputStream that writes to the specified file.
     * <p>
     * Files changed using OutputStream returned by this method do not need to be
     * explicitly refreshed using {@link #refresh(Path)}.
     * </p>
     * <p>
     * As an optimization, some implementations may attempt to avoid writing to the file
     * if the file content has not changed. This ensures that file timestamps are only
     * updated when actual changes occur, which is important for incremental build performance.
     * This optimization is implementation-dependent and may not apply to all implementations.
     * </p>
     *
     * @param file the file to write to
     * @return an OutputStream for writing to the file
     * @throws IOException if an I/O error occurs
     */
    OutputStream newOutputStream(Path file) throws IOException;

    /**
     * Returns a new OutputStream that writes to the specified file and optionally
     * marks it as derived.
     * <p>
     * Files changed using OutputStream returned by this method do not need to be
     * explicitly refreshed using {@link #refresh(Path)}.
     * </p>
     * <p>
     * As an optimization, some implementations may attempt to avoid writing to the file
     * if the file content has not changed. This optimization is implementation-dependent
     * and may not apply to all implementations.
     * </p>
     * <p>
     * When <code>derived</code> is <code>true</code>, the file is marked as generated/derived,
     * which can be used by IDEs to warn users if they attempt to edit the file.
     * This is useful for code generators that will overwrite any manual changes on
     * subsequent builds.
     * </p>
     *
     * @param file the file to write to
     * @param derived <code>true</code> to mark the file as derived/generated
     * @return an OutputStream for writing to the file
     * @throws IOException if an I/O error occurs
     */
    OutputStream newOutputStream(Path file, boolean derived) throws IOException;

    /**
     * Returns <code>true</code> if the target file exists and is up-to-date compared
     * to the source file.
     * <p>
     * More specifically, this method returns <code>true</code> when both target and
     * source files exist, do not have changes since last incremental build, and the
     * target file was last modified later than the source file. Returns <code>false</code>
     * in all other cases.
     * </p>
     * <p>
     * This method should be preferred over {@link #hasDelta(Path)} when there is a
     * clear input/output relationship between files, as it properly handles cases
     * where the target file may be deleted or modified outside of the build process.
     * </p>
     *
     * @param target the target/output file
     * @param source the source/input file
     * @return <code>true</code> if the target is up-to-date with the source
     */
    boolean isUptodate(Path target, Path source);

    /**
     * Converts a relative path string to an absolute Path based on the build context basedir.
     * <p>
     * This method is useful for converting relative paths (e.g., "src/main/java/Example.java")
     * to absolute Paths that can be used with other methods in this interface.
     * </p>
     *
     * @param relpath the relative path string
     * @return the absolute Path resolved against the build context basedir
     */
    Path getPath(String relpath);

    /**
     * Marks a file as derived (generated) by the build process.
     * <p>
     * This is useful for code generators and other build plugins that create files
     * that should not be edited manually. IDEs can use this information to warn
     * users if they attempt to modify derived files, as such changes will typically
     * be overwritten on the next build.
     * </p>
     * <p>
     * Note: In the default implementation, this is a no-op. Custom implementations
     * (e.g., IDE integrations) may provide actual derived file tracking.
     * </p>
     *
     * @param file the file to mark as derived
     */
    void markDerived(Path file);

    /**
     * Copies the source file to the target file if the target is not up-to-date.
     * <p>
     * This is a convenience method that combines {@link #isUptodate(Path, Path)}
     * checking with file copying using {@link #newOutputStream(Path)}. The copy
     * only occurs when the target does not exist or is not up-to-date with the source.
     * </p>
     * <p>
     * The implementation uses the Files API to open an input stream from the source
     * and copies it to an output stream obtained from {@link #newOutputStream(Path)},
     * ensuring that the target's timestamp is only updated if the content actually changes.
     * </p>
     *
     * @param source the source file to copy from
     * @param target the target file to copy to
     * @throws IOException if an I/O error occurs during the copy operation
     */
    void copy(Path source, Path target) throws IOException;
}
