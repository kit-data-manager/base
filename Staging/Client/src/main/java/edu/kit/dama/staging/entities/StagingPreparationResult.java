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
package edu.kit.dama.staging.entities;

import edu.kit.dama.staging.interfaces.ITransferStatus;

/**
 *
 * @author jejkal
 * @param <C> An implementation of ITransferStatus.
 */
public class StagingPreparationResult<C extends ITransferStatus> {

  private C status;
  private String errorMessage = null;
  private String stagingUrl = null;

  /**
   * Default constructor.
   */
  public StagingPreparationResult() {
  }

  /**
   * Default constructor.
   *
   * @param pStatus The preparation status.
   * @param pErrorMessage A optional error message.
   */
  public StagingPreparationResult(C pStatus, String pErrorMessage) {
    this(pStatus, pErrorMessage, null);
  }

  /**
   * Default constructor.
   *
   * @param pStatus The preparation status.
   * @param pErrorMessage A optional error message.
   * @param pStagingUrl The resulting staging URL that can be used to
   * upload/download data.
   */
  public StagingPreparationResult(C pStatus, String pErrorMessage, String pStagingUrl) {
    setStatus(pStatus);
    setErrorMessage(pErrorMessage);
    setStagingUrl(pStagingUrl);
  }

  /**
   * Get the status.
   *
   * @return The status.
   */
  public C getStatus() {
    return status;
  }

  /**
   * Set the status.
   *
   * @param status The status to set.
   */
  public final void setStatus(C status) {
    this.status = status;
  }

  /**
   * Get the error message.
   *
   * @return The errorMessage
   */
  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Set the error message.
   *
   * @param errorMessage The errorMessage to set.
   */
  public final void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * Get the staging URL.
   *
   * @return The stagingUrl.
   */
  public String getStagingUrl() {
    return stagingUrl;
  }

  /**
   * Set the staging URL.
   *
   * @param stagingUrl The stagingUrl to set.
   */
  public final void setStagingUrl(String stagingUrl) {
    this.stagingUrl = stagingUrl;
  }
}
