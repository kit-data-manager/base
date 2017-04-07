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
package edu.kit.dama.rest.services.staging.test;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.rest.services.staging.test.util.TestAuthorizationContext;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationPersistenceImpl;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.junit.*;

/**
 * Test cases for IngestInformationPersistenceImpl
 *
 * @see IngestInformationPersistenceImpl
 * @author jejkal
 */
public class IngestInformationPersistenceTest {

    static {
        PU.setPersistenceUnitName("AuthorizationUnit-Test");
    }
    private static final IngestInformationPersistenceImpl testCandidate = IngestInformationPersistenceImpl.getSingleton("Staging_Test");
    private static final List<IngestInformation> entities = new LinkedList<>();
    private static final List<IngestInformation> entitiesCtx = new LinkedList<>();
    private static final List<IngestInformation> entitiesCtx2 = new LinkedList<>();
    private static final IAuthorizationContext secCtx = new TestAuthorizationContext("someUser", "someGroup", Role.MEMBER);
    private static final IAuthorizationContext secCtx2 = new TestAuthorizationContext("anotherUser", "anotherGroup", Role.MEMBER);

    @BeforeClass
    public static void prepareGlobal() {
        try {
            Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Creating user 'someUser'");
            UserServiceLocal.getSingleton().register(new UserId("someUser"), Role.MANAGER, AuthorizationContext.factorySystemContext());
            Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Creating user 'anotherUser'");
            UserServiceLocal.getSingleton().register(new UserId("anotherUser"), Role.MANAGER, AuthorizationContext.factorySystemContext());
            Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Creating group 'someGroup'");
            GroupServiceLocal.getSingleton().create(new GroupId("someGroup"), new UserId("someUser"), AuthorizationContext.factorySystemContext());
            Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Creating group 'anotherGroup'");
            GroupServiceLocal.getSingleton().create(new GroupId("anotherGroup"), new UserId("anotherUser"), AuthorizationContext.factorySystemContext());
        } catch (Exception e) {
            Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.WARNING, "Failed to register users and groups.", e);
        }
    }

    @Before
    public void prepare() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Preparing test data");
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, " * Adding entries with context {0}", secCtx);
        for (int i = 0; i < 5; i++) {
            String uuid = UUID.randomUUID().toString();
            IngestInformation entity = testCandidate.createEntity(new DigitalObjectId(uuid), secCtx);
            entities.add(entity);
            entitiesCtx.add(entity);
        }
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, " * Adding entries with context {0}", secCtx2);
        for (int i = 0; i < 5; i++) {
            String uuid = UUID.randomUUID().toString();
            IngestInformation entity = testCandidate.createEntity(new DigitalObjectId(uuid), secCtx2);
            entities.add(entity);
            entitiesCtx2.add(entity);
        }
        Assert.assertEquals(10, entities.size());
        Assert.assertEquals(5, entitiesCtx.size());
        Assert.assertEquals(5, entitiesCtx2.size());
    }

    @After
    public void cleanup() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Cleaning up data");
        for (IngestInformation entity : entitiesCtx) {
            testCandidate.removeEntity(entity.getId(), AuthorizationContext.factorySystemContext());
        }
        for (IngestInformation entity : entitiesCtx2) {
            testCandidate.removeEntity(entity.getId(), AuthorizationContext.factorySystemContext());
        }
        entities.clear();
        entitiesCtx.clear();
        entitiesCtx2.clear();
    }

    @Test
    public void getEntityById() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity by id. Expecting one known result.");
        IngestInformation entity = entities.get(0);
        IngestInformation result = testCandidate.getEntityById(entity.getId(), secCtx);
        Assert.assertNotNull(result);
    }

    @Test
    public void getEntityByOwner1() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity by owner v1. Expecting one known result.");
        List<IngestInformation> result = testCandidate.getEntitiesByOwner(secCtx.getUserId(), secCtx);
        assertListsEqual(entitiesCtx, result);
    }

    @Test
    public void getEntityByOwner2() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity by owner v2. Expecting one known result.");
        List<IngestInformation> result = testCandidate.getEntitiesByOwner(0, 5, secCtx);
        assertListsEqual(entitiesCtx, result);
    }

    @Test
    public void getEntityByOwner3() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity by owner v3. Expecting one known result.");
        List<IngestInformation> result = testCandidate.getEntitiesByOwner(secCtx.getUserId(), 0, 5, secCtx);
        assertListsEqual(entitiesCtx, result);
    }

    @Test
    public void getExpiredEntities1() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting expired entities v1. Expecting no result.");
        List<IngestInformation> result = testCandidate.getExpiredEntities(secCtx);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getExpiredEntities2() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting expired entities v2. Expecting no result.");
        List<IngestInformation> result = testCandidate.getExpiredEntities(0, 5, secCtx);
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void getExpiredEntityCount() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting expired entity count. Expecting no result.");
        long result = (Long) testCandidate.getExpiredEntitiesCount(secCtx);
        Assert.assertEquals(0, result);
    }

    @Test
    public void getEntityByIdWithWrongId() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity with invalid id. Expecting PersistenceException.");
        Assert.assertNull(testCandidate.getEntityById(-1l, secCtx));
    }

    @Test
    public void getEntitiesByDigitalObjectId1() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity by digital object id v1. Expecting one known result.");
        IngestInformation entity = entities.get(0);
        List<IngestInformation> result = testCandidate.getEntitiesByDigitalObjectId(new DigitalObjectId(entity.getDigitalObjectId()), secCtx);
        Assert.assertEquals(1, result.size());
        Assert.assertNotNull(result.get(0));
    }

    @Test
    public void getEntitiesByDigitalObjectId2() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity by digital object id v2. Expecting one known result.");
        IngestInformation entity = entities.get(0);
        List<IngestInformation> result = testCandidate.getEntitiesByDigitalObjectId(new DigitalObjectId(entity.getDigitalObjectId()), 0, 5, secCtx);
        Assert.assertEquals(1, result.size());
        Assert.assertNotNull(result.get(0));
    }

    @Test
    public void getEntitiesCountByDigitalObjectId() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity count by digital object id. Expecting one known result.");
        IngestInformation entity = entities.get(0);
        long result = (Long) testCandidate.getEntitiesCountByDigitalObjectId(new DigitalObjectId(entity.getDigitalObjectId()), secCtx);
        Assert.assertEquals(1, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getEntitiesByDigitalObjectIdWithNullArgument() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity with object id 'null'. Expecting IllegalArgumentException.");
        testCandidate.getEntitiesByDigitalObjectId(null, secCtx);
    }

    @Test
    public void getEntitiesByInvalidDigitalObjectId() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entity with invalid object id. Expecting (expected PersistenceException");
        List<IngestInformation> result = testCandidate.getEntitiesByDigitalObjectId(new DigitalObjectId("invalidId"), secCtx);
        Assert.assertNotNull(result);
        Assert.assertEquals(result.isEmpty(), true);
    }

    @Test
    public void getAllEntities() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting all entities using a normal security context. Expecting a list of 5 entities of the test database.");
        //set role to member as admins get all entities and we only want to get entities owned by ctx.getUserId()
        List<IngestInformation> result = testCandidate.getAllEntities(secCtx);
        assertListsEqual(entitiesCtx, result);
    }

    @Test
    public void getAllEntitiesAsAdmin() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting all entities using the ADMINISTRATOR role overwriting the permissions of the current user. Expecting a list of 10 entities of the test database.");
        //set role to member as admins get all entities and we only want to get entities owned by ctx.getUserId()
        secCtx.setRoleRestriction(Role.ADMINISTRATOR);
        List<IngestInformation> result = testCandidate.getAllEntities(secCtx);
        assertListsEqual(entities, result);
        secCtx.setRoleRestriction(Role.MEMBER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAllEntitiesWithoutContext() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting all entities without any security context. Expecting a list of all 10 entities of the test database.");
        List<IngestInformation> result = testCandidate.getAllEntities(null);
        assertListsEqual(entities, result);
    }

    @Test
    public void getEntitiesByOwner() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entities by owner. Expecting a list containing 5 entities.");
        List<IngestInformation> result = testCandidate.getEntitiesByOwner(secCtx.getUserId(), secCtx);
        Assert.assertEquals(5, result.size());
    }

    @Test
    public void getEntitiesByNullOwner() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entities with owner 'null' and context ctx. Expecting same result as getEntities(ctx) .");
        List<IngestInformation> resultNull = testCandidate.getEntitiesByOwner(null, secCtx);
        List<IngestInformation> resultSecCtx = testCandidate.getEntitiesByOwner(secCtx.getUserId(), secCtx);
        assertListsEqual(resultNull, resultSecCtx);
    }

    @Test
    public void getEntitiesByUnknownOwner() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entities with invalid owner. Expecting empty list.");
        List<IngestInformation> result = testCandidate.getEntitiesByOwner(new UserId("unknownUser"), secCtx);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void getEntitiesByStatus1() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entities by status PREPARING for a specific context v1. Expecting a list of 5 entities of the test database.");
        List<IngestInformation> result = testCandidate.getEntitiesByStatus(INGEST_STATUS.PREPARING, secCtx);
        Assert.assertNotNull(result);
        assertListsEqual(entitiesCtx, result);
    }

    @Test
    public void getEntitiesByStatus2() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entities by status PREPARING for a specific context v2. Expecting a list of 5 entities of the test database.");
        List<IngestInformation> result = testCandidate.getEntitiesByStatus(INGEST_STATUS.PREPARING, 0, 5, secCtx);
        assertListsEqual(entitiesCtx, result);
    }

    @Test
    public void getEntitiesByStatusUnknown() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try getting entities with status UNKNOWN. Expecting empty list");
        List<IngestInformation> result = testCandidate.getEntitiesByStatus(INGEST_STATUS.UNKNOWN, secCtx);
        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.size());
    }

    @Test
    public void updateEntityStatus() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Trying to update entity status and error message. Expecting modified entity with updated status and error message.");
        List<IngestInformation> resultList = testCandidate.getEntitiesByStatus(INGEST_STATUS.INGEST_FAILED, AuthorizationContext.factorySystemContext());
        Assert.assertEquals(0, resultList.size());

        IngestInformation randomEntity = entitiesCtx.get(0);
        int affectedRows = testCandidate.updateStatus(randomEntity.getId(), INGEST_STATUS.INGEST_FAILED, "Some Error", AuthorizationContext.factorySystemContext());
        Assert.assertEquals(1, affectedRows);
        resultList = testCandidate.getEntitiesByStatus(INGEST_STATUS.INGEST_FAILED, AuthorizationContext.factorySystemContext());

        Assert.assertEquals(1, resultList.size());

        Assert.assertEquals(INGEST_STATUS.INGEST_FAILED.getId(), resultList.get(0).getStatusEnum().getId());
        Assert.assertEquals("Some Error", resultList.get(0).getErrorMessage());
    }

    @Test
    public void updateStatusForInvalidEntity() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Trying to update the status of invalid entity. Expecting PersistenceException (caught) and unmodified entity in database.");
        List<IngestInformation> before = testCandidate.getAllEntities(secCtx);
        int affectedRows = testCandidate.updateStatus(-1644l, INGEST_STATUS.INGEST_FAILED, "Some Error", secCtx);

        List<IngestInformation> after = testCandidate.getAllEntities(secCtx);
        assertListsEqual(before, after);
        Assert.assertEquals(0, affectedRows);
    }

    @Test
    public void updateStatusForWrongContext() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Trying to update entity status and error message for an entity which is owned by another user. Expecting no modified rows.");
        IngestInformation randomEntity = getRandomEntity(secCtx2);
        int affectedRows = testCandidate.updateStatus(randomEntity.getId(), INGEST_STATUS.INGEST_FAILED, "Some Error", secCtx);
        Assert.assertEquals(0, affectedRows);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateStatusToStatusUnknown() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Trying to update the status to 'null'. Expecting IllegalArgumentException");
        IngestInformation randomEntity = getRandomEntity(secCtx);
        testCandidate.updateStatus(randomEntity.getId(), null, null, secCtx);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateStatusToNull() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Trying to update the status to 'null'. Expecting IllegalArgumentException.");
        IngestInformation randomEntity = getRandomEntity(secCtx);
        testCandidate.updateStatus(randomEntity.getId(), null, null, secCtx);
    }

    @Test
    public void updateTransferUrl() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Trying to update the transfer URL of an entity. Expecting modified entity with new transferURL and status PRE_INGEST_SCHEDULED");
        IngestInformation randomEntity = getRandomEntity(secCtx);
        int affectedRows = testCandidate.updateClientAccessUrl(randomEntity.getId(), "http://localhost:1234", secCtx);
        Assert.assertEquals(1, affectedRows);

        List<IngestInformation> resultList = testCandidate.getAllEntities(secCtx);

        Assert.assertEquals(5, resultList.size());

        for (int i = 0; i < 5; i++) {
            if (resultList.get(i).getId() == randomEntity.getId()) {
                Assert.assertEquals("http://localhost:1234", resultList.get(i).getClientAccessUrl());
                Assert.assertEquals(resultList.get(i).getStatus(), INGEST_STATUS.PRE_INGEST_SCHEDULED.getId());
                return;
            }
        }
    }

    @Test
    public void updateTransferUrlForUnkownEntity() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Trying to update transfer URL for invalid entity. Expecting PersistenceException.");
        int affectedRows = testCandidate.updateClientAccessUrl(-1l, "http://localhost:1234", secCtx);
        Assert.assertEquals(0, affectedRows);
    }

    @Test
    @Ignore("URL is not longer validated")
    public void updateTransferUrlToInvalidValue() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Trying to update transfer URL to an invalid URL. Expecting one modified row, as there is no URL format check intended.");
        IngestInformation randomEntity = getRandomEntity(secCtx);
        int affectedRows = testCandidate.updateClientAccessUrl(randomEntity.getId(), "someWrongProtocol://anyHost:noPort", secCtx);
        Assert.assertEquals(1, affectedRows);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateTransferUrlToNull() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Trying to update transfer URL to value 'null'. Expecting IllegalArgumentException.");
        IngestInformation randomEntity = getRandomEntity(secCtx);
        testCandidate.updateClientAccessUrl(randomEntity.getId(), null, secCtx);
    }

    @Test
    @Ignore(value = "Only MANAGER role is allowed to remove entities. Therefore, this tests won't work.")
    public void removeEntity() {
        Logger.getLogger(IngestInformationPersistenceTest.class.getName()).log(Level.INFO, "Try to remove an entity. Expecting list with 9 entries not containing the removed entity.");
        IngestInformation randomEntity = getRandomEntity(secCtx);
        int affectedRows = testCandidate.removeEntity(randomEntity.getId(), secCtx);
        Assert.assertEquals(1, affectedRows);
        List<IngestInformation> result = testCandidate.getAllEntities(secCtx);

        Assert.assertEquals(4, result.size());
        IngestInformation missingEntity = null;
        for (IngestInformation entitie : entities) {
            if (entitie.getId() == randomEntity.getId()) {
                missingEntity = entitie;
                break;
            }
        }

        Assert.assertEquals(randomEntity, missingEntity);

    }

    /**
     * Helper method used to check two list for equality. It causes no assertion
     * fault if both lists: - are not 'null' - have the same size - have the
     * same elements
     */
    private void assertListsEqual(List<IngestInformation> pExpected, List<IngestInformation> pCurrent) {
        Assert.assertNotNull(pExpected);
        Assert.assertNotNull(pCurrent);
        Assert.assertEquals(pExpected.size(), pCurrent.size());

        for (IngestInformation entity : pExpected) {
            boolean list2ContainsElement = false;
            for (IngestInformation pCurrent1 : pCurrent) {
                if (Objects.equals(entity.getId(), pCurrent1.getId())) {
                    Assert.assertEquals(entity, pCurrent1);
                    list2ContainsElement = true;
                    break;
                }
            }
            Assert.assertEquals(list2ContainsElement, true);
        }
    }

    /**
     * Helper method to get a random entity from the test database for the
     * provided context
     */
    private IngestInformation getRandomEntity(IAuthorizationContext pCtx) {
        int idx = (int) Math.floor(Math.random() * 5);
        idx = (idx > 4) ? 4 : idx;
        if (pCtx.equals(secCtx)) {
            return entitiesCtx.get(idx);
        }
        return entitiesCtx2.get(idx);
    }
}
