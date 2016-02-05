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

import com.vaadin.data.Property;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.ui.admin.AdminUIMainView.VIEW;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.Constants;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class LoginInformationBar extends CustomComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginInformationBar.class);
    private Label loggedInUserLabel;
    private GridLayout mainLayout;
    private Label activeGroupLabel;
    private ComboBox groupSelection;
    private Label loggedInAsLabel;
    private final AdminUIMainView parent;
    //private Image currentIcon;
    private Image home;
    private Image profile;
    private Image simon;
    private Image settings;
    private Image exit;

    public LoginInformationBar(AdminUIMainView pParent) {
        parent = pParent;
        buildMainLayout();
        setCompositionRoot(mainLayout);
    }

    /**
     * @TODO (101214)This method is not doing anything anymore. It is used only
     * once in AdminUIMainView.updateView. Using line is now also commented
     * @param pView
     */
    protected void setView(VIEW pView) {
        //Image newIcon;
        switch (pView) {
            case INFORMATION:
                // newIcon = new Image(null, new ThemeResource("img/48x48/information2.png"));
                break;
            case PROFILE:
                //  newIcon = new Image(null, new ThemeResource("img/48x48/preferences.png"));
                break;
            case SETTINGS:
                // newIcon = new Image(null, new ThemeResource("img/48x48/gears_preferences.png"));
                break;
            default:
                //  newIcon = new Image(null, new ThemeResource("img/48x48/logo_default.png"));
                break;
        }
        //  mainLayout.replaceComponent(currentIcon, newIcon);
        // currentIcon = newIcon;
    }

    private void buildMainLayout() {
        loggedInAsLabel = new Label("<b>Logged in as:</b>", ContentMode.HTML);
        loggedInAsLabel.setWidth("80px");
        loggedInUserLabel = new Label();
        loggedInUserLabel.setWidth("200px");
        /* @TODO (101214)
         This spacer is never added to any layout
         -----------------------------------------------------------------------
         Label spacer = new Label("&nbsp;", ContentMode.HTML);
           spacer.setWidth("70px");
         */
        activeGroupLabel = new Label("<b>Active Group:</b>", ContentMode.HTML);
        activeGroupLabel.setWidth("80px");
        groupSelection = new ComboBox();
        groupSelection.setWidth("200px");
        groupSelection.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                UserData loggedInUser = parent.getLoggedInUser();
                // Check if loggedInUser equals NO_USER => loggedInUser logged off
                if (loggedInUser.equals(UserData.NO_USER)) {
                    LOGGER.debug("loggedInUser == UserData.NO_USER => loggedInUser logged off!");
                    return;
                }
                // Get selected groupId
                String selection = (String) event.getProperty().getValue();
                if (selection == null) {
                    //no group selected (yet), set USERS as default
                    selection = Constants.USERS_GROUP_ID;
                }
                GroupId groupId = new GroupId(selection);
                loggedInUser.setCurrentGroup(groupId);
                loggedInUser.setCurrentRole(Role.NO_ACCESS);
                try {
                    IAuthorizationContext authCtx = AuthorizationContext.factorySystemContext();
                    UserId userId = new UserId(loggedInUser.getDistinguishedName());
                    Role currentRole = (Role) GroupServiceLocal.getSingleton().getMaximumRole(
                            groupId, userId, authCtx);
                    loggedInUser.setCurrentRole(currentRole);
                } catch (EntityNotFoundException ex) {
                    String untracebleObject = "user '" + loggedInUser.getDistinguishedName()
                            + "' or group '" + groupId.getStringRepresentation() + "' ";
                    LOGGER.error("Failed to determine the maximum role of the loggedInUser. Cause: "
                            + MsgBuilder.notFound(untracebleObject), ex);
                    UIComponentTools.showError("ERROR", "Authorization level indeterminable. Cause: "
                            + NoteBuilder.notFound(untracebleObject), -1);
                } catch (UnauthorizedAccessAttemptException ex) {
                    String getRequest = "maximum role of user '" + loggedInUser.getDistinguishedName()
                            + "' within group '" + groupId.getStringRepresentation() + "' ";
                    LOGGER.warn("Failed to determine the maximum role of the loggedInUser. Cause: "
                            + MsgBuilder.unauthorizedGetRequest(getRequest), ex);
                    UIComponentTools.showWarning("WARNING", "Authorization level indeterminable. Cause: "
                            + NoteBuilder.unauthorizedGetRequest(getRequest), -1);
                }
                // Update view with respect to the selected groupId
                if (loggedInUser.getCurrentRole().lessThan(Role.MANAGER)) {
                    parent.updateView(VIEW.INFORMATION);
                } else {
                    parent.updateView(parent.getCurrentView());
                }
                updateMenu(loggedInUser.getCurrentRole());
            }
        });
        /* @TODO (101214) 
         Moved to AdminUIMainView, as it is not really a component
         of LoginInformationBar. By this, the LoginInformationBar can be set 
         (in-)visible mode without effecting the header.
         -----------------------------------------------------------------------
         final Label title = new Label("<h1>KIT Data Manager Administration</h1>", ContentMode.HTML);
         title.setDescription("Click to return to main page.");
         title.setSizeFull();
         title.addStyleName("title");
         */
        Label spacer1 = new Label("");
        home = buildMenuItem("home", new ThemeResource("img/70x48/logo_default.png"));
        profile = buildMenuItem("profile", new ThemeResource("img/70x48/preferences.png"));
        simon = buildMenuItem("simon", new ThemeResource("img/70x48/simon.png"));
        settings = buildMenuItem("settings", new ThemeResource("img/70x48/gears_preferences.png"));
        exit = buildMenuItem("exit", new ThemeResource("img/70x48/exit.png"));

        final AbsoluteLayout helps = new AbsoluteLayout();
        helps.setHeight("48px");
        helps.setWidth("100%");
        addHelpItem("home", "Return to the main page.", helps);
        addHelpItem("profile", "Open your profile, e.g. to change your password.", helps);
        addHelpItem("simon", "Open the <b>SI</b>mple<b>MON</b>itoring Tool, e.g. to check the availability of single services.", helps);
        addHelpItem("settings", "Open the system settings. (Only available for Group Managers and Administrators)", helps);
        addHelpItem("exit", "Logout.", helps);

        HorizontalLayout navigation = new HorizontalLayout(home, profile, simon, settings, exit, helps, spacer1);
        navigation.setExpandRatio(helps, .9f);
        navigation.setExpandRatio(spacer1, .1f);
        navigation.setSizeFull();
        navigation.addStyleName("mynavigationmargin");

        navigation.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {

            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (home.equals(event.getClickedComponent())) {
                    parent.updateView(VIEW.INFORMATION);
                } else if (profile.equals(event.getClickedComponent())) {
                    parent.updateView(VIEW.PROFILE);
                } else if (simon.equals(event.getClickedComponent())) {
                    parent.updateView(VIEW.SIMON);
                } else if (settings.equals(event.getClickedComponent())) {
                    parent.updateView(VIEW.SETTINGS);
                } else if (exit.equals(event.getClickedComponent())) {
                    parent.logout();
                    /* @TODO 101214
                     Not needed anymore(?)
                     -----------------------------------------------------------
                     //dirty hack to hide exit help as we never really leave the exit button
                     Iterator<Component> components = helps.iterator();
                     while (components.hasNext()) {
                     Component current = components.next();
                     if ((current instanceof Label) && ("exit_help".equals(((Label) current).getId()))) {
                     current.removeStyleName("visible");
                     current.addStyleName("invisible");
                     break;
                     }
                     }
                     */
                }
            }
        });

        mainLayout = new UIUtils7.GridLayoutBuilder(5, 4).
                /* @TODO (101214)
                 'title' moved to AdminUIMainView; Component replaced with horizontal separator.
                 ----------------------------------------------------------------
                 addComponent(title, Alignment.TOP_CENTER, 0, 0, 5, 1).
                 */
                addComponent(new Label("<hr/>", ContentMode.HTML), Alignment.TOP_CENTER, 0, 0, 5, 1).
                addComponent(navigation, Alignment.MIDDLE_LEFT, 0, 1, 3, 2).addComponent(loggedInAsLabel, Alignment.MIDDLE_RIGHT, 3, 1, 1, 1).addComponent(loggedInUserLabel, Alignment.MIDDLE_LEFT, 4, 1, 1, 1).
                addComponent(activeGroupLabel, Alignment.MIDDLE_RIGHT, 3, 2, 1, 1).addComponent(groupSelection, Alignment.MIDDLE_LEFT, 4, 2, 1, 1).
                addComponent(new Label("<hr/>", ContentMode.HTML), 0, 3, 5, 1).getLayout();
        mainLayout.setColumnExpandRatio(0, 1.0f);
        mainLayout.setColumnExpandRatio(1, .01f);
        mainLayout.setColumnExpandRatio(2, .01f);
        mainLayout.setColumnExpandRatio(3, .01f);
        mainLayout.setColumnExpandRatio(4, .01f);

        mainLayout.setSpacing(true);
        mainLayout.setMargin(new MarginInfo(false, true, false, true));
        mainLayout.setSizeFull();
        setLoggedInUser(UserData.NO_USER);
        /* @TODO (101214) 
         Moved to AdminUIMainLayout
         -----------------------------------------------------------------------
         mainLayout.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
         @Override
         public void layoutClick(LayoutEvents.LayoutClickEvent event) {
         if (title.equals(event.getClickedComponent())) {
         parent.refreshMainLayout();
         }
         }
         });
         */
    }

    /**
     * Build a menu item and return it.
     *
     * @param pId The item id.
     * @param pImage The item image.
     *
     * @return The item.
     */
    private Image buildMenuItem(String pId, ThemeResource pImage) {
        Image item = new Image(null, pImage);
        item.setId(pId);
        item.addStyleName("shadow");
        item.addStyleName("menu");
        item.addStyleName("myclickablecomponent");
        item.setWidth("70px");
        new HelpExtension().extend(item);
        return item;
    }

    /**
     * Add a help entry to the provided layout.
     *
     * @param pId The id of the item to which the help belongs.
     * @param pHelpText The help text.
     * @param pTargetLayout The layout where the help will be added to.
     */
    private void addHelpItem(String pId, String pHelpText, AbsoluteLayout pTargetLayout) {
        Label item = new Label(pHelpText, ContentMode.HTML);
        item.setId(pId + "_help");
        item.setHeight("48px");
        item.addStyleName("invisible");
        item.addStyleName("help-left");
        /* @TODO (18122014) Not needed anymore. 
         pTargetLayout.addComponent(item, "top:0px;left:30px;");
         */
        pTargetLayout.addComponent(item);
    }

    public void setLoggedInUser(UserData pUser) {
        Role role;
        boolean loginSectionVisible = false;
        if (pUser == null || UserData.NO_USER.equals(pUser)) {
            groupSelection.removeAllItems();
            role = Role.NO_ACCESS;
        } else {
            loginSectionVisible = true;
            loggedInAsLabel.setValue("<b>Logged in as:</b>");
            loggedInUserLabel.setValue(pUser.getFullname());
            try {
                List<GroupId> userGroups = GroupServiceLocal.getSingleton().membershipsOf(new UserId(pUser.getDistinguishedName()), 0, Integer.MAX_VALUE, AuthorizationContext.factorySystemContext());
                groupSelection.removeAllItems();
                for (GroupId groupId : userGroups) {
                    groupSelection.addItem(groupId.getStringRepresentation());
                }
                groupSelection.setNullSelectionAllowed(false);
                groupSelection.select(Constants.USERS_GROUP_ID);
            } catch (UnauthorizedAccessAttemptException ex) {
                new Notification("Error",
                        "Failed to obtain memberships of logged in user. Please contact an administrator.", Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
                LOGGER.error("Failed to obtain membership of logged in user.", ex);
            }
            role = parent.getLoggedInUser().getCurrentRole();
        }
        //set login section (in-)visible
        loggedInAsLabel.setVisible(loginSectionVisible);
        loggedInUserLabel.setVisible(loginSectionVisible);
        groupSelection.setVisible(loginSectionVisible);
        activeGroupLabel.setVisible(loginSectionVisible);

        updateMenu(role);
    }

    /**
     * Update the main menu depending on the role of the logged in user.
     * Depending of the role e.g. the settings menu item will be visible or not.
     *
     * @param pRole The current role.
     */
    private void updateMenu(Role pRole) {
        Role role = (pRole != null) ? pRole : Role.NO_ACCESS;
        boolean menuVisible = role.atLeast(Role.GUEST);
        boolean settingsVisible = menuVisible && role.atLeast(Role.MANAGER);
        home.setVisible(menuVisible);
        profile.setVisible(menuVisible);
        simon.setVisible(menuVisible);
        settings.setVisible(settingsVisible);
        exit.setVisible(menuVisible);
    }

    /**
     * Get the currently selected group value.
     *
     * @return The groupId string.
     */
    public String getSelectedGroup() {
        String selection = (String) groupSelection.getValue();
        return (selection != null) ? selection : Constants.USERS_GROUP_ID;
    }
}
