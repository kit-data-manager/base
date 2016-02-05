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
package edu.kit.dama.authorization.entities;

/**
 *
 * @author pasic
 */
public class SecurableResourceId {

  private String domain;
  private String domainUniqueId;

  /**
   * Default constructor.
   */
  public SecurableResourceId() {
  }

  /**
   * Default constructor.
   *
   * @param domain The domain.
   * @param domainUniqueId The domain unique id.
   */
  public SecurableResourceId(String domain, String domainUniqueId) {
    this.domain = domain;
    this.domainUniqueId = domainUniqueId;
  }

  /**
   * Get the domain.
   *
   * @return The domain.
   */
  public String getDomain() {
    return domain;
  }

  /**
   * Get the domain unique id.
   *
   * @return The domain unique id.
   */
  public String getDomainUniqueId() {
    return domainUniqueId;
  }

  /**
   * Set the domain.
   *
   * @param domain The domain.
   */
  public void setDomain(String domain) {
    this.domain = domain;
  }

  /**
   * Set the domain unique id.
   *
   * @param domainUniqueId The domain unique id.
   */
  public void setDomainUniqueId(String domainUniqueId) {
    this.domainUniqueId = domainUniqueId;
  }

  @Override
  public boolean equals(Object other) {
    boolean equals = true;
    if (this == other) {
      return equals;
    }
    if (other != null && (getClass() == other.getClass())) {
      SecurableResourceId otherSecurableResourceId = (SecurableResourceId) other;
      if (domain != null) {
        equals = equals && (domain.equals(otherSecurableResourceId.domain));
      } else {
        equals = equals && (otherSecurableResourceId.domain == null);
      }
      if (equals && (domainUniqueId != null)) {
        equals = equals && (domainUniqueId.equals(otherSecurableResourceId.domainUniqueId));
      } else {
        equals = equals && (otherSecurableResourceId.domainUniqueId == null);
      }
    } else {
      equals = false;
    }

    return equals;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + (this.domain != null ? this.domain.hashCode() : 0);
    hash = 67 * hash + (this.domainUniqueId != null ? this.domainUniqueId.hashCode() : 0);
    return hash;
  }

  @Override
  public String toString() {
    return "{" + domain + "}" + domainUniqueId; //To change body of generated methods, choose Tools | Templates.
  }
}
