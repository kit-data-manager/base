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

import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.admin.client.impl.UserGroupRestClient;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.rest.admin.types.UserGroupWrapper;
import edu.kit.dama.util.Constants;

/**
 *
 * @author jejkal
 */
public class UserGroupManagement extends BaseMetadataCreation {

    public void performUsersGroupManagement() {
        UserGroupRestClient userGroupClient = new UserGroupRestClient(restBaseUrl + "/rest/usergroup/", context);

        //At first we obtain a list of all groups and the users who are members of the respective group.
        UserGroupWrapper groupWrapper = userGroupClient.getAllGroups(0, Integer.MAX_VALUE);
        System.out.println("The following groups were found:");
        for (UserGroup group : groupWrapper.getEntities()) {
            System.out.println(" Name: " + group.getGroupName());
            UserDataWrapper members = userGroupClient.getUsersOfGroup(group.getId(), 0, Integer.MAX_VALUE);
            System.out.println(" The group has the following members:");
            for (UserData user : members.getEntities()) {
                System.out.println("   - " + user.getFullname() + " (" + user.getDistinguishedName() + ")");
            }
        }

        //We can also obtain a list of all users registered at the accessed KIT DM instance. 
        //Be aware, that most parts of the UserGroupService have access restrictions. Listing all users for example is only
        //possible for someone who has at least MANAGER privileges. This is determined based on the calling user and the group
        //defining the authorization context.
        UserDataWrapper userWrapper = userGroupClient.getAllUsers(Constants.USERS_GROUP_ID, 0, Integer.MAX_VALUE);
        for (UserData user : userWrapper.getEntities()) {
            System.out.println(" - " + user.getFullname() + " (" + user.getDistinguishedName() + ")");
        }

        //Furthermore, it is possible to create users using the RESTful service as follows: 
        //The newUserIdentifier is the unique identifier of the user. It can be any string, so it is also possible to 
        //re-use external identifiers like an ORCID or an X.500 distinguished name.
        String newUserIdentifier = "testUser";
        UserDataWrapper newUser = userGroupClient.addUser(Constants.USERS_GROUP_ID, "Test", "User", "test@mail.org", newUserIdentifier);
        newUserIdentifier = newUser.getEntities().get(0).getDistinguishedName();
        long userId = newUser.getEntities().get(0).getUserId();

        System.out.println("A new user has been created with the following information:");
        System.out.println(" Id: " + userId);
        System.out.println(" Distinguished Name: " + newUserIdentifier);
        System.out.println(" Full Name: " + newUser.getEntities().get(0).getFullname());
        System.out.println(" eMail: " + newUser.getEntities().get(0).getEmail());

        //Be aware that users created via the RESTful service are 
        //not able to login, e.g. at the AdminUI, nor are they able to access the RESTful services as no OAuth credentials are available for them.
        //Assigning credentials to users can be done only by using the MetadataManagement via its Java APIs as follows:
        //At first we create a MetaDataManager instance using our userId and groupId to authorize the subsequent calls.
//        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(new AuthorizationContext(new UserId("admin"), new GroupId(Constants.USERS_GROUP_ID), Role.ADMINISTRATOR));
//        try {
//            //No, we create a new ServiceAccessToken entity. These tokens can be used to store access credentials in the database.
//            //All credentials are associated with a specific user and service. For the AdminUI login the serviceId is available at 
//            //Constants.MAIN_LOGIN_SERVICE_ID. The userId is the distinguished name of the user we just created.
//            ServiceAccessToken token = new ServiceAccessToken(newUserIdentifier, Constants.MAIN_LOGIN_SERVICE_ID);
//            //Finally, key and secret have to be assigned. Their values depend on the service they are used for. For
//            //the main login, the key is the email of the user and the value is the password.
//            token.setTokenKey(newUser.getEntities().get(0).getEmail());
//            token.setTokenSecret("ThePassword");
//            //We now save the token in the database. Afterwards, the user will be able to login at the AdminUI.
//            mdm.save(token);
//        } catch (UnauthorizedAccessAttemptException ex) {
//            //We arrive here, if the context provided while creating the MetaDataManager instance is not authorized to persist new entities in the database.
//        } finally {
//            //Finally, we have to close the connection to the database.
//            mdm.close();
//        }
        //Updating users is done in a similar way. 
        //Be aware that changing the email address is allowed but this won't affect the ServiceAccessToken created above. Therefore,  
        //the token has to be changed also or the user will only be able to login using the old email address.
        newUser = userGroupClient.updateUser(Constants.USERS_GROUP_ID, userId, "John", "Doe", "test@mail.org");
        newUserIdentifier = newUser.getEntities().get(0).getDistinguishedName();
        userId = newUser.getEntities().get(0).getUserId();

        System.out.println("A updated user has the following information:");
        System.out.println(" Id: " + userId);
        System.out.println(" Distinguished Name: " + newUserIdentifier);
        System.out.println(" Full Name: " + newUser.getEntities().get(0).getFullname());
        System.out.println(" eMail: " + newUser.getEntities().get(0).getEmail());

        //We can also add a new user group.
        UserGroupWrapper wrapper = userGroupClient.addGroup("uniqueId", "A custom group", "A custom group added via RESTful service interface.");
        long groupId = wrapper.getEntities().get(0).getId();

        System.out.println("A new group with id " + groupId + " has been created.");

        //Now, we add the created user to the previously created group. The result count is the number of affected rows. 
        //If 1 is returned, the user has been added to the group. If 0 is returned, the user probably was already member of the group.
        int modified = userGroupClient.addUserToGroup(groupId, userId).getCount();
        System.out.println("The user with id " + userId + ((modified == 1) ? " is now member of " : " was already member of ") + "group with id " + groupId + ".");

        //We can also remove a user from a group. Please be aware that we now have to use the numeric id of the user instead of the distinguished name.
        modified = userGroupClient.removeUserFromGroup(groupId, userId).getCount();
        System.out.println("The user with id " + userId + ((modified == 1) ? " is not longer member of " : " was no member ") + "group with id " + groupId + ".");

        //Finally, it is also possible to deactivate users. From the interface perspective this operation is implemented as DELETE operation.
        //However, deleting a user in a repository system can be a complex process hard to automate as this may affect also the content.
        //Therefor, this process should be carried out in a manual or supervised way by a privileged user using appropriate tools.
        System.out.println("Deactivating user with id " + userId);
        System.out.println("User has " + ((userGroupClient.removeUser(Constants.USERS_GROUP_ID, userId) == false) ? "not been deactivated." : "been successfully deactivated."));
    }

}
