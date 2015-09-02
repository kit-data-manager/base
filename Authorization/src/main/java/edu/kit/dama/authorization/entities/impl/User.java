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
package edu.kit.dama.authorization.entities.impl;

import edu.kit.dama.authorization.entities.Role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;

/**
 *
 * @author mf6319
 */
@XmlNamedObjectGraph(
        name = "simple",
        attributeNodes = {
          @XmlNamedAttributeNode("userId")
        })
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "Users")
@Table(name = "Users")
public class User implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @Column(unique = true, nullable = false)
  private String userId;
  private Role maximumRole;
  @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
  private List<Membership> memberships = new ArrayList<Membership>();

  /**
   * Default constructor.
   *
   */
  public User() {
  }

  /**
   * Default constructor.
   *
   * @param userId The user id.
   * @param maximumRole The maximum role.
   */
  public User(String userId, Role maximumRole) {
    this.userId = userId;
    this.maximumRole = maximumRole;
  }

  /**
   * Get the id.
   *
   * @return The id.
   */
  public long getId() {
    return id;
  }

  /**
   * Set the id.
   *
   * @param id The id.
   */
  public void setId(long id) {
    this.id = id;
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
   * Set the user id.
   *
   * @param userId The user id.
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Get the maximum role.
   *
   * @return The maximum role.
   */
  public Role getMaximumRole() {
    return maximumRole;
  }

  /**
   * Set the maximum role.
   *
   * @param maximumRole The maximum role.
   */
  public void setMaximumRole(Role maximumRole) {
    this.maximumRole = maximumRole;
  }

  /**
   * Get the group memberships.
   *
   * @return Teh group memberships.
   */
  public List<Membership> getMemberships() {
    return memberships;
  }

  /**
   * Set the group memberships.
   *
   * @param memberships The group memberships.
   */
  public void setMemberships(List<Membership> memberships) {
    this.memberships = memberships;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final User other = (User) obj;
    if ((this.userId == null) ? (other.userId != null) : !this.userId.equals(other.userId)) {
      return false;
    }
    if (this.maximumRole != other.maximumRole) {
      return false;
    }
    return this.memberships == other.memberships || (this.memberships != null && this.memberships.equals(other.memberships));
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + (this.userId != null ? this.userId.hashCode() : 0);
    hash = 59 * hash + (this.maximumRole != null ? this.maximumRole.hashCode() : 0);
    hash = 59 * hash + (this.memberships != null ? this.memberships.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "User{" + "id=" + id + ", userId=" + userId + '}';
  }
}
