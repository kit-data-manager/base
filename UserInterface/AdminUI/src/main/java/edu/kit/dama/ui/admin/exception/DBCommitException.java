/* 
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.ui.admin.exception;

/**
 *
 * @author dx6468
 */
public class DBCommitException extends Exception {

  /**
   * Default constructor.
   *
   * @param message The message.
   */
  public DBCommitException(String message) {
    super(message);
  }

  /**
   * Default constructor.
   *
   * @param message The message.
   * @param cause The cause.
   */
  public DBCommitException(String message, Throwable cause) {
    super(message, cause);
  }
}
