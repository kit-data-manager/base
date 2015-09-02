/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.rest.basemetadata.test;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import edu.kit.dama.mdm.base.DigitalObjectType;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Relation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import edu.kit.dama.rest.basemetadata.services.interfaces.IBaseMetaDataService;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectTransitionWrapper;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectTypeWrapper;
import edu.kit.dama.rest.basemetadata.types.InvestigationWrapper;
import edu.kit.dama.rest.basemetadata.types.MetadataSchemaWrapper;
import edu.kit.dama.rest.basemetadata.types.OrganizationUnitWrapper;
import edu.kit.dama.rest.basemetadata.types.ParticipantWrapper;
import edu.kit.dama.rest.basemetadata.types.RelationWrapper;
import edu.kit.dama.rest.basemetadata.types.StudyWrapper;
import edu.kit.dama.rest.basemetadata.types.TaskWrapper;
import edu.kit.dama.rest.basemetadata.types.UserDataWrapper;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.util.Constants;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author mf6319
 */
@Path("/BaseMetaDataTest")
public class BaseMetaDataTestService implements IBaseMetaDataService {

  /**
   *
   *
   * BaseMetaData Service Implementation
   *
   *
   *
   */
  /**
   * Factory a new organization unit with the provided id.
   *
   * @param id The id of the organization unit.
   *
   * @return The organization unit.
   */
  protected static OrganizationUnit factoryOrganizationUnitEntity(long id) {
    OrganizationUnit unit = new OrganizationUnit();
    unit.setOrganizationUnitId(id);
    unit.setOuName("KIT");
    unit.setAddress("Hermann-von-Helmholtz Platz 1");
    unit.setCity("Eggenstein-Leopoldshafen");
    unit.setZipCode("76344");
    unit.setCountry("Germany");
    unit.setWebsite("http://www.kit.edu");
    return unit;
  }

  /**
   * Factory a new MetaDataSchema with the provided id.
   *
   * @param id The id of the MetaDataSchema.
   *
   * @return The MetaDataSchema.
   */
  protected static MetaDataSchema factoryMetadataSchemaEntity(long id) {
    MetaDataSchema schema = new MetaDataSchema();
    schema.setId(id);
    schema.setSchemaIdentifier("dc_v" + id);
    schema.setSchemaIdentifier("http://purl.org/dc/elements/1.1/" + id);
    return schema;
  }

  /**
   * Factory a single participant.
   *
   * @param userId The user id.
   * @param taskId The task id.
   *
   * @return A single participant.
   */
  protected static Participant factoryParticipantEntity(long userId,
          long taskId) {
    return new Participant(factoryUserDataEntity(userId), factoryTaskEntity(
            taskId));
  }

  /**
   * Factory a new UserData with the provided id.
   *
   * @param id The id of the UserData.
   *
   * @return The UserData.
   */
  protected static UserData factoryUserDataEntity(long id) {
    UserData user = new UserData();
    user.setUserId(id);
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setDistinguishedName("jd");
    user.setEmail("john.doe@kit.edu");
    return user;
  }

  /**
   * Factory a new Task with the provided id.
   *
   * @param id The id of the Task.
   *
   * @return The Task.
   */
  protected static Task factoryTaskEntity(long id) {
    Task task = new Task("Development");
    task.setTaskId(id);
    return task;
  }

  /**
   * Factory a new DigitalObjectType with the provided id.
   *
   * @param id The id of the type.
   *
   * @return The Type.
   */
  protected static DigitalObjectType factoryObjectTypeEntity(long id) {
    DigitalObjectType type = new DigitalObjectType();
    type.setId(id);
    type.setTypeDomain("http://ipe.kit.edu/kitdm");
    type.setIdentifier("type#" + id);
    type.setVersion(1);
    type.setDescription("Sample type.");
    return type;
  }

  /**
   * Factory a single user with the provided id.
   *
   * @param userId The user id.
   *
   * @return A single user.
   */
  protected static UserData factoryUserEntity(long userId) {
    UserData user = new UserData();
    user.setUserId(userId);
    user.setFirstName("Dummy");
    user.setLastName("User");
    user.setEmail("dummy.user" + userId + "@dama.kit.edu");
    user.setDistinguishedName(new UserId(Long.toString(userId)).
            getStringRepresentation());
    return user;
  }

