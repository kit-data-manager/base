/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
package edu.kit.dama.mdm.dataorganization.service.core;

import edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizerImpl;

/**
 * Is a factory for {@link DataOrganizerImpl} for getting the DataOrganizerImpl
 * implementation instance specific to your system.
 *
 * @author pasic
 */
public class DataOrganizerFactory {

  private final DataOrganizerImpl dataOrganizer;
  private static DataOrganizerFactory instance = null;

  /**
   * Default constructor.
   */
  protected DataOrganizerFactory() {
    dataOrganizer = new DataOrganizerImpl();
  }

  /**
   * Obtain a factory instance.
   *
   * @return A factory.
   */
  public final static DataOrganizerFactory getInstance() {
    if (null == instance) {
      instance = new DataOrganizerFactory();
    }
    return instance;
  }

  /**
   * Obtain a reference on the system-specific DataOrganizerImpl implementation
   * instance.
   *
   * @return A data organizer.
   */
  public final DataOrganizerImpl getDataOrganizer() {
    return dataOrganizer;
  }
}
