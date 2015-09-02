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
package edu.kit.dama.mdm.admin;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author jejkal
 */
@Entity
public class UserProperty {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String propertyKey;
  private String propertyValue;

  /**
   * Default constructor.
   */
  public UserProperty() {
  }

  /**
   * Default constructor.
   *
   * @param pKey Property key.
   * @param pValue Property value.
   */
  public UserProperty(String pKey, String pValue) {
    propertyKey = pKey;
    propertyValue = pValue;
  }

  /**
   * Set the id.
   *
   * @param id The id.
   */
  public void setId(Long id) {
    this.id = id;
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
   * Set the key.
   *
   * @param propertyKey The key.
   */
  public void setPropertyKey(String propertyKey) {
    this.propertyKey = propertyKey;
  }

  /**
   * Get the key.
   *
   * @return The key.
   */
  public String getPropertyKey() {
    return propertyKey;
  }

  /**
   * Set the value.
   *
   * @param propertyValue The value.
   */
  public void setPropertyValue(String propertyValue) {
    this.propertyValue = propertyValue;
  }

  /**
   * Get the value.
   *
   * @return The value.
   */
  public String getPropertyValue() {
    return propertyValue;
  }
}
