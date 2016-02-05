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
package edu.kit.dama.authorization.services.administration;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import java.util.List;

/**
 * The interface for the administration of resouces and access rights in the
 * authorization module.
 *
 * Rights can be managed for groups using the so-called references via
 * *Reference(*) methods or directly for individual user's using so-called
 * grants using the *Grant*(*) methods.
 *
 * @author pasic
 */
public interface IResourceService {

  /**
   * Register a resource. Should be used only by administrators.
   *
   * <b>Note:</b> Check if this method is necessary as no role is defined for
   * the registered resource. Checked access will be possible only for
   * administrators.
   *
   * Authorization requests for non-registered resouces should result in an
   * exception.
   *
   * @param resourceId resourceId of the resource to register
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityAlreadyExistsException If the provided resource id is already
   * registered.
   */
  void registerResource(SecurableResourceId resourceId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityAlreadyExistsException;

  /**
   * Registers a resource and grants the specified role for accessing the
   * resource to the given existing groupId.
   *
   * @param resourceId The id of the resource to register.
   * @param owner The owner of the resource.
   * @param role role for resource access for the group.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the owner group was not found.
   */
  void registerResource(SecurableResourceId resourceId, GroupId owner, Role role, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Registers a resource and grants the specified role for accessing the
   * resource to the given existing userId.
   *
   * @param resourceId The id of the resource to register.
   * @param grantRole The role for resource access by userId.
   * @param userId The id of the user which owns the resource.
   * @param grantSetRole maximum role the user can get through this grantset
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the user was not found.
   */
  void registerResource(SecurableResourceId resourceId, Role grantRole, UserId userId, Role grantSetRole, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Remove the existing resource from the authorization infrastructure together
   * with its references and extra grants.
   *
   * @param resourceId The id of the resource to unregister.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource doesn't exist.
   */
  void remove(SecurableResourceId resourceId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Create a resource reference. The reference id consists of the group to
   * which the access permission should be granted and the resource for which
   * the permission is granted. The restriction is the maximal effective role
   * which can be obtained trough this reference.
   *
   * @param referenceId The id of the reference.
   * @param roleRestriction The restriction of the grant.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If parts of the reference could not be
   * found.
   * @throws EntityAlreadyExistsException If the reference already exists.
   */
  void createReference(ReferenceId referenceId, Role roleRestriction, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException, EntityAlreadyExistsException;

  /**
   * Removes the resource reference.
   *
   * @param referenceId The id of the reference to remove.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If parts of the reference could not be
   * found.
   */
  void deleteReference(ReferenceId referenceId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Changes the minimum required role for the given reference.
   *
   * @param referenceId The id of the reference to change.
   * @param newRole The new role associated with the reference.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If parts of the reference could not be
   * found.
   */
  void changeReferenceRestriction(ReferenceId referenceId, Role newRole, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Returns the maximum role of the given reference.
   *
   * @param referenceId The id of the reference.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return The maximum role of the reference.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If parts of the reference could not be
   * found.
   */
  IRoleRestriction getReferenceRestriction(ReferenceId referenceId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Lists the references belonging to the particular resource.
   *
   * @param resourceId The id of the resource.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return A list of references pointing to the provided resource id.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource could not be found.
   */
  List<ReferenceId> getReferences(SecurableResourceId resourceId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Allow extra grants for a resource.
   *
   * @param resourceId The id of the resource.
   * @param restriction The most powerful role achiveable trough any grant for
   * the given resource.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource could not be found.
   */
  void allowGrants(SecurableResourceId resourceId, Role restriction, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Returns true if grants are allowed for the particular resource otherwise
   * false.
   *
   * @param resourceId The id of the resource.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return TRUE rue if grants allowed.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource could not be found.
   */
  boolean grantsAllowed(SecurableResourceId resourceId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Gets the grants restriction which is the upper limit of privileges
   * obtainable via extra grants on the particular resource.
   *
   * @param resourceId The id of the resource.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation
   *
   * @return The max obtainable role for the provided resource.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource could not be found.
   */
  Role getGrantsRestriction(SecurableResourceId resourceId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Changes the grants restriction which is the upper limit of privileges
   * obtainable via extra grants on the particular resource.
   *
   * @param resourceId The id of the resource.
   * @param restriction The most powerful role achiveable trough any grant for
   * the given resource.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource could not be found.
   */
  void changeGrantsRestriction(SecurableResourceId resourceId, Role restriction, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Creates a new grant with the given maximum role for the existing resource
   * and the existing user.
   *
   * <tt>
   * effectiveRoleForGrant := min(extraGrant.getRoleRestriction(),
   * UserService#getRoleRestriction(userId) )
   * </tt>
   *
   * The user can posses a higher role trough his group in-group role and a
   * reference.
   *
   * @param resourceId The id of the resource to add a grant to.
   * @param userId The id of the user which receives this grant.
   * @param role The role for the user associated with this resource.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource could not be found.
   * @throws EntityAlreadyExistsException If the grant already exists.
   */
  void addGrant(SecurableResourceId resourceId, UserId userId, Role role, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException, EntityAlreadyExistsException;

  /**
   * Changes the maximum role of a grant.
   *
   * <tt>
   * effectiveRoleForGrant := min(extraGrant.getRoleRestriction(),
   * UserService#getRoleRestriction(userId) )
   * </tt>
   *
   * The user can posses a higher role trough his group in-group role and a
   * reference.
   *
   * @param resourceId The id of the resource of which an existing grant gets
   * changed.
   * @param userId The id of the user whose grant gets changed.
   * @param newRole The new role associated with this user and grant.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource or the user could not be
   * found.
   */
  void changeGrant(SecurableResourceId resourceId, UserId userId, Role newRole, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Revokes an existing grant of a user for a resource.
   *
   * @param resourceId The id of the resource of which an existing grant gets
   * revoked.
   * @param userId The id of the user which receives this grant.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource or the user could not be
   * found.
   */
  void revokeGrant(SecurableResourceId resourceId, UserId userId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Revokes all grants for the given resource and disallows adding grants.
   *
   * @param resourceId The id of the resource of which all existing grants get
   * revoked.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource could not be found.
   */
  void revokeAllAndDisallowGrants(SecurableResourceId resourceId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Returns the grant role of the user for the resource or null if no extra
   * grant present.
   *
   * @param resourceId The of the resource of which the grant gets returned
   * @param userId The id of the user associated with the grant
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return The grant role of the user for the resource or null.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource or the user could not be
   * found.
   */
  Role getGrantRole(SecurableResourceId resourceId, UserId userId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Returns all grants associated with the given resource.
   *
   * @param resourceId The id of the resource of which all grants get returned.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return All grants of the given resource.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource or the user could not be
   * found.
   */
  List<Grant> getGrants(SecurableResourceId resourceId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Returns all users who have the required role to access the given resource.
   *
   * @param resourceId The id of the resource in question.
   * @param minimumRole The minimum required role.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return All grants with at least <tt>minimumRole</tt> for the given
   * resource.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource or the user could not be
   * found.
   */
  List<UserId> getAuthorizedUsers(SecurableResourceId resourceId, Role minimumRole, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Obtain a list of groups which are allowed to access the resource with the
   * provided key with at least the provided minRole.
   *
   * @param resourceId The id of the resource in question.
   * @param minRole The minimum required role.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation
   *
   * @return A list of groups authorized to access the resource.
   *
   * @throws UnauthorizedAccessAttemptException If the given
   * <code>IAuthorizationContext</code> doesn't have a sufficient role.
   * @throws EntityNotFoundException If the resource or the user could not be
   * found.
   */
  List<GroupId> getAuthorizedGroups(SecurableResourceId resourceId, Role minRole, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;
}
