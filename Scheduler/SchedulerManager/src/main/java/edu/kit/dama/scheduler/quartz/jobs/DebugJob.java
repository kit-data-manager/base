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
package edu.kit.dama.scheduler.quartz.jobs;

import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.util.Properties;
import javax.xml.bind.PropertyException;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wq7203
 */
public class DebugJob extends AbstractConfigurableJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugJob.class);
    private final static String MESSAGE_PROPERTY = "Debug Message";
    private final static String DELAY_PROPERTY = "Delay";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String key = context.getJobDetail().getKey().toString();
        String name = context.getJobDetail().getKey().getName();
        String group = context.getJobDetail().getKey().getGroup();
        String description = context.getJobDetail().getDescription();
        String clazz = context.getJobDetail().getClass().getName();
        try {
            final String jobParameters = context.getJobDetail().getJobDataMap().getString("jobParameters");
            Properties props = new Properties();
            if (jobParameters != null) {
                props = PropertiesUtil.propertiesFromString(jobParameters);
            }

            int delay = Integer.parseInt((String) props.get(DELAY_PROPERTY));

            String message = (String) props.get(MESSAGE_PROPERTY);
            LOGGER.debug("Executing DebugJob with job key := {}. name := {}, group := {}, description := {}, class := {} and message := {}.", new Object[]{key, name, group, description, clazz, message});
            LOGGER.debug("Sleeping {} ms...", delay);
            Thread.sleep(delay);
        } catch (Exception ex) {
            LOGGER.error("Failed to execute job '" + key + "'", ex);
        }
    }

    @Override
    public String propertiesToJobParameters(Properties pProperties) {
        Properties result = pProperties;
        try {
            if (result == null) {
                result = new Properties();
            }
            if (result.getProperty(MESSAGE_PROPERTY) == null) {
                result.setProperty(MESSAGE_PROPERTY, "Hello World.");
            }
            return PropertiesUtil.propertiesToString(result);
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public String[] getInternalPropertyKeys() {
        return new String[]{MESSAGE_PROPERTY, DELAY_PROPERTY};
    }

    @Override
    public String getInternalPropertyDescription(String pKey) {
        if (MESSAGE_PROPERTY.equals(pKey)) {
            return "The debug message to print out.";
        } else if (DELAY_PROPERTY.equals(pKey)) {
            return "The delay before the execution ends.";
        }
        return "Unknown property key '" + pKey + "'.";
    }

    @Override
    public void validateProperties(Properties pProperties) throws PropertyValidationException {
        if (pProperties.get(DELAY_PROPERTY) != null) {
            try {
                Integer.parseInt((String) pProperties.get(DELAY_PROPERTY));
            } catch (NumberFormatException ex) {
                throw new PropertyValidationException("Property " + DELAY_PROPERTY + " has an invalid value. A numeric value > 0 is expected.", ex);
            }
        }
    }
}
