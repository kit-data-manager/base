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
package edu.kit.dama.staging.util;

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.transfer.client.exceptions.CommandLineHelpOnlyException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.staging.exceptions.StagingIntitializationException;
import edu.kit.dama.staging.services.impl.StagingService;
import edu.kit.dama.staging.services.impl.download.DownloadInformationServiceLocal;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationServiceLocal;
import java.util.Arrays;
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
 * This tool is used to finalize transfers scheduled by KIT Data Manager. It
 * should be called frequently within e.g. a Cron job. There are two different
 * types of finalization which are done separately, the INGEST and DOWNLOAD
 * finalization. The type can be selected by the command line switch -t or
 * --type in combination with the additional argument INGEST or DOWNLOAD.
 *
 * A complete call of the finalizer tool may look as follows:
 *
 * edu.kit.dama.rest.staging.util.TransferFinalizer -t INGEST
 *
 * There are four different exit codes possible:
 *
 * <ul>
 * <li>0: Everything was fine, one transfer was finalized successfully.</li>
 *
 * <li>1: Command line argument validation failed.</li>
 *
 * <li>2: The finalization failed for some reasons. This happens if the max.
 * number of simultaneous transfers is already finalized or if an error occured
 * while finalizing the selected transfer.</li>
 *
 * <li>3: Initialization of the staging component failed. Mostly this is due to
 * misconfiguration or because the configured archive location is offline.</li>
 *
 * </ul>
 *
 * @author jejkal
 */
public class TransferFinalizer {

  private final static Logger LOGGER = LoggerFactory.getLogger(TransferFinalizer.class);

  private enum FINALIZE_TYPE {

    UNDEFINED, INGEST, DOWNLOAD;
  }
  /**
   * The command line switch for setting the finalization type.
   */
  private static final String TYPE = "type";
  /**
   * The command line switch for showing command line help
   */
  private static final String HELP = "help";
  private final static Options OPTIONS;
  private FINALIZE_TYPE type = FINALIZE_TYPE.UNDEFINED;
  private static CommandLine processedCommandLineArgs = null;

  static {
    OPTIONS = new Options();
    OPTIONS.addOption(OptionBuilder.withLongOpt(TYPE).withDescription("The finalization type. Valid options are: INGEST or DOWNLOAD.").hasArg().isRequired(true).create((char) 't'));
    OPTIONS.addOption(OptionBuilder.withLongOpt(HELP).withDescription("Print command line options.").create((char) 'h'));
  }

  /**
   * Main method.
   *
   * @param args The CL arguments.
   */
  public static void main(String[] args) {
    TransferFinalizer finalizer = new TransferFinalizer();
    int exitCode = 0;
    LOGGER.debug("Starting transfer finalizer");
    try {
      LOGGER.debug(" - Configuring finalizer");
      finalizer.configure(args);
      LOGGER.debug(" - Performing finalization");
      if (!finalizer.performFinalization()) {
        exitCode ^= 2;
      }

      LOGGER.debug(" - Performing cleanup");
      finalizer.performCleanup();
      LOGGER.debug("Finalizer has finished.");
    } catch (ConfigurationException ex) {
      LOGGER.error("Failed to configure transfer finalizer.", ex);
      exitCode ^= 1;
    } catch (StagingIntitializationException ex) {
      LOGGER.error("Failed to initialize staging.", ex);
      exitCode ^= 3;
    } catch (CommandLineHelpOnlyException ex) {
      finalizer.printParameterHelp();
      exitCode = 0;
    }
    System.exit(exitCode);
  }

  /**
   * Perform the actual finalization for the next transfer entry.
   *
   * @return TRUE if the selected transfer entry was finalized successfully,
   * FALSE otherwise.
   */
  private boolean performFinalization() {
    LOGGER.info("Starting transfer finalizer for type {}", type);
    boolean result = false;
    switch (type) {
      case DOWNLOAD:
        LOGGER.debug("Finalizing the next download");
        result = StagingService.getSingleton().finalizeDownloads();
        break;
      case INGEST:
        LOGGER.debug("Finalizing the next ingest");
        result = StagingService.getSingleton().finalizeIngests();
        break;
      case UNDEFINED:
        LOGGER.info("Finalization type is undefined. Skipping finalization.");
        result = true;
        break;
      default:
        LOGGER.error("Unknown finalization type set.");
    }
    return result;
  }

  /**
   * Perform cleanup by removing expired and deleted entities.
   */
  private void performCleanup() {
    LOGGER.debug("Cleaned up {} ingest(s)", IngestInformationServiceLocal.getSingleton().cleanup(AuthorizationContext.factorySystemContext()));
    LOGGER.debug("Cleaned up {} download(s)", DownloadInformationServiceLocal.getSingleton().cleanup(AuthorizationContext.factorySystemContext()));
  }

  /**
   * Print the command line help.
   */
  private void printParameterHelp() {
    new HelpFormatter().printHelp(getClass().getCanonicalName(), OPTIONS, true);
  }

  /**
   * Configure the finalizer using the provided command line options.
   *
   * @param pArgs The command line options provided by the main method.
   *
   * @throws ConfigurationException If the command line options were in a wrong
   * format.
   * @throws CommandLineHelpOnlyException If the help switch was detected.
   */
  private void configure(String[] pArgs) throws ConfigurationException, CommandLineHelpOnlyException {
    LOGGER.debug("Disabling ADALAPI overwrite checks");
    AbstractFile.setOverwritePermission(AbstractFile.OVERWRITE_PERMISSION.ALLOWED);
    CommandLineParser parser = new PosixParser();
    if (pArgs != null) {
      LOGGER.debug("Configuring transfer finalizer using argument array {}", Arrays.asList(pArgs));
    } else {
      LOGGER.warn("No argument array provided for configuration.");
    }

    processedCommandLineArgs = null;
    try {
      processedCommandLineArgs = parser.parse(OPTIONS, pArgs);
    } catch (ParseException pe) {
      printParameterHelp();
      throw new ConfigurationException("Failed to parse arguments", pe);
    }
    if (processedCommandLineArgs.hasOption(HELP)) {//print help and exit
      printParameterHelp();
      throw new CommandLineHelpOnlyException();
    }

    //<editor-fold defaultstate="collapsed" desc=" Handle command line parsing and transfer client initialization ">
    String sType = processedCommandLineArgs.getOptionValue((char) 't');

    LOGGER.debug("Configured finalization type is: {}", sType);

    if (sType == null) {
      LOGGER.info("No finalization type specified, setting {}", FINALIZE_TYPE.UNDEFINED);
      type = FINALIZE_TYPE.UNDEFINED;
    } else {
      try {
        type = FINALIZE_TYPE.valueOf(sType.trim());
      } catch (IllegalArgumentException ex) {
        throw new ConfigurationException("Failed to obtain finalization type. Type argument must be either INGEST or DOWNLOAD but is " + sType, ex);
      }
    }

    //</editor-fold>
    LOGGER.debug("Intitialization finished");
  }
}
