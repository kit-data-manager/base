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
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.util.Constants;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.fzk.tools.xml.JaxenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extraction the metadata from data set. Complete content metadata has to be
 * available as XML file.
 *
 * @author hartmann-v
 */
public class ExtractXmlMetadataFromFile extends MetsMetadataExtractor {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExtractXmlMetadataFromFile.class);
  /**
   * The version configuration property
   */
  private static final String FILENAME_PROPERTY = "filename";
  /**
   * Default name of the file holding metadata information.
   */
  private static final String METADATA_FILENAME = "metadata.xml";

  /**
   * Name of the file holding metadata information.
   */
  private String metadataFilename = METADATA_FILENAME;

  /**
   * Default constructor
   *
   * @param pUniqueIdentifier The unique identifier of this OP. This identifier
   * should be used to name generated output files associated with this OP.
   */
  public ExtractXmlMetadataFromFile(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
  }

  @Override
  public final String getName() {
    return "Metadata_Extractor_From_XML_File";
  }

  @Override
  protected String[] getExtractorPropertyKeys() {
    return new String[]{FILENAME_PROPERTY};
  }

  @Override
  protected String getExtractorPropertyDescription(String pProperty) {
    if (FILENAME_PROPERTY.equals(pProperty)) {
      return "The name of the file which contain the metadata as XML. Only file name is needed without any path information.";
    }
    return null;
  }

  @Override
  protected void validateExtractorProperties(Properties pProperties) throws PropertyValidationException {
    if (pProperties == null) {
      throw new IllegalArgumentException("Argument pProperties must not be null");

    }
    String filenameProperty = pProperties.getProperty(FILENAME_PROPERTY);
    try {
      if (filenameProperty == null) {
        //no filename set
        throw new IllegalArgumentException("Property '" + FILENAME_PROPERTY + "' not provided.");
      }
      //check version...throws IllegalArgumentException if invalid
      LOGGER.debug("File name set: {}", filenameProperty);
    } catch (IllegalArgumentException iae) {
      throw new PropertyValidationException("Failed to read file name from property value " + filenameProperty + ".", iae);
    }
  }

  @Override
  public String[] getUserPropertyKeys() {
    LOGGER.trace("getUserPropertyKeys - Nothing to do");
    return null;
  }

  @Override
  public String getUserPropertyDescription(String pKey) {
    LOGGER.trace("getUserPropertyDescription - Nothing to do");
    return null;
  }

  @Override
  protected void configureExtractor(Properties pProperties) {
    // <editor-fold defaultstate="collapsed" desc="Check and set version property">
    LOGGER.debug("configureExtractor() -- configure properties");
    String filename = pProperties.getProperty(FILENAME_PROPERTY, METADATA_FILENAME);
    metadataFilename = filename;
    // </editor-fold>
    LOGGER.debug("Chosen metadata file: " + metadataFilename);
  }

  @Override
  protected Document createCommunitySpecificDocument(TransferTaskContainer pContainer) throws MetaDataExtractionException {
    LOGGER.debug("{}: createCommunitySpecificDocument", this.getClass().toString());
    Document xmlDocument = null;
    String xmlAsString;

    // <editor-fold defaultstate="collapsed" desc="check for and read metadata.xml ">
    ICollectionNode root = pContainer.getFileTree().getRootNode();
    //initialize hashes list
    LOGGER.debug("Searching for file '{}'.", metadataFilename);
    //obtain "generated" folder node
    IDataOrganizationNode dataSubTree = Util.getNodeByName(root, Constants.STAGING_DATA_FOLDER_NAME);

    IFileNode metadataFile = (IFileNode) Util.getNodeByName((ICollectionNode) dataSubTree, metadataFilename);

    if (metadataFile == null) {
      LOGGER.warn("No metadata file found. Skipping meta data parsing.");

      xmlDocument = createEmptyDocument();
    } else {
      try {
        File theFile = new File(new URL(metadataFile.getLogicalFileName().asString()).toURI());
        xmlAsString = FileUtils.readFileToString(theFile);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("Content of file: " + xmlAsString);
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(xmlAsString.getBytes());
        xmlDocument = JaxenUtil.getW3CDocument(bin);

      } catch (MalformedURLException | URISyntaxException ex) {
        LOGGER.error("Failed to obtain metadata file from URL " + metadataFile.getLogicalFileName().asString() + ".", ex);
      } catch (IOException ex) {
        LOGGER.error("Error reading file from URL " + metadataFile.getLogicalFileName().asString() + ".", ex);
      } catch (Exception exc) {
        throw new MetaDataExtractionException("Failed to read content metadata as XML.", exc);
      }
    }
    // </editor-fold>

    return xmlDocument;
  }
}
