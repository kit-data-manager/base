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

/*
 * GUIClient.java
 *
 * Created on 29.11.2011, 11:50:42
 */
package edu.kit.dama.transfer.client.impl.ui;

import edu.kit.dama.transfer.client.exceptions.TransferClientInstatiationException;
import edu.kit.dama.transfer.client.impl.AbstractTransferClient;
import edu.kit.dama.transfer.client.impl.AbstractTransferClient.TRANSFER_STATUS;
import edu.kit.dama.transfer.client.impl.BaseUserClient;
import edu.kit.dama.transfer.client.impl.GUIDownloadClient;
import edu.kit.dama.transfer.client.impl.GUIUploadClient;
import edu.kit.dama.transfer.client.interfaces.ITransferStatusListener;
import edu.kit.dama.transfer.client.interfaces.ITransferTaskListener;
import edu.kit.dama.transfer.client.types.TransferTask;
import edu.kit.dama.util.StackTraceUtil;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.TexturePaint;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.jdesktop.swingx.painter.MattePainter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class TransferUI extends javax.swing.JFrame implements DropTargetListener, ITransferStatusListener, ITransferTaskListener {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TransferUI.class);

  /**
   * Enum representing the current transfer state including resources for
   * graphical representation
   */
  private enum TRANSFER_STATE {

    NOT_INITIALIZED("/48x48/hand_point.png"),
    READY4TRANSFER("/48x48/media_play.png"),
    READY4RESUME("/48x48/refresh.png"),
    RUNNING("/48x48/server_client_exchange.png"),
    FINISHED("/48x48/check2.png"),
    FAILED("/48x48/error.png");
    /**
     * The icon resource representing the transfer state
     */
    private URL resource = null;

    /**
     * Default constructor
     *
     * @param pResourcePath The icon resource path
     */
    private TRANSFER_STATE(String pResourcePath) {
      resource = TRANSFER_STATE.class.getResource(pResourcePath);
    }

    /**
     * Returns the icon resource as URL
     *
     * @return The URL
     */
    public URL getImageResource() {
      return resource;
    }
  }
  /**
   * The local URL (source for upload, target for download)
   */
  private URL localURL = null;
  /**
   * The current transfer state
   */
  private TRANSFER_STATE currentState = TRANSFER_STATE.NOT_INITIALIZED;
  /**
   * The date formatter for logging output
   */
  private SimpleDateFormat dateFormat = null;
  /**
   * A list of status handlers notified by this client on state changes
   */
  private List<ITransferStatusListener> transferStatusListeners = null;
  /**
   * The base user client instance
   */
  private transient BaseUserClient guiClient = null;
  /**
   * Flag which indicated an upload/download
   */
  private boolean isUpload = false;

  /**
   * Default constructor
   *
   * @param pClient The base user client responsible for the file transfer
   */
  public TransferUI(BaseUserClient pClient) {
    initComponents();
    transferStatusListeners = new LinkedList<ITransferStatusListener>();
    guiClient = pClient;
    isUpload = guiClient instanceof GUIUploadClient;
    //  setupLogging();
    setupDnD();
    setupUI();
    checkForSourceTargetParam();
  }

  /**
   * Setup internal logging to the info frame.
   */
  /* private void setupLogging() {
   ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
   root.setLevel(Level.DEBUG);
   AppenderBase fa = new AppenderBase<ILoggingEvent>() {
   @Override
   protected void append(ILoggingEvent e) {
   if (e.getLevel().toInt() == Level.DEBUG_INTEGER) {
   logInfoMessage("(D) " + e.getFormattedMessage());
   } else if (e.getLevel().toInt() == Level.INFO_INTEGER) {
   logMessage("(I) " + e.getFormattedMessage());
   } else if (e.getLevel().toInt() == Level.WARN_INTEGER) {
   logMessage("(!!!) " + e.getFormattedMessage());
   } else if (e.getLevel().toInt() == Level.ERROR_INTEGER) {
   logErrorMessage(e.getFormattedMessage());
   }
   }
   };

   fa.setContext(root.getLoggerContext());
   fa.setName("ui.debug");
   root.addAppender(fa);
   fa.start();
   }*/
  public void handleException(String tname, Throwable thrown) {
    JOptionPane.showMessageDialog(this, StackTraceUtil.getStackTrace(thrown), "Uncaught Exception in Thread " + tname, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Add a new status listener
   *
   * @param pListener The new listener
   */
  public void addTransferStatusListener(ITransferStatusListener pListener) {
    transferStatusListeners.add(pListener);
  }

  /**
   * Remove a status listener
   *
   * @param pListener The listener to remove
   */
  public void removeTransferStatusListener(ITransferStatusListener pListener) {
    transferStatusListeners.remove(pListener);
  }

  /**
   * Check whether a source/target directory is already set on command line. If
   * so, skip file selector. In case of an upload, the source argument will be
   * checked, for a download the target argument is relevant.
   */
  private void checkForSourceTargetParam() {
    String theUrl = guiClient.getLocalUrl();
    if (theUrl != null) {
      try {
        File localDir = new File(new URI(theUrl));
        if (localDir.exists()) {
          handleItemDrop(localDir, null);
        }
      } catch (URISyntaxException ex) {
        LOGGER.error("No valid local url as command line parameter!", ex);
      }
    }
  }

  /**
   * Sets up the DnD features for the action area
   */
  private void setupDnD() {
    if (isUpload) {//add file drop only for upload
      DropTarget dt = new DropTarget(jActionLabel, this);
      if (dt.isActive()) {
        LOGGER.debug("DnD has been activated");
      }
    }

    addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        setLocation(e.getLocationOnScreen());
      }
    });

  }

  /**
   * Performs the extended UI setup (title, styles, GlassPane)
   */
  private void setupUI() {
    setTitle("TransferClient #" + guiClient.getTransferId());
    StyledDocument doc = (StyledDocument) jTextPane1.getDocument();
    // Create a style object and then set the style attributes
    Style defaultStyle = doc.addStyle("Default", null);
    StyleConstants.setItalic(defaultStyle, true);
    StyleConstants.setFontFamily(defaultStyle, "SansSerif");
    Style infoStyle = doc.addStyle("Info", null);
    StyleConstants.setItalic(infoStyle, true);
    StyleConstants.setFontFamily(infoStyle, "SansSerif");
    StyleConstants.setForeground(infoStyle, Color.LIGHT_GRAY);
    Style errorStyle = doc.addStyle("Error", null);
    StyleConstants.setFontFamily(errorStyle, "SansSerif");
    StyleConstants.setForeground(errorStyle, Color.RED);
    dateFormat = new SimpleDateFormat("HH:mm:ss");
    BufferedImage back = new BufferedImage(3, 3, BufferedImage.TRANSLUCENT);
    Graphics g = back.getGraphics();
    g.setColor(new Color(200, 200, 200, 120));
    g.fillRect(0, 0, back.getWidth(), back.getHeight());
    g.setColor(new Color(200, 200, 200));
    g.drawLine(0, 0, 3, 3);
    g.dispose();
    TexturePaint paint = new TexturePaint(back, new Rectangle2D.Double(0, 0, back.getWidth(), back.getHeight()));
    jXPanel1.setBackgroundPainter(new MattePainter(paint));
    jTransferLogFrame.pack();
    jProgressBar1.setIndeterminate(false);

    /*  if (TransferHelper.canRestoreTransfer(guiClient.getTransferObjectId())) {
     refreshState(TRANSFER_STATE.READY4RESUME);
     }*/
  }

  /**
   * A drop operation onto the action field was finished. Handle it and update
   * the status.
   *
   * @param pFile The directory dropped to the action field or 'null' if nothing
   * valid was dropped
   * @param pMessage An error message if nothing valid was dropped or 'null' if
   * pFile is valid
   */
  private void handleItemDrop(File pFile, String pMessage) {
    if (pFile != null) {
      try {
        localURL = pFile.toURI().toURL();
        refreshState(TRANSFER_STATE.READY4TRANSFER);
      } catch (MalformedURLException mue) {
        LOGGER.error("Failed to obtain local URL from dropped file", mue);
        JOptionPane.showMessageDialog(this, "Failed to obtain local URL from dropped file.", "Drop failed", JOptionPane.WARNING_MESSAGE);
        refreshState(TRANSFER_STATE.NOT_INITIALIZED);
      }
    } else {
      JOptionPane.showMessageDialog(this, pMessage, "Drop failed", JOptionPane.WARNING_MESSAGE);
      refreshState(TRANSFER_STATE.NOT_INITIALIZED);
    }
  }

  /**
   * Refresh the state of the transfer client. The state controlls icon and
   * tooltip of the action field.
   *
   * @param pNewState The new state
   */
  private void refreshState(TRANSFER_STATE pNewState) {
    currentState = pNewState;
    jActionLabel.setIcon(new ImageIcon(currentState.getImageResource()));
    switch (currentState) {
      case NOT_INITIALIZED:
        if (isUpload) {
          jActionLabel.setToolTipText("<html>Click to select a folder to upload or drag a folder into this box.</html>");
        } else {
          jActionLabel.setToolTipText("<html>Click to select a folder to store downloaded data.</html>");
        }
        break;
      case READY4RESUME:
        jActionLabel.setToolTipText("<html>There was found a checkpoint for the transfer associated with this transfer client.<br/>"
                + "Click to resume the transfer.</html>");
        break;
      case READY4TRANSFER:
        jActionLabel.setToolTipText("<html>The selected data can be transferred.<br/>Click to start the transfer.</html>");
        break;
      case FAILED:
        jActionLabel.setToolTipText("<html>The transfer has failed.<br/>Please see the logfile for details.</html>");
        break;
      case FINISHED:
        jActionLabel.setToolTipText("<html>The transfer has finished successfully.<br/>"
                + "You can now close the transfer client.</html>");
        break;
      case RUNNING:
        jActionLabel.setToolTipText("<html>The transfer is now running.<br/>"
                + "Please wait...</html>");
        break;
    }
  }

  //<editor-fold defaultstate="collapsed" desc="Status logging">   
  /**
   * Appends a default message to the transfer log
   *
   * @param pText The message
   */
  private void logMessage(String pText) {
    logMessage(pText, "Default");
  }

  /**
   * Appends an information message to the transfer log
   *
   * @param pText The information message
   */
  private void logInfoMessage(String pText) {
    logMessage(pText, "Info");
  }

  /**
   * Appends an error message to the transfer log
   *
   * @param pText The error message
   */
  private void logErrorMessage(String pText) {
    logMessage(pText, "Error");
  }

  /**
   * Add a message to the transfer log using the provided style
   *
   * @param pMessage The message text
   * @param pStyleName The name of the style (Default, Info or Error)
   */
  private void logMessage(String pMessage, String pStyleName) {
    try {
      StyledDocument doc = jTextPane1.getStyledDocument();
      doc.insertString(doc.getLength(), dateFormat.format(new Date(System.currentTimeMillis())) + " - " + pMessage + "\n", doc.getStyle(pStyleName));
      scrollLog();
      //limit log output to 100KB
      if (doc.getLength() > 100 * 1024) {
        doc.remove(0, doc.getLength() - 100 * 1024);
      }
    } catch (BadLocationException ble) {
      LOGGER.debug("Failed to add message '" + pMessage + "' with style '" + pStyleName + "' to transfer log", ble);
    }
  }

  /**
   * Scrolls the text pane to the end of its document if autoscroll is enabled
   */
  private void scrollLog() {
    if (jAutoscrollBox.isSelected()) {//only scroll if autoscroll is enabled
      Point point = new Point(0, (int) (jTextPane1.getSize().getHeight()));
      JViewport vp = jScrollPane1.getViewport();
      if ((vp == null) || (point == null)) {//invalide viewport or location
        return;
      }
      vp.setViewPosition(point);
    }
  }
  //</editor-fold>

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    jTransferLogFrame = new javax.swing.JFrame();
    jScrollPane1 = new javax.swing.JScrollPane();
    jTextPane1 = new javax.swing.JTextPane();
    jButton1 = new javax.swing.JButton();
    jAutoscrollBox = new javax.swing.JCheckBox();
    jXPanel1 = new org.jdesktop.swingx.JXPanel();
    jActionLabel = new javax.swing.JLabel();
    jProgressBar1 = new javax.swing.JProgressBar();
    jMenuBar1 = new javax.swing.JMenuBar();
    jFileMenu = new javax.swing.JMenu();
    jExitMenuItem = new javax.swing.JMenuItem();
    jViewMenu = new javax.swing.JMenu();
    jLogFrameMenuItem = new javax.swing.JMenuItem();

    jTransferLogFrame.setTitle("Transfer Log");
    jTransferLogFrame.setMinimumSize(new java.awt.Dimension(400, 400));
    jTransferLogFrame.getContentPane().setLayout(new java.awt.GridBagLayout());

    jScrollPane1.setViewportView(jTextPane1);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    jTransferLogFrame.getContentPane().add(jScrollPane1, gridBagConstraints);

    jButton1.setText("Hide");
    jButton1.setToolTipText("Close the transfer log");
    jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        fireHideTransferLogEvent(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    jTransferLogFrame.getContentPane().add(jButton1, gridBagConstraints);

    jAutoscrollBox.setSelected(true);
    jAutoscrollBox.setText("Autoscroll");
    jAutoscrollBox.setToolTipText("Turns autoscroll feature on/off");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
    jTransferLogFrame.getContentPane().add(jAutoscrollBox, gridBagConstraints);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("Data Transfer Client");
    setBackground(new java.awt.Color(153, 153, 153));
    setMinimumSize(new java.awt.Dimension(200, 200));
    addWindowListener(new java.awt.event.WindowAdapter() {
      public void windowClosing(java.awt.event.WindowEvent evt) {
        fireClosingEvent(evt);
      }
    });
    getContentPane().setLayout(new java.awt.GridBagLayout());

    jXPanel1.setLayout(new java.awt.BorderLayout());

    jActionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jActionLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/48x48/hand_point.png"))); // NOI18N
    jActionLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
    jActionLabel.setMaximumSize(new java.awt.Dimension(70, 70));
    jActionLabel.setMinimumSize(new java.awt.Dimension(70, 70));
    jActionLabel.setPreferredSize(new java.awt.Dimension(70, 70));
    jActionLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
    jActionLabel.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        fireActionPaneClickedEvent(evt);
      }
    });
    jXPanel1.add(jActionLabel, java.awt.BorderLayout.CENTER);
    jXPanel1.add(jProgressBar1, java.awt.BorderLayout.PAGE_END);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 3;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    getContentPane().add(jXPanel1, gridBagConstraints);

    jFileMenu.setText("File");

    jExitMenuItem.setText("Exit");
    jExitMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        fireExitEvent(evt);
      }
    });
    jFileMenu.add(jExitMenuItem);

    jMenuBar1.add(jFileMenu);

    jViewMenu.setText("View");

    jLogFrameMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/16x16/information.png"))); // NOI18N
    jLogFrameMenuItem.setText("Transfer Log");
    jLogFrameMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseReleased(java.awt.event.MouseEvent evt) {
        fireShowTransferLogEvent(evt);
      }
    });
    jViewMenu.add(jLogFrameMenuItem);

    jMenuBar1.add(jViewMenu);

    setJMenuBar(jMenuBar1);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  /**
   * The action pane was clicked. The action depends on the current status of
   * the transfer client
   *
   * @param evt The MouseEvent
   */
