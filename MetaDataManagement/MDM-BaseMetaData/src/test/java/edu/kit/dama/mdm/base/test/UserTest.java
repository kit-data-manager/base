/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.mdm.base.test;

import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import org.junit.After;
import java.util.Date;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.UserData;
import java.util.Calendar;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author hartmann-v
 */
@Ignore
public class UserTest {

  /** For logging purposes. */
  private static Logger LOGGER = LoggerFactory.getLogger(UserTest.class);
  /** Manager holding all Information. */
  private static IMetaDataManager entityManager = null;
  private static UserData referenceEntity = null;
  private static final String FIRST_NAME = "Erika";
  private static final String FIRST_NAME_NEW = "Hans";
  private static final String LAST_NAME = "Mustermann";
  private static long instanceCounter = 0;

  @BeforeClass
  public static void prepareClass() {
    MetaDataManagementHelper.replaceConfig(null);
    LOGGER.info("Prepare class 'UserTest' for Tests!");
    // create entity Manager if entiy Manager is null otherway use the existing entity Manager
      entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-BaseMetaData-Test");
    assertNotNull(entityManager);
  }

  @AfterClass
  public static void releaseClass() {
    LOGGER.info("All tests done!\nRelease class 'UserTest' for Tests!");
    entityManager.close();
  }

