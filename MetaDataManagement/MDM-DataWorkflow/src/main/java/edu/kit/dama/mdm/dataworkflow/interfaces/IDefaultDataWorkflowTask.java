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
package edu.kit.dama.mdm.dataworkflow.interfaces;

import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import java.util.Date;

/**
 *
 * @author jejkal
 */
public interface IDefaultDataWorkflowTask extends ISimpleDataWorkflowTask {

    /**
     * Get the task configuration.
     *
     * @return The configuration.
     */
    ISimpleDataWorkflowConfiguration getConfiguration();

    /**
     * Get the environment in which the task will be executed.
     *
     * @return The target environment.
     */
    ISimpleExecutionEnvironment getExecutionEnvironment();

    /**
     * Get the predecessor task.
     *
     * @return The predecessor task.
     */
    ISimpleDataWorkflowTask getPredecessor();

    /**
     * Get the string representation of the object-view map used as input for
     * this task.
     *
     * @return The objectViewMap.
     */
    String getObjectViewMap();

    /**
     * Get the string representation of the object-transfer map used to store
     * the status of all transfers associated with this task.
     *
     * @return The objectTransferMap.
     */
    String getObjectTransferMap();

    /**
     * Get the string representation of the execution settings containing custom
     * properties to parameterize the task execution.
     *
     * @return The executionSettings.
     */
    String getExecutionSettings();

    /**
     * Get the custom application arguments.
     *
     * @return The application arguments.
     */
    String getApplicationArguments();

    /**
     * Get the current task status.
     *
     * @return The current status.
     */
    DataWorkflowTask.TASK_STATUS getStatus();

    /**
     * Get the input directory Url.
     *
     * @return The input directory Url.
     */
    String getInputDirectoryUrl();

    /**
     * Get the output directory Url.
     *
     * @return The output directory Url.
     */
    String getOutputDirectoryUrl();

    /**
     * Get the working directory Url.
     *
     * @return The working directory Url.
     */
    String getWorkingDirectoryUrl();

    /**
     * Get the temp directory Url.
     *
     * @return The temp directory Url.
     */
    String getTempDirectoryUrl();

    /**
     * Get the last error message.
     *
     * @return The errorMessage.
     */
    String getErrorMessage();

    /**
     * Get the last update date.
     *
     * @return The last update date.
     */
    Date getLastUpdate();

    /**
     * Get the custom job id.
     *
     * @return The custom job id.
     */
    String getJobId();

    /**
     * Get the UserId of the executor.
     *
     * @return The UserId of the executor.
     */
    String getExecutorId();

    /**
     * Get the GroupId of the executor group.
     *
     * @return The GroupId of the executor group.
     */
    String getExecutorGroupId();

    /**
     * Get the linked investigation.
     *
     * @return The linked investigationId.
     */
    Long getInvestigationId();

}
