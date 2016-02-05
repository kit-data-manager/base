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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.content.search.impl;

/**
 * @param C The generic type of this term.
 * 
 * @author mf6319
 */
public class BaseSearchTerm<C> {

  private final String label;
  private final String key;
  private C value;

  /**
   * Constructor for creating a Term.
   *
   * @param label User readable string representation.
   * @param key The search key of the term.
   */
  public BaseSearchTerm(String label, String key) {
    this.label = label;
    this.key = key;
  }

  /**
   * Returns the label of the term.
   *
   * @return Label of term.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns the key of the Term.
   *
   * @return Key of term.
   */
  public String getKey() {
    return key;
  }

  /**
   * Set the value of this term.
   *
   * @param pValue The value.
   */
  public void setValue(C pValue) {
    value = pValue;
  }

  /**
   * Set the value of this term.
   *
   * @return The value.
   */
  public C getValue() {
    return value;
  }

}
