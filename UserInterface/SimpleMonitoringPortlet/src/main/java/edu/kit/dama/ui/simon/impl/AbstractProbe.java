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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.ui.simon.impl;

import edu.kit.dama.ui.simon.exceptions.ProbeConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public abstract class AbstractProbe {

  private static Logger LOGGER = LoggerFactory.getLogger(AbstractProbe.class);

  public enum PROBE_STATUS {

    SUCCEEDED, UPDATING, FAILED, UNKNOWN, UNAVAILABLE
  }
  private PROBE_STATUS currentStatus = PROBE_STATUS.UNKNOWN;
  private String name = null;
  private String category = "Uncategorized";

  public String getName() {
    return name;
  }

  public void setName(String pName) {
    if (pName == null) {
      name = "Unnamed";
    } else {
      name = pName;
    }
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String pCategory) {
    if (pCategory != null) {
      category = pCategory;
    }
  }

  public final void refreshProbeStatus() {
    switch (getCurrentStatus()) {
      case FAILED:
      case SUCCEEDED:
      case UNKNOWN:
        //update available, setting new status to updating and performing update
        setCurrentStatus(PROBE_STATUS.UPDATING);
        //perform update and set status according to result
        setCurrentStatus(checkProbe() ? PROBE_STATUS.SUCCEEDED : PROBE_STATUS.FAILED);
        break;
      case UPDATING:
        //update running
        LOGGER.info("Checking probe status not possible. Update is still running.");
        break;
      case UNAVAILABLE:
        //not possible
        LOGGER.info("Checking probe status not possible. Probe is unavailable.");
        break;
    }
  }

  abstract boolean checkProbe();

  public final boolean configure(PropertiesConfiguration pProperties) {
    boolean result = false;
    try {
      configureProbe(pProperties);
      result = true;
    } catch (ProbeConfigurationException ex) {
      LOGGER.error("Failed to configure probe", ex);
      setCurrentStatus(PROBE_STATUS.UNAVAILABLE);
    }
    return result;
  }

  abstract void configureProbe(PropertiesConfiguration pProperties) throws ProbeConfigurationException;

  /**
   * Return all configuration property keys of this this probe. These keys are
   * used to configure the probe at runtime.
   *
   * @return An array of property keys.
   */
  public abstract String[] getConfigurationPropertyKeys();

  /**
   * Get a plain description for the provided configuration property key 'pKey'.
   * This method may be used for user interfaces to give the user some idea of
   * the property and valid ranges.
   *
   * @param pKey The key for which a description should be returned.
   *
   * @return The description.
   */
  public abstract String getConfigurationPropertyDescription(String pKey);

  public final PROBE_STATUS getCurrentStatus() {
    return currentStatus;
  }

  public final void setCurrentStatus(PROBE_STATUS pNewStatus) {
    currentStatus = pNewStatus;
  }
}
