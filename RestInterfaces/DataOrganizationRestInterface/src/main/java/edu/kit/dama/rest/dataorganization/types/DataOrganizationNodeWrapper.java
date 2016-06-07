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
package edu.kit.dama.rest.dataorganization.types;

import edu.kit.dama.rest.base.AbstractEntityWrapper;
import edu.kit.dama.mdm.dataorganization.impl.staging.DataOrganizationNodeImpl;
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
public final class DataOrganizationNodeWrapper extends AbstractEntityWrapper<DataOrganizationNodeImpl> {

    //@XmlElementWrapper
    // @XmlElement(name = "entity", type = DataOrganizationNodeImpl.class, namespace = "http://ipe.kit.edu/2014/KITDataManager/DataOrganization")
    private List<DataOrganizationNodeImpl> entities;

    /**
     * Default constructor.
     */
    public DataOrganizationNodeWrapper() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param pEntities An array of wrapped entities.
     */
    public DataOrganizationNodeWrapper(DataOrganizationNodeImpl... pEntities) {
        super(pEntities);
    }

    /**
     * Default constructor.
     *
     * @param pEntities A list of wrapped entities.
     */
    public DataOrganizationNodeWrapper(List<DataOrganizationNodeImpl> pEntities) {
        super(pEntities);
    }

    /**
     * Default constructor.
     *
     * @param pCount The number of modified rows.
     */
    public DataOrganizationNodeWrapper(Integer pCount) {
        super(pCount);
    }

    @Override
    public void setEntities(List<DataOrganizationNodeImpl> pEntities) {
        entities = pEntities;
    }

    @Override
    public List<DataOrganizationNodeImpl> getEntities() {
        return entities;
    }
}
