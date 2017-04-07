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
package edu.kit.dama.rest.scheduler.services.interfaces;

import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;

import edu.kit.dama.rest.base.ICommonRestInterface;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author wq7203
 */
@Path("/rest/scheduler")
public interface ISchedulerRestService extends ICommonRestInterface {

    /**
     * Get a list of registered schedules.
     *
     * @summary Get a list of registered schedules.
     *
     * @param hc the HttpContext for OAuth check.
     *
     * @return a list of registered schedules.
     */
    @GET
    @Path(value = "/schedules")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.ScheduleWrapper")
    StreamingOutput getAllSchedules(@javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single schedule by its id.
     *
     * @summary Get a schedule by its id.
     *
     * @param id the id of the schedule.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the schedule with the provided id.
     */
    @GET
    @Path(value = "/schedules/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.ScheduleWrapper")
    StreamingOutput getScheduleById(
            @PathParam("id") String id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a list of registered triggers.
     *
     * @summary Get a list of registered triggers
     *
     * @param hc the HttpContext for OAuth check.
     *
     * @return a list of registered triggers.
     */
    @GET
    @Path(value = "/triggers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.TriggerWrapper")
    StreamingOutput getAllTriggers(@javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a single trigger by its id.
     *
     * @summary Get a trigger by its id.
     *
     * @param id the id of the trigger.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the trigger with the provided id.
     */
    @GET
    @Path(value = "/triggers/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.TriggerWrapper")
    StreamingOutput getTriggerById(
            @PathParam("id") String id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a list of triggers for a schedule with the provided id.
     *
     * @summary Get a list of triggers for a schedule with the provided id.
     *
     * @param id the id of the schedule.
     * @param hc the HttpContext for OAuth check.
     *
     * @return a list of triggers for the provided schedule id.
     */
    @GET
    @Path(value = "/schedules/{id}/triggers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.TriggerWrapper")
    StreamingOutput getTriggersByScheduleId(
            @PathParam("id") String id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a list of currently executing jobs.
     *
     * @summary Get a list of currently executing jobs.
     *
     * @param hc the HttpContext for OAuth check.
     *
     * @return a list of currently executing jobs.
     */
    @GET
    @Path(value = "/currentlyExecutingJobs")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.ScheduleWrapper")
    StreamingOutput getCurrentlyExecutingJobs(@javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new schedule with a initial expression trigger via POST request.
     *
     * @summary Create a new schedule via POST request.
     *
     * @param name the schedule name.
     * @param group the schedule group.
     * @param description the schedule description.
     * @param jobClass the job class.
     * @param jobParameters the job parameters.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the created schedule.
     */
    @POST
    @Path(value = "/schedules")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.ScheduleWrapper")
    StreamingOutput addSchedule(
            @NotNull @FormParam("name") String name,
            @FormParam("group") String group,
            @FormParam("description") String description,
            @NotNull @FormParam("jobClass") String jobClass,
            @FormParam("jobParameters") String jobParameters,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new expression trigger and add it to the provided schedule via
     * POST request.
     *
     * @summary Create a new expression trigger and add it to the provided
     * schedule via POST request.
     *
     * @param id the schedule id.
     * @param name the trigger name.
     * @param group the trigger group.
     * @param priority the trigger priority.
     * @param description the trigger description.
     * @param startDate the trigger start date.
     * @param endDate the trigger end date.
     * @param expression the trigger expression.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the created expression trigger.
     */
    @POST
    @Path(value = "/schedules/{id}/expressionTriggers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.TriggerWrapper")
    StreamingOutput addExpressionTrigger(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("group") String group,
            @FormParam("priority") Integer priority,
            @FormParam("description") String description,
            @FormParam("startDate") String startDate,
            @FormParam("endDate") String endDate,
            @NotNull @FormParam("expression") String expression,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new intervall trigger and add it to the provided schedule via
     * POST request.
     *
     * @summary Create a new intervall trigger and add it to the provided
     * schedule via POST request.
     *
     * @param id the schedule id.
     * @param name the trigger name.
     * @param group the trigger group.
     * @param priority the trigger priority.
     * @param description the trigger description.
     * @param startDate the trigger start date.
     * @param endDate the trigger end date.
     * @param times the number of times.
     * @param period the trigger period.
     * @param initialDelay the trigger initial delay.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the created intervall trigger.
     */
    @POST
    @Path(value = "/schedules/{id}/intervalTriggers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.TriggerWrapper")
    StreamingOutput addIntervallTrigger(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("group") String group,
            @FormParam("priority") Integer priority,
            @FormParam("description") String description,
            @FormParam("startDate") String startDate,
            @FormParam("endDate") String endDate,
            @FormParam("times") Integer times,
            @NotNull @FormParam("period") Long period,
            @FormParam("initialDelay") Long initialDelay,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new delay trigger and add it to the provided schedule via POST
     * request.
     *
     * @param id the schedule id.
     * @param name the trigger name.
     * @param group the trigger group.
     * @param priority the trigger priority.
     * @param description the trigger description.
     * @param startDate the trigger start date.
     * @param endDate the trigger end date.
     * @param times the number of times.
     * @param delay the trigger delay.
     * @param initialDelay the trigger initial delay.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the created delay trigger.
     */
    @POST
    @Path(value = "/schedules/{id}/delayTriggers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.TriggerWrapper")
    StreamingOutput addDelayTrigger(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("group") String group,
            @FormParam("priority") Integer priority,
            @FormParam("description") String description,
            @FormParam("startDate") String startDate,
            @FormParam("endDate") String endDate,
            @FormParam("times") Integer times,
            @NotNull @FormParam("delay") Long delay,
            @FormParam("initialDelay") Long initialDelay,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new now-trigger and add it to the provided schedule via POST
     * request.
     *
     * @param id the schedule id.
     * @param name the trigger name.
     * @param group the trigger group.
     * @param priority the trigger priority.
     * @param description the trigger description.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the created now-trigger.
     */
    @POST
    @Path(value = "/schedules/{id}/nowTriggers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.TriggerWrapper")
    StreamingOutput addNowTrigger(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("group") String group,
            @FormParam("priority") Integer priority,
            @FormParam("description") String description,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new at-trigger and add it to the provided schedule via POST
     * request.
     *
     * @param id the schedule id.
     * @param name the trigger name.
     * @param group the trigger group.
     * @param priority the trigger priority.
     * @param description the trigger description.
     * @param startDate the trigger start date.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the created at-trigger.
     */
    @POST
    @Path(value = "/schedules/{id}/atTriggers")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.scheduler.types.TriggerWrapper")
    StreamingOutput addAtTrigger(
            @PathParam("id") String id,
            @FormParam("name") String name,
            @FormParam("group") String group,
            @FormParam("priority") Integer priority,
            @FormParam("description") String description,
            @NotNull @FormParam("startDate") String startDate,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Delete the schedule with the provided id.
     *
     * @summary Delete the schedule with the provided id.
     *
     * @param id the id of the schedule to delete.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the HTTP response with an appropriate HTTP response code.
     */
    @DELETE
    @Path(value = "/schedules/{id}")
    @ReturnType("java.lang.Void")
    Response deleteScheduleById(
            @PathParam("id") String id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Delete the trigger with the provided id.
     *
     * @summary Delete the trigger with the provided id.
     *
     * @param id the id of the trigger to delete.
     * @param hc the HttpContext for OAuth check.
     *
     * @return the HTTP response with an appropriate HTTP response code.
     */
    @DELETE
    @Path(value = "/triggers/{id}")
    @ReturnType("java.lang.Void")
    Response deleteTriggerById(
            @PathParam("id") String id,
            @javax.ws.rs.core.Context HttpContext hc);
}
