/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.mdm.base;

import edu.kit.dama.commons.types.DigitalObjectId;

/**
 * Interface for getting and setting DigitalObjectId. Access rights are
 * depending on the DigitalObjectId. Public accessible Objects has to implement
 * this interface. It's a unique Id to refer to a digital object. It may be used
 * in 2 ways:
 * <ol> <li>unique id for the digital object</li>
 * <li>reference to link (meta) data to a digital object</li>
 * </ol>
 *
 * @author hartmann-v
 */
public interface IDigitalObjectId {

  /**
   * Set the Id for this instance.
   *
   * @param digitalObjectId the digital object id.
   */
  void setDigitalObjectId(DigitalObjectId digitalObjectId);

  /**
   * Get the id for this instance.
   *
   * @return id of the digital object.
   */
  DigitalObjectId getDigitalObjectId();

}
