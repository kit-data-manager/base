/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.authorization.entities;

/**
 *
 * @param <T> Generic type.
 * @author pasic
 */
public interface IRoleRestriction<T> extends Comparable<T> {

  /**
   * Check if this restriction is at least the provided role.
   *
   * @param role The role.
   *
   * @return This restriction is at least <tt>role</tt>.
   */
  boolean atLeast(T role);

  /**
   * Check if this restriction is at most the provided role.
   *
   * @param role The role.
   *
   * @return This restriction is at most <tt>role</tt>.
   */
  boolean atMost(T role);

  /**
   * Check if this restriction is more than the provided role.
   *
   * @param role The role.
   *
   * @return This restriction is more than <tt>role</tt>.
   */
  boolean moreThan(T role);

  /**
   * Check if this restriction is less than the provided role.
   *
   * @param role The role.
   *
   * @return This restriction is less than <tt>role</tt>.
   */
  boolean lessThan(T role);
}
