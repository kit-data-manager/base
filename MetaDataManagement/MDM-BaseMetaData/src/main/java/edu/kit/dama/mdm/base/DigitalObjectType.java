/*
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.mdm.base;

import edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectType;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @author mf6319
 */
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"typeDomain", "identifier", "version"})
)
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
                @XmlNamedAttributeNode("typeDomain"),
                @XmlNamedAttributeNode("identifier"),
                @XmlNamedAttributeNode("version"),
                @XmlNamedAttributeNode("description")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "DigitalObjectType.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")}),
    @NamedEntityGraph(
            name = "DigitalObjectType.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("typeDomain"),
                @NamedAttributeNode("identifier"),
                @NamedAttributeNode("version"),
                @NamedAttributeNode("description")
            })
})
public class DigitalObjectType implements Serializable, IDefaultDigitalObjectType, FetchGroupTracker {

    private static final long serialVersionUID = -3295299648568555345L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String typeDomain;
    @Column(nullable = false)
    private String identifier;
    @Column(nullable = false)
    private Integer version = 1;
    @Column(length = 255)
    private String description;

    public DigitalObjectType() {
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id The Id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getTypeDomain() {
        return typeDomain;
    }

    /**
     * Set the type domain.
     *
     * @param typeDomain The type domain.
     */
    public void setTypeDomain(String typeDomain) {
        this.typeDomain = typeDomain;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the type identifier.
     *
     * @param identifier The type identifier.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    /**
     * Set the type version.
     *
     * @param version The type version.
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Set the type description.
     *
     * @param description The type description.
     */
    public void setDescription(String description) {
        this.description = description;
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
