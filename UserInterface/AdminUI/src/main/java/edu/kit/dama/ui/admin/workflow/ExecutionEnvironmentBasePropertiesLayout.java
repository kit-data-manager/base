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
package edu.kit.dama.ui.admin.workflow;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.UI;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IPathSelector;
import edu.kit.dama.ui.admin.utils.PathSelector;
import edu.kit.dama.ui.admin.AbstractBasePropertiesLayout;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.workflow.property.AddEnvironmentPropertyComponent;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public final class ExecutionEnvironmentBasePropertiesLayout extends AbstractBasePropertiesLayout<ExecutionEnvironmentConfiguration> implements IEnvironmentPropertyCreationListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExecutionEnvironmentBasePropertiesLayout.class);
    private static final String DEBUG_ID_PREFIX = ExecutionEnvironmentBasePropertiesLayout.class.getName() + "_";

    private TextField maxTasksField;
    private ComboBox accessPointBox;
    private TextField accessPointBasePathField;
    private Button pathSelectorButton;
    private TwinColSelect environmentProperties;
    private AddEnvironmentPropertyComponent addPropertyComponent;

    /**
     * Default constructor.
     */
    public ExecutionEnvironmentBasePropertiesLayout() {
        super();

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        setColumns(4);
        setRows(6);

        addComponent(getNameField(), 0, 0, 2, 0);
        addComponent(getGroupBox(), 3, 0);
        //
        addComponent(getAccessPointBox(), 0, 1, 2, 1);
        addComponent(getCheckBoxesLayout(), 3, 1);
        //

        addComponent(getAccessPointBasePathField(), 0, 2, 2, 2);
        addComponent(getPathSelectorButton(), 3, 2);
        //
        addComponent(getMaxTasksField(), 0, 3, 2, 3);
        //
        addComponent(getDescriptionArea(), 0, 4, 2, 5);

        //add property selection
        Button addPropertyButton = new Button();
        addPropertyButton.setIcon(new ThemeResource(IconContainer.ADD));
        addPropertyButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                addPropertyComponent.reset();
                addPropertyComponent.showWindow();
            }
        });

        HorizontalLayout layout = new HorizontalLayout(getEnvironmentPropertiesSelect(), addPropertyButton);
        layout.setComponentAlignment(getEnvironmentPropertiesSelect(), Alignment.TOP_LEFT);
        layout.setComponentAlignment(addPropertyButton, Alignment.BOTTOM_RIGHT);
        layout.setSizeFull();
        layout.setExpandRatio(getEnvironmentPropertiesSelect(), .95f);
        layout.setExpandRatio(addPropertyButton, .05f);
        addComponent(layout, 3, 4, 3, 5);

        //add popup to layout
        addPropertyComponent = new AddEnvironmentPropertyComponent(this);

        setComponentAlignment(getPathSelectorButton(), Alignment.BOTTOM_LEFT);
        setColumnExpandRatio(0, 0.2f);
        setColumnExpandRatio(1, 0.2f);
        setColumnExpandRatio(2, 0.2f);
        setColumnExpandRatio(3, 0.2f);
        setRowExpandRatio(5, 1f);
    }

    public TwinColSelect getEnvironmentPropertiesSelect() {
        if (environmentProperties == null) {
            String id = "providedEnvironmentProperties";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            environmentProperties = new TwinColSelect("ENVIRONMENT PROPERTIES");
            environmentProperties.setId(DEBUG_ID_PREFIX + id);
            environmentProperties.setSizeFull();
            environmentProperties.setRows(8);
            environmentProperties.setImmediate(true);
            environmentProperties.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            environmentProperties.addStyleName("colored");
        }

        return environmentProperties;
    }

    /**
     * Returns the access point base path field holding the base path of the
     * configured access point within the execution environment.
     *
     * @return The access point base path field.
     */
    public final ComboBox getAccessPointBox() {
        if (accessPointBox == null) {
            String id = "accessPointBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            accessPointBox = new ComboBox("ACCESS POINT");
            accessPointBox.setId(DEBUG_ID_PREFIX + id);
            accessPointBox.setWidth("100%");
            accessPointBox.setNullSelectionAllowed(false);
            accessPointBox.setRequired(true);
            accessPointBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return accessPointBox;
    }

    /**
     * Returns the max number of parallel tasks that should be handles by the
     * environment.
     *
     * @return The max tasks field.
     */
    public TextField getMaxTasksField() {
        if (maxTasksField == null) {
            maxTasksField = factoryTextField("MAX PARALLEL TASKS", "maxTasksField", true);
        }
        return maxTasksField;
    }

    /**
     * Returns the path selector button for selecting the local base path of the
     * configured access point.
     *
     * @return The path selector button.
     */
    public Button getPathSelectorButton() {
        if (pathSelectorButton == null) {
            String id = "apPathSelectorButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            pathSelectorButton = new Button("Select Path");
            pathSelectorButton.setId(DEBUG_ID_PREFIX + id);
            pathSelectorButton.setImmediate(true);

            pathSelectorButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    PathSelector pathSelector = new PathSelector(new IPathSelector() {

                        @Override
                        public void firePathSelectorCloseEvent(String selectedPath) {
                            getAccessPointBasePathField().setValue(selectedPath);
                        }
                    });
                    UI.getCurrent().addWindow(pathSelector);
                }
            });
        }
        return pathSelectorButton;
    }

    /**
     * Returns thebase path field for the provided access point.
     *
     * @return The base path field.
     */
    public TextField getAccessPointBasePathField() {
        if (accessPointBasePathField == null) {
            accessPointBasePathField = factoryTextField("ACCESS POINT BASE PATH", "accessPointBasePathField", true);
        }
        return accessPointBasePathField;
    }

    @Override
    public void updateSelection(ExecutionEnvironmentConfiguration pValue) throws UIComponentUpdateException {
        reset();

        if (pValue == null) {
            throw new UIComponentUpdateException("Invalid execution environment.");
        }

        getNameField().setValue(pValue.getName());
        if (pValue.getGroupId() == null) {
            getGroupBox().select(AbstractBasePropertiesLayout.ALL_GROUPS_ID);
        } else {
            getGroupBox().select(pValue.getGroupId());
        }
        getMaxTasksField().setValue(Integer.toString(pValue.getMaxParallelTasks()));
        getAccessPointBasePathField().setValue(pValue.getAccessPointLocalBasePath());
        getDefaultBox().setValue(pValue.isDefaultEnvironment());
        getDisabledBox().setValue(pValue.isDisabled());
        getAccessPointBox().select(pValue.getStagingAccessPointId());
        getDescriptionArea().setValue(pValue.getDescription());

        pValue.getProvidedEnvironmentProperties().forEach((prop) -> {
            getEnvironmentPropertiesSelect().select(prop.getId());
        });
    }

    @Override
    public void reset() {
        setEnabled(true);
        getNameField().setValue(null);
        getGroupBox().select(ALL_GROUPS_ID);
        getMaxTasksField().setValue("1");
        getAccessPointBasePathField().setValue(null);
        getDefaultBox().setValue(false);
        getDisabledBox().setValue(false);
        getDescriptionArea().setValue(null);

        List<StagingAccessPointConfiguration> accessPoints = StagingConfigurationPersistence.getSingleton().findAllAccessPointConfigurations();
        String first = null;
        for (StagingAccessPointConfiguration accessPoint : accessPoints) {
            accessPointBox.addItem(accessPoint.getUniqueIdentifier());
            accessPointBox.setItemCaption(accessPoint.getUniqueIdentifier(), accessPoint.getName());
            if (first == null) {
                first = accessPoint.getUniqueIdentifier();
            }
        }

        accessPointBox.select(first);

        reloadEnvironmentPropertiesList();
    }

    private void reloadEnvironmentPropertiesList() {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        List<ExecutionEnvironmentProperty> props;
        try {
            props = mdm.find(ExecutionEnvironmentProperty.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            props = new LinkedList<>();
            LOGGER.error("Failed to obtain ExecutionEnvironmentProperties.", ex);
        } finally {
            mdm.close();
        }
        //sort alphabetically by name
        Collections.sort(props, new Comparator<ExecutionEnvironmentProperty>() {

            @Override
            public int compare(ExecutionEnvironmentProperty o1, ExecutionEnvironmentProperty o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (ExecutionEnvironmentProperty prop : props) {
            getEnvironmentPropertiesSelect().addItem(prop.getId());
            getEnvironmentPropertiesSelect().setItemCaption(prop.getId(), prop.getName());
        }
    }

    @Override
    public String getNameFieldLabel() {
        return "ENVIRONMENT NAME";
    }

    @Override
    public void fireEnvironmentPropertyCreatedEvent(ExecutionEnvironmentProperty pProperty) {
        Object selection = getEnvironmentPropertiesSelect().getValue();;
        reloadEnvironmentPropertiesList();
        if (selection != null) {
            if (selection instanceof Set) {
                Set set = (Set) selection;
                for (Object o : set) {
                    getEnvironmentPropertiesSelect().select(o);
                }
            } else {
                getEnvironmentPropertiesSelect().select(selection);
            }
        }
    }
}
