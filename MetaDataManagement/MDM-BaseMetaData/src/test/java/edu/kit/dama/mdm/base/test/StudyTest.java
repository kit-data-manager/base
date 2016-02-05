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
import edu.kit.dama.mdm.base.Relation;
import org.junit.AfterClass;
import java.util.Set;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import edu.kit.dama.mdm.core.MetaDataManagement;
import org.junit.BeforeClass;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import java.util.Date;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.UserData;
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
public class StudyTest {

  Study study = null;
  private static final Logger LOGGER = LoggerFactory.getLogger(StudyTest.class);
  private static IMetaDataManager entityManager = null;
  private static Study referenceEntity = null;
  private static final String NOTE = "Erika";
  private static final String LEGAL_NOTE = "Hans";
  private static final String TOPIC = "topic";
  private static final UserData HEAD = new UserData();
  private static Date START_DATE = new Date();
  private static Date END_DATE = new Date(2099, 12, 31, 23, 59, 59);
  private static final String WEBSITE = "http://www.kit.edu";
  private static final String ZIPCODE = "76344";
  private static final String COUNTRY = "DE";
  private static final String ADDRESS = "Hermann-von-Helmholtz-Platz 1";

  @BeforeClass
  public static void prepareClassForTests() {
    MetaDataManagementHelper.replaceConfig(null);
    entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-BaseMetaData-Test");
  }

  @AfterClass
  public static void releaseClassForTests() {
    entityManager.close();
  }

  @Before
  public void prepareTest() {
    study = Study.factoryNewStudy();
  }

