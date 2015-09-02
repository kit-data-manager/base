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
package edu.kit.dama.authorization.services.administration.impl;

import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.services.administration.impl.UserServiceImpl;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.entities.util.PU;
import org.junit.*;

import static org.junit.Assert.*;

/**
 *
 * @author soc
 */
public class UserServiceImplTest {

  UserServiceImpl implInstance = new UserServiceImpl();
  IAuthorizationContext ctx = TestUtil.sysCtx;

  /**
   *
   */
  public UserServiceImplTest() {
  }

  /**
   *
   * @throws Exception
   */
  @BeforeClass
  public static void setUpClass() throws Exception {
    PU.setPersistenceUnitName("AuthorizationUnit-Test");
  }

  /**
   *
   * @throws Exception
   */
  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  /**
   *
   */
  @Before
  public void setUp() {
    TestUtil.clearDB();
  }

  /**
   *
   */
  @After
  public void tearDown() {
//        TestUtil.tearDownMethod();
  }

  /**
   * Test of register method, of class UserServiceImpl.
   *
   * @throws EntityAlreadyExistsException
   * @throws UnauthorizedAccessAttemptException
   */
  @Test
  public void testRegister() throws EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    implInstance.register(new UserId("User1"), Role.MEMBER, ctx);
  }

  /**
   * Test of register method, of class UserServiceImpl.
   *
   * @throws EntityAlreadyExistsException
   * @throws UnauthorizedAccessAttemptException
   */
  @Test(expected = EntityAlreadyExistsException.class)
  public void testReRegister() throws EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    implInstance.register(new UserId("User1"), Role.MEMBER, ctx);
    implInstance.register(new UserId("User1"), Role.MEMBER, ctx);
  }

  /**
   * Test of getRoleRestriction method, of class UserServiceImpl.
   *
   * @throws EntityNotFoundException
   * @throws EntityAlreadyExistsException
   * @throws UnauthorizedAccessAttemptException
   */
  @Test
  public void testGetRoleRestrictionAfterRegister() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    System.out.println("getRoleRestriction");
    UserId user = new UserId("User1"); // Need to have a reason
    Role expResult = Role.MEMBER;
    implInstance.register(user, expResult, ctx);
    IRoleRestriction result = implInstance.getRoleRestriction(user, ctx);
    assertEquals(expResult, result);
  }

  /**
   * Test of getRoleRestriction method, of class UserServiceImpl.
   *
   * @throws EntityNotFoundException
   * @throws UnauthorizedAccessAttemptException
   */
  @Test(expected = EntityNotFoundException.class)
  public void testGetRoleRestrictionNotRegistered() throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    System.out.println("getRoleRestriction");
    UserId user = new UserId("User1"); // Need to have a reason
    Role expResult = Role.MEMBER;
    IRoleRestriction result = implInstance.getRoleRestriction(user, ctx);
  }

  /**
   * Test of setRoleRestriction method, of class UserServiceImpl.
   *
   * @throws EntityNotFoundException
   * @throws EntityAlreadyExistsException
   * @throws UnauthorizedAccessAttemptException
   */
  @Test
  public void testSetRoleRestrictionAfterRegister() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    System.out.println("setRoleRestriction");

    UserId user = new UserId("User1"); // Need to have a reason
    Role expResult1 = Role.GUEST;
    Role expResult2 = Role.ADMINISTRATOR;
    implInstance.register(user, expResult1, ctx);
    implInstance.setRoleRestriction(user, expResult2, ctx);
    IRoleRestriction result = implInstance.getRoleRestriction(user, ctx);
    assertEquals(expResult2, result);
  }

  /**
   * Test of setRoleRestriction method, of class UserServiceImpl.
   *
   * @throws EntityNotFoundException
   * @throws UnauthorizedAccessAttemptException
   */
  @Test(expected = EntityNotFoundException.class)
  public void testSetRoleRestrictionNotRegistered() throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    System.out.println("setRoleRestriction");

    UserId user = new UserId("User1"); // Need to have a reason
    Role expResult1 = Role.GUEST;
    implInstance.setRoleRestriction(user, expResult1, ctx);

  }
}
