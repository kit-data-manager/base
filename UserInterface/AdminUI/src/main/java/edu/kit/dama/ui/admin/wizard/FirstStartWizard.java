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

import com.vaadin.server.Page;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jejkal
 */
public class FirstStartWizard extends CustomComponent {

    private GridLayout mainLayout;
    private VerticalLayout stepLayout;
    private WizardStep[] stepList = new WizardStep[]{new IntroductionStep(), new AdministratorAccountCreation(), new AccessPointCreation(), new BaseMetadataExtractionAndIndexingCreation(), new WizardSummary()};

    private int currentStep = 0;
    private final Button next = new Button("Next");
    private final Button back = new Button("Back");

    private Map<String, String> properties = new HashMap<>();

    public FirstStartWizard() {
        buildMainLayout();
        setSizeFull();
        setCompositionRoot(mainLayout);
    }

    private void buildMainLayout() {
        stepLayout = new VerticalLayout();

        back.setEnabled(false);
        stepLayout.addComponent(stepList[currentStep]);
        stepLayout.setComponentAlignment(stepList[currentStep], Alignment.TOP_RIGHT);
        stepLayout.setSpacing(false);
        stepLayout.setMargin(false);
        stepLayout.setWidth("100%");
        stepLayout.setHeight("500px");

        final VerticalLayout stepLabels = new VerticalLayout();
        for (WizardStep step : stepList) {
            Label stepLabel = new Label(step.getStepName());
            stepLabel.setWidth("250px");
            stepLabels.addComponent(stepLabel);
            stepLabels.setComponentAlignment(stepLabel, Alignment.TOP_LEFT);
        }

        //make introduction label bold
        stepLabels.getComponent(0).addStyleName("myboldcaption");

        Label spacer = new Label();
        stepLabels.addComponent(spacer);
        stepLabels.setExpandRatio(spacer, 1.0f);
        stepLabels.setWidth("275px");
        stepLabels.setHeight("550px");
        stepLabels.setSpacing(true);

        UIUtils7.GridLayoutBuilder builder = new UIUtils7.GridLayoutBuilder(2, 2);

        HorizontalLayout buttonLayout = new HorizontalLayout(back, next);
        buttonLayout.setSizeFull();
        buttonLayout.setComponentAlignment(back, Alignment.BOTTOM_RIGHT);
        buttonLayout.setComponentAlignment(next, Alignment.BOTTOM_RIGHT);
        buttonLayout.setExpandRatio(back, 1.0f);

        next.addClickListener((event) -> {
            if ("Go To Login".equals(next.getCaption())) {
                Page.getCurrent().reload();
            } else if ("Finish".equals(next.getCaption())) {
                //do finish
                WizardPersistHelper helper = new WizardPersistHelper();
                if (helper.persist(properties)) {
                    UIUtils7.showInformation("Success", "All information have been successfully stored into the database. For details, please refer to the log output above.\n"
                            + "You may now dismiss this message and click 'Go To Login' in order to access the login page.\n"
                            + "From there you can to login using your administrator account or create a personalized user account.", 3000);
                    back.setVisible(false);
                    next.setCaption("Go To Login");
                } else {
                    UIUtils7.showError("Failed to store collected information in database.\n"
                            + "Please refer to the log output above.");
                }
                ((WizardSummary) stepList[currentStep]).setSummary(helper.getMessages());
            } else {
                if (currentStep + 1 <= stepList.length - 1) {
                    if (stepList[currentStep].validate()) {
                        stepList[currentStep].collectProperties(properties);
                        currentStep++;
                        stepLayout.replaceComponent(stepList[currentStep - 1], stepList[currentStep]);
                        Label currentLabel = (Label) stepLabels.getComponent(currentStep);
                        Label prevLabel = (Label) stepLabels.getComponent(currentStep - 1);
                        currentLabel.addStyleName("myboldcaption");
                        prevLabel.removeStyleName("myboldcaption");

                        if (stepList[currentStep] instanceof WizardSummary) {
                            StringBuilder summary = new StringBuilder();
                            for (WizardStep step : stepList) {
                                summary.append(step.getSummary()).append("\n");
                            }
                            ((WizardSummary) stepList[currentStep]).setSummary(summary.toString());
                        }
                    }
                }

                if (currentStep == stepList.length - 1) {
                    //finish
                    next.setCaption("Finish");
                } else {
                    next.setCaption("Next");
                }

                back.setEnabled(true);
            }
        });

        back.addClickListener((event) -> {
            if (currentStep - 1 >= 0) {
                stepList[currentStep].collectProperties(properties);
                currentStep--;
                stepLayout.replaceComponent(stepList[currentStep + 1], stepList[currentStep]);
                Label currentLabel = (Label) stepLabels.getComponent(currentStep);
                Label prevLabel = (Label) stepLabels.getComponent(currentStep + 1);
                currentLabel.addStyleName("myboldcaption");
                prevLabel.removeStyleName("myboldcaption");
            }
            next.setEnabled(true);
            back.setEnabled(currentStep > 0);
            next.setCaption("Next");
        });

        builder.addComponent(stepLabels, Alignment.TOP_LEFT, 0, 0, 1, 2);
        builder.addComponent(stepLayout, Alignment.TOP_LEFT, 1, 0, 1, 1);
        builder.addComponent(buttonLayout, Alignment.BOTTOM_LEFT, 1, 1, 1, 1);

        mainLayout = builder.getLayout();
        mainLayout.setMargin(true);
        mainLayout.setSizeFull();

        // mainLayout.setColumnExpandRatio(0, .3f);
        mainLayout.setColumnExpandRatio(1, 1f);
        mainLayout.setRowExpandRatio(0, .95f);
        mainLayout.setRowExpandRatio(1, .05f);
    }

}
