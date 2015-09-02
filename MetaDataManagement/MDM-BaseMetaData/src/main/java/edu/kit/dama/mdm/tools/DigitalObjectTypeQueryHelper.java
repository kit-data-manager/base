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
package edu.kit.dama.mdm.tools;

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.FilterOutput;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.DigitalObjectType;
import edu.kit.dama.mdm.base.ObjectTypeMapping;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class DigitalObjectTypeQueryHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectTypeQueryHelper.class);

  /**
   * Get types assigned to the provided digital object. Typically, each object
   * should have one type but for special use cases there might be multiple
   * types.
   *
   * @param pInputObject The input object.
   * @param pContext The context used to authorize the access.
   *
   * @return A list of digital object types assigned to the provided digital
   * object.
   *
   * @throws UnauthorizedAccessAttemptException if pContext is not authorized to
   * perform the query.
   */
  public static List<DigitalObjectType> getTypesOfObject(DigitalObject pInputObject, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    if (pInputObject == null) {
      throw new IllegalArgumentException("Argument pInputObject should not be null.");
    }
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pContext);
    try {
      return mdm.findResultList("SELECT t FROM ObjectTypeMapping m, DigitalObjectType t WHERE m.objectType.id = t.id AND m.digitalObject.baseId = " + pInputObject.getBaseId(), DigitalObjectType.class);
    } finally {
      mdm.close();
    }
  }

  /**
   * Get all accessible digital objects to provided type is assigned to. The
   * method is accessible if pContext contains at least the role GUEST and the
   * result is filtered for GUEST access to elements of the complete list of
   * digital objects having pObjectType assigned.
   *
   * @param pObjectType The object type to query for.
   * @param pContext The context used to authorize the access.
   *
   * @return A list of accessible digital objects having pObjectType assigned.
   *
   * @throws UnauthorizedAccessAttemptException if pContext is not authorized to
   * perform the query.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  @FilterOutput(roleRequired = Role.GUEST)
  public static List<DigitalObject> getDigitalObjectsByDigitalObjectType(DigitalObjectType pObjectType, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    if (pObjectType == null) {
      throw new IllegalArgumentException("Argument pObjectType should not be null.");
    }

    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pContext);
    try {
      LOGGER.debug("Getting ObjectTypeMapping for type with id {}", pObjectType.getId());
      List<ObjectTypeMapping> resultMappings = mdm.findResultList("SELECT m FROM ObjectTypeMapping m WHERE m.objectType.id = " + pObjectType.getId(), ObjectTypeMapping.class);
      LOGGER.debug("Obtaining digital objects from mappings.");
      List<DigitalObject> result = new ArrayList<>();
      for (ObjectTypeMapping mapping : resultMappings) {
        result.add(mapping.getDigitalObject());
      }
      LOGGER.debug("Obtained {} digital object(s). Returning filtered result list.", result.size());
      return result;
    } finally {
      mdm.close();
    }
  }

  /**
   * Get all accessible digital objects to provided type is assigned to. The
   * method is accessible if pContext contains at least the role GUEST and the
   * result is filtered for GUEST access to elements of the complete list of
   * digital objects having pObjectType assigned.
   *
   * @param pObjectType The object type to query for.
   * @param pContext The context used to authorize the access.
   *
   * @return A list of accessible digital objects having pObjectType assigned.
   *
   * @throws UnauthorizedAccessAttemptException if pContext is not authorized to
   * perform the query.
   */
  public static Long getDigitalObjectsByDigitalObjectTypeCount(DigitalObjectType pObjectType, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    if (pObjectType == null) {
      throw new IllegalArgumentException("Argument pObjectType should not be null.");
    }

    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pContext);
    try {
      LOGGER.debug("Returning ObjectTypeMapping count for type with id {}", pObjectType.getId());
      return mdm.findSingleResult("SELECT COUNT(m) FROM ObjectTypeMapping m WHERE m.objectType.id = " + pObjectType.getId(), Long.class);
    } finally {
      mdm.close();
    }
  }

}
