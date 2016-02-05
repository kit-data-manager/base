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
package edu.kit.dama.mdm.admin.util;

import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.exception.SecretEncryptionException;
import edu.kit.dama.util.Constants;
import java.util.List;
import javax.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class ServiceAccessUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAccessUtil.class);

  /**
   * Performs a query for a service access token. This token is used to
   * authorize the user with the id pUserId for accessing the service with the
   * id pServiceId. If no token exists for this user-service combination, null
   * is returned.
   *
   * @param pMetadataManager The meta data manager used to query for the access
   * token.
   * @param pUserId The id of the user who wants to access the service.
   * @param pServiceId The id of the service to access.
   *
   * @return The service access token from the database or NULL if no token is
   * available for the user and the service.
   *
   * @throws UnauthorizedAccessAttemptException If the access via the meta data
   * manager fails.
   */
  public static ServiceAccessToken getAccessToken(IMetaDataManager pMetadataManager, UserId pUserId, String pServiceId) throws UnauthorizedAccessAttemptException {
    if (pMetadataManager == null) {
      throw new IllegalArgumentException("Argument pMetadataManager must not be null");
    }
    if (pUserId == null) {
      throw new IllegalArgumentException("Argument pUserId must not be null");
    }
    if (pServiceId == null) {
      throw new IllegalArgumentException("Argument pServiceId must not be null");
    }

    try {
      LOGGER.debug("Try to obtain service access token for user {} and service '{}' from database", new Object[]{pUserId.getStringRepresentation(), pServiceId});
      ServiceAccessToken result = pMetadataManager.findSingleResult(
              "SELECT t FROM ServiceAccessToken t WHERE t.serviceId='" + Constants.REST_API_SERVICE_KEY + "' AND t.userId='" + pUserId.getStringRepresentation() + "'",
              ServiceAccessToken.class);
      LOGGER.debug("Access token successfully obtained.");
      return result;
    } catch (NoResultException ex) {
      LOGGER.error("No access token found for user " + pUserId.getStringRepresentation() + " and service " + pServiceId);
      return null;
    }
  }

  /**
   * Returns a user id based on the provided service access token entries
   * associated with the provided service id. This method will return the user
   * id allowed to access the service with the provided key and secret obtained
   * by e.g. a REST service secured via OAuth. Afterwards, the user id may be
   * used within a KIT Data Manager AuthorizationContext for further access
   * decisions.
   *
   * @param pMetadataManager The meta data manager used to query for the access
   * token.
   * @param pKey The plain token key obtained from an external interface,
   * e.g. REST + OAuth.
   * @param pPlainSecret The plain token secret obtained from an external
   * interface, e.g. REST + OAuth.
   * @param pServiceId The id of the service to access.
   *
   * @return The user id allowed to access the service with pPlainKey and
   * pPlainSecret or null, if no access token was found.
   *
   * @throws UnauthorizedAccessAttemptException If the access via the meta data
   * manager fails.
   */
  public static UserId getUserForToken(IMetaDataManager pMetadataManager, String pKey, String pPlainSecret, String pServiceId) throws UnauthorizedAccessAttemptException {
    try {
      ServiceAccessToken template = new ServiceAccessToken();

      template.setTokenKey(pKey);
      template.setSecret(pPlainSecret);
      template.setServiceId(pServiceId);
      List<ServiceAccessToken> token = pMetadataManager.find(template, template);
      ServiceAccessToken result;
      if (token.isEmpty()) {
        LOGGER.warn("No access token returned for service " + pServiceId + " and the provided credentials.");
        return null;
      } else if (token.size() > 1) {
        result = token.get(0);
        LOGGER.warn("More than one token was obtained for service " + pServiceId + " and plain key " + pKey + ". Check configuration! I'll now return the first entry.");
      } else {
        result = token.get(0);
      }
      return new UserId(result.getUserId());
    } catch (SecretEncryptionException ex) {
      LOGGER.error("Failed to obtain userId for service access token", ex);
      return null;
    }
  }

  /**
   * Performs a query for a service access token. This token is used to
   * authorize the user with the plain key pPlainKey for accessing the service
   * with the id pServiceId. If no token exists for this user-service
   * combination, null is returned.
   *
   * @param pMetadataManager The meta data manager used to query for the access
   * token.
   * @param pKey The plain token key obtained from an external interface,
   * e.g. REST + OAuth.
   * @param pServiceId The id of the service to access.
   *
   * @return The service access token from the database or NULL if no token is
   * available for the user and the service.
   *
   * @throws UnauthorizedAccessAttemptException If the access via the meta data
   * manager fails.
   */
  public static ServiceAccessToken getAccessToken(IMetaDataManager pMetadataManager, String pKey, String pServiceId) throws UnauthorizedAccessAttemptException {
    try {
      ServiceAccessToken template = new ServiceAccessToken();
      template.setTokenKey(pKey);
      template.setServiceId(pServiceId);
      List<ServiceAccessToken> token = pMetadataManager.find(template, template);
      ServiceAccessToken result;
      if (token.isEmpty()) {
        LOGGER.warn("No access token returned for service " + pServiceId + " and the provided credentials.");
        return null;
      } else if (token.size() > 1) {
        result = token.get(0);
        LOGGER.warn("More than one token was obtained for service " + pServiceId + " and plain key " + pKey + ". Check configuration! I'll now return the first entry.");
      } else {
        result = token.get(0);
      }
      return result;
    } catch (Exception ex) {
      LOGGER.error("Failed to obtain ServiceAccessToken plain key " + pKey, ex);
      return null;
    }
  }
}
