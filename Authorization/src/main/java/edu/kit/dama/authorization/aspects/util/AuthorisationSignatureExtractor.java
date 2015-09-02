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
package edu.kit.dama.authorization.aspects.util;

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredArgument;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pasic
 */
public class AuthorisationSignatureExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorisationSignatureExtractor.class);

  /**
   * Extract the authentication signature.
   *
   * @param jp The AspectJ JointPoint.
   *
   * @return The extracted AuthSignature information.
   */
  protected final AuthSignature extractAuthSignature(final JoinPoint jp) {
    AuthSignature result = new AuthSignature();
    Object[] args = jp.getArgs();
    Signature signature = jp.getStaticPart().getSignature();
    LOGGER.debug("Looking for method signature.");
    if (signature instanceof MethodSignature) {
      LOGGER.debug("Checking signature of method {}#{}", signature.getDeclaringTypeName(), signature.getName());
      MethodSignature methodSignature = (MethodSignature) signature;
      Method method = methodSignature.getMethod();
      Class[] parametersTypes = method.getParameterTypes();
      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      LOGGER.debug("Checking {} parameter annotations.", parameterAnnotations.length);
      for (int i = 0; i < parameterAnnotations.length; i++) {
        Annotation[] annotations = parameterAnnotations[i];
        LOGGER.debug("Checking {} annotations for parameter {} of type {}.", annotations.length, i, parametersTypes[i]);
        for (Annotation annotation : annotations) {
          LOGGER.debug("Checking argument annotation {} of type {}", annotation, annotation.annotationType());
          if (Context.class.isAssignableFrom(annotation.annotationType())) {
            LOGGER.debug("Argument with @Context annotation found");
            if (null == args[i]) {
              throw new IllegalArgumentException("Argument annotated with @Context must not be 'null'. Argument skipped.");
            } else if (!(args[i] instanceof IAuthorizationContext)) {
              throw new IllegalArgumentException("Argument annotated with @Context does not implement IAuthorizationContext. Argument skipped.");
            } else {
              LOGGER.debug("Setting provided argument {} as authorization context.", args[i]);
              result.setAuthContext((IAuthorizationContext) args[i]);
            }
          } else if (SecuredArgument.class.isAssignableFrom(annotation.annotationType())) {
            LOGGER.debug("Argument with @SecuredArgument annotation found");
            if (null == args[i]) {
              //Just ignore argument
              LOGGER.debug("Argument {} is 'null' and therefore ignored.", i);
            } else if (args[i] instanceof SecurableResourceId) {
              LOGGER.debug("Adding SecurableResourceId {}.", args[i]);
              result.addSecurableResourceId((SecurableResourceId) args[i]);
            } else if (args[i] instanceof ISecurableResource) {
              LOGGER.debug("Adding SecurableResource {}.", args[i]);
              result.addSecurableResource((ISecurableResource) args[i]);
            } else {
              throw new IllegalArgumentException("Argument annotated with @SecuredArgument does not implement ISecurableResourceId or ISecurableResource. Argument ignored.");
            }
          } else {
            LOGGER.debug("Annotation with unknown type {} found. Ignoring it.", annotation.annotationType());
          }
        }
        LOGGER.debug("Checks for parameter {} of type {} finished.", i, parametersTypes[i]);
      }
    } else {
      LOGGER.info("Provided signature is no MethodSignature");
    }

    if (!result.isValid()) {
      LOGGER.warn("No valid auth signature extracted.");
    }
    return result;
  }
}
