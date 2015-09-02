/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.rest.dataworkflow.types;

import edu.kit.dama.rest.base.AbstractEntityWrapper;
import edu.kit.dama.mdm.dataworkflow.ExecutionEnvironmentConfiguration;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 *
 * @author mf6319
 */
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "simple",
          attributeNodes = {
            @XmlNamedAttributeNode(value = "count"),
            @XmlNamedAttributeNode(value = "entities", subgraph = "simple")
          }),
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode(value = "count"),
            @XmlNamedAttributeNode(value = "entities", subgraph = "default")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public final class ExecutionEnvironmentConfigurationWrapper extends AbstractEntityWrapper<ExecutionEnvironmentConfiguration> {

  @XmlElementWrapper
  @XmlElement(name = "entity", type = ExecutionEnvironmentConfiguration.class, namespace = "http://ipe.kit.edu/2014/KITDataManager/DataWorkflow")
  private List<ExecutionEnvironmentConfiguration> entities;

  /**
   * Default constructor.
   */
  public ExecutionEnvironmentConfigurationWrapper() {
    super();
  }

  /**
   * Default constructor.
   *
   * @param pEntities An array of wrapped entities.
   */
  public ExecutionEnvironmentConfigurationWrapper(ExecutionEnvironmentConfiguration... pEntities) {
    super(pEntities);
  }

  /**
   * Default constructor.
   *
   * @param pEntities A list of wrapped entities.
   */
  public ExecutionEnvironmentConfigurationWrapper(List<ExecutionEnvironmentConfiguration> pEntities) {
    super(pEntities);
  }

  /**
   * Default constructor.
   *
   * @param pCount The number of modified rows.
   */
  public ExecutionEnvironmentConfigurationWrapper(Integer pCount) {
    super(pCount);
  }

  @Override
  public void setEntities(List<ExecutionEnvironmentConfiguration> pEntities) {
    entities = pEntities;
  }

  @Override
  public List<ExecutionEnvironmentConfiguration> getEntities() {
    return entities;
  }
}
