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

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.FilterOutput;
import edu.kit.dama.authorization.annotations.resources.SecurityTestEntity;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.ResourceServiceLocal;
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
 *
 * @author hartmann-v
 */
@RunWith(Parameterized.class)
public class FilterResourceGroupTest extends SecurityUtil {

  private static volatile boolean initialized = false;
  private static volatile ArrayList<SecurityTestEntity> allEntities = new ArrayList<SecurityTestEntity>();
  private int noOfResults;
  /**
   * For logging purposes.
   */
  private static Logger LOGGER = LoggerFactory.getLogger(FilterResourceGroupTest.class);

    /**
     *
     * @param resourceUserRole
     * @param resourceGroupRole
     * @param userId
     * @param groupId
     * @param userRole
     * @param noOfResults
     */
    public FilterResourceGroupTest(Role resourceUserRole, Role resourceGroupRole, UserId userId, GroupId groupId, Role userRole, int noOfResults) {
    this.resourceUserRole = resourceUserRole;
    this.resourceGroupRole = resourceGroupRole;
    this.userId = userId;
    this.groupId = groupId;
    this.noOfResults = noOfResults;
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

            new Object[]{null, null, userNoAccess, groupNoAccess, Role.NO_ACCESS, 0}, //0  
            new Object[]{null, null, userNoAccess, groupGuest, Role.GUEST, 0},
            new Object[]{null, null, userNoAccess, groupMember, Role.MEMBER, 0},
            new Object[]{null, null, userNoAccess, groupManager, Role.MANAGER, 0},
            new Object[]{null, null, userNoAccess, groupAdmin, Role.ADMINISTRATOR, 0},
            new Object[]{null, null, userGuest, groupNoAccess, Role.NO_ACCESS, 0}, //5  
            new Object[]{null, null, userGuest, groupGuest, Role.GUEST, 4},
            new Object[]{null, null, userGuest, groupMember, Role.MEMBER, 4},
            new Object[]{null, null, userGuest, groupManager, Role.MANAGER, 4},
            new Object[]{null, null, userGuest, groupAdmin, Role.ADMINISTRATOR, 4},
            new Object[]{null, null, userMember, groupNoAccess, Role.NO_ACCESS, 0}, //10  
            new Object[]{null, null, userMember, groupGuest, Role.GUEST, 4},
            new Object[]{null, null, userMember, groupMember, Role.MEMBER, 4},
            new Object[]{null, null, userMember, groupManager, Role.MANAGER, 4},
            new Object[]{null, null, userMember, groupAdmin, Role.ADMINISTRATOR, 4},
            new Object[]{null, null, userManager, groupNoAccess, Role.NO_ACCESS, 0}, //15  
            new Object[]{null, null, userManager, groupGuest, Role.GUEST, 4},
            new Object[]{null, null, userManager, groupMember, Role.MEMBER, 4},
            new Object[]{null, null, userManager, groupManager, Role.MANAGER, 4},
            new Object[]{null, null, userManager, groupAdmin, Role.ADMINISTRATOR, 4},
            new Object[]{null, null, userAdmin, groupNoAccess, Role.NO_ACCESS, 0}, //20  
            new Object[]{null, null, userAdmin, groupGuest, Role.GUEST, 4},
            new Object[]{null, null, userAdmin, groupMember, Role.MEMBER, 4},
            new Object[]{null, null, userAdmin, groupManager, Role.MANAGER, 4},
            new Object[]{null, null, userAdmin, groupAdmin, Role.ADMINISTRATOR, 4});
  }
  //</editor-fold>

    /**
     *
     */
    @BeforeClass
  public static void prepareClass() {
    prepare();
    initResources();

  }

    /**
     *
     */
    @AfterClass
  public static void releaseTestAfterTest() {
    release();
  }

  private static void initResources() {
    Role allRoles[] = {Role.NO_ACCESS, Role.GUEST, Role.MEMBER, Role.MANAGER, Role.ADMINISTRATOR};
    GroupId allGroups[] = {groupNoAccess, groupGuest, groupMember, groupManager, groupAdmin};
    initialized = true;
    int counter = 0;
    try {
      // Add 2 Entries without any registration 
      // One at beginning...
      SecurityTestEntity ste = new SecurityTestEntity();
      ste.setSummary(Integer.toString(counter++));
      allEntities.add(ste);
      for (GroupId group : allGroups) {
        for (Role role : allRoles) {
          ste = new SecurityTestEntity();
          ste.setSummary(group.getStringRepresentation() + "_" + role);
          //ResourceServiceLocal.registerResource(ste.getSecurableResourceId(), adminContext);
          allEntities.add(ste);
          ResourceServiceLocal.getSingleton().registerResource(ste.getSecurableResourceId(), group, role, adminContext);
        }

      }
      // ...one at the end.
      ste = new SecurityTestEntity();
      ste.setSummary(Integer.toString(counter++));
      ResourceServiceLocal.getSingleton().registerResource(ste.getSecurableResourceId(), adminContext);
      allEntities.add(ste);
    } catch (EntityNotFoundException ex) {
      java.util.logging.Logger.getLogger(FilterResourceGroupTest.class.getName()).log(Level.SEVERE, null, ex);
    } catch (UnauthorizedAccessAttemptException ex) {
      java.util.logging.Logger.getLogger(FilterResourceGroupTest.class.getName()).log(Level.SEVERE, null, ex);
    } catch (EntityAlreadyExistsException eaee) {
      java.util.logging.Logger.getLogger(FilterResourceGroupTest.class.getName()).log(Level.SEVERE, null, eaee);
    }

  }

  /**
   * Test for default parameter.
   */
  @Test
  public void testDefault() {
    AuthorizationContext act = new AuthorizationContext(userId, groupId, userRole);
     try {
      List<SecurityTestEntity> filteredEntities = getAllEntities(act);
      assertEquals(noOfResults, filteredEntities.size());
      // All items should be registered to the given group -> DomainUniqueId contains group name.
      for (SecurityTestEntity item : filteredEntities) {
        assertTrue(item.getSecurableResourceId().getDomainUniqueId().contains(groupId.getStringRepresentation()));
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      assertFalse(true);
    } catch (EntityNotFoundException nfe) {
      assertFalse(true);
    }
  }

    /**
     *
     * @param act
     * @return
     * @throws UnauthorizedAccessAttemptException
     * @throws EntityNotFoundException
     */
    @FilterOutput(roleRequired = Role.GUEST)
  public List<SecurityTestEntity> getAllEntities(@Context AuthorizationContext act) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    List<SecurityTestEntity> copyList = new ArrayList<SecurityTestEntity>();
    copyList.addAll(allEntities);

    return copyList;
  }
}
