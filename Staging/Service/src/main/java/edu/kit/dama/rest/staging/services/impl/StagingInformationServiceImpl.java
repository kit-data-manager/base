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
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.mdm.tools.DigitalObjectSecureQueryHelper;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.staging.services.impl.download.DownloadInformationServiceLocal;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationServiceLocal;
import static edu.kit.dama.rest.util.RestUtils.authorize;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.rest.staging.types.StagingAccessPointConfigurationWrapper;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.rest.staging.types.DownloadInformationWrapper;
import edu.kit.dama.rest.staging.services.interfaces.IStagingService;
import edu.kit.dama.rest.staging.types.IngestInformationWrapper;
import edu.kit.dama.rest.staging.types.StagingProcessorWrapper;
import edu.kit.dama.rest.util.RestUtils;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.entities.interfaces.IDefaultDownloadInformation;
import edu.kit.dama.staging.entities.interfaces.IDefaultIngestInformation;
import edu.kit.dama.staging.entities.interfaces.IDefaultStagingAccessPointConfiguration;
import edu.kit.dama.staging.entities.interfaces.IDefaultStagingProcessor;
import edu.kit.dama.staging.entities.interfaces.IDefaultTransferTaskContainer;
import edu.kit.dama.staging.entities.interfaces.ISimpleTransferInformation;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.staging.util.StagingConfigurationManager;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the IStagingService interface as ReST service.
 *
 * @author jejkal
 */
@Path("/")
public class StagingInformationServiceImpl implements IStagingService {

