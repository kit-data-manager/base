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
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.filter.FilterProperties;
import edu.kit.dama.ui.admin.filter.UserDataFilter;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.commons.util.UIHelper;
import java.text.SimpleDateFormat;
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
    public final static String ID_COLUMN_ID = "ID";
    public final static String USERID_COLUMN_ID = "USERID";
    public final static String FIRST_NAME_COLUMN_ID = "FIRSTNAME";
    public final static String LAST_NAME_COLUMN_ID = "LASTNAME";
    public final static String EMAIL_COLUMN_ID = "EMAIL";
    public final static String VALID_FROM_COLUMN_ID = "VALIDFROM";
    public final static String VALID_UNTIL_COLUMN_ID = "VALIDUNTIL";

    private final UserDataAdministrationTab userDataAdministrationTab;
    private Table userDataTable;
    private final HashMap<String, FilterProperties<String, UserDataFilter.SearchSpace>> filters;

    public enum UserDataEffectivity {

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
        setCompositionRoot(getUserDataTable());
    }

    protected final Table getUserDataTable() {
        if (userDataTable == null) {
            String id = "userDataTable";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

            userDataTable = new Table();

            userDataTable.setId(DEBUG_ID_PREFIX + id);
            userDataTable.setSizeFull();
            userDataTable.setSelectable(true);
            userDataTable.setNullSelectionAllowed(true);

            IndexedContainer c = new IndexedContainer();

            c.addContainerProperty(ID_COLUMN_ID, Long.class, null);
            c.addContainerProperty(USERID_COLUMN_ID, String.class, null);
            c.addContainerProperty(FIRST_NAME_COLUMN_ID, String.class, null);
            c.addContainerProperty(LAST_NAME_COLUMN_ID, String.class, null);
            c.addContainerProperty(EMAIL_COLUMN_ID, String.class, null);
            c.addContainerProperty(VALID_FROM_COLUMN_ID, Date.class, null);
            c.addContainerProperty(VALID_UNTIL_COLUMN_ID, Date.class, null);

            userDataTable.setContainerDataSource(c);
            userDataTable.setColumnHeader(ID_COLUMN_ID, "ID");
            userDataTable.setColumnHeader(USERID_COLUMN_ID, "DISTINGUISHED NAME");
            userDataTable.setColumnHeader(FIRST_NAME_COLUMN_ID, "FIRST NAME");
            userDataTable.setColumnHeader(LAST_NAME_COLUMN_ID, "LAST NAME");
            userDataTable.setColumnHeader(EMAIL_COLUMN_ID, "EMAIL");
            userDataTable.setColumnHeader(VALID_FROM_COLUMN_ID, "VALID FROM");
            userDataTable.setColumnHeader(VALID_UNTIL_COLUMN_ID, "VALID UNTIL");

            Table.ColumnGenerator dateGenerator = (Table source, Object itemId, Object columnId) -> {
                Date date = (Date) source.getContainerDataSource().getContainerProperty(itemId, columnId).getValue();
                Label cellContent = new Label();
                if (date != null) {
                    cellContent.setValue(new SimpleDateFormat("dd.MM.yyyy").format(date));
                } else {
                    cellContent.setValue("-");
                }
                return cellContent;
            };

            userDataTable.addGeneratedColumn(VALID_FROM_COLUMN_ID, dateGenerator);
            userDataTable.addGeneratedColumn(VALID_UNTIL_COLUMN_ID, dateGenerator);

            userDataTable.addValueChangeListener((Property.ValueChangeEvent event) -> {
                userDataAdministrationTab.getUserDataForm().update(UIHelper.getSessionUserRole());
            });

            userDataTable.addItemSetChangeListener((Container.ItemSetChangeEvent event) -> {
                userDataTable.refreshRowCache();
            });

            userDataTable.addHeaderClickListener((Table.HeaderClickEvent event) -> {
                if (!event.isDoubleClick()) {
                    return;
                }
                if (userDataTable.getColumnIcon(event.getPropertyId()) == null) {
                    return;
                }

                removeTableFilter((String) event.getPropertyId());
            });

            reloadUserDataTable();
        }
        return userDataTable;
    }

    protected final void reloadUserDataTable() {
        getUserDataTable().getContainerDataSource().removeAllItems();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());
        try {
            List<UserData> allUsersData = mdm.find(UserData.class);
            allUsersData.forEach((userData) -> {
                getUserDataTable().getContainerDataSource().addItem(userData.getUserId());
                getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), ID_COLUMN_ID).setValue(userData.getUserId());
                getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), USERID_COLUMN_ID).setValue(userData.getDistinguishedName());
                getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), FIRST_NAME_COLUMN_ID).setValue(userData.getFirstName());
                getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), LAST_NAME_COLUMN_ID).setValue(userData.getLastName());
                getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), EMAIL_COLUMN_ID).setValue(userData.getEmail());
                getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), VALID_FROM_COLUMN_ID).setValue(userData.getValidFrom());
                getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), VALID_UNTIL_COLUMN_ID).setValue(userData.getValidUntil());
            });
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "information about users";
            UIComponentTools.showWarning("User-table not reloadable! Cause: "
                    + NoteBuilder.unauthorizedGetRequest(object));
            LOGGER.error("Failed to reload '" + getUserDataTable().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        } finally {
            mdm.close();
        }
    }

    protected final UserDataEffectivity validateSelectedUserData() {
        UserData selection = getSelectedUserData();
        if (selection == null) {
            return UserDataEffectivity.NO;
        }

        UserData loggedInUser = UIHelper.getSessionUser();
        if (loggedInUser.getDistinguishedName().equals(selection.getDistinguishedName())) {
            return UserDataEffectivity.LOGGED_IN_USER;
        }

        UserId userId = new UserId(selection.getDistinguishedName());
        try {
            IAuthorizationContext authCtx = UIHelper.getSessionContext();
            Role userRole = (Role) UserServiceLocal.getSingleton().getRoleRestriction(userId, authCtx);
            if (userRole.atMost(Role.NO_ACCESS)) {
                return UserDataEffectivity.DISABLED_USER;
            } else {
                return UserDataEffectivity.VALID;
            }
        } catch (EntityNotFoundException ex) {
            String object = "user '" + userId.getStringRepresentation() + "'";
            LOGGER.error("Failed to check if selected user is disabled. Cause: "
                    + MsgBuilder.notFound(object), ex);
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "the maximum role of " + userId.getStringRepresentation();
            LOGGER.warn("Failed to check if selected user is disabled. Cause: "
                    + NoteBuilder.unauthorizedChangeRequest(object), ex);
        }
        return UserDataEffectivity.INVALID;
    }

    protected final void updateTableEntry(UserData userData) {
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), FIRST_NAME_COLUMN_ID).setValue(userData.getFirstName());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), LAST_NAME_COLUMN_ID).setValue(userData.getLastName());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), VALID_FROM_COLUMN_ID).setValue(userData.getValidFrom());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), VALID_UNTIL_COLUMN_ID).setValue(userData.getValidUntil());
        getUserDataTable().refreshRowCache();
    }

    protected final void addTableEntry(UserData userData) {
        getUserDataTable().getContainerDataSource().addItem(userData.getUserId());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), ID_COLUMN_ID).setValue(userData.getUserId());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), USERID_COLUMN_ID).setValue(userData.getDistinguishedName());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), FIRST_NAME_COLUMN_ID).setValue(userData.getFirstName());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), LAST_NAME_COLUMN_ID).setValue(userData.getLastName());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), EMAIL_COLUMN_ID).setValue(userData.getEmail());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), VALID_FROM_COLUMN_ID).setValue(userData.getValidFrom());
        getUserDataTable().getContainerDataSource().getContainerProperty(userData.getUserId(), VALID_UNTIL_COLUMN_ID).setValue(userData.getValidUntil());
        getUserDataTable().select(userData.getUserId());
    }

    protected final UserData getSelectedUserData() {
        Long id = (Long) getUserDataTable().getValue();
        if (id != null) {
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            mdm.setAuthorizationContext(UIHelper.getSessionContext());
            try {
                return mdm.find(UserData.class, id);
            } catch (UnauthorizedAccessAttemptException ex) {
                //not authorized
            } finally {
                mdm.close();
            }
        }
        return null;
    }

    protected final void removeTableFilter(String columnId) {
        // Remove all existing container filters
        IndexedContainer container = (IndexedContainer) getUserDataTable().getContainerDataSource();
        container.removeAllContainerFilters();
        // Add all filters except of the filter supposed to be deleted
        filters.remove(columnId);
        filters.keySet().forEach((iProperty) -> {
            String filterExpression = filters.get(columnId).filterExpression;
            UserDataFilter.SearchSpace searchSpace = filters.get(iProperty).searchSpace;
            addTableFilter(filterExpression, columnId, searchSpace);
        });
        // Remove filter icon from corresponding table column
        getUserDataTable().setColumnIcon(columnId, null);
        // Disable button-icon for filter removing in case all filters have been removed
        if (filters.isEmpty()) {
            userDataAdministrationTab.getRemoveAllFiltersButton().setEnabled(false);
        }
        // Select first table entry
        getUserDataTable().select(getUserDataTable().firstItemId());
    }

    protected final void removeAllTableFilters() {
        // Remove all existing container filters
        IndexedContainer container = (IndexedContainer) getUserDataTable().getContainerDataSource();
        container.removeAllContainerFilters();
        // Remove filter icon from corresponding table columns
        filters.keySet().forEach((property) -> {
            getUserDataTable().setColumnIcon(property, null);
        });
        // Sort all table entries by first column
        getUserDataTable().sort(new Object[]{getUserDataTable().getVisibleColumns()[0]}, new boolean[]{true});
        // Select first table entry
        getUserDataTable().select(getUserDataTable().firstItemId());
    }

    protected final void addTableFilter(String filterExpression, String columnId, UserDataFilter.SearchSpace searchSpace) {
        // Add requested filter
        IndexedContainer container = (IndexedContainer) getUserDataTable().getContainerDataSource();
        UserDataFilter userDataFilter = new UserDataFilter(filterExpression, columnId, searchSpace);
        container.addContainerFilter(userDataFilter);
        filters.put(columnId, new FilterProperties<>(filterExpression, searchSpace));
        // Set filter icon at corresponding table columns if missing
        ThemeResource filterAddIcon = new ThemeResource(IconContainer.FILTER_ADD);
        if (!filterAddIcon.equals(getUserDataTable().getColumnIcon(columnId))) {
            // Set missing filterAddIcon
            getUserDataTable().setColumnIcon(columnId, filterAddIcon);
        }
    }
}
