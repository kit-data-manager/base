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
package edu.kit.dama.ui.simon.impl;

import edu.kit.dama.ui.simon.exceptions.ProbeConfigurationException;
import edu.kit.dama.util.FileUtils;
import java.io.File;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class MountProbe extends AbstractProbe {

  private static Logger LOGGER = LoggerFactory.getLogger(MountProbe.class);
  private final String MOUNT_POINT_KEY = "mount.point";
  private final String TIMEOUT_KEY = "timeout";
  private String mountPoint = null;
  private int timeout = 1000;

  @Override
  public boolean checkProbe() {
    return FileUtils.isAccessible(new File(mountPoint), timeout);
  }

  @Override
  void configureProbe(PropertiesConfiguration pProperties) throws ProbeConfigurationException {
    LOGGER.debug("Configuring mount probe...");
    mountPoint = pProperties.getString(MOUNT_POINT_KEY);
    if (mountPoint == null) {
      throw new ProbeConfigurationException("Property " + MOUNT_POINT_KEY + " is missing.");
    } else {
      LOGGER.debug(" - Setting property {} to value {}", new Object[]{MOUNT_POINT_KEY, mountPoint});
    }
    String sTimeout = pProperties.getString(TIMEOUT_KEY, "1000");
    try {
      timeout = Integer.parseInt(sTimeout);
      LOGGER.debug(" - Setting property {} to value {}", new Object[]{TIMEOUT_KEY, timeout});
    } catch (NumberFormatException ex) {
      LOGGER.warn("Property " + TIMEOUT_KEY + " is not a valid integer number, using default value 1000.");
      timeout = 1000;
    }
    LOGGER.debug("Mount probe successfully configured.");
  }

  @Override
  public String[] getConfigurationPropertyKeys() {
    return new String[]{MOUNT_POINT_KEY, TIMEOUT_KEY};
  }

  @Override
  public String getConfigurationPropertyDescription(String pKey) {
    if (MOUNT_POINT_KEY.equals(pKey)) {
      return "The mount point which will be checked by this probe.";
    } else if (TIMEOUT_KEY.equals(pKey)) {
      return "The timeout in milliseconds until the probe is marked as FAILED. (Default: 1000 ms)";
    }
    return "Unknown property";
  }
}
