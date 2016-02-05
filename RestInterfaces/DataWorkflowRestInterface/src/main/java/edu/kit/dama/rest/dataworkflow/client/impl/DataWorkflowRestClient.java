/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.rest.dataworkflow.client.impl;

import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskWrapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.dataworkflow.types.ExecutionEnvironmentConfigurationWrapper;
import edu.kit.dama.rest.dataworkflow.types.DataWorkflowTaskConfigurationWrapper;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.PropertiesUtil;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration;
import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author mf6319
 */
public class DataWorkflowRestClient extends AbstractRestClient {

    // <editor-fold defaultstate="collapsed" desc="URL components">
    /**
     * 'url' for count.
     */
    private static final String COUNT = "/count";
    /**
     * 'url' for tasks.
     */
    private static final String TASKS = "/tasks";
    /**
     * 'url' for configurations.
     */
    private static final String CONFIGURATIONS = "/configurations";
    /**
     * 'url' for environments.
     */
    private static final String ENVIRONMENTS = "/environments";

    /**
     * 'url' for task count.
     */
    private static final String TASKS_COUNT = TASKS + COUNT;
    /**
     * 'url' for task count.
     */
    private static final String CONFIGURATIONS_COUNT = CONFIGURATIONS + COUNT;
    /**
     * 'url' for task count.
     */
    private static final String ENVIRONMENTS_COUNT = ENVIRONMENTS + COUNT;
    /**
     * Get a task by its id.
     */
    private static final String TASK_BY_ID = TASKS + "/{0}";
    /**
     * Get a configuration by its id.
     */
    private static final String CONFIGURATION_BY_ID = CONFIGURATIONS + "/{0}";
    /**
     * Get a environment by its id.
     */
    private static final String ENVIRONMENT_BY_ID = ENVIRONMENTS + "/{0}";

// </editor-fold>
    /**
     * Default constructor.
     *
     * @param rootUrl The service root Url.
     * @param pContext The security context.
     */
    public DataWorkflowRestClient(String rootUrl, SimpleRESTContext pContext) {
        super(rootUrl, pContext);
    }
// <editor-fold defaultstate="collapsed" desc="Generic Rest methods (GET, PUT, POST, DELETE)">

