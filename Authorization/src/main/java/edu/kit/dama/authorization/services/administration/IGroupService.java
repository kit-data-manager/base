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
package edu.kit.dama.authorization.services.administration;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import java.util.List;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;

/**
 * Interface for the administration of groups, group memberships and roles
 * inside of the groups in the authorization module.
 *
 * @author pasic
 */
public interface IGroupService {

  /**
   * Creates a new group and specifies an existing user who can manage the
   * group.
   *
   * The administrative and organizational informations for groups should be
   * managed outside of the authorization module.
   *
   * @param newGroup groupId of the group to create
   * @param groupManager userId of the user who will be able to manage this
   * group
   * @param authCtx the <code>IAuthorizationContext</code> used to authorize
   * this operation
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   * @throws EntityAlreadyExistsException if the group already exists, the group
   * will remain unchanged and no user with role MANAGER will be added.
   * @throws EntityNotFoundException If the group manager could not be found.
   */
  void create(GroupId newGroup, UserId groupManager, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException, EntityAlreadyExistsException;

  /**
   * Removes the group and all related references.
   *
   * @param groupId groupId of the group to remove
   * @param authCtx <code>IAuthorizationContext</code> used to authorize this
   * operation
   *
   * @return TRUE if the group has been removed.
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   * @throws EntityNotFoundException If the group was not found.
   */
  boolean remove(GroupId groupId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Adds an existing user with the specified in-group role to a group.
   *
   * The semantics of the in-group role follows from the definition of the
   * effective role. The effective role of the user userId acting as an member
   * of the group groupId is computed in the following way:
   *
   * <tt>
   * effectiveRoleForResourceId := min( max(
   * ResourceService#listExtraGrant(resourceId, userId), min(
   * ResourceService#listReference(resourceId).getRoleRestriction(),
   * GroupService#listRole(gruopId, userId) ) ),
   * UserService#listRoleRestriction(userId) );
   * </tt>
   *
   * @param groupId The if of the group to add the user to.
   * @param userId The id of the user to add.
   * @param role The in-group role of the user.
   * @param authCtx the <code>IAuthorizationContext</code> used to authorize
   * this operation
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   * @throws EntityNotFoundException If the group or the user does not exist.
   * @throws EntityAlreadyExistsException If the user is already member of the
   * group.
   */
  void addUser(GroupId groupId, UserId userId, Role role, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException, EntityAlreadyExistsException;

  /**
   * Removes an user from a group. The user still exists after this operation,
   * even if the removed group was the only membership of the user.
   *
   * @param groupId The id of the group to remove the user from.
   * @param userId The id of the user to remove.
   * @param authCtx the <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   * @throws EntityNotFoundException If the group or the user could not be
   * found.
   */
  void removeUser(GroupId groupId, UserId userId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Returns the in-group role for a user of a group.
   *
   * The semantics of the in-group role follow from the definition of the
   * effective role. The effective role of the user <tt>userId</tt> acting as an
   * member of the group <tt>groupId</tt> is computed in the following way:
   *
   * <tt>
   * effectiveRoleForResourceId := min( max(
   * ResourceService#listExtraGrant(resourceId, userId), min(
   * ResourceService#listReference(resourceId).getRoleRestriction(),
   * GroupService#listRole(gruopId, userId) ) ),
   * UserService#listRoleRestriction(userId) );
   * </tt>
   *
   * @param groupId The id of the group to which the user belongs.
   * @param userId The id of the user which restriction should be retrieved.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return The in-group role of the user.
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   * @throws EntityNotFoundException If the group or the user could not be
   * found.
   */
  IRoleRestriction getMaximumRole(GroupId groupId, UserId userId, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Changes the in-group role for a user of a group and returns the previous
   * role. The semantics of the in-group role follow from the definition of the
   * effective role. The effective role of the user <tt>userId</tt> acting as an
   * member of the group <tt>groupId</tt> is computed in the following way:
   *
   * <tt>
   * effectiveRoleForResourceId := min( max(
   * ResourceService#listExtraGrant(resourceId, userId), min(
   * ResourceService#listReference(resourceId).getRoleRestriction(),
   * GroupService#listRole(gruopId, userId) ) ),
   * UserService#listRoleRestriction(userId) );
   * </tt>
   *
   * @param groupId The id of the group where the user's role change should
   * happen.
   * @param userId The id of the user whose role in this group should be
   * modified.
   * @param newRole The new role of the user in this group.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation
   *
   * @return The previous role of the user in this group.
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   * @throws EntityNotFoundException If the group or the user could not be
   * found.
   */
  Role changeRole(GroupId groupId, UserId userId, Role newRole, IAuthorizationContext authCtx)
          throws UnauthorizedAccessAttemptException, EntityNotFoundException;

  /**
   * Returns a list of groups.
   *
   * @param first The first index.
   * @param results The max. number of results.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return A list of group ids.
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   */
  List<GroupId> getAllGroupsIds(int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException;

  /**
   * Returns a list of users which are members in the specified group.
   *
   * @param groupId The id of the group whose members are returned.
   * @param first The first index.
   * @param results The max. number of results.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return A list of members of the given group.
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   */
  List<UserId> getUsersIds(GroupId groupId, int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException;

  /**
   * Returns a list of users which are manager in the specified group.
   *
   * @param groupId the id of the group whose managers are returned.
   * @param first The first index.
   * @param results The max. number of results.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation.
   *
   * @return A list of managers of the given group.
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   */
  List<UserId> getGroupManagers(GroupId groupId, int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException;

  /**
   * Returns a list of groups in which the user is a member.
   *
   * @param userId The id of the user whose group memberships are returned.
   * @param first The first index.
   * @param results The max. number of results.
   * @param authCtx The <code>IAuthorizationContext</code> used to authorize
   * this operation
   *
   * @return A list of groups in which the user is a member.
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   */
  List<GroupId> membershipsOf(UserId userId, int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException;

  /**
   * Returns a list of groups in which the user identified by the argument is a
   * member and has a role equal or greater than the specified
   * <tt>minimumRole</tt>. This means he can access resources trough his group
   * membership which require a role equivalent or higher than the
   * <tt>minimumRole</tt> specified if he isn't restricted by his own
   * <tt>maximumRole</tt>.
   *
   * @param userId The id of the user whose group memberships are returned.
   * @param minimumRole The minimum role required.
   * @param first The first index.
   * @param results The max. number of results.
   * @param authCtx the <code>IAuthorizationContext</code> used to authorize
   * this operation
   *
   * @return A list of groups in which the user is a member and has at least
   * <tt>minimumRole</tt>
   *
   * @throws UnauthorizedAccessAttemptException If the context is not authorized
   * to call this method.
   */
  List<GroupId> membershipsOf(UserId userId, Role minimumRole, int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException;
}
