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
package edu.kit.dama.staging.adapters;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.dama.transfer.client.impl.AbstractTransferClient;
import edu.kit.dama.transfer.client.impl.InProcStagingClient;
import edu.kit.dama.transfer.client.interfaces.IStagingCallback;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.interfaces.IStorageVirtualizationServiceAdapter;
import edu.kit.dama.staging.entities.StagingFile;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.staging.exceptions.StagingIntitializationException;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationServiceLocal;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.staging.util.StagingConfigurationManager;
import edu.kit.dama.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class DefaultStorageVirtualizationAdapter implements IStorageVirtualizationServiceAdapter, IStagingCallback {

    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStorageVirtualizationAdapter.class);
    private final static String TMP_DIR_PATTERN = "$tmp";
    private final static String YEAR_PATTERN = "$year";
    private final static String MONTH_PATTERN = "$month";
    private final static String DAY_PATTERN = "$day";
    private final static String OWNER_PATTERN = "$owner";
    private final static String GROUP_PATTERN = "$group";
    private URL archiveUrl = null;
    private String pathPattern = null;

    /**
     * Default constructor.
     */
    public DefaultStorageVirtualizationAdapter() {
    }

    @Override
    public boolean configure(Configuration pConfig) throws ConfigurationException {
        pathPattern = pConfig.getString("pathPattern");
        String archiveUrlProperty = pConfig.getString("archiveUrl");
        if (archiveUrlProperty == null) {
            throw new ConfigurationException("Property 'archiveUrl' is missing");
        }

        //replace TMP_DIR_PATTERN in target URL
        LOGGER.debug("Looking for replacements in archive location");
        String tmp = System.getProperty("java.io.tmpdir");
        if (!tmp.startsWith("/")) {
            tmp = "/" + tmp;
        }
        archiveUrlProperty = archiveUrlProperty.replaceAll(Pattern.quote(TMP_DIR_PATTERN), Matcher.quoteReplacement(tmp));
        LOGGER.debug("Using archive Url {}", archiveUrlProperty);
        //set baseDestinationURL property

        try {
            archiveUrl = new URL(archiveUrlProperty);
        } catch (MalformedURLException mue) {
            throw new ConfigurationException("archiveUrl property (" + archiveUrlProperty + ") is not a valid URL", mue);
        }

        AbstractFile destination = new AbstractFile(archiveUrl);
        //if caching location is local, check accessibility
        if (destination.isLocal()) {
            LOGGER.debug("Archive URL is local. Checking accessibility.");
            URI uri = null;

            try {
                uri = destination.getUrl().toURI();
            } catch (IllegalArgumentException ex) {
                //provided archive URL is no URI (e.g. file://folder/ -> third slash is missing after file:)
                throw new StagingIntitializationException("Archive location " + destination.getUrl().toString() + " has an invalid URI syntax", ex);
            } catch (URISyntaxException ex) {
                LOGGER.warn("Failed to check local archive URL. URL " + destination.getUrl() + " seems not to be a valid URI.", ex);
            }
            File localFile = null;
            try {
                localFile = new File(uri);
            } catch (IllegalArgumentException ex) {
                throw new ConfigurationException("Archive URI " + uri + " seems not to be a supported file URI. This storage virtualization implementation only supports URLs in the format file:///<path>/", ex);
            }

            if (!FileUtils.isAccessible(localFile)) {
                throw new StagingIntitializationException("Archive location seems to be offline: '" + destination.getUrl().toString() + "'");
            }
        }

        try {
            if (!destination.exists()) {
                LOGGER.debug("Archive location does not exists, trying to create it...");
                destination = AbstractFile.createDirectory(destination);
                LOGGER.debug("Archive location successfully created ");
            } else {
                LOGGER.debug("Archive location already exists");
            }
            //clear cached values, e.g. for isReadable() and isWriteable()
            destination.clearCachedValues();
            //check access
            if (destination.isReadable() && destination.isWriteable()) {
                LOGGER.debug("Archive location '{}' is valid", destination.getUrl());
            } else {
                throw new StagingIntitializationException("Archive location '" + destination.getUrl().toString() + "' is not accessible");
            }
        } catch (AdalapiException ae) {
            throw new StagingIntitializationException("Failed to setup archive location '" + destination.getUrl().toString() + "'", ae);
        }
        return true;
    }

    @Override
    public String calculateChecksum(StagingFile pFile, HASH_TYPE type) {
        //implement this if required...currently, no checksumming is supported
        return "";
    }

    @Override
    public IFileTree store(TransferTaskContainer pContainer, IAuthorizationContext pContext) {
        String transferId = pContainer.getTransferInformation().getTransferId();
        AbstractFile destination = createDestination(transferId, pContext);
        if (destination == null) {
            LOGGER.error("Failed to obtain destination for transfer id '{}'. Aborting!", transferId);
            return null;
        }
        LOGGER.debug("Setting staging destination to {}", destination);
        pContainer.setDestination(destination.getUrl());

        LOGGER.debug("Storing object with transfer ID '{}'", pContainer.getUniqueTransferIdentifier());
        //udpate storage URL in ingest information entity and commit changes
        ((IngestInformation) pContainer.getTransferInformation()).setStorageUrl(destination.getUrl().toString());
        LOGGER.debug("Setting storage URL for ingest with id '{}' to {}", transferId, destination.getUrl().toString());
        IngestInformationServiceLocal.getSingleton().updateStorageUrl(pContainer.getTransferId(), destination.getUrl().toString(), pContext);
        LOGGER.debug("Storage URL successfully set to {}", destination.getUrl().toString());

        //start with transfer
        LOGGER.info("Disabling ADALAPI overwrite checks");
        AbstractFile.OVERWRITE_PERMISSION permission = AbstractFile.getOverwritePermission();
        AbstractFile.setOverwritePermission(AbstractFile.OVERWRITE_PERMISSION.ALLOWED);
        InProcStagingClient isc = new InProcStagingClient(pContainer, destination);
        isc.addStagingCallbackListener(this);
        isc.start();

        //perform the storage operation in a blocking fashion
        while (isc.isTransferRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
            }
        }

        LOGGER.info("Reset ADALAPI overwrite checks");
        AbstractFile.setOverwritePermission(permission);

        //build file tree
        IFileTree tree;
        LOGGER.debug("Building file tree for data organization");
        try {
            tree = DataOrganizationUtils.createTreeFromFile(pContainer.getTransferInformation().getDigitalObjectId(), destination, true);
        } catch (IOException ex) {
            LOGGER.error("Failed to get local file for path '" + destination + "'", ex);
            tree = null;
        }
        return tree;
    }

    @Override
    public boolean restore(DownloadInformation pDownloadInformation, IFileTree pArchivedTree, StagingFile pDownloadDestination) {
        LOGGER.debug("Restoring file tree for download {}", pDownloadInformation.getTransferId());
        IFileTree tree = TransferTaskContainer.createCompatibleTree(pDownloadInformation, pArchivedTree.getRootNode());
        LOGGER.debug("File tree obtained.");
        TransferTaskContainer container = TransferTaskContainer.factoryDownloadContainer(pDownloadInformation.getId(), tree, StagingConfigurationManager.getSingleton().getRestServiceUrl());
        container.setTransferInformation(pDownloadInformation);
        LOGGER.debug("Transfer container created. Creating transfer client to download destination {}", pDownloadDestination);
        InProcStagingClient isc = new InProcStagingClient(container, pDownloadDestination.getAbstractFile());
        isc.addStagingCallbackListener(this);
        isc.start();
        LOGGER.debug("Transfer client started for download {}. Waiting for client termination.", pDownloadInformation.getTransferId());
        //perform the storage operation in a blocking fashion
        while (isc.isTransferRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
            }
        }
        LOGGER.debug("Transfer client for download {} finished with status {}.", pDownloadInformation.getTransferId(), isc.getStatus());
        return isc.getStatus().equals(AbstractTransferClient.TRANSFER_STATUS.SUCCEEDED);
    }

    @Override
    public boolean isHashTypeSupported(HASH_TYPE type) {
        return false;
    }

    @Override
    public void stagingStarted(String pId) {
        LOGGER.info("Staging started for object with ID '{}'", pId);
    }

    @Override
    public void stagingRunning(String pId) {
        LOGGER.debug("Staging for object with ID '{}' is still running", pId);
    }

    @Override
    public void stagingFinished(String pId, boolean pSuccess) {
        if (pSuccess) {
            LOGGER.info("Staging successfully finished for object with ID '{}'", pId);
        } else {
            LOGGER.error("Staging failed for object with ID '{}'", pId);
        }
    }

    /**
     * Create the destination folder for the ingest. This folder is located
     * withing the storage virtualization system. For this very basic adapter it
     * will be a folder with with a fixed scheme telling when the object was
     * uploaded by whom and which transfer id it had. The folder will be
     * generated as follows:
     *
     * <i>archiveURL</i>/<i>pathPattern</i>/SHA1(pTransferId) where
     * <i>pathPattern</i> allows the use or variables like $year, $month, $day
     * and $owner and pTransferId is the unique identifier of the transfer.
     *
     * @param pTransferId The transfer id as it comes from the ingest
     * information entity.
     * @param pOwner The owner who ingested the object.
     *
     * @return An AbstractFile representing the destination for the final
     * ingest.
     *
     */
    private AbstractFile createDestination(String pTransferId, IAuthorizationContext pContext) {
        if (pTransferId == null) {//transfer id is part of the destination, so it must not be null
            throw new IllegalArgumentException("Argument 'pTransferId' must not be 'null'");
        }

        String sUrl = archiveUrl.toString();
        if (pathPattern != null) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            String dynPath = pathPattern;
            dynPath = dynPath.
                    replaceAll(Pattern.quote(YEAR_PATTERN), Integer.toString(year)).
                    replaceAll(Pattern.quote(MONTH_PATTERN), Integer.toString(month)).
                    replaceAll(Pattern.quote(DAY_PATTERN), Integer.toString(day));
            if (dynPath.contains(OWNER_PATTERN) || dynPath.contains(GROUP_PATTERN)) {//owner/group should be replaced by pattern definition
                if (pContext == null) {//uploader is 'null' but we need it for replacement
                    throw new IllegalArgumentException("Argument 'pOwner' must not be 'null' if pattern contains element '" + OWNER_PATTERN + "' or '" + GROUP_PATTERN + "'");
                } else {//everything is fine

                    LOGGER.debug("Replacing owner/group pattern with values from context '{}'", pContext);
                    dynPath = dynPath.
                            replaceAll(Pattern.quote(OWNER_PATTERN), Matcher.quoteReplacement(pContext.getUserId().getStringRepresentation())).
                            replaceAll(Pattern.quote(GROUP_PATTERN), Matcher.quoteReplacement(pContext.getGroupId().getStringRepresentation()));
                }
            }
            LOGGER.debug("Appending pattern-based path '{}' to base destination '{}'", new Object[]{dynPath, sUrl});
            sUrl += "/" + dynPath;
        }

        //finally, create abstract file and return
        AbstractFile result;
        try {
            if (!sUrl.endsWith("/")) {
                sUrl += "/";
            }
            LOGGER.debug("Appending transfer ID '{}' to current destination '{}'.", new Object[]{pTransferId, sUrl});
            sUrl += pTransferId;
            LOGGER.debug("Preparing destination at {}.", sUrl);

            result = new AbstractFile(new URL(sUrl));
            Configuration config = result.getConfiguration();
            String context = pContext.getUserId().getStringRepresentation() + " " + pContext.getGroupId().getStringRepresentation();
            LOGGER.debug("Adding repository context {} to custom access protocol configuration.", context);
            config.setProperty("repository.context", context);
            result = new AbstractFile(new URL(sUrl), config);

            //check if destination exists and create it if required
            if (result.exists()) {
                LOGGER.info("Destination at '{}' already exists.", sUrl);
            } else {//try to create destination
                result = AbstractFile.createDirectory(result);
            }

            //check destination
            if (result != null) {//destination could be obtained
                result.clearCachedValues();
                if (result.isReadable() && result.isWriteable()) {
                    //everything is fine...return result
                    return result;
                } else {
                    //destination cannot be accessed
                    LOGGER.error("Destination '{}' exists but is not read- or writeable", sUrl);
                    result = null;
                }
            } else {
                LOGGER.warn("No result obtained from directory creation.");
            }
        } catch (MalformedURLException mue) {
            LOGGER.error("Failed to create valid destination URL for '" + sUrl + "' and transferId " + pTransferId, mue);
            result = null;
        } catch (AdalapiException ae) {
            LOGGER.error("Failed to check/create destination for '" + sUrl + "'", ae);
            result = null;
        }
        return result;
    }

}
