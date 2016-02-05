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
package edu.kit.dama.dataworkflow.impl;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.tools.DataWorkflowTaskSecureQueryHelper;
import java.util.Date;
import java.util.List;
import javax.persistence.EntityNotFoundException;

/**
 *
 * @author jejkal
 */
public class DataWorkflowPersistenceImpl {

  public static final String DEFAULT_PU = "MDM-Core";
  private static DataWorkflowPersistenceImpl SINGLETON = null;
  private IMetaDataManager mdm = null;
  private IAuthorizationContext context = null;
  private final DataWorkflowTaskSecureQueryHelper queryHelper = new DataWorkflowTaskSecureQueryHelper();

  /**
   * Get the singleton instance of this persistence implementation. The provided
   * context is used to filter the access to available tasks.
   *
   * Attention: Due to the type of the internal queries, the provided context
   * must be linked to a concrete user and group. It is <b>not possible</b> to
   * use a system user context.
   *
   * @param pCtx The authorization context used to query for results.
   *
   * @return The DataWorkflowPersistenceImpl singleton instance.
   */
  public static synchronized DataWorkflowPersistenceImpl getSingleton(IAuthorizationContext pCtx) {
    if (SINGLETON == null) {
      //just use default PU for the moment...do it more flexible in the future if required
      SINGLETON = new DataWorkflowPersistenceImpl(DEFAULT_PU);
    }
    SINGLETON.setAuthorizationContext(pCtx);
    return SINGLETON;
  }

  /**
   * Default constructor.
   *
   * @param pPersistenceUnit the PersistenceUnit (default: MDM-Core)
   */
  DataWorkflowPersistenceImpl(String pPersistenceUnit) {
    mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(pPersistenceUnit);
  }

  /**
   * Set the authorization context and store it for later queries.
   *
   * @param pCtx The AuthorizationContext to use.
   */
  private void setAuthorizationContext(IAuthorizationContext pCtx) {
    mdm.setAuthorizationContext(pCtx);
    context = pCtx;
  }

  /**
   * Returns a list of all accessible DataWorkflowTasks.
   *
   * @param pFirst The first index which will be returned.
   * @param pResults The number of results that will be returned.
   *
   * @return A list of accessible tasks.
   *
   * @throws UnauthorizedAccessAttemptException If access to tasks is not
   * allowed.
   */
  public List<DataWorkflowTask> getAllTasks(int pFirst, int pResults) throws UnauthorizedAccessAttemptException {
    return queryHelper.getReadableResources(mdm, pFirst, pResults, context);
  }

  /**
   * Returns a DataWorkflowTask by its Id.
   *
   * @param pId The id of the task.
   *
   * @return The task with the id pId.
   *
   * @throws UnauthorizedAccessAttemptException If access to the task with the
   * provided id is no allowed.
   * @throws EntityNotFoundException If no entity was found for the provided id.
   */
  public DataWorkflowTask getTaskById(Long pId) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return queryHelper.getDataWorkflowTaskById(pId, mdm, context);
  }

  /**
   * Returns a list of DataWorkflowTasks with the provided status.
   *
   * @param pStatus The status to search for.
   * @param pFirst The first index which will be returned.
   * @param pResults The number of results that will be returned.
   *
   * @return The task with the id pId.
   *
   * @throws UnauthorizedAccessAttemptException If access to the task with the
   * provided id is not allowed.
   */
  public List<DataWorkflowTask> getTaskByStatus(DataWorkflowTask.TASK_STATUS pStatus, int pFirst, int pResults) throws UnauthorizedAccessAttemptException {
    return queryHelper.getDataWorkflowTaskByStatus(pStatus, mdm, pFirst, pResults, context);
  }

  /**
   * Returns a list of DataWorkflowTasks associated with the provided object id.
   *
   * @param pDigitalObjectId The object id.
   * @param pFirst The first index which will be returned.
   * @param pResults The number of results that will be returned.
   *
   * @return The task with the id pId.
   *
   * @throws UnauthorizedAccessAttemptException If access to the task with the
   * provided object id is not allowed.
   */
  public List<DataWorkflowTask> getTaskByDigitalObjectId(DigitalObjectId pDigitalObjectId, int pFirst, int pResults) throws UnauthorizedAccessAttemptException {
    return queryHelper.getDataWorkflowTaskByDigitalObjectId(pDigitalObjectId, mdm, pFirst, pResults, context);
  }

  /**
   * Returns a task by its job id. The job id depends on the underlaying
   * processing infrastructure and is intended to be used for monitoring.
   *
   * @param pJobId The internal job id.
   *
   * @return The task with the job id pJobId.
   *
   * @throws UnauthorizedAccessAttemptException If access to the task with the
   * provided id is no allowed.
   * @throws EntityNotFoundException If no task was found for the provided job
   * id.
   */
  public DataWorkflowTask getTaskByJobId(String pJobId) throws EntityNotFoundException, UnauthorizedAccessAttemptException {
    return queryHelper.getDataWorkflowTaskByJobId(pJobId, mdm, context);
  }

  /**
   * Update the provided task. The task should have been persisted before. The
   * provided task will be updated and the last modification date is set to NOW.
   * Finally, the update task will be returned.
   *
   * @param pTask The task to update.
   *
   * @return The updated task.
   *
   * @throws UnauthorizedAccessAttemptException If the access to the provided
   * task is not allowed.
   */
  public DataWorkflowTask updateTask(DataWorkflowTask pTask) throws UnauthorizedAccessAttemptException {
    pTask.setLastUpdate(new Date());
    return mdm.save(pTask);
  }
}
