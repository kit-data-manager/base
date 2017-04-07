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
package edu.kit.dama.staging.scheduler.jobs;

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.scheduler.quartz.jobs.AbstractConfigurableJob;
import edu.kit.dama.staging.entities.download.DOWNLOAD_STATUS;
import edu.kit.dama.staging.entities.download.DownloadInformation;
import edu.kit.dama.staging.services.impl.StagingService;
import edu.kit.dama.staging.services.impl.download.DownloadInformationPersistenceImpl;
import edu.kit.dama.staging.services.impl.download.DownloadInformationServiceLocal;
import edu.kit.dama.util.DataManagerSettings;
import static java.lang.Thread.MIN_PRIORITY;
import java.util.List;
import java.util.Properties;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wq7203
 */
@DisallowConcurrentExecution
public class FinalizeDownloadsJob extends AbstractConfigurableJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinalizeDownloadsJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int startedRunners = 0;
        LOGGER.debug("Executing FinalizeDownloadsJob with job key := {}.", context.getJobDetail().getKey());
        int maxParallelDownloads = DataManagerSettings.getSingleton().getIntProperty(DataManagerSettings.STAGING_MAX_PARALLEL_DOWNLOADS, 4);
        if (maxParallelDownloads > 25) {
            LOGGER.warn("Property " + DataManagerSettings.STAGING_MAX_PARALLEL_DOWNLOADS + " is set to {} but the limit is 25.", maxParallelDownloads);
            maxParallelDownloads = 25;
        }

        LOGGER.debug("Checking running downloads.");

        int running = DownloadInformationPersistenceImpl.getSingleton().getEntitiesCountByStatus(DOWNLOAD_STATUS.PREPARING, AuthorizationContext.factorySystemContext()).intValue();

        if (running >= maxParallelDownloads) {
            LOGGER.info("There is already the max. amount of downloads ({}) running.", maxParallelDownloads);
        } else {
            int freeSlots = maxParallelDownloads - running;
            LOGGER.debug("Less than {} downloads are running. Trying to obtain {} finalizable downloads.", maxParallelDownloads, freeSlots);

            List<DownloadInformation> transferableDownloads = DownloadInformationPersistenceImpl.getSingleton().getTransferableEntities(freeSlots, AuthorizationContext.factorySystemContext());
            if (!transferableDownloads.isEmpty()) {
                LOGGER.debug("Retrieved {} finalizable downloads. Updating status and starting download runners.", transferableDownloads.size());
                for (DownloadInformation download : transferableDownloads) {
                    LOGGER.debug("Setting download with id {} to {}.", download.getId(), DOWNLOAD_STATUS.PREPARING);
                    DownloadInformationPersistenceImpl.getSingleton().updateStatus(download.getId(), DOWNLOAD_STATUS.PREPARING, null, AuthorizationContext.factorySystemContext());
                    LOGGER.debug("Spawning download runner for download {} and object id {}.", download.getId(), download.getDigitalObjectId());
                    new DownloadRunner(download).start();
                    LOGGER.debug("Download runner successfully started.");
                    startedRunners++;
                }
            } else {
                LOGGER.debug("No finalizable downloads found. Finishing finalizer job.");
            }
        }
        LOGGER.debug("Started {} download runners.", startedRunners);
        context.setResult(startedRunners);

        LOGGER.debug("Finishing FinalizeDownloadsJob with job key := {}, result := {}.", context.getJobDetail().getKey(), startedRunners);
    }

    static class DownloadRunner extends Thread {

        private final DownloadInformation download;

        public DownloadRunner(DownloadInformation download) {
            this.download = download;
            setDaemon(true);
            setPriority(MIN_PRIORITY);
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Starting download runner for download with transfer id {}.", download.getTransferId());
                boolean result = StagingService.getSingleton().finalizeDownload(download);
                LOGGER.info("Download runner for trasfer id {} has {}", download.getTransferId(), (result) ? "succeeded." : "failed.");
            } catch (Exception ex) {
                LOGGER.error("Exception occurred while finalizing download with transfer id " + download.getTransferId() + ".", ex);
            } finally {
                LOGGER.debug("Cleaned up {} downloads(s)", DownloadInformationServiceLocal.getSingleton().cleanup(AuthorizationContext.factorySystemContext()));
            }
        }
    }

    @Override
    public String[] getInternalPropertyKeys() {
        return new String[]{};
    }

    @Override
    public String getInternalPropertyDescription(String pKey) {
        return "";
    }

    @Override
    public void validateProperties(Properties pProperties) throws PropertyValidationException {
    }
}
