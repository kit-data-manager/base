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
package edu.kit.dama.ui.admin.workflow.property;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;

/**
 *
 * @author jejkal
 * @param <C> The property type.
 */
public abstract class AbstractPropertyConfigurationPanel<C extends ExecutionEnvironmentProperty> {

    private TextField nameField;
    private TextArea descriptionField;

    /**
     * Default constructor.
     */
    public AbstractPropertyConfigurationPanel() {
    }

    /**
     * Get the name field.
     *
     * @return The name field.
     */
    public final TextField getNameField() {
        if (nameField == null) {
            nameField = new TextField("NAME");
            nameField.setWidth("100%");
            nameField.setImmediate(true);
            nameField.setRequired(true);
            nameField.setNullRepresentation("");
            nameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return nameField;
    }

    /**
     * Get the description area.
     *
     * @return The description area.
     */
    public final TextArea getDescriptionField() {
        if (descriptionField == null) {
            descriptionField = new TextArea("DESCRIPTION");
            descriptionField.setSizeFull();
            descriptionField.setRows(8);
            descriptionField.setImmediate(true);
            descriptionField.setNullRepresentation("");
            descriptionField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return descriptionField;
    }

    /**
     * Reset all fields provided by this base class and triggers
     * {@link #resetCustomComponents()}.
     */
    public final void reset() {
        nameField.setValue("");
        descriptionField.setValue("");
        resetCustomComponents();
    }

    /**
     * Reset all custom components added by implementations of this base class.
     */
    public abstract void resetCustomComponents();

    /**
     * Check whether the provide inputs for the property are valid.
     *
     * @return TRUE if the inputs are valid, FALSE otherwise.
     */
    public abstract boolean isValid();

    /**
     * Return the property instance.
     *
     * @return The property instance.
     */
    public abstract C getPropertyInstance();

    /**
     * Return the layout of this panel including all default and custom fields.
     *
     * @return The layout.
     */
    public abstract AbstractLayout getLayout();

}
