/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
package edu.kit.dama.staging.handlers;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.exceptions.PropertyNotFoundException;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.dama.staging.interfaces.ITransferInformationPersistence;
import edu.kit.dama.staging.interfaces.ITransferStatus;
import edu.kit.dama.staging.util.TransferClientPropertiesUtils;
import edu.kit.dama.staging.entities.StagingPreparationResult;
import edu.kit.dama.staging.util.StagingConfigurationPersistence;
import edu.kit.dama.staging.services.impl.StagingService;
import edu.kit.dama.util.DataManagerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AbstractTransferPreparationHandler defined how to prepare data transfers.
 * A concrete implementation is linked to a transfer status implementation.
 * According to this implementation a persistence implementation can be set in
 * the constructor. After this, a call to prepareTransfer() will start the
 * initialization. At the very first setupInternal() is called. This method
 * takes care for internal initialization, e.g. the creation of the transfer
 * preparation client based on the provided property map. At the end of this
 * internal setup, the external setup is performed. The external setup is
 * optional and empty by default. After setting up the transfer handler, three
 * preparation phases are performed: <ul> <li> Environment preparation: Takes
 * care of setting up the environment if needed. For the default implementation
 * this step is empty as all essential environment preparation (directory
 * creation, granting of permissions) are covered by the StagingService accessed
 * by this handler. As a result, the source/destination directory is added to
 * the property map.</li> <li> Transfer preparation: At this point the transfer
 * client preparation handler is called. It is responsible for creating an
 * access to the source/destination prepared in the environment preparation
 * phase. In this second phase setting up security and the (optional) creation
 * of some transfer client takes place. As a result the link to the transfer
 * client is added to the property map.</li> <li>Publishing: In the publishing
 * phase relevant information stored within the property map is persisted. In
 * the simplest case the transfer client URL generated in the second phase is
 * set for the prepared transfer and the transfer status is changed
 * accordingly.</li> </ul> Finally, if everything worked as expected, all
 * persisted entities should be updated and all properties added during
 * preparation should be contained within the property map provided while
 * calling prepareTransfer().
 *
 * @author jejkal
 * @param <C> Class extending ITransferStatus.
 * @param <D> Class extending ITransferInformation&lt;C&gt>
 */
public abstract class AbstractTransferPreparationHandler<C extends ITransferStatus, D extends ITransferInformation<C>> {

