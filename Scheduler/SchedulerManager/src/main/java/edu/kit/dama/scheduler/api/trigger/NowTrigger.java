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

import edu.kit.dama.scheduler.api.impl.QuartzNowTrigger;
import edu.kit.dama.scheduler.api.impl.test.SimpleNowTrigger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

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
                @XmlNamedAttributeNode(value = "id"),
                @XmlNamedAttributeNode(value = "name"),
                @XmlNamedAttributeNode(value = "triggerGroup"),
                @XmlNamedAttributeNode(value = "priority"),
                @XmlNamedAttributeNode(value = "description"),
                @XmlNamedAttributeNode(value = "nextFireTime")
            }
    )
})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({SimpleNowTrigger.class, QuartzNowTrigger.class})
public abstract class NowTrigger extends JobTrigger {
}
