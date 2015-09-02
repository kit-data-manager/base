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
package edu.kit.dama.dataworkflow;

import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTransition;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.IConfigurable;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.services.impl.download.DownloadInformationServiceLocal;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationServiceLocal;
import edu.kit.dama.staging.util.StagingConfigurationManager;
import edu.kit.dama.util.SystemUtils;
import edu.kit.dama.util.ZipUtils;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask.TASK_STATUS;
import edu.kit.dama.dataworkflow.exceptions.CleanupException;
import edu.kit.dama.dataworkflow.exceptions.DataWorkflowProcessingException;
import edu.kit.dama.dataworkflow.exceptions.ExecutionPreparationException;
import edu.kit.dama.dataworkflow.exceptions.IngestException;
import edu.kit.dama.dataworkflow.exceptions.StagingPreparationException;
import edu.kit.dama.dataworkflow.impl.DataWorkflowPersistenceImpl;
import edu.kit.dama.dataworkflow.util.DataWorkflowHelper;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;

/**
 * The AbstractExecutionEnvironmentHandler defines a generic implementation of a
 * typical DataWorkflow task execution workflow. The workflow is executed using
 * the method {@link #executeTask(edu.kit.dama.dataworkflow.DataWorkflowTask) }
 * of AbstractExecutionEnvironmentHandler. Please refer to the documentation of
 * this method for more details about a task execution workflow.
 *
 * There only two differences between different execution handler
 * implementations:
 *
 * <ul>
 * <li>The way how/where a user application is started. This is covered by
 * implementing {@link #startUserApplication(edu.kit.dama.dataworkflow.DataWorkflowTask)
 * }.</li>
 * <li>The way how a user application is monitored after it has started. This is
 * done by implementing {@link #getTaskStatus(edu.kit.dama.dataworkflow.DataWorkflowTask)
 * }.</li>
 * </ul>
 *
 * @author mf6319
 */
