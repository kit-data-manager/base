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
package edu.kit.dama.mdm.content.mets.util;

import java.io.File;
import java.util.List;
import org.fzk.tools.xml.JaxenUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class extracting nodes from XML file.
 *
 * @author hartmann-v
 */
public final class MetsHelper {

  /**
   * Logger.
   */
  private static Logger LOGGER = LoggerFactory.getLogger(MetsHelper.class);

  /**
   * Extract XML from XML file.
   *
   * @param pSourceFile METS file
   * @param xPath XPATH expression selecting node
   * @param namespaces used namespaces.
   * @return Selected node as String.
   */
  public static String extractFromFile(File pSourceFile, String xPath, Namespace... namespaces) {
    String xmlString = "Error";
    LOGGER.debug("Extract xml from file '{}' with xPath '{}'.", pSourceFile.getAbsolutePath(), xPath);
    if (LOGGER.isTraceEnabled()) {
      for (Namespace item : namespaces) {
        LOGGER.trace("Used namespace: {} : {}", item.getPrefix(), item.getURI());
      }
    }
    try {
      Document document = JaxenUtil.getDocument(pSourceFile);
      List<Element> nodes;
      if (namespaces.length < 1) {
        nodes = JaxenUtil.getNodes(document, xPath);
      } else {
        nodes = JaxenUtil.getNodes(document, xPath, namespaces);

      }
      if (nodes.size() < 1) {
        LOGGER.warn("XPath expression '{}' seems to be invalid!", xPath);
      } else {
        if (nodes.size() > 1) {
          LOGGER.warn("XPath expression '{}' is not unique. Selected first occurrence!", xPath);
        }
        Element node = nodes.get(0);
        Document document2 = new Document((Element) node.detach());
        XMLOutputter outputter = new XMLOutputter();
        xmlString = outputter.outputString(document2);
//        xmlString = outputter.outputString(node);
      }

    } catch (Exception ex) {
      LOGGER.error("Error while parsing METS file: " + pSourceFile.getAbsolutePath(), ex);
    }
    return xmlString;
  }

}
