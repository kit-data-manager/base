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
import edu.kit.dama.transfer.client.util.TransferHelper;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class GenericDownloadClient extends AbstractTransferClient {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericDownloadClient.class);

  /**
   * Default constructor to download data defined by the provided TaskContainer
   * to pTargetFolder.
   *
   * @param pTaskContainer The transfer task container.
   * @param pTargetFolder The target folder which is the destination for the
   * download.
   */
  public GenericDownloadClient(TransferTaskContainer pTaskContainer, AbstractFile pTargetFolder) {
    super(pTaskContainer, pTargetFolder);
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
    LOGGER.debug("Performing shutdown for download client");
    //create a checkpoint for re-starting
    if (!getStatus().equals(TRANSFER_STATUS.SUCCEEDED)) {
      createCheckpoint();
    }
    //store properties if the download has not finished or 
    //remove the download directory if the upload has finished
    LOGGER.debug("Shutdown finished");
  }

  @Override
  public final void fireStatusChangedEvent(TRANSFER_STATUS pOldStatus, TRANSFER_STATUS pNewStatus) {
    LOGGER.debug("Download status has changed from {} to {}", new Object[]{pOldStatus, pNewStatus});
  }

  @Override
  public final void fireTransferAliveEvent() {
    LOGGER.debug("Download still running...");
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
