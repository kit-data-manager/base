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

import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.aspects.util.AuthSignature;
import edu.kit.dama.authorization.aspects.util.AuthorisationSignatureExtractor;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.base.PlainAuthorizerLocal;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareError;
import org.aspectj.lang.annotation.Pointcut;

/**
 *
 * @author pasic
 */
@Aspect
public class AuthorizeAccess extends AuthorisationSignatureExtractor {

    /**
     * Operation definition for secured methods.
     *
     * @param method The secured method.
     */
    @Pointcut("execution(@edu.kit.dama.authorization.annotations.SecuredMethod * *.*(..) throws edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException) && @annotation(method)")
    public void operation(final SecuredMethod method) {
    }

    /**
     * Error definition.
     *
     */
    @DeclareError("withincode(@edu.kit.dama.authorization.annotations.SecuredMethod * *.*(..) throws !edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException)")
    public static final String CONTRACT_ERROR = "All methods annotated with @SecuredMethod need to throw edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException.";

    /**
     * Authorize operation for secured methods.
     *
     * @param method The method.
     * @param joinPoint The joint point.
     *
     * @throws UnauthorizedAccessAttemptException If the access is not
     * authorized.
     * @throws EntityNotFoundException If there are problems with the
     * authorization context provided by the method.
     */
    @Before("operation(method)")
    public void authorizeOperation(final SecuredMethod method, JoinPoint joinPoint) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        AuthSignature signature = extractAuthSignature(joinPoint);
        if (null == signature.getAuthContext()) {
            throw new UnauthorizedAccessAttemptException("No @Context annotation provided. Authorization not possible.");
        }
        if (!signature.hasSecurableResourceIds()) {
            PlainAuthorizerLocal.authorize(signature.getAuthContext(), method.roleRequired());
        } else {
            PlainAuthorizerLocal.authorize(signature.getAuthContext(), signature.getSecurableResourceIds(), method.roleRequired());
        }
    }

}
