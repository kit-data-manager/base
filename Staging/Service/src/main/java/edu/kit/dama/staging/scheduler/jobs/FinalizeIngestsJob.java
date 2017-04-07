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
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.scheduler.quartz.jobs.AbstractConfigurableJob;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.services.impl.StagingService;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationPersistenceImpl;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationServiceLocal;
import edu.kit.dama.util.DataManagerSettings;
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
public class FinalizeIngestsJob extends AbstractConfigurableJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(FinalizeIngestsJob.class);

    public FinalizeIngestsJob() {
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        int startedRunners = 0;
        LOGGER.debug("Executing FinalizeIngestsJob with job key := {}.", context.getJobDetail().getKey());
        int maxParallelIngest = DataManagerSettings.getSingleton().getIntProperty(DataManagerSettings.STAGING_MAX_PARALLEL_INGESTS, 4);
        if (maxParallelIngest > 25) {
            LOGGER.warn("Property " + DataManagerSettings.STAGING_MAX_PARALLEL_INGESTS + " is set to {} but the limit is 25.", maxParallelIngest);
            maxParallelIngest = 25;
        }

        LOGGER.debug("Checking running ingests.");

        int running = IngestInformationPersistenceImpl.getSingleton().getEntitiesCountByStatus(INGEST_STATUS.INGEST_RUNNING, AuthorizationContext.factorySystemContext()).intValue();
        if (running >= maxParallelIngest) {
            LOGGER.info("There is already the max. amount of ingests ({}) running.", maxParallelIngest);
        } else {
            int freeSlots = maxParallelIngest - running;
            LOGGER.debug("Less than {} ingests are running. Trying to obtain {} finalizable ingests.", maxParallelIngest, freeSlots);

            List<IngestInformation> transferableIngests = IngestInformationPersistenceImpl.getSingleton().getTransferableEntities(freeSlots, AuthorizationContext.factorySystemContext());
            if (!transferableIngests.isEmpty()) {
                LOGGER.debug("Retrieved {} finalizable ingests. Updating status and starting ingest runners.", transferableIngests.size());
                for (IngestInformation ingest : transferableIngests) {
                    LOGGER.debug("Setting ingest with id {} to {}.", ingest.getId(), INGEST_STATUS.INGEST_RUNNING);
                    IngestInformationPersistenceImpl.getSingleton().updateStatus(ingest.getId(), INGEST_STATUS.INGEST_RUNNING, null, AuthorizationContext.factorySystemContext());
                    LOGGER.debug("Spawning ingest runner for ingest {} and object id {}.", ingest.getId(), ingest.getDigitalObjectId());
                    new IngestRunner(new DigitalObjectId(ingest.getDigitalObjectId())).start();
                    LOGGER.debug("Ingest runner successfully started.");
                    startedRunners++;
                }
            } else {
                LOGGER.debug("No finalizable ingests found. Finishing finalizer job.");
            }
        }
        LOGGER.debug("Started {} ingest runners.", startedRunners);
        context.setResult(startedRunners);

        LOGGER.debug("Finishing  FinalizeIngestsJob with job key := {}, result := {}.", context.getJobDetail().getKey(), startedRunners);
    }

    static class IngestRunner extends Thread {

        private final DigitalObjectId objectId;

        public IngestRunner(DigitalObjectId objectId) {
            this.objectId = objectId;
            setDaemon(true);
            setPriority(MIN_PRIORITY);
        }

        @Override
        public void run() {
            try {
                LOGGER.info("Starting ingest runner for object id {}.", objectId);
                boolean result = StagingService.getSingleton().finalizeIngest(objectId, AuthorizationContext.factorySystemContext());
                LOGGER.info("Ingest runner for object id {} has {}", objectId, (result) ? "succeeded." : "failed.");
            } catch (Exception ex) {
                LOGGER.error("Exception occurred while finalizing ingest for object " + objectId + ".", ex);
            } finally {
                LOGGER.debug("Cleaned up {} ingest(s)", IngestInformationServiceLocal.getSingleton().cleanup(AuthorizationContext.factorySystemContext()));
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
