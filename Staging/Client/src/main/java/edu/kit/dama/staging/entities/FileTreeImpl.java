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
package edu.kit.dama.staging.entities;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNodeVisitor;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;

/**
 *
 * @author jejkal
 */
public final class FileTreeImpl extends CollectionNodeImpl implements IFileTree {

  private DigitalObjectId digitalObjectId;

  @Override
  public void setDigitalObjectId(DigitalObjectId digitalObjectId) {
    this.digitalObjectId = digitalObjectId;
  }

  @Override
  public ICollectionNode getRootNode() {
    return this;
  }

  @Override
  public DigitalObjectId getDigitalObjectId() {
    return digitalObjectId;
  }

  @Override
  public IDataOrganizationNode getNodeByName(String name) {
    return Util.getNodeByName(this, name);
  }

  @Override
  public void walkTree(IDataOrganizationNodeVisitor preorederAction, IDataOrganizationNodeVisitor postorderAction) {
    Util.walkSubtree(this, preorederAction, postorderAction);
  }
}
