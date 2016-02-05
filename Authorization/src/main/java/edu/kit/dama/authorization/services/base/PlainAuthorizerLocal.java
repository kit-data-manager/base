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
package edu.kit.dama.authorization.services.base;

import edu.kit.dama.authorization.aspects.util.AuthorisationSignatureExtractor;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.base.impl.PlainAuthorizerImpl;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pasic
 */
public final class PlainAuthorizerLocal {

  private static final Logger LOGGER = LoggerFactory.getLogger(PlainAuthorizerLocal.class);

  /**
   * Default constructor.
   */
  private PlainAuthorizerLocal() {
  }

  private final static PlainAuthorizerImpl SINGLETON = new PlainAuthorizerImpl();

  /**
   * Try to authorize the context with the provided required role.
   *
   * @param context The context to authorize.
   * @param roleRequired The role required to authorize successfully.
   *
   * @throws UnauthorizedAccessAttemptException The provided context does not
   * possess the required role.
   * @throws EntityNotFoundException At least one component of the authorization
   * context was not found.
   */
  public static void authorize(IAuthorizationContext context, Role roleRequired) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    LOGGER.debug("Authorizing access for context {}. Required role is {}.", context, roleRequired);
    SINGLETON.authorize(context, roleRequired);
    LOGGER.debug("Authorization successful.");
  }

  /**
   * Try to authorize the context for accessing the resource with the provided
   * id with the provided required role.
   *
   * @param context The context to authorize.
   * @param resourceId The id of the resource to access.
   * @param roleRequired The role required to authorize successfully.
   *
   * @throws UnauthorizedAccessAttemptException The provided context does not
   * possess the required role.
   * @throws EntityNotFoundException At least one component of the authorization
   * context or the resource was not found.
   */
  public static void authorize(IAuthorizationContext context, SecurableResourceId resourceId, Role roleRequired)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    LOGGER.debug("Authorizing access to resource {} for context {}. Required role is {}.", resourceId, context, roleRequired);
    SINGLETON.authorize(context, resourceId, roleRequired);
    LOGGER.debug("Authorization successful.");
  }

  /**
   * Try to authorize the context for accessing any resource withing the
   * provided resource list with the provided required role.
   *
   * @param context The context to authorize.
   * @param resourceIds A list of ids of the resources to access.
   * @param roleRequired The role required to authorize successfully.
   *
   * @throws UnauthorizedAccessAttemptException The provided context does not
   * possess the required role.
   * @throws EntityNotFoundException At least one component of the authorization
   * context or any resource was not found.
   */
  public static void authorize(IAuthorizationContext context, List<SecurableResourceId> resourceIds, Role roleRequired)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    LOGGER.debug("Authorizing access to resources {} for context {}. Required role is {}.", resourceIds, context, roleRequired);
    SINGLETON.authorize(context, resourceIds, roleRequired);
    LOGGER.debug("Authorization successful.");
  }

  /**
   * Filter the provided list of resources using the provided context. Resources
   * which are accessible with the required role will appear in the result list.
   *
   * @param context The authorization context.
   * @param roleRequired The role required.
   * @param resourceIdsToFilter The iterator of resource ids to filter.
   * @param result The elements of resourceIdsToFilter for which the context is
   * authorized to access them with the requiredRole (@see authorize) are added
   * to result.
   */
  public static void filterOnAccessAllowed(
          IAuthorizationContext context,
          Role roleRequired,
          Iterable<SecurableResourceId> resourceIdsToFilter,
          Collection<SecurableResourceId> result) {
    LOGGER.debug("Filtering resources {} for access by context {}. Required role is {}.", resourceIdsToFilter, context, roleRequired);
    SINGLETON.filterOnAccessAllowed(context, roleRequired, resourceIdsToFilter, result);
    LOGGER.debug("Filtering successful. Result: {}", result);
  }

  /**
   * Filter out resources not accessible by the context at least with
   * roleRequired privilege. In this call, the provided list of resource ids is
   * directly modified.
   *
   * @param authContext The authorization context.
   * @param roleRequired The required role.
   * @param resourcesToFilter The list of resource ids to filter.
   */
  public static void filterOnAccessAllowed(
          IAuthorizationContext authContext,
          Role roleRequired,
          Collection<ISecurableResource> resourcesToFilter) {
    LOGGER.debug("Filtering resources {} for access by context {}. Required role is {}.", resourcesToFilter, authContext, roleRequired);
    SINGLETON.filterOnAccessAllowed(authContext, roleRequired, resourcesToFilter);
    LOGGER.debug("Filtering successful. Result: {}", resourcesToFilter);
  }

}
