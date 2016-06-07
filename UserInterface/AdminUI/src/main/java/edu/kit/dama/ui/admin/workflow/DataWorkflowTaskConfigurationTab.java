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

import com.vaadin.server.AbstractErrorMessage;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration;
import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.ui.admin.AbstractConfigurationTab;
import edu.kit.dama.ui.admin.AdminUIMainView;
import edu.kit.dama.ui.admin.exception.DBCommitException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.components.ConfirmationWindow7;
import edu.kit.dama.ui.components.IConfirmationWindowListener7;
import edu.kit.dama.util.Constants;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class DataWorkflowTaskConfigurationTab extends AbstractConfigurationTab<DataWorkflowTaskConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataWorkflowTaskConfigurationTab.class);
    private String DEBUG_ID_PREFIX = DataWorkflowTaskConfigurationTab.class.getName() + "_";

    private GridLayout mainLayout;
    private DataWorkflowBasePropertiesLayout basicPropertiesLayout;
    private boolean createNewWorkflowMode = false;
    private DataWorkflowTaskConfiguration selection;

    public DataWorkflowTaskConfigurationTab(AdminUIMainView pParentApp) {
        super(pParentApp);
        DEBUG_ID_PREFIX += hashCode() + "_";
    }

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
        mainLayout.addComponent(getPropertiesPanel(), 1, 0, 2, 3);

        mainLayout.addComponent(new Label("<hr/>", ContentMode.HTML), 1, 4, 2, 4);
        mainLayout.addComponent(getCommitChangesButton(), 1, 5, 2, 5);

        mainLayout.setComponentAlignment(getCommitChangesButton(), Alignment.BOTTOM_RIGHT);

        mainLayout.setColumnExpandRatio(0, 0.3f);
        mainLayout.setColumnExpandRatio(1, 0.69f);
        mainLayout.setColumnExpandRatio(2, 0.01f);
        return mainLayout;

    }

    @Override
    public void fillElementList() {
        List<DataWorkflowTaskConfiguration> configurations = new LinkedList<>();

        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            configurations = mdm.find(DataWorkflowTaskConfiguration.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain data workflow configurations", ex);
        } finally {
            mdm.close();
        }

        for (DataWorkflowTaskConfiguration configuration : configurations) {
            getElementList().addItem(Long.toString(configuration.getId()));
            getElementList().setItemCaption(Long.toString(configuration.getId()), getWorkflowConfigurationCaption(configuration));
        }
    }

    @Override
    public boolean elementWithIdExists(String pId) {
        boolean result = false;
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            result = mdm.find(DataWorkflowTaskConfiguration.class, Long.parseLong(pId)) != null;
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain data workflow configuration for id " + pId, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public DataWorkflowTaskConfiguration loadElementById(String pId) {
        DataWorkflowTaskConfiguration result = null;
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            result = mdm.find(DataWorkflowTaskConfiguration.class, Long.parseLong(pId));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain data workflow configuration for id " + pId, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public void selectElement(DataWorkflowTaskConfiguration pSelection) throws ConfigurationException, UIComponentUpdateException {
        //called if a valid element is selected.
        if (pSelection != null) {
            //Store all information of the selected workflow that may not change without changing also the version number.
            selection = new DataWorkflowTaskConfiguration();
            selection.setId(pSelection.getId());
            selection.setName(pSelection.getName());
            selection.setApplicationPackageUrl(pSelection.getApplicationPackageUrl());
            selection.setApplicationArguments(pSelection.getApplicationArguments());
        } else {
            //called if new element is selected
            DataWorkflowTaskConfiguration dummySelection = new DataWorkflowTaskConfiguration();
            dummySelection.setName("DataWorkflow " + UUID.randomUUID().toString());
            dummySelection.setVersion(1);
            dummySelection.setContactUserId("admin");
            dummySelection.setApplicationPackageUrl("file:///");
            dummySelection.setGroupId(Constants.USERS_GROUP_ID);
            dummySelection.setDisabled(Boolean.TRUE);
            dummySelection.setDefaultTask(Boolean.FALSE);
            getBasicPropertiesLayout().updateSelection(dummySelection);

            selection = null;
        }
    }

    @Override
    public void resetComponents() {
        //nothing to do here
    }

    @Override
    public void enableComponents(boolean pValue) {
        //nothing to do here
    }

    @Override
    public void setEnabledComponents(ListSelection listSelection) {
        //overwriting of this method is needed as this tab uses the properties panel for both: creating and changing elements.
        switch (listSelection) {
            case NO:
            case INVALID:
                getPropertiesPanel().setEnabled(false);
                getCommitChangesButton().setEnabled(false);
                enableComponents(false);
                createNewWorkflowMode = false;
                break;
            case NEW:
                getPropertiesPanel().setEnabled(true);
                getCommitChangesButton().setEnabled(true);
                enableComponents(true);
                createNewWorkflowMode = true;
                try {
                    selectElement(null);
                } catch (ConfigurationException | UIComponentUpdateException ex) {
                    //ignore
                }
                break;
            case VALID:
                getPropertiesPanel().setEnabled(true);
                getCommitChangesButton().setEnabled(true);
                enableComponents(true);
                createNewWorkflowMode = false;
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "element. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                        + ". Cause: Undefined enum constant detected, namely '"
                        + listSelection.name() + "'.");
                createNewWorkflowMode = false;
        }
    }

    @Override
    public void commitChanges() {
        final String applicationName = getBasicPropertiesLayout().getNameField().getValue();
        final int currentVersion = getWorkflowVersion(applicationName);
        LOGGER.debug("Committing changed to workflow configuration with name '{}' and version '{}'", applicationName, currentVersion);
        if (createNewWorkflowMode) {
            //'selection' should be null, check for existing workflow with same name

            if (currentVersion <= 0) {//if not exist, check packageUrl and contactId, persist entity and reload
                LOGGER.debug("Creating new workflow.");
                createNewWorkflowConfiguration(applicationName);
            } else {//if exists, ask for update...cancel if no update, update version otherwise 
                ConfirmationWindow7.showConfirmation(
                        "Update Workflow Configuration",
                        "There is already a workflow configuration named '" + applicationName + "' registered. "
                        + "If you continue, the existing configuration is updated to version " + (currentVersion + 1) + ". "
                        + "Do you want to continue?", ConfirmationWindow7.OPTION_TYPE.YES_NO_OPTION, ConfirmationWindow7.MESSAGE_TYPE.WARNING, new IConfirmationWindowListener7() {
                            @Override
                            public void fireConfirmationWindowCloseEvent(ConfirmationWindow7.RESULT pResult) {
                                switch (pResult) {
                                    case YES:
                                        updateWorkflowConfiguration(applicationName, currentVersion + 1);
                                        break;
                                    default:
                                    //do nothing
                                }
                            }
                        });
            }
        } else {
            //'selection' should contain the name of the currently selected workflow, compare the name field with it.
            if (applicationName.equals(selection.getName())) {
                //if names are equal, check whether update-relevant fields have changed (applicationUrl and arguments)
                updateWorkflowConfiguration(applicationName, currentVersion);
            } else {
                //if names are not equal, behave like creating a new workflow, check packageUrl and contactId, persist entity and reload.
                createNewWorkflowConfiguration(applicationName);
            }
        }
    }

    /**
     * Create a new workflow configuration using the values from the UI.
     *
     * @param pName The configuration name.
     */
    private void createNewWorkflowConfiguration(String pName) {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        boolean result = false;

        if (getWorkflowVersion(pName) > 0) {
            ConfirmationWindow7.showConfirmation(
                    "Create Workflow Configuration",
                    "There is already a workflow configuration named '" + pName + "' registered. "
                    + "Please select another name to proceed. ", ConfirmationWindow7.OPTION_TYPE.OK_OPTION, ConfirmationWindow7.MESSAGE_TYPE.WARNING, null);
            return;
        }

        try {
            LOGGER.debug("Creating new workflow configuration from UI.");
            selection = new DataWorkflowTaskConfiguration();
            selection.setName(pName);
            selection.setGroupId((String) getBasicPropertiesLayout().getGroupBox().getValue());
            String contactUserId = getBasicPropertiesLayout().getContactIdField().getValue();
            if (!checkContactUserId(contactUserId)) {
                return;
            }
            getBasicPropertiesLayout().getContactIdField().setComponentError(null);
            selection.setApplicationPackageUrl(getBasicPropertiesLayout().getApplicationPackageUrlField().getValue());
            selection.setApplicationArguments(getBasicPropertiesLayout().getApplicationArgumentsField().getValue());
            selection.setVersion(1);
            selection.setContactUserId(getBasicPropertiesLayout().getContactIdField().getValue());
            LOGGER.debug("Setting other attributes.");
            selection.setKeywords(getBasicPropertiesLayout().getKeywordsField().getValue());
            selection.setDescription(getBasicPropertiesLayout().getDescriptionArea().getValue());
            selection.setDisabled(getBasicPropertiesLayout().getDisabledBox().getValue());
            selection.setDefaultTask(getBasicPropertiesLayout().getDefaultBox().getValue());
            LOGGER.debug("Updating environment requirements.");
            selection.removeRequiredEnvironmentProperties();
            Set<Object> environmentProperties = (Set<Object>) getBasicPropertiesLayout().getEnvironmentPropertiesSelect().getValue();
            for (Object propId : environmentProperties) {
                ExecutionEnvironmentProperty property = mdm.find(ExecutionEnvironmentProperty.class, propId);
                if (property == null) {
                    LOGGER.warn("Failed to add environment property with id {}. Entry not found in database.", propId);
                    return;
                } else {
                    selection.addRequiredEnvironmentProperty(property);
                }
            }
            LOGGER.debug("Saving new workflow configuration.");
            selection = mdm.save(selection);
            LOGGER.debug("Workflow configuration '{}' successully saved.", pName);
            result = true;
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to obtain data workflow configuration for id " + selection.getId(), ex);
        } finally {
            mdm.close();
        }
        if (result) {
            addNewElementInstance(selection);
        }

    }

    /**
     * Update the workflow configuration with the provided name to the provided
     * version. A new version will be created if either the
     * applicationPackageUrl or the applicationArguments have changed. To check
     * this, the member variable 'selection' should contain those field that can
     * be compared to the current values in the UI. Otherwise, the existing
     * workflow configuration will just be updated.
     *
     * @param
     */
    private void updateWorkflowConfiguration(String pName, int pNewVersion) {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        boolean result = false;
        try {
            if (selection.getId() == null) {
                throw new DBCommitException("Id of the current selection must not be null.");
            }

            //just reload the result.
            selection = mdm.find(DataWorkflowTaskConfiguration.class, selection.getId());

            String contactUserId = getBasicPropertiesLayout().getContactIdField().getValue();
            if (!checkContactUserId(contactUserId)) {
                return;
            }
            getBasicPropertiesLayout().getContactIdField().setComponentError(null);
            selection.setContactUserId(getBasicPropertiesLayout().getContactIdField().getValue());
            selection.setGroupId((String) getBasicPropertiesLayout().getGroupBox().getValue());
            LOGGER.debug("Setting other attributes.");
            selection.setKeywords(getBasicPropertiesLayout().getKeywordsField().getValue());
            selection.setDescription(getBasicPropertiesLayout().getDescriptionArea().getValue());
            selection.setDisabled(getBasicPropertiesLayout().getDisabledBox().getValue());
            selection.setDefaultTask(getBasicPropertiesLayout().getDefaultBox().getValue());

            LOGGER.debug("Updating environment requirements.");
            selection.removeRequiredEnvironmentProperties();
            Set<Object> environmentProperties = (Set<Object>) getBasicPropertiesLayout().getEnvironmentPropertiesSelect().getValue();
            for (Object propId : environmentProperties) {
                ExecutionEnvironmentProperty property = mdm.find(ExecutionEnvironmentProperty.class, propId);
                if (property == null) {
                    LOGGER.warn("Failed to add environment property with id {}. Entry not found in database.", propId);
                } else {
                    selection.addRequiredEnvironmentProperty(property);
                }
            }
            LOGGER.debug("Checking whether to perform an update or an upgrade.");

            if (selection.getApplicationPackageUrl().equals(getBasicPropertiesLayout().getApplicationPackageUrlField().getValue())
                    && selection.getApplicationArguments().equals(getBasicPropertiesLayout().getApplicationArgumentsField().getValue())) {
                //simple update
                LOGGER.debug("No upgrade-relevant fields have been modified. Updating existing workflow configuration.");
                selection = mdm.save(selection);
                LOGGER.debug("Workflow configuration '{}' successully updated, version number unchanged.", pName);
            } else {
                //url or arguments have changed...do version update
                LOGGER.debug("At least one upgrade-relevant field hast been modified. Upgrading existing workflow configuration to new version '{}'.", pNewVersion);
                selection.setId(null);
                selection.setApplicationPackageUrl(getBasicPropertiesLayout().getApplicationPackageUrlField().getValue());
                selection.setApplicationArguments(getBasicPropertiesLayout().getApplicationArgumentsField().getValue());
                selection.setVersion(pNewVersion);
                selection = mdm.save(selection);
                LOGGER.debug("Workflow configuration '{}' successully upgraded to version '{}'", pName, pNewVersion);
            }
            result = true;
        } catch (DBCommitException ex) {
            getParentApp().showError("Failed to update data workflow configuration! Cause: " + ex.getMessage());
            String object = "the changed workflow configuration '" + pName + "'";
            LOGGER.error(MsgBuilder.commitFailed(object) + "Cause: " + ex.getMessage(), ex);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain data workflow configuration for id " + selection.getId(), ex);
        } finally {
            mdm.close();
        }

        if (result) {
            updateElementInstance(selection);
        }
    }

    /**
     * Check the provided userId. If the user id exists and the max. role is
     * {@link edu.kit.dama.authorization.entities.Role#MEMBER}, TRUE is
     * returned. Otherwise, FALSE is returned and the cause of the failed check
     * is set as component error to the userId input field of the according
     * properties UI.
     *
     * @param pUserId The userId to check.
     *
     * @return TRUE if the user exists and has a proper max. role, FALSE
     * otherwise.
     */
    private boolean checkContactUserId(String pUserId) {
        boolean result = true;
        LOGGER.debug("Checking contact user id {}.", pUserId);
        if (pUserId != null) {
            try {
                IRoleRestriction<Role> role = UserServiceLocal.getSingleton().getRoleRestriction(new UserId(pUserId), AuthorizationContext.factorySystemContext());
                if (!role.atLeast(Role.MEMBER)) {
                    LOGGER.warn("Invalid contact userId '{}'. MaxRole is lower than MEMBER.", pUserId);
                    getBasicPropertiesLayout().getContactIdField().setComponentError(new UserError("User has insufficient permissions (role < MEMBER).", AbstractErrorMessage.ContentMode.TEXT, ErrorMessage.ErrorLevel.WARNING));
                    result = false;
                }
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Failed to check contact userId. SystemContext is not authorized.", ex);
                getBasicPropertiesLayout().getContactIdField().setComponentError(new UserError("Failed to check for user id '" + pUserId + "'", AbstractErrorMessage.ContentMode.TEXT, ErrorMessage.ErrorLevel.WARNING));
                result = false;
            } catch (EntityNotFoundException ex) {
                LOGGER.warn("Invalid contact userId '{}'. UserId not found.", pUserId);
                getBasicPropertiesLayout().getContactIdField().setComponentError(new UserError("No valid user id '" + pUserId + "'", AbstractErrorMessage.ContentMode.TEXT, ErrorMessage.ErrorLevel.WARNING));
                result = false;
            }
        }
        return result;
    }

    /**
     * Get the current version of the workflow configuration with the provided
     * name. There are three possible results: -1 means that the query for the
     * configuration with the provided name failed. 0 means, that there is no
     * configuration with the provided name and a value larger 0 is the current
     * version of the configuration with the provided name.
     *
     * @param pConfigurationName The name of the configuration.
     *
     * @return The version number as described above.
     */
    private int getWorkflowVersion(String pConfigurationName) {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        List<DataWorkflowTaskConfiguration> result = new LinkedList<>();
        try {
            result = mdm.findResultList("SELECT w FROM DataWorkflowTaskConfiguration w WHERE w.name='" + pConfigurationName + "'", DataWorkflowTaskConfiguration.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            //error ... cannot continue
            return -1;
        } finally {
            mdm.close();
        }

        if (result.isEmpty()) {
            return 0;
        }

        Collections.sort(result, new Comparator<DataWorkflowTaskConfiguration>() {

            @Override
            public int compare(DataWorkflowTaskConfiguration o1, DataWorkflowTaskConfiguration o2) {
                return Integer.compare(o1.getVersion(), o2.getVersion());
            }
        });

        return result.get(result.size() - 1).getVersion();
    }

    @Override
    public void addNewElementInstance(DataWorkflowTaskConfiguration pElementToAdd) {
        getElementList().addItem(Long.toString(pElementToAdd.getId()));
        getElementList().setItemCaption(Long.toString(pElementToAdd.getId()), getWorkflowConfigurationCaption(pElementToAdd));
        getElementList().select(Long.toString(pElementToAdd.getId()));
    }

    @Override
    public void updateElementInstance(DataWorkflowTaskConfiguration pElementToAdd) {
        getElementList().addItem(Long.toString(pElementToAdd.getId()));
        getElementList().setItemCaption(Long.toString(pElementToAdd.getId()), getWorkflowConfigurationCaption(pElementToAdd));
        getElementList().select(Long.toString(pElementToAdd.getId()));
    }

    /**
     * Get the (list) item caption for the provided workflow configuration.
     *
     * @param accessPoint The workflow configuration.
     *
     * @return The item caption in the format 'ConfigurationName
     * ConfigurationVersion (ConfigurationId)'
     */
    private String getWorkflowConfigurationCaption(DataWorkflowTaskConfiguration workflowConfiguration) {
        return workflowConfiguration.getName() + " v." + workflowConfiguration.getVersion() + " (" + workflowConfiguration.getId() + ")";
    }

    @Override
    public DataWorkflowBasePropertiesLayout getBasicPropertiesLayout() {
        if (basicPropertiesLayout == null) {
            basicPropertiesLayout = new DataWorkflowBasePropertiesLayout(getParentApp());
        }
        return basicPropertiesLayout;
    }

}
