/*
 * Copyright 2016 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.json.JSONObject;

/**
 * JSON utils helping to handle JSON-Java object transformation, e.g. for
 * persisting.
 *
 * @author jejkal
 */
public final class JSONUtils {

    /**
     * Convert the provided map into a JSON object.
     *
     * @param pMap The map to convert.
     *
     * @return The JSON object representing the map. If pMap is null, an object
     * representing an empty map is returned.
     */
    public static JSONObject mapToJson(Map<String, Object> pMap) {
        if (pMap != null) {
            return new JSONObject(pMap);
        }
        return new JSONObject(new HashMap<String, Object>());

    }

    /**
     * Convert the provided properties object into a JSON object.
     *
     * @param pProperties The properties object to convert.
     *
     * @return The JSON object representing the properties object. If
     * pProperties is null, an object representing an empty map is returned.
     */
    public static JSONObject propertiesToJson(Properties pProperties) {
        Properties internal = (pProperties != null) ? pProperties : new Properties();
        Enumeration<Object> keys = internal.keys();
        Map<String, Object> map = new HashMap<>();

        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            map.put(key, internal.get(key));
        }

        return mapToJson(map);
    }

    /**
     * Convert the provided JSON object into a map.
     *
     * @param pObject The JSON object to convert.
     *
     * @return The map representing the provided JSON object. If pObject is
     * null, an empty map is returned.
     */
    public static Map<String, Object> jsonToMap(JSONObject pObject) {
        if (pObject != null) {
            Iterator<String> keys = pObject.keys();
            Map<String, Object> map = new HashMap<>();

            while (keys.hasNext()) {
                String key = keys.next();
                map.put(key, pObject.get(key));
            }
            return map;
        }
        return new HashMap<>();
    }

    /**
     * Convert the provided JSON object into a properties object.
     *
     * @param pObject The JSON object to convert.
     *
     * @return The properties object representing the provided JSON object. If
     * pObject is null, an empty properties object is returned.
     */
    public static Properties jsonToProperties(JSONObject pObject) {
        if (pObject != null) {
            Iterator<String> keys = pObject.keys();
            Properties props = new Properties();

            while (keys.hasNext()) {
                String key = keys.next();
                props.put(key, pObject.get(key));
            }
            return props;
        }
        return new Properties();
    }

    /**
     * Convert the provided JSON object into a single JSON string.
     *
     * @param pObject The JSON object to convert.
     *
     * @return The string representing the provided JSON object. If pObject is
     * null, an string representing an object with no content is returned.
     */
    public static String jsonObjectToString(JSONObject pObject) {
        if (pObject != null) {
            return pObject.toString();
        }
        return new JSONObject().toString();
    }

    /**
     * Convert the provided properties object into a single JSON string.
     *
     * @param pProperties The properties object to convert.
     *
     * @return The string representing the provided properties object. If
     * pObject is null, an string representing an object with no content is
     * returned.
     */
    public static String propertiesToJsonString(Properties pProperties) {
        return jsonObjectToString(propertiesToJson(pProperties));
    }

    /**
     * Convert the provided JSON string into a map.
     *
     * @param pJsonString The JSON string to convert.
     *
     * @return The map represented by the provided string. If pJsonString is
     * null, an empty map is returned.
     */
    public static Map<String, Object> jsonStringToMap(String pJsonString) {
        if (pJsonString != null) {
            return jsonToMap(new JSONObject(pJsonString));
        }
        return new HashMap<>();
    }

    /**
     * Convert the provided JSON string into a properties object.
     *
     * @param pJsonString The JSON string to convert.
     *
     * @return The properties object represented by the provided string. If
     * pJsonString is null, an empty properties object is returned.
     */
    public static Properties jsonStringToProperties(String pJsonString) {
        if (pJsonString != null) {
            return jsonToProperties(new JSONObject(pJsonString));
        }
        return new Properties();
    }
}
