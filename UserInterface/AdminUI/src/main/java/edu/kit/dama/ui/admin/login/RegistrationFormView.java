/* 
 * Copyright 2015 Karlsruhe Institute of Technology.
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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.ui.admin.exception.UserRegistrationException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.ui.admin.exception.RegistrationAbortedException;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.Constants;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class RegistrationFormView extends CustomComponent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationFormView.class);
    
    private final TextField screenName = UIUtils7.factoryTextField("Screen Name", "Insert your screen name", "100%", true, 3, 255);
    private final TextField firstName = UIUtils7.factoryTextField("First Name", "Insert your first name", "100%", true, 3, 255);
    private final TextField lastName = UIUtils7.factoryTextField("Last Name", "Insert your last name", "100%", true, 3, 255);
    private final TextField email = UIUtils7.factoryTextField("Email", "Insert your email", "100%", true, 3, 255);
    private final PasswordField password = UIUtils7.factoryPasswordField("Password", "100%", true, 3, 255);
    private final PasswordField passwordVerify = UIUtils7.factoryPasswordField("Repeat Password", "100%", true, 3, 255);
    private VerticalLayout mainLayout;
    private GridLayout registerForm;
    
    public RegistrationFormView() {
        initComponent();
        setCompositionRoot(mainLayout);
    }
    
    public void loadFromUserData(UserData template) {
        if (template != null) {
            screenName.setValue(template.getDistinguishedName());
            firstName.setValue(template.getFirstName());
            lastName.setValue(template.getLastName());
            email.setValue(template.getEmail());
        } else {
            screenName.setValue("");
            firstName.setValue("");
            lastName.setValue("");
            email.setValue("");
        }
    }

    /**
     * Initialize the user interface components.
     */
    private void initComponent() {
        screenName.setRequired(true);
        email.setRequired(true);
        email.setNullRepresentation("");
        registerForm = new UIUtils7.GridLayoutBuilder(2, 5).
                addComponent(screenName, 0, 0, 2, 1).
                addComponent(lastName, 0, 1, 1, 1).addComponent(firstName, 1, 1, 1, 1).
                addComponent(email, 0, 2, 2, 1).
                addComponent(password, 0, 3, 2, 1).
                addComponent(passwordVerify, 0, 4, 2, 1).
                getLayout();
        registerForm.setSpacing(true);
        registerForm.setMargin(false);
        registerForm.setSizeFull();
        mainLayout = new VerticalLayout(registerForm);
        mainLayout.setComponentAlignment(registerForm, Alignment.MIDDLE_CENTER);
        mainLayout.setSizeFull();
    }

    /**
     * Do the user registration including all validation steps.
     *
     * @return the registered user if registration was successful.
     *
     * @throws UserRegistrationException If validating the provided information
     * fails.
     * @throws RegistrationAbortedException If the registration fails after a
     * user has been created.
     */
    public UserData register() throws UserRegistrationException, RegistrationAbortedException {
        if (!UIUtils7.validate(registerForm) || email.getValue() == null || screenName.getValue() == null) {
            //something is wrong...show error
            throw new UserRegistrationException("Form validation failed. Please check all fields again.");
        }
        UserId userScreenName = new UserId(screenName.getValue());
        String userMail = email.getValue().trim();
        String userFirstName = firstName.getValue().trim();
        String userLastName = lastName.getValue().trim();
        String userPassword1 = password.getValue().trim();
        String userPassword2 = passwordVerify.getValue().trim();
        
        IAuthorizationContext ctx = AuthorizationContext.factorySystemContext();
        
        UserData template = new UserData();
        template.setEmail(userMail);
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            try {
                List<UserData> existing = mdm.find(template, template);
                if (!existing.isEmpty()) {
                    throw new UserRegistrationException("The email address " + userMail + " is already used.");
                }
            } catch (UnauthorizedAccessAttemptException ex) {
                throw new UserRegistrationException("Failed to check email " + userMail + ". Please try again later.");
            }
            
            if (userPassword1 == null || userPassword2 == null) {
                throw new UserRegistrationException("Please provide a password and its confirmation.");
            }
            
            if (!userPassword1.equals(userPassword2)) {
                throw new UserRegistrationException("Passwords are not equal.");
            }

            //save authorization information
            try {
                UserServiceLocal.getSingleton().register(userScreenName, Role.MANAGER, ctx);
            } catch (UnauthorizedAccessAttemptException e) {
                LOGGER.error("Failed to register new user for screenName '" + screenName + "'.", e);
                throw new UserRegistrationException("Failed to register new user for screenName " + screenName + ".", e);
            } catch (EntityAlreadyExistsException e) {
                LOGGER.error("User with screenName '" + screenName + "' already exists.", e);
                throw new UserRegistrationException("User with screenName " + userScreenName + " already exists.");
            }
            
            try {
                GroupServiceLocal.getSingleton().addUser(new GroupId(Constants.USERS_GROUP_ID), userScreenName, Role.MEMBER, ctx);
            } catch (EntityNotFoundException | UnauthorizedAccessAttemptException | EntityAlreadyExistsException e) {
                LOGGER.error("Failed to add user with screen name '" + userScreenName + "' to group USERS.", e);
                handleRegistrationError(userScreenName, "Failed to add user with screenName '" + userScreenName + "' to default group 'USERS'.");
            }

            //save user metadata
            template.setFirstName(userFirstName);
            template.setLastName(userLastName);
            template.setValidFrom(new Date());
            template.setDistinguishedName(userScreenName.getStringRepresentation());
            try {
                template = mdm.save(template);
            } catch (UnauthorizedAccessAttemptException e) {
                LOGGER.error("Failed to create userdata entity for user with screen name '" + userScreenName + "'.", e);
                handleRegistrationError(userScreenName, "Failed to save user properties.");
            }

            //save password entity
            try {
                ServiceAccessToken token = new ServiceAccessToken(userScreenName.getStringRepresentation(), Constants.MAIN_LOGIN_SERVICE_ID);
                token.setTokenKey(userMail);
                token.setSecret(userPassword1);
                mdm.save(token);
            } catch (UnauthorizedAccessAttemptException | SecretEncryptionException e) {
                LOGGER.error("Failed to store service access token for user with screen name '" + userScreenName + "'.", e);
                handleRegistrationError(userScreenName, "Failed to save login information.");
            }
        } finally {
            mdm.close();
        }
        return template;
    }

    /**
     * Handle fatal registration errors which occur after an authorization
     * userId has been added. In this case, the according user is disabled and
     * an exception is thrown. To re-activate the user, manual steps must be
     * performed.
     *
     * @param pUserId The userId for which the registration failed.
     * @param pErrorMessage The error message that will be part of the thrown
     * exception
     *
     * @throws RegistrationAbortedException The exception containing
     * pErrorMessage.
     */
    private void handleRegistrationError(UserId pUserId, String pErrorMessage) throws RegistrationAbortedException {
        try {
            UserServiceLocal.getSingleton().setRoleRestriction(pUserId, Role.NO_ACCESS, AuthorizationContext.factorySystemContext());
        } catch (UnauthorizedAccessAttemptException | EntityNotFoundException ex) {
            LOGGER.error("Failed to disable user with id '" + pUserId + "'.", ex);
            throw new RegistrationAbortedException(pErrorMessage, ex);
        }
    }
    
}
