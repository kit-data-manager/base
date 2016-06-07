/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
package edu.kit.dama.rest.sharing.client.impl;

import com.sun.jersey.api.client.ClientResponse;
import edu.kit.dama.rest.sharing.types.ReferenceIdWrapper;
import edu.kit.dama.rest.sharing.types.GrantSetWrapper;
import edu.kit.dama.rest.sharing.types.GroupIdWrapper;
import edu.kit.dama.rest.sharing.types.UserIdWrapper;
import edu.kit.dama.rest.sharing.types.GrantWrapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.util.Constants;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Rest client for Sharing service.
 *
 * @author mf6319
 */
public final class SharingRestClient extends AbstractRestClient {

  /**
   * The logger
   */
  //private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SharingRestClient.class);
  //<editor-fold defaultstate="collapsed" desc="Parameter names">
  /**
   * Id of the user.
   */
  protected static final String PARAMETER_USER_ID = "userId";
  /**
   * Domain parameter
   */
  protected static final String PARAMETER_DOMAIN = "domain";
  /**
   * DomainUniqueId parameter
   */
  protected static final String PARAMETER_DOMAIN_UNIQUE_ID = "domainUniqueId";
  /**
   * ReferenceGroup parameter
   */
  protected static final String PARAMETER_REFERENCE_GROUP_ID = "referenceGroupId";

  /**
   * Role parameter
   */
  protected static final String PARAMETER_ROLE = "role";

//</editor-fold>
  // <editor-fold defaultstate="collapsed" desc="URL components">
  /**
   * References part.
   */
  private static final String REFERENCES_URL = "/resources/references";
  /**
   * Grants part.
   */
  private static final String GRANTS_URL = "/resources/grants";
  /**
   * Get grant by given id.
   */
  private static final String GRANT_BY_ID = GRANTS_URL + "/{0}";
  /**
   * List all users with granted access.
   */
  private static final String AUTHORIZED_USERS = GRANTS_URL + "/users";
  /**
   * List all groups with granted access.
   */
  private static final String REFERENCED_GROUPS = REFERENCES_URL + "/groups";
// </editor-fold>

  /**
   * Create a REST client with a predefined context.
   *
   * @param rootUrl root url of the staging service. (e.g.:
   * "http://dama.lsdf.kit.edu/KITDM/rest/UserGroupService")
   * @param pContext initial context
   */
  public SharingRestClient(String rootUrl, SimpleRESTContext pContext) {
    super(rootUrl, pContext);
  }

  // <editor-fold defaultstate="collapsed" desc="Generic Rest methods (GET, PUT, POST, DELETE)">
  /**
   * Perform a get for a resourceId.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   *
   * @return ReferenceIdWrapper.
   */
  private ReferenceIdWrapper performResourceIdGet(String pPath, MultivaluedMap pQueryParams) {
    return RestClientUtils.performGet(ReferenceIdWrapper.class, getWebResource(pPath), pQueryParams);
  }

  /**
   * Perform a get for a userId.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   *
   * @return UserIdWrapper.
   */
  private UserIdWrapper performUserIdGet(String pPath, MultivaluedMap pQueryParams) {
    return RestClientUtils.performGet(UserIdWrapper.class, getWebResource(pPath), pQueryParams);
  }

  /**
   * Perform a get for a groupId.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   *
   * @return GroupIdWrapper.
   */
  private GroupIdWrapper performGroupIdGet(String pPath, MultivaluedMap pQueryParams) {
    return RestClientUtils.performGet(GroupIdWrapper.class, getWebResource(pPath), pQueryParams);
  }

  /**
   * Perform a get for a grantSet.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   *
   * @return GrantSetWrapper.
   */
  private GrantSetWrapper performGrantSetGet(String pPath, MultivaluedMap pQueryParams) {
    return RestClientUtils.performGet(GrantSetWrapper.class, getWebResource(pPath), pQueryParams);
  }

  /**
   * Perform a get for a grant.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   *
   * @return GrantWrapper.
   */
  private GrantWrapper performGrantGet(String pPath, MultivaluedMap pQueryParams) {
    return RestClientUtils.performGet(GrantWrapper.class, getWebResource(pPath), pQueryParams);
  }

