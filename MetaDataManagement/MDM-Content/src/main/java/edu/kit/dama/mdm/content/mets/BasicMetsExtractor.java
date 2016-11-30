/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.mdm.content.mets;

import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * Extraction the metadata from data set without supporting content metadata.
 *
 * @author hartmann-v
 */
public class BasicMetsExtractor extends MetsMetadataExtractor {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(BasicMetsExtractor.class);

  /**
   * Default constructor
   *
   * @param pUniqueIdentifier The unique identifier of this OP. This identifier
   * should be used to name generated output files associated with this OP.
   */
  public BasicMetsExtractor(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
  }

  @Override
  public final String getName() {
    return "Basic_Mets_Metadata_Extractor";
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
  protected void validateExtractorProperties(Properties pProperties) throws PropertyValidationException {
    // nothing to do
  }

  @Override
  public String[] getUserPropertyKeys() {
    LOGGER.trace("getUserPropertyKeys - No user properties defined!");
    return null;
  }

  @Override
  public String getUserPropertyDescription(String pKey) {
    LOGGER.trace("getUserPropertyDescription - Nothing to do");
    return null;
  }

  @Override
  protected void configureExtractor(Properties pProperties) {
    // nothing to do
  }

  @Override
  protected Document createCommunitySpecificDocument(TransferTaskContainer pContainer) throws MetaDataExtractionException {
    LOGGER.debug("{}: createCommunitySpecificDocument", this.getClass().toString());
    return createEmptyDocument();
  }
}
