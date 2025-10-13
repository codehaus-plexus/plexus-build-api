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

package org.codehaus.plexus.build;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.build.connect.BuildConnection;
import org.codehaus.plexus.build.connect.messages.RefreshMessage;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.Scanner;
import org.codehaus.plexus.util.io.CachingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filesystem based non-incremental build context implementation which behaves
 * as if all files were just created. More specifically,
 *
 * <ol>
 * <li>hasDelta returns <code>true</code> for all paths</li>
 * <li>newScanner returns Scanner that scans all files under provided
 * basedir</li>
 * <li>newDeletedScanner always returns empty scanner</li>
 * <li>isIncremental returns <code>false</code></li>
 * <li>getValue always returns the last set value in this session and only
 * stores to memory</li>
 * </ol>
 */
@Named("default")
@Singleton
public class DefaultBuildContext implements BuildContext {

    private final Logger logger = LoggerFactory.getLogger(DefaultBuildContext.class);
    // the legacy API requires the AbstractLogEnabled we just have it here to get
    // compile errors in case it is missing from the classpath!
    @SuppressWarnings("unused")
    private static final AbstractLogEnabled DUMMY = null;

    private final Map<String, Object> contextMap = new ConcurrentHashMap<>();
    private org.sonatype.plexus.build.incremental.BuildContext legacy;
    private BuildConnection connection;
    private LegacySupport legacySupport;

    /**
     * @param legacy        the legacy API we delegate to by default, this allow us
     *                      to support "older" plugins and implementors of the API
     *                      while still having a way to move forward!
     * @param connection    the connection we use to forward refresh events
     * @param legacySupport legacy support to get the current session
     */
    @Inject
    public DefaultBuildContext(
            org.sonatype.plexus.build.incremental.BuildContext legacy,
            BuildConnection connection,
            LegacySupport legacySupport) {
        this.legacy = legacy;
        this.connection = connection;
        this.legacySupport = legacySupport;
    }

    /** {@inheritDoc} */
    public boolean hasDelta(String relpath) {
        return legacy.hasDelta(relpath);
    }

    /**
     * <p>hasDelta.</p>
     *
     * @param file a {@link java.io.File} object.
     * @return a boolean.
     */
    public boolean hasDelta(File file) {
        return legacy.hasDelta(file);
    }

    /**
     * <p>hasDelta.</p>
     *
     * @param relpaths a {@link java.util.List} object.
     * @return a boolean.
     */
    public boolean hasDelta(List<String> relpaths) {
        return legacy.hasDelta(relpaths);
    }

    /** {@inheritDoc} */
    public OutputStream newFileOutputStream(File file) throws IOException {
        if (isDefaultImplementation()) {
            return new CachingOutputStream(file.toPath());
        }
        return legacy.newFileOutputStream(file);
    }

    /**
     * @return <code>true</code> if the legacy is the default implementation and we
     *         can safely override/change behavior here, or <code>false</code> if a
     *         custom implementation is used and full delegation is required.
     */
    private boolean isDefaultImplementation() {
        return legacy.getClass().equals(org.sonatype.plexus.build.incremental.DefaultBuildContext.class);
    }

    /** {@inheritDoc} */
    public Scanner newScanner(File basedir) {
        return legacy.newScanner(basedir);
    }

    /** {@inheritDoc} */
    public void refresh(File file) {
        legacy.refresh(file);
        connection.send(new RefreshMessage(file.toPath()), legacySupport.getSession());
    }

    /** {@inheritDoc} */
    public Scanner newDeleteScanner(File basedir) {
        return legacy.newDeleteScanner(basedir);
    }

    /** {@inheritDoc} */
    public Scanner newScanner(File basedir, boolean ignoreDelta) {
        return legacy.newScanner(basedir, ignoreDelta);
    }

    /**
     * <p>isIncremental.</p>
     *
     * @return a boolean.
     */
    public boolean isIncremental() {
        return legacy.isIncremental();
    }

    /** {@inheritDoc} */
    public Object getValue(String key) {
        return contextMap.get(key);
    }

    /** {@inheritDoc} */
    public void setValue(String key, Object value) {
        contextMap.put(key, value);
    }

    /** {@inheritDoc} */
    public void addError(File file, int line, int column, String message, Throwable cause) {
        addMessage(file, line, column, message, Severity.ERROR, cause);
    }

    /** {@inheritDoc} */
    public void addWarning(File file, int line, int column, String message, Throwable cause) {
        addMessage(file, line, column, message, Severity.WARNING, cause);
    }

    private String getMessage(File file, int line, int column, String message) {
        return file.getAbsolutePath() + " [" + line + ':' + column + "]: " + message;
    }

    /** {@inheritDoc} */
    @Override
    public void addMessage(File file, int line, int column, String message, Severity severity, Throwable cause) {
        if (isDefaultImplementation()) {
            switch (severity) {
                case ERROR:
                    logger.error(getMessage(file, line, column, message), cause);
                    return;
                case WARNING:
                    logger.warn(getMessage(file, line, column, message), cause);
                    return;
                default:
                    logger.debug(getMessage(file, line, column, message), cause);
                    return;
            }
        }
        legacy.addMessage(file, line, column, message, severity.getValue(), cause);
    }

    /** {@inheritDoc} */
    @Override
    @Deprecated
    public void addMessage(File file, int line, int column, String message, int severity, Throwable cause) {
        addMessage(file, line, column, message, Severity.fromValue(severity), cause);
    }

    /** {@inheritDoc} */
    public void removeMessages(File file) {
        if (isDefaultImplementation()) {
            return;
        }
        legacy.removeMessages(file);
    }

    /** {@inheritDoc} */
    public boolean isUptodate(File target, File source) {
        return legacy.isUptodate(target, source);
    }
}
