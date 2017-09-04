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

import edu.kit.dama.ui.admin.wizard.FirstStartWizard;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Property;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.DefaultErrorHandler;
import static com.vaadin.server.DefaultErrorHandler.doDefault;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.ResourceServiceLocal;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.ui.admin.login.AbstractLoginComponent;
import edu.kit.dama.ui.admin.login.OrcidLoginComponent;
import edu.kit.dama.ui.admin.login.EmailPasswordLoginComponent;
import edu.kit.dama.ui.commons.util.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.ui.simon.panel.SimonMainPanel;
import edu.kit.dama.ui.simon.util.SimonConfigurator;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import edu.kit.dama.util.StackTraceUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Theme("mytheme")
@SuppressWarnings("serial")
public class AdminUIMainView extends UI {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminUIMainView.class);

    public enum VIEW {

        LOGIN, SIMON, INFORMATION, PROFILE, SETTINGS, LANDING
    }

    private VerticalLayout loginForm;
    private AbstractLoginComponent loginComponent;
    private AbstractLoginComponent[] loginComponents = null;
    private HorizontalLayout header = null;
    private Label title = null;
    private Image logo = null;
    private SimonMainPanel simonPanel = null;
    private ProfileView profileView = null;
    private InformationView informationView = null;
    private LoginInformationBar loginInformationBar = null;
    private DataManagerSettingsPanel datamanagerSettings = null;
    private LandingPageComponent landingPage;
    private VerticalLayout mainLayout;
    private Component viewComponent;
    private boolean INITIALIZING = true;

    @Override
    protected void init(VaadinRequest request) {
        LOGGER.debug("Initializing AdminUI");
        boolean firstStart = false;
        //check first start
        LOGGER.debug("Checking for first start...");
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            if (mdm.findSingleResult("SELECT COUNT(u) FROM Users u", Number.class).intValue() == 0) {
                //first start
                LOGGER.info("Recognized first start. Showing wizard.");
                firstStart = true;
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            //failed
        } finally {
            mdm.close();
        }
        LOGGER.debug("Setting up UI...");
        try {
            //setup SiMon configuration and error handler
            doBasicSetup();

            //check if a landing page is requested and create the header accordingly
            boolean isLandingPage = request.getParameter("landing") != null;
            String landingObjectId = request.getParameter("oid");
            //setup header
            setupHeader(isLandingPage, landingObjectId);

            if (firstStart) {
                LOGGER.debug("First start detected. Starting wizard.");
                //do nothing else but first start handling
                mainLayout = new VerticalLayout();
                mainLayout.setMargin(false);
                mainLayout.setSizeFull();
                mainLayout.addComponent(new FirstStartWizard());
                setContent(mainLayout);
                setSizeFull();
                return;
            }
            LOGGER.debug("No first start detected. Continuing with UI setup.");

            //Check for OAuth redirect
            String pendingAuthId = null;
            AbstractLoginComponent.AUTH_MODE type = AbstractLoginComponent.AUTH_MODE.LOGIN;
            if (VaadinSession.getCurrent().getAttribute("auth_pending") != null) {
                pendingAuthId = (String) VaadinSession.getCurrent().getAttribute("auth_pending");
            } else if (VaadinSession.getCurrent().getAttribute("registration_pending") != null) {
                pendingAuthId = (String) VaadinSession.getCurrent().getAttribute("registration_pending");
                type = AbstractLoginComponent.AUTH_MODE.REGISTRATION;
            }

            //setup login form
            setupLoginForm(type, pendingAuthId, request);
            if (pendingAuthId != null && !type.equals(AbstractLoginComponent.AUTH_MODE.REGISTRATION)) {
                //auth will redirect to start page so we'll stop here
                return;
            }

            mainLayout = new VerticalLayout();
            mainLayout.setMargin(false);
            mainLayout.setSizeFull();

            //setup login bar
            loginInformationBar = new LoginInformationBar(this);
            loginInformationBar.reload();

            setContent(mainLayout);
            setSizeFull();

            //fill the main layout, either with landing page content or the default content
            if (isLandingPage) {
                //setup landing page
                LOGGER.debug("Showing landing page.");
                setupLandingPage(landingObjectId);
            } else {
                LOGGER.debug("Updating default UI.");
                refreshMainLayout();
            }
            INITIALIZING = false;
        } catch (Throwable t) {
            LOGGER.error("Failed to initialize application. Closing session.", t);
            VaadinSession.getCurrent().close();
        }
    }

    /**
     * Setup SiMon configuration and error handler.
     */
    private void doBasicSetup() {
        //configure SiMon
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

        //set error handler in order to catch unhandled exceptions
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
    }

    /**
     * Setup header depending on whether a landing page is requested or not.
     */
    private void setupHeader(boolean landingPage, String landingObjectId) {
        if (!landingPage) {
            String repositoryName = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_REPOSITORY_NAME, "Repository");
            title = new Label("<h2>" + repositoryName + " UI</h2>Build " + readVersion(), ContentMode.HTML);
            title.setDescription("Click to return to main page.");
            title.setWidth("50%");
            title.addStyleName("title");
            title.addStyleName("myclickablecomponent");

            logo = new Image();
            String logoUrl;
            try {
                logoUrl = new URL(DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_REPOSITORY_LOGO_URL, "http://datamanager.kit.edu/dama/logo_default.png")).toString();
            } catch (MalformedURLException ex) {
                logoUrl = URI.create("http://datamanager.kit.edu/dama/logo_default.png").toString();
            }
            logo.setSource(new ExternalResource(logoUrl));

            header = new HorizontalLayout(logo, title);
            header.setComponentAlignment(logo, Alignment.TOP_CENTER);
            header.setComponentAlignment(title, Alignment.TOP_CENTER);
        } else {
            title = new Label("<h1>Landing Page for Object #" + landingObjectId + "</h1><h5>Build " + readVersion() + "</h5>", ContentMode.HTML);
            title.setWidth("50%");
            title.addStyleName("title");
            title.addStyleName("myclickablecomponent");

            logo = new Image();
            String logoUrl;
            try {
                logoUrl = new URL(DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_REPOSITORY_LOGO_URL, "http://datamanager.kit.edu/dama/logo_default.png")).toString();
            } catch (MalformedURLException ex) {
                logoUrl = URI.create("http://datamanager.kit.edu/dama/logo_default.png").toString();
            }
            logo.setSource(new ExternalResource(logoUrl));

            header = new HorizontalLayout(logo, title);
            header.setComponentAlignment(logo, Alignment.MIDDLE_CENTER);
            header.setComponentAlignment(title, Alignment.MIDDLE_CENTER);
        }
    }

    /**
     * Setup the landing page.
     *
     * @param oid The object id for which the landing page should be shown.
     */
    private void setupLandingPage(String oid) {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        DigitalObject result = null;
        Role viewRole = Role.GUEST;
        boolean objectNotFound = false;
        boolean extendedAccess = false;
        try {
            mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
            //check if object exists
            result = mdm.findSingleResult("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier=?1", new Object[]{oid}, DigitalObject.class);
            if (result == null) {
                //object does not exist
                objectNotFound = true;
            } else {
                //object does exist, check permission for current context
                try {
                    viewRole = ResourceServiceLocal.getSingleton().getGrantRole(result.getSecurableResourceId(), UIHelper.getSessionContext().getUserId(), AuthorizationContext.factorySystemContext());
                } catch (UnsupportedOperationException | EntityNotFoundException nogrants) {
                    //no grant found, check group role
                    try {
                        viewRole = (Role) ResourceServiceLocal.getSingleton().getReferenceRestriction(new ReferenceId(result.getSecurableResourceId(), UIHelper.getSessionGroupId()), AuthorizationContext.factorySystemContext());
                    } catch (EntityNotFoundException ex) {
                        viewRole = Role.NO_ACCESS;
                    }
                }
            }

            if (objectNotFound) {
                //object not found, if user logged in, show error...otherwise show login page
                if (UIHelper.getSessionUser().getDistinguishedName().equals(Constants.WORLD_USER_ID)) {
                    VaadinSession.getCurrent().setAttribute("from", UIHelper.getWebAppUrl().toString() + "?landing&oid=" + oid);
                    updateView(VIEW.LOGIN);
                    return;
                } else {
                    throw new UnauthorizedAccessAttemptException("No object found for object id " + oid);
                }
            } else {
                //object not found, if role >= GUEST, show landing page...otherwise show login page if anonymous access
                if (!viewRole.atLeast(Role.GUEST)) {
                    VaadinSession.getCurrent().setAttribute("from", UIHelper.getWebAppUrl().toString() + "?landing&oid=" + oid);
                    updateView(VIEW.LOGIN);
                    return;
                }
            }
            //http://localhost:8080/KITDM/?landing&oid=3b1243b2-df09-4a98-ad87-21b7cda74be9
        } catch (UnauthorizedAccessAttemptException ex) {
            //not found, should result in error page
            LOGGER.error("Failed to access digital object with id " + oid, ex);
            result = null;
        } finally {
            mdm.close();
        }

        if (landingPage == null) {
            landingPage = new LandingPageComponent();
        }
        landingPage.update(result, extendedAccess);
        updateView(VIEW.LANDING);
    }

    /**
     * Setup the login form including its logic.
     *
     * @param type Either REGISTRATION or LOGIN
     * @param pendingAuth The id of the pending authentication or null if no
     * authentication is pending.
     * @param request The original request used to obtain further authentication
     * information, e.g. header values.
     */
    private void setupLoginForm(AbstractLoginComponent.AUTH_MODE type, String pendingAuth, VaadinRequest request) {
        ComboBox authSelection = new ComboBox();
        authSelection.setWidth("400px");
        authSelection.setNullSelectionAllowed(false);
        authSelection.setStyleName("auth_selection");
        Label spacer = new Label("<br/>", ContentMode.HTML);
        spacer.setWidth("400px");

        String orcidClientId = DataManagerSettings.getSingleton().getStringProperty(OrcidLoginComponent.ORCID_CLIENT_ID_PROPERTY, null);
        String orcidClientSecret = DataManagerSettings.getSingleton().getStringProperty(OrcidLoginComponent.ORCID_CLIENT_SECRET_PROPERTY, null);

        /// String b2AccessClientId = DataManagerSettings.getSingleton().getStringProperty(B2AccessLoginComponent.B2ACCESS_CLIENT_ID_PROPERTY, null);
        // String b2AccessClientSecret = DataManagerSettings.getSingleton().getStringProperty(B2AccessLoginComponent.B2ACCESS_CLIENT_SECRET_PROPERTY, null);
        List<AbstractLoginComponent> components = new ArrayList<>();

        if (orcidClientId != null && !orcidClientId.equals("ORCID_CLIENT_ID") && orcidClientSecret != null && !orcidClientSecret.equals("ORCID_CLIENT_SECRET")) {
            components.add(new OrcidLoginComponent());
        }

        /*B2Access is currently not supported. 
        if (b2AccessClientId != null && b2AccessClientSecret != null) {
            components.add(new B2AccessLoginComponent());
        }*/
        components.add(new EmailPasswordLoginComponent());

        loginComponents = components.toArray(new AbstractLoginComponent[]{});

        //default login component has index 0
        loginComponent = loginComponents[0];
        for (AbstractLoginComponent component : loginComponents) {
            //add new login component
            authSelection.addItem(component.getLoginIdentifier());
            authSelection.setItemCaption(component.getLoginIdentifier(), component.getLoginLabel());

            if (pendingAuth != null && pendingAuth.equals(component.getLoginIdentifier())) {
                //login or registration process in pending, continue process
                loginComponent = component;
                try {
                    switch (type) {
                        case REGISTRATION:
                            loginComponent.doRegistration(request);
                            break;
                        default:
                            loginComponent.doLogin(request);
                            break;
                    }

                } catch (UnauthorizedAccessAttemptException ex) {
                    //failed to continue auth...cancel.
                    String message = "Failed to continue pending " + (AbstractLoginComponent.AUTH_MODE.LOGIN.equals(type) ? "login" : "registration") + " for authentication #" + pendingAuth + ".";
                    LOGGER.error(message, ex);
                    UIUtils7.showError(message);
                    VaadinSession.getCurrent().setAttribute("auth_pending", null);
                    VaadinSession.getCurrent().setAttribute("registration_pending", null);
                    loginComponent.reset();
                }
            }
        }

        authSelection.select(loginComponent.getLoginIdentifier());

        authSelection.addValueChangeListener((Property.ValueChangeEvent event) -> {
            String value = (String) event.getProperty().getValue();
            if (value != null) {
                for (AbstractLoginComponent component : loginComponents) {
                    if (value.equals(component.getLoginIdentifier())) {
                        loginForm.replaceComponent(loginComponent, component);
                        loginComponent = component;
                    }
                }
            }
        });

        loginForm = new VerticalLayout(authSelection, spacer, loginComponent);
        loginForm.setComponentAlignment(authSelection, Alignment.TOP_CENTER);
        loginForm.setComponentAlignment(spacer, Alignment.TOP_CENTER);
        loginForm.setComponentAlignment(loginComponent, Alignment.TOP_CENTER);
    }

    /**
     * Update the view depending on the user selection.
     *
     * @param pView The new view.
     */
    public void updateView(VIEW pView) {
        mainLayout.removeAllComponents();
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
                    informationView = new InformationView();
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
                    profileView = new ProfileView();
                }
                profileView.reload();
                viewComponent = profileView;
                break;
            case SETTINGS:
                loginInformationBar.setVisible(true);
                if (datamanagerSettings == null) {
                    datamanagerSettings = new DataManagerSettingsPanel();
                }
                viewComponent = datamanagerSettings;
                break;
            case LANDING:
                loginInformationBar.setVisible(false);
                if (landingPage == null) {
                    landingPage = new LandingPageComponent();
                }
                viewComponent = landingPage;
                break;
        }

        mainLayout.addComponent(header);
        mainLayout.addComponent(loginInformationBar);
        mainLayout.addComponent(viewComponent);
        mainLayout.setExpandRatio(header, .09f);
        mainLayout.setExpandRatio(loginInformationBar, .11f);
        mainLayout.setExpandRatio(viewComponent, .8f);
        mainLayout.setComponentAlignment(header, Alignment.MIDDLE_CENTER);
        mainLayout.setComponentAlignment(viewComponent, Alignment.MIDDLE_CENTER);
        mainLayout.setComponentAlignment(loginInformationBar, Alignment.TOP_RIGHT);

        mainLayout.addLayoutClickListener((LayoutEvents.LayoutClickEvent event) -> {
            if (header.equals(event.getClickedComponent()) || title.equals(event.getClickedComponent()) || logo.equals(event.getClickedComponent())) {
                refreshMainLayout();
            }
        });
    }

    /**
     * Refresh the main layout depending on the logged in user. If nobody is
     * logged in, the login form will be shown. Otherwise, the main control
     * panel gets visible.
     */
    protected void refreshMainLayout() {
        LOGGER.debug("Refreshing main layout.");
        if (UserData.WORLD_USER.equals(UIHelper.getSessionUser())) {
            LOGGER.debug("Updating login view.");
            updateView(VIEW.LOGIN);
        } else {
            //logged in
            LOGGER.debug("Updating information view.");
            updateView(VIEW.INFORMATION);
        }
    }

    /**
     * Selected group has changed. Refresh the main layout.
     */
    protected void sessionGroupChanged() {
        if (!INITIALIZING) {
            refreshMainLayout();
        }
    }

    /**
     * Perform Logout and switch view to login form.
     */
    protected void logout() {
        UIHelper.logout(UIHelper.getWebAppUrl().toString());
    }

    /**
     * Read the current version available in the file 'version.txt'.
     */
    private String readVersion() {
        InputStream versionFile = null;
        String version = "UNKNOWN";
        try {
            versionFile = InformationView.class.getClassLoader().getResourceAsStream("version.txt");
            byte[] versionData = new byte[1024];
            int size = versionFile.read(versionData);
            version = new String(versionData, 0, size);
        } catch (IOException ex) {
            //failed to determine version number
        } finally {
            if (versionFile != null) {
                try {
                    versionFile.close();
                } catch (IOException ex) {
                }
            }
        }
        return version;
    }

}
