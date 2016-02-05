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
package edu.kit.dama.ui.admin.workflow.property;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.DateField;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import edu.kit.dama.mdm.dataworkflow.properties.LinuxSoftwareMapProperty;
import edu.kit.dama.ui.admin.utils.CSSTokenContainer;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.Date;

/**
 *
 * @author jejkal
 */
public final class LinuxSoftwareMapPropertyConfigurationPanel extends AbstractPropertyConfigurationPanel<LinuxSoftwareMapProperty> {

    private GridLayout mainLayout;

    private TextField versionField;
    private DateField enteredDateField;
    private TextField keywordsField;
    private TextField authorField;
    private TextField maintainedByField;
    private TextField originalSiteField;
    private TextArea primarySiteField;
    private TextField alternateSiteField;
    private TextArea platformsArea;
    private ComboBox copyingPolicyField;

    /**
     * Default constructor.
     */
    public LinuxSoftwareMapPropertyConfigurationPanel() {
        getNameField().setInputPrompt("e.g. foobar-1.2.3.bin.tar.gz");
    }

    private DateField getEnteredDataField() {
        if (enteredDateField == null) {
            enteredDateField = new DateField("ENTERED DATE");
            enteredDateField.setSizeFull();
            enteredDateField.setWidth("100%");
            enteredDateField.setValue(new Date());
            enteredDateField.setImmediate(true);
            enteredDateField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        }
        return enteredDateField;
    }

    /**
     * Get the version field.
     */
    private TextField getVersionField() {
        if (versionField == null) {
            versionField = factoryTextField("VERSION", false);
            versionField.setInputPrompt("e.g. 1.2.3");
        }
        return versionField;
    }

    /**
     * Get the keywords field.
     */
    private TextField getKeywordsField() {
        if (keywordsField == null) {
            keywordsField = factoryTextField("KEYWORDS", false);
            keywordsField.setInputPrompt("e.g. foo bar tool");
        }
        return keywordsField;
    }

    /**
     * Get the author field.
     */
    private TextField getAuthorField() {
        if (authorField == null) {
            authorField = factoryTextField("AUTHOR", false);
            authorField.setInputPrompt("e.g. author.name@kit.edu (Author Name)");
        }
        return authorField;
    }

    /**
     * Get the maintainer field.
     */
    private TextField getMaintainedByField() {
        if (maintainedByField == null) {
            maintainedByField = factoryTextField("MAINTAINER", false);
            maintainedByField.setInputPrompt("e.g. maintainer.name@kit.edu (Maintainer Name)");
        }
        return maintainedByField;
    }

    /**
     * Get the original site field.
     */
    private TextField getOriginalSiteField() {
        if (originalSiteField == null) {
            originalSiteField = factoryTextField("ORIGINAL SITE", false);
            originalSiteField.setInputPrompt("e.g. ftp.kit.edu/downloads/foobar/");
        }
        return originalSiteField;
    }

    /**
     * Get the primary site field.
     */
    private TextArea getPrimarySiteArea() {
        if (primarySiteField == null) {
            primarySiteField = new TextArea("PRIMARY SITE");
            primarySiteField.setSizeFull();
            primarySiteField.setRows(5);
            primarySiteField.setImmediate(true);
            primarySiteField.setRequired(true);
            primarySiteField.setNullRepresentation("");
            primarySiteField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            primarySiteField.setInputPrompt("e.g. ftp.kit.edu/downloads/foobar/\n10kB foobar-1.2.3.bin.tar.gz\nfoobar-1.2.3.src.tar.gz");
        }
        return primarySiteField;
    }

    /**
     * Get the alternative site field.
     */
    private TextField getAlternateSiteField() {
        if (alternateSiteField == null) {
            alternateSiteField = factoryTextField("ALTERNATE SITE", false);
            alternateSiteField.setInputPrompt("e.g. ftp.othersite.edu/data/foobar/");
        }
        return alternateSiteField;
    }

    /**
     * Get the platforms field.
     */
    private TextArea getPlatformsArea() {
        if (platformsArea == null) {
            platformsArea = new TextArea("PLATFORMS");
            platformsArea.setWidth("100%");
            platformsArea.setRows(5);
            platformsArea.setImmediate(true);
            platformsArea.setNullRepresentation("");
            platformsArea.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            platformsArea.setInputPrompt("e.g. unusual software or hardware required");
        }
        return platformsArea;
    }

