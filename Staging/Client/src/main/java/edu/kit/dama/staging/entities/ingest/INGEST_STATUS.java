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
package edu.kit.dama.staging.entities.ingest;

import edu.kit.dama.staging.interfaces.ITransferStatus;

/**
 *
 * @author jejkal
 */
public enum INGEST_STATUS implements ITransferStatus {

    UNKNOWN(0),//Unknown status
    PREPARING(1), //pre-ingest is still preparing or inside preparePreIngest()
    PREPARATION_FAILED(2), //preparation has failed, interaction by an administrator is needed
    PRE_INGEST_SCHEDULED(4), //ingest prepared...user can start uploading data to the cache
    PRE_INGEST_RUNNING(8), //user started upload using an upload client
    PRE_INGEST_FINISHED(16), //upload has finished, all data has been transferred successfully
    PRE_INGEST_FAILED(32), //pre-ingest has failed for some reason (either before or during upload)
    INGEST_RUNNING(64), //ingest operation to data virtualization is running
    INGEST_FINISHED(128), //ingest operation to data virtualization has finished successfully
    INGEST_FAILED(256), //ingest operation to data virtualization has failed, interaction by an administrator is needed
    INGEST_REMOVED(512); //ingest has been virtually removed. All data will be deleted during the next cleanup cycle

    private final int id;

    /**
     * Default constructor.
     *
     * @param pId The status id.
     */
    INGEST_STATUS(int pId) {
        id = pId;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isFinalizationPossible() {
        switch (this) {
            case PRE_INGEST_FINISHED:
            case PRE_INGEST_RUNNING:
            case PRE_INGEST_SCHEDULED:
                return true;
        }
        return false;
    }

    @Override
    public boolean isUserInteractionPossible() {
        switch (this) {
            case PRE_INGEST_SCHEDULED:
                return true;
        }
        return false;
    }

    @Override
    public boolean isFinalState() {
        switch (this) {
            case INGEST_FINISHED:
                return true;
        }
        return false;
    }

    @Override
    public boolean isErrorState() {
        switch (this) {
            case INGEST_FAILED:
            case PREPARATION_FAILED:
            case PRE_INGEST_FAILED:
                return true;
        }
        return false;
    }

    /**
     * Converts the integer representation of a status to its enumeration type
     *
     * @param pId The ID of the status
     *
     * @return The status enum corresponding to pId
     */
    public static INGEST_STATUS idToStatus(int pId) {
        for (INGEST_STATUS status : INGEST_STATUS.values()) {
            if (status.getId() == pId) {
                return status;
            }
        }
        return INGEST_STATUS.UNKNOWN;
    }

    /**
     * Check if one specific status is set at pValue. This method can be used to
     * check the result of linkStatus() for different status codes easily.
     *
     * @param pValue The (linked) value holding one or more status codes.
     * @param pStatus The status to check.
     *
     * @return boolean TRUE if pStatus is set at pValue.
     */
    public static boolean hasStatus(int pValue, INGEST_STATUS pStatus) {
        if (pStatus.equals(INGEST_STATUS.UNKNOWN) && pValue == 0) {
            return true;
        }
        return (pValue & pStatus.getId()) != 0;
    }

    /**
     * Combine different status codes to allow querying easily for more than one
     * status code.
     *
     * @param pStatus One or more status codes that will be combined.
     *
     * @return int The combined value containing all status codes of pStatus.
     */
    public static int combineStatus(INGEST_STATUS... pStatus) {
        int result = 0;

        for (INGEST_STATUS status : pStatus) {
            result |= status.getId();
        }
        return result;
    }
}
