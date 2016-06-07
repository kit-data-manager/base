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

import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import edu.kit.dama.rest.basemetadata.types.InvestigationWrapper;
import edu.kit.dama.rest.basemetadata.types.StudyWrapper;
import edu.kit.dama.util.Constants;
import java.util.Date;

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
public class BaseMetadataCreation {

    String accessKey = "putYourKeyHere";
    String accessSecret = "putYourSecretHere";
    String restBaseUrl = "http://localhost:8080/KITDM";
    SimpleRESTContext context = new SimpleRESTContext(accessKey, accessSecret);
    DigitalObject newDigitalObject;
    BaseMetaDataRestClient client;
    Study newStudy;
    Investigation newInvestigation;
    DigitalObject digitalObject;

    void createBaseMetadata() {
        //First, create temporary objects which contain the attributes you want to provide for the metadata entities.
        //It is recommended to collect as many attributes as possible in order to be able to distinguish registered metadata entities as good as possible.
        //At least topic/label should be provided for studies, investigations and digital objects.
        //Collect all study attributes and return it as a temporary object.
        newStudy = getStudy();
        //Collect all investigation attributes and return it as a temporary object.
        newInvestigation = getInvestigation();
        //Collect all digital object attributes and return it as a temporary object.
        digitalObject = getDigitalObject();

        //Instantiate BaseMetaDataRestClient using the base URL and the security context, both defined above
        client = new BaseMetaDataRestClient(restBaseUrl + "/rest/basemetadata/", context);

        //Create a new study. The study will be assigned to the default group whose ID we've obtained above.
        StudyWrapper studyWrapper = client.addStudy(newStudy, Constants.USERS_GROUP_ID);
        //Assign returned study to 'newStudy' as the created entity now contains a valid studyId.
        newStudy = studyWrapper.getEntities().get(0);

        //Use the studyId to add a new investigation to the study we've just created.
        InvestigationWrapper investigationWrapper = client.addInvestigationToStudy(newStudy.getStudyId(), newInvestigation, Constants.USERS_GROUP_ID);
        //Assign returned investigation to 'newInvestigation' as the created entity now contains a valid investigationId.
        newInvestigation = investigationWrapper.getEntities().get(0);

        //Use the investigationId to add a new digital object to the investigation just created.
        DigitalObjectWrapper digitalObjectWrapper = client.addDigitalObjectToInvestigation(newInvestigation.getInvestigationId(), digitalObject, Constants.USERS_GROUP_ID);
        //Assign returned digitalObject to 'newDigitalObject' as the created entity now contains a valid objectId.
        newDigitalObject = digitalObjectWrapper.getEntities().get(0);

        System.out.println("Successfully created new object:\n" + newDigitalObject);
    }

    Study getStudy() {
        Study result = Study.factoryNewStudy();
        result.setTopic("Sample Study");
        result.setNote("This is a sample");
        result.setStartDate(new Date());
        return result;
    }

    Investigation getInvestigation() {
        Investigation result = Investigation.factoryNewInvestigation();
        result.setTopic("Sample Investigation");
        result.setNote("This is a sample");
        result.setStartDate(new Date());
        return result;
    }

    DigitalObject getDigitalObject() {
        DigitalObject result = DigitalObject.factoryNewDigitalObject();
        result.setLabel("Sample DigitalObject");
        result.setNote("This is a sample");
        result.setStartDate(new Date());
        return result;
    }

}
