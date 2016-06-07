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
package edu.kit.dama.staging.interfaces;

import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.exceptions.ServiceAdapterException;
import java.util.List;

/**
 *
 * @author jejkal
 */
public interface IDownloadInformationServiceAdapter extends IConfigurableAdapter {

  /**
   * Get the download information for the provided digital object Id. For normal
   * users, this method should return a single result.
   *
   * @param pDigitalObjectId The digital object id.
   * @param pContext The authorization context associated with the download
   * information. This argument is needed here as there might be multiple
   * downloads for the same object but for different users.
   *
   * @return The download information associated with pDigitalObjectId.
   *
   * @throws ServiceAdapterException If no entity was found for pDigitalObjectId
   * or if another error occurs.
   */
  DownloadInformation getDownloadInformation(DigitalObjectId pDigitalObjectId, IAuthorizationContext pContext) throws ServiceAdapterException;

  /**
   * Get a download information by its id (primary key). This method is intended
   * to be used internally, e.g. for cleanup, to identify a single download. *
   *
   * @param pId The id.
   * @param pContext The authorization context associated with the download
   * information. This argument is needed here as there might be multiple
   * downloads for the same object but for different users.
   *
   * @return The download information identified by pId.
   *
   * @throws ServiceAdapterException If no entity was found for pId or if
   * another error occurs.
   */
  DownloadInformation getDownloadInformation(Long pId, IAuthorizationContext pContext) throws ServiceAdapterException;

  /**
   * Remove the download information for the provided id (primary key).
   *
   * @param pId The id of the ingest to remove.
   * @param pContext The authorization context.
   *
   * @throws ServiceAdapterException If an error occurs.
   */
  void removeDownloadInformation(Long pId, IAuthorizationContext pContext) throws ServiceAdapterException;

  /**
   * Returns all download informations ready for staging. This method will
   * return a list a download information or an empty list if no entity is ready
   * for staging.
   *
   * @param pContext The authorization context.
   *
   * @return A list of download information ready for staging.
   *
   * @throws ServiceAdapterException If anything went wrong.
   */
  List<DownloadInformation> getDownloadsForStaging(IAuthorizationContext pContext) throws ServiceAdapterException;

  /**
   * Merge the provided download information into the data backend. By default,
   * the merge should only affect the status and the error message.
   *
   * @param pInformation The download information containing the new state and
   * error message.
   * @param pContext The authorization context.
   *
   * @throws ServiceAdapterException If the update fails.
   */
  void updateDownloadInformation(DownloadInformation pInformation, IAuthorizationContext pContext) throws ServiceAdapterException;

  /**
   * Returns all download informations by their status.
   *
   * @param pStatus The status to search for.
   * @param pContext The authorization context.
   *
   * @return A list of download information by their status.
   *
   * @throws ServiceAdapterException If anything went wrong.
   */
  List<DownloadInformation> getDownloadsByStatus(DOWNLOAD_STATUS pStatus, IAuthorizationContext pContext) throws ServiceAdapterException;
}
