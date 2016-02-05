/*
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.staging.entities.interfaces;

import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import java.util.Set;

/**
 *
 * @author jejkal
 */
public interface IDefaultIngestInformation extends ISimpleTransferInformation {

    /**
     * Returns the status.
     *
     * @return the status
     */
    int getStatus();

    long getLastUpdate();

    long getExpiresAt();

    String getTransferId();

    String getDigitalObjectUuid();

    String getOwnerUuid();

    String getClientAccessUrl();

    String getStagingUrl();

    String getStorageUrl();

    String getErrorMessage();

    String getAccessPointId();

    /**
     * Get all assigned staging processors.
     *
     * @return A list of assigned staging processors.
     */
    Set<? extends ISimpleStagingProcessor> getStagingProcessors();

}
