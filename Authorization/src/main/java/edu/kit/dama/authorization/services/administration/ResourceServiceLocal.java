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
package edu.kit.dama.authorization.services.administration;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.ReferenceId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.Grant;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.impl.ResourceServiceImpl;
import java.util.List;

/**
 *
 * @author pasic
 */
public final class ResourceServiceLocal implements IResourceService {

  private static final ResourceServiceLocal SINGLETON = new ResourceServiceLocal();
  private final IResourceService resourceService;

  /**
   * Hidden constructor.
   */
  private ResourceServiceLocal() {
    resourceService = new ResourceServiceImpl();
  }

  /**
   * Get the singleton instance.
   *
   * @return The singleton instance.
   */
  public static ResourceServiceLocal getSingleton() {
    return SINGLETON;
  }

  @Override
  public void registerResource(SecurableResourceId resourceId, IAuthorizationContext authCtx) throws EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    resourceService.registerResource(resourceId, authCtx);
  }

  @Override
  public void registerResource(SecurableResourceId resourceId, GroupId owner, Role role, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.registerResource(resourceId, owner, role, authCtx);
  }

  @Override
  public void registerResource(SecurableResourceId resourceId, Role grantRole, UserId userId, Role grantSetRole, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.registerResource(resourceId, grantRole, userId, grantSetRole, authCtx);
  }

  @Override
  public void remove(SecurableResourceId resourceId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.remove(resourceId, authCtx);
  }

  @Override
  public void createReference(ReferenceId referenceId, Role roleRestriction, IAuthorizationContext authCtx) throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    resourceService.createReference(referenceId, roleRestriction, authCtx);
  }

  @Override
  public void deleteReference(ReferenceId referenceId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.deleteReference(referenceId, authCtx);
  }

  @Override
  public void changeReferenceRestriction(ReferenceId referenceId, Role newRole, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.changeReferenceRestriction(referenceId, newRole, authCtx);
  }

  @Override
  public IRoleRestriction getReferenceRestriction(ReferenceId referenceId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return resourceService.getReferenceRestriction(referenceId, authCtx);
  }

  @Override
  public List<ReferenceId> getReferences(SecurableResourceId resourceId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return resourceService.getReferences(resourceId, authCtx);
  }

  @Override
  public void allowGrants(SecurableResourceId resourceId, Role restriction, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.allowGrants(resourceId, restriction, authCtx);
  }

  @Override
  public boolean grantsAllowed(SecurableResourceId resourceId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return resourceService.grantsAllowed(resourceId, authCtx);
  }

  @Override
  public Role getGrantsRestriction(SecurableResourceId resourceId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return resourceService.getGrantsRestriction(resourceId, authCtx);
  }

  @Override
  public void changeGrantsRestriction(SecurableResourceId resourceId, Role restriction, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.changeGrantsRestriction(resourceId, restriction, authCtx);
  }

  @Override
  public void addGrant(SecurableResourceId resourceId, UserId userId, Role role, IAuthorizationContext authCtx) throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
    resourceService.addGrant(resourceId, userId, role, authCtx);
  }

  @Override
  public void changeGrant(SecurableResourceId resourceId, UserId userId, Role newRole, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.changeGrant(resourceId, userId, newRole, authCtx);
  }

  @Override
  public void revokeGrant(SecurableResourceId resourceId, UserId userId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.revokeGrant(resourceId, userId, authCtx);
  }

  @Override
  public void revokeAllAndDisallowGrants(SecurableResourceId resourceId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    resourceService.revokeAllAndDisallowGrants(resourceId, authCtx);
  }

  @Override
  public Role getGrantRole(SecurableResourceId resourceId, UserId userId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return resourceService.getGrantRole(resourceId, userId, authCtx);
  }

  @Override
  public List<Grant> getGrants(SecurableResourceId resourceId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return resourceService.getGrants(resourceId, authCtx);
  }

  @Override
  public List<UserId> getAuthorizedUsers(SecurableResourceId resourceId, Role minimumRole, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return resourceService.getAuthorizedUsers(resourceId, minimumRole, authCtx);
  }

  @Override
  public List<GroupId> getAuthorizedGroups(SecurableResourceId resourceId, Role role, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return resourceService.getAuthorizedGroups(resourceId, role, authCtx);
  }
}
