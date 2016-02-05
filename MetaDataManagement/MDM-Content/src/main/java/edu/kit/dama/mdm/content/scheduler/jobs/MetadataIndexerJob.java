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
package edu.kit.dama.mdm.content.scheduler.jobs;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.entities.util.FindUtil;
import edu.kit.dama.authorization.entities.util.PU;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.mdm.content.es.MetadataIndexingHelper;
import edu.kit.dama.scheduler.quartz.jobs.AbstractConfigurableJob;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import edu.kit.dama.util.PropertiesUtil;
import java.util.Properties;
import javax.persistence.EntityManager;
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
public class MetadataIndexerJob extends AbstractConfigurableJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataIndexerJob.class);

    /**
     * The property key for setting the id of the group for which the metadata
     * should be processed.
     */
    private static final String GROUP_ID = "groupId";

    /**
     * The property key for setting the index for publishing the metadata.
     */
    private static final String INDEX = "index";

    /**
     * The property key for setting the cluster for publishing the metadata.
     */
    private static final String CLUSTER = "cluster";

    private String groupId = Constants.USERS_GROUP_ID;
    private String cluster = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_CLUSTER_ID, "KITDataManager");
    private String index = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_INDEX_ID, "dc");

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        LOGGER.debug("Executing MetadataIndexerJob with job key := {}.", context.getJobDetail().getKey());

        int exitCode = 0;
        try {
            LOGGER.debug("Starting metadata indexer");
            boolean success = false;
            try {
                LOGGER.debug(" - Configuring indexer");
                final String jobParameters = context.getJobDetail().getJobDataMap().getString("jobParameters");
                Properties props = new Properties();
                if (jobParameters != null) {
                    props = PropertiesUtil.propertiesFromString(jobParameters);
                }
                groupId = (props.getProperty(GROUP_ID) != null) ? props.getProperty(GROUP_ID) : Constants.USERS_GROUP_ID;
                cluster = (props.getProperty(CLUSTER) != null) ? props.getProperty(CLUSTER) : cluster;
                index = (props.getProperty(INDEX) != null) ? props.getProperty(INDEX) : index;
                LOGGER.debug(" - Performing indexing");
                boolean result = MetadataIndexingHelper.getSingleton().performIndexing(cluster, index, new GroupId(groupId), 10, AuthorizationContext.factorySystemContext());
                if (!result) {
                    exitCode ^= 2;
                }
                LOGGER.debug("Indexer has finished.");
                success = true;
            } finally {
                if (exitCode == 0 && !success) {
                    LOGGER.warn("Unexpected result detected.");
                    exitCode ^= 1;
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while executing MetadataIndexerJob with job key := " + context.getJobDetail().getKey() + ".", ex);
            throw new JobExecutionException(ex);
        }
        context.setResult(exitCode);

        LOGGER.debug("Finishing  MetadataIndexerJob with job key := {}, exitCode := {}.", context.getJobDetail().getKey(), exitCode);
    }

    @Override
    public String[] getInternalPropertyKeys() {
        return new String[]{GROUP_ID, CLUSTER, INDEX};
    }

    @Override
    public String getInternalPropertyDescription(String pKey) {
        if (null != pKey) {
            switch (pKey) {
                case GROUP_ID:
                    return "The group whose metadata entries should be indexed.";
                case CLUSTER:
                    return "The cluster at which the metadata is indexed.";
                case INDEX:
                    return "The index at which the metadata is indexed.";
            }
        }
        return "Unknown property key '" + pKey + "'";
    }

    @Override
    public void validateProperties(Properties pProperties) throws PropertyValidationException {
        EntityManager em = PU.entityManager();
        String group = pProperties.getProperty(GROUP_ID);
        if (group != null) {
            try {
                FindUtil.findGroup(em, new GroupId(group));
            } catch (EntityNotFoundException ex) {
                throw new PropertyValidationException("Group with id '" + group + "' not found.", ex);
            }
        }
    }
}
