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
package edu.kit.dama.commons.types;

import java.io.Serializable;

/**
 * Access rights are depending on the DigitalObjectId. Public accessible Objects
 * has to reference a instance of this class. It has to hold a unique Id to
 * refer to a digital object. It may be used in 2 ways:
 * <ol> <li>unique id for the digital object</li>
 * <li>reference to link (meta) data to a digital object</li>
 * </ol>
 *
 * @author pasic
 */
public class DigitalObjectId implements Serializable {

  private static final long serialVersionUID = 1L;
  private String stringRepresentation;

  /**
   * Hidden constructor.
   */
  private DigitalObjectId() {
  }

  /**
   * Constructor for providing the string representation of this object.
   *
   * @param stringRepresentation The string representation.
   */
  public DigitalObjectId(String stringRepresentation) {
    if (stringRepresentation == null) {
      throw new IllegalArgumentException("Argument stringRepresentation must not be null.");
    }
    this.stringRepresentation = stringRepresentation.trim();
  }

  /**
   * Get the string representation of this object.
   *
   * @return The string representation.
   */
  public final String getStringRepresentation() {
    return stringRepresentation;
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof DigitalObjectId) {
      return null == stringRepresentation
              ? null == ((DigitalObjectId) obj).stringRepresentation
              : stringRepresentation.equals(((DigitalObjectId) obj).stringRepresentation);
    }
    return false;
  }

  @Override
  public final int hashCode() {
    int hash = 7;
    hash = 89 * hash + (this.stringRepresentation != null ? this.stringRepresentation.hashCode() : 0);
    return hash;
  }

  @Override
  public final String toString() {
    return getStringRepresentation();
  }
}
