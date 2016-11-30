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
package edu.kit.dama.rest.sharing.services.impl;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.IDefaultGrant;
import edu.kit.dama.authorization.entities.IDefaultGrantSet;
import edu.kit.dama.authorization.entities.IDefaultReferenceId;
import edu.kit.dama.authorization.entities.ISimpleGroupId;
import edu.kit.dama.authorization.entities.ISimpleUserId;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.entities.impl.GrantSet;
import edu.kit.dama.authorization.entities.impl.SecurableResource;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.ResourceServiceLocal;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.util.RestUtils;
import edu.kit.dama.rest.sharing.services.interfaces.ISharingService;
import edu.kit.dama.rest.sharing.types.GrantSetWrapper;
import edu.kit.dama.rest.sharing.types.GrantWrapper;
import edu.kit.dama.rest.sharing.types.GroupIdWrapper;
import edu.kit.dama.rest.sharing.types.ReferenceIdWrapper;
import edu.kit.dama.rest.sharing.types.UserIdWrapper;
import edu.kit.dama.util.Constants;
import java.util.List;
import javax.persistence.EntityManager;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
@Path("/")
public final class SharingRestServiceImpl implements ISharingService {

    private static final Class[] IMPL_CLASSES = new Class[]{
        GrantSetWrapper.class,
        ReferenceIdWrapper.class,
        GrantSet.class,
        Grant.class,
        SecurableResource.class,
        ReferenceId.class,
        Role.class,
        User.class
    };
    private static final Logger LOGGER = LoggerFactory.getLogger(SharingRestServiceImpl.class);

    @Override
    public IEntityWrapper<? extends IDefaultReferenceId> createReference(String pDomain, String pDomainUniqueId, String pReferenceGroupId, String pRole, String pGroupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        LOGGER.debug("Creating referenceId for domain {}, uniqueId {} and reference group {}", pDomain, pDomainUniqueId, pReferenceGroupId);
        ReferenceId refId = factoryReferenceId(pDomain, pDomainUniqueId, pReferenceGroupId);

        try {
            LOGGER.debug("Try creating reference {}", refId);
            ResourceServiceLocal.getSingleton().createReference(refId, Role.valueOf(pRole), ctx);
            LOGGER.debug("Reference successfully created.");
            // return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new ReferenceIdWrapper(refId));
            return new ReferenceIdWrapper(refId);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to create reference for resource " + refId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while creating a ResourceReference. Probably, the resource " + factoryResourceId(pDomain, pDomainUniqueId) + " was not registered before.", ex);
            throw new WebApplicationException(404);
        } catch (EntityAlreadyExistsException ex) {
            LOGGER.error("Failed to create resource reference for resource " + factoryResourceId(pDomain, pDomainUniqueId) + " and group " + pGroupId + " . Reference already exists.", ex);
            throw new WebApplicationException(409);
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultReferenceId> getReferences(String pDomain, String pDomainUniqueId, String pGroupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        SecurableResourceId resId = factoryResourceId(pDomain, pDomainUniqueId);
        try {
            // return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new ReferenceIdWrapper(ResourceServiceLocal.getSingleton().getReferences(resId, ctx)));
            return new ReferenceIdWrapper(ResourceServiceLocal.getSingleton().getReferences(resId, ctx));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get references for resource " + resId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while obtaining ResourceReferences. Probably, the resource " + new SecurableResourceId(pDomain, pDomainUniqueId) + " was not registered before.", ex);
            throw new WebApplicationException(404);
        }
    }

