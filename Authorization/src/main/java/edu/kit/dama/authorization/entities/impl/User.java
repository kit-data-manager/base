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
package edu.kit.dama.authorization.entities.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.kit.dama.authorization.entities.IDefaultUser;
import edu.kit.dama.authorization.entities.Role;

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
                @XmlNamedAttributeNode("id")
                ,
                @XmlNamedAttributeNode("userId")
            })
    ,
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("id")
                ,
                @XmlNamedAttributeNode("userId")
                ,
                @XmlNamedAttributeNode("maximumRole")
            //Add memberships if necessary
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "Users")
@Table(name = "Users")
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "User.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")
                ,
                @NamedAttributeNode("maximumRole")
                ,
                @NamedAttributeNode("userId")
            })
    ,
    @NamedEntityGraph(
            name = "User.memberships",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")
                ,
                @NamedAttributeNode("userId")
                ,@NamedAttributeNode("maximumRole")
                ,
                @NamedAttributeNode(value = "memberships")
            })
})
public class User implements IDefaultUser, Serializable, FetchGroupTracker {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String userId;
    private Role maximumRole;
    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @XmlTransient
    @JsonIgnore
    private List<Membership> memberships = new ArrayList<>();

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

    @Override
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

    @Override
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
