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
package edu.kit.dama.mdm.content.util;

import edu.kit.dama.mdm.content.es.MetadataIndexingHelper;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.transfer.client.exceptions.CommandLineHelpOnlyException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class MetadataIndexer {

  private final static Logger LOGGER = LoggerFactory.getLogger(MetadataIndexer.class);

  /**
   * The command line switch for setting the id of the group for which the
   * metadata should be processed.
   */
  private static final String GROUP_ID = "groupId";
  /**
   * The command line switch for setting the elasticsearch hostname.
   */
  private static final String HOSTNAME = "hostname";
  /**
   * The command line switch for setting the elasticsearch port.
   */
  private static final String PORT = "port";
  /**
   * The command line switch for setting the cluster for publishing the
   * metadata.
   */
  private static final String CLUSTER = "cluster";
  /**
   * The command line switch for setting the index for publishing the metadata.
   */
  private static final String INDEX = "index";
  /**
   * The command line switch for setting the number of index tasks that should
   * be performed in this call.
   */
  private static final String TASKS = "tasks";
  /**
   * The command line switch for showing command line help
   */
  private static final String HELP = "help";
  private final static Options OPTIONS;
  private static CommandLine processedCommandLineArgs = null;

  private String groupId = Constants.USERS_GROUP_ID;
  private String hostname = "localhost";
  private int port = 9300;
  private String cluster = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_CLUSTER_ID, "KITDataManager");
  private String index = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_INDEX_ID, "dc");
  private int tasks = 10;

  static {
    OPTIONS = new Options();
    OPTIONS.addOption("g", GROUP_ID, true, "The id of the group whose metadata indexing entries should be processed. (default: USERS)");
    OPTIONS.addOption("n", HOSTNAME, true, "The hostname of the elasticsearch node to which the metadata should be published. (default: localhost)");
    OPTIONS.addOption("p", PORT, true, "The port of the elasticsearch node to which the metadata should be published. (default: 9300)");
    OPTIONS.addOption("c", CLUSTER, true, "The cluster to which the metadata should be published. (default: KITDataManager)");
    OPTIONS.addOption("i", INDEX, true, "The index to which the metadata should be published. (default: dc)");
    OPTIONS.addOption("t", TASKS, true, "The max. number of indexing tasks that should be performed in this call. With a task count of -1 all pending entries are indexed. (default: 10)");
    OPTIONS.addOption("h", HELP, false, "Print command line options.");

    OPTIONS.getOption("g").setRequired(false);
    OPTIONS.getOption("n").setRequired(false);
    OPTIONS.getOption("p").setRequired(false);
    OPTIONS.getOption("c").setRequired(false);
    OPTIONS.getOption("i").setRequired(false);
    OPTIONS.getOption("t").setRequired(false);
  }

  /**
   * Print the command line help.
   */
  private void printParameterHelp() {
    new HelpFormatter().printHelp("MetadataIndexer.sh", OPTIONS, true);
  }

  /**
   * Configure the indexer using the provided command line options.
   *
   * @param pArgs The command line options provided by the main method.
   *
   * @throws ConfigurationException If the command line options were in a wrong
   * format.
   * @throws CommandLineHelpOnlyException If the help switch was detected.
   */
  private void configure(String[] pArgs) throws ConfigurationException, CommandLineHelpOnlyException {
    CommandLineParser parser = new org.apache.commons.cli.DefaultParser();
    if (pArgs != null) {
      LOGGER.debug("Configuring metadata indexer using argument array {}", Arrays.asList(pArgs));
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
      throw new CommandLineHelpOnlyException();
    }

    LOGGER.debug("Processed CL options: {}", processedCommandLineArgs);
    //<editor-fold defaultstate="collapsed" desc=" Handle command line parsing and client initialization ">
    String sGroupId = processedCommandLineArgs.getOptionValue((char) 'g');
    //use provided id if one is specified, otherwise use default is 'USERS'
    if (sGroupId != null) {
      LOGGER.debug("Using groupId '{}' obtained from command line.", sGroupId);
      groupId = sGroupId;
    } else {
      LOGGER.debug("No groupId obtained from command line. Using default value {}.", groupId);
    }

    String sHostname = processedCommandLineArgs.getOptionValue((char) 'n');
    //use provided hostname if one is specified, otherwise use default is 'USERS'

    if (sHostname != null) {
      LOGGER.debug("Using hostname '{}' obtained from command line.", sHostname);
      hostname = sHostname;
    } else {
      LOGGER.debug("No hostname obtained from command line. Using default value {}.", hostname);
    }

    String sPort = processedCommandLineArgs.getOptionValue((char) 'p');
    //use provided port if one is specified, otherwise use default is 'USERS'
    try {
      if (sPort != null) {
        LOGGER.debug("Using port '{}' obtained from command line.", sPort);
        port = Integer.parseInt(sPort);
      } else {
        LOGGER.debug("No port obtained from command line. Using default value {}.", port);
      }
    } catch (NumberFormatException ex) {
      LOGGER.warn("Failed to parse port from value '{}'. Using default value {}", sPort, port);
    }

    String sIndex = processedCommandLineArgs.getOptionValue((char) 'i');
    if (sIndex != null) {
      LOGGER.debug("Using index '{}' obtained from command line.", sIndex);
      index = sIndex;
    } else {
      LOGGER.debug("No index obtained from command line. Using default value {}.", index);
    }

    String sClusterName = processedCommandLineArgs.getOptionValue((char) 'c');
    if (sClusterName != null) {
      LOGGER.debug("Using cluster '{}' obtained from command line.", sClusterName);
      cluster = sClusterName;
    } else {
      LOGGER.debug("No cluster obtained from command line. Using default value {}.", cluster);
    }

    String sTasks = processedCommandLineArgs.getOptionValue((char) 't');
    //use provided task count if one is specified, otherwise use default is '10'
    try {
      if (sTasks != null) {
        LOGGER.debug("Using task count '{}' obtained from command line.", sTasks);
        tasks = Integer.parseInt(sTasks);
        if (tasks < 0) {
          LOGGER.debug("Detected task count {} < 0 Setting task count to maximum.", tasks);
          tasks = Integer.MAX_VALUE;
        }
      } else {
        LOGGER.debug("No task count obtained from command line. Using default value {}.", tasks);
      }
    } catch (NumberFormatException ex) {
      LOGGER.warn("Failed to parse task count from value '{}'. Using default value {}", sTasks, tasks);
    }

    //</editor-fold>
    LOGGER.debug("Intitialization finished");
  }

  /**
   * Perform the actual indexing by using the {@link MetadataIndexingHelper}.
   *
   * @return The result of {@link MetadataIndexingHelper#performIndexing(java.lang.String, edu.kit.authorization.entities.GroupId, int, edu.kit.authorization.entities.IAuthorizationContext)
   * }.
   */
  private boolean performIndexing() {
    MetadataIndexingHelper.getSingleton().setHostname(hostname);
    MetadataIndexingHelper.getSingleton().setPort(port);
    return MetadataIndexingHelper.getSingleton().performIndexing(cluster, index, new GroupId(groupId), tasks, AuthorizationContext.factorySystemContext());
  }

  /**
   * The main method.
   *
   * @param args The provided command line arguments.
   */
  public static void main(String[] args) {
    MetadataIndexer indexer = new MetadataIndexer();
    int exitCode = 0;
    LOGGER.debug("Starting metadata indexer");
    boolean success = false;
    try {
      LOGGER.debug(" - Configuring indexer");
      indexer.configure(args);
      LOGGER.debug(" - Performing indexing");
      if (!indexer.performIndexing()) {
        exitCode ^= 2;
      }
      LOGGER.debug("Indexer has finished.");
      success = true;
    } catch (ConfigurationException ex) {
      LOGGER.error("Failed to configure metadata indexer", ex);
      exitCode ^= 1;
    } catch (CommandLineHelpOnlyException ex) {
      indexer.printParameterHelp();
      success = true;
      exitCode = 0;
    } finally {
      if (exitCode == 0 && !success) {
        LOGGER.warn("Unexpected result detected.");
        exitCode ^= 1;
      }
    }
    System.exit(exitCode);
  }
}
