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

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.ui.admin.utils.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public final class MembershipRoleEditorWindow extends Window {

    public final static Logger LOGGER = LoggerFactory.getLogger(MembershipRoleEditorWindow.class);
    public final static String DEBUG_ID_PREFIX = MembershipRoleEditorWindow.class.getName() + "_";

    private final static String NAME_COLUMN_ID = "Name";
    private final static String ROLE_COLUMN_ID = "Role";
    private final static String INFO_COLUMN_ID = "Info";

    private final UserDataAdministrationTab userDataAdministrationTab;
    private final UserId userId;

    private GridLayout mainPanel;
    private Table membershipsTable;
    private ComboBox roleComboBox;
    private Button commitChangeButton;

    /**
     * Create a new instance of a Memberships view window.
     *
     * @param userDataAdministrationTab The parent tab.
     */
    public MembershipRoleEditorWindow(UserDataAdministrationTab userDataAdministrationTab) {
        this.userDataAdministrationTab = userDataAdministrationTab;
        userId = new UserId(userDataAdministrationTab.getUserDataTablePanel().getSelectedUserData().getDistinguishedName());
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setWidth("900px");
        setHeight("500px");
        setModal(true);
        setCaption("Memberships of '" + userId.getStringRepresentation() + "'");
        setContent(getMainLayout());
    }

    /**
     * Get the main layout.
     *
     * @return The main layout.
     */
    private GridLayout getMainLayout() {
        if (mainPanel == null) {
            String id = "mainPanel";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            UIUtils7.GridLayoutBuilder builder = new UIUtils7.GridLayoutBuilder(2, 2);

            builder.addComponent(getMembershipsTable(), 0, 0);
            Label l = new Label("To update the membership role of the selected group(s) select the new role below and click <i>'Apply New Role'</i>. The new role is limited by the user's maximum role.", ContentMode.HTML);
            Label spacer = new Label("<br/>", ContentMode.HTML);

            Button closeButton = new Button("Close");
            closeButton.addClickListener((event) -> {
                close();
            });

            VerticalLayout actionLayout = new VerticalLayout(l, spacer, getRoleComboBox(), getCommitChangeButton());
            actionLayout.setComponentAlignment(l, Alignment.TOP_CENTER);
            actionLayout.setComponentAlignment(getRoleComboBox(), Alignment.TOP_RIGHT);
            actionLayout.setComponentAlignment(getCommitChangeButton(), Alignment.TOP_RIGHT);
            actionLayout.setComponentAlignment(getCommitChangeButton(), Alignment.TOP_RIGHT);
            actionLayout.setSpacing(true);
            actionLayout.setSizeFull();

            builder.addComponent(actionLayout, 1, 0);
            builder.addComponent(closeButton, Alignment.BOTTOM_RIGHT, 1, 1, 1, 1);

            mainPanel = builder.getLayout();
            mainPanel.setId(DEBUG_ID_PREFIX + id);
            mainPanel.setSizeFull();

            mainPanel.setColumnExpandRatio(0, .7f);
            mainPanel.setColumnExpandRatio(1, .3f);
            mainPanel.setRowExpandRatio(0, .99f);
            mainPanel.setRowExpandRatio(1, .01f);
            mainPanel.setSpacing(true);
            mainPanel.setMargin(true);
            updateMainPanel();
        }
        return mainPanel;
    }

    /**
     * Get the memberships table.
     *
     * @return The memberships table.
     */
    private Table getMembershipsTable() {
        if (membershipsTable == null) {
            String id = "membershipsTable";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            membershipsTable = new Table();
            membershipsTable.setId(DEBUG_ID_PREFIX + id);
            membershipsTable.setSizeFull();
            membershipsTable.setSelectable(true);
            membershipsTable.setMultiSelect(true);

            IndexedContainer c = new IndexedContainer();

            c.addContainerProperty(NAME_COLUMN_ID, String.class, null);
            c.addContainerProperty(ROLE_COLUMN_ID, Role.class, null);
            c.addContainerProperty(INFO_COLUMN_ID, String.class, null);
            membershipsTable.setContainerDataSource(c);
            membershipsTable.setColumnHeader(NAME_COLUMN_ID, "Group Name");
            membershipsTable.setColumnHeader(ROLE_COLUMN_ID, "Role");
            membershipsTable.setColumnHeader(INFO_COLUMN_ID, "Info");

            membershipsTable.addGeneratedColumn(ROLE_COLUMN_ID, (Table source, Object itemId, Object columnId) -> {
                Role role = (Role) source.getContainerDataSource().getContainerProperty(itemId, columnId).getValue();
                Label cellContent = new Label();
                if (role != null && role.moreThan(Role.NO_ACCESS)) {
                    cellContent.addStyleName(CSSTokenContainer.GREEN_BOLD_CENTERED_LABEL);
                    cellContent.setValue(role.toString());
                } else if (role != null && !role.moreThan(Role.NO_ACCESS)) {
                    cellContent.addStyleName(CSSTokenContainer.RED_BOLD_CENTERED_LABEL);
                    cellContent.setValue(role.toString());
                } else {
                    cellContent.addStyleName(CSSTokenContainer.ORANGE_BOLD_CENTERED_LABEL);
                    cellContent.setValue("NO MEMBER");
                }
                return cellContent;
            });

            membershipsTable.addGeneratedColumn(INFO_COLUMN_ID, (Table source, Object itemId, Object columnId) -> {
                String value = (String) source.getContainerDataSource().getContainerProperty(itemId, columnId).getValue();
                Label cellContent = new Label();
                cellContent.addStyleName(CSSTokenContainer.RED_BOLD_CENTERED_LABEL);
                if (value != null) {
                    cellContent.setValue("!");
                    cellContent.setDescription(value);
                } else {
                    cellContent.setValue("");
                    cellContent.setDescription(null);
                }
                return cellContent;
            });
        }
        return membershipsTable;
    }

    /**
     * Get the role selection combobox.
     *
     * @return The ComboBox.
     */
    private ComboBox getRoleComboBox() {
        if (roleComboBox == null) {
            String id = "roleComboBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            roleComboBox = new ComboBox("NEW ROLE");
            roleComboBox.setId(DEBUG_ID_PREFIX + id);
            roleComboBox.setWidth("100%");
            roleComboBox.setNullSelectionAllowed(false);
            try {
                Role maxRole = (Role) UserServiceLocal.getSingleton().getRoleRestriction(userId, UIHelper.getSessionContext());
                for (Role role : Role.getValidRoles()) {
                    if (role.moreThan(maxRole)) {
                        //do not continue
                        break;
                    }
                    roleComboBox.addItem(role);
                }
                //select max. role by default
                roleComboBox.select(maxRole);
            } catch (UnauthorizedAccessAttemptException | EntityNotFoundException ex) {
                //unable to determine max. role, just continue
            }
            roleComboBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);

        }
        return roleComboBox;
    }

    /**
     * Get the commit button.
     *
     * @return The commit button.
     */
    private Button getCommitChangeButton() {
        if (commitChangeButton == null) {
            String id = "commitChangeButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            commitChangeButton = new Button("Apply New Role");
            commitChangeButton.setId(DEBUG_ID_PREFIX + id);
            commitChangeButton.setDescription("Apply the selected role to all selected group memberships.");
            commitChangeButton.addClickListener((Button.ClickEvent event) -> {
                boolean wasSuccess = commitChangedRoles();
                getMembershipsTable().select(null);
                if (wasSuccess) {
                    UIComponentTools.showInformation("Selected memberships successfully updated.");
                } else {
                    UIComponentTools.showWarning("There where errors during membership update. Please refer to the 'Info' column in the table.");
                }
            });
        }
        return commitChangeButton;
    }

    /**
     * Commit changes to the memberships.
     *
     * @return TRUE if all updates where successful.
     */
    private boolean commitChangedRoles() {
        Collection<String> groupIds = (Collection<String>) getMembershipsTable().getValue();
        boolean wasSuccess = true;
        for (String groupId : groupIds) {
            //update role for selecte group
            GroupId gid = new GroupId(groupId);
            Role newRole = (Role) getRoleComboBox().getValue();
            //obtain current role to check "last manager"
            Role currentRole = (Role) getMembershipsTable().getContainerDataSource().getContainerProperty(groupId, ROLE_COLUMN_ID).getValue();
            if (currentRole != null && currentRole.atLeast(Role.MANAGER) && newRole.lessThan(currentRole)) {
                //current role is manager, check if we have the last manager
                if (UIHelper.isLastGroupManager(gid, userId)) {
                    //unable to take away last manager permission
                    getMembershipsTable().getContainerDataSource().getContainerProperty(groupId, INFO_COLUMN_ID).setValue("Unable to update group. User '" + userId + "' is the only group manager.");
                    wasSuccess = false;
                }
            }

            if (wasSuccess) {
                //if no error until now, try to update role using default context but ONLY if the role (in the USERS group) is at least CURATOR
                wasSuccess = UIHelper.getSessionUserRole().atLeast(Role.CURATOR) && updateRole(groupId, userId, newRole, UIHelper.getSessionContext());
                if (!wasSuccess) {
                    //update using default context failed/not possible, use group-specific context
                    wasSuccess = updateRole(groupId, userId, newRole, UIHelper.getSessionContext(gid));
                }
            }
            if (wasSuccess) {
                //reset error it no error occured
                getMembershipsTable().getContainerDataSource().getContainerProperty(groupId, INFO_COLUMN_ID).setValue(null);
            }
        }
        getMembershipsTable().refreshRowCache();
        return wasSuccess;
    }

    /**
     * Update the membership role for the provided user in the provided group to
     * 'role'.
     *
     * @param groupId The groupId.
     * @param userId The userId.
     * @param role The new role.
     * @param context The authorization context used to authorize the membership
     * update.
     *
     * @return TRUE if everything succeeded.
     */
    private boolean updateRole(String groupId, UserId userId, Role role, IAuthorizationContext context) {
        boolean result = false;
        try {
            GroupServiceLocal.getSingleton().changeRole(new GroupId(groupId), userId, role, context);
            //update table
            getMembershipsTable().getContainerDataSource().getContainerProperty(groupId, ROLE_COLUMN_ID).setValue(role);
            result = true;
        } catch (EntityNotFoundException ex) {
            //no member?
            try {
                GroupServiceLocal.getSingleton().addUser(new GroupId(groupId), userId, role, context);
                getMembershipsTable().getContainerDataSource().getContainerProperty(groupId, ROLE_COLUMN_ID).setValue(role);
                result = true;
            } catch (EntityAlreadyExistsException ex1) {
                //should not happen            
                getMembershipsTable().getContainerDataSource().getContainerProperty(groupId, INFO_COLUMN_ID).setValue("Membership of user '" + userId + "' in group '" + groupId + "' already exists.");
            } catch (EntityNotFoundException | UnauthorizedAccessAttemptException ex1) {
                getMembershipsTable().getContainerDataSource().getContainerProperty(groupId, INFO_COLUMN_ID).setValue("Failed to create membership of user '" + userId + "' in group '" + groupId + "'. User or group not found/accessible.");
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            //not authorized
            getMembershipsTable().getContainerDataSource().getContainerProperty(groupId, INFO_COLUMN_ID).setValue("You are not authorized to modify the membership of user '" + userId + "' in group '" + groupId + "'.");
        }
        return result;
    }

    /**
     * Update the main panel, e.g. reload the memberships table.
     */
    private void updateMainPanel() {
        // Update components
        reloadMembershipsTable();

        // Execute case-dependent commands  
        UserDataTablePanel.UserDataEffectivity userEffectivity = userDataAdministrationTab.getUserDataTablePanel().validateSelectedUserData();
        boolean commitAllowed = true;

        switch (userEffectivity) {
            case NO:
            case INVALID:
                commitAllowed = false;
                break;
            default://allow update
        }
        getCommitChangeButton().setEnabled(commitAllowed);
        if (!commitAllowed) {
            UIComponentTools.showWarning("Invalid user selected. Update not supported.");
        }
    }

    /**
     * Reload the memberships table.
     */
    private void reloadMembershipsTable() {
        getMembershipsTable().getContainerDataSource().removeAllItems();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());

        try {
            List<UserGroup> groups = mdm.find(UserGroup.class);
            for (UserGroup group : groups) {
                IRoleRestriction role = null;
                try {
                    role = GroupServiceLocal.getSingleton().getMaximumRole(new GroupId(group.getGroupId()), userId, UIHelper.getSessionContext());
                } catch (EntityNotFoundException ex) {
                    //no member, role remains null
                }

                //add a new group row
                getMembershipsTable().getContainerDataSource().addItem(group.getGroupId());
                getMembershipsTable().getContainerDataSource().getContainerProperty(group.getGroupId(), NAME_COLUMN_ID).setValue(group.getGroupName());
                getMembershipsTable().getContainerDataSource().getContainerProperty(group.getGroupId(), ROLE_COLUMN_ID).setValue(role);
                getMembershipsTable().getContainerDataSource().getContainerProperty(group.getGroupId(), INFO_COLUMN_ID).setValue(null);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            UIComponentTools.showWarning("You are not authorized to obtain membership information.");
        } finally {
            mdm.close();
        }
    }

}
