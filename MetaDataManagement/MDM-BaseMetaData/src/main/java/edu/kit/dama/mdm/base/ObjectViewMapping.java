/*
 * Copyright 2015 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.dama.mdm.base;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 *
 * @author mf6319
 */
@Entity
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "simple",
          attributeNodes = {
            @XmlNamedAttributeNode("id")
          }),
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            //id is not in because it is not needed
            @XmlNamedAttributeNode(value = "digitalObject", subgraph = "simple"),
            @XmlNamedAttributeNode("viewName")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
public class ObjectViewMapping implements Serializable {

  private static final long serialVersionUID = -6157665960655560658L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;
  @ManyToOne
  private DigitalObject digitalObject;
  private String viewName;

  /**
   * Default constructor.
   */
  public ObjectViewMapping() {
  }

  /**
   * Default constructor.
   *
   * @param digitalObject The digital object.
   * @param viewName The view name.
   */
  public ObjectViewMapping(DigitalObject digitalObject, String viewName) {
    this.digitalObject = digitalObject;
    this.viewName = viewName;
  }

  /**
   * Get the id.
   *
   * @return The id.
   */
  public long getId() {
    return id;
  }

  /**
   * Set the id.
   *
   * @param id The id.
   */
  public void setId(long id) {
    this.id = id;
  }

  /**
   * Get the digital object.
   *
   * @return The digital object.
   */
  public DigitalObject getDigitalObject() {
    return digitalObject;
  }

  /**
   * Set the digital object.
   *
   * @param digitalObject The digital object.
   */
  public void setDigitalObject(DigitalObject digitalObject) {
    this.digitalObject = digitalObject;
  }

  /**
   * Get the view name.
   *
   * @return The view name.
   */
  public String getViewName() {
    return viewName;
  }

  /**
   * Set the view name.
   *
   * @param viewName The view name.
   */
  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  @Override
  public String toString() {
    return getDigitalObject().getDigitalObjectIdentifier() + " / " + getViewName();
  }

}
