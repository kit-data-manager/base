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
package edu.kit.dama.ui.admin.utils;

import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UIComponentTools {

    private static final Logger LOGGER = LoggerFactory.getLogger(UIComponentTools.class);

    public enum Layout {

        GRID,
        VERTICAL,
        HORIZONTAL,
        UNDEFINED;
    }

    /**
     *
     * @param <T>
     * @param layout
     * @param locked
     */
    public static <T> void setLockedLayoutComponents(T layout, boolean locked) {
        if (layout == null) {
            return;
        }
        Layout layoutType;
        if (layout.getClass().equals(GridLayout.class) || layout instanceof GridLayout) {
            layoutType = Layout.GRID;
        } else if (layout.getClass().equals(VerticalLayout.class) || layout instanceof VerticalLayout) {
            layoutType = Layout.VERTICAL;
        } else if (layout.getClass().equals(HorizontalLayout.class) || layout instanceof HorizontalLayout) {
            layoutType = Layout.HORIZONTAL;
        } else {
            layoutType = Layout.UNDEFINED;
        }
        switch (layoutType) {
            case GRID:
                setLockedGridComponents((GridLayout) layout, locked);
                break;
            case VERTICAL:
                setLockedComponents((VerticalLayout) layout, locked);
                break;
            case HORIZONTAL:
                setLockedComponents((HorizontalLayout) layout, locked);
                break;
            case UNDEFINED:
            default:
                LOGGER.error(new StringBuilder("Failed to lock the layout components. ")
                        .append("Cause: Layout type undefined.").toString());
                break;
        }
    }

    /**
     *
     */
    private static void setLockedGridComponents(GridLayout layout, boolean locked) {
        for (int i = 0; i < layout.getColumns(); i++) {
            for (int j = 0; j < layout.getRows(); j++) {
                if (layout.getComponent(i, j) == null) {
                    continue;
                }
                if (layout.getComponent(i, j).getClass().equals(Button.class)) {
                    layout.getComponent(i, j).setEnabled(!locked);
                } else if (layout.getComponent(i, j).getClass().equals(ComboBox.class)) {
                    layout.getComponent(i, j).setEnabled(!locked);
                    layout.getComponent(i, j).setReadOnly(false);
                } else {
                    layout.getComponent(i, j).setReadOnly(locked);
                }
            }
        }
    }

    private static void setLockedComponents(AbstractOrderedLayout layout, boolean locked) {
        for (int i = 0; i < layout.getComponentCount(); i++) {
            if (layout.getComponent(i).getClass().equals(Button.class)) {
                layout.getComponent(i).setEnabled(!locked);
            } else if (layout.getComponent(i).getClass().equals(ComboBox.class)) {
                layout.getComponent(i).setEnabled(!locked);
                layout.getComponent(i).setReadOnly(locked);
            } else {
                layout.getComponent(i).setReadOnly(locked);
            }
        }
    }

    /**
     *
     * @param field
     * @return
     */
    public static boolean isEmpty(TextField field) {
        return field.getValue() == null || field.getValue().trim().isEmpty();
    }

    /**
     *
     * @param area
     * @return
     */
    public static boolean isEmpty(TextArea area) {
        return area.getValue().trim().isEmpty();
    }

    /**
     *
     * @param description
     *
     * @deprecated Moved to UIUtils7
     */
    public static void showError(String description) {
        showError("Error", description, -1);
    }

    /**
     *
     * @param caption
     * @param description
     * @param delay
     *
     * @deprecated Moved to UIUtils7
     */
    public static void showError(String caption, String description, int delay) {
        showNotification(caption, description, delay, Notification.Type.ERROR_MESSAGE);
    }

    /**
     *
     * @param description
     */
    public static void showWarning(String description) {
        showWarning("Warning", description, -1);
    }

    /**
     *
     * @param caption
     * @param description
     * @param delay
     *
     * @deprecated Moved to UIUtils7
     */
    public static void showWarning(String caption, String description, int delay) {
        showNotification(caption, description, delay, Notification.Type.WARNING_MESSAGE);
    }

    /**
     * Show a default information message that disappears automatically after 3
     * seconds.
     *
     * @param description
     *
     * @deprecated Moved to UIUtils7
     */
    public static void showInformation(String description) {
        showInformation("Information", description, 3000);
    }

    /**
     * Show an information message at the top_right position.
     *
     * @param caption
     * @param description
     * @param delay
     *
     * @deprecated Moved to UIUtils7
     */
    public static void showInformation(String caption, String description, int delay) {
        showNotification(caption, description, delay, Notification.Type.HUMANIZED_MESSAGE, Position.TOP_RIGHT);
    }

    /**
     * Show a notification with caption <i>caption</i>, message <i>message</i>
     * of type <i>pType</i> for <i>delay</i> milliseconds at middle_center
     * position.
     *
     * @param caption The caption.
     * @param message The message.
     * @param delay The delay (ms) until the nofication disappears.
     * @param pType The notification type.
     *
     * @deprecated Moved to UIUtils7
     */
    private static void showNotification(String caption, String message, int delay, Notification.Type pType) {
        showNotification(caption, message, delay, pType, Position.MIDDLE_CENTER);
    }

    /**
     * Show a notification with caption <i>caption</i>, message <i>message</i>
     * of type <i>pType</i> for
     * <i>delay</i> at top_center position.
     *
     * @param caption The caption.
     * @param message The message.
     * @param delay The delay (ms) until the nofication disappears.
     * @param pType The notification type.
     * @param pPosition The notification position.
     *
     * @deprecated Moved to UIUtils7
     */
    private static void showNotification(String caption, String message, int delay, Notification.Type pType, Position position) {
        Notification notification = new Notification(caption, message, pType);
        notification.setPosition(position);
        notification.setDelayMsec(delay);
        notification.setHtmlContentAllowed(true);
        notification.show(Page.getCurrent());
    }
}
