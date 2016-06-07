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
package edu.kit.dama.authorization.entities;

/**
 *
 * @author pasic
 */
public enum Role implements IRoleRestriction<Role> {

  /**
   *0
   */
  NO_ACCESS,
  /**
   *1
   */
  MEMBERSHIP_REQUESTED,
  /**
   *2
   */
  UNALLOC_02,
  /**
   *3
   */
  GUEST,
  /**
   *4
   */
  UNALLOC_04,
  /**
   *5
   */
  UNALLOC_05,
  /**
   *6
   */
  MEMBER,
  /**
   *7
   */
  UNALLOC_07,
  /**
   *8
   */
  UNALLOC_08,
  /**
   *9
   */
  MANAGER,
  /**
   *10
   */
  UNALLOC_10,
  /**
   *11
   */
  UNALLOC_11,
  /**
   *12
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
