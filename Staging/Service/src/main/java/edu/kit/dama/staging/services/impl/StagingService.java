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
package edu.kit.dama.staging.services.impl;

import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import edu.kit.dama.staging.util.MailNotificationHelper;
import edu.kit.dama.authorization.entities.GroupId;
import java.net.MalformedURLException;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.entities.StagingFile;
import edu.kit.dama.staging.entities.StagingPreparationResult;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileTreeImpl;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.exceptions.ServiceAdapterException;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.handlers.impl.DownloadPreparationHandler;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.staging.interfaces.IStorageVirtualizationServiceAdapter;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.staging.util.StagingConfigurationManager;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import edu.kit.dama.util.SystemUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation provides a basic staging service responsible for managing
 * ingest and download operations. The service is based on a two-step process.
 *
 * <b>Download</b> In a first step the data is moved to a caching location which
 * is defined by the according access handler. Data will be located in a folder
 * named 'data', transfer-related settings will be stored under 'settings'.
 *
 * The user can access the caching location via high-performance transfer
 * clients supporting many transfer protocols and security methods or other,
 * flexible methods. If this data movement is finished, the user can be notified
 * via mail or the information is published within some user interface. Then the
 * user can download the data.
 *
 * <b>Ingest</b> In case of an ingest, a new folder will be created at the
 * caching location. Afterwards, an upload client will be made available to
 * upload the data by the user. If the upload has finished (what is either
 * triggered by the upload client or by the user), the status of the ingest will
 * change. This service will query all ingests periodically to recognize
 * finished uploads. These are archived and registered within the system.
 *
 * @author Thomas Jejkal <a>mailto:support@kitdatamanager.net</a>
 */
