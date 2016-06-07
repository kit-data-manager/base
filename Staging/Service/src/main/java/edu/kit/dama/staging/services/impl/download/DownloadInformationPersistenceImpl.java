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
package edu.kit.dama.staging.services.impl.download;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.interfaces.ITransferInformationPersistence;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.util.Constants;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.PersistenceException;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the TransferInformationPersistence interface for data
 * ingest.
 *
 * @see edu.kit.dama.staging.interfaces.ITransferInformationPersistence
 * @author jejkal
 */
public final class DownloadInformationPersistenceImpl implements ITransferInformationPersistence<DOWNLOAD_STATUS, DownloadInformation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadInformationPersistenceImpl.class);
    /**
     * Default persistence unit
     */
    private final static String PERSISTENCE_UNIT_NAME = "StagingUnit";
    private String alternativePersistenceUnit = null;
    /**
     * Entity manager used to access the persistence backend
     */
    private static DownloadInformationPersistenceImpl SINGLETON = null;

    /**
     * Get the singleton instance.
     *
     * @param pDownloadUnit The persistence unit to use. By default,
     * 'StagingUnit' will be used.
     *
     * @return The singleton instance.
     */
    public static synchronized DownloadInformationPersistenceImpl getSingleton(String pDownloadUnit) {
        if (SINGLETON == null) {
            if (pDownloadUnit == null) {
                SINGLETON = new DownloadInformationPersistenceImpl();
            } else {
                SINGLETON = new DownloadInformationPersistenceImpl(pDownloadUnit);
            }
        } else if (SINGLETON.getPersistenceUnit() != null && !SINGLETON.getPersistenceUnit().equals(pDownloadUnit)) {
            LOGGER.warn("ATTENTION: Current persistence unit '" + SINGLETON.getPersistenceUnit() + "' is not equal provided persistence unit '" + pDownloadUnit + "'");
        }
        return SINGLETON;
    }

    /**
     * Get the singleton instance using the default persistence unit.
     *
     * @return The singleton instance.
     */
    public static synchronized DownloadInformationPersistenceImpl getSingleton() {
        return getSingleton(null);
    }

    /**
     * Hidden constructor
     */
    DownloadInformationPersistenceImpl() {
    }

    /**
     * Constructor for providing a custom persistence unit used for testing.
     *
     * @param pAltPersistenceUnit The name of the alternative persistence unit.
     */
    DownloadInformationPersistenceImpl(String pAltPersistenceUnit) {
        alternativePersistenceUnit = pAltPersistenceUnit;
    }

    /**
     * Returns the currently used persistence unit.
     *
     * @return The persistence unit's name.
     */
    public String getPersistenceUnit() {
        return (alternativePersistenceUnit == null) ? PERSISTENCE_UNIT_NAME : alternativePersistenceUnit;
    }

    /**
     * Create a new download entity.
     *
     * @param pDigitalObjectId The digital object id for which the download will
     * be created.
     * @param pAccessPointId The AccessPoint to use for downloading data.
     * @param pSecurityContext The security context.
     *
     * @return The created download information.
     */
    public DownloadInformation createEntity(final DigitalObjectId pDigitalObjectId, String pAccessPointId, IAuthorizationContext pSecurityContext) {
        return createEntity(pDigitalObjectId, pAccessPointId, new LinkedList<StagingProcessor>(), pSecurityContext);
    }

    /**
     * Create a new download entity.
     *
     * @param pDigitalObjectId The digital object id for which the download will
     * be created.
     * @param pAccessPointId The AccessPoint to use for downloading data.
     * @param pProcessors A collection of StagingProcessor applied after the download
     * preparation.
     * @param pSecurityContext The security context.
     *
     * @return The created download information.
     */
    public DownloadInformation createEntity(final DigitalObjectId pDigitalObjectId, String pAccessPointId, Collection<StagingProcessor> pProcessors, IAuthorizationContext pSecurityContext) {
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
            List<DownloadInformation> entities = getEntitiesByDigitalObjectId(pDigitalObjectId, 0, Integer.MAX_VALUE, pSecurityContext);
            if (!entities.isEmpty()) {
                throw new PersistenceException("Obviously, there is already an entity with the digital object id '" + pDigitalObjectId + "' registed in the current context.");
            }
        } catch (PersistenceException pe) {
            //some error occured
            throw new PersistenceException("Failed to check for duplicated entities", pe);
        }
        DownloadInformation result = new DownloadInformation(pDigitalObjectId);
        //add staging processors if available
        if (pProcessors != null && !pProcessors.isEmpty()) {
            for (StagingProcessor processor : pProcessors) {
                switch (processor.getType()) {
                    case CLIENT_SIDE_ONLY:
                        result.addClientSideStagingProcessor(processor);
                        break;
                    case SERVER_SIDE_ONLY:
                        result.addServerSideStagingProcessor(processor);
                        break;
                    case CLIENT_AND_SERVER_SIDE:
                        result.addClientSideStagingProcessor(processor);
                        result.addServerSideStagingProcessor(processor);
                        break;
                    default:
                        LOGGER.warn("Unknown/unsupported StagingProcessor type '{}'", processor.getType());
                }
            }
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        try {
            result.setAccessPointId(pAccessPointId);
            result.setOwnerId(pSecurityContext.getUserId().getStringRepresentation());
            result.setGroupId(pSecurityContext.getGroupId().getStringRepresentation());
            result.setLastUpdate(System.currentTimeMillis());
            result.setExpiresAt(System.currentTimeMillis() + DownloadInformation.DEFAULT_LIFETIME);
            LOGGER.debug("Persisting download for object id  '{}'", result.getDigitalObjectId());
            result = mdm.save(result);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new PersistenceException("Failed for persist download information for object id " + pDigitalObjectId + "'", ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public DownloadInformation createEntity(final DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) {
        return createEntity(pDigitalObjectId, null, pSecurityContext);
    }

    /**
     * Get all download entities accessible by pSecurityContext.
     *
     * @param pSecurityContext The security context.
     *
     * @return All download entities.
     */
    public List<DownloadInformation> getAllEntities(IAuthorizationContext pSecurityContext) {
        return getAllEntities(0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public List<DownloadInformation> getAllEntities(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        List<DownloadInformation> results = new LinkedList<>();
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        try {
            results = mdm.findResultList("SELECT d FROM DownloadInformation d WHERE d.ownerUuid LIKE ?1", new Object[]{getOwnerFromContext(pSecurityContext)}, DownloadInformation.class, pMinIndex, pMaxResults);
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
            result = mdm.findSingleResult("SELECT COUNT(d) FROM DownloadInformation d WHERE d.ownerUuid LIKE ?1", new Object[]{getOwnerFromContext(pSecurityContext)}, Number.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get download count using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Get all download entities for the provided digital object id accessible
     * by the provided context.
     *
     * @param pDigitalObjectId The digital object id.
     * @param pSecurityContext The security context.
     *
     * @return A list of download information entities.
     */
    public List<DownloadInformation> getEntitiesByDigitalObjectId(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) {
        return getEntitiesByDigitalObjectId(pDigitalObjectId, 0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public List<DownloadInformation> getEntitiesByDigitalObjectId(DigitalObjectId pDigitalObjectId, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        if (pDigitalObjectId == null) {
            throw new IllegalArgumentException("Argument pDigitalObjectId must not be 'null'");
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        List<DownloadInformation> result = new LinkedList<>();
        try {
            result = mdm.findResultList("SELECT x FROM DownloadInformation x WHERE x.digitalObjectUuid = ?1 AND x.ownerUuid LIKE ?2",
                    new Object[]{pDigitalObjectId.getStringRepresentation(), getOwnerFromContext(pSecurityContext)},
                    DownloadInformation.class, pMinIndex, pMaxResults);
            //We expect for each digital object id within a context one result at maximum. For privileged access by an admin this can be different.
            //Otherwise, only the first element will be returned.
            if (result.size() > 1 && !isPrivilegedContext(pSecurityContext)) {
                LOGGER.warn("Query for DownloadInformation with id '" + pDigitalObjectId + "' returned multiple results. This should not happen for non-privileged queries for DownloadInformation entities. I'll return only the first element.");
                CollectionUtils.removeAll(result, result.subList(0, 1));
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get download for object id " + pDigitalObjectId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }

        return result;
    }

    @Override
    public Number getEntitiesCountByDigitalObjectId(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) {
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        LOGGER.debug("Executing query for entity count by digital object id");
        Number result = 0;
        try {
            result = mdm.findSingleResult("SELECT x FROM DownloadInformation x WHERE x.digitalObjectUuid = ?1 AND x.ownerUuid LIKE ?2", new Object[]{pDigitalObjectId, getOwnerFromContext(pSecurityContext)}, Number.class);
            if (result.intValue() > 1 && !isPrivilegedContext(pSecurityContext)) {
                LOGGER.warn("Query for DownloadInformation count with id '" + pDigitalObjectId + "' returned " + result + ". This should not happen for non-privileged queries for DownloadInformation entities.");
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get download count for object id " + pDigitalObjectId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Get all entities owned by the user identified by the provided
     * authorization context.
     *
     * @param pSecurityContext The authorization context.
     *
     * @return A list of download information entities.
     */
    public List<DownloadInformation> getEntitiesByOwner(IAuthorizationContext pSecurityContext) {
        return getEntitiesByOwner(0, Integer.MAX_VALUE, pSecurityContext);
    }

    /**
     * Get all IngestInformation entities owned by the user specified in
     * pSecurityContext.
     *
     * @param pMinIndex The index of the first element which will be returned.
     * @param pMaxResults The max amount of results beginning at pMinIndex.
     * @param pSecurityContext The security context used to check access
     * permissions for this method.
     *
     * @return The list of download information owned by the current user.
     */
    public List<DownloadInformation> getEntitiesByOwner(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Try getting entities for owner '{}'", pSecurityContext.getUserId());
        return getEntitiesByOwner(pSecurityContext.getUserId(), pMinIndex, pMaxResults, pSecurityContext);
    }

    /**
     * Get all IngestInformation entities owned by the provided user.
     *
     * @param pOwner The owner.
     * @param pSecurityContext The security context used to check access
     * permissions for this method.
     *
     * @return The list of download information owned by the current user.
     */
    public List<DownloadInformation> getEntitiesByOwner(UserId pOwner, IAuthorizationContext pSecurityContext) {
        return getEntitiesByOwner(pOwner, 0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public List<DownloadInformation> getEntitiesByOwner(UserId pOwner, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        UserId userId = pOwner;
        if (userId == null) {
            LOGGER.debug("Argument pOwner is 'null'. Using userId '{}' from context.", pSecurityContext.getUserId());
            userId = pSecurityContext.getUserId();
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        List<DownloadInformation> results = new LinkedList<>();
        try {
            results = mdm.findResultList("SELECT x FROM DownloadInformation x WHERE x.ownerUuid = ?1", new Object[]{userId.getStringRepresentation()}, DownloadInformation.class, pMinIndex, pMaxResults);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get downloads for owner id " + userId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return results;
    }

    @Override
    public Number getEntitiesCountByOwner(UserId pUserId, IAuthorizationContext pSecurityContext) {
        UserId userId = pUserId;
        if (userId == null) {
            LOGGER.debug("Argument pUserId is 'null'. Using userId '{}' from context.", pSecurityContext.getUserId());
            userId = pSecurityContext.getUserId();
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        LOGGER.debug("Executing query for entity count by owner");
        Number result = 0;
        try {
            result = mdm.findSingleResult("SELECT COUNT(i) FROM DownloadInformation i WHERE i.ownerUuid LIKE  ?1", new Object[]{userId.getStringRepresentation()}, Number.class);

        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get download count for owner id " + userId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Get all DownloadInformation entities having the provided status.
     *
     * @param pStatus The status.
     * @param pSecurityContext The security context used to check access
     * permissions for this method.
     *
     * @return The list of download information having the provided status.
     */
    public List<DownloadInformation> getEntitiesByStatus(DOWNLOAD_STATUS pStatus, IAuthorizationContext pSecurityContext) {
        return getEntitiesByStatus(pStatus, 0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public List<DownloadInformation> getEntitiesByStatus(DOWNLOAD_STATUS pStatus, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        if (pStatus == null) {
            throw new IllegalArgumentException("Argument pStatus must not be 'null'");
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        List<DownloadInformation> results = new LinkedList<>();
        try {
            results = mdm.findResultList("SELECT x FROM DownloadInformation x WHERE x.status = ?1 AND x.ownerUuid LIKE ?2",
                    new Object[]{pStatus.getId(), getOwnerFromContext(pSecurityContext)}, DownloadInformation.class, pMinIndex, pMaxResults);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get downloads for status " + pStatus + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return results;
    }

    @Override
    public Number getEntitiesCountByStatus(DOWNLOAD_STATUS pStatus, IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Executing query for entity count by status");
        Number result = 0;
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        try {
            result = mdm.findSingleResult("SELECT COUNT(i) FROM DownloadInformation i WHERE i.status = ?1 AND x.ownerUuid LIKE ?2",
                    new Object[]{pStatus.getId(), getOwnerFromContext(pSecurityContext)}, Number.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get download count for status " + pStatus + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public DownloadInformation getEntityById(long pId, IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Executing query for download entity with id {}", pId);

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        DownloadInformation result = null;
        try {
            result = mdm.findSingleResult("SELECT x FROM DownloadInformation x WHERE x.id = ?1 AND x.ownerUuid LIKE ?2",
                    new Object[]{pId, getOwnerFromContext(pSecurityContext)}, DownloadInformation.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get download for id " + pId + " using context " + pSecurityContext, ex);
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
    public List<DownloadInformation> getExpiredEntities(IAuthorizationContext pSecurityContext) {
        return getExpiredEntities(0, Integer.MAX_VALUE, pSecurityContext);
    }

    @Override
    public List<DownloadInformation> getExpiredEntities(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Executing query for expired downloads.");

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        List<DownloadInformation> results = new LinkedList<>();
        try {
            results = mdm.findResultList("SELECT x FROM DownloadInformation x WHERE (x.expiresAt = -1 AND x.lastUpdate + ?1 < x.expiresAt) OR (x.expiresAt != -1 AND x.expiresAt < ?2) AND x.ownerUuid LIKE ?3",
                    new Object[]{DownloadInformation.DEFAULT_LIFETIME, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)}, DownloadInformation.class, pMinIndex, pMaxResults);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get expired downloads using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return results;
    }

    @Override
    public Number getExpiredEntitiesCount(IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Executing query for expired download count");
        Number result = 0;

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        try {
            result = mdm.findSingleResult("SELECT COUNT(x) FROM DownloadInformation x WHERE (x.expiresAt = -1 AND x.lastUpdate + ?1 < x.expiresAt) OR (x.expiresAt != -1 AND x.expiresAt < ?2) AND x.ownerUuid LIKE ?3",
                    new Object[]{DownloadInformation.DEFAULT_LIFETIME, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)}, Number.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to get expired download count using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    @Override
    public int updateStatus(long pId, DOWNLOAD_STATUS pStatus, String pErrorMessage, IAuthorizationContext pSecurityContext) {
        if (pStatus == null) {
            throw new IllegalArgumentException("Argument pStatus must not be 'null'");
        }
        LOGGER.debug("Updating status of download {} to status {} and error message {}", pId, pStatus, pErrorMessage);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        int result = 0;
        try {
            result = mdm.performUpdate("UPDATE DownloadInformation x SET x.status = ?2, x.errorMessage = ?3, x.lastUpdate = ?4 WHERE x.id = ?1 AND x.ownerUuid LIKE ?5",
                    new Object[]{pId, pStatus.getId(), pErrorMessage, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)});
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to update status for download with id " + pId + " using context " + pSecurityContext, ex);
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

        LOGGER.debug("Updating client access url of download {} to {} ", pId, pClientAccessUrl);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        int result = 0;
        try {
            result = mdm.performUpdate("UPDATE DownloadInformation x SET x.clientAccessUrl = ?2, x.lastUpdate = ?3 WHERE x.id = ?1 AND x.ownerUuid LIKE ?4",
                    new Object[]{pId, pClientAccessUrl, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)});
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to update client access url for download with id " + pId + " using context " + pSecurityContext, ex);
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

        LOGGER.debug("Updating staging url of download {} to {} ", pId, pStagingUrl);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        int result = 0;
        try {
            result = mdm.performUpdate("UPDATE DownloadInformation x SET x.stagingUrl = ?2, x.lastUpdate = ?3 WHERE x.id = ?1 AND x.ownerUuid LIKE ?4",
                    new Object[]{pId, pStagingUrl, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext)});
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to update staging url for download with id " + pId + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;

    }

    @Override
    public int updateStorageUrl(long pId, String pStorageUrl, IAuthorizationContext pSecurityContext) {
        //not implemented for downloads
        return 0;
    }

    @Override
    public int removeEntity(long pId, IAuthorizationContext pSecurityContext) {
        LOGGER.debug("Removing download with id {}", pId);
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        int result = 0;
        try {
            DownloadInformation toDelete = getEntityById(pId, pSecurityContext);
            if (toDelete == null) {
                throw new EntityNotFoundException("Unable to find download for id " + pId + " accessible by context " + pSecurityContext);
            }
            mdm.remove(toDelete);
            result = 1;
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to remove download with id " + pId + " using context " + pSecurityContext, ex);
        } catch (EntityNotFoundException ex) {
            LOGGER.error("No download with id " + pId + " found using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Persists the entity's state in the database with state of the provided
     * entity. This method should only be available for <u>internal use</u>, as
     * illegal states may be produced due to wrong usage (e.g. duplicated
     * entities)!
     *
     * @param pEntity The entity to persist.
     * @param pSecurityContext The security context used for authorization.
     *
     * @return The merged entity, reloaded from the database.
     */
    public DownloadInformation mergeEntity(DownloadInformation pEntity, IAuthorizationContext pSecurityContext) {
        if (pEntity == null) {
            throw new IllegalArgumentException("Argument pEntity must not be 'null'");
        }

        LOGGER.debug("Persisting download information with id {} from database.", pEntity.getId());
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(getPersistenceUnit(), pSecurityContext);
        DownloadInformation result = pEntity;
        try {
            result = mdm.save(pEntity);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to refresh download with id " + pEntity.getId() + " using context " + pSecurityContext, ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Returns the owner id from the current security context. If no context is
     * used, a wildcard entry will be returned and there is no user-based
     * filtering during query operations.
     *
     * @param pSecurityContext The security context which contains the user id.
     *
     * @return The owner used to query for entities. If pSecurityContext belongs
     * to an administrator '%' is returned as a wildcard for all entities.
     */
    private String getOwnerFromContext(IAuthorizationContext pSecurityContext) {
        String owner;

        /* if (pSecurityContext == null) {
            owner = "%";
        } else */
        if (pSecurityContext.getGroupId().getStringRepresentation().equals(Constants.SYSTEM_GROUP)
                || pSecurityContext.getUserId().getStringRepresentation().equals(Constants.SYSTEM_ADMIN)
                || pSecurityContext.getRoleRestriction().equals(Role.ADMINISTRATOR)) {
            //return wildcard entry for administrators
            owner = "%";
        } else {
            owner = pSecurityContext.getUserId().getStringRepresentation();
        }
        return owner;
    }

    /**
     * Returns the owner id from the current security context. If no context is
     * used, a wildcard entry will be returned and there is no user-based
     * filtering during query operations.
     *
     * @param pSecurityContext The security context which contains the user id.
     *
     * @return The owner used to query for entities. If pSecurityContext belongs
     * to an administrator '%' is returned as a wildcard for all entities.
     */
    private boolean isPrivilegedContext(IAuthorizationContext pSecurityContext) {
        return (pSecurityContext.getGroupId().getStringRepresentation().equals(Constants.SYSTEM_GROUP)
                || pSecurityContext.getUserId().getStringRepresentation().equals(Constants.SYSTEM_ADMIN)
                || pSecurityContext.getRoleRestriction().equals(Role.ADMINISTRATOR));
    }
}
