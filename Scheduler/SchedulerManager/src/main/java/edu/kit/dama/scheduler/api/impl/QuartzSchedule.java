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
package edu.kit.dama.scheduler.api.impl;

import edu.kit.dama.scheduler.SchedulerManagerException;
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;

/**
 * Adapter class for the class <code>org.quartz.JobDetail</code>.
 *
 * @author wq7203
 */
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode(value = "id")
            }
    ),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode(value = "id"),
                @XmlNamedAttributeNode(value = "name"),
                @XmlNamedAttributeNode(value = "scheduleGroup"),
                @XmlNamedAttributeNode(value = "description"),
                @XmlNamedAttributeNode(value = "jobClass"),
                @XmlNamedAttributeNode(value = "jobParameters")
            }
    )
})
@XmlAccessorType(XmlAccessType.FIELD)
public class QuartzSchedule extends SimpleSchedule {

    /**
     * Default constructor.
     */
    public QuartzSchedule() {
        super(null);
    }

    /**
     * Default constructor.
     *
     * @param pName The schedule name.
     */
    public QuartzSchedule(String pName) {
        super(pName);
    }

    /**
     * A constructor used to create a QuartzSchedule based on the provided
     * JobDetail.
     *
     * @param jobDetail the job detail.
     */
    public QuartzSchedule(JobDetail jobDetail) {
        super(jobDetail.getKey().getName());
        setId(jobDetail.getKey().toString());
        setScheduleGroup(jobDetail.getKey().getGroup());
        setDescription(jobDetail.getDescription());
        setJobClass(jobDetail.getJobClass().getName());
        setJobParameters(jobDetail.getJobDataMap().getString("jobParameters"));
    }

    @Override
    public final void setId(String id) {
        super.setId(id);
    }

    @Override
    public final void setScheduleGroup(String group) {
        super.setScheduleGroup(group);
    }

    @Override
    public final void setDescription(String description) {
        super.setDescription(description);
    }

    @Override
    public final void setJobClass(String jobClass) {
        super.setJobClass(jobClass);
    }

    @Override
    public final void setJobParameters(String jobParameters) {
        super.setJobParameters(jobParameters);
    }

    /**
     * Builds a new JobDetail instance based on this object.
     *
     * @return a JobBuilder instance based on this object.
     */
    public JobBuilder build() {
        Class clazz;
        try {
            clazz = Class.forName(getJobClass());
        } catch (ClassNotFoundException ex) {
            throw new SchedulerManagerException("Class '" + getJobClass() + "' not found.", ex);
        }
        JobBuilder jobBuilder = JobBuilder.newJob().
                withIdentity(getName(), getScheduleGroup()).
                withDescription(getDescription()).
                ofType(clazz).
                usingJobData("jobParameters", getJobParameters());

        return jobBuilder;
    }
}
