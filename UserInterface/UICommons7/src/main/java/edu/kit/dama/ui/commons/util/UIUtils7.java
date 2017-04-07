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
package edu.kit.dama.ui.commons.util;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.UserError;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractComponentContainer;
import com.vaadin.ui.AbstractField;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.File;
import java.io.PrintStream;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;

/**
 * UIUtils7 containing several helper methods for working with Vaadin UIs. This
 * implementation is identical with
 * {@link edu.kit.dama.ui.commons.util.UIUtils7} but compatible with Vaadin 7.
 *
 * @author jejkal
 */
public class UIUtils7 {

    /**
     * Set all components within pContainer to readOnly/notReadOnly. If
     * pRecursive is set true, containers withing the provided container are
     * processed recursively. If a contained component is a button, the button
     * is enabled/disabled instead of setting it to readOnly.
     *
     * @param pContainer The container to process
     * @param pRecursive Recurse into sub-containers
     * @param pValue TRUE = ReadOnly
     */
    public static void setReadOnly(ComponentContainer pContainer, boolean pRecursive, boolean pValue) {
        pContainer.setReadOnly(pValue);

        if (pRecursive) {
            Iterator<Component> com = pContainer.iterator();
            while (com.hasNext()) {
                Component next = com.next();
                if (next instanceof ComponentContainer) {
                    setReadOnly((ComponentContainer) next, pRecursive, pValue);
                } else if (next instanceof Button) {
                    ((Button) next).setEnabled(!pValue);
                } else {
                    next.setReadOnly(pValue);
                }
            }
        }
    }

    /**
     * Enable/disable all components within pContainer. If pRecursive is set
     * true, containers withing the provided container are processed
     * recursively.
     *
     * @param pContainer The container to process
     * @param pRecursive Recurse into sub-containers
     * @param pValue TRUE = Enabled
     */
    public static void setEnabled(ComponentContainer pContainer, boolean pRecursive, boolean pValue) {
        pContainer.setReadOnly(pValue);

        if (pRecursive) {
            Iterator<Component> com = pContainer.iterator();
            while (com.hasNext()) {
                Component next = com.next();
                if (next instanceof ComponentContainer) {
                    setEnabled((ComponentContainer) next, pRecursive, pValue);
                } else {
                    next.setEnabled(pValue);
                }
            }
        }
    }

    /**
     * Create a text area with the provided properties. The size is set to
     * SizeFull and the text area is set to 'immediate' (show changed
     * immediately to the user).
     *
     * @param pCaption The caption
     * @param pInputPrompt The input prompt shown if no value is set
     * @param pMaxLength The max. string length of the text area. The content
     * can be checked during validation.
     *
     * @return The text area
     */
    public static TextArea factoryTextArea(String pCaption, String pInputPrompt, int pMaxLength) {
        return factoryTextArea(pCaption, pInputPrompt, -1, pMaxLength);
    }

    /**
     * Create a text area with the provided properties. The size is set to
     * SizeFull and the text area is set to 'immediate' (show changed
     * immediately to the user).
     *
     * @param pCaption The caption
     * @param pInputPrompt The input prompt shown if no value is set
     * @param pMinLength The min. string length of the text area. The content
     * can be checked during validation.
     * @param pMaxLength The max. string length of the text area. The content
     * can be checked during validation.
     *
     * @return The text area
     */
    public static TextArea factoryTextArea(String pCaption, String pInputPrompt, int pMinLength, int pMaxLength) {
        TextArea theArea = new TextArea();
        theArea.setCaption(pCaption);
        theArea.setImmediate(true);
        theArea.setSizeFull();
        theArea.setInputPrompt(pInputPrompt);
        theArea.setNullSettingAllowed(false);
        theArea.setNullRepresentation("");
        StringLengthValidator val;
        if (pMinLength > 0) {
            val = new StringLengthValidator("The content length of this text area must be between " + pMinLength + " and " + pMaxLength + " characters.");
            val.setMinLength(pMinLength);
        } else {
            val = new StringLengthValidator("The maximum content length of this text area is " + pMaxLength + " characters.");
        }

        val.setMaxLength(pMaxLength);
        theArea.addValidator(val);
        return theArea;
    }

