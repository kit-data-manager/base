/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
package edu.kit.dama.mdm.core;

import edu.kit.dama.util.DataManagerSettings;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface for the MetaDataManagement. Implementation as abstract factory.
 * Class is implemented as a singleton.
 *
 * @author hartmann-v
 */
public final class MetaDataManagement {

// <editor-fold defaultstate="collapsed" desc="Declaration of attributes">
  /**
   * For logging purposes.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataManagement.class);
  /**
   * Implementation of the MetaDataManagement. There should be only one instance
   * available (singleton).
   */
  private final static MetaDataManagement SINGLETON = new MetaDataManagement();
  /**
   * Constants for the config files: filename.
   */
  //private static final String CONFIG_FILE = "datamanager.xml";
  /**
   * Constants for the config files: root of MDM configuration.
   */
  private static final String CONFIG_ROOT = "MetaDataManagement";
  /**
   * Constants for the config files: subpath to persistence implementations.
   */
  private static final String CONFIG_PERSISTENCE_IMPL = "PersistenceImplementations.PersistenceImplementation";
  /**
   * Constants for the config files: subpath to persistence units.
   */
  private static final String CONFIG_PERSISTENCE_UNIT = "PersistenceUnits.PersistenceUnit";
  /**
   * Constants for the config files: key for name of persistence implementation.
   */
  private static final String CONFIG_PERSISTENCE_NAME = "Name";
  /**
   * Constants for the config files: key for class of persistence
   * implementation.
   */
  private static final String CONFIG_PERSISTENCE_CLASS = "Class";
  /**
   * Constants for the config files: key for default persistence implementation.
   */
  private static final String CONFIG_DEFAULT_PERSISTENCE = "Default";
  /**
   * Map of the persistenceImplementation names and their persistence units.
   */
  private volatile Map<String, List<String>> persistenceUnitMap = null;
  /**
   * Map of the persistenceImplementation names and their default persistence
   * units.
   */
  private volatile Map<String, String> persistenceUnitDefaultMap = null;
  /**
   * Map of the persistenceImplementation names and their implementation
   * classes.
   */
  private volatile Map<String, IPersistenceFactory> persistenceClassMap = null;
  /**
   * Name of the default implementation.
   */
  private volatile String defaultImplementation = null;
  
// </editor-fold>

  /**
   * Private Constructor.
   */
  private MetaDataManagement() {
    LOGGER.info("Initializing MetaDataManagement");
    loadConfiguration();
  }

  /**
   * Get single instance of MetaDataManagement (singleton).
   *
   * @return instance of MetaDataManagement (singleton).
   */
  public static MetaDataManagement getMetaDataManagement() {
    return SINGLETON;
  }

  /**
   * Get single instance for manage queries of meta data. throws
   * IllegalArgumentException if implementation/unit is not defined in
   * configuration.
   *
   * @param persistenceImplementation Label of the implementation which should
   * be used
   * @return Instance for managing the associated objects.
   */
  private synchronized IPersistenceFactory getPersistenceFactory(final String persistenceImplementation) {
    IPersistenceFactory returnValue;
    // Test for existence of implementation
    if (!persistenceUnitMap.containsKey(persistenceImplementation)) {
      throw new IllegalArgumentException("Persistence implementation '" + persistenceImplementation + "' doesn't exist!");
    }
    // Create instance of IPersistenceFactory
    returnValue = persistenceClassMap.get(persistenceImplementation);

    return returnValue;
  }

  /**
   * Get single instance for manage queries of meta data. Throws an
   * IllegalArgumentException if implementation/unit is not defined in
   * configuration.
   *
   * @param persistenceUnit Label of the persistence unit to use.
   * @return Instance for managing the associated objects.
   */
  public synchronized IMetaDataManager getMetaDataManager(final String persistenceUnit) {
    return getMetaDataManager(getDefaultImplementation(), persistenceUnit);
  }

  /**
   * Get a single instance for manage queries of meta data. Throws an
   * IllegalArgumentException if implementation/unit is not defined in
   * configuration.
   *
   * @return Instance for managing the associated objects.
   */
  public synchronized IMetaDataManager getMetaDataManager() {
    return getMetaDataManager(getDefaultImplementation(), getDefaultPersistenceUnit());
  }

