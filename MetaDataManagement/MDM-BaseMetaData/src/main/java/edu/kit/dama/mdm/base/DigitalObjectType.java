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

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

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
public class DigitalObjectType implements Serializable {

  private static final long serialVersionUID = -3295299648568555345L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @Column(nullable = false)
  private String typeDomain;
  @Column(nullable = false)
  private String identifier;
  @Column(nullable = false)
  private int version = 1;
  @Column(length = 255)
  private String description;

  public DigitalObjectType() {
  }

  /**
   * Get the id.
   *
   * @return The Id.
   */
  public long getId() {
    return id;
  }

  /**
   * Set the id.
   *
   * @param id The Id.
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Get the type domain.
   *
   * @return The type domain.
   */
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

  /**
   * Get the type identifier.
   *
   * @return The type identifier.
   */
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

  /**
   * Get the type version.
   *
   * @return The type version.
   */
  public int getVersion() {
    return version;
  }

  /**
   * Set the type version.
   *
   * @param version The type version.
   */
  public void setVersion(int version) {
    this.version = version;
  }

  /**
   * Get the type description.
   *
   * @return The type description.
   */
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

}
