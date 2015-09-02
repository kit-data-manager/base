/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.ui.simon.impl;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import edu.kit.dama.ui.simon.exceptions.ProbeConfigurationException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class RestServiceProbe extends AbstractProbe {

  private static Logger LOGGER = LoggerFactory.getLogger(RestServiceProbe.class);
  private final String SERVICE_URL_KEY = "service.url";
  private final String SERVICE_METHOD_KEY = "service.method";
  private URI resourceURI = null;
  private String serviceMethod = null;

  @Override
  public boolean checkProbe() {
    try {
      LOGGER.debug("Creating REST client");
      Client client = Client.create();
      LOGGER.debug("Creating resoruce for URI {}", resourceURI);
      WebResource webResource = client.resource(resourceURI);
      LOGGER.debug("Calling HEAD request for path {}", serviceMethod);
      ClientResponse response = webResource.path(serviceMethod).get(ClientResponse.class);
      LOGGER.debug("Obtained response code {}", response.getClientResponseStatus());
      return response.getClientResponseStatus().equals(Status.OK);
    } catch (Throwable t) {
      LOGGER.error("Failed to check probe.", t);
      return false;
    }
  }

  @Override
  public void configureProbe(PropertiesConfiguration pProperties) throws ProbeConfigurationException {
    LOGGER.debug("Configuring REST service probe...");
    serviceMethod = pProperties.getString(SERVICE_METHOD_KEY);
    if (serviceMethod == null) {
      throw new ProbeConfigurationException("Property " + SERVICE_METHOD_KEY + " is missing.");
    } else {
      LOGGER.debug(" - Setting property {} to value {}", new Object[]{SERVICE_METHOD_KEY, serviceMethod});
    }

    String sServiceUrl = pProperties.getString(SERVICE_URL_KEY);
    if (sServiceUrl == null) {
      throw new ProbeConfigurationException("Property " + SERVICE_URL_KEY + " is missing.");
    } else {
      LOGGER.debug(" - Setting property {} to value {}", new Object[]{SERVICE_URL_KEY, sServiceUrl});
    }

    try {
      resourceURI = new URL(sServiceUrl).toURI();
    } catch (MalformedURLException ex) {
      throw new ProbeConfigurationException("The provided service URL " + sServiceUrl + " is invalid", ex);
    } catch (URISyntaxException ex) {
      throw new ProbeConfigurationException("The provided service URL " + sServiceUrl + " is invalid", ex);
    }
    LOGGER.debug("REST service probe successfully configured.");
  }

  @Override
  public String[] getConfigurationPropertyKeys() {
    return new String[]{SERVICE_URL_KEY, SERVICE_METHOD_KEY};
  }

  @Override
  public String getConfigurationPropertyDescription(String pKey) {
    if (SERVICE_URL_KEY.equals(pKey)) {
      return "The URL of the REST service to test, e.g. http://localhost/MyService";
    } else if (SERVICE_METHOD_KEY.equals(pKey)) {
      return "The service method used to test the responsiveness, e.g. /myMethod";
    }

    return "Unknown property";
  }
}
