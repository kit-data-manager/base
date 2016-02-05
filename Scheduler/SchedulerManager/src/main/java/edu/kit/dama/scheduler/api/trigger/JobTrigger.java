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
package edu.kit.dama.scheduler.api.trigger;

import edu.kit.dama.scheduler.api.impl.QuartzAtTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzExpressionTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzIntervalTrigger;
import edu.kit.dama.scheduler.api.impl.QuartzNowTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleAtTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleDelayTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleExpressionTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleIntervalTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleNowTrigger;
import java.io.Serializable;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({ExpressionTrigger.class,
    IntervalTrigger.class, DelayTrigger.class, NowTrigger.class, AtTrigger.class,
    SimpleDelayTrigger.class, SimpleExpressionTrigger.class, SimpleIntervalTrigger.class,
    SimpleNowTrigger.class, SimpleAtTrigger.class, QuartzExpressionTrigger.class,
    QuartzIntervalTrigger.class, QuartzNowTrigger.class, QuartzAtTrigger.class})
public abstract class JobTrigger implements Serializable{

    private String id;
    private String name;
    private String triggerGroup;
    private int priority;
    private String description;

    /**
     * Sets the trigger id.
     *
     * @param id the trigger id.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the trigger id.
     *
     * @return the id of this trigger.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the trigger name.
     *
     * @param name the trigger name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the trigger name.
     *
     * @return the name of this trigger.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the trigger group.
     *
     * @param group the trigger group.
     */
    public void setTriggerGroup(String group) {
        this.triggerGroup = group;
    }

    /**
     * Gets the trigger group.
     *
     * @return the group of this trigger.
     */
    public String getTriggerGroup() {
        return triggerGroup;
    }

    /**
     * Sets the trigger priority.
     *
     * @param priority the trigger priority.
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Gets the trigger priority.
     *
     * @return the priority of this trigger.
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Sets the trigger description.
     *
     * @param description the trigger description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the trigger description.
     *
     * @return the description of this trigger.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the trigger next fire time.
     *
     * @return the next fire time of this trigger.
     */
    public abstract Date getNextFireTime();
}
