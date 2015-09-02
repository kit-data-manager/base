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
package edu.kit.dama.rest.services.staging.test.util;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;

/**
 *
 * @author jejkal
 */
public class TestAuthorizationContext implements IAuthorizationContext {

  private String user = null;
  private String group = null;
  private Role role = null;

  public TestAuthorizationContext(String pUser, String pGroup, Role pRole) {
    user = pUser;
    group = pGroup;
    role = pRole;
  }

  @Override
  public void setUserId(UserId userId) {
  }

  @Override
  public void setGroupId(GroupId groupId) {
  }

  @Override
  public void setRoleRestriction(Role roleRestriction) {
    role = roleRestriction;
  }

  @Override
  public UserId getUserId() {
    return new UserId(user);
  }

  @Override
  public GroupId getGroupId() {
    return new GroupId(group);
  }

  @Override
  public Role getRoleRestriction() {
    return role;
  }

  @Override
  public String toString() {
    return getUserId() + "/" + getGroupId() + "/" + getRoleRestriction(); //To change body of generated methods, choose Tools | Templates.
  }
  
  
}
