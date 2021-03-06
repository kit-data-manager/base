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
package edu.kit.dama.dataworkflow.exceptions;

/**
 *
 * @author mf6319
 */
public class StagingPreparationException extends DataWorkflowException {

  private static final long serialVersionUID = 4825958613681186836L;

  /**
   * Default constructor.
   */
  public StagingPreparationException() {
    super();
  }

  /**
   * Default constructor.
   *
   * @param pMessage The message.
   */
  public StagingPreparationException(String pMessage) {
    super(pMessage);
  }

  /**
   * Default constructor.
   *
   * @param pMessage The message.
   * @param pCause The cause.
   */
  public StagingPreparationException(String pMessage, Throwable pCause) {
    super(pMessage, pCause);
  }
}
