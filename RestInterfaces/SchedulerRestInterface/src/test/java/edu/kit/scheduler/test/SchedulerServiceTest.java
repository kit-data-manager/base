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
package edu.kit.scheduler.test;

import java.io.IOException;
import java.net.ServerSocket;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sun.jersey.test.framework.JerseyTest;

import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.scheduler.service.client.impl.SchedulerRestClient;
import edu.kit.dama.rest.scheduler.types.ScheduleWrapper;
import edu.kit.dama.rest.scheduler.types.TriggerWrapper;
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

/**
 *
 * @author mf6319
 */
public class SchedulerServiceTest extends JerseyTest {

    private static SchedulerRestClient client;
    private static SimpleRESTContext ctx;

    /**
     * Default constructor.
     */
    public SchedulerServiceTest() {
        super("edu.kit.scheduler.test");
        ctx = new SimpleRESTContext("secret", "secret");
        client = new SchedulerRestClient(
                "http://localhost:9998/SchedulerTest", ctx);
    }

    @Override
    protected int getPort(int defaultPort) {
        ServerSocket server = null;
        int port = -1;
        try {
            server = new ServerSocket(defaultPort);
            port = server.getLocalPort();
        } catch (IOException e) {
            // ignore
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        if ((port != -1) || (defaultPort == 0)) {
            return port;
        }
        return this.getPort(0);
    }

    @Test
    public void testGetAllSchedules() {

        ScheduleWrapper wrapper = client.getAllSchedules(ctx);
        Assert.assertEquals(3, wrapper.getCount().intValue());
    }

    @Test
    public void testGetScheduleById() {

        final String name = "TestName";
        final String group = "TestGroup";

        ScheduleWrapper wrapper = client.getScheduleById(group + "." + name, ctx);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        SimpleSchedule schedule = wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(name, schedule.getName());
        Assert.assertEquals(group, schedule.getScheduleGroup());
    }

    @Test
    public void testGetAllTriggers() {

        TriggerWrapper wrapper = client.getAllTriggers(ctx);

        Assert.assertEquals(5, wrapper.getCount().intValue());
    }

    @Test
    public void testGetTriggerById() {

        final String name = "TestName";
        final String group = "TestGroup";

        TriggerWrapper wrapper = client.getTriggerById(group + "." + name, ctx);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        JobTrigger trigger = wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(name, trigger.getName());
        Assert.assertEquals(group, trigger.getTriggerGroup());
    }

    @Test
    public void testGetTriggersBySchedulerId() {

        final String name = "TestName";
        final String group = "TestGroup";

        TriggerWrapper wrapper = client.getTriggersBySchedulerId(group + "." + name, ctx);

        Assert.assertEquals(5, wrapper.getCount().intValue());
    }

    @Test
    public void testAddSchedule() {

        final String name = "TestName";
        final String group = "TestGroup";
        final String description = "description";
        final String jobClass = DebugJob.class.getName();
        final String jobParameters = "jobParameters";

        SimpleSchedule schedule = new QuartzSchedule();
        schedule.setName(name);
        schedule.setScheduleGroup(group);
        schedule.setDescription(description);
        schedule.setJobClass(jobClass);
        schedule.setJobParameters(jobParameters);

        ScheduleWrapper wrapper = client.addSchedule(schedule, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        SimpleSchedule scheduleResult = (SimpleSchedule) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(name, scheduleResult.getName());
        Assert.assertEquals(group, scheduleResult.getScheduleGroup());
        Assert.assertEquals(description, scheduleResult.getDescription());
        Assert.assertEquals(jobClass, scheduleResult.getJobClass());
        Assert.assertEquals(jobParameters, scheduleResult.getJobParameters());
    }

    @Test
    public void testAddExpressionTrigger() {

        final String name = "TestName";
        final String group = "TestGroup";
        final Integer priority = 3;
        final String description = "description";
        Calendar startCalendar = new GregorianCalendar();
        startCalendar.add(DateFormat.YEAR_FIELD, 1);
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.add(DateFormat.YEAR_FIELD, 2);
        final Date startDate = startCalendar.getTime();
        final Date endDate = endCalendar.getTime();
        final String expression = "expression";

        ExpressionTrigger trigger = new SimpleExpressionTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(startDate);
        trigger.setEndDate(endDate);
        trigger.setExpression(expression);

        TriggerWrapper wrapper = client.addExpressionTrigger("TestName.TestGroup", trigger, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        ExpressionTrigger triggerResult = (ExpressionTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(name, triggerResult.getName());
        Assert.assertEquals(group, triggerResult.getTriggerGroup());
        Assert.assertEquals(priority.intValue(), triggerResult.getPriority().intValue());
        Assert.assertEquals(description, triggerResult.getDescription());
        Assert.assertEquals(startDate, triggerResult.getStartDate());
        Assert.assertEquals(endDate, triggerResult.getEndDate());
        Assert.assertEquals(expression, triggerResult.getExpression());
    }

    public void testAddIntervallTrigger() {

        final String name = "TestName";
        final String group = "TestGroup";
        final Integer priority = 3;
        final String description = "description";
        Calendar startCalendar = new GregorianCalendar();
        startCalendar.add(DateFormat.YEAR_FIELD, 1);
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.add(DateFormat.YEAR_FIELD, 2);
        final Date startDate = startCalendar.getTime();
        final Date endDate = endCalendar.getTime();
        final Integer times = 41;
        final Long period = 42L;
        final Long initialDelay = 43L;

        IntervalTrigger trigger = new SimpleIntervalTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(startDate);
        trigger.setEndDate(endDate);
        trigger.setTimes(times);
        trigger.setPeriod(period);
        trigger.setInitialDelay(initialDelay);

        TriggerWrapper wrapper = client.addIntervallTrigger("TestName.TestGroup", trigger, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        IntervalTrigger triggerResult = (IntervalTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(name, triggerResult.getName());
        Assert.assertEquals(group, triggerResult.getTriggerGroup());
        Assert.assertEquals(priority.intValue(), triggerResult.getPriority().intValue());
        Assert.assertEquals(description, triggerResult.getDescription());
        Assert.assertEquals(startDate, triggerResult.getStartDate());
        Assert.assertEquals(endDate, triggerResult.getEndDate());
        Assert.assertEquals(times, triggerResult.getTimes());
        Assert.assertEquals(period, triggerResult.getPeriod());
        Assert.assertEquals(initialDelay, triggerResult.getInitialDelay());
    }

    @Test
    public void testAddDelayTrigger() {

        final String name = "TestName";
        final String group = "TestGroup";
        final Integer priority = 3;
        final String description = "description";
        Calendar startCalendar = new GregorianCalendar();
        startCalendar.add(DateFormat.YEAR_FIELD, 1);
        startCalendar.set(Calendar.MILLISECOND, 0);  // Necessary because Quartz ignores milliseconds.
        Calendar endCalendar = new GregorianCalendar();
        endCalendar.add(DateFormat.YEAR_FIELD, 2);
        endCalendar.set(Calendar.MILLISECOND, 0);  // Necessary because Quartz ignores milliseconds.
        final Date startDate = startCalendar.getTime();
        final Date endDate = endCalendar.getTime();
        final Integer times = 41;
        final Long delay = 42L;
        final Long initialDelay = 43L;

        DelayTrigger trigger = new SimpleDelayTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(startDate);
        trigger.setEndDate(endDate);
        trigger.setTimes(times);
        trigger.setDelay(delay);
        trigger.setInitialDelay(initialDelay);

        TriggerWrapper wrapper = client.addDelayTrigger("TestName.TestGroup", trigger, ctx);
        DelayTrigger triggerResult = (DelayTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(1, wrapper.getCount().intValue());
        Assert.assertEquals(name, triggerResult.getName());
        Assert.assertEquals(group, triggerResult.getTriggerGroup());
        Assert.assertEquals(priority.intValue(), triggerResult.getPriority().intValue());
        Assert.assertEquals(description, triggerResult.getDescription());
        Assert.assertEquals(startDate, triggerResult.getStartDate());
        Assert.assertEquals(endDate, triggerResult.getEndDate());
        Assert.assertEquals(times, triggerResult.getTimes());
        Assert.assertEquals(delay, triggerResult.getDelay());
        Assert.assertEquals(initialDelay, triggerResult.getInitialDelay());
    }

    @Test
    public void testAddNowTrigger() {

        final String name = "TestName";
        final String group = "TestGroup";
        final Integer priority = 3;
        final String description = "description";

        NowTrigger trigger = new SimpleNowTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);

        TriggerWrapper wrapper = client.addNowTrigger("TestName.TestGroup", trigger, ctx);
        NowTrigger triggerResult = (NowTrigger) wrapper.getWrappedEntities().get(0);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        Assert.assertEquals(name, triggerResult.getName());
        Assert.assertEquals(group, triggerResult.getTriggerGroup());
        Assert.assertEquals(priority.intValue(), triggerResult.getPriority().intValue());
        Assert.assertEquals(description, triggerResult.getDescription());
    }

    @Test
    public void testAddAtTrigger() {

        final String name = "TestName";
        final String group = "TestGroup";
        final Integer priority = 3;
        final String description = "description";

        AtTrigger trigger = new SimpleAtTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        Calendar startCalendar = new GregorianCalendar();
        startCalendar.add(DateFormat.YEAR_FIELD, 1);
        final Date startDate = startCalendar.getTime();
        trigger.setStartDate(startDate);

        TriggerWrapper wrapper = client.addAtTrigger("TestName.TestGroup", trigger, ctx);
        AtTrigger triggerResult = (AtTrigger) wrapper.getWrappedEntities().get(0);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        Assert.assertEquals(name, triggerResult.getName());
        Assert.assertEquals(group, triggerResult.getTriggerGroup());
        Assert.assertEquals(priority.intValue(), triggerResult.getPriority().intValue());
        Assert.assertEquals(description, triggerResult.getDescription());
        Assert.assertEquals(startDate, triggerResult.getStartDate());
    }
}
