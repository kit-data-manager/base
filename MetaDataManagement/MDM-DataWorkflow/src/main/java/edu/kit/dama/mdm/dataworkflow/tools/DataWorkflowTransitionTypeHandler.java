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
package edu.kit.dama.mdm.dataworkflow.tools;

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.exception.EntityNotFoundException;
import edu.kit.dama.mdm.core.exception.PersistFailedException;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTransition;
import edu.kit.dama.mdm.tools.AbstractTransitionTypeHandler;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class DataWorkflowTransitionTypeHandler extends AbstractTransitionTypeHandler<DataWorkflowTask> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DataWorkflowTransitionTypeHandler.class);

    @Override
    public DigitalObjectTransition<DataWorkflowTask> factoryTransitionEntity() {
        return new DataWorkflowTransition();
    }

    @Override
    public String getTransitionEntityId(DataWorkflowTask pTransitionEntity) throws PersistFailedException {
        if (pTransitionEntity.getId() == null) {
            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
            try {
                DataWorkflowTask result = mdm.save(pTransitionEntity);
                return Long.toString(result.getId());
            } catch (UnauthorizedAccessAttemptException ex) {
                throw new PersistFailedException("Failed to persist DataWorkflow entity", ex);
            } finally {
                mdm.close();
            }
        }
        return Long.toString(pTransitionEntity.getId());
    }

    @Override
    public DataWorkflowTask handleTransitionEntityData(String pTransitionEntityData) {
        LOGGER.debug("Trying to obtain data workflow entity from entity data '{}'", pTransitionEntityData);
        String id = parseTransitionEntityIdFromData(pTransitionEntityData);
        LOGGER.debug("Extracted data workflow id is '{}'", id);
        if (id != null) {
            try {
                LOGGER.debug("Try to load transition entity for id '{}'", id);
                return loadTransitionEntity(id);
            } catch (EntityNotFoundException ex) {
                //ignore...the exception will come later
            }
        }
        throw new IllegalArgumentException("Transition entity data must be in the format [{\"transitionEntityId\":\"WORKFLOW_TASK_ID\"}] but is '" + pTransitionEntityData + "'.");
    }

    @Override
    public DataWorkflowTask loadTransitionEntity(String pTransitionEntityId) throws EntityNotFoundException {
        long workflowId = Long.parseLong(pTransitionEntityId);
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            DataWorkflowTask result = mdm.find(DataWorkflowTask.class, workflowId);
            if (result == null) {
                throw new EntityNotFoundException("Unable to find data workflow with id " + pTransitionEntityId);
            }
            return result;
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new EntityNotFoundException("Not authorized to query for entity with id " + pTransitionEntityId, ex);
        } finally {
            mdm.close();
        }
    }

    @Override
    public boolean configure(Configuration pConfig) throws ConfigurationException {
        return true;
    }

}
