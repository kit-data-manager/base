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

import com.vaadin.server.UserError;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jejkal
 */
public class AccessPointCreation extends WizardStep {

    private CheckBox addWebDavAccessPoint;
    private TextField accessPointName;
    private TextField baseUrl;
    private TextField basePath;
    private CheckBox createAdminLogin;
    private PasswordField adminPassword;
    private PasswordField adminPasswordCheck;

    public AccessPointCreation() {
        super();
    }

    @Override
    public void buildMainLayout() {
        Label information = new Label("In this step you can choose to create a WebDav Access Point. This access point can be used to upload/download data to/from the repository easily. "
                + "The advantage compared to externally configured access points is the tight integration into the repository system's authentication framework and operating system permissions.<br/>"
                + "In order to allow access for the system administrator it is recommended to create a WebDav login for the admin user. The login will be the email and the password selected below. "
                + "You may use the same password as before, but for security reasons it is recommended to use another password for WebDav access.", ContentMode.HTML);

        addWebDavAccessPoint = new CheckBox("Select to Create a WebDav Access Point");
        addWebDavAccessPoint.setValue(Boolean.TRUE);
        createAdminLogin = new CheckBox("Select to Create an Admin Login for the Access Point");
        createAdminLogin.setValue(Boolean.TRUE);
        addWebDavAccessPoint.addValueChangeListener((event) -> {
            createAdminLogin.setEnabled(addWebDavAccessPoint.getValue());
            accessPointName.setEnabled(addWebDavAccessPoint.getValue());
            baseUrl.setEnabled(addWebDavAccessPoint.getValue());
            basePath.setEnabled(addWebDavAccessPoint.getValue());
            adminPassword.setEnabled(addWebDavAccessPoint.getValue() && createAdminLogin.getValue());
            adminPasswordCheck.setEnabled(addWebDavAccessPoint.getValue() && createAdminLogin.getValue());
        });

        createAdminLogin.addValueChangeListener((event) -> {
            adminPassword.setEnabled(createAdminLogin.getValue());
            adminPasswordCheck.setEnabled(createAdminLogin.getValue());
        });

        accessPointName = UIUtils7.factoryTextField("Access Point Name", "e.g WebDav Access Point", 3, 255);
        accessPointName.setValue("WebDav Access Point");
        accessPointName.setRequired(true);
        accessPointName.setWidth("400px");

        baseUrl = UIUtils7.factoryTextField("Base Url", "e.g http://localhost:8080/webdav", 3, 255);
        baseUrl.setRequired(true);
        baseUrl.setWidth("400px");
        basePath = UIUtils7.factoryTextField("Base Path", "e.g /mnt/data/webdav/", 3, 255);
        basePath.setRequired(true);
        basePath.setWidth("400px");

        adminPassword = new PasswordField("WebDav Password");
        adminPassword.setWidth("400px");
        adminPassword.setRequired(true);
        adminPasswordCheck = new PasswordField("Confirm WebDav Password");
        adminPasswordCheck.setWidth("400px");
        adminPasswordCheck.setRequired(true);

        getMainLayout().addComponent(information);
        getMainLayout().addComponent(addWebDavAccessPoint);
        getMainLayout().addComponent(accessPointName);
        getMainLayout().addComponent(baseUrl);
        getMainLayout().addComponent(basePath);
        getMainLayout().addComponent(createAdminLogin);
        getMainLayout().addComponent(adminPassword);
        getMainLayout().addComponent(adminPasswordCheck);

        getMainLayout().setComponentAlignment(information, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(addWebDavAccessPoint, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(accessPointName, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(baseUrl, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(basePath, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(createAdminLogin, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(adminPassword, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(adminPasswordCheck, Alignment.TOP_LEFT);
    }

    @Override
    public String getStepName() {
        return "Data Access Point";
    }

    @Override
    public boolean validateSettings() {
        if (addWebDavAccessPoint.getValue()) {
            if (!UIUtils7.validate(getMainLayout())) {
                return false;
            }

            if (createAdminLogin.getValue()) {
                if (adminPassword.getValue().equals(adminPasswordCheck.getValue())) {
                    adminPasswordCheck.setComponentError(null);
                } else {
                    adminPasswordCheck.setComponentError(new UserError("Passwords are different."));
                    return false;
                }
            }

            try {
                new URL(baseUrl.getValue());
                baseUrl.setComponentError(null);
            } catch (MalformedURLException ex) {
                baseUrl.setComponentError(new UserError("Not a valid URL."));
                return false;
            }

            File f = new File(basePath.getValue());
            if (!f.exists()) {
                basePath.setComponentError(new UserError("Base path does not exist."));
                return false;
            }

            if (!f.isDirectory()) {
                basePath.setComponentError(new UserError("Base path is not a directory."));
                return false;
            }

            if (!f.canRead() || !f.canWrite()) {
                basePath.setComponentError(new UserError("Base path is not readable and/or writeable."));
                return false;
            }

            File usersDir = new File(f, "USERS");
            if (!usersDir.exists()) {
                basePath.setComponentError(new UserError("Base path seems not be be the local web dav folder. No 'USERS' sub directory found."));
                return false;
            }
        }

        return true;
    }

    @Override
    public String getSummary() {
        StringBuilder result = new StringBuilder();
        result.append(getStepName()).append("\n");
        result.append(StringUtils.rightPad("", 50, "_")).append("\n");

        if (addWebDavAccessPoint.getValue()) {
            result.append("Access Point Name: ").append(accessPointName.getValue()).append("\n");
            result.append("Base Url: ").append(baseUrl.getValue()).append("\n");
            result.append("Base Path: ").append(basePath.getValue()).append("\n");
            result.append("Create Administrator Webdav Login: ").append((createAdminLogin.getValue()) ? "yes\n" : "no\n");
            if (createAdminLogin.getValue()) {
                result.append("  Webdav Login Name: ").append("<Administrator EMail>\n");
                result.append("  Webdav Login Password: ").append(adminPassword.getValue()).append("\n");
            }
        } else {
            result.append("No WebDav Access Point will be created.\n");
        }

        return result.toString();
    }

    @Override
    public void collectProperties(Map<String, String> properties) {
        if (addWebDavAccessPoint.getValue()) {
            properties.put(WizardPersistHelper.CREATE_ACCESS_POINT, "yes");
            properties.put(WizardPersistHelper.ACCESS_POINT_NAME, accessPointName.getValue());
            properties.put(WizardPersistHelper.ACCESS_POINT_BASE_URL, baseUrl.getValue());
            properties.put(WizardPersistHelper.ACCESS_POINT_BASE_PATH, basePath.getValue());
            if (createAdminLogin.getValue()) {
                properties.put(WizardPersistHelper.ACCESS_POINT_ADMIN_PASSWORD, adminPassword.getValue());
            } else {
                properties.remove(WizardPersistHelper.ACCESS_POINT_ADMIN_PASSWORD);
            }
        } else {
            properties.remove(WizardPersistHelper.CREATE_ACCESS_POINT);
        }
    }

}
