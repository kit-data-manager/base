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

package edu.kit.dama.mdm.base.test;

import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import org.junit.After;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.base.xml.TestXML;
import java.util.List;
import java.util.logging.Level;
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
public class XmlTest {

  /** For logging purposes. */
  private static Logger LOGGER = LoggerFactory.getLogger(XmlTest.class);
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
    LOGGER.warn("This test is only for locale usage on 'ipehartmann4'!");
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
  @Test
  public void createXML() {
    try {
      TestXML.main(null);
    } catch (Exception ex) {
      java.util.logging.Logger.getLogger(DigitalObjectTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