    /**
     * Create a text area with the provided properties. The size is set to
     * SizeFull and the text area is set to 'immediate' (show changed
     * immediately to the user).
     *
     * @param pCaption The caption
     * @param pInputPrompt The input prompt shown if no value is set
     * @param pMinLength The min. string length of the text area. The content
     * can be checked during validation.
     * @param pMaxLength The max. string length of the text area. The content
     * can be checked during validation.
     *
     * @return The text area
     */
    public static TextArea factoryNullableTextArea(String pCaption, String pInputPrompt, int pMinLength, int pMaxLength) {
        TextArea result = factoryTextArea(pCaption, pInputPrompt, pMinLength, pMaxLength);
        result.setNullSettingAllowed(true);
        result.setNullRepresentation(null);
        return result;
    }

    /**
     * Create a text area with the provided properties. The size is set to
     * SizeFull and the text area is set to 'immediate' (show changed
     * immediately to the user).
     *
     * @param pCaption The caption
     * @param pInputPrompt The input prompt shown if no value is set
     *
     * @return The text area
     */
    public static TextArea factoryNullableTextArea(String pCaption, String pInputPrompt) {
        TextArea theArea = new TextArea();
        theArea.setCaption(pCaption);
        theArea.setImmediate(true);
        theArea.setSizeFull();
        theArea.setInputPrompt(pInputPrompt);
        return theArea;
    }

    /**
     * Create a text area with the provided properties. The size is set to
     * SizeFull and the text area is set to 'immediate' (show changed
     * immediately to the user). The max. string length for the returned area is
     * 1024 characters.
     *
     * @param pCaption The caption
     * @param pInputPrompt The input prompt shown if no value is set
     *
     * @return The text area
     */
    public static TextArea factoryTextArea(String pCaption, String pInputPrompt) {
        return factoryTextArea(pCaption, pInputPrompt, 1024);
    }

    /**
     * Create a text field with the provided properties. The width is set to
     * 100% and the text field is set to 'immediate' (show changed immediately
     * to the user). The max. string length for the returned field is 255
     * characters.
     *
     * @param pCaption The field caption
     * @param pInputPrompt The input prompt shown if no value is set
     *
     * @return The text field
     *
     * @see factoryTextField(String pCaption, String pInputPrompt, String
     * pWidth, boolean pImmediate)
     */
    public static TextField factoryTextField(String pCaption, String pInputPrompt) {
        return factoryTextField(pCaption, pInputPrompt, "100%", true, -1, 255);
    }

    /**
     * Create a text field with the provided properties. The width is set to
     * 100% and the text field is set to 'immediate' (show changed immediately
     * to the user). The max. string length for the returned field is 255
     * characters.
     *
     * @param pCaption The field caption
     * @param pInputPrompt The input prompt shown if no value is set
     * @param pMinLength The min length.
     *
     * @return The text field
     *
     * @see factoryTextField(String pCaption, String pInputPrompt, String
     * pWidth, boolean pImmediate)
     */
    public static TextField factoryTextField(String pCaption, String pInputPrompt, int pMinLength) {
        return factoryTextField(pCaption, pInputPrompt, "100%", true, pMinLength, 255);
    }

    /**
     * Create a text field with the provided properties. The width is set to
     * 100% and the text field is set to 'immediate' (show changed immediately
     * to the user).
     *
     * @param pCaption The field caption
     * @param pInputPrompt The input prompt shown if no value is set
     * @param pMinLength The min length.
     * @param pMaxLength The max length.
     *
     * @return The text field
     *
     * @see factoryTextField(String pCaption, String pInputPrompt, String
     * pWidth, boolean pImmediate)
     */
    public static TextField factoryTextField(String pCaption, String pInputPrompt, int pMinLength, int pMaxLength) {
        return factoryTextField(pCaption, pInputPrompt, "100%", true, pMinLength, pMaxLength);
    }

