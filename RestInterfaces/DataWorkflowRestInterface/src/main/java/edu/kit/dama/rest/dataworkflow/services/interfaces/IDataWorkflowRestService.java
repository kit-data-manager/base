/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.rest.dataworkflow.services.interfaces;

import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowConfiguration;
import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultExecutionEnvironment;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.util.Constants;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author mf6319
 */
@Path("/rest/dataworkflow")
public interface IDataWorkflowRestService extends ICommonRestInterface {

    /**
     * Get a DataWorkflow task by its id.
     *
     * @summary Get a DataWorkflow task by its id.
     *
     * @param groupId The id of the group used to access the task.
     * @param id The id of the task.
     * @param hc The HttpContext for OAuth check.
     *
     * @return An DataWorkflowTaskWrapper object.
     *
     * @see edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper
     */
    @GET
    @Path(value = "/tasks/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowTask>")
    IEntityWrapper<? extends IDefaultDataWorkflowTask> getTaskById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get all accessible DataWorkflow tasks.
     *
     * @summary Get all accessible DataWorkflow tasks.
     *
     * @param groupId The id of the group used to access the tasks.
     * @param first The first task index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of DataWorkflowTaskWrapper objects.
     *
     * @see edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper
     */
    @GET
    @Path(value = "/tasks/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowTask>")
    IEntityWrapper<? extends IDefaultDataWorkflowTask> getAllTasks(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the number of accessible DataWorkflow tasks.
     *
     * @summary Get the number of accessible DataWorkflow tasks.
     *
     * @param groupId The id of the group used to access the tasks.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of DataWorkflowTaskWrapper objects.
     *
     * @see edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper
     */
    @GET
    @Path(value = "/tasks/count/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowTask>")
    IEntityWrapper<? extends IDefaultDataWorkflowTask> getTaskCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new DataWorkflow task. Therefor, the following arguments are
     * needed:
     *
     * <ul>
     * <li>configurationId: The Id of a DataWorkflowTaskConfiguration describing
     * the task and standard parameters.</li>
     * <li>environmentId: The Id of the ExecutionEnvironmentConfiguration
     * responsible for executing the task.</li>
     * <li>predecessorId: The Id of the DataWorkflowTask that is predecessor of
     * this task. This parameter is optional.</li>
     * <li>inputObjectMap: A map of input object id (key) and an associated data
     * organization view name (value). The format of this string can be either
     * in the form
     * <i>1=default\n2=customView</i> or
     * <i>[{"1":"default"},{"2":"customView"}]</i>. Furthermore, the baseId as
     * well as the digitalObjectIdentifier are supported as keys.
     * </li>
     * <li>executionSettings: A map of custom execution settings. The according
     * properties file will be stored in the working directory of the task
     * execution and can be read by the user application. The format of this
     * string can be either in the form
     * <i>key1=value1\nkey2=value2</i> or
     * <i>[{"key1":"value1"},{"key2":"value2"}]</i>
     * This argument should be optional in most cases.</li>
     * <li>applicationArguments: Custom application arguments that are provided
     * during the user application execution. The value can be provided as space
     * separated string. This argument should be optional in most cases.</li>
     * </ul>
     *
     * After successful creation the task execution will be performed within one
     * of the next processing cycles. As the processing of each task takes
     * multiple processing cycles a regular check of the task status will be
     * necessary.
     *
     * @summary Create a new DataWorkflow task.
     *
     * @param groupId The id of the group the node belongs to.
     * @param investigationId The investigation the output object of the task is
     * assigned to.
     * @param configurationId The task configuration id.
     * @param environmentId The execution environment configuration id.
     * @param predecessorId The predecessor task id.
     * @param inputObjectMap The input object map.
     * @param executionSettings The execution settings map.
     * @param applicationArguments The application arguments.
     *
     * @param hc The HttpContext for OAuth check.
     *
     * @return A DataWorkflowTaskWrapper object.
     *
     * @see edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper
     */
    @POST
    @Path(value = "/tasks/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowTask>")
    IEntityWrapper<? extends IDefaultDataWorkflowTask> createTask(
            @FormParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("investigationId") Long investigationId,
            @FormParam("configurationId") Long configurationId,
            @FormParam("environmentId") Long environmentId,
            @FormParam("predecessorId") Long predecessorId,
            @FormParam("inputObjectMap") String inputObjectMap,
            @FormParam("executionSettings") String executionSettings,
            @FormParam("applicationArguments") String applicationArguments,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a task configurations by its id.
     *
     * @summary Get a task configuration by its id.
     *
     * @param groupId The id of the group the task configurations belong to
     * (default: USERS).
     * @param id The id of the task configuration.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A DataWorkflowTaskConfigurationWrapper object containing all
     * results.
     *
     * @see
     * edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper
     */
    @GET
    @Path(value = "/configurations/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowConfiguration>")
    IEntityWrapper<? extends IDefaultDataWorkflowConfiguration> getTaskConfigurationById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get all task configurations accessible by the group with the id
     * <i>groupId</i> beginning with index
     * <i>first</i>. The max. number of results is defined by <i>results</i>.
     *
     * @summary Get all task configurations.
     *
     * @param groupId The id of the group the task configurations belong to
     * (default: USERS).
     * @param first The first index (default: 0).
     * @param results The max. number of results (default: 10).
     * @param hc The HttpContext for OAuth check.
     *
     * @return A DataWorkflowTaskConfigurationWrapper object containing all
     * results.
     *
     * @see
     * edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper
     */
    @GET
    @Path(value = "/configurations/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowConfiguration>")
    IEntityWrapper<? extends IDefaultDataWorkflowConfiguration> getAllTaskConfigurations(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the number of task configurations accessible by the group with the id
     * <i>groupId</i>.
     *
     * @summary Get the task configuration count.
     *
     * @param groupId The id of the group the task configurations belong to.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A DataWorkflowTaskConfigurationWrapper object containing all
     * results.
     *
     * @see
     * edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper
     */
    @GET
    @Path(value = "/configurations/count/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowConfiguration>")
    IEntityWrapper<? extends IDefaultDataWorkflowConfiguration> getTaskConfigurationCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get an execution environment configurations by its id.
     *
     * @summary Get an execution environment configuration by its id.
     *
     * @param groupId The id of the group the environment configurations belong
     * to (default: USERS).
     * @param id The id of the execution environment configuration.
     * @param hc The HttpContext for OAuth check.
     *
     * @return An ExecutionEnvironmentConfigurationWrapper object containing all
     * results.
     *
     * @see
     * edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper
     */
    @GET
    @Path(value = "/environments/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultExecutionEnvironment>")
    IEntityWrapper<? extends IDefaultExecutionEnvironment> getExecutionEnvironmentConfigurationById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get all execution environment configurations accessible by the group with
     * the id <i>groupId</i> beginning with index
     * <i>first</i>. The max. number of results is defined by <i>results</i>.
     *
     * @summary Get all execution environment configurations.
     *
     * @param groupId The id of the group the environment configurations belong
     * to (default: USERS).
     * @param first The first index (default: 0).
     * @param results The max. number of results (default: 10).
     * @param hc The HttpContext for OAuth check.
     *
     * @return An ExecutionEnvironmentConfigurationWrapper object containing all
     * results.
     *
     * @see
     * edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper
     */
    @GET
    @Path(value = "/environments/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultExecutionEnvironment>")
    IEntityWrapper<? extends IDefaultExecutionEnvironment> getAllExecutionEnvironmentConfigurations(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the number of execution environment configurations accessible by the
     * group with the id <i>groupId</i>.
     *
     * @summary Get the execution environment configuration count.
     *
     * @param groupId The id of the group the environment configurations belong
     * to.
     * @param hc The HttpContext for OAuth check.
     *
     * @return An ExecutionEnvironmentConfigurationWrapper object containing all
     * results.
     *
     * @see
     * edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper
     */
    @GET
    @Path(value = "/environments/count/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultExecutionEnvironment>")
    IEntityWrapper<? extends IDefaultExecutionEnvironment> getExecutionEnvironmentConfigurationCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc);
}
