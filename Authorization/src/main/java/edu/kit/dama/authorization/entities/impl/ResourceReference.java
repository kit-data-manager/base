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

import edu.kit.dama.authorization.entities.IDefaultResourceReference;
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
                @XmlNamedAttributeNode("roleRestriction"),
                @XmlNamedAttributeNode(value = "group", subgraph = "simple")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "ResourceReferences")
@Table(name = "ResourceReferences", uniqueConstraints = @UniqueConstraint(columnNames = {"RESOURCE_ID", "GROUP_ID"}))
public class ResourceReference implements IDefaultResourceReference, Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ROLE_RESTRICTION")
    private Role roleRestriction;
    @ManyToOne(fetch = FetchType.EAGER)
    @XmlTransient
    @JoinColumn(nullable = false)
    @BatchFetch(value = BatchFetchType.EXISTS)
    private SecurableResource resource;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Group group;

    /**
     * Default constructor.
     */
    public ResourceReference() {
    }

    /**
     * Default constructor.
     *
     * @param roleRestriction The role restriction for this resource reference.
     * @param resource The resource to reference.
     * @param group The group which should be able to access the reference.
     */
    public ResourceReference(Role roleRestriction, SecurableResource resource, Group group) {
        this.roleRestriction = roleRestriction;
        this.resource = resource;
        this.group = group;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(Long id) {
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

    @Override
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ResourceReference other = (ResourceReference) obj;
        if (this.roleRestriction != other.roleRestriction) {
            return false;
        }
        if (this.resource != other.resource && (this.resource == null || !this.resource.equals(other.resource))) {
            return false;
        }
        return this.group == other.group || (this.group != null && this.group.equals(other.group));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.roleRestriction != null ? this.roleRestriction.hashCode() : 0);
        hash = 79 * hash + (this.resource != null ? this.resource.hashCode() : 0);
        hash = 79 * hash + (this.group != null ? this.group.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "ResourceReference{" + "id=" + id + "}";
    }
}
