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
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.ui.admin.AdminUIMainView.VIEW;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.ui.admin.utils.UIComponentTools;
import edu.kit.dama.ui.admin.utils.UIHelper;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.Constants;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class LoginInformationBar extends CustomComponent implements Property.ValueChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginInformationBar.class);
    private Label loggedInUserLabel;
    private GridLayout mainLayout;
    private Label activeGroupLabel;
    private Label activeRoleLabel;
    private Label roleLabel;
    private ComboBox groupSelection;
    private Label loggedInAsLabel;
    private final AdminUIMainView parent;
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

    private void buildMainLayout() {
        loggedInAsLabel = new Label("Login Username");
        loggedInAsLabel.addStyleName("myboldcaption");
        loggedInUserLabel = new Label();
        activeGroupLabel = new Label("Active Group");
        activeGroupLabel.addStyleName("myboldcaption");
        groupSelection = new ComboBox();
        groupSelection.addValueChangeListener(this);
        activeRoleLabel = new Label("Current Role");
        activeRoleLabel.addStyleName("myboldcaption");
        roleLabel = new Label();

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

        navigation.addLayoutClickListener((LayoutEvents.LayoutClickEvent event) -> {
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
            }
        });

        GridLayout loginInformationLayout = new UIUtils7.GridLayoutBuilder(3, 2).
                addComponent(loggedInAsLabel, Alignment.MIDDLE_CENTER, 0, 0, 1, 1).addComponent(activeGroupLabel, Alignment.MIDDLE_CENTER, 1, 0, 1, 1).addComponent(activeRoleLabel, Alignment.MIDDLE_CENTER, 2, 0, 1, 1).
                addComponent(loggedInUserLabel, Alignment.MIDDLE_CENTER, 0, 1, 1, 1).addComponent(groupSelection, Alignment.MIDDLE_CENTER, 1, 1, 1, 1).addComponent(roleLabel, Alignment.MIDDLE_CENTER, 2, 1, 1, 1).getLayout();

        loginInformationLayout.setSpacing(true);

        mainLayout = new UIUtils7.GridLayoutBuilder(5, 2).
                addComponent(navigation, Alignment.MIDDLE_LEFT, 0, 0, 3, 2).
                addComponent(loginInformationLayout, Alignment.MIDDLE_RIGHT, 3, 0, 2, 2).
                getLayout();
        mainLayout.setColumnExpandRatio(0, 1.0f);
        mainLayout.setColumnExpandRatio(1, .01f);
        mainLayout.setColumnExpandRatio(2, .01f);
        mainLayout.setColumnExpandRatio(3, .01f);
        mainLayout.setColumnExpandRatio(4, .01f);

        mainLayout.setSpacing(true);
        mainLayout.setMargin(new MarginInfo(false, true, false, true));
        mainLayout.setSizeFull();
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
        pTargetLayout.addComponent(item);
    }

    public void reload() {
        UserData loggedInUser = UIHelper.getSessionUser();
        boolean loginSectionVisible = false;
        if (loggedInUser == null || UserData.WORLD_USER.equals(loggedInUser)) {
            groupSelection.removeAllItems();
            loggedInUserLabel.setValue("");
        } else {
            loginSectionVisible = true;
            loggedInUserLabel.setValue(loggedInUser.getFullname());
            try {
                List<GroupId> userGroups = GroupServiceLocal.getSingleton().membershipsOf(new UserId(loggedInUser.getDistinguishedName()), 0, Integer.MAX_VALUE, AuthorizationContext.factorySystemContext());
                groupSelection.removeAllItems();
                userGroups.forEach((groupId) -> {
                    groupSelection.addItem(groupId.getStringRepresentation());
                });
                groupSelection.setNullSelectionAllowed(false);
                groupSelection.select(Constants.USERS_GROUP_ID);
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Failed to obtain membership of logged in user.", ex);
                UIComponentTools.showError("Failed to obtain memberships of logged in user. Please contact an administrator.");
            }
        }
        //set login section (in-)visible
        loggedInAsLabel.setVisible(loginSectionVisible);
        loggedInUserLabel.setVisible(loginSectionVisible);
        groupSelection.setVisible(loginSectionVisible);
        activeGroupLabel.setVisible(loginSectionVisible);
    }

    /**
     * Update the main menu depending on the role of the logged in user.
     * Depending of the role e.g. the settings menu item will be visible or not.
     *
     * @param pRole The current role.
     */
    private void updateMenu() {
        Role role = UIHelper.getSessionUserRole();
        roleLabel.setValue(role.toString());
        boolean menuVisible = !UserData.WORLD_USER.equals(UIHelper.getSessionUser()) && role.atLeast(Role.GUEST);
        boolean settingsVisible = menuVisible && role.atLeast(Role.MANAGER);
        home.setVisible(menuVisible);
        profile.setVisible(menuVisible);
        simon.setVisible(menuVisible);
        settings.setVisible(settingsVisible);
        exit.setVisible(menuVisible);
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        UIHelper.changeSessionGroup(new GroupId((String) event.getProperty().getValue()));
        updateMenu();
        parent.sessionGroupChanged();
    }
}
