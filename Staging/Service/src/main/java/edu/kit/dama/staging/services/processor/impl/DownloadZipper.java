/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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

import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.dama.staging.services.impl.StagingService;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.CryptUtil;
import edu.kit.dama.util.ZipUtils;
import edu.kit.tools.url.URLCreator;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Staging Processor implementation that allows to ZIP all data of a digital
 * object during download preparation. Therefor, the already staged data of a
 * download is zipped, the ZIP file is firstly generated in the 'generated'
 * directory of the download. Afterwards, the 'data' directory is emptied and
 * the ZIP file is moved there, such that a download will deliver the ZIP file
 * instead of the single files of the digital object.
 *
 * Attention: This StagingProcessor is intended to be used only for downloads.
 * If one uses it for ingest, this will result in empty digital objects as the
 * replacement of the data is not tracked at the associated TransferContainer
 * and the actually registered data is removed by the processor.
 *
 * @author mf6319
 */
public class DownloadZipper extends AbstractStagingProcessor {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadZipper.class);

    /**
     * Default constructor.
     *
     * @param pUniqueIdentifier The unique identifier.
     */
    public DownloadZipper(String pUniqueIdentifier) {
        super(pUniqueIdentifier);
    }

    @Override
    public String getName() {
        return "DownloadZipper";
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
    }

    @Override
    public void finalizePreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    }

    @Override
    public void performPostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        LOGGER.debug("Zipping data");
        URL generatedFolder = pContainer.getGeneratedUrl();
        LOGGER.debug("Using target folder: {}", generatedFolder);

        try {
            String zipFileName = CryptUtil.stringToSHA1(pContainer.getTransferInformation().getDigitalObjectId()) + ".zip";
            URL targetZip = URLCreator.appendToURL(generatedFolder, zipFileName);
            LOGGER.debug("Zipping all data to file {}", targetZip);
            File targetFile = new File(targetZip.toURI());

            ITransferInformation info = pContainer.getTransferInformation();
            LOGGER.debug("Obtaining local folder for transfer with id {}", info.getTransferId());

            File localFolder = StagingService.getSingleton().getLocalStagingFolder(info, StagingService.getSingleton().getContext(info));
            File dataFolder = new File(FilenameUtils.concat(localFolder.getAbsolutePath(), Constants.STAGING_DATA_FOLDER_NAME));
            if (!dataFolder.exists()) {
                throw new IOException("Data folder " + dataFolder.getAbsolutePath() + " does not exist. Aborting zip operation.");
            }

            LOGGER.debug("Start zip operation using data input folder URL {}", dataFolder);
            ZipUtils.zip(new File(dataFolder.toURI()), targetFile);
            LOGGER.debug("Deleting 'data' folder content.");
            for (File f : new File(dataFolder.toURI()).listFiles()) {
                FileUtils.deleteQuietly(f);
            }
            LOGGER.debug("Moving result file {} to 'data' folder.", targetFile);
            FileUtils.moveFileToDirectory(targetFile, new File(dataFolder.toURI()), false);
            LOGGER.debug("Zip operation successfully finished.");
        } catch (IOException | URISyntaxException ex) {
            throw new StagingProcessorException("Failed to zip data", ex);
        }
    }

    @Override
    public void finalizePostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {

    }

}
