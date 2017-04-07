/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.authorization.services.administration.impl;

import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import java.util.List;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.entities.util.PU;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author soc
 */
public class ResourceServiceImplTest {

    ResourceServiceImpl resourceServiceInstance = new ResourceServiceImpl();
    IAuthorizationContext ctx = TestUtil.sysCtx;

    /**
     *
     */
    public ResourceServiceImplTest() {
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

    GroupId getRegisteredGroup() throws EntityAlreadyExistsException, edu.kit.dama.authorization.exceptions.EntityNotFoundException, UnauthorizedAccessAttemptException {
        GroupId gid = new GroupId("Group");
        UserId uid = new UserId("User");
        new UserServiceImpl().register(uid, Role.MANAGER, ctx);
        new GroupServiceImpl().create(gid, uid, ctx);
        return gid;
    }

    /**
     * Test of registerResource method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testRegisterResource_ResourceId() throws EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
    }

    /**
     *
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityAlreadyExistsException.class)
    public void testRegisterResource_ResourceId_Re() throws EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.registerResource(resourceId, ctx);
    }

    /**
     * Test of registerResource method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws edu.kit.dama.authorization.exceptions.EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testRegisterResource_3args() throws EntityAlreadyExistsException, edu.kit.dama.authorization.exceptions.EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, getRegisteredGroup(), Role.MANAGER, ctx);

    }

    /**
     * Test of registerResource method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws edu.kit.dama.authorization.exceptions.EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityNotFoundException.class)
    public void testRegisterResource_3args_GropupNotReg() throws EntityAlreadyExistsException, edu.kit.dama.authorization.exceptions.EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, new GroupId("bla"), Role.MANAGER, ctx);

    }

    /**
     * Test of registerResource method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    @Ignore
    public void testRegisterResource_4args() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        UserId user = new UserId("User");
        (new UserServiceImpl()).register(user, Role.MEMBER, ctx);
        resourceServiceInstance.registerResource(resourceId, Role.MEMBER, user, Role.GUEST, ctx);
    }

    /**
     * Test of remove method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityAlreadyExistsException
     */
    @Test
    public void testRemove() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.remove(resourceId, ctx);
    }

