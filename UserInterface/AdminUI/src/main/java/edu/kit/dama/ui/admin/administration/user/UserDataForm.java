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
import edu.kit.dama.ui.admin.administration.user.UserDataTablePanel.UserDataEffictivity;
import edu.kit.dama.ui.admin.container.UserDataContainer;
import edu.kit.dama.ui.admin.exception.DBCommitException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UnsupportedEnumException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
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
        setImmediate(true);
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

    /**
     * @return the userIdField
     */
    public final TextField getUserIdField() {
        if (userIdField == null) {
            buildUserIdField();
        }
        return userIdField;
    }

    /**
     *
     */
    private void buildUserIdField() {
        String id = "userIdField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        userIdField = new TextField(UserDataContainer.Property.USER_ID.columnHeader);
        userIdField.setId(DEBUG_ID_PREFIX + id);
        userIdField.setWidth("100%");
        userIdField.setImmediate(true);
        userIdField.setReadOnly(true);
        userIdField.setNullRepresentation("");
        userIdField.setDescription("Automatically created ID used as primary "
                + "key within the underlaying database");
        userIdField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the distinguishedNameField
     */
    public final TextField getDistinguishedNameField() {
        if (distinguishedNameField == null) {
            buildDistinguishedNameField();
        }
        return distinguishedNameField;
    }

    /**
     *
     */
    private void buildDistinguishedNameField() {
        String id = "distinguishedNameField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        distinguishedNameField = new TextField(UserDataContainer.Property.DISTINGUISHED_NAME.columnHeader);
        distinguishedNameField.setId(DEBUG_ID_PREFIX + id);
        distinguishedNameField.setWidth("100%");
        distinguishedNameField.setImmediate(true);
        distinguishedNameField.setReadOnly(true);
        distinguishedNameField.setNullRepresentation("");
        distinguishedNameField.setDescription("Identifier of selected user, "
                + "defined while registration");
        distinguishedNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the firstNameField
     */
    public final TextField getFirstNameField() {
        if (firstNameField == null) {
            buildFirstNameField();
        }
        return firstNameField;
    }

    /**
     *
     */
    private void buildFirstNameField() {
        String id = "firstNameField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        firstNameField = new TextField(UserDataContainer.Property.FIRST_NAME.columnHeader);
        firstNameField.setId(DEBUG_ID_PREFIX + id);
        firstNameField.setWidth("100%");
        firstNameField.setImmediate(true);
        firstNameField.setRequired(true);
        firstNameField.setReadOnly(true);
        firstNameField.setNullRepresentation("");
        firstNameField.setDescription("First name of selected user");
        firstNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the lastNameField
     */
    public final TextField getLastNameField() {
        if (lastNameField == null) {
            buildLastNameField();
        }
        return lastNameField;
    }

    /**
     *
     */
    private void buildLastNameField() {
        String id = "lastNameField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        lastNameField = new TextField(UserDataContainer.Property.LAST_NAME.columnHeader);
        lastNameField.setId(DEBUG_ID_PREFIX + id);
        lastNameField.setWidth("100%");
        lastNameField.setImmediate(true);
        lastNameField.setRequired(true);
        lastNameField.setReadOnly(true);
        lastNameField.setNullRepresentation("");
        lastNameField.setDescription("Last name of selected user");
        lastNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the emailField
     */
    public final TextField getEmailField() {
        if (emailField == null) {
            buildEmailField();
        }
        return emailField;
    }

    /**
     *
     */
    private void buildEmailField() {
        String id = "emailField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        emailField = new TextField(UserDataContainer.Property.EMAIL.columnHeader);
        emailField.setId(DEBUG_ID_PREFIX + id);
        emailField.setWidth("100%");
        emailField.setImmediate(true);
        emailField.setRequired(true);
        emailField.setNullRepresentation("");
        emailField.setReadOnly(true);
        emailField.setDescription("Email address of selected user");
        emailField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the validFromField
     */
    public final DateField getValidFromField() {
        if (validFromField == null) {
            buildValidFromField();
        }
        return validFromField;
    }

    /**
     *
     */
    private void buildValidFromField() {
        String id = "validFromField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        validFromField = new DateField(UserDataContainer.Property.VALID_FROM.columnHeader);
        validFromField.setId(DEBUG_ID_PREFIX + id);
        validFromField.setWidth("100%");
        validFromField.setImmediate(true);
        validFromField.setReadOnly(true);
        validFromField.setDateFormat("dd.MM.yyyy");
        validFromField.setDescription("Date from which the account of the selected user is available");
        validFromField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        validFromField.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                getValidUntilField().setRangeStart((Date) event.getProperty().getValue());
            }
        });
    }

    /**
     * @return the validUntilField
     */
    public final DateField getValidUntilField() {
        if (validUntilField == null) {
            buildValidUntilField();
        }
        return validUntilField;
    }

    /**
     *
     */
    private void buildValidUntilField() {
        String id = "validUntilField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        validUntilField = new DateField(UserDataContainer.Property.VALID_UNTIL.columnHeader);
        validUntilField.setId(DEBUG_ID_PREFIX + id);
        validUntilField.setWidth("100%");
        validFromField.setImmediate(true);
        validUntilField.setReadOnly(true);
        validUntilField.setDateFormat("dd.MM.yyyy");
        validUntilField.setDescription("Date until which the user's KIT Data Manager account is valid");
        validUntilField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        validUntilField.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                getValidFromField().setRangeEnd((Date) event.getProperty().getValue());
            }
        });
    }

    /**
     * @return the maximumRoleBox
     */
    public final ComboBox getMaximumRoleBox() {
        if (maximumRoleBox == null) {
            buildMaximumRoleBox();
        }
        return maximumRoleBox;
    }

    /**
     *
     */
    private void buildMaximumRoleBox() {
        String id = "maximumRoleBox";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        maximumRoleBox = new ComboBox("MAXIMUM ROLE");
        maximumRoleBox.setId(DEBUG_ID_PREFIX + id);
        maximumRoleBox.setWidth("100%");
        maximumRoleBox.setRequired(true);
        maximumRoleBox.setNullSelectionAllowed(false);
        maximumRoleBox.setReadOnly(true);
        maximumRoleBox.setDescription("Maximum role of the selected user");
        maximumRoleBox.addItem(Role.NO_ACCESS);
        maximumRoleBox.addItem(Role.GUEST);
        maximumRoleBox.addItem(Role.MEMBER);
        maximumRoleBox.addItem(Role.MEMBERSHIP_REQUESTED);
        maximumRoleBox.addItem(Role.MANAGER);
        maximumRoleBox.addItem(Role.ADMINISTRATOR);
        maximumRoleBox.select(null);
        maximumRoleBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     *
     * @return the commitChangesButton
     */
    public final Button getCommitChangesButton() {
        if (commitChangesButton == null) {
            buildCommitChangesButton();
        }
        return commitChangesButton;
    }

    /**
     *
     */
    private void buildCommitChangesButton() {
        String id = "commitChangesButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        commitChangesButton = new Button("Commit Changes");
        commitChangesButton.setId(DEBUG_ID_PREFIX + id);
        commitChangesButton.setSizeFull();
        commitChangesButton.setImmediate(true);
        commitChangesButton.setDescription("Click for committing the changes "
                + "applied on the selected user data.");

        commitChangesButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                UserDataTablePanel userDataTablePanel= userDataAdministrationTab.getUserDataTablePanel();
                UserData clonedUserData = cloneUserData(userDataTablePanel.getSelectedUserData());
                UserData changedUserData = changeUserData(clonedUserData);
                try {
                    commitChanges(changedUserData);
                    userDataTablePanel.updateTableEntry(userDataTablePanel.getSelectedUserData(), changedUserData);
                } catch (DBCommitException ex) {
                    String object = "the changed user data of '" + changedUserData.getDistinguishedName() + "'";
                    LOGGER.error(MsgBuilder.commitFailed(object) + "Cause: " + ex.getMessage(), ex);
                }
            }
        });
    }

    /**
     *
     * @param userData
     * @return
     */
    private UserData cloneUserData(UserData userData) {
        UserData clonedUserData = new UserData();
        clonedUserData.setCurrentGroup(userData.getCurrentGroup());
        clonedUserData.setCurrentRole(userData.getCurrentRole());
        clonedUserData.setDistinguishedName(userData.getDistinguishedName());
        clonedUserData.setEmail(userData.getEmail());
        clonedUserData.setFirstName(userData.getFirstName());
        clonedUserData.setLastName(userData.getLastName());
        clonedUserData.setUserId(userData.getUserId());
        clonedUserData.setValidFrom(userData.getValidFrom());
        clonedUserData.setValidUntil(userData.getValidUntil());
        return clonedUserData;
    }

    /**
     *
     * @param selectedUserData
     * @return
     */
    private UserData changeUserData(UserData selectedUserData) {
        selectedUserData.setFirstName((String) getFirstNameField().getValue());
        selectedUserData.setLastName((String) getLastNameField().getValue());
        selectedUserData.setValidFrom((Date) getValidFromField().getValue());
        selectedUserData.setValidUntil((Date) getValidUntilField().getValue());
        return selectedUserData;
    }

    /**
     *
     * @param selectedUserData
     * @throws DBCommitException
     */
    private void commitChanges(UserData selectedUserData) throws DBCommitException {
        // Commit maximum role separately as it is not an attribute of UserData
        try {
            Role newRole = (Role) getMaximumRoleBox().getValue();
            UserId userId = new UserId((String) getDistinguishedNameField().getValue());
            IAuthorizationContext authCtx = userDataAdministrationTab.getParentApp().getAuthorizationContext();
            UserServiceLocal.getSingleton().setRoleRestriction(userId, newRole, authCtx);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "the maximum role of " + selectedUserData.getDistinguishedName();
            userDataAdministrationTab.getParentApp().showWarning("Maximum role not modifiable! Cause: "
                    + NoteBuilder.unauthorizedChangeRequest(object));
            throw new DBCommitException(MsgBuilder.unauthorizedChangeRequest(object), ex);
        } catch (EntityNotFoundException ex) {
            String object = "user '" + selectedUserData.getDistinguishedName() + "'";
            userDataAdministrationTab.getParentApp().showError("Maximum role not modifiable! Cause: "
                    + NoteBuilder.notFound(object));
            throw new DBCommitException(MsgBuilder.notFound(object), ex);
        } catch (AuthorizationException ex) {
            userDataAdministrationTab.getParentApp().showWarning("Maximum role not modifiable! Cause: "
                    + NoteBuilder.unauthorizedContext());
            throw new DBCommitException(MsgBuilder.unauthorizedContext(), ex);
        }
        // Commit user data
        try {
            userDataAdministrationTab.getParentApp().getMetaDataManager().save(selectedUserData);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "user data of '" + selectedUserData.getDistinguishedName() + "'";
            userDataAdministrationTab.getParentApp().showWarning("User data not modifiable! Cause: "
                    + NoteBuilder.unauthorizedSaveRequest(object));
            throw new DBCommitException(MsgBuilder.unauthorizedSaveRequest(object), ex);
        }
    }

    /**
     * @return the showMembershipsButton
     */
    public Button getShowMembershipsButton() {
        if (showMembershipsButton == null) {
            buildShowMembershipsButton();
        }
        return showMembershipsButton;
    }

    /**
     *
     */
    private void buildShowMembershipsButton() {
        String id = "showMembershipsButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        showMembershipsButton = new Button("Show Memberships");
        showMembershipsButton.setId(DEBUG_ID_PREFIX + id);
        showMembershipsButton.setSizeFull();
        showMembershipsButton.setImmediate(true);
        showMembershipsButton.setDescription(
                "Click to view all the memberships of the selected user.");

        showMembershipsButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!isActionValid()) {
                    userDataAdministrationTab.getParentApp().showWarning(NoteBuilder.unauthorizedContext());
                    return;
                }
                try {
                    UI.getCurrent().addWindow(new MembershipsView(userDataAdministrationTab));
                } catch (UnsupportedEnumException | AuthorizationException ex) {
                    LOGGER.error("Failed to open '" + MembershipsView.class.getSimpleName() 
                            + "'. Cause: "+ ex.getMessage(), ex);
                }
            }

            private boolean isActionValid() {
                boolean atLeastManager = false;
                try {
                    atLeastManager = userDataAdministrationTab.getParentApp().getAuthorizationContext()
                            .getRoleRestriction().atLeast(Role.MANAGER);
                } catch (AuthorizationException ex) {
                    LOGGER.warn("Failed to determine the maximum role of loggedInUser. Cause: "
                            + MsgBuilder.unauthorizedContext(), ex);
                }
                return atLeastManager;
            }
        });
    }

    /**
     *
     * @return
     */
    public Embedded getRegisterUserButton() {
        if (registerUserEmbeddedIcon == null) {
            buildRegisterUserEmbeddedIcon();
        }
        return registerUserEmbeddedIcon;
    }

    /**
     *
     */
    private void buildRegisterUserEmbeddedIcon() {
        String id = "registerUserButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        registerUserEmbeddedIcon = new Embedded("", new ThemeResource(IconContainer.USER_NEW));
        registerUserEmbeddedIcon.setId(DEBUG_ID_PREFIX + id);
        registerUserEmbeddedIcon.addStyleName("myclickablecomponent");
        registerUserEmbeddedIcon.setImmediate(true);
        
        registerUserEmbeddedIcon.addClickListener(new MouseEvents.ClickListener() {

            @Override
            public void click(MouseEvents.ClickEvent event) {
                userDataAdministrationTab.getParentApp().showWarning("User registration "
                        + "only via registration form of KIT Data Manager possible.");
            }
        });
    }

    /**
     *
     * @param lock
     */
    private void setLockedUserDataComponents(boolean lock) {
        getUserIdField().setReadOnly(lock);
        getDistinguishedNameField().setReadOnly(lock);
        getFirstNameField().setReadOnly(lock);
        getLastNameField().setReadOnly(lock);
        getEmailField().setReadOnly(lock);
        getValidFromField().setReadOnly(lock);
        getValidUntilField().setReadOnly(lock);
        getMaximumRoleBox().setEnabled(!lock);
        getMaximumRoleBox().setReadOnly(false);
    }

    /**
     *
     * @param userData
     */
    public void updateComponentValues(UserData userData) {
        clearUserDataComponents();
        if (userData == null) {
            LOGGER.debug("No user data selected.");
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
            IAuthorizationContext authCtx = userDataAdministrationTab.getParentApp().getAuthorizationContext();
            UserId userId = new UserId(userData.getDistinguishedName());
            Role newMaxRole = (Role) UserServiceLocal.getSingleton().getRoleRestriction(userId, authCtx);
            getMaximumRoleBox().setValue(newMaxRole);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "the maximum role of " + userData.getDistinguishedName();
            userDataAdministrationTab.getParentApp().showWarning(NoteBuilder.unauthorizedGetRequest(object));
            LOGGER.error("Failed to update '" + getMaximumRoleBox().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        } catch (AuthorizationException ex) {
            String object = "user '" + userData.getDistinguishedName() + "'";
            userDataAdministrationTab.getParentApp().showWarning(NoteBuilder.notFound(object));
            LOGGER.error("Failed to update '" + getMaximumRoleBox().getId() + "'. Cause: "
                    + MsgBuilder.notFound(object), ex);
        }
    }

    /**
     *
     */
    public void clearUserDataComponents() {
        setLockedUserDataComponents(false);
        getUserIdField().setValue(null);
        getDistinguishedNameField().setValue(null);
        getFirstNameField().setValue(null);
        getLastNameField().setValue(null);
        getEmailField().setValue(null);
        getValidFromField().setValue(null);
        getValidUntilField().setValue(null);
        getMaximumRoleBox().select(null);
    }

    /**
     * 
     * @param role 
     */
    public void update(Role role) {
        updateComponentValues(
                userDataAdministrationTab.getUserDataTablePanel().getSelectedUserData());
        UserDataEffictivity userDataValidation
                = userDataAdministrationTab.getUserDataTablePanel().validateSelectedUserData();
        switch (role) {
            case ADMINISTRATOR:
                switchToAdministratorView(userDataValidation);
                break;
            case MANAGER:
                switchToManagerView(userDataValidation);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'User Administration'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + this.getId() + ". Cause: Undefined enum "
                        + "constant detected, namely '" + role.name() + "'.");
                break;
        }
    }

    /**
     *
     * @param userDataValidation
     */
    private void switchToAdministratorView(UserDataEffictivity userDataValidation) {
        switch (userDataValidation) {
            case NO:
            case INVALID:
                UIComponentTools.setLockedLayoutComponents(this, true);
                break;
            case VALID:
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
            case DISABLED_USER:
                // Lock components
                getUserIdField().setReadOnly(true);
                getDistinguishedNameField().setReadOnly(true);
                getEmailField().setReadOnly(true);
                getFirstNameField().setReadOnly(true);
                getLastNameField().setReadOnly(true);
                getValidFromField().setReadOnly(true);
                getValidUntilField().setReadOnly(true);
                // Unlock components
                getMaximumRoleBox().setEnabled(true);
                getMaximumRoleBox().setReadOnly(false);
                getCommitChangesButton().setEnabled(true);
                getShowMembershipsButton().setEnabled(true);
                getRegisterUserButton().setEnabled(true);
                break;
            case LOGGED_IN_USER:
                // Lock components
                setLockedUserDataComponents(true);
                getCommitChangesButton().setEnabled(false);
                // Unlock components
                getShowMembershipsButton().setEnabled(true);
                getRegisterUserButton().setEnabled(true);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'User Administration'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + this.getId() + ". Cause: Undefined enum "
                        + "constant detected, namely '" + userDataValidation.name() + "'.");
                break;
        }
    }

    /**
     *
     * @param userDataValidation
     */
    private void switchToManagerView(UserDataEffictivity userDataValidation) {
        switch (userDataValidation) {
            case NO:
            case INVALID:
                UIComponentTools.setLockedLayoutComponents(this, true);
                break;
            case VALID:
            case DISABLED_USER:
            case LOGGED_IN_USER:
                // Lock components
                setLockedUserDataComponents(true);
//                getCommitChangesButton().setEnabled(false);
//                // Unlock components
//                getShowMembershipsButton().setEnabled(true);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'User Administration'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + this.getId() + ". Cause: Undefined enum "
                        + "constant detected, namely '" + userDataValidation.name() + "'.");
                break;
        }
    }
}
