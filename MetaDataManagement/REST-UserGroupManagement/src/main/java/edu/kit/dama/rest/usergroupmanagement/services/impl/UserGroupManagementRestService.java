/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.util.RestUtils;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.util.Constants;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
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
  public StreamingOutput getGroupIds(Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try getting group ids ({}-{}).", first, first + results);
      List<UserGroup> groups = mdm.findResultList("SELECT x FROM UserGroup x", first, results);
      return createObjectGraphStream(UserGroupWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserGroupWrapper(groups));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain group ids.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addGroup(String groupId, String groupName, String description, HttpContext hc) {
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
      return createObjectGraphStream(UserGroupWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new UserGroupWrapper(result));
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
  public StreamingOutput getGroupCount(HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try getting group count.");
      Long lCount = mdm.findSingleResult("SELECT COUNT(x) FROM UserGroup x", Long.class);
      return createObjectGraphStream(UserGroupWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserGroupWrapper((lCount != null) ? lCount.intValue() : 0));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get group count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getGroupDetails(Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      UserGroup group = mdm.findSingleResult("SELECT x FROM UserGroup x WHERE x.id=" + id, UserGroup.class);
      return createObjectGraphStream(UserGroupWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new UserGroupWrapper(group));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get group details.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput updateGroup(Long id, String groupName, String description, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try updating group with id {}", id);
      UserGroup group = mdm.findSingleResult("SELECT x FROM UserGroup x WHERE x.id=" + id, UserGroup.class);
      if (group == null) {
        LOGGER.warn("No group for id {} found. Too fast access to created group?", id);
        throw new WebApplicationException(new Exception("Group with id " + id + " not found. Please try again later."), 404);
      }
      group.setGroupName(groupName);
      group.setDescription(description);
      group = mdm.save(group);
      return createObjectGraphStream(UserGroupWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new UserGroupWrapper(group));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to create group.", ex);
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
  public StreamingOutput getGroupUsers(Long id, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      //Build a query which joins KIT DM and AAI User/Group entities. 
      List<UserData> users = mdm.findResultList("SELECT damau FROM "
              + "UserData damau, UserGroup damag, Groups aaig, Users aaiu, Memberships aaim WHERE "
              + "damag.id=" + id + " AND "
              + "aaiu.userId=dama.distinguishedName AND "
              + "aaim.user.id=aaiu.id AND "
              + "aaim.group.id=aaig.id AND "
              + "aaig.groupId=damag.groupId", UserData.class, first, results);
      return createObjectGraphStream(UserDataWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(users));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain group users", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addGroupUser(Long id, String userId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    LOGGER.debug("Try adding user with id '{}' to group with id '{}' ", userId, id);
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Obtaining group for id {}", id);
      UserGroup group = mdm.find(UserGroup.class, id);
      LOGGER.debug("Switching security context to group '{}'", group.getGroupId());
      IRoleRestriction maxRole = GroupServiceLocal.getSingleton().getMaximumRole(new GroupId(group.getGroupId()), ctx.getUserId(), ctx);
      ctx = new AuthorizationContext(ctx.getUserId(), new GroupId(group.getGroupId()), (Role) maxRole);
      LOGGER.debug("Adding user with id " + userId + " to group with id " + id);
      GroupServiceLocal.getSingleton().addUser(new GroupId(group.getGroupId()), new UserId(userId), Role.MEMBER, ctx);
      LOGGER.debug("Returning result");
      return createObjectGraphStream(UserGroupWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(1));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to create group. Probably, the caller context (" + ctx + ") is not authorized to modidy the group with id " + id + ".", ex);
      throw new WebApplicationException(401);
    } catch (EntityNotFoundException ex) {
      LOGGER.error("Failed to add user to group. Probably, the designated group manager (the caller) with id " + ctx.getUserId() + " was not found or is not manager of group with id " + id, ex);
      throw new WebApplicationException(404);
    } catch (EntityAlreadyExistsException ex) {
      LOGGER.warn("Failed add user " + userId + " to group with id " + id + ". Obviously, the user is already member of the group.", ex);
      return createObjectGraphStream(UserGroupWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(0));
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput removeGroupUser(Long id, Long userId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to remove user with id {} from group with id {}", userId, id);
      UserGroup group = mdm.findSingleResult("SELECT x FROM UserGroup x WHERE x.id=" + id, UserGroup.class);
      UserData user = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.userId=" + userId, UserData.class);
      GroupServiceLocal.getSingleton().removeUser(new GroupId(group.getGroupId()), new UserId(user.getDistinguishedName()), ctx);
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to remove user from group.", ex);
      throw new WebApplicationException(401);
    } catch (EntityNotFoundException ex) {
      return createObjectGraphStream(UserGroupWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(0));
    } finally {
      mdm.close();
    }

    return createObjectGraphStream(UserGroupWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(1));
  }

  @Override
  public StreamingOutput getUsersIds(String groupId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to get user ids ({}-{}) for group {}", first, first + results, groupId);
      List<UserData> users = mdm.findResultList("SELECT x FROM UserData x", first, results);
      return createObjectGraphStream(UserDataWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(users));
    } catch (UnauthorizedAccessAttemptException ex) {
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addUser(String groupId, String firstName, String lastName, String email, String distinguishedName, HttpContext hc) {
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
      return createObjectGraphStream(UserDataWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new UserDataWrapper(user));
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
  public StreamingOutput getUserCount(String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Getting user count of group {}", groupId);
      Long lCount = mdm.findSingleResult("SELECT COUNT(damau) FROM "
              + "UserData damau, UserGroup damag, Groups aaig, Users aaiu, Memberships aaim WHERE "
              + "damag.groupId='" + groupId + "' AND "
              + "aaiu.userId=damau.distinguishedName AND "
              + "aaim.user.id=aaiu.id AND "
              + "aaim.group.id=aaig.id AND "
              + "aaig.groupId=damag.groupId", Long.class);

      return createObjectGraphStream(UserDataWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper((lCount != null) ? lCount.intValue() : 0));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get user count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getUserDetails(Long userId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      UserData user;
      LOGGER.debug("Getting user details for user with id {} (0 = caller)", userId);
      if (userId <= 0) {
        user = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.distinguishedName LIKE '" + ctx.getUserId().getStringRepresentation() + "'", UserData.class);
      } else {
        user = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.userId=" + userId, UserData.class);
      }
      return createObjectGraphStream(UserDataWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new UserDataWrapper(user));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get user details.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput updateUser(String groupId, Long userId, String firstName, String lastName, String email, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to update user with id {}", userId);
      UserData user = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.userId=" + userId, UserData.class);
      if (user == null) {
        throw new WebApplicationException(404);
      }

      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setEmail(email);
      user = mdm.save(user);
      return createObjectGraphStream(UserDataWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new UserDataWrapper(user));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to update user.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public Response deleteUser(Long userId, HttpContext hc) {
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
