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

import com.vaadin.server.Page;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretDecryptionException;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.mdm.admin.util.ServiceAccessUtil;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.ui.components.ConfirmationWindow7;
import static edu.kit.dama.ui.components.ConfirmationWindow7.RESULT.YES;
import edu.kit.dama.ui.components.IConfirmationWindowListener7;
import edu.kit.dama.util.Constants;

/**
 *
 * @author mf6319
 */
public class ProfileView extends CustomComponent {

  private final AdminUIMainView parent;
  private VerticalLayout mainLayout;
  private Label oauthKey;
  private Label oauthSecret;
  private Label screenName;
  private Label firstName;
  private Label lastName;
  private Label email;
  private PasswordField currentPassword;
  private PasswordField newPassword;
  private PasswordField newRetypePassword;
  private FormLayout passwordForm;

  public ProfileView(AdminUIMainView pParent) {
    parent = pParent;
    buildMainLayout();
    setCompositionRoot(mainLayout);
    setSizeFull();
  }

  /**
   * Build the main layout.
   */
  private void buildMainLayout() {
    //information area
    screenName = new Label();
    screenName.setCaption("Screen Name:");
    screenName.setStyleName("form");
    firstName = new Label();
    firstName.setCaption("First Name:");
    firstName.setStyleName("form");
    lastName = new Label();
    lastName.setCaption("Last Name:");
    lastName.setStyleName("form");
    email = new Label();
    email.setCaption("Email:");
    email.setStyleName("form");
    FormLayout userInformationForm = new FormLayout(screenName, firstName, lastName, email);
    userInformationForm.setStyleName("form");
    userInformationForm.setCaption("User Information");
    userInformationForm.setWidth("400px");
    //change password
    currentPassword = UIUtils7.factoryPasswordField("Current Password:", "200px", true, -1, 255);
    currentPassword.setStyleName("form");
    newPassword = UIUtils7.factoryPasswordField("New Password:", "200px", true, 6, 255);
    newPassword.setStyleName("form");
    newRetypePassword = UIUtils7.factoryPasswordField("Repeat New Password:", "200px", true, 6, 255);
    newRetypePassword.setStyleName("form");
    Button changePasswordButton = new Button("Change", new Button.ClickListener() {

      @Override
      public void buttonClick(Button.ClickEvent event) {
        doPasswordChange();
      }
    });

    passwordForm = new FormLayout(currentPassword, newPassword, newRetypePassword, changePasswordButton, new Label("<hr/>", ContentMode.HTML));
    passwordForm.setCaption("Change Password");
    passwordForm.setStyleName("form");
    passwordForm.setWidth("400px");
    //rest credentials
    oauthKey = new Label();
    oauthKey.setCaption("Access Token:");
    oauthKey.setStyleName("form");
    oauthSecret = new Label();
    oauthSecret.setCaption("Access Token Secret:");
    oauthSecret.setStyleName("form");
    Button changeRestCredential = new Button("Regenerate", new Button.ClickListener() {
      @Override
      public void buttonClick(Button.ClickEvent event) {
        ConfirmationWindow7.showConfirmation(
                "Regenerate OAuth Credentials",
                "Attention: The regeneration of OAuth credentials invalidates all "
                + "running/pending ingests and downloads, as well as other REST-based accesses.<br/>"
                + "Do you really want to regenerate your OAuth credentials?", ConfirmationWindow7.OPTION_TYPE.YES_NO_OPTION, ConfirmationWindow7.MESSAGE_TYPE.WARNING, new IConfirmationWindowListener7() {
                  @Override
                  public void fireConfirmationWindowCloseEvent(ConfirmationWindow7.RESULT pResult) {
                    switch (pResult) {
                      case YES:
                        performOAuthCredentialReset();
                        break;
                      default:
                      //do nothing
                    }
                  }
                });
      }
    });
    FormLayout restCredentialForm = new FormLayout(oauthKey, oauthSecret, changeRestCredential, new Label("<hr/>", ContentMode.HTML));
    restCredentialForm.setCaption("OAuth Credentials");
    restCredentialForm.setStyleName("form");
    restCredentialForm.setWidth("400px");

    mainLayout = new VerticalLayout(userInformationForm, passwordForm, restCredentialForm);
    mainLayout.setSpacing(true);
    mainLayout.setMargin(true);
    mainLayout.setComponentAlignment(userInformationForm, Alignment.TOP_CENTER);
    mainLayout.setComponentAlignment(passwordForm, Alignment.TOP_CENTER);
    mainLayout.setComponentAlignment(restCredentialForm, Alignment.TOP_CENTER);
    mainLayout.setSizeFull();
    reload();
  }

