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
package edu.kit.dama.mdm.content.oaipmh.impl;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.content.oaipmh.util.OAIPMHBuilder;
import edu.kit.dama.mdm.content.util.DublinCoreHelper;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.util.DataManagerSettings;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.openarchives.oai._2.DeletedRecordType;
import org.openarchives.oai._2.DescriptionType;
import org.openarchives.oai._2.GranularityType;
import org.openarchives.oai._2.OAIPMHerrorcodeType;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import edu.kit.dama.mdm.content.oaipmh.AbstractOAIPMHRepository;
import edu.kit.dama.util.Constants;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.openarchives.oai._2.ResumptionTokenType;
import org.slf4j.LoggerFactory;

/**
 * Simple OAI-PMH repository implementation taking its information from a KIT
 * Data Manager instance. Metadata formats are obtained from the metadata schema
 * table. The according metadata documents are the ones extracted during ingest
 * and stored in the metadata indexing table. By default, this implementation
 * supports the Dublin Core metadata format. If no metadata extraction is
 * performed during ingest and no metadata schemas are registered, the according
 * Dublin Core documents are created on the fly by this implementation. This
 * offers at least basic OAI-PMH support out of the box for each repository
 * based on KIT Data Manager.
 *
 * This OAI-PMH repository identifies itself with the following properties:
 *
 * <pre>
 * Name: DefaultRepository
 * RepositoryDescription: Empty (might be Re3Data Identification alter on)
 * DeletedRecordSupport: No
 * AdminEmail: DataManagerSettings.GENERAL_SYSTEM_MAIL_ADDRESS
 * EarliestDatestamp: 1970-01-01T00:00:00Z
 * Granularity: YYYY_MM_DD_THH_MM_SS_Z
 * BaseUrl: DataManagerSettings.GENERAL_BASE_URL_ID + /oaipmh
 * </pre>
 *
 * OAI-PMH sets are currently not supported. However, listing all sets returns a
 * single element 'default' and all records are defined to be in set 'default'.
 *
 * @author jejkal
 */
