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
import edu.kit.dama.mdm.base.DigitalObjectType;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectTypeWrapper;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import edu.kit.dama.util.Constants;

/**
 * This sample covers assigning and querying for digital object types. These
 * types can be used to annotate/tag digital objects, e.g. to mark objects
 * having special properties or to allow fine-grained filtering for specific
 * objects. A digital object type is defined by its domain (which might be used
 * for grouping), its identifier (e.g. a human readable name) and a version
 * number. Each combination of domain, identifier and version defines a unique
 * transition type. The sample describes how to register and assign a digital
 * object type and how to retrieve all objects having a type assigned and all
 * types assigned to an object.
 *
 * <b>Attention</b>: Please remember to change OAuth access key and access
 * secret according to your user. For default KIT DM instances, which are using
 * the provided sampledata.sql script, accessKey 'admin' and accessSecret
 * 'dama14' should work.
 *
 * @author jejkal
 */
public class DigitalObjectTypes extends BaseMetadataCreation {

    public void assignDigitalObjectType() {
        //perform the base metadata creation from the super class in order to have a digital object. We skip the data ingest for this example.      
        createBaseMetadata();
        //At first, we create a digital object type.
        DigitalObjectType type = new DigitalObjectType();
        type.setTypeDomain("MyTypes");
        type.setIdentifier("SpecialObject");
        type.setVersion(1);
        type.setDescription("No description provided.");
        //Regisgter the type using the base metadata rest client.
        DigitalObjectTypeWrapper wrapper = client.addDigitalObjectType(type, Constants.USERS_GROUP_ID);
        type = wrapper.getEntities().get(0);
        System.out.println("A type with the identifier " + type.getIdentifier() + "  has been successfully registered.");

        //assign the created type to the previously created digital object.
        client.addDigitalObjectTypeToDigitalObject(newDigitalObject.getBaseId(), type, Constants.USERS_GROUP_ID);

        //Now we can obtain all digital objects having our type assigned.
        DigitalObjectWrapper result = client.getDigitalObjectByDigitalObjectType(type, accessKey);
        System.out.println("The following objects have assigned the type '" + type.getIdentifier() + "':");
        for (DigitalObject o : result.getEntities()) {
            System.out.println("[" + o.getBaseId() + "] " + o.getLabel());
        }

        //We can also obtain a list of types assigned to a specific object.
        wrapper = client.getDigitalObjectTypesByDigitalObject(digitalObject, accessKey);
        System.out.println("The following object types have assigned 'newDigitalObject':");
        for (DigitalObjectType t : wrapper.getEntities()) {
            System.out.println(" - " + t);
        }

        //As each combination of domain, identifier and version must be unique, only the description of an existing type might be updated. 
        //This can be done in the same way as a type is initially registered. You'll see that the id won't change.
        System.out.println("The id of the previously registered type is: " + type.getId());
        System.out.println("Unique identity: " + type);
        System.out.println("Description: " + type.getDescription());
        System.out.println("---");
        type = new DigitalObjectType();
        type.setTypeDomain("MyTypes");
        type.setIdentifier("SpecialObject");
        type.setVersion(1);
        type.setDescription("Type for annotating special objects.");
        //Update the type.
        wrapper = client.addDigitalObjectType(type, Constants.USERS_GROUP_ID);
        type = wrapper.getEntities().get(0);
        System.out.println("The id of the updated type is: " + type.getId());
        System.out.println("Unique identity: " + type);
        System.out.println("Description: " + type.getDescription());
    }
}
