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
package edu.kit.dama.ui.admin.login;

import com.sun.jersey.api.core.HttpRequestContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.rest.util.auth.AbstractAuthenticator;
import java.util.Map;
import org.apache.commons.configuration.Configuration;

/**
 * Dummy authenticator for the main login. This authentictor is used at the
 * profile view of the AdminUI in order to collect the credential properties and
 * to generate a service access token in the expected form.
 *
 * @author jejkal
 */
public class MainLoginAuthenticator extends AbstractAuthenticator {

    @Override
    public String[] getCredentialAttributeNames() {
        return new String[]{"eMail", "Password"};
    }

    @Override
    public ServiceAccessToken generateServiceAccessToken(UserId pUser, Map<String, String> pCredential) throws SecretEncryptionException {
        ServiceAccessToken token = new ServiceAccessToken(pUser.getStringRepresentation(), "mainLogin");
        token.setTokenKey(pCredential.get("eMail"));
        token.setSecret(pCredential.get("Password"));
        return token;
    }

    @Override
    public IAuthorizationContext obtainAuthorizationContext(HttpRequestContext httpContext, GroupId groupId) throws UnauthorizedAccessAttemptException {
        //not supported
        return null;
    }

    @Override
    public boolean performCustomConfiguration(Configuration pConfig) throws ConfigurationException {
        //not supported
        return true;
    }

    @Override
    public String getAuthenticatorId() {
        return "mainLogin";
    }

    @Override
    public void setAuthenticatorId(String authenticatorId) {
        //not supported
    }
}
