/*
 * Copyright 2017 Karlsruhe Institute of Technology.
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
package edu.kit.dama.staging.services.processor.impl;

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple staging processor that assigns the current date as upload date to a
 * digital object if this has not been done, yet.
 *
 * @author jejkal
 */
public class UploadDateAssigner extends AbstractStagingProcessor {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadDateAssigner.class);

    /**
     * Default constructor.
     *
     * @param pUniqueIdentifier The unique identifier.
     */
    public UploadDateAssigner(String pUniqueIdentifier) {
        super(pUniqueIdentifier);
    }

    @Override
    public String getName() {
        return "UploadDateAssigner";
    }

    @Override
    public String[] getInternalPropertyKeys() {
        return new String[]{};
    }

    @Override
    public String getInternalPropertyDescription(String string) {
        return "No description available";
    }

    @Override
    public String[] getUserPropertyKeys() {
        return new String[]{};
    }

    @Override
    public String getUserPropertyDescription(String string) {
        return "No description available";
    }

    @Override
    public void validateProperties(Properties pProperties) throws PropertyValidationException {
    }

    @Override
    public void configure(Properties pProperties) {
    }

    @Override
    public void performPreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        LOGGER.debug("Performing pre-transfer processing of processor {}", getName());

        String digitalObjectIdentifier = pContainer.getTransferInformation().getDigitalObjectId();
        LOGGER.debug("Trying to find digital object for object identifier {}.", digitalObjectIdentifier);
        // Load digital object from database 
        IMetaDataManager imdm = null;
        try {
            imdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            imdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());

            List<DigitalObject> find = imdm.findResultList("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier=?1", new Object[]{digitalObjectIdentifier}, DigitalObject.class);
            if (find.isEmpty()) {
                throw new StagingProcessorException("No DigitalObject found for identifier '" + digitalObjectIdentifier + "'");
            } else {
                LOGGER.debug("Obtained digital object for object identifier {}. Checking upload date.", digitalObjectIdentifier);
                DigitalObject object = find.get(0);
                Date uploadDate = object.getUploadDate();
                if (uploadDate == null) {
                    uploadDate = new Date();
                    LOGGER.debug("Upload date not assigned, yet. Setting upload date to {}.", uploadDate);
                    object.setUploadDate(uploadDate);
                    LOGGER.debug("Persisting digital object with new upload date.");
                    imdm.save(object);
                    LOGGER.debug("Upload date successfully assigned.");
                } else {
                    LOGGER.info("Upload data already set to {} for digital object with identifier {}. Skipping staging processor execution.", uploadDate, digitalObjectIdentifier);
                }
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            throw new StagingProcessorException("Not authorized to access digital object with id " + digitalObjectIdentifier, ex);
        } finally {
            if (imdm != null) {
                imdm.close();
            }
        }
    }

    @Override
    public void finalizePreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    }

    @Override
    public void performPostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {

    }

    @Override
    public void finalizePostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {

    }
}
