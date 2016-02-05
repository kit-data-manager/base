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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.ui.simon.impl;

import edu.kit.dama.ui.simon.exceptions.ProbeConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class WebServerProbe extends AbstractProbe {

  private static Logger LOGGER = LoggerFactory.getLogger(WebServerProbe.class);
  private final String SERVICE_URL_KEY = "server.url";
  private final String CONNECTION_TIMEOUT_KEY = "timeout";
  private URL serverUrl = null;
  private int timeout = 5000;

  @Override
  public boolean checkProbe() {
    try {
      URLConnection con = serverUrl.openConnection();
      con.setConnectTimeout(timeout);
      con.connect();
      String header0 = con.getHeaderField(0);
      return header0 != null && header0.endsWith("200 OK");
    } catch (IOException ex) {
      LOGGER.error("Failed to check Web server probe", ex);
    }
    return false;
  }

  @Override
  public void configureProbe(PropertiesConfiguration pProperties) throws ProbeConfigurationException {
    LOGGER.debug("Configuring WebServer probe...");

    String sServiceUrl = pProperties.getString(SERVICE_URL_KEY);
    if (sServiceUrl == null) {
      throw new ProbeConfigurationException("Property " + SERVICE_URL_KEY + " is missing.");
    } else {
      LOGGER.debug(" - Setting property {} to value {}", new Object[]{SERVICE_URL_KEY, sServiceUrl});
    }

    try {
      serverUrl = new URL(sServiceUrl);
    } catch (MalformedURLException ex) {
      throw new ProbeConfigurationException("The provided service URL " + sServiceUrl + " is invalid", ex);
    }

    String sTimeout = pProperties.getString(CONNECTION_TIMEOUT_KEY, "5000");
    try {
      timeout = Integer.parseInt(sTimeout);
      LOGGER.debug(" - Setting property {} to value {}", new Object[]{CONNECTION_TIMEOUT_KEY, timeout});
    } catch (NumberFormatException ex) {
      LOGGER.warn("Property " + CONNECTION_TIMEOUT_KEY + " is not a valid integer number, using default value 5000.");
      timeout = 5000;
    }

    LOGGER.debug("WebServer probe successfully configured.");
  }

  @Override
  public String[] getConfigurationPropertyKeys() {
    return new String[]{SERVICE_URL_KEY, CONNECTION_TIMEOUT_KEY};
  }

  @Override
  public String getConfigurationPropertyDescription(String pKey) {
    if (SERVICE_URL_KEY.equals(pKey)) {
      return "The URL of the Web server to test, e.g. http://localhost/";
    } else if (CONNECTION_TIMEOUT_KEY.equals(pKey)) {
      return "The timeout in milliseconds until the probe is marked as FAILED. (Default: 1000 ms)";
    }
    return "Unknown property";
  }
}
