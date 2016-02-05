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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.scheduler.service.client.impl.SchedulerRestClient;
import edu.kit.dama.rest.scheduler.test.util.DummyJob;
import edu.kit.dama.rest.scheduler.types.ScheduleWrapper;
import edu.kit.dama.scheduler.api.impl.QuartzSchedule;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import org.junit.AfterClass;

/**
 *
 * @author mf6319
 */
public class SchedulerRestServiceAddScheduleTest {

    private static final String KEY = "admin";
    private static final String SECRET = "dama14";
    private static final String ROOT_URL = "http://localhost:8080/RESTIntegrationTest/rest/scheduler";

    private static final String QUARTZ_DEFAULT_GROUP = "DEFAULT";

    private static final String SCHEDULE_NAME_1 = "scheduleName1";
    private static final String SCHEDULE_NAME_2 = "scheduleName2";
    private static final String SCHEDULE_NAME_3 = "scheduleName3";

    private static final String SCHEDULE_GROUP = "scheduleGroup";
    private static final String SCHEDULE_ID_1 = SCHEDULE_GROUP + "." + SCHEDULE_NAME_1;
    private static final String SCHEDULE_ID_2 = QUARTZ_DEFAULT_GROUP + "." + SCHEDULE_NAME_2;
    private static final String SCHEDULE_ID_3 = QUARTZ_DEFAULT_GROUP + "." + SCHEDULE_NAME_3;

    private static final String SCHEDULE_DESCRIPTION = "scheduleDescription";
    private static final String SCHEDULE_JOB_CLASS = DummyJob.class.getName();
    private static final String SCHEDULE_JOB_PARAMETERS = "jobParameters";

    private static SchedulerRestClient client;
    private static SimpleRESTContext ctx;

    @BeforeClass
    public static void init() {
        ctx = new SimpleRESTContext(KEY, SECRET);
        client = new SchedulerRestClient(ROOT_URL, ctx);

        ScheduleWrapper wrapper = client.getAllSchedules(ctx);
        for (SimpleSchedule schedule : wrapper.getWrappedEntities()) {
            client.deleteSchedule(schedule.getId(), ctx);
        }
    }

    @AfterClass
    public static void clear() {
        ScheduleWrapper wrapper = client.getAllSchedules(ctx);
        for (SimpleSchedule schedule : wrapper.getWrappedEntities()) {
            client.deleteSchedule(schedule.getId(), ctx);
        }
    }

    @Test
    public void testAddScheduleAllParameters() {

        SimpleSchedule schedule = new QuartzSchedule();
        schedule.setName(SCHEDULE_NAME_1);
        schedule.setScheduleGroup(SCHEDULE_GROUP);
        schedule.setDescription(SCHEDULE_DESCRIPTION);
        schedule.setJobClass(SCHEDULE_JOB_CLASS);
        schedule.setJobParameters(SCHEDULE_JOB_PARAMETERS);

        ScheduleWrapper wrapper = client.addSchedule(schedule, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        SimpleSchedule scheduleResult = (SimpleSchedule) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(SCHEDULE_ID_1, scheduleResult.getId());
        Assert.assertEquals(SCHEDULE_NAME_1, scheduleResult.getName());
        Assert.assertEquals(SCHEDULE_GROUP, scheduleResult.getScheduleGroup());
        Assert.assertEquals(SCHEDULE_DESCRIPTION, scheduleResult.getDescription());
        Assert.assertEquals(SCHEDULE_JOB_CLASS, scheduleResult.getJobClass());
        Assert.assertEquals(SCHEDULE_JOB_PARAMETERS, scheduleResult.getJobParameters());
    }

    @Test
    public void testAddScheduleMinimalParametersWithNull() {

        SimpleSchedule schedule = new QuartzSchedule();
        schedule.setName(SCHEDULE_NAME_2);
        schedule.setScheduleGroup(null);
        schedule.setDescription(null);
        schedule.setJobClass(SCHEDULE_JOB_CLASS);
        schedule.setJobParameters(null);

        ScheduleWrapper wrapper = client.addSchedule(schedule, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        SimpleSchedule scheduleResult = (SimpleSchedule) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(SCHEDULE_ID_2, scheduleResult.getId());
        Assert.assertEquals(SCHEDULE_NAME_2, scheduleResult.getName());
        Assert.assertEquals(QUARTZ_DEFAULT_GROUP, scheduleResult.getScheduleGroup());
        Assert.assertNull(scheduleResult.getDescription());
        Assert.assertEquals(SCHEDULE_JOB_CLASS, scheduleResult.getJobClass());
        Assert.assertNull(scheduleResult.getJobParameters());
    }

    @Test
    public void testAddScheduleMinimalParametersWithoutNull() {

        SimpleSchedule schedule = new QuartzSchedule();
        schedule.setName(SCHEDULE_NAME_3);
        schedule.setJobClass(SCHEDULE_JOB_CLASS);

        ScheduleWrapper wrapper = client.addSchedule(schedule, ctx);
        Assert.assertEquals(1, wrapper.getCount().intValue());

        SimpleSchedule scheduleResult = (SimpleSchedule) wrapper.getWrappedEntities().get(0);
        Assert.assertEquals(SCHEDULE_ID_3, scheduleResult.getId());
        Assert.assertEquals(SCHEDULE_NAME_3, scheduleResult.getName());
        Assert.assertEquals(QUARTZ_DEFAULT_GROUP, scheduleResult.getScheduleGroup());
        Assert.assertNull(scheduleResult.getDescription());
        Assert.assertEquals(SCHEDULE_JOB_CLASS, scheduleResult.getJobClass());
        Assert.assertNull(scheduleResult.getJobParameters());
    }
}
