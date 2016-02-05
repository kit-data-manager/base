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
package edu.kit.dama.authorization.annotations;

import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.services.base.IAuthorizationService;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Is applicable on methods of arbitrary signature with following restrictions:
 * <ul>
 * <li>
 * The method must have an argument of an subclass of IAuthorizationContext
 * annotated with {@link Context}
 * </li>
 * <li>
 * The method must throw UnauthorizedAccessAttemptException.
 * </li>
 * </ul>
 * 
 * All methods annotated with SecuredMethod are protected by this authorization
 * module. This means before starting to execute the method body the
 * authorization service specified by the service (see
 * {@link IAuthorizationService}) property will perform an authorization check
 * and throw an UnauthorizedAccessAttemptException if the authorization is
 * unsuccessful (otherwise the execution of the body will start).
 * 
 * The connection between the annotated method and its arguments and the
 * authorization service is the following.
 * <ul>
 * <li>
 * If there are any arguments annotated with {@link 
 * SecuredArgument} the
 * corresponding {@link SecurableResourceId} will be passed to the authorize
 * call as a member of the protectedArguments list.
 * </li>
 * <li>
 * If there no arguments annotated with {@link SecuredArgument} the one argument
 * version of authorize will be called.
 * </li>
 * <li>
 * The parameter annotated with {@link Context} will be passed to the authorize
 * the security context.
 * </li>
 * </ul> 
 * @author pasic
 * @see SecuredArgument
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface SecuredMethod {

  /**
   * The role required by the user of the annotation to execute the body of the
   * annotated method.
   * 
   * @return The reqired role for this method.
   */
  Role roleRequired() default edu.kit.dama.authorization.entities.Role.NO_ACCESS;

  /**
   * The service implementation (strategy) which will perform the authorization.
   * 
   * @return The used implementation of IAuthorizationService.
   */
  Class<? extends IAuthorizationService> service() default edu.kit.dama.authorization.services.base.impl.PlainAuthorizerImpl.class;
}
