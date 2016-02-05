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

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class AddEnvironmentPropertyComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddEnvironmentPropertyComponent.class);

    private VerticalLayout mainLayout;
    private VerticalLayout propertyEditorLayout;
    private PopupView propertyPopup;
    private ComboBox propertyTypeSelectionBox;
    private AbstractPropertyConfigurationPanel currentPanel = null;

    enum ENVIRONMENT_PROPERTY_TYPE {

        STRING_VALUE_PROPERTY("StringValue"),
        SOFTWARE_MAP_PROPERTY("LinuxSoftwareMap");

        private final String name;

        private ENVIRONMENT_PROPERTY_TYPE(String pName) {
            name = pName;
        }

        public String getName() {
            return name;
        }

    }

    /**
     * Default constructor.
     */
    public AddEnvironmentPropertyComponent() {
        buildMainLayout();
    }

    /**
     * Build the main layout including the type selection combobox, the buttons
     * and the placeholder for the property configuration component.
     */
    private void buildMainLayout() {
        propertyEditorLayout = new VerticalLayout();
        propertyEditorLayout.setSizeFull();
        propertyEditorLayout.setMargin(false);
        propertyEditorLayout.setSpacing(true);
        propertyEditorLayout.setWidth("400px");

        propertyTypeSelectionBox = new ComboBox("PROPERTY TYPE");
        propertyTypeSelectionBox.setWidth("100%");
        propertyTypeSelectionBox.setImmediate(true);
        propertyTypeSelectionBox.setNullSelectionAllowed(false);
        propertyTypeSelectionBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);

        for (ENVIRONMENT_PROPERTY_TYPE type : ENVIRONMENT_PROPERTY_TYPE.values()) {
            propertyTypeSelectionBox.addItem(type.toString());
            propertyTypeSelectionBox.setItemCaption(type, type.getName());
        }

        propertyTypeSelectionBox.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updatePropertySelection(ENVIRONMENT_PROPERTY_TYPE.valueOf((String) propertyTypeSelectionBox.getValue()));
            }
        });

        final NativeButton createButton = new NativeButton("Create");
        final NativeButton cancelButton = new NativeButton("Cancel");

        Button.ClickListener listener = new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (createButton.equals(event.getButton())) {
                    createProperty();
                }
                propertyPopup.setPopupVisible(false);
            }
        };

        createButton.addClickListener(listener);
        cancelButton.addClickListener(listener);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, createButton);
        mainLayout = new VerticalLayout(propertyTypeSelectionBox, propertyEditorLayout, buttonLayout);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
        mainLayout.setExpandRatio(propertyTypeSelectionBox, .1f);
        mainLayout.setExpandRatio(propertyEditorLayout, .9f);
        mainLayout.setExpandRatio(buttonLayout, .1f);

        propertyTypeSelectionBox.setValue(ENVIRONMENT_PROPERTY_TYPE.STRING_VALUE_PROPERTY.toString());
    }

    /**
     * Update the property configuration component depending on the selected
     * type.
     *
     * @param pSelection The selected property type to create.
     */
    private void updatePropertySelection(ENVIRONMENT_PROPERTY_TYPE pSelection) {
        LOGGER.debug("Updating property selection component to type '{}'", pSelection);
        switch (pSelection) {
            case STRING_VALUE_PROPERTY:
                propertyEditorLayout.removeAllComponents();
                currentPanel = new StringValuePropertyConfigurationPanel();
                propertyEditorLayout.addComponent(currentPanel.getLayout());
                break;
            case SOFTWARE_MAP_PROPERTY:
                propertyEditorLayout.removeAllComponents();
                currentPanel = new LinuxSoftwareMapPropertyConfigurationPanel();
                propertyEditorLayout.addComponent(currentPanel.getLayout());
                break;
            default:
                propertyEditorLayout.removeAllComponents();
                currentPanel = null;
                Label errorLabel = new Label("Unsupported Property Type");
                propertyEditorLayout.addComponent(errorLabel);
                propertyEditorLayout.setComponentAlignment(errorLabel, Alignment.MIDDLE_CENTER);
        }
    }

    /**
     * Reset the current panel.
     */
    public void reset() {
        if (currentPanel != null) {
            currentPanel.reset();
        }
    }

    /**
     * Create and persist a new property based on the values of the currently
     * selected property configuration component.
     */
    private void createProperty() {
        LOGGER.debug("Persisting property.");
        if (currentPanel != null) {
            LOGGER.debug("Checking if property is valid.");
            if (currentPanel.isValid()) {
                LOGGER.debug("Property is valid, obtaining and committing new property.");
                ExecutionEnvironmentProperty property = currentPanel.getPropertyInstance();
                IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
                mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
                try {
                    mdm.save(property);
                } catch (UnauthorizedAccessAttemptException ex) {
                    //whatever
                    LOGGER.error("Not authorized to commit new properties.", ex);
                } finally {
                    mdm.close();
                }
            } else {
                //invalid
                LOGGER.warn("Property validation failed.");
            }
        }
    }

    /**
     * Get the popup view containing this component.
     *
     * @return The popup view.
     */
    public final PopupView getPopupView() {
        if (propertyPopup == null) {
            propertyPopup = new PopupView(null, mainLayout);
            propertyPopup.setHideOnMouseOut(false);
        }

        return propertyPopup;
    }
}
