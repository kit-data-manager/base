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
import edu.kit.dama.commons.types.DigitalObjectId;
import org.junit.AfterClass;
import java.util.HashSet;
import java.util.Set;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import edu.kit.dama.mdm.core.MetaDataManagement;
import org.junit.BeforeClass;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import java.util.Date;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
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
public class DigitalObjectTest {

  DigitalObject digitalObject = null;
  private static final Logger LOGGER = LoggerFactory.getLogger(DigitalObjectTest.class);
  private static IMetaDataManager entityManager = null;
  private static DigitalObject referenceEntity = null;
  private static String DIGITAL_OBJECT_IDENTIFIER = "uniqueString";
  private static final String NOTE = "Erika";
  private static final String TOPIC = "topic";
  private static UserData UPLOADER = new UserData();
  private static UserData EXPERIMENTER_1 = new UserData();
  private static UserData EXPERIMENTER_2 = new UserData();
  private static Date START_DATE = new Date();
  private static Date END_DATE = new Date(2099, 11, 31, 23, 59, 59);
  private static Date UPLOAD_DATE = new Date(2012, 11, 10, 23, 59, 59);

  @BeforeClass
  public static void prepareClassForTests() throws UnauthorizedAccessAttemptException {
    MetaDataManagementHelper.replaceConfig(null);
    entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-BaseMetaData-Test");
    UPLOADER = createUserDataReference("up", "loader");

    EXPERIMENTER_1 = createUserDataReference("exp", "erimenter 1");
    EXPERIMENTER_2 = createUserDataReference("exp", "erimenter 2");

    Investigation inv = new Investigation();
    inv.setDescription("description");
    inv.setNote("note");
    inv.setTopic("topic");


    entityManager.persist(UPLOADER);
    entityManager.persist(EXPERIMENTER_1);
    entityManager.persist(EXPERIMENTER_2);
    entityManager.persist(inv);
  }

