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
package edu.kit.dama.authorization.entities.util;

import edu.kit.dama.util.DataManagerSettings;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ochsenreither
 */
public final class PU {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(PU.class);

    private static String persistenceUnitName = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.PERSISTENCE_AUTHORIZATION_PU_ID, "AuthorizationPU");

    /**
     * Hidden constructor.
     */
    private PU() {
    }

    /**
     * Set the used persistence unit name. This method should only be used for
     * internal testing. The default value is 'AuthorisationPU'.
     *
     * @param pUnitName The name of the used persistence unit.
     */
    public static void setPersistenceUnitName(String pUnitName) {
        LOGGER.debug("Setting persistence unit name to {}", pUnitName);
        persistenceUnitName = pUnitName;
    }

    /**
     * Returns the currently set persistence unit name.
     *
     * @return The currently used persistence unit name.
     */
    public static String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    /**
     * Get the entity manager.
     *
     * @return The entity manager.
     */
    public static EntityManager entityManager() {
        LOGGER.debug("Returning entity manager for persistence unit {}", persistenceUnitName);
        return Persistence.createEntityManagerFactory(persistenceUnitName).createEntityManager();
    }

    /**
     * Exception handler for transactions. Inside it is checked if a transaction
     * is active. If this is the case, a rollback is triggered and the entity
     * manager is closed.
     *
     * @param ex The exception. (Currently not needed)
     * @param em The EntityManager.
     */
    public static void handleUnexpectedPersistenceExceptionInTransaction(PersistenceException ex, EntityManager em) {
        if (em.isOpen()) {
            EntityTransaction transaction = em.getTransaction();
            if (transaction.isActive()) {
                transaction.rollback();
            }
            em.close();
        }
    }
}
