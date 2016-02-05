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
package edu.kit.dama.scheduler.api.schedule;

import edu.kit.dama.scheduler.api.impl.QuartzSchedule;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({QuartzSchedule.class})
public abstract class SimpleSchedule {

    private String id;
    private String name;
    private String scheduleGroup;
    private String description;
    private String jobClass;
    private String jobParameters;

    public SimpleSchedule(String pName) {
        name = pName;
    }

    /**
     * Sets the schedule id.
     *
     * @param id the schedule id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the schedule id.
     *
     * @return the id of this schedule.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the schedule name.
     *
     * @param name the schedule name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the schedule name.
     *
     * @return the name of this schedule.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the schedule group.
     *
     * @param group the schedule group.
     */
    public void setScheduleGroup(String group) {
        this.scheduleGroup = group;
    }

    /**
     * Gets the schedule group.
     *
     * @return the group of this schedule.
     */
    public String getScheduleGroup() {
        return scheduleGroup;
    }

    /**
     * Sets the schedule description.
     *
     * @param description the schedule description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the schedule description.
     *
     * @return the description of this schedule.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the job class.
     *
     * @param jobClass the job class.
     */
    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
    }

    /**
     * Gets the job class.
     *
     * @return the class of this job.
     */
    public String getJobClass() {
        return jobClass;
    }

    /**
     * Gets the job parameters.
     *
     * @param jobParameters the job parameters.
     */
    public void setJobParameters(String jobParameters) {
        this.jobParameters = jobParameters;
    }

    /**
     * Gets the job parameters.
     *
     * @return the parameters of this job.
     */
    public String getJobParameters() {
        return jobParameters;
    }
}
