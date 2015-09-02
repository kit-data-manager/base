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
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.User;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.IUserService;
import edu.kit.dama.authorization.entities.util.PU;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author soc
 */
public class UserServiceImpl implements IUserService {

  private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final void register(UserId userId, Role maximumRole, @Context IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException, EntityAlreadyExistsException {
    LOGGER.debug("Registering user {} with maximum role {}", new Object[]{userId, maximumRole});
    EntityManager em = PU.entityManager();
    try {
      LOGGER.debug(" - Checking for existing user");
      FindUtil.findUser(em, userId);
      em.close();
      throw new EntityAlreadyExistsException("There is already a user with "
              + "userId=" + userId.getStringRepresentation() + " registered!");
    } catch (EntityNotFoundException ex) {
      LOGGER.debug(" - Creating new user");
      User user = new User(userId.getStringRepresentation(), maximumRole);
      EntityTransaction transaction = em.getTransaction();
      transaction.begin();
      try {
        LOGGER.debug(" - Persisting user");
        em.persist(user);
        em.flush();
        transaction.commit();
        em.close();
        LOGGER.debug("Successfully registered user with user id {} and maximum role {}", new Object[]{userId, maximumRole});
      } finally {
        PU.handleUnexpectedPersistenceExceptionInTransaction(new PersistenceException("Failed to register user with id " + userId + " and maximum role " + maximumRole), em);
      }
    }
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public final IRoleRestriction getRoleRestriction(UserId userId, @Context IAuthorizationContext ctx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    LOGGER.debug("Getting role restriction for user with id {}", userId);
    EntityManager em = PU.entityManager();
    LOGGER.debug(" - Finding user");
    User user = FindUtil.findUser(em, userId);
    em.close();
    LOGGER.debug("Returning maximum role {}", user.getMaximumRole());
    return user.getMaximumRole();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MANAGER)
  public final void setRoleRestriction(UserId userId, Role maximumRole, @Context IAuthorizationContext ctx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    LOGGER.debug("Setting role restriction for user with id {}", userId);
    EntityManager em = PU.entityManager();
    EntityTransaction transaction = em.getTransaction();
    transaction.begin();
    try {
      LOGGER.debug(" - Finding user");
      User user = FindUtil.findUser(em, userId);
      LOGGER.debug(" - Changing maximum role from {} to {}", new Object[]{user.getMaximumRole(), maximumRole});
      user.setMaximumRole(maximumRole);
      LOGGER.debug(" - Merging user to database");
      em.merge(user);
      transaction.commit();
      em.close();
      LOGGER.debug("Role restriction for user {} successfully set to role {}", new Object[]{userId, maximumRole});
    } catch (PersistenceException except) {
      PU.handleUnexpectedPersistenceExceptionInTransaction(except, em);
      throw new PersistenceException("Failed to set role restriction for user with id " + userId + " to " + maximumRole, except);
    }
  }
}
