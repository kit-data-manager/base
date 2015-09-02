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

/**
 *
 * @author mf6319
 */
public class ModuleInformation {

  private String name;
  private String version;
  private String build;
  private String buildDate;

  /**
   * Default constructor.
   */
  public ModuleInformation() {
  }

  /**
   * Default constructor.
   *
   * @param name The module name.
   * @param version The module version.
   * @param build The module build number.
   * @param buildDate The build date.
   */
  public ModuleInformation(String name, String version, String build, String buildDate) {
    this.name = name;
    this.version = version;
    this.build = build;
    this.buildDate = buildDate;
  }

  /**
   * Set the module name.
   *
   * @param name The module name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Get the module name.
   *
   * @return The module name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set the module version.
   *
   * @param version The module version.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Get the module version.
   *
   * @return The module version.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Set the module build number.
   *
   * @param build The module build number.
   */
  public void setBuild(String build) {
    this.build = build;
  }

  /**
   * Get the module build number.
   *
   * @return The module build number.
   */
  public String getBuild() {
    return build;
  }

  /**
   * Set the module build date.
   *
   * @param buildDate The module build date.
   */
  public void setBuildDate(String buildDate) {
    this.buildDate = buildDate;
  }

  /**
   * Get the module build date.
   *
   * @return The module build date.
   */
  public String getBuildDate() {
    return buildDate;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    b.append(name).append(" v").append(version).append(" (build").append(build).append(")");
    return b.toString();
  }

}
