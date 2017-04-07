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

import edu.kit.dama.authorization.entities.IDefaultSecurableResource;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
            })
    ,
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("id")
                ,
                @XmlNamedAttributeNode("domainId")
                ,
                @XmlNamedAttributeNode("domainUniqueId")
                ,
                @XmlNamedAttributeNode(value = "resourceReferences", subgraph = "simple")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "Resources")
@Table(name = "Resources", uniqueConstraints = @UniqueConstraint(columnNames = {"domainUniqueId", "domainId"}))
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "SecurableResource.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")
                ,
                @NamedAttributeNode("domainUniqueId")
                ,
                @NamedAttributeNode("domainId")
                ,
                @NamedAttributeNode(value = "resourceReferences", subgraph = "SecurableResource.simple.ResourceReferences.simple")
                ,
                @NamedAttributeNode(value = "grantSet", subgraph = "SecurableResource.simple.GrantSet.simple")
            },
            subgraphs = {
                @NamedSubgraph(
                        name = "SecurableResource.simple.ResourceReferences.simple",
                        attributeNodes = {
                            @NamedAttributeNode("id")}
                )
                ,
                @NamedSubgraph(
                        name = "SecurableResource.simple.GrantSet.simple",
                        attributeNodes = {
                            @NamedAttributeNode("id")}
                )
            })
})
public class SecurableResource implements IDefaultSecurableResource, ISecurableResource, Serializable, FetchGroupTracker {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String domainUniqueId;
    @Column(nullable = false)
    private String domainId;
    @OneToMany(mappedBy = "resource")
    @XmlElementWrapper(name = "resourceReferences")
    @XmlElement(name = "resourceReference")
    private List<ResourceReference> resourceReferences;
    @OneToOne(orphanRemoval = true)
    @XmlTransient
    @JoinColumn(nullable = false)
    private GrantSet grantSet;

    /**
     * Default constructor.
     *
     */
    public SecurableResource() {
    }

    /**
     * Default constructor.
     *
     * @param domainUniqueId The unique id of this resource within the resource
     * domain, e.g. some auto-generated UUID.
     * @param domainId The domain id of the resource, e.g. the class name.
     */
    public SecurableResource(String domainUniqueId, String domainId) {
        this.domainUniqueId = domainUniqueId;
        this.domainId = domainId;
    }

    /**
     * Default constructor.
     *
     * @param resourceId The resource id including domain id and unique id.
     */
    public SecurableResource(SecurableResourceId resourceId) {
        this.domainId = resourceId.getDomain();
        this.domainUniqueId = resourceId.getDomainUniqueId();
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
    public String getDomainId() {
        return domainId;
    }

    @Override
    public String getDomainUniqueId() {
        return domainUniqueId;
    }

    @Override
    public SecurableResourceId getSecurableResourceId() {
        return new SecurableResourceId(domainId, domainUniqueId);
    }

    /**
     * Set the resource id.
     *
     * @param resourceId The resource id.
     */
    public void setSecurableResourceId(SecurableResourceId resourceId) {
        this.domainUniqueId = resourceId.getDomainUniqueId();
        this.domainId = resourceId.getDomain();
    }

    @Override
    public List<ResourceReference> getResourceReferences() {
        if (null == resourceReferences) {
            resourceReferences = new ArrayList<>();
        }
        return resourceReferences;
    }

    /**
     * Set the resource references.
     *
     * @param resourceReferences The resource references.
     */
    public void setResourceReferences(List<ResourceReference> resourceReferences) {
        this.resourceReferences = resourceReferences;
    }

    /**
     * get the grant set.
     *
     * @return The grant set.
     */
    public GrantSet getGrantSet() {
        return grantSet;
    }

    /**
     * Set the grant set.
     *
     * @param grantSet The grant set.
     */
    public void setGrantSet(GrantSet grantSet) {
        this.grantSet = grantSet;
    }

    @Override
    public String toString() {
        return "Resource{" + "id=" + id + ", resourceId=" + domainUniqueId + "," + domainId + "}";
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
