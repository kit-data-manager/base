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

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.ui.commons.interfaces.IUserChangeListener;
import edu.kit.dama.ui.user.AbstractUserBridge;
import edu.kit.dama.util.Constants;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public abstract class AbstractApplication extends UI {

  private static Logger logger = LoggerFactory.getLogger(AbstractApplication.class);
  private final List<IUserChangeListener> userChangeListeners = Collections.synchronizedList(new LinkedList<IUserChangeListener>());
  private IMetaDataManager manager;

  public AbstractApplication() {
  }

  public IAuthorizationContext getSystemContext() {
    return new AuthorizationContext(new UserId(Constants.SYSTEM_ADMIN), new GroupId(Constants.SYSTEM_GROUP), Role.ADMINISTRATOR);
  }

  public abstract IAuthorizationContext getAuthorizationContext();

  public abstract UserData getActiveUser();

  public abstract AbstractUserBridge getUserBridge();

  public IMetaDataManager getMetaDataManager() {
    if (manager == null) {
      manager = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    }
    return manager;
  }

  public void reset() {
    try {
      logger.debug("Resetting application");
      if (manager != null) {
        logger.debug("* Closing meta data manger");
        manager.close();
      }
    } catch (Throwable t) {
      //ignored
    }

    logger.debug("* Obtaining meta data manger");
    manager = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    manager.setAuthorizationContext(getAuthorizationContext());
  }

  /**
   * Propagate that one or more user property/properties has/have changed. All
   * registered user change listeners will be notified to be able to update
   * depending on changed properties.
   */
  public void propagateUserPropertyChange() {
    fireUserChangedEvents(getActiveUser(), getActiveUser());
  }

  public void addUserChangeListener(final IUserChangeListener pListener) {
    synchronized (userChangeListeners) {
      userChangeListeners.add(pListener);
    }
  }

  public void removeUserChangeListener(IUserChangeListener pListener) {
    synchronized (userChangeListeners) {
      userChangeListeners.remove(pListener);
    }
  }

  public void fireUserChangedEvents(final UserData pOldUser, final UserData pNewUser) {
    logger.debug("Active user has changed from {} to {}", new Object[]{pOldUser, pNewUser});
    getMetaDataManager().setAuthorizationContext(getAuthorizationContext());
    for (IUserChangeListener listener : userChangeListeners.toArray(new IUserChangeListener[userChangeListeners.size()])) {
      listener.fireUserChangeEvent(pOldUser, pNewUser);
    }
  }

  /**
   * Show a notification to the user. This notification is a tray notification
   * provided by Vaadin, shown in the lower right of the main window. The
   * caption is 'Notification'.
   *
   * @param message The message to show.
   */
  @Override
  public void showNotification(String message) {
    showWarning("Notification", message);
  }

  /**
   * Show a notification to the user. This notification is a tray notification
   * provided by Vaadin, shown in the lower right of the main window.
   *
   * @param caption A custom caption.
   * @param message The message to show.
   */
  @Override
  public void showNotification(String caption, String message) {
    if (caption == null) {
      caption = "Notification";
    }
    new Notification(caption, message, Type.TRAY_NOTIFICATION).show(Page.getCurrent());
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
    new Notification(caption, message, Type.WARNING_MESSAGE).show(Page.getCurrent());
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
    new Notification(caption, message, Type.ERROR_MESSAGE).show(Page.getCurrent());
  }

  @Override
  public void markAsDirty() {
    //nothing to do here
  }
}
