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
package edu.kit.dama.authorization.entities.util;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.entities.impl.Group;
import edu.kit.dama.authorization.entities.impl.Membership;
import edu.kit.dama.authorization.entities.impl.ResourceReference;
import edu.kit.dama.authorization.entities.impl.SecurableResource;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

/**
 *
 * @author pasic
 */
public final class FindUtil {

  /**
   * Hidden constructor.
   */
  private FindUtil() {
  }

  /**
   * Find all users.
   *
   * @param entityManager The entity manager to use to query.
   *
   * @return The list of users.
   */
  public static List<User> findAllUsers(EntityManager entityManager) {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> q = cb.createQuery(User.class);
    Root<User> user = q.from(User.class);
    q.where(cb.notEqual(user.get("id"), 0));
    TypedQuery<User> tq = entityManager.createQuery(q);
    return tq.getResultList();
  }

  /**
   * Find a single user by its user id.
   *
   * @param entityManager The entity manager to use to query.
   * @param id The user id of the user to find.
   *
   * @return The found user.
   *
   * @throws EntityNotFoundException If no user was found for user id 'id'.
   */
  public static User findUser(EntityManager entityManager, UserId id) throws EntityNotFoundException {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<User> q = cb.createQuery(User.class);
    Root<User> user = q.from(User.class);
    q.where(cb.equal(user.get("userId"), id.getStringRepresentation()));
    TypedQuery<User> tq = entityManager.createQuery(q);
    User singleResult;
    try {
      singleResult = tq.getSingleResult();
    } catch (NonUniqueResultException e) {
      throw new EntityNotFoundException("Non unique entry found for UserId '" + id.getStringRepresentation() + "'", e);
    } catch (NoResultException e) {
      throw new EntityNotFoundException("User for UserId '" + id.getStringRepresentation() + "' does not exist.", e);
    }
    return singleResult;
  }

  /**
   * Find a single group by its group id.
   *
   * @param entityManager The entity manager to use to query.
   * @param id The group id of the group to find.
   *
   * @return The found group.
   *
   * @throws EntityNotFoundException If no group was found for group id 'id'.
   */
  public static Group findGroup(EntityManager entityManager, GroupId id) throws EntityNotFoundException {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Group> q = cb.createQuery(Group.class);
    Root<Group> group = q.from(Group.class);
    q.where(cb.equal(group.get("groupId"), id.getStringRepresentation()));
    TypedQuery<Group> tq = entityManager.createQuery(q);
    Group singleResult;
    try {
      singleResult = tq.getSingleResult();
    } catch (NonUniqueResultException e) {
      throw new EntityNotFoundException("Non unique entry found for GroupId '" + id.getStringRepresentation() + "'", e);
    } catch (NoResultException e) {
      throw new EntityNotFoundException("Group for GroupId '" + id.getStringRepresentation() + "' does not exist.", e);
    }

    return singleResult;
  }

  /**
   * Find a single membership by group and user id.
   *
   * @param entityManager The entity manager to use to query.
   * @param groupId The group id of the membership to find.
   * @param userId The user id of the membership to find.
   *
   * @return The found membership.
   *
   * @throws EntityNotFoundException If no membership was found for group id
   * 'groupId' and user id 'userId'.
   */
  public static Membership findMembership(EntityManager entityManager, GroupId groupId, UserId userId) throws EntityNotFoundException {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Membership> q = cb.createQuery(Membership.class);
    Root<Membership> membership = q.from(Membership.class);
    Join<Membership, Group> group = membership.<Membership, Group>join("group");
    Join<Membership, User> user = membership.<Membership, User>join("user");
    q.where(cb.and(
            cb.equal(group.get("groupId"), groupId.getStringRepresentation()),
            cb.equal(user.get("userId"), userId.getStringRepresentation())));
    Membership result;
    try {
      result = entityManager.createQuery(q).getSingleResult();
    } catch (NonUniqueResultException e) {
      throw new EntityNotFoundException("Non unique entry found for Membersihip with UserId '" + userId.getStringRepresentation()
              + "' and GroupId '" + groupId.getStringRepresentation() + "'", e);
    } catch (NoResultException e) {
      throw new EntityNotFoundException("Membership for UserId '" + userId.getStringRepresentation()
              + "' and GroupId '" + groupId.getStringRepresentation() + "' not available.", e);
    }
    return result;

  }