  @After
  public void releaseTest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    List<UserData> find = entityManager.find(UserData.class);
    for (UserData item : find) {
      entityManager.remove(item);
    }
// Alternativ als ausgeschriebene for-Schleife:
//    for (int i = 0; i < find.size(); i++) {
//      entityManager.remove(find.get(i));
//    }
  }

  @Test(expected = NullPointerException.class)
  public void test01_DataBaseFailure() {
    IMetaDataManager noEntityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager(null, null);
    assertEquals(noEntityManager.toString(), "Should not happen!");
  }

  @Test
  public void test02_DataBaseExists() {
    assertNotNull(entityManager);
    assertEquals(entityManager.getClass().getCanonicalName(), "edu.kit.dama.mdm.authorization.SecureMetaDataManager");
    
  }

  /**
   * Test if all existing Entities are shown as valid objects. 
   */
  @Test
  public void test03_Entities() throws UnauthorizedAccessAttemptException {
    UserData User = new UserData();

    assertEquals(entityManager.contains(User), false);
  }

  /**
   * Test if all existing Entities are shown as valid objects. 
   */
  @Test
  public void test04_NoEntities() throws UnauthorizedAccessAttemptException {
    try{
    assertEquals(entityManager.contains(this), false);
  }catch(IllegalArgumentException iae) {
    LOGGER.error("HALLO VOLKER HIER IST DEINE ROLLBACKEXCEPTION: ",iae);
  }
  }

  /**
   * Test persist entity to database.
   */
  @Test
  public void test05_SaveEntity() throws UnauthorizedAccessAttemptException {
    UserData userForTestFive = createUserReference();
    Long idBefore = userForTestFive.getUserId();
    entityManager.save(userForTestFive);
    Long idAfter = userForTestFive.getUserId();
    assertNotNull(idAfter);
    assertNotSame(idBefore, idAfter);
    referenceEntity = userForTestFive;
    assertEquals(entityManager.contains(referenceEntity), true);
  }

  /**
   * Test if entity if find works.
   */
  @Test
  public void test06_FindEntity() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    UserData userForTesSix = createUserReference();
    entityManager.save(userForTesSix);
    UserData find = entityManager.find(userForTesSix.getClass(), userForTesSix.getUserId());
    assertEquals(find.getUserId(), userForTesSix.getUserId());
    assertEquals(find.getFirstName(), userForTesSix.getFirstName());
    assertEquals(find.getLastName(), userForTesSix.getLastName());
    assertEquals(find.getFirstName(), FIRST_NAME);
    assertEquals(find.getLastName(), LAST_NAME);
    assertEquals(find, userForTesSix);
  }

  /**
   * Test if entity if find works.
   */
  @Test
  public void test07_FindAllEntities() throws UnauthorizedAccessAttemptException {
    UserData user = createUserReference();
    entityManager.save(user);
    List<UserData> find = entityManager.find(UserData.class);
    assertEquals(find.size(), 1);
    UserData firstElement = find.get(0);
    assertEquals(firstElement.getUserId(), user.getUserId());
    assertEquals(firstElement.getFirstName(), user.getFirstName());
    assertEquals(firstElement.getLastName(), user.getLastName());
    assertEquals(firstElement.getFirstName(), FIRST_NAME);
    assertEquals(firstElement.getLastName(), LAST_NAME);
    assertEquals(firstElement, user);
  }

  /**
   * Test if entity if refresh works.
   */
  @Test
  @Ignore("Will not work with the mysql data base")
  public void test08_RefreshEntity() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    entityManager.save(referenceEntity);
    UserData find = entityManager.find(referenceEntity.getClass(), referenceEntity.getUserId());
    // make some changes to the entity
    find.setLastName(FIRST_NAME);
    find.setLastName(FIRST_NAME_NEW);
    // do refresh
    entityManager.refresh(find);
    // Values should be the same as before...
    assertEquals(find.getUserId(), referenceEntity.getUserId());
    assertEquals(find.getFirstName(), referenceEntity.getFirstName());
    assertEquals(find.getLastName(), referenceEntity.getLastName());
    assertEquals(find.getFirstName(), FIRST_NAME);
    assertEquals(find.getLastName(), LAST_NAME);
    assertEquals(find, referenceEntity);
  }

  /**
   * Test if entity if update works.
   */
  @Test
  public void test09_UpdateEntity() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    UserData userForTestNine = createUserReference();
    entityManager.save(userForTestNine);
    UserData find = entityManager.find(userForTestNine.getClass(), userForTestNine.getUserId());
    // make one change to entity
    find.setFirstName(FIRST_NAME_NEW);
    // save changed entity
    entityManager.save(find);
    // Load find from database
    find = entityManager.find(userForTestNine.getClass(), userForTestNine.getUserId());
    assertEquals(find.getUserId(), userForTestNine.getUserId());
    assertSame(find.getFirstName(), userForTestNine.getFirstName());
    assertEquals(find.getLastName(), userForTestNine.getLastName());
    assertEquals(find.getFirstName(), FIRST_NAME_NEW);
    assertEquals(find.getLastName(), LAST_NAME);
    assertEquals(find, userForTestNine);
  }

  /**
   * Test if entity if update works.
   */
  @Test
  public void test10_RemoveEntity() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    entityManager.save(referenceEntity);
    int noOfTestEntities = entityManager.find(UserData.class).size();
    UserData newEntity = createUserReference();
    // add new entity
    entityManager.save(newEntity);
    boolean contains = entityManager.contains(newEntity);
    assertTrue(contains);
    List<UserData> resultList = entityManager.find(UserData.class);
    assertEquals(resultList.size(), noOfTestEntities + 1);

    // remove entity from database
    entityManager.remove(newEntity);
    resultList = entityManager.find(UserData.class);
    assertEquals(resultList.size(), noOfTestEntities);
    contains = entityManager.contains(newEntity);
    assertFalse(contains);
  }

  @Test
  public void test11_setUserId() {
    boolean exception = false;
    try {
      Long newUserId = Long.MIN_VALUE;
      referenceEntity = createUserReference();
      entityManager.save(referenceEntity);
      referenceEntity.setUserId(newUserId);
      assertEquals(referenceEntity.getUserId().toString(), newUserId.toString());
      entityManager.remove(referenceEntity);
    } catch (Exception e) {
      exception = true;
      LOGGER.warn("exception found!");
    }
    assertEquals(exception, true);
  }

  @Test
  public void test12_setUserIdFailure() {
    boolean exception = false;
    try {
      Long newUserId = null;
      referenceEntity = createUserReference();
      referenceEntity = entityManager.save(referenceEntity);
      Long id = referenceEntity.getUserId();
      referenceEntity.setUserId(newUserId);
      if (entityManager.contains(referenceEntity)) {
        assertNotNull(referenceEntity.getUserId());
      }
      referenceEntity.setUserId(id);
      entityManager.remove(referenceEntity);
    } catch (Exception e) {
      exception = true;
      LOGGER.warn("exception found: ");
    }
    assertEquals(exception, false);
  }

  /**
   * Test for search between two 'references'.
   */
