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
package edu.kit.dama.mdm.dataorganization.entity.impl.client;

import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNodeVisitor;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.commons.types.DigitalObjectId;

/**
 *
 * @author pasic
 */
public class FileTree extends CollectionNode implements IFileTree, Cloneable {

  /**
   * The digital object id.
   */
  private DigitalObjectId digitalObjectId;

  @Override
  public final void setDigitalObjectId(DigitalObjectId digitalObjectId) {
    this.digitalObjectId = digitalObjectId;
  }

  @Override
  public final ICollectionNode getRootNode() {
    return this;
  }

  @Override
  public final FileTree clone() throws CloneNotSupportedException {
    //implement if needed
    throw new CloneNotSupportedException("Clone is not supported for FileTree.");
  }

  @Override
  public final boolean equals(Object obj) {
    if (obj instanceof IFileTree) {
      if (this == obj) {
        return true;
      }

      return super.equals(obj);
    } else {
      return false;
    }
  }

  @Override
  public final DigitalObjectId getDigitalObjectId() {
    return digitalObjectId;
  }

  @Override
  public final int hashCode() {
    int hash = 51;
    hash = 41 * hash + (this.digitalObjectId != null ? this.digitalObjectId.hashCode() : 0);
    return super.hashCode() ^ hash;
  }

  @Override
  public final IDataOrganizationNode getNodeByName(String name) {
    return Util.getNodeByName(this, name);
  }

  @Override
  public final void walkTree(IDataOrganizationNodeVisitor preorederAction, IDataOrganizationNodeVisitor postorderAction) {
    Util.walkSubtree(this, preorederAction, postorderAction);
  }
}
