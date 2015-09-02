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
package edu.kit.dama.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.rest.base.exceptions.SSLContextException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.slf4j.LoggerFactory;

/**
 * Abstract Client for REST calls to the KIT-DM The context should contain the
 * credentials of a valid user. (Access key and secret)
 *
 * @author hartmann-v
 */
public abstract class AbstractRestClient {

  /**
   * The logger
   */
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractRestClient.class);
  // <editor-fold defaultstate="collapsed" desc="resources for query parameters">

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc="error messages">
  /**
   * Error message ssl context
   */
  private static final String ERROR_SSL_CONTEXT = "Failed initialize SSL context for REST client";
  /**
   * Error creating REST client
   */
  private static final String ERROR_REST_CLIENT = "Failed to create REST client";
  /**
   * Error null argument.
   */
  private static final String ERROR_ARGUMENT = "Argument '%s' must not be null!";
  // </editor-fold>
  private final static String SIGNATURE_METHOD = "PLAINTEXT";
  private SimpleRESTContext context = null;
  private OAuthClientFilter currentContextFilter = null;
  private Client client;
  private WebResource webResource;
  private final static X509TrustManager TRUST_MANAGER = new X509TrustManager() {
    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }

    @Override
    public void checkClientTrusted(X509Certificate[] xcs, String string) throws java.security.cert.CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] xcs, String string) throws java.security.cert.CertificateException {
    }
  };

  private final static HostnameVerifier VERIFIER = new HostnameVerifier() {
    @Override
    public boolean verify(String hostname, SSLSession session) {
      return true;
    }
  };

  /**
   * Create an instance of a client.
   *
   * @param pServiceUrl The base URL of the REST service.
   * @param pContext The security context to access the service.
   */
  protected AbstractRestClient(String pServiceUrl, SimpleRESTContext pContext) {
    if (pContext == null) {
      throw new IllegalArgumentException(String.format(ERROR_ARGUMENT, "pContext"));
    }

    if (pServiceUrl == null) {
      throw new IllegalArgumentException(String.format(ERROR_ARGUMENT, "pServiceUrl"));
    }

    //client = Client.create();
    LOGGER.debug("Creating client for service URL {} and context {}", new Object[]{pServiceUrl, pContext});
    ClientConfig config = new DefaultClientConfig();
    //String authentication = "Basic " + encodeCredentialsBasic("<<User>>", "<<Password>>");

    try {
      LOGGER.debug("Initializing TLS");
      SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init(null, new TrustManager[]{TRUST_MANAGER}, new SecureRandom());

      config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(VERIFIER, ctx));
      client = Client.create(config);
    } catch (NoSuchAlgorithmException | KeyManagementException ex) {
      throw new SSLContextException(ERROR_SSL_CONTEXT, ex);
    }
    LOGGER.debug("Creating service URI resource from URL {}", pServiceUrl);
    //  client = Client.create();
    URI resourceUri = null;
    try {
      resourceUri = new URL(pServiceUrl).toURI();
    } catch (MalformedURLException | URISyntaxException ex) {
      throw new SSLContextException(ERROR_REST_CLIENT, ex);
    }

    LOGGER.debug("Creating webresource instance for resource URI {}", resourceUri);
    webResource = client.resource(resourceUri);
    // webResource.path("").header(AUTHENTICATION_HEADER, authentication).get(DownloadInformationResult.class);
    LOGGER.debug("Applying RESTContext");
    setFilterFromContext(pContext);
    LOGGER.debug("Client initialized.");
  }

  /**
   * Set the credentials for the REST call. First check if context is already
   * set. If not, remove old context and add the new one.
   *
   * @param pContext context holding credentials.
   */
  protected final void setFilterFromContext(SimpleRESTContext pContext) {
    if (pContext == null) {
      //throw new IllegalArgumentException(String.format(ERROR_ARGUMENT, "pContext"));
      LOGGER.info("Context not provided. Using previously set context.");
      return;
    }
    // est if context is already configured
    if (!pContext.equals(context)) {
      LOGGER.debug("Applying REST Context {}", pContext);
      // new context! 
      // do configuration
      if (currentContextFilter != null) {
        LOGGER.debug("Removing previous context filter");
        webResource.removeFilter(currentContextFilter);
      }
      OAuthParameters params = new OAuthParameters();
      params.signatureMethod(SIGNATURE_METHOD);
      params.consumerKey("key").setToken(pContext.getAccessKey());
      params.version();

      // OAuth secrets to access resource
      OAuthSecrets secrets = new OAuthSecrets();
      secrets.consumerSecret("secret").setTokenSecret(pContext.getAccessSecret());

      // if parameters and secrets remain static, filter can be added to each web resource
      LOGGER.debug("Adding new context filter");
      context = pContext;
      currentContextFilter = new OAuthClientFilter(client.getProviders(), params, secrets);
      webResource.addFilter(currentContextFilter);
    }
  }

  /**
   * Create webresource for access via REST.
   *
   * @param path relative path
   * @return new webresource for given (relative) path
   */
  protected final WebResource getWebResource(String path) {
    return webResource.path(path);
  }

  /**
   * Check if service is available. This basic method should be provided by
   * implementing {@link ICommonRestInterface} on the server side of the
   * service. If the service cannot be accessed, a warning is logged and FALSE
   * is returned.
   *
   * @return TRUE if the service returns and if the status is 200, FALSE
   * otherwise.
   */
  public boolean checkService() {
    boolean result = false;
    try {
      ClientResponse response = getWebResource("/checkService").get(ClientResponse.class);
      LOGGER.debug("Response: " + response);
      result = response != null && response.getStatus() == 200;
    } catch (Throwable t) {
      LOGGER.warn("Call to checkService() threw an exception. Service is expected to be unavailable.", t);
    }
    return result;
  }
}
