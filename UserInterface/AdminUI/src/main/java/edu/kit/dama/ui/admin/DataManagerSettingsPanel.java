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
package edu.kit.dama.ui.admin;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.ui.admin.administration.usergroup.UserGroupAdministrationTab;
import edu.kit.dama.ui.admin.staging.accesspoints.StagingAccessPointConfigurationTab;
import edu.kit.dama.ui.admin.staging.processors.StagingProcessorConfigurationTab;
import edu.kit.dama.ui.admin.administration.user.UserDataAdministrationTab;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.ui.admin.exception.UnsupportedEnumException;
import edu.kit.dama.ui.admin.utils.IconContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class DataManagerSettingsPanel extends CustomComponent {

  private static final Logger LOGGER
          = LoggerFactory.getLogger(DataManagerSettingsPanel.class);

  public final static String DEBUG_ID_PREFIX
          = DataManagerSettingsPanel.class.getName() + "_";
  private final AdminUIMainView parentApp;
  private TabSheet mainComponentContainer;
  private VerticalLayout mainLayout = null;
  private UserDataAdministrationTab userAdministrationTab;
  private UserGroupAdministrationTab groupAdministrationTab;
  // private StagingAccessProviderConfigurationTab accessProviderConfigurationTab;
  private StagingAccessPointConfigurationTab accessPointConfigurationTab;
  private StagingProcessorConfigurationTab processorConfigurationTab;

  public enum Tab {

    ALL,
    USER_ADMINISTRATION,
    GROUP_ADMINISTRATION,
    ACCESS_POINT_CONFIGURATION,
    // ACCESS_PROVIDER_CONFIGURATION,
    PROCESSOR_CONFIGURATION;
  }

  public DataManagerSettingsPanel(AdminUIMainView pParentApp) {
    parentApp = pParentApp;
    LOGGER.debug(new StringBuilder("Building ").append(DEBUG_ID_PREFIX)
            .append(" ...").toString());

    setId(DEBUG_ID_PREFIX);
    buildMainComponentContainer();
    setCompositionRoot(mainLayout);
    setSizeFull();
  }

  private void buildMainComponentContainer() {
    String id = "mainComponent";
    LOGGER.debug(new StringBuilder("Building ").append(
            DEBUG_ID_PREFIX).append(id).append(" ...").toString());

    mainComponentContainer = new TabSheet();
    mainComponentContainer.setId(new StringBuilder(DEBUG_ID_PREFIX).append(id).toString());
    mainComponentContainer.setSizeFull();
    mainComponentContainer.setImmediate(true);

    // Add tab for user administration
    mainComponentContainer.addTab(getUserAdministrationTab(), "User Administration",
            new ThemeResource(IconContainer.USERS));
    mainComponentContainer.addTab(getGroupAdministrationTab(), "Group Administration",
            new ThemeResource(IconContainer.GROUPS));
    /* mainComponentContainer.addTab(getAccessProviderConfigurationTab(),
     "Staging Access Provider Configuration", new ThemeResource(IconContainer.GEARS));*/
    mainComponentContainer.addTab(getAccessPointConfigurationTab(),
            "Staging Access Point Configuration", new ThemeResource(IconContainer.GEARS));
    mainComponentContainer.addTab(getProcessorConfigurationTab(),
            "Staging Processor Configuration", new ThemeResource(IconContainer.GEARS));

    // Add listener for tab change
    mainComponentContainer.addSelectedTabChangeListener(
            new TabSheet.SelectedTabChangeListener() {

              @Override
              public void selectedTabChange(TabSheet.SelectedTabChangeEvent event) {
                if (!event.getTabSheet().getSelectedTab().isEnabled()) {
                  return;
                }
                if (event.getTabSheet().getSelectedTab().equals(userAdministrationTab)) {
                  userAdministrationTab.reload();
                } else if (event.getTabSheet().getSelectedTab().equals(groupAdministrationTab)) {
                  groupAdministrationTab.reload();
                } else if (event.getTabSheet().getSelectedTab().equals(accessPointConfigurationTab)) {
                  accessPointConfigurationTab.reload();
                  /*} else if (event.getTabSheet().getSelectedTab().equals(accessProviderConfigurationTab)) {
                   accessProviderConfigurationTab.reload();
                   */
                } else if (event.getTabSheet().getSelectedTab().equals(processorConfigurationTab)) {
                  processorConfigurationTab.reload();
                }
              }
            });

    mainLayout = new VerticalLayout(mainComponentContainer);
    mainLayout.setSpacing(true);
    mainLayout.setMargin(true);
    mainLayout.setSizeFull();
  }

  /**
   *
   * @param tab
   */
  public void updateTab(Tab tab) {
    switch (tab) {
      case ALL:
        getUserAdministrationTab().update();
        getUserAdministrationTab().update();
        getGroupAdministrationTab().update();
        getAccessPointConfigurationTab().update();
        // getAccessProviderConfigurationTab().update();
        getProcessorConfigurationTab().update();
        break;
      case USER_ADMINISTRATION:
        getUserAdministrationTab().update();
        break;
      case GROUP_ADMINISTRATION:
        getGroupAdministrationTab().update();
        break;
      case ACCESS_POINT_CONFIGURATION:
        getAccessPointConfigurationTab().update();
        break;
      /* case ACCESS_PROVIDER_CONFIGURATION:
       getAccessProviderConfigurationTab().update();
       break;*/
      case PROCESSOR_CONFIGURATION:
        getProcessorConfigurationTab().update();
        break;
      default:
        getParentApp().showError(new StringBuilder("Unknow error occurred! ")
                .append(NoteBuilder.CONTACT).toString());
        LOGGER.error(new StringBuilder("Failed to update the tab(s) of ")
                .append(this.getClass().getSimpleName()).append(".").toString(),
                new UnsupportedEnumException("Undefined enum constant!"));
    }
  }

  public AdminUIMainView getParentApp() {
    return parentApp;
  }

  /**
   * @return the mainComponentContainer
   */
  public TabSheet getMainComponentContainer() {
    if (mainComponentContainer == null) {
      buildMainComponentContainer();
    }
    return mainComponentContainer;
  }

  /**
   * @return the userAdministrationTab
   */
  public UserDataAdministrationTab getUserAdministrationTab() {
    if (userAdministrationTab == null) {
      userAdministrationTab = new UserDataAdministrationTab(parentApp);
    }
    return userAdministrationTab;
  }

  /**
   * @return the groupAdministrationTab
   */
  public UserGroupAdministrationTab getGroupAdministrationTab() {
    if (groupAdministrationTab == null) {
      groupAdministrationTab = new UserGroupAdministrationTab(parentApp);
    }
    return groupAdministrationTab;
  }

  /**
   * @return the accessProviderConfigurationTab
   */
  /*public StagingAccessProviderConfigurationTab getAccessProviderConfigurationTab() {
   if (accessProviderConfigurationTab == null) {
   accessProviderConfigurationTab
   = new StagingAccessProviderConfigurationTab(parentApp);
   }
   return accessProviderConfigurationTab;
   }*/
  
  /**
   * @return the accessPointConfigurationTab
   */
  public StagingAccessPointConfigurationTab getAccessPointConfigurationTab() {
    if (accessPointConfigurationTab == null) {
      accessPointConfigurationTab
              = new StagingAccessPointConfigurationTab(parentApp);
    }
    return accessPointConfigurationTab;
  }

  /**
   * @return the processorConfigurationTab
   */
  public StagingProcessorConfigurationTab getProcessorConfigurationTab() {
    if (processorConfigurationTab == null) {
      processorConfigurationTab = new StagingProcessorConfigurationTab(parentApp);
    }
    return processorConfigurationTab;
  }
}
