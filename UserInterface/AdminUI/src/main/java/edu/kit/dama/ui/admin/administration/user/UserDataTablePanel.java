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

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.ui.admin.container.UserDataContainer;
import edu.kit.dama.ui.admin.exception.EnumParameterNotFoundException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.filter.FilterProperties;
import edu.kit.dama.ui.admin.filter.UserDataFilter;
import edu.kit.dama.ui.admin.utils.IconContainer;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UserDataTablePanel extends CustomComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataTablePanel.class);
    public static final String DEBUG_ID_PREFIX = UserDataTablePanel.class.getName() + "_";

    private final UserDataAdministrationTab userDataAdministrationTab;
    private Table userDataTable;
    private final HashMap<UserDataContainer.Property, FilterProperties<String, UserDataFilter.SearchSpace>> filters;

    public enum UserDataEffictivity {

        /**
         * No user selected. => No user data available.
         */
        NO,
        /**
         * Valid user selected. => User data are unrestrictedly available.
         */
        VALID,
        /**
         * User with the role 'NO_ACCESS' selected. => User data are
         * restrictedly available.
         */
        DISABLED_USER,
        /**
         * User selected him-/herself. => User data are restrictedly available.
         */
        LOGGED_IN_USER,
        /**
         * Invalid selection due of some errors. => No user data available.
         */
        INVALID;
    }

    /**
     *
     * @param userAdministrationTab
     */
    public UserDataTablePanel(UserDataAdministrationTab userAdministrationTab) {
        this.userDataAdministrationTab = userAdministrationTab;

        filters = new HashMap<>();

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setSizeFull();
        setImmediate(true);
        setCompositionRoot(getUserDataTable());
    }

    /**
     *
     * @return
     */
    public final Table getUserDataTable() {
        if (userDataTable == null) {
            buildTable();
        }
        return userDataTable;
    }

    /**
     *
     */
    private void buildTable() {
        String id = "userDataTable";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        userDataTable = new Table() {

            @Override
            protected String formatPropertyValue(Object rowId, Object colId,
                    Property property) {
                Object v = property.getValue();
                if (v instanceof Date) {
                    Date dateValue = (Date) v;
                    return new SimpleDateFormat("dd.MM.yyyy").format(dateValue);
                }
                return super.formatPropertyValue(rowId, colId, property);
            }

        };

        userDataTable.setId(DEBUG_ID_PREFIX + id);
        userDataTable.setImmediate(true);
        userDataTable.setSizeFull();
        userDataTable.setSelectable(true);
        userDataTable.setNullSelectionAllowed(true);
        userDataTable.setContainerDataSource(new UserDataContainer());
        userDataTable.setVisibleColumns(UserDataContainer.COLUMN_ORDER);
        userDataTable.setColumnHeaders(UserDataContainer.COLUMN_HEADERS);

        userDataTable.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Role loggedInUserRole = userDataAdministrationTab.getParentApp()
                        .getLoggedInUser().getCurrentRole();
                userDataAdministrationTab.getUserDataForm().update(loggedInUserRole);
            }
        });

        userDataTable.addItemSetChangeListener(new Container.ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(Container.ItemSetChangeEvent event) {
                userDataTable.refreshRowCache();
            }
        });

        userDataTable.addHeaderClickListener(new Table.HeaderClickListener() {

            @Override
            public void headerClick(Table.HeaderClickEvent event) {
                if (!event.isDoubleClick()) {
                    return;
                }
                if (userDataTable.getColumnIcon(event.getPropertyId()) == null) {
                    return;
                }
                try {
                    UserDataContainer.Property property = UserDataContainer.Property.getEnumByKey(
                            (String) event.getPropertyId());
                    removeTableFilter(property);
                } catch (EnumParameterNotFoundException ex) {
                    userDataAdministrationTab.getParentApp().showError("Unknown error occurred "
                            + "while removing the table filter. " + NoteBuilder.CONTACT);
                    LOGGER.error("Failed to remove the table filter. Cause: " + ex.getMessage(), ex);
                }
            }
        });

        reloadUserDataTable();
    }

    /**
     *
     */
    public final void reloadUserDataTable() {
        getUserDataTable().removeAllItems();
        try {
            List<UserData> allUsersData = userDataAdministrationTab.getParentApp()
                    .getMetaDataManager().find(UserData.class);
            for (UserData userData : allUsersData) {
                getUserDataTable().addItem(userData);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "information about users";
            userDataAdministrationTab.getParentApp().showWarning("User-table not reloadable! Cause: "
                    + NoteBuilder.unauthorizedGetRequest(object));
            LOGGER.error("Failed to reload '" + getUserDataTable().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        }
    }

    /**
     *
     * @return
     */
    public UserDataEffictivity validateSelectedUserData() {
        if (getSelectedUserData() == null) {
            return UserDataEffictivity.NO;
        }

        UserData loggedInUser = userDataAdministrationTab.getParentApp().getLoggedInUser();
        if (loggedInUser.getDistinguishedName().equals(getSelectedUserData().getDistinguishedName())) {
            return UserDataEffictivity.LOGGED_IN_USER;
        }

        UserId userId = new UserId(getSelectedUserData().getDistinguishedName());
        try {
            IAuthorizationContext authCtx = userDataAdministrationTab.getParentApp().getAuthorizationContext();
            Role userRole = (Role) UserServiceLocal.getSingleton().getRoleRestriction(userId, authCtx);
            if (userRole.atMost(Role.NO_ACCESS)) {
                return UserDataEffictivity.DISABLED_USER;
            } else {
                return UserDataEffictivity.VALID;
            }
        } catch (EntityNotFoundException ex) {
            String object = "user '" + userId.getStringRepresentation() + "'";
            LOGGER.error("Failed to check if selected user is disabled. Cause: "
                    + MsgBuilder.notFound(object), ex);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "the maximum role of " + userId.getStringRepresentation();
            LOGGER.warn("Failed to check if selected user is disabled. Cause: "
                    + NoteBuilder.unauthorizedChangeRequest(object), ex);
        } catch (AuthorizationException ex) {
            LOGGER.warn("Failed to check if selected user is disabled. Cause: "
                    + MsgBuilder.unauthorizedContext(), ex);
        }
        return UserDataEffictivity.INVALID;
    }

    /**
     * Returns a collection of all userDataTable items.
     *
     * @return allTableItems - collection of all userDataTable items
     */
    public final Collection<UserData> getAllTableItems() {
        Collection<UserData> allTableItems = (Collection<UserData>) getUserDataTable().getItemIds();
        return allTableItems;
    }

    /**
     *
     * @param originalUserData
     * @param changedUserData
     */
    public void updateTableEntry(UserData originalUserData, UserData changedUserData) {
        getUserDataTable().getContainerDataSource().removeItem(originalUserData);
        getUserDataTable().select(null);
        addTableEntry(originalUserData, changedUserData);
    }

    /**
     *
     * @param originalUserData
     * @param changedUserData
     */
    public void addTableEntry(UserData originalUserData, UserData changedUserData) {
        getUserDataTable().getContainerDataSource().addItem(changedUserData);
        getUserDataTable().sort(new Object[]{UserDataContainer.Property.USER_ID.propertyId}, 
                new boolean[]{false});
        getUserDataTable().select(changedUserData);
    }

    /**
     *
     * @return
     */
    public UserData getSelectedUserData() {
        return (UserData) getUserDataTable().getValue();
    }

    /**
     *
     * @param property
     */
    public void removeTableFilter(UserDataContainer.Property property) {
        // Remove all existing container filters
        UserDataContainer container = (UserDataContainer) getUserDataTable().getContainerDataSource();
        container.removeAllContainerFilters();
        // Add all filters except of the filter supposed to be deleted
        filters.remove(property);
        for (UserDataContainer.Property iProperty : filters.keySet()) {
            String filterExpression = filters.get(iProperty).filterExpression;
            UserDataFilter.SearchSpace searchSpace = filters.get(iProperty).searchSpace;
            addTableFilter(filterExpression, iProperty, searchSpace);
        }
        // Remove filter icon from corresponding table column
        getUserDataTable().setColumnIcon(property.propertyId, null);
        // Disable button-icon for filter removing in case all filters have been removed
        if (filters.isEmpty()) {
            userDataAdministrationTab.getUserDataSearch().getRemoveAllFiltersButton().setEnabled(false);
        }
        // Select first table entry
        getUserDataTable().select(getUserDataTable().firstItemId());
    }

    /**
     *
     */
    public void removeAllTableFilters() {
        // Remove all existing container filters
        UserDataContainer container = (UserDataContainer) getUserDataTable().getContainerDataSource();
        container.removeAllContainerFilters();
        // Remove filter icon from corresponding table columns
        for (UserDataContainer.Property property : filters.keySet()) {
            getUserDataTable().setColumnIcon(property.propertyId, null);
        }
        // Sort all table entries by first column
        getUserDataTable().sort(new Object[]{getUserDataTable().getVisibleColumns()[0]}, new boolean[]{true});
        // Select first table entry
        getUserDataTable().select(getUserDataTable().firstItemId());
    }

    /**
     *
     * @param filterExpression
     * @param property
     * @param searchSpace
     */
    public void addTableFilter(String filterExpression, UserDataContainer.Property property,
            UserDataFilter.SearchSpace searchSpace) {
        // Add requested filter
        UserDataContainer container = (UserDataContainer) getUserDataTable().getContainerDataSource();
        UserDataFilter userDataFilter = new UserDataFilter(filterExpression, property.propertyId, searchSpace);
        container.addContainerFilter(userDataFilter);
        filters.put(property, new FilterProperties<>(filterExpression, searchSpace));
        // Set filter icon at corresponding table columns if missing
        ThemeResource filterAddIcon = new ThemeResource(IconContainer.FILTER_ADD);
        if (!filterAddIcon.equals(getUserDataTable().getColumnIcon(property.propertyId))) {
            // Set missing filterAddIcon
            getUserDataTable().setColumnIcon(property.propertyId, filterAddIcon);
        }
    }
}
