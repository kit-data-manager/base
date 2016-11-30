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
package edu.kit.dama.rest.sharing.services.interfaces;

import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.IDefaultGrant;
import edu.kit.dama.authorization.entities.IDefaultGrantSet;
import edu.kit.dama.authorization.entities.IDefaultReferenceId;
import edu.kit.dama.authorization.entities.ISimpleGroupId;
import edu.kit.dama.authorization.entities.ISimpleUserId;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.rest.base.IEntityWrapper;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author mf6319
 */
@Path("/rest/sharing")
public interface ISharingService extends ICommonRestInterface {

    /**
     * Create a new reference for the resource from the provided domain with the
     * provided domainUniqueId which will be accessible by the group with id
     * <i>pGroupId</i> with the role <i>pRole</i>.
     *
     * @summary Create a new resource reference.
     *
     * @param pDomain The domain of the resource that should be referenced.
     * @param pDomainUniqueId The domain unique id of the resource that should
     * be referenced.
     * @param pReferenceGroupId The id of the group who will be allowed to
     * access the resource.
     * @param pRole The role which the group gets to access the resource.
     * @param pGroupId The id of the group in which the operation will be
     * performed.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created reference id.
     *
     * @see edu.kit.dama.rest.sharing.types.ReferenceIdWrapper
     */
    @POST
    @Path(value = "/resources/references/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.IDefaultReferenceId>")
    IEntityWrapper<? extends IDefaultReferenceId> createReference(
            @FormParam("domain") String pDomain,
            @FormParam("domainUniqueId") String pDomainUniqueId,
            @FormParam("referenceGroupId") String pReferenceGroupId,
            @FormParam("role") String pRole,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get all resource references for the resource from the provided domain
     * with the provided domainUniqueId. If <i>pGroupId</i> is provided, only
     * the resource reference for this groupId is returned or nothing if this
     * group owns no reference to the resource.
     *
     * @summary Get all resource references or the resource reference for the
     * group with the provided id.
     *
     * @param pDomain The domain of the resource that should be referenced.
     * @param pDomainUniqueId The domain unique id of the resource that should
     * be referenced.
     * @param pGroupId The id of the group that is used to query for references.
     * @param hc The HttpContext for OAuth check.
     *
     * @return All reference ids.
     *
     * @see edu.kit.dama.rest.sharing.types.ReferenceIdWrapper
     */
    @GET
    @Path(value = "/resources/references/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.IDefaultReferenceId>")
    IEntityWrapper<? extends IDefaultReferenceId> getReferences(
            @QueryParam("domain") String pDomain,
            @QueryParam("domainUniqueId") String pDomainUniqueId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get all groups referencing the resource from the provided domain with the
     * domainUniqueId.
     *
     * @summary Get all groups referencing a resource.
     *
     * @param pDomain The domain of the resource for which the authorized groups
     * should be obtained.
     * @param pDomainUniqueId The domain unique id of the resource for which the
     * authorized groups should be obtained.
     * @param pRole The role which the returned groups should have at most.
     * @param pGroupId The id of the group that is used to access the in whose
     * name the users will be queried.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The list of referencing groups.
     *
     * @see edu.kit.dama.rest.sharing.types.GroupIdWrapper
     */
    @GET
    @Path(value = "/resources/references/groups")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.ISimpleGroupId>")
    IEntityWrapper<? extends ISimpleGroupId> getReferencedGroups(
            @QueryParam("domain") String pDomain,
            @QueryParam("domainUniqueId") String pDomainUniqueId,
            @QueryParam("role") String pRole,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Delete the resource references with the provided id.
     *
     * @summary Delete the resource references with the provided id.
     *
     * @param pDomain The domain of the resource reference which should be
     * deleted.
     * @param pDomainUniqueId The id of the resource reference which should be
     * deleted.
     * @param pGroupId The id of the group which owns the resource reference
     * which should be deleted.
     *
     * @param hc The HttpContext for OAuth check.
     *
     * @return The response with an according HTTP response code.
     */
    @DELETE
    @Path(value = "/resources/references/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response deleteReference(
            @QueryParam("domain") String pDomain,
            @QueryParam("domainUniqueId") String pDomainUniqueId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get all single user grants for the resource from the provided domain with
     * the domainUniqueId.
     *
     * @summary Get all grants for a resource.
     *
     * @param pDomain The domain of the resource for which the grants should be
     * obtained.
     * @param pDomainUniqueId The domain unique id of the resource for which the
     * grants should be obtained.
     * @param pGroupId The id of the group in whose name the grants will be
     * queried.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The resulting grant set.
     *
     * @see edu.kit.dama.rest.sharing.types.GrantSetWrapper
     */
    @GET
    @Path(value = "/resources/grants/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.IDefaultGrantSet>")
    IEntityWrapper<? extends IDefaultGrantSet> getGrantSetForResource(
            @QueryParam("domain") String pDomain,
            @QueryParam("domainUniqueId") String pDomainUniqueId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Get details about the grant with the provided id.
     *
     * @summary Get details for a specific grant.
     *
     * @param pId The id of the grant.
     * @param pGroupId The id of the group in whose name the grant will be
     * queried.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The grant.
     *
     * @see edu.kit.dama.rest.sharing.types.GrantWrapper
     */
    @GET
    @Path(value = "/resources/grants/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.IDefaultGrant>")
    IEntityWrapper<? extends IDefaultGrant> getGrantById(
            @PathParam("id") Long pId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Create a new access grant for the resource from the provided domain with
     * the domainUniqueId, for the user with the id pUserId and the role pRole.
     * If grants are not allowed for the resource, grants will be allowed first
     * with the limitation to role MEMBER and then the new grant will be added.
     *
     * @summary Grant access to a resource for a user.
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
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created grant.
     *
     * @see edu.kit.dama.rest.sharing.types.GrantWrapper
     */
    @POST
    @Path(value = "/resources/grants/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.IDefaultGrant>")
    IEntityWrapper<? extends IDefaultGrant> createGrant(
            @FormParam("domain") String pDomain,
            @FormParam("domainUniqueId") String pDomainUniqueId,
            @FormParam("userId") String pUserId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @FormParam("role") String pRole,
            @javax.ws.rs.core.Context HttpContext hc
    );

        
    /**
     * Revoke the grant with the provided id.
     *
     * @summary Revoke a specific grant.
     *
     * @param pId The id of the grant to revoke.
     * @param pGroupId The id of the group in whose name the grant will be
     * revoked.
     * @param hc The HttpContext for OAuth check.
     *
     * @return HTTP response with an according HTTP status code.
     */
    @DELETE
    @Path(value = "/resources/grants/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response revokeGrant(
            @PathParam("id") Long pId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Revoke all grants for the resource from the provided domain with the
     * domainUniqueId. Finally, allowing grants for the resource will be
     * forbidden until a new grant is added or allowing grants is enabled by the
     * according API separately.
     *
     * @summary Revoke all grants for a resource.
     *
     * @param pDomain The domain of the resource for which access should be
     * revoked.
     * @param pDomainUniqueId The domain unique id of the resource for which
     * access should be revoked.
     * @param pGroupId The id of the group in whose name the grants will be
     * revoked.
     * @param hc The HttpContext for OAuth check.
     *
     * @return HTTP response with an according HTTP status code.
     */
    @DELETE
    @Path(value = "/resources/grants/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response revokeAllGrants(
            @QueryParam("domain") String pDomain,
            @QueryParam("domainUniqueId") String pDomainUniqueId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Update the role of the grant with the provided id to pRole.
     *
     * @summary Update a specific grant.
     *
     * @param pId The id of the grant.
     * @param pGroupId The id of the group in whose name the grant will be
     * updated.
     * @param pRole The role which will be granted.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The updated grant.
     *
     * @see edu.kit.dama.rest.sharing.types.GrantWrapper
     */
    @PUT
    @Path(value = "/resources/grants/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.IDefaultGrant>")
    IEntityWrapper<? extends IDefaultGrant> updateGrant(
            @PathParam("id") Long pId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @FormParam("role") String pRole,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Get all users with granted access for the resource from the provided
     * domain with the domainUniqueId with at least pRole.
     *
     * @summary Get all users with granted access for a resource.
     *
     * @param pDomain The domain of the resource for which the granted users
     * should be obtained.
     * @param pDomainUniqueId The domain unique id of the resource for which the
     * granted users should be obtained.
     * @param pRole The minimum role the user must have.
     * @param pGroupId The id of the group in whose name the users will be
     * queried.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The list of userIds.
     *
     * @see edu.kit.dama.rest.sharing.types.UserIdWrapper
     */
    @GET
    @Path(value = "/resources/grants/users")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.ISimpleUserId>")
    IEntityWrapper<? extends ISimpleUserId> getAuthorizedUsers(
            @QueryParam("domain") String pDomain,
            @QueryParam("domainUniqueId") String pDomainUniqueId,
            @QueryParam("role") String pRole,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );
}
