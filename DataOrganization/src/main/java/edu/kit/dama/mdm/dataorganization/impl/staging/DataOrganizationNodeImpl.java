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
package edu.kit.dama.mdm.dataorganization.impl.staging;

import edu.kit.dama.commons.types.ILFN;
import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 *
 * @author jejkal
 */
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("nodeId")
                ,
                @XmlNamedAttributeNode("name")
            })
    ,
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("nodeId")
                ,
                @XmlNamedAttributeNode("name")
                ,
                @XmlNamedAttributeNode("description")
                ,
                @XmlNamedAttributeNode("logicalFileName")
                ,
                @XmlNamedAttributeNode(value = "attributes", subgraph = "default")
                ,
                @XmlNamedAttributeNode(value = "children", subgraph = "simple")
            })

})
@XmlAccessorType(XmlAccessType.FIELD)
public class DataOrganizationNodeImpl implements IDefaultDataOrganizationNode, ISelectable {

    private Long nodeId;
    private String name;
    private String viewName;
    @XmlElementWrapper
    @XmlElement(name = "attribute", type = AttributeImpl.class)
    private Set<IAttribute> attributes;
    private String description;
    @XmlTransient
    private ICollectionNode parent;
    @XmlTransient
    private boolean selected = false;

    /**
     * Default costructor.
     */
    public DataOrganizationNodeImpl() {
    }

    /**
     * Set the node id.
     *
     * @param nodeId The node id.
     */
    public final void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public final Long getNodeId() {
        return nodeId;
    }

    @Override
    final public String getDescription() {
        return description;
    }

    @Override
    final public void setDescription(String description) {
        this.description = description;
    }

    @Override
    final public ICollectionNode getParent() {
        return parent;
    }

    @Override
    final public void setParent(ICollectionNode parent) {
        this.parent = parent;
    }

    @Override
    final public String getName() {
        return name;
    }

    @Override
    final public void setName(String name) {
        this.name = name;
    }

    @Override
    final public Set<IAttribute> getAttributes() {
        if (null == attributes) {
            attributes = new HashSet<>();
        }
        return attributes;
    }

    @Override
    final public void setAttributes(Set<? extends IAttribute> attributes) {
        Set<IAttribute> myAttributes = getAttributes();
        myAttributes.clear();
        if (attributes != null) {
            for (IAttribute attr : attributes) {
                myAttributes.add(attr);
            }
        }
    }

    @Override
    final public void addAttribute(IAttribute attribute) {
        getAttributes().add(attribute);
    }

    @Override
    public NodeId getTransientNodeId() {
        if (nodeId != null) {
            return new NodeId(null, nodeId, 1, getViewName());
        } else {
            //if nodeId is null, the implicit conversion the the primitive used by NodeId() will fail.
            return null;
        }
    }

    @Override
    public IDataOrganizationNode clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public String toString() {
        return getName() + " (" + hashCode() + ")";
    }

    @Override
    public Boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(Boolean pValue) {
        selected = pValue;
    }

    @Override
    public String getViewName() {
        return viewName;
    }

    @Override
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @Override
    @XmlElementWrapper
    @XmlElement(name = "child", type = DataOrganizationNodeImpl.class)
    public List<? extends IDataOrganizationNode> getChildren() {
        if (this instanceof CollectionNodeImpl) {
            return ((CollectionNodeImpl) this).getChildren();
        }
        return null;
    }

    public void setChildren(List<? extends IDataOrganizationNode> children) {
        //not supported
    }

    public void setLogicalFileName() {
        //not supported
    }

    @Override
    @XmlElement(name = "logicalFileName", type = LFNImpl.class)
    public ILFN getLogicalFileName() {
        if (this instanceof FileNodeImpl) {
            return ((FileNodeImpl) this).getLogicalFileName();
        }
        return null;
    }
}
