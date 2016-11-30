/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.mdm.dataworkflow;

import edu.kit.dama.authorization.annotations.SecurableResourceIdField;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowTask;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.apache.commons.io.FileUtils;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.LoggerFactory;

/**
 * The definition of a DataWorkflow Task. A DataWorkflow Task is basically an
 * instance of a DataWorkflowConfiguration that will be executed on within an
 * ExecutionEnvironment provided by an ExectionEnvironmentConfiguration. Each
 * task is associated with one or more digital objects and an object-specific
 * view, which contain the input data for processing. To manage its data, each
 * task has access to several directories:
 *
 * <ul>
 * <li>Input directory - The directory where the data of input digital objects
 * is staged.</li>
 * <li>Output directory - The directory where the task is supposed to write
 * result data. All result data is ingested as new digital object after a
 * successful task execution.</li>
 * <li>Temp directory - The directory where to write temporary files. Data
 * located in the temp directory is removed during the cleanup phase of the task
 * execution.</li>
 * <li>Working directory - The directory where executables and configuration
 * files are placed and from where the task application is executed.</li>
 * </ul>
 *
 * In addition, tasks can be configured in a way where they are based on a
 * predecessor task. In this case the output of the predecessor serves as input
 * of the current task.
 *
 * @author hartmann-v
 */
@Entity
//@XmlNamedObjectGraphs({
//    @XmlNamedObjectGraph(
//            name = "simple",
//            attributeNodes = {
//                @XmlNamedAttributeNode("id"),
//                @XmlNamedAttributeNode("uniqueIdentifier")
//            }),
//    @XmlNamedObjectGraph(
//            name = "default",
//            attributeNodes = {
//                @XmlNamedAttributeNode("id"),
//                @XmlNamedAttributeNode("uniqueIdentifier"),
//                @XmlNamedAttributeNode(value = "configuration", subgraph = "simple"),
//                @XmlNamedAttributeNode(value = "executionEnvironment", subgraph = "simple"),
//                @XmlNamedAttributeNode(value = "predecessor", subgraph = "simple"),
//                @XmlNamedAttributeNode("objectViewMap"),
//                @XmlNamedAttributeNode("objectTransferMap"),
//                @XmlNamedAttributeNode("executionSettings"),
//                @XmlNamedAttributeNode("applicationArguments"),
//                @XmlNamedAttributeNode("status"),
//                @XmlNamedAttributeNode("inputDirectoryUrl"),
//                @XmlNamedAttributeNode("outputDirectoryUrl"),
//                @XmlNamedAttributeNode("workingDirectoryUrl"),
//                @XmlNamedAttributeNode("tempDirectoryUrl"),
//                @XmlNamedAttributeNode("errorMessage"),
//                @XmlNamedAttributeNode("lastUpdate"),
//                @XmlNamedAttributeNode("jobId"),
//                @XmlNamedAttributeNode("executorId"),
//                @XmlNamedAttributeNode("executorGroupId"),
//                @XmlNamedAttributeNode("investigationId")
//            })})
@XmlAccessorType(XmlAccessType.FIELD)
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "DataWorkflowTask.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")
                ,
                @NamedAttributeNode("uniqueIdentifier")})
    ,
    @NamedEntityGraph(
            name = "DataWorkflowTask.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")
                ,
                @NamedAttributeNode("uniqueIdentifier")
                ,
                @NamedAttributeNode("objectViewMap")
                ,
                @NamedAttributeNode("objectTransferMap")
                ,
                @NamedAttributeNode("executionSettings")
                ,
                @NamedAttributeNode("applicationArguments")
                ,
                @NamedAttributeNode("status")
                ,
                @NamedAttributeNode("inputDirectoryUrl")
                ,
                @NamedAttributeNode("outputDirectoryUrl")
                ,
                @NamedAttributeNode("workingDirectoryUrl")
                ,
                @NamedAttributeNode("tempDirectoryUrl")
                ,
                @NamedAttributeNode("errorMessage")
                ,
                @NamedAttributeNode("lastUpdate")
                ,
                @NamedAttributeNode("jobId")
                ,
                @NamedAttributeNode("executorId")
                ,
                @NamedAttributeNode("executorGroupId")
                ,
                @NamedAttributeNode("investigationId")
                ,
                @NamedAttributeNode(value = "configuration", subgraph = "DataWorkflowTaskConfiguration.simple")
                ,
                @NamedAttributeNode(value = "executionEnvironment", subgraph = "ExecutionEnvironmentConfiguration.simple")
                ,
                @NamedAttributeNode(value = "predecessor", subgraph = "DataWorkflowTask.simple")},
            subgraphs = {
                @NamedSubgraph(
                        name = "DataWorkflowTaskConfiguration.simple",
                        attributeNodes = {
                            @NamedAttributeNode("id")
                        }
                )
                ,
                @NamedSubgraph(
                        name = "ExecutionEnvironmentConfiguration.simple",
                        attributeNodes = {
                            @NamedAttributeNode("id")
                        }
                )
                ,
                @NamedSubgraph(
                        name = "DataWorkflowTask.simple",
                        attributeNodes = {
                            @NamedAttributeNode("id")
                            ,
                            @NamedAttributeNode("uniqueIdentifier")}
                )
            })
})
public class DataWorkflowTask implements IDefaultDataWorkflowTask, ISecurableResource, Serializable, FetchGroupTracker {

