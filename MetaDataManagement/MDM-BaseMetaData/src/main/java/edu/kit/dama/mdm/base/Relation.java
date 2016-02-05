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
package edu.kit.dama.mdm.base;

import edu.kit.dama.mdm.base.interfaces.IDefaultRelation;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

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
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "Relation.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("relationId")}),
    @NamedEntityGraph(
            name = "Relation.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("relationId"),
                @NamedAttributeNode(value = "task", subgraph = "Relation.default.Task.simple"),
                @NamedAttributeNode(value = "organizationUnit", subgraph = "Relation.default.OrganizationUnit.simple")
            },
            subgraphs = {
                @NamedSubgraph(
                        name = "Relation.default.Task.simple",
                        attributeNodes = {
                            @NamedAttributeNode("taskId")}
                ),
                @NamedSubgraph(
                        name = "Relation.default.OrganizationUnit.simple",
                        attributeNodes = {
                            @NamedAttributeNode("organizationUnitId")}
                )
            })
})
public class Relation implements Serializable, IDefaultRelation, FetchGroupTracker {

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
    @Override
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

    @Override
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

    @Override
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
    private transient org.eclipse.persistence.queries.FetchGroup fg;
    private transient Session sn;

    @Override
    public org.eclipse.persistence.queries.FetchGroup _persistence_getFetchGroup() {
        return this.fg;
    }

    @Override
    public void _persistence_setFetchGroup(org.eclipse.persistence.queries.FetchGroup fg) {
        this.fg = fg;
    }

    @Override
    public boolean _persistence_isAttributeFetched(String string) {
        return true;
    }

    @Override
    public void _persistence_resetFetchGroup() {
    }

    @Override
    public boolean _persistence_shouldRefreshFetchGroup() {
        return false;
    }

    @Override
    public void _persistence_setShouldRefreshFetchGroup(boolean bln) {

    }

    @Override
    public Session _persistence_getSession() {

        return sn;
    }

    @Override
    public void _persistence_setSession(Session sn) {
        this.sn = sn;

    }
}
