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
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @author pasic
 */
@Entity(name = "Memberships")
@Table(name = "Memberships")
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "Membership.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")
                ,
                @NamedAttributeNode("role")
                ,
                @NamedAttributeNode(value = "user", subgraph = "Membership.simple.User.simple")
                ,
                @NamedAttributeNode(value = "group", subgraph = "Membership.simple.Group.simple")
            },
            subgraphs = {
                @NamedSubgraph(
                        name = "Membership.simple.User.simple",
                        attributeNodes = {
                            @NamedAttributeNode("id")}
                )
                ,
                @NamedSubgraph(
                        name = "Membership.simple.Group.simple",
                        attributeNodes = {
                            @NamedAttributeNode("id")}
                )
            })
})
public class Membership implements Serializable, FetchGroupTracker {

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
