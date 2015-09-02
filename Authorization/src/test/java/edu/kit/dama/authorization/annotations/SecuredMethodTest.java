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
package edu.kit.dama.authorization.annotations;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.ResourceServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.authorization.services.administration.impl.TestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author pasic
 */
public class SecuredMethodTest {

    UserId userId = new UserId("UserA");
    GroupId groupId = new GroupId("GroupA");
    SecurableResourceId resourceId = new SecurableResourceId("mydom", "res1");
    Role roleRestriction = Role.MANAGER;
    IAuthorizationContext context = new AuthorizationContext(userId, groupId, roleRestriction);
    IAuthorizationContext sysCtx = TestUtil.sysCtx;
    Role roleRequired = Role.MEMBER;

    /**
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    public SecuredMethodTest() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        TestUtil.clearDB();
        UserServiceLocal.getSingleton().register(userId, roleRequired, sysCtx);
        GroupServiceLocal.getSingleton().create(groupId, userId, sysCtx);
        ResourceServiceLocal.getSingleton().registerResource(resourceId, groupId, roleRequired, sysCtx);

    }

    /**
     *
     * @throws Exception
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
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
    }

    /**
     *
     */
    @After
    public void tearDown() {
    }
    
    @SecuredMethod(roleRequired = Role.MEMBER)
    void secMethod_1_Res_1(@Context IAuthorizationContext ctx,
            @SecuredArgument SecurableResourceId res) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        System.out.println("secMethod_No_Res_2");
    }

    @SecuredMethod(roleRequired = Role.MEMBER)
    int secMethod_No_Res_1(@Context IAuthorizationContext ctx, int dummy) throws UnauthorizedAccessAttemptException {
        System.out.println("secMethod_No_Res_1");
        return ++dummy;
    }

    @SecuredMethod(roleRequired = Role.MEMBER)
    void secMethod_No_Res_2(@Context IAuthorizationContext ctx) throws UnauthorizedAccessAttemptException {
        System.out.println("secMethod_No_Res_2");
    }

    @SecuredMethod(roleRequired = Role.MEMBER)
    void secMethod_No_Res_3(@Context IAuthorizationContext ctx, SecurableResourceId res) throws UnauthorizedAccessAttemptException {
        System.out.println("secMethod_No_Res_2");
    }

    /**
     * Test of roleRequired method, of class SecuredMethod.
     * @throws EntityAlreadyExistsException 
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityNotFoundException  
     */
    @Test
    public void testSecMethod_No_Res() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
        try {
            //Should be OK
            secMethod_No_Res_1(context, 1);
            secMethod_No_Res_2(context);
            secMethod_No_Res_3(context, new SecurableResourceId());
        } catch (UnauthorizedAccessAttemptException ex) {
            fail("No exception expected!");
        }
        UserId userId1 = new UserId("User1");
        UserServiceLocal.getSingleton().register(userId1, Role.GUEST, sysCtx);
        GroupServiceLocal.getSingleton().addUser(groupId, userId1, roleRequired, sysCtx);
        IAuthorizationContext ctx1 = new AuthorizationContext(userId1, groupId, Role.MANAGER);
        try {
            secMethod_No_Res_1(ctx1, 1);
            fail("Exception expected!");
        } catch (UnauthorizedAccessAttemptException ex) {
            //OK
        }
        try {
            secMethod_No_Res_2(ctx1);
            fail("Exception expected!");
        } catch (UnauthorizedAccessAttemptException ex) {
            //OK
        }
        try {
            secMethod_No_Res_3(ctx1, new SecurableResourceId());
            fail("Exception expected!");
        } catch (UnauthorizedAccessAttemptException ex) {
            //OK
        }
    }

    /**
     *
     * @throws EntityAlreadyExistsException
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     */
    @Test
    public void testSecMethod_1_Res_1() throws EntityAlreadyExistsException, EntityNotFoundException, UnauthorizedAccessAttemptException {
//        try {
//            //Should be OK
//            secMethod_No_Res_1(context, 1);
//            secMethod_No_Res_2(context);
//        } catch (UnauthorizedAccessAttemptException ex) {
//            fail("No exception expected!");
//        }
      
        try {
            secMethod_1_Res_1(context, new SecurableResourceId());
            fail("Exception expected!");
        } catch (EntityNotFoundException ex) {
            //OK
        } catch (UnauthorizedAccessAttemptException ex) {
            fail("EntityNotFoundException expected!");
        }
        try {
            secMethod_1_Res_1(context, resourceId);
        } catch (UnauthorizedAccessAttemptException ex) {
        }
        UserId userId1 = new UserId("User1");
        UserServiceLocal.getSingleton().register(userId1, Role.GUEST, sysCtx);
        GroupServiceLocal.getSingleton().addUser(groupId, userId1, roleRequired, sysCtx);
        IAuthorizationContext ctx1 = new AuthorizationContext(userId1, groupId, Role.MANAGER);
        try {
            secMethod_1_Res_1(ctx1, resourceId);
            fail("Exception expected!");
        } catch (UnauthorizedAccessAttemptException ex) {
            //OK
        }
        UserServiceLocal.getSingleton().setRoleRestriction(userId1, Role.MEMBER, sysCtx);
        try {
            secMethod_1_Res_1(ctx1, resourceId);
        } catch (UnauthorizedAccessAttemptException ex) {
            fail("No exception expected!");
        }
    }


}
