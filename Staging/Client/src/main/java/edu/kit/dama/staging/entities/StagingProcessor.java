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
package edu.kit.dama.staging.entities;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.entities.interfaces.IDefaultStagingProcessor;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Properties;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @author jejkal
 */
@Entity
//@XmlNamedObjectGraph(name = "simple", attributeNodes = {
//    @XmlNamedAttributeNode("id")})
//@NamedEntityGraphs({
//    @NamedEntityGraph(
//            name = "StagingProcessor.simple",
//            includeAllAttributes = false,
//            attributeNodes = {
//                @NamedAttributeNode("id"),
//                @NamedAttributeNode("uniqueIdentifier")}),
//    @NamedEntityGraph(
//            name = "StagingProcessor.default",
//            includeAllAttributes = false,
//            attributeNodes = {
//                @NamedAttributeNode("id"),
//                @NamedAttributeNode("uniqueIdentifier"),
//                @NamedAttributeNode("name"),
//                @NamedAttributeNode("description"),
//                @NamedAttributeNode("implementationClass"),
//                @NamedAttributeNode("groupId"),
//                @NamedAttributeNode("type"),
//                @NamedAttributeNode("defaultOn"),
//                @NamedAttributeNode("disabled"),
//                @NamedAttributeNode("ingestProcessingSupported"),
//                @NamedAttributeNode("downloadProcessingSupported"),})
//})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StagingProcessor implements IDefaultStagingProcessor, FetchGroupTracker, Serializable {

//    public enum PROCESSOR_TYPE {
//
//        /**
//         * Processor that is executed only on the client side by a transfer
//         * client capable of executing StagingProcessors, e.g. to check data
//         * before transfer.
//         */
//        CLIENT_SIDE_ONLY,
//        /**
//         * Processor that is executed only on the server side, e.g. to extract
//         * metadata. Executing these processors takes place before archiving,
//         * therefor, NO access to the DataOrganization is possible for
//         * SERVER_SIDE_ONLY processors.
//         */
//        SERVER_SIDE_ONLY,
//        /**
//         * Processor that is executed on the client side by a transfer client
//         * capable of executing StagingProcessors and on the server side, e.g.
//         * compare data before and after the transfer. Executing these
//         * processors takes place before archiving, therefor, NO access to the
//         * DataOrganization is possible for CLIENT_AND_SERVER_SIDE processors.
//         */
//        CLIENT_AND_SERVER_SIDE,
//        /**
//         * Processor that is executed on the server side bewfore and after
//         * archiving took place. These special processors can be used if e.g. an
//         * existing DataOrganization is needed or the final destination of files
//         * must be known.
//         */
//        POST_ARCHIVING;
//
//        public boolean isServerSideProcessor() {
//            return !CLIENT_SIDE_ONLY.equals(this);
//        }
//    }
    public final static Comparator<StagingProcessor> DEFAULT_PRIORITY_COMPARATOR = new Comparator<StagingProcessor>() {
        @Override
        public int compare(StagingProcessor o2, StagingProcessor o1) {
            return Byte.compare((o1.getPriority() != null) ? o1.getPriority() : 0, (o2.getPriority() != null) ? o2.getPriority() : 0);
        }
    };

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String uniqueIdentifier;
    private String name;
    private String implementationClass;
    @Column(length = 1024)
    private String properties;
    private String groupId = null;
