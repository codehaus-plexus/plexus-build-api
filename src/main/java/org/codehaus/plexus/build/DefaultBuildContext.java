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

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.Scanner;
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

    private final Map<String, Object> contextMap = new ConcurrentHashMap<>();
  private final Logger logger = LoggerFactory.getLogger(DefaultBuildContext.class);
  /** {@inheritDoc} */
  public boolean hasDelta(String relpath) {
    return true;
  }

  /**
   * <p>hasDelta.</p>
   *
   * @param file a {@link java.io.File} object.
   * @return a boolean.
   */
  public boolean hasDelta(File file) {
    return true;
  }

  /**
   * <p>hasDelta.</p>
   *
   * @param relpaths a {@link java.util.List} object.
   * @return a boolean.
   */
  public boolean hasDelta(List<String> relpaths) {
    return true;
  }

  /** {@inheritDoc} */
  public OutputStream newFileOutputStream(File file) throws IOException {
    return Files.newOutputStream(file.toPath());
  }

  /** {@inheritDoc} */
  public Scanner newScanner(File basedir) {
    DirectoryScanner ds = new DirectoryScanner();
    ds.setBasedir(basedir);
    return ds;
  }

  /** {@inheritDoc} */
  public void refresh(File file) {
    // do nothing
  }

  /** {@inheritDoc} */
  public Scanner newDeleteScanner(File basedir) {
    return new EmptyScanner(basedir);
  }

  /** {@inheritDoc} */
  public Scanner newScanner(File basedir, boolean ignoreDelta) {
    return newScanner(basedir);
  }

  /**
   * <p>isIncremental.</p>
   *
   * @return a boolean.
   */
  public boolean isIncremental() {
    return false;
  }

  /** {@inheritDoc} */
  public Object getValue(String key) {
      return contextMap.get(key);
  }

  /** {@inheritDoc} */
  public void setValue(String key, Object value) {
      contextMap.put(key, value);
  }

  private String getMessage(File file, int line, int column, String message) {
    return file.getAbsolutePath() + " [" + line + ':' + column + "]: " + message;
  }

  /** {@inheritDoc} */
  public void addError(File file, int line, int column, String message, Throwable cause) {
    addMessage(file, line, column, message, SEVERITY_ERROR, cause);
  }

  /** {@inheritDoc} */
  public void addWarning(File file, int line, int column, String message, Throwable cause) {
    addMessage(file, line, column, message, SEVERITY_WARNING, cause);
  }

  /** {@inheritDoc} */
  public void addMessage(File file, int line, int column, String message, int severity, Throwable cause) {
    switch(severity) {
      case BuildContext.SEVERITY_ERROR:
        logger.error(getMessage(file, line, column, message), cause);
        return;
      case BuildContext.SEVERITY_WARNING:
        logger.warn(getMessage(file, line, column, message), cause);
        return;
    }
    throw new IllegalArgumentException("severity=" + severity);
  }

  /** {@inheritDoc} */
  public void removeMessages(File file) {
  }

  /** {@inheritDoc} */
  public boolean isUptodate(File target, File source) {
    return target != null && target.exists() && source != null && source.exists()
        && target.lastModified() > source.lastModified();
  }
}