    private final Class[] IMPL_CLASSES = new Class[]{IngestInformationWrapper.class, DownloadInformationWrapper.class, IngestInformation.class, DownloadInformation.class, StagingProcessor.class};
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
            LOGGER.debug("Doing service check by getting one ingest.");
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
    public IEntityWrapper<? extends IDefaultIngestInformation> getIngestById(Long id, HttpContext hc) {
        final IngestInformation result = IngestInformationServiceLocal.getSingleton().getIngestInformationById(id, authorize(hc));
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new IngestInformationWrapper(result));
    }

    @Override
    public IEntityWrapper<? extends IDefaultIngestInformation> createIngest(String groupId, String objectId, String accessPointId, String stagingProcessors, HttpContext hc) {
        TransferClientProperties props = new TransferClientProperties();
        IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
        props.setStagingAccessPointId(getAccessPointConfigurationForId(accessPointId, ctx).getUniqueIdentifier());
        if (stagingProcessors != null) {
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(StagingConfigurationPersistence.getSingleton().getPersistenceUnit());
            mdm.setAuthorizationContext(ctx);
            try {
                LOGGER.debug("Assigning staging processors to ingest.");
                try {
                    JSONArray a = new JSONArray(stagingProcessors);
                    for (int i = 0; i < a.length(); i++) {
                        Long id = a.getLong(i);
                        LOGGER.debug("Checking staging processor for id {}.", id);
                        StagingProcessor processor = mdm.find(StagingProcessor.class, id);
                        if (processor != null) {
                            LOGGER.debug("Staging processor found. Adding it to transfer.");
                            props.addProcessor(processor);
                        } else {
                            LOGGER.warn("No staging processor for id {} found. Ignoring value.", id);
                        }
                    }
                } catch (JSONException ex) {
                    LOGGER.error("Failed to deserialize json array.", ex);
                }
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Unauthorized to obtain staging processor.", ex);
            } finally {
                mdm.close();
            }
        }

        LOGGER.debug("Start preparing ingest entity.");
        try {
            DigitalObjectId digitalObjectIdentifier = getObjectIdentifierForId(objectId, ctx);
            final IngestInformation result = IngestInformationServiceLocal.getSingleton().prepareIngest(digitalObjectIdentifier, props, ctx);
            return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new IngestInformationWrapper(result));
        } catch (TransferPreparationException ex) {
            throw new WebApplicationException(ex);
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultIngestInformation> getIngests(String ownerId, String groupId, String objectId, Integer status, Integer first, Integer results, HttpContext hc) {
        final List<IngestInformation> result;
        IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));

        if (ownerId != null) {
            LOGGER.debug("Filtering ingest ids by owner {}.", ownerId);
            result = IngestInformationServiceLocal.getSingleton().getIngestInformationByOwner(ownerId.trim(), first, results, ctx);
        } else if (objectId != null) {
            DigitalObjectId digitalObjectIdentifier = getObjectIdentifierForId(objectId, ctx);
            LOGGER.debug("Filtering ingest ids by digital object id {}.", digitalObjectIdentifier);
            result = Arrays.asList(IngestInformationServiceLocal.getSingleton().getIngestInformationByDigitalObjectId(digitalObjectIdentifier, ctx));
        } else if (status != null && status != -1) {
            LOGGER.debug("Filtering ingest ids by status {}.", status);
            result = IngestInformationServiceLocal.getSingleton().getIngestInformationByStatus(status, first, results, ctx);
        } else {
            //all arguments are null...return all entities
            LOGGER.debug("Returning all ingest ids without filtering.");
            result = IngestInformationServiceLocal.getSingleton().getAllIngestInformation(first, results, ctx);
        }

        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new IngestInformationWrapper(result));
    }

    @Override
    public IEntityWrapper<? extends IDefaultIngestInformation> getIngestsCount(String ownerId, String groupId, Integer status, HttpContext hc) {
        final Number result;
        IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
        if (ownerId != null) {
            LOGGER.debug("Obtaining ingest information count for owner '{}'.", ownerId);
            result = IngestInformationServiceLocal.getSingleton().getIngestInformationCountByOwner(ownerId.trim(), ctx);
        } else if (status != null && status != -1) {
            LOGGER.debug("Obtaining ingest information count for status {}.", status);
            result = IngestInformationServiceLocal.getSingleton().getIngestInformationCountByStatus(status, ctx);
        } else {
            //all arguments are null...return all entities
            LOGGER.debug("Obtaining ingest information count without filtering.");
            result = IngestInformationServiceLocal.getSingleton().getAllIngestInformationCount(ctx);
        }

        LOGGER.debug("Obtained ingest information count is '{}'.", result);
        return new IngestInformationWrapper((result == null) ? 0 : result.intValue());
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
    public IEntityWrapper<? extends IDefaultIngestInformation> getExpiredIngests(String groupId, Integer first, Integer results, HttpContext hc) {
        final List<IngestInformation> result = IngestInformationServiceLocal.getSingleton().getExpiredIngestInformation(first, results, authorize(hc, new GroupId(groupId)));
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new IngestInformationWrapper(result));
    }

    @Override
    public IEntityWrapper<? extends IDefaultIngestInformation> getExpiredIngestsCount(String groupId, HttpContext hc) {
        Integer result = IngestInformationServiceLocal.getSingleton().getExpiredIngestInformationCount(authorize(hc, new GroupId(groupId)));
        return new IngestInformationWrapper(result);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDownloadInformation> getDownloads(String ownerId, String groupId, String objectId, Integer status, Integer first, Integer results, HttpContext hc) {
        final List<DownloadInformation> result;
        IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
        if (ownerId != null) {
            LOGGER.debug("Filtering download ids by owner '{}'.", ownerId);
            result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationByOwner(ownerId.trim(), first, results, ctx);
        } else if (objectId != null) {
            LOGGER.debug("Filtering download ids by digital object id {}.", objectId);
            DigitalObjectId digitalObjectIdentifier = getObjectIdentifierForId(objectId, ctx);
            result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationByDigitalObjectId(digitalObjectIdentifier, first, results, ctx);
        } else if (status != null && status != -1) {
            LOGGER.debug("Filtering download ids by status {}.", status);
            result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationByStatus(status, first, results, ctx);
        } else {
            //all arguments are null...return all entities
            LOGGER.debug("Returning all ingest ids without filtering.");
            result = DownloadInformationServiceLocal.getSingleton().getAllDownloadInformation(first, results, ctx);
        }
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DownloadInformationWrapper(result));
    }

    @Override
    public IEntityWrapper<? extends IDefaultDownloadInformation> createDownload(String groupId, String objectId, String accessPointId, String dataOrganizationTree, String stagingProcessors, HttpContext hc) {
        TransferClientProperties props = new TransferClientProperties();
        IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
        props.setStagingAccessPointId(getAccessPointConfigurationForId(accessPointId, ctx).getUniqueIdentifier());

        if (stagingProcessors != null) {
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(StagingConfigurationPersistence.getSingleton().getPersistenceUnit());
            mdm.setAuthorizationContext(ctx);
            try {
                LOGGER.debug("Assigning staging processors to download.");
                try {
                    JSONArray a = new JSONArray(stagingProcessors);
                    for (int i = 0; i < a.length(); i++) {
                        Long id = a.getLong(i);
                        LOGGER.debug("Checking staging processors for id {}.", id);
                        StagingProcessor processor = mdm.find(StagingProcessor.class, id);
                        if (processor != null) {
                            LOGGER.debug("Staging processors found. Assigning it to download.");
                            props.addProcessor(processor);
                        } else {
                            LOGGER.warn("No staging processor for id {} found. Ignoring value.", id);
                        }
                    }
                } catch (JSONException ex) {
                    LOGGER.error("Failed to deserialize json array.", ex);
                }
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Unauthorized to obtain staging processor.", ex);
            } finally {
                mdm.close();
            }
        }

        try {
            LOGGER.debug("Obtaining digital object for download.");
            DigitalObjectId digitalObjectIdentifier = getObjectIdentifierForId(objectId, ctx);
            IFileTree treeToDownload = null;
            if (dataOrganizationTree != null) {
                try {
                    treeToDownload = Util.jsonViewToFileTree(new JSONObject(dataOrganizationTree), false);
                    DigitalObjectId treeObjectId = treeToDownload.getDigitalObjectId();
                    if (!treeObjectId.equals(digitalObjectIdentifier)) {
                        LOGGER.error("ObjectId in tree ({}) does not fit object id for baseId {} ({}).", treeObjectId, objectId, digitalObjectIdentifier);
                        throw new WebApplicationException(Response.Status.BAD_REQUEST);
                    }

                    if (treeToDownload.getRootNode().getChildren().isEmpty()) {
                        //probably only the view name is provided to download the entire tree
                        String viewName = (treeToDownload.getViewName() != null) ? treeToDownload.getViewName() : Constants.DEFAULT_VIEW;
                        LOGGER.debug("Empty file tree detected. Loading file tree for view '{}'.", viewName);
                        treeToDownload = DataOrganizerFactory.getInstance().getDataOrganizer().loadFileTree(treeObjectId, viewName);
                    }
                } catch (InvalidNodeIdException ex) {
                    LOGGER.error("Invalid node reference in file tree.", ex);
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                } catch (JSONException ex) {
                    LOGGER.error("Failed to deserialize file tree.", ex);
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                } catch (edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException ex) {
                    LOGGER.error("Failed to load file tree for provided view name.", ex);
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
                }
            }

            LOGGER.debug("Calling scheduleDownload().");
            final DownloadInformation result = DownloadInformationServiceLocal.getSingleton().scheduleDownload(digitalObjectIdentifier, treeToDownload, props, ctx);
            LOGGER.debug("Creating stream from DownloadInformation entity with id {}.", result.getId());
            return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DownloadInformationWrapper(result));
        } catch (TransferPreparationException ex) {
            LOGGER.error("Failed to schedule download.", ex);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public IEntityWrapper<? extends ISimpleTransferInformation> getDownloadsCount(String ownerId, String groupId, Integer status, HttpContext hc) {
        final Number result;
        IAuthorizationContext ctx = authorize(hc, new GroupId(groupId));
        if (ownerId != null) {
            LOGGER.debug("Obtaining download information count for owner '{}'.", ownerId);
            result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationCountByOwner(ownerId, ctx);
        } else if (status != null && status != -1) {
            LOGGER.debug("Obtaining download information count for status {}.", status);
            result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationCountByStatus(status, ctx);
        } else {
            //all arguments are null...return all entities
            LOGGER.debug("Obtaining download information count without filtering.");
            result = DownloadInformationServiceLocal.getSingleton().getAllDownloadInformationCount(ctx);
        }

        LOGGER.debug("Obtained ingest information count is '{}'.", result);
        return new DownloadInformationWrapper((result == null) ? 0 : result.intValue());
    }

    @Override
    public IEntityWrapper<? extends IDefaultDownloadInformation> getDownloadById(Long id, HttpContext hc) {
        final DownloadInformation result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationById(id, authorize(hc));
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DownloadInformationWrapper(result));
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
    public IEntityWrapper<? extends ISimpleTransferInformation> getExpiredDownloads(String ownerId, String groupId, Integer first, Integer results, HttpContext hc) {
        final List<DownloadInformation> result = DownloadInformationServiceLocal.getSingleton().getExpiredDownloadInformation(first, results, authorize(hc, new GroupId(groupId)));
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DownloadInformationWrapper(result));
    }

    @Override
    public IEntityWrapper<? extends ISimpleTransferInformation> getExpiredDownloadsCount(String ownerId, String groupId, HttpContext hc) {
        return new DownloadInformationWrapper(DownloadInformationServiceLocal.getSingleton().getExpiredDownloadInformationCount(authorize(hc, new GroupId(groupId))));
    }

    @Override
    public IDefaultTransferTaskContainer getDownloadContainer(Long pId, HttpContext hc) {
        LOGGER.debug("Obtaining download information for id {}.", pId);
        IAuthorizationContext ctx = authorize(hc);
        DownloadInformation result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationById(pId, ctx);
        final TransferTaskContainer container;
        try {
            URL stagingUrl = new URL(result.getStagingUrl());
            LOGGER.debug("Obtaining base staging path for URL '{}'.", stagingUrl.toString());

            AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(result.getAccessPointId());
            File localStagingPath = accessPoint.getLocalPathForUrl(stagingUrl, ctx);

            IFileTree accessibleTree = DataOrganizationUtils.createTreeFromFile(result.getDigitalObjectId(), new AbstractFile(localStagingPath), stagingUrl, false);
            LOGGER.debug("Creating transfer task container.");
            container = TransferTaskContainer.factoryDownloadContainer(pId, accessibleTree, StagingConfigurationManager.getSingleton().getRestServiceUrl());
        } catch (MalformedURLException ex) {
            LOGGER.error("Failed to parse staging URL.", ex);
            throw new WebApplicationException(ex, 404);
        } catch (IOException ex) {
            LOGGER.error("Failed to create transfer task container.", ex);
            throw new WebApplicationException(ex, 404);
        }

        return container;
    }

    @Override
    public IEntityWrapper<? extends IDefaultStagingAccessPointConfiguration> getStagingAccessPoints(String pUniqueIdentifier, Boolean pIncludeDisabled, String pGroupId, HttpContext hc) {
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

        List<StagingAccessPointConfiguration> cleanedResultList = new ArrayList<>();
        if (pIncludeDisabled == null || !pIncludeDisabled) {
            for (StagingAccessPointConfiguration ap : results) {
                if (!ap.isDisabled()) {
                    cleanedResultList.add(ap);
                }
            }
            return new StagingAccessPointConfigurationWrapper(cleanedResultList);
        }

        return new StagingAccessPointConfigurationWrapper(results);
    }

    @Override
    public IEntityWrapper<? extends IDefaultStagingAccessPointConfiguration> getStagingAccessPoint(Long pId, HttpContext hc) {
        String stagingPU = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit");
        return new StagingAccessPointConfigurationWrapper(StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationById(pId));
    }

    @Override
    public IEntityWrapper<? extends IDefaultStagingProcessor> getStagingProcessors(String pGroupId, HttpContext hc) {
        String stagingPU = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit");
        return new StagingProcessorWrapper(StagingConfigurationPersistence.getSingleton(stagingPU).findStagingProcessorsForGroup(pGroupId));
    }

    @Override
    public IEntityWrapper<? extends IDefaultStagingProcessor> getStagingProcessor(@PathParam("id") Long pId, @javax.ws.rs.core.Context HttpContext hc) {
        String stagingPU = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit");
        return new StagingProcessorWrapper(StagingConfigurationPersistence.getSingleton(stagingPU).findStagingProcessorById(pId));
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
                LOGGER.debug("Trying to parse provided object id {} as long value.", s_id);
                long id = Long.parseLong(s_id);
                LOGGER.debug("Parsing to long succeeded. Obtaining string identifier for long id.");
                objectId = mdm.findSingleResult("SELECT o.digitalObjectIdentifier FROM DigitalObject o WHERE o.baseId=?1", new Object[]{id}, String.class);
                LOGGER.debug("Successfully retrieved object id for baseId {}", pId);
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
            LOGGER.error("Context " + pCtx.toString() + " is not authorized to access object with id '" + s_id + "'", ex);
            throw new WebApplicationException(401);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("No DigitalObject accessible by context " + pCtx + " found for provided id '" + pId + "'.", ex);
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
            LOGGER.debug("Trying to parse provided access point id {} as long value.", s_id);
            long id = Long.parseLong(s_id);
            LOGGER.debug("Parsing to long succeeded. Continuing with long identifier.");
            result = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationById(id);
        } catch (NumberFormatException ex) {
            LOGGER.debug("Parsing to long failed, expecting string identifier.");
            result = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByUniqueIdentifier(s_id);
        }

        if (result == null) {
            LOGGER.error("No StagingAccessPointConfiguration found for identifier '{}'.", s_id);
            throw new WebApplicationException(404);
        }

        return result;
    }
}
