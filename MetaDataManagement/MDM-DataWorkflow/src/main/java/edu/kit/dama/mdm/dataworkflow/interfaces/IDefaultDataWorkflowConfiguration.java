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

import java.util.Date;
import java.util.Set;

/**
 *
 * @author jejkal
 */
public interface IDefaultDataWorkflowConfiguration extends ISimpleDataWorkflowConfiguration {

    /**
     * Get name of the configuration.
     *
     * @return The name.
     */
    String getName();

    /**
     * Get description of the configuration.
     *
     * @return The description.
     */
    String getDescription();

    /**
     * Get version number of the configuration.
     *
     * @return The version number.
     */
    Integer getVersion();

    /**
     * Get the creation date of the configuration.
     *
     * @return The creation date.
     */
    Date getCreationDate();

    /**
     * Set the keywords describing the configuration.
     *
     * @return Keywords for this configuration.
     */
    String getKeywords();

    /**
     * Get the contact userId for this configuration.
     *
     * @return The contact userId.
     */
    String getContactUserId();

    /**
     * Get the groupId for this configuration.
     *
     * @return The groupId.
     */
    String getGroupId();

    /**
     * Get the URL of the application package.
     *
     * @return The applicationPackageUrl.
     */
    String getApplicationPackageUrl();

    /**
     * Get the application arguments.
     *
     * @return Application arguments .
     */
    String getApplicationArguments();

//    /**
//     * Returns whether this task is flagged as default or not.
//     *
//     * @return TRUE if default, FALSE otherwise.
//     */
//    Boolean isDefaultTask();
//
//    /**
//     * Returns whether this task is flagged as disabled or not.
//     *
//     * @return TRUE if disabled, FALSE otherwise.
//     */
//    Boolean isDisabled();

    /**
     * Get the list of required environment properties.
     *
     * @return The list of required environment properties.
     */
    Set<? extends IDefaultEnvironmentProperty> getRequiredEnvironmentProperties();

}
