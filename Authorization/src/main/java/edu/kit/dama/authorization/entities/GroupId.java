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
package edu.kit.dama.authorization.entities;

/**
 *
 * @author pasic
 */
public class GroupId implements ISecurableResource {

  private String stringRepresentation;
  private static final String DOMAIN = "edu.kit.dama.authorization.groupid";

  /**
   * Default constructor.
   *
   */
  public GroupId() {
  }

  /**
   * Default constructor.
   *
   * @param stringRepresentation The string representation of this group id.
   */
  public GroupId(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  /**
   * Get the string representation.
   *
   * @return The string representation.
   */
  public String getStringRepresentation() {
    return stringRepresentation;
  }

  /**
   * Set the string representation.
   *
   * @param stringRepresentation The string representation.
   */
  public void setStringRepresentation(String stringRepresentation) {
    this.stringRepresentation = stringRepresentation;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + (this.stringRepresentation != null ? this.stringRepresentation.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof GroupId)) {
      return false;
    }
    GroupId other = (GroupId) obj;
    return null == stringRepresentation ? false : stringRepresentation.equals(other.stringRepresentation);
  }

  /**
   * Get the securable resource id.
   *
   * @return The securable resource id.
   */
  @Override
  public SecurableResourceId getSecurableResourceId() {
    return new SecurableResourceId(DOMAIN, stringRepresentation);
  }

  @Override
  public String toString() {
    return getStringRepresentation();
  }
}
