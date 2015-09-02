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
package edu.kit.dama.authorization.services.base;

import edu.kit.dama.authorization.services.base.PlainAuthorizerLocal;
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
import java.util.ArrayList;
import java.util.List;
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
public class PlainAuthorizerLocalTest {
    UserId userId = new UserId("User1");
    GroupId groupId = new GroupId("Group1");
    SecurableResourceId resourceId = new SecurableResourceId("mydom", "res1");
    Role roleRestriction = Role.MANAGER;
    IAuthorizationContext sysContext = TestUtil.sysCtx;
    IAuthorizationContext normalContext;
    Role roleRequired  = Role.MEMBER;

    
    /**
     *
     * @throws EntityNotFoundException
     * @throws EntityAlreadyExistsException
     * @throws UnauthorizedAccessAttemptException
     */
    public PlainAuthorizerLocalTest() throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        
        TestUtil.clearDB();
        UserServiceLocal.getSingleton().register(userId, roleRequired, sysContext);
        GroupServiceLocal.getSingleton().create(groupId, userId, sysContext);
        ResourceServiceLocal.getSingleton().registerResource(resourceId, groupId, roleRequired, sysContext);
        normalContext = new AuthorizationContext(userId, groupId, Role.ADMINISTRATOR);

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

    /**
     * Test of authorize method, of class PlainAuthorizerLocal.
     * @throws Exception 
     */
    @Test
    public void testAuthorize_IAuthorizationContext_Role() throws Exception {
        System.out.println("authorize");
        try{
            PlainAuthorizerLocal.authorize(sysContext, roleRequired);
        }catch(UnauthorizedAccessAttemptException e){
            fail("Supposed to be authorized!");
        }
        
    }

    /**
     * Test of authorize method, of class PlainAuthorizerLocal.
     * @throws Exception 
     */
    @Test
    public void testAuthorize_3args_1() throws Exception {
        System.out.println("authorize");
        try{
            PlainAuthorizerLocal.authorize(sysContext, resourceId, roleRequired);
        }catch(UnauthorizedAccessAttemptException e){
            fail("Supposed to be authorized!");
        }
    }

    /**
     * Test of authorize method, of class PlainAuthorizerLocal.
     * @throws Exception 
     */
    @Test
    public void testAuthorize_3args_2() throws Exception {
        List<SecurableResourceId> resourceIds = null;
        try{
            PlainAuthorizerLocal.authorize(sysContext, resourceIds, roleRequired);
        }catch(UnauthorizedAccessAttemptException e){
            fail("Supposed to be authorized!");
        }
    }
    
    /**
     *
     * @throws EntityNotFoundException
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityAlreadyExistsException
     */
    @Test
    public void testFilter() throws EntityNotFoundException, UnauthorizedAccessAttemptException, EntityAlreadyExistsException{
        List<SecurableResourceId> resList = new ArrayList<SecurableResourceId>();
        SecurableResourceId current;
        final int count = 15;
        for(int i = 0; i < count; ++i){
            current = new SecurableResourceId("mydom", "resource_id"+i);
            ResourceServiceLocal.getSingleton().registerResource(current, groupId, roleRequired, sysContext);            
            resList.add(current);
        }
        current = new SecurableResourceId("mydom", "resource_id_noref");
        ResourceServiceLocal.getSingleton().registerResource(current, sysContext);            
        resList.add(current);
        List<SecurableResourceId> filteredList = new ArrayList<SecurableResourceId>();
        PlainAuthorizerLocal.filterOnAccessAllowed(normalContext, roleRequired, resList, filteredList);
        assertEquals(resList.size(), filteredList.size()+1);
        
    }
}
