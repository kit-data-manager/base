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

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.rest.sharing.client.impl.SharingRestClient;
import edu.kit.dama.rest.sharing.types.GrantSetWrapper;
import edu.kit.dama.rest.sharing.types.GrantWrapper;
import edu.kit.dama.rest.sharing.types.GroupIdWrapper;
import edu.kit.dama.rest.sharing.types.UserIdWrapper;
import edu.kit.dama.util.Constants;

/**
 * For a data repository sharing access to resources might be one of the most
 * important features. Of course, providing the entire content for open access
 * is way easier but for research data not always wanted. KIT DM offers a quite
 * sophisticated, generic and fine-grained system for sharing resources. It is
 * possible to share resources with user groups or with single users, where
 * shares for single users have priority compared to group shares.
 *
 * There are some terms one has to understand in order to understand how sharing
 * works. The entire sharing process is based on resources having a unique
 * resource id consisting of a domain and a domain unique id. This concept is
 * generic, for Java entities we are using the fully qualified package name as
 * domain and the class name as identifier.
 *
 * If a resource is shared with a group a so called 'resoure reference' is
 * created, which is basically a combination of resource id, the respective group
 * id and an assigned role valid for accessing the resource by the group's
 * users.
 *
 * If a resource is shared with a single user, a so called 'grant' is created,
 * which consists of a user id, the assigned role and a 'grant set' id. A 'grant
 * set' is the collection of all grants belonging to a resource.
 *
 * With these information it should now be possible to understand the code
 * below. Actually, the sharing concept is implemented as independent service
 * and can be used to maintain sharing information to arbitrary resources.
 * However, this service is also tightly integrated into internal KIT DM
 * workflows in order to authorize access to some resources (e.g. study,
 * investigation and digital object).
 *
 * @author jejkal
 */
public class DigitalObjectSharing extends BaseMetadataCreation {

    public void shareDigitalObject() {
        //perform the base metadata creation from the super class in order to have a digital object. We skip the data ingest for this example.      
        createBaseMetadata();

        SharingRestClient sharingClient = new SharingRestClient(restBaseUrl + "/rest/sharing/", context);
        //Obtain the securable resource id of the created digital object. All operations of the sharing service are related to this resource id.
        //This allows to use this service to secure literally all entities implementing the ISecurableResource interface providing such 
        //securable resource id the same way. 
        SecurableResourceId resourceId = newDigitalObject.getSecurableResourceId();

        //Securable resources can be shared in two different ways: with entire groups and with single users. 
        //E.g. a new securable resource registered in a default KIT DM instance will be made accessible with MANAGER permissions (read + write access to metadata)
        //by the entire group defined by the authorization context used during registration. Therefor, the following call should return our default groupId 'USERS"
        //as the digital object 'newDigitalObject' has been created using this group in the authorization context.
        GroupIdWrapper groupWrapper = sharingClient.getReferencedGroups(resourceId.getDomain(), resourceId.getDomainUniqueId(), Role.MANAGER, Constants.USERS_GROUP_ID, context);
        System.out.println("MANAGER permissions to the resource with resource id " + resourceId + " is granted to the following group(s):");
        for (GroupId g : groupWrapper.getEntities()) {
            System.out.println(" - " + g);
        }

        //Besides permissions for entire groups KIT DM allows to assign permissions to single users based on the userId. 
        //Let's assign GUEST permissions (read) for the another user with userId 'anotherUser' (Attention: The userId 'anotherUser' might not exist!).
        //The provided groupId (Constants.USERS_GROUP_ID) is just necessary to         //check whether the caller defined by 'context' is authorized to perform the requested authorization.
        GrantWrapper grantWrapper = sharingClient.createGrant(resourceId.getDomain(), resourceId.getDomainUniqueId(), "anotherUser", Constants.USERS_GROUP_ID, Role.GUEST, context);
        Grant grant = grantWrapper.getEntities().get(0);
        System.out.println("Successfully created grant with id " + grant.getId() + " and role " + grant.getGrantedRole());

        //It is also possible to modify an existing grant, in our case we change the eligible role for 'anotherUser' from GUEST (read access) to member (read/write access).
        grantWrapper = sharingClient.updateGrant(grant.getId(), Constants.USERS_GROUP_ID, Role.MEMBER, context);
        grant = grantWrapper.getEntities().get(0);
        System.out.println("Successfully updated grant with id " + grant.getId() + " to role " + grant.getGrantedRole());

        //All grants assigned to a specific resource are organized in a GrantSet.
        //Now, let's obtain all grants for our resource 'newDigitalObject'.
        GrantSetWrapper grantSetWrapper = sharingClient.getGrantSetForResource(resourceId.getDomain(), resourceId.getDomainUniqueId(), Constants.USERS_GROUP_ID, context);
        System.out.println("The following grants are assigned to the resource with resource id " + resourceId + ":");
        for (Grant g : grantSetWrapper.getEntities().get(0).getGrants()) {
            System.out.println(" - " + g.getGrantee().getUserId() + " (Role: " + g.getGrantedRole() + ")");
        }

        //Furthermore, it is also possible to obtain the list of users allowed to access a resource at least with the provided role.
        UserIdWrapper userWrapper = sharingClient.getAuthorizedUsers(resourceId.getDomain(), resourceId.getDomainUniqueId(), Role.GUEST, Constants.USERS_GROUP_ID, context);
        System.out.println("The following users are authorized to access the resource with resource id " + resourceId + " with at least GUEST (read) permissions:");
        for (UserId id : userWrapper.getEntities()) {
            System.out.println(" - " + id);
        }

        //In case a resource should not be shared with a specific user any longer it is of course also possible to remove grants using its id. 
        sharingClient.revokeGrant(grant.getId(), Constants.USERS_GROUP_ID, context);

        //If a resource should be removed it is recommended to revoke all grants and delete all references, first. In order to revoke all grants, 'revokeAllGrants' can be used.
        sharingClient.revokeAllGrants(resourceId.getDomain(), resourceId.getDomainUniqueId(), Constants.USERS_GROUP_ID, context);

        //References must be deleted one by one as follows.
        //-->Please keep in mind that removing all references will cause the object to be no longer accessible!
        //-->Thus, removing all base metadata and all associated information from the database and the disk would be a detached workflow without any authorization.
        for (GroupId g : groupWrapper.getEntities()) {
            sharingClient.deleteReference(resourceId.getDomain(), resourceId.getDomainUniqueId(), g.getStringRepresentation(), context);
        }

    }

}
