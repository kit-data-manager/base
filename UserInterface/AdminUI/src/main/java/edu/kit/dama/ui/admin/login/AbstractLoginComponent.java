/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.ui.admin.login;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.ui.admin.exception.RegistrationAbortedException;
import edu.kit.dama.ui.admin.exception.UserRegistrationException;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.admin.utils.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.Constants;

/**
 *
 * @author jejkal
 */
public abstract class AbstractLoginComponent extends CustomComponent implements Button.ClickListener {

    public enum AUTH_MODE {

        LOGIN, REGISTRATION;
    }
    private GridLayout mainLayout = null;
    private Button loginButton;
    private Button registerButton;
    private AUTH_MODE currentMode = AUTH_MODE.LOGIN;
    private final RegistrationFormView registrationFormView = new RegistrationFormView();

    /**
     * Default constructor.
     */
    public AbstractLoginComponent() {
        initializeLayout();
    }

    /**
     * Reset the component, e.g. reset login mode, registration form and all UI
     * elements.
     */
    public abstract void reset();

    /**
     * Return the login form to collect user information. This form might be
     * empty for external authentication.
     *
     * @return The login form.
     */
    public abstract AbstractLayout getLoginForm();

    /**
     * Do the actual login using the information provided by the login form. At
     * the end, a registered user should be returned if the login was
     * successful. If the login has failed, an exception is thrown.
     *
     * @param request Optional request if login consists of multiple steps.
     *
     * @throws UnauthorizedAccessAttemptException If the login fails.
     */
    public abstract void doLogin(VaadinRequest request) throws UnauthorizedAccessAttemptException;

    /**
     * Initiate a user registration process, e.g. collecting basic information
     * from an external source. This method is intended to be used to initialize
     * the registration process. It calls
     * {@link #doRegistration(com.vaadin.server.VaadinRequest)} with a 'null'
     * request. A local login provider should immediately switch to the
     * registration form providing only e.g. a generated unique identifier.
     *
     * A remote login provider may redirect the user to a remote login page in
     * order to collect attributes. To allow the AdminUI to determine an ongoing
     * registration process, the login provider must add an attribute
     * 'registration_pending' with the value of {@link #getLoginIdentifier() }
     * to the current session that can be obtained via
     * VaadinSession.getCurrent().
     *
     * @throws UnauthorizedAccessAttemptException If pre-registration fails,
     * e.g. of a remote identity provider cannot be accessed.
     */
    public void doRegistration() throws UnauthorizedAccessAttemptException {
        doRegistration(null);
    }

    /**
     * Continue a pending the registration using the provided request. This
     * method is called for an ongoing registration process as soon as the
     * external identity provider redirects back to the AdminUI. If a session
     * attribute 'registration_pending' exists, doRegistration is called at the
     * according login provider. The login provider may now issue additional
     * calls, e.g. using a provided login token, to obtain user information that
     * can be used for registration.
     *
     * At the end of this process,
     * {@link #setup(edu.kit.dama.ui.admin.login.AbstractLoginComponent.AUTH_MODE, edu.kit.dama.mdm.base.UserData)}
     * should be called to switch to REGISTRATION mode providing a UserData
     * entity that contains all collected information. Missing information are
     * then obtained from the user in a manual step.
     *
     * @param request Current request that can be used to obtain user
     * information that can be provided for registration.
     *
     * @throws UnauthorizedAccessAttemptException Should be only thrown if there
     * is a fatal error while initializing the registration process, e.g. if
     * mandatory attributes like the unique user identifier cannot be obtained
     * from an external identity provider.
     */
    public abstract void doRegistration(VaadinRequest request) throws UnauthorizedAccessAttemptException;

    /**
     * Do a (optional) post registration, e.g. creating additional credentials
     * or notifying custom services. This method is called after a user has been
     * successfully registered, thus it should not fail nor invalidate the
     * registration process.
     *
     * @param registeredUser The just registered user.
     */
    public abstract void doPostRegistration(UserData registeredUser);

    /**
     * Get the unique identifier of this login mechanism.
     *
     * @return The unique identifier.
     */
    public abstract String getLoginIdentifier();

    /**
     * Get the human readable name of this login mechanism.
     *
     * @return The human readable name.
     */
    public abstract String getLoginLabel();

