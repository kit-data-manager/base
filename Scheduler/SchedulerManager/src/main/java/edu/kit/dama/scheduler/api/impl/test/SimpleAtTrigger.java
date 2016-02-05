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
package edu.kit.dama.scheduler.api.impl.test;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

import edu.kit.dama.scheduler.api.trigger.AtTrigger;

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
                @XmlNamedAttributeNode("id"),
                @XmlNamedAttributeNode("name"),
                @XmlNamedAttributeNode("triggerGroup"),
                @XmlNamedAttributeNode("priority"),
                @XmlNamedAttributeNode("description"),
                @XmlNamedAttributeNode("nextFireTime"),
                @XmlNamedAttributeNode("startDate")
            }
    )
})
@XmlAccessorType(XmlAccessType.FIELD)
public class SimpleAtTrigger extends AtTrigger {

    private Date startDate;
    private Date nextFireTime;

    /**
     * Default constructor.
     */
    public SimpleAtTrigger() {
    }

    @Override
    public void setStartDate(Date startDate) {
        this.startDate = (startDate != null) ? (Date) startDate.clone() : null;
    }

    @Override
    public Date getStartDate() {
        return (this.startDate != null) ? (Date) this.startDate.clone() : null;
    }

    public void setNextFireTime(Date nextFireTime) {
        this.nextFireTime = (nextFireTime != null) ? (Date) nextFireTime.clone() : null;
    }

    @Override
    public Date getNextFireTime() {
        return (this.nextFireTime != null) ? (Date) this.nextFireTime.clone() : null;
    }
}
