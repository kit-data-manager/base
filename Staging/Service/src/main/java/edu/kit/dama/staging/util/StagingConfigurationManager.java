/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
package edu.kit.dama.staging.util;

import edu.kit.dama.staging.interfaces.IDataOrganizationServiceAdapter;
import edu.kit.dama.staging.interfaces.IIngestInformationServiceAdapter;
import edu.kit.dama.staging.interfaces.IStorageVirtualizationServiceAdapter;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.staging.exceptions.StagingIntitializationException;
import edu.kit.dama.staging.interfaces.IDownloadInformationServiceAdapter;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.staging.ap.AbstractStagingAccessPoint;
import edu.kit.dama.staging.interfaces.IConfigurableAdapter;
import edu.kit.dama.util.DataManagerSettings;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.naming.resources.DirContextURLStreamHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the configuration of a staging service. The
 * configuration is realized via XML. The content should look as follows:
 *
 * <pre>
 * <staging>
 *<adapters>
 * <dataOrganizationAdapter
 * class="edu.kit.dama.rest.staging.impl.DefaultDataOrganizationServiceAdapter"
 * target="LOCAL"/>
 * <ingestInformationServiceAdapter
 * class="edu.kit.dama.rest.staging.ingest.impl.DefaultIngestInformationServiceAdapter"
 * target="LOCAL"/>
 * <storageVirtualizationAdapter
 * class="edu.kit.dama.rest.staging.impl.DefaultDataVirtualizationAdapter"
 * target="LOCAL"/>
 * </adapters>
 * </staging>
 * </pre>
 *
 * What you see is the definition of several adapters. Each adapter is
 * responsible for accessing one service needed for staging operations. The
 * target of each adapter defines, if the underlaying service is accessed
 * locally ('LOCAL') or by any other mechanism (e.g. via REST-service).
 * Currently either local or URL-based mechanisms are supported.
 *
 * For custom configuration of single adapters, additional XML entries may be
 * added after each adapter entry, e.g.:
 *
 * <pre>
 * <dataOrganizationAdapter
 * class="edu.kit.dama.rest.staging.impl.DefaultDataOrganizationServiveAdapter"
 * target="LOCAL">
 * <myCustomTag>myCustomValue</myCustomTag>
 * </dataOrganizationAdapter>
 * </pre>
 *
 * Be aware, the the adapter attributes 'class' and 'target' are mandatory, even
 * for custom configuration.
 *
 * @author jejkal
 */
public final class StagingConfigurationManager {

  /**
   * The logger instance
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(StagingConfigurationManager.class);
  private final static String STAGING_CONFIG_ROOT = "staging";
  private final static String STAGING_PU_PROPERTY = "persistenceUnit";
  /**
   * XML element for providing the data organization adapter
   */
  private final static String DATA_ORGANIZATION_ADAPTER_ID = "dataOrganizationAdapter";
  /**
   * XML element for providing the ingest information service adapter
   */
  private final static String INGEST_INFORMATION_SERVICE_ADAPTER_ID = "ingestInformationServiceAdapter";
  /**
   * XML element for providing the download information service adapter
   */
  private final static String DOWNLOAD_INFORMATION_SERVICE_ADAPTER_ID = "downloadInformationServiceAdapter";
  /**
   * XML element for providing the storage virtualization adapter
   */
  private final static String STORAGE_VIRTUALIZATION_ADAPTER_ID = "storageVirtualizationAdapter";
  /**
   * The singleton for this class
   */
  private static StagingConfigurationManager singleton = null;
  /**
   * The actual configuration which contains all properties
   */
  private SubnodeConfiguration stagingConfig = null;
  private String stagingPU = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit");
  private IDataOrganizationServiceAdapter dataOrganizationAdapter = null;
  private IIngestInformationServiceAdapter ingestInformationAdapter = null;
  private IDownloadInformationServiceAdapter downloadInformationAdapter = null;
  private IStorageVirtualizationServiceAdapter storageVirtualizationAdapter = null;
  private String restUrl;
  /*private File tracBasePath;
   private String tracBaseUrl;*/

