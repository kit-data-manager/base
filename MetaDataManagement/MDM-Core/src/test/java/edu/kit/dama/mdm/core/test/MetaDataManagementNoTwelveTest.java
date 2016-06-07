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

import edu.kit.dama.mdm.core.test.entities.TestEntity;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author niedermaier
 */
@RunWith(Parameterized.class)
public class MetaDataManagementNoTwelveTest {

  private static IMetaDataManager entityManager = null;
  private static final String DESCRIPTION = "reference description";
  private static final String DESCRIPTION_NEW = "updated reference description";
  private static final String SUMMARY = "reference summary";
  String testSummaryFirst;
  String testSummaryLast;
  String testDescriptionFirst;
  String testDescriptionLast;
  int noOfResults;
  static int noOfTestEntities;
  static TestEntity first, last;
  private static Logger LOGGER = LoggerFactory.getLogger(MetaDataManagementFindTest.class);

  @Parameters
  public static List<Object[]> testData() {
    return Arrays.asList(
            // firstSummary, lastSummary,result,firstDescription, lastDescription
            new Object[]{"a", "a", "0", "a", "e"}, //0
            new Object[]{"a", "e", "4", "a", "e"},
            new Object[]{"e", "a", "0", "e", "a"},
            new Object[]{"a", null, "26", "a", null},
            new Object[]{null, "a", "0", null, "a"},
            // firstSummary, lastSummary,result,firstDescription, lastDescription
            new Object[]{"0", "a", "0", "d", "z"}, //5
            new Object[]{"a", "0", "0", "f", "m"},
            new Object[]{"z", "z", "0", "z", "z"},
            new Object[]{"z", "x", "0", "z", "x"},
            new Object[]{"x", "z", "0", "a", "e"},
            // firstSummary, lastSummary,result,firstDescription, lastDescription
            new Object[]{"z", null, "1", "z", null}, //10
            new Object[]{null, "z", "25", null, "z"},
            new Object[]{"0", "z", "25", "0", "z"},
            new Object[]{"z", "0", "0", "z", "0"},
            new Object[]{"e", "e", "0", "e", "e"},
            // firstSummary, lastSummary,result,firstDescription, lastDescription
            new Object[]{"0", "0", "0", "0", "0"}, //15
            new Object[]{"A", "A", "0", "A", "A"},
            new Object[]{"_", null, "26", "_", null},
            new Object[]{"0", null, "26", "0", null},
            new Object[]{"A", null, "26", "A", null},
            // firstSummary, lastSummary,result,firstDescription, lastDescription
            new Object[]{null, "_", "0", null, "_"}, //20
            new Object[]{null, "0", "0", null, "0"},
            new Object[]{null, "A", "0", null, "A"},
            new Object[]{null, null, "26", null, null});
  }

  public MetaDataManagementNoTwelveTest(String testSummaryFirst, String testSummaryLast, String noOfResult, String testDescriptionFirst, String testDescriptionLast) {
    this.testSummaryFirst = testSummaryFirst;
    this.testSummaryLast = testSummaryLast;
    this.noOfResults = Integer.valueOf(noOfResult);
    this.testDescriptionFirst = testDescriptionFirst;
    this.testDescriptionLast = testDescriptionLast;

  }