  /**
   * The logger
   */
  private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTransferPreparationHandler.class);
  /**
   * Persistence implementation for the associated transfer status class
   */
  private ITransferInformationPersistence<C, D> persistence = null;
  /**
   * The transfer status entity which will be prepared for transfer
   */
  private D transferEntity = null;

  /**
   * Default constructor for creating a new transfer preparation handler
   *
   * @param pPersistence Persistence implementation to update the ingest status
   * @param pIngestInfo Information about the ingest to prepare
   */
  public AbstractTransferPreparationHandler(ITransferInformationPersistence<C, D> pPersistence, D pIngestInfo) {
    if (pPersistence == null || pIngestInfo == null) {
      throw new IllegalArgumentException("Neither 'pPersistence' nor 'pIngestInfo' argument must be 'null'");
    }
    persistence = pPersistence;
    transferEntity = pIngestInfo;
  }

  /**
   * Method responsible for the entire transfer preparation. Performed steps
   * are:
   *
   * <ul> <li>Internal preparation via setupInternal() including external
   * preparation via setup()</li> <li>Internal environment preparation via
   * prepareEnvironmentInternal() including external preparation via
   * prepareEnvironment()</li> <li>Internal client access preparation via
   * prepareClientAccessInternal() including external preparation via
   * prepareClientAccess()</li> <li>Publishing of transfer information by
   * updating the ingest entity associated with this transfer</li> </ul>
   *
   * @param pProperties Properties needed to prepare the transfer
   * @param pSecurityContext The security context used for authorization or to
   * obtain information about preferred transfer clients/protocols
   *
   * @throws TransferPreparationException If the transfer preparation fails
   */
  public final void prepareTransfer(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    setupInternal(pProperties);
    prepareEnvironmentInternal(pProperties, pSecurityContext);
    prepareClientAccessInternal(pProperties, pSecurityContext);
    publishTransferInformationInternal(pProperties, pSecurityContext);
  }

  /**
   * Performs the internal setup for this preparation handler. It sets up this
   * transfer client preparation handler using a map of key-value pairs.
   * Finally, setup() is called to perform external setup steps.
   *
   * @param pProperties All properties needed to configure the transfer
   * preparation handler
   *
   * @throws TransferPreparationException If the preparation fails
   */
  private void setupInternal(TransferClientProperties pProperties) throws TransferPreparationException {
    if (pProperties == null) {
      throw new IllegalArgumentException("Argument pProperties must not be 'null'");
    }
    String stagingPU = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit");
    //try to use AccessPoint id...probably user comes via REST interface
    String accessPointId = pProperties.getStagingAccessPointId();
    StagingAccessPointConfiguration accessPoint = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByUniqueIdentifier(accessPointId);
    if (accessPoint == null) {
      throw new TransferPreparationException("No AccessPoint found for id " + accessPointId);
    }

    LOGGER.debug("Triggering external setup.");
    setup(pProperties);
  }

  /**
   * This setup method is called after the internal setup process has finished.
   * This method can be implemented to perform customized setup steps or to
   * forward any information to the following preparation steps.
   *
   * @param pProperties Properties which are provided while calling
   * prepareTransfer(), forwarded to the internal setup and finally to this
   * customized setup.
   *
   * @throws TransferPreparationException If there is any problem which
   * necessitates the abort of the entire transfer preparation
   */
  public abstract void setup(TransferClientProperties pProperties) throws TransferPreparationException;

  /**
   * The internal implementation of the environment preparation phase. Within
   * this step, destination directories should be created and all security
   * related properties should should be set up by the Staging service. The call
   * prepareIngest() will return the transfer destination if everything
   * succeeded or 'null' in case of an error. If the preparation succeeded, the
   * property STAGING_URL_KEY will be set inside the properties pProperties and
   * can be used during prepareClientAccessInternal() to setup the access
   * client.
   *
   * @param pProperties The properties to prepare the transfer
   * @param pSecurityContext The security context used for authorization or to
   * obtain information about preferred transfer clients/protocols
   *
   * @throws TransferPreparationException If the transfer preparation fails
   */
  private void prepareEnvironmentInternal(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    String preferredAccessPoint = pProperties.getStagingAccessPointId();
    if (preferredAccessPoint == null) {
      throw new TransferPreparationException("Transfer preparation failed (Cause: No staging AccessPoint ID set in transfer properties.");
    }

    LOGGER.debug("Using staging service to prepare ingest with id '{}'", getTransferInformation().getDigitalObjectId());

    StagingPreparationResult result;
    if (isIngest()) {
      result = StagingService.getSingleton().prepareIngest(new DigitalObjectId(getTransferInformation().getDigitalObjectId()), preferredAccessPoint, pSecurityContext);
    } else {
      result = StagingService.getSingleton().scheduleDownload(new DigitalObjectId(getTransferInformation().getDigitalObjectId()), preferredAccessPoint, pSecurityContext);
    }

    if (result.getStatus().equals(INGEST_STATUS.PRE_INGEST_SCHEDULED) || result.getStatus().equals(DOWNLOAD_STATUS.SCHEDULED)) {
      //SUCCESS, add result to properties!
      pProperties.setStagingUrl(result.getStagingUrl());
      LOGGER.debug("Transfer successfully prepared, preparing environment");
      prepareEnvironment(pProperties, pSecurityContext);
    } else {//update status here to set the detailed error message
      getPersistence().updateStatus(getTransferInformation().getId(), (C) result.getStatus(), result.getErrorMessage(), pSecurityContext);
      throw new TransferPreparationException("Transfer preparation failed (Cause: " + result.getErrorMessage() + ". Please see logfile for details.");
    }
  }

  /**
   * Prepare the environment which is needed to perform the transfer operation.
   * This may include creating directories, setting up security or adding static
   * properties to pProperties.
   *
   * @param pProperties Properties object used to receive and to pass properties
   * needed by any step of transfer preparation
   * @param pSecurityContext The security context needed to authorize access or
   * to get context specific properties
   *
   * @throws TransferPreparationException If the extended environment
   * preparation fails
   */
  public abstract void prepareEnvironment(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException;

  /**
   * Prepare the client access using the transfer client preparation handler
   * obtained by getTransferClientPreparationHandler(). The handler was set
   * during internal setup and will provide a transfer client, accessible via
   * URL. The URL is added to pProperties and can be accessed via the key
   * AbstractTransferClientPreparationHandler.TRANSFER_URL_KEY afterwards.
   *
   * @param pProperties Properties object used to receive and to pass properties
   * needed by any step of transfer preparation
   * @param pSecurityContext The security context needed to authorize access or
   * to get context specific properties
   *
   * @throws TransferPreparationException If the preparation of the transfer
   * client fails
   */
  private void prepareClientAccessInternal(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    LOGGER.debug("No internal preparation necessary.");
  }

  /**
   * Prepare the actual client access. This call should add some property
   * containing a transfer URL used for later data transfer to pProperties. This
   * property has to be set at the transfer information entity during
   * prepareClientAccess().
   *
   * @param pProperties Properties object used to receive and to pass properties
   * needed by any step of transfer preparation
   * @param pSecurityContext The security context needed to authorize access or
   * to get context specific properties
   *
   * @throws TransferPreparationException If the preparation fails for some
   * reasons.
   */
  public abstract void prepareClientAccess(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException;

  /**
   * The internal implementation of the publish phase.
   *
   * @param pProperties The transfer client properties.
   * @param pSecurityContext The security context.
   *
   * @throws TransferPreparationException If the publication fails.
   */
  private void publishTransferInformationInternal(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    String stagingUrl = pProperties.getStagingUrl();
    if (stagingUrl == null) {
      throw new TransferPreparationException("Unable to publish transfer information", new PropertyNotFoundException(TransferClientPropertiesUtils.STAGING_URL_KEY));
    } else {
      LOGGER.info("Updating staging url for object with ID {} to {}", new Object[]{getTransferInformation().getDigitalObjectId(), stagingUrl});
      getPersistence().updateStagingUrl(getTransferInformation().getId(), stagingUrl, pSecurityContext);
    }

    String transferClientUrl = pProperties.getTransferClientUrl();
    if (transferClientUrl == null) {
      //throw new TransferPreparationException("Unable to publish transfer information", new PropertyNotFoundException(TransferClientPropertiesUtils.CLIENT_ACCESS_URL_KEY));
      LOGGER.info("No transfer client URL found. Probably, direct access is configured.");
    } else {
      LOGGER.info("Updating client access url for object with ID {} to {}", new Object[]{getTransferInformation().getDigitalObjectId(), transferClientUrl});
      getPersistence().updateClientAccessUrl(getTransferInformation().getId(), transferClientUrl, pSecurityContext);
    }

    publishTransferInformation(pProperties, pSecurityContext);
  }

  /**
   * Publish all transfer information obtained during prepareEnvironment() and
   * prepareClientAccess(). At least there has to be one transfer URL which must
   * be set at the transfer information entity and persisted via the persistence
   * implementation.
   *
   * @param pProperties Properties object used to receive and to pass properties
   * needed by any step of transfer preparation
   * @param pSecurityContext The security context needed to authorize access or
   * to get context specific properties. As the transfer information entity is
   * linked to the current context, this context has also to be used for
   * persisting the modified transfer information entity.
   *
   * @throws TransferPreparationException If the extended publishing of transfer
   * information fails
   */
  public abstract void publishTransferInformation(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException;

  /**
   * Get the currently used persistence implementation
   *
   * @return The persistence implementation which can be used to persist
   * transfer information entities
   */
  protected final ITransferInformationPersistence<C, D> getPersistence() {
    return persistence;
  }

  /**
   * Get the transfer information entity which is currently prepared by this
   * preparation handler
   *
   * @return The transfer information entity
   */
  public final D getTransferInformation() {
    return transferEntity;
  }

  /**
   * Check if the underlaying transfer is an ingest. Otherwise, it should be a
   * download.
   *
   * @return TRUE = The transfer is an ingest.
   */
  public boolean isIngest() {
    return (transferEntity instanceof IngestInformation);
  }
}
