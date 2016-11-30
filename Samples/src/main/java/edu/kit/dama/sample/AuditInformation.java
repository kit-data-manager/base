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

import edu.kit.dama.mdm.audit.types.AuditDetail;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.rest.audit.client.impl.AuditRestClient;
import edu.kit.dama.rest.audit.types.AuditEventWrapper;
import edu.kit.dama.util.Constants;
import java.util.Arrays;

/**
 * Trigger and obtain audit information. Therefor, a KIT DM instance running on
 * localhost, port 8080 deployed at path KITDM is accessed via its RESTful audit
 * service.
 *
 * <b>Attention</b>: Please remember to change OAuth access key and access
 * secret according to your user. For default KIT DM instances, which are using
 * the provided sampledata.sql script, accessKey 'admin' and accessSecret
 * 'dama14' should work.
 *
 * @author jejkal
 */
public class AuditInformation extends BaseMetadataCreation {

    void auditInformation() {
        //Perform the base metadata creation from the super class in order to have a digital object we can capture audit information for
        createBaseMetadata();
        //Actually, if the audit service is configured correctly, we now already have our first audit event as the creation of digital objects 
        //via REST endpoint is an INTERNAL audit event trigger. Hence, there should be an audit event of type AuditEvent.Type.CREATION in the database.
        //First, lets instantiate a REST client for the audit service.
        AuditRestClient auditClient = new AuditRestClient(restBaseUrl + "/rest/audit/", context);
        //At this point we expect one audit event stored in the database, so let's query for available event.
        AuditEventWrapper result = auditClient.getEvents(digitalObject.getDigitalObjectIdentifier(), 0, Integer.MAX_VALUE, Constants.USERS_GROUP_ID);
        //Print all audit events obtained for the provided object id.
        System.out.println("For object '" + digitalObject.getDigitalObjectIdentifier() + "' the following audit events could be obtained:");
        for (AuditEvent event : result.getEntities()) {
            System.out.println(event);
        }
        //As mentioned before, we expect one event of type AuditEvent.Type.CREATION. Basically, such an event should occur only once for each and every audited resource,
        //so let's query for events of type  AuditEvent.TYPE.CREATION for our digital object.
        result = auditClient.getEvents(digitalObject.getDigitalObjectIdentifier(), AuditEvent.TYPE.CREATION, null, null, null, null, 0, Integer.MAX_VALUE, Constants.USERS_GROUP_ID);
        //Print all audit events obtained for the provided object id.
        System.out.println("For object '" + digitalObject.getDigitalObjectIdentifier() + "' the following audit events of type AuditEvent.TYPE.CREATION could be obtained:");
        for (AuditEvent event : result.getEntities()) {
            System.out.println(event);
        }
        //Apart from internally triggered events it is also possible to trigger audit events externally via REST endpoint. Events created via REST endpoint are always EXTERNAL events. 
        //Parameters are at least the pid of the resource the event is associated with, the event type and a category.
        //The category defines the scope of the event. By default, the category has to start with 'audit.' followed by a sub-category defining the resource type, e.g. 'digitalObject'. 
        //The main part of the category ('audit.') is configurable on the server side, the sub-category is currently not evaluated.
        boolean bResult = auditClient.createEvent(digitalObject.getDigitalObjectIdentifier(), AuditEvent.TYPE.VALIDATION, "audit.digitalObject", Constants.USERS_GROUP_ID);
        System.out.println("A new external audit event has " + ((!bResult) ? "not" : "") + " been published.");
        //Please be aware the auditing information are handled asynchronously. Therefor, a positive result of the service call does not mean that the event has been consumed by any of the configured consumers.
        //Furthermore, an audit event could be extended by audit details. Currently, there are two types of details: AuditDetail.TYPE.ARGUMENT and AuditDetail.TYPE.COMMENT
        //Argument details are typically used to provide arguments which parameterized the operation that led to the event, e.g. updated metadata fields. Comment details can be used to provide human 
        //readable information for an audit event. Providing audit details is rather easy:
        bResult = auditClient.createEvent(digitalObject.getDigitalObjectIdentifier(), AuditEvent.TYPE.VALIDATION, "audit.digitalObject", Arrays.asList(AuditDetail.factoryCommentDetail("This is a comment.")), Constants.USERS_GROUP_ID);
        System.out.println("A new external audit event with a comment detail has " + ((!bResult) ? "not" : "") + " been published.");

        //Finally, something that is not of importance for client development, but will definately be relevant for server-side development is how to trigger internal audit events:
        //
        //        AuditUtils.audit("1234-5678-abcd",    //PID
        //                "admin",  // caller
        //                "http://localhost:8080/KITDM/rest/audit/events",  //the resource
        //                "Java/1.8.0_60",  //agent
        //                "audit.test", //category
        //                AuditEvent.TYPE.CREATION, //event type
        //                AuditEvent.TRIGGER.INTERNAL,  //event trigger
        //                AuditDetail.factoryArgumentDetail(String.class.getCanonicalName(), "argument", "value"), //event detail #1
        //                AuditDetail.factoryCommentDetail("Hello World!"));    //event detail #2
        //
        //Almost all arguments should be obtained from the call. For REST endpoints, resource and agent argument could be obtained from the HTTPRequest, event details are typically the 
        //passed arguments. The PID identies the resource that is affected/created, the caller is the identified user who accessed the resource.
        //The event type should be chose according to the operation that should be captured. The trigger type is in this case always INTERNAL. Finally, the category is a bit tricky.
        //The first potion 'audit.' is configured for the audit service on the server side and determines the topic under which the audit events are published. 'audit' is the default value.
        //The second part, in our example 'test', currently has no influence but should be chosen for further categorization.
        //
        //If you take a look into the implementation of AuditUtils.audit() you'll see that there is nothing. The reason is, that each occurence of this method call is handled by an aspect putting 
        //the actual functionality in at compile time. The actual implementation is available in class edu.kit.dama.mdm.audit.aspect.AuditAspect
        //However, in order to active the so called weaving of aspects, the build process of the project has to be adapted. Therefor, the pom.xml of the module has to be modified as follows:
        //
        //        <build>
        //        <plugins>
        //        <!--Other build plugins-->
        //            <plugin>
        //                <groupId>org.codehaus.mojo</groupId>
        //                <artifactId>aspectj-maven-plugin</artifactId>
        //                <version>1.7</version>
        //                <configuration>
        //                    <complianceLevel>1.7</complianceLevel>
        //                    <aspectLibraries> 
        //                        <aspectLibrary>
        //                            <groupId>edu.kit.dama</groupId>
        //                            <artifactId>MDM-Audit</artifactId>
        //                        </aspectLibrary>
        //                    </aspectLibraries> 
        //                </configuration>
        //                <executions>
        //                    <execution>
        //                        <goals>
        //                            <goal>compile</goal>
        //                            <goal>test-compile</goal>
        //                        </goals>
        //                    </execution>
        //                </executions>
        //            </plugin>
        //        </plugins>
        //    </build>
        //
        //This will cause the AspectJ weaver to integrate the aspect code during compilation. If this not happens, the unchanged code will be called and nothing will happen.
    }

}
