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

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.scheduler.SchedulerManagement;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.AtTrigger;
import edu.kit.dama.scheduler.api.trigger.DelayTrigger;
import edu.kit.dama.scheduler.api.trigger.ExpressionTrigger;
import edu.kit.dama.scheduler.api.trigger.IntervalTrigger;
import edu.kit.dama.scheduler.api.trigger.NowTrigger;
import edu.kit.dama.scheduler.api.trigger.JobTrigger;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author wq7203
 */
public final class SecureSchedulerManager implements ISchedulerManager {

    /**
     * Factory a new instance of a SecureSchedulerManager using the provided
     * context.
     *
     * @param ctx The context.
     *
     * @return The metadata manager.
     */
    public static ISchedulerManager factorySecureSchedulerManager(IAuthorizationContext ctx) {
        ISchedulerManager schedulerManager = SchedulerManagement.getSchedulerManagement().getSchedulerManager();
        schedulerManager.setAuthorizationContext(ctx);

        return schedulerManager;
    }

    // private static final Logger LOGGER = LoggerFactory.getLogger(SecureSchedulerManager.class);
    /**
     * Authorization Context for secure Access.
     */
    private IAuthorizationContext authCtx = null;

    /**
     * Implementation of SchedulerManager.
     */
    private ISchedulerManager impl = null;

    /**
     * Private default constructor.
     *
     * @param implementation The scheduler manager implementation.
     */
    public SecureSchedulerManager(ISchedulerManager implementation) {
        this.impl = Objects.requireNonNull(implementation, "Argument implementation can't be null.");
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    List<SimpleSchedule> getAllSchedules(@Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.getAllSchedules();
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    SimpleSchedule getScheduleById(String id, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.getScheduleById(id);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    List<JobTrigger> getAllTriggers(@Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.getAllTriggers();
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    JobTrigger getTriggerById(String id, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.getTriggerById(id);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    List<JobTrigger> getTriggersByScheduleId(String id, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.getTriggersByScheduleId(id);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    List<SimpleSchedule> getCurrentlyExecutingJobs(@Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.getCurrentlyExecutingJobs();
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    SimpleSchedule addSchedule(SimpleSchedule schedule, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.addSchedule(schedule);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    JobTrigger addTrigger(String jobId, JobTrigger trigger, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.addTrigger(jobId, trigger);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    boolean removeSchedule(String id, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.removeSchedule(id);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    boolean removeTrigger(String id, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.removeTrigger(id);
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    SimpleSchedule SCHEDULE(@Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.createSchedule();
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    NowTrigger NOW(@Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.createNowTrigger();
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    AtTrigger AT(@Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.createAtTrigger();
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    IntervalTrigger INTERVAL(@Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.createIntervalTrigger();
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    DelayTrigger DELAY(@Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.createDelayTrigger();
    }

    @SecuredMethod(roleRequired = Role.GUEST)
    ExpressionTrigger EXPRESSION(@Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
        return this.impl.createExpressionTrigger();
    }

    @Override
    public void setAuthorizationContext(IAuthorizationContext authorizationContext) {
        authCtx = authorizationContext;
    }

    @Override
    public List<SimpleSchedule> getAllSchedules() throws UnauthorizedAccessAttemptException {
        return this.getAllSchedules(authCtx);
    }

    @Override
    public SimpleSchedule getScheduleById(String id) throws UnauthorizedAccessAttemptException {
        return this.getScheduleById(id, authCtx);
    }

    @Override
    public List<JobTrigger> getAllTriggers() throws UnauthorizedAccessAttemptException {
        return this.getAllTriggers(authCtx);
    }

    @Override
    public JobTrigger getTriggerById(String id) throws UnauthorizedAccessAttemptException {
        return this.getTriggerById(id, authCtx);
    }

    @Override
    public List<JobTrigger> getTriggersByScheduleId(String id) throws UnauthorizedAccessAttemptException {
        return this.getTriggersByScheduleId(id, authCtx);
    }

    @Override
    public List<SimpleSchedule> getCurrentlyExecutingJobs() throws UnauthorizedAccessAttemptException {
        return this.getCurrentlyExecutingJobs(authCtx);
    }

    @Override
    public SimpleSchedule addSchedule(SimpleSchedule schedule) throws UnauthorizedAccessAttemptException {
        return this.addSchedule(schedule, authCtx);
    }

    @Override
    public JobTrigger addTrigger(String jobId, JobTrigger trigger) throws UnauthorizedAccessAttemptException {
        return this.addTrigger(jobId, trigger, authCtx);
    }

    @Override
    public boolean removeSchedule(String id) throws UnauthorizedAccessAttemptException {
        return this.removeSchedule(id, authCtx);
    }

    @Override
    public boolean removeTrigger(String id) throws UnauthorizedAccessAttemptException {
        return this.removeTrigger(id, authCtx);
    }

    @Override
    public SimpleSchedule createSchedule() throws UnauthorizedAccessAttemptException {
        return this.SCHEDULE(authCtx);
    }

    @Override
    public NowTrigger createNowTrigger() throws UnauthorizedAccessAttemptException {
        return this.NOW(authCtx);
    }

    @Override
    public AtTrigger createAtTrigger() throws UnauthorizedAccessAttemptException {
        return this.AT(authCtx);
    }

    @Override
    public IntervalTrigger createIntervalTrigger() throws UnauthorizedAccessAttemptException {
        return this.INTERVAL(authCtx);
    }

    @Override
    public DelayTrigger createDelayTrigger() throws UnauthorizedAccessAttemptException {
        return this.DELAY(authCtx);
    }

    @Override
    public ExpressionTrigger createExpressionTrigger() throws UnauthorizedAccessAttemptException {
        return this.EXPRESSION(authCtx);
    }

}