  static {
    java.net.URLStreamHandlerFactory myFactory = new java.net.URLStreamHandlerFactory() {
      @Override
      public java.net.URLStreamHandler createURLStreamHandler(final String protocol) {
        if ("sftp".compareTo(protocol) == 0) {
          return new sun.net.www.protocol.sftp.Handler();
        } else if ("gsiftp".compareTo(protocol) == 0) {
          return new sun.net.www.protocol.gsiftp.Handler();
        }
        return null;
      }
    };

    //register protocol handlers in tomcat mode
    try {
      LOGGER.info("Try to register stream handler factory in Tomcat 7 environment");
      if (Class.forName("org.apache.naming.resources.DirContextURLStreamHandlerFactory") != null) {
        //we are in tomcat
        DirContextURLStreamHandlerFactory.addUserFactory(myFactory);
        LOGGER.info("Factory successfully registered.");
      }
    } catch (Throwable t) {
      LOGGER.info("Factory registration failed, probably we are not within Tomcat 7. Trying default registration...");
      //class not found? not in tomcat? are we in tomcat 6? If the next call fails, nothing will help...
      //try to use default handler registration...
      try {
        java.net.URL.setURLStreamHandlerFactory(myFactory);
      } catch (Error e) {
        LOGGER.error("Factory registration not possible. This environment is not supported!");
        throw e;
      }
    }
  }

  /**
   * Get the configuration manager singleton.
   *
   * @return The singleton
   */
  public static synchronized StagingConfigurationManager getSingleton() {
    if (singleton == null) {
      singleton = new StagingConfigurationManager();
    }
    return singleton;
  }

  /**
   * Default constructor which implicitly loads the configuration from any of
   * the supported locations
   */
  StagingConfigurationManager() {
    configure();
  }

  /**
   * Perform the configuration for the staging. This covers reading the
   * configuration from one supported source (env. variable, file, resource) and
   * the actual setup. If anything fails the staging will not work and a
   * StagingIntitializationException is thrown.
   */
  private void configure() {
    boolean initialized = false;
    try {
      LOGGER.debug("Searching for staging configuration URL");
      URL configUrl = DataManagerSettings.getConfigurationURL();
      if (configUrl == null) {
        throw new StagingIntitializationException("No configuration file found.");
      }
      LOGGER.debug("Trying to configure staging from URL {}", configUrl);
      stagingConfig = new HierarchicalConfiguration(new XMLConfiguration(configUrl)).configurationAt(STAGING_CONFIG_ROOT);

      initialized = true;
    } catch (org.apache.commons.configuration.ConfigurationException ce) {
      LOGGER.warn("Failed to configure staging using provided configuration", ce);
    }

    if (!initialized) {
      throw new StagingIntitializationException("Staging not initialized. Probably, the provided configuration is invalid.");
    }

    LOGGER.debug("Configuring staging persistence");
    configurePU(stagingConfig);

    LOGGER.debug("Obtaining staging access points from database");
    List<StagingAccessPointConfiguration> accessPoints = StagingConfigurationPersistence.getSingleton(stagingPU).findAllAccessPointConfigurations();
    for (StagingAccessPointConfiguration accessPoint : accessPoints) {
      configureAccessPoint(accessPoint);
    }

    LOGGER.debug("Configurung external adapters");
    configureAdapters(stagingConfig);
    LOGGER.debug("Configuring remote access");
    configureRemoteAccess(stagingConfig);
    LOGGER.debug("Configuring mail notification");
    configureMailNotifier();
    LOGGER.debug("Configuration finished.");
  }

   /**
   * Configure an access point using the provided configuration. During the
   * configuration the implementation class will be checked as well as
   * localBasePath and remoteBaseURL. Also the properties are deserialized for
   * testing purposes. If anything goes wrong, a StagingIntitializationException
   * is thrown.
   *
   * @param pAccessPoint The AccessPoint to configure/check.
   */
  private void configureAccessPoint(StagingAccessPointConfiguration pAccessPoint) {
    if (pAccessPoint == null) {
      return;
    }

    LOGGER.debug("Checking access point {}", pAccessPoint.getName());
    String className = pAccessPoint.getImplementationClass();
    if (className == null) {
      throw new StagingIntitializationException("Access point class for staging access point '" + pAccessPoint.getUniqueIdentifier() + "' is null");
    }

    try {
      if (Class.forName(className) != null) {
        LOGGER.debug("Checked access point {}", className);
      }
    } catch (ClassNotFoundException ex) {
      throw new StagingIntitializationException("Failed to find class " + className + " for staging access point '" + pAccessPoint.getUniqueIdentifier() + "'", ex);
    }

    try {
      pAccessPoint.getPropertiesAsObject();
    } catch (IOException ex) {
      throw new StagingIntitializationException("Failed to deserialize custom properties for staging access point '" + pAccessPoint.getUniqueIdentifier() + "'", ex);
    }

    String localBasePath = pAccessPoint.getLocalBasePath();
    if (localBasePath == null) {
      throw new StagingIntitializationException("Local base path for staging access point '" + pAccessPoint.getUniqueIdentifier() + "' is null.");
    } else {
      LOGGER.debug("Checked local path for access point {}", className);
      if (!new File(localBasePath).exists()) {
        LOGGER.debug("Local path at {} does not exist. Trying to create it.", localBasePath);
        if (new File(localBasePath).mkdirs()) {
          LOGGER.debug("Local base path successfully created.");
        } else {
          throw new StagingIntitializationException("Failed to create local base path for staging access point '" + pAccessPoint.getUniqueIdentifier() + "' at location " + localBasePath + ".");
        }
      }
    }

    String remoteBaseUrl = pAccessPoint.getRemoteBaseUrl();
    if (remoteBaseUrl == null) {
      throw new StagingIntitializationException("Remote base URL for staging access point '" + pAccessPoint.getUniqueIdentifier() + "' is null.");
    }
    try {
      LOGGER.debug("Remote base URL {} successfully checked.", new URL(remoteBaseUrl));
    } catch (MalformedURLException ex) {
      throw new StagingIntitializationException("Failed to check remote base URL " + remoteBaseUrl + " for staging access point '" + pAccessPoint.getUniqueIdentifier() + "'.", ex);
    }
  }

