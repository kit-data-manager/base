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
package edu.kit.dama.mdm.core.test;

import edu.kit.dama.authorization.annotations.resources.SecurityTestEntity;
import edu.kit.dama.authorization.annotations.resources.TestEntity;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.UserServiceLocal;
import edu.kit.dama.authorization.services.administration.impl.TestUtil;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.util.Constants;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hartmann-v
 */
public class SecurityUtil {

  /**
   * For logging purposes.
   */
  private static Logger LOGGER = LoggerFactory.getLogger(SecurityUtil.class);
  protected static final UserId userNoAccess = new UserId("no access");
  protected static final UserId userGuest = new UserId("guest");
  protected static final UserId userMember = new UserId("member");
  protected static final UserId userManager = new UserId("manager");
  protected static final UserId userAdmin = new UserId("admin");
  protected static final GroupId groupNoAccess = new GroupId("no access");
  protected static final GroupId groupGuest = new GroupId("guest");
  protected static final GroupId groupMember = new GroupId("member");
  protected static final GroupId groupManager = new GroupId("manager");
  protected static final GroupId groupAdmin = new GroupId("admin");
  protected Role resourceUserRole;
  protected Role resourceGroupRole;
  protected UserId userId;
  protected GroupId groupId;
  protected boolean success;
  protected Role userRole;
  protected static IMetaDataManager entityManager = null;
  public static final AuthorizationContext adminContext = new AuthorizationContext(new UserId(Constants.SYSTEM_ADMIN), new GroupId(Constants.SYSTEM_GROUP), Role.ADMINISTRATOR);

  public SecurityUtil() {
  }

  public static void prepare() throws UnauthorizedAccessAttemptException, EntityNotFoundException {

    removeEntities(entityManager);

    TestUtil.clearDB();

    try {
      UserServiceLocal.getSingleton().register(userNoAccess, Role.NO_ACCESS, adminContext);
      UserServiceLocal.getSingleton().register(userGuest, Role.GUEST, adminContext);
      UserServiceLocal.getSingleton().register(userMember, Role.MEMBER, adminContext);
      UserServiceLocal.getSingleton().register(userManager, Role.MANAGER, adminContext);
      UserServiceLocal.getSingleton().register(userAdmin, Role.ADMINISTRATOR, adminContext);

      addUsers2Group(groupNoAccess, Role.NO_ACCESS, userNoAccess, userGuest, userMember, userManager, userAdmin);
      addUsers2Group(groupGuest, Role.GUEST, userGuest, userNoAccess, userMember, userManager, userAdmin);
      addUsers2Group(groupMember, Role.MEMBER, userMember, userNoAccess, userGuest, userManager, userAdmin);
      addUsers2Group(groupManager, Role.MANAGER, userManager, userNoAccess, userGuest, userMember, userAdmin);
      addUsers2Group(groupAdmin, Role.ADMINISTRATOR, userAdmin, userNoAccess, userGuest, userMember, userManager);
    } catch (EntityAlreadyExistsException ex) {
      java.util.logging.Logger.getLogger(SecurityUtil.class.getName()).log(Level.SEVERE, null, ex);
    } catch (UnauthorizedAccessAttemptException ex) {
      java.util.logging.Logger.getLogger(SecurityUtil.class.getName()).log(Level.SEVERE, null, ex);
    }

  }

  public static void release() throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    removeEntities(entityManager);

    TestUtil.clearDB();
  }

  private static void removeEntities(IMetaDataManager entityManager) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    entityManager.setAuthorizationContext(adminContext);
    List<SecurityTestEntity> find = entityManager.find(SecurityTestEntity.class);
    for (SecurityTestEntity item : find) {
      try {
        entityManager.remove(item);
      } catch (EntityNotFoundException ex) {
      }
    }
    List<TestEntity> findTe = entityManager.find(TestEntity.class);
    for (TestEntity item : findTe) {
      try {
        entityManager.remove(item);
      } catch (EntityNotFoundException ex) {
      }
    }
  }

  private static void addUsers2Group(GroupId group, Role maxRole, UserId manager, UserId... members) {
    try {
      GroupServiceLocal.getSingleton().create(group, manager, adminContext);
      if (maxRole == Role.ADMINISTRATOR) {
        GroupServiceLocal.getSingleton().changeRole(group, manager, maxRole, adminContext);
      }
      for (UserId item : members) {
        GroupServiceLocal.getSingleton().addUser(group, item, maxRole, adminContext);
      }
    } catch (EntityNotFoundException ex) {
      java.util.logging.Logger.getLogger(SecurityUtil.class.getName()).log(Level.SEVERE, null, ex);
    } catch (EntityAlreadyExistsException ex) {
      java.util.logging.Logger.getLogger(SecurityUtil.class.getName()).log(Level.SEVERE, null, ex);
    } catch (UnauthorizedAccessAttemptException ex) {
      java.util.logging.Logger.getLogger(SecurityUtil.class.getName()).log(Level.SEVERE, null, ex);
    }


  }
  // <editor-fold defaultstate="collapsed" desc="Secure Methods without SecureArguments">
}
