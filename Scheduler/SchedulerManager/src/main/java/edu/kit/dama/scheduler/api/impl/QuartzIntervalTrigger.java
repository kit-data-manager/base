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

import edu.kit.dama.scheduler.api.trigger.IntervalTrigger;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
                @XmlNamedAttributeNode(value = "triggerGroup"),
                @XmlNamedAttributeNode(value = "priority"),
                @XmlNamedAttributeNode(value = "description"),
                @XmlNamedAttributeNode(value = "nextFireTime"),
                @XmlNamedAttributeNode(value = "startDate"),
                @XmlNamedAttributeNode(value = "endDate"),
                @XmlNamedAttributeNode(value = "times"),
                @XmlNamedAttributeNode(value = "period")
            }
    )
})
@XmlAccessorType(XmlAccessType.FIELD)
public class QuartzIntervalTrigger extends IntervalTrigger {

    @XmlTransient
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzIntervalTrigger.class);

    private Date nextFireTime;

    /**
     * Default constructor.
     */
    public QuartzIntervalTrigger() {
    }

    /**
     * A constructor used to create a QuartzIntervalTrigger based on the
     * provided SimpleTrigger.
     *
     * @param simpleTrigger the simple trigger.
     */
    public QuartzIntervalTrigger(SimpleTrigger simpleTrigger) {
        setId(simpleTrigger.getKey().toString());
        setName(simpleTrigger.getKey().getName());
        setTriggerGroup(simpleTrigger.getKey().getGroup());
        setPriority(simpleTrigger.getPriority());
        setDescription(simpleTrigger.getDescription());
        setStartDate(simpleTrigger.getStartTime());
        setEndDate(simpleTrigger.getEndTime());
        setTimes((simpleTrigger.getRepeatCount() >= 0) ? simpleTrigger.getRepeatCount() + 1 : simpleTrigger.getRepeatCount());
        setPeriod(simpleTrigger.getRepeatInterval());
        this.nextFireTime = simpleTrigger.getNextFireTime();
    }

    @Override
    public void setInitialDelay(Long initialDelay) {
        LOGGER.warn("Method setInitialDelay(Long initialDelay) is not implemented for Class {}, this call has no effect.", getClass());
    }

    @Override
    public Long getInitialDelay() {
        LOGGER.info("Method getInitialDelay() is not implemented for Class {}.", getClass());
        return null;
    }

    @Override
    public Date getNextFireTime() {
        return (this.nextFireTime != null) ? (Date) this.nextFireTime.clone() : null;
    }

    /**
     * Builds a new TriggerBuilder instance based on this object.
     *
     * @return a TriggerBuilder instance based on this object.
     */
    public TriggerBuilder<? extends Trigger> build() {

        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();

        if (getName() != null) {
            triggerBuilder.withIdentity(getName(), getTriggerGroup());
        }

        if (getPriority() != null) {
            triggerBuilder.withPriority(getPriority());
        }

        triggerBuilder.withDescription(getDescription());

        if (getStartDate() != null) {
            triggerBuilder.startAt(getStartDate());
        } else {
            triggerBuilder.startNow();
        }

        if (getEndDate() != null) {
            triggerBuilder.endAt(getEndDate());
        }

        if (getTimes() == null || getTimes() == SimpleTrigger.REPEAT_INDEFINITELY) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(getPeriod()).repeatForever());

        } else if (getTimes() <= 0) {
            throw new IllegalArgumentException("Number of times must be > 0 or -1, use -1 for infinite.");
        } else {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(getPeriod()).withRepeatCount(getTimes() - 1));
        }

        return triggerBuilder;
    }
}
