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
package edu.kit.dama.mdm.dataworkflow.properties;

import java.io.Serializable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * LinuxSoftwareMap property defined according to the LSM template definition
 * (see http://ibiblio.org/pub/linux/LSM-TEMPLATE.html). It can be used to
 * descibe a software component including version, download location and
 * resposibilities. Mandatory fields according to the definition are: title,
 * version, description, enteredDate, author and primarySite. Currently, the
 * existance of these fields is not checked by this implementation.
 *
 * @author mf6319
 */
@Entity
@DiscriminatorValue("LinuxSoftwareMap")
public class LinuxSoftwareMapProperty extends ExecutionEnvironmentProperty implements Serializable {

  private static final long serialVersionUID = -742294652672377914L;
  private String version;
  private long enteredDate;
  private String keywords;
  private String author;
  private String maintainedBy;
  private String primarySite;
  private String alternateSite;
  private String originalSite;
  private String platforms;
  private String copyingPolicy;

  public LinuxSoftwareMapProperty() {
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return super.getName();
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    setName(title);
  }

  /**
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * @param version the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return the enteredDate
   */
  public long getEnteredDate() {
    return enteredDate;
  }

  /**
   * @param enteredDate the enteredDate to set
   */
  public void setEnteredDate(long enteredDate) {
    this.enteredDate = enteredDate;
  }

  /**
   * @return the keywords
   */
  public String getKeywords() {
    return keywords;
  }

  /**
   * @param keywords the keywords to set
   */
  public void setKeywords(String keywords) {
    this.keywords = keywords;
  }

  /**
   * @return the author
   */
  public String getAuthor() {
    return author;
  }

  /**
   * @param author the author to set
   */
  public void setAuthor(String author) {
    this.author = author;
  }

  /**
   * @return the maintainedBy
   */
  public String getMaintainedBy() {
    return maintainedBy;
  }

  /**
   * @param maintainedBy the maintainedBy to set
   */
  public void setMaintainedBy(String maintainedBy) {
    this.maintainedBy = maintainedBy;
  }

  /**
   * @return the primarySite
   */
  public String getPrimarySite() {
    return primarySite;
  }

  /**
   * @param primarySite the primarySite to set
   */
  public void setPrimarySite(String primarySite) {
    this.primarySite = primarySite;
  }

  /**
   * @return the alternateSite
   */
  public String getAlternateSite() {
    return alternateSite;
  }

  /**
   * @param alternateSite the alternateSite to set
   */
  public void setAlternateSite(String alternateSite) {
    this.alternateSite = alternateSite;
  }

  /**
   * @return the originalSite
   */
  public String getOriginalSite() {
    return originalSite;
  }

  /**
   * @param originalSite the originalSite to set
   */
  public void setOriginalSite(String originalSite) {
    this.originalSite = originalSite;
  }

  /**
   * @return the platforms
   */
  public String getPlatforms() {
    return platforms;
  }

  /**
   * @param platforms the platforms to set
   */
  public void setPlatforms(String platforms) {
    this.platforms = platforms;
  }

  /**
   * @return the copyingPolicy
   */
  public String getCopyingPolicy() {
    return copyingPolicy;
  }

  /**
   * @param copyingPolicy the copyingPolicy to set
   */
  public void setCopyingPolicy(String copyingPolicy) {
    this.copyingPolicy = copyingPolicy;
  }
}
