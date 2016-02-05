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
package edu.kit.dama.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.joran.util.ConfigurationWatchListUtil;
import ch.qos.logback.core.util.StatusPrinter;
import edu.kit.dama.commons.exceptions.InitializationError;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public final class DataManagerSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataManagerSettings.class);

    private final static DataManagerSettings SINGLETON = new DataManagerSettings();
    private static final String SETTINGS_FILENAME = "datamanager.xml";
    private XMLConfiguration settings = null;

    //Property keys for elasticsearch section
    public final static String ELASTIC_SEARCH_DEFAULT_PORT_ID = "elasticsearch.port";//9300;
    public final static String ELASTIC_SEARCH_DEFAULT_CLUSTER_ID = "elasticsearch.cluster";//KITDataManager;
    public final static String ELASTIC_SEARCH_DEFAULT_HOST_ID = "elasticsearch.host";//localhost;
    public final static String ELASTIC_SEARCH_DEFAULT_INDEX_ID = "elasticsearch.index";//kitdatamanager;
    public final static String ELASTIC_SEARCH_FULLTEXT_SEARCH_KEY_ID = "elasticsearch.fulltextKey";//es.fulltext;
    //Property keys for persistence section
    public final static String PERSISTENCE_STAGING_PU_ID = "persistence.stagingPU";//StagingUnit;
    public final static String PERSISTENCE_AUTHORIZATION_PU_ID = "persistence.authorizationPU";//AuthorizationPU;
    //Property keys for staging section
    public final static String STAGING_MAX_PARALLEL_TRANSFERS = "staging.maxParallelTransfers";//10;
    public final static String STAGING_MAX_PARALLEL_INGESTS = "staging.maxParallelIngests";//2;
    public final static String STAGING_MAX_PARALLEL_DOWNLOADS = "staging.maxParallelDownloads";//2;
    public final static String STAGING_MAX_DOWNLOAD_LIFETIME = "staging.maxDownloadLifetime";//60 * 60 * 24 * 7 seconds = 1 week;
    public final static String STAGING_MAX_INGEST_LIFETIME = "staging.maxIngestLifetime";//60 * 60 * 24 * 7 seconds = 1 week;

    //Property key for simple monitoring
    public static final String SIMON_CONFIG_LOCATION_ID = "simon.configLocation";

    //Property keys for general section
    public final static String GENERAL_SYSTEM_MAIL_ADDRESS = "general.systemMailAddress";
    public final static String GENERAL_MAIL_SERVER = "general.mailServer";
    public final static String GENERAL_GLOBAL_SECRET = "general.globalSecret";//qr2I9Hyp0CBhUUXj
    public static final String GENERAL_BASE_URL_ID = "general.baseUrl"; //"http://localhost:8080/";
    public static final String PRODUCTION_MODE_ID = "general.productionMode"; //"false";

    // Global production mode switch, disables e.g. serialization/deserialization c
    // class instance checks
    private boolean productionMode = false;

    /**
     * Default constructor.
     */
    DataManagerSettings() {
        initialize();
    }

    /**
     * Perform the internal initialization by loading the properties from the
     * URL obtained from {@link #getConfigurationURL()}.
     */
    private void initialize() {
        System.err.println("Initializing KIT Data Manager settings");
        try {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            StatusPrinter.print(lc);

        } catch (ClassCastException cce) {
            //ignore this...
            System.err.println("Failed to check logback settings. Probably another slf4j adapter is used.");
            cce.printStackTrace();
        }
        URL configUrl = null;
        try {
            configUrl = getConfigurationURL();
            LOGGER.debug("Loading configuration from {}", configUrl);
            settings = new XMLConfiguration(configUrl);
            LOGGER.debug("Loading productionMode property");
            productionMode = getBooleanProperty(PRODUCTION_MODE_ID, false);
            LOGGER.debug("Configuration initialized successfully.");
        } catch (ConfigurationException e) {
            //initialization not possible, quit everything.
            throw new InitializationError("Failed to load KIT Data Manager settings from URL " + configUrl, e);
        }
    }

    /**
     * Get the settings singleton.
     *
     * @return The singleton instance.
     */
    public static DataManagerSettings getSingleton() {
        return SINGLETON;
    }

    /**
     * Get KIT Data Manager configuration URL from one of the following sources
     * in the following order:
     * <ul>
     * <li>Environment variable DATAMANAGER_CONFIG</li>
     * <li>System.getProperty('datamanager.config')</li>
     * <li>File or resource 'datamanager.xml' in current folder/classpath</li>
     * </ul>
     *
     * Environment variable and system property may contain resource paths or
     * absolute/relative paths to a file on disk. If no configuration was found,
     * a ConfigurationException is thrown.
     *
     * @return The configuration URL.
     *
     * @throws ConfigurationException If no configuration was found at any
     * supported location.
     */
    public static URL getConfigurationURL() throws ConfigurationException {
        LOGGER.debug("Checking for configuration in environment variable DATAMANAGER_CONFIG");
        String configResource = System.getenv("DATAMANAGER_CONFIG");

        if (configResource == null) {
            LOGGER.debug("Environment variable is empty. Checking system property 'datamanager.config'");
            configResource = System.getProperty("datamanager.config");
            LOGGER.debug("System property datamanager.config is set to: {}", configResource);
        }

        URL resource = null;
        if (configResource != null) {
            //just try if configResource is not null
            resource = Thread.currentThread().getContextClassLoader().
                    getResource(configResource);
        }

        if (resource != null) {
            //system property/environment variable is a resource
            return resource;
        } else if (configResource != null && new File(configResource).exists()) {
            //system property is a file
            LOGGER.debug("Trying to return URL for provided configuration {}", configResource);
            try {
                //return URL to file
                return new File(configResource).toURI().toURL();
            } catch (MalformedURLException mue) {
                throw new ConfigurationException(
                        "Provided configuration file '" + configResource
                        + "' could not be mapped to a file", mue);
            }
        } else {
            LOGGER.debug("System property is invalid. Checking file {}", SETTINGS_FILENAME);
            configResource = SETTINGS_FILENAME;
        }

        File fileToResource = new File(configResource);
        if (fileToResource.exists()) {//file exists...use it
            LOGGER.debug("Trying to return URL for provided configuration {}", configResource);
            try {
                return fileToResource.toURI().toURL();
            } catch (MalformedURLException mue) {
                throw new ConfigurationException(
                        "Provided configuration file '" + configResource
                        + "' could not be mapped to a file", mue);
            }
        } else {//try to get from resource
            configResource = SETTINGS_FILENAME;
            LOGGER.debug("Try getting default configuration from resource '{}'", configResource);
            URL resourceURL = DataManagerSettings.class.getResource(configResource);
            if (resourceURL != null) {
                return resourceURL;
            } else {
                LOGGER.debug("Try getting default configuration from context classloader");
                resourceURL = Thread.currentThread().getContextClassLoader().getResource(SETTINGS_FILENAME);
                if (resourceURL != null) {
                    return resourceURL;
                }
            }
        }

        throw new ConfigurationException("Neither environment variable nor configuration file are pointing to a valid settings file.");
    }

    /**
     * Get a single string property.
     *
     * @param pKey The property key.
     * @param defaultValue The default value.
     *
     * @return The property value or the default value.
     */
    public String getStringProperty(String pKey, String defaultValue) {
        return settings.getString(pKey, defaultValue);
    }

    /**
     * Get a single long property.
     *
     * @param pKey The property key.
     * @param defaultValue The default value.
     *
     * @return The property value or the default value.
     */
    public Long getLongProperty(String pKey, long defaultValue) {
        return settings.getLong(pKey, defaultValue);
    }

    /**
     * Get a single integer property.
     *
     * @param pKey The property key.
     * @param defaultValue The default value.
     *
     * @return The property value or the default value.
     */
    public Integer getIntProperty(String pKey, int defaultValue) {
        return settings.getInt(pKey, defaultValue);
    }

    /**
     * Get a single boolean property.
     *
     * @param pKey The property key.
     * @param defaultValue The default value.
     *
     * @return The property value or the default value.
     */
    public Boolean getBooleanProperty(String pKey, boolean defaultValue) {
        return settings.getBoolean(pKey, defaultValue);
    }

    /**
     * Returns whether the application is running in productionMode or not.
     *
     * @return TRUE = production mode.
     */
    public boolean isProductionMode() {
        return productionMode;
    }
}
