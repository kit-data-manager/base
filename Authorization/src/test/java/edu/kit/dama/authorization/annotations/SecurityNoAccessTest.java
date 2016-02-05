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

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import java.util.Arrays;
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
public class SecurityNoAccessTest extends SecurityUtil {

  /**
   * For logging purposes.
   */
  private static Logger LOGGER = LoggerFactory.getLogger(SecurityGuestTest.class);

    /**
     *
     * @param resourceUserRole
     * @param resourceGroupRole
     * @param userId
     * @param groupId
     * @param userRole
     * @param success
     */
    public SecurityNoAccessTest(Role resourceUserRole, Role resourceGroupRole, UserId userId, GroupId groupId, Role userRole, Boolean success) {
    this.resourceUserRole = resourceUserRole;
    this.resourceGroupRole = resourceGroupRole;
    this. userId = userId;
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
            //Resource User Role, Resource Group Role, Context User, Context Group, Success.
            
            new Object[]{null, null, userNoAccess, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //0  
            new Object[]{null, null, userNoAccess, groupGuest, Role.GUEST, Boolean.FALSE},
            new Object[]{null, null, userNoAccess, groupMember, Role.MEMBER, Boolean.FALSE},
            new Object[]{null, null, userNoAccess, groupManager, Role.MANAGER, Boolean.FALSE},
            new Object[]{null, null, userNoAccess, groupAdmin, Role.ADMINISTRATOR, Boolean.FALSE},
            
            new Object[]{null, null, userGuest, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //5  
            new Object[]{null, null, userGuest, groupGuest, Role.GUEST, Boolean.TRUE},
            new Object[]{null, null, userGuest, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, null, userGuest, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, null, userGuest, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
            
            new Object[]{null, null, userMember, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //10  
            new Object[]{null, null, userMember, groupGuest, Role.GUEST, Boolean.TRUE},
            new Object[]{null, null, userMember, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, null, userMember, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, null, userMember, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
            
            new Object[]{null, null, userManager, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //15  
            new Object[]{null, null, userManager, groupGuest, Role.GUEST, Boolean.TRUE},
            new Object[]{null, null, userManager, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, null, userManager, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, null, userManager, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE},
            
            new Object[]{null, null, userAdmin, groupNoAccess, Role.NO_ACCESS, Boolean.FALSE}, //20  
            new Object[]{null, null, userAdmin, groupGuest, Role.GUEST, Boolean.TRUE},
            new Object[]{null, null, userAdmin, groupMember, Role.MEMBER, Boolean.TRUE},
            new Object[]{null, null, userAdmin, groupManager, Role.MANAGER, Boolean.TRUE},
            new Object[]{null, null, userAdmin, groupAdmin, Role.ADMINISTRATOR, Boolean.TRUE}
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
     try {
     securedMethod_guest(act, 1);
      assertTrue(success);
    } catch (UnauthorizedAccessAttemptException ex) {
      assertFalse(success);
    }
  }
}
