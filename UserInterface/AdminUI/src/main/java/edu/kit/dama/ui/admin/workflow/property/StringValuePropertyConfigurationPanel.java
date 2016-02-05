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
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.mdm.dataworkflow.properties.StringValueProperty;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.commons.util.UIUtils7;

/**
 *
 * @author jejkal
 */
public final class StringValuePropertyConfigurationPanel extends AbstractPropertyConfigurationPanel<StringValueProperty> {

    /**
     * The property value text field.
     */
    private TextField valueField;
    /**
     * The main layout.
     */
    private VerticalLayout mainLayout;

    /**
     * Default constructor.
     */
    public StringValuePropertyConfigurationPanel() {
    }

    /**
     * Get the property value text field. If it is not initialized this will be
     * done before returning it.
     *
     * @return The value text field.
     */
    private TextField getValueField() {
        if (valueField == null) {
            valueField = new TextField("PROPERTY VALUE");
            valueField.setWidth("100%");
            valueField.setImmediate(true);
            valueField.setRequired(true);
            valueField.setNullRepresentation("");
            valueField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return valueField;
    }

    @Override
    public void resetCustomComponents() {
        getValueField().setValue("");
    }

    @Override
    public boolean isValid() {
        return UIUtils7.validate(getNameField())
                && UIUtils7.validate(getValueField());
    }

    @Override
    public StringValueProperty getPropertyInstance() {
        StringValueProperty result = new StringValueProperty();
        result.setName(getNameField().getValue());
        result.setDescription(getDescriptionField().getValue());
        result.setPropertyValue(getValueField().getValue());
        return result;
    }

    @Override
    public AbstractLayout getLayout() {
        if (mainLayout == null) {
            mainLayout = new VerticalLayout();
            mainLayout.setSizeFull();
            mainLayout.setWidth("100%");
            mainLayout.setImmediate(true);
            mainLayout.setMargin(false);
            mainLayout.setSpacing(true);
            mainLayout.addComponent(getNameField());
            mainLayout.addComponent(getDescriptionField());
            mainLayout.addComponent(getValueField());
            mainLayout.setExpandRatio(getNameField(), .1f);
            mainLayout.setExpandRatio(getDescriptionField(), .8f);
            mainLayout.setExpandRatio(getValueField(), .1f);
        }
        return mainLayout;
    }
}
