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
package edu.kit.dama.staging.util;

import edu.kit.dama.staging.entities.download.DownloadInformation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.fzk.grid.util.MailNotifier;
import org.fzk.grid.util.MailNotifierListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mail notification helper which is used to notify the user about available
 * downloads. The notification settings are obtained while scheduling a download
 * and they are written to a sub folder of the locally cached download. If the
 * download is prepared, the settings are read again and the notification is
 * send to the user.
 *
 * @author jejkal
 */
public final class MailNotificationHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(MailNotificationHelper.class);
  private static String mailServer = null;
  private static String sender = null;
  private final static String NOTIFICATION_PROPERTY_FILE = "notify.properties";
  private static boolean CONFIGURED = false;

  /**
   * Hidden constructor.
   */
  MailNotificationHelper() {
  }

  /**
   * Configure the helper with a mail server and the sender email.
   *
   * @param pServer The mail server.
   * @param pSender The sender of notification mails.
   */
  public static void configure(String pServer, String pSender) {
    mailServer = pServer;
    sender = pSender;

    if (mailServer != null && sender != null) {
      CONFIGURED = true;
    } else {
      LOGGER.warn("Either the configured mail server or sender is 'null'. Mail notification won't work.");
    }
  }

  /**
   * Store the mail notification properties in a file located in the provided
   * destination folder.
   *
   * @param pDestinationFolder The folder where the properties file is stored.
   * @param pReceiver The receiver of the mail notification.
   */
  public static void storeProperties(File pDestinationFolder, String pReceiver) {
    if (!CONFIGURED) {
      LOGGER.warn("Mail notifier is not configured properly");
      return;
    }
    Properties props = new Properties();
    props.put("receiver", pReceiver);
    FileOutputStream fout = null;
    try {
      fout = new FileOutputStream(new File(pDestinationFolder, NOTIFICATION_PROPERTY_FILE));
      props.store(fout, null);
    } catch (IOException ioe) {
      LOGGER.error("Failed to store mail notifier properies", ioe);
    } finally {
      if (fout != null) {
        try {
          fout.close();
        } catch (IOException ex) {
        }
      }
    }
  }

  /**
   * Restore all properties from a properties file located in the provided
   * folder.
   *
   * @param pSourceFolder The folder where the properties file is located.
   *
   * @return The restored properties object.
   */
  public static Properties restoreProperties(File pSourceFolder) {
    if (!CONFIGURED) {
      LOGGER.warn("Mail notifier is not configured properly");
      return new Properties();
    }
    File notifyProperties = new File(pSourceFolder, NOTIFICATION_PROPERTY_FILE);

    Properties result = new Properties();
    if (notifyProperties.exists()) {
      FileInputStream fin = null;
      try {
        LOGGER.debug("Loading notification properties");
        fin = new FileInputStream(notifyProperties);
        result.load(fin);
        LOGGER.debug("Removing notification properties file");
        if (notifyProperties.delete()) {
          LOGGER.debug("Notification properties file successfully deleted");
        } else {
          LOGGER.warn("Failed to delete notification properties file");
        }
      } catch (IOException ex) {
        LOGGER.error("Failed to load notification properties. Notification won't be sent.", ex);
      } finally {
        if (fin != null) {
          try {
            fin.close();
          } catch (IOException ex) {
          }
        }
      }
    } else {
      LOGGER.info("No mail notification properties found at {}", pSourceFolder.getPath());
    }
    return result;
  }

  /**
   * Send a download notification.
   *
   * @param pProperties Properties to use.
   * @param pInformation The download information about to notify.
   */
  public static void sendDownloadNotification(Properties pProperties, final DownloadInformation pInformation) {
    if (!CONFIGURED) {
      LOGGER.warn("Mail notifier is not configured properly");
      return;
    }

    final String receiver = pProperties.getProperty("receiver");
    LOGGER.debug("Try to send notification to {}", receiver);
    LOGGER.debug(" - Building message");
    StringBuilder builder = new StringBuilder();

    builder.append("Your download with the transfer ID ").append(pInformation.getTransferId()).append(" is now ready.\n");
    builder.append("You can access the data via:\n\n").append("  ").append(pInformation.getClientAccessUrl()).append("\n\n");
    builder.append("This mail is generated automatically. Please do not reply.");

    LOGGER.debug(" - Sending mail via mail server {} as {}", new Object[]{mailServer, sender});
    MailNotifier notifier = new MailNotifier(sender, new MailNotifierListener() {
      @Override
      public void fireMailSendEvent() {
        LOGGER.debug("Download notification for download {} successfully sent to {}", new Object[]{pInformation.getTransferId(), receiver});
      }

      @Override
      public void fireMailSendFailedEvent(String errorMessage) {
        LOGGER.error("Failed to send notification for download {} to {}. Message: {}", new Object[]{pInformation.getTransferId(), receiver, errorMessage});
      }
    }, mailServer);
    notifier.sendMail(receiver, "Download #" + pInformation.getTransferId() + " available", builder.toString());
    LOGGER.debug("Mail notification finished.");
  }

//  public static void main(String[] args) {
//    System.out.println("START");
//    new MailNotifier("noreply@dama.kit.edu", new MailNotifierListener() {
//      @Override
//      public void fireMailSendEvent() {
//        System.out.println("OK");
//      }
//
//      @Override
//      public void fireMailSendFailedEvent(String errorMessage) {
//
//      }
//    }, "smtp.kit.edu").sendMail("support@kitdatamanager.net", "Download  available", "Success!");
//    System.out.println("DONE");
//  }
}
