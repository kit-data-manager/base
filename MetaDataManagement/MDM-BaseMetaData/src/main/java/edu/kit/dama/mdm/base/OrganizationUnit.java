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
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 * All information about the organization units.
 *
 * @author hartmann-v
 */
@Entity
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "simple",
          attributeNodes = {
            @XmlNamedAttributeNode("organizationUnitId")
          }),
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode("organizationUnitId"),
            @XmlNamedAttributeNode("ouName"),
            @XmlNamedAttributeNode("address"),
            @XmlNamedAttributeNode("zipCode"),
            @XmlNamedAttributeNode("city"),
            @XmlNamedAttributeNode("country"),
            @XmlNamedAttributeNode("website"),
            @XmlNamedAttributeNode(value = "manager", subgraph = "simple")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
public class OrganizationUnit implements Serializable {

  /**
   * UID should be the date of the last change in the format yyyyMMdd.
   */
  private static final long serialVersionUID = 20111201L;
  // <editor-fold defaultstate="collapsed" desc="declaration of variables">
  /**
   * Identification number of the organization unit. primary key of the data
   * set.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long organizationUnitId;
  /**
   * Name of organization unit.
   */
  private String ouName;
  /**
   * Head of this organization unit.
   */
  @ManyToOne(fetch = FetchType.EAGER)
  @XmlElement(name = "manager")
  private UserData manager;
  /**
   * Address of organization unit.
   */
  private String address;
  /**
   * Zip code of organization unit.
   */
  private String zipCode;
  /**
   * City of organization unit.
   */
  private String city;
  /**
   * Country of organization unit.
   */
  private String country;
  /**
   * Web site of the organization unit.
   */
  private String website;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="setters and getters">
  /**
   * Get uniform id of this entry.
   *
   * @return the organizationUnitId
   */
  public Long getOrganizationUnitId() {
    return organizationUnitId;
  }

  /**
   * Set uniform id of this entry.
   *
   * @param organizationUnitId the organizationUnitId to set
   */
  public void setOrganizationUnitId(final Long organizationUnitId) {
    this.organizationUnitId = organizationUnitId;
  }

  /**
   * Get name of ou.
   *
   * @return the ouName
   */
  public String getOuName() {
    return ouName;
  }

  /**
   * Set Name of ou.
   *
   * @param ouName the ouName to set
   */
  public void setOuName(final String ouName) {
    this.ouName = ouName;
  }

  /**
   * Get the manager of organization unit.
   *
   * @return the manager
   */
  public UserData getManager() {
    return manager;
  }

  /**
   * Set Manager of this organization unit.
   *
   * @param manager the manager to set
   */
  public void setManager(final UserData manager) {
    this.manager = manager;
  }

  /**
   * Get address of ou.
   *
   * @return the address
   */
  public String getAddress() {
    return address;
  }

  /**
   * Set address of ou.
   *
   * @param address the address to set
   */
  public void setAddress(final String address) {
    this.address = address;
  }

  /**
   * Get zip-code of city.
   *
   * @return the zipCode
   */
  public String getZipCode() {
    return zipCode;
  }

  /**
   * Set zip-code of city.
   *
   * @param zipCode the zipCode to set
   */
  public void setZipCode(final String zipCode) {
    this.zipCode = zipCode;
  }

  /**
   * Get city of ou.
   *
   * @return the city
   */
  public String getCity() {
    return city;
  }

  /**
   * Set city of ou.
   *
   * @param city the city to set
   */
  public void setCity(final String city) {
    this.city = city;
  }

  /**
   * Get country of ou.
   *
   * @return the country
   */
  public String getCountry() {
    return country;
  }

  /**
   * Set country of ou..
   *
   * @param country the country to set
   */
  public void setCountry(final String country) {
    this.country = country;
  }

  /**
   * Set web site of the ou.
   *
   * @return the website
   */
  public String getWebsite() {
    return website;
  }

  /**
   * Get web site of the ou.
   *
   * @param website the website to set
   */
  public void setWebsite(String website) {
    this.website = website;
  }
// </editor-fold>

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder("Organization Unit\n-----------------\n");
    buffer.append(getOrganizationUnitId()).append(": ").append(ouName).append("\n");
    if (manager != null) {
      buffer.append(manager.toString()).append("\n");
    }
    buffer.append(address).append("\n");
    buffer.append(zipCode).append(" ").append(city).append("\n");
    buffer.append(country).append("\nwww: ").append(getWebsite()).append("\n");
    return buffer.toString();
  }

  @Override
  public boolean equals(Object other) {
    boolean equals = true;
    if (this == other) {
      return equals;
    }
    if (other != null && (getClass() == other.getClass())) {
      OrganizationUnit otherOrganizationUnit = (OrganizationUnit) other;
      if (organizationUnitId != null) {
        equals = equals && (organizationUnitId.equals(otherOrganizationUnit.organizationUnitId));
      } else {
        equals = equals && (otherOrganizationUnit.organizationUnitId == null);
      }
      if (equals && (ouName != null)) {
        equals = equals && (ouName.equals(otherOrganizationUnit.ouName));
      } else {
        equals = equals && (otherOrganizationUnit.ouName == null);
      }
      if (equals && (address != null)) {
        equals = equals && (address.equals(otherOrganizationUnit.address));
      } else {
        equals = equals && (otherOrganizationUnit.address == null);
      }
      if (equals && (zipCode != null)) {
        equals = equals && (zipCode.equals(otherOrganizationUnit.zipCode));
      } else {
        equals = equals && (otherOrganizationUnit.zipCode == null);
      }
      if (equals && (city != null)) {
        equals = equals && (city.equals(otherOrganizationUnit.city));
      } else {
        equals = equals && (otherOrganizationUnit.city == null);
      }
      if (equals && (country != null)) {
        equals = equals && (country.equals(otherOrganizationUnit.country));
      } else {
        equals = equals && (otherOrganizationUnit.country == null);
      }
      if (equals && (website != null)) {
        equals = equals && (website.equals(otherOrganizationUnit.website));
      } else {
        equals = equals && (otherOrganizationUnit.website == null);
      }
      if (equals && (manager != null)) {
        equals = equals && (manager.equals(otherOrganizationUnit.manager));
      } else {
        equals = equals && (otherOrganizationUnit.manager == null);
      }
    } else {
      equals = false;
    }

    return equals;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 41 * hash + (this.ouName != null ? this.ouName.hashCode() : 0);
    hash = 41 * hash + (this.address != null ? this.address.hashCode() : 0);
    hash = 41 * hash + (this.zipCode != null ? this.zipCode.hashCode() : 0);
    hash = 41 * hash + (this.city != null ? this.city.hashCode() : 0);
    hash = 41 * hash + (this.country != null ? this.country.hashCode() : 0);
    hash = 41 * hash + (this.website != null ? this.website.hashCode() : 0);
    return hash;
  }
}
