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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.dataorganization.entity.impl.client;

import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;

/**
 *
 * @author pasic
 */
public final class Attribute implements IAttribute {

  /**
   * The key.
   */
  private String key;
  /**
   * The value.
   */
  private String value;

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
    clone.setKey(key);
    clone.setValue(value);
    return clone;
  }

}
