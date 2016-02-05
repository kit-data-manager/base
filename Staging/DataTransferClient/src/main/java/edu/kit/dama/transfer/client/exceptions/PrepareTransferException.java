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
package edu.kit.dama.transfer.client.exceptions;

/**
 *
 * @author jejkal
 */
public class PrepareTransferException extends Exception {

    /**Default constructor.
     * 
     * @param pMessage The message.
     */
    public PrepareTransferException(String pMessage) {
        super(pMessage);
    }

    /**Default constructor with cause.
     * 
     * @param pMessage The message.
     * @param pCause The cause.
     */
    public PrepareTransferException(String pMessage, Throwable pCause) {
        super(pMessage, pCause);
    }
}
