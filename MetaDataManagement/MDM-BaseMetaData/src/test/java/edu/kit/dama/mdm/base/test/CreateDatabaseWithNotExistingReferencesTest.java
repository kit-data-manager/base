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
import org.junit.AfterClass;
import java.util.Set;
import java.util.HashSet;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import org.junit.After;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.UserData;
import java.util.Date;
import java.util.List;
import javax.persistence.RollbackException;
import org.junit.BeforeClass;
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
public class CreateDatabaseWithNotExistingReferencesTest {

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

  @BeforeClass
  public static void prepareClass() {
    MetaDataManagementHelper.replaceConfig(null);

    entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-BaseMetaData-Test");
    userEntity = createUserReference();
    studyEntity = createStudyReference(userEntity);
  }

  @AfterClass
  public static void releaseClass() {
    entityManager.close();
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
  }

  @Test(expected = RollbackException.class)
  public void TestCreateBaseWithWrongStudy() throws UnauthorizedAccessAttemptException {
    Study newStudy = Study.factoryNewStudy();
    DigitalObject newBaseMetaData = createBaseMetaDataReference(newStudy, userEntity);
    entityManager.save(newBaseMetaData);
    assertTrue(entityManager.contains(newBaseMetaData));
    assertTrue(entityManager.contains(newStudy));
  }

  @Test(expected = RollbackException.class)
  public void TestCreateBaseWithWrongUser() throws UnauthorizedAccessAttemptException {
    UserData newUser = createUserReference();
    DigitalObject newBaseMetaData = createBaseMetaDataReference(studyEntity, newUser);
    entityManager.save(newBaseMetaData);
    assertTrue(entityManager.contains(newBaseMetaData));
    assertTrue(entityManager.contains(newUser));
  }

  @Test
  public void TestCreateBaseWithWrongDevice() throws UnauthorizedAccessAttemptException {
    boolean exception = false;
    try {
      DigitalObject newBaseMetaData = createBaseMetaDataReference(studyEntity, userEntity);
      entityManager.save(newBaseMetaData);
      assertTrue(entityManager.contains(newBaseMetaData));
    } catch (Exception e) {
      exception = true;
    }
    entityManager.save(userEntity);
    assertTrue(entityManager.contains(userEntity));
    assertTrue(exception);
  }

  @Test(expected = IllegalArgumentException.class)
  public void TestCreateStudyWithWrongUser() throws UnauthorizedAccessAttemptException {
    entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "Digital-Object-Test");
    UserData newUser = createUserReference();
    Study newStudy = createStudyReference(newUser);
    entityManager.save(newStudy);
    assertTrue(entityManager.contains(newStudy));
    assertTrue(entityManager.contains(newUser));
  }

  static private UserData createUserReference() {
    UserData User = new UserData();
    User.setFirstName(FIRST_NAME);
    User.setLastName(LAST_NAME);
    User.setEmail(FIRST_NAME + "." + LAST_NAME + "@kit.edu");
    User.setDistinguishedName("O=GermanGrid,OU=KIT,CN=" + FIRST_NAME + " " + LAST_NAME);
    User.setValidFrom(new Date());
    User.setValidUntil(END_DATE);
    return User;
  }

  static private Study createStudyReference(UserData User) {
    Study study = Study.factoryNewStudy();
    study.setManager(User);
    study.setLegalNote(LEGAL_NOTE);
    study.setNote(NOTE);
    study.setTopic(TOPIC);
    study.setStartDate(new Date());
    study.setEndDate(END_DATE);
    return study;
  }

  static private DigitalObject createBaseMetaDataReference(Study study, UserData user) {
    DigitalObject base =  DigitalObject.factoryNewDigitalObject("test text");
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
    base.setUploadDate(new Date());
    base.setVisible(Boolean.TRUE);
    base.setStartDate(new Date());
    return base;
  }
}
