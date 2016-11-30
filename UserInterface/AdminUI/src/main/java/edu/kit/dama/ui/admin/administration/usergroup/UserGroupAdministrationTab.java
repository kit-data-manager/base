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
package edu.kit.dama.ui.admin.administration.usergroup;

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
import edu.kit.dama.ui.admin.filter.UserDataFilter;
import edu.kit.dama.ui.admin.filter.UserGroupFilter;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIHelper;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UserGroupAdministrationTab extends CustomComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupAdministrationTab.class);
    public final static String DEBUG_ID_PREFIX = UserGroupAdministrationTab.class.getName() + "_";

    private VerticalLayout mainLayout;
    private UserGroupForm userGroupForm;
    private UserGroupTablePanel userGroupTablePanel;
    //filter related components
    private GridLayout filterWindowLayout;
    private TextField filterSearchField;
    private Button addFilterButton;
    private Button removeAllFiltersButton;
    private ComboBox columnBox;
    private Button filterButton;
    private OptionGroup filterOptions;
    private Window filterWindow;

    public UserGroupAdministrationTab() {
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setCompositionRoot(getMainLayout());
    }

    public final void reload() {
        getUserGroupTablePanel().reloadUserGroupTable();
    }

    public final void update() {
        //just reload the table and trigger a selection event. Everything else will depend on the selection. 
        reload();
        getUserGroupForm().update(UIHelper.getSessionUserRole());
    }

    protected UserGroupTablePanel getUserGroupTablePanel() {
        if (userGroupTablePanel == null) {
            userGroupTablePanel = new UserGroupTablePanel(this);
        }
        return userGroupTablePanel;
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
            mainLayout.addComponent(getUserGroupForm());
            mainLayout.addComponent(filterButtons);
            mainLayout.addComponent(getUserGroupTablePanel());

            mainLayout.setComponentAlignment(filterButtons, Alignment.BOTTOM_RIGHT);
        }
        return mainLayout;
    }

    protected UserGroupForm getUserGroupForm() {
        if (userGroupForm == null) {
            userGroupForm = new UserGroupForm(this);
        }
        return userGroupForm;
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
                Table table = getUserGroupTablePanel().getUserGroupTable();
                table.select(table.firstItemId());
                getColumnBox().select(null);
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

            filterWindowLayout.addComponent(getColumnBox(), 0, 0);
            filterWindowLayout.addComponent(getFilterOptions(), 1, 0, 1, 1);
            filterWindowLayout.addComponent(getFilterSearchField(), 0, 1);
            filterWindowLayout.addComponent(getFilterButton(), 1, 2);
            filterWindowLayout.setComponentAlignment(getFilterButton(), Alignment.BOTTOM_RIGHT);

            filterWindowLayout.setColumnExpandRatio(0, 0.7f);
            filterWindowLayout.setColumnExpandRatio(1, 0.3f);
        }
        return filterWindowLayout;
    }

    private ComboBox getColumnBox() {
        if (columnBox == null) {
            String id = "columnBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            columnBox = new ComboBox("TABLE COLUMN");
            columnBox.setId(DEBUG_ID_PREFIX + id);
            columnBox.setRequired(true);
            columnBox.setWidth("150px");
            columnBox.setNullSelectionAllowed(false);
            columnBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            columnBox.setDescription("Table column that shall be filtered");

            columnBox.addValueChangeListener((Property.ValueChangeEvent event) -> {
                getFilterSearchField().setValue("");
            });

            reloadColumnBox();
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
            filterOptions.addItems(Arrays.asList(UserGroupFilter.SearchSpace.values()));
            for (UserGroupFilter.SearchSpace option : UserGroupFilter.SearchSpace.values()) {
                filterOptions.setItemCaption(option, option.caption);
            }
            filterOptions.select(UserGroupFilter.SearchSpace.STARTS);
        }
        return filterOptions;
    }

    private TextField getFilterSearchField() {
        if (filterSearchField == null) {
            String id = "searchField";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            filterSearchField = new TextField("FILTER EXPRESSION");
            filterSearchField.setId(DEBUG_ID_PREFIX + id);
            filterSearchField.setWidth("150px");
            filterSearchField.setNullRepresentation("");
            filterSearchField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            filterSearchField.setDescription("Expression after which the selected table "
                    + "column will be filtered");
        }
        return filterSearchField;
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
                getColumnBox().getValue();
                UserGroupFilter.SearchSpace searchSpace = (UserGroupFilter.SearchSpace) getFilterOptions().getValue();
                getUserGroupTablePanel().addTableFilter(filterExpression, (String) getColumnBox().getValue(), searchSpace);
                getRemoveAllFiltersButton().setEnabled(true);
                closeFilterWindow();
            });
        }
        return filterButton;
    }

    private void reloadColumnBox() {
        getColumnBox().removeAllItems();
        getColumnBox().addItem(UserGroupTablePanel.ID_COLUMN_ID);
        getColumnBox().addItem(UserGroupTablePanel.GROUPID_COLUMN_ID);
        getColumnBox().addItem(UserGroupTablePanel.NAME_COLUMN_ID);
        getColumnBox().addItem(UserGroupTablePanel.DESCRIPTION_COLUMN_ID);
        getColumnBox().setItemCaption(UserGroupTablePanel.ID_COLUMN_ID, "ID");
        getColumnBox().setItemCaption(UserGroupTablePanel.GROUPID_COLUMN_ID, "GROUPID");
        getColumnBox().setItemCaption(UserGroupTablePanel.NAME_COLUMN_ID, "GROUP NAME");
        getColumnBox().setItemCaption(UserGroupTablePanel.DESCRIPTION_COLUMN_ID, "DESCRIPTION");
    }

    private Button getAddFilterButton() {
        if (addFilterButton == null) {
            String id = "addFilterButton";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            addFilterButton = new Button();
            addFilterButton.setId(DEBUG_ID_PREFIX + id);
            addFilterButton.setIcon(new ThemeResource(IconContainer.FILTER_ADD));
            addFilterButton.setStyleName(BaseTheme.BUTTON_LINK);
            addFilterButton.setDescription("Click to add a table column filter");

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
                getUserGroupTablePanel().removeAllTableFilters();
            });
        }
        return removeAllFiltersButton;
    }
}
