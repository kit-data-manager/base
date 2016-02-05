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
package edu.kit.dama.staging.entities.interfaces;

/**
 *
 * @author jejkal
 */
public interface IDefaultStagingAccessPointConfiguration extends ISimpleStagingAccessPointConfiguration {

    /**
     * Get the unique identifier.
     *
     * @return The unique identifier.
     */
    String getUniqueIdentifier();

    /**
     * Get the implementation class.
     *
     * @return The implementation class.
     */
    String getImplementationClass();

    /**
     * Get this access point's name.
     *
     * @return This access point's name.
     */
    String getName();

    /**
     * Get the description.
     *
     * @return The description.
     */
    String getDescription();

    /**
     * Get the group id this access point is valid for.
     *
     * @return The group id this access point is valid for.
     */
    String getGroupId();

    /**
     * Get the custom properties path.
     *
     * @return The custom properties path.
     */
    String getCustomProperties();

    /**
     * Get the remote base Url.
     *
     * @return The remote base Url.
     */
    String getRemoteBaseUrl();

    /**
     * Get the local base path.
     *
     * @return The local base path.
     */
    String getLocalBasePath();

    /**
     * Check if this access point is the default for the associated group.
     *
     * @return TRUE = This access poin is the default access point.
     */
    boolean isDefaultAccessPoint();

    /**
     * Check if the access point is transient.
     *
     * @return TRUE = the access point is transient.
     */
    boolean isTransientAccessPoint();

    /**
     * Return if this access point is enabled/disabled.
     *
     * @return TRUE = Disabled.
     */
    boolean isDisabled();
}
