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
package edu.kit.dama.authorization.aspects;

import edu.kit.dama.authorization.annotations.FilterOutput;
import edu.kit.dama.authorization.aspects.util.AuthSignature;
import edu.kit.dama.authorization.aspects.util.AuthorisationSignatureExtractor;
import edu.kit.dama.authorization.services.base.PlainAuthorizerLocal;
import java.util.Collection;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 *
 * @author mf6319
 */
@Aspect
public class FilterAspect extends AuthorisationSignatureExtractor// extends ContextAspect 
{

  /**
   * Filter an operation.
   *
   * @param filter The filter.
   */
  @Pointcut("execution(@edu.kit.dama.authorization.annotations.FilterOutput * *.*(..))  && @annotation(filter)")
  public void operation(final FilterOutput filter) {

  }

  /**
   * The return handler of the filter.
   *
   * @param filter The filter.
   * @param jp The JointPoint.
   * @param retObj The returned object.
   */
  @AfterReturning(pointcut = "operation(filter)", returning = "retObj")
  public void filterReturn(FilterOutput filter, JoinPoint jp, Object retObj) {
    AuthSignature signature = extractAuthSignature(jp);
    PlainAuthorizerLocal.filterOnAccessAllowed(signature.getAuthContext(), filter.roleRequired(), (Collection) retObj);
  }

}
