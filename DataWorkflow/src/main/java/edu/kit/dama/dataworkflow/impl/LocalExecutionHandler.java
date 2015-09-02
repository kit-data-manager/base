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
package edu.kit.dama.dataworkflow.impl;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.dataworkflow.AbstractExecutionEnvironmentHandler;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.dataworkflow.exceptions.DataWorkflowProcessingException;
import edu.kit.dama.dataworkflow.exceptions.UnsupportedOperatingSystemException;
import edu.kit.dama.dataworkflow.util.DataWorkflowHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the AbstractExecutionEnvironmentHandler base class for
 * executing a DataWorkflow application locally on the machine where the repository
 * system is located. Compared to other EnvironmentHandler implementations the
 * LocalExecutionHandler executes the user application in a blocking way. This
 * means, as soon as {@link #startUserApplication(edu.kit.dama.dataworkflow.DataWorkflowTask)
 * } returns, the execution has finished. For asynchronous monitoring a file
 * named .RUNNING is created withing the temp directory of the task. This file
 * is removed as soon as the task execution has finished, successful or not.
 *
 * @author Jejkal
 */
public class LocalExecutionHandler extends AbstractExecutionEnvironmentHandler {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LocalExecutionHandler.class);

  /**
   * Default constructor.
   */
  public LocalExecutionHandler() {
    super();
  }

  /**
   * Execute the user application. This method will start a new process running
   * the prepared user application locally. The method will return as soon as
   * the application has terminated. An asnychronous monitoring task my check
   * whether the process is still running or not via {@link #isProcessingFinished(edu.kit.dama.dataworkflow.DataWorkflowTask)
   * } This method will check the runningIndicator file '.RUNNING', which only
   * exists as long as the application is running.
   *
   * @param pTask The task whose application should be executed.
   *
   * @throws DataWorkflowProcessingException If either the startup or the processing
   * fails for any reason, or if the user application returns an exit code != 0.
   */
  @Override
  public void startUserApplication(DataWorkflowTask pTask) throws DataWorkflowProcessingException {
    //simply start the process...monitoring will be connected later
    File runningIndicator = getRunningIndicator(pTask);
    FileOutputStream fout = null;
    FileOutputStream ferr = null;
    File executablePath;

    try {
      executablePath = DataWorkflowHelper.getTaskMainExecutable(pTask);
      File executionBasePath = DataWorkflowHelper.getExecutionBasePath(pTask);
      File workingDirectory = DataWorkflowHelper.getTaskWorkingDirectory(executionBasePath);
      File tempDirectory = DataWorkflowHelper.getTaskTempDirectory(executionBasePath);
      File inputDirectory = DataWorkflowHelper.getTaskInputDirectory(executionBasePath);
      File outputDirectory = DataWorkflowHelper.getTaskOutputDirectory(executionBasePath);

      if (!executablePath.canExecute()) {
        LOGGER.debug("Executable at location {} seems not to be executable. Taking care of this...");
        if (executablePath.setExecutable(true)) {
          LOGGER.debug("Executable was successfully set to be executable.");
        } else {
          LOGGER.warn("Failed to set executable to be executable. Trying to continue.");
        }
      }

      String cmdLineString = executablePath.getAbsolutePath() + " " + pTask.getConfiguration().getApplicationArguments() + " " + pTask.getApplicationArguments();
      LOGGER.debug("Building up command array from string '{}'", cmdLineString);

      CommandLine cmdLine = CommandLine.parse(cmdLineString);
      DefaultExecutor executor = new DefaultExecutor();
      executor.setExitValue(0);
      Map<String, String> env = new HashMap<>();
      env.put("WORKING_DIR", workingDirectory.getAbsolutePath());
      env.put("TEMP_DIR", tempDirectory.getAbsolutePath());
      env.put("INPUT_DIR", inputDirectory.getAbsolutePath());
      env.put("OUTPUT_DIR", outputDirectory.getAbsolutePath());

      fout = new FileOutputStream(new File(tempDirectory, "stdout.log"));
      ferr = new FileOutputStream(new File(tempDirectory, "stderr.log"));
      LOGGER.debug("Setting stream handler for stdout and stderr.");
      executor.setStreamHandler(new PumpStreamHandler(fout, ferr));
      LOGGER.debug("Creating .RUNNING file for monitoring.");
      FileUtils.touch(runningIndicator);
      LOGGER.debug("Executing process.");
      int exitCode = executor.execute(cmdLine);
      if (exitCode != 0) {
        throw new DataWorkflowProcessingException("Execution returned exit code " + exitCode + ". See logfiles for details.");
      } else {
        LOGGER.debug("Process successfully finished with exit code {}", exitCode);
      }
    } catch (IOException | UnsupportedOperatingSystemException e) {
      throw new DataWorkflowProcessingException("Failed to start executable for task " + pTask.getId(), e);
    } finally {
      LOGGER.debug("Removing running indicator file {}", runningIndicator);
      FileUtils.deleteQuietly(runningIndicator);
      if (fout != null) {
        try {
          fout.close();
        } catch (IOException ex) {
        }
      }

      if (ferr != null) {
        try {
          ferr.close();
        } catch (IOException ex) {
        }
      }
    }
  }

  /**
   * Helper method to get the running indicator file. The running indicator is a
   * file named '.RUNNING' that is located in the temp directory of the provided
   * task. As soon as the user application starts, the file is created. If the
   * application is finished, the file is removed. Therfor, it offers a simple
   * way to monitor the execution status.
   *
   * @param pTask The task.
   *
   * @return The file relative to the executionBasePath of the task.
   */
  public static File getRunningIndicator(DataWorkflowTask pTask) {
    File executionBasePath = DataWorkflowHelper.getExecutionBasePath(pTask);
    File tempDirectory = DataWorkflowHelper.getTaskTempDirectory(executionBasePath);
    return new File(tempDirectory, ".RUNNING");
  }

  @Override
  public DataWorkflowTask.TASK_STATUS getTaskStatus(DataWorkflowTask pTask) {
    //process is finished if 
    //  - the runningIndicator variable was created (is not null) 
    //  - the file itself does not exist (was deleted after execution)
    //As this local executor waits for termination, the runningIndicator can be held as member variable.
    if (getRunningIndicator(pTask).exists()) {
      return DataWorkflowTask.TASK_STATUS.PROCESSING;
    }

    //running indicator does not exist...as the execution in this handler is blocking this means that the processing has finished.
    return DataWorkflowTask.TASK_STATUS.PROCESSING_FINISHED;
  }

  @Override
  public String[] getInternalPropertyKeys() {
    return new String[]{};
  }

  @Override
  public String getInternalPropertyDescription(String pKey) {
    return null;
  }

  @Override
  public String[] getUserPropertyKeys() {
    return new String[]{};
  }

  @Override
  public String getUserPropertyDescription(String pKey) {
    return null;
  }

  @Override
  public void validateProperties(Properties pProperties) throws PropertyValidationException {
    //nothing to do here
  }

  @Override
  public void configure(Properties pProperties) throws PropertyValidationException, ConfigurationException {
    //nothing to do here
  }
}
