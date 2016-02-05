/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.dataworkflow.exceptions;

/**
 *
 * @author jejkal
 */
public class DataWorkflowProcessingException extends DataWorkflowException {

  private static final long serialVersionUID = -8579125069955385395L;

  /**
   * Default constructor.
   */
  public DataWorkflowProcessingException() {
    super();
  }

  /**
   * Default constructor.
   *
   * @param pMessage The message.
   */
  public DataWorkflowProcessingException(String pMessage) {
    super(pMessage);
  }

  /**
   * Default constructor.
   *
   * @param pMessage The message.
   * @param pCause The cause.
   */
  public DataWorkflowProcessingException(String pMessage, Throwable pCause) {
    super(pMessage, pCause);
  }
}
