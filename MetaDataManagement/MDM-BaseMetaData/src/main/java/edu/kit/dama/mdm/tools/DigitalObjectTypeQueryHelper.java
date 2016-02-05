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
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.DigitalObjectType;
import edu.kit.dama.mdm.base.ObjectTypeMapping;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
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
     * object loaded using the DigitalObjectType.simple fetch graph.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the query.
     */
    public static List<DigitalObjectType> getTypesOfObject(DigitalObject pInputObject, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pInputObject == null) {
            throw new IllegalArgumentException("Argument pInputObject should not be null.");
        }
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectType.simple");
            return mdm.findResultList("SELECT t FROM ObjectTypeMapping m, DigitalObjectType t WHERE m.objectType.id = t.id AND m.digitalObject.baseId = " + pInputObject.getBaseId(), DigitalObjectType.class);
        } finally {
            mdm.close();
        }
    }

    /**
     * Assigns the provided object type to the provided digital object. Both
     * arguments must be existing, persisted entities. If there is already a
     * mapping between the object and the type, the existing mapping will be
     * returned. Otherwise, a new mapping is created and returned.
     *
     * @param pInputObject The input object.
     * @param pType The digital object type to assign.
     * @param pContext The context used to authorize the access.
     *
     * @return The created/existing type-object mapping.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the operaion.
     */
    public static ObjectTypeMapping assignTypeToObject(DigitalObject pInputObject, DigitalObjectType pType, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pInputObject == null) {
            throw new IllegalArgumentException("Argument pInputObject should not be null.");
        }

        if (pType == null) {
            throw new IllegalArgumentException("Argument pType should not be null.");
        }
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        ObjectTypeMapping existingMapping = mdm.findSingleResult("SELECT m FROM ObjectTypeMapping m WHERE m.digitalObject.baseId=" + pInputObject.getBaseId() + "AND m.objectType.id=" + pType.getId(), ObjectTypeMapping.class);
        if (existingMapping == null) {
            //no mapping exist...create it.
            LOGGER.debug("No existing mapping found for base id {} and type id {}. Creating and returning new mapping.", pInputObject.getBaseId(), pType.getId());
            try {
                ObjectTypeMapping mapping = new ObjectTypeMapping();
                mapping.setDigitalObject(pInputObject);
                mapping.setObjectType(pType);
                return mdm.save(mapping);
            } finally {
                mdm.close();
            }
        } else {
            LOGGER.debug("Existing mapping found for base id {} and type id {}. Returning existing mapping.", pInputObject.getBaseId(), pType.getId());
            return existingMapping;
        }
    }

    /**
     * Check whether the provided type is already assigned to the provided
     * object type to the provided digital object. Both arguments must be
     * existing, persisted entities. If the type is assigned, true is returned.
     * Otherwise, false is returned.
     *
     * @param pInputObject The input object.
     * @param pType The digital object type to check.
     * @param pContext The context used to authorize the access.
     *
     * @return TRUE if pType is assigned to pInputObject.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the operaion.
     */
    public static boolean isTypeAssignedToObject(DigitalObject pInputObject, DigitalObjectType pType, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pInputObject == null) {
            throw new IllegalArgumentException("Argument pInputObject should not be null.");
        }

        if (pType == null) {
            throw new IllegalArgumentException("Argument pType should not be null.");
        }
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            Number resultCount = mdm.findSingleResult("SELECT COUNT(m) FROM ObjectTypeMapping m WHERE m.digitalObject.baseId=" + pInputObject.getBaseId() + " AND m.objectType.id=" + pType.getId(), Number.class);
            return (resultCount == null) ? false : (resultCount.intValue() == 1);
        } finally {
            mdm.close();
        }
    }

    /**
     * Removed the provided object type from the provided digital object. Both
     * arguments must be existing, persisted entities. If there is no mapping
     * between both entities, the call just returns and logs an info message.
     * Otherwise, the mapping is removed..
     *
     * @param pInputObject The input object.
     * @param pType The digital object type to remove.
     * @param pContext The context used to authorize the access.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the operaion.
     */
    public static void removeTypeFromObject(DigitalObject pInputObject, DigitalObjectType pType, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pInputObject == null) {
            throw new IllegalArgumentException("Argument pInputObject should not be null.");
        }

        if (pType == null) {
            throw new IllegalArgumentException("Argument pType should not be null.");
        }

        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        ObjectTypeMapping existingMapping = mdm.findSingleResult("SELECT m FROM ObjectTypeMapping m WHERE m.digitalObject.baseId=" + pInputObject.getBaseId() + " AND m.objectType.id=" + pType.getId(), ObjectTypeMapping.class);
        if (existingMapping == null) {
            //no mapping exist...do nothing.
            LOGGER.info("No existing mapping found for base id {} and type id {}. Skip removal.", pInputObject.getBaseId(), pType.getId());
        } else {
            LOGGER.debug("Existing mapping found for base id {} and type id {}. Removing entity.", pInputObject.getBaseId(), pType.getId());
            try {
                mdm.remove(existingMapping);
            } catch (EntityNotFoundException ex) {
                LOGGER.warn("Failed to remove object mapping due to EntityNotFoundException. Actually, this should never happen so I'll ignore it.", ex);
            } finally {
                mdm.close();
            }
        }
    }

    /**
     * Get all accessible digital objects the provided type is assigned to. The
     * method is accessible if pContext contains at least the role GUEST and the
     * result is filtered for GUEST access to elements of the complete list of
     * digital objects having pObjectType assigned.
     *
     * @param pObjectType The object type to query for.
     * @param pContext The context used to authorize the access.
     *
     * @return A list of accessible digital objects having pObjectType assigned
     * loaded using the DigitalObject.simple fetch graph.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the query.
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
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
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
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the query.
     */
    public static Long getDigitalObjectsByDigitalObjectTypeCount(DigitalObjectType pObjectType, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pObjectType == null) {
            throw new IllegalArgumentException("Argument pObjectType should not be null.");
        }

        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            LOGGER.debug("Returning ObjectTypeMapping count for type with id {}", pObjectType.getId());
            return ((Number) mdm.findSingleResult("SELECT COUNT(m) FROM ObjectTypeMapping m WHERE m.objectType.id = " + pObjectType.getId())).longValue();
        } finally {
            mdm.close();
        }
    }

}
