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
package edu.kit.dama.mdm.content;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import edu.kit.dama.mdm.base.TransitionType;
import edu.kit.dama.mdm.core.exception.EntityNotFoundException;
import edu.kit.dama.mdm.tools.AbstractTransitionTypeHandler;
import edu.kit.dama.mdm.tools.TransitionTypeHandlerFactory;
import edu.kit.dama.util.CryptUtil;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
@Entity
@DiscriminatorValue(value = "ELASTICSEARCH")
public class ElasticsearchTransition extends DigitalObjectTransition<JSONObject> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ElasticsearchTransition.class);

    private static final long serialVersionUID = -8654831290865696474L;

    /**
     * Default constructor.
     */
    public ElasticsearchTransition() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param pArray The JSON array from which the transition entity id (SHA1
     * hash of the entire content) will be extracted in order to be able to
     * obtain the task entity later by calling
     * {@link #getTransitionEntity(edu.kit.dama.authorization.entities.IAuthorizationContext)}.
     */
    public ElasticsearchTransition(JSONArray pArray) {
        super();
        setTransitionEntityId(CryptUtil.stringToSHA1(pArray.toString()));
    }

    @Override
    public TransitionType getTransitionType() {
        return TransitionType.DATAWORKFLOW;
    }

    @Override
    public JSONObject getTransitionEntity(IAuthorizationContext pContext) {
        try {
            AbstractTransitionTypeHandler<JSONObject> handler = TransitionTypeHandlerFactory.factoryTransitionTypeHandler(TransitionType.ELASTICSEARCH);
            return handler.getTransitionEntity(getTransitionEntityId());
        } catch (ConfigurationException | EntityNotFoundException ex) {
            LOGGER.warn("Failed to obtain ElasticsearchTransition entity for id " + getTransitionEntityId(), ex);
        }
        //wrong configuration or id...just return nothing.
        return null;
    }
}
