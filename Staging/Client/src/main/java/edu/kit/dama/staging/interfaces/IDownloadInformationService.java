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
package edu.kit.dama.staging.interfaces;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jejkal
 * @param <C> An implementation of IAuthorizationContext.
 */
public interface IDownloadInformationService<C> {

  /**
   * Get information about one specific download identified by a unique
   * DownloadID.
   *
   * @param pDownloadId The ID of the download to find.
   * @param pSecurityContext The security context which is used to authorize the
   * access.
   *
   * @return The download information entity associated with pDownloadId.
   */
  DownloadInformation getDownloadInformationById(Long pDownloadId, C pSecurityContext);

  /**
   * Get all download informations associated with a specific digital object id.
   * For a normal user a single entity should be returned, for administrator
   * access multiple entities might be returned.
   *
   * @param pDigitalObjectId The digital object id.
   * @param pFirstIndex The first index returned.
   * @param pResults The max. number of results.
   * @param pSecurityContext The security context which is used to filter the
   * list of downloads.
   *
   * @return All download informations associated with pDigitalObjectId.
   */
  List<DownloadInformation> getDownloadInformationByDigitalObjectId(DigitalObjectId pDigitalObjectId, int pFirstIndex, int pResults, C pSecurityContext);

  /**
   * Get the number of download informations associated with a specific digital
   * object id. For a normal user 0 or 1 should be returned, for an
   * administrator values largen than 1 might be returned.
   *
   * @param pDigitalObjectId The digital object id.
   * @param pSecurityContext The security context which is used to filter the
   * list of downloads.
   *
   * @return The number of download informations associated with
   * pDigitalObjectId.
   */
  Number getDownloadInformationCountByDigitalObjectId(DigitalObjectId pDigitalObjectId, C pSecurityContext);

  /**
   * Get a list of download information associated with a specific user.
   *
   * @param pOwner The owner.
   * @param pFirstIndex The first index the query will start with.
   * @param pResults The maximum number of results returned.
   * @param pSecurityContext The security context which is used to filter the
   * list of downloads.
   *
   * @return All download informations accessible using pSecurityContext.
   */
  List<DownloadInformation> getDownloadInformationByOwner(String pOwner, int pFirstIndex, int pResults, C pSecurityContext);

  /**
   * Get the number of downloads for the provided owner.
   *
   * @param pOwner The owner.
   * @param pSecurityContext The security context which is used to filter the
   * list of downloads.
   *
   * @return The number of downloads for pOwner.
   */
  Integer getDownloadInformationCountByOwner(String pOwner, C pSecurityContext);

  /**
   * Get all download informations associated with the current security context.
   * For a standard user all downloads owned by the user will be returned, for
   * an admistrator all downloads will be returned.
   *
   * @param pFirstIndex The first index the query will start with.
   * @param pResults The maximum number of results returned.
   * @param pSecurityContext The security context which is used to filter the
   * list of downloads.
   *
   * @return All download informations accessible using pSecurityContext.
   */
  List<DownloadInformation> getAllDownloadInformation(int pFirstIndex, int pResults, C pSecurityContext);

  /**
   * Get the number of all download informations associated with the current
   * security context.
   *
   * @param pSecurityContext The security context which is used to filter the
   * list of downloads.
   *
   * @return The number of accessible download entities.
   */
  Integer getAllDownloadInformationCount(C pSecurityContext);

  /**
   * Get a list of download information associated with pSecurityContext having
   * the status pStatus. For a user all downloads will be returned that were
   * initiated by her/him.
   *
   * @param pStatusCode The status code used to query for download information.
   * Here we use Integer instead of DOWNLOAD_STATUS enum to be able to implement
   * this interface for remote services accessible by other programming
   * languages.
   * @param pFirstIndex The first index the query will start with.
   * @param pResults The maximum number of results returned.
   * @param pSecurityContext The security context which is used to authorize the
   * access.
   *
   * @return All downloads having status pStatus.
   */
  List<DownloadInformation> getDownloadInformationByStatus(Integer pStatusCode, int pFirstIndex, int pResults, C pSecurityContext);

