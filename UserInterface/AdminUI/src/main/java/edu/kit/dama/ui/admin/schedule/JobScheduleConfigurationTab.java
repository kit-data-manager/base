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
package edu.kit.dama.ui.admin.schedule;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.zybnet.autocomplete.server.AutocompleteField;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.scheduler.SchedulerManagement;
import edu.kit.dama.scheduler.api.impl.QuartzExpressionTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzIntervalTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzSchedule;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.JobTrigger;
import edu.kit.dama.scheduler.manager.ISchedulerManager;
import edu.kit.dama.scheduler.quartz.jobs.AbstractConfigurableJob;
import edu.kit.dama.ui.admin.AbstractConfigurationTab;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.admin.workflow.DataWorkflowTaskConfigurationTab;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
 * @author jejkal
 */
public final class JobScheduleConfigurationTab extends AbstractConfigurationTab<SimpleSchedule> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataWorkflowTaskConfigurationTab.class);

    private String DEBUG_ID_PREFIX = JobScheduleConfigurationTab.class.getName() + "_";

    public static final String NEW_SCHEDULE_CAPTION = "NEW";
    public static final String NEW_SCHEDULE_UNIQUE_ID = "MINUS_ONE_SCHEDULE";
    public static final String UNSCHEDULED_JOB_UNIQUE_ID = "UNSCHEDULED";

    private GridLayout mainLayout;
    private AutocompleteField<String> implementationClassField;
    private Button loadImplementationClassButton;
    private SchedulerBasePropertiesLayout basicPropertiesLayout;

    public JobScheduleConfigurationTab() {
        super();
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
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        mainLayout.setColumnExpandRatio(1, 1f);
        mainLayout.setRowExpandRatio(1, 1f);

        return mainLayout;
    }

    /**
     * Generated the schedule caption in the format NAME@GROUP if group exists,
     * otherwise the caption is only the name.
     *
     * @param pSchedule The schedule for which to obtain the caption.
     *
     * @return The caption.
     */
    private String getScheduleCaption(SimpleSchedule pSchedule) {
        String result = pSchedule.getName();
        return result + ((pSchedule.getScheduleGroup() != null) ? "@" + pSchedule.getScheduleGroup() : "");
    }

    @Override
    public boolean elementWithIdExists(String pId) {
        return loadElementById(pId) != null;
    }

    @Override
    public SimpleSchedule loadElementById(String pId) {
        SimpleSchedule result = null;
        if (NEW_UNIQUE_ID.equals(pId) || UNSCHEDULED_JOB_UNIQUE_ID.equals(pId)) {
            //create 'new' element
            result = new QuartzSchedule();
            result.setId(pId);
            if (getImplementationClassField().getText() != null) {
                result.setJobClass(getImplementationClassField().getText());
            }
        } else {
            ISchedulerManager manager = SchedulerManagement.getSchedulerManagement().getSchedulerManager();
            try {
                result = manager.getScheduleById(pId);
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Failed to obtain schedule for id " + pId, ex);
            }
        }
        return result;
    }

    @Override
    public void fillElementList() {
        ISchedulerManager manager = SchedulerManagement.getSchedulerManagement().getSchedulerManager();
        List<SimpleSchedule> schedules = new LinkedList<>();
        try {
            schedules = manager.getAllSchedules();
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain list of schedules.", ex);
        }

        Collections.sort(schedules, (SimpleSchedule o1, SimpleSchedule o2) -> {
            int result = o1.getScheduleGroup().compareToIgnoreCase(o2.getScheduleGroup());
            if (result == 0) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }

            return result;
        });

        for (SimpleSchedule schedule : schedules) {
            getElementList().addItem(schedule.getId());
            getElementList().setItemCaption(schedule.getId(), getScheduleCaption(schedule));
        }
    }

    @Override
    public void resetComponents() {
        getImplementationClassField().setReadOnly(false);
        SimpleSchedule selection = loadElementById(getSelectedElementId());

        if (selection == null || selection.getJobClass() == null || UNSCHEDULED_JOB_UNIQUE_ID.equals(selection.getId()) || NEW_UNIQUE_ID.equals(getSelectedElementId())) {
            getImplementationClassField().clearChoices();
            getImplementationClassField().setValue(null);
        } else {
            getImplementationClassField().setText(selection.getJobClass());
            getImplementationClassField().setValue(selection.getJobClass());
        }
    }

    @Override
    public SchedulerBasePropertiesLayout getBasicPropertiesLayout() {
        if (basicPropertiesLayout == null) {
            basicPropertiesLayout = new SchedulerBasePropertiesLayout();
        }
        return basicPropertiesLayout;
    }

    @Override
    public void addNewElementInstance(SimpleSchedule pElementToAdd) {
        getElementList().addItem(pElementToAdd.getId());
        getElementList().setItemCaption(pElementToAdd.getId(), getScheduleCaption(pElementToAdd));
        getElementList().select(pElementToAdd.getId());
    }

    @Override
    public void updateElementInstance(SimpleSchedule pElementToUpdate) {
        getElementList().removeItem(pElementToUpdate.getId());
        addNewElementInstance(pElementToUpdate);
    }

    @Override
    public void enableComponents(boolean pValue) {
        getImplementationClassField().setReadOnly(!pValue);
        getLoadImplementationClassButton().setEnabled(pValue);
    }

    @Override
    public void selectElement(SimpleSchedule pSelectedElement) throws ConfigurationException, UIComponentUpdateException {
        implementationClassField.setReadOnly(false);
        if (pSelectedElement != null && pSelectedElement.getJobClass() != null && !pSelectedElement.getJobClass().isEmpty()) {
            getImplementationClassField().setText(pSelectedElement.getJobClass());
            getImplementationClassField().setValue(pSelectedElement.getJobClass());
            AbstractConfigurableJob jobInstance = createJobInstance(pSelectedElement.getJobClass());
            Properties props = new Properties();
            try {
                props = PropertiesUtil.propertiesFromString(pSelectedElement.getJobParameters());
            } catch (IOException ex) {
                LOGGER.error("Failed to deserialize job properties.");
            }
            getSpecificPropertiesLayout().updateComponents(jobInstance, props);
        } else {
            getImplementationClassField().clearChoices();
            getSpecificPropertiesLayout().reset();
        }
    }

    @Override
    public void commitChanges() {
        try {
            validateRequiredComponentValues();
            checkScheduleConfigurability();

            ISchedulerManager manager = SchedulerManagement.getSchedulerManagement().getSchedulerManager();
            SimpleSchedule collectSchedule = new QuartzSchedule();
            collectSchedule.setName(getBasicPropertiesLayout().getNameField().getValue());
            collectSchedule.setDescription(getBasicPropertiesLayout().getDescriptionArea().getValue());
            collectSchedule.setScheduleGroup(getBasicPropertiesLayout().getGroupField().getValue());
            collectSchedule.setJobClass(getImplementationClassField().getText());
            collectSchedule.setJobParameters(PropertiesUtil.propertiesToString(getSpecificPropertiesLayout().getProperties()));
            List<JobTrigger> storedTriggers = new ArrayList<>();
            String jobId = getBasicPropertiesLayout().getIdField().getValue();
            if (!NEW_UNIQUE_ID.equals(jobId) && !UNSCHEDULED_JOB_UNIQUE_ID.equals(jobId)) {
                //probably exists....remove first.
                LOGGER.debug("Removing existing job with id '{}' in order to allow update.", jobId);
                storedTriggers = manager.getTriggersByScheduleId(jobId);
                manager.removeSchedule(jobId);
                getElementList().removeItem(jobId);
                LOGGER.debug("Storing job with id '{}'", jobId);
            } else {
                LOGGER.debug("Storing new job.");
            }
            LOGGER.debug("Scheduling job.");
            SimpleSchedule result = manager.addSchedule(collectSchedule);
            if (!storedTriggers.isEmpty()) {
                for (JobTrigger trigger : storedTriggers) {
                    LOGGER.debug("Adding new trigger.");
                    if (trigger instanceof QuartzExpressionTrigger) {
                        LOGGER.debug("Restoring expression trigger.");
                    } else if (trigger instanceof QuartzIntervalTrigger) {
                        LOGGER.debug("Restoring interval trigger.");
                    } else {
                        LOGGER.debug("Restoring misc trigger.");
                    }
                    manager.addTrigger(jobId, trigger);
                }
            }

            UIComponentTools.showInformation("Job successfully stored with id '" + result.getId() + "'.");
            getElementList().removeItem(UNSCHEDULED_JOB_UNIQUE_ID);
            addNewElementInstance(result);
        } catch (ConfigurationException | IOException | UnauthorizedAccessAttemptException ex) {
            UIComponentTools.showError("Failed to schedule job. Cause: " + ex.getMessage());
            String object = "the provided job";
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
     * Get the implementation class field.
     *
     * @return The implementation class field.
     */
    private AutocompleteField getImplementationClassField() {
        if (implementationClassField == null) {
            String id = "implementationClassField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            implementationClassField = new AutocompleteField<>();
            implementationClassField.setCaption("JOB IMPLEMENTATION CLASS");
            implementationClassField.setId(DEBUG_ID_PREFIX + id);
            implementationClassField.setImmediate(true);
            implementationClassField.setWidth("100%");
            implementationClassField.setRequired(true);
            implementationClassField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            implementationClassField.setMinimumQueryCharacters(3);
            implementationClassField.addStyleName("v-textfield");

            implementationClassField.setQueryListener((AutocompleteField<String> field, String query) -> {
                Reflections reflections = new Reflections(query);
                Set<Class<? extends AbstractConfigurableJob>> jobs = reflections.getSubTypesOf(AbstractConfigurableJob.class);
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
                    SimpleSchedule newSchedule = createSchedule();
                    newSchedule.setId(UNSCHEDULED_JOB_UNIQUE_ID);
                    addNewElementInstance(newSchedule);
                    setEnabledComponents(ListSelection.VALID);
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
     * Create a new SimpleSchedule instance for the provided implementation
     * class with the implementation class name as name.
     *
     * @return The schedule.
     *
     * @throws ConfigurationException If the creation has failed, e.g. due to an
     * invalid class.
     */
    private SimpleSchedule createSchedule() throws ConfigurationException {
        // Validate value of implementation class field
        String implClass = getImplementationClassField().getText();
        if (implClass == null) {
            throw new ConfigurationException("Please use the auto-completion feature to select an implementation class.");
        }

        // Valid implementation class name => Create new job instance
        createJobInstance(implClass);
        //success...create schedule
        QuartzSchedule schedule = new QuartzSchedule(implClass.substring(implClass.lastIndexOf(".") + 1));
        schedule.setJobClass(implClass);
        return schedule;
    }

    /**
     * Create a new job instance using the provided class name. This method is
     * mainly used for validation and to obtain job-specific arguments and their
     * descriptions.
     *
     * @param implClassName The fully qualifies class name.
     *
     * @return The job instance.
     *
     * @throws ConfigurationException If the creation has failed.
     */
    private AbstractConfigurableJob createJobInstance(String implClassName) throws ConfigurationException {
        try {
            Class clazz = Class.forName(implClassName);
            return (AbstractConfigurableJob) clazz.getConstructor().newInstance();
        } catch (ClassNotFoundException ex) {
            String msg = "Implementation class '" + implClassName + "' not found.";
            throw new ConfigurationException(msg, ex);
        } catch (ClassCastException ex) {
            String msg = "Implementation class '" + implClassName + "' does not implement AbstractConfigurableJob.";
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
     * Check the name field for being valid.
     *
     * @throws ConfigurationException If the value of the name field is not
     * valid.
     */
    private void checkNameField() throws ConfigurationException {
        if (UIComponentTools.isEmpty(getBasicPropertiesLayout().getNameField())
                || !UIUtils7.validate(getBasicPropertiesLayout().getNameField())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Schedule name is invalid.");
        }
    }

    /**
     *
     * @throws ConfigurationException
     */
    private void checkScheduleConfigurability() throws ConfigurationException {
        checkImplementationClassField();
        SimpleSchedule scheduleInstance = createSchedule();
        try {
            AbstractConfigurableJob jobInstance = createJobInstance(scheduleInstance.getJobClass());
            jobInstance.validateProperties(getSpecificPropertiesLayout().getProperties());
        } catch (PropertyValidationException ex) {
            setPropertiesLayout(PropertiesLayoutType.SPECIFIC);
            throw new ConfigurationException("Failed to validate the staging processor properties. " + ex.getMessage(), ex);
        }
    }

}
