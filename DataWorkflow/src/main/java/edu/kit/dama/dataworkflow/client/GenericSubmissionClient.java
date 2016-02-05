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
package edu.kit.dama.dataworkflow.client;

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.client.status.CommandStatus;
import edu.kit.dama.client.status.Status;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.dataworkflow.client.parameters.ProcessParameters;
import edu.kit.dama.dataworkflow.exceptions.DataWorkflowException;
import edu.kit.dama.dataworkflow.exceptions.UnsupportedTaskException;
import edu.kit.dama.dataworkflow.impl.GenericExecutionEnvironment;
import edu.kit.dama.dataworkflow.util.DataWorkflowHelper;
import edu.kit.dama.dataworkflow.util.DataWorkflowTaskUtils;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.util.Constants;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 * Generic submission client running next to the computing infrastructure and is
 * preferentially executed automatically, e.g. via CRON job. This client ist
 * responsible for dealing with processing directives for execution arbitrary
 * jobs based on their meta data description.
 *
 * The execution of the client is divided into several parts which are:
 *
 * <ul>
 * <li>Obtain processing directives from a defined, abstract data accessor</li>
 * <li>Check validity of each directive (Data exists? Job can be prepared?)</li>
 * <li>Prepare the execution of each job's executable (Extract executable data
 * via data accessor, replace placeholder variables, prepare output and tmp
 * folder, generate start script...)</li>
 * <li>Execute start script by an abstract executor that depends on the
 * infrastructure that is currently used, e.g. Hadoop. Update the status within
 * the meta data to 'processing'.</li>
 * <li>During execution each process is monitored, if the execution fails this
 * information will be escalated to person(s) responsible for that task</li>
 * <li>After execution the status within the meta data is updates to 'processed'
 * or 'failed'</li>
 * <li>Cleanup (remove contents of tmp dir)</li>
 * </ul>
 *
 * @author Jejkal
 */
