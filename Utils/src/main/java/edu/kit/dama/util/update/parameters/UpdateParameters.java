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
package edu.kit.dama.util.update.parameters;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import edu.kit.dama.util.update.JarDiff;
import edu.kit.jcommander.generic.parameter.CommandLineParameters;
import edu.kit.jcommander.generic.status.CommandStatus;
import java.io.File;

/**
 * Parameter wrapper for the JarDiff tool.
 *
 * @author mf6319
 */
@Parameters(commandNames = "update", commandDescription = "Check for updated libraries by comparing two library folders of the same application, one for the current version and one for a new version.")
public class UpdateParameters extends CommandLineParameters {

  /**
   * The absolute folder containing the current version libraries.
   */
  @Parameter(names = {"-c", "--currentVersion"}, description = "The current application version's library folder.", required = true)
  public File currentDirectory;

  /**
   * The absolute folder containing the new version libraries.
   */
  @Parameter(names = {"-n", "--newVersion"}, description = "The new application version's library folder.", required = true)
  public File newDirectory;

  /**
   * The destingation folder where an update script is stored. If this argument
   * is not provided, the results are only print to stdout.
   */
  @Parameter(names = {"-s", "--scriptDestination"}, description = "The output folder for the update script if 'printOnly' is not set.", required = false)
  public File scriptDestination;

  /**
   * Default constructor.
   */
  public UpdateParameters() {
    super("update");
  }

  @Override
  public CommandStatus executeCommand() {
    return JarDiff.update(this);
  }
}
