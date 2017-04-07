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

import com.vaadin.server.UserError;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import edu.kit.dama.mdm.content.mets.BasicMetsExtractor;
import edu.kit.dama.mdm.content.mets.MetsMetadataExtractor;
import edu.kit.dama.ui.commons.util.UIUtils7;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jejkal
 */
public class BaseMetadataExtractionAndIndexingCreation extends WizardStep {

    private CheckBox createMetsExtractor;
    private Map<String, TextField> extractorProperties;

    public BaseMetadataExtractionAndIndexingCreation() {
        super();
    }

    public void buildMainLayout() {
        extractorProperties = new HashMap<>();
        Label information = new Label("In order to query for digital objects by metadata, relevant metadata must be extracted, processed and indexed at ingest time. "
                + "Typically, this process highly depends on the community ingesting data as metadata might be located next to the data, within the data or externally. "
                + "In order to be able to query at least for base metadata available for all digital objects, basic METS metadata extraction marked below should remain enabled. "
                + "It is highly recommended to keep the default settings and to modify them later according to special needs.<br/>"
                + "If you already have a custom metadata extractor in place, feel free to disable the checkbox.", ContentMode.HTML);

        createMetsExtractor = new CheckBox("Select to Enable basic METS metadata extraction for each ingest.");
        createMetsExtractor.setValue(true);

        createMetsExtractor.addValueChangeListener((event) -> {
            Set<Entry<String, TextField>> entries = extractorProperties.entrySet();
            entries.forEach((entry) -> {
                entry.getValue().setEnabled(createMetsExtractor.getValue());
            });
        });

        getMainLayout().addComponent(information);
        getMainLayout().addComponent(createMetsExtractor);
        getMainLayout().setComponentAlignment(information, Alignment.TOP_LEFT);
        getMainLayout().setComponentAlignment(createMetsExtractor, Alignment.TOP_LEFT);

        BasicMetsExtractor ext = new BasicMetsExtractor("");
        String[] internalKeys = ext.getInternalPropertyKeys();

        for (String internalKey : internalKeys) {
            TextField field = UIUtils7.factoryTextField(internalKey, null);
            field.setRequired(true);
            field.setDescription(ext.getInternalPropertyDescription(internalKey));
            if (MetsMetadataExtractor.COMMUNITY_DMD_SECTION_ID.equals(internalKey)) {
                field.setValue("cmd0");
            } else if (MetsMetadataExtractor.COMMUNITY_MD_TYPE_ID.equals(internalKey)) {
                field.setValue("DC");
            } else if (MetsMetadataExtractor.COMMUNITY_METADATA_SCHEMA_ID.equals(internalKey)) {
                field.setValue("oai_dc");
            } else {
                field.setValue("TRUE");
            }
            extractorProperties.put(internalKey, field);
            getMainLayout().addComponent(field);
            getMainLayout().setComponentAlignment(field, Alignment.TOP_LEFT);
        }
    }

    @Override
    public String getStepName() {
        return "Basic Metadata Extraction and Indexing";
    }

    @Override
    public boolean validateSettings() {
        if (createMetsExtractor.getValue()) {
            return UIUtils7.validate(getMainLayout());
        }
        return true;
    }

    @Override
    public String getSummary() {
        StringBuilder result = new StringBuilder();
        result.append(getStepName()).append("\n");
        result.append(StringUtils.rightPad("", 50, "_")).append("\n");

        if (createMetsExtractor.getValue()) {
            result.append("Create METS Extractor: ").append((createMetsExtractor.getValue()) ? "yes\n" : "no\n");
            Set<Entry<String, TextField>> entries = extractorProperties.entrySet();
            result.append("Properties:\n");
            entries.forEach((entry) -> {
                result.append(entry.getKey()).append(": ").append(entry.getValue().getValue()).append("\n");
            });
        } else {
            result.append("No metadata extractor will be created.\n");
        }

        return result.toString();
    }

    @Override
    public void collectProperties(Map<String, String> properties) {
        if (createMetsExtractor.getValue()) {
            properties.put(WizardPersistHelper.METADATA_CREATE_EXTRACTOR, "yes");
            Properties props = new Properties();
            Set<Entry<String, TextField>> entries = extractorProperties.entrySet();

            entries.forEach((entry) -> {
                props.put(entry.getKey(), entry.getValue().getValue());
            });
            try {
                properties.put(WizardPersistHelper.METADATA_EXTRACTOR_PROPERTIES, PropertiesUtil.propertiesToString(props));
            } catch (IOException ex) {
                UIUtils7.showError("Failed to collect properties. Disabling metadata extraction.");
                properties.remove(WizardPersistHelper.METADATA_CREATE_EXTRACTOR);
            }
        } else {
            properties.remove(WizardPersistHelper.METADATA_CREATE_EXTRACTOR);
        }
    }
}
