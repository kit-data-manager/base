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
package edu.kit.dama.util.test;

import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class RestTestClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestTestClient.class);

  public static void main(String[] args) throws Exception {
    String accessKey = "admin";
    String accessSecret = "dama14";
    SimpleRESTContext context = new SimpleRESTContext(accessKey, accessSecret);

    //  BaseMetaDataRestClient client = new BaseMetaDataRestClient("http://ipesuco1.ipe.kit.edu:8080/KITDM/rest/basemetadata/", context);
    BaseMetaDataRestClient client = new BaseMetaDataRestClient("http://localhost:8080/KITDM/rest/basemetadata/", context);

    DigitalObject o1 = new DigitalObject();
    o1.setBaseId(1l);
    System.out.println(client.getDigitalObjectDerivationInformation(o1, "USERS").getEntities().get(0));
  }
}
