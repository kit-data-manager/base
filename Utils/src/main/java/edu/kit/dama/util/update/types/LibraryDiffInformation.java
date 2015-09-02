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
package edu.kit.dama.util.update.types;

import edu.kit.dama.util.update.JarDiff;
import edu.kit.dama.util.update.UpdateScriptGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * The helper object will contain the cumulated diff information created while
 * executing {@link JarDiff}. It allows to print this information in a formatted
 * way and it can be used to generate update scripts like in the
 * {@link UpdateScriptGenerator} implementation.
 *
 * @see UpdateScriptGenerator
 * @author mf6319
 */
public final class LibraryDiffInformation {

  private int detectedSnapshots = 0;
  private final List<JavaLibrary> unknown;
  private final List<JavaLibrary> unchanged;
  private final List<JavaLibrary> added;
  private final List<JavaLibrary> changed;
  private final List<JavaLibrary> deprecated;
  private final List<File> ignored;

  /**
   * Default constructor.
   */
  public LibraryDiffInformation() {
    unknown = new ArrayList<>();
    unchanged = new ArrayList<>();
    changed = new ArrayList<>();
    added = new ArrayList<>();
    deprecated = new ArrayList<>();
    ignored = new ArrayList<>();
  }

  public void addUnknownEntry(JavaLibrary pUnknown) {
    unknown.add(pUnknown);
  }

  public List<JavaLibrary> getUnknownEntries() {
    return unknown;
  }

  public void addUnchangedEntry(JavaLibrary pUnchanged) {
    unchanged.add(pUnchanged);
  }

  public List<JavaLibrary> getUnchangedEntries() {
    return unchanged;
  }

  public void addAddedEntry(JavaLibrary pAdded) {
    added.add(pAdded);
  }

  public List<JavaLibrary> getAddedEntries() {
    return added;
  }

  public void addChangedEntry(JavaLibrary pChanged) {
    changed.add(pChanged);
  }

  public List<JavaLibrary> getChangedEntries() {
    return changed;
  }

  public void addDeprecatedEntry(JavaLibrary pDeprecated) {
    deprecated.add(pDeprecated);
  }

  public List<JavaLibrary> getDeprecatedEntries() {
    return deprecated;
  }

  public void setInvalidEntries(List<File> pIgnored) {
    if (pIgnored != null) {
      ignored.addAll(pIgnored);
    }
  }

  public List<File> getIgnoredFiles() {
    return ignored;
  }

  public void addSnapshot() {
    detectedSnapshots++;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    String caption = "Library Diff Information";
    int maxWidth = 72;
    b.append(StringUtils.center(caption, maxWidth)).append("\n");
    b.append(StringUtils.rightPad("", maxWidth, "-")).append("\n");
    for (JavaLibrary lib : added) {
      b.append(StringUtils.abbreviateMiddle("(+) " + lib, "[...]", maxWidth)).append("\n");
    }
    if (!changed.isEmpty()) {
      b.append("\n");
    }
    for (JavaLibrary lib : changed) {
      b.append(StringUtils.abbreviateMiddle("(U) " + lib, "[...]", maxWidth)).append("\n");
    }
    if (!unknown.isEmpty()) {
      b.append("\n");
    }
    for (JavaLibrary lib : unknown) {
      b.append(StringUtils.abbreviateMiddle("(?) " + lib, "[...]", maxWidth)).append("\n");
    }
    if (!deprecated.isEmpty()) {
      b.append("\n");
    }
    for (JavaLibrary lib : deprecated) {
      b.append(StringUtils.abbreviateMiddle("(-) " + lib, "[...]", maxWidth)).append("\n");
    }
    if (!ignored.isEmpty()) {
      b.append("\n");
    }
    for (File file : ignored) {
      b.append(StringUtils.abbreviateMiddle("(X) " + file.getAbsolutePath(), "[...]", maxWidth)).append("\n");
    }
    b.append("\n");
    b.append(StringUtils.rightPad("", maxWidth, "-")).append("\n");
    b.append("Added     : ").append(StringUtils.leftPad(String.valueOf(added.size()), 6)).append("\n");
    b.append("Changed   : ").append(StringUtils.leftPad(String.valueOf(changed.size()), 6)).append("\n");
    b.append("Deprecated: ").append(StringUtils.leftPad(String.valueOf(deprecated.size()), 6)).append("\n");
    b.append("Unknown   : ").append(StringUtils.leftPad(String.valueOf(unknown.size()), 6)).append("\n");
    b.append("Snapshots : ").append(StringUtils.leftPad(String.valueOf(detectedSnapshots), 6)).append("\n");
    b.append("Ignored   : ").append(StringUtils.leftPad(String.valueOf(ignored.size()), 6)).append("\n");
    b.append("Unchanged : ").append(StringUtils.leftPad(String.valueOf(unchanged.size()), 6)).append("\n");
    b.append(StringUtils.rightPad("", maxWidth, "-")).append("\n");
    return b.toString();
  }

}