public class SimpleOAIPMHRepository extends AbstractOAIPMHRepository {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SimpleOAIPMHRepository.class);

    private final MetaDataSchema DC_SCHEMA = new MetaDataSchema("dc", "http://www.openarchives.org/OAI/2.0/oai_dc/", "http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
    private final int maxElementsPerList = 100;

    /**
     * Default constructor.
     */
    public SimpleOAIPMHRepository() {
        super("DefaultRepository");
    }

    /**
     * Default constructor.
     *
     * @param name The repository name.
     */
    public SimpleOAIPMHRepository(String name) {
        super(name);
    }

    @Override
    public List<DescriptionType> getRepositoryDescription() {
        return new ArrayList<>();
    }

    @Override
    public DeletedRecordType getDeletedRecordSupport() {
        return DeletedRecordType.NO;
    }

    @Override
    public List<String> getAdminEmail() {
        return Arrays.asList(DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_SYSTEM_MAIL_ADDRESS, "dama@kit.edu"));
    }

    @Override
    public String getEarliestDatestamp() {
        return getDateFormat().format(new Date(0l));
    }

    @Override
    public GranularityType getGranularity() {
        return GranularityType.YYYY_MM_DD_THH_MM_SS_Z;
    }

    @Override
    public String getBaseUrl() {
        return DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_BASE_URL_ID, "http://localhost:8080/KITDM") + "/oaipmh";
    }

    @Override
    public boolean isPrefixSupported(String prefix) {
        LOGGER.debug("Checking whether prefix {} is supported.", prefix);
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(getAuthorizationContext());
        MetaDataSchema schema = null;
        try {
            LOGGER.debug("Searching metadata format for prefix {}.", prefix);
            schema = mdm.findSingleResult("SELECT s FROM MetaDataSchema s WHERE s.schemaIdentifier=?1", new Object[]{prefix}, MetaDataSchema.class);
            if (schema == null && DC_SCHEMA.getSchemaIdentifier().equals(prefix)) {
                LOGGER.debug("No metadata format found, but the provided prefix stands for the Dublin Core format.");
                schema = DC_SCHEMA;
            } else if (schema == null) {
                LOGGER.warn("No metadata format found for prefix {}.", prefix);
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain metadata format for prefix " + prefix + ".", ex);
        } finally {
            mdm.close();
        }
        return schema != null;
    }

    @Override
    public void identify(OAIPMHBuilder builder) {
        LOGGER.debug("Performing identify().");
        //should already been handled in builder...modification of response possible here
    }

    @Override
    public void listSets(OAIPMHBuilder builder) {
        LOGGER.debug("Performing listSets().");
        //only default set containing all objects currently supported
        builder.addSet("default", "default");
    }

    @Override
    public void listMetadataFormats(OAIPMHBuilder builder) {
        LOGGER.debug("Performing listMetadataFormats().");
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(getAuthorizationContext());
        try {
            List<MetaDataSchema> schemas;
            if (builder.getIdentifier() != null) {
                LOGGER.debug("Getting digital object for identifier {}.", builder.getIdentifier());
                //get all schemas existing for identifier...first check identifier
                DigitalObject result = mdm.findSingleResult("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier=?1", new Object[]{builder.getIdentifier()}, DigitalObject.class);
                if (result == null) {
                    LOGGER.error("No digital object found for identifier {}. Returning OAI-PMH error ID_DOES_NOT_EXIST.", builder.getIdentifier());
                    builder.addError(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, "No object for identifier " + builder.getIdentifier() + " found.");
                }
                //identifier exists...check metadata for this identifier
                LOGGER.debug("Digital object found. Getting metadata formats supported by this object.");
                schemas = mdm.findResultList("SELECT t.schemaReference FROM MetadataIndexingTask t WHERE t.digitalObjectId=?1", new Object[]{builder.getIdentifier()}, MetaDataSchema.class);
                LOGGER.debug("Obtained {} metadata formats.", schemas.size());
            } else {
                //get all schemas
                schemas = mdm.find(MetaDataSchema.class);
            }

            MetaDataSchema dcSchema = (MetaDataSchema) CollectionUtils.find(schemas, (Object o) -> {
                return "dc".equals(((MetaDataSchema) o).getSchemaIdentifier());
            });

            //add DC schema in every case
            if (dcSchema == null) {
                LOGGER.debug("Adding default format Dublin Core to schema list.");
                schemas.add(DC_SCHEMA);
            }
            //add the schemas
            LOGGER.debug("Adding metadata formats to response.");
            schemas.stream().forEach((schema) -> {
                builder.addMetadataFormat(schema.getSchemaIdentifier(), schema.getNamespace(), schema.getMetaDataSchemaUrl());
            });
        } catch (UnauthorizedAccessAttemptException ex) {
            //this error is only relevant if there is an object identifier...otherwise, it should not occur as the metadata schema entities are not secured entities.
            LOGGER.error("Failed to obtain metadata formats for object with identifier " + builder.getIdentifier() + ". Returning OAI-PMH error ID_DOES_NOT_EXIST.");
            builder.addError(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, "No object for identifier " + builder.getIdentifier() + " accessible.");
        } finally {
            mdm.close();
        }
    }

    @Override
    public void listIdentifiers(OAIPMHBuilder builder) {
        LOGGER.debug("Performing listIdentifiers().");
        List<DigitalObject> results = getEntities(builder);
        if (results.isEmpty()) {
            if (!builder.isError()) {
                LOGGER.error("No results obtained. Returning OAI-PMH error NO_RECORDS_MATCH.");
                builder.addError(OAIPMHerrorcodeType.NO_RECORDS_MATCH, null);
            }
            return;
        }

        LOGGER.debug("Adding {} records to result.", results.size());
        results.stream().forEach((result) -> {
            builder.addRecord(result.getDigitalObjectIdentifier(), (result.getUploadDate() != null) ? result.getUploadDate() : new Date(0l), Arrays.asList("default"));
        });
    }

    @Override
    public void getRecord(OAIPMHBuilder builder) {
        //only get record entries for prefix
        LOGGER.debug("Performing getRecord().");
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(getAuthorizationContext());
        try {
            LOGGER.debug("Querying for digital object with identifier {}", builder.getIdentifier());
            DigitalObject result = mdm.findSingleResult("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier=?1", new Object[]{builder.getIdentifier()}, DigitalObject.class);
            if (result != null) {
                LOGGER.debug("Adding single record to result.");
                addRecordEntry(result, builder);
            } else {
                LOGGER.error("No result obtained. Returning OAI-PMH error ID_DOES_NOT_EXIST.");
                builder.addError(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, "No object for identifier " + builder.getIdentifier() + " found.");
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            //log error
            LOGGER.error("Failed to obtain record for identifier " + builder.getIdentifier() + ". Returning OAI-PMH error ID_DOES_NOT_EXIST.");
            builder.addError(OAIPMHerrorcodeType.ID_DOES_NOT_EXIST, "No object for identifier " + builder.getIdentifier() + " accessible.");
        } finally {
            mdm.close();
        }
    }

    @Override
    public void listRecords(OAIPMHBuilder builder) {
        LOGGER.debug("Performing listRecords().");
        List<DigitalObject> results = getEntities(builder);
        if (results.isEmpty()) {
            if (!builder.isError()) {
                LOGGER.error("No results obtained. Returning OAI-PMH error NO_RECORDS_MATCH.");
                builder.addError(OAIPMHerrorcodeType.NO_RECORDS_MATCH, null);
            }
            return;
        }
        LOGGER.debug("Adding {} records to result.", results.size());
        results.stream().forEach((result) -> {
            addRecordEntry(result, builder);
        });
    }

    /**
     * Get the metadata document for the provided object and schema id. The
     * metadata document is loaded from the URL read from the entry fitting the
     * object-schemaId-combination in the MetadataIndexingTask table. If no
     * entry was found, it is checked whether DublinCore metadata is requested
     * by the schemaId 'dc'. If this is the case, DublinCore metadata is
     * generated on the fly.
     *
     * Otherwise, null is returned and must be handled by the caller with an
     * according OAI-PMH error.
     *
     * @param object The object to obtain the metadata document for.
     * @param schemaId The id of the metadata schema.
     *
     * @return The metadata document or null.
     */
    private Document getMetadataDocument(DigitalObject object, String schemaId) {
        LOGGER.debug("Obtaining metadata document for schema {} and object identifier {}", schemaId, object.getDigitalObjectIdentifier());
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(getAuthorizationContext());
        String metadataDocument = null;
        try {
            //try to obtain metadata document for prefix and object
            metadataDocument = mdm.findSingleResult("SELECT t.metadataDocumentUrl FROM MetadataIndexingTask t WHERE t.digitalObjectId=?1 AND t.schemaReference.schemaIdentifier=?2", new Object[]{object.getDigitalObjectIdentifier(), schemaId}, String.class);
            LOGGER.debug("Metadata document should be located at URL {}", metadataDocument);
        } catch (UnauthorizedAccessAttemptException ex) {
            //error
        } finally {
            mdm.close();
        }
        //try to load metadata
        try {
            if (metadataDocument == null && DC_SCHEMA.getSchemaIdentifier().equals(schemaId)) {
                //create DC document on the fly
                LOGGER.info("No metadata document found for object identifier {}. Creating Dublin Core document on the fly.", object.getDigitalObjectIdentifier());
                return DublinCoreHelper.createDublinCoreDocument(object, object.getUploader());
            } else if (metadataDocument != null) {
                LOGGER.debug("Metadata document found. Parsing content.");
                DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
                fac.setNamespaceAware(true);
                DocumentBuilder docBuilder = fac.newDocumentBuilder();
                Document doc = docBuilder.parse(URI.create(metadataDocument).toURL().openStream());
                return doc;
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOGGER.error("Failed to obtain metadata document for schema " + schemaId + " and object identifier " + object.getDigitalObjectIdentifier(), ex);
        }
        //invalid schemaId
        return null;
    }

    /**
     * Add a record entry for the provided digital object to the provided
     * builder. This call tries to obtain the metadata document for the provided
     * object and is this succeeds, an according record is added to the builder.
     *
     * If no metadata document can be obtained, an according OAI-PMH error is
     * added.
     *
     * @param result The digital object to add a record for.
     * @param builder The OAIPMHBuilder.
     */
    private void addRecordEntry(DigitalObject result, OAIPMHBuilder builder) {
        LOGGER.debug("Adding record for object identifier {} to response.", result.getDigitalObjectIdentifier());
        Document doc = getMetadataDocument(result, builder.getMetadataPrefix());
        if (doc != null) {
            LOGGER.debug("Adding record using obtained metadata document.");
            builder.addRecord(result.getDigitalObjectIdentifier(), (result.getUploadDate() != null) ? result.getUploadDate() : new Date(0l), Arrays.asList("default"), doc.getDocumentElement());
        } else {
            LOGGER.error("No metadata document found for rpefix {} and object identifier {}. Returning OAI-PMH error CANNOT_DISSEMINATE_FORMAT.", builder.getMetadataPrefix(), result.getDigitalObjectIdentifier());
            builder.addError(OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT, null);
        }
    }

    /**
     * Get all digital objects according to the arguments set at the provided
     * OAIPMHBuilder.
     *
     * Depending of the values ot 'from', 'until' and 'metadataPrefix' set at
     * the OAIPMHBuilder the result list may contain all or a reduced list of
     * objects. The list might also be empty. In that case a proper OAI-PMH
     * error must be created by the caller.
     *
     * @param builder The OAIPMHBuilder.
     *
     * @return A list of entities which might be empty.
     */
    private List<DigitalObject> getEntities(OAIPMHBuilder builder) {
        List<DigitalObject> results = new ArrayList<>();

        String prefix = builder.getMetadataPrefix();
        LOGGER.debug("Getting entities for metadata prefix {} from repository.", prefix);
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(getAuthorizationContext());
        try {
            LOGGER.debug("Checking request for resumption token");
            String resumptionToken = builder.getResumptionToken();
            int currentCursor = 0;
            int overallCount = 0;
            //check resumption token
            if (resumptionToken != null) {
                String tokenValue = new String(Base64.getDecoder().decode(URLDecoder.decode(resumptionToken, "UTF-8")));
                LOGGER.debug("Found token with value {}", tokenValue);
                String[] elements = tokenValue.split("/");
                if (elements.length != 2) {
                    LOGGER.error("Invalid resumption token. Returning OAI-PMH error BAD_RESUMPTION_TOKEN.");
                    builder.addError(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN, null);
                    return new ArrayList<>();
                }
                try {
                    LOGGER.debug("Parsing token values.");
                    currentCursor = Integer.parseInt(elements[0]);
                    overallCount = Integer.parseInt(elements[1]);
                    LOGGER.debug("Obtained {} as current cursor from token.", currentCursor);
                } catch (NumberFormatException ex) {
                    //log error
                    builder.addError(OAIPMHerrorcodeType.BAD_RESUMPTION_TOKEN, null);
                    return new ArrayList<>();
                }
            } else {
                LOGGER.debug("No resumption token found.");
            }

            if (DC_SCHEMA.getSchemaIdentifier().equals(prefix)) {
                LOGGER.debug("Using Dublin Core schema handling.");
                //handle default schema which is supported by ALL objects, so no complex query is needed.
                Date from = builder.getFromDate();
                Date until = builder.getUntilDate();
                if (from != null && until != null) {
                    LOGGER.debug("Getting all digital objects from {} until {}.", from, until);
                    results = mdm.findResultList("SELECT o FROM DigitalObject o WHERE o.uploadDate>=?1 AND o.uploadDate <= ?2", new Object[]{from, until}, DigitalObject.class, currentCursor, maxElementsPerList);
                    overallCount = (overallCount == 0) ? mdm.findSingleResult("SELECT COUNT(o) FROM DigitalObject o WHERE o.uploadDate>=?1 AND o.uploadDate <= ?2", new Object[]{from, until}, Number.class).intValue() : overallCount;
                } else if (from != null && until == null) {
                    LOGGER.debug("Getting all digital objects from {}.", from);
                    results = mdm.findResultList("SELECT o FROM DigitalObject o WHERE o.uploadDate >= ?1", new Object[]{from}, DigitalObject.class, currentCursor, maxElementsPerList);
                    overallCount = (overallCount == 0) ? mdm.findSingleResult("SELECT COUNT(o) FROM DigitalObject o WHERE o.uploadDate >= ?1", new Object[]{from}, Number.class).intValue() : overallCount;
                } else if (from == null && until != null) {
                    LOGGER.debug("Getting all digital objects until {}.", until);
                    results = mdm.findResultList("SELECT o FROM DigitalObject o WHERE o.uploadDate <= ?1", new Object[]{until}, DigitalObject.class, currentCursor, maxElementsPerList);
                    overallCount = (overallCount == 0) ? mdm.findSingleResult("SELECT COUNT(o) FROM DigitalObject o WHERE o.uploadDate <= ?1", new Object[]{until}, Number.class).intValue() : overallCount;
                } else {
                    LOGGER.debug("Getting all digital object.");
                    results = mdm.findResultList("SELECT o FROM DigitalObject o", DigitalObject.class, currentCursor, maxElementsPerList);
                    overallCount = (overallCount == 0) ? mdm.findSingleResult("SELECT COUNT(o) FROM DigitalObject o", Number.class).intValue() : overallCount;
                }
            } else {

                //@TODO Check where to obtain the metadata document if no MetadataIndexingTask entry is available, e.g. via DataOrganization?
                LOGGER.debug("Using custom schema handling for prefix {}.", prefix);
                //filter by special schema which might not be supported by all objects
                Date from = builder.getFromDate();
                Date until = builder.getUntilDate();
                if (from != null && until != null) {
                    LOGGER.debug("Getting all digital objects from {} until {}.", from, until);
                    results = mdm.findResultList("SELECT o FROM DigitalObject o,MetadataIndexingTask t WHERE o.uploadDate>=?1 AND o.uploadDate <= ?2 AND t.digitalObjectId=o.digitalObjectIdentifier AND t.schemaReference.schemaIdentifier=?3", new Object[]{from, until, prefix}, DigitalObject.class, currentCursor, maxElementsPerList);
                    overallCount = (overallCount == 0) ? mdm.findSingleResult("SELECT o FROM DigitalObject o,MetadataIndexingTask t WHERE o.uploadDate>=?1 AND o.uploadDate <= ?2 AND t.digitalObjectId=o.digitalObjectIdentifier AND t.schemaReference.schemaIdentifier=?3", new Object[]{from, until, prefix}, Number.class).intValue() : overallCount;
                } else if (from != null && until == null) {
                    LOGGER.debug("Getting all digital objects from {}.", from);
                    results = mdm.findResultList("SELECT o FROM DigitalObject o,MetadataIndexingTask t WHERE o.uploadDate>=?1 AND t.digitalObjectId=o.digitalObjectIdentifier AND t.schemaReference.schemaIdentifier=?2", new Object[]{from, prefix}, DigitalObject.class, currentCursor, maxElementsPerList);
                    overallCount = (overallCount == 0) ? mdm.findSingleResult("SELECT COUNT(o) FROM DigitalObject o,MetadataIndexingTask t WHERE o.uploadDate>=?1 AND t.digitalObjectId=o.digitalObjectIdentifier AND t.schemaReference.schemaIdentifier=?2", new Object[]{from, prefix}, Number.class).intValue() : overallCount;
                } else if (from == null && until != null) {
                    LOGGER.debug("Getting all digital objects until {}.", until);
                    results = mdm.findResultList("SELECT o FROM DigitalObject o,MetadataIndexingTask t WHERE o.uploadDate <= ?1 AND t.digitalObjectId=o.digitalObjectIdentifier AND t.schemaReference.schemaIdentifier=?2", new Object[]{until, prefix}, DigitalObject.class, currentCursor, maxElementsPerList);
                    overallCount = (overallCount == 0) ? mdm.findSingleResult("SELECT COUNT(o) FROM DigitalObject o,MetadataIndexingTask t WHERE o.uploadDate <= ?1 AND t.digitalObjectId=o.digitalObjectIdentifier AND t.schemaReference.schemaIdentifier=?2", new Object[]{until, prefix}, Number.class).intValue() : overallCount;
                } else {
                    LOGGER.debug("Getting all digital object.");
                    results = mdm.findResultList("SELECT o FROM DigitalObject o,MetadataIndexingTask t WHERE t.digitalObjectId=o.digitalObjectIdentifier AND t.schemaReference.schemaIdentifier=?1", new Object[]{prefix}, DigitalObject.class, currentCursor, maxElementsPerList);
                    overallCount = (overallCount == 0) ? mdm.findSingleResult("SELECT COUNT(o) FROM DigitalObject o,MetadataIndexingTask t WHERE t.digitalObjectId=o.digitalObjectIdentifier AND t.schemaReference.schemaIdentifier=?1", new Object[]{prefix}, Number.class).intValue() : overallCount;
                }
            }

            LOGGER.debug("Setting next resumption token.");
            if (currentCursor + maxElementsPerList > overallCount) {
                LOGGER.debug("Cursor exceeds element count, no more elements available. Setting resumption token to 'null'.");
                //lsit complete, add no resumptiontoken
                builder.setResumptionToken(null);
            } else {
                ResumptionTokenType token = new ResumptionTokenType();
                //set list size
                token.setCompleteListSize(BigInteger.valueOf(overallCount));
                //set current cursor
                token.setCursor(BigInteger.valueOf(currentCursor + results.size()));
                LOGGER.debug("Setting new resumption token with cursor at position " + token.getCursor());
                //we set no expiration as the token never expires
                String value = token.getCursor().intValue() + "/" + token.getCompleteListSize().intValue();
                LOGGER.debug("Setting resumption token value to {}.", value);
                token.setValue(URLEncoder.encode(Base64.getEncoder().encodeToString(value.getBytes()), "UTF-8"));
                builder.setResumptionToken(token);
            }
        } catch (UnauthorizedAccessAttemptException | UnsupportedEncodingException ex) {
            //error
            LOGGER.error("Failed to get results from repository. Returning empty list.", ex);
        } finally {
            mdm.close();
        }
        return results;
    }

    /**
     * Get the authorization context of the OAI-PMH user.
     */
    private IAuthorizationContext getAuthorizationContext() {
        return new AuthorizationContext(new UserId(Constants.OAI_PMH_USER_ID), new GroupId(Constants.WORLD_GROUP_ID), Role.GUEST);
    }

}
