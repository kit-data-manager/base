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
package edu.kit.dama.dataworkflow.util;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizationServiceLocal;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.services.impl.download.DownloadInformationServiceLocal;
import edu.kit.dama.staging.util.StagingConfigurationManager;
import edu.kit.dama.util.SystemUtils;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import edu.kit.dama.dataworkflow.exceptions.StagingPreparationException;
import edu.kit.dama.dataworkflow.exceptions.UnsupportedOperatingSystemException;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public final class DataWorkflowHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataWorkflowHelper.class);
  private static final List<DataWorkflowTask.TASK_STATUS> filteredStatusCodes;
  private static final List<DataWorkflowTask.TASK_STATUS> unfilteredStatusCodes;

  static {
    filteredStatusCodes = new ArrayList<>();
    unfilteredStatusCodes = new ArrayList<>();
    for (DataWorkflowTask.TASK_STATUS s : DataWorkflowTask.TASK_STATUS.values()) {
      unfilteredStatusCodes.add(s);
      if (!DataWorkflowTask.TASK_STATUS.isErrorState(s) && !DataWorkflowTask.TASK_STATUS.isFinishedState(s)) {
        filteredStatusCodes.add(s);
      }
    }
  }
  public final static String DATA_IN_DIR = "data_in";
  public final static String DATA_OUT_DIR = "data_out";
  public final static String WORKING_DIR = "working";
  public final static String TEMP_DIR = "temp";

  public static final String DATA_IN_DIR_VARIABLE = "${data.input.dir}";
  public static final String DATA_OUT_DIR_VARIABLE = "${data.output.dir}";
  public static final String TEMP_DIR_VARIABLE = "${temp.dir}";
  public static final String WORKING_DIR_VARIABLE = "${working.dir}";

  /**
   * Obtain the base path for data access for the provided task from the
   * repository perspective, which means, that the local base path of the
   * AccessPoint associated with the ExecutionEnvironment of the task will be
   * the first part of this base path. The base path will contain directories
   * for input and output data, temporary files and the working directory
   * containing the user application. The base path consists of the following
   * parts:
   * <ul>
   * <li>pTask.getConfiguration().getLocalBasePath()</li>
   * <li>pContext.getUserId().getStringRepresentation()</li>
   * <li>pTask.getId().toString()</li>
   * </ul>
   *
   * For obtaining the base path from the perspective of the execution
   * environment the method {@link #getExecutionBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * } must be used.
   *
   * @param pTask The task for which the base path should be obtained.
   *
   * @return The staging base path as file.
   */
  public static File getStagingBasePath(DataWorkflowTask pTask) {
    ExecutionEnvironmentConfiguration env = pTask.getExecutionEnvironment();
    String accessPointId = env.getStagingAccessPointId();
    StagingAccessPointConfiguration accessPointConfig = StagingConfigurationManager.getSingleton().getAccessPointConfigurationById(accessPointId);
    //Get local base path of access point. This is be the repository-accessible path that will be available for the task at execution time
    //under env.getAccessPointLocalBasePath()
    //For the moment we use this path to setup our task-specific folder structure:
    //<BASE_PATH>/<USER_ID>/<JOB_ID>/DATA_IN, DATA_OUT, WORKING, TEMP
    String localBasePath = accessPointConfig.getLocalBasePath();
    //obtain task base path <BASE_PATH>/<USER_ID>/<JOB_ID>/
    return new File(localBasePath, getTaskContext(pTask).getUserId().getStringRepresentation() + File.separator + pTask.getId().toString() + File.separator);
  }

  /**
   * FileFilter for extracting all files in a folder, except directories and the
   * file 'dataworkflow_substitution'.
   */
  private final static FileFilter VAR_FILTER = new FileFilter() {
    @Override
    public boolean accept(File pathname) {
      return (pathname.isFile() && !pathname.getName().equals("dataworkflow_substitution"));
    }
  };

  /**
   * Get the base path within the execution environment. This path reflects the
   * same physical location as the result of {@link #getStagingBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * }.
   *
   * @param pTask The task for which the base path should be obtained.
   *
   * @return The execution base path as file.
   */
  public static File getExecutionBasePath(DataWorkflowTask pTask) {
    //Get local base path of the execution environment. This might a a non-repository-accessible path that will be available for the task at execution time.
    //The path is set up according to the following schema: 
    //<BASE_PATH>/<USER_ID>/<JOB_ID>/DATA_IN, DATA_OUT, WORKING, TEMP
    ExecutionEnvironmentConfiguration env = pTask.getExecutionEnvironment();
    String localBasePath = env.getAccessPointLocalBasePath();
    //obtain task base path <BASE_PATH>/<USER_ID>/<JOB_ID>/
    return new File(localBasePath, getTaskContext(pTask).getUserId().getStringRepresentation() + File.separator + pTask.getId().toString() + File.separator);
  }

  /**
   * Get the task input directory which is a sub-directory named
   * {@link #DATA_IN_DIR} of task base path.
   *
   * @param pTaskBasePath The task base path obtained using {@link #getStagingBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * } or {@link #getExecutionBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * }.
   *
   * @return The task input directory.
   */
  public static File getTaskInputDirectory(File pTaskBasePath) {
    return new File(pTaskBasePath, "data_in");
  }

  /**
   * Get the task output directory which is a sub-directory named
   * {@link #DATA_OUT_DIR} of task base path.
   *
   * @param pTaskBasePath The task base path obtained using {@link #getStagingBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * } or {@link #getExecutionBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * }.
   *
   * @return The task output directory.
   */
  public static File getTaskOutputDirectory(File pTaskBasePath) {
    return new File(pTaskBasePath, "data_out");
  }

  /**
   * Get the task temp directory which is a sub-directory named
   * {@link #TEMP_DIR} of task base path.
   *
   * @param pTaskBasePath The task base path obtained using {@link #getStagingBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * } or {@link #getExecutionBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * }.
   *
   * @return The task temp directory.
   */
  public static File getTaskTempDirectory(File pTaskBasePath) {
    return new File(pTaskBasePath, "temp");
  }

  /**
   * Get the task working directory which is a sub-directory named
   * {@link #WORKING_DIR} of task base path.
   *
   * @param pTaskBasePath The task base path obtained using {@link #getStagingBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * } or {@link #getExecutionBasePath(edu.kit.dama.dataworkflow.DataWorkflowTask, edu.kit.dama.authorization.entities.IAuthorizationContext)
   * }.
   *
   * @return The task working directory.
   */
  public static File getTaskWorkingDirectory(File pTaskBasePath) {
    return new File(pTaskBasePath, "working");
  }

  /**
   * Obtain the contact entity for the provided task. If no entity is found,
   * UserData.NO_USER will be returned.
   *
   * @param pTask The task for which to obtain the contact information.
   *
   * @return The contact entity or UserData.NO_USER.
   */
  public static UserData getContact(DataWorkflowTask pTask) {
    //setting default user mail value
    UserData result = UserData.NO_USER;

    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    IAuthorizationContext ctx = getTaskContext(pTask);
    mdm.setAuthorizationContext(ctx);
    try {
      LOGGER.debug("Getting contact information for task with id {}", pTask.getId());
      result = mdm.findSingleResult("SELECT o FROM UserData o WHERE o.distinguishedName = \"" + ctx.getUserId().getStringRepresentation() + "\"", UserData.class);
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.warn("Failed to access user data for user with id " + ctx.getUserId() + ". Returning UserData.NO_USER", ex);
    } finally {
      mdm.close();
    }
    return result;
  }

  /**
   * Schedule the staging process(es) for the data needed by the provided task.
   * The process contains the following points:
   * <ul>
   * <li>Creation of the task base path including data_in, data_out, working and
   * temp directories.</li>
   * <li>Obtaining selected data organization views of digital objects to
   * stage.</li>
   * <li>Schedule downloads for the content of each digital object.</li>
   * <li>Create symbolic links of the first-level content of each view into
   * 'data_in'. Existing files will be skipped.</li>
   * </ul>
   * As soon as the staging for all digital objects is done, the symbolic links
   * in the data_in directory should point the valid data located inside the
   * different staging locations.
   *
   * @param pTask The task for which the staging should be scheduled.
   *
   * @return A properties object containing the object-transferId mapping. This
   * mapping should be stored in the DataWorkflowTask in order to be able to check the
   * data staging process.
   *
   * @throws StagingPreparationException if anything fails.
   */
  public static Properties scheduleStaging(DataWorkflowTask pTask) throws StagingPreparationException {
    Properties objectDownloadMap = new Properties();
    IAuthorizationContext ctx = getTaskContext(pTask);
    File taskBasePath = getStagingBasePath(pTask);
    LOGGER.debug("Checking task base path {}", taskBasePath);
    if (taskBasePath.exists()) {
      LOGGER.debug("Task base path at " + taskBasePath + " already exists.");
    } else {
      LOGGER.debug("Task base path does not exist. Creating directory strucute {}", taskBasePath);
      if (!taskBasePath.mkdirs()) {
        throw new StagingPreparationException("Failed to create task base path at " + taskBasePath);
      }
      LOGGER.debug("Task base path structure successfully created.");
    }

    File inputDir = getTaskInputDirectory(taskBasePath);
    File outputDir = getTaskOutputDirectory(taskBasePath);
    File tempDir = getTaskTempDirectory(taskBasePath);
    File workingDir = getTaskWorkingDirectory(taskBasePath);
    LOGGER.debug("Creating directories:");
    LOGGER.debug(" - Input: {}", inputDir);
    LOGGER.debug(" - Output: {}", outputDir);
    LOGGER.debug(" - Working: {}", workingDir);
    LOGGER.debug(" - Temp: {}", tempDir);

    LOGGER.debug("Obtaining object-view list for DataWorkflow task {}", pTask.getId());
    Properties objectViewMap = null;

    try {
      objectViewMap = pTask.getObjectViewMapAsObject();
    } catch (IOException ex) {
      throw new StagingPreparationException("Failed to deserialize object-view list from task " + pTask.getId());
    }

    try {
      TransferClientProperties props = new TransferClientProperties();
      String accessPointId = pTask.getExecutionEnvironment().getStagingAccessPointId();
      AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(accessPointId);
      LOGGER.debug("Adding staging acccess point id {} to TransferClientProperties.", accessPointId);
      props.setStagingAccessPointId(accessPointId);
      String mail = getContact(pTask).getEmail();
      LOGGER.debug("Adding contact mail {} to TransferClientProperties.", mail);
      props.setReceiverMail(mail);
      Set<Entry<Object, Object>> entries = objectViewMap.entrySet();

      for (Entry<Object, Object> entry : entries) {
        String objectId = (String) entry.getKey();
        String viewId = (String) entry.getValue();
        DigitalObjectId doid = new DigitalObjectId(objectId);
        IFileTree tree = DataOrganizationServiceLocal.getSingleton().loadFileTree(doid, viewId, ctx);
        LOGGER.debug("Scheduling download for object {} and view {}", objectId, viewId);
        DownloadInformation downloadInfo = DownloadInformationServiceLocal.getSingleton().scheduleDownload(doid, tree, props, ctx);
        LOGGER.debug("Putting transfer id {} for object {} to object-transfer list.", downloadInfo.getId(), objectId);
        objectDownloadMap.put(doid.getStringRepresentation(), Long.toString(downloadInfo.getId()));

        List<? extends IDataOrganizationNode> firstLevelNodes = tree.getRootNode().getChildren();
        LOGGER.debug("Creating links for {}  first level data organization nodes", firstLevelNodes.size());
        for (IDataOrganizationNode node : firstLevelNodes) {
          File linkedFile = new File(inputDir + File.separator + node.getName());
          LOGGER.debug("Creating link for file {}", linkedFile);
          if (linkedFile.exists()) {
            LOGGER.error("File link " + linkedFile + " already exists. Skipping link creation but processing might fail.");
          } else {
            LOGGER.debug("Writing data to staging URL {}", downloadInfo.getStagingUrl());
            File dataPath = accessPoint.getLocalPathForUrl(downloadInfo.getDataFolderURL(), ctx);
            SystemUtils.createSymbolicLink(new File(dataPath, node.getName()), linkedFile);
            LOGGER.debug("Link successfully created.");
          }
        }
        LOGGER.debug("Staging of object {} for task {} successfully scheduled.", objectId, pTask.getId());
      }
      LOGGER.debug("Scheduling of all objects for task {} successfully finished.", pTask.getId());
    } catch (IOException | EntityNotFoundException | TransferPreparationException ex) {
      //Failed to create link/view not found/transfer preparation has failed
      throw new StagingPreparationException("Failed to prepare task directory structure for task " + pTask.getId(), ex);
    }
    return objectDownloadMap;
  }

  /**
   * Abort the staging process for all input objects of the provided task. The
   * associated download entities will be set to DOWNLOAD_REMOVED.
   *
   * @param pTask The task for which the staging should be aborted.
   */
  public static void abortStaging(DataWorkflowTask pTask) {
    try {
      //update status
      Properties dataMap = pTask.getObjectTransferMapAsObject();
      Set<Entry<Object, Object>> entries = dataMap.entrySet();
      IAuthorizationContext ctx = DataWorkflowHelper.getTaskContext(pTask);
      for (Entry<Object, Object> entry : entries) {
        String objectId = (String) entry.getKey();
        Long transferId = Long.parseLong((String) entry.getValue());
        LOGGER.debug("Checking download status for object {} with download id {}", objectId, transferId);
        if (DownloadInformationServiceLocal.getSingleton().updateStatus(transferId, DOWNLOAD_STATUS.DOWNLOAD_REMOVED.getId(), "Download aborted.", ctx) != 1) {
          LOGGER.warn("Download with id {} was not properly aborted.", transferId);
        } else {
          LOGGER.debug("Download with id {} aborted.", transferId);
        }
      }
    } catch (IOException ex) {
      LOGGER.error("Failed to obtain object-transfer map. Unable to abort staging.", ex);
    }
  }

  /**
   * Get the main executable of the user application of the provided task. This
   * method will check for an executable run.sh or run.bat depending on the
   * detected operating system. Currently, Windows, Unix and MacOSX are
   * detected. If the OS check determines another system, an
   * {@link UnsupportedOperatingSystemException} will be thrown. This is also
   * the case, if the user application does not contain an according start
   * script (run.bat for Windows or run.sh for Unix/MacOSX). In this case, the
   * application is not supporting the OS of the execution environment.
   *
   * @param pTask The task for which the main executable should be determined.
   *
   * @return The file location of the main executable within the execution
   * environment.
   *
   * @throws UnsupportedOperatingSystemException If the user application of
   * pTask does not provide an executable for this operating system.
   */
  public static File getTaskMainExecutable(DataWorkflowTask pTask) throws UnsupportedOperatingSystemException {
    File basePath = getExecutionBasePath(pTask);
    File workingDir = getTaskWorkingDirectory(basePath);
    File mainExecutable = null;
    if (org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS) {
      mainExecutable = new File(workingDir, "run.bat");
    } else if (org.apache.commons.lang3.SystemUtils.IS_OS_UNIX || org.apache.commons.lang3.SystemUtils.IS_OS_MAC_OSX) {
      mainExecutable = new File(workingDir, "run.sh");
    }

    LOGGER.info("Main executable is: {}", mainExecutable);
    if (mainExecutable == null) {
      //no OS branch entered, OS not supported.
      throw new UnsupportedOperatingSystemException("Your system was not detected to be Windows, Unix or MacOSX. Unable to continue.");
    } else if (!mainExecutable.exists()) {
      //executable does not exists, application support for OS not available.
      throw new UnsupportedOperatingSystemException("The user application of task with the id " + pTask.getId() + " is not supporting your operating system. Unable to continue.");
    }
    //return the main executable which shouldn't be null and should exist
    return mainExecutable;
  }

  /**
   * Search within the working directory of the provided task for files, where
   * DataWorkflow variables (e.g. working-, temp-, input- and output-directory) should
   * be substituted. Substitutions will be applied to all files in directories
   * containing a file named 'dataworkflow_substitution'. If this file is not within a
   * directory, variable substitution will be skipped for this directory.
   *
   * @param pTask The task whose working directory should be checked for
   * substitution.
   *
   * @throws IOException If the replacement operation fails for some reason.
   * @throws URISyntaxException If any of the URLs in the task (input, output,
   * temp or working dir URL) is invalid.
   */
  public static void substituteVariablesInDirectory(DataWorkflowTask pTask) throws IOException, URISyntaxException {
    //perform substitution in base path in every case...then continue
    performSubstitution(pTask, getStagingBasePath(pTask));
    //perform substitution in sub-folders
    substituteVariablesInDirectory(pTask, getStagingBasePath(pTask));
  }

  /**
   * Internal method for recursive substition within pTargetPath.
   *
   * @param pTask The task whose working directory should be checked for
   * substitution.
   * @param pTargetPath The target path.
   *
   * @throws IOException If the replacement operation fails for some reason.
   * @throws URISyntaxException If any of the URLs in the task (input, output,
   * temp or working dir URL) is invalid.
   */
  private static void substituteVariablesInDirectory(DataWorkflowTask pTask, File pTargetPath) throws IOException, URISyntaxException {
    if (pTargetPath == null || !pTargetPath.exists()) {
      LOGGER.warn("Argument pTargetPath must not be 'null' and must exist");
      return;
    }
    LOGGER.info("Checking directory '" + pTargetPath.getPath() + "'");

    //get a list of relevant files
    File[] relevantFileList = pTargetPath.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        //accept a file only if it is a directory or
        //if it is a file called "replace_vars", which marks directories that should be affected by replacements
        return (pathname.isDirectory() || (pathname.isFile() && pathname.getName().equals("dataworkflow_substitution")));
      }
    });

    //go through all listed files within pTargetPath
    for (File relevantFile : relevantFileList) {
      if (relevantFile.isDirectory()) {
        //continue recursively
        substituteVariablesInDirectory(pTask, relevantFile);
      } else {
        //due to the filtering we have now a replace_var file. Therefore we have to replace variables in all files within its parent directory.
        performSubstitution(pTask, relevantFile.getParentFile());
      }
    }
  }

  /**
   * Helper method to perform the actual substitution.
   *
   * @param pTask The task whose working directory should be checked for
   * substitution.
   * @param pTargetPath The target path.
   *
   * @throws IOException If the replacement operation fails for some reason.
   * @throws URISyntaxException If any of the URLs in the task (input, output,
   * temp or working dir URL) is invalid.
   */
  private static void performSubstitution(DataWorkflowTask pTask, File pDirectory) throws IOException, URISyntaxException {
    File[] relevantFileList = pDirectory.listFiles(VAR_FILTER);
    LOGGER.info("Substituting variables in " + relevantFileList.length + ((relevantFileList.length == 1) ? " file" : " files"));

    for (File f : relevantFileList) {
      if (f.length() > 10 * FileUtils.ONE_MB) {
        LOGGER.warn("File {} has a size of {} bytes. Variable substitution is only supported for files with less than 10MB. File is skipped.", f, f.length());
        continue;
      }
      //perform replacement
      LOGGER.info(" * Substituting variables in file '" + f.getPath() + "'");
      DataInputStream din = null;
      FileOutputStream fout = null;
      try {
        LOGGER.info("   - Reading input file");
        byte[] data = new byte[(int) f.length()];
        din = new DataInputStream(new FileInputStream(f));
        din.readFully(data);

        LOGGER.info("   - Substituting variables");
        String dataString = new String(data);
        String inputDirReplacement = new URL(pTask.getInputDirectoryUrl()).toURI().getPath().substring(1);
        String outputDirReplacement = new URL(pTask.getOutputDirectoryUrl()).toURI().getPath().substring(1);
        String workingDirReplacement = new URL(pTask.getWorkingDirectoryUrl()).toURI().getPath().substring(1);
        String tempDirReplacement = new URL(pTask.getTempDirectoryUrl()).toURI().getPath().substring(1);

        LOGGER.info("     " + DATA_IN_DIR + ": " + inputDirReplacement);
        LOGGER.info("     " + DATA_OUT_DIR + ": " + outputDirReplacement);
        LOGGER.info("     " + TEMP_DIR + ": " + tempDirReplacement);
        LOGGER.info("     " + WORKING_DIR + ": " + workingDirReplacement);
        //replace all variables
        //To obtain a proper path format the input paths are put into a file object and the URI path is used for replacement. Therefore differences between
        //source and destination platform are not relevant. Due to the URI.toPath() returns the path with leading slash, we use the path beginning with
        //the second index to avoid problems with other programming languages not able to deal with the leading slash.
        dataString = dataString.replaceAll(Pattern.quote(DATA_IN_DIR_VARIABLE), inputDirReplacement).
                replaceAll(Pattern.quote(DATA_OUT_DIR_VARIABLE), outputDirReplacement).
                replaceAll(Pattern.quote(TEMP_DIR_VARIABLE), tempDirReplacement).
                replaceAll(Pattern.quote(WORKING_DIR_VARIABLE), workingDirReplacement);
        LOGGER.info("   - Writing output file");
        fout = new FileOutputStream(f);
        fout.write(dataString.getBytes());
        fout.flush();
        LOGGER.info(" * Substituting operations finished successfully");
      } finally {
        try {
          if (din != null) {
            din.close();
          }
        } catch (IOException ioe) {
        }
        try {
          if (fout != null) {
            fout.close();
          }
        } catch (IOException ioe) {
        }
      }
    }
    LOGGER.info("Directory {} processed successfully", pDirectory);
  }

  /**
   * Get the authorization context used to execute the provided task. To obtain
   * the context, {@link DataWorkflowTask#getExecutorId() } and {@link DataWorkflowTask#getExecutorGroupId()
   * } are used defining UserId and GroupId of the returned
   * AuthorizationContext.
   *
   * @param pTask The task for which the context should be obtained.
   *
   * @return The AuthorizationContext for pTask.
   */
  public static IAuthorizationContext getTaskContext(DataWorkflowTask pTask) {
    return new AuthorizationContext(new UserId(pTask.getExecutorId()), new GroupId(pTask.getExecutorGroupId()), Role.MEMBER);
  }

  /**
   * Get the list of DataWorkflowTasks that have to be processed. The list can be
   * filtered by ids or by the status of contained tasks and it can be limited
   * to a max. size. In every case the list is storted ascending by the last
   * update of the contained tasks.
   *
   * @param pTaskIds The tasks with these Ids will be in the result list. If
   * argument pFilter is TRUE, the result list might be smaller if one or more
   * tasks are in a filtered status.
   * @param pMaxResults The max. number of tasks that will be returned. If
   * argument <i>pTaskIds</i> is provided <i>pMaxResults</i> is ignored and the
   * size of <i>pTaskIds</i> is the max. number of results.
   * @param pFilter Only select tasks that are active (not in a finished or
   * failed state).
   *
   * @return The list of DataWorkflow tasks.
   */
  public static List<DataWorkflowTask> getDataWorkflowTasks(List<Long> pTaskIds, int pMaxResults, boolean pFilter) throws UnauthorizedAccessAttemptException {
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    List<DataWorkflowTask> result;
    try {
      mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
      List<DataWorkflowTask.TASK_STATUS> statusList = (pFilter) ? filteredStatusCodes : unfilteredStatusCodes;
      if (pTaskIds == null || pTaskIds.isEmpty()) {
        LOGGER.debug("Obtaining all DataWorkflow tasks.");
        result = mdm.findResultList("SELECT t FROM DataWorkflowTask t WHERE t.status IN :1 ORDER BY t.lastUpdate ASC", new Object[]{statusList}, DataWorkflowTask.class, 0, pMaxResults);
      } else {
        LOGGER.debug("Obtaining DataWorkflow tasks for ids {}.", pTaskIds);
        result = mdm.findResultList("SELECT t FROM DataWorkflowTask t WHERE t.id IN :1 AND t.status IN :2 ORDER BY t.lastUpdate ASC", new Object[]{pTaskIds, statusList}, DataWorkflowTask.class);
      }
    } finally {
      mdm.close();
    }
    return result;
  }

  /**
   * Check whether the provided ExecutionEnvironment is capable of executing
   * another task. Therefor, the database is queried for all DataWorkflow tasks
   * currently running on the provided ExecutionEnvironment. This number is
   * compared to the max. number of tasks configured for the
   * ExectutionEnvironment. If another task can be scheduled, TRUE is returned.
   * Otherwise, this method returns FALSE and the submission has to be
   * postponed.
   *
   * @param pConfiguration The configuration of the ExecutionEnvironment that
   * will be checked for running tasks.
   *
   * @return TRUE if the provided ExecutionEnvironment can take another task.
   *
   * @throws UnauthorizedAccessAttemptException The the query to the database
   * failed.
   */
  public static boolean canScheduleTask(ExecutionEnvironmentConfiguration pConfiguration) throws UnauthorizedAccessAttemptException {
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    long cnt = 0;
    try {
      mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
      LOGGER.debug("Obtaining all DataWorkflow tasks running within execution environment {}.", pConfiguration.getId());
      List<DataWorkflowTask.TASK_STATUS> processing = Arrays.asList(DataWorkflowTask.TASK_STATUS.PROCESSING);
      cnt = mdm.findSingleResult("SELECT COUNT(t) FROM DataWorkflowTask t WHERE t.status IN :1 AND t.executionEnvironment.id=" + pConfiguration.getId(), new Object[]{processing}, Long.class);
      LOGGER.debug("Found {} running task(s). Comparing with max. number of tasks supported by ExecutionEnvironment ({}).", cnt, pConfiguration.getMaxParallelTasks());
    } finally {
      mdm.close();
    }
    return pConfiguration.getMaxParallelTasks() > cnt;
  }
}
