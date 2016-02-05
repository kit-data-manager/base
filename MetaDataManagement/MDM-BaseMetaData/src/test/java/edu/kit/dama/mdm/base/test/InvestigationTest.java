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
package edu.kit.dama.mdm.base.test;

import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.base.Participant;
import org.junit.AfterClass;
import java.util.HashSet;
import java.util.Set;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import edu.kit.dama.mdm.core.MetaDataManagement;
import org.junit.BeforeClass;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import java.util.Date;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.MetaDataSchema;
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
public class InvestigationTest {

  Investigation investigation = null;
  private static final Logger LOGGER = LoggerFactory.getLogger(InvestigationTest.class);
  private static IMetaDataManager entityManager = null;
  private static Investigation referenceEntity = null;
  private static final String NOTE = "Erika";
  private static final String TOPIC = "topic";
  private static final Participant PARTICIPANT = new Participant(new UserData());
  private static Date START_DATE = new Date();
  private static Date END_DATE = new Date(2099, 12, 31, 23, 59, 59);

  @BeforeClass
  public static void prepareClassForTests() {
    MetaDataManagementHelper.replaceConfig(null);
       entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-BaseMetaData-Test");
  }

  @AfterClass
  public static void releaseClassForTests() {
    entityManager.close();
  }
  
  @After
  public void releaseTest() throws UnauthorizedAccessAttemptException, EntityNotFoundException{
    List<Investigation>all = entityManager.find(Investigation.class);
    for (Investigation item : all) {
      entityManager.remove(item);
    }
  }

  @Before
  public void prepareTest() {
    investigation = new Investigation();
  }
  
  @Test
  public void investigationAddParticipantTest() {

    investigation.addParticipant(PARTICIPANT);
    assertTrue(investigation.getParticipants().contains(PARTICIPANT));
  }

  @Test
  public void investigationSetNoteTest() {

    investigation.setNote(NOTE);
    assertEquals(investigation.getNote(), NOTE);
  }

  @Test
  public void testInvestigationAddParticipant() {
    Investigation bmd = new Investigation();
    Participant user = createParticipantReference("firstName","lastName");
    bmd.addParticipant(user);
    Set<Participant> userSet = bmd.getParticipants();
    assertEquals(1, userSet.size());
    assertTrue(userSet.contains(user));
  }

  @Test
  public void testInvestigationAdd2Participant() {
    Investigation bmd = new Investigation();
    Participant user1 = createParticipantReference("firstName","lastName");
    Participant user2 = createParticipantReference("firstName2","lastName2");
    Participant user3 = createParticipantReference("firstName3","lastName3");
    bmd.addParticipant(user1);
    bmd.addParticipant(user2);
    Set<Participant> userSet = bmd.getParticipants();
    assertEquals(2, userSet.size());
    assertTrue(userSet.contains(user1));
    assertTrue(userSet.contains(user2));
    assertFalse(userSet.contains(user3));
  }

