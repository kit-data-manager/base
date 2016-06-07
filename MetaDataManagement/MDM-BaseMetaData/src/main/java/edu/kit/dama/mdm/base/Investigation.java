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
import edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation;
import edu.kit.dama.mdm.core.tools.DateTester;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 * All information about investigation.
 *
 * @author hartmann-v
 */
@Entity
//@XmlNamedObjectGraphs({
//    @XmlNamedObjectGraph(
//            name = "simple",
//            attributeNodes = {
//                @XmlNamedAttributeNode("investigationId")
//            }),
//    @XmlNamedObjectGraph(
//            name = "default",
//            attributeNodes = {
//                @XmlNamedAttributeNode("investigationId"),
//                @XmlNamedAttributeNode("uniqueIdentifier"),
//                @XmlNamedAttributeNode("topic"),
//                @XmlNamedAttributeNode("note"),
//                @XmlNamedAttributeNode("description"),
//                @XmlNamedAttributeNode(value = "study", subgraph = "simple"),
//                @XmlNamedAttributeNode(value = "metaDataSchema", subgraph = "simple"),
//                @XmlNamedAttributeNode(value = "participants", subgraph = "simple"),
//                @XmlNamedAttributeNode(value = "dataSets", subgraph = "simple"),
//                @XmlNamedAttributeNode("startDate"),
//                @XmlNamedAttributeNode("endDate")
//            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "Investigation.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("investigationId"),
                @NamedAttributeNode("uniqueIdentifier")
            }),
    @NamedEntityGraph(
            name = "Investigation.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("investigationId"),
                @NamedAttributeNode("uniqueIdentifier"),
                @NamedAttributeNode("topic"),
                @NamedAttributeNode("note"),
                @NamedAttributeNode("description"),
                @NamedAttributeNode(value = "study", subgraph = "Investigation.default.Study.simple"),
                @NamedAttributeNode(value = "metaDataSchema", subgraph = "Investigation.default.MetadataSchema.simple"),
                @NamedAttributeNode(value = "participants", subgraph = "Investigation.default.Participants.simple"),
                @NamedAttributeNode(value = "dataSets", subgraph = "Investigation.default.Datasets.simple"),
                @NamedAttributeNode("startDate"),
                @NamedAttributeNode("endDate"),
                @NamedAttributeNode("visible")
            },
            subgraphs = {
                @NamedSubgraph(
                        name = "Investigation.default.Study.simple",
                        attributeNodes = {
                            @NamedAttributeNode("studyId")}
                ),
                @NamedSubgraph(
                        name = "Investigation.default.MetadataSchema.simple",
                        attributeNodes = {
                            @NamedAttributeNode("id")}
                ),
                @NamedSubgraph(
                        name = "Investigation.default.Participants.simple",
                        attributeNodes = {
                            @NamedAttributeNode("participantId")}
                ),
                @NamedSubgraph(
                        name = "Investigation.default.Datasets.simple",
                        attributeNodes = {
                            @NamedAttributeNode("baseId")}
                )

            })
})
public class Investigation implements Serializable, IDefaultInvestigation, ISecurableResource, FetchGroupTracker {

