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
package edu.kit.dama.ui.user;

import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.mdm.base.UserData;
import java.util.List;

/**
 *
 * @author jejkal
 */
public abstract class AbstractUserBridge<C> {

  /**
   * The current user logged in
   */
  private UserData currentUser = UserData.NO_USER;

  /**
   * Create an abstract user bridge
   */
  public AbstractUserBridge(UserData pCurrentUser) {
    setCurrentUser(pCurrentUser);
  }

  /**
   * Set the current user logged in.
   *
   * @param pCurrentUser The current user.
   */
  public final void setCurrentUser(UserData pCurrentUser) {
    if (pCurrentUser == null) {
      currentUser = UserData.NO_USER;
    } else {
      currentUser = pCurrentUser;
    }
  }

  /**
   * Get the current user logged in.
   *
   * @return The current user.
   */
  public UserData getCurrentUser() {
    return currentUser;
  }

  public abstract UserData getUserFromRequest(C pRequest);

  /**
   * Get the user for the provided user id.
   *
   * @param pId The user id
   *
   * @return The user for the id pId
   */
  public abstract UserData getUserById(UserId pId);

  /**
   * Get all users by the provided first name. Depending of the user bridge
   * implementation, this first name must exactly match the users first name or
   * it may begin with the provided string/contains the string.
   *
   * @param pFirstName The first name of a user or a part of the first name
   * @param pStart The first index if there are multiple results.
   * @param pCnt The max. number of results.
   *
   * @return A list of users having pFirstName as/withing their
   * first name.
   */
  public abstract List<UserData> getUsersByFirstName(String pFirstName, int pStart, int pCnt);

  /**
   * Get the user count for the provided first name.
   *
   * @param pFirstName The first name of a user or a part of the first name
   *
   * @return The number of users with pFirstName.
   */
  public abstract long getUsersCountByFirstName(String pFirstName);
}
