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
package edu.kit.dama.mdm.base;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 * Link an organization unit to a task.
 *
 * @author hartmann-v
 */
@Entity
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "simple",
          attributeNodes = {
            @XmlNamedAttributeNode("relationId")
          }),
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode("relationId"),
            @XmlNamedAttributeNode(value = "task", subgraph = "simple"),
            @XmlNamedAttributeNode(value = "organizationUnit", subgraph = "simple")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
public class Relation implements Serializable {

  /**
   * UID should be the date of the last change in the format yyyyMMdd.
   */
  private static final long serialVersionUID = 20111201L;
  // <editor-fold defaultstate="collapsed" desc="declaration of variables">
  /**
   * Identification number of the relation. primary key of the data set.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long relationId;
  /**
   * Task of the organization unit.
   */
  @ManyToOne(fetch = FetchType.EAGER)
  private Task task;
  /**
   * Organization Unit.
   */
  @ManyToOne(fetch = FetchType.EAGER)
  private OrganizationUnit organizationUnit;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="constructors">
  /**
   * Make default constructor inaccessible for others.
   */
  protected Relation() {
    // this constructor is useless so it's forbidden for the users.
  }

  /**
   * Build relation without any defined task.
   *
   * @param organizationUnit organization unit linked in this relation.
   */
  public Relation(OrganizationUnit organizationUnit) {
    this(organizationUnit, null);
  }

  /**
   * Build relation.
   *
   * @param organizationUnit organization unit linked in this relation.
   * @param task task of the organization unit.
   */
  public Relation(OrganizationUnit organizationUnit, Task task) {
    this.organizationUnit = organizationUnit;
    this.task = task;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="setters and getters">
  /**
   * Get relation id.
   *
   * @return the relationId
   */
  public Long getRelationId() {
    return relationId;
  }

  /**
   * Set relation id.
   *
   * @param relationId the relation id to set
   */
  public void setRelationId(final Long relationId) {
    this.relationId = relationId;
  }

  /**
   * Get task.
   *
   * @return the task
   */
  public Task getTask() {
    return task;
  }

  /**
   * Set task.
   *
   * @param task the task to set
   */
  public void setTask(final Task task) {
    this.task = task;
  }

  /**
   * The organization unit of this relation.
   *
   * @return the organizationUnit
   */
  public OrganizationUnit getOrganizationUnit() {
    return organizationUnit;
  }

  /**
   * Set the organization unit of this relation.
   *
   * @param organizationUnit the organizationUnit to set
   */
  public void setOrganizationUnit(OrganizationUnit organizationUnit) {
    this.organizationUnit = organizationUnit;
  }
  // </editor-fold>

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    if (organizationUnit != null) {
      buffer.append(organizationUnit.getOuName());
    }
    if (task != null) {
      buffer.append(" - ").append(task.getTask());
    }
    return buffer.toString();
  }

  @Override
  public boolean equals(Object other) {
    boolean equals = true;
    if (this == other) {
      return equals;
    }
    if (other != null && (getClass() == other.getClass())) {
      Relation otherRelation = (Relation) other;
      if (relationId != null) {
        equals = equals && (relationId.equals(otherRelation.relationId));
      } else {
        equals = equals && (otherRelation.relationId == null);
      }
      if (equals && (task != null)) {
        equals = equals && (task.equals(otherRelation.task));
      } else {
        equals = equals && (otherRelation.task == null);
      }
      if (equals && (organizationUnit != null)) {
        equals = equals && (organizationUnit.equals(otherRelation.organizationUnit));
      } else {
        equals = equals && (otherRelation.organizationUnit == null);
      }
    } else {
      equals = false;
    }

    return equals;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 59 * hash + (this.task != null ? this.task.hashCode() : 0);
    hash = 59 * hash + (this.organizationUnit != null ? this.organizationUnit.hashCode() : 0);
    return hash;
  }
}