public class GenericSubmissionClient {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GenericSubmissionClient.class);

    public static CommandStatus processTasks(ProcessParameters params) {
        Status status = Status.SUCCESSFUL;
        Exception exception = null;

        try {
            //DataWorkflowPersistenceImpl not usable here...use manual query
            List<DataWorkflowTask> tasks = DataWorkflowHelper.getDataWorkflowTasks(params.taskIds, (params.count > 0) ? params.count : 10, !params.listOnly);

            if (params.listOnly) {
                DataWorkflowTaskUtils.printTaskList(tasks, params.verbose);
            } else {
                //check if count argument if applicable
                if (params.taskIds == null || params.taskIds.isEmpty()) {
                    //apply count argument
                    int taskCount = (params.count > 0) ? params.count : 10;
                    LOGGER.debug("Applying count argument. Processing first {} tasks of task list.", taskCount);
                    tasks = tasks.subList(0, Math.min(tasks.size(), taskCount));
                }

                for (DataWorkflowTask task : tasks) {
                    LOGGER.debug("Processing of task with id {} .", task.getId());
                    DataWorkflowTask predecessor = task.getPredecessor();
                    LOGGER.debug("Checking current workflow task with id {} for predecessor.", task.getId());
                    if (predecessor != null) {
                        LOGGER.debug("Task with id {} has a predecessor task with id {}. Checking status.", task.getId(), predecessor.getId());
                        //predecessor handling
                        if (DataWorkflowTask.TASK_STATUS.isCleanupPhase(predecessor.getStatus())) {
                            LOGGER.debug("Predecessor task with id {} is in cleanup phase. Assigning task output to task with id {}.", predecessor.getId(), task.getId());
                            //execution sucessfully finished, proceed.
                            IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
                            mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
                            try {
                                Properties ingestedObjects = predecessor.getObjectTransferMapAsObject();
                                LOGGER.debug("Assigning {} output object(s) to task with id {}", ingestedObjects.size(), task.getId());
                                Set<Object> keys = ingestedObjects.keySet();
                                for (Object o : keys) {
                                    LOGGER.debug("Adding 'default' view of output object {} of predecessor task {} to task {}.", o, predecessor.getId(), task.getId());
                                    task.getObjectViewMapAsObject().put(o, Constants.DEFAULT_VIEW);
                                }
                                //finished...task can start
                                LOGGER.debug("Output objects successfully assigned. Persisting updated task with id {}.", task.getId());
                                task = mdm.save(task);
                                LOGGER.debug("Task successfully persisted. Continuing execution.");
                            } catch (IOException ex) {
                                LOGGER.error("Failed to obtain output objects of predecessor task with id " + predecessor.getId() + ". Task with id " + task.getId() + " cannot continue.", ex);
                            } finally {
                                mdm.close();
                            }
                        } else if (DataWorkflowTask.TASK_STATUS.isErrorState(predecessor.getStatus())) {
                            //execution of predecessor has failed...warn and break.
                            LOGGER.warn("Predecessor task with id {} has failed with status {}. The task with id {} will not continue until the predecessor has finished.", predecessor.getId(), predecessor.getStatus(), task.getId());
                            continue;
                        } else {
                            //execution not yet finished, break.
                            LOGGER.debug("Predecessor task with id {} is not finished, yet. The current status is {}. As soon as the task enters the cleanup phase, the task with id {} will continue.", predecessor.getId(), predecessor.getStatus(), task.getId());
                            continue;
                        }
                    } else {
                        LOGGER.debug("Task with id {} has no predecessor task. Continuing task execution.", task.getId());
                    }

                    //do processing
                    try {
                        LOGGER.debug("Processing task with id {}", task.getId());
                        //DataWorkflowTask.TASK_STATUS statusBefore = task.getStatus();
                        DataWorkflowTask.TASK_STATUS statusBefore = task.getStatus();
                        DataWorkflowTask.TASK_STATUS statusAfter;

                        if (DataWorkflowTask.TASK_STATUS.STAGING_FINISHED.equals(statusBefore)) {
                            //next step would be submission...check environment first
                            LOGGER.debug("Checking whether task can be scheduled by ExecutionEnvironment.");
                            if (!DataWorkflowHelper.canScheduleTask(task.getExecutionEnvironment())) {
                                LOGGER.info("ExecutionEnvironment with id {} cannot schedule another task. Submission of task with id {} is postponed.", task.getExecutionEnvironment().getId(), task.getId());
                                statusAfter = statusBefore;
                            } else {
                                LOGGER.debug("ExecutionEnvironment is capable of another task execution. Submitting task.");
                                statusAfter = GenericExecutionEnvironment.processTask(task);
                            }
                        } else {
                            //normal step will follow, no checks needed.
                            statusAfter = GenericExecutionEnvironment.processTask(task);
                        }
                        LOGGER.debug("Task processing returned with status {}", statusAfter);
                    } catch (ConfigurationException ex) {
                        LOGGER.error("Failed to configure task execution.", ex);
                        exception = ex;
                    } catch (DataWorkflowException | UnsupportedTaskException ex) {
                        LOGGER.error("Failed to execute DataWorkflow task.", ex);
                        exception = ex;
                    }
                }
            }
            //status might be SUCCESS even if no task has succeeded...'exception' holds the last exception.
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain DataWorkflow tasks.", ex);
            //result is FAILED as no task could be accessed.
            status = Status.FAILED;
            exception = ex;
        }

        return new CommandStatus(status, exception, null);
    }

