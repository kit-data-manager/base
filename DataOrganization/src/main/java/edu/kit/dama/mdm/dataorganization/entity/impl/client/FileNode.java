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

import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.commons.types.ILFN;

/**
 *
 * @author pasic
 */
public final class FileNode extends DataOrganizationNode implements IFileNode,
        Cloneable {

    /**
     * The logical filename.
     */
    private ILFN logicalFileName;

    /**
     * Hidden constructor.
     */
    protected FileNode() {
    }

    /**
     * Default constructor.
     *
     * @param logicalFileName The logical filename of the node.
     */
    public FileNode(ILFN logicalFileName) {
        this.logicalFileName = logicalFileName;
    }

    @Override
    public ILFN getLogicalFileName() {
        return logicalFileName;
    }

    @Override
    public void setLogicalFileName(ILFN logicalFileName) {
        this.logicalFileName = logicalFileName;
    }

    @Override
    public FileNode clone() throws CloneNotSupportedException {
        FileNode clone = (FileNode) super.clone();
        clone.setLogicalFileName(logicalFileName.clone());
        return clone;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IFileNode) {
            if (logicalFileName == null) {
                return false;
            }
            return logicalFileName.
                    equals(((IFileNode) obj).getLogicalFileName()) && super.
                    equals(obj);
        } else {
            return false;
        }

    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.logicalFileName != null ? this.logicalFileName.
                asString().hashCode() : 0);
        return super.hashCode() ^ hash;
    }

}
