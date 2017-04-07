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
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.rest.util.auth.AbstractAuthenticator;
import edu.kit.dama.rest.util.auth.exception.MissingCredentialException;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.CryptUtil;
import java.util.Map;
import org.apache.commons.configuration.Configuration;

/**
 * Custom authenticator that can be used to authenticate access for SWAGGER-UI.
 * The authenticator is configured with an api-key and an api-user. The key is
 * provided as query parameter from SWAGGER-UI. If the key matches the
 * configured key, the access is authorized for api-user in group WORLD with
 * role MANAGER. Therefore, in order to allow this authenticator to function
 * properly, an according user with the according permissions must exist. If
 * this is not the case the configuration of this authenticator will fail.
 *
 * @author jejkal
 */
public class SwaggerApiKeyAuthenticator extends AbstractAuthenticator {

    public static final String USER_TOKEN_KEY = "API KEY";
    private String apiKey = null;
    private String apiUser = null;

    @Override
    public String[] getCredentialAttributeNames() {
        return new String[]{};
    }

    @Override
    public ServiceAccessToken generateServiceAccessToken(UserId pUser, Map<String, String> pCredential) throws SecretEncryptionException {
        ServiceAccessToken token = new ServiceAccessToken(pUser.getStringRepresentation(), getAuthenticatorId());
        token.setTokenKey(CryptUtil.stringToSHA1(pCredential.get(USER_TOKEN_KEY)));
        return token;
    }

    @Override
    public IAuthorizationContext obtainAuthorizationContext(HttpRequestContext hc, GroupId groupId) throws UnauthorizedAccessAttemptException, MissingCredentialException {
        String token = hc.getQueryParameters().getFirst("api_key");
        if (token == null) {
            //check header
            String authHeader = hc.getHeaderValue("Authorization");
            if (authHeader != null) {
                String[] split = authHeader.split(" ");
                if (split.length == 2 && "api_key".equals(split[0])) {
                    token = split[1];
                }
            }
        }

        if (token == null) {
            throw new MissingCredentialException("No access token obtained for tokenKey 'api_key' and serviceId '" + getAuthenticatorId() + "'");
        }

        if (token.equals(apiKey)) {
            return new AuthorizationContext(new UserId(apiUser), new GroupId(Constants.WORLD_GROUP_ID), Role.MANAGER);
        }
        throw new UnauthorizedAccessAttemptException("API access not allowed for provided api_key.");
    }

    public IAuthorizationContext authorize(String apiKey) throws UnauthorizedAccessAttemptException {
        if (apiKey == null) {
            throw new UnauthorizedAccessAttemptException("No authentication token 'api_key' found.");
        }
        if (apiKey.equals(this.apiKey)) {
            return new AuthorizationContext(new UserId(apiUser), new GroupId(Constants.WORLD_GROUP_ID), Role.MANAGER);
        }
        throw new UnauthorizedAccessAttemptException("API access not allowed for provided api_key.");
    }

    @Override
    public boolean performCustomConfiguration(Configuration pConfig) throws ConfigurationException {
        apiKey = pConfig.getString("apiKey");
        if (apiKey == null) {
            throw new ConfigurationException("Property 'apiKey' is missing.");
        }
        apiUser = pConfig.getString("apiUser");
        if (apiUser == null) {
            throw new ConfigurationException("Property 'apiUser' is missing.");
        }
        try {
            Role role = (Role) GroupServiceLocal.getSingleton().getMaximumRole(new GroupId(Constants.WORLD_GROUP_ID), new UserId(apiUser), AuthorizationContext.factorySystemContext());
            if (role.lessThan(Role.MANAGER)) {
                throw new ConfigurationException("The provided api user '" + apiUser + "' has an insufficient role in group " + Constants.WORLD_GROUP_ID + ". (" + role + " < MANAGER)");
            }
        } catch (EntityNotFoundException | UnauthorizedAccessAttemptException e) {
            throw new ConfigurationException("Failed to check configured api user '" + apiUser + "'. Probably the user does not exist.");
        }
        return true;
    }
}
