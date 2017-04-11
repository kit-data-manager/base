/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;
import edu.kit.dama.mdm.core.MetaDataManagement;

/**
 *
 * @author jejkal
 */
//Set standard configuration by annotation, the actual servlet path must be configured in web.xml
//@VaadinServletConfiguration(productionMode = true, ui = AdminUIMainView.class, widgetset = "edu.kit.dama.ui.admin.AppWidgetSet")
public class AdminUIServlet extends VaadinServlet {

    @Override
    public void destroy() {
        //destroy also all metadata management instances on redeployment in order to close all database connections
        MetaDataManagement.getMetaDataManagement().destroy();
        super.destroy();
    }

}
