/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.rest.usergroupmanagement.test;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.rest.admin.services.interfaces.IUserGroupService;
import edu.kit.dama.rest.admin.types.UserGroupWrapper;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.util.Constants;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

/**
 *
 * @author mf6319
 */
@Path("/UserGroupTest")
public class UserGroupTestService implements IUserGroupService {

  private static List<UserGroup> groups = factoryGroupEntities();
  private static List<UserData> users = factoryUserEntities();

  /**
   * Factory a list of user with ids 1-10.
   *
   * @return A list of users.
   */
  private static List<UserData> factoryUserEntities() {
    List<UserData> userList = new LinkedList<>();
    for (int i = 1; i < 11; i++) {
      userList.add(factoryUserEntity((long) i));
    }
    return userList;
  }

  /**
   * Factory a list of 10 groups with the ids 1-10.
   *
   * @return A list of groups.
   */
  private static List<UserGroup> factoryGroupEntities() {
    List<UserGroup> groupList = new LinkedList<>();
    for (int i = 1; i < 11; i++) {
      groupList.add(factoryGroupEntity((long) i));
    }
    return groupList;
  }

  private UserGroup findGroupById(final Long id) {
    return (UserGroup) CollectionUtils.find(groups, new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        return Objects.equals(((UserGroup) o).getId(), id);
      }
    });
  }

  private UserData findUserByUserId(final String userId) {
    return (UserData) CollectionUtils.find(users, new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        return Objects.equals(((UserData) o).getDistinguishedName(), userId);
      }
    });
  }

  private UserData findUserById(final Long id) {
    return (UserData) CollectionUtils.find(users, new Predicate() {

      @Override
      public boolean evaluate(Object o) {
        return Objects.equals(((UserData) o).getUserId(), id);
      }
    });
  }

  /**
   * Factory a single group with the provided id.
   *
   * @param groupId The group id.
   *
   * @return A single groups.
   */
  private static UserGroup factoryGroupEntity(long groupId) {
    UserGroup group = new UserGroup();
    if (groupId == 1) {
      group.setGroupId(Constants.USERS_GROUP_ID);
      group.setGroupName("Users");
      group.setDescription("Default user group");
      group.setId(groupId);
    } else {
      group.setGroupId("group" + groupId);
      group.setGroupName("Group" + groupId);
      group.setDescription("Group #" + groupId);
      group.setId(groupId);
    }
    return group;
  }

  /**
   * Factory a single group with the provided id.
   *
   * @param groupId The group id.
   *
   * @return A single groups.
   */
  private static UserGroup factoryGroupEntity(String groupId) {
    UserGroup group = new UserGroup();
    group.setGroupId(groupId);
    group.setGroupName("Custom group");
    group.setDescription("Custom group");
    group.setId(666l);
    return group;
  }

  /**
   * Factory a single user with the provided id.
   *
   * @param userId The user id.
   *
   * @return A single user.
   */
  private static UserData factoryUserEntity(long userId) {
    UserData user = new UserData();
    user.setUserId(userId);
    user.setFirstName("Dummy");
    user.setLastName("User");
    user.setEmail("dummy.user" + userId + "@dama.kit.edu");
    user.setDistinguishedName(new UserId(Long.toString(userId)).
            getStringRepresentation());
    return user;
  }

  @Override
  public StreamingOutput getGroupIds(Integer first, Integer results,
          HttpContext hc) {
    return createObjectGraphStream(UserGroupWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new UserGroupWrapper(groups));
  }

  @Override
  public StreamingOutput addGroup(String groupId, String groupName,
          String description, HttpContext hc) {
    UserGroup group = factoryGroupEntity(groupId);
    group.setId(Long.MIN_VALUE);
    group.setGroupName(groupName);
    group.setDescription(description);
    groups.add(group);
    return createObjectGraphStream(UserGroupWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new UserGroupWrapper(group));
  }

  @Override
  public StreamingOutput getGroupCount(HttpContext hc) {
    return createObjectGraphStream(UserGroupWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new UserGroupWrapper(groups.size()));
  }

  @Override
  public StreamingOutput getGroupDetails(final Long id, HttpContext hc) {
    UserGroup group = findGroupById(id);
    return createObjectGraphStream(UserGroupWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new UserGroupWrapper(
                    group));
  }

  @Override
  public StreamingOutput updateGroup(Long id, String groupName,
          String description, HttpContext hc) {
    UserGroup group = findGroupById(id);
    group.setGroupName(groupName);
    group.setDescription(description);
    return createObjectGraphStream(UserGroupWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH,
            new UserGroupWrapper(group));
  }

  @Override
  public Response deleteGroup(Long id, HttpContext hc) {
    UserGroup group = findGroupById(id);
    if (group == null) {
      return Response.status(404).build();
    } else {
      groups.remove(group);
      return Response.ok().build();
    }
  }

  @Override
  public StreamingOutput getGroupUsers(Long id, Integer first, Integer results,
          HttpContext hc) {
    UserGroup group = findGroupById(id);
    if (group == null) {
      //invalid group, no users
      return createObjectGraphStream(UserDataWrapper.class,
              Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(0));
    }
    return createObjectGraphStream(UserDataWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(
                    users));
  }

  @Override
  public StreamingOutput addGroupUser(Long id, String userId, HttpContext hc) {
    UserGroup group = findGroupById(id);
    UserData user = findUserByUserId(userId);
    if (group != null && user != null) {
      //group exists, return 1 modified row
      return createObjectGraphStream(UserGroupWrapper.class,
              Constants.REST_SIMPLE_OBJECT_GRAPH, new UserGroupWrapper(1));
    }
    //group does not exist, return 0 modified rows
    return createObjectGraphStream(UserGroupWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new UserGroupWrapper(0));
  }

  @Override
  public StreamingOutput removeGroupUser(Long id, Long userId, HttpContext hc) {
    UserGroup group = findGroupById(id);
    UserData user = findUserById(userId);

    if (group != null && user != null) {
      //does exist, return 1 modified row
      return createObjectGraphStream(UserGroupWrapper.class,
              Constants.REST_SIMPLE_OBJECT_GRAPH, new UserGroupWrapper(1));
    }
    //does not exist, return 0 modified rows
    return createObjectGraphStream(UserGroupWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new UserGroupWrapper(0));
  }

  @Override
  public StreamingOutput getUsersIds(String groupId, Integer first,
          Integer results, HttpContext hc) {
    return createObjectGraphStream(UserDataWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(
                    users));
  }

  @Override
  public StreamingOutput addUser(String groupId, String firstName,
          String lastName, String email, String distinguishedName, HttpContext hc) {
    UserData user = factoryUserEntity(11l);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setDistinguishedName(distinguishedName);
    users.add(user);
    return createObjectGraphStream(UserDataWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new UserDataWrapper(user));
  }

  @Override
  public StreamingOutput getUserCount(String groupId, HttpContext hc) {
    return createObjectGraphStream(UserDataWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new UserDataWrapper(users.size()));
  }

  @Override
  public StreamingOutput getUserDetails(Long userId, HttpContext hc) {
    UserData user = findUserById(userId);
    return createObjectGraphStream(UserDataWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new UserDataWrapper(user));
  }

  @Override
  public StreamingOutput updateUser(String groupId, Long userId, String firstName,
          String lastName, String email, HttpContext hc) {
    UserData user = findUserById(userId);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    return createObjectGraphStream(UserDataWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new UserDataWrapper(user));
  }

  @Override
  public Response deleteUser(Long userId, HttpContext hc) {
    //return "Not Implemented"
    return Response.status(501).build();
  }

  @Override
  public Response checkService() {
    return Response.status(200).entity(new CheckServiceResponse("UserGroupManagementTest", ServiceStatus.OK)).build();
  }

}
