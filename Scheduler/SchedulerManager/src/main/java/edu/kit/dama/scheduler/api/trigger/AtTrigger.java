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
import edu.kit.dama.scheduler.api.impl.test.SimpleAtTrigger;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

import javax.xml.bind.annotation.*;
import java.util.Date;

/**
 * Root interface of all trigger adapter.
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
@XmlSeeAlso({SimpleAtTrigger.class, QuartzAtTrigger.class})
public abstract class AtTrigger extends JobTrigger {

    private Date startDate;

    /**
     * Sets the trigger start date.
     *
     * @param startDate the trigger start date.
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Gets the trigger start date.
     *
     * @return the start date of this trigger.
     */
    public Date getStartDate() {
        return startDate;
    }
}
