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
package edu.kit.dama.staging.processor;

import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.commons.types.IConfigurable;
import edu.kit.dama.staging.exceptions.StagingProcessorException;

/**
 * Abstract definition of an processor carried out before or after a transfer.
 * Currently, transfer processors are only supported for ingests. Typically,
 * staging processors will be only executed on the server side, e.g. for
 * metadata extraction or ingest validation. They are executed before the data
 * is copied to the archive, so processor-generated data can be archived, too. *
 * Each processor consists of several phases:
 * <ul>
 * <li>Configuration: In this phase the processor is instantiated and configured
 * using a properties object. This properties object can be expected as valid if
 * they can pass <i>validateProperties()</i>. This validation is performed out
 * in beforehand by the setup of the processor.</li>
 * <li>preTransferProcessing: This phase is intended to be used in special cases
 * and is carried out on the client side before the data transfer is started. It
 * is available for staging processors of type PROCESSOR_TYPE.CLIENT_SIDE_ONLY
 * or PROCESSOR_TYPE.CLIENT_AND_SERVER_SIDE. Pre-transfer processing requires a
 * special client implementation capable of executing staging processors. At
 * first,
 * <i>performPreTransferProcessing()</i> is called doing the actual processing,
 * afterwards <i>finalizePreTransferProcessing</i>
 * is called for finalization.</li>
 * <li>postTransferProcessing: This phase is carried out on the server side,
 * typically before the archiving starts (PROCESSOR_TYPE.SERVER_SIDE_ONLY) or
 * after archiving (PROCESSOR_TYPE.POST_ARCHIVING). At first,
 * <i>performPostTransferProcessing()</i>
 * is called doing the actual processing, afterwards
 * <i>finalizePostTransferProcessing</i> is called for finalization.</li>
 * </ul>
 *
 * A typical use case is the transfer validation using checksums (see
 * edu.kit.dama.rest.staging.client.processor.impl.InputHashOP). At first, the
 * hash method is configured before the transfer is scheduled. This
 * configuration is part of the TransferTaskContainer access from the transfer
 * client. The configuration is read, the processor is configured and the
 * checksums are generated during performPreTransferProcessing() and written to
 * a file with a defined name. This file is added to the TransferTaskContainer
 * and uploaded afterwards. On the server side, performPreTransferProcessing()
 * obtains the file, reads it and validates the content against the data
 * uploaded by the transfer client. If this step fails, an exception is thrown
 * and the transfer is expected to have failed.
 *
 * @author jejkal
 */
public abstract class AbstractStagingProcessor implements IConfigurable {

  private String uniqueIdentifier = null;

  /**
   * Default constructor.
   *
   * @param pUniqueIdentifier The unique identifier of this processor.
   */
  public AbstractStagingProcessor(String pUniqueIdentifier) {
    uniqueIdentifier = pUniqueIdentifier;
  }

  /**
   * Returns the unique identifier of this processor. The identifier is
   * basically used to identify output of the processor on the client/server
   * side.
   *
   * @return The unique identifier.
   */
  public String getUniqueIdentifier() {
    return uniqueIdentifier;
  }

  /**
   * Return the (possible unique) name of this processor.
   *
   * @return The name.
   */
  public abstract String getName();

  /**
   * Perform the pre-transfer processing using this processor for the transfer
   * described by pContainer. The pre-processing is done where the transfer
   * comes from, e.g. on the ingest machine for ingests or on the server machine
   * for downloads.
   *
   * @param pContainer The transfer task container which contains the file tree
   * on the transfer source machine. This should be locally and all files in the
   * tree should be accessible in a posix-like way.
   *
   * @throws StagingProcessorException If the processor fails.
   */
  public abstract void performPreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException;

  /**
   * Finalize the pre-transfer processing.
   *
   * @param pContainer The transfer task container which contains the file tree
   * on the transfer source machine. This should be locally and all files in the
   * tree should be accessible in a posix-like way.
   *
   * @throws StagingProcessorException If finishing the processor fails
   */
  public abstract void finalizePreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException;

  /**
   * Perform the post-transfer processing using this processor for the transfer
   * described by pContainer. The post-processing is done at the destination of
   * the transfer, e.g. on the server machine for ingests or on the user desktop
   * for downloads.
   *
   * @param pContainer The transfer task container which contains the file tree
   * on the transfer destination machine. This should be locally and all files
   * in the tree should be accessible in a posix-like way.
   *
   * @throws StagingProcessorException If the processor fails.
   */
  public abstract void performPostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException;

  /**
   * Finalize the post-transfer processing.
   *
   * @param pContainer The transfer task container which contains the file tree
   * on the transfer destination machine. This should be locally and all files
   * in the tree should be accessible in a posix-like way.
   *
   * @throws StagingProcessorException If finishing the processor fails.
   */
  public abstract void finalizePostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException;
}
