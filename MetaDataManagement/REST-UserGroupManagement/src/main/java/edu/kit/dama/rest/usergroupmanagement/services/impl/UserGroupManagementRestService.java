/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.rest.usergroupmanagement.services.impl;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.entities.impl.Group;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.rest.admin.services.interfaces.IUserGroupService;
import edu.kit.dama.rest.admin.types.UserGroupWrapper;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.admin.interfaces.IDefaultUserGroup;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.base.interfaces.IDefaultUserData;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.util.RestUtils;
import edu.kit.dama.util.Constants;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
@Path("/")
public final class UserGroupManagementRestService implements IUserGroupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserGroupManagementRestService.class);

    @Override
    public IEntityWrapper<? extends IDefaultUserGroup> getGroups(Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try getting group ids ({}-{}).", first, first + results);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserGroup.default");
            return new UserGroupWrapper(mdm.findResultList("SELECT x FROM UserGroup x", UserGroup.class, first, results));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain group ids.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserGroup> addGroup(String groupId, String groupName, String description, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try adding group {} with name '{}' and description '{}'", groupId, groupName, description);
            LOGGER.debug(" - Registering groupId to authorization module");
            GroupServiceLocal.getSingleton().create(new GroupId(groupId), ctx.getUserId(), ctx);
            UserGroup template = new UserGroup();
            template.setGroupId(groupId);
            template.setGroupName(groupName);
            template.setDescription(description);
            LOGGER.debug(" - Persisting group entity via metadata management");
            UserGroup result = mdm.save(template);
            LOGGER.debug("Returning result via REST interface.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserGroup.default");
            return new UserGroupWrapper(mdm.find(UserGroup.class, result.getId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to create group. Probably, the caller context (" + ctx + ") is not authorized to create groups.", ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to register group to authorization module. Probably, the designated group manager (the caller) with id " + ctx.getUserId() + " was not found", ex);
            throw new WebApplicationException(404);
        } catch (EntityAlreadyExistsException ex) {
            LOGGER.error("Failed to register group to authorization module. A group with id " + groupId + " already exists.", ex);
            throw new WebApplicationException(409);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserGroup> getGroupCount(HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try getting group count.");
            return new UserGroupWrapper(((Number) mdm.findSingleResult("SELECT COUNT(x) FROM UserGroup x")).intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get group count.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserGroup> getGroupDetails(Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserGroup.default");
            UserGroup group = mdm.findSingleResult("SELECT x FROM UserGroup x WHERE x.id=" + id, UserGroup.class);

            if (group == null) {
                LOGGER.error("No group found for groupId {}", id);
                throw new WebApplicationException(404);
            }
            return new UserGroupWrapper(group);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get group details.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserGroup> updateGroup(Long id, String groupName, String description, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            if (ctx.getRoleRestriction().lessThan(Role.MANAGER)) {
                throw new UnauthorizedAccessAttemptException("Role " + ctx.getRoleRestriction() + " < MANAGER");
            }
            LOGGER.debug("Try updating group with id {}", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserGroup.default");
            UserGroup group = mdm.findSingleResult("SELECT x FROM UserGroup x WHERE x.id=" + id, UserGroup.class);
            if (group == null) {
                LOGGER.warn("No group for id {} found. Too fast access to created group?", id);
                throw new WebApplicationException(new Exception("Group with id " + id + " not found. Please try again later."), 404);
            }
            group.setGroupName(groupName);
            group.setDescription(description);
            group = mdm.save(group);
            return new UserGroupWrapper(group);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to update group.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public Response deleteGroup(Long id, HttpContext hc) {
        LOGGER.warn("deleteGroup() is currently not supported.");
        throw new WebApplicationException(405);
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> getGroupUsers(Long groupId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            if (ctx.getRoleRestriction().lessThan(Role.MEMBER)) {
                throw new UnauthorizedAccessAttemptException("Role " + ctx.getRoleRestriction() + " < MEMBER");
            }
            LOGGER.debug("Obtaining users of group {}", groupId);
            //Build a query which joins KIT DM and AAI User/Group entities. 
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");
            List<UserData> users = mdm.findResultList("SELECT damau FROM "
                    + "UserData damau, UserGroup damag, Groups aaig, Users aaiu, Memberships aaim WHERE "
                    + "damag.id=" + groupId + " AND "
                    + "aaiu.userId=damau.distinguishedName AND "
                    + "aaim.user.id=aaiu.id AND "
                    + "aaim.group.id=aaig.id AND "
                    + "aaig.groupId=damag.groupId", UserData.class, first, results);
            LOGGER.debug("Obtained {} user entries", users.size());
            return new UserDataWrapper(users);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to list group users.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserGroup> addGroupUser(Long id, String userId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        LOGGER.debug("Try adding user with id '{}' to group with id '{}' ", userId, id);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Checking whether userId '{}' is a distinguished name or a primary key of a UserData entity.", userId);
            try {
                long userDataId = Long.parseLong(userId);
                LOGGER.debug("Value is parsable to Long. Trying to find UserData for id {}", userDataId);
                UserData user = mdm.find(UserData.class, userDataId);
                if (user != null) {
                    LOGGER.debug("UserData found. Using distinguished name '{}'", user.getDistinguishedName());
                    userId = user.getDistinguishedName();
                }
            } catch (NumberFormatException ex) {
                //seems not to be the long id of a UserData entity
                LOGGER.debug("Value is not parsable to Long. Assuming userId '{}' to be a distinguished name.", userId);
            }
            LOGGER.debug("Obtaining group for id '{}'", id);
            UserGroup group = mdm.find(UserGroup.class, id);
            LOGGER.debug("Switching security context to group '{}'", group.getGroupId());
            IRoleRestriction maxRole = GroupServiceLocal.getSingleton().getMaximumRole(new GroupId(group.getGroupId()), ctx.getUserId(), ctx);
            ctx = new AuthorizationContext(ctx.getUserId(), new GroupId(group.getGroupId()), (Role) maxRole);
            LOGGER.debug("Adding user with id " + userId + " to group with id " + id);
            GroupServiceLocal.getSingleton().addUser(new GroupId(group.getGroupId()), new UserId(userId), Role.MEMBER, ctx);
            LOGGER.debug("Returning result");
            return new UserGroupWrapper(1);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to create group. Probably, the caller context (" + ctx + ") is not authorized to modidy the group with id " + id + ".", ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to add user to group. Probably, the designated group manager (the caller) with id " + ctx.getUserId() + " was not found or is not manager of group with id " + id, ex);
            throw new WebApplicationException(404);
        } catch (EntityAlreadyExistsException ex) {
            LOGGER.warn("Failed add user " + userId + " to group with id " + id + ". Obviously, the user is already member of the group.", ex);
            return new UserGroupWrapper(0);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserGroup> removeGroupUser(Long id, Long userId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to remove user with id {} from group with id {}", userId, id);
            UserGroup group = mdm.find(UserGroup.class, id);
            UserData user = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.userId=" + userId, UserData.class);
            LOGGER.debug("Switching security context to group '{}'", group.getGroupId());
            IRoleRestriction maxRole = GroupServiceLocal.getSingleton().getMaximumRole(new GroupId(group.getGroupId()), ctx.getUserId(), ctx);
            ctx = new AuthorizationContext(ctx.getUserId(), new GroupId(group.getGroupId()), (Role) maxRole);
            LOGGER.debug("Try to remove user {} from group {}", user, group);
            GroupServiceLocal.getSingleton().removeUser(new GroupId(group.getGroupId()), new UserId(user.getDistinguishedName()), ctx);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to remove user from group.", ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            return new UserGroupWrapper(0);
        } finally {
            mdm.close();
        }

        return new UserGroupWrapper(1);
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> getUsers(String groupId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            if (ctx.getRoleRestriction().lessThan(Role.MANAGER)) {
                throw new UnauthorizedAccessAttemptException("Role " + ctx.getRoleRestriction() + " < MANAGER");
            }

            LOGGER.debug("Try to get users ({}-{})", first, first + results);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");
            return new UserDataWrapper(mdm.findResultList("SELECT x FROM UserData x", UserData.class, first, results));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to obtain all users.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> addUser(String groupId, String firstName, String lastName, String email, String distinguishedName, HttpContext hc) {
        LOGGER.debug("Try adding user with distinguished name {}", distinguishedName);
        UserData user = new UserData();
        user.setDistinguishedName(distinguishedName);
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Checking for existing user with distinguished name {}", distinguishedName);
            List<UserData> result = mdm.find(user, user);

            if (!result.isEmpty()) {
                throw new EntityAlreadyExistsException("UserData entity with distinguished name " + distinguishedName + " found.");
            }
            LOGGER.debug("Registering UserId {}", distinguishedName);
            UserServiceLocal.getSingleton().register(new UserId(distinguishedName), Role.MEMBER, AuthorizationContext.factorySystemContext());
            LOGGER.debug("Adding user to default group {}", Constants.USERS_GROUP_ID);
            GroupServiceLocal.getSingleton().addUser(new GroupId(Constants.USERS_GROUP_ID), new UserId(distinguishedName), Role.GUEST, AuthorizationContext.factorySystemContext());
            LOGGER.debug("Saving UserData");
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user = mdm.save(user);
            LOGGER.debug("User successfully created.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");
            return new UserDataWrapper(mdm.find(UserData.class, user.getUserId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add user.", ex);
            throw new WebApplicationException(401);
        } catch (EntityAlreadyExistsException ex) {
            LOGGER.error("Adding user failed. User with distinguished name " + distinguishedName + " already exists.", ex);
            throw new WebApplicationException(409);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to add user with distinguished name " + distinguishedName + " to default group " + Constants.USERS_GROUP_ID, ex);
            throw new WebApplicationException(404);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> getUserCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting user count of group {}", groupId);
            int iCount = ((Number) mdm.findSingleResult("SELECT COUNT(damau) FROM "
                    + "UserData damau, UserGroup damag, Groups aaig, Users aaiu, Memberships aaim WHERE "
                    + "damag.groupId='" + groupId + "' AND "
                    + "aaiu.userId=damau.distinguishedName AND "
                    + "aaim.user.id=aaiu.id AND "
                    + "aaim.group.id=aaig.id AND "
                    + "aaig.groupId=damag.groupId")).intValue();

            return new UserDataWrapper(iCount);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get user count.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> getUserDetails(Long userId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            if (userId > 0 && ctx.getRoleRestriction().lessThan(Role.MEMBER)) {
                throw new UnauthorizedAccessAttemptException("Role " + ctx.getRoleRestriction() + " < MEMBER");
            }
            UserData user;
            LOGGER.debug("Getting user details for user with id {} (0 = caller)", userId);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");
            if (userId <= 0) {
                user = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.distinguishedName LIKE '" + ctx.getUserId().getStringRepresentation() + "'", UserData.class);
            } else {
                user = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.userId=" + userId, UserData.class);
            }
            return new UserDataWrapper(user);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to obtain user details.", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> updateUser(String groupId, Long userId, String firstName, String lastName, String email, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to update user with id {}", userId);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");

            UserData user = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.userId=" + userId, UserData.class);
            if (user == null) {
                throw new WebApplicationException(404);
            }

            if (!ctx.getUserId().getStringRepresentation().equals(user.getDistinguishedName()) && ctx.getRoleRestriction().lessThan(Role.MANAGER)) {
                //no self update
                throw new UnauthorizedAccessAttemptException("Role " + ctx.getRoleRestriction() + " < MANAGER");
            }

            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user = mdm.save(user);
            return new UserDataWrapper(user);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to update user with id " + userId, ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public Response deleteUser(String groupId, Long userId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to delete user with id {}", userId);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");

            UserData user = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.userId=" + userId, UserData.class);
            if (user == null) {
                throw new WebApplicationException(404);
            }

            if (ctx.getRoleRestriction().lessThan(Role.ADMINISTRATOR)) {
                //no self update
                throw new UnauthorizedAccessAttemptException("Role " + ctx.getRoleRestriction() + " < ADMINISTRATOR");
            }

            UserServiceLocal.getSingleton().setRoleRestriction(new UserId(user.getDistinguishedName()), Role.NO_ACCESS, ctx);
            return Response.ok().build();
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Context " + ctx + " is not authorized to deactivate (delete) user with id " + userId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {

        } finally {
            mdm.close();
        }

        LOGGER.warn("Deleting a user is currently not supported.");
        throw new WebApplicationException(405);
    }

    @Override
    public Response checkService() {
        ServiceStatus status;
        try {
            LOGGER.debug("Doing service check by getting USERS group");
            Group users = FindUtil.findGroup(PU.entityManager(), new GroupId(Constants.USERS_GROUP_ID));

            LOGGER.debug("Service check using USERS group returned {}.", users);
            status = ServiceStatus.OK;
        } catch (Throwable t) {
            LOGGER.error("Obtaining USERS group returned an error. Service status is set to ERROR", t);
            status = ServiceStatus.ERROR;
        }
        return Response.status(200).entity(new CheckServiceResponse("UserGroupManagement", status)).build();
    }
}
