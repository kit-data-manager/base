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
package edu.kit.dama.rest.scheduler.test;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.rest.scheduler.services.interfaces.ISchedulerRestService;
import edu.kit.dama.rest.scheduler.types.ScheduleWrapper;
import edu.kit.dama.rest.scheduler.types.TriggerWrapper;
import edu.kit.dama.rest.util.RestUtils;
import edu.kit.dama.scheduler.api.impl.QuartzSchedule;
import edu.kit.dama.scheduler.api.impl.test.SimpleAtTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleDelayTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleExpressionTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleIntervalTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleNowTrigger;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.AtTrigger;
import edu.kit.dama.scheduler.api.trigger.DelayTrigger;
import edu.kit.dama.scheduler.api.trigger.ExpressionTrigger;
import edu.kit.dama.scheduler.api.trigger.IntervalTrigger;
import edu.kit.dama.scheduler.api.trigger.NowTrigger;
import edu.kit.dama.scheduler.api.trigger.JobTrigger;
import edu.kit.dama.scheduler.quartz.jobs.DebugJob;
import edu.kit.dama.util.Constants;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * @author wq7203
 */
@Path("/SchedulerTest")
public class SchedulerTestService implements ISchedulerRestService {

    @Override
    public Response checkService() {
        return Response.ok().build();
    }

    @Override
    public StreamingOutput getAllSchedules(HttpContext hc) {

        List<SimpleSchedule> schedules = new ArrayList<>();
        schedules.add(this.factorySchedule("TestNameA", "TestGroupA"));
        schedules.add(this.factorySchedule("TestNameB", "TestGroupB"));
        schedules.add(this.factorySchedule("TestNameC", "TestGroupC"));

        return RestUtils.createObjectGraphStream(ScheduleWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ScheduleWrapper(schedules));
    }

    @Override
    public StreamingOutput getScheduleById(String id, HttpContext hc) {

        SimpleSchedule schedule = this.factorySchedule(id.split("\\.")[1], id.split("\\.")[0]);

        return RestUtils.createObjectGraphStream(ScheduleWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ScheduleWrapper(schedule));
    }

