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
package edu.kit.dama.staging.entities;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.util.PropertiesUtil;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;

/**
 *
 * @author jejkal
 */
@Entity
@XmlNamedObjectGraph(name = "simple", attributeNodes = {
  @XmlNamedAttributeNode("id")})
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StagingProcessor {

  /**
   *
   */
  public enum PROCESSOR_TYPE {

    /**
     * Processor that is executed only on the client side by a transfer client
     * capable of executing StagingProcessors, e.g. to check data before
     * transfer.
     */
    CLIENT_SIDE_ONLY,
    /**
     * Processor that is executed only on the server side, e.g. to extract
     * metadata.
     */
    SERVER_SIDE_ONLY,
    /**
     * Processor that is executed on the client side by a transfer client
     * capable of executing StagingProcessors and on the server side, e.g.
     * compare data before and after the transfer.
     */
    CLIENT_AND_SERVER_SIDE,
    /**
     * Processor that is executed on the server side after archiving took place.
     * These special processors can be used if e.g. an existing data
     * organization is needed or the final destination of files must be known.
     */
    POST_ARCHIVING;
  }
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String implementationClass;
  @Column(length = 1024)
  private String properties;
  @Column(nullable = false, unique = true)
  private String uniqueIdentifier;
  private String groupId = null;
  @Enumerated(EnumType.STRING)
  private PROCESSOR_TYPE type;
  @Column(length = 1024)
  private String description;
  private boolean defaultOn = false;
  private boolean disabled = false;

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
   * identifier is generated using {@link UUID.randomUUID().toString()}
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

  /**
   * Get the id.
   *
   * @return The id.
   */
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

  /**
   * Get the name.
   *
   * @return The name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the type.
   *
   * @param type The type.
   */
  public void setType(PROCESSOR_TYPE type) {
    this.type = type;
  }

  /**
   * Get the type.
   *
   * @return The type.
   */
  public PROCESSOR_TYPE getType() {
    return type;
  }

  /**
   * Set the unique identifier.
   *
   * @param uniqueIdentifier The unique identifier.
   */
  public void setUniqueIdentifier(String uniqueIdentifier) {
    this.uniqueIdentifier = uniqueIdentifier;
  }

  /**
   * Get the unique identifier.
   *
   * @return The unique identifier.
   */
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

  /**
   * Set the description.
   *
   * @return The description.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Set the implementation class.
   *
   * @param implementationClass The implementation class.
   */
  public void setImplementationClass(String implementationClass) {
    this.implementationClass = implementationClass;
  }

  /**
   * Get the implementation class.
   *
   * @return The implementation class.
   */
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

  /**
   * Get the properties.
   *
   * @return The properties.
   */
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

  /**
   * Get the group id this processor is associated with.
   *
   * @return The group id this processor is associated with.
   */
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

  /**
   * Check if this processor is selected by default.
   *
   * @return TRUE = This processor is selected by default.
   */
  public boolean isDefaultOn() {
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

  /**
   * Check if this processor is enabled/disabled.
   *
   * @return TRUE = This processor is disabled.
   */
  public boolean isDisabled() {
    return disabled;
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
}
