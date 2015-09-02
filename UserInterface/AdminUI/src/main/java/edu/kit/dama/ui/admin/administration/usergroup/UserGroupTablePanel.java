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

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.ui.admin.container.UserGroupContainer;
import edu.kit.dama.ui.admin.exception.EnumParameterNotFoundException;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.filter.FilterProperties;
import edu.kit.dama.ui.admin.filter.UserGroupFilter;
import edu.kit.dama.ui.admin.utils.IconContainer;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UserGroupTablePanel extends CustomComponent {

    private static final Logger LOGGER
            = LoggerFactory.getLogger(UserGroupTablePanel.class);
    public final static String DEBUG_ID_PREFIX
            = UserGroupTablePanel.class.getName() + "_";

    private final UserGroupAdministrationTab userGroupAdministrationTab;
    private Table userGroupTable;
    private final HashMap<UserGroupContainer.Property, FilterProperties<String, UserGroupFilter.SearchSpace>> filters;

    public enum UserGroupEffectivity {

        NO,
        VALID,
        INVALID;
    }

    public UserGroupTablePanel(UserGroupAdministrationTab groupAdministrationTab) {
        this.userGroupAdministrationTab = groupAdministrationTab;

        filters = new HashMap<>();

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setSizeFull();
        setImmediate(true);
        setCompositionRoot(getUserGroupTable());
    }

    /**
     *
     * @return
     */
    public final Table getUserGroupTable() {
        if (userGroupTable == null) {
            buildUserGroupTable();
        }
        return userGroupTable;
    }

    /**
     *
     */
    private void buildUserGroupTable() {
        String id = "userGroupTable";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        userGroupTable = new Table();
        userGroupTable.setId(DEBUG_ID_PREFIX + id);
        userGroupTable.setSizeFull();
        userGroupTable.setImmediate(true);
        userGroupTable.setSelectable(true);
        userGroupTable.setNullSelectionAllowed(true);
        userGroupTable.setContainerDataSource(new UserGroupContainer());
        userGroupTable.setVisibleColumns(UserGroupContainer.COLUMN_ORDER);
        userGroupTable.setColumnHeaders(UserGroupContainer.COLUMN_HEADERS);

        userGroupTable.addItemSetChangeListener(new Container.ItemSetChangeListener() {

            @Override
            public void containerItemSetChange(Container.ItemSetChangeEvent event) {
                userGroupTable.refreshRowCache();
            }
        });

        userGroupTable.addValueChangeListener(new Table.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                Role loggedInUserRole = userGroupAdministrationTab.getParentApp()
                        .getLoggedInUser().getCurrentRole();
                userGroupAdministrationTab.getUserGroupForm().update(loggedInUserRole);
            }
        });

        userGroupTable.addHeaderClickListener(new Table.HeaderClickListener() {

            @Override
            public void headerClick(Table.HeaderClickEvent event) {
                if (!event.isDoubleClick()) {
                    return;
                }
                if (userGroupTable.getColumnIcon(event.getPropertyId()) == null) {
                    return;
                }
                try {
                    UserGroupContainer.Property property = UserGroupContainer.Property.getEnumByKey(
                            (String) event.getPropertyId());
                    removeTableFilter(property);
                } catch (EnumParameterNotFoundException ex) {
                    userGroupAdministrationTab.getParentApp().showError("Unknown error occurred "
                            + "while removing the table filter. " + NoteBuilder.CONTACT);
                    LOGGER.error("Failed to remove the table filter. Cause: " + ex.getMessage(), ex);
                }
            }
        });
        reloadUserGroupTable();
    }

    /**
     *
     */
    public final void reloadUserGroupTable() {
        getUserGroupTable().removeAllItems();
        try {
            List<UserGroup> userGroups = userGroupAdministrationTab.getParentApp()
                    .getMetaDataManager().find(UserGroup.class);
            for (UserGroup userGroup : userGroups) {
                getUserGroupTable().addItem(userGroup);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            String object = "information about user groups";
            userGroupAdministrationTab.getParentApp().showWarning("Group-table not reloadable! Cause: "
                    + NoteBuilder.unauthorizedGetRequest(object));
            LOGGER.warn("Failed to reload '" + getUserGroupTable().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        }
    }

    /**
     *
     * @return
     */
    public UserGroupEffectivity validateSelectedUserGroup() {
        if (getSelectedUserGroup() == null) {
            return UserGroupEffectivity.NO;
        }
        Role loggedInUserRole = userGroupAdministrationTab.getParentApp().getLoggedInUser()
                .getCurrentRole();
        String loggedInUserGroupId = userGroupAdministrationTab.getParentApp()
                .getLoggedInUser().getCurrentGroup().getStringRepresentation();
        if (loggedInUserRole.equals(Role.ADMINISTRATOR) || (loggedInUserRole.equals(Role.MANAGER)
                && loggedInUserGroupId.equals(getSelectedUserGroup().getGroupId()))) {
            return UserGroupEffectivity.VALID;
        } else {
            return UserGroupEffectivity.INVALID;
        }
    }

    /**
     *
     * @param originalUserGroup
     * @param changedUserGroup
     */
    public void updateTableEntry(UserGroup originalUserGroup, UserGroup changedUserGroup) {
        getUserGroupTable().getContainerDataSource().removeItem(originalUserGroup);
        getUserGroupTable().select(null);
        addTableEntry(changedUserGroup);
    }

    /**
     *
     * @param userGroup
     */
    public void addTableEntry(UserGroup userGroup) {
        getUserGroupTable().getContainerDataSource().addItem(userGroup);
        getUserGroupTable().sort(new Object[]{UserGroupContainer.Property.ID.propertyId}, 
                new boolean[]{false});
        getUserGroupTable().select(userGroup);
    }

    /**
     *
     * @return
     */
    public UserGroup getSelectedUserGroup() {
        return (UserGroup) getUserGroupTable().getValue();
    }

    /**
     *
     * @param property
     */
    public void removeTableFilter(UserGroupContainer.Property property) {
        // Remove all existing container filters
        UserGroupContainer container = (UserGroupContainer) getUserGroupTable().getContainerDataSource();
        container.removeAllContainerFilters();
        // Add all filters except of the filter supposed to be deleted
        filters.remove(property);
        for (UserGroupContainer.Property iProperty : filters.keySet()) {
            String filterExpression = filters.get(iProperty).filterExpression;
            UserGroupFilter.SearchSpace searchSpace = filters.get(iProperty).searchSpace;
            addTableFilter(filterExpression, iProperty, searchSpace);
        }
        // Remove filter icon from corresponding table column
        getUserGroupTable().setColumnIcon(property.propertyId, null);
        // Disable button-icon for filter removing in case all filters have been removed
        if (filters.isEmpty()) {
            userGroupAdministrationTab.getUserGroupSearch().getRemoveAllFiltersButton().setEnabled(false);
        }
        // Select first table entry
        getUserGroupTable().select(getUserGroupTable().firstItemId());
    }

    /**
     *
     */
    public void removeAllTableFilters() {
        // Remove all existing container filters
        UserGroupContainer container = (UserGroupContainer) getUserGroupTable().getContainerDataSource();
        container.removeAllContainerFilters();
        // Remove filter icon from corresponding table columns
        for (UserGroupContainer.Property property : filters.keySet()) {
            getUserGroupTable().setColumnIcon(property.propertyId, null);
        }
        // Sort all table entries by first column
        getUserGroupTable().sort(new Object[]{getUserGroupTable().getVisibleColumns()[0]},
                new boolean[]{true});
        // Select first table entry
        getUserGroupTable().select(getUserGroupTable().firstItemId());
    }

    /**
     *
     * @param filterExpression
     * @param property
     * @param searchSpace
     */
    public void addTableFilter(String filterExpression, UserGroupContainer.Property property,
            UserGroupFilter.SearchSpace searchSpace) {
        // Add requested filter
        UserGroupContainer container = (UserGroupContainer) getUserGroupTable().getContainerDataSource();
        UserGroupFilter userGroupFilter = new UserGroupFilter(filterExpression, property.propertyId, searchSpace);
        container.addContainerFilter(userGroupFilter);
        filters.put(property, new FilterProperties<>(filterExpression, searchSpace));
        // Set filter icon at corresponding table columns if missing
        ThemeResource filterAddIcon = new ThemeResource(IconContainer.FILTER_ADD);
        if (!filterAddIcon.equals(getUserGroupTable().getColumnIcon(property.propertyId))) {
            // Set missing filterAddIcon
            getUserGroupTable().setColumnIcon(property.propertyId, filterAddIcon);
        }
    }
}
