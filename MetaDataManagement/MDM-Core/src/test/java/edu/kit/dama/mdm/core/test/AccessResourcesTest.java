/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.core.test;


import edu.kit.dama.authorization.annotations.resources.SecurityTestEntity;
import edu.kit.dama.authorization.annotations.resources.TestEntity;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.MetaDataManagementHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Try to persist several entities with different users. And then filter them by
 * different users in different groups.
 *
 * @author hartmann-v
 */
@RunWith(Parameterized.class)
public class AccessResourcesTest extends SecurityUtil {

  private static volatile boolean initialized = false;
  private static volatile List<SecurityTestEntity> allSecurableEntities = new ArrayList<SecurityTestEntity>();
  private static volatile List<TestEntity> allEntities = new ArrayList<TestEntity>();
  private int noOfResults;
  /**
   * For logging purposes.
   */
  private static Logger LOGGER = LoggerFactory.getLogger(AccessResourcesTest.class);

  public AccessResourcesTest(Role resourceUserRole, Role resourceGroupRole, UserId userId, GroupId groupId, Role userRole, int noOfResults) {
    this.resourceUserRole = resourceUserRole;
    this.resourceGroupRole = resourceGroupRole;
    this.userId = userId;
    this.groupId = groupId;
    this.noOfResults = noOfResults;
    this.userRole = userRole;
  }

  //<editor-fold defaultstate="collapsed" desc="parameters for the testclass">
  @Parameterized.Parameters
  public static List<Object[]> testData() {
    return Arrays.asList(
            //Resource User Role, Resource Group Role, Context User, Context Group, Success.,

            //            new Object[]{null, null, userNoAccess, groupNoAccess, Role.NO_ACCESS, 0}, //0  
            //            new Object[]{null, null, userNoAccess, groupGuest, Role.GUEST, 0},
            //            new Object[]{null, null, userNoAccess, groupMember, Role.MEMBER, 0},
            //            new Object[]{null, null, userNoAccess, groupManager, Role.MANAGER, 0},
            //            new Object[]{null, null, userNoAccess, groupAdmin, Role.ADMINISTRATOR, 0},
            //            new Object[]{null, null, userGuest, groupNoAccess, Role.NO_ACCESS, 0}, //5  
            //            new Object[]{null, null, userGuest, groupGuest, Role.GUEST, 0},
            //            new Object[]{null, null, userGuest, groupMember, Role.MEMBER, 3},
            //            new Object[]{null, null, userGuest, groupManager, Role.MANAGER, 3},
            //            new Object[]{null, null, userGuest, groupAdmin, Role.ADMINISTRATOR, 3},
            //            new Object[]{null, null, userMember, groupNoAccess, Role.NO_ACCESS, 0}, //10  
            //            new Object[]{null, null, userMember, groupGuest, Role.GUEST, 0},
            //            new Object[]{null, null, userMember, groupMember, Role.MEMBER, 3},
            //            new Object[]{null, null, userMember, groupManager, Role.MANAGER, 3},
            //            new Object[]{null, null, userMember, groupAdmin, Role.ADMINISTRATOR, 3},
            //            new Object[]{null, null, userManager, groupNoAccess, Role.NO_ACCESS, 0}, //15  
            //            new Object[]{null, null, userManager, groupGuest, Role.GUEST, 0},
            //            new Object[]{null, null, userManager, groupMember, Role.MEMBER, 3},
            //            new Object[]{null, null, userManager, groupManager, Role.MANAGER, 3},
            //            new Object[]{null, null, userManager, groupAdmin, Role.ADMINISTRATOR, 3},
            new Object[]{null, null, userAdmin, groupNoAccess, Role.NO_ACCESS, 0}, //20  
            new Object[]{null, null, userAdmin, groupGuest, Role.GUEST, 0},
            new Object[]{null, null, userAdmin, groupMember, Role.MEMBER, 3},
            new Object[]{null, null, userAdmin, groupManager, Role.MANAGER, 3},
            new Object[]{null, null, userAdmin, groupAdmin, Role.ADMINISTRATOR, 3});
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Initialization">
  @BeforeClass
  public static void prepareClass() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    MetaDataManagementHelper.replaceConfig(null);
    entityManager = MetaDataManagement.getMetaDataManagement().getMetaDataManager("JPA", "MDM-Core-Test");
    prepare();
    initResources();

  }