    /**
     * Perform a get for DataWorkflowTask.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     *
     * @return DataWorkflowTaskWrapper.
     */
    private DataWorkflowTaskWrapper performDataWorkflowTaskGet(
            String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(DataWorkflowTaskWrapper.class,
                getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a post for DataWorkflowTask.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     *
     * @return DataWorkflowTaskWrapper.
     */
    private DataWorkflowTaskWrapper performDataWorkflowTaskPost(
            String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(DataWorkflowTaskWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
    }

    /**
     * Perform a get for DataWorkflowTaskConfigurationWrapper.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     *
     * @return DataWorkflowTaskConfigurationWrapper.
     */
    private DataWorkflowTaskConfigurationWrapper performDataWorkflowTaskConfigurationGet(
            String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(DataWorkflowTaskConfigurationWrapper.class,
                getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a get for ExecutionEnvironmentConfigurationWrapper.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     *
     * @return ExecutionEnvironmentConfigurationWrapper.
     */
    private ExecutionEnvironmentConfigurationWrapper performExecutionEnvironmentConfigurationGet(
            String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(ExecutionEnvironmentConfigurationWrapper.class, getWebResource(pPath), pQueryParams);
    }

    // </editor-fold>
    /**
     * Get all tasks accessible by the pSecurityContext and group pGroupId
     * beginning with pFirstIndex. The max. number of results is defined by
     * pResults.
     *
     * @param pGroupId The id of the group to which the associated task belongs.
     * @param pFirstIndex The first index, which can be ignored.
     * @param pResults The max. number of results.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return A DataWorkflowTaskWrapper entity containing the result.
     */
    public DataWorkflowTaskWrapper getAllTasks(String pGroupId,
            int pFirstIndex, int pResults,
            SimpleRESTContext pSecurityContext) {

        DataWorkflowTaskWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));

        returnValue = performDataWorkflowTaskGet(RestClientUtils.encodeUrl(
                TASKS), queryParams);
        return returnValue;
    }

    /**
     * Get the number of tasks accessible by the pSecurityContext and group
     * pGroupId.
     *
     * @param pGroupId The id of the group to which the associated task belongs.
     *
     * @return A DataWorkflowTaskWrapper entity containing the result.
     */
    public DataWorkflowTaskWrapper getTaskCount(String pGroupId) {
        return getTaskCount(pGroupId, null);
    }

    /**
     * Get the number of tasks accessible by the pSecurityContext and group
     * pGroupId.
     *
     * @param pGroupId The id of the group to which the associated task belongs.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return A DataWorkflowTaskWrapper entity containing the result.
     */
    public DataWorkflowTaskWrapper getTaskCount(String pGroupId,
            SimpleRESTContext pSecurityContext) {

        DataWorkflowTaskWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performDataWorkflowTaskGet(RestClientUtils.encodeUrl(
                TASKS_COUNT), queryParams);
        return returnValue;
    }

    /**
     * Get a single task by its id.
     *
     * @param pGroupId The id of the group to which the associated task belongs.
     * @param pId The id of the task.
     *
     * @return A DataWorkflowTaskWrapper entity containing the result.
     */
    public DataWorkflowTaskWrapper getTaskById(String pGroupId,
            Long pId) {
        return getTaskById(pGroupId, pId, null);
    }

    /**
     * Get a single task by its id.
     *
     * @param pGroupId The id of the group to which the associated task belongs.
     * @param pId The id of the task.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return A DataWorkflowTaskWrapper entity containing the result.
     */
    public DataWorkflowTaskWrapper getTaskById(String pGroupId,
            Long pId, SimpleRESTContext pSecurityContext) {

        DataWorkflowTaskWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();

        if (pId == null) {
            throw new IllegalArgumentException(
                    "Task ID may not be null");
        }

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performDataWorkflowTaskGet(RestClientUtils.encodeUrl(
                TASK_BY_ID, pId), queryParams);
        return returnValue;
    }

    /**
     * Create a new task. Therefor, the following arguments are needed:
     *
     * <ul>
     * <li>taskConfiguration: The DataWorkflowTaskConfiguration describing the
     * task and standard parameters.</li>
     * <li>environmentConfiguration: The ExecutionEnvironmentConfiguration
     * responsible for executing the task.</li>
     * <li>predecessor: The DataWorkflowTask that is predecessor of this task.
     * This parameter is optional and can be null.</li>
     * <li>inputObjectMap: A {@link java.util.Properties} object containing a
     * map of input digital object ids (key) and the data organization view name
     * (value) of the according object used by the task. </li>
     * <li>executionSettings: A {@link java.util.Properties} object containing
     * key-value entries of custom execution settings. This argument should be
     * optional in most cases.</li>
     * <li>applicationArguments: Custom application arguments that are provided
     * during the user application execution. This argument should be optional
     * in most cases.</li>
     * </ul>
     *
     * After successful creation the task execution will be performed within one
     * of the next processing cycles. As the processing of each task takes
     * multiple processing cycles a regular check of the task status will be
     * necessary.
     *
     * @param pGroupId The id of the group to which the associated task belongs.
     * @param investigation The investigation the output object of the task will
     * be associated with.
     * @param taskConfiguration The task configuration.
     * @param environmentConfiguration The execution environment configuration.
     * @param predecessor The predecessor task or null if no predecessor exists.
     * @param inputObjectMap The input object mapping containing mappings of
     * digitalObjectIds (keys) and DataOrganizationViews (values).
     * @param executionSettings The execution settings as key-value object.
     * @param applicationArguments The application arguments.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return A DataWorkflowTaskWrapper entity containing the result.
     */
    public DataWorkflowTaskWrapper createTask(String pGroupId,
            Investigation investigation,
            DataWorkflowTaskConfiguration taskConfiguration,
            ExecutionEnvironmentConfiguration environmentConfiguration,
            DataWorkflowTask predecessor,
            Properties inputObjectMap,
            Properties executionSettings,
            String applicationArguments,
            SimpleRESTContext pSecurityContext) {

        DataWorkflowTaskWrapper returnValue;
        MultivaluedMap queryParams;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        formParams = new MultivaluedMapImpl();

        if (investigation == null) {
            throw new IllegalArgumentException(
                    "Argument investigation may not be null");
        }

        if (taskConfiguration == null) {
            throw new IllegalArgumentException(
                    "Argument taskConfiguration may not be null");
        }
        if (environmentConfiguration == null) {
            throw new IllegalArgumentException(
                    "Argument environmentConfiguration may not be null");
        }

        if (inputObjectMap == null || inputObjectMap.isEmpty()) {
            throw new IllegalArgumentException(
                    "Argument inputObjectMap may not be null or empty");
        }

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        formParams.add("investigationId", Long.toString(investigation.getInvestigationId()));
        formParams.add("configurationId", Long.toString(taskConfiguration.getId()));
        formParams.add("environmentId", Long.toString(environmentConfiguration.getId()));
        if (predecessor != null) {
            formParams.add("predecessorId", Long.toString(predecessor.getId()));
        }

        try {
            formParams.add("inputObjectMap", PropertiesUtil.propertiesToString(inputObjectMap));
        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "Argument inputObjectMap cannot be serialized.", ex);
        }

        if (executionSettings != null) {
            try {
                formParams.add("executionSettings", PropertiesUtil.propertiesToString(executionSettings));
            } catch (IOException ex) {
                throw new IllegalArgumentException(
                        "Argument executionSettings cannot be serialized.", ex);
            }
        }
        if (applicationArguments != null) {
            formParams.add("applicationArguments", applicationArguments);
        }

        returnValue = performDataWorkflowTaskPost(RestClientUtils.encodeUrl(
                TASKS), queryParams, formParams);
        return returnValue;
    }

    /**
     * Get all task configurations accessible by the pSecurityContext and group
     * pGroupId beginning with pFirstIndex. The max. number of results is
     * defined by pResults.
     *
     * @param pGroupId The id of the group to which the associated task
     * configuration belongs.
     * @param pFirstIndex The first index.
     * @param pResults The max. number of results.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return A DataWorkflowTaskConfigurationWrapper entity containing the
     * result.
     */
    public DataWorkflowTaskConfigurationWrapper getAllConfigurations(String pGroupId,
            int pFirstIndex, int pResults,
            SimpleRESTContext pSecurityContext) {

        DataWorkflowTaskConfigurationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));

        returnValue = performDataWorkflowTaskConfigurationGet(RestClientUtils.encodeUrl(
                CONFIGURATIONS), queryParams);
        return returnValue;
    }

