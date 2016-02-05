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
package edu.kit.dama.mdm.dataworkflow.properties;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author mf6319
 */
@Entity
@DiscriminatorValue("StringValue")
public class StringValueProperty extends ExecutionEnvironmentProperty {

  private static final long serialVersionUID = 8934287797040022484L;

  /**
   * The value of this property.
   */
  private String propertyValue = null;

  /**
   * Default constructor.
   */
  public StringValueProperty() {
  }

  /**
   * Default constructor.
   *
   * @param pValue The property value.
   */
  public StringValueProperty(String pValue) {
    propertyValue = pValue;
  }

  /**
   * Get the property value.
   *
   * @return The property value.
   */
  public String getPropertyValue() {
    return propertyValue;
  }

  /**
   * Set the property value.
   *
   * @param value The property value
   */
  public void setPropertyValue(String value) {
    this.propertyValue = value;
  }
}
