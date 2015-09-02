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
package edu.kit.dama.ui.admin.staging.accesspoints;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.ui.admin.AdminUIMainView;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IPathSelector;
import edu.kit.dama.ui.admin.utils.PathSelector;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import static edu.kit.dama.util.Constants.USERS_GROUP_ID;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class BasicPropertiesLayout extends GridLayout {

    public static final Logger LOGGER
            = LoggerFactory.getLogger(BasicPropertiesLayout.class);
    private static final String DEBUG_ID_PREFIX
            = BasicPropertiesLayout.class.getName() + "_";

    private final AdminUIMainView parentApp;

    private TextField nameField;
    private ComboBox groupBox;
    private TextField remoteBaseUrlField;
    private TextField localBasePathField;
    private NativeButton pathSelectorButton;
    private VerticalLayout checkBoxesLayout;
    private CheckBox defaultBox;
    private CheckBox disabledBox;
    private CheckBox transientBox;
    private TextArea descriptionArea;

    public BasicPropertiesLayout(AdminUIMainView parentApp) {
        this.parentApp = parentApp;

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setWidth("100%");
        setImmediate(true);
        setMargin(true);
        setSpacing(true);

        setColumns(4);
        setRows(4);

        addComponent(getNameField(), 0, 0, 1, 0);
        addComponent(getGroupBox(), 3, 0);
        addComponent(getRemoteBaseUrlField(), 0, 1, 1, 1);
        addComponent(getLocalBasePathField(), 0, 2);
        addComponent(getPathSelectorButton(), 1, 2);
        addComponent(getCheckBoxesLayout(), 3, 1, 3, 2);
        addComponent(getDescriptionArea(), 0, 3, 3, 3);

        setComponentAlignment(getPathSelectorButton(), Alignment.BOTTOM_RIGHT);

        setColumnExpandRatio(0, 0.69f);
        setColumnExpandRatio(1, 0.01f);
        setColumnExpandRatio(2, 0.01f);
        setColumnExpandRatio(3, 0.19f);
    }

    /**
     * @return the nameField
     */
    public final TextField getNameField() {
        if (nameField == null) {
            buildNameField();
        }
        return nameField;
    }

    /**
     *
     */
    private void buildNameField() {
        String id = "nameField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        nameField = new TextField("ACCESS POINT NAME");
        nameField.setId(DEBUG_ID_PREFIX + id);
        nameField.setWidth("100%");
        nameField.setImmediate(true);
        nameField.setRequired(true);
        nameField.setNullRepresentation("");
        nameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the groupBox
     */
    public final ComboBox getGroupBox() {
        if (groupBox == null) {
            buildGroupBox();
        }
        return groupBox;
    }

    /**
     *
     */
    private void buildGroupBox() {
        String id = "groupBox";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        groupBox = new ComboBox("ACCESSIBLE BY");
        groupBox.setId(DEBUG_ID_PREFIX + id);
        groupBox.setWidth("100%");
        groupBox.setImmediate(true);
        groupBox.setNullSelectionAllowed(false);
        groupBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return
     */
    public final TextField getRemoteBaseUrlField() {
        if (remoteBaseUrlField == null) {
            buildRemoteBaseUrlField();
        }
        return remoteBaseUrlField;
    }

    /**
     *
     */
    private void buildRemoteBaseUrlField() {
        String id = "remoteBaseUrlField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        remoteBaseUrlField = new TextField("REMOTE BASE URL");
        remoteBaseUrlField.setId(DEBUG_ID_PREFIX + id);
        remoteBaseUrlField.setWidth("100%");
        remoteBaseUrlField.setImmediate(true);
        remoteBaseUrlField.setNullRepresentation("");
        remoteBaseUrlField.setRequired(true);
        remoteBaseUrlField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return
     */
    public final TextField getLocalBasePathField() {
        if (localBasePathField == null) {
            buildLocalBasePathField();
        }
        return localBasePathField;
    }

    /**
     *
     */
    private void buildLocalBasePathField() {
        String id = "localBasePathField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        localBasePathField = new TextField("LOCAL BASE PATH");
        localBasePathField.setId(DEBUG_ID_PREFIX + id);
        localBasePathField.setWidth("100%");
        localBasePathField.setImmediate(true);
        localBasePathField.setNullRepresentation("");
        localBasePathField.setRequired(true);
        localBasePathField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return
     */
    public final NativeButton getPathSelectorButton() {
        if (pathSelectorButton == null) {
            buildPathSelectorButton();
        }
        return pathSelectorButton;
    }

    /**
     *
     */
    private void buildPathSelectorButton() {
        String id = "pathSelectorButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        pathSelectorButton = new NativeButton("Select Path");
        pathSelectorButton.setId(DEBUG_ID_PREFIX + id);
        pathSelectorButton.setImmediate(true);

        pathSelectorButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                PathSelector pathSelector = new PathSelector(new IPathSelector() {

                    @Override
                    public void firePathSelectorCloseEvent(String selectedPath) {
                        getLocalBasePathField().setValue(selectedPath);
                    }
                });
                UI.getCurrent().addWindow(pathSelector);
            }
        });
    }

    /**
     * @return
     */
    public final VerticalLayout getCheckBoxesLayout() {
        if (checkBoxesLayout == null) {
            buildCheckBoxesLayout();
        }
        return checkBoxesLayout;
    }

    /**
     *
     */
    private void buildCheckBoxesLayout() {
        String id = "checkBoxesLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        checkBoxesLayout = new VerticalLayout();
        checkBoxesLayout.setId(DEBUG_ID_PREFIX + id);
        checkBoxesLayout.setCaption("ACCESS POINT OPTIONS");
        checkBoxesLayout.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        checkBoxesLayout.setImmediate(true);
        checkBoxesLayout.setSpacing(true);

        checkBoxesLayout.addComponent(getDefaultBox());
        checkBoxesLayout.addComponent(getDisabledBox());
        checkBoxesLayout.addComponent(getTransientBox());
    }

    /**
     * @return the defaultBox
     */
    public final CheckBox getDefaultBox() {
        if (defaultBox == null) {
            buildDefaultBox();
        }
        return defaultBox;
    }

    /**
     *
     */
    private void buildDefaultBox() {
        String id = "defaultBox";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        defaultBox = new CheckBox("Default");
        defaultBox.setId(DEBUG_ID_PREFIX + id);
        defaultBox.setImmediate(true);
        defaultBox.setDescription("Set this provider as default provider.");
        defaultBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the disabledBox
     */
    public final CheckBox getDisabledBox() {
        if (disabledBox == null) {
            buildDisabledBox();
        }
        return disabledBox;
    }

    /**
     *
     */
    private void buildDisabledBox() {
        String id = "disabledBox";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        disabledBox = new CheckBox("Disabled");
        disabledBox.setId(DEBUG_ID_PREFIX + id);
        disabledBox.setImmediate(true);
        disabledBox.setDescription("Disable this provider.");
        disabledBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     * @return the transientBox
     */
    public final CheckBox getTransientBox() {
        if (transientBox == null) {
            buildTransientBox();
        }
        return transientBox;
    }

    /**
     *
     */
    private void buildTransientBox() {
        String id = "transientBox";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        transientBox = new CheckBox("Transient");
        transientBox.setId(DEBUG_ID_PREFIX + id);
        transientBox.setImmediate(true);
        transientBox.setDescription("Set this access point as transient access point; "
                + "that means, the local folder of this access point will be re-created on each start");
        transientBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
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

        descriptionArea = new TextArea("DESCRIPTION");
        descriptionArea.setId(DEBUG_ID_PREFIX + id);
        descriptionArea.setSizeFull();
        descriptionArea.setImmediate(true);
        descriptionArea.setNullRepresentation("");
        descriptionArea.addStyleName(CSSTokenContainer.BOLD_CAPTION);
    }

    /**
     *
     * @param accessPoint
     * @throws UIComponentUpdateException
     */
    public void updateComponents(StagingAccessPointConfiguration accessPoint)
            throws UIComponentUpdateException {
        reset();

        if (accessPoint == null) {
            throw new UIComponentUpdateException("Invalid access point.");
        }

        getNameField().setValue(accessPoint.getName());
        if (accessPoint.getGroupId() == null) {
            getGroupBox().select(USERS_GROUP_ID);
        } else {
            getGroupBox().select(accessPoint.getGroupId());
        }
        getRemoteBaseUrlField().setValue(accessPoint.getRemoteBaseUrl());
        getLocalBasePathField().setValue(accessPoint.getLocalBasePath());
        getDefaultBox().setValue(accessPoint.isDefaultAccessPoint());
        getDisabledBox().setValue(accessPoint.isDisabled());
        getTransientBox().setValue(accessPoint.isTransientAccessPoint());
        getDescriptionArea().setValue(accessPoint.getDescription());
    }

    /**
     *
     */
    public void reset() {
        setEnabled(true);
        getNameField().setValue(null);
        getGroupBox().select(USERS_GROUP_ID);
        getRemoteBaseUrlField().setValue(null);
        getLocalBasePathField().setValue(null);
        getDefaultBox().setValue(false);
        getDisabledBox().setValue(false);
        getTransientBox().setValue(false);
        getDescriptionArea().setValue(null);
    }

    /**
     * 
     */
    public final void reloadGroupBox() {
        getGroupBox().removeAllItems();

        try {
            List<UserGroup>  userGroups = parentApp.getMetaDataManager().find(UserGroup.class);

            for (UserGroup userGroup : userGroups) {
                getGroupBox().addItem(userGroup.getGroupId());
                getGroupBox().setItemCaption(userGroup.getGroupId(), getGroupBoxItemCaption(userGroup));
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "all registered user groups";
            parentApp.showWarning("Group-box not reloadable! Cause: " 
                    + NoteBuilder.unauthorizedGetRequest(object));
            LOGGER.warn("Failed to reload '" + getGroupBox().getId() + "'. Cause: " + 
                    MsgBuilder.unauthorizedGetRequest(object), ex);
        }
    }
    
    /**
     *
     * @param userGroup
     * @return
     */
    private String getGroupBoxItemCaption(UserGroup userGroup) {
        return userGroup.getGroupName() + " (" + userGroup.getId() + ")";
    }
}
