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
package edu.kit.dama.authorization.services.administration;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;

/**
 * Interface for the administration of users in the authorization module.
 *
 * For users not present in the persistence backend of the authorization module,
 * all request should be denied.
 *
 * @author pasic
 */
public interface IUserService {

  /**
   * Registers an user and specifies the maximal role which can be granted to
   * the given user.
   *
   * @param userId The id of the user to register.
   * @param maximumRole The maximum role this user can get.
   * @param ctx The authorization context.
   *
   * @throws UnauthorizedAccessAttemptException The context is not allowed to
   * register a user.
   * @throws EntityAlreadyExistsException The user with the provided id already
   * exists.
   */
  void register(UserId userId, Role maximumRole, IAuthorizationContext ctx)
          throws UnauthorizedAccessAttemptException, EntityAlreadyExistsException;

  /**
   * Returns the current maximum role for the specified user.
   *
   * The user's <tt>maximumRole</tt> is the maximal role which can be granted to
   * the given user.
   *
   * @param userId The id of the user.
   * @param ctx The authorization context.
   *
   * @return The current role of user.
   *
   * @throws UnauthorizedAccessAttemptException The context is not allowed to
   * register a user.
   * @throws EntityNotFoundException A user with the provided id was not found.
   */
  IRoleRestriction getRoleRestriction(UserId userId, IAuthorizationContext ctx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Changes the maximum role of this user.
   *
   * The user's <tt>maximumRole</tt> is the maximal role which can be granted to
   * the given user.
   *
   * @param userId The id of the user whose role should be changed.
   * @param newMaximumRole The new maximum role.
   * @param ctx The authorization context.
   *
   * @throws UnauthorizedAccessAttemptException The context is not allowed to
   * register a user.
   * @throws EntityNotFoundException A user with the provided id was not found.
   */
  void setRoleRestriction(UserId userId, Role newMaximumRole, IAuthorizationContext ctx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;
}
