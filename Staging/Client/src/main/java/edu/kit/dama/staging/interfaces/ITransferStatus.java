/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.staging.interfaces;

/**
 *
 * @author jejkal
 */
public interface ITransferStatus {

    /**
     * Get the integer representation of a specific element
     *
     * @return The integer representation
     */
    int getId();

    /**
     * Returns true if the status represents a value which allows the ingest
     * finalization.
     *
     * @return True if finalization is possible.
     */
    boolean isFinalizationPossible();

    /**
     * Returns true if the status represents a value which allows user
     * interaction, e.g. file transfer.
     *
     * @return True if interaction is possible.
     */
    boolean isUserInteractionPossible();

    /**
     * Returns true if the status is final and successful. In case of a download
     * 'true' be returned only if the download is available to the user. In case
     * of an ingest, 'true' should be returned if the data is ingested was
     * transferred to the final storage location.
     *
     * @return True if the transfer is in a final stage.
     */
    boolean isFinalState();

    /**
     * Returns 'true' if the status represents an error state.
     *
     * @return True in case of an error state.
     */
    boolean isErrorState();
}
