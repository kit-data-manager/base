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
package edu.kit.dama.ui.admin;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
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
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.ui.components.ConfirmationWindow7;
import edu.kit.dama.ui.components.IConfirmationWindowListener7;
import edu.kit.dama.util.Constants;
import java.util.Date;
import java.util.List;

/**
 *
 * @author mf6319
 */
public class RegistrationFormView extends CustomComponent {

  private final TextField screenName = UIUtils7.factoryTextField("Screen Name", "Insert your screen name", "300px", true, 3, 255);
  private final TextField firstName = UIUtils7.factoryTextField("First Name", "Insert your first name", "150px", true, 3, 255);
  private final TextField lastName = UIUtils7.factoryTextField("Last Name", "Insert your last name", "150px", true, 3, 255);
  private final TextField email = UIUtils7.factoryTextField("Email", "Insert your email", "300px", true, 3, 255);
  private final PasswordField password = UIUtils7.factoryPasswordField("Password", "300px", true, 3, 255);
  private final PasswordField passwordVerify = UIUtils7.factoryPasswordField("Repeat Password", "300px", true, 3, 255);
  private Button register;
  private Button cancel;
  private VerticalLayout mainLayout;
  private final IRegistrationCallback callback;

  public RegistrationFormView(IRegistrationCallback pCallback) {
    if (pCallback == null) {
      throw new IllegalArgumentException("Argument pCallback must not be null");
    }
    callback = pCallback;
    initComponent();
    setCompositionRoot(mainLayout);
  }

  /**
   * Initialize the user interface components.
   */
  private void initComponent() {
    register = new Button("Register");
    register.setWidth("100px");
    register.addClickListener(new Button.ClickListener() {

      @Override
      public void buttonClick(Button.ClickEvent event) {
        try {
          doRegister();
        } catch (UserRegistrationException e) {
          ConfirmationWindow7.showConfirmation("Registration failed.", e.getMessage(), ConfirmationWindow7.OPTION_TYPE.OK_OPTION, ConfirmationWindow7.MESSAGE_TYPE.ERROR, new IConfirmationWindowListener7() {

            @Override
            public void fireConfirmationWindowCloseEvent(ConfirmationWindow7.RESULT pResult) {
              //no work needed here...we'll stay on the page
            }
          });
        }
      }
    });
    cancel = new Button("Cancel");
    cancel.setWidth("100px");
    cancel.addClickListener(new Button.ClickListener() {

      @Override
      public void buttonClick(Button.ClickEvent event) {
        //we go back
        callback.fireRegistrationCanceledEvent(RegistrationFormView.this);
      }
    });

    GridLayout registerForm = new UIUtils7.GridLayoutBuilder(2, 6).
            addComponent(screenName, 0, 0, 2, 1).
            addComponent(lastName, 0, 1, 1, 1).addComponent(firstName, 1, 1, 1, 1).
            addComponent(email, 0, 2, 2, 1).
            addComponent(password, 0, 3, 2, 1).
            addComponent(passwordVerify, 0, 4, 2, 1).
            addComponent(register, 0, 5, 1, 1).
            addComponent(cancel, 1, 5, 1, 1).
            getLayout();
    registerForm.setComponentAlignment(register, Alignment.MIDDLE_LEFT);
    registerForm.setComponentAlignment(cancel, Alignment.MIDDLE_RIGHT);
    registerForm.setSpacing(true);
    registerForm.setMargin(true);
    mainLayout = new VerticalLayout(registerForm);
    mainLayout.setComponentAlignment(registerForm, Alignment.MIDDLE_CENTER);
    mainLayout.setSizeFull();
  }

  /**
   * Do the user registration including all validation steps.
   *
   * @throws UserRegistrationException If the registration fails for any reason.
   */
  private void doRegister() throws UserRegistrationException {
    if (!UIUtils7.validate(mainLayout)) {
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
      GroupServiceLocal.getSingleton().addUser(new GroupId(Constants.USERS_GROUP_ID), userScreenName, Role.MEMBER, ctx);
    } catch (UnauthorizedAccessAttemptException e) {
      throw new UserRegistrationException("Failed to register new user for screenName " + screenName + ".", e);
    } catch (EntityAlreadyExistsException e) {
      throw new UserRegistrationException("User with screenName " + userScreenName + " already exists.");
    } catch (EntityNotFoundException e) {
      handleRegistrationError(userScreenName, "Failed to add user with screenName " + userScreenName + " to default group 'USERS'.");
    }

    //save user metadata
    template.setFirstName(userFirstName);
    template.setLastName(userLastName);
    template.setValidFrom(new Date());
    template.setDistinguishedName(userScreenName.getStringRepresentation());
    try {
      template = mdm.save(template);
    } catch (UnauthorizedAccessAttemptException e) {
      handleRegistrationError(userScreenName, "Failed to save user properties.");
    }

    //save password entity
    try {
      ServiceAccessToken token = new ServiceAccessToken(userScreenName.getStringRepresentation(), Constants.MAIN_LOGIN_SERVICE_ID);
      token.setTokenKey(userMail);
      token.setSecret(userPassword1);
      mdm.save(token);
    } catch (UnauthorizedAccessAttemptException | SecretEncryptionException e) {
      handleRegistrationError(userScreenName, "Failed to save login information.");
    }

    callback.fireRegistrationSucceededEvent(RegistrationFormView.this, template);
  }

  /**
   * Handle fatal registration errors which occur after an authorization userId
   * has been added. In this case, the according user is disabled and an
   * exception is thrown. To re-activate the user, manual steps must be
   * performed.
   *
   * @param pUserId The userId for which the registration failed.
   * @param pErrorMessage The error message that will be part of the thrown
   * exception
   *
   * @throws UserRegistrationException The exception containing pErrorMessage.
   */
  private void handleRegistrationError(UserId pUserId, String pErrorMessage) throws UserRegistrationException {
    try {
      UserServiceLocal.getSingleton().setRoleRestriction(pUserId, Role.NO_ACCESS, AuthorizationContext.factorySystemContext());
    } catch (UnauthorizedAccessAttemptException ex) {
    } catch (EntityNotFoundException ex) {
    }
    throw new UserRegistrationException(pErrorMessage);
  }

}