    /**
     * UID should be the date of the last change in the format yyyyMMdd.
     */
    private static final long serialVersionUID = 20111201L;
    // <editor-fold defaultstate="collapsed" desc="declaration of variables">
    /**
     * Identification number of the investigation. primary key of the data set.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long investigationId;
    /**
     * Unique identifier internally used for linking Authorization to this
     * entity.
     */
    @SecurableResourceIdField(domainName = "edu.kit.dama.mdm.base.Investigation")
    @Column(nullable = false, unique = true)
    private String uniqueIdentifier;
    /**
     * Topic of the investigation.
     */
    private String topic;
    /**
     * Note.
     */
    @Column(length = 1024)
    private String note;
    /**
     * Description.
     */
    @Column(length = 1024)
    private String description;
    /**
     * Study which initiated this investigation. Bidirectional relation.
     */
    @ManyToOne
    // @BatchFetch(BatchFetchType.EXISTS)
    private Study study;
    /**
     * All types of meta data which should be supported by the digital objects.
     * Which kind of meta data is supported by this investigation. The meta data
     * type is an String which should contain the following data in human
     * readable form: <ul><li>community</li> <li>investigation</li>
     * <li>version</li></ul>
     * e.g.:
     * http://www.ipe.kit.edu/dama/microscopy/toxicology/otte/1.0/schema.xsd
     */
    @OneToMany(fetch = FetchType.EAGER)
    @XmlElementWrapper(name = "metaDataSchemas")
    @XmlElement(name = "metaDataSchema")
    @BatchFetch(BatchFetchType.EXISTS)
    private Set<MetaDataSchema> metaDataSchema = new HashSet<>();
    /**
     * Group of users who are participated.
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE})
    @XmlElementWrapper
    @XmlElement(name = "participant")
    @BatchFetch(BatchFetchType.EXISTS)
    private Set<Participant> participants = new HashSet<>();
    /**
     * Data sets which are linked to this investigation.
     */
    @OneToMany(mappedBy = "investigation", fetch = FetchType.EAGER)
    @XmlElementWrapper(name = "digitalObjects")
    @XmlElement(name = "digitalObject")
    @BatchFetch(BatchFetchType.EXISTS)
    private Set<DigitalObject> dataSets = new HashSet<>();
    /**
     * Start date of study.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    /**
     * End date of study.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    /**
     * Investigation visible or not. Invisible investigations may be deleted.
     */
    private Boolean visible = Boolean.TRUE;

    // </editor-fold>
    /**
     * Factory a new investigation with an auto-generated unique identifier. The
     * identifier is generated using UUID.randomUUID().toString().
     *
     * @return The new investigation.
     */
    public static Investigation factoryNewInvestigation() {
        Investigation result = new Investigation();
        result.setUniqueIdentifier(UUID.randomUUID().toString());
        result.setDataSets(new HashSet<DigitalObject>());
        result.setParticipants(new HashSet<Participant>());
        result.setMetaDataSchema(new HashSet<MetaDataSchema>());
        return result;
    }

    /**
     * Factory a new investigation with a given unique identifier.
     *
     * @param uniqueIdentifier the investigations unique id.
     *
     * @return The new investigation or null if unique identifier is null.
     */
    public static Investigation factoryNewInvestigation(String uniqueIdentifier) {
        if (uniqueIdentifier == null) {
            return null;
        }
        Investigation result = new Investigation();
        result.setUniqueIdentifier(uniqueIdentifier);
        result.setDataSets(new HashSet<DigitalObject>());
        result.setParticipants(new HashSet<Participant>());
        result.setMetaDataSchema(new HashSet<MetaDataSchema>());
        return result;
    }

    /**
     * Default constructor. When using this constructor, the unique identifier
     * has to be set manually. Otherwise, the object is not suitable for
     * persisting. If you want to persist the object, use
     * factoryNewInvestigation() instead.
     */
    public Investigation() {
    }

    // <editor-fold defaultstate="collapsed" desc="setters and getters">
    @Override
    public Long getInvestigationId() {
        return investigationId;
    }

    /**
     * Set Id of the instance. (Should be only used by the JPA implementation.)
     *
     * @param investigationId the investigationId to set
     */
    public void setInvestigationId(Long investigationId) {
        this.investigationId = investigationId;
    }

    /**
     * The unique identifier should be set only once during instantiation via
     * factoryNewInvestigation().
     *
     * @param uniqueIdentifier Ignored
     */
    protected void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public String getTopic() {
        return topic;
    }

    /**
     * Set topic.
     *
     * @param topic the topic to set
     */
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String getNote() {
        return note;
    }

    /**
     * Set free text note.
     *
     * @param note the note to set
     */
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Set description of the investigation.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Set<MetaDataSchema> getMetaDataSchema() {
        return metaDataSchema;
    }

