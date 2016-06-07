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

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 * This class
 *
 * @author pasic
 */
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("resourceId")
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("resourceId"),
                @XmlNamedAttributeNode(value = "groupId", subgraph = "simple")
            })})
public class ReferenceId implements IDefaultReferenceId, ISecurableResource {

    private SecurableResourceId resourceId;
    private GroupId groupId;

    /**
     * Default constructor. Only internally used.
     */
    public ReferenceId() {
    }

    /**
     * Create a new reference id for a resource belonging to a group.
     *
     * @param resourceId The id of the resource.
     * @param groupId The id of the group.
     */
    public ReferenceId(SecurableResourceId resourceId, GroupId groupId) {
        this.resourceId = resourceId;
        this.groupId = groupId;
    }

    @Override
    public final GroupId getGroupId() {
        return groupId;
    }

    /**
     * Set the value of groupId
     *
     * @param groupId new value of groupId
     */
    public final void setGroupId(GroupId groupId) {
        this.groupId = groupId;
    }

    /**
     * Get the value of resourceId
     *
     * @return the value of resourceId
     */
    @Override
    public final SecurableResourceId getResourceId() {
        return resourceId;
    }

    /**
     * Set the value of resourceId
     *
     * @param resourceId new value of resourceId
     */
    public final void setResourceId(SecurableResourceId resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ReferenceId other = (ReferenceId) obj;
        if (this.resourceId != other.resourceId && (this.resourceId == null || !this.resourceId.equals(other.resourceId))) {
            return false;
        }
        if (this.groupId != other.groupId && (this.groupId == null || !this.groupId.equals(other.groupId))) {
            return false;
        }
        return true;
    }

    @Override
    public final int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.resourceId != null ? this.resourceId.hashCode() : 0);
        hash = 29 * hash + (this.groupId != null ? this.groupId.hashCode() : 0);
        return hash;
    }

    @Override
    public final String toString() {
        return "ReferenceId{" + "resourceId=" + resourceId + ", groupId=" + groupId + '}';
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public final SecurableResourceId getSecurableResourceId() {
        return resourceId;
    }
}
