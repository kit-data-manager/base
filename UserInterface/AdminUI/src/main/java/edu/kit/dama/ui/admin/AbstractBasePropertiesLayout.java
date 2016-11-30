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
package edu.kit.dama.ui.admin;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.admin.utils.UIHelper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public abstract class AbstractBasePropertiesLayout<C> extends GridLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBasePropertiesLayout.class);
    private static final String DEBUG_ID_PREFIX = AbstractBasePropertiesLayout.class.getName() + "_";
    private ComboBox groupBox;
    private TextArea descriptionArea;
    private CheckBox disabledBox;
    private TextField nameField;
    private VerticalLayout checkBoxesLayout;
    private CheckBox defaultBox;

    /**
     * Get the text field holding the name of the managed entity.
     *
     * @return The name field.
     */
    public final TextField getNameField() {
        if (nameField == null) {
            nameField = factoryTextField(getNameFieldLabel(), "nameField", true);
        }
        return nameField;
    }

    /**
     * Factory a standard text field that with the default look and behavior.
     *
     * @param pLabel The field label.
     * @param pDebugId The debug id used in debug log messages and in the UI.
     * The debug id should be unique on the entire resulting web page.
     * @param pRequired Set the field to be required.
     *
     * @return The text field.
     */
    public final TextField factoryTextField(String pLabel, String pDebugId, boolean pRequired) {
        if (pDebugId != null) {
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + pDebugId + " ...");
        }
        TextField result = new TextField(pLabel);
        if (pDebugId != null) {
            result.setId(DEBUG_ID_PREFIX + pDebugId);
        }
        result.setWidth("100%");
        result.setImmediate(true);
        result.setRequired(pRequired);
        result.setNullRepresentation("");
        result.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        return result;
    }

    /**
     * Get the group combobox allowing to select by which group the element is
     * available.
     *
     * @return The groupbox.
     */
    public final ComboBox getGroupBox() {
        if (groupBox == null) {
            String id = "groupBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            groupBox = new ComboBox("ACCESSIBLE BY");
            groupBox.setId(DEBUG_ID_PREFIX + id);
            groupBox.setWidth("100%");
            groupBox.setImmediate(true);
            groupBox.setNullSelectionAllowed(false);
            groupBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return groupBox;
    }

    /**
     * Reload the group box from the database.
     */
    public final void reloadGroupBox() {
        getGroupBox().removeAllItems();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());
        try {
            List<UserGroup> userGroups = mdm.find(UserGroup.class);
            userGroups.stream().map((userGroup) -> {
                getGroupBox().addItem(userGroup.getGroupId());
                return userGroup;
            }).forEachOrdered((userGroup) -> {
                getGroupBox().setItemCaption(userGroup.getGroupId(), userGroup.getGroupName());
            });
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "all registered user groups";
            UIComponentTools.showWarning("Group-box not reloadable! Cause: "
                    + NoteBuilder.unauthorizedGetRequest(object));
            LOGGER.warn("Failed to reload '" + getGroupBox().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        } finally {
            mdm.close();
        }
    }

    /**
     * Returns the TextArea holding the description of the element.
     *
     * @return The descriptionArea.
     */
    public final TextArea getDescriptionArea() {
        if (descriptionArea == null) {
            String id = "descriptionArea";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            descriptionArea = new TextArea("DESCRIPTION");
            descriptionArea.setId(DEBUG_ID_PREFIX + id);
            descriptionArea.setSizeFull();
            descriptionArea.setRows(8);
            descriptionArea.setImmediate(true);
            descriptionArea.setNullRepresentation("");
            descriptionArea.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return descriptionArea;
    }

    /**
     * Returns the vertical layout holding checkboxed for setting/unsetting the
     * 'default' and 'disabled' flags for an element. Checkboxed for additional
     * flags might be added by implementations extending this abstract class.
     *
     * @return The vertical layout.
     */
    public final VerticalLayout getCheckBoxesLayout() {
        if (checkBoxesLayout == null) {
            String id = "checkBoxesLayout";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            checkBoxesLayout = new VerticalLayout();
            checkBoxesLayout.setId(DEBUG_ID_PREFIX + id);
            checkBoxesLayout.setCaption("GENERAL OPTIONS");
            checkBoxesLayout.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            checkBoxesLayout.setImmediate(true);
            checkBoxesLayout.setSpacing(true);

            checkBoxesLayout.addComponent(getDefaultBox());
            checkBoxesLayout.addComponent(getDisabledBox());
        }
        return checkBoxesLayout;
    }

    /**
     * Returns the checkbox for setting the 'default' flag of an element.
     *
     * @return The 'default' box
     */
    public final CheckBox getDefaultBox() {
        if (defaultBox == null) {
            String id = "defaultBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            defaultBox = new CheckBox("Default");
            defaultBox.setId(DEBUG_ID_PREFIX + id);
            defaultBox.setImmediate(true);
            defaultBox.setDescription("Set this processor as default processor.");
            defaultBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            defaultBox.addStyleName("yesno");
        }
        return defaultBox;
    }

    /**
     * Returns the checkbox for setting the 'disabled' flag of an element.
     *
     * @return The 'disabled' box
     */
    public final CheckBox getDisabledBox() {
        if (disabledBox == null) {
            String id = "disabledBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            disabledBox = new CheckBox("Disabled");
            disabledBox.setId(DEBUG_ID_PREFIX + id);
            disabledBox.setImmediate(true);
            disabledBox.setDescription("Disable this element from being used.");
            disabledBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            disabledBox.addStyleName("yesno");
        }
        return disabledBox;
    }

    /**
     * Returns the label of the 'name' field of the element. The value might be
     * element specific, e.g. '&lt;Element&gt; Name', or generic, e.g. 'Name'.
     *
     * @return The name field label.
     */
    public abstract String getNameFieldLabel();

    /**
     * Reset the UI by removing all entered entries. Each implementation has to
     * reset all generic elements, e.g. name field, group combobox, 'disabled'
     * and 'default' checkbox, as well as all custom UI elements provided by the
     * concrete implementation.
     */
    public abstract void reset();

    /**
     * Update the fields of the UI using the provided element entity. Each
     * implementation has to set fill all generic elements, e.g. name field,
     * group combobox, 'disabled' and 'default' checkbox, as well as all custom
     * UI elements provided by the concrete implementation.
     *
     *
     * @param pValue The element entity.
     *
     * @throws UIComponentUpdateException If the update fails for some reason.
     */
    public abstract void updateSelection(C pValue) throws UIComponentUpdateException;
}
