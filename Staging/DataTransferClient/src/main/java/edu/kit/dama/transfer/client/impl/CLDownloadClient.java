/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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

import edu.kit.dama.transfer.client.exceptions.CommandLineHelpOnlyException;
import edu.kit.dama.transfer.client.exceptions.TransferClientInstatiationException;
import edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS;
import edu.kit.dama.transfer.client.interfaces.ITransferStatusListener;
import edu.kit.dama.transfer.client.interfaces.ITransferTaskListener;
import edu.kit.dama.transfer.client.types.TransferTask;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class CLDownloadClient extends BaseUserClient implements ITransferTaskListener, ITransferStatusListener, Thread.UncaughtExceptionHandler {

  /**
   * The Logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(CLDownloadClient.class);

  /**
   * Default constructor.
   *
   * @param pArgs Arguments obtained from the command line call.
   *
   * @throws TransferClientInstatiationException If the instantiation fails.
   * @throws CommandLineHelpOnlyException If printing the command line help was
   * requested via command line args.
   */
  public CLDownloadClient(String[] pArgs) throws TransferClientInstatiationException, CommandLineHelpOnlyException {
    addOption(LOCAL, "The local target directory for putting the downloaded data.", 1, "localTarget", true);
    configure(pArgs);
    String localFolder = getProcessedOptionValue(LOCAL);

    if (localFolder == null) {
      throw new TransferClientInstatiationException("No valid local folder specified via -l or --local");
    }
    //check for url
    LOGGER.debug("Checking local folder {}", localFolder);
    try {
      URL localFolderUrl = new URL(localFolder);
      LOGGER.debug("Local folder seems to be a valid URL ({})", localFolderUrl);
      setLocalUrl(localFolder);
      LOGGER.debug("Local folder seems to be a URL");
    } catch (MalformedURLException ex) {
      LOGGER.debug("Provided local folder seems to be no URL");
      File localFolderFile = new File(localFolder);
      if (localFolderFile.exists() && localFolderFile.isDirectory()) {
        
        LOGGER.debug("Provided local folder is a valid directory");
        try {
          setLocalUrl(localFolderFile.toURI().toURL().toString());
        } catch (MalformedURLException inner) {
          throw new TransferClientInstatiationException("Failed to convert local folder " + localFolder + " to a URL", inner);
        }
      } else {
        throw new TransferClientInstatiationException("Argument -l | --local " + localFolder + " seems to be no valid URL and no local directory", ex);
      }
    }

    addTransferTaskListener(CLDownloadClient.this);
    addTransferStatusListener(CLDownloadClient.this);
    //add a shutdown hook to be able to cancel the transfer if the user terminates the client
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override
      public void run() {
        LOGGER.info("Performing shutdown operations.");
        if (getTransferClient() != null && getTransferClient().isTransferRunning()) {
          LOGGER.info(" * Transfer still running. Canceling outstanding operations.");
          cancelTransfer();
        } else {
          LOGGER.info(" * Transfer has finished, waiting for default cleanup.");
        }
      }
    }));
  }

  @Override
  public final void transferStarted(TransferTask pTask) {
    System.out.printf("Transfer %1$s -> %2$s started.%n", new Object[]{formatString(pTask.getSourceFile().getUrl().toString()), formatString(pTask.getTargetFile().getUrl().toString())});
  }

  @Override
  public final void transferFinished(TransferTask pTask) {
    int finished = (int) Math.rint((double) getTransferClient().getTransferInfo().getFinishedTaskCount() / (double) getTransferClient().getTransferInfo().getTaskCount() * 100d);
    System.out.printf("Transfer %1$s -> %2$s finished. [%3$s%%]%n", new Object[]{formatString(pTask.getSourceFile().getUrl().toString()), formatString(pTask.getTargetFile().getUrl().toString()), Integer.toString(finished)});
  }

  @Override
  public final void transferFailed(TransferTask pTask) {
    System.err.printf("Transfer %1$s -> %2$s failed.%n", new Object[]{formatString(pTask.getSourceFile().getUrl().toString()), formatString(pTask.getTargetFile().getUrl().toString())});
  }

  /**
   * String format helper for abbreviation.
   *
   * @param pString The string.
   *
   * @return The string limited to 20 characters.
   */
  private String formatString(String pString) {
    return StringUtils.abbreviateMiddle(pString, "(...)", 20);
  }

  /**
   * Main entry point.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    int result = 0;
    CLDownloadClient client;
    try {
      client = new CLDownloadClient(args);
      Thread.currentThread().setUncaughtExceptionHandler(client);
      client.performDownload();
      while (client.getTransferClient().isTransferRunning()) {
        try {
          Thread.sleep(DateUtils.MILLIS_PER_SECOND);
        } catch (InterruptedException ie) {
        }
      }
    } catch (TransferClientInstatiationException ie) {
      LOGGER.error("Failed to create instance of command line client", ie);
      result = 1;
    } catch (CommandLineHelpOnlyException choe) {
      result = 0;
    }
    System.exit(result);
  }

  @Override
  public final void fireStatusChangedEvent(TRANSFER_STATUS pOldStatus, TRANSFER_STATUS pNewStatus) {
    LOGGER.info("Transfer status changed from {} to {}", new Object[]{pOldStatus, pNewStatus});
  }

  @Override
  public final void fireTransferAliveEvent() {
    LOGGER.info("Transfer still running...");
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    LOGGER.error("Uncaught exception detected in thread " + t, e);
  }
}
