/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.mdm.dataorganization.service.core;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.InitializationError;
import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizerImpl;
import edu.kit.dama.util.DataManagerSettings;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Is a factory for {@link DataOrganizerImpl} for getting the DataOrganizerImpl
 * implementation instance specific to your system.
 *
 * @author pasic
 */
public class DataOrganizerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataOrganizerFactory.class);

    private final DataOrganizer dataOrganizer;
    private static DataOrganizerFactory instance = null;

    /**
     * Default constructor.
     */
    protected DataOrganizerFactory() {
        Configuration doConfigSection = DataManagerSettings.getSingleton().getSubConfiguration(DataManagerSettings.DATA_ORGANIZATION_CONFIG_ROOT);
        if (doConfigSection == null) {
            LOGGER.info("No DataOrganization configuration section found in 'datamanager.xml'. Falling back to default implementation.");
            dataOrganizer = new DataOrganizerImpl();
        } else {
            LOGGER.debug("Try to initialize DataOrganizer.");
            try {
                dataOrganizer = createDataOrganizerAdapterInstance(doConfigSection);
            } catch (ConfigurationException ex) {
                throw new InitializationError("Failed to initialize DataOrganizer. ", ex);
            }
        }
    }

    /**
     * Creates an instance of the configured DataOrganizer defined in a
     * subsection of pConfig named 'dataOrganizerAdapter'. The root node of the
     * section also contains the implementation class. Depending on the
     * implementation, there might be further child nodes containing specific
     * configuration values for the adapter implementation.
     *
     * @param <T> Adapter class implementing IConfigurableAdapter.
     * @param pConfig The configuration used to obtain the DataOrganizerAdapter.
     *
     * @return An instance of the created DataOrganizer implementation.
     *
     * @throws ConfigurationException if anything goes wrong (e.g. if the
     * provided adapter class was not found, instantiation or configuration
     * failed...)
     */
    private <T extends IConfigurableAdapter> T createDataOrganizerAdapterInstance(Configuration pConfig) throws ConfigurationException {
        try {
            String adapterClass = pConfig.getString("dataOrganizerAdapter[@class]");

            //check adapter class
            if (adapterClass == null || adapterClass.length() < 1) {
                throw new ConfigurationException("No valid adapter class attribute found for adapter 'dataOrganizerAdapter'");
            }

            Configuration customConfig = pConfig.subset("dataOrganizerAdapter");

            LOGGER.debug("Creating adapter instance for 'dataOrganizerAdapter'");
            LOGGER.debug(" * Adapter class: '{}'", adapterClass);

            //create and configure instance
            Class clazz = Class.forName(adapterClass);
            Object inst = clazz.getConstructor().newInstance();
            if (customConfig != null && !customConfig.isEmpty()) {//try custom configuration
                ((T) inst).configure(customConfig);
            }
            return (T) inst;
        } catch (ClassNotFoundException cnfe) {
            throw new ConfigurationException("Failed to locate adapter class for adapter 'dataOrganizerAdapter'", cnfe);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ie) {
            throw new ConfigurationException("Failed to instantiate and configure adapter for adapter 'dataOrganizerAdapter'", ie);
        } catch (NoSuchMethodException nsme) {
            throw new ConfigurationException("Invalid adapter class for adapter 'dataOrganizerAdapter'", nsme);
        } catch (ClassCastException cce) {
            throw new ConfigurationException("Adapter instance for adapter 'dataOrganizerAdapter' does not implement IConfigurableAdapter interface", cce);
        }
    }

    /**
     * Obtain a factory instance.
     *
     * @return A factory.
     */
    public final static DataOrganizerFactory getInstance() {
        if (null == instance) {
            instance = new DataOrganizerFactory();
        }
        return instance;
    }

    /**
     * Obtain a reference on the system-specific DataOrganizerImpl
     * implementation instance.
     *
     * @return A data organizer.
     */
    public final DataOrganizer getDataOrganizer() {
        return dataOrganizer;
    }
}
