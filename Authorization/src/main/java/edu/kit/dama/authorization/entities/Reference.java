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

import edu.kit.dama.authorization.entities.util.PU;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author pasic
 */
public class Reference {

  private IRoleRestriction roleRestriction;
  private ReferenceId referenceId;

  /**
   * Defautl constructor. Only internally used.
   */
  public Reference() {
  }

  /**
   * Default constructor.
   *
   * @param roleRestriction The role restriction.
   * @param referenceId The reference id.
   */
  public Reference(IRoleRestriction roleRestriction, ReferenceId referenceId) {
    this.roleRestriction = roleRestriction;
    this.referenceId = referenceId;
  }

  /**
   * Get the value of referenceId
   *
   * @return the value of referenceId
   */
  public final ReferenceId getReferenceId() {
    return referenceId;
  }

  /**
   * Set the value of referenceId
   *
   * @param referenceId new value of referenceId
   */
  public final void setReferenceId(ReferenceId referenceId) {
    this.referenceId = referenceId;
  }

  /**
   * Get the value of roleRestriction
   *
   * @return the value of roleRestriction
   */
  public final IRoleRestriction getRoleRestriction() {
    return roleRestriction;
  }

  /**
   * Set the value of roleRestriction
   *
   * @param roleRestriction new value of roleRestriction
   */
  public final void setRoleRestriction(IRoleRestriction roleRestriction) {
    this.roleRestriction = roleRestriction;
  }

  /**
   * Obtain a reference by its id.
   *
   * @param referenceId The reference id.
   *
   * @return The reference with the provided id.
   */
  public final static Reference getReferenceByReferenceId(ReferenceId referenceId) {
    EntityManager em = PU.entityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Reference> cq = em.getCriteriaBuilder().createQuery(Reference.class);
    Root<Reference> reference = cq.from(Reference.class);
    cq.select(reference).where(cb.equal(reference.get("referenceId"), referenceId));
    TypedQuery<Reference> q = em.createQuery(cq);
    Reference result = q.getSingleResult();

    em.close();

    return result;
  }
}
