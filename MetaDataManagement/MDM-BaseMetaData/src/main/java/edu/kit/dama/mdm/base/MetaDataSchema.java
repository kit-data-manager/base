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

import edu.kit.dama.mdm.base.interfaces.IDefaultMetaDataSchema;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 * All possible meta data schemas of a digital object.
 *
 * @author hartmann-v
 */
@Entity
//@XmlNamedObjectGraphs({
//    @XmlNamedObjectGraph(
//            name = "simple",
//            attributeNodes = {
//                @XmlNamedAttributeNode("id")
//            }),
//    @XmlNamedObjectGraph(
//            name = "default",
//            attributeNodes = {
//                @XmlNamedAttributeNode("id"),
//                @XmlNamedAttributeNode("schemaIdentifier"),
//                @XmlNamedAttributeNode("metaDataSchemaUrl")
//            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "MetaDataSchema.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")}),
    @NamedEntityGraph(
            name = "MetaDataSchema.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("schemaIdentifier"),
                @NamedAttributeNode("metaDataSchemaUrl")
            })
})
public class MetaDataSchema implements Serializable, IDefaultMetaDataSchema, FetchGroupTracker {

    /**
     * UID should be the date of the last change in the format yyyyMMdd.
     */
    private static final long serialVersionUID = 20111201L;
    // <editor-fold defaultstate="collapsed" desc="declaration of variables">
    /**
     * Primary key of the schema.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique identifier of the metadata schema.
     */
    @Column(nullable = false, unique = true)
    private String schemaIdentifier = null;

    /**
     * MetaDataSchema URL pointing to the schema.
     */
    @Column(nullable = false)
    private String metaDataSchemaUrl;

    /**
     * The metadata namespace.
     */
    @Column(nullable = false)
    private String namespace = null;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="constructors">
    /**
     * Default constructor. Not accessible from outside.
     */
    public MetaDataSchema() {
        // this constructor is useless so it's forbidden for the users.
    }

    /**
     * Constructor only allow to set identifier. This constructor is intended to
     * be used for search queries by identifier.
     *
     * @param schemaIdentifier The identifiert which may be used as/like a
     * namespace.
     */
    public MetaDataSchema(String schemaIdentifier) {
        this(schemaIdentifier, "", "");
    }

    /**
     * Create a new MetaDataSchema.
     *
     * @param schemaIdentifier The identifiert which may be used as/like a
     * namespace.
     * @param metaDataSchemaUrl Url of the meta data schema.
     */
    public MetaDataSchema(String schemaIdentifier, String metaDataSchemaUrl) {
        this(schemaIdentifier, "", metaDataSchemaUrl);

    }

    /**
     * Create a new MetaDataSchema.
     *
     * @param schemaIdentifier The identifiert which may be used as/like a
     * namespace.
     * @param namespace The namespace.
     * @param metaDataSchemaUrl Url of the meta data schema.
     */
    public MetaDataSchema(String schemaIdentifier, String namespace, String metaDataSchemaUrl) {
        this.schemaIdentifier = schemaIdentifier;
        this.namespace = namespace;
        this.metaDataSchemaUrl = metaDataSchemaUrl;

    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="setters and getters">
    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set id of the meta data schema.
     *
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    /**
     * Set the schema identifier. This identifier may be used/look like a
     * namespace.
     *
     * @param schemaIdentifier The identifier.
     */
    public void setSchemaIdentifier(String schemaIdentifier) {
        this.schemaIdentifier = schemaIdentifier;
    }

    public String getNamespace() {
        return namespace;
    }

    /**
     * Set the namespace.
     *
     * @param namespace The namespace.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Get the namespace.
     *
     * @return The namespace.
     */
    @Override
    public String getMetaDataSchemaUrl() {
        return metaDataSchemaUrl;
    }

    /**
     * Set meta data schema Url.
     *
     * @param metaDataSchemaUrl the meta data schema Url to set
     */
    public void setMetaDataSchemaUrl(final String metaDataSchemaUrl) {
        this.metaDataSchemaUrl = metaDataSchemaUrl;
    }
    // </editor-fold>

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("MetaDataSchema\n----\n");
        buffer.append("MetaDataSchema ID: ").append(getId()).append(" --- ").append(getSchemaIdentifier()).append(" --- ").append(getNamespace()).append(" --- ").append(getMetaDataSchemaUrl());

        return buffer.toString();
    }

    @Override
    public boolean equals(Object other) {
        boolean equals = true;
        if (this == other) {
            return equals;
        }
        if (other != null && (getClass() == other.getClass())) {
            MetaDataSchema otherMetaDataSchema = (MetaDataSchema) other;
            if (id != null) {
                equals = equals && (id.equals(otherMetaDataSchema.id));
            } else {
                equals = equals && (otherMetaDataSchema.id == null);
            }
            if (equals && (metaDataSchemaUrl != null)) {
                equals = equals && (metaDataSchemaUrl.equals(otherMetaDataSchema.metaDataSchemaUrl));
            } else {
                equals = equals && (otherMetaDataSchema.metaDataSchemaUrl == null);
            }
            if (equals && (namespace != null)) {
                equals = equals && (namespace.equals(otherMetaDataSchema.namespace));
            } else {
                equals = equals && (otherMetaDataSchema.namespace == null);
            }
        } else {
            equals = false;
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + (this.schemaIdentifier != null ? this.schemaIdentifier.hashCode() : 0);
        hash = 37 * hash + (this.metaDataSchemaUrl != null ? this.metaDataSchemaUrl.hashCode() : 0);
        hash = 37 * hash + (this.namespace != null ? this.namespace.hashCode() : 0);
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
