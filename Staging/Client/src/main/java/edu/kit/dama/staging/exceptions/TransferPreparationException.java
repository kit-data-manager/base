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
package edu.kit.dama.staging.exceptions;

/**
 * General exception implementation thrown during transfer preparation
 *
 * @author jejkal
 */
public class TransferPreparationException extends Exception {

  /**
   * Default constructor.
   *
   * @param pMessage The message.
   */
  public TransferPreparationException(String pMessage) {
    super(pMessage);
  }

  /**
   * Default constructor.
   *
   * @param pMessage The message.
   * @param t The cause.
   */
  public TransferPreparationException(String pMessage, Throwable t) {
    super(pMessage, t);
  }
}
