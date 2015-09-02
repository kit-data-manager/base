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
package edu.kit.dama.dataworkflow.client.parameters;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import edu.kit.dama.client.status.CommandStatus;
import edu.kit.dama.cmdline.generic.parameter.CommandLineParameters;
import edu.kit.dama.dataworkflow.client.GenericSubmissionClient;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mf6319
 */
@Parameters(commandNames = "process", commandDescription = "Process open DataWorkflow tasks.")
public class ProcessParameters extends CommandLineParameters {

  /**
   * List of task ids to handle. If none are provided all open tasks are
   * handled.
   */
  @Parameter(names = {"-t", "--taskIds"}, description = "One or more task ids that should be processed.", validateWith = TaskIdParameterValidator.class, variableArity = true, required = false)
  public List<Long> taskIds = new ArrayList<>();

  /**
   * Maximum number of task that will be processed within one call.
   */
  @Parameter(names = {"-c", "--count"}, description = "Maximum number of processed tasks. This option has no impact if option --taskIds is provided. (default: 10)", required = false)
  public int count = 10;

  /**
   * List open tasks without any processing.
   */
  @Parameter(names = {"-l", "--listOnly"}, description = "Only list open tasks and their current status by their processing order.", required = false)
  public boolean listOnly = false;

  /**
   * Parameter for output messages.
   */
  @Parameter(names = {"-v", "--verbose"}, description = "Show detailed output in listOnly mode.", required = false)
  public boolean verbose = false;

  /**
   * Default constructor.
   */
  public ProcessParameters() {
    super("process");
    this.listOnly = false;
  }

  @Override
  public CommandStatus executeCommand() {
    return GenericSubmissionClient.processTasks(this);
  }

}