  /**
   * Factory a new study with the provided id.
   *
   * @param id The study id.
   *
   * @return The study.
   */
  protected static Study factoryStudyEntity(long id) {
    Study study = Study.factoryNewStudy();
    study.setStudyId(id);
    study.setEndDate(null);
    study.setStartDate(new Date());
    study.setLegalNote("Insert legal notes here");
    study.setNote("Insert notes here");
    study.setTopic("Insert topic here");
    OrganizationUnit unit = factoryOrganizationUnitEntity(id);
    unit.setManager(factoryUserEntity(1));
    study.setOrganizationUnits(new HashSet<Relation>(Arrays.asList(
            new Relation(unit))));
    study.addInvestigation(factoryInvestigationEntity(id));
    study.addInvestigation(factoryInvestigationEntity(id + 1));
    study.addRelation(new Relation(unit, factoryTaskEntity(id)));

    return study;
  }

  /**
   * Factory a new investigation with the provided id.
   *
   * @param id The investigation id.
   *
   * @return The investigation.
   */
  protected static Investigation factoryInvestigationEntity(long id) {
    Investigation investigation = Investigation.factoryNewInvestigation();
    investigation.setInvestigationId(id);
    investigation.setEndDate(null);
    investigation.setStartDate(new Date());
    investigation.setDescription("Insert description here");
    investigation.setNote("Insert notes here");
    investigation.setParticipants(new HashSet<>(Arrays.asList(
            new Participant(factoryUserEntity(1)))));
    investigation.setTopic("Some topic");
    investigation.addDataSet(factoryDigitalObjectEntity(id));
    investigation.addParticipant(new Participant(factoryUserDataEntity(id),
            factoryTaskEntity(id)));
    investigation.addMetaDataSchema(factoryMetadataSchemaEntity(id));
    return investigation;
  }

  /**
   * Factory a new digital object with the provided id. The actual digital
   * object id is generated using the following pattern:
   *
   * <b>abcd-efgh-ijkl-</b><i>id</i>
   *
   * @param id The last part of the digital object id.
   *
   * @return The digital object.
   */
  protected static DigitalObject factoryDigitalObjectEntity(long id) {
    DigitalObject object = DigitalObject.factoryNewDigitalObject();
    object.setBaseId(id);
    object.setDigitalObjectId(new DigitalObjectId("abcd-efgh-ijkl-" + id));
    object.setStartDate(new Date());
    object.setEndDate(null);
    object.setLabel("Some label");
    object.setLabel("Some label");
    object.setNote("Some note");
    object.setUploadDate(new Date());
    object.setExperimenters(new HashSet<>(Arrays.asList(
            factoryUserEntity(1))));
    object.setUploader(factoryUserEntity(1));
    object.addExperimenter(factoryUserDataEntity(id));
    return object;
  }

  /**
   * Factory a new digital object transition where the input object has inId and
   * the output object outId.
   *
   * @param inId The base id of the input object.
   * @param outId The base id of the output object.
   *
   * @return The digital object transition.
   */
  protected static DigitalObjectTransition factoryDigitalObjectTransitionEntity(long inId, long outId) {
    DigitalObject inputObject = factoryDigitalObjectEntity(inId);
    DigitalObject outputObject = factoryDigitalObjectEntity(outId);
    DigitalObjectTransition t = new DigitalObjectTransition();
    t.setId(1l);
    t.addInputMapping(inputObject, Constants.DEFAULT_VIEW);
    t.addOutputObject(outputObject);
    t.setCreationTimestamp(System.currentTimeMillis());
    t.setTransitionEntityId("none");
    return t;
  }

  /**
   * Factory a relation entity.
   *
   * @param ouId The organization unit id.
   * @param taskId The task id.
   *
   * @return A single relation.
   */
  protected static Relation factoryRelationEntity(long ouId, long taskId) {
    Relation r = new Relation(factoryOrganizationUnitEntity(ouId),
            factoryTaskEntity(taskId));
    r.setRelationId(1l);
    return r;
  }

  @Override
  public StreamingOutput getStudyIds(String groupId, Integer first,
          Integer results, HttpContext hc) {
    return createObjectGraphStream(StudyWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new StudyWrapper(Arrays.
                    asList(factoryStudyEntity(1l))));
  }

