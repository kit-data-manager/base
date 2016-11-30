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
package edu.kit.dama.staging.services.impl.ingest;

import edu.kit.dama.staging.interfaces.ITransferInformationPersistence;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.util.Constants;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the TransferInformationPersistence interface for data
 * ingest.
 *
 * @see edu.kit.dama.staging.interfaces.ITransferInformationPersistence
 * @author jejkal
 */
public final class IngestInformationPersistenceImpl implements ITransferInformationPersistence<INGEST_STATUS, IngestInformation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestInformationPersistenceImpl.class);

    /**
     * Default persistence unit
     */
    private final static String PERSISTENCE_UNIT_NAME = "StagingUnit";
    private String alternativePersistenceUnit = null;

    /**
     * The SINGLETON
     */
    private static IngestInformationPersistenceImpl SINGLETON = null;

    /**
     * Get the singleton instance using a specified persistence unit different
     * from the default one.
     *
     * @param pIngestUnit Alternate persistence unit.
     *
     * @return The singleton instance.
     */
    public synchronized static IngestInformationPersistenceImpl getSingleton(String pIngestUnit) {
        if (SINGLETON == null) {
            if (pIngestUnit == null) {
                SINGLETON = new IngestInformationPersistenceImpl();
            } else {
                SINGLETON = new IngestInformationPersistenceImpl(pIngestUnit);
            }
        } else if (SINGLETON.getPersistenceUnit() != null && !SINGLETON.getPersistenceUnit().equals(pIngestUnit)) {
            LOGGER.warn("ATTENTION: Current persistence unit '" + SINGLETON.getPersistenceUnit() + "' is not equal provided persistence unit '" + pIngestUnit + "'");
        }
        return SINGLETON;
    }

    /**
     * Get the singleton instance using the default persistence unit.
     *
     * @return The singleton instance.
     */
    public static synchronized IngestInformationPersistenceImpl getSingleton() {
        return getSingleton(null);
    }

    /**
     * Default constructor
     */
    IngestInformationPersistenceImpl() {
    }

    /**
     * Constructor for providing a custom persistence unit used for testing.
     *
     * @param pAltPersistenceUnit The name of the persistence unit.
     */
    IngestInformationPersistenceImpl(String pAltPersistenceUnit) {
        alternativePersistenceUnit = pAltPersistenceUnit;
    }

    /**
     * Returns the currently used persistence unit
     *
     * @return The persistence unit's name
     */
    public String getPersistenceUnit() {
        return (alternativePersistenceUnit == null) ? PERSISTENCE_UNIT_NAME : alternativePersistenceUnit;
    }

    /**
     * Create a new ingest entity.
     *
     * @param pDigitalObjectId The digital object id for which the ingest will
     * be created.
     * @param pAccessPointId The AccessPoint to use for ingesting data.
     * @param pProcessors A collection of StagingProcessor applied before/after
     * the ingest.
     * @param pSecurityContext The security context.
     *
     * @return The created ingest information.
     */
    public IngestInformation createEntity(final DigitalObjectId pDigitalObjectId, String pAccessPointId, Collection<StagingProcessor> pProcessors, IAuthorizationContext pSecurityContext) {
        if (pDigitalObjectId == null) {
            throw new IllegalArgumentException("Argument pDigitalObjectId must not be 'null'");
        }
        if (pAccessPointId == null) {
            LOGGER.warn("AccessPointId should not be null! E.g. cleanup may fail.");
        }

        if (pSecurityContext == null) {
            throw new IllegalArgumentException("Argument pSecurityContext must not be 'null'");
        } else if (pSecurityContext.getUserId() == null) {
            throw new IllegalArgumentException("Illegal security context. User must not be 'null'");
        }

        try {
            List<IngestInformation> entities = getEntitiesByDigitalObjectId(pDigitalObjectId, 0, Integer.MAX_VALUE, pSecurityContext);
            if (!entities.isEmpty()) {
                throw new PersistenceException("Obviously, there is already an entity with the digital object id '" + pDigitalObjectId + "' registed in the current context.");
            }
        } catch (PersistenceException pe) {
            //some error occured
            throw new PersistenceException("Failed to check for duplicated entities", pe);
        }
        IngestInformation result = new IngestInformation(pDigitalObjectId);
        //add staging processors if available
        if (pProcessors != null && !pProcessors.isEmpty()) {
            for (StagingProcessor processor : pProcessors) {
                result.addStagingProcessor(processor);
            }
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        try {
            result.setAccessPointId(pAccessPointId);
            result.setOwnerId(pSecurityContext.getUserId().getStringRepresentation());
            result.setGroupId(pSecurityContext.getGroupId().getStringRepresentation());
            result.setLastUpdate(System.currentTimeMillis());
            result.setExpiresAt(System.currentTimeMillis() + IngestInformation.DEFAULT_LIFETIME);
            LOGGER.debug("Persisting ingest for object id '{}'", result.getDigitalObjectId());
            result = mdm.save(result);
            LOGGER.debug("Object with id '{}' was successfully committed", result.getDigitalObjectId());
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new PersistenceException("Failed for persist ingest information for object id " + pDigitalObjectId + "'", ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public IngestInformation createEntity(final DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) {
        return createEntity(pDigitalObjectId, null, null, pSecurityContext);
    }

    /**
     * Get all ingest entities accessible by pSecurityContext.
     *
     * @param pSecurityContext The security context.
     *
     * @return All ingest entities.
     */
    public List<IngestInformation> getAllEntities(IAuthorizationContext pSecurityContext) {
        return getAllEntities(0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public List<IngestInformation> getAllEntities(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        List<IngestInformation> results = new LinkedList<>();
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        try {
            results = mdm.findResultList("SELECT d FROM IngestInformation d WHERE d.ownerUuid LIKE ?1", new Object[]{getOwnerFromContext(pSecurityContext)}, IngestInformation.class, pMinIndex, pMaxResults);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain entities using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return results;
    }

    @Override
    public Number getAllEntitiesCount(IAuthorizationContext pSecurityContext) {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        LOGGER.debug("Executing query for entity count");
        Number result = 0;
        try {
            result = mdm.findSingleResult("SELECT COUNT(d) FROM IngestInformation d WHERE d.ownerUuid LIKE ?1", new Object[]{getOwnerFromContext(pSecurityContext)}, Number.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get ingest count using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Get all ingest entities for the provided digital object id accessible by
     * the provided context.
     *
     * @param pDigitalObjectId The digital object id.
     * @param pSecurityContext The security context.
     *
     * @return A list of ingest information entities. Normally, the list should
     * contain exactly one entity.
     */
    public List<IngestInformation> getEntitiesByDigitalObjectId(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) {
        return getEntitiesByDigitalObjectId(pDigitalObjectId, 0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public List<IngestInformation> getEntitiesByDigitalObjectId(DigitalObjectId pDigitalObjectId, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        if (pDigitalObjectId == null) {
            throw new IllegalArgumentException("Argument pDigitalObjectId must not be 'null'");
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        List<IngestInformation> result = new LinkedList<>();
        try {
            result = mdm.findResultList("SELECT x FROM IngestInformation x WHERE x.digitalObjectUuid = ?1 AND x.ownerUuid LIKE ?2",
                    new Object[]{pDigitalObjectId.getStringRepresentation(), getOwnerFromContext(pSecurityContext)},
                    IngestInformation.class, pMinIndex, pMaxResults);
            //For each digital object we expect exactly one ingest entry. Otherwise, we quit here.
            if (result.size() > 1) {
                LOGGER.error("Query for IngestInformation with id '" + pDigitalObjectId + "' returned multiple results. This should never happen for IngestInformation entities.");
                throw new IllegalStateException("Staging service is in an illegal state. There are multiple ingest entities for object id " + pDigitalObjectId);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get ingest for object id " + pDigitalObjectId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }

        return result;
    }

    @Override
    public Number getEntitiesCountByDigitalObjectId(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) {
        //As we expect only one ingest per object we can obtain "all" results. Thus we won't need to do the error handling again. 
        return (long) getEntitiesByDigitalObjectId(pDigitalObjectId, pSecurityContext).size();
    }

    /**
     * Get all entities owned by the user identified by the provided
     * authorization context.
     *
     * @param pSecurityContext The authorization context.
     *
     * @return A list of ingest information entities.
     */
    public List<IngestInformation> getEntitiesByOwner(IAuthorizationContext pSecurityContext) {
        return getEntitiesByOwner(pSecurityContext.getUserId(), pSecurityContext);
    }

    /**
     * Get all ingest information entities by their owner.
     *
     * @param pOwner The user id of the owner.
     * @param pSecurityContext The context containing the owner information.
     *
     * @return A list of ingest information.
     */
    public List<IngestInformation> getEntitiesByOwner(UserId pOwner, IAuthorizationContext pSecurityContext) {
        return getEntitiesByOwner(pOwner, 0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public Number getEntitiesCountByOwner(UserId pOwner, IAuthorizationContext pSecurityContext) {
        UserId userId = pOwner;
        if (userId == null) {
            LOGGER.debug("Argument pUserId is 'null'. Using userId '{}' from context.", pSecurityContext.getUserId());
            userId = pSecurityContext.getUserId();
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        LOGGER.debug("Executing query for entity count by owner");
        Number result = 0;
        try {
            result = mdm.findSingleResult("SELECT COUNT(i) FROM IngestInformation i WHERE i.ownerUuid LIKE  ?1", new Object[]{userId.getStringRepresentation()}, Number.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get ingest count for owner id " + userId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Get all IngestInformation entities owned by the user specified in
     * pSecurityContext
     *
     * @param pMinIndex The index of the first element which will be returned.
     * @param pMaxResults The max amount of results beginning at pMinIndex.
     * @param pSecurityContext The security context used to check access
     * permissions for this method.
     *
     * @return The list of ingest information owned by the current user.
     */
    public List<IngestInformation> getEntitiesByOwner(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        if (pSecurityContext != null) {
            LOGGER.debug("Try getting entities for owner '{}'", pSecurityContext.getUserId());
            return getEntitiesByOwner(pSecurityContext.getUserId(), pMinIndex, pMaxResults, pSecurityContext);
        } else {
            throw new IllegalArgumentException("Argument pSecurityContext must not be 'null'");
        }
    }

    @Override
    public List<IngestInformation> getEntitiesByOwner(UserId pOwner, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        UserId userId = pOwner;
        if (userId == null) {
            LOGGER.debug("Argument pOwner is 'null'. Using userId '{}' from context.", pSecurityContext.getUserId());
            userId = pSecurityContext.getUserId();
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        List<IngestInformation> results = new LinkedList<>();
        try {
            results = mdm.findResultList("SELECT x FROM IngestInformation x WHERE x.ownerUuid = ?1", new Object[]{userId.getStringRepresentation()}, IngestInformation.class, pMinIndex, pMaxResults);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get ingests for owner id " + userId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return results;
    }

    /**
     * Get all IngestInformation entities having the provided status.
     *
     * @param pStatus The status.
     * @param pSecurityContext The security context used to check access
     * permissions for this method.
     *
     * @return The list of ingest information having the provided status.
     */
    public List<IngestInformation> getEntitiesByStatus(INGEST_STATUS pStatus, IAuthorizationContext pSecurityContext) {
        return getEntitiesByStatus(pStatus, 0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public List<IngestInformation> getEntitiesByStatus(INGEST_STATUS pStatus, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        if (pStatus == null) {
            throw new IllegalArgumentException("Argument pStatus must not be 'null'");
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        List<IngestInformation> results = new LinkedList<>();
        try {
            results = mdm.findResultList("SELECT x FROM IngestInformation x WHERE x.status = ?1 AND x.ownerUuid LIKE ?2", new Object[]{pStatus.getId(), getOwnerFromContext(pSecurityContext)}, IngestInformation.class, pMinIndex, pMaxResults);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get ingests for status " + pStatus + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return results;
    }

    @Override
    public Number getEntitiesCountByStatus(INGEST_STATUS pStatus, IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Executing query for entity count by status");
        Number result = 0;
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        try {
            result = mdm.findSingleResult("SELECT COUNT(i) FROM IngestInformation i WHERE i.status = ?1 AND x.ownerUuid LIKE ?2",
                    new Object[]{pStatus.getId(), getOwnerFromContext(pSecurityContext)}, Number.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get ingest count for status " + pStatus + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;

    }

    @Override
    public IngestInformation getEntityById(long pId, IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Executing query for ingest  entity with id {}", pId);

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        IngestInformation result = null;
        try {
            result = mdm.findSingleResult("SELECT x FROM IngestInformation x WHERE x.id = ?1 AND x.ownerUuid LIKE ?2",
                    new Object[]{pId, getOwnerFromContext(pSecurityContext)}, IngestInformation.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get ingest for id " + pId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Get all expired entities. This method is intended to be used for internal
     * management only. For user access getExpiredEntities(int pMinIndex, int
     * pMaxResults, IAuthorizationContext pSecurityContext) should be used.
     *
     * @param pSecurityContext The security context.
     *
     * @return A list of all expired entities accessible by pSecurityContext.
     */
    public List<IngestInformation> getExpiredEntities(IAuthorizationContext pSecurityContext) {
        return getExpiredEntities(0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public List<IngestInformation> getExpiredEntities(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Executing query for expired ingests.");

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        List<IngestInformation> results = new LinkedList<>();
        try {
            results = mdm.findResultList("SELECT x FROM IngestInformation x WHERE (x.expiresAt = -1 AND x.lastUpdate + ?1 < x.expiresAt) OR (x.expiresAt != -1 AND x.expiresAt < ?2) AND x.ownerUuid LIKE ?3",
                    new Object[]{IngestInformation.DEFAULT_LIFETIME, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)}, IngestInformation.class, pMinIndex, pMaxResults);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get expired ingests using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return results;
    }

    @Override
    public Number getExpiredEntitiesCount(IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Executing query for expired ingest count");
        Number result = 0;

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        try {
            result = mdm.findSingleResult("SELECT COUNT(x) FROM IngestInformation x WHERE (x.expiresAt = -1 AND x.lastUpdate + ?1 < x.expiresAt) OR (x.expiresAt != -1 AND x.expiresAt < ?2) AND x.ownerUuid LIKE ?3",
                    new Object[]{IngestInformation.DEFAULT_LIFETIME, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)}, Number.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get expired ingest count using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public int updateStatus(long pId, INGEST_STATUS pStatus, String pErrorMessage, IAuthorizationContext pSecurityContext) {
        if (pStatus == null) {
            throw new IllegalArgumentException("Argument pStatus must not be 'null'");
        }
        LOGGER.debug("Updating status of ingest {} to status {} and error message {}", pId, pStatus, pErrorMessage);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        int result = 0;
        try {
            result = mdm.performUpdate("UPDATE IngestInformation x SET x.status = ?2, x.errorMessage = ?3, x.lastUpdate = ?4 WHERE x.id = ?1 AND x.ownerUuid LIKE ?5",
                    new Object[]{pId, pStatus.getId(), pErrorMessage, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)});
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to update status for ingest with id " + pId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public int updateClientAccessUrl(long pId, String pClientAccessUrl, IAuthorizationContext pSecurityContext) {
        if (pClientAccessUrl == null) {
            throw new IllegalArgumentException("Argument pClientAccessUrl must not be 'null'");
        }

        LOGGER.debug("Updating client access url of ingest {} to {} ", pId, pClientAccessUrl);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        int result = 0;
        try {
            result = mdm.performUpdate("UPDATE IngestInformation x SET x.clientAccessUrl = ?2, x.status = ?3, x.lastUpdate = ?4 WHERE x.id = ?1 AND x.ownerUuid LIKE ?5",
                    new Object[]{pId, pClientAccessUrl, INGEST_STATUS.PRE_INGEST_SCHEDULED.getId(), System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)});
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to update client access url for ingest with id " + pId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public int updateStagingUrl(long pId, String pStagingUrl, IAuthorizationContext pSecurityContext) {
        if (pStagingUrl == null) {
            throw new IllegalArgumentException("Argument pStagingUrl must not be 'null'");
        }

        LOGGER.debug("Updating staging url of ingest {} to {} ", pId, pStagingUrl);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        int result = 0;
        try {
            result = mdm.performUpdate("UPDATE IngestInformation x SET x.stagingUrl = ?2, x.status = ?3, x.lastUpdate = ?4 WHERE x.id = ?1 AND x.ownerUuid LIKE ?5",
                    new Object[]{pId, pStagingUrl, INGEST_STATUS.PRE_INGEST_SCHEDULED.getId(), System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)});
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to update staging url for ingest with id " + pId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public int updateStorageUrl(long pId, String pStorageUrl, IAuthorizationContext pSecurityContext) {
        if (pStorageUrl == null) {
            throw new IllegalArgumentException("Argument pStorageUrl must not be 'null'");
        }

        LOGGER.debug("Updating storage url of ingest {} to {} ", pId, pStorageUrl);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        int result = 0;
        try {
            result = mdm.performUpdate("UPDATE IngestInformation x SET x.storageUrl = ?2, x.status = ?3, x.lastUpdate = ?4 WHERE x.id = ?1 AND x.ownerUuid LIKE ?5",
                    new Object[]{pId, pStorageUrl, INGEST_STATUS.INGEST_RUNNING.getId(), System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)});
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to update storage url for ingest with id " + pId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public int removeEntity(long pId, IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Removing ingest with id {}", pId);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        int result = 0;
        try {
            IngestInformation toDelete = getEntityById(pId, pSecurityContext);
            if (toDelete == null) {
                throw new EntityNotFoundException("Unable to find ingest for id " + pId + " accessible by context " + pSecurityContext);
            }
            mdm.remove(toDelete);
            result = 1;
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to remove ingest with id " + pId + " using context " + pSecurityContext, ex);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("No ingest with id " + pId + " found using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Merge the entity's state in the database with state of the provided
     * entity. This method should only be available for <u>internal use</u>, as
     * illegal states may be produced due to wrong usage (e.g. duplicated
     * entities for one digital object if)!
     *
     * @param pEntity The entity to merge
     * @param pSecurityContext The security context used for authorization
     *
     * @return The merged entity, reloaded from the database
     */
    public IngestInformation mergeEntity(IngestInformation pEntity, IAuthorizationContext pSecurityContext) {
        if (pEntity == null) {
            throw new IllegalArgumentException("Argument pEntity must not be 'null'");
        }

        LOGGER.debug("Persisting ingest information with id {} from database.", pEntity.getId());
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        IngestInformation result = pEntity;
        try {
            result = mdm.save(pEntity);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to refresh ingest with id " + pEntity.getId() + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Returns the owner id from the current security context. If no context is
     * used, a wildcard entry will be returned and there is no user-based
     * filtering during query operations
     *
     * @param pSecurityContext The security context which contains the user id
     * @return The owner used to query for entities. If pSecurityContext belongs
     * to an administrator '%' is returned as a wildcard for all entities
     */
    private String getOwnerFromContext(IAuthorizationContext pSecurityContext) {
        String owner;
        LOGGER.debug("Getting owner from context {}", pSecurityContext);
        if (pSecurityContext == null) {
            owner = "%";
        } else if (pSecurityContext.getGroupId().getStringRepresentation().equals(Constants.SYSTEM_GROUP)
                || pSecurityContext.getUserId().getStringRepresentation().equals(Constants.SYSTEM_ADMIN)
                || pSecurityContext.getRoleRestriction().equals(Role.ADMINISTRATOR)) {
            //return wildcard entry for administrators
            owner = "%";
        } else {
            owner = pSecurityContext.getUserId().getStringRepresentation();
        }
        LOGGER.debug("Obtained owner: {}", owner);
        return owner;
    }
}
