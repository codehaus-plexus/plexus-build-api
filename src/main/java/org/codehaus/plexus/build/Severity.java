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

/**
 * Severity levels for build messages.
 *
 * @since 1.2.1
 */
public enum Severity {
    /**
     * Warning severity level.
     */
    WARNING(1),

    /**
     * Error severity level.
     */
    ERROR(2);

    private final int value;

    Severity(int value) {
        this.value = value;
    }

    /**
     * Returns the legacy integer value for this severity level.
     * This is provided for backward compatibility.
     *
     * @return the integer value
     */
    public int getValue() {
        return value;
    }

    /**
     * Converts a legacy integer severity value to a Severity enum.
     *
     * @param value the integer severity value
     * @return the corresponding Severity enum value
     * @throws IllegalArgumentException if the value doesn't correspond to a known severity
     */
    public static Severity fromValue(int value) {
        for (Severity severity : values()) {
            if (severity.value == value) {
                return severity;
            }
        }
        throw new IllegalArgumentException("Unknown severity value: " + value);
    }
}
