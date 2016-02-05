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
package edu.kit.dama.rest.scheduler.service.client.impl;

import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.scheduler.types.ScheduleWrapper;
import edu.kit.dama.rest.scheduler.types.TriggerWrapper;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.AtTrigger;
import edu.kit.dama.scheduler.api.trigger.DelayTrigger;
import edu.kit.dama.scheduler.api.trigger.ExpressionTrigger;
import edu.kit.dama.scheduler.api.trigger.IntervalTrigger;
import edu.kit.dama.scheduler.api.trigger.NowTrigger;

/**
 * Rest client for scheduler.
 *
 * @author wq7203
 */
public final class SchedulerRestClient extends AbstractRestClient {

    /**
     * The logger
     */
    // private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SchedulerRestClient.class);
    /**
     * Path for all schedules.
     */
    private static final String SCHEDULES = "/schedules";

    /**
     * Path to schedule with given id.
     */
    private static final String SCHEDULE_BY_ID = SCHEDULES + "/{0}";

    /**
     * Path for all triggers.
     */
    private static final String TRIGGERS = "/triggers";

    /**
     * Path for all expression triggers.
     */
    private static final String EXPRESSION_TRIGGERS = "/expressionTriggers";

    /**
     * Path for all intervall triggers.
     */
    private static final String INTERVALL_TRIGGERS = "/intervalTriggers";

    /**
     * Path for all delay triggers.
     */
    private static final String DELAY_TRIGGERS = "/delayTriggers";

    /**
     * Path for all now-triggers.
     */
    private static final String NOW_TRIGGERS = "/nowTriggers";

    /**
     * Path for all at-triggers.
     */
    private static final String AT_TRIGGERS = "/atTriggers";

    /**
     * Path for all currently executing jobs.
     */
    private static final String CURRENTLY_EXECUTING_JOBS = "/currentlyExecutingJobs";

    /**
     * Path to trigger with given id.
     */
    private static final String TRIGGER_BY_ID = TRIGGERS + "/{0}";

    /**
     * Path to triggers for scheduler with given id.
     */
    private static final String TRIGGER_BY_SCHEDULER_ID = SCHEDULES + "/{0}" + TRIGGERS;

    /**
     * Path to expression triggers for scheduler with given id.
     */
    private static final String EXPRESSION_TRIGGERS_BY_SCHEDULER_ID = SCHEDULES + "/{0}" + EXPRESSION_TRIGGERS;

    /**
     * Path to intervall triggers for scheduler with given id.
     */
    private static final String INTERVALL_TRIGGERS_BY_SCHEDULER_ID = SCHEDULES + "/{0}" + INTERVALL_TRIGGERS;

    /**
     * Path to delay triggers for scheduler with given id.
     */
    private static final String DELAY_TRIGGERS_BY_SCHEDULER_ID = SCHEDULES + "/{0}" + DELAY_TRIGGERS;

    /**
     * Path to now-triggers for scheduler with given id.
     */
    private static final String NOW_TRIGGERS_BY_SCHEDULER_ID = SCHEDULES + "/{0}" + NOW_TRIGGERS;

    /**
     * Path to at-triggers for scheduler with given id.
     */
    private static final String AT_TRIGGERS_BY_SCHEDULER_ID = SCHEDULES + "/{0}" + AT_TRIGGERS;

    /**
     * Name for schedule and trigger.
     */
    private static final String FORM_PARAMETER_NAME = "name";

    /**
     * Group for schedule and trigger.
     */
    private static final String FORM_PARAMETER_GROUP = "group";

    /**
     * Priority for trigger.
     */
    private static final String FORM_PARAMETER_PRIORITY = "priority";

    /**
     * Description for schedule and trigger.
     */
    private static final String FORM_PARAMETER_DESCRIPTION = "description";

    /**
     * Job class for schedule.
     */
    private static final String FORM_PARAMETER_JOB_CLASS = "jobClass";

    /**
     * Job parameters for schedule.
     */
    private static final String FORM_PARAMETER_JOB_PARAMETERS = "jobParameters";

    /**
     * Name for trigger.
     */
    private static final String FORM_PARAMETER_TRIGGER_NAME = "triggerName";

    /**
     * Group for trigger.
     */
    private static final String FORM_PARAMETER_TRIGGER_GROUP = "triggerGroup";

    /**
     * Description for trigger.
     */
    private static final String FORM_PARAMETER_TRIGGER_DESCRIPTION = "triggerDescription";

