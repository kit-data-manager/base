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

import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultDataWorkflowConfiguration;
import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @author mf6319
 */
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
                @XmlNamedAttributeNode("name"),
                @XmlNamedAttributeNode("description"),
                @XmlNamedAttributeNode("version"),
                @XmlNamedAttributeNode("creationDate"),
                @XmlNamedAttributeNode("keywords"),
                @XmlNamedAttributeNode("contactUserId"),
                @XmlNamedAttributeNode("groupId"),
                @XmlNamedAttributeNode("applicationPackageUrl"),
                @XmlNamedAttributeNode("applicationArguments"),
                @XmlNamedAttributeNode("defaultTask"),
                @XmlNamedAttributeNode("disabled"),
                @XmlNamedAttributeNode(value = "requiredEnvironmentProperties", subgraph = "default")
            })})
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "DataWorkflowTaskConfiguration.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")}),
    @NamedEntityGraph(
            name = "DataWorkflowTaskConfiguration.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("name"),
                @NamedAttributeNode("description"),
                @NamedAttributeNode("version"),
                @NamedAttributeNode("creationDate"),
                @NamedAttributeNode("keywords"),
                @NamedAttributeNode("contactUserId"),
                @NamedAttributeNode("groupId"),
                @NamedAttributeNode("applicationPackageUrl"),
                @NamedAttributeNode("applicationArguments"),
                @NamedAttributeNode("defaultTask"),
                @NamedAttributeNode("disabled"),
                @NamedAttributeNode(value = "requiredEnvironmentProperties", subgraph = "DataWorkflowTaskConfiguration.default.EnvironmentProperty.default"),},
            subgraphs = {
                @NamedSubgraph(
                        name = "DataWorkflowTaskConfiguration.default.EnvironmentProperty.default",
                        attributeNodes = {
                            @NamedAttributeNode("id"),
                            @NamedAttributeNode("name"),
                            @NamedAttributeNode("description")
                        }
                )
            })
})
public class DataWorkflowTaskConfiguration implements IDefaultDataWorkflowConfiguration, FetchGroupTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Name of the configuration.
     */
    private String name;
    /**
     * Description of the configuration.
     */
    @Column(length = 1024)
    private String description;
    /**
     * Version number of the configuration.
     */
    private Integer version;
    /**
     * The creation date of the configuration.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    /**
     * Keywords describing the configuration, e.g. for search. Multiple keywords
     * must be comma separated.
     */
    private String keywords;

    /**
     * Contact userId for issues, questions and other requests.
     */
    private String contactUserId;
    /**
     * The groupId by which this task configuration is intended to be used.
     */
    private String groupId = null;

    /**
     * URL of the ZIP file which contains the application and all files needed
     * to execute the application, except dependencies already covered by the
     * registered dependency elements. The URL will be accessed during
     * preparation and will be ectracted into the working directory.
     */
    private String applicationPackageUrl;

    /**
     * Default application arguments that will be appended for each execution.
     * Multiple arguments are separated by spaces.
     */
    private String applicationArguments;
    /**
     * Flag that indicated that this task is enabled by default, e.g. for new
     * ingests.
     */
    private Boolean defaultTask = false;
    /**
     * Flag that indicated, that this task is disabeled, e.g. should be used any
     * longer.
     */
    private Boolean disabled = false;

    @OneToMany(fetch = FetchType.EAGER)
    @XmlElementWrapper(name = "executionEnvironmentProperties")
    @XmlElement(name = "executionEnvironmentProperty")
    private Set<ExecutionEnvironmentProperty> requiredEnvironmentProperties = new HashSet<>();

    /**
     * Default configuration.
     */
    public DataWorkflowTaskConfiguration() {
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Integer getVersion() {
        return version;
    }

    /**
     * Set id of this configuration.
     *
     * @param pId The id to set.
     */
    public void setId(Long pId) {
        this.id = pId;
    }

    /**
     * Set name of the configuration.
     *
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Set description of the configuration.
     *
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Set version number of the configuration.
     *
     * @param version The version to set.
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * Set the creation date of the configuration.
     *
     * @param creationDate The creation date.
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * Set keywords describing the configuration. Multiple keywords must be
     * comma separated.
     *
     * @param keywords Keywords for this configuration.
     */
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    @Override
    public String getKeywords() {
        return keywords;
    }

    @Override
    public String getApplicationArguments() {
        return applicationArguments;
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

    /**
     * Set the application arguments used for every application call.
     *
     * @param applicationArguments The application arguments.
     */
    public void setApplicationArguments(String applicationArguments) {
        this.applicationArguments = applicationArguments;
    }

    @Override
    public String getContactUserId() {
        return contactUserId;
    }

    /**
     * Set the contact userId for this configuration.
     *
     * @param contactUserId The contact userId to set.
     */
    public void setContactUserId(String contactUserId) {
        this.contactUserId = contactUserId;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set the groupId for this configuration.
     *
     * @param groupId The groupId to set.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getApplicationPackageUrl() {
        return applicationPackageUrl;
    }

    /**
     * Set the application package URL. The application package is a ZIP file
     * which contains the application associated with this configuration. The
     * URL can be local or remote. Before the processing starts, the archive is
     * obtained and extracted into the working directory of the execution.
     *
     * @param pApplicationPackageUrl The applicationPackageUrl to set.
     */
    public void setApplicationPackageUrl(String pApplicationPackageUrl) {
        this.applicationPackageUrl = pApplicationPackageUrl;
    }

    @Override
    public Boolean isDefaultTask() {
        return defaultTask;
    }

    /**
     * Flag this task as default.
     *
     * @param defaultTask New default task flag value.
     */
    public void setDefaultTask(boolean defaultTask) {
        this.defaultTask = defaultTask;
    }

    @Override
    public Boolean isDisabled() {
        return disabled;
    }

    /**
     * Flag this task as disabled.
     *
     * @param disabled New disabled flag value.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public Set<ExecutionEnvironmentProperty> getRequiredEnvironmentProperties() {
        return requiredEnvironmentProperties;
    }

    /**
     * Set the list of required environment properties.
     *
     * @param requiredEnvironmentProperties The list of required environment
     * properties.
     */
    public void setRequiredEnvironmentProperties(Set<ExecutionEnvironmentProperty> requiredEnvironmentProperties) {
        this.requiredEnvironmentProperties = requiredEnvironmentProperties;
    }

    /**
     * Add a required environment property.
     *
     * @param pProperty The property to add.
     */
    public void addRequiredEnvironmentProperty(ExecutionEnvironmentProperty pProperty) {
        requiredEnvironmentProperties.add(pProperty);
    }

    /**
     * Remove a required environment property.
     *
     * @param pProperty The property to remove.
     */
    public void removeRequiredEnvironmentProperty(ExecutionEnvironmentProperty pProperty) {
        requiredEnvironmentProperties.remove(pProperty);
    }

    /**
     * Remove all required environment properties.
     */
    public void removeRequiredEnvironmentProperties() {
        requiredEnvironmentProperties.clear();
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