  @BeforeClass
  public static void prepareClass() {
    try {
      MetaDataManagementHelper.replaceConfig(null);

      if (entityManager == null) {
        entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-Core-Test");

      }
      entityManager.setAuthorizationContext(SecurityUtil.adminContext);
      assertNotNull(entityManager);

      for (TestEntity item : entityManager.find(TestEntity.class)) {
        entityManager.remove(item);
      }

      noOfTestEntities = entityManager.find(TestEntity.class).size();
      assertEquals(0, noOfTestEntities);

      first = new TestEntity();
      last = new TestEntity();
      char[] prefix = {'x', '_'};
      TestEntity dummy = null;
      for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
        prefix[0] = alphabet;
        dummy = createTestEntityReference(new String(prefix));
        entityManager.save(dummy);
        noOfTestEntities++;
      }
    } catch (AuthorizationException ex) {
      java.util.logging.Logger.getLogger(MetaDataManagementNoTwelveTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @AfterClass
  public static void releaseTestAfterTest() {
    entityManager.setAuthorizationContext(SecurityUtil.adminContext);
    try {
      List<TestEntity> find = entityManager.find(TestEntity.class);
      for (TestEntity item : find) {
        entityManager.remove(item);
      }
      entityManager.close();
  // Alternativ als ausgeschriebene for-Schleife:
  //    for (int i = 0; i < find.size(); i++) {
  //      entityManager.remove(find.get(i));
  //    }
    } catch (AuthorizationException ex) {
      java.util.logging.Logger.getLogger(MetaDataManagementNoTwelveTest.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  @Test
  public void testFindWithReferencesAnd2Properties() {
    try {
      noOfTestEntities = entityManager.find(TestEntity.class).size();

      first = new TestEntity();
      last = new TestEntity();

      List<TestEntity> resultList = entityManager.find(TestEntity.class);
      assertEquals(resultList.size(), noOfTestEntities);

      first.setSummary(testSummaryFirst);
      last.setSummary(testSummaryLast);
      first.setDescription(testDescriptionFirst);
      last.setDescription(testDescriptionLast);
      resultList = entityManager.find(first, last);
      if (resultList.size() != noOfResults) {
        LOGGER.debug("The result list contains {} results." + resultList.size());
        for (TestEntity item : resultList) {
          LOGGER.debug(item.toString());
        }
      }
      assertEquals(resultList.size(), noOfResults);
    } catch (UnauthorizedAccessAttemptException ex) {
      java.util.logging.Logger.getLogger(MetaDataManagementNoTwelveTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

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
//  public void test12_FindWith2ReferencesAnd2Properties() {
//    TestEntity EntityForTestTwelve = createTestEntityReference();
//    entityManager.save(EntityForTestTwelve);
//    int noOfTestEntities = entityManager.find(EntityForTestTwelve).size();
//    String[] testSummaryFirst = {"a", "a", "e", "a", null, "0", "a", "a", "a", "x", "z", null, "0", "z", "e", "0", "A", "_", "0", "A", null, null, null, null};
//    String[] testDescriptionFirst = {"a", "a", "e", "a", null, "d", "f", "z", "z", "a", "z", null, "0", "z", "e", "0", "A", "_", "0", "A", null, null, null, null};
//    String[] testSummaryLast = {"z", "e", "a", null, "a", "e", "e", "z", "z", "z", null, "z", "z", "0", "e", "0", "A", null, null, null, "_", "0", "A", null};
//    String[] testDescriptionLast = {"e", "e", "a", null, "a", "z", "m", "z", "x", "e", null, "z", "z", "0", "e", "0", "A", null, null, null, "_", "0", "A", null};
//    int[] noOfResults = {4, 4, 0, 27, 0, 1, 0, 0, 0, 0, 1, 26, 26, 0, 0, 0, 0, 27, 27, 27, 0, 0, 0, 27};
//    TestEntity first = new TestEntity();
//    TestEntity last = new TestEntity();
//
//    List<TestEntity> resultList = entityManager.find(EntityForTestTwelve);
//    assertEquals(resultList.size(), noOfTestEntities);
//    for (int index1 = 0; index1 < noOfResults.length; index1++) {
//      first.setSummary(testSummaryFirst[index1]);
//      last.setSummary(testSummaryLast[index1]);
//      first.setDescription(testDescriptionFirst[index1]);
//      last.setDescription(testDescriptionLast[index1]);
//      resultList = entityManager.find(first, last);
//      if (resultList.size() != noOfResults[index1]) {
//        LOGGER.debug("The result list contains {} results." + resultList.size());
//        for (TestEntity item: resultList) {
//          LOGGER.debug(item.toString());
//        }
//      }
//      assertEquals(resultList.size(), noOfResults[index1]);
//    }
//    entityManager.remove(EntityForTestTwelve);
//  }