    /**
     * Start date for trigger.
     */
    private static final String FORM_PARAMETER_START_DATE = "startDate";

    /**
     * End date for trigger.
     */
    private static final String FORM_PARAMETER_END_DATE = "endDate";

    /**
     * Times for trigger.
     */
    private static final String FORM_PARAMETER_TIMES = "times";

    /**
     * Expression for expression trigger.
     */
    private static final String FORM_PARAMETER_EXPRESSION = "expression";

    /**
     * Period for intervall trigger.
     */
    private static final String FORM_PARAMETER_PERIOD = "period";

    /**
     * Initial delay for intervall and delay trigger.
     */
    private static final String FORM_PARAMETER_INITIAL_DELAY = "initialDelay";

    /**
     * Delay for delay trigger.
     */
    private static final String FORM_PARAMETER_DELAY = "delay";

    /**
     * Create a REST client with a predefined context.
     *
     * @param rootUrl root url of the scheduler service. (e.g.:
     * "http://dama.lsdf.kit.edu/KITDM/rest/scheduler")
     * @param context initial context
     */
    public SchedulerRestClient(String rootUrl, SimpleRESTContext context) {
        super(rootUrl, context);
    }

    /**
     * Perform a get for schedules.
     *
     * @param path url.
     * @param queryParams url parameters.
     *
     * @return ScheduleWrapper.
     */
    private ScheduleWrapper performScheduleGet(String path,
            MultivaluedMap<String, String> queryParams) {
        return RestClientUtils.performGet(ScheduleWrapper.class,
                this.getWebResource(path), queryParams);
    }

    /**
     * Perform a get for triggers.
     *
     * @param path url.
     * @param queryParams url parameters.
     *
     * @return ScheduleWrapper.
     */
    private TriggerWrapper performTriggerGet(String path,
            MultivaluedMap<String, String> queryParams) {
        return RestClientUtils.performGet(TriggerWrapper.class,
                this.getWebResource(path), queryParams);
    }

    /**
     * Perform a post for schedules.
     *
     * @param path url.
     * @param queryParams url parameters.
     * @param formParams Form parameters.
     *
     * @return ScheduleWrapper.
     */
    private ScheduleWrapper performSchedulePost(String path,
            MultivaluedMap<String, String> queryParams, MultivaluedMap<String, String> formParams) {
        return RestClientUtils.performPost(ScheduleWrapper.class, this.getWebResource(path), queryParams, formParams);
    }

    /**
     * Perform a post for triggers.
     *
     * @param path url.
     * @param queryParams url parameters.
     * @param formParams Form parameters.
     *
     * @return TriggerWrapper.
     */
    private TriggerWrapper performTriggerPost(String path,
            MultivaluedMap<String, String> queryParams, MultivaluedMap<String, String> formParams) {
        return RestClientUtils.performPost(TriggerWrapper.class, this.getWebResource(path), queryParams, formParams);
    }

    /**
     * Perform a delete for schedules.
     *
     * @param path url.
     * @param queryParams url parameters.
     *
     * @return ScheduleWrapper.
     */
    private boolean performScheduleDelete(String path, MultivaluedMap<String, String> queryParams) {
        return ((ClientResponse) RestClientUtils.performDelete(null, this.getWebResource(path), queryParams)).getStatus() == 200;
    }

    /**
     * Perform a delete for triggers.
     *
     * @param path url.
     * @param queryParams url parameters.
     *
     * @return TriggerWrapper.
     */
    private boolean performTriggerDelete(String path, MultivaluedMap<String, String> queryParams) {
        return ((ClientResponse) RestClientUtils.performDelete(null, this.getWebResource(path), queryParams)).getStatus() == 200;
    }

    /**
     * Get all schedules.
     *
     * @param securityContext security context
     *
     * @return ScheduleWrapper
     */
    public ScheduleWrapper getAllSchedules(SimpleRESTContext securityContext) {

        ScheduleWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        this.setFilterFromContext(securityContext);

        retVal = this.performScheduleGet(SCHEDULES, queryParams);
        return retVal;
    }

    /**
     * Get schedule by id.
     *
     * @param id the id of the schedule.
     * @param securityContext security context
     *
     * @return ScheduleWrapper
     */
    public ScheduleWrapper getScheduleById(String id, SimpleRESTContext securityContext) {

        ScheduleWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        this.setFilterFromContext(securityContext);

        retVal = this.performScheduleGet(RestClientUtils.encodeUrl(SCHEDULE_BY_ID, id), queryParams);
        return retVal;
    }

