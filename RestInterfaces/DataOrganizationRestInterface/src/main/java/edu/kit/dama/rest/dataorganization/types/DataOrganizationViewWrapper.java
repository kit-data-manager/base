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
package edu.kit.dama.rest.dataorganization.types;

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
 * @author dapp
 */
//two graphs doing the same. 'simple' was just added for convenience.
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "simple",
          attributeNodes = {
            @XmlNamedAttributeNode(value = "count"),
            @XmlNamedAttributeNode(value = "entities")
          }),
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode(value = "count"),
            @XmlNamedAttributeNode(value = "entities")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class DataOrganizationViewWrapper extends AbstractEntityWrapper<String> {

  @XmlElementWrapper
  @XmlElement(name = "entity",
          type = String.class,
          namespace
          = "http://ipe.kit.edu/2014/KITDataManager/DataOrganization")
  private List<String> entities;

  public DataOrganizationViewWrapper() {
    super();
  }

  public DataOrganizationViewWrapper(String... pEntities) {
    super(pEntities);
  }

  /**
   * Default constructor.
   *
   * @param pCount The number of modified rows.
   */
  public DataOrganizationViewWrapper(Integer pCount) {
    super(pCount);
  }

  public DataOrganizationViewWrapper(List<String> pEntities) {
    super(pEntities);
  }

  @Override
  public void setEntities(List<String> pEntities) {
    this.entities = pEntities;
  }

  @Override
  public List<String> getEntities() {
    return this.entities;
  }

}
