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
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
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
public class StagingAccessPointConfigurationTab extends CustomComponent {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(StagingAccessPointConfigurationTab.class);
    private static final String DEBUG_ID_PREFIX
            = StagingAccessPointConfigurationTab.class.getName() + "_";

    public static final String NEW_ACCESS_POINT_CAPTION = "NEW";
    public static final String NEW_ACCESS_POINT_UNIQUE_ID = "MINUS_ONE_ACCESS_POINT";

    private final AdminUIMainView parentApp;
    private GridLayout mainLayout;
    private ListSelect accessPointLister;
    private TextField implementationClassField;
    private NativeButton loadImplementationClassButton;
    private Panel propertiesPanel;
    private NativeButton navigationButton;
    private HorizontalLayout bulletLineLayout;
    private BasicPropertiesLayout basicPropertiesLayout;
    private SpecificPropertiesLayout specificPropertiesLayout;
    private NativeButton commitChangesButton;
    private StagingAccessPointConfiguration selectedAccessPoint
            = StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration();
    private PropertiesLayout propertiesLayout;
    private NativeButton bulletBasic;
    private NativeButton bulletSpecific;

    enum PropertiesLayout {

        BASIC,
        SPECIFIC;
    }

    enum ListSelection {

        NO,
        NEW,
        VALID,
        INVALID;
    }

