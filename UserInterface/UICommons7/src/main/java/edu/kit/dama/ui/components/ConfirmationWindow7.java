/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.ui.components;

import com.vaadin.event.ShortcutAction;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import edu.kit.dama.ui.commons.util.UIUtils7;

/**
 * This class builds a subwindow <b>ConfirmationWindow</b> that asks the user to
 * confirm his requested action via a button click. The dialog can be customized
 * to some extend and supports:
 *
 * <ul>
 *
 * <li>Custom window title.</li>
 *
 * <li>XHTML-based notification message.</li>
 *
 * <li>Different option types (OK or YES_NO).</li>
 *
 * <li>Different message types (INFORMATION, WARNING or ERROR) visualized by an
 * according icon.</li>
 *
 * </ul>
 *
 * The window can be opened by using one of the provided static methods. These
 * methods also define the appearence of the confirmation window.
 *
 * @author rindone
 */
public class ConfirmationWindow7 extends Window implements Button.ClickListener {

  /**
   * The option type determines which buttons are available to the user.
   * OK_OPTION results in a dialog with one OK button. YES_NO_OPTION will
   * produce a dialog with two buttons, one for YES and one for NO option.
   */
  public enum OPTION_TYPE {

    OK_OPTION, YES_NO_OPTION
  }

  /**
   * The message type determines the icon which is shown on the upper left side.
   * There are three possible types: INFORMATION, WARNING and ERROR.
   */
  public enum MESSAGE_TYPE {

    QUESTION, INFORMATION, WARNING, ERROR
  }

  /**
   * Possible results. YES/NO results are produced by YES (OK) and NO button,
   * CANCEL is produced, if the dialog is closed without pressing any button.
   */
  public enum RESULT {

    YES, NO, CANCEL
  }
  private final String DEFAULT_TITLE = "Confirm";
  private NativeButton yesButton = null;
  private NativeButton noButton = null;
  private IConfirmationWindowListener7 listener = null;

  /**
   * Builds a customized subwindow <b>ConfirmationWindow</b> that allows the
   * user to confirm his previously requested action.
   *
   * @param pTitle The title of the window.
   * @param pMessage The message shown in the window.
   * @param pOptionType The type of the window (OK or YES_NO) which defines the
   * visible buttons.
   * @param pMessageType The message type (INFORMATION, WARNING, ERROR) which
   * determines the icon. If pMessageType is null, no icon will be shown.
   * @param pListener The listener which receives the result if a button was
   * pressed or the window was closed.
   *
   */
  ConfirmationWindow7(String pTitle, String pMessage, OPTION_TYPE pOptionType, MESSAGE_TYPE pMessageType, IConfirmationWindowListener7 pListener) {
    this.listener = pListener;
    //basic setup

    //set caption depending on type
    String caption = pTitle;
    if (caption == null) {
      if (pMessageType != null) {
        switch (pMessageType) {
          case QUESTION:
            caption = DEFAULT_TITLE;
            break;
          case INFORMATION:
            caption = "Information";
            break;
          case WARNING:
            caption = "Warning";
            break;
          case ERROR:
            caption = "Error";
            break;
        }
      } else {
        //no type provided...use default title
        caption = DEFAULT_TITLE;
      }
    }

    setCaption(caption);
    setModal(true);
    center();

    // Build line of buttons depending on pOptionType
    HorizontalLayout buttonLine = new HorizontalLayout();
    buttonLine.setSpacing(true);
    buttonLine.setWidth("100%");
    //add spacer
    Label spacer = new Label();
    buttonLine.addComponent(spacer);
    //add buttons
    if (OPTION_TYPE.YES_NO_OPTION.equals(pOptionType)) {
      buttonLine.addComponent(buildYesButton("Yes"));
      buttonLine.addComponent(buildNoButton());
      buttonLine.setComponentAlignment(yesButton, Alignment.MIDDLE_RIGHT);
      buttonLine.setComponentAlignment(noButton, Alignment.MIDDLE_RIGHT);
      //Assign ENTER to the YES button
      yesButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
      //Assign ESC to the NO button
      noButton.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
    } else {
      buttonLine.addComponent(buildYesButton("OK"));
      buttonLine.setComponentAlignment(yesButton, Alignment.MIDDLE_RIGHT);
      //Assign ENTER to the OK button
      yesButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
      //Assign ESC to close the dialog
      setCloseShortcut(ShortcutAction.KeyCode.ESCAPE, null);
    }

    buttonLine.setExpandRatio(spacer, 1.0f);

    //determine the icon depending on pMessageType
    ThemeResource icon = null;

    if (pMessageType != null) {
      switch (pMessageType) {
        case QUESTION:
          icon = new ThemeResource("img/24x24/question.png");
          break;
        case INFORMATION:
          icon = new ThemeResource("img/24x24/information.png");
          break;
        case WARNING:
          icon = new ThemeResource("img/24x24/warning.png");
          break;
        case ERROR:
          icon = new ThemeResource("img/24x24/forbidden.png");
          break;
      }
    }
    Component iconComponent = new Label();
    if (icon != null) {
      //icon was set, overwrite icon component
      iconComponent = new Image(null, icon);
    }

    //build the message label
    Label message = new Label(pMessage, ContentMode.HTML);
    message.setSizeUndefined();
    //build the main layout
    GridLayout mainLayout = new UIUtils7.GridLayoutBuilder(2, 2).addComponent(iconComponent, Alignment.TOP_LEFT, 0, 0, 1, 1).addComponent(message, 1, 0, 1, 1).addComponent(buttonLine, 0, 1, 2, 1).getLayout();
    mainLayout.setMargin(true);
    mainLayout.setSpacing(true);
    mainLayout.setColumnExpandRatio(0, .05f);
    mainLayout.setColumnExpandRatio(1, 1f);
    mainLayout.setRowExpandRatio(0, 1f);
    mainLayout.setRowExpandRatio(1, .05f);
    setContent(mainLayout);

    //add the close listener
    addCloseListener(new CloseListener() {
      @Override
      public void windowClose(CloseEvent e) {
        fireCloseEvents(RESULT.CANCEL);
      }
    });
  }

