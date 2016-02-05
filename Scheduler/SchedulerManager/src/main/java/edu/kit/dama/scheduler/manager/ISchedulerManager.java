/*
 * Copyright 2015 Karlsruhe Institute of Technology.
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
package edu.kit.dama.scheduler.manager;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.AtTrigger;
import edu.kit.dama.scheduler.api.trigger.DelayTrigger;
import edu.kit.dama.scheduler.api.trigger.ExpressionTrigger;
import edu.kit.dama.scheduler.api.trigger.IntervalTrigger;
import edu.kit.dama.scheduler.api.trigger.NowTrigger;
import edu.kit.dama.scheduler.api.trigger.JobTrigger;
import java.util.List;

/**
 *
 * @author wq7203
 */
public interface ISchedulerManager {

    /**
     * Set authorization context which is used to authorize secured access and
     * filtering of secured resources. When using security, for each method of
     * this interface a secured version has to be implemented.
     *
     * Example remove():
     *
     * <pre>
     * &#64;Override
     * public &lt;T&gt; void remove(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
     *  if (entity instanceof ISecurableResource) {
     *    //use secured implementation
     *    removeSecured(entity, authCtx);
     *  } else {
     *     //use insecure implementation
     *     remove(entity);
     *  }
     * }
     *
     * &#64;SecuredMethod(roleRequired = Role.MANAGER)
     * private &lt;T&gt; void removeSecured(&#64;SecuredArgument T entity, &#64;Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
     *  //method will only be entered if authorizationContext satisfies Role.MANAGER
     *  ISecurableResource securableResource = (ISecurableResource) entity;
     *  //perform access checks and remove entity if allowed, throw UnauthorizedAccessAttemptException if not allowed
     *  remove(entity);
     * }
     * </pre>
     *
     * In this example, <i>authCtx</i> was set before and is used when an
     * operation is performed. The authorization context may change between
     * different calls, e.g. for temporary using system permissions.
     *
     * @param authorizationContext authorization context.
     */
    void setAuthorizationContext(IAuthorizationContext authorizationContext);

    /**
     * Get all schedules currently registered at the manager instance.
     *
     * @return A list of currently registered schedules.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to access the list of schedules.
     */
    List<SimpleSchedule> getAllSchedules() throws UnauthorizedAccessAttemptException;

    /**
     * Get a single schedule by its id.
     *
     * @param id The schedule id.
     *
     * @return A single schedule or null of no schedule exists for the id.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to access the schedule.
     */
    SimpleSchedule getScheduleById(String id) throws UnauthorizedAccessAttemptException;

    /**
     * Get all triggers currently registered at the manager instance.
     *
     * @return A list of currently registered triggers.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to access the list of triggers.
     */
    List<JobTrigger> getAllTriggers() throws UnauthorizedAccessAttemptException;

    /**
     * Get a single trigger by its id.
     *
     * @param id The trigger id.
     *
     * @return A single trigger or null of no trigger exists for the id.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to access the trigger.
     */
    JobTrigger getTriggerById(String id) throws UnauthorizedAccessAttemptException;

    /**
     * Get all triggers currently associated with the schedule with the provided
     * id.
     *
     * @param id The schedule id the triggers are associated with.
     *
     * @return A list of triggers assigned to the schedule with the provided id
     * or an empty list if no trigger is assigned or the schedule does not
     * exist.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to access the list of triggers.
     */
    List<JobTrigger> getTriggersByScheduleId(String id) throws UnauthorizedAccessAttemptException;

    /**
     * Get a list of jobs that are currently executed.
     *
     * @return A list of currently executed jobs.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to access the list of currently executed
     * jobs.
     */
    List<SimpleSchedule> getCurrentlyExecutingJobs() throws UnauthorizedAccessAttemptException;

    /**
     * Add a new schedule to the manager.
     *
     * @param schedule The schedule to add.
     *
     * @return The created schedule with an identifier assigned for later
     * queries.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to access the list of currently executed
     * jobs.
     */
    SimpleSchedule addSchedule(SimpleSchedule schedule) throws UnauthorizedAccessAttemptException;

    /**
     * Add a new trigger to the schedule with the provided id. scheduleId and
     * trigger must be valid inputs. If one of them is not valid, a
     * RuntimeException may be thrown by the implementing class. This method
     * should only return if a trigger has been successfully assigned.
     *
     * @param scheduleId The id of the schedule.
     * @param trigger The trigger that should be assigned to the schedule.
     *
     * @return The assigned trigger.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to add a trigger to the schedule.
     */
    JobTrigger addTrigger(String scheduleId, JobTrigger trigger) throws UnauthorizedAccessAttemptException;

    /**
     * Remove the schedule with the provided id.
     *
     * @param id The id of the schedule to remove.
     *
     * @return TRUE if the schedule was found and has been removed, FALSE
     * otherwise.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to remove the schedule.
     */
    boolean removeSchedule(String id) throws UnauthorizedAccessAttemptException;

    /**
     * Remove the trigger with the provided id.
     *
     * @param id The id of the trigger to remove.
     *
     * @return TRUE if the trigger was found and has been removed, FALSE
     * otherwise.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to remove the trigger.
     */
    boolean removeTrigger(String id) throws UnauthorizedAccessAttemptException;

    /**
     * Create a new schedule.
     *
     * @return The new schedule.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to create a schedule.
     */
    SimpleSchedule createSchedule() throws UnauthorizedAccessAttemptException;

    /**
     * Create a NOW trigger.
     *
     * @return The trigger.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to create a trigger.
     */
    NowTrigger createNowTrigger() throws UnauthorizedAccessAttemptException;

    /**
     * Create an AT trigger.
     *
     * @return The trigger.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to create a trigger.
     */
    AtTrigger createAtTrigger() throws UnauthorizedAccessAttemptException;

    /**
     * Create an INTERVAL trigger.
     *
     * @return The trigger.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to create a trigger.
     */
    IntervalTrigger createIntervalTrigger() throws UnauthorizedAccessAttemptException;

    /**
     * Create a DELAY trigger.
     *
     * @return The trigger.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to create a trigger.
     */
    DelayTrigger createDelayTrigger() throws UnauthorizedAccessAttemptException;

    /**
     * Create an EXPRESSION  trigger.
     *
     * @return The trigger.
     *
     * @throws UnauthorizedAccessAttemptException If the manager is secured and
     * the caller is not authorized to create a trigger.
     */
    ExpressionTrigger createExpressionTrigger() throws UnauthorizedAccessAttemptException;
}
