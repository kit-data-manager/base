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
package edu.kit.dama.ui.admin.administration.user;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.ErrorMessage;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import edu.kit.dama.authorization.entities.Role;
import static edu.kit.dama.authorization.entities.Role.ADMINISTRATOR;
import static edu.kit.dama.authorization.entities.Role.MANAGER;
import edu.kit.dama.ui.admin.filter.UserDataFilter;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.commons.util.UIHelper;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UserDataAdministrationTab extends CustomComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataAdministrationTab.class);
    private static final String DEBUG_ID_PREFIX = UserDataAdministrationTab.class.getName() + "_";

    private VerticalLayout mainLayout;
    private UserDataForm userDataForm;
    private UserDataTablePanel userDataTablePanel;
    //filter related components
    private Window filterWindow;
    private GridLayout filterWindowLayout;
    private TextField searchField;
    private Button addFilterButton;
    private Button removeAllFiltersButton;
    private ComboBox columnBox;
    private Button filterButton;
    private OptionGroup filterOptions;

    public UserDataAdministrationTab() {
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");
        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setCompositionRoot(getMainLayout());
    }

    public final void reload() {
        userDataTablePanel.reloadUserDataTable();
    }

    private VerticalLayout getMainLayout() {
        if (mainLayout == null) {
            String id = "mainLayout";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            mainLayout = new VerticalLayout();
            mainLayout.setId(DEBUG_ID_PREFIX + id);
            mainLayout.setSizeFull();
            mainLayout.setMargin(true);
            mainLayout.setSpacing(true);

            HorizontalLayout filterButtons = new HorizontalLayout(getRemoveAllFiltersButton(), getAddFilterButton());

            filterButtons.setComponentAlignment(getRemoveAllFiltersButton(), Alignment.BOTTOM_RIGHT);
            filterButtons.setComponentAlignment(getAddFilterButton(), Alignment.BOTTOM_RIGHT);

            // Add components to mainLayout
            mainLayout.addComponent(getUserDataForm());
            mainLayout.addComponent(filterButtons);
            mainLayout.addComponent(getUserDataTablePanel());

            mainLayout.setComponentAlignment(filterButtons, Alignment.BOTTOM_RIGHT);
        }
        return mainLayout;
    }

    public final void update() {
        Role loggedInUserRole = UIHelper.getSessionUserRole();
        switch (loggedInUserRole) {
            case ADMINISTRATOR:
            case MANAGER:
                if (!isEnabled()) {
                    setEnabled(true);
                }
                reload();
                break;
            default:
                if (isEnabled()) {
                    setEnabled(false);
                }
                getUserDataTablePanel().getUserDataTable().removeAllItems();
                break;
        }
        getUserDataForm().update(UIHelper.getSessionUserRole());
    }

    protected final UserDataForm getUserDataForm() {
        if (userDataForm == null) {
            userDataForm = new UserDataForm(this);
        }
        return userDataForm;
    }

    protected final UserDataTablePanel getUserDataTablePanel() {
        if (userDataTablePanel == null) {
            userDataTablePanel = new UserDataTablePanel(this);
        }
        return userDataTablePanel;
    }

    /**
     * TABLE FILTERING RELATED FUNCTIONALITY *
     */
    private void openFilterWindow() {
        if (filterWindow == null) {
            String id = "View";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            filterWindow = new Window("Filter Users");
            filterWindow.setContent(getFilterWindowLayout());
            filterWindow.setId(DEBUG_ID_PREFIX + id);
            filterWindow.addStyleName(CSSTokenContainer.FILTER_POSITION);

            filterWindow.addCloseListener((event) -> {
                Table table = getUserDataTablePanel().getUserDataTable();
                table.select(table.firstItemId());
                getFilterColumnBox().select(null);
                getFilterOptions().select(UserDataFilter.SearchSpace.STARTS);
                closeFilterWindow();
            });
            filterWindow.center();
            UI.getCurrent().addWindow(filterWindow);
        }
    }

    private void closeFilterWindow() {
        if (filterWindow != null) {
            UI.getCurrent().removeWindow(filterWindow);
            filterWindow = null;
        }
    }

    private GridLayout getFilterWindowLayout() {
        if (filterWindowLayout == null) {
            String id = "viewLayout";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            filterWindowLayout = new GridLayout(2, 3);
            filterWindowLayout.setId(DEBUG_ID_PREFIX + id);
            filterWindowLayout.setSpacing(true);
            filterWindowLayout.setMargin(true);
            filterWindowLayout.addStyleName(CSSTokenContainer.GREY_CAPTION);

            filterWindowLayout.addComponent(getFilterColumnBox(), 0, 0);
            filterWindowLayout.addComponent(getFilterOptions(), 1, 0, 1, 1);
            filterWindowLayout.addComponent(getFilterSearchField(), 0, 1);
            filterWindowLayout.addComponent(getFilterButton(), 1, 2);
            filterWindowLayout.setComponentAlignment(getFilterButton(), Alignment.BOTTOM_RIGHT);

            filterWindowLayout.setColumnExpandRatio(0, 0.7f);
            filterWindowLayout.setColumnExpandRatio(1, 0.3f);
        }
        return filterWindowLayout;
    }

    private ComboBox getFilterColumnBox() {
        if (columnBox == null) {
            String id = "columnBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            columnBox = new ComboBox("TABLE COLUMN");
            columnBox.setId(DEBUG_ID_PREFIX + id);
            columnBox.setRequired(true);
            columnBox.setWidth("150px");
            columnBox.setNullSelectionAllowed(false);
            columnBox.setDescription("Table column that shall be filtered");
            columnBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);

            columnBox.addValueChangeListener((Property.ValueChangeEvent event) -> {
                //reset search field on change
                getFilterSearchField().setValue("");
            });

            reloadFilterColumnBox();
        }
        return columnBox;
    }

    private OptionGroup getFilterOptions() {
        if (filterOptions == null) {
            String id = "filterOptions";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            filterOptions = new OptionGroup("FILTER OPTION");
            filterOptions.setId(DEBUG_ID_PREFIX + id);
            filterOptions.setRequired(true);
            filterOptions.setWidth("150px");
            filterOptions.setNullSelectionAllowed(false);
            filterOptions.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            filterOptions.setDescription("Part of the cell content where the filter "
                    + "expression is supposed to be found");
            filterOptions.addItems(Arrays.asList(UserDataFilter.SearchSpace.values()));
            for (UserDataFilter.SearchSpace option : UserDataFilter.SearchSpace.values()) {
                filterOptions.setItemCaption(option, option.caption);
            }
            filterOptions.select(UserDataFilter.SearchSpace.STARTS);
        }
        return filterOptions;
    }

    private TextField getFilterSearchField() {
        if (searchField == null) {
            String id = "searchField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            searchField = new TextField("FILTER EXPRESSION");
            searchField.setRequired(true);
            searchField.setId(DEBUG_ID_PREFIX + id);
            searchField.setWidth("150px");
            searchField.setNullRepresentation("");
            searchField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            searchField.setDescription("Expression after which the selected table "
                    + "column will be filtered.");
        }
        return searchField;
    }

    private Button getFilterButton() {
        if (filterButton == null) {
            String id = "filterButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            filterButton = new Button("Filter");
            filterButton.setId(DEBUG_ID_PREFIX + id);
            filterButton.setWidth("100%");
            filterButton.setSizeFull();
            filterButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, new int[]{});

            filterButton.addClickListener((Button.ClickEvent event) -> {
                String filterExpression = (String) getFilterSearchField().getValue();
                // Check if searchField is empty
                if (filterExpression == null || filterExpression.isEmpty()) {
                    getFilterSearchField().setComponentError(new ErrorMessage() {
                        @Override
                        public ErrorMessage.ErrorLevel getErrorLevel() {
                            return ErrorMessage.ErrorLevel.ERROR;
                        }

                        @Override
                        public String getFormattedHtmlMessage() {
                            return "Filter expression must not be empty.";
                        }
                    });
                    return;
                } else {
                    getFilterSearchField().setComponentError(null);
                }
                // Filter selected column
                UserDataFilter.SearchSpace searchSpace = (UserDataFilter.SearchSpace) getFilterOptions().getValue();
                getUserDataTablePanel().addTableFilter(filterExpression, (String) getFilterColumnBox().getValue(), searchSpace);
                getRemoveAllFiltersButton().setEnabled(true);
                closeFilterWindow();
            });
        }
        return filterButton;
    }

    private void reloadFilterColumnBox() {
        getFilterColumnBox().removeAllItems();
        getFilterColumnBox().addItem(UserDataTablePanel.ID_COLUMN_ID);
        getFilterColumnBox().addItem(UserDataTablePanel.USERID_COLUMN_ID);
        getFilterColumnBox().addItem(UserDataTablePanel.FIRST_NAME_COLUMN_ID);
        getFilterColumnBox().addItem(UserDataTablePanel.LAST_NAME_COLUMN_ID);
        getFilterColumnBox().addItem(UserDataTablePanel.EMAIL_COLUMN_ID);
        getFilterColumnBox().addItem(UserDataTablePanel.VALID_FROM_COLUMN_ID);
        getFilterColumnBox().addItem(UserDataTablePanel.VALID_UNTIL_COLUMN_ID);

        getFilterColumnBox().setItemCaption(UserDataTablePanel.ID_COLUMN_ID, "ID");
        getFilterColumnBox().setItemCaption(UserDataTablePanel.USERID_COLUMN_ID, "DISTINGUISHED NAME");
        getFilterColumnBox().setItemCaption(UserDataTablePanel.FIRST_NAME_COLUMN_ID, "FIRST NAME");
        getFilterColumnBox().setItemCaption(UserDataTablePanel.LAST_NAME_COLUMN_ID, "LAST NAME");
        getFilterColumnBox().setItemCaption(UserDataTablePanel.EMAIL_COLUMN_ID, "EMAIL");
        getFilterColumnBox().setItemCaption(UserDataTablePanel.VALID_FROM_COLUMN_ID, "VALID FROM");
        getFilterColumnBox().setItemCaption(UserDataTablePanel.VALID_UNTIL_COLUMN_ID, "VALID UNTIL");
    }

    private Button getAddFilterButton() {
        if (addFilterButton == null) {
            String id = "filterButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            addFilterButton = new Button();
            addFilterButton.setId(DEBUG_ID_PREFIX + id);
            addFilterButton.setIcon(new ThemeResource(IconContainer.FILTER_ADD));
            addFilterButton.setStyleName(BaseTheme.BUTTON_LINK);
            addFilterButton.setDescription("Click to add a table column filter.");

            addFilterButton.addClickListener((Button.ClickEvent event) -> {
                openFilterWindow();
            });
        }
        return addFilterButton;
    }

    protected final Button getRemoveAllFiltersButton() {
        if (removeAllFiltersButton == null) {
            String id = "removeAllFiltersButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            removeAllFiltersButton = new Button();
            removeAllFiltersButton.setId(DEBUG_ID_PREFIX + id);
            removeAllFiltersButton.setIcon(new ThemeResource(IconContainer.FILTER_DELETE));
            removeAllFiltersButton.setStyleName(BaseTheme.BUTTON_LINK);
            removeAllFiltersButton.setDescription("Click to remove ALL filters. Hint: "
                    + "Double-click on a column header for removing its filter.");
            removeAllFiltersButton.setEnabled(false);
            removeAllFiltersButton.setDisableOnClick(true);

            removeAllFiltersButton.addClickListener((Button.ClickEvent event) -> {
                getUserDataTablePanel().removeAllTableFilters();
            });
        }
        return removeAllFiltersButton;
    }

}
