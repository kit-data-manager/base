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

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import java.util.Map;

/**
 *
 * @author jejkal
 */
public class IntroductionStep extends WizardStep {

    @Override
    public void buildMainLayout() {
        Label introduction = new Label("Welcome to your new repository system installation. This wizard will guide you through the initial setup of your system. "
                + "If you need more information about the underlying repository framework please visit the <a href='http://datamanager.kit.edu'>KIT Data Manager Homepage</a><br/>"
                + "This wizard is organized in multiple steps. You may switch between completed steps back and forth. If all information are collected, you can finish the wizard. "
                + "This is the moment where all collected data is written to the database. You will be redirected to the login page where you can login using the created administrator account or"
                + "you can create a user account for yourself and login with this account.", ContentMode.HTML);
        introduction.setSizeFull();
        getMainLayout().addComponent(introduction);
        getMainLayout().setComponentAlignment(introduction, Alignment.MIDDLE_CENTER);
    }

    @Override
    public String getStepName() {
        return "Introduction";
    }

    @Override
    public String getSummary() {
        return "";
    }

    @Override
    public boolean validateSettings() {
        return true;
    }

    @Override
    public void collectProperties(Map<String, String> properties) {
        //nothing to collect
    }

}