//    @Enumerated(EnumType.STRING)
//    private PROCESSOR_TYPE type;
    @Column(length = 1024)
    private String description;
    private Byte priority = 0;
    private Boolean defaultOn = false;
    private Boolean disabled = false;
    private Boolean ingestProcessingSupported = false;
    private Boolean downloadProcessingSupported = false;

    /**
     * Factory a new staging processor with the provided identifier.
     *
     * @param pIdentifier The unique staging processor identifier.
     *
     * @return The new staging processor .
     */
    public static StagingProcessor factoryNewStagingProcessor(String pIdentifier) {
        if (pIdentifier == null) {
            throw new IllegalArgumentException("Argument 'pIdentifier' must not be 'null'");
        }
        StagingProcessor result = new StagingProcessor();
        result.setUniqueIdentifier(pIdentifier);
        return result;
    }

    /**
     * Factory a new staging processor with an auto-generated identifier. The
     * identifier is generated using {@link java.util.UUID#randomUUID()}
     *
     * @return The new staging processor .
     */
    public static StagingProcessor factoryNewStagingProcessor() {
        return factoryNewStagingProcessor(UUID.randomUUID().toString());
    }

    /**
     * Default constructor.
     */
    public StagingProcessor() {
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
     * Set the name.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

//    /**
//     * Set the type.
//     *
//     * @param type The type.
//     */
//    public void setType(PROCESSOR_TYPE type) {
//        this.type = type;
//    }
//
//    @Override
//    public PROCESSOR_TYPE getType() {
//        return type;
//    }
    /**
     * Set the unique identifier.
     *
     * @param uniqueIdentifier The unique identifier.
     */
    public void setUniqueIdentifier(String uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public String getUniqueIdentifier() {
        return uniqueIdentifier;
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

    @Override
    public Byte getPriority() {
        return priority;
    }

    /**
     * Set the priority.
     *
     * @param priority The priority.
     */
    public void setPriority(Byte priority) {
        this.priority = priority;
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
     * Set the properties.
     *
     * @param properties The properties.
     */
    public void setProperties(String properties) {
        this.properties = properties;
    }

    /**
     * Set the properties from an object.
     *
     * @param pProperties The properties object.
     *
     * @throws IOException If the serialization fails.
     */
    public void setPropertiesFromObject(Properties pProperties) throws IOException {
        this.properties = PropertiesUtil.propertiesToString(pProperties);
    }

    /**
     * Get the properties from an object.
     *
     * @return The properties object.
     *
     * @throws IOException If the deserialization fails.
     */
    public Properties getPropertiesAsObject() throws IOException {
        return PropertiesUtil.propertiesFromString(properties);
    }

    @Override
    public String getProperties() {
        return properties;
    }

    /**
     * Set the group id this processor is associated with.
     *
     * @param groupId The group id this processor is associated with.
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    /**
     * Set this processor to be selected by default.
     *
     * @param defaultOn TRUE = This processor is selected by default.
     */
    public void setDefaultOn(boolean defaultOn) {
        this.defaultOn = defaultOn;
    }

    @Override
    public Boolean isDefaultOn() {
        return defaultOn;
    }

    /**
     * Enable/disable this processor.
     *
     * @param disabled TRUE = This processor is disabled.
     */
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public Boolean isDisabled() {
        return disabled;
    }

    /**
     * Set whether this processor supports processing downloads or not.
     *
     * @param pValue TRUE = This processor supports processing downloads.
     */
    public void setDownloadProcessingSupported(boolean pValue) {
        downloadProcessingSupported = pValue;
    }

    @Override
    public Boolean isDownloadProcessingSupported() {
        return downloadProcessingSupported;
    }

    /**
     * Set whether this processor supports processing ingests or not.
     *
     * @param pValue TRUE = This processor supports processing ingests.
     */
    public void setIngestProcessingSupported(boolean pValue) {
        ingestProcessingSupported = pValue;
    }

    @Override
    public Boolean isIngestProcessingSupported() {
        return ingestProcessingSupported;
    }

    /**
     * Create an instance of this processor.
     *
     * @return An instance of this processor.
     *
     * @throws ConfigurationException If the instantiation failed.
     */
    public AbstractStagingProcessor createInstance() throws ConfigurationException {
        try {
            //instantiate processor
            AbstractStagingProcessor processor = (AbstractStagingProcessor) Class.forName(getImplementationClass()).getConstructor(String.class).newInstance(getUniqueIdentifier());
            //configure the processor using the stored properties
            processor.configure(getPropertiesAsObject());
            return processor;
        } catch (ClassNotFoundException ex) {
            throw new ConfigurationException("Staging Processor class not found.", ex);
        } catch (InstantiationException ex) {
            throw new ConfigurationException("Staging Processor class cannot be instantiated.", ex);
        } catch (IllegalAccessException ex) {
            throw new ConfigurationException("Default constructor of Staging Processor not accessible.", ex);
        } catch (ClassCastException ex) {
            throw new ConfigurationException("Provided implementation class is no instance of StagingProcessor.", ex);
        } catch (IOException ex) {
            throw new ConfigurationException("failed to read properties due to an IOException", ex);
        } catch (NoSuchMethodException ex) {
            throw new ConfigurationException("Provided implementation class has no constructor with argument String.class.", ex);
        } catch (InvocationTargetException ex) {
            throw new ConfigurationException("Failed to invoke constructor with argument String.class of provided implementation.", ex);
        } catch (PropertyValidationException ex) {
            throw new ConfigurationException("Failed to validate properties of StagingProcessor.", ex);
        }
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
