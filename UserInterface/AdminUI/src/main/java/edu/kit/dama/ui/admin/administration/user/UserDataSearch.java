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
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PopupView;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.BaseTheme;
import edu.kit.dama.ui.admin.container.UserDataContainer;
import edu.kit.dama.ui.admin.filter.UserDataFilter;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.admin.utils.IconContainer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UserDataSearch extends HorizontalLayout {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataSearch.class);
    public static final String DEBUG_ID_PREFIX = UserDataSearch.class.getName() + "_";

    private final UserDataAdministrationTab userDataAdministrationTab;

    private PopupView view;
    private GridLayout viewLayout;
    private TextField searchField;
    private Button addFilterButton;
    private Button removeAllFiltersButton;
    private ComboBox columnBox;
    private Button filterButton;
    private OptionGroup filterOptions;

    public UserDataSearch(UserDataAdministrationTab userDataAdministrationTab) {

        this.userDataAdministrationTab = userDataAdministrationTab;
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));

        setImmediate(true);

        addComponent(getView());
        addComponent(getRemoveAllFiltersButton());
        addComponent(getAddFilterButton());

        setComponentAlignment(getView(), Alignment.MIDDLE_CENTER);
        setComponentAlignment(getRemoveAllFiltersButton(), Alignment.BOTTOM_RIGHT);
        setComponentAlignment(getAddFilterButton(), Alignment.BOTTOM_RIGHT);
    }

    /**
     *
     * @return
     */
    public final PopupView getView() {
        if (view == null) {
            buildView();
        }
        return view;
    }

    /**
     *
     */
    private void buildView() {
        String id = "View";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        view = new PopupView(null, getViewLayout());
        view.setId(DEBUG_ID_PREFIX + id);
        view.setImmediate(true);
        view.setHideOnMouseOut(false);
        view.addStyleName(CSSTokenContainer.FILTER_POSITION);

        view.addPopupVisibilityListener(new PopupView.PopupVisibilityListener() {

            @Override
            public void popupVisibilityChange(PopupView.PopupVisibilityEvent event) {
                if (!event.isPopupVisible()) {
                    Table table = userDataAdministrationTab.getUserDataTablePanel()
                            .getUserDataTable();
                    table.select(table.firstItemId());
                    getColumnBox().select(null);
                    getFilterOptions().select(UserDataFilter.SearchSpace.STARTS);
                }
            }
        });
    }

    /**
     *
     * @return
     */
    public GridLayout getViewLayout() {
        if (viewLayout == null) {
            buildViewLayout();
        }
        return viewLayout;
    }

    /**
     *
     */
    private void buildViewLayout() {
        String id = "viewLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        viewLayout = new GridLayout(2, 3);
        viewLayout.setId(DEBUG_ID_PREFIX + id);
        viewLayout.setImmediate(true);
        viewLayout.setWidth("300px");
        viewLayout.setSpacing(true);
        viewLayout.setMargin(new MarginInfo(true, true, true, true));
        viewLayout.setCaption("FILTER SETTER");
        viewLayout.addStyleName(CSSTokenContainer.GREY_CAPTION);

        viewLayout.addComponent(getColumnBox(), 0, 0);
        viewLayout.addComponent(getFilterOptions(), 1, 0, 1, 1);
        viewLayout.addComponent(getSearchField(), 0, 1);
        viewLayout.addComponent(getFilterButton(), 1, 2);
        viewLayout.setComponentAlignment(getFilterButton(), Alignment.BOTTOM_RIGHT);

        viewLayout.setColumnExpandRatio(0, 0.7f);
        viewLayout.setColumnExpandRatio(1, 0.3f);
    }

    /**
     *
     * @return
     */
    public ComboBox getColumnBox() {
        if (columnBox == null) {
            buildColumnBox();
        }
        return columnBox;
    }

    /**
     *
     */
    private void buildColumnBox() {
        String id = "columnBox";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        columnBox = new ComboBox("TABLE COLUMN");
        columnBox.setId(DEBUG_ID_PREFIX + id);
        columnBox.setImmediate(true);
        columnBox.setRequired(true);
        columnBox.setSizeFull();
        columnBox.setNullSelectionAllowed(false);
        columnBox.setDescription("Table column that shall be filtered");
        columnBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);

        columnBox.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() == null
                        && !getSearchField().isReadOnly()) {
                    getSearchField().setValue(null);
                    getSearchField().setReadOnly(true);
                } else if (event.getProperty().getValue() != null
                        && !getSearchField().isReadOnly()) {
                    getSearchField().setValue(null);
                } else {
                    getSearchField().setReadOnly(false);
                }
            }
        });

        reloadColumnBox();
    }

    /**
     *
     * @return
     */
    public OptionGroup getFilterOptions() {
        if (filterOptions == null) {
            buildFilterOptions();
        }
        return filterOptions;
    }

    /**
     *
     */
    private void buildFilterOptions() {
        String id = "filterOptions";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        filterOptions = new OptionGroup("FILTER OPTION");
        filterOptions.setId(DEBUG_ID_PREFIX + id);
        filterOptions.setImmediate(true);
        filterOptions.setRequired(true);
        filterOptions.setSizeFull();
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

    /**
     * @return the searchField
     */
    public final TextField getSearchField() {
        if (searchField == null) {
            buildSearchField();
        }
        return searchField;
    }

    /**
     *
     */
    private void buildSearchField() {
        String id = "searchField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        searchField = new TextField("FILTER EXPRESSION");
        searchField.setId(DEBUG_ID_PREFIX + id);
        searchField.setWidth("100%");
        searchField.setImmediate(true);
        searchField.setNullRepresentation("");
        searchField.setReadOnly(true);
        searchField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        searchField.setDescription("Expression after which the selected table "
                + "column will be filtered");
    }

    /**
     *
     * @return
     */
    public final Button getFilterButton() {
        if (filterButton == null) {
            buildFilterButton();
        }
        return filterButton;
    }

    /**
     *
     */
    private void buildFilterButton() {
        String id = "filterButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        filterButton = new Button("Filter");
        filterButton.setId(DEBUG_ID_PREFIX + id);
        filterButton.setImmediate(true);
        filterButton.setWidth("100%");
        filterButton.setClickShortcut(KeyCode.ENTER, new int[]{});

        filterButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                String filterExpression = (String) getSearchField().getValue();
                // Check if searchField is empty
                if (filterExpression == null || filterExpression.isEmpty()) {
                    return;
                }
                // Filter selected column
                UserDataContainer.Property property = (UserDataContainer.Property) getColumnBox().getValue();
                UserDataFilter.SearchSpace searchSpace = (UserDataFilter.SearchSpace) getFilterOptions().getValue();
                userDataAdministrationTab.getUserDataTablePanel().addTableFilter(
                        filterExpression, property, searchSpace);
                getRemoveAllFiltersButton().setEnabled(true);
                getView().setPopupVisible(false);
            }
        });
    }

    /**
     *
     */
    private void reloadColumnBox() {
        getColumnBox().removeAllItems();
        getColumnBox().addItems(Arrays.asList(UserDataContainer.Property.values()));

        for (UserDataContainer.Property property : UserDataContainer.Property.values()) {
            getColumnBox().setItemCaption(property, property.columnHeader);
        }
    }

    /**
     *
     * @return
     */
    public final Button getAddFilterButton() {
        if (addFilterButton == null) {
            buildAddFilterButton();
        }
        return addFilterButton;
    }

    /**
     *
     */
    private void buildAddFilterButton() {
        String id = "filterButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        addFilterButton = new Button();
        addFilterButton.setId(DEBUG_ID_PREFIX + id);
        addFilterButton.setIcon(new ThemeResource(IconContainer.FILTER_ADD));
        addFilterButton.setImmediate(true);
        addFilterButton.setStyleName(BaseTheme.BUTTON_LINK);
        addFilterButton.setDescription("Click to add a table column filter");

        addFilterButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                getView().setPopupVisible(true);
            }
        });
    }

    /**
     *
     * @return
     */
    public final Button getRemoveAllFiltersButton() {
        if (removeAllFiltersButton == null) {
            buildRemoveAllFiltersButton();
        }
        return removeAllFiltersButton;
    }

    /**
     *
     */
    private void buildRemoveAllFiltersButton() {
        String id = "removeAllFiltersButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        removeAllFiltersButton = new Button();
        removeAllFiltersButton.setId(DEBUG_ID_PREFIX + id);
        removeAllFiltersButton.setIcon(new ThemeResource(IconContainer.FILTER_DELETE));
        removeAllFiltersButton.setImmediate(true);
        removeAllFiltersButton.setStyleName(BaseTheme.BUTTON_LINK);
        removeAllFiltersButton.setDescription("Click to remove ALL filters. <p>"
                + "Double-click on a column header for removing its filter.");
        removeAllFiltersButton.setEnabled(false);
        removeAllFiltersButton.setDisableOnClick(true);

        removeAllFiltersButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                userDataAdministrationTab.getUserDataTablePanel().removeAllTableFilters();
            }
        });
    }
}
