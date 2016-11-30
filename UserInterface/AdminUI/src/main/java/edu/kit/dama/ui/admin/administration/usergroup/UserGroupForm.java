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

import com.vaadin.data.Validator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.ui.admin.administration.usergroup.UserGroupTablePanel.UserGroupEffectivity;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UnsupportedEnumException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
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

    //Create group components
    private Window createGroupWindow = null;
    private VerticalLayout createGroupLayout;
    private Button createUserGroupButton;
    private TextField newGroupIdField;
    private TextField newGroupNameField;
    private TextArea newGroupDescriptionField;
    private Button commitNewUserGroupButton;

    public UserGroupForm(UserGroupAdministrationTab groupAdministrationTab) {
        this.userGroupAdministrationTab = groupAdministrationTab;

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setColumns(5);
        setRows(4);
        setSizeFull();
        setSpacing(true);

        // Add components
        addComponent(getIdField(), 0, 0);
        addComponent(getGroupNameField(), 0, 1);
        addComponent(getDescriptionArea(), 0, 2, 2, 3);
        addComponent(getGroupIdField(), 2, 0);
        addComponent(getNumberOfMembersField(), 2, 1);
        addComponent(getCommitChangesButton(), 4, 0);
        addComponent(getShowMembersButton(), 4, 1);
        addComponent(getCreateUserGroupButton(), 4, 2, 4, 3);

        setColumnExpandRatio(0, 0.39f);
        setColumnExpandRatio(1, 0.01f);
        setColumnExpandRatio(2, 0.39f);
        setColumnExpandRatio(3, 0.01f);
        setColumnExpandRatio(4, 0.2f);

        setComponentAlignment(getCommitChangesButton(), Alignment.BOTTOM_RIGHT);
        setComponentAlignment(getShowMembersButton(), Alignment.MIDDLE_RIGHT);

        setComponentAlignment(getCreateUserGroupButton(), Alignment.MIDDLE_CENTER);
    }

    private TextField getIdField() {
        if (idField == null) {
            String id = "idField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            idField = new TextField("ID");
            idField.setId(DEBUG_ID_PREFIX + id);
            idField.setWidth("100%");
            idField.setReadOnly(true);
            idField.setNullRepresentation("");
            idField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return idField;
    }

    private TextField getGroupIdField() {
        if (groupIdField == null) {
            String id = "groupIdField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            groupIdField = new TextField("GROUPID");
            groupIdField.setId(DEBUG_ID_PREFIX + id);
            groupIdField.setWidth("100%");
            groupIdField.setReadOnly(true);
            groupIdField.setNullRepresentation("");
            groupIdField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return groupIdField;
    }

    private TextField getGroupNameField() {
        if (groupNameField == null) {
            String id = "groupNameField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            groupNameField = new TextField("GROUP NAME");
            groupNameField.setId(DEBUG_ID_PREFIX + id);
            groupNameField.setWidth("100%");
            groupNameField.setRequired(true);
            groupNameField.setReadOnly(true);
            groupNameField.setNullRepresentation("");
            groupNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return groupNameField;
    }

    private TextField getNumberOfMembersField() {
        if (numberOfMembersField == null) {
            String id = "numberOfMembersField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            numberOfMembersField = new TextField("NUMBER OF MEMBERS");
            numberOfMembersField.setId(DEBUG_ID_PREFIX + id);
            numberOfMembersField.setWidth("100%");
            numberOfMembersField.setReadOnly(true);
            numberOfMembersField.setNullRepresentation("");
            numberOfMembersField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return numberOfMembersField;
    }

    private TextArea getDescriptionArea() {
        if (descriptionArea == null) {
            String id = "descriptionArea";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            descriptionArea = new TextArea("DESCRIPTION");
            descriptionArea.setId(DEBUG_ID_PREFIX + id);
            descriptionArea.setSizeFull();
            descriptionArea.setReadOnly(true);
            descriptionArea.setNullRepresentation("");
            descriptionArea.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return descriptionArea;
    }

    private Button getShowMembersButton() {
        if (showMembersButton == null) {
            String id = "showMembersButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + "...");

            showMembersButton = new Button("Show Members");
            showMembersButton.setId(DEBUG_ID_PREFIX + id);
            showMembersButton.setWidth("100%");

            showMembersButton.addClickListener((Button.ClickEvent event) -> {
                try {
                    if (!UIHelper.getSessionContext().getRoleRestriction().atLeast(Role.MANAGER)) {
                        UIComponentTools.showWarning(NoteBuilder.unauthorizedContext());
                        return;
                    }
                    UI.getCurrent().addWindow(new GroupMembershipEditorWindow(userGroupAdministrationTab));
                } catch (UnsupportedEnumException | AuthorizationException ex) {
                    LOGGER.error("Failed to open '" + GroupMembershipEditorWindow.class.getSimpleName()
                            + "'. Cause: " + ex.getMessage(), ex);
                }
            });
        }
        return showMembersButton;
    }

    private Button getCommitChangesButton() {
        if (commitChangesButton == null) {
            String id = "commitChangesButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            commitChangesButton = new Button("Commit Changes");
            commitChangesButton.setId(DEBUG_ID_PREFIX + id);
            commitChangesButton.setWidth("100%");

            commitChangesButton.addClickListener((Button.ClickEvent event) -> {
                UserGroupTablePanel userGroupTablePanel = userGroupAdministrationTab.getUserGroupTablePanel();
                UserGroup selectedGroup = userGroupTablePanel.getSelectedUserGroup();
                selectedGroup.setGroupName(getGroupNameField().getValue());
                selectedGroup.setDescription(getDescriptionArea().getValue());
                IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
                mdm.setAuthorizationContext(UIHelper.getSessionContext());
                try {
                    mdm.save(selectedGroup);
                    userGroupTablePanel.updateTableEntry(selectedGroup);
                } catch (UnauthorizedAccessAttemptException ex) {
                    UIComponentTools.showWarning("You are not authorized to change the selected group.");
                } finally {
                    mdm.close();
                }
            });
        }
        return commitChangesButton;
    }

    protected final void clearGroupDataComponents() {
        lockGroupDataComponents(false);
        getIdField().setValue("");
        getGroupIdField().setValue("");
        getGroupNameField().setValue("");
        getNumberOfMembersField().setValue("");
        getDescriptionArea().setValue(null);
    }

    private void updateComponentValues(UserGroup userGroup) {
        clearGroupDataComponents();
        if (userGroup == null) {
            return;
        }
        getIdField().setValue(Long.toString(userGroup.getId()));
        getGroupIdField().setValue(userGroup.getGroupId());
        getGroupNameField().setValue(userGroup.getGroupName());
        getNumberOfMembersField().setValue(Integer.toString(userGroup.getMembers().size()));
        getDescriptionArea().setValue(userGroup.getDescription());
    }

    protected final void update(Role role) {
        //set the component values according to the selected group
        updateComponentValues(userGroupAdministrationTab.getUserGroupTablePanel().getSelectedUserGroup());

        UserGroupEffectivity userGroupValidation = userGroupAdministrationTab.getUserGroupTablePanel().validateSelectedUserGroup();
        switch (role) {
            case ADMINISTRATOR:
            case MANAGER:
                switchToAuthorizedView(userGroupValidation);
                break;
            default:
                //others should not appear here but if they do they also only read
                UIComponentTools.setLockedLayoutComponents(this, true);
                break;
        }
    }

    private void lockGroupDataComponents(boolean locked) {
        getIdField().setReadOnly(locked);
        getGroupIdField().setReadOnly(locked);
        getGroupNameField().setReadOnly(locked);
        getNumberOfMembersField().setReadOnly(locked);
        getDescriptionArea().setReadOnly(locked);
    }

    private void switchToAuthorizedView(UserGroupEffectivity userGroupValidation) {
        switch (userGroupValidation) {
            case VALID:
                //Logged in user is either administrator or a manager of the selecte group
                // Lock components
                getIdField().setReadOnly(true);
                getGroupIdField().setReadOnly(true);
                getNumberOfMembersField().setReadOnly(true);
                // Unlock components
                getGroupNameField().setReadOnly(false);
                getDescriptionArea().setReadOnly(false);
                getCommitChangesButton().setEnabled(true);
                getCreateUserGroupButton().setEnabled(true);
                getShowMembersButton().setEnabled(true);
                break;
            default:
                //No group selected or user is not authorized to modify group information
                //Lock components
                UIComponentTools.setLockedLayoutComponents(this, true);
                break;
        }
    }

    /**
     * GROUP CREATION RELATED FUNCTIONALITY *
     */
    private void showCreateGroupWindow() {
        if (createGroupWindow == null) {
            String id = "createGroupWindow";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            createGroupWindow = new Window("Create New Group");
            createGroupWindow.setModal(true);
            createGroupWindow.center();
            createGroupWindow.setId(DEBUG_ID_PREFIX + id);
            createGroupWindow.setContent(getCreateGroupLayout());
            //reset layout
            getNewGroupIdField().setValue("");
            getNewGroupNameField().setValue("");
            getNewGroupDescriptionField().setValue(null);

            createGroupWindow.addCloseListener((e) -> {
                hideCreateGroupWindow();
            });
        }
        UI.getCurrent().addWindow(createGroupWindow);
    }

    private void hideCreateGroupWindow() {
        if (createGroupWindow != null) {
            UI.getCurrent().removeWindow(createGroupWindow);
            createGroupWindow = null;
        }
    }

    private VerticalLayout getCreateGroupLayout() {
        if (createGroupLayout == null) {
            String id = "viewLayout";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            createGroupLayout = new VerticalLayout(getNewGroupIdField(), getNewGroupNameField(), getNewGroupDescriptionField(), getCommitNewGroupButton());
            createGroupLayout.setId(DEBUG_ID_PREFIX + id);
            createGroupLayout.setWidth("300px");
            createGroupLayout.setSpacing(true);
            createGroupLayout.setMargin(true);
            createGroupLayout.setCaption("NEW GROUP");
            createGroupLayout.addStyleName(CSSTokenContainer.GREY_CAPTION);

            createGroupLayout.setComponentAlignment(getNewGroupIdField(), Alignment.TOP_LEFT);
            createGroupLayout.setComponentAlignment(getNewGroupNameField(), Alignment.TOP_LEFT);
            createGroupLayout.setComponentAlignment(getNewGroupDescriptionField(), Alignment.TOP_LEFT);
            createGroupLayout.setComponentAlignment(getCommitNewGroupButton(), Alignment.BOTTOM_RIGHT);
        }
        return createGroupLayout;
    }

    private TextField getNewGroupIdField() {
        if (newGroupIdField == null) {
            String id = "newGroupIdField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            newGroupIdField = new TextField("GROUP ID");
            newGroupIdField.setId(DEBUG_ID_PREFIX + id);
            newGroupIdField.setWidth("100%");
            newGroupIdField.setRequired(true);
            newGroupIdField.setNullRepresentation("");
            newGroupIdField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return newGroupIdField;
    }

    private TextField getNewGroupNameField() {
        if (newGroupNameField == null) {
            String id = "newGroupNameField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            newGroupNameField = new TextField("GROUP NAME");
            newGroupNameField.setId(DEBUG_ID_PREFIX + id);
            newGroupNameField.setWidth("100%");
            newGroupNameField.setRequired(true);
            newGroupNameField.setNullRepresentation("");
            newGroupNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return newGroupNameField;
    }

    private TextArea getNewGroupDescriptionField() {
        if (newGroupDescriptionField == null) {
            String id = "newGroupDescriptionField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            newGroupDescriptionField = new TextArea("DESCRIPTION");
            newGroupDescriptionField.setId(DEBUG_ID_PREFIX + id);
            newGroupDescriptionField.setWidth("100%");
            newGroupDescriptionField.setRows(3);
            newGroupDescriptionField.setNullRepresentation("");
            newGroupDescriptionField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return newGroupDescriptionField;
    }

    private Button getCommitNewGroupButton() {
        if (commitNewUserGroupButton == null) {
            String id = "commitButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            commitNewUserGroupButton = new Button("Create");
            commitNewUserGroupButton.setId(DEBUG_ID_PREFIX + id);
            commitNewUserGroupButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, new int[]{});

            commitNewUserGroupButton.addClickListener((Button.ClickEvent event) -> {
                UserGroupTablePanel userGroupTablePanel = userGroupAdministrationTab.getUserGroupTablePanel();
                try {
                    UserGroup newUserGroup = collectUserGroupInformation();
                    persistUserGroup(newUserGroup);
                    userGroupTablePanel.addTableEntry(newUserGroup);
                    hideCreateGroupWindow();
                } catch (Validator.EmptyValueException ex) {
                    UIComponentTools.showWarning("Please fill all mandatory fields.");
                } catch (UnauthorizedAccessAttemptException ex) {
                    LOGGER.error("Not authorized to persist new UserGroup.", ex);
                    UIComponentTools.showWarning("You are not authorized to create a new UserGroup.");
                } catch (EntityAlreadyExistsException ex) {
                    LOGGER.error("Failed to persist new UserGroup. Group for id " + getGroupIdField().getValue() + " already exists.", ex);
                    UIComponentTools.showWarning("A UserGroup with the provided GroupId already exists.");
                } catch (EntityNotFoundException ex) {
                    LOGGER.error("Failed to persist new UserGroup.", ex);
                    UIComponentTools.showWarning("Internal error while creating UserGroup.");
                }
            });
        }
        return commitNewUserGroupButton;
    }

    private Button getCreateUserGroupButton() {
        if (createUserGroupButton == null) {
            String id = "createUserGroupButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + "...");

            createUserGroupButton = new Button();
            createUserGroupButton.setId(DEBUG_ID_PREFIX + id);

            createUserGroupButton.setIcon(new ThemeResource(IconContainer.GROUP_ADD));
            createUserGroupButton.setStyleName(BaseTheme.BUTTON_LINK);
            createUserGroupButton.setDescription("Create a new UserGroup");
            createUserGroupButton.setWidth("14%");
            createUserGroupButton.addClickListener((Button.ClickEvent event) -> {
                showCreateGroupWindow();
            });
        }
        return createUserGroupButton;
    }

    private UserGroup collectUserGroupInformation() throws Validator.EmptyValueException {
        if (UIComponentTools.isEmpty(getNewGroupIdField()) || UIComponentTools.isEmpty(getNewGroupNameField())) {
            throw new Validator.EmptyValueException(getGroupIdField().getId());
        }
        UIUtils7.validate(getNewGroupIdField());
        UIUtils7.validate(getNewGroupNameField());

        //validation ok, continue
        UserGroup newUserGroup = new UserGroup();
        newUserGroup.setGroupId((String) getNewGroupIdField().getValue());
        newUserGroup.setGroupName((String) getNewGroupNameField().getValue());
        newUserGroup.setDescription(getNewGroupDescriptionField().getValue());

        return newUserGroup;
    }

    private void persistUserGroup(UserGroup userGroup) throws UnauthorizedAccessAttemptException, EntityAlreadyExistsException, EntityNotFoundException {
        IAuthorizationContext ctx = UIHelper.getSessionContext();
        UserId loggedInUserId = new UserId(UIHelper.getSessionUser().getDistinguishedName());
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            GroupId groupId = new GroupId(userGroup.getGroupId());
            GroupServiceLocal.getSingleton().create(groupId, loggedInUserId, ctx);
            mdm.save(userGroup);
        } finally {
            mdm.close();
        }
    }

}
