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
package edu.kit.dama.rest.staging.types;

import com.thoughtworks.xstream.XStream;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.commons.types.ILFN;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.staging.entities.ingest.IngestInformation;
import edu.kit.dama.staging.exceptions.ContainerInitializationException;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.staging.entities.CollectionNodeImpl;
import edu.kit.dama.staging.entities.FileTreeImpl;
import edu.kit.dama.staging.entities.LFNImpl;
import edu.kit.dama.staging.entities.StagingFile;
import edu.kit.dama.rest.staging.client.impl.StagingServiceRESTClient;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.util.Constants;
import edu.kit.tools.url.URLCreator;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode("transferId"),
            @XmlNamedAttributeNode("closed"),
            @XmlNamedAttributeNode("serviceUrl"),
            @XmlNamedAttributeNode("tree"),
            @XmlNamedAttributeNode("destination"),
            @XmlNamedAttributeNode("type")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class TransferTaskContainer {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransferTaskContainer.class);

  /**
   *
   */
  public enum TYPE {

    /**
     *
     */
    INGEST,
    /**
     *
     */
    DOWNLOAD,
    /**
     *
     */
    INTERNAL
  }
  private long transferId = 0l;
  private boolean closed = false;
  private String serviceUrl = null;
  private FileTreeImpl tree = null;
  private URL destination = null;
  private TYPE type = TYPE.INGEST;

  /**
   *
   */
  @XmlTransient
  private transient ITransferInformation transferInfo = null;
  /**
   *
   */
  //private transient Map<URL, ICollectionNode> explodedCollections = null;
  @XmlTransient
  private transient Map<URI, IFileNode> explodedFiles = null;

  /**
   * Default constructor
   */
  public TransferTaskContainer() {
  }

  /**
   * Factory a new ingest container. This method will set the provided
   * attributes and initializes the container. Afterwards, files may be added to
   * the container. If all files were added, the container has to be closed and
   * the transfer processing can start.
   *
   * @param pTransferId The associated transfer id.
   * @param pServiceUrl The service URL of the staging REST service.
   * @param pRestKey The key used to access the REST service.
   * @param pRestSecret The secret used to access the REST service.
   *
   * @return An initialized TransferTaskContainer.
   *
   * @throws ContainerInitializationException If the initialization fails, e.g.
   * if communication with the REST service is not possible.
   */
  public static TransferTaskContainer factoryIngestContainer(Long pTransferId, String pServiceUrl, String pRestKey, String pRestSecret) throws ContainerInitializationException {
    TransferTaskContainer container = new TransferTaskContainer();
    container.setServiceUrl(pServiceUrl);
    container.setTransferId(pTransferId);
    container.setType(TYPE.INGEST);
    container.initialize(pRestKey, pRestSecret);
    return container;
  }

  /**
   * Factory a new ingest container. This method will set the provided
   * attributes and returns the container. This method is intended to be used on
   * the server side to create an ingest container for a file tree obtained
   * based on the uploaded files after staging. The provided file tree must
   * fulfill structural constraints and should be created using createTree(). As
   * the container is obtained before server-sided StagingProcessors are
   * performed, the returned container is not closed in order to be able to add
   * generated files, e.g. extracted metadata. The container must be closed
   * 'manually' before the actual transfer to the archive starts.
   *
   * The returned container will contain the provided file tree and is <b>not
   * closed</b>.
   *
   * @param pIngestInfo The associated ingest information entity.
   * @param pFileTree The file tree to download.
   * @param pServiceUrl The service URL of the staging REST service.
   *
   * @return The TransferTaskContainer.
   */
  public static TransferTaskContainer factoryIngestContainer(IngestInformation pIngestInfo, IFileTree pFileTree, String pServiceUrl) {
    TransferTaskContainer container = new TransferTaskContainer();
    container.setServiceUrl(pServiceUrl);
    container.setTransferId(pIngestInfo.getId());
    container.setTransferInformation(pIngestInfo);
    container.setType(TYPE.INGEST);
    container.setFileTree(pFileTree);
    return container;
  }

  /**
   * Factory a new download container. This method will set the provided
   * attributes and returns the container. This method is intended to be used on
   * the server side to create a download container for a previously selected
   * file tree. The provided file tree must fulfill structural constraints and
   * should be created using createTree().
   *
   * The returned container will contain the provided file tree and is closed.
   * To allow the user to access the container it has to be serialized to a file
   * accessible to the user. On the client side, the file can be loaded using
   * loadFromStream() and processed.
   *
   * @param pTransferId The associated transfer id.
   * @param pFileTree The file tree to download.
   * @param pServiceUrl The service URL of the staging REST service.
   *
   * @return The TransferTaskContainer.
   */
  public static TransferTaskContainer factoryDownloadContainer(Long pTransferId, IFileTree pFileTree, String pServiceUrl) {
    TransferTaskContainer container = new TransferTaskContainer();
    container.setServiceUrl(pServiceUrl);
    container.setTransferId(pTransferId);
    container.setType(TYPE.DOWNLOAD);
    container.setFileTree(pFileTree);
    container.close();
    return container;
  }

  /**
   * Factory an internal container. Internal containers are used e.g. for
   * internal staging processes where no external transfer information entity is
   * involved. As the transfer information entity is needed for internal
   * processes, it will be created inside this method using the provided object
   * id and transfer id.
   *
   * The returned container will contain the provided file tree and is closed.
   *
   * @param pDigitalObjectId The associated object id.
   * @param pTransferId The associated transfer id.
   * @param pFileTree The file tree to download.
   *
   * @return The TransferTaskContainer.
   */
  public static TransferTaskContainer factoryInternalContainer(DigitalObjectId pDigitalObjectId, long pTransferId, IFileTree pFileTree) {
    TransferTaskContainer container = new TransferTaskContainer();
    container.setTransferId(pTransferId);
    container.setTransferInformation(new IngestInformation(pDigitalObjectId));
    container.setType(TYPE.INTERNAL);
    container.setFileTree(pFileTree);
    container.close();
    return container;
  }

  /**
   * Load a TransferTaskContainer from the provided input stream. This stream
   * may point to a file or to a web resource. The container will be loaded, is
   * deserialized using XStream and initialized. For initialization the REST URL
   * defined within the container is used. In case of a download the transfer
   * can start afterwards. For an ingest, new files can be added as long as the
   * container is not closed. If the container is closed the transfer can start
   * or continue.
   *
   * @param pInputStream The input stream where to load the container from.
   * @param pRestKey The REST key used to access the container.
   * @param pRestSecret The REST key used to access the container.
   *
   * @return The transfer task container.
   *
   * @throws ContainerInitializationException If the initialization of the
   * container failed, e.g. because there is no connection with the REST
   * service.
   */
  public static TransferTaskContainer loadFromStream(InputStream pInputStream, String pRestKey, String pRestSecret) throws ContainerInitializationException {
    TransferTaskContainer container = (TransferTaskContainer) new XStream().fromXML(pInputStream);
    container.initialize(pRestKey, pRestSecret);
    return container;
  }

  /**
   * Initialize the container. This will trigger a query to the underlaying REST
   * service using the provided credentials. The transfer information entity
   * associated with the previously set transfer id is obtained and internally
   * set.
   *
   * @param pRestKey The key which is used to access the REST service.
   * @param pRestSecret The secret which is used to access the REST service.
   *
   * @throws ContainerInitializationException If no entity can be obtained for
   * the defined transfer id.
   */
  public final void initialize(String pRestKey, String pRestSecret) throws ContainerInitializationException {
    SimpleRESTContext ctx = new SimpleRESTContext(pRestKey, pRestSecret);
    switch (type) {
      case DOWNLOAD:
        DownloadInformationWrapper downloadResult = new StagingServiceRESTClient(serviceUrl, ctx).getDownloadById(transferId, ctx);
        if (downloadResult == null || downloadResult.getEntities().size() < 1) {
          throw new ContainerInitializationException("Query to download service with id " + transferId + " returned no result.");
        } else {
          transferInfo = downloadResult.getEntities().get(0);
        }
        break;
      case INGEST:
        IngestInformationWrapper ingestResult = new StagingServiceRESTClient(serviceUrl, ctx).getIngestById(transferId, ctx);
        if (ingestResult == null || ingestResult.getEntities().size() < 1) {
          throw new ContainerInitializationException("Query to ingest service with id " + transferId + " returned no result.");
        } else {
          transferInfo = ingestResult.getEntities().get(0);
        }

        //obtain destination from ingest information
        try {
          destination = new URL(transferInfo.getStagingUrl());
        } catch (MalformedURLException ex) {
          throw new ContainerInitializationException("Failed to create TransferTaskContainer for ingest " + getUniqueTransferIdentifier() + ". Invalid staging URL " + transferInfo.getStagingUrl(), ex);
        }
        if (tree == null) {
          tree = (FileTreeImpl) createCompatibleTree(transferInfo);
        }
        break;
      case INTERNAL:
        LOGGER.warn("Containers of type INTERNAL do not have to be initialized.");
        break;
    }

    //if container is closed the next step is the transfer, which needs an exploded file tree
    if (isClosed()) {
      explodeTree();
    }
  }

  /**
   * Explode the file tree of this container. Exploding the file tree will
   * separate all nodes into two maps: explodedCollections and explodedFiles.
   * The first map is used to determine folders that have to be created, whereas
   * the second map contains needed transfers.
   */
  private void explodeTree() {
    explodedFiles = new HashMap<URI, IFileNode>();

    if (tree == null) {
      throw new IllegalStateException("Cannot explode file tree as it is not set, yet.");
    }

    explodeNode(tree.getRootNode());
  }

  /**
   * Explode a single node. If the node is a collection node, all children will
   * be exploded subsequently. Depending on the node type pNode will be added to
   * explodedCollections or to explodedFiles.
   *
   * @param pNode The node to explode.
   */
  private void explodeNode(IDataOrganizationNode pNode) {
    if (pNode instanceof ICollectionNode) {
      /*ILFN lfn = ((IFileNode) pNode).getLogicalFileName();
       if (lfn != null) {
       try {
       explodedCollections.put(new URL(lfn.asString()), (ICollectionNode) pNode);
       } catch (MalformedURLException ex) {
       LOGGER.warn("Failed to explode node {}. LFN URL {} is invalid.", new Object[]{pNode, lfn});
       }
       }*/
      for (IDataOrganizationNode child : ((ICollectionNode) pNode).getChildren()) {
        explodeNode(child);
      }
    } else if (pNode instanceof IFileNode) {
      ILFN lfn = ((IFileNode) pNode).getLogicalFileName();
      if (lfn != null) {
        try {
          explodedFiles.put(new URL(lfn.asString()).toURI(), (IFileNode) pNode);
        } catch (MalformedURLException ex) {
          LOGGER.warn("Failed to explode node {}. LFN URL {} is invalid.", new Object[]{pNode, lfn});
        } catch (URISyntaxException ex) {
          LOGGER.warn("Failed to explode node {}. LFN URI syntax {} is invalid.", new Object[]{pNode, lfn});
        }
      }
    }
  }

  /**
   * Set the transfer id which is used to obtain the transfer information
   * entity.
   *
   * @param transferId The transfer id.
   */
  public final void setTransferId(long transferId) {
    this.transferId = transferId;
  }

  /**
   * Get the transfer id which is used to obtain the transfer information
   * entity.
   *
   * @return The transfer id.
   */
  public final long getTransferId() {
    return transferId;
  }

  /**
   * Returns the destination URL.
   *
   * @return The transfer destination.
   */
  public final URL getDestination() {
    LOGGER.warn("No destination set. The destination must be set manually depending on the base path of the internal file tree.");
    return destination;
  }

  /**
   * Get the data url.
   *
   * @return The data url.
   */
  public final URL getDataUrl() {
    return URLCreator.appendToURL(getDestination(), Constants.STAGING_DATA_FOLDER_NAME);
  }

  /**
   * Get the settings url.
   *
   * @return The settings url.
   */
  public final URL getSettingsUrl() {
    return URLCreator.appendToURL(getDestination(), Constants.STAGING_SETTINGS_FOLDER_NAME);
  }

  /**
   * Get the 'generated' folder url.
   *
   * @return The 'generated' folder url.
   */
  public final URL getGeneratedUrl() {
    return URLCreator.appendToURL(getDestination(), Constants.STAGING_GENERATED_FOLDER_NAME);
  }

  /**
   * Set the destination URL. In case of an ingest triggered by the user, this
   * URL is obtained via the REST interface while initializing this container.
   * In case of download the destination is located on the user machine and it
   * is set manually. For internal workflows, e.g. for staging files using the
   * InProcStagingClient, the destination URL might be obtained and set
   * programatically.
   *
   * @param pDestination The transfer destination.
   */
  public final void setDestination(URL pDestination) {
    destination = pDestination;
  }

  /**
   * Close the container. If a container is closed, no new files can be added.
   * Download containers accessed on the client side are automatically while
   * they are factored. Ingest container should be closed before the transfer
   * starts after all files (e.g. generated preprocessing files) are added.
   * After closing the container, explodeTree() will be called. Afterwards, the
   * transfers can take place.
   */
  public final void close() {
    //close the container
    closed = true;
    //explode the file tree
    explodeTree();
  }

  /**
   * Returns whether the container is closed or not.
   *
   * @return TRUE = container is closed.
   */
  public final boolean isClosed() {
    return closed;
  }

  /**
   * Set the file tree. This method is used internally in case of a download.
   * Keep in mind, that the file tree set for transfer task containers has to
   * offer a defined structure. It is recommended to perform file tree creation
   * and modifications only by the static methods provided by this class.
   *
   * @param pTree The tree to set.
   */
  public final void setFileTree(IFileTree pTree) {
    if (pTree == null) {
      throw new IllegalArgumentException("Argument pTree must not be null");
    }

    if (!(pTree instanceof FileTreeImpl)) {
      throw new IllegalArgumentException("Argument pTree must be an instance of edu.​kit.​dama.​staging.​entities.FileTreeImpl");
    }

    IDataOrganizationNode dataNode = (IDataOrganizationNode) CollectionUtils.find(pTree.getRootNode().getChildren(), new Predicate() {
      @Override
      public boolean evaluate(Object o) {
        return Constants.STAGING_DATA_FOLDER_NAME.equals(((IDataOrganizationNode) o).getName());
      }
    });

    if (dataNode == null) {
      throw new IllegalArgumentException("Argument pTree has an invalid structure. No '" + Constants.STAGING_DATA_FOLDER_NAME + "' node found.");
    }
    IDataOrganizationNode generatedNode = (IDataOrganizationNode) CollectionUtils.find(pTree.getRootNode().getChildren(), new Predicate() {
      @Override
      public boolean evaluate(Object o) {
        return Constants.STAGING_GENERATED_FOLDER_NAME.equals(((IDataOrganizationNode) o).getName());
      }
    });
    if (generatedNode == null) {
      throw new IllegalArgumentException("Argument pTree has an invalid structure. No '" + Constants.STAGING_GENERATED_FOLDER_NAME + "' node found.");
    }
    IDataOrganizationNode settingsNode = (IDataOrganizationNode) CollectionUtils.find(pTree.getRootNode().getChildren(), new Predicate() {
      @Override
      public boolean evaluate(Object o) {
        return Constants.STAGING_SETTINGS_FOLDER_NAME.equals(((IDataOrganizationNode) o).getName());
      }
    });
    if (settingsNode == null) {
      throw new IllegalArgumentException("Argument pTree has an invalid structure. No '" + Constants.STAGING_SETTINGS_FOLDER_NAME + "' node found.");
    }

    tree = (FileTreeImpl) pTree;
  }

  /**
   * Returns the currently set file tree.
   *
   * @return The file tree.
   */
  public final IFileTree getFileTree() {
    return tree;
  }

  /**
   * Returns the unique transfer identifier. This identifier is a combination of
   * the digital object id the transfer belongs to and the transfer id. This
   * should guarantee uniqueness even if different REST endpoints are used.
   *
   * @return The unique transfer identifier.
   */
  public final String getUniqueTransferIdentifier() {
    return getTransferInformation().getDigitalObjectId() + "-" + getTransferId();
  }

  /**
   * Get the associated transfer information. The transfer information should be
   * available as soon as initialize() was called. Otherwise, an
   * IllegalStateException is thrown.
   *
   * @return The transfer information associated with this container.
   */
  public final ITransferInformation getTransferInformation() {
    if (transferInfo == null) {
      throw new IllegalStateException("No transfer information obtained, yet. initialize() has to be called in order to obtain the transfer information entity.");
    }
    return transferInfo;
  }

  /**
   * Set the transfer information programatically. This is only need for the
   * InProcStagingClient, in all other cases the transfer information should be
   * loaded during the container intialization. Therefore no public setter is
   * available.
   *
   * @param pTransferInformation The transfer information to set.
   */
  public final void setTransferInformation(ITransferInformation pTransferInformation) {
    transferInfo = pTransferInformation;
  }

  /**
   * Set the REST service URL used to query for the transfer information.
   *
   * @param serviceUrl The service URL.
   */
  public final void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  /**
   * Get the REST service URL used to query for the transfer information.
   *
   * @return The service URL.
   */
  public final String getServiceUrl() {
    return serviceUrl;
  }

  /**
   * Set the container type (INGEST or DOWNLOAD).
   *
   * @param type The container type.
   */
  public final void setType(TYPE type) {
    this.type = type;
  }

  /**
   * Get the container type (INGEST or DOWNLOAD).
   *
   * @return The container type.
   */
  public final TYPE getType() {
    return type;
  }

  /**
   * This method creates a file tree compatible with the KIT Data Manager
   * staging process. The file tree contains three collection nodes for data,
   * for generated files and for settings. Therefore, is can be easily
   * transferred to the server side, as the KIT Data Manager staging service
   * takes care of creating this structure there.
   *
   * @param pTransferInfo The transfer information.
   *
   * @return The file tree.
   */
  public final static IFileTree createCompatibleTree(ITransferInformation pTransferInfo) {
    FileTreeImpl theTree = new FileTreeImpl();
    theTree.setDigitalObjectId(new DigitalObjectId(pTransferInfo.getDigitalObjectId()));
    ICollectionNode dataChild = new CollectionNodeImpl(new StagingFile(new AbstractFile(pTransferInfo.getDataFolderURL())));
    dataChild.setName(Constants.STAGING_DATA_FOLDER_NAME);
    theTree.getRootNode().addChild(dataChild);
    ICollectionNode generatedChild = new CollectionNodeImpl(new StagingFile(new AbstractFile(pTransferInfo.getGeneratedFolderURL())));
    generatedChild.setName(Constants.STAGING_GENERATED_FOLDER_NAME);
    theTree.getRootNode().addChild(generatedChild);
    ICollectionNode settingsChild = new CollectionNodeImpl(new StagingFile(new AbstractFile(pTransferInfo.getSettingsFolderURL())));
    settingsChild.setName(Constants.STAGING_SETTINGS_FOLDER_NAME);
    theTree.getRootNode().addChild(settingsChild);
    return theTree;
  }

  /**
   * This method creates a file tree compatible with the KIT Data Manager
   * staging process. The file tree contains three collection nodes for data,
   * for generated files and for settings. Therefore, is can be easily
   * transferred to the server side, as the KIT Data Manager staging service
   * takes care of creating this structure there. Furthermore, this method
   * allows to provide the data branch of the tree. This is useful for
   * server-side staging where the data tree is available and has only to be
   * added to the staging tree.
   *
   * @param pTransferInfo The transfer information the tree is associated with.
   * @param pDataNode The data node/branch of the tree.
   *
   * @return The file tree.
   */
  public final static IFileTree createCompatibleTree(ITransferInformation pTransferInfo, ICollectionNode pDataNode) {
    FileTreeImpl theTree = new FileTreeImpl();
    theTree.setDigitalObjectId(new DigitalObjectId(pTransferInfo.getDigitalObjectId()));
    ICollectionNode dataChild = new CollectionNodeImpl(new StagingFile(new AbstractFile(pTransferInfo.getDataFolderURL())));
    dataChild.setName(Constants.STAGING_DATA_FOLDER_NAME);
    theTree.getRootNode().addChild(dataChild);
    //add all data nodes to the data branch
    for (IDataOrganizationNode childNode : pDataNode.getChildren()) {
      dataChild.addChild(DataOrganizationUtils.copyNode(childNode, false));
    }

    ICollectionNode generatedChild = new CollectionNodeImpl(new StagingFile(new AbstractFile(pTransferInfo.getGeneratedFolderURL())));
    generatedChild.setName(Constants.STAGING_GENERATED_FOLDER_NAME);
    theTree.getRootNode().addChild(generatedChild);
    ICollectionNode settingsChild = new CollectionNodeImpl(new StagingFile(new AbstractFile(pTransferInfo.getSettingsFolderURL())));
    settingsChild.setName(Constants.STAGING_SETTINGS_FOLDER_NAME);
    theTree.getRootNode().addChild(settingsChild);
    return theTree;
  }

  /**
   * Add the content of pDataFile to this container. If pDataFile is a file, the
   * file is added to the data node of this container's file tree. If pDataFile
   * is a folder, all contained files and directories are added to the data node
   * of this container's fileTree. This method is only supported for containers
   * of type INGEST and as long as the container is not closed.
   *
   * @param pDataFile The data file/folder to add.
   *
   * @throws edu.kit.lsdf.adalapi.exception.AdalapiException If pFile cannot be
   * accessed.
   * @throws java.net.MalformedURLException If extracting the URL information
   * from pFile fails. This should never happen.
   */
  public final void addDataFile(File pDataFile) throws AdalapiException, MalformedURLException {
    if (!TYPE.INGEST.equals(type)) {
      LOGGER.warn("Adding files should only be used for ingest containers");
    }

    if (isClosed()) {
      throw new IllegalStateException("Container is closed, adding new files is not available.");
    }
    addDataFile(tree, transferInfo, pDataFile);
  }

  /**
   * Add the content of pGeneratedFile to this container. If pGeneratedFile is a
   * file, the file is added to the 'generated' node of this container's file
   * tree. If pGeneratedFile is a folder, all contained files and directories
   * are added to the 'generated' node of this container's fileTree. This method
   * is only supported for containers of type INGEST and as long as the
   * container is not closed.
   *
   * @param pGeneratedFile The generated file/folder to add.
   *
   * @throws edu.kit.lsdf.adalapi.exception.AdalapiException If pGeneratedFile
   * cannot be accessed.
   * @throws java.net.MalformedURLException If extracting the URL information
   * from pGeneratedFile fails. This should never happen.
   */
  public final void addGeneratedFile(File pGeneratedFile) throws AdalapiException, MalformedURLException {
    if (!TYPE.INGEST.equals(type)) {
      LOGGER.warn("Adding files should only be used for ingest containers");
    }

    if (isClosed()) {
      throw new IllegalStateException("Container is closed, adding new files is not available.");
    }
    addGeneratedFile(tree, transferInfo, pGeneratedFile);
  }

  /**
   * Add the content of pSettingsFile to this container. If pSettingsFile is a
   * file, the file is added to the 'settings' node of this container's file
   * tree. If pSettingsFile is a folder, all contained files and directories are
   * added to the 'settings' node of this container's fileTree. This method is
   * only supported for containers of type INGEST and as long as the container
   * is not closed.
   *
   * @param pSettingsFile The settings file/folder to add.
   *
   * @throws edu.kit.lsdf.adalapi.exception.AdalapiException If pSettingsFile
   * cannot be accessed.
   * @throws java.net.MalformedURLException If extracting the URL information
   * from pSettingsFile fails. This should never happen.
   */
  public final void addSettingsFile(File pSettingsFile) throws AdalapiException, MalformedURLException {
    if (!TYPE.INGEST.equals(type)) {
      LOGGER.warn("Adding files should only be used for ingest containers");
    }

    if (isClosed()) {
      throw new IllegalStateException("Container is closed, adding new files is not available.");
    }
    addSettingsFile(tree, transferInfo, pSettingsFile);
  }

  /**
   * Add a data file/directory to the provided tree. If pDataFile is a file, the
   * file is added to the 'data' node of the provided file tree. If pDataFile is
   * a folder, all contained files and directories are added to the 'data' node
   * of the provided file tree. The tree must be compatible to the required tree
   * structure. Therefore, it should be created by createCompatibleTree()
   * before.
   *
   * @param pTree The tree to which the file/directory is added.
   * @param pTransferInfo The transfer information associated with the tree.
   * @param pDataFile The file/directory to add.
   *
   * @throws edu.kit.lsdf.adalapi.exception.AdalapiException If pDataFile cannot
   * be accessed.
   * @throws java.net.MalformedURLException If extracting the URL information
   * from pDataFile fails. This should never happen.
   */
  public final static void addDataFile(IFileTree pTree, ITransferInformation pTransferInfo, File pDataFile) throws AdalapiException, MalformedURLException {
    ICollectionNode dataNode = (ICollectionNode) CollectionUtils.find(pTree.getRootNode().getChildren(), new Predicate() {
      @Override
      public boolean evaluate(Object o) {
        return Constants.STAGING_DATA_FOLDER_NAME.equals(((IDataOrganizationNode) o).getName());
      }
    });

    //DataOrganizationUtils.printTree(pTree.getRootNode(), true);
    if (dataNode == null) {
      throw new IllegalArgumentException("Provided tree has an invalid structure. No '" + Constants.STAGING_DATA_FOLDER_NAME + "' node found.");
    }

    AbstractFile dataFile = new AbstractFile(pDataFile);
    IFileTree newTree = DataOrganizationUtils.createTreeFromFile(pTransferInfo.getDigitalObjectId(), dataFile, dataFile.getUrl(), false);
    DataOrganizationUtils.merge(dataNode, new LinkedList<ICollectionNode>(), newTree.getRootNode().getChildren().toArray(new IDataOrganizationNode[newTree.getRootNode().getChildren().size()]));
  }

  /**
   * Add a generated file/directory to the provided tree. If pGeneratedFile is a
   * file, the file is added to the 'generated' node of the provided file tree.
   * If pGeneratedFile is a folder, all contained files and directories are
   * added to the 'generated' node of the provided file tree. The tree must be
   * compatible to the required tree structure. Therefore, it should be created
   * by createCompatibleTree() before.
   *
   * @param pTree The tree to which the file/directory is added.
   * @param pTransferInfo The transfer information associated with the tree.
   * @param pGeneratedFile The file/directory to add.
   *
   * @throws edu.kit.lsdf.adalapi.exception.AdalapiException If pGeneratedFile
   * cannot be accessed.
   * @throws java.net.MalformedURLException If extracting the URL information
   * from pGeneratedFile fails. This should never happen.
   */
  public final static void addGeneratedFile(IFileTree pTree, ITransferInformation pTransferInfo, File pGeneratedFile) throws AdalapiException, MalformedURLException {
    ICollectionNode generatedNode = (ICollectionNode) CollectionUtils.find(pTree.getRootNode().getChildren(), new Predicate() {
      @Override
      public boolean evaluate(Object o) {
        return Constants.STAGING_GENERATED_FOLDER_NAME.equals(((IDataOrganizationNode) o).getName());
      }
    });

    if (generatedNode == null) {
      throw new IllegalArgumentException("Provided tree has an invalid structure. No '" + Constants.STAGING_GENERATED_FOLDER_NAME + "' node found.");
    }

    IFileTree newTree = DataOrganizationUtils.createTreeFromFile(pTransferInfo.getDigitalObjectId(), new AbstractFile(pGeneratedFile), pTransferInfo.getGeneratedFolderURL(), false);
    DataOrganizationUtils.merge(generatedNode, new LinkedList<ICollectionNode>(), newTree.getRootNode().getChildren().toArray(new IDataOrganizationNode[newTree.getRootNode().getChildren().size()]));
  }

  /**
   * Add a settings file/directory to the provided tree. If pSettingsFile is a
   * file, the file is added to the 'settings' node of the provided file tree.
   * If pSettingsFile is a folder, all contained files and directories are added
   * to the 'settings' node of the provided file tree. The tree must be
   * compatible to the required tree structure. Therefore, it should be created
   * by createCompatibleTree() before.
   *
   * @param pTree The tree to which the file/directory is added.
   * @param pTransferInfo The transfer information associated with the tree.
   * @param pSettingsFile The file/directory to add.
   *
   * @throws edu.kit.lsdf.adalapi.exception.AdalapiException If pSettingsFile
   * cannot be accessed.
   * @throws java.net.MalformedURLException If extracting the URL information
   * from pSettingsFile fails. This should never happen.
   */
  public final static void addSettingsFile(IFileTree pTree, ITransferInformation pTransferInfo, File pSettingsFile) throws AdalapiException, MalformedURLException {
    ICollectionNode settingsNode = (ICollectionNode) CollectionUtils.find(pTree.getRootNode().getChildren(), new Predicate() {
      @Override
      public boolean evaluate(Object o) {
        return Constants.STAGING_SETTINGS_FOLDER_NAME.equals(((IDataOrganizationNode) o).getName());
      }
    });

    if (settingsNode == null) {
      throw new IllegalArgumentException("Provided tree has an invalid structure. No '" + Constants.STAGING_SETTINGS_FOLDER_NAME + "' node found.");
    }

    IFileTree newTree = DataOrganizationUtils.createTreeFromFile(pTransferInfo.getDigitalObjectId(), new AbstractFile(pSettingsFile), pTransferInfo.getSettingsFolderURL(), false);
    DataOrganizationUtils.merge(settingsNode, new LinkedList<ICollectionNode>(), newTree.getRootNode().getChildren().toArray(new IDataOrganizationNode[newTree.getRootNode().getChildren().size()]));
  }

  /**
   * Mark the file node associated with pUrl as TRANSFERRED. If there is no node
   * for pUrl found inside explodedFiles and IllegalArgumentException is thrown.
   *
   * @param pSourceUrl The URL of the file node as it was before the transfer.
   * @param pDestinationUrl The URL of the file node as it is after the
   * transfer.
   */
  public final void markFileTransferred(URL pSourceUrl, URL pDestinationUrl) {
    IFileNode node = null;
    try {
      node = explodedFiles.get(pSourceUrl.toURI());
    } catch (URISyntaxException ex) {
      throw new IllegalArgumentException("Failed to get URI from URL " + pSourceUrl, ex);
    }

    if (node == null) {
      throw new IllegalArgumentException("No file node found for URL " + pSourceUrl);
    }
    //mark node as transferred
    DataOrganizationUtils.markFileTransferred(node);
    //set destination as new LFN
    node.setLogicalFileName(new LFNImpl(pDestinationUrl));
  }