private void fireActionPaneClickedEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireActionPaneClickedEvent
  LOGGER.debug("Clicked action pane {}", evt.getComponent());
  switch (currentState) {
    case READY4TRANSFER:
    case READY4RESUME:
      startTransfer();
      break;
    case NOT_INITIALIZED:
      showFolderSelection();
      break;
    case FINISHED:
      closeTransferClient(false);
      break;
    default:
    //do nothing
    }
}//GEN-LAST:event_fireActionPaneClickedEvent

  /**
   * Hide the transfer log
   *
   * @param evt The MouseEvent
   */
    private void fireHideTransferLogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireHideTransferLogEvent
      LOGGER.debug("Closing transfer log by button {}", evt.getComponent());
      jTransferLogFrame.setVisible(false);
    }//GEN-LAST:event_fireHideTransferLogEvent

  /**
   * Close the client
   *
   * @param evt The WindowEvent
   */
    private void fireClosingEvent(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_fireClosingEvent
      exit();
    }//GEN-LAST:event_fireClosingEvent

    private void fireShowTransferLogEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireShowTransferLogEvent
      LOGGER.debug("Enabled transfer log using button {}", evt.getComponent());
      jTransferLogFrame.setVisible(true);
    }//GEN-LAST:event_fireShowTransferLogEvent

    private void fireExitEvent(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fireExitEvent
      exit();
    }//GEN-LAST:event_fireExitEvent

  private void exit() {
    LOGGER.debug("Closing application");
    closeTransferClient(true);
  }

  /**
   * Start the transfer using the current settings
   */
  private void startTransfer() {
    //run
    try {
      guiClient.setLocalUrl(localURL.toString());
      guiClient.addTransferStatusListener(this);
      guiClient.addTransferTaskListener(this);

      if (guiClient instanceof GUIDownloadClient) {
        guiClient.performDownload();
      } else if (guiClient instanceof GUIUploadClient) {
        guiClient.performUpload();
      }
    } catch (TransferClientInstatiationException tcie) {
      LOGGER.error("Failed to start transfer client", tcie);
      logErrorMessage("Failed to start transfer client. See log for more details.");
      refreshState(TRANSFER_STATE.FAILED);
      return;
    } catch (Throwable t) {
      LOGGER.error("Unexpected exception during start of transfer", t);
      logErrorMessage("Failed to start transfer client. See log for more details.");
      refreshState(TRANSFER_STATE.FAILED);
      return;
    }
    refreshState(TRANSFER_STATE.RUNNING);
    jProgressBar1.setIndeterminate(true);
  }

  /**
   * Show the folder chooser dialog to select local upload source/ download
   * target folder manually
   */
  private void showFolderSelection() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Please select a directory...");
    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int result = JFileChooser.CANCEL_OPTION;
    if (isUpload) {
      result = chooser.showOpenDialog(null);
    } else {
      result = chooser.showSaveDialog(null);
    }

    if (result == JFileChooser.APPROVE_OPTION) {
      handleItemDrop(chooser.getSelectedFile(), null);
    }
  }

  /**
   * Close the transfer client
   *
   * @param pConfirm Show a confirmation message before the client is terminated
   */
  private void closeTransferClient(boolean pConfirm) {
    if (pConfirm) {
      if (currentState.equals(TRANSFER_STATE.RUNNING)) {//show special warning as the transfer will be aborted
        if (JOptionPane.showConfirmDialog(this, "Transfer is running. Do you really want to quit?", "Transfer Running", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
          //cancel or closed
          return;
        }
      } else {
        //check if status is "FINISHED" (skip confirmation) or if the user really wants to exit
        if (!currentState.equals(TRANSFER_STATE.FINISHED) && JOptionPane.showConfirmDialog(this, "Do you really want to quit?", "Quit", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) {
          //cancel or abort
          return;
        }
      }
    }
    dispose();
  }

  @Override
  public final void dispose() {
    if (guiClient.getTransferClient() != null) {
      guiClient.getTransferClient().setCanceled(true);
    }
    setVisible(false);
    System.exit(0);
  }

  //<editor-fold defaultstate="collapsed" desc=" DnD listener impl ">
  @Override
  public final void dragEnter(DropTargetDragEvent dtde) {
  }

  @Override
  public final void dragOver(DropTargetDragEvent dtde) {
    if (!dtde.getCurrentDataFlavorsAsList().contains(DataFlavor.javaFileListFlavor)) {
      dtde.rejectDrag();
    }
  }

  @Override
  public final void dropActionChanged(DropTargetDragEvent dtde) {
  }

  @Override
  public final void dragExit(DropTargetEvent dte) {
  }

  @Override
  public final void drop(DropTargetDropEvent dtde) {
    if (!dtde.getCurrentDataFlavorsAsList().contains(DataFlavor.javaFileListFlavor)) {
      dtde.rejectDrop();
    } else {
      dtde.acceptDrop(DnDConstants.ACTION_COPY);
      try {
        List<File> fileList = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

        if (fileList.size() != 1 || !fileList.get(0).isDirectory()) {
          handleItemDrop(null, "Invalid data dropped.\nPlease drop only a single directory containing all data.");
        } else {
          handleItemDrop(fileList.get(0), null);
        }
      } catch (UnsupportedFlavorException ufe) {
        LOGGER.error("Failed to receive dropped data", ufe);
        handleItemDrop(null, "Failed to receive dropped data.\nFor details see logfile.");
      } catch (IOException ioe) {
        LOGGER.error("Failed to receive dropped data", ioe);
        handleItemDrop(null, "Failed to receive dropped data.\nFor details see logfile.");
      } catch (ClassCastException cce) {
        LOGGER.error("Invalid drop data", cce);
        handleItemDrop(null, "Failed to receive dropped data.\nFor details see logfile.");
      }
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc=" ITransferTaskListener impl">
  @Override
  public final void transferStarted(TransferTask pTask) {
    logInfoMessage("Transferring " + pTask.getSourceFile() + " to " + pTask.getTargetFile());
    AbstractTransferClient.TransferInfo info = guiClient.getTransferClient().getTransferInfo();
    jActionLabel.setText("<html><font size='-2'>Finished: " + info.getFinishedTaskCount() + "<br/>Running: " + info.getRunningTaskCount() + "<br/>All: " + info.getTaskCount() + "</font></html>");
    jProgressBar1.setIndeterminate(false);
    jProgressBar1.setMaximum(info.getTaskCount());
    jProgressBar1.setValue(info.getFinishedTaskCount());
  }

  @Override
  public final void transferFinished(TransferTask pTask) {
    logMessage("Successfully transferred " + pTask.getSourceFile());
    AbstractTransferClient.TransferInfo info = guiClient.getTransferClient().getTransferInfo();
    jActionLabel.setText("<html><font size='-2'>Finished: " + info.getFinishedTaskCount() + "<br/>Running: " + info.getRunningTaskCount() + "<br/>All: " + info.getTaskCount() + "</font></html>");
    jProgressBar1.setIndeterminate(false);
    jProgressBar1.setMaximum(info.getTaskCount());
    jProgressBar1.setValue(info.getFinishedTaskCount());
  }

  @Override
  public final void transferFailed(TransferTask pTask) {
    logErrorMessage("Failed to transfer " + pTask.getSourceFile() + " to " + pTask.getTargetFile());
    jProgressBar1.setIndeterminate(false);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc=" ITransferStatusListener impl">
  @Override
  public final void fireStatusChangedEvent(TRANSFER_STATUS pOldStatus, TRANSFER_STATUS pNewStatus) {
    switch (pNewStatus) {
      case PREPARING:
        jActionLabel.setText("Preparing...");
        logInfoMessage("Preparing transfer...");
        break;
      case RUNNING:
        jActionLabel.setText("Transferring...");
        logInfoMessage("Transfer running...");
        break;
      case TRANSFERRING:
        jActionLabel.setText("Transferring...");
        logInfoMessage("Starting data transfer...");
        break;
      case CLEANUP:
        jActionLabel.setText("Cleanup...");
        logInfoMessage("Cleaning up...");
        break;
      case SUCCEEDED:
        jActionLabel.setText("Finished");
        logMessage("Transfer finished successfully!");
        refreshState(TRANSFER_STATE.FINISHED);
        break;
      case INTERNAL_PREPARATION_FAILED:
      case EXTERNAL_PREPARATION_FAILED:
      case PRE_PROCESSING_FAILED:
      case TRANSFER_FAILED:
      case TRANSFER_LOCKED:
      case FAILED:
        jActionLabel.setText("Failed");
        logErrorMessage("Transfer failed! See log for more details.");
        refreshState(TRANSFER_STATE.FAILED);
        break;
      default:
        //do nothing
        LOGGER.warn("No handling for status {}", pNewStatus);
    }

    for (ITransferStatusListener listener : transferStatusListeners.toArray(new ITransferStatusListener[transferStatusListeners.size()])) {
      listener.fireStatusChangedEvent(pOldStatus, pNewStatus);
    }

    //publish status to upload client only
    if (guiClient instanceof GUIUploadClient) {
      if (!guiClient.publishStatusChange(pNewStatus)) {
        logErrorMessage("Failed to send new status " + pNewStatus.toString() + " to server.");
      } else {
        logInfoMessage("Successfully updated new status at server.");
      }
    }
  }

  @Override
  public final void fireTransferAliveEvent() {
    for (ITransferStatusListener listener : transferStatusListeners.toArray(new ITransferStatusListener[transferStatusListeners.size()])) {
      listener.fireTransferAliveEvent();
    }
    //publish status to upload client only
    if (guiClient instanceof GUIUploadClient) {
      if (!guiClient.sendHeartbeat()) {
        logErrorMessage("Failed to send Heartbeat to server.");
      } else {
        logInfoMessage("Heartbeat successfully sent.");
      }

    }
  }
  //</editor-fold>
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel jActionLabel;
  private javax.swing.JCheckBox jAutoscrollBox;
  private javax.swing.JButton jButton1;
  private javax.swing.JMenuItem jExitMenuItem;
  private javax.swing.JMenu jFileMenu;
  private javax.swing.JMenuItem jLogFrameMenuItem;
  private javax.swing.JMenuBar jMenuBar1;
  private javax.swing.JProgressBar jProgressBar1;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JTextPane jTextPane1;
  private javax.swing.JFrame jTransferLogFrame;
  private javax.swing.JMenu jViewMenu;
  private org.jdesktop.swingx.JXPanel jXPanel1;
  // End of variables declaration//GEN-END:variables
}
