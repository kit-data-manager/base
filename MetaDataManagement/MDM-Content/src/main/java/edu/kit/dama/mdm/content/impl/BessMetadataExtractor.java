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
package edu.kit.dama.mdm.content.impl;

import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.util.Constants;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Extraction the metadata from data set. Data set has to be a SPIM data set
 * containing a series of btf-files containing OME meta data and a text file
 * called 'measurement_report.txt' holding additional information.
 *
 * @author hartmann-v
 */
public class BessMetadataExtractor extends AbstractMetadataExtractor {

  /**
   * Schema path of the OME XML file.
   */
  private static final String SCHEMA_PATH = "http://ipelsdf1.lsdf.kit.edu/dama/bess/2013-07";
  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(BessMetadataExtractor.class);
  /**
   * The version configuration property
   */
  private static final String VERSION_PROPERTY = "version";
  /**
   * The most actual version.
   */
  private static final String VERSION_1_0 = "Version 1.0";
  /**
   * The next most actual version in future.
   */
  private static final String VERSION_1_1 = "Version 1.1";
  /**
   * file name of the measurement report. Tab separated file containing some
   * useful values.
   */
  private static final String METADATA_FILENAME = "metadata.xml";

  /**
   * Enumeration of all known versions.
   */
  public enum VersionString {

    /**
     * Version 1.0
     */
    VERSION10(VERSION_1_0),
    /**
     * Version 1.1
     */
    VERSION20(VERSION_1_1);
    String version;

    /**
     * Hidden constructor.
     *
     * @param label The label.
     */
    private VersionString(String label) {
      version = label;
    }

    @Override
    public String toString() {
      return version;
    }

    /**
     * Alternative implementation select enumeration by string representation
     * instead of variable declaration.
     *
     * @param value The value.
     *
     * @return The enum.
     */
    public static VersionString getEnum(String value) {
      for (VersionString item : VersionString.values()) {
        if (item.version.compareTo(value) == 0) {
          return item;
        }
      }
      throw new IllegalArgumentException("Invalid VersionString value: " + value);
    }
  }

  /**
   * Default constructor
   *
   * @param pUniqueIdentifier The unique identifier of this OP. This identifier
   * should be used to name generated output files associated with this OP.
   */
  public BessMetadataExtractor(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
  }

  @Override
  public final String getName() {
    return "BessMetadataExtractor";
  }

  @Override
  protected void validateExtractorProperties(Properties pProperties) throws PropertyValidationException {
    if (pProperties == null) {
      throw new IllegalArgumentException("Argument pProperties must not be null");

    }
    String version = pProperties.getProperty(VERSION_PROPERTY);
    try {
      if (version == null) {
        //no version set
        throw new IllegalArgumentException("Property VERSION_PROPERTY not provided.");
      }
      //check version...throws IllegalArgumentException if invalid
      LOGGER.debug("Validated parser version {}", VersionString.getEnum(version));
    } catch (IllegalArgumentException iae) {
      throw new PropertyValidationException("Failed to parse version from property value " + version + ". Value not part of " + Arrays.toString(VersionString.values()) + ".", iae);
    }
  }

  @Override
  protected String[] getExtractorPropertyKeys() {
    return new String[]{VERSION_PROPERTY};
  }

  @Override
  protected String getExtractorPropertyDescription(String pProperty) {
    if (VERSION_PROPERTY.equals(pProperty)) {
      return "The version which is used to parse the files.";
    }
    return null;
  }

  @Override
  protected void configureExtractor(Properties pProperties) {
    // <editor-fold defaultstate="collapsed" desc="Check and set version property">
    LOGGER.debug("configureExtractor() -- configure properties");
    String version = pProperties.getProperty(VERSION_PROPERTY, VERSION_1_0);
    VersionString actualVersion = VersionString.VERSION10;
    try {
      actualVersion = VersionString.getEnum(version);
    } catch (IllegalArgumentException iae) {
      LOGGER.warn("Failed to parse version from property value {}. Using default value '{}'", version, actualVersion);
    }
    // Test for valid entry
    switch (actualVersion) {
      case VERSION10:
        break;
      case VERSION20:
      default:
        LOGGER.error("'" + actualVersion + "' is not a supported version!");
        throw new UnknownError("'" + actualVersion + "' is not a supported version!");
    }
    // </editor-fold>
    LOGGER.debug("chosen version: " + actualVersion);
  }

  @Override
  public String createMetadataDocument(TransferTaskContainer pContainer) throws StagingProcessorException {
    LOGGER.debug("{}: createCommunitySpecificElement", this.getClass().toString());
    String returnValue = null;

    // <editor-fold defaultstate="collapsed" desc="check for and read metadata.xml ">
    ICollectionNode root = pContainer.getFileTree().getRootNode();
    //initialize hashes list
    LOGGER.debug("Searching for metadata.xml file.");
    //obtain "generated" folder node
    IDataOrganizationNode dataSubTree = Util.getNodeByName(root, Constants.STAGING_DATA_FOLDER_NAME);

    IFileNode metadataFile = (IFileNode) Util.getNodeByName((ICollectionNode) dataSubTree, METADATA_FILENAME);

    if (metadataFile == null) {
      LOGGER.warn("No metadata.xml file found. Skipping meta data parsing.");
    } else {
      try {
        File theFile = new File(new URL(metadataFile.getLogicalFileName().asString()).toURI());
        returnValue = FileUtils.readFileToString(theFile);
                
      } catch (MalformedURLException | URISyntaxException ex) {
        LOGGER.error("Failed to obtain metadata file from URL " + metadataFile.getLogicalFileName().asString() + ".", ex);
      } catch (IOException ex) {
        LOGGER.error("Error reading file from URL " + metadataFile.getLogicalFileName().asString() + ".", ex);
      }
    }
    // </editor-fold>

    return returnValue;
  }

  @Override
  protected Element createCommunitySpecificElement(TransferTaskContainer pContainer) throws MetaDataExtractionException {
    return null;
  }

  @Override
  protected void performPreTransferExtraction(TransferTaskContainer pContainer)  throws StagingProcessorException {
    //do nothing here
    LOGGER.debug("performPreTransferExtraction() -- do nothing");
  }
}
