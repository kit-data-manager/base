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
package edu.kit.dama.authorization.services.base.impl;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.impl.FilterHelper;
import edu.kit.dama.authorization.entities.impl.Group;
import edu.kit.dama.authorization.entities.impl.Membership;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.base.IAuthorizationService;
import edu.kit.dama.authorization.services.base.PlainAuthorizerLocal;
import edu.kit.dama.util.Constants;
import java.util.*;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import org.slf4j.LoggerFactory;

/**
 * Is an implementation of the {@link IAuthorizationService} interface which,
 * uses an relational database as a backend.
 *
 * @author pasic
 */
public final class PlainAuthorizerImpl implements IAuthorizationService {

  private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PlainAuthorizerImpl.class);

  private static final String USER_ID_COLUMN = "userId";
  private static final String GROUP_ID_COLUMN = "groupId";
  private static final String DOMAIN_ID_COLUMN = "domainId";
  private static final String DOMAIN_UNIQUE_ID_COLUMN = "domainUniqueId";

  @Override
  public void authorize(IAuthorizationContext authContext, Role roleRequired) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    if (authContext.getRoleRestriction() != null && authContext.getRoleRestriction().lessThan(roleRequired)) {
      throw new UnauthorizedAccessAttemptException("Unauthorized attempt (" + authContext.getRoleRestriction() + " < " + roleRequired + ")");
    }
    if (authContext.getGroupId().getStringRepresentation().equals(Constants.SYSTEM_GROUP)
            && authContext.getUserId().getStringRepresentation().equals(Constants.SYSTEM_ADMIN)) {
      return;
    }
    EntityManager entityManager = PU.entityManager();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Tuple> q = cb.createTupleQuery();
    Root<Membership> from = q.from(Membership.class);
    Join<Membership, User> juser = from.<Membership, User>join("user");
    Join<Membership, Group> jgroup = from.<Membership, Group>join("group");
    q.where(cb.and(
            cb.equal(
                    jgroup.get(GROUP_ID_COLUMN),
                    authContext.getGroupId().getStringRepresentation()),
            cb.equal(
                    juser.get(USER_ID_COLUMN),
                    authContext.getUserId().getStringRepresentation())));
    q.select(cb.tuple(from.<Role>get("role"), juser.<Role>get("maximumRole")));
    TypedQuery<Tuple> tq = entityManager.createQuery(q);
    try {
      Tuple singleResult = tq.getSingleResult();

      if (singleResult.get(0, Role.class).lessThan(roleRequired) || //group role is smaller
              singleResult.get(1, Role.class).lessThan(roleRequired) //global maximum role is smaller
              ) {
        throw new UnauthorizedAccessAttemptException("Unauthorized attempt. Insufficient group, global or context permissions.");
      }
    } catch (NoResultException e) {
      throw new UnauthorizedAccessAttemptException("Unauthorized attempt. Wrong User-/GroupId or missing permission?", e);
    }
  }

  @Override
  public void authorize(IAuthorizationContext context, SecurableResourceId resourceId, Role roleRequired) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    if (null == resourceId) {
      authorize(context, roleRequired);
    } else {
      ArrayList<SecurableResourceId> coll = new ArrayList<SecurableResourceId>(1);
      coll.add(resourceId);
      authorize(context, coll, roleRequired);
    }
  }

  @Override
  public void authorize(
          IAuthorizationContext authContext,
          List<SecurableResourceId> protectedResources,
          Role roleRequired)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    if (authContext.getRoleRestriction() != null && authContext.getRoleRestriction().lessThan(roleRequired)) {
      throw new UnauthorizedAccessAttemptException("Unauthorized attempt. RoleRestriction < roleRequired (" + authContext.getRoleRestriction() + " < " + roleRequired + ")");
    }
    if (authContext.getGroupId().getStringRepresentation().equals(Constants.SYSTEM_GROUP)) {
      authorize(authContext, roleRequired);
      return;
    }
    EntityManager entityManager = PU.entityManager();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    if (null != protectedResources
            && protectedResources.size() > 0
            && !authContext.getGroupId().getStringRepresentation().equals(Constants.SYSTEM_GROUP)) {
      CriteriaQuery<Integer> q = cb.createQuery(Integer.class);
      Root<FilterHelper> record = q.from(FilterHelper.class);
      Predicate where;

      Predicate whereCTX
              = cb.and(cb.equal(
                              record.get(GROUP_ID_COLUMN),
                              authContext.getGroupId().getStringRepresentation()),
                      cb.equal(
                              record.get(USER_ID_COLUMN),
                              authContext.getUserId().getStringRepresentation()));

      Predicate whereRES = cb.and(
              cb.equal(
                      record.get(DOMAIN_UNIQUE_ID_COLUMN),
                      protectedResources.get(0).getDomainUniqueId()),
              cb.equal(
                      record.get(DOMAIN_ID_COLUMN),
                      protectedResources.get(0).getDomain()));
      for (int i = 1; i < protectedResources.size(); ++i) {
        cb.or(whereRES,
                cb.and(
                        cb.equal(
                                record.get(DOMAIN_UNIQUE_ID_COLUMN),
                                protectedResources.get(i).getDomainUniqueId()),
                        cb.equal(
                                record.get(DOMAIN_ID_COLUMN),
                                protectedResources.get(i).getDomain())));
      }
      where = cb.and(whereCTX, whereRES);
      q.where(where);
      q.groupBy(record.get(USER_ID_COLUMN));
      cb.least(record.<Integer>get("roleAllowed"));
      q.select(cb.least(record.<Integer>get("roleAllowed")));
      TypedQuery<Integer> tq = entityManager.createQuery(q);
      try {
        Integer result = tq.getSingleResult();
        if (roleRequired.ordinal() > result) {
          throw new UnauthorizedAccessAttemptException("Unauthorized attempt. RoleRequired > QueryResult (" + roleRequired.ordinal() + " > " + result + ")");
        }
      } catch (NoResultException e) {
        //Search for resource(s). If any resource does not exist, an EntityNotFoundException is produced...
        for (SecurableResourceId res : protectedResources) {
         FindUtil.findResource(entityManager, res);
         }
        //...otherwise, an UnauthorizedAccessAttemptException is thrown.
        throw new UnauthorizedAccessAttemptException("Unauthorized attempt!", e);
      }
    } else {
      authorize(authContext, roleRequired);
    }

  }

  @Override
  public void filterOnAccessAllowed(
          IAuthorizationContext authContext,
          Role roleRequired,
          Iterable<SecurableResourceId> resourceIdsToFilter,
          Collection<SecurableResourceId> result) {
    if ((authContext.getRoleRestriction() != null && authContext.getRoleRestriction().lessThan(roleRequired)) || resourceIdsToFilter == null) {
      return;
    }
    if (authContext.getGroupId().getStringRepresentation().equals(Constants.SYSTEM_GROUP)) {
      try {
        //simple authorize and add all resources
        authorize(authContext, roleRequired);
        for (SecurableResourceId r : resourceIdsToFilter) {
          result.add(r);
        }
      } catch (EntityNotFoundException ex) {
        // No data corresponding to the context
        // should NEVER happen!
        LOGGER.warn("You should have never seen this message. Please contact a developer.", ex);
      } catch (UnauthorizedAccessAttemptException e) {
        //not authorized
      }
    } else {
      EntityManager entityManager = PU.entityManager();
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      Iterator<SecurableResourceId> it = resourceIdsToFilter.iterator();
      if (it.hasNext()) {
        CriteriaQuery<Tuple> q = cb.createTupleQuery();
        Root<FilterHelper> record = q.from(FilterHelper.class);
        Predicate where;

        Predicate whereCTX
                = cb.and(cb.equal(
                                record.get(GROUP_ID_COLUMN),
                                authContext.getGroupId().getStringRepresentation()),
                        cb.equal(
                                record.get(USER_ID_COLUMN),
                                authContext.getUserId().getStringRepresentation()));
        SecurableResourceId filteredResource = it.next();
        Predicate whereRES = cb.and(
                cb.equal(
                        record.get(DOMAIN_UNIQUE_ID_COLUMN),
                        filteredResource.getDomainUniqueId()),
                cb.equal(
                        record.get(DOMAIN_ID_COLUMN),
                        filteredResource.getDomain()));
        while (it.hasNext()) {
          filteredResource = it.next();
          whereRES = cb.or(
                  whereRES,
                  cb.and(
                          cb.equal(
                                  record.get(DOMAIN_UNIQUE_ID_COLUMN),
                                  filteredResource.getDomainUniqueId()),
                          cb.equal(
                                  record.get(DOMAIN_ID_COLUMN),
                                  filteredResource.getDomain())));
        }
        Predicate whereRole = cb.ge(
                record.<Integer>get("roleAllowed"),
                roleRequired.ordinal());
        where = cb.and(whereCTX, whereRES, whereRole);
        q.where(where);
        q.multiselect(record.get(DOMAIN_ID_COLUMN), record.get(DOMAIN_UNIQUE_ID_COLUMN));
        TypedQuery<Tuple> tq = entityManager.createQuery(q);
        try {
          List<Tuple> resultList = tq.getResultList();
          SecurableResourceId current;
          for (Tuple t : resultList) {
            current = new SecurableResourceId(
                    t.get(0, String.class),
                    t.get(1, String.class));
            result.add(current);
          }
        } catch (NoResultException e) {
          //no result
        }
      }
    }
  }

  @Override
  public void filterOnAccessAllowed(
          IAuthorizationContext authContext,
          Role roleRequired,
          Collection<? extends ISecurableResource> resourcesToFilter) {
    // Create list containing only the securableResourceIds
    List<SecurableResourceId> resourceIds = new ArrayList<SecurableResourceId>();
    for (ISecurableResource element : resourcesToFilter) {
      resourceIds.add(element.getSecurableResourceId());
    }
    // Create a third empty list!?
    Set<SecurableResourceId> filtered = new HashSet<SecurableResourceId>();
    PlainAuthorizerLocal.filterOnAccessAllowed(authContext, roleRequired, resourceIds, filtered);
    Iterator<? extends ISecurableResource> iterator = resourcesToFilter.iterator();
    ISecurableResource current;
    while (iterator.hasNext()) {
      current = iterator.next();
      if (!filtered.contains(current.getSecurableResourceId())) {
        iterator.remove();
      }
    }
  }
}
