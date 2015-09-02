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

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import edu.kit.dama.ui.admin.staging.AbstractSpecificPropertiesLayout;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class SpecificPropertiesLayout extends AbstractSpecificPropertiesLayout {

    public static final Logger LOGGER
            = LoggerFactory.getLogger(SpecificPropertiesLayout.class);
    private static final String DEBUG_ID_PREFIX
            = SpecificPropertiesLayout.class.getName() + "_";

    public SpecificPropertiesLayout() {
        super(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
    }

    /**
     * Obtain valid processor properties and put the into the UI. If we are
     * using an existing staging processor, the properties should be already in
     * the database and they can be procided by 'pValues'. The contained
     * properties are then set as values of the according text field.
     *
     * @param processorInstance
     * @param properties
     * @throws edu.kit.dama.ui.admin.exception.UIComponentUpdateException
     */
    public void updateComponents(AbstractStagingProcessor processorInstance,
            Properties properties) throws UIComponentUpdateException {

        if (processorInstance == null) {
            reset();
            throw new UIComponentUpdateException("Invalid processor.");
        }

        //obtain all properties
        String[] keys = processorInstance.getInternalPropertyKeys();
        if (keys == null || keys.length == 0) {
            reset();
            LOGGER.warn("Processor has no specific properties.");
            return;
        }
        
        removeAllComponents();

        for (String key : keys) {
            TextField propertyField = UIUtils7.factoryTextField(key, "");
            if (properties != null) {
                //obtain and set the value
                String defaultValue = (String) properties.get(key);
                propertyField.setValue(defaultValue);
                propertyField.setId(key);
            }
            String description = processorInstance.getInternalPropertyDescription(key);
            Label propertyDescription;
            if (description != null && description.length() > 1) {
                propertyDescription = new Label("Description:  " 
                        + processorInstance.getInternalPropertyDescription(key));
            } else {
                propertyDescription = new Label("No description available");
            }
            propertyDescription.setEnabled(false);
            propertyField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            addComponent(propertyField);
            addComponent(propertyDescription);
            getPropertiesFields().add(propertyField);
        }
    }
}
