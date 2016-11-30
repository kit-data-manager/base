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
package edu.kit.dama.ui.admin;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretDecryptionException;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.util.auth.AbstractAuthenticator;
import edu.kit.dama.rest.util.auth.AuthenticatorFactory;
import edu.kit.dama.ui.admin.login.MainLoginAuthenticator;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.admin.utils.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author jejkal
 */
public class ServiceAccessTokenDialog {

    private ComboBox authenticatorSelection;

    private VerticalLayout mainLayout;
    private GridLayout authenticatorConfigurationLayout;
    final List<AbstractAuthenticator> authenticators = new ArrayList<>();
    private TextField tokenField = new TextField();
    private PasswordField secretField = new PasswordField();
    private TextField nosecretField = new TextField();
    private final CheckBox showSecret = new CheckBox("Show Secret");
    private final Button generateButton = new Button("Generate");
    private final Button okButton = new Button("OK");
    private final Button cancelButton = new Button("Cancel");
    private Window currentWin = null;
    private long selectedId = -1;

    public ServiceAccessTokenDialog() {
        buildMainLayout();
    }

    private void buildMainLayout() {
        authenticatorSelection = new ComboBox("Credential Type");
        authenticatorSelection.setWidth("100%");
        authenticatorSelection.setNullSelectionAllowed(false);
        authenticatorSelection.addStyleName("myboldcaption");

        //load all authenticators
        authenticators.add(new MainLoginAuthenticator());
        authenticatorSelection.addItem(authenticators.get(0).getAuthenticatorId());

        //fill authenticator list and selection box
        AuthenticatorFactory.getInstance().getAuthenticators().forEach((auth) -> {
            authenticators.add(auth);
            authenticatorSelection.addItem(auth.getAuthenticatorId());
        });

        //selection handler
        authenticatorSelection.addValueChangeListener((event) -> {
            String value = (String) authenticatorSelection.getValue();
            authenticators.forEach((auth) -> {
                if (auth.getAuthenticatorId().equals(value)) {
                    updateAuthenticatorAttributeLayout(auth);
                }
            });
        });

        //generate secret handling
        generateButton.addClickListener((event) -> {
            String newSecret = RandomStringUtils.randomAlphanumeric(16);
            if (secretField != null) {
                //check this just to get sure
                secretField.setValue(newSecret);
                nosecretField.setValue(newSecret);
                UIComponentTools.showInformation("New random secret has been generated.");
            }
        });

        //show secret handling
        showSecret.addValueChangeListener((event2) -> {
            if (secretField != null) {
                //check this just to get sure
                if (showSecret.getValue()) {
                    nosecretField.setValue(secretField.getValue());
                    authenticatorConfigurationLayout.replaceComponent(secretField, nosecretField);
                } else {
                    secretField.setValue(nosecretField.getValue());
                    authenticatorConfigurationLayout.replaceComponent(nosecretField, secretField);
                }
            }
        });

        ClickListener listener = (Button.ClickEvent event) -> {
            boolean update = false;
            boolean created = false;

            if (okButton.equals(event.getSource()) && selectedId > 0) {
                //update of existing credential
                update = true;
                IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
                mdm.setAuthorizationContext(UIHelper.getSessionContext());
                try {
                    ServiceAccessToken existingToken = mdm.find(ServiceAccessToken.class, selectedId);
                    //transfer information from existing token to new one and persist
                    ServiceAccessToken newToken = getToken(existingToken.getUserId());
                    newToken.setId(selectedId);
                    newToken.setServiceId(existingToken.getServiceId());
                    mdm.save(newToken);
                } catch (UnauthorizedAccessAttemptException | SecretEncryptionException ex) {
                    UIComponentTools.showWarning("Unable to store credential.");
                    return;
                } finally {
                    mdm.close();
                }
            } else if (okButton.equals(event.getSource()) && selectedId <= 0) {
                //creation of new token
                String typeSelection = (String) authenticatorSelection.getValue();
                IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
                mdm.setAuthorizationContext(UIHelper.getSessionContext());
                String userId = UIHelper.getSessionUser().getDistinguishedName();
                try {
                    try {
                        List<ServiceAccessToken> token = mdm.findResultList("SELECT t FROM ServiceAccessToken t WHERE t.serviceId=?1 AND t.userId=?2", new Object[]{typeSelection, userId}, ServiceAccessToken.class);
                        if (!token.isEmpty()) {
                            UIComponentTools.showWarning("There exists already a credential of type '" + typeSelection + "' for your userId.");
                            return;
                        }
                    } catch (UnauthorizedAccessAttemptException ex) {
                        UIComponentTools.showWarning("Unable to check for existing credential.");
                        return;
                    }

                    ServiceAccessToken newToken = getToken(userId);

                    String uid = newToken.getUserId();
                    newToken.setUserId(null);
                    if (!mdm.find(newToken, newToken).isEmpty()) {
                        throw new UnauthorizedAccessAttemptException("Duplicate credential detected.");
                    }

                    newToken.setUserId(uid);
                    mdm.save(newToken);
                    created = true;
                } catch (UnauthorizedAccessAttemptException | SecretEncryptionException ex) {
                    UIComponentTools.showWarning("Failed to create new credential. (Message: " + ex.getMessage() + ")");
                    return;
                } finally {
                    mdm.close();
                }

            }
            //close window
            if (currentWin != null) {
                UI.getCurrent().removeWindow(currentWin);
                if (update) {
                    UIComponentTools.showInformation("Credential successfully updated.");
                } else if (created) {
                    UIComponentTools.showInformation("Credential successfully created.");
                }
            }
        };
        okButton.addClickListener(listener);
        cancelButton.addClickListener(listener);

        //fill dummy config layout
        authenticatorConfigurationLayout = new GridLayout(1, 1);
        authenticatorConfigurationLayout.addComponent(new Label("Please select an authenticator."));
        authenticatorConfigurationLayout.setSpacing(true);
        authenticatorConfigurationLayout.setWidth("400px");

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, okButton);
        buttonLayout.setSpacing(true);

