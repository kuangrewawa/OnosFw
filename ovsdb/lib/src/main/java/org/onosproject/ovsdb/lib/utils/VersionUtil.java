/*
 * Copyright 2015 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.ovsdb.lib.utils;

/**
 * Version utility class.
 */
public final class VersionUtil {

    private VersionUtil() {
    }

    public static final String DEFAULT_VERSION_STRING = "0.0.0";
    private static final String FORMAT = "(\\d+)\\.(\\d+)\\.(\\d+)";

    /**
     * Match varsion by the format.
     * @param version the version String
     */
    public static void versionMatch(String version) {
        if (!version.matches(FORMAT)) {
            throw new IllegalArgumentException("<" + version
                    + "> does not match format " + FORMAT);
        }
    }

    /**
     * Compare fromVersion and toVersion.
     * @param fromVersion the initial version
     * @param toVersion the end of the version
     */
    public static long versionCompare(String fromVersion, String toVersion) {
        Long fromNum = Long.parseLong(fromVersion.replace(".", ""));
        Long toNum = Long.parseLong(toVersion.replace(".", ""));
        return (fromNum - toNum);
    }
}
