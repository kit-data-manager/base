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
package edu.kit.dama.mdm.audit.interfaces;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jejkal
 */
public abstract class AbstractAuditConsumer implements IConfigurableAdapter {

    /**
     * Default constructor.
     */
    public AbstractAuditConsumer() {
    }

    public abstract void consume(AuditEvent entry);

    public abstract boolean performCustomConfiguration(Configuration config) throws ConfigurationException;

    @Override
    public boolean configure(Configuration pConfig) throws ConfigurationException {
        return performCustomConfiguration(pConfig);
    }

}
