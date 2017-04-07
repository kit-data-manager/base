/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
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
package edu.kit.dama.transfer.client.util;

import com.thoughtworks.xstream.XStream;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.transfer.client.exceptions.TransferClientInstatiationException;
import edu.kit.dama.transfer.client.impl.AbstractTransferClient;
import edu.kit.dama.transfer.client.impl.GenericDownloadClient;
import edu.kit.dama.transfer.client.impl.GenericIngestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.exceptions.ContainerInitializationException;
import edu.kit.dama.staging.util.StagingUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class TransferHelper {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferHelper.class);
    private static final String CHECKPOINT_FILENAME = "checkpoint.xml";

    /**
     * Hidden default constructor
     */
    private TransferHelper() {
    }

    /**
     * Checks if the transfer for the provided container can be restored from a
     * checkpoint. This method only checks if a checkpoint file is located
     * within the transfer temp directory and returns TRUE if this file exists,
     * no matter if the file is valid or not.
     *
     * @param pContainer The transfer container.
     *
     * @return TRUE if there is a checkpoint stored for this transfer
     */
    public static boolean canRestoreTransfer(TransferTaskContainer pContainer) {
        boolean result;
        try {
            String tmpDir = StagingUtils.getTempDir(pContainer);
            if (new File(tmpDir).exists()) {//temp dir exists...check for checkpoint file
                result = new File(tmpDir + File.separator + CHECKPOINT_FILENAME).exists();
            } else {//temp dir does not exist...checkpoint file will also not exist
                result = false;
            }
        } catch (IOException ioe) {//failed to check temp dir
            result = false;
        }
        return result;
    }

    /**
     * Restore a transfer from the local checkpoint.
     *
     * @param pContainer The container which contains the transfer information.
     * @param pContext The credentials to access the staging service used to
     * check the validity of the transfer.
     *
     * @return The restored container.
     */
    public static TransferTaskContainer restoreTransfer(TransferTaskContainer pContainer, SimpleRESTContext pContext) {
        TransferTaskContainer restoredContainer = null;
        FileInputStream fin = null;

        try {
            String tmpDir = StagingUtils.getTempDir(pContainer);
            File checkpointFile = new File(tmpDir + File.separator + CHECKPOINT_FILENAME);
            if (checkpointFile.exists()) {
                LOGGER.info("Restoring transfer from checkpoint");
                fin = new FileInputStream(checkpointFile);
                restoredContainer = TransferTaskContainer.loadFromStream(fin, pContext.getAccessKey(), pContext.getAccessSecret());
            } else {
                LOGGER.info("No checkpoint found");
            }
        } catch (IOException ioe) {
            //failed to obtain tmp dir
            LOGGER.error("Failed to obtain temporary directory or to restore last checkpoint", ioe);
        } catch (ContainerInitializationException ex) {
            LOGGER.error("Failed to initialize the restored transfer container", ex);
        } finally {
            if (fin != null) {
                try {
                    fin.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
        return restoredContainer;
    }

    /**
     * Create a new checkpoint for the transfer with the ID pTransferId. The
     * checkpoint file will contain all tasks included in pTasks exclusive the
     * tasks in pTasksToIgnore. This second list may contain tasks which have
     * already finished.
     *
     * @param pContainer The transfer task container which contains all
     * information about the transfer.
     *
     * @return TRUE if the checkpoint could be created.
     */
    public static boolean createCheckpoint(TransferTaskContainer pContainer) {
        boolean result = false;

        FileOutputStream fout = null;
        try {
            String tempDir = StagingUtils.getTempDir(pContainer);
            File checkpointFile = new File(tempDir + File.separator + CHECKPOINT_FILENAME);
            fout = new FileOutputStream(checkpointFile);
            new XStream().toXML(pContainer, fout);
            fout.flush();
            result = true;
        } catch (IOException ioe) {
            LOGGER.trace("Failed to create checkpoint. I'll proceed, but will not be able to resume unfinished transfers.", ioe);
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
        return result;
    }

    /**
     * Factory a new transfer client for the provided transfer task container.
     * This method is intended to be used for ingests as no destination can be
     * provided. The destination is obtained by the staging REST interface
     * internally.
     *
     * @param pContainer The transfer task container.
     *
     * @return The transfer client.
     *
     * @throws TransferClientInstatiationException If the transfer client could
     * not be instantiated.
     */
    public static AbstractTransferClient factoryTransferClient(TransferTaskContainer pContainer) throws TransferClientInstatiationException {
        return factoryTransferClient(pContainer, null);
    }

    /**
     * Factory a new transfer client for the provided transfer task container.
     * This method is intended to be used for downloads. The download
     * destination will be set in the transfer task container.
     *
     * @param pContainer The transfer task container.
     * @param pDestination The download destination.
     *
     * @return The transfer client.
     *
     * @throws TransferClientInstatiationException If the transfer client could
     * not be instantiated.
     */
    public static AbstractTransferClient factoryTransferClient(TransferTaskContainer pContainer, AbstractFile pDestination) throws TransferClientInstatiationException {
        if (pContainer == null) {
            throw new IllegalArgumentException("Argument pTransferInfo must not be null");
        }
        if (pDestination == null && TransferTaskContainer.TYPE.DOWNLOAD.equals(pContainer.getType())) {
            throw new IllegalArgumentException("Argument pDestination must be provided for download containers.");
        }
        if (TransferTaskContainer.TYPE.DOWNLOAD.equals(pContainer.getType())) {
            return new GenericDownloadClient(pContainer, pDestination);
        } else if (TransferTaskContainer.TYPE.INGEST.equals(pContainer.getType())) {
            return new GenericIngestClient(pContainer);
        }

        throw new UnsupportedOperationException("Unsupported transfer task container type provided (" + pContainer.getType() + ")");
    }

    /**
     * Get a list of IDs of all local transfers.
     *
     * @return A list of locally available transfer IDs.
     *
     * @throws IOException If obtaining the local transfer folders fails.
     */
    public static List<Long> getLocalTransfers() throws IOException {
        File tmpDir = new File(StagingUtils.getTempDir());
        List<Long> transferIds = new ArrayList<Long>();
        for (File f : tmpDir.listFiles()) {
            try {
                transferIds.add(Long.parseLong(f.getName()));
            } catch (NumberFormatException ex) {
                LOGGER.debug("Invalid transfer id " + f.getName() + " in file " + f, ex);
            }
        }
        return transferIds;
    }
}