    @Override
    public final void buttonClick(Button.ClickEvent event) {
        if (loginButton.equals(event.getButton()) && AUTH_MODE.LOGIN.equals(currentMode)) {
            //normal login
            try {
                doLogin(null);
            } catch (UnauthorizedAccessAttemptException ex) {
                //failed
                UIComponentTools.showError("Login Failed", ex.getMessage(), -1);
            }
        } else if (loginButton.equals(event.getButton()) && AUTH_MODE.REGISTRATION.equals(currentMode)) {
            //do actual user registration
            try {
                UserData registered = registrationFormView.register();
                doPostRegistration(registered);
                //do actual login
                UIHelper.login(new UserId(registered.getDistinguishedName()), new GroupId(Constants.USERS_GROUP_ID));
            } catch (UserRegistrationException ex) {
                //validation failed....may try again
                UIComponentTools.showWarning("Validation Failed", ex.getMessage(), -1);
                return;
            } catch (RegistrationAbortedException ex) {
                //registration failed...fatal error
                UIComponentTools.showError("Error", "Registration aborted. " + ex.getMessage() + " Please contact an administrator.", -1);
            }
            //switch back to login mode
            setup(AUTH_MODE.LOGIN, null);
        } else if (registerButton.equals(event.getButton()) && AUTH_MODE.LOGIN.equals(currentMode)) {
            //switch to registration mode and reset the registration form before
            registrationFormView.loadFromUserData(null);
            try {
                //do pre-registration, e.g. redirect to external registration authority
                doRegistration(null);
            } catch (UnauthorizedAccessAttemptException ex) {
                //failed, go back to login
                setup(AUTH_MODE.LOGIN, null);
                UIComponentTools.showError("Registration Failed", ex.getMessage(), -1);
            }
            //collect information and update UI
        } else if (registerButton.equals(event.getButton()) && AUTH_MODE.REGISTRATION.equals(currentMode)) {
            //cancel registration
            setup(AUTH_MODE.LOGIN, null);
        }
    }

    /**
     * Setup this form either for login or registration mode. In registration
     * mode a UserData entity might be provided containing preselected values,
     * e.g. obtained from an external identity provider.
     *
     * @param pMode The mode to setup, either LOGIN or REGISTRATION.
     * @param preselection A UserData entity containing values that will be
     * filled in the registration form.
     */
    public final void setup(AUTH_MODE pMode, UserData preselection) {
        switch (pMode) {
            case LOGIN:
                if (AUTH_MODE.REGISTRATION.equals(currentMode)) {
                    mainLayout.replaceComponent(registrationFormView, getLoginForm());
                    loginButton.setCaption("Login");
                    registerButton.setCaption("Register");
                    currentMode = AUTH_MODE.LOGIN;
                }
                break;
            case REGISTRATION:
                if (AUTH_MODE.LOGIN.equals(currentMode)) {
                    mainLayout.replaceComponent(getLoginForm(), registrationFormView);
                    registrationFormView.loadFromUserData(preselection);
                    loginButton.setCaption("Register");
                    registerButton.setCaption("Cancel");
                    currentMode = AUTH_MODE.REGISTRATION;
                }
                break;
        }
    }

    /**
     * Basic initialization of components.
     */
    private void initializeLayout() {
        registrationFormView.setWidth("400px");
        loginButton = new Button("Login");
        loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        loginButton.setWidth("100px");
        loginButton.addClickListener(this);

        registerButton = new Button("Register");
        registerButton.setWidth("100px");
        registerButton.addClickListener(this);
        mainLayout = new UIUtils7.GridLayoutBuilder(2, 2).addComponent(getLoginForm(), 0, 0, 2, 1).addComponent(registerButton, 0, 1, 1, 1).addComponent(loginButton, 1, 1, 1, 1).getLayout();

        mainLayout.setComponentAlignment(getLoginForm(), Alignment.TOP_CENTER);
        mainLayout.setComponentAlignment(loginButton, Alignment.BOTTOM_RIGHT);
        mainLayout.setSpacing(true);
        mainLayout.setMargin(false);
        mainLayout.setComponentAlignment(registerButton, Alignment.BOTTOM_LEFT);
        VerticalLayout vLayout = new VerticalLayout(mainLayout);
        vLayout.setImmediate(true);
        vLayout.setComponentAlignment(mainLayout, Alignment.MIDDLE_CENTER);
        setCompositionRoot(vLayout);
    }
}
