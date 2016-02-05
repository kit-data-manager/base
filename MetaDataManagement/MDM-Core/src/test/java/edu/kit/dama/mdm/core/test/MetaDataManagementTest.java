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
package edu.kit.dama.mdm.core.test;

import edu.kit.dama.authorization.annotations.resources.TestEntity;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import java.util.List;
import org.slf4j.Logger;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import javax.persistence.EntityExistsException;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author hartmann-v
 */
public class MetaDataManagementTest {

  /**
   * For logging purposes.
   */
  private static Logger LOGGER = LoggerFactory.getLogger(MetaDataManagementTest.class);
  /**
   * Manager holding all Information.
   */
  private static IMetaDataManager entityManager = null;
  private static TestEntity referenceEntity = null;
  private static final String DESCRIPTION = "reference description";
  private static final String DESCRIPTION_NEW = "updated reference description";
  private static final String SUMMARY = "reference summary";

  @BeforeClass
  public static void prepareClassForTests() {
    LOGGER.info("Prepare class 'MetaDataManagementTest' for Tests!");
    // create entity Manager
    MetaDataManagementHelper.replaceConfig(null);
    prepareEntityManager();
    assertNotNull(entityManager);
  }

  public static void prepareEntityManager() {
    entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-Core-Test");
    entityManager.setAuthorizationContext(SecurityUtil.adminContext);
  }

  @AfterClass
  public static void releaseClassAfterTests() throws AuthorizationException {
    LOGGER.info("All tests done!\nRelease class 'MetaDataManagementTest' for Tests!");
    List<TestEntity> find = entityManager.find(TestEntity.class);
    for (TestEntity item : find) {
      entityManager.remove(item);
    }
    entityManager.close();
  }

  @Before
  public void addTestEntityReference() throws UnauthorizedAccessAttemptException {
    referenceEntity = createTestEntityReference();
    entityManager.save(referenceEntity);
  }

  @After
  public void removeAllEntities() throws AuthorizationException {
    entityManager.setAuthorizationContext(SecurityUtil.adminContext);
    List<TestEntity> find = entityManager.find(TestEntity.class);
    for (TestEntity entity : find) {
      entityManager.remove(entity);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void test01_DataBaseFailure() {
    IMetaDataManager noEntityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "notExist");
    assertEquals(noEntityManager.toString(), "Should not happen!");
  }

  @Test
  public void test02_testDataBaseExists() {
    assertNotNull(entityManager);
    assertTrue(IMetaDataManager.class.isAssignableFrom(entityManager.getClass()));
  }

  /**
   * Test if all existing Entities are shown as valid objects.
   */
  @Test
  public void test03_Entities() throws UnauthorizedAccessAttemptException {
    TestEntity testEntity = new TestEntity();

    assertEquals(false, entityManager.contains(testEntity));
  }

  /**
   * Test if all existing Entities are shown as valid objects.
   */
  @Test(expected = IllegalArgumentException.class)
  public void test04_NoEntities() throws UnauthorizedAccessAttemptException {
    assertEquals(false, entityManager.contains(this));
  }

  /**
   * Test save entity to database.
   */
  @Test
  public void test05_SaveEntity() throws UnauthorizedAccessAttemptException {
    TestEntity EntityForTestFive = createTestEntityReference();
    Long idBefore = EntityForTestFive.getId();
    entityManager.save(EntityForTestFive);
    Long idAfter = EntityForTestFive.getId();
    assertNotNull(idAfter);
    assertNotSame(idBefore, idAfter);
    referenceEntity = EntityForTestFive;
    assertEquals(true, entityManager.contains(referenceEntity));
  }

  /**
   * Test save entity to database.
   */
  @Test
  public void testSaveEntityTwice() throws UnauthorizedAccessAttemptException {
    TestEntity entityForTestFive = createTestEntityReference();
    entityManager.save(entityForTestFive);
    entityManager.save(entityForTestFive);
    assertEquals(true, entityManager.contains(entityForTestFive));
  }

  /**
   * Test persist entity to database.
   */
  @Test(expected = EntityExistsException.class)
  public void testPersistEntityTwice() throws UnauthorizedAccessAttemptException {
    TestEntity entityForTestFive = createTestEntityReference();
    entityManager.persist(entityForTestFive);
    entityManager.persist(entityForTestFive);
    assertEquals(true, entityManager.contains(entityForTestFive));
  }

  /**
   * Test persist entity to database twice.
   */
  @Test(expected = EntityExistsException.class)
  public void testPersistDetachedEntityTwice() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    TestEntity entityForTestFive = createTestEntityReference();
    entityManager.persist(entityForTestFive);
    int noOfTestEntities = entityManager.find(entityForTestFive.getClass()).size();
    assertEquals(noOfTestEntities, entityManager.find(TestEntity.class).size());
    MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-Core-Test").persist(entityForTestFive);
    assertFalse(true);
  }

  @Test
  public void testCloseManager() throws UnauthorizedAccessAttemptException {
    entityManager.close();
    try {
      entityManager.find(referenceEntity.getClass());
      assertTrue(false);
    } catch (IllegalStateException ise) {
    } finally {
      prepareEntityManager();
    }
  }

