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
package edu.kit.dama.ui.admin.administration.usergroup;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.ui.admin.administration.usergroup.UserGroupTablePanel.UserGroupEffectivity;
import edu.kit.dama.ui.admin.container.UserGroupContainer;
import edu.kit.dama.ui.admin.exception.DBCommitException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UnsupportedEnumException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.mdm.admin.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public final class UserGroupForm extends GridLayout {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(UserGroupForm.class);
    public final static String DEBUG_ID_PREFIX
            = UserGroupForm.class.getName() + "_";

    private final UserGroupAdministrationTab userGroupAdministrationTab;

    private TextField idField;
    private TextField groupIdField;
    private TextField groupNameField;
    private TextField numberOfMembersField;
    private TextArea descriptionArea;
    private Button commitChangesButton;
    private Button showMembersButton;
    private UserGroupBuilder userGroupBuilder;

    public UserGroupForm(UserGroupAdministrationTab groupAdministrationTab) {
        this.userGroupAdministrationTab = groupAdministrationTab;

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setColumns(5);
        setRows(4);
        setSizeFull();
        setImmediate(true);
        setSpacing(true);

        // Add components
        addComponent(getIdField(), 0, 0);
        addComponent(getGroupNameField(), 0, 1);
        addComponent(getDescriptionArea(), 0, 2, 2, 3);
        addComponent(getGroupIdField(), 2, 0);
        addComponent(getNumberOfMembersField(), 2, 1);
        addComponent(getCommitChangesButton(), 4, 0);
        addComponent(getShowMembersButton(), 4, 1);
        addComponent(getUserGroupBuilder(), 4, 2, 4, 3);

        setColumnExpandRatio(0, 0.39f);
        setColumnExpandRatio(1, 0.01f);
        setColumnExpandRatio(2, 0.39f);
        setColumnExpandRatio(3, 0.01f);
        setColumnExpandRatio(4, 0.2f);

        setComponentAlignment(getCommitChangesButton(), Alignment.BOTTOM_RIGHT);
        setComponentAlignment(getShowMembersButton(), Alignment.MIDDLE_RIGHT);
        setComponentAlignment(getUserGroupBuilder(), Alignment.MIDDLE_CENTER);
    }

    /**
     * @return the idField
     */
    public final TextField getIdField() {
        if (idField == null) {
            buildIdField();
        }
        return idField;
    }

    /**
     *
     */
    private void buildIdField() {
        String id = "idField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        idField = new TextField(UserGroupContainer.Property.ID.columnHeader);
        idField.setId(DEBUG_ID_PREFIX + id);
        idField.setWidth("100%");
        idField.setImmediate(true);
        idField.setReadOnly(true);
        idField.setNullRepresentation("");
        idField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the groupIdField
     */
    public final TextField getGroupIdField() {
        if (groupIdField == null) {
            buildGroupIdField();
        }
        return groupIdField;
    }

    /**
     *
     */
    private void buildGroupIdField() {
        String id = "groupIdField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        groupIdField = new TextField(UserGroupContainer.Property.GROUP_ID.columnHeader);
        groupIdField.setId(DEBUG_ID_PREFIX + id);
        groupIdField.setWidth("100%");
        groupIdField.setImmediate(true);
        groupIdField.setReadOnly(true);
        groupIdField.setNullRepresentation("");
        groupIdField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the groupNameField
     */
    public final TextField getGroupNameField() {
        if (groupNameField == null) {
            buildGroupNameField();
        }
        return groupNameField;
    }

    /**
     *
     */
    private void buildGroupNameField() {
        String id = "groupNameField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        groupNameField = new TextField(UserGroupContainer.Property.GROUP_NAME.columnHeader);
        groupNameField.setId(DEBUG_ID_PREFIX + id);
        groupNameField.setWidth("100%");
        groupNameField.setImmediate(true);
        groupNameField.setRequired(true);
        groupNameField.setReadOnly(true);
        groupNameField.setNullRepresentation("");
        groupNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the numberOfMembersField
     */
    public final TextField getNumberOfMembersField() {
        if (numberOfMembersField == null) {
            buildNumberOfMembersField();
        }
        return numberOfMembersField;
    }

    /**
     *
     */
    private void buildNumberOfMembersField() {
        String id = "numberOfMembersField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        numberOfMembersField = new TextField("NUMBER OF MEMBERS");
        numberOfMembersField.setId(DEBUG_ID_PREFIX + id);
        numberOfMembersField.setWidth("100%");
        numberOfMembersField.setImmediate(true);
        numberOfMembersField.setReadOnly(true);
        numberOfMembersField.setNullRepresentation("");
        numberOfMembersField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the descriptionArea
     */
    public final TextArea getDescriptionArea() {
        if (descriptionArea == null) {
            buildDescriptionArea();
        }
        return descriptionArea;
    }

    /**
     *
     */
    private void buildDescriptionArea() {
        String id = "descriptionArea";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        descriptionArea = new TextArea(
                UserGroupContainer.Property.DESCRIPTION.columnHeader);
        descriptionArea.setId(DEBUG_ID_PREFIX + id);
        descriptionArea.setSizeFull();
        descriptionArea.setReadOnly(true);
        descriptionArea.setNullRepresentation("");
        descriptionArea.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
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
        commitChangesButton.setWidth("100%");
        commitChangesButton.setImmediate(true);

        commitChangesButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                UserGroupTablePanel userGroupTablePanel = userGroupAdministrationTab.getUserGroupTablePanel();
                UserGroup selectedGroup = userGroupTablePanel.getSelectedUserGroup();
                UserGroup clonedUserGroup = cloneUserGroup(selectedGroup);
                UserGroup changedUserGroup = changeUserGroup(clonedUserGroup);
                try {
                    commitChanges(changedUserGroup);
                    userGroupTablePanel.updateTableEntry(selectedGroup, changedUserGroup);
                } catch (DBCommitException ex) {
                    String object = "the changed user group '" + changedUserGroup.getGroupId() + "'";
                    LOGGER.error(MsgBuilder.commitFailed(object) + "Cause: " + ex.getMessage(), ex);
                }
            }
        });
    }

    /**
     *
     * @param userGroup
     * @return
     */
    private UserGroup cloneUserGroup(UserGroup userGroup) {
        UserGroup clonedUserGroup = new UserGroup();
        clonedUserGroup.setId(userGroup.getId());
        clonedUserGroup.setGroupId(userGroup.getGroupId());
        clonedUserGroup.setGroupName(userGroup.getGroupName());
        clonedUserGroup.setDescription(userGroup.getDescription());
        return clonedUserGroup;
    }

    /**
     *
     */
    private UserGroup changeUserGroup(UserGroup userGroup) {
        userGroup.setGroupName((String) getGroupNameField().getValue());
        userGroup.setDescription((String) getDescriptionArea().getValue());
        return userGroup;
    }

    /**
     *
     * @param userGroup
     */
    private void commitChanges(UserGroup userGroup) throws DBCommitException {
        try {
            userGroupAdministrationTab.getParentApp().getMetaDataManager().save(userGroup);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "changed user group '" + userGroup + "'";
            userGroupAdministrationTab.getParentApp().showWarning("User group not modifiable! Cause: "
                    + NoteBuilder.unauthorizedSaveRequest(object));
            throw new DBCommitException(MsgBuilder.unauthorizedSaveRequest(object), ex);
        }
    }

    /**
     * @return the showMembersButton
     */
    public final Button getShowMembersButton() {
        if (showMembersButton == null) {
            buildShowMembersButton();
        }
        return showMembersButton;
    }

    /**
     *
     */
    private void buildShowMembersButton() {
        String id = "showMembersButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + "...");

        showMembersButton = new Button("Show Members");
        showMembersButton.setId(DEBUG_ID_PREFIX + id);
        showMembersButton.setImmediate(true);
        showMembersButton.setWidth("100%");

        showMembersButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!isActionValid()) {
                    userGroupAdministrationTab.getParentApp().showWarning(NoteBuilder.unauthorizedContext());
                    return;
                }
                try {
                    UI.getCurrent().addWindow(new MembersView(userGroupAdministrationTab));
                } catch (UnsupportedEnumException | AuthorizationException ex) {
                    LOGGER.error("Failed to open '" + MembersView.class.getSimpleName() 
                            + "'. Cause: "+ ex.getMessage(), ex);
                }
            }

            private boolean isActionValid() {
                boolean atLeastManager = false;
                try {
                    atLeastManager = userGroupAdministrationTab.getParentApp().getAuthorizationContext()
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
     * @return
     */
    public UserGroupBuilder getUserGroupBuilder() {
        if (userGroupBuilder == null) {
            userGroupBuilder = new UserGroupBuilder(userGroupAdministrationTab);
        }
        return userGroupBuilder;
    }

    /**
     *
     * @param id
     * @param groupId
     * @param groupName
     * @param numberOfMembers
     * @param description
     */
    private void setLockedGroupDataComponents(boolean lock) {
        getIdField().setReadOnly(lock);
        getGroupIdField().setReadOnly(lock);
        getGroupNameField().setReadOnly(lock);
        getNumberOfMembersField().setReadOnly(lock);
        getDescriptionArea().setReadOnly(lock);
    }

    /**
     *
     * @param userGroup
     */
    public void updateComponentValues(UserGroup userGroup) {
        clearGroupDataComponents();
        if (userGroup == null) {
            LOGGER.debug("No user group selected!");
            return;
        }
        getIdField().setValue(Long.toString(userGroup.getId()));
        getGroupIdField().setValue(userGroup.getGroupId());
        getGroupNameField().setValue(userGroup.getGroupName());
        getNumberOfMembersField().setValue(
                Integer.toString(userGroup.getMembers().size()));
        getDescriptionArea().setValue(userGroup.getDescription());
    }

    /**
     *
     */
    public void clearGroupDataComponents() {
        setLockedGroupDataComponents(false);
        getIdField().setValue(null);
        getGroupIdField().setValue(null);
        getGroupNameField().setValue(null);
        getNumberOfMembersField().setValue(null);
        getDescriptionArea().setValue(null);
    }

    /**
     *
     * @throws AuthorizationException
     */
    void update(Role role) {
        updateComponentValues(
                userGroupAdministrationTab.getUserGroupTablePanel().getSelectedUserGroup());
        UserGroupEffectivity userGroupValidation
                = userGroupAdministrationTab.getUserGroupTablePanel().validateSelectedUserGroup();
        switch (role) {
            case ADMINISTRATOR:
            case MANAGER:
                switchToAuthorizedView(userGroupValidation);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'User Group Administration'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + this.getId() + ". Cause: Undefined enum "
                        + "constant detected, namely '" + role.name() + "'.");
                break;
        }
    }

    /**
     *
     * @param userGroupValidation
     */
    private void switchToAuthorizedView(UserGroupEffectivity userGroupValidation) {
        switch (userGroupValidation) {
            case NO:
            case INVALID:
                // Lock components
                UIComponentTools.setLockedLayoutComponents(this, true);
                break;
            case VALID:
                // Lock components
                getIdField().setReadOnly(true);
                getGroupIdField().setReadOnly(true);
                getNumberOfMembersField().setReadOnly(true);
                // Unlock components
                getGroupNameField().setReadOnly(false);
                getDescriptionArea().setReadOnly(false);
                getCommitChangesButton().setEnabled(true);
                getShowMembersButton().setEnabled(true);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'User Group Administration'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + this.getId() + ". Cause: Undefined enum "
                        + "constant detected, namely '" + userGroupValidation.name() + "'.");
                break;
        }
    }
}
