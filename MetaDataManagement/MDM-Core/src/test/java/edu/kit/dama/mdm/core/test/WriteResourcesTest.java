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
package edu.kit.dama.mdm.core.test;

import edu.kit.dama.mdm.core.test.entities.SecurityTestEntity;
import edu.kit.dama.mdm.core.test.entities.TestEntity;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityExistsException;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Try to persist several entities with different users. And then filter them by
 * different users in different groups.
 *
 * @author hartmann-v
 */
@RunWith(Parameterized.class)
public class WriteResourcesTest extends SecurityUtil {

    private static volatile boolean initialized = false;
    private static volatile List<SecurityTestEntity> allSecurableEntities = new ArrayList<SecurityTestEntity>();
    private static volatile List<TestEntity> allEntities = new ArrayList<TestEntity>();
    private int noOfResults;
    /**
     * For logging purposes.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(WriteResourcesTest.class);

    public WriteResourcesTest(Role resourceUserRole, Role resourceGroupRole, UserId userId, GroupId groupId, Role userRole, int noOfResults) {
        this.resourceUserRole = resourceUserRole;
        this.resourceGroupRole = resourceGroupRole;
        this.userId = userId;
        this.groupId = groupId;
        this.noOfResults = noOfResults;
        this.userRole = userRole;
    }

    //<editor-fold defaultstate="collapsed" desc="parameters for the testclass">
    @Parameterized.Parameters
    public static List<Object[]> testData() {
        return Arrays.asList(
                //Resource User Role, Resource Group Role, Context User, Context Group, Success.,

                //            new Object[]{null, null, userNoAccess, groupNoAccess, Role.NO_ACCESS, 0}, //0  
                //            new Object[]{null, null, userNoAccess, groupGuest, Role.GUEST, 0},
                //            new Object[]{null, null, userNoAccess, groupMember, Role.MEMBER, 0},
                //            new Object[]{null, null, userNoAccess, groupManager, Role.MANAGER, 0},
                //            new Object[]{null, null, userNoAccess, groupAdmin, Role.ADMINISTRATOR, 0},
                //            new Object[]{null, null, userGuest, groupNoAccess, Role.NO_ACCESS, 0}, //5  
                //            new Object[]{null, null, userGuest, groupGuest, Role.GUEST, 0},
                //            new Object[]{null, null, userGuest, groupMember, Role.MEMBER, 3},
                //            new Object[]{null, null, userGuest, groupManager, Role.MANAGER, 3},
                //            new Object[]{null, null, userGuest, groupAdmin, Role.ADMINISTRATOR, 3},
                //            new Object[]{null, null, userMember, groupNoAccess, Role.NO_ACCESS, 0}, //10  
                //            new Object[]{null, null, userMember, groupGuest, Role.GUEST, 0},
                //            new Object[]{null, null, userMember, groupMember, Role.MEMBER, 3},
                //            new Object[]{null, null, userMember, groupManager, Role.MANAGER, 3},
                //            new Object[]{null, null, userMember, groupAdmin, Role.ADMINISTRATOR, 3},
                //            new Object[]{null, null, userManager, groupNoAccess, Role.NO_ACCESS, 0}, //15  
                //            new Object[]{null, null, userManager, groupGuest, Role.GUEST, 0},
                //            new Object[]{null, null, userManager, groupMember, Role.MEMBER, 3},
                //            new Object[]{null, null, userManager, groupManager, Role.MANAGER, 3},
                //            new Object[]{null, null, userManager, groupAdmin, Role.ADMINISTRATOR, 3},
                new Object[]{null, null, userAdmin, groupNoAccess, Role.NO_ACCESS, 0}, //20  
                new Object[]{null, null, userAdmin, groupGuest, Role.GUEST, 0},
                new Object[]{null, null, userAdmin, groupMember, Role.MEMBER, 3},
                new Object[]{null, null, userAdmin, groupManager, Role.MANAGER, 3},
                new Object[]{null, null, userAdmin, groupAdmin, Role.ADMINISTRATOR, 3});
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Initialization">
    @BeforeClass
    public static void prepareClass() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        MetaDataManagementHelper.replaceConfig(null);
        metadataManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-Core-Test");
        prepare();
        initResources();

    }

    @AfterClass
    public static void releaseTestAfterTest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        release();
    }

    private static void initResources() {
        Role allRoles[] = {Role.NO_ACCESS, Role.GUEST, Role.MEMBER, Role.MANAGER, Role.ADMINISTRATOR};
        GroupId allGroups[] = {groupNoAccess, groupGuest, groupMember, groupManager, groupAdmin};
        initialized = true;
        int counter = 0;
//    try {
        // Add 2 Entries without any registration 
        // One at beginning...
        SecurityTestEntity ste = new SecurityTestEntity();
        TestEntity te = new TestEntity();
        te.setDescription(Integer.toString(counter++));
        ste.setSummary(Integer.toString(counter++));
        allSecurableEntities.add(ste);
        allEntities.add(te);
        for (GroupId group : allGroups) {
            for (Role role : allRoles) {
                ste = new SecurityTestEntity();
                te = new TestEntity();
                ste.setSummary(group.getStringRepresentation() + "_" + role);
                ste.setDescription("secure_" + role.toString());
                te.setDescription(group.getStringRepresentation() + "_" + role);
                //ResourceServiceLocal.registerResource(ste.getSecurableResourceId(), adminContext);
                allSecurableEntities.add(ste);
                persistItem(ste, group, role);
                allEntities.add(te);
                persistItem(te, group, role);
                //ResourceServiceLocal.registerResource(ste.getSecurableResourceId(), group, role, adminContext);
            }

        }
        // ...one at the end.
        ste = new SecurityTestEntity();
        ste.setSummary(Integer.toString(counter++));
        //ResourceServiceLocal.registerResource(ste.getSecurableResourceId(), adminContext);
        allSecurableEntities.add(ste);
        te = new TestEntity();
        te.setDescription(Integer.toString(counter++));
        allEntities.add(te);
        //    } catch (UnauthorizedAccessAttemptException ex) {
        //      java.util.logging.Logger.getLogger(FilterResourceGroupTest.class.getName()).log(Level.SEVERE, null, ex);
        //    } catch (EntityAllreadyExistsException eaee) {
        //      java.util.logging.Logger.getLogger(FilterResourceGroupTest.class.getName()).log(Level.SEVERE, null, eaee);
        //    }
        try {
            metadataManager.setAuthorizationContext(adminContext);
            allSecurableEntities = metadataManager.find(SecurityTestEntity.class);
            allEntities = metadataManager.find(TestEntity.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            java.util.logging.Logger.getLogger(WriteResourcesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write entity to database. Set authorization context before.
     *
     * @param item entity
     * @param group group for context
     * @param role role for context
     */
    private static void persistItem(SecurityTestEntity item, GroupId group, Role role) {
        AuthorizationContext ctx = null;
        if ((group != null) && (role != null)) {
            ctx = new AuthorizationContext(userMember, group, role);
        }
        metadataManager.setAuthorizationContext(ctx);
        try {
            metadataManager.persist(item);
        } catch (UnauthorizedAccessAttemptException ex) {
            java.util.logging.Logger.getLogger(AccessResourcesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Write entity to database. Set authorization context before.
     *
     * @param item entity
     * @param group group for context
     * @param role role for context
     */
    private static void persistItem(TestEntity item, GroupId group, Role role) {
        AuthorizationContext ctx = null;
        if ((group != null) && (role != null)) {
            ctx = new AuthorizationContext(userMember, group, role);
        }
        metadataManager.setAuthorizationContext(ctx);
        try {
            metadataManager.persist(item);
        } catch (UnauthorizedAccessAttemptException ex) {
            java.util.logging.Logger.getLogger(WriteResourcesTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Test method save">
    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest.
     */
    @Test
    public void testSaveSecureWithRoleAndUnmanagedEntity() throws EntityNotFoundException {
        SecurityTestEntity ste = new SecurityTestEntity();
        ste.setDescription("beforeDes");
        ste.setAnyValue(123L);
        boolean throwException = false;
        if (groupId.getStringRepresentation().startsWith("guest")
                || (groupId.getStringRepresentation().startsWith("no access"))) {
            throwException = true;
        }
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        try {
            metadataManager.save(ste);
            assertFalse(throwException);
            assertTrue(metadataManager.contains(ste));
            // Check for Changes in saved entity
            metadataManager.setAuthorizationContext(adminContext);
            List<SecurityTestEntity> ste2 = metadataManager.find(ste, ste);
            assertEquals(1, ste2.size());
            assertEquals(ste2.get(0).getDescription(), ste.getDescription());
            assertEquals(ste2.get(0).getAnyValue(), ste.getAnyValue());
            assertEquals(ste2.get(0).getId(), ste.getId());

            metadataManager.remove(ste);
        } catch (UnauthorizedAccessAttemptException ex) {
            assertTrue(throwException);
        }
    }

    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest. Save existing entity after changing values.
     */
    @Test
    public void testSaveSecureWithRoleAndManagedEntity() throws EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        SecurityTestEntity ste = getSecurityTestEntity();
        boolean throwException = false;
        if (groupId.getStringRepresentation().startsWith("guest")
                || (groupId.getStringRepresentation().startsWith("no access"))) {
            throwException = true;
        }
        try {
            metadataManager.setAuthorizationContext(adminContext);
            ste = metadataManager.find(SecurityTestEntity.class, ste.getId());
            String oldDescription = ste.getDescription();
            ste.setDescription("beforeDes");
            ste.setAnyValue(123L);

            metadataManager.setAuthorizationContext(act);
            assertTrue(metadataManager.contains(ste));

            // Save entity
            metadataManager.save(ste);
            assertFalse(throwException);
            assertTrue(metadataManager.contains(ste));

            // Check for Changes in saved entity
            metadataManager.setAuthorizationContext(adminContext);
            List<SecurityTestEntity> ste2 = metadataManager.find(ste, ste);
            assertEquals(1, ste2.size());
            assertEquals(ste2.get(0).getDescription(), ste.getDescription());
            assertEquals(ste2.get(0).getAnyValue(), ste.getAnyValue());
            assertEquals(ste2.get(0).getId(), ste.getId());
            // Restore old values
            ste.setDescription(oldDescription);
            metadataManager.save(ste);
            assertFalse(throwException);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error(ste.getDescription() + " / " + ste.getSummary());
            LOGGER.error("Test for testSaveSecureWithRoleAndManagedEntity " + userId.getStringRepresentation() + ", " + groupId.getStringRepresentation() + ", " + userRole + " failed!", ex);
            assertTrue(throwException);
        }
    }

    /**
     * Get a securityTestEntity of the group of the user.
     *
     * @return
     */
    private SecurityTestEntity getSecurityTestEntity() {
        SecurityTestEntity returnValue = allSecurableEntities.get(0);
        try {
            List<SecurityTestEntity> find = metadataManager.find(SecurityTestEntity.class);
            if (!find.isEmpty()) {
                returnValue = find.get(0);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            //ignore
        }
        return returnValue;
    }

    /**
     * Test contains with role NOACCESS
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testSaveWithNoAccess() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
        metadataManager.setAuthorizationContext(act);
        TestEntity te = new TestEntity();
        te.setDescription("aiuh4rbk");
        te.setSummary("kjawephira");
        metadataManager.save(te);
        assertTrue(metadataManager.contains(te));
        List<TestEntity> te2 = metadataManager.find(te, te);
        assertEquals(1, te2.size());
        assertEquals(te2.get(0).getDescription(), te.getDescription());
        assertEquals(te2.get(0).getSummary(), te.getSummary());
        assertEquals(te2.get(0).getId(), te.getId());
        metadataManager.remove(te);
        assertTrue(true);
    }

    /**
     * Test contains with role GUEST
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testSaveWithGuest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        boolean throwException = false;
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        TestEntity ste = new TestEntity();
        assertFalse(metadataManager.contains(ste));
        metadataManager.save(ste);
        assertTrue(metadataManager.contains(ste));
        assertFalse(throwException);
        metadataManager.setAuthorizationContext(adminContext);
        metadataManager.remove(ste);
    }

    /**
     * Test contains with role GUEST
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testSaveExistingEntityWithGuest() throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        boolean throwException = false;
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        TestEntity ste = metadataManager.find(TestEntity.class).get(0);
        assertTrue(metadataManager.contains(ste));
        ste.setDescription(userId.getStringRepresentation() + groupId.getStringRepresentation());
        Long id = ste.getId();
        metadataManager.save(ste);
        TestEntity te2 = metadataManager.find(TestEntity.class, id);
        assertEquals(ste.getDescription(), te2.getDescription());
        assertEquals(ste.getId(), te2.getId());
        assertFalse(throwException);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Test method persist">
    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest.
     */
    @Test
    public void testPersistSecureWithRoleAndUnmanagedEntity() throws EntityNotFoundException {
        SecurityTestEntity ste = new SecurityTestEntity();
        ste.setDescription("beforeDes");
        ste.setAnyValue(123L);
        boolean throwException = false;
        if (groupId.getStringRepresentation().startsWith("guest")
                || (groupId.getStringRepresentation().startsWith("no access"))) {
            throwException = true;
        }
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        try {
            metadataManager.persist(ste);
            assertFalse(throwException);
            assertTrue(metadataManager.contains(ste));
            // Check for Changes in saved entity
            metadataManager.setAuthorizationContext(adminContext);
            List<SecurityTestEntity> ste2 = metadataManager.find(ste, ste);
            assertEquals(1, ste2.size());
            assertEquals(ste2.get(0).getDescription(), ste.getDescription());
            assertEquals(ste2.get(0).getAnyValue(), ste.getAnyValue());
            assertEquals(ste2.get(0).getId(), ste.getId());

            metadataManager.remove(ste);
        } catch (UnauthorizedAccessAttemptException ex) {
            assertTrue(throwException);
        }
    }

    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest. Save existing entity after changing values.
     */
    @Test
    public void testPersistSecureWithRoleAndManagedEntity() throws EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        SecurityTestEntity ste = getSecurityTestEntity();
        boolean throwException = false;
        if (groupId.getStringRepresentation().startsWith("guest")
                || (groupId.getStringRepresentation().startsWith("no access"))) {
            throwException = true;
        }
        try {
            metadataManager.setAuthorizationContext(adminContext);
            ste = metadataManager.find(SecurityTestEntity.class, ste.getId());
            String oldDescription = ste.getDescription();
            ste.setDescription("beforeDes");
            ste.setAnyValue(123L);
            metadataManager.setAuthorizationContext(act);
            assertTrue(metadataManager.contains(ste));
            // Save entity
            metadataManager.persist(ste);
            assertFalse(throwException);
            assertTrue(metadataManager.contains(ste));
            // Check for Changes in saved entity
            metadataManager.setAuthorizationContext(adminContext);
            List<SecurityTestEntity> ste2 = metadataManager.find(ste, ste);
            assertEquals(1, ste2.size());
            assertEquals(ste2.get(0).getDescription(), ste.getDescription());
            assertEquals(ste2.get(0).getAnyValue(), ste.getAnyValue());
            assertEquals(ste2.get(0).getId(), ste.getId());
            // Restore old values
            ste.setDescription(oldDescription);
            metadataManager.save(ste);
            assertFalse(throwException);
        } catch (UnauthorizedAccessAttemptException ex) {
            assertTrue(throwException);
        } catch (EntityExistsException eeex) {
            // This is the right path.
            assertFalse(throwException);
        }
    }

    /**
     * Test contains with role NOACCESS
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testPersistWithNoAccess() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
        metadataManager.setAuthorizationContext(act);
        TestEntity te = new TestEntity();
        te.setDescription("aiuh4rbk");
        te.setSummary("kjawephira");
        assertFalse(metadataManager.contains(te));
        metadataManager.persist(te);
        assertTrue(metadataManager.contains(te));
        List<TestEntity> te2 = metadataManager.find(te, te);
        assertEquals(1, te2.size());
        assertEquals(te2.get(0).getDescription(), te.getDescription());
        assertEquals(te2.get(0).getSummary(), te.getSummary());
        assertEquals(te2.get(0).getId(), te.getId());
        metadataManager.remove(te);
        assertTrue(true);
    }

    /**
     * Test contains with role GUEST
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testPersistWithGuest() throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        TestEntity ste = new TestEntity();
        ste.setDescription("kiepripiioi");
        ste.setSummary("ksdjrh");
        boolean throwException = false;
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        assertFalse(metadataManager.contains(ste));
        metadataManager.persist(ste);
        assertFalse(throwException);
    }

    /**
     * Test contains with role GUEST
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testPersistExistingEntityWithGuest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        TestEntity ste = new TestEntity();
        ste.setDescription("kasjelrhg");
        ste.setSummary("ioewrhah");
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        assertFalse(metadataManager.contains(ste));
        metadataManager.persist(ste);
        assertTrue(metadataManager.contains(ste));
        metadataManager.persist(ste);
        metadataManager.setAuthorizationContext(adminContext);
        metadataManager.remove(ste);

    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Test method update">
    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest.
     */
    @Test
    public void testUpdateSecureWithRoleAndUnmanagedEntity() throws EntityNotFoundException {
        SecurityTestEntity ste = new SecurityTestEntity();
        ste.setDescription("beforeUpdateDes");
        ste.setAnyValue(1234L);
        boolean throwException = false;
        boolean entityNotExist = true;
        if (groupId.getStringRepresentation().startsWith("guest")
                || groupId.getStringRepresentation().startsWith("no access")) {
            throwException = true;
            entityNotExist = false;
        }
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        try {
            SecurityTestEntity update = metadataManager.update(ste);
            // should not happen
            assertFalse(true);
        } catch (UnauthorizedAccessAttemptException ex) {
            assertTrue(throwException);
            assertFalse(entityNotExist);
        } catch (EntityNotFoundException enfe) {
            assertTrue(entityNotExist);
            assertFalse(throwException);
        }
    }

    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest. Save existing entity after changing values.
     */
    @Test
    public void testUpdateSecureWithRoleAndManagedEntity() throws EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        SecurityTestEntity ste = getSecurityTestEntity();
        boolean throwException = false;
        if (groupId.getStringRepresentation().startsWith("guest")
                || (groupId.getStringRepresentation().startsWith("no access"))) {
            throwException = true;
        }
        try {
            metadataManager.setAuthorizationContext(adminContext);
            ste = metadataManager.find(SecurityTestEntity.class, ste.getId());
            String oldDescription = ste.getDescription();
            ste.setDescription("beforeDes");
            ste.setAnyValue(123L);
            metadataManager.setAuthorizationContext(act);
            assertTrue(metadataManager.contains(ste));
            // Save entity
            metadataManager.update(ste);
            assertFalse(throwException);
            assertTrue(metadataManager.contains(ste));
            // Check for Changes in saved entity
            metadataManager.setAuthorizationContext(adminContext);
            List<SecurityTestEntity> ste2 = metadataManager.find(ste, ste);
            assertEquals(1, ste2.size());
            assertEquals(ste2.get(0).getDescription(), ste.getDescription());
            assertEquals(ste2.get(0).getAnyValue(), ste.getAnyValue());
            assertEquals(ste2.get(0).getId(), ste.getId());
            // Restore old values
            ste.setDescription(oldDescription);
            metadataManager.save(ste);
            assertFalse(throwException);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error(ste.getDescription() + " / " + ste.getSummary());
            LOGGER.error("Test for testUpdateSecureWithRoleAndManagedEntity " + userId.getStringRepresentation() + ", " + groupId.getStringRepresentation() + ", " + userRole + " failed!", ex);
            assertTrue(throwException);
        }
    }

    /**
     * Test contains with role NOACCESS
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testUpdateWithNoAccess() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
        metadataManager.setAuthorizationContext(act);
        TestEntity te = new TestEntity();
        Date date = new Date();
        te.setDescription("eruyaweuoyr" + date.toString());
        te.setSummary("io3uq2ouhrjhl" + date.getTime());
        TestEntity update;
        update = metadataManager.update(te);
        assertTrue(metadataManager.contains(update));
        List<TestEntity> te2 = metadataManager.find(te, te);
        assertEquals(1, te2.size());
        assertEquals(te2.get(0).getDescription(), te.getDescription());
        assertEquals(te2.get(0).getSummary(), te.getSummary());
        assertEquals(te2.get(0).getId(), te.getId());
        metadataManager.remove(te);
        assertTrue(true);
    }

    /**
     * Test contains with role GUEST
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testUpdateWithGuest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        TestEntity ste = new TestEntity();
        assertFalse(metadataManager.contains(ste));
        metadataManager.update(ste);
        // An exception should be thrown.
        assertTrue(false);
    }

    /**
     * Test contains with role GUEST
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testUpdateExistingEntityWithGuest() throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        TestEntity ste = metadataManager.find(TestEntity.class).get(0);
        assertTrue(metadataManager.contains(ste));
        ste.setDescription(userId.getStringRepresentation() + groupId.getStringRepresentation());
        Long id = ste.getId();
        metadataManager.update(ste);
        TestEntity te2 = metadataManager.find(TestEntity.class, id);
        assertEquals(ste.getDescription(), te2.getDescription());
        assertEquals(ste.getId(), te2.getId());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Test method remove">
    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest.
     */
    @Test
    public void testRemoveSecureWithRoleAndUnmanagedEntity() {
        SecurityTestEntity ste = new SecurityTestEntity();
        ste.setDescription("beforeUpdateDes");
        ste.setAnyValue(1234L);
        boolean manager = false;
        boolean throwException = true;
        if (groupId.getStringRepresentation().startsWith("admin")
                || groupId.getStringRepresentation().startsWith("manager")) {
            manager = true;
            throwException = false;
        }
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        try {
            metadataManager.remove(ste);
            assertFalse(throwException);
        } catch (UnauthorizedAccessAttemptException ex) {
            assertTrue(throwException);
        } catch (EntityNotFoundException enfe) {
            assertFalse(throwException);
            assertTrue(manager);
        }
    }

    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest. Save existing entity after changing values.
     */
    @Test
    public void testRemoveSecureWithRoleAndManagedEntity() throws EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        SecurityTestEntity ste = getSecurityTestEntity();
        boolean throwException = true;
        boolean admin = false;
        if (groupId.getStringRepresentation().startsWith("admin")
                || groupId.getStringRepresentation().startsWith("manager")) {
            admin = true;
            throwException = false;
        }
        try {
            metadataManager.setAuthorizationContext(adminContext);
            ste = metadataManager.find(SecurityTestEntity.class, ste.getId());
            String oldDescription = ste.getDescription();
            ste.setDescription("beforeDes");
            ste.setAnyValue(123L);
            metadataManager.setAuthorizationContext(act);
            assertTrue(metadataManager.contains(ste));
            // Save entity
            metadataManager.remove(ste);
            assertFalse(throwException);
            assertTrue(admin);
            assertFalse(metadataManager.contains(ste));
        } catch (UnauthorizedAccessAttemptException ex) {
            assertTrue(throwException);
        }
    }

    /**
     * Test contains with role NOACCESS
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testRemoveWithNoAccess() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
        metadataManager.setAuthorizationContext(act);
        TestEntity te = new TestEntity();
        te.setDescription("eruyaweuoyr");
        te.setSummary("io3uq2ouhrjhl");
        assertFalse(metadataManager.contains(te));
        metadataManager.remove(te);
//    assertTrue(false);
    }

    /**
     * Test contains with role GUEST
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testRemoveWithGuest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        TestEntity ste = new TestEntity();
        assertFalse(metadataManager.contains(ste));
        metadataManager.remove(ste);
        // An exception should be thrown.
        assertTrue(false);
    }

    /**
     * Test contains with role GUEST
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testRemoveExistingEntityWithGuest() throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        TestEntity ste = metadataManager.find(TestEntity.class).get(0);

        assertTrue(metadataManager.contains(ste));
        ste.setDescription(userId.getStringRepresentation() + groupId.getStringRepresentation());
        Long id = ste.getId();
        metadataManager.remove(ste);
        assertFalse(metadataManager.contains(ste));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Test method refresh">
    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest.
     */
    @Test
    public void testRefreshSecureWithRoleAndUnmanagedEntity() throws EntityNotFoundException {
        SecurityTestEntity ste = new SecurityTestEntity();
        ste.setDescription("beforeUpdateDes");
        ste.setAnyValue(1234L);
        boolean throwException = false;
        boolean entityNotExist = true;
        if (groupId.getStringRepresentation().startsWith("no access")) {
            throwException = true;
            entityNotExist = false;
        }
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        try {
            ste = metadataManager.refresh(ste);
            // should not happen
            assertFalse(true);
        } catch (UnauthorizedAccessAttemptException ex) {
            assertTrue(throwException);
            assertFalse(entityNotExist);
        } catch (EntityNotFoundException enfe) {
            assertTrue(entityNotExist);
            assertFalse(throwException);
        }
    }

    /**
     * Test save on securableResource with role GUEST Throws exception for
     * noaccess and guest. Save existing entity after changing values.
     */
    @Test
    public void testRefreshSecureWithRoleAndManagedEntity() throws EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
        metadataManager.setAuthorizationContext(act);
        SecurityTestEntity ste = getSecurityTestEntity();
        boolean throwException = false;
        boolean unauhorized = false;
        if (groupId.getStringRepresentation().startsWith("guest")
                || (groupId.getStringRepresentation().startsWith("no access"))) {
            unauhorized = true;
        }
        try {
//      entityManager.setAuthorizationContext(adminContext);
            ste = metadataManager.find(SecurityTestEntity.class, ste.getId());
            String oldDescription = ste.getDescription();
            Long oldAnyValue = ste.getAnyValue();
            ste.setDescription("beforeDes" + new Date().getTime() % 10000);
            ste.setAnyValue(12345L);
//      entityManager.setAuthorizationContext(act);
            assertTrue(metadataManager.contains(ste));
            // Refresh entity
            ste = metadataManager.refresh(ste);
            assertFalse(throwException);
            assertTrue(metadataManager.contains(ste));
            assertEquals(oldDescription, ste.getDescription());
            assertEquals(oldAnyValue, new Long(ste.getAnyValue()));
            assertFalse(throwException);
        } catch (UnauthorizedAccessAttemptException ex) {
            assertTrue(unauhorized);
        }
    }

    /**
     * Test contains with role NOACCESS
     */
    @Test(expected = UnauthorizedAccessAttemptException.class)
    public void testRefreshWithNoAccess() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
        metadataManager.setAuthorizationContext(act);
        TestEntity te = new TestEntity();
        Date date = new Date();
        te.setDescription("eruyaweuoyr" + date.toString());
        te.setSummary("io3uq2ouhrjhl" + date.getTime());
        assertFalse(metadataManager.contains(te));
        metadataManager.refresh(te);
        //should not happen
        assertTrue(metadataManager.contains(te));
        assertTrue(false);
    }

    /**
     * Test contains with role GUEST
     */
    @Test
    public void testRefreshWithGuest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        try {
            TestEntity ste = metadataManager.find(TestEntity.class).get(0);
            TestEntity te2 = new TestEntity();
            te2.setId(ste.getId());
            metadataManager.refresh(te2);
        } catch (UnauthorizedAccessAttemptException ex) {
            //does not work for group 'no access'
            assertEquals("no access", groupId.getStringRepresentation());
        }
    }

    /**
     * Test contains with role GUEST
     */
    @Test
    public void testRefreshExistingEntityWithGuest() throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
        metadataManager.setAuthorizationContext(act);
        try {
            TestEntity ste = metadataManager.find(TestEntity.class).get(0);
            String oldDescription = ste.getDescription();
            assertTrue(metadataManager.contains(ste));
            ste.setDescription(userId.getStringRepresentation() + groupId.getStringRepresentation() + (new Date().getTime() % 10000));
            String wrongDescription = ste.getDescription();
            ste = metadataManager.refresh(ste);
            Long id = ste.getId();
            TestEntity te2 = metadataManager.find(TestEntity.class, id);
            assertNotSame(wrongDescription, ste.getDescription());
            assertNotSame(wrongDescription, ste.getDescription());
            assertEquals(oldDescription, te2.getDescription());
            assertEquals(ste.getDescription(), te2.getDescription());
            assertEquals(ste.getId(), te2.getId());
        } catch (UnauthorizedAccessAttemptException ex) {
            assertEquals("no access", groupId.getStringRepresentation());
        }
    }
    //</editor-fold>
}
