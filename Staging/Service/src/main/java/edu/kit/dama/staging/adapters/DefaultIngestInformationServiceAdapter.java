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
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.interfaces.IIngestInformationServiceAdapter;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationServiceLocal;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.staging.exceptions.ServiceAdapterException;
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
public class DefaultIngestInformationServiceAdapter implements IIngestInformationServiceAdapter {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIngestInformationServiceAdapter.class);

    /**
     * Default constructor.
     */
    public DefaultIngestInformationServiceAdapter() {
        //do nothing as the local service does not need any initalization
    }

    /**
     * Default constructor.
     *
     * @param pUrl The URL pointing the the ingest information service. (Not
     * supported,yet)
     */
    public DefaultIngestInformationServiceAdapter(URL pUrl) {
        //External service not supported in this implementation...we only use a local instance
    }

    @Override
    public IngestInformation getIngestInformation(DigitalObjectId pDigitalObjectId, IAuthorizationContext pContext) throws ServiceAdapterException {
        LOGGER.debug("Getting ingest information for object with id '{}'", pDigitalObjectId);
        IngestInformation result = IngestInformationServiceLocal.getSingleton().getIngestInformationByDigitalObjectId(pDigitalObjectId, pContext);
        if (result == null) {
            throw new ServiceAdapterException("Query to IngestInformationService with id " + pDigitalObjectId + "  returned no result");
        }
        return result;
    }

    @Override
    public void removeIngestInformation(Long pId, IAuthorizationContext pContext) throws ServiceAdapterException {
        LOGGER.debug("Removing ingest with id '{}'", pId);
        Integer queryResult = IngestInformationServiceLocal.getSingleton().removeEntity(pId, null);
        if (queryResult == 1) {
            LOGGER.debug("Ingest successfully removed");
        }
    }

    @Override
    public List<IngestInformation> getIngestsForArchiving(IAuthorizationContext pContext) throws ServiceAdapterException {
        LOGGER.debug("Getting all ingests ready for staging");
        List<IngestInformation> result = new LinkedList<IngestInformation>();
        List<IngestInformation> queryResult = IngestInformationServiceLocal.getSingleton().getIngestInformationByStatus(INGEST_STATUS.PRE_INGEST_FINISHED.getId(), -1, -1, null);
        if (queryResult != null) {
            LOGGER.debug("Query for archivable ingests returned '{}' results", queryResult.size());
            Collections.addAll(result, queryResult.toArray(new IngestInformation[queryResult.size()]));
        } else {
            LOGGER.debug("Query for archivable ingests returned no result");
        }
        return result;
    }

    @Override
    public void updateIngestInformation(IngestInformation pInformation, IAuthorizationContext pContext) throws ServiceAdapterException {
        //update storage URL (used in DefaultStorageVirtualizationAdapter) and status (used in StagingService)
        String storageUrl = pInformation.getStorageUrl();
        if (storageUrl != null) {
            LOGGER.debug("Updating storage URL for entity with id {} to value {}", new Object[]{pInformation.getId(), storageUrl});
            IngestInformationServiceLocal.getSingleton().updateStorageUrl(pInformation.getId(), storageUrl, null);
        }

        LOGGER.debug("Updating status for entity with id {}", pInformation.getId());
        //update information (staging URL?)
        IngestInformationServiceLocal.getSingleton().updateStatus(pInformation.getId(), pInformation.getStatus(), pInformation.getErrorMessage(), null);
    }

    @Override
    public List<IngestInformation> getIngestsByStatus(INGEST_STATUS pStatus, IAuthorizationContext pContext) throws ServiceAdapterException {
        LOGGER.debug("Getting all ingests by status {}", pStatus);
        List<IngestInformation> result = new LinkedList<IngestInformation>();
        List<IngestInformation> queryResult = IngestInformationServiceLocal.getSingleton().getIngestInformationByStatus(pStatus.getId(), -1, -1, null);
        if (queryResult != null) {
            LOGGER.debug("Query for ingests by status {} returned '{}' results", pStatus, queryResult.size());
            Collections.addAll(result, queryResult.toArray(new IngestInformation[queryResult.size()]));
        } else {
            LOGGER.debug("Query for ingests by status {} returned no results", pStatus);
        }
        return result;
    }

    @Override
    public boolean configure(Configuration pConfig) throws ConfigurationException {
        return true;
    }
}
