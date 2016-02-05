/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
package edu.kit.dama.dataworkflow.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a monitor responsible for getting monitoring information
 * via input stream.
 *
 * @author Jejkal
 */
public class InputStreamMonitor extends Thread {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(InputStreamMonitor.class);
  private InputStream mSource = null;
  private FileOutputStream mDestination = null;

  /**
   * Default constructor which is used to initialize member variables, to set
   * the thread name and to set the thread to daemon mode
   *
   * @param pName Name used to identify this monitor within logfiles. Names
   * might be 'StdOut' or 'StdErr'
   * @param pSource InputStream from which we'll read
   * @param pDestination FileOutputStream to which we'll write
   */
  public InputStreamMonitor(String pName, InputStream pSource, FileOutputStream pDestination) {
    mSource = pSource;
    mDestination = pDestination;
    setName("InputStreamMonitor - " + pName + " (" + hashCode() + ") -");
    setDaemon(true);
  }

  @Override
  public void run() {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Starting new monitor named '" + getName() + " (ID: " + hashCode() + ")");
    }
    try {
      if (mSource == null || mDestination == null) {
        throw new IOException("Either source or destination stream is null");
      }
      //enter the run loop
      while (true) {
        byte[] data = new byte[1024];
        int read;
        if ((read = mSource.read(data)) != -1) {
          //write read data to destination
          mDestination.write(data, 0, read);
          mDestination.flush();
        } else {
          //nothing was read/the source stream has no more data
          if (LOGGER.isInfoEnabled()) {
            LOGGER.info("InputStreamMonitor '" + getName() + " (ID: " + hashCode() + ") has terminated properly");
          }
          //leave the run loop
          break;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("InputStreamMonitor '" + getName() + " (ID: " + hashCode() + ") has terminated unexpectedly", e);
    } finally {
      //close source and destination...exceptions can be ignored
      try {
        if (mDestination != null) {
          mDestination.flush();
          mDestination.close();
        }
      } catch (IOException ioe) {
      }
      try {
        if (mSource != null) {
          mSource.close();
        }
      } catch (IOException ioe) {
      }
    }
  }
}
