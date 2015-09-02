/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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

import edu.kit.dama.rest.staging.client.impl.StagingServiceRESTClient;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.transfer.client.exceptions.CommandLineHelpOnlyException;
import edu.kit.dama.transfer.client.exceptions.TransferClientInstatiationException;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.CLEANUP;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.EXTERNAL_PREPARATION_FAILED;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.FAILED;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.INTERNAL_PREPARATION_FAILED;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.PREPARING;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.PRE_PROCESSING_FAILED;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.RUNNING;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.SUCCEEDED;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.TRANSFERRING;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.TRANSFER_FAILED;
import static edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS.TRANSFER_LOCKED;
import edu.kit.dama.transfer.client.interfaces.ITransferStatusListener;
import edu.kit.dama.transfer.client.interfaces.ITransferTaskListener;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.transfer.client.util.TransferHelper;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.staging.exceptions.ContainerInitializationException;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.staging.types.DownloadInformationWrapper;
import edu.kit.dama.rest.staging.types.IngestInformationWrapper;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic user client which is integrated into user interfaces. The
 * BaseUserClient takes care of the configuration of an appropriate
 * AbstractTransferClient. Furthermore, it wraps event listening and
 * communication with the Staging Service via a REST interface. The actual data
 * transfer is performed by the AbstractTransferClient in a multi threaded way.
 *
 * @author jejkal
 */
public class BaseUserClient {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(BaseUserClient.class);
  /**
   * Command line switch for the transfer ID.
   */
  public static final String TRANSFER_ID = "i";
  /**
   * Command line switch for the local Url (source for ingest, target for
   * download).
   */
  public static final String LOCAL_URL = "l";
  /**
   * Command line switch for defining the staging service URL.
   */
  private static final String SERVICE_URL = "u";
  /**
   * Command line switch for defining the access key for the staging service.
   */
  private static final String ACCESS_KEY = "k";
  /**
   * Command line switch for defining the access secret for the staging service.
   */
  private static final String ACCESS_SECRET = "s";
  /**
   * The command line switch for setting the 'force transfer' flag.
   */
  private static final String FORCE = "f";
  /**
   * The command line switch for showing command line help.
   */
  private static final String HELP = "h";

  /**
   * The command line switch for the local source/destination of the transfer.
   */
  public static final String LOCAL = "l";
  /**
   * The command line options object.
   */
  private static final Options OPTIONS = new Options();
  /**
   * The unique transfer ID provided by the caller.
   */
  private Long transferId = null;
  /**
   * Flag which indicates to force the transfer (e.g. if the transfer is
   * locked).
   */
  private boolean force = false;
  /**
   * The local URL of the transfer (source for ingest, target for download).
   */
  private String localUrl = null;
  /**
   * The service Url to the remove staging service.
   */
  private String serviceUrl = null;
  /**
   * The access key for accessing the staging service.
   */
  private String serviceAccessKey = null;
  /**
   * The access secret for accessing the staging service.
   */
  private String serviceAccessSecret = null;
  /**
   * Client for REST access.
   */
  private StagingServiceRESTClient stagingServiceRESTClient = null;
  //private TransferTaskContainer container = null;
  private CommandLine processedCommandLineArgs = null;

  static {
    OPTIONS.addOption(TRANSFER_ID, "transferId", true, "The ID of the transfer.");
    OPTIONS.addOption(SERVICE_URL, "serviceUrl", true, "The staging service URL.");
    OPTIONS.addOption(ACCESS_KEY, "accessKey", true, "The user key for accessing the staging service.");
    OPTIONS.addOption(ACCESS_SECRET, "accessSecret", true, "The user secret for accessing the staging service.");
    OPTIONS.addOption(FORCE, "force", false, "Force the transfer.");
    OPTIONS.addOption(HELP, "help", false, "Print command line options.");
  }
  /**
   * The transfer client instance responsible for all file transfer operations
   */
  private AbstractTransferClient transferClient = null;
  /**
   * Registered transfer status listeners
   */
  private final List<ITransferStatusListener> statusListeners = new LinkedList<ITransferStatusListener>();
  /**
   * Registered task listeners
   */
  private final List<ITransferTaskListener> taskListeners = new LinkedList<ITransferTaskListener>();

