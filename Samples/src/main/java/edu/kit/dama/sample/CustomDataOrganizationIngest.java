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

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl;
import edu.kit.dama.mdm.dataorganization.impl.util.DataOrganizationTreeBuilder;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.rest.staging.client.impl.StagingRestClient;
import edu.kit.dama.rest.staging.types.IngestInformationWrapper;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.exceptions.StagingIntitializationException;
import edu.kit.dama.util.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import org.json.JSONObject;

/**
 * An enhanced feature of KIT DM is the ability to define custom data
 * organization views that must not reflect existing file trees but may consist
 * of references to data stored in other systems. The workflow is identical to
 * the default file-based ingest. The difference is made by linking a
 * StagingProcessor to the ingest.
 *
 * This processor is implemented in the class
 * edu.kit.dama.staging.processor.impl.ReferenceTreeIngestProcessor. It expects
 * files in a specific JSON format named by a specific pattern in the data
 * folder of the pre-ingest. Please refer to the code of this sample class for
 * further explainations on the JSON format and the filename pattern.
 *
 * <b>Attention</b>: Please remember to change OAuth access key and access
 * secret according to your user. For default KIT DM instances, which are using
 * the provided sampledata.sql script, accessKey 'admin' and accessSecret
 * 'dama14' should work.
 *
 * @author jejkal
 */
public class CustomDataOrganizationIngest extends BaseMetadataCreation {

