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
package edu.kit.dama.mdm.dataorganization.impl.util;

import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNodeVisitor;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Search for files which filenames matching regex. Examples: * - all files.
 * *.java - all files whose names end with “.java”. *.[ch] - all files whose
 * names end with either “.c” or “.h”. .*.(c|cpp|h|hpp|cxx|hxx) - all C or C++
 * files. [^#]* - all files whose names do not start with “#”.
 *
 * @author: hartmann-v
 */
public class SearchPatternVisitor implements IDataOrganizationNodeVisitor {

  /**
   * Logger for the class.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SearchPatternVisitor.class);

  /**
   * The node name.
   */
  private final String patternString;
  /**
   * The pattern instance for match operation.
   */
  private final Pattern pattern;
  /**
   * The matching node(s).
   */
  private Set<IDataOrganizationNode> matchingNodes;

  /**
   * Default constructor.
   *
   * @param pPattern The pattern of the name.
   */
  public SearchPatternVisitor(String pPattern) {
    matchingNodes = new HashSet<>();
    this.patternString = pPattern;
    pattern = Pattern.compile(patternString);
  }

  @Override
  public void action(IDataOrganizationNode node) {
    if (LOGGER.isTraceEnabled()) {
      LOGGER.trace("Testing node '{}' for pattern '{}'", node.getName(), patternString);
    }
    Matcher matcher = pattern.matcher(node.getName());
    if (matcher.matches()) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Add node '{}' to matching nodes.", node.getName());
      }

      matchingNodes.add(node);
    }
  }

  /**
   * Get all matching nodes.
   *
   * @return A set with all matching nodes.
   */
  public Set<IDataOrganizationNode> getAllNodes() {
    return matchingNodes;
  }
}
