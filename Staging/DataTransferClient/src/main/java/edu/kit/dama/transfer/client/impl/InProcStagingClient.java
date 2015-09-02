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
package edu.kit.dama.transfer.client.impl;

import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.dama.transfer.client.exceptions.PrepareTransferException;
import edu.kit.dama.transfer.client.interfaces.IStagingCallback;
import edu.kit.dama.transfer.client.util.TransferHelper;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmatically usable version of a transfer client for staging purposes.
 * Basically, this implementation includes both, an upload and a download
 * client. However, following the rules of an AbstractTransferClient
 * implementation, at least one side must be local, as third party transfers are
 * not supported at the moment.
 *
 * Basically, this client is used in two operation modes:
 *
 * The first mode is responsible for moving data from the archive to some
 * user-accessible location. In this case, the map-based transfer mode is used
 * as normally not all files of one data set are downloaded at once. The parent
 * service takes care of filtering the relevant files and it takes care to
 * create the according folder structure, so that the map-based transfer can be
 * performed.
 *
 * The second mode covers the transfer from some user-accessible location (e.g.
 * a upload cache) into the archive. In this case, all files located in the
 * cache will be copied to a defined archive location and the transfer client
 * takes care of creating the identical directory structure within the archive,
 * if required.
 *
 * However, in both cases one side of the transfer has to be local. Normally, it
 * is suggested that the archive is locally accessible to be flexible in terms
 * of ingest (e.g. ingest data directly from a third-party side).
 *
 * @author jejkal
 */
public class InProcStagingClient extends AbstractTransferClient {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(InProcStagingClient.class);
  /**
   * Staging callback listener
   */
  private List<IStagingCallback> stagingCallbackListeners = null;

  /**
   * Default constructor for ingest operations from some cache location into the
   * archive. During construction it is checked, whether the provided archive
   * folder or the data folder obtained by
   * pTransferInformtion.getDataFolderURL() is a local folder. If none of both
   * fulfills this requirement, an IllegalArgumentException is thrown, which is
   * also thrown if one of both cannot be checked for some reasons.
   *
   * @param pContainer The transfer task container.
   * @param pDestination The destination folder.
   */
  public InProcStagingClient(TransferTaskContainer pContainer, AbstractFile pDestination) {
    super(pContainer, pDestination);
    if (pDestination == null) {
      throw new IllegalArgumentException("Argument pDestination must not be null");
    }
    try {
      //the source in case of an ingest is the data folder URL
      //AbstractFile transferDataFolder = new AbstractFile(pTransferInformation.getDataFolderURL());
      //check provided archive folder
      if (!pDestination.isDirectory()) {
        throw new IllegalArgumentException("Destination " + pDestination + " is no directory");
      }
      //     setTargetFolder(pDestination);
    } catch (AdalapiException ex) {
      throw new IllegalArgumentException("Failed to assign destination from provided argument " + pDestination, ex);
    }

    stagingCallbackListeners = new LinkedList<IStagingCallback>();
  }

  /**
   * Add the provided staging callback listener.
   *
   * @param pListener The listener to add.
   */
  public final synchronized void addStagingCallbackListener(IStagingCallback pListener) {
    if (!stagingCallbackListeners.contains(pListener)) {
      stagingCallbackListeners.add(pListener);
    }
  }

  /**
   * Remove the provided staging callback listener.
   *
   * @param pListener The listener to remove.
   */
  public final synchronized void removeStagingCallbackListener(IStagingCallback pListener) {
    stagingCallbackListeners.remove(pListener);
  }

  @Override
  public boolean restoreTransfer(SimpleRESTContext pContext) {
    boolean result = false;
    // String identifier = getTransferTaskContainer().getUniqueTransferIdentifier();
    if (TransferHelper.canRestoreTransfer(getTransferTaskContainer())) {
      LOGGER.debug("Checkpoint found, tying to restore transfer.");
      TransferTaskContainer restored = TransferHelper.restoreTransfer(getTransferTaskContainer(), pContext);
      if (restored != null) {
        LOGGER.debug("Transfer successfully restored.");
        setTransferTaskContainer(restored);
        result = true;
      } else {
        LOGGER.warn("Failed to restore transfer. Proceeding.");
      }
    }
    return result;
  }

