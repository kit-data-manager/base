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
package edu.kit.dama.staging.entities;

import edu.kit.dama.staging.entities.interfaces.IDefaultStagingAccessPointConfiguration;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 * Staging access point configuration. This configuration defines an access
 * point for a caching location for staging. Each access access point can be
 * identified by a unique identifier and a plain name. Furthermore, a remote
 * base URL and a local path are defined. The remote base URL is the URL which
 * is used to access the local path remotely using this access point. E.g. for
 * GridFTP the remote base URL might be
 * gsiftp://ipelsdf1.lsdf.kit.edu:2811/tmp/cache/, whereas the local path might
 * be /nfs/storage/data/. But in most of all cases, remote base URL and local
 * path will look similar. How the mapping between remote and local path is
 * realized, is decided in an implementation of AbstractStagingAccessPoint which
 * uses this configuration class for basic setup.
 *
 * @author jejkal
 */
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
                @XmlNamedAttributeNode("uniqueIdentifier"),
                @XmlNamedAttributeNode("implementationClass"),
                @XmlNamedAttributeNode("name"),
                @XmlNamedAttributeNode("description"),
                @XmlNamedAttributeNode("groupId"),
                @XmlNamedAttributeNode("customProperties"),
                @XmlNamedAttributeNode("remoteBaseUrl"),
                @XmlNamedAttributeNode("localBasePath"),
                @XmlNamedAttributeNode("defaultAccessPoint"),
                @XmlNamedAttributeNode("transientAccessPoint"),
                @XmlNamedAttributeNode("disabled")
            })
})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@Entity
public class StagingAccessPointConfiguration implements IDefaultStagingAccessPointConfiguration, Cloneable, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String uniqueIdentifier = null;
    /**
     * The implementation class of this access point.
     */
    private String implementationClass = null;
    /**
     * The plain name.
     */
    private String name = null;
    /**
     * A plain description.
     */
    @Column(length = 1024)
    private String description = null;
    /**
     * The groupId for which this access point is intended to be used.
     */
    private String groupId = null;
    /**
     * The serialized custom properties.
     */
    @Column(length = 1024)
    private String customProperties = null;

    /**
     * Base URL for remote access.
     */
    private String remoteBaseUrl = null;
    /**
     * Local base path equivalent to remoteBaseUrl.
     */
    private String localBasePath = null;
    /**
     * Flag which indicates if this access point is the default one.
     */
    private boolean defaultAccessPoint = false;
    /**
     * Flag which defines whether this access point is transient or not. If an
     * access point is transient, the local folder of this access point will be
     * re-created on each start. This flag is basically intended to be used for
     * testing.
     */
    private boolean transientAccessPoint = false;
    /**
     * Flag which indicates if this access point is disabled or not.
     */
    private boolean disabled = false;

    /**
     * Factory a new access point configuration with the provided identifier.
     *
     * @param pIdentifier The unique access point configuration identifier.
     *
     * @return The new access point configuration .
     */
    public static StagingAccessPointConfiguration factoryNewStagingAccessPointConfiguration(String pIdentifier) {
        if (pIdentifier == null) {
            throw new IllegalArgumentException("Argument 'pIdentifier' must not be 'null'");
        }
        StagingAccessPointConfiguration result = new StagingAccessPointConfiguration();
        result.setUniqueIdentifier(pIdentifier);
        return result;
    }

    /**
     * Factory a new access point configuration with an auto-generated
     * identifier. The identifier is generated using
     * {@link java.util.UUID#randomUUID()}
     *
     * @return The new access point configuration.
     */
    public static StagingAccessPointConfiguration factoryNewStagingAccessPointConfiguration() {
        return factoryNewStagingAccessPointConfiguration(UUID.randomUUID().toString());
    }

    /**
     * Default constructor.
     */
    public StagingAccessPointConfiguration() {
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the unique identifier.
     *
     * @param uniqueIdentifier A unique identifier.
     */
    protected void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    /**
     * Set the implementation class.
     *
     * @param implementationClass The implementation class.
     */
    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    @Override
    public String getImplementationClass() {
        return implementationClass;
    }

    /**
     * Set the group id this method is valid for.
     *
     * @param groupId The group id this method is valid for.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set this access point's name.
     *
     * @param name This access point's name.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the description.
     *
     * @param description The description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Set the custom properties path.
     *
     * @param customProperties The custom properties path.
     */
    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public String getCustomProperties() {
        return customProperties;
    }

    /**
     * Set custom properties as object.
     *
     * @param pProperties The properties object.
     *
     * @throws IOException If the serialization failed.
     */
    public void setPropertiesAsObject(Properties pProperties) throws IOException {
        this.customProperties = PropertiesUtil.propertiesToString(pProperties);
    }

    /**
     * Get the custom properties as object.
     *
     * @return The properties object.
     *
     * @throws IOException If the deserialization failed.
     */
    public Properties getPropertiesAsObject() throws IOException {
        return PropertiesUtil.propertiesFromString(customProperties);
    }

    @Override
    public String getRemoteBaseUrl() {
        return remoteBaseUrl;
    }

    /**
     * Set the remote base Url. This url defines the remote access point to a
     * folder, e.g. http://anyhost:80/webdav/. To reflect the folder status,
     * remoteBaseUrl should end with a slash.
     *
     * @param remoteBaseUrl The remote base Url.
     */
    public void setRemoteBaseUrl(String remoteBaseUrl) {
        this.remoteBaseUrl = remoteBaseUrl;
    }

    @Override
    public String getLocalBasePath() {
        return localBasePath;
    }

    /**
     * Set the local base path. This path defines the locally accessible path
     * equivalent to remoteBaseUrl, e.g. /var/www/htdocs/davfolder/. To reflect
     * the folder status, localBasePath should end with a slash.
     *
     * @param localBasePath The local base path..
     */
    public void setLocalBasePath(String localBasePath) {
        this.localBasePath = localBasePath;
    }

    /**
     * Set this access point as default for the associated group.
     *
     * @param defaultAccessPoint TRUE = default access point.
     */
    public void setDefaultAccessPoint(boolean defaultAccessPoint) {
        this.defaultAccessPoint = defaultAccessPoint;
    }

    @Override
    public boolean isDefaultAccessPoint() {
        return defaultAccessPoint;
    }

    /**
     * (Un-)Set this access point transient.
     *
     * @param transientAccessPoint TRUE = the access point is transient.
     */
    public void setTransientAccessPoint(boolean transientAccessPoint) {
        this.transientAccessPoint = transientAccessPoint;
    }

    @Override
    public boolean isTransientAccessPoint() {
        return transientAccessPoint;
    }

    /**
     * Enable/disable this access point.
     *
     * @param disabled TRUE = Disabled.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public StagingAccessPointConfiguration clone() {
        StagingAccessPointConfiguration temporaryAccessPoint
                = StagingAccessPointConfiguration.factoryNewStagingAccessPointConfiguration(
                        getUniqueIdentifier());
        temporaryAccessPoint.setName(getName());
        temporaryAccessPoint.setGroupId(getGroupId());
        temporaryAccessPoint.setRemoteBaseUrl(getRemoteBaseUrl());
        temporaryAccessPoint.setLocalBasePath(getLocalBasePath());
        temporaryAccessPoint.setDefaultAccessPoint(isDefaultAccessPoint());
        temporaryAccessPoint.setDisabled(isDisabled());
        temporaryAccessPoint.setTransientAccessPoint(isTransientAccessPoint());
        temporaryAccessPoint.setDescription(getDescription());
        return temporaryAccessPoint;
    }
}
