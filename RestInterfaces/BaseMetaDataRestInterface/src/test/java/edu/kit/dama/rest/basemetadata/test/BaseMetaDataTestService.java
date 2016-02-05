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
import edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObject;
import edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectTransition;
import edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectType;
import edu.kit.dama.mdm.base.interfaces.IDefaultInvestigation;
import edu.kit.dama.mdm.base.interfaces.IDefaultMetaDataSchema;
import edu.kit.dama.mdm.base.interfaces.IDefaultOrganizationUnit;
import edu.kit.dama.mdm.base.interfaces.IDefaultParticipant;
import edu.kit.dama.mdm.base.interfaces.IDefaultRelation;
import edu.kit.dama.mdm.base.interfaces.IDefaultStudy;
import edu.kit.dama.mdm.base.interfaces.IDefaultTask;
import edu.kit.dama.mdm.base.interfaces.IDefaultUserData;
import edu.kit.dama.mdm.base.interfaces.ISimpleDigitalObject;
import edu.kit.dama.mdm.base.interfaces.ISimpleDigitalObjectTransition;
import edu.kit.dama.mdm.base.interfaces.ISimpleDigitalObjectType;
import edu.kit.dama.mdm.base.interfaces.ISimpleInvestigation;
import edu.kit.dama.mdm.base.interfaces.ISimpleMetaDataSchema;
import edu.kit.dama.mdm.base.interfaces.ISimpleOrganizationUnit;
import edu.kit.dama.mdm.base.interfaces.ISimpleStudy;
import edu.kit.dama.mdm.base.interfaces.ISimpleTask;
import edu.kit.dama.mdm.base.interfaces.ISimpleUserData;
import edu.kit.dama.rest.base.IEntityWrapper;
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
import edu.kit.dama.util.Constants;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

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
     * @param pSimple TRUE = Only set the ids.
     *
     * @return The organization unit.
     */
    protected static OrganizationUnit factoryOrganizationUnitEntity(long id, boolean pSimple) {
        OrganizationUnit unit = new OrganizationUnit();
        unit.setOrganizationUnitId(id);
        if (!pSimple) {
            unit.setOuName("KIT");
            unit.setAddress("Hermann-von-Helmholtz Platz 1");
            unit.setCity("Eggenstein-Leopoldshafen");
            unit.setZipCode("76344");
            unit.setCountry("Germany");
            unit.setWebsite("http://www.kit.edu");
        }
        return unit;
    }

    /**
     * Factory a new MetaDataSchema with the provided id.
     *
     * @param id The id of the MetaDataSchema.
     * @param pSimple TRUE = Only set the id.
     *
     * @return The MetaDataSchema.
     */
    protected static MetaDataSchema factoryMetadataSchemaEntity(long id, boolean pSimple) {
        MetaDataSchema schema = new MetaDataSchema();
        schema.setId(id);
        if (!pSimple) {
            schema.setSchemaIdentifier("dc_v" + id);
            schema.setSchemaIdentifier("http://purl.org/dc/elements/1.1/" + id);
        }
        return schema;
    }

    /**
     * Factory a single participant.
     *
     * @param userId The user id.
     * @param taskId The task id.
     * @param pSimple TRUE = Only set the ids.
     *
     * @return A single participant.
     */
    protected static Participant factoryParticipantEntity(long userId,
            long taskId, boolean pSimple) {
        if (!pSimple) {
            Participant p = new Participant(factoryUserDataEntity(userId, pSimple), factoryTaskEntity(
                    taskId, pSimple));
            p.setParticipantId(1l);
            return p;
        }
        Participant p = new Participant(null);
        p.setParticipantId(1l);
        return p;
    }

    /**
     * Factory a new UserData with the provided id.
     *
     * @param id The id of the UserData.
     * @param pSimple TRUE = Only set the id.
     *
     * @return The UserData.
     */
    protected static UserData factoryUserDataEntity(long id, boolean pSimple) {
        UserData user = new UserData();
        user.setUserId(id);
        if (!pSimple) {
            user.setFirstName("John");
            user.setLastName("Doe");
            user.setDistinguishedName("jd");
            user.setEmail("john.doe@kit.edu");
        }
        return user;
    }

    /**
     * Factory a new Task with the provided id.
     *
     * @param id The id of the Task.
     * @param pSimple TRUE = Only set the id.
     *
     * @return The Task.
     */
    protected static Task factoryTaskEntity(long id, boolean pSimple) {
        Task task;
        if (!pSimple) {
            task = new Task("Development");
        } else {
            task = new Task(null);
        }
        task.setTaskId(id);
        return task;
    }

    /**
     * Factory a new DigitalObjectType with the provided id.
     *
     * @param id The id of the type.
     * @param pSimple TRUE = Only set the id.
     *
     * @return The Type.
     */
    protected static DigitalObjectType factoryObjectTypeEntity(long id, boolean pSimple) {
        DigitalObjectType type = new DigitalObjectType();
        type.setId(id);
        if (!pSimple) {
            type.setTypeDomain("http://ipe.kit.edu/kitdm");
            type.setIdentifier("type#" + id);
            type.setVersion(1);
            type.setDescription("Sample type.");
        }
        return type;
    }

    /**
     * Factory a single user with the provided id.
     *
     * @param userId The user id.
     * @param pSimple TRUE = Only set the id.
     *
     * @return A single user.
     */
    protected static UserData factoryUserEntity(long userId, boolean pSimple) {
        UserData user = new UserData();
        user.setUserId(userId);
        if (!pSimple) {
            user.setFirstName("Dummy");
            user.setLastName("User");
            user.setEmail("dummy.user" + userId + "@dama.kit.edu");
            user.setDistinguishedName(new UserId(Long.toString(userId)).
                    getStringRepresentation());
        }
        return user;
    }

    /**
     * Factory a new study with the provided id.
     *
     * @param id The study id.
     * @param pSimple TRUE = Only set the id.
     *
     * @return The study.
     */
    protected static Study factoryStudyEntity(long id, boolean pSimple) {
        Study study = Study.factoryNewStudy();
        study.setStudyId(id);
        if (!pSimple) {
            study.setEndDate(null);
            study.setStartDate(new Date());
            study.setLegalNote("Insert legal notes here");
            study.setNote("Insert notes here");
            study.setTopic("Insert topic here");
        }
        OrganizationUnit unit = factoryOrganizationUnitEntity(id, true);
        unit.setManager(factoryUserEntity(1, true));
        study.setOrganizationUnits(new HashSet<>(Arrays.asList(
                factoryRelationEntity(unit.getOrganizationUnitId(), 1l, true))));
        Investigation i1 = factoryInvestigationEntity(id, true);
        Investigation i2 = factoryInvestigationEntity(id + 1, true);
        study.addInvestigation(i1);
        study.addInvestigation(i2);
        i1.setStudy(null);
        i2.setStudy(null);
        study.addRelation(factoryRelationEntity(id, id, true));

        return study;
    }

    /**
     * Factory a new investigation with the provided id.
     *
     * @param id The investigation id.
     * @param pSimple TRUE = Only set the id.
     *
     * @return The investigation.
     */
    protected static Investigation factoryInvestigationEntity(long id, boolean pSimple) {
        Investigation investigation = Investigation.factoryNewInvestigation();
        investigation.setInvestigationId(id);
        if (!pSimple) {
            investigation.setEndDate(null);
            investigation.setStartDate(new Date());
            investigation.setDescription("Insert description here");
            investigation.setNote("Insert notes here");
            investigation.setParticipants(new HashSet<>(Arrays.asList(
                    factoryParticipantEntity(1l, 1l, true))));
            investigation.setTopic("Some topic");
            DigitalObject o = factoryDigitalObjectEntity(id, true);
            investigation.addDataSet(o);
            o.setInvestigation(null);
            investigation.addParticipant(new Participant(factoryUserDataEntity(id, true),
                    factoryTaskEntity(id, pSimple)));
            investigation.addMetaDataSchema(factoryMetadataSchemaEntity(id, true));
        }
        return investigation;
    }

    /**
     * Factory a new digital object with the provided id. The actual digital
     * object id is generated using the following pattern:
     *
     * <b>abcd-efgh-ijkl-</b><i>id</i>
     *
     * @param id The last part of the digital object id.
     * @param pSimple TRUE = Only set the id.
     *
     * @return The digital object.
     */
    protected static DigitalObject factoryDigitalObjectEntity(long id, boolean pSimple) {
        DigitalObject object = DigitalObject.factoryNewDigitalObject();
        object.setBaseId(id);
        if (!pSimple) {
            object.setDigitalObjectId(new DigitalObjectId("abcd-efgh-ijkl-" + id));
            object.setStartDate(new Date());
            object.setEndDate(null);
            object.setLabel("Some label");
            object.setLabel("Some label");
            object.setNote("Some note");
            object.setUploadDate(new Date());
            UserData user = factoryUserEntity(1, true);
            object.setExperimenters(new HashSet<>(Arrays.asList(user)));
            object.setUploader(user);
            object.addExperimenter(user);
        }
        return object;
    }

    /**
     * Factory a new digital object transition where the input object has inId
     * and the output object outId.
     *
     * @param inId The base id of the input object.
     * @param outId The base id of the output object.
     * @param pSimple TRUE = Only set the id.
     *
     * @return The digital object transition.
     */
    protected static DigitalObjectTransition factoryDigitalObjectTransitionEntity(long inId, long outId, boolean pSimple) {
        DigitalObjectTransition t = new DigitalObjectTransition();
        t.setId(1l);
        if (!pSimple) {
            DigitalObject inputObject = factoryDigitalObjectEntity(inId, true);
            DigitalObject outputObject = factoryDigitalObjectEntity(outId, true);
            t.addInputMapping(inputObject, Constants.DEFAULT_VIEW);
            t.addOutputObject(outputObject);
            t.setCreationTimestamp(System.currentTimeMillis());
            t.setTransitionEntityId("none");
        }
        return t;
    }

    /**
     * Factory a relation entity.
     *
     * @param ouId The organization unit id.
     * @param taskId The task id.
     * @param pSimple TRUE = Only set the id.
     *
     * @return A single relation.
     */
    protected static Relation factoryRelationEntity(long ouId, long taskId, boolean pSimple) {
        Relation r;
        if (!pSimple) {
            r = new Relation(factoryOrganizationUnitEntity(ouId, true), factoryTaskEntity(taskId, true));
        } else {
            r = new Relation(factoryOrganizationUnitEntity(ouId, true));
        }
        r.setRelationId(1l);
        return r;
    }

    @Override
    public IEntityWrapper<? extends ISimpleStudy> getStudyIds(String groupId, Integer first, Integer results, HttpContext hc) {
        return new StudyWrapper(Arrays.asList(factoryStudyEntity(1l, true)));
    }

    @Override
    public IEntityWrapper<? extends IDefaultStudy> createStudy(String groupId, String topic, String note,
            String legalNote, Long managerUserId, Long startDate, Long endDate,
            HttpContext hc) {
        Study s = factoryStudyEntity(2l, false);
        s.setTopic(topic);
        s.setNote(note);
        s.setManager(factoryUserEntity(managerUserId, true));
        s.setStartDate(new Date(startDate));
        s.setEndDate(new Date(endDate));
        //remove default investigation
        s.setInvestigations(new HashSet<Investigation>());
        return new StudyWrapper(s);
    }

    @Override
    public IEntityWrapper<? extends ISimpleStudy> getStudyCount(String groupId, HttpContext hc) {
        return new StudyWrapper(1);
    }

    @Override
    public IEntityWrapper<? extends IDefaultStudy> getStudyById(String groupId, Long id, HttpContext hc) {
        if ((int) Math.rint(id) != 1) {
            throw new WebApplicationException(404);
        }
        return new StudyWrapper(factoryStudyEntity(id, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> addInvestigationToStudy(String groupId, Long id, String topic, String note, String description, Long startDate, Long endDate, HttpContext hc) {
        Study study = factoryStudyEntity(id, true);
        study.setInvestigations(new HashSet<Investigation>());
        Investigation investigation = factoryInvestigationEntity(2l, false);
        study.addInvestigation(investigation);
        return new InvestigationWrapper(investigation);
    }

    @Override
    public IEntityWrapper<? extends IDefaultStudy> updateStudyById(Long id, String groupId, String topic, String note, String legalNote, Long startDate, Long endDate, HttpContext hc) {
        Study study = factoryStudyEntity(id, false);
        study.setTopic(topic);
        study.setNote(note);
        study.setLegalNote(legalNote);
        study.setStartDate(new Date(startDate));
        study.setEndDate(new Date(endDate));
        return new StudyWrapper(study);
    }

    @Override
    public Response deleteStudyById(Long id, String groupId, HttpContext hc) {
        if ((int) Math.rint(id) != 1) {
            throw new WebApplicationException(404);
        }
        return Response.ok().build();
    }

    @Override
    public IEntityWrapper<? extends ISimpleInvestigation> getInvestigationIds(String groupId, Long studyId, Integer first, Integer results, HttpContext hc) {
        return new InvestigationWrapper(Arrays.asList(factoryInvestigationEntity(1l, true)));
    }

    @Override
    public IEntityWrapper<? extends ISimpleInvestigation> getInvestigationCount(String groupId, Long studyId, HttpContext hc) {
        return new InvestigationWrapper(1);
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> getInvestigationById(Long id, String groupId, HttpContext hc) {
        if ((int) Math.rint(id) != 1) {
            throw new WebApplicationException(404);
        }
        return new InvestigationWrapper(
                factoryInvestigationEntity(id, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> addDigitalObjectToInvestigation(Long id, String groupId, String label, Long uploaderId, String note, Long startDate, Long endDate, HttpContext hc) {
        Investigation investigation = factoryInvestigationEntity(id, true);
        investigation.setDataSets(new HashSet<DigitalObject>());
        DigitalObject object = factoryDigitalObjectEntity(2l, false);
        investigation.addDataSet(object);
        return new DigitalObjectWrapper(object);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> addExperimenterToDigitalObject(String groupId, Long baseId, Long userDataId, HttpContext hc) {
        DigitalObject object = factoryDigitalObjectEntity(baseId, false);
        UserData user = factoryUserDataEntity(userDataId, true);
        object.addExperimenter(user);
        return new DigitalObjectWrapper(object);
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> updateInvestigationById(Long id, String groupId, String topic, String note, String description, Long startDate, Long endDate, HttpContext hc) {
        Investigation investigation = factoryInvestigationEntity(id, false);
        investigation.setTopic(topic);
        investigation.setNote(note);
        investigation.setDescription(description);
        investigation.setStartDate(new Date(startDate));
        investigation.setEndDate(new Date(endDate));
        return new InvestigationWrapper(investigation);
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
    public IEntityWrapper<? extends ISimpleDigitalObject> getDigitalObjectIds(String groupId, Long investigationId, Integer first, Integer results, HttpContext hc) {
        return new DigitalObjectWrapper(
                Arrays.asList(factoryDigitalObjectEntity(1l, true))
        );
    }

    @Override
    public IEntityWrapper<? extends ISimpleDigitalObject> getDigitalObjectCount(String groupId, Long investigationId, HttpContext hc) {
        return new DigitalObjectWrapper(1);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectById(Long id, String groupId, HttpContext hc) {
        if ((int) Math.rint(id) != 1) {
            throw new WebApplicationException(404);
        }
        return new DigitalObjectWrapper(factoryDigitalObjectEntity(1l, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectByDOI(String digitalObjectId, String groupId, HttpContext hc) {
        DigitalObject result = factoryDigitalObjectEntity(1l, false);
        result.setDigitalObjectId(new DigitalObjectId(digitalObjectId));
        return new DigitalObjectWrapper(result);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> updateDigitalObjectById(Long id, String groupId, String label, String note, Long startDate, Long endDate, HttpContext hc) {
        DigitalObject d = factoryDigitalObjectEntity(id, false);
        d.setLabel(label);
        d.setNote(note);
        d.setStartDate(new Date(startDate));
        d.setEndDate(new Date(endDate));
        return new DigitalObjectWrapper(d);
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
    public IEntityWrapper<? extends ISimpleOrganizationUnit> getOrganizationUnitIds(String groupId, Integer first, Integer results, HttpContext hc) {
        return new OrganizationUnitWrapper(
                factoryOrganizationUnitEntity(1l, true), factoryOrganizationUnitEntity(2l, true));
    }

    @Override
    public IEntityWrapper<? extends IDefaultOrganizationUnit> createOrganizationUnit(String groupId, String ouName, Long managerUserId, String address, String zipCode, String city, String country, String website, HttpContext hc) {
        OrganizationUnit unit = factoryOrganizationUnitEntity(1l, false);
        unit.setManager(factoryUserDataEntity(managerUserId, true));
        unit.setOuName(ouName);
        unit.setAddress(address);
        unit.setCity(city);
        unit.setZipCode(zipCode);
        unit.setCountry(country);
        unit.setWebsite(website);
        return new OrganizationUnitWrapper(unit);
    }

    @Override
    public IEntityWrapper<? extends ISimpleOrganizationUnit> getOrganizationUnitCount(String groupId, HttpContext hc) {
        return new OrganizationUnitWrapper(2);
    }

    @Override
    public IEntityWrapper<? extends IDefaultOrganizationUnit> getOrganizationUnitById(String groupId, Long id, HttpContext hc) {
        return new OrganizationUnitWrapper(
                factoryOrganizationUnitEntity(id, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultOrganizationUnit> updateOrganizationUnit(String groupId, Long id, String ouName, String address, String zipCode, String city, String country, String website, HttpContext hc) {
        OrganizationUnit unit = factoryOrganizationUnitEntity(id, false);
        unit.setOuName(ouName);
        unit.setAddress(address);
        unit.setCity(city);
        unit.setZipCode(zipCode);
        unit.setCountry(country);
        unit.setWebsite(website);
        return new OrganizationUnitWrapper(unit);
    }

    @Override
    public IEntityWrapper<? extends ISimpleMetaDataSchema> getMetadataSchemaIds(String groupId, Integer first, Integer results, HttpContext hc) {
        return new MetadataSchemaWrapper(
                factoryMetadataSchemaEntity(1l, true), factoryMetadataSchemaEntity(2l, true));
    }

    @Override
    public IEntityWrapper<? extends IDefaultMetaDataSchema> createMetadataSchema(String groupId, String identifier, String schemaUrl, HttpContext hc) {
        MetaDataSchema schema = new MetaDataSchema(identifier, schemaUrl);
        return new MetadataSchemaWrapper(schema);
    }

    @Override
    public IEntityWrapper<? extends ISimpleMetaDataSchema> getMetadataSchemaCount(String groupId, HttpContext hc) {
        return new MetadataSchemaWrapper(2);
    }

    @Override
    public IEntityWrapper<? extends IDefaultMetaDataSchema> getMetadataSchemaById(String groupId, Long id, HttpContext hc) {
        return new MetadataSchemaWrapper(factoryMetadataSchemaEntity(id, false));
    }

    @Override
    public IEntityWrapper<? extends ISimpleTask> getTaskIds(String groupId, Integer first, Integer results, HttpContext hc) {
        return new TaskWrapper(factoryTaskEntity(1l, true), factoryTaskEntity(2l, true));
    }

    @Override
    public IEntityWrapper<? extends IDefaultTask> createTask(String groupId, String taskName, HttpContext hc) {
        Task t = factoryTaskEntity(1l, false);
        t.setTask(taskName);
        return new TaskWrapper(t);
    }

    @Override
    public IEntityWrapper<? extends ISimpleTask> getTaskCount(String groupId, HttpContext hc) {
        return new TaskWrapper(2);
    }

    @Override
    public IEntityWrapper<? extends IDefaultTask> getTaskById(String groupId, Long id, HttpContext hc) {
        return new TaskWrapper(factoryTaskEntity(id, false));
    }

    @Override
    public IEntityWrapper<? extends ISimpleUserData> getUserDataIds(String groupId, Integer first, Integer results, HttpContext hc) {
        return new UserDataWrapper(
                factoryUserDataEntity(1l, true), factoryUserDataEntity(2l, true));
    }

    @Override
    public IEntityWrapper<? extends ISimpleUserData> getUserDataCount(String groupId, HttpContext hc) {
        return new UserDataWrapper(2);
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> getUserDataById(String groupId, Long id, HttpContext hc) {
        return new UserDataWrapper(
                factoryUserDataEntity(id, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultParticipant> getParticipantById(String groupId, Long id, HttpContext hc) {
        return new ParticipantWrapper(factoryParticipantEntity(id, id, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultRelation> getRelationById(String groupId, Long id, HttpContext hc) {
        return new RelationWrapper(factoryRelationEntity(id, id, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultStudy> addRelationToStudy(String groupId, Long id, Long organizationUnitId, Long taskId, HttpContext hc) {
        Study s = factoryStudyEntity(id, false);
        return new StudyWrapper(s);
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> addParticipantToInvestigation(String groupId, Long id, Long userDataId, Long taskId, HttpContext hc) {
        Investigation i = factoryInvestigationEntity(id, false);
        i.setParticipants(new HashSet<Participant>());
        i.addParticipant(factoryParticipantEntity(1l, 1l, true));
        return new InvestigationWrapper(i);
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> addMetadataSchemaToInvestigation(String groupId, Long id, Long metadataSchemaId, HttpContext hc) {
        Investigation i = factoryInvestigationEntity(id, false);
        i.setMetaDataSchema(new HashSet<MetaDataSchema>());
        i.addMetaDataSchema(factoryMetadataSchemaEntity(1l, true));
        return new InvestigationWrapper(i);
    }

    @Override
    public Response checkService() {
        return Response.status(200).entity(new CheckServiceResponse("BaseMetadataTest", ServiceStatus.OK)).build();
    }

    @Override
    public IEntityWrapper<? extends ISimpleDigitalObjectType> getDigitalObjectTypeIds(String groupId, Integer first, Integer results, HttpContext hc) {
        return new DigitalObjectTypeWrapper(factoryObjectTypeEntity(1l, true));
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectType> getDigitalObjectTypeById(String groupId, Long id, HttpContext hc) {
        return new DigitalObjectTypeWrapper(factoryObjectTypeEntity(id, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectType> addDigitalObjectType(String groupId, String typeDomain, String identifier, int version, String description, HttpContext hc) {
        DigitalObjectType type = factoryObjectTypeEntity(1l, false);
        type.setTypeDomain(typeDomain);
        type.setIdentifier(identifier);
        type.setVersion(version);
        type.setDescription(description);
        return new DigitalObjectTypeWrapper(type);
    }

    @Override
    public IEntityWrapper<? extends ISimpleDigitalObjectType> getDigitalObjectTypeCount(String groupId, HttpContext hc) {
        return new DigitalObjectTypeWrapper(1);
    }

    @Override
    public IEntityWrapper<? extends ISimpleDigitalObject> getDigitalObjectsForDigitalObjectType(String groupId, Long id, Integer first, Integer results, HttpContext hc) {
        return new DigitalObjectWrapper(factoryDigitalObjectEntity(1l, true));
    }

    @Override
    public IEntityWrapper<? extends ISimpleDigitalObject> getDigitalObjectCountForType(String groupId, Long id, HttpContext hc) {
        return new DigitalObjectWrapper(1);
    }

    @Override
    public IEntityWrapper<? extends ISimpleDigitalObjectType> getDigitalObjectTypesForDigitalObject(String groupId, Long id, HttpContext hc) {
        if (new Long(1l).equals(id)) {
            return new DigitalObjectTypeWrapper(factoryObjectTypeEntity(1l, true));
        }
        return new DigitalObjectTypeWrapper(0);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectType> addDigitalObjectTypeToDigitalObject(String groupId, Long id, Long objectTypeId, HttpContext hc) {
        return new DigitalObjectTypeWrapper(factoryObjectTypeEntity(id, false));
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectTransition> getDigitalObjectTransitionById(String groupId, Long id, HttpContext hc) {
        return new DigitalObjectTransitionWrapper(factoryDigitalObjectTransitionEntity(1l, 2l, false));
    }

    @Override
    public IEntityWrapper<? extends ISimpleDigitalObjectTransition> getDigitalObjectDerivationInformation(String groupId, Long id, HttpContext hc) {
        if (id == 2l) {
            //object with id 2 is derived from object with id 1
            return new DigitalObjectTransitionWrapper(factoryDigitalObjectTransitionEntity(1l, 2l, true));
        }

        //other ids are not derived from any other object
        return new DigitalObjectTransitionWrapper(0);
    }

    @Override
    public IEntityWrapper<? extends ISimpleDigitalObjectTransition> getDigitalObjectContributionInformation(String groupId, Long id, HttpContext hc) {
        if (id == 1l) {
            //object with id 1  contributes to object with id 1
            return new DigitalObjectTransitionWrapper(factoryDigitalObjectTransitionEntity(1l, 2l, true));
        }

        //other ids are not contributing to any other object
        return new DigitalObjectTransitionWrapper(0);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectTransition> addTransitionToDigitalObject(String groupId, Long id, Long otherId, String viewName, Long outputId, HttpContext hc) {
        if (id == 1l && outputId == 2l) {
            return new DigitalObjectTransitionWrapper(factoryDigitalObjectTransitionEntity(id, outputId, false));
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
