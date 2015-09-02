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
package edu.kit.dama.util;

/**
 *
 * @author jejkal
 */
public final class Constants {

  /**
   * General constants
   */
  public static final String REST_API_SERVICE_KEY = "restServiceAccess";
  public static final String USERS_GROUP_ID = "USERS";

  /**
   * Staging folder name constants
   */
  public static final String STAGING_DATA_FOLDER_NAME = "data";
  public static final String STAGING_GENERATED_FOLDER_NAME = "generated";
  public static final String STAGING_SETTINGS_FOLDER_NAME = "settings";
  public final static String STAGING_DELETED_FILENAME = ".deleted";

  /**
   * MOxy XML graph constants
   */
  public static final String REST_SIMPLE_OBJECT_GRAPH = "simple";
  public static final String REST_DEFAULT_OBJECT_GRAPH = "default";
  public static final String REST_COMPLETE_OBJECT_GRAPH = "complete";

  /**
   * REST service constants
   */
  public final static String REST_ALL_INT = "-1";
  public final static String REST_DEFAULT_MIN_INDEX = "0";
  public final static String REST_DEFAULT_MAX_RESULTS = "10";

  public static final String REST_PARAMETER_GROUP_ID = "groupId";
  public static final String REST_PARAMETER_FIRST = "first";
  public static final String REST_PARAMETER_RESULT = "results";
  public static final String REST_PARAMETER_DESCRIPTION = "description";

  /**
   * DataOrganization constants
   */
  public static final String DEFAULT_VIEW = "default";

  //Authorization constants
  /**
   * The system administrator group id.
   */
  public static final String SYSTEM_GROUP = "SYS_ADMIN_GROUP";
  /**
   * The system administrator user id.
   */
  public static final String SYSTEM_ADMIN = "SYS_ADMIN";

  /**
   * Hidden constuctor.
   */
  private Constants() {
  }
}
