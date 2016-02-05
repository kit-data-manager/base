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

import edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS;

/**
 *
 * @author jejkal
 */
public interface ITransferStatusListener {

    /**Event fired if the transfer status has changed
     * 
     * @param pOldStatus The status before the change occured
     * @param pNewStatus The new/current status 
     */
    void fireStatusChangedEvent(TRANSFER_STATUS pOldStatus, TRANSFER_STATUS pNewStatus);

    /**Event fired frequently as long as the transfer is alive and running*/
    void fireTransferAliveEvent();
}
