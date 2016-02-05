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

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.exceptions.ServiceAdapterException;
import java.util.List;

/**
 *
 * @author jejkal
 */
public interface IIngestInformationServiceAdapter extends IConfigurableAdapter {

  /**
   * Get the ingest information for the provided digital object Id. This method
   * returns a single entity if the according ingest information was found and
   * throws an exception if not entity is assigned to the provided digital
   * object id.
   *
   * @param pDigitalObjectId The digital object id to get the ingest information
   * for.
   * @param pContext The context to authorize the access.
   *
   * @return The ingest information associated with pDigitalObjectId.
   *
   * @throws ServiceAdapterException If no entity was found for pDigitalObjectId
   * or if another error occurs.
   */
  IngestInformation getIngestInformation(DigitalObjectId pDigitalObjectId, IAuthorizationContext pContext) throws ServiceAdapterException;

  /**
   * Remove the ingest information for the provided id (primary key).
   *
   * @param pId The id of the ingest to remove.
   * @param pContext The context to authorize the access.
   *
   * @throws ServiceAdapterException If an error occurs.
   */
  void removeIngestInformation(Long pId, IAuthorizationContext pContext) throws ServiceAdapterException;

  /**
   * Returns all ingest informations ready for archiving (status =
   * INGEST_STATUS.PRE_INGEST_FINISHED). This method will return a list a ingest
   * information or an empty list if no entity is ready for archiving.
   *
   * @param pContext The context to authorize the access.
   *
   * @return A list of ingest information ready for archiving or an empty list
   * of no ingest is ready.
   *
   * @throws ServiceAdapterException If anything went wrong.
   */
  List<IngestInformation> getIngestsForArchiving(IAuthorizationContext pContext) throws ServiceAdapterException;

  /**
   * Merge the provided ingest information into the data backend. Mering should
   * affect at least storage URL, the status and the error message.
   *
   * @param pInformation The ingest information containing the new storage URL,
   * state or error message.
   * @param pContext The context to authorize the access.
   *
   * @throws ServiceAdapterException If the update fails.
   */
  void updateIngestInformation(IngestInformation pInformation, IAuthorizationContext pContext) throws ServiceAdapterException;

  /**
   * Returns all ingest informations by their status.
   *
   * @param pStatus The status to search for.
   * @param pContext The context to authorize the access.
   *
   * @return A list of ingest information by their status or an empty list if no
   * ingest was found for status pStatus.
   *
   * @throws ServiceAdapterException If anything went wrong.
   */
  List<IngestInformation> getIngestsByStatus(INGEST_STATUS pStatus, IAuthorizationContext pContext) throws ServiceAdapterException;
}
