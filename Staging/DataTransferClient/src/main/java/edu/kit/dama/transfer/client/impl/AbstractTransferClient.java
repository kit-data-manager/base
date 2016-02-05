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
package edu.kit.dama.transfer.client.impl;

import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.util.AdalapiSettings;
import edu.kit.dama.transfer.client.exceptions.PrepareTransferException;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.transfer.client.exceptions.TransferException;
import edu.kit.dama.transfer.client.interfaces.ITransferStatusListener;
import edu.kit.dama.transfer.client.interfaces.ITransferTaskListener;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.transfer.client.types.TransferTask;
import edu.kit.dama.transfer.client.util.CleanupManager;
import edu.kit.dama.transfer.client.util.TransferHelper;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.staging.entities.StagingFile;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.staging.util.StagingUtils;
import edu.kit.dama.util.DataManagerSettings;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract base implementation of the actual transfer client. This transfer
 * client handles the entire transfer including pre- and postprocessing,
 * checkpointing, monitoring and cleanup. It is configured by the BaseUserClient
 * and can be implemented for different transfer scenarios, e.g. Upload,
 * Download or internal transfers.
 *
 * The default usage is to transfer all files from one directory to another
 * directory. Currently, at least one of these directories must be local caused
 * by the missing support of third party transfers by the underlaying ADALAPI.
 *
 * Another scenario, the map-based transfer, is used for internal transfers. In
 * this case one or many source file(s) are transferred to a provided target
 * file, one target file for each source file. In this scenario it is expected,
 * that all directory structures were created before and that there is no
 * additional pre- or postprocessing necessary.
 *
 * @author jejkal
 */