  @Override
  public StreamingOutput createStudy(String groupId, String topic, String note,
          String legalNote, Long managerUserId, Long startDate, Long endDate,
          HttpContext hc) {
    Study s = factoryStudyEntity(2l);
    s.setTopic(topic);
    s.setNote(note);
    s.setManager(factoryUserEntity(managerUserId));
    s.setStartDate(new Date(startDate));
    s.setEndDate(new Date(endDate));
    //remove default investigation
    s.setInvestigations(new HashSet<Investigation>());
    return createObjectGraphStream(StudyWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(s));
  }

  @Override
  public StreamingOutput getStudyCount(String groupId, HttpContext hc) {
    return createObjectGraphStream(StudyWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(1));
  }

  @Override
  public StreamingOutput getStudyById(String groupId, Long id, HttpContext hc) {
    if ((int) Math.rint(id) != 1) {
      throw new WebApplicationException(404);
    }
    return createObjectGraphStream(StudyWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(
                    factoryStudyEntity(id)));
  }

  @Override
  public StreamingOutput addInvestigationToStudy(String groupId,
          Long id,
          String topic,
          String note,
          String description,
          Long startDate,
          Long endDate,
          HttpContext hc) {
    Study study = factoryStudyEntity(id);
    study.setInvestigations(new HashSet<Investigation>());
    Investigation investigation = factoryInvestigationEntity(2l);
    study.addInvestigation(investigation);
    return createObjectGraphStream(InvestigationWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(
                    investigation));
  }

  @Override
  public StreamingOutput updateStudyById(Long id, String groupId, String topic,
          String note, String legalNote, Long startDate, Long endDate,
          HttpContext hc) {
    Study study = factoryStudyEntity(id);
    study.setTopic(topic);
    study.setNote(note);
    study.setLegalNote(legalNote);
    study.setStartDate(new Date(startDate));
    study.setEndDate(new Date(endDate));
    return createObjectGraphStream(StudyWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(study));
  }

  @Override
  public Response deleteStudyById(Long id, String groupId, HttpContext hc) {
    if ((int) Math.rint(id) != 1) {
      throw new WebApplicationException(404);
    }
    return Response.ok().build();
  }

  @Override
  public StreamingOutput getInvestigationIds(String groupId, Long studyId,
          Integer first, Integer results, HttpContext hc) {
    return createObjectGraphStream(InvestigationWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new InvestigationWrapper(
                    Arrays.asList(factoryInvestigationEntity(1l))));
  }

  @Override
  public StreamingOutput getInvestigationCount(String groupId, Long studyId,
          HttpContext hc) {
    return createObjectGraphStream(InvestigationWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(1));
  }

  @Override
  public StreamingOutput getInvestigationById(Long id, String groupId,
          HttpContext hc) {
    if ((int) Math.rint(id) != 1) {
      throw new WebApplicationException(404);
    }
    return createObjectGraphStream(InvestigationWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(
                    factoryInvestigationEntity(id)));
  }

  @Override
  public StreamingOutput addDigitalObjectToInvestigation(Long id,
          String groupId,
          String label,
          Long uploaderId,
          String note,
          Long startDate,
          Long endDate,
          HttpContext hc) {

    Investigation investigation = factoryInvestigationEntity(id);
    investigation.setDataSets(new HashSet<DigitalObject>());
    DigitalObject object = factoryDigitalObjectEntity(2l);
    investigation.addDataSet(object);
    return createObjectGraphStream(DigitalObjectWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(
                    object));
  }

  @Override
  public StreamingOutput addExperimenterToDigitalObject(String groupId,
          Long objectId, Long userDataId, HttpContext hc) {
    DigitalObject object = factoryDigitalObjectEntity(objectId);
    UserData user = factoryUserDataEntity(userDataId);
    object.addExperimenter(user);
    return createObjectGraphStream(DigitalObjectWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(
                    object));
  }

  @Override
  public StreamingOutput updateInvestigationById(Long id, String groupId,
          String topic, String note, String description, Long startDate,
          Long endDate, HttpContext hc) {
    Investigation investigation = factoryInvestigationEntity(id);
    investigation.setTopic(topic);
    investigation.setNote(note);
    investigation.setDescription(description);
    investigation.setStartDate(new Date(startDate));
    investigation.setEndDate(new Date(endDate));
    return createObjectGraphStream(InvestigationWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(
                    investigation));
  }

