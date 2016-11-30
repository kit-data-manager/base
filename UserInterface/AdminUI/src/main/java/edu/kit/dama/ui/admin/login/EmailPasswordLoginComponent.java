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

import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.util.ServiceAccessUtil;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.ui.admin.utils.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.Constants;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class EmailPasswordLoginComponent extends AbstractLoginComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailPasswordLoginComponent.class);

    private TextField email;
    private PasswordField password;
    private VerticalLayout loginForm;

    @Override
    public String getLoginIdentifier() {
        return "EMAIL_PASSWORD";
    }

    @Override
    public String getLoginLabel() {
        return "Email/Password";
    }

    @Override
    public AbstractLayout getLoginForm() {
        if (loginForm == null) {
            email = UIUtils7.factoryTextField("Email", "Please enter your email.", "300px", true, -1, 255);
            password = UIUtils7.factoryPasswordField("Password", "300px", true, -1, 255);
            loginForm = new VerticalLayout(email, password);
            loginForm.setWidth("300px");
        }
        return loginForm;
    }

    @Override
    public void doLogin(VaadinRequest request) throws UnauthorizedAccessAttemptException {
        if (!UIUtils7.validate(loginForm)) {
            throw new UnauthorizedAccessAttemptException("Login Failed. Please correct the error(s) above.");
        }
        String userMail = email.getValue();
        String userPassword = password.getValue();

        if (userMail == null || password == null) {
            throw new UnauthorizedAccessAttemptException("Please provide username and password.");
        }

        IMetaDataManager manager = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        manager.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            LOGGER.debug("Getting access token for user {}", userMail);
            ServiceAccessToken token = ServiceAccessUtil.getAccessToken(manager, userMail, Constants.MAIN_LOGIN_SERVICE_ID);
            if (token == null) {
                throw new UnauthorizedAccessAttemptException("Login Failed. No login information found for email " + userMail + ".");
            } else {
                LOGGER.debug("Access token sucessfully obtained. Checking password.");
            }

            if (!userPassword.equals(token.getSecret())) {
                throw new UnauthorizedAccessAttemptException("Login Failed. Wrong password for email " + userMail + ".");
            } else {
                LOGGER.debug("Password is correct. Getting user information.");
                //login successful
                UserData template = new UserData();
                template.setDistinguishedName(token.getUserId());
                List<UserData> result = manager.find(template, template);
                if (result.isEmpty() || result.size() > 1) {
                    throw new Exception("Invalid number of user entries (" + result.size() + ") found for userId " + token.getUserId() + ". Please contact a system administrator.");
                }
                LOGGER.debug("User information obtained. Setting logged in user and updating main layout.");
                //do actual login
                UIHelper.login(new UserId(result.get(0).getDistinguishedName()), new GroupId(Constants.USERS_GROUP_ID));
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to access login database.", ex);
            throw new UnauthorizedAccessAttemptException("Login failed due to an internal error. Please contact an administrator.");
        } finally {
            manager.close();
        }

        String fromPage = (String) VaadinSession.getCurrent().getAttribute("from");
        if (fromPage != null) {
            VaadinSession.getCurrent().setAttribute("from", null);
            Page.getCurrent().setLocation(fromPage);
        } else {
            Page.getCurrent().setLocation(UIHelper.getWebAppUrl().toString());
        }
    }

    @Override
    public void doRegistration(VaadinRequest request) {
        UserData newUser = new UserData();
        newUser.setDistinguishedName(UUID.randomUUID().toString());
        setup(AUTH_MODE.REGISTRATION, newUser);
    }

    @Override
    public void doPostRegistration(UserData registeredUser) {
        //nothing to do here ... maybe credential creation?
    }

    @Override
    public void reset() {
        email.setValue("");
        password.setValue("");
        setup(AUTH_MODE.LOGIN, null);
    }

}
