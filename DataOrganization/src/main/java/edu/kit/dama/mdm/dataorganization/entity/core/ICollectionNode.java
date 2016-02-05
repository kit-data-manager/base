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

import java.util.List;

/**
 * ICollectionNode is the kind of {@link IDataOrganizationNode} which is meant
 * to express the structural organization of the data. Each
 * {@link IDataOrganizationNode} can instance have one parent node but only
 * ICollectionNode instances can have a children nodes. That means data
 * organization nodes are in a tree structure where the inner nodes implement
 * the ICollectionNode interface. In the term of the file-system analogy
 * collection nodes are folders or directories.
 *
 * @author pasic
 */
public interface ICollectionNode extends IDataOrganizationNode {

    /**
     * Gets a reference on the list of all children nodes of this node. The
     * order of the nodes is preserved during the object life cycle but their
     * type just on the interface level, <b>implementations of the interface
     * might change</b>.
     *
     * @return list of children.
     */
    List<? extends IDataOrganizationNode> getChildren();

    /**
     * Sets the list of all children nodes. <b>The other side of the
     * relationship is not maintained. The type is just preserved on the
     * interface level, implementations of the interface might change. </b>
     *
     * @param children Set a list of children.
     */
    void setChildren(List<? extends IDataOrganizationNode> children);

    /**
     * Adds a new child to this node. Manages both sides of the relationship.
     *
     * @param child Add a child.
     */
    void addChild(IDataOrganizationNode child);

    /**
     * Adds a list of new child to this node. Manages both sides of the
     * relationship.
     *
     * @param children The list of children.
     */
    void addChildren(List<IDataOrganizationNode> children);
}
