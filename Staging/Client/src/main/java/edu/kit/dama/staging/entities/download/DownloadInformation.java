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
package edu.kit.dama.staging.entities.download;

import edu.kit.dama.staging.entities.ingest.INGEST_STATUS;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.staging.entities.StagingProcessor;
import edu.kit.dama.staging.entities.interfaces.IDefaultDownloadInformation;
import edu.kit.dama.staging.interfaces.ITransferInformation;
import edu.kit.dama.util.DataManagerSettings;
import edu.kit.tools.url.URLCreator;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 * Download information object representing a single data download. The status
 * of the download is represented by the 'status' property which holds a status
 * code based on valid elements of DOWNLOAD_STATUS. A new download information
 * should be created by the constructor taking a digital object id as argument,
 * as there is no setter for changing the digital object id after instantiation.
 * The empty constructor is only available for reasons of bean-compliance.
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
                @XmlNamedAttributeNode("errorMessage"),
                @XmlNamedAttributeNode("accessPointId"),
                @XmlNamedAttributeNode(value = "stagingProcessors", subgraph = "simple")
            })
})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
//</editor-fold>
public class DownloadInformation implements IDefaultDownloadInformation, ITransferInformation<DOWNLOAD_STATUS>, Serializable {

    //default lifetime for unfinished ingest is one week
    @Transient
    public static final long DEFAULT_LIFETIME = DataManagerSettings.getSingleton().getLongProperty(DataManagerSettings.STAGING_MAX_DOWNLOAD_LIFETIME, 60 * 60 * 24 * 7) * 1000;
    /**
     * The status of this ingest
     */
    private int status = DOWNLOAD_STATUS.UNKNOWN.getId();
    /**
     * The id of this download (unique/autoincrement for RDBMS)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;
    /**
     * Timestamp when this download entry was updated the last time
     */
    private long lastUpdate = -1l;
    /**
     * Timestamp when this download entry is removed automatically
     */
    private long expiresAt = -1l;
    /**
     * The UUID of the download. This ID must be unique for each download
     */
    private String transferId = null;
    /**
     * The UUID of the digital object this download is associated with
     */
    @Column(nullable = false, updatable = false)
    private String digitalObjectUuid = null;
    /**
     * The UUID of the owner/initiator of this download operation
     */
    private String ownerUuid = null;
    /**
     * The UUID of the group this download belongs to.
     */
    private String groupUuid = null;

    /*
   * The URL which will be accessible by external clients. This URL may point to
   * a download of a prepared client or directly to some physical data location
   * accessible via an appropriate protocol.
     */
    private String clientAccessUrl = null;
    /**
     * The URL where the data will be copied during download preparation.
     * Basically this should be some caching location.
     */
    private String stagingUrl = null;
    /**
     * Plain error message if any error during (pre-)ingest occurs
     */
    @Column
    private String errorMessage = null;
    /**
     * The id of the access point used to perform this download.
     */
    private String accessPointId = null;
    @OneToMany(fetch = FetchType.EAGER)
    @XmlElement(name = "stagingProcessor")
    private Set<StagingProcessor> stagingProcessors = new HashSet<>();
    @Transient
    @XmlTransient
    private Set<StagingProcessor> clientSideStagingProcessors = null;
    @Transient
    @XmlTransient
    private Set<StagingProcessor> serverSideStagingProcessors = null;

    /**
     * Constructor only for bean-compliance. This constructor should no be used
     * directly as setDigitalObjectId() should only be used by deserialization.
     * Due to the invalid state, the status is set to INGEST_STATUS.UNKNOWN,
     * which is blocked by the persistence layer.
     */
    public DownloadInformation() {
        setStatusInternal(DOWNLOAD_STATUS.UNKNOWN.getId());
    }