    /**
     * Set type of meta data linked with this investigation.
     *
     * @param metaDataSchema the set of metaDataSchema to set
     */
    public void setMetaDataSchema(Set<MetaDataSchema> metaDataSchema) {
        this.metaDataSchema = metaDataSchema;
    }

    /**
     * Add type of meta data linked with this investigation.
     *
     * @param metaDataSchema the metaDataSchema to set
     */
    public void addMetaDataSchema(MetaDataSchema metaDataSchema) {
        this.metaDataSchema.add(metaDataSchema);
    }

    /**
     * Remove type of meta data linked with this investigation.
     *
     * @param metaDataSchema the metaDataSchema to set
     */
    public void removeMetaDataSchema(MetaDataSchema metaDataSchema) {
        this.metaDataSchema.remove(metaDataSchema);
    }

    @Override
    public Set<Participant> getParticipants() {
        return participants;
    }

    /**
     * Set participants on this investigation.
     *
     * @param participants the participants to set
     */
    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }

    /**
     * Add a participant to the set of participants. Before the add operation is
     * performed, it will be checked if there is already a participant with the
     * same user with the same task. In this case, the participant won't be
     * added again.
     *
     * @param participant the participant to add
     */
    public void addParticipant(final Participant participant) {
        if (participants == null) {
            participants = new HashSet<>();
        }
        if (participant != null) {
            if (!containsParticipant(participant)) {
                participants.add(participant);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Check if the investigation contains the provided participant.
     *
     * @param pParticipant The participant to check for.
     *
     * @return TRUE if the investigation contains the participant.
     */
    private boolean containsParticipant(final Participant pParticipant) {
        return CollectionUtils.find(participants, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                Participant toCheck = (Participant) o;
                Task t = pParticipant.getTask();
                UserData user = pParticipant.getUser();
                boolean result = user.equals(toCheck.getUser());
                if (result) {
                    result = (t == null && toCheck.getTask() == null) || (t != null && t.equals(toCheck.getTask()));
                }
                return result;
            }
        }) != null;
    }

    /**
     * Remove a participant of the set of participants.
     *
     * @param participant the participant to remove
     */
    public void removeParticipant(final Participant participant) {
        if (participant != null) {
            if (participants != null) {
                participants.remove(participant);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Set<DigitalObject> getDataSets() {
        return dataSets;
    }

    /**
     * Get set of data linked to this investigation.
     *
     * @param pVisibleOnly TRUE = Return only visible datasets.
     *
     * @return the datasets
     */
    public Set<DigitalObject> getDataSets(boolean pVisibleOnly) {
        if (pVisibleOnly) {
            Set<DigitalObject> visibleElements = new HashSet<>();
            for (DigitalObject d : dataSets) {
                if (d.isVisible()) {
                    visibleElements.add(d);
                }
            }
            return visibleElements;
        }
        //return all investigations
        return dataSets;
    }

    /**
     * Set set of data linked to this investigation.
     *
     * @param dataSets the dataSets to set
     */
    public void setDataSets(Set<DigitalObject> dataSets) {
        this.dataSets = dataSets;
    }

    /**
     * Add a data set to the set of dataSets.
     *
     * @param dataSet the dataSet to add
     */
    public void addDataSet(final DigitalObject dataSet) {
        if (dataSets == null) {
            dataSets = new HashSet<>();
        }
        if (dataSet != null) {
            dataSets.add(dataSet);
            dataSet.setInvestigation(this);
        } else {
            throw new IllegalArgumentException("Argument dataSet must not be null.");
        }
    }

    /**
     * Remove a data set of the set of dataSets.
     *
     * @param dataSet the dataSet to remove
     */
    public void removeDataSet(final DigitalObject dataSet) {
        if (dataSet != null) {
            if (dataSets != null) {
                dataSets.remove(dataSet);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Set start date of investigation.
     *
     * @param startDateOfInvestigation start date of the investigation
     */
    public void setStartDate(final Date startDateOfInvestigation) {
        DateTester.testForValidDates(startDateOfInvestigation, endDate);
        this.startDate = startDateOfInvestigation;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set end date of investigation.
     *
     * @param endDateOfInvestigation end date of the investigation
     */
    public void setEndDate(final Date endDateOfInvestigation) {
        DateTester.testForValidDates(startDate, endDateOfInvestigation);
        this.endDate = endDateOfInvestigation;
    }

    @Override
    public Study getStudy() {
        return study;
    }

    /**
     * Set the study initiating this investigation. This method is called via a
     * study instance. Please add the investigation instance to a study instance
     * (e.g.: study.addInvestigation(...))
     *
     * @param study the study to set
     */
    public void setStudy(Study study) {
        this.study = study;
    }

    /**
     * Get visibility of investigation with JPA-compliant naming.
     *
     * @return visibility of investigation
     */
    public Boolean getVisible() {
        return visible;
    }

    @Override
    public Boolean isVisible() {
        return getVisible();
    }

    /**
     * Set visibility of investigation.
     *
     * @param visible visibility of investigation
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
// </editor-fold>

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("Investigation:\n----\n");
        buffer.append(getInvestigationId()).append(": ").append(topic);
        buffer.append("\nNote: ").append(note);
        buffer.append("\nDescription: ").append(description);
        buffer.append("\nStudy: ").append(study);
        buffer.append(metaDataSchema).append("\n").append("Participants: \n");
        if (participants != null) {
            for (Participant participant : participants) {
                buffer.append(participant.toString()).append("\n");
            }
        }
        return buffer.toString();
    }

    @Override
    public boolean equals(Object other) {
        boolean equals = true;
        if (this == other) {
            return equals;
        }
        if (other != null && (getClass() == other.getClass())) {
            Investigation otherInvestigation = (Investigation) other;
            if (investigationId != null) {
                equals = equals && (investigationId.equals(otherInvestigation.investigationId));
            } else {
                equals = equals && (otherInvestigation.investigationId == null);
            }
            if (equals && (topic != null)) {
                equals = equals && (topic.equals(otherInvestigation.topic));
            } else {
                equals = equals && (otherInvestigation.topic == null);
            }
            if (equals && (startDate != null)) {
                equals = equals && (startDate.equals(otherInvestigation.startDate));
            } else {
                equals = equals && (otherInvestigation.startDate == null);
            }
            if (equals && (endDate != null)) {
                equals = equals && (endDate.equals(otherInvestigation.endDate));
            } else {
                equals = equals && (otherInvestigation.endDate == null);
            }
            if (equals && (description != null)) {
                equals = equals && (description.equals(otherInvestigation.description));
            } else {
                equals = equals && (otherInvestigation.description == null);
            }
            if (equals && (note != null)) {
                equals = equals && (note.equals(otherInvestigation.note));
            } else {
                equals = equals && (otherInvestigation.note == null);
            }
            if (equals && (visible != null)) {
                equals = equals && (visible.equals(otherInvestigation.visible));
            } else {
                equals = equals && (otherInvestigation.visible == null);
            }
            if (equals && (metaDataSchema != null)) {
                equals = equals && metaDataSchema.size() == otherInvestigation.metaDataSchema.size()
                        && metaDataSchema.containsAll(otherInvestigation.metaDataSchema);
            } else {
                equals = equals && (otherInvestigation.metaDataSchema == null);
            }
            if (equals && (participants != null)) {
                equals = equals && participants.size() == otherInvestigation.participants.size()
                        && participants.containsAll(otherInvestigation.participants);
            } else {
                equals = equals && (otherInvestigation.participants == null);
            }
        } else {
            equals = false;
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.topic != null ? this.topic.hashCode() : 0);
        hash = 31 * hash + (this.note != null ? this.note.hashCode() : 0);
        hash = 31 * hash + (this.description != null ? this.description.hashCode() : 0);
        return hash;
    }

    @Override
    public SecurableResourceId getSecurableResourceId() {
        return new SecurableResourceId("edu.kit.dama.mdm.base.Investigation", getUniqueIdentifier());
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
