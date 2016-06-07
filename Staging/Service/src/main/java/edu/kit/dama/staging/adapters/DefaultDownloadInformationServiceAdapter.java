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
package edu.kit.dama.staging.adapters;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.services.impl.download.DownloadInformationServiceLocal;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.staging.exceptions.ServiceAdapterException;
import edu.kit.dama.staging.interfaces.IDownloadInformationServiceAdapter;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class DefaultDownloadInformationServiceAdapter implements IDownloadInformationServiceAdapter {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDownloadInformationServiceAdapter.class);

  /**
   * Default constructor.
   */
  public DefaultDownloadInformationServiceAdapter() {
    //do nothing as the local service does not need any initalization
  }

  /**
   * Default constructor.
   *
   * @param pUrl Download information service URL. (Not supported, yet)
   */
  public DefaultDownloadInformationServiceAdapter(URL pUrl) {
    //External service not supported in this implementation...we only use a local instance
  }

  @Override
  public DownloadInformation getDownloadInformation(DigitalObjectId pDigitalObjectId, IAuthorizationContext pContext) throws ServiceAdapterException {
    LOGGER.debug("Getting download information for object with id '{}'", pDigitalObjectId);
    DownloadInformation result;
    List<DownloadInformation> queryResult = DownloadInformationServiceLocal.getSingleton().getDownloadInformationByDigitalObjectId(pDigitalObjectId, 0, Integer.MAX_VALUE, pContext);
    if (queryResult != null && !queryResult.isEmpty()) {
      LOGGER.debug("Query to DownloadInformationService returned");
      if (queryResult.size() == 1) {
        result = queryResult.get(0);
      } else {
        throw new ServiceAdapterException("Query returned more than one result. Probably this method was called by an administrator.");
      }
    } else {
      throw new ServiceAdapterException("Query to DownloadInformationService returned no result");
    }
    return result;
  }

  @Override
  public DownloadInformation getDownloadInformation(Long pId, IAuthorizationContext pContext) throws ServiceAdapterException {
    LOGGER.debug("Getting download information for entity with id '{}'", pId);
    DownloadInformation queryResult = DownloadInformationServiceLocal.getSingleton().getDownloadInformationById(pId, pContext);
    if (queryResult != null) {
      LOGGER.debug("Query to DownloadInformationService returned");
    } else {
      throw new ServiceAdapterException("Query to DownloadInformationService returned no result");
    }
    return queryResult;
  }

  @Override
  public void removeDownloadInformation(Long pId, IAuthorizationContext pContext) throws ServiceAdapterException {
    LOGGER.debug("Removing download information for id '{}'", pId);
    if (DownloadInformationServiceLocal.getSingleton().removeDownload(pId, pContext) == 1) {
      LOGGER.debug("One download row was successfully removed");
    }
  }

  @Override
  public List<DownloadInformation> getDownloadsForStaging(IAuthorizationContext pContext) throws ServiceAdapterException {
    LOGGER.debug("Getting all downloads ready for staging");
    List<DownloadInformation> result = new LinkedList<>();
    List<DownloadInformation> queryResult = DownloadInformationServiceLocal.getSingleton().getDownloadInformationByStatus(DOWNLOAD_STATUS.SCHEDULED.getId(), 0, Integer.MAX_VALUE, pContext);
    if (queryResult != null && !queryResult.isEmpty()) {
      LOGGER.debug("Query for stageable downloads returned '{}' results", queryResult.size());
      Collections.addAll(result, queryResult.toArray(new DownloadInformation[queryResult.size()]));
    } else {
      LOGGER.info("Query to DownloadInformationService returned no result");
    }
    return result;
  }

  @Override
  public void updateDownloadInformation(DownloadInformation pInformation, IAuthorizationContext pContext) throws ServiceAdapterException {
    //update status (used in StagingService)
    DownloadInformationServiceLocal.getSingleton().updateStatus(pInformation.getId(), pInformation.getStatus(), pInformation.getErrorMessage(), pContext);
  }

  @Override
  public List<DownloadInformation> getDownloadsByStatus(DOWNLOAD_STATUS pStatus, IAuthorizationContext pContext) throws ServiceAdapterException {
    LOGGER.debug("Getting all downloads by status {}", pStatus);
    List<DownloadInformation> result = new LinkedList<>();
    List<DownloadInformation> queryResult = DownloadInformationServiceLocal.getSingleton().getDownloadInformationByStatus(pStatus.getId(), 0, Integer.MAX_VALUE, pContext);
    if (queryResult != null && !queryResult.isEmpty()) {
      LOGGER.debug("Query for downloads by status returned '{}' results", queryResult.size());
      Collections.addAll(result, queryResult.toArray(new DownloadInformation[queryResult.size()]));
    } else {
      LOGGER.info("Query to DownloadInformationService returned no result");
    }
    return result;
  }

  @Override
  public boolean configure(Configuration pConfig) throws ConfigurationException {
    // throw new UnsupportedOperationException("Not supported yet.");
    return true;
  }
}
