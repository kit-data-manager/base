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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.transfer.client.types;

import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.lsdf.adalapi.exception.TransferNotSupportedException;
import edu.kit.dama.transfer.client.exceptions.InvalidEntityException;
import edu.kit.dama.transfer.client.interfaces.ITransferTaskListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.fzk.tools.xml.JaxenUtil;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class TransferTask extends Thread {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferTask.class);

  /**
   * Transfer status enum
   */
  public enum TRANSFER_TASK_STATUS {

    WAITING, RUNNING, SUCCEEDED, FAILED
  };
  /**
   * Delay between different transfer attempts
   */
  private static final int RETRY_DELAY_IN_SECONDS = 10;
  /**
   * The source file of this transfer
   */
  private AbstractFile sourceFile = null;
  /**
   * The target file of this transfer
   */
  private AbstractFile targetFile = null;
  /**
   * Flag which indicated if cleanup is performed or not
   */
  private boolean cleanup = false;
  /**
   * The current transfer status
   */
  private TRANSFER_TASK_STATUS status = TRANSFER_TASK_STATUS.WAITING;
  /**
   * The collection of transfer task listeners
   */
  private Collection<ITransferTaskListener> transferTaskListener = null;
  /**
   * The max. number of tries to transfer a file successfully
   */
  private static final int MAX_TRIES = 3;

  /**
   * Creates a new transfer task which performs no cleanup after transfer. This
   * should be used as default constructor for transferring original files.
   *
   * @param pSource The source file of this transfer
   * @param pTarget The target file of this transfer
   */
  public TransferTask(AbstractFile pSource, AbstractFile pTarget) {
    this(pSource, pTarget, false);
  }

  /**
   * Creates a new transfer task.
   *
   * @param pSource The source file of this transfer
   * @param pTarget The target file of this transfer
   * @param pCleanup Delete source file after successfull transfer
   */
  public TransferTask(AbstractFile pSource, AbstractFile pTarget, boolean pCleanup) {
    if (pSource == null || pTarget == null) {
      throw new IllegalArgumentException("Neither pSource nor pTarget argument must be null");
    }
    sourceFile = pSource;
    targetFile = pTarget;
    cleanup = pCleanup;
    transferTaskListener = new ArrayList<ITransferTaskListener>();
    setDaemon(true);
  }

  /**
   * Add a transfer task listener
   *
   * @param pListener The listener to add
   */
  public final void addTransferTaskListener(ITransferTaskListener pListener) {
    if (!transferTaskListener.contains(pListener)) {
      transferTaskListener.add(pListener);
    }
  }

  /**
   * Remove a transfer task listener
   *
   * @param pListener The listener to remove
   */
  public final void removeTransferTaskListener(ITransferTaskListener pListener) {
    transferTaskListener.remove(pListener);
  }

  /**
   * Get the source file of this transfer task. The source file should be local
   * for upload tasks and may be local or remote for download tasks.
   *
   * @return An AbstractFile instance representing the target file of this
   * transfer task
   */
  public final AbstractFile getSourceFile() {
    return sourceFile;
  }

  /**
   * Get the target file of this transfer task. The target file must be local
   * for download tasks and may be local or remote for upload tasks.
   *
   * @return An AbstractFile instance representing the target file of this
   * transfer task
   */
  public final AbstractFile getTargetFile() {
    return targetFile;
  }

  /**
   * Returns if this tasks removes the source file after a successfull transfer.
   *
   * @return TRUE if the source file will be removed autmatically
   */
  public final boolean shouldCleanup() {
    return cleanup;
  }

  /**
   * Sets if this tasks removes the source file after a successfull transfer or
   * not.
   *
   * @param pValue TRUE = perform cleanup
   */
  public final void setCleanup(boolean pValue) {
    cleanup = pValue;
  }

  /**
   * Returns 'true' if this TransferTask's source file equals pSource
   *
   * @param pSource The AbstractFile to check
   *
   * @return TRUE if pSource equals getSourceFile()
   */
  public final boolean sourceEquals(AbstractFile pSource) {
    return getSourceFile().equals(pSource);
  }

  /**
   * Returns 'true' if this TransferTask's target file equals pTarget
   *
   * @param pTarget The AbstractFile to check
   *
   * @return TRUE if pTarget equals getTargetFile()
   */
  public final boolean targetEquals(AbstractFile pTarget) {
    return getTargetFile().equals(pTarget);
  }

  /**
   * Returns the current status of the transfer task
   *
   * @return The transfer status
   */
  public final TRANSFER_TASK_STATUS getStatus() {
    return status;
  }

  /**
   * Perform the transfer task by transferring the source file to the target
   * file. After the transfer the source file may be deleted if cleanup was
   * requested. If cleanup was requested and fails, only a warning is logged, as
   * the transfer has succeeded.
   */
  @Override
  public final void run() {
    status = TRANSFER_TASK_STATUS.RUNNING;
    fireTransferStartedEvents();
    boolean transferSucceeded = false;
    //perform the transfer task including retry handling
    for (int attempt = 1; attempt <= MAX_TRIES; attempt++) {
      if (!transferSucceeded && attempt > 1) {
        LOGGER.info("Retrying transfer...");
      }
      try {
        LOGGER.info("TransferTask starts transfer from {} to {}", new Object[]{getSourceFile(), getTargetFile()});
        getSourceFile().transfer(getTargetFile());
        transferSucceeded = true;
        break;
      } catch (AdalapiException ae) {//normal exception...retry
        LOGGER.error("Failed to transfer file " + getSourceFile() + " to " + getTargetFile() + "(Attempt: " + attempt + ")", ae);
      } catch (TransferNotSupportedException tnse) {//fatal exception (file not readable, does not exist...), retry won't help.
        LOGGER.error("Failed to transfer file " + getSourceFile() + " to " + getTargetFile() + ". Not using more attempts.", tnse);
        attempt = MAX_TRIES;
      }

      //wait retry delay
      try {
        Thread.sleep(RETRY_DELAY_IN_SECONDS * DateUtils.MILLIS_PER_SECOND);
      } catch (InterruptedException ie) {
      }
    }

    //do cleanup if transfer has succeeded
    if (!transferSucceeded) {
      LOGGER.error("Transfer failed within {} tries. Skipping cleanup and aborting!", MAX_TRIES);
    } else {
      if (!cleanup()) {
        //log warning
        LOGGER.info("Cleanup failed. Manual cleanup for file {} necessary.", getSourceFile());
      }
    }

    if (transferSucceeded) {
      status = TRANSFER_TASK_STATUS.SUCCEEDED;
      fireTransferFinishedEvents();
    } else {
      status = TRANSFER_TASK_STATUS.FAILED;
      fireTransferFailedEvents();
    }
  }

  /**
   * Cleanup the source file if the transfer has succeeded.
   *
   * @return TRUE if the cleanup succeeded
   */
  public final boolean cleanup() {
    if (!cleanup) {
      //no cleanup needed
      return true;
    }
    return true;
  }

  /**
   * Creates an XML representation of this TransferTask. The returned string
   * contains at least source and target URL. It may also contain the cleanup
   * behavior if cleanup is requested. Otherwise, this element is skipped for
   * performance reasons.
   *
   * @return The XML representation of this TransferTask as string
   *
   * @throws IOException If source or target file could not be transformed to an
   * URI
   */
  public final String toXml() throws IOException {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<transferTask>\n");
    //try {
    try {
      buffer.append("<source encoding='UTF-8'>").append(URLEncoder.encode(getSourceFile().getUrl().toString(), "UTF-8")).append("</source>\n");
      buffer.append("<target encoding='UTF-8'>").append(URLEncoder.encode(getTargetFile().getUrl().toString(), "UTF-8")).append("</target>\n");
    } catch (UnsupportedEncodingException usee) {//try to add entry without enconding
      buffer.append("<source>").append(getSourceFile().getUrl().toString()).append("</source>\n");
      buffer.append("<target>").append(getTargetFile().getUrl().toString()).append("</target>\n");
    }
    /*
     * } catch (URISyntaxException use) { throw new IOException("Failed to
     * transform transfer task to XML (Source: " + getSourceFile() + ",
     * Target: " + getTargetFile() + ")", use);
     }
     */
    if (shouldCleanup()) {
      buffer.append("<cleanup>").append(shouldCleanup()).append("</cleanup>\n");
    }
    buffer.append("</transferTask>");
    return buffer.toString();
  }

  /**
   * Writes a list of TransferTask entities in XML format into pFile
   *
   * @param pTasks The list of tasks to persist
   * @param pFile The file where the data will be written to
   *
   * @throws IOException If the file could not be written
   */
  public static void toXML(List<TransferTask> pTasks, File pFile) throws IOException {
    StringBuilder buffer = new StringBuilder();
    FileWriter writer = null;
    buffer.append("<transferTasks>\n");
    try {
      writer = new FileWriter(pFile);
      int cnt = 0;
      for (TransferTask task : pTasks) {
        buffer.append(task.toXml()).append("\n");
        cnt++;
        if (cnt % 100 == 0) {//flush buffer all 100 tasks
          writer.write(buffer.toString());
          writer.flush();
          buffer = new StringBuilder();
        }
      }
      writer.write(buffer.toString());
      writer.write("</transferTasks>\n");
      writer.flush();
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException ioe) {
        }
      }
    }
  }

  /**
   * Reads a list of TransferTask entities from pFile
   *
   * @param pFile The file containing a list of TransferTask entities in their
   * XML representation
   *
   * @return The list of obtained entities
   *
   * @throws IOException If the list of TransferTask entities could not be
   * obtained, either because the file is invalid or because any XML
   * representation was invalid
   */
  public static List<TransferTask> fromXML(File pFile) throws IOException {
    List<TransferTask> tasks = new ArrayList<TransferTask>();
    Document doc = null;
    try {
      doc = JaxenUtil.getDocument(pFile);
    } catch (Exception e) {
      throw new IOException("Failed to read TransferTasks from file '" + pFile + "'", e);
    }
    for (Element element : (List<Element>) JaxenUtil.getNodes(doc, "//transferTasks/transferTask")) {
      try {
        tasks.add(TransferTask.fromXml(element));
      } catch (InvalidEntityException iee) {
        throw new IOException("Failed to read TransferTask entity from file '" + pFile + "'", iee);
      }
    }
    return tasks;
  }

  /**
   * Configures this TransferTask using a JDOM element, which contains source
   * and target URL (mandatory) and cleanup behavior (optional).
   *
   * @param pElement The JDOM element containing this TransferTask
   * representation
   *
   * @return A TransferTask configured via the provided JDOM element pElement
   *
   * @throws InvalidEntityException If pElement cannot be parsed, either because
   * source or target is missing/invalid, or because the source file does not
   * exist or could not be accessed.
   */
  private static TransferTask fromXml(Element pElement) throws InvalidEntityException {
    Element sourceElement = pElement.getChild("source");
    Element targetElement = pElement.getChild("target");
    Element cleanupElement = pElement.getChild("cleanup");
    if (sourceElement == null || targetElement == null) {
      throw new InvalidEntityException("Either source or target child is missing");
    }

    AbstractFile source = elementToAbstractFile(sourceElement);
    try {
      if (!source.exists()) {
        throw new InvalidEntityException("Source file '" + source + "' of TransferTask does not exists anymore");
      }
    } catch (AdalapiException ae) {
      throw new InvalidEntityException("Failed to check whether source file '" + source + "' exists or not", ae);
    }
    AbstractFile target = elementToAbstractFile(targetElement);
    boolean doCleanup = false;
    if (cleanupElement != null) {
      doCleanup = Boolean.parseBoolean(cleanupElement.getTextTrim());
    }
    return new TransferTask(source, target, doCleanup);
  }

  /**
   * Convernt an XML element to an AbstractFile by encoding the contained URL
   * using the provided encoding type or no type is none is defined.
   *
   * @param pElement The XML element containing enconding as attribute and URL
   * as value
   *
   * @return AbstractFile The AbstractFile obtained from pElement
   *
   * @throws InvalidEntityException If the encoding is not supported or if the
   * contained URL is invalid
   */
  private static AbstractFile elementToAbstractFile(Element pElement) throws InvalidEntityException {
    AbstractFile result = null;
    Attribute encondingAttribute = pElement.getAttribute("encoding");
    if (encondingAttribute != null) {
      try {
        result = new AbstractFile(new URL(URLDecoder.decode(pElement.getTextTrim(), encondingAttribute.getValue())));
      } catch (UnsupportedEncodingException use) {
        throw new InvalidEntityException("URL encoding '" + encondingAttribute.getValue() + "' is not supported", use);
      } catch (MalformedURLException mue) {
        throw new InvalidEntityException("URL '" + pElement.getTextTrim() + "' is invalid", mue);
      }
    } else {
      try {
        result = new AbstractFile(new URL(pElement.getTextTrim()));
      } catch (MalformedURLException mue) {
        throw new InvalidEntityException("URL '" + pElement.getTextTrim() + "' is invalid", mue);
      }
    }
    return result;
  }

  /**
   * Notify all transfer listener on a started transfer
   */
  private void fireTransferStartedEvents() {
    for (ITransferTaskListener listener : transferTaskListener.toArray(new ITransferTaskListener[transferTaskListener.size()])) {
      listener.transferStarted(this);
    }
  }

  /**
   * Notify all transfer listener on a finished transfer
   */
  private void fireTransferFinishedEvents() {
    for (ITransferTaskListener listener : transferTaskListener.toArray(new ITransferTaskListener[transferTaskListener.size()])) {
      listener.transferFinished(this);
    }
  }

  /**
   * Notify all transfer listener on a failed transfer
   */
  private void fireTransferFailedEvents() {
    for (ITransferTaskListener listener : transferTaskListener.toArray(new ITransferTaskListener[transferTaskListener.size()])) {
      listener.transferFailed(this);
    }
  }
}