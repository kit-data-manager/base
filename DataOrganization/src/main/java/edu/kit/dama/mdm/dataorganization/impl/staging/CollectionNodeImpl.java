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
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("nodeId"),
                @XmlNamedAttributeNode("name"),
                @XmlNamedAttributeNode("description"),
                @XmlNamedAttributeNode(value = "parent", subgraph = "simple"),
                @XmlNamedAttributeNode(value = "children", subgraph = "simple"),
                @XmlNamedAttributeNode(value = "attributes", subgraph = "default")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionNodeImpl extends DataOrganizationNodeImpl implements ICollectionNode, ISelectable {

    @XmlElementWrapper
    @XmlElement(name = "child", type = DataOrganizationNodeImpl.class)
    private List<IDataOrganizationNode> children;
    @XmlElement(name = "lfn", type = LFNImpl.class)
    private ILFN logicalFileName;

    /**
     * Default constructor.
     */
    public CollectionNodeImpl() {
    }

    /**
     * Default constructor.
     *
     * @param pLfn The LFN.
     */
    public CollectionNodeImpl(ILFN pLfn) {
        setLogicalFileName(pLfn);
    }

    @Override
    public final String toString() {
        return "(C) " + getName() + " (" + hashCode() + ")";
    }

    @Override
    public ILFN getLogicalFileName() {
        return logicalFileName;
    }

    public void setLogicalFileName(ILFN logicalFileName) {
        this.logicalFileName = logicalFileName;
    }

    @Override
    public final List<? extends IDataOrganizationNode> getChildren() {
        if (null == children) {
            children = new ArrayList<>();
        }
        return children;
    }

    /**
     * Add a new child to this node.
     *
     * @param child The new child.
     * @param pOverwrite Overwrite the child if it already exists.
     */
    public final void addChild(final IDataOrganizationNode child, boolean pOverwrite) {
        IDataOrganizationNode result;

        if (pOverwrite) {
            result = (IDataOrganizationNode) CollectionUtils.find(children, new Predicate() {
                @Override
                public boolean evaluate(Object o) {
                    IDataOrganizationNode node = (IDataOrganizationNode) o;
                    if (o instanceof ICollectionNode && child instanceof IFileNode) {
                        return false;
                    } else if (o instanceof IFileNode && child instanceof ICollectionNode) {
                        return false;
                    } else {//node types are equal...check them
                        if (node.getName() != null) {
                            return node.getName().equals(child.getName());
                        } else if (child.getName() != null) {
                            return child.getName().equals(node.getName());
                        }
                        //both are null
                        return true;
                    }
                }
            });
            if (children == null) {
                children = new ArrayList<>();
            }
            children.remove(result);
        }

        addChild(child);
    }

    /**
     * Remove a child.
     *
     * @param child The child to remove.
     */
    public final void removeChild(IDataOrganizationNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.remove(child);
    }

    @Override
    public final void addChild(final IDataOrganizationNode child) {
        if (child == null) {
            throw new IllegalArgumentException("Argument 'child' must not be null");
        }

        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        child.setParent(this);
    }

    @Override
    public final void addChildren(List<? extends IDataOrganizationNode> children) {
        for (IDataOrganizationNode child : children) {
            addChild(child);
        }
    }

    @Override
    public final void setChildren(List<? extends IDataOrganizationNode> children) {
        this.children.clear();
        for (IDataOrganizationNode don : children) {
            this.children.add(don);
        }
    }
}
