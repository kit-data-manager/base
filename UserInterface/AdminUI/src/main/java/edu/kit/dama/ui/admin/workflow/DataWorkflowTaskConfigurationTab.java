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

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.AbstractErrorMessage;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.UserError;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Tree;
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
import edu.kit.dama.ui.admin.exception.DBCommitException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.Constants;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private Tree elementTree;

    public DataWorkflowTaskConfigurationTab() {
        super();
        DEBUG_ID_PREFIX += hashCode() + "_";
    }

    public final Tree getElementTree() {
        if (elementTree == null) {
            String id = "elementList";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            elementTree = new Tree("AVAILABLE ELEMENTS");
            elementTree.setId(DEBUG_ID_PREFIX + id);
            HierarchicalContainer container = new HierarchicalContainer();
            container.addContainerProperty("caption", String.class, null);
            elementTree.setContainerDataSource(container);
            elementTree.setItemCaptionPropertyId("caption");
            elementTree.setHeight("100%");
            elementTree.setWidth("300px");
            elementTree.setNullSelectionAllowed(false);
            elementTree.setImmediate(true);
            elementTree.addStyleName(CSSTokenContainer.BOLD_CAPTION);

            elementTree.addValueChangeListener(new Property.ValueChangeListener() {

                @Override
                public void valueChange(Property.ValueChangeEvent event) {
                    Object value = event.getProperty().getValue();
                    String selection;
                    if (value instanceof Long) {
                        selection = Long.toString((Long) value);
                    } else {
                        selection = (String) value;
                    }

                    ListSelection listSelection = validateListSelection(selection);

                    switch (listSelection) {
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
                                    + "element selection. " + NoteBuilder.CONTACT, -1);
                            LOGGER.error("Failed to update " + this.getClass().getSimpleName()
                                    + ". Cause: Undefined enum constant detected, namely '"
                                    + listSelection.name() + "'.");
                            break;
                    }
                }

            });
        }
        return elementTree;
    }

    @Override
    public GridLayout buildMainLayout() {
        String id = "mainLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        UIUtils7.GridLayoutBuilder mainLayoutBuilder = new UIUtils7.GridLayoutBuilder(2, 2);

        // Add components to mainLayout
        mainLayoutBuilder.fillColumn(getElementTree(), 0, 0, 1);
        mainLayoutBuilder.fillRow(getPropertiesPanel(), 1, 0, 1);
        mainLayoutBuilder.addComponent(getCommitChangesButton(), Alignment.BOTTOM_RIGHT, 1, 1, 1, 1);
        mainLayout = mainLayoutBuilder.getLayout();
        mainLayout.setId(DEBUG_ID_PREFIX + id);
        mainLayout.setSizeFull();
        mainLayout.setImmediate(true);
        mainLayout.setSpacing(true);
        mainLayout.setMargin(true);

        mainLayout.setColumnExpandRatio(1, 1f);
        mainLayout.setRowExpandRatio(0, 1f);

        return mainLayout;

    }

    @Override
    public String getSelectedItemId() {
        return (String) getElementTree().getValue();
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

        Map<String, List<DataWorkflowTaskConfiguration>> elementMap = new HashMap<>();

        for (DataWorkflowTaskConfiguration config : configurations) {
            String name = config.getName();
            List<DataWorkflowTaskConfiguration> configList = elementMap.get(name);
            if (configList == null) {
                configList = new ArrayList<>();
                elementMap.put(name, configList);
            }
            configList.add(config);
        }

        HierarchicalContainer container = (HierarchicalContainer) getElementTree().getContainerDataSource();
        container.removeAllItems();

        //add NEW element
        container.addItem(NEW_UNIQUE_ID);
        container.setChildrenAllowed(NEW_UNIQUE_ID, false);
        container.getContainerProperty(NEW_UNIQUE_ID, "caption").setValue(NEW_ELEMENT_LABEL);
        //add remaining elements
        elementMap.entrySet().forEach((entry) -> {
            List<DataWorkflowTaskConfiguration> elements = entry.getValue();
            Collections.sort(elements, (DataWorkflowTaskConfiguration o1, DataWorkflowTaskConfiguration o2) -> Integer.compare(o1.getVersion(), o2.getVersion()));
            //parentId is ELEMENT_NAME + ID_OF_FIRST_VERSION
            String parentId = entry.getKey() + "@" + elements.get(0).getId();
            Item parent = container.addItem(parentId);
            parent.getItemProperty("caption").setValue(entry.getKey() + " ( " + elements.size() + " version" + ((elements.size() > 1) ? "s)" : ")"));
            container.setChildrenAllowed(parentId, true);
            for (DataWorkflowTaskConfiguration config : elements) {
                Item child = container.addItem(config.getId());
                container.setChildrenAllowed(config.getId(), false);
                child.getItemProperty("caption").setValue(getWorkflowConfigurationCaption(config));
                container.setParent(config.getId(), parentId);
            }
        });
    }

    /**
     * Get the primary key for the provided node elementId. The elementId can be
     * one of the following: 'null' for no selection, 'MINUS_ONE_ELEMENT' for
     * 'NEW' selection, '1' for selection of a task version node, 'Task@1' for
     * selection of a task parent node containing all versions.
     */
    private long getPrimaryKeyByElementId(String elementId) {
        long value = 0;
        if (null == elementId) {
            //no selection
            value = -1;
        } else {
            switch (elementId) {
                case NEW_UNIQUE_ID:
                    //we have the "NEW' node
                    value = 0;
                    break;
                default:
                    try {
                        value = Long.parseLong(elementId);
                        //if this works, the node is a task version node
                    } catch (NumberFormatException ex) {
                        //string selection
                        int minusIdx = elementId.indexOf("@");
                        if (minusIdx > -1) {
                            value = Long.parseLong(elementId.substring(minusIdx + 1, elementId.length()));
                            //if this works, the node is a task parent node
                        }
                    }
                    break;
            }
        }
        return value;
    }

    @Override
    public boolean elementWithIdExists(String pId) {
        boolean result = false;
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            result = mdm.find(DataWorkflowTaskConfiguration.class, getPrimaryKeyByElementId(pId)) != null;
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
            result = mdm.find(DataWorkflowTaskConfiguration.class, getPrimaryKeyByElementId(pId));
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
            //'selection' is later used to check whether the version number must be updated or not.
            selection = new DataWorkflowTaskConfiguration();
            selection.setId(pSelection.getId());
            selection.setName(pSelection.getName());
            selection.setApplicationPackageUrl(pSelection.getApplicationPackageUrl());
            selection.setApplicationArguments(pSelection.getApplicationArguments());
        } else {
            //called if new element is selected
            DataWorkflowTaskConfiguration dummySelection = new DataWorkflowTaskConfiguration();
            dummySelection.setName("NewDataWorkflowTask");
            dummySelection.setVersion(1);
            dummySelection.setContactUserId("admin");
            dummySelection.setApplicationPackageUrl("file:///");
            dummySelection.setGroupId(Constants.USERS_GROUP_ID);
//            dummySelection.setDisabled(Boolean.TRUE);
//            dummySelection.setDefaultTask(Boolean.FALSE);
            getBasicPropertiesLayout().updateSelection(dummySelection);
            //no selection, new element creation
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
                UIComponentTools.showWarning("There is already a workflow configuration named '" + applicationName + "' registered. "
                        + "Please select another name to proceed.");
                return;
            }
        } else {
            //'selection' should contain the name of the currently selected workflow, compare the name field with it.
            if (applicationName.equals(selection.getName())) {
                //if names are equal, check whether update-relevant fields have changed (applicationUrl and arguments)
                updateWorkflowConfiguration(applicationName, currentVersion + 1);
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
            UIComponentTools.showWarning("There is already a workflow configuration named '" + pName + "' registered. "
                    + "Please select another name to proceed.");
            return;
        }
        if (pName.contains("@")) {
            getBasicPropertiesLayout().getNameField().setComponentError(new UserError("Data workflow configuration names may not contain the @ character.", AbstractErrorMessage.ContentMode.TEXT, ErrorMessage.ErrorLevel.WARNING));
            return;
        } else {
            getBasicPropertiesLayout().getNameField().setComponentError(null);
        }

        try {
            LOGGER.debug("Creating new workflow configuration from UI.");
            selection = new DataWorkflowTaskConfiguration();
            selection.setName(pName);
            selection.setGroupId((String) getBasicPropertiesLayout().getGroupBox().getValue());
            String contactUserId = (String) getBasicPropertiesLayout().getContactBox().getValue();
            if (!checkContactUserId(contactUserId)) {
                return;
            }

            String applicationUrl = getBasicPropertiesLayout().getApplicationPackageUrlField().getValue();

            try {
                LOGGER.debug("Successfully validated application package URL {}", new URL(applicationUrl));
            } catch (MalformedURLException ex) {
                UIComponentTools.showWarning("The provided application package URL is invalid.");
                return;
            }

            selection.setApplicationPackageUrl(getBasicPropertiesLayout().getApplicationPackageUrlField().getValue());

            selection.setApplicationArguments(getBasicPropertiesLayout().getApplicationArgumentsField().getValue());
            selection.setVersion(1);
            selection.setContactUserId((String) getBasicPropertiesLayout().getContactBox().getValue());
            LOGGER.debug("Setting other attributes.");
            selection.setKeywords(getBasicPropertiesLayout().getKeywordsField().getValue());
            selection.setDescription(getBasicPropertiesLayout().getDescriptionArea().getValue());
//            selection.setDisabled(getBasicPropertiesLayout().getDisabledBox().getValue());
//            selection.setDefaultTask(getBasicPropertiesLayout().getDefaultBox().getValue());
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

            String contactUserId = (String) getBasicPropertiesLayout().getContactBox().getValue();
            if (!checkContactUserId(contactUserId)) {
                return;
            }

            selection.setContactUserId((String) getBasicPropertiesLayout().getContactBox().getValue());
            selection.setGroupId((String) getBasicPropertiesLayout().getGroupBox().getValue());
            LOGGER.debug("Setting other attributes.");
            selection.setKeywords(getBasicPropertiesLayout().getKeywordsField().getValue());
            selection.setDescription(getBasicPropertiesLayout().getDescriptionArea().getValue());
//            selection.setDisabled(getBasicPropertiesLayout().getDisabledBox().getValue());
//            selection.setDefaultTask(getBasicPropertiesLayout().getDefaultBox().getValue());

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

            String appPkg1 = selection.getApplicationPackageUrl();
            String appPkg2 = getBasicPropertiesLayout().getApplicationPackageUrlField().getValue();

            if (appPkg1 == null || appPkg2 == null) {
                throw new DBCommitException("Package Url must not be null.");
            }

            String arg1 = selection.getApplicationArguments() == null ? "" : selection.getApplicationArguments();
            String arg2 = getBasicPropertiesLayout().getApplicationArgumentsField().getValue() == null ? "" : getBasicPropertiesLayout().getApplicationArgumentsField().getValue();

            if (appPkg1.equals(appPkg2) && arg1.equals(arg2)) {
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
            UIComponentTools.showError("Failed to update data workflow configuration! Cause: " + ex.getMessage());
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
                    getBasicPropertiesLayout().getContactBox().setComponentError(new UserError("User has insufficient permissions (role < MEMBER).", AbstractErrorMessage.ContentMode.TEXT, ErrorMessage.ErrorLevel.WARNING));
                    result = false;
                }
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Failed to check contact userId. SystemContext is not authorized.", ex);
                getBasicPropertiesLayout().getContactBox().setComponentError(new UserError("Failed to check for user id '" + pUserId + "'", AbstractErrorMessage.ContentMode.TEXT, ErrorMessage.ErrorLevel.WARNING));
                result = false;
            } catch (EntityNotFoundException ex) {
                LOGGER.warn("Invalid contact userId '{}'. UserId not found.", pUserId);
                getBasicPropertiesLayout().getContactBox().setComponentError(new UserError("No valid user id '" + pUserId + "'", AbstractErrorMessage.ContentMode.TEXT, ErrorMessage.ErrorLevel.WARNING));
                result = false;
            }
        }
        if (result) {
            getBasicPropertiesLayout().getContactBox().setComponentError(null);
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
            result = mdm.findResultList("SELECT w FROM DataWorkflowTaskConfiguration w WHERE w.name=?1", new Object[]{pConfigurationName}, DataWorkflowTaskConfiguration.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            //error ... cannot continue
            return -1;
        } finally {
            mdm.close();
        }

        if (result.isEmpty()) {
            return 0;
        }
        Collections.sort(result, (DataWorkflowTaskConfiguration o1, DataWorkflowTaskConfiguration o2) -> Integer.compare(o1.getVersion(), o2.getVersion()));

        return result.get(result.size() - 1).getVersion();
    }

    @Override
    public void addNewElementInstance(DataWorkflowTaskConfiguration pElementToAdd) {
        fillElementList();
        Object parent = elementTree.getParent(pElementToAdd.getId());
        elementTree.expandItemsRecursively(parent);
        elementTree.select(pElementToAdd.getId());
    }

    @Override
    public void updateElementInstance(DataWorkflowTaskConfiguration pElementToUpdate) {
        fillElementList();
        Object parent = elementTree.getParent(pElementToUpdate.getId());
        elementTree.expandItemsRecursively(parent);
        elementTree.select(pElementToUpdate.getId());
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
            basicPropertiesLayout = new DataWorkflowBasePropertiesLayout();
        }
        return basicPropertiesLayout;
    }

}
