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

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.util.auth.AbstractAuthenticator;
import edu.kit.dama.rest.util.auth.AuthenticatorFactory;
import edu.kit.dama.rest.util.auth.impl.SimpleTokenAuthenticator;
import edu.kit.dama.ui.admin.utils.UIHelper;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class B2AccessLoginComponent extends AbstractLoginComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(B2AccessLoginComponent.class);

    public static final String B2ACCESS_CLIENT_ID_PROPERTY = "authorization.login.b2access.clientid";
    public static final String B2ACCESS_CLIENT_SECRET_PROPERTY = "authorization.login.b2access.clientsecret";
    private VerticalLayout loginForm;
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

    private final static HostnameVerifier VERIFIER = (String hostname, SSLSession session) -> true;

    @Override
    public String getLoginIdentifier() {
        return "B2ACCESS";
    }

    @Override
    public String getLoginLabel() {
        return "B2ACCESS";
    }

    @Override
    public AbstractLayout getLoginForm() {
        if (loginForm == null) {
            Image im = new Image(null, new ExternalResource("https://b2access.eudat.eu:8443/home/VAADIN/themes/common/img/logo.png"));
            im.setWidth("300px");
            loginForm = new VerticalLayout(im);
            loginForm.setWidth("300px");
        }
        return loginForm;
    }

    @Override
    public void doLogin(VaadinRequest request) throws UnauthorizedAccessAttemptException {
        String clientId = DataManagerSettings.getSingleton().getStringProperty(B2ACCESS_CLIENT_ID_PROPERTY, null);
        String clientSecret = DataManagerSettings.getSingleton().getStringProperty(B2ACCESS_CLIENT_SECRET_PROPERTY, null);

        if (request == null) {
            //set auth_pending attribute in order to be able to finish authentication later
            VaadinSession.getCurrent().setAttribute("auth_pending", getLoginIdentifier());
            Page.getCurrent().setLocation("https://unity.eudat-aai.fz-juelich.de:8443/oauth2-as/oauth2-authz?client_id=" + clientId + "&response_type=code&scope=/authenticate&redirect_uri=" + UIHelper.getWebAppUrl().toString());
        } else {
            //delete auth_pending attribute as we'll finish now or never
            VaadinSession.getCurrent().setAttribute("auth_pending", null);
            //obtain remaining information and do redirect
            //do actual login
            LOGGER.debug("Obtaining OAuth2 code from URL parameter.");
            String code = request.getParameter("code");

            MultivaluedMap formData = new MultivaluedMapImpl();
            formData.putSingle("client_id", clientId);
            formData.putSingle("client_secret", clientSecret);
            formData.putSingle("grant_type", "authorization_code");
            formData.putSingle("redirect_uri", UIHelper.getWebAppUrl().toString());
            formData.putSingle("code", code);

            ClientConfig config = new DefaultClientConfig();
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{TRUST_MANAGER}, new SecureRandom());

                config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(VERIFIER, ctx));
                Client client = Client.create(config);
                WebResource webResource = client.resource("https://unity.eudat-aai.fz-juelich.de:8443/oauth2/token");
                webResource.addFilter(new HTTPBasicAuthFilter("KITDM", "0kudH2O."));

                LOGGER.debug("Obtaining access token.");
                ClientResponse response = webResource.header("Content-Type", "application/x-www-form-urlencoded").accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, formData);

                if (response.getStatus() == 200) {
                    LOGGER.debug("Response status is HTTP 200. Parsing JSON response.");
                    String responseData = response.getEntity(String.class);
                    JSONObject responseObject = new JSONObject(responseData);
                    String access_token = responseObject.getString("access_token");
                    webResource = client.resource("https://unity.eudat-aai.fz-juelich.de:8443/oauth2/userinfo");
                    LOGGER.debug("Accessing B2Access UserInfo at {}." + webResource.getURI());
                    response = webResource.header("Content-Type", "application/x-www-form-urlencoded").accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + access_token).get(ClientResponse.class);

                    if (response.getStatus() == 200) {
                        JSONObject userInfoResponse = new JSONObject(response.getEntity(String.class));
                        String userId = userInfoResponse.getString("sub");
                        UserData result = mdm.findSingleResult("Select u FROM UserData u WHERE u.distinguishedName=?1", new Object[]{userId}, UserData.class);
                        if (result != null) {
                            LOGGER.debug("User with distinguished name {} found. Logging in and redirecting user.", userId);
                            UIHelper.login(new UserId(result.getDistinguishedName()), new GroupId(Constants.USERS_GROUP_ID));
                        } else {
                            LOGGER.warn("No user found for ORCiD {}. Login denied.", userId);
                            throw new UnauthorizedAccessAttemptException("No user found for ORCiD '" + userId + "'.");
                        }
                    } else {
                        //failed, not enough information to proceed!
                    }
                } else {
                    throw new HttpException("Failed to obtain access token from ORCiD service. Status is " + response.getStatus() + ", response data is: " + response.getEntity(String.class));
                }

                //{"access_token":"84e8f8d0-1df6-43af-9456-6619ef514aed","token_type":"bearer","refresh_token":"2f5116b4-f046-4f69-99c5-097e6066a132","expires_in":631138518,"scope":"/authenticate","name":"Thomas Jejkal","orcid":"0000-0003-2804-688X"}
                //https://pub.orcid.org/v1.2/0000-0003-2804-688X/orcid-bio
            } catch (NoSuchAlgorithmException | KeyManagementException | HttpException ex) {
                LOGGER.error("Failed to access B2Access service.", ex);
                throw new UnauthorizedAccessAttemptException("Failed to login via B2Access.", ex);
            } finally {
                mdm.close();
            }

            String fromPage = (String) VaadinSession.getCurrent().getAttribute("from");
            if (fromPage != null) {
                VaadinSession.getCurrent().setAttribute("from", null);
                Page.getCurrent().setLocation(fromPage);
            } else {
                Page.getCurrent().setLocation(UIHelper.getWebAppUrl().toString());
            }
        }
    }

    @Override
    public void doRegistration(VaadinRequest request) throws UnauthorizedAccessAttemptException {
        String clientId = DataManagerSettings.getSingleton().getStringProperty(B2ACCESS_CLIENT_ID_PROPERTY, null);
        String clientSecret = DataManagerSettings.getSingleton().getStringProperty(B2ACCESS_CLIENT_SECRET_PROPERTY, null);

        UserData result = new UserData();
        if (request == null) {
            VaadinSession.getCurrent().setAttribute("registration_pending", getLoginIdentifier());
            Page.getCurrent().setLocation("https://unity.eudat-aai.fz-juelich.de:8443/oauth2-as/oauth2-authz?client_id=" + clientId + "&response_type=code&scope=write&redirect_uri=" + UIHelper.getWebAppUrl().toString());
        } else {
            //delete auth_pending attribute as we'll finish now or never
            VaadinSession.getCurrent().setAttribute("registration_pending", null);
            //obtain remaining information and do redirect
            //do actual login
            LOGGER.debug("Obtaining OAuth2 code from URL parameter.");
            String code = request.getParameter("code");

            MultivaluedMap formData = new MultivaluedMapImpl();
            formData.putSingle("client_id", clientId);
            formData.putSingle("client_secret", clientSecret);
            formData.putSingle("grant_type", "authorization_code");
            formData.putSingle("redirect_uri", UIHelper.getWebAppUrl().toString());
            formData.putSingle("code", code);

            ClientConfig config = new DefaultClientConfig();
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{TRUST_MANAGER}, new SecureRandom());
                config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(VERIFIER, ctx));
                Client client = Client.create(config);
                WebResource webResource = client.resource("https://unity.eudat-aai.fz-juelich.de:8443/oauth2/token");
                webResource.addFilter(new HTTPBasicAuthFilter("KITDM", "0kudH2O."));

                LOGGER.debug("Obtaining access token.");
                ClientResponse response = webResource.header("Content-Type", "application/x-www-form-urlencoded").accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, formData);

                if (response.getStatus() == 200) {
                    String responseData = response.getEntity(String.class);
                    JSONObject responseObject = new JSONObject(responseData);
                    String access_token = responseObject.getString("access_token");
                    webResource = client.resource("https://unity.eudat-aai.fz-juelich.de:8443/oauth2/userinfo");

                    LOGGER.debug("Accessing B2Access UserInfo at {}." + webResource.getURI());
                    response = webResource.header("Content-Type", "application/x-www-form-urlencoded").accept(MediaType.APPLICATION_JSON).header("Authorization", "Bearer " + access_token).get(ClientResponse.class);

                    if (response.getStatus() == 200) {
                        JSONObject userInfoResponse = new JSONObject(response.getEntity(String.class));
                        try {
                            String userId = userInfoResponse.getString("sub");
                            List<UserData> existingUsers = mdm.findResultList("Select u FROM UserData u WHERE u.distinguishedName=?1", new Object[]{userId}, UserData.class);
                            if (!existingUsers.isEmpty()) {
                                //user for B2Access subject already exists...unable to continue
                                throw new UnauthorizedAccessAttemptException("There is already a user registered for the obtained B2Access id '" + userId + "'.");
                            }
                            result.setDistinguishedName(userId);
                        } catch (JSONException ex) {
                            //failed, not enough information to proceed!
                        }
                    } else {
                        //failed, not enough information to proceed!
                    }
                } else {
                    //failed, not enough information to proceed!
                }
            } catch (NoSuchAlgorithmException | KeyManagementException | JSONException ex) {
                LOGGER.error("Failed to collect information from B2Access service.", ex);
                throw new UnauthorizedAccessAttemptException("Failed to collect information from B2Access service.", ex);
            } finally {
                mdm.close();
            }
            setup(AUTH_MODE.REGISTRATION, result);
        }
    }

    @Override
    public void doPostRegistration(UserData registeredUser) {
        boolean authenticatorFound = false;
        for (AbstractAuthenticator auth : AuthenticatorFactory.getInstance().getAuthenticators()) {
            if (auth.getAuthenticatorId().equals(getLoginIdentifier())) {
                authenticatorFound = true;
                Map<String, String> propertyMap = new HashMap<>();
                propertyMap.put(SimpleTokenAuthenticator.USER_TOKEN_KEY, registeredUser.getDistinguishedName());
                IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
                mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
                try {
                    ServiceAccessToken token = auth.generateServiceAccessToken(new UserId(registeredUser.getDistinguishedName()), propertyMap);
                    mdm.save(token);
                } catch (SecretEncryptionException | UnauthorizedAccessAttemptException ex) {
                    //should never happen
                    LOGGER.error("Failed to do B2ACCESS post registration. B2ACCESS login won't be possible for user " + registeredUser.getDistinguishedName(), ex);
                } finally {
                    mdm.close();
                }
                break;
            }
        }
        if (!authenticatorFound) {
            LOGGER.warn("No authenticator with id " + getLoginIdentifier() + " found. B2ACCESS login won't be possible.");
        }

    }

    @Override
    public void reset() {
        setup(AUTH_MODE.LOGIN, null);
    }
}