  @Override
  public Response deleteInvestigationById(Long id, String groupId,
          HttpContext hc) {
    if ((int) Math.rint(id) != 1) {
      throw new WebApplicationException(404);
    }
    return Response.ok().build();
  }

  @Override
  public StreamingOutput getDigitalObjectIds(String groupId,
          Long investigationId, Integer first, Integer results, HttpContext hc) {
    return createObjectGraphStream(DigitalObjectWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectWrapper(
                    Arrays.asList(factoryDigitalObjectEntity(1l))));
  }

  @Override
  public StreamingOutput getDigitalObjectCount(String groupId,
          Long investigationId, HttpContext hc) {
    return createObjectGraphStream(DigitalObjectWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(1));
  }

  @Override
  public StreamingOutput getDigitalObjectById(Long id, String groupId,
          HttpContext hc) {
    if ((int) Math.rint(id) != 1) {
      throw new WebApplicationException(404);
    }
    return createObjectGraphStream(DigitalObjectWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(
                    factoryDigitalObjectEntity(1l)));
  }

  @Override
  public StreamingOutput getDigitalObjectByDOI(String doi, String groupId,
          HttpContext hc) {
    DigitalObject result = factoryDigitalObjectEntity(1l);
    result.setDigitalObjectId(new DigitalObjectId(doi));
    return createObjectGraphStream(DigitalObjectWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(result));
  }

  @Override
  public StreamingOutput updateDigitalObjectById(Long id, String groupId,
          String label, String note, Long startDate, Long endDate,
          HttpContext hc) {
    DigitalObject d = factoryDigitalObjectEntity(id);
    d.setLabel(label);
    d.setNote(note);
    d.setStartDate(new Date(startDate));
    d.setEndDate(new Date(endDate));
    return createObjectGraphStream(DigitalObjectWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(d));
  }

  @Override
  public Response deleteDigitalObjectById(Long id, String groupId,
          HttpContext hc) {
    if ((int) Math.rint(id) != 1) {
      throw new WebApplicationException(404);
    }
    return Response.ok().build();
  }

  @Override
  public StreamingOutput getOrganizationUnitIds(String groupId, Integer first,
          Integer results, HttpContext hc) {
    return createObjectGraphStream(OrganizationUnitWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new OrganizationUnitWrapper(
                    factoryOrganizationUnitEntity(1l), factoryOrganizationUnitEntity(2l)));
  }

  @Override
  public StreamingOutput createOrganizationUnit(String groupId, String ouName, Long managerUserId,
          String address, String zipCode, String city, String country,
          String website, HttpContext hc) {
    OrganizationUnit unit = factoryOrganizationUnitEntity(1l);
    unit.setManager(factoryUserDataEntity(managerUserId));
    unit.setOuName(ouName);
    unit.setAddress(address);
    unit.setCity(city);
    unit.setZipCode(zipCode);
    unit.setCountry(country);
    unit.setWebsite(website);
    return createObjectGraphStream(OrganizationUnitWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new OrganizationUnitWrapper(unit));
  }

  @Override
  public StreamingOutput getOrganizationUnitCount(String groupId,
          HttpContext hc) {
    return createObjectGraphStream(OrganizationUnitWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new OrganizationUnitWrapper(2));

  }

  @Override
  public StreamingOutput getOrganizationUnitById(String groupId, Long id,
          HttpContext hc) {
    return createObjectGraphStream(OrganizationUnitWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new OrganizationUnitWrapper(
                    factoryOrganizationUnitEntity(id)));
  }

  @Override
  public StreamingOutput updateOrganizationUnit(String groupId, Long id,
          String ouName, String address, String zipCode, String city, String country,
          String website, HttpContext hc) {
    OrganizationUnit unit = factoryOrganizationUnitEntity(id);
    unit.setOuName(ouName);
    unit.setAddress(address);
    unit.setCity(city);
    unit.setZipCode(zipCode);
    unit.setCountry(country);
    unit.setWebsite(website);
    return createObjectGraphStream(OrganizationUnitWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new OrganizationUnitWrapper(unit));
  }