  /**
   * Perform a post for referenceId.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   * @param pFormParams Form parameters
   *
   * @return ReferenceIdWrapper.
   */
  private ReferenceIdWrapper performReferenceIdPost(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
    return RestClientUtils.performPost(ReferenceIdWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
  }

  /**
   * Perform a post for grant.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   * @param pFormParams Form parameters
   *
   * @return GrantWrapper.
   */
  private GrantWrapper performGrantPost(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
    return RestClientUtils.performPost(GrantWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
  }

  /**
   * Perform a delete for an entity.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   *
   * @return Response.
   */
  private boolean performDelete(String pPath, MultivaluedMap pQueryParams) {
    return ((ClientResponse) RestClientUtils.performDelete(null, getWebResource(pPath), pQueryParams)).getStatus() == 200;
  }

  /**
   * Perform a put for grant.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   * @param pFormParams Form parameters
   *
   * @return GrantSetWrapper.
   */
  private GrantWrapper performGrantPut(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
    return RestClientUtils.performPut(GrantWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
  }

  // </editor-fold>
  //<editor-fold defaultstate="collapsed" desc="get[AuthorizedUsers|GrantById|ReferencedGroups|GrantSetForResource|References]">
  /**
   * Get all users with granted access for the resource from the provided domain
   * with the domainUniqueId with at least pRole.
   *
   * @param pDomain The domain of the resource for which the granted users
   * should be obtained.
   * @param pDomainUniqueId The domain unique id of the resource for which the
   * granted users should be obtained.
   * @param pRole The minimum role the user must have.
   * @param pGroupId The id of the group in whose name the users will be
   * queried.
   * @param pSecurityContext The SecurityContext for OAuth check.
   *
   * @return The list of userIds.
   *
   * @see edu.kit.dama.rest.sharing.types.UserIdWrapper
   */
  public UserIdWrapper getAuthorizedUsers(String pDomain, String pDomainUniqueId, Role pRole, String pGroupId, SimpleRESTContext pSecurityContext) {
    UserIdWrapper returnValue;
    setFilterFromContext(pSecurityContext);
    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pDomain == null || pDomainUniqueId == null || pRole == null) {
      throw new IllegalArgumentException("Neither pDomain nor pDomainUniqueId or pRole must be null.");
    }

    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }

    queryParams.add(PARAMETER_DOMAIN, pDomain);
    queryParams.add(PARAMETER_DOMAIN_UNIQUE_ID, pDomainUniqueId);
    queryParams.add(PARAMETER_ROLE, pRole.toString());

    returnValue = performUserIdGet(AUTHORIZED_USERS, queryParams);
    return returnValue;
  }

  /**
   * Get all groups referencing the resource from the provided domain with the
   * domainUniqueId.
   *
   * @param pDomain The domain of the resource for which the authorized groups
   * should be obtained.
   * @param pDomainUniqueId The domain unique id of the resource for which the
   * authorized groups should be obtained.
   * @param pRole The role which the returned groups should have at most.
   * @param pGroupId The id of the group in whose name the users will be
   * queried.
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return The list of userIds.
   *
   * @see edu.kit.dama.rest.sharing.types.GroupIdWrapper
   */
  public GroupIdWrapper getReferencedGroups(String pDomain,
          String pDomainUniqueId,
          Role pRole,
          String pGroupId,
          SimpleRESTContext pSecurityContext) {
    GroupIdWrapper returnValue;
    setFilterFromContext(pSecurityContext);
    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pDomain == null || pDomainUniqueId == null || pRole == null) {
      throw new IllegalArgumentException("Neither pDomain nor pDomainUniqueId or pRole must be null.");
    }

    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }

    queryParams.add(PARAMETER_DOMAIN, pDomain);
    queryParams.add(PARAMETER_DOMAIN_UNIQUE_ID, pDomainUniqueId);
    queryParams.add(PARAMETER_ROLE, pRole.toString());
    returnValue = performGroupIdGet(REFERENCED_GROUPS, queryParams);
    return returnValue;
  }

  /**
   * Get details about the grant with the provided id.
   *
   * @param pId The id of the grant.
   * @param pGroupId The id of the group in whose name the grant will be
   * queried.
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return The grant.
   *
   * @see edu.kit.dama.rest.sharing.types.GrantWrapper
   */
  public GrantWrapper getGrantById(Long pId,
          String pGroupId,
          SimpleRESTContext pSecurityContext) {
    GrantWrapper returnValue;
    setFilterFromContext(pSecurityContext);

    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pId == null) {
      throw new IllegalArgumentException("Argument pId must not be null");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    returnValue = performGrantGet(RestClientUtils.encodeUrl(GRANT_BY_ID, pId), queryParams);
    return returnValue;

  }

  /**
   * Get all single user grants for the resource from the provided domain with
   * the domainUniqueId.
   *
   * @param pDomain The domain of the resource for which the grants should be
   * obtained.
   * @param pDomainUniqueId The domain unique id of the resource for which the
   * grants should be obtained.
   * @param pGroupId The id of the group in whose name the grants will be
   * queried.
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return The resulting GrantSet.
   *
   * @see edu.kit.dama.rest.sharing.types.GrantSetWrapper
   */
  public GrantSetWrapper getGrantSetForResource(String pDomain,
          String pDomainUniqueId,
          String pGroupId,
          SimpleRESTContext pSecurityContext) {
    GrantSetWrapper returnValue;
    setFilterFromContext(pSecurityContext);
    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pDomain == null || pDomainUniqueId == null) {
      throw new IllegalArgumentException("Neither pDomain nor pDomainUniqueId must be null.");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    queryParams.add(PARAMETER_DOMAIN, pDomain);
    queryParams.add(PARAMETER_DOMAIN_UNIQUE_ID, pDomainUniqueId);
    returnValue = performGrantSetGet(GRANTS_URL, queryParams);
    return returnValue;
  }

  /**
   * Get all resource references for the resource from the provided domain with
   * the provided domainUniqueId. If <i>pGroupId</i> is provided, only the
   * resource reference for this groupId is returned or nothing if this group
   * owns no reference to the resource.
   *
   *
   * @param pDomain The domain of the resource that should be referenced.
   * @param pDomainUniqueId The domain unique id of the resource that should be
   * referenced.
   * @param pGroupId The id of the group who will be allowed to access the
   * resource.
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return All ReferenceIds.
   *
   * @see edu.kit.dama.rest.sharing.types.ReferenceIdWrapper
   */
  public ReferenceIdWrapper getReferences(String pDomain,
          String pDomainUniqueId,
          String pGroupId,
          SimpleRESTContext pSecurityContext) {
    ReferenceIdWrapper returnValue;
    setFilterFromContext(pSecurityContext);
    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pDomain == null || pDomainUniqueId == null) {
      throw new IllegalArgumentException("Neither pDomain nor pDomainUniqueId must be null.");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    queryParams.add(PARAMETER_DOMAIN, pDomain);
    queryParams.add(PARAMETER_DOMAIN_UNIQUE_ID, pDomainUniqueId);
    returnValue = performResourceIdGet(REFERENCES_URL, queryParams);
    return returnValue;

  }

