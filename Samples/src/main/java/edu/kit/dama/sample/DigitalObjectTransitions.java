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
import edu.kit.dama.mdm.base.DigitalObjectTransition;
import edu.kit.dama.mdm.base.TransitionType;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectTransitionWrapper;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import edu.kit.dama.util.Constants;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * While working with digital objects stored in a repository system it might be
 * necessary to reflect relations between digital objects. For KIT DM this is
 * the case for the Data Workflow service to represent the relationship between
 * workflow input and output objects to gather provenance information. However,
 * the feature of digital object transitions can also be used to reflect custom
 * relationships between digital objects.The following example shows the two
 * different ways of adding transitions using the RESTful base metadata service
 * of KIT DM. A third option allowing to assign extended information to a
 * transition is presented but commented as this option requires further
 * configuration as described below.
 *
 * <b>Attention</b>: Please remember to change OAuth access key and access
 * secret according to your user. For default KIT DM instances, which are using
 * the provided sampledata.sql script, accessKey 'admin' and accessSecret
 * 'dama14' should work.
 *
 * @author jejkal
 */
public class DigitalObjectTransitions extends BaseMetadataCreation {

    public void createDigitalObjectTransition() {
        //perform the base metadata creation from the super class in order to have a digital object. We skip the data ingest for this example.      
        createBaseMetadata();

        //We now create another digital objects, the first one available as 'newDigitalObject', acts as 'version 1' and the second one as 'version 2'.
        //How both versions where created doesn't matter for the moment, let's assume we performed a local processing of the data of 
        //'newDigitalObject' and ingested both, the original data and the processed data as two objects.
        DigitalObject object2 = getDigitalObject();
        object2.setLabel("Object_v2");
        //Create the second object.
        DigitalObjectWrapper digitalObjectWrapper = client.addDigitalObjectToInvestigation(newInvestigation.getInvestigationId(), object2, Constants.USERS_GROUP_ID);
        object2 = digitalObjectWrapper.getEntities().get(0);
        //Now there are two independent objects in the repository that we want to connect now. Therefor, KIT DM uses so called transitions. 
        //A transition is defined to have one or more input objects including a data organization view for each input object, 
        //just to define where the input data for the transition came frome for objects having multiple views, and one or more output objects. 
        //For the output objects it is assumed that the data is located in the view named 'default'.
        //There are two different ways for adding transitions:
        /**
         * a) The simple way adding a 1:1 transition with no type. The first
         * argument is the first object of the transition, the second parameter
         * is the second object, followed by the view associated with the input
         * object. The fourth parameter is the object which is the output of the
         * transition (the other object is than assumed to be input). Meaning,
         * the call below results in the following transition (view names are in
         * backets): newDigitalObject(default) --> object2(default)
         */
        DigitalObjectTransitionWrapper result = client.addTransitionToDigitalObject(newDigitalObject, object2, Constants.DEFAULT_VIEW, object2, Constants.USERS_GROUP_ID);
        System.out.println("Transition successfully created with id " + result.getEntities().get(0).getId());
        /**
         * a) The enhanced way adding a n:n transition with optional type
         * information. Basically, the followin code has the same result as the
         * line above. However, it offers much more flexibility. Apart from
         * being able to provide multiple inputs and outputs one can also
         * provide a transition type including type-specific data, e.g.
         * provenance information. The way how this data is handled depends on
         * the server configuration. Therefor, we present here no specific
         * example as this may not work in every case. If you are interested in
         * an example please refer to the commendet code block below.
         */
        Map<DigitalObject, String> inputObjectViewMap = new HashMap<>();
        inputObjectViewMap.put(newDigitalObject, Constants.DEFAULT_VIEW);
        List<DigitalObject> outputObjectList = Arrays.asList(object2);
        result = client.addDigitalObjectTransition(inputObjectViewMap, outputObjectList, TransitionType.NONE, null, Constants.USERS_GROUP_ID);
        System.out.println("Transition successfully created with id " + result.getEntities().get(0).getId());

        //Now that we've added a transition we may also want to retrieve this information.
        //The following call is responsible to get the contribution information, meaning, the transitions where the provided object is the contributor (the input).
        //As we've created two transitions before, where 'newDigitalObject' is defined as input, we expect the output of two ids.
        result = client.getDigitalObjectContributionInformation(newDigitalObject, Constants.USERS_GROUP_ID);
        System.out.println("NewDigitalObject is input in the following transitions:");
        for (DigitalObjectTransition t : result.getEntities()) {
            System.out.println(" - " + t.getId());
        }
        //Please remember that object2 was in both transitions the output. Therefor, we now expect no result.
        result = client.getDigitalObjectContributionInformation(object2, Constants.USERS_GROUP_ID);
        System.out.println("Object2 is input in the following transitions:");
        for (DigitalObjectTransition t : result.getEntities()) {
            System.out.println(" - " + t.getId());
        }
        //To obtain transitions where an object is the output, the second method is used. 
        //This returns transitions where the provided object is derived (thr output) from another object. 
        result = client.getDigitalObjectDerivationInformation(object2, Constants.USERS_GROUP_ID);
        System.out.println("Object2 is output in the following transitions:");
        for (DigitalObjectTransition t : result.getEntities()) {
            System.out.println(" - " + t.getId());
        }

        /**
         * Example for providing transition type information. If the used
         * repository system is configured to use the default handler for
         * transitions of type TransitionType.ELASTICSEARCH, the provided
         * transition data can be indexed at a configured elasticsearch
         * instance.
         */
//        //First we create/obtain the information serving as type-specific data. 
//        //The provided type TransitionType.ELASTICSEARCH is rather flexible at this point. 
//        //It assumes to get a JSON structure that can be put into an elasicsearch index.
//        //However, as mentioned before, this needs server-side configuration so it might not work
//        //out-of-the-box.
//        //The information we create will look as follows: 
//        //
//        //{
//        //"editor": "Jane Doe",
//        //"software": {
//        //   "name": "Photoshop",
//        //   "developer": "Adobe",
//        //   "version": "CS6"
//        //   },
//        //"creationDate": "2016-05-11T12:29:12+00:00"
//        //}
//        //
//        JSONObject object = new JSONObject();
//        JSONObject software = new JSONObject();
//        software.put("name", "Photoshop");
//        software.put("developer", "Adobe");
//        software.put("version", "CS6");
//        object.put("software", software);
//        object.put("creationDate", "2016-05-11T12:29:12+00:00");
//        object.put("editor", "Jane Doe");
//
//        //Now, we just provide the string representation of this JSON object to the addTransition method as follows:
//        Map<DigitalObject, String> inputObjectViewMap = new HashMap<>();
//        inputObjectViewMap.put(newDigitalObject, Constants.DEFAULT_VIEW);
//        List<DigitalObject> outputObjectList = Arrays.asList(object2);
//        result = client.addDigitalObjectTransition(inputObjectViewMap, outputObjectList, TransitionType.ELASTICSEARCH, object.toString(), Constants.USERS_GROUP_ID);
//        System.out.println("Transition successfully created with id " + result.getEntities().get(0).getId());
    }
}
