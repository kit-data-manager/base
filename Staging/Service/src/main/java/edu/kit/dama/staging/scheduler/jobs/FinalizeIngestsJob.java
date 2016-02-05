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

import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.scheduler.quartz.jobs.AbstractConfigurableJob;
import edu.kit.dama.staging.services.impl.StagingService;
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

        LOGGER.debug("Executing FinalizeIngestsJob with job key := {}.", context.getJobDetail().getKey());

        boolean result;
        try {
            result = StagingService.getSingleton().finalizeIngests();
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while executing FinalizeDownloadsJob with job key := " + context.getJobDetail().getKey() + ".", ex);
            throw new JobExecutionException(ex);
        }
        context.setResult(result);

        LOGGER.debug("Finishing  FinalizeIngestsJob with job key := {}, result := {}.", context.getJobDetail().getKey(), result);
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