  @Override
  public boolean prepareTransfer() throws PrepareTransferException {
    //The following block is not needed here as it is covered by AbstractTransferClient.prepare()
   /* boolean result = true;
     try {
     LOGGER.debug("Restoring tree structure");
     DataOrganizationUtils.restoreTreeStructure(getTransferTaskContainer().getFileTree(), getDestination(), transfers);
     } catch (MalformedURLException ex) {
     LOGGER.error("Failed to prepare transfer. File tree could not be restored.", ex);
     result = false;
     } catch (IOException ex) {
     LOGGER.error("Failed to prepare transfer. File tree could not be restored.", ex);
     result = false;
     }

     Set<Map.Entry<StagingFile, StagingFile>> entries = transfers.entrySet();
     for (Map.Entry<StagingFile, StagingFile> entry : entries) {
     LOGGER.debug("Adding transfer task from {} to {}", new Object[]{entry.getKey().getAbstractFile(), entry.getValue().getAbstractFile()});
     addTransferTask(new TransferTask(entry.getKey().getAbstractFile(), entry.getValue().getAbstractFile()));
     }

     return result;*/
    return true;
  }

  @Override
  public void performShutdown() {
    LOGGER.debug("Performing shutdown for staging client");
    //create a checkpoint for re-starting
    createCheckpoint();
    //store properties if the staging has not finished or 
    LOGGER.debug("Shutdown finished");
  }

  @Override
  public void fireStatusChangedEvent(TRANSFER_STATUS pOldStatus, TRANSFER_STATUS pNewStatus) {
    switch (pNewStatus) {
      case RUNNING: {
        fireStagingStartedEvents();
        break;
      }
      case SUCCEEDED: {
        fireStagingFinishedEvents(true);
        break;
      }
      case PREPARING:
      case CLEANUP:
      case PENDING:
      case TRANSFERRING: {
        fireTransferAliveEvent();
        break;
      }
      default:
        fireStagingFinishedEvents(false);
    }
  }

  @Override
  public void fireTransferAliveEvent() {
    fireStagingRunningEvents();
  }

  /**
   * Notify all listener that the staging has started.
   */
  public final void fireStagingStartedEvents() {
    for (IStagingCallback listener : stagingCallbackListeners.toArray(new IStagingCallback[stagingCallbackListeners.size()])) {
      listener.stagingStarted(getTransferTaskContainer().getUniqueTransferIdentifier());
    }
  }

  /**
   * Notify all listener that the staging is running.
   */
  public final void fireStagingRunningEvents() {
    for (IStagingCallback listener : stagingCallbackListeners.toArray(new IStagingCallback[stagingCallbackListeners.size()])) {
      listener.stagingRunning(getTransferTaskContainer().getUniqueTransferIdentifier());
    }
  }

  /**
   * Notify all listener that the staging has finished.
   *
   * @param pResult TRUE = The staging has finished successfully.
   */
  public final void fireStagingFinishedEvents(boolean pResult) {
    for (IStagingCallback listener : stagingCallbackListeners.toArray(new IStagingCallback[stagingCallbackListeners.size()])) {
      listener.stagingFinished(getTransferTaskContainer().getUniqueTransferIdentifier(), pResult);
    }
  }

  @Override
  public boolean performStagingProcessors() throws StagingProcessorException {
    if (getStagingProcessors().length > 0) {
      throw new StagingProcessorException("Staging processors are currently not supported for this implementation.");
    } else {
      LOGGER.info("Staging processors are currently not supported for this implementation. As there are no processors registered, I'll return 'TRUE'.");
    }
    return true;
  }
}
