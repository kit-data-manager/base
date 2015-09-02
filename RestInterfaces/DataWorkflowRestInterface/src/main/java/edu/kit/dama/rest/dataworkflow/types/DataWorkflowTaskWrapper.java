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
import edu.kit.dama.mdm.dataworkflow.DataWorkflowTask;
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
public final class DataWorkflowTaskWrapper extends AbstractEntityWrapper<DataWorkflowTask> {

  @XmlElementWrapper
  @XmlElement(name = "entity", type = DataWorkflowTask.class, namespace = "http://ipe.kit.edu/2014/KITDataManager/DataWorkflow")
  private List<DataWorkflowTask> entities;

  /**
   * Default constructor.
   */
  public DataWorkflowTaskWrapper() {
    super();
  }

  /**
   * Default constructor.
   *
   * @param pEntities An array of wrapped entities.
   */
  public DataWorkflowTaskWrapper(DataWorkflowTask... pEntities) {
    super(pEntities);
  }

  /**
   * Default constructor.
   *
   * @param pEntities A list of wrapped entities.
   */
  public DataWorkflowTaskWrapper(List<DataWorkflowTask> pEntities) {
    super(pEntities);
  }

  /**
   * Default constructor.
   *
   * @param pCount The number of modified rows.
   */
  public DataWorkflowTaskWrapper(Integer pCount) {
    super(pCount);
  }

  @Override
  public void setEntities(List<DataWorkflowTask> pEntities) {
    entities = pEntities;
  }

  @Override
  public List<DataWorkflowTask> getEntities() {
    return entities;
  }
}
