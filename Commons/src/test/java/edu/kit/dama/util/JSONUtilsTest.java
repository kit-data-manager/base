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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jejkal
 */
public class JSONUtilsTest {

    private final static String KEY1 = "string";
    private final static String VALUE1 = "value";
    private final static String KEY2 = "one";
    private final static Integer VALUE2 = 1;

    private static Map<String, Object> generateData() {
        Map<String, Object> data = new HashMap<>();
        data.put(KEY1, VALUE1);
        data.put(KEY2, VALUE2);
        return data;
    }

    @Test
    public void testSerializationDeserialization() {
        JSONObject object = JSONUtils.mapToJson(generateData());
        Assert.assertEquals(2, object.keySet().size());
        Assert.assertEquals(VALUE1, object.get(KEY1));
        Assert.assertEquals(VALUE2, object.get(KEY2));
        ///
        Map<String, Object> map2 = JSONUtils.jsonToMap(object);
        Assert.assertEquals(2, map2.keySet().size());
        Assert.assertEquals(VALUE1, map2.get(KEY1));
        Assert.assertEquals(VALUE2, map2.get(KEY2));
        ///
        String jsonString = JSONUtils.jsonObjectToString(object);
        Assert.assertTrue(jsonString.length() > 10);
        ///
        Properties props = JSONUtils.jsonToProperties(object);
        Assert.assertEquals(2, props.keySet().size());
        Assert.assertEquals(VALUE1, props.get(KEY1));
        Assert.assertEquals(VALUE2, props.get(KEY2));
        ///
        Map<String, Object> map = JSONUtils.jsonStringToMap(jsonString);
        Assert.assertEquals(2, props.keySet().size());
        Assert.assertEquals(VALUE1, map.get(KEY1));
        Assert.assertEquals(VALUE2, map.get(KEY2));
        ///
        Properties props2 = JSONUtils.jsonStringToProperties(jsonString);
        Assert.assertEquals(2, props2.keySet().size());
        Assert.assertEquals(VALUE1, props2.get(KEY1));
        Assert.assertEquals(VALUE2, props2.get(KEY2));
        ///
        JSONObject object2 = JSONUtils.propertiesToJson(props);
        Assert.assertEquals(2, object2.keySet().size());
        Assert.assertEquals(VALUE1, object2.get(KEY1));
        Assert.assertEquals(VALUE2, object2.get(KEY2));
        ///
        String jsonString2 = JSONUtils.propertiesToJsonString(props);
        Assert.assertEquals(jsonString, jsonString2);
    }

    @Test
    public void testNullMapToJson() {
        JSONObject object = JSONUtils.mapToJson(null);
        Assert.assertNotNull(object);
        Assert.assertEquals(0, object.keySet().size());
    }

    @Test
    public void testNullPropertiesToJson() {
        JSONObject object = JSONUtils.propertiesToJson(null);
        Assert.assertNotNull(object);
        Assert.assertEquals(0, object.keySet().size());
    }

    @Test
    public void testNullStringToMap() {
        Map<String, Object> map = JSONUtils.jsonStringToMap(null);
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void testNullStringToProperties() {
        Properties props = JSONUtils.jsonStringToProperties(null);
        Assert.assertNotNull(props);
        Assert.assertTrue(props.isEmpty());
    }

    @Test
    public void testNullJsonToProperties() {
        Properties props = JSONUtils.jsonToProperties(null);
        Assert.assertNotNull(props);
        Assert.assertTrue(props.isEmpty());
    }

    @Test
    public void testNullJsonToMap() {
        Map<String, Object> map = JSONUtils.jsonStringToMap(null);
        Assert.assertNotNull(map);
        Assert.assertTrue(map.isEmpty());
    }
}