  @After
  public void releaseTest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    List<Study> all = entityManager.find(Study.class);
    for (Study item : all) {
      entityManager.remove(item);
    }
  }

  @Test
  public void studySetManagerTest() {

    study.setManager(HEAD);
    assertEquals(study.getManager(), HEAD);
  }

  @Test
  public void studySetNoteTest() {

    study.setNote(NOTE);
    assertEquals(study.getNote(), NOTE);
  }

  @Test
  public void studySetLegalNoteTest() {

    study.setLegalNote(LEGAL_NOTE);
    assertEquals(study.getLegalNote(), LEGAL_NOTE);
  }

  @Test
  public void studySetTopicTest() {

    study.setTopic(TOPIC);
    assertEquals(study.getTopic(), TOPIC);
  }

  @Test
  public void studySetStartDateTest() {

    study.setStartDate(START_DATE);
    assertEquals(study.getStartDate(), START_DATE);
  }

  @Test
  public void studySetEndDateTest() {

    study.setEndDate(END_DATE);
    assertEquals(study.getEndDate(), END_DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void studySetDateFailureTest() {

    study.setEndDate(START_DATE);
    study.setStartDate(END_DATE);
    if (END_DATE.before(START_DATE)) {
      LOGGER.error("The end date can't be after the start date!");
      assertTrue(false);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void studyTestSetDateSame() {
    study.setEndDate(START_DATE);
    study.setStartDate(START_DATE);
    if (START_DATE.before(END_DATE)) {
      assertTrue(true);
    } else {
      assertTrue(false);
    }
  }

  @Test
  public void studySetManagerTestAsNull() {

    study.setManager(null);
    assertEquals(study.getManager(), null);
  }

  @Test
  public void studySetNoteTestAsNull() {

    study.setNote(null);
    assertEquals(study.getNote(), null);
  }

  @Test
  public void studySetLegalNoteTestAsNull() {

    study.setLegalNote(null);
    assertEquals(study.getLegalNote(), null);
  }

  @Test
  public void studySetTopicTestAsNull() {

    study.setTopic(null);
    assertEquals(study.getTopic(), null);
  }

  @Test
  public void studySetStartDateTestAsNull() {

    study.setStartDate(null);
    assertEquals(study.getStartDate(), null);
  }

  @Test
  public void studySetEndDateTestAsNull() {

    study.setEndDate(null);
    assertEquals(study.getEndDate(), null);
  }

  @Test
  public void studySaveInDataBase() throws UnauthorizedAccessAttemptException {
    referenceEntity = study;
    entityManager.save(referenceEntity);
    assertEquals(study, entityManager.find(Study.class).get(0));
  }

  @Test
  public void studySaveInDataBaseWithSetManagerTestAsNull() throws UnauthorizedAccessAttemptException {

    study.setManager(null);
    assertEquals(study.getManager(), null);
    referenceEntity = study;
    entityManager.save(referenceEntity);
    assertEquals(study, entityManager.find(Study.class).get(0));
  }

  @Test
  public void studySaveInDataBaseWithSetNoteTestAsNull() throws UnauthorizedAccessAttemptException {

    study.setNote(null);
    assertEquals(study.getNote(), null);
    referenceEntity = study;
    entityManager.save(referenceEntity);
    assertEquals(study, entityManager.find(Study.class).get(0));
  }

  @Test
  public void studySaveInDataBaseWithSetLegalNoteTestAsNull() throws UnauthorizedAccessAttemptException {

    study.setLegalNote(null);
    assertEquals(study.getLegalNote(), null);
    referenceEntity = study;
    entityManager.save(referenceEntity);
    assertEquals(study, entityManager.find(Study.class).get(0));
  }

  @Test
  public void studySaveInDataBaseWithSetTopicTestAsNull() throws UnauthorizedAccessAttemptException {

    study.setTopic(null);
    assertEquals(study.getTopic(), null);
    referenceEntity = study;
    entityManager.save(referenceEntity);
    assertEquals(study, entityManager.find(Study.class).get(0));
  }

  @Test
  public void studySaveInDataBaseWithSetStartDateTestAsNull() throws UnauthorizedAccessAttemptException {

    study.setStartDate(null);
    assertEquals(study.getStartDate(), null);
    referenceEntity = study;
    entityManager.save(referenceEntity);
    assertEquals(study, entityManager.find(Study.class).get(0));
  }

  @Test
  public void studySaveInDataBaseWithSetEndDateTestAsNull() throws UnauthorizedAccessAttemptException {

    study.setEndDate(null);
    assertEquals(study.getEndDate(), null);
    referenceEntity = study;
    entityManager.save(referenceEntity);
    assertEquals(study, entityManager.find(Study.class).get(0));
  }

  @Test
  public void testStudyAddOrganizationUnit() {
    Study study = Study.factoryNewStudy();
    OrganizationUnit oUnit = createOrganizationUnitReference("ouName", "city");
    study.addOrganizationUnit(oUnit);
    Set<Relation> oUnitSet = study.getOrganizationUnits();
    assertEquals(1, oUnitSet.size());
//    assertTrue(oUnitSet.contains(oUnit));
  }

  @Test
  public void testStudyAdd2OrganizationUnit() {
    Study study = Study.factoryNewStudy();
    OrganizationUnit oUnit1 = createOrganizationUnitReference("ouName", "city");
    OrganizationUnit oUnit2 = createOrganizationUnitReference("ouName2", "city2");
    OrganizationUnit oUnit3 = createOrganizationUnitReference("ouName3", "city3");
    study.addOrganizationUnit(oUnit1);
    study.addOrganizationUnit(oUnit2);
    Set<Relation> oUnitSet = study.getOrganizationUnits();
    assertEquals(2, oUnitSet.size());
//    assertTrue(oUnitSet.contains(oUnit1));
//    assertTrue(oUnitSet.contains(oUnit2));
//    assertFalse(oUnitSet.contains(oUnit3));
  }

  @Test
  public void testStudyRemove2OrganizationUnit() {
    Study study = Study.factoryNewStudy();
    OrganizationUnit oUnit1 = createOrganizationUnitReference("ouName", "city");
    OrganizationUnit oUnit2 = createOrganizationUnitReference("ouName2", "city2");
    OrganizationUnit oUnit3 = createOrganizationUnitReference("ouName3", "city3");
    study.addOrganizationUnit(oUnit1);
    study.addOrganizationUnit(oUnit2);
    study.removeOrganizationUnit(oUnit1);
    Set<Relation> oUnitSet = study.getOrganizationUnits();
    assertEquals(1, oUnitSet.size());
    study.removeOrganizationUnit(oUnit2);
    oUnitSet = study.getOrganizationUnits();
    assertEquals(0, oUnitSet.size());
//    assertFalse(oUnitSet.contains(oUnit1));
//    assertFalse(oUnitSet.contains(oUnit2));
//    assertFalse(oUnitSet.contains(oUnit3));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStudyAddNoOrganizationUnit() {
    Study study = Study.factoryNewStudy();
    study.addOrganizationUnit(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStudyRemoveNoOrganizationUnit1() {
    Study study = Study.factoryNewStudy();
    study.removeOrganizationUnit(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStudyRemoveNoOrganizationUnit2() {
    Study study = Study.factoryNewStudy();
    OrganizationUnit oUnit1 = createOrganizationUnitReference("ouName", "city");
    study.addOrganizationUnit(oUnit1);
    study.removeOrganizationUnit(null);
    assertTrue(false);
  }

  @Test
  public void testStudyRemoveNotExistingOrganizationUnit() {
    Study study = Study.factoryNewStudy();
    OrganizationUnit oUnit1 = createOrganizationUnitReference("ouName", "city");
    OrganizationUnit oUnit2 = createOrganizationUnitReference("ouName2", "city2");
    study.addOrganizationUnit(oUnit1);
    study.removeOrganizationUnit(oUnit2);
    assertEquals(1, study.getOrganizationUnits().size());
  }

  @Test
  public void testOu() {
    String city = "Karlsruhe";
    String ouName = "KIT";
    Long id = 1234L;
    OrganizationUnit ou = createOrganizationUnitReference(ouName, city);
    ou.setOrganizationUnitId(id);
    assertEquals(city, ou.getCity());
    assertEquals(ouName, ou.getOuName());
    assertEquals(id, ou.getOrganizationUnitId());
    assertEquals(ADDRESS, ou.getAddress());
    assertEquals(WEBSITE, ou.getWebsite());
    assertEquals(ZIPCODE, ou.getZipCode());
    assertEquals(COUNTRY, ou.getCountry());
//    assertEquals(HEAD, ou.getManager());

  }

  @Test
  public void testRelation() {
    OrganizationUnit ou = createOrganizationUnitReference("KIT", "Karlsruhe");
    Long id = 4567L;
    String manager = "Manager";
    Relation relation = new Relation(ou);
    relation.setRelationId(id);
   // assertEquals(----, relation.getTask());
    assertEquals(ou, relation.getOrganizationUnit());
    assertEquals(id, relation.getRelationId());
    Task task = new Task(manager);
    relation = new Relation(ou, task);
    relation.setRelationId(id);
    assertEquals(task, relation.getTask());
    assertEquals(ou, relation.getOrganizationUnit());
    assertEquals(id, relation.getRelationId());
    ou = createOrganizationUnitReference("one", "two");
    task = new Task("no");
    relation.setOrganizationUnit(ou);
    relation.setTask(task);
    assertEquals(ou, relation.getOrganizationUnit());
    assertEquals(id, relation.getRelationId());
  }

  @Test
  public void testVisibility() {
    study.setVisible(Boolean.TRUE);
    assertTrue(study.isVisible());
    study.setVisible(Boolean.FALSE);
    assertFalse(study.isVisible());

  }

  private static OrganizationUnit createOrganizationUnitReference(String ouName, String city) {
    OrganizationUnit ou = new OrganizationUnit();
    ou.setOuName(ouName);
    ou.setManager(new UserData());
    ou.setWebsite(WEBSITE);
    ou.setZipCode(ZIPCODE);
    ou.setCity(city);
    ou.setCountry(COUNTRY);
    ou.setAddress(ADDRESS);
    return ou;
  }
}
