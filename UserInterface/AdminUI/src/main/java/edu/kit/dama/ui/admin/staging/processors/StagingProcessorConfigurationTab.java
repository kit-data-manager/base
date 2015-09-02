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

import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.LiferayTheme;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.ui.admin.AdminUIMainView;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.ui.commons.util.UIUtils7;
import static edu.kit.dama.util.Constants.USERS_GROUP_ID;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class StagingProcessorConfigurationTab extends CustomComponent {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(StagingProcessorConfigurationTab.class);
    private static final String DEBUG_ID_PREFIX
            = StagingProcessorConfigurationTab.class.getName() + "_";

    public static final String NEW_PROCESSOR_CAPTION = "NEW";
    public static final String NEW_PROCESSOR_UNIQUE_ID = "MINUS_ONE_PROCESSOR";

    private final AdminUIMainView parentApp;
    private GridLayout mainLayout;
    private ListSelect processorLister;
    private TextField implementationClassField;
    private NativeButton loadImplementationClassButton;
    private Panel propertiesPanel;
    private NativeButton navigationButton;
    private HorizontalLayout bulletLineLayout;
    private BasicPropertiesLayout basicPropertiesLayout;
    private SpecificPropertiesLayout specificPropertiesLayout;
    private NativeButton commitChangesButton;
    private StagingProcessor selectedProcessor = StagingProcessor.factoryNewStagingProcessor();
    private PropertiesLayout propertiesLayout;
    private NativeButton bulletBasic;
    private NativeButton bulletSpecific;

    public enum PropertiesLayout {

        BASIC,
        SPECIFIC;
    }

    public enum ListSelection {

        NO,
        NEW,
        VALID,
        INVALID;
    }

    public StagingProcessorConfigurationTab(AdminUIMainView pParentApp) {
        parentApp = pParentApp;
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));

        setCompositionRoot(getMainLayout());
        update();
    }

    /**
     *
     * @return
     */
    private GridLayout getMainLayout() {
        if (mainLayout == null) {
            buildMainLayout();
        }
        return mainLayout;
    }

    /**
     *
     */
    private void buildMainLayout() {
        String id = "mainLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        mainLayout = new GridLayout(3, 6);
        mainLayout.setId(DEBUG_ID_PREFIX + id);
        mainLayout.setSizeFull();
        mainLayout.setImmediate(true);
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        // Add components to mainLayout
        mainLayout.addComponent(getProcessorLister(), 0, 0, 0, 5);
        mainLayout.addComponent(getImplementationClassField(), 1, 0);
        mainLayout.addComponent(getLoadImplementationClassButton(), 2, 0);
        mainLayout.addComponent(new Label("<p> <hr/>", ContentMode.HTML), 1, 1, 2, 1);
        mainLayout.addComponent(getPropertiesPanel(), 1, 2);
        mainLayout.addComponent(new Label("<hr/>", ContentMode.HTML), 1, 3, 2, 3);
        mainLayout.addComponent(getNavigationButton(), 2, 2);
        mainLayout.addComponent(getBulletLineLayout(), 1, 4, 2, 4);
        mainLayout.addComponent(getCommitChangesButton(), 1, 5, 2, 5);

        mainLayout.setComponentAlignment(
                getImplementationClassField(), Alignment.BOTTOM_LEFT);
        mainLayout.setComponentAlignment(
                getLoadImplementationClassButton(), Alignment.BOTTOM_RIGHT);
        mainLayout.setComponentAlignment(
                getNavigationButton(), Alignment.MIDDLE_RIGHT);
        mainLayout.setComponentAlignment(
                getBulletLineLayout(), Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(
                getCommitChangesButton(), Alignment.BOTTOM_RIGHT);

        mainLayout.setColumnExpandRatio(0, 0.3f);
        mainLayout.setColumnExpandRatio(1, 0.69f);
        mainLayout.setColumnExpandRatio(2, 0.01f);
    }

    /**
     *
     * @return
     */
    public final ListSelect getProcessorLister() {
        if (processorLister == null) {
            buildProcessorLister();
        }
        return processorLister;
    }

    /**
     *
     */
    private void buildProcessorLister() {
        String id = "processorLister";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        processorLister = new ListSelect("AVAILABLE PROCESSORS");
        processorLister.setId(DEBUG_ID_PREFIX + id);
        processorLister.setWidth("95%");
        processorLister.setHeight("100%");
        processorLister.setImmediate(true);
        processorLister.setNullSelectionAllowed(false);
        processorLister.addStyleName(CSSTokenContainer.BOLD_CAPTION);

        processorLister.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                ListSelection listSelection = validateListSelection();
                switch (validateListSelection()) {
                    case NO:
                        fireNoListEntrySelected();
                        break;
                    case NEW:
                        fireNewInstanceSelected();
                        break;
                    case VALID:
                        fireValidListEntrySelected();
                        break;
                    case INVALID:
                        fireInvalidListEntrySelected();
                        break;
                    default:
                        UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                                + "'Staging Access Point'. " + NoteBuilder.CONTACT, -1);
                        LOGGER.error("Failed to update " + this.getClass().getSimpleName()
                                + ". Cause: Undefined enum constant detected, namely '"
                                + listSelection.name() + "'.");
                        break;
                }
            }
        });
    }

    /**
     *
     */
    private ListSelection validateListSelection() {
        // Get selected list entry
        String processorUniqueId = (String) getProcessorLister().getValue();

        // Validate selection
        if (processorUniqueId == null) {
            return ListSelection.NO;
        }

        if (NEW_PROCESSOR_UNIQUE_ID.equals(processorUniqueId)) {
            selectedProcessor = StagingProcessor.factoryNewStagingProcessor();
            return ListSelection.NEW;
        }

        // Find selected processor in the database
        selectedProcessor = StagingConfigurationPersistence
                .getSingleton(null).findStagingProcessorById(processorUniqueId);

        if (selectedProcessor == null) {
            selectedProcessor = StagingProcessor.factoryNewStagingProcessor();
            return ListSelection.INVALID;
        } else {
            return ListSelection.VALID;
        }
    }

    /**
     *
     */
    private void fireNewInstanceSelected() {
        resetConfigurationComponents();
        setEnabledComponents(ListSelection.NEW);
    }

    /**
     *
     */
    private void fireValidListEntrySelected() {
        getImplementationClassField().setReadOnly(false);
        getImplementationClassField().setValue(selectedProcessor.getImplementationClass());
        try {
            AbstractStagingProcessor processorInstance = createProcessorInstance(
                    selectedProcessor.getImplementationClass());
            getBasicPropertiesLayout().updateComponents(selectedProcessor);
            getSpecificPropertiesLayout().updateComponents(processorInstance,
                    selectedProcessor.getPropertiesAsObject());
            setEnabledComponents(ListSelection.VALID);
        } catch (ConfigurationException ex) {
            parentApp.showError("Update of 'Staging Processor' not possible! Cause: " + ex.getMessage());
            LOGGER.error("Failed to update '" + StagingProcessorConfigurationTab.class.getSimpleName()
                    + "'. Cause: " + ex.getMessage(), ex);
            resetConfigurationComponents();
            setEnabledComponents(ListSelection.INVALID);
        } catch (UIComponentUpdateException | IOException ex) {
            parentApp.showError("Update of 'Staging Processor' not possible! Cause: " + ex.getMessage());
            LOGGER.error(MsgBuilder.updateFailed(getPropertiesPanel().getId()) + "Cause: " + ex.getMessage(), ex);
            setEnabledComponents(ListSelection.INVALID);
        }
    }

    /**
     *
     */
    private void fireInvalidListEntrySelected() {
        parentApp.showWarning("Processor not found in database.");
        resetConfigurationComponents();
        setEnabledComponents(ListSelection.INVALID);
    }

    /**
     *
     */
    private void fireNoListEntrySelected() {
        resetConfigurationComponents();
        setEnabledComponents(ListSelection.NO);
    }

    /**
     *
     */
    private void resetConfigurationComponents() {
        getImplementationClassField().setReadOnly(false);
        getImplementationClassField().setValue(
                selectedProcessor.getImplementationClass());
        getBasicPropertiesLayout().reset();
        getSpecificPropertiesLayout().reset();
        setPropertiesLayout(PropertiesLayout.BASIC);
    }

    /**
     *
     * @return
     */
    public TextField getImplementationClassField() {
        if (implementationClassField == null) {
            buildImplementationClassField();
        }
        return implementationClassField;
    }

    /**
     *
     */
    private void buildImplementationClassField() {
        String id = "implementationClassField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        implementationClassField = new TextField("PROCESSOR IMPLEMENTATION CLASS");
        implementationClassField.setId(DEBUG_ID_PREFIX + id);
        implementationClassField.setImmediate(true);
        implementationClassField.setWidth("100%");
        implementationClassField.setRequired(true);
        implementationClassField.setNullRepresentation("");
        implementationClassField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        implementationClassField.setInputPrompt(
                "Enter (Java) classpath of processor implementation ...");
    }

    /**
     *
     * @return
     */
    public NativeButton getLoadImplementationClassButton() {
        if (loadImplementationClassButton == null) {
            buildLoadImplementationClassButton();
        }
        return loadImplementationClassButton;
    }

    /**
     *
     */
    private void buildLoadImplementationClassButton() {
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
                    addProcessorListerItem(newProcessor);
                    setEnabledComponents(ListSelection.VALID);
                    parentApp.showNotification("Staging processor successfully created.");
                } catch (ConfigurationException ex) {
                    parentApp.showError("Implementation class not loadable! Cause: " + ex.getMessage());
                    LOGGER.error("Failed to load requested implementation class. Cause: "
                            + ex.getMessage(), ex);
                }
            }
        });
    }

    /**
     * Create a new processor.
     *
     * <p align="right"> <b>by</b> Jejkal</p>
     */
    private StagingProcessor createProcessor() throws ConfigurationException {
        // Validate value of implementation class field
        if (!UIUtils7.validate(getImplementationClassField())) {
            throw new ConfigurationException("Invalid implementation class.");
        }

        // Valid implementation class name => Create new processor
        String implClassName = getImplementationClassField().getValue();
        try {
            // Build instance of implementation class
            createProcessorInstance(implClassName);
        } catch (ConfigurationException ex) {
            setPropertiesLayout(PropertiesLayout.BASIC);
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
     *
     * @param implClassName
     * @return
     * @throws ConfigurationException
     */
    private AbstractStagingProcessor createProcessorInstance(String implClassName,
            StagingProcessor processor) throws ConfigurationException {
        try {
            if (processor == null) {
                return (AbstractStagingProcessor) Class.forName(implClassName)
                        .getConstructor(String.class).newInstance("123");
            }
            return (AbstractStagingProcessor) Class.forName(implClassName)
                    .getConstructor(String.class).newInstance(processor.getName());
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

    /**
     *
     * @return
     */
    public Panel getPropertiesPanel() {
        if (propertiesPanel == null) {
            buildPropertiesPanel();
        }
        return propertiesPanel;
    }

    /**
     *
     */
    private void buildPropertiesPanel() {
        String id = "propertiesPanel";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        propertiesPanel = new Panel();
        propertiesPanel.setId(DEBUG_ID_PREFIX + id);
        propertiesPanel.setHeight("300px");
        propertiesPanel.setStyleName(LiferayTheme.PANEL_LIGHT);
        propertiesPanel.setImmediate(true);

        setPropertiesLayout(PropertiesLayout.BASIC);
    }

    /**
     *
     * @return
     */
    public BasicPropertiesLayout getBasicPropertiesLayout() {
        if (basicPropertiesLayout == null) {
            basicPropertiesLayout = new BasicPropertiesLayout(parentApp);
        }
        return basicPropertiesLayout;
    }

    /**
     *
     * @return
     */
    public SpecificPropertiesLayout getSpecificPropertiesLayout() {
        if (specificPropertiesLayout == null) {
            specificPropertiesLayout = new SpecificPropertiesLayout();
        }
        return specificPropertiesLayout;
    }

    /**
     *
     * @return
     */
    public NativeButton getNavigationButton() {
        if (navigationButton == null) {
            buildNavigationButton();
        }
        return navigationButton;
    }

    /**
     *
     */
    private void buildNavigationButton() {
        String id = "navigationButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        navigationButton = new NativeButton();
        navigationButton.setId(DEBUG_ID_PREFIX + id);
        navigationButton.setIcon(new ThemeResource(IconContainer.NAVIGATE_RIGHT));
        navigationButton.setStyleName(BaseTheme.BUTTON_LINK);
        navigationButton.setImmediate(true);

        navigationButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                switch (getPropertiesLayout()) {
                    case BASIC:
                        setPropertiesLayout(PropertiesLayout.SPECIFIC);
                        break;
                    case SPECIFIC:
                        setPropertiesLayout(PropertiesLayout.BASIC);
                        break;
                    default:
                        UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                                + "'Staging Access Point'. " + NoteBuilder.CONTACT, -1);
                        LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                                + ". Cause: Undefined enum constant detected, namely '"
                                + getPropertiesLayout().name() + "'.");
                        break;
                }
            }
        });
    }

    /**
     *
     * @return
     */
    public HorizontalLayout getBulletLineLayout() {
        if (bulletLineLayout == null) {
            buildBulletLineLayout();
        }
        return bulletLineLayout;
    }

    /**
     *
     */
    private void buildBulletLineLayout() {
        String id = "bulletLineLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        bulletLineLayout = new HorizontalLayout();
        bulletLineLayout.setId(DEBUG_ID_PREFIX + id);
        bulletLineLayout.setImmediate(true);

        bulletLineLayout.addComponent(getBulletBasic());
        bulletLineLayout.addComponent(getBulletSpecific());
    }

    /**
     * @return the bulletBasic
     */
    public NativeButton getBulletBasic() {
        if (bulletBasic == null) {
            buildBulletBasic();
        }
        return bulletBasic;
    }

    /**
     *
     */
    private void buildBulletBasic() {
        String id = "bulletBasic";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        bulletBasic = new NativeButton();
        bulletBasic.setId(DEBUG_ID_PREFIX + id);
        bulletBasic.setIcon(new ThemeResource(IconContainer.BULLET_GREEN));
        bulletBasic.setStyleName(BaseTheme.BUTTON_LINK);
        bulletBasic.setWidth("17px");

        bulletBasic.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                switch (getPropertiesLayout()) {
                    case BASIC:
                        break;
                    case SPECIFIC:
                        setPropertiesLayout(PropertiesLayout.BASIC);
                        break;
                    default:
                        UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                                + "'Staging Access Point'. " + NoteBuilder.CONTACT, -1);
                        LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                                + ". Cause: Undefined enum constant detected, namely '"
                                + getPropertiesLayout().name() + "'.");
                        break;
                }
            }
        });
    }

    /**
     * @return the bulletSpecific
     */
    public NativeButton getBulletSpecific() {
        if (bulletSpecific == null) {
            buildBulletSpecific();
        }
        return bulletSpecific;
    }

    /**
     *
     */
    private void buildBulletSpecific() {
        String id = "bulletSpecific";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        bulletSpecific = new NativeButton();
        bulletSpecific.setId(DEBUG_ID_PREFIX + id);
        bulletSpecific.setIcon(new ThemeResource(IconContainer.BULLET_GREY));
        bulletSpecific.setStyleName(BaseTheme.BUTTON_LINK);
        bulletSpecific.setWidth("17px");

        bulletSpecific.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                switch (getPropertiesLayout()) {
                    case BASIC:
                        setPropertiesLayout(PropertiesLayout.SPECIFIC);
                        break;
                    case SPECIFIC:
                        break;
                    default:
                        UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                                + "'Staging Access Point'. " + NoteBuilder.CONTACT, -1);
                        LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                                + ". Cause: Undefined enum constant detected, namely '"
                                + getPropertiesLayout().name() + "'.");
                        break;
                }
            }
        });
    }

    /**
     *
     * @return
     */
    public NativeButton getCommitChangesButton() {
        if (commitChangesButton == null) {
            buildCommitChangesButton();
        }
        return commitChangesButton;
    }

    /**
     *
     */
    private void buildCommitChangesButton() {
        String id = "commitChangesButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        commitChangesButton = new NativeButton("Commit Changes");
        commitChangesButton.setId(DEBUG_ID_PREFIX + id);
        commitChangesButton.setImmediate(true);

        commitChangesButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    validateRequiredComponentValues();
                    StagingProcessor changedProcessor = changeProcessor(selectedProcessor);
                    checkProcessorConfigurability(changedProcessor);
                    commitProcessor(changedProcessor);
                    updateProcessorListerItem(changedProcessor);
                    parentApp.showNotification("Changes successfully committed.");
                } catch (ConfigurationException ex) {
                    parentApp.showError("Staging processor not modifiable! Cause: " + ex.getMessage());
                    String object = "the changed processor '" + selectedProcessor.getUniqueIdentifier() + "'";
                    LOGGER.error(MsgBuilder.commitFailed(object) + "Cause: " + ex.getMessage(), ex);
                }
            }
        });
    }

    /**
     *
     * @param processor
     */
    public void addProcessorListerItem(StagingProcessor processor) {
        getProcessorLister().addItem(processor.getUniqueIdentifier());
        getProcessorLister().setItemCaption(processor.getUniqueIdentifier(),
                getProcessorItemCaption(processor));
        getProcessorLister().select(processor.getUniqueIdentifier());
    }

    /**
     *
     * @param processor
     */
    public void updateProcessorListerItem(StagingProcessor processor) {
        getProcessorLister().removeItem(processor.getUniqueIdentifier());
        addProcessorListerItem(processor);
    }

    /**
     * Check the currently selected staging processor. During this check, some
     * properties will be validated. For some minor issues, warnings are
     * generated, which are returned as result. For major issues, an exception
     * is thrown. If 'null' is returned, an internal error which is indicated in
     * an own way (e.g. validation of a text field has failed) has occured. If
     * the staging processor is valid, an empty string is returned.
     *
     * @return En empty string in case of success, a string which contains all
     * warnings or 'null' in case of an internal validation error.
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
        if (UIComponentTools.isEmpty(getImplementationClassField())
                || !UIUtils7.validate(getImplementationClassField())) {
            setPropertiesLayout(PropertiesLayout.BASIC);
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
            setPropertiesLayout(PropertiesLayout.BASIC);
            throw new ConfigurationException("Processor name is invalid.");
        }

        StagingProcessor existingProcessor = StagingConfigurationPersistence.getSingleton(null).
                findStagingProcessorByName(getBasicPropertiesLayout().getNameField().getValue());

        if (existingProcessor != null && !existingProcessor.getUniqueIdentifier()
                .equals(selectedProcessor.getUniqueIdentifier())) {
            setPropertiesLayout(PropertiesLayout.BASIC);
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
        String implClass = getImplementationClassField().getValue();
        AbstractStagingProcessor processorInstance = createProcessorInstance(implClass, processor);
        try {
            processorInstance.validateProperties(getSpecificPropertiesLayout().getProperties());
        } catch (PropertyValidationException ex) {
            setPropertiesLayout(PropertiesLayout.SPECIFIC);
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
        processor.setDescription(
                (String) getBasicPropertiesLayout().getDescriptionArea().getValue());
        processor.setGroupId((String) getBasicPropertiesLayout().getGroupBox().getValue());
        processor.setType((StagingProcessor.PROCESSOR_TYPE) getBasicPropertiesLayout()
                .getProcessorTypeBox().getValue());
        try {
            processor.setPropertiesFromObject(getSpecificPropertiesLayout().getProperties());
        } catch (IOException ex) {
            setPropertiesLayout(PropertiesLayout.SPECIFIC);
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
            setPropertiesLayout(PropertiesLayout.BASIC);
            String object = "processor '" + processor.getName() + "'";
            throw new ConfigurationException(MsgBuilder.unauthorizedSaveRequest(object), ex);
        }
    }

    /**
     *
     * @param processorId
     */
    private void reloadProcessorLister() {
        getProcessorLister().removeAllItems();

        getProcessorLister().addItem(NEW_PROCESSOR_UNIQUE_ID);
        getProcessorLister().setItemCaption(NEW_PROCESSOR_UNIQUE_ID,
                NEW_PROCESSOR_CAPTION);

        List<StagingProcessor> processors = StagingConfigurationPersistence
                .getSingleton(null).findAllStagingProcessors();
        for (StagingProcessor processor : processors) {
            getProcessorLister().addItem(processor.getUniqueIdentifier());
            getProcessorLister().setItemCaption(processor.getUniqueIdentifier(),
                    getProcessorItemCaption(processor));
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

    /**
     *
     * @param listSelection
     */
    private void setEnabledComponents(ListSelection listSelection) {
        switch (listSelection) {
            case NO:
                getImplementationClassField().setReadOnly(true);
                getLoadImplementationClassButton().setEnabled(false);
                getPropertiesPanel().setEnabled(false);
                getCommitChangesButton().setEnabled(false);
                break;
            case NEW:
                getImplementationClassField().setReadOnly(false);
                getLoadImplementationClassButton().setEnabled(true);
                getPropertiesPanel().setEnabled(false);
                getCommitChangesButton().setEnabled(false);
                break;
            case VALID:
                getImplementationClassField().setReadOnly(true);
                getLoadImplementationClassButton().setEnabled(false);
                getPropertiesPanel().setEnabled(true);
                getCommitChangesButton().setEnabled(true);
                break;
            case INVALID:
                getImplementationClassField().setReadOnly(true);
                getLoadImplementationClassButton().setEnabled(false);
                getPropertiesPanel().setEnabled(false);
                getCommitChangesButton().setEnabled(false);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'Staging Access Point'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                        + ". Cause: Undefined enum constant detected, namely '"
                        + listSelection.name() + "'.");
                break;
        }
    }

    /**
     *
     * @return
     */
    private PropertiesLayout getPropertiesLayout() {
        return propertiesLayout;
    }

    /**
     *
     * @param propertiesLayout
     */
    private void setPropertiesLayout(PropertiesLayout propertiesLayout) {
        this.propertiesLayout = propertiesLayout;
        switch (propertiesLayout) {
            case BASIC:
                fireBasicPropertiesLayoutSelected();
                break;
            case SPECIFIC:
                fireSpecificPropertiesLayoutSelected();
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "'Staging Access Point'. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                        + ". Cause: Undefined enum constant detected, namely '"
                        + propertiesLayout.name() + "'.");
                break;
        }
    }

    /**
     *
     */
    private void fireBasicPropertiesLayoutSelected() {
        getPropertiesPanel().setContent(getBasicPropertiesLayout());
        getBulletBasic().setIcon(new ThemeResource(IconContainer.BULLET_GREEN));
        getBulletSpecific().setIcon(new ThemeResource(IconContainer.BULLET_GREY));
    }

    /**
     *
     */
    private void fireSpecificPropertiesLayoutSelected() {
        getPropertiesPanel().setContent(getSpecificPropertiesLayout());
        getBulletSpecific().setIcon(new ThemeResource(IconContainer.BULLET_GREEN));
        getBulletBasic().setIcon(new ThemeResource(IconContainer.BULLET_GREY));
    }

    /**
     *
     */
    public void reload() {
        reloadProcessorLister();
        getProcessorLister().select(NEW_PROCESSOR_UNIQUE_ID);
        getBasicPropertiesLayout().reloadGroupBox();
        getBasicPropertiesLayout().getGroupBox().select(USERS_GROUP_ID);
    }

    /**
     *
     */
    public void disable() {
        getProcessorLister().removeAllItems();
        getBasicPropertiesLayout().getGroupBox().removeAllItems();
        setEnabled(false);
    }

    /**
     *
     */
    public void enable() {
        reload();
        setEnabled(true);
    }

    /**
     *
     */
    public final void update() {
        Role loggedInUserRole = parentApp.getLoggedInUser().getCurrentRole();
        switch (loggedInUserRole) {
            case ADMINISTRATOR:
            case MANAGER:
                if (!isEnabled()) {
                    enable();
                }
                break;
            default:
                if (isEnabled()) {
                    disable();
                }
                UIComponentTools.showWarning("WARNING", "Unauthorized access attempt! " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + this.getId() + ". Cause: Unauthorized access attempt!");
                break;
        }
        reload();
    }
}
