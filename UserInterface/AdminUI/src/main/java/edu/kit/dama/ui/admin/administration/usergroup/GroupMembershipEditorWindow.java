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

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.Group;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.UnsupportedEnumException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.mdm.base.UserData;
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
public class GroupMembershipEditorWindow extends Window {

    private final static Logger LOGGER = LoggerFactory.getLogger(GroupMembershipEditorWindow.class);
    public final static String DEBUG_ID_PREFIX = GroupMembershipEditorWindow.class.getName() + "_";
    private final static String NAME_COLUMN_ID = "Name";
    private final static String ROLE_COLUMN_ID = "Role";
    private final static String INFO_COLUMN_ID = "Info";

    private final UserGroupAdministrationTab userGroupAdministrationTab;

    private final GroupId groupId;
    private GridLayout mainPanel;
    private Table membersTable;
    private Button addMembersButton;
    private Button excludeMembersButton;
    private ListSelect userSelector;

    public enum UserSelectionSize {

        EQUAL,
        EXTENDED,
        REDUCED;
    }

    public GroupMembershipEditorWindow(UserGroupAdministrationTab groupAdministrationTab) throws AuthorizationException, UnsupportedEnumException {
        this.userGroupAdministrationTab = groupAdministrationTab;
        groupId = new GroupId(groupAdministrationTab.getUserGroupTablePanel()
                .getSelectedUserGroup().getGroupId());

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setWidth("900px");
        setHeight("500px");
        setModal(true);
        setCaption("Members of group '" + groupId.getStringRepresentation() + "'");
        setContent(getMainLayout());
    }

