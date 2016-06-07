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
package edu.kit.dama.rest.admin.client.impl;

import com.sun.jersey.api.client.ClientResponse;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.rest.admin.types.UserGroupWrapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.util.Constants;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Rest client for UserGroupManagement.
 *
 * @author hartmann-v
 */
public final class UserGroupRestClient extends AbstractRestClient {

    /**
     * The logger
     */
    //private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(UserGroupRestClient.class);
    //<editor-fold defaultstate="collapsed" desc="Parameter names">
    /**
     * Name of the group.
     */
    protected static final String FORM_PARAMETER_GROUP_NAME = "groupName";
    /**
     * Id of the user.
     */
    protected static final String FORM_PARAMETER_USER_ID = "userId";
    /**
     * First name of the user.
     */
    protected static final String FORM_PARAMETER_USER_FIRST_NAME = "firstName";
    /**
     * Last name of the user.
     */
    protected static final String FORM_PARAMETER_USER_LAST_NAME = "lastName";
    /**
     * mail of the user.
     */
    protected static final String FORM_PARAMETER_USER_EMAIL = "email";

    /**
     * Distinguished name of the user.
     */
    protected static final String FORM_PARAMETER_DISTINGUISHED_NAME = "distinguishedName";

//</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="URL components">
    /**
     * 'url' for count.
     */
    private static final String COUNT_URL = "/count";
    /**
     * List all groups.
     */
    private static final String GROUPS_URL = "/groups";
    /**
     * Get group with given id.
     */
    private static final String GROUP_BY_ID = GROUPS_URL + "/{0}";
    /**
     * List all users of selected group.
     */
    private static final String USERS_OF_GROUP = GROUP_BY_ID + "/users";
    /**
     * Get user of selected group with given id.
     */
    private static final String USER_OF_GROUP = USERS_OF_GROUP + "/{1}";
    /**
     * Get no of groups.
     */
    private static final String GROUP_COUNT = GROUPS_URL + COUNT_URL;
    /**
     * List all users.
     */
    private static final String USERS_URL = "/users";
    /**
     * Get user with given id.
     */
    private static final String USER_BY_ID = USERS_URL + "/{0}";
    /**
     * Get no of users.
     */
    private static final String USER_COUNT = USERS_URL + COUNT_URL;
// </editor-fold>

    /**
     * Create a REST client with a predefined context.
     *
     * @param rootUrl root url of the staging service. (e.g.:
     * "http://dama.lsdf.kit.edu/KITDM/rest/UserGroupService")
     * @param pContext initial context
     */
    public UserGroupRestClient(String rootUrl, SimpleRESTContext pContext) {
        super(rootUrl, pContext);
    }
    // <editor-fold defaultstate="collapsed" desc="Generic Rest methods (GET, PUT, POST, DELETE)">

