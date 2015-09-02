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
package edu.kit.dama.rest.staging.services.impl;

import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.tools.DigitalObjectSecureQueryHelper;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.staging.services.impl.download.DownloadInformationServiceLocal;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationServiceLocal;
import static edu.kit.dama.rest.util.RestUtils.authorize;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.staging.entities.AttributeImpl;
import edu.kit.dama.staging.entities.DataOrganizationNodeImpl;
import edu.kit.dama.staging.entities.FileNodeImpl;
import edu.kit.dama.staging.entities.LFNImpl;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.rest.staging.types.StagingAccessPointConfigurationWrapper;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.rest.staging.types.DownloadInformationWrapper;
import edu.kit.dama.rest.staging.services.interfaces.IStagingService;
import edu.kit.dama.rest.staging.types.IngestInformationWrapper;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.staging.util.StagingConfigurationManager;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the IStagingService interface as ReST service.
 *
 * @author jejkal
 */
@Path("/")
public class StagingInformationServiceImpl implements IStagingService {

  private static final Logger LOGGER = LoggerFactory.getLogger(StagingInformationServiceImpl.class);

  /**
   * Initialize staging in a static way in order to have it available with the
   * first call to the service.
   */
  static {
    LOGGER.info("Configuring staging.");
    String restServiceURL = StagingConfigurationManager.getSingleton().getRestServiceUrl();
    LOGGER.info("Staging successfully configured at service URL {}", restServiceURL);
  }

  @Override
  public Response checkService() {
    ServiceStatus status;
    try {
      LOGGER.debug("Doing service check by getting one ingest");
      List<IngestInformation> ingests = IngestInformationServiceLocal.getSingleton().getAllIngestInformation(0, 1, AuthorizationContext.factorySystemContext());
      LOGGER.debug("Service check getting one ingest returned {} ingest.", ingests.size());
      status = ServiceStatus.OK;
    } catch (Throwable t) {
      LOGGER.error("Obtaining one ingest returned an error. Service status is set to ERROR", t);
      status = ServiceStatus.ERROR;
    }
    return Response.status(200).entity(new CheckServiceResponse("Staging", status)).build();
  }

  @Override
  public final StreamingOutput getIngestById(Long id, HttpContext hc) {
    final IngestInformation result = IngestInformationServiceLocal.getSingleton().getIngestInformationById(id, authorize(hc));
    return createObjectGraphStream(IngestInformationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new IngestInformationWrapper(result));
  }

  @Override
  public final StreamingOutput createIngest(String groupId, String objectId, String accessPointId, HttpContext hc) {
    TransferClientProperties props = new TransferClientProperties();
    IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
    props.setStagingAccessPointId(getAccessPointConfigurationForId(accessPointId, ctx).getUniqueIdentifier());
    LOGGER.debug("Start preparing ingest entity.");
    try {
      DigitalObjectId digitalObjectIdentifier = getObjectIdentifierForId(objectId, ctx);
      final IngestInformation result = IngestInformationServiceLocal.getSingleton().prepareIngest(digitalObjectIdentifier, props, ctx);
      return createObjectGraphStream(IngestInformationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new IngestInformationWrapper(result));
    } catch (TransferPreparationException ex) {
      throw new WebApplicationException(ex);
    }
  }

