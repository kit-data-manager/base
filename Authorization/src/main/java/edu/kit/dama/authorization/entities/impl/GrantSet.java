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
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 *
 * @author mf6319
 */

//XML simple -> Grant.id
//XML default -> all grant

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
            @XmlNamedAttributeNode("resource"),
            @XmlNamedAttributeNode("roleRestriction"),
            @XmlNamedAttributeNode(value = "grants", subgraph = "simple")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "GrantSets")
@Table(name = "GrantSets")
public class GrantSet implements Serializable {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @OneToOne(/*orphanRemoval = true*/)
  private SecurableResource resource;
  private Role roleRestriction;
  @OneToMany(mappedBy = "grants", orphanRemoval = true)
  @XmlElementWrapper(name = "grants")
  @XmlElement(name = "grant")
  private List<Grant> grants = new ArrayList<Grant>();

  /**
   * Default constructor.
   */
  public GrantSet() {
  }

  /**
   * Default constructor.
   *
   * @param resource The resource.
   * @param roleRestriction The role restriction of this grant.
   */
  public GrantSet(SecurableResource resource, Role roleRestriction) {
    this.resource = resource;
    this.roleRestriction = roleRestriction;
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
   * Get the resource.
   *
   * @return The resource.
   */
  public SecurableResource getResource() {
    return resource;
  }

  /**
   * Set the resource.
   *
   * @param resource The resource.
   */
  public void setResource(SecurableResource resource) {
    this.resource = resource;
  }

  /**
   * Get the role restriction.
   *
   * @return The role restriction.
   */
  public Role getRoleRestriction() {
    return roleRestriction;
  }

  /**
   * Set the role restriction.
   *
   * @param roleRestriction The role restriction.
   */
  public void setRoleRestriction(Role roleRestriction) {
    this.roleRestriction = roleRestriction;
  }

  /**
   * Get all grants.
   *
   * @return All grants.
   */
  public List<Grant> getGrants() {
    return grants;
  }

  /**
   * Set a list of grants.
   *
   * @param grants A list of grants.
   */
  public void setGrants(List<Grant> grants) {
    this.grants = grants;
  }
//
//    public static GrantSet getGrantSetByResource(SecurableResource resource) {
//        EntityManager em = PU.entityManager();
//
//        CriteriaBuilder cb = em.getCriteriaBuilder();
//        CriteriaQuery<GrantSet> cq = em.getCriteriaBuilder().createQuery(GrantSet.class);
//        Root<GrantSet> grantSet = cq.from(GrantSet.class);
//        cq.select(grantSet).where(cb.equal(grantSet.get("resource"), resource));
//        TypedQuery<GrantSet> q = em.createQuery(cq);
//        GrantSet resultGrantSet = q.getSingleResult();
//
//        em.close();
//
//        return resultGrantSet;
//    }

//    public static GrantSet getGrantSetByResourceId(ResourceId resourceId) {
//        EntityManager em = PU.entityManager();
//
//        CriteriaBuilder cb = em.getCriteriaBuilder();
//        CriteriaQuery<GrantSet> cq = em.getCriteriaBuilder().createQuery(GrantSet.class);
//        Root<GrantSet> grantSet = cq.from(GrantSet.class);
//        cq.select(grantSet).where(cb.equal(grantSet.get("resource").get("resourceId"), resourceId.asString()));
//        TypedQuery<GrantSet> q = em.createQuery(cq);
//        GrantSet resultGrantSet = q.getSingleResult();
//
//        em.close();
//
//        return resultGrantSet;
//    }
  @Override
  public String toString() {
    return "GrantSet{" + "id=" + id + '}';
  }
}
