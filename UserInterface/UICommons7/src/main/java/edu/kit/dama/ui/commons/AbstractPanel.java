/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
package edu.kit.dama.ui.commons;

import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.admin.UserPropertyCollection;
import edu.kit.dama.mdm.admin.util.UserPropertyUtil;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.ui.commons.interfaces.IUserChangeListener;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public abstract class AbstractPanel extends VerticalLayout implements IUserChangeListener {

  /**
   * The logger.
   */
  private static Logger logger = LoggerFactory.getLogger(AbstractPanel.class);
  private static final String DUMMY_IDENTIFIER = "dummy";
  /**
   * The parent application.
   */
  private AbstractApplication parentApp;
  /**
   * The properties container for this panel.
   */
  private UserPropertyCollection properties = null;

  /**
   * Default constructor
   */
  public AbstractPanel(AbstractApplication pParentApp) {
    parentApp = pParentApp;
    if (parentApp != null) {
      parentApp.addUserChangeListener(AbstractPanel.this);
    } else {
      logger.warn("Parent application is null. Application events (e.g. user change) won't be published to this panel!");
    }
  }

  /**
   * Get the parent application of this panel. The parent application manages
   * several things needed by the panel, e.g. the metadata manager or the
   * current user context.
   *
   * @return The parent application.
   */
  public AbstractApplication getParentApp() {
    return parentApp;
  }

  /**
   * Obtains the metadata manager from the parent application. The metadata
   * manager is used to query the data backend for persisted objects.
   *
   * @return The metadata manager.
   */
  public IMetaDataManager getMetaDataManager() {
    return parentApp.getMetaDataManager();
  }

  /**
   * Return the user properties container for this panel. If no properties are
   * stored yet, a new container will be created and returned. If no user is
   * logged in or if the panel provides no panel id, a dummy container will be
   * returned, but not stored.
   *
   * @return The properties container for this panel.
   */
  public UserPropertyCollection getPropertiesContainer() {
    if (properties == null) {
      String id = getPanelId();
      if (id == null) {
        logger.warn("Panel returned no id. Properties won't be stored.");
        id = DUMMY_IDENTIFIER;
      }
      UserId currentUser = getParentApp().getAuthorizationContext().getUserId();
      if (currentUser == null) {
        logger.warn("No user logged in, yet. Returning dummy properties container.");
        return new UserPropertyCollection(id, DUMMY_IDENTIFIER);
      }
      try {
        properties = UserPropertyUtil.getProperties(getMetaDataManager(), currentUser, id);
      } catch (UnauthorizedAccessAttemptException ex) {
        logger.error("Not authorized to obtain properties", ex);
        return new UserPropertyCollection(id, DUMMY_IDENTIFIER);
      }
    }
    return properties;
  }

  /**
   * Store the properties container. This method will be called by the main
   * application if the application exits. Normally, this method should not be
   * overwritten as everything is managed by this implementation. But in some
   * case (e.g. a panel which is container for several other panels) overwriting
   * could make sense.
   */
  public void storePropertiesContainer() {
    if (properties != null) {
      if (DUMMY_IDENTIFIER.equals(properties.getCollectionIdentifier()) || DUMMY_IDENTIFIER.equals(properties.getUserId())) {
        logger.info("Identifier of user id is set to dummy value. Skip storing properties.");
      } else {
        try {
          logger.debug("Storing user properties.");
          getMetaDataManager().save(properties);
          logger.debug("Properties successfully stored.");
        } catch (UnauthorizedAccessAttemptException ex) {
          logger.error("Failed to save properties", ex);
        }
      }
    }
  }

  /**
   * Rebuild the panel layout. This method is called by
   * {@link #fireUserChangeEvent(edu.kit.dama.mdm.base.UserData, edu.kit.dama.mdm.base.UserData) }
   * and checks the current users role. Depending on the role, {@link #modifyLayout(edu.kit.dama.authorization.entities.Role)
   * } will be called and might modifiy the panels representation or not.
   */
  private void rebuildLayout() {
    IAuthorizationContext ctx = getParentApp().getAuthorizationContext();
    try {
      checkGuestAccess(ctx);
      try {
        checkMemberAccess(ctx);
        try {
          checkManagerAccess(ctx);
          try {
            checkAdminAccess(ctx);
            modifyLayout(Role.ADMINISTRATOR);
          } catch (UnauthorizedAccessAttemptException noAdminException) {
            logger.info("User has only MANAGER access");
            modifyLayout(Role.MANAGER);
          }
        } catch (UnauthorizedAccessAttemptException noManagerException) {
          logger.info("User has only MEMBER access");
          modifyLayout(Role.MEMBER);
        }
      } catch (UnauthorizedAccessAttemptException noMemberException) {
        logger.info("User has only GUEST access");
        modifyLayout(Role.GUEST);
      }
    } catch (UnauthorizedAccessAttemptException noGuestException) {
      logger.info("User has no access");
      modifyLayout(Role.NO_ACCESS);
    }
  }

  /**
   * Force the role check and rebuilding the layout depending on the current
   * user permissions.
   */
  public void forceRoleCheck() {
    rebuildLayout();
  }

  /**
   * Modify the panel layout depending on the provided role. The role represents
   * to current role of the user owning the current session. This method may be
   * overwritten by all instances of AbstractPanel if the panel contains
   * functionality which depends on a specific role (e.g. administrative
   * functionality). Otherwise, this method can be ignored. The default way to
   * increase/reduce functionality is by showing/hiding single elements within
   * the panel menu. In special cases also components of the panel itself may be
   * shown/hidden.
   *
   * @param pRole The role of the user accessing this panel.
   */
  public void modifyLayout(Role pRole) {
    logger.debug("No role-specific modifications necessary.");
  }

  @SecuredMethod(roleRequired = Role.GUEST)
  private void checkGuestAccess(@Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    logger.debug("Guest access granted");
  }

  @SecuredMethod(roleRequired = Role.MEMBER)
  private void checkMemberAccess(@Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    logger.debug("Member access granted");
  }

  @SecuredMethod(roleRequired = Role.MANAGER)
  private void checkManagerAccess(@Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    logger.debug("Manager access granted");
  }

  @SecuredMethod(roleRequired = Role.ADMINISTRATOR)
  private void checkAdminAccess(@Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    logger.debug("Admin access granted");
  }

  /**
   * Return the internal panel ID. This ID is used to identify the panel, e.g.
   * to assign panel properties.
   *
   * @return The ID of the panel
   */
  public abstract String getPanelId();

  /**
   * Get the plain title of the panel. This title might be shown in the user
   * interface, therefore it should be understandable.
   *
   * @return The title of the panel
   */
  public abstract String getPanelTitle();

  /**
   * Get the panel menu which contains functionality which is not directly
   * accessible. The menu can be shown in the parent application depending on
   * the current context.
   *
   * @return The Menubar for this panel
   */
  public abstract MenuBar getPanelMenu();

  /**
   * Update the panel for the provided digital object. Some panels may not
   * depend on a specific digital object. These panels should call 'reload()'
   * instead.
   *
   * @param pSelectedObject The currently selected digital object
   */
  public abstract void update(DigitalObjectId pSelectedObject);

  /**
   * Update the panel using the provided study. By default, the first object of
   * the first investigation of this study will be used. If no investigation is
   * available, update((Investigation)null) will be called, otherwise
   * update(firstInvestigationOfStudy) is called. This method may be overwritten
   * by panels which support visualization of studies itself.
   *
   * @param pStudy The study which will be used to update the panel.
   */
  public void update(Study pStudy) {
    logger.debug("Updating panel by study");
    if (pStudy != null) {
      Set<Investigation> investigations = pStudy.getInvestigations();
      if (investigations != null && !investigations.isEmpty()) {
        for (Investigation inv : investigations) {
          if (inv.isVisible()) {
            update(inv);
          }
        }
      }
    }
    update((Investigation) null);
  }

  /**
   * Update the panel using the provided investigation. By default, the first
   * object of the provided investigation will be used. If no object is
   * available, update((DigitalObjectId)null) will be called, otherwise
   * update(firstObjectOfInvestigation) is called. This method may be
   * overwritten by panels which support visualization of investigations itself.
   *
   * @param pInvestigation The investigation which will be used to update the
   * panel.
   */
  public void update(Investigation pInvestigation) {
    logger.debug("Updating panel by investigation");
    if (pInvestigation != null) {
      Set<DigitalObject> digitalObjects = pInvestigation.getDataSets();
      if (digitalObjects != null && !digitalObjects.isEmpty()) {
        for (DigitalObject o : digitalObjects) {
          if (o.isVisible()) {
            update((DigitalObjectId) o.getDigitalObjectId());
            return;
          }
        }
      }
    }
    //no update done yet
    update((DigitalObjectId) null);
  }

  /**
   * Reload the panel and all data from the data backend.
   */
  public abstract void reload();

  /**
   * This method will be called by the parent application, when the application
   * is terminated. The panel should not collect all properties and store them
   * in the UserPropertiesCollection container of the panel.
   */
  public abstract void collectProperties();

  /**
   * Restore all properties of this panel.This method is called by the parent
   * application in the moment, the panel is initialized and the
   * UserPropertiesCollection container is set. Now, the panel can restore all
   * properties from the container to the user interface.
   */
  public abstract void restoreProperties();

  /**
   * Event listener for user change events. This method will be called by the
   * parent application when the current user has changed. As the user is normally
   * managed by the portal running the parent application, this method mainly
   * covers property and/or group/role changes, whereas the provided user
   * objects should represent the same user. This method triggers an update of
   * the panel layout and restores all user-dependent properties.
   *
   * @param pOldUser The old user.
   * @param pNewUser The new user.
   *
   * @see modifyLayout(Role pRole)
   */
  public void fireUserChangeEvent(UserData pOldUser, UserData pNewUser) {
    logger.info("The current user or its properties have changed, updating panel {}", getPanelId());
    rebuildLayout();
  }

  /**
   * Show a notification to the user. This notification is a simple tray
   * notification provided by Vaadin, shown in the lower right corner of the
   * main window.
   *
   * @param message The message to show.
   */
  public void showNotification(String message) {
    showNotification("Information", message);
  }

  /**
   * Show a notification to the user. This notification is a simple tray
   * notification provided by Vaadin, shown in the lower right corner of the
   * main window.
   *
   * @param caption A custom caption.
   * @param message The message to show.
   */
  public void showNotification(String caption, String message) {
    if (caption == null) {
      caption = "Information";
    }
    getParentApp().showNotification(caption, message);
  }

  /**
   * Show a notification to the user. This notification is a warning
   * notification provided by Vaadin, shown in the middle of the main window.
   * The caption is 'Warning'.
   *
   * @param message The message to show.
   */
  public void showWarning(String message) {
    showWarning("Warning", message);
  }

  /**
   * Show a notification to the user. This notification is a warning
   * notification provided by Vaadin, shown in the middle of the main window.
   *
   * @param caption A custom caption.
   * @param message The message to show.
   */
  public void showWarning(String caption, String message) {
    if (caption == null) {
      caption = "Warning";
    }
    getParentApp().showWarning(caption, message);
  }

  /**
   * Show a notification to the user. This notification is a modal error
   * notification provided by Vaadin, shown in the middle of the main window.
   * The caption is 'Error'.
   *
   * @param message The error message to show.
   */
  public void showError(String message) {
    showError("Error", message);
  }

  /**
   * Show a notification to the user. This notification is a modal error
   * notification provided by Vaadin, shown in the middle of the main window.
   *
   * @param caption A custom caption.
   * @param message The message to show.
   */
  public void showError(String caption, String message) {
    if (caption == null) {
      caption = "Error";
    }
    getParentApp().showError(caption, message);
  }
}
