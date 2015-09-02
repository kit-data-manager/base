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
package edu.kit.dama.mdm.dataorganization.entity.impl.client;

import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pasic
 */
public class CollectionNode extends DataOrganizationNode implements
        ICollectionNode, Cloneable {

  /**
   * The list of children.
   */
  private List<IDataOrganizationNode> children;

  /**
   * Default constructor.
   */
  public CollectionNode() {
  }

  @Override
  public final List<IDataOrganizationNode> getChildren() {
    if (null == children) {
      children = new ArrayList<IDataOrganizationNode>();
    }
    return children;
  }

  @Override
  public final void addChild(final IDataOrganizationNode child) {
    if (null == children) {
      children = new ArrayList<IDataOrganizationNode>();
    }
    children.add(child);
    child.setParent(this);

  }

  @Override
  public final void addChildren(List<IDataOrganizationNode> children) {
    for (IDataOrganizationNode child : children) {
      addChild(child);
    }
  }

  @Override
  public final void setChildren(List<? extends IDataOrganizationNode> children) {
    List<IDataOrganizationNode> myChildren = getChildren();
    myChildren.clear();
    for (IDataOrganizationNode don : children) {
      myChildren.add(don);
    }
  }

  @Override
  public IDataOrganizationNode clone() throws CloneNotSupportedException {
    IDataOrganizationNode node = super.clone();
    List<IDataOrganizationNode> nodeChildren = new ArrayList<IDataOrganizationNode>();
    for (IDataOrganizationNode child : getChildren()) {
      nodeChildren.add(child.clone());
    }
    ((ICollectionNode) node).setChildren(nodeChildren);
    return node;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ICollectionNode) {
      if (this == obj) {
        return true;
      }
      ICollectionNode other = (ICollectionNode) obj;
      if (!super.equals(other) || other.getChildren().size() != this.
              getChildren().size()) {
        return false;
      } else {
        List<IDataOrganizationNode> myC = this.getChildren();
        List<IDataOrganizationNode> oC
                = (List<IDataOrganizationNode>) other.getChildren();
        for (int i = 0; i < myC.size(); i++) {
          if (!myC.get(i).equals(oC.get(i))) {
            return false;
          }
        }
        return true;
      }
    } else {
      return false;
    }
  }

  @Override()
  public int hashCode() {
    int hash = 51;
    hash = 41 * hash
            + (this.children != null ? this.children.hashCode() : 0);
    return super.hashCode() ^ hash;
  }

}
