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
package edu.kit.dama.util;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class FileUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
  public static final int DEFAULT_TIMEOUT = 1000;

  /**
   * Hidden constructor.
   */
  private FileUtils() {
  }

  /**
   * Calls isAccessible(File, int) using the default timeout DEFAULT_TIMEOUT
   * (1000 ms).
   *
   * @param pFile The file to check.
   *
   * @return TRUE if pFile is read- and writeable.
   */
  public static boolean isAccessible(File pFile) {
    return isAccessible(pFile, DEFAULT_TIMEOUT);
  }

  /**
   * Helper method which checks if the provided file or one of its parents is
   * accessible (exists + r/w) within the provided timeout. In the background a
   * thread will perform the exists/r/w checks. If pFile is accessible, the
   * thread should return before the provided timeout was reached. If pFile is
   * located on a network device which is disconnected, the call should return
   * 'false' immediately. If pFile is located on a stale network device, 'false'
   * should be returned as soon as pTimeout was reached.
   *
   * @param pFile The file to check.
   * @param pTimeout The timeout in milliseconds (pTimeout must be &gt;= 1000,
   * otherwise it will be set to 1000)
   *
   * @return TRUE if pFile is read- and writeable.
   */
  public static boolean isAccessible(File pFile, int pTimeout) {
    int timeout = pTimeout;
    if (timeout < 1000) {
      LOGGER.debug("Provided timeout is less than 1 second which might be too short for network devices. Settings it to 1 second.");
      timeout = 1000;
    }
    LOGGER.debug("Performing accessibility check for file {}", pFile.getAbsolutePath());
    FileAccessWorker worker = new FileAccessWorker(pFile, Thread.currentThread());
    LOGGER.debug(" - Starting worker thread");
    worker.start();
    //wait until worker has finished
    LOGGER.debug(" - Waiting {} milliseconds", timeout);
    synchronized (Thread.currentThread()) {
      try {
        // long tBefore = System.currentTimeMillis();
        //while (System.currentTimeMillis() - tBefore < timeout) {
        //wait for 'timeout' or for notification and worker termination
        while (worker.isRunning()) {
          Thread.currentThread().wait(timeout);
        }
      } catch (InterruptedException ex) {
        LOGGER.warn("Waiting has been interrupted", ex);
      }
    }
    LOGGER.debug("Returning result '{}'", (worker.getResult()) ? "Accessible" : "NOT Accessible");
    return worker.getResult();
  }

//  public static void main(String[] args) {
//    long s = System.currentTimeMillis();
//    System.out.println(FileUtils.isAccessible(new File("a:/"), 1000));
//    System.out.println("Dur: " + (System.currentTimeMillis() - s));
//    s = System.currentTimeMillis();
//    System.out.println(FileUtils.isAccessible(new File("b:/"), 1000));
//    System.out.println("Dur: " + (System.currentTimeMillis() - s));
//    s = System.currentTimeMillis();
//    System.out.println(FileUtils.isAccessible(new File("c:/"), 1000));
//    System.out.println("Dur: " + (System.currentTimeMillis() - s));
//  }
}

/**
 * Worker implementation for checking file accessibility in a separate thread.
 * The caller has to wait and will be notified as soon as the access check has
 * finished.
 */
class FileAccessWorker extends Thread {

  private static final Logger LOGGER
          = LoggerFactory.getLogger(FileAccessWorker.class);
  private File file = null;
  private boolean running = true;
  private boolean result = false;
  private final Thread parentThread;

  /**
   * Default constructor.
   *
   * @param pFile The file for which the accessability should be checked.
   * @param pParent The parent thread to notify.
   */
  public FileAccessWorker(File pFile, Thread pParent) {
    parentThread = pParent;
    setDaemon(true);
    setPriority(Thread.MIN_PRIORITY);
    file = pFile;
  }

  @Override
  public void run() {
    File currentFile = file;
    LOGGER.debug("Checking file {}", currentFile);
    while (currentFile != null) {
      if (currentFile.exists()) {
        LOGGER.debug(" - File exists, checking accessibility");
        result = currentFile.canRead() && currentFile.canWrite();
        break;
      } else {
        File parent = currentFile.getParentFile();
        LOGGER.debug(" - File {} does not exist, checking parent file {}", new Object[]{currentFile, parent});
        currentFile = parent;
      }
    }
    LOGGER.debug("Accessibility successfully checked. Result is: {}", result);
    running = false;
    synchronized (parentThread) {
      parentThread.notify();
    }
  }

  /**
   * Get the result of the accessability check.
   *
   * @return TRUE = The file is accessible.
   */
  public boolean getResult() {
    return result;
  }

  /**
   * Check if this thread is still running.
   *
   * @return TRUE = The thread is still running.
   */
  public boolean isRunning() {
    return running;
  }
}
