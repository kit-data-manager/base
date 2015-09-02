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
package edu.kit.dama.rest.util;

import edu.kit.dama.rest.base.types.ModuleInformation;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public final class CheckServiceHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(CheckServiceHelper.class);

  public static List<ModuleInformation> getModuleInformation() {
    LOGGER.debug("Obtaining module information.");
    List<ModuleInformation> info = new ArrayList<>();
    try {
      LOGGER.debug("Getting MANIFEST.MF entries from class loader.");
      Enumeration<URL> resources = CheckServiceHelper.class.getClassLoader().getResources("META-INF/MANIFEST.MF");

      LOGGER.debug("Obtained resources. Start iteration.");
      while (resources.hasMoreElements()) {
        LOGGER.debug("Checking new MANIFEST.MF");
        Manifest manifest = new Manifest(resources.nextElement().openStream());
        Attributes mainAttributes = manifest.getMainAttributes();
        String vendorId = mainAttributes.getValue("Implementation-Vendor-Id");
        LOGGER.debug("Checking vendor id '{}'", vendorId);
        if (vendorId != null && vendorId.startsWith("edu.kit.dama")) {
          LOGGER.debug("Extracting module information from MANIFEST.");
          ModuleInformation moduleInfo = new ModuleInformation(
                  mainAttributes.getValue("Implementation-Title"),
                  mainAttributes.getValue("Implementation-Version"),
                  mainAttributes.getValue("Implementation-Build"),
                  mainAttributes.getValue("Build-Time"));
          info.add(moduleInfo);
          LOGGER.debug("Added new module information {} to result list.", moduleInfo);
        } else {
          LOGGER.debug("Vendor id is not usable. Ignoring MANIFEST entry.");
        }
      }
    } catch (IOException ex) {
      //failed to obtain module information
      LOGGER.error("Failed to obtain module information", ex);
    }
    return info;
  }

}
