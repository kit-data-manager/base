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
package edu.kit.dama.staging.services.processor.impl;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.entities.TransferClientProperties;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.staging.services.impl.download.DownloadInformationServiceLocal;
import edu.kit.dama.staging.util.StagingConfigurationManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This processor transforms an ingest directly into a download after the ingest
 * is finished. It should be configured as
 * {@link StagingProcessor.PROCESSOR_TYPE#POST_ARCHIVING} as all ingested data
 * will be <b>moved</b> from the ingest location to the download location of a
 * provided AccessPoint.
 *
 * @author mf6319
 */
public class IngestToDownloadProcessor extends AbstractStagingProcessor {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(IngestToDownloadProcessor.class);
  /**
   * The access point id configuration property
   */
  private static final String ACCESS_POINT_ID_PROPERTY = "accessPointId";
  /**
   * The changeGroup configuration property.
   */
  private static final String DO_CHGRP_PROPERTY = "changeGroup";

  private AbstractStagingAccessPoint accessPoint;
  private boolean doChgrp = false;

  /**
   * Default constructor.
   *
   * @param pUniqueIdentifier The unique identifier of this processor.
   */
  public IngestToDownloadProcessor(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
  }

  @Override
  public String getName() {
    return "IngestToDownloadProcessor";
  }

  @Override
  public void performPreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    //no pre-transfer processing supported
  }

  @Override
  public void finalizePreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    //no pre-transfer processing supported
  }

  @Override
  public void performPostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    LOGGER.debug("Performing IngestToDownloadProcessor");
    IngestInformation ingest = (IngestInformation) pContainer.getTransferInformation();

    LOGGER.debug(" - Creating download entry");
    IAuthorizationContext ctx = new AuthorizationContext(new UserId(ingest.getOwnerId()), new GroupId(ingest.getGroupId()), Role.MEMBER);
    TransferClientProperties props = new TransferClientProperties();
    props.setStagingAccessPointId(accessPoint.getConfiguration().getUniqueIdentifier());
    DownloadInformation download;
    try {
      download = DownloadInformationServiceLocal.getSingleton().scheduleDownload(new DigitalObjectId(ingest.getDigitalObjectId()), props, ctx);
      LOGGER.debug(" - Download entry created. Setting status to DOWNLOAD_STATUS.PREPARING.");
      DownloadInformationServiceLocal.getSingleton().updateStatus(download.getId(), DOWNLOAD_STATUS.PREPARING.getId(), null, ctx);
      LOGGER.debug(" - Status successfully set. Starting moving data.");
    } catch (TransferPreparationException ex) {
      throw new StagingProcessorException("Failed to create download for ingest with object id " + ingest.getDigitalObjectId(), ex);
    }

    LOGGER.debug(" - Obtaining data folder URL of ingest.");
    URL data = ingest.getDataFolderURL();

    LOGGER.debug(" - Ingest data folder URL is: {}", data);

    AbstractStagingAccessPoint ingestAP = StagingConfigurationManager.getSingleton().getAccessPointById(ingest.getAccessPointId());
    File baseFile = ingestAP.getLocalPathForUrl(data, ctx);
    LOGGER.debug(" - Ingest data path is: {}", baseFile);

    LOGGER.debug(" - Obtaining data folder URL of download.");
    File stagingPath = accessPoint.getLocalPathForUrl(download.getDataFolderURL(), ctx);
    LOGGER.debug(" - Download data folder URL is: {}", stagingPath);

    try {
      LOGGER.debug("Moving {} to {}", baseFile, stagingPath);
      for (File f : baseFile.listFiles()) {
        LOGGER.debug(" - Moving file {}", f);
        if (f.isFile()) {
          FileUtils.moveFileToDirectory(f, stagingPath, false);
        } else if (f.isDirectory()) {
          FileUtils.moveDirectoryToDirectory(f, stagingPath, false);
        }
      }
      LOGGER.debug("All files successfully moved.");
    } catch (IOException ex) {
      throw new StagingProcessorException("Failed to move " + baseFile + " to " + stagingPath, ex);
    }

    if (doChgrp) {
      LOGGER.debug("GroupChange flag ist TRUE, changing ownership of data folder at {} to group {}", stagingPath, ingest.getGroupId());
      if (edu.kit.dama.util.SystemUtils.chgrpFolder(stagingPath, ingest.getGroupId())) {
        LOGGER.debug("Group ownership successfully changed.");
      } else {
        throw new StagingProcessorException("Failed to change group ownership for data folder " + stagingPath + " to groupId {} " + ingest.getGroupId());
      }
    }

    LOGGER.debug("Successfully moved data to download path. Setting download to be ready.");
    DownloadInformationServiceLocal.getSingleton().updateStatus(download.getId(), DOWNLOAD_STATUS.DOWNLOAD_READY.getId(), null, ctx);
  }

  @Override
  public void finalizePostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    //no post transfer processing
  }

  @Override
  public String[] getInternalPropertyKeys() {
    return new String[]{ACCESS_POINT_ID_PROPERTY, DO_CHGRP_PROPERTY};
  }

  @Override
  public String getInternalPropertyDescription(String pKey) {
    if (null != pKey) {
      switch (pKey) {
        case ACCESS_POINT_ID_PROPERTY:
          return "The unique identifier of the StagingAccessPoint used to provide the of the ingest data for download.";
        case DO_CHGRP_PROPERTY:
          return "If TRUE, all download data will be owned by the group the ingest belongs to on filesystem level. This option is only available on Unix platforms. "
                  + "In order to be able to assign a proper ownership, the repository GroupId should be reflected on the system level, either as a numeric or alphanumeric GID. (default: FALSE)";
      }
    }
    return null;
  }

  @Override
  public String[] getUserPropertyKeys() {
    return new String[]{};
  }

  @Override
  public String getUserPropertyDescription(String pKey) {
    return null;
  }

  @Override
  public void validateProperties(Properties pProperties) throws PropertyValidationException {
    if (pProperties == null) {
      throw new IllegalArgumentException("Argument pProperties must not be null");
    }
    String apId = pProperties.getProperty(ACCESS_POINT_ID_PROPERTY);
    if (apId == null) {
      throw new PropertyValidationException("AccessPointId is not provided. Unable to continue.");
    }

    AbstractStagingAccessPoint ap = StagingConfigurationManager.getSingleton().getAccessPointById(apId);
    if (ap == null) {
      throw new PropertyValidationException("No StagingAccessPoint found for AccessPointId " + apId + ".");
    }

    if (Boolean.parseBoolean(pProperties.getProperty(DO_CHGRP_PROPERTY))) {
      if (!SystemUtils.IS_OS_UNIX) {
        throw new PropertyValidationException("Option 'DO_CHGRP_PROPERTY' is only available in Unix environments.");
      }
    }
  }

  @Override
  public void configure(Properties pProperties) throws PropertyValidationException, ConfigurationException {
    String apId = pProperties.getProperty(ACCESS_POINT_ID_PROPERTY);
    LOGGER.debug("Reading AccessPointConfiguration for AccessPointId {}", apId);
    accessPoint = StagingConfigurationManager.getSingleton().getAccessPointById(apId);
    if (accessPoint == null) {
      throw new ConfigurationException("No StagingAccessPoint found for AccessPointId " + apId + ".");
    } else {
      LOGGER.debug("AccessPointConfiguration successfully read.");
    }
    LOGGER.debug("Reading doChgrp flag.");
    doChgrp = Boolean.parseBoolean(pProperties.getProperty(DO_CHGRP_PROPERTY));
  }
}
