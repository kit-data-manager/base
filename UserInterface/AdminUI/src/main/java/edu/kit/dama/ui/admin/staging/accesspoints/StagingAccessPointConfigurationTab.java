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

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.zybnet.autocomplete.server.AutocompleteField;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import edu.kit.dama.staging.util.StagingConfigurationManager;
import edu.kit.dama.ui.admin.AbstractConfigurationTab;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class StagingAccessPointConfigurationTab extends AbstractConfigurationTab<StagingAccessPointConfiguration> {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(StagingAccessPointConfigurationTab.class);
    private String DEBUG_ID_PREFIX = StagingAccessPointConfigurationTab.class.getName() + "_";

    public static final String NEW_ACCESS_POINT_UNIQUE_ID = "MINUS_ONE_ACCESS_POINT";

    private GridLayout mainLayout;
    private AutocompleteField<String> implementationClassField;
    private Button loadImplementationClassButton;
    private AccessPointBasePropertiesLayout basicPropertiesLayout;

    public StagingAccessPointConfigurationTab() {
        super();
        DEBUG_ID_PREFIX += hashCode() + "_";
    }

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

    @Override
    public void resetComponents() {
        getImplementationClassField().setReadOnly(false);
        StagingAccessPointConfiguration selection = loadElementById(getSelectedElementId());
        if (selection == null || selection.getImplementationClass() == null) {
            getImplementationClassField().clearChoices();
            getImplementationClassField().setValue(null);
        } else {
            getImplementationClassField().setText(selection.getImplementationClass());
            getImplementationClassField().setValue(selection.getImplementationClass());
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
            implementationClassField.setCaption("ACCESS POINT IMPLEMENTATION CLASS");
            implementationClassField.setId(DEBUG_ID_PREFIX + id);
            implementationClassField.setImmediate(true);
            implementationClassField.setWidth("100%");
            implementationClassField.setRequired(true);
            implementationClassField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            implementationClassField.setMinimumQueryCharacters(3);
            implementationClassField.addStyleName("v-textfield");
            implementationClassField.setQueryListener((AutocompleteField<String> field, String query) -> {
                Reflections reflections = new Reflections(query);
                Set<Class<? extends AbstractStagingAccessPoint>> types = reflections.getSubTypesOf(AbstractStagingAccessPoint.class);
                Class[] elements = types.toArray(new Class[]{});
                Arrays.sort(elements, (Class o1, Class o2) -> o1.getCanonicalName().compareTo(o2.getCanonicalName()));

                Arrays.asList(elements).forEach((type) -> {
                    if (!type.isInterface() && !Modifier.isAbstract(type.getModifiers()) && !type.isAnnotationPresent(Deprecated.class)) {
                        field.addSuggestion(type.getCanonicalName(), type.getCanonicalName());
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
     *
     * @return
     */
    public Button getLoadImplementationClassButton() {
        if (loadImplementationClassButton == null) {
            String id = "loadImplementationClassField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            loadImplementationClassButton = new Button();
            loadImplementationClassButton.setId(DEBUG_ID_PREFIX + id);
            loadImplementationClassButton.setIcon(new ThemeResource(IconContainer.TEXT_CODE_JAVA_INPUT));
            loadImplementationClassButton.setWidth("100%");
            // loadImplementationClassButton.setStyleName(BaseTheme.BUTTON_LINK);
            // loadImplementationClassButton.setImmediate(true);
            loadImplementationClassButton.setDescription(
                    "Load the entered implementation class for setting the "
                    + "configuration of the requested access point");

            loadImplementationClassButton.addClickListener((Button.ClickEvent event) -> {
                try {
                    StagingAccessPointConfiguration newAccessPoint = createAccessPoint();

                    commitAccessPoint(newAccessPoint);

                    addNewElementInstance(newAccessPoint);
                    setEnabledComponents(ListSelection.VALID);
                    UIComponentTools.showInformation("Staging access point successfully created.");
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
     * Create a new access point. The created access point will be immediately
     * persisted. Modifications can be applied afterwards.
     */
    private StagingAccessPointConfiguration createAccessPoint() throws ConfigurationException {
        // Valid implementation class name => Create new access point
        String implClassName = getImplementationClassField().getText();
        if (implClassName == null) {
            throw new ConfigurationException("Please use the auto-completion feature to select an implementation class.");
        }

        try {
            // Build instance of implementation class
            createAccessPointInstance(implClassName);
        } catch (ConfigurationException ex) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Failed to create access point instance for implementation class '" + implClassName + "'", ex);
        }
        // Create new access point
        StagingAccessPointConfiguration newAccessPoint = StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration();
        newAccessPoint.setName(implClassName.substring(implClassName.lastIndexOf(".") + 1) + " (" + newAccessPoint.getUniqueIdentifier() + ")");
        newAccessPoint.setImplementationClass(implClassName);
        newAccessPoint.setDisabled(true);
        return newAccessPoint;
    }

    /**
     * This method tries to instantiate the provided access point class and sets
     * some UI fields whose values are defined by the access point. If an error
     * occurs,a ConfigurationException will be thrown.
     *
     * @param implClassName The access point implementation class.
     *
     * @throws ConfigurationException If the access point class is invalid.
     */
    private AbstractStagingAccessPoint createAccessPointInstance(String implClassName)
            throws ConfigurationException {
        return createAccessPointInstance(implClassName, null);
    }

    /**
     *
     * @param implClassName
     * @param accessPointConfig
     * @return
     * @throws ConfigurationException
     */
    private AbstractStagingAccessPoint createAccessPointInstance(String implClassName, StagingAccessPointConfiguration accessPointConfig) throws ConfigurationException {
        try {
            if (accessPointConfig == null) {
                return (AbstractStagingAccessPoint) Class.forName(implClassName).newInstance();
            }
            return (AbstractStagingAccessPoint) Class.forName(implClassName)
                    .getConstructor(StagingAccessPointConfiguration.class)
                    .newInstance(accessPointConfig);
        } catch (ClassNotFoundException ex) {
            String msg = "Implementation class '" + implClassName + "' not found.";
            throw new ConfigurationException(msg, ex);
        } catch (ClassCastException ex) {
            String msg = "Implementation class '" + implClassName + "' does not implement AbstractStagingAccessPoint.";
            throw new ConfigurationException(msg, ex);
        } catch (InstantiationException ex) {
            String msg = "Failed to instantiate implementation class '"
                    + implClassName + "' with the default constructor.";
            throw new ConfigurationException(msg, ex);
        } catch (IllegalAccessException ex) {
            String msg = "Failed to access the default constructor of the implementation class '"
                    + implClassName + "'";
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
    public AccessPointBasePropertiesLayout getBasicPropertiesLayout() {
        if (basicPropertiesLayout == null) {
            basicPropertiesLayout = new AccessPointBasePropertiesLayout();
        }
        return basicPropertiesLayout;
    }

    @Override
    public void commitChanges() {
        StagingAccessPointConfiguration selectedAccessPoint = loadElementById(getSelectedElementId());
        try {
            validateRequiredComponentValues();
            StagingAccessPointConfiguration temporaryAccessPoint = selectedAccessPoint.clone();
            StagingAccessPointConfiguration changedAccessPoint = collectSettings(selectedAccessPoint);
            checkAccessPointConfigurability(temporaryAccessPoint, changedAccessPoint);
            commitAccessPoint(changedAccessPoint);
            updateElementInstance(changedAccessPoint);
            UIComponentTools.showInformation("Changes successfully committed.");
        } catch (ConfigurationException ex) {
            UIComponentTools.showError("Failed to commit changes. Cause: " + ex.getMessage());
            String object = "the changed access point '" + selectedAccessPoint.getUniqueIdentifier() + "'";
            LOGGER.error(MsgBuilder.commitFailed(object) + "Cause: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void addNewElementInstance(StagingAccessPointConfiguration pElementToAdd) {
        getElementList().addItem(pElementToAdd.getUniqueIdentifier());
        getElementList().setItemCaption(pElementToAdd.getUniqueIdentifier(), getAccessPointItemCaption(pElementToAdd));
        getElementList().select(pElementToAdd.getUniqueIdentifier());
    }

    @Override
    public void updateElementInstance(StagingAccessPointConfiguration pElementToUpdate) {
        getElementList().addItem(pElementToUpdate.getUniqueIdentifier());
        getElementList().setItemCaption(pElementToUpdate.getUniqueIdentifier(), getAccessPointItemCaption(pElementToUpdate));
        getElementList().select(pElementToUpdate.getUniqueIdentifier());
    }

    /**
     * Check the currently selected access point. During this check, custom
     * properties will be validated. For some minor issues warnings are
     * generated, which are returned as result. For major issues an exception is
     * thrown. If 'null' is returned, an internal error which is indicated in a
     * custom way (e.g. validation of a text field has failed) has occurred. If
     * the access point is valid, an empty string is returned.
     *
     * @return En empty string in case of success, a string which contains all
     * warnings or 'null' in case of an internal validation error.
     *
     * @throws ConfigurationException in case of a major configuration error.
     */
    private void validateRequiredComponentValues() throws ConfigurationException {
        // Check implementation class of selected access point
        checkImplementationClassField();
        // Check name of selected access point
        checkNameField();
        // Check if selected access point is and can be defined as default   
        checkDefaultBox();
        // Check given remote base path   
        checkRemoteBaseUrl();
        // Check given local base path  
        checkLocalBasePath();
    }

    /**
     * Check the value of the implementation class field. The field shouldn't be
     * empty. If the field contains a valid string it is tried to load the
     * class. If this fails, a ConfigurationException is thrown. ̰
     *
     * @throws ConfigurationException If the field is empty or the class cannot
     * be found.
     */
    private void checkImplementationClassField() throws ConfigurationException {
        if (getImplementationClassField().getText() == null || getImplementationClassField().getText().isEmpty() || !UIUtils7.validate(getImplementationClassField())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Implementation class is invalid.");
        } else {
            try {
                if (Class.forName(getImplementationClassField().getText()) == null) {
                    throw new ClassNotFoundException("Implementation class " + getImplementationClassField().getText() + " not found.");
                }
            } catch (ClassNotFoundException ex) {
                throw new ConfigurationException("Implementation class " + getImplementationClassField().getText() + " not found.");
            }
        }
    }

    /**
     * Check the name field. The name field must not be empty and if currently a
     * new processor is added, there must be no processor with the same name.
     *
     * @throws ConfigurationException If the name field is empty or if another
     * processor with the same name exists.
     */
    private void checkNameField() throws ConfigurationException {
        if (UIComponentTools.isEmpty(getBasicPropertiesLayout().getNameField())
                || !UIUtils7.validate(getBasicPropertiesLayout().getNameField())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Access point name is invalid.");
        }

        String accessPointName = getBasicPropertiesLayout().getNameField().getValue();
        StagingAccessPointConfiguration selectedAccessPoint = loadElementById(getSelectedElementId());
        if (selectedAccessPoint != null && selectedAccessPoint.getId() == null) {
            //check only when we have a new access point
            StagingAccessPointConfiguration existingAccessPoint = StagingConfigurationPersistence.getSingleton(null).findAccessPointConfigurationByName(accessPointName);
            if (existingAccessPoint != null && !existingAccessPoint.getUniqueIdentifier().equals(selectedAccessPoint.getUniqueIdentifier())) {
                setPropertiesLayout(PropertiesLayoutType.BASIC);
                throw new ConfigurationException("There is already a staging access point named '"
                        + existingAccessPoint.getName() + "'.");
            }
        }
    }

    /**
     * Check the value of the 'default' checkbox. There must be no other access
     * point marked as 'default' for the selected group. Therefor, the name of
     * the current access point is compared to the currently defaulted.
     *
     * @throws ConfigurationException If there is already a default access point
     * with another name.
     */
    private void checkDefaultBox() throws ConfigurationException {
        if (!getBasicPropertiesLayout().getDefaultBox().getValue()) {
            LOGGER.debug("Access point with id '" + getSelectedElementId()
                    + "' is not defined as default access point.");
            return;
        }

        String groupId = (String) getBasicPropertiesLayout().getGroupBox().getValue();
        StagingAccessPointConfiguration defaultAccessPoint = StagingConfigurationPersistence.getSingleton(null).findDefaultAccessPointConfigurationForGroup(groupId);
        String accessPointName = getBasicPropertiesLayout().getNameField().getValue();
        if (defaultAccessPoint != null && !defaultAccessPoint.getName().equals(accessPointName)) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("There is already a default access point registered for group '"
                    + groupId + "' (Name: " + defaultAccessPoint.getName() + ")");
        }
    }

    /**
     * Check the local base path of the access point. The local base path should
     * be an existing directory. If it is not, a warning is logged as it might
     * be that the directory is created while setting up the access point later
     * on.
     *
     * @throws ConfigurationException If the provided local base path is empty.
     */
    private void checkLocalBasePath() throws ConfigurationException {
        if (UIComponentTools.isEmpty(getBasicPropertiesLayout().getLocalBasePathField())
                || !UIUtils7.validate(getBasicPropertiesLayout().getLocalBasePathField())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Local base path is invalid. ");
        } else {
            File f = new File(getBasicPropertiesLayout().getLocalBasePathField().getValue());
            if (!f.exists() || !f.isDirectory()) {
                LOGGER.warn("The provided local base path is not an existing directory. Probably it is created while checking the AP configurability.");
            }
        }
    }

    /**
     * Check the provided remove base URL. The remote base URL must be a valid
     * URL.
     *
     * @throws ConfigurationException If the provided remote base URL it not a
     * valid URL.
     */
    private void checkRemoteBaseUrl() throws ConfigurationException {
        if (UIComponentTools.isEmpty(getBasicPropertiesLayout().getRemoteBaseUrlField())
                || !UIUtils7.validate(getBasicPropertiesLayout().getRemoteBaseUrlField())) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Remote base URL is invalid.");
        } else {
            try {
                LOGGER.debug("Validated remote base URL is {}", new URL(getBasicPropertiesLayout().getRemoteBaseUrlField().getValue()).toURI());
            } catch (MalformedURLException | URISyntaxException ex) {
                throw new ConfigurationException("The provided remote base URL is not a supported URL.");
            }
        }
    }

    /**
     * Check if the provided custom configuration arguments can be used to
     * instantiate the provided access point. If the 'old' access point, which
     * is the access point before applying any changes, is already a valid
     * access point
     * ({@link #isAccessPointValid(edu.kit.dama.staging.entities.StagingAccessPointConfiguration)}
     * returns TRUE), the setup using the 'new' access point configuration is
     * only performed in check-mode. If the 'old' access point is not yet set
     * up, the setup is done normally which means that the underlying system
     * might be influenced, e.g. by creating folders.
     *
     * @param pExistingAccessPoint The currently existing access point.
     * @param pModifiedAccessPoint The modified access point that should be
     * checked.
     *
     * @throws ConfigurationException If checking pModifiedAccessPoint fails.
     */
    private void checkAccessPointConfigurability(StagingAccessPointConfiguration pExistingAccessPoint, StagingAccessPointConfiguration pModifiedAccessPoint) throws ConfigurationException {
        checkImplementationClassField();
        String implClass = getImplementationClassField().getText();
        try {
            AbstractStagingAccessPoint accessPointInstance = createAccessPointInstance(implClass, pModifiedAccessPoint);
            accessPointInstance.setup(isAccessPointValid(pExistingAccessPoint));
        } catch (ConfigurationException ex) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            throw new ConfigurationException("Failed to setup the access point using the provided properties. ", ex);
        }
    }

    /**
     * Check if the provided access point is valid. This is the case if local
     * base path, remote base URL and name are not empty and if the access point
     * is not disabled. This check does not involve any custom properties of the
     * access point.
     *
     * @param accessPoint The access point to check.
     *
     * @return TRUE if the access point is valid, FALSE otherwise.
     *
     * @throws ConfigurationException If the provided access point is null.
     */
    private boolean isAccessPointValid(StagingAccessPointConfiguration accessPoint) throws ConfigurationException {
        if (accessPoint == null) {
            throw new ConfigurationException("Failed to validate access point. "
                    + "Cause: Access point is null.");
        }
        return accessPoint.getLocalBasePath() != null
                && !accessPoint.getLocalBasePath().trim().isEmpty()
                && accessPoint.getRemoteBaseUrl() != null
                && !accessPoint.getRemoteBaseUrl().trim().isEmpty()
                && accessPoint.getName() != null
                && !accessPoint.getName().trim().isEmpty()
                && !accessPoint.isDisabled();
    }

    /**
     * Collect all settings, write them into the provided access point and
     * return it for writing it into the database.
     *
     * @throws ConfigurationException If accessPoint is null.
     */
    private StagingAccessPointConfiguration collectSettings(StagingAccessPointConfiguration pAccessPoint) throws ConfigurationException {
        if (pAccessPoint == null) {
            throw new ConfigurationException("Access point is null.");
        }

        pAccessPoint.setName(getBasicPropertiesLayout().getNameField().getValue());
        pAccessPoint.setGroupId((String) getBasicPropertiesLayout().getGroupBox().getValue());
        pAccessPoint.setRemoteBaseUrl(getBasicPropertiesLayout().getRemoteBaseUrlField().getValue());
        pAccessPoint.setLocalBasePath(getBasicPropertiesLayout().getLocalBasePathField().getValue());
        pAccessPoint.setDefaultAccessPoint(getBasicPropertiesLayout().getDefaultBox().getValue());
        pAccessPoint.setDisabled(getBasicPropertiesLayout().getDisabledBox().getValue());
        pAccessPoint.setTransientAccessPoint(getBasicPropertiesLayout().getTransientBox().getValue());
        pAccessPoint.setDescription((String) getBasicPropertiesLayout().getDescriptionArea().getValue());
        try {
            pAccessPoint.setPropertiesAsObject(getSpecificPropertiesLayout().getProperties());
        } catch (IOException ex) {
            setPropertiesLayout(PropertiesLayoutType.SPECIFIC);
            throw new ConfigurationException("Failed to obtain properties from UI.", ex);
        }
        return pAccessPoint;
    }

    /**
     * Commit all changes to the database.
     *
     * @throws ConfigurationException If committing failed.
     */
    private void commitAccessPoint(StagingAccessPointConfiguration accessPoint) throws ConfigurationException {
        try {
            StagingConfigurationPersistence.getSingleton(null).saveAccessPointConfiguration(accessPoint);
        } catch (UnauthorizedAccessAttemptException ex) {
            setPropertiesLayout(PropertiesLayoutType.BASIC);
            String object = "access point '" + accessPoint.getName() + "'";
            throw new ConfigurationException(MsgBuilder.unauthorizedSaveRequest(object), ex);
        }
    }

    /**
     * Get the (list) item caption for the provided access point.
     *
     * @param accessPoint The access point.
     *
     * @return The item caption in the format 'AccessPointName (AccessPointId)'
     */
    private String getAccessPointItemCaption(StagingAccessPointConfiguration accessPoint) {
        return accessPoint.getName() + " (" + accessPoint.getId() + ")";
    }

    @Override
    public void enableComponents(boolean pValue) {
        getImplementationClassField().setEnabled(pValue);
        getLoadImplementationClassButton().setEnabled(pValue);
    }

    @Override
    public void fillElementList() {
        List<StagingAccessPointConfiguration> accessPoints = StagingConfigurationPersistence.getSingleton(null).findAllAccessPointConfigurations();
        Collections.sort(accessPoints, (StagingAccessPointConfiguration o1, StagingAccessPointConfiguration o2) -> Long.compare(o1.getId(), o2.getId()));

        accessPoints.stream().map((accessPoint) -> {
            getElementList().addItem(accessPoint.getUniqueIdentifier());
            return accessPoint;
        }).forEachOrdered((accessPoint) -> {
            getElementList().setItemCaption(accessPoint.getUniqueIdentifier(), getAccessPointItemCaption(accessPoint));
        });
    }

    @Override
    public boolean elementWithIdExists(String pId) {
        return StagingConfigurationManager.getSingleton().isExistingAccessPoint(pId);
    }

    @Override
    public StagingAccessPointConfiguration loadElementById(String pId) {
        if (NEW_UNIQUE_ID.equals(pId)) {
            return StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration();
        }
        return StagingConfigurationManager.getSingleton().getAccessPointConfigurationById(pId);
    }

    @Override
    public void selectElement(StagingAccessPointConfiguration pSelectedElement) throws ConfigurationException, UIComponentUpdateException {
        implementationClassField.setReadOnly(false);
        if (pSelectedElement != null && pSelectedElement.getImplementationClass() != null) {
            getImplementationClassField().setText(pSelectedElement.getImplementationClass());
            getImplementationClassField().setValue(pSelectedElement.getImplementationClass());
            AbstractStagingAccessPoint accessPointInstance = createAccessPointInstance(pSelectedElement.getImplementationClass());
            try {
                getSpecificPropertiesLayout().updateComponents(accessPointInstance, pSelectedElement.getPropertiesAsObject());
            } catch (IOException ex) {
                throw new ConfigurationException("Failed to read properties from provided access point.", ex);
            }
        } else {
            getImplementationClassField().clearChoices();
            getSpecificPropertiesLayout().reset();
        }
    }
}
