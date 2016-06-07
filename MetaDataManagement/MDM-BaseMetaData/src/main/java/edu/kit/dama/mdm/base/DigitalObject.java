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

import edu.kit.dama.authorization.annotations.SecurableResourceIdField;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject;
import edu.kit.dama.mdm.core.tools.DateTester;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @author hartmann-v
 */
@Entity
//@XmlNamedObjectGraphs({
//    @XmlNamedObjectGraph(
//            name = "simple",
//            attributeNodes = {
//                @XmlNamedAttributeNode("baseId")
//            }),
//    @XmlNamedObjectGraph(
//            name = "default",
//            attributeNodes = {
//                @XmlNamedAttributeNode("baseId"),
//                @XmlNamedAttributeNode("digitalObjectIdentifier"),
//                @XmlNamedAttributeNode("label"),
//                @XmlNamedAttributeNode("note"),
//                @XmlNamedAttributeNode(value = "investigation", subgraph = "simple"),
//                @XmlNamedAttributeNode(value = "uploader", subgraph = "simple"),
//                @XmlNamedAttributeNode(value = "experimenters", subgraph = "simple"),
//                @XmlNamedAttributeNode("startDate"),
//                @XmlNamedAttributeNode("endDate"),
//                @XmlNamedAttributeNode("uploadDate")
//            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "DigitalObject.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("baseId"),
                @NamedAttributeNode("digitalObjectIdentifier")}),
    @NamedEntityGraph(
            name = "DigitalObject.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("baseId"),
                @NamedAttributeNode("digitalObjectIdentifier"),
                @NamedAttributeNode("label"),
                @NamedAttributeNode("note"),
                @NamedAttributeNode(value = "investigation", subgraph = "DigitalObject.default.Investigation.simple"),
                @NamedAttributeNode(value = "uploader", subgraph = "DigitalObject.default.Uploader.simple"),
                @NamedAttributeNode(value = "experimenters", subgraph = "DigitalObject.default.Experimenters.simple"),
                @NamedAttributeNode("startDate"),
                @NamedAttributeNode("endDate"),
                @NamedAttributeNode("uploadDate")
            },
            subgraphs = {
                @NamedSubgraph(
                        name = "DigitalObject.default.Investigation.simple",
                        attributeNodes = {
                            @NamedAttributeNode("investigationId")}
                ),
                @NamedSubgraph(
                        name = "DigitalObject.default.Uploader.simple",
                        attributeNodes = {
                            @NamedAttributeNode("userId")}
                ),
                @NamedSubgraph(
                        name = "DigitalObject.default.Experimenters.simple",
                        attributeNodes = {
                            @NamedAttributeNode("userId")}
                )
            })
})
public class DigitalObject implements Serializable, IDefaultDigitalObject, IDigitalObjectId, ISecurableResource, FetchGroupTracker {

    /**
     * UID should be the date of the last change in the format yyyyMMdd.
     */
    private static final long serialVersionUID = 20111201L;
    // <editor-fold defaultstate="collapsed" desc="declaration of variables">
    /**
     * Identification number of this data set. primary key of the data set.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long baseId;
    /**
     * Reference to the DigitalObject.
     */
    @Transient
    private DigitalObjectId digitalObjectId;
    /**
     * Digital Object Identity (DOI) of the dataset. The DOI should be unique
     * (worldwide).
     */
    @SecurableResourceIdField(domainName = "edu.kit.dama.mdm.base.DigitalObject")
    @Column(nullable = false, unique = true)
    private String digitalObjectIdentifier;
    private String label = null;
    /**
     * Linked investigation.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private Investigation investigation;
    /**
     * UserData who uploads the data set.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    private UserData uploader;
    /**
     * UserData who initiate the measurement.
     */
    @OneToMany(fetch = FetchType.EAGER)
    @XmlElementWrapper(name = "experimenters")
    @XmlElement(name = "experimenter")
    @BatchFetch(BatchFetchType.EXISTS)
    private Set<UserData> experimenters;
    /**
     * Start date of measurement.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    /**
     * End date of the measurement.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    /**
     * Date of the upload. It's needed for an unique label for the data set.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadDate;
    /**
     * Short notes for the measurement.
     */
    @Column(length = 1024)
    private String note;
    /**
     * Data set visible or not.
     */
    private Boolean visible;
    // </editor-fold>

