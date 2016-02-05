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

/**
 * Basic interface for persisting transfer information for data ingest and
 * download. This interface covers the creation of new entities, querying for
 * several conditions, the update of updateable fields and removing single
 * entities. An implementation of this interface requires two types: <ul> <li>A
 * status element, implementing the ITransferStatus interface</li> <li>An entity
 * implementation, implementing the ITransferInformation interface</li> </ul>
 *
 * @author jejkal
 */
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.commons.types.DigitalObjectId;
import java.util.List;

public interface ITransferInformationPersistence<C extends ITransferStatus, D extends ITransferInformation<C>> {

  /**
   * **************************************
   * CREATE *******************************
   * **************************************
   */
  /**
   * Create and return a new entity. This call may throw any runtime exception,
   * e.g. PersistenceException if persisting the entity fails or
   * IllegalArgumentException if the digital object id is invalid.
   *
   * @param pDigitalObjectId The digital object id the new entity will be
   * associated with.
   * @param pSecurityContext The security context.
   *
   * @return The created entity.
   */
  D createEntity(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext);

  /**
   * **************************************
   * READ *********************************
   * **************************************
   */
  /**
   * Get the entity for the id pId.
   *
   * @param pId The unique entity id.
   * @param pSecurityContext The security context used to check access
   * permissions for this method.
   *
   * @return The entity with the id pId or null, if there is no entity with this
   * id.
   */
  D getEntityById(long pId, IAuthorizationContext pSecurityContext);

  /**
   * Get all entities associated with a digital object id. The list may contain
   * exactly one entity (data ingest) or multiple entities (data download)
   * accessible by the context pSecurityContext.
   *
   * @param pDigitalObjectId The digital object id used to query for entities.
   * @param pMinIndex The first index that will be returned. Using an index
   * smaller or equal 0, we'll start with the first result.
   * @param pMaxResults The max. number of results starting at index pIndex.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return A list of entities. In case of transfer operations there should be
   * only one result, for download there might be more.
   */
  List<D> getEntitiesByDigitalObjectId(DigitalObjectId pDigitalObjectId, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext);

  /**
   * Get the number of entities for a specific digital object id. In case of an
   * ingest, this method should return 0 or 1, in case of a download any value
   * larger than 0 are possible.
   *
   * @param pDigitalObjectId The digital object id used to query for entities.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The number of entities associated with the provided digital object
   * id.
   */
  Number getEntitiesCountByDigitalObjectId(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext);

  /**
   * Get all entities having the status pStatus.
   *
   * @param pStatus The status code used to query for entities.
   * @param pMinIndex The first index that will be returned. Using an index
   * smaller or equal 0, we'll start with the first result.
   * @param pMaxResults The max. number of results starting at index pIndex.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return A list of entities with status pStatus.
   */
  List<D> getEntitiesByStatus(C pStatus, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext);

  /**
   * Get the number of entities with a given status.
   *
   * @param pStatus The entity status.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The number of entities with status pStatus.
   */
  Number getEntitiesCountByStatus(C pStatus, IAuthorizationContext pSecurityContext);

  /**
   * Get all entities owned by the user with the id pUserId. This method is
   * intended to be used for administrative purposes. By default, the user id of
   * the authorization context is used to determine the owner.
   *
   * @param pUserId The user id used to query for entities.
   * @param pMinIndex The first index that will be returned. Using an index
   * smaller or equal 0, we'll start with the first result..
   * @param pMaxResults The max. number of results starting at index pIndex.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return A list of entities.
   */
  List<D> getEntitiesByOwner(UserId pUserId, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext);

  /**
   * Get the number of entities for a provided owner id. This method is intended
   * to be used for administrative purposes. By default, the user id of the
   * authorization context is used to determine the owner.
   *
   *
   * @param pUserId The owner id.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The number of entities belonging to pUserId.
   */
  Number getEntitiesCountByOwner(UserId pUserId, IAuthorizationContext pSecurityContext);

