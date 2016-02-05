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
package edu.kit.dama.rest.scheduler.types;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

import edu.kit.dama.rest.base.AbstractEntityWrapper;
import edu.kit.dama.scheduler.api.trigger.JobTrigger;

/**
 *
 * @author mf6319
 */
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode(value = "count"),
                @XmlNamedAttributeNode(value = "entities", subgraph = "simple")
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode(value = "count"),
                @XmlNamedAttributeNode(value = "entities", subgraph = "default")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "triggerWrapper")
public final class TriggerWrapper extends AbstractEntityWrapper<JobTrigger> {

    @XmlElementWrapper(name = "triggers")
    @XmlElement(name = "trigger", type = JobTrigger.class, namespace = "http://ipe.kit.edu/2015/KITDataManager/Scheduler")
    private List<JobTrigger> entities;

    /**
     * Default constructor.
     */
    public TriggerWrapper() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param entities An array of entities.
     */
    public TriggerWrapper(JobTrigger... entities) {
        super(entities);
    }

    /**
     * Default constructor.
     *
     * @param entities A list of entities.
     */
    public TriggerWrapper(List<JobTrigger> entities) {
        super(entities);
    }

    /**
     * Default constructor.
     *
     * @param count The number of affected entities.
     */
    public TriggerWrapper(Integer count) {
        super(count);
    }

    @Override
    public void setEntities(List<JobTrigger> pEntities) {
        this.entities = pEntities;
    }

    @Override
    public List<JobTrigger> getEntities() {
        return this.entities;
    }
}
