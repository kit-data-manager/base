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

/**
 *
 * @author jejkal
 */
public interface IDefaultExecutionEnvironment extends ISimpleExecutionEnvironment {
 /**
     * @return the name
     */
    String getName();
  /**
     * @return the description
     */
    String getDescription();
  /**
     * @return the customProperties
     */
    String getCustomProperties();
 /**
     * @return the groupId
     */
    String getGroupId();
 /**
     * @return the handlerImplementationClass
     */
    String getHandlerImplementationClass();
  /**
     * Get the id of the StagingAccessPoint that will be used to provide/obtain
     * data to/from task executions.
     *
     * @return the stagingAccessPointId
     */
    String getStagingAccessPointId();
 /**
     * Get the local path used by the AccessPoint of this
     * EnvironmentConfiguration.
     *
     * @return The accessPointLocalBasePath.
     */
    String getAccessPointLocalBasePath();
 /**
     * Get the number of max. parallel tasks running in this execution
     * environment.
     *
     * @return The number of max. parallel tasks.
     */
    Integer getMaxParallelTasks();

    /**
     * Get whether this ExecutionEnvironment is the default one for the group it
     * is associated with.
     *
     * @return TRUE = This is the default ExecutionEnvironment.
     */
    Boolean isDefaultEnvironment();
 /**
     * Check whether this ExecutionEnvironment is disabled or not.
     *
     * @return TRUE = This ExecutionEnvironment is disabled.
     */
    Boolean isDisabled();
}
