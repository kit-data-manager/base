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
package edu.kit.dama.mdm.dataorganization.service.core;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import java.util.List;

/**
 * Manages persistent {@link IFileTree} objects.
 *
 * @author pasic
 */
public interface DataOrganizer {

  /**
   * Loads (and detaches) the persistent {@link IFileTree} instance associated
   * with the given digital object. The changes made on this object are not
   * persisted.
   *
   * @param digitalObjectId the identifier of the digital object
   * @param viewName The name of the view that should be loaded.
   *
   * @return a persistent {@link IFileTree} instance.
   *
   * @throws EntityNotFoundException no tree for the digitalObjectId present
   */
  IFileTree loadFileTree(DigitalObjectId digitalObjectId, String viewName)
          throws EntityNotFoundException;

  IFileTree loadFileTree(DigitalObjectId digitalObjectId)
          throws EntityNotFoundException;

  /**
   * Save the {@link IFileTree} instance. fileTree.getDigitalObjectID() must not
   * be null.
   *
   * @param fileTree The file tree to create.
   *
   * @throws EntityExistsException you can create a tree just once
   */
  void createFileTree(IFileTree fileTree) throws EntityExistsException;

  /**
   * Get a range of the children nodes for some node which is identified by the
   * node parameter.
   *
   * @param nodeId The node id.
   * @param firstResult The first index of the children list to include.
   * @param maxResult Maximum number of results.
   *
   * @return A list of children.
   *
   * @throws InvalidNodeIdException your nodeId became invalid due to structural
   * modifications.
   */
  List<? extends IDataOrganizationNode> getChildren(
          NodeId nodeId,
          int firstResult,
          int maxResult) throws InvalidNodeIdException;

  /**
   * Get the number of children for some node which is identified by the node
   * parameter.
   *
   * @param nodeId The node id.
   *
   * @return The number of children.
   *
   * @throws InvalidNodeIdException your nodeId became invalid due to structural
   * modifications.
   */
  Long getChildCount(NodeId nodeId) throws
          InvalidNodeIdException;

  /**
   * Get a subtree with relativeRoot as root containing all descendants up to
   * relativeDepth.
   *
   * @param relativeRoot The first node of the subtree.
   * @param relativeDepth Relative depth, starts with 0.
   *
   * @return The file tree.
   *
   * @throws InvalidNodeIdException your nodeId became invalid due to structural
   * modifications
   */
  IFileTree loadSubTree(
          NodeId relativeRoot,
          int relativeDepth) throws InvalidNodeIdException;

  /**
   * Get a transient handle to the root node for a digital object id. Be aware,
   * the handle may become invalid if the tree is modified. Operations on an
   * invalid handle are safe and result in an exception.
   *
   * @param digitalObjectId The object id.
   *
   * @return The root node id.
   *
   * @throws EntityNotFoundException If no root node was found for the provided
   * object id.
   */
  NodeId getRootNodeId(DigitalObjectId digitalObjectId)
          throws
          EntityNotFoundException;

  /**
   * Get a transient handle to the root node for a digital object id. Be aware,
   * the handle may become invalid if the tree is modified. Operations on an
   * invalid handle are safe and result in an exception.
   *
   * @param digitalObjectId The object id.
   * @param viewName The name of the view the node is associated with.
   *
   * @return The root node id.
   *
   * @throws EntityNotFoundException If no root node was found for the provided
   * object id.
   */
  NodeId getRootNodeId(DigitalObjectId digitalObjectId, String viewName)
          throws
          EntityNotFoundException;

  /**
   * Update the data for the node identified by nodeId with the values held by
   * newData. <b>This involves the attributes, too. </b>
   * Attributes not present in <i>newData</i> will be deleted, and the new ones
   * will be added. The parent children relationships of <i>newData</i>
   * will be ignored. Of course <i>newData</i> needs to be the a same type of
   * node as the node addressed by nodeId. If not, an exception will be thrown.
   *
   * @param nodeId The node id.
   * @param newData The new content of the node with the provided id.
   *
   * @throws InvalidNodeIdException your nodeId became invalid due to structural
   * modifications
   */
  void updateNodeData(
          NodeId nodeId,
          IDataOrganizationNode newData) throws InvalidNodeIdException;

  /**
   * Load a single node (without loading the ancestors and descendants).
   *
   * @param nodeId The node id.
   *
   * @return The node for the provided node id.
   *
   * @throws InvalidNodeIdException your nodeId became invalid due to structural
   * modifications
   */
  IDataOrganizationNode loadNode(NodeId nodeId) throws
          InvalidNodeIdException;

  List<String> getViews(DigitalObjectId doid);
}
