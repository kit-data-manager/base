/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.mdm.content.mets.plugin;

import edu.kit.dama.mdm.content.mets.util.MetsIndexExtractor;

/**
 * Extract base metadata available inside a METS document.
 * @author hartmann-v
 */
public class BaseMetadataExtractor extends MetsIndexExtractor {

  @Override
  public String getXPath() {
    return "//bmd:basemetadata";
  }

  @Override
  public String getName() {
    return "bmd";
  }

  @Override
  public String getDescription() {
    return "Extract Basemetadata from METS file.";
  }

  @Override
  public String getNamespace() {
    return "http://datamanager.kit.edu/dama/basemetadata";
  }
  
}
