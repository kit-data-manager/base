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
package edu.kit.dama.mdm.admin.util;

import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.admin.UserPropertyCollection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public final class UserPropertyUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAccessUtil.class);

  /**
   * Hidden constructor.
   */
  private UserPropertyUtil() {
  }

  /**
   * Returns the user property collection for the provided user and the
   * collection identifier. If no entity was found, an empty entity is created
   * and returned.
   *
   * @param pMetadataManager The meta data manager used to query for the
   * property collection.
   * @param pUserId The id of the user for whom the properties will be returned.
   * @param pCollectionId The collection identifier.
   *
   * @return The user properties entity for the collection with the provide if
   * or a new entity of no object was found.
   *
   * @throws UnauthorizedAccessAttemptException If the access via the meta data
   * manager fails.
   */
  public static UserPropertyCollection getProperties(IMetaDataManager pMetadataManager, UserId pUserId, String pCollectionId) throws UnauthorizedAccessAttemptException {
    if (pMetadataManager == null) {
      throw new IllegalArgumentException("Argument pMetadataManager must not be null");
    }
    if (pUserId == null) {
      throw new IllegalArgumentException("Argument pUserId must not be null");
    }
    if (pCollectionId == null) {
      throw new IllegalArgumentException("Argument pCollectionId must not be null");
    }

    LOGGER.debug("Try to obtain user properties for user {} and collection id {} from database", new Object[]{pUserId.getStringRepresentation(), pCollectionId});
    List<UserPropertyCollection> result = pMetadataManager.findResultList(
            "SELECT c FROM UserPropertyCollection c WHERE c.collectionIdentifier='" + pCollectionId + "' AND c.userId='" + pUserId.getStringRepresentation() + "'",
            UserPropertyCollection.class);

    if (result.isEmpty()) {
      //no result
      LOGGER.info("No property collection found for user " + pUserId.getStringRepresentation() + " and identifier " + pCollectionId + ". Creating new entry.");
      UserPropertyCollection newCollection = new UserPropertyCollection(pCollectionId, pUserId.getStringRepresentation());
      result.add(pMetadataManager.save(newCollection));
    } else if (result.size() == 1) {
      //success
      LOGGER.debug("Property collection successfully obtained.");
    } else {
      LOGGER.warn("More than one property collection obtained. Possible misconfiguration! Returning first result.");
    }
    return result.get(0);
  }
}
