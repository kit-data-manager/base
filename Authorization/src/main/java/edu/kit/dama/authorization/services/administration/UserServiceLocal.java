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
import edu.kit.dama.authorization.services.administration.impl.UserServiceImpl;

/**
 *
 * @author pasic
 */
public final class UserServiceLocal implements IUserService {

  private static final UserServiceLocal SINGLETON = new UserServiceLocal();
  private final IUserService userService;

  /**
   * Hidden constructor.
   */
  private UserServiceLocal() {
    userService = new UserServiceImpl();
  }

  /**
   * Get the singleton instance.
   *
   * @return The singleton instance.
   */
  public static IUserService getSingleton() {
    return SINGLETON;
  }

  @Override
  public void register(UserId userId, Role maximumRole, IAuthorizationContext ctx) throws EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    userService.register(userId, maximumRole, ctx);
  }

  @Override
  public IRoleRestriction getRoleRestriction(UserId userId, IAuthorizationContext ctx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return userService.getRoleRestriction(userId, ctx);
  }

  @Override
  public void setRoleRestriction(UserId userId, Role newMaximumRole, IAuthorizationContext ctx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    userService.setRoleRestriction(userId, newMaximumRole, ctx);
  }
}