  @AfterClass
  public static void releaseClassForTests() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    List<Investigation> findInv = entityManager.find(Investigation.class);
    assertEquals(1, findInv.size());
    for (Investigation item : findInv) {
      entityManager.remove(item);
    }
    List<DigitalObject> findDigObj = entityManager.find(DigitalObject.class);
    assertEquals(0, findDigObj.size());
    for (DigitalObject item : findDigObj) {
      entityManager.remove(item);
    }
    List<UserData> find = entityManager.find(UserData.class);
    assertEquals(3, find.size());
    for (UserData item : find) {
      entityManager.remove(item);
    }
    entityManager.close();
  }

  @After
  public void releaseTest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    List<DigitalObject> all = entityManager.find(DigitalObject.class);
    for (DigitalObject item : all) {
      entityManager.remove(item);
    }
  }

  @Before
  public void prepareTest() {
    digitalObject = new DigitalObject();
  }

  @Test
  public void digitalObjectAddExperimenterTest() {

    digitalObject.addExperimenter(EXPERIMENTER_1);
    assertTrue(digitalObject.getExperimenters().contains(EXPERIMENTER_1));
  }

  @Test
  public void digitalObjectSetNoteTest() {

    digitalObject.setNote(NOTE);
    assertEquals(digitalObject.getNote(), NOTE);
  }

  @Test
  public void testDigitalObjectAddUserData() {
    DigitalObject bmd = new DigitalObject();
    UserData user = createUserDataReference("firstName", "lastName");
    bmd.addExperimenter(user);
    Set<UserData> userSet = bmd.getExperimenters();
    assertEquals(1, userSet.size());
    assertTrue(userSet.contains(user));
  }

  @Test
  public void testDigitalObjectAdd2UserData() {
    DigitalObject bmd = new DigitalObject();
    UserData user1 = createUserDataReference("firstName", "lastName");
    UserData user2 = createUserDataReference("firstName2", "lastName2");
    UserData user3 = createUserDataReference("firstName3", "lastName3");
    bmd.addExperimenter(user1);
    bmd.addExperimenter(user2);
    Set<UserData> userSet = bmd.getExperimenters();
    assertEquals(2, userSet.size());
    assertTrue(userSet.contains(user1));
    assertTrue(userSet.contains(user2));
    assertFalse(userSet.contains(user3));
  }

  @Test
  public void testDigitalObjectRemove2UserData() {
    DigitalObject bmd = new DigitalObject();
    UserData user1 = createUserDataReference("firstName", "lastName");
    UserData user2 = createUserDataReference("firstName2", "lastName2");
    UserData user3 = createUserDataReference("firstName3", "lastName3");
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
  public void testDigitalObjectAddNoUserData() {
    DigitalObject bmd = new DigitalObject();
    bmd.addExperimenter(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDigitalObjectRemoveNoExperimenter1() {
    DigitalObject bmd = new DigitalObject();
    bmd.removeExperimenter(null);
    assertTrue(false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDigitalObjectRemoveNoExperimenter2() {
    DigitalObject bmd = new DigitalObject();
    UserData user1 = createUserDataReference("firstName", "lastName");
    bmd.addExperimenter(user1);
    bmd.removeExperimenter(null);
    assertTrue(false);
  }

  @Test
  public void testDigitalObjectRemoveNotExistingUserData() {
    DigitalObject bmd = new DigitalObject();
    UserData user1 = createUserDataReference("firstName", "lastName");
    UserData user2 = createUserDataReference("firstName2", "lastName2");
    bmd.addExperimenter(user1);
    bmd.removeExperimenter(user2);
    assertEquals(1, bmd.getExperimenters().size());
  }

  @Test
  public void digitalObjectSetDigitalObjectIdentifierTest() {
    DigitalObject o = DigitalObject.factoryNewDigitalObject(DIGITAL_OBJECT_IDENTIFIER);
    assertEquals(o.getDigitalObjectIdentifier(), DIGITAL_OBJECT_IDENTIFIER);
  }

  @Test
  public void digitalObjectSetStartDateTest() {

    digitalObject.setStartDate(START_DATE);
    assertEquals(digitalObject.getStartDate(), START_DATE);
  }

  @Test
  public void digitalObjectSetEndDateTest() {

    digitalObject.setEndDate(END_DATE);
    assertEquals(digitalObject.getEndDate(), END_DATE);
  }

  @Test
  public void digitalObjectSetUploadDateTest() {

    digitalObject.setUploadDate(UPLOAD_DATE);
    assertEquals(digitalObject.getUploadDate(), UPLOAD_DATE);
  }

  @Test(expected = IllegalArgumentException.class)
  public void digitalObjectSetDateFailureTest() {

    digitalObject.setEndDate(START_DATE);
    digitalObject.setStartDate(END_DATE);
    if (END_DATE.before(START_DATE)) {
      LOGGER.error("The end date can't be after the start date!");
      assertTrue(false);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void digitalObjectTestSetDateSame() {
    digitalObject.setEndDate(START_DATE);
    digitalObject.setStartDate(START_DATE);
    if (START_DATE.before(END_DATE)) {
      assertTrue(true);
    } else {
      assertTrue(false);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void digitalObjectSetHeadTestAsNull() {
    digitalObject.addExperimenter(null);
    assertTrue(false);
  }

  @Test
  public void digitalObjectSetNoteTestAsNull() {

    digitalObject.setNote(null);
    assertEquals(digitalObject.getNote(), null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void digitalObjectSetDigitalObjectIdentifierTestAsNull() {
    DigitalObject o = DigitalObject.factoryNewDigitalObject(null);
    //should not happen
    assertTrue(false);
  }

  @Test
  public void digitalObjectSetStartDateTestAsNull() {

    digitalObject.setStartDate(null);
    assertEquals(digitalObject.getStartDate(), null);
  }

  @Test
  public void digitalObjectSetEndDateTestAsNull() {

    digitalObject.setEndDate(null);
    assertEquals(digitalObject.getEndDate(), null);
  }

  @Test
  public void digitalObjectSaveInDataBase() throws UnauthorizedAccessAttemptException {
    referenceEntity = digitalObject;
    entityManager.save(referenceEntity);
    assertTrue(entityManager.find(DigitalObject.class).contains(digitalObject));
  }

  @Test
  public void digitalObjectSaveInDataBaseWithSetHeadTestAsNull() throws UnauthorizedAccessAttemptException {
    HashSet<UserData> hashSet = new HashSet<UserData>();

    digitalObject.setExperimenters(hashSet);
    assertEquals(digitalObject.getExperimenters(), hashSet);
    referenceEntity = digitalObject;
    entityManager.save(referenceEntity);
    assertTrue(true);
  }

  @Test
  public void digitalObjectSaveInDataBaseWithSetNoteTestAsNull() throws UnauthorizedAccessAttemptException {

    digitalObject.setNote(null);
    assertEquals(digitalObject.getNote(), null);
    referenceEntity = digitalObject;
    entityManager.save(referenceEntity);
    assertEquals(digitalObject, entityManager.find(DigitalObject.class).get(0));
  }

  @Test
  public void digitalObjectSaveInDataBaseWithSetUploaderTestAsNull() throws UnauthorizedAccessAttemptException {

    digitalObject.setUploader(null);
    assertEquals(digitalObject.getUploader(), null);
    referenceEntity = digitalObject;
    entityManager.save(referenceEntity);
    assertEquals(digitalObject, entityManager.find(DigitalObject.class).get(0));
  }

  @Test
  public void digitalObjectSaveInDataBaseWithSetStartDateTestAsNull() throws UnauthorizedAccessAttemptException {

    digitalObject.setStartDate(null);
    assertEquals(digitalObject.getStartDate(), null);
    referenceEntity = digitalObject;
    entityManager.save(referenceEntity);
    assertEquals(digitalObject, entityManager.find(DigitalObject.class).get(0));
  }

  @Test
  public void digitalObjectSaveInDataBaseWithSetEndDateTestAsNull() throws UnauthorizedAccessAttemptException {

    digitalObject.setEndDate(null);
    assertEquals(digitalObject.getEndDate(), null);
    referenceEntity = digitalObject;
    entityManager.save(referenceEntity);
    assertEquals(digitalObject, entityManager.find(DigitalObject.class).get(0));
  }

  @Test
  public void digitalObjectSaveInDataBaseWithSetDigitalObjectId() throws UnauthorizedAccessAttemptException {
    DigitalObjectId doi = new DigitalObjectId("anyUniqueString");

    digitalObject.setDigitalObjectId(doi);
    assertEquals(digitalObject.getDigitalObjectIdentifier(), doi.getStringRepresentation());
    referenceEntity = digitalObject;
    entityManager.save(referenceEntity);
    assertEquals(digitalObject, entityManager.find(DigitalObject.class).get(0));
  }

  @Test
  public void digitalObjectFindInDataBaseWithSetDigitalObjectId() throws UnauthorizedAccessAttemptException {
    DigitalObjectId doi = new DigitalObjectId("anyUniqueString");
    DigitalObject o = DigitalObject.factoryNewDigitalObject(doi.getStringRepresentation());
    assertEquals(o.getDigitalObjectIdentifier(), doi.getStringRepresentation());
    referenceEntity = digitalObject;
    entityManager.save(referenceEntity);

    digitalObject = DigitalObject.factoryNewDigitalObject(TOPIC);
    entityManager.save(digitalObject);

    List<DigitalObject> result = entityManager.find(digitalObject, digitalObject);
    assertEquals(1, result.size());
    assertEquals(digitalObject, result.get(0));

    result = entityManager.find(DigitalObject.class);
    assertEquals(2, result.size());
    assertTrue(result.contains(digitalObject));
    assertTrue(result.contains(referenceEntity));

    DigitalObject dobj = new DigitalObject();
    dobj.setDigitalObjectId(doi);
    result = entityManager.find(dobj, dobj);
    assertEquals(1, result.size());
    assertEquals(referenceEntity, result.get(0));
  }

  @Test
  public void testDigitalObjectId() throws UnauthorizedAccessAttemptException {
    DigitalObject dobj = new DigitalObject();
    dobj.setDigitalObjectId(new DigitalObjectId(DIGITAL_OBJECT_IDENTIFIER));
    assertEquals(dobj.getDigitalObjectId().getStringRepresentation(), DIGITAL_OBJECT_IDENTIFIER);
    assertEquals(dobj.getDigitalObjectIdentifier(), DIGITAL_OBJECT_IDENTIFIER);
    dobj.setNote(new Date().toString());
    entityManager.persist(dobj);
    List<DigitalObject> result = entityManager.find(dobj, dobj);
    assertEquals(result.size(), 1);
    assertEquals(result.get(0).getDigitalObjectId().getStringRepresentation(), DIGITAL_OBJECT_IDENTIFIER);
    assertEquals(result.get(0).getDigitalObjectIdentifier(), DIGITAL_OBJECT_IDENTIFIER);
  }

  @Test
  public void testVisibility() {
    digitalObject.setVisible(Boolean.TRUE);
    assertTrue(digitalObject.getVisible());
    digitalObject.setVisible(Boolean.FALSE);
    assertFalse(digitalObject.getVisible());
  }

  static private UserData createUserDataReference(String first, String last) {
    UserData user = new UserData();
    user.setFirstName(first);
    user.setLastName(last);
    user.setEmail(first + "." + last + "@kit.edu");
    user.setDistinguishedName("O=GermanGrid,OU=KIT,CN=" + first + " " + last);
    user.setValidFrom(START_DATE);
    user.setValidUntil(END_DATE);
    return user;
  }
}