    /**
     * Create a text field with the provided properties. In addition, the
     * returned text field won't allow to set 'null' values by the user and has
     * no null-representation. Therefore it shows the input prompt when no value
     * is provided. The height of the text field is left to the default value
     * ('undefined').
     *
     * @param pCaption The field caption
     * @param pInputPrompt The input prompt shown if no value is set
     * @param pWidth The field width (e.g. 100% or 60px)
     * @param pImmediate The value of the 'immediate' flag
     * @param pMaxLength The max. string length of the text field. The content
     * can be checked during validation.
     *
     * @return The text field
     */
    public static TextField factoryTextField(String pCaption, String pInputPrompt, String pWidth, boolean pImmediate, int pMaxLength) {
        return factoryTextField(pCaption, pInputPrompt, pWidth, pImmediate, -1, pMaxLength);
    }

    /**
     * Create a text field with the provided properties. In addition, the
     * returned text field won't allow to set 'null' values by the user and
     * returns an empty string if no value has been set. The height of the text
     * field is left to the default value ('undefined').
     *
     * @param pCaption The field caption.
     * @param pInputPrompt The input prompt shown if no value is set.
     * @param pWidth The field width (e.g. 100% or 60px).
     * @param pImmediate The value of the 'immediate' flag.
     * @param pMinLength The min. string length of the text field. The content
     * can be checked during validation.
     * @param pMaxLength The max. string length of the text field. The content
     * can be checked during validation.
     *
     * @return The text field
     */
    public static TextField factoryTextField(String pCaption, String pInputPrompt, String pWidth, boolean pImmediate, int pMinLength, int pMaxLength) {
        TextField theField = new TextField();
        theField.setCaption(pCaption);
        theField.setImmediate(pImmediate);
        theField.setWidth(pWidth);
        theField.setInputPrompt(pInputPrompt);
        theField.setNullSettingAllowed(false);
        theField.setNullRepresentation("");

        StringLengthValidator val;
        if (pMinLength > 0) {
            val = new StringLengthValidator("The content length of this text field must be between " + pMinLength + " and " + pMaxLength + " characters.");
            val.setMinLength(pMinLength);
        } else {
            val = new StringLengthValidator("The maximum content length of this text field is " + pMaxLength + " characters.");
        }
        val.setMaxLength(pMaxLength);
        theField.addValidator(val);
        return theField;
    }

    /**
     * Create a text field with the provided properties. This field allows null
     * values. Furthermore, it assumes a min. length of 0 characters and a max.
     * length of 255 characters. With these characteristics, this textfield is
     * optimal for collection input that is stored in a default database field
     * of type varchar without further checks. The height of the text field is
     * left to the default value ('undefined'), the width is 100%.
     *
     * @param pCaption The field caption.
     * @param pInputPrompt The input prompt shown if no value is set.
     *
     * @return The text field
     */
    public static TextField factoryNullableTextField(String pCaption, String pInputPrompt) {
        return factoryNullableTextField(pCaption, pInputPrompt, "100%", true, 0, 255);
    }

    /**
     * Create a text field with the provided properties. This field allows null
     * values. The height of the text field is left to the default value
     * ('undefined').
     *
     * @param pCaption The field caption.
     * @param pInputPrompt The input prompt shown if no value is set.
     * @param pWidth The field width (e.g. 100% or 60px).
     * @param pMinLength The min. string length of the text field. The content
     * can be checked during validation.
     * @param pMaxLength The max. string length of the text field. The content
     * can be checked during validation.
     *
     * @return The text field
     */
    public static TextField factoryNullableTextField(String pCaption, String pInputPrompt, String pWidth, int pMinLength, int pMaxLength) {
        return factoryNullableTextField(pCaption, pInputPrompt, pWidth, true, pMinLength, pMaxLength);
    }

