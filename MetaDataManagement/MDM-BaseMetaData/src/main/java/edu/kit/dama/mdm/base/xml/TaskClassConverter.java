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
package edu.kit.dama.mdm.base.xml;

import com.thoughtworks.xstream.converters.SingleValueConverter;
import edu.kit.dama.mdm.base.Task;

/**
 * Converter for xStream.
 *
 * @author hartmann-v
 */
public class TaskClassConverter implements SingleValueConverter {

  @Override
  public String toString(Object obj) {
    return ((Task) obj).getTask();
  }

  @Override
  public Object fromString(String name) {
    return new Task(name);
  }

  @Override
  public boolean canConvert(Class type) {
    return type.equals(Task.class);
  }
}
