/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 * All possible meta data schemas of a digital object.
 *
 * @author hartmann-v
 */
@Entity
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
            @XmlNamedAttributeNode("schemaIdentifier"),
            @XmlNamedAttributeNode("metaDataSchemaUrl")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
public class MetaDataSchema implements Serializable {

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
   * MetaDataSchema. MetaDataSchema depends on participant (organization
   * unit/user) and the context. Nevertheless there will be a limited number of
   * possible metaDataSchemas.
   */
  private String metaDataSchemaUrl;
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
    this.schemaIdentifier = schemaIdentifier;
  }

  /**
   * Create a new MetaDataSchema.
   *
   * @param schemaIdentifier The identifiert which may be used as/like a
   * namespace.
   * @param metaDataSchemaUrl Url of the meta data schema.
   */
  public MetaDataSchema(String schemaIdentifier, String metaDataSchemaUrl) {
    this.schemaIdentifier = schemaIdentifier;
    this.metaDataSchemaUrl = metaDataSchemaUrl;

  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="setters and getters">
  /**
   * Get meta data schema id.
   *
   * @return the id
   */
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

  /**
   * Get the schema identifier. This identifier may be used/look like a
   * namespace.
   *
   * @return The identifier.
   */
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

  /**
   * Get meta data schema Url.
   *
   * @return the metaDataSchemaUrl
   */
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
    buffer.append("MetaDataSchema ID: ").append(getId()).append(" --- ").append(getSchemaIdentifier()).append(" --- ").append(getMetaDataSchemaUrl());

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
    return hash;
  }
}