    /**
     * Get the number of task configurations accessible by the pSecurityContext
     * and group pGroupId.
     *
     * @param pGroupId The id of the group to which the associated task
     * configurations belongs.
     *
     * @return A DataWorkflowTaskConfigurationWrapper entity containing the
     * result.
     */
    public DataWorkflowTaskConfigurationWrapper getTaskConfigurationCount(String pGroupId) {
        return getTaskConfigurationCount(pGroupId, null);
    }

    /**
     * Get the number of task configurations accessible by the pSecurityContext
     * and group pGroupId.
     *
     * @param pGroupId The id of the group to which the associated task
     * configuration belongs.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return A DataWorkflowTaskConfigurationWrapper entity containing the
     * result.
     */
    public DataWorkflowTaskConfigurationWrapper getTaskConfigurationCount(String pGroupId,
            SimpleRESTContext pSecurityContext) {

        DataWorkflowTaskConfigurationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performDataWorkflowTaskConfigurationGet(RestClientUtils.encodeUrl(
                CONFIGURATIONS_COUNT), queryParams);
        return returnValue;
    }

    /**
     * Get a single task configuration by its id.
     *
     * @param pGroupId The id of the group to which the associated task
     * configuration belongs.
     * @param pId The id of the task configuration.
     *
     * @return A DataWorkflowTaskConfigurationWrapper entity containing the
     * result.
     */
    public DataWorkflowTaskConfigurationWrapper getTaskConfigurationById(String pGroupId,
            Long pId) {
        return getTaskConfigurationById(pGroupId, pId, null);
    }

    /**
     * Get a single task configuration by its id.
     *
     * @param pGroupId The id of the group to which the associated task
     * configuration belongs.
     * @param pId The id of the task configuration.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return A DataWorkflowTaskConfigurationWrapper entity containing the
     * result.
     */
    public DataWorkflowTaskConfigurationWrapper getTaskConfigurationById(String pGroupId,
            Long pId, SimpleRESTContext pSecurityContext) {

        DataWorkflowTaskConfigurationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();

        if (pId == null) {
            throw new IllegalArgumentException(
                    "TaskConfiguration ID may not be null");
        }

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performDataWorkflowTaskConfigurationGet(RestClientUtils.encodeUrl(
                CONFIGURATION_BY_ID, pId), queryParams);
        return returnValue;
    }

    /**
     * Get all environment configurations accessible by the pSecurityContext and
     * group pGroupId beginning with pFirstIndex. The max. number of results is
     * defined by pResults.
     *
     * @param pGroupId The id of the group to which the associated environment
     * configuration belongs.
     * @param pFirstIndex The first index.
     * @param pResults The max. number of results.
     *
     * @return An ExecutionEnvironmentConfigurationWrapper entity containing the
     * result.
     */
    public ExecutionEnvironmentConfigurationWrapper getAllExecutionEnvironmentConfigurations(String pGroupId,
            int pFirstIndex, int pResults) {
        return getAllExecutionEnvironmentConfigurations(pGroupId, pFirstIndex, pResults, null);
    }

