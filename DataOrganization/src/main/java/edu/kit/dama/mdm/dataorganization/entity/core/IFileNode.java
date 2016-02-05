/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.dataorganization.entity.core;

import edu.kit.dama.commons.types.ILFN;

/**
 * The IFileNode represents an abstraction of the file-proxy. Besides the
 * properties inherited from {@link IDataOrganizationNode} it has a
 * logicalFileName property which is an unique identifier used to identify
 * StorageVirtualisationService layer file and for example retrieve its content
 * as a bitstream.
 *
 * @author pasic
 */
public interface IFileNode extends IDataOrganizationNode {

  /**
   * Gets the logical file name.
   *
   * @return logical file name
   */
  ILFN getLogicalFileName();

  /**
   * Sets the logical file name.
   *
   * @param logicalFileName The logical file name.
   */
  void setLogicalFileName(ILFN logicalFileName);
}
