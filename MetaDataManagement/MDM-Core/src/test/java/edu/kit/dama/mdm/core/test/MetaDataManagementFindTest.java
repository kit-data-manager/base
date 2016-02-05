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
public class MetaDataManagementFindTest {

  private static Logger LOGGER = LoggerFactory.getLogger(MetaDataManagementFindTest.class);
  private static IMetaDataManager entityManager = null;
  private static final String DESCRIPTION = "reference description";
  private static final String SUMMARY = "reference summary";
  static int noOfTestEntities;
  static TestEntity first, last;
  //<editor-fold defaultstate="collapsed" desc="Fields used by test constructor">
  /* Field used by test constructor. */
  String testSummaryFirst;
  /* Field used by test constructor. */
  String testSummaryLast;
  /* Field used by test constructor. */
  int noOfResults;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="parameters for the testclass">
  @Parameters
  public static List<Object[]> testData() {
    return Arrays.asList(
            //firstSummary, lastSummary, result.
            new Object[]{"a", "a", "0"}, //0
            new Object[]{"a", "e", "4"},
            new Object[]{"e", "a", "0"},
            new Object[]{"a", null, "26"},
            new Object[]{null, "a", "0"},
            //firstSummary, lastSummary, result.
            new Object[]{"0", "a", "0"}, //5
            new Object[]{"a", "0", "0"},
            new Object[]{"z", "z", "0"},
            new Object[]{"z", "x", "0"},
            new Object[]{"x", "z", "2"},
            //firstSummary, lastSummary, result.
            new Object[]{"z", null, "1"}, //10
            new Object[]{null, "z", "25"},
            new Object[]{"0", "z", "25"},
            new Object[]{"z", "0", "0"},
            new Object[]{"e", "e", "0"},
            //firstSummary, lastSummary, result.
            new Object[]{"0", "0", "0"}, //15
            new Object[]{"A", "A", "0"},
            new Object[]{"_", null, "26"},
            new Object[]{"0", null, "26"},
            new Object[]{"A", null, "26"},
            //firstSummary, lastSummary, result.
            new Object[]{null, "_", "0"}, //20
            new Object[]{null, "0", "0"},
            new Object[]{null, "A", "0"}
//            , new Object[]{null, null, "26"} This case is not valid any more
            );
  }
  //</editor-fold>

  public MetaDataManagementFindTest(String testSummaryFirst, String testSummaryLast, String noOfResult) {
    this.testSummaryFirst = testSummaryFirst;
    this.testSummaryLast = testSummaryLast;
    this.noOfResults = Integer.valueOf(noOfResult);

  }

  @BeforeClass
  public static void prepareClass() {
    try {
      MetaDataManagementHelper.replaceConfig(null);
      // create entity Manager
      entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-Core-Test");
       entityManager.setAuthorizationContext(SecurityUtil.adminContext);
    
      for (TestEntity item : entityManager.find(TestEntity.class)) {
        entityManager.remove(item);
      }

      assertEquals(0, entityManager.find(TestEntity.class).size());
      noOfTestEntities = 0;
   
      char[] prefix = {'x', '_'};
      TestEntity dummy;
      for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
        prefix[0] = alphabet;
        dummy = createTestEntityReference(new String(prefix));
        entityManager.save(dummy);
        noOfTestEntities++;
      }
      LOGGER.debug("Create a database with {} entries of class {}", noOfTestEntities, TestEntity.class.getName());
      assertEquals('z' - 'a' + 1, noOfTestEntities);
    } catch (AuthorizationException ex) {
      java.util.logging.Logger.getLogger(MetaDataManagementFindTest.class.getName()).log(Level.SEVERE, null, ex);
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
      java.util.logging.Logger.getLogger(MetaDataManagementFindTest.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  @Test
  public void findTestDescription() throws UnauthorizedAccessAttemptException {
      first = new TestEntity();
      last = new TestEntity();
      first.setDescription(testSummaryFirst);
      last.setDescription(testSummaryLast);
      List<TestEntity> resultList = entityManager.find(first, last);
      assertEquals(resultList.size(), noOfResults);
      if (testSummaryFirst == null) {
        first = null;
      }
      if (testSummaryLast == null) {
        last = null;
      }
      resultList = entityManager.find(first, last);
      assertEquals(resultList.size(), noOfResults);
  }

  @Test
  public void findTestSummary() throws UnauthorizedAccessAttemptException {
    first = new TestEntity();
    last = new TestEntity();
    first.setSummary(testSummaryFirst);
    last.setSummary(testSummaryLast);
    List<TestEntity> resultList = entityManager.find(first, last);
    assertEquals(resultList.size(), noOfResults);
    if (testSummaryFirst == null) {
      first = null;
    }
    if (testSummaryLast == null) {
      last = null;
    }
    resultList = entityManager.find(first, last);
    assertEquals(resultList.size(), noOfResults);
  }

  @Test
  public void findTestBoth() throws UnauthorizedAccessAttemptException {
    first = new TestEntity();
    last = new TestEntity();
    first.setSummary(testSummaryFirst);
    last.setDescription(testSummaryLast);
    List<TestEntity> resultList = entityManager.find(first, last);
    assertEquals(resultList.size(), noOfResults);
    if (testSummaryFirst == null) {
      first = null;
    }
    if (testSummaryLast == null) {
      last = null;
    }
    resultList = entityManager.find(first, last);
    assertEquals(resultList.size(), noOfResults);
  }

  private static TestEntity createTestEntityReference(String prefix) {
    TestEntity testEntity = new TestEntity();
    testEntity.setDescription(prefix + DESCRIPTION);
    testEntity.setSummary(prefix + SUMMARY);
    return testEntity;
  }
}
