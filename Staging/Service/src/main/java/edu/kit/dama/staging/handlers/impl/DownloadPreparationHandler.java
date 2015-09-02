/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
package edu.kit.dama.staging.handlers.impl;

import edu.kit.dama.staging.handlers.AbstractTransferPreparationHandler;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.services.impl.download.DownloadInformationPersistenceImpl;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.util.MailNotificationHelper;
import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import edu.kit.dama.staging.services.impl.StagingService;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.staging.util.StagingConfigurationManager;
import edu.kit.dama.staging.util.TransferClientPropertiesUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class DownloadPreparationHandler extends AbstractTransferPreparationHandler<DOWNLOAD_STATUS, DownloadInformation> {

  /**
   * The logger instance
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DownloadPreparationHandler.class);
  private IFileTree treeToDownload = null;
  public final static String DATA_FILENAME = "preparationContent.xml";

  /**
   * Preparation handler for data ingest
   *
   * @param pPersistence The persistence implementation used to access the
   * persistence backend
   * @param pEntity The ingest information entity of the ingest to prepare
   * @param pTreeToDownload The file tree to download.
   */
  public DownloadPreparationHandler(DownloadInformationPersistenceImpl pPersistence, DownloadInformation pEntity, IFileTree pTreeToDownload) {
    super(pPersistence, pEntity);
    treeToDownload = pTreeToDownload;
  }

  @Override
  public final void setup(TransferClientProperties pProperties) {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Nothing to do here for the moment. Later we may add additional properties used by to following phases.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
  }

  @Override
  public final void prepareEnvironment(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    LOGGER.debug("Preparing data download tree");
    LOGGER.debug(" - Obtaining AccessPoint");
    AbstractStagingAccessPoint accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(pProperties.getStagingAccessPointId());
    LOGGER.debug(" - Getting access URL");
    URL accessUrl = accessPoint.getAccessUrl(getTransferInformation(), pSecurityContext);
    LOGGER.debug(" - Access URL: {}. Getting local staging folder.", accessUrl);
    File localStagingFolder = accessPoint.getLocalPathForUrl(accessUrl, pSecurityContext);
    LOGGER.debug(" - Local staging folder: {}", localStagingFolder);
    File localSettingsFolder = AbstractStagingAccessPoint.getSettingsFolder(localStagingFolder);
    LOGGER.debug(" - Local settings folder: {}. Obtaining file tree destination file.", localSettingsFolder);
    String treeFile = FilenameUtils.concat(localSettingsFolder.getPath(), DATA_FILENAME);

    if (new File(treeFile).exists() && !StagingService.getSingleton().isTransferDeleted(getTransferInformation())) {
      throw new TransferPreparationException("Download for this object is still in preparation. Please try again later");
    }
    LOGGER.debug("Writing tree data to {}", treeFile);
    DataOrganizationUtils.writeTreeToFile(treeToDownload, new File(treeFile));

    if (pProperties.isSendMailNotification()) {
      LOGGER.debug("Writing mail notification information to file");
      MailNotificationHelper.storeProperties(localSettingsFolder, pProperties.getReceiverMail());
    }
    LOGGER.debug("Storing access point properties.");
    Properties props = TransferClientPropertiesUtils.propertiesToProperties(pProperties);
    FileOutputStream fout = null;
    try {
      fout = new FileOutputStream(new File(localSettingsFolder, pProperties.getStagingAccessPointId() + ".properties"));
      props.store(fout, null);
    } catch (IOException ioe) {
      LOGGER.error("Failed to store access point properies", ioe);
    } finally {
      if (fout != null) {
        try {
          fout.close();
        } catch (IOException ex) {
        }
      }
    }

    LOGGER.debug("Environment preparation successfully finished");
  }

  @Override
  public final void prepareClientAccess(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Nothing to do here for the moment. Later we may add additional properties used by to following phases.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
  }

  @Override
  public final void publishTransferInformation(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) {
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Nothing to do here for the moment. Later we may add additional properties used by to following phases.
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
  }
}
