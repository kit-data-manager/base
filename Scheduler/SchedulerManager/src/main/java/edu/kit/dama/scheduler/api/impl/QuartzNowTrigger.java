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

import edu.kit.dama.scheduler.api.trigger.NowTrigger;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
public class QuartzNowTrigger extends NowTrigger {

    private Date nextFireTime;

    /**
     * Default constructor.
     */
    public QuartzNowTrigger() {
    }

    /**
     * A constructor used to create a QuartzNowTrigger based on the provided
     * SimpleTrigger.
     *
     * @param simpleTrigger the simple trigger.
     */
    public QuartzNowTrigger(SimpleTrigger simpleTrigger) {
        setId(simpleTrigger.getKey().toString());
        setName(simpleTrigger.getKey().getName());
        setTriggerGroup(simpleTrigger.getKey().getGroup());
        setPriority(simpleTrigger.getPriority());
        setDescription(simpleTrigger.getDescription());
        this.nextFireTime = simpleTrigger.getNextFireTime();
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
        triggerBuilder.startNow();

        return triggerBuilder;
    }
}
