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
package edu.kit.dama.staging.ap.impl;

import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.exceptions.StagingIntitializationException;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.tools.url.URLCreator;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This implementation provides to most basic staging AccessPoint with the
 * following mappings:
 *
 * <ul>
 * <li>REMOTE_ACCESS_URL = REMOTE_BASE_URL + /userId/transferId/ </li>
 * <li>LOCAL_ACCESS_PATH = LOCAL_BASE_PATH + /userId/transferId/</li>
 * </ul>
 *
 * There is no additional configuration necessary. This AccessPoint should be
 * used only if the cache/protocol takes care of access restrictions or if no
 * access restrictions are needed, as it is easy for users of this AccessPoint
 * to determine the location of another dataset and there are no additional
 * actions to restrict access to the REMOTE_ACCESS_URL by this implementation.
 *
 * @author mf6319
 */
public class BasicStagingAccessPoint extends AbstractStagingAccessPoint {

  private static final Logger LOGGER = LoggerFactory.getLogger(BasicStagingAccessPoint.class);

  /**
   * Default constructor.
   */
  public BasicStagingAccessPoint() {
  }

  /**
   * Default constructor.
   *
   * @param pConfiguration The AccessPoint configuration.
   */
  public BasicStagingAccessPoint(StagingAccessPointConfiguration pConfiguration) {
    super(pConfiguration);
  }

  @Override
  public String[] getInternalPropertyKeys() {
    //no properties
    return new String[]{};
  }

  @Override
  public String getInternalPropertyDescription(String pKey) {
    //no properties, no description
    return null;
  }

  @Override
  public String[] getUserPropertyKeys() {
    //no properties
    return new String[]{};
  }

  @Override
  public String getUserPropertyDescription(String pKey) {
    //no properties, no description
    return null;
  }

  @Override
  public void validateProperties(Properties pProperties) throws PropertyValidationException {
    String localBasePath = getConfiguration().getLocalBasePath();
    File localBaseFile = new File(localBasePath);
    LOGGER.debug("Checking local base path {} of AccessPoint", localBasePath);
    if (!edu.kit.dama.util.FileUtils.isAccessible(localBaseFile)) {
      throw new PropertyValidationException("The provided local base path at '" + localBasePath + "' is not accessible.");
    } else {
      LOGGER.debug("Local base path at {} successfully checked.", localBasePath);
    }
  }

  @Override
  public void configure(Properties pProperties) throws PropertyValidationException, ConfigurationException {
    String localBasePath = getConfiguration().getLocalBasePath();
    File localBaseFile = new File(localBasePath);
    if (!localBaseFile.exists()) {
      LOGGER.debug("Local staging AccessPoint path {} does not exist. Try to create it.", localBasePath);
      if (localBaseFile.mkdirs()) {
        LOGGER.debug("Local staging AccessPoint path at {} successfully created.", localBasePath);
      } else {
        throw new StagingIntitializationException("Failed to create local staging access path at " + localBasePath);
      }
    } else if (localBaseFile.exists() && !localBaseFile.isDirectory()) {
      //local base path exists but is no directory...
      throw new StagingIntitializationException("Local staging access path at " + localBasePath + " exists but is no directory.");
    } else {
      //check for transient AccessPoint and delete content
      if (getConfiguration().isTransientAccessPoint()) {
        LOGGER.debug("Deleting staging folder {} of transient AccessPoint.", localBasePath);
        File basePath = new File(localBasePath);
        for (File f : basePath.listFiles()) {
          LOGGER.debug("  Deleting file {} ", f);
          FileUtils.deleteQuietly(f);
        }
        LOGGER.debug("Cleanup finished.");
      }
    }
  }

  @Override
  protected boolean prepareCleanupInternal(ITransferInformation pTransferInformation, IAuthorizationContext pSecurityContext) {
    //nothing to prepare here
    return true;
  }

