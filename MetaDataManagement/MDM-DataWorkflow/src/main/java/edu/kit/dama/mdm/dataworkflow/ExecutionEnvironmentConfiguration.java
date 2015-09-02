/*
 * Copyright 2015 Karlsruhe Institute of Technology.
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
package edu.kit.dama.mdm.dataworkflow;

import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import org.apache.commons.io.FileUtils;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration entity describing an execution environment capable of executing
 * DataWorkflow tasks. Each execution environment is defined to have:
 *
 * <ul>
 * <li>Basic metadata (name, description etc.)</li>
 * <li>An associated StagingAccessPoint used to provide/obtain data for/from
 * task executions</li>
 * <li>A list of ExecutionEnvironmentProperty offered by this environment
 * defining whether an environment is capable of executing a specific task that
 * requires a specific set of properties.</li>
 * </ul>
 *
 * Furthermore, the ExecutionEnvironmentConfiguration defined the implementation
 * class of an AbstractExecutionEnvironmentHandler capable of submitting DataWorkflow
 * tasks using this configuration.
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
            @XmlNamedAttributeNode("customProperties"),
            @XmlNamedAttributeNode("groupId"),
            @XmlNamedAttributeNode("handlerImplementationClass"),
            @XmlNamedAttributeNode("stagingAccessPointId"),
            @XmlNamedAttributeNode("accessPointLocalBasePath"),
            @XmlNamedAttributeNode("maxParallelTasks"),
            @XmlNamedAttributeNode("defaultEnvironment"),
            @XmlNamedAttributeNode("disabled")
          })})
public class ExecutionEnvironmentConfiguration {

  /**
   * For logging purposes.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionEnvironmentConfiguration.class);
  /**
   * Id of the environment. Has to be unique.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /**
   * Human readable name.
   */
  private String name = null;
  /**
   * Human readable description.
   */
  @Column(length = 10240)
  private String decription = null;
  /**
   * The serialized custom properties used to configure the execution
   * environment handler.
   */
  @Column(length = 10240)
  private String customProperties = null;

  /**
   * The groupId for which this environment is intended to be used.
   */
  private String groupId = null;

  /**
   * The implementation class of the execution environment handler.
   */
  private String handlerImplementationClass = null;

  /**
   * Staging access point configuration used to stage data in and out the
   * computing environment. Inside the computing environment, the path of the
   * access point is reachable via {@link #accessPointLocalBasePath} which can
   * but must not be equal to the access point's local base path.
   */
  private String stagingAccessPointId = null;

  /**
   * The local base path where the location accessed via
   * {@link #stagingAccessPointId} is made available. Basically, this path
   * contains the absolute file path where the staging location is reachable for
   * all computing nodes/processes, e.g. via mount point.
   */
  private String accessPointLocalBasePath = null;

  /**
   * The number of max. parallel DataWorkflow tasks running on this execution
   * environment at the same time in parallel.
   */
  private int maxParallelTasks = 10;
  /**
   * Flag which indicates if this environment is the default one.
   */
  private boolean defaultEnvironment = false;

  /**
   * Flag which indicates if this environment is disabled or not.
   */
  private boolean disabled = false;

  @OneToMany(fetch = FetchType.EAGER)
  private Set<ExecutionEnvironmentProperty> providedEnvironmentProperties = new HashSet<>();

  /**
   * Set custom properties as object.
   *
   * @param pProperties The properties object.
   *
   * @throws IOException If the serialization failed.
   */
  public void setPropertiesAsObject(Properties pProperties) throws IOException {
    String serialized = PropertiesUtil.propertiesToString(pProperties);
    if (serialized != null && serialized.length() > 10 * FileUtils.ONE_KB) {
      throw new IOException("Failed to store custom properties from object. Serialized content exceeds max. size of database field (10 KB).");
    }

    this.setCustomProperties(serialized);
  }

  /**
   * Get the custom properties as object.
   *
   * @return The properties object.
   *
   * @throws IOException If the deserialization failed.
   */
  public Properties getPropertiesAsObject() throws IOException {
    return PropertiesUtil.propertiesFromString(getCustomProperties());
  }

  /**
   * Check whether this execution environment is applicable for executing the
   * provided task. This method internally maps to {@link #canExecute(edu.kit.dama.dataworkflow.DataWorkflowTaskConfiguration)
   * } by using {@link DataWorkflowTask#getConfiguration() }.
   *
   * @param pTask The task to check.
   *
   * @return TRUE if all required properties are provided.
   */
  public boolean canExecute(DataWorkflowTask pTask) {
    return canExecute(pTask.getConfiguration());
  }

  /**
   * Check whether this execution environment is applicable for executing the
   * provided configuration. The check will compare the provided
   * ExecutionEnvironmentProperties with the required ones defined in the
   * DataWorkflowConfiguration. True is returned, if the environment provides all
   * required properties.
   *
   * @param pConfiguration The configuration to check.
   *
   * @return TRUE if all required properties are provided.
   */
  public boolean canExecute(DataWorkflowTaskConfiguration pConfiguration) {
    Set<ExecutionEnvironmentProperty> required = pConfiguration.getRequiredEnvironmentProperties();
    boolean result = true;
    for (ExecutionEnvironmentProperty property : required) {
      if (!providedEnvironmentProperties.contains(property)) {
        //not applicable ... Log this
        LOGGER.error("Execution environment with id {} cannot handle task configuration with id {}. Property {} not supported.", getId(), pConfiguration.getId(), property);
        result = false;
        break;
      }
    }
    //all available
    return result;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the decription
   */
  public String getDecription() {
    return decription;
  }

  /**
   * @param decription the decription to set
   */
  public void setDecription(String decription) {
    this.decription = decription;
  }

  /**
   * @return the customProperties
   */
  public String getCustomProperties() {
    return customProperties;
  }

  /**
   * @param customProperties the customProperties to set
   */
  public void setCustomProperties(String customProperties) {
    this.customProperties = customProperties;
  }

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * @return the handlerImplementationClass
   */
  public String getHandlerImplementationClass() {
    return handlerImplementationClass;
  }

  /**
   * @param handlerImplementationClass the handlerImplementationClass to set
   */
  public void setHandlerImplementationClass(String handlerImplementationClass) {
    this.handlerImplementationClass = handlerImplementationClass;
  }

  /**
   * Get the id of the StagingAccessPoint that will be used to provide/obtain
   * data to/from task executions.
   *
   * @return the stagingAccessPointId
   */
  public String getStagingAccessPointId() {
    return stagingAccessPointId;
  }

  /**
   * Set the id of the StagingAccessPoint that will be used to provide/obtain
   * data to/from task executions.
   *
   * @param stagingAccessPointId The stagingAccessPointId to set.
   */
  public void setStagingAccessPointId(String stagingAccessPointId) {
    this.stagingAccessPointId = stagingAccessPointId;
  }

  /**
   * Get the local path used by the AccessPoint of this
   * EnvironmentConfiguration.
   *
   * @return The accessPointLocalBasePath.
   */
  public String getAccessPointLocalBasePath() {
    return accessPointLocalBasePath;
  }

  /**
   * Set the local path used by the AccessPoint of this
   * EnvironmentConfiguration.
   *
   * @param accessPointLocalBasePath The accessPointLocalBasePath to set.
   */
  public void setAccessPointLocalBasePath(String accessPointLocalBasePath) {
    this.accessPointLocalBasePath = accessPointLocalBasePath;
  }

  /**
   * Get the number of max. parallel tasks running in this execution
   * environment.
   *
   * @return The number of max. parallel tasks.
   */
  public int getMaxParallelTasks() {
    return maxParallelTasks;
  }

  /**
   * Set the number of max. parallel tasks running in this execution
   * environment.
   *
   * @param maxParallelTasks The number of max. parallel tasks.
   */
  public void setMaxParallelTasks(int maxParallelTasks) {
    this.maxParallelTasks = maxParallelTasks;
  }

  /**
   * Get whether this ExecutionEnvironment is the default one for the group it
   * is associated with.
   *
   * @return TRUE = This is the default ExecutionEnvironment.
   */
  public boolean isDefaultEnvironment() {
    return defaultEnvironment;
  }

  /**
   * Set whether this ExecutionEnvironment is the default one for the group it
   * is associated with.
   *
   * @param defaultEnvironment TRUE = This is the default ExecutionEnvironment.
   */
  public void setDefaultEnvironment(boolean defaultEnvironment) {
    this.defaultEnvironment = defaultEnvironment;
  }

  /**
   * Check whether this ExecutionEnvironment is disabled or not.
   *
   * @return TRUE = This ExecutionEnvironment is disabled.
   */
  public boolean isDisabled() {
    return disabled;
  }

  /**
   * Set whether this ExecutionEnvironment is disabled or not.
   *
   * @param disabled TRUE = This ExecutionEnvironment is disabled.
   */
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  /**
   * Get the list of provided environment properties.
   *
   * @return The list of provided environment properties.
   */
  public Set<ExecutionEnvironmentProperty> getProvidedEnvironmentProperties() {
    return providedEnvironmentProperties;
  }

  /**
   * Set the list of provided environment properties.
   *
   * @param providedEnvironmentProperties The list of provided environment
   * properties.
   */
  public void setProvidedEnvironmentProperties(Set<ExecutionEnvironmentProperty> providedEnvironmentProperties) {
    this.providedEnvironmentProperties = providedEnvironmentProperties;
  }

  /**
   * Add a provided environment property.
   *
   * @param pProperty The property to add.
   */
  public void addProvidedEnvironmentProperty(ExecutionEnvironmentProperty pProperty) {
    providedEnvironmentProperties.add(pProperty);
  }

  /**
   * Remove a provided environment property.
   *
   * @param pProperty The property to remove.
   */
  public void removeProvidedEnvironmentProperty(ExecutionEnvironmentProperty pProperty) {
    providedEnvironmentProperties.remove(pProperty);
  }
}
