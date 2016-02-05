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
package edu.kit.dama.authorization.entities.impl;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.util.Constants;

/**
 *
 * @author ochsenreither
 */
public class AuthorizationContext implements IAuthorizationContext {

  private UserId userId;
  private GroupId groupId;
  private Role roleRestriction;

  /**
   * Default constructor.
   *
   * @param userId The user id.
   * @param groupId The group id.
   * @param roleRestriction The role restriction.
   */
  public AuthorizationContext(UserId userId, GroupId groupId, Role roleRestriction) {
    this.userId = userId;
    this.groupId = groupId;
    this.roleRestriction = roleRestriction;
  }

  @Override
  public final void setUserId(UserId userId) {
    this.userId = userId;
  }

  @Override
  public final void setGroupId(GroupId groupId) {
    this.groupId = groupId;
  }

  @Override
  public final void setRoleRestriction(Role roleRestriction) {
    this.roleRestriction = roleRestriction;
  }

  @Override
  public final UserId getUserId() {
    return userId;
  }

  @Override
  public final GroupId getGroupId() {
    return groupId;
  }

  @Override
  public final Role getRoleRestriction() {
    return roleRestriction;
  }

  /**
   * Factory the system context which is authorized to do <b>everything</b>.
   * Therefore, this context should be used very carefully and only internally!
   *
   * @return The system context.
   */
  public static final AuthorizationContext factorySystemContext() {
    return new AuthorizationContext(new UserId(Constants.SYSTEM_ADMIN), new GroupId(Constants.SYSTEM_GROUP), Role.ADMINISTRATOR);
  }

  @Override
  public final String toString() {
    StringBuilder result = new StringBuilder();
    result.append("[");
    result.append((getUserId() != null) ? getUserId().getStringRepresentation() : "NoUser");
    result.append("|");
    result.append((getGroupId() != null) ? getGroupId().getStringRepresentation() : "NoGroup");
    result.append("|");
    result.append((getRoleRestriction() != null) ? getRoleRestriction() : "NoRole");
    result.append("]");
    return result.toString();
  }
}
