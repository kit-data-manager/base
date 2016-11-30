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
package edu.kit.dama.sample.mets;

import edu.kit.dama.mdm.content.mets.plugin.IMetsTypeExtractor;
import edu.kit.dama.mdm.content.mets.util.MetsHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Namespace;

/**
 * <p>Example class for extractor plugins extracting dublin core.</p>
 *
 * <b>Step 1: Implement Interface</b>
 * <p>To add your own indexer based on METS document the IMetsTypeExtractor has to
 * be implemented. Create a new project with dependency to maven project
 * MDM-Content.</p>
 *
 * <b>Step 2: Register new plugin</b>
 *
 * <p>To register a new plugin the pom.xml has to be prepared like the following:</p>
 * <pre>
 * {@code
 *       <plugin>
 *         <groupId>eu.somatik.serviceloader-maven-plugin</groupId>
 *         <artifactId>serviceloader-maven-plugin</artifactId>
 *         <version>1.0.7</version>
 *         <configuration>
 *           <services>
 *             <param>edu.kit.dama.content.mets.plugin.IMetsTypeExtractor</param>
 *           </services>
 *         </configuration>
 *         <executions>
 *           <execution>
 *             <goals>
 *               <goal>generate</goal>
 *             </goals>
 *           </execution>
 *         </executions>
 *       </plugin>
 * }
 * </pre>
 * 
 * <b>Step 3: Register new metadata schema</b>
 *
 * <p>To enable the implemented plugin the linked metadata schema has to be
 * registered.</p>
 * <ul>
 * <li>schema identifier - name of the plugin</li>
 * <li>schema URL - namespace of the generated XML document.</li>
 * </ul>
 * <p></p>
 * 
 * <b>Step 4: Add new plugin to KIT Data Manager</b>
 *
 * <p>That’s it. Now you can generate and add the jar file in the lib directory and
 * restart KIT Data Manager.</p>
 *
 * @author hartmann-v
 */
public abstract class MetsExampleExtractor implements IMetsTypeExtractor {

  /**
   * List holding at least all namespaces needed inside xPath expression.
   */
  private List<Namespace> allNamespaces = new ArrayList<>();

  /**
   * Default constructor.
   */
  protected MetsExampleExtractor() {
    addNamespace("mets", "http://www.loc.gov/METS/");
  }

  /**
   * Get the XPATH expression selecting the given
   *
   * @return xPath expression.
   */
  public String getXPath() {
    return "//oai_dc:dc";
  }

  @Override
  public String getName() {
    return "oai_dc";
  }

  @Override
  public String getDescription() {
    return "Extract Dublin Core metadata from METS file.";
  }

  @Override
  public String getNamespace() {
    return "http://www.openarchives.org/OAI/2.0/oai_dc/";
  }

  /**
   * If XPATH use more than the default namespaces the additional namespaces
   * have to be added via this method. Default namespaces: - mets -
   *
   * @param prefix prefix of the namespace.
   * @param uri URI of the namespace.
   */
  private void addNamespace(String prefix, String uri) {
    allNamespaces.add(Namespace.getNamespace(prefix, uri));
  }

  @Override
  public String extractType(File pMetsFile) {
    allNamespaces.add(Namespace.getNamespace(getName(), getNamespace()));
    return MetsHelper.extractFromFile(pMetsFile, getXPath(), allNamespaces.toArray(new Namespace[allNamespaces.size()]));
  }

}
