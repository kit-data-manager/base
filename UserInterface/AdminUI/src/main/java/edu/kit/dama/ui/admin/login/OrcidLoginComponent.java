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
import edu.kit.dama.ui.commons.util.UIHelper;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.CryptUtil;
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
public class OrcidLoginComponent extends AbstractLoginComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrcidLoginComponent.class);

    public static final String ORCID_CLIENT_ID_PROPERTY = "authorization.login.orcid.clientid";
    public static final String ORCID_CLIENT_SECRET_PROPERTY = "authorization.login.orcid.clientsecret";
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
        return "ORCID";
    }

    @Override
    public String getLoginLabel() {
        return "ORCiD";
    }

    @Override
    public AbstractLayout getLoginForm() {
        if (loginForm == null) {
            Image im = new Image(null, new ExternalResource("http://ebling.library.wisc.edu/images/logos/orcid-hero-logo.png"));
            im.setWidth("300px");
            loginForm = new VerticalLayout(im);
            loginForm.setWidth("300px");
        }
        return loginForm;
    }

    @Override
    public void doLogin(VaadinRequest request) throws UnauthorizedAccessAttemptException {
        String clientId = DataManagerSettings.getSingleton().getStringProperty(ORCID_CLIENT_ID_PROPERTY, null);
        String clientSecret = DataManagerSettings.getSingleton().getStringProperty(ORCID_CLIENT_SECRET_PROPERTY, null);

        if (request == null) {
            //set auth_pending attribute in order to be able to finish authentication later
            VaadinSession.getCurrent().setAttribute("auth_pending", getLoginIdentifier());
            Page.getCurrent().setLocation("https://orcid.org/oauth/authorize?client_id=" + clientId + "&response_type=code&scope=/authenticate&redirect_uri=" + UIHelper.getWebAppUrl().toString());
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
                URI resourceUri = new URL("https://orcid.org/oauth/token").toURI();
                WebResource webResource = client.resource(resourceUri);

                LOGGER.debug("Requesting OAuth2 access token.");
                ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, formData);
                if (response.getStatus() == 200) {
                    LOGGER.debug("Response status is HTTP 200. Parsing JSON response.");
                    String responseData = response.getEntity(String.class);

                    JSONObject responseObject = new JSONObject(responseData);
                    String orcid = responseObject.getString("orcid");
                    //   String accessToken = responseObject.getString("access_token");
                    LOGGER.debug("Obtained ORCiD is {}.", orcid);

                    ServiceAccessToken result = mdm.findSingleResult("Select t FROM ServiceAccessToken t WHERE t.tokenKey=?1", new Object[]{CryptUtil.stringToSHA1(orcid)}, ServiceAccessToken.class);

                    if (result != null) {
                        LOGGER.debug("User with id {} found. Logging in and redirecting user.", result.getUserId());
                        UIHelper.login(new UserId(result.getUserId()), new GroupId(Constants.USERS_GROUP_ID));
                    } else {
                        LOGGER.warn("No user found for ORCiD {}. Login denied.", orcid);
                        throw new UnauthorizedAccessAttemptException("No login credential found for ORCiD '" + orcid + "'.");
                    }
                } else {
                    throw new HttpException("Failed to obtain access token from ORCiD service. Status is " + response.getStatus() + ", response data is: " + response.getEntity(String.class));
                }

                //{"access_token":"84e8f8d0-1df6-43af-9456-6619ef514aed","token_type":"bearer","refresh_token":"2f5116b4-f046-4f69-99c5-097e6066a132","expires_in":631138518,"scope":"/authenticate","name":"Thomas Jejkal","orcid":"0000-0003-2804-688X"}
                //https://pub.orcid.org/v1.2/0000-0003-2804-688X/orcid-bio
            } catch (NoSuchAlgorithmException | KeyManagementException | MalformedURLException | URISyntaxException | HttpException ex) {
                LOGGER.error("Failed to access ORCiD service.", ex);
                throw new UnauthorizedAccessAttemptException("Failed to login via ORCiD.", ex);
            } finally {
                mdm.close();
            }

            String fromPage = (String) VaadinSession.getCurrent().getAttribute("from");
            if (fromPage != null) {
                VaadinSession.getCurrent().setAttribute("from", null);
                Page.getCurrent().setLocation(fromPage);
            } else {
                Page.getCurrent().reload();
            }
        }
    }

    @Override
    public void doRegistration(VaadinRequest request) throws UnauthorizedAccessAttemptException {
        String clientId = DataManagerSettings.getSingleton().getStringProperty(ORCID_CLIENT_ID_PROPERTY, null);
        String clientSecret = DataManagerSettings.getSingleton().getStringProperty(ORCID_CLIENT_SECRET_PROPERTY, null);

        UserData result = new UserData();
        if (request == null) {
            VaadinSession.getCurrent().setAttribute("registration_pending", getLoginIdentifier());
            Page.getCurrent().setLocation("https://orcid.org/oauth/authorize?client_id=" + clientId + "&response_type=code&scope=/authenticate&redirect_uri=" + UIHelper.getWebAppUrl().toString());
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
                WebResource webResource = client.resource("https://orcid.org/oauth/token");
                LOGGER.debug("Obtaining access token.");
                ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, formData);

                if (response.getStatus() == 200) {
                    String responseData = response.getEntity(String.class);
                    JSONObject responseObject = new JSONObject(responseData);
                    String orcid = responseObject.getString("orcid");
                    List<UserData> existingUsers = mdm.findResultList("Select u FROM UserData u WHERE u.distinguishedName=?1", new Object[]{orcid}, UserData.class);
                    if (!existingUsers.isEmpty()) {
                        //user for ORCiD already exists...unable to continue
                        throw new UnauthorizedAccessAttemptException("There is already a user registered for ORCiD " + orcid + ".");
                    }

                    LOGGER.debug("Requesting registration information for ORCiD {}.", orcid);
                    result.setDistinguishedName(orcid);

                    String access_token = responseObject.getString("access_token");
                    //https://pub.orcid.org/v1.2/0000-0003-2804-688X/orcid-bio
                    webResource = client.resource("https://pub.orcid.org/v1.2/" + orcid + "/orcid-bio");
                    LOGGER.debug("Accessing ORCiD service at {}." + webResource.getURI());
                    response = webResource.accept(MediaType.APPLICATION_JSON).header("Authentication", "Bearer " + access_token).get(ClientResponse.class);
                    if (response.getStatus() == 200) {
                        JSONObject orcidResponse = new JSONObject(response.getEntity(String.class));
                        //Sample response with mail visible
                        //{"message-version":"1.2","orcid-profile":{"orcid":null,"orcid-id":null,"orcid-identifier":{"value":null,"uri":"http://orcid.org/0000-0003-2804-688X","path":"0000-0003-2804-688X","host":"orcid.org"},"orcid-deprecated":null,"orcid-preferences":{"locale":"EN"},"orcid-history":{"creation-method":"DIRECT","completion-date":null,"submission-date":{"value":1432891995500},"last-modified-date":{"value":1476705802439},"claimed":{"value":true},"source":null,"deactivation-date":null,"verified-email":{"value":true},"verified-primary-email":{"value":true},"visibility":null},"orcid-bio":{"personal-details":{"given-names":{"value":"Thomas","visibility":null},"family-name":{"value":"Jejkal","visibility":null},"credit-name":{"value":"Thomas Jejkal","visibility":"PUBLIC"},"other-names":null},"biography":null,"researcher-urls":null,"contact-details":{"email":[{"value":"thomas.jejkal@kit.edu","primary":true,"current":true,"verified":true,"visibility":"PUBLIC","source":"0000-0003-2804-688X","source-client-id":null}],"address":{"country":{"value":"DE","visibility":"PUBLIC"}}},"keywords":null,"external-identifiers":null,"delegation":null,"scope":null},"orcid-activities":null,"orcid-internal":null,"type":"USER","group-type":null,"client-type":null},"orcid-search-results":null,"error-desc":null}
                        //Sample response with mail invisible
                        //{"message-version":"1.2","orcid-profile":{"orcid":null,"orcid-id":null,"orcid-identifier":{"value":null,"uri":"http://orcid.org/0000-0003-2804-688X","path":"0000-0003-2804-688X","host":"orcid.org"},"orcid-deprecated":null,"orcid-preferences":{"locale":"EN"},"orcid-history":{"creation-method":"DIRECT","completion-date":null,"submission-date":{"value":1432891995500},"last-modified-date":{"value":1476705875890},"claimed":{"value":true},"source":null,"deactivation-date":null,"verified-email":{"value":true},"verified-primary-email":{"value":true},"visibility":null},"orcid-bio":{"personal-details":{"given-names":{"value":"Thomas","visibility":null},"family-name":{"value":"Jejkal","visibility":null},"credit-name":{"value":"Thomas Jejkal","visibility":"PUBLIC"},"other-names":null},"biography":null,"researcher-urls":null,"contact-details":{"email":[],"address":{"country":{"value":"DE","visibility":"PUBLIC"}}},"keywords":null,"external-identifiers":null,"delegation":null,"scope":null},"orcid-activities":null,"orcid-internal":null,"type":"USER","group-type":null,"client-type":null},"orcid-search-results":null,"error-desc":null}
                        try {
                            JSONObject orcidBio = orcidResponse.getJSONObject("orcid-profile").getJSONObject("orcid-bio");
                            try {
                                JSONObject personalDetails = orcidBio.getJSONObject("personal-details");
                                String lastName = personalDetails.getJSONObject("family-name").getString("value");
                                String firstName = personalDetails.getJSONObject("given-names").getString("value");
                                result.setFirstName(firstName);
                                result.setLastName(lastName);
                            } catch (JSONException ex) {
                                //failed to collect personal information
                                LOGGER.info("No personal-details element found in ORCiD response entity. Skipping first and last name properties.");
                            }

                            try {
                                JSONObject contactDetails = orcidBio.getJSONObject("contact-details");
                                String email = contactDetails.getJSONArray("email").getJSONObject(0).getString("value");
                                result.setEmail(email);
                            } catch (JSONException ex) {
                                //failed to collect email
                                LOGGER.info("No contact-details element found in ORCiD response entity. Skipping email property.");
                            }
                        } catch (JSONException ex) {
                            //failed to collect email
                            LOGGER.info("No orcid-profile and/or orcid-bio elements found in ORCiD response entity. No properties can be obtained.");
                        }
                    } else {
                        LOGGER.warn("Failed to obtain user profile from ORCiD service. Status is " + response.getStatus() + ", response data is: " + response.getEntity(String.class));
                    }
                } else {
                    //unable to obtain ORCiD id...unable to continue 
                    throw new UnauthorizedAccessAttemptException("Failed to obtain access token from ORCiD service. Status is " + response.getStatus() + ", response data is: " + response.getEntity(String.class));
                }
                //{"access_token":"84e8f8d0-1df6-43af-9456-6619ef514aed","token_type":"bearer","refresh_token":"2f5116b4-f046-4f69-99c5-097e6066a132","expires_in":631138518,"scope":"/authenticate","name":"Thomas Jejkal","orcid":"0000-0003-2804-688X"}
            } catch (NoSuchAlgorithmException | KeyManagementException | JSONException ex) {
                LOGGER.error("Failed to collect information from ORCiD service.", ex);
                throw new UnauthorizedAccessAttemptException("Failed to collect information from ORCiD service.", ex);
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
                propertyMap.put(SimpleTokenAuthenticator.USER_TOKEN_PROPERTY_KEY, registeredUser.getDistinguishedName());
                IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
                mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
                try {
                    ServiceAccessToken token = auth.generateServiceAccessToken(new UserId(registeredUser.getDistinguishedName()), propertyMap);
                    mdm.save(token);
                } catch (SecretEncryptionException | UnauthorizedAccessAttemptException ex) {
                    //should never happen
                    LOGGER.error("Failed to do ORCiD post registration. ORCiD login won't be possible for user " + registeredUser.getDistinguishedName(), ex);
                } finally {
                    mdm.close();
                }
                break;
            }
        }
        if (!authenticatorFound) {
            LOGGER.warn("No authenticator with id " + getLoginIdentifier() + " found. ORCiD login won't be possible.");
        }

    }

    @Override
    public void reset() {
        setup(AUTH_MODE.LOGIN, null);
    }
}
