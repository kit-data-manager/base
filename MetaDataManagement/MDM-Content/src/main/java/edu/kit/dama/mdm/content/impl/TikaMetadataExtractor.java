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

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.util.DataManagerSettings;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class TikaMetadataExtractor extends AbstractStagingProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TikaMetadataExtractor.class);

  public static final int ELASTIC_SEARCH_DEFAULT_PORT = 9300;
  public static final String ELASTIC_SEARCH_DEFAULT_HOST = "localhost";
  public static final String ELASTIC_SEARCH_DEFAULT_CLUSTER = "kitdatamanager";
  public static final String ELASTIC_SEARCH_DEFAULT_INDEX = ELASTIC_SEARCH_DEFAULT_CLUSTER;
  public static final String ELASTIC_SEARCH_DEFAULT_TYPE = "do";

  private Client esClient;

  private String esCluster;
  private String esIndex;

  private String esType;

  public TikaMetadataExtractor(String pUniqueIdentifier) {
    super(pUniqueIdentifier);
  }

  @Override
  public String getName() {
    return "TikaMetadataExtractor";
  }

  @Override
  public String[] getInternalPropertyKeys() {
    //Not needed, yet.
    return new String[]{};
  }

  @Override
  public String getInternalPropertyDescription(String pKey) {
    //Not needed, yet.
    return null;
  }

  @Override
  public String[] getUserPropertyKeys() {
    //Not needed, yet.
    return new String[]{};
  }

  @Override
  public String getUserPropertyDescription(String pKey) {
    //Not needed, yet.
    return null;
  }

  @Override
  public void validateProperties(Properties pProperties) throws PropertyValidationException {
  }

  @Override
  public void configure(Properties pProperties) {
    // set up elasticsearch connection
    try {
      LOGGER.debug("Configuring TikaExtractor");

      DataManagerSettings settings = DataManagerSettings.
              getSingleton();

      int port = settings.getIntProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_PORT_ID,
              ELASTIC_SEARCH_DEFAULT_PORT);

      String host = settings.getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_HOST_ID,
              ELASTIC_SEARCH_DEFAULT_HOST);

      esCluster = settings.getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_CLUSTER_ID,
              ELASTIC_SEARCH_DEFAULT_CLUSTER);

      esType = ELASTIC_SEARCH_DEFAULT_TYPE;

      esIndex = settings.getStringProperty(DataManagerSettings.ELASTIC_SEARCH_DEFAULT_INDEX_ID,
              ELASTIC_SEARCH_DEFAULT_INDEX);

      LOGGER.debug("Port: {}, host: {}, cluster: {}, index: {}", port,
              host, esCluster, esIndex);

      Settings esSettings = ImmutableSettings.settingsBuilder()
              .put("cluster.name", esCluster).build();

      esClient = new TransportClient(esSettings)
              .addTransportAddress(new InetSocketTransportAddress(host, port));

      LOGGER.debug(esClient.toString());
    } catch (Exception ex) {
      LOGGER.error(ex.toString());
    }
  }

  @Override
  public void performPreTransferProcessing(TransferTaskContainer pContainer)
          throws StagingProcessorException {
  }

  @Override
  public void finalizePreTransferProcessing(TransferTaskContainer pContainer)
          throws StagingProcessorException {
  }

  @Override
  public void performPostTransferProcessing(TransferTaskContainer pContainer)
          throws StagingProcessorException {
    LOGGER.debug("Preparing server-side metadata extraction via Tika");

    // get digital object id from transfer
    String doid = pContainer.getTransferInformation().getDigitalObjectId();

    DataOrganizer dor = DataOrganizerFactory.getInstance().
            getDataOrganizer();

    try {
      IMetaDataManager mdm = SecureMetaDataManager.
              factorySecureMetaDataManager(AuthorizationContext.
                      factorySystemContext());

      // perform explicit query to get digital object
      LOGGER.debug("Obtaining digital object for id {}", doid);
      DigitalObject object = mdm.findSingleResult(
              "SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier='"
              + doid + "'", DigitalObject.class);

      // get non-string digital object id
      Long baseid = object.getBaseId();
      LOGGER.debug(String.valueOf(baseid));

      // obtain short (non-string) digital object id from database
      DigitalObjectId dob = new DigitalObjectId(doid);

      // get tree representation of digital object
      NodeId rootNode = dor.getRootNodeId(dob);
      // FIXME: should pass -1 for whole subtree but doesn't work currently
      IFileTree ft = dor.loadSubTree(rootNode, 1000000);

      // get all nodes in tree
      List<IDataOrganizationNode> dataNodes = DataOrganizationUtils.
              flattenNode((ICollectionNode) ft);

      // loop through all nodes and extract metadata of filenodes
      for (IDataOrganizationNode node : dataNodes) {
        if (node instanceof IFileNode) {
          extractMetadata((IFileNode) node, baseid);
        }
      }
    } catch (EntityNotFoundException e) {
      LOGGER.error("Error loading digital object id", e);
    } catch (InvalidNodeIdException e) {
      LOGGER.error("Invalid node", e);
    } catch (UnauthorizedAccessAttemptException e) {
      LOGGER.error("Authorization exception during metadata extraction.", e);
    }

    LOGGER.debug("Metadata extraction finished");
  }

  /**
   * Extracts file metadata via Tika for the specified file node.
   *
   * @param node The file node to examine
   * @param doid Digital object id used to index and link the metadata in
   * elasticsearch
   */
  private void extractMetadata(IFileNode node, long doid) {
    try {
      LOGGER.debug("TikaExtractor started");

      LOGGER.debug("Current logical file name: {}", node.getLogicalFileName().
              asString());

      String lfn = node.getLogicalFileName().asString();
      URI fileUri = new URL(lfn).toURI();

      // FIXME: get file name from URL
           /* String dec = URLDecoder.decode(node.getLogicalFileName().asString(),
       "UTF-8");
       dec = dec.replace("file:", "");
       */
      LOGGER.debug("File URI: {}", fileUri);

      File file = new File(fileUri);

      // get simple file system metadata
      long lastmod = file.lastModified();
      long sz = file.length();
      boolean hidden = file.isHidden();

      InputStream is = new FileInputStream(file);
      Metadata metadata = new Metadata();

      ParseContext context = new ParseContext();
      AutoDetectParser parser = new AutoDetectParser();
      context.set(Parser.class, parser);

      ContentHandler handler = new BodyContentHandler();

      // generic map to hold all metadata and their respective type
      Map<String, Object> metadataHashmap = new HashMap<>();

      metadataHashmap.put("last-modified", lastmod);
      metadataHashmap.put("size", sz);
      metadataHashmap.put("hidden", hidden);
      metadataHashmap.put("name", file.getName());

      LOGGER.debug("File name {}, size {}", file.getName(), sz);

      // actually extract the metadata via Tika
      parser.parse(is, handler, metadata, context);

      // try to parse various types of metadata by casting
      // (integer, double, date). If all casts fail, insert as string
      // Caution: If a new metadata field is inserted into elasticsearch
      // its type (and the type of all following insertions for this field
      // name) is the type used for the first entry. I.e. a if a field
      // named deletionDate is inserted as string all following
      // deletionDates will also be inserted as string and a proper date
      // search or filtering will not be possible.
      // To avoid this, either specify a "schema"/elasticsearch mapping or
      // take care of the correct parsing of new types.
      for (String item : metadata.names()) {
        if (metadata.get(item) != null) {
          String val = metadata.get(item);
          try {
            int i = Integer.parseInt(val);
            metadataHashmap.put(item, i);
          } catch (NumberFormatException ex) {
            try {
              double d = Double.parseDouble(val);
              metadataHashmap.put(item, d);
            } catch (NumberFormatException ex2) {
              try {
                DateFormat df = DateFormat.getDateInstance();
                Date date = df.parse(val);
                metadataHashmap.put(item, date);
              } catch (ParseException ex3) {
                metadataHashmap.put(item, val);
              }
            }
          }
        }

        LOGGER.trace(item + " -- " + metadata.get(item));
      }

      LOGGER.debug("nodeid: " + node.getTransientNodeId());
      LOGGER.debug("doid: " + doid);
      LOGGER.debug("id: " + node.getTransientNodeId().getInTreeId());

      // prepare query, a file is identified by its doid, view name and
      // its node id in the dataorganization tree
      // e.g. localhost:9300/kitdatamanager/do/100_defaultView_1000
      IndexRequestBuilder irb = esClient.prepareIndex(esIndex, esType,
              doid + "_" + node.getViewName() + "_"
              + node.getTransientNodeId().getInTreeId());

      // push metadata to elasticsearch
      IndexRequestBuilder req = irb.setSource(metadataHashmap);
      ListenableActionFuture<IndexResponse> laf = req.execute();
      IndexResponse response = laf.actionGet();
      LOGGER.debug("response: " + response.toString());

      LOGGER.info("Ingested Metadata");
    } catch (URISyntaxException | IOException | SAXException | TikaException ex) {
      LOGGER.error(ex.toString());
    }
  }

  @Override
  public void finalizePostTransferProcessing(TransferTaskContainer pContainer)
          throws StagingProcessorException {
  }

}
