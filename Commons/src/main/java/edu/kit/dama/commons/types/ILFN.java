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
package edu.kit.dama.commons.types;

/**
 * DRAFT for logical file name. <b> Extending implementations must have a no
 * argument constructor!!!</b>
 *
 * @author pasic
 */
public abstract class ILFN implements Cloneable {

  /**
   * An abstract version of a logical filename.
   */
  public ILFN() {
  }

  /**
   * Returns this LFN as string.
   *
   * @return The LFN as string.
   */
  public abstract String asString();

  /**
   * Parses this LFN from string.
   *
   * @param stringRepresentation The string representation to parse.
   */
  public abstract void fromString(String stringRepresentation);

  @Override
  public final ILFN clone() throws CloneNotSupportedException {
    return (ILFN) super.clone();
  }
}
