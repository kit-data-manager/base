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

import com.sun.jersey.api.core.HttpRequestContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.mdm.admin.util.ServiceAccessUtil;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.util.auth.AbstractAuthenticator;
import edu.kit.dama.rest.util.auth.exception.MissingCredentialException;
import edu.kit.dama.util.CryptUtil;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class SimpleTokenAuthenticator extends AbstractAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleTokenAuthenticator.class);
    public static final String USER_TOKEN_PROPERTY_KEY = "Token";

    @Override
    public String[] getCredentialAttributeNames() {
        return new String[]{USER_TOKEN_PROPERTY_KEY};
    }

    @Override
    public ServiceAccessToken generateServiceAccessToken(UserId pUser, Map<String, String> pCredential) throws SecretEncryptionException {
        ServiceAccessToken token = new ServiceAccessToken(pUser.getStringRepresentation(), getAuthenticatorId());
        token.setTokenKey(CryptUtil.stringToSHA1(pCredential.get(USER_TOKEN_PROPERTY_KEY)));
        return token;
    }

    @Override
    public IAuthorizationContext obtainAuthorizationContext(HttpRequestContext hc, GroupId groupId) throws UnauthorizedAccessAttemptException, MissingCredentialException {
        String token = hc.getQueryParameters().getFirst("authToken");

        if (token == null) {
            //check header
            String authHeader = hc.getHeaderValue("Authorization");
            if (authHeader != null) {
                String[] split = authHeader.split(" ");
                if (split.length == 2 && "authToken".equals(split[0])) {
                    token = split[1];
                }
            }
        }

        if (token == null) {
            throw new MissingCredentialException("No query parameter 'authToken' and no authorization header found.");
        }

        LOGGER.debug("Starting simple token authentication.");

        IMetaDataManager manager = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        manager.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            String tokenKey = CryptUtil.stringToSHA1(token);
            LOGGER.debug("Obtaining service access token for key {}", tokenKey);
            ServiceAccessToken accessToken = ServiceAccessUtil.getAccessToken(manager, tokenKey, getAuthenticatorId());

            if (accessToken == null) {
                throw new UnauthorizedAccessAttemptException("No access token obtained for tokenKey '" + tokenKey + "' and serviceId '" + getAuthenticatorId() + "'");
            }
            LOGGER.debug("Access token successfully obtained. Creating and returning AuthorizationContext for user {}", accessToken.getUserId());
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
        return true;
    }
}
