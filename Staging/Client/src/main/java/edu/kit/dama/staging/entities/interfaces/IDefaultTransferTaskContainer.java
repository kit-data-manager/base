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

import edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import java.net.URL;

/**
 *
 * @author jejkal
 */
public interface IDefaultTransferTaskContainer {

    /**
     * Get the transfer id which is used to obtain the transfer information
     * entity.
     *
     * @return The transfer id.
     */
    Long getTransferId();

    /**
     * Returns whether the container is closed or not.
     *
     * @return TRUE = container is closed.
     */
    Boolean isClosed();

    /**
     * Get the REST service URL used to query for the transfer information.
     *
     * @return The service URL.
     */
    String getServiceUrl();

    /**
     * Returns the (sub-)tree of files that should be downloaded.
     *
     * @return The (sub-)tree to download.
     */
    IDefaultDataOrganizationNode getTree();

    /**
     * Returns the destination URL.
     *
     * @return The transfer destination.
     */
    URL getDestination();

    /**
     * Get the container type (INGEST or DOWNLOAD).
     *
     * @return The container type.
     */
    TransferTaskContainer.TYPE getType();
}
