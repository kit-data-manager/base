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
package edu.kit.dama.mdm.base.helper;

import edu.kit.dama.mdm.tools.BaseMetaDataHelper;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.base.test.InvestigationTest;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hartmann-v
 */
@Ignore
public class BaseMetaDataHelperTest {

  Investigation investigation = null;
  private static final Logger LOGGER = LoggerFactory.getLogger(InvestigationTest.class);
  private static IMetaDataManager entityManager = null;
  private static Investigation referenceEntity = null;
  private static final String NOTE = "Erika";
  private static final String TOPIC = "topic";
  private static final Participant PARTICIPANT = new Participant(new UserData());
  private static Date START_DATE = new Date();
  private static Date END_DATE = new Date(2099, 12, 31, 23, 59, 59);

  public BaseMetaDataHelperTest() {
  }

  @BeforeClass
  public static void prepareClassForTests() {
    MetaDataManagementHelper.replaceConfig(null);
    entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-BaseMetaData-Test");
    entityManager.setAuthorizationContext(AuthorizationContext.factorySystemContext());
  }
  
  @AfterClass
  public static void releaseClassForTests() {
    entityManager.close();
  }

  @After
  public void releaseTest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    List<DigitalObject> allDigitalObject = entityManager.find(DigitalObject.class);
    for (DigitalObject item : allDigitalObject) {
      entityManager.remove(item);
    }
    List<Investigation> all = entityManager.find(Investigation.class);
    for (Investigation item : all) {
      entityManager.remove(item);
    }
    List<Study> allStudy = entityManager.find(Study.class);
    for (Study item : allStudy) {
      entityManager.remove(item);
    }
  }

  @Before
  public void prepareTest() {
    investigation = new Investigation();
  }

  /**
   * Test of createDummyInvestigation method, of class BaseMetaDataHelper.
   */
  @Test
  public void testCreateDummyInvestigation() throws UnauthorizedAccessAttemptException {
    Study study = Study.factoryNewStudy();
    study.setTopic(TOPIC);
    BaseMetaDataHelper.createDummyInvestigation(study);

    entityManager.persist(study);
    List<Investigation> all = entityManager.find(Investigation.class);
    List<Study> allStudy = entityManager.find(Study.class);
    List<DigitalObject> allDigitalObject = entityManager.find(DigitalObject.class);
    assertEquals(allStudy.size(), 1);
    assertEquals(all.size(), 1);
    assertEquals(allDigitalObject.size(), 1);
  }

  /**
   * Test of createDummyInvestigation method, of class BaseMetaDataHelper.
   */
  @Test
  public void testCreateDummyInvestigationWithNull() throws UnauthorizedAccessAttemptException {
    Study study = Study.factoryNewStudy();
    study.setTopic(TOPIC);
    BaseMetaDataHelper.createDummyInvestigation(null);

    entityManager.persist(study);
    List<Investigation> all = entityManager.find(Investigation.class);
    List<Study> allStudy = entityManager.find(Study.class);
    List<DigitalObject> allDigitalObject = entityManager.find(DigitalObject.class);
    assertEquals(allStudy.size(), 1);
    assertEquals(all.size(), 0);
    assertEquals(allDigitalObject.size(), 0);
  }

  /**
   * Test of createDummyDigitalObject method, of class BaseMetaDataHelper.
   */
  @Test
  public void testCreateDummyDigitalObject() throws UnauthorizedAccessAttemptException {
    Investigation investigation = new Investigation();
    investigation.setTopic(TOPIC);
    BaseMetaDataHelper.createDummyDigitalObject(investigation);

    entityManager.persist(investigation);
    List<Investigation> all = entityManager.find(Investigation.class);
    List<Study> allStudy = entityManager.find(Study.class);
    List<DigitalObject> allDigitalObject = entityManager.find(DigitalObject.class);
    assertEquals(allStudy.size(), 0);
    assertEquals(all.size(), 1);
    assertEquals(allDigitalObject.size(), 1);
  }

  /**
   * Test of createDummyDigitalObject method, of class BaseMetaDataHelper.
   */
  @Test
  public void testCreateDummyDigitalObjectWithNull() throws UnauthorizedAccessAttemptException {
    Investigation investigation = new Investigation();
    investigation.setTopic(TOPIC);
    BaseMetaDataHelper.createDummyDigitalObject(null);

    entityManager.persist(investigation);
    List<Investigation> all = entityManager.find(Investigation.class);
    List<Study> allStudy = entityManager.find(Study.class);
    List<DigitalObject> allDigitalObject = entityManager.find(DigitalObject.class);
    assertEquals(allStudy.size(), 0);
    assertEquals(all.size(), 1);
    assertEquals(allDigitalObject.size(), 0);
  }
}
