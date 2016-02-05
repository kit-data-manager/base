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
package edu.kit.dama.mdm.dataorganization.impl.util;

import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNodeVisitor;

/**
 * Utility methods for the implementation.
 *
 * @author pasic
 */
public final class Util {

  /**
   * Hidden constructor.
   */
  private Util() {
  }

  /**
   * Walks a subtree.
   *
   * @param node The node to start with.
   * @param preorderAction preorderAction.action(thisNode) will be called before
   * processing the children of thisNode
   * @param postorderAction postorder.action(thisNode) is called after
   * processing the children of thisNode (postorderAction if not null)
   */
  public static void walkSubtree(IDataOrganizationNode node,
          IDataOrganizationNodeVisitor preorderAction,
          IDataOrganizationNodeVisitor postorderAction) {
    if (null != preorderAction) {
      preorderAction.action(node);
    }
    if (node instanceof ICollectionNode) {
      ICollectionNode coll = (ICollectionNode) node;
      for (IDataOrganizationNode n : coll.getChildren()) {
        walkSubtree(n, preorderAction, postorderAction);
      }
    }
    if (null != postorderAction) {
      postorderAction.action(node);
    }
  }

  /**
   * Makes a deep copy of the subtree.
   *
   * @param don Root node of the subtree.
   * @param parent The parent. Null at beginning.
   *
   * @throws CloneNotSupportedException Clone is not supported.
   */
  public static void walkAndClone(IDataOrganizationNode don, ICollectionNode parent) throws CloneNotSupportedException {
    if (null != parent) {
      parent.addChild(don.clone());
    }
    if (don instanceof ICollectionNode) {
      for (IDataOrganizationNode child : ((ICollectionNode) don).getChildren()) {
        walkAndClone(child, (ICollectionNode) don);
      }
    }
  }

  /**
   * Gets the a first node with the provided name from the provided subtree.
   *
   * @param subtree The subtree to search.
   * @param name The node name to search for.
   *
   * @return The found node or null if no node was found.
   */
  public static IDataOrganizationNode getNodeByName(ICollectionNode subtree, String name) {
    SearchVisitor searchVisitor = new SearchVisitor(name);
    try {
      walkSubtree(subtree, searchVisitor, null);
    } catch (SearchVisitor.NodeFoundException e) {
      return e.getNode();
    }
    return null;
  }
}

/**
 * Utility class for search.
 *
 * @author pasic
 */
class SearchVisitor implements IDataOrganizationNodeVisitor {

  class NodeFoundException extends RuntimeException {

    /**
     * The node.
     */
    private final IDataOrganizationNode node;

    /**
     * Default constructor.
     *
     * @param node The node to return.
     */
    public NodeFoundException(IDataOrganizationNode node) {
      this.node = node;
    }

    /**
     * Returns the node.
     *
     * @return The node.
     */
    public IDataOrganizationNode getNode() {
      return node;
    }
  }

  /**
   * The node name.
   */
  private final String name;

  /**
   * Default constructor.
   *
   * @param name The name.
   */
  public SearchVisitor(String name) {
    this.name = name;
  }

  @Override
  public void action(IDataOrganizationNode node) {
    if (name.equals(node.getName())) {
      throw new NodeFoundException(node);
    }
  }
}
