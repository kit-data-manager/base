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
package edu.kit.dama.ui.components;

/**
 * This interface collects all methods needed for designing an customized
 * <b>ConfirmationWindow</b>.
 *
 * @author rindone
 */
public interface IConfirmationWindowListener7 {

  /**
   * Fires an event after having got the user's decision as boolean value
   * <b>confirmed</b>. The boolean value <b>confirmed</b> is set after clicking
   * one of both buttons (yes/no), placed on the
   * <b>ConfirmationWindow</b>:<ul><li>confirmed == true == "<b>yesButton</b>
   * was clicked by user"</li><li>confirmed == false == "<b>noButton</b> was
   * clicked by user"</li></ul>
   *
   * @see ConfirmationWindow
   *
   * @param pResult The result.
   */
  public void fireConfirmationWindowCloseEvent(ConfirmationWindow7.RESULT pResult);

}
