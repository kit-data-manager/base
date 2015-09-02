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

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.tools.url.URLCreator;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This implementation provides a staging access point which masks parts of the
 * remote or the local access point. In contrast to the
 * {@link BasicStagingAccessPoint} there is no direct mapping between the local
 * path and the remote access URL. This first implementation is dedicated to the
 * use case, where WebDAV access to a caching location is secured using
 * mod_rewrite. In this scenario the user authenticates to the WebDAV service
 * using the configured authentication method. The mapped user will appear in
 * the REMOTE_USER variable of the web sever and is used by mod_rewrite to point
 * the user to 'his' storage location. For two users the remote access URL might
 * be the same, but due to the redirect they will point to two different
 * locations. This will lead to the following scenario:
 *
 * <ul>
 * <li>REMOTE_ACCESS_URL = http://anyhost/webdav/transferId/ </li>
 * <li>LOCAL_ACCESS_PATH = /cache/dav/userId/transferId/</li>
 * </ul>
 *
 * For this first version the user id inside KIT Data Manager obtained from the
 * authorization context is expected to be the user id on WebDAV level. Due to
 * this assumption, the mapping between remote and local access points is again
 * quite simple. This allows us simply to extend BasicStagingAccessPoint for
 * the moment overwriting the points responsible for mappings between paths and
 * URLs.
 *
 * @author mf6319
 */
public class MaskingAccessPoint extends BasicStagingAccessPoint {

  /**
   * Default constructor.
   */
  public MaskingAccessPoint() {
  }

  /**
   * Default constructor.
   *
   * @param pConfiguration The access point configuration.
   */
  public MaskingAccessPoint(StagingAccessPointConfiguration pConfiguration) {
    super(pConfiguration);
  }

  @Override
  public File getLocalPathForUrl(URL pUrl, IAuthorizationContext pContext) {
    //pUrl = http://localhost/webdav/4711/
    URL remoteBaseUrl;
    try {
      remoteBaseUrl = new URL(getConfiguration().getRemoteBaseUrl());
      //remoteBaseUrl = http://localhost/webdav/
    } catch (MalformedURLException ex) {
      throw new IllegalStateException("Remote base URL of access point " + getConfiguration().getUniqueIdentifier() + " is no valid URL. This should have been checked before!", ex);
    }
    String relativePath = URLCreator.getPathRelativeToRoot(remoteBaseUrl, pUrl);
    //relative path is null if pUrl is no child of remoteBaseUrl
    if (relativePath == null) {
      //remote base URL is no parent of provided url
      throw new IllegalArgumentException("Remote base URL " + remoteBaseUrl + " is no parent of " + pUrl);
    }
    //relativePath = 4711/
    //localBasePath = /var/www/htdocs/dav/
    //append masked user id to base path before adding the actual releative path
    String result = getConfiguration().getLocalBasePath() + "/" + pContext.getUserId().getStringRepresentation() + "/" + relativePath;
    //result = /var/www/htdocs/dav/<userId>/4711/
    return new File(result);
  }

  @Override
  public URL getUrlForLocalPath(File pLocalPath, IAuthorizationContext pContext) {
    URL localBasePathUrl;
    URL localPathUrl;
    URL remoteBaseUrl;
    //check local base path -> append masked user id to base path
    try {
      localBasePathUrl = new File(getConfiguration().getLocalBasePath() + "/" + pContext.getUserId().getStringRepresentation()).toURI().toURL();
      //localBasePathUrl = file:///var/www/htdocs/dav/<userId>
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
    //relativePath = 4711/
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

    String transferId = pTransferInformation.getTransferId();
    //transferId = 4711
    //just append the transfer id and omit the user id, as it should be masked
    return URLCreator.appendToURL(remoteBaseUrl, transferId + "/");
    //return http://localhost/webdav/4711/
  }
}
