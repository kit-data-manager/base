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
package edu.kit.dama.authorization.exceptions;

/**
 *
 * @author jejkal
 */
public class SecurableResourceAnnotationException extends RuntimeException {

  /**
   * Default constructor.
   */
  public SecurableResourceAnnotationException() {
  }

  /**
   * Default constructor.
   *
   * @param message The message
   */
  public SecurableResourceAnnotationException(String message) {
    super(message);
  }

  /**
   * Default constructor.
   *
   * @param cause The cause
   */
  public SecurableResourceAnnotationException(Throwable cause) {
    super(cause);
  }

  /**
   * Default constructor.
   *
   * @param message The message
   * @param cause The cause
   */
  public SecurableResourceAnnotationException(String message, Throwable cause) {
    super(message, cause);
  }
}
