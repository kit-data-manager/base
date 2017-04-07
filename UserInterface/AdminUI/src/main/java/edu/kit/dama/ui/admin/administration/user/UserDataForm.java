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
package edu.kit.dama.ui.admin.administration.user;

import com.vaadin.data.Property;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.SystemError;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.ui.admin.administration.user.UserDataTablePanel.UserDataEffectivity;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.commons.util.UIHelper;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public final class UserDataForm extends GridLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataForm.class);
    public static final String DEBUG_ID_PREFIX = UserDataForm.class.getSimpleName() + "_";

    private final UserDataAdministrationTab userDataAdministrationTab;

    private TextField userIdField;
    private TextField distinguishedNameField;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField emailField;
    private DateField validFromField;
    private DateField validUntilField;
    private ComboBox maximumRoleBox;
    private Button commitChangesButton;
    private Button showMembershipsButton;
    private Embedded registerUserEmbeddedIcon;

    public UserDataForm(UserDataAdministrationTab userDataAdministrationTab) {
        this.userDataAdministrationTab = userDataAdministrationTab;

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setColumns(5);
        setRows(4);
        setSizeFull();
        setSpacing(true);
        // Add components to
        addComponent(getUserIdField(), 0, 0);
        addComponent(getFirstNameField(), 0, 1);
        addComponent(getValidFromField(), 0, 2);
        addComponent(getEmailField(), 0, 3);
        addComponent(getDistinguishedNameField(), 2, 0);
        addComponent(getLastNameField(), 2, 1);
        addComponent(getValidUntilField(), 2, 2);
        addComponent(getMaximumRoleBox(), 2, 3);
        addComponent(getCommitChangesButton(), 4, 0);
        addComponent(getShowMembershipsButton(), 4, 1);
        addComponent(getRegisterUserButton(), 4, 2, 4, 3);
        setColumnExpandRatio(0, 0.39f);
        setColumnExpandRatio(1, 0.01f);
        setColumnExpandRatio(2, 0.39f);
        setColumnExpandRatio(3, 0.01f);
        setColumnExpandRatio(4, 0.2f);
        setComponentAlignment(getCommitChangesButton(), Alignment.MIDDLE_RIGHT);
        setComponentAlignment(getShowMembershipsButton(), Alignment.MIDDLE_RIGHT);
        setComponentAlignment(getRegisterUserButton(), Alignment.MIDDLE_CENTER);
    }

    private TextField getUserIdField() {
        if (userIdField == null) {
            String id = "userIdField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            userIdField = new TextField("ID");
            userIdField.setId(DEBUG_ID_PREFIX + id);
            userIdField.setWidth("100%");
            userIdField.setImmediate(true);
            userIdField.setReadOnly(true);
            userIdField.setNullRepresentation("");
            userIdField.setDescription("Automatically created ID used as primary "
                    + "key within the underlaying database");
            userIdField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return userIdField;
    }

    private TextField getDistinguishedNameField() {
        if (distinguishedNameField == null) {
            String id = "distinguishedNameField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            distinguishedNameField = new TextField("DISTINGUISHED NAME");
            distinguishedNameField.setId(DEBUG_ID_PREFIX + id);
            distinguishedNameField.setWidth("100%");
            distinguishedNameField.setImmediate(true);
            distinguishedNameField.setReadOnly(true);
            distinguishedNameField.setNullRepresentation("");
            distinguishedNameField.setDescription("Identifier of selected user, "
                    + "defined while registration");
            distinguishedNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return distinguishedNameField;
    }

    private TextField getFirstNameField() {
        if (firstNameField == null) {
            String id = "firstNameField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            firstNameField = new TextField("FIRST NAME");
            firstNameField.setId(DEBUG_ID_PREFIX + id);
            firstNameField.setWidth("100%");
            firstNameField.setImmediate(true);
            firstNameField.setRequired(true);
            firstNameField.setReadOnly(true);
            firstNameField.setNullRepresentation("");
            firstNameField.setDescription("First name of selected user");
            firstNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return firstNameField;
    }

    private TextField getLastNameField() {
        if (lastNameField == null) {
            String id = "lastNameField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            lastNameField = new TextField("LAST NAME");
            lastNameField.setId(DEBUG_ID_PREFIX + id);
            lastNameField.setWidth("100%");
            lastNameField.setImmediate(true);
            lastNameField.setRequired(true);
            lastNameField.setReadOnly(true);
            lastNameField.setNullRepresentation("");
            lastNameField.setDescription("Last name of selected user");
            lastNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return lastNameField;
    }

    private TextField getEmailField() {
        if (emailField == null) {
            String id = "emailField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            emailField = new TextField("EMAIL");
            emailField.setId(DEBUG_ID_PREFIX + id);
            emailField.setWidth("100%");
            emailField.setImmediate(true);
            emailField.setRequired(true);
            emailField.setNullRepresentation("");
            emailField.setReadOnly(true);
            emailField.setDescription("Email address of selected user");
            emailField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return emailField;
    }

    private DateField getValidFromField() {
        if (validFromField == null) {
            String id = "validFromField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            validFromField = new DateField("VALID FROM");
            validFromField.setId(DEBUG_ID_PREFIX + id);
            validFromField.setWidth("100%");
            validFromField.setImmediate(true);
            validFromField.setReadOnly(true);
            validFromField.setDateFormat("dd.MM.yyyy");
            validFromField.setDescription("Date from which the account of the selected user is available");
            validFromField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            validFromField.addValueChangeListener((Property.ValueChangeEvent event) -> {
                getValidUntilField().setRangeStart((Date) event.getProperty().getValue());
            });
        }
        return validFromField;
    }

    private DateField getValidUntilField() {
        if (validUntilField == null) {
            String id = "validUntilField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            validUntilField = new DateField("VALID UNTIL");
            validUntilField.setId(DEBUG_ID_PREFIX + id);
            validUntilField.setWidth("100%");
            validFromField.setImmediate(true);
            validUntilField.setReadOnly(true);
            validUntilField.setDateFormat("dd.MM.yyyy");
            validUntilField.setDescription("Date until which the user's KIT Data Manager account is valid");
            validUntilField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            validUntilField.addValueChangeListener((Property.ValueChangeEvent event) -> {
                getValidFromField().setRangeEnd((Date) event.getProperty().getValue());
            });
        }
        return validUntilField;
    }

    private ComboBox getMaximumRoleBox() {
        if (maximumRoleBox == null) {
            String id = "maximumRoleBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            maximumRoleBox = new ComboBox("MAXIMUM ROLE");
            maximumRoleBox.setId(DEBUG_ID_PREFIX + id);
            maximumRoleBox.setWidth("100%");
            maximumRoleBox.setRequired(true);
            maximumRoleBox.setNullSelectionAllowed(false);
            maximumRoleBox.setReadOnly(true);
            maximumRoleBox.setDescription("Maximum role of the selected user.");

            for (Role role : Role.getValidRoles()) {
                maximumRoleBox.addItem(role);
            }

            maximumRoleBox.select(null);
            maximumRoleBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return maximumRoleBox;
    }

    private Button getCommitChangesButton() {
        if (commitChangesButton == null) {
            String id = "commitChangesButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            commitChangesButton = new Button("Commit Changes");
            commitChangesButton.setId(DEBUG_ID_PREFIX + id);
            commitChangesButton.setSizeFull();
            commitChangesButton.setImmediate(true);
            commitChangesButton.setDescription("Click for committing the changes "
                    + "applied on the selected user data.");

            commitChangesButton.addClickListener((Button.ClickEvent event) -> {
                UserDataTablePanel userDataTablePanel = userDataAdministrationTab.getUserDataTablePanel();
                UserData selectedUser = userDataTablePanel.getSelectedUserData();
                selectedUser.setFirstName(getFirstNameField().getValue());
                selectedUser.setLastName(getLastNameField().getValue());
                selectedUser.setValidFrom(getValidFromField().getValue());
                selectedUser.setValidUntil(getValidUntilField().getValue());
                Role newRole = (Role) getMaximumRoleBox().getValue();
                UserId userId = new UserId((String) getDistinguishedNameField().getValue());
                IAuthorizationContext authCtx = UIHelper.getSessionContext();
                IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
                mdm.setAuthorizationContext(authCtx);

                try {
                    UserServiceLocal.getSingleton().setRoleRestriction(userId, newRole, authCtx);
                    mdm.save(selectedUser);
                    userDataTablePanel.updateTableEntry(selectedUser);
                } catch (UnauthorizedAccessAttemptException ex) {
                    UIComponentTools.showWarning("You are not authorized to change the selected user.");
                } catch (EntityNotFoundException ex) {
                    LOGGER.error("Failed to update maximum role of user " + userId, ex);
                    UIComponentTools.showWarning("Internal error while updating maximum user role.");
                } finally {
                    mdm.close();
                }
            });
        }
        return commitChangesButton;
    }

    private Button getShowMembershipsButton() {
        if (showMembershipsButton == null) {
            String id = "showMembershipsButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            showMembershipsButton = new Button("Show Memberships");
            showMembershipsButton.setId(DEBUG_ID_PREFIX + id);
            showMembershipsButton.setSizeFull();
            showMembershipsButton.setDescription("Click to view all the memberships of the selected user.");

            showMembershipsButton.addClickListener((Button.ClickEvent event) -> {
                if (!UIHelper.getSessionContext().getRoleRestriction().atLeast(Role.MANAGER)) {
                    UIComponentTools.showWarning(NoteBuilder.unauthorizedContext());
                    return;
                }
                UI.getCurrent().addWindow(new MembershipRoleEditorWindow(userDataAdministrationTab));
            });
        }
        return showMembershipsButton;
    }

    private Embedded getRegisterUserButton() {
        if (registerUserEmbeddedIcon == null) {
            String id = "registerUserButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            registerUserEmbeddedIcon = new Embedded("", new ThemeResource(IconContainer.USER_NEW));
            registerUserEmbeddedIcon.setId(DEBUG_ID_PREFIX + id);
            registerUserEmbeddedIcon.addStyleName("myclickablecomponent");

            registerUserEmbeddedIcon.addClickListener((MouseEvents.ClickEvent event) -> {
                UIComponentTools.showWarning("User registration only possible via login page.");
            });
        }
        return registerUserEmbeddedIcon;
    }

    private void lockUserDataComponents(boolean locked) {
        getUserIdField().setReadOnly(locked);
        getDistinguishedNameField().setReadOnly(locked);
        getFirstNameField().setReadOnly(locked);
        getLastNameField().setReadOnly(locked);
        getEmailField().setReadOnly(locked);
        getValidFromField().setReadOnly(locked);
        getValidUntilField().setReadOnly(locked);
        getMaximumRoleBox().setEnabled(!locked);
        getMaximumRoleBox().setReadOnly(false);
    }

    private void clearUserDataComponents() {
        lockUserDataComponents(false);
        getUserIdField().setValue(null);
        getDistinguishedNameField().setValue(null);
        getFirstNameField().setValue(null);
        getLastNameField().setValue(null);
        getEmailField().setValue(null);
        getValidFromField().setValue(null);
        getValidUntilField().setValue(null);
        getMaximumRoleBox().select(null);
        getMaximumRoleBox().setComponentError(null);
    }

    private void updateComponentValues(UserData userData) {
        clearUserDataComponents();
        if (userData == null) {
            //do nothing
            return;
        }
        getUserIdField().setValue(Long.toString(userData.getUserId()));
        getDistinguishedNameField().setValue(userData.getDistinguishedName());
        getFirstNameField().setValue(userData.getFirstName());
        getLastNameField().setValue(userData.getLastName());
        getEmailField().setValue(userData.getEmail());
        getValidFromField().setValue(userData.getValidFrom());
        getValidFromField().setValue(userData.getValidFrom());
        getValidUntilField().setValue(userData.getValidUntil());
        try {
            Role maxRole = (Role) UserServiceLocal.getSingleton().getRoleRestriction(new UserId(userData.getDistinguishedName()), UIHelper.getSessionContext());
            getMaximumRoleBox().setValue(maxRole);
        } catch (AuthorizationException ex) {
            getMaximumRoleBox().setComponentError(new SystemError("Failed to determine maximum role for selected user."));
            LOGGER.error("Failed to obtain max. role for user '" + userData.getDistinguishedName(), ex);
        }
    }

    protected void update(Role role) {
        //set values according to the current user
        updateComponentValues(userDataAdministrationTab.getUserDataTablePanel().getSelectedUserData());
        //determine type of user
        UserDataEffectivity userDataValidation = userDataAdministrationTab.getUserDataTablePanel().validateSelectedUserData();
        switch (role) {
            case ADMINISTRATOR:
            case MANAGER:
                if (role.equals(Role.ADMINISTRATOR)) {
                    //in case of admin use actual user effectivity
                    switchToAdministratorView(userDataValidation);
                } else {
                    //in case of manager behave as the user is the logged in user aka. allow only read access
                    switchToAdministratorView(userDataValidation);
                    UIComponentTools.setLockedLayoutComponents(this, true);
                }
                break;
            default:
                //others should not appear here but if they do they also only read
                UIComponentTools.setLockedLayoutComponents(this, true);
                break;
        }
    }

    /**
     * Update the view to allow ADMINISTRATOR access. Administrators are allowed
     * to see and modify user information except their own information.
     */
    private void switchToAdministratorView(UserDataEffectivity userDataValidation) {
        switch (userDataValidation) {
            case DISABLED_USER:
            case VALID:
                //if a valid user is selected disable read-only fields and enable updatable fields
                // Lock components
                getUserIdField().setReadOnly(true);
                getDistinguishedNameField().setReadOnly(true);
                getEmailField().setReadOnly(true);
                // Unlock components
                getFirstNameField().setReadOnly(false);
                getLastNameField().setReadOnly(false);
                getValidFromField().setReadOnly(false);
                getValidUntilField().setReadOnly(false);
                getMaximumRoleBox().setEnabled(true);
                getMaximumRoleBox().setReadOnly(false);
                getCommitChangesButton().setEnabled(true);
                getShowMembershipsButton().setEnabled(true);
                getRegisterUserButton().setEnabled(true);
                break;
            case LOGGED_IN_USER:
                //if the currently logged in user is selected, disable changing the max. role
                // Lock components
                getUserIdField().setReadOnly(true);
                getDistinguishedNameField().setReadOnly(true);
                getEmailField().setReadOnly(true);
                getMaximumRoleBox().setEnabled(false);
                getMaximumRoleBox().setReadOnly(true);
                // Unlock components
                getFirstNameField().setReadOnly(false);
                getLastNameField().setReadOnly(false);
                getValidFromField().setReadOnly(false);
                getValidUntilField().setReadOnly(false);
                getCommitChangesButton().setEnabled(true);
                getShowMembershipsButton().setEnabled(true);
                getRegisterUserButton().setEnabled(true);
                break;
            default:
                //if no user or an invalid user is selected disable all
                UIComponentTools.setLockedLayoutComponents(this, true);
                break;
        }
    }

}
