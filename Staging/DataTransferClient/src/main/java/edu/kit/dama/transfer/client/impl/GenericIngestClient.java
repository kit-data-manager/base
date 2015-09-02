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

import edu.kit.dama.transfer.client.interfaces.IIngestCallback;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.transfer.client.util.TransferHelper;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 *
 */
public class GenericIngestClient extends AbstractTransferClient {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericIngestClient.class);
  /**
   * Ingest callback listener.
   */
  private List<IIngestCallback> ingestCallbackListeners = null;

  /**
   * Default constructor to ingest data with the transfer ID pTransferId to the
   * remote destination pDestination. The source directory has to be set
   * manually prior to starting the transfer
   *
   * @param pContainer The container which contains all transfer information.
   */
  public GenericIngestClient(TransferTaskContainer pContainer) {
    super(pContainer);
    ingestCallbackListeners = new LinkedList<IIngestCallback>();
  }

  /**
   * Add the provided ingest callback listener.
   *
   * @param pListener The listener to add.
   */
  public final synchronized void addIngestCallbackListener(IIngestCallback pListener) {
    if (!ingestCallbackListeners.contains(pListener)) {
      ingestCallbackListeners.add(pListener);
    }
  }

  /**
   * Remove the provided ingest callback listener.
   *
   * @param pListener The listener to remove.
   */
  public final synchronized void removeIngestCallbackListener(IIngestCallback pListener) {
    ingestCallbackListeners.remove(pListener);
  }

  @Override
  public final boolean restoreTransfer(SimpleRESTContext pContext) {
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
  public final void performShutdown() {
    LOGGER.debug("Performing shutdown for ingest client");
    //create a checkpoint for re-starting
    if (!getStatus().equals(TRANSFER_STATUS.SUCCEEDED)) {
      createCheckpoint();
    }
    //store properties if the ingest has not finished or 
    //remove the upload directory if the upload has finished
    LOGGER.debug("Shutdown finished");
  }

  /**
   * Notify all listener that the ingest has started.
   */
  public final void fireIngestStartedEvents() {
    for (IIngestCallback listener : ingestCallbackListeners.toArray(new IIngestCallback[ingestCallbackListeners.size()])) {
      listener.ingestStarted(getTransferTaskContainer().getUniqueTransferIdentifier());
    }
  }

  /**
   * Notify all listener that the ingest is running.
   */
  public final void fireIngestRunningEvents() {
    for (IIngestCallback listener : ingestCallbackListeners.toArray(new IIngestCallback[ingestCallbackListeners.size()])) {
      listener.ingestRunning(getTransferTaskContainer().getUniqueTransferIdentifier());
    }
  }

  /**
   * Notify all listener that the ingest has finished.
   *
   * @param pResult TRUE = The ingest has finished successfully.
   */
  public final void fireIngestFinishedEvents(boolean pResult) {
    for (IIngestCallback listener : ingestCallbackListeners.toArray(new IIngestCallback[ingestCallbackListeners.size()])) {
      listener.ingestFinished(getTransferTaskContainer().getUniqueTransferIdentifier(), pResult);
    }
  }

  @Override
  public final void fireStatusChangedEvent(TRANSFER_STATUS pOldStatus, TRANSFER_STATUS pNewStatus) {
    switch (pNewStatus) {
      case RUNNING: {
        fireIngestStartedEvents();
        break;
      }
      case SUCCEEDED: {
        fireIngestFinishedEvents(true);
        break;
      }
      default: {
        fireIngestFinishedEvents(false);
        break;
      }
    }
  }

  @Override
  public final void fireTransferAliveEvent() {
    fireIngestRunningEvents();
  }

  @Override
  public boolean performStagingProcessors() throws StagingProcessorException {
    boolean result = false;
    for (AbstractStagingProcessor op : getStagingProcessors()) {
      if (isCanceled()) {
        LOGGER.debug("Transfer was canceled by the user, skipping all outstanding staging processors.");
        setStatus(TRANSFER_STATUS.CANCELED);
        break;
      }
      LOGGER.debug("Performing processor '{}' on current transfer task container", op.getName());
      op.performPreTransferProcessing(getTransferTaskContainer());

      //finish processor and schedule transfer 
      LOGGER.debug("Finishing processor '{}'", op.getName());
      op.finalizePreTransferProcessing(getTransferTaskContainer());

      if (isCanceled()) {
        LOGGER.debug("Transfer was canceled by the user, skipping all outstanding staging processors.");
        setStatus(TRANSFER_STATUS.CANCELED);
        break;
      }
    }
    if (!isCanceled()) {
      //all pre-transfer ops have finished successfully
      result = true;
    }
    return result;
  }
}