    /**
     * Perform a get for users.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return UserWrapper.
     */
    private UserDataWrapper performUserGet(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(UserDataWrapper.class, getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a put for users.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return UserWrapper.
     */
    private UserDataWrapper performUserPut(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPut(UserDataWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a post for users.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return UserWrapper.
     */
    private UserDataWrapper performUserPost(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(UserDataWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a delete for users.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return true or false.
     */
    private boolean performUserDelete(String pPath, MultivaluedMap pQueryParams) {
        return ((ClientResponse) RestClientUtils.performDelete(null, getWebResource(pPath), pQueryParams)).getStatus() == Response.Status.OK.getStatusCode();
    }

    /**
     * Perform a get for groups.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return GroupWrapper.
     */
    private UserGroupWrapper performGroupGet(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(UserGroupWrapper.class, getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a put for groups.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return GroupWrapper.
     */
    private UserGroupWrapper performGroupPut(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPut(UserGroupWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a post for groups.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     * @return GroupWrapper.
     */
    private UserGroupWrapper performGroupPost(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(UserGroupWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a delete for groups.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return GroupWrapper.
     */
    private UserGroupWrapper performGroupDelete(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performDelete(UserGroupWrapper.class, getWebResource(pPath), pQueryParams);
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="get[AllGroups|AllUsers|UsersOfGroup]">
    /**
     * Get all groups.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     *
     * @return GroupWrapper
     */
    public UserGroupWrapper getAllGroups(int pFirstIndex, Integer pResults) {
        return getAllGroups(pFirstIndex, pResults, null);
    }

    /**
     * Get all groups.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @return GroupWrapper
     */
    public UserGroupWrapper getAllGroups(int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        UserGroupWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(pResults));
        returnValue = performGroupGet(GROUPS_URL, queryParams);
        return returnValue;
    }

    /**
     * Get all users.
     *
     * Attention: pGroupId is not used to filter the result by group, it is only
     * used to authorize the access.
     *
     * @param pGroupId The group id used to authorize the query.
     * @param pFirstIndex index of first result.
     * @param pResults no of results.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper getAllUsers(String pGroupId, int pFirstIndex, int pResults) {
        return getAllUsers(pGroupId, pFirstIndex, pResults, null);
    }

    /**
     * Get all users.
     *
     * Attention: pGroupId is not used to filter the result by group, it is only
     * used to authorize the access.
     *
     * @param pGroupId The group id used to authorize the query.
     * @param pFirstIndex index of first result.
     * @param pResults no of results.
     * @param pSecurityContext security context.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper getAllUsers(String pGroupId, int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        UserDataWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(pResults));
        returnValue = performUserGet(USERS_URL, queryParams);
        return returnValue;
    }

    /**
     * Get all users.
     *
     * @param pFirstIndex index of first result.
     * @param pResults no of results.
     * @param pSecurityContext security context.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper getAllUsers(int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        UserDataWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(pResults));
        returnValue = performUserGet(USERS_URL, queryParams);
        return returnValue;
    }

    /**
     * Get list of users of group with given id.
     *
     * @param pGroupId id of the ingest.
     * @param pFirstIndex index of first result.
     * @param pResults no of results.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper getUsersOfGroup(long pGroupId, int pFirstIndex, int pResults) {
        return getUsersOfGroup(pGroupId, pFirstIndex, pResults, null);
    }

    /**
     * Get list of users of group with given id.
     *
     * @param pGroupId id of the ingest.
     * @param pFirstIndex index of first result.
     * @param pResults no of results.
     * @param pSecurityContext security context.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper getUsersOfGroup(long pGroupId, int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        UserDataWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(pResults));
        returnValue = performUserGet(RestClientUtils.encodeUrl(USERS_OF_GROUP, pGroupId), queryParams);
        return returnValue;
    }

    // </editor-fold>
    //<editor-fold defaultstate="collapsed" desc="get[Group|User]ById">
    /**
     * Get group with given id.
     *
     * @param pGroupId id of the ingest.
     *
     * @return GroupWrapper.
     */
    public UserGroupWrapper getGroupById(long pGroupId) {
        return getGroupById(pGroupId, null);
    }

    /**
     * Get group with given id.
     *
     * @param pGroupId id of the ingest.
     * @param pSecurityContext security context.
     *
     * @return GroupWrapper.
     */
    public UserGroupWrapper getGroupById(long pGroupId, SimpleRESTContext pSecurityContext) {
        UserGroupWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        returnValue = performGroupGet(RestClientUtils.encodeUrl(GROUP_BY_ID, pGroupId), null);
        return returnValue;
    }

    /**
     * Get user with given id.
     *
     * @param pUserId id of the ingest.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper getUserById(long pUserId) {
        return getUserById(pUserId, null);
    }

    /**
     * Get user with given id.
     *
     * @param pUserId id of the ingest.
     * @param pSecurityContext security context.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper getUserById(long pUserId, SimpleRESTContext pSecurityContext) {
        UserDataWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        returnValue = performUserGet(RestClientUtils.encodeUrl(USER_BY_ID, pUserId), null);
        return returnValue;
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="get[Group|User]Count">
    /**
     * Get no of all ingests.
     *
     * @return GroupWrapper
     */
    public UserGroupWrapper getGroupCount() {
        return getGroupCount(null);
    }

    /**
     * Get no of all ingests.
     *
     * @param pSecurityContext security context
     * @return GroupWrapper
     */
    public UserGroupWrapper getGroupCount(SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        return performGroupGet(GROUP_COUNT, null);
    }

    /**
     * Get no of all users.
     *
     *
     * @return UserWrapper.
     */
    public UserDataWrapper getUserCount() {
        return getUserCount(null);
    }

    /**
     * Get no of all users.
     *
     * @param pSecurityContext security context.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper getUserCount(SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        return performUserGet(USER_COUNT, null);
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="update[Group|User]">

    /**
     * Update group. Please mind that the provided values pGroupName and
     * pGroupDescription are not checked. If any value is set to null, this
     * value will be stored.
     *
     * @param pGroupId id of the group to update.
     * @param pGroupName Updated name of the group.
     * @param pGroupDescription Updated description of the group.
     *
     * @return GroupWrapper.
     */
    public UserGroupWrapper updateGroup(long pGroupId, String pGroupName, String pGroupDescription) {
        return updateGroup(pGroupId, pGroupName, pGroupDescription, null);
    }

    /**
     * Update group. Please mind that the provided values pGroupName and
     * pGroupDescription are not checked. If any value is set to null, this
     * value will be stored.
     *
     * @param pGroupId id of the group to update.
     * @param pGroupName Updated name of the group.
     * @param pGroupDescription Updated description of the group.
     * @param pSecurityContext The security context.
     *
     * @return GroupWrapper.
     */
    public UserGroupWrapper updateGroup(long pGroupId, String pGroupName, String pGroupDescription, SimpleRESTContext pSecurityContext) {
        UserGroupWrapper returnValue;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);
        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_GROUP_NAME, pGroupName);
        formParams.add(Constants.REST_PARAMETER_DESCRIPTION, pGroupDescription);
        returnValue = performGroupPut(RestClientUtils.encodeUrl(GROUP_BY_ID, pGroupId), null, formParams);
        return returnValue;
    }

    /**
     * Update the users first name, last name and e-mail address. Please mind
     * that the provided values pFirstName, pLastName and pEmail are not
     * checked. If any value is set to null, this value will be stored.
     *
     * @param pGroupId id of the group.
     * @param pUserId id of the user.
     * @param pFirstName first name of the user.
     * @param pLastName last name of the user.
     * @param pEmail Email of the user.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper updateUser(String pGroupId, long pUserId, String pFirstName, String pLastName, String pEmail) {
        return updateUser(pGroupId, pUserId, pFirstName, pLastName, pEmail, null);
    }

    /**
     * Update the users first name, last name and e-mail address. Please mind
     * that the provided values pFirstName, pLastName and pEmail are not
     * checked. If any value is set to null, this value will be stored.
     *
     * @param pGroupId id of the group in which the operation will be performed.
     * @param pUserId id of the user.
     * @param pFirstName first name of the user.
     * @param pLastName last name of the user.
     * @param pEmail Email of the user.
     * @param pSecurityContext security context.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper updateUser(String pGroupId, long pUserId, String pFirstName, String pLastName, String pEmail, SimpleRESTContext pSecurityContext) {
        UserDataWrapper returnValue;
        MultivaluedMap formParams;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_USER_FIRST_NAME, pFirstName);
        formParams.add(FORM_PARAMETER_USER_LAST_NAME, pLastName);
        formParams.add(FORM_PARAMETER_USER_EMAIL, pEmail);
        returnValue = performUserPut(RestClientUtils.encodeUrl(USER_BY_ID, pUserId), queryParams, formParams);
        return returnValue;
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="add[Group|User|UserToGroup]">

    /**
     * Create a new group.
     *
     * @param pGroupId id of the group of the user (for authorization check
     * only).
     * @param pGroupName Name of the new group.
     * @param pGroupDescription Description of the new group.
     *
     * @return GroupWrapper.
     */
    public UserGroupWrapper addGroup(String pGroupId, String pGroupName, String pGroupDescription) {
        return addGroup(pGroupId, pGroupName, pGroupDescription, null);
    }

    /**
     * Create a new group.
     *
     * @param pGroupId id of the group of the user (for authorization check
     * only).
     * @param pGroupName Name of the new group.
     * @param pGroupDescription Description of the new group.
     * @param pSecurityContext The security context.
     *
     * @return GroupWrapper.
     */
    public UserGroupWrapper addGroup(String pGroupId, String pGroupName, String pGroupDescription, SimpleRESTContext pSecurityContext) {
        UserGroupWrapper returnValue;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        formParams = new MultivaluedMapImpl();
        formParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        formParams.add(FORM_PARAMETER_GROUP_NAME, pGroupName);
        formParams.add(Constants.REST_PARAMETER_DESCRIPTION, pGroupDescription);
        returnValue = performGroupPost(GROUPS_URL, null, formParams);
        return returnValue;
    }

    /**
     * Add user to group with given id.
     *
     * @param pGroupId id of the group.
     * @param pFirstName first name of the user.
     * @param pLastName last name of the user.
     * @param pEmail Email of the user.
     * @param pDistinguishedName The distinguieshed name.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper addUser(String pGroupId, String pFirstName, String pLastName, String pEmail, String pDistinguishedName) {
        return addUser(pGroupId, pFirstName, pLastName, pEmail, pDistinguishedName, null);
    }

    /**
     * Add user with given id to the system
     *
     * @param pGroupId id of the group.
     * @param pFirstName first name of the user.
     * @param pLastName last name of the user.
     * @param pEmail Email of the user.
     * @param pDistinguishedName The distinguieshed name.
     * @param pSecurityContext security context.
     *
     * @return UserWrapper.
     */
    public UserDataWrapper addUser(String pGroupId, String pFirstName, String pLastName, String pEmail, String pDistinguishedName, SimpleRESTContext pSecurityContext) {
        UserDataWrapper returnValue;
        MultivaluedMap queryParams = null;
        MultivaluedMap formParams;
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        setFilterFromContext(pSecurityContext);
        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_USER_FIRST_NAME, pFirstName);
        formParams.add(FORM_PARAMETER_USER_LAST_NAME, pLastName);
        formParams.add(FORM_PARAMETER_USER_EMAIL, pEmail);
        formParams.add(FORM_PARAMETER_DISTINGUISHED_NAME, pDistinguishedName);
        returnValue = performUserPost(USERS_URL, queryParams, formParams);
        return returnValue;
    }

    /**
     * Add user with the given id to group with given id. The
     *
     * @param pGroupId id of the group.
     * @param pUserId The primary key of the user's UserGroup record.
     *
     * @return UserGroupWrapper.
     */
    public UserGroupWrapper addUserToGroup(long pGroupId, long pUserId) {
        return addUserToGroup(pGroupId, Long.toString(pUserId));
    }

    /**
     * Add user with the given id to group with given id. The id pUserId can be
     * either the distinguished name or the string representation of the primary
     * key of the user's UserData record.
     *
     * @param pGroupId id of the group.
     * @param pUserId id of the user, either the primary key or the
     * distinguished name of the UserData record.
     *
     * @return UserGroupWrapper
     */
    public UserGroupWrapper addUserToGroup(long pGroupId, String pUserId) {
        return addUserToGroup(pGroupId, pUserId, null);
    }

    /**
     * Add user with the given id to group with given id.
     *
     * @param pGroupId id of the group.
     * @param pUserId id of the user.
     * @param pSecurityContext security context.
     *
     * @return UserGroupWrapper.
     */
    public UserGroupWrapper addUserToGroup(long pGroupId, String pUserId, SimpleRESTContext pSecurityContext) {
        UserGroupWrapper returnValue;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);
        formParams = new MultivaluedMapImpl();
        formParams.add(FORM_PARAMETER_USER_ID, pUserId);
        returnValue = performGroupPost(RestClientUtils.encodeUrl(USERS_OF_GROUP, pGroupId), null, formParams);
        return returnValue;
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="remove[User|UserFromGroup]">
    /**
     * Remove user with given id.
     *
     * @param pGroupId id of the group in which the operation will be performed.
     * @param pUserId id of the user.
     *
     * @return true if the call was successful, false otherwise.
     */
    public boolean removeUser(String pGroupId, long pUserId) {
        return removeUser(pGroupId, pUserId, null);
    }

    /**
     * Remove user with given id.
     *
     * @param pGroupId id of the group in which the operation will be performed.
     * @param pUserId id of the user.
     * @param pSecurityContext security context.
     *
     * @return true if the call was successful, false otherwise.
     */
    public boolean removeUser(String pGroupId, long pUserId, SimpleRESTContext pSecurityContext) {
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (pGroupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        return performUserDelete(RestClientUtils.encodeUrl(USER_BY_ID, pUserId), queryParams);
    }

    /**
     * Remove user from group with given id.
     *
     * @param pGroupId id of the group.
     * @param pUserId id of the user.
     *
     * @return GroupWrapper.
     */
    public UserGroupWrapper removeUserFromGroup(long pGroupId, long pUserId) {
        return removeUserFromGroup(pGroupId, pUserId, null);
    }

    /**
     * Remove user from group with given id.
     *
     * @param pGroupId id of the group.
     * @param pUserId id of the user.
     * @param pSecurityContext security context.
     *
     * @return GroupWrapper.
     */
    public UserGroupWrapper removeUserFromGroup(long pGroupId, long pUserId, SimpleRESTContext pSecurityContext) {
        UserGroupWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        returnValue = performGroupDelete(RestClientUtils.encodeUrl(USER_OF_GROUP, pGroupId, pUserId), null);
        return returnValue;
    }
    // </editor-fold>

    /**
     * Only a small test scenario. The variable 'USER_GROUP_SERVICE' has to be
     * set to 'TestService' to check.
     *
     * @param args no arguments.
     */
//  public static void main(String[] args) {
//    String accessKey = "t3Qyl4dFJ3HS4GzQ";
//    String accessSecret = "Bl9pcHHBgE6WPH7H";
//    // Alternatively put them in the arguments!
//    if (args.length == 2) {
//      accessKey = args[0];
//      accessSecret = args[1];
//    }
//    SimpleRESTContext context = new SimpleRESTContext(accessKey, accessSecret);
//    String serviceUrl = "http://141.52.65.6:8080/KITDM/rest/usergroup/";
//    UserGroupRestClient ugrc = new UserGroupRestClient(serviceUrl, context);
//    //GroupWrapper postNewGroup = ugrc.postNewGroup("groupId", "newGroupName", "Beschreibung!", context);
//    //System.out.println(postNewGroup.getEntities().get(0).getDescription());
//
//    UserWrapper userCount = ugrc.getUserCount(context);
//    System.out.println(userCount.getCount());
//    userCount = ugrc.getUsersOfGroup(1l, 0, 99, context);
//    System.out.println(userCount.getEntities().get(0));
//    userCount = ugrc.getUserById(1l, context);
//    System.out.println(userCount.getEntities().get(0));
//
//  }
}
