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
package edu.kit.dama.transfer.client.interfaces;

import edu.kit.dama.transfer.client.types.TransferTask;

/**
 *
 * @author jejkal
 */
public interface ITransferTaskListener {

    /**Event fired if the transfer has started
     * 
     * @param pTask The notifying task
     */
    void transferStarted(TransferTask pTask);

    /**Event fired if the transfer has finished
     * 
     * @param pTask The notifying task
     */
    void transferFinished(TransferTask pTask);

    /**Event fired if the transfer has failed
     * 
     * @param pTask The notifying task
     */
    void transferFailed(TransferTask pTask);
}
