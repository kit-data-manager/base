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
package edu.kit.dama.rest.basemetadata.services.impl;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import edu.kit.dama.mdm.base.DigitalObjectType;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.ObjectTypeMapping;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Relation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.TransitionType;
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
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.exception.PersistFailedException;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.mdm.tools.AbstractTransitionTypeHandler;
import edu.kit.dama.mdm.tools.DigitalObjectSecureQueryHelper;
import edu.kit.dama.mdm.tools.DigitalObjectTypeQueryHelper;
import edu.kit.dama.mdm.tools.InvestigationSecureQueryHelper;
import edu.kit.dama.mdm.tools.StudySecureQueryHelper;
import edu.kit.dama.mdm.tools.TransitionQueryHelper;
import edu.kit.dama.mdm.tools.TransitionTypeHandlerFactory;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
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
import edu.kit.dama.rest.util.RestUtils;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
@Path("/")
public final class BaseMetaDataRestService implements IBaseMetaDataService {

    private final static Logger LOGGER = LoggerFactory.getLogger(BaseMetaDataRestService.class);

    @Override
    public IEntityWrapper<? extends IDefaultStudy> getStudies(String groupId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            return new StudyWrapper(new StudySecureQueryHelper().getReadableStudies(mdm, first, results, ctx));
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultStudy> createStudy(String groupId,
            String topic,
            String note,
            String legalNote,
            Long managerUserId,
            Long startDate,
            Long endDate,
            HttpContext hc) {
        Study study = Study.factoryNewStudy();
        study.setTopic(topic);
        study.setNote(note);
        study.setLegalNote(legalNote);
        if (startDate != null && startDate != -1) {
            study.setStartDate(new Date(startDate));
        }
        if (endDate != null && endDate != -1) {
            study.setEndDate(new Date(endDate));
        }

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        LOGGER.debug("Try creating a new study.");
        try {
            UserData managerUser;
            if (managerUserId == null || -1 == managerUserId) {
                LOGGER.debug("No manager user id specified or id is -1. Getting user id from caller id {}.", ctx.getUserId());
                managerUser = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.distinguishedName LIKE '" + ctx.getUserId().getStringRepresentation() + "'", UserData.class);
            } else {
                LOGGER.debug("Getting manager user for id {}", managerUserId);
                managerUser = mdm.find(UserData.class, managerUserId);
            }
            if (managerUser == null) {
                LOGGER.error("No manager user found for id " + managerUserId + " and caller " + ctx.getUserId());
                throw new WebApplicationException(new Exception("User for id " + managerUserId + " not found"), Response.Status.NOT_FOUND);
            }
            study.setManager(managerUser);
            study = mdm.save(study);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Study.default");
            return new StudyWrapper(mdm.find(Study.class, study.getStudyId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to create a new study.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultStudy> getStudyCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to get study count.");
            return new StudyWrapper(new StudySecureQueryHelper().getStudyCount(mdm, ctx).intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get study count.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultStudy> getStudyById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to get study with id {}", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Study.default");
            Study study = mdm.find(Study.class, id);
            if (study == null) {
                throw new WebApplicationException(new Exception("Study for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            return new StudyWrapper(study);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get study.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> addInvestigationToStudy(String groupId, Long id, String topic, String note, String description, Long startDate, Long endDate, HttpContext hc) {
        LOGGER.debug("Try to add investigation to study with id {}", id);
        LOGGER.debug(" - Creating investigation");
        Investigation investigation = Investigation.factoryNewInvestigation();
        investigation.setTopic(topic);
        investigation.setNote(note);
        investigation.setDescription(description);
        if (startDate != null && startDate != -1) {
            investigation.setStartDate(new Date(startDate));
        }
        if (endDate != null && endDate != -1) {
            investigation.setEndDate(new Date(endDate));
        }
        LOGGER.debug("Try to add investigation to study with id {}", id);
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug(" - Searching for study");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Study.simple");
            Study study = mdm.find(Study.class, id);
            if (study == null) {
                throw new WebApplicationException(new Exception("Study for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
            LOGGER.debug("Assigning study with id {} to investigation.", study.getStudyId());
            investigation.setStudy(study);
            LOGGER.debug(" - Saving investigation");
            investigation = mdm.save(investigation);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.default");
            return new InvestigationWrapper(mdm.find(Investigation.class, investigation.getInvestigationId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add investigation to study.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultStudy> updateStudyById(Long id, String groupId, String topic, String note, String legalNote, Long startDate, Long endDate, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to update study with id {}", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Study.default");
            Study study = mdm.find(Study.class, id);
            if (study == null) {
                throw new WebApplicationException(new Exception("Study for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            study.setTopic(topic);
            study.setNote(note);
            study.setLegalNote(legalNote);
            if (startDate != null && startDate != -1) {
                study.setStartDate(new Date(startDate));
            }
            if (endDate != null && endDate != -1) {
                study.setEndDate(new Date(endDate));
            }
            LOGGER.debug("Persisting updated study.");
            study = mdm.save(study);
            //@TODO: check whether wer have to get the study again to obtain the Study.default graph
            return new StudyWrapper(mdm.find(Study.class, study));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to udpate study.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public Response deleteStudyById(Long id, String groupId, HttpContext hc) {
        LOGGER.warn("Deleting studies is currently not supported.");
        throw new WebApplicationException(405);
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> getInvestigations(String groupId, Long studyId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        List<Investigation> investigations;
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        LOGGER.debug("Try to get investigations ({}-{}) for study {} (0 = all)", first, first + results, studyId);
        if (studyId != null && studyId > 0) {//get investigations by study
            try {
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Study.simple");
                Study study = mdm.find(Study.class, studyId);
                if (study == null) {
                    //no study found for id studyId
                    LOGGER.error("Study with id {} not found.", studyId);
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.default");
                investigations = new InvestigationSecureQueryHelper().getInvestigationsInStudy(study, mdm, first, results, ctx);
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Failed to get investigations.", ex);
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            } finally {
                mdm.close();
            }
        } else {//no study provided...get all investigation
            LOGGER.debug("StudyId is 0. Try getting all investigations.");
            try {
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.default");
                investigations = new InvestigationSecureQueryHelper().getReadableInvestigations(mdm, first, results, ctx);
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Failed to get investigations.", ex);
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            } finally {
                mdm.close();
            }
        }
        return new InvestigationWrapper(investigations);
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> getInvestigationCount(String groupId, Long studyId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting investigation count.");
            return new InvestigationWrapper(new InvestigationSecureQueryHelper().getInvestigationCount(mdm, ctx).intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get investigation count.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> getInvestigationById(Long id, String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to get investigation for id {}", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.default");
            Investigation investigation = mdm.find(Investigation.class, id);
            if (investigation == null) {
                throw new WebApplicationException(new Exception("Investigation for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            return new InvestigationWrapper(investigation);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get investigation by id.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> addDigitalObjectToInvestigation(Long id, String groupId, String label, Long uploaderId, String note, Long startDate, Long endDate, HttpContext hc) {
        LOGGER.debug("Try to add digital object to investigation with id {}", id);
        LOGGER.debug(" - Creating digital object");
        DigitalObject object = DigitalObject.factoryNewDigitalObject();
        object.setLabel(label);
        object.setNote(note);
        if (startDate != null && startDate != -1) {
            object.setStartDate(new Date(startDate));
        }
        if (endDate != null && endDate != -1) {
            object.setEndDate(new Date(endDate));
        }
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        UserData uploader;
        if (uploaderId == null || uploaderId <= 0) {
            uploader = null;
        } else {
            try {
                uploader = mdm.find(UserData.class, uploaderId);
            } catch (UnauthorizedAccessAttemptException ex) {
                uploader = null;
            }
        }
        object.setUploader(uploader);

        try {
            LOGGER.debug(" - Try to find investigation");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.simple");
            Investigation investigation = mdm.find(Investigation.class, id);
            if (investigation == null) {
                throw new WebApplicationException(new Exception("Investigation for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
            LOGGER.debug(" - Adding digital object to investigation");
            object.setInvestigation(investigation);
            object = mdm.save(object);

            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
            return new DigitalObjectWrapper(mdm.find(DigitalObject.class, object.getBaseId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add digital object to investigation.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> updateInvestigationById(Long id, String groupId, String topic, String note, String description, Long startDate, Long endDate, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to update investigation with id {}", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.default");
            Investigation investigation = mdm.find(Investigation.class, id);
            if (investigation == null) {
                throw new WebApplicationException(new Exception("Investigation for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            investigation.setTopic(topic);
            investigation.setNote(note);
            investigation.setDescription(description);
            if (startDate != null && startDate != -1) {
                investigation.setStartDate(new Date(startDate));
            }
            if (endDate != null && endDate != -1) {
                investigation.setEndDate(new Date(endDate));
            }
            investigation = mdm.save(investigation);

            return new InvestigationWrapper(mdm.find(Investigation.class, investigation.getInvestigationId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to update investigation by id.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public Response deleteInvestigationById(Long id, String groupId, HttpContext hc) {
        LOGGER.warn("Deleting investigations is currently not supported.");
        throw new WebApplicationException(405);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjects(String groupId, Long investigationId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to get digital objects ({}-{}) for investigation with id {} (0=all)", first, first + results, investigationId);
            List<DigitalObject> objects;
            if (investigationId <= 0) {
                //no investigationId provided...get all objects
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
                objects = mdm.findResultList("SELECT o FROM DigitalObject o", DigitalObject.class, first, results);
            } else {
                //first, obtain investigation for id
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.simple");
                Investigation investigation = mdm.find(Investigation.class, investigationId);
                if (investigation == null) {
                    LOGGER.error("Investigation for id {} not found.", investigationId);
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
                //try to get objects in investigation
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
                objects = mdm.findResultList("SELECT o FROM DigitalObject o WHERE o.investigation.investigationId=" + investigationId, DigitalObject.class, first, results);
            }
            return new DigitalObjectWrapper(objects);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get digital objects.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectCount(String groupId, Long investigationId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Get digital object count for investigation {} (0=all)", investigationId);
            int count = 0;
            if (investigationId <= 0) {
                //no investigationId provided...get all objects
                count = new DigitalObjectSecureQueryHelper().getReadableResourceCount(mdm, ctx);
            } else {
                //first, obtain investigation for id
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.simple");
                Investigation investigation = mdm.find(Investigation.class, investigationId);
                if (investigation == null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
                //try to get objects in investigation
                mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
                count = new DigitalObjectSecureQueryHelper().getObjectCountInInvestigation(investigation, null, mdm, ctx).intValue();
            }

            return new DigitalObjectWrapper(count);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get digital object count.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectById(Long id, String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to get digital object by id {}", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
            DigitalObject object = mdm.find(DigitalObject.class, id);
            if (object == null) {
                throw new WebApplicationException(new Exception("DigitalObject for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            return new DigitalObjectWrapper(object);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get digital object by id.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectByDOI(String digitalObjectId, String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to get digital object by DOI {}", digitalObjectId);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
            DigitalObject result = mdm.findSingleResult("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier='" + digitalObjectId + "'", DigitalObject.class);

            if (result == null) {
                throw new WebApplicationException(new Exception("DigitalObject for digital object id " + digitalObjectId + " not found"), Response.Status.NOT_FOUND);
            }
            return new DigitalObjectWrapper(result);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get digital object by DOI", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> updateDigitalObjectById(Long id, String groupId, String label, String note, Long startDate, Long endDate, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to update digital object with id {}", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
            DigitalObject object = mdm.find(DigitalObject.class, id);
            if (object == null) {
                throw new WebApplicationException(new Exception("DigitalObject for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            object.setLabel(label);
            object.setNote(note);
            if (startDate != null && startDate != -1) {
                object.setStartDate(new Date(startDate));
            }
            if (endDate != null && endDate != -1) {
                object.setEndDate(new Date(endDate));
            }
            object = mdm.save(object);
            return new DigitalObjectWrapper(object);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to update digital object by id.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public Response deleteDigitalObjectById(Long id, String groupId, HttpContext hc) {
        LOGGER.warn("Deleting digital objects is currently not supported.");
        throw new WebApplicationException(405);
    }

    @Override
    public IEntityWrapper<? extends IDefaultOrganizationUnit> getOrganizationUnits(String groupId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to get organization units ({}-{})", first, first + results);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "OrganizationUnit.default");
            return new OrganizationUnitWrapper(mdm.find(OrganizationUnit.class));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get organization units.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultOrganizationUnit> createOrganizationUnit(String groupId, String ouName, Long managerUserId, String address, String zipCode, String city, String country, String website, HttpContext hc) {
        OrganizationUnit ou = new OrganizationUnit();
        ou.setOuName(ouName);
        ou.setAddress(address);
        ou.setCity(city);
        ou.setZipCode(zipCode);
        ou.setCountry(country);
        ou.setWebsite(website);

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);

        try {
            UserData managerUser;
            if (managerUserId == null || -1 == managerUserId) {
                LOGGER.debug("No manager user id specified or id is -1. Getting user id from caller id {}.", ctx.getUserId());
                managerUser = mdm.findSingleResult("SELECT x FROM UserData x WHERE x.distinguishedName LIKE '" + ctx.getUserId().getStringRepresentation() + "'", UserData.class);
            } else {
                LOGGER.debug("Getting manager user for id {}", managerUserId);
                managerUser = mdm.find(UserData.class, managerUserId);
            }
            if (managerUser == null) {
                LOGGER.error("No manager user found for id " + managerUserId + " and caller " + ctx.getUserId());
                throw new WebApplicationException(new Exception("User for id " + managerUserId + " not found"), Response.Status.NOT_FOUND);
            }
            ou.setManager(managerUser);

            LOGGER.debug("Try to save organization unit.");
            ou = mdm.save(ou);

            return new OrganizationUnitWrapper(mdm.find(OrganizationUnit.class, ou.getOrganizationUnitId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add organization unit.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultOrganizationUnit> getOrganizationUnitCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Try to get organization unit count.");
            return new OrganizationUnitWrapper(((Number) mdm.findSingleResult("SELECT COUNT(x) FROM OrganizationUnit x")).intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get organization unit count.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultOrganizationUnit> getOrganizationUnitById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        OrganizationUnit organizationUnit;
        try {
            LOGGER.debug("Try to get organization unit by id {}", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "OrganizationUnit.default");
            organizationUnit = mdm.find(OrganizationUnit.class, id);
            if (organizationUnit == null) {
                LOGGER.error("OrganizationUnit for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            return new OrganizationUnitWrapper(organizationUnit);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get organization unit by id", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultOrganizationUnit> updateOrganizationUnit(String groupId, Long id, String ouName, String address, String zipCode, String city, String country, String website, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);

        try {
            LOGGER.debug("Try to update organization unit with id {}", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "OrganizationUnit.default");
            OrganizationUnit ou = mdm.find(OrganizationUnit.class, id);
            if (ou == null) {
                LOGGER.error("OrganizationUnit for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            ou.setOuName(ouName);
            ou.setAddress(address);
            ou.setCity(city);
            ou.setZipCode(zipCode);
            ou.setCountry(country);
            ou.setWebsite(website);
            ou = mdm.save(ou);
            return new OrganizationUnitWrapper(ou);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to update organization unit by id", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultMetaDataSchema> getMetadataSchemas(String groupId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "MetaDataSchema.default");
            return new MetadataSchemaWrapper(mdm.find(MetaDataSchema.class));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get metadata schemas.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultMetaDataSchema> createMetadataSchema(String groupId, String identifier, String schemaUrl, HttpContext hc) {
        MetaDataSchema schema = new MetaDataSchema(identifier);

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);

        try {
            //check for existing schemas
            List<MetaDataSchema> existingSchema = mdm.find(schema, schema);
            if (!existingSchema.isEmpty()) {
                LOGGER.info("Metadata schema for id {} already exists. Returning existing entity.", identifier);
                schema = existingSchema.get(0);
            } else {
                //not existing, save
                schema.setMetaDataSchemaUrl(schemaUrl);
                schema = mdm.save(schema);
            }

            return new MetadataSchemaWrapper(mdm.find(MetaDataSchema.class, schema.getId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to create metadata schema", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultMetaDataSchema> getMetadataSchemaCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            return new MetadataSchemaWrapper(((Number) mdm.findSingleResult("SELECT COUNT(x) FROM MetaDataSchema x")).intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get metadata schema count.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultMetaDataSchema> getMetadataSchemaById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "MetaDataSchema.default");
            MetaDataSchema metadataSchema = mdm.find(MetaDataSchema.class, id);
            if (metadataSchema == null) {
                throw new WebApplicationException(new Exception("MetaDataSchema for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            return new MetadataSchemaWrapper(metadataSchema);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get metadata schema by id.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultTask> getTasks(String groupId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Task.default");
            return new TaskWrapper(mdm.find(Task.class));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get tasks.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultTask> createTask(String groupId, String taskName, HttpContext hc) {
        Task task = new Task(taskName);

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            //check for existing schemas
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Task.default");
            List<Task> existingTask = mdm.find(task, task);
            if (!existingTask.isEmpty()) {
                task = existingTask.get(0);
            } else {
                task = mdm.save(task);
            }

            return new TaskWrapper(mdm.find(Task.class, task.getTaskId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to create task.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultTask> getTaskCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            return new TaskWrapper(((Number) mdm.findSingleResult("SELECT COUNT(x) FROM Task x")).intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get task count.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultTask> getTaskById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Task.default");
            Task task = mdm.find(Task.class, id);
            if (task == null) {
                throw new WebApplicationException(new Exception("Task for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            return new TaskWrapper(task);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get task by id.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> getUserDataEntities(String groupId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");
            return new UserDataWrapper(mdm.find(UserData.class));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get userdata entities.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> getUserDataCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            return new UserDataWrapper(((Number) mdm.findSingleResult("SELECT COUNT(x) FROM UserData x")).intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get userdata count.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultUserData> getUserDataById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        UserData userData;
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");
            userData = mdm.find(UserData.class, id);
            if (userData == null) {
                throw new WebApplicationException(new Exception("UserData for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            return new UserDataWrapper(userData);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to userdata by id.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultParticipant> getParticipantById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Participant.default");
            Participant participant = mdm.find(Participant.class, id);
            if (participant == null) {
                throw new WebApplicationException(new Exception("Participant for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            return new ParticipantWrapper(participant);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get participant by id.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultRelation> getRelationById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Relation.default");
            Relation relation = mdm.find(Relation.class, id);
            if (relation == null) {
                throw new WebApplicationException(new Exception("Relation for id " + id + " not found"), Response.Status.NOT_FOUND);
            }
            return new RelationWrapper(relation);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to get relation by id.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultStudy> addRelationToStudy(String groupId, Long studyId, Long organizationUnitId, Long taskId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Study.default");
            Study study = mdm.find(Study.class, studyId);
            if (study == null) {
                LOGGER.error("Study for id " + studyId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "OrganizationUnit.simple");
            OrganizationUnit ou = mdm.find(OrganizationUnit.class, organizationUnitId);
            if (ou == null) {
                LOGGER.error("OrganizationUnit for id " + organizationUnitId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            Task task;

            if (taskId == null || taskId <= 0) {
                LOGGER.debug("No task provided. Setting task to 'null'.");
                task = null;
            } else {
                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Task.simple");
                task = mdm.find(Task.class, taskId);
                if (task == null) {
                    LOGGER.warn("Task for id " + taskId + " not found. Setting task to 'null'.");
                }
            }

            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
            Relation relation = new Relation(ou, task);
            relation = mdm.save(relation);
            LOGGER.debug("Relation saved. Adding relation to study and updating study.");
            study.addRelation(relation);
            study = mdm.update(study);
            LOGGER.debug("Study updated.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Study.default");
            return new StudyWrapper(mdm.find(Study.class, study.getStudyId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add relation to study.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to add relation to study.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> addParticipantToInvestigation(String groupId, Long investigationId, Long userDataId, Long taskId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.default");
            Investigation investigation = mdm.find(Investigation.class, investigationId);
            if (investigation == null) {
                LOGGER.error("Investigation for id " + investigationId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");
            UserData user = mdm.find(UserData.class, userDataId);
            if (user == null) {
                LOGGER.error("UserData for id " + userDataId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Task.default");
            Task task = mdm.find(Task.class, taskId);
            if (task == null) {
                LOGGER.error("Task for id " + taskId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);

            Participant participant = new Participant(user, task);
            LOGGER.debug("Participant saved.");
            participant = mdm.save(participant);
            LOGGER.debug("Adding participant to investigation and updating investigation.");
            investigation.addParticipant(participant);
            investigation = mdm.update(investigation);
            LOGGER.debug("Investigation updated.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.default");
            return new InvestigationWrapper(mdm.find(Investigation.class, investigation.getInvestigationId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add participant to investigation.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to add participant to investigation.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultInvestigation> addMetadataSchemaToInvestigation(String groupId, Long investigationId, Long metadataSchemaId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.default");
            Investigation investigation = mdm.find(Investigation.class, investigationId);
            if (investigation == null) {
                LOGGER.error("Investigation for id " + investigationId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "MetaDataSchema.simple");
            MetaDataSchema schema = mdm.find(MetaDataSchema.class, metadataSchemaId);
            if (schema == null) {
                LOGGER.error("MetaDataSchema for id " + metadataSchemaId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
            LOGGER.debug("Adding schema to investigation and updating investigation.");
            investigation.addMetaDataSchema(schema);
            investigation = mdm.update(investigation);
            LOGGER.debug("Investigation updated.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.default");
            return new InvestigationWrapper(mdm.find(Investigation.class, investigation.getInvestigationId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add metadata schema to investigation.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to add metadata schema to investigation.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> addExperimenterToDigitalObject(String groupId, Long baseId, Long userDataId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
            DigitalObject digitalObject = mdm.find(DigitalObject.class, baseId);
            if (digitalObject == null) {
                LOGGER.error("DigitalObject for id " + baseId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "UserData.default");
            UserData user = mdm.find(UserData.class, userDataId);
            if (user == null) {
                LOGGER.error("UserData for id " + userDataId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);

            LOGGER.debug("Adding experimenter to digitalObject and updating digitalObject.");
            digitalObject.addExperimenter(user);
            digitalObject = mdm.update(digitalObject);
            LOGGER.debug("DigitalObject updated.");

            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
            return new DigitalObjectWrapper(digitalObject);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add experimenter to digital object.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("Failed to add experimenter to digital object.", ex);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } finally {
            mdm.close();
        }
    }

    @Override
    public Response checkService() {
        ServiceStatus status;
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            LOGGER.debug("Doing service check by getting object count");
            Number result = (Number) mdm.findSingleResult("SELECT COUNT(o) FROM DigitalObject o");
            LOGGER.debug("Service check using object count returned {} objects.", result);
            status = ServiceStatus.OK;
        } catch (Throwable t) {
            LOGGER.error("Obtaining object count returned an error. Service status is set to ERROR", t);
            status = ServiceStatus.ERROR;
        } finally {
            mdm.close();
        }
        return Response.status(200).entity(new CheckServiceResponse("BaseMetadata", status)).build();
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectType> getDigitalObjectTypes(String groupId, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Obtaining DigitalObjectType entities.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectType.default");
            List<DigitalObjectType> types = mdm.findResultList("SELECT t FROM DigitalObjectType t", DigitalObjectType.class, first, results);
            LOGGER.debug("{} entities obtained. Returning result.", types.size());
            return new DigitalObjectTypeWrapper(types);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain all digital object types.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectType> getDigitalObjectTypeById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Obtaining DigitalObjectType for id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectType.default");
            DigitalObjectType type = mdm.find(DigitalObjectType.class, id);
            if (type == null) {
                LOGGER.error("DigitalObjectType for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            LOGGER.debug("Entity obtained. Returning result.");
            return new DigitalObjectTypeWrapper(type);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain digital object type for id " + id + ".", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectType> addDigitalObjectType(String groupId, String typeDomain, String identifier, int version, String description, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Creating new DigitalObjectType.");
            DigitalObjectType t = new DigitalObjectType();
            t.setTypeDomain(typeDomain);
            t.setIdentifier(identifier);
            t.setVersion(version);
            List<DigitalObjectType> existing = mdm.find(t, t);
            if (!existing.isEmpty()) {
                t = existing.get(0);
                LOGGER.debug("Existing DigitalObjectType found for domain, identifier and version. Updating description and returning entity with id {}", t.getId());
            }

            t.setDescription(description);
            t = mdm.save(t);
            LOGGER.debug("DigitalObjectType entity with id {} stored/updated. Returning result.", t.getId());

            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectType.default");
            return new DigitalObjectTypeWrapper(mdm.find(DigitalObjectType.class, t.getId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to store digital object type.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectType> getDigitalObjectTypeCount(String groupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Obtaining DigitalObjectType count.");
            Number count = (Number) mdm.findSingleResult("SELECT COUNT(t) FROM DigitalObjectType t");
            LOGGER.debug("Count with value {} obtained. Returning result.", count.intValue());
            return new DigitalObjectTypeWrapper(count.intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain digital object type count.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectsForDigitalObjectType(String groupId, Long id, Integer first, Integer results, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Obtaining DigitalObjectType for id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectType.simple");
            DigitalObjectType type = mdm.find(DigitalObjectType.class, id);

            if (type == null) {
                LOGGER.error("DigitalObjectType for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            List<DigitalObject> result = DigitalObjectTypeQueryHelper.getDigitalObjectsByDigitalObjectType(type, ctx);
            LOGGER.debug("Obtained {} digital objects associated with object type  with id {}. Returning result.", result.size(), id);
            return new DigitalObjectWrapper(result);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain digital objects for type with id " + id + ".", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObject> getDigitalObjectCountForType(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Obtaining DigitalObjectType for id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectType.simple");
            DigitalObjectType type = mdm.find(DigitalObjectType.class, id);

            if (type == null) {
                LOGGER.error("DigitalObjectType for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            Long count = DigitalObjectTypeQueryHelper.getDigitalObjectsByDigitalObjectTypeCount(type, ctx);
            LOGGER.debug("Obtained mapping count of {}. Returning result.", count);
            return new DigitalObjectWrapper(count.intValue());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain digital object count for type with id " + id + ".", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectType> getDigitalObjectTypesForDigitalObject(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Obtaining DigitalObject for id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
            DigitalObject object = mdm.find(DigitalObject.class, id);

            if (object == null) {
                LOGGER.error("DigitalObject for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            LOGGER.debug("Getting DigitalObjectType entities for digital object.");
            List<DigitalObjectType> types = DigitalObjectTypeQueryHelper.getTypesOfObject(object, ctx);
            LOGGER.debug("Obtained {} type(s) for object with id {}. Returning result.", types.size(), id);
            return new DigitalObjectTypeWrapper(types);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain digital object types for digital object with id " + id + ".", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectType> addDigitalObjectTypeToDigitalObject(String groupId, Long id, Long objectTypeId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Obtaining DigitalObjectType for id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
            DigitalObject object = mdm.find(DigitalObject.class, id);

            if (object == null) {
                LOGGER.error("DigitalObject for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectType.simple");
            DigitalObjectType type = mdm.find(DigitalObjectType.class, objectTypeId);
            if (type == null) {
                LOGGER.error("DigitalObjectType for id " + objectTypeId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
            ObjectTypeMapping mapping = new ObjectTypeMapping();
            mapping.setDigitalObject(object);
            mapping.setObjectType(type);
            LOGGER.debug("Writing object-type mapping to database.");
            mapping = mdm.save(mapping);
            LOGGER.debug("Stored new object-type mapping with id {}. Returning DigitalObjectType.", mapping.getObjectType().getId());
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectType.default");
            return new DigitalObjectTypeWrapper(mdm.find(DigitalObjectType.class, mapping.getObjectType().getId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to store object-type mapping to database.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectTransition> getDigitalObjectTransitionById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Obtaining DigitalObjectTransition for id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectTransition.default");
            DigitalObjectTransition transition = mdm.find(DigitalObjectTransition.class, id);

            if (transition == null) {
                LOGGER.error("DigitalObjectTransition for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            LOGGER.debug("Obtained DigitalObjectTransition. Returning result.");
            return new DigitalObjectTransitionWrapper(transition);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain digital object transition with id " + id + ".", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectTransition> getDigitalObjectDerivationInformation(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting DigitalObject for id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
            DigitalObject object = mdm.find(DigitalObject.class, id);
            if (object == null) {
                LOGGER.error("DigitalObject for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            LOGGER.debug("DigitalObject obtained. Getting transition with object with id {} as output.", object.getBaseId());
            List<DigitalObjectTransition> transitions = TransitionQueryHelper.getTransitionsByOutputObject(object, ctx);
            LOGGER.debug("Obtained {} DigitalObjectTransition(s) where the object with the id {} is derived from. Returning result.", transitions.size(), id);
            return new DigitalObjectTransitionWrapper(transitions);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain digital object transitions for object with id " + id + " as output.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectTransition> addDigitalObjectTransition(String groupId, String inputObjectMap, String outputObjectList, TransitionType type, String typeData, HttpContext hc) {
        LOGGER.debug("Try to add digital object transition of type '{}'", type);
        DigitalObjectTransition transition;
        try {
            LOGGER.debug("Obtaining transition type handler.");
            AbstractTransitionTypeHandler handler = TransitionTypeHandlerFactory.factoryTransitionTypeHandler(type);
            LOGGER.debug("Handler obtained. Creating transition entity.");
            transition = handler.factoryTransitionEntity();
            LOGGER.debug("Handling provided transition type data '{}'", typeData);
            Object entity = handler.handleTransitionEntityData(typeData);
            LOGGER.debug("Obtaining entity id from entity.");
            String entityId = handler.getTransitionEntityId(entity);
            LOGGER.debug("Setting obtained entity id '{}' at transition.", entityId);
            transition.setTransitionEntityId(entityId);
        } catch (ConfigurationException | PersistFailedException ex) {
            LOGGER.error("Failed to add transition of type '" + type + "'", ex);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(ctx);
        try {
            //parsing and validating input mappings
            try {
                JSONArray array = new JSONArray(inputObjectMap);
                LOGGER.debug("Parsing input object mappings from JSON array.");
                for (int i = 0; i < array.length(); i++) {
                    LOGGER.debug("Getting mapping '{}'", i);
                    JSONObject objectViewMapping = array.getJSONObject(i);
                    String objectId = (String) objectViewMapping.keys().next();
                    String viewName = objectViewMapping.getString(objectId);

                    long lObjectId = Long.parseLong(objectId);
                    DigitalObject result = mdm.find(DigitalObject.class, lObjectId);
                    if (result == null) {
                        LOGGER.error("The transition input object with the id  " + lObjectId + " was not found.");
                        throw new WebApplicationException(Response.Status.NOT_FOUND);
                    }

                    LOGGER.debug("Adding input mapping {}:{} to transition.", objectId, viewName);
                    transition.addInputMapping(result, viewName);
                }
            } catch (JSONException ex) {
                LOGGER.error("Failed to parse argument inputObjectMap with value " + inputObjectMap + " as JSON array.", ex);
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            //parsing and validating output objects
            try {
                JSONArray array = new JSONArray(outputObjectList);
                LOGGER.debug("Parsing output object lsit from JSON array.");
                for (int i = 0; i < array.length(); i++) {
                    LOGGER.debug("Getting mapping as long value '{}'", i);
                    long objectId = array.getLong(i);
                    DigitalObject result = mdm.find(DigitalObject.class, objectId);
                    if (result == null) {
                        LOGGER.error("The transition output object with the id  " + objectId + " was not found.");
                        throw new WebApplicationException(Response.Status.NOT_FOUND);
                    }
                    LOGGER.debug("Adding object id {} to temporary list.", objectId);
                    transition.addOutputObject(result);
                }
            } catch (JSONException ex) {
                LOGGER.error("Failed to parse argument outputObjectList with value " + outputObjectList + " as JSON array.", ex);
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            LOGGER.debug("Persisting transition.");
            transition = mdm.save(transition);
            LOGGER.debug("Querying and returning default transition representation.");
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectTransition.default");
            return new DigitalObjectTransitionWrapper(mdm.find(DigitalObjectTransition.class, transition.getId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add digital object transition.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectTransition> getDigitalObjectContributionInformation(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting DigitalObject for id {}.", id);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
            DigitalObject object = mdm.find(DigitalObject.class, id);
            if (object == null) {
                LOGGER.error("DigitalObject for id " + id + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            LOGGER.debug("DigitalObject obtained. Getting transition with object as input.");

            List<DigitalObjectTransition> transitions = TransitionQueryHelper.getTransitionsByInputObject(object, ctx);
            LOGGER.debug("Obtained {} DigitalObjectTransition(s) where the object with the id {} contributes to. Returning result.", transitions.size(), id);
            return new DigitalObjectTransitionWrapper(transitions);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain digital object transitions for object with id " + id + " as output.", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDigitalObjectTransition> addTransitionToDigitalObject(String groupId, Long baseId, Long otherBaseId, String viewName, Long outputBaseId, TransitionType type, String typeData, HttpContext hc) {
        LOGGER.debug("Try to add digital object transition of type '{}'", type);
        DigitalObjectTransition transition;
        try {
            LOGGER.debug("Obtaining transition type handler.");
            AbstractTransitionTypeHandler handler = TransitionTypeHandlerFactory.factoryTransitionTypeHandler(type);
            LOGGER.debug("Handler obtained. Creating transition entity.");
            transition = handler.factoryTransitionEntity();
            LOGGER.debug("Handling provided transition type data '{}'", typeData);
            Object entity = handler.handleTransitionEntityData(typeData);
            LOGGER.debug("Obtaining entity id from entity.");
            String entityId = handler.getTransitionEntityId(entity);
            LOGGER.debug("Setting obtained entity id '{}' at transition.", entityId);
            transition.setTransitionEntityId(entityId);
        } catch (ConfigurationException | PersistFailedException ex) {
            LOGGER.error("Failed to add transition of type '" + type + "'", ex);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting DigitalObject for id {}.", baseId);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
            DigitalObject object = mdm.find(DigitalObject.class, baseId);
            if (object == null) {
                LOGGER.error("DigitalObject for id " + baseId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            DigitalObject otherObject = mdm.find(DigitalObject.class, otherBaseId);
            if (otherObject == null) {
                LOGGER.error("DigitalObject for id " + otherBaseId + " not found");
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            DigitalObject input = object;
            DigitalObject output = otherObject;
            LOGGER.debug("Checking outputId argument for plausibility.");
            if (outputBaseId == null || new Long(-1l).equals(outputBaseId)) {
                LOGGER.debug("outputId is either null or -1. Using otherId as output object.");
            } else if (!Objects.equals(outputBaseId, baseId) && !Objects.equals(outputBaseId, otherBaseId)) {
                LOGGER.error("Transition output id " + outputBaseId + " is not equal to one of the provided ids " + baseId + " and " + outputBaseId);
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            } else {
                LOGGER.debug("outputId equals id. Using id as output object.");
                if (Objects.equals(outputBaseId, baseId)) {
                    input = otherObject;
                    output = object;
                }
            }

            mdm.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
            DigitalObjectTransition t = new DigitalObjectTransition();
            t.addInputMapping(input, viewName);
            t.addOutputObject(output);
            t.setCreationTimestamp(System.currentTimeMillis());
            LOGGER.debug("Storing DigitalObjectTransition.");
            t = mdm.save(t);
            LOGGER.debug("DigitalObjectTransition stored with id {}. Returning result.", t.getId());
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectTransition.default");
            return new DigitalObjectTransitionWrapper(mdm.find(DigitalObjectTransition.class, t.getId()));
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to add digital object transition for object with ids " + baseId + " and " + otherBaseId + ".", ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

}
