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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.dataorganization.entity.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import java.util.Set;
import javax.xml.bind.annotation.XmlTransient;

/**
 * The IDataOrganizationNode interface is the most important part of the package
 * and holds the common properties and functionalities of the interfaces
 * {@link ICollectionNode} and {@link IFileNode}. IDataOrganizationNodes are
 * organized in tree structures. These trees mimic folder-file trees well known
 * from the file-systems but with some differences and in a more abstract way.
 * Therefore, each IDataOrganizationNode instance holds a reference to its
 * parent in the parent property (which is null), has a name, can have a
 * full-text description and is related to a set of {@link IAttribute}s which
 * can be used to store for example additional meta-data, caching information,
 * storage policies.
 *
 * @author pasic
 */
public interface IDataOrganizationNode extends Cloneable {

    /**
     * Gets the full-text description of the particular node. These description
     * holds no semantics from the system point of view. It is allowed to offer
     * a search mechanism which relies on its content, but the application site
     * interpretation is discouraged.
     *
     * @return description
     */
    String getDescription();

    /**
     * Sets the full-text description of the particular node. These description
     * holds no semantics from the system point of view. It is allowed to offer
     * a search mechanism which relies on its content, but the application site
     * interpretation is discouraged.
     *
     * @param description The description to set.
     */
    void setDescription(String description);

    /**
     * Gets the reference to the parent node in the tree or null if there is no
     * parent.
     *
     * @return The parent node.
     */
    @XmlTransient
    @JsonIgnore
    ICollectionNode getParent();

    /**
     * Sets the reference to the parent node in the tree. Use null to express
     * that there is no parent node.
     *
     * @param parent The parent node.
     */
    @XmlTransient
    @JsonIgnore
    void setParent(ICollectionNode parent);

    /**
     * Gets the name of the node.
     *
     * @return The name.
     */
    String getName();

    /**
     * Sets the name of the node.
     *
     * @param name The node name.
     */
    void setName(String name);

    /**
     * Gets all attributes associated with the node.
     *
     * @return All attributes.
     */
    Set<? extends IAttribute> getAttributes();

    /**
     * Sets a set of attributes for the node.
     *
     * @param attributes The attributes.
     */
    void setAttributes(Set<? extends IAttribute> attributes);

    /**
     * Adds an attribute.
     *
     * @param attribute The attribute.
     */
    void addAttribute(IAttribute attribute);

    /**
     * Gets a transient (non-persistent) identifier of the node.
     *
     * @return The transient node id.
     */
    @XmlTransient
    @JsonIgnore
    NodeId getTransientNodeId();

    /**
     * Clone this node.
     *
     * @return A deep copy of this node.
     *
     * @throws CloneNotSupportedException Clone is not supported.
     */
    IDataOrganizationNode clone() throws CloneNotSupportedException;

    /**
     * Get the view name this node is associated with.
     *
     * @return The view name.
     */
    String getViewName();

    /**
     * Set the view name this node is associated with.
     *
     * @param viewName The view name.
     */
    void setViewName(String viewName);
}
