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
package edu.kit.dama.dataworkflow.impl;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.dataworkflow.AbstractExecutionEnvironmentHandler;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.dataworkflow.exceptions.DataWorkflowException;
import edu.kit.dama.dataworkflow.exceptions.UnsupportedTaskException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class GenericExecutionEnvironment {

  /**
   * For logging purposes.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericExecutionEnvironment.class);
  private static AbstractExecutionEnvironmentHandler instance = null;

  /**
   * Internal setup method in order to setup the environment using the
   * configuration provided in the constructor. Basically, the environment
   * handler class will be instantiated and configured using the properties
   * defined in the configuration object. If everything succeeds, the instance
   * of the environment handler will be available for to process a task.
   * Otherwise, a ConfigurationException will be thrown.
   *
   * @throws ConfigurationException If the configuration of the environment
   * handler fails.
   */
  private static void setup(ExecutionEnvironmentConfiguration pConfiguration) throws ConfigurationException {
    String handlerImplClass = pConfiguration.getHandlerImplementationClass();
    try {
      //create and configure instance
      Class clazz = Class.forName(handlerImplClass);
      instance = (AbstractExecutionEnvironmentHandler) clazz.getConstructor().newInstance();
      //perform custom configuration
      LOGGER.debug("Configuring handler instance.");
      instance.configure(pConfiguration.getPropertiesAsObject());
    } catch (ClassNotFoundException cnfe) {
      throw new ConfigurationException("Failed to locate EnvironmentHandler class for ID '" + pConfiguration.getId() + "'", cnfe);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException ie) {
      throw new ConfigurationException("Failed to instantiate and configure EnvironmentHandler for ID '" + pConfiguration.getId() + "'", ie);
    } catch (NoSuchMethodException nsme) {
      throw new ConfigurationException("Invalid EnvironmentHandler class for ID '" + pConfiguration.getId() + "'", nsme);
    } catch (ClassCastException cce) {
      throw new ConfigurationException("EnvironmentHandler instance for ID '" + pConfiguration.getId() + "' does not abstract handler class", cce);
    } catch (PropertyValidationException | IOException pve) {
      throw new ConfigurationException("Failed to validate properties for EnvironmentHandler instance with ID '" + pConfiguration.getId() + "'.", pve);
    }
  }

  /**
   * Process the provided task. At the beginning the execution environment
   * linked to the task is initialized. This call failes if the environment is
   * not (long) capable of processing the task or if its setup fails. Otherwise,
   * a new processing phase of the task will be triggered. Due to the nature of
   * a DataWorkflow task execution there are different possible results:
   *
   * <ul>
   * <li>The task is in a error state: A warning message is logged and the
   * unchanged status is returned.</li>
   * <li>The task is in a finish state: A info message is logged and the
   * unchanged status is returned.</li>
   * <li>The task is in any other intermediate state: The task is executed using
   * the configured environment handler and the current status is returned if
   * the current processing phase has succeeded without error. If the phase
   * could not succeed an exception is thrown.</li>
   * </ul>
   *
   * For details about the different phases please refer to
   * {@link AbstractExecutionEnvironmentHandler#executeTask(edu.kit.dama.dataworkflow.DataWorkflowTask)}.
   *
   * For the processing workflow this means that
   * {@link #processTask(edu.kit.dama.dataworkflow.DataWorkflowTask)} should be executed
   * multiple times as long as the result indicated a final state (error or
   * success).The returned status should be checked against
   * {@link DataWorkflowTask.TASK_STATUS#isError()} and
   * {@link DataWorkflowTask.TASK_STATUS#isFinished()} for failure or success.
   *
   * @param pTask The task to process.
   *
   * @return The current status of pTask.
   *
   * @throws ConfigurationException If the execution environment could not be
   * configured.
   * @throws DataWorkflowException If the current processing step has failed. The task
   * status should reflect which step exactly has failed.
   * @throws UnsupportedTaskException If the provided task cannot be handled by
   * the environment configured for this instance.
   */
  public static final DataWorkflowTask.TASK_STATUS processTask(DataWorkflowTask pTask) throws ConfigurationException, DataWorkflowException, UnsupportedTaskException {
    if (pTask == null) {
      throw new IllegalArgumentException("Argument pTask must not be null.");
    }
    DataWorkflowTask.TASK_STATUS result;

    if (!pTask.getExecutionEnvironment().canExecute(pTask)) {
      throw new UnsupportedTaskException("Task with id " + pTask.getId() + " cannot be executed by environment with id " + pTask.getExecutionEnvironment().getId());
    }
    LOGGER.debug("Settings up execution environment for task with id {}", pTask.getId());
    setup(pTask.getExecutionEnvironment());
    LOGGER.debug("Executing next step of task with id {}", pTask.getId());
    //execute next step
    result = instance.executeTask(pTask);
    LOGGER.debug("Task status after execution: {}", result);
    return result;
  }
}
