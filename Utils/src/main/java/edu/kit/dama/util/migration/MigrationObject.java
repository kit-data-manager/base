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

package edu.kit.dama.util.migration;

import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Relation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.UserData;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 *
 * @author yc0475
 */
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode(value = "digitalObject",     subgraph = "default"),
            @XmlNamedAttributeNode(value = "investigation",     subgraph = "default"),
            @XmlNamedAttributeNode(value = "study",             subgraph = "default"),
            @XmlNamedAttributeNode(value = "metaDataSchema",    subgraph = "default"),
            @XmlNamedAttributeNode(value = "participants",      subgraph = "default"),
            @XmlNamedAttributeNode(value = "tasks",             subgraph = "default"),
            @XmlNamedAttributeNode(value = "organizationunits", subgraph = "default"),
            @XmlNamedAttributeNode(value = "relations",         subgraph = "default"),
            @XmlNamedAttributeNode(value = "userData",          subgraph = "default")
          })})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class MigrationObject {
  
  // BASE META DATA 1.0

  private DigitalObject digitalObject;
  private Study         study;
  private Investigation investigation;
  
  @XmlElementWrapper(name = "userData")
  @XmlElement(name = "user")
  private Set<UserData> userData;
  
  @XmlElementWrapper(name = "metaDataSchemas")
  @XmlElement(name = "metaDataSchema")
  private Set<MetaDataSchema> metaDataSchema;
  
  @XmlElementWrapper(name = "participants")
  @XmlElement(name = "participant")
  private Set<Participant> participants;

  // The naming (taskobject) is iritationg otherwise a task would have a task 
  // inside, which is in my opinion a little more irritating.
  @XmlElementWrapper(name = "tasks")
  @XmlElement(name = "taskobject")
  private Set<Task> tasks;
  
  @XmlElementWrapper(name = "relations")
  @XmlElement(name = "relation")
  private Set<Relation> relations;
  
  @XmlElementWrapper(name = "organizations")
  @XmlElement(name = "organization")
  private Set<OrganizationUnit> organizationunits;
  

  public MigrationObject() {
    this.metaDataSchema    = new HashSet<MetaDataSchema>();
    this.userData          = new HashSet<UserData>();
    this.participants      = new HashSet<Participant>();
    this.tasks             = new HashSet<Task>();
    this.relations         = new HashSet<Relation>();
    this.organizationunits = new HashSet<OrganizationUnit>();
  }

  /**
   * @return the digitalObject
   */
  public DigitalObject getDigitalObject() {
    return digitalObject;
  }

  /**
   * @param digitalObject the digitalObject to set
   */
  public void setDigitalObject(DigitalObject digitalObject) {
    this.digitalObject = digitalObject;
  }

  /**
   * @return the study
   */
  public Study getStudy() {
    return study;
  }

  /**
   * @param study the study to set
   */
  public void setStudy(Study study) {
    this.study = study;
  }

  /**
   * @return the investigation
   */
  public Investigation getInvestigation() {
    return investigation;
  }

  /**
   * @param investigation the investigation to set
   */
  public void setInvestigation(Investigation investigation) {
    this.investigation = investigation;
  }

  /**
   * @return the userData
   */
  public Set<UserData> getUserData() {
    return userData;
  }

  /**
   * @param userData the userData to set
   */
  public void setUserData(Set<UserData> userData) {
    this.userData = userData;
  }

  /**
   * @return the metaDataSchema
   */
  public Set<MetaDataSchema> getMetaDataSchema() {
    return metaDataSchema;
  }

  /**
   * @param metaDataSchema the metaDataSchema to set
   */
  public void setMetaDataSchema(Set<MetaDataSchema> metaDataSchema) {
    this.metaDataSchema = metaDataSchema;
  }

  /**
   * @return the participants
   */
  public Set<Participant> getParticipants() {
    return participants;
  }

  /**
   * @param participants the participants to set
   */
  public void setParticipants(Set<Participant> participants) {
    this.participants = participants;
  }

  /**
   * @return the tasks
   */
  public Set<Task> getTasks() {
    return tasks;
  }

  /**
   * @param tasks the tasks to set
   */
  public void setTasks(Set<Task> tasks) {
    this.tasks = tasks;
  }

  /**
   * @return the organizationUnits
   */
  public Set<Relation> getRelations() {
    return relations;
  }

  /**
   * @param organizationUnits the organizationUnits to set
   */
  public void setRelations(Set<Relation> organizationUnits) {
    this.relations = organizationUnits;
  }

  /**
   * @return the organizationunits
   */
  public Set<OrganizationUnit> getOrganizationunits() {
    return organizationunits;
  }

  /**
   * @param organizationunits the organizationunits to set
   */
  public void setOrganizationunits(Set<OrganizationUnit> organizationunits) {
    this.organizationunits = organizationunits;
  }
  
  
    /**
   * Searches for a user in the given set by its primary key.
   *
   * A null valued primary key or a null valued user data set are not allowed.
   *
   * @param userData set of users.
   * @param primaryKey to search the user.
   *
      * @return a user with the given primary key or null if not found.
   */ 
  public static UserData getUserByPrimaryKey(Set<UserData> userData, Long primaryKey) {
    if (userData == null) {
      throw new NullPointerException("The userData set is null.");
    }
    if (primaryKey == null) {
      throw new NullPointerException("The primaryKey is null.");
    }
    
    for (UserData user : userData) {
      if (user.getUserId() != null && user.getUserId() == primaryKey.longValue()) {
        return user;
      }
    }
    return null;
  }
  
  /**
   * Searches for a organization in the given set by its primary key.
   *
   * A null valued primary key or a null valued organization set are not allowed.
   *
   * @param organizations set of organizations.
   * @param primaryKey to search the organization.
   *
   * @return an organization unit with the given primary key or null if not found.
   */ 
  public static OrganizationUnit getOrganizationUnitByPrimaryKey(Set<OrganizationUnit> organizations, Long primaryKey) {    if (organizations == null) {
      throw new NullPointerException("The organizations set is null.");
    }
    if (primaryKey == null) {
      throw new NullPointerException("The primaryKey is null.");
    }
    
    for (OrganizationUnit organization : organizations) {
      if (organization.getOrganizationUnitId() != null && organization.getOrganizationUnitId() == primaryKey.longValue()) {
        return organization;
      }
    }
    return null;
  }

  /**
   * Searches for a task in the given set by its primary key.
   *
   * A null valued primary key or a null valued task set are not allowed.
   *
   * @param tasks set of tasks.
   * @param primaryKey to search the task.
   *
   * @return a task with the given primary key or null if not found.
   */ 
  public static Task getTaskByPrimaryKey(Set<Task> tasks, Long primaryKey) {
    if (tasks == null) {
      throw new NullPointerException("The task set is null.");
    }
    if (primaryKey == null) {
      throw new NullPointerException("The primaryKey is null.");
    }
    
    for (Task task : tasks) {
      if (task.getTaskId() != null && task.getTaskId() == primaryKey.longValue()) {
        return task;
      }
    }
    return null;
  }
  
  
  
  /**
   * Searches for a relation in the given set by its primary key.
   *
   * A null valued primary key or a null valued relation set are not allowed.
   *
   * @param relations set of relations.
   * @param primaryKey to search the relation.
   *
   * @return a relation with the given primary key or null if not found.
   */ 
  public static Relation getRelationByPrimaryKey(Set<Relation> relations, Long primaryKey) {
    if (relations == null) {
      throw new NullPointerException("The relation set is null.");
    }
    if (primaryKey == null) {
      throw new NullPointerException("The primaryKey is null.");
    }
    
    for (Relation relation : relations) {
      if (relation.getRelationId()!= null && relation.getRelationId() == primaryKey.longValue()) {
        return relation;
      }
    }
    return null;
  }
    
  /**
   * Searches for a metadata schema in the given set by its primary key.
   *
   * A null valued primary key or a null valued metadata schema set are not allowed.
   *
   * @param relations set of metadata schemas.
   * @param primaryKey to search the metadata schema.
   *
   * @return a metadata schema with the given primary key or null if not found.
   */ 
  public static MetaDataSchema getMetadataschemaByPrimaryKey(Set<MetaDataSchema> metadataschemas, Long primaryKey) {
    if (metadataschemas == null) {
      throw new NullPointerException("The metadata schema set is null.");
    }
    if (primaryKey == null) {
      throw new NullPointerException("The primaryKey is null.");
    }
    
    for (MetaDataSchema metadataschema : metadataschemas) {
      if (metadataschema.getId() != null && metadataschema.getId() == primaryKey.longValue()) {
        return metadataschema;
      }
    }
    return null;
  }
  
  /**
   * Searches for a participant in the given set by its primary key.
   *
   * A null valued primary key or a null valued participant set are not allowed.
   *
   * @param relations set of participant.
   * @param primaryKey to search the participant.
   *
   * @return a participant with the given primary key or null if not found.
   */ 
  public static Participant getParticipantByPrimaryKey(Set<Participant> participants, Long primaryKey) {
    if (participants == null) {
      throw new NullPointerException("The participant set is null.");
    }
    if (primaryKey == null) {
      throw new NullPointerException("The primaryKey is null.");
    }
    
    for (Participant participant : participants) {
      if (participant.getParticipantId() != null && participant.getParticipantId() == primaryKey.longValue()) {
        return participant;
      }
    }
    return null;
  }

  
}
