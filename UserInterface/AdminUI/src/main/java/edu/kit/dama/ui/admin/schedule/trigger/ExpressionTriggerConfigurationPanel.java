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
package edu.kit.dama.ui.admin.schedule.trigger;

import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import edu.kit.dama.scheduler.api.impl.QuartzExpressionTrigger;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.Arrays;

/**
 *
 * @author jejkal
 */
public final class ExpressionTriggerConfigurationPanel extends AbstractTriggerConfigurationPanel<QuartzExpressionTrigger> {

    private final String[] specialCharacters = new String[]{",", "-", "*", "/"};
    private final String[] domSpecialCharacters = new String[]{",", "-", "*", "/", "?", "L", "W"};
    private final String[] dowSpecialCharacters = new String[]{",", "-", "*", "/", "?", "L", "#"};

    private DateField startDateField;
    private DateField endDateField;
    private HorizontalLayout expressionLayout;
    private TextField secondsField;
    private TextField minutesField;
    private TextField hoursField;
    private TextField dayOfMonthField;
    private TextField monthField;
    private TextField dayOfWeekField;
    private TextField yearField;

    /**
     * Default constructor.
     */
    public ExpressionTriggerConfigurationPanel() {
        super();
    }

    @Override
    public void resetCustomComponents() {
        getStartDateField().setValue(null);
        getEndDateField().setValue(null);
        secondsField.setValue("*");
        minutesField.setValue("*");
        hoursField.setValue("*");
        dayOfMonthField.setValue("?");
        monthField.setValue("*");
        dayOfWeekField.setValue("*");
        yearField.setValue(null);
    }

    @Override
    public boolean customComponentsValid() {
        return UIUtils7.validate(expressionLayout);
    }

    @Override
    public QuartzExpressionTrigger getTriggerInstance() {
        QuartzExpressionTrigger result = new QuartzExpressionTrigger();
        result.setName(getNameField().getValue());
        result.setTriggerGroup(getGroupField().getValue());
        result.setDescription(getDescriptionField().getValue());
        result.setPriority((int) Math.rint(getPrioritySlider().getValue()));
        result.setStartDate(startDateField.getValue());
        result.setEndDate(endDateField.getValue());
        String expression = secondsField.getValue() + " "
                + minutesField.getValue() + " "
                + hoursField.getValue() + " "
                + dayOfMonthField.getValue() + " "
                + monthField.getValue() + " "
                + dayOfWeekField.getValue();
        if (yearField.getValue() != null) {
            expression += " " + yearField.getValue();
        }
        result.setExpression(expression);
        return result;
    }

    @Override
    public AbstractLayout getLayout() {
        GridLayout layout = new UIUtils7.GridLayoutBuilder(2, 6).addComponent(getGroupField(), 0, 0).addComponent(getNameField(), 1, 0).
                addComponent(getPrioritySlider(), 0, 1, 2, 1).
                addComponent(getStartDateField(), 0, 2, 1, 1).addComponent(getEndDateField(), 1, 2, 1, 1).
                addComponent(getExpressionLayout(), 0, 3, 2, 1).
                addComponent(getDescriptionField(), 0, 4, 2, 2).getLayout();
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.setSizeFull();
        return layout;
    }

