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
import java.util.HashSet;
import java.util.Set;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import org.junit.Before;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.UserData;
import java.util.Date;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author niedermaier
 */
@Ignore
public class CreateDataBaseWithExistReferenceTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateDatabaseWithNotExistingReferencesTest.class);
  private static IMetaDataManager entityManager = null;
  private static Study studyEntity = null;
  private static UserData userEntity = null;
  private static final String FIRST_NAME = "Erika";
  private static final String LAST_NAME = "Musterman";
  private static final String NOTE = "write a note here.";
  private static final String LEGAL_NOTE = "write a legal note here.";
  private static final String TOPIC = "topic";
  private static Date END_DATE = new Date(2099, 12, 31, 23, 59, 59);
  private static Date START_DATE = new Date(2009, 12, 31, 23, 59, 59);
  private static long instanceCounter = 0;
  @Before
  public void prepareTest() throws UnauthorizedAccessAttemptException {
    MetaDataManagementHelper.replaceConfig(null);
    entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-BaseMetaData-Test");
    userEntity = createUserReference();
    studyEntity = createStudyReference();

    entityManager.save(userEntity);
    entityManager.save(studyEntity);
  }

  @After
  public void releaseTest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    List<DigitalObject> allBase = entityManager.find(DigitalObject.class);
    for (DigitalObject itemBase : allBase) {
      entityManager.remove(itemBase);
    }

    List<Study> allStudy = entityManager.find(Study.class);
    for (Study itemStudy : allStudy) {
      entityManager.remove(itemStudy);
    }

    List<UserData> allUser = entityManager.find(UserData.class);
    for (UserData itemUser : allUser) {
      entityManager.remove(itemUser);
    }

    List<OrganizationUnit> allOrganizationUnit = entityManager.find(OrganizationUnit.class);
    for (OrganizationUnit itemOrganizationUnit : allOrganizationUnit) {
      entityManager.remove(itemOrganizationUnit);
    }
    entityManager.close();
  }

  @Test
  public void TestCreateBase() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    DigitalObject baseMetaData = createBaseMetaDataReference(studyEntity, userEntity);
    DigitalObject testBaseMetaData = DigitalObject.factoryNewDigitalObject("test text");
    entityManager.save(baseMetaData);
    Set<UserData> users = new HashSet<UserData>();
    users.add(userEntity);
    testBaseMetaData.setExperimenters(users);
    testBaseMetaData.setNote(NOTE);
    testBaseMetaData.setEndDate(END_DATE);
//    testBaseMetaData.setStudy(studyEntity);
    testBaseMetaData.setUploadDate(START_DATE);
    testBaseMetaData.setVisible(Boolean.TRUE);
    testBaseMetaData.setStartDate(START_DATE);
    assertTrue(entityManager.contains(baseMetaData));
    assertTrue(entityManager.contains(studyEntity));
    assertTrue(entityManager.contains(userEntity));
    DigitalObject find = entityManager.find(DigitalObject.class, baseMetaData.getBaseId());
    //assertEquals(find.getDevice(), testBaseMetaData.getDevice());
    assertEquals(find.getDigitalObjectIdentifier(), testBaseMetaData.getDigitalObjectIdentifier());
    assertEquals(find.getEndDate(), testBaseMetaData.getEndDate());
    assertEquals(find.getExperimenters(), testBaseMetaData.getExperimenters());
    assertEquals(find.getNote(), testBaseMetaData.getNote());
    assertEquals(find.getStartDate(), testBaseMetaData.getStartDate());
