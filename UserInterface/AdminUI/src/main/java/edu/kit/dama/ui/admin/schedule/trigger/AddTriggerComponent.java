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

import com.vaadin.data.Property;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.scheduler.api.trigger.JobTrigger;
import edu.kit.dama.ui.admin.schedule.SchedulerBasePropertiesLayout;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class AddTriggerComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddTriggerComponent.class);

    private VerticalLayout mainLayout;
    private VerticalLayout triggerEditorLayout;
    private PopupView triggerPopup;
    private ComboBox triggerTypeSelectionBox;
    private AbstractTriggerConfigurationPanel currentPanel = null;
    private final SchedulerBasePropertiesLayout parent;

    enum TRIGGER_TYPE {

        NOW_TRIGGER("'Now' Trigger"),
        AT_TRIGGER("'At' Trigger"),
        EXPRESSION_TRIGGER("'Expression' Trigger"),
        INTERVAL_TRIGGER("'Interval' Trigger");

        private final String name;

        private TRIGGER_TYPE(String pName) {
            name = pName;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Default constructor.
     *
     * @param pParent The parent component for callback.
     */
    public AddTriggerComponent(SchedulerBasePropertiesLayout pParent) {
        parent = pParent;
        buildMainLayout();
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
     * Get the popup view containing this component.
     *
     * @return The popup view.
     */
    public PopupView getPopupView() {
        if (triggerPopup == null) {
            triggerPopup = new PopupView(null, mainLayout);
            //add 'toback' style putting the popup to z-index 10.000 (from 20.000) allowing to show tooltips also located at z-index 20.000
            triggerPopup.addStyleName("toback");
            triggerPopup.setHideOnMouseOut(false);
        }

        return triggerPopup;
    }

    /**
     * Build the main layout including the type selection combobox, the buttons
     * and the placeholder for the property configuration component.
     */
    private void buildMainLayout() {
        triggerEditorLayout = new VerticalLayout();
        triggerEditorLayout.setSizeFull();
        triggerEditorLayout.setMargin(false);
        triggerEditorLayout.setSpacing(true);
        triggerEditorLayout.setWidth("400px");

        triggerTypeSelectionBox = new ComboBox("TRIGGER TYPE");
        triggerTypeSelectionBox.setWidth("100%");
        triggerTypeSelectionBox.setImmediate(true);
        triggerTypeSelectionBox.setNullSelectionAllowed(false);
        triggerTypeSelectionBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);

        for (TRIGGER_TYPE type : TRIGGER_TYPE.values()) {
            triggerTypeSelectionBox.addItem(type.toString());
            triggerTypeSelectionBox.setItemCaption(type.toString(), type.getName());
        }

        triggerTypeSelectionBox.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                updateTriggerSelection(TRIGGER_TYPE.valueOf((String) triggerTypeSelectionBox.getValue()));
            }
        });
        final NativeButton createButton = new NativeButton("Create");
        final NativeButton cancelButton = new NativeButton("Cancel");

        Button.ClickListener listener = new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (cancelButton.equals(event.getButton()) || (createButton.equals(event.getButton()) && createTrigger())) {
                    //hide popup if dialog was canceled or if createTrigger succeeded (and 'create' was pressed) 
                    triggerPopup.setPopupVisible(false);
                } //otherwise, createTrigger failed 
            }
        };

        createButton.addClickListener(listener);
        cancelButton.addClickListener(listener);

        HorizontalLayout buttonLayout = new HorizontalLayout(cancelButton, createButton);
        mainLayout = new VerticalLayout(triggerTypeSelectionBox, triggerEditorLayout, buttonLayout);
        mainLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
        mainLayout.setExpandRatio(triggerTypeSelectionBox, .1f);
        mainLayout.setExpandRatio(triggerEditorLayout, .9f);
        mainLayout.setExpandRatio(buttonLayout, .1f);

        triggerTypeSelectionBox.setValue(TRIGGER_TYPE.NOW_TRIGGER.toString());
    }

    /**
     * Update the property configuration component depending on the selected
     * type.
     *
     * @param pSelection The selected property type to create.
     */
    private void updateTriggerSelection(TRIGGER_TYPE pSelection) {
        LOGGER.debug("Updating trigger selection component to type '{}'", pSelection);
        triggerEditorLayout.removeAllComponents();
        Component componentToAdd;
        switch (pSelection) {
            case NOW_TRIGGER:
                currentPanel = new NowTriggerConfigurationPanel();
                componentToAdd = currentPanel.getLayout();
                break;
            case AT_TRIGGER:
                currentPanel = new AtTriggerConfigurationPanel();
                componentToAdd = currentPanel.getLayout();
                break;
            case EXPRESSION_TRIGGER:
                currentPanel = new ExpressionTriggerConfigurationPanel();
                componentToAdd = currentPanel.getLayout();
                break;
            case INTERVAL_TRIGGER:
                currentPanel = new IntervalTriggerConfigurationPanel();
                componentToAdd = currentPanel.getLayout();
                break;
            default:
                currentPanel = null;
                componentToAdd = new Label("Unsupported Trigger Type");
        }

        triggerEditorLayout.addComponent(componentToAdd);
        triggerEditorLayout.setComponentAlignment(componentToAdd, Alignment.MIDDLE_CENTER);
    }

    /**
     * Create and persist a new property based on the values of the currently
     * selected property configuration component.
     */
    private boolean createTrigger() {
        LOGGER.debug("Persisting trigger.");
        boolean result = false;
        if (currentPanel != null) {
            LOGGER.debug("Checking if trigger is valid.");
            if (currentPanel.isValid()) {
                LOGGER.debug("Property is valid, obtaining and committing new property.");
                JobTrigger trigger = currentPanel.getTriggerInstance();
                parent.fireAddTriggerEvent(trigger);
                result = true;
            } else {
                //invalid
                LOGGER.warn("Trigger validation failed.");
            }
        }
        return result;
    }

}
