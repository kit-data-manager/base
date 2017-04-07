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

import edu.kit.dama.commons.exceptions.InitializationError;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
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

    //Property keys for staging section
    public final static String STAGING_MAX_PARALLEL_TRANSFERS = "staging.maxParallelTransfers";//10;
    public final static String STAGING_MAX_PARALLEL_INGESTS = "staging.maxParallelIngests";//2;
    public final static String STAGING_MAX_PARALLEL_DOWNLOADS = "staging.maxParallelDownloads";//2;
    public final static String STAGING_MAX_DOWNLOAD_LIFETIME = "staging.maxDownloadLifetime";//60 * 60 * 24 * 7 seconds = 1 week;
    public final static String STAGING_MAX_INGEST_LIFETIME = "staging.maxIngestLifetime";//60 * 60 * 24 * 7 seconds = 1 week;

    //Property key for simple monitoring
    public static final String SIMON_CONFIG_LOCATION_ID = "simon.configLocation";

    //Property keys for general section
    public final static String GENERAL_REPOSITORY_NAME = "general.repositoryName";
    public final static String GENERAL_REPOSITORY_LOGO_URL = "general.repositoryLogoUrl";
    public final static String GENERAL_SYSTEM_MAIL_ADDRESS = "general.systemMailAddress";
    public final static String GENERAL_MAIL_SERVER = "general.mailServer";
    public final static String GENERAL_GLOBAL_SECRET = "general.globalSecret";//qr2I9Hyp0CBhUUXj
    public static final String GENERAL_BASE_URL_ID = "general.baseUrl"; //"http://localhost:8080/KITDM";
    public static final String PRODUCTION_MODE_ID = "general.productionMode"; //"true";

    public static final String AUTHORIZATION_CONFIG_ROOT = "authorization";
    public static final String AUDIT_CONFIG_ROOT = "audit";

    public static final String DATA_ORGANIZATION_CONFIG_ROOT = "dataOrganization";
    public static final String DATA_ORGANIZATION_DOWNLOAD_BLOCK_SIZE = "dataOrganization.download.blockSize";
    public static final String DATA_ORGANIZATION_DOWNLOAD_ZIP_COMPRESSION = "dataOrganization.download.compression";

    public static final String METADATA_MANAGEMENT_CONFIG_ROOT = "metaDataManagement";

    public static final String PERSISTENCE_AUTHORIZATION_PU_ID = "authorization.defaultPU";
    public static final String PERSISTENCE_STAGING_PU_ID = "staging.defaultPU";

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
        LOGGER.info("Initializing KIT Data Manager settings");
        URL configUrl = null;
        try {
            configUrl = getConfigurationURL();
            LOGGER.debug("Loading configuration from {}", configUrl);
            settings = new XMLConfiguration(configUrl);
            LOGGER.debug("Loading productionMode property");
            productionMode = getBooleanProperty(PRODUCTION_MODE_ID, true);
            //Read default persistence units for staging and authorization services
            loadDefaultPersistenceUnits();
            LOGGER.debug("Configuration initialized successfully.");
        } catch (ConfigurationException e) {
            //initialization not possible, quit everything.
            throw new InitializationError("Failed to load KIT Data Manager settings from URL " + configUrl, e);
        }
    }

    /**
     * Load the default persistence units from the datamanager.xml settings
     * file. The main reason for reading these entries at this point is due to
     * the fact, that the authorization service is not based on the default
     * metadata management of KIT Data Manager and has therefore no dependency
     * to the metadata management, where all other persistence settings are
     * read. Furthermore, for backwards compatibility it is required that
     * authorization and staging persistence unit ids are accessible by the keys
     * PERSISTENCE_AUTHORIZATION_PU_ID and PERSISTENCE_STAGING_PU_ID via the
     * datamanager settings.
     */
    private void loadDefaultPersistenceUnits() {
        List<HierarchicalConfiguration> persistenceUnitsList = settings.configurationsAt(METADATA_MANAGEMENT_CONFIG_ROOT + ".persistenceImplementations.persistenceImplementation.persistenceUnits.persistenceUnit");
        if (persistenceUnitsList == null) {
            persistenceUnitsList = new LinkedList<>();
        }
        LOGGER.debug("Configuration contains {} persistence units.", persistenceUnitsList.size());
        boolean haveStagingPU = false;
        boolean haveAuthorizationPU = false;
        for (HierarchicalConfiguration item : persistenceUnitsList) {
            String value = item.getString(".");
            String stagingAttribute = item.getString("[@staging]");
            String authorizationAttribute = item.getString("[@authorization]");
            if (Boolean.parseBoolean(stagingAttribute)) {
                if (!haveStagingPU) {
                    settings.addProperty(PERSISTENCE_STAGING_PU_ID, value);
                    haveStagingPU = true;
                } else {
                    LOGGER.warn("Default staging persistence unit already set to '{}'. Ignoring duplicate annotation of persistence unit '{}'", settings.getString(PERSISTENCE_STAGING_PU_ID), value);
                }
            }
            if (Boolean.parseBoolean(authorizationAttribute)) {
                if (!haveAuthorizationPU) {
                    settings.addProperty(PERSISTENCE_AUTHORIZATION_PU_ID, value);
                    haveAuthorizationPU = true;
                } else {
                    LOGGER.warn("Default authorization persistence unit already set to '{}'. Ignoring duplicate annotation of persistence unit '{}'", settings.getString(PERSISTENCE_AUTHORIZATION_PU_ID), value);
                }
            }
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
            LOGGER.debug("Try to read resource from {}", configResource);

            //just try if configResource is not null
            resource = Thread.currentThread().getContextClassLoader().getResource(configResource);
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
     * Get a sub-configuration located at path pConfiguration or null if no
     * sub-configuration was found. Valid paths are relative to the root node
     * 'config', e.g. <i>dataOrganization</i> or <i>staging</i>.
     *
     * @param pConfiguration The sub-configuration path.
     *
     * @return The sub-configuration.
     */
    public Configuration getSubConfiguration(String pConfiguration) {
        try {
            return settings.configurationAt(pConfiguration);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("No configuration node found for subpath '" + pConfiguration + "'");
            return null;
        }
    }

    /**
     * Get a list of sub-configurations located at path pConfiguration or an
     * empty list if no sub-configurations were found. Valid paths are relative
     * to the root node 'config', e.g.
     * <i>dataOrganization.dataOrganizerAdapter</i> or
     * <i>staging</i>.
     *
     * @param pConfiguration The sub-configuration's path.
     *
     * @return The list of sub-configurations.
     */
    public List<Configuration> getSubConfigurations(String pConfiguration) {
        List<Configuration> result = new ArrayList<>();
        try {
            result = settings.configurationsAt(pConfiguration);
        } catch (IllegalArgumentException ex) {
            LOGGER.warn("No configuration nodes found for subpath '" + pConfiguration + "'");
        }
        return result;
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
