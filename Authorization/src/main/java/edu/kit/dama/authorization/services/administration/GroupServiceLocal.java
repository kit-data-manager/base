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
package edu.kit.dama.authorization.services.administration;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.IRoleRestriction;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.EntityAlreadyExistsException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.impl.GroupServiceImpl;
import java.util.List;

/**
 *
 * @author pasic
 */
public final class GroupServiceLocal implements IGroupService {

    private static final GroupServiceLocal SINGLETON = new GroupServiceLocal();
    private final IGroupService groupService;

    /**
     * Hidden constructor.
     */
    private GroupServiceLocal() {
        groupService = new GroupServiceImpl();
    }

    /**
     * Get the singleton instance.
     *
     * @return The singleton instance.
     */
    public static GroupServiceLocal getSingleton() {
        return SINGLETON;
    }

    @Override
    public void create(GroupId newGroup, UserId groupManager, IAuthorizationContext authCtx) throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        groupService.create(newGroup, groupManager, authCtx);
    }

    @Override
    public boolean remove(GroupId groupId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        return groupService.remove(groupId, authCtx);
    }

    @Override
    public void addUser(GroupId groupId, UserId userId, Role role, IAuthorizationContext authCtx) throws EntityNotFoundException, EntityAlreadyExistsException, UnauthorizedAccessAttemptException {
        groupService.addUser(groupId, userId, role, authCtx);
    }

    @Override
    public void removeUser(GroupId groupId, UserId userId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        groupService.removeUser(groupId, userId, authCtx);
    }

    @Override
    public IRoleRestriction getMaximumRole(GroupId groupId, UserId userId, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        return groupService.getMaximumRole(groupId, userId, authCtx);
    }

    @Override
    public Role changeRole(GroupId groupId, UserId userId, Role newRole, IAuthorizationContext authCtx) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        return groupService.changeRole(groupId, userId, newRole, authCtx);
    }

    @Override
    public List<GroupId> getAllGroupsIds(int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
        return groupService.getAllGroupsIds(first, results, authCtx);
    }

    @Override
    public List<UserId> getUsersIds(GroupId groupId, int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
        return groupService.getUsersIds(groupId, first, results, authCtx);
    }

    @Override
    public List<UserId> getGroupManagers(GroupId groupId, int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
        return groupService.getGroupManagers(groupId, first, results, authCtx);
    }

    @Override
    public List<GroupId> membershipsOf(UserId userId, int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
        return groupService.membershipsOf(userId, first, results, authCtx);
    }

    @Override
    public List<GroupId> membershipsOf(UserId userId, Role minimumRole, int first, int results, IAuthorizationContext authCtx) throws UnauthorizedAccessAttemptException {
        return groupService.membershipsOf(userId, minimumRole, first, results, authCtx);
    }
}
