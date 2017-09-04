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
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.rest.util.auth.AbstractAuthenticator;
import edu.kit.dama.rest.util.auth.exception.MissingCredentialException;
import java.security.Principal;
import java.util.Map;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticator based on successful HTTP Basic/Digest authentication performed
 * by the KITDMJdbcRealm implementation. This authenticator assumes that
 * pre-authentication based on the mentioned custom realm implementation has
 * been performed. The pre-authentication provides a principal that is obtained
 * from the provided HttpContext. It is assumed, that the principal name is the
 * userId of the successfully authenticated user. Therefore, this implementation
 * only has to build the authorization context using the userId provided by the
 * principal and groupId provided by the HTTP call.
 *
 * There are some special requirements if using the authenticator:
 *
 * <ul>
 * <li>This authenticator only works if HTTP authentication with KITDMJdbcRealm
 * is configured.</li>
 * <li>The configured authentication method (BASIC or DIGEST) must fit between
 * KITDMJdbcRealm and this authenticator.</li>
 * <li>If authentication method DIGEST is configured, the configured realm must
 * fit the one configured for the servlet.</li>
 * <li>Depending on the authentication method, the ServiceAccessToken in the
 * database has to be generated via 'generateServiceAccessToken()'. The tokenkey
 * is always the userId, the tokensecret is the unencrypted MD5A1 part of the
 * according HTTP authentication.</li>
 * </ul>
 *
 * @author jejkal
 */
public class HTTPAuthenticator extends AbstractAuthenticator {

    public static enum AUTHENTICATION_TYPE {
        BASIC,
        DIGEST;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPAuthenticator.class);

    public static final String USER_NAME_KEY = "Username";
    public static final String USER_PASSWORD_KEY = "Password";

    public static final String AUTHENTICATION_TYPE_PROPERTY_KEY = "type";
    public static final String REALM_PROPERTY_KEY = "realm";
    private AUTHENTICATION_TYPE authType;
    private String realm = "kitdm";

    @Override
    public String[] getCredentialAttributeNames() {
        return new String[]{USER_NAME_KEY, USER_PASSWORD_KEY};
    }

    @Override
    public ServiceAccessToken generateServiceAccessToken(UserId pUser, Map<String, String> pCredential) {
        String userName = pCredential.get(USER_NAME_KEY);
        String password = pCredential.get(USER_PASSWORD_KEY);

        String md5a1Expected;

        if (AUTHENTICATION_TYPE.BASIC.equals(authType)) {
            //HTTP Basic
            md5a1Expected = DigestUtils.md5Hex(userName + ":" + password);
        } else {
            //HTTP Digest
            md5a1Expected = DigestUtils.md5Hex(userName + ":" + realm + ":" + password);
        }

        ServiceAccessToken result = new ServiceAccessToken(pUser.getStringRepresentation(), getAuthenticatorId());
        result.setTokenKey(userName);
        result.setTokenSecret(md5a1Expected);
        return result;
    }

    @Override
    public IAuthorizationContext obtainAuthorizationContext(HttpRequestContext httpContext, GroupId groupId) throws UnauthorizedAccessAttemptException, MissingCredentialException {
        Principal principal = httpContext.getUserPrincipal();

        if (principal == null) {
            throw new MissingCredentialException("Pre-authorization has not been performed. Principal not available, authorization not possible.");
        }

        String authorizedUser = principal.getName();
        if (authorizedUser.lastIndexOf("@") > 0) {
            //probably nffa-authenticated user following schema user@group -> remove group
            authorizedUser = authorizedUser.substring(0, authorizedUser.lastIndexOf("@"));
        }
        try {
            LOGGER.debug("Principal successfully obtained.  Building and returning AuthorizationContext for user '{}' in group '{}'.", authorizedUser, groupId);
            return buildAuthorizationContext(new UserId(authorizedUser), groupId);
        } catch (EntityNotFoundException ex) {
            throw new UnauthorizedAccessAttemptException("Failed to build authorization context. Accessing user seems not to be member of group" + groupId + ".", ex);
        }
    }

    @Override
    public boolean performCustomConfiguration(Configuration pConfig) throws ConfigurationException {
        String type = realm = pConfig.getString(AUTHENTICATION_TYPE_PROPERTY_KEY);
        authType = AUTHENTICATION_TYPE.valueOf(type);

        if (authType == null) {
            throw new ConfigurationException("Failed to configure Authenticator. Mandatory property " + AUTHENTICATION_TYPE_PROPERTY_KEY + " is invalid. Supported values are " + AUTHENTICATION_TYPE.BASIC + " and " + AUTHENTICATION_TYPE.DIGEST + ".");
        }

        realm = pConfig.getString(REALM_PROPERTY_KEY);
        if (realm == null && AUTHENTICATION_TYPE.DIGEST.equals(authType)) {
            throw new ConfigurationException("Failed to configure Authenticator. Mandatory property " + REALM_PROPERTY_KEY + " is missing.");
        }
        return true;
    }

}
