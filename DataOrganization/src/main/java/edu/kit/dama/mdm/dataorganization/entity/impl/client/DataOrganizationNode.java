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

import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.service.exception.DataOrganizationError;
import edu.kit.dama.util.Constants;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author pasic
 */
public abstract class DataOrganizationNode implements IDataOrganizationNode,
        Cloneable {

  /**
   * The node name.
   */
  private String name;
  /**
   * A list of node attributes.
   */
  private Set<IAttribute> attributes;
  /**
   * The parent or null if not parent is available.
   */
  private ICollectionNode parent;
  /**
   * The description.
   */
  private String description;

  private String viewName = Constants.DEFAULT_VIEW;

  /**
   * Default constructor.
   */
  public DataOrganizationNode() {
  }

  /**
   * Default constructor used to copy the properties of the provided node.
   *
   * @param other The node from which the properties will be copied to this
   * node.
   */
  public DataOrganizationNode(IDataOrganizationNode other) {
    name = other.getName();
    description = other.getDescription();
    attributes = (Set<IAttribute>) other.getAttributes();
  }

  @Override
  public final String getDescription() {
    return description;
  }

  @Override
  public final void setDescription(String description) {
    this.description = description;
  }

  @Override
  public final ICollectionNode getParent() {
    return parent;
  }

  @Override
  public final void setParent(ICollectionNode parent) {
    this.parent = parent;
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  public final void setName(String name) {
    this.name = name;
  }

  @Override
  final public Set<IAttribute> getAttributes() {
    if (null == attributes) {
      attributes = new HashSet<IAttribute>();
    }
    return attributes;
  }

  @Override
  public final void setAttributes(Set<? extends IAttribute> attributes) {
    Set<IAttribute> myAttributes = getAttributes();
    myAttributes.clear();
    for (IAttribute attr : attributes) {
      myAttributes.add(attr);
    }
  }

  @Override
  public final void addAttribute(IAttribute attribute) {
    getAttributes().add(attribute);
  }

  @Override
  public final NodeId getTransientNodeId() {
    throw new UnsupportedOperationException("The node is detached!");
  }

  @Override
  public IDataOrganizationNode clone() throws CloneNotSupportedException {
    DataOrganizationNode don = null;
    if (this instanceof ICollectionNode) {
      don = new CollectionNode();
    } else if (this instanceof IFileNode) {
      don = new FileNode();
    }
    if (null == don) {
      throw new DataOrganizationError(
              "node:IDataOrganizationNode should be either"
              + " instance of IFileNode or instance of ICollectionNode");
    }
    don.setDescription(description);
    don.setName(name);
    Set<IAttribute> attributes1 = don.getAttributes();

    if (attributes != null) {
      for (IAttribute attr : attributes) {
        attributes1.add(attr.clone());
      }
    }
    return don;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof IDataOrganizationNode) {
      if (this == obj) {
        return true;
      }
      IDataOrganizationNode other = (IDataOrganizationNode) obj;

      if ((null == description ? null == other.getDescription()
              : description.equals(other.getDescription()))
              && (null == name ? null == other.getName()
              : name.equals(other.getName()))) {
        return getAttributes().equals(other.getAttributes());
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
    hash = 53 * hash
            + (this.attributes != null ? this.attributes.hashCode() : 0);
    //  hash = 53 * hash + (this.parent != null ? this.parent.hashCode() : 0);
    hash = 53 * hash + (this.description != null ? this.description.
            hashCode() : 0);
    return hash;
  }

  /**
   * @return the viewName
   */
  @Override
  public String getViewName() {
    return viewName;
  }

  /**
   * @param viewName the viewName to set
   */
  @Override
  public void setViewName(String viewName) {
    if (viewName != null) {
      this.viewName = viewName;
    }
  }
}
