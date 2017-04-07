/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.mdm.dataworkflow.tools;

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.tools.AbstractSecureQueryHelper;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import java.util.List;
import javax.persistence.EntityNotFoundException;

/**
 *
 * @author mf6319
 */
public class DataWorkflowTaskSecureQueryHelper extends AbstractSecureQueryHelper<DataWorkflowTask> {

    @SecuredMethod(roleRequired = Role.GUEST)
    public Number getTaskCount(String pDefailedQuery, IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        return getReadableResourceCount(pMetaDataManager,
                pDefailedQuery,
                pContext);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    public List<DataWorkflowTask> getAllTasks(String pDefailedQuery, IMetaDataManager pMetaDataManager, Integer pFirst, Integer pResults, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        return getReadableResources(pMetaDataManager, pDefailedQuery, ORDER.ASC, pFirst, pResults, pContext);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    public DataWorkflowTask getDataWorkflowTaskById(Long pId, IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
        List<DataWorkflowTask> result = getReadableResources(pMetaDataManager, "o.id='" + Long.toString(pId) + "'", ORDER.ASC, 0, 1, pContext);
        if (result.isEmpty()) {
            throw new EntityNotFoundException("No entity found/accessible with id " + pId);
        } else {
            return result.get(0);
        }
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    public List<DataWorkflowTask> getDataWorkflowTaskByStatus(DataWorkflowTask.TASK_STATUS pStatus, IMetaDataManager pMetaDataManager, int pFirst, int pResults, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        return getReadableResources(pMetaDataManager, "o.status='" + pStatus.toString() + "'", ORDER.ASC, pFirst, pResults, pContext);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    public List<DataWorkflowTask> getDataWorkflowTaskByDigitalObjectId(DigitalObjectId pId, IMetaDataManager pMetaDataManager, int pFirst, int pResults, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        return getReadableResources(pMetaDataManager, "o.objectViewMap LIKE '%" + pId.toString() + "%'", ORDER.ASC, pFirst, pResults, pContext);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    public DataWorkflowTask getDataWorkflowTaskByJobId(String pJobId, IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        List<DataWorkflowTask> result = getReadableResources(pMetaDataManager, "o.jobId='" + pJobId + "'", ORDER.ASC, 0, 1, pContext);
        if (result.isEmpty()) {
            throw new EntityNotFoundException("No entity found/accessible with job id " + pJobId);
        } else {
            return result.get(0);
        }
    }
}