  @AfterClass
  public static void releaseTestAfterTest() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    release();
  }

  private static void initResources() {
    Role allRoles[] = {Role.NO_ACCESS, Role.GUEST, Role.MEMBER, Role.MANAGER, Role.ADMINISTRATOR};
    GroupId allGroups[] = {groupNoAccess, groupGuest, groupMember, groupManager, groupAdmin};
    initialized = true;
    int counter = 0;
//    try {
    // Add 2 Entries without any registration 
    // One at beginning...
    SecurityTestEntity ste = new SecurityTestEntity();
    TestEntity te = new TestEntity();
    te.setDescription(Integer.toString(counter++));
    ste.setSummary(Integer.toString(counter++));
    allSecurableEntities.add(ste);
    allEntities.add(te);
    for (GroupId group : allGroups) {
      for (Role role : allRoles) {
        ste = new SecurityTestEntity();
        te = new TestEntity();
        ste.setSummary(group.getStringRepresentation() + "_" + role);
        ste.setDescription("secure_" + role.toString());
        te.setDescription(group.getStringRepresentation() + "_" + role);
        //ResourceServiceLocal.registerResource(ste.getSecurableResourceId(), adminContext);
        allSecurableEntities.add(ste);
        persistItem(ste, group, role);
        allEntities.add(te);
        persistItem(te, group, role);
        //ResourceServiceLocal.registerResource(ste.getSecurableResourceId(), group, role, adminContext);
      }

    }
    // ...one at the end.
    ste = new SecurityTestEntity();
    ste.setSummary(Integer.toString(counter++));
    //ResourceServiceLocal.registerResource(ste.getSecurableResourceId(), adminContext);
    allSecurableEntities.add(ste);
    te = new TestEntity();
    te.setDescription(Integer.toString(counter++));
    allEntities.add(te);
    //    } catch (UnauthorizedAccessAttemptException ex) {
    //      java.util.logging.Logger.getLogger(FilterResourceGroupTest.class.getName()).log(Level.SEVERE, null, ex);
    //    } catch (EntityAllreadyExistsException eaee) {
    //      java.util.logging.Logger.getLogger(FilterResourceGroupTest.class.getName()).log(Level.SEVERE, null, eaee);
    //    }
    try {
      entityManager.setAuthorizationContext(adminContext);
      allSecurableEntities = entityManager.find(SecurityTestEntity.class);
      allEntities = entityManager.find(TestEntity.class);
    } catch (UnauthorizedAccessAttemptException ex) {
      java.util.logging.Logger.getLogger(AccessResourcesTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Write entity to database. Set authorization context before.
   *
   * @param item entity
   * @param group group for context
   * @param role role for context
   */
  private static void persistItem(SecurityTestEntity item, GroupId group, Role role) {
    AuthorizationContext ctx = null;
    if ((group != null) && (role != null)) {
      ctx = new AuthorizationContext(userMember, group, role);
    }
    entityManager.setAuthorizationContext(ctx);
    try {
      entityManager.persist(item);
    } catch (UnauthorizedAccessAttemptException ex) {
      java.util.logging.Logger.getLogger(AccessResourcesTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  /**
   * Write entity to database. Set authorization context before.
   *
   * @param item entity
   * @param group group for context
   * @param role role for context
   */
  private static void persistItem(TestEntity item, GroupId group, Role role) {
    AuthorizationContext ctx = null;
    if ((group != null) && (role != null)) {
      ctx = new AuthorizationContext(userMember, group, role);
    }
    entityManager.setAuthorizationContext(ctx);
    try {
      entityManager.persist(item);
    } catch (UnauthorizedAccessAttemptException ex) {
      java.util.logging.Logger.getLogger(AccessResourcesTest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Test method contains">
  /**
   * Test contains on securableResource with role NOACCESS
   */
  @Test(expected = UnauthorizedAccessAttemptException.class)
  public void testContainsSecureWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    boolean contains = entityManager.contains(allSecurableEntities.get(0));
    assertFalse(contains);
    assertTrue(false);
  }

  /**
   * Test contains on securableResource with role GUEST
   */
  @Test
  public void testContainsSecureWithGuest() {
    SecurityTestEntity ste = allSecurableEntities.get(0);
    boolean throwException = true;
    if (ste.getSummary().startsWith(groupId.getStringRepresentation())) {
      throwException = false;
    }
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    try {
      entityManager.contains(ste);
      assertFalse(throwException);
    } catch (UnauthorizedAccessAttemptException ex) {
      assertTrue(throwException);
    }
  }

  /**
   * Test contains with role NOACCESS
   */
  @Test
  public void testContainsWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    assertTrue(entityManager.contains(allEntities.get(0)));
  }

  /**
   * Test contains with role GUEST
   */
  @Test
  public void testContainsWithGuest() {
    TestEntity ste = allEntities.get(0);
    boolean throwException = false;
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    try {
      entityManager.contains(ste);
      assertFalse(throwException);
    } catch (UnauthorizedAccessAttemptException ex) {
      assertTrue(throwException);
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Test method find(class)">
  /**
   * Test find on securableResource with role NOACCESS
   */
  @Test(expected = UnauthorizedAccessAttemptException.class)
  public void testFindSecureWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    List<? extends SecurityTestEntity> find = entityManager.find(allSecurableEntities.get(0).getClass());
    assertTrue(false);
  }

  /**
   * Test find with role GUEST
   */
  @Test
  public void testFindSecureWithGuest() {
    SecurityTestEntity ste = allSecurableEntities.get(0);
    boolean throwException = false;
    if (groupId.getStringRepresentation().startsWith("no access")) {
      throwException = true;
    }
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    try {
      List<? extends SecurityTestEntity> find = entityManager.find(allSecurableEntities.get(0).getClass());
      assertFalse(throwException);
      assertEquals(noOfResults, find.size());
      System.out.println("uuuuuuuuu - testFindSecureWithGuest group" + groupId.getStringRepresentation());
      for (SecurityTestEntity item : find) {
        System.out.println("uuuuuuuuuu - " + item);
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      assertTrue(throwException);
    }
  }

  /**
   * Test find with role NOACCESS
   */
  @Test
  public void testFindWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    List<? extends TestEntity> find = entityManager.find(allEntities.get(0).getClass());
    assertNotNull(find);
    // should return all entities
    assertEquals(allEntities.size(), find.size());
  }

  /**
   * Test find with role GUEST
   */
  @Test
  public void testFindWithGuest() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    List<? extends TestEntity> find = entityManager.find(allEntities.get(0).getClass());
    assertNotNull(find);
    // should return all entities
    assertEquals(allEntities.size(), find.size());
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Test method find(key)">
  /**
   * Test findKey on securableResource with role NOACCESS
   */
  @Test(expected = UnauthorizedAccessAttemptException.class)
  public void testFindKeySecureWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    entityManager.find(allSecurableEntities.get(0).getClass(), allSecurableEntities.get(0).getId());
    assertTrue(false);
  }

  /**
   * Test findKey with role GUEST
   */
  @Test
  public void testFindKeySecureWithGuest() {
    SecurityTestEntity ste = allSecurableEntities.get(0);
    boolean throwException = true;
    if (ste.getSummary().startsWith(groupId.getStringRepresentation())) {
      throwException = false;
    }
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    try {
      entityManager.find(allSecurableEntities.get(0).getClass(), allSecurableEntities.get(0).getId());
      assertFalse(throwException);
    } catch (UnauthorizedAccessAttemptException ex) {
      assertTrue(throwException);
    }
  }

  /**
   * Test findKey with role NOACCESS
   */
  @Test
  public void testFindKeyWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    TestEntity find = entityManager.find(allEntities.get(0).getClass(), allEntities.get(0).getId());
    assertNotNull(find);
    // should return all entities
    assertEquals(find.getDescription(), allEntities.get(0).getDescription());
  }

  /**
   * Test findKey with role GUEST
   */
  @Test
  public void testFindKeyWithGuest() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    TestEntity find = entityManager.find(allEntities.get(0).getClass(), allEntities.get(0).getId());
    assertNotNull(find);
    // should return all entities
    assertEquals(find.getDescription(), allEntities.get(0).getDescription());
  }

  /**
   * Test findKey on securableResource with role NOACCESS
   */
  @Test(expected = UnauthorizedAccessAttemptException.class)
  public void testFindUnknownKeySecureWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    entityManager.find(allSecurableEntities.get(0).getClass(), new Long(-1));
    assertTrue(false);
  }

  /**
   * Test findKey with role GUEST
   */
  @Test
  public void testFindUnknownKeySecureWithGuest() {
    SecurityTestEntity ste = allSecurableEntities.get(0);
    boolean throwException = true;
    if (ste.getSummary().startsWith(groupId.getStringRepresentation())) {
      throwException = false;
    }
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    try {
      SecurityTestEntity find = entityManager.find(allSecurableEntities.get(0).getClass(), new Long(-1));
      assertNull(find);
    } catch (UnauthorizedAccessAttemptException ex) {
      assertTrue(true);
    }
  }

  /**
   * Test findKey with role NOACCESS
   */
  @Test
  public void testFindUnknownKeyWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    try {
      TestEntity find = entityManager.find(allEntities.get(0).getClass(), new Long(-1));
      assertNull(find);
    } catch (UnauthorizedAccessAttemptException ex) {
      assertTrue(false);
    }
  }

  /**
   * Test findKey with role GUEST
   */
  @Test
  public void testFindUnknownKeyWithGuest() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    try {
      TestEntity find = entityManager.find(allEntities.get(0).getClass(), new Long(-1));
      assertNull(find);
    } catch (UnauthorizedAccessAttemptException ex) {
      assertTrue(true);
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Test method find(first, last)">
  /**
   * Test find on securableResource with role NOACCESS
   */
  @Test(expected = UnauthorizedAccessAttemptException.class)
  public void testFindRangeSecureWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    List<? extends SecurityTestEntity> find = entityManager.find(createRangeSecurityEntity(true), createRangeSecurityEntity(false));
    assertTrue(false);
  }

  /**
   * Test find with role GUEST
   */
  @Test
  public void testFindRangeSecureWithGuest() {
    SecurityTestEntity ste = allSecurableEntities.get(0);
    boolean throwException = false;
    int noOfResult = 2;
    if (groupId.getStringRepresentation().startsWith("no access")) {
      throwException = true;
    }
    if (groupId.getStringRepresentation().startsWith("guest")) {
      noOfResult = 0;
    }
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    try {
      List<SecurityTestEntity> find = entityManager.find(createRangeSecurityEntity(true), createRangeSecurityEntity(false));
      assertFalse(throwException);
      assertEquals(noOfResult, find.size());
    } catch (UnauthorizedAccessAttemptException ex) {
      assertTrue(throwException);
    }
  }

  /**
   * Test find with role NOACCESS
   */
  @Test
  public void testFindRangeWithNoAccess() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.NO_ACCESS);
    entityManager.setAuthorizationContext(act);
    List<? extends TestEntity> find = entityManager.find(createRangeEntity(true), createRangeEntity(false));
    assertNotNull(find);
    // should return all entities
    assertEquals(10, find.size());
  }

  /**
   * Test find with role GUEST
   */
  @Test
  public void testFindRangeWithGuest() throws UnauthorizedAccessAttemptException {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, Role.GUEST);
    entityManager.setAuthorizationContext(act);
    List<? extends TestEntity> find = entityManager.find(createRangeEntity(true), createRangeEntity(false));
    assertNotNull(find);
    // should return all entities
    assertEquals(10, find.size());
  }

  private SecurityTestEntity createRangeSecurityEntity(boolean first) {
    SecurityTestEntity ste = new SecurityTestEntity();
    if (first) {
      ste.setDescription("secure_" + "admin");
    } else {
      ste.setDescription("secure_" + "mem");
    }
    return ste;
  }

  private TestEntity createRangeEntity(boolean first) {
    TestEntity ste = new TestEntity();
    if (first) {
      ste.setDescription("admin");
    } else {
      ste.setDescription("halodri");
    }
    return ste;
  }
  //</editor-fold>

}
