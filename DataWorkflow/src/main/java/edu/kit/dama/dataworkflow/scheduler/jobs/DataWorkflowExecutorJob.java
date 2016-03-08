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
package edu.kit.dama.dataworkflow.scheduler.jobs;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.dataworkflow.client.GenericSubmissionClient;
import edu.kit.dama.dataworkflow.client.parameters.ProcessParameters;
import edu.kit.dama.scheduler.quartz.jobs.AbstractConfigurableJob;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.PropertiesUtil;
import edu.kit.jcommander.generic.status.CommandStatus;
import java.util.Properties;
import javax.persistence.EntityManager;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
@DisallowConcurrentExecution
public class DataWorkflowExecutorJob extends AbstractConfigurableJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataWorkflowExecutorJob.class);
    private static final String GROUP_ID = "groupId";
    private static final String HANDLED_WORKFLOW_TASK_COUNT = "handledWorkflowTaskCount";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        LOGGER.debug("Executing MetadataIndexerJob with job key := {}.", context.getJobDetail().getKey());

        int exitCode = 0;
        try {
            LOGGER.debug("Starting workflow task handling cycle.");
            boolean success = false;
            try {
                LOGGER.debug(" - Configuring task handler");
                final String jobParameters = context.getJobDetail().getJobDataMap().getString("jobParameters");
                Properties props = new Properties();
                if (jobParameters != null) {
                    props = PropertiesUtil.propertiesFromString(jobParameters);
                }
                ProcessParameters param = new ProcessParameters();
                param.group = (props.getProperty(GROUP_ID) != null) ? props.getProperty(GROUP_ID) : Constants.USERS_GROUP_ID;
                try {
                    param.count = (props.getProperty(HANDLED_WORKFLOW_TASK_COUNT) != null) ? Integer.parseInt(props.getProperty(HANDLED_WORKFLOW_TASK_COUNT)) : 10;
                } catch (NumberFormatException ex) {
                    LOGGER.warn("Invalid parameter for argument {} detected. Using default value (10).", HANDLED_WORKFLOW_TASK_COUNT);
                }

                CommandStatus status = GenericSubmissionClient.processTasks(param);
                exitCode = status.getStatusCode();

                LOGGER.info("Job workflow task handling cycle finished. Status code is '{}', status message is '{}'", status.getStatusCode(), status.getStatusMessage());
                success = true;
            } finally {
                if (exitCode == 0 && !success) {
                    LOGGER.warn("Unexpected result detected.");
                    exitCode ^= 1;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while executing DataWorkflowExecutorJob with job key := " + context.getJobDetail().getKey() + ".", ex);
            throw new JobExecutionException(ex);
        }
        context.setResult(exitCode);

        LOGGER.debug("Finishing DataWorkflowExecutorJob with job key := {}, exitCode := {}.", context.getJobDetail().getKey(), exitCode);
    }

    @Override
    public String[] getInternalPropertyKeys() {
        return new String[]{GROUP_ID, HANDLED_WORKFLOW_TASK_COUNT};
    }

    @Override
    public String getInternalPropertyDescription(String pKey) {
        if (null != pKey) {
            switch (pKey) {
                case GROUP_ID:
                    return "The group whose workflow tasks are triggered by this job.";
                case HANDLED_WORKFLOW_TASK_COUNT:
                    return "The max. number of workflow tasks that are triggered during one cycle.";
            }
        }
        return "Unknown property key '" + pKey + "'";
    }

    @Override
    public void validateProperties(Properties pProperties) throws PropertyValidationException {
        EntityManager em = PU.entityManager();
        String group = pProperties.getProperty(GROUP_ID);
        if (group != null) {
            try {
                LOGGER.debug("Valid property GROUP_ID with value '{}' found.", FindUtil.findGroup(em, new GroupId(group)));
            } catch (EntityNotFoundException ex) {
                throw new PropertyValidationException("Group with id '" + group + "' not found.", ex);
            }
        }
        String value = pProperties.getProperty(HANDLED_WORKFLOW_TASK_COUNT);
        try {
            LOGGER.debug("Valid property HANDLED_WORKFLOW_TASK_COUNT with value '{}' found.", Integer.parseInt(value));
        } catch (NumberFormatException ex) {
            throw new PropertyValidationException("Invalid value for max. workflow task count. '" + value + "' is no integer value.", ex);
        }
    }

}
