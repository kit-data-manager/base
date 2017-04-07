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

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.mdm.audit.interfaces.AbstractAuditConsumer;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class DatabaseConsumer extends AbstractAuditConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConsumer.class);

    @Override
    public void consume(AuditEvent entry) {
        LOGGER.debug("Consuming audit event for pid {}", entry.getPid());
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            mdm.save(entry);
            LOGGER.debug("Audit event with id {} successfully processed.", entry.getPid());
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to persist audit entry.", ex);
        } finally {
            mdm.close();
        }
    }

    @Override
    public boolean performCustomConfiguration(Configuration config) throws ConfigurationException {
        return true;
    }

}