    public StagingAccessPointConfigurationTab(AdminUIMainView pParentApp) {
        parentApp = pParentApp;
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));

        setCompositionRoot(getMainLayout());
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
        mainLayout.addComponent(getAccessPointLister(), 0, 0, 0, 5);
        mainLayout.addComponent(getImplementationClassField(), 1, 0);
        mainLayout.addComponent(getLoadImplementationClassButton(), 2, 0);
        mainLayout.addComponent(new Label("<p> <hr/>", ContentMode.HTML),
                1, 1, 2, 1);
        mainLayout.addComponent(getPropertiesPanel(), 1, 2);
        mainLayout.addComponent(new Label("<hr/>", ContentMode.HTML),
                1, 3, 2, 3);
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
    public final ListSelect getAccessPointLister() {
        if (accessPointLister == null) {
            buildAccessPointLister();
        }
        return accessPointLister;
    }

    /**
     *
     */
    private void buildAccessPointLister() {
        String id = "accessPointLister";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        accessPointLister = new ListSelect("AVAILABLE ACCESS POINTS");
        accessPointLister.setId(DEBUG_ID_PREFIX + id);
        accessPointLister.setWidth("95%");
        accessPointLister.setHeight("100%");
        accessPointLister.setImmediate(true);
        accessPointLister.setNullSelectionAllowed(false);
        accessPointLister.addStyleName(CSSTokenContainer.BOLD_CAPTION);

        accessPointLister.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                ListSelection listSelection = validateListSelection();
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
        String accessPointUniqueId = (String) getAccessPointLister().getValue();

        // Validate selection
        if (accessPointUniqueId == null) {
            return ListSelection.NO;
        }

        if (NEW_ACCESS_POINT_UNIQUE_ID.equals(accessPointUniqueId)) {
            // Case 1: New acess point requested
            selectedAccessPoint = StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration();
            return ListSelection.NEW;
        }

        // Find selected access point in the database
        selectedAccessPoint = StagingConfigurationPersistence.getSingleton(null)
                .findAccessPointConfigurationByUniqueIdentifier(accessPointUniqueId);

        if (selectedAccessPoint == null) { // Case 2: Access point invalid
            selectedAccessPoint = StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration();
            return ListSelection.INVALID;
        } else { // Case 3: Access point valid
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
        getImplementationClassField().setValue(selectedAccessPoint.getImplementationClass());
        try {
            AbstractStagingAccessPoint accessPointInstance
                    = createAccessPointInstance(selectedAccessPoint.getImplementationClass());
            getBasicPropertiesLayout().updateComponents(selectedAccessPoint);
            getSpecificPropertiesLayout().updateComponents(selectedAccessPoint, accessPointInstance);
            setEnabledComponents(ListSelection.VALID);
        } catch (ConfigurationException ex) {
            parentApp.showError("Update of 'Staging Access Point' not possible! Cause: " + ex.getMessage());
            LOGGER.error("Failed to update '" + StagingAccessPointConfigurationTab.class.getSimpleName() 
                    + "'. Cause: " + ex.getMessage(), ex);
            resetConfigurationComponents();
            setEnabledComponents(ListSelection.INVALID);
        } catch (UIComponentUpdateException | IOException ex) {
            parentApp.showError("Update of 'Staging Access Point' not possible! Cause: " + ex.getMessage());
            LOGGER.error(MsgBuilder.updateFailed(getPropertiesPanel().getId()) + "Cause: " + ex.getMessage(), ex);
            setEnabledComponents(ListSelection.INVALID);
        }
    }

    /**
     *
     */
    private void fireInvalidListEntrySelected() {
        parentApp.showWarning("Access point not found in database.");
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
        getImplementationClassField().setValue(selectedAccessPoint.getImplementationClass());
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

        implementationClassField = new TextField("ACCESS POINT IMPLEMENTATION CLASS");
        implementationClassField.setId(DEBUG_ID_PREFIX + id);
        implementationClassField.setImmediate(true);
        implementationClassField.setWidth("100%");
        implementationClassField.setRequired(true);
        implementationClassField.setNullRepresentation("");
        implementationClassField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        implementationClassField.setInputPrompt(
                "Enter (Java) classpath of access point implementation ...");
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
        loadImplementationClassButton.setDescription(
                "Load the entered implementation class for setting the "
                + "configuration of the requested access point");

        loadImplementationClassButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    StagingAccessPointConfiguration newAccessPoint = createAccessPoint();
                    commitAccessPoint(newAccessPoint);
                    addAccessPointListerItem(newAccessPoint);
                    setEnabledComponents(ListSelection.VALID);
                    parentApp.showNotification("Staging access point successfully created.");
                } catch (ConfigurationException ex) {
                    parentApp.showError("Implementation class not loadable! Cause: " + ex.getMessage());
                    LOGGER.error("Failed to load requested implementation class. Cause: "
                            + ex.getMessage(), ex);
                }
            }
        });
    }

    /**
     * Create a new access point. The created access point will be immediately
     * persisted. Modifications can be applied afterwards.
     *
     * <p align="right"> <b>by</b> Jejkal</p>
     */
    private StagingAccessPointConfiguration createAccessPoint() throws ConfigurationException {
        // Validate value of implementation class field
        if (!UIUtils7.validate(getImplementationClassField())) {
            throw new ConfigurationException("Invalid implementation class.");
        }

        // Valid implementation class name => Create new access point
        String implClassName = getImplementationClassField().getValue();
        try {
            // Build instance of implementation class
            createAccessPointInstance(implClassName);
        } catch (ConfigurationException ex) {
            setPropertiesLayout(PropertiesLayout.BASIC);
            throw new ConfigurationException("Failed to create access provider instance. ", ex);
        }
        // Create new access point
        StagingAccessPointConfiguration newAccessPoint
                = StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration();
        newAccessPoint.setName(implClassName.substring(implClassName.lastIndexOf(".") + 1) + " ("
                + newAccessPoint.getUniqueIdentifier() + ")");
        newAccessPoint.setImplementationClass(implClassName);
        newAccessPoint.setDisabled(true);
        return newAccessPoint;
    }

    /**
     * This access point tries to instantiate the provided access point class
     * and sets some UI fields whose values are defined by the access point. If
     * an error occurs,a ConfigurationException will be thrown.
     *
     * <p align="right"> <b>by</b> Jejkal</p>
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
    private AbstractStagingAccessPoint createAccessPointInstance(String implClassName,
            StagingAccessPointConfiguration accessPointConfig) throws ConfigurationException {
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
                    StagingAccessPointConfiguration temporaryAccessPoint
                            = copyAccessPoint(selectedAccessPoint);
                    StagingAccessPointConfiguration changedAccessPoint
                            = changeAccessPoint(selectedAccessPoint);
                    checkAccessPointConfigurability(temporaryAccessPoint, changedAccessPoint);
                    commitAccessPoint(changedAccessPoint);
                    updateAccessPointListerItem(changedAccessPoint);
                    parentApp.showNotification("Changes successfully committed.");
                } catch (ConfigurationException ex) {
                    parentApp.showError("Staging access point not modifiable! Cause: " + ex.getMessage());
                    String object = "the changed access point '" + selectedAccessPoint.getUniqueIdentifier() + "'";
                    LOGGER.error(MsgBuilder.commitFailed(object) + "Cause: " + ex.getMessage(), ex);
                }
            }
        });
    }

    /**
     *
     * @param accessPoint
     */
    public void addAccessPointListerItem(StagingAccessPointConfiguration accessPoint) {
        getAccessPointLister().addItem(accessPoint.getUniqueIdentifier());
        getAccessPointLister().setItemCaption(accessPoint.getUniqueIdentifier(),
                getAccessPointItemCaption(accessPoint));
        getAccessPointLister().select(accessPoint.getUniqueIdentifier());
    }

    /**
     *
     * @param accessPoint
     */
    public void updateAccessPointListerItem(StagingAccessPointConfiguration accessPoint) {
        getAccessPointLister().removeItem(accessPoint.getUniqueIdentifier());
        addAccessPointListerItem(accessPoint);
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
        checkRemoteBasePath();
        // Check given local base path  
        checkLocalBasePath();
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
            throw new ConfigurationException("Access point name is invalid.");
        }

        String accessPointName = getBasicPropertiesLayout().getNameField().getValue();
        if (selectedAccessPoint != null && selectedAccessPoint.getId() == null) {
            //check only when we have a new access point
            StagingAccessPointConfiguration existingAccessPoint
                    = StagingConfigurationPersistence.getSingleton(null)
                    .findAccessPointConfigurationByName(accessPointName);
            if (existingAccessPoint != null && !existingAccessPoint
                    .getUniqueIdentifier().equals(selectedAccessPoint.getUniqueIdentifier())) {
                setPropertiesLayout(PropertiesLayout.BASIC);
                throw new ConfigurationException("There is already a staging access point named '"
                        + existingAccessPoint.getName() + "'.");
            }
        }
    }

    /**
     *
     * @throws ConfigurationException
     */
    private void checkDefaultBox() throws ConfigurationException {
        if (!getBasicPropertiesLayout().getDefaultBox().getValue()) {
            LOGGER.debug("Access point '" + selectedAccessPoint.getName()
                    + "' is not defined as default access point.");
            return;
        }

        String groupId = (String) getBasicPropertiesLayout().getGroupBox().getValue();
        StagingAccessPointConfiguration defaultAccessPoint
                = StagingConfigurationPersistence.getSingleton(null)
                .findDefaultAccessPointConfigurationForGroup(groupId);
        String accessPointName = getBasicPropertiesLayout().getNameField().getValue();
        if (defaultAccessPoint != null && !defaultAccessPoint.getName()
                .equals(accessPointName)) {
            setPropertiesLayout(PropertiesLayout.BASIC);
            throw new ConfigurationException("There is already a default access point registered for group '"
                    + groupId + "' (Name: " + defaultAccessPoint.getName() + ")");
        }
    }

    /**
     * 
     * @throws ConfigurationException 
     */
    private void checkLocalBasePath() throws ConfigurationException {
        if (UIComponentTools.isEmpty(getBasicPropertiesLayout().getLocalBasePathField())
                || !UIUtils7.validate(getBasicPropertiesLayout().getLocalBasePathField())) {
            setPropertiesLayout(PropertiesLayout.BASIC);
            throw new ConfigurationException("Local base path is invalid. ");
        }
    }

    /**
     * 
     * @throws ConfigurationException 
     */
    private void checkRemoteBasePath() throws ConfigurationException {
        if (UIComponentTools.isEmpty(getBasicPropertiesLayout().getLocalBasePathField())
                || !UIUtils7.validate(getBasicPropertiesLayout().getRemoteBaseUrlField())) {
            setPropertiesLayout(PropertiesLayout.BASIC);
            throw new ConfigurationException("Remote base path is invalid. ");
        }
    }

    /**
     *
     * @param oldAccessPoint
     * @param newAccessPoint
     * @throws ConfigurationException
     */
    private void checkAccessPointConfigurability(StagingAccessPointConfiguration oldAccessPoint,
            StagingAccessPointConfiguration newAccessPoint) throws ConfigurationException {
        checkImplementationClassField();
        String implClass = getImplementationClassField().getValue();
        try {
            AbstractStagingAccessPoint accessPointInstance = createAccessPointInstance(implClass, newAccessPoint);
            accessPointInstance.setup(isAccessPointValid(oldAccessPoint));
        } catch (ConfigurationException ex) {
            setPropertiesLayout(PropertiesLayout.BASIC);
            throw new ConfigurationException("Failed to setup the access point using the provided properties. ", ex);
        }
    }

    /**
     *
     * @param accessPoint
     * @return
     * @throws ConfigurationException
     */
    private boolean isAccessPointValid(StagingAccessPointConfiguration accessPoint)
            throws ConfigurationException {
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
     *
     * @param accessPoint
     * @return
     * @throws ConfigurationException
     */
    private StagingAccessPointConfiguration copyAccessPoint(
            StagingAccessPointConfiguration accessPoint) throws ConfigurationException {
        if (accessPoint == null) {
            throw new ConfigurationException("Access point is null.");
        }
        StagingAccessPointConfiguration temporaryAccessPoint
                = StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration(
                        accessPoint.getUniqueIdentifier());
        temporaryAccessPoint.setName(accessPoint.getName());
        temporaryAccessPoint.setGroupId(accessPoint.getGroupId());
        temporaryAccessPoint.setRemoteBaseUrl(accessPoint.getRemoteBaseUrl());
        temporaryAccessPoint.setLocalBasePath(accessPoint.getLocalBasePath());
        temporaryAccessPoint.setDefaultAccessPoint(accessPoint.isDefaultAccessPoint());
        temporaryAccessPoint.setDisabled(accessPoint.isDisabled());
        temporaryAccessPoint.setTransientAccessPoint(accessPoint.isTransientAccessPoint());
        temporaryAccessPoint.setDescription(accessPoint.getDescription());
        return temporaryAccessPoint;
    }

    /**
     * Save all changes to the database.
     */
    private StagingAccessPointConfiguration changeAccessPoint(
            StagingAccessPointConfiguration accessPoint) throws ConfigurationException {
        if (accessPoint == null) {
            throw new ConfigurationException("Access point is null.");
        }

        accessPoint.setName(getBasicPropertiesLayout().getNameField().getValue());
        accessPoint.setGroupId(
                (String) getBasicPropertiesLayout().getGroupBox().getValue());
        accessPoint.setRemoteBaseUrl(
                getBasicPropertiesLayout().getRemoteBaseUrlField().getValue());
        accessPoint.setLocalBasePath(
                getBasicPropertiesLayout().getLocalBasePathField().getValue());
        accessPoint.setDefaultAccessPoint(
                getBasicPropertiesLayout().getDefaultBox().getValue());
        accessPoint.setDisabled(
                getBasicPropertiesLayout().getDisabledBox().getValue());
        accessPoint.setTransientAccessPoint(
                getBasicPropertiesLayout().getTransientBox().getValue());
        accessPoint.setDescription(
                (String) getBasicPropertiesLayout().getDescriptionArea().getValue());
        try {
            accessPoint.setPropertiesAsObject(getSpecificPropertiesLayout().getProperties());
        } catch (IOException ex) {
            setPropertiesLayout(PropertiesLayout.SPECIFIC);
            throw new ConfigurationException("Failed to obtain properties from UI.", ex);
        }
        return accessPoint;
    }

    /**
     * Commit all changes to the database.
     */
    private void commitAccessPoint(StagingAccessPointConfiguration accessPoint) throws ConfigurationException {
        try {
            StagingConfigurationPersistence.getSingleton(null).saveAccessPointConfiguration(accessPoint);
        } catch (UnauthorizedAccessAttemptException ex) {
            setPropertiesLayout(PropertiesLayout.BASIC);
            String object = "access point '" + accessPoint.getName() + "'";
            throw new ConfigurationException(MsgBuilder.unauthorizedSaveRequest(object), ex);
        }
    }

    /**
     *
     */
    public void reloadAccessPointLister() {
        getAccessPointLister().removeAllItems();

        getAccessPointLister().addItem(NEW_ACCESS_POINT_UNIQUE_ID);
        getAccessPointLister().setItemCaption(NEW_ACCESS_POINT_UNIQUE_ID,
                NEW_ACCESS_POINT_CAPTION);

        List<StagingAccessPointConfiguration> accessPoints
                = StagingConfigurationPersistence.getSingleton(null)
                .findAllAccessPointConfigurations();
        for (StagingAccessPointConfiguration accessPoint : accessPoints) {
            getAccessPointLister().addItem(accessPoint.getUniqueIdentifier());
            getAccessPointLister().setItemCaption(accessPoint.getUniqueIdentifier(),
                    getAccessPointItemCaption(accessPoint));
        }
    }

    /**
     *
     * @param accessPoint
     * @return
     */
    private String getAccessPointItemCaption(StagingAccessPointConfiguration accessPoint) {
        return accessPoint.getName() + " (" + accessPoint.getId() + ")";
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
        reloadAccessPointLister();
        getAccessPointLister().select(NEW_ACCESS_POINT_UNIQUE_ID);
        getBasicPropertiesLayout().reloadGroupBox();
        getBasicPropertiesLayout().getGroupBox().select(USERS_GROUP_ID);
    }

    
    /**
     *
     */
    public void disable() {
        getAccessPointLister().removeAllItems();
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
