/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.authorization.exceptions;

/**
 * Indicates an unauthorized access attempt.
 *
 * @author pasic
 */
public class UnauthorizedAccessAttemptException extends AuthorizationException {

  /**
   * Default constructor.
   */
  public UnauthorizedAccessAttemptException() {
  }

  /**
   * Default constructor.
   *
   * @param message The message.
   */
  public UnauthorizedAccessAttemptException(String message) {
    super(message);
  }

  /**
   * Default constructor.
   *
   * @param cause The cause.
   */
  public UnauthorizedAccessAttemptException(Throwable cause) {
    super(cause);
  }

  /**
   * Default constructor.
   *
   * @param message The message.
   * @param cause The cause.
   */
  public UnauthorizedAccessAttemptException(String message, Throwable cause) {
    super(message, cause);
  }
}
