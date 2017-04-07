/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.authorization.services.administration.impl;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import java.util.List;
import java.util.ArrayList;
import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author soc
 */
public class GroupServiceImplTest {

    GroupServiceImpl groupServiceInstance = new GroupServiceImpl();
    IAuthorizationContext ctx = TestUtil.sysCtx;

    /**
     *
     */
    public GroupServiceImplTest() {
    }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        PU.setPersistenceUnitName("AuthorizationUnit-Test");
    }

    /**
     *
     * @throws Exception
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        TestUtil.clearDB();
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }

    /**
     * Test of create method, of class GroupServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityAlreadyExistsException
     */
    @Test
    public void testCreate() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        new UserServiceImpl().register(mngrId, Role.MEMBER, ctx);
        groupServiceInstance.create(gid, mngrId, ctx);
    }

    /**
     * Test of create method, of class GroupServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityAlreadyExistsException.class)
    public void testReCreate() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        new UserServiceImpl().register(mngrId, Role.MEMBER, ctx);
        groupServiceInstance.create(gid, mngrId, ctx);
        groupServiceInstance.create(gid, mngrId, ctx);
    }

    /**
     * Test of create method, of class GroupServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityAlreadyExistsException
     */
    @Test(expected = EntityNotFoundException.class)
    @Ignore

    public void testCreateWithNonExistingUser() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        groupServiceInstance.create(gid, mngrId, ctx);
    }

    /**
     * Test of remove method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityNotFoundException
     */
    @Test
    @Ignore

    public void testRemove() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        new UserServiceImpl().register(mngrId, Role.MEMBER, ctx);
        groupServiceInstance.create(gid, mngrId, ctx);
        boolean remove = groupServiceInstance.remove(gid, ctx);
        assertEquals(remove, true);
    }

    /**
     * Test of remove method, of class GroupServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityNotFoundException.class)
    @Ignore

    public void testRemoveNonExistent() throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        boolean remove = groupServiceInstance.remove(gid, ctx);
        assertEquals(remove, false);
    }

    /**
     * Test of addUser method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testAddUser() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.register(mngrId, Role.MEMBER, ctx);

        groupServiceInstance.create(gid, mngrId, ctx);
        UserId userId = new UserId("user1");
        userServiceImpl.register(userId, Role.MEMBER, ctx);
        groupServiceInstance.addUser(gid, userId, Role.GUEST, ctx);
    }

    /**
     * Test of addUser method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityNotFoundException.class)
    @Ignore
    public void testAddUserWithNonExistingUser() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.register(mngrId, Role.MEMBER, ctx);
        groupServiceInstance.create(gid, mngrId, ctx);
        UserId userId = new UserId("user1");
        groupServiceInstance.addUser(gid, userId, Role.GUEST, ctx);
    }

    /**
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityAlreadyExistsException.class)
    @Ignore

    public void testAddUserWithAddedUser() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.register(mngrId, Role.MEMBER, ctx);
        groupServiceInstance.create(gid, mngrId, ctx);
        UserId userId = new UserId("user1");
        userServiceImpl.register(userId, Role.MEMBER, ctx);
        groupServiceInstance.addUser(gid, userId, Role.GUEST, ctx);
        groupServiceInstance.addUser(gid, userId, Role.GUEST, ctx);
    }

    /**
     * Test of addUser method, of class GroupServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityNotFoundException.class)
    public void testAddUserWithNonExistingGroup() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.register(mngrId, Role.MEMBER, ctx);
        //dont create group
        UserId userId = new UserId("user1");
        userServiceImpl.register(userId, Role.MEMBER, ctx);
        groupServiceInstance.addUser(gid, userId, Role.GUEST, ctx);
    }

    /**
     * Test of removeUser method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testRemoveUser() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.register(mngrId, Role.MEMBER, ctx);
        groupServiceInstance.create(gid, mngrId, ctx);
        UserId userId = new UserId("user1");
        userServiceImpl.register(userId, Role.MEMBER, ctx);
        groupServiceInstance.addUser(gid, userId, Role.GUEST, ctx);
        groupServiceInstance.removeUser(gid, userId, ctx);
    }

    /**
     * Test of getMaximumRole method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityNotFoundException
     */
    @Test
    public void testGetMaximumRole() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        Role expRole1 = Role.MEMBER;
        Role expRole2 = Role.MANAGER;
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.register(mngrId, expRole2, ctx);
        groupServiceInstance.create(gid, mngrId, ctx);
        UserId userId1 = new UserId("user1");
        userServiceImpl.register(userId1, expRole1, ctx);
        groupServiceInstance.addUser(gid, userId1, expRole2, ctx);
        UserId userId2 = new UserId("user2");
        userServiceImpl.register(userId2, expRole2, ctx);
        groupServiceInstance.addUser(gid, userId2, expRole1, ctx);

        assertEquals(groupServiceInstance.getMaximumRole(gid, mngrId, ctx), expRole2);
        assertEquals(groupServiceInstance.getMaximumRole(gid, userId1, ctx), expRole1);
        assertEquals(groupServiceInstance.getMaximumRole(gid, userId2, ctx), expRole1);
    }

    /**
     * Test of changeRole method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testChangeRole() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.register(mngrId, Role.MEMBER, ctx);
        groupServiceInstance.create(gid, mngrId, ctx);
        groupServiceInstance.changeRole(gid, mngrId, Role.ADMINISTRATOR, ctx);
    }

    /**
     * Test of getAllGroups method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws InterruptedException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testGetAllGroups() throws EntityAlreadyExistsException, EntityNotFoundException, InterruptedException, UnauthorizedAccessAttemptException {
        GroupId gid1 = new GroupId("group1");
        UserId mngrId = new UserId("mngr");
        new UserServiceImpl().register(mngrId, Role.MEMBER, ctx);
        groupServiceInstance.create(gid1, mngrId, ctx);
        GroupId gid2 = new GroupId("group2");
        groupServiceInstance.create(gid2, mngrId, ctx);
        List<GroupId> allGroups = groupServiceInstance.getAllGroupsIds(0, 3, ctx);
        assertEquals(2, allGroups.size());
        GroupId gid3 = new GroupId("group3");
        groupServiceInstance.create(gid3, mngrId, ctx);
        allGroups = groupServiceInstance.getAllGroupsIds(0, 3, ctx);
        assertEquals(3, allGroups.size());
        groupServiceInstance.remove(gid3, ctx);
        allGroups = groupServiceInstance.getAllGroupsIds(0, 2, ctx);
        allGroups.get(1).getStringRepresentation().equals(gid2.getStringRepresentation());
        assertEquals(2, allGroups.size());
    }

    /**
     * Test of getUsersIds method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testGetAllUsers() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        UserServiceImpl userServiceImpl = new UserServiceImpl();
        List<UserId> userIdList = new ArrayList<UserId>();
        for (int i = 0; i < 50; ++i) {
            userIdList.add(new UserId("UserId" + i));
        }
        userServiceImpl.register(userIdList.get(0), Role.MANAGER, ctx);
        for (int i = 1; i < 50; ++i) {
            userServiceImpl.register(userIdList.get(i), Role.MEMBER, ctx);
        }
        GroupId group = new GroupId("42");
        groupServiceInstance.create(group, userIdList.get(0), ctx);
        for (int i = 1; i < 50; ++i) {
            groupServiceInstance.addUser(group, userIdList.get(i), Role.MEMBER, ctx);
        }
        List<UserId> resultLists = groupServiceInstance.getUsersIds(group, 0, 50, ctx);
        assertTrue(resultLists.containsAll(userIdList));

    }

    /**
     * Test of membershipsOf method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testMembershipsOf() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        UserId mngr1 = new UserId("Mngr1");
        UserId mngr2 = new UserId("Mngr2");

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.register(mngr1, Role.MANAGER, ctx);
        userServiceImpl.register(mngr2, Role.MANAGER, ctx);

        List<GroupId> mngr1Groups = new ArrayList<GroupId>();
        mngr1Groups.add(new GroupId("G1"));
        mngr1Groups.add(new GroupId("G2"));
        mngr1Groups.add(new GroupId("G3"));
        GroupId group4 = new GroupId("G4");

        groupServiceInstance.create(mngr1Groups.get(0), mngr1, ctx);
        groupServiceInstance.create(mngr1Groups.get(1), mngr2, ctx);
        groupServiceInstance.create(mngr1Groups.get(2), mngr2, ctx);
        groupServiceInstance.create(group4, mngr2, ctx);

        for (int i = 1; i < mngr1Groups.size(); ++i) {
            groupServiceInstance.addUser(mngr1Groups.get(i), mngr1, Role.GUEST, ctx);
        }

        assertTrue(mngr1Groups.containsAll(groupServiceInstance.membershipsOf(mngr1, 0, 3, ctx)));

    }

    /**
     * Test of membershipsOf method, of class GroupServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testMembershipsWithMaximumRoleOf() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        UserId mngr1 = new UserId("Mngr1");
        UserId mngr2 = new UserId("Mngr2");

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        userServiceImpl.register(mngr1, Role.MANAGER, ctx);
        userServiceImpl.register(mngr2, Role.MANAGER, ctx);

        List<GroupId> mngr1GroupsMember = new ArrayList<GroupId>();
        mngr1GroupsMember.add(new GroupId("G1"));
        mngr1GroupsMember.add(new GroupId("G2"));
        mngr1GroupsMember.add(new GroupId("G3"));
        GroupId group4 = new GroupId("G4");

        groupServiceInstance.create(mngr1GroupsMember.get(0), mngr1, ctx);
        groupServiceInstance.create(mngr1GroupsMember.get(1), mngr2, ctx);
        groupServiceInstance.create(mngr1GroupsMember.get(2), mngr2, ctx);
        groupServiceInstance.create(group4, mngr2, ctx);

        for (int i = 1; i < mngr1GroupsMember.size(); ++i) {
            groupServiceInstance.addUser(mngr1GroupsMember.get(i), mngr1, Role.MEMBER, ctx);
        }
        groupServiceInstance.addUser(group4, mngr1, Role.GUEST, ctx);

        assertTrue(mngr1GroupsMember.containsAll(groupServiceInstance.membershipsOf(mngr1, Role.MEMBER, 0, 3, ctx)));
    }
}
