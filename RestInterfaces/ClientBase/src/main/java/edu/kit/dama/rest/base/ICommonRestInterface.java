/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.rest.base;

import com.qmino.miredot.annotations.ReturnType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Basic service interface providing method that should be available in all
 * services.
 *
 * @author mf6319
 */
public interface ICommonRestInterface {

  /**
   * Perform a simple service check by calling it. If the service is available,
   * status 200 should be returned.
   *
   * @summary Perform a simple service check.
   *
   * @return HTTP response with an according HTTP status code.
   */
  @GET
  @Path(value = "/checkService/")
  @Produces(MediaType.APPLICATION_JSON)
  @ReturnType("edu.kit.dama.rest.base.types.CheckServiceResponse")
  Response checkService();
}
