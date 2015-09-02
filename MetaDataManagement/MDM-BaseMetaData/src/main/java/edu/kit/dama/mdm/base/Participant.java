/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.base;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 * Link an user with a specific task.
 *
 * @author hartmann-v
 */
@Entity
@XmlNamedObjectGraphs({
  @XmlNamedObjectGraph(
          name = "simple",
          attributeNodes = {
            @XmlNamedAttributeNode("participantId")
          }),
  @XmlNamedObjectGraph(
          name = "default",
          attributeNodes = {
            @XmlNamedAttributeNode("participantId"),
            @XmlNamedAttributeNode(value = "task", subgraph = "simple"),
            @XmlNamedAttributeNode(value = "user", subgraph = "simple")
          })})
@XmlAccessorType(XmlAccessType.FIELD)
public class Participant implements Serializable {

  /**
   * UID should be the date of the last change in the format yyyyMMdd.
   */
  private static final long serialVersionUID = 20111201L;
  // <editor-fold defaultstate="collapsed" desc="declaration of variables">
  /**
   * Identification number of the user. primary key of the data set.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long participantId;
  /**
   * Task of the user.
   */
  @ManyToOne(fetch = FetchType.EAGER)
  private Task task;
  /**
   * UserData.
   */
  @ManyToOne(fetch = FetchType.EAGER)
  private UserData user;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="constructors">
  /**
   * Make default constructor inaccessible for others.
   */
  protected Participant() {
    // this constructor is useless so it's forbidden for the users.
  }

  /**
   * Build participant without any defined task.
   *
   * @param user user linked in this relation.
   */
  public Participant(UserData user) {
    this(user, null);
  }

  /**
   * Build participant.
   *
   * @param user user linked in this relation.
   * @param task task of the user.
   */
  public Participant(UserData user, Task task) {
    this.user = user;
    this.task = task;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="setters and getters">
  /**
   * Get participant id.
   *
   * @return the participantId
   */
  public Long getParticipantId() {
    return participantId;
  }

  /**
   * Set participant id.
   *
   * @param participantId the participant id to set
   */
  public void setParticipantId(final Long participantId) {
    this.participantId = participantId;
  }

  /**
   * Get task.
   *
   * @return the task
   */
  public Task getTask() {
    return task;
  }

  /**
   * Set task.
   *
   * @param task the task to set
   */
  public void setTask(final Task task) {
    this.task = task;
  }

  /**
   * The user of this relation.
   *
   * @return the organizationUnit
   */
  public UserData getUser() {
    return user;
  }

  /**
   * Set the user of this relation.
   *
   * @param user the user to set
   */
  public void setUser(UserData user) {
    this.user = user;
  }
  // </editor-fold>

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    if (user != null) {
      buffer.append(user.getLastName()).append(", ");
      buffer.append(user.getFirstName());
      buffer.append(" (").append(user.getEmail()).append(")");
    }
    if (task != null) {
      buffer.append(" - ").append(task.getTask());
    }
    return buffer.toString();
  }

  @Override
  public boolean equals(Object other) {
    boolean equals = true;
    if (this == other) {
      return equals;
    }
    if (other != null && (getClass() == other.getClass())) {
      Participant otherParticipant = (Participant) other;
      if (participantId != null) {
        equals = equals && (participantId.equals(otherParticipant.participantId));
      } else {
        equals = equals && (otherParticipant.participantId == null);
      }
      if (equals && (task != null)) {
        equals = equals && (task.equals(otherParticipant.task));
      } else {
        equals = equals && (otherParticipant.task == null);
      }
      if (equals && (user != null)) {
        equals = equals && (user.equals(otherParticipant.user));
      } else {
        equals = equals && (otherParticipant.user == null);
      }
    } else {
      equals = false;
    }

    return equals;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 83 * hash + (this.task != null ? this.task.hashCode() : 0);
    hash = 83 * hash + (this.user != null ? this.user.hashCode() : 0);
    return hash;
  }
}
