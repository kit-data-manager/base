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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.dataorganization.entity.core;

import edu.kit.dama.commons.types.DigitalObjectId;

/**
 * An <b>IFileTree</b> instance represents a whole tree of
 * {@link IDataOrganizationNode} instances which describes the data-part of a
 * digital object. IDataOrganizationNodes are organized in tree structures.
 * These trees mimic folder-file trees well known from the file-systems but with
 * some differences and in a more abstract way. Each IFileTree is associated
 * with a digital object from the MetaMataManagement trough the digitalObjectID
 * property. You should begin the traversal of the tree getting the rootNode
 * property which is a reference to a special ICollectionNode. This
 * ICollectionNode is just special in the sense that it should be interpreted in
 * a different way than normal ICollectionNodes: the name is irrelevant and
 * attributes are of global meaning.
 *
 * @author pasic
 */
public interface IFileTree extends Cloneable {

    /**
     * Gets the digital object identifier.
     *
     * @see MetaDataService
     *
     * @return The digital object identifier.
     */
    DigitalObjectId getDigitalObjectId();

    /**
     * Sets the digital object identifier.
     *
     * @param digitalObjectId The digital object identifier.
     *
     * @see MetaDataService
     */
    void setDigitalObjectId(DigitalObjectId digitalObjectId);

    /**
     * Gets the root node. The retrieved ICollectionNode is just a container for
     * the actual structure and it is special in the sense that it should be
     * interpreted in a different way than a normal ICollectionNodes: the name
     * is irrelevant and attributes are of global meaning.
     *
     * @return The root node.
     */
    ICollectionNode getRootNode();

    /**
     * Gets the first node in the tree with the given name.
     *
     * @param name The node name to search for.
     *
     * @return The node or null if no node was found.
     */
    IDataOrganizationNode getNodeByName(String name);

    /**
     * Walks the tree and executes a pre-order and a post-order action. Should
     * be implemented in means of calling
     * {@link edu.kit.dataorganization.impl.Util#walkSubtree} on the root node.
     *
     * @param preorederAction if not null action is executed on the node before
     * processing children
     * @param postorderAction if not null action is executed on the node after
     * processing children
     */
    void walkTree(IDataOrganizationNodeVisitor preorederAction,
            IDataOrganizationNodeVisitor postorderAction);

    void setViewName(String viewName);

    String getViewName();
}
