/*
Copyright (c) 2025 Christoph Läubrich All rights reserved.

This program is licensed to you under the Apache License Version 2.0,
and you may not use this file except in compliance with the Apache License Version 2.0.
You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing,
software distributed under the Apache License Version 2.0 is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
*/
package org.codehaus.plexus.build.connect.messages;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.eventspy.EventSpy.Context;

/**
 * Message send to init the inital communication with the endpoints
 */
public class InitMessage extends Message {

    /**
     * Creates a message with inital information about the running maven system
     *
     * @param context the context settings to send to the endpoint
     */
    public InitMessage(Context context) {
        super(toMap(context));
    }

    private static Map<String, String> toMap(Context context) {
        Map<String, String> data = new LinkedHashMap<>();
        context.getData().forEach((k, v) -> {
            if (v instanceof String) {
                data.put(k, (String) v);
            }
            if (v instanceof Properties) {
                Properties properties = (Properties) v;
                for (String p : properties.stringPropertyNames()) {
                    data.put(k + "." + p, properties.getProperty(p));
                }
            }
        });
        return data;
    }

    InitMessage(String sessionId, long threadId, Map<String, String> payload) {
        super(sessionId, threadId, payload);
    }
}
