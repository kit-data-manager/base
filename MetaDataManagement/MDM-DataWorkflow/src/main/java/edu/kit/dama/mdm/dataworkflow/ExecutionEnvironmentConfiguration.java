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

import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultEnvironmentProperty;
import edu.kit.dama.mdm.dataworkflow.interfaces.IDefaultExecutionEnvironment;
import edu.kit.dama.mdm.dataworkflow.properties.ExecutionEnvironmentProperty;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FileUtils;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;
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
 * class of an AbstractExecutionEnvironmentHandler capable of submitting
 * DataWorkflow tasks using this configuration.
 *
 * @author mf6319
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
//                @XmlNamedAttributeNode("name"),
//                @XmlNamedAttributeNode("description"),
//                @XmlNamedAttributeNode("customProperties"),
//                @XmlNamedAttributeNode("groupId"),
//                @XmlNamedAttributeNode("handlerImplementationClass"),
//                @XmlNamedAttributeNode("stagingAccessPointId"),
//                @XmlNamedAttributeNode("accessPointLocalBasePath"),
//                @XmlNamedAttributeNode("maxParallelTasks"),
//                @XmlNamedAttributeNode("defaultEnvironment"),
//                @XmlNamedAttributeNode("disabled")
//            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "ExecutionEnvironmentConfiguration.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("uniqueIdentifier")
            }),
    @NamedEntityGraph(
            name = "ExecutionEnvironmentConfiguration.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("uniqueIdentifier"),
                @NamedAttributeNode("name"),
                @NamedAttributeNode("description"),
                @NamedAttributeNode("customProperties"),
                @NamedAttributeNode("groupId"),
                @NamedAttributeNode("handlerImplementationClass"),
                @NamedAttributeNode("stagingAccessPointId"),
                @NamedAttributeNode("accessPointLocalBasePath"),
                @NamedAttributeNode("maxParallelTasks"),
                @NamedAttributeNode("defaultEnvironment"),
                @NamedAttributeNode("disabled")}
    )
})
public class ExecutionEnvironmentConfiguration implements IDefaultExecutionEnvironment, FetchGroupTracker {

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

    @Column(nullable = false, unique = true)
    private String uniqueIdentifier = null;
    /**
     * Human readable name.
     */
    private String name = null;
    /**
     * Human readable description.
     */
    @Column(length = 10240)
    private String description = null;
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
     * contains the absolute file path where the staging location is reachable
     * for all computing nodes/processes, e.g. via mount point.
     */
    private String accessPointLocalBasePath = null;

    /**
     * The number of max. parallel DataWorkflow tasks running on this execution
     * environment at the same time in parallel.
     */
    private Integer maxParallelTasks = 10;
    /**
     * Flag which indicates if this environment is the default one.
     */
    private Boolean defaultEnvironment = false;

    /**
     * Flag which indicates if this environment is disabled or not.
     */
    private Boolean disabled = false;

    @OneToMany(fetch = FetchType.EAGER)
    private Set<ExecutionEnvironmentProperty> providedEnvironmentProperties = new HashSet<>();

    /**
     * Factory a new ExecutionEnvironmentConfiguration with the provided
     * identifier.
     *
     * @param pIdentifier The unique ExecutionEnvironmentConfiguration
     * identifier.
     *
     * @return The new ExecutionEnvironmentConfiguration.
     */
    public static ExecutionEnvironmentConfiguration factoryNewExecutionEnvironmentConfiguration(String pIdentifier) {
        if (pIdentifier == null) {
            throw new IllegalArgumentException("Argument 'pIdentifier' must not be 'null'");
        }
        ExecutionEnvironmentConfiguration result = new ExecutionEnvironmentConfiguration();
        result.setUniqueIdentifier(pIdentifier);
        return result;
    }

    /**
     * Factory a new ExecutionEnvironmentConfiguration with an auto-generated
     * identifier. The identifier is generated using
     * {@link java.util.UUID#randomUUID()}
     *
     * @return The new ExecutionEnvironmentConfiguration.
     */
    public static ExecutionEnvironmentConfiguration factoryNewExecutionEnvironmentConfiguration() {
        return factoryNewExecutionEnvironmentConfiguration(UUID.randomUUID().toString());
    }

    /**
     * Default constructor.
     */
    public ExecutionEnvironmentConfiguration() {
    }

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
     * provided task. This method internally maps to {@link #canExecute(edu.kit.dama.mdm.dataworkflow.DataWorkflowTaskConfiguration)
     * }
     * by using {@link DataWorkflowTask#getConfiguration() }.
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
     * DataWorkflowConfiguration. True is returned, if the environment provides
     * all required properties.
     *
     * @param pConfiguration The configuration to check.
     *
     * @return TRUE if all required properties are provided.
     */
    public boolean canExecute(DataWorkflowTaskConfiguration pConfiguration) {
        Set<? extends IDefaultEnvironmentProperty> required = pConfiguration.getRequiredEnvironmentProperties();
        boolean result = true;
        for (final IDefaultEnvironmentProperty property : required) {

            IDefaultEnvironmentProperty providedProperty = (IDefaultEnvironmentProperty) CollectionUtils.find(providedEnvironmentProperties, new Predicate() {
                @Override
                public boolean evaluate(Object o) {
                    return Long.compare(((IDefaultEnvironmentProperty) o).getId(), property.getId()) == 0;
                }
            });

            if (providedProperty == null) {
                //not applicable ... Log this
                LOGGER.error("Execution environment with id {} cannot handle task configuration with id {}. Property {} not supported.", getId(), pConfiguration.getId(), property);
                result = false;
                break;
            }
        }
        //all available
        return result;
    }

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

    @Override
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getCustomProperties() {
        return customProperties;
    }

    /**
     * @param customProperties the customProperties to set
     */
    public void setCustomProperties(String customProperties) {
        this.customProperties = customProperties;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId the groupId to set
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getHandlerImplementationClass() {
        return handlerImplementationClass;
    }

    /**
     * @param handlerImplementationClass the handlerImplementationClass to set
     */
    public void setHandlerImplementationClass(String handlerImplementationClass) {
        this.handlerImplementationClass = handlerImplementationClass;
    }

    @Override
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

    @Override
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

    @Override
    public Integer getMaxParallelTasks() {
        return this.maxParallelTasks;
    }

    /**
     * Set the number of max. parallel tasks running in this execution
     * environment.
     *
     * @param maxParallelTasks The number of max. parallel tasks.
     */
    public void setMaxParallelTasks(Integer maxParallelTasks) {
        this.maxParallelTasks = maxParallelTasks;
    }

    @Override
    public Boolean isDefaultEnvironment() {
        return defaultEnvironment;
    }

    /**
     * Set whether this ExecutionEnvironment is the default one for the group it
     * is associated with.
     *
     * @param defaultEnvironment TRUE = This is the default
     * ExecutionEnvironment.
     */
    public void setDefaultEnvironment(Boolean defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    @Override
    public Boolean isDisabled() {
        return disabled;
    }

    /**
     * Set whether this ExecutionEnvironment is disabled or not.
     *
     * @param disabled TRUE = This ExecutionEnvironment is disabled.
     */
    public void setDisabled(Boolean disabled) {
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

    /**
     * Remove all provided environment properties.
     */
    public void removeProvidedEnvironmentProperties() {
        providedEnvironmentProperties.clear();
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
