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
package edu.kit.dama.ui.simon;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import edu.kit.dama.ui.simon.panel.SimonMainPanel;
import edu.kit.dama.ui.simon.util.SimonConfigurator;
import edu.kit.dama.util.DataManagerSettings;
import java.io.File;
import javax.servlet.annotation.WebServlet;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Theme("mytheme")
@SuppressWarnings("serial")
public class SimonPortlet extends UI {

  private static final Logger LOGGER = LoggerFactory.getLogger(SimonPortlet.class);

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = true, ui = SimonPortlet.class)
  public static class Servlet extends VaadinServlet {
  }

  @Override
  protected void init(VaadinRequest request) {
    try {
      String path = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.SIMON_CONFIG_LOCATION_ID, null);
      if (path == null || !new File(path).exists()) {
        throw new ConfigurationException("Configuration element '" + DataManagerSettings.SIMON_CONFIG_LOCATION_ID + "' is not set or not a valid directory.");
      }
      File configLocation = new File(path);
      SimonConfigurator.getSingleton().setConfigLocation(configLocation);
    } catch (ConfigurationException ex) {
      LOGGER.error("Failed to initialize SimpleMonitoring", ex);
    }
    setContent(new SimonMainPanel());
    setWidth("1024px");
    setHeight("800px");
  }
}
