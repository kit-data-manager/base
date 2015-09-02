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
package edu.kit.dama.authorization.entities;

/**
 *
 * @author pasic
 */
public enum Role implements IRoleRestriction<Role> {

  /**
   *
   */
  NO_ACCESS,
  /**
   *
   */
  MEMBERSHIP_REQUESTED,
  /**
   *
   */
  UNALLOC_02,
  /**
   *
   */
  GUEST,
  /**
   *
   */
  UNALLOC_04,
  /**
   *
   */
  UNALLOC_05,
  /**
   *
   */
  MEMBER,
  /**
   *
   */
  UNALLOC_07,
  /**
   *
   */
  UNALLOC_08,
  /**
   *
   */
  MANAGER,
  /**
   *
   */
  UNALLOC_10,
  /**
   *
   */
  UNALLOC_11,
  /**
   *
   */
  ADMINISTRATOR;

  @Override
  public boolean atLeast(Role role) {
    return (this.compareTo(role) >= 0);
  }

  @Override
  public boolean atMost(Role role) {
    return (this.compareTo(role) <= 0);
  }

  @Override
  public boolean moreThan(Role role) {
    return (this.compareTo(role) > 0);
  }

  @Override
  public boolean lessThan(Role role) {
    return (this.compareTo(role) < 0);
  }

  /**
   * Get a list of valid (currently defined) roles.
   *
   * @return A list of currently defined roles.
   */
  public static Role[] getValidRoles() {
    return new Role[]{NO_ACCESS, GUEST, MEMBER, MANAGER, ADMINISTRATOR};
  }
}