public abstract class AbstractTransferClient extends Thread implements ITransferStatusListener, ITransferTaskListener {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransferClient.class);

  /**
   * Status enum for the transfer
   */
  public enum TRANSFER_STATUS {

    PENDING,
    RUNNING,
    PREPARING,
    TRANSFERRING,
    CLEANUP,
    SUCCEEDED,
    FAILED,
    CANCELED,
    TRANSFER_LOCKED,
    INTERNAL_PREPARATION_FAILED,
    EXTERNAL_PREPARATION_FAILED,
    PRE_PROCESSING_FAILED,
    TRANSFER_FAILED
  }
  /**
   * Hashmap which contains all source / target mappings. This map is intended
   * to be used for special transfer cases, where e.g. the source files do not
   * contain any structural information. In this case, the bit stream from file
   * KEY is just copied to file VALUE.
   */
  private TransferTaskContainer transferContainer = null;
  /**
   * A list of registered pre-transfer processors.
   */
  private final List<AbstractStagingProcessor> stagingProcessors = new ArrayList<>();
  /**
   * Flag which indicated, that the transfer is running.
   */
  private boolean transferRunning = true;
  /**
   * The delay until the actual transfer starts. This flag was only introduced.
   * for testing purposes
   */
  private long delay = 0;
  /**
   * Flag which indicates, that the transfer has been canceled by the user.
   */
  private boolean canceled = false;
  /**
   * The timer responsible for creating checkpoints for transfer resuming.
   */
  private Timer checkpointTimer = null;
  /**
   * The frequency of creating checkpoints.
   */
  private static final long CHECKPOINT_DELAY = DateUtils.MILLIS_PER_MINUTE;
  /**
   * The frequency of internal checks (used for Thread.sleep()).
   */
  private static final int CHECK_DELAY = 100;
  /**
   * The max. number of parallel transfers (Apart from this entry there is
   * another "limitation" by the Adalapi's AbstractProtocolFactory, where the
   * number of client instances is defined. However, normally a single protocol
   * client can be used by several (&gt;&gt;5) transfer tasks in parallel).
   */
  private static long MAX_PARALLEL_TRANSFERS = DataManagerSettings.getSingleton().getIntProperty(DataManagerSettings.STAGING_MAX_PARALLEL_TRANSFERS, 10);
  /**
   * The result of the last transfer.
   */
  private TRANSFER_STATUS status = TRANSFER_STATUS.PENDING;
  /**
   * The list of transfer tasks.
   */
  private final List<TransferTask> transferTasks = new ArrayList<>();
  /**
   * List of transfer status listeners.
   */
  private List<ITransferStatusListener> transferStatusListeners = null;
  /**
   * List of external transfer task listeners to allow monitoring and
   * visalization.
   */
  private List<ITransferTaskListener> transferTaskListeners = null;
  /**
   * The list of running tasks
   */
  private final List<TransferTask> runningTasks = new ArrayList<>();
  /**
   * The list of successfully finished tasks.
   */
  private final List<TransferTask> finishedTasks = new ArrayList<>();
  /**
   * The list of failed tasks.
   */
  private final List<TransferTask> failedTasks = new ArrayList<>();
  /**
   * The file appender for transfer logging.
   */
  private AbstractFile destination = null;

  /**
   * Default constructor to create a transfer client for downloads. In this
   * case, the destination is set during construction.
   *
   * @param pContainer The transfer task container.
   * @param pDestination The destination folder in case of a download. If
   * pContainer defines an ingest container, an IllegalArgumentException will be
   * thrown.
   */
  public AbstractTransferClient(TransferTaskContainer pContainer, AbstractFile pDestination) {
    transferContainer = pContainer;
    pContainer.setDestination(pDestination.getUrl());
    transferStatusListeners = new LinkedList<>();
    transferTaskListeners = new LinkedList<>();
    addTransferStatusListener(AbstractTransferClient.this);
    setDaemon(true);
    //add new shutdown hook for this transfer
    Runtime.getRuntime().addShutdownHook(new TransferShutdownHook(this));
  }

  /**
   * Default constructor to create a transfer client for ingests. In this case,
   * the destination is obtained by the transfer task container.
   *
   * @param pContainer The transfer task container.
   */
  public AbstractTransferClient(TransferTaskContainer pContainer) {
    transferContainer = pContainer;
    transferStatusListeners = new LinkedList<>();
    transferTaskListeners = new LinkedList<>();
    addTransferStatusListener(AbstractTransferClient.this);
    setDaemon(true);
    //add new shutdown hook for this transfer
    Runtime.getRuntime().addShutdownHook(new TransferShutdownHook(this));
  }

  /**
   * Returns the transfer task container.
   *
   * @return The TransferTaskContainer.
   */
  public TransferTaskContainer getTransferTaskContainer() {
    return transferContainer;
  }

  /**
   * Set the transfer task container. This method is intended to be used only in
   * case a transfer is restored. Otherwisem, it is recommended to load the
   * container from the server using the REST interface.
   *
   * @param pContainer The TransferTaskContainer.
   */
  public void setTransferTaskContainer(TransferTaskContainer pContainer) {
    transferContainer = pContainer;
  }

  /**
   * Return the destination folder for this transfer.
   *
   * @return The target folder.
   */
  public final AbstractFile getDestination() {
    if (destination == null) {
      destination = new AbstractFile(getTransferTaskContainer().getDestination());
    }
    return destination;
  }

  /**
   * Add the provided transfer status listener.
   *
   * @param pListener The transfer status listener.
   */
  public final void addTransferStatusListener(ITransferStatusListener pListener) {
    if (!transferStatusListeners.contains(pListener)) {
      transferStatusListeners.add(pListener);
    }
  }

  /**
   * Remove the provided transfer status listener.
   *
   * @param pListener The transfer status listener.
   */
  public final void removeTransferStatusListener(ITransferStatusListener pListener) {
    transferStatusListeners.remove(pListener);
  }

  /**
   * Add the provided transfer task listener.
   *
   * @param pListener The transfer task listener.
   */
  public final void addTransferTaskListener(ITransferTaskListener pListener) {
    if (pListener != null && !transferTaskListeners.contains(pListener)) {
      transferTaskListeners.add(pListener);
    }
  }

  /**
   * Remove the provided transfer task listener.
   *
   * @param pListener The transfer task listener.
   */
  public final void removeTransferStatusListener(ITransferTaskListener pListener) {
    transferTaskListeners.remove(pListener);
  }

  /**
   * Add a staging processor.
   *
   * @param pProcessor The staging processor to add.
   */
  public final void addStagingProcessor(AbstractStagingProcessor pProcessor) {
    stagingProcessors.add(pProcessor);
  }

  /**
   * Remove a staging processor.
   *
   * @param pProcessor The staging processor to remove.
   */
  public final void removeStagingProcessor(AbstractStagingProcessor pProcessor) {
    stagingProcessors.remove(pProcessor);
  }

  /**
   * Return all registered staging processors performed before the actual
   * transfer.
   *
   * @return An array of staging processors.
   */
  public final AbstractStagingProcessor[] getStagingProcessors() {
    return stagingProcessors.toArray(new AbstractStagingProcessor[stagingProcessors.size()]);
  }

  /**
   * Perform staging processors. This may include preparation of the data which
   * has to be transfered. Available processors and their selection is done in
   * beforehand by the user via a user interface.
   *
   * @return TRUE if all processors were performed, FALSE on external
   * interruption (e.g. if the user aborted the excution).
   *
   * @throws StagingProcessorException If one processor has failed due to an
   * internal error.
   */
  public abstract boolean performStagingProcessors() throws StagingProcessorException;

  /**
   * Start the transfer delayed by pDelay milliseconds.
   *
   * @param pDelay the delay after which the transfer is started.
   */
  public final void startTransfer(long pDelay) {
    LOGGER.info("Starting transfer");
    delay = pDelay;
    start();
  }

  /**
   * Start the transfer.
   */
  public final void startTransfer() {
    startTransfer(0);
  }

  /**
   * Try to restore a transfer from the last checkpoint.
   *
   * @param pContext The REST credentials to access the staging service for
   * transfer validation.
   *
   * @return TRUE if the transfer could be restored.
   */
  public abstract boolean restoreTransfer(SimpleRESTContext pContext);

  /**
   * Prepare the transfer. This step may include additional preparation stuff
   * which is performed before the actual transfer starts. At this point, all
   * preprocessing is finished and the list of files to transfer is prepared.
   *
   * @return TRUE on success, FALSE on external interruption.
   *
   * @throws PrepareTransferException If there was an internal error that forced
   * the preparation to stop.
   */
  public boolean prepareTransfer() throws PrepareTransferException {
    //might be overwritten or not
    return true;
  }

  /**
   * This method is called by the ShutdownHook attached to each transfer client.
   * If the transfer client is terminated by the user via SIGINT (text-based
   * client) or by closing the main application window (graphical client), the
   * ShutdownHook will be executed and calls performShutdown, to allow the
   * implementation to react on the shutdown request, e.g., by cleaning up or by
   * storing the current transfer state to be able to continue at this point
   * later.
   */
  public abstract void performShutdown();

  /**
   * Initialized and perform the actual transfer. This method should return TRUE
   * if the transfer was finished successfully, FALSE if the transfer was
   * interrupted externally (e.g. by a cancel-request from a user) and it should
   * throw a TransferException if there was an internal error that forced the
   * transfer to stop.
   *
   * @return TRUE on success, FALSE on external interruption.
   */
  public final boolean initializeAndPerformTransfer() {
    LOGGER.info("Initializing transfer");
    initializeTransfer();
    LOGGER.info("Start to transfer {} file(s)", getTransferTasks().size());
    boolean transferSucceeded = transferFiles();

    //cleanup
    cancelCheckpointTimer();

    if (!transferSucceeded && !isCanceled()) {
      //throw exception to notify on error
      throw new TransferException("Transfer failed. See logging output for more details");
    }
    return transferSucceeded;
  }

  /**
   * Check whether this transfer is locked or not. Therefor we'll check if there
   * is a file '.lock' located in the transfer's temporary directory. Each
   * transfer is locked as part of the internal preparation. If the transfer has
   * finished.
   *
   * @return TRUE if the transfer is locked.
   *
   * @throws IOException If there were problems getting the temporary transfer
   * directory.
   */
  public final boolean isTransferLocked() throws IOException {
    return new File(StagingUtils.getTempDir(transferContainer) + File.separator + ".lock").exists();
  }

  /**
   * Create the temporary transfer directory and a .lock file to avoid multiple
   * transfers for the same DOID. Calling this method is part of the internal
   * transfer preparation.
   *
   * @return TRUE if the directory could be created and locked.
   */
  private boolean createAndLockTransfer() {
    boolean result = false;
    try {
      String identifier = transferContainer.getUniqueTransferIdentifier();
      LOGGER.debug("Try to create temp transfer directory for transfer ID {}", identifier);
      String tempDir = StagingUtils.getTempDir(transferContainer);
      LOGGER.debug(" - Temp transfer directory: {}", tempDir);
      FileUtils.touch(new File(tempDir + File.separator + ".lock"));
      LOGGER.debug("Created and locked temp transfer directory");
      result = true;
    } catch (IOException ioe) {
      LOGGER.error("Failed to lock transfer directory", ioe);
    }
    return result;
  }

  /**
   * Unlock this transfer by removing the .lock file from the transfer's
   * temporary directory. This method is used only if the transfer has failed or
   * if it was canceled by the user to allow resuming the transfer.
   *
   * @return TRUE if the transfer was unlocked successfully.
   */
  private boolean unlockTransfer() {
    boolean result = false;
    try {
      result = FileUtils.deleteQuietly(new File(StagingUtils.getTempDir(transferContainer) + File.separator + ".lock"));
    } catch (IOException ioe) {
      LOGGER.warn("Failed to unlock transfer directory", ioe);
    }
    return result;
  }

  /**
   * Reset the transfer by removing the temporary directory.
   */
  public final void resetTransfer() {
    try {
      if (!isTransferRunning()) {
        File tmpDir = new File(StagingUtils.getTempDir(transferContainer));
        if (tmpDir.exists()) {
          FileUtils.deleteDirectory(tmpDir);
        }
      } else {
        LOGGER.warn("Transfer is already running. Reset not supported.");
      }
    } catch (IOException ioe) {
      LOGGER.warn("Failed to reset transfer", ioe);
    }
  }

  /**
   * Add a new transfer task.
   *
   * @param pTask The new transfer task.
   */
  public final void addTransferTask(TransferTask pTask) {
    transferTasks.add(pTask);
  }

  /**
   * Check the transfer temp directory. After this method call, the temp
   * directory should exist and be locked. If the temp directory exists and is
   * locked, the transfer is aborted with status TRANSFER_LOCKED.
   *
   * @throws IOException If the temp directory could not be obtained, created or
   * locked.
   */
  private void checkTempDir() throws IOException {
    String transferTempDir = StagingUtils.getTempDir(transferContainer);
    LOGGER.debug("Checking temporary transfer directory '{}'", transferTempDir);
    File tempDir = new File(transferTempDir);
    boolean exists = false;
    if (tempDir.exists()) {
      //check for running transfer
      LOGGER.debug("Temporary transfer directory already exists, checking lock state.");
      if (isTransferLocked()) {
        //transfer running or crashed
        setStatus(TRANSFER_STATUS.TRANSFER_LOCKED);
        throw new IOException("Temporary transfer directory seems to be locked. If you are sure, that there is no transfer running, please remove the file '" + transferTempDir + File.separator + ".lock' manually.");
      } else {
        exists = true;
      }
    }

    //no temp dir found or it is not locked, create and lock
    LOGGER.debug("{} temporary transfer directory", ((exists) ? "Locking" : "Creating and locking"));
    if (!createAndLockTransfer()) {
      if (exists) {
        throw new IOException("Failed to lock temporary transfer directory");
      } else {
        throw new IOException("Failed to create and lock temporary transfer directory");
      }
    }
  }

  /**
   * Returns the current list of transfer tasks. This list may change over time,
   * as local pre-processing can combine, add or remove entries.
   *
   * @return The list of transfer tasks.
   */
  public final List<TransferTask> getTransferTasks() {
    return transferTasks;
  }

  /**
   * Shutdown this transfer. This method is called only by the ShutdownHook
   * registered during construction. It takes care, that the transfer is
   * unlocked and that external shutdown operations are performed. Therefor
   * performShutdown() is called to allow implementations of this abstract class
   * to perform custom shutdown steps. The ShutdownHook is only executed when
   * terminating the transfer client normally. For graphical applications this
   * would be if the main windows has closed, for command line applications
   * interrupting the execution via CTRL+C will work. Internally System.exit()
   * would terminate the current VM and execute all shutdown hooks.
   * Nevertheless, stopTransfer() should be used by default to allow a proper
   * cleanup.
   */
  public final void shutdown() {
    if (unlockTransfer()) {
      LOGGER.debug("Transfer unlocked successfully");
    }
    try {
      performShutdown();
    } catch (Exception e) {
      LOGGER.debug("performShutdown() threw an exception. We'll ignore this.");
    }
  }

  /**
   * Check whether this transfer is running or not.
   *
   * @return TRUE if the transfer is still running.
   */
  public final boolean isTransferRunning() {
    return transferRunning;
  }

  /**
   * Returns the delay until which the actual transfer should start. (Testing
   * only).
   *
   * @return long The delay in ms.
   */
  public final long getTransferDelay() {
    return delay;
  }

  /**
   * Returns the current transfer status. During transfer, the status should be
   * TRANSFER_STATUS.RUNNING
   *
   * If the transfer has finished, the status is either
   * TRANSFER_STATUS.SUCCEEDED or or it represents the phase, where the transfer
   * has failed. If TRANSFER_STATUS.FAILED is returned, some unknown error has
   * occured.
   *
   * @return The current status of the transfer.
   */
  public final TRANSFER_STATUS getStatus() {
    return status;
  }

  /**
   * Execute the preparation phase. In this phase the internal prepareation is
   * performed (create temporary directory, lock transfer) and all pre-process
   * operations are executed.
   *
   * @return TRUE if this phase was finished successfully, FALSE if there was an
   * error or the use canceled the transfer.
   */
  public final boolean prepare() {
    setStatus(TRANSFER_STATUS.PREPARING);
    boolean result = true;
    LOGGER.info(" * Preparing transfer");
    try {
      LOGGER.debug(" * Checking temp directory");
      checkTempDir();
    } catch (IOException ex) {
      //preparation failed, return
      LOGGER.warn("prepareTransferInternal() returned 'false'");
      setStatus(TRANSFER_STATUS.INTERNAL_PREPARATION_FAILED);
      result = false;
    }

    //perform staging processors
    if (result) {
      LOGGER.info(" * Performing staging processors");
      if (getTransferTaskContainer().isClosed()) {
        LOGGER.info("Transfer container is closed. Preprocessing is either finished or not necessary.");
      } else {
        if (!performStagingProcessorsInternal()) {
          LOGGER.error("Failed to perform staging processors");
          setStatus(TRANSFER_STATUS.PRE_PROCESSING_FAILED);
          result = false;
        } else {
          LOGGER.info(" * Staging processors successfully completed. Closing transfer container.");
          //close container as all files are in now
          getTransferTaskContainer().close();
        }
      }
    }

    //create transfer tasks
    if (result) {
      Map<StagingFile, StagingFile> openTransfers = new HashMap<StagingFile, StagingFile>();
      try {
        LOGGER.info(" * Checking and restoring tree structure at destination {}", destination);
        DataOrganizationUtils.restoreTreeStructure(getTransferTaskContainer().getFileTree(), getDestination(), openTransfers);
      } catch (MalformedURLException ex) {
        LOGGER.error("Failed to prepare transfer. File tree could not be restored.", ex);
        result = false;
      } catch (IOException ex) {
        LOGGER.error("Failed to prepare transfer. File tree could not be restored.", ex);
        result = false;
      }
      LOGGER.info(" * Setting up transfer tasks");
      Set<Map.Entry<StagingFile, StagingFile>> entries = openTransfers.entrySet();
      for (Map.Entry<StagingFile, StagingFile> entry : entries) {
        LOGGER.debug("Adding transfer task from {} to {}", new Object[]{entry.getKey().getAbstractFile(), entry.getValue().getAbstractFile()});
        addTransferTask(new TransferTask(entry.getKey().getAbstractFile(), entry.getValue().getAbstractFile()));
      }
    }

    //external preparation...this should always take place, as this call is responsible for creating/checking (remote-) directories and doing any custom preparation
    if (result) {
      try {
        LOGGER.debug("Performing external preparation");
        if (!prepareTransfer()) {
          LOGGER.error("External preparation returned 'false'");
          setStatus(TRANSFER_STATUS.EXTERNAL_PREPARATION_FAILED);
          result = false;
        }
      } catch (PrepareTransferException pte) {
        LOGGER.error("External preparation threw an exception", pte);
        setStatus(TRANSFER_STATUS.EXTERNAL_PREPARATION_FAILED);
        result = false;
      }

      if (isCanceled()) {
        //stop was requested
        LOGGER.debug("Transfer was canceled by the user. All following steps will be skipped.");
        setStatus(TRANSFER_STATUS.CANCELED);
        result = false;
      }
    }
    return result;
  }

  /**
   * Perform staging processors as part of the preparation phase.
   *
   * @return TRUE if all staging processors have succeeded.
   */
  private boolean performStagingProcessorsInternal() {
    boolean result = false;
    if (!isCanceled()) {
      //stop was not requested, start preparation
      try {
        LOGGER.debug("Performing staging processors");
        if (performStagingProcessors()) {
          //preparation has succeeded
          LOGGER.debug("Staging processors successfully finished");
          result = true;
        } else {
          //preparation returned false...probably the user canceled the transfer
          LOGGER.warn("performStagingProcessors() returned 'false'. Either the transfer was canceled or the provided Protocol/URL is invalid.");
          setStatus(TRANSFER_STATUS.PRE_PROCESSING_FAILED);
        }
      } catch (StagingProcessorException tcope) {
        LOGGER.error("Pre-Transfer threw an exception, aborting transfer", tcope);
        setStatus(TRANSFER_STATUS.PRE_PROCESSING_FAILED);
      }
    } else {
      setStatus(TRANSFER_STATUS.CANCELED);
    }
    return result;
  }

  /**
   * Initialize the transfer by creating a checkpoint task and obtaining the
   * list of source files to transfer from the transferMap built up during
   * preparation.
   */
  public final void initializeTransfer() {
    //prepare resume capabilities
    LOGGER.info("Starting checkpoint monitor");
    setupCheckpointTask();

    if (getTransferDelay() != 0) {
      LOGGER.debug(" * Delaying transfer by {} ms", getTransferDelay());
      try {
        Thread.sleep(getTransferDelay());
      } catch (InterruptedException ie) {
      }
    }
  }

  /**
   * Execute the actual transfer phase. In this phase the entire data transfer
   * takes place.
   *
   * @return TRUE if this phase was finished successfully, FALSE if there was an
   * error or the use canceled the transfer.
   */
  public final boolean transfer() {
    setStatus(TRANSFER_STATUS.TRANSFERRING);
    boolean result = false;
    LOGGER.info(" * Performing transfer");

    if (!isCanceled()) {
      try {
        if (initializeAndPerformTransfer()) {//initialization and transfer, both have finished
          LOGGER.debug("Transfer successfully finished");
          result = true;
        } else {//something failed during initialization or transfer
          LOGGER.error("initializeAndPerformTransfer() returned 'false'");
          setStatus(TRANSFER_STATUS.TRANSFER_FAILED);
        }
      } catch (TransferException te) {
        LOGGER.error("performTransfer() threw an exception, aborting transfer", te);
        setStatus(TRANSFER_STATUS.TRANSFER_FAILED);
      }
    } else {
      LOGGER.info("Transfer was canceled by the user. All following steps will be skipped.");
      setStatus(TRANSFER_STATUS.CANCELED);
    }
    return result;
  }

  /**
   * Performs all transfer tasks defined for this client. If the transfer fails,
   * there are MAX_TRIES retries. After this amount, FALSE is returned, the last
   * file is put back to the transfer map and no more files will be transfered.
   *
   * @return TRUE if the transfer was finished successfully.
   */
  private boolean transferFiles() {
    createCheckpoint();
    runningTasks.clear();
    finishedTasks.clear();
    failedTasks.clear();
    int maxProtocolnstances = AdalapiSettings.getSingleton().getMaxProtocolInstances();
    LOGGER.debug("Staging transfer of files using {} ADALAPI protocol instances by {} parallel tasks.", maxProtocolnstances, MAX_PARALLEL_TRANSFERS);
    long nextAliveAt = System.currentTimeMillis() + DateUtils.MILLIS_PER_MINUTE * 10;
    for (TransferTask transferTask : getTransferTasks().toArray(new TransferTask[getTransferTasks().size()])) {
      LOGGER.debug("Try to schedule transfer task {}", transferTask);
      while (getRunningTaskCount() >= MAX_PARALLEL_TRANSFERS) {
        try {
          Thread.sleep(CHECK_DELAY);
        } catch (InterruptedException ie) {
        }
        if (isCanceled()) {
          LOGGER.debug("Transfer was canceled. Aborting!");
          break;
        }
        if (System.currentTimeMillis() >= nextAliveAt) {
          fireTransferAliveEvents();
          nextAliveAt = System.currentTimeMillis() + DateUtils.MILLIS_PER_MINUTE * 10;
        }
      }
      if (!isCanceled()) {
        LOGGER.info("Starting new transfer task");
        transferTask.addTransferTaskListener(this);
        runTask(transferTask);
      }
    }

    //wait until all running tasks have finished
    while (getRunningTaskCount() != 0) {
      try {
        Thread.sleep(CHECK_DELAY);
      } catch (InterruptedException ie) {
      }
      if (System.currentTimeMillis() >= nextAliveAt) {
        fireTransferAliveEvents();
        nextAliveAt = System.currentTimeMillis() + DateUtils.MILLIS_PER_MINUTE * 10;
      }
    }

    if (!isCanceled() && (finishedTasks.size() + failedTasks.size() == getTransferTasks().size())) {
      LOGGER.debug("All files were transferred successfully");
    }

    return failedTasks.isEmpty();
  }

  /**
   * Executed a transfer tasks and adds it to the list of running tasks.
   *
   * @param pTask The transfer task to start.
   */
  private synchronized void runTask(TransferTask pTask) {
    synchronized (this) {
      runningTasks.add(pTask);
      pTask.start();
    }
  }

  @Override
  public final void run() {
    setStatus(TRANSFER_STATUS.RUNNING);
    transferRunning = true;
    boolean success = false;
    try {
      if (prepare() && transfer() && !isCanceled()) {
        //everything has succeeded
        LOGGER.info(" * Transfer successfully finished");
        success = true;
      }
    } catch (Exception e) {
      LOGGER.error("Handling uncaught exception thrown during transfer", e);
    }

    if (success) {
      LOGGER.debug("Performing cleanup due to successful transfer");
      cleanup();
      setStatus(TRANSFER_STATUS.SUCCEEDED);
    } else {
      LOGGER.debug("Unlocking transfer due to failure to allow restart");
      if (unlockTransfer()) {
        LOGGER.debug("Transfer unlocked successfully");
      }
      setStatus(TRANSFER_STATUS.FAILED);
    }
    transferRunning = false;
  }

  /**
   * Perform cleanup operations in case of a successful transfer. During cleanup
   * the temporary transfer directory will be removed. If the removal fails, the
   * deletion will be sheduled to be performed during exit.
   */
  private void cleanup() {
    setStatus(TRANSFER_STATUS.CLEANUP);
    String tempDir = null;
    LOGGER.info(" * Performing cleanup");
    CleanupManager.getSingleton().performCleanup(transferContainer.getUniqueTransferIdentifier());
    //wait a while as sometimes FileUtils.deleteDirectory() finds files which were deleted by the CleanupManager
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {
    }
    //try to remove the tmp directory
    try {
      tempDir = StagingUtils.getTempDir(transferContainer);
      LOGGER.debug("Try to remove temporary transfer directory '{}'", tempDir);
      FileUtils.deleteDirectory(new File(tempDir));
    } catch (IOException ioe) {
      if (tempDir == null) {
        LOGGER.warn("Cleanup failed. Could not obtain temporary transfer directory", ioe);
      } else {
        LOGGER.warn("Cleanup failed. Trying to schedule DeleteOnExit", ioe);
        try {
          FileUtils.forceDeleteOnExit(new File(tempDir));
        } catch (IOException ioe2) {
          LOGGER.warn("Failed to shedule DeleteOnExit. Please remove the temporary directory manually.", ioe2);
        }
      }
    }
  }

  /**
   * Sets up the checkpoint task, which happens at prepareTransfer().
   */
  public final void setupCheckpointTask() {
    if (getTransferTasks().size() > 1) {
      String identifier = transferContainer.getUniqueTransferIdentifier();
      LOGGER.debug("Create and start checkpoint timer 'CheckpointTimer_{}'", identifier);
      checkpointTimer = new Timer("CheckpointTimer_" + identifier);
      checkpointTimer.schedule(new CheckpointTask(this), CHECKPOINT_DELAY, CHECKPOINT_DELAY);
    } else {
      LOGGER.debug("Checkpoint capabilities not available for single files");
    }
  }

  /**
   * Cancel the checkpoint timer task.
   */
  public final void cancelCheckpointTimer() {
    if (checkpointTimer != null) {
      checkpointTimer.cancel();
    }
  }

  /**
   * Create a new checkpoint. This method is called frequently by a
   * CheckpoinTask.
   */
  public final synchronized void createCheckpoint() {
    boolean result = TransferHelper.createCheckpoint(transferContainer);
    LOGGER.debug("Checkpoint {}", (result) ? "created" : "not created");
  }

  /**
   * Try to set the current status. This method only changes the status, if the
   * current status is TRANSFER_STATUS.RUNNING. If the status can be changed, it
   * is checked, whether the transfer was canceled or not. If it was canceled,
   * the status is set to TRANSFER_STATUS.CANCELED, otherwise it is set to
   * pStatus.
   *
   * @param pStatus The new status
   */
  public final void setStatus(TRANSFER_STATUS pStatus) {
    TRANSFER_STATUS old = status;

    boolean preparePhase = status.equals(TRANSFER_STATUS.PENDING) || status.equals(TRANSFER_STATUS.RUNNING) || status.equals(TRANSFER_STATUS.PREPARING);

    boolean performPhase = status.equals(TRANSFER_STATUS.TRANSFERRING) || status.equals(TRANSFER_STATUS.CLEANUP);
    if (preparePhase || performPhase) {
      if (isCanceled()) {
        LOGGER.debug("Transfer was canceled. Ignoring new status {} and setting status to CANCELED", pStatus);
        status = TRANSFER_STATUS.CANCELED;
      } else {
        LOGGER.debug("Setting new status to {}", pStatus);
        status = pStatus;
      }
    } else {
      LOGGER.debug("Try to set status to {}, but status is already {}. Ignoring status change.", new Object[]{pStatus, status});
    }
    //notify all transfer status listeners
    fireTransferStatusEvents(old, status);
  }

  /**
   * Check if the transfer was canceled or not.
   *
   * @return TRUE = the transfer was canceled by the user.
   */
  public final boolean isCanceled() {
    return canceled;
  }

  /**
   * Cancel the transfer.
   *
   * @param pValue True = cancel the transfer.
   */
  public final void setCanceled(boolean pValue) {
    canceled = pValue;
  }

  /**
   * Notify all transfer status listeners on a status change.
   *
   * @param pOld The old status.
   * @param pNew The new/current status.
   */
  public final void fireTransferStatusEvents(TRANSFER_STATUS pOld, TRANSFER_STATUS pNew) {
    for (ITransferStatusListener listener : transferStatusListeners.toArray(new ITransferStatusListener[transferStatusListeners.size()])) {
      listener.fireStatusChangedEvent(pOld, pNew);
    }
  }

  /**
   * Notify all transfer status listeners on alive transfer.
   */
  public final void fireTransferAliveEvents() {
    for (ITransferStatusListener listener : transferStatusListeners.toArray(new ITransferStatusListener[transferStatusListeners.size()])) {
      listener.fireTransferAliveEvent();
    }
  }

  /**
   * Returns the number of running transfer tasks.
   *
   * @return The number of running tasks.
   */
  private int getRunningTaskCount() {
    int result;
    synchronized (this) {
      result = runningTasks.size();
    }
    return result;
  }

  /**
   * Returns information about this transfer.
   *
   * @return The TransferInfo of this transfer.
   */
  public final TransferInfo getTransferInfo() {
    TransferInfo info;
    synchronized (this) {
      info = new TransferInfo(getTransferTasks().size(), runningTasks.size(), finishedTasks.size(), getStatus());
    }
    return info;
  }

  @Override
  public final synchronized void transferStarted(TransferTask pTask) {
    LOGGER.debug("Transfer task {} has started", pTask);
    notifyTransferStarted(pTask);
  }

  @Override
  public final synchronized void transferFinished(TransferTask pTask) {
    LOGGER.debug("Transfer task {} has successfully finished", pTask);
    synchronized (this) {
      runningTasks.remove(pTask);
      finishedTasks.add(pTask);
    }
    pTask.removeTransferTaskListener(this);
    notifyTransferFinished(pTask);
  }

  @Override
  public final synchronized void transferFailed(TransferTask pTask) {
    LOGGER.error("Transfer task {} has failed", pTask);
    synchronized (this) {
      runningTasks.remove(pTask);
      failedTasks.add(pTask);
    }
    pTask.removeTransferTaskListener(this);
    notifyTransferFailed(pTask);
  }

  /**
   * Notifies all transfer task listeners that TransferTask pTask has started.
   *
   * @param pTask The transfer task.
   */
  private void notifyTransferStarted(TransferTask pTask) {
    for (ITransferTaskListener listener : transferTaskListeners.toArray(new ITransferTaskListener[transferTaskListeners.size()])) {
      listener.transferStarted(pTask);
    }
  }

  /**
   * Notifies all transfer task listeners that TransferTask pTask has finished.
   *
   * @param pTask The transfer task.
   */
  private void notifyTransferFinished(TransferTask pTask) {
    //mark the node for URL as transferred
    transferContainer.markFileTransferred(pTask.getSourceFile().getUrl(), pTask.getTargetFile().getUrl());
    //notify all registered listener
    for (ITransferTaskListener listener : transferTaskListeners.toArray(new ITransferTaskListener[transferTaskListeners.size()])) {
      listener.transferFinished(pTask);
    }
  }

  /**
   * Notifies all transfer task listeners that TransferTask pTask has failed.
   *
   * @param pTask The transfer task.
   */
  private void notifyTransferFailed(TransferTask pTask) {
    for (ITransferTaskListener listener : transferTaskListeners.toArray(new ITransferTaskListener[transferTaskListeners.size()])) {
      listener.transferFailed(pTask);
    }
  }

  /**
   * ShutdownHook implementation calling performShutdownInternal() if a shutdown
   * of the current VM was detected.
   */
  public static class TransferShutdownHook extends Thread {

    private AbstractTransferClient client = null;

    /**
     * Default constructor.
     *
     * @param pClient The client to monitor.
     */
    public TransferShutdownHook(AbstractTransferClient pClient) {
      client = pClient;
      setDaemon(true);
    }

    @Override
    public final void run() {
      if (client != null) {
        client.shutdown();
      }
    }
  }

  /**
   * TransferInfo implementation to allow to monitor the transfer without
   * accessing each and every property separately.
   */
  public static class TransferInfo {

    /**
     * The overall number of transfer tasks.
     */
    private int taskCount = 0;
    /**
     * The number of running tasks.
     */
    private int runningTaskCount = 0;
    /**
     * The number of finished tasks.
     */
    private int finishedTaskCount = 0;
    /**
     * The current status of the transfer.
     */
    private TRANSFER_STATUS currentStatus = null;

    /**
     * Default constructor.
     *
     * @param pTaskCount The number of tasks.
     * @param pRunningTaskCount The number of running tasks.
     * @param pFinishedTaskCount The number of finished tasks.
     * @param pStatus the current transfer status.
     */
    public TransferInfo(int pTaskCount, int pRunningTaskCount, int pFinishedTaskCount, TRANSFER_STATUS pStatus) {
      taskCount = pTaskCount;
      runningTaskCount = pRunningTaskCount;
      finishedTaskCount = pFinishedTaskCount;
      currentStatus = pStatus;
    }

    /**
     * Get the task count.
     *
     * @return The task count.
     */
    public final int getTaskCount() {
      return taskCount;
    }

    /**
     * Get the running task count.
     *
     * @return The running task count.
     */
    public final int getRunningTaskCount() {
      return runningTaskCount;
    }

    /**
     * Get the finished task count.
     *
     * @return The finished task count.
     */
    public final int getFinishedTaskCount() {
      return finishedTaskCount;
    }

    /**
     * Get the current status.
     *
     * @return The current status.
     */
    public final TRANSFER_STATUS getCurrentStatus() {
      return currentStatus;
    }
  }
}
