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
import edu.kit.dama.authorization.entities.IDefaultReferenceId;
import edu.kit.dama.authorization.entities.ISimpleGrantSet;
import edu.kit.dama.authorization.entities.ISimpleGroupId;
import edu.kit.dama.authorization.entities.ISimpleUserId;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.rest.base.IEntityWrapper;
import javax.ws.rs.DELETE;
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
     * @return The created ReferenceId serialized using the
     * <b>default</b> object graph of ReferenceIdWrapper, which contains all
     * attributes.
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
            @QueryParam("referenceGroupId") String pReferenceGroupId,
            @FormParam("role") String pRole,
            @QueryParam("groupId") String pGroupId,
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
     * @return All ReferenceIds serialized using the
     * <b>default</b> object graph of ReferenceIdWrapper, which contains the id
     * of the resource reference.
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
            @QueryParam("groupId") String pGroupId,
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
     * @return The list of userIds serialized using the
     * <b>default</b> object graph of GroupIdWrapper, which contains all
     * attributes.
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
            @QueryParam("groupId") String pGroupId,
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
            @QueryParam("groupId") String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get all single user Grants for the resource from the provided domain with
     * the domainUniqueId.
     *
     * @summary Get all Grants for a resource.
     *
     * @param pDomain The domain of the resource for which the Grants should be
     * obtained.
     * @param pDomainUniqueId The domain unique id of the resource for which the
     * Grants should be obtained.
     * @param pGroupId The id of the group in whose name the Grants will be
     * queried.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The resulting GrantSet serialized using the
     * <b>simple</b> object graph of GrantSetWrapper, which contains all
     * attributes of the GrantSet itself and the ids of the contained Grants.
     *
     * @see edu.kit.dama.rest.sharing.types.GrantSetWrapper
     */
    @GET
    @Path(value = "/resources/grants/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.ISimpleGrantSet>")
    IEntityWrapper<? extends ISimpleGrantSet> getGrantSetForResource(
            @QueryParam("domain") String pDomain,
            @QueryParam("domainUniqueId") String pDomainUniqueId,
            @QueryParam("groupId") String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Get details about the Grant with the provided id.
     *
     * @summary Get details for a specific Grant.
     *
     * @param pId The id of the Grant.
     * @param pGroupId The id of the group in whose name the Grant will be
     * queried.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The Grant serialized using the
     * <b>default</b> object graph of GrantWrapper, which contains all
     * attributes.
     *
     * @see edu.kit.dama.rest.sharing.types.GrantWrapper
     */
    @GET
    @Path(value = "/resources/grants/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.IDefaultGrant>")
    IEntityWrapper<? extends IDefaultGrant> getGrantById(
            @PathParam("id") Long pId,
            @QueryParam("groupId") String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Create a new access Grant for the resource from the provided domain with
     * the domainUniqueId, for the user with the id pUserId and the role pRole.
     * If Grants are not allowed for the resource, Grants will be allowed first
     * with the limitation to role MEMBER and then the new Grant will be added.
     *
     * @summary Grant access to a resource for a user.
     *
     * @param pDomain The domain of the resource for which access should be
     * granted.
     * @param pDomainUniqueId The domain unique id of the resource for which
     * access should be granted.
     * @param pUserId The id of the user who should be allowed to access the
     * resource.
     * @param pGroupId The id of the group in whose name the Grant will be
     * created.
     * @param pRole The role which will be granted.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created Grant serialized using the
     * <b>default</b> object graph of GrantWrapper, which contains all
     * attributes.
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
            @QueryParam("groupId") String pGroupId,
            @FormParam("role") String pRole,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Revoke the Grant with the provided id.
     *
     * @summary Revoke a specific Grant.
     *
     * @param pId The id of the Grant to revoke.
     * @param pGroupId The id of the group in whose name the Grant will be
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
            @QueryParam("groupId") String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Revoke all Grants for the resource from the provided domain with the
     * domainUniqueId. Finally, allowing Grants for the resource will be
     * forbidden until a new Grant is added or allowing Grants is enabled by the
     * according API separately.
     *
     * @summary Revoke all Grants for a resource.
     *
     * @param pDomain The domain of the resource for which access should be
     * revoked.
     * @param pDomainUniqueId The domain unique id of the resource for which
     * access should be revoked.
     * @param pGroupId The id of the group in whose name the Grants will be
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
            @QueryParam("groupId") String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Update the role of the Grant with the provided id to pRole.
     *
     * @summary Update a specific Grant.
     *
     * @param pId The id of the Grant.
     * @param pGroupId The id of the group in whose name the Grant will be
     * updated.
     * @param pRole The role which will be granted.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The updated Grant serialized using the
     * <b>default</b> object graph of GrantWrapper, which contains all
     * attributes.
     *
     * @see edu.kit.dama.rest.sharing.types.GrantWrapper
     */
    @PUT
    @Path(value = "/resources/grants/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.authorization.entities.IDefaultGrant>")
    IEntityWrapper<? extends IDefaultGrant> updateGrant(
            @PathParam("id") Long pId,
            @QueryParam("groupId") String pGroupId,
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
     * @return The list of userIds serialized using the
     * <b>default</b> object graph of UserIdWrapper, which contains all
     * attributes.
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
            @QueryParam("groupId") String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );
}
