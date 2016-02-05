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
package edu.kit.dama.util.update;

import edu.kit.dama.util.update.types.JavaLibrary;
import edu.kit.dama.util.update.types.LibraryDiffInformation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Helper that allows to write an update bash script based on the provided
 * {@link LibraryDiffInformation} object. The script will cover the following
 * tasks:
 *
 * <ul>
 * <li>Delete all files marked as deprecated entries from the provided library
 * output folder.</li>
 * <li>Copy all files marked as updated entries to the provided library output
 * folder.</li>
 * <li>Copy all files marked as new entries to the provided library output
 * folder.</li>
 * <li>Print information about what has changed.</li>
 * </ul>
 *
 * The script itself is written to the provided script target folder.
 *
 * @author mf6319
 */
public final class UpdateScriptGenerator {

  /**
   * Generate an update script located in pScriptTargetFolder that will apply
   * changes defined in pDiffInfo to pLibraryOutputFolder.
   *
   * @param pScriptTargetFolder The folder where the resulting script is written
   * to.
   * @param pLibraryOutputFolder The folder containing libraries that should be
   * updated according to pDiffInfo.
   * @param pDiffInfo Collected changes between pLibraryOutputFolder and another
   * folder containing an updated set of libraries.
   *
   * @throws IOException If writing the script fails.
   */
  public static void generateUpdateScript(File pScriptTargetFolder, File pLibraryOutputFolder, LibraryDiffInformation pDiffInfo) throws IOException {
    List<JavaLibrary> deprecatedList = pDiffInfo.getDeprecatedEntries();
    StringBuilder b = new StringBuilder();
    int filesBefore = pLibraryOutputFolder.list().length;
    int removed = 0;
    int updated = 0;
    int added = 0;
    b.append("echo Removing ").append(deprecatedList.size()).append(" old libraries.\n");
    for (JavaLibrary lib : deprecatedList) {
      b.append("echo rm ").append(lib.getLocation().getAbsolutePath()).append("\n");
      b.append("rm ").append(lib.getLocation().getAbsolutePath()).append("\n");
      removed++;
    }
    List<JavaLibrary> updatedList = pDiffInfo.getChangedEntries();
    b.append("echo Adding ").append(updatedList.size()).append(" updated libraries.\n");
    for (JavaLibrary lib : updatedList) {
      b.append("echo cp ").append(lib.getLocation().getAbsolutePath()).append(" ").append(pLibraryOutputFolder.getAbsolutePath()).append("\n");
      b.append("cp ").append(lib.getLocation().getAbsolutePath()).append(" ").append(pLibraryOutputFolder.getAbsolutePath()).append("\n");
      updated++;
    }
    List<JavaLibrary> addedList = pDiffInfo.getAddedEntries();
    b.append("echo Adding ").append(addedList.size()).append(" new libraries.\n");
    for (JavaLibrary lib : addedList) {
      b.append("echo cp ").append(lib.getLocation().getAbsolutePath()).append(" ").append(pLibraryOutputFolder.getAbsolutePath()).append("\n");
      b.append("cp ").append(lib.getLocation().getAbsolutePath()).append(" ").append(pLibraryOutputFolder.getAbsolutePath()).append("\n");
      added++;
    }
    int filesAfter = pLibraryOutputFolder.list().length;
    b.append("echo Removed ").append(removed).append(" library(ies), updated ").append(updated).append(" library(ies), added ").append(added).append(" library(ies).\n");
    b.append("echo Output folder contains now ").append(filesAfter).append(" libraries (Before: ").append(filesBefore).append(").\n");
    b.append("echo Update done.\n");

    try (FileWriter w = new FileWriter(new File(pScriptTargetFolder, "update.sh"))) {
      w.write("#!/bin/bash\n");
      w.write(b.toString());
      w.flush();
    }
  }
}
