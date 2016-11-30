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
 * Abstract definition of a processor running on the server side carried out
 * before or after ingesting data into the repository. Typically, staging
 * processors will be used for metadata extraction or ingest validation. Each
 * processor consists of several phases:
 * <ul>
 * <li>Configuration: In this phase the processor is instantiated and configured
 * using a properties object. This properties object can be expected as valid if
 * they can pass <i>validateProperties()</i>. This validation is performed out
 * in beforehand by the setup of the processor.</li>
 * <li>preTransferProcessing: This phase executed before a data transfer is
 * started, which is before the data is transferred from the access point cache
 * to the repository storage. For staging processors applied to downloads this
 * phase is not available. For ingests keep in mind, that in this phase no data
 * organization is available, yet.
 * </li>
 * <li>postTransferProcessing: This phase is carried out after the data transfer
 * has successfully finished, e.g. after the data has been ingested into the
 * repository storage or the data has been obtained from the repository storage
 * and was place at the access point's caching location.
 * </ul>
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
     * described by pContainer. Pre-transfer processing is done before the
     * actual data transfer starts, e.g. before the uploaded data on the server
     * is ingested into the repository storage.
     *
     * @param pContainer The transfer task container which contains the file
     * tree on the transfer source machine. Typically, file nodes in the tree
     * should be accessible in a posix-like way. However, in special cases, e.g.
     * if data was uploaded to a remote location from where it is ingested into
     * the repository, file nodes may be accessible remotely.
     *
     *
     * @throws StagingProcessorException If the processor fails.
     */
    public abstract void performPreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException;

    /**
     * Finalize the pre-transfer processing.
     *
     * @param pContainer The transfer task container which contains the file
     * tree on the transfer source machine. Typically, file nodes in the tree
     * should be accessible in a posix-like way. However, in special cases, e.g.
     * if data was uploaded to a remote location from where it is ingested into
     * the repository, file nodes may be accessible remotely.
     *
     * @throws StagingProcessorException If finishing the processor fails
     */
    public abstract void finalizePreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException;

    /**
     * Perform the post-transfer processing using this processor for the
     * transfer described by pContainer. The post-processing is done after the
     * transfer has finished, e.g. after the data has been ingested into the
     * repository storage or after the data has been downloaded from the
     * repository storage.
     *
     * @param pContainer The transfer task container which contains the file
     * tree on the transfer source machine. Typically, file nodes in the tree
     * should be accessible in a posix-like way. However, in special cases, e.g.
     * if data was uploaded to a remote location from where it is ingested into
     * the repository, file nodes may be accessible remotely.
     *
     * @throws StagingProcessorException If the processor fails.
     */
    public abstract void performPostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException;

    /**
     * Finalize the post-transfer processing.
     *
     * @param pContainer The transfer task container which contains the file
     * tree on the transfer source machine. Typically, file nodes in the tree
     * should be accessible in a posix-like way. However, in special cases, e.g.
     * if data was uploaded to a remote location from where it is ingested into
     * the repository, file nodes may be accessible remotely.
     *
     * @throws StagingProcessorException If finishing the processor fails.
     */
    public abstract void finalizePostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException;
}
