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

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.commons.types.DigitalObjectId;
import java.util.List;

/**
 *
 * @author pasic
 * @param <C> type of the security context
 */
public interface IDataOrganizationService<C extends IAuthorizationContext> {

  /**
   * Loads (and detaches) the persistent {@link IFileTree} instance associated
   * with the given digital object. The changes made on this object are not
   * persisted.
   *
   * @param digitalObjectId the identifier of the digital object
   * @param ctx security context
   *
   * @return a persistent {@link IFileTree} instance
   *
   * @throws EntityNotFoundException no tree for the digitalObjectId present
   */
  IFileTree loadFileTree(DigitalObjectId digitalObjectId, C ctx)
          throws EntityNotFoundException;

  /**
   * Loads (and detaches) the provided view of the persistent {@link IFileTree}
   * instance associated with the given digital object. The changes made on this
   * object are not persisted.
   *
   * @param digitalObjectId the identifier of the digital object
   * @param viewName The view to load.
   * @param ctx security context
   *
   * @return a persistent {@link IFileTree} instance
   *
   * @throws EntityNotFoundException no tree for the digitalObjectId present
   */
  IFileTree loadFileTree(DigitalObjectId digitalObjectId, String viewName,
          C ctx)
          throws EntityNotFoundException;

  /**
   * Save the {@link IFileTree} instance. fileTree.getDigitalObjectID() must not
   * be null.
   *
   * @param fileTree The file tree to persist.
   * @param ctx security context
   * @throws EntityExistsException you can create a tree just once
   */
  void createFileTree(IFileTree fileTree, C ctx) throws EntityExistsException;

  /**
   * Get a range of the children nodes for some node which is identified by the
   * node parameter.
   *
   * @param nodeId The node id.
   * @param firstResult the first index of the children list to include
   * @param maxResult maximum number of results
   * @param ctx security context
   *
   * @return The list of children.
   *
   * @throws InvalidNodeIdException your nodeId became invalid due to structural
   * modifications
   */
  List<? extends IDataOrganizationNode> getChildren(
          NodeId nodeId,
          int firstResult,
          int maxResult,
          C ctx) throws
          InvalidNodeIdException;

  /**
   * Get a subtree with relativeRoot as root containing all descendants up to
   * relativeDepth if relativeDepth &gt;= 0 and all descendants if relativeDepth =
   * -1 the whole subtree.
   *
   * @param relativeRoot The root of the subtree.
   * @param relativeDepth The max. depth.
   * @param ctx The security context
   *
   * @return The file tree.
   *
   * @throws InvalidNodeIdException your nodeId became invalid due to structural
   * modifications
   */
  IFileTree loadSubTree(
          NodeId relativeRoot,
          int relativeDepth,
          C ctx) throws
          InvalidNodeIdException;

  /**
   * Get a transient handle to the root node for some digital object. Be aware
   * the handle may become invalid if the tree is modified. Operations on an
   * invalid handle are safe and result in exception
   *
   * @param digitalObjectId The digital object id.
   * @param viewName The name of the view.
   * @param ctx The security context.
   *
   * @return The node id.
   *
   * @throws EntityNotFoundException If the entity was not found.
   */
  NodeId getRootNodeId(DigitalObjectId digitalObjectId, String viewName, C ctx)
          throws EntityNotFoundException;

  NodeId getRootNodeId(DigitalObjectId digitalObjectId, C ctx)
          throws EntityNotFoundException;

  /**
   * Update the data for the node identified by nodeId with the values held by
   * newData. <b>This involves the attributes too. </b>
   * Attributes not present in the newData will be deleted, and the new ones
   * will be added. The parent children relationships of newData will be
   * ignored. Of course newData needs tho be the a same type of node as the node
   * addressed by nodeId, if not an exception will be thrown.
   *
   * @param nodeId The id of the node to update.
   * @param newData The new data for the node.
   * @param ctx security context
   *
   * @throws InvalidNodeIdException your nodeId became invalid due to structural
   * modifications
   */
  void updateNodeData(
          NodeId nodeId,
          IDataOrganizationNode newData, C ctx) throws
          InvalidNodeIdException;

  /**
   * Load a single node (without loading the ancestors and descendants).
   *
   * @param nodeId The id of the node to load.
   * @param ctx security context
   *
   * @return The node.
   *
   * @throws InvalidNodeIdException your nodeId became invalid due to structural
   * modifications
   */
  IDataOrganizationNode loadNode(NodeId nodeId, C ctx) throws
          InvalidNodeIdException;

}