    /**
     * Get the start date field.
     *
     * @return The start date field.
     */
    private DateField getStartDateField() {
        if (startDateField == null) {
            startDateField = new DateField("START DATE");
            startDateField.setImmediate(true);
            startDateField.setDateFormat("dd.MM.yyyy HH:mm");
            startDateField.setResolution(Resolution.MINUTE);
            startDateField.setWidth("100%");
            startDateField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return startDateField;
    }

    /**
     * Get the end date field.
     *
     * @return The end date field.
     */
    private DateField getEndDateField() {
        if (endDateField == null) {
            endDateField = new DateField("END DATE");
            endDateField.setImmediate(true);
            endDateField.setDateFormat("dd.MM.yyyy HH:mm");
            endDateField.setResolution(Resolution.MINUTE);
            endDateField.setWidth("100%");
            endDateField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return endDateField;
    }

    /**
     * Get the cron expression layout.
     *
     * @return The expression layout.
     */
    private HorizontalLayout getExpressionLayout() {
        if (expressionLayout == null) {
            secondsField = factoryTextField("s", "Seconds [0-59 , - * /]", "*", true, false);
            secondsField.addValidator(factoryValidator(0, 59, specialCharacters, false));
            minutesField = factoryTextField("m", "Minutes [0-59 , - * /]", "*", true, false);
            minutesField.addValidator(factoryValidator(0, 59, specialCharacters, false));
            hoursField = factoryTextField("H", "Hours [0-23 , - * /]", "*", true, false);
            hoursField.addValidator(factoryValidator(0, 23, specialCharacters, false));
            dayOfMonthField = factoryTextField("d", "Day of month [1-31 , - * ? / L W]", "?", true, false);
            dayOfMonthField.addValidator(factoryValidator(1, 31, domSpecialCharacters, false));
            monthField = factoryTextField("m", "Month [1-12 , - * /]", "*", true, false);
            monthField.addValidator(factoryValidator(1, 12, specialCharacters, false));
            dayOfWeekField = factoryTextField("E", "Day of week [1-7 , - * ? / L #]", "*", true, false);
            dayOfWeekField.addValidator(factoryValidator(1, 7, dowSpecialCharacters, false));
            yearField = factoryTextField("y", "Year [1970-2099 , - * /]", "*", false, true);
            yearField.addValidator(factoryValidator(1970, 2099, specialCharacters, true));
            expressionLayout = new HorizontalLayout(secondsField, minutesField, hoursField, dayOfMonthField, monthField, dayOfWeekField, yearField);
            expressionLayout.setMargin(false);
            expressionLayout.setSpacing(false);
            expressionLayout.setImmediate(true);
        }
        return expressionLayout;
    }

    /**
     * Factory a standard text field that with the default look and behavior.
     *
     * @param pLabel The field label.
     * @param pDescription The field description that may contain information
     * about allowed values.
     * @param pDefaultValue The default value.
     * @param pRequired Set the field to be required.
     * @param pNullAllowed TRUE = Null is a valid value.
     *
     * @return The text field.
     */
    private TextField factoryTextField(String pLabel, String pDescription, String pDefaultValue, boolean pRequired, boolean pNullAllowed) {
        TextField result = new TextField(pLabel);
        result.setDescription(pDescription);
        result.setWidth("40px");
        result.setImmediate(true);
        result.setRequired(pRequired);
        result.setValue(pDefaultValue);
        result.setNullSettingAllowed(pNullAllowed);
        if (pNullAllowed) {
            result.setNullRepresentation("");
        }
        result.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        return result;
    }

    /**
     * Factory a three-way validator allowing to validate for null values, an
     * integer range and a list of strings. If at least one validation criteria
     * is TRUE, the validator succeeds.
     *
     * @param pMin The min value of the integer range or Integer.MIN_VALUE is
     * null is provided.
     * @param pMax The max value of the integer range or Integer.MAX_VALUE is
     * null is provided.
     * @param pString An array of allowed string values.
     * @param pNullAllowed If TRUE, null values are interpreted as valid values.
     *
     * @return The validator.
     */
    private Validator factoryValidator(Integer pMin, Integer pMax, final String[] pStrings, final boolean pNullAllowed) {
        final int min = (pMin == null) ? Integer.MIN_VALUE : pMin;
        final int max = (pMax == null) ? Integer.MAX_VALUE : pMax;
        StringBuilder b = new StringBuilder();

        b.append("[").append(min).append("-").append(max);

        if (pStrings != null) {
            for (String s : pStrings) {
                b.append(" ").append(s);
            }
        }
        if (pNullAllowed) {
            b.append(" 'null'");
        }
        b.append("]");
        return new AbstractStringValidator("Values must be in the range " + b.toString()) {

            @Override
            protected boolean isValidValue(String value) {
                //first apply null check
                boolean firstLevelResult = pNullAllowed && (value == null || value.isEmpty());
                //if null check is not yet TRUE and pString is not null, check if value is in the list
                boolean secondLevelResult = firstLevelResult || (pStrings == null || Arrays.asList(pStrings).contains(value));
                try {
                    int fieldValue = Integer.parseInt(value);
                    return secondLevelResult || (fieldValue >= min && fieldValue < max);
                } catch (NumberFormatException ex) {
                    //obviously we have a string value
                    return secondLevelResult;
                }
            }
        };
    }

}
