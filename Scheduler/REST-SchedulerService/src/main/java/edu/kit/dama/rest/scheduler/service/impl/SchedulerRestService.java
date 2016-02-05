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
package edu.kit.dama.rest.scheduler.service.impl;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.rest.scheduler.services.interfaces.ISchedulerRestService;
import edu.kit.dama.rest.scheduler.types.ScheduleWrapper;
import edu.kit.dama.rest.scheduler.types.TriggerWrapper;
import edu.kit.dama.rest.util.RestUtils;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.*;
import edu.kit.dama.scheduler.manager.SecureSchedulerManager;
import edu.kit.dama.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author wq7203
 */
@Path("/")
public final class SchedulerRestService implements ISchedulerRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerRestService.class);
    private static final String[] DATE_FORMATS = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"};

    @Override
    public Response checkService() {
        return Response.ok().build();
    }

    @Override
    public StreamingOutput getAllSchedules(HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        List<SimpleSchedule> schedules;

        try {
            schedules = SecureSchedulerManager.factorySecureSchedulerManager(ctx).getAllSchedules();
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        return RestUtils.createObjectGraphStream(ScheduleWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ScheduleWrapper(schedules));
    }

    @Override
    public StreamingOutput getScheduleById(String id, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        SimpleSchedule schedule;

        try {
            schedule = SecureSchedulerManager.factorySecureSchedulerManager(ctx).getScheduleById(id);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        if (schedule == null) {
            throw new WebApplicationException(new Exception("Schedule for id " + id + " not found"), Response.Status.NOT_FOUND);
        }

        return RestUtils.createObjectGraphStream(ScheduleWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ScheduleWrapper(schedule));
    }

    @Override
    public StreamingOutput getAllTriggers(HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        List<JobTrigger> triggers;

        try {
            triggers = SecureSchedulerManager.factorySecureSchedulerManager(ctx).getAllTriggers();
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(triggers));
    }

    @Override
    public StreamingOutput getTriggerById(String id, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        JobTrigger trigger;

        try {
            trigger = SecureSchedulerManager.factorySecureSchedulerManager(ctx).getTriggerById(id);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        if (trigger == null) {
            throw new WebApplicationException(new Exception("Trigger for id " + id + " not found"), Response.Status.NOT_FOUND);
        }

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(trigger));
    }

    @Override
    public StreamingOutput getCurrentlyExecutingJobs(HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        List<SimpleSchedule> schedules;

        try {
            schedules = SecureSchedulerManager.factorySecureSchedulerManager(ctx).getCurrentlyExecutingJobs();
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        return RestUtils.createObjectGraphStream(ScheduleWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ScheduleWrapper(schedules));
    }

    @Override
    public StreamingOutput getTriggersByScheduleId(String id, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));
        List<JobTrigger> triggers;

        try {
            triggers = SecureSchedulerManager.factorySecureSchedulerManager(ctx).getTriggersByScheduleId(id);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(triggers));
    }

    @Override
    public StreamingOutput addSchedule(String name, String group, String description, String jobClass, String jobParameters, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));

        SimpleSchedule schedule;
        try {
            schedule = SecureSchedulerManager.factorySecureSchedulerManager(ctx).createSchedule();
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }
        schedule.setName(name);
        schedule.setScheduleGroup(group);
        schedule.setDescription(description);
        schedule.setJobClass(jobClass);
        schedule.setJobParameters(jobParameters);

        SimpleSchedule result;
        try {
            result = SecureSchedulerManager.factorySecureSchedulerManager(ctx).addSchedule(schedule);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        return RestUtils.createObjectGraphStream(ScheduleWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ScheduleWrapper(result));
    }

    @Override
    public StreamingOutput addExpressionTrigger(String id, String name, String group, Integer priority, String description, String startDate, String endDate, String expression, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));

        ExpressionTrigger trigger;
        try {
            trigger = SecureSchedulerManager.factorySecureSchedulerManager(ctx).createExpressionTrigger();
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(this.parseDate(startDate));
        trigger.setEndDate(this.parseDate(endDate));
        trigger.setExpression(expression);

        JobTrigger addedTrigger;
        try {
            addedTrigger = SecureSchedulerManager.factorySecureSchedulerManager(ctx).addTrigger(id, trigger);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }
        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(addedTrigger));
    }

    @Override
    public StreamingOutput addIntervallTrigger(String id, String name, String group, Integer priority, String description, String startDate, String endDate, Integer times, Long period, Long initialDelay, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));

        IntervalTrigger trigger;
        try {
            trigger = SecureSchedulerManager.factorySecureSchedulerManager(ctx).createIntervalTrigger();
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(this.parseDate(startDate));
        trigger.setEndDate(this.parseDate(endDate));
        trigger.setTimes(times);
        trigger.setPeriod(period);
        trigger.setInitialDelay(initialDelay);

        JobTrigger addedTrigger;
        try {
            addedTrigger = SecureSchedulerManager.factorySecureSchedulerManager(ctx).addTrigger(id, trigger);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }
        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(addedTrigger));
    }

    @Override
    public StreamingOutput addDelayTrigger(String id, String name, String group, Integer priority, String description, String startDate, String endDate, Integer times, Long delay, Long initialDelay, HttpContext hc) {
        LOGGER.warn("Adding delay triggers is currently not supported.");
        throw new WebApplicationException(405);
    }

    @Override
    public StreamingOutput addNowTrigger(String id, String name, String group, Integer priority, String description, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));

        NowTrigger trigger;
        try {
            trigger = SecureSchedulerManager.factorySecureSchedulerManager(ctx).createNowTrigger();
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);

        JobTrigger addedTrigger;
        try {
            addedTrigger = SecureSchedulerManager.factorySecureSchedulerManager(ctx).addTrigger(id, trigger);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }
        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(addedTrigger));
    }

    @Override
    public StreamingOutput addAtTrigger(String id, String name, String group, Integer priority, String description, String startDate, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));

        AtTrigger trigger;
        try {
            trigger = SecureSchedulerManager.factorySecureSchedulerManager(ctx).createAtTrigger();
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(this.parseDate(startDate));

        JobTrigger addedTrigger;
        try {
            addedTrigger = SecureSchedulerManager.factorySecureSchedulerManager(ctx).addTrigger(id, trigger);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }
        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(addedTrigger));
    }

    @Override
    public Response deleteScheduleById(String id, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));

        boolean removed;
        try {
            removed = SecureSchedulerManager.factorySecureSchedulerManager(ctx).removeSchedule(id);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        Response response;
        if (removed) {
            response = Response.ok().build();
        } else {
            response = Response.status(Response.Status.NOT_FOUND).build();
        }
        return response;
    }

    @Override
    public Response deleteTriggerById(String id, HttpContext hc) {

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(Constants.USERS_GROUP_ID));

        boolean removed;
        try {
            removed = SecureSchedulerManager.factorySecureSchedulerManager(ctx).removeTrigger(id);
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new WebApplicationException(ex, Response.Status.UNAUTHORIZED);
        }

        Response response;
        if (removed) {
            response = Response.ok().build();
        } else {
            response = Response.status(Response.Status.NOT_FOUND).build();
        }
        return response;
    }

    private Date parseDate(String date) {

        if (date == null) {
            return null;
        }

        try {
            return new Date(Long.parseLong(date));
        } catch (NumberFormatException ex) {
        }

        for (String pattern : DATE_FORMATS) {
            try {
                return (new SimpleDateFormat(pattern)).parse(date);
            } catch (ParseException ex) {
            }
        }
        throw new WebApplicationException(new Exception("Date parameter " + date + "is malformed, expected formats are " + Arrays.toString(DATE_FORMATS) + "."), Response.Status.BAD_REQUEST);
    }
}
