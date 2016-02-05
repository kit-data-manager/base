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
package edu.kit.dama.mdm.dataworkflow;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import edu.kit.dama.mdm.base.TransitionType;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataworkflow.tools.DataWorkflowTaskSecureQueryHelper;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
@Entity
@DiscriminatorValue(value = "DATAWORKFLOW")
public class DataWorkflowTransition extends DigitalObjectTransition<DataWorkflowTask> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DataWorkflowTransition.class);

    private static final long serialVersionUID = -8654831290865696474L;

    /**
     * Default constructor.
     */
    public DataWorkflowTransition() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param pTask The task from which the transition entity id (the task id)
     * will be extracted in order to be able to obtain the task entity later by
     * calling
     * {@link #getTransitionEntity(edu.kit.dama.authorization.entities.IAuthorizationContext)}.
     */
    public DataWorkflowTransition(DataWorkflowTask pTask) {
        super();
        setTransitionEntityId(Long.toString(pTask.getId()));
    }

    @Override
    public TransitionType getTransitionType() {
        return TransitionType.DATAWORKFLOW;
    }

    @Override
    public DataWorkflowTask getTransitionEntity(IAuthorizationContext pContext) {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(pContext);
        try {
            return new DataWorkflowTaskSecureQueryHelper().getDataWorkflowTaskById(Long.parseLong(getTransitionEntityId()), mdm, pContext);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.warn("Failed to obtain DataWorkflow entity for id " + getTransitionEntityId(), ex);
        } finally {
            mdm.close();
        }

        //unauthorized access exception...just return nothing.
        return null;
    }
}