//</editor-fold>
  //<editor-fold defaultstate="collapsed" desc="create[Grant|Reference]">
  /**
   * Create a new access grant for the resource from the provided domain with
   * the domainUniqueId, for the user with the id pUserId and the role pRole. If
   * grants are not allowed for the resource, grants will be allowed first with
   * the limitation to role MEMBER and then the new grant will be added.
   *
   * @param pDomain The domain of the resource for which access should be
   * granted.
   * @param pDomainUniqueId The domain unique id of the resource for which
   * access should be granted.
   * @param pUserId The id of the user who should be allowed to access the
   * resource.
   * @param pGroupId The id of the group in whose name the grant will be
   * created.
   * @param pRole The role which will be granted.
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return The created grant.
   *
   * @see edu.kit.dama.rest.sharing.types.GrantWrapper
   */
  public GrantWrapper createGrant(String pDomain,
          String pDomainUniqueId,
          String pUserId,
          String pGroupId,
          Role pRole,
          SimpleRESTContext pSecurityContext) {
    GrantWrapper returnValue;
    setFilterFromContext(pSecurityContext);
    MultivaluedMap formParams;
    formParams = new MultivaluedMapImpl();
    if (pDomain == null || pDomainUniqueId == null || pUserId == null || pRole == null) {
      throw new IllegalArgumentException("Neither pDomain nor pDomainUniqueId, pUserId or pRole must be null.");
    }

    formParams.add(PARAMETER_DOMAIN, pDomain);
    formParams.add(PARAMETER_DOMAIN_UNIQUE_ID, pDomainUniqueId);
    formParams.add(PARAMETER_USER_ID, pUserId);
    formParams.add(PARAMETER_ROLE, pRole.toString());

    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    returnValue = performGrantPost(GRANTS_URL, queryParams, formParams);
    return returnValue;
  }

  /**
   * Create a new reference for the resource from the provided domain with the
   * provided domainUniqueId which will be accessible by the group with id
   * <i>pGroupId</i> with the role <i>pRole</i>.
   *
   * @param pDomain The domain of the resource that should be referenced.
   * @param pDomainUniqueId The domain unique id of the resource that should be
   * referenced.
   * @param pReferenceGroupId The id of the group who will be allowed to access
   * the resource.
   * @param pRole The role which the group gets to access the resource.
   * @param pGroupId The id of the group in which the operation is performed.
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return The created ReferenceId.
   *
   * @see edu.kit.dama.rest.sharing.types.ReferenceIdWrapper
   */
  public ReferenceIdWrapper createReference(String pDomain,
          String pDomainUniqueId,
          String pReferenceGroupId,
          Role pRole,
          String pGroupId,
          SimpleRESTContext pSecurityContext) {
    ReferenceIdWrapper returnValue;
    setFilterFromContext(pSecurityContext);
    MultivaluedMap formParams;
    formParams = new MultivaluedMapImpl();
    if (pDomain == null || pDomainUniqueId == null || pReferenceGroupId == null || pRole == null) {
      throw new IllegalArgumentException("Neither pDomain nor pDomainUniqueId, pRole or pReferenceGroupId must be null.");
    }

    formParams.add(PARAMETER_DOMAIN, pDomain);
    formParams.add(PARAMETER_DOMAIN_UNIQUE_ID, pDomainUniqueId);
    formParams.add(PARAMETER_REFERENCE_GROUP_ID, pReferenceGroupId);
    formParams.add(PARAMETER_ROLE, pRole.toString());

    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    returnValue = performReferenceIdPost(REFERENCES_URL, queryParams, formParams);
    return returnValue;
  }

//</editor-fold>
  //<editor-fold defaultstate="collapsed" desc="revoke[Grant|AllGrants],deleteReference">
  /**
   * Revoke the Grant with the provided id.
   *
   * @param pId The id of the Grant to revoke.
   * @param pGroupId The id of the group in whose name the Grant will be
   * revoked.
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return HTTP response with an according HTTP status code.
   */
  public boolean revokeGrant(Long pId,
          String pGroupId,
          SimpleRESTContext pSecurityContext) {
    setFilterFromContext(pSecurityContext);
    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pId == null) {
      throw new IllegalArgumentException("Argument pId must not be null.");
    }

    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    return performDelete(RestClientUtils.encodeUrl(GRANT_BY_ID, pId), queryParams);
  }

  /**
   * Revoke all Grants for the resource from the provided domain with the
   * domainUniqueId. Finally, allowing Grants for the resource will be forbidden
   * until a new Grant is added or allowing Grants is enabled by the according
   * API separately.
   *
   * @param pDomain The domain of the resource for which access should be
   * revoked.
   * @param pDomainUniqueId The domain unique id of the resource for which
   * access should be revoked.
   * @param pGroupId The id of the group in whose name the Grants will be
   * revoked.
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return HTTP response with an according HTTP status code.
   */
  public boolean revokeAllGrants(String pDomain,
          String pDomainUniqueId,
          String pGroupId,
          SimpleRESTContext pSecurityContext) {
    setFilterFromContext(pSecurityContext);

    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pDomain == null || pDomainUniqueId == null) {
      throw new IllegalArgumentException("Neither pDomain nor pDomainUniqueId must be null.");
    }

    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    queryParams.add(PARAMETER_DOMAIN, pDomain);
    queryParams.add(PARAMETER_DOMAIN_UNIQUE_ID, pDomainUniqueId);

    return performDelete(GRANTS_URL, queryParams);
  }

  /**
   * Delete the resource references with the provided id.
   *
   * @param pDomain The domain of the resource reference which should be
   * deleted.
   * @param pDomainUniqueId The id of the resource reference which should be
   * deleted.
   * @param pGroupId The id of the group which owns the resource reference which
   * should be deleted.
   *
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return The response with an according HTTP response code.
   */
  public boolean deleteReference(String pDomain,
          String pDomainUniqueId,
          String pGroupId,
          SimpleRESTContext pSecurityContext) {
    setFilterFromContext(pSecurityContext);

    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pDomain == null || pDomainUniqueId == null) {
      throw new IllegalArgumentException("Neither pDomain nor pDomainUniqueId must be null.");
    }

    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    queryParams.add(PARAMETER_DOMAIN, pDomain);
    queryParams.add(PARAMETER_DOMAIN_UNIQUE_ID, pDomainUniqueId);

    return performDelete(REFERENCES_URL, queryParams);
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="updateGrant">
  /**
   * Update the role of the grant with the provided id to pRole.
   *
   * @param pId The id of the grant.
   * @param pGroupId The id of the group in whose name the grant will be
   * updated.
   * @param pRole The role which will be granted.
   * @param pSecurityContext The SimpleRESTContext for OAuth check.
   *
   * @return The updated grant.
   *
   * @see edu.kit.dama.rest.sharing.types.GrantWrapper
   */
  public GrantWrapper updateGrant(Long pId,
          String pGroupId,
          Role pRole,
          SimpleRESTContext pSecurityContext) {
    GrantWrapper returnValue;
    setFilterFromContext(pSecurityContext);
    MultivaluedMap formParams;
    formParams = new MultivaluedMapImpl();
    if (pId == null || pRole == null) {
      throw new IllegalArgumentException("Neither pId nor pRole must be null.");
    }

    formParams.add(PARAMETER_ROLE, pRole.toString());

    MultivaluedMap queryParams;
    queryParams = new MultivaluedMapImpl();
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    returnValue = performGrantPut(RestClientUtils.encodeUrl(GRANT_BY_ID, pId), queryParams, formParams);
    return returnValue;
  }

