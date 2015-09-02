/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 *
 * @author mf6319
 */
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "simple",
          attributeNodes = {
            @XmlNamedAttributeNode("id")
          }),
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode("id"),
            @XmlNamedAttributeNode(value = "grantee", subgraph = "simple"),
            @XmlNamedAttributeNode("grantedRole")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "Grants")
@Table(name = "Grants")
public class Grant implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @ManyToOne(cascade = CascadeType.ALL)
  @BatchFetch(BatchFetchType.EXISTS)
  private User grantee;
  private Role grantedRole;
  @ManyToOne
  @XmlTransient
  private GrantSet grants;

  /**
   * Default constructor.
   *
   * @param grantee The user of this grant.
   * @param grantedRole The granted role.
   * @param grants The set of grants.
   */
  public Grant(User grantee, Role grantedRole, GrantSet grants) {
    this.grantee = grantee;
    this.grantedRole = grantedRole;
    this.grants = grants;
  }

  /**
   * Default constructor.
   *
   */
  public Grant() {
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
   * Get the grantee.
   *
   * @return The grantee.
   */
  public User getGrantee() {
    return grantee;
  }

  /**
   * Set the grantee.
   *
   * @param grantee The grantee.
   */
  public void setGrantee(User grantee) {
    this.grantee = grantee;
  }

  /**
   * Get the granted role.
   *
   * @return The granted role.
   */
  public Role getGrantedRole() {
    return grantedRole;
  }

  /**
   * Set the granted role.
   *
   * @param grantedRole The granted role.
   */
  public void setGrantedRole(Role grantedRole) {
    this.grantedRole = grantedRole;
  }

  /**
   * Get the grant set.
   *
   * @return The grant set.
   */
  public GrantSet getGrants() {
    return grants;
  }

  /**
   * Set the grant set.
   *
   * @param grants The grant set.
   */
  public void setGrants(GrantSet grants) {
    this.grants = grants;
  }

  @Override
  public String toString() {
    return "Grant{" + "id=" + id + "}";
  }
}