  /**
   * Get single instance for manage queries of meta data. Throws an
   * IllegalArgumentException if implementation/unit is not defined in
   * configuration.
   *
   * @param persistenceImplementation Label of the implementation which should
   * be used
   * @param persistenceUnit Label of the persistence unit to use.
   * @return Instance for managing the associated objects.
   */
  public synchronized IMetaDataManager getMetaDataManager(final String persistenceImplementation, final String persistenceUnit) {
    IPersistenceFactory persistenceFactory;

    // Test for existence of persistence unit
    if (!persistenceUnitMap.get(persistenceImplementation).contains(persistenceUnit)) {
      throw new IllegalArgumentException("Persistence unit '" + persistenceUnit + "' for implementation '" + persistenceImplementation + "' doesn't exist!");
    }

    persistenceFactory = getPersistenceFactory(persistenceImplementation);

    return persistenceFactory.getMetaDataManager(persistenceUnit);
  }

  /**
   * Get labels of all configured implementations.
   *
   * @return list with all implementation labels.
   */
  public List<String> getAllImplementations() {
    return new ArrayList<>(persistenceClassMap.keySet());
  }

  /**
   * Get labels of all configured persistence units for the given
   * implementation. Return 'null' if implementation doesn't exist.
   *
   * @param persistenceImplementation label of the persistence implementation.
   * @return list with all persistence units.
   */
  public List<String> getAllPersistenceUnits(final String persistenceImplementation) {
    return persistenceUnitMap.get(persistenceImplementation);
  }

  /**
   * Get label of the persistence unit defined as default for the given
   * implementation. Return 'null' if implementation doesn't exist.
   *
   * @param persistenceImplementation label of the persistence implementation.
   * @return default persistence unit.
   */
  public String getDefaultPersistenceUnit(final String persistenceImplementation) {
    return persistenceUnitDefaultMap.get(persistenceImplementation);
  }

  /**
   * Get labels of all configured persistence units for the default
   * implementation.
   *
   * @return list with all persistence units.
   */
  public List<String> getAllPersistenceUnits() {
    return persistenceUnitMap.get(getDefaultImplementation());
  }

  /**
   * Get label of the persistence unit defined as default of the default
   * implementation. Return 'null' if implementation doesn't exist.
   *
   * @return default persistence unit.
   */
  public String getDefaultPersistenceUnit() {
    return persistenceUnitDefaultMap.get(getDefaultImplementation());
  }

  /**
   * Warning: This method is only for testing purposes. Reseting during run time
   * will possibly loss data base informations.
   */
  static void reset() {
    LOGGER.warn("Resetting configuration. This is intended to be done only in TEST MODE!");
    SINGLETON.defaultImplementation = null;
    SINGLETON.loadConfiguration();
  }

  /**
   * Get default implementation.
   *
   * @return default implementation.
   */
  public String getDefaultImplementation() {
    return defaultImplementation;
  }