//
//  /**
//   * Obtain unhandled processing directives from a supported data source, e.g. a
//   * Hibernate database. Obtained directives are validated before<BR/>
//   * the actual execution is started. If a directive is valid (all related data
//   * and meta data found), the execution of this directive is prepared<BR/>
//   * and finally performed.
//   */
//  public void handleOpenProcessingDirectives() throws Exception {
//    logger.info("===Start of handling scheduled DataWorkflow tasks===");
//    //get directives from generic source
//    logger.info("Obtaining scheduled tasks");
//
//    List<DataWorkflowTask> tasks = DataWorkflowPersistenceImpl.getSingleton(AuthorizationContext.factorySystemContext()).getTaskByStatus(DataWorkflowTask.TASK_STATUS.SCHEDULED, -1, -1);
//    if (tasks != null && !tasks.isEmpty()) {
//      logger.info("Found {} scheduled DataWorkflow task(s)", tasks.size());
//
//      for (DataWorkflowTask task : tasks) {
//        logger.info("Starting task with id {}", task.getId());
//        //int activeProcessings = mParams.getDataAccessor().getActiveProcessings();
//        //check if activeProcessings <= MAX_PROCESSINGS
//        // if (activeProcessings <= mParams.getMaxProcessings()) {
//        //@TODO Check max. running tasks
//        scheduleExecution(task);
//        // }
//      }
//    } else {
//      logger.info("No scheduled task found");
//    }
//
//    logger.info("===End of handling scheduled tasks===");
//
//    logger.info("===Start waiting for running executions===");
//    DataWorkflowTaskProcessor[] aActiveProcessors = mActiveTasks.toArray(new DataWorkflowTaskProcessor[mActiveTasks.size()]);
//    int timeUntilNextNotification = 60000;
//    final int TEN_SECONDS = 1000 * 10;
//    final int ONE_MINUTE = 6 * TEN_SECONDS;
//    logger.info(" * Next vital sign expected in one minute");
//    while (!mActiveTasks.isEmpty()) {
//      if (aActiveProcessors.length != mActiveTasks.size()) {
//        //something has changed during last for-loop, rebuild array
//        aActiveProcessors = mActiveTasks.toArray(new DataWorkflowTaskProcessor[mActiveTasks.size()]);
//      }
//      //check all processors if they have finished
//      for (DataWorkflowTaskProcessor activeProcessor : aActiveProcessors) {
//        if (activeProcessor.hasFinished()) {
//          logger.info("Processor '{}' has finished {}", new Object[]{activeProcessor.getName(), ((activeProcessor.getFinalResult() == DataWorkflowTaskProcessor.ID_OK) ? "successfully" : "with errors")});
//        }
//        logger.debug("Removing processor from list of active tasks");
//        mActiveTasks.remove(activeProcessor);
//        logger.debug("Updating task result in database");
//        DataWorkflowPersistenceImpl.getSingleton(AuthorizationContext.factorySystemContext()).updateTask(activeProcessor.getDataWorkflowTask());
//      }
//    }
//
//
//    //if one minute has elapsed, show some status information to tell that we're still alive
//    if (timeUntilNextNotification <= 0) {
//      int runningExecutions = mActiveTasks.size();
//      //skip this one if the loop somehow or other finishes within the next iteration
//      if (runningExecutions != 0) {
//        logger.info("{} remaining execution(s)", runningExecutions);
//      }
//      timeUntilNextNotification = ONE_MINUTE;
//    }
//
//    //wait a while until next polling
//    try {
//      Thread.sleep(TEN_SECONDS);
//    } catch (Exception e) {
//    }
//    //decrease time until next notification
//    timeUntilNextNotification -= TEN_SECONDS;
//    logger.info("===All running executions have finished===");
//  }
//
//  /**
//   * Schedule the execution of one directive
//   *
//   * @param pTask The task to schedule
//   */
//  private void scheduleExecution(DataWorkflowTask pTask) {
//    try {
//      logger.info("---Trying to schedule DataWorkflow task---");
//      logger.info("Creating task  processor");
//      DataWorkflowTaskProcessor taskProcessor = new DataWorkflowTaskProcessor(pTask);
//      logger.info("Start processing task");
//      pTask.setStatus(DataWorkflowTask.TASK_STATUS.PROCESSING);
//      taskProcessor.start();
//      mActiveTasks.add(taskProcessor);
//      logger.info("---DataWorkflow task scheduled for execution successfully---");
//    } catch (Exception e) {
//      logger.error("---Failed to schedule DataWorkflow task---", e);
//    }
//  }
//
//  public static void main(String[] args) {
//    CommandLineParser parser = new PosixParser();
//    Options options = new Options();
//    options.addOption(OptionBuilder.withLongOpt(HIBERNATE_CONFIG_FILE).withDescription("Set the hibernate config file (mandatory)").hasArg().withArgName(HIBERNATE_CONFIG_FILE + ".xml").isRequired().create((char) 'c'));
//    options.addOption("l", LOG4J_CONFIG, true, "Sets the log4j config file");
//    options.addOption(OptionBuilder.withLongOpt(DATA_ACCESSOR).withDescription("Set the data accessor class").hasArg().withArgName("CLASSNAME").create((char) 'a'));
//    options.addOption(OptionBuilder.withLongOpt(MAX_PROCESSINGS).withDescription("Set the max. amount of parallel processing").hasArg().withArgName("MAXPROCESSINGS").create((char) 'p'));
//    options.addOption("h", "help", false, "Print command line options");
//
//    GenericSubmissionClientParams params = new GenericSubmissionClientParams();
//    try {
//      logger.info("Parsing command line...");
//      CommandLine line = parser.parse(options, args);
//
//      if (line.hasOption(HIBERNATE_CONFIG_FILE)) {
//        if (logger.isInfoEnabled()) {
//          logger.info(" * Found '" + HIBERNATE_CONFIG_FILE + "' command line argument with value '" + line.getOptionValue(HIBERNATE_CONFIG_FILE) + "'");
//        }
//        // set the data accessor class
//        HibernateUtil.createSession(line.getOptionValue(HIBERNATE_CONFIG_FILE));
//      }
//      if (line.hasOption(DATA_ACCESSOR)) {
//        if (logger.isInfoEnabled()) {
//          logger.info(" * Found '" + DATA_ACCESSOR + "' command line argument with value '" + line.getOptionValue(DATA_ACCESSOR) + "'");
//        }
//        // set the data accessor class
//        params.setDataAccessorClass(line.getOptionValue(DATA_ACCESSOR));
//      } else {
//        params.setDataAccessorClass("edu.kit.lsdf.exec.impl.HibernateAccessor");
//      }
//      if (line.hasOption(MAX_PROCESSINGS)) {
//        if (logger.isInfoEnabled()) {
//          logger.info(" * Found '" + MAX_PROCESSINGS + "' command line argument with value '" + line.getOptionValue(MAX_PROCESSINGS) + "'");
//        }
//        // set the max amount of parallel processings
//        params.setMaxProcessings(Integer.parseInt(line.getOptionValue(MAX_PROCESSINGS)));
//      }
//      if (line.hasOption(LOG4J_CONFIG)) {
//        if (logger.isInfoEnabled()) {
//          logger.info(" * Found '" + LOG4J_CONFIG + "' command line argument with value '" + line.getOptionValue(LOG4J_CONFIG) + "'");
//        }
//        // set the log4j config file
//        params.setLogConfig((String) line.getOptionValue(LOG4J_CONFIG));
//      }
//
//      if (line.hasOption("help")) {
//        logger.info(" * Found 'help' command line argument");
//        // show the command line help and exit
//        params.printParameterHelp();
//        return;
//      }
//    } catch (Exception argumentError) {
//      System.err.println("Failed to parse arguments");
//      argumentError.printStackTrace(System.err);
//      System.err.println("\n");
//      params.printParameterHelp(System.err);
//      return;
//    }
//
//    //check if we got a valid data accessor from command line
//    if (params.getDataAccessor() == null) {
//      //use HibernateAccessor by default
//      HibernateAccessor accessor = new HibernateAccessor();
//      params.setDataAccessor(accessor);
//      /* WorkflowMonitorPanel<HibernateAccessor> monitor = new WorkflowMonitorPanel<HibernateAccessor>(accessor);
//       JFrame f = new JFrame();
//       monitor.setSize(400, 200);
//       f.add(monitor);
//       f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//       f.pack();
//       f.setVisible(true);*/
//    }
//
//    logger.info("Start processing...");
//    try {
//      GenericSubmissionClient client = new GenericSubmissionClient(params);
//      //do processing
//      client.handleOpenProcessingDirectives();
//    } catch (Exception processingError) {
//      logger.error("Failed to perform submission", processingError);
//    }
//  }
}
