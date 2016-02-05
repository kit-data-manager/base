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
package edu.kit.dama.mdm.dataorganization.impl.jpa;

import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNodeVisitor;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.util.Constants;

/**
 *
 * @author pasic
 */
final public class FileTree extends CollectionNode implements IFileTree,
        Cloneable {

    private static final long serialVersionUID = 7526472295622776127L;
    /**
     * The digital object id the tree is associated with.
     */
    private DigitalObjectId digitalObjectId;

    /**
     * Default constructor.
     */
    public FileTree() {
    }

    /**
     * Constructor using to create a copy of the provided node. Tree-specific
     * field may not part of this copy as is is not guaranteed that <i>other</i>
     * is a tree root.
     *
     * @param other The node to copy.
     */
    public FileTree(CollectionNode other) {
        super(other);
    }

    @Override
    public void setDigitalObjectId(DigitalObjectId digitalObjectId) {
        this.digitalObjectId = digitalObjectId;
    }

    @Override
    public ICollectionNode getRootNode() {
        return this;
    }

    @Override
    public IDataOrganizationNode clone() throws CloneNotSupportedException {
        FileTree clone = (FileTree) super.clone();
        edu.kit.dama.mdm.dataorganization.impl.util.Util.walkAndClone(clone,
                null);
        return clone;
    }

    @Override
    public boolean equals(Object obj) {
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
    public int hashCode() {
        return super.hashCode() + 51;
    }

    @Override
    public IDataOrganizationNode getNodeByName(String name) {
        return Util.getNodeByName(this, name);
    }

    @Override
    public void walkTree(IDataOrganizationNodeVisitor preorederAction,
            IDataOrganizationNodeVisitor postorderAction) {
        Util.walkSubtree(this, preorederAction, postorderAction);
    }

    @Override
    public DigitalObjectId getDigitalObjectId() {
        return digitalObjectId;
    }

    @Override
    public String getViewName() {
        if (super.getViewName() != null) {
            return super.getViewName();
        } else {
            return Constants.DEFAULT_VIEW;
        }
    }

}