    @SecurableResourceIdField(domainName = "edu.kit.dama.dataworkflow.DataWorkflowTask")
    @Column(nullable = false, unique = true)
    private String uniqueIdentifier;

    public enum TASK_STATUS {

        //Initial states
        UNKNOWN, SCHEDULED,
        //Preparation phase
        PREPARING, PREPARATION_FAILED, PREPARATION_FINISHED,
        //Staging phase
        STAGING, STAGING_FAILED, STAGING_FINISHED,
        //Processing phase
        PROCESSING, PROCESSING_FAILED, PROCESSING_FINISHED,
        //Ingest phase
        INGEST, INGEST_FAILED, INGEST_FINISHED,
        //Cleanup phase
        CLEANUP, CLEANUP_FAILED, CLEANUP_FINISHED;

        public static boolean isErrorState(TASK_STATUS pStatus) {

            return PREPARATION_FAILED.equals(pStatus)
                    || STAGING_FAILED.equals(pStatus)
                    || PROCESSING_FAILED.equals(pStatus)
                    || INGEST_FAILED.equals(pStatus)
                    || CLEANUP_FAILED.equals(pStatus);
        }

        public static boolean isFinishedState(TASK_STATUS pStatus) {
            return CLEANUP_FINISHED.equals(pStatus) || CLEANUP_FAILED.equals(pStatus);
        }

        public static boolean isPreparationPhase(TASK_STATUS pStatus) {
            return SCHEDULED.equals(pStatus) || PREPARING.equals(pStatus) || PREPARATION_FAILED.equals(pStatus) || PREPARATION_FINISHED.equals(pStatus);
        }

        public static boolean isStagingPhase(TASK_STATUS pStatus) {
            return PREPARATION_FINISHED.equals(pStatus) || STAGING.equals(pStatus) || STAGING_FAILED.equals(pStatus) || STAGING_FINISHED.equals(pStatus);
        }

        public static boolean isProcessingPhase(TASK_STATUS pStatus) {
            return STAGING_FINISHED.equals(pStatus) || PROCESSING.equals(pStatus) || PROCESSING_FAILED.equals(pStatus) || PROCESSING_FINISHED.equals(pStatus);
        }

        public static boolean isIngestPhase(TASK_STATUS pStatus) {
            return PROCESSING_FINISHED.equals(pStatus) || INGEST.equals(pStatus) || INGEST_FAILED.equals(pStatus) || INGEST_FINISHED.equals(pStatus);
        }

