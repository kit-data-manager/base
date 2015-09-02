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
package edu.kit.dama.ui.admin.staging.processors;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.ui.admin.AdminUIMainView;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.staging.entities.StagingProcessor;
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
    private ComboBox processorTypeBox;
    private VerticalLayout checkBoxesLayout;
    private CheckBox defaultBox;
    private CheckBox disabledBox;
    private TextArea descriptionArea;

    public BasicPropertiesLayout(AdminUIMainView parentApp) {
        this.parentApp = parentApp;

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setWidth("100%");
        setImmediate(true);
        setMargin(true);
        setSpacing(true);

        setColumns(3);
        setRows(4);

        addComponent(getNameField(), 0, 0, 2, 0);
        addComponent(getGroupBox(), 0, 1);
        addComponent(getProcessorTypeBox(), 0, 2);
        addComponent(getCheckBoxesLayout(), 2, 1, 2, 2);
        addComponent(getDescriptionArea(), 0, 3, 2, 3);

        setColumnExpandRatio(0, 0.7f);
        setColumnExpandRatio(1, 0.01f);
        setColumnExpandRatio(2, 0.29f);
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

        nameField = new TextField("PROCESSOR NAME");
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
     * @return the processorTypeBox
     */
    public final ComboBox getProcessorTypeBox() {
        if (processorTypeBox == null) {
            buildProcessorTypeBox();
        }
        return processorTypeBox;
    }

    /**
     *
     */
    private void buildProcessorTypeBox() {
        String id = "processorTypeLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        processorTypeBox = new ComboBox("PROCESSOR TYPE");
        processorTypeBox.setId(DEBUG_ID_PREFIX + id);
        processorTypeBox.setWidth("100%");
        processorTypeBox.setImmediate(true);
        processorTypeBox.setNullSelectionAllowed(false);
        processorTypeBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        processorTypeBox.addItem(StagingProcessor.PROCESSOR_TYPE.CLIENT_AND_SERVER_SIDE);
        processorTypeBox.addItem(StagingProcessor.PROCESSOR_TYPE.CLIENT_SIDE_ONLY);
        processorTypeBox.addItem(StagingProcessor.PROCESSOR_TYPE.SERVER_SIDE_ONLY);
        processorTypeBox.addItem(StagingProcessor.PROCESSOR_TYPE.POST_ARCHIVING);
        processorTypeBox.setNullSelectionItemId(StagingProcessor.PROCESSOR_TYPE.CLIENT_AND_SERVER_SIDE);
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
        checkBoxesLayout.setCaption("PROCESSOR OPTIONS");
        checkBoxesLayout.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        checkBoxesLayout.setImmediate(true);
        checkBoxesLayout.setSpacing(true);

        checkBoxesLayout.addComponent(getDefaultBox());
        checkBoxesLayout.addComponent(getDisabledBox());
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
        defaultBox.setDescription("Set this processor as default processor.");
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
        disabledBox.setDescription("Disable this processor.");
        disabledBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
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
     * @param processor
     * @throws UIComponentUpdateException
     */
    public void updateComponents(StagingProcessor processor) throws UIComponentUpdateException {
        reset();

        if (processor == null) {
            throw new UIComponentUpdateException("Invalid processor.");
        }
        if (processor.getGroupId() == null) {
            getGroupBox().select(USERS_GROUP_ID);
        } else {
            getGroupBox().select(processor.getGroupId());
        }
        if (processor.getGroupId() == null) {
            getProcessorTypeBox().setValue(StagingProcessor.PROCESSOR_TYPE.CLIENT_AND_SERVER_SIDE);
        } else {
            getProcessorTypeBox().setValue(processor.getType());
        }
        getNameField().setValue(processor.getName());
        getDefaultBox().setValue(processor.isDefaultOn());
        getDisabledBox().setValue(processor.isDisabled());
        getDescriptionArea().setValue(processor.getDescription());
    }

    /**
     *
     */
    public void reset() {
        setEnabled(true);
        getGroupBox().select(USERS_GROUP_ID);
        getProcessorTypeBox().select(StagingProcessor.PROCESSOR_TYPE.CLIENT_AND_SERVER_SIDE);
        getNameField().setValue(null);
        getDefaultBox().setValue(false);
        getDisabledBox().setValue(false);
        getDescriptionArea().setValue(null);
    }

    /**
     *
     */
    public final void reloadGroupBox() {
        getGroupBox().removeAllItems();

        try {
            List<UserGroup> userGroups = parentApp.getMetaDataManager().find(UserGroup.class);

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