public abstract class AbstractExecutionEnvironmentHandler implements IConfigurable {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractExecutionEnvironmentHandler.class);

  /**
   * Default constructor.
   */
  public AbstractExecutionEnvironmentHandler() {
  }

  /**
   * Execute the provided task on the execution environment associated with this
   * handler. The execution is divided into multiple steps that are partly
   * executed asynchronously. Therefor, this method must be executed multiple
   * times per task. The single phases are the following:
   *
   * <b>Preparation Phase:</b> In this phase the execution is prepared.
   * Directory structures are created, the user application is obtained,
   * extracted and prepared and the data staging of digital objects is
   * scheduled. This phase will end with one or multiple data transfer tasks for
   * providing the input data. The check for availability of the data goes into
   * the next phase.<br/>
   * <b>Execution Phase:</b> In this phase the actual execution takes place.
   * This phase can start if all data is staged to the processing storage. As
   * soon as this is done, the execution is started either locally or remotely.
   * Depending on the handler implementation the implementation can be done
   * synchronously or asynchronously. However, this phase ends as soon as the
   * client application has terminated, which will be checked via {@link #isProcessingFinished(edu.kit.dama.dataworkflow.DataWorkflowTask)
   * }.<br/>
   * <b>Ingest Phase:</b> After successful execution the ingest of the result
   * data is scheduled. Therefor, according metadata structures will be created
   * and a new ingest is scheduled. As the ingest should be available
   * immediately, the pre-ingest is directly done in this phase by creating
   * links of all output files at the pre-ingest location. The status of the
   * ingest is set to PRE_INGEST_FINISHED immediately, the task will change its
   * status into INGEST and the actual ingest process will take place
   * asynchronously.<br/>
   * <b>Cleanup Phase:</b> As soon as the ingest has finished successfully, all
   * task-related data can be removed. The task changes its status to CLEANUP.
   * If the cleanup succeeds, the task enters the FINISHED state, otherwise the
   * CLEANUP_FAILED state is entered. However, both states describe a
   * successfully finished task from the user perspective.
   *
   * This method throws several different exceptions depending on the state
   * where the execution has failed. Depending on the exception, the state of
   * the task will change as follows:
   *
   * <ul>
   * <li>ExecutionPreparationException: PREPARTION_FAILED</li>
   * <li>StagingPreparationException: STAGING_FAILED</li>
   * <li>StartProcessingException: PROCESSING_FAILED</li>
   * <li>IngestException: INGEST_FAILED</li>
   * <li>CleanupException: CLEANUP_FAILED</li>
   * </ul>
   *
   * Depending on the handler implementation single steps (e.g. parts of
   * preparation and execution) must be performed remotely.
   *
   * @param pTask The task to submit.
   *
   * @return The current task status.
   *
   * @throws ExecutionPreparationException If the preparation of the execution
   * fails.
   * @throws DataWorkflowProcessingException If the processing cannot be
   * started.
   * @throws StagingPreparationException If some staging related step fails,
   * e.g. check for downloads or the ingest of result data.
   * @throws IngestException If the result ingest process has failed.
   * @throws CleanupException If the cleanup has failed.
   */
  public final DataWorkflowTask.TASK_STATUS executeTask(DataWorkflowTask pTask) throws ExecutionPreparationException, StagingPreparationException, DataWorkflowProcessingException, IngestException, CleanupException {
    //perfom basic checks for null and mandatory fields
    checkTask(pTask);

    DataWorkflowTask.TASK_STATUS statusBefore = pTask.getStatus();
    DataWorkflowTask.TASK_STATUS statusAfter = TASK_STATUS.UNKNOWN;
    LOGGER.debug("Status of task {} before execution cycle: {}", pTask.getId(), statusBefore);
    try {
      switch (statusBefore) {
        case SCHEDULED:
          //task is scheduled, status change to PREPARING possible
          LOGGER.debug("Task is scheduled, preparing execution.");
          if (performPreparation(pTask)) {
            LOGGER.debug("Preparation returned TRUE. Task should be in status PREPARATION_FINISHED.");
          } else {
            LOGGER.debug("Preparation returned FALSE. Task should be in status PREPARATION_FAILED.");
          }
          break;
        case PREPARING:
          //Preparation running. This should never happen, but it happened somehow...so come back later.
          LOGGER.debug("Task with id {} is still in state PREPARING.", pTask.getId());
          if (performPreparation(pTask)) {
            LOGGER.debug("Preparation returned TRUE. Task should be in status PREPARATION_FINISHED.");
          } else {
            LOGGER.debug("Preparation returned FALSE. Task should be in status PREPARATION_FAILED.");
          }
          break;
        case PREPARATION_FAILED:
          //Preparation has failed. Unable to continue without reset to SCHEDULED.
          LOGGER.warn("Task with id {} is in state PREPARATION_FAILED. Reset the state to SCHEDULED in order to re-attempt preparation.", pTask.getId());
          break;
        case PREPARATION_FINISHED:
          //Preparation has failed. Unable to continue without reset to SCHEDULED.
          LOGGER.debug("Task preparation has finished. Performing staging.");
          if (performStaging(pTask)) {
            LOGGER.debug("Staging returned TRUE. Task should be in status STAGING_FINISHED.");
          } else {
            LOGGER.debug("Staging returned FALSE. Task is in status {}.", pTask.getStatus());
          }
          break;
        case STAGING:
          LOGGER.debug("Task with id {} is still in state STAGING. Checking status.", pTask.getId());
          if (performStaging(pTask)) {
            LOGGER.debug("Staging returned TRUE. Task should be in status STAGING_FINISHED.");
          } else {
            LOGGER.debug("Staging returned FALSE. Task is in status {}.", pTask.getStatus());
          }
          break;
        case STAGING_FAILED:
          //Staging has failed. Unable to continue without reset to SCHEDULED.
          LOGGER.warn("Task with id {} is in state STAGING_FAILED. Reset the state to SCHEDULED in order to re-attempt preparation.", pTask.getId());
          break;
        case STAGING_FINISHED:
          LOGGER.debug("Task staging has finished. Starting user application.");
          if (performProcessing(pTask)) {
            LOGGER.debug("Processing returned TRUE. Task should be in status PROCESSING_FINISHED or PROCESSING_FAILED.");
          } else {
            LOGGER.debug("Processing returned FALSE. Task is in status {}.", pTask.getStatus());
          }
          break;
        case PROCESSING:
          LOGGER.debug("Task with id {} is still in state PROCESSING. Checking task status.", pTask.getId());
          if (performProcessing(pTask)) {
            LOGGER.debug("Processing returned TRUE. Task should be in status PROCESSING_FINISHED or PROCESSING_FAILED.");
          } else {
            LOGGER.debug("Processing returned FALSE. Task is in status {}.", pTask.getStatus());
          }
          break;
        case PROCESSING_FAILED:
          //Processing has failed.  Unable to continue without reset to STAGING.
          LOGGER.warn("Task with id {} is in state PROCESSING_FAILED. Reset the state to STAGING in order to re-attempt processing.", pTask.getId());
          break;
        case PROCESSING_FINISHED:
          LOGGER.debug("Task processing has finished. Starting ingest.");
          performIngest(pTask);
        case INGEST:
          LOGGER.debug("Task with id {} is still in state INGEST.", pTask.getId());
          performIngest(pTask);
          break;
        case INGEST_FINISHED:
          LOGGER.debug("Ingest is finished. Starting cleanup.");
          if (performCleanup(pTask)) {
            LOGGER.debug("Cleanup returned TRUE. Task should be in status CLEANUP_FINISHED or CLEANUP_FAILED.");
          } else {
            LOGGER.debug("Cleanup returned FALSE. Task is in status {}.", pTask.getStatus());
          }
          break;
        case INGEST_FAILED:
          //Ingest has failed. Unable to continue without reset to PROCESSING.
          LOGGER.warn("Task with id {} is in state INGEST_FAILED. Reset the state to PROCESSING in order to re-attempt staging out.", pTask.getId());
          break;
        case CLEANUP:
          //Cleanup running. Come back later.
          LOGGER.debug("Task with id {} is still in state CLEANUP.", pTask.getId());
          break;
        case CLEANUP_FAILED:
          //Cleanup failed. This is a final status.
          LOGGER.debug("Task with id {} is in final state CLEANUP_FAILED.", pTask.getId());
          break;
        case CLEANUP_FINISHED:
          //Cleanup succeeded. This is a final status.
          LOGGER.debug("Task with id {} is in final state CLEANUP_FINISHED.", pTask.getId());
          break;
        case UNKNOWN:
          //whatever
          LOGGER.info("Task with id {} is in state UNKNOWN. No idea why.", pTask.getId());
          break;
        default:
          LOGGER.error("Task with id {} is in an unexpected state. Manual intervention necessary.", pTask.getId());
      }
      statusAfter = pTask.getStatus();
    } catch (ExecutionPreparationException ex) {
      statusAfter = DataWorkflowTask.TASK_STATUS.PREPARATION_FAILED;
      throw ex;
    } catch (StagingPreparationException ex) {
      statusAfter = DataWorkflowTask.TASK_STATUS.STAGING_FAILED;
      throw ex;
    } catch (DataWorkflowProcessingException ex) {
      statusAfter = DataWorkflowTask.TASK_STATUS.PROCESSING_FAILED;
      throw ex;
    } catch (IngestException ex) {
      statusAfter = DataWorkflowTask.TASK_STATUS.INGEST_FAILED;
      throw ex;
    } catch (CleanupException ex) {
      statusAfter = DataWorkflowTask.TASK_STATUS.CLEANUP_FAILED;
      throw ex;
    } finally {
      LOGGER.debug("Status of task {} after execution cycle: {}", pTask.getId(), statusAfter);
      if (statusBefore.equals(statusAfter)) {
        LOGGER.debug("Updating status of task {} from {} to {}", pTask.getId(), statusBefore, statusAfter);
        setTaskStatus(pTask, statusAfter);
      }
    }
    return statusAfter;
  }

  /**
   * Check helper for null arguments and the existence of mandatory fields, e.g.
   * the task id or an associated investigation. If any check fails, an
   * IllegalArgumentException will be thrown.
   *
   * @param pTask The task to check.
   */
  private void checkTask(DataWorkflowTask pTask) {
    if (pTask == null) {
      throw new IllegalArgumentException("Argument pTask must not be null.");
    }

    if (pTask.getId() == null) {
      throw new IllegalArgumentException("Argument pTask must be a persisted entity.");
    }

    if (pTask.getInvestigationId() <= 0l) {
      throw new IllegalArgumentException("Task with id " + pTask.getId() + " is not associated with an investigation.");
    }
  }

  /**
   * Helper method for setting the task status to the provided status enum.
   */
  private boolean setTaskStatus(DataWorkflowTask pTask, DataWorkflowTask.TASK_STATUS pStatus) {
    boolean result = false;
    pTask.setStatus(pStatus);
    IAuthorizationContext ctx = DataWorkflowHelper.getTaskContext(pTask);
    try {
      DataWorkflowPersistenceImpl.getSingleton(ctx).updateTask(pTask);
      result = true;
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to update status of task with id " + pTask.getId() + " to " + pStatus, ex);
    }
    return result;
  }

  /**
   * Setup basic properties of the execution, e.g. directories, and prepare the
   * user application. The preparation phase can only be entered if pTask is in
   * status {@link DataWorkflowTask.TASK_STATUS#SCHEDULED} or for checks if the
   * status is
   * {@link DataWorkflowTask.TASK_STATUS#PREPARING}, {@link DataWorkflowTask.TASK_STATUS#PREPARATION_FINISHED}
   * or {@link DataWorkflowTask.TASK_STATUS#PREPARATION_FAILED}. Otherwise, an
   * ExecutionPreparationException will be thrown. After creating all necessary
   * directories, {@link #prepareApplication(edu.kit.dama.dataworkflow.DataWorkflowTask)
   * } will be called. As a result pTask is either in state
   * {@link DataWorkflowTask.TASK_STATUS#PREPARATION_FINISHED} and TRUE is
   * returned or pTask is in state
   * {@link DataWorkflowTask.TASK_STATUS#PREPARATION_FAILED} and a
   * RuntimeException is thrown. There is no possibility for any other exception
   * at this point. If the preparation has failed, FALSE is returned.
   *
   * Once the preparation phase was finished, subsequent calls will then either
   * return TRUE or FALSE, depending on the status. To repeat the preparation
   * phase, the status of pTask has to be reset to
   * {@link DataWorkflowTask.TASK_STATUS#SCHEDULED}.
   *
   * @param pTask The task that will be prepared.
   *
   * @return TRUE if the preparation was successful, FALSE otherwise.
   *
   * @throws ExecutionPreparationException If pTask is not in a proper state, if
   * any directory could not be obtained or if a the application preparation has
   * failed.
   */
  public final boolean performPreparation(DataWorkflowTask pTask) throws ExecutionPreparationException {
    if (!DataWorkflowTask.TASK_STATUS.isPreparationPhase(pTask.getStatus())) {
      throw new ExecutionPreparationException("Task with id " + pTask.getId() + " is in state " + pTask.getStatus() + ". Preparation not possible.");
    }

    boolean preparationSuccessful = false;
    if (DataWorkflowTask.TASK_STATUS.SCHEDULED.equals(pTask.getStatus())) {//schedule preparation
      LOGGER.debug("Setting status of task {} to PREPARING", pTask.getId());
      setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.PREPARING);
      IAuthorizationContext ctx = DataWorkflowHelper.getTaskContext(pTask);
      File stagingBasePath = DataWorkflowHelper.getStagingBasePath(pTask);
      File inputDir = DataWorkflowHelper.getTaskInputDirectory(stagingBasePath);
      File outputDir = DataWorkflowHelper.getTaskOutputDirectory(stagingBasePath);
      File tempDir = DataWorkflowHelper.getTaskTempDirectory(stagingBasePath);
      File workingDir = DataWorkflowHelper.getTaskWorkingDirectory(stagingBasePath);
      //prepare all directories needed for execution
      LOGGER.info("Setting up task directories\nInput: {}\nOutput: {}\nWorking: {}\nTemp: {}", inputDir, outputDir, workingDir, tempDir);

      if (!inputDir.mkdirs() || !outputDir.mkdirs() || !tempDir.mkdirs() || !workingDir.mkdirs()) {
        throw new ExecutionPreparationException("Failed to create sub-directory structure at " + stagingBasePath);
      } else {
        LOGGER.debug("Directories successfully created.");
      }

      String accessPointId = pTask.getExecutionEnvironment().getStagingAccessPointId();
      AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(accessPointId);
      String inUrl = accessPoint.getUrlForLocalPath(inputDir, ctx).toString();
      LOGGER.info("Setting input directory as URL {}", inUrl);
      pTask.setInputDirectoryUrl(inUrl);
      String outUrl = accessPoint.getUrlForLocalPath(outputDir, ctx).toString();
      LOGGER.info("Setting output directory as URL {}", outUrl);
      pTask.setOutputDirectoryUrl(outUrl);
      String workingUrl = accessPoint.getUrlForLocalPath(workingDir, ctx).toString();
      LOGGER.info("Setting working directory as URL {}", workingUrl);
      pTask.setWorkingDirectoryUrl(workingUrl);
      String tempUrl = accessPoint.getUrlForLocalPath(tempDir, ctx).toString();
      LOGGER.info("Setting temp directory as URL {}", tempUrl);
      pTask.setTempDirectoryUrl(tempUrl);

      try {
        //Obtain and extract the application into the working directory and modify scripts according to variables.
        prepareApplication(pTask);
        setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.PREPARATION_FINISHED);
        preparationSuccessful = true;
      } finally {
        if (!preparationSuccessful) {
          LOGGER.debug("Application preparation for task with id {} was not successful. Aborting scheduled staging for task.", pTask.getId());
          setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.PREPARATION_FAILED);
        }
      }
    } else if (DataWorkflowTask.TASK_STATUS.PREPARATION_FINISHED.equals(pTask.getStatus())) {
      //return fast
      preparationSuccessful = true;
    }//otherwise, status should be PREPARATION_FAILED and FALSE will be returned
    return preparationSuccessful;
  }

  /**
   * Prepare the user application of the provided task. The preparation includes
   * obtaining the application archive from the application archive URL that is
   * part of the task configuration, extract the obtained ZIP archive to the
   * working directory of the task and finally, perform variable substitution.
   *
   * Due to the fact, that the working directory of the task is accessible from
   * the repository system running locally as well as from the execution
   * environment potentially located somewhere else, the entire preparation can
   * be identical for almost any imaginable EnvironmentHandler-implementation.
   * Therefor, this method is not abstract but implemented in a default way and
   * can be overwritten if required.
   *
   * @param pTask The task for which the application should be prepared.
   *
   * @throws ExecutionPreparationException If one part of the preparation fails.
   */
  private void prepareApplication(DataWorkflowTask pTask) throws ExecutionPreparationException {
    //obtain and extract zip
    File executionBasePath = DataWorkflowHelper.getStagingBasePath(pTask);
    File workingDirectory = DataWorkflowHelper.getTaskWorkingDirectory(executionBasePath);
    File destination = new File(workingDirectory, "userApplication.zip");
    FileOutputStream propertiesStream = null;
    try {
      LOGGER.debug("Obtaining application package URL");
      AbstractFile applicationPackage = new AbstractFile(new URL(pTask.getConfiguration().getApplicationPackageUrl()));
      LOGGER.debug("Downloading application archive from {} to {}", applicationPackage, destination);
      applicationPackage.downloadFileToFile(new AbstractFile(destination));
      LOGGER.debug("User application archive successfully downloaded. Extracting user application to {}", destination.getParentFile());
      ZipUtils.unzip(destination, destination.getParentFile());
      LOGGER.debug("Deleting user application archive. Success: {}", destination.delete());
      LOGGER.debug("User application successfully extracted. Performing variable substition.");
      DataWorkflowHelper.substituteVariablesInDirectory(pTask);
      LOGGER.debug("Variable substitution finished. Storing custom properties.");
      Properties settings = pTask.getExecutionSettingsAsObject();
      if (settings != null) {
        propertiesStream = new FileOutputStream(new File(workingDirectory, "dataworkflow.properties"));
        settings.store(propertiesStream, null);
      }
      LOGGER.debug("Custom properties stored. User application successfully prepared.");
    } catch (MalformedURLException | AdalapiException ex) {
      throw new ExecutionPreparationException("Failed to obtain user application package.", ex);
    } catch (IOException | URISyntaxException ex) {
      throw new ExecutionPreparationException("Failed to extract user application package or failed to substitute DataWorkflow variables.", ex);
    } finally {
      if (propertiesStream != null) {
        try {
          propertiesStream.close();
        } catch (IOException ex) {
          //ignore this
        }
      }
    }
  }

  /**
   * Stage all data needed for pTask to the storage location accessible by the
   * processing environment. The staging phase can only be entered if pTask is
   * in status {@link DataWorkflowTask.TASK_STATUS#PREPARATION_FINISHED} or for
   * checks if the status is
   * {@link DataWorkflowTask.TASK_STATUS#STAGING}, {@link DataWorkflowTask.TASK_STATUS#STAGING_FINISHED}
   * or {@link DataWorkflowTask.TASK_STATUS#STAGING_FAILED}. Otherwise, a
   * StagingPreparationException will be thrown. If this method is called for
   * pTask for the first time, downloads will be scheduled for all digital
   * objects associated with pTask. The ids of the downloads will be stored in
   * the task, the task status will change to
   * {@link DataWorkflowTask.TASK_STATUS#STAGING} and FALSE will be returned. If
   * scheduling fails, the status will change to
   * {@link DataWorkflowTask.TASK_STATUS#STAGING_FAILED} and a
   * StagingPreparationException will be thrown.
   *
   * In subsequent calls, the transfer ids are used to query for the staging
   * status of all transfers. As long as not all transfers have finished, this
   * method will return FALSE and the status will remain
   * {@link DataWorkflowTask.TASK_STATUS#STAGING}. As soon as all data is
   * staged, the task status will switch to
   * {@link DataWorkflowTask.TASK_STATUS#STAGING_FINISHED} and TRUE is returned.
   * If any transfer fails, the status will switch to
   * {@link DataWorkflowTask.TASK_STATUS#STAGING_FAILED} and TRUE will be
   * returned in subsequent calls to indicate that the staging is in a final
   * state.
   *
   * In that case the task has to be reset to
   * {@link DataWorkflowTask.TASK_STATUS#PREPARATION_FINISHED} in order to
   * reattempt the staging process.
   *
   * @param pTask The task for which the transfers will be created/checked.
   *
   * @return TRUE if the staging process has finished (successful or not), FALSE
   * if at least one download is still unfinished.
   *
   * @throws StagingPreparationException if at least one associated object has
   * no valid transfer or if any download is in a failure state.
   */
  public final boolean performStaging(DataWorkflowTask pTask) throws StagingPreparationException {
    if (!DataWorkflowTask.TASK_STATUS.isStagingPhase(pTask.getStatus())) {
      throw new StagingPreparationException("Task with id " + pTask.getId() + " is in state " + pTask.getStatus() + ". Staging not possible.");
    }

    boolean stagingFinished = true;
    if (!DataWorkflowTask.TASK_STATUS.STAGING_FINISHED.equals(pTask.getStatus())) {
      //only enter as long as staging is not finished
      if (DataWorkflowTask.TASK_STATUS.PREPARATION_FINISHED.equals(pTask.getStatus())) {//schedule staging
        LOGGER.debug("Schedule staging for task with id {}", pTask.getId());
        stagingFinished = false;
        boolean statusUpdated = false;
        try {
          Properties objectTransferMap = DataWorkflowHelper.scheduleStaging(pTask);
          pTask.setObjectTransferMapAsObject(objectTransferMap);
          setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.STAGING);
          statusUpdated = true;
        } catch (StagingPreparationException | IOException ex) {
          setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.STAGING_FAILED);
          statusUpdated = true;
          throw new StagingPreparationException("Failed to schedule staging of input objects.", ex);
        } finally {
          if (!statusUpdated) {
            //try to update manually again
            setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.STAGING_FAILED);
          }
        }
      } else if (DataWorkflowTask.TASK_STATUS.STAGING.equals(pTask.getStatus())) {//status is STAGING, monitor status
        try {
          //update status
          Properties dataMap = pTask.getObjectTransferMapAsObject();
          Set<Entry<Object, Object>> entries = dataMap.entrySet();
          IAuthorizationContext ctx = DataWorkflowHelper.getTaskContext(pTask);
          for (Entry<Object, Object> entry : entries) {
            String objectId = (String) entry.getKey();
            Long transferId = Long.parseLong((String) entry.getValue());
            LOGGER.debug("Checking download status for object {} with download id {}", objectId, transferId);
            DownloadInformation result = DownloadInformationServiceLocal.getSingleton().getDownloadInformationById(transferId, ctx);
            if (result == null) {//failed due to missing download entry
              setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.STAGING_FAILED);
              LOGGER.error("No download information found for id " + transferId + ". Processing of task " + pTask.getId() + " cannot be continued.");
              //"stagingFinished" remains 'TRUE' as the staging process itself ends here
            } else {
              if (DOWNLOAD_STATUS.DOWNLOAD_READY.equals(result.getStatusEnum())) {
                LOGGER.debug("Download for object {} with download id {} is ready.", objectId, transferId);
              } else if (DOWNLOAD_STATUS.SCHEDULED.equals(result.getStatusEnum()) || DOWNLOAD_STATUS.PREPARING.equals(result.getStatusEnum())) {
                LOGGER.debug("Download for object {} with download id {} is still in preparation with status {}.", objectId, transferId, result.getStatusEnum());
                stagingFinished = false;
                //cancel loop over downloads
                break;
              } else {//transfer has failed normally
                setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.STAGING_FAILED);
                LOGGER.error("Download for object " + objectId + " with download id " + pTask.getId() + " has failed with status " + result.getStatusEnum());
                //"stagingFinished" remains 'TRUE' as the staging process itself ends here
              }
              //if we arrive here, all downloads have been done
              setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.STAGING_FINISHED);
            }
          }
        } catch (IOException ex) {
          setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.STAGING_FAILED);
          LOGGER.error("Failed to obtain object-transfer mapping for task with id " + pTask.getId(), ex);
          //"stagingFinished" remains 'TRUE' as the staging process itself ends here
        }
      } else {//status should be STAGING_FAILED...just set dataAvailable to false
        stagingFinished = false;
      }
    }//status is STAGING_FINISHED, just return TRUE
    return stagingFinished;
  }

  /**
   * Perform the processing phase of pTask. The processing phase can only be
   * entered if pTask is in status
   * {@link DataWorkflowTask.TASK_STATUS#STAGING_FINISHED} or for checks if the
   * status is
   * {@link DataWorkflowTask.TASK_STATUS#PROCESSING}, {@link DataWorkflowTask.TASK_STATUS#PROCESSING_FINISHED}
   * or {@link DataWorkflowTask.TASK_STATUS#PROCESSING_FAILED}. Otherwise, a
   * DataWorkflowProcessingException will be thrown. If this method is called
   * for pTask for the first time, the processing will be started, either
   * locally or remotely, depending on the handler implementation. Also
   * depending on the handler implementation the execution may or may not be
   * blocking (preferably it should be non-blocking). However, in all cases the
   * task status will switch to {@link DataWorkflowTask.TASK_STATUS#PROCESSING}
   * and FALSE will be returned after the first call (if no
   * DataWorkflowProcessingException is thrown) as indicator that the task has
   * not finished yet. This happens until
   * {@link #getTaskStatus(edu.kit.dama.dataworkflow.DataWorkflowTask)} returns
   * {@link DataWorkflowTask.TASK_STATUS#PROCESSING_FINISHED} or
   * {@link DataWorkflowTask.TASK_STATUS#PROCESSING_FAILED}. In both cases the
   * status of the task will be updated and TRUE will be returned as indicator
   * that the processing has finished. The caller has than to check whether the
   * processing was successful or not.
   *
   * Once a task has failed the processing can be retried by setting the task to
   * status {@link DataWorkflowTask.TASK_STATUS#STAGING_FINISHED}.
   *
   * @param pTask The task for which the processing will be created/checked.
   *
   * @return TRUE if the underlaying user application has finished (successfully
   * or not), FALSE if it has not yet finished.
   *
   * @throws DataWorkflowProcessingException if the task is in a wrong state or
   * if starting the user application has failed.
   */
  public final boolean performProcessing(DataWorkflowTask pTask) throws DataWorkflowProcessingException {
    if (!DataWorkflowTask.TASK_STATUS.isProcessingPhase(pTask.getStatus())) {
      throw new DataWorkflowProcessingException("Task with id " + pTask.getId() + " is in state " + pTask.getStatus() + ". Processing not possible.");
    }
    boolean processingFinished = false;
    if (DataWorkflowTask.TASK_STATUS.STAGING_FINISHED.equals(pTask.getStatus())) {//start processing
      boolean started = false;
      try {
        LOGGER.debug("Delegating user application start to implementation class.");
        startUserApplication(pTask);
        LOGGER.debug("User application start has returned. Updating status.");
        setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.PROCESSING);
        started = true;
      } finally {
        if (!started) {
          //exception occured...change status
          setTaskStatus(pTask, DataWorkflowTask.TASK_STATUS.PROCESSING_FAILED);
        }
      }
    } else if (DataWorkflowTask.TASK_STATUS.PROCESSING.equals(pTask.getStatus())) {//monitoring
      LOGGER.debug("Delegating process status check to implementation class.");
      DataWorkflowTask.TASK_STATUS currentStatus = getTaskStatus(pTask);
      //Check for valid state. As getTaskStatus() only allows the return of PROCESSING, PROCESSING_FINISHED or PROCESSING_FAILED any other result will lead to 
      //the new task status PROCESSING_FAILED. If a valid status is returned it'll be checked further.
      if (!DataWorkflowTask.TASK_STATUS.isProcessingPhase(currentStatus) || DataWorkflowTask.TASK_STATUS.STAGING_FINISHED.equals(currentStatus)) {
        LOGGER.error("Invalid status {} returned by handler implementation. Setting status to PROCESSING_FAILED.", currentStatus);
        currentStatus = DataWorkflowTask.TASK_STATUS.PROCESSING_FAILED;
      }
      LOGGER.debug("Status check returned current status of: {}", currentStatus);
      if (!DataWorkflowTask.TASK_STATUS.PROCESSING.equals(currentStatus)) {//not any longer in PROCESSING status. Status is either PROCESSING_FINISHED or PROCESSING_FAILED.
        LOGGER.debug("Updating task status to current status.");
        setTaskStatus(pTask, currentStatus);
        processingFinished = true;
      } else {//still PROCESSING status
        LOGGER.debug("Current status is still PROCESSING. Waiting until next check cycle.");
      }
    }

    //return status
    return processingFinished;
  }

  /**
   * Obtain a TASK_STATUS representing the current processing status of the user
   * application associated with the provided task. Possible results are:
   * {@link DataWorkflowTask.TASK_STATUS#PROCESSING}, {@link DataWorkflowTask.TASK_STATUS#PROCESSING_FINISHED}
   * and {@link DataWorkflowTask.TASK_STATUS#PROCESSING_FAILED}. All other
   * results will cause a failed execution!
   *
   * The EnvironmentHandler-implementation is responsible for performing
   * appropriate checks. It may use a handler-specific JobId stored in {@link DataWorkflowTask#getJobId()
   * } or any other custom mechanism to obtain a valid status.
   *
   * @param pTask The task for which the processing status should be obtained.
   *
   * @return One of the allowed TASK_STATUS values
   * {@link DataWorkflowTask.TASK_STATUS#PROCESSING}, {@link DataWorkflowTask.TASK_STATUS#PROCESSING_FINISHED}
   * and {@link DataWorkflowTask.TASK_STATUS#PROCESSING_FAILED}.
   */
  public abstract DataWorkflowTask.TASK_STATUS getTaskStatus(DataWorkflowTask pTask);

  /**
   * Start the user application for the provided task. The implementation of
   * this method is highly dependent of the execution environment. It defines
   * where the application main script is executed (locally or remotely), covers
   * the remote login if required based on handler-specific properties and the
   * actual execution. If the execution has started, this method call should
   * return. Checking for the execution status should be covered by calling {@link #isProcessingFinished(edu.kit.dama.dataworkflow.DataWorkflowTask)
   * } separately.
   *
   * @param pTask The task containing the application information.
   *
   * @throws DataWorkflowProcessingException If the application startup fails.
   */
  public abstract void startUserApplication(DataWorkflowTask pTask) throws DataWorkflowProcessingException;

  /**
   * Perform the ingest phase of pTask. The ingest phase can only be entered if
   * pTask is in status {@link DataWorkflowTask.TASK_STATUS#PROCESSING_FINISHED}
   * or for checks if the status is
   * {@link DataWorkflowTask.TASK_STATUS#INGEST}, {@link DataWorkflowTask.TASK_STATUS#INGEST_FINISHED}
   * or {@link DataWorkflowTask.TASK_STATUS#INGEST_FAILED}. Otherwise, a
   * IngestException will be thrown. If this method is called for pTask for the
   * first time, the pre-ingest will be performed. This means, all metadata
   * structures will be created, an ingest will be scheduled and the result data
   * of the processing will be linked to the ingest location. Finally, the
   * status of the ingest is set to PRE_INGEST_FINISHED and the task will be in
   * the status INGEST.
   *
   * In the next step the data will be ingested to the repository storage, which
   * takes place asynchronously. The ingest status will be checked using {@link #isIngestFinished(edu.kit.dama.dataworkflow.DataWorkflowTask)
   * }. As long as this method not returns TRUE, {@link #performIngest(edu.kit.dama.dataworkflow.DataWorkflowTask)
   * } will return FALSE and the task status will be
   * {@link DataWorkflowTask.TASK_STATUS#INGEST}. As soon as the ingest has
   * finished (successfully or not), TRUE is returned and the task will have the
   * status {@link DataWorkflowTask.TASK_STATUS#INGEST_FINISHED} or
   * {@link DataWorkflowTask.TASK_STATUS#INGEST_FAILED}.
   *
   * Once a ingest has been failed the ingest phase can be entered again by
   * setting the task to status
   * {@link DataWorkflowTask.TASK_STATUS#STAGING_FINISHED}.
   *
   * @param pTask The task for which the ingest will be scheduled/checked.
   *
   * @return TRUE if the ingest has finished (successfully or not), FALSE if it
   * has not yet finished.
   *
   * @throws IngestException if the task is in a wrong state or if
   * scheduling/checking the ingest has failed.
   */
  public boolean performIngest(DataWorkflowTask pTask) throws IngestException {
    if (!DataWorkflowTask.TASK_STATUS.isIngestPhase(pTask.getStatus())) {
      throw new IngestException("Task with id " + pTask.getId() + " is in state " + pTask.getStatus() + ". Ingest not possible.");
    }

    boolean ingestFinished = false;
    if (DataWorkflowTask.TASK_STATUS.PROCESSING_FINISHED.equals(pTask.getStatus())) {//start ingest
      LOGGER.debug("Processing is in finished state. Scheduling ingest.");
      scheduleIngest(pTask);
    } else if (DataWorkflowTask.TASK_STATUS.INGEST.equals(pTask.getStatus())) {//monitor ingest
      LOGGER.debug("Ingest is in running state. Checking status.");
      ingestFinished = isIngestFinished(pTask);
    } else {//INGEST_FINISHED or INGEST_FAILED
      ingestFinished = true;
    }
    return ingestFinished;
  }

  /**
   * Schedule the ingest of the output data of the provided task. At first, a
   * new digital object is created and added to the investigation linked to the
   * provided task. The object label will be statically assigned by the pattern
   * <i>"DataWorkflowProcessingResult of Task #TASK_ID"</i> where TASK_ID is the
   * numeric key of the task.
   *
   * In the next step, an ingest is scheduled. As soon as the ingest is
   * prepared, all files located in the output directory of pTask will be linked
   * into the staging location. Due to the fact, that repository and execution
   * environment can access the same location, this can be done immediately. As
   * soon as all file links are created, the pre-ingest is marked as finished
   * and the final ingest step into the repository storage can take place
   * asynchronously. The status of the ingest is then checked within {@link #performIngest(edu.kit.dama.dataworkflow.DataWorkflowTask)
   * } by calling {@link #isIngestFinished(edu.kit.dama.dataworkflow.DataWorkflowTask)
   * }.
   *
   * @param pTask The task whose output should be ingested.
   *
   * @throws IngestException If the ingest scheduling fails for some reason,
   * e.g. if the metadata/ingest creation fails.
   */
  private void scheduleIngest(DataWorkflowTask pTask) throws IngestException {
    IAuthorizationContext ctx = DataWorkflowHelper.getTaskContext(pTask);
    File stagingPath = DataWorkflowHelper.getStagingBasePath(pTask);
    File outputDir = DataWorkflowHelper.getTaskOutputDirectory(stagingPath);
    LOGGER.debug("Performing ingest of output directory {}", outputDir);
    setTaskStatus(pTask, TASK_STATUS.INGEST);

    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    Investigation inv = null;
    try {
      inv = mdm.find(Investigation.class, pTask.getInvestigationId());
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to access investigation with id " + pTask.getInvestigationId(), ex);
    }

    if (inv == null) {
      throw new IngestException("Failed to obtain investigation. Ingest not possible.");
    }
    LOGGER.debug("Creating result data object for task {}", pTask.getId());
    DigitalObject resultObject = DigitalObject.factoryNewDigitalObject();
    resultObject.setLabel("DataWorkflowProcessingResult of Task #" + pTask.getId());
    resultObject.setInvestigation(inv);
    resultObject.setUploadDate(new Date());
    resultObject.setUploader(DataWorkflowHelper.getContact(pTask));
    try {
      LOGGER.debug("Storing digital object with id {} for result data of task {}", resultObject.getDigitalObjectId(), pTask.getId());
      resultObject = mdm.save(resultObject);
      LOGGER.debug("Adding object with base id {} to investigation with id {}", resultObject.getBaseId(), inv.getInvestigationId());
      inv.addDataSet(resultObject);
      mdm.update(inv);
      LOGGER.debug("Base metadata for task with id {} successfully stored.", pTask.getId());
    } catch (UnauthorizedAccessAttemptException | EntityNotFoundException ex) {
      throw new IngestException("Failed to create base metadata structure for task " + pTask.getJobId(), ex);
    }

    LOGGER.debug("Storing digital object transition information");
    try {
      DataWorkflowTransition t = new DataWorkflowTransition(pTask);
      Properties inputObjects = pTask.getObjectViewMapAsObject();
      Set<Entry<Object, Object>> entries = inputObjects.entrySet();
      for (Entry<Object, Object> entry : entries) {
        try {
          LOGGER.debug("Getting input object with object id {}", (String) entry.getKey());
          DigitalObject input = mdm.findSingleResult("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier='" + (String) entry.getKey() + "'", DigitalObject.class);
          if (input == null) {
            throw new IngestException("Failed to obtain digital object for id " + (String) entry.getValue() + ". Result is null. Unable to build object transition.");
          }
          LOGGER.debug("Adding input object to transition.");
          t.addInputMapping(input, (String) entry.getValue());
        } catch (UnauthorizedAccessAttemptException ex) {
          throw new IngestException("Failed to obtain digital object for id " + (String) entry.getValue() + ". Unable to build up object transition.", ex);
        }
      }
      //add single output object
      t.addOutputObject(resultObject);
      LOGGER.debug("Storing transition for task {}", pTask.getId());
      mdm.save(t);
      LOGGER.debug("DataWorkflow task transition for task {} successfully stored.", pTask.getId());
    } catch (IOException ex) {
      throw new IngestException("Failed to get object-view map to build up object transitions for task " + pTask.getId(), ex);
    } catch (UnauthorizedAccessAttemptException ex) {
      throw new IngestException("Failed to store object transitions for task " + pTask.getId(), ex);
    }

    LOGGER.debug("Collecting TransferClientProperties");
    TransferClientProperties props = new TransferClientProperties();
    props.setSendMailNotification(true);
    props.setReceiverMail(DataWorkflowHelper.getContact(pTask).getEmail());
    props.setStagingAccessPointId(pTask.getExecutionEnvironment().getStagingAccessPointId());
    IngestInformation ingest;
    try {
      LOGGER.debug("Scheduling new ingest for object with id {}", resultObject.getDigitalObjectId());
      ingest = IngestInformationServiceLocal.getSingleton().prepareIngest(resultObject.getDigitalObjectId(), props, ctx);
      LOGGER.debug("Scheduled ingest with id {}", ingest.getId());
    } catch (TransferPreparationException ex) {
      throw new IngestException("Failed to prepare ingest for task results.", ex);
    }

    //Wait for the ingest to be prepared. Basically, this should be the case immediately.
    int sleepCnt = 6;
    while (!INGEST_STATUS.PRE_INGEST_SCHEDULED.equals(ingest.getStatusEnum()) && sleepCnt > 0) {
      try {
        Thread.sleep(5000);
        sleepCnt--;
      } catch (InterruptedException ex) {
      }
      //reload ingest information
      ingest = IngestInformationServiceLocal.getSingleton().getIngestInformationById(ingest.getId(), ctx);
      sleepCnt--;
    }

    //Check again for status...if it is not PRE_INGEST_SCHEDULED the preparation has failed.
    if (!INGEST_STATUS.PRE_INGEST_SCHEDULED.equals(ingest.getStatusEnum())) {
      //ingest preparation not successful...abort.
      throw new IngestException("Preparation of result ingest did not succeed within one minute. Current status is " + ingest.getStatusEnum() + ". Ingest will be aborted.");
    }

    AbstractStagingAccessPoint ap = StagingConfigurationManager.getSingleton().getAccessPointById(pTask.getExecutionEnvironment().getStagingAccessPointId());
    File dataFolder = ap.getLocalPathForUrl(ingest.getDataFolderURL(), ctx);

    for (File outputFile : outputDir.listFiles()) {
      try {
        SystemUtils.createSymbolicLink(outputFile, new File(dataFolder, outputFile.getName()));
      } catch (IOException ex) {
        throw new IngestException("Linking output data from " + outputDir + " to ingest data folder " + dataFolder + " failed. Ingest will be aborted.", ex);
      }
    }
    LOGGER.debug("Output data successfully linked to ingest data directory. Setting status of ingest with id {} to {}", ingest.getId(), INGEST_STATUS.PRE_INGEST_FINISHED);
    if (IngestInformationServiceLocal.getSingleton().updateStatus(ingest.getId(), INGEST_STATUS.PRE_INGEST_FINISHED.getId(), null, ctx) == 1) {
      LOGGER.debug("Ingest status successfully updated. Pre-Ingest finished.");
      Properties ingestTransferMap = new Properties();
      ingestTransferMap.put(ingest.getDigitalObjectId(), Long.toString(ingest.getId()));
      try {
        LOGGER.debug("Updating object-transfer map in task with id {}", pTask.getId());
        pTask.setObjectTransferMapAsObject(ingestTransferMap);
        LOGGER.debug("Persisting updated task.");
        DataWorkflowPersistenceImpl.getSingleton(ctx).updateTask(pTask);
        LOGGER.debug("Object-transfer map in task successfully updated.");
      } catch (UnauthorizedAccessAttemptException | IOException ex) {
        throw new IngestException("Failed to update object-transfer map in task with id {} " + pTask.getId(), ex);
      }
    } else {
      throw new IngestException("Failed to update ingest status. No rows where modified.");
    }
  }

  /**
   * Check if the scheduled ingest has finished. Therefor, the associated ingest
   * stored in the object-transfer-map of pTask will be checked for the status
   * {@link INGEST_STATUS#INGEST_FINISHED}. If the ingest status is
   * {@link INGEST_STATUS#PRE_INGEST_FINISHED} or
   * {@link INGEST_STATUS#INGEST_RUNNING} FALSE is returned. If the ingest
   * status is {@link INGEST_STATUS#INGEST_FINISHED} or
   * {@link INGEST_STATUS#INGEST_FAILED} then TRUE is returned as the ingest is
   * basically finished because it won't change its state without further
   * interaction.
   *
   * @param pTask The task for which the ingest will be checked.
   *
   * @return TRUE the ingest has finished (successfully or not), FALSE if the
   * ingest is not yet or still running.
   *
   * @throws IngestException If the ingest has failed.
   */
  private boolean isIngestFinished(DataWorkflowTask pTask) throws IngestException {
    boolean ingestFinished = true;
    try {
      Properties dataMap = pTask.getObjectTransferMapAsObject();
      Set<Entry<Object, Object>> entries = dataMap.entrySet();
      IAuthorizationContext ctx = DataWorkflowHelper.getTaskContext(pTask);
      for (Entry<Object, Object> entry : entries) {
        String objectId = (String) entry.getKey();
        Long transferId = Long.parseLong((String) entry.getValue());
        LOGGER.debug("Checking ingest status for object {} with ingest id {}", objectId, transferId);
        IngestInformation result = IngestInformationServiceLocal.getSingleton().getIngestInformationById(transferId, ctx);
        if (result == null) {
          setTaskStatus(pTask, TASK_STATUS.INGEST_FAILED);
          throw new IngestException("No ingest information found for id " + transferId + ". Processing of task " + pTask.getId() + " cannot be continued.");
        }
        if (INGEST_STATUS.INGEST_FINISHED.equals(result.getStatusEnum())) {
          LOGGER.debug("Ingest for object {} with ingest id {} has finished.", objectId, transferId);
          setTaskStatus(pTask, TASK_STATUS.INGEST_FINISHED);
        } else if (INGEST_STATUS.INGEST_RUNNING.equals(result.getStatusEnum()) || INGEST_STATUS.PRE_INGEST_FINISHED.equals(result.getStatusEnum())) {
          LOGGER.debug("Ingest for object {} with ingest id {} is still in progress with status {}.", objectId, transferId, result.getStatusEnum());
          ingestFinished = false;
          break;
        } else {
          setTaskStatus(pTask, TASK_STATUS.INGEST_FAILED);
          throw new IngestException("Ingest for object " + objectId + " with ingest id " + pTask.getId() + " has failed with status " + result.getStatusEnum());
        }
      }
    } catch (IOException ex) {
      throw new IngestException("Failed to obtain object-transfer mapping for task with id " + pTask.getId(), ex);
    }

    return ingestFinished;
  }

  /**
   * Perform the cleanup phase of pTask. The cleanup phase can only be entered
   * if pTask is in status {@link DataWorkflowTask.TASK_STATUS#INGEST_FINISHED}
   * or for checks if the status is
   * {@link DataWorkflowTask.TASK_STATUS#CLEANUP}, {@link DataWorkflowTask.TASK_STATUS#CLEANUP_FINISHED}
   * or {@link DataWorkflowTask.TASK_STATUS#CLEANUP_FAILED}. Otherwise, a
   * CleanupException will be thrown.
   *
   * If this method is called for pTask for the first time, the entire cleanup
   * procedure is executed. Internally, the task status will change to
   * {@link DataWorkflowTask.TASK_STATUS#CLEANUP} and then directly to
   * {@link DataWorkflowTask.TASK_STATUS#CLEANUP_FINISHED} or
   * {@link DataWorkflowTask.TASK_STATUS#CLEANUP_FAILED}, depending on the
   * cleanup result. However, in both cases the first call to this method for
   * pTask will return TRUE as long as the task is in the cleanup phase.
   *
   * Once a cleanup has been failed the cleanup phase can be entered again by
   * setting the task to status
   * {@link DataWorkflowTask.TASK_STATUS#INGEST_FINISHED}.
   *
   * @param pTask The task for which the cleanup will be performed.
   *
   * @return TRUE if the cleanup has finished (successfully or not). Currently,
   * except a CleanupException, there is not other possible result than TRUE.
   *
   * @throws CleanupException if the task is in a wrong state.
   */
  public boolean performCleanup(DataWorkflowTask pTask) throws CleanupException {
    if (!DataWorkflowTask.TASK_STATUS.isCleanupPhase(pTask.getStatus())) {
      throw new CleanupException("Task with id " + pTask.getId() + " is in state " + pTask.getStatus() + ". Cleanup not possible.");
    }

    //cleanup will always be finished after the first call as it is implemented synchronously for the moment.
    boolean cleanupFinished = true;
    if (DataWorkflowTask.TASK_STATUS.INGEST_FINISHED.equals(pTask.getStatus())) {//start ingest
      LOGGER.debug("Ingest is in finished state. Cleaning up task.");
      //setting cleanup state...actually not necessary, just to follow the protocol
      setTaskStatus(pTask, TASK_STATUS.CLEANUP);
      //remove the staging base path of the task
      File stagingPath = DataWorkflowHelper.getStagingBasePath(pTask);
      try {
        LOGGER.debug("Try to remove task directory of task {} at {}", pTask.getId(), stagingPath);
        FileUtils.deleteDirectory(stagingPath);
        LOGGER.debug("Task directory at {} successfully deleted.", stagingPath);
        setTaskStatus(pTask, TASK_STATUS.CLEANUP_FINISHED);
      } catch (IOException ex) {
        LOGGER.error("Failed to remove task directory of task " + pTask.getId() + " at " + stagingPath + ". Setting task status to CLEANUP_FAILED.", ex);
        setTaskStatus(pTask, TASK_STATUS.CLEANUP_FAILED);
      }
    }

    return cleanupFinished;
  }
}
