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
package edu.kit.dama.staging.adalapi.protocol;

import edu.kit.dama.staging.entities.AdalapiProtocolConfiguration;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.lsdf.adalapi.protocols.interfaces.IExternalProtocolConfigurator;
import java.net.URL;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class DatabaseProtocolConfigurator implements IExternalProtocolConfigurator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseProtocolConfigurator.class);

    @Override
    public Configuration getConfiguration(URL pUrl) {
        LOGGER.debug("Trying to obtain ADALAPI configuration for url {} from database.", pUrl);
        String identifier = AdalapiProtocolConfiguration.getProtocolIdentifier(pUrl);
        LOGGER.debug("Looking for identifier {}", identifier);
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            AdalapiProtocolConfiguration result = mdm.findSingleResult("SELECT c FROM AdalapiProtocolConfiguration c WHERE c.identifier=?1", new Object[]{identifier}, AdalapiProtocolConfiguration.class);
            LOGGER.debug("Found {} protocol configuration for identifier {}.", (result == null ? "no" : "one"), identifier);
            if (result != null) {
                LOGGER.debug("Returning protocol configuration.");
                return result.toConfiguration();
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            //should not happen
            LOGGER.error("Not authorized to obtain protocol configuration for Url " + pUrl + ".", ex);
        } finally {
            mdm.close();
        }
        LOGGER.warn("Unable to obtain protocol configuration for Url {}. Returning 'null'.", pUrl);
        return null;
    }

}