  /**
   * Read a single AccessPoint found within the configuration. AccessPoints are
   * responsible to provide a way to access the cache. Each AccessPoint must
   * contain some default properties and may contain custom properties for
   * special setup.
   *
   * @param pAccessPointConfig The configuration including all custom
   * properties.
   *
   * @return The access point instance.
   *
   * @throws ConfigurationException If the configuration could not be performed.
   */
  private AbstractStagingAccessPoint setupAccessPoint(StagingAccessPointConfiguration pAccessPointConfig) throws ConfigurationException {
    if (pAccessPointConfig == null) {
      throw new ConfigurationException("No valid argument for pAccessPointConfig provided");
    }

    String accessPointClass = pAccessPointConfig.getImplementationClass();
    //check handler class
    if (accessPointClass == null || accessPointClass.length() < 1) {
      throw new ConfigurationException("No valid implementation class found for AccessPoint '" + pAccessPointConfig.getUniqueIdentifier() + "'");
    }

    LOGGER.debug("Creating AccessPoint instance for ID '{}'", pAccessPointConfig.getUniqueIdentifier());
    LOGGER.debug(" * AccessPoint class: '{}'", accessPointClass);
    try {
      //create and configure instance
      Class clazz = Class.forName(accessPointClass);
      AbstractStagingAccessPoint instance = (AbstractStagingAccessPoint) clazz.getConstructor(StagingAccessPointConfiguration.class).newInstance(pAccessPointConfig);

      //perform custom configuration
      LOGGER.debug("Performing custom setup for AccessPoint with ID '{}'", pAccessPointConfig.getUniqueIdentifier());
      instance.setup(false);
      return instance;
    } catch (ClassNotFoundException cnfe) {
      throw new ConfigurationException("Failed to locate AccessPoint class for ID '" + pAccessPointConfig.getUniqueIdentifier() + "'", cnfe);
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException ie) {
      throw new ConfigurationException("Failed to instantiate and configure AccessPoint for ID '" + pAccessPointConfig.getUniqueIdentifier() + "'", ie);
    } catch (NoSuchMethodException nsme) {
      throw new ConfigurationException("Invalid AccessPoint classt for ID '" + pAccessPointConfig.getUniqueIdentifier() + "'", nsme);
    } catch (ClassCastException cce) {
      throw new ConfigurationException("AccessPoint instance for ID '" + pAccessPointConfig.getUniqueIdentifier() + "' does not implement adapter interface", cce);
    }
  }

