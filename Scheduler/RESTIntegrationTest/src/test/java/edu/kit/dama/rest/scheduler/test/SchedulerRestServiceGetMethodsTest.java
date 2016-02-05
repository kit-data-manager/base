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
import edu.kit.dama.scheduler.api.impl.QuartzSchedule;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.scheduler.api.trigger.AtTrigger;
import edu.kit.dama.scheduler.api.trigger.ExpressionTrigger;
import edu.kit.dama.scheduler.api.trigger.IntervalTrigger;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 *
 * @author mf6319
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SchedulerRestServiceGetMethodsTest {

    private static SchedulerRestClient client;
    private static SimpleRESTContext ctx;

    private static final String KEY = "admin";
    private static final String SECRET = "dama14";
    private static final String ROOT_URL = "http://localhost:8080/RESTIntegrationTest/rest/scheduler";

    private static final String SCHEDULE_NAME = "scheduleName";
    private static final String SCHEDULE_NAME_2 = "scheduleName2";
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
    // Attribute initialDelay is not supported for QuartzIntervalTriggers.
    // private static final Long INTERVAL_TRIGGER_INITIAL_DELAY = 43L;

    // private static final String NOW_TRIGGER_NAME = "nowTriggerName";
    // private static final String NOW_TRIGGER_GROUP = "nowTriggerGroup";
    private static final String AT_TRIGGER_NAME = "atTriggerName";
    private static final String AT_TRIGGER_GROUP = "atTriggerGroup";
    private static final String AT_TRIGGER_ID = AT_TRIGGER_GROUP + "." + AT_TRIGGER_NAME;

    private static final String TRIGGER_DESCRIPTION = "triggerDescription";
    private static final Date TRIGGER_START_DATE;
    private static final Date TRIGGER_END_DATE;

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
        initExpressionTrigger();
        initIntervalTrigger();
        initAtTrigger();
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

        SimpleSchedule schedule2 = new QuartzSchedule();
        schedule2.setName(SCHEDULE_NAME_2);
        schedule2.setJobClass(SCHEDULE_JOB_CLASS);

        client.addSchedule(schedule2, ctx);
    }

    private static void initExpressionTrigger() {

        final String name = EXPRESSION_TRIGGER_NAME;
        final String group = EXPRESSION_TRIGGER_GROUP;
        final String description = TRIGGER_DESCRIPTION;
        final Date startDate = TRIGGER_START_DATE;
        final Date endDate = TRIGGER_END_DATE;
        final String expression = EXPRESSION_TRIGGER_EXPRESSION;

        ExpressionTrigger trigger = new QuartzExpressionTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setDescription(description);
        trigger.setStartDate(startDate);
        trigger.setEndDate(endDate);
        trigger.setExpression(expression);

        client.addExpressionTrigger(SCHEDULE_ID, trigger, ctx);
    }

    private static void initIntervalTrigger() {

        final String name = INTERVAL_TRIGGER_NAME;
        final String group = INTERVAL_TRIGGER_GROUP;
        final String description = TRIGGER_DESCRIPTION;
        final Date startDate = TRIGGER_START_DATE;
        final Date endDate = TRIGGER_END_DATE;
        final Integer times = INTERVAL_TRIGGER_TIMES;
        final Long period = INTERVAL_TRIGGER_PERIOD;
        //Attribute initialDelay is not supported for QuartzIntervalTriggers.
        //final Long initialDelay = INTERVAL_TRIGGER_INITIAL_DELAY;

        IntervalTrigger trigger = new QuartzIntervalTrigger();
        trigger.setName(name);
        trigger.setTriggerGroup(group);
        trigger.setDescription(description);
        trigger.setStartDate(startDate);
        trigger.setEndDate(endDate);
        trigger.setTimes(times);
        trigger.setPeriod(period);
        //trigger.setInitialDelay(INTERVAL_TRIGGER_INITIAL_DELAY);

        client.addIntervallTrigger(SCHEDULE_ID, trigger, ctx);
    }

    private static void initAtTrigger() {

        AtTrigger trigger = new QuartzAtTrigger();
        trigger.setName(AT_TRIGGER_NAME);
        trigger.setTriggerGroup(AT_TRIGGER_GROUP);
        trigger.setDescription(TRIGGER_DESCRIPTION);
        trigger.setStartDate(TRIGGER_START_DATE);

        client.addAtTrigger(SCHEDULE_ID, trigger, ctx);
    }

    @Test
    public void testGetAllSchedules() {

        ScheduleWrapper wrapper = client.getAllSchedules(ctx);

        Assert.assertEquals(2, wrapper.getCount().intValue());
        Assert.assertEquals(2, wrapper.getEntities().size());
        Assert.assertEquals(2, wrapper.getWrappedEntities().size());
    }

    @Test
    public void testGetScheduleById() {

        ScheduleWrapper wrapper = client.getScheduleById(SCHEDULE_ID, ctx);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        Assert.assertEquals(1, wrapper.getEntities().size());
        Assert.assertEquals(1, wrapper.getWrappedEntities().size());

        SimpleSchedule scheduleResult = (SimpleSchedule) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(SCHEDULE_ID, scheduleResult.getId());
        Assert.assertEquals(SCHEDULE_NAME, scheduleResult.getName());
        Assert.assertEquals(SCHEDULE_GROUP, scheduleResult.getScheduleGroup());
        Assert.assertEquals(SCHEDULE_DESCRIPTION, scheduleResult.getDescription());
        Assert.assertEquals(SCHEDULE_JOB_CLASS, scheduleResult.getJobClass());
        Assert.assertEquals(SCHEDULE_JOB_PARAMETERS, scheduleResult.getJobParameters());
    }

    @Test
    public void testGetAllTriggers() {

        TriggerWrapper wrapper = client.getAllTriggers(ctx);

        Assert.assertEquals(3, wrapper.getCount().intValue());
        Assert.assertEquals(3, wrapper.getEntities().size());
        Assert.assertEquals(3, wrapper.getWrappedEntities().size());
    }

    @Test
    public void testGetExpressionTriggerById() {

        TriggerWrapper wrapper = client.getTriggerById(EXPRESSION_TRIGGER_ID, ctx);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        ExpressionTrigger triggerResult = (ExpressionTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(EXPRESSION_TRIGGER_ID, triggerResult.getId());
        Assert.assertEquals(EXPRESSION_TRIGGER_NAME, triggerResult.getName());
        Assert.assertEquals(EXPRESSION_TRIGGER_GROUP, triggerResult.getTriggerGroup());
        Assert.assertEquals(TRIGGER_DESCRIPTION, triggerResult.getDescription());
        Assert.assertEquals(TRIGGER_START_DATE, triggerResult.getStartDate());
        Assert.assertEquals(TRIGGER_END_DATE, triggerResult.getEndDate());
        Assert.assertEquals(EXPRESSION_TRIGGER_EXPRESSION, triggerResult.getExpression());
    }

    @Test
    public void testGetIntervalTriggerById() {

        TriggerWrapper wrapper = client.getTriggerById(INTERVAL_TRIGGER_ID, ctx);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        IntervalTrigger triggerResult = (IntervalTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(INTERVAL_TRIGGER_ID, triggerResult.getId());
        Assert.assertEquals(INTERVAL_TRIGGER_NAME, triggerResult.getName());
        Assert.assertEquals(INTERVAL_TRIGGER_GROUP, triggerResult.getTriggerGroup());
        Assert.assertEquals(TRIGGER_DESCRIPTION, triggerResult.getDescription());
        Assert.assertEquals(TRIGGER_START_DATE, triggerResult.getStartDate());
        Assert.assertEquals(TRIGGER_END_DATE, triggerResult.getEndDate());
        Assert.assertEquals(INTERVAL_TRIGGER_TIMES, triggerResult.getTimes());
        Assert.assertEquals(INTERVAL_TRIGGER_PERIOD, triggerResult.getPeriod());
        //Assert.assertEquals(INTERVAL_TRIGGER_INITIAL_DELAY, triggerResult.getInitialDelay());
    }

    @Test
    public void testGetAtTriggerById() {

        TriggerWrapper wrapper = client.getTriggerById(AT_TRIGGER_ID, ctx);

        Assert.assertEquals(1, wrapper.getCount().intValue());
        IntervalTrigger triggerResult = (IntervalTrigger) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(1, wrapper.getCount().intValue());
        Assert.assertEquals(AT_TRIGGER_ID, triggerResult.getId());
        Assert.assertEquals(AT_TRIGGER_NAME, triggerResult.getName());
        Assert.assertEquals(AT_TRIGGER_GROUP, triggerResult.getTriggerGroup());
        Assert.assertEquals(TRIGGER_DESCRIPTION, triggerResult.getDescription());
        Assert.assertEquals(TRIGGER_START_DATE, triggerResult.getStartDate());
    }

    @Test
    public void testGetTriggersByScheduleId() {

        TriggerWrapper wrapper = client.getTriggersBySchedulerId(SCHEDULE_ID, ctx);

        Assert.assertEquals(3, wrapper.getCount().intValue());
        Assert.assertEquals(3, wrapper.getEntities().size());
        Assert.assertEquals(3, wrapper.getWrappedEntities().size());
    }
}