    /**
     * Get all triggers.
     *
     * @param securityContext security context
     *
     * @return TriggerWrapper
     */
    public TriggerWrapper getAllTriggers(SimpleRESTContext securityContext) {

        TriggerWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        this.setFilterFromContext(securityContext);

        retVal = this.performTriggerGet(RestClientUtils.encodeUrl(TRIGGERS), queryParams);
        return retVal;
    }

    /**
     * Get trigger by id.
     *
     * @param id the id of the trigger.
     * @param securityContext security context
     *
     * @return TriggerWrapper
     */
    public TriggerWrapper getTriggerById(String id, SimpleRESTContext securityContext) {

        TriggerWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        this.setFilterFromContext(securityContext);

        retVal = this.performTriggerGet(RestClientUtils.encodeUrl(TRIGGER_BY_ID, id), queryParams);
        return retVal;
    }

    /**
     * Get trigger by schedule id.
     *
     * @param id the id of the schedule.
     * @param securityContext security context
     *
     * @return TriggerWrapper
     */
    public TriggerWrapper getTriggersBySchedulerId(String id, SimpleRESTContext securityContext) {

        TriggerWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        this.setFilterFromContext(securityContext);

        retVal = this.performTriggerGet(RestClientUtils.encodeUrl(TRIGGER_BY_SCHEDULER_ID, id), queryParams);
        return retVal;
    }

    /**
     * Get a list of currently executing jobs.
     *
     * @param securityContext security context
     *
     * @return ScheduleWrapper
     */
    public ScheduleWrapper getCurrentlyExecutingJobs(SimpleRESTContext securityContext) {

        ScheduleWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        this.setFilterFromContext(securityContext);

        retVal = this.performScheduleGet(CURRENTLY_EXECUTING_JOBS, queryParams);

        return retVal;
    }

    /**
     * Add schedule with expression trigger.
     *
     * @param simpleSchedule new schedule instance to add.
     * @param securityContext security context.
     *
     * @return StudyWrapper.
     */
    public ScheduleWrapper addSchedule(SimpleSchedule simpleSchedule, SimpleRESTContext securityContext) {

        if (simpleSchedule == null) {
            throw new IllegalArgumentException("Argument simpleSchedule can't be null.");
        }

        ScheduleWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
        this.setFilterFromContext(securityContext);

        this.addNonNull(formParams, FORM_PARAMETER_NAME, simpleSchedule.getName());
        this.addNonNull(formParams, FORM_PARAMETER_GROUP, simpleSchedule.getScheduleGroup());
        this.addNonNull(formParams, FORM_PARAMETER_DESCRIPTION, simpleSchedule.getDescription());
        this.addNonNull(formParams, FORM_PARAMETER_JOB_CLASS, simpleSchedule.getJobClass());
        this.addNonNull(formParams, FORM_PARAMETER_JOB_PARAMETERS, simpleSchedule.getJobParameters());

        retVal = this.performSchedulePost(SCHEDULES, queryParams, formParams);
        return retVal;
    }

    /**
     * Add expression trigger.
     *
     * @param scheduleId the id of the schedule.
     * @param expressionTrigger new expression trigger instance to add.
     * @param securityContext security context.
     *
     * @return TriggerWrapper.
     */
    public TriggerWrapper addExpressionTrigger(String scheduleId, ExpressionTrigger expressionTrigger,
            SimpleRESTContext securityContext) {

        if (scheduleId == null) {
            throw new IllegalArgumentException("Argument scheduleId can't be null.");
        }
        if (expressionTrigger == null) {
            throw new IllegalArgumentException("Argument expressionTrigger can't be null.");
        }

        TriggerWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
        this.setFilterFromContext(securityContext);

        this.addNonNull(formParams, FORM_PARAMETER_NAME, expressionTrigger.getName());
        this.addNonNull(formParams, FORM_PARAMETER_GROUP, expressionTrigger.getTriggerGroup());

        if (expressionTrigger.getPriority() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_PRIORITY, expressionTrigger.getPriority().toString());
        }

        this.addNonNull(formParams, FORM_PARAMETER_DESCRIPTION, expressionTrigger.getDescription());