  /**
   * Print the command line help.
   */
  private void printParameterHelp() {
    new HelpFormatter().printHelp(getClass().getCanonicalName(), OPTIONS, true);
  }

  /**
   * Get the defined command line options. Normally, default options are
   * returned. If addOption() was called, the returned object also contains all
   * added options.
   *
   * @return The Options object.
   */
  public static Options getOptions() {
    return OPTIONS;
  }

  /**
   * Add the provided transfer status listener.
   *
   * @param pListener The status listener to add.
   */
  public final void addTransferStatusListener(ITransferStatusListener pListener) {
    if (!statusListeners.contains(pListener)) {
      statusListeners.add(pListener);
    }
  }

  /**
   * Remove the provided transfer status listener.
   *
   * @param pListener The status listener to remove.
   */
  public final void removeTransferStatusListener(ITransferStatusListener pListener) {
    statusListeners.remove(pListener);
  }

  /**
   * Add the provided transfer task listener.
   *
   * @param pListener The task listener to add.
   */
  public final void addTransferTaskListener(ITransferTaskListener pListener) {
    if (pListener != null && !taskListeners.contains(pListener)) {
      taskListeners.add(pListener);
    }
  }

  /**
   * Remove the provided transfer task listener.
   *
   * @param pListener The task listener to remove.
   */
  public final void removeTransferTaskListener(ITransferTaskListener pListener) {
    taskListeners.remove(pListener);
  }

  /**
   * Set the service access key.
   *
   * @param pServiceAccessKey The access key.
   */
  public final void setServiceAccessKey(String pServiceAccessKey) {
    this.serviceAccessKey = pServiceAccessKey;
  }

  /**
   * Set the service access secret.
   *
   * @param pServiceAccessSecret The access secret.
   */
  public final void setServiceAccessSecret(String pServiceAccessSecret) {
    this.serviceAccessSecret = pServiceAccessSecret;
  }

  /**
   * Get the context for accessing the staging REST service. The context
   * contains accessKey and accessSecret set via command line or the according
   * method.
   *
   * @return The REST context.
   */
  public SimpleRESTContext getContext() {
    if (serviceAccessKey == null || serviceAccessSecret == null) {
      throw new IllegalArgumentException("Either serviceAccessKey or serviceAccessSecret are not set.");
    }

    return new SimpleRESTContext(serviceAccessKey, serviceAccessSecret);
  }

  /**
   * Returns the REST client used to communicate with the Staging Service.
   *
   * @return The REST client.
   */
  public StagingServiceRESTClient getStagingServiceRESTClient() {
    if (stagingServiceRESTClient == null) {
      LOGGER.debug("Creating instance of REST access client");
      stagingServiceRESTClient = new StagingServiceRESTClient(getServiceURL(), getContext());
      LOGGER.debug("REST client instance successfully created, testing connectivity...");
      IngestInformationWrapper result = stagingServiceRESTClient.getIngestCount(getContext());
      LOGGER.debug("Received number of registered ingests ({}) as result.", result.getEntities().size());
    }
    return stagingServiceRESTClient;
  }

  /**
   * Set the local URL programmatically. This value overrides any value provided
   * by command line options.
   *
   * @param pLocalUrl The local URL.
   */
  public final void setLocalUrl(String pLocalUrl) {
    if (pLocalUrl == null) {
      throw new IllegalArgumentException("Argument pLocalUrl must not be null");
    }

    try {
      LOGGER.debug("Checking provided argument {} for local Url", pLocalUrl);
      URL local = new URL(pLocalUrl);
      localUrl = local.toString();
      LOGGER.debug("Local Url successfully set to {}", local.toString());
    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException("Argument pLocalUrl must be a valid URL", ex);
    }
  }