//  @Test
//  public void test11_FindWith2References() {
//    int noOfTestEntities = entityManager.find(referenceEntity).size();
//    UserData dummy;
//    String[] testSummaryFirst = {"a", "a", "e", "a", null, "0", "a", "z", "z", "x", "z", null, "0", "z", "e", "0", "A", "_", "0", "A", null, null, null, null};
//    String[] testSummaryLast = {"a", "e", "a", null, "a", "a", "0", "z", "x", "z", null, "z", "z", "0", "e", "0", "A", null, null, null, "_", "0", "A", null};
//    int[] noOfResults = {0, 4, 0, 26, 1, 1, 0, 0, 0, 2, 1, 26, 26, 0, 0, 0, 0, 26, 27, 27, 1, 0, 0, 27};
//    UserData first = new UserData();
//    UserData last = new UserData();
//    char[] prefix = {'x', '_'};
//    for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
//      prefix[0] = alphabet;
//      dummy = createUserReference(new String(prefix));
//      entityManager.save(dummy);
//      noOfTestEntities++;
//    }
//    List<User> resultList = entityManager.find(referenceEntity);
//    assertEquals(resultList.size(), noOfTestEntities);
//    for (int index1 = 0; index1 < noOfResults.length; index1++) {
//      first.setLastName(testSummaryFirst[index1]);
//      last.setLastName(testSummaryLast[index1]);
//      resultList = entityManager.find(first, last);
//      assertEquals(resultList.size(), noOfResults[index1]);
//    }
//  }
//  /**
//   * Test for search between two 'references'.
//   */
//  @Test
//  public void test12_FindWith2ReferencesAnd2Properties() {
//    int noOfTestEntities = entityManager.find(referenceEntity).size();
//    String[] testSummaryFirst = {"a", "a", "e", "a", null, "0", "a", "a", "a", "x", "z", null, "0", "z", "e", "0", "A", "_", "0", "A", null, null, null, null};
//    String[] testDescriptionFirst = {"a", "a", "e", "a", null, "d", "f", "z", "z", "a", "z", null, "0", "z", "e", "0", "A", "_", "0", "A", null, null, null, null};
//    String[] testSummaryLast = {"z", "e", "a", null, "a", "e", "e", "z", "z", "z", null, "z", "z", "0", "e", "0", "A", null, null, null, "_", "0", "A", null};
//    String[] testDescriptionLast = {"e", "e", "a", null, "a", "z", "m", "z", "x", "e", null, "z", "z", "0", "e", "0", "A", null, null, null, "_", "0", "A", null};
//    int[] noOfResults = {4, 4, 0, 26, 1, 1, 0, 0, 0, 0, 1, 26, 26, 0, 0, 0, 0, 26, 27, 27, 1, 0, 0, 27};
//    UserData first = new UserData();
//    UserData last = new UserData();
//
//    List<User> resultList = entityManager.find(referenceEntity);
//    assertEquals(resultList.size(), noOfTestEntities);
//    for (int index1 = 0; index1 < noOfResults.length; index1++) {
//      first.setFirstName(testSummaryFirst[index1]);
//      last.setFirstName(testSummaryLast[index1]);
//      first.setLastName(testDescriptionFirst[index1]);
//      last.setLastName(testDescriptionLast[index1]);
//      resultList = entityManager.find(first, last);
//      if (resultList.size() != noOfResults[index1]) {
//        LOGGER.debug("The result list contains {} results." + resultList.size());
//        for (UserData item : resultList) {
//          LOGGER.debug(item.toString());
//        }
//      }
//      assertEquals(resultList.size(), noOfResults[index1]);
//    }
//  }
  private UserData createUserReference() {
    UserData User = new UserData();
    User.setFirstName(FIRST_NAME);
    User.setLastName(LAST_NAME);
    User.setEmail(FIRST_NAME + "." + LAST_NAME + "@kit.edu");
    User.setDistinguishedName("O=GermanGrid,OU=KIT,CN=" + FIRST_NAME + " " + LAST_NAME + "_" + instanceCounter);
    User.setValidFrom(new Date());

    Calendar until = Calendar.getInstance();
    until.set(2099, 12, 31);
    User.setValidUntil(until.getTime());
    return User;
  }
}
