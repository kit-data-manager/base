/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.mdm.content;

import edu.kit.dama.mdm.base.MetaDataSchema;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 *
 * @author mf6319
 */
@Entity
public class MetadataIndexingTask implements Serializable {

  /**
   * UID should be the date of the last change in the format yyyyMMdd.
   */
  private static final long serialVersionUID = 20140425L;

  /**
   * Primary key of the schema.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  /**
   * The location of the metadata document. Currently, this location should be
   * locally accessible. Later remote locations may be supported.
   */
  private String metadataDocumentUrl = null;
  /**
   * The id of the user who scheduled the task.
   */
  private String ownerId = null;
  /**
   * The id of the group in which context the task was scheduled. This
   * information may be used to decide which index will be used.
   */
  private String groupId = null;

  /**
   * The id of the associated digital object.
   */
  private String digitalObjectId = null;

  /**
   * The reference to the used schema.
   */
  @OneToOne(targetEntity = MetaDataSchema.class, fetch = FetchType.EAGER)
  private MetaDataSchema schemaReference = null;
  /**
   * The timestamp when the task was scheduled.
   */
  private long scheduleTimestamp = 0l;
  /**
   * The timestamp when the task was successfully finished.
   */
  private long finishTimestamp = -1l;
  /**
   * The timestamp when the last error occured.
   */
  private long lastErrorTimestamp = -1l;
  /**
   * The number of failed attempts.
   */
  private int failCount = 0;

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the metadataDocumentUrl
   */
  public String getMetadataDocumentUrl() {
    return metadataDocumentUrl;
  }

  /**
   * @param metadataDocumentUrl the metadataDocumentUrl to set
   */
  public void setMetadataDocumentUrl(String metadataDocumentUrl) {
    this.metadataDocumentUrl = metadataDocumentUrl;
  }

  /**
   * @return the ownerId
   */
  public String getOwnerId() {
    return ownerId;
  }

  /**
   * @param ownerId the ownerId to set
   */
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * @return the groupId
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * @param groupId the groupId to set
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * @return the digitalObjectId
   */
  public String getDigitalObjectId() {
    return digitalObjectId;
  }

  /**
   * @param digitalObjectId the digitalObjectId to set
   */
  public void setDigitalObjectId(String digitalObjectId) {
    this.digitalObjectId = digitalObjectId;
  }

  /**
   * @return the schemaReference
   */
  public MetaDataSchema getSchemaReference() {
    return schemaReference;
  }

  /**
   * @param schemaReference the schemaReference to set
   */
  public void setSchemaReference(MetaDataSchema schemaReference) {
    this.schemaReference = schemaReference;
  }

  /**
   * @return the scheduleTimestamp
   */
  public long getScheduleTimestamp() {
    return scheduleTimestamp;
  }

  /**
   * @param scheduleTimestamp the scheduleTimestamp to set
   */
  public void setScheduleTimestamp(long scheduleTimestamp) {
    this.scheduleTimestamp = scheduleTimestamp;
  }

  /**
   * @return the lastErrorTimestamp
   */
  public long getLastErrorTimestamp() {
    return lastErrorTimestamp;
  }

  /**
   * @param lastErrorTimestamp the lastErrorTimestamp to set
   */
  public void setLastErrorTimestamp(long lastErrorTimestamp) {
    this.lastErrorTimestamp = lastErrorTimestamp;
  }

  /**
   * @return the finishTimestamp
   */
  public long getFinishTimestamp() {
    return finishTimestamp;
  }

  /**
   * @param finishTimestamp the finishTimestamp to set
   */
  public void setFinishTimestamp(long finishTimestamp) {
    this.finishTimestamp = finishTimestamp;
  }

  /**
   * @return the failCount
   */
  public int getFailCount() {
    return failCount;
  }

  /**
   * @param failCount the failCount to set
   */
  public void setFailCount(int failCount) {
    this.failCount = failCount;
  }
}
