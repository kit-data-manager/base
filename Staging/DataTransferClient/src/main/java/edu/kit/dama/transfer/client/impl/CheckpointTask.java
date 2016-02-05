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
package edu.kit.dama.transfer.client.impl;

import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class CheckpointTask extends TimerTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(GenericIngestClient.class);
  /**
   * The client to monitor.
   */
  private AbstractTransferClient client = null;

  /**
   * Default constructor.
   *
   * @param pClient The client to monitor.
   */
  public CheckpointTask(AbstractTransferClient pClient) {
    client = pClient;
  }

  @Override
  public final void run() {
    if (client != null) {
      LOGGER.debug("Creating new checkpoint for client with ID {}", client.getTransferTaskContainer().getUniqueTransferIdentifier());
      client.createCheckpoint();
    }
  }
}
