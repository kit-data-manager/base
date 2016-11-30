/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.ui.simon.panel;

import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import edu.kit.dama.ui.simon.impl.AbstractProbe;
import static edu.kit.dama.ui.simon.impl.AbstractProbe.PROBE_STATUS.FAILED;
import static edu.kit.dama.ui.simon.impl.AbstractProbe.PROBE_STATUS.SUCCEEDED;
import static edu.kit.dama.ui.simon.impl.AbstractProbe.PROBE_STATUS.UNAVAILABLE;
import edu.kit.dama.ui.simon.util.SimonConfigurator;
import edu.kit.dama.ui.commons.util.UIUtils7;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class SimonMainPanel extends CustomComponent implements Button.ClickListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SimonMainPanel.class);
    private VerticalLayout mainLayout;
    private TabSheet tabSheet;
    private final Map<String, List<AbstractProbe>> probesByCategory;
    private final List<Tab> tabs = new LinkedList<>();
    private final Map<String, Component> tabsByCategory;
    private Button updateButton;

    /**
     * Default constructor.
     */
    public SimonMainPanel() {
        probesByCategory = new HashMap<>();
        tabsByCategory = new HashMap<>();
        buildMainLayout();
        setCompositionRoot(mainLayout);
        setSizeFull();
    }

    /**
     * Build the main layout including the update of all probes.
     */
    private void buildMainLayout() {
        tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        mainLayout = new VerticalLayout(tabSheet);
        mainLayout.setSizeFull();
        mainLayout.setMargin(true);
        reload();
    }

    /**
     * Reload the view and refresh the status of all probes.
     */
    private void reload() {
        LOGGER.debug("Reloading main panel");

        //remove all tabs and clear all categories
        for (Tab tab : tabs) {
            tabSheet.removeTab(tab);
            probesByCategory.clear();
            tabsByCategory.clear();
        }

        //get all probes
        AbstractProbe[] probes = SimonConfigurator.getSingleton().getProbes();
        LOGGER.debug(" - Obtaining categories for {} probes", probes.length);
        for (AbstractProbe probe : probes) {
            //refresh the status
            probe.refreshProbeStatus();
            //obtain category and assign probe to according list
            String currentProbesCategory = probe.getCategory();
            List<AbstractProbe> probesInCategory = probesByCategory.get(currentProbesCategory);
            if (probesInCategory == null) {
                LOGGER.debug(" - Obtained new category {}", currentProbesCategory);
                probesInCategory = new LinkedList<>();
                probesInCategory.add(probe);
                LOGGER.debug(" - Adding probe {} to new category", probe.getName());
                probesByCategory.put(currentProbesCategory, probesInCategory);
            } else {
                LOGGER.debug(" - Adding probe {} to existing category {}", new Object[]{probe.getName(), currentProbesCategory});
                probesInCategory.add(probe);
            }
        }

        //sort all category keys by name
        Set<String> keys = probesByCategory.keySet();
        String[] aKeys = keys.toArray(new String[keys.size()]);
        Arrays.sort(aKeys);
        LOGGER.debug(" - Building category tabs");
        for (String key : aKeys) {
            tabsByCategory.put(key, createCategoryTab(probesByCategory.get(key)));
        }
        tabsByCategory.put("Overview", buildOverviewTab(aKeys));
        tabs.add(tabSheet.addTab(tabsByCategory.get("Overview"), "Overview"));
        
        for (String key : aKeys) {
            Tab categoryTab = tabSheet.addTab(tabsByCategory.get(key), key);
            List<AbstractProbe> probeList = probesByCategory.get(key);
            if (CollectionUtils.find(probeList, new Predicate() {
                @Override
                public boolean evaluate(Object o) {
                    return ((AbstractProbe) o).getCurrentStatus().equals(AbstractProbe.PROBE_STATUS.FAILED);
                }
            }) != null) {
                categoryTab.setIcon(new ThemeResource("img/icons/warning.png"));
            }
            
            tabs.add(categoryTab);
        }
        
        LOGGER.debug("Layout successfully created.");
    }

    /**
     * Create the tab for a single category.
     *
     * @param pProbes The probes of this category.
     *
     * @return The category tab component.
     */
    private Component createCategoryTab(List<AbstractProbe> pProbes) {
        UIUtils7.GridLayoutBuilder layoutBuilder = new UIUtils7.GridLayoutBuilder(2, pProbes.size());
        
        int row = 0;
        for (AbstractProbe probe : pProbes) {
            Embedded status = new Embedded(null, getResourceForStatus(probe.getCurrentStatus()));
            Label name = new Label(probe.getName());
            layoutBuilder.addComponent(status, Alignment.MIDDLE_LEFT, 0, row, 1, 1).addComponent(name, Alignment.MIDDLE_LEFT, 1, row, 1, 1);
            row++;
        }
        GridLayout tabLayout = layoutBuilder.getLayout();
        tabLayout.setColumnExpandRatio(0, .01f);
        tabLayout.setColumnExpandRatio(1, 1.0f);
        tabLayout.setImmediate(true);
        tabLayout.setSpacing(true);
        tabLayout.setMargin(true);
        tabLayout.setWidth("100%");
        return tabLayout;
    }

    /**
     * Return the theme resource (icon) for the provided status.
     *
     * @param pStatus The status of a probe.
     *
     * @return The resource for the provided status.
     */
    private ThemeResource getResourceForStatus(AbstractProbe.PROBE_STATUS pStatus) {
        switch (pStatus) {
            case SUCCEEDED:
                return new ThemeResource("img/24x24/bullet_ball_glass_green.png");
            case FAILED:
                return new ThemeResource("img/24x24/bullet_ball_glass_red.png");
            case UNAVAILABLE:
                return new ThemeResource("img/24x24/bullet_ball_glass_grey.png");
            case UPDATING:
                return new ThemeResource("img/24x24/refresh.png");
            default:
                //unknown
                return new ThemeResource("img/24x24/help2.png");
        }
    }

    /**
     * Assign the current "simon says" comment to the label including the
     * correct format.
     *
     * @param pLabel The label to assign the comment to.
     * @param pMessage The comment.
     */
    private void setSimonSaysContent(Label pLabel, String pMessage) {
        pLabel.setValue("<p style=\"font-size:30px;line-height:30px;vertical-align:middle;\">Simon says: " + pMessage + "</p>");
    }

    /**
     * Build the overview tab including the list of all categories and der
     * overall status.
     *
     * @param pCategories A list of all categories.
     *
     * @return The tab component.
     */
    private Component buildOverviewTab(String[] pCategories) {
        AbsoluteLayout abLay = new AbsoluteLayout();
        UIUtils7.GridLayoutBuilder layoutBuilder = new UIUtils7.GridLayoutBuilder(4, pCategories.length + 1);
        
        updateButton = new Button("Update Status");
        updateButton.addClickListener(this);
        Embedded logo = new Embedded(null, new ThemeResource("img/simon.png"));
        abLay.addComponent(logo, "top:30px;left:30px;");
        
        Label simonSaysLabel = new Label("", ContentMode.HTML);
        simonSaysLabel.setHeight("150px");
        setSimonSaysContent(simonSaysLabel, "Everything is fine.");
        abLay.addComponent(simonSaysLabel, "top:30px;left:250px;");
        
        int row = 0;
        for (String category : pCategories) {
            HorizontalLayout rowLayout = new HorizontalLayout();
            Label name = new Label(category);
            name.setWidth("200px");
            name.setHeight("24px");
            List<AbstractProbe> probes = probesByCategory.get(category);
            Collections.sort(probes, new Comparator<AbstractProbe>() {
                @Override
                public int compare(AbstractProbe o1, AbstractProbe o2) {
                    return o1.getCurrentStatus().compareTo(o2.getCurrentStatus());
                }
            });
            
            int failed = 0;
            int unknown = 0;
            int unavailable = 0;
            int charactersPerProbe = 100;
            if (probes.size() > 0) {
                charactersPerProbe = (int) Math.rint((700.0 / probes.size()) / 8.0);
            }
            
            for (AbstractProbe probe : probes) {
                Label probeLabel = new Label(StringUtils.abbreviate(probe.getName(), charactersPerProbe));
                probeLabel.setHeight("24px");
                switch (probe.getCurrentStatus()) {
                    case UNKNOWN:
                        probeLabel.setDescription(probe.getName() + ": UNKNOWN");
                        probeLabel.addStyleName("probe-unknown");
                        unknown++;
                        break;
                    case UPDATING:
                        probeLabel.setDescription(probe.getName() + ": UPDATING");
                        probeLabel.addStyleName("probe-updating");
                        break;
                    case UNAVAILABLE:
                        probeLabel.setDescription(probe.getName() + ": UNAVAILABLE");
                        probeLabel.addStyleName("probe-unavailable");
                        unavailable++;
                        break;
                    case FAILED:
                        probeLabel.setDescription(probe.getName() + ": FAILED");
                        probeLabel.addStyleName("probe-failed");
                        failed++;
                        break;
                    default:
                        probeLabel.setDescription(probe.getName() + ": SUCCESS");
                        probeLabel.addStyleName("probe-success");
                }
                
                probeLabel.addStyleName("probe");
                rowLayout.addComponent(probeLabel);
            }
            
            if (failed != 0) {
                setSimonSaysContent(simonSaysLabel, "There are errors!");
            } else {
                if (unknown != 0) {
                    setSimonSaysContent(simonSaysLabel, "There are unknown states. Please select 'Update Status'.");
                } else {
                    if (unavailable != 0) {
                        setSimonSaysContent(simonSaysLabel, "Some probes are unavailable. Please check their configuration.");
                    }
                }
            }

            rowLayout.setWidth("700px");
            layoutBuilder.addComponent(name, Alignment.TOP_LEFT, 0, row, 1, 1).addComponent(rowLayout, Alignment.TOP_LEFT, 1, row, 3, 1);
            row++;
        }
        
        layoutBuilder.addComponent(updateButton, Alignment.BOTTOM_RIGHT, 3, row, 1, 1);
        
        GridLayout tabLayout = layoutBuilder.getLayout();
        tabLayout.setSpacing(true);
        tabLayout.setMargin(true);
        Panel p = new Panel();
        p.setContent(tabLayout);
        p.setWidth("1024px");
        p.setHeight("400px");
        abLay.addComponent(p, "top:160px;left:30px;");
        abLay.setSizeFull();
        return abLay;
    }
    
    @Override
    public void buttonClick(Button.ClickEvent event) {
        reload();
    }
}
