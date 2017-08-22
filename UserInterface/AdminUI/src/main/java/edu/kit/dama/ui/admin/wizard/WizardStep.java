/*
 * Copyright 2017 Karlsruhe Institute of Technology.
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
package edu.kit.dama.ui.admin.wizard;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import java.util.Map;

/**
 *
 * @author jejkal
 */
public abstract class WizardStep extends CustomComponent {

    private VerticalLayout mainLayout;

    public WizardStep() {
        mainLayout = new VerticalLayout();
        mainLayout.setSizeFull();
        mainLayout.setSpacing(true);
        mainLayout.setMargin(false);
        buildMainLayout();
        setCompositionRoot(mainLayout);
    }

    public final VerticalLayout getMainLayout() {
        return mainLayout;
    }

    public abstract void buildMainLayout();

    public abstract String getStepName();

    public abstract String getSummary();

    public boolean validate() {
        return validateSettings();
    }

    public abstract boolean validateSettings();

    public abstract void collectProperties(Map<String, String> properties);
}
