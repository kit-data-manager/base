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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.authorization.services.base;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import java.util.Collection;
import java.util.List;

/**
 * Authorizer interface provides methods to check the authorization of some
 * security context against some protected resource;
 *
 * @author pasic
 */
public interface IAuthorizationService {

  /**
   * Throws UnauthorizedAccessAttemptException if the system-wide restrictions
   * specified on the context are less permissive than roleRequiered and returns
   * if the authorization was successful.
   *
   * @param context The context to authorize.
   * @param roleRequired The min. required role.
   *
   * @throws UnauthorizedAccessAttemptException If the context does not possess
   * roleRequired.
   * @throws EntityNotFoundException If any element of the context was not
   * found.
   */
  void authorize(IAuthorizationContext context, Role roleRequired)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Throws UnauthorizedAccessAttemptException if the system-wide restrictions
   * specified on the context are less permissive than roleRequiered or the
   * context holds a less permissive role than roleRequired for the resource
   * specified by resourceId. If the authorization was successful the method
   * simply returns.
   *
   * @param context The context to authorize.
   * @param resourceId The id of the resource to check.
   * @param roleRequired The min. required role.
   *
   * @throws UnauthorizedAccessAttemptException If the context does not possess
   * roleRequired.
   * @throws EntityNotFoundException If any element of the context or the
   * resource id was not found.
   */
  void authorize(IAuthorizationContext context, SecurableResourceId resourceId, Role roleRequired)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Throws UnauthorizedAccessAttemptException if the system-wide restrictions
   * specified on the context are less permissive than roleRequiered or the
   * context holds a less permissive role than roleRequired for any of the
   * resources specified by resourceIds. If the authorization was successful the
   * method simply returns.
   *
   * @param context The context to authorize.
   * @param resourceIds A list of resource ids to check.
   * @param roleRequired The min. required role.
   *
   * @throws UnauthorizedAccessAttemptException If the context does not possess
   * roleRequired.
   * @throws EntityNotFoundException If any element of the context or any
   * resource id was not found.
   */
  void authorize(IAuthorizationContext context, List<SecurableResourceId> resourceIds, Role roleRequired)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Filter out resourceIds not accessible by the context at least with
   * roleRequired privilege.
   *
   * @param context The authorization context.
   * @param roleRequired The role required.
   * @param resourceIdsToFilter The list of resource ids to filter.
   * @param result The elements of resourceIdsToFilter for which the context is
   * authorized to access them with the requiredRole (@see authorize) are added
   * to result.
   */
  void filterOnAccessAllowed(
          IAuthorizationContext context,
          Role roleRequired,
          Iterable<SecurableResourceId> resourceIdsToFilter,
          Collection<SecurableResourceId> result);

  /**
   * Filter out resources not accessible by the context at least with
   * roleRequired privilege. In this call, the provided list of resource ids is
   * directly modified.
   *
   * @param authContext The authorization context.
   * @param roleRequired The role required.
   * @param resourcesToFilter The list of resource ids to filter.
   */
  void filterOnAccessAllowed(
          IAuthorizationContext authContext,
          Role roleRequired,
          Collection<? extends ISecurableResource> resourcesToFilter);

}
