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

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.LayoutEvents;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.DefaultErrorHandler;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.util.AuthorizationUtil;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.admin.ServiceAccessToken;
import edu.kit.dama.mdm.admin.util.ServiceAccessUtil;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.ui.simon.panel.SimonMainPanel;
import edu.kit.dama.ui.simon.util.SimonConfigurator;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.DataManagerSettings;
import edu.kit.dama.util.StackTraceUtil;
import java.io.File;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Theme("mytheme")
@SuppressWarnings("serial")
public class AdminUIMainView extends UI implements IRegistrationCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(AdminUIMainView.class);

  public enum VIEW {

    LOGIN, SIMON, INFORMATION, PROFILE, SETTINGS
  }

  public static final String MAIN_LOGIN_TOKEN_KEY = "mainLogin";
  private UserData loggedInUser = UserData.NO_USER;
  private GridLayout loginForm;
  /* @TODO (101214)
   Do we still need it? Was already commented before
   ---------------------------------------------------------------------------
   // private MainControlPanel mainControlPanel = null;
   */
  private Label title = null;
  private SimonMainPanel simonPanel = null;
  private ProfileView profileView = null;
  private InformationView informationView = null;
  private LoginInformationBar loginInformationBar = null;
  private DataManagerSettingsPanel datamanagerSettings = null;
  private VerticalLayout mainLayout;
  private TextField email;
  private PasswordField password;
  private Component viewComponent;
  private IMetaDataManager mdm;
  private VIEW currentView;

  @Override
  public void fireRegistrationSucceededEvent(Component pParent, UserData pUser) {
    mainLayout.replaceComponent(pParent, loginForm);
    email.setValue(pUser.getEmail());
  }

  @Override
  public void fireRegistrationCanceledEvent(Component pParent) {
    mainLayout.replaceComponent(pParent, loginForm);
  }

  @WebServlet(value = "/*", asyncSupported = true)
  @VaadinServletConfiguration(productionMode = false, ui = AdminUIMainView.class, widgetset = "edu.kit.dama.ui.admin.AppWidgetSet")
  public static class Servlet extends VaadinServlet {
  }

  @Override
  protected void init(VaadinRequest request) {
    mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());

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
    /* @TODO (101214)
     Do we still need it? Was already commented.
     -----------------------------------------------------------------------
     //    VaadinSession.getCurrent().addRequestHandler(
     //            new RequestHandler() {
     //              @Override
     //              public boolean handleRequest(VaadinSession session,
     //                      VaadinRequest request,
     //                      VaadinResponse response)
     //              throws IOException {
     //                Enumeration<String> att = request.getAttributeNames();
     //                while(att.hasMoreElements()){
     //                  System.out.println(att.nextElement());
     //                }
     //                if ("/rhexample".equals(request.getPathInfo())) {
     //                  response.setContentType("text/plain");
     //                  response.getWriter().append(
     //                          "Here's some dynamically generated content.\n"
     //                          + "Time: " + (new Date()).toString());
     //                  return true; // We wrote a response
     //                } else {
     //                  return false; // No response was written
     //                }
     //              }
     //            });
     */
    UI.getCurrent().setErrorHandler(new DefaultErrorHandler() {
      @Override
      public void error(com.vaadin.server.ErrorEvent event) {
        String cause = "<h3>An unexpected error has occured. Please reload the page.<br/>"
                + "If the error persists, please contact an administrator.</h3>";

        Label errorLabel = new Label(cause, ContentMode.HTML);
        errorLabel.setDescription(StackTraceUtil.getCustomStackTrace(event.getThrowable(), false));
        LOGGER.error("An unhandled exception has occured!", event.getThrowable());
        // Display the error message in a custom fashion
        mainLayout.addComponent(errorLabel, 0);
        // Do the default error handling (optional)
        doDefault(event);
      }
    });

    mainLayout = new VerticalLayout();
    mainLayout.setMargin(false);
    setupLoginForm();
    title = new Label("<h1>KIT Data Manager Administration</h1>", ContentMode.HTML);
    title.setDescription("Click to return to main page.");
    title.setWidth("50%");
    title.addStyleName("title");
    title.addStyleName("myclickablecomponent");
    loginInformationBar = new LoginInformationBar(this);
    loginInformationBar.setLoggedInUser(loggedInUser);
    mainLayout.setSizeFull();
    setContent(mainLayout);
    setSizeFull();
    refreshMainLayout();
  }

  public IMetaDataManager getMetaDataManager() {
    return mdm;
  }

  /**
   * Setup the login form including its logic.
   */
  private void setupLoginForm() {
    email = UIUtils7.factoryTextField("Email", "Please enter your email.", "300px", true, -1, 255);
    password = UIUtils7.factoryPasswordField("Password", "300px", true, -1, 255);
    Button login = new Button("Login");
    login.setClickShortcut(KeyCode.ENTER);
    login.setWidth("100px");
    Button register = new Button("Register");
    register.setWidth("100px");
    loginForm = new UIUtils7.GridLayoutBuilder(2, 3).addComponent(email, 0, 0, 2, 1).addComponent(password, 0, 1, 2, 1).addComponent(login, 0, 2, 1, 1).addComponent(register, 1, 2, 1, 1).getLayout();
    loginForm.setComponentAlignment(login, Alignment.MIDDLE_LEFT);
    loginForm.setComponentAlignment(register, Alignment.MIDDLE_RIGHT);

    //login listener
    login.addClickListener(new Button.ClickListener() {

      @Override
      public void buttonClick(Button.ClickEvent event) {
        if (!UIUtils7.validate(loginForm)) {
          new Notification("Login Failed",
                  "Please correct the error(s) above.", Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
          return;
        }
        String userMail = email.getValue();
        String userPassword = password.getValue();
        IMetaDataManager manager = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        manager.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
          LOGGER.debug("Getting access token for user {}", userMail);
          ServiceAccessToken token = ServiceAccessUtil.getAccessToken(manager, userMail, MAIN_LOGIN_TOKEN_KEY);
          if (token == null) {
            new Notification("Login Failed",
                    "No login information found for email " + userMail + ".", Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
            return;
          } else {
            LOGGER.debug("Access token sucessfully obtained. Checking password.");
          }

          if (!userPassword.equals(token.getSecret())) {
            new Notification("Login Failed",
                    "Wrong password for email " + userMail + ".", Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
          } else {
            LOGGER.debug("Password is correct. Getting user information.");
            //login successful
            UserData template = new UserData();
            template.setDistinguishedName(token.getUserId());
            List<UserData> result = manager.find(template, template);
            if (result.isEmpty() || result.size() > 1) {
              throw new Exception("Invalid number of user entries (" + result.size() + ") found for userId " + token.getUserId() + ". Please contact a system administrator.");
            }
            LOGGER.debug("User information obtained. Setting logged in user and updating main layout.");
            //done
            loggedInUser = result.get(0);
            loginInformationBar.setLoggedInUser(loggedInUser);
            refreshMainLayout();
          }
        } catch (Exception ex) {
          new Notification("Login Failed",
                  "Failed to access login database. Please contact an administrator.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
          LOGGER.error("Failed to access login database.", ex);
        } finally {
          manager.close();
        }
      }
    });

    //register listener
    register.addClickListener(new Button.ClickListener() {

      @Override
      public void buttonClick(Button.ClickEvent event) {
        RegistrationFormView registerForm = new RegistrationFormView(AdminUIMainView.this);
        mainLayout.replaceComponent(loginForm, registerForm);
      }
    });
    loginForm.setSpacing(true);
    loginForm.setMargin(true);
  }

  /**
   * Update the view depending on the user selection.
   *
   * @param pView The new view.
   */
  public void updateView(VIEW pView) {
    currentView = pView;
    mainLayout.removeAllComponents();
    /* @TODO (101214)
     Do we really need to add loginInformationBar here if it is added after 
     the switch-command (again)?
     ------------------------------------------------------------------------
     mainLayout.addComponent(loginInformationBar);
     */
    switch (pView) {
      case SIMON:
        loginInformationBar.setVisible(true);
        if (simonPanel == null) {
          simonPanel = new SimonMainPanel();
        }
        viewComponent = simonPanel;
        break;
      case INFORMATION:
        loginInformationBar.setVisible(true);
        if (informationView == null) {
          informationView = new InformationView(this);
        }
        viewComponent = informationView;
        break;
      case LOGIN:
        loginInformationBar.setVisible(false);
        viewComponent = loginForm;
        break;
      case PROFILE:
        loginInformationBar.setVisible(true);
        if (profileView == null) {
          profileView = new ProfileView(this);
        }
        viewComponent = profileView;
        break;
      case SETTINGS:
        loginInformationBar.setVisible(true);
        if (datamanagerSettings == null) {
          datamanagerSettings = new DataManagerSettingsPanel(this);
        } else {
          datamanagerSettings.updateTab(DataManagerSettingsPanel.Tab.ALL);
        }
        viewComponent = datamanagerSettings;
        break;
    }

    mainLayout.addComponent(title);
    mainLayout.addComponent(loginInformationBar);
    /* @TODO (101214)
     setView is doing nothing. 
     -----------------------------------------------------------------------
     loginInformationBar.setView(pView);
     */
    mainLayout.addComponent(viewComponent);
    mainLayout.setExpandRatio(title, .09f);
    mainLayout.setExpandRatio(loginInformationBar, .11f);
    mainLayout.setExpandRatio(viewComponent, .8f);
    mainLayout.setComponentAlignment(title, Alignment.MIDDLE_CENTER);
    mainLayout.setComponentAlignment(viewComponent, Alignment.MIDDLE_CENTER);
    mainLayout.setComponentAlignment(loginInformationBar, Alignment.TOP_RIGHT);

    mainLayout.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {

      @Override
      public void layoutClick(LayoutEvents.LayoutClickEvent event) {
        if (title.equals(event.getClickedComponent())) {
          refreshMainLayout();
        }
      }
    });
  }

  /**
   * Show an error dialog.
   *
   * @param message The message.
   */
  public void showError(String message) {
    new Notification("Error", message, Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
  }

  /**
   * Show a warning dialog.
   *
   * @param message The message.
   */
  public void showWarning(String message) {
    new Notification("Warning", message, Notification.Type.WARNING_MESSAGE).show(Page.getCurrent());
  }

  @Override
  public void showNotification(String message) {
    new Notification("Information", message, Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
  }

  /**
   * Refresh the main layout depending on the logged in user. If nobody is
   * logged in, the login form will be shown. Otherwise, the main control panel
   * gets visible.
   */
  protected void refreshMainLayout() {
    LOGGER.debug("Refreshing main layout.");
    if (UserData.NO_USER.equals(loggedInUser)) {
      LOGGER.debug("Updating login view.");
      updateView(VIEW.LOGIN);
    } else {
      //logged in
      LOGGER.debug("Updating information view.");
      updateView(VIEW.INFORMATION);
    }
  }

  /**
   * Get the currently logged in user.
   *
   * @return The logged in user.
   */
  public UserData getLoggedInUser() {
    return loggedInUser;
  }

  /**
   * Get the current authorization context defined by the logged in user and the
   * group selection in the loginInformationBar. The according role is queried
   * from the database in each call.
   *
   * @return The AuthorizationContext.
   *
   * @throws AuthorizationException If authorization fails.
   */
  public IAuthorizationContext getAuthorizationContext() throws AuthorizationException {
    return AuthorizationUtil.getAuthorizationContext(new UserId(getLoggedInUser().getDistinguishedName()), new GroupId(loginInformationBar.getSelectedGroup()));
  }

  /**
   * Get the current authorization context defined by the logged in user and the
   * provided group. This method is intended to be used if another context is
   * temporary needed to authorize a single operation that is coupled to the
   * provided group. The according role is queried from the database in each
   * call.
   *
   * @param pCurrentGroup The groupId to create the AuthorizationContext for.
   *
   * @return The AuthorizationContext.
   *
   * @throws AuthorizationException If authorization fails.
   */
  public IAuthorizationContext getAuthorizationContext(GroupId pCurrentGroup) throws AuthorizationException {
    return AuthorizationUtil.getAuthorizationContext(new UserId(getLoggedInUser().getDistinguishedName()), pCurrentGroup);
  }

  /**
   * Perform Logout and switch view to login form.
   */
  protected void logout() {
    loggedInUser = UserData.NO_USER;
    loginInformationBar.setLoggedInUser(loggedInUser);
    password.setValue("");
    //reset all panels
    informationView = null;
    profileView = null;
    datamanagerSettings = null;
    refreshMainLayout();
    new Notification("Logout",
            "You've been logged out successfully.", Notification.Type.TRAY_NOTIFICATION).show(Page.getCurrent());
  }

  /**
   * @return the currentView
   */
  public VIEW getCurrentView() {
    if (currentView == null) {
      refreshMainLayout();
    }
    return currentView;
  }
}