    void ingestData() throws StagingIntitializationException {
        //perform the base metadata creation from the super class in order to have a digital object we can ingest data for
        createBaseMetadata();

        StagingRestClient stagingClient = new StagingRestClient(restBaseUrl + "/rest/staging/", context);

        //At first we have to obtain the StagingAccessPoint in order to be able to schedule an ingest.
        //For convenience we expect to have exactly one AccessPoint as set up during the default installation.
        //At first we obtain the id, followed by a query for detailed information.
        long accessPointId = stagingClient.getAllAccessPoints(Constants.USERS_GROUP_ID, context).getEntities().get(0).getId();
        String uniqueAPIdentifier = stagingClient.getAccessPointById(accessPointId, context).getEntities().get(0).getUniqueIdentifier();

        //Now, we schedule the ingest for the DigitalObject we have just created.
        //To identify the object we use the primary key of the object available after persisting the object.
        IngestInformationWrapper wrapper = stagingClient.createIngest(Long.toString(newDigitalObject.getBaseId()), uniqueAPIdentifier);

        //Now, we can obtain the id of the ingest, which will be used further on.
        long ingestId = wrapper.getEntities().get(0).getId();

        //As ingest preparation takes place synchronously, the ingest is set up immediately.
        //To be sure, the current status can be polled by calling:
        boolean preIngestReady = false;
        while (!preIngestReady) {
            wrapper = stagingClient.getIngestById(ingestId);
            INGEST_STATUS ingestStatus = INGEST_STATUS.idToStatus(wrapper.getEntities().get(0).getStatus());
            if (ingestStatus.isErrorState()) {
                throw new StagingIntitializationException("Failed to prepare ingest. Data upload not possible.");
            }
            preIngestReady = ingestStatus.isUserInteractionPossible();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
        }//this loop should be left after one iteration as the state should be INGEST_STATUS.PRE_INGEST_SCHEDULED after creating the ingest, except if ingest preparation has failed.

        //Now, we create the data organization view definition that will be uploaded. 
        //Therefore, we build a virtual file tree with the following structure:
        //+-/
        //  +-papers/
        //    +-Paper1.pdf (https://www.researchgate.net/profile/T_Jejkal/publication/224226353_Perspective_of_the_Large_Scale_Data_Facility_(LSDF)_Supporting_Nuclear_Fusion_Applications/links/0912f509a04be916b9000000.pdf)
        //    +-Paper2.pdf (http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.461.71&rep=rep1&type=pdf)
        //
        //Please be aware that the LFNs must be valid URLs!
        IFileTree tree = new DataOrganizationTreeBuilder().
                create(new DigitalObjectId(wrapper.getEntities().get(0).getDigitalObjectId()), Constants.DEFAULT_VIEW).
                createAndEnterCollection("papers").
                addFile(new LFNImpl("https://www.researchgate.net/profile/T_Jejkal/publication/224226353_Perspective_of_the_Large_Scale_Data_Facility_(LSDF)_Supporting_Nuclear_Fusion_Applications/links/0912f509a04be916b9000000.pdf"), "Paper1.pdf").
                addFile(new LFNImpl("http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.461.71&rep=rep1&type=pdf"), "Paper2.pdf").
                leaveCollection().
                buildTree();

        //Now, we store a JSON representation of the created file tree in a file named 'default_view.json'. 
        //The pattern for the file must be the name of the data organization view provided for the contained file tree (in out case this is Constants.DEFAULT_VIEW) followed by _view.json
        //There are some reserved view names that should not be used. These are defined as Constants.DATA_VIEW and Constants.GENERATED_VIEW. Constants.DEFAULT_VIEW is also reserved but
        //is allowed to be used for the initial ingest.
        JSONObject jsonFileTree = Util.fileTreeToJsonView(tree);

        File f = new File(Constants.DEFAULT_VIEW + "_view.json");
        try {
            new FileOutputStream(f).write(jsonFileTree.toString().getBytes());
        } catch (IOException ex) {
            throw new StagingIntitializationException("Failed to prepare custom data organization definition file.");
        }

        //It is also possible to upload multiple view definitions containing a different structure in different files. 
        //All view definitions must be stored directly in the root folder of the upload. 
        //View definitions are handled by a specific staging processor located in edu.kit.dama.staging.processor.impl.ReferenceTreeIngestProcessor
        //This processor must be configured using the AdminUI of the repository system in order to handle ingested view definitions and should be
        //set to be enabled by 'default' for every ingest. Please refer to the manual on how to add a staging processor.     
        //
        //
        //
        //Now we can obtain the data upload folder and upload the view definition directly to it.
        wrapper = stagingClient.getIngestById(ingestId);
        URL dataUploadFolder = wrapper.getEntities().get(0).getDataFolderUrl();
        /**
         * Here we should continue only if:
         *
         * a) We have uploaded the file default_view.json programmatically to
         * dataUploadFolder or
         *
         * b) If the user has uploaded the file in an external workflow.
         *
         * Continuing without any upload would result in a digital object
         * containing no data.
         *
         */
        //As soon as the upload has finished, the status has to be set to INGEST_STATUS.PRE_INGEST_FINISHED (code 16) in order to trigger the actual ingest of the data into the repository storage
        //including all further steps necessary to register the uploaded data, e.g. metadata extraction.
        //updateIngest() returns a ClientResponse object that can be checked for success via response.getStatus() == 200
        if (stagingClient.updateIngest(ingestId, null, INGEST_STATUS.PRE_INGEST_FINISHED.getId()).getStatus() != 200) {
            throw new StagingIntitializationException("Failed to update ingest status to  INGEST_STATUS.PRE_INGEST_FINISHED. Data ingest into repository not possible.");
        }

        //If further monitoring is required, you can wait for a status change to status INGEST_STATUS.INGEST_FINISHED (code 128), which means the data is stored in the repository storage.
        //Depending on the setup of the repository system and the number of open transfers it may take one or more minutes until the ingest of our object is triggered by the KIT DM scheduler.
        boolean ingestFinished = false;

        while (!ingestFinished) {
            wrapper = stagingClient.getIngestById(ingestId);
            INGEST_STATUS ingestStatus = INGEST_STATUS.idToStatus(wrapper.getEntities().get(0).getStatus());
            ingestFinished = ingestStatus.isFinalState() || ingestStatus.isErrorState();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
        }

        //At this point, the view definition should be handled and registered in the repository system.
    }
}
