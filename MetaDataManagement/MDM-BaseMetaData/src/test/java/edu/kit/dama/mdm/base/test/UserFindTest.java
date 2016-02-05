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
package edu.kit.dama.mdm.base.test;

import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import org.junit.runners.Parameterized;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import static org.junit.Assert.*;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author niedermaier
 */
@Ignore
@RunWith(Parameterized.class)
public class UserFindTest {

  private static Logger LOGGER = LoggerFactory.getLogger(UserFindTest.class);
  private static IMetaDataManager entityManager = null;
  static int noOfTestEntities;
  static UserData first, last;
  private static final String FIRST_NAME = "Erika";
  private static final String LAST_NAME = "Mustermann";
  //<editor-fold defaultstate="collapsed" desc="Fields used by test constructor">
  /* Field used by test constructor. */
  String testSummaryFirst;
  /* Field used by test constructor. */
  String testSummaryLast;
  /* Field used by test constructor. */
  int noOfResults;

  public UserFindTest(String testSummaryFirst, String testSummaryLast, String noOfResult) {
    this.testSummaryFirst = testSummaryFirst;
    this.testSummaryLast = testSummaryLast;
    this.noOfResults = (Integer.valueOf(noOfResult));
  }
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
            new Object[]{null, "A", "0"},
            new Object[]{null, null, "26"});
  }
  //</editor-fold>

  @BeforeClass
  public static void prepareClass() throws UnauthorizedAccessAttemptException {
    MetaDataManagementHelper.replaceConfig(null);
    // create entity Manager
      entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-BaseMetaData-Test");

    assertEquals(0, entityManager.find(UserData.class).size());
    noOfTestEntities = 0;

    first = new UserData();
    last = new UserData();
    char[] prefix = {'x', '_'};
    UserData dummy;
    for (char alphabet = 'a'; alphabet <= 'z'; alphabet++) {
      prefix[0] = alphabet;
      dummy = createUserReference(new String(prefix));
      entityManager.save(dummy);
      noOfTestEntities++;
    }
    LOGGER.debug("Create a database with {} entries of class {}", noOfTestEntities, UserData.class.getName());
    assertEquals('z' - 'a' + 1, noOfTestEntities);
  }

  @AfterClass
  public static void releaseTestAfterTest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    List<UserData> find = entityManager.find(UserData.class);
    for (UserData item : find) {
      entityManager.remove(item);
    }
    entityManager.close();
// Alternativ als ausgeschriebene for-Schleife:
//    for (int i = 0; i < find.size(); i++) {
//      entityManager.remove(find.get(i));
//    }

  }

  @Test
  public void testFindWith2References() throws UnauthorizedAccessAttemptException {
    List<UserData> resultList = entityManager.find(UserData.class);
    assertEquals(resultList.size(), noOfTestEntities);
    first.setFirstName(testSummaryFirst);
    last.setFirstName(testSummaryLast);
    resultList = entityManager.find(first, last);
    assertEquals(resultList.size(), noOfResults);
  }
  private static UserData createUserReference(String prefix) {
    UserData User = new UserData();
    User.setFirstName(prefix + FIRST_NAME);
    User.setLastName(prefix + LAST_NAME);
    User.setEmail(FIRST_NAME + "." + LAST_NAME + "@kit.edu");
    User.setDistinguishedName("O=GermanGrid,OU=KIT,CN=" + FIRST_NAME + " " + LAST_NAME);
    User.setValidFrom(new Date());

    Calendar until = Calendar.getInstance();
    until.set(2099, 12, 31);
    User.setValidUntil(until.getTime());
    return User;
  }
}
