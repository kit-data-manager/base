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

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretDecryptionException;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.admin.utils.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.ui.components.ConfirmationWindow7;
import static edu.kit.dama.ui.components.ConfirmationWindow7.RESULT.YES;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class ProfileView extends CustomComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileView.class);

    private GridLayout mainLayout;
    private Label screenName;
    private Label firstName;
    private Label lastName;
    private Label email;
    private Table credentialTable;
    private ServiceAccessTokenDialog tokenDialog;
    private boolean showSecrets = false;

    public ProfileView() {
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
        tokenDialog = new ServiceAccessTokenDialog();
        //Credentials
        //table with serviceId, key, secret for logged in userId
        //---mainLogin and webdav are special (username = email), others can be configured in AuthenticatorFactory
        //---support for adding/regenerating new tokens (support for random secrets)
        //---support for blocking?
        //---'show secrets' button
        credentialTable = new Table("Credentials");
        credentialTable.setSelectable(true);
        credentialTable.setMultiSelect(false);
        credentialTable.addContainerProperty("ServiceId", String.class, null);
        credentialTable.addContainerProperty("Key", String.class, null);
        credentialTable.addContainerProperty("Secret", String.class, null);
        credentialTable.addStyleName("myboldcaption");
        credentialTable.setSizeFull();

        Button addCredential = new Button();
        addCredential.setIcon(new ThemeResource(IconContainer.ADD));
        Button modifyCredential = new Button();
        modifyCredential.setIcon(new ThemeResource(IconContainer.EDIT));
        Button removeCredential = new Button();
        removeCredential.setIcon(new ThemeResource(IconContainer.DELETE));
        Button reloadCredentialTable = new Button();
        reloadCredentialTable.setIcon(new ThemeResource(IconContainer.REFRESH));
        reloadCredentialTable.addClickListener(((event) -> {
            reload();
        }));

        CheckBox showPasswords = new CheckBox("Show Secrets");
        showPasswords.addValueChangeListener((Property.ValueChangeEvent event) -> {
            showSecrets(showPasswords.getValue());
        });

        removeCredential.addClickListener((event) -> {
            Long selection = (Long) credentialTable.getValue();
            if (selection != null) {
                ConfirmationWindow7.showConfirmation("Delete Credential",
                        "If you delete the selected credential, you won't be able to access the associated service any longer.<br/> "
                        + "Do you wish to proceed?", ConfirmationWindow7.OPTION_TYPE.YES_NO_OPTION, ConfirmationWindow7.MESSAGE_TYPE.WARNING, (ConfirmationWindow7.RESULT pResult) -> {
                            switch (pResult) {
                                case YES:
                                    removeCredential(selection);
                                    break;
                                default:
                                //do nothing
                            }
                        });
            }

        });

        modifyCredential.addClickListener((event) -> {
            Long selection = (Long) credentialTable.getValue();
            if (selection != null) {
                updateCredential(selection);
            }
        });

        addCredential.addClickListener((event) -> {
            createCredential();
        });

        VerticalLayout buttonLayout = new VerticalLayout(addCredential, modifyCredential, removeCredential, reloadCredentialTable);

        buttonLayout.setComponentAlignment(modifyCredential, Alignment.TOP_LEFT);
        buttonLayout.setComponentAlignment(removeCredential, Alignment.TOP_LEFT);
        buttonLayout.setComponentAlignment(reloadCredentialTable, Alignment.TOP_LEFT);
        buttonLayout.setMargin(true);

        UIUtils7.GridLayoutBuilder builder = new UIUtils7.GridLayoutBuilder(2, 3);

        builder.fillRow(userInformationForm, 0, 0, 1);
        builder.addComponent(credentialTable, 0, 1).addComponent(buttonLayout, 1, 1);
        builder.fillRow(showPasswords, 0, 2, 1);
        mainLayout = builder.getLayout();

        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setRowExpandRatio(1, 1.0f);
        mainLayout.setColumnExpandRatio(0, .9f);
        mainLayout.setColumnExpandRatio(1, .1f);
        mainLayout.setSizeFull();
        reload();
    }

    private void showSecrets(boolean pValue) {
        showSecrets = pValue;
        reload();
    }

    private void removeCredential(long id) {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());
        try {
            ServiceAccessToken token = mdm.find(ServiceAccessToken.class, id);
            if (token != null) {
                mdm.remove(token);
            }
            reload();
            UIComponentTools.showInformation("Credential successfully deleted.");
        } catch (UnauthorizedAccessAttemptException | EntityNotFoundException ex) {
            UIComponentTools.showWarning("Unable to delete credential.");
        } finally {
            mdm.close();
        }
    }

    private void createCredential() {
        tokenDialog.showDialog(-1);
    }

    private void updateCredential(long id) {
        tokenDialog.showDialog(id);
    }

    /**
     * Reload the form data.
     */
    public void reload() {
        UserData currentUser = UIHelper.getSessionUser();
        if (!UserData.WORLD_USER.equals(currentUser)) {
            screenName.setValue(currentUser.getDistinguishedName());
            firstName.setValue(currentUser.getFirstName());
            lastName.setValue(currentUser.getLastName());
            email.setValue(currentUser.getEmail());
        } else {
            screenName.setValue("");
            firstName.setValue("");
            lastName.setValue("");
            email.setValue("");
        }

        //fill credential table
        credentialTable.removeAllItems();

        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());
        try {
            List<ServiceAccessToken> tokens = mdm.findResultList("SELECT t FROM ServiceAccessToken t WHERE t.userId=?1", new Object[]{currentUser.getDistinguishedName()}, ServiceAccessToken.class);

            tokens.forEach((ServiceAccessToken token) -> {
                Item i = (Item) credentialTable.addItem(token.getId());
                i.getItemProperty("ServiceId").setValue(token.getServiceId());
                i.getItemProperty("Key").setValue(token.getTokenKey());
                if (showSecrets) {
                    try {
                        i.getItemProperty("Secret").setValue(token.getSecret());
                    } catch (SecretDecryptionException ex) {
                        LOGGER.error("Failed to decrypt secret for service " + token.getServiceId() + " and user " + currentUser.getDistinguishedName());
                        i.getItemProperty("Secret").setValue("<FAILED TO DECRYPT SECRET>");
                    }
                } else {
                    i.getItemProperty("Secret").setValue(token.getTokenSecret());
                }
            });
        } catch (UnauthorizedAccessAttemptException ex) {
            //no access?
            UIComponentTools.showError("Failed to obtain credentials from database.");
        } finally {
            mdm.close();
        }
    }