//</editor-fold>
  /**
   * Only a small test scenario.
   *
   * @param args no arguments.
   */
//  public static void main(String[] args) {
//    String accessKey = "admin";//"tester";
//    //accessKey = "tester";
//    String accessSecret = "dama14";
//
//    // Alternatively put them in the arguments!
//    if (args.length == 2) {
//      accessKey = args[0];
//      accessSecret = args[1];
//    }
//    SimpleRESTContext context = new SimpleRESTContext(accessKey, accessSecret);
//    String serviceUrl = "http://localhost:8080/AdminUI/rest/sharing/";
//    SharingRestClient src = new SharingRestClient(serviceUrl, context);
//
//    //src.getGrantById(1l, "USERS", context);
//    //src.createGrant("edu.kit.dama.mdm.base.Study", "e3b9e16f-967c-49d9-83bc-c03d819c5496", "tester", "TESTERS", Role.NO_ACCESS, context);
//    //GrantSetWrapper gsw = src.getGrantSetForResource("edu.kit.dama.mdm.base.Study", "e3b9e16f-967c-49d9-83bc-c03d819c5496", "TESTERS", context);
//    //System.out.println(gsw.getEntities().get(0).getGrants());
//    //src.updateGrant(1l, "TESTERS", Role.NO_ACCESS, context);
//    //src.createReference("edu.kit.dama.mdm.base.Study", "e3b9e16f-967c-49d9-83bc-c03d819c5496", "USERS", Role.MANAGER, "TESTERS", context);
//    //src.deleteReference("edu.kit.dama.mdm.base.Study", "e3b9e16f-967c-49d9-83bc-c03d819c5496", "USERS", context);
//    //System.out.println(src.getAuthorizedUsers("edu.kit.dama.mdm.base.Study", "e3b9e16f-967c-49d9-83bc-c03d819c5496", Role.MEMBER, "USERS", context).getEntities());
//    //System.out.println(src.getReferences("edu.kit.dama.mdm.base.Study", "e3b9e16f-967c-49d9-83bc-c03d819c5496", "USERS", context).getEntities());
//    src.deleteReference("edu.kit.dama.mdm.base.Study", "e3b9e16f-967c-49d9-83bc-c03d819c5496", "TESTERS", context);
//    System.out.println(src.getReferencedGroups("edu.kit.dama.mdm.base.Study", "e3b9e16f-967c-49d9-83bc-c03d819c5496", Role.MEMBER, "USERS", context).getEntities());
//  }
}
