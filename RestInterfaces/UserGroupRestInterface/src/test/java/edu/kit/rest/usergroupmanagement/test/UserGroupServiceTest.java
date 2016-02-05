/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.rest.usergroupmanagement.test;

import com.sun.jersey.test.framework.JerseyTest;
import edu.kit.dama.rest.admin.types.UserGroupWrapper;
import edu.kit.dama.rest.admin.types.UserDataWrapper;
import edu.kit.dama.rest.admin.client.impl.UserGroupRestClient;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.util.Constants;
import java.io.IOException;
import java.net.ServerSocket;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mf6319
 */
public class UserGroupServiceTest extends JerseyTest {

    private static UserGroupRestClient client;

    public UserGroupServiceTest() throws Exception {
        super("edu.kit.rest.usergroupmanagement.test");
    }

    @Override
    protected int getPort(int defaultPort) {
        ServerSocket server = null;
        int port = -1;
        try {
            server = new ServerSocket(defaultPort);
            port = server.getLocalPort();
        } catch (IOException e) {
            // ignore
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        if ((port != -1) || (defaultPort == 0)) {
            return port;
        }
        return getPort(0);
    }

    @BeforeClass
    public static void initClient() {
        client = new UserGroupRestClient("http://localhost:9998/UserGroupTest", new SimpleRESTContext("secret", "secret"));
    }

    @Test
    public void testGetGroups() {
        int cnt = client.getAllGroups(1, Integer.MAX_VALUE).getCount();
        assertEquals(Boolean.TRUE, cnt == 10 || cnt == 11);
    }

    @Test
    public void testGetUsers() {
        int cnt = client.getAllUsers(Constants.USERS_GROUP_ID, 1, Integer.MAX_VALUE).getCount();
        assertEquals(Boolean.TRUE, cnt == 10 || cnt == 11);
        //second way to get users of group USERS
        cnt = client.getAllUsers(1, Integer.MAX_VALUE, null).getCount();
        assertEquals(Boolean.TRUE, cnt == 10 || cnt == 11);
    }

    @Test
    public void testGetUsersWithNullArgument() {
        //should user group USERS to perform the request. This should change nothing in this environment.
        int users = client.getAllUsers(Constants.USERS_GROUP_ID, 1, Integer.MAX_VALUE).getCount();
        assertEquals(Integer.valueOf(users), client.getAllUsers(null, 0, Integer.MAX_VALUE).getCount());
    }

    @Test
    public void testGetGroupById() {
        UserGroupWrapper wrapper = client.getGroupById(1);
        assertEquals(Integer.valueOf(1), wrapper.getCount());
        assertEquals(Constants.USERS_GROUP_ID, wrapper.getEntities().get(0).getGroupId());
        assertEquals("Users", wrapper.getEntities().get(0).getGroupName());
        wrapper = client.getGroupById(2);
        assertEquals(Integer.valueOf(1), wrapper.getCount());
        assertEquals("group2", wrapper.getEntities().get(0).getGroupId());
        assertEquals("Group2", wrapper.getEntities().get(0).getGroupName());
    }

    @Test
    public void testGetGroupCount() {
        int cnt = client.getGroupCount().getCount();
        assertEquals(Boolean.TRUE, cnt == 10 || cnt == 11);
    }

    @Test
    public void testGetUserById() {
        assertEquals(Integer.valueOf(1), client.getUserById(1l).getCount());
        assertEquals(Integer.valueOf(0), client.getUserById(4711l).getCount());
    }

    @Test
    public void testGetUserCount() {
        int cnt = client.getUserCount().getCount();
        assertEquals(Boolean.TRUE, cnt == 10 || cnt == 11);
    }

    @Test
    public void testGetUsersByGroup() {
        int cnt = client.getUsersOfGroup(1l, 0, Integer.MAX_VALUE).getCount();
        assertEquals(Boolean.TRUE, cnt == 10 || cnt == 11);
        assertEquals(Integer.valueOf(0), client.getUsersOfGroup(4711l, 0, Integer.MAX_VALUE).getCount());
    }

    @Test
    public void testGroupWorkflow() {
        int groupCountBefore = client.getGroupCount().getCount();
        //add group
        UserGroupWrapper wrapper = client.addGroup("someGroup", "Some Group", "No description");
        //now there are 11 groups in sum
        assertEquals(Integer.valueOf(groupCountBefore + 1), client.getGroupCount().getCount());
        //1 new entity is returned
        assertEquals(Integer.valueOf(1), wrapper.getCount());
        //check returned entity
        UserGroup group = wrapper.getEntities().get(0);
        //name is not set due to the 'simple' object graph
        assertEquals("Some Group", group.getGroupName());
        //store id and query again for details
        long groupId = group.getId();
        wrapper = client.getGroupById(groupId);
        //one result by id
        assertEquals(Integer.valueOf(1), wrapper.getCount());
        //check group
        group = wrapper.getEntities().get(0);
        assertEquals("someGroup", group.getGroupId());
        assertEquals("Some Group", group.getGroupName());
        assertEquals("No description", group.getDescription());
        //update group and set new name
        wrapper = client.updateGroup(group.getId(), "New Name", group.getDescription());
        //one modified row
        assertEquals(Integer.valueOf(1), wrapper.getCount());
        //same id as before
        assertEquals(Long.valueOf(groupId), wrapper.getEntities().get(0).getId());
        //check result for changes
        group = wrapper.getEntities().get(0);
        assertEquals("someGroup", group.getGroupId());
        assertEquals("New Name", group.getGroupName());
        assertEquals("No description", group.getDescription());
    }

    @Test
    public void testUserWorkflow() {
        int userCountBefore = client.getUserCount().getCount();
        //add user
        UserDataWrapper wrapper = client.addUser(Constants.USERS_GROUP_ID, "Some", "User", "some.user@kit.edu", "someUser");
        //now there are 11 users in sum
        assertEquals(Integer.valueOf(userCountBefore + 1), client.getUserCount().getCount());
        //1 new entity is returned
        assertEquals(Integer.valueOf(1), wrapper.getCount());
        //check returned entity
        UserData user = wrapper.getEntities().get(0);
        assertEquals("Some", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("some.user@kit.edu", user.getEmail());
        assertEquals("someUser", user.getDistinguishedName());
        //store id and query again for details
        long userId = user.getUserId();
        wrapper = client.getUserById(userId);
        //one result by id
        assertEquals(Integer.valueOf(1), wrapper.getCount());
        //check group
        user = wrapper.getEntities().get(0);
        assertEquals("Some", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("some.user@kit.edu", user.getEmail());
        assertEquals("someUser", user.getDistinguishedName());
        //update user and set new email
        wrapper = client.updateUser(Constants.USERS_GROUP_ID, userId, user.getFirstName(), user.getLastName(), "new.mail@kit.edu");
        //one modified row
        assertEquals(Integer.valueOf(1), wrapper.getCount());
        //same id as before
        assertEquals(Long.valueOf(userId), wrapper.getEntities().get(0).getUserId());
        //check result for changes
        user = wrapper.getEntities().get(0);
        assertEquals("Some", user.getFirstName());
        assertEquals("User", user.getLastName());
        assertEquals("new.mail@kit.edu", user.getEmail());
        assertEquals("someUser", user.getDistinguishedName());
    }

    @Test
    public void testUserGroupWorkflow() {
        //pseudo-add user (should return 1 modified row)
        assertEquals(Integer.valueOf(1), client.addUserToGroup(1l, "1").getCount());
        //should return 0 as there is no group with id Long.MAX_VALUE
        assertEquals(Integer.valueOf(0), client.addUserToGroup(Long.MAX_VALUE, "1").getCount());
        //should return 0 as there is no user with id Long.MAX_VALUE
        assertEquals(Integer.valueOf(0), client.addUserToGroup(1l, Long.toString(Long.MAX_VALUE)).getCount());

        //pseudo-remove user (should return 1 modified row)
        assertEquals(Integer.valueOf(1), client.removeUserFromGroup(1l, 1l).getCount());
        //should return 0 as there is no group with id Long.MAX_VALUE
        assertEquals(Integer.valueOf(0), client.removeUserFromGroup(Long.MAX_VALUE, 1l).getCount());
        //should return 0 as there is no user with id Long.MAX_VALUE
        assertEquals(Integer.valueOf(0), client.removeUserFromGroup(1l, Long.MAX_VALUE).getCount());
    }
}
