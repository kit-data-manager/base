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

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Relation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.slf4j.LoggerFactory;

/**
 *
 * @author yc0475
 */
public class MigrateObject {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MigrateObject.class);

  
  
  /**
   * The insertion of a digital object requires to map entities of the file with the 
   * entities of the added entities because the primary key between the the xml file
   * and the database differ. 
   * 
   * Comment 2015-06-02 Sasa
   * The main method has to be decomposed into methods for readability.
   */
  public static void main(String[] args) throws Exception {

    String filename = "/home/linsec/vondrous/tmp/migrationObject2.xml";
    String userId = "foobar";

    AuthorizationContext ctx;
    IMetaDataManager mdm;

    JAXBContext jc;
    File file;

    DigitalObject digitalObject;
    Investigation investigation;
    Study study;

    // Hash maps are used to map objects of the migration object (xml file) with the objects of the database.
    UserData user;
    UserData manager;
    UserData uploader;
    Set<UserData> experimenters = new HashSet<UserData>();
    HashMap<UserData, UserData> userDataMapping = new HashMap<UserData, UserData>();

    MetaDataSchema metadataSchema;
    Set<MetaDataSchema> metadataSchemas = new HashSet<MetaDataSchema>();
    HashMap<MetaDataSchema, MetaDataSchema> metadataSchemasMapping = new HashMap<MetaDataSchema, MetaDataSchema>();

    Participant participant;
    Set<Participant> participants = new HashSet<Participant>();
    HashMap<Participant, Participant> participantsMapping = new HashMap<Participant, Participant>();

    Task task;
    HashMap<Task, Task> tasksMapping = new HashMap<Task, Task>();

    Relation relation;
    Set<Relation> relations = new HashSet<Relation>();
    HashMap<Relation, Relation> relationsMapping = new HashMap<Relation, Relation>();

    OrganizationUnit organization;
    HashMap<OrganizationUnit, OrganizationUnit> organizationsMapping = new HashMap<OrganizationUnit, OrganizationUnit>();

    MigrationObject migrationObject;

    Query query;

    
    // Get a metadata manager.
    mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    ctx = new AuthorizationContext(new UserId(userId), new GroupId("USERS"), Role.MEMBER);
    mdm.setAuthorizationContext(ctx);
    jc = JAXBContext.newInstance(MigrationObject.class);

    // Read a migration object.
    file = new File(filename);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    migrationObject = (MigrationObject) unmarshaller.unmarshal(file);

    
    // Start the base metadata migration.
    EntityManagerFactory emfactory = Persistence.createEntityManagerFactory("MDM-Core");
    EntityManager entitymanager = emfactory.createEntityManager();
    try {
      entitymanager.getTransaction().begin();

      
      
      // Add users to the database.
      query = entitymanager.createQuery("SELECT u FROM UserData u WHERE u.distinguishedName = ?1");
      for (UserData migrationUser : migrationObject.getUserData()) {
        query.setParameter(1, migrationUser.getDistinguishedName());
        List<UserData> list = query.getResultList();
        if (list.isEmpty()) {
          user = copyMigrationUser(migrationUser);
          user = mdm.persist(user);
          LOGGER.info("User " + user.getDistinguishedName() + " migrated.");
        } else {
          user = list.get(0);
          LOGGER.info("Migration of user " + migrationUser.getDistinguishedName() + " not performed, beacuse user is already present.");
        }
        userDataMapping.put(migrationUser, user);
      }
      
      
      
      // Add tasks to the database.
      query = entitymanager.createQuery("SELECT t FROM Task t WHERE t.task = ?1");
      for (Task migrationTask : migrationObject.getTasks()) {
        query.setParameter(1, migrationTask.getTask());
        List<Task> list = query.getResultList();
        if (list.isEmpty()) {
          task = new Task(migrationTask.getTask());
          task = mdm.persist(task);
          LOGGER.info("Task " + task.getTask() + " migrated.");
        } else {
          task = list.get(0);
          LOGGER.info("Migration of task " + task.getTask() + " not performed, beacuse task is already present.");
        }
        tasksMapping.put(migrationTask, task);
      }

      
      
      // Add organization unit to the database.
      query = entitymanager.createQuery("SELECT o FROM OrganizationUnit o WHERE o.ouName = ?1");
      for (OrganizationUnit migrationOrganization : migrationObject.getOrganizationunits()) {
        query.setParameter(1, migrationOrganization.getOuName());
        List<OrganizationUnit> list = query.getResultList();
        if (list.isEmpty()) {
          if (migrationOrganization.getManager() == null) {
            manager = null;
          } else {
            // Get the proper migration user from the file.
            manager = MigrationObject.getUserByPrimaryKey(migrationObject.getUserData(), migrationOrganization.getManager().getUserId());
            if (manager == null) {
              throw new Exception("The manager of the organization " + migrationOrganization.getOuName() + " is not present in the input file.");
            }
            // Now get the mapped user, which is already migrated and stored in the database.
            manager = userDataMapping.get(manager);
          }
          organization = copyOrganizationUnit(migrationOrganization);
          organization.setManager(manager);
          organization = mdm.persist(organization);
          
          LOGGER.info("Organization " + migrationOrganization.getOuName() + " migrated.");
        } else {
          organization = list.get(0);
          LOGGER.info("Migration of organization " + migrationOrganization.getOuName() + " not performed, because orgnaization is already present.");
        }
        organizationsMapping.put(migrationOrganization, organization);
      }



      // Add metadata schemas to the database.
      query = entitymanager.createQuery("SELECT m FROM MetaDataSchema m WHERE m.schemaIdentifier = ?1");
      for (MetaDataSchema migrationMetaDataSchema : migrationObject.getMetaDataSchema()) {
        query.setParameter(1, migrationMetaDataSchema.getSchemaIdentifier());
        List<MetaDataSchema> list = query.getResultList();
        if (list.isEmpty()) {
          metadataSchema = new MetaDataSchema(migrationMetaDataSchema.getSchemaIdentifier(), migrationMetaDataSchema.getMetaDataSchemaUrl());
          metadataSchema = mdm.persist(metadataSchema);
          LOGGER.info("Metadata schema " + migrationMetaDataSchema.getId() + " migrated.");
        } else {
          metadataSchema = list.get(0);
          LOGGER.info("Migration of metadata schema " + migrationMetaDataSchema.getId() + " not performed, beacuse meta data schema is already present.");
        }
        metadataSchemasMapping.put(migrationMetaDataSchema, metadataSchema);
      }
      
      
      
      // Add participants to the database.
      for (Participant migrationParticipant : migrationObject.getParticipants()) {
        task = null;
        if (migrationParticipant.getTask() != null) {
          task = MigrationObject.getTaskByPrimaryKey(migrationObject.getTasks(), migrationParticipant.getTask().getTaskId());
          if (task == null) {
            throw new Exception("The participant with the id " + migrationParticipant.getParticipantId() + " contains a task, which is not present in the input file.");
          }
          if (task.getTask() == null) {
            throw new Exception("The task with the id " + task.getTaskId() + " contains an empty task.");
          }
        }
        user = null;
        if (migrationParticipant.getUser() != null) {
          user = MigrationObject.getUserByPrimaryKey(migrationObject.getUserData(), migrationParticipant.getUser().getUserId());
          if (user == null) {
            throw new Exception("The participant with the id " + migrationParticipant.getParticipantId() + " contains a user, which is not present in the input file.");
          }
          if (user.getDistinguishedName() == null) {
            throw new Exception("The user with the id " + user.getUserId() + " contains an empty distinguished name.");
          }
        }
        // No migration of an empty participant is performed.
        if (task == null && user == null) {
          LOGGER.info("Participant with the id " + migrationParticipant.getParticipantId() + " of the input file is not migrated because it is empty.");
          participantsMapping.put(migrationParticipant, null);
          continue;
        }
        
        // Create the query according to the presence of a task and organization.
        // Only task is available.
        if (user == null) {
          query = entitymanager.createQuery("SELECT p "
                                         + " FROM Participant p, Task t "
                                         + " WHERE p.task = t AND t.task = ?1 AND p.user IS NULL");
          query.setParameter(1, task.getTask());
        }
        // Only user is available.
        if (task == null) {
          query = entitymanager.createQuery("SELECT p "
                                         + " FROM Participant p, UserData u "
                                         + " WHERE p.user = u AND u.distinguishedName = ?1 AND p.task IS NULL");
          query.setParameter(1, user.getDistinguishedName());
        }
        // Task and user are available.
        if (user != null && task != null) {
          query = entitymanager.createQuery("SELECT p "
                                         + " FROM Participant p, UserData u, Task t "
                                         + " WHERE p.user = u AND p.task = t AND u.distinguishedName = ?1 AND t.task = ?2 ");
          query.setParameter(1, user.getDistinguishedName());
          query.setParameter(2, task.getTask());
        }

        // Persit participant.
        List<Participant> list = query.getResultList();
        if (list.isEmpty()) {
          // Add the already migrated user and task which are stored in the database.
          participant = new Participant(userDataMapping.get(user), tasksMapping.get(task));
          participant = mdm.persist(participant);
          LOGGER.info("Participant " + migrationParticipant.getParticipantId() + " migrated.");
          
        } else {
          // It is possible to get more relations for the same organization and task. The first relation is used for the mapping.
          participant = list.get(0);
          LOGGER.info("Migration of participant " + migrationParticipant.getParticipantId() + " not performed, because participant is already present.");
          if (list.size() > 1) {
            LOGGER.warn("More than one participant for the given user and task found. Please check for consistency.");
          }
        }
        participantsMapping.put(migrationParticipant, participant);
      }



      // Add relations to the database.   
      for (Relation migrationRelation : migrationObject.getRelations()) {
        organization = null;
        if (migrationRelation.getOrganizationUnit() != null) {
          organization = MigrationObject.getOrganizationUnitByPrimaryKey(migrationObject.getOrganizationunits(), migrationRelation.getOrganizationUnit().getOrganizationUnitId());
          if (organization == null) {
            throw new Exception("The relation with the id " + migrationRelation.getRelationId()+ " contains a organization, which is not present in the input file.");
          }
          if (organization.getOuName() == null) {
            throw new Exception("The organization with the id " + organization.getOrganizationUnitId() + " contains an empty organization name.");
          }
        }
        task = null;
        if (migrationRelation.getTask() != null) {
          task = MigrationObject.getTaskByPrimaryKey(migrationObject.getTasks(), migrationRelation.getTask().getTaskId());
          if (task == null) {
            throw new Exception("The relation with the id " + migrationRelation.getRelationId()+ " contains a task, which is not present in the input file.");
          }
          if (task.getTask() == null) {
            throw new Exception("The task with the id " + task.getTaskId() + " contains an empty task.");
          }
        }
        // No migration of an empty relation is performed.
        if (organization == null && task == null) {
          LOGGER.info("Relation with the id " + migrationRelation.getRelationId() + " of the input file is not migrated because it is empty.");
          relationsMapping.put(migrationRelation, null);
          continue;
        }
        
        // Create the query according to the presence of a task and organization.
        // Only task is available.
        if (organization == null) {
          query = entitymanager.createQuery("SELECT r "
                                         + " FROM Relation r, Task t "
                                         + " WHERE r.task = t AND t.task = ?1 AND r.organizationUnit IS NULL ");
          query.setParameter(1, task.getTask());
        }
        // Only organization is available.
        if (task == null) {
          query = entitymanager.createQuery("SELECT r "
                                         + " FROM Relation r, OrganizationUnit o "
                                         + " WHERE r.organizationUnit = o AND o.ouName = ?1 AND r.task IS NULL ");
          query.setParameter(1, organization.getOuName());
        }
        // Task and organization are available.
        if (organization != null && task != null) {
          query = entitymanager.createQuery("SELECT r "
                                         + " FROM Relation r, OrganizationUnit o, Task t "
                                         + " WHERE r.organizationUnit = o AND r.task = t AND o.ouName = ?1 AND t.task = ?2 ");
          query.setParameter(1, organization.getOuName());
          query.setParameter(2, task.getTask());
        }
        
        // Persit the relation.
        List<Relation> list = query.getResultList();
        if (list.isEmpty()) {
          // Add the already migrated ogranization and tasks, which are stored in the database.
          relation = new Relation(organizationsMapping.get(organization), tasksMapping.get(task));
          relation = mdm.persist(relation);
          LOGGER.info("Relation " + migrationRelation.getRelationId() + " migrated.");
          
        } else {
          // It is possible to get more relations for the same organization and task. The first relation is used for the mapping.
          relation = list.get(0);
          LOGGER.info("Migration of relation " + migrationRelation.getRelationId() + " not performed, because relation is already present.");
          if (list.size() > 1) {
            LOGGER.warn("More than one relation for the given organization and task found. Please check for consistency.");
          }
        }
        relationsMapping.put(migrationRelation, relation);
      }



      // Add study to the database.
      query = entitymanager.createQuery("SELECT s FROM Study s WHERE s.uniqueIdentifier = ?1");
      query.setParameter(1, migrationObject.getStudy().getUniqueIdentifier());
      List<Study> studyList = query.getResultList();
      if (studyList.isEmpty()) {
        manager = MigrationObject.getUserByPrimaryKey(migrationObject.getUserData(), migrationObject.getStudy().getManager().getUserId());
        if (manager == null) {
          throw new Exception("No manger could be found in the migration file.");
        }
        
        for (Relation migrationRelation : migrationObject.getStudy().getOrganizationUnits()) {
          // get relation stored in the file by its id.
          relation = MigrationObject.getRelationByPrimaryKey(migrationObject.getRelations(), migrationRelation.getRelationId());
          if (relation == null) {
            throw new Exception("The study contains a relation, which is not written in the file.");
          }
          // get the already migrated relation stored in the database.
          relation = relationsMapping.get(relation);
          relations.add(relation);
        }
        if (relations.isEmpty()) {
          relations = null;
        }
        
        study = copyMigrationStudy(migrationObject.getStudy());
        study.setManager(manager);
        study.setOrganizationUnits(relations);
        study = mdm.persist(study);
        LOGGER.info("Study " + study.getUniqueIdentifier() + " migrated.");
      } else {
        study = studyList.get(0);
        LOGGER.info("Migration of study " + study.getUniqueIdentifier() + " not performed, beacuse study is already present.");
      }
      // Study has to be updated, when the corresponding investigation is created.
      
      
      
      // Add investigation to the database.
      query = entitymanager.createQuery("SELECT i FROM Investigation i WHERE i.uniqueIdentifier = ?1");
      query.setParameter(1, migrationObject.getInvestigation().getUniqueIdentifier());
      List<Investigation> investigationList = query.getResultList();
      if (investigationList.isEmpty()) {
        
        for (MetaDataSchema migrationmetadataschema : migrationObject.getInvestigation().getMetaDataSchema()) {
          // get the metadata schema stored in the file by its id.
          metadataSchema = MigrationObject.getMetadataschemaByPrimaryKey(migrationObject.getMetaDataSchema(), migrationmetadataschema.getId());          
          if (metadataSchema == null) {
            throw new Exception("The investigation contains a metadata schema, which is not written in the file.");
          }
          // get the already migrated metadata schema stored in the database.
          metadataSchema = metadataSchemasMapping.get(metadataSchema);
          metadataSchemas.add(metadataSchema);
        }
        
        for (Participant migrationparticipant : migrationObject.getInvestigation().getParticipants()) {
          // get the participant stored in the file by its id.
          participant = MigrationObject.getParticipantByPrimaryKey(migrationObject.getParticipants(), migrationparticipant.getParticipantId());          
          if (participant == null) {
            throw new Exception("The investigation contains a participant, which is not written in the file.");
          }
          // get the already migrated participant stored in the database.
          participant = participantsMapping.get(participant);
          participants.add(participant);
        }

        investigation = copyMigrationInvestigation(migrationObject.getInvestigation());
        investigation.setStudy(study);
        investigation.setMetaDataSchema(metadataSchemas);
        investigation.setParticipants(participants);
        investigation = mdm.persist(investigation);
        LOGGER.info("Investigation " + study.getUniqueIdentifier() + " migrated.");
      } else {
        investigation = investigationList.get(0);
        LOGGER.info("Migration of investigation " + study.getUniqueIdentifier() + " not performed, beacuse investigation is already present.");
      }
      // Investigation has to be updated, when the corresponding digital object (dataset) is created.
      
      
      
      // Add digital object to the database.
      query = entitymanager.createQuery("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier = ?1");
      query.setParameter(1, migrationObject.getDigitalObject().getDigitalObjectIdentifier());
      List<DigitalObject> digitalObjectList = query.getResultList();
      if (digitalObjectList.isEmpty()) {
        
        uploader = migrationObject.getDigitalObject().getUploader();
        if (uploader != null) {
          // get the user stored in the file by its id.
          uploader = MigrationObject.getUserByPrimaryKey(migrationObject.getUserData(), uploader.getUserId());
          if (uploader == null) {
            throw new Exception("The digital object contains a uploader, which is not written in the file.");
          }
          // get the already migrated user stored in the database.
          uploader = userDataMapping.get(uploader);
        }
        
        for (UserData migrationuser : migrationObject.getDigitalObject().getExperimenters()) {
          // get the user stored in the file by its id.
          user = MigrationObject.getUserByPrimaryKey(migrationObject.getUserData(), migrationuser.getUserId());
          if (user == null) {
            throw new Exception("The digital object contains user, which is not written in the file.");
          }
          // get the already migrated user stored in the database.
          user = userDataMapping.get(user);
          experimenters.add(user);
        }
        
        digitalObject = copyMigrationDigitalObject(migrationObject.getDigitalObject());
        digitalObject.setInvestigation(investigation);
        digitalObject.setUploader(uploader);
        digitalObject.setExperimenters(experimenters);
        digitalObject = mdm.persist(digitalObject);
        LOGGER.info("Digital object " + study.getUniqueIdentifier() + " migrated.");
      } else {
        digitalObject = digitalObjectList.get(0);
        LOGGER.info("Migration of digital object " + study.getUniqueIdentifier() + " not performed, beacuse digital object is already present.");
      }
      
      
      
      // Update the relations between study, investigation and digital object.
      // Add digital object to the investigation.
      if (investigation.getDataSets() == null) {
        throw new Exception("The investigation object did not initialize the set data structure.");
      }
      boolean digitalObjectIsPartOfInvestigation = false;
      for (DigitalObject migrationDigitalObject : migrationObject.getInvestigation().getDataSets()) {
        if (migrationDigitalObject.getBaseId().longValue() == migrationObject.getDigitalObject().getBaseId()) {
          digitalObjectIsPartOfInvestigation = true;
        }
      }
      if (!digitalObjectIsPartOfInvestigation) {
        throw new Exception("The investigation does not contain the provieded digital object in the migration file.");
      }
      if (investigation.getDataSets().add(digitalObject))  {
        LOGGER.info("Link Investigation -> DigitalObject: Digital object added to the datasets list of the investiation.");
        mdm.update(investigation);
      } else {
        LOGGER.info("Link Investigation -> DigitalObject: Digital object already in the list of the corresponding investiation.");
      }
      

      // Add the investigation to the study. Link Investigation -> Study: 
      if (study.getInvestigations() == null) {
        throw new Exception("The study object did not initialize the set data structure.");
      }
      boolean investigationIsPartOfStudy = false;
      for (Investigation migrationInvestigation : migrationObject.getStudy().getInvestigations()) {
        if (migrationInvestigation.getInvestigationId().longValue() == migrationObject.getInvestigation().getInvestigationId()) {
          investigationIsPartOfStudy = true;
        }
      }
      if (!investigationIsPartOfStudy) {
        throw new Exception("The study does not contain the provieded investigation in the migration file.");
      }
      
      if (study.getInvestigations().add(investigation))  {
        LOGGER.info("Link Study -> Investigation: Investigation added to the investigations list of the study.");
        mdm.update(investigation);
      } else {
        LOGGER.info("Link Study -> Investigation: Investigation already in the list of the corresponding study.");
      }      
      
    } catch (Exception e) {
      LOGGER.error(e.getMessage());
      LOGGER.error("Mirgration is not performed. Rollback of all modifications in the database.");
      if (entitymanager.getTransaction().isActive()) {
        entitymanager.getTransaction().rollback();
      }
    } finally {
      if (entitymanager.getTransaction().isActive()) {
        LOGGER.info("Mirgration successfully performed.");
        entitymanager.getTransaction().commit();
      }
    }

    mdm.close();
  }

 
  
  
  /**
   * Creates a new user and copies all fields captured by the migration.
   *
   * The primary key is not set because the destination repository will generate a
   * primary key.
   *
   * @param user copy is made from this.
   *
   * @return a new user with the field captured by the migration.
   */
  private static UserData copyMigrationUser(UserData user) {
    UserData copy;

    if (user == null) {
      throw new NullPointerException("Copy of a null UserData object.");
    }
    
    copy = new UserData();
    copy.setDistinguishedName(user.getDistinguishedName());
    copy.setEmail(user.getEmail());
    copy.setFirstName(user.getFirstName());
    copy.setLastName(user.getLastName());

    return copy;
  }

  /**
   * Creates a new organization unit and copies all fields captured by the migration.
   *
   * The primary key is not set because the destination repository will generate a
   * primary key.
   *
   * @param migrationOrganization copy is made from this.
   *
   * @return a new organization unit with the field captured by the migration.
   */
  private static OrganizationUnit copyOrganizationUnit(OrganizationUnit migrationOrganization) {
    OrganizationUnit copy;

    if (migrationOrganization == null) {
      throw new NullPointerException("The migrationOrganization is null.");
    }

    copy = new OrganizationUnit();
    copy.setOuName(migrationOrganization.getOuName());
    copy.setAddress(migrationOrganization.getAddress());
    copy.setZipCode(migrationOrganization.getZipCode());
    copy.setCity(migrationOrganization.getCity());
    copy.setCountry(migrationOrganization.getCountry());
    copy.setWebsite(migrationOrganization.getWebsite());
    // The manager is a complex data structure, which have to be set outside.
    // copy.setManager(manager);

    return copy;
  }

  /**
   * Creates a new study and copies all fields captured by the migration.
   *
   * The primary key is not set because the destination repository will generate a
   * primary key.
   *
   * @param migrationStudy copy is made from this.
   *
   * @return a new study with the field captured by the migration.
   */
  private static Study copyMigrationStudy(Study migrationStudy) {
    Study copy;

    if (migrationStudy == null) {
      throw new NullPointerException("The migration study is null.");
    }
    
    copy = Study.factoryNewStudy(migrationStudy.getUniqueIdentifier());
    copy.setTopic(migrationStudy.getTopic());
    copy.setNote(migrationStudy.getNote());
    copy.setLegalNote(migrationStudy.getLegalNote());
    // The following commented lines of code indicate complex data structures,
    // which have to be set outside.
    // copy.setManager(manager);
    // copy.setOrganizationUnits(organizationUnits);
    // copy.setInvestigations(null);
    copy.setStartDate(migrationStudy.getStartDate());
    copy.setEndDate(migrationStudy.getEndDate());
    
    return copy;
  }
  
  /**
   * Creates a new investigation and copies all fields captured by the migration.
   *
   * The primary key is not set because the destination repository will generate a
   * primary key.
   *
   * @param migrationInvestigation copy is made from this.
   *
   * @return a new investigation with the field captured by the migration.
   */
  private static Investigation copyMigrationInvestigation(Investigation migrationInvestigation) {
    Investigation copy;

    if (migrationInvestigation == null) {
      throw new NullPointerException("The migration investigatiob is null.");
    }
    copy = Investigation.factoryNewInvestigation(migrationInvestigation.getUniqueIdentifier());
    copy.setTopic(migrationInvestigation.getTopic());
    copy.setNote(migrationInvestigation.getNote());
    copy.setDescription(migrationInvestigation.getDescription());
    // The following commented lines of code indicate complex data structures,
    // which have to be set outside.
    // copy.setStudy(...)
    // copy.setMetaDataSchema(...)
    // copy.setParticipants(...)
    // copy.setDataSets(...)
    copy.setStartDate(migrationInvestigation.getStartDate());
    copy.setEndDate(migrationInvestigation.getEndDate());
    return copy;
  }
  
  /**
   * Creates a new investigation and copies all fields captured by the migration.
   *
   * The primary key is not set because the destination repository will generate a
   * primary key.
   *
   * @param migrationInvestigation copy is made from this.
   *
   * @return a new investigation with the field captured by the migration.
   */
  private static DigitalObject copyMigrationDigitalObject(DigitalObject migrationDigitalObject) {
    DigitalObject copy;

    if (migrationDigitalObject == null) {
      throw new NullPointerException("The migration investigatiob is null.");
    }
    copy = DigitalObject.factoryNewDigitalObject(migrationDigitalObject.getDigitalObjectIdentifier());
    copy.setLabel(migrationDigitalObject.getLabel());
    copy.setNote(migrationDigitalObject.getNote());
    // The following commented lines of code indicate complex data structures,
    // which have to be set outside.
    // copy.setInvestigation(...);
    // copy.setUploader(...);
    // copy.setExperimenters(...);
    copy.setStartDate(migrationDigitalObject.getStartDate());
    copy.setEndDate(migrationDigitalObject.getEndDate());
    copy.setUploadDate(migrationDigitalObject.getUploadDate());
    return copy;
  }
  
  // Comment 2015-06-02 by Sasa
  // The following code does not follow the DRY principle, such that optimization is 
  // possible and necessary. The code is written to make things possible.
  
}
