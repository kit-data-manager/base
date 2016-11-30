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
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.scheduler.SchedulerManagement;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.JobTrigger;
import edu.kit.dama.scheduler.manager.ISchedulerManager;
import edu.kit.dama.ui.admin.AbstractBasePropertiesLayout;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.schedule.trigger.AddTriggerComponent;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import static edu.kit.dama.ui.admin.workflow.DataWorkflowBasePropertiesLayout.LOGGER;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.ui.components.ConfirmationWindow7;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jejkal
 */
public final class SchedulerBasePropertiesLayout extends AbstractBasePropertiesLayout<SimpleSchedule> {

    private static final String DEBUG_ID_PREFIX = SchedulerBasePropertiesLayout.class.getName() + "_";

    private TextField idField;
    private TextField groupField;
    private Table triggerTable;
    private final AddTriggerComponent addTriggerComponent;
    private Window addTriggerWindow = null;

    /**
     * Default constructor.
     */
    public SchedulerBasePropertiesLayout() {
        super();
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));

        setSizeFull();
        setMargin(true);
        setSpacing(true);

        setColumns(3);
        setRows(3);
        //first row
        addComponent(getIdField(), 0, 0);
        addComponent(getGroupField(), 1, 0);
        addComponent(getNameField(), 2, 0);

        //second row
        addComponent(getDescriptionArea(), 0, 1, 2, 1);
        Button addTriggerButton = new Button();
        addTriggerButton.setDescription("Add a new trigger.");
        addTriggerButton.setIcon(new ThemeResource(IconContainer.ADD));
        addTriggerButton.addClickListener((Button.ClickEvent event) -> {
            addTrigger();
        });

        Button removeTriggerButton = new Button();
        removeTriggerButton.setDescription("Remove the selected trigger.");
        removeTriggerButton.setIcon(new ThemeResource(IconContainer.DELETE));
        removeTriggerButton.addClickListener((Button.ClickEvent event) -> {
            removeTrigger();
        });

        Button refreshTriggerButton = new Button();
        refreshTriggerButton.setDescription("Refresh the list of triggers.");
        refreshTriggerButton.setIcon(new ThemeResource(IconContainer.REFRESH));
        refreshTriggerButton.addClickListener((Button.ClickEvent event) -> {
            reloadTriggers();
        });

        VerticalLayout buttonLayout = new VerticalLayout(addTriggerButton, refreshTriggerButton, removeTriggerButton);
        buttonLayout.setComponentAlignment(addTriggerButton, Alignment.TOP_RIGHT);
        buttonLayout.setComponentAlignment(removeTriggerButton, Alignment.TOP_RIGHT);
        buttonLayout.setMargin(true);

        GridLayout triggerLayout = new UIUtils7.GridLayoutBuilder(2, 2).addComponent(getTriggerTable(), 0, 0, 1, 2).addComponent(buttonLayout, 1, 0, 1, 2).getLayout();
        triggerLayout.setSizeFull();
        triggerLayout.setMargin(false);
        triggerLayout.setColumnExpandRatio(0, .95f);
        triggerLayout.setColumnExpandRatio(1, .05f);
        triggerLayout.setRowExpandRatio(0, .9f);
        triggerLayout.setRowExpandRatio(1, .05f);
        triggerLayout.setRowExpandRatio(2, .05f);

        //third row
        addComponent(triggerLayout, 0, 2, 2, 2);
        addTriggerComponent = new AddTriggerComponent(this);

        setRowExpandRatio(1, .3f);
        setRowExpandRatio(2, .6f);
    }

    @Override
    public String getNameFieldLabel() {
        return "JOB NAME";
    }

    @Override
    public void reset() {
        getGroupField().setValue("");
        getIdField().setReadOnly(false);
        getIdField().setValue("");
        getIdField().setReadOnly(true);
    }

    @Override
    public void updateSelection(SimpleSchedule pValue) throws UIComponentUpdateException {
        reset();

        if (pValue == null) {
            throw new UIComponentUpdateException("Invalid schedule.");
        }
        getIdField().setReadOnly(false);
        getIdField().setValue(pValue.getId());
        getIdField().setReadOnly(true);
        getNameField().setValue(pValue.getName());
        getDescriptionArea().setValue(pValue.getDescription());
        getGroupField().setValue(pValue.getScheduleGroup());

        reloadTriggers();
    }

    /**
     * Fire an addTriggerEvent if a new trigger has been created, e.g. using the
     * {@link AddTriggerComponent}. The trigger will be persisted and the
     * trigger table will be reloaded.
     *
     * @param pTrigger The trigger to add.
     */
    public void fireAddTriggerEvent(JobTrigger pTrigger) {
        ISchedulerManager manager = SchedulerManagement.getSchedulerManagement().getSchedulerManager();
        try {
            LOGGER.debug("Adding trigger to job with id '{}'", getIdField().getValue());
            manager.addTrigger(getIdField().getValue(), pTrigger);
            reloadTriggers();
        } catch (Throwable t) {
            LOGGER.error("Failed to add trigger.", t);
            ConfirmationWindow7.showConfirmation("Error", "Failed to add trigger for internal reasons. Cause: " + t.getMessage(), ConfirmationWindow7.OPTION_TYPE.OK_OPTION, ConfirmationWindow7.MESSAGE_TYPE.ERROR, null);
        }
    }

    public final void hideAddTriggerWindow() {
        if (addTriggerWindow != null) {
            addTriggerWindow.setVisible(false);
            UI.getCurrent().removeWindow(addTriggerWindow);
            addTriggerWindow = null;
        }
    }

    /**
     * Get the id field.
     *
     * @return The id field.
     */
    protected TextField getIdField() {
        if (idField == null) {
            idField = factoryTextField("JOB ID", null, false);
            idField.setInputPrompt("Not scheduled, yet.");
            idField.setValue("");
            idField.setReadOnly(true);
        }
        return idField;
    }

    /**
     * Get the group field.
     *
     * @return The group field.
     */
    protected TextField getGroupField() {
        if (groupField == null) {
            groupField = factoryTextField("JOB GROUP", null, false);
        }
        return groupField;
    }

    /**
     * Get the triggers table.
     *
     * @return The triggers table.
     */
    private Table getTriggerTable() {
        if (triggerTable == null) {
            triggerTable = new Table("TRIGGERS");
            triggerTable.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            triggerTable.setSizeFull();
            triggerTable.setImmediate(true);
            triggerTable.setSelectable(true);
            triggerTable.setNullSelectionAllowed(true);
            triggerTable.addContainerProperty("ID", String.class, null);
            triggerTable.addContainerProperty("GROUP", String.class, null);
            triggerTable.addContainerProperty("NAME", String.class, null);
            triggerTable.addContainerProperty("PRIORITY", Integer.class, null);
            triggerTable.addContainerProperty("DESCRIPTION", String.class, null);
            triggerTable.addContainerProperty("NEXT FIRE", Date.class, null);
        }
        return triggerTable;
    }

    /**
     * Reload the trigger table.
     */
    private void reloadTriggers() {
        triggerTable.removeAllItems();
        ISchedulerManager manager = SchedulerManagement.getSchedulerManagement().getSchedulerManager();
        String selectIndex = null;
        String selectedJobId = getIdField().getValue();
        try {
            List<JobTrigger> triggers = manager.getTriggersByScheduleId(selectedJobId);
            for (JobTrigger trigger : triggers) {
                triggerTable.addItem(new Object[]{trigger.getId(), trigger.getTriggerGroup(), trigger.getName(), trigger.getPriority(), trigger.getDescription(), trigger.getNextFireTime()}, trigger.getId());
                if (selectIndex == null) {
                    selectIndex = trigger.getId();
                }
            }
            triggerTable.select(selectIndex);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get triggerd for job with id '" + selectedJobId + "'.", ex);
        }
    }

    /**
     * Add a new trigger.
     */
    private void addTrigger() {
        if (JobScheduleConfigurationTab.UNSCHEDULED_JOB_UNIQUE_ID.equals(getIdField().getValue())) {
            ConfirmationWindow7.showConfirmation("Information", "Triggers can only be added to committed jobs. Please commit the job before you continue.", ConfirmationWindow7.OPTION_TYPE.OK_OPTION, ConfirmationWindow7.MESSAGE_TYPE.INFORMATION, null);
            return;
        }
        if (addTriggerWindow == null) {
            addTriggerWindow = new Window("Add Trigger");
            addTriggerWindow.setWidth(400.0f, Unit.PIXELS);
            addTriggerComponent.reset();
            addTriggerWindow.setContent(addTriggerComponent);
            addTriggerWindow.center();
            addTriggerWindow.addCloseListener((Window.CloseEvent e) -> {
                hideAddTriggerWindow();
            });
            UI.getCurrent().addWindow(addTriggerWindow);
        }//otherwise, there is already a window open
    }

    /**
     * Remove the selected trigger.
     */
    private void removeTrigger() {
        LOGGER.debug("Obtaining selected trigger for removal.");
        final String selection = (String) triggerTable.getValue();
        if (selection == null) {
            LOGGER.debug("Nothing selected.");
            return;
        }
        ConfirmationWindow7.showConfirmation("Remove Trigger", "Do you really want to remove the trigger '" + selection + "' from job '" + getIdField().getValue() + "'?", ConfirmationWindow7.OPTION_TYPE.YES_NO_OPTION, (ConfirmationWindow7.RESULT pResult) -> {
            switch (pResult) {
                case YES:
                    ISchedulerManager manager = SchedulerManagement.getSchedulerManagement().getSchedulerManager();
                    LOGGER.debug("Removing trigger with id '{}'", selection);
                    try {
                        if (manager.removeTrigger(selection)) {
                            LOGGER.debug("Trigger with id '{}' successfully removed.", selection);
                        }
                        reloadTriggers();
                    } catch (UnauthorizedAccessAttemptException ex) {
                        LOGGER.error("Not authorized to remove trigger with id '" + selection + "'.", ex);
                    }
            }
        });
    }

}
