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
package edu.kit.dama.staging.entities.download;

import edu.kit.dama.staging.interfaces.ITransferStatus;

/**
 *
 * @author jejkal
 */
public enum DOWNLOAD_STATUS implements ITransferStatus {

  UNKNOWN(0),//Unknown status
  PREPARING(1), //download is preparing or inside prepareDownload()
  PREPARATION_FAILED(2), //preparation has failed, interaction by an administrator is needed
  DOWNLOAD_READY(4), //download is ready and can be accessed by the user
  DOWNLOAD_REMOVED(8), //download was virtually removed. All data will be deleted during the next cleanup cycle
  SCHEDULED(16); //download is scheduled for preparation
  private final int id;

  /**
   * Default constructor.
   *
   * @param pId The status id.
   */
  DOWNLOAD_STATUS(int pId) {
    id = pId;
  }

  @Override
  public int getId() {
    return id;
  }

  @Override
  public boolean isUserInteractionPossible() {
    switch (this) {
      case DOWNLOAD_READY:
        return true;
    }
    return false;
  }

  @Override
  public boolean isFinalState() {
    switch (this) {
      case DOWNLOAD_READY:
        return true;
    }
    return false;
  }

  @Override
  public boolean isErrorState() {
    switch (this) {
      case PREPARATION_FAILED:
        return true;
    }
    return false;
  }

  /**
   * Converts the integer representation of a status to its enumeration type
   *
   * @param pId The ID of the status.
   *
   * @return The status enum corresponding to pId.
   */
  public static DOWNLOAD_STATUS idToStatus(int pId) {
    for (DOWNLOAD_STATUS status : DOWNLOAD_STATUS.values()) {
      if (status.getId() == pId) {
        return status;
      }
    }
    return DOWNLOAD_STATUS.UNKNOWN;
  }

  /**
   * Check if one specific status is set at pValue. This method can be used to
   * check the result of linkStatus() for different status codes easily.
   *
   * @param pValue The (linked) value holding one or more status codes.
   * @param pStatus The status to check.
   *
   * @return boolean TRUE if pStatus is set at pValue.
   */
  public static boolean hasStatus(int pValue, DOWNLOAD_STATUS pStatus) {
    if (pStatus.equals(DOWNLOAD_STATUS.UNKNOWN) && pValue == 0) {
      return true;
    }
    return (pValue & pStatus.getId()) != 0;
  }

  /**
   * Combine different status codes to allow querying easily for more than one
   * status code.
   *
   * @param pStatus One or more status codes that will be combined.
   *
   * @return int The combined value containing all status codes of pStatus.
   */
  public static int combineStatus(DOWNLOAD_STATUS... pStatus) {
    int result = 0;

    for (DOWNLOAD_STATUS status : pStatus) {
      result |= status.getId();
    }
    return result;
  }
}