  /**
   * Returns the source URL of this transfer.
   *
   * @return The source URL.
   */
  public final String getLocalUrl() {
    return localUrl;
  }

  /**
   * Set the staging service URL programmatically. This value overrides any
   * value provided by command line options.
   *
   * @param pServiceUrl The callback URL.
   */
  public final void setServiceURL(String pServiceUrl) {
    serviceUrl = pServiceUrl;
  }

  /**
   * Returns the source URL of this transfer.
   *
   * @return The source URL.
   */
  public final String getServiceURL() {
    return serviceUrl;
  }

  /**
   * Returns the transfer id of this transfer.
   *
   * @return The transfer.
   */
  public final Long getTransferId() {
    return transferId;
  }

  /**
   * Set the force transfer flag programmatically. This value overrides any
   * value provided by command line options.
   *
   * @param pForce The force transfer flag.
   */
  public final void setForceTransfer(boolean pForce) {
    force = pForce;
  }

  /**
   * Adds a new option to the default command line options.
   *
   * @param pOptionName The option and the short option character.
   * @param pDescription The plain text description of the option.
   * @param pArgCount The argument count.
   * @param pLongOption The long option string.
   * @param pMandatory TRUE = This option MUST be provided.
   */
  public final void addOption(String pOptionName, String pDescription, int pArgCount, String pLongOption, boolean pMandatory) {
    if (pOptionName == null || pDescription == null) {
      throw new IllegalArgumentException("Neither pOptionName nor pDescription must be 'null'");
    }
    OptionBuilder b = OptionBuilder.withLongOpt(pLongOption).withDescription(pDescription).isRequired(pMandatory);
    if (pArgCount != 0) {
      if (pArgCount == 1) {
        b = b.hasArg();
      } else {
        b = b.hasArgs(pArgCount);
      }
    }
    OPTIONS.addOption(b.create(pOptionName));
  }

  /**
   * Get a additional option value for options added via addOption() before.
   * This method will return 'null' as long as configure(String[]) was not
   * called. Afterwards, it will return the value for the provided long option
   * or null, if the value was not found.
   *
   * @param pOpt The option name used for addOption() before.
   *
   * @return The value of the option or null.
   */
  public final String getProcessedOptionValue(String pOpt) {
    if (processedCommandLineArgs == null) {
      LOGGER.debug("Command line not processed, yet. Returning null.");
      return null;
    }
    return processedCommandLineArgs.getOptionValue(pOpt).trim();
  }

  /**
   * Returns the current transfer client or 'null', if
   * initializeTransferClient() has not been called. The transfer client takes
   * care of the actual transfer.
   *
   * @return The AbstractTransferClient object.
   */
  public final AbstractTransferClient getTransferClient() {
    return transferClient;
  }

