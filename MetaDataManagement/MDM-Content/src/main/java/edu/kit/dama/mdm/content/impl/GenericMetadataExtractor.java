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
package edu.kit.dama.mdm.content.impl;

import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Extract the metadata from a dataset. This class is a basic implementation of
 * the AbstractMetadataExtractor which should be used to generate the basic
 * metadate directly from the digital object without any community specific
 * metadata. For this purpose, the namespace
 * BaseMetaDataHelper.DAMA_NAMESPACE_PREFIX should be used as schema identifier
 * and the schema should be registered in the persistent backend.
 *
 * @see AbstractMetadataExtractor
 * @author hartmann-v
 */
public class GenericMetadataExtractor extends AbstractMetadataExtractor {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(GenericMetadataExtractor.class);

  /**
   * Default constructor
   *
   * @param pUniqueIdentifier The unique identifier of this processor. This identifier
   * should be used to name generated output files associated with this processor.
   */
  public GenericMetadataExtractor(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
  }

  @Override
  public final String getName() {
    return "GenericMetadataExtractor";
  }

  @Override
  public final void performPreTransferExtraction(TransferTaskContainer pContainer) throws StagingProcessorException {
    //do nothing here
    LOGGER.debug("performPreTransferExtraction() -- do nothing");
  }

  @Override
  protected final Element createCommunitySpecificElement(TransferTaskContainer pContainer) throws MetaDataExtractionException {
    LOGGER.debug("{}: createCommunitySpecificElement", this.getClass().toString());
    return null;
  }

  @Override
  protected void validateExtractorProperties(Properties pProperties) throws PropertyValidationException {
  }

  @Override
  protected String[] getExtractorPropertyKeys() {
    return null;
  }

  @Override
  protected String getExtractorPropertyDescription(String pProperty) {
    return null;
  }

  @Override
  protected void configureExtractor(Properties pProperties) {
  }
}
