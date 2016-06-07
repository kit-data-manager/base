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

import edu.kit.dama.rest.staging.client.impl.StagingServiceRESTClient;
import edu.kit.dama.rest.staging.types.IngestInformationWrapper;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.exceptions.StagingIntitializationException;
import edu.kit.dama.util.Constants;
import java.net.URL;

/**
 * Create a base metadata structure consisting of study, investigation and
 * digital object. Therefor, a KIT DM instance running on localhost, port 8080
 * deployed at path KITDM is accessed via its RESTful base metadata service.
 *
 * <b>Attention</b>: Please remember to change OAuth access key and access
 * secret according to your user. For default KIT DM instances, which are using
 * the provided sampledata.sql script, accessKey 'admin' and accessSecret
 * 'dama14' should work.
 *
 * @author jejkal
 */
public class BasicDataIngest extends BaseMetadataCreation {

    void ingestData() throws StagingIntitializationException {
        //perform the base metadata creation from the super class in order to have a digital object we can ingest data for
        createBaseMetadata();

        StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(restBaseUrl + "/rest/staging/", context);

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

        //If the status is INGEST_STATUS.PRE_INGEST_SCHEDULED (code 4), data can be uploaded to the URL obtainable by:
        wrapper = stagingClient.getIngestById(ingestId);
        URL dataUploadFolder = wrapper.getEntities().get(0).getDataFolderUrl();
        //TODO: Upload the data to dataUploadFolder using an appropriate data transfer client depending on the protocol and the utilized access point.//
        System.out.println("Your ingest has been prepared. You can now upload your data to " + dataUploadFolder);

        /**
         * Here we should continue only if:
         *
         * a) We have uploaded the data programmatically to dataUploadFolder or
         *
         * b) If the user has uploaded the data in an external workflow.
         *
         * Continuing without any upload would result in a digital object
         * containing no data.
         *
         */
        //As soon as the upload has finished, the status has to be set to INGEST_STATUS.PRE_INGEST_FINISHED (code 16) in order to trigger the actual ingest of the data into the repository storage
        //including all further steps necessary to register the uploaded data, e.g. metadata extraction.
        //A call to updateIngest() returns a ClientResponse object that can be checked for success via response.getStatus() == 200
        if (stagingClient.updateIngest(ingestId, null, INGEST_STATUS.PRE_INGEST_FINISHED.getId()).getStatus() == 200) {
            //status update successful
        } else {
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

        //At this point, the object's data should be registered in the repository storage and can be downloaded.
    }

}
