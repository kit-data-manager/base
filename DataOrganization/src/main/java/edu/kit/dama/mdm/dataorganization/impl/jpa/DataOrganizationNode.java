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
package edu.kit.dama.mdm.dataorganization.impl.jpa;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.service.exception.DataOrganizationError;
import edu.kit.dama.util.Constants;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;

/**
 *
 * @author pasic
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@IdClass(DataOrganizationNodeId.class)
public class DataOrganizationNode implements IDataOrganizationNode, Serializable,
        Cloneable {

    private static final long serialVersionUID = 7526472295622776127L;

    /*
     */
    @Id
    @Column(name = "VIEWNAME", nullable = false)
    private String viewName = Constants.DEFAULT_VIEW;

    @Id
    @Column(name = "DIGIT_OBJ_ID")
    private String digitalObjectIDStr;

    private String name;

    @OneToMany(mappedBy = "annotatedNode", cascade = {CascadeType.ALL})
    @BatchFetch(BatchFetchType.EXISTS)
    private Set<Attribute> attributes;
    @Transient
    private CollectionNode parent;
    private String description;

    @Id
    @Column(name = "STEPNOARRIVED")
    private Long stepNoArrived;
    private Long stepNoLeaved;
    private int nodeDepth;
    private int idVersion;

    /**
     * Default constructor.
     */
    public DataOrganizationNode() {
    }

    /**
     * Constructor which copies only the properies of a node but not the
     * relationships (parent, children). Also the digitalObjectId is not copied
     * as there is no guarantee that there is this id for the provided node.
     *
     * @param other The other node.
     */
    public DataOrganizationNode(IDataOrganizationNode other) {
        name = other.getName();
        description = other.getDescription();
        //Attributes
        attributes = new HashSet<Attribute>();
        for (IAttribute attr : other.getAttributes()) {
            addAttribute(attr);
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public ICollectionNode getParent() {
        return parent;
    }

    @Override
    public void setParent(ICollectionNode parent) {
        this.parent = new CollectionNode(parent);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Set<Attribute> getAttributes() {
        if (null == attributes) {
            attributes = new HashSet<>();
        }
        return attributes;
    }

    @Override
    public void setAttributes(Set<? extends IAttribute> attributes) {
        Set<Attribute> myAttributes = getAttributes();
        myAttributes.clear();
        for (IAttribute iattr : attributes) {
            addAttribute(iattr);//myAttributes.add(new Attribute(iattr));
        }
    }

    @Override
    public final void addAttribute(IAttribute attribute) {
        Attribute attrImpl = new Attribute(attribute);
        attrImpl.setAnnotatedNode(this);
        getAttributes().add(attrImpl);
    }

    /**
     * Set the digital objcet id string.
     *
     * @param digitalObjectID The digital object id string.
     */
    public void setDigitalObjectIDStr(String digitalObjectID) {
        this.digitalObjectIDStr = digitalObjectID;
    }

    /**
     * Get the digital objcet id string.
     *
     * @return The digital object id string.
     */
    public String getDigitalObjectIDStr() {
        return this.digitalObjectIDStr;
    }

    /**
     * Gets the stepNoArrived property which indicates the number of steps
     * performed before arrived (1.time) to this node during a modified
     * pre-order walk of the tree.
     *
     * @return The arrived step number.
     */
    public Long getStepNoArrived() {
        return stepNoArrived;
    }

    /**
     * Sets the stepNoArrived.
     *
     * @param stepNoArrived The arrived step number.
     */
    public void setStepNoArrived(Long stepNoArrived) {
        this.stepNoArrived = stepNoArrived;
    }

    /**
     * Gets the stepNoArrived property which indicates the number of steps
     * performed before leaved (2. time) this node during a modified pre-order
     * walk of the tree.
     *
     * @return The left step number.
     */
    public Long getStepNoLeaved() {
        return stepNoLeaved;
    }

    /**
     * Sets the stepNoLeaved
     *
     * @param stepNoLeaved The left step number.
     */
    public void setStepNoLeaved(Long stepNoLeaved) {
        this.stepNoLeaved = stepNoLeaved;
    }

    /**
     * Get the node id version.
     *
     * @return The id version.
     */
    public int getIdVersion() {
        return idVersion;
    }

    /**
     * Set the node id version.
     *
     * @param idVersion The id version.
     */
    public void setIdVersion(int idVersion) {
        this.idVersion = idVersion;
    }

    /**
     * Get the node depth.
     *
     * @return The node depth.
     */
    public int getNodeDepth() {
        return nodeDepth;
    }

    /**
     * Set the node depth.
     *
     * @param nodeDepth The node depth.
     */
    public void setNodeDepth(int nodeDepth) {
        this.nodeDepth = nodeDepth;
    }

    @Override
    public IDataOrganizationNode clone() throws CloneNotSupportedException {
        DataOrganizationNode don = null;
        if (this instanceof ICollectionNode) {
            don = new CollectionNode();
        } else if (this instanceof IFileNode) {
            don = new FileNode();
        }
        if (null == don) {
            throw new DataOrganizationError(
                    "node:IDataOrganizationNode should be eighter"
                    + " instance of IFileNoder or instance of ICollectionNode");
        }
        don.setDescription(description);
        don.setName(name);
//        don.digitalObjectID = new DigitalObjectId();
//        don.digitalObjectID.setId(digitalObjectID.getId());
        Set<Attribute> attributes1 = don.getAttributes();
        for (Attribute attr : attributes) {
            attributes1.add(attr.clone());
        }
        return don;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IDataOrganizationNode) {
            if (this == obj) {
                return true;
            }
            IDataOrganizationNode other = (IDataOrganizationNode) obj;
            if ( //                null == digitalObjectID? null == other.getDigitalObjectID():
                    //                digitalObjectID.equals(other.getDigitalObjectID())
                    //                &&
                    null == description ? null == other.getDescription()
                            : description.equals(other.getDescription())
                            && null == name ? null == other.getName()
                                    : name.equals(other.getName())) {
                return getAttributes().equals(other.getAttributes());
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + (this.digitalObjectIDStr != null
                ? this.digitalObjectIDStr.hashCode() : 0);
        hash = 31 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 31 * hash
                + (this.attributes != null ? this.attributes.hashCode() : 0);
        hash = 31 * hash + (this.parent != null ? this.parent.hashCode() : 0);
        hash = 31 * hash + (this.description != null ? this.description.
                hashCode() : 0);
        hash = 31 * hash + (this.stepNoArrived != null ? this.stepNoArrived.
                hashCode() : 0);
        hash = 31 * hash + (this.stepNoLeaved != null ? this.stepNoLeaved.
                hashCode() : 0);
        hash = 31 * hash + this.nodeDepth;
        hash = 31 * hash + this.idVersion;
        return hash;
    }

    @Override
    public NodeId getTransientNodeId() {
        DigitalObjectId digitalObjectID = (digitalObjectIDStr == null) ? null : new DigitalObjectId(digitalObjectIDStr);
        return new NodeId(digitalObjectID, stepNoArrived, idVersion, viewName);
    }

    @Override
    public void setViewName(String viewName) {
        if (viewName != null) {
            this.viewName = viewName;
        }
    }

    @Override
    public String getViewName() {
        return viewName;
    }
}
