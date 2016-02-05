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
package edu.kit.dama.ui.admin;

import com.vaadin.event.LayoutEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.ui.commons.util.UIUtils7;

/**
 *
 * @author mf6319
 */
public class MainControlPanel extends CustomComponent {

    private VerticalLayout mainLayout;
    private VerticalLayout infoCell;
    private VerticalLayout profileCell;
    private VerticalLayout administrationCell;
    private VerticalLayout logoutCell;
    private final AdminUIMainView parent;

    public MainControlPanel(AdminUIMainView pParent) {
        parent = pParent;
        buildMainLayout();
        setCompositionRoot(mainLayout);
        setSizeFull();
    }

    /**
     * Create a new cell for the UI. Each cell contains an image located in the
     * provided resource and a help label for the cell adverse to it.
     * Furthermore, a style is provided which can be 'help-left' or 'help-right'
     * depending on which side the help text should be aligned (help in left col
     * -> align right).
     *
     * @param pResourceString The image resource string.
     * @param The alignment of the image (TOP_LEFT or TOP_RIGHT, depending on
     * the column)
     * @param cellNumber The cell number (0-3) counting from top left to bottom
     * right
     * @param pHelp The help string which may contain HTML tags.
     * @param pStyle The help label style ('help-left' or 'help-right').
     *
     * @return The cell layout.
     */
    private VerticalLayout createCell(String pResourceString, Alignment pAlignment, int cellNumber, String pHelp, String pStyle) {
        final String cellHeight = "132px";
        //create the cell image
        Image cellImage = new Image(null, new ThemeResource(pResourceString));
        cellImage.addStyleName("border");

        //create the cell image wrapper, which provides the shadow and this show/hide functionality
        VerticalLayout imageWrapper = new VerticalLayout(cellImage);
        imageWrapper.addComponent(cellImage);
        imageWrapper.setComponentAlignment(cellImage, Alignment.MIDDLE_CENTER);
        imageWrapper.setWidth(cellHeight);
        imageWrapper.setHeight(cellHeight);
        imageWrapper.addStyleName("shadow");
        imageWrapper.addStyleName("visible");

        //help label for the cell adverse to the current cell
        Label oppositeCellHelp = new Label(pHelp, ContentMode.HTML);
        oppositeCellHelp.addStyleName(pStyle);
        oppositeCellHelp.setSizeFull();
        oppositeCellHelp.addStyleName("invisible");
        oppositeCellHelp.setHeight(cellHeight);

        //the cell layout containing image and help label
        VerticalLayout cell = new VerticalLayout();
        cell.addComponent(imageWrapper);
        cell.setComponentAlignment(imageWrapper, pAlignment);
        cell.setMargin(true);
        cell.addComponent(oppositeCellHelp);
        cell.setComponentAlignment(oppositeCellHelp, Alignment.MIDDLE_CENTER);

        //define component ids depending on the provided cell number
        //---------
        //| 0 | 1 |
        //| 2 | 3 |
        //---------
        //Each cell gets the id 'image<cellNumber>'
        //The currently created wrapper and help label are getting the cellId of the adverse cell (0 -> 1, 1 -> 0, 2 -> 3, 3 -> 2).
        //These ids are used then by edu.kit.dama.ui.admin.client.HelpConnector to show/hide elements on mouse over.
        switch (cellNumber) {
            case 0:
                cellImage.setId("image0");
                //this cell contains the help for cell 1
                imageWrapper.setId("image1_wrapper");
                oppositeCellHelp.setId("image1_help");
                break;
            case 1:
                cellImage.setId("image1");
                //this cell contains the help for cell 0
                imageWrapper.setId("image0_wrapper");
                oppositeCellHelp.setId("image0_help");
                break;
            case 2:
                cellImage.setId("image2");
                //this cell contains the help for cell 3
                imageWrapper.setId("image3_wrapper");
                oppositeCellHelp.setId("image3_help");
                break;
            case 3:
                cellImage.setId("image3");
                //this cell contains the help for cell 2
                imageWrapper.setId("image2_wrapper");
                oppositeCellHelp.setId("image2_help");
                break;
        }
        //link the HelpExtension to the image
        new HelpExtension().extend(cellImage);
        return cell;
    }

    /**
     * Build the main layout.
     */
    private void buildMainLayout() {
        infoCell = createCell("img/128x128/information2.png", Alignment.TOP_RIGHT, 0, "<p>Show/edit your profile and personel settings.</p>", "help-right");
        profileCell = createCell("img/128x128/preferences.png", Alignment.TOP_LEFT, 1, "<p>Get information on how you use this KIT Data Manager instance.</p>", "help-left");
        administrationCell = createCell("img/128x128/gears_preferences.png", Alignment.BOTTOM_RIGHT, 2, "<p>Logout.</p>", "help-right");
        logoutCell = createCell("img/128x128/exit.png", Alignment.BOTTOM_LEFT, 3, "<p>Show KIT Data Manager settings.<br/>This view is only available for administrators and group manager</p>.", "help-left");

        GridLayout actionCellLayout = new UIUtils7.GridLayoutBuilder(2, 2).
                addComponent(infoCell, Alignment.BOTTOM_RIGHT, 0, 0, 1, 1).
                addComponent(profileCell, Alignment.BOTTOM_LEFT, 1, 0, 1, 1).
                addComponent(administrationCell, Alignment.TOP_RIGHT, 0, 1, 1, 1).addComponent(logoutCell, Alignment.TOP_LEFT, 1, 1, 1, 1).getLayout();
        actionCellLayout.setSpacing(true);
        actionCellLayout.setMargin(true);
        actionCellLayout.setSizeFull();

        actionCellLayout.addLayoutClickListener(new LayoutEvents.LayoutClickListener() {

            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                if (event.getClickedComponent() instanceof Image) {
                    // Check if childComponent is disabled
                    if (!event.getChildComponent().isEnabled()) {
                        return;
                    }
                    Image img = (Image) event.getClickedComponent();
                    if ("image0".equals(img.getId())) {
                        parent.updateView(AdminUIMainView.VIEW.INFORMATION);
                    } else if ("image1".equals(img.getId())) {
                        parent.updateView(AdminUIMainView.VIEW.PROFILE);
                    } else if ("image2".equals(img.getId())) {
                        parent.updateView(AdminUIMainView.VIEW.SETTINGS);
                    } else if ("image3".equals(img.getId())) {
                        parent.logout();
                    }
                }
            }
        });

        mainLayout = new VerticalLayout(actionCellLayout);
        mainLayout.setSizeFull();
    }

    /**
     *
     * @return
     */
    public VerticalLayout getAdministrationCell() {
        return administrationCell;
    }
}
