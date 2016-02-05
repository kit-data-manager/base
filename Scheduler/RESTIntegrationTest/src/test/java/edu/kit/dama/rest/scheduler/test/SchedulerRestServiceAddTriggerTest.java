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

import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.scheduler.service.client.impl.SchedulerRestClient;
import edu.kit.dama.rest.scheduler.test.util.DummyJob;
import edu.kit.dama.rest.scheduler.types.ScheduleWrapper;
import edu.kit.dama.rest.scheduler.types.TriggerWrapper;
import edu.kit.dama.scheduler.api.impl.QuartzAtTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzExpressionTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzIntervalTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzNowTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzSchedule;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.AtTrigger;
import edu.kit.dama.scheduler.api.trigger.ExpressionTrigger;
import edu.kit.dama.scheduler.api.trigger.IntervalTrigger;
import edu.kit.dama.scheduler.api.trigger.NowTrigger;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.AfterClass;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mf6319
 */
public class SchedulerRestServiceAddTriggerTest {

    private static final String KEY = "admin";
    private static final String SECRET = "dama14";
    private static final String ROOT_URL = "http://localhost:8080/RESTIntegrationTest/rest/scheduler";

    private static final String SCHEDULE_NAME = "scheduleName";
    private static final String SCHEDULE_GROUP = "scheduleGroup";
    private static final String SCHEDULE_ID = SCHEDULE_GROUP + "." + SCHEDULE_NAME;

    private static final String SCHEDULE_DESCRIPTION = "scheduleDescription";
    private static final String SCHEDULE_JOB_CLASS = DummyJob.class.getName();
    private static final String SCHEDULE_JOB_PARAMETERS = "jobParameters";

    private static final String EXPRESSION_TRIGGER_NAME = "expressionTriggerName";
    private static final String EXPRESSION_TRIGGER_GROUP = "expressionTriggerGroup";
    private static final String EXPRESSION_TRIGGER_ID = EXPRESSION_TRIGGER_GROUP + "." + EXPRESSION_TRIGGER_NAME;
    private static final String EXPRESSION_TRIGGER_EXPRESSION = "0/30 * * * * ?";

    private static final String INTERVAL_TRIGGER_NAME = "intervalTriggerName";
    private static final String INTERVAL_TRIGGER_GROUP = "intervalTriggerGroup";
    private static final String INTERVAL_TRIGGER_ID = INTERVAL_TRIGGER_GROUP + "." + INTERVAL_TRIGGER_NAME;
    private static final Integer INTERVAL_TRIGGER_TIMES = 41;
    private static final Long INTERVAL_TRIGGER_PERIOD = 42L;

    private static final String NOW_TRIGGER_NAME = "nowTriggerName";
    private static final String NOW_TRIGGER_GROUP = "nowTriggerGroup";
    private static final String NOW_TRIGGER_ID = NOW_TRIGGER_GROUP + "." + NOW_TRIGGER_NAME;

    private static final String AT_TRIGGER_NAME = "atTriggerName";
    private static final String AT_TRIGGER_GROUP = "atTriggerGroup";
    private static final String AT_TRIGGER_ID = AT_TRIGGER_GROUP + "." + AT_TRIGGER_NAME;

    private static final String TRIGGER_DESCRIPTION = "triggerDescription";
    private static final Integer TRIGGER_PRIORITY = 42;
    private static final Date TRIGGER_START_DATE;
    private static final Date TRIGGER_END_DATE;

    private static SchedulerRestClient client;
    private static SimpleRESTContext ctx;

    static {
        Calendar startCalendar = new GregorianCalendar();
        startCalendar.add(DateFormat.YEAR_FIELD, 1);
        startCalendar.set(Calendar.MILLISECOND, 0);  // Necessary because Quartz ignores milliseconds.
        TRIGGER_START_DATE = startCalendar.getTime();

        Calendar endCalendar = new GregorianCalendar();
        endCalendar.add(DateFormat.YEAR_FIELD, 2);
        startCalendar.set(Calendar.MILLISECOND, 0); // Necessary because Quartz ignores milliseconds.
        TRIGGER_END_DATE = endCalendar.getTime();
    }

    @BeforeClass
    public static void init() {
        ctx = new SimpleRESTContext(KEY, SECRET);
        client = new SchedulerRestClient(ROOT_URL, ctx);

        ScheduleWrapper wrapper = client.getAllSchedules(ctx);
        for (SimpleSchedule schedule : wrapper.getWrappedEntities()) {
            client.deleteSchedule(schedule.getId(), ctx);
        }

        initSchedule();
    }

    @AfterClass
    public static void clear() {
        ScheduleWrapper wrapper = client.getAllSchedules(ctx);
        for (SimpleSchedule schedule : wrapper.getWrappedEntities()) {
            client.deleteSchedule(schedule.getId(), ctx);
        }
    }

