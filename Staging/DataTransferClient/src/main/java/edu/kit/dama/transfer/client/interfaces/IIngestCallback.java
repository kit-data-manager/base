/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.transfer.client.interfaces;

/**
 *
 * @author jejkal
 */
public interface IIngestCallback {

    /**Callback event fired, if ingest has just started
     * 
     * @param pTransferId The ID of the transfer
     */
    void ingestStarted(String pTransferId);

    /**Callback event fired frequently, if ingest is running
     * 
     * @param pTransferId The ID of the transfer
     */
    void ingestRunning(String pTransferId);

    /**Callback fired, if ingest has finished
     * 
     * @param pTransferId The ID of the transfer
     * @param pSuccess TRUE = ingest finished successfully
     */
    void ingestFinished(String pTransferId, boolean pSuccess);
}
