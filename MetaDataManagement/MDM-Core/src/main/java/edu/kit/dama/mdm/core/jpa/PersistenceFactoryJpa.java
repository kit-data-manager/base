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
package edu.kit.dama.mdm.core.jpa;

import edu.kit.dama.commons.exceptions.InitializationError;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.IPersistenceFactory;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.util.StackTraceUtil;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class for the different data bases. There should be only one instance
 * for each data base. This class is implemented as singleton.
 *
 * @author hartmann-v
 */
public class PersistenceFactoryJpa implements IPersistenceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceFactoryJpa.class);

    /**
     * Hold single instances of each database.
     */
    private volatile Map<String, EntityManagerFactory> persistenceUnitsMap = null;
    /**
     * Singleton for distributing MetaDataManagers.
     */
    private final static PersistenceFactoryJpa SINGLETON = new PersistenceFactoryJpa();

    /**
     * Constructor. Creating HashMap.
     */
    public PersistenceFactoryJpa() {
        LOGGER.info("Instantiating PersistenceFactoryJpa");
        persistenceUnitsMap = new ConcurrentHashMap<>();
        try {
            LOGGER.debug("Checking for existing persistence.xml resources.");
            Enumeration<URL> resources = PersistenceFactoryJpa.class.getClassLoader().getResources("META-INF/persistence.xml");
            int cnt = 0;
            URL last = null;
            while (resources.hasMoreElements()) {
                last = resources.nextElement();
                LOGGER.debug(" - Found persistence.xml at URL {}", last);
                cnt++;
            }
            if (cnt > 1) {
                LOGGER.warn("Multiple persistence.xml resources found. Probably, the one located at {} will be used by JPA.", last);
            }
            if (cnt == 0) {
                throw new IOException("No persistence.xml found in classpath. Please add META-INF/persistence.xml in order to be able to use this persistence factory.");
            }
        } catch (IOException ex) {
            throw new InitializationError("Failed to check for persistence.xml.", ex);
        }
    }

    @Override
    public synchronized IMetaDataManager getMetaDataManager(final String persistenceUnit) {
        EntityManagerFactory emFactory = SINGLETON.persistenceUnitsMap.get(persistenceUnit);
        if (emFactory == null) {
            LOGGER.debug("Creating EntityManagerFactory for persistenceUnit {}.", persistenceUnit);
            emFactory = Persistence.createEntityManagerFactory(persistenceUnit);
            LOGGER.debug("Putting EntityManagerFactory for persistenceUnit {} to cache.", persistenceUnit);
            SINGLETON.persistenceUnitsMap.put(persistenceUnit, emFactory);
        } else {
            LOGGER.debug("Re-Using EntityManagerFactory for persistenceUnit {} from cache.", persistenceUnit);
        }
        LOGGER.debug("Obtaining IMetaDataManager implementation from factory.");
        IMetaDataManager implementation = new MetaDataManagerJpa(emFactory.createEntityManager());
        LOGGER.debug("Wrapping implementation by SecureMetaDataManager and returning it.");
        return new SecureMetaDataManager(implementation);
    }

    @Override
    public void destroy() {
        Set<Entry<String, EntityManagerFactory>> factories = SINGLETON.persistenceUnitsMap.entrySet();
        for (Entry<String, EntityManagerFactory> factory : factories) {
            try {
                LOGGER.debug("Closing EntityManagerFactory '{}'.", factory.getKey());
                factory.getValue().close();
            } catch (IllegalStateException | DatabaseException ex) {
                //ignore
            }
        }
        SINGLETON.persistenceUnitsMap.clear();
    }

}