    private static void initSchedule() {

        SimpleSchedule schedule = new QuartzSchedule();
        schedule.setName(SCHEDULE_NAME);
        schedule.setScheduleGroup(SCHEDULE_GROUP);
        schedule.setDescription(SCHEDULE_DESCRIPTION);
        schedule.setJobClass(SCHEDULE_JOB_CLASS);
        schedule.setJobParameters(SCHEDULE_JOB_PARAMETERS);

        client.addSchedule(schedule, ctx);
    }

    @Test
    public void testAddExpressionTriggerAllParameters() {

        final String name = EXPRESSION_TRIGGER_NAME;
        final String group = EXPRESSION_TRIGGER_GROUP;
        final Integer priority = TRIGGER_PRIORITY;
        final String description = TRIGGER_DESCRIPTION;
        final Date startDate = TRIGGER_START_DATE;
        final Date endDate = TRIGGER_END_DATE;
        final String expression = EXPRESSION_TRIGGER_EXPRESSION;

        ExpressionTrigger trigger = new QuartzExpressionTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(startDate);
        trigger.setEndDate(endDate);
        trigger.setExpression(expression);

        TriggerWrapper wrapper = client.addExpressionTrigger(SCHEDULE_ID, trigger, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        ExpressionTrigger triggerResult = (ExpressionTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(EXPRESSION_TRIGGER_ID, triggerResult.getId());
        Assert.assertEquals(name, triggerResult.getName());
        Assert.assertEquals(group, triggerResult.getTriggerGroup());
        Assert.assertEquals(priority.intValue(), triggerResult.getPriority().intValue());
        Assert.assertEquals(description, triggerResult.getDescription());
        Assert.assertEquals(startDate, triggerResult.getStartDate());
        Assert.assertEquals(endDate, triggerResult.getEndDate());
        Assert.assertEquals(expression, triggerResult.getExpression());
    }

    @Test
    public void testAddExpressionTriggerMinimalParametersWithNull() {

        final String name = null;
        final String group = null;
        final String description = null;
        final Integer priority = null;
        final Date startDate = null;
        final Date endDate = null;
        final String expression = EXPRESSION_TRIGGER_EXPRESSION;

        ExpressionTrigger trigger = new QuartzExpressionTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(startDate);
        trigger.setEndDate(endDate);
        trigger.setExpression(expression);

        TriggerWrapper wrapper = client.addExpressionTrigger(SCHEDULE_ID, trigger, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        ExpressionTrigger triggerResult = (ExpressionTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertNotNull(triggerResult.getId());
        Assert.assertNotNull(triggerResult.getName());
        Assert.assertNotNull(triggerResult.getTriggerGroup());
        Assert.assertNotNull(triggerResult.getPriority());
        Assert.assertNull(triggerResult.getDescription());
        Assert.assertNotNull(triggerResult.getStartDate());
        Assert.assertNull(triggerResult.getEndDate());
        Assert.assertEquals(expression, triggerResult.getExpression());
    }

    @Test
    public void testAddExpressionTriggerMinimalParametersWithoutNull() {

        ExpressionTrigger trigger = new QuartzExpressionTrigger();
        trigger.setExpression(EXPRESSION_TRIGGER_EXPRESSION);

        TriggerWrapper wrapper = client.addExpressionTrigger(SCHEDULE_ID, trigger, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        ExpressionTrigger triggerResult = (ExpressionTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertNotNull(triggerResult.getId());
        Assert.assertNotNull(triggerResult.getName());
        Assert.assertNotNull(triggerResult.getTriggerGroup());
        Assert.assertNotNull(triggerResult.getPriority());
        Assert.assertNull(triggerResult.getDescription());
        Assert.assertNotNull(triggerResult.getStartDate());
        Assert.assertNull(triggerResult.getEndDate());
        Assert.assertEquals(EXPRESSION_TRIGGER_EXPRESSION, triggerResult.getExpression());
    }

    @Test
    public void testAddIntervalTriggerAllParameters() {

        final String name = INTERVAL_TRIGGER_NAME;
        final String group = INTERVAL_TRIGGER_GROUP;
        final String description = TRIGGER_DESCRIPTION;
        final Integer priority = TRIGGER_PRIORITY;
        final Date startDate = TRIGGER_START_DATE;
        final Date endDate = TRIGGER_END_DATE;
        final Integer times = INTERVAL_TRIGGER_TIMES;
        final Long period = INTERVAL_TRIGGER_PERIOD;
        //Attribute initialDelay is not supported for QuartzIntervalTriggers.
        //final Long initialDelay = INTERVAL_TRIGGER_INITIAL_DELAY;

        IntervalTrigger trigger = new QuartzIntervalTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(startDate);
        trigger.setEndDate(endDate);
        trigger.setTimes(times);
        trigger.setPeriod(period);
        //trigger.setInitialDelay(initialDelay);

        TriggerWrapper wrapper = client.addIntervallTrigger(SCHEDULE_ID, trigger, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        IntervalTrigger triggerResult = (IntervalTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(INTERVAL_TRIGGER_ID, triggerResult.getId());
        Assert.assertEquals(name, triggerResult.getName());
        Assert.assertEquals(group, triggerResult.getTriggerGroup());
        Assert.assertEquals(priority.intValue(), triggerResult.getPriority().intValue());
        Assert.assertEquals(description, triggerResult.getDescription());
        Assert.assertEquals(startDate, triggerResult.getStartDate());
        Assert.assertEquals(endDate, triggerResult.getEndDate());
        Assert.assertEquals(times, triggerResult.getTimes());
        Assert.assertEquals(period, triggerResult.getPeriod());
        //Assert.assertEquals(initialDelay, triggerResult.getInitialDelay());
    }

    @Test
    public void testAddIntervalTriggerMinimalParametersWithNull() {

        final String name = null;
        final String group = null;
        final Integer priority = null;
        final String description = null;
        final Date startDate = null;
        final Date endDate = null;
        final Integer times = null;
        final Long period = INTERVAL_TRIGGER_PERIOD;
        //Attribute initialDelay is not supported for QuartzIntervalTriggers.
        //final Long initialDelay = INTERVAL_TRIGGER_INITIAL_DELAY;

        IntervalTrigger trigger = new QuartzIntervalTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setPriority(priority);
        trigger.setDescription(description);
        trigger.setStartDate(startDate);
        trigger.setEndDate(endDate);
        trigger.setTimes(times);
        trigger.setPeriod(period);
        //trigger.setInitialDelay(initialDelay);

        TriggerWrapper wrapper = client.addIntervallTrigger(SCHEDULE_ID, trigger, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        IntervalTrigger triggerResult = (IntervalTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertNotNull(triggerResult.getId());
        Assert.assertNotNull(triggerResult.getName());
        Assert.assertNotNull(triggerResult.getTriggerGroup());
        Assert.assertNotNull(triggerResult.getPriority());
        Assert.assertNull(triggerResult.getDescription());
        Assert.assertNotNull(triggerResult.getStartDate());
        Assert.assertNull(triggerResult.getEndDate());
        Assert.assertEquals(-1, triggerResult.getTimes().intValue());
        Assert.assertEquals(period, triggerResult.getPeriod());
        //Assert.assertEquals(initialDelay, triggerResult.getInitialDelay());
    }

    @Test
    public void testAddIntervalTriggerMinimalParametersWithoutNull() {

        IntervalTrigger trigger = new QuartzIntervalTrigger();
        trigger.setPeriod(INTERVAL_TRIGGER_PERIOD);

        TriggerWrapper wrapper = client.addIntervallTrigger(SCHEDULE_ID, trigger, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        IntervalTrigger triggerResult = (IntervalTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertNotNull(triggerResult.getId());
        Assert.assertNotNull(triggerResult.getName());
        Assert.assertNotNull(triggerResult.getTriggerGroup());
        Assert.assertNotNull(triggerResult.getPriority());
        Assert.assertNull(triggerResult.getDescription());
        Assert.assertNotNull(triggerResult.getStartDate());
        Assert.assertNull(triggerResult.getEndDate());
        Assert.assertEquals(-1, triggerResult.getTimes().intValue());
        Assert.assertEquals(INTERVAL_TRIGGER_PERIOD, triggerResult.getPeriod());
        //Assert.assertNull(triggerResult.getInitialDelay());
    }
//    @Test
//    @Ignore
//    public void testAddDelayTrigger() {
    // Trigger type is not implemented in Quartz.
//    }

    @Test
    public void testAddNowTriggerAllParameters() {

        NowTrigger trigger = new QuartzNowTrigger();
        trigger.setName(NOW_TRIGGER_NAME);
        trigger.setTriggerGroup(NOW_TRIGGER_GROUP);
        trigger.setPriority(TRIGGER_PRIORITY);
        trigger.setDescription(TRIGGER_DESCRIPTION);

        TriggerWrapper wrapper = client.addNowTrigger(SCHEDULE_ID, trigger, ctx);
        NowTrigger triggerResult = (NowTrigger) wrapper.getWrappedEntities().get(0);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        Assert.assertEquals(NOW_TRIGGER_ID, triggerResult.getId());
        Assert.assertEquals(NOW_TRIGGER_NAME, triggerResult.getName());
        Assert.assertEquals(NOW_TRIGGER_GROUP, triggerResult.getTriggerGroup());
        Assert.assertEquals(TRIGGER_PRIORITY.intValue(), triggerResult.getPriority().intValue());
        Assert.assertEquals(TRIGGER_DESCRIPTION, triggerResult.getDescription());
    }

    @Test
    public void testAddNowTriggerMinimalParametersWithNull() {

        NowTrigger trigger = new QuartzNowTrigger();
        trigger.setName(null);
        trigger.setTriggerGroup(null);
        trigger.setPriority(null);
        trigger.setDescription(null);

        TriggerWrapper wrapper = client.addNowTrigger(SCHEDULE_ID, trigger, ctx);
        NowTrigger triggerResult = (NowTrigger) wrapper.getWrappedEntities().get(0);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        Assert.assertNotNull(triggerResult.getId());
        Assert.assertNotNull(triggerResult.getName());
        Assert.assertNotNull(triggerResult.getTriggerGroup());
        Assert.assertNotNull(triggerResult.getPriority());
        Assert.assertNull(triggerResult.getDescription());
    }

    @Test
    public void testAddNowTriggerMinimalParametersWithoutNull() {

        NowTrigger trigger = new QuartzNowTrigger();

        TriggerWrapper wrapper = client.addNowTrigger(SCHEDULE_ID, trigger, ctx);
        NowTrigger triggerResult = (NowTrigger) wrapper.getWrappedEntities().get(0);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        Assert.assertNotNull(triggerResult.getId());
        Assert.assertNotNull(triggerResult.getName());
        Assert.assertNotNull(triggerResult.getTriggerGroup());
        Assert.assertNotNull(triggerResult.getPriority());
        Assert.assertNull(triggerResult.getDescription());
    }

    @Test
    public void testAddAtTriggerAllParameters() {

        AtTrigger trigger = new QuartzAtTrigger();
        trigger.setName(AT_TRIGGER_NAME);
        trigger.setTriggerGroup(AT_TRIGGER_GROUP);
        trigger.setPriority(TRIGGER_PRIORITY);
        trigger.setDescription(TRIGGER_DESCRIPTION);
        trigger.setStartDate(TRIGGER_START_DATE);

        TriggerWrapper wrapper = client.addAtTrigger(SCHEDULE_ID, trigger, ctx);
        AtTrigger triggerResult = (QuartzAtTrigger) wrapper.getWrappedEntities().get(0);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        Assert.assertEquals(AT_TRIGGER_ID, triggerResult.getId());
        Assert.assertEquals(AT_TRIGGER_NAME, triggerResult.getName());
        Assert.assertEquals(AT_TRIGGER_GROUP, triggerResult.getTriggerGroup());
        Assert.assertEquals(TRIGGER_PRIORITY.intValue(), triggerResult.getPriority().intValue());
        Assert.assertEquals(TRIGGER_DESCRIPTION, triggerResult.getDescription());
        Assert.assertEquals(TRIGGER_START_DATE, triggerResult.getStartDate());
    }

    @Test
    public void testAddAtTriggerMinimalParametersWithNull() {

        AtTrigger trigger = new QuartzAtTrigger();
        trigger.setName(null);
        trigger.setTriggerGroup(null);
        trigger.setDescription(null);
        trigger.setPriority(null);
        trigger.setStartDate(TRIGGER_START_DATE);

        TriggerWrapper wrapper = client.addAtTrigger(SCHEDULE_ID, trigger, ctx);
        AtTrigger triggerResult = (QuartzAtTrigger) wrapper.getWrappedEntities().get(0);

        Assert.assertNotNull(triggerResult.getName());
        Assert.assertNotNull(triggerResult.getId());
        Assert.assertNotNull(triggerResult.getTriggerGroup());
        Assert.assertNotNull(triggerResult.getPriority());
        Assert.assertNull(triggerResult.getDescription());
        Assert.assertNotNull(triggerResult.getStartDate());
    }

    @Test
    public void testAddAtTriggerMinimalParametersWithoutNull() {

        AtTrigger trigger = new QuartzAtTrigger();
        trigger.setStartDate(TRIGGER_START_DATE);

        TriggerWrapper wrapper = client.addAtTrigger(SCHEDULE_ID, trigger, ctx);
        AtTrigger triggerResult = (QuartzAtTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertNotNull(triggerResult.getId());
        Assert.assertNotNull(triggerResult.getName());
        Assert.assertNotNull(triggerResult.getTriggerGroup());
        Assert.assertNotNull(triggerResult.getPriority());
        Assert.assertNull(triggerResult.getDescription());
        Assert.assertNotNull(triggerResult.getStartDate());
    }
}