    /**
     * Factory a new digital object with the provided digital object identifier.
     *
     * @param pDigitalObjectIdentifier The digital object identifier.
     *
     * @return The new object.
     */
    public static DigitalObject factoryNewDigitalObject(String pDigitalObjectIdentifier) {
        if (pDigitalObjectIdentifier == null) {
            throw new IllegalArgumentException("Argument 'pDigitalObjectIdentifier' must not be 'null'");
        }
        DigitalObject result = new DigitalObject();
        result.setDigitalObjectIdentifier(pDigitalObjectIdentifier);
        result.setExperimenters(new HashSet<UserData>());
        result.setVisible(Boolean.TRUE);
        return result;
    }

    /**
     * Factory a new digital object with an auto-generated digital object
     * identifier. The identifier is generated using
     * UUID.randomUUID().toString().
     *
     * @return The new object.
     */
    public static DigitalObject factoryNewDigitalObject() {
        return factoryNewDigitalObject(UUID.randomUUID().toString());
    }

    /**
     * Default constructor. When using this constructor, the digital object
     * identifier has to be set manually. Otherwise, the object is not suitable
     * for persisting. If you want to persist the object, use
     * factoryNewDigitalObject() instead.
     */
    public DigitalObject() {
    }

    // <editor-fold defaultstate="collapsed" desc="setters and getters">
    @Override
    public Long getBaseId() {
        return baseId;
    }

    /**
     * @param baseID the baseID to set
     */
    public void setBaseId(final Long baseID) {
        this.baseId = baseID;
    }

    /**
     * Set the label of this object. The label is a human readable identifier
     * for the object. If no label is specified, the digital object identifier
     * will be used as label.
     *
     * @param label The label of this object.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getDigitalObjectIdentifier() {
        return digitalObjectIdentifier;
    }

    /**
     * Set the digital object identifier.
     *
     * @param digitalObjectIdentifier the DOI to set
     */
    protected void setDigitalObjectIdentifier(final String digitalObjectIdentifier) {
        this.digitalObjectIdentifier = digitalObjectIdentifier;
        digitalObjectId = new DigitalObjectId(digitalObjectIdentifier);
    }

    @Override
    public void setDigitalObjectId(DigitalObjectId digitalObjectId) {
        this.digitalObjectId = digitalObjectId;
        if (digitalObjectId != null) {
            digitalObjectIdentifier = this.digitalObjectId.getStringRepresentation();
        } else {
            digitalObjectIdentifier = null;
        }
    }

    @Override
    public DigitalObjectId getDigitalObjectId() {
        // due to dirty hacks in the JPA the method setDigitalObjectIdentifier()
        // is not used if instance was restored from database.
        if ((digitalObjectId == null) && (digitalObjectIdentifier != null)) {
            setDigitalObjectIdentifier(digitalObjectIdentifier);
        }
        return digitalObjectId;
    }

    @Override
    public Investigation getInvestigation() {
        return investigation;
    }

    /**
     * Set investigation. This method is called via the investigation instance.
     * Please add the digitalObject instance to a investigation instance (e.g.:
     * investigation.addDataSet(...))
     *
     * @param investigation the investigation to set
     */
    public void setInvestigation(final Investigation investigation) {
        this.investigation = investigation;
    }

    @Override
    public String getNote() {
        return note;
    }

    /**
     * Set note for data.
     *
     * @param note the note to set
     */
    public void setNote(final String note) {
        this.note = note;
    }

    @Override
    public Date getUploadDate() {
        return uploadDate;
    }

