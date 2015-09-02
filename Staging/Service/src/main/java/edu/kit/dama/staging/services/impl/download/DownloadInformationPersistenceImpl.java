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
package edu.kit.dama.staging.services.impl.download;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.jpa.EntityManagerHelper;
import edu.kit.dama.staging.interfaces.ITransferInformationPersistence;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.util.Constants;
import java.util.List;
import javax.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the TransferInformationPersistence interface for data
 * ingest.
 *
 * @see TransferInformationPersistence
 * @author jejkal
 */
public final class DownloadInformationPersistenceImpl implements ITransferInformationPersistence<DOWNLOAD_STATUS, DownloadInformation> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DownloadInformationPersistenceImpl.class);
  private static final String QUERY_ERROR = "Failed to execute query";
  private static final String NAMED_QUERY_LOG = "Executing named query '{}'";
  /**
   * Default persistence unit
   */
  private final static String PERSISTENCE_UNIT_NAME = "StagingUnit";
  private String alternativePersistenceUnit = null;
  /**
   * Entity manager used to access the persistence backend
   */
  private EntityManagerFactory factory;
  private static DownloadInformationPersistenceImpl SINGLETON = null;

  /**
   * Get the singleton instance.
   *
   * @param pDownloadUnit The persistence unit to use. By default, 'StagingUnit'
   * will be used.
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
    } else {
      if (SINGLETON.getPersistenceUnit() != null && !SINGLETON.getPersistenceUnit().equals(pDownloadUnit)) {
        LOGGER.warn("ATTENTION: Current persistence unit '" + SINGLETON.getPersistenceUnit() + "' is not equal provided persistence unit '" + pDownloadUnit + "'");
      }
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
   * Returns the EntityManager. If the manager does not exist yet it will be
   * created.
   *
   * @return The EntityManager.
   */
  private EntityManager getEntityManager() {
    if (factory == null) {
      factory = Persistence.createEntityManagerFactory(getPersistenceUnit());
    }
    return factory.createEntityManager();
  }

  /**
   * Clear all cached values.
   */
  public void clearCache() {
    if (factory != null) {
      factory.getCache().evictAll();
    }
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
    EntityManager entityManager = getEntityManager();
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
      List<DownloadInformation> entities = getEntitiesByDigitalObjectId(pDigitalObjectId, -1, -1, pSecurityContext);
      if (!entities.isEmpty()) {
        throw new PersistenceException("Obviously, there is already an entity with the digital object id '" + pDigitalObjectId + "' registed in the current context.");
      }
    } catch (PersistenceException pe) {
      //some error occured
      throw new PersistenceException("Failed to check for duplicated entities", pe);
    }
    DownloadInformation result = new DownloadInformation(pDigitalObjectId);
    try {
      result.setAccessPointId(pAccessPointId);
      result.setOwnerId(pSecurityContext.getUserId().getStringRepresentation());
      result.setGroupId(pSecurityContext.getGroupId().getStringRepresentation());
      result.setLastUpdate(System.currentTimeMillis());
      result.setExpiresAt(System.currentTimeMillis() + DownloadInformation.DEFAULT_LIFETIME);
      LOGGER.debug("Persisting object with id '{}'", result.getDigitalObjectId());
      entityManager.getTransaction().begin();
      entityManager.persist(result);
      entityManager.flush();
    } catch (PersistenceException pe1) {
      try {
        entityManager.getTransaction().rollback();
      } catch (IllegalStateException | PersistenceException ise) {
        LOGGER.error("Rollback failed for object " + result.getDigitalObjectId(), ise);
      }
      throw new PersistenceException("Failed to persist download information for object '" + result.getDigitalObjectId() + "'", pe1);
    } finally {
      finalizeTransaction(entityManager);
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
    return getAllEntities(-1, -1, pSecurityContext);
  }

  @Override
  public List<DownloadInformation> getAllEntities(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
    return performQueryForList("GetAllDownloads", pMinIndex, pMaxResults, getOwnerFromContext(pSecurityContext));
  }

  @Override
  public Number getAllEntitiesCount(IAuthorizationContext pSecurityContext) {
    EntityManager entityManager = getEntityManager();
    LOGGER.debug("Executing query for entity count");
    Number result = 0;
    try {
      entityManager.getTransaction().begin();
      Query q = entityManager.createQuery("SELECT COUNT(d) FROM DownloadInformation d WHERE d.ownerUuid LIKE '" + getOwnerFromContext(pSecurityContext) + "'");
      result = (Number) q.getSingleResult();
    } catch (NoResultException nre) {
      LOGGER.info("Query returned no result", nre);
    } catch (PersistenceException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
    }
    return result;
  }

  /**
   * Get all download entities for the provided digital object id accessible by
   * the provided context.
   *
   * @param pDigitalObjectId The digital object id.
   * @param pSecurityContext The security context.
   *
   * @return A list of download information entities.
   */
  public List<DownloadInformation> getEntitiesByDigitalObjectId(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) {
    return getEntitiesByDigitalObjectId(pDigitalObjectId, -1, -1, pSecurityContext);
  }

  @Override
  public List<DownloadInformation> getEntitiesByDigitalObjectId(DigitalObjectId pDigitalObjectId, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
    if (pDigitalObjectId == null) {
      throw new IllegalArgumentException("Argument pDigitalObjectId must not be 'null'");
    }

    //we expect for each digital object id within a context one result at maximum. If there are more results a PersistenceException will be thrown.
    //In case of data downloads this may happen, but for data download only one entity must exist for one object id.
    List<DownloadInformation> result = performQueryForList("GetDownloadsByObjectId", pMinIndex, pMaxResults, pDigitalObjectId.getStringRepresentation(), getOwnerFromContext(pSecurityContext));
    if (result.size() > 0 && result.size() != 1) {
      throw new PersistenceException("Query for DownloadInformation with id '" + pDigitalObjectId + "' returned multiple results. This should not happen for DownloadInformation entities.");
    }
    return result;
  }

  @Override
  public Number getEntitiesCountByDigitalObjectId(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) {
    EntityManager entityManager = getEntityManager();
    LOGGER.debug("Executing query for entity count by digital object id");
    Number result = 0;
    try {
      entityManager.getTransaction().begin();
      Query q = entityManager.createQuery("SELECT x FROM DownloadInformation x WHERE x.digitalObjectUuid = ?1 AND x.ownerUuid LIKE " + pSecurityContext.getUserId().getStringRepresentation());
      result = (Number) q.getSingleResult();
    } catch (NoResultException nre) {
      LOGGER.info("Query returned no result", nre);
    } catch (PersistenceException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
    }
    return result;
  }

  /**
   * Get all entities owned by the user identified by the provided authorization
   * context.
   *
   * @param pSecurityContext The authorization context.
   *
   * @return A list of download information entities.
   */
  public List<DownloadInformation> getEntitiesByOwner(IAuthorizationContext pSecurityContext) {
    return getEntitiesByOwner(-1, -1, pSecurityContext);
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
    if (pSecurityContext != null) {
      LOGGER.debug("Try getting entities for owner '{}'", pSecurityContext.getUserId());
      return getEntitiesByOwner(pSecurityContext.getUserId(), pMinIndex, pMaxResults, pSecurityContext);
    } else {
      throw new IllegalArgumentException("Argument pSecurityContext must not be 'null'");
    }
  }

  /**
   * Get all IngestInformation entities owned by the provided user.
   *
   * @param pUserId The owner.
   * @param pSecurityContext The security context used to check access
   * permissions for this method.
   *
   * @return The list of download information owned by the current user.
   */
  public List<DownloadInformation> getEntitiesByOwner(UserId pUserId, IAuthorizationContext pSecurityContext) {
    return getEntitiesByOwner(pUserId, -1, -1, pSecurityContext);
  }

  @Override
  public List<DownloadInformation> getEntitiesByOwner(UserId pUserId, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
    if (pUserId == null) {
      throw new IllegalArgumentException("Argument pUserId must not be 'null'");
    }

    return performQueryForList("GetDownloadsByOwner", pMinIndex, pMaxResults, pUserId.getStringRepresentation());
  }

  @Override
  public Number getEntitiesCountByOwner(UserId pUserId, IAuthorizationContext pSecurityContext) {
    EntityManager entityManager = getEntityManager();
    LOGGER.debug("Executing query for entity count by owner");
    Number result = 0;
    try {
      entityManager.getTransaction().begin();
      Query q = entityManager.createQuery("SELECT COUNT(i) FROM DownloadInformation i WHERE i.ownerUuid LIKE '" + pUserId.getStringRepresentation() + "'");
      result = (Number) q.getSingleResult();
    } catch (NoResultException nre) {
      LOGGER.info("Query returned no result", nre);
    } catch (PersistenceException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
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
    return getEntitiesByStatus(pStatus, -1, -1, pSecurityContext);
  }

  @Override
  public List<DownloadInformation> getEntitiesByStatus(DOWNLOAD_STATUS pStatus, int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
    if (pStatus == null) {
      throw new IllegalArgumentException("Argument pStatus must not be 'null'");
    }
    return performQueryForList("GetDownloadsByStatus", pMinIndex, pMaxResults, pStatus.getId(), getOwnerFromContext(pSecurityContext));
  }

  @Override
  public Number getEntitiesCountByStatus(DOWNLOAD_STATUS pStatus, IAuthorizationContext pSecurityContext) {
    EntityManager entityManager = getEntityManager();
    LOGGER.debug("Executing query for entity count by status");
    Number result = 0;
    try {
      entityManager.getTransaction().begin();
      Query q = entityManager.createQuery("SELECT COUNT(i) FROM DownloadInformation i WHERE i.status=" + pStatus.getId());
      result = (Number) q.getSingleResult();
    } catch (NoResultException nre) {
      LOGGER.info("Query returned no result", nre);
    } catch (PersistenceException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
    }
    return result;
  }

  @Override
  public DownloadInformation getEntityById(long pId, IAuthorizationContext pSecurityContext) {
    return performQueryForSingleValue("GetDownloadsById", pId, getOwnerFromContext(pSecurityContext));
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
    return getExpiredEntities(-1, -1, pSecurityContext);
  }

  @Override
  public List<DownloadInformation> getExpiredEntities(int pMinIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
    return performQueryForList("GetExpiredDownloads", pMinIndex, pMaxResults, DownloadInformation.DEFAULT_LIFETIME, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext));
  }

  @Override
  public Number getExpiredEntitiesCount(IAuthorizationContext pSecurityContext) {
    EntityManager entityManager = getEntityManager();
    LOGGER.debug("Executing query for expired entity count");
    Number result = 0;
    try {
      entityManager.getTransaction().begin();
      Query q = entityManager.createQuery("SELECT COUNT(i) FROM DownloadInformation i WHERE (i.expiresAt = -1 AND i.lastUpdate + "
              + DownloadInformation.DEFAULT_LIFETIME
              + " < i.expiresAt) OR (i.expiresAt != -1 AND i.expiresAt < " + System.currentTimeMillis() + ") AND i.ownerUuid LIKE '" + getOwnerFromContext(pSecurityContext) + "'");
      result = (Number) q.getSingleResult();
    } catch (NoResultException nre) {
      LOGGER.info("Query returned no result", nre);
    } catch (PersistenceException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
    }
    return result;
  }

  @Override
  public int updateStatus(long pId, DOWNLOAD_STATUS pStatus, String pErrorMessage, IAuthorizationContext pSecurityContext) {
    if (pStatus == null) {
      throw new IllegalArgumentException("Argument pStatus must not be 'null'");
    }
    return performUpdate("UpdateDownloadStatus", pId, pStatus.getId(), pErrorMessage, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext));
  }

  @Override
  public int updateClientAccessUrl(long pId, String pClientAccessUrl, IAuthorizationContext pSecurityContext) {
    if (pClientAccessUrl == null) {
      throw new IllegalArgumentException("Argument pClientAccessUrl must not be 'null'");
    }
    return performUpdate("UpdateDownloadClientAccessUrl", pId, pClientAccessUrl, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext));
  }

  @Override
  public int updateStagingUrl(long pId, String pStagingUrl, IAuthorizationContext pSecurityContext) {
    if (pStagingUrl == null) {
      throw new IllegalArgumentException("Argument pStagingUrl must not be 'null'");
    }
    return performUpdate("UpdateDownloadStagingUrl", pId, pStagingUrl, System.currentTimeMillis(), getOwnerFromContext(pSecurityContext));
  }

  @Override
  public int updateStorageUrl(long pId, String pStorageUrl, IAuthorizationContext pSecurityContext) {
    //not implemented for downloads
    return 0;
  }

  @Override
  public int removeEntity(long pId, IAuthorizationContext pSecurityContext) {
    return performDelete("DeleteDownloadById", pId, getOwnerFromContext(pSecurityContext));
  }

  /**
   * Merge the entity's state in the database with state of the provided entity.
   * This method should only be available for <u>internal use</u>, as illegal
   * states may be produced due to wrong usage (e.g. duplicated entities)!
   *
   * @param pEntity The entity to merge.
   * @param pSecurityContext The security context used for authorization.
   *
   * @return The merged entity, reloaded from the database.
   */
  public DownloadInformation mergeEntity(DownloadInformation pEntity, IAuthorizationContext pSecurityContext) {
    return performMerge(pEntity);
  }

  /**
   * Perform a query whose result is a list of entities. The query itself is
   * defined by a named query directly in the entity implementation, arguments
   * must be provided in the correct order withing pParams.
   *
   * @param pNamedQuery The named query identifier.
   * @param pMinIndex The index at which the query should start. If this
   * argument is smaller or equal 0, it will be started with the first index.
   * @param pMaxResults The max. amount of results returned starting from
   * pMinIndex. If this argument is smaller or equal 0, all results, starting
   * from pMinIndex, will be returned.
   * @param pParams Array of arguments for the named query.
   *
   * @return A list of DownloadInformation entities fitting the named query.
   */
  private List<DownloadInformation> performQueryForList(String pNamedQuery, int pMinIndex, int pMaxResults, Object... pParams) {
    EntityManager entityManager = getEntityManager();

    List<DownloadInformation> result = null;
    try {
      LOGGER.debug(NAMED_QUERY_LOG, pNamedQuery);
      entityManager.getTransaction().begin();
      Query q = entityManager.createNamedQuery(pNamedQuery, DownloadInformation.class);
      q.setHint("eclipselink.refresh", "true");
      if (pParams != null) {
        LOGGER.debug("Adding '{}' query arguments", pParams.length);
        for (int id = 1; id <= pParams.length; id++) {
          LOGGER.debug("Adding arg '{}'", pParams[id - 1]);
          q.setParameter(id, pParams[id - 1]);
        }
      }
      if (pMinIndex > 0) {
        q.setFirstResult(pMinIndex);
      }

      if (pMaxResults > 0) {
        q.setMaxResults(pMaxResults);
      }

      LOGGER.debug("Executing query for result list");
      result = q.getResultList();
    } catch (NoResultException nre) {
      LOGGER.info("Query returned no result", nre);
      result = null;
    } catch (PersistenceException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
    }

    return result;
  }

  /**
   * Perform a query whose result is a single entities. The query itself is
   * defined by a named query directly in the entity implementation, arguments
   * must be provided in the correct order withing pParams. This method will
   * throw an error if there are multiple results returned by the query.
   *
   * @param pNamedQuery The named query identifier.
   * @param pParams Array of arguments for the named query.
   *
   * @return A DownloadInformation entity fitting the named query.
   */
  private DownloadInformation performQueryForSingleValue(String pNamedQuery, Object... pParams) {
    EntityManager entityManager = getEntityManager();

    DownloadInformation result = null;
    try {
      LOGGER.debug(NAMED_QUERY_LOG, pNamedQuery);
      entityManager.getTransaction().begin();
      Query q = entityManager.createNamedQuery(pNamedQuery, DownloadInformation.class);
      q.setHint("eclipselink.refresh", "true");

      if (pParams != null) {
        LOGGER.debug("Adding '{}' query arguments", pParams.length);
        for (int id = 1; id <= pParams.length; id++) {
          LOGGER.debug("Adding arg '{}'", pParams[id - 1]);
          q.setParameter(id, pParams[id - 1]);
        }
      }
      LOGGER.debug("Executing query for single result");
      result = (DownloadInformation) q.getSingleResult();
    } catch (NoResultException nre) {
      LOGGER.info("Query returned no result", nre);
      result = null;
    } catch (PersistenceException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
    }
    return result;
  }

  /**
   * Perform an update operation. The query itself is defined by a named query
   * directly in the entity implementation, arguments must be provided in the
   * correct order withing pParams.
   *
   * @param pNamedQuery The named query identifier.
   * @param pParams Array of arguments for the named query.
   *
   * @return The amount of affected rows.
   */
  private int performUpdate(String pNamedQuery, Object... pParams) {
    EntityManager entityManager = getEntityManager();

    int result = 0;
    try {
      LOGGER.debug(NAMED_QUERY_LOG, pNamedQuery);
      entityManager.getTransaction().begin();
      Query q = entityManager.createNamedQuery(pNamedQuery, DownloadInformation.class);

      if (pParams != null) {
        LOGGER.debug("Adding {} update arguments", pParams.length);
        for (int id = 1; id <= pParams.length; id++) {
          LOGGER.debug("Adding arg '{}'", pParams[id - 1]);
          q.setParameter(id, pParams[id - 1]);
        }
      }
      LOGGER.debug("Executing update");
      result = q.executeUpdate();
    } catch (PersistenceException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
    }
    return result;
  }

  /**
   * Merge an entity with the state of the database.
   *
   * @param pDownloadInformation The locally modified entity.
   *
   * @return The merged entity.
   */
  private DownloadInformation performMerge(DownloadInformation pDownloadInformation) {
    EntityManager entityManager = getEntityManager();

    DownloadInformation result = null;

    try {
      LOGGER.debug(NAMED_QUERY_LOG, "Performing merge operation");
      entityManager.getTransaction().begin();
      if (EntityManagerHelper.getIdOfEntity(DownloadInformation.class, pDownloadInformation) == null) {
        throw new EntityNotFoundException(pDownloadInformation.toString());
      }
      LOGGER.debug("Executing merge");
      result = entityManager.merge(pDownloadInformation);
    } catch (EntityNotFoundException | UnauthorizedAccessAttemptException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
    }
    return result;
  }

  /**
   * Perform a delete operation. The query itself is defined by a named query
   * directly in the entity implementation, arguments must be provided in the
   * correct order withing pParams.
   *
   * @param pNamedQuery The named query identifier.
   * @param pParams Array of arguments for the named query.
   *
   * @return The amount of affected rows.
   */
  private int performDelete(String pNamedQuery, Object... pParams) {
    EntityManager entityManager = getEntityManager();

    int result = 0;
    try {
      LOGGER.debug(NAMED_QUERY_LOG, pNamedQuery);
      entityManager.getTransaction().begin();
      Query q = entityManager.createNamedQuery(pNamedQuery, DownloadInformation.class);
      if (pParams != null) {
        LOGGER.debug("Adding {0} deletion arguments", pParams.length);
        for (int id = 1; id <= pParams.length; id++) {
          LOGGER.debug("Adding arg '{}'", pParams[id - 1]);
          q.setParameter(id, pParams[id - 1]);
        }
      }
      LOGGER.debug("Executing deletion");
      result = q.executeUpdate();
    } catch (PersistenceException pe1) {
      throw new PersistenceException(QUERY_ERROR, pe1);
    } finally {
      finalizeTransaction(entityManager);
    }
    return result;
  }

  /**
   * Finalize the currently active transaction.
   */
  private void finalizeTransaction(EntityManager entityManager) {
    if (entityManager == null) {
      LOGGER.warn("EntityManager is null. Skipping finalization.");
      return;
    }

    EntityTransaction currentTransaction = entityManager.getTransaction();
    if (currentTransaction == null) {
      LOGGER.warn("Current transaction is null. Skipping finalization.");
    } else {
      try {
        entityManager.flush();
        currentTransaction.commit();
      } catch (PersistenceException ex) {
        LOGGER.error("Failed to commit current transaction.", ex);
      } finally {
        if (currentTransaction.isActive()) {
          LOGGER.debug("Transaction is still active. Performing rollback.");
          currentTransaction.rollback();
        }
      }
    }
    entityManager.close();
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
    return owner;
  }
  ////////////////////////////////////////////////////////////////////////
  //////////////////////////TESTING STUFF/////////////////////////////////
  ////////////////////////////////////////////////////////////////////////
    /*
   * public TypedQuery<IngestInformation> buildQuery(EntityManager pManager) {
   * CriteriaBuilder cb = pManager.getCriteriaBuilder();
   * CriteriaQuery<IngestInformation> critQuery =
   * cb.createQuery(IngestInformation.class); Root<IngestInformation> root =
   * critQuery.from(IngestInformation.class);
   * critQuery.select(root).where(cb.greaterThan(root.get(IngestInformation_.id),
   * 300l)); critQuery.orderBy(cb.asc(root.get("lastUpdate")));
   * TypedQuery<IngestInformation> typeQuery =
   * pManager.createQuery(critQuery);
   *
   * return typeQuery; }
   */
  /*
   * public static void main(String[] args) { IngestInformationPersistenceImpl
   * impl = new IngestInformationPersistenceImpl();
   * impl.createEntity(UUID.randomUUID().toString(), new
   * IAuthorizationContext() {
   *
   * @Override public void setUserId(UserId userId) { }
   *
   * @Override public void setGropId(GroupId groupId) { }
   *
   * @Override public void setRoleRestriction(RoleRestriction roleRestriction)
   * { }
   *
   * @Override public UserId getUserId() { return new UserId() {
   *
   * @Override public String asString() { return "testUser"; }
   *
   * @Override public void parse(String userId) { } }; }
   *
   * @Override public GroupId getGropId() { return null; }
   *
   * @Override public RoleRestriction getRoleRestriction() { return null; }
   * }); List<IngestInformation> all = impl.getAllEntities(null); for
   * (IngestInformation i : all) { System.out.println(i.getId() + ": " +
   * i.getLastUpdate()); } }
   */
}
