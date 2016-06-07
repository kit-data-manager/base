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
package edu.kit.dama.staging.entities.ingest;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.entities.interfaces.IDefaultIngestInformation;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.dama.util.Constants;
import edu.kit.dama.util.DataManagerSettings;
import edu.kit.tools.url.URLCreator;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 * Ingest information object representing a single data ingest. The status of
 * the ingest is represented by the 'status' property which holds a status code
 * based on valid elements of INGEST_STATUS. A new ingest information should be
 * created by the constructor taking a digital object id as argument, as there
 * is no setter for changing the digital object id after instantiation. The
 * empty constructor is only available for reasons of bean-compliance.
 *
 * Furthermore this class is implemented following the JPA specification, using
 * according annotations and named queries, that are expected by the persistence
 * implementation for this entity.
 *
 * @author jejkal
 */
//<editor-fold defaultstate="collapsed" desc="Annotations for JPA">
@Entity
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("id")
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("id"),
                @XmlNamedAttributeNode("status"),
                @XmlNamedAttributeNode("lastUpdate"),
                @XmlNamedAttributeNode("expiresAt"),
                @XmlNamedAttributeNode("transferId"),
                @XmlNamedAttributeNode("digitalObjectUuid"),
                @XmlNamedAttributeNode("ownerUuid"),
                @XmlNamedAttributeNode("clientAccessUrl"),
                @XmlNamedAttributeNode("stagingUrl"),
                @XmlNamedAttributeNode("storageUrl"),
                @XmlNamedAttributeNode("accessPointId"),
                @XmlNamedAttributeNode("errorMessage"),
                @XmlNamedAttributeNode(value = "stagingProcessors", subgraph = "simple")
            }),
    @XmlNamedObjectGraph(
            name = "complete",
            attributeNodes = {
                @XmlNamedAttributeNode("id"),
                @XmlNamedAttributeNode("status"),
                @XmlNamedAttributeNode("lastUpdate"),
                @XmlNamedAttributeNode("expiresAt"),
                @XmlNamedAttributeNode("transferId"),
                @XmlNamedAttributeNode("digitalObjectUuid"),
                @XmlNamedAttributeNode("ownerUuid"),
                @XmlNamedAttributeNode("clientAccessUrl"),
                @XmlNamedAttributeNode("stagingUrl"),
                @XmlNamedAttributeNode("storageUrl"),
                @XmlNamedAttributeNode("accessPointId"),
                @XmlNamedAttributeNode("errorMessage"),
                @XmlNamedAttributeNode(value = "stagingProcessors")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "IngestInformation")
//</editor-fold>
public class IngestInformation implements IDefaultIngestInformation, ITransferInformation<INGEST_STATUS>, Serializable {

    //default lifetime for unfinished ingest is one week
    @Transient
    public static final long DEFAULT_LIFETIME = DataManagerSettings.getSingleton().getLongProperty(DataManagerSettings.STAGING_MAX_INGEST_LIFETIME, 60 * 60 * 24 * 7) * 1000;
    /**
     * The status of this ingest
     */
    private int status = INGEST_STATUS.UNKNOWN.getId();
    /**
     * The id of this ingest (unique/auto increment for RDBMS)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    /**
     * Timestamp when this ingest entry was updated the last time
     */
    private long lastUpdate = -1l;
    /**
     * Timestamp when this ingest entry is removed automatically
     */
    private long expiresAt = -1l;
    /**
     * The UUID of the ingest. This ID must be unique for each ingest
     */
    private String transferId = null;
    /**
     * The UUID of the digital object this ingest is associated with
     */
    @Column(nullable = false, updatable = false)
    private String digitalObjectUuid = null;
    /**
     * The UUID of the owner/initiator of this ingest operation
     */
    private String ownerUuid = null;
    /**
     * The UUID of the group of this ingest operation
     */
    private String groupUuid = null;
    /**
     * The URL which will be accessible by external clients. This URL may point
     * to a download of a prepared client or directly to some physical data
     * location accessible via an appropriate protocol.
     */
    private String clientAccessUrl = null;
    /**
     * The remote URL where the all ingest related data will be placed during
     * pre-ingest. Under this URL the staging service is responsible to create a
     * folder named 'data', where all user data will be uploaded to.
     * Furthermore, there will be a folder 'settings', were internal settings
     * can be stored. This folder is also used to store additional files
     * generated before/after the transfer, e.g. post-processing instructions.
     *
     * The final ingest will be performed by a staging service to
     * <i>storageUrl</i>.
     */
    private String stagingUrl = null;
    /**
     * The URL where the data will be finally stored. This field is set during
     * the final staging process by the data virtualization. It is the physical
     * location of the data which will be hidden behind logical filenames later
     * by the data organization.
     */
    private String storageUrl = null;
    /**
     * The id of the access point used to perform this ingest.
     */
    private String accessPointId = null;
    /**
     * Plain error message if any error during (pre-)ingest occurs
     */
    @Column
    private String errorMessage = null;
    @OneToMany(fetch = FetchType.EAGER)
    @XmlElement(name = "stagingProcessor")
    private Set<StagingProcessor> stagingProcessors = new HashSet<>();
    @Transient
    @XmlTransient
    private Set<StagingProcessor> clientSideStagingProcessors = null;
    @Transient
    @XmlTransient
    private Set<StagingProcessor> serverSideStagingProcessors = null;
    @Transient
    @XmlTransient
    private Set<StagingProcessor> postArchivingStagingProcessors = null;

    /**
     * Constructor only for bean-compliance. This constructor should no be used
     * directly as setDigitalObjectId() should only be used by deserialization.
     * Due to the invalid state, the status is set to INGEST_STATUS.UNKNOWN,
     * which is blocked by the persistence layer.
     */
    public IngestInformation() {
        status = INGEST_STATUS.UNKNOWN.getId();
    }

    /**
     * Default constructor associating this ingest to a digital object id.
     * Furthermore the status is set to INGEST_STATUS.PREPARING.
     *
     * @param pDigitalObjectId The digital object id.
     */
    public IngestInformation(DigitalObjectId pDigitalObjectId) {
        status = INGEST_STATUS.PREPARING.getId();
        digitalObjectUuid = pDigitalObjectId.getStringRepresentation();
    }

    /**
     * Returns if there was any error during (pre-)ingest or not
     *
     * @return boolean TRUE=If status equals INGEST_STATUS.PRE_INGEST_FAILED or
     * INGEST_STATUS.INGEST_FAILED
     * @see INGEST_STATUS
     */
    public final boolean wasError() {
        return (getStatus() == INGEST_STATUS.INGEST_FAILED.getId()) || (getStatus() == INGEST_STATUS.PRE_INGEST_FAILED.getId());
    }

    /**
     * Returns if this ingest operation has expired or not. If the operation has
     * expired it can be remove from the data backend within the next cleanup
     * cycle
     *
     * @return boolean TRUE if validity is set and if it lays in the past
     */
    @Override
    public final boolean isExpired() {
        return System.currentTimeMillis() > ((expiresAt == -1) ? lastUpdate + DEFAULT_LIFETIME : expiresAt);
    }

    @Override
    public String getDigitalObjectId() {
        return digitalObjectUuid;
    }

    @Override
    public String getDigitalObjectUuid() {
        return getDigitalObjectId();
    }

    @Override
    public void setDigitalObjectId(String pObjectId) {
        digitalObjectUuid = pObjectId;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public void setErrorMessage(String pErrorMessage) {
        this.errorMessage = StringUtils.abbreviate(pErrorMessage, 255);
    }

    @Override
    public long getExpiresAt() {
        return expiresAt;
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public String getOwnerId() {
        return ownerUuid;
    }

    @Override
    public String getOwnerUuid() {
        return getOwnerId();
    }

    @Override
    public String getGroupId() {
        return groupUuid;
    }

    @Override
    public INGEST_STATUS getStatusEnum() {
        return INGEST_STATUS.idToStatus(status);
    }

    @Override
    public String getTransferId() {
        if (transferId == null) {
            transferId = Long.toString(getId());
        }
        return transferId;
    }

    @Override
    public String getClientAccessUrl() {
        return clientAccessUrl;
    }

    @Override
    public void setClientAccessUrl(String pUrl) {
        this.clientAccessUrl = pUrl;
    }

    @Override
    public final URL getDataFolderUrl() {
        if (stagingUrl != null) {
            try {
                return URLCreator.appendToURL(new URL(stagingUrl), Constants.STAGING_DATA_FOLDER_NAME + "/");
            } catch (MalformedURLException ex) {
                throw new IllegalStateException("The staging URL " + stagingUrl + " seems to be invalid", ex);
            }
        }
        throw new IllegalStateException("The data folder URL cannot be returned as long as the staging URL is not set");
    }

    @Override
    public URL getSettingsFolderUrl() {
        if (stagingUrl != null) {
            try {
                return URLCreator.appendToURL(new URL(stagingUrl), "settings/");
            } catch (MalformedURLException ex) {
                throw new IllegalStateException("The staging URL " + stagingUrl + " seems to be invalid", ex);
            }
        }
        throw new IllegalStateException("The settings folder URL cannot be returned as long as the staging URL is not set");
    }

    @Override
    public URL getGeneratedFolderUrl() {
        if (stagingUrl != null) {
            try {
                return URLCreator.appendToURL(new URL(stagingUrl), "generated/");
            } catch (MalformedURLException ex) {
                throw new IllegalStateException("The staging URL " + stagingUrl + " seems to be invalid", ex);
            }
        }
        throw new IllegalStateException("The generated folder URL cannot be returned as long as the staging URL is not set");
    }

    @Override
    public String getStagingUrl() {
        return stagingUrl;
    }

    @Override
    public void setStagingUrl(String pUrl) {
        this.stagingUrl = pUrl;
    }

    @Override
    public String getStorageUrl() {
        return storageUrl;
    }

    @Override
    public void setStorageUrl(String pUrl) {
        this.storageUrl = pUrl;
    }

    @Override
    public void setExpiresAt(long pTimestamp) {
        expiresAt = pTimestamp;
    }

    @Override
    public void setLastUpdate(long pTimestamp) {
        this.lastUpdate = pTimestamp;
    }

    @Override
    public void setOwnerId(String pOwnerId) {
        this.ownerUuid = pOwnerId;
    }

    @Override
    public void setGroupId(String pGroupId) {
        this.groupUuid = pGroupId;
    }

    @Override
    public final void setStatusEnum(INGEST_STATUS pStatus) {
        status = pStatus.getId();
    }

    @Override
    public int getStatus() {
        return status;
    }

    /**
     * Set the status.
     *
     * @param pStatus The status to set.
     */
    public void setStatus(int pStatus) {
        this.status = pStatus;
    }

    /*Return the id. 
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public void setAccessPointId(String pAccessPointId) {
        accessPointId = pAccessPointId;
    }

    @Override
    public String getAccessPointId() {
        return accessPointId;
    }

    /**
     * Set a list of staging processors.
     *
     * @param stagingProcessors A list of staging processors.
     */
    public void setStagingProcessors(Set<StagingProcessor> stagingProcessors) {
        this.stagingProcessors = stagingProcessors;
    }

    @Override
    public Set<StagingProcessor> getStagingProcessors() {
        return stagingProcessors;
    }

    /**
     * Clear the list of staging processors.
     */
    public void clearStagingProcessors() {
        if (stagingProcessors == null) {
            stagingProcessors = new HashSet<>();
        } else {
            stagingProcessors.clear();
        }

        if (serverSideStagingProcessors != null) {
            serverSideStagingProcessors.clear();
        }

        if (clientSideStagingProcessors != null) {
            clientSideStagingProcessors.clear();
        }

        if (postArchivingStagingProcessors != null) {
            postArchivingStagingProcessors.clear();
        }
    }

    /**
     * Add a client-side staging processor.
     *
     * @param pProcessor The processor to add.
     */
    public final void addClientSideStagingProcessor(StagingProcessor pProcessor) {
        stagingProcessors.add(pProcessor);
        if (clientSideStagingProcessors == null) {
            clientSideStagingProcessors = new HashSet<>();
        }
        clientSideStagingProcessors.add(pProcessor);
    }

    /**
     * Get all client-side staging processors.
     *
     * @return All client-side staging processors.
     */
    public final StagingProcessor[] getClientSideStagingProcessor() {
        if (clientSideStagingProcessors == null) {
            clientSideStagingProcessors = new HashSet<>();
            for (StagingProcessor processor : stagingProcessors) {
                if (!processor.getType().equals(StagingProcessor.PROCESSOR_TYPE.SERVER_SIDE_ONLY)) {
                    clientSideStagingProcessors.add(processor);
                }
            }
        }
        return clientSideStagingProcessors.toArray(new StagingProcessor[clientSideStagingProcessors.size()]);
    }

    /**
     * Add a server-side staging processor.
     *
     * @param pProcessor The processor to add.
     */
    public final void addServerSideStagingProcessor(StagingProcessor pProcessor) {
        if (pProcessor == null || pProcessor.getType() == null || !pProcessor.getType().isServerSideProcessor()) {
            throw new IllegalArgumentException("Provided processor must not be null and must be of type PROCESSOR_TYPE.SERVER_SIDE_ONLY or PROCESSOR_TYPE.CLIENT_AND_SERVER_SIDE or PROCESSOR_TYPE.POST_ARCHIVING");
        }

        if (StagingProcessor.PROCESSOR_TYPE.SERVER_SIDE_ONLY.equals(pProcessor.getType())) {
            stagingProcessors.add(pProcessor);
            if (serverSideStagingProcessors == null) {
                serverSideStagingProcessors = new HashSet<>();
            }
            serverSideStagingProcessors.add(pProcessor);
        } else if (StagingProcessor.PROCESSOR_TYPE.POST_ARCHIVING.equals(pProcessor.getType())) {
            addPostArchivingStagingProcessor(pProcessor);
        }
    }

    /**
     * Get all server-side staging processors excluding all processors with type
     * StagingProcessor.PROCESSOR_TYPE.POST_ARCHIVING.
     *
     * @return All server-side staging processors which are not executes after
     * archiving.
     */
    public final StagingProcessor[] getServerSideStagingProcessor() {
        return getServerSideStagingProcessor(true);
    }

    /**
     * Get all server-side staging processors either with or without processors
     * of type StagingProcessor.PROCESSOR_TYPE.POST_ARCHIVING.
     *
     * @param pExcludePostArchiving TRUE = Exclude all processors of type
     * StagingProcessor.PROCESSOR_TYPE.POST_ARCHIVING.
     *
     * @return All server-side staging processors.
     */
    public final StagingProcessor[] getServerSideStagingProcessor(boolean pExcludePostArchiving) {
        if (serverSideStagingProcessors == null) {
            serverSideStagingProcessors = new HashSet<>();
            for (StagingProcessor processor : stagingProcessors) {
                if (!processor.getType().equals(StagingProcessor.PROCESSOR_TYPE.CLIENT_SIDE_ONLY)) {
                    if (!processor.getType().equals(StagingProcessor.PROCESSOR_TYPE.POST_ARCHIVING) || !pExcludePostArchiving) {
                        //add processor if it is not of type POST_ARCHIVING or if POST_ARCHIVING processors should not be excluded
                        serverSideStagingProcessors.add(processor);
                    }
                }
            }
        }
        return serverSideStagingProcessors.toArray(new StagingProcessor[serverSideStagingProcessors.size()]);
    }

    /**
     * Add a post-archiving staging processor.
     *
     * @param pProcessor The processor to add.
     */
    public final void addPostArchivingStagingProcessor(StagingProcessor pProcessor) {
        if (pProcessor == null || !StagingProcessor.PROCESSOR_TYPE.POST_ARCHIVING.equals(pProcessor.getType())) {
            throw new IllegalArgumentException("Provided processor must not be null and must be of type PROCESSOR_TYPE.POST_ARCHIVING");
        }
        stagingProcessors.add(pProcessor);
        if (postArchivingStagingProcessors == null) {
            postArchivingStagingProcessors = new HashSet<>();
        }
        postArchivingStagingProcessors.add(pProcessor);
    }

    /**
     * Get all post-archiving staging processors.
     *
     * @return All post-archiving staging processors.
     */
    public final StagingProcessor[] getPostArchivingStagingProcessor() {
        if (postArchivingStagingProcessors == null) {
            postArchivingStagingProcessors = new HashSet<>();
            for (StagingProcessor processor : stagingProcessors) {
                if (processor.getType().equals(StagingProcessor.PROCESSOR_TYPE.POST_ARCHIVING)) {
                    postArchivingStagingProcessors.add(processor);
                }
            }
        }
        return postArchivingStagingProcessors.toArray(new StagingProcessor[postArchivingStagingProcessors.size()]);
    }

    @Override
    public final int hashCode() {
        int hash = 3;
        hash = 61 * hash + this.status;
        hash = 61 * hash + (int) (this.id ^ (this.id >>> 32));
        //hash = 61 * hash + (int) (this.lastUpdate ^ (this.lastUpdate >>> 32));
        hash = 61 * hash + (int) (this.expiresAt ^ (this.expiresAt >>> 32));
        hash = 61 * hash + (this.transferId != null ? this.transferId.hashCode() : 0);
        hash = 61 * hash + (this.digitalObjectUuid != null ? this.digitalObjectUuid.hashCode() : 0);
        hash = 61 * hash + (this.ownerUuid != null ? this.ownerUuid.hashCode() : 0);
        hash = 61 * hash + (this.clientAccessUrl != null ? this.clientAccessUrl.hashCode() : 0);
        hash = 61 * hash + (this.storageUrl != null ? this.storageUrl.hashCode() : 0);
        hash = 61 * hash + (this.stagingUrl != null ? this.stagingUrl.hashCode() : 0);
        hash = 61 * hash + (this.errorMessage != null ? this.errorMessage.hashCode() : 0);
        return hash;
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IngestInformation other = (IngestInformation) obj;
        if (this.status != other.status) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.expiresAt != other.expiresAt) {
            return false;
        }
        if ((this.transferId == null) ? (other.transferId != null) : !this.transferId.equals(other.transferId)) {
            return false;
        }
        if ((this.digitalObjectUuid == null) ? (other.digitalObjectUuid != null) : !this.digitalObjectUuid.equals(other.digitalObjectUuid)) {
            return false;
        }
        if ((this.ownerUuid == null) ? (other.ownerUuid != null) : !this.ownerUuid.equals(other.ownerUuid)) {
            return false;
        }
        if ((this.clientAccessUrl == null) ? (other.clientAccessUrl != null) : !this.clientAccessUrl.equals(other.clientAccessUrl)) {
            return false;
        }
        if ((this.storageUrl == null) ? (other.storageUrl != null) : !this.storageUrl.equals(other.storageUrl)) {
            return false;
        }
        if ((this.stagingUrl == null) ? (other.stagingUrl != null) : !this.stagingUrl.equals(other.stagingUrl)) {
            return false;
        }
        return !((this.errorMessage == null) ? (other.errorMessage != null) : !this.errorMessage.equals(other.errorMessage));
    }
}
