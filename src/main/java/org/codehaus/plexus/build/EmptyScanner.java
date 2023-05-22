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

import java.io.File;
import java.util.Comparator;

import org.codehaus.plexus.util.Scanner;

/**
 * Scanner implementation never finds any files/directories.
 */
public class EmptyScanner implements Scanner {

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private final File basedir;

    /**
     * <p>Constructor for EmptyScanner.</p>
     *
     * @param basedir a {@link java.io.File} object.
     */
    public EmptyScanner(File basedir) {
        this.basedir = basedir;
    }

    /**
     * <p>addDefaultExcludes.</p>
     */
    public void addDefaultExcludes() {}

    /**
     * <p>getIncludedDirectories.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getIncludedDirectories() {
        return EMPTY_STRING_ARRAY;
    }

    /**
     * <p>getIncludedFiles.</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getIncludedFiles() {
        return EMPTY_STRING_ARRAY;
    }

    /**
     * <p>scan.</p>
     */
    public void scan() {}

    /**
     * <p>setExcludes.</p>
     *
     * @param excludes an array of {@link java.lang.String} objects.
     */
    public void setExcludes(String[] excludes) {}

    /**
     * <p>setIncludes.</p>
     *
     * @param includes an array of {@link java.lang.String} objects.
     */
    public void setIncludes(String[] includes) {}

    /**
     * <p>Getter for the field <code>basedir</code>.</p>
     *
     * @return a {@link java.io.File} object.
     */
    public File getBasedir() {
        return basedir;
    }

    /** {@inheritDoc} */
    @Override
    public void setFilenameComparator(Comparator<String> comparator) {}
}
