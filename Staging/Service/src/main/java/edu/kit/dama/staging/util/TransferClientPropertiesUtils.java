/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.staging.util;

import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.entities.TransferClientProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 * Helper class for working with TransferClientProperties objects
 *
 * @author jejkal
 */
public class TransferClientPropertiesUtils {

  public static final String STAGING_ACCESS_POINT_KEY = "staging.access.point.id";
  public static final String STAGING_URL_KEY = "staging.url";
  public static final String CLIENT_ACCESS_URL_KEY = "client.access.url";
  public static final String MAIL_NOTIFICATION_KEY = "mail.notification";
  public static final String MAIL_RECEIVER_KEY = "mail.receiver";
  public static final String API_KEY_KEY = "dama.rest.key";
  public static final String API_SECRET_KEY = "dama.rest.secret";
  public static final String PROCESSOR_KEY = "processors";

  /**
   * Helper method to convert TransferClientProperties object to a map of
   * key-value pairs.
   *
   * @param pProperties A TransferClientProperties object.
   *
   * @return A map representing pProperties.
   */
  public static Map<String, String> propertiesToMap(TransferClientProperties pProperties) {
    Map<String, String> result = new HashMap<>();
    //add standard properties
    result.put(STAGING_ACCESS_POINT_KEY, pProperties.getStagingAccessPointId());
    result.put(STAGING_URL_KEY, pProperties.getStagingUrl());
    result.put(CLIENT_ACCESS_URL_KEY, pProperties.getTransferClientUrl());
    result.put(MAIL_NOTIFICATION_KEY, Boolean.toString(pProperties.isSendMailNotification()));
    result.put(MAIL_RECEIVER_KEY, pProperties.getReceiverMail());
    result.put(API_KEY_KEY, pProperties.getApiKey());
    result.put(API_SECRET_KEY, pProperties.getApiSecret());

    if (!pProperties.getProcessors().isEmpty()) {
      StringBuilder processors = new StringBuilder();
      for (StagingProcessor processor : pProperties.getProcessors()) {
        processors.append(processor.getUniqueIdentifier()).append(";");
      }
      result.put(PROCESSOR_KEY, processors.toString());
    }

    //add custom properties
    for (String key : pProperties.getPropertyKeys()) {
      if (key != null) {
        result.put(key, pProperties.getCustomProperty(key));
      }
    }

    return result;
  }

  /**
   * Helper method to convert TransferClientProperties object to a properties
   * object. This is needed e.g. to store the properties for later use.
   *
   * @param pProperties A TransferClientProperties object.
   *
   * @return A properties object containing all properties of pProperties.
   */
  public static Properties propertiesToProperties(TransferClientProperties pProperties) {
    Map<String, String> result = propertiesToMap(pProperties);

    Properties props = new Properties();

    Set<Entry<String, String>> values = result.entrySet();
    for (Entry<String, String> value : values) {
      if (value != null && value.getKey() != null && value.getValue() != null) {
        props.put(value.getKey(), value.getValue());
      }
    }
    return props;
  }

  /**
   * Helper method to convert a map of key-value-pairs to a
   * TransferClientProperties object.
   *
   * @param pProperties a key-value map.
   *
   * @return A TransferClientProperties object reflecting pProperties.
   */
  public static TransferClientProperties mapToProperties(Map<String, String> pProperties) {
    TransferClientProperties result = new TransferClientProperties();

    //transfer standard properties and remove them from the map
    String  v = pProperties.get(STAGING_ACCESS_POINT_KEY);
    if (v != null) {
      result.setStagingAccessPointId(v);
      pProperties.remove(v);
    }
    v = pProperties.get(STAGING_URL_KEY);
    if (v != null) {
      result.setStagingUrl(v);
      pProperties.remove(v);
    }
    v = pProperties.get(CLIENT_ACCESS_URL_KEY);
    if (v != null) {
      result.setTransferClientUrl(v);
      pProperties.remove(v);
    }

    v = pProperties.get(MAIL_NOTIFICATION_KEY);
    if (v != null) {
      result.setSendMailNotification(Boolean.parseBoolean(v));
      pProperties.remove(v);
    }
    v = pProperties.get(MAIL_RECEIVER_KEY);
    if (v != null) {
      result.setReceiverMail(v);
      pProperties.remove(v);
    }
    v = pProperties.get(API_KEY_KEY);
    if (v != null) {
      result.setApiKey(v);
      pProperties.remove(v);
    }
    v = pProperties.get(API_SECRET_KEY);
    if (v != null) {
      result.setApiSecret(v);
      pProperties.remove(v);
    }

    v = pProperties.get(PROCESSOR_KEY);
    if (v != null) {
      //extract all staging processors by their ID
      String[] processorIds = StringUtils.split(v, ";");
      for (String id : processorIds) {
        if (id != null && id.length() > 3) {
          //get processor by id
          StagingProcessor processor = StagingConfigurationPersistence.getSingleton(StagingConfigurationManager.getSingleton().getStagingPersistenceUnit()).findStagingProcessorById(id.trim());
          if (processor != null) {
            //add processor
            result.addProcessor(processor);
          }
        }
      }
      pProperties.remove(v);
    }

    //transfer custom properties
    Set<Entry<String, String>> entries = pProperties.entrySet();

    for (Entry<String, String> entry : entries) {
      result.addCustomProperty(entry.getKey(), entry.getValue());
    }

    return result;
  }

}
