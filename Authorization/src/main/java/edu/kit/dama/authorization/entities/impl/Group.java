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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @author mf6319
 */
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("groupId")
            })
    ,
  @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("groupId")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "Groups")
@Table(name = "Groups")
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "Group.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")
                ,
                @NamedAttributeNode("groupId")})
    ,
    @NamedEntityGraph(
            name = "Group.memberships",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")
                ,
                @NamedAttributeNode("groupId")
                ,
                @NamedAttributeNode(value = "memberships")
            })
})
public class Group implements Serializable, FetchGroupTracker {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(unique = true, nullable = false)
    private String groupId;
    @OneToMany(mappedBy = "group", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @XmlTransient
    private List<Membership> memberships = new ArrayList<>();
    @OneToMany(mappedBy = "group", orphanRemoval = true, fetch = FetchType.EAGER)
    @XmlTransient
    private List<ResourceReference> resourceReferences = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param groupId The group id.
     */
    public Group(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Default constructor.
     *
     */
    public Group() {
    }

    /**
     * Get the id.
     *
     * @return Teh id.
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
     * Get the group id.
     *
     * @return The group id.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set the group id.
     *
     * @param groupId Teh group id.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /**
     * Get all memberships.
     *
     * @return The memberships.
     */
    public List<Membership> getMemberships() {
        return memberships;
    }

    /**
     * Set memberships.
     *
     * @param memberships The memberships.
     */
    public void setMemberships(List<Membership> memberships) {
        this.memberships = memberships;
    }

    /**
     * Get a list of resource references.
     *
     * @return A list of resource references.
     */
    public List<ResourceReference> getResourceReferences() {
        return resourceReferences;
    }

    /**
     * Set a list of resource references.
     *
     * @param resourceReferences A list of resource references.
     */
    public void setResourceReferences(List<ResourceReference> resourceReferences) {
        this.resourceReferences = resourceReferences;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Group other = (Group) obj;
        if ((this.groupId == null) ? (other.groupId != null) : !this.groupId.equals(other.groupId)) {
            return false;
        }
        return this.memberships == other.memberships || (this.memberships != null && this.memberships.equals(other.memberships));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.groupId != null ? this.groupId.hashCode() : 0);
        hash = 47 * hash + (this.memberships != null ? this.memberships.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "Group{" + "id=" + id + ", groupId=" + groupId + "}";
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
