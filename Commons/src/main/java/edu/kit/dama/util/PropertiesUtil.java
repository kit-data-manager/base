/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class PropertiesUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);

  /**
   * Hidden constructor.
   */
  private PropertiesUtil() {
  }

  /**
   * Store a properties object into a single string. If the provided properties
   * object is null, an empty string is returned.
   *
   * @param pProperties The properties object.
   *
   * @return pProperties as string.
   *
   * @throws IOException If the preoperties could not be serialized to string.
   */
  public static String propertiesToString(Properties pProperties) throws IOException {
    return propertiesToString(pProperties, null);
  }

  /**
   * Store a properties object into a single string. If the provided properties
   * object is null, an empty string is returned.
   *
   * @param pProperties The properties object.
   * @param pComments Optional comments.
   *
   * @return pProperties as string.
   *
   * @throws IOException If the preoperties could not be serialized to string.
   */
  public static String propertiesToString(Properties pProperties, String pComments) throws IOException {
    if (pProperties == null) {
      LOGGER.warn("Argument pProperties is null. Returning empty string.");
      return "";
    }
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try (PrintWriter pw = new PrintWriter(bout)) {
      pProperties.store(pw, pComments);
      return bout.toString();
    }
  }

  /**
   * Restore properties from a single string. If the provided string is null, an
   * empty properties object is returned.
   *
   * @param pPropertiesString The properties string. It is highly recommended to
   * generate the string using propertiesToString(). Other formats may result in
   * unexpected key-value-pairs.
   *
   * @return The restored properties object.
   *
   * @throws IOException If the preoperties could not be deserialized from
   * string.
   */
  public static Properties propertiesFromString(String pPropertiesString) throws IOException {
    Properties result = new Properties();
    if (pPropertiesString == null) {
      LOGGER.warn("Argument pPropertiesString is null. Returning empty properties.");
      return result;
    }
    ByteArrayInputStream bin = new ByteArrayInputStream(pPropertiesString.getBytes());
    result.load(bin);
    return result;
  }
}
