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
import edu.kit.dama.rest.staging.types.DownloadInformationWrapper;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
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
public class BasicDataDownload extends BasicDataIngest {

    void downloadData() throws StagingIntitializationException {
        //perform the base metadata creation and data ingest from the super classes in order to have a digital object and data we can download
        createBaseMetadata();
        ingestData();

        StagingServiceRESTClient stagingClient = new StagingServiceRESTClient(restBaseUrl + "/rest/staging/", context);

        //At first we have to obtain the StagingAccessPoint in order to be able to create a download.
        //For convenience we expect to have exactly one AccessPoint as set up during the default installation.
        //At first we obtain the id, followed by a query for detailed information.
        long accessPointId = stagingClient.getAllAccessPoints(Constants.USERS_GROUP_ID, context).getEntities().get(0).getId();
        String uniqueAPIdentifier = stagingClient.getAccessPointById(accessPointId, context).getEntities().get(0).getUniqueIdentifier();

        //Now, we create the download for the DigitalObject we have just created and ingested.
        //To identify the object we use the primary key of the object available after persisting the object.
        DownloadInformationWrapper wrapper = stagingClient.createDownload(Long.toString(newDigitalObject.getBaseId()), uniqueAPIdentifier);

        //Now, we can obtain the id of the download, which will be used further on.
        long downloadId = wrapper.getEntities().get(0).getId();

        //As download preparation takes place asynchronously, providing the data will take a while.
        //Depending on the setup of the repository system and the number of open transfers it may 
        //take one or more minutes until the download preparation for our object is triggered by the KIT DM scheduler.
        //The current status can be polled by calling:
        boolean downloadReady = false;
        while (!downloadReady) {
            wrapper = stagingClient.getDownloadById(downloadId);
            DOWNLOAD_STATUS downloadStatus = DOWNLOAD_STATUS.idToStatus(wrapper.getEntities().get(0).getStatus());
            if (downloadStatus.isErrorState()) {
                throw new StagingIntitializationException("Failed to prepare download. Data download not possible.");
            }
            downloadReady = downloadStatus.isUserInteractionPossible();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                break;
            }
        }

        //At this point, the object's data should be available at the caching location defined by the selected staging access point. 
        //If the status is DOWNLOAD_STATUS.DOWNLOAD_READY (code 4), data can be downloaded from the URL obtainable by:
        wrapper = stagingClient.getDownloadById(downloadId);
        URL dataDownloadFolder = wrapper.getEntities().get(0).getDataFolderUrl();

        //TODO: Download the data from dataUploadFolder using an appropriate data transfer client depending on the protocol and the utilized access point.
        System.out.println("Your download is prepared. You can download your data from " + dataDownloadFolder);
        //Cleanup (removing of staged data) should be handled by the repository system. By default, the download will be removed after one week. 
        //This can be changed by editing the file datamanager.xml containing all KIT DM properties.
    }

}
