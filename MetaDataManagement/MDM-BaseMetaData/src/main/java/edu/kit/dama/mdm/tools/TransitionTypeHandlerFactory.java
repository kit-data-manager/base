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
package edu.kit.dama.mdm.tools;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.mdm.base.TransitionType;
import edu.kit.dama.util.DataManagerSettings;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import org.apache.commons.configuration.Configuration;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public final class TransitionTypeHandlerFactory {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TransitionTypeHandlerFactory.class);

    /**
     * Factory a transition type handler for the provided transition type. The
     * factory will check in the according configuration section for the
     * configured handler. If no handler section exists, a
     * NullTransitionTypeHandler is returned.
     *
     * If a section for the transition type exists, the handler class is
     * obtained, checked and the handler is instantiated, configured and
     * returned. If the handler class is not set, a NullTransitionTypeHandler is
     * returned.. Otherwise, if a handler class is provided and any of these
     * previous steps fails, a ConfigurationException is thrown.
     *
     * @param pType The transition type for which the handler should be
     * returned.
     *
     * @return The transition type handler instance.
     *
     * @throws ConfigurationException if a handler section was found, but
     * instantiation or configuration of the handler has failed.
     */
    public static AbstractTransitionTypeHandler factoryTransitionTypeHandler(TransitionType pType) throws ConfigurationException {
        LOGGER.debug("Trying to factory transition type handler for type '{}'", pType);
        Configuration subConfig = DataManagerSettings.getSingleton().getSubConfiguration(DataManagerSettings.METADATA_MANAGEMENT_CONFIG_ROOT);
        if (subConfig == null) {
            //old datamanager.xml, no metadataManagement config node
            LOGGER.info("No section '{}' found in datamanager.xml configuration file. Probably, transition type handlers have not been configured, yet. Returning NullTransitionTypeHandler.", DataManagerSettings.METADATA_MANAGEMENT_CONFIG_ROOT);
            return new NullTransitionTypeHandler();
        }

        Configuration subset = subConfig.subset("transitionTypes." + pType.toString());
        if (subset == null) {
            //no configuration found for te transition type...type not supported.
            LOGGER.info("No sub-section for handler type '{}' found in datamanager.xml configuration file. Probably, the transition type handler has not been configured, yet. Returning NullTransitionTypeHandler.", pType);
            return new NullTransitionTypeHandler();
        }
        
        Iterator key = subset.getKeys();
               while(key.hasNext()){
                   String k = (String)key.next();
                   
                   System.out.println("KEE " + k+ "- " +subset.getProperty(k));
               }
               
        
        try {
            String handlerClass = subset.getString("handlerClass");
            if (handlerClass == null) {
                LOGGER.info("No handler class defined for type '" + pType + "'. Returning NullTransitionTypeHandler.");
                return new NullTransitionTypeHandler();
            }
            Class clazz = Class.forName(handlerClass);
            Object instance = clazz.getConstructor().newInstance();
            ((IConfigurableAdapter) instance).configure(subset);
            return (AbstractTransitionTypeHandler) instance;
        } catch (ClassNotFoundException cnfe) {
            throw new ConfigurationException("Failed to locate transition handler class for type '" + pType + "'", cnfe);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ie) {
            throw new ConfigurationException("Failed to instantiate and configure transition handler for type '" + pType + "'", ie);
        } catch (NoSuchMethodException nsme) {
            throw new ConfigurationException("Missing default constructor for transition handler class for type '" + pType + "'", nsme);
        } catch (ClassCastException cce) {
            throw new ConfigurationException("Transition handler instance for type '" + pType + "' does not implement IConfigurableAdapter.", cce);
        }
    }

}
