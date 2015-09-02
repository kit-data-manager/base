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
package edu.kit.dama.transfer.client.impl;

import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.transfer.client.impl.ui.TransferUI;
import edu.kit.dama.transfer.client.exceptions.CommandLineHelpOnlyException;
import edu.kit.dama.transfer.client.exceptions.TransferClientInstatiationException;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class GUIUploadClient extends BaseUserClient implements Thread.UncaughtExceptionHandler {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GUIUploadClient.class);
  /**
   * The graphical user interface
   */
  private TransferUI userInterface = null;

  /**
   * Default constructor
   *
   * @param pArgs The command line argument array
   *
   * @throws TransferClientInstatiationException If the initialization fails
   * @throws CommandLineHelpOnlyException If the user provided the option to
   * show command line help only
   */
  public GUIUploadClient(String[] pArgs) throws TransferClientInstatiationException, CommandLineHelpOnlyException {
    super();
    configure(pArgs);
    userInterface = new TransferUI(this);
  }

  /**
   * Set the user interface visible
   */
  public final void setVisible() {
    userInterface.setVisible(true);
  }

  /**
   * Returns if the transfer UI is still visible
   *
   * @return TRUE = The transfer UI is visible
   */
  public final boolean isVisible() {
    return userInterface.isVisible();
  }

  /**
   * The main entry point
   *
   * @param args The command line argument array
   */
  public static void main(String[] args) {
    int result = 0;
    AbstractFile.setCheckLevel(AbstractFile.CHECK_LEVEL.COARSE);
    GUIUploadClient client;
    try {
      client = new GUIUploadClient(args);
      Thread.currentThread().setUncaughtExceptionHandler(client);
      client.setVisible();
      while (client.isVisible()) {
        try {
          Thread.sleep(DateUtils.MILLIS_PER_SECOND);
        } catch (InterruptedException ie) {
        }
      }
    } catch (TransferClientInstatiationException ie) {
      LOGGER.error("Failed to instantiate GUI client", ie);
      result = 1;
    } catch (CommandLineHelpOnlyException choe) {
      result = 0;
    }
    System.exit(result);
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    if (userInterface != null) {
      userInterface.handleException(t.getName(), e);
    }
  }
}
