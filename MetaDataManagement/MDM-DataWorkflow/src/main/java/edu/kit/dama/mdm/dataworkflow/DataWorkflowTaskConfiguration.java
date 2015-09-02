/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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

import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.mdm.base.UserData;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

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
            @XmlNamedAttributeNode(value = "contact", subgraph = "simple"),
            @XmlNamedAttributeNode("applicationPackageUrl"),
            @XmlNamedAttributeNode("applicationArguments"),
            @XmlNamedAttributeNode(value = "requiredEnvironmentProperties", subgraph = "default")
          })})
public class DataWorkflowTaskConfiguration {

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
   * URL of the ZIP file which contains the application and all files needed to
   * execute the application, except dependencies already covered by the
   * registered dependency elements. The URL will be accessed during preparation
   * and will be ectracted into the working directory.
   */
  private String applicationPackageUrl;

  /**
   * Default application arguments that will be appended for each execution.
   * Multiple arguments are separated by spaces.
   */
  private String applicationArguments;

  @OneToMany(fetch = FetchType.EAGER)
  @XmlElementWrapper(name = "executionEnvironmentProperties")
  @XmlElement(name = "executionEnvironmentProperty")
  private Set<ExecutionEnvironmentProperty> requiredEnvironmentProperties = new HashSet<>();

  /**
   * Default configuration.
   */
  public DataWorkflowTaskConfiguration() {
  }

  /**
   * Get identification number of this instance.
   *
   * @return the configID
   */
  public Long getId() {
    return id;
  }

  /**
   * Get name of the configuration.
   *
   * @return The name.
   */
  public String getName() {
    return name;
  }

  /**
   * Get description of the configuration.
   *
   * @return The description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get version number of the configuration.
   *
   * @return The version number.
   */
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
  public void setLabel(String name) {
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

  /**
   * Get the creation date of the configuration.
   *
   * @return The creation date.
   */
  public Date getCreationDate() {
    return creationDate;
  }

  /**
   * Set keywords describing the configuration. Multiple keywords must be comma
   * separated.
   *
   * @param keywords Keywords for this configuration.
   */
  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  /**
   * Set the keywords describing the configuration.
   *
   * @return Keywords for this configuration.
   */
  public String getKeywords() {
    return keywords;
  }

  /**
   * Get the application arguments.
   *
   * @return Application arguments .
   */
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

  /**
   * Get the contact userId for this configuration.
   *
   * @return The contact userId.
   */
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

  /**
   * Get the URL of the application package.
   *
   * @return The applicationPackageUrl.
   */
  public String getApplicationPackageUrl() {
    return applicationPackageUrl;
  }

  /**
   * Set the application package URL. The application package is a ZIP file
   * which contains the application associated with this configuration. The URL
   * can be local or remote. Before the processing starts, the archive is
   * obtained and extracted into the working directory of the execution.
   *
   * @param pApplicationPackageUrl The applicationPackageUrl to set.
   */
  public void setApplicationPackageUrl(String pApplicationPackageUrl) {
    this.applicationPackageUrl = pApplicationPackageUrl;
  }

  /**
   * Get the list of required environment properties.
   *
   * @return The list of required environment properties.
   */
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
}
