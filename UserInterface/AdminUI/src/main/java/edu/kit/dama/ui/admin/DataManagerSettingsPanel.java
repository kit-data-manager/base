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
package edu.kit.dama.ui.admin;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.ui.admin.administration.usergroup.UserGroupAdministrationTab;
import edu.kit.dama.ui.admin.staging.accesspoints.StagingAccessPointConfigurationTab;
import edu.kit.dama.ui.admin.staging.processors.StagingProcessorConfigurationTab;
import edu.kit.dama.ui.admin.administration.user.UserDataAdministrationTab;
import edu.kit.dama.ui.admin.schedule.JobScheduleConfigurationTab;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.admin.workflow.DataWorkflowTaskConfigurationTab;
import edu.kit.dama.ui.admin.workflow.ExecutionEnvironmentConfigurationTab;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class DataManagerSettingsPanel extends CustomComponent {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(DataManagerSettingsPanel.class);

    public final static String DEBUG_ID_PREFIX = DataManagerSettingsPanel.class.getName() + "_";
    private TabSheet mainComponentContainer;
    private VerticalLayout mainLayout = null;
    private UserDataAdministrationTab userAdministrationTab;
    private UserGroupAdministrationTab groupAdministrationTab;
    private StagingAccessPointConfigurationTab accessPointConfigurationTab;
    private StagingProcessorConfigurationTab processorConfigurationTab;
    private DataWorkflowTaskConfigurationTab dataWorkflowConfigurationTab;
    private ExecutionEnvironmentConfigurationTab executionEnvironmentConfigurationTab;
    private JobScheduleConfigurationTab jobScheduleConfigurationTab;

    public DataManagerSettingsPanel() {
        LOGGER.debug(new StringBuilder("Building ").append(DEBUG_ID_PREFIX).append(" ...").toString());

        setId(DEBUG_ID_PREFIX);
        buildMainComponentContainer();
        setCompositionRoot(mainLayout);
        getUserAdministrationTab().update();
        setSizeFull();
    }

    private void buildMainComponentContainer() {
        String id = "mainComponent";
        LOGGER.debug(new StringBuilder("Building ").append(
                DEBUG_ID_PREFIX).append(id).append(" ...").toString());

        mainComponentContainer = new TabSheet();
        mainComponentContainer.setId(new StringBuilder(DEBUG_ID_PREFIX).append(id).toString());
        mainComponentContainer.setSizeFull();

        // Add tab for user administration
        mainComponentContainer.addTab(getUserAdministrationTab(), "User Administration",
                new ThemeResource(IconContainer.USERS));
        mainComponentContainer.addTab(getGroupAdministrationTab(), "Group Administration",
                new ThemeResource(IconContainer.GROUPS));
        mainComponentContainer.addTab(getAccessPointConfigurationTab(),
                "Staging Access Points", new ThemeResource(IconContainer.ACCESS_POINT));
        mainComponentContainer.addTab(getProcessorConfigurationTab(),
                "Staging Processors", new ThemeResource(IconContainer.STAGING_PROCESSOR));
        mainComponentContainer.addTab(getExecutionEnvironmentConfigurationTab(),
                "Execution Environments", new ThemeResource(IconContainer.EXECUTION_ENVIRONMENT));
        mainComponentContainer.addTab(getDataWorkflowConfigurationTab(),
                "Data Workflow Tasks", new ThemeResource(IconContainer.DATA_WORKFLOW));
        mainComponentContainer.addTab(getJobScheduleConfigurationTab(),
                "Job Scheduling", new ThemeResource(IconContainer.JOB_SCHEDULE));
        // Add listener for tab change
        mainComponentContainer.addSelectedTabChangeListener((TabSheet.SelectedTabChangeEvent event) -> {
            if (!event.getTabSheet().getSelectedTab().isEnabled()) {
                UIComponentTools.showError("You are not authorized to access or modify these settings.");
                return;
            }
            if (event.getTabSheet().getSelectedTab().equals(userAdministrationTab)) {
                userAdministrationTab.update();
            } else if (event.getTabSheet().getSelectedTab().equals(groupAdministrationTab)) {
                groupAdministrationTab.update();
            } else if (event.getTabSheet().getSelectedTab().equals(accessPointConfigurationTab)) {
                accessPointConfigurationTab.update();
            } else if (event.getTabSheet().getSelectedTab().equals(processorConfigurationTab)) {
                processorConfigurationTab.update();
            } else if (event.getTabSheet().getSelectedTab().equals(dataWorkflowConfigurationTab)) {
                dataWorkflowConfigurationTab.update();
            } else if (event.getTabSheet().getSelectedTab().equals(executionEnvironmentConfigurationTab)) {
                executionEnvironmentConfigurationTab.update();
            } else if (event.getTabSheet().getSelectedTab().equals(jobScheduleConfigurationTab)) {
                jobScheduleConfigurationTab.update();
            }

            if (!event.getTabSheet().getSelectedTab().isEnabled()) {
                //check again and show error if privileges have changed...in that case recommend a reload
                UIComponentTools.showError("You are not authorized to access or modify these settings. Please reload the page.");
            }
        });

        mainLayout = new VerticalLayout(mainComponentContainer);
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();
    }

    /**
     * @return the mainComponentContainer
     */
    public TabSheet getMainComponentContainer() {
        if (mainComponentContainer == null) {
            buildMainComponentContainer();
        }
        return mainComponentContainer;
    }

    /**
     * @return the userAdministrationTab
     */
    public UserDataAdministrationTab getUserAdministrationTab() {
        if (userAdministrationTab == null) {
            userAdministrationTab = new UserDataAdministrationTab();
        }
        return userAdministrationTab;
    }

    /**
     * @return the groupAdministrationTab
     */
    public UserGroupAdministrationTab getGroupAdministrationTab() {
        if (groupAdministrationTab == null) {
            groupAdministrationTab = new UserGroupAdministrationTab();
        }
        return groupAdministrationTab;
    }

    /**
     * @return the accessPointConfigurationTab
     */
    public StagingAccessPointConfigurationTab getAccessPointConfigurationTab() {
        if (accessPointConfigurationTab == null) {
            accessPointConfigurationTab = new StagingAccessPointConfigurationTab();
        }
        return accessPointConfigurationTab;
    }

    /**
     * @return the processorConfigurationTab
     */
    public StagingProcessorConfigurationTab getProcessorConfigurationTab() {
        if (processorConfigurationTab == null) {
            processorConfigurationTab = new StagingProcessorConfigurationTab();
        }
        return processorConfigurationTab;
    }

    /**
     * @return the dataWorkflowConfigurationTab
     */
    public DataWorkflowTaskConfigurationTab getDataWorkflowConfigurationTab() {
        if (dataWorkflowConfigurationTab == null) {
            dataWorkflowConfigurationTab = new DataWorkflowTaskConfigurationTab();
        }
        return dataWorkflowConfigurationTab;
    }

    /**
     * @return the executionEnvironmentConfigurationTab
     */
    public ExecutionEnvironmentConfigurationTab getExecutionEnvironmentConfigurationTab() {
        if (executionEnvironmentConfigurationTab == null) {
            executionEnvironmentConfigurationTab = new ExecutionEnvironmentConfigurationTab();
        }
        return executionEnvironmentConfigurationTab;
    }

    /**
     * @return the dataWorkflowConfigurationTab
     */
    public JobScheduleConfigurationTab getJobScheduleConfigurationTab() {
        if (jobScheduleConfigurationTab == null) {
            jobScheduleConfigurationTab = new JobScheduleConfigurationTab();
        }
        return jobScheduleConfigurationTab;
    }
}
