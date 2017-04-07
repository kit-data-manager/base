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
package edu.kit.dama.staging.interfaces;

import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.entities.interfaces.ISimpleTransferInformation;
import java.net.URL;
import java.util.Set;

/**
 * Basic interface reflecting a transfer information entity. The status of the
 * according transfer is reflected by an implementation of the ITransferStatus
 * interface.
 *
 * @param <C> An implementation of ITransferStatus.
 * @see ITransferStatus
 *
 * @author jejkal
 */
public interface ITransferInformation<C extends ITransferStatus> extends ISimpleTransferInformation {

    /**
     * Returns the internal transfer id. The transfer id is a unique identifier
     * of the transfer. It should contain the transfer type (e.g. ingest or
     * download), the primary key that can be used to find the transfer in the
     * database and the associated object id. The result should be escaped in a
     * proper way in order to use the transfer id as folder name for storing and
     * identifying the transfer on the local disk.
     *
     * @return The transfer id.
     */
    String getTransferId();

    /**
     * Returns the digital object id this transfer is associated with.
     *
     * @return The digital object id.
     */
    String getDigitalObjectId();

    /**
     * Sets the digital object id this transfer is associated with.
     *
     * @param pObjectId The digital object id.
     */
    void setDigitalObjectId(String pObjectId);

    /**
     * Get the id of the owner of this transfer.
     *
     * @return String The owner id.
     */
    String getOwnerId();

    /**
     * Get the id of the group to which this transfer belongs.
     *
     * @return String The group id.
     */
    String getGroupId();

    /**
     * Set the id of the owner of this transfer.
     *
     * @param pOwnerId The id of the owner.
     */
    void setOwnerId(String pOwnerId);

    /**
     * Set the id of the group to which this transfer belongs.
     *
     * @param pGroupId The id of the group.
     */
    void setGroupId(String pGroupId);

    /**
     * Get the client access URL for this transfer. This URL may point to a
     * location accessible via some transfer client or to a separate
     * transmission client.
     *
     * @return The URL of this transfer.
     */
    String getClientAccessUrl();

    /**
     * Set the client access URL for this transfer. This URL may point to a
     * location accessible via some transfer client or to a separate
     * transmission client.
     *
     * @param pUrl The URL of this transfer.
     */
    void setClientAccessUrl(String pUrl);

    /**
     * Get the staging URL for this transfer. This method is intended to be used
     * for monitoring (user) or for rollback operations (administrator/system).
     * The staging URL will be created while preparing a transfer and will be
     * filled with data either by the system (download) or by the user (ingest).
     *
     * @return The staging URL of this transfer.
     */
    String getStagingUrl();

    /**
     * Set the staging URL for this transfer. This URL will be located within
     * the staging cache. The value of this field is set during the
     * ingest-/download-preparation.
     *
     * @param pUrl The staging URL of this transfer.
     */
    void setStagingUrl(String pUrl);

    /**
     * Get the storage URL for this transfer. This URL points to the location at
     * the storage backend where the data will be finally stored during ingest.
     * For downloads this method should not be implemented.
     *
     * @return The storage URL of this transfer.
     */
    String getStorageUrl();

    /**
     * Set the storage URL for this transfer. This URL points to the location at
     * the storage backend where the data will be finally stored during ingest.
     * For downloads this method should not be implemented.
     *
     * @param pUrl The storage URL of this transfer.
     */
    void setStorageUrl(String pUrl);

    /**
     * Get the status of this transfer.
     *
     * @return The status enumeration.
     */
    C getStatusEnum();

    /**
     * Set the status of this transfer.
     *
     * @param pStatus The new status.
     */
    void setStatusEnum(C pStatus);

    /**
     * Get the last error message which is set if an error status occured.
     *
     * @return The error message.
     */
    String getErrorMessage();

    /**
     * Set a new error message.
     *
     * @param pErrorMessage The error message.
     */
    void setErrorMessage(String pErrorMessage);

    /**
     * Get the timestamp of the last update operation to this transfer.
     *
     * @return The timestamp of the last update.
     */
    long getLastUpdate();

    /**
     * Set the timestamp of the last update.
     *
     * @param pTimestamp The timestamp of the last update.
     */
    void setLastUpdate(long pTimestamp);

    /**
     * Returns if the transfer has expired or not.
     *
     * @return TRUE=Transfer has expired and is ready for cleanup.
     */
    boolean isExpired();

    /**
     * Set timestamp when this transfer should expire. Normally the expiration
     * time should calculated dynamically via lastChange + X.
     *
     * @param pTimestamp The timestamp when the transfer expires.
     */
    void setExpiresAt(long pTimestamp);

    /**
     * Get the timestamp when this transfer expires.
     *
     * @return long Timestamp when this transfer expires. If no timestamp was
     * set, lastChange + X should be returned.
     */
    long getExpiresAt();

    /**
     * Returns the URL to the remote folder which a used to put all user data.
     * Only the data in this folder will be stored withing the storage backend.
     *
     * @return The URL to the remote 'data' folder.
     */
    URL getDataFolderUrl();

    /**
     * Returns the URL to the remote folder which a used to put all transfer
     * related settings. Normally, this folder is just needed on the server
     * side.
     *
     * @return The URL to the remote 'settings' folder.
     */
    URL getSettingsFolderUrl();

    /**
     * Returns the URL to the remote folder which a used to put all generated
     * stuff, e.g. output of staging processors.
     *
     * @return The URL to the remote 'generated' folder.
     */
    URL getGeneratedFolderUrl();

    /**
     * Set the Id of the access point used to perform this transfer. This Id is
     * used later for cleanup as it allows the mapping of the staging URL to a
     * local folder.
     *
     * @param pAccessPointId The Id of the access point.
     */
    void setAccessPointId(String pAccessPointId);

    /**
     * Returns the Id of the used access point.
     *
     * @return The Id of the access point.
     */
    String getAccessPointId();

    /**
     * Get the list of staging processors.
     *
     * @return The list of staging processors.
     */
    Set<StagingProcessor> getStagingProcessors();

    /**
     * Set a list of staging processors.
     *
     * @param stagingProcessors A list of staging processors.
     */
    void setStagingProcessors(Set<StagingProcessor> stagingProcessors);
}
