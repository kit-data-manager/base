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
package edu.kit.dama.ui.admin.staging.accesspoints;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IPathSelector;
import edu.kit.dama.ui.admin.utils.PathSelector;
import edu.kit.dama.ui.admin.AbstractBasePropertiesLayout;
import static edu.kit.dama.util.Constants.USERS_GROUP_ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public final class AccessPointBasePropertiesLayout extends AbstractBasePropertiesLayout<StagingAccessPointConfiguration> {

    public static final Logger LOGGER = LoggerFactory.getLogger(AccessPointBasePropertiesLayout.class);
    private static final String DEBUG_ID_PREFIX = AccessPointBasePropertiesLayout.class.getName() + "_";
    private TextField remoteBaseUrlField;
    private TextField localBasePathField;
    private Button pathSelectorButton;
    private CheckBox transientBox;

    /**
     * Default constructor.
     */
    public AccessPointBasePropertiesLayout() {
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        setColumns(4);
        setRows(4);

        getCheckBoxesLayout().addComponent(getTransientBox());

        addComponent(getNameField(), 0, 0, 1, 0);
        addComponent(getGroupBox(), 3, 0);
        addComponent(getRemoteBaseUrlField(), 0, 1, 1, 1);
        addComponent(getLocalBasePathField(), 0, 2);
        addComponent(getPathSelectorButton(), 1, 2);
        addComponent(getCheckBoxesLayout(), 3, 1, 3, 2);
        addComponent(getDescriptionArea(), 0, 3, 3, 3);

        setComponentAlignment(getPathSelectorButton(), Alignment.BOTTOM_RIGHT);
        setColumnExpandRatio(0, .88f);
        setColumnExpandRatio(1, 0.01f);
        setColumnExpandRatio(2, 0.01f);
        setColumnExpandRatio(3, 0.1f);
        setRowExpandRatio(3, 1f);
    }

    /**
     * Returns the remote base path URL field holding the remote base URL of the
     * configured access point.
     *
     * @return The remote base URL field.
     */
    public final TextField getRemoteBaseUrlField() {
        if (remoteBaseUrlField == null) {
            remoteBaseUrlField = factoryTextField("REMOTE BASE URL", "remoteBaseUrlField", true);
            remoteBaseUrlField.setDescription("The base URL at which this access point is reachable from remote, e.g. http://myHost/webdav");
        }
        return remoteBaseUrlField;
    }

    /**
     * Returns the local base path text field holding the local base path of the
     * configured access point.
     *
     * @return The local base path field.
     */
    public TextField getLocalBasePathField() {
        if (localBasePathField == null) {
            localBasePathField = factoryTextField("LOCAL BASE PATH", "localBasePathField", true);
            localBasePathField.setDescription("The local absolute path that is equivalent to the remote base URL, e.g. /var/www/webdav");
        }
        return localBasePathField;
    }

    /**
     * Returns the path selector button for selecting the local base path of the
     * configured access point.
     *
     * @return The path selector button.
     */
    public Button getPathSelectorButton() {
        if (pathSelectorButton == null) {
            String id = "pathSelectorButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            pathSelectorButton = new Button("Select Path");
            pathSelectorButton.setId(DEBUG_ID_PREFIX + id);
            pathSelectorButton.setImmediate(true);

            pathSelectorButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    PathSelector pathSelector = new PathSelector(new IPathSelector() {

                        @Override
                        public void firePathSelectorCloseEvent(String selectedPath) {
                            getLocalBasePathField().setValue(selectedPath);
                        }
                    });
                    UI.getCurrent().addWindow(pathSelector);
                }
            });
        }
        return pathSelectorButton;
    }

    /**
     * @return the transientBox
     */
    public CheckBox getTransientBox() {
        if (transientBox == null) {
            String id = "transientBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            transientBox = new CheckBox("Transient");
            transientBox.setId(DEBUG_ID_PREFIX + id);
            transientBox.setImmediate(true);
            transientBox.setDescription("If an access point is transient, all its local data will be removed at each start of the repository system.");
            transientBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            transientBox.addStyleName("yesno");
        }
        return transientBox;
    }

    @Override
    public void updateSelection(StagingAccessPointConfiguration pValue) throws UIComponentUpdateException {
        reset();

        if (pValue == null) {
            throw new UIComponentUpdateException("Invalid access point.");
        }

        getNameField().setValue(pValue.getName());
        if (pValue.getGroupId() == null) {
            getGroupBox().select(USERS_GROUP_ID);
        } else {
            getGroupBox().select(pValue.getGroupId());
        }
        getRemoteBaseUrlField().setValue(pValue.getRemoteBaseUrl());
        getLocalBasePathField().setValue(pValue.getLocalBasePath());
        getDefaultBox().setValue(pValue.isDefaultAccessPoint());
        getDisabledBox().setValue(pValue.isDisabled());
        getTransientBox().setValue(pValue.isTransientAccessPoint());
        getDescriptionArea().setValue(pValue.getDescription());
    }

    @Override
    public void reset() {
        setEnabled(true);
        getNameField().setValue(null);
        getGroupBox().select(USERS_GROUP_ID);
        getRemoteBaseUrlField().setValue(null);
        getLocalBasePathField().setValue(null);
        getDefaultBox().setValue(false);
        getDisabledBox().setValue(false);
        getTransientBox().setValue(false);
        getDescriptionArea().setValue(null);
    }

    @Override
    public String getNameFieldLabel() {
        return "ACCESS POINT NAME";
    }
}