    @Override
    public StreamingOutput getAllTriggers(HttpContext hc) {

        List<JobTrigger> triggers = new ArrayList<>();
        triggers.add(this.factoryExpressionTrigger("TestNameA", "TestGroupA"));
        triggers.add(this.factoryIntervallTrigger("TestNameB", "TestGroupB"));
        triggers.add(this.factoryDelayTrigger("TestNameC", "TestGroupC"));
        triggers.add(this.factoryNowTrigger("TestNameD", "TestGroupD"));
        triggers.add(this.factoryAtTrigger("TestNameE", "TestGroupE"));

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(triggers));
    }

    @Override
    public StreamingOutput getTriggerById(String id, HttpContext hc) {

        JobTrigger trigger = this.factoryExpressionTrigger(id.split("\\.")[1], id.split("\\.")[0]);

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(trigger));
    }

    @Override
    public StreamingOutput getTriggersByScheduleId(String id, HttpContext hc) {

        List<JobTrigger> triggers = new ArrayList<>();
        triggers.add(this.factoryExpressionTrigger("TestNameA", "TestGroupA"));
        triggers.add(this.factoryIntervallTrigger("TestNameB", "TestGroupB"));
        triggers.add(this.factoryDelayTrigger("TestNameC", "TestGroupC"));
        triggers.add(this.factoryNowTrigger("TestNameD", "TestGroupD"));
        triggers.add(this.factoryAtTrigger("TestNameE", "TestGroupE"));

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(triggers));
    }

    @Override
    public StreamingOutput getCurrentlyExecutingJobs(HttpContext hc) {

        List<SimpleSchedule> schedules = new ArrayList<>();
        schedules.add(this.factorySchedule("TestNameA", "TestGroupA"));
        schedules.add(this.factorySchedule("TestNameB", "TestGroupB"));

        return RestUtils.createObjectGraphStream(ScheduleWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ScheduleWrapper(schedules));
    }

    @Override
    public StreamingOutput addSchedule(String name, String group, String description, String jobClass, String jobParameters, HttpContext hc) {

        QuartzSchedule schedule = new QuartzSchedule();
        schedule.setName(name);
        schedule.setScheduleGroup(group);
        schedule.setDescription(description);
        schedule.setJobClass(jobClass);
        schedule.setJobParameters(jobParameters);

        return RestUtils.createObjectGraphStream(ScheduleWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new ScheduleWrapper(schedule));
    }

    @Override
    public StreamingOutput addExpressionTrigger(String id, String name, String group, Integer priority, String description, String startDate, String endDate, String expression, HttpContext hc) {

        ExpressionTrigger trigger = new SimpleExpressionTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        try {
            trigger.setStartDate(new Date(Long.parseLong(startDate)));
        } catch (NumberFormatException ex) {
            trigger.setStartDate(null);
        }
        try {
            trigger.setEndDate(new Date(Long.parseLong(endDate)));
        } catch (NumberFormatException ex) {
            trigger.setEndDate(null);
        }
        trigger.setExpression(expression);

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(trigger));
    }

    @Override
    public StreamingOutput addIntervallTrigger(String id, String name, String group, Integer priority, String description, String startDate, String endDate, Integer times, Long period, Long initialDelay, HttpContext hc) {

        IntervalTrigger trigger = new SimpleIntervalTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        try {
            trigger.setStartDate(new Date(Long.parseLong(startDate)));
        } catch (NumberFormatException ex) {
            trigger.setStartDate(null);
        }
        try {
            trigger.setStartDate(new Date(Long.parseLong(endDate)));
        } catch (NumberFormatException ex) {
            trigger.setStartDate(null);
        }
        trigger.setTimes(times);
        trigger.setPeriod(period);
        trigger.setInitialDelay(initialDelay);

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(trigger));
    }

    @Override
    public StreamingOutput addDelayTrigger(String id, String name, String group, Integer priority, String description, String startDate, String endDate, Integer times, Long delay, Long initialDelay, HttpContext hc) {

        DelayTrigger trigger = new SimpleDelayTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        try {
            trigger.setStartDate(new Date(Long.parseLong(startDate)));
        } catch (NumberFormatException ex) {
            trigger.setStartDate(null);
        }
        try {
            trigger.setEndDate(new Date(Long.parseLong(endDate)));
        } catch (NumberFormatException ex) {
            trigger.setEndDate(null);
        }
        trigger.setTimes(times);
        trigger.setDelay(delay);
        trigger.setInitialDelay(initialDelay);

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(trigger));
    }

    @Override
    public StreamingOutput addNowTrigger(String id, String name, String group, Integer priority, String description, HttpContext hc) {

        NowTrigger trigger = new SimpleNowTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(trigger));
    }

    @Override
    public StreamingOutput addAtTrigger(String id, String name, String group, Integer priority, String description, String startDate, HttpContext hc) {

        AtTrigger trigger = new SimpleAtTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        try {
            trigger.setStartDate(new Date(Long.parseLong(startDate)));
        } catch (NumberFormatException ex) {
            trigger.setStartDate(null);
        }

        return RestUtils.createObjectGraphStream(TriggerWrapper.class, Constants.REST_DEFAULT_OBJECT_GRAPH, new TriggerWrapper(trigger));
    }

    private SimpleSchedule factorySchedule(String name, String group) {
        QuartzSchedule schedule = new QuartzSchedule();
        schedule.setName(name);
        schedule.setScheduleGroup(group);
        schedule.setDescription("description");
        schedule.setJobClass(DebugJob.class.getName());
        schedule.setJobParameters("jobParameters");
        return schedule;
    }

    private JobTrigger factoryExpressionTrigger(String name, String group) {

        ExpressionTrigger trigger = new SimpleExpressionTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(42);
        trigger.setDescription("description");
        trigger.setStartDate(new Date(0));
        trigger.setEndDate(new Date(946681200000L));
        trigger.setExpression("expression");
        return trigger;
    }

    private JobTrigger factoryIntervallTrigger(String name, String group) {

        IntervalTrigger trigger = new SimpleIntervalTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(42);
        trigger.setDescription("description");
        trigger.setStartDate(new Date(0));
        trigger.setEndDate(new Date(946681200000L));
        trigger.setTimes(43);
        trigger.setPeriod(10000L);
        return trigger;
    }

    private JobTrigger factoryDelayTrigger(String name, String group) {

        DelayTrigger trigger = new SimpleDelayTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(42);
        trigger.setDescription("description");
        trigger.setStartDate(new Date(0));
        trigger.setEndDate(new Date(946681200000L));
        trigger.setTimes(43);
        trigger.setDelay(44L);
        trigger.setInitialDelay(45L);
        return trigger;
    }

    private JobTrigger factoryNowTrigger(String name, String group) {

        NowTrigger trigger = new SimpleNowTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(42);
        trigger.setDescription("description");
        return trigger;
    }

    private JobTrigger factoryAtTrigger(String name, String group) {

        AtTrigger trigger = new SimpleAtTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(42);
        trigger.setDescription("description");
        trigger.setStartDate(new Date(0));
        return trigger;
    }

    @Override
    public Response deleteScheduleById(String id, HttpContext hc) {
        return Response.ok().build();
    }

    @Override
    public Response deleteTriggerById(String id, HttpContext hc) {
        return Response.ok().build();
    }
}
