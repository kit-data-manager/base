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

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.tools.DigitalObjectSecureQueryHelper;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.handlers.impl.IngestPreparationHandler;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.staging.services.impl.StagingService;
import edu.kit.dama.staging.util.TransferClientPropertiesUtils;
import edu.kit.dama.staging.interfaces.IIngestInformationService;
import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local implementation of the IngestInformationService. This service is
 * intended to be used server-sided, either by the user frontend or by external
 * services. This service offers a limited access to the IngestInformation
 * persistence implementation, a method to prepare an ingest and some
 * administrative methods for cleanup.
 *
 * @author jejkal
 */
public final class IngestInformationServiceLocal implements IIngestInformationService<IAuthorizationContext> {

  /**
   * The logger
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(IngestInformationServiceLocal.class);
  /**
   * IngestInformationPersistanceImpl used by this service. The actual
   * implementation can be chosen during initialize()
   */
  private static IngestInformationPersistenceImpl persistenceImpl = null;
  /**
   * The singleton instance
   */
  private static IngestInformationServiceLocal SINGLETON = null;

  static {
    //intitially configure the ingest service
    initialize();
    SINGLETON = new IngestInformationServiceLocal();
  }

  /**
   * Get the singleton instance of the local IngestInformationService.
   *
   * @return The singleton instance.
   */
  public static IngestInformationServiceLocal getSingleton() {
    return SINGLETON;
  }

  /**
   * Default constructor.
   */
  IngestInformationServiceLocal() {
  }