  /**
   * Test update entity to database.
   */
  @Test(expected = EntityNotFoundException.class)
  @Ignore("Should be allowed now")
  public void testUpdateForNotManagedEntity() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    TestEntity entityForTestFive = createTestEntityReference();
    entityForTestFive = entityManager.update(entityForTestFive);
    assertEquals(true, entityManager.contains(entityForTestFive));
  }

  /**
   * Test if entity is already persisted to database.
   */
  /**
   * Test if entity if find works.
   */
  @Test
  public void testFindEntity() throws AuthorizationException {
    TestEntity find = entityManager.find(TestEntity.class, referenceEntity.getId());
    assertEquals(referenceEntity.getId(), find.getId());
    assertEquals(referenceEntity.getDescription(), find.getDescription());
    assertEquals(referenceEntity.getSummary(), find.getSummary());
    assertEquals(DESCRIPTION, find.getDescription());
    assertEquals(SUMMARY, find.getSummary());
  }

  /**
   * Test if entity if find works.
   */
  @Test
  public void testFindAllEntities() throws UnauthorizedAccessAttemptException {
    List<TestEntity> find = entityManager.find(TestEntity.class);
    assertEquals(find.size(), 1);
    TestEntity firstElement = find.get(0);
    assertEquals(referenceEntity.getId(), firstElement.getId());
    assertEquals(referenceEntity.getDescription(), firstElement.getDescription());
    assertEquals(referenceEntity.getSummary(), firstElement.getSummary());
    assertEquals(DESCRIPTION, firstElement.getDescription());
    assertEquals(SUMMARY, firstElement.getSummary());
  }

  /**
   * Test if entity if refresh works.
   * 
   * @throws AuthorizationException If authorization fails
   */
  @Test
  public void testRefreshEntity() throws AuthorizationException {
    // ToDo: Try to understand why refresh seems not to work with mysql!?
    TestEntity find = entityManager.find(TestEntity.class, referenceEntity.getId());
    assertEquals(DESCRIPTION, find.getDescription());
    assertEquals(SUMMARY, find.getSummary());
    // make some changes to the entity
    find.setSummary(DESCRIPTION);
    find.setDescription(DESCRIPTION_NEW);
    // do refresh
    find = entityManager.refresh(find);
    // Values should be the same as before...
    assertEquals(referenceEntity.getId(), find.getId());
    assertEquals(referenceEntity.getDescription(), find.getDescription());
    assertEquals(referenceEntity.getSummary(), find.getSummary());
    assertEquals(DESCRIPTION, find.getDescription());
    assertEquals(SUMMARY, find.getSummary());
  }

  /**
   * Test if entity if update works.
   */
  @Test
  public void testUpdateEntity() throws AuthorizationException {
    TestEntity find = entityManager.find(TestEntity.class, referenceEntity.getId());
    // make one change to entity
    find.setDescription(DESCRIPTION_NEW);
    find.setSummary(SUMMARY);
    // save changed entity
    entityManager.save(find);
    // Load find from database
    find = entityManager.find(TestEntity.class, referenceEntity.getId());
    assertEquals(referenceEntity.getId(), find.getId());
    assertEquals(referenceEntity.getSummary(), find.getSummary());
    assertEquals(DESCRIPTION_NEW, find.getDescription());
  }

  /**
   * Test if entity if update works.
   */
  @Test
  public void testRemoveEntity() throws AuthorizationException {
    int noOfTestEntities = entityManager.find(TestEntity.class).size();
    TestEntity newEntity = createTestEntityReference();
    // add new entity
    entityManager.save(newEntity);
    boolean contains = entityManager.contains(newEntity);
    assertTrue(contains);
    List<TestEntity> resultList = entityManager.find(TestEntity.class);
    assertEquals(noOfTestEntities + 1, resultList.size());

    // remove entity from database
    entityManager.remove(newEntity);
    resultList = entityManager.find(TestEntity.class);
    assertEquals(noOfTestEntities, resultList.size());
    contains = entityManager.contains(newEntity);
    assertFalse(contains);
  }

//  /**
//   * Test for search between two 'references'.
//   */
//  @Test
//  public void test11_FindWith2References() {
//    TestEntity EntityForTestEleven = createTestEntityReference();
//    entityManager.save(EntityForTestEleven);    
//    int noOfTestEntities = entityManager.find(EntityForTestEleven).size();
//    TestEntity dummy;
//    String[] testSummaryFirst = {"a", "a", "e", "a", null, "0", "a", "z", "z", "x", "z", null, "0", "z", "e", "0", "A", "_", "0", "A", null, null, null, null};
//    String[] testSummaryLast = {"a", "e", "a", null, "a", "a", "0", "z", "x", "z", null, "z", "z", "0", "e", "0", "A", null, null, null, "_", "0", "A", null};
//    int[] noOfResults = {0, 4, 0, 27, 0, 0, 0, 0, 0, 2, 1, 26, 26, 0, 0, 0, 0, 27, 27, 27, 0, 0, 0, 27};
//    TestEntity first = new TestEntity();
//    TestEntity last = new TestEntity();
//    char[] prefix = {'x', '_'};
//    for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
//      prefix[0] = alphabet;
//      dummy = createTestEntityReference(new String(prefix));
//      entityManager.save(dummy);
//      noOfTestEntities++;
//    }
//    List<TestEntity> resultList = entityManager.find(EntityForTestEleven);
//    assertEquals(resultList.size(), noOfTestEntities);
//    for (int index1 = 0; index1 < noOfResults.length; index1++) {
//      first.setSummary(testSummaryFirst[index1]);
//      last.setSummary(testSummaryLast[index1]);
//      resultList = entityManager.find(first, last);
//      assertEquals(resultList.size(), noOfResults[index1]);
//    }
//    entityManager.remove(EntityForTestEleven);
//  }
  private TestEntity createTestEntityReference() {
    TestEntity testEntity = new TestEntity();
    testEntity.setDescription(DESCRIPTION);
    testEntity.setSummary(SUMMARY);
    return testEntity;
  }

  private static TestEntity createTestEntityReference(String prefix) {
    TestEntity testEntity = new TestEntity();
    testEntity.setDescription(prefix + DESCRIPTION);
    testEntity.setSummary(prefix + SUMMARY);
    return testEntity;
  }
}
