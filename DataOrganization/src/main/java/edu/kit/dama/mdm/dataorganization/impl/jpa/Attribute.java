/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.dataorganization.impl.jpa;

import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;

/**
 *
 * @author pasic
 */
@Entity
@IdClass(AttributeId.class)
public class Attribute implements IAttribute, Serializable, Cloneable {

  private static final long serialVersionUID = 7526472295622776127L;

  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "attr_key")
  private String key;
  @Column(name = "attr_value")
  private String value;

  @Id
  @ManyToOne
  private DataOrganizationNode annotatedNode;

  /**
   * Default constructor.
   */
  public Attribute() {
  }

  /**
   * Default constructor for providing key and value of the attribute.
   *
   * @param key The attribute key.
   * @param value The attribute value.
   */
  public Attribute(String key, String value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Default constructor for copying the properties of another attribute.
   *
   * @param other The other attribute.
   */
  public Attribute(IAttribute other) {
    key = other.getKey();
    value = other.getValue();
  }

  /**
   * Get the id.
   *
   * @return The id.
   */
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
  public String getKey() {
    return key;
  }

  @Override
  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IAttribute) {
      IAttribute other = (IAttribute) obj;
      if (key.equals(other.getKey()) && value.equals(other.getValue())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 73 * hash + (this.key != null ? this.key.hashCode() : 0);
    hash = 73 * hash + (this.value != null ? this.value.hashCode() : 0);
    return hash;
  }

  @Override
  public Attribute clone() throws CloneNotSupportedException {
    Attribute clone = (Attribute) super.clone();
    clone.setId(id);
    clone.setKey(key);
    clone.setValue(value);
    return clone;
  }

  /**
   * Get the annotated node.
   *
   * @return The annotated node.
   */
  public DataOrganizationNode getAnnotatedNode() {
    return annotatedNode;
  }

  /**
   * Set the annotated node.
   *
   * @param annotatedNode The annotated node.
   */
  void setAnnotatedNode(DataOrganizationNode annotatedNode) {
    this.annotatedNode = annotatedNode;
  }

}