  @Test
  public void testInvestigationRemove2Participant() {
    Investigation bmd = new Investigation();
    Participant user1 = createParticipantReference("firstName","lastName");
    Participant user2 = createParticipantReference("firstName2","lastName2");
    Participant user3 = createParticipantReference("firstName3","lastName3");
    bmd.addParticipant(user1);
    bmd.addParticipant(user2);
    bmd.removeParticipant(user1);
    Set<Participant> userSet = bmd.getParticipants();
    assertEquals(1, userSet.size());
    bmd.removeParticipant(user2);
    userSet = bmd.getParticipants();
    assertEquals(0, userSet.size());
    assertFalse(userSet.contains(user1));
    assertFalse(userSet.contains(user2));
    assertFalse(userSet.contains(user3));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvestigationAddNoParticipant() {
    Investigation bmd = new Investigation();
    bmd.addParticipant(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvestigationRemoveNoParticipant1() {
    Investigation bmd = new Investigation();
    bmd.removeParticipant(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvestigationRemoveNoParticipant2() {
    Investigation bmd = new Investigation();
    Participant user1 = createParticipantReference("firstName","lastName");
    bmd.addParticipant(user1);
    bmd.removeParticipant(null);
    assertTrue(false);
  }

  @Test
  public void testInvestigationRemoveNotExistingParticipant() {
    Investigation bmd = new Investigation();
    Participant user1 = createParticipantReference("firstName","lastName");
    Participant user2 = createParticipantReference("firstName2","lastName2");
    bmd.addParticipant(user1);
    bmd.removeParticipant(user2);
    assertEquals(1,bmd.getParticipants().size());
  }
  @Test
  public void testInvestigationAddDataSet() {
    Investigation invest = new Investigation();
    DigitalObject bmd = createBaseMetaData();
    invest.addDataSet(bmd);
    Set<DigitalObject> bmdSet = invest.getDataSets();
    assertEquals(1, bmdSet.size());
    assertTrue(bmdSet.contains(bmd));
  }

  @Test
  public void testInvestigationAdd2DataSet() {
    Investigation invest = new Investigation();
    DigitalObject bmd1 = createBaseMetaData();
    DigitalObject bmd2 = createBaseMetaData();
    DigitalObject bmd3 = createBaseMetaData();
    invest.addDataSet(bmd1);
    invest.addDataSet(bmd2);
    Set<DigitalObject> bmdSet = invest.getDataSets();
    assertEquals(2, bmdSet.size());
    assertTrue(bmdSet.contains(bmd1));
    assertTrue(bmdSet.contains(bmd2));
    assertFalse(bmdSet.contains(bmd3));
  }

  @Test
  public void testInvestigationRemove2DataSet() {
    Investigation invest = new Investigation();
    DigitalObject bmd1 = createBaseMetaData();
    DigitalObject bmd2 = createBaseMetaData();
    DigitalObject bmd3 = createBaseMetaData();
    invest.addDataSet(bmd1);
    invest.addDataSet(bmd2);
    invest.removeDataSet(bmd1);
    Set<DigitalObject> bmdSet = invest.getDataSets();
    assertEquals(1, bmdSet.size());
    invest.removeDataSet(bmd2);
    bmdSet = invest.getDataSets();
    assertEquals(0, bmdSet.size());
    assertFalse(bmdSet.contains(bmd1));
    assertFalse(bmdSet.contains(bmd2));
    assertFalse(bmdSet.contains(bmd3));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvestigationAddNoDataSet() {
    Investigation invest = new Investigation();
    invest.addDataSet(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvestigationRemoveNoDataSet1() {
    Investigation invest = new Investigation();
    invest.removeDataSet(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvestigationRemoveNoDataSet2() {
    Investigation invest = new Investigation();
    DigitalObject bmd1 = createBaseMetaData();
    invest.addDataSet(bmd1);
    invest.removeDataSet(null);
    assertTrue(false);
  }

  @Test
  public void testInvestigationRemoveNotExistingDataSet() {
    Investigation invest = new Investigation();
    DigitalObject bmd1 = createBaseMetaData();
    DigitalObject bmd2 = createBaseMetaData();
    invest.addDataSet(bmd1);
    invest.removeDataSet(bmd2);
    assertEquals(1,invest.getDataSets().size());
  }


  @Test
  public void investigationSetTopicTest() {

    investigation.setTopic(TOPIC);
    assertEquals(investigation.getTopic(), TOPIC);
  }

  @Test
  public void investigationSetStartDateTest() {

    investigation.setStartDate(START_DATE);
    assertEquals(investigation.getStartDate(), START_DATE);
  }

  @Test
  public void investigationSetEndDateTest() {

    investigation.setEndDate(END_DATE);
    assertEquals(investigation.getEndDate(), END_DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void investigationSetDateFailureTest() {

    investigation.setEndDate(START_DATE);
    investigation.setStartDate(END_DATE);
    if (END_DATE.before(START_DATE)) {
      LOGGER.error("The end date can't be after the start date!");
      assertTrue(false);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void investigationTestSetDateSame() {
    investigation.setEndDate(START_DATE);
    investigation.setStartDate(START_DATE);
    if (START_DATE.before(END_DATE)) {
      assertTrue(true);
    } else {
      assertTrue(false);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void investigationSetHeadTestAsNull() {
    investigation.addParticipant(null);
    assertTrue(false);
  }

  @Test
  public void investigationSetNoteTestAsNull() {

    investigation.setNote(null);
    assertEquals(investigation.getNote(), null);
  }
  @Test
  public void investigationSetTopicTestAsNull() {

    investigation.setTopic(null);
    assertEquals(investigation.getTopic(), null);
  }

  @Test
  public void investigationSetStartDateTestAsNull() {

    investigation.setStartDate(null);
    assertEquals(investigation.getStartDate(), null);
  }

  @Test
  public void investigationSetEndDateTestAsNull() {

    investigation.setEndDate(null);
    assertEquals(investigation.getEndDate(), null);
  }
   
  @Test 
  public void investigationSaveInDataBase() throws UnauthorizedAccessAttemptException {
    referenceEntity = investigation;
    entityManager.save(referenceEntity);
    assertTrue(entityManager.find(Investigation.class).contains(investigation));
  }
  
  @Test 
  public void investigationSaveInDataBaseWithSetHeadTestAsNull() throws UnauthorizedAccessAttemptException {
    HashSet<Participant> hashSet = new HashSet<Participant>();

    investigation.setParticipants(hashSet);
    assertEquals(investigation.getParticipants(), hashSet);
    referenceEntity = investigation;
    entityManager.save(referenceEntity);
    assertTrue(true);
  }

  @Test 
  public void investigationSaveInDataBaseWithSetNoteTestAsNull() throws UnauthorizedAccessAttemptException {

    investigation.setNote(null);
    assertEquals(investigation.getNote(), null);
    referenceEntity = investigation;
    entityManager.save(referenceEntity);
    assertEquals(investigation, entityManager.find(Investigation.class).get(0));
  }

  @Test 
  public void investigationSaveInDataBaseWithSetTopicTestAsNull() throws UnauthorizedAccessAttemptException {

    investigation.setTopic(null);
    assertEquals(investigation.getTopic(), null);
    referenceEntity = investigation;
    entityManager.save(referenceEntity);
    assertEquals(investigation, entityManager.find(Investigation.class).get(0));
  }

  @Test
  public void investigationSaveInDataBaseWithSetStartDateTestAsNull() throws UnauthorizedAccessAttemptException {

    investigation.setStartDate(null);
    assertEquals(investigation.getStartDate(), null);
    referenceEntity = investigation;
    entityManager.save(referenceEntity);
    assertEquals(investigation, entityManager.find(Investigation.class).get(0));
  }

  @Test 
  public void investigationSaveInDataBaseWithSetEndDateTestAsNull() throws UnauthorizedAccessAttemptException {

    investigation.setEndDate(null);
    assertEquals(investigation.getEndDate(), null);
    referenceEntity = investigation;
    entityManager.save(referenceEntity);
    assertEquals(investigation, entityManager.find(Investigation.class).get(0));
  }
  @Test
  public void testOneMetaDataSchema() {
    String schema = "http://any.domain.org/anyCommunity/anyVersion";
    MetaDataSchema mds = createMetaDataSchema(schema);
    investigation.addMetaDataSchema(mds);
    assertTrue(investigation.getMetaDataSchema().contains(mds));
    investigation.removeMetaDataSchema(mds);
    assertFalse(investigation.getMetaDataSchema().contains(mds));
  }
  @Test
  public void testTwoMetaDataSchema() {
    String schema = "http://any.domain.org/anyCommunity/anyVersion";
    MetaDataSchema mds = createMetaDataSchema(schema);
     String schema2 = "http://any.other.domain.org/anyCommunity/anyVersion";
    MetaDataSchema mds2 = createMetaDataSchema(schema2);
    investigation.addMetaDataSchema(mds);
    assertTrue(investigation.getMetaDataSchema().contains(mds));
    assertFalse(investigation.getMetaDataSchema().contains(mds2));
   investigation.addMetaDataSchema(mds2);
    assertTrue(investigation.getMetaDataSchema().contains(mds));
    assertTrue(investigation.getMetaDataSchema().contains(mds2));
    investigation.removeMetaDataSchema(mds);
    assertFalse(investigation.getMetaDataSchema().contains(mds));
    assertTrue(investigation.getMetaDataSchema().contains(mds2));
    investigation.removeMetaDataSchema(mds2);
    assertFalse(investigation.getMetaDataSchema().contains(mds));
    assertFalse(investigation.getMetaDataSchema().contains(mds2));
  }
  @Test
  public void testMetaDataSchemaSet() {
    String schema = "http://any.domain.org/anyCommunity/anyVersion";
    MetaDataSchema mds = createMetaDataSchema(schema);
     String schema2 = "http://any.other.domain.org/anyCommunity/anyVersion";
    MetaDataSchema mds2 = createMetaDataSchema(schema2);
    Set<MetaDataSchema> setMds = new HashSet<MetaDataSchema>();
    setMds.add(mds);
    setMds.add(mds2);
    investigation.setMetaDataSchema(setMds);
    Set<MetaDataSchema> getMds = investigation.getMetaDataSchema();
    assertTrue(getMds.contains(mds));
    assertTrue(getMds.contains(mds2));
    investigation.removeMetaDataSchema(mds);
    investigation.removeMetaDataSchema(mds2);
    assertFalse(investigation.getMetaDataSchema().contains(mds));
    assertFalse(investigation.getMetaDataSchema().contains(mds2));
   
   }
  @Test
  public void testVisibility() {
    investigation.setVisible(Boolean.TRUE);
    assertTrue(investigation.isVisible());
     investigation.setVisible(Boolean.FALSE);
    assertFalse(investigation.isVisible());
   
  }
  static private MetaDataSchema createMetaDataSchema(String schemaURLAsString) {
    MetaDataSchema mds = new MetaDataSchema("dama", schemaURLAsString);
    return mds;
  }
  
  static private DigitalObject createBaseMetaData() {
    DigitalObject bmd = new DigitalObject();
    bmd.setNote("any note");
    bmd.setVisible(Boolean.FALSE);
    return bmd;
  }

  static private Participant createParticipantReference(String first, String last) {
    UserData user = new UserData();
    user.setFirstName(first);
    user.setLastName(last);
    user.setEmail(first + "." + last + "@kit.edu");
    user.setDistinguishedName("O=GermanGrid,OU=KIT,CN=" + first + " " + last);
    user.setValidFrom(START_DATE);
    user.setValidUntil(END_DATE);
    return new Participant(user);
  }
  
}
