/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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

import edu.kit.dama.mdm.base.interfaces.IDefaultOrganizationUnit;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 * All information about the organization units.
 *
 * @author hartmann-v
 */
@Entity
//@XmlNamedObjectGraphs({
//    @XmlNamedObjectGraph(
//            name = "simple",
//            attributeNodes = {
//                @XmlNamedAttributeNode("organizationUnitId")
//            }),
//    @XmlNamedObjectGraph(
//            name = "default",
//            attributeNodes = {
//                @XmlNamedAttributeNode("organizationUnitId"),
//                @XmlNamedAttributeNode("ouName"),
//                @XmlNamedAttributeNode("address"),
//                @XmlNamedAttributeNode("zipCode"),
//                @XmlNamedAttributeNode("city"),
//                @XmlNamedAttributeNode("country"),
//                @XmlNamedAttributeNode("website"),
//                @XmlNamedAttributeNode(value = "manager", subgraph = "simple")
//            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "OrganizationUnit.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("organizationUnitId")}),
    @NamedEntityGraph(
            name = "OrganizationUnit.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("organizationUnitId"),
                @NamedAttributeNode("ouName"),
                @NamedAttributeNode("address"),
                @NamedAttributeNode("zipCode"),
                @NamedAttributeNode("city"),
                @NamedAttributeNode("country"),
                @NamedAttributeNode("website"),
                @NamedAttributeNode(value = "manager", subgraph = "OrganizationUnit.default.Manager.simple")
            },
            subgraphs = {
                @NamedSubgraph(
                        name = "OrganizationUnit.default.Manager.simple",
                        attributeNodes = {
                            @NamedAttributeNode("userId")}
                )
            })
})
public class OrganizationUnit implements Serializable, IDefaultOrganizationUnit, FetchGroupTracker {

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
    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    private transient org.eclipse.persistence.queries.FetchGroup fg;
    private transient Session sn;

    @Override
    public org.eclipse.persistence.queries.FetchGroup _persistence_getFetchGroup() {
        return this.fg;
    }

    @Override
    public void _persistence_setFetchGroup(org.eclipse.persistence.queries.FetchGroup fg) {
        this.fg = fg;
    }

    @Override
    public boolean _persistence_isAttributeFetched(String string) {
        return true;
    }

    @Override
    public void _persistence_resetFetchGroup() {
    }

    @Override
    public boolean _persistence_shouldRefreshFetchGroup() {
        return false;
    }

    @Override
    public void _persistence_setShouldRefreshFetchGroup(boolean bln) {

    }

    @Override
    public Session _persistence_getSession() {

        return sn;
    }

    @Override
    public void _persistence_setSession(Session sn) {
        this.sn = sn;

    }
}
