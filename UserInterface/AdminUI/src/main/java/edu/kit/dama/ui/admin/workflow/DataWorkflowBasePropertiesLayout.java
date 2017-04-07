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
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration;
import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.ui.admin.AbstractBasePropertiesLayout;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.commons.util.UIHelper;
import edu.kit.dama.ui.admin.workflow.property.AddEnvironmentPropertyComponent;
import static edu.kit.dama.util.Constants.USERS_GROUP_ID;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class DataWorkflowBasePropertiesLayout extends AbstractBasePropertiesLayout<DataWorkflowTaskConfiguration> implements IEnvironmentPropertyCreationListener {

    public static final Logger LOGGER = LoggerFactory.getLogger(DataWorkflowBasePropertiesLayout.class);
    private static final String DEBUG_ID_PREFIX = DataWorkflowBasePropertiesLayout.class.getName() + "_";

    private TextField applicationArgumentsField;
    private TextField applicationPackageUrlField;
    //private TextField contactIdField;
    private ComboBox contactBox;

    private TextField keywordsField;
    private TextField versionField;
    private TwinColSelect environmentProperties;
    private AddEnvironmentPropertyComponent addPropertyComponent;

    public DataWorkflowBasePropertiesLayout() {
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setSizeFull();
        setMargin(true);
        setSpacing(true);
        setCaption("TASK CONFIGURATION");

        setColumns(4);
        setRows(6);
        //first row
        addComponent(getNameField(), 0, 0);
        addComponent(getVersionField(), 1, 0);
        addComponent(getContactBox(), 2, 0);
        addComponent(getGroupBox(), 3, 0);
        //second row
        addComponent(getApplicationPackageUrlField(), 0, 1, 1, 1);
        addComponent(getApplicationArgumentsField(), 2, 1);
        //addComponent(getCheckBoxesLayout(), 3, 1, 3, 2);
        //add placeholder only
        addComponent(new VerticalLayout(), 3, 1, 3, 2);
        Label l = new Label("* Changing fields with a red border will update the version of the associated task.");
        l.addStyleName("red-text");
        addComponent(l, 0, 2, 2, 2);
        l.setHeight("12px");
        setComponentAlignment(l, Alignment.TOP_CENTER);

        //
        addComponent(getKeywordsField(), 0, 3, 2, 3);
        //
        addComponent(getDescriptionArea(), 0, 4, 2, 5);

        Button addPropertyButton = new Button();
        addPropertyButton.setIcon(new ThemeResource(IconContainer.ADD));
        addPropertyButton.addClickListener((Button.ClickEvent event) -> {
            addPropertyComponent.reset();
            addPropertyComponent.showWindow();
        });

        HorizontalLayout layout = new HorizontalLayout(getEnvironmentPropertiesSelect(), addPropertyButton);
        layout.setComponentAlignment(getEnvironmentPropertiesSelect(), Alignment.TOP_LEFT);
        layout.setComponentAlignment(addPropertyButton, Alignment.BOTTOM_RIGHT);
        layout.setSizeFull();
        layout.setExpandRatio(getEnvironmentPropertiesSelect(), .95f);
        layout.setExpandRatio(addPropertyButton, .05f);
        addComponent(layout, 3, 3, 3, 5);

        //add popup to layout
        addPropertyComponent = new AddEnvironmentPropertyComponent(this);

        //set dummy row height to 0
        setColumnExpandRatio(0, 0.2f);
        setColumnExpandRatio(1, 0.15f);
        setColumnExpandRatio(2, 0.2f);
        setColumnExpandRatio(3, 0.25f);
        setRowExpandRatio(5, 1f);

    }

    public TextField getApplicationPackageUrlField() {
        if (applicationPackageUrlField == null) {
            applicationPackageUrlField = factoryTextField("PACKAGE URL", "applicationPackageUrlField", true);
            applicationPackageUrlField.setDescription("The local or remote URL where this task's application package can be obtained from, e.g. file:///software/vault/myApp/1.0/myApp.zip");
            applicationPackageUrlField.addStyleName("red-border");
        }
        return applicationPackageUrlField;
    }

    public TextField getApplicationArgumentsField() {
        if (applicationArgumentsField == null) {
            applicationArgumentsField = factoryTextField("ARGUMENTS", "applicationArgumentsField", false);
            applicationArgumentsField.setDescription("Application arguments that are identical for each task execution. Multiple arguments are space separated.");
            applicationArgumentsField.addStyleName("red-border");
        }
        return applicationArgumentsField;
    }

    public ComboBox getContactBox() {
        if (contactBox == null) {
            String id = "contactBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");
            contactBox = new ComboBox("CONTACT");
            contactBox.setDescription("The user who serves as contact for this task, e.g. the developer.");
            contactBox.setId(DEBUG_ID_PREFIX + id);
            contactBox.setWidth("100%");
            contactBox.setImmediate(true);
            contactBox.setNullSelectionAllowed(true);
            contactBox.setNullSelectionItemId("None");
            contactBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return contactBox;
    }

    public TextField getKeywordsField() {
        if (keywordsField == null) {
            keywordsField = factoryTextField("KEYWORDS", "keywordsField", false);
            keywordsField.setDescription("A list of space-separated keywords describing the task.");
        }
        return keywordsField;
    }

    public TextField getVersionField() {
        if (versionField == null) {
            versionField = factoryTextField("VERSION", "versionField", false);
            versionField.setDescription("This task's version. ");
        }
        return versionField;
    }

    public TwinColSelect getEnvironmentPropertiesSelect() {
        if (environmentProperties == null) {
            String id = "environmentProperties";
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

    @Override
    public String getNameFieldLabel() {
        return "TASK NAME";
    }

    /**
     * Reload the group box from the database.
     */
    public final void reloadContactBox() {
        getContactBox().removeAllItems();
        getContactBox().addItem("None");
        getContactBox().setItemCaption("None", "None");
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());
        try {
            List<UserData> users = mdm.find(UserData.class);

            users.stream().map((user) -> {
                if (user.getDistinguishedName() != null) {
                    getContactBox().addItem(user.getDistinguishedName());
                }
                return user;
            }).forEachOrdered((user) -> {
                if (user.getDistinguishedName() != null) {
                    getContactBox().setItemCaption(user.getDistinguishedName(), user.getFullname());
                }
            });
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "all users";
            UIComponentTools.showWarning("Contact-box not reloadable! Cause: "
                    + NoteBuilder.unauthorizedGetRequest(object));
            LOGGER.warn("Failed to reload '" + getGroupBox().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        } finally {
            mdm.close();
        }
    }

    @Override
    public void reset() {
        setEnabled(true);
        getNameField().setReadOnly(false);
        getNameField().setValue(null);
        getGroupBox().select(USERS_GROUP_ID);
        getApplicationPackageUrlField().setValue(null);
        getApplicationArgumentsField().setValue(null);
        reloadContactBox();
        getKeywordsField().setValue(null);
        getVersionField().setReadOnly(false);
        getVersionField().setValue(null);
        getVersionField().setReadOnly(true);
        getDescriptionArea().setValue(null);
        getEnvironmentPropertiesSelect().removeAllItems();

        reloadEnvironmentPropertiesList();
    }

    @Override
    public void updateSelection(DataWorkflowTaskConfiguration pValue) throws UIComponentUpdateException {
        reset();

        if (pValue == null) {
            throw new UIComponentUpdateException("Invalid task configuration.");
        }

        if (pValue.getId() != null) {
            //existing task...no name update allowed
            getVersionField().setReadOnly(false);
            getNameField().setValue(pValue.getName());
            getVersionField().setReadOnly(true);
        } else {
            //new task, name can be assigned once
            getVersionField().setReadOnly(false);
            getNameField().setValue(pValue.getName());
        }

        if (pValue.getGroupId() == null) {
            getGroupBox().select(USERS_GROUP_ID);
        } else {
            getGroupBox().select(pValue.getGroupId());
        }

        getApplicationPackageUrlField().setValue(pValue.getApplicationPackageUrl());
        getApplicationArgumentsField().setValue(pValue.getApplicationArguments());
        getContactBox().select(pValue.getContactUserId());
        getKeywordsField().setValue(pValue.getKeywords());
        getVersionField().setReadOnly(false);
        getVersionField().setValue(Integer.toString(pValue.getVersion()));
        getVersionField().setReadOnly(true);
        pValue.getRequiredEnvironmentProperties().forEach((prop) -> {
            getEnvironmentPropertiesSelect().select(prop.getId());
        });
        getDescriptionArea().setValue(pValue.getDescription());
        // getDefaultBox().setValue(pValue.isDefaultTask());
        // getDisabledBox().setValue(pValue.isDisabled());
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
        Collections.sort(props, (ExecutionEnvironmentProperty o1, ExecutionEnvironmentProperty o2) -> o1.getName().compareTo(o2.getName()));

        for (ExecutionEnvironmentProperty prop : props) {
            getEnvironmentPropertiesSelect().addItem(prop.getId());
            getEnvironmentPropertiesSelect().setItemCaption(prop.getId(), prop.getName());
        }
    }

    @Override
    public void fireEnvironmentPropertyCreatedEvent(ExecutionEnvironmentProperty pProperty) {
        Object selection = getEnvironmentPropertiesSelect().getValue();
        reloadEnvironmentPropertiesList();
        if (selection != null) {
            if (selection instanceof Set) {
                Set set = (Set) selection;
                set.forEach((o) -> {
                    getEnvironmentPropertiesSelect().select(o);
                });
            } else {
                getEnvironmentPropertiesSelect().select(selection);
            }
        }

    }

}
