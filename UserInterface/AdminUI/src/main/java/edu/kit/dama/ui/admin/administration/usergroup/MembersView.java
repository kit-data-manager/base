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

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.Group;
import edu.kit.dama.authorization.entities.impl.Membership;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.ui.admin.container.MembersContainer;
import edu.kit.dama.ui.admin.exception.DBCommitException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UnsupportedEnumException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.mdm.base.UserData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class MembersView extends Window implements TwinColSelect.ValueChangeListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(MembersView.class);
    public final static String DEBUG_ID_PREFIX = MembersView.class.getName() + "_";

    private final UserGroupAdministrationTab userGroupAdministrationTab;
    public final static String COLUMN_ID_CONFLICT = "conflict";

    private final GroupId groupId;
    private HorizontalSplitPanel mainPanel;
    private VerticalLayout leftPanelLayout;
    private GridLayout rightPanelLayout;
    private Table membersTable;
    private ComboBox roleComboBox;
    private Button commitChangeButton;
    private TwinColSelect userSelector;
    private Collection<String> formerUserSelection = new ArrayList<>();

    public enum UserSelectionSize {

        EQUAL,
        EXTENDED,
        REDUCED;
    }

    public MembersView(UserGroupAdministrationTab groupAdministrationTab) throws AuthorizationException, UnsupportedEnumException {
        this.userGroupAdministrationTab = groupAdministrationTab;
        groupId = new GroupId(groupAdministrationTab.getUserGroupTablePanel()
                .getSelectedUserGroup().getGroupId());

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setWidth("900px");
        setHeight("500px");
        setModal(true);
        setCaption("Members of '" + groupId.getStringRepresentation() + "'");
        setImmediate(true);
        setContent(getMainPanel());
    }

    /**
     *
     * @return @throws AuthorizationException
     * @throws UnsupportedEnumException
     */
    public final HorizontalSplitPanel getMainPanel() throws AuthorizationException, UnsupportedEnumException {
        if (mainPanel == null) {
            buildMainPanel();
        }
        return mainPanel;
    }

    /**
     *
     * @throws AuthorizationException
     * @throws UnsupportedEnumException
     */
    private void buildMainPanel() throws AuthorizationException, UnsupportedEnumException {
        String id = "mainPanel";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        mainPanel = new HorizontalSplitPanel();
        mainPanel.setId(DEBUG_ID_PREFIX + id);
        mainPanel.setSizeFull();
        mainPanel.setImmediate(true);

        mainPanel.setFirstComponent(getLeftPanelLayout());
        mainPanel.setSecondComponent(getRightPanelLayout());

        mainPanel.setSplitPosition(40, Unit.PERCENTAGE);

        updateMainPanel();
    }

    /**
     *
     * @return
     */
    public VerticalLayout getLeftPanelLayout() {
        if (leftPanelLayout == null) {
            buildLeftPanelLayout();
        }
        return leftPanelLayout;
    }

    /**
     *
     */
    private void buildLeftPanelLayout() {
        String id = "leftPanelLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        leftPanelLayout = new VerticalLayout();
        leftPanelLayout.setId(DEBUG_ID_PREFIX + id);
        leftPanelLayout.setSizeFull();
        leftPanelLayout.setMargin(true);
        leftPanelLayout.setImmediate(true);

        leftPanelLayout.addComponent(getMembersTable());
    }

    /**
     *
     * @return
     */
    public final Table getMembersTable() {
        if (membersTable == null) {
            buildMembersTable();
        }
        return membersTable;
    }

    /**
     *
     */
    private void buildMembersTable() {
        String id = "membersTable";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        membersTable = new Table();
        membersTable.setId(DEBUG_ID_PREFIX + id);
        membersTable.setWidth("100%");
        membersTable.setImmediate(true);
        membersTable.setSelectable(true);
        membersTable.setMultiSelect(false);

        membersTable.setContainerDataSource(new MembersContainer());
        membersTable.setVisibleColumns(MembersContainer.COLUMN_ORDER);
        membersTable.setColumnHeaders(MembersContainer.COLUMN_HEADERS);

        membersTable.addGeneratedColumn(COLUMN_ID_CONFLICT, new Table.ColumnGenerator() {

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Membership membership = (Membership) itemId;
                Label cellContent = new Label();
                cellContent.addStyleName(CSSTokenContainer.ORANGE_BOLD_CENTERED_LABEL);
                cellContent.setValue("?");
                cellContent.setDescription("Validatio of member role failed." + NoteBuilder.CONTACT);
                try {
                    UserId userId = new UserId(membership.getUser().getUserId());
                    IAuthorizationContext authCtx = userGroupAdministrationTab.getParentApp().getAuthorizationContext();
                    Role maximumRole = (Role) UserServiceLocal.getSingleton().getRoleRestriction(userId, authCtx);
                    if (!membership.getRole().moreThan(maximumRole)) {
                        return null;
                    }
                    cellContent.setValue(maximumRole.name());
                    cellContent.setDescription("ATTENTION: maximum role < role");
                } catch (UnauthorizedAccessAttemptException ex) {
                    String object = "maximum role of '" + membership.getUser().getUserId() + "'";
                    LOGGER.warn("Failed to check if member role is more than maximum role. Cause: "
                            + MsgBuilder.unauthorizedGetRequest(object), ex);
                } catch (EntityNotFoundException ex) {
                    String object = "user '" + membership.getUser().getUserId() + "'";
                    LOGGER.error("Failed to check if member role is more than maximum role. Cause: "
                            + MsgBuilder.notFound(object), ex);
                } catch (AuthorizationException ex) {
                    LOGGER.warn("Failed to check if member role is more than maximum role. Cause: "
                            + MsgBuilder.unauthorizedContext(), ex);
                }
                return cellContent;
            }
        });

        membersTable.setColumnAlignment(COLUMN_ID_CONFLICT, Table.Align.CENTER);
        membersTable.addValueChangeListener(new Table.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (getSelectedMember() == null || isLoggedInUser(getSelectedMember().getUser().getUserId())) {
                    getRoleComboBox().setEnabled(false);
                    return;
                }
                getRoleComboBox().setEnabled(true);
                Membership membership = getSelectedMember();
                getRoleComboBox().select(membership.getRole());
            }
        });

        membersTable.addItemSetChangeListener(new Container.ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(Container.ItemSetChangeEvent event) {
                getMembersTable().refreshRowCache();
            }
        });

        reloadMembersTable();
    }

    /**
     *
     * @return
     */
    public GridLayout getRightPanelLayout() {
        if (rightPanelLayout == null) {
            buildRightPanelLayout();
        }
        return rightPanelLayout;
    }

    /**
     *
     */
    private void buildRightPanelLayout() {
        String id = "rightPanelLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        rightPanelLayout = new GridLayout(2, 4);
        rightPanelLayout.setId(DEBUG_ID_PREFIX + id);
        rightPanelLayout.setSizeFull();
        rightPanelLayout.setMargin(true);
        rightPanelLayout.setSpacing(true);

        Label roleBoxDescription = new Label("Member roles listed in the "
                + "left-hand table can be changed by selecting them and choosing "
                + "the requested role from the combobox below.<p>");
        roleBoxDescription.setContentMode(ContentMode.HTML);
        roleBoxDescription.setSizeFull();

        Label twinSelectorDescription = new Label("Move users from the "
                + "left-hand to the right-hand list for adding more "
                + "members to the group'" + groupId.getStringRepresentation()
                + "'. For excluding members from the group, "
                + "move users from the right-hand to the left-hand list.<p>");
        twinSelectorDescription.setContentMode(ContentMode.HTML);
        twinSelectorDescription.setSizeFull();

        rightPanelLayout.addComponent(roleBoxDescription, 0, 0, 1, 0);
        rightPanelLayout.addComponent(getRoleComboBox(), 0, 1);
        rightPanelLayout.addComponent(getCommitChangeButton(), 1, 1);
        rightPanelLayout.addComponent(twinSelectorDescription, 0, 2, 1, 2);
        rightPanelLayout.addComponent(getUserSelector(), 0, 3, 1, 3);

        rightPanelLayout.setComponentAlignment(
                getCommitChangeButton(), Alignment.MIDDLE_RIGHT);
        rightPanelLayout.setComponentAlignment(
                getRoleComboBox(), Alignment.MIDDLE_LEFT);

        rightPanelLayout.setColumnExpandRatio(0, 0.7f);

        rightPanelLayout.setRowExpandRatio(0, 0.1f);
        rightPanelLayout.setRowExpandRatio(1, 0.1f);
        rightPanelLayout.setRowExpandRatio(2, 0.1f);
        rightPanelLayout.setRowExpandRatio(3, 0.7f);
    }

    /**
     *
     * @return
     */
    public ComboBox getRoleComboBox() {
        if (roleComboBox == null) {
            buildRoleComboBox();
        }
        return roleComboBox;
    }

    /**
     *
     */
    private void buildRoleComboBox() {
        String id = "roleComboBox";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        roleComboBox = new ComboBox("NEW ROLE");
        roleComboBox.setId(DEBUG_ID_PREFIX + id);
        roleComboBox.setSizeFull();
        roleComboBox.setImmediate(true);
        roleComboBox.setNullSelectionAllowed(false);
        roleComboBox.setEnabled(false);
        roleComboBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        roleComboBox.addItem(Role.NO_ACCESS);
        roleComboBox.addItem(Role.GUEST);
        roleComboBox.addItem(Role.MEMBER);
        roleComboBox.addItem(Role.MANAGER);
    }

    /**
     *
     * @return
     */
    public Button getCommitChangeButton() {
        if (commitChangeButton == null) {
            buildCommitChangeButton();
        }
        return commitChangeButton;
    }

    /**
     *
     */
    private void buildCommitChangeButton() {
        String id = "commitChangeButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        commitChangeButton = new Button("Commit Change");
        commitChangeButton.setId(DEBUG_ID_PREFIX + id);
        commitChangeButton.setImmediate(true);

        commitChangeButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!isActionValid()) {
                    return;
                }
                try {
                    commitChangedRole(getSelectedMember());
                    reloadMembersTable();
                    getMembersTable().select(getSelectedMember());
                    userGroupAdministrationTab.getParentApp().showNotification(
                            "Selected roles successfully updated.");
                } catch (DBCommitException ex) {
                    String object = "changed membership between user '" + getSelectedMember().getUser()
                            + "' and group '" + getSelectedMember().getGroup() + "'";
                    LOGGER.error(MsgBuilder.commitFailed(object), ex);
                }
            }

            /**
             *
             */
            private boolean isActionValid() {
                if (getSelectedMember() != null && getRoleComboBox().getValue() != null) {
                    return true;
                }
                StringBuilder note = new StringBuilder();
                if (getSelectedMember() == null) {
                    note.append("No member selected! ");
                }
                if (getRoleComboBox().getValue() == null) {
                    note.append("No (new) role set!");
                }
                userGroupAdministrationTab.getParentApp().showWarning(note.toString());
                LOGGER.warn("Action triggered by '" + getCommitChangeButton()
                        + "' denied. Cause: " + note);
                return false;
            }
        });

    }

    /**
     *
     * @param selectedMembership
     * @throws DBCommitException
     */
    private void commitChangedRole(Membership selectedMembership) throws DBCommitException {
        try {
            UserId userId = new UserId(selectedMembership.getUser().getUserId());
            Role newRole = (Role) getRoleComboBox().getValue();
            IAuthorizationContext authCtx = userGroupAdministrationTab.getACTX(groupId);
            GroupServiceLocal.getSingleton().changeRole(groupId, userId, newRole, authCtx);
        } catch (EntityNotFoundException ex) {
            String object = "user '" + selectedMembership.getUser().getUserId() + "'";
            userGroupAdministrationTab.getParentApp().showError("Role not modifiable! Cause: "
                    + NoteBuilder.notFound(object));
            throw new DBCommitException(MsgBuilder.notFound(object), ex);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "role of " + selectedMembership.getUser().getUserId()
                    + "' within '" + selectedMembership.getGroup().getGroupId() + "'";
            userGroupAdministrationTab.getParentApp().showError("Role not modifiable! Cause: "
                    + NoteBuilder.unauthorizedChangeRequest(object));
            throw new DBCommitException(MsgBuilder.unauthorizedChangeRequest(object), ex);
        } catch (AuthorizationException ex) {
            userGroupAdministrationTab.getParentApp().showError("Role not modifiable! Cause: "
                    + NoteBuilder.unauthorizedContext());
            throw new DBCommitException(MsgBuilder.unauthorizedContext(), ex);
        }
    }

    /**
     *
     * @return
     */
    public final TwinColSelect getUserSelector() {
        if (userSelector == null) {
            buildUserSelector();
        }
        return userSelector;
    }

    /**
     *
     */
    private void buildUserSelector() {
        String id = "userSelector";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        userSelector = new TwinColSelect();
        userSelector.setId(DEBUG_ID_PREFIX + id);
        userSelector.setSizeFull();
        userSelector.setImmediate(true);

        userSelector.addValueChangeListener(this);

        reloadUserSelector();
    }

    /**
     *
     * @param userSelection
     */
    private void updateMemberList(Collection<String> userSelection) {
        UserSelectionSize size = validateUserSelectionSize(userSelection);
        switch (size) {
            case EQUAL:
                break;
            case EXTENDED:
                Collection<String> users2Add = new ArrayList<>(userSelection);
                users2Add.removeAll(formerUserSelection);
                addMembers(users2Add);
                break;
            case REDUCED:
                Collection<String> users2Remove = new ArrayList<>(formerUserSelection);
                users2Remove.removeAll(userSelection);
                removeMembers(users2Remove);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unkown error occurred. "
                        + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update the user list. Cause: Undefined "
                        + "enum constant detected, namely '" + size.name() + "'.");
                break;
        }
    }

    /**
     *
     * @param userSelection
     * @return
     */
    private UserSelectionSize validateUserSelectionSize(Collection<String> userSelection) {
        if (formerUserSelection.size() > userSelection.size()) {
            return UserSelectionSize.REDUCED;
        } else if (formerUserSelection.size() < userSelection.size()) {
            return UserSelectionSize.EXTENDED;
        } else {
            return UserSelectionSize.EQUAL;
        }
    }

    /**
     *
     * @param userIds
     */
    private void addMembers(Collection<String> userIds) {
        for (String id : userIds) {
            try {
                UserId userId = new UserId(id);
                IAuthorizationContext authCtx = userGroupAdministrationTab.getACTX(groupId);
                GroupServiceLocal.getSingleton().addUser(groupId, userId, Role.MEMBER, authCtx);
            } catch (EntityNotFoundException ex) {
                String object = "user ' " + id + "' or group '" + groupId.getStringRepresentation() + "'";
                userGroupAdministrationTab.getParentApp().showError("Membership not granted! Cause: "
                        + NoteBuilder.notFound(object));
                LOGGER.error("Failed to add member! Cause: " + MsgBuilder.notFound(object), ex);
            } catch (EntityAlreadyExistsException ex) {
                String object = "membership between user '" + id + "' and group '"
                        + groupId.getStringRepresentation() + "'";
                userGroupAdministrationTab.getParentApp().showWarning("Membership not granted! Cause: "
                        + NoteBuilder.alreadyExists(object));
                LOGGER.warn("Failed to add member! Cause: " + MsgBuilder.alreadyExists(object), ex);
            } catch (UnauthorizedAccessAttemptException ex) {
                String object = "user '" + id + "' as a member of '"
                        + groupId.getStringRepresentation();
                userGroupAdministrationTab.getParentApp().showWarning("Membership not granted! Cause: "
                        + NoteBuilder.unauthorizedAddRequest(object));
                LOGGER.warn("Failed to add member! Cause: " + MsgBuilder.unauthorizedAddRequest(object), ex);
            } catch (AuthorizationException ex) {
                userGroupAdministrationTab.getParentApp().showWarning("Membership not granted! Cause: "
                        + NoteBuilder.unauthorizedContext());
                LOGGER.warn("Failed to add member! Cause: " + MsgBuilder.unauthorizedContext(), ex);
            }
        }
    }

    /**
     *
     * @param userSelection
     */
    private void removeMembers(Collection<String> userSelection) {
        for (String id : userSelection) {
            UserId userId = new UserId(id);
            if (isLoggedInUser(id)) {
                userGroupAdministrationTab.getParentApp().showWarning(NoteBuilder.unauthorizedContext());
                LOGGER.warn("Failed to remove user '" + id + "' from group '"
                        + groupId.getStringRepresentation() + "'. Cause: User is loggedInUser.");
                continue;
            }

            try {
                if (isLastGroupManager(userId)) {
                    userGroupAdministrationTab.getParentApp().showWarning("User '" + userId
                            + "' cannot be removed from this group. Cause: User is last group manager.");
                    LOGGER.warn("Failed to remove user '" + id + "' from group '"
                            + groupId.getStringRepresentation() + "'. Cause: User is last group manager.");
                    continue;
                }
                IAuthorizationContext authCtx = userGroupAdministrationTab.getACTX(groupId);
                GroupServiceLocal.getSingleton().removeUser(groupId, userId, authCtx);
            } catch (EntityNotFoundException ex) {
                String object = "user '" + id + "' or group '" + groupId.getStringRepresentation() + "'";
                userGroupAdministrationTab.getParentApp().showError("Member not removable from group! Cause: "
                        + NoteBuilder.notFound(object));
                LOGGER.error("Failed to remove member from group! Cause: " + MsgBuilder.notFound(object), ex);
            } catch (UnauthorizedAccessAttemptException ex) {
                String object = "user '" + id + "' as a member of '" + groupId.getStringRepresentation() + "'";
                userGroupAdministrationTab.getParentApp().showWarning("Member not removable from group! Cause: "
                        + NoteBuilder.unauthorizedRemoveRequest(object));
                LOGGER.warn("Failed to remove member from group! Cause: " + MsgBuilder.unauthorizedRemoveRequest(object), ex);
            } catch (AuthorizationException ex) {
                userGroupAdministrationTab.getParentApp().showWarning("Member not removable from group! Cause: "
                        + NoteBuilder.unauthorizedContext());
                LOGGER.warn("Failed to remove member from group! Cause: " + MsgBuilder.unauthorizedContext(), ex);
            }
        }
    }

    /**
     *
     * @param userId
     * @return
     * @throws UnauthorizedAccessAttemptException
     * @throws AuthorizationException
     */
    private boolean isLastGroupManager(UserId userId) throws UnauthorizedAccessAttemptException, AuthorizationException {
        IAuthorizationContext authCtx = userGroupAdministrationTab.getACTX(groupId);
        List<UserId> groupManagers = GroupServiceLocal.getSingleton().getGroupManagers(
                groupId, 0, Integer.MAX_VALUE, authCtx);
        for (UserId managerId : groupManagers) {
            if (!userId.getStringRepresentation().equals(managerId.getStringRepresentation())) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    private void reloadMembersTable() {
        getMembersTable().removeAllItems();
        try {
            Group group = FindUtil.findGroup(PU.entityManager(), groupId);
            for (Membership membership : group.getMemberships()) {
                getMembersTable().addItem(membership);
            }
        } catch (EntityNotFoundException ex) {
            String object = "user '" + groupId.getStringRepresentation() + "'";
            userGroupAdministrationTab.getParentApp().showError(
                    NoteBuilder.notFound(object));
            LOGGER.error("Failed to reload '" + getMembersTable().getId() + "'. Cause: "
                    + MsgBuilder.notFound(object), ex);
        }
    }
    
    /**
     *
     */
    private void reloadUserSelector() {
        getUserSelector().removeValueChangeListener(this);
        getUserSelector().removeAllItems();
        List<UserId> preselection = new ArrayList<>();
        try {
            List<UserData> userDataList = userGroupAdministrationTab.getParentApp()
                    .getMetaDataManager().find(UserData.class);
            for (UserData userData : userDataList) {
                getUserSelector().addItem(userData.getDistinguishedName());
                getUserSelector().setItemCaption(userData.getDistinguishedName(),
                        getUserSelectorItemCaption(userData));
            }
            IAuthorizationContext authCtx = userGroupAdministrationTab.getParentApp().getAuthorizationContext();
            preselection = GroupServiceLocal.getSingleton().getUsersIds(groupId, 0, Integer.MAX_VALUE, authCtx);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "information about registered users";
            userGroupAdministrationTab.getParentApp().showError("User-selector not reloadable! Cause: "
                    + NoteBuilder.unauthorizedGetRequest(object));
            LOGGER.warn("Failed to reload '" + getUserSelector().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        } catch (AuthorizationException ex) {
            userGroupAdministrationTab.getParentApp().showWarning("User-selector not reloadable! Cause: "
                    + NoteBuilder.unauthorizedContext());
            LOGGER.warn("Failed to reload '" + getUserSelector().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedContext(), ex);
        }
        Collection<String> preselectedUsers = new ArrayList<>();
        for (UserId userId : preselection) {
            preselectedUsers.add(userId.getStringRepresentation());
        }
        getUserSelector().setValue(preselectedUsers);
        getUserSelector().addValueChangeListener(this);
        formerUserSelection = preselectedUsers;
    }

    /**
     *
     * @param userData
     * @return
     */
    private String getUserSelectorItemCaption(UserData userData) {
        return userData.getFullname() + " (" + userData.getDistinguishedName() + ")";
    }

    /**
     *
     * @return
     */
    public Membership getSelectedMember() {
        return (Membership) getMembersTable().getValue();
    }

    /**
     *
     * @param userId
     * @return
     */
    private boolean isLoggedInUser(String userId) {
        return userGroupAdministrationTab.getParentApp().getLoggedInUser()
                .getDistinguishedName().equals(userId);
    }

    /**
     *
     * @param viewStatus
     */
    private void updateMainPanel() {
        // Execute case-independent commands:
        // Unlock all components of right and left panel for update
        UIComponentTools.setLockedLayoutComponents(getRightPanelLayout(), false);
        UIComponentTools.setLockedLayoutComponents(getLeftPanelLayout(), false);
        // Update components
        reloadMembersTable();
        getMembersTable().select(null);
        reloadUserSelector();
        // Execute case-dependent commands  
        UserGroupTablePanel.UserGroupEffectivity groupEffectivity
                = userGroupAdministrationTab.getUserGroupTablePanel().validateSelectedUserGroup();
        switch (groupEffectivity) {
            case NO:
            case VALID:
                break;
            case INVALID:
                UIComponentTools.setLockedLayoutComponents(getLeftPanelLayout(), true);
                UIComponentTools.setLockedLayoutComponents(getRightPanelLayout(), true);
                userGroupAdministrationTab.getParentApp().showNotification(
                        "Group data not editable as you are not logged in as administrator or group manager.");
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'MembersView'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update 'MembersView'. Cause: Undefined enum constant detectednamely '"
                        + groupEffectivity.name() + "'.");
                break;
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Collection<String> userSelection
                = (Collection<String>) getUserSelector().getValue();
        updateMemberList(userSelection);
        formerUserSelection = userSelection;
        reloadMembersTable();
        getMembersTable().select(null);
        reloadUserSelector();
    }
}
