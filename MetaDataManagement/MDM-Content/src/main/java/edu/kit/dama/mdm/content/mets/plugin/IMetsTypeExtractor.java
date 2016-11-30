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

import java.io.File;

/**
 * Interface for extracting any type of a METS file in XML format.
 * XML will be indexed separately.
 * @author hartmann-v
 */
public interface IMetsTypeExtractor {
  /**
   * Get the schema identifier. Schema identifier has to be unique and should
   * match the type used for the search engine.
   * @return schema identifier.
   */
  String getName();
  /**
   * Get human readable description of this extractor.
   * @return description
   */
  String getDescription();
  /**
   * Get the namespace of the XML document. The given namespace is used to
   * look up for a registered schema. If no schema is registered the extractor
   * will be skipped. If schema exists the delivered document will be validated
   * against it.
   * @return namespace of the XML document.
   */
  String getNamespace();
  /**
   * Get the type as XML.
   * @param pMetsFile
   * @return XML containing type as String.
   */
  String extractType(File pMetsFile);
}
