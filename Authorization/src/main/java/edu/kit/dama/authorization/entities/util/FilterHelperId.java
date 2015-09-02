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
package edu.kit.dama.authorization.entities.util;

import java.io.Serializable;

/**
 *
 * @author pasic
 */
public final class FilterHelperId implements Serializable {

  private static final long serialVersionUID = 1L;
  private String userId = null;
  private String groupId = null;
  private String domainId = null;
  private String domainUniqueId = null;

  /**
   * Set the user id.
   *
   * @param userId The user id.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Get the user id.
   *
   * @return The user id.
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Set the group id.
   *
   * @param groupId The group id.
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Get the group id.
   *
   * @return The group id.
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Set the domain id.
   *
   * @param domainId The domain id.
   */
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  /**
   * Get the domain id.
   *
   * @return The domain id.
   */
  public String getDomainId() {
    return domainId;
  }

  /**
   * Set the domain unique id.
   *
   * @param domainUniqueId The domain unique id.
   */
  public void setDomainUniqueId(String domainUniqueId) {
    this.domainUniqueId = domainUniqueId;
  }

  /**
   * Get the domain unique id.
   *
   * @return The domain unique id.
   */
  public String getDomainUniqueId() {
    return domainUniqueId;
  }

  @Override
  public boolean equals(Object obj) {
    FilterHelperId o;
    if (obj instanceof FilterHelperId) {
      o = (FilterHelperId) obj;
    } else {
      return false;
    }
    return (userId == null) ? false : userId.equals(o.userId)
            && (null == groupId) ? false : groupId.equals(o.groupId)
            && (null == domainId) ? false : domainId.equals(o.domainId)
            && (null == domainUniqueId) ? false : domainUniqueId.equals(o.domainUniqueId);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 73 * hash + (this.userId != null ? this.groupId.hashCode() : 0);
    hash = 73 * hash + (this.userId != null ? this.groupId.hashCode() : 0);
    hash = 73 * hash + (this.domainId != null ? this.domainId.hashCode() : 0);
    hash = 73 * hash + (this.domainUniqueId != null ? this.domainUniqueId.hashCode() : 0);
    return hash;
  }
}
