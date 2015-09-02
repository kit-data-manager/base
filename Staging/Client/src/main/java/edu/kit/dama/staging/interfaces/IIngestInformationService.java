/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jejkal
 * @param <C> An implementation of IAuthorizationContext.
 */
public interface IIngestInformationService<C> {

  /**
   * Get information about one specific ingest identified by a unique IngestID
   *
   * @param pIngestId The ID of the ingest to find.
   * @param pSecurityContext The security context which is used to authorize the
   * access.
   *
   * @return The ingest information entity associated with pIngestId.
   */
  IngestInformation getIngestInformationById(Long pIngestId, C pSecurityContext);

  /**
   * Get all ingest information associated with a specific digital object id.
   *
   * @param pDigitalObjectId The digital object id.
   * @param pSecurityContext The security context which is used to filter the
   * list of ingests.
   *
   * @return All ingest informations associated with pDigitalObjectId. For
   * ingests this list should contain one or no element.
   */
  IngestInformation getIngestInformationByDigitalObjectId(DigitalObjectId pDigitalObjectId, C pSecurityContext);

  /**
   * Get a list of ingest information associated with a specific user.
   *
   * @param pOwner The owner.
   * @param pFirstIndex The first index the query will start with.
   * @param pResults The maximum number of results returned.
   * @param pSecurityContext The security context which is used to filter the
   * list of ingests.
   *
   * @return All ingest informations accessible using pSecurityContext.
   */
  List<IngestInformation> getIngestInformationByOwner(String pOwner, int pFirstIndex, int pResults, C pSecurityContext);

  /**
   * Get the number of ingests for the provided owner.
   *
   * @param pOwner The owner.
   * @param pSecurityContext The security context which is used to filter the
   * list of ingests.
   *
   * @return The number of ingests for pOwner.
   */
  Integer getIngestInformationCountByOwner(String pOwner, C pSecurityContext);

  /**
   * Get all ingest information associated with the current security context.
   * For a standard user all ingests owned by the user will be returned, for an
   * admistrator all ingests will be returned.
   *
   * @param pFirstIndex The first index the query will start with.
   * @param pResults The maximum number of results returned.
   * @param pSecurityContext The security context which is used to filter the
   * list of ingests.
   *
   * @return All ingest informations accessible using pSecurityContext .
   */
  List<IngestInformation> getAllIngestInformation(int pFirstIndex, int pResults, C pSecurityContext);

  /**
   * Get the number of all ingest information associated with the current
   * security context.
   *
   * @param pSecurityContext The security context which is used to filter the
   * list of ingests.
   *
   * @return The number of accessible ingest entities.
   */
  Integer getAllIngestInformationCount(C pSecurityContext);

  /**
   * Get a list of ingest information associated with pSecurityContext having
   * the status pStatus. For a user all ingests will be returned that were
   * initiated by her/him.
   *
   * @param pStatusCode The status code used to query for ingest information.
   * Here we use Integer instead of INGEST_STATUS enum to be able to implement
   * this interface for remote services accessible by other programming
   * languages.
   * @param pFirstIndex The first index the query will start with.
   * @param pResults The maximum number of results returned.
   * @param pSecurityContext The security context which is used to authorize the
   * access.
   *
   * @return All ingests having status pStatus.
   */
  List<IngestInformation> getIngestInformationByStatus(Integer pStatusCode, int pFirstIndex, int pResults, C pSecurityContext);

  /**
   * Get the number of ingests for the provided status.
   *
   * @param pStatusCode The status.
   * @param pSecurityContext The security context which is used to filter the
   * list of ingests.
   *
   * @return The number of ingests with pStatusCode.
   */
  Integer getIngestInformationCountByStatus(Integer pStatusCode, C pSecurityContext);

  /**
   * Get a list of expired ingest information entities.
   *
   * @param pSecurityContext The security context which is used to authorize the
   * access.
   * @param pFirstIndex The first index the query will start with.
   * @param pResults The maximum number of results returned.
   *
   * @return All expired ingest entites accessible via pSecurityContext.
   */
  List<IngestInformation> getExpiredIngestInformation(int pFirstIndex, int pResults, C pSecurityContext);

  /**
   * Get the number of expired ingest information entities.
   *
   * @param pSecurityContext The security context which is used to authorize the
   * access.
   *
   * @return The number expired ingest information entities accessible via
   * pSecurityContext.
   */
  Integer getExpiredIngestInformationCount(C pSecurityContext);

  /**
   * Prepares an ingest. The preparation consists of the following steps: <ul>
   * <li>Check if pSecurityContext is allowed to ingest data</li> <li>Create a
   * new ingest entity and write it into the data backend</li> <li>If there is
   * already an entity for the provided object id check for the status. In case
   * of a previous error allow modification, in all other cases throw an
   * exception</li> <li>Perform all necessary ingest preparation </li> </ul>
   *
   * @param pDigitalObjectId The object ID.
   * @param pProperties Properties used to configure the access to the ingest.
   * This map contains ingest specific arguments provided by the access layer,
   * e.g. how the ingest will be performed using which protocol.
   * @param pSecurityContext The security context.
   *
   * @return The created ingest information entity.
   *
   * @throws TransferPreparationException If the transfer could not be prepared.
   */
  IngestInformation prepareIngest(DigitalObjectId pDigitalObjectId, Map<String, String> pProperties, C pSecurityContext) throws TransferPreparationException;

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
   * @return The number of affected rows.
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
   * @return The number of affected rows.
   */
  Integer updateStagingUrl(Long pId, String pStagingURL, C pSecurityContext);

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
