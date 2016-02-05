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
package edu.kit.dama.ui.admin.schedule.trigger;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Slider;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import edu.kit.dama.scheduler.api.trigger.JobTrigger;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.commons.util.UIUtils7;

/**
 *
 * @author jejkal
 * @param <C>
 */
public abstract class AbstractTriggerConfigurationPanel<C extends JobTrigger> {

    private TextField nameField;
    private TextField groupField;
    private Slider prioritySlider;
    private TextArea descriptionField;

    /**
     * Default constructor.
     */
    public AbstractTriggerConfigurationPanel() {
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
            nameField.setNullSettingAllowed(false);
            nameField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return nameField;
    }

    /**
     * Get the group field.
     *
     * @return The name field.
     */
    public final TextField getGroupField() {
        if (groupField == null) {
            groupField = new TextField("GROUP");
            groupField.setWidth("100%");
            groupField.setImmediate(true);
            groupField.setRequired(true);
            groupField.setNullRepresentation("");
            groupField.setNullSettingAllowed(false);
            groupField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return groupField;
    }

    /**
     * Get the priority slider.
     *
     * @return The priority slider.
     */
    public final Slider getPrioritySlider() {
        if (prioritySlider == null) {
            prioritySlider = new Slider("PRIORITY", 0, 10);
            prioritySlider.setImmediate(true);
            prioritySlider.setWidth("100%");
            prioritySlider.setValue(0.0);
            prioritySlider.setResolution(0);
            prioritySlider.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return prioritySlider;
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
            descriptionField.setNullSettingAllowed(true);
            descriptionField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return descriptionField;
    }

    /**
     * Check whether the default properties (name and group) of the trigger are
     * valid.
     *
     * @return TRUE if the inputs are valid, FALSE otherwise.
     */
    public final boolean isValid() {
        return UIUtils7.validate(nameField) && UIUtils7.validate(groupField) && customComponentsValid();
    }

    /**
     * Reset all fields provided by this base class and triggers
     * {@link #resetCustomComponents()}.
     */
    public final void reset() {
        nameField.setValue("");
        groupField.setValue("");
        descriptionField.setValue("");
        prioritySlider.setValue(0.0);
        resetCustomComponents();
    }

    /**
     * Reset all custom components added by implementations of this base class.
     */
    public abstract void resetCustomComponents();

    /**
     * Check whether the custom properties of the trigger are valid.
     *
     * @return TRUE if the inputs are valid, FALSE otherwise.
     */
    public abstract boolean customComponentsValid();

    /**
     * Return the trigger instance.
     *
     * @return The property instance.
     */
    public abstract C getTriggerInstance();

    /**
     * Return the layout of this panel including all default and custom fields.
     *
     * @return The layout.
     */
    public abstract AbstractLayout getLayout();

}
