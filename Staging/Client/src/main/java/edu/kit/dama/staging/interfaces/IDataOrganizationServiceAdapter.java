/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.staging.interfaces;

import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;

/**
 * This interface wraps the access to the configured DataOrganizationService. It
 * is responsible for saving and loading a file tree associated with a digital
 * object.
 *
 * @author Thomas Jejkal <a>mailto:support@kitdatamanager.net</a>
 */
public interface IDataOrganizationServiceAdapter extends IConfigurableAdapter {

    /**
     * Save the provided file tree to the data organization accessed by this
     * adapter. The argument pInputTree must not be null and must represent a
     * tree for a digital object which is not registered yet. If there is
     * already a tree for the associated digital object, this call will fail and
     * return 'false'.
     * 
     * @param pInputTree The tree to register
     * 
     * @return TRUE if the tree could be stored successfully
     */
     boolean saveFileTree(IFileTree pInputTree);

    /**
     * Load a file tree for the provided digital object ID using the data
     * organization service adapter implementation. This method will return the
     * associated file tree if pDigitalObjectId is not null and belongs to a
     * valid file tree.
     *
     * @param pDigitalObjectId The digital object ID of the tree to load
     *
     * @return The file tree
     */
     IFileTree loadFileTree(String pDigitalObjectId);
}
