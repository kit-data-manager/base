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

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.oauth.server.OAuthServerRequest;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import com.sun.jersey.oauth.signature.OAuthSignature;
import com.sun.jersey.oauth.signature.OAuthSignatureException;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretDecryptionException;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.mdm.admin.util.ServiceAccessUtil;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.util.auth.AbstractAuthenticator;
import edu.kit.dama.rest.util.auth.exception.MissingCredentialException;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class OAuthAuthenticator extends AbstractAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthAuthenticator.class);

    public static final String DEFAULT_CONSUMER_KEY_PROPERTY_KEY = "defaultConsumerKey";
    public static final String DEFAULT_CONSUMER_SECRET_PROPERTY_KEY = "defaultConsumerSecret";

    public static final String USER_TOKEN_KEY = "Token";
    public static final String USER_SECRET_KEY = "Secret";

    private String defaultConsumerKey = "DefaultConsumer";
    private String defaultConsumerSecret = "secret";

    @Override
    public String[] getCredentialAttributeNames() {
        return new String[]{USER_TOKEN_KEY, USER_SECRET_KEY};
    }

    @Override
    public ServiceAccessToken generateServiceAccessToken(UserId pUser, Map<String, String> pCredential) throws SecretEncryptionException {
        ServiceAccessToken token = new ServiceAccessToken(pUser.getStringRepresentation(), getAuthenticatorId());
        token.setTokenKey(pCredential.get(USER_TOKEN_KEY));
        token.setSecret(pCredential.get(USER_SECRET_KEY));
        return token;
    }

    @Override
    public IAuthorizationContext obtainAuthorizationContext(HttpRequestContext hc, GroupId pGroupId) throws UnauthorizedAccessAttemptException, MissingCredentialException {
        LOGGER.debug("Starting OAuth authentication.");
        OAuthServerRequest request = new OAuthServerRequest(hc);
        // get incoming OAuth parameters
        OAuthParameters params = new OAuthParameters();
        params.readRequest(request);
        IMetaDataManager manager = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        manager.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            LOGGER.debug("Obtaining consumer credentials.");
            //obtain consumer key and secret
            String consumerKey = params.getConsumerKey();
            if (consumerKey == null) {
                throw new MissingCredentialException("No consumer key provided.");
            }
            LOGGER.debug("Consumer key is: {}", consumerKey);
            String consumerSecret;
            if (consumerKey.equals(defaultConsumerKey)) {
                //default consumer
                LOGGER.debug("Consumer key is the default key. Using configured default secret.");
                consumerSecret = defaultConsumerSecret;
            } else {
                LOGGER.debug("Obtaining service access token for provided consumer key '{}'", consumerKey);
                //obtaining consumer secret for provided consumer key
                ServiceAccessToken consumerToken = ServiceAccessUtil.getAccessToken(manager, consumerKey, getAuthenticatorId());

                if (consumerToken == null) {
                    throw new UnauthorizedAccessAttemptException("No access token found for provided consumer key '" + consumerKey + "'.");
                }
                LOGGER.debug("Obtaining consumer secret.");
                consumerSecret = consumerToken.getSecret();
            }
            LOGGER.debug("Consumer credentials successfully obtained. Obtaining user credentials for token '{}'", params.getToken());

            //obtain user key and secret
            ServiceAccessToken userToken = ServiceAccessUtil.getAccessToken(manager, params.getToken(), getAuthenticatorId());

            if (userToken == null) {
                throw new UnauthorizedAccessAttemptException("No access token found for provided key '" + params.getToken() + "'.");
            }

            LOGGER.debug("Successfully obtained service access token for user. Generating OAuthSecrets.");
            //set the secret from the obtained token
            OAuthSecrets secrets = new OAuthSecrets();
            secrets = secrets.consumerSecret(consumerSecret);
            secrets.setTokenSecret(userToken.getSecret());

            LOGGER.debug("Verifying OAuth signature.");
            //verify the signature using key and secret
            if (OAuthSignature.verify(request, params, secrets)) {
                //allowed, map everything to an appropriate context
                LOGGER.debug("Signature verification succeeded.  Building and returning AuthorizationContext for user '{}'", userToken.getUserId());
                return buildAuthorizationContext(new UserId(userToken.getUserId()), pGroupId);
            } else {
                throw new OAuthSignatureException("Signature verification failed.");
            }
        } catch (SecretDecryptionException ex) {
            throw new UnauthorizedAccessAttemptException("Failed to decrypt either consumer secret or user secret.", ex);
        } catch (OAuthSignatureException ose) {
            throw new UnauthorizedAccessAttemptException("Failed to verify OAuth signature.", ose);
        } catch (EntityNotFoundException ex) {
            throw new UnauthorizedAccessAttemptException("Failed to build authorization context. Accessing user seems not to be member of group" + pGroupId + ".", ex);
        } finally {
            manager.close();
        }
    }

    @Override
    public boolean performCustomConfiguration(Configuration pConfig) throws ConfigurationException {
        defaultConsumerKey = pConfig.getString(DEFAULT_CONSUMER_KEY_PROPERTY_KEY, "DefaultConsumer");
        defaultConsumerSecret = pConfig.getString(DEFAULT_CONSUMER_SECRET_PROPERTY_KEY, "secret");
        return true;
    }

    public void setDefaultConsumerKey(String defaultConsumerKey) {
        this.defaultConsumerKey = defaultConsumerKey;
    }

    public void setDefaultConsumerSecret(String defaultConsumerSecret) {
        this.defaultConsumerSecret = defaultConsumerSecret;
    }
}
