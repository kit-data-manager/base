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

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import edu.kit.dama.scheduler.api.impl.QuartzNowTrigger;
import edu.kit.dama.ui.commons.util.UIUtils7;

/**
 *
 * @author jejkal
 */
public final class NowTriggerConfigurationPanel extends AbstractTriggerConfigurationPanel<QuartzNowTrigger> {

    /**
     * Default constructor.
     */
    public NowTriggerConfigurationPanel() {
        super();
    }

    @Override
    public void resetCustomComponents() {
    }

    @Override
    public boolean customComponentsValid() {
        return true;
    }

    @Override
    public QuartzNowTrigger getTriggerInstance() {
        QuartzNowTrigger result = new QuartzNowTrigger();
        result.setName(getNameField().getValue());
        result.setTriggerGroup(getGroupField().getValue());
        result.setDescription(getDescriptionField().getValue());
        result.setPriority((int) Math.rint(getPrioritySlider().getValue()));
        return result;
    }

    @Override
    public AbstractLayout getLayout() {
        GridLayout layout = new UIUtils7.GridLayoutBuilder(2, 5).addComponent(getGroupField(), 0, 0).addComponent(getNameField(), 1, 0).
                addComponent(getPrioritySlider(), 0, 1, 2, 1).
                addComponent(new Label("No configuration needed."), Alignment.MIDDLE_CENTER, 0, 2, 2, 1).
                addComponent(getDescriptionField(), 0, 3, 2, 2).getLayout();
        layout.setMargin(false);
        layout.setSpacing(true);
        layout.setSizeFull();
        return layout;
    }

}
