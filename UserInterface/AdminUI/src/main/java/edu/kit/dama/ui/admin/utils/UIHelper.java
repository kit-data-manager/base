/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.ui.admin.utils;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.util.AuthorizationUtil;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import java.net.URI;
import java.util.List;

/**
 *
 * @author jejkal
 */
public class UIHelper {

    public static URI getWebAppUrl() {
        String baseUrl = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_BASE_URL_ID, "http://localhost:8080");
        return URI.create(baseUrl + VaadinServlet.getCurrent().getServletContext().getContextPath());
    }

    public static void login(UserId user, GroupId group) {
        VaadinSession.getCurrent().setAttribute("userId", user.getStringRepresentation());
        changeSessionGroup(group);
    }

    public static void logout(String destination) {
        VaadinSession.getCurrent().close();
        Page.getCurrent().setLocation(destination);
    }

    public static void changeSessionGroup(GroupId group) {
        VaadinSession.getCurrent().setAttribute("groupId", group.getStringRepresentation());
    }

    public static UserData getSessionUser() {
        String userId = (String) VaadinSession.getCurrent().getAttribute("userId");
        UserData result = null;
        if (userId != null) {
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
            try {
                result = mdm.findSingleResult("SELECT u FROM UserData u WHERE u.distinguishedName=?1", new Object[]{userId}, UserData.class);
            } catch (UnauthorizedAccessAttemptException ex) {
                //no user found
            } finally {
                mdm.close();
            }
        }

        return (result == null) ? UserData.WORLD_USER : result;
    }

    public static GroupId getSessionGroupId() {
        String userId = (String) VaadinSession.getCurrent().getAttribute("userId");
        String groupId = (String) VaadinSession.getCurrent().getAttribute("groupId");
        //set userId to WORLD if no user is logged in
        userId = (userId != null) ? userId : Constants.WORLD_USER_ID;
        //set groupId to WORLD if no user is logged in, otherwise set it to USERS by default or to the actual value if one is set
        groupId = (groupId != null) ? groupId : (Constants.WORLD_USER_ID.equals(userId) ? Constants.WORLD_GROUP_ID : Constants.USERS_GROUP_ID);
        return new GroupId(groupId);
    }

    public static Role getSessionUserRole() {
        return getSessionContext().getRoleRestriction();

    }

    public static IAuthorizationContext getSessionContext() {
        return getSessionContext(getSessionGroupId());
    }

    public static IAuthorizationContext getSessionContext(GroupId groupId) {
        String userId = (String) VaadinSession.getCurrent().getAttribute("userId");
        //set userId to WORLD if no user is logged in
        userId = (userId != null) ? userId : Constants.WORLD_USER_ID;

        try {
            return AuthorizationUtil.getAuthorizationContext(new UserId(userId), groupId);
        } catch (AuthorizationException ex) {
            //failed, return world context
            return new AuthorizationContext(new UserId(Constants.WORLD_USER_ID), new GroupId(Constants.WORLD_GROUP_ID), Role.GUEST);
        }
    }

    /**
     * Check if the provided user is the last manager of the provided group.
     *
     * @param groupId The groupId.
     * @param userId The userId
     *
     * @return TRUE if userId is the last manager of groupId.
     */
    public static boolean isLastGroupManager(GroupId groupId, UserId userId) {
        try {
            List<UserId> groupManagers = GroupServiceLocal.getSingleton().getGroupManagers(groupId, 0, Integer.MAX_VALUE, UIHelper.getSessionContext(groupId));
            return groupManagers.stream().noneMatch((managerId) -> (!userId.equals(managerId)));
        } catch (AuthorizationException ex) {
            return false;
        }
    }
}
