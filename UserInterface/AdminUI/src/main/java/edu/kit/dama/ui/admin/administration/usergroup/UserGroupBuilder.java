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

import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.ui.admin.exception.DBCommitException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UserGroupBuilder extends VerticalLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupBuilder.class);
    public final static String DEBUG_ID_PREFIX = UserGroupBuilder.class.getName() + "_";

    private final UserGroupAdministrationTab userGroupAdministrationTab;
    private PopupView view;
    private VerticalLayout viewLayout;
    private Button createUserGroupButton;
    private TextField groupIdField;
    private TextField groupNameField;
    private TextArea groupDescritionField;
    private Button commitButton;

    public UserGroupBuilder(final UserGroupAdministrationTab groupAdministrationTab) {
        this.userGroupAdministrationTab = groupAdministrationTab;

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));

        setImmediate(true);

        addComponent(getView());
        addComponent(getCreateUserGroupButton());

        setComponentAlignment(getView(), Alignment.MIDDLE_CENTER);
        setComponentAlignment(getCreateUserGroupButton(), Alignment.MIDDLE_CENTER);
    }

    /**
     *
     * @return
     */
    public final PopupView getView() {
        if (view == null) {
            buildView();
        }
        return view;
    }

    /**
     *
     */
    private void buildView() {
        String id = "view";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        view = new PopupView(null, getViewLayout());
        view.setId(DEBUG_ID_PREFIX + id);
        view.setImmediate(true);
        view.setHideOnMouseOut(false);

        view.addPopupVisibilityListener(new PopupView.PopupVisibilityListener() {

            @Override
            public void popupVisibilityChange(PopupView.PopupVisibilityEvent event) {
                if (!event.isPopupVisible()) {
                    clearComponents();
                }
            }
        });
    }

    /**
     *
     * @return
     */
    public VerticalLayout getViewLayout() {
        if (viewLayout == null) {
            buildViewLayout();
        }
        return viewLayout;
    }

    /**
     *
     */
    private void buildViewLayout() {
        String id = "viewLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        viewLayout = new VerticalLayout();
        viewLayout.setId(DEBUG_ID_PREFIX + id);
        viewLayout.setWidth("300px");
        viewLayout.setImmediate(true);
        viewLayout.setSpacing(true);
        viewLayout.setMargin(new MarginInfo(true, true, false, true));
        viewLayout.setCaption("NEW GROUP");
        viewLayout.addStyleName(CSSTokenContainer.GREY_CAPTION);

        viewLayout.addComponent(getGroupIdField());
        viewLayout.addComponent(getGroupNameField());
        viewLayout.addComponent(getGroupDescriptionField());
        viewLayout.addComponent(getCommitButton());

        viewLayout.setComponentAlignment(getGroupNameField(), Alignment.MIDDLE_LEFT);
        viewLayout.setComponentAlignment(getGroupDescriptionField(), Alignment.MIDDLE_LEFT);
        viewLayout.setComponentAlignment(getCommitButton(), Alignment.BOTTOM_RIGHT);
    }

    /**
     *
     * @return
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

        groupIdField = new TextField("GROUP ID");
        groupIdField.setId(DEBUG_ID_PREFIX + id);
        groupIdField.setWidth("55%");
        groupIdField.setImmediate(true);
        groupIdField.setRequired(true);
        groupIdField.setNullRepresentation("");
        groupIdField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     *
     * @return
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

        groupNameField = new TextField("GROUP NAME");
        groupNameField.setId(DEBUG_ID_PREFIX + id);
        groupNameField.setWidth("55%");
        groupNameField.setImmediate(true);
        groupNameField.setRequired(true);
        groupNameField.setNullRepresentation("");
        groupNameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     *
     * @return
     */
    public final TextArea getGroupDescriptionField() {
        if (groupDescritionField == null) {
            buildGroupDescriptionField();
        }
        return groupDescritionField;
    }

    /**
     *
     */
    private void buildGroupDescriptionField() {
        String id = "groupDescriptionField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        groupDescritionField = new TextArea("DESCRIPTION");
        groupDescritionField.setId(DEBUG_ID_PREFIX + id);
        groupDescritionField.setWidth("100%");
        groupDescritionField.setImmediate(true);
        groupDescritionField.setNullRepresentation("");
        groupDescritionField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     *
     * @return
     */
    public final Button getCommitButton() {
        if (commitButton == null) {
            buildCommitButton();
        }
        return commitButton;
    }

    /**
     *
     */
    private void buildCommitButton() {
        String id = "commitButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        commitButton = new Button("Commit");
        commitButton.setId(DEBUG_ID_PREFIX + id);
        commitButton.setImmediate(true);
        commitButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, new int[]{});

        commitButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                UserGroupTablePanel userGroupTablePanel
                        = userGroupAdministrationTab.getUserGroupTablePanel();
                try {
                    UserGroup newUserGroup = createUserGroup();
                    commitUserGroup(newUserGroup);
                    userGroupTablePanel.addTableEntry(newUserGroup);
                    getView().setPopupVisible(false);
                } catch (EmptyValueException ex) {
                    LOGGER.error("Failed to create a new user group. Cause: " + ex.getMessage(), ex);
                } catch (DBCommitException ex) {
                    LOGGER.error("Failed to commit a new user group to the database. Cause: " 
                            + ex.getMessage(), ex);
                }
            }
        });
    }

    /**
     * 
     * @return
     * @throws com.vaadin.data.Validator.EmptyValueException 
     */
    private UserGroup createUserGroup() throws EmptyValueException {
        try {
            validateRequiredComponents();
        } catch (EmptyValueException ex) {
            throw new EmptyValueException(ex.getMessage());
        }
        UserGroup newUserGroup = new UserGroup();
        newUserGroup.setGroupId((String) getGroupIdField().getValue());
        newUserGroup.setGroupName((String) getGroupNameField().getValue());
        newUserGroup.setDescription(getGroupDescriptionField().getValue());

        return newUserGroup;
    }

    /**
     * 
     * @param userGroup
     * @throws DBCommitException 
     */
    private void commitUserGroup(UserGroup userGroup) throws DBCommitException {
        AuthorizationContext sysCtx = new AuthorizationContext(
                new UserId(Constants.SYSTEM_ADMIN),
                new GroupId(Constants.SYSTEM_GROUP), Role.ADMINISTRATOR);
        UserId loggedInUserId = new UserId(userGroupAdministrationTab.getParentApp()
                .getLoggedInUser().getDistinguishedName());
        try {
            GroupId groupId = new GroupId(userGroup.getGroupId());
            GroupServiceLocal.getSingleton().create(groupId, loggedInUserId, sysCtx);
            userGroupAdministrationTab.getParentApp().getMetaDataManager().save(userGroup);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "user group '" + userGroup.getGroupId() + "'";
            userGroupAdministrationTab.getParentApp().showWarning("User group not modifiable! Cause: "
                    + NoteBuilder.unauthorizedCreateRequest(object));
            throw new DBCommitException(MsgBuilder.unauthorizedCreateRequest(object), ex);
        } catch (EntityAlreadyExistsException ex) {
            String object = "user group '" + userGroup.getGroupId() + "'";
            userGroupAdministrationTab.getParentApp().showWarning("User group not modifiable! Cause: "
                    + NoteBuilder.alreadyExists(object));
            throw new DBCommitException(MsgBuilder.alreadyExists(object), ex);
        } catch (EntityNotFoundException ex) {
            String object = "user group manager of '" + userGroup.getGroupId() + "'";
            userGroupAdministrationTab.getParentApp().showError("User group not modifiable! Cause: "
                    + NoteBuilder.notFound(object));
            throw new DBCommitException(MsgBuilder.notFound(object), ex);
        }
    }

    /**
     * 
     * @throws com.vaadin.data.Validator.EmptyValueException 
     */
    private void validateRequiredComponents() throws EmptyValueException {
        if (UIComponentTools.isEmpty(getGroupIdField())) {
            String object = "the required textfield '" + getGroupIdField().getCaption() + "'";
            userGroupAdministrationTab.getParentApp().showWarning(NoteBuilder.emptyValue(object));
            throw new EmptyValueException(getGroupIdField().getId());
        } else if (UIComponentTools.isEmpty(getGroupNameField())) {
            String object = "the required textfield '" + getGroupNameField().getCaption() + "'";
            userGroupAdministrationTab.getParentApp().showWarning(NoteBuilder.emptyValue(object));
            throw new EmptyValueException(getGroupNameField().getId());
        }
        UIUtils7.validate(getGroupIdField());
        UIUtils7.validate(getGroupNameField());
    }

    /**
     * @return
     */
    public final Button getCreateUserGroupButton() {
        if (createUserGroupButton == null) {
            buildCreateUserGroupButton();
        }
        return createUserGroupButton;
    }

    /**
     *
     */
    private void buildCreateUserGroupButton() {
        String id = "createUserGroupButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + "...");

        createUserGroupButton = new Button();
        createUserGroupButton.setId(DEBUG_ID_PREFIX + id);
        createUserGroupButton.setImmediate(true);
        createUserGroupButton.setIcon(new ThemeResource(IconContainer.GROUP_ADD));
        createUserGroupButton.setStyleName(BaseTheme.BUTTON_LINK);
        createUserGroupButton.setDescription("Create a new KIT Data Manager Group");
        createUserGroupButton.setWidth("14%");

        createUserGroupButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getView().setPopupVisible(true);
            }
        });
    }

    /**
     *
     */
    public void clearComponents() {
        getGroupIdField().setValue(null);
        getGroupNameField().setValue(null);
        getGroupDescriptionField().setValue(null);
    }
}