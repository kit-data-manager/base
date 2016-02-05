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

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.server.Sizeable.Unit;
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
import edu.kit.dama.authorization.entities.impl.Membership;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.ui.admin.container.MembershipsContainer;
import edu.kit.dama.ui.admin.exception.DBCommitException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UnsupportedEnumException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.mdm.admin.UserGroup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public final class MembershipsView extends Window implements TwinColSelect.ValueChangeListener {

    public final static Logger LOGGER = LoggerFactory.getLogger(MembershipsView.class);
    public final static String DEBUG_ID_PREFIX = MembershipsView.class.getName() + "_";
    public final static String COLUMN_ID_CONFLICT = "conflict";

    private final UserDataAdministrationTab userDataAdministrationTab;
    private final UserId userId;

    private HorizontalSplitPanel mainPanel;
    private VerticalLayout leftPanelLayout;
    private GridLayout rightPanelLayout;
    private Table membershipsTable;
    private ComboBox roleComboBox;
    private Button commitChangeButton;
    private TwinColSelect groupSelector;
    private Label introductionLabel;
    private Collection<String> formerGroupSelection = new ArrayList<>();

    public enum GroupSelectionSize {

        EQUAL,
        EXTENDED,
        REDUCED;
    }
    /**
     *
     * @param userDataAdministrationTab
     * @throws UnsupportedEnumException
     * @throws AuthorizationException
     */
    public MembershipsView(UserDataAdministrationTab userDataAdministrationTab)
            throws UnsupportedEnumException, AuthorizationException {
        this.userDataAdministrationTab = userDataAdministrationTab;
        userId = new UserId(userDataAdministrationTab.getUserDataTablePanel()
                .getSelectedUserData().getDistinguishedName());

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setWidth("900px");
        setHeight("500px");
        setModal(true);
        setCaption("Memberships of '" + userId.getStringRepresentation() + "'");
        setImmediate(true);
        setContent(getMainPanel());
    }

    /**
     *
     * @return @throws AuthorizationException
     * @throws UnsupportedEnumException
     */
    public HorizontalSplitPanel getMainPanel() throws AuthorizationException,
            UnsupportedEnumException {
        if (mainPanel == null) {
            buildMainPanel();
        }
        return mainPanel;
    }

    /**
     *
     */
    private void buildMainPanel() throws AuthorizationException, UnsupportedEnumException {
        String id = "mainPanel";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        mainPanel = new HorizontalSplitPanel();
        mainPanel.setId(DEBUG_ID_PREFIX + id);
        mainPanel.setSizeFull();

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
        leftPanelLayout.setImmediate(true);
        leftPanelLayout.setMargin(true);

        leftPanelLayout.addComponent(getMembershipsTable());
    }

    /**
     *
     * @return
     */
    public Table getMembershipsTable() {
        if (membershipsTable == null) {
            buildMembershipsTable();
        }
        return membershipsTable;
    }

    /**
     *
     */
    private void buildMembershipsTable() {
        String id = "membershipsTable";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        membershipsTable = new Table();
        membershipsTable.setId(DEBUG_ID_PREFIX + id);
        membershipsTable.setImmediate(true);
        membershipsTable.setWidth("100%");
        membershipsTable.setSelectable(true);
        membershipsTable.setMultiSelect(true);

        membershipsTable.setContainerDataSource(new MembershipsContainer());
        membershipsTable.setVisibleColumns(MembershipsContainer.COLUMN_ORDER);
        membershipsTable.setColumnHeaders(MembershipsContainer.COLUMN_HEADERS);

        membershipsTable.addGeneratedColumn(COLUMN_ID_CONFLICT, new Table.ColumnGenerator() {

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Role role = ((Membership) itemId).getRole();
                Label cellContent = new Label();
                cellContent.addStyleName(CSSTokenContainer.ORANGE_BOLD_CENTERED_LABEL);
                cellContent.setValue("?");
                cellContent.setDescription("Validation of member roles failed. " + NoteBuilder.CONTACT);
                try {
                    EntityManager em = PU.entityManager();
                    User user = FindUtil.findUser(em, userId);
                    em.close();
                    if (!role.moreThan(user.getMaximumRole())) {
                        return null;
                    }
                    cellContent.setValue(user.getMaximumRole().name());
                    cellContent.setDescription("ATTENTION: maximum role < role");
                } catch (EntityNotFoundException ex) {
                    String object = "user '" + userId.getStringRepresentation() + "'";
                    LOGGER.error("Failed to check if member role is more than maximum role. Cause: "
                            + MsgBuilder.notFound(object), ex);
                }
                return cellContent;
            }
        });

        membershipsTable.setColumnAlignment(COLUMN_ID_CONFLICT, Table.Align.CENTER);
        membershipsTable.addItemSetChangeListener(new Container.ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(Container.ItemSetChangeEvent event) {
                getMembershipsTable().refreshRowCache();
            }
        });

        membershipsTable.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (getSelectedMemberships().size() != 1) {
                    getRoleComboBox().select(null);
                    return;
                }
                Membership membership = (Membership) getSelectedMemberships().toArray()[0];
                getRoleComboBox().select(membership.getRole());
            }
        });
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
     * @param userRolesValidation
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

        Label twinSelectorDescription = new Label("Move groups from the "
                + "left-hand to the right-hand list for granting more memberships "
                + "to the user '" + userId.getStringRepresentation() + "'. "
                + "For canceling memberships of the user, move the groups from "
                + "the right-hand to the left-hand list.<p>");
        twinSelectorDescription.setContentMode(ContentMode.HTML);
        twinSelectorDescription.setSizeFull();

        rightPanelLayout.addComponent(roleBoxDescription, 0, 0, 1, 0);
        rightPanelLayout.addComponent(getRoleComboBox(), 0, 1);
        rightPanelLayout.addComponent(getCommitChangeButton(), 1, 1);
        rightPanelLayout.addComponent(twinSelectorDescription, 0, 2, 1, 2);
        rightPanelLayout.addComponent(getGroupSelector(), 0, 3, 1, 3);

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
        roleComboBox.setEnabled(false);
        roleComboBox.setNullSelectionAllowed(false);
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
                    commitChangedRoles();
                    reloadMembershipsTable();
                    getMembershipsTable().select(null);
                    userDataAdministrationTab.getParentApp().showNotification(
                            "Selected roles successfully updated.");
                } catch (DBCommitException ex) {
                    String object = "changed user roles of '" + userId.getStringRepresentation() + "'";
                    LOGGER.error(MsgBuilder.commitFailed(object) + "Cause: " + ex.getMessage(), ex);
                }
            }

            /**
             *
             */
            private boolean isActionValid() {
                if (!getSelectedMemberships().isEmpty() && getRoleComboBox().getValue() != null) {
                    return true;
                }
                StringBuilder note = new StringBuilder();
                if (getSelectedMemberships().isEmpty()) {
                    note.append("No memberships selected! ");
                }
                if (getRoleComboBox().getValue() == null) {
                    note.append("No (new) role set! ");
                }
                userDataAdministrationTab.getParentApp().showWarning(note.toString());
                LOGGER.warn("Action triggered by '" + getCommitChangeButton()
                        + "' denied. Cause: " + note);
                return false;
            }
        });
    }

    /**
     *
     * @throws DBCommitException
     */
    private void commitChangedRoles() throws DBCommitException {
        for (Membership membership : getSelectedMemberships()) {
            try {
                GroupId groupId = new GroupId(membership.getGroup().getGroupId());
                Role newRole = (Role) getRoleComboBox().getValue();
                IAuthorizationContext authCtx = userDataAdministrationTab.getAuthCtx(groupId);
                GroupServiceLocal.getSingleton().changeRole(groupId, userId, newRole, authCtx);
            } catch (EntityNotFoundException ex) {
                String object = "user '" + membership.getUser().getUserId()
                        + "' or group '" + membership.getGroup().getGroupId() + "'";
                userDataAdministrationTab.getParentApp().showError("Roles not modifiable! Cause: "
                        + NoteBuilder.notFound(object));
                throw new DBCommitException(MsgBuilder.notFound(object), ex);
            } catch (UnauthorizedAccessAttemptException ex) {
                String object = "the user role of '" + membership.getUser().getUserId()
                        + "' within the group '" + membership.getGroup().getGroupId() + "'";
                userDataAdministrationTab.getParentApp().showWarning("Roles not modifiable! Cause: "
                        + NoteBuilder.unauthorizedChangeRequest(object));
                throw new DBCommitException(MsgBuilder.unauthorizedChangeRequest(object), ex);
            } catch (AuthorizationException ex) {
                userDataAdministrationTab.getParentApp().showWarning("Roles not modifiable! Cause: "
                        + NoteBuilder.unauthorizedContext());
                throw new DBCommitException(MsgBuilder.unauthorizedContext(), ex);
            }
        }
    }

    /**
     *
     * @return
     */
    public TwinColSelect getGroupSelector() {
        if (groupSelector == null) {
            buildGroupSelector();
        }
        return groupSelector;
    }

    /**
     *
     */
    private void buildGroupSelector() {
        String id = "groupSelector";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        groupSelector = new TwinColSelect();
        groupSelector.setId(DEBUG_ID_PREFIX + id);
        groupSelector.setImmediate(true);
        groupSelector.setSizeFull();
        groupSelector.addValueChangeListener(this);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        Collection<String> groupSelection = (Collection<String>) getGroupSelector().getValue();
        updateMembershipList(groupSelection);
        formerGroupSelection = groupSelection;
        reloadMembershipsTable();
        getMembershipsTable().select(null);
        reloadGroupSelector();
    }

    /**
     *
     * @param groupSelection
     */
    private void updateMembershipList(Collection<String> groupSelection) {
        GroupSelectionSize size = validateGroupSelectionSize(groupSelection);
        switch (size) {
            case EQUAL:
                break;
            case EXTENDED:
                Collection<String> groups2Add = new ArrayList<>(groupSelection);
                groups2Add.removeAll(formerGroupSelection);
                createMemberships(groups2Add);
                break;
            case REDUCED:
                Collection<String> groups2Remove = new ArrayList<>(formerGroupSelection);
                groups2Remove.removeAll(groupSelection);
                cancelMemberships(groups2Remove);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unkown error occurred. "
                        + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update user's membership list. Cause: "
                        + "Undefined enum constant detected, namely '" + size.name() + "'.");
                break;
        }
    }

    /**
     *
     * @param groupSelection
     * @return
     */
    private GroupSelectionSize validateGroupSelectionSize(Collection<String> groupSelection) {
        if (formerGroupSelection.size() > groupSelection.size()) {
            return GroupSelectionSize.REDUCED;
        } else if (formerGroupSelection.size() < groupSelection.size()) {
            return GroupSelectionSize.EXTENDED;
        } else {
            return GroupSelectionSize.EQUAL;
        }
    }

    /**
     *
     * @param groupSelection
     */
    private void createMemberships(Collection<String> groupSelection) {
        for (String id : groupSelection) {
            GroupId groupId = new GroupId(id);
            try {
                IAuthorizationContext authCtx = userDataAdministrationTab.getAuthCtx(groupId);
                GroupServiceLocal.getSingleton().addUser(groupId, userId, Role.MEMBER, authCtx);
            } catch (EntityNotFoundException ex) {
                String object = "user '" + userId.getStringRepresentation() + "' or group '"
                        + groupId + "'";
                userDataAdministrationTab.getParentApp().showError("Membership not granted! Cause: "
                        + NoteBuilder.notFound(object));
                LOGGER.error("Failed to create membership! Cause: " + MsgBuilder.notFound(object), ex);
            } catch (EntityAlreadyExistsException ex) {
                String object = "membership between user '" + userId.getStringRepresentation()
                        + "' and group '" + groupId + "'";
                userDataAdministrationTab.getParentApp().showWarning("Membership not granted! Cause: "
                        + NoteBuilder.alreadyExists(object));
                LOGGER.warn("Failed to create membership! Cause: " + MsgBuilder.alreadyExists(object), ex);
            } catch (UnauthorizedAccessAttemptException ex) {
                String object = "user '" + userId.getStringRepresentation() + "' as a member of '"
                        + groupId + "'";
                userDataAdministrationTab.getParentApp().showWarning("Membership not granted! Cause: "
                        + NoteBuilder.unauthorizedAddRequest(object));
                LOGGER.warn("Failed to create membership! Cause: " + MsgBuilder.unauthorizedAddRequest(object), ex);
            } catch (AuthorizationException ex) {
                userDataAdministrationTab.getParentApp().showWarning("Membership not granted! Cause: "
                        + NoteBuilder.unauthorizedContext());
                LOGGER.warn("Failed to create membership! Cause: " + MsgBuilder.unauthorizedContext(), ex);
            }
        }
    }

    /**
     *
     * @param groupSelection
     */
    private void cancelMemberships(Collection<String> groupSelection) {
        for (String id : groupSelection) {
            GroupId groupId = new GroupId(id);
            if (isLastGroupManager(groupId, userId)) {
                userDataAdministrationTab.getParentApp().showWarning(
                        "Membership not cancelable! Cause: User is last group manager of '" + id + "'.");
                LOGGER.warn("Failed to cancel membership! Cause: '" + userId.getStringRepresentation()
                        + "' is last group manager of '" + id);
                continue;
            }
            try {
                IAuthorizationContext authCtx = userDataAdministrationTab.getAuthCtx(groupId);
                GroupServiceLocal.getSingleton().removeUser(groupId, userId, authCtx);
            } catch (EntityNotFoundException ex) {
                String object = "user '" + userId.getStringRepresentation() + "' or group '"
                        + id + "'";
                userDataAdministrationTab.getParentApp().showError("Membership not cancelable! Cause: "
                        + NoteBuilder.notFound(object));
                LOGGER.error("Failed to cancel membership! Cause: " + MsgBuilder.notFound(object), ex);
            } catch (UnauthorizedAccessAttemptException ex) {
                String object = "user '" + userId.getStringRepresentation() + "' as a member of '"
                        + id + "'";
                userDataAdministrationTab.getParentApp().showWarning("Membership not cancelable! Cause: "
                        + NoteBuilder.unauthorizedRemoveRequest(object));
                LOGGER.warn("Failed to cancel membership! Cause: '"
                        + MsgBuilder.unauthorizedRemoveRequest(object), ex);
            } catch (AuthorizationException ex) {
                userDataAdministrationTab.getParentApp().showWarning("Membership not cancelable! Cause: "
                        + NoteBuilder.unauthorizedContext());
                LOGGER.warn("Failed to cancel membership! Cause: '" + MsgBuilder.unauthorizedContext(), ex);
            }
        }
    }

    /**
     *
     * @param groupId
     * @param userId
     * @return
     */
    private boolean isLastGroupManager(GroupId groupId, UserId userId) {
        try {
            List<UserId> groupManagers = GroupServiceLocal.getSingleton().getGroupManagers(
                    groupId, 0, Integer.MAX_VALUE, userDataAdministrationTab.getAuthCtx(groupId));
            for (UserId managerId : groupManagers) {
                if (!userId.equals(managerId)) {
                    return false;
                }
            }
            return true;
        } catch (AuthorizationException ex) {
            userDataAdministrationTab.getParentApp().showWarning(NoteBuilder.unauthorizedContext());
            LOGGER.warn(MsgBuilder.unauthorizedContext(), ex);
            return false;
        }
    }

    /**
     *
     * @return
     */
    public Label getIntroductionLabel() {
        if (introductionLabel == null) {
            buildIntroductionLabel();
        }
        return introductionLabel;
    }

    /**
     *
     */
    private void buildIntroductionLabel() {
        String id = "introductionLabel";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        String label = "A mismatch between the user's maximum role and "
                + "at least one role the user has within a group was detected. "
                + "Member roles are supposed to be at least as high as the user's "
                + "maximum role. <p>In the following, you can choose between "
                + "different ways how to handle the detected mismatch: ";
        introductionLabel = new Label(label);
        introductionLabel.setId(id);
        introductionLabel.setImmediate(true);
        introductionLabel.setContentMode(ContentMode.HTML);
        introductionLabel.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }
    
    /**
     *
     * @param userSelection
     */
    private void updateMainPanel() {
        // Execute case-independent commands:
        // Unlock all components of right and left panel for update
        UIComponentTools.setLockedLayoutComponents(getRightPanelLayout(), false);
        UIComponentTools.setLockedLayoutComponents(getLeftPanelLayout(), false);
        // Update components
        reloadMembershipsTable();
        getMembershipsTable().select(null);
        reloadGroupSelector();
        // Execute case-dependent commands  
        UserDataTablePanel.UserDataEffictivity userEffectivity
                = userDataAdministrationTab.getUserDataTablePanel().validateSelectedUserData();
        switch (userEffectivity) {
            case NO:
            case VALID:
                break;
            case INVALID:
            case DISABLED_USER:
            case LOGGED_IN_USER:
                UIComponentTools.setLockedLayoutComponents(getLeftPanelLayout(), true);
                UIComponentTools.setLockedLayoutComponents(getRightPanelLayout(), true);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'MembershipsView'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update 'MembershipsView'. Cause: Undefined enum constant detectednamely '"
                        + userEffectivity.name() + "'.");
                break;
        }
        
        switch (userEffectivity) {
            case NO:
            case VALID:
                break;
            case INVALID:
                UIComponentTools.showWarning("WARNING", "Data of an invalid user are not editable.", 3);
                break;
            case DISABLED_USER:
                UIComponentTools.showWarning("WARNING", "Data of a disabled user are not editable.", 3);
                break;
            case LOGGED_IN_USER:
                UIComponentTools.showWarning("WARNING", "Data related to oneself are not editable.", 3);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'MembershipsView'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update 'MembershipsView'. Cause: Undefined enum constant detectednamely '"
                        + userEffectivity.name() + "'.");
                break;
        }
    }
    
    /**
     *
     */
    public void reloadMembershipsTable() {
        getMembershipsTable().removeAllItems();
        try {
            EntityManager em = PU.entityManager();
            User user = FindUtil.findUser(PU.entityManager(), userId);
            em.close();
            for (Membership membership : user.getMemberships()) {
                getMembershipsTable().addItem(membership);
            }
        } catch (EntityNotFoundException ex) {
            String object = "user '" + userId.getStringRepresentation() + "'";
            userDataAdministrationTab.getParentApp().showError("Membership-table not "
                    + "reloadable! Cause: " + NoteBuilder.notFound(object));
            LOGGER.error("Failed to reload '" + getMembershipsTable().getId() + "'. Cause: "
                    + MsgBuilder.notFound(object), ex);
        }
    }
    
    /**
     *
     */
    public void reloadGroupSelector() {
        getGroupSelector().removeValueChangeListener(this);
        getGroupSelector().removeAllItems();
        List<GroupId> preselectedGroupIds = new ArrayList<>();
        try {
            List<UserGroup> userGroupList = userDataAdministrationTab.getParentApp().getMetaDataManager()
                    .find(UserGroup.class);
            for (UserGroup userGroup : userGroupList) {
                getGroupSelector().addItem(userGroup.getGroupId());
                getGroupSelector().setItemCaption(userGroup.getGroupId(),
                        getGroupSelectorItemCaption(userGroup));
            }
            IAuthorizationContext authCtx = userDataAdministrationTab.getParentApp()
                    .getAuthorizationContext();
            preselectedGroupIds = GroupServiceLocal.getSingleton().membershipsOf(
                    userId, 0, Integer.MAX_VALUE, authCtx);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "information about registered user groups";
            userDataAdministrationTab.getParentApp().showWarning("Group-selector not reloadable! Cause: "
                    + NoteBuilder.unauthorizedGetRequest(object));
            LOGGER.warn("Failed to reload '" + getGroupSelector().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        } catch (AuthorizationException ex) {
            userDataAdministrationTab.getParentApp().showWarning("Group-selector not reloadable! Cause: "
                    + NoteBuilder.unauthorizedContext());
            LOGGER.warn("Failed to reload '" + getGroupSelector().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedContext(), ex);
        }
        Collection<String> preselectedGroups = new ArrayList<>();
        for (GroupId groupId : preselectedGroupIds) {
            preselectedGroups.add(groupId.getStringRepresentation());
        }
        getGroupSelector().setValue(preselectedGroups);
        getGroupSelector().addValueChangeListener(this);
        formerGroupSelection = preselectedGroups;
    }

    /**
     *
     * @param userGroup
     * @return
     */
    private String getGroupSelectorItemCaption(UserGroup userGroup) {
        return userGroup.getGroupName() + " (" + userGroup.getGroupId() + ")";
    }

    /**
     *
     * @return
     */
    public Collection<Membership> getSelectedMemberships() {
        return (Collection<Membership>) getMembershipsTable().getValue();
    }
}
