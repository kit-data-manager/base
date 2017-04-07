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

import com.vaadin.ui.TextArea;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.Map;

/**
 *
 * @author jejkal
 */
public class WizardSummary extends WizardStep {

    private TextArea summaryArea;

    public WizardSummary() {
        super();
    }

    @Override
    public void buildMainLayout() {
        summaryArea = UIUtils7.factoryTextArea("Summary", null);
        summaryArea.setMaxLength(-1);
        summaryArea.setSizeFull();
        summaryArea.setRows(30);
        getMainLayout().addComponent(summaryArea);
    }

    public void setSummary(String summary) {
        summaryArea.setValue(summary);
    }

    @Override
    public String getStepName() {
        return "Summary";
    }

    @Override
    public boolean validateSettings() {
        return true;
    }

    @Override
    public String getSummary() {
        return "";
    }

    @Override
    public void collectProperties(Map<String, String> properties) {
        //nothing to do here
    }
}
