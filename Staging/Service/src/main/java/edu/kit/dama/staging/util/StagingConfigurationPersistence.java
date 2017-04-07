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
package edu.kit.dama.staging.util;

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationPersistenceImpl;
import edu.kit.dama.staging.entities.StagingAccessPointConfiguration;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class StagingConfigurationPersistence {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestInformationPersistenceImpl.class);

    private String alternativePersistenceUnit = null;
    /**
     * Entity manager factory used to access the persistence backend
     */
    private static StagingConfigurationPersistence SINGLETON = null;

    /**
     * Get StagingConfigurationPersistence Singleton using
     * StagingConfigurationPersistence.DEFAULT_PU as persistence unit.
     *
     * @return The singleton instance.
     */
    public static synchronized StagingConfigurationPersistence getSingleton() {
        return getSingleton(DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit"));
    }

    /**
     * Get StagingConfigurationPersistence Singleton.
     *
     * @param pUnit The persistence unit to use. Typically,
     * StagingConfigurationPersistence.DEFAULT_PU should be used.
     *
     * @return The singleton instance.
     */
    public static synchronized StagingConfigurationPersistence getSingleton(String pUnit) {
        if (SINGLETON == null) {
            if (pUnit == null) {
                SINGLETON = new StagingConfigurationPersistence();
            } else {
                SINGLETON = new StagingConfigurationPersistence(pUnit);
            }
        } else if (SINGLETON.getPersistenceUnit() != null && pUnit != null && !SINGLETON.getPersistenceUnit().equals(pUnit)) {
            LOGGER.warn("ATTENTION: Current persistence unit '" + SINGLETON.getPersistenceUnit() + "' is not equal provided persistence unit '" + pUnit + "'");
        }
        return SINGLETON;
    }

    /**
     * Default constructor
     */
    StagingConfigurationPersistence() {
        this(DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit"));
    }

    /**
     * Constructor for providing a custom persistence unit used for testing.
     *
     * @param pAltPersistenceUnit The name of the persistence unit
     */
    StagingConfigurationPersistence(String pAltPersistenceUnit) {
        alternativePersistenceUnit = pAltPersistenceUnit;
    }

    /**
     * Create and return a MetaDataManager instance. As this implementation is
     * intended to be used on server-side only, a system context is used to
     * authorize all MetaDataManagement operations.
     *
     * @return A IMetaDataManager instance.
     */
    private IMetaDataManager getMetaDataManager() {
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(alternativePersistenceUnit);
        //set system context to workaround any security issues
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        return mdm;
    }

    /**
     * Returns the currently used persistence unit
     *
     * @return The persistence unit's name
     */
    public String getPersistenceUnit() {
        return (alternativePersistenceUnit == null)
                ? DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_STAGING_PU_ID, "StagingUnit")
                : alternativePersistenceUnit;
    }

    /**
     * Save the provided AccessPoint.
     *
     * @param pNewMethod The AccessPoint to save.
     *
     * @return The persisted AccessPoint.
     *
     * @throws UnauthorizedAccessAttemptException Internal context is not
     * allowed to save. Should never happen.
     */
    public StagingAccessPointConfiguration saveAccessPointConfiguration(StagingAccessPointConfiguration pNewMethod) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = getMetaDataManager();
        try {
            return mdm.save(pNewMethod);
        } finally {
            mdm.close();
        }
    }

    /**
     * Find all AccessPoint configurations.
     *
     * @return All AccessPoint configurations.
     */
    public List<StagingAccessPointConfiguration> findAllAccessPointConfigurations() {
        List<StagingAccessPointConfiguration> result = new LinkedList<>();
        IMetaDataManager mdm = getMetaDataManager();
        try {
            result = mdm.find(StagingAccessPointConfiguration.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain available AccessPoint configuration", ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Find an AccessPoint by name. If no AccessPoint was found, 'null' is
     * returned. If more than one AccessPoint was found, the first AccessPoint
     * is returned and a warning is logged as there should be only one
     * AccessPoint per name. By default, only one AccessPoint is obtained and
     * will be returned.
     *
     * @param pName The name of the AccessPoint.
     *
     * @return The AccessPoint.
     */
    public StagingAccessPointConfiguration findAccessPointConfigurationByName(String pName) {
        List<StagingAccessPointConfiguration> result = new LinkedList<>();
        IMetaDataManager mdm = getMetaDataManager();
        try {
            result = mdm.findResultList("SELECT m FROM StagingAccessPointConfiguration m WHERE m.name=?1", new Object[]{pName}, StagingAccessPointConfiguration.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain access point configuration by name '" + pName + "'", ex);
        } finally {
            mdm.close();
        }

        if (result.isEmpty()) {
            LOGGER.warn("No access point configuration found for name '{}'", pName);
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            LOGGER.warn("Possible misconfiguration. More than one access point configuration found for name '{}'. Returning first result.", pName);
            return result.get(0);
        }
    }

    /**
     * Return the default access AccessPoint for the provided group id. If
     * pGroupId is null, AccessPoints with an unspecified groupId are checked.
     * If no default AccessPoint was found, 'null' is returned. If more than one
     * AccessPoint was found, the first AccessPoint is returned and a warning is
     * logged as there should be only one default AccessPoint. By default, only
     * one AccessPoint is obtained and will be returned.
     *
     * @param pGroupId The group for which the default AccessPoint should be
     * returned.
     *
     * @return The default AccessPoint.
     */
    public StagingAccessPointConfiguration findDefaultAccessPointConfigurationForGroup(String pGroupId) {
        List<StagingAccessPointConfiguration> result = new LinkedList<>();
        IMetaDataManager mdm = getMetaDataManager();

        if (pGroupId == null || pGroupId.equals(Constants.SYSTEM_GROUP)) {
            try {
                result = mdm.findResultList("SELECT h FROM StagingAccessPointConfiguration h WHERE h.groupId IS NULL AND h.defaultAccessPoint='TRUE'", StagingAccessPointConfiguration.class);
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Failed to obtain default AccessPoint configuration", ex);
            } finally {
                mdm.close();
            }
        } else {
            try {
                result = mdm.findResultList("SELECT h FROM StagingAccessPointConfiguration h WHERE h.groupId=?1 AND h.defaultAccessPoint='TRUE'", new Object[]{pGroupId}, StagingAccessPointConfiguration.class);
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Failed to obtain default AccessPoint configuration", ex);
            } finally {
                mdm.close();
            }
        }

        if (result.isEmpty()) {
            LOGGER.warn("No default AccessPoint configuration found for group '{}'", pGroupId);
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            LOGGER.warn("Possible misconfiguration. More than one default AccessPoint configurations found for group '{}'. Returning first value.", pGroupId);
            return result.get(0);
        }
    }

    /**
     * Find all AccessPoints for the provided group and for AccessPoints that
     * are available for all groups.
     *
     * @param pGroupId The group.
     *
     * @return A list of group-specific AccessPoints.
     */
    public List<StagingAccessPointConfiguration> findAccessPointConfigurationsForGroup(String pGroupId) {
        List<StagingAccessPointConfiguration> result = new LinkedList<>();
        String groupId = pGroupId;

        if (Constants.SYSTEM_GROUP.equals(groupId)) {
            LOGGER.info("Returning all AccessPoint configurations for group SYSTEM_GROUP");
            return findAllAccessPointConfigurations();
        }
        IMetaDataManager mdm = getMetaDataManager();

        try {
            if (groupId == null) {
                result = mdm.findResultList("SELECT h FROM StagingAccessPointConfiguration h WHERE h.groupId IS NULL", StagingAccessPointConfiguration.class);
            } else {
                result = mdm.findResultList("SELECT h FROM StagingAccessPointConfiguration h WHERE h.groupId=?1 OR h.groupId IS NULL", new Object[]{groupId}, StagingAccessPointConfiguration.class);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain available AccessPoint configurations", ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Find an AccessPointConfiguration by its unique identifier.
     *
     * @param pUniqueIdentifier The unique identifier.
     *
     * @return The AccessPointConfiguration or 'null' if none was found.
     */
    public StagingAccessPointConfiguration findAccessPointConfigurationByUniqueIdentifier(String pUniqueIdentifier) {
        StagingAccessPointConfiguration result = null;
        IMetaDataManager mdm = getMetaDataManager();
        try {
            result = mdm.findSingleResult("SELECT m FROM StagingAccessPointConfiguration m WHERE m.uniqueIdentifier=?1", new Object[]{pUniqueIdentifier}, StagingAccessPointConfiguration.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain StagingAccessPointConfiguration configuration by unique identifier '" + pUniqueIdentifier + "'", ex);
        } finally {
            mdm.close();
        }

        return result;
    }

    /**
     * Find an AccessPointConfiguration by its id.
     *
     * @param pId The id.
     *
     * @return The AccessPointConfiguration or 'null' if none was found.
     */
    public StagingAccessPointConfiguration findAccessPointConfigurationById(Long pId) {
        StagingAccessPointConfiguration result = null;
        IMetaDataManager mdm = getMetaDataManager();
        try {
            result = mdm.find(StagingAccessPointConfiguration.class, pId);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain StagingAccessPointConfiguration configuration by id '" + pId + "'", ex);
        } finally {
            mdm.close();
        }

        return result;
    }

    /**
     * Get the unique identifiers of all AccessPoints.
     *
     * @return A list of all unique identifiers.
     */
    public List<String> getAccessPointIds() {
        List<String> result = new LinkedList<>();
        IMetaDataManager mdm = getMetaDataManager();
        try {
            result = (List<String>) mdm.findResultList("SELECT h.uniqueIdentifier FROM StagingAccessPointConfiguration h");
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain available AccessPoint IDs", ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Get a list of all StagingProcessorConfigurations.
     *
     * @return A list of all StagingProcessorConfigurations.
     */
    public List<StagingProcessor> findAllStagingProcessors() {
        List<StagingProcessor> result = new LinkedList<>();
        IMetaDataManager mdm = getMetaDataManager();
        try {
            result = mdm.find(StagingProcessor.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain available staging processor configurations", ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Get a list of all StagingProcessorConfigurations for the provided group
     * or where no specific group is defined. If pGroupId is 'null', an empty
     * group string will be used. If pGroupId is the system group, all
     * StagingProcessorConfigurations are returned.
     *
     * @param pGroupId The group for which the StagingProcessorConfigurations
     * will be obtained.
     *
     * @return A list of all StagingProcessorConfigurations for the provided
     * group.
     */
    public List<StagingProcessor> findStagingProcessorsForGroup(String pGroupId) {
        List<StagingProcessor> result = new LinkedList<>();

        if (Constants.SYSTEM_GROUP.equals(pGroupId)) {
            LOGGER.info("Returning all staging processor configurations for group SYSTEM_GROUP");
            return findAllStagingProcessors();
        }
        IMetaDataManager mdm = getMetaDataManager();
        try {
            if (pGroupId == null) {
                result = mdm.findResultList("SELECT p FROM StagingProcessor p WHERE p.groupId IS NULL", new Object[]{pGroupId}, StagingProcessor.class);
            } else {
                result = mdm.findResultList("SELECT p FROM StagingProcessor p WHERE p.groupId=?1 OR p.groupId IS NULL", new Object[]{pGroupId}, StagingProcessor.class);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain available staging processor configurations", ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Find a StagingProcessor by its name. If no processor was found, 'null' is
     * returned. If more than one StagingProcessor was found, the first
     * StagingProcessor is returned and a warning is logged as there should be
     * only one StagingProcessor per name. By default, only one StagingProcessor
     * is obtained and will be returned.
     *
     * @param pName The name of the StagingProcessor.
     *
     * @return The StagingProcessor.
     */
    public StagingProcessor findStagingProcessorByName(String pName) {
        List<StagingProcessor> result = new LinkedList<>();
        IMetaDataManager mdm = getMetaDataManager();
        try {
            result = mdm.findResultList("SELECT p FROM StagingProcessor p WHERE p.name=?1", new Object[]{pName}, StagingProcessor.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain staging processor configuration by name '" + pName + "'", ex);
        } finally {
            mdm.close();
        }

        if (result.isEmpty()) {
            LOGGER.warn("No staging processor configuration found for name '{}'", pName);
            return null;
        } else if (result.size() == 1) {
            return result.get(0);
        } else {
            LOGGER.warn("Possible misconfiguration. More than one staging processor configuration found for name '{}'. Returning first value.", pName);
            return result.get(0);
        }
    }

    /**
     * Find a StagingProcessor by its id.
     *
     * @param pId The id.
     *
     * @return The StagingProcessor or 'null' if none was found.
     */
    public StagingProcessor findStagingProcessorById(Long pId) {
        StagingProcessor result = null;
        IMetaDataManager mdm = getMetaDataManager();
        try {
            result = mdm.find(StagingProcessor.class, pId);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain staging processor by id '" + pId + "'", ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Find a StagingProcessor by its unique identifier.
     *
     * @param pUniqueIdentifier The unique identifier.
     *
     * @return The StagingProcessor or 'null' if none was found.
     */
    public StagingProcessor findStagingProcessorById(String pUniqueIdentifier) {
        StagingProcessor result = null;
        IMetaDataManager mdm = getMetaDataManager();
        try {
            result = mdm.findSingleResult("SELECT p FROM StagingProcessor p WHERE p.uniqueIdentifier=?1", new Object[]{pUniqueIdentifier}, StagingProcessor.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain staging processor configuration by identifier '" + pUniqueIdentifier + "'", ex);
        } finally {
            mdm.close();
        }
        return result;
    }

    /**
     * Save the provided StagingProcessor.
     *
     * @param pNewProcessor The StagingProcessor to save.
     *
     * @return The persisted StagingProcessor.
     *
     * @throws UnauthorizedAccessAttemptException Internal context is not
     * allowed to save. Should never happen.
     */
    public StagingProcessor saveStagingProcessor(StagingProcessor pNewProcessor) throws UnauthorizedAccessAttemptException {
        IMetaDataManager mdm = getMetaDataManager();
        try {
            return mdm.save(pNewProcessor);
        } finally {
            mdm.close();
        }
    }
}
