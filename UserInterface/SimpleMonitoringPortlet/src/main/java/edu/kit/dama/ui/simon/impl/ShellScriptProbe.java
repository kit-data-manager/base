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
package edu.kit.dama.ui.simon.impl;

import edu.kit.dama.ui.simon.exceptions.ProbeConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class ShellScriptProbe extends AbstractProbe {

  private static Logger LOGGER = LoggerFactory.getLogger(ShellScriptProbe.class);
  private final String SCRIPT_LOCATION_KEY = "script.location";
  private final String SCRIPT_ARGUMENTS_KEY = "script.arguments";
  private String script = null;
  private String arguments = null;

  @Override
  public boolean checkProbe() {
    BufferedReader brStdOut = null;
    BufferedReader brStdErr = null;
    try {
      String line;
      Process p = Runtime.getRuntime().exec("sh " + script + ((arguments != null) ? " " + arguments : ""));
      brStdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
      brStdErr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      while ((line = brStdOut.readLine()) != null) {
        LOGGER.debug("{}", line);
      }
      brStdOut.close();
      while ((line = brStdErr.readLine()) != null) {
        LOGGER.warn("{}", line);
      }
      brStdErr.close();
      return p.waitFor() == 0;
    } catch (IOException err) {
      LOGGER.error("Script execution failed", err);
      return false;
    } catch (InterruptedException err) {
      LOGGER.error("Script execution might have failed", err);
      return false;
    } finally {
      if (brStdErr != null) {
        try {
          brStdErr.close();
        } catch (IOException ex) {
        }
      }
      if (brStdOut != null) {
        try {
          brStdOut.close();
        } catch (IOException ex) {
        }
      }
    }
  }

  @Override
  public void configureProbe(PropertiesConfiguration pProperties) throws ProbeConfigurationException {
    LOGGER.debug("Configuring shell script probe...");
    script = pProperties.getString(SCRIPT_LOCATION_KEY);
    if (script == null) {
      throw new ProbeConfigurationException("Property " + SCRIPT_LOCATION_KEY + " is missing.");
    }

    File f = new File(script);

    if (!f.exists()) {
      throw new ProbeConfigurationException("Script " + script + " does not exist.");
    } else {
      LOGGER.debug(" - Setting property {} to value {}", new Object[]{SCRIPT_LOCATION_KEY, script});
    }

    arguments = pProperties.getString(SCRIPT_ARGUMENTS_KEY);
    if (arguments != null) {
      LOGGER.debug("Using script arguments '{}'", arguments);
    } else {
      LOGGER.debug("Using no script arguments.");
    }


    LOGGER.debug("Shell script probe successfully configured.");
  }

  @Override
  public String[] getConfigurationPropertyKeys() {
    return new String[]{SCRIPT_LOCATION_KEY, SCRIPT_ARGUMENTS_KEY};

  }

  @Override
  public String getConfigurationPropertyDescription(String pKey) {
    if (SCRIPT_LOCATION_KEY.equals(pKey)) {
      return "The full path to the script executed by this probe. "
              + "The script must be accessible, executable and must return 0 in case of a successful execution. (e.g. /tmp/myScript.sh)";
    } else if (SCRIPT_ARGUMENTS_KEY.equals(pKey)) {
      return "Optional arguments for the provided script.";
    }
    return "Unknown property";
  }
}
