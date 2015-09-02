/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.dataorganization.impl.jpa;

import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.commons.types.ILFN;
import javax.persistence.Embedded;
import javax.persistence.Entity;

/**
 *
 * @author pasic
 */
@Entity
public class FileNode extends DataOrganizationNode implements IFileNode, Cloneable {

  private static final long serialVersionUID = 7526472295622776127L;
  @Embedded
  private LFNStringRepresentation lfn = new LFNStringRepresentation();

  /**
   * The default constructor.
   */
  public FileNode() {
  }

  /**
   * A constructor used to create a FileNode which is a copy of the provided
   * node.
   *
   * @param other The node to copy.
   */
  public FileNode(IFileNode other) {
    super(other);
    lfn.setLFN(other.getLogicalFileName());
  }

  @Override
  public ILFN getLogicalFileName() {
    return lfn.getLFN();
  }

  @Override
  public void setLogicalFileName(ILFN logicalFileName) {
    lfn.setLFN(logicalFileName);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IFileNode) {
      if (this == obj) {
        return true;
      }
      ILFN othersLFN = ((IFileNode) obj).getLogicalFileName();
      return super.equals(obj) && lfn.getValue().equals(othersLFN.asString())
              && lfn.getFullyQualifiedTypeName().equals(othersLFN.getClass().getCanonicalName());
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 61 * hash + (this.lfn != null && this.lfn.getValue() != null ? this.lfn.getValue().hashCode() : 0);
    return super.hashCode() ^ hash;
  }

  @Override
  public FileNode clone() throws CloneNotSupportedException {
    FileNode clone = (FileNode) super.clone();
    clone.setLogicalFileName(getLogicalFileName().clone());
    return clone;
  }
}
