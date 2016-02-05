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
package edu.kit.dama.rest.base.types;

import edu.kit.dama.rest.util.CheckServiceHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *
 * @author mf6319
 */
public class CheckServiceResponse {

  private String serviceName;
  private ServiceStatus serviceStatus;
  private String dateTime;
  private List<ModuleInformation> modules;

  /**
   * Default constructor.
   */
  public CheckServiceResponse() {
  }

  /**
   * Default constructor.
   *
   * @param serviceName The name of the service that is checked.
   * @param serviceStatus The status of the service.
   */
  public CheckServiceResponse(String serviceName, ServiceStatus serviceStatus) {
    this.serviceName = serviceName;
    this.serviceStatus = serviceStatus;
    this.dateTime = SimpleDateFormat.getDateTimeInstance().format(new Date());
    this.modules = CheckServiceHelper.getModuleInformation();
  }

  /**
   * Set the service name.
   *
   * @param serviceName The service name.
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * Get the service name.
   *
   * @return The service name.
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Set the service status.
   *
   * @param serviceStatus The service status.
   */
  public void setServiceStatus(ServiceStatus serviceStatus) {
    this.serviceStatus = serviceStatus;
  }

  /**
   * Get the service status.
   *
   * @return The service status.
   */
  public ServiceStatus getServiceStatus() {
    return serviceStatus;
  }

  /**
   * Set the current date/time.
   *
   * @param dateTime The current date/time.
   */
  void setDateTime(String dateTime) {
    this.dateTime = dateTime;
  }

  /**
   * Get the current date/time.
   *
   * @return The current date/time.
   */
  public String getDateTime() {
    return dateTime;
  }

  /**
   * Set the list of modules.
   *
   * @param modules The list of modules.
   */
  void setModules(List<ModuleInformation> modules) {
    this.modules = modules;
  }

  /**
   * Get the list of modules.
   *
   * @return The list of modules.
   */
  public List<ModuleInformation> getModules() {
    return modules;
  }
}