  /**
   * Perform the upload by using the configured transfer client. Prior to the
   * upload, the following steps are performed:
   *
   * <ul>
   *
   * <li>Create an instance of an upload client. If there is no source URL set,
   * the upload is expected to be restored from a former checkpoint.</li>
   * <li>Reset the transfer if the 'force' flag is set and if a source URL is
   * provided</li>
   * <li>Add all registered transfer status listeners to the transfer
   * client.</li>
   * <li>Add all registered transfer task listeners to the transfer client.</li>
   * <li>Add all registered transfer OPs to the transfer client.</li>
   *
   * </ul>
   *
   * @throws TransferClientInstatiationException If the instantiation was not
   * performed yet and fails
   */
  public final void performUpload() throws TransferClientInstatiationException {
    /* IngestInformationWrapper result = getStagingServiceRESTClient().getIngestInformationById(getTransferId(), getContext());

     if (result.getEntities().isEmpty()) {
     throw new TransferClientInstatiationException("Failed to obtain ingest information for object '" + getTransferId() + "'. Service returned no result.");
     }
     IngestInformation entity = result.getEntities().get(0);*/

    TransferTaskContainer container;
    try {
      container = TransferTaskContainer.factoryIngestContainer(getTransferId(), getServiceURL(), serviceAccessKey, serviceAccessSecret);
      container.addDataFile(new File(new URL(getLocalUrl()).toURI()));
      transferClient = TransferHelper.factoryTransferClient(container);
    } catch (Exception ex) {
      throw new TransferClientInstatiationException("Failed to initialize transfer container", ex);
    }

    LOGGER.debug(" * Instance successfully created.");
    if (force) {
      LOGGER.info(" * Resetting transfer due to 'force' argument");
      transferClient.resetTransfer();
    } else {
      LOGGER.info(" * Checking for restorable transfer.");
      if (transferClient.restoreTransfer(getContext())) {
        LOGGER.info("Transfer successfully restored from disk.");
      }
    }

    LOGGER.debug(" * Adding {} transfer status listener(s)", taskListeners.size());
    for (ITransferStatusListener listener : statusListeners.toArray(new ITransferStatusListener[statusListeners.size()])) {
      transferClient.addTransferStatusListener(listener);
    }
    LOGGER.debug(" * Adding {} transfer task listener(s)", taskListeners.size());
    for (ITransferTaskListener listener : taskListeners.toArray(new ITransferTaskListener[statusListeners.size()])) {
      transferClient.addTransferTaskListener(listener);
    }

    IngestInformation entity = (IngestInformation) container.getTransferInformation();
    LOGGER.debug(" * Adding {} client-side staging processors", entity.getClientSideStagingProcessor().length);
    for (StagingProcessor processor : entity.getClientSideStagingProcessor()) {
      try {
        LOGGER.debug(" - Try to add processor {} ({})", new Object[]{processor.getName(), processor.getUniqueIdentifier()});
        transferClient.addStagingProcessor(processor.createInstance());
        LOGGER.debug(" - Processor successfully added");
      } catch (ConfigurationException ex) {
        throw new TransferClientInstatiationException("Failed to add staging processor '" + processor.getName() + "'", ex);
      }
    }

    LOGGER.debug("Transfer client ready. Starting transfer...");
    transferClient.start();
  }

