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
import edu.kit.dama.mdm.base.interfaces.IDefaultStudy;
import edu.kit.dama.mdm.core.tools.DateTester;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
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
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All information about the study.
 *
 * @author hartmann-v
 */
@Entity
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("studyId")
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("studyId"),
                @XmlNamedAttributeNode("uniqueIdentifier"),
                @XmlNamedAttributeNode("topic"),
                @XmlNamedAttributeNode("note"),
                @XmlNamedAttributeNode("legalNote"),
                @XmlNamedAttributeNode(value = "manager", subgraph = "simple"),
                @XmlNamedAttributeNode(value = "organizationUnits", subgraph = "simple"),
                @XmlNamedAttributeNode(value = "investigations", subgraph = "simple"),
                @XmlNamedAttributeNode("startDate"),
                @XmlNamedAttributeNode("endDate")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "Study.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("studyId"),
                @NamedAttributeNode("uniqueIdentifier")}),
    @NamedEntityGraph(
            name = "Study.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("studyId"),
                @NamedAttributeNode("uniqueIdentifier"),
                @NamedAttributeNode("topic"),
                @NamedAttributeNode("note"),
                @NamedAttributeNode("legalNote"),
                @NamedAttributeNode(value = "manager", subgraph = "Study.default.Manager.simple"),
                @NamedAttributeNode(value = "organizationUnits", subgraph = "Study.default.OrganizationUnit.simple"),
                @NamedAttributeNode(value = "investigations", subgraph = "Study.default.Investigation.simple"),
                @NamedAttributeNode("startDate"),
                @NamedAttributeNode("endDate"),
                @NamedAttributeNode("visible"),},
            subgraphs = {
                @NamedSubgraph(
                        name = "Study.default.Manager.simple",
                        attributeNodes = {
                            @NamedAttributeNode("userId")}
                ),
                @NamedSubgraph(
                        name = "Study.default.OrganizationUnit.simple",
                        attributeNodes = {
                            @NamedAttributeNode("relationId")}
                ),
                @NamedSubgraph(
                        name = "Study.default.Investigation.simple",
                        attributeNodes = {
                            @NamedAttributeNode("investigationId")}
                )
            })
})
public class Study implements Serializable, IDefaultStudy, ISecurableResource, FetchGroupTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(Study.class);

    /**
     * UID should be the date of the last change in the format yyyyMMdd.
     */
    private static final long serialVersionUID = 20111201L;
    // <editor-fold defaultstate="collapsed" desc="declaration of variables">
    /**
     * Identification number of the study. primary key of the data set.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studyId;
    /**
     * Unique identifier internally used for linking Authorization to this
     * entity.
     */
    @SecurableResourceIdField(domainName = "edu.kit.dama.mdm.base.Study")
    @Column(nullable = false, unique = true)
    private String uniqueIdentifier;
    /**
     * Topic of the study.
     */
    private String topic;
    /**
     * Note of the study.
     */
    @Column(length = 1024)
    private String note;
    /**
     * Legal note of the study.
     */
    @Column(length = 1024)
    private String legalNote;
    /**
     * Manager of this topic.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @XmlElement(name = "manager")
    private UserData manager;
    /**
     * Organization units which participate.
     */
    @OneToMany(fetch = FetchType.EAGER)
    @XmlElementWrapper(name = "relations")
    @XmlElement(name = "relation")
    @BatchFetch(BatchFetchType.EXISTS)
    private Set<Relation> organizationUnits;
    /**
     * Organization units which participate.
     */
    @OneToMany(mappedBy = "study", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    @XmlElementWrapper(name = "investigations")
    @XmlElement(name = "investigation")
    @BatchFetch(BatchFetchType.EXISTS)
    private Set<Investigation> investigations;
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
     * Study visible or not. Invisible studies may be deleted.
     */
    private Boolean visible = Boolean.TRUE;

    // </editor-fold>
    /**
     * Factory a new study with an auto-generated unique identifier. The
     * identifier is generated using UUID.randomUUID().toString().
     *
     * @return The new study.
     */
    public static Study factoryNewStudy() {
        Study result = new Study();
        result.setUniqueIdentifier(UUID.randomUUID().toString());
        result.setInvestigations(new HashSet<Investigation>());
        result.setOrganizationUnits(new HashSet<Relation>());
        return result;
    }

    /**
     * Factory a new study with a given unique identifier.
     *
     * @param uniqueIdentifier the studies unique id.
     *
     * @return The new study or null if unique identifier is null.
     */
    public static Study factoryNewStudy(String uniqueIdentifier) {
        if (uniqueIdentifier == null) {
            return null;
        }
        Study result = new Study();
        result.setUniqueIdentifier(uniqueIdentifier);
        result.setInvestigations(new HashSet<Investigation>());
        result.setOrganizationUnits(new HashSet<Relation>());

        return result;
    }

    /**
     * Default constructor. When using this constructor, the unique identifier
     * has to be set manually. Otherwise, the object is not suitable for
     * persisting. If you want to persist the object, use factoryNewStudy()
     * instead.
     */
    public Study() {
    }

    // <editor-fold defaultstate="collapsed" desc="setters and getters">
    @Override
    public Long getStudyId() {
        return studyId;
    }

    /**
     * Set study id.
     *
     * @param studyId the study id to set
     */
    public void setStudyId(final Long studyId) {
        this.studyId = studyId;
    }

    /**
     * This method does <b>nothing</b>. The unique identifier is set only
     * automatically once during instantiation.
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
     * Set topic of study.
     *
     * @param topic the topic to set
     */
    public void setTopic(final String topic) {
        this.topic = topic;
    }

    @Override
    public String getNote() {
        return note;
    }

    /**
     * Set note.
     *
     * @param note the note to set
     */
    public void setNote(final String note) {
        this.note = note;
    }

    @Override
    public String getLegalNote() {
        return legalNote;
    }

    /**
     * Set legal note.
     *
     * @param legalNote the legal note to set
     */
    public void setLegalNote(final String legalNote) {
        this.legalNote = legalNote;
    }

    @Override
    public UserData getManager() {
        return manager;
    }

    /**
     * Get the manager of this topic.
     *
     * @param manager the manager to set
     */
    public void setManager(final UserData manager) {
        this.manager = manager;
    }

    @Override
    public Set<Relation> getOrganizationUnits() {
        return organizationUnits;
    }

    /**
     * Set the involved organization unit.
     *
     * @param organizationUnits the organizationUnits to set
     */
    public void setOrganizationUnits(final Set<Relation> organizationUnits) {
        this.organizationUnits = organizationUnits;
    }

    /**
     * Add a OU to the set of organizationUnits.
     *
     * @param organizationUnit the organizationUnit to add
     */
    public void addOrganizationUnit(final OrganizationUnit organizationUnit) {
        addOrganizationUnit(organizationUnit, null);
    }

    /**
     * Add a relation to the set of organizationUnits. Before the relation is
     * added it will be checked whether the provided relation was already added
     * (based on the relation ID) or not.
     *
     * @param pRelation the relation to add.
     */
    public void addRelation(final Relation pRelation) {
        LOGGER.debug("Adding relation with id {} to study with id {}", pRelation.getRelationId(), getStudyId());
        addOrganizationUnit(pRelation.getOrganizationUnit(), pRelation.getTask());
    }

    /**
     * Add an OU with a task to the set of organizationUnits.
     *
     * @param organizationUnit the organizationUnit to add
     * @param task Task of the organization unit.
     */
    public void addOrganizationUnit(final OrganizationUnit organizationUnit, Task task) {
        if (organizationUnits == null) {
            organizationUnits = new HashSet<>();
        }
        if (organizationUnit != null) {
            organizationUnits.add(new Relation(organizationUnit, task));
        } else {
            throw new IllegalArgumentException("Argument organizationUnit must not be null");
        }
    }

    /**
     * Remove a OU of the set of organizationUnits.
     *
     * @param organizationUnit the organizationUnit to remove
     */
    public void removeOrganizationUnit(final OrganizationUnit organizationUnit) {
        if (organizationUnit != null) {
            if (organizationUnits != null) {
                for (Relation relation : organizationUnits) {
                    if (Objects.equals(relation.getOrganizationUnit().getOrganizationUnitId(), organizationUnit.getOrganizationUnitId())) {
                        organizationUnits.remove(relation);
                        break;
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Argument organizationUnit must not be null.");
        }
    }

    @Override
    public Set<Investigation> getInvestigations() {
        return investigations;
    }

    /**
     * Get investigations of this investigation.
     *
     * @param pVisibleOnly TRUE = Return only visible investigations.
     *
     * @return the investigations
     */
    public Set<Investigation> getInvestigations(boolean pVisibleOnly) {
        if (investigations == null) {
            investigations = new HashSet<>();
        }

        if (pVisibleOnly) {
            Set<Investigation> visibleElements = new HashSet<>();
            for (Investigation i : investigations) {
                if (i.isVisible()) {
                    visibleElements.add(i);
                }
            }
            return visibleElements;
        }
        //return all investigations
        return investigations;
    }

    /**
     * Set investigations on this investigation.
     *
     * @param investigations the investigations to set
     */
    public void setInvestigations(Set<Investigation> investigations) {
        this.investigations = investigations;
    }

    /**
     * Add a user to the set of investigations.
     *
     * @param investigation the investigation to add
     */
    public void addInvestigation(final Investigation investigation) {
        if (investigations == null) {
            investigations = new HashSet<>();
        }
        if (investigation != null) {
            investigations.add(investigation);
            investigation.setStudy(this);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Remove a user of the set of investigations.
     *
     * @param investigation the investigation to remove
     */
    public void removeInvestigation(final Investigation investigation) {
        if (investigation != null) {
            if (investigations != null) {
                investigations.remove(investigation);
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
     * Set start date of study.
     *
     * @param startDateOfStudy start date of study
     */
    public void setStartDate(final Date startDateOfStudy) {
        DateTester.testForValidDates(startDateOfStudy, endDate);
        this.startDate = startDateOfStudy;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Set end date of study.
     *
     * @param endDateOfStudy end date of study
     */
    public void setEndDate(final Date endDateOfStudy) {
        DateTester.testForValidDates(startDate, endDateOfStudy);
        this.endDate = endDateOfStudy;
    }

    @Override
    public Boolean isVisible() {
        return this.visible;
    }

    /**
     * Set visibility of study.
     *
     * @param visible visibility of the study
     */
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    // </editor-fold>

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("Study\n----\n");
        buffer.append("Study ID: ").append(getStudyId());
        buffer.append("\nTopic: ").append(topic).
                append("\nLegal Note: ").append(legalNote).append("\n");
        if (manager != null) {
            buffer.append("\nManager ").append(manager);
        }
        if (organizationUnits != null) {
            for (Relation item : organizationUnits) {
                buffer.append(item.toString()).append("\n");
            }
        }
        buffer.append("\nStart Date: ").append(startDate);
        buffer.append("\nEnd Date: ").append(endDate);
        return buffer.toString();
    }

    @Override
    public boolean equals(Object other) {
        boolean equals = true;
        if (this == other) {
            return equals;
        }
        if (other != null && (getClass() == other.getClass())) {
            Study otherStudy = (Study) other;
            if (studyId != null) {
                equals = equals && (studyId.equals(otherStudy.studyId));
            } else {
                equals = equals && (otherStudy.studyId == null);
            }
            if (equals && (topic != null)) {
                equals = equals && (topic.equals(otherStudy.topic));
            } else {
                equals = equals && (otherStudy.topic == null);
            }
            if (equals && (startDate != null)) {
                equals = equals && (startDate.equals(otherStudy.startDate));
            } else {
                equals = equals && (otherStudy.startDate == null);
            }
            if (equals && (endDate != null)) {
                equals = equals && (endDate.equals(otherStudy.endDate));
            } else {
                equals = equals && (otherStudy.endDate == null);
            }
            if (equals && (legalNote != null)) {
                equals = equals && (legalNote.equals(otherStudy.legalNote));
            } else {
                equals = equals && (otherStudy.legalNote == null);
            }
            if (equals && (manager != null)) {
                equals = equals && (manager.equals(otherStudy.manager));
            } else {
                equals = equals && (otherStudy.manager == null);
            }
            if (equals && (note != null)) {
                equals = equals && (note.equals(otherStudy.note));
            } else {
                equals = equals && (otherStudy.note == null);
            }
            if (equals && (visible != null)) {
                equals = equals && (visible.equals(otherStudy.visible));
            } else {
                equals = equals && (otherStudy.visible == null);
            }
            if (equals && (organizationUnits != null)) {
                equals = equals && organizationUnits.size() == otherStudy.organizationUnits.size()
                        && organizationUnits.containsAll(otherStudy.organizationUnits);
            } else {
                equals = equals && (otherStudy.organizationUnits == null);
            }

            if (equals && (investigations != null)) {
                equals = equals && investigations.size() == otherStudy.investigations.size()
                        && investigations.containsAll(otherStudy.investigations);
            } else {
                equals = equals && (otherStudy.investigations == null);
            }
        } else {
            equals = false;
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 73 * hash + (this.studyId != null ? this.studyId.hashCode() : 0);
        hash = 73 * hash + (this.topic != null ? this.topic.hashCode() : 0);
        hash = 73 * hash + (this.note != null ? this.note.hashCode() : 0);
        hash = 73 * hash + (this.legalNote != null ? this.legalNote.hashCode() : 0);
        hash = 73 * hash + (this.manager != null ? this.manager.hashCode() : 0);
        hash = 73 * hash + (this.organizationUnits != null ? this.organizationUnits.hashCode() : 0);
        hash = 73 * hash + (this.investigations != null ? this.investigations.hashCode() : 0);
        hash = 73 * hash + (this.startDate != null ? this.startDate.hashCode() : 0);
        hash = 73 * hash + (this.endDate != null ? this.endDate.hashCode() : 0);
        hash = 73 * hash + (this.visible != null ? this.visible.hashCode() : 0);
        return hash;
    }

    @Override
    public SecurableResourceId getSecurableResourceId() {
        return new SecurableResourceId("edu.kit.dama.mdm.base.Study", getUniqueIdentifier());
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
