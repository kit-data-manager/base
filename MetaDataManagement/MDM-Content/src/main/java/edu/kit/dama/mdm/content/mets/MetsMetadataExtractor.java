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
package edu.kit.dama.mdm.content.mets;

import au.edu.apsr.mtk.base.MDTYPE;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.content.es.MetadataIndexingHelper;
import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import edu.kit.dama.mdm.content.mets.plugin.IMetsTypeExtractor;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.mdm.content.mets.util.MetsBuilder;
import edu.kit.dama.mdm.content.mets.util.MetsHelper;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.util.DataManagerSettings;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Function;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This abstract staging processor implementation creates a mets document for an
 * ingested digital object. An implementing class may or may not add custom
 * properties. Furthermore, an implementation may or may not extract community
 * specific metadata added as custom descriptive metadata to the final mets
 * document.
 *
 * The default extractor allows to link the extractor either to a metadata
 * schema defined by the repository or to a standard md type defined by the mets
 * standard.
 *
 * The result mets file is stored in a subfolder named 'metadata' in the
 * 'generated' folder of the ingest. The file will be named
 * 'mets_&lt;oid&gt;.xml where &lt;oid&gt; is the digital object identifier of
 * the associated object.
 *
 * @author jejkal
 */
public abstract class MetsMetadataExtractor extends AbstractStagingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetsMetadataExtractor.class);
    /**
     * Holding meta data information about data.
     */
    private DigitalObject digitalObject = null;
    /**
     * Unique Id of digital object.
     */
    private String digitalObjectIdentifier = null;
    /**
     * Id of the DMD section should be unique for each community.
     */
    private String communityDmdSectionId = null;
    /**
     * Type of the community metadata.
     */
    private String communityMDType = null;
    /**
     * List of additional extractors from mets file.
     */
    private List<IMetsTypeExtractor> additionalExtractors;
    /**
     * Constant for Mets DMD ID.
     */
    private final String COMMUNITY_DMD_SECTION_ID = "communityMetadataDmdId";
    /**
     * Constant for Mets DMD Type.
     */
    private final String COMMUNITY_MD_TYPE_ID = "communityMDType";
    /**
     * Constant for Mets schema Id of community metadata.
     */
    private final String COMMUNITY_METADATA_SCHEMA_ID = "communityMetadataSchemaId";
    /**
     * Schema Id of community metadata. should be unique for each community.
     */
    private String communityMetadataId = null;
    /**
     * Map holding all available extractors.
     */
    private Map<String, IMetsTypeExtractor> extractorMap;
    /**
     * Mets file holding all metadata.
     */
    private MetsBuilder metsBuilder;

    /**
     * Context for ingest authorization.
     */
    private AuthorizationContext ingestAuthorization;

    /**
     * Subdir for metadata in the generated folder.
     */
    protected static final String METADATA_FOLDER_NAME = "metadata";

    /**
     * Default constructor.
     *
     * @param pUniqueIdentifier The unique identifier of this processor.
     */
    public MetsMetadataExtractor(String pUniqueIdentifier) {
        super(pUniqueIdentifier);
        this.additionalExtractors = new ArrayList<>();
        buildExtractorMap();
    }

    /**
     * Get all property keys specific for this mets extractor implementation.
     *
     * @return An array of property keys.
     */
    protected abstract String[] getExtractorPropertyKeys();

    /**
     * Get a description for an extractor-specific property keys.
     *
     * @param pProperty The property key.
     *
     * @return The description.
     */
    protected abstract String getExtractorPropertyDescription(String pProperty);

    /**
     * Validate all extractor-specific properties.
     *
     * @param pProperties The properties mapping.
     *
     * @throws PropertyValidationException if one property is missing or
     * invalid.
     */
    protected abstract void validateExtractorProperties(Properties pProperties) throws PropertyValidationException;

    /**
     * Configure the extractor instance using the provided properties.
     *
     * @param pProperties The properties mapping.
     */
    protected abstract void configureExtractor(Properties pProperties);

    /**
     * Create XML document containing community specific metadata. This element
     * will be inserted in the metadata xml. This method will be called inside
     * the method finalizePostTransferProcessing. (see
     * {@link AbstractStagingProcessor#finalizePostTransferProcessing(edu.kit.dama.rest.staging.types.TransferTaskContainer)})
     *
     * @param pContainer The transfer task container which contains the file
     * tree on the transfer source machine. This should be locally and all files
     * in the tree should be accessible in a posix-like way. This is necessary
     * as some metadata might be extracted from uploaded files.
     *
     * @return community specific metadata as XML document.
     *
     * @throws MetaDataExtractionException Error during extraction.
     */
    protected abstract Document createCommunitySpecificDocument(TransferTaskContainer pContainer) throws MetaDataExtractionException;

    @Override
    public final void performPreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        digitalObjectIdentifier = pContainer.getTransferInformation().getDigitalObjectId();
        LOGGER.debug("performPreTransferExtraction for object with identifier: '{}'", digitalObjectIdentifier);

        if (getDigitalObject() == null) {
            // Load digital object from database 
            IMetaDataManager imdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            imdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
            try {
                DigitalObject result = imdm.findSingleResult("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier=?1", new Object[]{digitalObjectIdentifier}, DigitalObject.class);
                if (result == null) {
                    throw new StagingProcessorException("No DigitalObject found for identifier '" + digitalObjectIdentifier + "'");
                } else {
                    setDigitalObject(result);
                }
            } catch (UnauthorizedAccessAttemptException ex) {
                throw new StagingProcessorException("Not authorized to access digital object with id " + digitalObjectIdentifier, ex);
            } finally {
                imdm.close();
            }
        }
    }

    @Override
    public final void finalizePreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        LOGGER.debug("finalizePreTransferExtraction -- Creating metadata document in mets extractor {}", getName());

        File metsFile = getMetsFile(pContainer);
        try {
            String xmlString = createMetadataDocument(pContainer);
            if (xmlString == null) {
                LOGGER.warn("createMetadataDocument() returned 'null'. XML document {} will be empty.", metsFile.getAbsolutePath());
                xmlString = "";
            }
            FileUtils.writeStringToFile(metsFile, xmlString);
            LOGGER.debug("XML metadata successfully written.");
        } catch (IOException ex) {
            throw new StagingProcessorException("Failed to write metadata to file " + metsFile.getAbsolutePath() + ".", ex);
        }
        generateIndexFiles(metsFile, true);

        try {
            LOGGER.debug("Adding generated content located at {} to transfer container.", metsFile.getParentFile().getParentFile());
            //add /generated folder content to container
            pContainer.addGeneratedFile(metsFile.getParentFile().getParentFile());
        } catch (AdalapiException | MalformedURLException ex) {
            throw new StagingProcessorException("Failed to add generated content to transfer container.", ex);
        }
    }

    @Override
    public final void performPostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        LOGGER.trace("performPostTransferProcessing - Nothing to do");
    }

    @Override
    public final void finalizePostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        LOGGER.trace("finalizePostTransferProcessing - Add Dataorganization and Structure section to Mets document.");

        Function<IDataOrganizationNode, String> defaultObjectNodeResolver = (IDataOrganizationNode node) -> {
            return DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_BASE_URL_ID, "http://localhost:8080")
                    + "/rest/dataorganization/organization/download/" + Long.toString(digitalObject.getBaseId()) + "/" + ((node.getName() != null) ? node.getName() : "");
        };

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            metsBuilder.createDOSection(defaultObjectNodeResolver);
            metsBuilder.createStructureSection(defaultObjectNodeResolver);
            metsBuilder.write(baos);
            File metsFile = getMetsFile(pContainer);
            FileUtils.writeStringToFile(metsFile, baos.toString());
            generateIndexFiles(metsFile, false);
        } catch (IOException ex) {
            LOGGER.error("Error writing mets file", ex);
        } catch (Exception ex) {
            LOGGER.error("Error adding data organization to mets file.", ex);
        }
    }

    /**
     * Get file for METS file. Location depends on state of the transfer.
     *
     * @param pContainer The transfer task container which contains the file
     * tree on the transfer source machine. This should be locally and all files
     * in the tree should be accessible in a posix-like way. This is necessary
     * as some metadata might be extracted from uploaded files.
     * @return File pointing to Mets file.
     * @throws StagingProcessorException If finishing the processor fails.
     */
    private File getMetsFile(TransferTaskContainer pContainer) throws StagingProcessorException {
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
        String xmlFileName = "mets_" + getDigitalObject().getDigitalObjectIdentifier() + ".xml";

        File metsFile = new File(serverGeneratedDir, xmlFileName);
        LOGGER.debug("XML metadata will be written to: '{}'", metsFile.getAbsolutePath());

        return metsFile;
    }

    /**
     * Get the metadata schema linked to this extractor. Basically, a database
     * query for the schemaIdentifier obtained from the properties is done. If
     * nothing was found or if the search fails, <i>null</i> is returned.
     *
     * @return The schema or <i>null</i> if nothing was found.
     */
    public final MetaDataSchema getLinkedSchema() {
        return getLinkedSchema(communityMetadataId);
    }

    /**
     * Get the digital object.
     *
     * @return the digitalObject
     */
    public final DigitalObject getDigitalObject() {
        return digitalObject;
    }

    @Override
    public final String[] getInternalPropertyKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(COMMUNITY_DMD_SECTION_ID);
        keys.add(COMMUNITY_MD_TYPE_ID);
        keys.add(COMMUNITY_METADATA_SCHEMA_ID);
        String[] extractorKeys = getExtractorPropertyKeys();
        if (extractorKeys != null) {
            keys.addAll(Arrays.asList(extractorKeys));
        }
        /**
         * Test for plugins
         */
        extractorKeys = getExtractorPluginsKeys();
        if (extractorKeys.length > 0) {
            keys.addAll(Arrays.asList(extractorKeys));
        }
        String[] returnValue = keys.toArray(new String[keys.size()]);
        if (LOGGER.isTraceEnabled()) {
            StringBuilder types = new StringBuilder();
            for (String type : returnValue) {
                if (types.length() > 0) {
                    types.append(", ");
                }
                types.append(type);
            }
            LOGGER.trace("Found the follwong property keys: [{}]", types.toString());
        }
        return returnValue;
    }

    @Override
    public final String getInternalPropertyDescription(String pKey) {
        String postfix = "\nPossible values: TRUE, FALSE";
        StringBuilder types = new StringBuilder();
        for (MDTYPE type : MDTYPE.values()) {
            if (types.length() > 0) {
                types.append(", ");
            }
            types.append(type.toString());
        }

        if (null != pKey) {
            switch (pKey) {
                case COMMUNITY_DMD_SECTION_ID:
                    return "Id of the descriptive metadata section of the mets document containing the community metadata. This id should be defined in the mets profile of the according community.";
                case COMMUNITY_MD_TYPE_ID:
                    return "Id of an endorsed metadata type defined by the mets standard. Possible types are: [" + types.toString() + "]. If no MDType is defined, an external metadata schema identified via the property " + COMMUNITY_METADATA_SCHEMA_ID + " might be used instead.";
                case COMMUNITY_METADATA_SCHEMA_ID:
                    return "Id of the metadata schema registered at the repository. The schema url is used as OTHERMDTYPE attribute in the according descriptive metadata section.";
                default:
                    /**
                     * Test if property is part of a plugin.
                     */
                    IMetsTypeExtractor plugin = extractorMap.get(pKey);
                    if (plugin != null) {
                        return plugin.getDescription() + postfix;
                    }
                    return getExtractorPropertyDescription(pKey);
            }
        }
        String message = "No custom property with key " + pKey + " supported.";
        LOGGER.warn(message);

        return message;
    }

    @Override
    public final void validateProperties(Properties pProperties) throws PropertyValidationException {
        LOGGER.debug("Validating mets metadata extractor named {}", getName());
        if (pProperties.getProperty(COMMUNITY_DMD_SECTION_ID) == null) {
            // LOGGER.warn("No {} property defined for mets extractor named {}. No community metadata will be added to mets documents created by this extractor.", COMMUNITY_DMD_SECTION_ID, getName());
            throw new PropertyValidationException("Mandatory property '" + COMMUNITY_DMD_SECTION_ID + "' is missing.");
        }
        //Does this check make any sense?
        /* if (pProperties.getProperty(COMMUNITY_METADATA_SCHEMA_ID) != null && pProperties.getProperty(COMMUNITY_MD_TYPE_ID) != null) {
           LOGGER.warn("Both, property {} and property {}, are defined for mets extractor named {}. The value of property {} will be ignored.", COMMUNITY_METADATA_SCHEMA_ID, COMMUNITY_MD_TYPE_ID, getName(), COMMUNITY_METADATA_SCHEMA_ID);
        }*/
        if (pProperties.getProperty(COMMUNITY_METADATA_SCHEMA_ID) == null) {
            throw new PropertyValidationException("Mandatory property '" + COMMUNITY_METADATA_SCHEMA_ID + "' is missing.");
        } else {
            if (getLinkedSchema(pProperties.getProperty(COMMUNITY_METADATA_SCHEMA_ID)) == null) {
                throw new PropertyValidationException("The value of '" + COMMUNITY_METADATA_SCHEMA_ID + "' does not refer to a valid schema identifier.");
            }
        }

        if (pProperties.getProperty(COMMUNITY_MD_TYPE_ID) == null) {
            throw new PropertyValidationException("Mandatory property '" + COMMUNITY_MD_TYPE_ID + "' is missing.");
        }

        String mdType = pProperties.getProperty(COMMUNITY_MD_TYPE_ID);
        if (mdType != null) {
            try {
                LOGGER.debug("Successfully checked value {} of property {}", MDTYPE.valueOf(mdType), COMMUNITY_MD_TYPE_ID);
            } catch (IllegalArgumentException ex) {
                throw new PropertyValidationException("Property " + COMMUNITY_MD_TYPE_ID + " has an invalid value.", ex);
            }
        }
        for (String plugins : extractorMap.keySet()) {
            IMetsTypeExtractor plugin = extractorMap.get(plugins);
            if (pProperties.containsKey(plugins)) {
                validatePluginString(plugins, pProperties.getProperty(plugins));
            } else {
                LOGGER.warn("New plugin '{}' available! As it is not configured yet it will be ignored. Please "
                        + "reconfigure extractor '{}' if you want to support this plugin!", plugin.getName(), getClass().toString());
            }
        }
        LOGGER.debug("Performing validation of extractor-specific properties.");
        validateExtractorProperties(pProperties);
    }

    @Override
    public final void configure(Properties pProperties) throws PropertyValidationException, ConfigurationException {
        LOGGER.debug("Configuring mets metadata extractor named {}", getName());
        communityDmdSectionId = pProperties.getProperty(COMMUNITY_DMD_SECTION_ID);
        communityMetadataId = pProperties.getProperty(COMMUNITY_METADATA_SCHEMA_ID);
        communityMDType = pProperties.getProperty(COMMUNITY_MD_TYPE_ID);

        if (communityDmdSectionId == null || communityMetadataId == null || communityMDType == null) {
            throw new ConfigurationException("Missing mandatory properties detected. Unable to instantiate metadata extractor.");
        }

        LOGGER.debug("Performing extractor-specific configuration.");
        for (String pluginKey : extractorMap.keySet()) {
            IMetsTypeExtractor plugin = extractorMap.get(pluginKey);
            LOGGER.debug("Check for plugin '{}'.", plugin.getName());
            if (pProperties.containsKey(pluginKey)) {
                LOGGER.debug("Property for plugin '{}' exists.", plugin.getName());
                if (validatePluginString(pluginKey, pProperties.getProperty(pluginKey))) {
                    LOGGER.debug("Add plugin '{}'!", plugin.getName());
                    additionalExtractors.add(plugin);
                }
            }
        }
        configureExtractor(pProperties);
    }

    /**
     * Create a METS document compiling all existing metadata.
     *
     * @param pContainer Container holding all files.
     * @return METS document as String.
     * @throws StagingProcessorException Error during metadata extraction.
     */
    public final String createMetadataDocument(TransferTaskContainer pContainer) throws StagingProcessorException {
        UserData creator = null;
        String owner = pContainer.getTransferInformation().getOwnerId();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        ingestAuthorization = new AuthorizationContext(new UserId(pContainer.getTransferInformation().getOwnerId()), new GroupId(pContainer.getTransferInformation().getGroupId()), Role.MEMBER);

        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());

        try {
            creator = mdm.findSingleResult("SELECT u FROM UserData u WHERE u.distinguishedName=?1", new Object[]{owner}, UserData.class);
        } catch (UnauthorizedAccessAttemptException ex) {
            //ignore...the mets builder will handle a missing creator later on
        } finally {
            mdm.close();
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            LOGGER.debug("Creating basic mets document.");
            metsBuilder = MetsBuilder.init(getDigitalObject()).createMinimalMetsDocument(creator);

            LOGGER.debug("Creating community specific descriptive metadata document.");
            Document doc = createCommunitySpecificDocument(pContainer);
            if (doc == null) {
                throw new Exception("No community metadata element was returned. Assuming ingest of invalid digital object.");
            }

            LOGGER.debug("Determining md type and schema location for community specific descriptive metadata section.");
            MetaDataSchema linkedSchema = getLinkedSchema();
            MDTYPE schemaType = MDTYPE.OTHER;
            String schemaLocation = null;
            if (communityMDType != null) {
                LOGGER.debug("Using provided MDTYPE element {}.", communityMDType);
                schemaType = MDTYPE.valueOf(communityMDType);
                //schemaLocation remains null as it is already defined in the mets profile
            } else if (linkedSchema != null) {
                LOGGER.debug("Using MDTYPE.OTHER and provided metadata schema url {}.", linkedSchema.getMetaDataSchemaUrl());
                //schemaType remains OTHER as another schema is used
                schemaLocation = linkedSchema.getMetaDataSchemaUrl();
            }
            LOGGER.debug("Adding custom descriptive metadata section to mets document.");
            metsBuilder.addCustomDmdSection(communityDmdSectionId, schemaType, schemaLocation, doc).write(bout);
            LOGGER.debug("Returning final mets document.");
            return bout.toString();
        } catch (Exception e) {
            throw new StagingProcessorException("Failed to create METS document for transfer #" + pContainer.getTransferId(), e);
        }
    }

    /**
     * Set the digital object.
     *
     * @param digitalObject the digitalObject to set
     */
    private void setDigitalObject(DigitalObject digitalObject) {
        this.digitalObject = digitalObject;
    }

    /**
     * Look for all existing plugins implementing the IMetsTypeExtractor
     * interface. Add all found plugins to the extractorMap.
     *
     * @see #extractorMap
     * @return String array containing all found schema identifiers.
     */
    private String[] getExtractorPluginsKeys() {
        return extractorMap.keySet().toArray(new String[0]);
    }

    private void buildExtractorMap() {
        LOGGER.debug("Looking for 'MetsTypeExtractor' plugins!");
        String prefix = "Plugin:";
        extractorMap = new HashMap();
        for (IMetsTypeExtractor plugin : ServiceLoader.load(IMetsTypeExtractor.class)) {
            String name = plugin.getName();
            String namespace = plugin.getNamespace();
            String pluginKey = prefix + name;
            LOGGER.info("Found plugin for schema identifier '{}'!", name);
            // Check if schema is 'registered' as Metadataschema.

            if (checkForSchema(name) || namespace.length() > 0) { // dummy test 
                if (extractorMap.containsKey(pluginKey)) {
                    String className = extractorMap.get(pluginKey).getClass().toString();
                    LOGGER.error("Two plugins with the same schema identifier '{}' exists: {} / {}", name, className, plugin.getClass().toString());
                }
                extractorMap.put(pluginKey, plugin);
            }
        }
    }

    /**
     * Validate input for plugins ignoring case. Only 'TRUE' and 'FALSE' are
     * allowed. If value is 'null' false will be returned.
     *
     * @param pPlugin Name (schema identifier) of the plugin
     * @param pTrueOrFalse Value of the plugin.
     * @return true if the value is 'TRUE'
     * @throws PropertyValidationException Invalid input detected.
     */
    private boolean validatePluginString(String pPlugin, String pTrueOrFalse) throws PropertyValidationException {
        boolean yesOrNo;
        if (pTrueOrFalse != null) {
            if (pTrueOrFalse.equalsIgnoreCase("true")) {
                yesOrNo = true;
            } else if (pTrueOrFalse.equalsIgnoreCase("false")) {
                yesOrNo = false;
            } else {
                throw new PropertyValidationException("Property '" + pPlugin + "' has to be TRUE or FALSE.");
            }
        } else {
            LOGGER.warn("No value available for plugin '{}'. It may be added later.", pPlugin);
            yesOrNo = false;
        }
        return yesOrNo;
    }

    /**
     * Get the metadata schema linked to this extractor. Basically, a database
     * query for the schemaIdentifier obtained from the properties is done. If
     * nothing was found or if the search fails, <i>false</i> is returned.
     *
     * @param pSchemaIdentifier identifier of the schema. (identifier has to be
     * unique)
     *
     * @return False if schema is not defined or there are more than one schema
     * with the same identifier.
     */
    private boolean checkForSchema(String pSchemaIdentifier) {
        boolean schemaDefined = (getLinkedSchema(pSchemaIdentifier) != null);

        return schemaDefined;
    }

    /**
     * Get the MetaDataSchema linked with the given schema identifier. The
     * schema identifier has to be unique.
     *
     * @param pSchemaIdentifier schema identifier of the schema.
     * @return MetaDataSchema of given schema identifier.
     */
    private MetaDataSchema getLinkedSchema(String pSchemaIdentifier) {
        MetaDataSchema metadataSchema = null;
        //check in backend
        IMetaDataManager imdm = null;
        try {
            imdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
            imdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
            List<MetaDataSchema> find = imdm.findResultList("SELECT s FROM MetaDataSchema s WHERE s.schemaIdentifier=?1", new Object[]{pSchemaIdentifier}, MetaDataSchema.class);

            if (!find.isEmpty()) {
                if (find.size() > 1) {
                    LOGGER.warn("There are {} schemas registered for schema identifier '{}'. This may cause problems.", find.size(), pSchemaIdentifier);
                }
                metadataSchema = find.get(0);
            } else {
                LOGGER.warn("No metadata schema with id '{}'.", pSchemaIdentifier);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.warn("Failed to check for existence of metadata schema with id " + pSchemaIdentifier + ". Skipping check.", ex);
        } finally {
            if (imdm != null) {
                imdm.close();
            }
        }
        //search failed at all
        return metadataSchema;

    }

    /**
     * Generate all selected index files from given METS document.
     *
     * @param pSourceFile METS document.
     * @param test Generate files without content.
     */
    private void generateIndexFiles(File pSourceFile, boolean test) throws StagingProcessorException {
        String xmlString = "Dry run";
        if (test) {
            LOGGER.debug("Generate dummy index files. (Mets file: '{}')", pSourceFile.getAbsolutePath());
        } else {
            LOGGER.debug("Generate index files and schedule them for indexing. (Mets file: '{}')", pSourceFile.getAbsolutePath());
        }
        String targetDir = pSourceFile.getParentFile().getAbsolutePath() + File.separator;
        String postfixFileName = "_" + getDigitalObject().getDigitalObjectIdentifier() + ".xml";
        try {
            // first write the community specific part
            File communityIndexFile = new File(targetDir + communityMetadataId + postfixFileName);
            String xPath = String.format("mets:dmdSec[@ID=\"%s\"]/mets:mdWrap/mets:xmlData/*", communityDmdSectionId);
            LOGGER.debug("Extract from file '{}' with xPath '{}'.", communityIndexFile.getAbsolutePath(), xPath);
            if (!test) {
                xmlString = MetsHelper.extractFromFile(pSourceFile, xPath, Namespace.getNamespace("mets", "http://www.loc.gov/METS/"));
            }
            FileUtils.writeStringToFile(communityIndexFile, xmlString);
            if (!test) {
                schedule4Index(communityIndexFile, communityMetadataId.toLowerCase());
            }
        } catch (IOException ex) {
            throw new StagingProcessorException("Error writing community index file.", ex);
        }

        // Write index files from all selected plugins
        for (IMetsTypeExtractor item : additionalExtractors) {
            LOGGER.debug("Add extractor '{}' to indexing. ", item.getName());
            File pluginFile = new File(targetDir + item.getName() + postfixFileName);
            try {
                String xmlStringPlugin = "dry run";
                if (!test) {
                    xmlStringPlugin = item.extractType(pSourceFile);
                }
                FileUtils.writeStringToFile(pluginFile, xmlStringPlugin);
                if (!test) {
                    schedule4Index(pluginFile, item.getName());
                }
            } catch (IOException ex) {
                throw new StagingProcessorException("Error writing mets index file: " + pluginFile.getAbsolutePath(), ex);
            }
        }
    }

    /**
     * Add generated XML file to MetadataIndexingHelper. Files are only indexed
     * if given schema identifier is registered in the MetadataSchema table.
     *
     * @param xmlFile file name of the generated XML file.
     * @param schemaIdentifier schema identifier
     */
    private void schedule4Index(File xmlFile, String schemaIdentifier) {
        MetaDataSchema linkedSchema = getLinkedSchema(schemaIdentifier);
        if (linkedSchema != null) {
            MetadataIndexingHelper.getSingleton().scheduleIndexingTask(
                    getDigitalObject().getDigitalObjectId(),
                    new AbstractFile(xmlFile),
                    linkedSchema,
                    ingestAuthorization);
            LOGGER.debug("Indexing task for metadata schema with id '{}' successfully scheduled.", schemaIdentifier);
        } else {
            LOGGER.warn("Linked metadata schema with id '" + schemaIdentifier + "' not found. Registration of indexing task skipped.");
        }
    }

    /**
     * Create an 'empty' document with the linked namespace. Only needed if no
     * content metadata exists.
     *
     * @return Empty document.
     */
    protected Document createEmptyDocument() {
        Document xmlDocument = null;
        // <editor-fold defaultstate="collapsed" desc="create empty xml ">
        try {
            LOGGER.debug("Create empty document.");
            xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            MetaDataSchema linkedSchema = getLinkedSchema();
            StringBuilder elementName = new StringBuilder();
            StringBuilder attribute = new StringBuilder("xmlns");
            if (linkedSchema != null) {
                elementName.append(linkedSchema.getSchemaIdentifier()).append(":");
                attribute.append(":").append(linkedSchema.getSchemaIdentifier());
            }
            elementName.append("nometadata");
            Element rootElement = xmlDocument.createElement(elementName.toString());
            if (linkedSchema != null) {
                rootElement.setAttribute(attribute.toString(), linkedSchema.getMetaDataSchemaUrl());
            }
            xmlDocument.appendChild(rootElement);
        } catch (ParserConfigurationException ex) {
            LOGGER.error("Error creating empty document.", ex);
        }
        // </editor-fold>

        return xmlDocument;
    }
}
