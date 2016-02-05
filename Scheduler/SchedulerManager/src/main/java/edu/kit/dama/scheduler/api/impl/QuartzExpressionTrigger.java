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

import edu.kit.dama.scheduler.api.trigger.ExpressionTrigger;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
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
                @XmlNamedAttributeNode(value = "expression")
            }
    )
})
@XmlAccessorType(XmlAccessType.FIELD)
public class QuartzExpressionTrigger extends ExpressionTrigger {

    @XmlTransient
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzExpressionTrigger.class);

    private Date nextFireTime;

    /**
     * Default constructor.
     */
    public QuartzExpressionTrigger() {
    }

    /**
     * A constructor used to create a QuartzExpressionTrigger based on the
     * provided CronTrigger.
     *
     * @param cronTrigger the cron trigger.
     */
    public QuartzExpressionTrigger(CronTrigger cronTrigger) {
        setId(cronTrigger.getKey().toString());
        setName(cronTrigger.getKey().getName());
        setTriggerGroup(cronTrigger.getKey().getGroup());
        setPriority(cronTrigger.getPriority());
        setDescription(cronTrigger.getDescription());
        setStartDate(cronTrigger.getStartTime());
        setEndDate(cronTrigger.getEndTime());
        setExpression(cronTrigger.getCronExpression());
        this.nextFireTime = cronTrigger.getNextFireTime();
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

        triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(getExpression()));

        return triggerBuilder;
    }
}