    /**
     * Default constructor associating this download to a digital object id.
     * Furthermore the status is set to DOWNLOAD_STATUS.SCHEDULED.
     *
     * @param pDigitalObjectId The digital object id.
     */
    public DownloadInformation(DigitalObjectId pDigitalObjectId) {
        setStatusInternal(DOWNLOAD_STATUS.SCHEDULED.getId());
        digitalObjectUuid = pDigitalObjectId.getStringRepresentation();
    }

    /**
     * Returns if there was any error during (pre-)ingest or not.
     *
     * @return boolean TRUE=If status equals INGEST_STATUS.PRE_INGEST_FAILED or
     * INGEST_STATUS.INGEST_FAILED.
     *
     * @see INGEST_STATUS
     */
    public final boolean wasError() {
        return (getStatus() == INGEST_STATUS.INGEST_FAILED.getId()) || (getStatus() == INGEST_STATUS.PRE_INGEST_FAILED.getId());
    }

    /**
     * Returns if this ingest operation has expired or not. If the operation has
     * expired it can be remove from the data backend within the next cleanup
     * cycle.
     *
     * @return boolean TRUE if validity is set and if it lays in the past.
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
    public DOWNLOAD_STATUS getStatusEnum() {
        return DOWNLOAD_STATUS.idToStatus(getStatus());
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
    public URL getDataFolderUrl() {
        if (stagingUrl != null) {
            try {
                return URLCreator.appendToURL(new URL(stagingUrl), "data/");
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
    public void setStagingUrl(String pUrl) {
        this.stagingUrl = pUrl;
    }

    @Override
    public String getStagingUrl() {
        return stagingUrl;
    }

    @Override
    public String getStorageUrl() {
        return null;
    }

    @Override
    public void setStorageUrl(String pUrl) {
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
    public final void setStatusEnum(DOWNLOAD_STATUS pStatus) {
        status = pStatus.getId();
    }

    /**
     * Get the status.
     *
     * @return The status.
     */
    @Override
    public int getStatus() {
        return status;
    }

    /**
     * Set the status internally used by the constructor.
     *
     * @param pStatus The status to set.
     */
    private void setStatusInternal(int pStatus) {
        setStatus(pStatus);
    }

    /**
     * Set the status.
     *
     * @param pStatus The status to set.
     */
    public void setStatus(int pStatus) {
        this.status = pStatus;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id The id to set.
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
        if (pProcessor == null || !StagingProcessor.PROCESSOR_TYPE.SERVER_SIDE_ONLY.equals(pProcessor.getType()) && !StagingProcessor.PROCESSOR_TYPE.CLIENT_AND_SERVER_SIDE.equals(pProcessor.getType())) {
            throw new IllegalArgumentException("Provided processor must not be null and must be of type PROCESSOR_TYPE.SERVER_SIDE_ONLY");
        }
        stagingProcessors.add(pProcessor);
        if (serverSideStagingProcessors == null) {
            serverSideStagingProcessors = new HashSet<>();
        }
        serverSideStagingProcessors.add(pProcessor);
    }

    /**
     * Get all server-side staging processors.
     *
     * @return All server-side staging processors.
     */
    public final StagingProcessor[] getServerSideStagingProcessor() {
        if (serverSideStagingProcessors == null) {
            serverSideStagingProcessors = new HashSet<>();
            for (StagingProcessor processor : stagingProcessors) {
                if (!processor.getType().equals(StagingProcessor.PROCESSOR_TYPE.CLIENT_SIDE_ONLY)) {
                    serverSideStagingProcessors.add(processor);
                }
            }
        }
        return serverSideStagingProcessors.toArray(new StagingProcessor[serverSideStagingProcessors.size()]);
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
        final DownloadInformation other = (DownloadInformation) obj;
        if (this.status != other.status) {
            return false;
        }
        if (this.id != other.id) {
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
        if ((this.stagingUrl == null) ? (other.stagingUrl != null) : !this.stagingUrl.equals(other.stagingUrl)) {
            return false;
        }
        if ((this.errorMessage == null) ? (other.errorMessage != null) : !this.errorMessage.equals(other.errorMessage)) {
            return false;
        }
        return true;
    }
}