        mainLayout = new VerticalLayout(authenticatorSelection, authenticatorConfigurationLayout, buttonLayout);
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setExpandRatio(authenticatorConfigurationLayout, 1.0f);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
    }

    private ServiceAccessToken getToken(String userId) throws SecretEncryptionException {

        String value = (String) authenticatorSelection.getValue();

        AbstractAuthenticator selection = (AbstractAuthenticator) CollectionUtils.find(authenticators, (Object o) -> ((AbstractAuthenticator) o).getAuthenticatorId().equals(value));

        Map<String, String> credentialMap = new HashMap<>();

        if (tokenField == null) {
            //no credential information
        } else {
            //token field visible, use caption as key and value as secret
            credentialMap.put(tokenField.getCaption(), tokenField.getValue());
            if (secretField != null) {
                //secret is also visible, use caption as key and value as secret 
                credentialMap.put(secretField.getCaption(), secretField.getValue());
            }
        }

        //let the AbstractAuthenticator create the access token as only its implementation 'knows' how the information is stored
        return selection.generateServiceAccessToken(new UserId(userId), credentialMap);
    }

    private void updateAuthenticatorAttributeLayout(AbstractAuthenticator authenticator) {
        UIUtils7.GridLayoutBuilder builder;
        String[] attributes = authenticator.getCredentialAttributeNames();

        switch (attributes.length) {
            case 0: {
                tokenField = null;
                secretField = null;
                nosecretField = null;
                builder = new UIUtils7.GridLayoutBuilder(1, 1);
                break;
            }
            case 1: {
                tokenField = new TextField(attributes[0]);
                tokenField.addStyleName("myboldcaption");
                secretField = null;
                nosecretField = null;
                builder = new UIUtils7.GridLayoutBuilder(1, 1);
                break;
            }
            default: {
                tokenField = new TextField(attributes[0]);
                tokenField.addStyleName("myboldcaption");
                secretField = new PasswordField(attributes[1]);
                secretField.addStyleName("myboldcaption");
                nosecretField = new TextField(attributes[1]);
                nosecretField.addStyleName("myboldcaption");
                builder = new UIUtils7.GridLayoutBuilder(2, 3);
            }
        }

        if (tokenField != null) {
            builder.fillRow(tokenField, 0, 0, 1);
        } else {
            builder.fillRow(new Label("No configuration needed."), 0, 0, 1);
        }

        if (secretField != null) {
            builder.addComponent(secretField, 0, 1);
            builder.addComponent(generateButton, Alignment.BOTTOM_RIGHT, 1, 1, 1, 1);
            builder.fillRow(showSecret, 0, 2, 1);
        }
        GridLayout newLayout = builder.getLayout();
        newLayout.setSpacing(true);
        newLayout.setWidth("400px");

        mainLayout.replaceComponent(authenticatorConfigurationLayout, newLayout);
        authenticatorConfigurationLayout = newLayout;
    }

    public void showDialog(long selection) {
        selectedId = selection;
        //load data in case of update
        boolean updateMode = false;
        if (selectedId > 0) {
            updateMode = true;
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            mdm.setAuthorizationContext(UIHelper.getSessionContext());

            try {
                ServiceAccessToken token = mdm.find(ServiceAccessToken.class, selectedId);
                //check if there is an authenticator for the token's service id
                boolean validAuthenticator = false;
                for (AbstractAuthenticator auth : authenticators) {
                    if (auth.getAuthenticatorId().equals(token.getServiceId())) {
                        validAuthenticator = true;
                        break;
                    }
                }
                if (!validAuthenticator) {
                    UIComponentTools.showWarning("No authenticator for credential with serviceId '" + token.getServiceId() + "' configured. Please contact a system adminstrator.");
                    return;
                } else {
                    authenticatorSelection.select(token.getServiceId());
                    if (tokenField != null) {
                        tokenField.setValue(token.getTokenKey());
                    }
                    if (secretField != null) {
                        secretField.setValue(token.getSecret());
                        nosecretField.setValue(secretField.getValue());
                    }
                }
            } catch (UnauthorizedAccessAttemptException ex) {
                UIComponentTools.showWarning("You are not authorized to retrieve the selected credential.");
            } catch (SecretDecryptionException ex) {
                //The secret cannot be decrypted. This can be the case if it is not encrypted, e.g. for the HTTPAuthenticator, but could also be the case of the global secret 
                //in the datamanager.xml was changed. However, we cannot see the secret but we allow to set a new one, so we warn but continue.
                UIComponentTools.showWarning("Unable to decrypt the secret of the selected credential.<br/>You can either assign a new secret or cancel the update.");
            } finally {
                mdm.close();
            }
        }

        authenticatorSelection.setEnabled(!updateMode);
        okButton.setCaption((updateMode) ? "Update" : "Create");
        //create and show new window
        currentWin = new Window((updateMode) ? "Update Credential" : "Create Credential");
        currentWin.setContent(mainLayout);
        currentWin.center();
        UI.getCurrent().addWindow(currentWin);
    }
}
