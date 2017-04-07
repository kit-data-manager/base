/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.mdm.audit.impl;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.mdm.audit.interfaces.AbstractAuditConsumer;
import edu.kit.dama.mdm.audit.interfaces.AbstractAuditPublisher;
import edu.kit.dama.util.DataManagerSettings;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class AuditManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditManager.class);

    private static AuditManager instance = null;
    private List<AbstractAuditConsumer> consumers = new ArrayList<>();
    private AbstractAuditPublisher publisher;

    /**
     * Default constructor.
     */
    protected AuditManager() {
        Configuration auditPublisherConfig = DataManagerSettings.getSingleton().getSubConfiguration(DataManagerSettings.AUDIT_CONFIG_ROOT + ".publisher");
        if (auditPublisherConfig != null) {
            LOGGER.debug("Try to initialize audit publisher.");

            try {
                publisher = createAuditAdapterInstance(auditPublisherConfig);
                LOGGER.debug("Successfully instanciated new audit publisher.");
            } catch (ConfigurationException ex) {
                LOGGER.error("Failed to instanciate audit publisher.", ex);
            }
        }

        if (publisher == null) {
            LOGGER.info("No valid audit publisher found in 'datamanager.xml'. Falling back to default implementation.");
            publisher = new RabbitMQPublisher();
        }

        boolean consumersSupported = true;
        LOGGER.debug("Initializing publisher.");
        if (publisher.initialize()) {
            LOGGER.debug("Publisher successfully initialized.");
        } else {
            LOGGER.warn("Failed to initialize publisher. FAlling back to LogfilePublisher.");
            publisher = new LogbackPublisher();
            publisher.initialize();
            consumersSupported = false;
        }

        if (!consumersSupported) {
            LOGGER.info("No audit event consumers supported as LogbackPublisher is used. Skipping consumer configuration.");
        } else {
            List<Configuration> auditConsumerConfigs = DataManagerSettings.getSingleton().getSubConfigurations(DataManagerSettings.AUDIT_CONFIG_ROOT + ".consumers.consumer");
            if (auditConsumerConfigs != null) {
                LOGGER.debug("Try to initialize audit consumers.");
                for (Configuration config : auditConsumerConfigs) {
                    try {
                        AbstractAuditConsumer consumer = createAuditAdapterInstance(config);
                        LOGGER.debug("Successfully instantiated new audit consumers.");
                        consumers.add(consumer);
                    } catch (ConfigurationException ex) {
                        LOGGER.error("Failed to instantiate audit consumer.", ex);
                    }
                }
            }
            if (consumers.isEmpty()) {
                LOGGER.info("No valid audit consumers found in 'datamanager.xml'. Falling back to default implementation.");
                consumers.add(new ConsoleConsumer());
            }
        }
    }

    /**
     * Creates an instance of the configured authenticator defined in a
     * subsection of pConfig named 'authenticators'. The root node of the
     * section also contains the implementation class. Depending on the
     * implementation, there might be further child nodes containing specific
     * configuration values for the adapter implementation.
     *
     * @param <T> Adapter class implementing IConfigurableAdapter.
     * @param pConfig The configuration used to obtain the Authenticator.
     *
     * @return An instance of the created Authenticator implementation.
     *
     * @throws ConfigurationException if anything goes wrong (e.g. if the
     * provided adapter class was not found, instantiation or configuration
     * failed...)
     */
    private <T extends IConfigurableAdapter> T createAuditAdapterInstance(Configuration pConfig) throws ConfigurationException {
        try {
            String adapterClass = pConfig.getString("[@class]");

            //check adapter class
            if (adapterClass == null || adapterClass.length() < 1) {
                throw new ConfigurationException("No valid handler class attribute found for adapter 'AuditAdapter'");
            }

            LOGGER.debug("Creating adapter instance for 'AuditAdapter'");
            LOGGER.debug(" * Adapter class: '{}'", adapterClass);

            //create and configure instance
            Class clazz = Class.forName(adapterClass);
            Object inst = clazz.getConstructor().newInstance();
            ((T) inst).configure(pConfig);
            return (T) inst;
        } catch (ClassNotFoundException cnfe) {
            throw new ConfigurationException("Failed to locate adapter class for adapter 'AuditAdapter'", cnfe);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ie) {
            throw new ConfigurationException("Failed to instantiate and configure adapter for adapter 'AuditAdapter'", ie);
        } catch (NoSuchMethodException nsme) {
            throw new ConfigurationException("Invalid adapter class for adapter 'AuditAdapter'", nsme);
        } catch (ClassCastException cce) {
            throw new ConfigurationException("Adapter instance for adapter 'AuditAdapter' does not implement IConfigurableAdapter interface", cce);
        }
    }

    /**
     * Obtain a factory instance.
     *
     * @return A factory.
     */
    public final static AuditManager getInstance() {
        if (null == instance) {
            instance = new AuditManager();
        }
        return instance;
    }

    /**
     * Obtain the configured audit publisher.
     *
     * @return The audit publisher.
     */
    public final AbstractAuditPublisher getPublisher() {
        return publisher;
    }

    /**
     * Obtain a list of configured audit consumers.
     *
     * @return A list of audit consumers.
     */
    public final List<AbstractAuditConsumer> getConsumers() {
        return consumers;
    }

}
