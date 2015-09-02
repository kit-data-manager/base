/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.staging.ap;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.commons.types.IConfigurable;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.SystemUtils;
import edu.kit.tools.url.URLCreator;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for all AccessPoints. Each AccessPoint defines a cache
 * used to store data for ingest and download and is configured using a
 * persistent {@link SStagingAccessPointConfiguration. The cache must be
 * accessible locally and remotely. For local access,
 * {@link StagingAccessPointConfiguration#getLocalBasePath()} is used, whereas
 * for remote access {@link StagingAccessPointConfiguration#getRemoteBaseUrl()}
 * is responsible.<br/>The main responsibilities of an AccessPoint are:
 * <ul>
 * <li>Provide remote data access URLs depending on a transfer information
 * entity and a defined security context.</li>
 * <li>Provide a mappings between remote URLs and local paths and vice versa, as
 * long as they are children of remoteBaseUrl and localBasePath.</li>
 * <li>Prepare the local folder prior to the actual data staging (e.g. including
 * changes of ownership and permissions).</li>
 * <li>Prepare the cleanup of a local transfer folder (e.g. including changes of
 * ownership and permissions).</li>
 * </ul>
 *
 * As especially the mapping process can be very complex, AccessPoints are
 * allowed to have internal properties defined globally as well as user
 * properties defined per transfer.
 *
 * @author mf6319
 */
public abstract class AbstractStagingAccessPoint implements IConfigurable {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStagingAccessPoint.class);

  /**
   * The AccessPoint configuration used to configure this staging AccessPoint.
   */
  private StagingAccessPointConfiguration configuration;

  /**
   * Default constructor.
   */
  public AbstractStagingAccessPoint() {
  }

  /**
   * Default constructor.
   *
   * @param pConfiguration Configuration which contains all properties of this
   * AccessPoint.
   */
  public AbstractStagingAccessPoint(StagingAccessPointConfiguration pConfiguration) {
    this();
    if (pConfiguration == null) {
      throw new IllegalArgumentException("Argument pConfiguration must not be null");
    }
    configuration = pConfiguration;
  }

  /**
   * Internal setup method called at the end of setup(boolean) in order to
   * perform AccessPoint-specific setup.
   *
   * @param pCheckOnly Only perform checks without changing anything in the
   * underlaying system, e.g. creating/deleting folders.
   *
   * @throws ConfigurationException If setup fails and cannot be continued.
   */
  protected void setupInternal(boolean pCheckOnly) throws ConfigurationException {
    try {
      if (pCheckOnly) {
        validateProperties(configuration.getPropertiesAsObject());
      } else {
        configure(configuration.getPropertiesAsObject());
      }
    } catch (IOException | PropertyValidationException ex) {
      throw new ConfigurationException("Failed to perform internal setup", ex);
    }
  }

  /**
   * Setup this AccessPoint. Setup is used during staging service startup to
   * check, whether this method can be used or not, e.g. whether the local path
   * is accessible and if all provided settings are valid. Furthermore, this
   * setup is called while instantiating any AccessPoint to perform availability
   * checks and to check the configuration.
   *
   * @param pCheckOnly Only perform checks without changing anything in the
   * underlaying system, e.g. creating/deleting folders.
   *
   * @throws ConfigurationException If setup fails and cannot be continued.
   */
  public final void setup(boolean pCheckOnly) throws ConfigurationException {
    //ensure that local base path and remote base url are in a proper format before starting the actual setup.
    String localBasePath = getConfiguration().getLocalBasePath();

    if (localBasePath == null) {
      throw new ConfigurationException("Mandatory property 'LOCAL BASE PATH' is not defined.");
    }

    if (!localBasePath.endsWith("/")) {
      //fix base path to fulfill expectations
      LOGGER.debug("Local base path for AccessPoint {} does not end with slash...I'll fix this.", getConfiguration().getUniqueIdentifier());
      localBasePath += "/";
      getConfiguration().setLocalBasePath(localBasePath);
    }
    URL remoteBaseUrl;

    try {
      remoteBaseUrl = new URL(getConfiguration().getRemoteBaseUrl());
    } catch (MalformedURLException ex) {
      throw new ConfigurationException("Remote base URL " + getConfiguration().getRemoteBaseUrl() + " seems to be no valid URL", ex);
    }
    String baseUrl = URLCreator.cleanUrl(remoteBaseUrl).toString();
    if (!baseUrl.endsWith("/")) {
      //fix base url to fulfill expectations
      LOGGER.debug("Remote base URL for AccessPoint {} does not end with slash...I'll fix this.", getConfiguration().getUniqueIdentifier());
      baseUrl += "/";
      getConfiguration().setRemoteBaseUrl(baseUrl);
    }

    LOGGER.debug("Performing internal setup of AccessPoint with id {}", getConfiguration().getUniqueIdentifier());
    setupInternal(pCheckOnly);
  }

  /**
   * Perform the actual preparation for the provided transfer information entity
   * using a map of properties and a security context.In a first preparation
   * phase,
   * {@link #prepareCache(edu.kit.dama.rest.staging.interfaces.ITransferInformation, edu.kit.authorization.entities.IAuthorizationContext)}
   * is used to perform the basic, AccessPoint-specific preparation of the base
   * caching location. Afterwards, the common KIT Data Manager specific staging
   * structure is created by this abstract class.
   *
   * This method may throw a TransferPreparationException or a
   * PropertyNotFoundException if anything goes wrong during preparation.
   *
   * @param pTransferInformation The transfer information entity to prepare.
   * @param pSecurityContext Security context used to authorize access and to
   * obtain user/group information.
   *
   * @throws TransferPreparationException If the preparation fails.
   */
  public final void prepare(ITransferInformation pTransferInformation, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    prepareCache(pTransferInformation, pSecurityContext);
    URL accessUrl = getAccessUrl(pTransferInformation, pSecurityContext);
    File localPath = getLocalPathForUrl(accessUrl, pSecurityContext);
    File localDataPath;
    File localSettingsPath;
    File localGeneratedPath;
    try {
      localDataPath = getLocalPathForUrl(getDataFolderUrl(accessUrl), pSecurityContext);
      localSettingsPath = getLocalPathForUrl(getSettingsFolderUrl(accessUrl), pSecurityContext);
      localGeneratedPath = getLocalPathForUrl(getGeneratedFolderUrl(accessUrl), pSecurityContext);
    } catch (MalformedURLException ex) {
      throw new TransferPreparationException("Failed to obtain local paths for data, settings and generated data", ex);
    }

    LOGGER.debug("Try creating transfer folder structure at {}", localDataPath);
    if (!createFolder(localPath) || !createFolder(localDataPath) || !createFolder(localSettingsPath) || !createFolder(localGeneratedPath)) {
      throw new TransferPreparationException("Failed to create transfer folder structure at " + localPath);
    }
  }

  /**
   * Create pFolder after checking that it is accessible. If the pFolder
   * exists/could be created TRUE is returned, FALSE otherwise.
   *
   * @param pFolder The folder to create.
   *
   * @return TRUE if pFolder was created/exists and is accessible.
   */
  private boolean createFolder(File pFolder) {
    boolean result = true;
    if (!edu.kit.dama.util.FileUtils.isAccessible(pFolder) || (!pFolder.exists() && !pFolder.mkdir())) {
      LOGGER.error("- Failed to create folder {}", pFolder);
      result = false;
    } else {
      LOGGER.debug("Folder {} successfully created. Changing permissions to 2770.", pFolder);
      if (SystemUtils.openFolder(pFolder)) {
        LOGGER.debug("Folder permissions successfully changed.");
      } else {
        LOGGER.info("Failed to change folder permissions. I'll ignore this.");
      }
    }
    return result;
  }

  /**
   * Prepare the cleanup the provided transfer. During cleanup preparation the
   * AccessPoint first takes care, that no file is write protected and
   * everything can be removed automatically. For this purpose, {@link #prepareCleanupInternal(edu.kit.dama.rest.staging.interfaces.ITransferInformation)
   * } is called. Afterwards, to mark the transfer folder as "deleteable", a
   * file named StagingService.DELETED_FILENAME (.deleted) is created inside the
   * folder of the transfer.
   *
   * @param pTransferInformation The transfer to cleanup.
   * @param pSecurityContext Security context used to authorize access and to
   * obtain user/group information.
   *
   * @return TRUE if the cleanup was succeeded (.deleted was created) or no
   * files were left, FALSE if .deleted could not be created and there are files
   * left.
   */
  public final boolean prepareCleanup(ITransferInformation pTransferInformation, IAuthorizationContext pSecurityContext) {
    boolean result;
    URL accessUrl = getAccessUrl(pTransferInformation, pSecurityContext);
    File localPath = getLocalPathForUrl(accessUrl, pSecurityContext);
    if (!localPath.exists()) {
      //nothing exists...looks already "clean" to me.
      result = true;
    } else {
      LOGGER.debug("Performing method-specific cleanup preparation.");
      result = prepareCleanupInternal(pTransferInformation, pSecurityContext);
      if (result) {
        File deleteFile = new File(localPath, Constants.STAGING_DELETED_FILENAME);
        LOGGER.debug("Try creating cleanup indicator file at {}", deleteFile);
        try {
          FileUtils.touch(deleteFile);
          LOGGER.debug("Cleanup indicator successfully created.");
        } catch (IOException ex) {
          LOGGER.error("Failed to touch cleanup indicator file " + deleteFile + ". Cleanup preparation failed.", ex);
          result = false;
        }
      }
    }
    return result;
  }

  /**
   * Perform the internal cleanup necessary to be able to cleanup all local
   * files. This may contains changes to the ownership or changes of access
   * permissions.
   *
   * @param pTransferInformation The transfer to cleanup.
   * @param pSecurityContext Security context used to authorize access and to
   * obtain user/group information.
   *
   * @return TRUE if the cleanup was succeeded (.deleted was created) or no
   * files were left, FALS if .deleted could not be created and there are files
   * left.
   */
  protected abstract boolean prepareCleanupInternal(ITransferInformation pTransferInformation, IAuthorizationContext pSecurityContext);

  /**
   * Prepare the caching location. After preparation there should be a locally
   * accessible folder on the caching location available. The AccessPoint
   * implementation should be able to return a remotely accessible URL by
   * calling
   * {@link #getAccessUrl(edu.kit.dama.rest.staging.interfaces.ITransferInformation, edu.kit.authorization.entities.IAuthorizationContext)}
   * afterwards and this location should then be accessible using a appropriate
   * data transfer client. This method is executed while calling prepare(),
   * direct access should be forbidden.
   *
   * @param pTransferInformation The transfer information entity this transfer
   * is associated with.
   * @param pSecurityContext The security context used for authorization or to
   * obtain user/group specific transfer client properties.
   *
   * @throws TransferPreparationException If something goes wrong while
   * preparing the cache.
   */
  protected abstract void prepareCache(ITransferInformation pTransferInformation, IAuthorizationContext pSecurityContext)
          throws TransferPreparationException;

  /**
   * Get the method name.
   *
   * @return The name.
   */
  public final String getName() {
    return configuration.getName();
  }

  /**
   * Get the id.
   *
   * @return the id.
   */
  public String getId() {
    return configuration.getUniqueIdentifier();
  }

  /**
   * Get the configuration.
   *
   * @return The configuration.
   */
  public final StagingAccessPointConfiguration getConfiguration() {
    return configuration;
  }

  /**
   * Get the local representation of the provided URL.
   *
   * @param pUrl The URL.
   * @param pContext The security context.
   *
   * @return The file corresponding to pUrl.
   */
  public abstract File getLocalPathForUrl(URL pUrl, IAuthorizationContext pContext);

  /**
   * Get the URL representation of the provided local path.
   *
   * @param pLocalPath The local file representation.
   * @param pContext The security context.
   *
   * @return The URL corresponding to pLocalPath.
   */
  public abstract URL getUrlForLocalPath(File pLocalPath, IAuthorizationContext pContext);

  /**
   * Get the access URL where the staged data can be accessed. Typically, the
   * returned URL points to the remotely accessible base URL of the transfer
   * where everything (data, generated files and settings) is located.
   *
   * @param pTransferInformation The transfer information for which the access
   * URL should be created.
   * @param pContext The security context.
   *
   * @return The access URL.
   */
  public abstract URL getAccessUrl(ITransferInformation pTransferInformation, IAuthorizationContext pContext);

  /**
   * Returns the URL for the data folder. In order to be able to obtain a valid
   * data URL, pBaseFolderUrl should be the result of calling {@link #getAccessUrl(edu.kit.dama.rest.staging.interfaces.ITransferInformation, edu.kit.authorization.entities.IAuthorizationContext)
   * } at an implementation of AbstractStagingAccessPoint.
   *
   * @param pBaseFolderUrl The base folder URL obtained via {@link #getAccessUrl(edu.kit.dama.rest.staging.interfaces.ITransferInformation, edu.kit.authorization.entities.IAuthorizationContext)
   * } or from an entity's staging URL.
   *
   * @return The URL to the data folder.
   *
   * @throws MalformedURLException If concatanation of pBaseFolderUrl and the
   * data path fails.
   */
  public static URL getDataFolderUrl(URL pBaseFolderUrl) throws MalformedURLException {
    return URLCreator.appendToURL(pBaseFolderUrl, Constants.STAGING_DATA_FOLDER_NAME + "/");
  }

  /**
   * Returns the data folder depending on the provided base folder.
   *
   * @param pBaseFolder The base folder.
   *
   * @return The data folder.
   */
  public static File getDataFolder(File pBaseFolder) {
    return new File(pBaseFolder, Constants.STAGING_DATA_FOLDER_NAME + "/");
  }

  /**
   * Returns the URL for the settings folder. In order to be able to obtain a
   * valid settings URL, pBaseFolderUrl should be the result of calling {@link #getAccessUrl(edu.kit.dama.rest.staging.interfaces.ITransferInformation, edu.kit.authorization.entities.IAuthorizationContext)
   * } at an implementation of AbstractStagingAccessPoint.
   *
   * @param pBaseFolderUrl The base folder URL obtained via {@link #getAccessUrl(edu.kit.dama.rest.staging.interfaces.ITransferInformation, edu.kit.authorization.entities.IAuthorizationContext)
   * } or from an entity's staging URL.
   *
   * @return The URL to the settings folder.
   *
   * @throws MalformedURLException If concatanation of pBaseFolderUrl and the
   * settings path fails.
   */
  public static URL getSettingsFolderUrl(URL pBaseFolderUrl) throws MalformedURLException {
    return URLCreator.appendToURL(pBaseFolderUrl, Constants.STAGING_SETTINGS_FOLDER_NAME + "/");
  }

  /**
   * Returns the setting folder depending on the provided base folder.
   *
   * @param pBaseFolder The base folder.
   *
   * @return The settings folder.
   */
  public static File getSettingsFolder(File pBaseFolder) {
    return new File(pBaseFolder, Constants.STAGING_SETTINGS_FOLDER_NAME + "/");
  }

  /**
   * Returns the URL for the generated data folder. In order to be able to
   * obtain a valid generated data URL, pBaseFolderUrl should be the result of
   * calling {@link #getAccessUrl(edu.kit.dama.rest.staging.interfaces.ITransferInformation, edu.kit.authorization.entities.IAuthorizationContext)
   * } at an implementation of AbstractStagingAccessPoint.
   *
   * @param pBaseFolderUrl The base folder URL obtained via {@link #getAccessUrl(edu.kit.dama.rest.staging.interfaces.ITransferInformation, edu.kit.authorization.entities.IAuthorizationContext)
   * } or from an entity's staging URL.
   *
   * @return The URL to the generated folder.
   *
   * @throws MalformedURLException If concatanation of pBaseFolderUrl and the
   * generated path fails.
   */
  public static URL getGeneratedFolderUrl(URL pBaseFolderUrl) throws MalformedURLException {
    return URLCreator.appendToURL(pBaseFolderUrl, Constants.STAGING_GENERATED_FOLDER_NAME + "/");
  }

  /**
   * Returns the generated folder depending on the provided base folder.
   *
   * @param pBaseFolder The base folder.
   *
   * @return The generated folder.
   */
  public static File getGeneratedFolder(File pBaseFolder) {
    return new File(pBaseFolder, Constants.STAGING_GENERATED_FOLDER_NAME + "/");
  }
}
