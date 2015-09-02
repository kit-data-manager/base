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
package edu.kit.dama.mdm.dataorganization.service.exception;

/**
 *
 * @author pasic
 */
public class DataOrganizationError extends Error {

  /**
   * Default constructor.
   */
  public DataOrganizationError() {
  }

  /**
   * Constructor using a provided message.
   *
   * @param message The message.
   */
  public DataOrganizationError(String message) {
    super(message);
  }

  /**
   * Constructor using a provided throwable.
   *
   * @param cause The cause.
   */
  public DataOrganizationError(Throwable cause) {
    super(cause);
  }

  /**
   * Constructor using a provided message and throwable.
   *
   * @param message The message.
   * @param cause The cause.
   */
  public DataOrganizationError(String message, Throwable cause) {
    super(message, cause);
  }
}