//  public static void main(String[] args) throws Exception {
//    String key = "lYdegmLxox7faYpk";
//    String secret = "ACfbYGCdAj3mLziU";
//    long id = 33703l;
//    String url = "https://dama.lsdf.kit.edu/KITDM/rest/staging/StagingService/";
//    SimpleRESTContext ctx = new SimpleRESTContext(key, secret);
//    ITransferInformation info = new StagingServiceRESTClient(url, ctx).getDownloadById(id, ctx).getEntities().get(0);
//
//    IFileTree tree = TransferTaskContainer.createCompatibleTree(info);
//    TransferTaskContainer.addDataFile(tree, info, new File("d:/tmp/Bla/"));
//
//    TransferTaskContainer container = TransferTaskContainer.factoryDownloadContainer(id, tree, url);
//    container.initialize(key, secret);
//
//    /* XStream x = new XStream();
//     String xml = x.toXML(container);
//     System.out.println(xml);*/
//    /*container = (TransferTaskContainer) x.fromXML(xml);
//     DataOrganizationUtils.printTree(container.getFileTree().getRootNode(), true);*/
//    try {
//      Marshaller marshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(new Class[]{
//      LFNImpl.class,
//      URL.class,
//      TransferTaskContainer.class,
//      DataOrganizationNodeImpl.class,
//      FileNodeImpl.class,
//      AttributeImpl.class
//    }).createMarshaller();
//      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//
//      //marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, "default");
//      ByteArrayOutputStream bout = new ByteArrayOutputStream();
//      marshaller.marshal(container, bout);
//      System.out.println(bout.toString());
//
//      System.out.println("-----------------");
//
//      if(true) return;
//      Unmarshaller unmarshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(new Class[]{
//      LFNImpl.class,
//      URL.class,
//      TransferTaskContainer.class,
//      DataOrganizationNodeImpl.class,
//      FileNodeImpl.class,
//      AttributeImpl.class
//    }).createUnmarshaller();
//      ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
//      TransferTaskContainer t = (TransferTaskContainer) unmarshaller.unmarshal(bin);
//      System.out.println("OK");
//      System.out.println("-----------------");
//
//      bout = new ByteArrayOutputStream();
//      marshaller.marshal(container, bout);
//      System.out.println(bout.toString());
//
//    } catch (JAXBException e) {
//      LOGGER.error("Failed to marshal result to output stream.", e);
//    }
//
//  }
}
