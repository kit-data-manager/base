/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
package edu.kit.dama.rest.admin.types;

import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.rest.base.AbstractEntityWrapper;
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
public final class UserGroupWrapper extends AbstractEntityWrapper<UserGroup> {

  @XmlElementWrapper
  @XmlElement(name = "entity")
  private List<UserGroup> entities;

  /**
   * Default constructor.
   */
  public UserGroupWrapper() {
    super();
  }

  /**
   * Default constructor.
   *
   * @param pEntities An array of entities.
   */
  public UserGroupWrapper(UserGroup... pEntities) {
    super(pEntities);
  }

  /**
   * Default constructor.
   *
   * @param pEntities A list of entities.
   */
  public UserGroupWrapper(List<UserGroup> pEntities) {
    super(pEntities);
  }

  /**
   * Default constructor.
   *
   * @param pCount The number of affected entities.
   */
  public UserGroupWrapper(Integer pCount) {
    super(pCount);
  }

  @Override
  public void setEntities(List<UserGroup> pEntities) {
    this.entities = pEntities;
  }

  @Override
  public List<UserGroup> getEntities() {
    return entities;
  }
}