    /**
     * Test of remove method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityNotFoundException.class)
    public void testRemove_NotReg() throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.remove(resourceId, ctx);
    }

    /**
     * Test of createReference method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityNotFoundException
     */
    @Test
    public void testCreateReference() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        ReferenceId referenceId = new ReferenceId(resourceId, getRegisteredGroup());
        resourceServiceInstance.createReference(referenceId, Role.MEMBER, ctx);
    }

    /**
     * Test of createReference method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityAlreadyExistsException.class)
    public void testCreateReference_Exists() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        ReferenceId referenceId = new ReferenceId(resourceId, getRegisteredGroup());
        resourceServiceInstance.createReference(referenceId, Role.MEMBER, ctx);
        resourceServiceInstance.createReference(referenceId, Role.MEMBER, ctx);
    }

    /**
     * Test of deleteReference method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testDeleteReference() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        ReferenceId referenceId = new ReferenceId(resourceId, getRegisteredGroup());
        resourceServiceInstance.createReference(referenceId, Role.MEMBER, ctx);
        resourceServiceInstance.deleteReference(referenceId, ctx);
    }

    /**
     * Test of deleteReference method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityNotFoundException.class)
    @Ignore
    public void testDeleteReference_No() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        ReferenceId referenceId = new ReferenceId(resourceId, getRegisteredGroup());
//        resourceServiceInstance.createReference(referenceId, Role.MEMBER, ctx);
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException ex) {
            Logger.getLogger(ResourceServiceImplTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        resourceServiceInstance.deleteReference(referenceId, ctx);
    }

    /**
     * Test of getReferenceRestriction method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testGetReferenceRestriction() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        ReferenceId referenceId = new ReferenceId(resourceId, getRegisteredGroup());
        resourceServiceInstance.createReference(referenceId, role, ctx);
        assertEquals(resourceServiceInstance.getReferenceRestriction(referenceId, ctx), role);

    }

    /**
     * Test of changeReferenceRestriction method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testChangeReferenceRestriction() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        Role newRole = Role.NO_ACCESS;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        ReferenceId referenceId = new ReferenceId(resourceId, getRegisteredGroup());
        resourceServiceInstance.createReference(referenceId, role, ctx);
        assertEquals(resourceServiceInstance.getReferenceRestriction(referenceId, ctx), role);
        resourceServiceInstance.changeReferenceRestriction(referenceId, newRole, ctx);
        assertEquals(resourceServiceInstance.getReferenceRestriction(referenceId, ctx), newRole);
    }

    /**
     * Test of getReferences method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testGetReferences() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        List<SecurableResourceId> resourceIds = new ArrayList<SecurableResourceId>();
        for (int i = 0; i < 5; ++i) {
            resourceIds.add(new SecurableResourceId("mydomain", "myresId" + i));
            resourceServiceInstance.registerResource(resourceIds.get(i), ctx);
        }
        List<GroupId> groupIds = new ArrayList<GroupId>();
        List<UserId> userIds = new ArrayList<UserId>();
        UserServiceImpl userService = new UserServiceImpl();
        GroupServiceImpl groupService = new GroupServiceImpl();
        for (int i = 0; i < 5; ++i) {
            groupIds.add(new GroupId("Group" + i));
            userIds.add(new UserId("User" + i));
            userService.register(userIds.get(i), Role.MANAGER, ctx);
            groupService.create(groupIds.get(i), userIds.get(i), ctx);
        }
        int n = 3;
        for (int i = 0; i < n; ++i) {
            resourceServiceInstance.createReference(
                    new ReferenceId(resourceIds.get(0),
                            groupIds.get(i)), role, ctx);
        }

        int m = 2;
        for (int i = 2; i < m + 2; ++i) {
            resourceServiceInstance.createReference(
                    new ReferenceId(resourceIds.get(i),
                            groupIds.get(i)), role, ctx);
            assertEquals(resourceServiceInstance.
                    getReferences(resourceIds.get(i), ctx).size(), 1);
        }
        List<ReferenceId> references0 = resourceServiceInstance.getReferences(resourceIds.get(0), ctx);
        assertEquals(references0.size(), n);
        resourceServiceInstance.deleteReference(new ReferenceId(resourceIds.get(0), groupIds.get(0)), ctx);
        List<ReferenceId> references01 = resourceServiceInstance.getReferences(resourceIds.get(0), ctx);
        assertEquals(references01.size(), n - 1);

    }

    /**
     * Test of allowGrants method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityNotFoundException
     */
    @Test
    public void testAllowGrants() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role, ctx);
    }

    /**
     * Test of addGrant method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testAddGrant() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role, ctx);

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserId userId = new UserId("User");
        userServiceImpl.register(userId, role, ctx);

        resourceServiceInstance.addGrant(resourceId, userId, role, ctx);
    }

    /**
     * Test of addGrant method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = EntityNotFoundException.class)
    public void testAddGrant_NotAllowed() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role, ctx);

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserId userId = new UserId("User");
        //userServiceImpl.register(userId, role, ctx);        
        resourceServiceInstance.addGrant(resourceId, userId, role, ctx);
    }

    /**
     * Test of addGrant method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testAddGrant_AlreadyExists() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role, ctx);

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserId userId = new UserId("User");
        userServiceImpl.register(userId, role, ctx);
        resourceServiceInstance.addGrant(resourceId, userId, role, ctx);
        //should be possible two times whereas the second time is ignored
        resourceServiceInstance.addGrant(resourceId, userId, role, ctx);
    }

    /**
     * Test of getGrantsRestriction method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testGetGrantsRestriction() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role, ctx);

        assertEquals(role, resourceServiceInstance.getGrantsRestriction(resourceId, ctx));
    }

    /**
     * Test of getGrantsRestriction method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testChangeGrantsRestriction() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        Role role1 = Role.MEMBER;
        Role role2 = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role1, ctx);

        assertEquals(role1, resourceServiceInstance.getGrantsRestriction(resourceId, ctx));

        resourceServiceInstance.changeGrantsRestriction(resourceId, role2, ctx);
        assertEquals(role2, resourceServiceInstance.getGrantsRestriction(resourceId, ctx));
    }

    /**
     * Test of getGrantRole method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testGetGrantRole() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        Role roleAllow = Role.MEMBER;
        Role roleUser1 = Role.MANAGER;
        Role roleUser2 = Role.MEMBER;
        Role grantedRole1 = Role.MEMBER;
        Role grantedRole2 = Role.MANAGER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, roleAllow, ctx);

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserId userId1 = new UserId("User1");
        userServiceImpl.register(userId1, roleUser1, ctx);
        UserId userId2 = new UserId("User2");
        userServiceImpl.register(userId2, roleUser2, ctx);

        resourceServiceInstance.addGrant(resourceId, userId1, grantedRole1, ctx);
        resourceServiceInstance.addGrant(resourceId, userId2, grantedRole2, ctx);
        assertEquals(grantedRole1, resourceServiceInstance.getGrantRole(resourceId, userId1, ctx));
        assertEquals(grantedRole2, resourceServiceInstance.getGrantRole(resourceId, userId2, ctx));
    }

    /**
     * Test of changeGrant method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testChangeGrant() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        System.out.println("changeGrant");
        Role role1 = Role.MEMBER;
        Role role2 = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role1, ctx);

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserId userId = new UserId("User");
        userServiceImpl.register(userId, role1, ctx);

        resourceServiceInstance.addGrant(resourceId, userId, role1, ctx);
        assertEquals(role1, resourceServiceInstance.getGrantRole(resourceId, userId, ctx));
        resourceServiceInstance.changeGrant(resourceId, userId, role2, ctx);
        assertEquals(role2, resourceServiceInstance.getGrantRole(resourceId, userId, ctx));
    }

    /**
     * Test of revokeGrant method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testRevokeGrant() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role, ctx);

        UserId user = new UserId("User1");
        UserServiceImpl userService = new UserServiceImpl();
        userService.register(user, role, ctx);

        resourceServiceInstance.addGrant(resourceId, user, role, ctx);

        resourceServiceInstance.revokeGrant(resourceId, user, ctx);

        assertEquals(Role.NO_ACCESS, resourceServiceInstance.getGrantRole(resourceId, user, ctx));
    }

    /**
     * Test of revokeAllGrants method, of class ResourceServiceImpl.
     *
     * TODO: Check why IllegalArgumentException is thrown instead of
     * UnsupportedOperation
     *
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityAlreadyExistsException
     */
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void testRevokeAllAndDisallowGrants() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role, ctx);

        UserId user1 = new UserId("User1");
        UserId user2 = new UserId("User2");
        UserServiceImpl userService = new UserServiceImpl();
        userService.register(user1, role, ctx);
        userService.register(user2, role, ctx);

        resourceServiceInstance.addGrant(resourceId, user1, role, ctx);
        resourceServiceInstance.addGrant(resourceId, user2, role, ctx);

        resourceServiceInstance.revokeAllAndDisallowGrants(resourceId, ctx);

        assertFalse(resourceServiceInstance.grantsAllowed(resourceId, ctx));
        resourceServiceInstance.getGrantRole(resourceId, user1, ctx);
    }

    /**
     * Test of getGrantRole method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testGetGrantRoleWithoutUserInGrantSet() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);
        resourceServiceInstance.allowGrants(resourceId, role, ctx);

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserId userId1 = new UserId("User1");
        userServiceImpl.register(userId1, role, ctx);

        resourceServiceInstance.addGrant(resourceId, userId1, role, ctx);

        UserId userId2 = new UserId("User2");
        userServiceImpl.register(userId2, role, ctx);
        assertEquals(Role.NO_ACCESS, resourceServiceInstance.getGrantRole(resourceId, userId2, ctx));
    }

    /**
     * Test of getGrantRole method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetGrantRoleWithoutAllowedGrants() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserId userId1 = new UserId("User1");
        userServiceImpl.register(userId1, Role.MANAGER, ctx);

        resourceServiceInstance.getGrantRole(resourceId, userId1, ctx);
    }

    /**
     * Test of getGrants method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testGetGrants() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        Role role = Role.MEMBER;
        SecurableResourceId resourceId1 = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId1, ctx);
        resourceServiceInstance.allowGrants(resourceId1, role, ctx);

        UserServiceImpl userServiceImpl = new UserServiceImpl();
        UserId userId1 = new UserId("User1");
        userServiceImpl.register(userId1, role, ctx);

        resourceServiceInstance.addGrant(resourceId1, userId1, role, ctx);
        List<Grant> grants = resourceServiceInstance.getGrants(resourceId1, ctx);
        assertEquals(grants.size(), 1);

        SecurableResourceId resourceId2 = new SecurableResourceId("mydomain", "myresId2");
        resourceServiceInstance.registerResource(resourceId2, ctx);
        resourceServiceInstance.allowGrants(resourceId2, role, ctx);
        grants = resourceServiceInstance.getGrants(resourceId2, ctx);
        assertEquals(grants.size(), 0);

    }

    /**
     * Test of getGrants method, of class ResourceServiceImpl.
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testGetGrantsWithoutAllowedGrants() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId1 = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId1, ctx);

        assertTrue(resourceServiceInstance.getGrants(resourceId1, ctx).isEmpty());
    }

    /**
     * Test of getAuthorizedUsers method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityNotFoundException
     */
    @Test
    public void testGetAuthorizedUsers() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);

        GroupServiceImpl groupServiceInstance = new GroupServiceImpl();
        UserServiceImpl userServiceInstance = new UserServiceImpl();

        GroupId group1 = new GroupId("Group1");
        GroupId group2 = new GroupId("Group2");

        UserId mngr1 = new UserId("Mngr1");
        UserId mngr2 = new UserId("Mngr2");

        userServiceInstance.register(mngr1, Role.MANAGER, ctx);
        userServiceInstance.register(mngr2, Role.MANAGER, ctx);

        groupServiceInstance.create(group1, mngr1, ctx);
        groupServiceInstance.create(group2, mngr2, ctx);

        resourceServiceInstance.createReference(new ReferenceId(resourceId, group1), Role.MANAGER, ctx);

        UserId usr1 = new UserId("Usr1");
        userServiceInstance.register(usr1, Role.MEMBER, ctx);
        groupServiceInstance.addUser(group1, usr1, Role.MEMBER, ctx);
        List<UserId> authorizedUsers = resourceServiceInstance.getAuthorizedUsers(resourceId, Role.MEMBER, ctx);
        assertEquals(2, authorizedUsers.size());
    }

    /**
     * Test of getAuthorizedGroups method, of class ResourceServiceImpl.
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    @Ignore
    public void testGetAuthorizedGroups() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        SecurableResourceId resourceId = new SecurableResourceId("mydomain", "myresId1");
        resourceServiceInstance.registerResource(resourceId, ctx);

        GroupServiceImpl groupServiceInstance = new GroupServiceImpl();
        UserServiceImpl userServiceInstance = new UserServiceImpl();

        GroupId group1 = new GroupId("Group1");
        GroupId group2 = new GroupId("Group2");
        GroupId group3 = new GroupId("Group2");

        UserId mngr1 = new UserId("Mngr1");
        UserId mngr2 = new UserId("Mngr2");

        userServiceInstance.register(mngr1, Role.MANAGER, ctx);
        userServiceInstance.register(mngr2, Role.MANAGER, ctx);

        groupServiceInstance.create(group1, mngr1, ctx);
        groupServiceInstance.create(group2, mngr2, ctx);

        resourceServiceInstance.createReference(new ReferenceId(resourceId, group1), Role.MANAGER, ctx);
        resourceServiceInstance.createReference(new ReferenceId(resourceId, group2), Role.MEMBER, ctx);

        UserId usr1 = new UserId("Usr1");
        userServiceInstance.register(usr1, Role.MEMBER, ctx);
        groupServiceInstance.addUser(group1, usr1, Role.MEMBER, ctx);
        List<GroupId> authorizedGroups = resourceServiceInstance.getAuthorizedGroups(resourceId, Role.MEMBER, ctx);
        assertEquals(2, authorizedGroups.size());
        authorizedGroups = resourceServiceInstance.getAuthorizedGroups(resourceId, Role.MANAGER, ctx);
        assertEquals(1, authorizedGroups.size());

    }

}