    /**
     * Create a text field with the provided properties. This field allows null
     * values. The height of the text field is left to the default value
     * ('undefined').
     *
     * @param pCaption The field caption.
     * @param pInputPrompt The input prompt shown if no value is set.
     * @param pWidth The field width (e.g. 100% or 60px).
     * @param pImmediate The value of the 'immediate' flag.
     * @param pMinLength The min. string length of the text field. The content
     * can be checked during validation.
     * @param pMaxLength The max. string length of the text field. The content
     * can be checked during validation.
     *
     * @return The text field
     */
    public static TextField factoryNullableTextField(String pCaption, String pInputPrompt, String pWidth, boolean pImmediate, int pMinLength, int pMaxLength) {
        TextField theField = new TextField();
        theField.setCaption(pCaption);
        theField.setImmediate(pImmediate);
        theField.setWidth(pWidth);
        theField.setInputPrompt(pInputPrompt);
        theField.setNullSettingAllowed(true);

        StringLengthValidator val;
        if (pMinLength > 0) {
            val = new StringLengthValidator("The content length of this text field must be between " + pMinLength + " and " + pMaxLength + " characters.");
            val.setMinLength(pMinLength);
        } else {
            val = new StringLengthValidator("The maximum content length of this text field is " + pMaxLength + " characters.");
        }
        val.setMaxLength(pMaxLength);
        theField.addValidator(val);
        return theField;
    }

    /**
     * Create a password field with the provided properties. In addition, the
     * returned password field won't allow to set 'null' values by the user and
     * has no null-representation. As this is a password field, no input prompt
     * is shown. The height of the text field is left to the default value
     * ('undefined').
     *
     * @param pCaption The field caption.
     * @param pWidth The field width (e.g. 100% or 60px).
     * @param pImmediate The value of the 'immediate' flag.
     * @param pMinLength The min. string length of the text field. The content
     * can be checked during validation.
     * @param pMaxLength The max. string length of the text field. The content
     * can be checked during validation.
     *
     * @return The text field
     */
    public static PasswordField factoryPasswordField(String pCaption, String pWidth, boolean pImmediate, int pMinLength, int pMaxLength) {
        PasswordField theField = new PasswordField();
        theField.setCaption(pCaption);
        theField.setImmediate(pImmediate);
        theField.setWidth(pWidth);
        theField.setNullSettingAllowed(false);
        theField.setNullRepresentation("");

        StringLengthValidator val;
        if (pMinLength > 0) {
            val = new StringLengthValidator("The content length of this text field must be between " + pMinLength + " and " + pMaxLength + " characters.");
            val.setMinLength(pMinLength);
        } else {
            val = new StringLengthValidator("The maximum content length of this text field is " + pMaxLength + " characters.");
        }
        val.setMaxLength(pMaxLength);
        theField.addValidator(val);
        return theField;
    }

    /**
     * Lock/unlock the provided layout. Locking a layout will disable all
     * components and set them to read only.
     *
     * @param layout The layout.
     * @param locked TRUE = lock the layout, FALSE = unlock the layout.
     */
    public static void setLockedLayoutComponents(AbstractLayout layout, boolean locked) {
        if (layout == null) {
            return;
        }
        if (layout instanceof GridLayout) {
            setLockedGridComponents((GridLayout) layout, locked);
        } else if (layout instanceof AbstractOrderedLayout) {
            setLockedComponents((AbstractOrderedLayout) layout, locked);
        }
    }

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
     * Check if the provided text field is empty.
     *
     * @param field The field to check
     *
     * @return TRUE if the field is empty
     */
    public static boolean isEmpty(TextField field) {
        return field.getValue() == null || field.getValue().trim().isEmpty();
    }

