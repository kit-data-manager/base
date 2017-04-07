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
package edu.kit.dama.authorization.services.administration.impl;

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredArgument;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.FilterHelper;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.entities.impl.GrantSet;
import edu.kit.dama.authorization.entities.impl.Group;
import edu.kit.dama.authorization.entities.impl.ResourceReference;
import edu.kit.dama.authorization.entities.impl.SecurableResource;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.IResourceService;
import edu.kit.dama.authorization.entities.util.PU;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pasic
 */
public class ResourceServiceImpl implements IResourceService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ResourceServiceImpl.class);
    private final static String FINDING_RESOUCE_STRING = " - Finding resource";
    private final static String FINDING_RESOUCE_REFERENCE_STRING = " - Finding resource reference";
    private final static String GRANTS_NOT_ALLOWED_STRING = "Grants are not allowed for this resource";
    private final static String COULD_NOT_FIND_RESOURCE_STRING = "Could not find the resource: ";
    private static final String USER_ID_COLUMN = "userId";
    private static final String GROUP_ID_COLUMN = "groupId";
    private static final String DOMAIN_ID_COLUMN = "domainId";
    private static final String DOMAIN_UNIQUE_ID_COLUMN = "domainUniqueId";

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void registerResource(SecurableResourceId resourceId, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityAlreadyExistsException {
        LOGGER.debug("Registering securable resource with resource id {}", resourceId);
        EntityManager em = PU.entityManager();
        em.getTransaction().begin();
        try {
            boolean alreadyExists = false;
            try {
                LOGGER.debug(" - Trying to find existing resource with id " + resourceId);
                FindUtil.findResource(em, resourceId);
                LOGGER.debug(" - Resource already exists");
                alreadyExists = true;
            } catch (EntityNotFoundException ex) {
                //resource does not exist
            }
            if (!alreadyExists) {
                LOGGER.debug(" - Registering new securable resource");
                SecurableResource resource = new SecurableResource(resourceId);
                em.persist(resource);
            }
            em.getTransaction().commit();
            em.close();
            if (alreadyExists) {
                throw new EntityAlreadyExistsException("The SecurableResourceId "
                        + resourceId.toString() + " is already registered.");
            }
            LOGGER.debug("Resource with id {} successfully registered.", resourceId);
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to register securable resource with id " + resourceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void registerResource(SecurableResourceId resourceId, GroupId ownerGroup, Role roleRestriction, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Registering securable resource with resource id {} for group {} and role {}", new Object[]{resourceId, ownerGroup, roleRestriction});
        EntityManager em = PU.entityManager();
        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            LOGGER.debug(" - Finding group");
            Group group = FindUtil.findGroupQuick(em, ownerGroup);
            SecurableResource resource = new SecurableResource(resourceId);
            ResourceReference reference = new ResourceReference();
            reference.setResource(resource);
            LOGGER.debug(" - Assigning resource to group {}", group);
            reference.setGroup(group);
            LOGGER.debug(" - Setting resource role restriction to {}", roleRestriction);
            reference.setRoleRestriction(roleRestriction);
            LOGGER.debug(" - Persisting resource {}", resource);
            em.persist(resource);
            LOGGER.debug(" - Persisting reference {}", reference);
            em.persist(reference);
            em.flush();
            LOGGER.debug(" - Adding resource reference to resource and group entities");
            resource.getResourceReferences().add(reference);
            group.getResourceReferences().add(reference);
            transaction.commit();
            em.close();
            LOGGER.debug("Securable resource with resource id {} successully added to group {} and role {}.", new Object[]{resourceId, ownerGroup, roleRestriction});
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to register securable resource with id " + resourceId + " for group " + ownerGroup + " with role " + roleRestriction, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void registerResource(SecurableResourceId resourceId, Role grantRole, UserId userId, Role grantSetRole, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Granting access to securable resource with resource id {} for user {} and role {}.", new Object[]{resourceId, userId, grantRole});
        EntityManager em = PU.entityManager();
        try {
            boolean creationSuccessful = false;
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            try {
                LOGGER.debug(" - Creating securable resource");
                SecurableResource resource = new SecurableResource(resourceId);
                em.persist(resource);
                em.flush();
                transaction.commit();
                em.close();
                LOGGER.debug(" - Securable resource successfully created.");
                creationSuccessful = true;
            } catch (PersistenceException except) {
                PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
                throw new PersistenceException("Failed to grant access to securable resource with id " + resourceId + " for user " + userId + " with role " + grantRole, except);
            }
            if (creationSuccessful) {
                try {
                    ResourceServiceImpl rs = new ResourceServiceImpl();
                    LOGGER.debug(" - Allow grant for resource {} and role {}", new Object[]{resourceId, grantRole});
                    rs.allowGrants(resourceId, grantSetRole, authCtx);
                    LOGGER.debug(" - Adding grant for resource {} and user {} with role {}", new Object[]{resourceId, userId, grantRole});
                    rs.addGrant(resourceId, userId, grantRole, authCtx);
                    LOGGER.debug("Access to securable resource with resource id {} successully granted for user {} with role {}.", new Object[]{resourceId, userId, grantRole});
                } catch (EntityAlreadyExistsException ex) {
                    LOGGER.warn("Grant not added. Entity already exists.", ex);
                }
            } else {
                LOGGER.warn("Securable resource not created, skipping grant assignment.");
            }
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void remove(@SecuredArgument SecurableResourceId resourceId, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Removing securable resource with resource id {} ", resourceId);
        EntityManager em = PU.entityManager();

        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            LOGGER.debug(" - Finding securable resource");
            SecurableResource resource = FindUtil.findResource(em, resourceId);
            LOGGER.debug(" - Removing {} resource references", resource.getResourceReferences().size());
            for (ResourceReference rr : resource.getResourceReferences()) {
                em.remove(rr);
            }
            LOGGER.debug(" - Removing resource");
            em.remove(resource);

            transaction.commit();
            em.close();
            LOGGER.debug("Securable resource with resource id {} successfully removed.", resourceId);
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to remove securable resource with id " + resourceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void createReference(@SecuredArgument ReferenceId referenceId, Role roleRestriction, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException, EntityAlreadyExistsException {
        LOGGER.debug("Creating resource reference with resource reference id {} and role {}", new Object[]{referenceId, roleRestriction});
        EntityManager em = PU.entityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        boolean alreadyExists = true;
        try {
            try {
                LOGGER.debug(FINDING_RESOUCE_REFERENCE_STRING);
                if (FindUtil.findResourceReference(em, referenceId) != null) {
                    LOGGER.warn("Resource reference already exists.");
                }
            } catch (EntityNotFoundException e) {
                //OK we dont't have a duplication
                alreadyExists = false;
            }
            if (!alreadyExists) {
                LOGGER.debug(" - Trying to create resource reference");
                LOGGER.debug(FINDING_RESOUCE_STRING);
                SecurableResource resource = FindUtil.findResource(em, referenceId.getResourceId());
                LOGGER.debug(" - Finding group");
                Group group = FindUtil.findGroupQuick(em, referenceId.getGroupId());
                LOGGER.debug(" - Creating resource reference with role restriction {}", roleRestriction);
                ResourceReference resourceReference = new ResourceReference(roleRestriction, resource, group);
                LOGGER.debug(" - Persisting resource reference");
                em.persist(resourceReference);
                LOGGER.debug(" - Adding resource reference to group");
                group.getResourceReferences().add(resourceReference);
            }
            transaction.commit();
            if (alreadyExists) {
                throw new EntityAlreadyExistsException("There is already a reference with referenceId '" + referenceId + "'");
            } else {
                LOGGER.debug("Resource reference for reference id {} and role {} successfully created.", new Object[]{referenceId, roleRestriction});
            }
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to create resource reference with reference id " + referenceId + " and role " + roleRestriction, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void deleteReference(@SecuredArgument ReferenceId referenceId, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Deleting resource reference for resource reference id {}", referenceId);
        EntityManager em = PU.entityManager();

        try {
            em.getTransaction().begin();
            LOGGER.debug(FINDING_RESOUCE_REFERENCE_STRING);
            ResourceReference reference = FindUtil.findResourceReferenceQuick(em, referenceId);
            Group group = reference.getGroup();
            LOGGER.debug(" - Removing reference");
            em.remove(reference);
            LOGGER.debug(" - Removing reference from group");
            group.getResourceReferences().remove(reference);
            em.getTransaction().commit();
            LOGGER.debug("Resource reference for reference id {} successfully removed.", referenceId);
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to remove resource reference with reference id " + referenceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void changeReferenceRestriction(@SecuredArgument ReferenceId referenceId, Role newRoleRestriction, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Changing resource reference restriction for resource reference id {} to role {}", new Object[]{referenceId, newRoleRestriction});
        EntityManager em = PU.entityManager();

        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            LOGGER.debug(FINDING_RESOUCE_REFERENCE_STRING);
            ResourceReference reference = FindUtil.findResourceReferenceQuick(em, referenceId);
            LOGGER.debug(" - Setting new role restriction");
            reference.setRoleRestriction(newRoleRestriction);
            LOGGER.debug(" - Merging resource reference to database");
            em.merge(reference);

            transaction.commit();
            em.close();
            LOGGER.debug("Resource reference restriction for reference id {} successfully changed to role {}.", new Object[]{referenceId, newRoleRestriction});
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to change resource reference restriction for reference id " + referenceId + " to role " + newRoleRestriction, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MEMBER)
    public final IRoleRestriction getReferenceRestriction(@SecuredArgument ReferenceId referenceId, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Getting resource reference restriction for resource reference id {}", referenceId);
        EntityManager em = PU.entityManager();
        try {
            LOGGER.debug(FINDING_RESOUCE_REFERENCE_STRING);
            ResourceReference reference = FindUtil.findResourceReferenceQuick(em, referenceId);
            LOGGER.debug("Returning role restriction {}", reference.getRoleRestriction());
            return reference.getRoleRestriction();
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to get reference restriction for reference id  " + referenceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final List<ReferenceId> getReferences(@SecuredArgument SecurableResourceId resourceId, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException {
        LOGGER.debug("Getting resource references for resource id {}", resourceId);
        EntityManager em = PU.entityManager();
        try {
            LOGGER.debug(" - Building query");
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<ResourceReference> cq = cb.createQuery(ResourceReference.class);
            Root<ResourceReference> resourceReferences = cq.from(ResourceReference.class);
            Join<ResourceReference, SecurableResource> resource = resourceReferences.
                    <ResourceReference, SecurableResource>join("resource");
            resourceReferences.<Group, ResourceReference>join("group");
            cq.select(resourceReferences).where(
                    cb.and(
                            cb.equal(resource.get(DOMAIN_ID_COLUMN),
                                    resourceId.getDomain())),
                    cb.equal(resource.get(DOMAIN_UNIQUE_ID_COLUMN),
                            resourceId.getDomainUniqueId()));
            LOGGER.debug(" - Executing query");
            TypedQuery<ResourceReference> tq = em.createQuery(cq);
            tq.setHint("javax.persistence.fetchgraph", em.getEntityGraph("ResourceReference.simple"));

            List<ResourceReference> resultList = tq.getResultList();
            List<ReferenceId> referenceIds = new ArrayList<>(resultList.size());
            LOGGER.debug(" - Building result list for {} elements", resultList.size());
            for (ResourceReference rr : resultList) {
                referenceIds.add(new ReferenceId(rr.getResource().getSecurableResourceId(), new GroupId(rr.getGroup().getGroupId())));
            }
            LOGGER.debug("{} references successfully obtained", referenceIds.size());
            return referenceIds;
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to get references for resource id  " + resourceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void allowGrants(@SecuredArgument SecurableResourceId resourceId, Role restriction, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Granting access to resource with resource id {} to role {}", new Object[]{resourceId, restriction});
        EntityManager em = PU.entityManager();

        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            LOGGER.debug(FINDING_RESOUCE_STRING);
            SecurableResource resource = FindUtil.findResourceQuick(em, resourceId);
            if (resource.getGrantSet() != null) {
                LOGGER.debug("Grant set found. Grants are already allowed for resource {}.", resourceId);
                if (!resource.getGrantSet().getRoleRestriction().equals(restriction)) {
                    LOGGER.debug("Updating role restriction from {} to {}.", resource.getGrantSet().getRoleRestriction(), restriction);
                    resource.getGrantSet().setRoleRestriction(restriction);
                }
            } else {
                LOGGER.debug(" - Creating grant set");
                GrantSet grantSet = new GrantSet(resource, restriction);
                em.persist(grantSet);
                LOGGER.debug(" - Assigning grant set to resource");
                resource.setGrantSet(grantSet);
            }
            transaction.commit();
            LOGGER.debug("Access to resource with resource id {} successfully granted to role {}", new Object[]{resourceId, restriction});
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to grant access to resource id " + resourceId + " for role " + resourceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final boolean grantsAllowed(@SecuredArgument SecurableResourceId resourceId, @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Checking for grants for resource with id {}", resourceId);
        EntityManager em = PU.entityManager();
        try {
            SecurableResource resource = FindUtil.findResourceQuick(em, resourceId);
            LOGGER.debug("Grants {}", (resource.getGrantSet() != null) ? "found" : "not found");
            return resource.getGrantSet() != null;
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to get grants for resource id " + resourceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void addGrant(@SecuredArgument SecurableResourceId resourceId, UserId userId,
            Role role,
            @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException, EntityAlreadyExistsException {
        LOGGER.debug("Adding grant for resource with id {} to user {} and role {}", new Object[]{resourceId, userId, role});
        EntityManager em = PU.entityManager();

        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            long s = System.currentTimeMillis();
            LOGGER.debug(" - Finding user");
            User user = FindUtil.findUserQuick(em, userId);
            LOGGER.debug(FINDING_RESOUCE_STRING);
            SecurableResource resource = FindUtil.findResourceQuick(em, resourceId);
            LOGGER.debug(" - Finding grant set");
            GrantSet grantSet = resource.getGrantSet();
            if (null == grantSet) {
                LOGGER.warn("No grant set found for resource {}", resourceId);
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw new UnsupportedOperationException(GRANTS_NOT_ALLOWED_STRING);
            }
            List<Grant> grants = grantSet.getGrants();
            LOGGER.debug(" - Checking {} existing grants", grants.size());
            boolean exists = false;
            for (Grant g : grants) {
                if (g.getGrantee().getUserId().equals(userId.getStringRepresentation())) {
                    LOGGER.info("Grant for user {} already exists.", userId);
                    if (!g.getGrantedRole().equals(role)) {
                        g.setGrantedRole(role);
                    }
                }
            }

            if (!exists) {
                LOGGER.debug("- Creating new grant for user {} and role {}", new Object[]{user, role});
                Grant grant = new Grant(user, role, grantSet);
                LOGGER.debug("Adding grant to grant set");
                grants.add(grant);
                LOGGER.debug(" - Merging grant set to database");
                em.merge(grantSet);
                LOGGER.debug(" - Persisting grant");
                em.persist(grant);
            }
            transaction.commit();
            LOGGER.debug("Grant successfully added for resource with id {} to user {} and role {}", new Object[]{resourceId, userId, role});
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to add grant for resource with id " + resourceId + " to user " + userId + " and role " + role, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void changeGrant(@SecuredArgument SecurableResourceId resourceId, UserId userId,
            Role role,
            @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Changing grant for resource with id {} to user {} and role {}", new Object[]{resourceId, userId, role});
        EntityManager em = PU.entityManager();

        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            LOGGER.debug(" - Finding resoure");
            SecurableResource resource = FindUtil.findResource(em, resourceId);
            GrantSet grantSet = resource.getGrantSet();
            LOGGER.debug(" - Checking {} grants", grantSet.getGrants().size());
            boolean changed = false;
            for (Grant grant : grantSet.getGrants()) {
                if (grant.getGrantee().getUserId().equals(userId.getStringRepresentation())) {
                    LOGGER.debug(" - Changing grant from role {} to role {}", new Object[]{grant.getGrantedRole(), role});
                    grant.setGrantedRole(role);
                    LOGGER.debug(" - Merging grant to database");
                    em.merge(grant);
                    changed = true;
                    break;
                }
            }

            transaction.commit();
            if (changed) {
                LOGGER.debug("Grant successfully changed for resource with id {} for user {} to role {}", new Object[]{resourceId, userId, role});
            } else {
                LOGGER.info("Nothing changed for resource {} and user {}. Probably, user has no grant assigned.", new Object[]{resourceId, userId, role});
            }
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to change grant for resource with id " + resourceId + " for user " + userId + " to role " + role, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void revokeGrant(@SecuredArgument SecurableResourceId resourceId, UserId userId,
            @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Revoking grant for resource with id {} and user {}", new Object[]{resourceId, userId});
        EntityManager em = PU.entityManager();

        try {
            EntityTransaction transaction = em.getTransaction();
            transaction.begin();
            LOGGER.debug(FINDING_RESOUCE_STRING);
            SecurableResource resource = FindUtil.findResource(em, resourceId);
            GrantSet grantSet = resource.getGrantSet();
            LOGGER.debug(" - Searching grant for user");
            Iterator<Grant> grantIterator = grantSet.getGrants().iterator();
            while (grantIterator.hasNext()) {
                Grant grant = grantIterator.next();
                if (grant.getGrantee().getUserId().equals(userId.getStringRepresentation())) {
                    LOGGER.debug(" - Removing grant");
                    //User user = grant.getGrantee();
                    em.remove(grant);
                    grantIterator.remove();
                    LOGGER.debug(" - Merging grant to database");
                    em.merge(grantSet);
                    break;
                }
            }

            transaction.commit();
            LOGGER.debug("Grant for resource with id {} successfully revoked for user {}", new Object[]{resourceId, userId});
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to revoke grant for resource with id " + resourceId + " for user " + userId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final void revokeAllAndDisallowGrants(@SecuredArgument SecurableResourceId resourceId,
            @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Revoking all grant for resource with id {}", resourceId);
        EntityManager em = PU.entityManager();

        try {
            em.getTransaction().begin();
            LOGGER.debug(FINDING_RESOUCE_STRING);
            SecurableResource resource = FindUtil.findResourceQuick(em, resourceId);
            GrantSet grantSet = resource.getGrantSet();
            LOGGER.debug(" - Resetting grant set");
            if (grantSet != null) {
                resource.setGrantSet(null);
                em.flush();
                LOGGER.debug(" - Removing {} grants", grantSet.getGrants());
                for (Grant grant : grantSet.getGrants()) {
                    em.remove(grant);
                }
                LOGGER.debug(" - Clearing grants");
                grantSet.getGrants().clear();
                LOGGER.debug(" - Merging grant set to database");
                em.merge(grantSet);

                LOGGER.debug(" - Removing grant set");
                em.remove(grantSet);
            }
            em.getTransaction().commit();
            LOGGER.debug("Successfully revoked all grant for resource with id {}.", resourceId);
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to revoke all grant for resource with id " + resourceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final Role getGrantRole(@SecuredArgument SecurableResourceId resourceId, UserId userId,
            @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Getting grant role for resource with id {} and user {}", new Object[]{resourceId, userId});
        EntityManager em = PU.entityManager();
        try {
            LOGGER.debug(FINDING_RESOUCE_STRING);
            SecurableResource resource = FindUtil.findResource(em, resourceId);
            if (null == resource) {
                throw new EntityNotFoundException(COULD_NOT_FIND_RESOURCE_STRING + resourceId.toString());
            }
            LOGGER.debug(" - Obtaining grant set");
            GrantSet grantSet = resource.getGrantSet();
            if (null == grantSet) {
                throw new UnsupportedOperationException(GRANTS_NOT_ALLOWED_STRING);
            }
            LOGGER.debug(" - Finding user grant");
            for (Grant grant : grantSet.getGrants()) {
                if (grant.getGrantee().getUserId().equals(userId.getStringRepresentation())) {
                    LOGGER.debug("Returning grant role {}", grant.getGrantedRole());
                    return grant.getGrantedRole();
                }
            }
            LOGGER.debug("No grant found, returning Role.NO_ACCESS");
            return Role.NO_ACCESS;
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to obtain grant role for resource id " + resourceId + " and user with id " + userId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final List<Grant> getGrants(@SecuredArgument SecurableResourceId resourceId,
            @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Getting grants resource with id {}", resourceId);
        EntityManager em = PU.entityManager();
        try {
            LOGGER.debug(FINDING_RESOUCE_STRING);
            SecurableResource resource = FindUtil.findResource(em, resourceId);
            if (null == resource) {
                throw new EntityNotFoundException(COULD_NOT_FIND_RESOURCE_STRING + resourceId.toString());
            }
            LOGGER.debug(" - Getting grant set");
            GrantSet grantSet = resource.getGrantSet();
            if (null == grantSet) {
                LOGGER.debug("No grant set found for resource with id {}. Returning empty grant list.", resourceId);
                return new ArrayList<>();
            } else {
                LOGGER.debug("Returning {} grants", grantSet.getGrants().size());
                return grantSet.getGrants();
            }
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed get grants for resource id " + resourceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final List<UserId> getAuthorizedUsers(@SecuredArgument SecurableResourceId resourceId, Role minimumRole,
            @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException {
        LOGGER.debug("Getting authorized users for resource with id {} with minimum role {}", new Object[]{resourceId, minimumRole});
        EntityManager em = PU.entityManager();
        try {
            LOGGER.debug(" - Building query");
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<String> q = cb.createQuery(String.class);
            Root<FilterHelper> record = q.from(FilterHelper.class);
            q.where(
                    cb.and(
                            cb.equal(
                                    record.get(DOMAIN_UNIQUE_ID_COLUMN),
                                    resourceId.getDomainUniqueId()),
                            cb.equal(
                                    record.get(DOMAIN_ID_COLUMN),
                                    resourceId.getDomain()),
                            cb.ge(record.<Integer>get("roleAllowed"),
                                    minimumRole.ordinal())));
            q.select(record.<String>get(USER_ID_COLUMN));
            q.distinct(true);
            LOGGER.debug(" - Executing query");
            TypedQuery<String> tq = em.createQuery(q);
            List<String> resultList = tq.getResultList();
            List<UserId> users = new ArrayList<>(resultList.size());
            LOGGER.debug(" - Building result list for {} elements", resultList.size());
            for (String userIdString : resultList) {
                users.add(new UserId(userIdString));
            }

            LOGGER.debug("{} users successfully obtained", users.size());
            return users;
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed get authorized users for resource " + resourceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    @SecuredMethod(roleRequired = Role.MANAGER)
    public final List<GroupId> getAuthorizedGroups(@SecuredArgument SecurableResourceId resourceId, Role role,
            @Context IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException {
        LOGGER.debug("Getting authorized groups for resource with id {} with role {}", new Object[]{resourceId, role});
        EntityManager em = PU.entityManager();
        LOGGER.debug(" - Building query");
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> q = cb.createQuery(String.class);
        Root<FilterHelper> record = q.from(FilterHelper.class);
        q.where(
                cb.and(
                        cb.equal(
                                record.get(DOMAIN_UNIQUE_ID_COLUMN),
                                resourceId.getDomainUniqueId()),
                        cb.equal(
                                record.get(DOMAIN_ID_COLUMN),
                                resourceId.getDomain()),
                        cb.ge(record.<Integer>get("roleAllowed"),
                                role.ordinal())));
        q.select(record.<String>get(GROUP_ID_COLUMN));
        q.distinct(true);
        LOGGER.debug(" - Executing query");
        TypedQuery<String> tq = em.createQuery(q);
        List<String> resultList = tq.getResultList();
        List<GroupId> groups = new ArrayList<>(resultList.size());

        /* LOGGER.debug("Obtaining reference ids for resource {}", resourceId);
        List<ReferenceId> referenceIds = getReferences(resourceId, authCtx);
        LOGGER.debug("Obtained {} referende ids. Extracting groups.", referenceIds.size());
        List<GroupId> groups = new ArrayList<>(referenceIds.size());
        for (ReferenceId referenceId : referenceIds) {
            LOGGER.debug("Adding group {}  to result list.", referenceId.getGroupId().getStringRepresentation());
            groups.add(referenceId.getGroupId());
        }*/
        LOGGER.debug(" - Building result list for {} elements", resultList.size());
        for (String groupIdString : resultList) {
            groups.add(new GroupId(groupIdString));
        }
        em.close();
        LOGGER.debug("{} groups successfully obtained", groups.size());
        return groups;
    }

    @Override
    public final Role getGrantsRestriction(@SecuredArgument SecurableResourceId resourceId, IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Getting grant restriction for resource id {}", resourceId);
        EntityManager em = PU.entityManager();
        try {
            LOGGER.debug(FINDING_RESOUCE_STRING);
            SecurableResource resource = FindUtil.findResource(em, resourceId);
            if (null == resource) {
                throw new EntityNotFoundException(COULD_NOT_FIND_RESOURCE_STRING + resourceId.toString());
            }
            GrantSet grantSet = resource.getGrantSet();
            if (null == grantSet) {
                throw new UnsupportedOperationException(GRANTS_NOT_ALLOWED_STRING);
            }
            LOGGER.debug("Returning grant restriction {}", resource.getGrantSet().getRoleRestriction());
            return grantSet.getRoleRestriction();
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed get grant restriction for resource " + resourceId, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }

    @Override
    public final void changeGrantsRestriction(@SecuredArgument SecurableResourceId resourceId, Role restriction,
            IAuthorizationContext authCtx)
            throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        LOGGER.debug("Changing grant restriction for resource id {} to role {}", new Object[]{resourceId, restriction});

        EntityManager em = PU.entityManager();

        try {
            em.getTransaction().begin();

            LOGGER.debug(FINDING_RESOUCE_STRING);
            SecurableResource resource = FindUtil.findResource(em, resourceId);
            if (null == resource) {
                throw new EntityNotFoundException(COULD_NOT_FIND_RESOURCE_STRING + resourceId.toString());
            }
            LOGGER.debug(" - Obtaining grant set");
            GrantSet grantSet = resource.getGrantSet();
            if (null == grantSet) {
                throw new UnsupportedOperationException(GRANTS_NOT_ALLOWED_STRING);
            }
            LOGGER.debug(" - Changing grant restriction from {} to {}", new Object[]{grantSet.getRoleRestriction(), restriction});
            grantSet.setRoleRestriction(restriction);
            em.getTransaction().commit();
            LOGGER.debug("Grantrestriction for resource id {} successfully changed to role {}", new Object[]{resourceId, restriction});
        } catch (PersistenceException except) {
            PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
            throw new PersistenceException("Failed to change grant restriction for resource with id " + resourceId + " to role " + restriction, except);
        } finally {
            try {
                em.close();
            } catch (IllegalStateException ex) {
            }
        }
    }
}
