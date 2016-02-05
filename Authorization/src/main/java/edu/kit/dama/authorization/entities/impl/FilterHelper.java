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

import edu.kit.dama.authorization.entities.util.FilterHelperId;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import org.eclipse.persistence.annotations.ReadOnly;

/**
 * This JPA entity is an utility entity which should be used to interface to a
 * DB view. The view should state the access privilege for each security context
 * (user and group) and each resource (which is represented by its domainId and
 * its domainUniqueId together).
 *
 * @author pasic
 */
@Entity
@ReadOnly
@Table(name = "FilterHelper")
@IdClass(FilterHelperId.class)
public class FilterHelper implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @Column(name = "USERID")
  private String userId;
  @Id
  @Column(name = "GROUPID")
  private String groupId;
  @Id
  @Column(name = "DOMAINID")
  private String domainId;
  @Id
  @Column(name = "DOMAINUNIQUEID")
  private String domainUniqueId;
  @Column(name = "POSSESSED_ROLE")
  private int roleAllowed;

  /**
   * Get the domain id.
   *
   * @return The domain id.
   */
  public String getDomainId() {
    return domainId;
  }

  /**
   * Get the domain unique id.
   *
   * @return The domain unique id.
   */
  public String getDomainUniqueId() {
    return domainUniqueId;
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
   * Get the allowed role.
   *
   * @return The allowed role.
   */
  public int getRoleAllowed() {
    return roleAllowed;
  }

  /**
   * Get the user id.
   *
   * @return The user id.
   */
  public String getUserId() {
    return userId;
  }

}
