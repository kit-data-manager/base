package edu.kit.dama.rest.util.auth;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.mdm.admin.util.ServiceAccessUtil;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.util.auth.exception.InvalidCredentialException;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
/**
 *
 * @author jejkal
 */
public abstract class AbstractAuthenticator implements IConfigurableAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAuthenticator.class);

    public final static String AUTHENTICATOR_ID_PROPERTY_KEY = "authenticatorId";
    public final static String ENABLE_FOR_PATTERN_PROPERTY_KEY = "enableFor";
    public final static String ENABLE_FOR_ALL_PATTERN = "(.*)";
    //The authenticator id used in a ServiceAccessToken to identify a token for a specific authenticator
    private String authenticatorId = null;
    private String enableForPattern = "(.*)";

    /**
     * Get a list of human readable credential attribute names, e.g. 'Username'
     * and 'Password' or 'Token'. This array is used to build up user interfaces
     * to obtain an entire credential for a specific authenticator from the user
     * and to map a named credential attribute to its value for storing both in
     * the database.
     *
     * @return An array of human readable credential attribute names.
     */
    public abstract String[] getCredentialAttributeNames();

    /**
     * Generate a service access token holding the authentication information
     * for the provided user. The returned ServiceAccessToken entity is
     * persisted within {@link #storeUserAuthentication(edu.kit.dama.authorization.entities.UserId, java.util.Map)
     * } and is later obtained inside
     * {@link #authenticate(com.sun.jersey.api.core.HttpContext, edu.kit.dama.authorization.entities.GroupId)}.
     * Therefore, the returned ServiceAccessToken should fit the evaluation
     * procedure applied to it during the actual authentication.
     *
     * The implementation of this method may use both, the plain key and the
     * enrypted secret field of ServiceAccessToken or only one of them.
     *
     * @param pUser The user the ServiceAccessToken should be connected with.
     * @param pCredential A map of all credential attributes and their values.
     *
     * @return The ServiceAccessToken.
     *
     * @throws SecretEncryptionException If the secret encryption failed.
     */
    public abstract ServiceAccessToken generateServiceAccessToken(UserId pUser, Map<String, String> pCredential) throws SecretEncryptionException;

    /**
     * Obtain the authorization context for a user in the provided group by
     * using the provided HttpContext. The HttpContext might be used to read
     * HTTP header information, query parameters or other information provided
     * by user client. All relevant authentication information are extracted and
     * validated against the credential database. If the credentials are valid,
     * an authorization context is created using the provided group and
     * containing also the resulting user role.
     *
     * @param httpContext The HttpContext containing all request information.
     * @param groupId The user group on which behalf the user has called the
     * service.
     *
     * @return The authorization context if the authorization succeeds.
     *
     * @throws UnauthorizedAccessAttemptException If the authorization has
     * failed.
     */
    public abstract IAuthorizationContext obtainAuthorizationContext(HttpContext httpContext, GroupId groupId) throws UnauthorizedAccessAttemptException;

    /**
     * Store authentication information for the provided userId. This method
     * checks if all credential attributes are present. If this is the case, the
     * abstract method {@link #storeUserAuthentication(edu.kit.dama.authorization.entities.UserId, java.util.Map)
     * } is called to persist the credential.
     *
     * @param pUser The user the credential should be connected with.
     * @param pCredential A map of all credential attributes and their values.
     *
     * @throws InvalidCredentialException If at least one credential attribute
     * is missing.
     */
    public final void storeUserAuthentication(UserId pUser, Map<String, String> pCredential) throws InvalidCredentialException {
        for (String credentialAttribute : getCredentialAttributeNames()) {
            if (pCredential.get(credentialAttribute) == null) {
                throw new InvalidCredentialException("Credential attribute '" + credentialAttribute + "' is missing.");
            }
        }
        IMetaDataManager manager = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        manager.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            LOGGER.debug("Obtaining token from authenticator implementation.");
            ServiceAccessToken newToken = generateServiceAccessToken(pUser, pCredential);
            LOGGER.debug("Checking for existing token for user '{}' and service '{}'", pUser, getAuthenticatorId());
            ServiceAccessToken existingToken = ServiceAccessUtil.getAccessToken(manager, pUser, getAuthenticatorId());
            if (existingToken != null) {
                LOGGER.debug("Updating existing token for user '{}' and service '{}'", pUser, getAuthenticatorId());
                existingToken.setTokenKey(newToken.getTokenKey());
                existingToken.setTokenSecret(newToken.getTokenSecret());
                existingToken = manager.update(existingToken);
            } else {
                LOGGER.debug("Storing new token for user '{}' and service '{}'", pUser, getAuthenticatorId());
                existingToken = manager.save(newToken);
            }
            LOGGER.debug("ServiceAccessToken with id '{}' successfully stored/updated.", existingToken.getId());
        } catch (UnauthorizedAccessAttemptException | EntityNotFoundException | SecretEncryptionException ex) {
            LOGGER.error("Failed to store user authentication.", ex);
        } finally {
            manager.close();
        }
    }

    /**
     * Authenticate a service access using the provided HttpContext. Depending
     * on the authenticator implementation credentials are obtained from a
     * specific header field or a request parameter. The provided groupId is
     * used to complete the returned AuthorizationContext and to determine the
     * user's role in this context.
     *
     * @param httpContext The HttpContext provided by the user while accessing a
     * REST endpoint.
     * @param groupId The group on which behalf the user accessed the endpoint.
     *
     * @return The AuthorizationContext or a WebApplicationException containing
     * status UNAUTHORIZED.
     *
     * @throws UnauthorizedAccessAttemptException If not authorized.
     */
    public final IAuthorizationContext authenticate(HttpContext httpContext, GroupId groupId) throws UnauthorizedAccessAttemptException {
        String baseUrl = httpContext.getRequest().getBaseUri().toString();
        if (!Pattern.matches(enableForPattern, baseUrl)) {
            throw new UnauthorizedAccessAttemptException("Authenticator not enabled for baseUrl '" + baseUrl + "'. EnablePattern is '" + enableForPattern + "'.");
        }

        return obtainAuthorizationContext(httpContext, groupId);
    }

    /**
     * Build the authorization context based on the provided userId and groupId.
     * The method used the GroupService to determine the role the user can
     * obtain in the provided group and stores this information in the returned
     * context. Furthermore, for security reasons, the maximum role is limited
     * by this method to Role.MANAGER for REST endpoints.
     *
     * @param pUser The user id of the accessing user.
     * @param pGroup The group id on which behalf the user accesses an endpoint.
     *
     * @return The AuthorizationContext.
     *
     * @throws EntityNotFoundException If the provided user is not member of the
     * provided group.
     * @throws UnauthorizedAccessAttemptException If the access to the group
     * service failed for an unknown reason.
     */
    public final IAuthorizationContext buildAuthorizationContext(UserId pUser, GroupId pGroup) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        Role maxRole = (Role) GroupServiceLocal.getSingleton().getMaximumRole(pGroup, pUser, AuthorizationContext.factorySystemContext());
        if (maxRole.moreThan(Role.MANAGER)) {
            maxRole = Role.MANAGER;
        }
        //return context with max. role
        return new AuthorizationContext(pUser, pGroup, maxRole);
    }

    @Override
    public boolean configure(Configuration pConfig) throws ConfigurationException {
        //get authenticatorId
        authenticatorId = pConfig.getString(AUTHENTICATOR_ID_PROPERTY_KEY);

        if (authenticatorId == null) {
            throw new ConfigurationException("Failed to configure AbstractAuthenticator. Mandatory property " + AUTHENTICATOR_ID_PROPERTY_KEY + " is missing.");
        }

        enableForPattern = pConfig.getString(ENABLE_FOR_PATTERN_PROPERTY_KEY);
        if (enableForPattern == null) {
            enableForPattern = ENABLE_FOR_ALL_PATTERN;
        } else {
            //check pattern
            try {
                Pattern.compile(enableForPattern);
            } catch (PatternSyntaxException ex) {
                LOGGER.error("Feiled to compile regex pattern " + enableForPattern, ex);
                throw new ConfigurationException("Failed to configure AbstractAuthenticator. Value of property " + ENABLE_FOR_PATTERN_PROPERTY_KEY + " is no valid regular expression.");
            }
        }

        return performCustomConfiguration(pConfig);
    }

    /**
     * Get the configured service id.
     *
     * @return The service id.
     */
    public String getAuthenticatorId() {
        return authenticatorId;
    }

    /**
     * Set the configured authenticator id. This method is just for internal
     * use.
     *
     * @param authenticatorId The authenticator id.
     */
    public void setAuthenticatorId(String authenticatorId) {
        this.authenticatorId = authenticatorId;
    }

    /**
     * Perform the custom configuration depending on the authenticator
     * implementation.
     *
     * @param pConfig The configuration object.
     *
     * @return TRUE if the configuration succeeded.
     *
     * @throws ConfigurationException If the configuration has failed.
     */
    public abstract boolean performCustomConfiguration(Configuration pConfig) throws ConfigurationException;
}
