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

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper implementation for accessing transitions. It covers the query for
 * transitions having a specific digital object as input or output, the qure for
 * transitions of a specific type and the removal of all transitions involving a
 * specific digital object.
 *
 * @author mf6319
 */
public final class TransitionQueryHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransitionQueryHelper.class);

    /**
     * Get all transitions where pInputObject is in the map of input objects.
     *
     * @param pInputObject The input object.
     * @param pContext The context used to authorize the access.
     *
     * @return A list of transitions where pInputObject is in the map of input
     * objects obtained using the DigitalObjectTransition.simple fetch graph.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the query.
     */
    public static List<DigitalObjectTransition> getTransitionsByInputObject(DigitalObject pInputObject, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectTransition.simple");
            return mdm.findResultList("SELECT t FROM DigitalObjectTransition t JOIN t.inputObjectViewMappings m WHERE m.digitalObject.baseId = " + pInputObject.getBaseId(), DigitalObjectTransition.class);
        } finally {
            mdm.close();
        }
    }

    /**
     * Get all transitions where pInputObject is in the map of input objects and
     * where the transition is of type pTransitionImplementationClass.
     *
     * @param pInputObject The input object.
     * @param pTransitionImplementationClass The transition implementation
     * class.
     * @param pContext The context used to authorize the access.
     *
     * @return A list of transitions of type pTransitionImplementationClass
     * where pInputObject is in the map of input objects obtained using the
     * DigitalObjectTransition.simple fetch graph.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the query.
     */
    public static List<DigitalObjectTransition> getTransitionsByInputObject(DigitalObject pInputObject, Class pTransitionImplementationClass, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectTransition.simple");
            return mdm.findResultList("SELECT t FROM DigitalObjectTransition t JOIN t.inputObjectViewMappings m WHERE TYPE(t) = " + pTransitionImplementationClass.getSimpleName() + " AND m.digitalObject.baseId = " + pInputObject.getBaseId(), DigitalObjectTransition.class);
        } finally {
            mdm.close();
        }
    }

    /**
     * Get all transitions where pOutputObject is in the list of input objects.
     *
     * @param pOutputObject The output object.
     * @param pContext The context used to authorize the access.
     *
     * @return A list of transitions where pOutputObject is in the list of
     * output objects obtained using the DigitalObjectTransition.simple fetch
     * graph.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the query.
     */
    public static List<DigitalObjectTransition> getTransitionsByOutputObject(DigitalObject pOutputObject, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectTransition.simple");
            return mdm.findResultList("SELECT t FROM DigitalObjectTransition t JOIN t.outputObjects o WHERE o.baseId = " + pOutputObject.getBaseId(), DigitalObjectTransition.class);
        } finally {
            mdm.close();
        }
    }

    /**
     * Get all transitions where pOutputObject is in the list of output objects
     * and where the transition is of type pTransitionImplementationClass.
     *
     * @param pOutputObject The output object.
     * @param pTransitionImplementationClass The transition implementation
     * class.
     * @param pContext The context used to authorize the access.
     *
     * @return A list of transitions of type pTransitionImplementationClass
     * where pOutputObject is in the list of outputobjects obtained using the
     * DigitalObjectTransition.simple fetch graph.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the query.
     */
    public static List<DigitalObjectTransition> getTransitionsByOutputObject(DigitalObject pOutputObject, Class pTransitionImplementationClass, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectTransition.simple");
            return mdm.findResultList("SELECT t FROM DigitalObjectTransition t JOIN t.outputObjects o WHERE TYPE(t) = " + pTransitionImplementationClass.getSimpleName() + " AND o.baseId = " + pOutputObject.getBaseId(), DigitalObjectTransition.class);
        } finally {
            mdm.close();
        }
    }

    /**
     * Get the transitions of type pTransitionImplementationClass with the
     * transition entity Id pTransitionEntityId. As the transition entity Id is
     * used to query for a transition entity of a specific type it has to be
     * unique. Therefor, only one or no result are expected.
     *
     * @param pTransitionEntityId The transition entity Id.
     * @param pTransitionImplementationClass The transition implementation
     * class.
     * @param pContext The context used to authorize the access.
     *
     * @return One or no transition for the transition entity Id
     * pTransitionEntityId obtained using the DigitalObjectTransition.simple
     * fetch graph.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the query.
     */
    public static DigitalObjectTransition getTransitionsByTransitionEntityId(String pTransitionEntityId, Class pTransitionImplementationClass, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectTransition.simple");
            return mdm.findSingleResult("SELECT t FROM DigitalObjectTransition t WHERE TYPE(t) = " + pTransitionImplementationClass.getSimpleName() + " AND t.transitionEntityId = '" + pTransitionEntityId + "'", DigitalObjectTransition.class);
        } finally {
            mdm.close();
        }
    }

    /**
     * Remove all transitions where pObject is input or output. This method
     * should only be necessary if pObject is also removed.
     *
     * @param pObject The object.
     * @param pContext The context used to authorize the access.
     *
     * @return The number of removed transitions.
     *
     * @throws UnauthorizedAccessAttemptException if pContext is not authorized
     * to perform the operation.
     */
    public static int removeTransitionsByObject(DigitalObject pObject, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObjectTransition.simple");
        List<DigitalObjectTransition> results = mdm.findResultList("SELECT t FROM DigitalObjectTransition t JOIN t.outputObjects o JOIN t.inputObjectViewMappings m WHERE m.digitalObject.baseId = " + pObject.getBaseId() + " OR o.baseId = " + pObject.getBaseId(), DigitalObjectTransition.class);
        int removeCount = 0;
        try {
            LOGGER.debug("Removing {} transitions for object with id {}", results.size(), pObject.getDigitalObjectId());
            for (DigitalObjectTransition result : results) {
                try {
                    mdm.remove(result);
                    removeCount++;
                } catch (EntityNotFoundException ex) {
                    LOGGER.warn("Unable to remove transition. Entity not found.", ex);
                }
            }
            LOGGER.debug("Object transitions removed.");
        } finally {
            mdm.close();
        }
        return removeCount;
    }
}
