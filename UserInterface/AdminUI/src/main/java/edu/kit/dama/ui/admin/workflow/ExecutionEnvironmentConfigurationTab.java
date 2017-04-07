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
import com.vaadin.ui.GridLayout;
import com.zybnet.autocomplete.server.AutocompleteField;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.dataworkflow.AbstractExecutionEnvironmentHandler;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.ui.admin.AbstractConfigurationTab;
import edu.kit.dama.ui.admin.AbstractConfigurationTab.ListSelection;
import static edu.kit.dama.ui.admin.AbstractConfigurationTab.NEW_UNIQUE_ID;
import edu.kit.dama.ui.admin.AbstractConfigurationTab.PropertiesLayoutType;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.commons.util.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class ExecutionEnvironmentConfigurationTab extends AbstractConfigurationTab<ExecutionEnvironmentConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionEnvironmentConfigurationTab.class);

    private String DEBUG_ID_PREFIX = ExecutionEnvironmentConfigurationTab.class.getName() + "_";

    public static final String NEW_ENVIRONMENT_CAPTION = "NEW";

    private GridLayout mainLayout;
    private AutocompleteField<String> implementationClassField;
    private Button loadImplementationClassButton;
    private ExecutionEnvironmentBasePropertiesLayout basicPropertiesLayout;

    public ExecutionEnvironmentConfigurationTab() {
        DEBUG_ID_PREFIX += hashCode() + "_";
    }

    /**
     * Build the main layout.
     *
     * @return The main layout.
     */
    @Override
    public GridLayout buildMainLayout() {
        String id = "mainLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        UIUtils7.GridLayoutBuilder mainLayoutBuilder = new UIUtils7.GridLayoutBuilder(3, 3);

        // Add components to mainLayout
        mainLayoutBuilder.fillColumn(getElementList(), 0, 0, 1);
        mainLayoutBuilder.addComponent(getImplementationClassField(), 1, 0, 1, 1).addComponent(getLoadImplementationClassButton(), Alignment.BOTTOM_RIGHT, 2, 0, 1, 1);
        mainLayoutBuilder.fillRow(getPropertiesPanel(), 1, 1, 1);
        mainLayoutBuilder.addComponent(getNavigateButton(), Alignment.BOTTOM_LEFT, 1, 2, 1, 1).addComponent(getCommitChangesButton(), Alignment.BOTTOM_RIGHT, 2, 2, 1, 1);

        mainLayout = mainLayoutBuilder.getLayout();
        mainLayout.setId(DEBUG_ID_PREFIX + id);
        mainLayout.setSizeFull();
        mainLayout.setImmediate(true);
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        mainLayout.setColumnExpandRatio(1, 1f);
        mainLayout.setRowExpandRatio(1, 1f);

        return mainLayout;
    }

    /**
     * Generated the ExecutionEnvironmentConfiguration caption in the format
     * NAME@GROUP if group exists, otherwise the caption is only the name.
     *
     * @param pEnvironment The ExecutionEnvironmentConfiguration for which to
     * obtain the caption.
     *
     * @return The caption.
     */
    private String getEnvironmentCaption(ExecutionEnvironmentConfiguration pEnvironment) {
        return pEnvironment.getName() + " (" + pEnvironment.getId() + ")";
    }

    @Override
    public boolean elementWithIdExists(String pId) {
        return loadElementById(pId) != null;
    }

    @Override
    public ExecutionEnvironmentConfiguration loadElementById(String pId) {
        ExecutionEnvironmentConfiguration result = null;
        if (NEW_UNIQUE_ID.equals(pId)) {
            //create 'new' element
            result = ExecutionEnvironmentConfiguration.factoryNewExecutionEnvironmentConfiguration();
            if (getImplementationClassField().getText() != null) {
                result.setHandlerImplementationClass(getImplementationClassField().getText());
            }
        } else {
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            try {
                mdm.setAuthorizationContext(UIHelper.getSessionContext());
                return mdm.findSingleResult("SELECT e FROM ExecutionEnvironmentConfiguration e WHERE e.uniqueIdentifier='" + pId + "'", ExecutionEnvironmentConfiguration.class);
            } catch (AuthorizationException ex) {
                LOGGER.error("Failed to obtain execution environment configuration for id " + pId, ex);
            } finally {
                mdm.close();
            }
        }
        return result;
    }

    @Override
    public void fillElementList() {
        List<ExecutionEnvironmentConfiguration> configurations = new LinkedList<>();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        try {
            mdm.setAuthorizationContext(UIHelper.getSessionContext());
            configurations = mdm.find(ExecutionEnvironmentConfiguration.class);
        } catch (AuthorizationException ex) {
            LOGGER.error("Failed to obtain list of all execution environment configurations", ex);
        } finally {
            mdm.close();
        }

        Collections.sort(configurations, (ExecutionEnvironmentConfiguration o1, ExecutionEnvironmentConfiguration o2) -> Long.compare(o1.getId(), o2.getId()));

        for (ExecutionEnvironmentConfiguration config : configurations) {
            getElementList().addItem(config.getUniqueIdentifier());
            getElementList().setItemCaption(config.getUniqueIdentifier(), getEnvironmentCaption(config));
        }
    }

    @Override
    public void resetComponents() {
        getImplementationClassField().setReadOnly(false);
        ExecutionEnvironmentConfiguration selection = loadElementById(getSelectedElementId());

        if (selection == null || selection.getHandlerImplementationClass() == null) {
            getImplementationClassField().clearChoices();
            getImplementationClassField().setValue(null);
        } else {
            getImplementationClassField().setText(selection.getHandlerImplementationClass());
            getImplementationClassField().setValue(selection.getHandlerImplementationClass());
        }
    }

    @Override
    public ExecutionEnvironmentBasePropertiesLayout getBasicPropertiesLayout() {
        if (basicPropertiesLayout == null) {
            basicPropertiesLayout = new ExecutionEnvironmentBasePropertiesLayout();
        }
        return basicPropertiesLayout;
    }

    @Override
    public void addNewElementInstance(ExecutionEnvironmentConfiguration pElementToAdd) {
        getElementList().addItem(pElementToAdd.getUniqueIdentifier());
        getElementList().setItemCaption(pElementToAdd.getUniqueIdentifier(), getEnvironmentCaption(pElementToAdd));
        getElementList().select(pElementToAdd.getUniqueIdentifier());
    }

    @Override
    public void updateElementInstance(ExecutionEnvironmentConfiguration pElementToUpdate) {
        getElementList().removeItem(pElementToUpdate.getUniqueIdentifier());
        addNewElementInstance(pElementToUpdate);
    }

    @Override
    public void enableComponents(boolean pValue) {
        getImplementationClassField().setReadOnly(!pValue);
        getLoadImplementationClassButton().setEnabled(pValue);
    }

    @Override
    public void selectElement(ExecutionEnvironmentConfiguration pSelectedElement) throws ConfigurationException, UIComponentUpdateException {
        implementationClassField.setReadOnly(false);
        if (pSelectedElement != null && pSelectedElement.getHandlerImplementationClass() != null && !pSelectedElement.getHandlerImplementationClass().isEmpty()) {
            getImplementationClassField().setText(pSelectedElement.getHandlerImplementationClass());
            getImplementationClassField().setValue(pSelectedElement.getHandlerImplementationClass());
            AbstractExecutionEnvironmentHandler handler = createExecutionEnvironmentHandlerInstance(pSelectedElement.getHandlerImplementationClass());
            Properties props = new Properties();
            try {
                props = PropertiesUtil.propertiesFromString(pSelectedElement.getCustomProperties());
            } catch (IOException ex) {
                LOGGER.error("Failed to deserialize environment configuration properties.");
            }
            getSpecificPropertiesLayout().updateComponents(handler, props);
        } else {
            getImplementationClassField().clearChoices();
            getSpecificPropertiesLayout().reset();
        }
    }

    @Override
    public void commitChanges() {
        ExecutionEnvironmentConfiguration selectedConfiguration = loadElementById(getSelectedElementId());
        try {
            validateRequiredComponentValues();
            checkEnvironmentHandlerConfigurability();
            ExecutionEnvironmentConfiguration changedConfiguration = collectSettings(selectedConfiguration);
            changedConfiguration = commitExecutionEnvironmentConfiguration(changedConfiguration);
            updateElementInstance(changedConfiguration);
            UIComponentTools.showInformation("Changes successfully committed.");
        } catch (ConfigurationException ex) {
            UIComponentTools.showError("Failed to commit changes. Cause: " + ex.getMessage());
            String object = "the changed environment configuration '" + selectedConfiguration.getUniqueIdentifier() + "'";
            LOGGER.error(MsgBuilder.commitFailed(object) + "Cause: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void setEnabledComponents(ListSelection listSelection) {
        //overwriting of this method is needed as this tab uses the properties panel for both: creating and changing elements.
        switch (listSelection) {
            case NEW:
                getPropertiesPanel().setEnabled(false);
                getCommitChangesButton().setEnabled(false);
                enableComponents(true);
                try {
                    selectElement(null);
                } catch (ConfigurationException | UIComponentUpdateException ex) {
                    //ignore
                }
                break;
            default:
                super.setEnabledComponents(listSelection);
        }
    }

    /**
     * Collect all settings, write them into the provided access point and
     * return it for writing it into the database.
     *
     * @throws ConfigurationException If accessPoint is null.
     */
    private ExecutionEnvironmentConfiguration collectSettings(ExecutionEnvironmentConfiguration pConfiguration) throws ConfigurationException {
        if (pConfiguration == null) {
            throw new ConfigurationException("Environment configuration is null.");
        }

        pConfiguration.setName(getBasicPropertiesLayout().getNameField().getValue());
        pConfiguration.setGroupId((String) getBasicPropertiesLayout().getGroupBox().getValue());
        pConfiguration.setDefaultEnvironment(getBasicPropertiesLayout().getDefaultBox().getValue());
        pConfiguration.setDisabled(getBasicPropertiesLayout().getDisabledBox().getValue());

        pConfiguration.setMaxParallelTasks(Integer.parseInt(getBasicPropertiesLayout().getMaxTasksField().getValue()));
        pConfiguration.setStagingAccessPointId((String) getBasicPropertiesLayout().getAccessPointBox().getValue());

        pConfiguration.setAccessPointLocalBasePath(getBasicPropertiesLayout().getAccessPointBasePathField().getValue());
        pConfiguration.setDescription((String) getBasicPropertiesLayout().getDescriptionArea().getValue());

        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        try {
            mdm.setAuthorizationContext(UIHelper.getSessionContext());
            LOGGER.debug("Updating provided environment properties.");
            pConfiguration.removeProvidedEnvironmentProperties();
            Set<Object> environmentProperties = (Set<Object>) getBasicPropertiesLayout().getEnvironmentPropertiesSelect().getValue();
            for (Object propId : environmentProperties) {
                ExecutionEnvironmentProperty property = mdm.find(ExecutionEnvironmentProperty.class, propId);
                if (property == null) {
                    LOGGER.warn("Failed to add environment property with id {}. Entry not found in database.", propId);
                } else {
                    pConfiguration.addProvidedEnvironmentProperty(property);
                }
            }
        } catch (AuthorizationException ex) {
            LOGGER.error("Failed to collect provided environment properties.", ex);
        } finally {
            mdm.close();
        }

        try {
            pConfiguration.setPropertiesAsObject(getSpecificPropertiesLayout().getProperties());
        } catch (IOException ex) {
            setPropertiesLayout(PropertiesLayoutType.SPECIFIC);
            throw new ConfigurationException("Failed to obtain properties from UI.", ex);
        }
        return pConfiguration;
    }

    /**
     * Get the implementation class field.
     *
     * @return The implementation class field.
     */
    private AutocompleteField getImplementationClassField() {
        if (implementationClassField == null) {
            String id = "implementationClassField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            implementationClassField = new AutocompleteField<>();
            implementationClassField.setCaption("ENVIRONMENT HANDLER IMPLEMENTATION CLASS");
            implementationClassField.setId(DEBUG_ID_PREFIX + id);
            implementationClassField.setImmediate(true);
            implementationClassField.setWidth("100%");
            implementationClassField.setRequired(true);
            implementationClassField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            implementationClassField.setMinimumQueryCharacters(3);
            implementationClassField.addStyleName("v-textfield");

            implementationClassField.setQueryListener((AutocompleteField<String> field, String query) -> {
                Reflections reflections = new Reflections(query);
                Set<Class<? extends AbstractExecutionEnvironmentHandler>> jobs = reflections.getSubTypesOf(AbstractExecutionEnvironmentHandler.class);
                Class[] elements = jobs.toArray(new Class[]{});
                Arrays.sort(elements, (Class o1, Class o2) -> o1.getCanonicalName().compareTo(o2.getCanonicalName()));

                Arrays.asList(elements).forEach((c) -> {
                    if (!c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && !c.isAnnotationPresent(Deprecated.class)) {
                        field.addSuggestion(c.getCanonicalName(), c.getCanonicalName());
                    }
                });
            });

            implementationClassField.setSuggestionPickedListener((String value) -> {
                getImplementationClassField().setText(value);
                getImplementationClassField().setValue(value);
            });
        }
        return implementationClassField;
    }

    /**
     * Get the load implementation class button.
     *
     * @return The load implementation class button.
     */
    private Button getLoadImplementationClassButton() {
        if (loadImplementationClassButton == null) {
            String id = "loadImplementationClassField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            loadImplementationClassButton = new Button();
            loadImplementationClassButton.setId(DEBUG_ID_PREFIX + id);
            loadImplementationClassButton.setIcon(new ThemeResource(IconContainer.TEXT_CODE_JAVA_INPUT));
            loadImplementationClassButton.setWidth("100%");
            loadImplementationClassButton.setDescription("Load the entered implementation "
                    + "class for setting the configuration of the requested job");

            loadImplementationClassButton.addClickListener((Button.ClickEvent event) -> {
                try {
                    ExecutionEnvironmentConfiguration newConfiguration = createExecutionEnvironmentConfiguration();
                    newConfiguration = commitExecutionEnvironmentConfiguration(newConfiguration);
                    addNewElementInstance(newConfiguration);
                    setEnabledComponents(ListSelection.VALID);
                    UIComponentTools.showInformation("Execution environment successfully created.");
                } catch (ConfigurationException ex) {
                    UIComponentTools.showError("Implementation class not loadable! Cause: " + ex.getMessage());
                    LOGGER.error("Failed to load requested implementation class. Cause: "
                            + ex.getMessage(), ex);
                }
            });
        }
        return loadImplementationClassButton;
    }

    /**
     * Create a new ExecutionEnvironmentConfiguration instance for the provided
     * implementation class with the implementation class name as name.
     *
     * @return The ExecutionEnvironmentConfiguration.
     *
     * @throws ConfigurationException If the creation has failed, e.g. due to an
     * invalid class.
     */
    private ExecutionEnvironmentConfiguration createExecutionEnvironmentConfiguration() throws ConfigurationException {
        // Validate value of implementation class field
        String implClass = getImplementationClassField().getText();
        if (implClass == null) {
            throw new ConfigurationException("Please use the auto-completion feature to select an implementation class.");
        }

        // Valid implementation class name => Create new handler instance
        createExecutionEnvironmentHandlerInstance(implClass);
        //success...create configuration
        ExecutionEnvironmentConfiguration configuration = ExecutionEnvironmentConfiguration.factoryNewExecutionEnvironmentConfiguration();
        configuration.setName(implClass.substring(implClass.lastIndexOf(".") + 1));
        configuration.setHandlerImplementationClass(implClass);
        configuration.setDisabled(true);
        return configuration;
    }

    /**
     * Create a new AbstractExecutionEnvironmentHandler instance using the
     * provided class name.
     *
     * @param implClassName The fully qualified class name.
     *
     * @return The AbstractExecutionEnvironmentHandler instance.
     *
     * @throws ConfigurationException If the creation has failed.
     */
    private AbstractExecutionEnvironmentHandler createExecutionEnvironmentHandlerInstance(String implClassName) throws ConfigurationException {
        try {
            Class clazz = Class.forName(implClassName);
            return (AbstractExecutionEnvironmentHandler) clazz.getConstructor().newInstance();
        } catch (ClassNotFoundException ex) {
            String msg = "Implementation class '" + implClassName + "' not found.";
            throw new ConfigurationException(msg, ex);
        } catch (ClassCastException ex) {
            String msg = "Implementation class '" + implClassName + "' does not implement AbstractExecutionEnvironmentHandler.";
            throw new ConfigurationException(msg, ex);
        } catch (InstantiationException ex) {
            String msg = "Failed to instantiate implementation class '" + implClassName + "' with the default constructor.";
            throw new ConfigurationException(msg, ex);
        } catch (IllegalAccessException ex) {
            String msg = "Failed to access the default constructor of the implementation class '"
                    + implClassName + "'.";
            throw new ConfigurationException(msg, ex);
        } catch (InvocationTargetException ex) {
            String msg = "Failed to invoke default constructor of the implementation class '"
                    + implClassName + "'.";
            throw new ConfigurationException(msg, ex);
        } catch (NoSuchMethodException ex) {
            String msg = "Failed to find default constructor with argument string for class '"
                    + implClassName + "'.";
            throw new ConfigurationException(msg, ex);
        }
    }

    /**
     * Check the currently selected schedule. During this check, some properties
     * will be validated. For some minor issues, warnings are generated, which
     * are returned as result. For major issues, an exception is thrown. If
     * 'null' is returned, an internal error which is indicated in an own way
     * (e.g. validation of a text field has failed) has occured.
     *
     * @throws ConfigurationException in case of a major configuration error.
     */
    private void validateRequiredComponentValues() throws ConfigurationException {
        checkImplementationClassField();
        checkNameField();
    }

    /**
     * Check the implementation class field for being valid.
     *
     * @throws ConfigurationException If the value of the implementation class
     * field is not valid.
     */
    private void checkImplementationClassField() throws ConfigurationException {
        if (getImplementationClassField().getText() == null || getImplementationClassField().getText().isEmpty()
                || !UIUtils7.validate(getImplementationClassField())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Implementation class is invalid.");
        }
    }

    /**
     * Commit all changes to the database.
     *
     * @throws ConfigurationException If committing failed.
     */
    private ExecutionEnvironmentConfiguration commitExecutionEnvironmentConfiguration(ExecutionEnvironmentConfiguration pConfiguration) throws ConfigurationException {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();

        try {
            mdm.setAuthorizationContext(UIHelper.getSessionContext());
            return mdm.save(pConfiguration);
        } catch (AuthorizationException ex) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            String object = "execution environment configuration '" + pConfiguration.getName() + "'";
            throw new ConfigurationException(MsgBuilder.unauthorizedSaveRequest(object), ex);
        } finally {
            mdm.close();
        }
    }

    /**
     * Check the name field for being valid.
     *
     * @throws ConfigurationException If the value of the name field is not
     * valid.
     */
    private void checkNameField() throws ConfigurationException {
        if (UIComponentTools.isEmpty(getBasicPropertiesLayout().getNameField())
                || !UIUtils7.validate(getBasicPropertiesLayout().getNameField())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Environment Configuration name is invalid.");
        }
    }

    /**
     * Check if the environment handler can be configured using the properties
     * provided by the specific properties layout.
     *
     * @throws ConfigurationException If the configuration fails.
     */
    private void checkEnvironmentHandlerConfigurability() throws ConfigurationException {
        checkImplementationClassField();
        AbstractExecutionEnvironmentHandler handler = createExecutionEnvironmentHandlerInstance(getImplementationClassField().getText());
        try {
            handler.validateProperties(getSpecificPropertiesLayout().getProperties());
        } catch (PropertyValidationException ex) {
            setPropertiesLayout(PropertiesLayoutType.SPECIFIC);
            throw new ConfigurationException("Failed to validate the execution environment properties. " + ex.getMessage(), ex);
        }
    }

}
