/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.mdm.base;

import edu.kit.dama.mdm.base.interfaces.IDefaultTask;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 * All possible tasks of an organization unit or a user. Task depends on
 * context. Nevertheless there will be a limited number of possible tasks.
 *
 * @author hartmann-v
 */
@Entity
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("taskId")
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("taskId"),
                @XmlNamedAttributeNode("task")})
})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "Task.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("taskId")}),
    @NamedEntityGraph(
            name = "Task.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("taskId"),
                @NamedAttributeNode("task")
            })
})
public class Task implements Serializable, IDefaultTask, FetchGroupTracker {

    /**
     * UID should be the date of the last change in the format yyyyMMdd.
     */
    private static final long serialVersionUID = 20111201L;
    // <editor-fold defaultstate="collapsed" desc="declaration of variables">
    /**
     * Identification number of the task. primary key of the data set.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;
    /**
     * Task. Task depends on participant (organization unit/user) and the
     * context.
     */
    private String task;
  // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="constructors">
    /**
     * Default constructor. Not accessible from outside.
     */
    protected Task() {
        // this constructor is useless so it's forbidden for the users.
    }

    /**
     * Create a new task.
     *
     * @param task task.
     */
    public Task(String task) {
        this.task = task;
    }
  // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="setters and getters">
    @Override
    public Long getTaskId() {
        return taskId;
    }

    /**
     * Set task id.
     *
     * @param taskId the task id to set
     */
    public void setTaskId(final Long taskId) {
        this.taskId = taskId;
    }

    @Override
    public String getTask() {
        return task;
    }

    /**
     * Set task.
     *
     * @param task the task to set
     */
    public void setTask(final String task) {
        this.task = task;
    }
    // </editor-fold>

    @Override
    public String toString() {
        return "#" + getTaskId() + " - " + getTask();
    }

    @Override
    public boolean equals(Object other) {
        boolean equals = true;
        if (this == other) {
            return equals;
        }
        if (other != null && (getClass() == other.getClass())) {
            Task otherTask = (Task) other;
            if (taskId != null) {
                equals = equals && (taskId.equals(otherTask.taskId));
            } else {
                equals = equals && (otherTask.taskId == null);
            }
            if (equals && (task != null)) {
                equals = equals && (task.equals(otherTask.task));
            } else {
                equals = equals && (otherTask.task == null);
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
        return hash;
    }

    private transient org.eclipse.persistence.queries.FetchGroup fg;
    private transient Session sn;

    @Override
    public org.eclipse.persistence.queries.FetchGroup _persistence_getFetchGroup() {
        return this.fg;
    }

    @Override
    public void _persistence_setFetchGroup(org.eclipse.persistence.queries.FetchGroup fg) {
        this.fg = fg;
    }

    @Override
    public boolean _persistence_isAttributeFetched(String string) {
        return true;
    }

    @Override
    public void _persistence_resetFetchGroup() {
    }

    @Override
    public boolean _persistence_shouldRefreshFetchGroup() {
        return false;
    }

    @Override
    public void _persistence_setShouldRefreshFetchGroup(boolean bln) {

    }

    @Override
    public Session _persistence_getSession() {

        return sn;
    }

    @Override
    public void _persistence_setSession(Session sn) {
        this.sn = sn;

    }
}
