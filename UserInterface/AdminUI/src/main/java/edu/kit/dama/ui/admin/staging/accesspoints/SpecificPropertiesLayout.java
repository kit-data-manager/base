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

import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import edu.kit.dama.ui.admin.staging.AbstractSpecificPropertiesLayout;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.io.IOException;
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
     * 
     * @param accessPoint
     * @param accessPointInstance
     * @throws IOException
     * @throws UIComponentUpdateException 
     */
    public void updateComponents(StagingAccessPointConfiguration accessPoint,
            AbstractStagingAccessPoint accessPointInstance) throws IOException, UIComponentUpdateException {

        if (accessPoint == null || accessPointInstance == null) {
            reset();
            throw new UIComponentUpdateException("Invalid access point.");
        }

        // Setup properties
        String[] keys = accessPointInstance.getInternalPropertyKeys();
        if (keys == null || keys.length == 0) {
            reset();
            LOGGER.warn("Access point has no specific properties.");
            return;
        }
        removeAllComponents();

        for (String key : keys) {
            TextField propertyField = UIUtils7.factoryTextField(key, "");
            if (accessPoint.getPropertiesAsObject() != null) {
                //obtain and set the value
                String defaultValue = (String) accessPoint.getPropertiesAsObject().get(key);
                propertyField.setValue(defaultValue);
                propertyField.setId(key);
            }
            String description = accessPointInstance.getInternalPropertyDescription(key);
            Label propertyDescription;
            if (description != null && description.length() > 1) {
                propertyDescription = new Label("Description: " 
                        + accessPointInstance.getInternalPropertyDescription(key));
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