  /**
   * Opens a basic confirmation window with a standard title and a single OK
   * button.
   *
   * @param pMessage The message.
   * @param pListener The listener notified about the result.
   */
  public static void showConfirmation(String pMessage, IConfirmationWindowListener7 pListener) {
    showConfirmation(null, pMessage, pListener);
  }

  /**
   * Opens a basic confirmation window with a custom title and a single OK
   * button.
   *
   * @param pTitle The title.
   * @param pMessage The message.
   * @param pListener The listener notified about the result.
   */
  public static void showConfirmation(String pTitle, String pMessage, IConfirmationWindowListener7 pListener) {
    showConfirmation(pTitle, pMessage, OPTION_TYPE.OK_OPTION, pListener);
  }

  /**
   * Opens a basic confirmation window with a custom title and custom buttons
   * (OK or YES_NO)
   *
   * @param pTitle The title.
   * @param pMessage The message.
   * @param pOptionType The option type.
   * @param pListener The listener notified about the result.
   */
  public static void showConfirmation(String pTitle, String pMessage, OPTION_TYPE pOptionType, IConfirmationWindowListener7 pListener) {
    showConfirmation(pTitle, pMessage, pOptionType, null, pListener);
  }

  /**
   * Opens a basic confirmation window with a custom title, custom buttons (OK
   * or YES_NO) and a custom message type (INFORMATION, WARNING or ERROR).
   *
   * @param pTitle The title.
   * @param pMessage The message.
   * @param pOptionType The option type.
   * @param pMessageType The message type.
   * @param pListener The listener notified about the result.
   */
  public static void showConfirmation(String pTitle, String pMessage, OPTION_TYPE pOptionType, MESSAGE_TYPE pMessageType, IConfirmationWindowListener7 pListener) {
    ConfirmationWindow7 w = new ConfirmationWindow7(pTitle, pMessage, pOptionType, pMessageType, pListener);
    UI.getCurrent().addWindow(w);
  }

  /**
   * Builds the button <b>yesButton</b> that allows the user to confirm his
   * previously requested action.
   *
   * @param pCaption The caption (Yes or OK)
   *
   * @return The button.
   */
  private NativeButton buildYesButton(String pCaption) {
    yesButton = new NativeButton(pCaption);
    yesButton.setWidth("100px");
    yesButton.setImmediate(true);
    yesButton.focus();
    yesButton.addClickListener(this);
    return yesButton;
  }

  /**
   * Builds the button <b>noButton</b> that allows the user to disconfirm/take
   * back his previously requested action.
   *
   * @return The button.
   */
  private NativeButton buildNoButton() {
    noButton = new NativeButton("No");
    noButton.setWidth("100px");
    noButton.setImmediate(true);
    noButton.addClickListener(this);
    return noButton;
  }

  /**
   * Event handler for button clicks. Within the handler to confirmation
   * listener is notified and the window is closed.
   *
   * @param event The Button.ClickEvent
   */
  @Override
  public void buttonClick(Button.ClickEvent event) {
    if (event.getButton().equals(yesButton)) {
      fireCloseEvents(RESULT.YES);
    } else if (event.getButton().equals(noButton)) {
      fireCloseEvents(RESULT.NO);
    }
    //remove listener to avoid sending another event if the window closes (see addCloseListener(new CloseListener()))
    listener = null;

    UI.getCurrent().removeWindow(ConfirmationWindow7.this);
  }

  /**
   * Notify the registered ConfirmationWindowListener about the result pResult.
   *
   * @param pResult The result based on the user interaction (YES [OK], NO or
   * CANCEL)
   */
  protected void fireCloseEvents(RESULT pResult) {
    if (listener != null) {
      listener.fireConfirmationWindowCloseEvent(pResult);
    }
  }
}
