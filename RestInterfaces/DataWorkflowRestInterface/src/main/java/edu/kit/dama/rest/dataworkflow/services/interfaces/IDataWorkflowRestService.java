/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.util.Constants;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author mf6319
 */
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
   * @return An DataWorkflowTaskWrapper object serialized using the
   * <b>default</b>
   * object graph of DataWorkflowTaskWrapper, which contains all attributes but
   * complex attributes. For complex attributes the ids of the sub-entity are
   * returned and can be used for additional queries.
   *
   * @see edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper
   */
  @GET
  @Path(value = "/tasks/{id}")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper")
  StreamingOutput getTaskById(
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
   * @return A list of DataWorkflowTaskWrapper objects serialized using the
   * <b>simple</b>
   * object graph of DataWorkflowTaskWrapper, which contains only the ids of the
   * task. Details are queried using {@link #getTaskById(java.lang.String, java.lang.Long, com.sun.jersey.api.core.HttpContext)
   * }.
   *
   * @see edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper
   */
  @GET
  @Path(value = "/tasks/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper")
  StreamingOutput getAllTasks(
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
   * @return A list of DataWorkflowTaskWrapper objects serialized using the
   * <b>simple</b>
   * object graph of DataWorkflowTaskWrapper, which contains the number of
   * accessible tasks. }.
   *
   * @see edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper
   */
  @GET
  @Path(value = "/tasks/count/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper")
  StreamingOutput getTaskCount(
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
   * <li>inputObjectMap: A string representing a {@link java.util.Properties}
   * object containing a map of input digital object ids (key) and the data
   * organization view name (value) of the according object used by the task.
   * The object should be serialized using {@link edu.kit.dama.util.PropertiesUtil#propertiesToString(java.util.Properties)
   * } which basically returns the content in the form
   * <i>key1=value1\nkey2=value2</i>. </li>
   * <li>executionSettings: A serialized {@link java.util.Properties} object
   * containing key-value entries of custom execution settings. The according
   * properties file will be stored in the working directory of the task
   * execution and can be read by the user application. The object should be
   * serialized using {@link edu.kit.dama.util.PropertiesUtil#propertiesToString(java.util.Properties)
   * } which basically returns the content in the form
   * <i>key1=value1\nkey2=value2</i>. This argument should be optional in most
   * cases.</li>
   * <li>applicationArguments: Custom application arguments that are provided
   * during the user application execution. This argument should be optional in
   * most cases.</li>
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
   * @param inputObjectMap The serialized input object map.
   * @param executionSettings The serialized execution settings.
   * @param applicationArguments The application arguments.
   *
   * @param hc The HttpContext for OAuth check.
   *
   * @return A DataWorkflowTaskWrapper object serialized using the
   * <b>default</b>
   * object graph of DataWorkflowTaskWrapper, which contains all attributes but
   * complex attributes. For complex attributes the ids of the sub-entity are
   * returned and can be used for additional queries.
   *
   * All returned entities are serialized using the <b>simple</b> object graph
   * of DataOrganizationNodeWrapper, which removes all attributes but the id
   * from the returned entities.
   *
   * @see edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper
   */
  @POST
  @Path(value = "/tasks/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper")
  StreamingOutput createTask(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
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
   * results. The returned entity is serialized using the <b>default</b> object
   * graph of DataWorkflowTaskConfigurationWrapper, which contains all
   * attributes but complex attributes. For complex attributes the ids of the
   * sub-entity are returned and can be used for additional queries.
   *
   * @see
   * edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper
   */
  @GET
  @Path(value = "/configurations/{id}")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper")
  StreamingOutput getTaskConfigurationById(
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
   * results. The returned entity is serialized using the <b>simple</b> object
   * graph of ExecutionEnvironmentConfigurationWrapper, which removes all
   * attributes but the id from the returned entities.
   *
   * @see
   * edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper
   */
  @GET
  @Path(value = "/configurations/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper")
  StreamingOutput getAllTaskConfigurations(
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
   * results. The returned entity is serialized using the <b>simple</b> object
   * graph of DataWorkflowTaskConfigurationWrapper, which removes all attributes
   * but the id from the returned entities.
   *
   * @see
   * edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper
   */
  @GET
  @Path(value = "/configurations/count/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper")
  StreamingOutput getTaskConfigurationCount(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Get an execution environment configurations by its id.
   *
   * @summary Get an execution environment configuration by its id.
   *
   * @param groupId The id of the group the environment configurations belong to
   * (default: USERS).
   * @param id The id of the execution environment configuration.
   * @param hc The HttpContext for OAuth check.
   *
   * @return An ExecutionEnvironmentConfigurationWrapper object containing all
   * results. The returned entity is serialized using the <b>default</b> object
   * graph of ExecutionEnvironmentConfigurationWrapper, which contains all
   * attributes but complex attributes. For complex attributes the ids of the
   * sub-entity are returned and can be used for additional queries.
   *
   * @see
   * edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper
   */
  @GET
  @Path(value = "/environments/{id}")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper")
  StreamingOutput getExecutionEnvironmentConfigurationById(
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
   * @param groupId The id of the group the environment configurations belong to
   * (default: USERS).
   * @param first The first index (default: 0).
   * @param results The max. number of results (default: 10).
   * @param hc The HttpContext for OAuth check.
   *
   * @return An ExecutionEnvironmentConfigurationWrapper object containing all
   * results. The returned entity is serialized using the <b>simple</b> object
   * graph of ExecutionEnvironmentConfigurationWrapper, which removes all
   * attributes but the id from the returned entities.
   *
   * @see
   * edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper
   */
  @GET
  @Path(value = "/environments/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper")
  StreamingOutput getAllExecutionEnvironmentConfigurations(
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
   * results. The returned entity is serialized using the <b>simple</b> object
   * graph of ExecutionEnvironmentConfigurationWrapper, which removes all
   * attributes but the id from the returned entities.
   *
   * @see
   * edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper
   */
  @GET
  @Path(value = "/environments/count/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper")
  StreamingOutput getExecutionEnvironmentConfigurationCount(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @javax.ws.rs.core.Context HttpContext hc);
}