    private GridLayout getMainLayout() throws AuthorizationException, UnsupportedEnumException {
        if (mainPanel == null) {
            String id = "mainPanel";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");
            UIUtils7.GridLayoutBuilder builder = new UIUtils7.GridLayoutBuilder(2, 2);
            //add table and exclude button
            builder.addComponent(getMembersTable(), 0, 0).addComponent(getExcludeMembersButton(), Alignment.BOTTOM_LEFT, 0, 1, 1, 1);

            Label l = new Label("To add members select them in the list below and click <i>'Add Member(s)'</i>. To exclude members select them in the table and click <i>'Exclude Member(s)'</i>", ContentMode.HTML);

            Button closeButton = new Button("Close");
            closeButton.addClickListener((event) -> {
                close();
            });

            VerticalLayout actionLayout = new VerticalLayout(l, getUserSelector(), getAddMembersButton());
            actionLayout.setComponentAlignment(l, Alignment.TOP_CENTER);
            actionLayout.setComponentAlignment(getUserSelector(), Alignment.TOP_RIGHT);
            actionLayout.setComponentAlignment(getAddMembersButton(), Alignment.TOP_RIGHT);
            actionLayout.setExpandRatio(getUserSelector(), 1.0f);
            actionLayout.setSpacing(true);
            actionLayout.setSizeFull();

            builder.addComponent(actionLayout, 1, 0, 1, 1).addComponent(closeButton, Alignment.BOTTOM_RIGHT, 1, 1, 1, 1);

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

    private Table getMembersTable() {
        if (membersTable == null) {
            String id = "membersTable";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            membersTable = new Table();
            membersTable.setId(DEBUG_ID_PREFIX + id);
            membersTable.setSizeFull();
            membersTable.setSelectable(true);
            membersTable.setMultiSelect(true);

            IndexedContainer c = new IndexedContainer();

            c.addContainerProperty(NAME_COLUMN_ID, String.class, null);
            c.addContainerProperty(ROLE_COLUMN_ID, Role.class, null);
            c.addContainerProperty(INFO_COLUMN_ID, String.class, null);
            membersTable.setContainerDataSource(c);
            membersTable.setColumnHeader(NAME_COLUMN_ID, "User Name");
            membersTable.setColumnHeader(ROLE_COLUMN_ID, "Role");
            membersTable.setColumnHeader(INFO_COLUMN_ID, "Info");

            membersTable.addGeneratedColumn(ROLE_COLUMN_ID, (Table source, Object itemId, Object columnId) -> {
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

            membersTable.addGeneratedColumn(INFO_COLUMN_ID, (Table source, Object itemId, Object columnId) -> {
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
        return membersTable;
    }

    private Button getAddMembersButton() {
        if (addMembersButton == null) {
            String id = "commitChangeButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            addMembersButton = new Button("Add Member(s)");
            addMembersButton.setId(DEBUG_ID_PREFIX + id);

            addMembersButton.addClickListener((Button.ClickEvent event) -> {
                Collection<String> selectedUserIds = (Collection<String>) userSelector.getValue();
                if (selectedUserIds.isEmpty()) {
                    return;
                }
                boolean wasSuccess = addMembers(selectedUserIds);
                getMembersTable().select(null);
                getUserSelector().select(null);
                if (wasSuccess) {
                    UIComponentTools.showInformation("Selected users successfully added.");
                } else {
                    UIComponentTools.showWarning("There where errors while adding the selected users. Please refer to the 'Info' column in the table.");
                }
            });
        }
        return addMembersButton;
    }

    private Button getExcludeMembersButton() {
        if (excludeMembersButton == null) {
            String id = "commitChangeButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            excludeMembersButton = new Button("Exclude Member(s)");
            excludeMembersButton.setId(DEBUG_ID_PREFIX + id);

            excludeMembersButton.addClickListener((Button.ClickEvent event) -> {
                Collection<String> selectedUserIds = (Collection<String>) getMembersTable().getValue();
                if (selectedUserIds.isEmpty()) {
                    return;
                }
                boolean wasSuccess = excludeMembers(selectedUserIds);
                getMembersTable().select(null);
                getUserSelector().select(null);
                if (wasSuccess) {
                    UIComponentTools.showInformation("Selected users successfully excluded.");
                } else {
                    UIComponentTools.showWarning("There where errors while excluding the selected users. Please refer to the 'Info' column in the table.");
                }
            });
        }
        return excludeMembersButton;
    }

    /**
     * Add the users with the provided user ids as members to the currently
     * selected group. The role of members is set to MEMBER and the users are
     * moved from the selectable user list to the members table.
     *
     * @param selectedUserIds A list of selected user ids.
     *
     * @return TRUE if all users where added as members, FALSE if at least one
     * user could not be added.
     */
    private boolean addMembers(Collection<String> selectedUserIds) {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());
        boolean fullSuccess = true;
        try {
            for (String id : selectedUserIds) {
                UserId userId = new UserId(id);
                try {
                    IAuthorizationContext authCtx = UIHelper.getSessionContext(groupId);
                    if (getMembersTable().getContainerDataSource().getItem(id) != null) {
                        //already in table but NO_ACCESS..change to member and update table
                        GroupServiceLocal.getSingleton().changeRole(groupId, userId, Role.MEMBER, authCtx);
                        //change also in table
                        getMembersTable().getContainerDataSource().getContainerProperty(id, ROLE_COLUMN_ID).setValue(Role.MEMBER);
                    } else {
                        //if not in table add to group and set MEMBER
                        GroupServiceLocal.getSingleton().addUser(groupId, userId, Role.MEMBER, authCtx);
                        //add user row
                        getMembersTable().getContainerDataSource().addItem(id);
                        UserData uData = mdm.findSingleResult("SELECT u FROM UserData u WHERE u.distinguishedName=?1", new Object[]{id}, UserData.class);
                        if (uData == null) {
                            //system user
                            getMembersTable().getContainerDataSource().getContainerProperty(id, NAME_COLUMN_ID).setValue(id);
                        } else {
                            getMembersTable().getContainerDataSource().getContainerProperty(id, NAME_COLUMN_ID).setValue(uData.getFullname());
                        }

                    }
                    //update role and info column
                    getMembersTable().getContainerDataSource().getContainerProperty(id, ROLE_COLUMN_ID).setValue(Role.MEMBER);
                    getMembersTable().getContainerDataSource().getContainerProperty(id, INFO_COLUMN_ID).setValue(null);
                } catch (EntityNotFoundException ex) {
                    getMembersTable().getContainerDataSource().getContainerProperty(id, INFO_COLUMN_ID).setValue("User/Group not found.");
                    fullSuccess = false;
                } catch (EntityAlreadyExistsException ex) {
                    getMembersTable().getContainerDataSource().getContainerProperty(id, INFO_COLUMN_ID).setValue("User was already member of group.");
                    fullSuccess = false;
                } catch (UnauthorizedAccessAttemptException ex) {
                    getMembersTable().getContainerDataSource().getContainerProperty(id, INFO_COLUMN_ID).setValue("You are not authorized to modify this group's users.");
                    fullSuccess = false;
                }
            }
            //update user selector and row cache for the case that at least one update was successful or at least to update the info column
            reloadUserSelector();
            getMembersTable().refreshRowCache();
            return fullSuccess;
        } finally {
            mdm.close();
        }
    }

    /**
     * Exclude the users with the provided user ids from the currently selected
     * group. The role of excluded members is set to NO_ACCESS and the users are
     * moved from the members table to the selectable user list.
     *
     * @param selectedUserIds A list of selected user ids.
     *
     * @return TRUE if all users where excluded, FALSE if at least one user
     * could not be excluded.
     */
    private boolean excludeMembers(Collection<String> selectedUserIds) {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());
        boolean fullSuccess = true;
        try {
            for (String id : selectedUserIds) {
                UserId userId = new UserId(id);
                if (UIHelper.getSessionUser().getDistinguishedName().equals(id)) {
                    getMembersTable().getContainerDataSource().getContainerProperty(id, INFO_COLUMN_ID).setValue("Excluding yourself from a group is not allowed.");
                    fullSuccess = false;
                    continue;
                }

                try {
                    //check if valid user or system account not having a UserData entity
                    UserData uData = mdm.findSingleResult("SELECT u FROM UserData u WHERE u.distinguishedName=?1", new Object[]{id}, UserData.class);
                    if (uData == null) {
                        //system user
                        getMembersTable().getContainerDataSource().getContainerProperty(id, INFO_COLUMN_ID).setValue("System accounts cannot be excluded.");
                        fullSuccess = false;
                        continue;
                    }

                    //check if the user is the last group manager
                    if (UIHelper.isLastGroupManager(groupId, userId)) {
                        getMembersTable().getContainerDataSource().getContainerProperty(id, INFO_COLUMN_ID).setValue("This user is the last group manager and cannot be excluded.");
                        fullSuccess = false;
                        continue;
                    }
                    //change the role to NO_ACCESS
                    IAuthorizationContext authCtx = UIHelper.getSessionContext(groupId);
                    GroupServiceLocal.getSingleton().changeRole(groupId, userId, Role.NO_ACCESS, authCtx);
                    //change also in table
                    getMembersTable().getContainerDataSource().getContainerProperty(id, ROLE_COLUMN_ID).setValue(Role.NO_ACCESS);

                } catch (EntityNotFoundException ex) {
                    getMembersTable().getContainerDataSource().getContainerProperty(id, INFO_COLUMN_ID).setValue("User/Group not found.");
                    fullSuccess = false;
                } catch (UnauthorizedAccessAttemptException ex) {
                    getMembersTable().getContainerDataSource().getContainerProperty(id, INFO_COLUMN_ID).setValue("You are not authorized to modify this group's users.");
                    fullSuccess = false;
                }
            }
            //update user selector and row cache for the case that at least one update was successful or at least to update the info column
            reloadUserSelector();
            getMembersTable().refreshRowCache();
            return fullSuccess;
        } finally {
            mdm.close();
        }
    }

    private ListSelect getUserSelector() {
        if (userSelector == null) {
            String id = "userSelector";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            userSelector = new ListSelect("USERS");
            userSelector.setId(DEBUG_ID_PREFIX + id);
            userSelector.setSizeFull();
            userSelector.setNullSelectionAllowed(false);
            userSelector.setMultiSelect(true);
            userSelector.setRows(5);

            reloadUserSelector();
        }
        return userSelector;
    }

    private void reloadMembersTable() {
        getMembersTable().getContainerDataSource().removeAllItems();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());

        try {
            Group theGroup = FindUtil.findGroup(PU.entityManager(), groupId);
            theGroup.getMemberships().stream().map((membership) -> {
                //add a new member row
                UserData member = null;
                try {
                    member = mdm.findSingleResult("SELECT u FROM UserData u WHERE u.distinguishedName=?1", new Object[]{membership.getUser().getUserId()}, UserData.class);
                    //null = no member or no user info
                } catch (UnauthorizedAccessAttemptException ex) {
                    //not authorized
                }
                getMembersTable().addItem(membership.getUser().getUserId());
                if (member != null) {
                    getMembersTable().getContainerDataSource().getContainerProperty(membership.getUser().getUserId(), NAME_COLUMN_ID).setValue(member.getFullname());
                    getMembersTable().getContainerDataSource().getContainerProperty(membership.getUser().getUserId(), INFO_COLUMN_ID).setValue(null);
                } else {
                    getMembersTable().getContainerDataSource().getContainerProperty(membership.getUser().getUserId(), NAME_COLUMN_ID).setValue(membership.getUser().getUserId());
                    getMembersTable().getContainerDataSource().getContainerProperty(membership.getUser().getUserId(), INFO_COLUMN_ID).setValue("System Account");
                }
                return membership;
            }).forEachOrdered((membership) -> {
                getMembersTable().getContainerDataSource().getContainerProperty(membership.getUser().getUserId(), ROLE_COLUMN_ID).setValue(membership.getRole());
            });
        } catch (EntityNotFoundException ex) {
            UIComponentTools.showWarning("You are not authorized to obtain membership information.");
        } finally {
            mdm.close();
        }
    }

    private void reloadUserSelector() {
        getUserSelector().removeAllItems();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());
        try {
            List<UserData> userDataList = mdm.find(UserData.class);
            Collection<String> itemIds = (Collection<String>) getMembersTable().getItemIds();

            //add users to user list if not in group or in group and role not NO_ACCESS
            for (UserData user : userDataList) {
                //check if user in table
                if (itemIds.contains(user.getDistinguishedName())) {
                    //members table contains user, but check if she is excluded
                    Role userRole = (Role) getMembersTable().getContainerDataSource().getContainerProperty(user.getDistinguishedName(), ROLE_COLUMN_ID).getValue();
                    if (userRole.moreThan(Role.NO_ACCESS)) {
                        //user is active group member, so not add her to the user list
                        continue;
                    }
                }
                //add user to list
                getUserSelector().addItem(user.getDistinguishedName());
                getUserSelector().setItemCaption(user.getDistinguishedName(), user.getFullname());
            }

        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "information about registered users";
            UIComponentTools.showError("You are not authorized to load the userlist.");
            LOGGER.error("Failed to reload '" + getUserSelector().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        } finally {
            mdm.close();
        }
    }

    private void updateMainPanel() {
        // Update components
        reloadMembersTable();

        reloadUserSelector();
        // Execute case-dependent commands  
        UserGroupTablePanel.UserGroupEffectivity groupEffectivity = userGroupAdministrationTab.getUserGroupTablePanel().validateSelectedUserGroup();
        boolean commitAllowed = true;

        switch (groupEffectivity) {
            case NO:
            case INVALID:
                commitAllowed = false;
                break;
            default://allow update
        }
        getAddMembersButton().setEnabled(commitAllowed);
        getExcludeMembersButton().setEnabled(commitAllowed);
        if (!commitAllowed) {
            UIComponentTools.showWarning("Invalid group selected. Update not supported.");
        }
    }
}
