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
package edu.kit.dama.ui.admin.staging;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import static edu.kit.dama.ui.admin.administration.user.MembershipsView.LOGGER;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author dx6468
 */
public class AbstractSpecificPropertiesLayout extends VerticalLayout {

    private final List<TextField> propertiesFields;
    private final Label noPropertiesAvailableLabel;

    public AbstractSpecificPropertiesLayout() {
        this("");
    }

    public AbstractSpecificPropertiesLayout(String debugIdPrefix) {
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
        for (TextField propertyField : getPropertiesFields()) {
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
     * @return the propertiesFields
     */
    public List<TextField> getPropertiesFields() {
        return propertiesFields;
    }

    @Override
    public void removeAllComponents() {
        super.removeAllComponents();
        setEnabled(true);
        getPropertiesFields().clear();
    }
    
    public void reset() {
        removeAllComponents();
        addComponent(noPropertiesAvailableLabel);
        setComponentAlignment(noPropertiesAvailableLabel, Alignment.MIDDLE_CENTER);
    }
}