  @Override
  public StreamingOutput getMetadataSchemaIds(String groupId, Integer first,
          Integer results, HttpContext hc) {
    return createObjectGraphStream(MetadataSchemaWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new MetadataSchemaWrapper(
                    factoryMetadataSchemaEntity(1l), factoryMetadataSchemaEntity(2l)));
  }

  @Override
  public StreamingOutput createMetadataSchema(String groupId,
          String identifier, String schemaUrl, HttpContext hc) {
    MetaDataSchema schema = new MetaDataSchema(identifier, schemaUrl);
    return createObjectGraphStream(MetadataSchemaWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new MetadataSchemaWrapper(schema));
  }

  @Override
  public StreamingOutput getMetadataSchemaCount(String groupId, HttpContext hc) {
    return createObjectGraphStream(MetadataSchemaWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new MetadataSchemaWrapper(2));
  }

  @Override
  public StreamingOutput getMetadataSchemaById(String groupId, Long id,
          HttpContext hc) {
    return createObjectGraphStream(MetadataSchemaWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new MetadataSchemaWrapper(
                    factoryMetadataSchemaEntity(id)));
  }

  @Override
  public StreamingOutput getTaskIds(String groupId, Integer first,
          Integer results, HttpContext hc) {
    return createObjectGraphStream(TaskWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new TaskWrapper(factoryTaskEntity(
                            1l), factoryTaskEntity(2l)));
  }

  @Override
  public StreamingOutput createTask(String groupId, String taskName,
          HttpContext hc) {
    Task t = factoryTaskEntity(1l);
    t.setTask(taskName);
    return createObjectGraphStream(TaskWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new TaskWrapper(t));
  }

  @Override
  public StreamingOutput getTaskCount(String groupId, HttpContext hc) {
    return createObjectGraphStream(TaskWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new TaskWrapper(2));
  }

  @Override
  public StreamingOutput getTaskById(String groupId, Long id, HttpContext hc) {
    return createObjectGraphStream(TaskWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new TaskWrapper(
                    factoryTaskEntity(id)));
  }

  @Override
  public StreamingOutput getUserDataIds(String groupId, Integer first,
          Integer results, HttpContext hc) {
    return createObjectGraphStream(UserDataWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(
                    factoryUserDataEntity(1l), factoryUserDataEntity(2l)));
  }

  @Override
  public StreamingOutput getUserDataCount(String groupId, HttpContext hc) {
    return createObjectGraphStream(UserDataWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(2));
  }

  @Override
  public StreamingOutput getUserDataById(String groupId, Long id,
          HttpContext hc) {
    return createObjectGraphStream(UserDataWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new UserDataWrapper(
                    factoryUserDataEntity(id)));
  }

  @Override
  public StreamingOutput getParticipantById(String groupId, Long id,
          HttpContext hc) {
    return createObjectGraphStream(ParticipantWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new ParticipantWrapper(
                    factoryParticipantEntity(id, id)));
  }

  @Override
  public StreamingOutput getRelationById(String groupId, Long id,
          HttpContext hc) {
    return createObjectGraphStream(RelationWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new RelationWrapper(
                    factoryRelationEntity(id, id)));
  }

  @Override
  public StreamingOutput addRelationToStudy(String groupId, Long id,
          Long organizationUnitId, Long taskId, HttpContext hc) {
    Study s = factoryStudyEntity(id);
    s.addRelation(factoryRelationEntity(organizationUnitId, taskId));
    return createObjectGraphStream(StudyWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(s));
  }

  @Override
  public StreamingOutput addParticipantToInvestigation(String groupId, Long id,
          Long userDataId, Long taskId, HttpContext hc) {
    Investigation i = factoryInvestigationEntity(id);
    i.addParticipant(factoryParticipantEntity(userDataId, taskId));

    return createObjectGraphStream(InvestigationWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(i));
  }

  @Override
  public StreamingOutput addMetadataSchemaToInvestigation(String groupId,
          Long id, Long metadataSchemaId, HttpContext hc) {
    Investigation i = factoryInvestigationEntity(id);
    i.addMetaDataSchema(factoryMetadataSchemaEntity(metadataSchemaId));
    return createObjectGraphStream(InvestigationWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(i));
  }

