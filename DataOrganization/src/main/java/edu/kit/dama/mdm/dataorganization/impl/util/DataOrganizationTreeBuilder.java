/*
 * Copyright 2016 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.mdm.dataorganization.impl.util;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.commons.types.ILFN;
import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileTreeImpl;
import java.util.Set;

/**
 *
 * @author jejkal
 */
public class DataOrganizationTreeBuilder {

    private FileTreeImpl theTree;
    private ICollectionNode currentCollection;

    public DataOrganizationTreeBuilder() {
    }

    /**
     * Start building a new tree for the provided objectId and viewName.
     *
     * @param pDigitalObjectId The object id.
     * @param viewName The view name.
     *
     * @return this
     */
    public DataOrganizationTreeBuilder create(DigitalObjectId pDigitalObjectId, String viewName) {
        theTree = new FileTreeImpl();
        theTree.setDigitalObjectId(pDigitalObjectId);
        theTree.setViewName(viewName);
        currentCollection = theTree.getRootNode();
        return this;
    }

    /**
     * Add an existing file node identified by its nodeId to the currently
     * active collection node. This method is useful while restructuring an
     * existing data organization tree or while downloading only parts of a data
     * organization tree.
     *
     * By providing the nodeId of an existing node the node will be referenced
     * in the newly created tree but might be located at another position.
     * However, 'nodeId' must match an existing file node of the digital object
     * with the intially provided digital object id.
     *
     * @param nodeId The node id of an existing file node.
     *
     * @return this
     */
    public DataOrganizationTreeBuilder addFile(long nodeId) {
        FileNodeImpl fileNode = new FileNodeImpl(null);
        fileNode.setNodeId(nodeId);
        currentCollection.addChild(fileNode);
        return this;
    }

    /**
     * Add an new file node to the currently active collection node. This method
     * is useful for adding an entirely new file node, e.g. to construct a new
     * tree that will be ingested or for testing purposes.
     *
     * @param logicalFilename The logical filename this node refers to.
     * @param name The node's name.
     *
     * @return this
     */
    public DataOrganizationTreeBuilder addFile(ILFN logicalFilename, String name) {
        return addFile(logicalFilename, name, null);
    }

    /**
     * Add an new file node to the currently active collection node. This method
     * is useful for adding an entirely new file node, e.g. to construct a new
     * tree that will be ingested or for testing purposes.
     *
     * @param logicalFilename The logical filename this node refers to.
     * @param name The node's name.
     * @param attributes A list of attributes assigned to the node.
     *
     * @return this
     */
    public DataOrganizationTreeBuilder addFile(ILFN logicalFilename, String name, Set<? extends IAttribute> attributes) {
        FileNodeImpl fileNode = new FileNodeImpl(logicalFilename);
        fileNode.setName(name);
        fileNode.setAttributes(attributes);
        currentCollection.addChild(fileNode);
        return this;
    }

    /**
     * Add an existing collection node identified by its nodeId to the currently
     * active collection node. This method is useful while restructuring an
     * existing data organization tree or while downloading only parts of a data
     * organization tree. This method only adds a single node instead of
     * recursing through the collection. Handling the collection's children is
     * the task of the implementation using the final tree.
     *
     * When adding a collection node by its nodeId it is not possible to add
     * other nodes to this collection. This internal collection cursor will
     * remain on the currently active collection.
     *
     * By providing the nodeId of an existing node the node will be referenced
     * in the newly created tree but might be located at another position.
     * However, 'nodeId' must match an existing file node of the digital object
     * with the intially provided digital object id.
     *
     * @param nodeId The node id of an existing collection node.
     *
     * @return this
     */
    public DataOrganizationTreeBuilder addCollection(long nodeId) {
        CollectionNodeImpl collectionNode = new CollectionNodeImpl();
        collectionNode.setNodeId(nodeId);
        currentCollection.addChild(collectionNode);
        return this;
    }

    /**
     * Add an new collection node and set the internal cursor to the created
     * collection. This method is useful for adding an entirely new collection
     * node, e.g. to construct a new tree that will be ingested, for testing
     * purposes or for restructuring a tree for download.
     *
     * In contrast to {@link #addCollection(long)} it is possible to add nodes
     * to the new collection. Therfor, the internal cursor pointing to the
     * current collection refers to the node created by this call afterwards.
     * All subsequent calls will affect this node as long as
     * {@link #leaveCollection()} it not called.
     *
     * @param name The name of the collection node to create.
     *
     * @return this
     */
    public DataOrganizationTreeBuilder createAndEnterCollection(String name) {
        CollectionNodeImpl collectionNode = new CollectionNodeImpl();
        collectionNode.setName(name);
        currentCollection.addChild(collectionNode);
        currentCollection = collectionNode;
        return this;
    }

    /**
     * Leave the collection the internal cursor is currently pointing at. The
     * cursor moves to the parent node of the currently selected collection. If
     * there is no parent node, e.g. because the cursor is pointing to the root
     * node, nothing happens.
     *
     * @return this
     */
    public DataOrganizationTreeBuilder leaveCollection() {
        ICollectionNode parent = currentCollection.getParent();
        if (parent != null) {
            currentCollection = parent;
        } else {
            //no parent...do nothing
        }
        return this;
    }

    /**
     * Return the final tree.
     *
     * @return The tree.
     */
    public FileTreeImpl buildTree() {
        return theTree;
    }
}
