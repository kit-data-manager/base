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
package edu.kit.dama.staging.servlet;

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.services.impl.download.DownloadInformationPersistenceImpl;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationPersistenceImpl;
import java.util.List;
import javax.persistence.PersistenceException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Staging initializer listener checking if any transfers are running at startup
 * time. Most probably, these transfers where running when the servlet or server
 * has been restarted. Thus, the associated threads might be gone and the
 * transfer won't be finished.
 *
 * For downloads the status can just be reset and the download will be prepared
 * again. For ingests it has to be checked, if there is already a data
 * organization in place. If this is the case, the ingest is marked as failed
 * and manual interaction is necessary. Otherwise, the ingest will be reset to
 * PRE_INGEST_FINISHED.
 *
 * @author jejkal
 */
@WebListener
public class StagingInitializerListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StagingInitializerListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.debug("StagingInitializerListener servlet loaded, checking for stale ingests/downloads.");

        int running = IngestInformationPersistenceImpl.getSingleton().getEntitiesCountByStatus(INGEST_STATUS.INGEST_RUNNING, AuthorizationContext.factorySystemContext()).intValue();
        if (running > 0) {
            LOGGER.warn("There are {} ingests in status INGEST_RUNNING at startup time. Problably, these ingests are staled. Checking if automatic handling is possible.", running);

            while (running > 0) {
                List<IngestInformation> ingests = IngestInformationPersistenceImpl.getSingleton().getEntitiesByStatus(INGEST_STATUS.INGEST_RUNNING, 0, 100, AuthorizationContext.factorySystemContext());
                LOGGER.warn("Handling next {} stale ingests.", ingests.size());

                for (IngestInformation ingest : ingests) {
                    String oid = ingest.getDigitalObjectId();
                    try {
                        LOGGER.debug("Checking for existing data organization for ingest with transfer id {}.", ingest.getTransferId());
                        List<String> views = DataOrganizerFactory.getInstance().getDataOrganizer().getViews(new DigitalObjectId(oid));
                        LOGGER.warn("Running ingest with transferId {} already created data organization views {}. Automatic handling not possible, setting ingest to error state.", ingest.getTransferId(), views);
                        IngestInformationPersistenceImpl.getSingleton().updateStatus(ingest.getId(), INGEST_STATUS.INGEST_FAILED, "Stale ingest with existing data organization detected. Unable to continue.", AuthorizationContext.factorySystemContext());
                    } catch (PersistenceException ex) {
                        //not exists
                        LOGGER.debug("Ingest with transfer id {} seems not to have an existing data organization. Resetting status to {}.", ingest.getTransferId(), INGEST_STATUS.PRE_INGEST_FINISHED);
                        IngestInformationPersistenceImpl.getSingleton().updateStatus(ingest.getId(), INGEST_STATUS.PRE_INGEST_FINISHED, "Stale ingest without existing data organization detected. Resetting status.", AuthorizationContext.factorySystemContext());
                    }
                }
                running -= ingests.size();
            }
        }

        running = DownloadInformationPersistenceImpl.getSingleton().getEntitiesCountByStatus(DOWNLOAD_STATUS.PREPARING, AuthorizationContext.factorySystemContext()).intValue();
        if (running > 0) {
            LOGGER.warn("There are {} downloads in status PREPARING at startup time. Resetting status.", running);
            while (running > 0) {
                List<DownloadInformation> downloads = DownloadInformationPersistenceImpl.getSingleton().getEntitiesByStatus(DOWNLOAD_STATUS.PREPARING, 0, 100, AuthorizationContext.factorySystemContext());
                LOGGER.warn("Handling next {} stale downloads.", downloads.size());
                for (DownloadInformation download : downloads) {
                    LOGGER.debug("Resetting status of download with transfer id {}.", download.getTransferId(), DOWNLOAD_STATUS.SCHEDULED);
                    DownloadInformationPersistenceImpl.getSingleton().updateStatus(download.getId(), DOWNLOAD_STATUS.SCHEDULED, "Stale download detected. Resetting status.", AuthorizationContext.factorySystemContext());
                }

                running -= downloads.size();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //nothing to do here
    }

}
