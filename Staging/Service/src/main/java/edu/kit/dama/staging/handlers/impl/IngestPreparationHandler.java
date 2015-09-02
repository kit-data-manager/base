/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.staging.handlers.impl;

import edu.kit.dama.staging.handlers.AbstractTransferPreparationHandler;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.staging.exceptions.TransferPreparationException;
import edu.kit.dama.staging.services.impl.ingest.IngestInformationPersistenceImpl;
import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.entities.TransferClientProperties;

/**
 * Transfer preparation handler implementation for data ingest. Most preparation
 * steps are covered by the AbstractTransferPreparationHandler. This
 * ingest-specific implementation may only add extended features for general
 * setup, environmental and client-access preparation.
 *
 * @author jejkal
 */
public class IngestPreparationHandler extends AbstractTransferPreparationHandler<INGEST_STATUS, IngestInformation> {

    /**
     * The logger
     */
    //private final static Logger LOGGER = LoggerFactory.getLogger(IngestPreparationHandler.class);

    /**
     * Preparation handler for data ingest
     *
     * @param pPersistence The persistence implementation used to access the
     * persistence backend
     * @param pEntity The ingest information entity of the ingest to prepare
     */
    public IngestPreparationHandler(IngestInformationPersistenceImpl pPersistence, IngestInformation pEntity) {
        super(pPersistence, pEntity);
    }

    @Override
    public final void setup(TransferClientProperties pProperties) throws TransferPreparationException{
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Nothing to do here for the moment. Later we may add additional properties used by to following phases.
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public final void prepareEnvironment(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Nothing to do here for the moment. Later we may add additional properties used by to following phases.
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public final void prepareClientAccess(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) throws TransferPreparationException {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Nothing to do here for the moment. Later we may add additional properties used by to following phases.
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    @Override
    public final void publishTransferInformation(TransferClientProperties pProperties, IAuthorizationContext pSecurityContext) {
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Nothing to do here for the moment. Later we may add additional properties used by to following phases.
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
}