        if (expressionTrigger.getStartDate() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_START_DATE, Long.toString(expressionTrigger.
                    getStartDate().getTime()));
        }
        if (expressionTrigger.getEndDate() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_END_DATE, Long.toString(expressionTrigger.
                    getEndDate().getTime()));
        }
        this.addNonNull(formParams, FORM_PARAMETER_EXPRESSION, expressionTrigger.getExpression());

        retVal = this.performTriggerPost(RestClientUtils.encodeUrl(EXPRESSION_TRIGGERS_BY_SCHEDULER_ID, scheduleId), queryParams, formParams);

        return retVal;
    }

    /**
     * Add intervall trigger.
     *
     * @param scheduleId the id of the schedule.
     * @param intervalTrigger new intervall trigger instance to add.
     * @param securityContext security context.
     *
     * @return StudyWrapper
     */
    public TriggerWrapper addIntervallTrigger(String scheduleId, IntervalTrigger intervalTrigger,
            SimpleRESTContext securityContext) {

        if (scheduleId == null) {
            throw new IllegalArgumentException("Argument scheduleId can't be null.");
        }
        if (intervalTrigger == null) {
            throw new IllegalArgumentException("Argument intervalTrigger can't be null.");
        }

        TriggerWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
        this.setFilterFromContext(securityContext);

        this.addNonNull(formParams, FORM_PARAMETER_NAME, intervalTrigger.getName());
        this.addNonNull(formParams, FORM_PARAMETER_GROUP, intervalTrigger.getTriggerGroup());

        if (intervalTrigger.getPriority() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_PRIORITY, intervalTrigger.getPriority().toString());
        }

        this.addNonNull(formParams, FORM_PARAMETER_DESCRIPTION, intervalTrigger.getDescription());

        if (intervalTrigger.getStartDate() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_START_DATE, Long.toString(intervalTrigger.
                    getStartDate().getTime()));
        }
        if (intervalTrigger.getEndDate() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_END_DATE, Long.toString(intervalTrigger.
                    getEndDate().getTime()));
        }
        if (intervalTrigger.getTimes() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_TIMES, intervalTrigger.getTimes().toString());
        }
        if (intervalTrigger.getPeriod() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_PERIOD, intervalTrigger.getPeriod().toString());
        }
        if (intervalTrigger.getInitialDelay() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_INITIAL_DELAY, intervalTrigger.getInitialDelay().toString());
        }

        retVal = this.performTriggerPost(RestClientUtils.encodeUrl(INTERVALL_TRIGGERS_BY_SCHEDULER_ID, scheduleId), queryParams, formParams);

        return retVal;
    }

    /**
     * Add intervall trigger.
     *
     * @param scheduleId the id of the schedule.
     * @param delayTrigger new delay trigger instance to add.
     * @param securityContext security context.
     *
     * @return StudyWrapper
     */
    public TriggerWrapper addDelayTrigger(String scheduleId, DelayTrigger delayTrigger,
            SimpleRESTContext securityContext) {

        if (scheduleId == null) {
            throw new IllegalArgumentException("Argument scheduleId can't be null.");
        }
        if (delayTrigger == null) {
            throw new IllegalArgumentException("Argument delayTrigger can't be null.");
        }

        TriggerWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
        this.setFilterFromContext(securityContext);

        this.addNonNull(formParams, FORM_PARAMETER_NAME, delayTrigger.getName());
        this.addNonNull(formParams, FORM_PARAMETER_GROUP, delayTrigger.getTriggerGroup());

        if (delayTrigger.getPriority() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_PRIORITY, delayTrigger.getPriority().toString());
        }

        this.addNonNull(formParams, FORM_PARAMETER_DESCRIPTION, delayTrigger.getDescription());

        if (delayTrigger.getStartDate() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_START_DATE, Long.toString(delayTrigger.
                    getStartDate().getTime()));
        }
        if (delayTrigger.getEndDate() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_END_DATE, Long.toString(delayTrigger.
                    getEndDate().getTime()));
        }
        if (delayTrigger.getTimes() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_TIMES, delayTrigger.getTimes().toString());
        }
        if (delayTrigger.getDelay() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_DELAY, delayTrigger.getDelay().toString());
        }
        if (delayTrigger.getInitialDelay() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_INITIAL_DELAY, delayTrigger.getInitialDelay().toString());
        }

        retVal = this.performTriggerPost(RestClientUtils.encodeUrl(DELAY_TRIGGERS_BY_SCHEDULER_ID, scheduleId), queryParams, formParams);

        return retVal;
    }

    /**
     * Add now-trigger.
     *
     * @param scheduleId the id of the schedule.
     * @param nowTrigger new now-trigger instance to add.
     * @param securityContext security context.
     *
     * @return StudyWrapper
     */
    public TriggerWrapper addNowTrigger(String scheduleId, NowTrigger nowTrigger,
            SimpleRESTContext securityContext) {

        if (scheduleId == null) {
            throw new IllegalArgumentException("Argument scheduleId can't be null.");
        }
        if (nowTrigger == null) {
            throw new IllegalArgumentException("Argument nowTrigger can't be null.");
        }

        TriggerWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
        this.setFilterFromContext(securityContext);

        this.addNonNull(formParams, FORM_PARAMETER_NAME, nowTrigger.getName());
        this.addNonNull(formParams, FORM_PARAMETER_GROUP, nowTrigger.getTriggerGroup());

        if (nowTrigger.getPriority() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_PRIORITY, nowTrigger.getPriority().toString());
        }
        this.addNonNull(formParams, FORM_PARAMETER_DESCRIPTION, nowTrigger.getDescription());

        retVal = this.performTriggerPost(RestClientUtils.encodeUrl(NOW_TRIGGERS_BY_SCHEDULER_ID, scheduleId), queryParams, formParams);

        return retVal;
    }

    /**
     * Add at-trigger.
     *
     * @param scheduleId the id of the schedule.
     * @param atTrigger new at-trigger instance to add.
     * @param securityContext security context.
     *
     * @return StudyWrapper
     */
    public TriggerWrapper addAtTrigger(String scheduleId, AtTrigger atTrigger,
            SimpleRESTContext securityContext) {

        if (scheduleId == null) {
            throw new IllegalArgumentException("Argument scheduleId can't be null.");
        }
        if (atTrigger == null) {
            throw new IllegalArgumentException("Argument atTrigger can't be null.");
        }

        TriggerWrapper retVal;
        MultivaluedMap<String, String> queryParams = null;
        MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
        this.setFilterFromContext(securityContext);

        this.addNonNull(formParams, FORM_PARAMETER_NAME, atTrigger.getName());
        this.addNonNull(formParams, FORM_PARAMETER_GROUP, atTrigger.getTriggerGroup());

        if (atTrigger.getPriority() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_PRIORITY, atTrigger.getPriority().toString());
        }
        this.addNonNull(formParams, FORM_PARAMETER_DESCRIPTION, atTrigger.getDescription());
        if (atTrigger.getStartDate() != null) {
            this.addNonNull(formParams, FORM_PARAMETER_START_DATE, Long.toString(atTrigger.
                    getStartDate().getTime()));
        }

        retVal = this.performTriggerPost(RestClientUtils.encodeUrl(AT_TRIGGERS_BY_SCHEDULER_ID, scheduleId), queryParams, formParams);

        return retVal;
    }

    /**
     * Delete schedule with provided scheduleId.
     *
     * @param scheduleId the id of the schedule.
     * @param securityContext security context.
     *
     * @return true if the schedule was deleted, otherwise false.
     */
    public boolean deleteSchedule(String scheduleId, SimpleRESTContext securityContext) {

        if (scheduleId == null) {
            throw new IllegalArgumentException("Argument scheduleId can't be null.");
        }

        boolean retVal;
        MultivaluedMap<String, String> queryParams = null;
        this.setFilterFromContext(securityContext);

        retVal = this.performScheduleDelete(RestClientUtils.encodeUrl(SCHEDULE_BY_ID, scheduleId), queryParams);

        return retVal;
    }

    /**
     * Delete trigger with provided triggerId.
     *
     * @param triggerId the id of the trigger.
     * @param securityContext security context.
     *
     * @return true if the trigger was deleted, otherwise false.
     */
    public boolean deleteTrigger(String triggerId, SimpleRESTContext securityContext) {

        if (triggerId == null) {
            throw new IllegalArgumentException("Argument triggerId can't be null.");
        }

        boolean retVal;
        MultivaluedMap<String, String> queryParams = null;
        this.setFilterFromContext(securityContext);

        retVal = this.performTriggerDelete(RestClientUtils.encodeUrl(TRIGGER_BY_ID, triggerId), queryParams);

        return retVal;
    }

    private void addNonNull(MultivaluedMap<String, String> formParams, String key, String val) {
        if (formParams == null) {
            throw new IllegalArgumentException("Argument formParams can't be null.");
        } else if (key == null) {
            throw new IllegalArgumentException("Argument key can't be null.");
        } else if (val != null) {
            formParams.add(key, val);
        }
    }
}
