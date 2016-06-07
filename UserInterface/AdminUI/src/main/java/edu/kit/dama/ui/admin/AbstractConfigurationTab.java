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

import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.BaseTheme;
import com.vaadin.ui.themes.LiferayTheme;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.types.IConfigurable;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import static edu.kit.dama.util.Constants.USERS_GROUP_ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public abstract class AbstractConfigurationTab<C> extends CustomComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfigurationTab.class);
    private String DEBUG_ID_PREFIX = AbstractConfigurationTab.class.getName() + "_";
    public static final String NEW_UNIQUE_ID = "MINUS_ONE_ELEMENT";

    public static final String NEW_ELEMENT_LABEL = "NEW";

    private final AdminUIMainView parentApp;
    private GridLayout mainLayout;
    private ListSelect elementList;
    private GenericSpecificPropertiesLayout specificPropertiesLayout;
    private NativeButton bulletBasic;
    private NativeButton bulletSpecific;
    private Panel propertiesPanel;
    private NativeButton commitChangesButton;
    private PropertiesLayoutType propertiesLayoutType;
    private NativeButton navigateRightButton;
    private NativeButton navigateLeftButton;
    private HorizontalLayout bulletLineLayout;

    private String selectedElementId;

    public enum PropertiesLayoutType {

        BASIC,
        SPECIFIC;
    }

    public enum ListSelection {

        NO,
        NEW,
        VALID,
        INVALID;
    }

    /**
     * Default constructor.
     *
     * @param pParentApp The parent application.
     */
    public AbstractConfigurationTab(AdminUIMainView pParentApp) {
        parentApp = pParentApp;
        DEBUG_ID_PREFIX += hashCode() + "_";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1) + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));

        setCompositionRoot(getMainLayout());
    }

    /**
     * Get the tab main layout.
     *
     * @return The main layout.
     */
    private GridLayout getMainLayout() {
        if (mainLayout == null) {
            mainLayout = buildMainLayout();
        }
        return mainLayout;
    }

    /**
     * Get the parent application.
     *
     * @return The parent application.
     */
    public AdminUIMainView getParentApp() {
        return parentApp;
    }

    /**
     * Build the main layout. The implementation of this method has to build and
     * return the main configuration layout of this tab located right to the
     * element list.
     *
     * @return The main layout as GridLayout.
     */
    public abstract GridLayout buildMainLayout();

    /**
     * Returns the element id which is currently selected in the element list.
     * The result could be either NEW_UNIQUE_ID if the 'New' entry is selected
     * or one of the element ids added to the list while calling
     * {@link #fillElementList()}.
     *
     * @return The selected element id.
     */
    public final String getSelectedElementId() {
        return selectedElementId;
    }

    /**
     * Get the element list containing the selection of elements that can be
     * configured using this tab.
     *
     * @return The element list.
     */
    public final ListSelect getElementList() {
        if (elementList == null) {
            String id = "elementList";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            elementList = new ListSelect("AVAILABLE ELEMENTS");
            elementList.setId(DEBUG_ID_PREFIX + id);
            elementList.setWidth("95%");
            elementList.setHeight("100%");
            elementList.setImmediate(true);
            elementList.setNullSelectionAllowed(false);
            elementList.addStyleName(CSSTokenContainer.BOLD_CAPTION);

            elementList.addValueChangeListener(new Property.ValueChangeListener() {

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
                                    + "element selection. " + NoteBuilder.CONTACT, -1);
                            LOGGER.error("Failed to update " + this.getClass().getSimpleName()
                                    + ". Cause: Undefined enum constant detected, namely '"
                                    + listSelection.name() + "'.");
                            break;
                    }
                }

            });
        }
        return elementList;
    }

    /**
     * Reload the element list by removing all elements, adding the dummy
     * element for creating new entries and calling {@link #fillElementList()}
     * to fill the remaining elements.
     *
     */
    public void reloadElementList() {
        elementList.removeAllItems();
        elementList.addItem(NEW_UNIQUE_ID);
        elementList.setItemCaption(NEW_UNIQUE_ID, NEW_ELEMENT_LABEL);
        fillElementList();
    }

    /**
     * Fill the element list with all currently existing elements.
     */
    public abstract void fillElementList();

    /**
     * Check whether an element with the provided ID already exists or not.
     *
     * @param pId The element id.
     *
     * @return TRUE if an element exits, FALS otherwise.
     */
    public abstract boolean elementWithIdExists(String pId);

    /**
     * Load an element by its ID.
     *
     * @param pId A valid element id or NEW_UNIQUE_ID for returning a new
     * element.
     *
     * @return The element or null if no element exists for the provided id.
     */
    public abstract C loadElementById(String pId);

    /**
     * Select the provided element, means filling the UI accordingly.
     *
     * @param pSelection The selected element.
     *
     * @throws ConfigurationException If the configuration of pSelection is
     * somehow invalid.
     * @throws UIComponentUpdateException If pSelection could not be used to
     * update the UI properly.
     */
    public abstract void selectElement(C pSelection) throws ConfigurationException, UIComponentUpdateException;

    /**
     * Reset the configuration part of the tab.
     */
    public void resetConfigurationComponents() {
        resetComponents();
        getBasicPropertiesLayout().reset();
        getSpecificPropertiesLayout().reset();
        setPropertiesLayout(PropertiesLayoutType.BASIC);
    }

    /**
     * Reset specific components of the configuration part of the tab, e.g. the
     * implementation class field which might not be available for all element
     * configuration tabs.
     */
    public abstract void resetComponents();

    /**
     * Enable/disable components depending on the element list selection denoted
     * by the provided value.
     *
     * @param listSelection The element list selection type.
     */
    public void setEnabledComponents(ListSelection listSelection) {
        switch (listSelection) {
            case NO:
            case INVALID:
                getPropertiesPanel().setEnabled(false);
                getCommitChangesButton().setEnabled(false);
                enableComponents(false);
                break;
            case NEW:
                getPropertiesPanel().setEnabled(false);
                getCommitChangesButton().setEnabled(false);
                enableComponents(true);
                break;
            case VALID:
                getPropertiesPanel().setEnabled(true);
                getCommitChangesButton().setEnabled(true);
                enableComponents(false);
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "element. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                        + ". Cause: Undefined enum constant detected, namely '"
                        + listSelection.name() + "'.");
        }
    }

    /**
     * Enable/Disable specific components of the configuration part of the tab,
     * e.g. the implementation class field which might not be available for all
     * element configuration tabs.
     *
     * @param pValue TRUE = enable specific components; disable them otherwise.
     */
    public abstract void enableComponents(boolean pValue);

    /**
     * Returns the commit button used to commit changes.
     *
     * @return The commit button.
     */
    public NativeButton getCommitChangesButton() {
        if (commitChangesButton == null) {
            String id = "commitChangesButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            commitChangesButton = new NativeButton("Commit Changes");
            commitChangesButton.setId(DEBUG_ID_PREFIX + id);
            commitChangesButton.setImmediate(true);

            commitChangesButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    commitChanges();
                }
            });
        }
        return commitChangesButton;
    }

    /**
     * Do commit changes. This method is triggered by the commit button.
     */
    public abstract void commitChanges();

    /**
     * Add the provided element instance to the element list. Providing this
     * method in an abstract way allows the implementation to decide how element
     * list entries are rendered and what are their ids.
     *
     * @param pItem The element instance to add.
     */
    public abstract void addNewElementInstance(C pItem);

    /**
     * Update the provided element instance in the element list. During the
     * update the element with the id pf pItem might be removed and re-added
     * afterwards. Providing this method in an abstract way allows the
     * implementation to decide how element list entries are rendered and what
     * are their ids.
     *
     * @param pItem The element instance to update.
     */
    public abstract void updateElementInstance(C pItem);

    /**
     * Set the selected properties layout type. Depending on the type, the
     * configuration part of the tab may change.
     *
     * @param pPropertiesLayoutType The new type.
     */
    public void setPropertiesLayout(PropertiesLayoutType pPropertiesLayoutType) {
        this.propertiesLayoutType = pPropertiesLayoutType;
        switch (pPropertiesLayoutType) {
            case BASIC:
                fireBasicPropertiesLayoutSelected();
                break;
            case SPECIFIC:
                fireSpecificPropertiesLayoutSelected();
                break;
            default:
                UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                        + "element view. " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                        + ". Cause: Undefined enum constant detected, namely '"
                        + pPropertiesLayoutType.name() + "'.");
        }
    }

    /**
     * Get the properties panel. The properties panel contains the configuration
     * part of the tab. There are two representations for each element: the
     * BASIC and the SPECIFIC one. Their look and their content depend on their
     * specific implementation.
     *
     * @return The panel holding the configuration part.
     */
    public Panel getPropertiesPanel() {
        if (propertiesPanel == null) {
            String id = "propertiesPanel";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            propertiesPanel = new Panel();
            propertiesPanel.setId(DEBUG_ID_PREFIX + id);
            propertiesPanel.setHeight("300px");
            propertiesPanel.setStyleName(LiferayTheme.PANEL_LIGHT);
            propertiesPanel.setImmediate(true);

            setPropertiesLayout(PropertiesLayoutType.BASIC);
        }
        return propertiesPanel;
    }

    /**
     * Reload the entire UI. This will reload the element list, selects the
     * 'NEW' entry, which causes a reload of the configuration part and
     * resetting it to the BASIC type.
     */
    public void reload() {
        reloadElementList();
        elementList.select(NEW_UNIQUE_ID);
        getBasicPropertiesLayout().reloadGroupBox();
        getBasicPropertiesLayout().getGroupBox().select(USERS_GROUP_ID);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (enabled) {
            super.setEnabled(true);
            reload();
        } else {
            if (!isEnabled()) {
                //have to do this in order to change values
                setEnabled(true);
            }
            getElementList().removeAllItems();
            getBasicPropertiesLayout().getGroupBox().removeAllItems();
            super.setEnabled(false);
        }
    }

    /**
     *
     */
    public final void update() {
        Role loggedInUserRole = parentApp.getLoggedInUser().getCurrentRole();
        switch (loggedInUserRole) {
            case ADMINISTRATOR:
            case MANAGER:
                setEnabled(true);
                break;
            default:
                setEnabled(false);
                UIComponentTools.showWarning("WARNING", "Unauthorized access attempt! " + NoteBuilder.CONTACT, -1);
                LOGGER.error("Failed to update " + this.getId() + ". Cause: Unauthorized access attempt!");
                break;
        }
        reload();
    }

    /**
     * Get the navigation button to switch between BASIC and SPECIFIC view.
     *
     * @return The navigation button.
     */
    public NativeButton getNavigateRightButton() {
        if (navigateRightButton == null) {
            String id = "navigateRightButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            navigateRightButton = new NativeButton();
            navigateRightButton.setId(DEBUG_ID_PREFIX + id);
            navigateRightButton.setIcon(new ThemeResource(IconContainer.NAVIGATE_RIGHT));
            navigateRightButton.setStyleName(BaseTheme.BUTTON_LINK);
            navigateRightButton.setImmediate(true);

            navigateRightButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    switch (getPropertiesLayout()) {
                        case BASIC:
                            setPropertiesLayout(PropertiesLayoutType.SPECIFIC);
                            break;
                        case SPECIFIC:
                            setPropertiesLayout(PropertiesLayoutType.BASIC);
                            break;
                        default:
                            UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                                    + "element view. " + NoteBuilder.CONTACT, -1);
                            LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                                    + ". Cause: Undefined enum constant detected, namely '"
                                    + getPropertiesLayout().name() + "'.");
                            break;
                    }
                }
            });
        }
        return navigateRightButton;
    }
    
    /**
     * Get the navigation button to switch between BASIC and SPECIFIC view.
     *
     * @return The navigation button.
     */
    public NativeButton getNavigateLeftButton() {
        if (navigateLeftButton == null) {
            String id = "navigateLeftButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            navigateLeftButton = new NativeButton();
            navigateLeftButton.setId(DEBUG_ID_PREFIX + id);
            navigateLeftButton.setIcon(new ThemeResource(IconContainer.NAVIGATE_LEFT));
            navigateLeftButton.setStyleName(BaseTheme.BUTTON_LINK);
            navigateLeftButton.setImmediate(true);

            navigateLeftButton.addClickListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    switch (getPropertiesLayout()) {
                        case BASIC:
                            setPropertiesLayout(PropertiesLayoutType.SPECIFIC);
                            break;
                        case SPECIFIC:
                            setPropertiesLayout(PropertiesLayoutType.BASIC);
                            break;
                        default:
                            UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                                    + "element view. " + NoteBuilder.CONTACT, -1);
                            LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                                    + ". Cause: Undefined enum constant detected, namely '"
                                    + getPropertiesLayout().name() + "'.");
                            break;
                    }
                }
            });
        }
        return navigateLeftButton;
    }

    /**
     * Get the bullet button for accessing the BASIC view directly.
     *
     * @return The bullet button.
     */
    public NativeButton getBulletBasic() {
        if (bulletBasic == null) {
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
                            setPropertiesLayout(PropertiesLayoutType.BASIC);
                            break;
                        default:
                            UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                                    + "element. " + NoteBuilder.CONTACT, -1);
                            LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                                    + ". Cause: Undefined enum constant detected, namely '"
                                    + getPropertiesLayout().name() + "'.");
                            break;
                    }
                }
            });
        }
        return bulletBasic;
    }

    /**
     * Get the bullet button for accessing the SPECIFIC view directly.
     *
     * @return The bullet button.
     */
    public NativeButton getBulletSpecific() {
        if (bulletSpecific == null) {
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
                            setPropertiesLayout(PropertiesLayoutType.SPECIFIC);
                            break;
                        case SPECIFIC:
                            break;
                        default:
                            UIComponentTools.showError("ERROR", "Unknown error occurred while updating "
                                    + "element. " + NoteBuilder.CONTACT, -1);
                            LOGGER.error("Failed to update " + getPropertiesPanel().getId()
                                    + ". Cause: Undefined enum constant detected, namely '"
                                    + getPropertiesLayout().name() + "'.");
                            break;
                    }
                }
            });
        }
        return bulletSpecific;
    }

    /**
     * Get the bullet button layout.
     *
     * @return The bullet button layout
     */
    public HorizontalLayout getBulletLineLayout() {
        if (bulletLineLayout == null) {
            String id = "bulletLineLayout";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            bulletLineLayout = new HorizontalLayout();
            bulletLineLayout.setId(DEBUG_ID_PREFIX + id);
            bulletLineLayout.setImmediate(true);

            bulletLineLayout.addComponent(getBulletBasic());
            bulletLineLayout.addComponent(getBulletSpecific());
        }
        return bulletLineLayout;
    }

    /**
     * Get the BASIC properties layout. As this layout might be different for
     * all configurable element this method must be implemented by the according
     * configuration tab.
     *
     * @return The base properties layout.
     */
    public abstract AbstractBasePropertiesLayout getBasicPropertiesLayout();

    /**
     * Get the SPECIFIC properties layout. The default layout can be used
     * together with elements implementing the {@link IConfigurable} interface
     * to configure supported attributes. Elements not implementing this
     * interface may return an own layout implementation.
     *
     * @return The specific properties layout.
     */
    public GenericSpecificPropertiesLayout getSpecificPropertiesLayout() {
        if (specificPropertiesLayout == null) {
            specificPropertiesLayout = new GenericSpecificPropertiesLayout();
        }
        return specificPropertiesLayout;
    }

    /**
     * Get the selected properties layout type.
     *
     * @return The selected properties layout type.
     */
    private PropertiesLayoutType getPropertiesLayout() {
        return propertiesLayoutType;
    }

    /**
     * Handler for changing the configuration part to the BASIC type.
     */
    private void fireBasicPropertiesLayoutSelected() {
        getPropertiesPanel().setContent(getBasicPropertiesLayout());
        getBulletBasic().setIcon(new ThemeResource(IconContainer.BULLET_GREEN));
        getBulletSpecific().setIcon(new ThemeResource(IconContainer.BULLET_GREY));
    }

    /**
     * Handler for changing the configuration part to the SPECIFIC type.
     */
    private void fireSpecificPropertiesLayoutSelected() {
        getPropertiesPanel().setContent(getSpecificPropertiesLayout());
        getBulletSpecific().setIcon(new ThemeResource(IconContainer.BULLET_GREEN));
        getBulletBasic().setIcon(new ThemeResource(IconContainer.BULLET_GREY));
    }

    /**
     * Event handler that is called if the 'New' element is selected in the
     * element list. It resets the configuration part of the tab and
     * enables/disables components according to the selection.
     */
    private void fireNewInstanceSelected() {
        resetConfigurationComponents();
        setEnabledComponents(ListSelection.NEW);
    }

    /**
     * Event handler that is called if a valid and existing element is selected
     * in the element list. It loads the element, fils the configuration part of
     * the tab and enables/disables components according to the selection.
     */
    private void fireValidListEntrySelected() {
        C selectedElement = loadElementById(selectedElementId);

        try {
            selectElement(selectedElement);
            getBasicPropertiesLayout().updateSelection(selectedElement);
            setEnabledComponents(ListSelection.VALID);
        } catch (ConfigurationException ex) {
            getParentApp().showError("Update of element selection not possible! Cause: " + ex.getMessage());
            LOGGER.error("Failed to update '" + this.getClass().getSimpleName()
                    + "'. Cause: " + ex.getMessage(), ex);
            resetComponents();
            setEnabledComponents(ListSelection.INVALID);
        } catch (UIComponentUpdateException ex) {
            getParentApp().showError("Update of element list selection not possible! Cause: " + ex.getMessage());
            LOGGER.error(MsgBuilder.updateFailed(getPropertiesPanel().getId()) + "Cause: " + ex.getMessage(), ex);
            setEnabledComponents(ListSelection.INVALID);
        }
    }

    /**
     * Event handler that is called if an invalid element (e.g. which is no
     * longer available) is selected in the element list. It resets the
     * configuration part of the tab, enables/disables components according to
     * the selection and shows a warning.
     */
    private void fireInvalidListEntrySelected() {
        resetConfigurationComponents();
        setEnabledComponents(ListSelection.INVALID);
        parentApp.showWarning("Selected element not found in database.");
    }

    /**
     * Event handler that is called if no element is selected in the element
     * list. It resets the configuration part of the tab and enables/disables
     * components according to the selection.
     */
    private void fireNoListEntrySelected() {
        resetConfigurationComponents();
        setEnabledComponents(ListSelection.NO);
    }

    /**
     * Validate the element list selection and return an according enum.
     * Depending on the enum's value UI elements might be enabled/disabled.
     *
     * @return The selection type enum.
     */
    private ListSelection validateListSelection() {
        // Get selected list entry
        String selection = (String) getElementList().getValue();
        LOGGER.debug("Validating list selection for current value '{}'", selection);
        // Validate selection
        if (selection == null) {
            LOGGER.warn("Found NO selection. This should only happen during commit operation.");
            return ListSelection.NO;
        }

        if (NEW_UNIQUE_ID.equals(selection)) {
            LOGGER.debug("Found NEW selection.");
            // Case 1: New element requested
            selectedElementId = selection;
            return ListSelection.NEW;
        }

        // Find selected element in the database
        selectedElementId = (elementWithIdExists(selection)) ? selection : NEW_UNIQUE_ID;

        if (NEW_UNIQUE_ID.equals(selection)) { // Case 2: Element invalid
            LOGGER.debug("Found INVALID selection.");
            selectedElementId = NEW_UNIQUE_ID;
            return ListSelection.INVALID;
        } else { // Case 3: Selection valid
            LOGGER.debug("Found VALID selection.");
            return ListSelection.VALID;
        }
    }

}