public final class StagingService {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StagingService.class);
    private static StagingService singleton = null;
    private final static String DOI_NULL_ERROR = "Argument 'pDigitalObjectId' must not be null";
    private final static String CTX_NULL_ERROR = "Argument 'pContext' must not be null";
    private final static String IIS_ACCESS_ERROR = "Failed to access IngestInformationService.";
    private final static String DIS_ACCESS_ERROR = "Failed to access DownloadInformationService.";

    /**
     * Returns the StagingService instance
     *
     * @return The singleton instance of the StagingService
     */
    public static synchronized StagingService getSingleton() {
        if (singleton == null) {
            singleton = new StagingService();
        }
        return singleton;
    }

    /**
     * Default constructor which performs the configuration of the staging
     * service. If the configuration fails, this constructor call will also fail
     * throwing a RuntimeException
     */
    StagingService() {
        LOGGER.debug("Initializing staging service");
        StagingConfigurationManager.getSingleton();
    }

    /**
     * Return the authorization context which can be used to access the provided
     * transfer information.
     *
     * @param pInfo The transfer information.
     *
     * @return The authorization context.
     */
    public final IAuthorizationContext getContext(ITransferInformation pInfo) {
        LOGGER.debug("Building authorization context for user id {} in group {} with role MEMBER", new Object[]{pInfo.getOwnerId(), pInfo.getGroupId()});
        return new AuthorizationContext(new UserId(pInfo.getOwnerId()), new GroupId(pInfo.getGroupId()), Role.MEMBER);
    }

    /**
     * Get the local staging folder for the provided transfer information and
     * context. The staging folder is obtained from the
     * {@link AbstractStagingAccessPoint} used for pTransferInfo.
     *
     * @param pTransferInfo The transfer information containing the staging
     * AccessPoint id.
     * @param pContext The context allowed to access the transfer information.
     *
     * @return The local staging folder.
     */
    public final File getLocalStagingFolder(ITransferInformation pTransferInfo, IAuthorizationContext pContext) {
        AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(pTransferInfo.getAccessPointId());
        if (accessPoint == null) {
            throw new IllegalArgumentException("Unable to find access point for access point id '" + pTransferInfo.getAccessPointId() + "'");
        }
        URL localAccessUrl = accessPoint.getAccessUrl(pTransferInfo, pContext);
        return accessPoint.getLocalPathForUrl(localAccessUrl, pContext);
    }

    /**
     * Helper method to update the status of the provided transfer.
     *
     * @param pInfo The modified entity.
     *
     * @return TRUE if everything is fine.
     */
    private boolean updateTransferStatus(ITransferInformation pInfo) {
        boolean result = true;
        String errorString = IIS_ACCESS_ERROR;
        try {
            if (pInfo instanceof IngestInformation) {
                errorString = IIS_ACCESS_ERROR;
                StagingConfigurationManager.getSingleton().getIngestInformationServiceAdapter().updateIngestInformation((IngestInformation) pInfo, getContext(pInfo));
            } else if (pInfo instanceof DownloadInformation) {
                errorString = DIS_ACCESS_ERROR;
                StagingConfigurationManager.getSingleton().getDownloadInformationServiceAdapter().updateDownloadInformation((DownloadInformation) pInfo, getContext(pInfo));
            } else {
                throw new IllegalArgumentException("Argument pInfo is no instance of IngestInformation or DownloadInformation");
            }
        } catch (ServiceAdapterException ex) {
            LOGGER.error(errorString, ex);
            result = false;
        }
        return result;
    }

    /**
     * Schedule a transfer for deletion by marking is as 'deleted'. Therefor, a
     * file is created inside the staging folder to be able to "undelete" later
     * as long as the physical deletion has not been performed.
     *
     * @param pTransfer The transfer to delete.
     *
     * @return TRUE if the transfer was deleted.
     */
    public boolean deleteTransfer(ITransferInformation pTransfer) {
        boolean result;
        LOGGER.debug("Obtaining staging AccessPoint for id {}", pTransfer.getAccessPointId());
        AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(pTransfer.getAccessPointId());
        LOGGER.debug("Cleaning up transfer with id {}.", pTransfer.getTransferId());
        result = accessPoint.prepareCleanup(pTransfer, getContext(pTransfer));
        if (result) {
            //cleanup prepared, update status      
            LOGGER.debug("Cleanup successfully prepared, updating transfer status.");
            if (pTransfer instanceof DownloadInformation) {
                pTransfer.setStatusEnum(DOWNLOAD_STATUS.DOWNLOAD_REMOVED);
                result = updateTransferStatus(pTransfer);
            } else if (pTransfer instanceof IngestInformation) {
                pTransfer.setStatusEnum(INGEST_STATUS.INGEST_REMOVED);
                result = updateTransferStatus(pTransfer);
            } else {
                throw new IllegalArgumentException("Invalid ITransferInformation entity of type " + pTransfer.getClass() + ".");
            }
        }
        return result;
    }

    /**
     * Check if the provided transfer is prepared for deletion.
     *
     * @param pTransfer The transfer to check.
     *
     * @return TRUE if the transfer is prepared for deletion.
     */
    public boolean isTransferDeleted(ITransferInformation pTransfer) {
        boolean result;
        LOGGER.debug("Obtaining staging folder for transfer with id '{}'", pTransfer.getTransferId());
        File stagingFolder = getLocalStagingFolder(pTransfer, getContext(pTransfer));
        if (stagingFolder.exists()) {
            File deleteFile = new File(stagingFolder, Constants.STAGING_DELETED_FILENAME);
            result = deleteFile.exists();
        } else {
            LOGGER.info("Staging folder was not found. Probably the transfer was already deleted.");
            result = true;
        }
        return result;
    }

    //<editor-fold defaultstate="collapsed" desc=" Ingest Preparation (Interactive)">
    /**
     * Prepares the ingest for the digital object with the provided digital
     * object ID. This method will be called by the transfer preparation handler
     * if the user requests an ingest. The method takes care, that there is an
     * upload folder existing after this call, which is writeable by the calling
     * user. This folder is stored at the preparation result entity returned by
     * this method, together with the appropriate status. The update of the
     * ingest information entity within the data backend is done by the
     * preparation handler.
     *
     * @param pDigitalObjectId The id of the digital object to ingest.
     * @param pAccessPointId The If of the AccessPoint used for ingest. It
     * defines access protocol and target folder used to upload data.
     * @param pContext The authorization context of the user who requested the
     * ingest
     *
     * @return A StagingPreparationResult object containing the result (e.g.
     * status, error message and/or staging URL)
     */
    public StagingPreparationResult<INGEST_STATUS> prepareIngest(DigitalObjectId pDigitalObjectId, String pAccessPointId, IAuthorizationContext pContext) {
        if (pDigitalObjectId == null) {
            throw new IllegalArgumentException(DOI_NULL_ERROR);
        }

        if (pContext == null) {
            throw new IllegalArgumentException(CTX_NULL_ERROR);
        }

        if (!StagingConfigurationManager.getSingleton().isExistingAccessPoint(pAccessPointId)) {
            throw new IllegalArgumentException("Provided AccessPoint '" + pAccessPointId + "' is not configured");
        }

        LOGGER.debug("Preparing ingest for ID '{}'", pDigitalObjectId);
        LOGGER.debug(" Context: {}/{}", new Object[]{pContext.getUserId().getStringRepresentation(), pContext.getGroupId().getStringRepresentation()});

        //get associated ingest information entity
        LOGGER.debug("Obtaining ingest information");
        IngestInformation ingest;
        StagingPreparationResult result = new StagingPreparationResult();

        try {
            ingest = StagingConfigurationManager.getSingleton().getIngestInformationServiceAdapter().getIngestInformation(pDigitalObjectId, pContext);
        } catch (ServiceAdapterException sae) {
            LOGGER.error(IIS_ACCESS_ERROR, sae);
            result.setStatus(INGEST_STATUS.PREPARATION_FAILED);
            result.setErrorMessage(IIS_ACCESS_ERROR);
            return result;
        }

        if (ingest == null) {
            LOGGER.error("Failed to obtain valid ingest information for object '{}'", pDigitalObjectId);
            result.setStatus(INGEST_STATUS.PREPARATION_FAILED);
            result.setErrorMessage("Failed to obtain valid ingest information for object " + pDigitalObjectId + ".");
            return result;
        }

        if (!ingest.getStatusEnum().equals(INGEST_STATUS.PREPARING)) {
            //ingest is not in preparing state...check which state it has, print appropriate log message and return
            if (ingest.getStatusEnum().equals(INGEST_STATUS.PRE_INGEST_SCHEDULED)) {//the ingest is already prepared...return TRUE
                LOGGER.info("Pre-Ingest is already scheduled for the object with ID '{}'", pDigitalObjectId);
                result.setStatus(INGEST_STATUS.PRE_INGEST_SCHEDULED);
                result.setStagingUrl(ingest.getStagingUrl());
                return result;
            } else {//other status...probably an error status
                LOGGER.warn("Object with ID '{}' has status {}. Preparation not possible/necessary.", new Object[]{pDigitalObjectId, ingest.getStatusEnum().toString()});
                result.setStatus(ingest.getStatusEnum());
                result.setErrorMessage(ingest.getErrorMessage());
                return result;
            }
        }//ingest is allowed beeing prepared

        //get transfer ID for logging purposes
        String transferId = ingest.getTransferId();
        LOGGER.debug("Successfully obtained ingest information with transfer ID '{}'. Generating staging access...", transferId);
        AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(pAccessPointId);

        try {
            accessPoint.prepare(ingest, pContext);
            URL ingestUrl = accessPoint.getAccessUrl(ingest, pContext);
            result.setStatus(INGEST_STATUS.PRE_INGEST_SCHEDULED);
            result.setStagingUrl(ingestUrl.toString());
        } catch (TransferPreparationException ex) {
            LOGGER.error("Failed to prepare staging access for ingest with id " + transferId, ex);
            result.setStatus(INGEST_STATUS.PREPARATION_FAILED);
            result.setErrorMessage("Internal error. Failed to setup staging access.");
        }

        return result;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Ingest Finalization (Automatic)">
    /**
     * This method finalizes the ingest for the provided digital object id. This
     * method can be called directly, e.g. as administrative using a user
     * interface, or by automated processes triggering transfer finalization. It
     * is recommended to use it in an asynchronous way via external tools, as a
     * call to this method is blocking and may take a long time.
     *
     * An ingest can be finalized if it has one of the status code
     * INGEST_STATUS.PRE_INGEST_FINISHED,INGEST_STATUS.PRE_INGEST_RUNNING or
     * INGEST_STATUS.PRE_INGEST_SCHEDULED. IF this is the case, the state is set
     * to INGEST_STATUS.INGEST_RUNNING. During the finalization the following
     * steps are performed:
     * <ul>
     * <li>Obtain the location of the data, depending on the AccessPoint used
     * for the ingest.</li>
     * <li>Obtain the file tree and create a {@link TransferTaskContainer} for
     * further processing.</li>
     * <li>Perform all server sided staging processors registered for the
     * ingest.</li>
     * <li>Copy all files to the archive location using the configured
     * {@link IStorageVirtualizationServiceAdapter}.</li>
     * </ul>
     *
     * @param pId The digital object id of the ingest to finalize.
     * @param pContext The context which is used to access the ingest. This
     * context is only used for the initial query. Ingest-related access is done
     * using the context stored in the obtained ingest entity.
     *
     * @return TRUE if the ingest could be finilized.
     */
    public boolean finalizeIngest(DigitalObjectId pId, IAuthorizationContext pContext) {
        LOGGER.debug("Finalizing ingest for object '{}'", pId);
        IngestInformation ingest;
        try {
            ingest = StagingConfigurationManager.getSingleton().getIngestInformationServiceAdapter().getIngestInformation(pId, pContext);
        } catch (ServiceAdapterException sae) {
            LOGGER.error(IIS_ACCESS_ERROR, sae);
            return false;
        }

        if (!ingest.getStatusEnum().equals(INGEST_STATUS.PRE_INGEST_FINISHED) && !ingest.getStatusEnum().equals(INGEST_STATUS.PRE_INGEST_RUNNING) && !ingest.getStatusEnum().equals(INGEST_STATUS.PRE_INGEST_SCHEDULED)) {
            LOGGER.error("Finalization of ingest impossible. Status of ingest '" + pId + "' is " + ingest.getStatusEnum() + ", but has to be PRE_INGEST_FINISHED or PRE_INGEST_RUNNING or PRE_INGEST_SCHEDULED");
            return false;
        }

        LOGGER.debug("Setting ingest status to INGEST_RUNNING");
        ingest.setStatusEnum(INGEST_STATUS.INGEST_RUNNING);
        if (!updateTransferStatus(ingest)) {
            LOGGER.error("Failed to update ingest status to {}. Finalizing impossible.", INGEST_STATUS.INGEST_RUNNING);
            return false;
        }

        LOGGER.debug("Performing ingest finalization");
        boolean result = true;

        try {
            //obtain the local path where this ingest's files (user data) are located
            URL stagingUrl = new URL(ingest.getStagingUrl());
            LOGGER.debug("Obtaining AccessPoint with id  '{}'", ingest.getAccessPointId());
            AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(ingest.getAccessPointId());

            LOGGER.debug("Obtaining local path for staging URL '{}'", stagingUrl.toString());
            File localPath = accessPoint.getLocalPathForUrl(stagingUrl, getContext(ingest));

            if (localPath == null) {
                LOGGER.error("Failed to obtain local path for staging URL '{}'", stagingUrl);
                //cannot be handled here...log error and hope for administrator
                ingest.setStatus(INGEST_STATUS.INGEST_FAILED.getId());
                ingest.setErrorMessage("Internal error. Failed to obtain local ingest path.");
                result = false;
            } else {
                LOGGER.debug("Local path is {}. Try locking path for further write operations.", localPath);

                if (SystemUtils.lockFolder(localPath)) {
                    LOGGER.info("Ingest folder successfully locked.");
                } else {
                    LOGGER.warn("Failed to lock ingest folder.");
                }

                LOGGER.debug("Creating file tree and transfer container.");
                IFileTree tree = DataOrganizationUtils.createTreeFromFile(ingest.getDigitalObjectId(), new AbstractFile(localPath), localPath.toURI().toURL(), false);
                TransferTaskContainer container = TransferTaskContainer.factoryIngestContainer(ingest, tree, StagingConfigurationManager.getSingleton().getRestServiceUrl());
                container.setDestination(localPath.toURI().toURL());
                LOGGER.debug("Transfer container successfully created.");
                boolean preProcessingSucceeded = true;
                LOGGER.debug("Executing {} staging processors", ingest.getServerSideStagingProcessor().length);
                for (StagingProcessor processor : ingest.getServerSideStagingProcessor()) {
                    if (processor.isDisabled()) {
                        LOGGER.info("Pre-Archiving StagingProcessor with id {} is disabled. Skipping execution.", processor.getUniqueIdentifier());
                        continue;
                    }

                    try {
                        LOGGER.debug(" - Try to execute processor {} ({})", new Object[]{processor.getName(), processor.getUniqueIdentifier()});
                        AbstractStagingProcessor sProcessor = processor.createInstance();
                        LOGGER.debug(" - Executing processor for local path {}", localPath.getAbsolutePath());
                        sProcessor.performPostTransferProcessing(container);
                        LOGGER.debug(" - Finishing processor execution");
                        sProcessor.finalizePostTransferProcessing(container);
                        LOGGER.debug(" - Processor successfully executed");
                    } catch (ConfigurationException ex) {
                        LOGGER.error("Failed to configure StagingProcessor " + processor.getName() + " (" + processor.getUniqueIdentifier() + ")", ex);
                        ingest.setStatus(INGEST_STATUS.INGEST_FAILED.getId());
                        ingest.setErrorMessage("Internal error. Failed to configure StagingProcessor '" + processor.getName() + "'.");
                        preProcessingSucceeded = false;
                        result = false;
                    } catch (StagingProcessorException ex) {
                        LOGGER.error("Failed to perform StagingProcessor " + processor.getName() + " (" + processor.getUniqueIdentifier() + ")", ex);
                        ingest.setStatus(INGEST_STATUS.INGEST_FAILED.getId());
                        ingest.setErrorMessage("Internal error. Failed to execute StagingProcessor '" + processor.getName() + "'.");
                        preProcessingSucceeded = false;
                        result = false;
                    }
                }

                //now close the container as all additional files are added
                container.close();
                //continue after preprocessing
                if (preProcessingSucceeded) {
                    //try to perform staging
                    LOGGER.debug("Perform staging for folder '{}'", localPath);
                    LOGGER.debug(" - Writing data to archive");
                    //ingest files and get file tree as a result

                    IFileTree virtualizedFileTree = StagingConfigurationManager.getSingleton().getStorageVirtualizationAdapter().store(container, getContext(ingest));
                    if (virtualizedFileTree == null) {
                        LOGGER.error("Writing to archive failed, no valid data organization was returned");
                        ingest.setStatus(INGEST_STATUS.INGEST_FAILED.getId());
                        ingest.setErrorMessage("Internal error. Failed to store data.");
                    } else {
                        LOGGER.debug(" - Registering data organization");

                        LOGGER.debug("Obtaining 'data' node.");
                        IDataOrganizationNode dataNode = Util.getNodeByName(virtualizedFileTree.getRootNode(), Constants.STAGING_DATA_FOLDER_NAME);
                        LOGGER.debug("Obtaining 'generated' node.");
                        IDataOrganizationNode generatedNode = Util.getNodeByName(virtualizedFileTree.getRootNode(), Constants.STAGING_GENERATED_FOLDER_NAME);
                        boolean dataStored;
                        if (dataNode == null || !(dataNode instanceof ICollectionNode)) {
                            LOGGER.warn("No valid 'data' node found. Expecting non-default tree structure and registering entire tree.");
                            dataStored = StagingConfigurationManager.getSingleton().getDataOrganizationAdapter().saveFileTree(virtualizedFileTree);
                        } else {
                            LOGGER.debug("Creating data tree.");
                            IFileTree dataTree = new FileTreeImpl();
                            dataTree.setDigitalObjectId(pId);
                            dataTree.setViewName(Constants.DEFAULT_VIEW);
                            for (IDataOrganizationNode childNode : ((ICollectionNode) dataNode).getChildren()) {
                                LOGGER.debug("Adding child {} to data tree.", childNode.getName());
                                dataTree.getRootNode().addChild(DataOrganizationUtils.copyNode(childNode, false));
                            }
                            LOGGER.debug("Storing data tree.");
                            dataStored = StagingConfigurationManager.getSingleton().getDataOrganizationAdapter().saveFileTree(dataTree);
                        }

                        if (dataStored) {
                             LOGGER.debug("Successfully stored 'data' view.");
                            if (generatedNode == null || !(generatedNode instanceof ICollectionNode) || ((ICollectionNode) generatedNode).getChildren().isEmpty()) {
                                LOGGER.info("Generated node not found or is empty. Skip registering view 'generated'.");
                            } else {
                                IFileTree generatedTree = new FileTreeImpl();
                                generatedTree.setDigitalObjectId(pId);
                                generatedTree.setViewName("generated");
                                for (IDataOrganizationNode childNode : ((ICollectionNode) generatedNode).getChildren()) {
                                    generatedTree.getRootNode().addChild(DataOrganizationUtils.copyNode(childNode, false));
                                }
                                if (StagingConfigurationManager.getSingleton().getDataOrganizationAdapter().saveFileTree(generatedTree)) {
                                    LOGGER.debug("Successfully stored 'generated' view.");
                                } else {
                                    LOGGER.error("Failed to stored 'generated' view.");
                                }
                            }

                            LOGGER.debug(" - Data organization successfully stored. Updating ingest status.");
                            ingest.setStatus(INGEST_STATUS.INGEST_FINISHED.getId());
                            ingest.setErrorMessage(null);
                            //Perform post-archive processors as soon as archiving has finished. Normally, these processors should not produce errors as the archiving is alread finished.
                            //Therefore, errors are logged but won't result in a failed ingest any longer.
                            LOGGER.debug("Executing {} post-archiving staging processors", ingest.getPostArchivingStagingProcessor().length);
                            for (StagingProcessor processor : ingest.getPostArchivingStagingProcessor()) {
                                if (processor.isDisabled()) {
                                    LOGGER.info("Post-Archiving StagingProcessor with id {} is disabled. Skipping execution.", processor.getUniqueIdentifier());
                                    continue;
                                }
                                try {
                                    LOGGER.debug(" - Try to execute processor {} ({})", new Object[]{processor.getName(), processor.getUniqueIdentifier()});
                                    AbstractStagingProcessor sProcessor = processor.createInstance();
                                    LOGGER.debug(" - Executing processor for local path {}", localPath.getAbsolutePath());
                                    sProcessor.performPostTransferProcessing(container);
                                    LOGGER.debug(" - Finishing processor execution");
                                    sProcessor.finalizePostTransferProcessing(container);
                                    LOGGER.debug(" - Processor successfully executed");
                                } catch (ConfigurationException ex) {
                                    LOGGER.error("Failed to configure StagingProcessor " + processor.getName() + " (" + processor.getUniqueIdentifier() + ")", ex);
                                } catch (StagingProcessorException ex) {
                                    LOGGER.error("Failed to perform StagingProcessor " + processor.getName() + " (" + processor.getUniqueIdentifier() + ")", ex);
                                }
                            }
                        } else {
                            LOGGER.error("Failed to store 'data' view.");
                            ingest.setStatus(INGEST_STATUS.INGEST_FAILED.getId());
                            ingest.setErrorMessage("Failed to register data organization.");
                        }
                    }
                } else {
                    LOGGER.error("Preprocessing failed, skipping ingest.");
                    //status to ingest should be already set
                }
            }
        } catch (MalformedURLException mue) {
            LOGGER.error("Failed to convert staging URL '" + ingest.getStagingUrl() + "' of transfer '" + ingest.getTransferId() + "' to local path", mue);
            ingest.setStatus(INGEST_STATUS.INGEST_FAILED.getId());
            ingest.setErrorMessage("Staging URL '" + ingest.getStagingUrl() + "' seems to be invalid.");
            result = false;
        } catch (AdalapiException ex) {
            //failed to handle file tree stuff
            LOGGER.error("Failed finalize ingest for object ID " + pId, ex);
            ingest.setStatus(INGEST_STATUS.INGEST_FAILED.getId());
            ingest.setErrorMessage("Failed to finalize ingest. Cause: " + ex.getMessage());
            result = false;
        } catch (RuntimeException t) {
            //failed to handle file tree stuff
            LOGGER.error("An unexpected error occured while ingesting object " + pId + ". Ingest cannot be continued.", t);
            ingest.setStatus(INGEST_STATUS.INGEST_FAILED.getId());
            ingest.setErrorMessage("Unhandled error during ingest. Cause: " + t.getMessage());
            result = false;
        }

        LOGGER.debug("Updating ingest information to current status");
        if (updateTransferStatus(ingest)) {
            LOGGER.debug("Ingest status successfully updated.");
        } else {
            LOGGER.error("Failed to update ingest status for transfer #" + ingest.getTransferId() + " to status " + ingest.getStatusEnum() + " and ErrorMessage '" + ingest.getErrorMessage() + ", URL:" + ingest.getStagingUrl() + "'");
        }
        return result;
    }

    /**
     * This method is used internally in an automated fashion to finalize the
     * next ingest. At first, all finalizable ingests are obtained and sorted by
     * their expiration date. Afterwards, for the ingest expiring next {@link #finalizeIngest(edu.kit.dama.commons.types.DigitalObjectId, edu.kit.dama.authorization.entities.IAuthorizationContext)
     * } is called.
     *
     * @return TRUE if an ingest could be finalized or if there was nothing to do.
     */
    public boolean finalizeIngests() {
        LOGGER.info("Finalizing ingests.");

        try {
            //check running ingests...2 allowed at the same time
            int maxParallelIngest = DataManagerSettings.getSingleton().getIntProperty(DataManagerSettings.STAGING_MAX_PARALLEL_INGESTS, 2);
            LOGGER.debug(" - Checking running ingests");
            List<IngestInformation> preparingIngests = StagingConfigurationManager.getSingleton().getIngestInformationServiceAdapter().getIngestsByStatus(INGEST_STATUS.INGEST_RUNNING, AuthorizationContext.factorySystemContext());
            if (preparingIngests.size() > maxParallelIngest) {
                LOGGER.info("There are already two ingests running. Skipping finalization cycle.");
                return false;
            } else {
                LOGGER.debug(" - Less than two ingests are running...continuing.");
            }
        } catch (ServiceAdapterException sae) {
            LOGGER.error(IIS_ACCESS_ERROR, sae);
            return false;
        }

        List<IngestInformation> finalizableIngests;
        try {
            LOGGER.debug("Obtaining finalizable ingests");
            finalizableIngests = StagingConfigurationManager.getSingleton().getIngestInformationServiceAdapter().getIngestsForArchiving(AuthorizationContext.factorySystemContext());
            LOGGER.debug(" - Received {} entity/entities", finalizableIngests.size());
        } catch (ServiceAdapterException sae) {
            LOGGER.error(IIS_ACCESS_ERROR, sae);
            return false;
        }

        if (finalizableIngests.isEmpty()) {
            LOGGER.debug("No finalizable ingests found");
            return true;
        }

        Collections.sort(finalizableIngests, new Comparator<IngestInformation>() {
            @Override
            public int compare(IngestInformation o1, IngestInformation o2) {
                return Long.valueOf(o1.getExpiresAt()).compareTo(o2.getExpiresAt());
            }
        });
        //finalize first ingest (the one which will expire next)
        return finalizeIngest(new DigitalObjectId(finalizableIngests.get(0).getDigitalObjectId()), getContext(finalizableIngests.get(0)));
    }

//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc=" Download Preparation (Interactive)">
    /**
     * Schedules the download for the digital object with the provided digital
     * object ID. This method will be called by the transfer preparation handler
     * if the user requests an ingest. The method takes care, that there is a
     * download folder existing after this call, which is writeable by the
     * calling user. This folder is stored at the preparation result entity
     * returned by this method, together with the appropriate status. The update
     * of the download information entity within the data backend is done by the
     * preparation handler.
     *
     * @param pDigitalObjectId The id of the digital object to download.
     * @param pAccessPointId The Id of the AccessPoint used for download. It
     * defines access protocol and target folder used to upload data.
     * @param pContext The authorization context to query for the download. This
     * context is only used for the initial query. Download-related access is
     * done using the context stored in the obtained download entity.
     *
     * @return A StagingPreparationResult object containing the result ( status,
     * error message and/or staging URL).
     */
    public StagingPreparationResult<DOWNLOAD_STATUS> scheduleDownload(DigitalObjectId pDigitalObjectId, String pAccessPointId, IAuthorizationContext pContext) {
        if (pDigitalObjectId == null) {
            throw new IllegalArgumentException(DOI_NULL_ERROR);
        }

        if (pContext == null) {
            throw new IllegalArgumentException(CTX_NULL_ERROR);
        }

        if (!StagingConfigurationManager.getSingleton().isExistingAccessPoint(pAccessPointId)) {
            throw new IllegalArgumentException("Provided AccessPoint '" + pAccessPointId + "' is not configured");
        }

        LOGGER.debug("Preparing download for ID '{}'", pDigitalObjectId);
        LOGGER.debug(" Context: {}/{}", new Object[]{pContext.getUserId().getStringRepresentation(), pContext.getGroupId().getStringRepresentation()});

        //get associated ingest information entity
        LOGGER.debug("Obtaining download information");
        DownloadInformation download;
        StagingPreparationResult result = new StagingPreparationResult();

        try {
            download = StagingConfigurationManager.getSingleton().getDownloadInformationServiceAdapter().getDownloadInformation(pDigitalObjectId, pContext);
        } catch (ServiceAdapterException sae) {
            LOGGER.error(DIS_ACCESS_ERROR, sae);
            result.setStatus(DOWNLOAD_STATUS.PREPARATION_FAILED);
            result.setErrorMessage(DIS_ACCESS_ERROR);
            return result;
        }

        if (download == null) {
            LOGGER.error("Failed to obtain valid download information for object with id '{}'", pDigitalObjectId);
            result.setStatus(DOWNLOAD_STATUS.PREPARATION_FAILED);
            result.setErrorMessage("Failed to obtain valid download information for object " + pDigitalObjectId + ".");
            return result;
        }

        if (!download.getStatusEnum().equals(DOWNLOAD_STATUS.SCHEDULED)) {
            //download is not in scheduled state...check which state it has, print appropriate log message and return
            if (download.getStatusEnum().equals(DOWNLOAD_STATUS.DOWNLOAD_READY)) {//the download is already prepared...return TRUE
                LOGGER.info("Download for digital object {} is already prepared and in READY state. Resetting it.", pDigitalObjectId);
                result.setStatus(DOWNLOAD_STATUS.SCHEDULED);
                result.setStagingUrl(download.getStagingUrl());
                //just return the result...the caller should take care that existing data is removed and new data is staged to 'StagingUrl'
                return result;
            } else {//other status...probably an error status
                LOGGER.warn("Object with ID '{}' has status {}. Preparation not possible or necessary.", new Object[]{pDigitalObjectId, download.getStatusEnum().toString()});
                result.setStatus(download.getStatusEnum());
                result.setErrorMessage(download.getErrorMessage());
                return result;
            }
        } //download is in scheduling mode

        //get transfer ID
        String transferId = download.getTransferId();
        LOGGER.debug("Successfully obtained download information with id #{}. Generating staging access...", transferId);

        AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(pAccessPointId);
        if (accessPoint == null) {
            LOGGER.warn("Failed to prepare staging access. No access point found for id {}", pAccessPointId);
            result.setStatus(DOWNLOAD_STATUS.PREPARATION_FAILED);
            result.setErrorMessage("Internal error. No staging access point found for id " + pAccessPointId + ".");
        } else {
            LOGGER.debug("Access point found. Preparing access point.");
            try {
                IAuthorizationContext downloadContext = getContext(download);
                accessPoint.prepare(download, downloadContext);
                URL downloadUrl = accessPoint.getAccessUrl(download, downloadContext);
                result.setStatus(DOWNLOAD_STATUS.SCHEDULED);
                result.setStagingUrl(downloadUrl.toString());
            } catch (TransferPreparationException ex) {
                LOGGER.warn("Failed to prepare staging access", ex);
                result.setStatus(DOWNLOAD_STATUS.PREPARATION_FAILED);
                result.setErrorMessage("Internal error. Failed to setup staging access.");
            }
        }
        return result;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Download Finalization (Automatic)">
    /**
     * This method is used internally in an automated fashion to finalize the
     * next ingest. Therefor the ingest service, the storage virtualization and
     * the data organization are needed. At first, all finalizable ingests are
     * obtained and sorted by their expiration date. Afterwards the service will
     * try to store all files located in the local ingest folder for the ingest
     * which will expire first. As a result, the virtual file tree is returned
     * and will be stored using the data organization if the ingest has
     * succeeded. If everything is done, the ingest information is update using
     * the ingest information service adapter.
     *
     * @return TRUE if any download could be finalized or if there was nothing to do.
     */
    public boolean finalizeDownloads() {
        LOGGER.info("Finalizing open downloads");

        try {
            LOGGER.debug(" - Checking running download preparations");
            int maxParallelDownloads = DataManagerSettings.getSingleton().getIntProperty(DataManagerSettings.STAGING_MAX_PARALLEL_DOWNLOADS, 2);
            List<DownloadInformation> preparingDownloads = StagingConfigurationManager.getSingleton().getDownloadInformationServiceAdapter().getDownloadsByStatus(DOWNLOAD_STATUS.PREPARING, AuthorizationContext.factorySystemContext());
            if (preparingDownloads != null && preparingDownloads.size() > maxParallelDownloads) {
                LOGGER.warn("There are already {} download preparing. Skipping finalization cycle.", maxParallelDownloads);
                return false;
            } else {
                LOGGER.debug(" - Less than 2 download preparations running...continuing.");
            }
        } catch (ServiceAdapterException sae) {
            LOGGER.error(DIS_ACCESS_ERROR, sae);
            return false;
        }

        List<DownloadInformation> preparableDownloads;
        try {
            LOGGER.debug("Obtaining finalizable downloads");
            preparableDownloads = StagingConfigurationManager.getSingleton().getDownloadInformationServiceAdapter().getDownloadsForStaging(AuthorizationContext.factorySystemContext());
            LOGGER.debug(" - Received {} entity/entities", preparableDownloads.size());
        } catch (ServiceAdapterException sae) {
            LOGGER.error(IIS_ACCESS_ERROR, sae);
            return false;
        }

        if (preparableDownloads.isEmpty()) {
            LOGGER.debug("No preparable downloads found");
            return true;
        }

        Collections.sort(preparableDownloads, new Comparator<DownloadInformation>() {
            @Override
            public int compare(DownloadInformation o1, DownloadInformation o2) {
                return Long.valueOf(o1.getExpiresAt()).compareTo(Long.valueOf(o2.getExpiresAt()));
            }
        });

        return finalizeDownload(new DigitalObjectId(preparableDownloads.get(0).getDigitalObjectId()), getContext(preparableDownloads.get(0)));
    }

    /**
     * This method finalizes the download for the provided digital object id. It
     * is the external interface to
     * {@link #finalizeDownload(edu.kit.dama.staging.entities.download.DownloadInformation)}.
     *
     * @param pId The digital object id of the download to finalize.
     * @param pContext The authorization context. This context is only used for
     * the initial query. Download-related access is done using the context
     * stored in the obtained download entity.
     *
     * @return TRUE if the download could be finilized.
     */
    public boolean finalizeDownload(DigitalObjectId pId, IAuthorizationContext pContext) {
        LOGGER.debug("Finalizing download for object '{}'", pId);
        DownloadInformation download;
        try {
            download = StagingConfigurationManager.getSingleton().getDownloadInformationServiceAdapter().getDownloadInformation(pId, pContext);
        } catch (ServiceAdapterException sae) {
            LOGGER.error(DIS_ACCESS_ERROR, sae);
            return false;
        }

        return finalizeDownload(download);
    }

    /**
     * This method finalizes the download for the provided download entity. This
     * method is intended to be used internally only.
     *
     * @param pDownloadInfo The download information.
     *
     * @return TRUE if the download could be finalized.
     */
    private boolean finalizeDownload(DownloadInformation pDownloadInfo) {
        if (!pDownloadInfo.getStatusEnum().equals(DOWNLOAD_STATUS.SCHEDULED)) {
            LOGGER.error("Finalization of download impossible. Status of download #{} is {}, but has to be DOWNLOAD_STATUS.SCHEDULED", pDownloadInfo.getTransferId(), pDownloadInfo.getStatusEnum());
            return false;
        }
        boolean result = true;

        pDownloadInfo.setStatusEnum(DOWNLOAD_STATUS.PREPARING);
        if (!updateTransferStatus(pDownloadInfo)) {
            LOGGER.error("Failed to update download status to {}. Finalizing impossible.", DOWNLOAD_STATUS.PREPARING);
            return false;
        }

        LOGGER.debug("Preparing download #{} for object '{}'", pDownloadInfo.getTransferId(), pDownloadInfo.getDigitalObjectId());
        try {
            //obtain the local path where this ingest's files are located
            AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(pDownloadInfo.getAccessPointId());

            URL stagingUrl = new URL(pDownloadInfo.getStagingUrl());
            LOGGER.debug("Obtaining user paths for  URL '{}'", stagingUrl.toString());
            File localStagingPath = accessPoint.getLocalPathForUrl(stagingUrl, getContext(pDownloadInfo));
            //File localDataPath = new File(localStagingPath, Constants.STAGING_DATA_FOLDER_NAME);
            File localSettingsPath = new File(localStagingPath, Constants.STAGING_SETTINGS_FOLDER_NAME);

            //try to perform staging
            StagingFile destinationForStaging = new StagingFile(new AbstractFile(localStagingPath));
            LOGGER.debug("Perform staging into folder '{}'", destinationForStaging);
            LOGGER.debug(" - Reading data from storage virtualization");

            String treeFile = FilenameUtils.concat(localSettingsPath.getPath(), DownloadPreparationHandler.DATA_FILENAME);
            IFileTree downloadTree = DataOrganizationUtils.readTreeFromFile(new File(treeFile));

            //get filetree and restore
            LOGGER.debug("Restoring data organization tree from file '{}'", treeFile);
            if (!StagingConfigurationManager.getSingleton().getStorageVirtualizationAdapter().restore(pDownloadInfo, downloadTree, destinationForStaging)) {
                LOGGER.error("Failed to restore data organization tree from file {}", treeFile);
                pDownloadInfo.setStatus(DOWNLOAD_STATUS.PREPARATION_FAILED.getId());
                pDownloadInfo.setErrorMessage("Failed to reconstruct file tree.");
                result = false;
            } else {
                //preparation successfully...transfer can be performed
                LOGGER.debug("Download preparation finished. Download #{} for object {} is now ready", pDownloadInfo.getTransferId(), pDownloadInfo.getDigitalObjectId());
                pDownloadInfo.setStatus(DOWNLOAD_STATUS.DOWNLOAD_READY.getId());
                pDownloadInfo.setErrorMessage(null);

                //handle notification
                Properties notificationProperties = MailNotificationHelper.restoreProperties(localSettingsPath);
                if (!notificationProperties.isEmpty()) {
                    if (result) {
                        LOGGER.debug("Try to notify user about finalized download");
                        MailNotificationHelper.sendDownloadNotification(notificationProperties, pDownloadInfo);
                    } else {
                        LOGGER.warn("Download not finalized successfully. Notification skipped.");
                    }
                }
            }
        } catch (MalformedURLException mue) {
            LOGGER.error("Failed to convert staging URL '" + pDownloadInfo.getStagingUrl() + "' of transfer '" + pDownloadInfo.getTransferId() + "' to local path", mue);
            pDownloadInfo.setStatus(DOWNLOAD_STATUS.PREPARATION_FAILED.getId());
            pDownloadInfo.setErrorMessage("Staging URL '" + pDownloadInfo.getStagingUrl() + "' seems to be invalid.");
            result = false;
        } catch (RuntimeException t) {
            //failed to handle file tree stuff
            LOGGER.error("An unexpected error occured while downloading object " + pDownloadInfo.getTransferId() + ". Download cannot be continued.", t);
            pDownloadInfo.setStatus(DOWNLOAD_STATUS.PREPARATION_FAILED.getId());
            pDownloadInfo.setErrorMessage("Unhandled error during download. Cause: " + t.getMessage());
            result = false;
        }

        LOGGER.debug("Updating download information to status {}", pDownloadInfo.getStatusEnum());
        if (updateTransferStatus(pDownloadInfo)) {
            LOGGER.debug("Download status successfully updated.");
        } else {
            LOGGER.error("Failed to update status or download #{} for digital object {} to {}", pDownloadInfo.getTransferId(), pDownloadInfo.getDigitalObjectId(), pDownloadInfo.getStatusEnum());
            result = false;
        }
        return result;
    }

//</editor-fold>
    /**
     * Flush the download with the provided id. The id is the primary key, as
     * there might be multiple downloads per digital object id.
     *
     * @param pId The id of the download.
     * @param pContext The security context. This context is only used for the
     * initial query. Download-related access is done using the context stored
     * in the obtained download entity.
     *
     * @return TRUE if the download was successfully flushed.
     */
    public boolean flushDownload(Long pId, IAuthorizationContext pContext) {
        if (pContext == null) {
            throw new IllegalArgumentException(CTX_NULL_ERROR);
        }
        LOGGER.debug("Flushing download(s) for object '{}'", pId);
        DownloadInformation download;
        try {
            download = StagingConfigurationManager.getSingleton().getDownloadInformationServiceAdapter().getDownloadInformation(pId, pContext);
        } catch (ServiceAdapterException sae) {
            LOGGER.error(DIS_ACCESS_ERROR, sae);
            return false;
        }

        if (download == null) {
            LOGGER.warn("No download obtained from database, returning.");
            return true;
        }

        LOGGER.debug("Trying to flush download #{} for digital object id {}", new Object[]{download.getId(), download.getDigitalObjectId()});
        File localFolder = getLocalStagingFolder(download, getContext(download));

        LOGGER.debug(" - Removing local staging folder {}", localFolder.getAbsolutePath());
        try {
            if (!edu.kit.dama.util.FileUtils.isAccessible(localFolder)) {
                LOGGER.debug("Local folder " + localFolder + " is not accessible. Cleanup not possible.");
                return false;
            }
            FileUtils.deleteDirectory(localFolder);
            LOGGER.debug(" - Staging folder successfully removed.");
        } catch (IOException ex) {
            LOGGER.error("Failed to remove staging folder for download with ID " + pId, ex);
        }

        //cleanup access point stuff
        String accessPointId = download.getAccessPointId();
        AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(accessPointId);
        if (accessPoint != null) {
            LOGGER.debug(" - Performing AccessPoint cleanup.");
            if (accessPoint.prepareCleanup(download, getContext(download))) {
                LOGGER.info("AccessPoint cleanup successfully scheduled.");
            } else {
                LOGGER.warn("Failed to schedule AccessPoint cleanup for transfer with id {} and AccessPoint {}", download.getTransferId(), accessPointId);
            }
        } else {
            LOGGER.warn("No AccessPoint obtained for id {}. Skipping access point cleanup.", accessPointId);
        }

        LOGGER.debug("Download successfully flushed.");
        return true;
    }

    /**
     * Flush the ingest with the provided digital object id. As There should be
     * only one ingest per digital object id, this id can be used here.
     *
     * @param pId The object id of the ingest to flush.
     * @param pContext The authorization context. This context is only used for
     * the initial query. Ingest-related access is done using the context stored
     * in the obtained ingest entity.
     *
     * @return TRUE if all files related to the ingest were removed.
     */
    public boolean flushIngest(DigitalObjectId pId, IAuthorizationContext pContext) {
        if (pContext == null) {
            throw new IllegalArgumentException(CTX_NULL_ERROR);
        }
        LOGGER.debug("Flushing ingest for object '{}'", pId);
        IngestInformation ingest;
        try {
            ingest = StagingConfigurationManager.getSingleton().getIngestInformationServiceAdapter().getIngestInformation(pId, pContext);
        } catch (ServiceAdapterException sae) {
            LOGGER.error(IIS_ACCESS_ERROR, sae);
            return false;
        }
        if (ingest == null) {
            LOGGER.warn("No ingest obtained from database, returning.");
            return true;
        }

        File localFolder = getLocalStagingFolder(ingest, getContext(ingest));

        LOGGER.debug("Removing local staging folder {}", localFolder.getAbsolutePath());
        try {
            if (!edu.kit.dama.util.FileUtils.isAccessible(localFolder)) {
                LOGGER.debug("Local folder " + localFolder + " is not accessible. Cleanup not possible.");
                return false;
            }
            FileUtils.deleteDirectory(localFolder);
            LOGGER.debug("Staging folder successfully removed.");
        } catch (IOException ex) {
            LOGGER.error("Failed to remove staging folder for ingest with ID " + pId, ex);
        }

        LOGGER.debug("Ingest successfully flushed.");
        return true;
    }

//  public static void main(String[] args) throws Exception{
//
//    IFileTree downloadTree = DataOrganizationUtils.readTreeFromFile(new File("D:\\Virtual Machines\\Ubuntu12\\Share\\preparationContent.xml"));
//    
//    System.out.println(((IFileNode) downloadTree.getRootNode().getChildren().get(0)).getLogicalFileName().asString());
//    System.out.println(new URL(((IFileNode) downloadTree.getRootNode().getChildren().get(0)).getLogicalFileName().asString()));     
//    System.out.println(new AbstractFile(new URL(((IFileNode) downloadTree.getRootNode().getChildren().get(0)).getLogicalFileName().asString())).getUrl());
//    StagingFile sourceLfn = new StagingFile(((IFileNode) downloadTree.getRootNode().getChildren().get(0)).getLogicalFileName());
//         
//        System.out.println(  sourceLfn.getAbstractFile().getUrl());
//        //target LFN is a local file
//        AbstractFile targetFile = new AbstractFile(URLCreator.appendToURL(new URL("http://localhost/webdav/"), downloadTree.getRootNode().getChildren().get(0).getName()));
//        System.out.println( targetFile.getUrl());
//        StagingFile targetLfn = new StagingFile(targetFile);
//       System.out.println(  targetLfn.getAbstractFile().getUrl());
//     
//    DataOrganizationUtils.printTree(downloadTree.getRootNode(), true);
//   }
    //public static void main(String[] args) throws Exception {
        /*
     * URL u = new
     * URL("gsiftp://ipelsdf1.lsdf.kit.edu:2811/test/myFolder/myFile.php?test");
     * URI ur = new URI("file", null, u.getPath(), null);
     * System.out.println(ur);
     */
    /* String configFile = JhoveBase.getConfigFileFromProperties();
     String saxClass = JhoveBase.getSaxClassFromProperties();
     JhoveBase base = new JhoveBase();
     base.init(configFile, saxClass);
     base.getHandler(XmlHandler.class.getCanonicalName());

     App a = new App("", "", new int[]{0, 0, 0}, "", "");
     long s = System.currentTimeMillis();
     base.dispatch(a, null, null, new XmlHandler() {
     @Override
     public void show(RepInfo ri) {
     super.show(ri);
     if (ri.isConsistent()) {
     System.out.println("Mime: " + ri.getMimeType());
     System.out.println("Note: " + ri.getNote());
     System.out.println("Created: " + ri.getCreated());
     System.out.println("LastMod: " + ri.getLastModified());
     System.out.println("Size: " + ri.getSize());
     System.out.println("Profile: " + ri.getProfile());
     System.out.println("-------------------");
     }

     }
     }, saxClass, new String[]{"file:///d:/CorelX4.iso"});

     System.out.println("D " + (System.currentTimeMillis() - s));
     if (true) {
     return;
     }

     final XmlHandler h = new XmlHandler("Test", "1", new int[]{0, 0, 0}, "", "");
     h.setWriter(new PrintWriter(System.out));
     h.setBase(base);
     for (Object o : base.getModuleList()) {
     //System.out.println("M " + o);
     Module current = (Module) o;
     base.process(null, current, new HandlerBase("Test", "1", new int[]{0, 0, 0}, "", "") {
     @Override
     public void show(Module module) {
     }

     @Override
     public void show(RepInfo ri) {
     if (ri.isConsistent()) {
     System.out.println("Mime: " + ri.getMimeType());
     System.out.println("Note: " + ri.getNote());
     System.out.println("Created: " + ri.getCreated());
     System.out.println("LastMod: " + ri.getLastModified());
     System.out.println("Size: " + ri.getSize());
     System.out.println("Profile: " + ri.getProfile());
     h.show(ri);
     h.reset();
     System.out.println("-------------------");
     }

     }

     @Override
     public void show(OutputHandler oh) {
     System.out.println("O " + oh);
     }

     @Override
     public void show() {
     }

     @Override
     public void show(App app) {
     }

     @Override
     public void showHeader() {
     }

     @Override
     public void showFooter() {
     }
     }, "file:///d:/carbon.gif");
     }*/
  // }
}