    /**
     * Set upload date.
     *
     * @param uploadDate the uploadDate to set
     */
    public void setUploadDate(final Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Set start date of measurement.
     *
     * @param startDateOfMeasurement the date to set
     */
    public void setStartDate(final Date startDateOfMeasurement) {
        DateTester.testForValidDates(startDateOfMeasurement, endDate);
        this.startDate = startDateOfMeasurement;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set end date of measurement.
     *
     * @param endDateOfMeasurement the date to set
     */
    public void setEndDate(final Date endDateOfMeasurement) {
        DateTester.testForValidDates(startDate, endDateOfMeasurement);
        this.endDate = endDateOfMeasurement;
    }

    /**
     * Set the user who uploads data.
     *
     * @param uploader the user who uploads the data
     */
    public void setUploader(final UserData uploader) {
        this.uploader = uploader;
    }

    @Override
    public UserData getUploader() {
        return uploader;
    }

    /**
     * Add a user to the set of experimenters.
     *
     * @param experimenter the experimenter to add
     */
    public void addExperimenter(final UserData experimenter) {
        if (experimenters == null) {
            experimenters = new HashSet<>();
        }
        if (experimenter != null) {
            experimenters.add(experimenter);
        } else {
            throw new IllegalArgumentException("Argument experimenter must not be null.");
        }
    }

    /**
     * Remove a user of the set of experimenters.
     *
     * @param experimenter the experimenter to remove
     */
    public void removeExperimenter(final UserData experimenter) {
        if (experimenter != null) {
            if (experimenters != null) {
                experimenters.remove(experimenter);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Set a set of experimenters.
     *
     * @param experimenters the experimenters to set
     */
    public void setExperimenters(Set<UserData> experimenters) {
        this.experimenters = experimenters;
    }

    @Override
    public Set<UserData> getExperimenters() {
        return experimenters;
    }

    @Override
    public Boolean isVisible() {
        return getVisible();
    }

    public Boolean getVisible() {
        return (visible == null) || visible;
    }

    /**
     * Set data set visible.
     *
     * @param visible the visible to set
     */
    public void setVisible(final Boolean visible) {
        this.visible = visible;
    }
    // </editor-fold>

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("DigitalObject:\n-------------");
        buffer.append("\nDOI: ").append(getDigitalObjectIdentifier());
        buffer.append("\nID: ").append(getBaseId());
        buffer.append("\nCreator: ").append(getExperimenters());
        buffer.append("\nDate: ").append(getStartDate()).append(" - ").append(getEndDate());
        buffer.append("\n Note: ").append(getNote());
        buffer.append("\n Visible: ").append(isVisible()).append("\n");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object other) {
        boolean equals = true;
        if (this == other) {
            return equals;
        }
        if (other != null && (getClass() == other.getClass())) {
            DigitalObject otherDigitalObject = (DigitalObject) other;
            if (baseId != null) {
                equals = equals && (baseId.equals(otherDigitalObject.baseId));
            } else {
                equals = equals && (otherDigitalObject.baseId == null);
            }
            if (equals && (digitalObjectIdentifier != null)) {
                equals = equals && (digitalObjectIdentifier.equals(otherDigitalObject.digitalObjectIdentifier));
            } else {
                equals = equals && (otherDigitalObject.digitalObjectIdentifier == null);
            }
            if (equals && (startDate != null)) {
                equals = equals && (startDate.equals(otherDigitalObject.startDate));
            } else {
                equals = equals && (otherDigitalObject.startDate == null);
            }
            if (equals && (endDate != null)) {
                equals = equals && (endDate.equals(otherDigitalObject.endDate));
            } else {
                equals = equals && (otherDigitalObject.endDate == null);
            }
            if (equals && (investigation != null)) {
                equals = equals && (investigation.equals(otherDigitalObject.investigation));
            } else {
                equals = equals && (otherDigitalObject.investigation == null);
            }
            if (equals && (note != null)) {
                equals = equals && (note.equals(otherDigitalObject.note));
            } else {
                equals = equals && (otherDigitalObject.note == null);
            }
            if (equals && (uploadDate != null)) {
                equals = equals && (uploadDate.equals(otherDigitalObject.uploadDate));
            } else {
                equals = equals && (otherDigitalObject.uploadDate == null);
            }
            if (equals && (uploader != null)) {
                equals = equals && (uploader.equals(otherDigitalObject.uploader));
            } else {
                equals = equals && (otherDigitalObject.uploader == null);
            }
            if (equals && (visible != null)) {
                equals = equals && (visible.equals(otherDigitalObject.visible));
            } else {
                equals = equals && (otherDigitalObject.visible == null);
            }
            if (equals && (experimenters != null)) {
                equals = equals && experimenters.size() == otherDigitalObject.experimenters.size()
                        && experimenters.containsAll(otherDigitalObject.experimenters);
            } else {
                equals = equals && (otherDigitalObject.experimenters == null);
            }
        } else {
            equals = false;
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.digitalObjectIdentifier != null ? this.digitalObjectIdentifier.hashCode() : 0);
        return hash;
    }

    @Override
    public SecurableResourceId getSecurableResourceId() {
        return new SecurableResourceId("edu.kit.dama.mdm.base.DigitalObject", getDigitalObjectIdentifier());
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
