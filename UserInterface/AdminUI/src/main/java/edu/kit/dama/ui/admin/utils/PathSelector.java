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
package edu.kit.dama.ui.admin.utils;

import com.vaadin.data.Property;
import com.vaadin.data.util.FilesystemContainer;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.BaseTheme;
import edu.kit.dama.ui.admin.exception.MsgBuilder;
import edu.kit.dama.ui.admin.exception.NoteBuilder;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class PathSelector extends Window {

    private final static Logger LOGGER = LoggerFactory.getLogger(PathSelector.class);
    public final static String DEBUG_ID_PREFIX = PathSelector.class.getName() + "_";

    private final IPathSelector iPathSelector;
    private GridLayout mainLayout;
    private ComboBox rootBox;
    private TreeTable treeTable;
    private TextField pathField;
    private NativeButton selectButton;

    public PathSelector(final IPathSelector iPathSelector) {
        this.iPathSelector = iPathSelector;

        LOGGER.debug("Building " + DEBUG_ID_PREFIX + " ...");
        setCaption("Path Selector");
        setId(DEBUG_ID_PREFIX.substring(0, DEBUG_ID_PREFIX.length() - 1));
        setWidth("500px");
        setHeight("600px");
        setImmediate(true);
        center();
        setModal(true);

        setContent(getMainLayout());
    }

    /**
     * @return the mainLayout
     */
    public final GridLayout getMainLayout() {
        if (mainLayout == null) {
            buildMainLayout();
        }
        return mainLayout;
    }

    /**
     *
     */
    private void buildMainLayout() {
        String id = "mainLayout";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        mainLayout = new GridLayout(3, 3);
        mainLayout.setImmediate(true);
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        mainLayout.setSpacing(true);

        mainLayout.addComponent(getRootBox(), 0, 0);
        mainLayout.addComponent(getTreeTable(), 0, 1, 2, 1);
        mainLayout.addComponent(getPathField(), 0, 2, 1, 2);
        mainLayout.addComponent(getSelectButton(), 2, 2);

        mainLayout.setComponentAlignment(getPathField(), Alignment.BOTTOM_LEFT);
        mainLayout.setComponentAlignment(getSelectButton(), Alignment.BOTTOM_RIGHT);

        mainLayout.setColumnExpandRatio(0, 0.4f);
        mainLayout.setColumnExpandRatio(1, 0.39f);
        mainLayout.setColumnExpandRatio(2, 0.01f);

        mainLayout.setRowExpandRatio(0, 0.01f);
        mainLayout.setRowExpandRatio(1, 0.9f);
        mainLayout.setRowExpandRatio(2, 0.01f);
    }

    /**
     * @return the rootLister
     */
    public ComboBox getRootBox() {
        if (rootBox == null) {
            buildRootBox();
        }
        return rootBox;
    }

    /**
     *
     */
    private void buildRootBox() {
        String id = "rootBox";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        rootBox = new ComboBox("ROOT");
        rootBox.setId(DEBUG_ID_PREFIX + id);
        rootBox.setWidth("100%");
        rootBox.setImmediate(true);
        rootBox.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        rootBox.setNullSelectionAllowed(false);

        rootBox.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                String root = (String) event.getProperty().getValue();
                if (root == null || root.trim().isEmpty()) {
                    String warning = "Invalid root selection!";
                    LOGGER.warn(warning);
                    UIComponentTools.showWarning("WARNING", warning, 5);
                    return;
                }
                FilesystemContainer container = new FilesystemContainer(new File(root), false);
                getTreeTable().setContainerDataSource(container);
                getTreeTable().setItemIconPropertyId("Icon");
                getTreeTable().setVisibleColumns(new Object[]{"Name", "Size", "Last Modified"});
                getPathField().setValue(root);
            }
        });

        reloadRootBox();
        rootBox.select(rootBox.getItemIds().toArray()[0]);
    }

    /**
     *
     */
    private void reloadRootBox() {
        File[] roots = File.listRoots();
        if (roots == null || roots.length == 0) {
            UIComponentTools.setLockedLayoutComponents(getMainLayout(), true);
            UIComponentTools.showWarning("WARNING", NoteBuilder.containerInitializationFailed(getRootBox().getId()), 5);
            LOGGER.warn(MsgBuilder.containerInitializationFailed(getRootBox().getId()));
            return;
        }
        for (File root : File.listRoots()) {
            rootBox.addItem(root.getAbsolutePath());
        }
    }

    /**
     * @return the treeTable
     */
    public TreeTable getTreeTable() {
        if (treeTable == null) {
            buildTreeTable();
        }
        return treeTable;
    }

    /**
     *
     */
    private void buildTreeTable() {
        String id = "treeTable";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        treeTable = new TreeTable();
        treeTable.setId(DEBUG_ID_PREFIX + id);
        treeTable.setCaption("FILE SYSTEM");
        treeTable.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        treeTable.setSizeFull();
        treeTable.setImmediate(true);
        treeTable.setSelectable(true);

        treeTable.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                File selection = (File) event.getProperty().getValue();
                if (selection == null || !selection.exists()) {
                    String warning = "Invalid path selection!";
                    LOGGER.warn(warning);
                    UIComponentTools.showWarning("WARNING", warning, 5);
                    return;
                }
                String selectedDir = selection.getAbsolutePath();
                if (selection.isFile()) {
                    selectedDir = selection.getAbsolutePath();
                }
                getPathField().setValue(selectedDir);
            }
        });
    }

    /**
     * @return the pathField
     */
    public TextField getPathField() {
        if (pathField == null) {
            buildPathField();
        }
        return pathField;
    }

    /**
     *
     */
    private void buildPathField() {
        String id = "pathField";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        pathField = new TextField("SELECTED PATH");
        pathField.setId(DEBUG_ID_PREFIX + id);
        pathField.addStyleName(CSSTokenContainer.BOLD_CAPTION);
        pathField.setImmediate(true);
        pathField.setNullRepresentation("");
        pathField.setWidth("100%");
    }

    /**
     * @return the selectButton
     */
    public NativeButton getSelectButton() {
        if (selectButton == null) {
            buildSelectButton();
        }
        return selectButton;
    }

    /**
     *
     */
    private void buildSelectButton() {
        String id = "selectButton";
        LOGGER.debug("Building " + DEBUG_ID_PREFIX + id + " ...");

        selectButton = new NativeButton();
        selectButton.setId(DEBUG_ID_PREFIX + id);
        selectButton.setImmediate(true);
        selectButton.setIcon(new ThemeResource(IconContainer.FOLDER_OK));
        selectButton.setStyleName(BaseTheme.BUTTON_LINK);

        selectButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                String path = (String) getPathField().getValue();
                if (path != null) {
                    path = path.trim();
                }
                iPathSelector.firePathSelectorCloseEvent(path);
                close();
            }
        });
    }
}
