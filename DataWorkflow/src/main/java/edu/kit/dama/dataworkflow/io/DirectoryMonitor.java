/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.dataworkflow.io;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.LoggerFactory;

/**
 * The DirectoryMonitor allows to monitor a single file or a directory for
 * changes using the lastModified flag. If the monitored file is a directory,
 * the directory will be checked recursively. If a new file appears in any
 * directory the internal ChangeDetected-flag is set 'true'. The same happens if
 * any previously found file has changed since the last check.
 *
 * @author Jejkal
 */
public class DirectoryMonitor extends Thread {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DirectoryMonitor.class);
  /**
   * Flag that tells us if this monitor is running or not
   */
  private boolean bRunning = false;
  /**
   * The list of monitored files and directories
   */
  private List<String> mFileList = null;
  /**
   * The map of all files (single files and files located in monitored
   * directories) and their lastModified flag
   */
  private Map<File, Long> mFileMap = null;
  /**
   * Flag that tells us if the last monitoring run found any modification
   */
  private boolean bChangeDetected = false;
  /**
   * The frequence at which check runs are performed
   */
  private long iCheckFrequency = (int) DateUtils.MILLIS_PER_MINUTE;
  /**
   * The number of runs that did not found any modification
   */
  private int iFailedCheckCount = 0;

  /**
   * Default constructor
   */
  public DirectoryMonitor() {
    setDaemon(true);
    setPriority(MIN_PRIORITY);
    bRunning = true;
    mFileList = new LinkedList<String>();
    mFileMap = new HashMap<File, Long>();
  }

  /**
   * Returns the frequency in milliseconds at which the monitored directories
   * are checked
   *
   * @return long Check frequency in milliseconds (default: one minute)
   */
  public long getCheckFrequency() {
    return iCheckFrequency;
  }

  /**
   * Add one file to the list of monitored files
   *
   * @param pFile File to add
   */
  public void addFileToMonitor(String pFile) {
    LOGGER.info("Adding file '{}' to monitoring", pFile);
    mFileList.add(pFile);
  }

  /**
   * Remove one file from the list of monitored files
   *
   * @param pFile File to remove
   */
  public void removeFileToMonitor(String pFile) {
    LOGGER.info("Removing file '{}' from monitoring", pFile);
    mFileList.remove(pFile);
  }

  @Override
  public void run() {
    LOGGER.info("Starting file monitoring");
    try {
      do {
        String[] fileList = mFileList.toArray(new String[mFileList.size()]);
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Checking '" + fileList.length + ((fileList.length == 1) ? "' entry" : "' entries"));
        }
        //go through all files
        for (String file : fileList) {
          LOGGER.info(" Checking file '{}'", file);

          File f = new File(file);
          if (checkForChange(f)) {
            LOGGER.info(" Change detected");
            bChangeDetected = true;
            iFailedCheckCount = 0;
          } else {
            LOGGER.info(" No change detected");
            bChangeDetected = false;
          }
          if (!bRunning) {
            LOGGER.info("Monitoring was stopped");
            break;
          }
        }//check cycle finished
        if (!bRunning) {
          //leave the while loop
          break;
        }
        if (!bChangeDetected) {
          //no change detected in this cycle, increment the amount of failed checks
          iFailedCheckCount++;
        } else {
          //change was detected, reset the amount of failed checks
          iFailedCheckCount = 0;
        }
        try {
          sleep(iCheckFrequency);
        } catch (InterruptedException ie) {
        }
        LOGGER.info("Entering new monitoring cycle");
      } while (bRunning);
    } catch (Exception e) {
      LOGGER.warn("File monitoring has terminated unexpectedly", e);
    }

    //clean up (maybe) huge file map
    mFileMap.clear();
  }

  /**
   * Returns the number of failed checks where no file has changed
   *
   * @return int Number of failed checks
   */
  public int getFailedCheckCount() {
    return iFailedCheckCount;
  }

  /**
   * Return if there was a change detected during the last iteration
   *
   * @return boolean TRUE = At least one file has changed
   */
  public boolean wasChangeDetected() {
    return bChangeDetected;
  }

  /**
   * Stop this monitor
   */
  public void stopMonitoring() {
    if (bRunning) {
      LOGGER.info("Stopping monitoring");
      bRunning = false;
    }
  }

  /**
   * Check if this monitor is still running
   *
   * @return boolean TRUE = Monitor is still running
   */
  public boolean isRunning() {
    return bRunning;
  }

  /**
   * Check one single file for change. If pFileToCheck is a directory this
   * method will call itself recursively, if pFileToCheck is a file it is
   * checked if the internal<BR/>
   * map of monitored files contains this file. If it does the lastModified
   * timestamp is compared to the stored value, if the file was modified 'true'
   * is returned.<BR/>
   * If the internal map does not contain the file it is added to the internal
   * map and 'true' is returned.
   *
   * @param pFileToCheck File to check for change
   * @return boolean TRUE = At least one file has changed or was not monitored
   * yet
   */
  private boolean checkForChange(File pFileToCheck) {
    if (pFileToCheck.isDirectory()) {
      //pFileToCheck is a directory. Call yourself recursively for each file withing this directory.
      for (File f : pFileToCheck.listFiles()) {
        return checkForChange(f);
      }
    } else {
      //check if mFileMap contains this file
      if (!mFileMap.containsKey(pFileToCheck)) {
        //add this file to mFileMap, store the lastModified timestamp and return "modified"-status
        mFileMap.put(pFileToCheck, pFileToCheck.lastModified());
        return true;
      } else {
        //check stored lastModified timestamp with the current timestamp returned by the file object
        long lastModified = mFileMap.get(pFileToCheck);
        if (lastModified < pFileToCheck.lastModified()) {
          return true;
        }
      }
    }
    //nothing has changed
    return false;
  }
}
