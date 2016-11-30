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

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.TextField;
import edu.kit.dama.scheduler.api.impl.QuartzIntervalTrigger;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.commons.util.UIUtils7;

/**
 *
 * @author jejkal
 */
public final class IntervalTriggerConfigurationPanel extends AbstractTriggerConfigurationPanel<QuartzIntervalTrigger> {

    private DateField startDateField;
    private DateField endDateField;
    private TextField timesField;
    private TextField periodField;

    /**
     * Default constructor.
     */
    public IntervalTriggerConfigurationPanel() {
        super();
    }

    @Override
    public void resetCustomComponents() {
        getStartDateField().setValue(null);
        getEndDateField().setValue(null);
        getPeriodField().setValue("");
        getTimesField().setValue("");
    }

    @Override
    public boolean customComponentsValid() {
        return UIUtils7.validate(getTimesField()) && UIUtils7.validate(getPeriodField());
    }

    @Override
    public QuartzIntervalTrigger getTriggerInstance() {
        QuartzIntervalTrigger result = new QuartzIntervalTrigger();
        result.setName(getNameField().getValue());
        result.setTriggerGroup(getGroupField().getValue());
        result.setDescription(getDescriptionField().getValue());
        result.setPriority((int) Math.rint(getPrioritySlider().getValue()));
        result.setStartDate(getStartDateField().getValue());
        result.setEndDate(getEndDateField().getValue());
        result.setPeriod(Long.parseLong(getPeriodField().getValue()) * 1000l);
        if (getTimesField().getValue() == null) {
            result.setTimes(-1);
        } else {
            result.setTimes(Integer.parseInt(getTimesField().getValue()));
        }
        return result;
    }

    @Override
    public AbstractLayout getLayout() {
        GridLayout layout = new UIUtils7.GridLayoutBuilder(2, 6).addComponent(getGroupField(), 0, 0).addComponent(getNameField(), 1, 0).
                addComponent(getPrioritySlider(), 0, 1, 2, 1).
                addComponent(getStartDateField(), 0, 2, 1, 1).addComponent(getEndDateField(), 1, 2, 1, 1).
                addComponent(getPeriodField(), 0, 3, 1, 1).addComponent(getTimesField(), 1, 3, 1, 1).
                addComponent(getDescriptionField(), 0, 4, 2, 2).getLayout();
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.setSizeFull();
        return layout;
    }

    /**
     * Get the start date field.
     *
     * @return The name field.
     */
    private DateField getStartDateField() {
        if (startDateField == null) {
            startDateField = new DateField("START DATE");
            startDateField.setWidth("100%");
            startDateField.setImmediate(true);
            startDateField.setDateFormat("dd.MM.yyyy HH:mm");
            startDateField.setResolution(Resolution.MINUTE);
            startDateField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return startDateField;
    }

    /**
     * Get the end date field.
     *
     * @return The name field.
     */
    private DateField getEndDateField() {
        if (endDateField == null) {
            endDateField = new DateField("END DATE");
            endDateField.setWidth("100%");
            endDateField.setImmediate(true);
            endDateField.setDateFormat("dd.MM.yyyy HH:mm");
            endDateField.setResolution(Resolution.MINUTE);
            endDateField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return endDateField;
    }

    /**
     * Get the end date field.
     *
     * @return The name field.
     */
    private TextField getPeriodField() {
        if (periodField == null) {
            periodField = new TextField("PERIOD IN SECONDS");
            periodField.setWidth("100%");
            periodField.setImmediate(true);
            periodField.addValidator(new AbstractStringValidator("Value must be >= 1.") {

                @Override
                protected boolean isValidValue(String value) {
                    try {
                        return Integer.parseInt(value) >= 1;
                    } catch (NumberFormatException ex) {
                        //no number
                    }
                    return false;
                }

            });
            periodField.setNullRepresentation("");
            periodField.setRequired(true);
            periodField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return periodField;
    }

    /**
     * Get the end date field.
     *
     * @return The name field.
     */
    private TextField getTimesField() {
        if (timesField == null) {
            timesField = new TextField("TIMES");
            timesField.setWidth("100%");
            timesField.setImmediate(true);
            timesField.addValidator(new AbstractStringValidator("Value must be > 0 or -1.") {

                @Override
                protected boolean isValidValue(String value) {
                    try {
                        int intValue = Integer.parseInt(value);
                        return intValue == -1 || intValue > 0;
                    } catch (NumberFormatException ex) {
                        //no number
                    }
                    return false;
                }

            });
            timesField.setNullRepresentation("");
            timesField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return timesField;
    }

}