  /**
   * Perform the download by using the configured transfer client. Prior to the
   * download, the following steps are performed:
   *
   * <ul>
   * <li>Create an instance of an download client. If there is no source URL
   * set, the download is expected to be restored from a former checkpoint.</li>
   * <li>Reset the transfer if the 'force' flag is set and if a source URL is
   * provided</li>
   * <li>Add all registered transfer status listeners to the transfer
   * client.</li>
   * <li>Add all registered transfer task listeners to the transfer client.</li>
   *
   * </ul>
   *
   * @throws TransferClientInstatiationException If the instantiation was not
   * performed yet and fails.
   */
  public final void performDownload() throws TransferClientInstatiationException {
    //create client
    LOGGER.debug("Creating download client instance");

    LOGGER.debug(" - Checking local target URL");
    try {
      if (!new AbstractFile(new URL(getLocalUrl())).isLocal()) {
        throw new TransferClientInstatiationException("Target URL must be local but is '" + getLocalUrl() + "'");
      }
    } catch (MalformedURLException mue) {
      throw new TransferClientInstatiationException("Target URL '" + getLocalUrl() + "' is invalid", mue);
    }

    LOGGER.debug(" - Obtaining download information");
    //get download information for provided object id
    DownloadInformationWrapper result = getStagingServiceRESTClient().getDownloadById(getTransferId(), getContext());
    if (result.getEntities().isEmpty()) {
      throw new TransferClientInstatiationException("Failed to obtain download information for object '" + getTransferId() + "'. Service returned no result.");
    }
    DownloadInformation entity = result.getEntities().get(0);

    if (entity.getStagingUrl() == null) {
      throw new TransferClientInstatiationException("Download not prepared yet, please try again later.");
    }

    LOGGER.debug(" - Obtaining and intializing transfer container");
    TransferTaskContainer container = getStagingServiceRESTClient().getTransferTaskContainerById(getTransferId(), getContext());
    try {
      container.initialize(getContext().getAccessKey(), getContext().getAccessSecret());
    } catch (ContainerInitializationException ex) {
      throw new TransferClientInstatiationException("Failed to initialized transfer container", ex);
    }

    LOGGER.debug(" - Instantiating transfer client");
    try {
      transferClient = TransferHelper.factoryTransferClient(container, new AbstractFile(new URL(getLocalUrl())));
    } catch (MalformedURLException | TransferClientInstatiationException ex) {
      throw new TransferClientInstatiationException("Failed to initialize transfer container", ex);
    }

    LOGGER.debug(" * Instance successfully created.");
    if (force) {
      LOGGER.info(" * Resetting transfer due to 'force' argument");
      transferClient.resetTransfer();
    } else {
      LOGGER.info(" * Checking for restorable transfer.");
      if (transferClient.restoreTransfer(getContext())) {
        LOGGER.info("Transfer successfully restored from disk.");
      }
    }

    LOGGER.debug(" - Adding {} transfer status listener(s)", taskListeners.size());
    for (ITransferStatusListener listener : statusListeners.toArray(new ITransferStatusListener[statusListeners.size()])) {
      transferClient.addTransferStatusListener(listener);
    }
    LOGGER.debug(" - Adding {} transfer task listener(s)", taskListeners.size());
    for (ITransferTaskListener listener : taskListeners.toArray(new ITransferTaskListener[statusListeners.size()])) {
      transferClient.addTransferTaskListener(listener);
    }

    //@TODO Add StagingProcessors here, if needed
    LOGGER.debug("Transfer client ready. Starting transfer...");
    transferClient.start();
  }

  /**
   * Cancel the transfer.
   */
  public final void cancelTransfer() {
    if (transferClient != null) {
      transferClient.setCanceled(true);
    }
  }

  /**
   * Configures this client using the agument array obtained from a command line
   * call. All arguments are only set, if there are no values assigned to the
   * according fields. Each field but the transferID can be still overwritten
   * programmatically after configuration.
   *
   * @param pArgs Command line arguments obtained from the main method.
   *
   * @throws TransferClientInstatiationException If pArgs is invalid.
   * @throws CommandLineHelpOnlyException If printing the command line help was
   * requested via command line args.
   */
  public final void configure(String[] pArgs) throws TransferClientInstatiationException, CommandLineHelpOnlyException {
    LOGGER.debug("Disabling ADALAPI overwrite checks");
    AbstractFile.setOverwritePermission(AbstractFile.OVERWRITE_PERMISSION.ALLOWED);
    CommandLineParser parser = new PosixParser();
    if (pArgs != null) {
      LOGGER.debug("Configuring transfer client using argument array {}", Arrays.asList(pArgs));
    } else {
      LOGGER.warn("No argument array provided for configuration. Transfer won't be performed.");
    }

    processedCommandLineArgs = null;
    try {
      processedCommandLineArgs = parser.parse(getOptions(), pArgs);
    } catch (ParseException pe) {
      printParameterHelp();
      throw new TransferClientInstatiationException("Failed to parse arguments", pe);
    }
    if (processedCommandLineArgs.hasOption(HELP)) {//print help and exit
      printParameterHelp();
      throw new CommandLineHelpOnlyException();
    }

    //<editor-fold defaultstate="collapsed" desc=" Handle command line parsing and transfer client initialization ">
    String sTransferId = (transferId == null) ? processedCommandLineArgs.getOptionValue(TRANSFER_ID).trim() : Long.toString(transferId);
    try {
      transferId = Long.parseLong(sTransferId);
    } catch (NumberFormatException ex) {
      throw new TransferClientInstatiationException("Failed to parse transferId argument " + sTransferId + ". Long value expected.", ex);
    }

    serviceUrl = (serviceUrl == null) ? processedCommandLineArgs.getOptionValue(SERVICE_URL).trim() : serviceUrl;
    serviceAccessKey = (serviceAccessKey == null) ? processedCommandLineArgs.getOptionValue(ACCESS_KEY).trim() : serviceAccessKey;
    serviceAccessSecret = (serviceAccessSecret == null) ? processedCommandLineArgs.getOptionValue(ACCESS_SECRET).trim() : serviceAccessSecret;

    force = (!force) ? Boolean.parseBoolean(processedCommandLineArgs.getOptionValue(FORCE)) : force;
    //</editor-fold>

    LOGGER.debug("Intitialization finished");
  }