  /**
   * Configure staging persistence.
   *
   * @param pConfig Staging configuration.
   */
  private void configurePU(Configuration pConfig) {
    LOGGER.debug("Configuring staging persistence");
    stagingPU = pConfig.getString(STAGING_PU_PROPERTY, DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit"));
  }

  /**
   * Obtain all adapters from the provided configuration. Adapters are used to
   * access dependent services, e.g. for DataOrganization, Ingest-,
   * DownloadInformation and StorageVirtualization. The method reads all adapter
   * configurations and tries to create an instance for each adapter.
   * Non-default adapter properties are forwarded to the adapter and will be
   * used for extended configuration. If everything works fine, all adapters are
   * accessible via appropriate getters at this StagingConfigManager instance.
   * If something goes wrong, in StagingInitializationException is thrown and
   * the staging won't work at all.
   *
   * @param pConfig The configuration from which the adapters are read.
   */
  private void configureAdapters(Configuration pConfig) {
    try {//try to load adapters
      dataOrganizationAdapter = createAdapterInstance(pConfig, DATA_ORGANIZATION_ADAPTER_ID);
      ingestInformationAdapter = createAdapterInstance(pConfig, INGEST_INFORMATION_SERVICE_ADAPTER_ID);
      downloadInformationAdapter = createAdapterInstance(pConfig, DOWNLOAD_INFORMATION_SERVICE_ADAPTER_ID);
      storageVirtualizationAdapter = createAdapterInstance(pConfig, STORAGE_VIRTUALIZATION_ADAPTER_ID);
    } catch (ConfigurationException ce) {
      throw new StagingIntitializationException("Failed to initialize staging. Configuration of at least one adapter failed", ce);
    }
  }

  /**
   * Configures all settings responsible for remote access. These settings are:
   * REST Url for accessing the staging REST service, the local path where
   * TransferTaskContainers are stored and the URL belonging to this local path
   * where the user can access the TransferTaskContainer remotely.
   *
   * @param pConfig The configuration from which the remote access configuration
   * is obtained.
   */
  private void configureRemoteAccess(Configuration pConfig) {
    LOGGER.debug("Configuring remote access");
    LOGGER.debug(" - Obtaining REST service Url");
    String configuredRestUrl = pConfig.getString("remoteAccess.restUrl");
    if (configuredRestUrl == null) {
      throw new StagingIntitializationException("No remote access URL configured");
    }
    try {
      LOGGER.debug("Setting REST access URL to {}", new URL(configuredRestUrl));
      this.restUrl = configuredRestUrl;
    } catch (MalformedURLException ex) {
      throw new StagingIntitializationException("Failed to read remote REST access URL from value " + configuredRestUrl, ex);
    }
  }

  /**
   * Configures the mail notification. The configuration is taken from the
   * global configuration in DataManagerSettings.
   */
  private void configureMailNotifier() {
    LOGGER.debug("Configuring mail notifier");
    String mailServer = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_MAIL_SERVER, null);
    String mailSender = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_SYSTEM_MAIL_ADDRESS, null);
    //try to configure notifier...if one argument is not set, mail notification won't work
    MailNotificationHelper.configure(mailServer, mailSender);
  }

  /**
   * Returns the URL configured for accessing the staging REST service.
   *
   * @return The REST service URL.
   */
  public String getRestServiceUrl() {
    return restUrl;
  }

  /**
   * Returns the configured PU for staging.
   *
   * @return The PU name.
   */
  public String getStagingPersistenceUnit() {
    return stagingPU;
  }

  /**
   * Get all available access point IDs (internal identifiers)
   *
   * @return An list of all AccessPoint IDs
   */
  public List<String> getAccessPointIDs() {
    LOGGER.debug("Getting all access point IDs");
    return StagingConfigurationPersistence.getSingleton(stagingPU).getAccessPointIds();
  }

  /**
   * Get an AccessPoint Id by its plain name.
   *
   * @param pAccessPointName The AccessPoint name.
   *
   * @return The ID of the AccessPoint.
   */
  public String getAccessPointIdByName(String pAccessPointName) {
    String accessPointName = pAccessPointName.trim();
    LOGGER.debug("Getting AccessPoint for name '{}'", accessPointName);

    StagingAccessPointConfiguration accessPointConfig = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByName(accessPointName);
    if (accessPointConfig == null) {
      LOGGER.warn("No AccessPoint found for name '" + accessPointName + "'");
      return null;
    }
    return accessPointConfig.getUniqueIdentifier();
  }

  /**
   * Get the name of the AccessPoint.
   *
   * @param pAccessPointId The internal AccessPoint's ID.
   *
   * @return The name of the AccessPoint.
   */
  public String getAccessPointName(String pAccessPointId) {
    String accessPointId = pAccessPointId.trim();
    LOGGER.debug("Getting name of AccessPoint with ID '{}'", accessPointId);

    StagingAccessPointConfiguration accessPoint = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByUniqueIdentifier(accessPointId);

    String result = null;
    if (accessPoint != null) {
      result = accessPoint.getName();
    } else {
      LOGGER.warn("Tried to access not-existing AccessPoint with ID '{}'", accessPointId);
    }
    return result;
  }

  /**
   * Get a AccessPoint by its unique identifier ID.
   *
   * @param pAccessPointId The AccessPoint's ID.
   *
   * @return The AccessPoint for the provided ID.
   */
  public AbstractStagingAccessPoint getAccessPointById(String pAccessPointId) {
    String accessPointId = pAccessPointId.trim();
    LOGGER.debug("Getting AccessPoint for Id '{}'", accessPointId);

    StagingAccessPointConfiguration accessPoint = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByUniqueIdentifier(accessPointId);
    try {
      return setupAccessPoint(accessPoint);
    } catch (ConfigurationException ex) {
      LOGGER.error("Failed to create AccessPoint by id " + accessPointId, ex);
      return null;
    }
  }

  /**
   * Get all available AccessPoint configurations for the provided group id.
   *
   * @param pGroupId The group id to which the AccessPoints are associated.
   *
   * @return An list of all AccessPoints.
   */
  public List<StagingAccessPointConfiguration> getAccessPointConfigurations(String pGroupId) {
    LOGGER.debug("Getting all AccessPoint configurations for group {}", pGroupId);
    return StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationsForGroup(pGroupId);
  }

  /**
   * Get a AccessPoint by its name.
   *
   * @param pAccessPointName The AccessPoint's name.
   *
   * @return The AccessPoint for the provided name.
   */
  public AbstractStagingAccessPoint getAccessPointByName(String pAccessPointName) {
    LOGGER.debug("Getting AccessPoint for name '{}'", pAccessPointName);

    StagingAccessPointConfiguration accessPoint = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByName(pAccessPointName);
    try {
      return setupAccessPoint(accessPoint);
    } catch (ConfigurationException ex) {
      LOGGER.error("Failed to create AccessPoint by name " + pAccessPointName, ex);
      return null;
    }
  }

  /**
   * Get a AccessPoint configuration by its name.
   *
   * @param pAccessPointName The AccessPoint configuration's name.
   *
   * @return The AccessPoint configuration for the provided name.
   */
  public StagingAccessPointConfiguration getAccessPointConfigurationByName(String pAccessPointName) {
    LOGGER.debug("Getting AccessPoint configuration for name '{}'", pAccessPointName);
    StagingAccessPointConfiguration accessPoint = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByName(pAccessPointName);
    if (accessPoint == null) {
      LOGGER.warn("No AccessPoint configuration found for name '{}'", pAccessPointName);
      return null;
    }
    return accessPoint;
  }

  /**
   * Get a AccessPoint configuration by its ID.
   *
   * @param pAccessPointId The AccessPoint configuration's ID.
   *
   * @return The AccessPoint configuration for the provided ID.
   */
  public StagingAccessPointConfiguration getAccessPointConfigurationById(String pAccessPointId) {
    LOGGER.debug("Getting AccessPoint configuration for id '{}'", pAccessPointId);
    StagingAccessPointConfiguration accessPoint = StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByUniqueIdentifier(pAccessPointId);
    if (accessPoint == null) {
      LOGGER.warn("No AccessPoint  configuration found for id '{}'", pAccessPointId);
      return null;
    }
    return accessPoint;
  }

  /**
   * Check whether the provided ID belongs to an existing AccessPoint.
   *
   * @param pAccessPointId The ID to check.
   *
   * @return True if the ID belongs to an existing AccessPoint.
   */
  public boolean isExistingAccessPoint(String pAccessPointId) {
    LOGGER.debug("Checking if AccessPoint with ID '{}' exists", pAccessPointId);
    return StagingConfigurationPersistence.getSingleton(stagingPU).findAccessPointConfigurationByUniqueIdentifier(pAccessPointId) != null;
  }

  ////////////////////////ADAPTER GETTERS/////////////////////////////////////////////
  /**
   * Returns the configured data organization adapter.
   *
   * @return An implementations of IDataOrganizationServiceAdapter.
   */
  public IDataOrganizationServiceAdapter getDataOrganizationAdapter() {
    LOGGER.debug("Getting configured DataOrganizationAdapter");
    return dataOrganizationAdapter;
  }

  /**
   * Returns the configured ingest information service adapter.
   *
   * @return An implementations of IIngestInformationServiceAdapter.
   */
  public IIngestInformationServiceAdapter getIngestInformationServiceAdapter() {
    LOGGER.debug("Getting configured IngestInformationServiceAdapter");
    return ingestInformationAdapter;
  }

  /**
   * Returns the configured download information service adapter.
   *
   * @return An implementations of IDownloadInformationServiceAdapter.
   */
  public IDownloadInformationServiceAdapter getDownloadInformationServiceAdapter() {
    LOGGER.debug("Getting configured DownloadInformationServiceAdapter");
    return downloadInformationAdapter;
  }

  /**
   * Returns the configured storage virtualization adapter.
   *
   * @return An implementations of IStorageVirtualizationServiceAdapter.
   */
  public IStorageVirtualizationServiceAdapter getStorageVirtualizationAdapter() {
    LOGGER.debug("Getting configured StorageVirtualizationAdapter");
    return storageVirtualizationAdapter;
  }

  /**
   * Creates an instance of one of the needed adapters. The type of the instance
   * is defined by the return value and by pAdapterId. Both definitions must fit
   * to avoid configuration exceptions.
   *
   * @param <T> Adapter class implementing IConfigurableAdapter.
   * @param pConfig The configuration used to obtain the adapter configurations.
   * @param pAdapterId The ID of the adapter. Depending on the ID, all
   * properties are obtained and logging is performed.
   *
   * @return An instance of the created adapter which extends
   * IConfigurableAdapter.
   *
   * @throws ConfigurationException if anything goes wrong (e.g. T and
   * pAdapterId do not fit, no entry for pAdapterId was found, the provided
   * adapter class was not found, instantiation or configuration failed...).
   */
  private <T extends IConfigurableAdapter> T createAdapterInstance(Configuration pConfig, String pAdapterId) throws ConfigurationException {
    try {
      String adapterClass = pConfig.getString("adapters." + pAdapterId + "[@class]");

      //check adapter class
      if (adapterClass == null || adapterClass.length() < 1) {
        throw new ConfigurationException("No valid adapter class attribute found for adapter '" + pAdapterId + "'");
      }

      String adapterTarget = pConfig.getString("adapters." + pAdapterId + "[@target]");
      Configuration customConfig = pConfig.subset("adapters." + pAdapterId);

      LOGGER.debug("Creating adapter instance for '{}'", pAdapterId);
      LOGGER.debug(" * Adapter class: '{}'", adapterClass);
      LOGGER.debug(" * Adapter target: '{}'", adapterTarget);

      //create and configure instance
      Class clazz = Class.forName(adapterClass);
      Object instance;
      if (adapterTarget == null || adapterTarget.length() < 1 || adapterTarget.equalsIgnoreCase("local")) {//no target provided...hopefully the adapter can be instantiated without a target
        instance = clazz.getConstructor().newInstance();
      } else {//target provided, use it for instantiation
        try {
          URL target = new URL(adapterTarget);
          instance = clazz.getConstructor(URL.class).newInstance(target);
        } catch (MalformedURLException mue) {
          throw new ConfigurationException("Provided adapter target '" + adapterTarget + "'is no valid URL", mue);
        }
      }
      if (customConfig != null && !customConfig.isEmpty()) {//try custom configuration
        ((T) instance).configure(customConfig);
      }
      return (T) instance;
    } catch (ClassNotFoundException cnfe) {
      throw new ConfigurationException("Failed to locate adapter class for ID '" + pAdapterId + "'", cnfe);
    } catch (InstantiationException ie) {
      throw new ConfigurationException("Failed to instantiate and configure adapter for ID '" + pAdapterId + "'", ie);
    } catch (IllegalAccessException iae) {
      throw new ConfigurationException("Failed to instantiate and configure adapter for ID '" + pAdapterId + "'", iae);
    } catch (InvocationTargetException ite) {
      throw new ConfigurationException("Failed to instantiate and configure adapter for ID '" + pAdapterId + "'", ite);
    } catch (NoSuchMethodException nsme) {
      throw new ConfigurationException("Invalid adapter class for ID '" + pAdapterId + "'", nsme);
    } catch (ClassCastException cce) {
      throw new ConfigurationException("Adapter instance for ID '" + pAdapterId + "' does not implement adapter interface", cce);
    }
  }

//  public static void main(String[] args) throws Exception {
//
//        String data = "\\/SYS_LSDF/LSDF_ADMIN/3900";
//        Matcher m = Pattern.compile("[/|\\\\]+(.*)").matcher(data);
//        if (m.find()) {
//            System.out.println(m.groupCount());
//            System.out.println(m.group(1));
//        }
//        // System.out.println(FilenameUtils.getPrefix("/SYS_LSDF/LSDF_ADMIN/390"));
//    }
}
