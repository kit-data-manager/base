/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
package edu.kit.dama.authorization.annotations.resources;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 *
 * @author hartmann-v
 */
@Entity
public class TestEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String summary;
  private String description;

    /**
     *
     * @return
     */
    public String getSummary() {
    return summary;
  }

    /**
     *
     * @param summary
     */
    public void setSummary(String summary) {
    this.summary = summary;
  }

    /**
     *
     * @return
     */
    public String getDescription() {
    return description;
  }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "[ID: " + id + " summary=" + summary + ", description=" + description
            + "]";
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }
}
