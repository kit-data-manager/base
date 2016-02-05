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

import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of helper methods related to DataWorkflowTasks.
 *
 * @author mf6319
 */
public class DataWorkflowTaskUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataWorkflowTaskUtils.class);

  /**
   * Print the provided list of DataWorkflow tasks to StdOut, either in a basic
   * tabular view or in verbose mode, in a detailed representation.
   *
   * @param pTasks The list of tasks to print out.
   * @param pVerbose TRUE = print detailed view, FALSE = print basic tabular
   * view.
   */
  public static void printTaskList(List<DataWorkflowTask> pTasks, boolean pVerbose) {
    if (!pVerbose) {
      //do table listing
      //Headers: ID | STATUS | LAST_MODIFIED
      //Lengths: 10 | 34 | 34
      StringBuilder listing = new StringBuilder();
      listing.append(StringUtils.center("Task ID", 10)).append("|")
              .append(StringUtils.center("Status", 34)).append("|")
              .append(StringUtils.center("Last Modified", 34)).append("\n");
      for (DataWorkflowTask task : pTasks) {
        listing.append(StringUtils.center(Long.toString(task.getId()), 10)).append("|")
                .append(StringUtils.center(task.getStatus().toString(), 34)).append("|")
                .append(StringUtils.center(new SimpleDateFormat().format(task.getLastUpdate()), 34)).append("\n");
      }
      System.out.println(listing.toString());
    } else {
      //do detailed listing
      //ID: <ID>     Status: <STATUS>  Last Update: <LAST_UPDATE>
      //Config: <CONFIG_ID> Environment: <ID> Predecessor: <ID>
      //Group: <GID> Executor: <UID> Contact: <EMAIL>
      //Investigation: <ID>
      //InputDir: <URL>
      //OutputDir: <URL>              
      //WorkingDir: <URL>              
      //TempDir: <URL>
      //Input Objects         
      // Object | View 
      //  OID 1 | default
      //  OID 2 | default
      //Transfers          
      // Object | TransferId 
      //  OID 1 | 123
      //--------------------------------------------------------------
      StringBuilder builder = new StringBuilder();
      for (DataWorkflowTask task : pTasks) {
        builder.append(StringUtils.rightPad("Id: " + task.getId(), 40)).
                append(StringUtils.rightPad("Predecessor: " + ((task.getPredecessor() != null) ? task.getPredecessor().getId() : "-"), 40)).
                append("\n").
                append(StringUtils.rightPad("Status: " + task.getStatus(), 40)).
                append(StringUtils.rightPad("Last Update: " + new SimpleDateFormat().format(task.getLastUpdate()), 40)).
                append("\n").
                append(StringUtils.rightPad("Configuration: " + ((task.getConfiguration() != null) ? task.getConfiguration().getId() : "-"), 40)).
                append(StringUtils.rightPad("Environment: " + ((task.getExecutionEnvironment() != null) ? task.getExecutionEnvironment().getId() : "-"), 40)).
                append("\n").
                append(StringUtils.rightPad("Group: " + task.getExecutorGroupId(), 40)).
                append(StringUtils.rightPad("User: " + task.getExecutorId(), 40)).
                append("\n").
                append(StringUtils.rightPad("Contact UserId: " + ((task.getContactUserId() != null) ? task.getContactUserId() : "-"), 80)).
                append("\n").
                append(StringUtils.rightPad("InvestigationId: " + task.getInvestigationId(), 80)).
                append("\n").
                append(StringUtils.rightPad("Input Dir:", 15)).append(StringUtils.abbreviateMiddle(task.getInputDirectoryUrl(), "...", 65)).
                append("\n").
                append(StringUtils.rightPad("Output Dir:", 15)).append(StringUtils.abbreviateMiddle(task.getOutputDirectoryUrl(), "...", 65)).
                append("\n").
                append(StringUtils.rightPad("Working Dir:", 15)).append(StringUtils.abbreviateMiddle(task.getWorkingDirectoryUrl(), "...", 65)).
                append("\n").
                append(StringUtils.rightPad("Temp Dir:", 15)).append(StringUtils.abbreviateMiddle(task.getTempDirectoryUrl(), "...", 65)).
                append("\n").
                append(StringUtils.rightPad("Input Objects:", 80)).
                append("\n").
                append(StringUtils.center("ObjectId", 39)).append("|").append(StringUtils.center("View", 40)).
                append("\n");
        try {
          Properties objectViewMap = task.getObjectViewMapAsObject();
          Set<Entry<Object, Object>> entries = objectViewMap.entrySet();
          if (entries.isEmpty()) {
            builder.append(StringUtils.center("---", 39)).append("|").append(StringUtils.center("---", 40)).append("\n");
          } else {
            for (Entry<Object, Object> entry : entries) {
              builder.append(StringUtils.center((String) entry.getKey(), 39)).append("|").append(StringUtils.center((String) entry.getValue(), 40)).append("\n");
            }
          }
        } catch (IOException ex) {
          LOGGER.error("Failed to deserialize object-view map of DataWorkflow task " + task.getId(), ex);
          builder.append(StringUtils.center("---", 39)).append("|").append(StringUtils.center("---", 40)).append("\n");
        }
        builder.append(StringUtils.rightPad("Transfers:", 80)).
                append("\n").
                append(StringUtils.center("ObjectId", 39)).append("|").append(StringUtils.center("TransferId", 40)).
                append("\n");
        try {
          Properties objectTransferMap = task.getObjectTransferMapAsObject();
          Set<Entry<Object, Object>> entries = objectTransferMap.entrySet();
          if (entries.isEmpty()) {
            builder.append(StringUtils.center("---", 39)).append("|").append(StringUtils.center("---", 40)).append("\n");
          } else {
            for (Entry<Object, Object> entry : entries) {
              builder.append(StringUtils.center((String) entry.getKey(), 39)).append("|").append(StringUtils.center((String) entry.getValue(), 40)).append("\n");
            }
          }
        } catch (IOException ex) {
          LOGGER.error("Failed to deserialize object-transfer map of DataWorkflow task " + task.getId(), ex);
          builder.append(StringUtils.center("---", 39)).append("|").append(StringUtils.center("---", 40)).append("\n");
        }
        //add closing line
        builder.append(StringUtils.leftPad("", 80, '-')).append("\n");
      }
      System.out.println(builder.toString());
    }
  }
}