  @Override
  protected void prepareCache(ITransferInformation pTransferInformation, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
    LOGGER.debug("Preparing cache for transfer with id {}  and context {}", pTransferInformation.getId(), pSecurityContext);
    URL accessUrl = getAccessUrl(pTransferInformation, pSecurityContext);
    LOGGER.debug("  Access URL is: {}", accessUrl);
    File localPath = getLocalPathForUrl(accessUrl, pSecurityContext);
    LOGGER.debug("  Local path is: {}", localPath);
    if (!localPath.exists()) {
      LOGGER.debug("  Local path does not exist. Trying to create it.");
      if (localPath.mkdirs()) {
        LOGGER.debug("  Local path successfully created.");
      } else {
        throw new TransferPreparationException("Failed to create local path for transfer with id " + pTransferInformation.getId() + " at " + localPath);
      }
    } else {
      LOGGER.debug("Local path already exists. Cleaning up existing content.");
      for (File file : localPath.listFiles()) {
        LOGGER.debug(" Deleting file/folder " + file);
        FileUtils.deleteQuietly(file);
      }
    }
    LOGGER.debug("Cache prepared for transfer with id {} and context {}", pTransferInformation.getId(), pSecurityContext);
  }

  @Override
  public File getLocalPathForUrl(URL pUrl, IAuthorizationContext pContext) {
    LOGGER.debug("Getting local path for URL {}", pUrl);
    //pUrl = http://localhost/webdav/<userId>/4711/
    URL remoteBaseUrl;
    try {
      LOGGER.debug("Obtaining configured remote base URL");
      remoteBaseUrl = new URL(getConfiguration().getRemoteBaseUrl());
      //remoteBaseUrl = http://localhost/webdav/
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("Remote base URL of AccessPoint " + getConfiguration().getUniqueIdentifier() + " is no valid URL. This should have been checked before!", ex);
    }
    LOGGER.debug("Getting relative path of URL {} and base URL {}", pUrl, remoteBaseUrl);
    String relativePath = URLCreator.getPathRelativeToRoot(remoteBaseUrl, pUrl);

    //relative path is null if pUrl is no child of remoteBaseUrl
    if (relativePath == null) {
      //remote base URL is no parent of provided url
      throw new IllegalArgumentException("Remote base URL " + remoteBaseUrl + " is no parent of " + pUrl);
    } else {
      LOGGER.debug("Relative path is: {}", relativePath);
    }
    //relativePath = <userId>/4711/
    //localBasePath = /var/www/htdocs/dav/
    String result = getConfiguration().getLocalBasePath() + "/" + relativePath;
    //result = /var/www/htdocs/dav/<userId>/4711/
    return new File(result);
  }

  @Override
  public URL getUrlForLocalPath(File pLocalPath, IAuthorizationContext pContext) {
    URL localBasePathUrl;
    URL localPathUrl;
    URL remoteBaseUrl;
    //check local base path
    try {
      localBasePathUrl = new File(getConfiguration().getLocalBasePath()).toURI().toURL();
      //localBasePathUrl = file:///var/www/htdocs/dav/
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("Failed to obtain URL for local base path " + getConfiguration().getLocalBasePath(), ex);
    }
    //check remote base URL
    try {
      remoteBaseUrl = new URL(getConfiguration().getRemoteBaseUrl());
      //remoteBaseUrl = http://localhost/webdav/
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("Failed to create URL for remote base URL " + getConfiguration().getRemoteBaseUrl(), ex);
    }
    //check provided local file
    try {
      localPathUrl = pLocalPath.toURI().toURL();
      //file:///var/www/htdocs/dav/<userId>/4711/
    } catch (MalformedURLException ex) {
      throw new IllegalArgumentException("Failed to obtain URL for provided local path " + pLocalPath, ex);
    }

    String relativePath = URLCreator.getPathRelativeToRoot(localBasePathUrl, localPathUrl);
    //relativePath = <userId>/4711/
    return URLCreator.appendToURL(remoteBaseUrl, relativePath);
    //return  http://localhost/webdav/<userId>/4711/
  }

  @Override
  public URL getAccessUrl(ITransferInformation pTransferInformation, IAuthorizationContext pContext) {
    URL remoteBaseUrl;
    try {
      remoteBaseUrl = new URL(getConfiguration().getRemoteBaseUrl());
      //remoteBaseUrl = http://localhost/webdav/
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("Failed to create URL for remote base URL " + getConfiguration().getRemoteBaseUrl(), ex);
    }

    String userId = pContext.getUserId().getStringRepresentation();
    //userId = 1
    String transferId = pTransferInformation.getTransferId();
    //transferId = 4711
    return URLCreator.appendToURL(remoteBaseUrl, userId + "/" + transferId + "/");
    //return http://localhost/webdav/1/4711/
  }
}
