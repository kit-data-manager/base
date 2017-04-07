/*
 * Copyright 2017 Karlsruhe Institute of Technology.
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
package edu.kit.dama.ui.admin.wizard;

import com.vaadin.data.Property;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.Map;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jejkal
 */
public class AdministratorAccountCreation extends WizardStep {

    private TextField adminFirstName;
    private TextField adminLastName;

    private TextField adminEMail;
    private PasswordField adminPassword;
    private PasswordField adminPasswordCheck;

    private CheckBox addOAuthCredentials;

    private final String oAuthKey = "admin";
    private String oAuthSecret = null;

    public AdministratorAccountCreation() {
        super();
    }

    @Override
    public void buildMainLayout() {
        Label information = new Label("In order to be able to perform administrative tasks and for basic configuration, a privileged user must be created. This user is allowed to "
                + "modify settings and internal workflows of the repository system. Therefor, the administrator should have at least valid contact information assigned and must be "
                + "able to log in into the web interface.<br/>Optionally, OAuth creadentials can be added directly to allow RESTful access to the administrator. However, this can "
                + "also be done later inside the web user interface.", ContentMode.HTML);

        adminFirstName = UIUtils7.factoryTextField("First Name", "The first name of the admin user.", 3, 255);
        adminFirstName.setRequired(true);
        adminFirstName.setWidth("400px");
        adminLastName = UIUtils7.factoryTextField("Last Name", "The last name of the admin user.", 3, 255);
        adminLastName.setRequired(true);
        adminLastName.setWidth("400px");
        adminEMail = UIUtils7.factoryTextField("Contact EMail", "The admin contact email.", 6, 255);
        adminEMail.setRequired(true);
        adminEMail.setWidth("400px");
        adminPassword = new PasswordField("Password");
        adminPassword.setWidth("400px");
        adminPassword.setRequired(true);
        adminPasswordCheck = new PasswordField("Confirm Password");
        adminPasswordCheck.setWidth("400px");
        adminPasswordCheck.setRequired(true);
        addOAuthCredentials = new CheckBox("Create OAuth Credentials for Administrator (optional)");
        addOAuthCredentials.setDescription("Create (random) OAuth credentials for the admin user. These credentials can be used to access the RESTful endpoints of the repository. "
                + "You can also create/modify OAuth credentials later inside the user interface.");
        addOAuthCredentials.setWidth("400px");
        getMainLayout().addComponent(information);
        getMainLayout().addComponent(adminFirstName);
        getMainLayout().addComponent(adminLastName);
        getMainLayout().addComponent(adminEMail);
        getMainLayout().addComponent(adminPassword);
        getMainLayout().addComponent(adminPasswordCheck);
        getMainLayout().addComponent(addOAuthCredentials);
        getMainLayout().setComponentAlignment(information, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(adminFirstName, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(adminLastName, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(adminEMail, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(adminPassword, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(adminPasswordCheck, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(addOAuthCredentials, Alignment.TOP_LEFT);
        addOAuthCredentials.addValueChangeListener((Property.ValueChangeEvent event) -> {
            if (addOAuthCredentials.getValue()) {
                oAuthSecret = RandomStringUtils.randomAlphabetic(16);
            } else {
                oAuthSecret = null;
            }
        });
    }

    @Override
    public String getStepName() {
        return "Administrator Login";
    }

    @Override
    public boolean validateSettings() {
        if (!UIUtils7.validate(getMainLayout())) {
            return false;
        }

        if (adminPassword.getValue().equals(adminPasswordCheck.getValue())) {
            if (adminEMail.getValue().contains("@")) {
                adminPasswordCheck.setComponentError(null);
                adminEMail.setComponentError(null);
                return true;
            } else {
                adminEMail.setComponentError(new UserError("A valid email is needed."));
            }
        } else {
            adminPasswordCheck.setComponentError(new UserError("Passwords are different."));
        }
        return false;
    }

    @Override
    public String getSummary() {
        StringBuilder result = new StringBuilder();
        result.append(getStepName()).append("\n");
        result.append(StringUtils.rightPad("", 50, "_")).append("\n");
        result.append("Administrator First Name: ").append(adminFirstName.getValue()).append("\n");
        result.append("Administrator Last Name: ").append(adminLastName.getValue()).append("\n");
        result.append("Administrator EMail: ").append(adminEMail.getValue()).append("\n");
        result.append("Web Login Password: ").append(adminPassword.getValue()).append("\n");
        result.append("Create OAuth access: ").append((addOAuthCredentials.getValue()) ? "yes\n" : "no\n");
        if (addOAuthCredentials.getValue()) {
            result.append("  OAuth Key: ").append(oAuthKey).append("\n");
            result.append("  OAuth Secret: ").append(oAuthSecret).append("\n");
        }
        return result.toString();
    }

    @Override
    public void collectProperties(Map<String, String> properties) {
        properties.put(WizardPersistHelper.ADMIN_FIRST_NAME, adminFirstName.getValue());
        properties.put(WizardPersistHelper.ADMIN_LAST_NAME, adminLastName.getValue());
        properties.put(WizardPersistHelper.ADMIN_EMAIL, adminEMail.getValue());
        properties.put(WizardPersistHelper.ADMIN_PASSWORD, adminPassword.getValue());
        if (addOAuthCredentials.getValue()) {
            properties.put(WizardPersistHelper.ADMIN_OAUTH_KEY, oAuthKey);
            properties.put(WizardPersistHelper.ADMIN_OAUTH_SECRET, oAuthSecret);
        } else {
            properties.remove(WizardPersistHelper.ADMIN_OAUTH_KEY);
            properties.remove(WizardPersistHelper.ADMIN_OAUTH_SECRET);
        }
    }

}
