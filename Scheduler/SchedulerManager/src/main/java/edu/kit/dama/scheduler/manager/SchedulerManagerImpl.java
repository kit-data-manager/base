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
import edu.kit.dama.scheduler.SchedulerManagerException;
import edu.kit.dama.scheduler.api.impl.QuartzAtTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzExpressionTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzIntervalTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzNowTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzSchedule;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.AtTrigger;
import edu.kit.dama.scheduler.api.trigger.DelayTrigger;
import edu.kit.dama.scheduler.api.trigger.ExpressionTrigger;
import edu.kit.dama.scheduler.api.trigger.IntervalTrigger;
import edu.kit.dama.scheduler.api.trigger.NowTrigger;
import edu.kit.dama.scheduler.api.trigger.JobTrigger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wq7203
 */
public final class SchedulerManagerImpl implements ISchedulerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerManagerImpl.class);

    private final Scheduler scheduler;

    public SchedulerManagerImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void setAuthorizationContext(IAuthorizationContext authorizationContext) {
    }

    @Override
    public List<SimpleSchedule> getAllSchedules() {
        List<SimpleSchedule> schedules = new ArrayList<>();
        try {
            for (JobKey jobKey : this.scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
                schedules.add(new QuartzSchedule(this.scheduler.getJobDetail(jobKey)));
            }
        } catch (SchedulerException ex) {
            throw new SchedulerManagerException("Could not get all jobs from scheduler. Internal scheduler exception occurred.", ex);
        }

        return schedules;
    }

    @Override
    public SimpleSchedule getScheduleById(String id) {
        JobDetail detail = this.findJobDetailById(id);
        if (detail == null) {
            return null;
        }

        return new QuartzSchedule(this.findJobDetailById(id));
    }

    @Override
    public List<JobTrigger> getAllTriggers() {
        List<JobTrigger> triggers = new ArrayList<>();

        try {
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup())) {
                org.quartz.Trigger trigger = scheduler.getTrigger(triggerKey);
                if (trigger != null) {
                    JobTrigger wrappedTrigger = this.wrapTrigger(scheduler.getTrigger(triggerKey));
                    if (wrappedTrigger != null) {
                        triggers.add(wrappedTrigger);
                    }
                }
            }
        } catch (SchedulerException ex) {
            throw new SchedulerManagerException("Could not get all triggers from scheduler. Internal scheduler exception occurred.", ex);
        }
        return triggers;
    }

    @Override
    public JobTrigger getTriggerById(String id) {
        org.quartz.Trigger trigger = this.findTriggerById(id);
        if (trigger == null) {
            return null;
        }
        return this.wrapTrigger(trigger);
    }

    @Override
    public List<JobTrigger> getTriggersByScheduleId(String id) {
        List<? extends org.quartz.Trigger> triggers = this.findTriggersBySchedulerId(id);
        List<JobTrigger> retVal = new ArrayList<>();

        if (triggers == null) {
            return retVal;
        }

        for (org.quartz.Trigger trigger : triggers) {
            if (trigger != null) {
                JobTrigger wrappedTrigger = this.wrapTrigger(trigger);
                if (wrappedTrigger != null) {
                    retVal.add(wrappedTrigger);
                }
            }
        }
        return retVal;
    }

    @Override
    public List<SimpleSchedule> getCurrentlyExecutingJobs() {

        List<SimpleSchedule> returnValue = new ArrayList<>();

        try {
            LOGGER.debug("Try to get all currently executing jobs from scheduler.");
            List<JobExecutionContext> currentlyExecutingJobs;
            currentlyExecutingJobs = this.scheduler.getCurrentlyExecutingJobs();

            for (JobExecutionContext job : currentlyExecutingJobs) {
                returnValue.add(new QuartzSchedule(job.getJobDetail()));
            }
        } catch (SchedulerException ex) {
            throw new SchedulerManagerException("Could not get all currently executing jobs from scheduler. Internal scheduler exception occurred.", ex);
        }

        return returnValue;
    }

    @Override
    public SimpleSchedule addSchedule(SimpleSchedule schedule) {
        if (!(schedule instanceof QuartzSchedule)) {
            throw new IllegalArgumentException("ScheduleOptions has not been created via scheduler or is null.");
        }
        QuartzSchedule quartzSchedule = (QuartzSchedule) schedule;
        JobBuilder jobBuilder = quartzSchedule.build();
        jobBuilder.storeDurably(true);
        JobDetail jobDetail = jobBuilder.build();

        try {
            this.scheduler.addJob(jobDetail, false);
        } catch (SchedulerException ex) {
            throw new SchedulerManagerException("Could not add schedule to the scheduler.", ex);
        }

        return getScheduleById(jobDetail.getKey().toString());
    }

    @Override
    public JobTrigger addTrigger(String scheduleId, JobTrigger trigger) {
        JobTrigger retVal;

        if (scheduleId == null) {
            throw new IllegalArgumentException("Argument scheduleId can't be null.");
        }

        JobDetail jobDetail = findJobDetailById(scheduleId);

        if (jobDetail == null) {
            throw new NoSuchElementException("Schedule for scheduleId " + scheduleId + " not found.");
        }

        org.quartz.Trigger quartzTrigger;

        if (trigger instanceof QuartzExpressionTrigger) {
            quartzTrigger = ((QuartzExpressionTrigger) trigger).build().forJob(jobDetail).build();
            retVal = new QuartzExpressionTrigger((CronTrigger) quartzTrigger);
        } else if (trigger instanceof QuartzIntervalTrigger) {
            quartzTrigger = ((QuartzIntervalTrigger) trigger).build().forJob(jobDetail).build();
            retVal = new QuartzIntervalTrigger((SimpleTrigger) quartzTrigger);
        } else if (trigger instanceof QuartzAtTrigger) {
            quartzTrigger = ((QuartzAtTrigger) trigger).build().forJob(jobDetail).build();
            retVal = new QuartzAtTrigger((SimpleTrigger) quartzTrigger);
        } else if (trigger instanceof QuartzNowTrigger) {
            quartzTrigger = ((QuartzNowTrigger) trigger).build().forJob(jobDetail).build();
            retVal = new QuartzNowTrigger((SimpleTrigger) quartzTrigger);
        } else {
            throw new IllegalArgumentException("TriggerOptions has not been created via scheduler or is null.");
        }

        try {
            this.scheduler.scheduleJob(quartzTrigger);
        } catch (SchedulerException ex) {
            throw new SchedulerManagerException("Could not add schedule to the scheduler. Internal scheduler exception occurred.", ex);
        }

        return retVal;
    }

    @Override
    public boolean removeSchedule(String id) {
        boolean deleted = false;
        JobDetail jobDetail = findJobDetailById(id);

        if (jobDetail != null) {
            try {
                deleted = this.scheduler.deleteJob(jobDetail.getKey());
            } catch (SchedulerException ex) {
                throw new SchedulerManagerException("Could not delete schedule widh id '" + id + "'. Internal scheduler exception occurred.", ex);
            }
        }
        return deleted;
    }

    @Override
    public boolean removeTrigger(String id) {
        boolean deleted = false;
        org.quartz.Trigger trigger = findTriggerById(id);

        if (trigger != null) {
            try {
                deleted = this.scheduler.unscheduleJob(trigger.getKey());
            } catch (SchedulerException ex) {
                throw new SchedulerManagerException("Could not delete trigger with id '" + id + "'. Internal scheduler exception occurred.", ex);
            }
        }
        return deleted;
    }

    @Override
    public SimpleSchedule createSchedule() throws UnauthorizedAccessAttemptException {
        return new QuartzSchedule();
    }

    @Override
    public NowTrigger createNowTrigger() throws UnauthorizedAccessAttemptException {
        return new QuartzNowTrigger();
    }

    @Override
    public AtTrigger createAtTrigger() throws UnauthorizedAccessAttemptException {
        return new QuartzAtTrigger();
    }

    @Override
    public IntervalTrigger createIntervalTrigger() throws UnauthorizedAccessAttemptException {
        return new QuartzIntervalTrigger();
    }

    @Override
    public DelayTrigger createDelayTrigger() throws UnauthorizedAccessAttemptException {
        throw new UnsupportedOperationException("Trigger of type DelayTrigger is not supported for QuartzScheduler.");
    }

    @Override
    public ExpressionTrigger createExpressionTrigger() throws UnauthorizedAccessAttemptException {
        return new QuartzExpressionTrigger();
    }

    private JobDetail findJobDetailById(String id) {
        JobDetail retVal = null;

        try {
            for (JobKey jobKey : this.scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {

                if (id.equals(jobKey.toString())) {
                    retVal = this.scheduler.getJobDetail(jobKey);
                    break;
                }
            }
        } catch (SchedulerException ex) {
            throw new SchedulerManagerException("Could not find job details for id '" + id + "'. Internal scheduler exception occurred.", ex);
        }
        return retVal;
    }

    private org.quartz.Trigger findTriggerById(String id) {

        org.quartz.Trigger retVal = null;

        try {
            for (TriggerKey triggerKey : this.scheduler.getTriggerKeys(GroupMatcher.anyTriggerGroup())) {

                if (id.equals(triggerKey.toString())) {
                    retVal = this.scheduler.getTrigger(triggerKey);
                    break;
                }
            }
        } catch (SchedulerException ex) {
            throw new SchedulerManagerException("Could not get trigger by id '" + id + "'. Internal scheduler exception occurred.", ex);
        }
        return retVal;
    }

    private List<? extends org.quartz.Trigger> findTriggersBySchedulerId(String id) {

        JobDetail jobDetail = this.findJobDetailById(id);

        if (jobDetail == null) {
            return null;
        }

        List<? extends org.quartz.Trigger> triggers;

        try {
            triggers = scheduler.getTriggersOfJob(jobDetail.getKey());
        } catch (SchedulerException ex) {
            throw new SchedulerManagerException("Could not get all triggers for scheduler with id '" + id + "'", ex);
        }
        return triggers;
    }

    private JobTrigger wrapTrigger(org.quartz.Trigger trigger) {
        if (trigger instanceof CronTrigger) {
            return new QuartzExpressionTrigger((CronTrigger) trigger);
        } else if (trigger instanceof org.quartz.SimpleTrigger) {
            return new QuartzIntervalTrigger((org.quartz.SimpleTrigger) trigger);
        } else {
            throw new IllegalArgumentException("Unsupported trigger type, could not wrap trigger of type '" + trigger.getClass() + "'.");
        }
    }
}
