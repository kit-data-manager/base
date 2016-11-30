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
package edu.kit.dama.ui.admin.staging.processors;

import com.vaadin.shared.ui.slider.SliderOrientation;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Slider;
import edu.kit.dama.ui.admin.exception.UIComponentUpdateException;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.ui.admin.AbstractBasePropertiesLayout;
import static edu.kit.dama.util.Constants.USERS_GROUP_ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public final class StagingProcessorBasePropertiesLayout extends AbstractBasePropertiesLayout<StagingProcessor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingProcessorBasePropertiesLayout.class);
    private static final String DEBUG_ID_PREFIX = StagingProcessorBasePropertiesLayout.class.getName() + "_";

    private CheckBox ingestSupportedBox;
    private CheckBox downloadSupportedBox;
    private Slider prioritySlider;
    private HorizontalLayout sliderLayout;

    public StagingProcessorBasePropertiesLayout() {
        super();

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        setColumns(3);
        setRows(4);

        /////////check layout and relocate
        addComponent(getNameField(), 0, 0, 2, 0);
        addComponent(getGroupBox(), 0, 1, 1, 1);
        Label minus = new Label("-");
        minus.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        Label plus = new Label("+");
        plus.addStyleName(CSSTokenContainer.BOLD_CAPTION);

        sliderLayout = new HorizontalLayout(minus, getPrioritySlider(), plus);
        sliderLayout.setComponentAlignment(minus, Alignment.BOTTOM_RIGHT);
        sliderLayout.setComponentAlignment(plus, Alignment.BOTTOM_LEFT);
        sliderLayout.setExpandRatio(getPrioritySlider(), .96f);
        sliderLayout.setExpandRatio(minus, .02f);
        sliderLayout.setExpandRatio(plus, .02f);
        sliderLayout.setWidth("100%");
        addComponent(sliderLayout, 0, 2, 1, 2);

        getCheckBoxesLayout().addComponent(getIngestProcessingSupportedBox());
        getCheckBoxesLayout().addComponent(getDownloadProcessingSupportedBox());

        addComponent(getCheckBoxesLayout(), 2, 1, 2, 2);
        addComponent(getDescriptionArea(), 0, 3, 2, 3);

        setColumnExpandRatio(0, .79f);
        setColumnExpandRatio(1, 0.01f);
        setColumnExpandRatio(2, 0.2f);
        setRowExpandRatio(3, 1f);
    }

    /**
     * @return the processorTypeBox
     */
    public final Slider getPrioritySlider() {
        if (prioritySlider == null) {
            String id = "prioritySlider";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            prioritySlider = factoryPrioritySlider();
        }
        return prioritySlider;
    }

    private Slider factoryPrioritySlider() {
        Slider slider = new Slider("PRIORITY", 0, 10);
        slider.setDescription("The priority defined the execution order of staging processors. A higher priority means an earlier execution.");
        slider.setOrientation(SliderOrientation.HORIZONTAL);
        slider.setImmediate(true);
        slider.setWidth("100%");
        slider.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        return slider;
    }

    /**
     * Returns the checkbox for getting the 'ingestSupported' flag of an
     * element.
     *
     * @return The 'ingestSupported' box
     */
    public final CheckBox getIngestProcessingSupportedBox() {
        if (ingestSupportedBox == null) {
            String id = "ingestProcessingSupportedBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            ingestSupportedBox = new CheckBox("Ingest Supported");
            ingestSupportedBox.setId(DEBUG_ID_PREFIX + id);
            ingestSupportedBox.addStyleName("yesno");
            ingestSupportedBox.setDescription("Set this processor to be applicable to ingests.");
            ingestSupportedBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return ingestSupportedBox;
    }

    /**
     * Returns the checkbox for getting the 'downloadSupported' flag of an
     * element.
     *
     * @return The 'downloadSupported' box
     */
    public final CheckBox getDownloadProcessingSupportedBox() {
        if (downloadSupportedBox == null) {
            String id = "downloadProcessingSupportedBox";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            downloadSupportedBox = new CheckBox("Download Supported");
            downloadSupportedBox.setId(DEBUG_ID_PREFIX + id);
            downloadSupportedBox.addStyleName("yesno");
            ingestSupportedBox.setDescription("Set this processor to be applicable to downloads.");
            downloadSupportedBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return downloadSupportedBox;
    }

    @Override
    public void updateSelection(StagingProcessor processor) throws UIComponentUpdateException {
        reset();

        if (processor == null) {
            throw new UIComponentUpdateException("Invalid processor.");
        }
        if (processor.getGroupId() == null) {
            getGroupBox().select(USERS_GROUP_ID);
        } else {
            getGroupBox().select(processor.getGroupId());
        }

        getPrioritySlider().setValue((double) Byte.toUnsignedInt(processor.getPriority()));

        getNameField().setValue(processor.getName());
        getDefaultBox().setValue(processor.isDefaultOn());
        getDisabledBox().setValue(processor.isDisabled());
        getDescriptionArea().setValue(processor.getDescription());
        getIngestProcessingSupportedBox().setValue(processor.isIngestProcessingSupported());
        getDownloadProcessingSupportedBox().setValue(processor.isDownloadProcessingSupported());
    }

    @Override
    public void reset() {
        setEnabled(true);
        getGroupBox().select(USERS_GROUP_ID);
        Slider newSlider = factoryPrioritySlider();
        sliderLayout.replaceComponent(getPrioritySlider(), newSlider);
        prioritySlider = newSlider;
        getNameField().setValue(null);
        getDefaultBox().setValue(false);
        getDisabledBox().setValue(false);
        getDescriptionArea().setValue(null);
        getIngestProcessingSupportedBox().setValue(false);
        getDownloadProcessingSupportedBox().setValue(false);
    }

    @Override
    public String getNameFieldLabel() {
        return "PROCESSOR NAME";
    }
}
