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
package edu.kit.dama.scheduler.servlet;

import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.scheduler.SchedulerManagement;
import edu.kit.dama.scheduler.api.impl.QuartzIntervalTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzSchedule;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import edu.kit.dama.util.DataManagerSettings;
import java.net.URL;
import java.util.Properties;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author wq7203
 */
@WebListener
public class JobSchedulerInitializerListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerInitializerListener.class);

    private static final String CONFIG_ROOT = "scheduler";

    private static final String CONFIG_WAIT_ON_SHUTDOWN = "waitOnShutdown";

    private static final String CONFIG_START_DELAY_SECONDS = "startDelaySeconds";

    private static final String ADD_DEFAULT_SCHEDULES = "addDefaultSchedules";
    private static final String JOB_STORE_DRIVER = "jobStoreDriver";

    private static final String JOB_STORE_CONNECTION_STRING = "jobStoreConnectionString";

    private static final String JOB_STORE_USER = "jobStoreUser";

    private static final String JOB_STORE_PASSWORD = "jobStorePassword";

    private static final String DEFAULT_JOB_STORE_DRIVER = "org.postgresql.Driver";
    private static final String DEFAULT_JOB_STORE_CONNECTION_STRING = "jdbc:postgresql://localhost:5432/datamanager";
    private static final String DEFAULT_JOB_STORE_USER = "postgres";
    private static final String DEFAULT_JOB_STORE_PASSWORD = "postgres";

    private static final boolean DEFAULT_WAIT_ON_SHUTDOWN = true;

    private static final boolean DEFAULT_ADD_DEFAULT_SCHEDULES = true;

    private static final int DEFAULT_START_DELAY_SECONDS = 5;

    private boolean waitOnShutdown = true;
    private int startDelaySeconds = 5;
    private boolean addDefaultSchedules = true;
    private String jobStoreDriver = "";
    private String jobStoreConnectionString = "";
    private String jobStoreUser = "";
    private String jobStorePassword = "";

    private boolean isInitialized = false;

    private org.quartz.Scheduler scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        LOGGER.debug("JobSchedulerInitializerListener servlet loaded, initializing scheduler.");

        loadConfiguration();

        LOGGER.debug("Using parameter '{}' := {}.", JOB_STORE_DRIVER, jobStoreDriver);
        LOGGER.debug("Using parameter '{}' := {}.", JOB_STORE_CONNECTION_STRING, jobStoreConnectionString);
        LOGGER.debug("Using parameter '{}' := {}.", JOB_STORE_USER, JOB_STORE_USER);
        LOGGER.debug("Using parameter '{}' := {}.", JOB_STORE_PASSWORD, JOB_STORE_PASSWORD);
        LOGGER.debug("Using parameter '{}' := {}.", CONFIG_WAIT_ON_SHUTDOWN, waitOnShutdown);
        LOGGER.debug("Using parameter '{}' := {}.", CONFIG_START_DELAY_SECONDS, startDelaySeconds);

        try {
            LOGGER.debug("Try to build a Quartz StdSchedulerFactory.");
            final SchedulerFactory sf = new StdSchedulerFactory(getQuertzProperties());

            LOGGER.debug("Try to get a scheduler object from SchedulerFactory.");
            scheduler = sf.getScheduler();

            scheduler.startDelayed(startDelaySeconds);
            LOGGER.debug("Scheduler successfully initialized.");

            if (addDefaultSchedules) {
                try {
                    LOGGER.debug("Checking for default schedules.");
                    SimpleSchedule ingestFinalizer = SchedulerManagement.getSchedulerManagement().getSchedulerManager().getScheduleById("Staging.IngestFinalizer");
                    if (ingestFinalizer == null) {
                        LOGGER.debug("Creating default schedules.");
                        SimpleSchedule ingestFinalizerSchedule = new QuartzSchedule();
                        ingestFinalizerSchedule.setName("IngestFinalizer");
                        ingestFinalizerSchedule.setDescription("Default ingest finalizer job for ingest operations.");
                        ingestFinalizerSchedule.setScheduleGroup("Staging");
                        ingestFinalizerSchedule.setJobClass("edu.kit.dama.staging.scheduler.jobs.FinalizeIngestsJob");
                        SimpleSchedule downloadFinalizerSchedule = new QuartzSchedule();
                        downloadFinalizerSchedule.setName("DownloadFinalizer");
                        downloadFinalizerSchedule.setDescription("Default download finalizer job for ingest operations.");
                        downloadFinalizerSchedule.setScheduleGroup("Staging");
                        downloadFinalizerSchedule.setJobClass("edu.kit.dama.staging.scheduler.jobs.FinalizeDownloadsJob");

                        LOGGER.debug("Creating default triggers.");
                        QuartzIntervalTrigger ingestTrigger = new QuartzIntervalTrigger();
                        ingestTrigger.setName("IngestFinalizerTrigger");
                        ingestTrigger.setTriggerGroup("Staging");
                        ingestTrigger.setDescription("Default trigger for the ingest finalizer job firing every 60 seconds.");
                        ingestTrigger.setPeriod(60000l);
                        ingestTrigger.setTimes(-1);
                        QuartzIntervalTrigger downloadTrigger = new QuartzIntervalTrigger();
                        downloadTrigger.setName("DownloadFinalizerTrigger");
                        downloadTrigger.setTriggerGroup("Staging");
                        downloadTrigger.setDescription("Default trigger for the download finalizer job firing every 60 seconds.");
                        downloadTrigger.setPeriod(30000l);
                        downloadTrigger.setTimes(-1);

                        LOGGER.debug("Adding ingest finalizer schedule.");
                        ingestFinalizerSchedule = SchedulerManagement.getSchedulerManagement().getSchedulerManager().addSchedule(ingestFinalizerSchedule);
                        LOGGER.debug("Adding download finalizer schedule.");
                        downloadFinalizerSchedule = SchedulerManagement.getSchedulerManagement().getSchedulerManager().addSchedule(downloadFinalizerSchedule);
                        LOGGER.debug("Adding ingest finalizer trigger.");
                        SchedulerManagement.getSchedulerManagement().getSchedulerManager().addTrigger(ingestFinalizerSchedule.getId(), ingestTrigger);
                        LOGGER.debug("Adding download finalizer trigger.");
                        SchedulerManagement.getSchedulerManagement().getSchedulerManager().addTrigger(downloadFinalizerSchedule.getId(), downloadTrigger);
                    } else {
                        LOGGER.debug("Default schedules are already registered.");
                    }
                } catch (UnauthorizedAccessAttemptException ex) {
                    LOGGER.error("Failed to register default schedules.", ex);
                }
            } else {
                LOGGER.debug("Skip adding default schedules.");
            }
            isInitialized = true;
        } catch (SchedulerException ex) {
            LOGGER.error("Cannot build SchedulerFactory, internal SchedulerException occurred.", ex);
        } finally {
            if (!isInitialized) {
                LOGGER.error("Failed to initialize SchedulerFactory. Job execution won't be possible.");
            }
        }
    }

    public Properties getQuertzProperties() {
        Properties p = new Properties();
        p.put("org.quartz.scheduler.instanceName", "KITDMScheduler");
        p.put("org.quartz.scheduler.skipUpdateCheck", "true");
        p.put("org.quartz.scheduler.jmx.export", "false");
        //## Can be set to "true" to have the the main thread of the scheduler created as daemon thread.
        //## Default is "false".
        //#org.quartz.scheduler.makeSchedulerThreadDaemon = true

        //## The name of the ThreadPool implementation.
        //## The thread pool that ships with Quartz is "org.quartz.simpl.SimpleThreadPool".
        p.put("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        p.put("org.quartz.threadPool.threadCount", "8");
        //## Can be set to "true" to have the threads in the pool created as daemon threads.
        //## Default is "false".
        //#org.quartz.threadPool.makeThreadsDaemons = true
        //## Can be any int between Thread.MIN_PRIORITY (which is 1) and Thread.MAX_PRIORITY (which is 10).
        //## The default is Thread.NORM_PRIORITY (5).

        //#org.quartz.threadPool.threadPriority = 5
        //# Using JobStoreTX
        //## Be sure to run the appropriate script first to create database/tables
        p.put("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");

        //# Configuring JDBCJobStore with the Table Prefix
        p.put("org.quartz.jobStore.tablePrefix", "qrtz_");
        //# Using DriverDelegate
        p.put("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        p.put("org.quartz.jobStore.dataSource", "quartzDataSource");

        p.put("org.quartz.dataSource.quartzDataSource.driver", jobStoreDriver);
        p.put("org.quartz.dataSource.quartzDataSource.URL", jobStoreConnectionString);
        p.put("org.quartz.dataSource.quartzDataSource.user", jobStoreUser);
        p.put("org.quartz.dataSource.quartzDataSource.password", jobStorePassword);
        p.put("org.quartz.dataSource.quartzDataSource.maxConnections", "30");

        return p;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.debug("Try to shutdown the scheduler.");

        if (scheduler != null && isInitialized) {
            try {
                scheduler.shutdown(waitOnShutdown);
            } catch (SchedulerException ex) {
                LOGGER.error("Scheduler failed to shutdown cleanly, internal SchedulerException occurred.", ex);
            } finally {
                try {
                    if (scheduler.isShutdown()) {
                        LOGGER.info("Quartz Scheduler successful destroyed.");
                    }
                } catch (SchedulerException ex) {
                    LOGGER.error("Cannot get operating status from scheduler, internal SchedulerException occurred.", ex);
                }
            }
        } else {
            LOGGER.error("Could not shutdown Quartz Scheduler, scheduler is null or not initialized.");
        }
    }

    /**
     * Load configuration from XML-File
     */
    private void loadConfiguration() {

        URL configURL;
        HierarchicalConfiguration hc;

        try {
            configURL = DataManagerSettings.getConfigurationURL();
            LOGGER.debug("Loading configuration from {}", configURL);
            hc = new HierarchicalConfiguration(new XMLConfiguration(configURL));
            LOGGER.debug("Configuration successfully loaded");
        } catch (ConfigurationException ex) {
            // error in configuration
            // reason see debug log message:
            LOGGER.warn("Failed to load configuration, using default values.", ex);
            loadDefaultConfiguration();
            return;
        }

        SubnodeConfiguration configurationAt = hc.configurationAt(CONFIG_ROOT);

        if (configurationAt != null) {

            String jobStoreDriverParam = null;
            try {
                jobStoreDriverParam = configurationAt.getString(JOB_STORE_DRIVER, null);
            } catch (ConversionException ex) {
                LOGGER.error("Failed to parse parameter '{}'.", JOB_STORE_DRIVER);
            }
            if (jobStoreDriverParam != null) {
                jobStoreDriver = jobStoreDriverParam;
            } else {
                LOGGER.info("No parameter '{}' defined, defaulting to {}.", JOB_STORE_DRIVER, DEFAULT_JOB_STORE_DRIVER);
                jobStoreDriver = DEFAULT_JOB_STORE_DRIVER;
            }

            String jobStoreConnectionStringParam = null;
            try {
                jobStoreConnectionStringParam = configurationAt.getString(JOB_STORE_CONNECTION_STRING, null);
            } catch (ConversionException ex) {
                LOGGER.error("Failed to parse parameter '{}'.", JOB_STORE_CONNECTION_STRING);
            }
            if (jobStoreConnectionStringParam != null) {
                jobStoreConnectionString = jobStoreConnectionStringParam;
            } else {
                LOGGER.info("No parameter '{}' defined, defaulting to {}.", JOB_STORE_CONNECTION_STRING, DEFAULT_JOB_STORE_CONNECTION_STRING);
                jobStoreConnectionString = DEFAULT_JOB_STORE_CONNECTION_STRING;
            }

            String jobStoreUserParam = null;
            try {
                jobStoreUserParam = configurationAt.getString(JOB_STORE_USER, null);
            } catch (ConversionException ex) {
                LOGGER.error("Failed to parse parameter '{}'.", JOB_STORE_USER);
            }
            if (jobStoreUserParam != null) {
                jobStoreUser = jobStoreUserParam;
            } else {
                LOGGER.info("No parameter '{}' defined, defaulting to {}.", JOB_STORE_USER, DEFAULT_JOB_STORE_USER);
                jobStoreUser = DEFAULT_JOB_STORE_USER;
            }
            String jobStorePasswordParam = null;
            try {
                jobStorePasswordParam = configurationAt.getString(JOB_STORE_PASSWORD, null);
            } catch (ConversionException ex) {
                LOGGER.error("Failed to parse parameter '{}'.", JOB_STORE_PASSWORD);
            }
            if (jobStoreUserParam != null) {
                jobStorePassword = jobStorePasswordParam;
            } else {
                LOGGER.info("No parameter '{}' defined, defaulting to {}.", JOB_STORE_PASSWORD, DEFAULT_JOB_STORE_PASSWORD);
                jobStorePassword = DEFAULT_JOB_STORE_PASSWORD;
            }

            Boolean waitOnShutdownParam = null;
            try {
                waitOnShutdownParam = configurationAt.getBoolean(CONFIG_WAIT_ON_SHUTDOWN, null);
            } catch (ConversionException ex) {
                LOGGER.error("Failed to parse parameter '{}'.", CONFIG_WAIT_ON_SHUTDOWN);
            }
            if (waitOnShutdownParam != null) {
                waitOnShutdown = waitOnShutdownParam;
            } else {
                LOGGER.info("No parameter '{}' defined, defaulting to {}.", CONFIG_WAIT_ON_SHUTDOWN, DEFAULT_WAIT_ON_SHUTDOWN);
                waitOnShutdown = DEFAULT_WAIT_ON_SHUTDOWN;
            }

            Boolean addDefaultSchedulesParam = null;
            try {
                addDefaultSchedulesParam = configurationAt.getBoolean(ADD_DEFAULT_SCHEDULES, null);
            } catch (ConversionException ex) {
                LOGGER.error("Failed to parse parameter '{}'.", ADD_DEFAULT_SCHEDULES);
            }
            if (addDefaultSchedulesParam != null) {
                addDefaultSchedules = addDefaultSchedulesParam;
            } else {
                LOGGER.info("No parameter '{}' defined, defaulting to {}.", ADD_DEFAULT_SCHEDULES, DEFAULT_ADD_DEFAULT_SCHEDULES);
                addDefaultSchedules = DEFAULT_ADD_DEFAULT_SCHEDULES;
            }

            Integer startDelaySecondsParam = null;
            try {
                startDelaySecondsParam = configurationAt.getInteger(CONFIG_START_DELAY_SECONDS, null);
            } catch (ConversionException ex) {
                LOGGER.error("Failed to parse parameter '{}'.", CONFIG_START_DELAY_SECONDS);
            }
            if (startDelaySecondsParam != null) {
                startDelaySeconds = startDelaySecondsParam;
            } else {
                LOGGER.info("No parameter '{}' defined, defaulting to {}.", CONFIG_START_DELAY_SECONDS, DEFAULT_START_DELAY_SECONDS);
                startDelaySeconds = DEFAULT_START_DELAY_SECONDS;
            }
        } else {
            LOGGER.info("No scheduler configuration node found in datamanager config. Using default values.");
        }
    }

    /**
     * Load default configuration.
     */
    private void loadDefaultConfiguration() {
        jobStoreConnectionString = DEFAULT_JOB_STORE_CONNECTION_STRING;
        jobStoreUser = DEFAULT_JOB_STORE_USER;
        jobStorePassword = DEFAULT_JOB_STORE_PASSWORD;
        waitOnShutdown = DEFAULT_WAIT_ON_SHUTDOWN;
        startDelaySeconds = DEFAULT_START_DELAY_SECONDS;
        addDefaultSchedules = DEFAULT_ADD_DEFAULT_SCHEDULES;
    }
}
