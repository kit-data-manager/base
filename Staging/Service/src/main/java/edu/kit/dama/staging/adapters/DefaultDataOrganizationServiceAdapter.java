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
package edu.kit.dama.staging.adapters;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizationServiceLocal;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.staging.interfaces.IDataOrganizationServiceAdapter;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import java.net.URL;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class DefaultDataOrganizationServiceAdapter implements IDataOrganizationServiceAdapter {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataOrganizationServiceAdapter.class);

  /**
   * Default constructor.
   */
  public DefaultDataOrganizationServiceAdapter() {
    //do nothing as the local service does not need any initalization
  }

  /**
   * Default constructor.
   *
   * @param pUrl Data organization service URL (Not supported, yet)
   */
  public DefaultDataOrganizationServiceAdapter(URL pUrl) {
    //External service not supported in this implementation...we only use a local instance
  }

  @Override
  public boolean saveFileTree(IFileTree pFileTree) {
    boolean result = true;
    if (pFileTree == null) {
      throw new IllegalArgumentException("Argument pFileTree must not be 'null'");
    }
    try {
      LOGGER.debug("Trying to store file tree");
      DataOrganizationServiceLocal.getSingleton().createFileTree(pFileTree, null);
    } catch (EntityExistsException eee) {
      LOGGER.error("Failed to store file tree in data organization service. Entity seems to exist already.", eee);
      result = false;
    }
    return result;
  }

  @Override
  public IFileTree loadFileTree(String pDigitalObjectId) {
    if (pDigitalObjectId == null) {
      throw new IllegalArgumentException("Argument pDigitalObjectId must not be 'null'");
    }
    IFileTree result;
    try {
      LOGGER.debug("Trying to load file tree");
      result = DataOrganizationServiceLocal.getSingleton().loadFileTree(new DigitalObjectId(pDigitalObjectId), null);
    } catch (EntityNotFoundException enfe) {
      LOGGER.error("Failed to load file tree for digital object ID '" + pDigitalObjectId + "'. No associated tree found.", enfe);
      result = null;
    }
    return result;
  }

  @Override
  public boolean configure(Configuration pConfig) throws ConfigurationException {
    //not needed at the moment...maybe some authorization context initialization will be done here in future!?
    return true;
  }
}
