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
package edu.kit.dama.authorization.util;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.util.Constants;

/**
 *
 * @author mf6319
 */
public final class AuthorizationUtil {

  /**
   * Hidden constructor.
   */
  private AuthorizationUtil() {
  }

  /**
   * Helper method to determine whether the provided authorization context is an
   * administrator context or not. This is the case if the group id is equal to
   * Constants.SYSTEM_GROUP and/or the role is Role.ADMINISTRATOR.
   *
   * @param pContext The context to check.
   *
   * @return TRUE if the context denotes an administrator.
   */
  public static boolean isAdminContext(IAuthorizationContext pContext) {
    return pContext != null
            && (pContext.getGroupId().getStringRepresentation().equals(Constants.SYSTEM_GROUP)
            || pContext.getRoleRestriction().equals(Role.ADMINISTRATOR));
  }

  /**
   * Obtain an authorization context for the provided user and group. Inside,
   * the GroupServiceLocal is used to obtain the role restriction for building
   * the final context. For querying the GroupService the system user context is
   * used.
   *
   * @param pUserId The user id of the resulting context.
   * @param pGroupId The group id of the resulting context.
   *
   * @return The AuthorizationContext.
   *
   * @throws AuthorizationException if something fails.
   */
  public static IAuthorizationContext getAuthorizationContext(UserId pUserId, GroupId pGroupId) throws AuthorizationException {
    try {
      Role role = (Role) GroupServiceLocal.getSingleton().getMaximumRole(pGroupId, pUserId, AuthorizationContext.factorySystemContext());
      return new AuthorizationContext(pUserId, pGroupId, role);
    } catch (UnauthorizedAccessAttemptException ex) {
      //fatal error
      throw new AuthorizationException("Failed to get maximum role using system context.", ex);
    }
  }

}