  /**
   * Find a securable resource by its resource id.
   *
   * @param entityManager The entity manager to use to query.
   * @param resourceId The resource id of the resource to find.
   *
   * @return The found resource.
   *
   * @throws EntityNotFoundException If no resource was found for resource id
   * 'resourceId'.
   */
  public static SecurableResource findResource(EntityManager entityManager, SecurableResourceId resourceId) throws EntityNotFoundException {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<SecurableResource> cq = entityManager.getCriteriaBuilder().createQuery(SecurableResource.class);
    Root<SecurableResource> resource = cq.from(SecurableResource.class);
    cq.select(resource).where(
            cb.and(
                    cb.equal(resource.get("domainId"), resourceId.getDomain()),
                    cb.equal(resource.get("domainUniqueId"), resourceId.getDomainUniqueId())));
    TypedQuery<SecurableResource> q = entityManager.createQuery(cq);
    return obtainUniqueResource(resourceId, q);
  }

  /**
   * Obtain a unique resource and produce errors if multiple resources were
   * found.
   *
   * @param <C> Generic type.
   * @param pReference The resource if for logging purposes.
   * @param pQuery The query to obtain the resource.
   *
   * @return The securable resource/resource reference.
   *
   * @throws EntityNotFoundException If no unique or no entity was found.
   */
  private static <C> C obtainUniqueResource(SecurableResourceId pReference, TypedQuery<C> pQuery) throws EntityNotFoundException {
    try {
      return pQuery.getSingleResult();
    } catch (NonUniqueResultException e) {
      throw new EntityNotFoundException("Non unique entry found for SecurableResource with DomainUniqueId='" + pReference.getDomainUniqueId()
              + "' and Domain '" + pReference.getDomain() + "'", e);
    } catch (NoResultException e) {
      throw new EntityNotFoundException("SecurableResource for DomainUniqueId '" + pReference.getDomainUniqueId()
              + "' and Domain '" + pReference.getDomain() + "' not found", e);
    }
  }

  /**
   * Obtain a unique grant and produce errors if multiple or no grants were
   * found.
   *
   * @param <C> Generic type.
   * @param pGrantId The id of the grant.
   * @param pQuery The query to obtain the resource.
   *
   * @return The grant.
   *
   * @throws EntityNotFoundException If no unique or no entity was found.
   */
  private static <C> C obtainUniqueGrant(long pGrantId, TypedQuery<C> pQuery) throws EntityNotFoundException {
    try {
      return pQuery.getSingleResult();
    } catch (NonUniqueResultException e) {
      throw new EntityNotFoundException("Non unique entry found for grant.id=='" + pGrantId + "'.", e);
    } catch (NoResultException e) {
      throw new EntityNotFoundException("Grant for grant.id=='" + pGrantId + "' not found.", e);
    }
  }

  /**
   * Find a resource reference by its reference id.
   *
   * @param entityManager The entity manager to use to query.
   * @param referenceId The reference id of the resource reference to find.
   *
   * @return The found resource reference.
   *
   * @throws EntityNotFoundException If no resource reference was found for
   * reference id 'referenceId'.
   */
  public static ResourceReference findResourceReference(EntityManager entityManager, ReferenceId referenceId) throws EntityNotFoundException {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<ResourceReference> cq = cb.createQuery(ResourceReference.class);
    Root<ResourceReference> resourceReferences = cq.from(ResourceReference.class);
    Join<ResourceReference, Group> group = resourceReferences.
            <ResourceReference, Group>join("group");
    Join<ResourceReference, SecurableResource> resource = resourceReferences.
            <ResourceReference, SecurableResource>join("resource");
    cq.where(
            cb.and(
                    cb.equal(group.get("groupId"),
                            referenceId.getGroupId().getStringRepresentation()),
                    cb.equal(resource.get("domainId"),
                            referenceId.getResourceId().getDomain())),
            cb.equal(resource.get("domainUniqueId"),
                    referenceId.getResourceId().getDomainUniqueId()));
    cq.select(resourceReferences);
    TypedQuery<ResourceReference> q = entityManager.createQuery(cq);
    return obtainUniqueResource(referenceId.getSecurableResourceId(), q);
  }

  /**
   * Find a grant by its id.
   *
   * @param entityManager The entity manager to use to query.
   * @param pGrantId The id of the grant.
   *
   * @return The found Grant.
   *
   * @throws EntityNotFoundException If no grant was found for id 'pGrantId'.
   */
  public static Grant findGrant(EntityManager entityManager, long pGrantId) throws EntityNotFoundException {
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Grant> cq = entityManager.getCriteriaBuilder().createQuery(Grant.class);
    Root<Grant> grant = cq.from(Grant.class);
    cq.select(grant).where(
            cb.and(
                    cb.equal(grant.get("id"), pGrantId)));
    TypedQuery<Grant> q = entityManager.createQuery(cq);
    return obtainUniqueGrant(pGrantId, q);
  }

}
