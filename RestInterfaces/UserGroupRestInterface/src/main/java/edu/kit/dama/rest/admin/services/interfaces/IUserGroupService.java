/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.rest.admin.services.interfaces;

import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.util.Constants;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author mf6319
 */
public interface IUserGroupService extends ICommonRestInterface {

  /**
   * Get a list of all group ids. Without any parameter this method will return
   * the first 10 group ids.
   *
   * @summary Get a list of all group ids.
   *
   * @param first The first index.
   * @param results The max. number of results.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A list of UserGroup entities serialized using the
   * <b>simple</b> object graph of UserGroupWrapper, which removes all attributes
   * but the id from the returned entities.
   *
   * @see edu.kit.dama.rest.admin.types.UserGroupWrapper
   */
  @GET
  @Path(value = "/groups/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserGroupWrapper")
  StreamingOutput getGroupIds(
          @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
          @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Add a new group with the provided groupId, name and description. The
   * description is optional. The provided groupId could be an arbitrary string
   * from an arbitrary source, but it has to be unique. The groupId <b>USERS</b>
   * is used by KIT Data Manager itself.
   *
   * @summary Add a new group.
   *
   * @param groupId The id of the created group. This id must be unique. If the
   * provided id already exists, an error occurs.
   * @param groupName The name of the group to create.
   * @param description A human readable description of the group to create.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The created UserGroup serialized using the
   * <b>default</b> object graph of UserGroupWrapper, which contains all
   * attributes.
   *
   * @see edu.kit.dama.rest.admin.types.UserGroupWrapper
   */
  @POST
  @Path(value = "/groups/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserGroupWrapper")
  StreamingOutput addGroup(
          @FormParam("groupId") String groupId,
          @FormParam("groupName") String groupName,
          @FormParam("description") String description,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Return the number of groups.
   *
   * @summary Get the number of groups.
   *
   * @param hc The HttpContext for OAuth check.
   *
   * @return The number of all groups wrapped by a UserGroupWrapper entity.
   *
   * @see edu.kit.dama.rest.admin.types.UserGroupWrapper
   */
  @GET
  @Path(value = "/groups/count")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserGroupWrapper")
  StreamingOutput getGroupCount(
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Get details of the group with the provided id.
   *
   * @summary Get details of the group with the provided id.
   *
   * @param id The group id.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The UserGroup for the provided id serialized using the
   * <b>default</b> object graph of UserGroupWrapper, which contains all
   * attributes including the generated id and groupId.
   *
   * @see edu.kit.dama.rest.admin.types.UserGroupWrapper
   */
  @GET
  @Path(value = "/groups/{id}")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserGroupWrapper")
  StreamingOutput getGroupDetails(
          @PathParam("id") Long id,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Update name and/or description of the group with the provided id. The
   * update will only work if the calling user is member of the group and/or has
   * appropriate permissions.
   *
   * @summary Update the group with the provided id.
   *
   * @param id The id of the group to update.
   * @param groupName The updated group name.
   * @param description The updated group description.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The updated UserGroup for the provided id serialized using the
   * <b>default</b> object graph of UserGroupWrapper, which contains all
   * attributes including the generated id and groupId.
   *
   * @see edu.kit.dama.rest.admin.types.UserGroupWrapper
   */
  @PUT
  @Path(value = "/groups/{id}")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserGroupWrapper")
  StreamingOutput updateGroup(
          @PathParam("id") Long id,
          @FormParam("groupName") String groupName,
          @FormParam("description") String description,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Delete the group with the provided id. Deletion will only work if the
   * caller has according permissions.
   *
   * @summary Delete the group with the provided id.
   *
   * @param hc The HttpContext for OAuth check.
   * @param id The id of the group to delete.
   *
   * @return HTTP response with an according HTTP status code.
   */
  @DELETE
  @Path(value = "/groups/{id}")
  @Produces("application/xml")
  @ReturnType("java.lang.Void")
  Response deleteGroup(
          @PathParam("id") Long id,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Get a list of users in the group with the provided id. By default, the
   * first 10 users of the group are returned. If this call succeeds depends on
   * the implementation. Normally, the calling user should be member of the
   * group and/or have appropriate permissions.
   *
   * @summary Get the list of users in the group with the provided id.
   *
   * @param id The group id.
   * @param first The first index to return.
   * @param results The max. number of results.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A list of users in the group with the provided id. The returned
   * users are serialized using the <b>simple</b> object graph of
   * UserDataWrapper, which contains no attributes but the user id.
   *
   * @ReturnType("java.util.List<edu.kit.dama.mdm.admin.UserData>")
   *
   * @see edu.kit.dama.rest.admin.types.UserDataWrapper
   */
  @GET
  @Path(value = "/groups/{id}/users")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserDataWrapper")
  StreamingOutput getGroupUsers(
          @PathParam("id") Long id,
          @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
          @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Add a new user to the group with the provided id. This call only succeeds
   * if the caller is manager of the group and/or has appropriate permissions.
   *
   * @summary Add a new user to the group with the provided id.
   *
   * @param id The group id.
   * @param userId The userId of the user that should be added to the group.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A UserGroupWrapper entity containing the number of modified rows in
   * the count field. If the value is 1, the operation was successfull.
   * Otherwise, the operation failed.
   *
   * @see edu.kit.dama.rest.admin.types.UserGroupWrapper
   */
  @POST
  @Path(value = "/groups/{id}/users")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserGroupWrapper")
  StreamingOutput addGroupUser(
          @PathParam("id") Long id,
          @FormParam("userId") String userId,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Remove the user with the provided user id from the group with the provided
   * group id. This call only succeeds if the caller is manager of the group
   * and/or has appropriate permissions.
   *
   * @summary Remove a user from a group.
   *
   * @param id The group id.
   * @param userId The userId of the user that should be removed from the group.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A UserGroupWrapper entity containing the number of modified rows in
   * the count field. If the value is 1, the operation was successful.
   * Otherwise, the operation failed.
   *
   * @see edu.kit.dama.rest.admin.types.UserGroupWrapper
   */
  @DELETE
  @Path(value = "/groups/{id}/users/{userId}")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserGroupWrapper")
  StreamingOutput removeGroupUser(
          @PathParam("id") Long id,
          @PathParam("userId") Long userId,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Get a list of all users. The provided group id is used to setup the context
   * which is used to performed the access. Pay attention that the group id is
   * not a numeric value but the internal string identifier stored in the
   * attribute <i>groupId</i> of UserGroup. Without any parameter this method
   * will return the first 10 users.
   *
   * @summary Get all users.
   *
   * @param groupId The group id used to perform the request.
   * @param first The first index.
   * @param results The max. number of results.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A list of UserData entities serialized using the
   * <b>simple</b> object graph of UserDataWrapper, which removes all attributes
   * but the id from the returned entities.
   *
   * @see edu.kit.dama.rest.admin.types.UserDataWrapper
   */
  @GET
  @Path(value = "/users/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserDataWrapper")
  StreamingOutput getUsersIds(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
          @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Add a new user with the provided attributes. The userId of the user has to
   * be generated automatically depending on the implementation. The generated
   * if will then be part of the returned user. All other fields may or may not
   * be provided. In addition to creating the user other actions might be
   * performed, e.g. adding the user to the default group
   * Constants.USERS_GROUP_ID.
   *
   * @summary Add a new user.
   *
   * @param groupId The group id used to perform the request.
   * @param firstName The first name of the user to create.
   * @param lastName The last name of the user to create.
   * @param email The email of the user to create.
   * @param distinguishedName The distinguished name, which is a unique
   * identifier for the user. Typically, this identifier comes from an external
   * source, e.g. an LDAP directory.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The created UserData serialized using the
   * <b>default</b> object graph of UserDataWrapper, which contains all
   * attributes including the generated userId.
   *
   * @see edu.kit.dama.rest.admin.types.UserDataWrapper
   */
  @POST
  @Path(value = "/users/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserDataWrapper")
  StreamingOutput addUser(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @FormParam("firstName") String firstName,
          @FormParam("lastName") String lastName,
          @FormParam("email") String email,
          @FormParam("distinguishedName") String distinguishedName,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Return the number of users in the group with the provided id. If no groupId
   * is provided, the user count in the default group USERS is returned.
   *
   * @summary Get the number of usersin the group with the provided id.
   *
   * @param groupId The group where to count the users.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The number of all users in the group with the provided id wrapped
   * by a UserDataWrapper entity.
   *
   * @see edu.kit.dama.rest.admin.types.UserDataWrapper
   */
  @GET
  @Path(value = "/users/count")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserDataWrapper")
  StreamingOutput getUserCount(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Get details of the user with the provided userId.
   *
   * @summary Get details of the user with the provided userId.
   *
   * @param userId The id of the user. If the provided user id is less or equal
   * 0, the information about the caller are returned.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The UserData for the provided id serialized using the
   * <b>default</b> object graph of UserDataWrapper, which contains all
   * attributes.
   *
   * @see edu.kit.dama.rest.admin.types.UserDataWrapper
   */
  @GET
  @Path(value = "/users/{userId}")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserDataWrapper")
  StreamingOutput getUserDetails(
          @PathParam("userId") Long userId,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Update first/last name and/or email of the user with the provided id. The
   * update is only performed if the caller is the user with the provided user
   * id or has an according role within the group with the id groupId.
   *
   * @summary Update the user with the provided id.
   *
   * @param groupId The group id of the callers context.
   * @param userId The id of the user to update.
   * @param firstName The first name of the user.
   * @param lastName The last name of the user.
   * @param email The email of the user.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The updated UserData for the provided id serialized using the
   * <b>default</b> object graph of UserDataWrapper, which contains all
   * attributes.
   *
   * @see edu.kit.dama.rest.admin.types.UserDataWrapper
   */
  @PUT
  @Path(value = "/users/{userId}")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.admin.types.UserDataWrapper")
  StreamingOutput updateUser(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("userId") Long userId,
          @FormParam("firstName") String firstName,
          @FormParam("lastName") String lastName,
          @FormParam("email") String email,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Delete the user with the provided userId.<br/><br/>
   * <b>By default this method should implemented to revoke all permissions
   * instead of deleting the user.</b>
   *
   * @summary Delete the user with the provided id.
   *
   * @param userId The id of the user to delete.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The response with an according HTTP response code.
   */
  @DELETE
  @Path(value = "/users/{userId}")
  @Produces("application/xml")
  @ReturnType("java.lang.Void")
  Response deleteUser(
          @PathParam("userId") Long userId,
          @javax.ws.rs.core.Context HttpContext hc);
}
