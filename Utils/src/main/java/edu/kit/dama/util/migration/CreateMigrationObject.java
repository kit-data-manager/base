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
import edu.kit.dama.util.migration.MigrationObject;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.slf4j.LoggerFactory;

/**
 *
 * @author yc0475
 */
public class CreateMigrationObject {
  
  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CreateMigrationObject.class);
  
  public static void main(String[] args) throws Exception {

    String filename = "/home/linsec/vondrous/tmp/migrationObject.xml";
    long   id     = 1;
    String userId = "foobar";
    
    AuthorizationContext ctx;
    IMetaDataManager mdm;

    Marshaller marshaller;
    JAXBContext jc;
    File file;

    DigitalObject         object;
    Investigation         investigation;
    Study                 study;
    UserData              user;
    Set<UserData>         userData = new HashSet<UserData>();
    MetaDataSchema        metadataSchema;
    Set<MetaDataSchema>   metadataSchemas = new HashSet<MetaDataSchema>();
    Participant           participant;
    Set<Participant>      participants = new HashSet<Participant>();    
    Task                  task;
    Set<Task>             tasks = new HashSet<Task>();
    Relation              relation;
    Set<Relation>         relations = new HashSet<Relation>();
    OrganizationUnit      organization;
    Set<OrganizationUnit> organizations = new HashSet<OrganizationUnit>();
    
    MigrationObject migrationObject;
    
    
    mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();      
    ctx = new AuthorizationContext(new UserId(userId), new GroupId("USERS"), Role.MEMBER);
    mdm.setAuthorizationContext(ctx);

    object = mdm.find(DigitalObject.class, id);
    if (object == null) {
      LOGGER.error("Digital object with id " + id + " not found.");
      return;
    }
    
    investigation = mdm.find(Investigation.class, object.getInvestigation().getInvestigationId());
    study         = mdm.find(Study.class, investigation.getStudy().getStudyId());
    
    // Get all users from digital object and study.
    if (object.getUploader() != null) {
      user = mdm.find(UserData.class, object.getUploader().getUserId());
      if (user != null) {
        userData.add(user);
      }        
    }
    for (UserData experimenter : object.getExperimenters()) {
      user = mdm.find(UserData.class, experimenter.getUserId());
      if (user != null) {
        userData.add(user);
      }
    }
    if (study.getManager() != null) {
      user = mdm.find(UserData.class, study.getManager().getUserId());
      if (user != null) {
        userData.add(user);
      }
    }
    
    // Get metadata schemas.
    for (MetaDataSchema metaDataSchemaIter : investigation.getMetaDataSchema()) {
      metadataSchema = mdm.find(MetaDataSchema.class, metaDataSchemaIter.getId());
      if (metadataSchema != null) {
        metadataSchemas.add(metadataSchema);
      }
    }
    
    // Get participants.
    for (Participant participantIter : investigation.getParticipants()) {
      participant = mdm.find(Participant.class, participantIter.getParticipantId());
      if (participant != null) {
        participants.add(participant);
        // Get task of participant.
        task = mdm.find(Task.class, participantIter.getTask().getTaskId());
        if (task != null) {
          tasks.add(task);
        }
        // Get user of participant.
        user = mdm.find(UserData.class, participantIter.getUser().getUserId());
        if (user != null) {
          userData.add(user);
        }
      }
    }
    
    // Get relations of a study.
    for (Relation relationIter : study.getOrganizationUnits()) {
      relation = mdm.find(Relation.class, relationIter.getRelationId());
      if (relation != null) {
        relations.add(relation);
        // Get organization units.
        organization = mdm.find(OrganizationUnit.class, relation.getOrganizationUnit().getOrganizationUnitId());
        if (organization != null) {
          organizations.add(organization);
          // Get manager of the organization unit.
          user = mdm.find(UserData.class, organization.getManager().getUserId());
          if (user != null) {
            userData.add(user);
          }
        }
        // Get tasks of the organization units.
        if (relation.getTask() != null) {
          task = mdm.find(Task.class, relation.getTask().getTaskId());
          if (task != null) {
            tasks.add(task);
          }
        }
      }
    }
    
    mdm.close();

    migrationObject = new MigrationObject();
    migrationObject.setDigitalObject(object);
    migrationObject.setInvestigation(investigation);
    migrationObject.setStudy(study);
    migrationObject.setUserData(userData);
    migrationObject.setMetaDataSchema(metadataSchemas);
    migrationObject.setParticipants(participants);
    migrationObject.setTasks(tasks);
    migrationObject.setRelations(relations);
    migrationObject.setOrganizationunits(organizations);

    file = new File(filename);

    jc = JAXBContext.newInstance(MigrationObject.class);
    marshaller = jc.createMarshaller();
    marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, "default");
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    marshaller.marshal(migrationObject, file);
  }
}