  protected void update() {
    reload();
  }

  /**
   * Reload the form data.
   */
  public void reload() {
    //set user information
    screenName.setValue(parent.getLoggedInUser().getDistinguishedName());
    firstName.setValue(parent.getLoggedInUser().getFirstName());
    lastName.setValue(parent.getLoggedInUser().getLastName());
    email.setValue(parent.getLoggedInUser().getEmail());

    //set OAuth credentials
    try {
      ServiceAccessToken token = ServiceAccessUtil.getAccessToken(parent.getMetaDataManager(), new UserId(parent.getLoggedInUser().getDistinguishedName()), Constants.REST_API_SERVICE_KEY);
      if (token != null) {
        oauthKey.setValue(token.getTokenKey());
        oauthSecret.setValue(token.getSecret());
      } else {
        oauthKey.setValue("---");
        oauthSecret.setValue("---");
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      //no access?
      new Notification("Error",
              "Failed to obtain OAuth credentials from database.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
    } catch (Exception ex) {
      new Notification("Error",
              "Failed to decrypt old OAuth credentials.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
    }
  }

  /**
   * Regenerate OAuth credentials and update UI.
   */
  private void performOAuthCredentialReset() {
    try {
      ServiceAccessToken token = ServiceAccessUtil.getAccessToken(parent.getMetaDataManager(), new UserId(parent.getLoggedInUser().getDistinguishedName()), Constants.REST_API_SERVICE_KEY);
      if (token == null) {
        //create new token
        token = new ServiceAccessToken(parent.getLoggedInUser().getDistinguishedName(), Constants.REST_API_SERVICE_KEY);
      }
      token.regenerate();
      parent.getMetaDataManager().save(token);
      oauthKey.setValue(token.getTokenKey());
      oauthSecret.setValue(token.getSecret());
      new Notification("Success",
              "OAuth credentials successfully regenerated.", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
    } catch (UnauthorizedAccessAttemptException | SecretEncryptionException | SecretDecryptionException ex) {
      // LOGGER.error("Failed to regenerate REST credentials.", ex);
      new Notification("Error",
              "Failed to regenerate OAuth credentials.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
    }
  }

  /**
   * Change the user password.
   */
  private void doPasswordChange() {
    if (!UIUtils7.validate(passwordForm)) {
      new Notification("Failed to change Password.",
              "Please correct the errors first.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
      return;
    }
    String sCurrentPassword = currentPassword.getValue();
    String sNewPassword = newPassword.getValue();
    String sNewPassword2 = newRetypePassword.getValue();

    try {
      ServiceAccessToken token = ServiceAccessUtil.getAccessToken(parent.getMetaDataManager(), parent.getLoggedInUser().getEmail(), Constants.MAIN_LOGIN_SERVICE_ID);
      if (token != null) {
        if (!sCurrentPassword.equals(token.getSecret())) {
          currentPassword.setComponentError(new UserError("Wrong password."));
          return;
        }
        if (!sNewPassword.equals(sNewPassword2)) {
          new Notification("Passwords not equal.",
                  "Please ensure, that both passwords are equal.", Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
        } else {
          token.setSecret(sNewPassword);
          parent.getMetaDataManager().save(token);
          currentPassword.setValue("");
          newPassword.setValue("");
          newRetypePassword.setValue("");
          new Notification("Password changed.",
                  "Your password has been successfully changed.", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
        }
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      //no access?
      new Notification("Error",
              "Failed to obtain old password from database.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
    } catch (SecretEncryptionException | SecretDecryptionException ex) {
      new Notification("Error",
              "Failed to decrypt old password.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
    }
  }
}
