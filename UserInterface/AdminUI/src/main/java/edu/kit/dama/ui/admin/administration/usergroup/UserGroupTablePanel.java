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
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Table;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.filter.FilterProperties;
import edu.kit.dama.ui.admin.filter.UserGroupFilter;
import edu.kit.dama.ui.admin.utils.IconContainer;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.admin.utils.UIHelper;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public final class UserGroupTablePanel extends CustomComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupTablePanel.class);
    public final static String DEBUG_ID_PREFIX = UserGroupTablePanel.class.getName() + "_";

    public final static String ID_COLUMN_ID = "ID";
    public final static String GROUPID_COLUMN_ID = "GROUPID";
    public final static String NAME_COLUMN_ID = "NAME";
    public final static String DESCRIPTION_COLUMN_ID = "DESCRIPTION";

    private final UserGroupAdministrationTab userGroupAdministrationTab;
    private Table userGroupTable;
    private final HashMap<String, FilterProperties<String, UserGroupFilter.SearchSpace>> filters;

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

    protected final Table getUserGroupTable() {
        if (userGroupTable == null) {
            String id = "userGroupTable";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            userGroupTable = new Table();
            userGroupTable.setId(DEBUG_ID_PREFIX + id);
            userGroupTable.setSizeFull();
            userGroupTable.setSelectable(true);
            userGroupTable.setNullSelectionAllowed(true);

            IndexedContainer c = new IndexedContainer();

            c.addContainerProperty(ID_COLUMN_ID, Long.class, null);
            c.addContainerProperty(GROUPID_COLUMN_ID, String.class, null);
            c.addContainerProperty(NAME_COLUMN_ID, String.class, null);
            c.addContainerProperty(DESCRIPTION_COLUMN_ID, String.class, null);

            userGroupTable.setContainerDataSource(c);
            userGroupTable.setColumnHeader(ID_COLUMN_ID, "ID");
            userGroupTable.setColumnHeader(GROUPID_COLUMN_ID, "GROUP ID");
            userGroupTable.setColumnHeader(NAME_COLUMN_ID, "GROUP NAME");
            userGroupTable.setColumnHeader(DESCRIPTION_COLUMN_ID, "GROUP DESCRIPTION");

            userGroupTable.addItemSetChangeListener((Container.ItemSetChangeEvent event) -> {
                userGroupTable.refreshRowCache();
            });

            userGroupTable.addValueChangeListener((Property.ValueChangeEvent event) -> {
                //update form if selected user changes
                UserGroup selectedGroup = getSelectedUserGroup();
                if (selectedGroup == null) {
                    //no group selected, use session context
                    userGroupAdministrationTab.getUserGroupForm().update(UIHelper.getSessionUserRole());
                } else {
                    //group selected, use group-related context
                    userGroupAdministrationTab.getUserGroupForm().update(UIHelper.getSessionContext(new GroupId(selectedGroup.getGroupId())).getRoleRestriction());
                }
            });

            userGroupTable.addHeaderClickListener((Table.HeaderClickEvent event) -> {
                if (!event.isDoubleClick()) {
                    return;
                }
                if (userGroupTable.getColumnIcon(event.getPropertyId()) == null) {
                    return;
                }
                removeTableFilter((String) event.getPropertyId());
            });
            reloadUserGroupTable();
        }
        return userGroupTable;
    }

    protected final void reloadUserGroupTable() {
        getUserGroupTable().getContainerDataSource().removeAllItems();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(UIHelper.getSessionContext());
        try {
            List<UserGroup> userGroups = mdm.find(UserGroup.class);
            userGroups.forEach((userGroup) -> {
                getUserGroupTable().getContainerDataSource().addItem(userGroup.getId());
                getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), ID_COLUMN_ID).setValue(userGroup.getId());
                getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), GROUPID_COLUMN_ID).setValue(userGroup.getGroupId());
                getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), NAME_COLUMN_ID).setValue(userGroup.getGroupName());
                getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), DESCRIPTION_COLUMN_ID).setValue(userGroup.getDescription());
            });
        } catch (UnauthorizedAccessAttemptException ex) {
            UIComponentTools.showWarning("You are not authorized to list all groups.");
            String object = "information about user groups";
            LOGGER.warn("Failed to reload '" + getUserGroupTable().getId() + "'. Cause: "
                    + MsgBuilder.unauthorizedGetRequest(object), ex);
        } finally {
            mdm.close();
        }
    }

    protected final UserGroupEffectivity validateSelectedUserGroup() {
        if (getSelectedUserGroup() == null) {
            return UserGroupEffectivity.NO;
        }
        //if user is administrator, continue
        if (UIHelper.getSessionUserRole().equals(Role.ADMINISTRATOR)) {
            return UserGroupEffectivity.VALID;
        } else {
            //otherwise, if user is at least manager in the selected group, continue
            String groupId = getSelectedUserGroup().getGroupId();
            if (UIHelper.getSessionContext(new GroupId(groupId)).getRoleRestriction().atLeast(Role.MANAGER)) {
                return UserGroupEffectivity.VALID;
            } else {
                //user is not at least manager
                return UserGroupEffectivity.INVALID;
            }
        }
    }

    protected final void updateTableEntry(UserGroup userGroup) {
        getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), NAME_COLUMN_ID).setValue(userGroup.getGroupName());
        getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), DESCRIPTION_COLUMN_ID).setValue(userGroup.getDescription());
    }

    protected final void addTableEntry(UserGroup userGroup) {
        getUserGroupTable().getContainerDataSource().addItem(userGroup.getId());
        getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), ID_COLUMN_ID).setValue(userGroup.getId());
        getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), GROUPID_COLUMN_ID).setValue(userGroup.getGroupId());
        getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), NAME_COLUMN_ID).setValue(userGroup.getGroupName());
        getUserGroupTable().getContainerDataSource().getContainerProperty(userGroup.getId(), DESCRIPTION_COLUMN_ID).setValue(userGroup.getDescription());
        getUserGroupTable().select(userGroup.getId());
    }

    protected UserGroup getSelectedUserGroup() {
        Long id = (Long) getUserGroupTable().getValue();
        if (id != null) {
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            mdm.setAuthorizationContext(UIHelper.getSessionContext());
            try {
                return mdm.find(UserGroup.class, id);
            } catch (UnauthorizedAccessAttemptException ex) {
                //not authorized
            } finally {
                mdm.close();
            }
        }
        return null;
    }

    protected void removeAllTableFilters() {
        // Remove all existing container filters
        IndexedContainer container = (IndexedContainer) getUserGroupTable().getContainerDataSource();
        container.removeAllContainerFilters();
        // Remove filter icon from corresponding table columns
        filters.keySet().forEach((property) -> {
            getUserGroupTable().setColumnIcon(property, null);
        });
        // Sort all table entries by first column
        getUserGroupTable().sort(new Object[]{getUserGroupTable().getVisibleColumns()[0]}, new boolean[]{true});
        // Select first table entry
        getUserGroupTable().select(getUserGroupTable().firstItemId());
    }

    protected void addTableFilter(String filterExpression, String columnId, UserGroupFilter.SearchSpace searchSpace) {
        // Add requested filter
        IndexedContainer container = (IndexedContainer) getUserGroupTable().getContainerDataSource();
        UserGroupFilter userGroupFilter = new UserGroupFilter(filterExpression, columnId, searchSpace);
        container.addContainerFilter(userGroupFilter);

        filters.put(columnId, new FilterProperties<>(filterExpression, searchSpace));
        // Set filter icon at corresponding table columns if missing
        ThemeResource filterAddIcon = new ThemeResource(IconContainer.FILTER_ADD);
        if (!filterAddIcon.equals(getUserGroupTable().getColumnIcon(columnId))) {
            // Set missing filterAddIcon
            getUserGroupTable().setColumnIcon(columnId, filterAddIcon);
        }
    }

    private void removeTableFilter(String columnId) {
        // Remove all existing container filters
        IndexedContainer container = (IndexedContainer) getUserGroupTable().getContainerDataSource();
        container.removeAllContainerFilters();
        // Add all filters except of the filter supposed to be deleted
        filters.remove(columnId);
        filters.keySet().forEach((iProperty) -> {
            String filterExpression = filters.get(iProperty).filterExpression;
            UserGroupFilter.SearchSpace searchSpace = filters.get(iProperty).searchSpace;
            addTableFilter(filterExpression, iProperty, searchSpace);
        });
        // Remove filter icon from corresponding table column
        getUserGroupTable().setColumnIcon(columnId, null);
        // Disable button-icon for filter removing in case all filters have been removed
        if (filters.isEmpty()) {
            userGroupAdministrationTab.getRemoveAllFiltersButton().setEnabled(false);
        }
        // Select first table entry
        getUserGroupTable().select(getUserGroupTable().firstItemId());
    }

}