  /**
   * Get all entities accessible by pSecurityContext.
   *
   * @param pMinIndex The first index that will be returned. Using an index
   * smaller or equal 0, we'll start with the first result.
   * @param pMaxResults The max. number of results starting at index pIndex.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return A list of all accessible entities.
   */
  List<D> getAllEntities(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext);

  /**
   * Get the number of all entities accessible by pSecurityContext.
   *
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The number of all entities.
   */
  Number getAllEntitiesCount(IAuthorizationContext pSecurityContext);

  /**
   * Get all entities that have expired (either because the expired field is set
   * to a concrete timestamp or the lastModification was too long ago).
   *
   * @param pMinIndex The first index that will be returned. Using an index
   * smaller or equal 0, we'll start with the first result.
   * @param pMaxResults The max. number of results starting at index pIndex.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return A list of expired entities.
   */
  List<D> getExpiredEntities(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext);

  /**
   * Get the number of entities that have expired (either because the expired
   * field is set to a concrete timestamp or the lastModification was too long
   * ago).
   *
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The amount of expired entities.
   */
  Number getExpiredEntitiesCount(IAuthorizationContext pSecurityContext);

  /**
   * **************************************
   * UPDATE *******************************
   * **************************************
   */
  /**
   * Update the status for the entity with id 'pId'. In case of an error there
   * can be provided an error message, too.
   *
   * @param pId The id of the transfer to update.
   * @param pStatus The new status for the transfer.
   * @param pErrorMessage An optional human readable error message.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The number of affected rows.
   */
  int updateStatus(long pId, C pStatus, String pErrorMessage, IAuthorizationContext pSecurityContext);

  /**
   * Update the client access URL for the entity with the id 'pId'. The client
   * access URL either points to an URL where a transfer client was generated if
   * only a dedicated transfer client can be used to access data, or to a
   * location where the data can be accessed directly by the appropriate
   * protocol. This call should be associated with a status change, e.g., in
   * case of data ingest the status should switch to PRE_INGEST_SCHEDULED
   *
   * @param pId The id of the transfer to update.
   * @param pAccessURL The URL were an access client can be downloaded or at
   * which the data can be accessed directly.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The number of affected rows.
   */
  int updateClientAccessUrl(long pId, String pAccessURL, IAuthorizationContext pSecurityContext);

  /**
   * Update the staging URL for the entity with the id 'pId'. The staging URL
   * points to the location were the data will be located physically. The access
   * can be done via the access URL (getAccessURL) either manually or by using a
   * transfer client. This call should be associated with a status change, e.g.,
   * in case of data ingest the status should switch to PRE_INGEST_SCHEDULED.
   *
   * @param pId The id of the transfer to update.
   * @param pStagingURL The URL where the data is located.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The number of affected rows.
   */
  int updateStagingUrl(long pId, String pStagingURL, IAuthorizationContext pSecurityContext);

  /**
   * Update the storage URL for the entity with the id 'pId'. The storage URL
   * points to the location were the data will be stored during the final
   * staging step. This method will only be available for the ingest as for the
   * download case the storage URL won't be changed or used. This method is
   * basically intended to be used for monitoring and rollback.
   *
   * @param pId The id of the transfer to update.
   * @param pStorageURL The URL where the data will be stored.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The number of affected rows.
   */
  int updateStorageUrl(long pId, String pStorageURL, IAuthorizationContext pSecurityContext);

  /**
   * **************************************
   * DELETE *******************************
   * **************************************
   */
  /**
   * Remove the entity with the id 'pId'. This method is for admistrative
   * purposes, e.g. to remove transfer entities that have expired.
   *
   * @param pId The id of the transfer to remove.
   * @param pSecurityContext The security context used to check access
   * permissions for this method and the ownership of the entity.
   *
   * @return The number of affected rows.
   */
  int removeEntity(long pId, IAuthorizationContext pSecurityContext);
}