//    /**
//     * Regenerate OAuth credentials and update UI.
//     */
//    private void performOAuthCredentialReset() {
//        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
//        mdm.setAuthorizationContext(UIHelper.getSessionContext());
//        try {
//            UserData currentUser = UIHelper.getSessionUser();
//            ServiceAccessToken token = ServiceAccessUtil.getAccessToken(mdm, new UserId(currentUser.getDistinguishedName()), Constants.REST_API_SERVICE_KEY);
//            if (token == null) {
//                //create new token
//                token = new ServiceAccessToken(currentUser.getDistinguishedName(), Constants.REST_API_SERVICE_KEY);
//            }
//            token.regenerate();
//            mdm.save(token);
//            oauthKey.setValue(token.getTokenKey());
//            oauthSecret.setValue(token.getSecret());
//            UIComponentTools.showInformation("OAuth credentials successfully regenerated.");
//        } catch (UnauthorizedAccessAttemptException | SecretEncryptionException | SecretDecryptionException ex) {
//            LOGGER.error("Failed to regenerate REST credentials.", ex);
//            UIComponentTools.showError("Failed to regenerate OAuth credentials.");
//        } finally {
//            mdm.close();
//        }
//    }
//    /**
//     * Change the user password.
//     */
//    private void doPasswordChange() {
//        if (!UIUtils7.validate(passwordForm)) {
//            UIComponentTools.showError("Failed to change Password. Please correct the errors first.");
//            return;
//        }
//        String sCurrentPassword = currentPassword.getValue();
//        String sNewPassword = newPassword.getValue();
//        String sNewPassword2 = newRetypePassword.getValue();
//        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
//        mdm.setAuthorizationContext(UIHelper.getSessionContext());
//        try {
//            UserData currentUser = UIHelper.getSessionUser();
//            ServiceAccessToken token = ServiceAccessUtil.getAccessToken(mdm, currentUser.getEmail(), Constants.MAIN_LOGIN_SERVICE_ID);
//            if (token != null) {
//                if (!sCurrentPassword.equals(token.getSecret())) {
//                    currentPassword.setComponentError(new UserError("Wrong password."));
//                    return;
//                }
//                if (!sNewPassword.equals(sNewPassword2)) {
//                    UIComponentTools.showError("Passwords not equal.");
//                } else {
//                    token.setSecret(sNewPassword);
//                    mdm.save(token);
//                    currentPassword.setValue("");
//                    newPassword.setValue("");
//                    newRetypePassword.setValue("");
//                    UIComponentTools.showInformation("Your password has been successfully changed.");
//                }
//            }
//        } catch (UnauthorizedAccessAttemptException | SecretEncryptionException | SecretDecryptionException ex) {
//            //no access?
//            LOGGER.error("Failed to change password.", ex);
//            UIComponentTools.showError("An internal error occured while changing password. Please try again later.");
//        } finally {
//            mdm.close();
//        }
//    }
}