        public static boolean isCleanupPhase(TASK_STATUS pStatus) {
            return INGEST_FINISHED.equals(pStatus) || CLEANUP.equals(pStatus) || CLEANUP_FAILED.equals(pStatus) || CLEANUP_FINISHED.equals(pStatus);
        }
    }
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DataWorkflowTask.class);
    /**
     * Identity of the task. Has to be unique.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Configuration which is used for this task.
     */
    @OneToOne
    private DataWorkflowTaskConfiguration configuration;
    /**
     * The execution environment used to submit the task.
     */
    @OneToOne
    private ExecutionEnvironmentConfiguration executionEnvironment;
    /**
     * Task that has to be executed before this task can start. If there is no
     * predecessor task, this value is null.
     */
    @OneToOne
    private DataWorkflowTask predecessor;
    /**
     * The serialized properties object containing the digital objects (keys)
     * and their data organization views (values) used by this task. The
     * associated data is provided before the task starts within the input
     * directory of the task.
     */
    @Column(length = 10240)
    private String objectViewMap = null;
    /**
     * The serialized properties object containing the digital objects (keys)
     * and their transfer ids (values). This map is used during input and output
     * staging to hold the status of the according staging processes.
     */
    @Column(length = 10240)
    private String objectTransferMap = null;

    /**
     * The serialized properties object that contains all custom execution
     * settings as key value pairs. These settings are stored in a file
     * <b>DataWorkflow.properties</b> in the format KEY=VALUE within the working
     * directory of the task execution.
     */
    @Column(length = 10240)
    private String executionSettings = null;

    /**
     * Custom application arguments that will be appended for the execution of
     * this task. Multiple arguments are separated by spaces.
     */
    private String applicationArguments;

    /**
     * Status of this task.
     */
    @Enumerated(value = EnumType.STRING)
    private TASK_STATUS status = TASK_STATUS.UNKNOWN;

    /**
     * Input directory Url of the task. Typically, this Url will point to a
     * local directory.
     */
    private String inputDirectoryUrl;

    /**
     * Output directory Url of the task. Typically, this Url will point to a
     * local directory.
     */
    private String outputDirectoryUrl;

    /**
     * Working directory Url of the task. Typically, this Url will point to a
     * local directory.
     */
    private String workingDirectoryUrl;

    /**
     * Temp directory Url of the task. Typically, this Url will point to a local
     * directory.
     */
    private String tempDirectoryUrl;

    /**
     * The last error message.
     */
    @Column(length = 1024)
    private String errorMessage;

    /**
     * The timestamp when this task was updated the last time.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    /**
     * Reference to job id. This id depends on the underlaying processing
     * infrastructure and might be used for custom monitoring.
     */
    private String jobId;

    /**
     * The id of the user who has requested the task execution.
     */
    private String executorId;

    /**
     * The id of the user group in which name the user has requested the task
     * execution.
     */
    private String executorGroupId;

    /**
     * The id of the linked Investigation where output object of this task will
     * be stored.
     */
    private Long investigationId;

    /**
     * Factory a new DataWorkflow task with a generated unique identifier. This
     * method should be used to create a new DataWorkflow task that is than
     * stored in the database.
     *
     * @return The new task.
     */
    public static DataWorkflowTask factoryNewDataWorkflowTask() {
        return factoryNewDataWorkflowTask(UUID.randomUUID().toString());
    }

    /**
     * Factory a new DataWorkflow task with the provided unique identifier. This
     * method could be used to create a DataWorkflow entity for a known
     * identifier in order to query for a specific entity. For creating a new
     * DataWorkflow task {@link #factoryNewDataWorkflowTask()
     * } should be used as it generates a unique identifier automatically.
     *
     * @param pUniqueIdentifier The unique identifier of the new task.
     *
     * @return The new task.
     */
    public static DataWorkflowTask factoryNewDataWorkflowTask(String pUniqueIdentifier) {
        if (pUniqueIdentifier == null) {
            throw new IllegalArgumentException("Argument 'pUniqueIdentifier' must not be 'null'");
        }
        DataWorkflowTask result = new DataWorkflowTask();
        result.setUniqueIdentifier(pUniqueIdentifier);
        return result;
    }

    /**
     * Default constructor. This constructor can be used for querying for a
     * DataWorkflow task. If required, a unique identifier can be set manually
     * using the according setter. If you want to create and persist a new
     * DataWorkflow task you should use {@link #factoryNewDataWorkflowTask() }
     * instead.
     */
    public DataWorkflowTask() {
    }

    @Override
    public String getObjectViewMap() {
        return objectViewMap;
    }

    /**
     * Set the string representation of the object-view map used as input for
     * this task.
     *
     * @param objectViewMap The objectViewMap.
     */
    public void setObjectViewMap(String objectViewMap) {
        this.objectViewMap = objectViewMap;
    }

    @Override
    public String getObjectTransferMap() {
        return objectTransferMap;
    }

    /**
     * Set the string representation of the object-transfer map used to store
     * the status of all transfers associated with this task.
     *
     * @param objectTransferMap The objectTransferMap.
     */
    public void setObjectTransferMap(String objectTransferMap) {
        this.objectTransferMap = objectTransferMap;
    }

    @Override
    public String getExecutionSettings() {
        return executionSettings;
    }

    /**
     * Set the string representation of the execution settings containing custom
     * properties to parameterize the task execution.
     *
     * @param executionSettings The executionSettings.
     */
    public void setExecutionSettings(String executionSettings) {
        this.executionSettings = executionSettings;
    }

    /**
     * Set the object-view-map properties object as object.
     *
     * @param pProperties The object-view-map properties object.
     *
     * @throws IOException If the serialization failed.
     */
    public void setObjectViewMapAsObject(Properties pProperties) throws IOException {
        String serialized = PropertiesUtil.propertiesToString(pProperties);
        if (serialized != null && serialized.length() > 10 * FileUtils.ONE_KB) {
            throw new IOException("Failed to store object view map from object. Serialized content exceeds max. size of database field (10 KB).");
        }

        this.setObjectViewMap(serialized);
    }

    /**
     * Get the object-view-map properties object as object.
     *
     * @return The object-view-map properties object.
     *
     * @throws IOException If the deserialization failed.
     */
    public Properties getObjectViewMapAsObject() throws IOException {
        return PropertiesUtil.propertiesFromString(getObjectViewMap());
    }

    /**
     * Set the object-transfer-map properties object as object.
     *
     * @param pProperties The object-transfer-map properties object.
     *
     * @throws IOException If the serialization failed.
     */
    public void setObjectTransferMapAsObject(Properties pProperties) throws IOException {
        String serialized = PropertiesUtil.propertiesToString(pProperties);
        if (serialized != null && serialized.length() > 10 * FileUtils.ONE_KB) {
            throw new IOException("Failed to store object transfer map from object. Serialized content exceeds max. size of database field (10 KB).");
        }

        this.setObjectTransferMap(serialized);
    }

    /**
     * Get the object-transfer-map properties object as object.
     *
     * @return The object-transfer-map properties object.
     *
     * @throws IOException If the deserialization failed.
     */
    public Properties getObjectTransferMapAsObject() throws IOException {
        return PropertiesUtil.propertiesFromString(getObjectTransferMap());
    }

    /**
     * Set the execution settings as properties object.
     *
     * @param pProperties The execution settings properties object.
     *
     * @throws IOException If the serialization failed.
     */
    public void setExecutionSettingsAsObject(Properties pProperties) throws IOException {
        String serialized = PropertiesUtil.propertiesToString(pProperties);
        if (serialized != null && serialized.length() > 10 * FileUtils.ONE_KB) {
            throw new IOException("Failed to store execution settings from object. Serialized content exceeds max. size of database field (10 KB).");
        }
        this.setExecutionSettings(serialized);
    }

    /**
     * Get the execution settings as properties object.
     *
     * @return The execution settings properties object.
     *
     * @throws IOException If the deserialization failed.
     */
    public Properties getExecutionSettingsAsObject() throws IOException {
        return PropertiesUtil.propertiesFromString(getExecutionSettings());
    }

    @Override
    public String getApplicationArguments() {
        return applicationArguments;
    }

    /**
     * Set the custom application arguments.
     *
     * @param applicationArguments The application arguments.
     */
    public void setApplicationArguments(String applicationArguments) {
        this.applicationArguments = applicationArguments;
    }

    /**
     * Get the application arguments.
     *
     * @return Application arguments .
     */
    public final String[] getApplicationArgumentsAsArray() {
        String[] result = new String[]{};
        if (applicationArguments != null) {
            //split arguments string by spaces
            result = applicationArguments.split(" ");
        }
        return result;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the task id.
     *
     * @param pId The task id.
     */
    public void setId(Long pId) {
        this.id = pId;
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * Set the unique identifier. The identifier is used to create the securable
     * resource id of this entity.
     *
     * @param uniqueIdentifier The unique identifier.
     */
    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    /**
     * Set the current task status.
     *
     * @param status The status.
     */
    public void setStatus(TASK_STATUS status) {
        this.status = status;
    }

    @Override
    public TASK_STATUS getStatus() {
        return status;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    /**
     * Set the last update date.
     *
     * @param lastUpdate The last update date.
     */
    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public String getJobId() {
        return jobId;
    }

    /**
     * Set the custom job id.
     *
     * @param pJobId The jobID to set.
     */
    public void setJobId(String pJobId) {
        this.jobId = pJobId;
    }

    /**
     * Set the task configuration.
     *
     * @param configuration The configuration.
     */
    public void setConfiguration(DataWorkflowTaskConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public DataWorkflowTaskConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Set the environment in which the task will be executed.
     *
     * @param executionEnvironment The execution environment.
     */
    public void setExecutionEnvironment(ExecutionEnvironmentConfiguration executionEnvironment) {
        this.executionEnvironment = executionEnvironment;
    }

    @Override
    public ExecutionEnvironmentConfiguration getExecutionEnvironment() {
        return executionEnvironment;
    }

    @Override
    public DataWorkflowTask getPredecessor() {
        return predecessor;
    }

    /**
     * Set the predecessor task.
     *
     * @param predecessor The predecessor to set.
     */
    public void setPredecessor(DataWorkflowTask predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * Get the contact UserId for the DataWorkflowTaskConfiguration this task is
     * based on. If no UserId is provided within the configuration, this element
     * might be null.
     *
     * @return The UserId of the contact person.
     */
    public String getContactUserId() {
        String result = null;
        if (configuration != null) {
            result = configuration.getContactUserId();
        }
        return result;
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    /**
     * Set the UserId of the executor.
     *
     * @param executorId The UserId of the executor .
     */
    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }

    @Override
    public String getExecutorGroupId() {
        return executorGroupId;
    }

    /**
     * Set the GroupId of the executor group.
     *
     * @param executorGroupId The GroupId of the executor group.
     */
    public void setExecutorGroupId(String executorGroupId) {
        this.executorGroupId = executorGroupId;
    }

    @Override
    public String getInputDirectoryUrl() {
        return inputDirectoryUrl;
    }

    /**
     * Set the input directory Url.
     *
     * @param inputDirectoryUrl The input Directory Url.
     */
    public void setInputDirectoryUrl(String inputDirectoryUrl) {
        this.inputDirectoryUrl = inputDirectoryUrl;
    }

    @Override
    public String getOutputDirectoryUrl() {
        return outputDirectoryUrl;
    }

    /**
     * Set the output directory Url.
     *
     * @param outputDirectoryUrl The output directory Url.
     */
    public void setOutputDirectoryUrl(String outputDirectoryUrl) {
        this.outputDirectoryUrl = outputDirectoryUrl;
    }

    @Override
    public String getWorkingDirectoryUrl() {
        return workingDirectoryUrl;
    }

    /**
     * Set the working directory Url.
     *
     * @param workingDirectoryUrl The working directory Url.
     */
    public void setWorkingDirectoryUrl(String workingDirectoryUrl) {
        this.workingDirectoryUrl = workingDirectoryUrl;
    }

    @Override
    public String getTempDirectoryUrl() {
        return tempDirectoryUrl;
    }

    /**
     * Set the temp directory Url.
     *
     * @param tempDirectoryUrl The temp directory Url.
     */
    public void setTempDirectoryUrl(String tempDirectoryUrl) {
        this.tempDirectoryUrl = tempDirectoryUrl;
    }

    @Override
    public Long getInvestigationId() {
        return investigationId;
    }

    /**
     * Set the id of the linked investigation.
     *
     * @param investigationId The investigationId.
     */
    public void setInvestigationId(long investigationId) {
        this.investigationId = investigationId;
    }

    @Override
    public SecurableResourceId getSecurableResourceId() {
        return new SecurableResourceId("edu.kit.dama.dataworkflow.DataWorkflowTask", getUniqueIdentifier());
    }
    private transient org.eclipse.persistence.queries.FetchGroup fg;
    private transient Session sn;

    @Override
    public org.eclipse.persistence.queries.FetchGroup _persistence_getFetchGroup() {
        return this.fg;
    }

    @Override
    public void _persistence_setFetchGroup(org.eclipse.persistence.queries.FetchGroup fg) {
        this.fg = fg;
    }

    @Override
    public boolean _persistence_isAttributeFetched(String string) {
        return true;
    }

    @Override
    public void _persistence_resetFetchGroup() {
    }

    @Override
    public boolean _persistence_shouldRefreshFetchGroup() {
        return false;
    }

    @Override
    public void _persistence_setShouldRefreshFetchGroup(boolean bln) {

    }

    @Override
    public Session _persistence_getSession() {

        return sn;
    }

    @Override
    public void _persistence_setSession(Session sn) {
        this.sn = sn;

    }
}