    /**
     * Check if the provided text area is empty.
     *
     * @param area The area to check
     * @return TRUE if the area is empty
     */
    public static boolean isEmpty(TextArea area) {
        return area.getValue().trim().isEmpty();
    }

    /**
     * Validate all components within the provided component container. If a
     * component is invalid, an appropriate component error will be set and the
     * method returns 'FALSE'. For valid components, the component error will be
     * removed. If all components are valid according to their setup, 'TRUE'
     * will be returned. A component can be invalid because:
     *
     * <ul>
     *
     * <li>A validator installed by <i>Component.addValidator()</i> fails</li>
     *
     * <li>The component is marked as 'required' and contains no value</li>
     *
     * </ul>
     *
     *
     * @param pContainer The component container to validate.
     *
     * @return TRUE if all components within pContainer are valid.
     */
    public static boolean validate(ComponentContainer pContainer) {
        boolean result = true;
        Iterator<Component> i = pContainer.iterator();
        while (i.hasNext()) {
            Component c = i.next();
            //keep 'false' value if result is once false
            result = (result == true) ? validate(c) : result;

        }
        return result;
    }

    /**
     * Validate a single component. If a component is invalid, an appropriate
     * component error will be set and the method returns 'FALSE'. For valid
     * components, the component error will be removed. If the component is
     * valid according to their setup, 'TRUE' will be returned. A component can
     * be invalid because:
     *
     * <ul>
     *
     * <li>A validator installed by <i>Component.addValidator()</i> fails</li>
     *
     * <li>The component is marked as 'required' and contains no value</li>
     *
     * </ul>
     *
     * @param pComponent The component to validate.
     *
     * @return TRUE if the component is valid.
     */
    public static boolean validate(Component pComponent) {
        boolean result = true;
        if (pComponent instanceof AbstractField) {
            try {
                ((AbstractField) pComponent).validate();
                ((AbstractField) pComponent).setComponentError(null);
            } catch (Validator.InvalidValueException e) {
                ((AbstractComponent) pComponent).setComponentError(new UserError("The value of this field is invalid."));
                result = false;
            }
        } else if (pComponent instanceof AbstractComponentContainer) {
            if (!validate((AbstractComponentContainer) pComponent)) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Open a window for accessing the provided file.
     *
     * @param sourceFile The file to provide. Must not be null.
     */
    public static void openResourceSubWindow(File sourceFile) {
        if (sourceFile == null) {
            throw new IllegalArgumentException("Argument sourceFile must not be null.");
        }
        boolean fileAccessible = sourceFile.exists() && sourceFile.canRead();

        // Set subwindow for displaying file resource
        final Window window = new Window(fileAccessible ? sourceFile.getName() : "Information");
        window.center();
        // Set window layout
        VerticalLayout windowLayout = new VerticalLayout();
        windowLayout.setSizeFull();

        if (fileAccessible) {
            // Set resource that has to be embedded
            final Embedded resource = new Embedded(null, new FileResource(sourceFile));
            if ("application/octet-stream".equals(resource.getMimeType())) {
                window.setWidth("570px");
                window.setHeight("150px");
                windowLayout.setMargin(true);

                Label attentionNote = new Label("A file preview is not possible as the file type is not supported by your browser.");
                attentionNote.setContentMode(ContentMode.HTML);
                Link fileURL = new Link("Click here for downloading the file.", new FileResource(sourceFile));

                windowLayout.addComponent(attentionNote);
                windowLayout.addComponent(fileURL);
                windowLayout.setComponentAlignment(attentionNote, Alignment.MIDDLE_CENTER);
                windowLayout.setComponentAlignment(fileURL, Alignment.MIDDLE_CENTER);
            } else {
                window.setResizable(true);
                window.setWidth("800px");
                window.setHeight("500px");
                final Image image = new Image(null, new FileResource(sourceFile));
                image.setSizeFull();
                windowLayout.addComponent(image);
            }
        } else {
            //file is not accessible
            window.setWidth("570px");
            window.setHeight("150px");
            windowLayout.setMargin(true);
            Label attentionNote = new Label("Provided file cannot be accessed.");
            attentionNote.setContentMode(ContentMode.HTML);
            windowLayout.addComponent(attentionNote);
            windowLayout.setComponentAlignment(attentionNote, Alignment.MIDDLE_CENTER);
        }

        window.setContent(windowLayout);
        UI.getCurrent().addWindow(window);
    }

    /**
     *
     * @param description
     */
    public static void showError(String description) {
        showError("Error", description, -1);
    }

    /**
     *
     * @param caption
     * @param description
     * @param delay
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
     */
    public static void showWarning(String caption, String description, int delay) {
        showNotification(caption, description, delay, Notification.Type.WARNING_MESSAGE);
    }

    /**
     * Show a default information message that disappears automatically after 3
     * seconds.
     *
     * @param description
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
     */
    public static void showInformation(String caption, String description, int delay) {
        showNotification(caption, description, delay, Notification.Type.HUMANIZED_MESSAGE, Position.MIDDLE_CENTER);
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
     */
    private static void showNotification(String caption, String message, int delay, Notification.Type pType, Position position) {
        Notification notification = new Notification(caption, message, pType);
        notification.setPosition(position);
        notification.setDelayMsec(delay);
        notification.setHtmlContentAllowed(true);
        notification.show(Page.getCurrent());
    }

    public static class GridLayoutBuilder {

        /**
         * The default alignment of a component within a cell
         */
        private static final Alignment DEFAULT_ALIGNMENT = Alignment.TOP_LEFT;
        /**
         * A representation used for empty cells
         */
        private static final String EMPTY_CELL = "||||||||||";
        /**
         * The final layout
         */
        private final GridLayout layout;
        /**
         * The debugging representation of the layout
         */
        private final String[][] assignedFields;
        /**
         * The number of columns
         */
        private final int columns;
        /**
         * The number of rows
         */
        private final int rows;

        /**
         * The default constructor initializing a layout builder with the
         * provided dimensions.
         *
         * @param pColumns The number of columns
         * @param pRows The number of rows
         */
        public GridLayoutBuilder(int pColumns, int pRows) {
            columns = pColumns;
            rows = pRows;
            layout = new GridLayout(columns, rows);
            assignedFields = new String[columns][rows];

            for (int i = 0; i < columns; i++) {
                for (int j = 0; j < rows; j++) {
                    assignedFields[i][j] = EMPTY_CELL;
                }
            }
        }

        /**
         * The constructor taking an existing GridLayout. This layout is parsed
         * and may be filled (only for cells which are not assigned yet) or
         * printed out for debugging purposes.
         *
         * @param pLayout The existing layout
         */
        public GridLayoutBuilder(GridLayout pLayout) {
            columns = pLayout.getColumns();
            rows = pLayout.getRows();
            assignedFields = new String[columns][rows];

            for (int i = 0; i < columns; i++) {
                for (int j = 0; j < rows; j++) {
                    assignedFields[i][j] = EMPTY_CELL;
                }
            }
            for (int i = 0; i < columns; i++) {
                for (int j = 0; j < rows; j++) {
                    Component com = pLayout.getComponent(i, j);
                    if (com != null) {
                        assignedFields[i][j] = getComponentIdentifier(com);
                    }
                }
            }
            layout = pLayout;
        }

        /**
         * Add a component filling row and column defined by pColumn and pRow
         * till the end of the grid. The default default alignment is used.
         *
         * @param pComponent The component, which must not be 'null'
         * @param pColumn The target column
         * @param pRow The target row
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder fill(Component pComponent, int pColumn, int pRow) {
            return addComponent(pComponent, null, pColumn, pRow, columns - pColumn, rows - pRow);
        }

        /**
         * Add a component filling row and column defined by pColumn and pRow
         * till the end of the grid.
         *
         * @param pComponent The component, which must not be 'null'
         * @param pAlignment The component alignment within the cell
         * @param pColumn The target column
         * @param pRow The target row
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder fill(Component pComponent, Alignment pAlignment, int pColumn, int pRow) {
            return addComponent(pComponent, pAlignment, pColumn, pRow, columns - pColumn, rows - pRow);
        }

        /**
         * Add a component filling the column defined by pColumn till the end of
         * the grid. The default default alignment is used.
         *
         * @param pComponent The component, which must not be 'null'
         * @param pColumn The target column
         * @param pRow The target row
         * @param pWidth The width in cells
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder fillColumn(Component pComponent, int pColumn, int pRow, int pWidth) {
            return addComponent(pComponent, null, pColumn, pRow, pWidth, rows - pRow);
        }

        /**
         * Add a component filling the column defined by pColumn till the end of
         * the grid.
         *
         * @param pComponent The component, which must not be 'null'
         * @param pAlignment The component alignment within the cell
         * @param pColumn The target column
         * @param pRow The target row
         * @param pWidth The width in cells
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder fillColumn(Component pComponent, Alignment pAlignment, int pColumn, int pRow, int pWidth) {
            return addComponent(pComponent, pAlignment, pColumn, pRow, pWidth, rows - pRow);
        }

        /**
         * Add a component filling the row defined by pRow till the end of the
         * grid. The default default alignment is used.
         *
         * @param pComponent The component, which must not be 'null'
         * @param pColumn The target column
         * @param pRow The target row
         * @param pHeight The height in cells
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder fillRow(Component pComponent, int pColumn, int pRow, int pHeight) {
            return addComponent(pComponent, null, pColumn, pRow, columns - pColumn, pHeight);
        }

        /**
         * Add a component filling the row defined by pRow till the end of the
         * grid.
         *
         * @param pComponent The component, which must not be 'null'
         * @param pAlignment The component alignment within the cell
         * @param pColumn The target column
         * @param pRow The target row
         * @param pHeight The height in cells
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder fillRow(Component pComponent, Alignment pAlignment, int pColumn, int pRow, int pHeight) {
            return addComponent(pComponent, pAlignment, pColumn, pRow, columns - pColumn, pHeight);
        }

        /**
         * Add a component with the size 1x1 using the default alignment.
         *
         * @param pComponent The component, which must not be 'null'
         * @param pColumn The target column
         * @param pRow The target row
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder addComponent(Component pComponent, int pColumn, int pRow) {
            return addComponent(pComponent, null, pColumn, pRow, 1, 1);
        }

        /**
         * Add a component using the default alignment.
         *
         * @param pComponent The component, which must not be 'null'
         * @param pColumn The target column
         * @param pRow The target row
         * @param pWidth The width in cells
         * @param pHeight The height in cells
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder addComponent(Component pComponent, int pColumn, int pRow, int pWidth, int pHeight) {
            return addComponent(pComponent, null, pColumn, pRow, pWidth, pHeight);
        }

        /**
         * Add a new component to the layout. This method may throw an
         * IllegalArgumentException if pComponent is null or an
         * IndexOutOfBoundsException if the component location exceeds the
         * layout grid. Furthermore it is checked, whether to component overlaps
         * another component added before. If this is the case, an
         * IllegalArgumentException is throws and the current layout is printed
         * to StdErr. If everything works fine, the component is added to the
         * layout.
         *
         * @param pComponent The component, which must not be 'null'
         * @param pAlignment The component alignment within the cell. The
         * default alignment is MIDDLE_CENTER.
         * @param pColumn The target column
         * @param pRow The target row
         * @param pWidth The width in cells
         * @param pHeight The height in cells
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder addComponent(Component pComponent, Alignment pAlignment, int pColumn, int pRow, int pWidth, int pHeight) {
            if (pComponent == null) {
                throw new IllegalArgumentException("Argument pComponent must not be null");
            }

            Alignment componentAlignment = pAlignment;
            if (componentAlignment == null) {
                componentAlignment = DEFAULT_ALIGNMENT;
            }

            if (pColumn + pWidth > columns || pRow + pHeight > rows) {
                printLayoutToStdErr();
                throw new IndexOutOfBoundsException("Failed to add component " + getComponentIdentifier(pComponent) + ", grid too small.");
            }
            for (int i = pColumn; i < pColumn + pWidth; i++) {
                for (int j = pRow; j < pRow + pHeight; j++) {
                    if (!assignedFields[i][j].equals(EMPTY_CELL)) {
                        printLayoutToStdErr();
                        throw new IllegalArgumentException("Failed to add component " + getComponentIdentifier(pComponent) + ", cell [" + i + "][" + j + "] already assigned.");
                    }
                    assignedFields[i][j] = getComponentIdentifier(pComponent);
                }
            }
            layout.addComponent(pComponent, pColumn, pRow, pColumn + pWidth - 1, pRow + pHeight - 1);
            layout.setComponentAlignment(pComponent, componentAlignment);
            return this;
        }

        /**
         * Get the component identifier. This might be the debug Id, the caption
         * or the item's toString() result, depending on which of these values
         * is not null. The identifier is used for the debug output via
         * printLayout().
         *
         * @param The component
         *
         * @return The identifiert
         */
        private String getComponentIdentifier(Component pComponent) {
            //removed DebugId to support Vaadin 7
            String identifier = null;//pComponent.getId();
            if (identifier == null) {
                identifier = pComponent.getCaption();
                if (identifier == null) {
                    identifier = pComponent.toString();
                }
            }
            return identifier;
        }

        /**
         * Returns the final layout with all components
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayout getLayout() {
            return layout;
        }

        /**
         * Print the current layout to StdErr. This method is for internal use
         * only.
         */
        private void printLayoutToStdErr() {
            printLayout(System.err);
        }

        /**
         * Internal helper method to redirect to debug output to StdOut or
         * StdErr.
         */
        private GridLayoutBuilder printLayout(PrintStream pOut) {
            StringBuilder b = new StringBuilder();

            StringBuilder hr = new StringBuilder();

            for (int i = 0; i < columns * 10 + 4; i++) {
                hr.append("-");
            }
            hr.append("\n");

            b.append(hr.toString());

            for (int j = 0; j < rows; j++) {
                b.append("|");
                for (int i = 0; i < columns; i++) {
                    b.append(StringUtils.center(StringUtils.substring(assignedFields[i][j], 0, 10), 10)).append("|");
                }
                b.append("\n").append(hr.toString());
            }

            pOut.println(b.toString());
            return this;
        }

        /**
         * Print the current layout to StdOut. This method is intended to be
         * used for debugging purposes.
         *
         * @return GridLayoutBuilder.this
         */
        public GridLayoutBuilder printLayout() {
            return printLayout(System.out);
        }
    }

    /**
     * SAMPLE CODE
     */
//    public static void main(String[] args) {
//        GridLayout layout = new UIUtils7.GridLayoutBuilder(3, 5).addComponent(new Label("1"), 0, 0, 3, 1).addComponent(new Label("2"), 0, 1, 1, 1).
//                addComponent(new Label("3"), 2, 1, 1, 1).fillRow(new Label("4"), 0, 2, 1).fillRow(new Label("5"), 0, 3, 1).
//                addComponent(new Label("6"), 0, 4, 1, 1).addComponent(new Label("="), 1, 4, 1, 1).addComponent(new Label("7"), 2, 4, 1, 1).getLayout();
//
//        new UIUtils7.GridLayoutBuilder(layout).printLayout();
//    }
}