  /**
   * Get the number of downloads for the provided status.
   *
   * @param pStatusCode The status.
   * @param pSecurityContext The security context which is used to filter the
   * list of downloads.
   *
   * @return The number of ingests with pStatusCode.
   */
  Integer getDownloadInformationCountByStatus(Integer pStatusCode, C pSecurityContext);

  /**
   * Get a list of expired download information entities.
   *
   * @param pFirstIndex The first index the query will start with.
   * @param pResults The maximum number of results returned.
   * @param pSecurityContext The security context which is used to authorize the
   * access.
   *
   * @return All expired download entites accessible via pSecurityContext.
   */
  List<DownloadInformation> getExpiredDownloadInformation(int pFirstIndex, int pResults, C pSecurityContext);

  /**
   * Get the number of expired download information entities.
   *
   * @param pSecurityContext The security context which is used to authorize the
   * access.
   *
   * @return The number expired download information entities accessible via
   * pSecurityContext.
   */
  Integer getExpiredDownloadInformationCount(C pSecurityContext);

  /**
   * Schedules a download. Scheduling consists of the following steps: <ul>
   * <li>Check if pSecurityContext is allowed to download data</li> <li>Create a
   * new download entity and write it into the data backend</li> <li>If there is
   * already an entity for the provided object id check for the status. In case
   * of a previous error allow modification, in all other cases throw an
   * exception</li> <li>Perform all necessary steps to allow asynchronous
   * download preparation. </li> </ul>
   *
   * @param pDigitalObjectId The digital object ID.
   * @param pFileTree The tree of data organisation objects which will be
   * downloaded.
   * @param pProperties Properties used to configure the access to the download.
   * This map contains download specific arguments provided by the access layer,
   * e.g. how the download will be performed using which protocol.
   * @param pSecurityContext The security context.
   *
   *
   * @return The created download information entity.
   *
   * @throws TransferPreparationException If the transfer could not be prepared.
   */
  DownloadInformation scheduleDownload(DigitalObjectId pDigitalObjectId, IFileTree pFileTree, Map<String, String> pProperties, C pSecurityContext) throws TransferPreparationException;

  /**
   * Update the status for the entity with the provided id to pStatus and the
   * error message to pErrorMessage. If pErrorMessage is null, the message will
   * remain unchanged.
   *
   * @param pId The id of the entity to update.
   * @param pStatus The new status in its integer representation.
   * @param pErrorMessage The new error message.
   * @param pSecurityContext The authorization context.
   *
   * @return Then umber of affected rows.
   */
  Integer updateStatus(Long pId, Integer pStatus, String pErrorMessage, C pSecurityContext);

  /**
   * Update the client access URL for the entity with the provided id to
   * pAccessURL.
   *
   * @param pId The id of the entity to update.
   * @param pAccessURL The client access URL.
   * @param pSecurityContext The authorization context.
   *
   * @return The number of affected rows.
   */
  Integer updateClientAccessUrl(Long pId, String pAccessURL, C pSecurityContext);

  /**
   * Update the staging URL for the entity with the provided id to pStagingURL.
   *
   * @param pId The id of the entity to update.
   * @param pStagingURL The staging URL.
   * @param pSecurityContext The authorization context.
   *
   * @return The result which contains the number of affected rows.
   */
  Number updateStagingUrl(Long pId, String pStagingURL, C pSecurityContext);

  /**
   * Update the storage URL for the entity with the provided id to pStorageURL.
   *
   * @param pId The id of the entity to update.
   * @param pStorageURL The storage URL.
   * @param pSecurityContext The authorization context.
   *
   * @return The number of affected rows.
   */
  Integer updateStorageUrl(Long pId, String pStorageURL, C pSecurityContext);

  /**
   * Remove the entity with the provided id.
   *
   * @param pId The id of the entity to update.
   * @param pSecurityContext The authorization context.
   *
   * @return The number of affected rows.
   */
  Integer removeEntity(Long pId, C pSecurityContext);
}
