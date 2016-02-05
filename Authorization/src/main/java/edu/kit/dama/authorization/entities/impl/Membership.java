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
package edu.kit.dama.authorization.entities.impl;

import edu.kit.dama.authorization.entities.Role;
import java.io.Serializable;
import javax.persistence.*;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;

/**
 *
 * @author pasic
 */
@Entity(name = "Memberships")
@Table(name = "Memberships")
public class Membership implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @ManyToOne
  @JoinColumn(nullable = false)
  @BatchFetch(BatchFetchType.EXISTS)
  private User user;
  @ManyToOne
  @JoinColumn(nullable = false)
  private Group group;
  @Column(name = "membersRole")
  private Role role;

  /**
   * Default constructor.
   *
   * @param user The user of the membership.
   * @param role The role of the user.
   * @param group The group of the membership.
   */
  public Membership(User user, Role role, Group group) {
    this.user = user;
    this.role = role;
    this.group = group;
  }

  /**
   * Default constructor.
   */
  public Membership() {
  }

  /**
   * Get the membership id.
   *
   * @return The id.
   */
  public long getId() {
    return id;
  }

  /**
   * Set the membership id.
   *
   * @param id The id.
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Get the user.
   *
   * @return The user.
   */
  public User getUser() {
    return user;
  }

  /**
   * Set the user.
   *
   * @param user The user.
   */
  public void setUser(User user) {
    this.user = user;
  }

  /**
   * Get the role.
   *
   * @return The role.
   */
  public Role getRole() {
    return role;
  }

  /**
   * Set the role.
   *
   * @param role The role.
   */
  public void setRole(Role role) {
    this.role = role;
  }

  /**
   * Get the group.
   *
   * @return The group.
   */
  public Group getGroup() {
    return group;
  }

  /**
   * Set the group.
   *
   * @param group The group.
   */
  public void setGroup(Group group) {
    this.group = group;
  }

  @Override
  public String toString() {
    return "Membership{" + "id=" + id + '}';
  }
}