  /**
   * Update the status of an ingest via the REST service. This method is only be
   * used for ingests. The status of the associated ingest will be updated on
   * the server side. Depending on the status, server-side actions will be
   * triggered, e.g. the actual ingest to an archive.
   *
   * @param pNewStatus The new status obtained from the transfer client. This
   * status will be mapped to INGEST_STATUS.PRE_INGEST_RUNNING,
   * INGEST_STATUS.PRE_INGEST_FINISHED or INGEST_STATUS.PRE_INGEST_FAILED.
   *
   * @return TRUE if the status was updated successfully.
   */
  public final boolean publishStatusChange(AbstractTransferClient.TRANSFER_STATUS pNewStatus) {
    INGEST_STATUS newStatus = null;
    switch (pNewStatus) {
      case PREPARING:
        //do nothing
        break;
      case RUNNING:
        newStatus = INGEST_STATUS.PRE_INGEST_RUNNING;
        break;
      case TRANSFERRING:
        break;
      case CLEANUP:
        break;
      case SUCCEEDED:
        newStatus = INGEST_STATUS.PRE_INGEST_FINISHED;
        break;
      case INTERNAL_PREPARATION_FAILED:
      case EXTERNAL_PREPARATION_FAILED:
      case PRE_PROCESSING_FAILED:
      case TRANSFER_FAILED:
      case TRANSFER_LOCKED:
      case FAILED:
        newStatus = INGEST_STATUS.PRE_INGEST_FAILED;
        break;
      default:
      //do nothing
    }
    boolean result;

    if (newStatus == null) {
      LOGGER.info("No status obtained for input {}. Returning FALSE.", pNewStatus);
      return false;
    }
    LOGGER.debug("Update status for id {} to new status {}", new Object[]{getTransferId(), newStatus});
    result = getStagingServiceRESTClient().updateIngest(getTransferId(), null, newStatus.getId(), getContext()).getStatus() == 200;
    if (result) {
      LOGGER.debug("Status update successful");
    } else {
      LOGGER.warn("Status update failed");
    }

    return result;
  }

  /**
   * Send a heartbeat (a null-update) via the REST service. This method must
   * only be used for ingests. The associated ingest entity if obtained and its
   * status and error message are submitted unchanged. On the server side this
   * will lead to an extended expire-timestamp and the lastAccess value will
   * change.
   *
   * @return TRUE if the heartbeat could be sent.
   */
  public final boolean sendHeartbeat() {
    boolean result = false;
    IngestInformationWrapper serviceResult = getStagingServiceRESTClient().getIngestById(getTransferId(), getContext());
    if (!serviceResult.getEntities().isEmpty()) {
      IngestInformation entity = serviceResult.getEntities().get(0);
      result = getStagingServiceRESTClient().updateIngest(getTransferId(), entity.getErrorMessage(), entity.getStatus(), getContext()).getStatus() == 200;
      LOGGER.debug("Sent heartbeat to ingest #{}. Success: {}" + getTransferId(), result);
    } else {
      LOGGER.warn("No entity found for id {}. Skip sending heartbeat.", getTransferId());
    }
    return result;
  }
}
