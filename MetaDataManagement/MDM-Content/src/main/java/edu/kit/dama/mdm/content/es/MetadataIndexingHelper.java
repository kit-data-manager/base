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
package edu.kit.dama.mdm.content.es;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.content.MetadataIndexingTask;
import edu.kit.dama.util.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.json.XML;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public final class MetadataIndexingHelper {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MetadataIndexingHelper.class);

  private IMetaDataManager mdm = null;
  private static String hostname = "localhost";
  private static int port = 9300;
  private final static MetadataIndexingHelper singleton = new MetadataIndexingHelper();

  /**
   * Default constructor.
   */
  MetadataIndexingHelper() {
    mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
  }

  /**
   * Set the hostname of the elasticsearch head node.
   *
   * @param pHostname The hostname (default: localhost)
   */
  public void setHostname(String pHostname) {
    MetadataIndexingHelper.hostname = pHostname;
  }

  /**
   * Get the hostname of the elasticsearch head node.
   *
   * @return The hostname (default: localhost)
   */
  public String getHostname() {
    return MetadataIndexingHelper.hostname;
  }

  /**
   * Set the port of the elasticsearch head node.
   *
   * @param pPort The port (default: 9300)
   */
  public void setPort(int pPort) {
    MetadataIndexingHelper.port = pPort;
  }

  /**
   * Get the port of the elasticsearch head node.
   *
   * @return The port (default: 9300)
   */
  public int getPort() {
    return MetadataIndexingHelper.port;
  }

  /**
   * Singleton accessor.
   *
   * @return The MetadataIndexingHelper singleton.
   */
  public static synchronized MetadataIndexingHelper getSingleton() {
    return singleton;
  }

  /**
   * Schedule a new indexing task for the provided digital object id and the
   * metadata document stored in the provided argument pIndexDocument. To
   * authorize metadata access the provided context is used. Furthermore, the
   * information of the context (userId and groupId) is used within the
   * generated indexing entity to define the ownership. The method returns
   * 'true' if the indexing operation was finished successfully.
   *
   * @param pDigitalObjectId The digital object id the metadata document is
   * associated with.
   * @param pIndexDocument The document to index.
   * @param pSchema The schema of the document.
   * @param pContext The autorization context used to authorize the metadata
   * access and to determine the ownership of the generated indexing task.
   *
   * @return TRUE if the indexing task was successfully created.
   */
  public boolean scheduleIndexingTask(DigitalObjectId pDigitalObjectId, AbstractFile pIndexDocument, MetaDataSchema pSchema, IAuthorizationContext pContext) {
    return scheduleIndexingTask(pDigitalObjectId, pIndexDocument, pSchema, pContext.getUserId(), pContext.getGroupId(), pContext);
  }

  /**
   * Schedule a new indexing task for the provided digital object id and the
   * metadata document stored in the provided argument pIndexDocument. In
   * contrast to {@link MetadataIndexingHelper#scheduleIndexingTask(edu.kit.dama.commons.types.DigitalObjectId, edu.kit.lsdf.adalapi.AbstractFile, edu.kit.dama.mdm.base.MetaDataSchema, edu.kit.dama.authorization.entities.UserId, edu.kit.dama.authorization.entities.GroupId, edu.kit.dama.authorization.entities.IAuthorizationContext) } the owning groupId is not obtained from the context but is provided
   * separately.
   *
   * @param pDigitalObjectId The digital object id the metadata document is
   * associated with.
   * @param pIndexDocument The document to index.
   * @param pSchema The schema of the document.
   * @param pGroupId The id of the owning group.
   * @param pContext The autorization context used to authorize the metadata
   * access.
   *
   * @return TRUE if the indexing task was successfully created.
   */
  public boolean scheduleIndexingTask(DigitalObjectId pDigitalObjectId, AbstractFile pIndexDocument, MetaDataSchema pSchema, GroupId pGroupId, IAuthorizationContext pContext) {
    return scheduleIndexingTask(pDigitalObjectId, pIndexDocument, pSchema, pContext.getUserId(), pGroupId, pContext);
  }

  /**
   * Schedule a new indexing task for the provided digital object id and the
   * metadata document stored in the provided argument pIndexDocument. In
   * contrast to {@link MetadataIndexingHelper#scheduleIndexingTask(edu.kit.dama.commons.types.DigitalObjectId, edu.kit.lsdf.adalapi.AbstractFile, edu.kit.dama.mdm.base.MetaDataSchema, edu.kit.dama.authorization.entities.UserId, edu.kit.dama.authorization.entities.GroupId, edu.kit.dama.authorization.entities.IAuthorizationContext) } the owning groupId and userId is not obtained from the context but is
   * provided separately.
   *
   * @param pDigitalObjectId The digital object id the metadata document is
   * associated with.
   * @param pIndexDocument The document to index.
   * @param pSchema The schema of the document.
   * @param pUserId The id of the owning user.
   * @param pGroupId The id of the owning group.
   * @param pContext The autorization context used to authorize the metadata
   * access.
   *
   * @return TRUE if the indexing task was successfully created.
   */
  public boolean scheduleIndexingTask(DigitalObjectId pDigitalObjectId, AbstractFile pIndexDocument, MetaDataSchema pSchema, UserId pUserId, GroupId pGroupId, IAuthorizationContext pContext) {
    LOGGER.debug("Try scheduling indexing task for object '{}'. Checking arguments...", pDigitalObjectId);
    LOGGER.debug("* Checking schema reference.");
    if (pSchema == null) {
      throw new IllegalArgumentException("Metadata schema reference must not be null.");
    }
    LOGGER.debug("* Checking metadata document.");
    if (pIndexDocument == null) {
      throw new IllegalArgumentException("Document scheduled for indexing must not be null.");
    }
    LOGGER.debug("* Checking metadata document for validity.");
    try {
      if (!pIndexDocument.exists() || !pIndexDocument.isLocal() || pIndexDocument.isDirectory()) {
        throw new IllegalArgumentException("Provided document " + pIndexDocument + " is invalid (does not exits, is not locally accessible or is no file).");
      }
    } catch (AdalapiException ex) {
      throw new IllegalArgumentException("Provided document at " + pIndexDocument + " could not be checked.", ex);
    }
    LOGGER.debug("* Creating indexing task entity.");
    MetadataIndexingTask task = new MetadataIndexingTask();
    task.setDigitalObjectId(pDigitalObjectId.getStringRepresentation());
    task.setGroupId((pGroupId != null) ? pGroupId.getStringRepresentation() : Constants.USERS_GROUP_ID);
    task.setOwnerId((pUserId != null) ? pUserId.getStringRepresentation() : null);
    task.setSchemaReference(pSchema);
    task.setMetadataDocumentUrl(pIndexDocument.getUrl().toString());
    boolean result = false;
    try {
      LOGGER.debug("* Persisting indexing task entity.");
      mdm.setAuthorizationContext(pContext);
      MetadataIndexingTask createdTask = mdm.save(task);
      LOGGER.debug("Indexing task with id {} successfully created.", createdTask.getId());
      result = true;
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to persist indexing task entity.", ex);
    }
    return result;
  }

  /**
   * Triggers the indexing process for a number of indexing tasks stored in the
   * database. The following steps are performed:
   *
   * <ul>
   * <li>Obtain pMaxNumberOfTasks tasks which are scheduled for indexing.</li>
   * <li>For each task:
   * <ul><li>Check the metadata document for avail- and accessibility.</li>
   * <li>Transform the XML document to json.</li>
   * <li>Publish the document to the index named pIndex.</li>
   * <li>Update to task entry in the database according to the result.</li>
   * </ul>
   * </ul>
   *
   * @param pCluster The name of cluster the documents will be written to.
   * @param pIndex The name of index the documents will be written to.
   * @param pGroupId The id of the group for which open indexing tasks should be
   * scheduled. If no groupId is provided, the default groupId USERS is used.
   * @param pMaxNumberOfTasks The max. number of tasks which are processed by a
   * single method call.
   * @param pContext The authorization context used to authorize database
   * access.
   *
   * @return TRUE if no error occured. If FALSE is returned it means that at
   * least one indexing tasks has failed even if all others have been finished
   * successfully.
   */
  public boolean performIndexing(String pCluster, String pIndex, GroupId pGroupId, int pMaxNumberOfTasks, IAuthorizationContext pContext) {
    GroupId group = (pGroupId != null) ? pGroupId : new GroupId(Constants.USERS_GROUP_ID);
    LOGGER.debug("Perform indexing of items of group '{}' to cluster '{}' and index '{}'", group, pCluster, pIndex);
    List<MetadataIndexingTask> tasks = getTasksToSchedule(group, pMaxNumberOfTasks, pContext);
    int errors = 0;
    mdm.setAuthorizationContext(pContext);
    for (MetadataIndexingTask task : tasks) {
      LOGGER.debug("Perform indexing task #{}.", task.getId());
      String documentUrl = task.getMetadataDocumentUrl();
      AbstractFile documentFile = null;
      boolean isError = false;
      LOGGER.debug(" * Checking document URL {}.", documentUrl);
      try {
        documentFile = new AbstractFile(new URL(documentUrl));
        if (!documentFile.exists() || !documentFile.isLocal()) {
          LOGGER.error("Metadata document {} of indexing task #{} does either not exist or is not locally accessible. Indexing skipped.", documentUrl, task.getId());
          isError = true;
        }
      } catch (MalformedURLException ex) {
        LOGGER.error("Metadata document " + documentUrl + " of indexing task #" + task.getId() + " is invalid. Indexing skipped.", ex);
        isError = true;
      } catch (AdalapiException ex) {
        LOGGER.error("Failed to check metadata document " + documentUrl + " of indexing task #" + task.getId() + ". Indexing skipped.", ex);
        isError = true;
      }

      if (!isError && documentFile != null) {
        try {
          LOGGER.debug(" * Obtaining JSON data.");
          String jsonString = convertDocumentToJSON(documentFile.getUrl().toURI());
          LOGGER.debug(" * Trying to index JSON data.");
          indexJson(task, jsonString, pCluster, pIndex);
          LOGGER.debug(" * JSON data successfully indexed. Setting finished timestamp for task # {} to NOW.", task.getId());
          task.setFinishTimestamp(System.currentTimeMillis());
          LOGGER.debug(" * Indexing finished and task entity updated successfully.");
        } catch (URISyntaxException ex) {
          LOGGER.debug("Failed to create URI from document URL " + task.getMetadataDocumentUrl() + ".", ex);
          isError = true;
        } catch (IOException ex) {
          LOGGER.debug("Failed to read document from document URL " + task.getMetadataDocumentUrl() + ".", ex);
          isError = true;
        } catch (Throwable t) {
          LOGGER.debug("Unknown error while indexing file " + task.getMetadataDocumentUrl() + ".", t);
          isError = true;
        }
      }

      if (isError) {
        LOGGER.debug("Setting lastError timestamp for task #{} to NOW and increasing the fail count.", task.getId());
        task.setLastErrorTimestamp(System.currentTimeMillis());
        task.setFailCount(task.getFailCount() + 1);
        errors++;
      }

      LOGGER.debug("Trying to save updated task #{}.", task.getId());
      try {
        mdm.save(task);
      } catch (UnauthorizedAccessAttemptException ex) {
        LOGGER.error("Failed to save updated task #" + task.getId() + ".", ex);
        errors++;
      }
    }
    //return TRUE only if no errors occured
    return (errors == 0);
  }

  /**
   * Write the provided JSON document into the index. This operation is
   * associated with the provided MetadataIndexingTask and the document will be
   * written to the index with the provided name. As document type the unique
   * identifier of the schema defined in pTask will be used. The unique
   * identifier of the document itself will be the digital object id (which is
   * part of pTask) extended by an underscore and the identifier of the metadata
   * schema, e.g. 1234-5678-abcd-efgh_dc for the object with the id
   * "1234-5678-abcd-efgh" and the Dublin Core schema with the identifier "dc".
   *
   * @param pTask The index task used to obtain document ids.
   * @param pJson The JSON representation of the document which belongs to task
   * pTask.
   * @param pCluster The cluster into which pJson will be written.
   * @param pIndexName The index into which pJson will be written.
   */
  private void indexJson(MetadataIndexingTask pTask, String pJson, String pCluster, String pIndexName) {
    LOGGER.debug("Intitializing transport client..");
    //Node node = nodeBuilder().clusterName(pCluster).node();
    //Client client = node.client();
    Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", pCluster).build();
    Client client = new TransportClient(settings)
            .addTransportAddress(new InetSocketTransportAddress(getHostname(), getPort()));
    LOGGER.debug("Indexing document...");
    IndexResponse response = client.prepareIndex(pIndexName,
            pTask.getSchemaReference().getSchemaIdentifier(),
            pTask.getDigitalObjectId() + "_" + pTask.getSchemaReference().getSchemaIdentifier()).
            setSource(pJson)
            .execute()
            .actionGet();
    LOGGER.debug("Document with id {} was {}. Current document version: {}", response.getId(), (response.isCreated()) ? "created" : "updated", response.getVersion());
  }

  /**
   * Convert the document behind pDocumentURI into JSON. Currently, only XML
   * documents which are locally accessible (URI protocol must be 'file') are
   * supported. They will be simply flattened using
   * {@link XML#toJSONObject(java.lang.String)}.
   *
   * @param pDocumentURI The URI to the locally accessible metadata XML file.
   *
   * @return The JSON representation of the content of pDocumentURI.
   */
  private String convertDocumentToJSON(URI pDocumentURI) throws IOException {
    File f = new File(pDocumentURI);
    byte[] b = new byte[(int) f.length()];
    try (FileInputStream fin = new FileInputStream(f)) {
      fin.read(b);
    }
    return XML.toJSONObject(new String(b)).toString();
  }

  /**
   * Get a list of MetadataIndexingTask entities which are scheduled. The list
   * size may be smaller or equal pMaxAmount. The criteria for the query is,
   * that the task is not yet finished (finishTimestamp &lt; 0) and its fail
   * count is smaller 3 (failCount &lt; 3). The result is ordered ascending by
   * the schedule date of the tasks.
   *
   * @param pGroupId The is of the group for which the open indexing tasks
   * should be obtained.
   * @param pMaxAmount The max number of returned entities.
   * @param pContext The authorization context.
   *
   * @return A list of indexing tasks or an empty list.
   */
  private List<MetadataIndexingTask> getTasksToSchedule(GroupId pGroupId, int pMaxAmount, IAuthorizationContext pContext) {
    try {
      mdm.setAuthorizationContext(pContext);
      return mdm.findResultList("SELECT t FROM MetadataIndexingTask t WHERE t.finishTimestamp < 0 AND t.failCount < 3 AND t.groupId = '" + pGroupId.getStringRepresentation() + "' ORDER BY t.scheduleTimestamp", 0, pMaxAmount);
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain a list of scheduled indexing tasks.", ex);
    }
    return Collections.emptyList();
  }

  /**
   * Remove all indexing tasks which have finished (more than 12h ago for
   * reasons of monitoring), or which have failed more than 2 times.
   *
   * @param pContext The authorization context used to perform the remove
   * operation.
   *
   * @return The number of removed tasks.
   */
  public int removeFinishedIndexingTasks(IAuthorizationContext pContext) {
    int removed = 0;
    mdm.setAuthorizationContext(pContext);
    LOGGER.debug("Obtaining removable indexing tasks.");
    try {
      List<MetadataIndexingTask> tasksToRemove = mdm.findResultList("SELECT t FROM MetadataIndexingTask t WHERE t.finishTimestamp > 0 AND " + System.currentTimeMillis() + " - t.finishTimestamp > " + DateUtils.MILLIS_PER_HOUR * 12 + " OR t.failCount >= 3");
      LOGGER.debug("Found {} tasks which can be removed.", tasksToRemove.size());
      for (MetadataIndexingTask task : tasksToRemove) {
        try {
          LOGGER.debug("Try to remove task with id {}.", task.getId());
          mdm.remove(task);
          removed++;
        } catch (UnauthorizedAccessAttemptException ex) {
          LOGGER.error("Failed to remove indexing task with id " + task.getId() + ".", ex);
        } catch (EntityNotFoundException ex) {
          LOGGER.error("Failed to remove indexing task  id " + task.getId() + ".", ex);
        }
      }
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to obtain list of removable indexing tasks.", ex);
    }
    return removed;
  }
}