  /**
   * Load configuration from XML-File
   */
  private void loadConfiguration() {
    String firstImplementation = null;
    String firstPersistenceUnit = null;
    HierarchicalConfiguration hc = null;
    List<String> persistenceUnits = null;
    URL configURL = null;
    try {
      configURL = DataManagerSettings.getConfigurationURL();
      LOGGER.debug("Loading configuration from {}", configURL);
      hc = new HierarchicalConfiguration(new XMLConfiguration(configURL));
      LOGGER.debug("Configuration successfully loaded");
    } catch (ConfigurationException ex) {
      // error in configuration
      // reason see debug log message:
      LOGGER.error("Failed to load configuration.", ex);
      throw new RuntimeException(ex);
    }
    SubnodeConfiguration configurationAt = hc.configurationAt(CONFIG_ROOT);
    List fields = configurationAt.configurationsAt(CONFIG_PERSISTENCE_IMPL);
    LOGGER.debug("Found {} configured persistence implementations", fields.size());
    persistenceUnitMap = new HashMap<>();
    persistenceClassMap = new HashMap<>();
    persistenceUnitDefaultMap = new HashMap<>();

    String implementationName;
    IPersistenceFactory iPersistenceFactory = null;
    for (Iterator it = fields.iterator(); it.hasNext();) {
      HierarchicalConfiguration sub = (HierarchicalConfiguration) it.next();
      LOGGER.debug("Reading sub-configuration");
      // First get all persistence units.
      persistenceUnits = new ArrayList<>();
      try {
        List<HierarchicalConfiguration> persistenceUnitsList = sub.configurationsAt(CONFIG_PERSISTENCE_UNIT);
        if (persistenceUnitsList == null) {
          persistenceUnitsList = new LinkedList<>();
        }
        LOGGER.debug("Configuration contains {} persistence units.", persistenceUnitsList.size());
        firstPersistenceUnit = null;
        for (HierarchicalConfiguration item : persistenceUnitsList) {
          String value = item.getString(".");
          String attribute = item.getString("[@default]");
          LOGGER.debug("PersistenceUnit found: " + value);
          LOGGER.debug("@default = {}", attribute);

          if (Boolean.parseBoolean(attribute)) {
            if (firstPersistenceUnit == null) {
              LOGGER.debug("{} is used as default persistence unit.", value);
              firstPersistenceUnit = value;
            } else {
              LOGGER.warn("{} is an additional persistence unit defined as default. We'll ignore this.", value);
            }
          }
          LOGGER.debug("Adding persistence unit to list of units.");
          persistenceUnits.add(value);
        }
      } catch (Exception any) {
        LOGGER.error("Failed to read persistence units.", any);
      }
      LOGGER.debug("firstPersistenceUnit: " + firstPersistenceUnit);
      if ((persistenceUnits.size() > 0) && (firstPersistenceUnit == null)) {
        LOGGER.debug("No default persistence unit defined. Using first entry ({})", persistenceUnits.get(0));
        firstPersistenceUnit = persistenceUnits.get(0);
      }
      LOGGER.debug("Getting implementation name.");
      implementationName = sub.getString(CONFIG_PERSISTENCE_NAME);
      LOGGER.debug("Implementation name '{}' found.", implementationName);
      if (firstImplementation == null) {
        LOGGER.debug("Using implementation '{}' as first implementation.", implementationName);
        firstImplementation = implementationName;
      }
      LOGGER.debug("Testing implementation '{}'", implementationName);

      if (sub.containsKey(CONFIG_DEFAULT_PERSISTENCE)) {
        LOGGER.debug("'{}' is configured as default implementation.", implementationName);
        if (defaultImplementation != null) {
          LOGGER.warn("{} is an additional implementation defined as default. We'll ignore this.", implementationName);
        } else {
          defaultImplementation = implementationName;
        }
      }

      Class<?> loadClass;
      boolean success = false;
      String persistenceClass = sub.getString(CONFIG_PERSISTENCE_CLASS);
      try {

        LOGGER.debug("Loading class '{}': ", persistenceClass);
        loadClass = getClass().getClassLoader().loadClass(persistenceClass);
        LOGGER.debug("Checking IPersistenceFactory.class.assignableFrom({})", persistenceClass);
        success = IPersistenceFactory.class.isAssignableFrom(loadClass);
        iPersistenceFactory = null;
        if (success) {
          LOGGER.debug("Creating instance of class {}", persistenceClass);
          iPersistenceFactory = (IPersistenceFactory) loadClass.newInstance();
          LOGGER.debug("Persistence factory successfully instantiated.");
        } else {
          LOGGER.error("IPersistenceFactory seems not to be assignable from class {}", persistenceClass);
        }
      } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
        LOGGER.error("Failed to create instance of persistence implementation " + persistenceClass, ex);
        success = false;
      }
      if (success) {
        persistenceUnitMap.put(implementationName, persistenceUnits);
        persistenceClassMap.put(implementationName, iPersistenceFactory);
        persistenceUnitDefaultMap.put(implementationName, firstPersistenceUnit);
      } else {
        throw new edu.kit.dama.mdm.core.exception.ConfigurationException("Failed to initialize persistence factory from URL '" + configURL + "'. See logfile for details.");
      }
    }
    if (defaultImplementation == null) {
      LOGGER.debug("Default implementation not set, yet. Using first one ({}) as default.", firstImplementation);
      defaultImplementation = firstImplementation;
    }
  }
}