  @Override
  public Response checkService() {
    return Response.status(200).entity(new CheckServiceResponse("BaseMetadataTest", ServiceStatus.OK)).build();
  }

  @Override
  public StreamingOutput getDigitalObjectTypeIds(String groupId, Integer first, Integer results, HttpContext hc) {
    return createObjectGraphStream(DigitalObjectTypeWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTypeWrapper(factoryObjectTypeEntity(1l)));
  }

  @Override
  public StreamingOutput getDigitalObjectTypeById(String groupId, Long id, HttpContext hc) {
    return createObjectGraphStream(DigitalObjectTypeWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTypeWrapper(factoryObjectTypeEntity(id)));
  }

  @Override
  public StreamingOutput addDigitalObjectType(String groupId, String typeDomain, String identifier, int version, String description, HttpContext hc) {
    DigitalObjectType type = factoryObjectTypeEntity(1l);
    type.setTypeDomain(typeDomain);
    type.setIdentifier(identifier);
    type.setVersion(version);
    type.setDescription(description);
    return createObjectGraphStream(DigitalObjectTypeWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTypeWrapper(type));
  }

  @Override
  public StreamingOutput getDigitalObjectTypeCount(String groupId, HttpContext hc) {
    return createObjectGraphStream(DigitalObjectTypeWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTypeWrapper(1));
  }

  @Override
  public StreamingOutput getDigitalObjectsForDigitalObjectType(String groupId, Long id, Integer first, Integer results, HttpContext hc) {
    return createObjectGraphStream(DigitalObjectWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(factoryDigitalObjectEntity(1l)));
  }

  @Override
  public StreamingOutput getDigitalObjectCountForType(String groupId, Long id, HttpContext hc) {
    return createObjectGraphStream(DigitalObjectWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(1));
  }

  @Override
  public StreamingOutput getDigitalObjectTypesForDigitalObject(String groupId, Long id, HttpContext hc) {
    if (new Long(1l).equals(id)) {
      return createObjectGraphStream(DigitalObjectTypeWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTypeWrapper(factoryObjectTypeEntity(1l)));
    }
    return createObjectGraphStream(DigitalObjectTypeWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTypeWrapper(0));
  }

  @Override
  public StreamingOutput addDigitalObjectTypeToDigitalObject(String groupId, Long id, Long objectTypeId, HttpContext hc) {
    return createObjectGraphStream(DigitalObjectTypeWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTypeWrapper(factoryObjectTypeEntity(id)));
  }

  @Override
  public StreamingOutput getDigitalObjectTransitionById(String groupId, Long id, HttpContext hc) {
    return createObjectGraphStream(DigitalObjectTransitionWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(factoryDigitalObjectTransitionEntity(1l, 2l)));
  }

  @Override
  public StreamingOutput getDigitalObjectDerivationInformation(String groupId, Long id, HttpContext hc) {
    if (id == 2l) {
      //object with id 2 is derived from object with id 1
      return createObjectGraphStream(DigitalObjectTransitionWrapper.class,
              Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(factoryDigitalObjectTransitionEntity(1l, 2l)));
    }

    //other ids are not derived from any other object
    return createObjectGraphStream(DigitalObjectTransitionWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(0));

  }

  @Override
  public StreamingOutput getDigitalObjectContributionInformation(String groupId, Long id, HttpContext hc) {
    if (id == 1l) {
      //object with id 1  contributes to object with id 1
      return createObjectGraphStream(DigitalObjectTransitionWrapper.class,
              Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(factoryDigitalObjectTransitionEntity(1l, 2l)));
    }

    //other ids are not contributing to any other object
    return createObjectGraphStream(DigitalObjectTransitionWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(0));
  }

  @Override
  public StreamingOutput addTransitionToDigitalObject(String groupId, Long id, Long otherId, String viewName, Long outputId, HttpContext hc) {
    if (id == 1l && outputId == 2l) {
      return createObjectGraphStream(DigitalObjectTransitionWrapper.class,
              Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(factoryDigitalObjectTransitionEntity(id, outputId)));
    } else if (id != 1l) {
      //invalid object
      throw new WebApplicationException(404);
    } else if (!Objects.equals(outputId, otherId)) {
      //output must be other object
      throw new WebApplicationException(400);
    }
    //invalid call 
    throw new WebApplicationException(404);
  }
}