    /**
     * Get all environment configurations accessible by the pSecurityContext and
     * group pGroupId beginning with pFirstIndex. The max. number of results is
     * defined by pResults.
     *
     * @param pGroupId The id of the group to which the associated environment
     * configuration belongs.
     * @param pFirstIndex The first index.
     * @param pResults The max. number of results.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return An ExecutionEnvironmentConfigurationWrapper entity containing the
     * result.
     */
    public ExecutionEnvironmentConfigurationWrapper getAllExecutionEnvironmentConfigurations(String pGroupId,
            int pFirstIndex, int pResults,
            SimpleRESTContext pSecurityContext) {

        ExecutionEnvironmentConfigurationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
                pFirstIndex));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
                pResults));

        returnValue = performExecutionEnvironmentConfigurationGet(RestClientUtils.encodeUrl(
                ENVIRONMENTS), queryParams);
        return returnValue;
    }

    /**
     * Get the number of environment configurations accessible by the
     * pSecurityContext and group pGroupId.
     *
     * @param pGroupId The id of the group to which the associated environment
     * configurations belongs.
     *
     * @return An ExecutionEnvironmentConfigurationWrapper entity containing the
     * result.
     */
    public ExecutionEnvironmentConfigurationWrapper getExecutionEnvironmentConfigurationCount(String pGroupId) {
        return getExecutionEnvironmentConfigurationCount(pGroupId, null);
    }

    /**
     * Get the number of environment configurations accessible by the
     * pSecurityContext and group pGroupId.
     *
     * @param pGroupId The id of the group to which the associated environment
     * configuration belongs.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return An DataWorkflowTaskConfigurationWrapper entity containing the
     * result.
     */
    public ExecutionEnvironmentConfigurationWrapper getExecutionEnvironmentConfigurationCount(String pGroupId,
            SimpleRESTContext pSecurityContext) {

        ExecutionEnvironmentConfigurationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performExecutionEnvironmentConfigurationGet(RestClientUtils.encodeUrl(
                ENVIRONMENTS_COUNT), queryParams);
        return returnValue;
    }

    /**
     * Get a single environment configuration by its id.
     *
     * @param pGroupId The id of the group to which the associated environment
     * configuration belongs.
     * @param pId The id of the environment configuration.
     *
     * @return An ExecutionEnvironmentConfigurationWrapper entity containing the
     * result.
     */
    public ExecutionEnvironmentConfigurationWrapper getExecutionEnvironmentConfigurationById(String pGroupId,
            Long pId) {
        return getExecutionEnvironmentConfigurationById(pGroupId, pId, null);
    }

    /**
     * Get a single environment configuration by its id.
     *
     * @param pGroupId The id of the group to which the associated environment
     * configuration belongs.
     * @param pId The id of the environment.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return An ExecutionEnvironmentConfigurationWrapper entity containing the
     * result.
     */
    public ExecutionEnvironmentConfigurationWrapper getExecutionEnvironmentConfigurationById(String pGroupId,
            Long pId, SimpleRESTContext pSecurityContext) {

        ExecutionEnvironmentConfigurationWrapper returnValue;
        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();

        if (pId == null) {
            throw new IllegalArgumentException(
                    "EnvironmentConfiguration ID may not be null");
        }

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performExecutionEnvironmentConfigurationGet(RestClientUtils.encodeUrl(ENVIRONMENT_BY_ID, pId), queryParams);
        return returnValue;
    }

    /**
     * Simple testing.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SimpleRESTContext ctx = new SimpleRESTContext("admin", "dama14");
        DataWorkflowRestClient client = new DataWorkflowRestClient("http://localhost:8080/KITDM/rest/dataworkflow/", ctx);
        //System.out.println(client.getTaskConfigurationById("USERS", 1l).getCount());
        // System.out.println(client.getTaskById("USERS", 1l).getEntities().get(0).getObjectTransferMap());
        Investigation inv = new Investigation();
        inv.setInvestigationId(1l);

        DataWorkflowTaskConfiguration config = new DataWorkflowTaskConfiguration();
        config.setId(5l);
        ExecutionEnvironmentConfiguration env = new ExecutionEnvironmentConfiguration();
        env.setId(1l);
        Properties inputMap = new Properties();
        inputMap.put("786f1eeb-3d58-4a8b-bbba-7fc2bb1fcfff", "default");
        DataWorkflowTask task = client.createTask("USERS", inv, config, env, null, inputMap, null, null, ctx).getEntities().get(0);
        System.out.println(task.getId());
        System.out.println(task.getLastUpdate());
    }
}