    @Override
    public IEntityWrapper<? extends ISimpleGroupId> getReferencedGroups(String pDomain, String pDomainUniqueId, String pRole, String pGroupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        SecurableResourceId resId = factoryResourceId(pDomain, pDomainUniqueId);
        try {
            LOGGER.debug("Try to obtain groups referencing resource {} with at least role {}", resId, pRole);
            return new GroupIdWrapper(ResourceServiceLocal.getSingleton().getAuthorizedGroups(resId, Role.valueOf(pRole), ctx));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtained referenced groups for resource " + resId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while obtaining ResourceReferences. Probably, the resource " + new SecurableResourceId(pDomain, pDomainUniqueId) + " was not registered before.", ex);
            throw new WebApplicationException(404);
        }
    }

    @Override
    public Response deleteReference(String pDomain, String pDomainUniqueId, String pGroupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        ReferenceId refId = factoryReferenceId(pDomain, pDomainUniqueId, pGroupId);
        try {
            LOGGER.debug("Try to delete reference {}", refId);
            ResourceServiceLocal.getSingleton().deleteReference(refId, ctx);
            LOGGER.debug("Reference successfully deleted.");
            return Response.ok().build();
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to delete reference with id " + refId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while obtaining ResourceReferences. Probably, the resource " + new SecurableResourceId(pDomain, pDomainUniqueId) + " was not registered before.", ex);
            throw new WebApplicationException(404);
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultGrantSet> getGrantSetForResource(String pDomain, String pDomainUniqueId, String pGroupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        SecurableResourceId resId = factoryResourceId(pDomain, pDomainUniqueId);
        try {
            LOGGER.debug("Try getting grantset for resource {}", resId);
            List<Grant> grants = ResourceServiceLocal.getSingleton().getGrants(resId, ctx);
            LOGGER.debug("Obtained {} grants.", (grants != null) ? grants.size() : 0);

            GrantSet grantSet = null;
            if (grants != null && !grants.isEmpty()) {
                //get grantSet from any grant...let's take the first.
                grantSet = grants.get(0).getGrants();
            }
            if (grantSet != null) {
                LOGGER.debug("Obtained grantset with id {}", grantSet.getId());
            } else {
                LOGGER.warn("Obtained no grantset. Returning empty result.");
            }

            // return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new GrantSetWrapper(grantSet));
            return new GrantSetWrapper(grantSet);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain grantset for resource " + resId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while obtaining ResourceReferences. Probably, the resource " + resId + " was not registered before.", ex);
            throw new WebApplicationException(404);
        } 
    }

    @Override
    public IEntityWrapper<? extends IDefaultGrant> getGrantById(Long pId, String pGroupId, HttpContext hc) {
        //@TODO Hack using EM directly...other way?
        EntityManager em = PU.entityManager();
        try {
            LOGGER.debug("Finding grant for id " + pId);
            Grant grant = FindUtil.findGrant(em, pId);
            // return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new GrantWrapper(grant));
            return new GrantWrapper(grant);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while obtaining Grant for id " + pId + ". Probably, there is no grant for this id.", ex);
            throw new WebApplicationException(404);
        } finally {
            em.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultGrant> createGrant(String pDomain, String pDomainUniqueId, final String pUserId, String pGroupId, String pRole, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        try {
            SecurableResourceId resId = factoryResourceId(pDomain, pDomainUniqueId);
            LOGGER.debug("Checking whether grants are allowed.");
            if (!ResourceServiceLocal.getSingleton().grantsAllowed(resId, ctx)) {
                LOGGER.debug("Grants are not allowed. Enabling grants with max. role MANAGER.");
                //allow grants with max. role MANAGER.
                //@TODO check whether this should be solved in a configurable way.
                ResourceServiceLocal.getSingleton().allowGrants(resId, Role.MANAGER, ctx);
                LOGGER.debug("Grants are now allowed.");
            } else {
                LOGGER.debug("Grants are already allowed.");
            }

            LOGGER.debug("Adding grant for user {} on resource {}", resId, pUserId);
            //add grant
            ResourceServiceLocal.getSingleton().addGrant(resId, new UserId(pUserId), Role.valueOf(pRole), ctx);
            LOGGER.debug("Obtaining grant information for resource {} from database.", resId);
            //get all grants as addGrant does not return anything.
            List<Grant> grants = ResourceServiceLocal.getSingleton().getGrants(resId, ctx);
            LOGGER.debug("Searching for grantee {}.", pUserId);
            //search for appropriate grant
            Grant grant = (Grant) CollectionUtils.find(grants, new Predicate() {

                @Override
                public boolean evaluate(Object o) {
                    return ((Grant) o).getGrantee().getUserId().equals(pUserId);
                }
            });
            if (grant != null) {
                LOGGER.debug("Grant information successfully obtained. Returning result.");
            } else {
                LOGGER.warn("No grant information obtained. Returning empty result.");
            }
            //return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new GrantWrapper(grant));
            return new GrantWrapper(grant);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to create grant", ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while adding Grant. Probably, the resource " + new SecurableResourceId(pDomain, pDomainUniqueId) + " was not registered before.", ex);
            throw new WebApplicationException(404);
        } catch (EntityAlreadyExistsException ex) {
            LOGGER.error("EntityAlreadyExistsException caught while adding Grant. Probably, there is already a grant for resource " + new SecurableResourceId(pDomain, pDomainUniqueId) + " and user " + pUserId + ".", ex);
            throw new WebApplicationException(409);
        }
    }

    @Override
    public Response revokeGrant(Long pId, String pGroupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        //Hack using EM directly...other way?
        EntityManager em = PU.entityManager();
        try {
            LOGGER.debug("Finding grant for id " + pId);
            Grant grant = FindUtil.findGrant(em, pId);
            LOGGER.debug("Revoking grant for id " + pId);
            ResourceServiceLocal.getSingleton().revokeGrant(grant.getGrants().getResource().getSecurableResourceId(), new UserId(grant.getGrantee().getUserId()), ctx);
            return Response.ok().build();
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to revoke grant with id " + pId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while obtaining Grant for id " + pId + ". Probably, there is no grant for this id.", ex);
            throw new WebApplicationException(404);
        } finally {
            em.close();
        }
    }

    @Override
    public Response revokeAllGrants(String pDomain, String pDomainUniqueId, String pGroupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        SecurableResourceId resId = factoryResourceId(pDomain, pDomainUniqueId);
        
        try {
            ResourceServiceLocal.getSingleton().revokeAllAndDisallowGrants(resId, ctx);
            return Response.ok().build();
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to revoke all grants for resource " + resId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while revoking all grants. Probably, the resource " + new SecurableResourceId(pDomain, pDomainUniqueId) + " was not registered before.", ex);
            throw new WebApplicationException(404);
        }

    }

    @Override
    public IEntityWrapper<? extends IDefaultGrant> updateGrant(Long pId, String pGroupId, String pRole, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        //Hack using EM directly...other way?
        EntityManager em = PU.entityManager();
        try {
            LOGGER.debug("Finding grant for id " + pId);
            Grant grant = FindUtil.findGrant(em, pId);
            LOGGER.debug("Updating grant for id " + pId);
            Role newRole = Role.valueOf(pRole);
            ResourceServiceLocal.getSingleton().changeGrant(grant.getGrants().getResource().getSecurableResourceId(), new UserId(grant.getGrantee().getUserId()), newRole, ctx);
            grant.setGrantedRole(newRole);
            return new GrantWrapper(grant);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to update grant with id " + pId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while obtaining Grant for id " + pId + ". Probably, there is no grant for this id.", ex);
            throw new WebApplicationException(404);
        } finally {
            em.close();
        }
    }

    @Override
    public IEntityWrapper<? extends ISimpleUserId> getAuthorizedUsers(String pDomain, String pDomainUniqueId, String pRole, String pGroupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        SecurableResourceId resId = factoryResourceId(pDomain, pDomainUniqueId);
        try {
            return new UserIdWrapper(ResourceServiceLocal.getSingleton().getAuthorizedUsers(resId, Role.valueOf(pRole), ctx));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get a list of authorized users for resource " + resId, ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("EntityNotFoundException caught while obtaining authorized users. Probably, the resource " + new SecurableResourceId(pDomain, pDomainUniqueId) + " was not registered before.", ex);
            throw new WebApplicationException(404);
        }
    }

    @Override
    public Response checkService() {
        ServiceStatus status = ServiceStatus.UNKNOWN;
        return Response.status(200).entity(new CheckServiceResponse("Sharing", status)).build();
    }

    /**
     * Internal helper to create a ReferenceId for pDomain, pDomainUniqueId and
     * pGroupId.
     *
     * @param pDomain The resource domain.
     * @param pDomainUniqueId The domain unique id.
     * @param pGroupId The group id.
     *
     * @return The ReferenceId.
     */
    private ReferenceId factoryReferenceId(String pDomain, String pDomainUniqueId, String pGroupId) {
        SecurableResourceId resId = factoryResourceId(pDomain, pDomainUniqueId);
        String id = pGroupId;
        if (id == null) {
            LOGGER.info("Provided groupId is null. Using default group {} instead.", Constants.USERS_GROUP_ID);
            id = Constants.USERS_GROUP_ID;
        }

        return new ReferenceId(resId, new GroupId(id));
    }

    /**
     * Internal helper to create a SecurableResourceId for pDomain and
     * pDomainUniqueId.
     *
     * @param pDomain The resource domain.
     * @param pDomainUniqueId The domain unique id.
     *
     * @return The SecurableResourceId.
     */
    private SecurableResourceId factoryResourceId(String pDomain, String pDomainUniqueId) {
        if (pDomain == null || pDomainUniqueId == null) {
            LOGGER.error("Arguments pDomain and pDomainUniqueId must not be null. Returning HTTP-BAD REQUEST (400)");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        return new SecurableResourceId(pDomain, pDomainUniqueId);
    }

}
