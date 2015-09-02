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
package edu.kit.dama.authorization.services.administration.impl;

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.entities.impl.GrantSet;
import edu.kit.dama.authorization.entities.impl.Group;
import edu.kit.dama.authorization.entities.impl.Membership;
import edu.kit.dama.authorization.entities.impl.ResourceReference;
import edu.kit.dama.authorization.entities.impl.SecurableResource;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.IGroupService;
import edu.kit.dama.authorization.entities.util.PU;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ochsenreither
 */
public class GroupServiceImpl implements IGroupService {

  private final static Logger LOGGER = LoggerFactory.getLogger(GroupServiceImpl.class);
  private final static String BUILDING_QUERY_STRING = " - Building query";
  private final static String EXECUTING_QUERY_STRING = " - Executing query";
  private final static String BUILDING_RESULT_LIST_STRING = " - Building result list for {} elements";

  private static final String USER_ID_COLUMN = "userId";
  private static final String GROUP_ID_COLUMN = "groupId";
  private static final String USER_COLUMN = "user";
  private static final String GROUP_COLUMN = "group";

  @Override
  @SecuredMethod(roleRequired = Role.ADMINISTRATOR)
  public final void create(GroupId newGroupId, UserId groupManager, @Context IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException, EntityAlreadyExistsException {
    LOGGER.debug("Creating new group {} with manager {}", new Object[]{newGroupId, groupManager});
    EntityManager entityManager = PU.entityManager();
    EntityTransaction transaction = entityManager.getTransaction();

    try {
      LOGGER.debug(" - Trying to find existing group");
      FindUtil.findGroup(entityManager, newGroupId);
      entityManager.close();
      throw new EntityAlreadyExistsException("There is already a group with groupId=" + newGroupId.getStringRepresentation());
    } catch (EntityNotFoundException e) {
      LOGGER.debug(" - Group {} does not exists, continue with creation.", newGroupId);
      transaction.begin();
      try {
        LOGGER.debug(" - Finding manager user with id {}", groupManager);
        User groupManagerE = FindUtil.findUser(entityManager, groupManager);
        LOGGER.debug(" - Creating new group");
        Group newGroup = new Group(newGroupId.getStringRepresentation());
        entityManager.persist(newGroup);
        LOGGER.debug(" - Persisting manager membership");
        entityManager.persist(new Membership(groupManagerE, Role.MANAGER, newGroup));
        LOGGER.debug(" - Group successfully created, mapping group to securable resource.");
        SecurableResource resource = new SecurableResource(newGroupId.getSecurableResourceId());
        entityManager.persist(resource);
        ResourceReference resourceReference = new ResourceReference(Role.MANAGER, resource, newGroup);
        entityManager.persist(resourceReference);
        LOGGER.debug(" - Linking securable resource to group");
        newGroup.getResourceReferences().add(resourceReference);
        resource.getResourceReferences().add(resourceReference);

        transaction.commit();
        entityManager.close();
        LOGGER.debug("Group for ID {} successfully created.", newGroupId);
      } finally {
        PU.handleUnexpectedPersistenceExceptionInTransaction(new PersistenceException("Failed to create group with id " + newGroupId + " and manager " + groupManager), entityManager);
      }
    }
  }

  @Override
  @SecuredMethod(roleRequired = Role.ADMINISTRATOR)
  public final boolean remove(GroupId groupId, @Context IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    LOGGER.debug("Removing group {}", groupId);
    EntityManager em = PU.entityManager();
    em.getTransaction().begin();

    boolean result = false;
    try {
      LOGGER.debug(" - Trying to find group {}", groupId);
      Group group = FindUtil.findGroup(em, groupId);
      LOGGER.debug(" - Getting all group memberships");
      List<Membership> memberships = em.createQuery("SELECT m "
              + "FROM Memberships m "
              + "WHERE m.group.groupId = :groupId",
              Membership.class).
              setParameter(GROUP_ID_COLUMN, groupId.getStringRepresentation()).
              getResultList();
      LOGGER.debug(" - {}", (memberships.isEmpty()) ? "No memberships found" : "Memberships found");
      LOGGER.debug(" - Getting associated securable resources");
      SecurableResource resource = FindUtil.findResource(em, groupId.getSecurableResourceId());
      LOGGER.debug(" - Getting grant set");
      GrantSet grantSet = resource.getGrantSet();
      if (null != grantSet) {
        LOGGER.debug(" - Removing all grants");
        for (Grant g : grantSet.getGrants()) {
          em.remove(g);
        }
        LOGGER.debug(" - Removing grant set");
        em.remove(grantSet);
      } else {
        LOGGER.debug(" - No grants found");
      }
      LOGGER.debug(" - Removing resource references");
      List<ResourceReference> resourceReferences = group.getResourceReferences();
      for (ResourceReference rr : resourceReferences) {
        em.remove(rr);
      }
      LOGGER.debug(" - Removing securable resource");
      em.remove(resource);
      LOGGER.debug(" - Removing memberships");
      for (Membership membership : memberships) {
        User currentUser = membership.getUser();
        em.remove(membership);
        currentUser.getMemberships().remove(membership);
      }
      LOGGER.debug(" - Removing group");
      em.remove(group);

      em.getTransaction().commit();
      em.close();
      LOGGER.debug("Group with id {} successfully removed", groupId);
      result = true;
    } finally {
      PU.handleUnexpectedPersistenceExceptionInTransaction(new PersistenceException("Failed to remove group with id " + groupId), em);
    }
    return result;
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final void addUser(GroupId groupId, UserId userId, Role role, @Context IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException, EntityAlreadyExistsException {
    LOGGER.debug("Adding user {} with role {} to group {}", new Object[]{userId, role, groupId});
    EntityManager em = PU.entityManager();
    try {
      LOGGER.debug(" - Checking for existing membership");
      FindUtil.findMembership(em, groupId, userId);
      em.close();
      throw new EntityAlreadyExistsException("The user identified by userId="
              + userId.getStringRepresentation() + " is already a member of "
              + "the group identified by groupId=" + groupId.getStringRepresentation());
    } catch (EntityNotFoundException e) {
      //user no member of this group
      em.getTransaction().begin();
      try {
        LOGGER.debug(" - Finding user");
        User user = FindUtil.findUser(em, userId);
        LOGGER.debug(" - Finding group");
        Group group = FindUtil.findGroup(em, groupId);
        LOGGER.debug(" - Creating membership");
        Membership newMembership = new Membership(user, role, group);
        LOGGER.debug(" - Persisting membership");
        em.persist(newMembership);
        LOGGER.debug(" - Adding membership to user and group");
        user.getMemberships().add(newMembership);
        group.getMemberships().add(newMembership);
        LOGGER.debug(" - Committing transaction");
        em.getTransaction().commit();
        em.close();
        LOGGER.debug("User {} successfully added to group {} with role {}", new Object[]{userId, groupId, role});
      } finally {
        PU.handleUnexpectedPersistenceExceptionInTransaction(new PersistenceException("Failed to add user " + userId + " with role " + role + " to group " + groupId), em);
      }
    }
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final void removeUser(GroupId groupId, UserId userId, @Context IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    LOGGER.debug("Removing user {} from group {}", new Object[]{userId, groupId});

    EntityManager em = PU.entityManager();
    EntityTransaction transaction = em.getTransaction();
    transaction.begin();
    try {
      LOGGER.debug(" - Finding user");
      User user = FindUtil.findUser(em, userId);
      LOGGER.debug(" - Finding group");
      Group group = FindUtil.findGroup(em, groupId);
      LOGGER.debug(" - Finding membership");
      Membership membership = FindUtil.findMembership(em, groupId, userId);
      LOGGER.debug(" - Removing membership");
      em.remove(membership);
      LOGGER.debug(" - Adapting user and group entities");
      user.getMemberships().remove(membership);
      group.getMemberships().remove(membership);

      transaction.commit();
      em.close();
      LOGGER.debug("User {} successfully removed from group {}", new Object[]{userId, groupId});
    } catch (PersistenceException except) {
      PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
      throw new PersistenceException("Failed to remove user " + userId + " from group " + groupId, except);
    }
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final IRoleRestriction getMaximumRole(GroupId groupId, UserId userId, @Context IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    LOGGER.debug("Getting maximum role of user {} in group {}", new Object[]{userId, groupId});
    EntityManager em = PU.entityManager();
    LOGGER.debug(BUILDING_QUERY_STRING);
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Tuple> q = cb.createTupleQuery();
    Root<Membership> from = q.from(Membership.class);
    Join<Membership, User> juser = from.<Membership, User>join(USER_COLUMN);
    Join<Membership, Group> jgroup = from.<Membership, Group>join(GROUP_COLUMN);
    q.select(cb.tuple(from.<Role>get("role").alias("groupRole"), juser.<Role>get("maximumRole").alias("userRole")));

    q.where(cb.and(
            cb.equal(
                    jgroup.get(GROUP_ID_COLUMN),
                    groupId.getStringRepresentation()),
            cb.equal(
                    juser.get(USER_ID_COLUMN),
                    userId.getStringRepresentation())));

    LOGGER.debug(EXECUTING_QUERY_STRING);
    TypedQuery<Tuple> tq = em.createQuery(q);
    Tuple singleResult;
    try {
      LOGGER.debug(" - Obtaining result");
      singleResult = tq.getSingleResult();
      LOGGER.debug(" - Result successfully obtained");
    } catch (NonUniqueResultException e) {
      throw new EntityNotFoundException("Role for userId=" + userId.getStringRepresentation()
              + " and group=" + groupId.getStringRepresentation()
              + " is undefined.", e);
    } catch (NoResultException e) {
      throw new EntityNotFoundException("Role for userId=" + userId.getStringRepresentation()
              + " and group=" + groupId.getStringRepresentation()
              + " is undefined.", e);
    }

    em.close();
    LOGGER.debug("Returning maximum role from result");
    return singleResult.get(0, Role.class).lessThan(singleResult.get(1, Role.class))
            ? singleResult.get(0, Role.class) : singleResult.get(1, Role.class);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final Role changeRole(GroupId groupId, UserId userId, Role role, @Context IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    LOGGER.debug("Changing role of user {} in group {} to new role {}", new Object[]{userId, groupId, role});
    EntityManager em = PU.entityManager();
    EntityTransaction transaction = em.getTransaction();
    Role oldRole = null;
    transaction.begin();
    try {
      LOGGER.debug(" - Finding membership");
      Membership membership = FindUtil.findMembership(em, groupId, userId);
      oldRole = membership.getRole();
      LOGGER.debug(" - Current role is: {}. Setting new role {}", new Object[]{oldRole, role});
      membership.setRole(role);
      transaction.commit();
      em.close();
      LOGGER.debug("Role of user {} in group {} successfully changed to {}", new Object[]{userId, groupId, role});
    } catch (PersistenceException except) {
      PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
      throw new PersistenceException("Failed to change role of user " + userId + " in group " + groupId + " to " + role, except);
    }
    return oldRole;
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public final List<GroupId> getAllGroupsIds(int first, int results, @Context IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Getting all groups");
    EntityManager em = PU.entityManager();
    LOGGER.debug(BUILDING_QUERY_STRING);
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<String> q = cb.createQuery(String.class);
    Root<Group> group = q.from(Group.class);
    q.select(group.<String>get(GROUP_ID_COLUMN));
    LOGGER.debug(EXECUTING_QUERY_STRING);
    TypedQuery<String> tq = em.createQuery(q);
    tq.setFirstResult(first);
    tq.setMaxResults(results);
    LOGGER.debug(" - Getting query result");
    List<String> idsAsString = tq.getResultList();
    em.close();
    LOGGER.debug(BUILDING_RESULT_LIST_STRING, idsAsString.size());
    List<GroupId> groupIds = new ArrayList<GroupId>(idsAsString.size());
    for (String idAsString : idsAsString) {
      groupIds.add(new GroupId(idAsString));
    }
    LOGGER.debug("{} groups successfully obtained", groupIds.size());
    return groupIds;
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final List<UserId> getUsersIds(GroupId groupId, int first, int results, @Context IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Getting all user ids in group {}", groupId);
    EntityManager em = PU.entityManager();
    LOGGER.debug(BUILDING_QUERY_STRING);
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<String> cq = cb.createQuery(String.class);
    Root<User> user = cq.from(User.class);
    Join<User, Membership> membership = user.<User, Membership>join("memberships");
    Join<Membership, Group> group = membership.<Membership, Group>join(GROUP_COLUMN);
    cq.where(cb.equal(group.get(GROUP_ID_COLUMN), groupId.getStringRepresentation()));
    cq.select(user.<String>get(USER_ID_COLUMN));
    LOGGER.debug(EXECUTING_QUERY_STRING);

    TypedQuery<String> tq = em.createQuery(cq);
    tq.setFirstResult(first);
    tq.setMaxResults(results);
    List<String> userIdStrings = tq.getResultList();
    em.close();
    List<UserId> userIds = new ArrayList<UserId>(userIdStrings.size());
    LOGGER.debug(BUILDING_RESULT_LIST_STRING, userIdStrings.size());
    for (String id : userIdStrings) {
      userIds.add(new UserId(id));
    }
    LOGGER.debug("{} users successfully obtained", userIds.size());
    return userIds;
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final List<UserId> getGroupManagers(GroupId groupId, int first, int results, @Context IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Getting all manager user ids in group {}", groupId);
    EntityManager em = PU.entityManager();
    LOGGER.debug(BUILDING_QUERY_STRING);

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<String> q = cb.createQuery(String.class);
    Root<Membership> membership = q.from(Membership.class);
    Join<Membership, User> membershipUserJoin = membership.<Membership, User>join(USER_COLUMN);
    Join<Membership, Group> membershipGroupJoin = membership.<Membership, Group>join(GROUP_COLUMN);

    q.select(membershipUserJoin.<String>get(USER_ID_COLUMN));

    q.where(cb.and(cb.equal(
            membershipGroupJoin.get(GROUP_ID_COLUMN),
            groupId.getStringRepresentation()),
            cb.greaterThanOrEqualTo(membership.<Role>get("role").as(Role.class), Role.MANAGER),
            cb.greaterThanOrEqualTo(membershipUserJoin.<Role>get("maximumRole").as(Role.class), Role.MANAGER)));

    LOGGER.debug(EXECUTING_QUERY_STRING);
    TypedQuery<String> tq = em.createQuery(q);
    tq.setFirstResult(first);
    tq.setMaxResults(results);
    List<String> userIdStrings = tq.getResultList();
    em.close();
    List<UserId> userIds = new ArrayList<UserId>(userIdStrings.size());
    LOGGER.debug(BUILDING_RESULT_LIST_STRING, userIdStrings.size());
    for (String id : userIdStrings) {
      userIds.add(new UserId(id));
    }
    LOGGER.debug("{} manager users successfully obtained", userIds.size());
    return userIds;
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final List<GroupId> membershipsOf(UserId userId, int first, int results, @Context IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Getting all memberships of user {}", userId);
    EntityManager em = PU.entityManager();
    LOGGER.debug(BUILDING_QUERY_STRING);
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<String> cq = cb.createQuery(String.class);
    Root<Group> group = cq.from(Group.class);
    Join<Group, Membership> membership = group.<Group, Membership>join("memberships");
    Join<Membership, User> user = membership.<Membership, User>join(USER_COLUMN);
    cq.where(cb.equal(user.get(USER_ID_COLUMN), userId.getStringRepresentation()));
    cq.select(group.<String>get(GROUP_ID_COLUMN));
    LOGGER.debug(EXECUTING_QUERY_STRING);
    TypedQuery<String> tq = em.createQuery(cq);
    tq.setFirstResult(first);
    tq.setMaxResults(results);
    List<String> idsAsString = tq.getResultList();
    List<GroupId> memberships = new ArrayList<GroupId>(idsAsString.size());
    em.close();
    LOGGER.debug(BUILDING_RESULT_LIST_STRING, idsAsString.size());
    for (String idAsString : idsAsString) {
      memberships.add(new GroupId(idAsString));
    }
    LOGGER.debug("{} memberships successfully obtained", memberships.size());
    return memberships;
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final List<GroupId> membershipsOf(UserId userId, Role maximumRole, int first, int results, @Context IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Getting all memberships of user {} with the maximum role {}", new Object[]{userId, maximumRole});
    EntityManager em = PU.entityManager();
    LOGGER.debug(BUILDING_QUERY_STRING);
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Tuple> cq = cb.createTupleQuery();
    Root<Group> group = cq.from(Group.class);
    Join<Group, Membership> membership = group.<Group, Membership>join("memberships");
    Join<Membership, User> user = membership.<Membership, User>join(USER_COLUMN);
    cq.where(cb.equal(user.get(USER_ID_COLUMN), userId.getStringRepresentation()));
    cq.select(cb.tuple(group.<String>get(GROUP_ID_COLUMN), user.<Role>get("maximumRole"), membership.<Role>get("role")));
    LOGGER.debug(EXECUTING_QUERY_STRING);
    TypedQuery<Tuple> tq = em.createQuery(cq);
    tq.setFirstResult(first);
    tq.setMaxResults(results);
    List<Tuple> tuples = tq.getResultList();
    List<GroupId> memberships = new ArrayList<GroupId>(tuples.size());
    em.close();
    LOGGER.debug(BUILDING_RESULT_LIST_STRING, tuples.size());
    for (Tuple t : tuples) {
      if (t.get(1, Role.class).atLeast(maximumRole) && t.get(2, Role.class).atLeast(maximumRole)) {
        memberships.add(new GroupId(t.get(0, String.class)));
      }
    }
    LOGGER.debug("{} memberships successfully obtained", memberships.size());
    return memberships;
  }
}
