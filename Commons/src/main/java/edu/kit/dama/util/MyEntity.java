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
package edu.kit.dama.util;

import java.io.Serializable;

/**
 *
 * @author mf6319
 */
public class MyEntity implements Serializable {

  private Long id;
  private Long predecessor;

  private String content;

  public MyEntity() {
  }

  public MyEntity(Long pId, String pContent, Long pPredecessor) {
    content = pContent;
    id = pId;
    predecessor = pPredecessor;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public void setPredecessor(Long predecessor) {
    this.predecessor = predecessor;
  }

  public Long getPredecessor() {
    return predecessor;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  @Override
  public String toString() {
    return getId() + ") " + getContent() + " -> " + getPredecessor();
  }

}
