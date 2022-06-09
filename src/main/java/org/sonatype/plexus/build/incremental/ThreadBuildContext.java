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

package org.sonatype.plexus.build.incremental;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.codehaus.plexus.util.Scanner;

/**
 * BuildContext implementation that delegates actual work to thread-local
 * build context set using {@link #setThreadBuildContext(BuildContext)}.
 * {@link org.sonatype.plexus.build.incremental.DefaultBuildContext} is used if no thread local build context was set.
 *
 * Note that plexus component metadata is not generated for this implementation.
 * Apparently, older version of plexus used by maven-filtering and likely
 * other projects, does not honour "default" role-hint.
 *
 * @author slachiewicz
 * @version $Id: $Id
 * @since 0.0.8
 */
public class ThreadBuildContext implements BuildContext {

  private static final ThreadLocal<BuildContext> threadContext = new ThreadLocal<BuildContext>(){
    @Override
    protected BuildContext initialValue() {
      return defaultContext;
    }
  };

  private static final DefaultBuildContext defaultContext = new DefaultBuildContext();

  /**
   * <p>getContext.</p>
   *
   * @return a {@link org.sonatype.plexus.build.incremental.BuildContext} object.
   */
  public static BuildContext getContext() {
    return threadContext.get();
  }

  /**
   * <p>setThreadBuildContext.</p>
   *
   * @param context a {@link org.sonatype.plexus.build.incremental.BuildContext} object.
   */
  public static void setThreadBuildContext(BuildContext context) {
    threadContext.set(context);
  }

  /** {@inheritDoc} */
  public boolean hasDelta(String relPath) {
    return getContext().hasDelta(relPath);
  }

  /**
   * <p>hasDelta.</p>
   *
   * @param file a {@link java.io.File} object.
   * @return a boolean.
   */
  public boolean hasDelta(File file) {
    return getContext().hasDelta(file);
  }

  /**
   * <p>hasDelta.</p>
   *
   * @param relPaths a {@link java.util.List} object.
   * @return a boolean.
   */
  public boolean hasDelta(List<String> relPaths) {
    return getContext().hasDelta(relPaths);
  }

  /** {@inheritDoc} */
  public Scanner newDeleteScanner(File basedir) {
    return getContext().newDeleteScanner(basedir);
  }

  /** {@inheritDoc} */
  public OutputStream newFileOutputStream(File file) throws IOException {
    return getContext().newFileOutputStream(file);
  }

  /** {@inheritDoc} */
  public Scanner newScanner(File basedir) {
    return getContext().newScanner(basedir);
  }

  /** {@inheritDoc} */
  public Scanner newScanner(File basedir, boolean ignoreDelta) {
    return getContext().newScanner(basedir, ignoreDelta);
  }

  /** {@inheritDoc} */
  public void refresh(File file) {
    getContext().refresh(file);
  }

  /** {@inheritDoc} */
  public Object getValue(String key) {
    return getContext().getValue(key);
  }

  /**
   * <p>isIncremental.</p>
   *
   * @return a boolean.
   */
  public boolean isIncremental() {
    return getContext().isIncremental();
  }

  /** {@inheritDoc} */
  public void setValue(String key, Object value) {
    getContext().setValue(key, value);
  }

  /** {@inheritDoc} */
  public void addMessage(File file, int line, int column, String message, int severity, Throwable cause) {
    getContext().addMessage(file, line, column, message, severity, cause);
  }

  /** {@inheritDoc} */
  public void removeMessages(File file) {
    getContext().removeMessages(file);
  }

  /** {@inheritDoc} */
  public void addWarning(File file, int line, int column, String message, Throwable cause) {
    addMessage(file, line, column, message, BuildContext.SEVERITY_WARNING, cause);
  }

  /** {@inheritDoc} */
  public void addError(File file, int line, int column, String message, Throwable cause) {
    addMessage(file, line, column, message, BuildContext.SEVERITY_ERROR, cause);
  }

  /** {@inheritDoc} */
  public boolean isUptodate(File target, File source) {
    return getContext().isUptodate(target, source);
  }
}