    /**
     * Get the copying policy field.
     */
    private ComboBox getCopyingPolicyField() {
        if (copyingPolicyField == null) {
            copyingPolicyField = new ComboBox("COPYING POLICY");
            copyingPolicyField.setWidth("100%");
            copyingPolicyField.setImmediate(true);
            copyingPolicyField.setNullSelectionAllowed(true);
            copyingPolicyField.setInputPrompt("Add Another...");
            copyingPolicyField.setTextInputAllowed(true);
            copyingPolicyField.setNewItemsAllowed(true);
            copyingPolicyField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
            //add standard licenses for linux software map (see http://www.ibiblio.org/pub/linux/LICENSES/theory.html)
            copyingPolicyField.addItem("PD");
            copyingPolicyField.addItem("Shareware");
            copyingPolicyField.addItem("BSD");
            copyingPolicyField.addItem("MIT");
            copyingPolicyField.addItem("Artistic License");
            copyingPolicyField.addItem("FRS Copyrighted");
            copyingPolicyField.addItem("GPL");
            copyingPolicyField.addItem("GPL 2.0");
            copyingPolicyField.addItem("GPL+LGPL");
            copyingPolicyField.addItem("W3C");
            copyingPolicyField.addItem("restricted");
        }
        return copyingPolicyField;
    }

    /**
     * Factory a standard text field that with the default look and behavior.
     *
     * @param pLabel The field label.
     * @param pRequired Set the field to be required.
     *
     * @return The text field.
     */
    public final TextField factoryTextField(String pLabel, boolean pRequired) {
        TextField result = new TextField(pLabel);
        result.setSizeFull();
        result.setWidth("100%");
        result.setImmediate(true);
        result.setRequired(pRequired);
        result.setNullRepresentation("");
        result.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        return result;
    }

    @Override
    public void resetCustomComponents() {
        getVersionField().setValue("");
        getEnteredDataField().setValue(new Date());
        getKeywordsField().setValue("");
        getAuthorField().setValue("");
        getMaintainedByField().setValue("");
        getOriginalSiteField().setValue("");
        getPrimarySiteArea().setValue("");
        getAlternateSiteField().setValue("");
        getPlatformsArea().setValue("");
        getCopyingPolicyField().setValue("");
    }

    @Override
    public boolean isValid() {
        //only the name field is relevant, everything else is optional
        return UIUtils7.validate(getNameField());
    }

    @Override
    public LinuxSoftwareMapProperty getPropertyInstance() {
        LinuxSoftwareMapProperty prop = new LinuxSoftwareMapProperty();
        prop.setTitle(getNameField().getValue());
        prop.setVersion(getVersionField().getValue());
        prop.setEnteredDate(getEnteredDataField().getValue().getTime());
        prop.setAuthor(getAuthorField().getValue());
        prop.setMaintainedBy(getMaintainedByField().getValue());
        prop.setKeywords(getKeywordsField().getValue());
        prop.setPlatforms(getPlatformsArea().getValue());
        prop.setCopyingPolicy((String) getCopyingPolicyField().getValue());
        prop.setPrimarySite(getPrimarySiteArea().getValue());
        prop.setOriginalSite(getOriginalSiteField().getValue());
        prop.setAlternateSite(getAlternateSiteField().getValue());
        return prop;
    }

    @Override
    public AbstractLayout getLayout() {
        if (mainLayout == null) {
            mainLayout = new UIUtils7.GridLayoutBuilder(3, 9).addComponent(getNameField(), 0, 0).addComponent(getVersionField(), 1, 0).addComponent(getEnteredDataField(), 2, 0).
                    addComponent(new HorizontalLayout(getAuthorField(), getMaintainedByField()), 0, 1, 3, 1).
                    addComponent(getKeywordsField(), 0, 2, 2, 1).addComponent(getCopyingPolicyField(), 2, 2).
                    addComponent(getPlatformsArea(), 0, 3, 3, 2).
                    addComponent(getPrimarySiteArea(), 0, 5, 3, 2).
                    addComponent(getOriginalSiteField(), 0, 7, 3, 1).
                    addComponent(getAlternateSiteField(), 0, 8, 3, 1).
                    getLayout();
            mainLayout.setSizeFull();
            mainLayout.setMargin(false);
            mainLayout.setSpacing(true);
        }
        return mainLayout;
    }

}
