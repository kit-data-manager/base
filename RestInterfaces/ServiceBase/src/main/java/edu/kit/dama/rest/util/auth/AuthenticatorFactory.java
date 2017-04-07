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
package edu.kit.dama.rest.util.auth;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.util.DataManagerSettings;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.rest.util.auth.impl.OAuthAuthenticator;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class AuthenticatorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatorFactory.class);

    private static AuthenticatorFactory instance = null;
    private List<AbstractAuthenticator> authenticators = new ArrayList<>();

    /**
     * Default constructor.
     */
    protected AuthenticatorFactory() {
        List<Configuration> authenticatorConfigs = DataManagerSettings.getSingleton().getSubConfigurations(DataManagerSettings.AUTHORIZATION_CONFIG_ROOT + ".rest.authenticators.authenticator");
        if (authenticatorConfigs != null) {
            LOGGER.debug("Try to initialize authenticators.");
            for (Configuration config : authenticatorConfigs) {
                try {
                    AbstractAuthenticator adapter = createAuthenticatorInstance(config);
                    LOGGER.debug("Successfully instantiated new authenticator.");
                    authenticators.add(adapter);
                } catch (ConfigurationException ex) {
                    LOGGER.error("Failed to instantiate Authenticator.", ex);
                }
            }
        }
        if (authenticators.isEmpty()) {
            LOGGER.info("No valid authenticators found in 'datamanager.xml'. Falling back to default implementation.");
            OAuthAuthenticator defaultAuthenticator = new OAuthAuthenticator();
            defaultAuthenticator.setAuthenticatorId("OAuth");
            defaultAuthenticator.setDefaultConsumerKey("key");
            defaultAuthenticator.setDefaultConsumerSecret("secret");
            authenticators.add(defaultAuthenticator);
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
    private <T extends IConfigurableAdapter> T createAuthenticatorInstance(Configuration pConfig) throws ConfigurationException {
        try {
            String adapterClass = pConfig.getString("[@class]");

            //check adapter class
            if (adapterClass == null || adapterClass.length() < 1) {
                throw new ConfigurationException("No valid adapter class attribute found for adapter 'Authenticator'");
            }

            LOGGER.debug("Creating adapter instance for 'Authenticator'");
            LOGGER.debug(" * Adapter class: '{}'", adapterClass);

            //create and configure instance
            Class clazz = Class.forName(adapterClass);
            Object inst = clazz.getConstructor().newInstance();
            ((T) inst).configure(pConfig);
            return (T) inst;
        } catch (ClassNotFoundException cnfe) {
            throw new ConfigurationException("Failed to locate adapter class for adapter 'Authenticator'", cnfe);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ie) {
            throw new ConfigurationException("Failed to instantiate and configure adapter for adapter 'Authenticator'", ie);
        } catch (NoSuchMethodException nsme) {
            throw new ConfigurationException("Invalid adapter class for adapter 'Authenticator'", nsme);
        } catch (ClassCastException cce) {
            throw new ConfigurationException("Adapter instance for adapter 'Authenticator' does not implement IConfigurableAdapter interface", cce);
        }
    }

    /**
     * Obtain a factory instance.
     *
     * @return A factory.
     */
    public final static AuthenticatorFactory getInstance() {
        if (null == instance) {
            instance = new AuthenticatorFactory();
        }
        return instance;
    }

    /**
     * Obtain a list of configured authenticators.
     *
     * @return A list of authenticators.
     */
    public final List<AbstractAuthenticator> getAuthenticators() {
        return authenticators;
    }

    /**
     * Get an authenticators by its id.
     *
     * @param authenticatorId The id of the authenticator.
     *
     * @return An authenticator or null if no authenticator for the provided id
     * is available.
     */
    public final AbstractAuthenticator getAuthenticator(final String authenticatorId) {
        return (AbstractAuthenticator) CollectionUtils.find(authenticators, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return ((AbstractAuthenticator) o).getAuthenticatorId().equals(authenticatorId);
            }
        });
    }
}
