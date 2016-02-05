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

import com.vaadin.ui.ComboBox;
import edu.kit.dama.ui.admin.AdminUIMainView;
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
public class StagingProcessorBasePropertiesLayout extends AbstractBasePropertiesLayout<StagingProcessor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingProcessorBasePropertiesLayout.class);
    private static final String DEBUG_ID_PREFIX = StagingProcessorBasePropertiesLayout.class.getName() + "_";

    private ComboBox processorTypeBox;

    public StagingProcessorBasePropertiesLayout(AdminUIMainView parentApp) {
        super(parentApp);

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");

        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setWidth("100%");
        setImmediate(true);
        setMargin(true);
        setSpacing(true);

        setColumns(3);
        setRows(4);

        addComponent(getNameField(), 0, 0, 2, 0);
        addComponent(getGroupBox(), 0, 1);
        addComponent(getProcessorTypeBox(), 0, 2);
        addComponent(getCheckBoxesLayout(), 2, 1, 2, 2);
        addComponent(getDescriptionArea(), 0, 3, 2, 3);

        setColumnExpandRatio(0, 0.7f);
        setColumnExpandRatio(1, 0.01f);
        setColumnExpandRatio(2, 0.29f);
    }

    /**
     * @return the processorTypeBox
     */
    public final ComboBox getProcessorTypeBox() {
        if (processorTypeBox == null) {
            String id = "processorTypeLayout";
            LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

            processorTypeBox = new ComboBox("PROCESSOR TYPE");
            processorTypeBox.setId(DEBUG_ID_PREFIX + id);
            processorTypeBox.setWidth("100%");
            processorTypeBox.setImmediate(true);
            processorTypeBox.setNullSelectionAllowed(false);
            processorTypeBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            for (StagingProcessor.PROCESSOR_TYPE value : StagingProcessor.PROCESSOR_TYPE.values()) {
                processorTypeBox.addItem(value);
            }

            processorTypeBox.setNullSelectionItemId(StagingProcessor.PROCESSOR_TYPE.SERVER_SIDE_ONLY);
        }
        return processorTypeBox;
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
        if (processor.getGroupId() == null) {
            getProcessorTypeBox().setValue(StagingProcessor.PROCESSOR_TYPE.CLIENT_AND_SERVER_SIDE);
        } else {
            getProcessorTypeBox().setValue(processor.getType());
        }
        getNameField().setValue(processor.getName());
        getDefaultBox().setValue(processor.isDefaultOn());
        getDisabledBox().setValue(processor.isDisabled());
        getDescriptionArea().setValue(processor.getDescription());
    }

    @Override
    public void reset() {
        setEnabled(true);
        getGroupBox().select(USERS_GROUP_ID);
        getProcessorTypeBox().select(StagingProcessor.PROCESSOR_TYPE.SERVER_SIDE_ONLY);
        getNameField().setValue(null);
        getDefaultBox().setValue(false);
        getDisabledBox().setValue(false);
        getDescriptionArea().setValue(null);
    }

    @Override
    public String getNameFieldLabel() {
        return "PROCESSOR NAME";
    }
}
