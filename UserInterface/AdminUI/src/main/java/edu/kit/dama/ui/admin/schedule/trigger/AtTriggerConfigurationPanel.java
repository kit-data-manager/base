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

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import edu.kit.dama.scheduler.api.impl.QuartzAtTrigger;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.Calendar;

/**
 *
 * @author jejkal
 */
public final class AtTriggerConfigurationPanel extends AbstractTriggerConfigurationPanel<QuartzAtTrigger> {

    private DateField atDateField;

    /**
     * Default constructor.
     */
    public AtTriggerConfigurationPanel() {
        super();
    }

    @Override
    public void resetCustomComponents() {
        atDateField.setValue(null);
    }

    @Override
    public boolean customComponentsValid() {
        return UIUtils7.validate(atDateField);
    }

    @Override
    public QuartzAtTrigger getTriggerInstance() {
        QuartzAtTrigger result = new QuartzAtTrigger();
        result.setName(getNameField().getValue());
        result.setTriggerGroup(getGroupField().getValue());
        result.setDescription(getDescriptionField().getValue());
        result.setPriority((int) Math.rint(getPrioritySlider().getValue()));
        result.setStartDate(atDateField.getValue());
        return result;
    }

    @Override
    public AbstractLayout getLayout() {
        GridLayout layout = new UIUtils7.GridLayoutBuilder(2, 5).addComponent(getGroupField(), 0, 0).addComponent(getNameField(), 1, 0).
                addComponent(getPrioritySlider(), 0, 1, 2, 1).
                addComponent(getAtDateField(), 0, 2, 2, 1).
                addComponent(getDescriptionField(), 0, 3, 2, 2).getLayout();
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.setSizeFull();
        return layout;
    }

    /**
     * Get the at date field.
     *
     * @return The at date field.
     */
    private DateField getAtDateField() {
        if (atDateField == null) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR, 1);
            atDateField = new DateField("SCHEDULE AT", cal.getTime());
            atDateField.setDateFormat("dd.MM.yyyy HH:mm");
            atDateField.setResolution(Resolution.MINUTE);
            atDateField.setImmediate(true);
            atDateField.setRequired(true);
            atDateField.setWidth("100%");
            atDateField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return atDateField;
    }

}