  /**
   * Initialize the service.
   */
  private static void initialize() {
    persistenceImpl = IngestInformationPersistenceImpl.getSingleton();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public List<IngestInformation> getAllIngestInformation(int pFirstIndex, int pMaxResults, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getAllIngestInformation()");
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all ingest information entities fulfilling the criteria
     * from the data backend
     */
    return persistenceImpl.getAllEntities(pFirstIndex, pMaxResults, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer getAllIngestInformationCount(IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getDownloadInformationCount()");
    return persistenceImpl.getAllEntitiesCount(pSecurityContext).intValue();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public List<IngestInformation> getIngestInformationByOwner(final String pOwner, int pFirstIndex, int pMaxResults, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getIngestInformationByOwner({})", pOwner);
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all ingest information entities fulfilling the criteria
     * from the data backend
     */
    return persistenceImpl.getEntitiesByOwner(new UserId(pOwner), pFirstIndex, pMaxResults, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer getIngestInformationCountByOwner(final String pOwner, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getIngestInformationCountByOwner({})", pOwner);
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all ingest information entities fulfilling the criteria
     * from the data backend
     */
    return persistenceImpl.getEntitiesCountByOwner(new UserId(pOwner), pSecurityContext).intValue();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public IngestInformation getIngestInformationByDigitalObjectId(DigitalObjectId pDigitalObjectId, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getIngestInformationByDigitalObjectId({})", pDigitalObjectId);
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all ingest information entities fulfilling the criteria
     * from the data backend
     */
    List<IngestInformation> result = persistenceImpl.getEntitiesByDigitalObjectId(pDigitalObjectId, pSecurityContext);
    if (result.isEmpty()) {
      return null;
    } else if (result.size() == 1) {
      return result.get(0);
    } else {
      LOGGER.warn("More than one ingest information found for digital object id {}. Returning first result.", pDigitalObjectId);
      return result.get(0);
    }
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public IngestInformation getIngestInformationById(Long pIngestId, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getIngestInformationById({})", pIngestId);
    /**
     * general access check using aspects map pIngestId and pSecurityContext to
     * a filter criteria retrieve the IngestInformation entity and return it
     */
    return persistenceImpl.getEntityById(pIngestId, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public List<IngestInformation> getIngestInformationByStatus(Integer pStatusCode, int pFirstIndex, int pMaxResults, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getIngestInformationByStatus({})", pStatusCode);
    /**
     * general access check using aspects map pStatus and pSecurityContext to a
     * filter criteria retrieve all ingest information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getEntitiesByStatus(INGEST_STATUS.idToStatus(pStatusCode), pFirstIndex, pMaxResults, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer getIngestInformationCountByStatus(final Integer pStatusCode, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getIngestInformationCountByStatus({})", pStatusCode);
    /**
     * general access check using aspects map pSecurityContext to a filter
     * criteria retrieve all ingest information entities fulfilling the criteria
     * from the data backend
     */
    return persistenceImpl.getEntitiesCountByStatus(INGEST_STATUS.idToStatus(pStatusCode), pSecurityContext).intValue();
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public List<IngestInformation> getExpiredIngestInformation(int pFirstIndex, int pMaxResults, @Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getExpiredIngestInformation()");
    /**
     * general access check using aspects map pStatus and pSecurityContext to a
     * filter criteria retrieve all ingest information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getExpiredEntities(pFirstIndex, pMaxResults, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer getExpiredIngestInformationCount(@Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing getExpiredIngestInformationCount()");
    /**
     * general access check using aspects map pStatus and pSecurityContext to a
     * filter criteria retrieve all ingest information entities fulfilling the
     * criteria from the data backend
     */
    return persistenceImpl.getExpiredEntitiesCount(pSecurityContext).intValue();
  }

  /**
   * Wrapper for better, internal access.
   *
   * @param pDigitalObjectId The digital object id.
   * @param pProperties The transfer client properties.
   * @param pSecurityContext The security context.
   *
   * @return The newly created ingest information entity.
   *
   * @throws TransferPreparationException If the transfer preparation failed.
   */
  @SecuredMethod(roleRequired = Role.MEMBER)
  public IngestInformation prepareIngest(DigitalObjectId pDigitalObjectId, TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    return prepareIngest(pDigitalObjectId, TransferClientPropertiesUtils.propertiesToMap(pProperties), pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public IngestInformation prepareIngest(DigitalObjectId pDigitalObjectId, Map<String, String> pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    LOGGER.info("Executing prepareIngest({}, {})", new Object[]{pDigitalObjectId, pProperties});

    if (null == pDigitalObjectId) {
      throw new IllegalArgumentException("Argument 'pDigitalObjectId' must not be null");
    }
    LOGGER.debug("Checking for digital object with id '{}'", pDigitalObjectId);
    checkObject(pDigitalObjectId, pSecurityContext);

    LOGGER.debug("Searching for default staging processors for group '{}'", pSecurityContext.getGroupId());
    List<StagingProcessor> processors = StagingConfigurationPersistence.getSingleton().findStagingProcessorsForGroup(pSecurityContext.getGroupId().getStringRepresentation());
    LOGGER.debug("Checking {} staging processor(s)", processors.size());
    List<StagingProcessor> enabledProcessors = new ArrayList<>();
    for (StagingProcessor processor : processors) {
      if (processor.isDefaultOn() && !processor.isDisabled()) {
        LOGGER.debug(" - Adding default staging processor " + processor.getUniqueIdentifier());
        enabledProcessors.add(processor);
      }
    }

    LOGGER.debug("Checking for existing ingest entities for object id '{}'", pDigitalObjectId);
    List<IngestInformation> existingEntities = persistenceImpl.getEntitiesByDigitalObjectId(pDigitalObjectId, pSecurityContext);

    IngestInformation activeIngest;
    TransferClientProperties props = TransferClientPropertiesUtils.mapToProperties(pProperties);
    if (!existingEntities.isEmpty()) {
      LOGGER.debug("Existing ingest found...checking status");
      //check status of existing entity
      IngestInformation existingEntity = existingEntities.get(0);
      switch (existingEntity.getStatusEnum()) {
        case PRE_INGEST_FAILED:
        case INGEST_FAILED:
          //error state...overwrite permitted
          LOGGER.debug("Existing ingest entity found, but due to an error state overwriting is allowed");
          break;
        default:
          //no error status...overwrite forbidden
          throw new TransferPreparationException("There is already an ingest entity for the digital object with id '" + pDigitalObjectId + "'");
      }

      LOGGER.debug("Existing ingest found. Removing local data.");
      StagingService.getSingleton().flushIngest(pDigitalObjectId, pSecurityContext);

      LOGGER.debug("Resetting existing ingest entry");
      //reset status and error message
      existingEntity.setStatusEnum(INGEST_STATUS.PREPARING);
      existingEntity.setErrorMessage(null);
      existingEntity.setExpiresAt(System.currentTimeMillis() + IngestInformation.DEFAULT_LIFETIME);
      existingEntity.setClientAccessUrl(null);
      existingEntity.setAccessPointId(props.getStagingAccessPointId());

      List<StagingProcessor> merged = mergeStagingProcessors(existingEntity.getStagingProcessors(), enabledProcessors);

      for (final StagingProcessor proc : merged) {
        LOGGER.debug("Default staging processor with id {} not linked to ingest, yet. Adding it.");
        existingEntity.addServerSideStagingProcessor(proc);
      }

      //merge the entity with the database
      activeIngest = persistenceImpl.mergeEntity(existingEntity, pSecurityContext);
    } else {
      List<StagingProcessor> merged = mergeStagingProcessors(props.getProcessors(), enabledProcessors);

      for (final StagingProcessor proc : merged) {
        LOGGER.debug("Default staging processor with id {} not found in properties, yet. Adding it.");
        props.addProcessor(proc);
      }

      LOGGER.debug("No entity found for ID '{}'. Creating new ingest entity.", pDigitalObjectId);
      //no entity for pDigitalObjectId found...create a new one
      activeIngest = persistenceImpl.createEntity(pDigitalObjectId, props.getStagingAccessPointId(), props.getProcessors(), pSecurityContext);
    }

    boolean success = false;

    try {
      LOGGER.debug("Creating IngestPreparationHandler");
      IngestPreparationHandler handler = new IngestPreparationHandler(persistenceImpl, activeIngest);
      LOGGER.debug("Preparing ingest");
      handler.prepareTransfer(props, pSecurityContext);
      LOGGER.debug("Ingest preparation finished");
      success = true;
    } finally {
      if (!success) {
        //put ingest in error state first
        LOGGER.debug("Transfer preparation failed. Putting ingest into error state.");
        activeIngest.setStatusEnum(INGEST_STATUS.PREPARATION_FAILED);
        activeIngest.setErrorMessage("Ingest preparation failed. See logfile for details.");
        LOGGER.debug("Persisting modified ingest.");
        activeIngest = persistenceImpl.mergeEntity(activeIngest, pSecurityContext);
        LOGGER.debug("Entity changed committed.");
      }
    }
    return activeIngest;
  }

  private List<StagingProcessor> mergeStagingProcessors(Collection<StagingProcessor> existing, List<StagingProcessor> toAdd) {
    LOGGER.debug("Checking default staging processors.");
    if (existing == null || existing.isEmpty()) {
      //return all
      LOGGER.debug("No staging processors used, using only default processors.");
      return toAdd;
    }
    List<StagingProcessor> result = new ArrayList<>();
    if (toAdd == null || toAdd.isEmpty()) {
      //return empty list
      LOGGER.debug("No default processors provided, using only manually assigned processors.");
      return result;
    }

    LOGGER.debug("Merging {} existing and {} default processor(s)", existing.size(), toAdd.size());
    for (final StagingProcessor processor : toAdd) {
      LOGGER.debug("Searching for default processor with id {}", processor.getUniqueIdentifier());
      StagingProcessor exists = (StagingProcessor) CollectionUtils.find(existing, new Predicate() {

        @Override
        public boolean evaluate(Object o) {
          return ((StagingProcessor) o).getUniqueIdentifier().equals(processor.getUniqueIdentifier());
        }
      });

      if (exists == null) {
        LOGGER.debug("Default processor with id {} is not assigned yet. Adding it.", processor.getUniqueIdentifier());
        //add as it not exists
        result.add(processor);
      }
    }
    return result;
  }

  /**
   * Check if a digital object for the provided id exists in order to allow an
   * ingest. If no entity exists or is not accessible using the provided
   * context, this method will throw an exception and the staging operation
   * should fail.
   *
   * @param pDigitalObjectId The id of the digital object to check for.
   * @param pSecurityContext The security context used to access the digital
   * object.
   *
   * @throws TransferPreparationException if anything goes wrong.
   */
  private void checkObject(DigitalObjectId pDigitalObjectId, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pSecurityContext);
    try {
      if (!new DigitalObjectSecureQueryHelper().objectByIdentifierExists(pDigitalObjectId.toString(), mdm, pSecurityContext)) {
        throw new TransferPreparationException("Digital object for id '" + pDigitalObjectId + "' not found");
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      throw new TransferPreparationException("Digital object for id '" + pDigitalObjectId + "' not accessible", ex);
    } catch (EntityNotFoundException ex) {
      throw new TransferPreparationException("Digital object for id '" + pDigitalObjectId + "' not found", ex);
    } finally {
      mdm.close();
    }
  }

  /**
   * Removes all ingests which have failed or finished and which are expired.
   * This method should be used internally only, either by an administrator or
   * by an automated process.
   *
   * @param pSecurityContext The security context.
   *
   * @return The number of removed entities.
   */
  @SecuredMethod(roleRequired = Role.ADMINISTRATOR)
  public Integer cleanup(@Context IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing cleanup()");
    //obtain relevant entities
    LOGGER.debug("Obtaining expired ingest information entities");
    List<IngestInformation> expiredIngests = getExpiredIngestInformation(-1, -1, pSecurityContext);
    LOGGER.debug("Obtaining ingest information with status INGEST_REMOVED");
    List<IngestInformation> removedIngests = getIngestInformationByStatus(INGEST_STATUS.INGEST_REMOVED.getId(), -1, -1, pSecurityContext);

    int removeEntities = 0;
    //remove remaining entities
    LOGGER.debug("Removing {} expired entities", expiredIngests.size());
    for (IngestInformation information : expiredIngests) {
      StagingService.getSingleton().flushIngest(new DigitalObjectId(information.getDigitalObjectId()), pSecurityContext);
      removeEntities += persistenceImpl.removeEntity(information.getId(), pSecurityContext);
    }
    LOGGER.debug("Removing {} removed entities", removedIngests.size());
    for (IngestInformation information : removedIngests) {
      StagingService.getSingleton().flushIngest(new DigitalObjectId(information.getDigitalObjectId()), pSecurityContext);
      removeEntities += persistenceImpl.removeEntity(information.getId(), pSecurityContext);
    }

    LOGGER.info("Cleanup done");
    return removeEntities;
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer updateStatus(Long pId, Integer pStatus, String pErrorMessage, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing updateStatus({}, {})", new Object[]{pId, pErrorMessage});
    return persistenceImpl.updateStatus(pId, INGEST_STATUS.idToStatus(pStatus), pErrorMessage, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer updateClientAccessUrl(Long pId, String pAccessURL, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing updateClientAccessUrl({}, {})", new Object[]{pId, pAccessURL});
    return persistenceImpl.updateClientAccessUrl(pId, pAccessURL, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer updateStagingUrl(Long pId, String pStagingURL, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing updateStagingUrl({}, {})", new Object[]{pId, pStagingURL});
    return persistenceImpl.updateStagingUrl(pId, pStagingURL, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer updateStorageUrl(Long pId, String pStorageURL, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing updateStorageUrl({}, {})", new Object[]{pId, pStorageURL});
    return persistenceImpl.updateStorageUrl(pId, pStorageURL, pSecurityContext);
  }

  @Override
  @SecuredMethod(roleRequired = Role.MEMBER)
  public Integer removeEntity(Long pId, IAuthorizationContext pSecurityContext) {
    LOGGER.info("Executing removeEntity({})", new Object[]{pId});
    return persistenceImpl.removeEntity(pId, pSecurityContext);
  }
}
