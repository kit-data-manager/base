/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.tools.BaseMetaDataHelper;
import edu.kit.dama.mdm.base.xml.DigitalObject2Xml;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.mdm.content.es.MetadataIndexingHelper;
import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.fzk.tools.xml.JaxenUtil;
import org.w3c.dom.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Extract the metadata from a dataset. This class has to be the parent class of
 * all metadata extractors of all communities. It obtains the BaseMetadata which
 * contains the administrative metadata available for all datasets. The
 * extracted metadata is stored in a file next to the data.<br/>
 * <b>Structure:</b>
 * <ul><li>staging directory</li>
 * <ul> <li>data/ - holding raw data of dataset</li>
 * <li>generated/metadata/&lt;metadataSchemaIdentifier&gt;_&lt;DigitalObjectIdentifier&gt;.xml
 * - holding metadata of the dataset obtained by an according extractor</li>
 * </ul>
 * </ul>
 *
 * By default this extractor used the standard KIT Data Manager extraction
 * schema with the schema identifier BaseMetaDataHelper.DAMA_NAMESPACE_PREFIX.
 * This schema contains the base metadata and the wrapped community metadata in
 * a special section. The content of the section can be filled by implementing
 * the abstract method <i>createCommunitySpecificElement()</i>. In case an
 * entirely different schema should be used,
 * <i>createMetadataDocument()</i> has to be overwritten.
 *
 * @author hartmann-v
 */
public abstract class AbstractMetadataExtractor extends AbstractStagingProcessor {

  /**
   * The logger
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMetadataExtractor.class);
  public final static String METADATA_SCHEMA_IDENTIFIER_KEY = "METADATA_SCHEMA_IDENTIFIER";
  /**
   * Holding meta data information about data.
   */
  private DigitalObject digitalObject = null;
  /**
   * Unique Id of digital object.
   */
  private String digitalObjectIdentifier = null;
  /**
   * Unique identifier of the used metadata schema.
   */
  private String schemaIdentifier = BaseMetaDataHelper.DAMA_NAMESPACE_PREFIX;

  /**
   * Subdir for metadata in the generated folder.
   */
  protected final String METADATA_FOLDER_NAME = "metadata";

  /**
   * Default constructor.
   *
   * @param pUniqueIdentifier The unique identifier of this processor. This
   * identifier should be used to name generated output files associated with
   * this processor.
   */
  public AbstractMetadataExtractor(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
  }

  /**
   * Get the metadata schema linked to this extractor. Basically, a database
   * query for the schemaIdentifier obtained from the properties is done. If
   * nothing was found or if the search fails, <i>null</i> is returned.
   *
   * @return The schema or <i>null</i> if nothing was found.
   */
  private MetaDataSchema getLinkedSchema() {
    //check in backend
    try {
      IMetaDataManager imdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
      imdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
      MetaDataSchema schema = new MetaDataSchema(schemaIdentifier);
      List<MetaDataSchema> find = imdm.find(schema, schema);
      if (find.isEmpty()) {
        //nothing found
        return null;
      } else if (find.size() > 1) {
        LOGGER.warn("There are " + find.size() + " schemas registered for schema identifiert '" + schemaIdentifier + "'. This may cause problems. For the moment, the first entry will be returned.");
      }
      //one ore more entries found
      return find.get(0);
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.warn("Failed to check for existence of metadata schema with id " + schemaIdentifier + ". Skipping check.", ex);
    }
    //search failed at all
    return null;
  }

