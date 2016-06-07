/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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

import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import edu.kit.dama.mdm.core.exception.EntityNotFoundException;
import edu.kit.dama.mdm.core.exception.PersistFailedException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 * Abstract transition type handler implementation that defines the basic
 * functionality of each transition type handler. A class offers a way to
 * configure the handler via the IConfigurableAdapter interface.
 *
 * @author jejkal
 * @param <C> The transition entity type that is returned when calling {@link #getTransitionEntity(java.lang.String)
 * }. Therefore it is not the type of the transition but the actual entity
 * describing the transition, e.g. a DataWorkflowTask for
 * TransitionType.DATA_WORKFLOW.
 */
public abstract class AbstractTransitionTypeHandler<C> implements IConfigurableAdapter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractTransitionTypeHandler.class);

    public AbstractTransitionTypeHandler() {
    }

    public abstract DigitalObjectTransition<C> factoryTransitionEntity();

    /**
     * Parse the provided transition entity data. Typically,
     * pTransitionEntityData will be provided via remote (RESTful) interface.
     * Therefore, pTransitionEntityData should be expected to be a JSON object
     * parsable as JSONObject.
     *
     * A special case that should be supported by every handler implementation
     * is providing the transitionEntityId of a previously persisted entity. In
     * this case a JSON structure in the form {"transitionEntityId":"12345"}
     * should be provided.
     *
     * However, the format of pTransitionEntityData depends on the handler
     * implementation. If the handler fails parsing pTransitionEntityData an
     * IllegalArgumentException might be thrown.
     *
     * @param pTransitionEntityData The transition entity data, preferable in
     * JSON format.
     *
     * @return The transition entity. This entity may or may not be persisted,
     * e.g. has a transitionEntityId or not.
     *
     * @throws EntityNotFoundException If the entity data contains an entity id
     * but the according entity was not found.
     */
    public C parseTransitionEntityData(String pTransitionEntityData) throws EntityNotFoundException {
        LOGGER.debug("Try to parse entity id from transition entity data '{}'", pTransitionEntityData);
        String entityId = parseTransitionEntityIdFromData(pTransitionEntityData);
        if (entityId != null) {
            LOGGER.debug("Transition entity id '{}' successfully parsed. Try to get entity.", entityId);
            return getTransitionEntity(entityId);
        } else {
            LOGGER.debug("No transition entity id found. Handling transition entity data by handler implementation.");
        }

        return handleTransitionEntityData(pTransitionEntityData);
    }

    /**
     * Try to parse the provided transition entity data assuming that it
     * contains the transitionEntityId. Therefor, pTransitionEntityData must be
     * a JSON object in the format {"transitionEntityId":"12345"}. If the format
     * is different 'null' is returned.
     *
     * @param pTransitionEntityData The transition entity data.
     *
     * @return The transition entity id from the format
     * {"transitionEntityId":"12345"} or null.
     */
    public String parseTransitionEntityIdFromData(String pTransitionEntityData) {
        try {
            return new JSONObject(pTransitionEntityData).getString("transitionEntityId");
        } catch (JSONException ex) {
            //unable to parse transitionEntityId JSON type
            return null;
        }
    }

    /**
     * Obtain a transition entity by its persistent entityId. To obtain the
     * entity the handler has to access the configured persistence backend and
     * returns the according entity.
     *
     * @param pTransitionEntityId The persistent transition entity id.
     *
     * @return The transition entity.
     *
     * @throws EntityNotFoundException If no transition entity was found for the
     * provided id.
     */
    public C getTransitionEntity(String pTransitionEntityId) throws EntityNotFoundException {
        return loadTransitionEntity(pTransitionEntityId);
    }

    /**
     * Extract the transition entity id from the provided transition entity. The
     * implementation of this method is responsible for checking whether the
     * provided entity is already persisted or not. If it is not persisted, this
     * should be done in order to be able to obtain the transition entity id. If
     * the entity is already persisted, only the id has to be obtained and
     * returned.
     *
     * @param pTransitionEntity The transition entity.
     *
     * @return The transition entity id.
     *
     * @throws PersistFailedException If the provided entity was not persisted,
     * yet, but persisting has failed.
     */
    public abstract String getTransitionEntityId(C pTransitionEntity) throws PersistFailedException;

    /**
     * Handle the provided transition entity data and return an according
     * transition entity. Typically, pTransitionEntityData will be provided via
     * remote (RESTful) interface. Therefore, pTransitionEntityData should be
     * expected to be a JSON object parsable as JSONObject.
     *
     * In some cases, e.g. if the transition entity has already been persisted,
     * pTransitionEntityData may contain a JSON structure containing the
     * transition entity id. In this case the handler should obtain the entity
     * from the backend and return it.
     *
     * @param pTransitionEntityData The transition entity data, preferable in
     * JSON format.
     *
     * @return The transition entity.
     */
    public abstract C handleTransitionEntityData(String pTransitionEntityData);

    /**
     * Obtain the transition entity for the provided id. The provided
     * pTransitionEntityId should be used by the handler to obtain and return
     * the transition entity from the configured backend.
     *
     * @param pTransitionEntityId The transition entity id stored in the
     * DigitalObjectTransition.
     *
     * @return The transition entity.
     *
     * @throws EntityNotFoundException If the transition entity could not be
     * found.
     */
    public abstract C loadTransitionEntity(String pTransitionEntityId) throws EntityNotFoundException;

}
