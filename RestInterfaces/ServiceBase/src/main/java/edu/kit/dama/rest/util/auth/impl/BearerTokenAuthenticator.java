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
package edu.kit.dama.rest.util.auth.impl;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.util.ServiceAccessUtil;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.util.auth.exception.MissingCredentialException;
import edu.kit.dama.util.CryptUtil;
import java.net.MalformedURLException;
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
import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class BearerTokenAuthenticator extends SimpleTokenAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerTokenAuthenticator.class);

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
    public static final String TOKEN_INFO_SERVICE_URL_PROPERTY_KEY = "tokenInfoServiceUrl";

    //The URL of the info service used to validate the bearer token
    private String tokenInfoServiceUrl = null;

    @Override
    public IAuthorizationContext obtainAuthorizationContext(HttpRequestContext hc, GroupId groupId) throws UnauthorizedAccessAttemptException, MissingCredentialException {
        String token = hc.getHeaderValue("Authorization");
        if (token == null) {
            throw new MissingCredentialException("No authorization header entry provided.");
        }
        if (token.startsWith("Bearer ")) {
            LOGGER.debug("Starting bearer token authentication.");
            if (tokenInfoServiceUrl != null) {
                LOGGER.debug("Validating provided bearer token using info service at '{}'.", tokenInfoServiceUrl);
                //if validate, do this
                ClientConfig config = new DefaultClientConfig();

                try {
                    SSLContext ctx = SSLContext.getInstance("TLS");
                    ctx.init(null, new TrustManager[]{TRUST_MANAGER}, new SecureRandom());

                    config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(VERIFIER, ctx));
                    com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create(config);
                    WebResource webResource = client.resource(new URL(tokenInfoServiceUrl).toURI());
                    String result = webResource.header("Authorization", token).get(String.class);
                    LOGGER.debug("Service returned result {}. Checking 'exp' property.", result);
                    JSONObject resultObject = new JSONObject(result);

                    long expiresAt = resultObject.getLong("exp");
                    LOGGER.debug("Token exp property is set to value {}.", expiresAt);
                    if (System.currentTimeMillis() > expiresAt) {
                        throw new UnauthorizedAccessAttemptException("The provided bearer token has expired at timestamp " + expiresAt + ".");
                    }
                } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                    throw new UnauthorizedAccessAttemptException("Failed to perform secured access to token info service.", ex);
                } catch (MalformedURLException | URISyntaxException ex) {
                    throw new UnauthorizedAccessAttemptException("Failed to access token info service due to a malformed URL.", ex);
                }
            }
            //still valid or not checked...remove 'Bearer ' part and continue
            LOGGER.debug("Token validation succeeded/skipped. Proceeding with authentication");
            token = token.replaceFirst("Bearer ", "");
        } else {
            throw new MissingCredentialException("No bearer token provided in authorization header. Token is '" + token + "'");
        }

        IMetaDataManager manager = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        manager.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            String tokenKey = CryptUtil.stringToSHA1(token);
            LOGGER.debug("Obtaining service access token for key {}", tokenKey);
            ServiceAccessToken accessToken = ServiceAccessUtil.getAccessToken(manager, tokenKey, getAuthenticatorId());

            if (accessToken == null) {
                throw new UnauthorizedAccessAttemptException("No access token obtained for tokenKey '" + tokenKey + "' and serviceId '" + getAuthenticatorId() + "'");
            }
            LOGGER.debug("Building and returning AuthorizationContext for user {}", accessToken.getUserId());
            //no secret handling needed for the moment as only the token is validated
            return buildAuthorizationContext(new UserId(accessToken.getUserId()), groupId);
        } catch (UnauthorizedAccessAttemptException | EntityNotFoundException ex) {
            throw new UnauthorizedAccessAttemptException("The access using the provided HttpContext has not been authorized.", ex);
        } finally {
            manager.close();
        }
    }

    @Override
    public boolean performCustomConfiguration(Configuration pConfig) throws ConfigurationException {
        //e.g. https://localhost:2443/oauth2/tokeninfo
        tokenInfoServiceUrl = pConfig.getString(TOKEN_INFO_SERVICE_URL_PROPERTY_KEY);
        if (tokenInfoServiceUrl != null) {
            try {
                LOGGER.debug("Successfully validated property '{}' with value ''", TOKEN_INFO_SERVICE_URL_PROPERTY_KEY, new URL(tokenInfoServiceUrl).toURI());
            } catch (MalformedURLException | URISyntaxException ex) {
                throw new ConfigurationException("Failed to configure authenticator for serviceId ' " + getAuthenticatorId() + "'. Value of property '" + TOKEN_INFO_SERVICE_URL_PROPERTY_KEY + "' is no proper URL.", ex);
            }
        }
        return true;
    }
}