  @Override
  public void validateProperties(Properties pProperties) throws PropertyValidationException {
    String schemaId = pProperties.getProperty(METADATA_SCHEMA_IDENTIFIER_KEY);
    if (schemaId == null) {
      // throw new PropertyValidationException("Property '" + METADATA_SCHEMA_IDENTIFIER_KEY + "' is not set.");
      LOGGER.info("Property {} is not provided. Using default metadata schema with identifier {}", METADATA_SCHEMA_IDENTIFIER_KEY, BaseMetaDataHelper.DAMA_NAMESPACE_PREFIX);
      schemaId = BaseMetaDataHelper.DAMA_NAMESPACE_PREFIX;
    }
    schemaIdentifier = schemaId;
    //check schema
    if (getLinkedSchema() == null) {
      LOGGER.warn("No metadata schema found for id " + schemaId + ". We'll ignore this, but validation or further use of the extracted metadata may fail at a later point.");
    }

    validateExtractorProperties(pProperties);
  }

  protected abstract void validateExtractorProperties(Properties pProperties) throws PropertyValidationException;

  @Override
  public String[] getInternalPropertyKeys() {
    List<String> keys = new ArrayList<>();
    keys.add(METADATA_SCHEMA_IDENTIFIER_KEY);
    String[] extractorKeys = getExtractorPropertyKeys();
    if (extractorKeys != null) {
      keys.addAll(Arrays.asList(extractorKeys));
    }
    return keys.toArray(new String[keys.size()]);
  }

  protected abstract String[] getExtractorPropertyKeys();

  @Override
  public String getInternalPropertyDescription(String pProperty) {
    if (METADATA_SCHEMA_IDENTIFIER_KEY.equals(pProperty)) {
      return "The identifier of the metadata schema for which metadata will be extracted.";
    }
    if (pProperty == null) {
      return null;
    } else {
      return getExtractorPropertyDescription(pProperty);
    }
  }

  @Override
  public String[] getUserPropertyKeys() {
    //User-defined property keys are currently not used by any extractor.
    //If they are relevant for any extractor, this and the next method can be overwritten.
    return new String[]{};
  }

  @Override
  public String getUserPropertyDescription(String pKey) {
    //User-defined property keys are currently not used by any extractor.
    //If they are relevant for any extractor, this and the previous method can be overwritten.
    return null;
  }

  protected abstract String getExtractorPropertyDescription(String pProperty);

  @Override
  public void configure(Properties pProperties) {
    LOGGER.debug("Obtaining schema identifier.");
    schemaIdentifier = pProperties.getProperty(METADATA_SCHEMA_IDENTIFIER_KEY);
    LOGGER.debug("Schema identifier set to {}.", schemaIdentifier);
    configureExtractor(pProperties);
  }

  protected abstract void configureExtractor(Properties pProperties);

