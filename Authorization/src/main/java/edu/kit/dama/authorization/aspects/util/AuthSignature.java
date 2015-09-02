/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.authorization.aspects.util;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class AuthSignature {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthSignature.class);

  private List<SecurableResourceId> resourceIds = new ArrayList<SecurableResourceId>();
  private IAuthorizationContext authContext = null;

  /**
   * Default constructor.
   */
  public AuthSignature() {
  }

  public void setAuthContext(IAuthorizationContext context) {
    if (authContext == null) {
      authContext = context;
    } else {
      LOGGER.info("Auth context already set to {}. Changing context to current/newer argument {}.", authContext, context);
    }
  }

  public IAuthorizationContext getAuthContext() {
    return authContext;
  }

  public void addSecurableResource(ISecurableResource pResource) {
    resourceIds.add(pResource.getSecurableResourceId());
  }

  public void addSecurableResourceId(SecurableResourceId pResourceId) {
    resourceIds.add(pResourceId);
  }

  public List<SecurableResourceId> getSecurableResourceIds() {
    return resourceIds;
  }

  public boolean hasSecurableResourceIds() {
    return !resourceIds.isEmpty();
  }

  public boolean isValid() {
    return authContext != null;
  }

}