  @Override
  public StreamingOutput getIngestIds(String ownerId, String groupId, String objectId, Integer status, Integer first, Integer results, HttpContext hc) {
    final List<IngestInformation> result;

    IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));

    if (ownerId != null) {
      LOGGER.debug("Filtering ingest ids by owner {}", ownerId);
      result = IngestInformationServiceLocal.getSingleton().getIngestInformationByOwner(ownerId.trim(), first, results, ctx);
    } else if (objectId != null) {
      DigitalObjectId digitalObjectIdentifier = getObjectIdentifierForId(objectId, ctx);
      LOGGER.debug("Filtering ingest ids by digital object id {}", digitalObjectIdentifier);
      result = Arrays.asList(IngestInformationServiceLocal.getSingleton().getIngestInformationByDigitalObjectId(digitalObjectIdentifier, ctx));
    } else if (status != null && status != -1) {
      LOGGER.debug("Filtering ingest ids by status {}", status);
      result = IngestInformationServiceLocal.getSingleton().getIngestInformationByStatus(status, first, results, ctx);
    } else {
      //all arguments are null...return all entities
      LOGGER.debug("Returning all ingest ids without filtering.");
      result = IngestInformationServiceLocal.getSingleton().getAllIngestInformation(first, results, ctx);
    }

    return createObjectGraphStream(IngestInformationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new IngestInformationWrapper(result));
  }

  @Override
  public final StreamingOutput getIngestsCount(String ownerId, String groupId, Integer status, HttpContext hc) {
    final Number result;
    IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
    if (ownerId != null) {
      LOGGER.debug("Obtaining ingest information count for owner {}", ownerId);
      result = IngestInformationServiceLocal.getSingleton().getIngestInformationCountByOwner(ownerId.trim(), ctx);
    } else if (status != null && status != -1) {
      LOGGER.debug("Obtaining ingest information count for status {}", status);
      result = IngestInformationServiceLocal.getSingleton().getIngestInformationCountByStatus(status, ctx);
    } else {
      //all arguments are null...return all entities
      LOGGER.debug("Obtaining ingest information count without filtering");
      result = IngestInformationServiceLocal.getSingleton().getAllIngestInformationCount(ctx);
    }

    LOGGER.debug("Obtained ingest information count is '{}'", result);
    if (result != null) {
      return createObjectGraphStream(IngestInformationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new IngestInformationWrapper(result.intValue()));
    } else {
      return createObjectGraphStream(IngestInformationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new IngestInformationWrapper());
    }
  }

  @Override
  public final Response updateIngestById(Long id, Integer status, String errorMessage, HttpContext hc) {
    Integer result = IngestInformationServiceLocal.getSingleton().updateStatus(id, status, errorMessage, authorize(hc));
    if (result == 1) {
      //update, ok.
      return Response.ok().build();
    } else {
      //no update, resource not found?!
      return Response.status(404).build();
    }
  }

  @Override
  public Response deleteIngestById(Long id, HttpContext hc) {
    Integer result = IngestInformationServiceLocal.getSingleton().removeEntity(id, authorize(hc));

    if (result == 0) {
      LOGGER.debug("Deletion of ingest with id {} returned 0 changed rows. Probably, this ingest was deleted before.", id);
    }
    //success
    return Response.ok().build();
  }

  @Override
  public final StreamingOutput getExpiredIngests(String groupId, Integer first, Integer results, HttpContext hc) {
    final List<IngestInformation> result = IngestInformationServiceLocal.getSingleton().getExpiredIngestInformation(first, results, authorize(hc, new GroupId(groupId)));
    return createObjectGraphStream(IngestInformationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new IngestInformationWrapper(result));
  }

  @Override
  public final StreamingOutput getExpiredIngestsCount(String groupId, HttpContext hc) {
    return createObjectGraphStream(IngestInformationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new IngestInformationWrapper(IngestInformationServiceLocal.getSingleton().getExpiredIngestInformationCount(authorize(hc, new GroupId(groupId)))));
  }

  @Override
  public final StreamingOutput getDownloadIds(String ownerId, String groupId, String objectId, Integer status, Integer first, Integer results, HttpContext hc) {
    final List<DownloadInformation> result;
    IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
    if (ownerId != null) {
      LOGGER.debug("Filtering download ids by owner {}", ownerId);
      result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationByOwner(ownerId.trim(), first, results, ctx);
    } else if (objectId != null) {
      LOGGER.debug("Filtering download ids by digital object id {}", objectId);
      DigitalObjectId digitalObjectIdentifier = getObjectIdentifierForId(objectId, ctx);
      result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationByDigitalObjectId(digitalObjectIdentifier, first, results, ctx);
    } else if (status != null && status != -1) {
      LOGGER.debug("Filtering download ids by status {}", status);
      result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationByStatus(status, first, results, ctx);
    } else {
      //all arguments are null...return all entities
      LOGGER.debug("Returning all ingest ids without filtering.");
      result = DownloadInformationServiceLocal.getSingleton().getAllDownloadInformation(first, results, ctx);
    }

    return createObjectGraphStream(DownloadInformationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DownloadInformationWrapper(result));
  }

  @Override
  public final StreamingOutput createDownload(String groupId, String objectId, String accessPointId, HttpContext hc) {
    TransferClientProperties props = new TransferClientProperties();
    IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
    props.setStagingAccessPointId(getAccessPointConfigurationForId(accessPointId, ctx).getUniqueIdentifier());
    try {
      LOGGER.debug("Obtaining digital object for download.");
      DigitalObjectId digitalObjectIdentifier = getObjectIdentifierForId(objectId, ctx);
      LOGGER.debug("Calling scheduleDownload()");
      final DownloadInformation result = DownloadInformationServiceLocal.getSingleton().scheduleDownload(digitalObjectIdentifier, props, ctx);
      LOGGER.debug("Creating stream from result {}", result);
      return createObjectGraphStream(DownloadInformationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DownloadInformationWrapper(result));
    } catch (TransferPreparationException ex) {
      LOGGER.error("Failed to obtain result.", ex);
      throw new WebApplicationException(ex);
    }
  }

  @Override
  public final StreamingOutput getDownloadsCount(String ownerId, String groupId, Integer status, HttpContext hc) {
    final Number result;
    IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
    if (ownerId != null) {
      LOGGER.debug("Obtaining download information count for owner {}", ownerId);
      result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationCountByOwner(ownerId, ctx);
    } else if (status != null && status != -1) {
      LOGGER.debug("Obtaining download information count for status {}", status);
      result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationCountByStatus(status, ctx);
    } else {
      //all arguments are null...return all entities
      LOGGER.debug("Obtaining download information count without filtering");
      result = DownloadInformationServiceLocal.getSingleton().getAllDownloadInformationCount(ctx);
    }

    LOGGER.debug("Obtained ingest information count is '{}'", result);
    if (result != null) {
      return createObjectGraphStream(DownloadInformationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DownloadInformationWrapper(result.intValue()));
    } else {
      return createObjectGraphStream(DownloadInformationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DownloadInformationWrapper());
    }
  }

  @Override
  public StreamingOutput getDownloadById(Long id, HttpContext hc) {
    final DownloadInformation result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationById(id, authorize(hc));
    return createObjectGraphStream(DownloadInformationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DownloadInformationWrapper(result));
  }

  @Override
  public final Response updateDownloadById(Long id, Integer status, String errorMessage, HttpContext hc) {
    Integer result = DownloadInformationServiceLocal.getSingleton().updateStatus(id, status, errorMessage, authorize(hc));
    if (result == 1) {
      //update, ok.
      return Response.ok().build();
    } else {
      //no update, resource not found?!
      return Response.status(404).build();
    }
  }

  @Override
  public final Response deleteDownloadById(Long id, HttpContext hc) {
    Integer result = DownloadInformationServiceLocal.getSingleton().removeEntity(id, authorize(hc));

    if (result == 0) {
      LOGGER.debug("Deletion of download with id {} returned 0 changed rows. Probably, this download was deleted before.", id);
    }
    //success
    return Response.ok().build();
  }

  @Override
  public final StreamingOutput getExpiredDownloads(String ownerId, String groupId, Integer first, Integer results, HttpContext hc) {
    final List<DownloadInformation> result = DownloadInformationServiceLocal.getSingleton().getExpiredDownloadInformation(first, results, authorize(hc, new GroupId(groupId)));
    return createObjectGraphStream(DownloadInformationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new DownloadInformationWrapper(result));
  }

  @Override
  public final StreamingOutput getExpiredDownloadsCount(String ownerId, String groupId, HttpContext hc) {
    return createObjectGraphStream(DownloadInformationWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new DownloadInformationWrapper(DownloadInformationServiceLocal.getSingleton().getExpiredDownloadInformationCount(authorize(hc, new GroupId(groupId)))));
  }

  @Override
  public final StreamingOutput getDownloadContainer(Long pId, HttpContext hc) {
    LOGGER.debug("Obtaining download information for id {}", pId);
    IAuthorizationContext ctx = authorize(hc);
    DownloadInformation result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationById(pId, ctx);
    final TransferTaskContainer container;
    try {
      URL stagingUrl = new URL(result.getStagingUrl());
      LOGGER.debug("Obtaining base staging path for URL '{}'", stagingUrl.toString());

      AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(result.getAccessPointId());
      File localStagingPath = accessPoint.getLocalPathForUrl(stagingUrl, ctx);

      IFileTree accessibleTree = DataOrganizationUtils.createTreeFromFile(result.getDigitalObjectId(), new AbstractFile(localStagingPath), stagingUrl, false);
      LOGGER.debug(" - Creating transfer task container");
      container = TransferTaskContainer.factoryDownloadContainer(pId, accessibleTree, StagingConfigurationManager.getSingleton().getRestServiceUrl());
    } catch (MalformedURLException ex) {
      LOGGER.error("Failed to parse staging URL.", ex);
      throw new WebApplicationException(ex, 404);
    } catch (IOException ex) {
      LOGGER.error("Failed to create transfer container", ex);
      throw new WebApplicationException(ex, 404);
    }

    return createObjectGraphStream(new Class[]{
      LFNImpl.class,
      URL.class,
      TransferTaskContainer.class,
      DataOrganizationNodeImpl.class,
      FileNodeImpl.class,
      AttributeImpl.class
    }, null, container);
  }

  @Override
  public StreamingOutput getStagingAccessPoints(String pUniqueIdentifier, String pGroupId, HttpContext hc) {
    List<StagingAccessPointConfiguration> results;
    //groupId will never be null...if nothing is provided it has the value USERS
    String stagingPU = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit");
    if (pUniqueIdentifier == null) {
      //only return access points for group
      results = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationsForGroup(pGroupId);
    } else {
      StagingAccessPointConfiguration result = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByUniqueIdentifier(pUniqueIdentifier);
      results = Arrays.asList(result);
    }

    return createObjectGraphStream(StagingAccessPointConfigurationWrapper.class, Constants.REST_SIMPLE_OBJECT_GRAPH, new StagingAccessPointConfigurationWrapper(results));
  }

  @Override
  public StreamingOutput getStagingAccessPoint(Long pId, HttpContext hc) {
    String stagingPU = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit");
    return createObjectGraphStream(StagingAccessPointConfigurationWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new StagingAccessPointConfigurationWrapper(StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationById(pId))
    );
  }

  /**
   * Get a digital object for the provided id. The id might be the string
   * representation of the numeric id or the unique identifier of the digital
   * object. This helper method will take care of the transformation.
   */
  private DigitalObjectId getObjectIdentifierForId(String pId, IAuthorizationContext pCtx) {
    if (pId == null) {
      LOGGER.error("Argument pId must not be null.");
      throw new WebApplicationException(401);
    }
    String s_id = pId.trim();
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pCtx);
    try {
      String objectId = s_id;
      try {
        LOGGER.debug("Trying to parse provided object id {} as long value", s_id);
        long id = Long.parseLong(s_id);
        LOGGER.debug("Parsing to long succeeded. Obtaining string identifiert for long id.");
        s_id = mdm.findSingleResult("SELECT o.digitalObjectIdentifier FROM DigitalObject o WHERE o.baseId=" + id, String.class);
      } catch (NumberFormatException ex) {
        LOGGER.debug("Parsing to long failed, expecting string identifier");
      }

      if (new DigitalObjectSecureQueryHelper().objectByIdentifierExists(objectId, mdm, pCtx)) {
        LOGGER.debug("Returning digital object id {}", objectId);
        return new DigitalObjectId(objectId);
      } else {
        LOGGER.error("No DigitalObject accessible by context " + pCtx + " found for provided id " + pId + ".");
        throw new WebApplicationException(404);
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Context " + pCtx.toString() + " is not authorized to access object by id " + s_id);
      throw new WebApplicationException(401);
    } catch (EntityNotFoundException ex) {
      LOGGER.error("No DigitalObject accessible by context " + pCtx + " found for provided id " + pId + ".");
      throw new WebApplicationException(404);
    } finally {
      mdm.close();
    }
  }

  /**
   * Get a digital object for the provided id. The id might be the string
   * representation of the numeric id or the unique identifier of the digital
   * object. This helper method will take care of the transformation.
   */
  private StagingAccessPointConfiguration getAccessPointConfigurationForId(String pId, IAuthorizationContext pCtx) {
    if (pId == null) {
      LOGGER.error("Argument pId must not be null.");
      throw new WebApplicationException(401);
    }
    String s_id = pId.trim();
    String stagingPU = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit");
    StagingAccessPointConfiguration result;
    try {
      LOGGER.debug("Trying to parse provided access point id {} as long value", s_id);
      long id = Long.parseLong(s_id);
      LOGGER.debug("Parsing to long succeeded. Continuing with long identifier.");
      result = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationById(id);
    } catch (NumberFormatException ex) {
      LOGGER.debug("Parsing to long failed, expecting string identifier");
      result = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByUniqueIdentifier(s_id);
    }

    if (result == null) {
      LOGGER.error("No StagingAccessPointConfiguration found for identifier " + s_id);
      throw new WebApplicationException(404);
    }

    return result;
  }

}
