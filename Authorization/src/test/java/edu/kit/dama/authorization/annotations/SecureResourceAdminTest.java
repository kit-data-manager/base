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

package edu.kit.dama.authorization.annotations;

import edu.kit.dama.authorization.annotations.resources.SecurityTestEntity;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.ResourceServiceLocal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author hartmann-v
 */
@RunWith(Parameterized.class)
public class SecureResourceAdminTest extends SecurityUtil {

  /**
   * For logging purposes.
   */
  private static Logger LOGGER = LoggerFactory.getLogger(SecureResourceAdminTest.class);

    /**
     *
     * @param resourceUserRole
     * @param resourceGroupRole
     * @param userId
     * @param groupId
     * @param userRole
     * @param success
     */
    public SecureResourceAdminTest(Role resourceUserRole, Role resourceGroupRole, UserId userId, GroupId groupId, Role userRole, Boolean success) {
    this.resourceUserRole = resourceUserRole;
    this.resourceGroupRole = resourceGroupRole;
    this.userId = userId;
    this.groupId = groupId;
    this.success = success;
    this.userRole = userRole;
  }
  
  //<editor-fold defaultstate="collapsed" desc="parameters for the testclass">
    /**
     *
     * @return
     */
    @Parameterized.Parameters
  public static List<Object[]> testData() {
    return Arrays.asList(
            //Resource User Role, Resource Group Role, Context User, Context Group, Success.,
            
            new Object[]{null, null, userNoAccess, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //0  
            new Object[]{null, null, userNoAccess, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, null, userNoAccess, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, null, userNoAccess, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, null, userNoAccess, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
            
            new Object[]{null, null, userGuest, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //5  
            new Object[]{null, null, userGuest, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, null, userGuest, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, null, userGuest, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, null, userGuest, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
            
            new Object[]{null, null, userMember, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //10  
            new Object[]{null, null, userMember, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, null, userMember, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, null, userMember, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, null, userMember, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
            
            new Object[]{null, null, userManager, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //15  
            new Object[]{null, null, userManager, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, null, userManager, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, null, userManager, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, null, userManager, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
            
            new Object[]{null, null, userAdmin, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //20  
            new Object[]{null, null, userAdmin, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, null, userAdmin, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, null, userAdmin, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, null, userAdmin, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
            
            new Object[]{null, Role.NO_ACCESS, userNoAccess, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //25  
            new Object[]{null, Role.NO_ACCESS, userNoAccess, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userNoAccess, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userNoAccess, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userNoAccess, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
     
            new Object[]{null, Role.NO_ACCESS, userGuest, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //30  
            new Object[]{null, Role.NO_ACCESS, userGuest, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userGuest, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userGuest, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userGuest, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
 
            new Object[]{null, Role.NO_ACCESS, userMember, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //35  
            new Object[]{null, Role.NO_ACCESS, userMember, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userMember, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userMember, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userMember, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
   
            new Object[]{null, Role.NO_ACCESS, userManager, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //40  
            new Object[]{null, Role.NO_ACCESS, userManager, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userManager, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userManager, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userManager, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
         
            new Object[]{null, Role.NO_ACCESS, userAdmin, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //45  
            new Object[]{null, Role.NO_ACCESS, userAdmin, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userAdmin, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userAdmin, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.NO_ACCESS, userAdmin, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
            
            new Object[]{null, Role.GUEST, userNoAccess, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //50  
            new Object[]{null, Role.GUEST, userNoAccess, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userNoAccess, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userNoAccess, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userNoAccess, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
     
            new Object[]{null, Role.GUEST, userGuest, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //55  
            new Object[]{null, Role.GUEST, userGuest, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userGuest, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userGuest, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userGuest, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
 
            new Object[]{null, Role.GUEST, userMember, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //60  
            new Object[]{null, Role.GUEST, userMember, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userMember, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userMember, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userMember, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
   
            new Object[]{null, Role.GUEST, userManager, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //65  
            new Object[]{null, Role.GUEST, userManager, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userManager, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userManager, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userManager, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
         
            new Object[]{null, Role.GUEST, userAdmin, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //70  
            new Object[]{null, Role.GUEST, userAdmin, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userAdmin, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userAdmin, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.GUEST, userAdmin, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
            
            new Object[]{null, Role.MEMBER, userNoAccess, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //75  
            new Object[]{null, Role.MEMBER, userNoAccess, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MEMBER, userNoAccess, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.MEMBER, userNoAccess, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.MEMBER, userNoAccess, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
     
            new Object[]{null, Role.MEMBER, userGuest, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //80  
            new Object[]{null, Role.MEMBER, userGuest, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MEMBER, userGuest, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.MEMBER, userGuest, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.MEMBER, userGuest, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
 
            new Object[]{null, Role.MEMBER, userMember, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //85  
            new Object[]{null, Role.MEMBER, userMember, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MEMBER, userMember, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, Role.MEMBER, userMember, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, Role.MEMBER, userMember, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
   
            new Object[]{null, Role.MEMBER, userManager, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //90  
            new Object[]{null, Role.MEMBER, userManager, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MEMBER, userManager, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, Role.MEMBER, userManager, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, Role.MEMBER, userManager, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
         
            new Object[]{null, Role.MEMBER, userAdmin, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //95  
            new Object[]{null, Role.MEMBER, userAdmin, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MEMBER, userAdmin, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, Role.MEMBER, userAdmin, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, Role.MEMBER, userAdmin, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
            
            new Object[]{null, Role.MANAGER, userNoAccess, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //100  
            new Object[]{null, Role.MANAGER, userNoAccess, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MANAGER, userNoAccess, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.MANAGER, userNoAccess, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.MANAGER, userNoAccess, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
     
            new Object[]{null, Role.MANAGER, userGuest, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //105  
            new Object[]{null, Role.MANAGER, userGuest, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MANAGER, userGuest, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.MANAGER, userGuest, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.MANAGER, userGuest, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
 
            new Object[]{null, Role.MANAGER, userMember, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //110  
            new Object[]{null, Role.MANAGER, userMember, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MANAGER, userMember, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, Role.MANAGER, userMember, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, Role.MANAGER, userMember, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
   
            new Object[]{null, Role.MANAGER, userManager, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //115  
            new Object[]{null, Role.MANAGER, userManager, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MANAGER, userManager, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, Role.MANAGER, userManager, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, Role.MANAGER, userManager, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
         
            new Object[]{null, Role.MANAGER, userAdmin, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //120  
            new Object[]{null, Role.MANAGER, userAdmin, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.MANAGER, userAdmin, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, Role.MANAGER, userAdmin, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, Role.MANAGER, userAdmin, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
            
            new Object[]{null, Role.ADMINISTRATOR, userNoAccess, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //125  
            new Object[]{null, Role.ADMINISTRATOR, userNoAccess, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.ADMINISTRATOR, userNoAccess, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.ADMINISTRATOR, userNoAccess, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.ADMINISTRATOR, userNoAccess, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
     
            new Object[]{null, Role.ADMINISTRATOR, userGuest, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //130  
            new Object[]{null, Role.ADMINISTRATOR, userGuest, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.ADMINISTRATOR, userGuest, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, Role.ADMINISTRATOR, userGuest, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, Role.ADMINISTRATOR, userGuest, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
 
            new Object[]{null, Role.ADMINISTRATOR, userMember, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //135  
            new Object[]{null, Role.ADMINISTRATOR, userMember, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.ADMINISTRATOR, userMember, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, Role.ADMINISTRATOR, userMember, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, Role.ADMINISTRATOR, userMember, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
   
            new Object[]{null, Role.ADMINISTRATOR, userManager, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //140  
            new Object[]{null, Role.ADMINISTRATOR, userManager, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.ADMINISTRATOR, userManager, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, Role.ADMINISTRATOR, userManager, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, Role.ADMINISTRATOR, userManager, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
         
            new Object[]{null, Role.ADMINISTRATOR, userAdmin, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //145  
            new Object[]{null, Role.ADMINISTRATOR, userAdmin, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, Role.ADMINISTRATOR, userAdmin, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, Role.ADMINISTRATOR, userAdmin, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, Role.ADMINISTRATOR, userAdmin, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE}
            );
  }
  //</editor-fold>

    /**
     *
     */
    @BeforeClass
  public static void prepareClass() {
    prepare();

  }

    /**
     *
     */
    @AfterClass
  public static void releaseTestAfterTest() {
    release();
  }

  /**
   * Test for default parameter.
   */
  @Test
  public void testDefault() {
      AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
      SecurityTestEntity ste = new SecurityTestEntity();
      ste.setSummary(Long.toString(new Date().getTime()));
      try{
      if (resourceUserRole != null) {
        ResourceServiceLocal.getSingleton().registerResource(ste.getSecurableResourceId(), resourceUserRole, userId, resourceUserRole, adminContext);
      }
      if (resourceGroupRole != null) {
        ResourceServiceLocal.getSingleton().registerResource(ste.getSecurableResourceId(), groupId, resourceGroupRole, adminContext);
      }
      } catch (EntityNotFoundException enfe) {
        
      } catch (UnauthorizedAccessAttemptException enfe) {
        
      }
     try {
     securedMethodPersist_member(act, ste);
      assertTrue(success);
    } catch (UnauthorizedAccessAttemptException ex) {
      assertFalse(success);
    }catch (EntityNotFoundException nfe) {
      assertFalse(success);
    }
  }
}
