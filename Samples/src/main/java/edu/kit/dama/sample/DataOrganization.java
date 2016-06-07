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
package edu.kit.dama.sample;

import edu.kit.dama.mdm.dataorganization.impl.staging.DataOrganizationNodeImpl;
import edu.kit.dama.rest.dataorganization.client.impl.DataOrganizationRestClient;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.util.Constants;
import java.io.File;
import java.io.IOException;

/**
 * This sample covers the access to a digital object's data organization. The
 * data organization basically reflects the data associated with a digital
 * object. In the easiest case it is created automatically from an ingested file
 * tree. Folders are reflected as collection nodes, files as file nodes. In more
 * sophisticated use cases a custom data organization might be ingested (see
 * example CustomDataOrganizationIngest).
 *
 * The following snippets show how to traverse through a data organization tree
 * view the RESTful service client. However, for typical end-user applications
 * it might be reasonable to provide a Web interface where the entire data
 * organization can be loaded in one call using the Java APIs of KIT DM.
 *
 * @author jejkal
 */
public class DataOrganization extends BasicDataIngest {

    public void accessDataOrganization() {
        //perform the base metadata creation and data ingest in order to be able to use the data organization 
        ingestData();

        DataOrganizationRestClient doClient = new DataOrganizationRestClient(restBaseUrl + "/rest/dataorganization/", context);

        //At first we obtain the root node of the data organization for a digital object. In order to be able to do so, a data ingest has to be successfully performed.
        //Obtaining the root node requires the base id of the object as input together with the so called 'view' which allows to distinguish between different data organizations
        //for the same digital object, e.g. for restructuring ingested data based on a specific use case.
        DataOrganizationNodeWrapper wrapper = doClient.getRootNode(Constants.USERS_GROUP_ID, newDigitalObject.getBaseId(), 0, Integer.MAX_VALUE, Constants.DEFAULT_VIEW);
        //Typically, each data organization should have one root node and multiple organizations are reflected by views. 
        //However, in real application this should be checked as multiple roots are supported.
        DataOrganizationNodeImpl root = wrapper.getEntities().get(0);
        System.out.println("Obtained root node with id " + root.getNodeId());
        //Based on the root node we can traverse through the entire data organization tree. The procedure is for all nodes the same: 
        //1) At first, we obtain the child node count for a node:
        wrapper = doClient.getChildCount(Constants.USERS_GROUP_ID, newDigitalObject.getBaseId(), root.getNodeId(), Constants.DEFAULT_VIEW);
        int childCount = wrapper.getCount();
        System.out.println("Root node has " + childCount + " child nodes.");
        //2) If there are children, we request a subset or all of them via getChildren using the node id of the parent.
        wrapper = doClient.getChildren(Constants.USERS_GROUP_ID, newDigitalObject.getBaseId(), root.getNodeId(), 0, childCount, Constants.DEFAULT_VIEW);
        System.out.println("The root node has the following children: ");
        for (DataOrganizationNodeImpl impl : wrapper.getEntities()) {
            System.out.println("Name: " + impl.getName());
            System.out.println(" View: " + impl.getViewName());
            System.out.println(" LFN: " + impl.getLogicalFileName());
            System.out.println(" Attributes: " + impl.getAttributes().size());
            //Additional levels can be queried recursively beginning here 
            System.out.println(" Children: " + impl.getChildren().size());
        }

        //As mentioned before, there might be different data organizations (so called 'views') associated with one digital object. 
        //Each of these 'views' can be accessed via a unique identifier called 'view name'. In order to obtain valid view names for an object, getViews can be used.
        DataOrganizationViewWrapper viewWrapper = doClient.getViews(Constants.USERS_GROUP_ID, newDigitalObject.getBaseId());
        System.out.println("The digital object with the id " + newDigitalObject.getDigitalObjectIdentifier() + " supports the following views:");
        for (String viewName : viewWrapper.getEntities()) {
            System.out.println("- " + viewName);
        }

        //Finally, the data organization service offers basic download capabilities. It is possible to download the content of file nodes stored in the local archive or accessible via HTTP 
        //or to download collection nodes. In the later case all contained nodes are packed and transferred an ZIP archive.
        //The arguments are almost identical to the previous calls except the DataOrganizationPath and the destination. The DataOrganizationPath is the hierarchical path to a DataOrganizationNodes.
        //The path is the concatenation of all nodes beginning from the root to the downloaded node. E.g. for the following data organization tree: 
        //
        //+-/
        //  +-papers/
        //    +-Paper1.pdf (https://www.researchgate.net/profile/T_Jejkal/publication/224226353_Perspective_of_the_Large_Scale_Data_Facility_(LSDF)_Supporting_Nuclear_Fusion_Applications/links/0912f509a04be916b9000000.pdf)
        //    +-Paper2.pdf (http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.461.71&rep=rep1&type=pdf)
        //
        //The DataOrganizationPath 'papers' is valid and refers to a collection node downloaded as ZIP. 'papers/Paper1.pdf' is also a valid DataOrganizationPath referring to a file node downloaded as PDF from the provided URL. 
        //The following call would download 'Paper1.pdf' and stored the content in a file named 'DownloadedPaper.pdf' in the current directory.
        try {
            doClient.downloadData(Constants.USERS_GROUP_ID, newDigitalObject.getBaseId(), Constants.DEFAULT_VIEW, "papers/Paper1.pdf", new File("."), "DownloadedPaper.pdf", context);
        } catch (IOException ex) {
            //some error occured
        }

        //ToDo: Add example for customizing views 
        //doClient.postView(Constants.USERS_GROUP_ID, newDigitalObject.getBaseId(), tree, Boolean.TRUE);
    }

}