  @Override
  public final void performPreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    performPreTransferExtraction(pContainer);
  }

  /**
   * Perform the pre-transfer processing using this processor for the transfer
   * described by pContainer. The pre-processing is done where the transfer
   * comes from, e.g. on the ingest machine for ingests or on the server machine
   * for downloads. In case of metadata extraction typically the extraction is
   * done only for ingests and on the server side during post transfer
   * processing.
   *
   * @param pContainer The transfer task container which contains the file tree
   * on the transfer source machine. This should be locally and all files in the
   * tree should be accessible in a posix-like way.
   *
   * @throws StagingProcessorException If the processor fails.
   * @see
   * StagingProcessor#performPreTransferProcessing(edu.kit.dama.staging.entities.TransferTaskContainer)
   */
  protected abstract void performPreTransferExtraction(TransferTaskContainer pContainer) throws StagingProcessorException;

  @Override
  public final void finalizePreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    // will not be called on server side!
  }

  /**
   * Create XML element containing community specific metadata. This element
   * will be inserted in the metadata xml. This method will be called inside the
   * method finalizePostTransferProcessing
   *
   * @param pContainer The transfer task container which contains the file tree
   * on the transfer source machine. This should be locally and all files in the
   * tree should be accessible in a posix-like way. This is necessary as some
   * metadata might be extracted from uploaded files.
   *
   * @return community specific metadata as XML element.
   *
   * @throws MetaDataExtractionException Error during extraction.
   * @see
   * StagingProcessor#performPreTransferProcessing(edu.kit.dama.staging.entities.TransferTaskContainer)
   */
  protected abstract Element createCommunitySpecificElement(TransferTaskContainer pContainer) throws MetaDataExtractionException;

  @Override
  public final void performPostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    performPostTransferExtraction(pContainer);
  }

  /**
   * Perform the post-transfer processing using this processor for the transfer
   * described by pContainer. The post-processing is done at the destination of
   * the transfer, e.g. on the server machine for ingests or on the user desktop
   * for downloads. For the metadata extraction this method is used to obtain
   * the digital object associated with the transfer.
   *
   * @param pContainer The transfer task container which contains the file tree
   * on the transfer destination machine. This should be locally and all files
   * in the tree should be accessible in a posix-like way.
   *
   * @throws StagingProcessorException If the processor fails.
   * @see
   * StagingProcessor#performPreTransferProcessing(edu.kit.dama.staging.entities.TransferTaskContainer)
   */
  private void performPostTransferExtraction(TransferTaskContainer pContainer) throws StagingProcessorException {
    digitalObjectIdentifier = pContainer.getTransferInformation().getDigitalObjectId();
    LOGGER.debug("performPostTransferExtraction for object with identifier: '{}'", digitalObjectIdentifier);

    // <editor-fold defaultstate="collapsed" desc="Load DigitalObject from database">
    if (getDigitalObject() == null) {
      // Load digital object from database 
      DigitalObject dObj = DigitalObject.factoryNewDigitalObject(digitalObjectIdentifier);
      try {
        IMetaDataManager imdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        imdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        List<DigitalObject> find = imdm.find(dObj, dObj);
        if (find.isEmpty()) {
          throw new StagingProcessorException("No DigitalObject found for identifier '" + digitalObjectIdentifier + "'");
        } else {
          setDigitalObject(find.get(0));
          if (find.size() > 1) {
            LOGGER.warn("More than one object with identifier '{}' found! "
                    + "Found {} results!?", digitalObjectIdentifier, find.size());
          }
        }
      } catch (UnauthorizedAccessAttemptException ex) {
        throw new StagingProcessorException("Not authorized to access digital object with id " + digitalObjectIdentifier, ex);
      }
    }
    // </editor-fold>
  }

  @Override
  public final void finalizePostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    finalizePostTransferExtraction(pContainer);
  }

  /**
   * Create method for building the metadata document. By default, the
   *
   * @param pContainer The TransferTaskContainer containing the FileTree of all
   * uploaded data for the case, that metadata is extracted from some of these
   * files.
   *
   * @return The string which contains the XML document with all extracted
   * metadata.
   *
   * @throws StagingProcessorException If anything fails and the metadata
   * document could not be created.
   */
  public String createMetadataDocument(TransferTaskContainer pContainer) throws StagingProcessorException {
    try {
      return createXML(createCommunitySpecificElement(pContainer));
    } catch (MetaDataExtractionException ex) {
      throw new StagingProcessorException("Failed to create metadata document.", ex);
    }
  }

  /**
   * Create the entire XML document string containing the basemetadata and the
   * community specific metadata for the default metadata schema
   * BaseMetaDataHelper.DAMA_NAMESPACE_PREFIX.
   *
   * @param rootElement Root element of the community specific part.
   *
   * @return XML string with the complete metadata.
   *
   * @throws MetaDataExtractionException Error during extraction.
   */
  private String createXML(Element rootElement) throws MetaDataExtractionException {
    Document docDigitalObject = null;

    String completeXml = null;

    String baseXML = DigitalObject2Xml.getXmlString(getDigitalObject());
    LOGGER.debug("createXML: {}", baseXML);
    LOGGER.debug("Extended version: {}", rootElement != null);
    try {
      ByteArrayInputStream bin = new ByteArrayInputStream(baseXML.getBytes());
      docDigitalObject = JaxenUtil.getW3CDocument(bin);//XMLTools.parseDOM(baseXML);
    } catch (Exception exc) {
      throw new MetaDataExtractionException("Failed to transform DigitalObject XML.", exc);
    }

    Element digitalObjectElement = docDigitalObject.getDocumentElement();

    Document completeDocument = null;
    try {
      completeDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (ParserConfigurationException ex) {
      throw new MetaDataExtractionException("Failed to generate target document.", ex);
    }
    Element root = completeDocument.createElementNS(BaseMetaDataHelper.DAMA_NAMESPACE_BASEMETADATA, BaseMetaDataHelper.DAMA_NAMESPACE_PREFIX);
    root.appendChild(completeDocument.importNode(digitalObjectElement, true));
    Node csmdRoot = root.appendChild(completeDocument.createElementNS(BaseMetaDataHelper.DAMA_NAMESPACE_METADATA, BaseMetaDataHelper.CSMD_NAMESPACE_PREFIX));
    if (rootElement != null) {
      csmdRoot.appendChild(completeDocument.importNode(rootElement, true));
    }
    completeDocument.appendChild(root);
    root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", BaseMetaDataHelper.DAMA_NAMESPACE_METADATA + " " + BaseMetaDataHelper.DAMA_NAMESPACE_BASEMETADATA + "/MetaData.xsd");
    // convert tweaked DOM back to XML string
    try {
      completeXml = getStringFromDocument(completeDocument);//XMLTools.getXML(completeDocument);
    } catch (Exception exc) {
      throw new MetaDataExtractionException("Internal XML conversion error.", exc);
    }
    LOGGER.debug("completeXML: {}", completeXml);
    return completeXml;
  }

  private String getStringFromDocument(Document doc) throws TransformerException {
    DOMSource domSource = new DOMSource(doc);
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.transform(domSource, result);
    return writer.toString();
  }

  /**
   * Finalize the post-transfer extraction. In this processing the actual
   * extraction takes place. The content of the associated digital object will
   * be merged with a community specific metadata element obtained via
   * createCommunitySpecificElement(pContainer). Afterwards, the result is
   * stored in the 'generated/metadata/' folder of the transfer and optionally
   * in an OAI-PMH indexer directorschemy on the server side.
   *
   * @param pContainer The transfer task container which contains the file tree
   * on the transfer destination machine. This should be locally and all files
   * in the tree should be accessible in a posix-like way.
   *
   * @throws StagingProcessorException If finishing the processor fails.
   * @see
   * StagingProcessor#performPreTransferProcessing(edu.kit.dama.staging.entities.TransferTaskContainer)
   */
  private void finalizePostTransferExtraction(TransferTaskContainer pContainer) throws StagingProcessorException {
    LOGGER.debug("Creating metadata document in TransferOP {}", getName());
    String xmlString = createMetadataDocument(pContainer);

    // ok, all meta data collected write data to file(s)
    String metadataDir;
    File serverGeneratedDir;
    try {
      metadataDir = new File(pContainer.getGeneratedUrl().toURI()).getAbsolutePath() + File.separator + METADATA_FOLDER_NAME;
      serverGeneratedDir = new File(metadataDir);
      serverGeneratedDir.mkdir();
    } catch (URISyntaxException ex) {
      LOGGER.error("Failed to obtain metadata destination directory.", ex);
      throw new StagingProcessorException("Failed to obtain metadata destination directory.", ex);
    }

    String xmlFileName = getSchemaIdentifier() + "_" + getDigitalObject().getDigitalObjectIdentifier() + ".xml";
    File xmlFile = new File(serverGeneratedDir, xmlFileName);
    LOGGER.debug("XML metadata will be written to: '{}'", xmlFile.getAbsolutePath());
    FileOutputStream fout = null;
    try {
      //MicroscopyMetaDataStream.writeToFile(xmlFile, xmlString);
      fout = new FileOutputStream(xmlFile);
      fout.write(xmlString.getBytes());
      fout.flush();
      LOGGER.debug("XML metadata successfully written. Adding file {} to transfer container for archiving.", xmlFile);
      pContainer.addGeneratedFile(xmlFile);
      LOGGER.debug("File successully added to transfer container. Registering indexing task.");
      MetaDataSchema linkedSchema = getLinkedSchema();
      if (linkedSchema != null) {
        MetadataIndexingHelper.getSingleton().scheduleIndexingTask(
                new DigitalObjectId(pContainer.getTransferInformation().getDigitalObjectId()),
                new AbstractFile(xmlFile),
                getLinkedSchema(),
                new AuthorizationContext(new UserId(pContainer.getTransferInformation().getOwnerId()), new GroupId(pContainer.getTransferInformation().getGroupId()), Role.MEMBER));
        LOGGER.debug("Indexing task successfully scheduled.");
      } else {
        LOGGER.warn("Linked metadata schema with id '" + schemaIdentifier + "' not found. Registration of indexing task skipped.");
      }
    } catch (IOException ex) {
      LOGGER.error("Failed to write metadata to file " + xmlFile.getAbsolutePath() + ".", ex);
    } finally {
      if (fout != null) {
        try {
          fout.close();
        } catch (IOException e) {//ignore
        }
      }
    }
  }

  /**
   * Get the digital object.
   *
   * @return the digitalObject
   */
  protected DigitalObject getDigitalObject() {
    return digitalObject;
  }

  /**
   * Set the digital object.
   *
   * @param digitalObject the digitalObject to set
   */
  protected void setDigitalObject(DigitalObject digitalObject) {
    this.digitalObject = digitalObject;
  }

  /**
   * Get the identifier of the metadata schema used by this extractor.
   *
   * @return The identifier.
   */
  protected String getSchemaIdentifier() {
    return schemaIdentifier;
  }

  /**
   * Set the identifier of the metadata schema used by this extractor.
   *
   * @param schemaIdentifier The identifier.
   */
  protected void setSchemaIdentifier(String schemaIdentifier) {
    this.schemaIdentifier = schemaIdentifier;
  }

  /**
   * Test routine for local usage. hsqldb has to be added to the dependencies.
   *
   * @param args no arguments will be parsed.
   */
//  public static void main(String[] args) {
//    ExtractSpimMetadata esm = new ExtractSpimMetadata("without any influence for me!");
//    String[] keys = esm.getPropertyKeys();
//    System.out.println("Supported keys: " + Arrays.toString(keys));
//    for (String key : keys) {
//      System.out.printf("Property description for key '%s': %s\n", key, esm.getPropertyDescription(key));
//    }
//    System.out.println("Name: " + esm.getName());
//
//    Properties properties = new Properties();
//    properties.setProperty(VERSION_PROPERTY, VERSION_1_0);
//    properties.setProperty(OAI_PMH_DIR_PROPERTY, "");
////    properties.setProperty(OAI_PMH_DIR_PROPERTY, "/tmp/extraction/OAIPMH");
//    try {
//      esm.validateProperties(properties);
//    } catch (PropertyValidationException ex) {
//      LOGGER.error(null, ex);
//    }
//    esm.configure(properties);
//    ITransferInformation iti = new IngestInformation(new DigitalObjectId("anyUniqueIdentifier"));
//    File dataDir = new File("/LSDF/download/andrey/12_12_19/");
//    File outputDir = new File("/tmp/extraction/output");
//
//    Collection<File> files = FileUtils.listFiles(dataDir, null, true);
//
//    for (File file : files) {
//      try {
//        System.out.println(file.getAbsolutePath());
//        esm.executeOnServerSide(iti, file, dataDir, outputDir);
//      } catch (StagingProcessorException ex) {
//        LOGGER.error(null, ex);
//      }
//    }
}
