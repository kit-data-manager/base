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
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration;
import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.ui.admin.AbstractBasePropertiesLayout;
import edu.kit.dama.ui.admin.AdminUIMainView;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.workflow.property.AddEnvironmentPropertyComponent;
import static edu.kit.dama.util.Constants.USERS_GROUP_ID;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class DataWorkflowBasePropertiesLayout extends AbstractBasePropertiesLayout<DataWorkflowTaskConfiguration> {

    public static final Logger LOGGER = LoggerFactory.getLogger(DataWorkflowBasePropertiesLayout.class);
    private static final String DEBUG_ID_PREFIX = DataWorkflowBasePropertiesLayout.class.getName() + "_";

    private TextField applicationArgumentsField;
    private TextField applicationPackageUrlField;
    private TextField contactIdField;
    private TextField keywordsField;
    private TextField versionField;
    private TwinColSelect environmentProperties;
    private AddEnvironmentPropertyComponent addPropertyComponent;

    public DataWorkflowBasePropertiesLayout(AdminUIMainView parentApp) {
        super(parentApp);
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setWidth("100%");
        setImmediate(true);
        setMargin(true);
        setSpacing(true);

        setColumns(4);
        setRows(5);
        //first row
        addComponent(getNameField(), 0, 0);
        addComponent(getVersionField(), 1, 0);
        addComponent(getContactIdField(), 2, 0);
        addComponent(getGroupBox(), 3, 0);
        //second row
        addComponent(getApplicationPackageUrlField(), 0, 1, 1, 1);
        addComponent(getApplicationArgumentsField(), 2, 1);
        addComponent(getCheckBoxesLayout(), 3, 1, 3, 2);
        addComponent(getKeywordsField(), 0, 2, 2, 2);
        addComponent(getDescriptionArea(), 0, 3, 1, 3);

        NativeButton addPropertyButton = new NativeButton();
        addPropertyButton.setIcon(new ThemeResource(IconContainer.ADD));
        addPropertyButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                addPropertyComponent.reset();
                addPropertyComponent.getPopupView().setPopupVisible(true);
            }
        });

        HorizontalLayout layout = new HorizontalLayout(getEnvironmentPropertiesSelect(), addPropertyButton);
        layout.setComponentAlignment(getEnvironmentPropertiesSelect(), Alignment.TOP_LEFT);
        layout.setComponentAlignment(addPropertyButton, Alignment.BOTTOM_RIGHT);
        layout.setSizeFull();
        layout.setExpandRatio(getEnvironmentPropertiesSelect(), .95f);
        layout.setExpandRatio(addPropertyButton, .05f);
        addPropertyComponent = new AddEnvironmentPropertyComponent();
        addComponent(addPropertyComponent.getPopupView(), 0, 4, 3, 4);
        setComponentAlignment(addPropertyComponent.getPopupView(), Alignment.MIDDLE_CENTER);

        //set dummy row height to 0
        setRowExpandRatio(4, 0f);
        addComponent(layout, 2, 3, 3, 3);

    }

    public TextField getApplicationPackageUrlField() {
        if (applicationPackageUrlField == null) {
            applicationPackageUrlField = factoryTextField("PACKAGE URL", "applicationPackageUrlField", true);
        }
        return applicationPackageUrlField;
    }

    public TextField getApplicationArgumentsField() {
        if (applicationArgumentsField == null) {
            applicationArgumentsField = factoryTextField("ARGUMENTS", "applicationArgumentsField", false);
        }
        return applicationArgumentsField;
    }

    public TextField getContactIdField() {
        if (contactIdField == null) {
            contactIdField = factoryTextField("CONTACT USERID", "contactIdField", false);
        }
        return contactIdField;
    }

    public TextField getKeywordsField() {
        if (keywordsField == null) {
            keywordsField = factoryTextField("KEYWORDS", "keywordsField", false);
        }
        return keywordsField;
    }

    public TextField getVersionField() {
        if (versionField == null) {
            versionField = factoryTextField("VERSION", "versionField", false);
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
        }

        return environmentProperties;
    }

    @Override
    public String getNameFieldLabel() {
        return "TASK NAME";
    }

    @Override
    public void reset() {
        setEnabled(true);
        getNameField().setValue(null);
        getGroupBox().select(USERS_GROUP_ID);
        getApplicationPackageUrlField().setValue(null);
        getApplicationArgumentsField().setValue(null);
        getContactIdField().setValue(null);
        getKeywordsField().setValue(null);
        getVersionField().setReadOnly(false);
        getVersionField().setValue(null);
        getVersionField().setReadOnly(true);
        getDescriptionArea().setValue(null);
        getEnvironmentPropertiesSelect().removeAllItems();

        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        List<ExecutionEnvironmentProperty> props;
        try {
            props = mdm.find(ExecutionEnvironmentProperty.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            props = new LinkedList<>();
            LOGGER.error("Failed to obtain ExecutionEnvironmentProperties.", ex);
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
    public void updateSelection(DataWorkflowTaskConfiguration pValue) throws UIComponentUpdateException {
        reset();

        if (pValue == null) {
            throw new UIComponentUpdateException("Invalid task configuration.");
        }
        getNameField().setValue(pValue.getName());
        if (pValue.getGroupId() == null) {
            getGroupBox().select(USERS_GROUP_ID);
        } else {
            getGroupBox().select(pValue.getGroupId());
        }

        getApplicationPackageUrlField().setValue(pValue.getApplicationPackageUrl());
        getApplicationArgumentsField().setValue(pValue.getApplicationArguments());
        getContactIdField().setValue(pValue.getContactUserId());
        getKeywordsField().setValue(pValue.getKeywords());
        getVersionField().setReadOnly(false);
        getVersionField().setValue(Integer.toString(pValue.getVersion()));
        getVersionField().setReadOnly(true);
        for (ExecutionEnvironmentProperty prop : pValue.getRequiredEnvironmentProperties()) {
            getEnvironmentPropertiesSelect().select(prop.getId());
        }
        getDescriptionArea().setValue(pValue.getDescription());
        getDefaultBox().setValue(pValue.isDefaultTask());
        getDisabledBox().setValue(pValue.isDisabled());
    }

}
