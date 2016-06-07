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

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.themes.BaseTheme;
import com.zybnet.autocomplete.server.AutocompleteField;
import com.zybnet.autocomplete.server.AutocompleteQueryListener;
import com.zybnet.autocomplete.server.AutocompleteSuggestionPickedListener;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.ui.admin.AdminUIMainView;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.ui.admin.AbstractConfigurationTab;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class StagingProcessorConfigurationTab extends AbstractConfigurationTab<StagingProcessor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingProcessorConfigurationTab.class);
    private String DEBUG_ID_PREFIX = StagingProcessorConfigurationTab.class.getName() + "_";

    private GridLayout mainLayout;
    private AutocompleteField<String> implementationClassField;
    private NativeButton loadImplementationClassButton;
    private StagingProcessorBasePropertiesLayout basicPropertiesLayout;

    public StagingProcessorConfigurationTab(AdminUIMainView pParentApp) {
        super(pParentApp);
        DEBUG_ID_PREFIX += hashCode() + "_";
    }

    /**
     *
     * @return
     */
    @Override
    public GridLayout buildMainLayout() {
        String id = "mainLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        mainLayout = new GridLayout(3, 6);
        mainLayout.setId(DEBUG_ID_PREFIX + id);
        mainLayout.setSizeFull();
        mainLayout.setImmediate(true);
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        // Add components to mainLayout
        mainLayout.addComponent(getElementList(), 0, 0, 0, 5);
        mainLayout.addComponent(getImplementationClassField(), 1, 0);
        mainLayout.addComponent(getLoadImplementationClassButton(), 2, 0);
        mainLayout.addComponent(new Label("<p> <hr/>", ContentMode.HTML), 1, 1, 2, 1);
        //mainLayout.addComponent(getPropertiesPanel(), 1, 2);
        HorizontalLayout coreLayout = new HorizontalLayout(getNavigateLeftButton(), getPropertiesPanel(), getNavigateRightButton());
        coreLayout.setSizeFull();
        coreLayout.setExpandRatio(getNavigateLeftButton(), .05f);
        coreLayout.setComponentAlignment(getNavigateLeftButton(), Alignment.MIDDLE_LEFT);
        coreLayout.setExpandRatio(getPropertiesPanel(), .9f);
        coreLayout.setExpandRatio(getNavigateRightButton(), .05f);
        coreLayout.setComponentAlignment(getNavigateRightButton(), Alignment.MIDDLE_RIGHT);
        mainLayout.addComponent(coreLayout, 1, 2, 2, 2);
        mainLayout.addComponent(new Label("<hr/>", ContentMode.HTML), 1, 3, 2, 3);
        // mainLayout.addComponent(getNavigationButton(), 2, 2);
        mainLayout.addComponent(getBulletLineLayout(), 1, 4, 2, 4);
        mainLayout.addComponent(getCommitChangesButton(), 1, 5, 2, 5);

        mainLayout.setComponentAlignment(getImplementationClassField(), Alignment.BOTTOM_LEFT);
        mainLayout.setComponentAlignment(getLoadImplementationClassButton(), Alignment.BOTTOM_RIGHT);
        // mainLayout.setComponentAlignment(getNavigationButton(), Alignment.MIDDLE_RIGHT);
        mainLayout.setComponentAlignment(getBulletLineLayout(), Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(getCommitChangesButton(), Alignment.BOTTOM_RIGHT);

        mainLayout.setColumnExpandRatio(0, 0.3f);
        mainLayout.setColumnExpandRatio(1, 0.69f);
        mainLayout.setColumnExpandRatio(2, 0.01f);
        return mainLayout;
    }

    @Override
    public void resetComponents() {
        getImplementationClassField().setReadOnly(false);
        StagingProcessor selection = loadElementById(getSelectedElementId());
        if (selection == null || selection.getImplementationClass() == null) {
            getImplementationClassField().clearChoices();
        } else {
            getImplementationClassField().setText(selection.getImplementationClass());
        }
    }

    /**
     *
     * @return
     */
    public AutocompleteField<String> getImplementationClassField() {
        if (implementationClassField == null) {
            String id = "implementationClassField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            implementationClassField = new AutocompleteField<>();
            implementationClassField.setCaption("PROCESSOR IMPLEMENTATION CLASS");
            implementationClassField.setId(DEBUG_ID_PREFIX + id);
            implementationClassField.setImmediate(true);
            implementationClassField.setWidth("100%");
            implementationClassField.setRequired(true);
            implementationClassField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            implementationClassField.setMinimumQueryCharacters(3);
            implementationClassField.setQueryListener(new AutocompleteQueryListener<String>() {
                @Override
                public void handleUserQuery(AutocompleteField<String> field, String query) {
                    Reflections reflections = new Reflections(query);
                    Set<Class<? extends AbstractStagingProcessor>> jobs = reflections.getSubTypesOf(AbstractStagingProcessor.class);
                    for (Class c : jobs) {
                        field.addSuggestion(c.getCanonicalName(), c.getCanonicalName());
                    }
                }
            });

            implementationClassField.setSuggestionPickedListener(new AutocompleteSuggestionPickedListener<String>() {
                @Override
                public void onSuggestionPicked(String value) {
                    getImplementationClassField().setText(value);
                    getImplementationClassField().setValue(value);
                }
            });

        }
        return implementationClassField;
    }

    /**
     *
     * @return
     */
    public NativeButton getLoadImplementationClassButton() {
        if (loadImplementationClassButton == null) {
            String id = "loadImplementationClassField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            loadImplementationClassButton = new NativeButton();
            loadImplementationClassButton.setId(DEBUG_ID_PREFIX + id);
            loadImplementationClassButton.setIcon(
                    new ThemeResource(IconContainer.TEXT_CODE_JAVA_INPUT));
            loadImplementationClassButton.setStyleName(BaseTheme.BUTTON_LINK);
            loadImplementationClassButton.setImmediate(true);
            loadImplementationClassButton.setDescription("Load the entered implementation "
                    + "class for setting the configuration of the requested processor");

            loadImplementationClassButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    try {
                        StagingProcessor newProcessor = createProcessor();
                        commitProcessor(newProcessor);
                        addNewElementInstance(newProcessor);
                        setEnabledComponents(ListSelection.VALID);
                        getParentApp().showNotification("Staging processor successfully created.");
                    } catch (ConfigurationException ex) {
                        getParentApp().showError("Implementation class not loadable! Cause: " + ex.getMessage());
                        LOGGER.error("Failed to load requested implementation class. Cause: "
                                + ex.getMessage(), ex);
                    }
                }
            });
        }
        return loadImplementationClassButton;
    }

    /**
     * Create a new processor.
     *
     * <p align="right"> <b>by</b> Jejkal</p>
     */
    private StagingProcessor createProcessor() throws ConfigurationException {
        // Validate value of implementation class field
        /*if (!UIUtils7.validate(getImplementationClassField())) {
            throw new ConfigurationException("Invalid implementation class.");
    }*/

        // Valid implementation class name => Create new processor
        String implClassName = getImplementationClassField().getText();
        try {
            // Build instance of implementation class
            createProcessorInstance(implClassName);
        } catch (ConfigurationException ex) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Failed to create access provider instance. ", ex);
        }
        // Create new processor
        StagingProcessor newProcessor = StagingProcessor.factoryNewStagingProcessor();
        newProcessor.setName("New processor (" + newProcessor.getUniqueIdentifier() + ")");
        newProcessor.setImplementationClass(implClassName);
        newProcessor.setDisabled(true);
        return newProcessor;
    }

    /**
     *
     * @param implClassName
     * @return
     * @throws ConfigurationException
     */
    private AbstractStagingProcessor createProcessorInstance(String implClassName)
            throws ConfigurationException {
        return createProcessorInstance(implClassName, null);
    }

    /**
     * Create a new processor instance.
     *
     * @param implClassName
     * @return
     * @throws ConfigurationException
     */
    private AbstractStagingProcessor createProcessorInstance(String implClassName, StagingProcessor processor) throws ConfigurationException {
        try {
            if (processor == null) {
                return (AbstractStagingProcessor) Class.forName(implClassName).getConstructor(String.class).newInstance("123");
            }
            return (AbstractStagingProcessor) Class.forName(implClassName).getConstructor(String.class).newInstance(processor.getName());
        } catch (ClassNotFoundException ex) {
            String msg = "Implementation class '" + implClassName + "' not found.";
            throw new ConfigurationException(msg, ex);
        } catch (ClassCastException ex) {
            String msg = "Implementation class '" + implClassName + "'does not implement AbstractStagingProcessor.";
            throw new ConfigurationException(msg, ex);
        } catch (InstantiationException ex) {
            String msg = "Failed to instantiate implementation class '"
                    + implClassName + "' with the default constructor.";
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

    @Override
    public StagingProcessorBasePropertiesLayout getBasicPropertiesLayout() {
        if (basicPropertiesLayout == null) {
            basicPropertiesLayout = new StagingProcessorBasePropertiesLayout(getParentApp());
        }
        return basicPropertiesLayout;
    }

    @Override
    public void commitChanges() {
        StagingProcessor selectedElement = loadElementById(getSelectedElementId());
        try {
            validateRequiredComponentValues();
            StagingProcessor changedProcessor = changeProcessor(selectedElement);
            checkProcessorConfigurability(changedProcessor);
            commitProcessor(changedProcessor);
            updateElementInstance(changedProcessor);

            if (!changedProcessor.isDownloadProcessingSupported() && !changedProcessor.isDownloadProcessingSupported()) {
                getParentApp().showNotification("Changes successfully committed. Attention: The current configuration neither supports ingests nor downloads and will be ignored.");
            } else {
                getParentApp().showNotification("Changes successfully committed.");
            }
        } catch (ConfigurationException ex) {
            getParentApp().showError("Staging processor not modifiable! Cause: " + ex.getMessage());
            String object = "the changed processor '" + selectedElement.getUniqueIdentifier() + "'";
            LOGGER.error(MsgBuilder.commitFailed(object) + "Cause: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void addNewElementInstance(StagingProcessor pElementToAdd) {
        getElementList().addItem(pElementToAdd.getUniqueIdentifier());
        getElementList().setItemCaption(pElementToAdd.getUniqueIdentifier(),
                getProcessorItemCaption(pElementToAdd));
        getElementList().select(pElementToAdd.getUniqueIdentifier());
    }

    @Override
    public void updateElementInstance(StagingProcessor pElementToUpdate) {
        getElementList().removeItem(pElementToUpdate.getUniqueIdentifier());
        addNewElementInstance(pElementToUpdate);
    }

    @Override
    public void enableComponents(boolean pValue) {
        getImplementationClassField().setReadOnly(!pValue);
        getLoadImplementationClassButton().setEnabled(pValue);
    }

    @Override
    public boolean elementWithIdExists(String pId) {
        return (StagingConfigurationPersistence.getSingleton().findStagingProcessorById(pId) != null);
    }

    @Override
    public StagingProcessor loadElementById(String pId) {
        if (NEW_UNIQUE_ID.equals(pId)) {
            return StagingProcessor.factoryNewStagingProcessor();
        }
        return StagingConfigurationPersistence.getSingleton().findStagingProcessorById(pId);
    }

    @Override
    public void selectElement(StagingProcessor pSelectedElement) throws ConfigurationException, UIComponentUpdateException {
        implementationClassField.setReadOnly(false);
        if (pSelectedElement != null && pSelectedElement.getImplementationClass() != null) {
            getImplementationClassField().setText(pSelectedElement.getImplementationClass());
            getImplementationClassField().setValue(pSelectedElement.getImplementationClass());
            AbstractStagingProcessor processorInstance = createProcessorInstance(pSelectedElement.getImplementationClass());
            try {
                getSpecificPropertiesLayout().updateComponents(processorInstance, pSelectedElement.getPropertiesAsObject());
            } catch (IOException ex) {
                throw new ConfigurationException("Failed to read properties from provided staging processor.", ex);
            }
        } else {
            getImplementationClassField().clearChoices();
            getSpecificPropertiesLayout().reset();
        }
    }

    @Override
    public void fillElementList() {
        List<StagingProcessor> processors = StagingConfigurationPersistence
                .getSingleton(null).findAllStagingProcessors();
        for (StagingProcessor processor : processors) {
            getElementList().addItem(processor.getUniqueIdentifier());
            getElementList().setItemCaption(processor.getUniqueIdentifier(),
                    getProcessorItemCaption(processor));
        }
    }

    /**
     * Check the currently selected staging processor. During this check, some
     * properties will be validated. For some minor issues, warnings are
     * generated, which are returned as result. For major issues, an exception
     * is thrown. If 'null' is returned, an internal error which is indicated in
     * an own way (e.g. validation of a text field has failed) has occured.
     *
     * @throws ConfigurationException in case of a major configuration error.
     */
    private void validateRequiredComponentValues() throws ConfigurationException {
        checkImplementationClassField();
        checkNameField();
    }

    /**
     *
     * @throws ConfigurationException
     */
    private void checkImplementationClassField() throws ConfigurationException {
        if (getImplementationClassField().getText() == null || getImplementationClassField().getText().isEmpty() || !UIUtils7.validate(getImplementationClassField())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Implementation class is invalid.");
        }
    }

    /**
     *
     * @throws ConfigurationException
     */
    private void checkNameField() throws ConfigurationException {
        if (UIComponentTools.isEmpty(getBasicPropertiesLayout().getNameField())
                || !UIUtils7.validate(getBasicPropertiesLayout().getNameField())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Processor name is invalid.");
        }

        StagingProcessor existingProcessor = StagingConfigurationPersistence.getSingleton(null).
                findStagingProcessorByName(getBasicPropertiesLayout().getNameField().getValue());

        if (existingProcessor != null && !existingProcessor.getUniqueIdentifier()
                .equals(getSelectedElementId())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("There is already a staging processor named '"
                    + existingProcessor.getName() + "'.");
        }
    }

    /**
     *
     * @throws ConfigurationException
     */
    private void checkProcessorConfigurability(StagingProcessor processor)
            throws ConfigurationException {
        checkImplementationClassField();
        String implClass = getImplementationClassField().getText();
        AbstractStagingProcessor processorInstance = createProcessorInstance(implClass, processor);
        try {
            processorInstance.validateProperties(getSpecificPropertiesLayout().getProperties());
        } catch (PropertyValidationException ex) {
            setPropertiesLayout(PropertiesLayoutType.SPECIFIC);
            throw new ConfigurationException("Failed to validate the staging processor properties. ", ex);
        }
    }

    /**
     *
     * @throws ConfigurationException
     */
    private StagingProcessor changeProcessor(StagingProcessor processor)
            throws ConfigurationException {
        if (processor == null) {
            processor = StagingProcessor.factoryNewStagingProcessor();
            LOGGER.warn("Processor, passed for being changed, is null. New instance created.");
        }
        processor.setName(getBasicPropertiesLayout().getNameField().getValue());
        processor.setDefaultOn(getBasicPropertiesLayout().getDefaultBox().getValue());
        processor.setDisabled(getBasicPropertiesLayout().getDisabledBox().getValue());
        processor.setDescription((String) getBasicPropertiesLayout().getDescriptionArea().getValue());
        processor.setGroupId((String) getBasicPropertiesLayout().getGroupBox().getValue());
        processor.setType((StagingProcessor.PROCESSOR_TYPE) getBasicPropertiesLayout()
                .getProcessorTypeBox().getValue());
        processor.setIngestProcessingSupported(getBasicPropertiesLayout().getIngestProcessingSupportedBox().getValue());
        processor.setDownloadProcessingSupported(getBasicPropertiesLayout().getDownloadProcessingSupportedBox().getValue());

        try {
            processor.setPropertiesFromObject(getSpecificPropertiesLayout().getProperties());
        } catch (IOException ex) {
            setPropertiesLayout(PropertiesLayoutType.SPECIFIC);
            throw new ConfigurationException("Failed to obtain properties from UI.", ex);
        }

        return processor;
    }

    /**
     * Commit all changes to the database.
     */
    private void commitProcessor(StagingProcessor processor) throws ConfigurationException {
        try {
            StagingConfigurationPersistence.getSingleton(null).saveStagingProcessor(processor);
        } catch (UnauthorizedAccessAttemptException ex) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            String object = "processor '" + processor.getName() + "'";
            throw new ConfigurationException(MsgBuilder.unauthorizedSaveRequest(object), ex);
        }
    }

    /**
     *
     * @param processor
     * @return
     */
    private String getProcessorItemCaption(StagingProcessor processor) {
        return processor.getName() + " (" + processor.getId() + ")";
    }

}
