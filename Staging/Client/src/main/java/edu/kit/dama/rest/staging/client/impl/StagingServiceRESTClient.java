/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
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
package edu.kit.dama.rest.staging.client.impl;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.rest.staging.types.DownloadInformationWrapper;
import edu.kit.dama.rest.staging.types.IngestInformationWrapper;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.mdm.dataorganization.impl.staging.AttributeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.DataOrganizationNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl;
import edu.kit.dama.rest.staging.types.StagingAccessPointConfigurationWrapper;
import edu.kit.dama.rest.staging.types.StagingProcessorWrapper;
import edu.kit.dama.util.Constants;
import java.net.URL;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.json.JSONArray;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Torridity
 */
public class StagingServiceRESTClient extends AbstractRestClient {

    /**
     * The logger
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StagingServiceRESTClient.class);
    //<editor-fold defaultstate="collapsed" desc="Parameter names">

    /**
     * The status of the ingest (see
     * edu.​kit.​dama.​staging.entities.​ingest.INGEST_STATUS).
     */
    protected static final String FORM_PARAMETER_STAGING_STATUS = "status";
    /**
     * The new error message.
     */
    protected static final String FORM_PARAMETER_STAGING_ERROR_MESSAGE = "errorMessage";
    /**
     * The ownerId.
     */
    protected static final String QUERY_PARAMETER_OWNER_ID = "ownerId";
    /**
     * The groupId.
     */
    protected static final String QUERY_PARAMETER_GROUP_ID = "groupId";
    /**
     * The objectId.
     */
    protected static final String QUERY_PARAMETER_OBJECT_ID = "objectId";
    /**
     * The AccessPoint.
     */
    protected static final String QUERY_PARAMETER_ACCESS_POINT = "accessPoint";

    /**
     * The StagingProcessors.
     */
    protected static final String QUERY_PARAMETER_STAGING_PROCESSORS = "stagingProcessors";

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="URL components">
    /**
     * 'url' for count.
     */
    private static final String COUNT_URL = "/count";
    /**
     * Downloads.
     */
    public static final String STAGING_ALL_DOWNLOADS = "/downloads";
    /**
     * Downloads by id.
     */
    public static final String STAGING_DOWNLOAD_BY_ID = STAGING_ALL_DOWNLOADS + "/{0}";
    /**
     * TransferTaskContainer for given id.
     */
    public static final String STAGING_DOWNLOAD_TTC = STAGING_ALL_DOWNLOADS + "/container/{0}";
    /**
     * Count of all downloads.
     */
    public static final String STAGING_DOWNLOAD_COUNT = STAGING_ALL_DOWNLOADS + COUNT_URL;
    /**
     * List of all expired downloads.
     */
    public static final String STAGING_DOWNLOAD_EXPIRED = STAGING_ALL_DOWNLOADS + "/expired";
    /**
     * Count of all expired downloads.
     */
    public static final String STAGING_DOWNLOAD_EXPIRED_COUNT = STAGING_DOWNLOAD_EXPIRED + COUNT_URL;
    /**
     * Ingests.
     */
    public static final String STAGING_ALL_INGESTS = "/ingests";
    /**
     * Ingests by id.
     */
    public static final String STAGING_INGEST_BY_ID = STAGING_ALL_INGESTS + "/{0}";
    /**
     * Count of all ingests.
     */
    public static final String STAGING_INGEST_COUNT = STAGING_ALL_INGESTS + COUNT_URL;
    /**
     * List of all expired ingests.
     */
    public static final String STAGING_INGEST_EXPIRED = STAGING_ALL_INGESTS + "/expired";
    /**
     * Count of all expired ingests.
     */
    public static final String STAGING_INGEST_EXPIRED_COUNT = STAGING_INGEST_EXPIRED + COUNT_URL;
    /**
     * AccessPoints.
     */
    public static final String STAGING_ALL_ACCESS_POINTS = "/accesspoints";
    /**
     * AccessPoints by id.
     */
    public static final String STAGING_ACCESS_POINTS_BY_ID = STAGING_ALL_ACCESS_POINTS + "/{0}";
    /**
     * The unique identifier of a staging access point.
     */
    protected static final String QUERY_PARAMETER_UNIQUE_IDENTIFIER = "uniqueIdentifier";
    /**
     * StagingProcessor.
     */
    public static final String STAGING_ALL_STAGING_PROCESSORS = "/processors";
    /**
     * StagingProcessor by id.
     */
    public static final String STAGING_PROCESSOR_BY_ID = STAGING_ALL_STAGING_PROCESSORS + "/{0}";

// </editor-fold>
    /**
     * Create a REST client with a predefined context.
     *
     * @param rootUrl root url of the staging service. (e.g.:
     * "http://dama.lsdf.kit.edu/KITDM/rest/StagingService")
     * @param pContext initial context
     */
    public StagingServiceRESTClient(String rootUrl, SimpleRESTContext pContext) {
        super(rootUrl, pContext);
    }

