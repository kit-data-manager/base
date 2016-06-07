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
package edu.kit.dama.rest.staging.services.interfaces;

import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.staging.entities.interfaces.IDefaultDownloadInformation;
import edu.kit.dama.staging.entities.interfaces.IDefaultIngestInformation;
import edu.kit.dama.staging.entities.interfaces.IDefaultStagingAccessPointConfiguration;
import edu.kit.dama.staging.entities.interfaces.IDefaultStagingProcessor;
import edu.kit.dama.staging.entities.interfaces.IDefaultTransferTaskContainer;
import edu.kit.dama.staging.entities.interfaces.ISimpleTransferInformation;
import edu.kit.dama.util.Constants;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author mf6319
 */
@Path("/rest/staging")
public interface IStagingService extends ICommonRestInterface {

    /**
     * Returns a list of accessible ingests. Without any arguments a call to
     * this method returns the first 10 (or less) ingest entries accessible by
     * the calling user. The list may be filtered by the owner of the ingest, by
     * the digital object id the ingest is associated with or by the integer
     * representation of the ingest's status. However, each of these arguments
     * must be used exclusively. E.g., if an owner id is provided, objectId and
     * status arguments are ignored. If no owner id is provided, the result can
     * be filtered by object id or by status. Available status codes are:
     *
     * <table summary="Ingest status codes">
     * <tr><th>Enum</th><th>Value</th></tr>
     * <tr><td>UNKNOWN</td><td>0</td></tr>
     * <tr><td>PREPARING</td><td>1</td></tr>
     * <tr><td>PREPARATION_FAILED</td><td>2</td></tr>
     * <tr><td>PRE_INGEST_SCHEDULED</td><td>4</td></tr>
     * <tr><td>PRE_INGEST_RUNNING</td><td>8</td></tr>
     * <tr><td>PRE_INGEST_FINISHED</td><td>16</td></tr>
     * <tr><td>PRE_INGEST_FAILED</td><td>32</td></tr>
     * <tr><td>INGEST_RUNNING</td><td>64</td></tr>
     * <tr><td>INGEST_FINISHED</td><td>128</td></tr>
     * <tr><td>INGEST_FAILED</td><td>256</td></tr>
     * <tr><td>INGEST_REMOVED</td><td>512</td></tr>
     * </table>
     *
     * @summary Get a list of accessible ingests, which may be filtered by
     * owner, digital object id and/or status.
     *
     * @param ownerId The owner id of the ingest. [default: Caller]
     * @param groupId The group id the ingest belongs to [default: USERS]
     * @param objectId The id of the digital object the ingest is associated
     * with. Valid values for this argument are either a digitalObjectIdentifier
     * (unique identifier of a digital object, e.g.
     * efb7aabd-5f35-41fc-8750-90de31b9231e) or the primary key (the database
     * primary key/the base id of the object, e.g. 4711). In future versions
     * this parameter may change to support only the numeric identifier.
     * [default: null/all objects]
     * @param status The status of the ingest (see edu.​kit.dama.​staging.
     * entities.​ingest.INGEST_STATUS).
     * @param first The first index. [default: 0]
     * @param results The max. number of results. [default: 10]
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of TransferInformation entities.
     *
     * @see edu.kit.dama.staging.entities.ingest.IngestInformation
     * @see edu.kit.dama.rest.staging.types.IngestInformationWrapper
     * @see edu.kit.dama.staging.entities.ingest.INGEST_STATUS
     */
    @GET
    @Path(value = "/ingests/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultIngestInformation>")
    IEntityWrapper<? extends IDefaultIngestInformation> getIngests(
            @QueryParam("ownerId") String ownerId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("objectId") String objectId,
            @QueryParam("status") @DefaultValue(Constants.REST_ALL_INT) Integer status,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new ingest entity via POST request. This basic interface
     * definition only allows to set the digital object id of the ingest and the
     * unique identifier of the AccessPoint to use.
     *
     * @summary Create a new ingest entity.
     *
     * @param groupId The group id the ingest belongs to [default: USERS]
     * @param objectId The object id for which an ingest should be created.
     * Valid values for this argument are either a digitalObjectIdentifier
     * (unique identifier of a digital object, e.g.
     * efb7aabd-5f35-41fc-8750-90de31b9231e) or the primary key (the database
     * primary key/the base id of the object, e.g. 4711). In future versions
     * this parameter may change to support only the numeric identifier.
     * @param accessPointId The id or unique identifier (e.g. 1 or
     * 86ad265e-ddf3-45fc-9c2e-c9c5bdb978b2) of the AccessPoint to use to
     * perform this ingest. In future versions this parameter may change to
     * support only the numeric identifier.
     * @param stagingProcessors A JSON array in the form [1,2,3] containing ids
     * of the staging processors associtated with this ingest. The content of
     * this element won't affect the assignment of staging processors marked as
     * 'default'.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created IngestInformation entity.
     *
     * @see edu.kit.dama.staging.entities.ingest.IngestInformation
     * @see edu.kit.dama.rest.staging.types.IngestInformationWrapper
     */
    @POST
    @Path(value = "/ingests/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultIngestInformation>")
    IEntityWrapper<? extends IDefaultIngestInformation> createIngest(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("objectId") String objectId,
            @FormParam("accessPoint") String accessPointId,
            @FormParam("stagingProcessors") String stagingProcessors,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all accessible ingests. The result can be filtered by owner id
     * and/or by ingest status. If no owner id is provided, the number of
     * ingests accessible by the caller of this method will be returned. If no
     * status is provided, ingests with all status codes are returned. Available
     * status codes are:
     *
     * <table summary="Ingest status codes">
     * <tr><th>Enum</th><th>Value</th></tr>
     * <tr><td>UNKNOWN</td><td>0</td></tr>
     * <tr><td>PREPARING</td><td>1</td></tr>
     * <tr><td>PREPARATION_FAILED</td><td>2</td></tr>
     * <tr><td>PRE_INGEST_SCHEDULED</td><td>4</td></tr>
     * <tr><td>PRE_INGEST_RUNNING</td><td>8</td></tr>
     * <tr><td>PRE_INGEST_FINISHED</td><td>16</td></tr>
     * <tr><td>PRE_INGEST_FAILED</td><td>32</td></tr>
     * <tr><td>INGEST_RUNNING</td><td>64</td></tr>
     * <tr><td>INGEST_FINISHED</td><td>128</td></tr>
     * <tr><td>INGEST_FAILED</td><td>256</td></tr>
     * <tr><td>INGEST_REMOVED</td><td>512</td></tr>
     * </table>
     *
     * @summary Get the count of all accessible ingests.
     *
     * @param ownerId The owner id of the ingest. [default: Caller]
     * @param groupId The group id the ingest belongs to [default: USERS]
     * @param status The status of the ingest (see edu.​kit.​dama.​staging.
     * entities.​ingest.INGEST_STATUS). [default: all]
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible ingests wrapped by an
     * IngestInformationWrapper.
     *
     * @see edu.kit.dama.staging.entities.ingest.IngestInformation
     * @see edu.kit.dama.staging.entities.ingest.INGEST_STATUS
     * @see edu.kit.dama.rest.staging.types.IngestInformationWrapper
     */
    @GET
    @Path(value = "/ingests/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultIngestInformation")
    IEntityWrapper<? extends IDefaultIngestInformation> getIngestsCount(
            @QueryParam("ownerId") String ownerId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("status") @DefaultValue(Constants.REST_ALL_INT) Integer status,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single ingest by its id.
     *
     * @summary Get an ingest by its id.
     *
     * @param id The id of the ingest to query for.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The ingest with the provided id.
     *
     * @see edu.kit.dama.staging.entities.ingest.IngestInformation
     * @see edu.kit.dama.rest.staging.types.IngestInformationWrapper
     */
    @GET
    @Path(value = "/ingests/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultIngestInformation>")
    IEntityWrapper<? extends IDefaultIngestInformation> getIngestById(
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Update the ingest with the provided id. Updates are only allowed for some
     * fields as most attributes are set internally and influence workflows and
     * data handling. Available status codes are:
     *
     * <table summary="Ingest status codes">
     * <tr><th>Enum</th><th>Value</th></tr>
     * <tr><td>UNKNOWN</td><td>0</td></tr>
     * <tr><td>PREPARING</td><td>1</td></tr>
     * <tr><td>PREPARATION_FAILED</td><td>2</td></tr>
     * <tr><td>PRE_INGEST_SCHEDULED</td><td>4</td></tr>
     * <tr><td>PRE_INGEST_RUNNING</td><td>8</td></tr>
     * <tr><td>PRE_INGEST_FINISHED</td><td>16</td></tr>
     * <tr><td>PRE_INGEST_FAILED</td><td>32</td></tr>
     * <tr><td>INGEST_RUNNING</td><td>64</td></tr>
     * <tr><td>INGEST_FINISHED</td><td>128</td></tr>
     * <tr><td>INGEST_FAILED</td><td>256</td></tr>
     * <tr><td>INGEST_REMOVED</td><td>512</td></tr>
     * </table>
     *
     * @summary Updates the ingest with the provided id.
     *
     * @param id The id of the ingest to update.
     * @param status The new status.
     * @param errorMessage The new error message.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A HTTP response with an appropriate response code.
     *
     * @see edu.kit.dama.staging.entities.ingest.IngestInformation
     * @see edu.kit.dama.staging.entities.ingest.INGEST_STATUS
     */
    @PUT
    @Path(value = "/ingests/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response updateIngestById(
            @PathParam("id") Long id,
            @FormParam("status") Integer status,
            @FormParam("errorMessage") String errorMessage,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Delete the ingest with the provided id.
     *
     * @summary Delete the ingest with the provided id.
     *
     * @param id The id of the ingest to delete.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A HTTP response with an appropriate response code.
     */
    @DELETE
    @Path(value = "/ingests/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response deleteIngestById(
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Returns a list of expired ingests. Without any arguments a call to this
     * method returns the first 10 (or less) expired ingest entries accessible
     * by the calling user in the provided groupF.
     *
     * @summary Get a list of expired ingests.
     *
     * @param groupId The group the ingests belong to. [default: USERS]
     * @param first The first index. [default: 0]
     * @param results The max. number of results. [default: 10]
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of IngestInformation entities.
     *
     * @see edu.kit.dama.staging.entities.ingest.IngestInformation
     * @see edu.kit.dama.rest.staging.types.IngestInformationWrapper
     */
    @GET
    @Path(value = "/ingests/expired")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultIngestInformation>")
    IEntityWrapper<? extends IDefaultIngestInformation> getExpiredIngests(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all expired ingests accessible by the calling user.
     *
     * @summary Get number of expired ingests.
     *
     * @param groupId The group the ingests belong to. [default: USERS]
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of expired ingests wrapped by an
     * IngestInformationWrapper.
     *
     * @see edu.kit.dama.staging.entities.ingest.IngestInformation
     * @see edu.kit.dama.rest.staging.types.IngestInformationWrapper
     */
    @GET
    @Path(value = "/ingests/expired/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.ISimpleTransferInformation>")
    IEntityWrapper<? extends ISimpleTransferInformation> getExpiredIngestsCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Returns a list of accessible downloads. Without any arguments a call to
     * this method returns the first 10 (or less) download entries accessible by
     * the calling user. The list may be filtered by the owner of the download,
     * by the digital object id the download is associated with and by the
     * integer representation of the download's status. Available status codes
     * are:
     *
     * <table summary="Download status codes">
     * <tr><th>Enum</th><th>Value</th></tr>
     * <tr><td>UNKNOWN</td><td>0</td></tr>
     * <tr><td>PREPARING</td><td>1</td></tr>
     * <tr><td>PREPARATION_FAILED</td><td>2</td></tr>
     * <tr><td>DOWNLOAD_READY</td><td>4</td></tr>
     * <tr><td>DOWNLOAD_REMOVED</td><td>8</td></tr>
     * <tr><td>SCHEDULED</td><td>16</td></tr>
     * </table>
     *
     * @summary Get a list of accessible downloads, which may be filtered by
     * owner, digital object id and/or status.
     *
     * @param ownerId The owner id of the downloads. [default: Caller]
     * @param groupId The group id the downloads belong. [default: USERS]
     * @param objectId The objectId of the download. Valid values for this
     * argument are either a digitalObjectIdentifier (unique identifier of a
     * digital object, e.g. efb7aabd-5f35-41fc-8750-90de31b9231e) or the primary
     * key (the database primary key/the base id of the object, e.g. 4711). In
     * future versions this parameter may change to support only the numeric
     * identifier. [default: null/all objects]
     * @param status The status of the download (see edu.​kit.​dama.​staging.
     * entities.​download.DOWNLOAD_STATUS). [default: all]
     * @param first The first index. [default: 0]
     * @param results The max. number of results. [default: 10]
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of DownloadInformation entities.
     *
     * @see edu.kit.dama.staging.entities.download.DownloadInformation
     * @see edu.kit.dama.rest.staging.types.DownloadInformationWrapper
     * @see edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS
     */
    @GET
    @Path(value = "/downloads/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultIngestInformation>")
    IEntityWrapper<? extends IDefaultDownloadInformation> getDownloads(
            @QueryParam("ownerId") String ownerId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("objectId") String objectId,
            @QueryParam("status") @DefaultValue(Constants.REST_ALL_INT) Integer status,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new download entity via POST request. This basic interface
     * definition only allows to set the digital object id of the download and
     * the unique identifier of the AccessPoint to use. Currently, there is no
     * way to select parts of a digital object to download.
     *
     * @summary Create a new download entity.
     *
     * @param groupId The group the download belongs to. [default: USERS]
     * @param objectId The object id for which a download should be created.
     * Valid values for this argument are either a digitalObjectIdentifier
     * (unique identifier of a digital object, e.g.
     * efb7aabd-5f35-41fc-8750-90de31b9231e) or the primary key (the database
     * primary key/the base id of the object, e.g. 4711). In future versions
     * this parameter may change to support only the numeric identifier.
     * @param accessPointId The id or unique identifier (e.g. 1 or
     * 86ad265e-ddf3-45fc-9c2e-c9c5bdb978b2) of the AccessPoint to use to
     * perform this download. In future versions this parameter may change to
     * support only the numeric identifier.
     * @param stagingProcessors A JSON array in the form [1,2,3] containing ids
     * of staging processors associtated with this download. The content of this
     * element won't affect the assignment of staging processors marked as
     * 'default'.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The created DownloadInformation entity.
     *
     * @see edu.kit.dama.staging.entities.download.DownloadInformation
     * @see edu.kit.dama.rest.staging.types.DownloadInformationWrapper
     */
    @POST
    @Path(value = "/downloads/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultDownloadInformation>")
    IEntityWrapper<? extends IDefaultDownloadInformation> createDownload(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("objectId") String objectId,
            @FormParam("accessPoint") String accessPointId,
            @FormParam("stagingProcessors") String stagingProcessors,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all accessible downloads. The result can be filtered by owner id
     * and/or by download status. If no owner id is provided, the number of
     * downloads accessible by the caller of this method will be returned. If no
     * status is provided, downloads with all status codes are returned.
     * Available status codes are:
     *
     * <table summary="Download status codes">
     * <tr><th>Enum</th><th>Value</th></tr>
     * <tr><td>UNKNOWN</td><td>0</td></tr>
     * <tr><td>PREPARING</td><td>1</td></tr>
     * <tr><td>PREPARATION_FAILED</td><td>2</td></tr>
     * <tr><td>DOWNLOAD_READY</td><td>4</td></tr>
     * <tr><td>DOWNLOAD_REMOVED</td><td>8</td></tr>
     * <tr><td>SCHEDULED</td><td>16</td></tr>
     * </table>
     *
     * @summary Get the count of all accessible downloads.
     *
     * @param ownerId The owner id of the download. [default: Caller]
     * @param groupId The group id of the download. [default: USERS]
     * @param status The status of the download (see edu.​kit.​dama.​staging.
     * entities.​download.DOWNLOAD_STATUS).
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of accessible downloads wrapped by a
     * DownloadInformationWrapper.
     *
     * @see edu.kit.dama.staging.entities.download.DownloadInformation
     * @see edu.kit.dama.rest.staging.types.DownloadInformationWrapper
     * @see edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS
     */
    @GET
    @Path(value = "/downloads/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.ISimpleTransferInformation>")
    IEntityWrapper<? extends ISimpleTransferInformation> getDownloadsCount(
            @QueryParam("ownerId") String ownerId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("status") @DefaultValue(Constants.REST_ALL_INT) Integer status,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single download by its id.
     *
     * @summary Get a download by its id.
     *
     * @param id The id of the download to query for.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The download with the provided id.
     *
     * @see edu.kit.dama.staging.entities.download.DownloadInformation
     * @see edu.kit.dama.rest.staging.types.DownloadInformationWrapper
     */
    @GET
    @Path(value = "/downloads/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultDownloadInformation>")
    IEntityWrapper<? extends IDefaultDownloadInformation> getDownloadById(
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Update the download with the provided id. Updates are only allowed for
     * some fields as most attributes are set internally and influence workflows
     * and data handling. Available status codes are:
     *
     * <table summary="Download status codes">
     * <tr><th>Enum</th><th>Value</th></tr>
     * <tr><td>UNKNOWN</td><td>0</td></tr>
     * <tr><td>PREPARING</td><td>1</td></tr>
     * <tr><td>PREPARATION_FAILED</td><td>2</td></tr>
     * <tr><td>DOWNLOAD_READY</td><td>4</td></tr>
     * <tr><td>DOWNLOAD_REMOVED</td><td>8</td></tr>
     * <tr><td>SCHEDULED</td><td>16</td></tr>
     * </table>
     *
     * @summary Updates the download with the provided id.
     *
     * @param id The id of the download to update.
     * @param status The new status.
     * @param errorMessage The new error message.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A HTTP response with an appropriate HTTP response code.
     *
     * @see edu.kit.dama.staging.entities.download.DownloadInformation
     * @see edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS
     */
    @PUT
    @Path(value = "/downloads/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response updateDownloadById(
            @PathParam("id") Long id,
            @FormParam("status") Integer status,
            @FormParam("errorMessage") String errorMessage,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Delete the download with the provided id.
     *
     * @summary Delete the download with the provided id.
     *
     * @param id The id of the download to delete.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A HTTP response with an appropriate HTTP response code.
     */
    @DELETE
    @Path(value = "/downloads/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response deleteDownloadById(
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Returns a list of expired downloads which might be filtered by the owner
     * id. Without any arguments a call to this method returns the first 10 (or
     * less) expired download entries accessible by the calling user.
     *
     * @summary Get a list of expired downloads.
     *
     * @param ownerId The owner id of the downloads. [default: Caller]
     * @param groupId The group id the downloads belong to. [default: USERS]
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of DownloadInformation entities.
     *
     * @see edu.kit.dama.staging.entities.download.DownloadInformation
     * @see edu.kit.dama.rest.staging.types.DownloadInformationWrapper
     */
    @GET
    @Path(value = "/downloads/expired")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.ISimpleTransferInformation>")
    IEntityWrapper<? extends ISimpleTransferInformation> getExpiredDownloads(
            @QueryParam("ownerId") String ownerId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Count all expired downloads accessible by the calling user or by the
     * provided owner id.
     *
     * @summary Get number of expired downloads.
     *
     * @param ownerId The owner id of the downloads. [default: Caller]
     * @param groupId The group id the downloads belong to. [default: USERS]
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of expired downloads wrapped by a
     * DownloadInformationWrapper.
     *
     * @see edu.kit.dama.staging.entities.download.DownloadInformation
     * @see edu.kit.dama.rest.staging.types.DownloadInformationWrapper
     */
    @GET
    @Path(value = "/downloads/expired/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.ISimpleTransferInformation>")
    IEntityWrapper<? extends ISimpleTransferInformation> getExpiredDownloadsCount(
            @QueryParam("ownerId") String ownerId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the transfer task container for the download with the provided id. A
     * transfer task container is available for prepared downloads. It contains
     * the configuration of the download and a tree of files prepared for
     * download. The container is generated dynamically and might be used by a
     * transfer client to access the staged files for download.
     *
     * If the download is not prepared yet, an according error (404) is thrown.
     *
     * @summary Get the transfer container for the download with the provided
     * id.
     *
     * @param pId The id of the download.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The transfer task container.
     *
     * @see edu.kit.dama.rest.staging.types.TransferTaskContainer
     */
    @GET
    @Path(value = "/downloads/container/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.staging.entities.interfaces.IDefaultTransferTaskContainer")
    IDefaultTransferTaskContainer getDownloadContainer(@PathParam("id") Long pId, @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a list of access points that can be used for data transfer from/to a
     * KIT Data Manager instance. Access points can be obtains by group or by
     * their unique identifier. If a groupId is provided, all access points
     * usable by this group are returned. If a unique identifier is provided,
     * the access point with this identifier is returned or nothing, if no
     * access point exists for this identifier. If both, unique identifier and
     * groupId are provided, the access point with the identifier usable by the
     * group or nothing are returned.
     *
     * @summary Get a list of usable access points.
     *
     * @param pUniqueIdentifier The unique identifier of an access point. If
     * this argument is provided, one or no result are returned.
     * @param pIncludeDisabled Include disabled access points (default: false)
     * @param pGroupId The id of the group to which the access points belong.
     * This argument should never be null. If no groupId is provided, the id of
     * the default group USERS will be used.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of StagingAccessPointConfiguration entities.
     *
     * @see
     * edu.kit.dama.rest.staging.types.StagingAccessPointConfigurationWrapper
     */
    @GET
    @Path(value = "/accesspoints/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultStagingAccessPointConfiguration>")
    IEntityWrapper<? extends IDefaultStagingAccessPointConfiguration> getStagingAccessPoints(
            @QueryParam("uniqueIdentifier") String pUniqueIdentifier,
            @QueryParam("includeDisabled") @DefaultValue("false") Boolean pIncludeDisabled,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId,
            @javax.ws.rs.core.Context HttpContext hc
    );

    /**
     * Get the staging access point configuration for the provided id. If no
     * access point configuration for this id exists, an empty result is
     * returned.
     *
     *
     * @summary Get the staging access point configuration for the provided id.
     *
     * @param pId The numeric id of the staging access point.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A single StagingAccessPointConfiguration entity or an empty list
     * if there is no access point for the provided id.
     */
    @GET
    @Path(value = "/accesspoints/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultStagingAccessPointConfiguration>")
    IEntityWrapper<? extends IDefaultStagingAccessPointConfiguration> getStagingAccessPoint(@PathParam("id") Long pId, @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a list of staging processors that can be used for data transfer
     * from/to a KIT Data Manager instance. Staging processors can be obtains by
     * group. If a groupId is provided, all staging processors usable by this
     * group are returned. If no groupId is provided, all processors usable by
     * the default group 'USERS' are returned.
     *
     * @summary Get a list of usable staging processors.
     *
     * @param pGroupId The id of the group to which the processor belong. If no
     * groupId is provided, the id of the default group USERS will be used.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of StagingProcessor entities.
     */
    @GET
    @Path(value = "/processors/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultStagingProcessor>")
    IEntityWrapper<? extends IDefaultStagingProcessor> getStagingProcessors(@QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String pGroupId, @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the staging processor for the provided id.
     *
     * @summary Get the staging processor for the provided id.
     *
     * @param pId The numeric id of the staging processor.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A single StagingProcessor entity or an empty list if there is no
     * processor for the provided id.
     */
    @GET
    @Path(value = "/processors/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.staging.entities.interfaces.IDefaultStagingProcessor>")
    IEntityWrapper<? extends IDefaultStagingProcessor> getStagingProcessor(@PathParam("id") Long pId, @javax.ws.rs.core.Context HttpContext hc);

}