//    assertEquals(find.getStudy(), testBaseMetaData.getStudy());
    assertEquals(find.getUploadDate(), testBaseMetaData.getUploadDate());
    assertEquals(find.isVisible(), testBaseMetaData.isVisible());
  }

  @Test
  public void TestCreateStudyAndBase() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    Study newStudyForTheSecondTest = createStudyReference();
    entityManager.save(newStudyForTheSecondTest);
    assertTrue(entityManager.contains(newStudyForTheSecondTest));
    assertTrue(entityManager.contains(studyEntity));
    assertTrue(entityManager.contains(userEntity));
    DigitalObject newBaseMetaData = createBaseMetaDataReference(newStudyForTheSecondTest, userEntity);
    entityManager.save(newBaseMetaData);
    assertTrue(entityManager.contains(newBaseMetaData));
    assertTrue(entityManager.contains(newStudyForTheSecondTest));
    assertTrue(entityManager.contains(userEntity));
    Study testStudy = Study.factoryNewStudy();
    entityManager.save(newStudyForTheSecondTest);
    testStudy.setManager(userEntity);
    testStudy.setNote(NOTE);
    testStudy.setEndDate(END_DATE);
    testStudy.setLegalNote(LEGAL_NOTE);
    testStudy.setStartDate(START_DATE);
    testStudy.setTopic(TOPIC);
    Study find = entityManager.find(newStudyForTheSecondTest.getClass(), newStudyForTheSecondTest.getStudyId());
    assertEquals(find.getEndDate(), testStudy.getEndDate());
    assertEquals(find.getManager(), testStudy.getManager());
    assertEquals(find.getLegalNote(), testStudy.getLegalNote());
    assertEquals(find.getTopic(), testStudy.getTopic());
    assertEquals(find.getNote(), testStudy.getNote());
    assertEquals(find.getStartDate(), testStudy.getStartDate());
  }

  @Test
  public void testBaseMetaDataAddExperimenter() {
    DigitalObject bmd = new DigitalObject();
    UserData user = createUserReference();
    bmd.addExperimenter(user);
    Set<UserData> userSet = bmd.getExperimenters();
    assertEquals(1, userSet.size());
    assertTrue(userSet.contains(user));
  }

  @Test
  public void testBaseMetaDataAdd2Experimenter() {
    DigitalObject bmd = new DigitalObject();
    UserData user1 = createUserReference(1);
    UserData user2 = createUserReference(2);
    UserData user3 = createUserReference(3);
    bmd.addExperimenter(user1);
    bmd.addExperimenter(user2);
    Set<UserData> userSet = bmd.getExperimenters();
    assertEquals(2, userSet.size());
    assertTrue(userSet.contains(user1));
    assertTrue(userSet.contains(user2));
    assertFalse(userSet.contains(user3));
  }

  @Test
  public void testBaseMetaDataRemove2Experimenter() {
    DigitalObject bmd = new DigitalObject();
    UserData user1 = createUserReference(1);
    UserData user2 = createUserReference(2);
    UserData user3 = createUserReference(3);
    bmd.addExperimenter(user1);
    bmd.addExperimenter(user2);
    bmd.removeExperimenter(user1);
    Set<UserData> userSet = bmd.getExperimenters();
    assertEquals(1, userSet.size());
    bmd.removeExperimenter(user2);
    userSet = bmd.getExperimenters();
    assertEquals(0, userSet.size());
    assertFalse(userSet.contains(user1));
    assertFalse(userSet.contains(user2));
    assertFalse(userSet.contains(user3));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBaseMetaDataAddNoExperimenter() {
    DigitalObject bmd = new DigitalObject();
    bmd.addExperimenter(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBaseMetaDataRemoveNoExperimenter1() {
    DigitalObject bmd = new DigitalObject();
    bmd.removeExperimenter(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBaseMetaDataRemoveNoExperimenter2() {
    DigitalObject bmd = new DigitalObject();
    UserData user1 = createUserReference();
    bmd.addExperimenter(user1);
    bmd.removeExperimenter(null);
    assertTrue(false);
  }

  @Test
  public void testBaseMetaDataRemoveNotExistingExperimenter() {
    DigitalObject bmd = new DigitalObject();
    UserData user1 = createUserReference(1);
    UserData user2 = createUserReference(2);
    bmd.addExperimenter(user1);
    bmd.removeExperimenter(user2);
    assertEquals(1, bmd.getExperimenters().size());
  }

  static private UserData createUserReference() {
    UserData User = new UserData();
    User.setFirstName(FIRST_NAME);
    User.setLastName(LAST_NAME);
    User.setEmail(FIRST_NAME + "." + LAST_NAME + "@kit.edu");
    User.setDistinguishedName("O=GermanGrid,OU=KIT,CN=" + FIRST_NAME + " " + LAST_NAME);
    User.setValidFrom(START_DATE);
    User.setValidUntil(END_DATE);
    return User;
  }

  static private UserData createUserReference(int instanceCounter) {
    UserData User = createUserReference();
    User.setFirstName(FIRST_NAME + "_" + instanceCounter++);
    return User;
  }

  static private Study createStudyReference() {
    Study study = Study.factoryNewStudy();
    study.setManager(userEntity);
    study.setLegalNote(LEGAL_NOTE);
    study.setNote(NOTE);
    study.setTopic(TOPIC);
    study.setStartDate(START_DATE);
    study.setEndDate(END_DATE);
    return study;
  }

  static private DigitalObject createBaseMetaDataReference(Study study, UserData user) {
    DigitalObject base = DigitalObject.factoryNewDigitalObject("test text");
    base.setEndDate(END_DATE);
    Set<UserData> users = new HashSet<UserData>();
    users.add(user);
    base.setExperimenters(users);
    if (study != null) {
      if (study.getInvestigations() != null) {
        if (!study.getInvestigations().isEmpty()) {
          base.setInvestigation(study.getInvestigations().iterator().next());
        }
      }
    }
    base.setNote(NOTE);
    base.setUploadDate(START_DATE);
    base.setVisible(Boolean.TRUE);
    base.setStartDate(START_DATE);
    return base;
  }
}
