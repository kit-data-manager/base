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
package edu.kit.dama.staging.entities.interfaces;

/**
 *
 * @author jejkal
 */
public interface IDefaultStagingProcessor extends ISimpleStagingProcessor {

    /**
     * Get the name.
     *
     * @return The name.
     */
    String getName();

    /**
     * Get the implementation class.
     *
     * @return The implementation class.
     */
    String getImplementationClass();

    /**
     * Get the properties.
     *
     * @return The properties.
     */
    String getProperties();

    /**
     * Get the group id this processor is associated with.
     *
     * @return The group id this processor is associated with.
     */
    String getGroupId();

//    /**
//     * Get the type.
//     *
//     * @return The type.
//     */
//    PROCESSOR_TYPE getType();
    /**
     * Set the description.
     *
     * @return The description.
     */
    String getDescription();

    /**
     * Set the priority.
     *
     * @return The priority.
     */
    Byte getPriority();

    /**
     * Check if this processor is selected by default.
     *
     * @return TRUE = This processor is selected by default.
     */
    Boolean isDefaultOn();

    /**
     * Check if this processor is enabled/disabled.
     *
     * @return TRUE = This processor is disabled.
     */
    Boolean isDisabled();

    /**
     * Check if this processor supports processing ingests.
     *
     * @return TRUE = This processor supports processing ingests.
     */
    Boolean isIngestProcessingSupported();

    /**
     * Check if this processor supports processing downloads.
     *
     * @return TRUE = This processor supports processing downloads.
     */
    Boolean isDownloadProcessingSupported();

}
