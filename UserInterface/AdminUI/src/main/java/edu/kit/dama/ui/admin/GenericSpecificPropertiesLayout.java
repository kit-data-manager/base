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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.commons.types.IConfigurable;
import static edu.kit.dama.ui.admin.administration.user.MembershipsView.LOGGER;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author dx6468
 */
public class GenericSpecificPropertiesLayout extends VerticalLayout {

    private final List<TextField> propertiesFields;
    private final Label noPropertiesAvailableLabel;

    /**
     * Default constructor.
     */
    public GenericSpecificPropertiesLayout() {
        this("");
    }

    /**
     * Default constructor.
     *
     * @param debugIdPrefix The debug id prefix used to identify specific
     * instances of this class in debugging messages.
     */
    public GenericSpecificPropertiesLayout(String debugIdPrefix) {
        LOGGER.debug("Building " + debugIdPrefix + " ...");

        propertiesFields = new LinkedList<>();
        noPropertiesAvailableLabel = new Label("NO PROPERTIES AVAILABLE");
        noPropertiesAvailableLabel.setSizeUndefined();

        setId(debugIdPrefix);
        setWidth("100%");
        setImmediate(true);
        setMargin(true);
    }

    /**
     * Get the current properties from the UI. The method will use all fields
     * registered in 'propertiesFields', uses their debugId as property key and
     * their value as property value. Finally, a new properties object is
     * returned.
     * <p align="right"> <b>by</b> Jejkal
     * <p align="right"> <b>edited by</b> Rindone
     *
     * @return The properties object obtained from the UI.
     */
    public Properties getProperties() {
        LOGGER.debug("Obtaining properties from UI");
        Properties result = new Properties();
        for (TextField propertyField : propertiesFields) {
            String key = propertyField.getId();
            String value = (String) propertyField.getValue();
            if (key != null && value != null) {
                LOGGER.debug("Adding property key {} with value {}", new Object[]{key, value});
                result.put(key, value);
            } else {
                LOGGER.debug("Ignoring property {} with value {}", new Object[]{key, value});
            }
        }
        return result;
    }

    /**
     * A list of textfields reflecting all configurable properties.
     *
     * @return The properties fields.
     */
    public List<TextField> getPropertiesFields() {
        return propertiesFields;
    }

    /**
     * Fill the layout depending on the provided configurable and properties.
     * 
     * @param pConfigurable The configurable used to obtain all supported property keys and their descriptions.
     * @param pProperties A set of key-value pairs for the provided configurable.
     * 
     * @throws UIComponentUpdateException If pConfigurable is null.
     */
    public void updateComponents(IConfigurable pConfigurable, Properties pProperties) throws UIComponentUpdateException {
        if (pConfigurable == null) {
            reset();
            throw new UIComponentUpdateException("Invalid IConfigurable argument 'null'.");
        }

        //obtain all properties
        String[] keys = pConfigurable.getInternalPropertyKeys();
        if (keys == null || keys.length == 0) {
            reset();
            LOGGER.warn("Provided configurable has no specific properties.");
            return;
        }

        removeAllComponents();

        for (String key : keys) {
            TextField propertyField = UIUtils7.factoryTextField(key, "");
            if (pProperties != null) {
                //obtain and set the value
                String defaultValue = (String) pProperties.get(key);
                propertyField.setValue(defaultValue);
                propertyField.setId(key);
            }
            String description = pConfigurable.getInternalPropertyDescription(key);
            Label propertyDescription;
            if (description != null && description.length() > 1) {
                propertyDescription = new Label("Description:  "
                        + pConfigurable.getInternalPropertyDescription(key));
            } else {
                propertyDescription = new Label("No description available");
            }
            propertyDescription.setEnabled(false);
            propertyField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            addComponent(propertyField);
            addComponent(propertyDescription);
            propertiesFields.add(propertyField);
        }

    }

    @Override
    public void removeAllComponents() {
        super.removeAllComponents();
        setEnabled(true);
        getPropertiesFields().clear();
    }

    /**
     * Reset all fields and show the 'no properties available' label.
     */
    public void reset() {
        removeAllComponents();
        addComponent(noPropertiesAvailableLabel);
        setComponentAlignment(noPropertiesAvailableLabel, Alignment.MIDDLE_CENTER);
    }
}
