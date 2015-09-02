/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
import edu.kit.dama.commons.types.DigitalObjectId;
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
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.tools.DigitalObjectSecureQueryHelper;
import edu.kit.dama.mdm.tools.DigitalObjectTypeQueryHelper;
import edu.kit.dama.mdm.tools.InvestigationSecureQueryHelper;
import edu.kit.dama.mdm.tools.StudySecureQueryHelper;
import edu.kit.dama.mdm.tools.TransitionQueryHelper;
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
import edu.kit.dama.rest.util.RestUtils;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.util.Constants;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
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
  public StreamingOutput getStudyIds(String groupId,
          Integer first,
          Integer results,
          HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);

    try {
      List<Study> studies = new StudySecureQueryHelper().getReadableStudies(mdm, first, results, ctx);
      return createObjectGraphStream(StudyWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new StudyWrapper(studies));
    } catch (UnauthorizedAccessAttemptException ex) {
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput createStudy(String groupId,
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
        throw new WebApplicationException(new Exception("User for id " + managerUserId + " not found"), 404);
      }
      study.setManager(managerUser);
      study = mdm.save(study);
      return createObjectGraphStream(StudyWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(study));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to create a new study.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getStudyCount(String groupId,
          HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to get study count.");
      Integer cnt = new StudySecureQueryHelper().getStudyCount(mdm, ctx).intValue();
      return createObjectGraphStream(StudyWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(cnt));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get study count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getStudyById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to get study with if {}", id);
      Study study = mdm.find(Study.class, id);
      if (study == null) {
        throw new WebApplicationException(new Exception("Study for id " + id + " not found"), 404);
      }
      return createObjectGraphStream(StudyWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(study));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get study.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addInvestigationToStudy(String groupId, Long id, String topic, String note, String description, Long startDate, Long endDate, HttpContext hc) {
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
      Study study = mdm.find(Study.class, id);
      if (study == null) {
        throw new WebApplicationException(new Exception("Study for id " + id + " not found"), 404);
      }
      investigation.setStudy(study);
      LOGGER.debug(" - Saving investigation");
      mdm.save(investigation);
      LOGGER.debug(" - Persisting updated study");
      return createObjectGraphStream(InvestigationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(investigation));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to add investigation to study.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput updateStudyById(Long id, String groupId, String topic, String note, String legalNote, Long startDate, Long endDate, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to update study with id {}", id);
      Study study = mdm.find(Study.class, id);
      if (study == null) {
        throw new WebApplicationException(new Exception("Study for id " + id + " not found"), 404);
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
      return createObjectGraphStream(StudyWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(study));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to udpate study.", ex);
      throw new WebApplicationException(401);
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
  public StreamingOutput getInvestigationIds(String groupId, Long studyId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    List<Investigation> investigations;
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    LOGGER.debug("Try to get investigation ids ({}-{}) for study {} (0 = all)", first, first + results, studyId);
    if (studyId != null && studyId > 0) {//get investigations by study
      try {
        Study study = mdm.find(Study.class, studyId);
        if (study == null) {
          //no study found for id studyId
          LOGGER.error("Study with id {} not found.", studyId);
          throw new WebApplicationException(404);
        }
        investigations = new InvestigationSecureQueryHelper().getInvestigationsInStudy(study, mdm, first, results, ctx);
      } catch (UnauthorizedAccessAttemptException ex) {
        LOGGER.error("Failed to get investigation ids.", ex);
        throw new WebApplicationException(401);
      } finally {
        mdm.close();
      }
    } else {//no study provided...get all investigation
      LOGGER.debug("StudyId is 0. Try getting all investigation ids.");
      try {
        investigations = new InvestigationSecureQueryHelper().getReadableInvestigations(mdm, first, results, ctx);
      } catch (UnauthorizedAccessAttemptException ex) {
        LOGGER.error("Failed to get investigation ids.", ex);
        throw new WebApplicationException(401);
      } finally {
        mdm.close();
      }
    }
    return createObjectGraphStream(InvestigationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new InvestigationWrapper(investigations));
  }

  @Override
  public StreamingOutput getInvestigationCount(String groupId, Long studyId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to get investigation count.");
      Integer cnt = new InvestigationSecureQueryHelper().getInvestigationCount(mdm, ctx).intValue();
      return createObjectGraphStream(InvestigationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(cnt));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get investigation count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getInvestigationById(Long id, String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to get investigation for id {}", id);
      Investigation investigation = mdm.find(Investigation.class, id);
      if (investigation == null) {
        throw new WebApplicationException(new Exception("Investigation for id " + id + " not found"), 404);
      }
      return createObjectGraphStream(InvestigationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(investigation));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get investigation by id.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addDigitalObjectToInvestigation(Long id, String groupId, String label, Long uploaderId, String note, Long startDate, Long endDate, HttpContext hc) {
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
      Investigation investigation = mdm.find(Investigation.class, id);
      if (investigation == null) {
        throw new WebApplicationException(new Exception("Investigation for id " + id + " not found"), 404);
      }
      LOGGER.debug(" - Adding digital object to investigation");
      object.setInvestigation(investigation);
      mdm.save(object);
      return createObjectGraphStream(DigitalObjectWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(object));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to add digital object to investigation.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput updateInvestigationById(Long id, String groupId, String topic, String note, String description, Long startDate, Long endDate, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to update investigation with id {}", id);
      Investigation investigation = mdm.find(Investigation.class, id);
      if (investigation == null) {
        throw new WebApplicationException(new Exception("Investigation for id " + id + " not found"), 404);
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
      return createObjectGraphStream(InvestigationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(investigation));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to update investigation by id.", ex);
      throw new WebApplicationException(401);
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
  public StreamingOutput getDigitalObjectIds(String groupId, Long investigationId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to get digital object ids ({}-{}) for investigation with id {} (0=all)", first, first + results, investigationId);
      List<DigitalObject> objects;
      if (investigationId <= 0) {
        //no investigationId provided...get all objects
        objects = new DigitalObjectSecureQueryHelper().getReadableResources(mdm, first, results, ctx);
      } else {
        //first, obtain investigation for id
        Investigation investigation = mdm.find(Investigation.class, investigationId);
        if (investigation == null) {
          LOGGER.error("Investigation for id {} not found.", investigationId);
          throw new WebApplicationException(404);
        }
        //try to get objects in investigation
        objects = new DigitalObjectSecureQueryHelper().getObjectsInInvestigation(investigation, null, mdm, first, results, ctx);
      }
      return createObjectGraphStream(DigitalObjectWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectWrapper(objects));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get digital object ids.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectCount(String groupId, Long investigationId, HttpContext hc) {
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
        Investigation investigation = mdm.find(Investigation.class, investigationId);
        if (investigation == null) {
          throw new WebApplicationException(404);
        }
        //try to get objects in investigation
        count = new DigitalObjectSecureQueryHelper().getObjectCountInInvestigation(investigation, null, mdm, ctx).intValue();
      }

      return createObjectGraphStream(DigitalObjectWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(count));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get digital object count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectById(Long id, String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to get digital object by id {}", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        throw new WebApplicationException(new Exception("DigitalObject for id " + id + " not found"), 404);
      }
      return createObjectGraphStream(DigitalObjectWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(object));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get digital object by id.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectByDOI(String doi, String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to get digital object by DOI {}", doi);
      DigitalObject template = new DigitalObject();
      template.setDigitalObjectId(new DigitalObjectId(doi));

      List<DigitalObject> result = mdm.find(template, template);
      if (result.isEmpty()) {
        throw new WebApplicationException(new Exception("DigitalObject for digital object id " + doi + " not found"), 404);
      }
      return createObjectGraphStream(DigitalObjectWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(result.get(0)));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get digital object by DOI", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput updateDigitalObjectById(Long id, String groupId, String label, String note, Long startDate, Long endDate, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to update digital object with id {}", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        throw new WebApplicationException(new Exception("DigitalObject for id " + id + " not found"), 404);
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
      return createObjectGraphStream(DigitalObjectWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(object));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to update digital object by id.", ex);
      throw new WebApplicationException(401);
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
  public StreamingOutput getOrganizationUnitIds(String groupId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    List<OrganizationUnit> organizationUnits;
    try {
      LOGGER.debug("Try to getorganization unit ids ({}-{})", first, first + results);
      organizationUnits = mdm.find(OrganizationUnit.class);
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get organization unit ids.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(OrganizationUnitWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new OrganizationUnitWrapper(organizationUnits));
  }

  @Override
  public StreamingOutput createOrganizationUnit(String groupId, String ouName, Long managerUserId, String address, String zipCode, String city, String country, String website, HttpContext hc) {
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
        throw new WebApplicationException(new Exception("User for id " + managerUserId + " not found"), 404);
      }
      ou.setManager(managerUser);

      LOGGER.debug("Try to save organization unit.");
      ou = mdm.save(ou);
      return createObjectGraphStream(OrganizationUnitWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new OrganizationUnitWrapper(ou));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to add organization unit.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getOrganizationUnitCount(String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Try to get organization unit count.");
      Long result = mdm.findSingleResult("SELECT COUNT(x) FROM OrganizationUnit x", Long.class);
      return createObjectGraphStream(OrganizationUnitWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new OrganizationUnitWrapper((result != null) ? result.intValue() : 0));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get organization unit count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getOrganizationUnitById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    OrganizationUnit organizationUnit;
    try {
      LOGGER.debug("Try to get organization unit by id {}", id);
      organizationUnit = mdm.find(OrganizationUnit.class, id);
      if (organizationUnit == null) {
        LOGGER.error("OrganizationUnit for id " + id + " not found");
        throw new WebApplicationException(404);
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get organization unit by id", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(OrganizationUnitWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new OrganizationUnitWrapper(organizationUnit));
  }

  @Override
  public StreamingOutput updateOrganizationUnit(String groupId, Long id, String ouName, String address, String zipCode, String city, String country, String website, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);

    try {
      LOGGER.debug("Try to update organization unit with id {}", id);
      OrganizationUnit ou = mdm.find(OrganizationUnit.class, id);
      if (ou == null) {
        LOGGER.error("OrganizationUnit for id " + id + " not found");
        throw new WebApplicationException(404);
      }
      ou.setOuName(ouName);
      ou.setAddress(address);
      ou.setCity(city);
      ou.setZipCode(zipCode);
      ou.setCountry(country);
      ou.setWebsite(website);
      ou = mdm.save(ou);
      return createObjectGraphStream(OrganizationUnitWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new OrganizationUnitWrapper(ou));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to update organization unit by id", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getMetadataSchemaIds(String groupId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    List<MetaDataSchema> metadataSchemas;
    try {
      metadataSchemas = mdm.find(MetaDataSchema.class);
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get metadata schema ids.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(MetadataSchemaWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new MetadataSchemaWrapper(metadataSchemas));
  }

  @Override
  public StreamingOutput createMetadataSchema(String groupId, String identifier, String schemaUrl, HttpContext hc) {
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
      return createObjectGraphStream(MetadataSchemaWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new MetadataSchemaWrapper(schema));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to create metadata schema", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getMetadataSchemaCount(String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      Long lCount = mdm.findSingleResult("SELECT COUNT(x) FROM MetaDataSchema x", Long.class);
      return createObjectGraphStream(MetadataSchemaWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new MetadataSchemaWrapper((lCount != null) ? lCount.intValue() : 0));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get metadata schema count.", ex);
      throw new WebApplicationException(401);
    }
  }

  @Override
  public StreamingOutput getMetadataSchemaById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    MetaDataSchema metadataSchema;
    try {
      metadataSchema = mdm.find(MetaDataSchema.class, id);
      if (metadataSchema == null) {
        throw new WebApplicationException(new Exception("MetaDataSchema for id " + id + " not found"), 404);
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get metadata schema by id.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(MetadataSchemaWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new MetadataSchemaWrapper(metadataSchema));
  }

  @Override
  public StreamingOutput getTaskIds(String groupId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    List<Task> tasks;
    try {
      tasks = mdm.find(Task.class);
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get task ids.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(TaskWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new TaskWrapper(tasks));
  }

  @Override
  public StreamingOutput createTask(String groupId, String taskName, HttpContext hc) {
    Task task = new Task(taskName);

    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      //check for existing schemas
      List<Task> existingTask = mdm.find(task, task);
      if (!existingTask.isEmpty()) {
        task = existingTask.get(0);
      } else {
        task = mdm.save(task);
      }

      return createObjectGraphStream(TaskWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TaskWrapper(task));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to create task.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getTaskCount(String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      Long lCount = mdm.findSingleResult("SELECT COUNT(x) FROM Task x", Long.class);
      return createObjectGraphStream(TaskWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new TaskWrapper((lCount != null) ? lCount.intValue() : 0));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get task count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getTaskById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    Task task;
    try {
      task = mdm.find(Task.class, id);
      if (task == null) {
        throw new WebApplicationException(new Exception("Task for id " + id + " not found"), 404);
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get task by id.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(TaskWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TaskWrapper(task));
  }

  @Override
  public StreamingOutput getUserDataIds(String groupId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    List<UserData> userData;
    try {
      userData = mdm.find(UserData.class);
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get userdata ids.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(UserDataWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper(userData));
  }

  @Override
  public StreamingOutput getUserDataCount(String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      Long lCount = mdm.findSingleResult("SELECT COUNT(x) FROM UserData x", Long.class);
      return createObjectGraphStream(UserDataWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new UserDataWrapper((lCount != null) ? lCount.intValue() : 0));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get userdata count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getUserDataById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    UserData userData;
    try {
      userData = mdm.find(UserData.class, id);
      if (userData == null) {
        throw new WebApplicationException(new Exception("UserData for id " + id + " not found"), 404);
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to userdata by id.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(UserDataWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new UserDataWrapper(userData));
  }

  @Override
  public StreamingOutput getParticipantById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    Participant participant;
    try {
      participant = mdm.find(Participant.class, id);
      if (participant == null) {
        throw new WebApplicationException(new Exception("Participant for id " + id + " not found"), 404);
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get participant by id.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(ParticipantWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ParticipantWrapper(participant));
  }

  @Override
  public StreamingOutput getRelationById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    Relation relation;
    try {
      relation = mdm.find(Relation.class, id);
      if (relation == null) {
        throw new WebApplicationException(new Exception("Relation for id " + id + " not found"), 404);
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get relation by id.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
    return createObjectGraphStream(RelationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new RelationWrapper(relation));
  }

  @Override
  public StreamingOutput addRelationToStudy(String groupId, Long studyId, Long organizationUnitId, Long taskId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      Study study = mdm.find(Study.class, studyId);
      if (study == null) {
        LOGGER.error("Study for id " + studyId + " not found");
        throw new WebApplicationException(404);
      }

      OrganizationUnit ou = mdm.find(OrganizationUnit.class, organizationUnitId);
      if (ou == null) {
        LOGGER.error("OrganizationUnit for id " + organizationUnitId + " not found");
        throw new WebApplicationException(404);
      }

      Task task;

      if (taskId == null || taskId <= 0) {
        LOGGER.debug("No task provided. Setting task to 'null'.");
        task = null;
      } else {
        task = mdm.find(Task.class, taskId);
        if (task == null) {
          LOGGER.warn("Task for id " + taskId + " not found. Setting task to 'null'.");
        }
      }

      Relation relation = new Relation(ou, task);
      LOGGER.debug("Relation saved.");
      relation = mdm.save(relation);
      LOGGER.debug("Adding relation to study and updating study.");
      study.addRelation(relation);
      study = mdm.update(study);
      LOGGER.debug("Study updated.");
      return createObjectGraphStream(StudyWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new StudyWrapper(study));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to add relation to study.", ex);
      throw new WebApplicationException(401);
    } catch (EntityNotFoundException ex) {
      LOGGER.error("Failed to add relation to study.", ex);
      throw new WebApplicationException(404);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addParticipantToInvestigation(String groupId, Long investigationId, Long userDataId, Long taskId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      Investigation investigation = mdm.find(Investigation.class, investigationId);
      if (investigation == null) {
        LOGGER.error("Investigation for id " + investigationId + " not found");
        throw new WebApplicationException(404);
      }

      UserData user = mdm.find(UserData.class, userDataId);
      if (user == null) {
        LOGGER.error("UserData for id " + userDataId + " not found");
        throw new WebApplicationException(404);
      }

      Task task = mdm.find(Task.class, taskId);
      if (task == null) {
        LOGGER.error("Task for id " + taskId + " not found");
        throw new WebApplicationException(404);
      }
      Participant participant = new Participant(user, task);
      LOGGER.debug("Participant saved.");
      participant = mdm.save(participant);
      LOGGER.debug("Adding participant to investigation and updating investigation.");
      investigation.addParticipant(participant);
      investigation = mdm.update(investigation);
      LOGGER.debug("Investigation updated.");
      return createObjectGraphStream(InvestigationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(investigation));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to add participant to investigation.", ex);
      throw new WebApplicationException(401);
    } catch (EntityNotFoundException ex) {
      LOGGER.error("Failed to add participant to investigation.", ex);
      throw new WebApplicationException(404);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addMetadataSchemaToInvestigation(String groupId, Long investigationId, Long metadataSchemaId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      Investigation investigation = mdm.find(Investigation.class, investigationId);
      if (investigation == null) {
        LOGGER.error("Investigation for id " + investigationId + " not found");
        throw new WebApplicationException(404);
      }

      MetaDataSchema schema = mdm.find(MetaDataSchema.class, metadataSchemaId);
      if (schema == null) {
        LOGGER.error("MetaDataSchema for id " + metadataSchemaId + " not found");
        throw new WebApplicationException(404);
      }

      LOGGER.debug("Adding schema to investigation and updating investigation.");
      investigation.addMetaDataSchema(schema);
      investigation = mdm.update(investigation);
      LOGGER.debug("Investigation updated.");
      return createObjectGraphStream(InvestigationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new InvestigationWrapper(investigation));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to add metadata schema to investigation.", ex);
      throw new WebApplicationException(401);
    } catch (EntityNotFoundException ex) {
      LOGGER.error("Failed to add metadata schema to investigation.", ex);
      throw new WebApplicationException(404);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addExperimenterToDigitalObject(String groupId, Long objectId, Long userDataId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      DigitalObject digitalObject = mdm.find(DigitalObject.class, objectId);
      if (digitalObject == null) {
        LOGGER.error("DigitalObject for id " + objectId + " not found");
        throw new WebApplicationException(404);
      }

      UserData user = mdm.find(UserData.class, userDataId);
      if (user == null) {
        LOGGER.error("UserData for id " + userDataId + " not found");
        throw new WebApplicationException(404);
      }

      LOGGER.debug("Adding experimenter to digitalObject and updating digitalObject.");
      digitalObject.addExperimenter(user);
      digitalObject = mdm.update(digitalObject);
      LOGGER.debug("DigitalObject updated.");
      return createObjectGraphStream(DigitalObjectWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectWrapper(digitalObject));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to add experimenter to digital object.", ex);
      throw new WebApplicationException(401);
    } catch (EntityNotFoundException ex) {
      LOGGER.error("Failed to add experimenter to digital object.", ex);
      throw new WebApplicationException(404);
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
  public StreamingOutput getDigitalObjectTypeIds(String groupId, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Obtaining DigitalObjectType entities.");
      List<DigitalObjectType> types = mdm.findResultList("SELECT t FROM DigitalObjectType t", first, results);
      LOGGER.debug("{} entities obtained. Returning result.", types.size());
      return createObjectGraphStream(DigitalObjectTypeWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTypeWrapper(types));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain all digital object types.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectTypeById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Obtaining DigitalObjectType for id {}.", id);
      DigitalObjectType type = mdm.find(DigitalObjectType.class, id);

      if (type == null) {
        LOGGER.error("DigitalObjectType for id " + id + " not found");
        throw new WebApplicationException(404);
      }

      LOGGER.debug("Entity obtained. Returning result.");
      return createObjectGraphStream(DigitalObjectTypeWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTypeWrapper(type));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain digital object type for id " + id + ".", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addDigitalObjectType(String groupId, String typeDomain, String identifier, int version, String description, HttpContext hc) {
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
      return createObjectGraphStream(DigitalObjectTypeWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTypeWrapper(t));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to store digital object type.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectTypeCount(String groupId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Obtaining DigitalObjectType count.");
      Long count = mdm.findSingleResult("SELECT COUNT(t) FROM DigitalObjectType t", Long.class);
      LOGGER.debug("Count with value {} obtained. Returning result.", count);
      return createObjectGraphStream(DigitalObjectTypeWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTypeWrapper(count.intValue()));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain digital object type count.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectsForDigitalObjectType(String groupId, Long id, Integer first, Integer results, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Obtaining DigitalObjectType for id {}.", id);
      DigitalObjectType type = mdm.find(DigitalObjectType.class, id);

      if (type == null) {
        LOGGER.error("DigitalObjectType for id " + id + " not found");
        throw new WebApplicationException(404);
      }
      List<DigitalObject> result = DigitalObjectTypeQueryHelper.getDigitalObjectsByDigitalObjectType(type, ctx);
      LOGGER.debug("Obtained {} digital objects associated with object type  with id {}. Returning result.", result.size(), id);
      return createObjectGraphStream(DigitalObjectWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectWrapper(result));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain digital objects for type with id " + id + ".", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectCountForType(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Obtaining DigitalObjectType for id {}.", id);
      DigitalObjectType type = mdm.find(DigitalObjectType.class, id);

      if (type == null) {
        LOGGER.error("DigitalObjectType for id " + id + " not found");
        throw new WebApplicationException(404);
      }
      Long count = DigitalObjectTypeQueryHelper.getDigitalObjectsByDigitalObjectTypeCount(type, ctx);
      LOGGER.debug("Obtained mapping count of {}. Returning result.", count);
      return createObjectGraphStream(DigitalObjectWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectWrapper(count.intValue()));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain digital object count for type with id " + id + ".", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectTypesForDigitalObject(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Obtaining DigitalObject for id {}.", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);

      if (object == null) {
        LOGGER.error("DigitalObject for id " + id + " not found");
        throw new WebApplicationException(404);
      }
      LOGGER.debug("Getting DigitalObjectType entities for digital object.");
      List<DigitalObjectType> types = DigitalObjectTypeQueryHelper.getTypesOfObject(object, ctx);
      LOGGER.debug("Obtained {} type(s) for object with id {}. Returning result.", types.size(), id);
      return createObjectGraphStream(DigitalObjectTypeWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTypeWrapper(types));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain digital object types for digital object with id " + id + ".", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addDigitalObjectTypeToDigitalObject(String groupId, Long id, Long objectTypeId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Obtaining DigitalObjectType for id {}.", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);

      if (object == null) {
        LOGGER.error("DigitalObject for id " + id + " not found");
        throw new WebApplicationException(404);
      }

      DigitalObjectType type = mdm.find(DigitalObjectType.class, objectTypeId);

      if (type == null) {
        LOGGER.error("DigitalObjectType for id " + objectTypeId + " not found");
        throw new WebApplicationException(404);
      }

      ObjectTypeMapping mapping = new ObjectTypeMapping();
      mapping.setDigitalObject(object);
      mapping.setObjectType(type);
      LOGGER.debug("Writing object-type mapping to database.");
      mapping = mdm.save(mapping);

      LOGGER.debug("Stored new object-type mapping with id {}. Returning DigitalObjectType.", mapping.getId());
      return createObjectGraphStream(DigitalObjectTypeWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTypeWrapper(type));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to store object-type mapping to database.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }

  }

  @Override
  public StreamingOutput getDigitalObjectTransitionById(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Obtaining DigitalObjectTransition for id {}.", id);
      DigitalObjectTransition transition = mdm.find(DigitalObjectTransition.class, id);

      if (transition == null) {
        LOGGER.error("DigitalObjectTransition for id " + id + " not found");
        throw new WebApplicationException(404);
      }

      LOGGER.debug("Obtained DigitalObjectTransition. Returning result.");
      return createObjectGraphStream(DigitalObjectTransitionWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(transition));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain digital object transition with id " + id + ".", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectDerivationInformation(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Getting DigitalObject for id {}.", id);

      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        LOGGER.error("DigitalObject for id " + id + " not found");
        throw new WebApplicationException(404);
      }

      LOGGER.debug("DigitalObject obtained. Getting transition with object with id {} as output.", object.getBaseId());
      List<DigitalObjectTransition> transitions = TransitionQueryHelper.getTransitionsByOutputObject(object, ctx);
      LOGGER.debug("Obtained {} DigitalObjectTransition(s) where the object with the id {} is derived from. Returning result.", transitions.size(), id);
      return createObjectGraphStream(DigitalObjectTransitionWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(transitions));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain digital object transitions for object with id " + id + " as output.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput getDigitalObjectContributionInformation(String groupId, Long id, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {
      LOGGER.debug("Getting DigitalObject for id {}.", id);

      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        LOGGER.error("DigitalObject for id " + id + " not found");
        throw new WebApplicationException(404);
      }

      LOGGER.debug("DigitalObject obtained. Getting transition with object as input.");

      List<DigitalObjectTransition> transitions = TransitionQueryHelper.getTransitionsByInputObject(object, ctx);
      LOGGER.debug("Obtained {} DigitalObjectTransition(s) where the object with the id {} contributes to. Returning result.", transitions.size(), id);
      return createObjectGraphStream(DigitalObjectTransitionWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(transitions));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain digital object transitions for object with id " + id + " as output.", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }

  @Override
  public StreamingOutput addTransitionToDigitalObject(String groupId, Long id, Long otherId, String viewName, Long outputId, HttpContext hc) {
    IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
    IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
    try {

      LOGGER.debug("Getting DigitalObject for id {}.", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        LOGGER.error("DigitalObject for id " + id + " not found");
        throw new WebApplicationException(404);
      }

      DigitalObject otherObject = mdm.find(DigitalObject.class, otherId);
      if (otherObject == null) {
        LOGGER.error("DigitalObject for id " + otherId + " not found");
        throw new WebApplicationException(404);
      }
      DigitalObject input = object;
      DigitalObject output = otherObject;
      LOGGER.debug("Checking outputId argument for plausibility.");
      if (outputId == null || new Long(-1l).equals(outputId)) {
        LOGGER.debug("outputId is either null or -1. Using otherId as output object.");
      } else {
        if (!Objects.equals(outputId, id) && !Objects.equals(outputId, otherId)) {
          LOGGER.error("Transition output id " + outputId + " is not equal to one of the provided ids " + id + " and " + outputId);
          throw new WebApplicationException(401);
        } else {
          LOGGER.debug("outputId equals id. Using id as output object.");
          if (Objects.equals(outputId, id)) {
            input = otherObject;
            output = object;
          }
        }
      }

      DigitalObjectTransition t = new DigitalObjectTransition();
      t.addInputMapping(input, viewName);
      t.addOutputObject(output);
      t.setCreationTimestamp(System.currentTimeMillis());
      LOGGER.debug("Storing DigitalObjectTransition.");
      t = mdm.save(t);

      LOGGER.debug("DigitalObjectTransition stored with id {}. Returning result.", t.getId());
      return createObjectGraphStream(DigitalObjectTransitionWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DigitalObjectTransitionWrapper(t));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to add digital object transition for object with ids " + id + " and " + otherId + ".", ex);
      throw new WebApplicationException(401);
    } finally {
      mdm.close();
    }
  }
}
