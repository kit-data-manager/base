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
package edu.kit.dama.mdm.dataorganization.entity.core;

/**
 * Stores various kinds of information in key-value pair fashion related to some
 * IDataOrganizationNode.
 *
 * @author pasic
 */
public interface IAttribute extends Cloneable {

  /**
   * Gets the attribute key, which indicates how the attribute value should be
   * interpreted.
   *
   * @return The key.
   */
  String getKey();

  /**
   * Sets the attribute key.
   *
   * @param key The key to set.
   */
  void setKey(String key);

  /**
   * Gets the attribute value.
   *
   * @return The value.
   */
  String getValue();

  /**
   * Sets the attribute value.
   *
   * @param value Attribute value to set.
   */
  void setValue(String value);

  /**
   * Clones the attribute.
   *
   * @return a deep copy of the attribute object
   *
   * @throws CloneNotSupportedException Clone not supported.
   */
  IAttribute clone() throws CloneNotSupportedException;

}