    // <editor-fold defaultstate="collapsed" desc="Generic Rest methods (GET, PUT, POST, DELETE)">
    /**
     * Perform a get for ingests.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return IngestInformationWrapper.
     */
    private IngestInformationWrapper performGetIngest(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(IngestInformationWrapper.class, getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a get for downloads.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return DownloadInformationWrapper.
     */
    private DownloadInformationWrapper performGetDownload(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(DownloadInformationWrapper.class, getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a get for staging access point configurations.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return StagingAccessPointConfigurationWrapper.
     */
    private StagingAccessPointConfigurationWrapper performGetStagingAccessPointConfiguration(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(StagingAccessPointConfigurationWrapper.class, getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a get for staging processors.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return StagingProcessorWrapper.
     */
    private StagingProcessorWrapper performGetStagingProcessor(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(StagingProcessorWrapper.class, getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a post for downloads.
     *
     * @param pPath url
     * @param pQueryParams query parameters
     * @param pFormParams form parameters
     * @return DownloadInformationWrapper.
     */
    private DownloadInformationWrapper performPostDownload(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(DownloadInformationWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a post for ingests.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams url parameters
     * @return IngestInformationWrapper.
     */
    private IngestInformationWrapper performPostIngest(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(IngestInformationWrapper.class, getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a post for downloads.
     *
     * @param pPath url
     * @param pFormParams url parameters
     *
     * @return A ClientResponse object which contains the response status.
     */
    private ClientResponse performPutDownload(String pPath, MultivaluedMap pFormParams) {
        return RestClientUtils.performPut(null, getWebResource(pPath), null, pFormParams);
    }

    /**
     * Perform a post for ingests.
     *
     * @param pPath url
     * @param pFormParams url parameters
     *
     * @return A ClientResponse object which contains the response status.
     */
    private ClientResponse performPutIngest(String pPath, MultivaluedMap pFormParams) {
        return RestClientUtils.performPut(null, getWebResource(pPath), null, pFormParams);
    }

    /**
     * Perform a get for TransferTaskContainer.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @return TransferTaskContainer.
     */
    private TransferTaskContainer performGetTransferTaskContainer(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(new Class[]{
            LFNImpl.class,
            URL.class,
            TransferTaskContainer.class,
            DataOrganizationNodeImpl.class,
            FileNodeImpl.class,
            AttributeImpl.class
        }, getWebResource(pPath), pQueryParams);
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="getAll[Ingests|Downloads|AccessPoints|StagingProcessors]">
    /**
     * Get all ingests.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getAllIngestInformation(int pFirstIndex, int pResults) {
        return getAllIngestInformation(null, null, -1, pFirstIndex, pResults);
    }

    /**
     * Get all ingests.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getAllIngestInformation(int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        return getAllIngestInformation(null, null, -1, pFirstIndex, pResults, pSecurityContext);
    }

    /**
     * Get all ingests.
     *
     * @param pOwnerId id of owner (null if not relevant)
     * @param pObjectId id of object (null if not relevant)
     * @param pStatus status of ingest service.
     * @param pFirstIndex index of first result
     * @param pResults no of results
     *
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getAllIngestInformation(String pOwnerId, String pObjectId, int pStatus, int pFirstIndex, int pResults) {
        return getAllIngestInformation(pOwnerId, pObjectId, pStatus, pFirstIndex, pResults, null);
    }

    /**
     * Get all ingests.
     *
     * @param pOwnerId id of owner (null if not relevant)
     * @param pObjectId id of object (null if not relevant)
     * @param pStatus status of ingest service.
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     *
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getAllIngestInformation(String pOwnerId, String pObjectId, int pStatus, int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        return getAllIngestInformation(pOwnerId, Constants.USERS_GROUP_ID, pObjectId, pStatus, pFirstIndex, pResults, pSecurityContext);
    }

    /**
     * Get all ingests.
     *
     * @param pOwnerId id of owner (null if not relevant)
     * @param pGroupId id of owner group (null if not relevant)
     * @param pObjectId id of object (null if not relevant)
     * @param pStatus status of ingest service.
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     *
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getAllIngestInformation(String pOwnerId, String pGroupId, String pObjectId, int pStatus, int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        IngestInformationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(pResults));
        if (pOwnerId != null) {
            queryParams.add(QUERY_PARAMETER_OWNER_ID, pOwnerId);
        }
        if (pGroupId != null) {
            queryParams.add(QUERY_PARAMETER_GROUP_ID, pGroupId);
        }
        if (pObjectId != null) {
            queryParams.add(QUERY_PARAMETER_OBJECT_ID, pObjectId);
        }
        if (pStatus >= 0) {
            queryParams.add(FORM_PARAMETER_STAGING_STATUS, Integer.toString(pStatus));
        }
        returnValue = performGetIngest(STAGING_ALL_INGESTS, queryParams);
        return returnValue;
    }

    /**
     * Get all available downloads.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     *
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getAllDownloadInformation(int pFirstIndex, int pResults) {
        return getAllDownloadInformation(null, null, -1, pFirstIndex, pResults);
    }

    /**
     * Get all available downloads.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getAllDownloadInformation(int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        return getAllDownloadInformation(null, null, -1, pFirstIndex, pResults, pSecurityContext);
    }

    /**
     * Get all available downloads.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param ownerId id of owner (null if not relevant)
     * @param objectId id of object (null if not relevant)
     * @param status status of ingest service.
     *
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getAllDownloadInformation(String ownerId, String objectId, int status, int pFirstIndex, int pResults) {
        return getAllDownloadInformation(ownerId, objectId, status, pFirstIndex, pResults, null);
    }

    /**
     * Get all available downloads.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pOwnerId id of owner (null if not relevant)
     * @param pObjectId id of object (null if not relevant)
     * @param pStatus status of ingest service.
     * @param pSecurityContext security context
     *
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getAllDownloadInformation(String pOwnerId, String pObjectId, int pStatus, int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        return getAllDownloadInformation(pOwnerId, Constants.USERS_GROUP_ID, pObjectId, pStatus, pFirstIndex, pResults, pSecurityContext);
    }

    /**
     * Get all available downloads.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pOwnerId id of owner (null if not relevant)
     * @param pGroupId id of owner group (null if not relevant)
     * @param pObjectId id of object (null if not relevant)
     * @param pStatus status of ingest service.
     * @param pSecurityContext security context
     *
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getAllDownloadInformation(String pOwnerId, String pGroupId, String pObjectId, int pStatus, int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        DownloadInformationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(pResults));

        if (pOwnerId != null) {
            queryParams.add(QUERY_PARAMETER_OWNER_ID, pOwnerId);
        }
        if (pGroupId != null) {
            queryParams.add(QUERY_PARAMETER_GROUP_ID, pGroupId);
        }
        if (pObjectId != null) {
            queryParams.add(QUERY_PARAMETER_OBJECT_ID, pObjectId);
        }
        if (pStatus >= 0) {
            queryParams.add(FORM_PARAMETER_STAGING_STATUS, Integer.toString(pStatus));
        }
        returnValue = performGetDownload(STAGING_ALL_DOWNLOADS, queryParams);
        return returnValue;
    }

    /**
     * Get all available access points.
     *
     * @param groupId The id of the group who is allowed to use the access
     * points returned.
     * @param pSecurityContext security context
     *
     * @return StagingAccessPointConfigurationWrapper
     */
    public StagingAccessPointConfigurationWrapper getAllAccessPoints(String groupId, SimpleRESTContext pSecurityContext) {
        return getAllAccessPoints(null, groupId, pSecurityContext);
    }

    /**
     * Get all available access points.
     *
     * @param uniqueIdentifier The unique identifier of the access points.
     * @param groupId The id of the group who is allowed to use the access
     * points returned.
     * @param pSecurityContext security context
     *
     * @return StagingAccessPointConfigurationWrapper
     */
    public StagingAccessPointConfigurationWrapper getAllAccessPoints(String uniqueIdentifier, String groupId, SimpleRESTContext pSecurityContext) {
        StagingAccessPointConfigurationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (groupId != null) {
            queryParams.add(QUERY_PARAMETER_GROUP_ID, groupId);
        }
        if (uniqueIdentifier != null) {
            queryParams.add(QUERY_PARAMETER_UNIQUE_IDENTIFIER, uniqueIdentifier);
        }

        returnValue = performGetStagingAccessPointConfiguration(STAGING_ALL_ACCESS_POINTS, queryParams);
        return returnValue;
    }

    /**
     * Get all available staging processors.
     *
     * @param groupId The id of the group who is allowed to use the staging
     * processor returned.
     * @param pSecurityContext security context
     *
     * @return StagingProcessorWrapper
     */
    public StagingProcessorWrapper getAllStagingProcessors(String groupId, SimpleRESTContext pSecurityContext) {
        StagingProcessorWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (groupId != null) {
            queryParams.add(QUERY_PARAMETER_GROUP_ID, groupId);
        }

        returnValue = performGetStagingProcessor(STAGING_ALL_STAGING_PROCESSORS, queryParams);
        return returnValue;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="get[Ingest|Download|TransferTaskContainer|AccessPoint|StagingProcessor]ById">
    /**
     * Get ingest with given id.
     *
     * @param pIngestId id of the ingest
     *
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getIngestById(long pIngestId) {
        return getIngestById(pIngestId, null);
    }

    /**
     * Get ingest with given id.
     *
     * @param pIngestId id of the ingest
     * @param pSecurityContext security context
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getIngestById(long pIngestId, SimpleRESTContext pSecurityContext) {
        IngestInformationWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        returnValue = performGetIngest(RestClientUtils.encodeUrl(STAGING_INGEST_BY_ID, pIngestId), null);
        return returnValue;
    }

    /**
     * Get download for given id.
     *
     * @param pDownloadId The download id.
     *
     * @return A DownloadInformationWrapper.
     */
    public DownloadInformationWrapper getDownloadById(long pDownloadId) {
        return getDownloadById(pDownloadId, null);
    }

    /**
     * Get download for given id.
     *
     * @param pDownloadId The download id.
     * @param pSecurityContext The security context.
     *
     * @return A DownloadInformationWrapper.
     */
    public DownloadInformationWrapper getDownloadById(long pDownloadId, SimpleRESTContext pSecurityContext) {
        DownloadInformationWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        returnValue = performGetDownload(RestClientUtils.encodeUrl(STAGING_DOWNLOAD_BY_ID, pDownloadId), null);
        return returnValue;
    }

    /**
     * Get the TransferTaskContainer for the provided download id.
     *
     * @param pId Id of the download.
     *
     * @return A TransferTaskContainer.
     */
    public TransferTaskContainer getTransferTaskContainerById(long pId) {
        return performGetTransferTaskContainer(RestClientUtils.encodeUrl(STAGING_DOWNLOAD_TTC, pId), null);
    }

    /**
     * Get the TransferTaskContainer for the provided download id.
     *
     * @param pId Id of the download.
     * @param pSecurityContext The security context.
     *
     * @return A TransferTaskContainer.
     */
    public TransferTaskContainer getTransferTaskContainerById(long pId, SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        return performGetTransferTaskContainer(RestClientUtils.encodeUrl(STAGING_DOWNLOAD_TTC, pId), null);
    }

    /**
     * Get access point by id.
     *
     * @param pId Id of the access point.
     * @param pSecurityContext security context
     *
     * @return StagingAccessPointConfigurationWrapper
     */
    public StagingAccessPointConfigurationWrapper getAccessPointById(long pId, SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);

        return performGetStagingAccessPointConfiguration(RestClientUtils.encodeUrl(STAGING_ACCESS_POINTS_BY_ID, pId), null);
    }

    /**
     * Get staging processor id.
     *
     * @param pId Id of the staging processor.
     * @param pSecurityContext security context
     *
     * @return StagingProcessorWrapper
     */
    public StagingProcessorWrapper getStagingProcessorById(long pId, SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);

        return performGetStagingProcessor(RestClientUtils.encodeUrl(STAGING_PROCESSOR_BY_ID, pId), null);
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="update[Ingest|Download]">

    /**
     * Update ingest by id.
     *
     * @param pIngestId id of ingest.
     * @param errorMessage new error message
     * @param status status of the ingest.
     *
     * @return ClientResponse which contains the HTTP response.
     */
    public ClientResponse updateIngest(long pIngestId, String errorMessage, int status) {
        return updateIngest(pIngestId, errorMessage, status, null);
    }

    /**
     * Update ingest by id.
     *
     * @param pIngestId id of ingest.
     * @param pSecurityContext security context
     * @param errorMessage new error message
     * @param status status of the ingest.
     *
     * @return ClientResponse which contains the HTTP response.
     */
    public ClientResponse updateIngest(long pIngestId, String errorMessage, int status, SimpleRESTContext pSecurityContext) {
        ClientResponse returnValue;

        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        formParams = new MultivaluedMapImpl();
        if (errorMessage != null) {
            formParams.add(QUERY_PARAMETER_OBJECT_ID, errorMessage);
        }
        formParams.add(FORM_PARAMETER_STAGING_STATUS, Integer.toString(status));

        returnValue = performPutIngest(RestClientUtils.encodeUrl(STAGING_INGEST_BY_ID, pIngestId), formParams);
        return returnValue;
    }

    /**
     * Update download for given id.
     *
     * @param pDownloadId The download id.
     * @param errorMessage The new errorMessage.
     * @param status The of the download.
     *
     * @return A ClientResponse.
     */
    public ClientResponse updateDownload(long pDownloadId, String errorMessage, int status) {
        return updateDownload(pDownloadId, errorMessage, status, null);
    }

    /**
     * Update download for given id.
     *
     * @param pDownloadId The download id.
     * @param pSecurityContext The security context.
     * @param errorMessage The new errorMessage.
     * @param status The of the download.
     *
     * @return A ClientResponse.
     */
    public ClientResponse updateDownload(long pDownloadId, String errorMessage, int status, SimpleRESTContext pSecurityContext) {
        ClientResponse returnValue;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);

        formParams = new MultivaluedMapImpl();
        if (errorMessage != null) {
            formParams.add(FORM_PARAMETER_STAGING_ERROR_MESSAGE, errorMessage);
        }
        formParams.add(FORM_PARAMETER_STAGING_STATUS, Integer.toString(status));

        returnValue = performPutDownload(RestClientUtils.encodeUrl(STAGING_DOWNLOAD_BY_ID, pDownloadId), formParams);
        return returnValue;
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="get[Ingest|Download]Count">
    /**
     * Get number of all ingests.
     *
     * @return The IngestInformationWrapper.
     */
    public IngestInformationWrapper getIngestCount() {
        return getIngestCount(null);
    }

    /**
     * Get number of all ingests.
     *
     * @param pSecurityContext security context.
     *
     * @return The IngestInformationWrapper.
     */
    public IngestInformationWrapper getIngestCount(SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        return performGetIngest(STAGING_INGEST_COUNT, null);
    }

    /**
     * Get no of available downloads.
     *
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getDownloadCount() {
        return getDownloadCount(null);
    }

    /**
     * Get no of available downloads.
     *
     * @param pSecurityContext security context
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getDownloadCount(SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        return performGetDownload(STAGING_DOWNLOAD_COUNT, null);
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="create[Ingest|Download]">
    /**
     * Create a new ingest for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     *
     * @return IngestInformationWrapper.
     */
    public IngestInformationWrapper createIngest(String objectId, String accessPoint) {
        return createIngest(objectId, accessPoint, null, null, null);
    }

    /**
     * Create a new ingest for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param stagingProcessorIds The list of staging processor ids assigned to
     * this ingest.
     *
     * @return IngestInformationWrapper.
     */
    public IngestInformationWrapper createIngest(String objectId, String accessPoint, List<Long> stagingProcessorIds) {
        return createIngest(objectId, accessPoint, stagingProcessorIds, null, null);
    }

    /**
     * Create a new ingest for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param groupId GroupId used to create the ingest.
     *
     * @return IngestInformationWrapper.
     */
    public IngestInformationWrapper createIngest(String objectId, String accessPoint, String groupId) {
        return createIngest(objectId, accessPoint, null, groupId, null);
    }

    /**
     * Create a new ingest for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param stagingProcessorIds The list of staging processor ids assigned to
     * this ingest.
     * @param groupId GroupId used to create the ingest.
     *
     * @return IngestInformationWrapper.
     */
    public IngestInformationWrapper createIngest(String objectId, String accessPoint, List<Long> stagingProcessorIds, String groupId) {
        return createIngest(objectId, accessPoint, stagingProcessorIds, groupId, null);
    }

    /**
     * Create a new ingest for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param stagingProcessorIds The list of staging processor ids assigned to
     * this ingest.
     * @param pSecurityContext The security context.
     *
     * @return IngestInformationWrapper.
     */
    public IngestInformationWrapper createIngest(String objectId, String accessPoint, List<Long> stagingProcessorIds, SimpleRESTContext pSecurityContext) {
        return createIngest(objectId, accessPoint, stagingProcessorIds, null, pSecurityContext);
    }

    /**
     * Create a new ingest for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param stagingProcessorIds The list of staging processor ids assigned to
     * this ingest.
     * @param groupId GroupId used to create the ingest.
     * @param pSecurityContext The security context.
     *
     * @return IngestInformationWrapper.
     */
    public IngestInformationWrapper createIngest(String objectId, String accessPoint, List<Long> stagingProcessorIds, String groupId, SimpleRESTContext pSecurityContext) {
        IngestInformationWrapper returnValue;
        MultivaluedMap formParams;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);
        if (groupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, groupId);
        }

        if (objectId == null) {
            throw new IllegalArgumentException("Argument 'objectId' must not be null.");
        }
        if (accessPoint == null) {
            throw new IllegalArgumentException("Argument 'accessPoint' must not be null.");
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(QUERY_PARAMETER_OBJECT_ID, objectId);
        formParams.add(QUERY_PARAMETER_ACCESS_POINT, accessPoint);

        if (stagingProcessorIds != null) {
            formParams.add(QUERY_PARAMETER_STAGING_PROCESSORS, new JSONArray(stagingProcessorIds).toString());
        }

        returnValue = performPostIngest(STAGING_ALL_INGESTS, queryParams, formParams);
        return returnValue;
    }

    /**
     * Create a new download for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     *
     * @return An IngestInformationWrapper.
     */
    public DownloadInformationWrapper createDownload(String objectId, String accessPoint) {
        return createDownload(objectId, accessPoint, null, null, null);
    }

    /**
     * Create a new download for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param stagingProcessorIds The list of staging processor ids assigned to
     * this download.
     *
     * @return An IngestInformationWrapper.
     */
    public DownloadInformationWrapper createDownload(String objectId, String accessPoint, List<Long> stagingProcessorIds) {
        return createDownload(objectId, accessPoint, stagingProcessorIds, null, null);
    }

    /**
     * Create a new download for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param groupId GroupId used to create the download.
     *
     * @return An IngestInformationWrapper.
     */
    public DownloadInformationWrapper createDownload(String objectId, String accessPoint, String groupId) {
        return createDownload(objectId, accessPoint, null, groupId, null);
    }

    /**
     * Create a new download for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param stagingProcessorIds The list of staging processor ids assigned to
     * this download.
     * @param groupId GroupId used to create the download.
     *
     * @return An IngestInformationWrapper.
     */
    public DownloadInformationWrapper createDownload(String objectId, String accessPoint, List<Long> stagingProcessorIds, String groupId) {
        return createDownload(objectId, accessPoint, stagingProcessorIds, groupId, null);
    }

    /**
     * Create a new download for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param pSecurityContext The security context.
     *
     * @return IngestInformationWrapper.
     */
    public DownloadInformationWrapper createDownload(String objectId, String accessPoint, SimpleRESTContext pSecurityContext) {
        return createDownload(objectId, accessPoint, null, null, pSecurityContext);
    }

    /**
     * Create a new download for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param stagingProcessorIds The list of staging processor ids assigned to
     * this download.
     * @param pSecurityContext The security context.
     *
     * @return IngestInformationWrapper.
     */
    public DownloadInformationWrapper createDownload(String objectId, String accessPoint, List<Long> stagingProcessorIds, SimpleRESTContext pSecurityContext) {
        return createDownload(objectId, accessPoint, stagingProcessorIds, null, pSecurityContext);
    }

    /**
     * Create a new download for given object id and given AccessPoint.
     *
     * @param objectId id of the object.
     * @param accessPoint AccessPoint for transfer.
     * @param stagingProcessorIds The list of staging processor ids assigned to
     * this download.
     * @param groupId GroupId used to create the download.
     * @param pSecurityContext The security context.
     *
     * @return An IngestInformationWrapper.
     */
    public DownloadInformationWrapper createDownload(String objectId, String accessPoint, List<Long> stagingProcessorIds, String groupId, SimpleRESTContext pSecurityContext) {
        DownloadInformationWrapper returnValue;
        MultivaluedMap formParams;
        MultivaluedMap queryParams = null;
        setFilterFromContext(pSecurityContext);

        if (groupId != null) {
            queryParams = new MultivaluedMapImpl();
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, groupId);
        }

        if (objectId == null) {
            throw new IllegalArgumentException("Argument 'objectId' must not be null.");
        }
        if (accessPoint == null) {
            throw new IllegalArgumentException("Argument 'accessPoint' must not be null.");
        }

        formParams = new MultivaluedMapImpl();
        formParams.add(QUERY_PARAMETER_OBJECT_ID, objectId);
        formParams.add(QUERY_PARAMETER_ACCESS_POINT, accessPoint);

        if (stagingProcessorIds != null) {
            formParams.add(QUERY_PARAMETER_STAGING_PROCESSORS, new JSONArray(stagingProcessorIds).toString());
        }

        returnValue = performPostDownload(STAGING_ALL_DOWNLOADS, queryParams, formParams);
        return returnValue;
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="getExpired[Ingests|Downloads]">
    /**
     * Get all expired ingests.
     *
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getExpiredIngests() {
        return getExpiredIngests(0, Integer.MAX_VALUE, null);
    }

    /**
     * Get all expired ingests.
     *
     * @param pSecurityContext security context
     *
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getExpiredIngests(SimpleRESTContext pSecurityContext) {
        return getExpiredIngests(0, Integer.MAX_VALUE, pSecurityContext);
    }

    /**
     * Get all expired ingests.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     *
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getExpiredIngests(int pFirstIndex, int pResults) {
        return getExpiredIngests(pFirstIndex, pResults, null);
    }

    /**
     * Get all expired ingests.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getExpiredIngests(int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        IngestInformationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(pResults));
        returnValue = performGetIngest(STAGING_ALL_INGESTS, queryParams);
        return returnValue;
    }

    /**
     * Get expired downloads.
     *
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getExpiredDownloads() {
        return getExpiredDownloads(0, Integer.MAX_VALUE, null);
    }

    /**
     * Get expired downloads.
     *
     * @param pSecurityContext security context
     *
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getExpiredDownloads(SimpleRESTContext pSecurityContext) {
        return getExpiredDownloads(0, Integer.MAX_VALUE, pSecurityContext);
    }

    /**
     * Get expired downloads.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     *
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getExpiredDownloads(int pFirstIndex, int pResults) {
        return getExpiredDownloads(pFirstIndex, pResults, null);
    }

    /**
     * Get expired downloads.
     *
     * @param pFirstIndex index of first result
     * @param pResults no of results
     * @param pSecurityContext security context
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getExpiredDownloads(int pFirstIndex, int pResults, SimpleRESTContext pSecurityContext) {
        DownloadInformationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(pResults));
        returnValue = performGetDownload(STAGING_DOWNLOAD_EXPIRED, queryParams);
        return returnValue;
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="getExpired[Ingest|Download]Count">

    /**
     * Get no of expired ingests.
     *
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getExpiredIngestCount() {
        return getExpiredIngestCount(null);
    }

    /**
     * Get no of expired ingests.
     *
     * @param pSecurityContext security context
     * @return IngestInformationWrapper
     */
    public IngestInformationWrapper getExpiredIngestCount(SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        return performGetIngest(STAGING_INGEST_EXPIRED_COUNT, null);
    }

    /**
     * Get no of expired downloads.
     *
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getExpiredDownloadCount() {
        return getExpiredDownloadCount(null);
    }

    /**
     * Get no of expired downloads.
     *
     * @param pSecurityContext security context
     * @return DownloadInformationWrapper
     */
    public DownloadInformationWrapper getExpiredDownloadCount(SimpleRESTContext pSecurityContext) {
        setFilterFromContext(pSecurityContext);
        return performGetDownload(STAGING_DOWNLOAD_EXPIRED_COUNT, null);
    }
//</editor-fold>

    /**
     * Main method for testing purposes.
     *
     * @param args arguments
     */
//  public static void main(String[] args) {
//    String accessKey = "admin";
//    String accessSecret = "dama14";
//    // Alternatively put them in the arguments!
//    if (args.length == 2) {
//      accessKey = args[0];
//      accessSecret = args[1];
//    }
//    SimpleRESTContext context = new SimpleRESTContext(accessKey, accessSecret);
//
//    //context = new SimpleRESTContext("XlSrRDAZBLCifJva", "xxmdOsBY6Jnzhs9t");
//    //for (int i = 0; i < 30; i++) {
//    //System.out.println(new BessIngestRestClient("https://dama.lsdf.kit.edu/KITDM/rest/StagingInformationService", context).produceTestEntity().getId());
//    StagingServiceRESTClient client = new StagingServiceRESTClient("http://localhost:8080/KITDM/rest/staging/", context);
//    System.out.println(client.getIngestCount().getCount());
//  }
}
