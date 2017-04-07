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
import edu.kit.dama.scheduler.api.schedule.SimpleSchedule;

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
@XmlRootElement(name = "scheduleWrapper")
public final class ScheduleWrapper extends AbstractEntityWrapper<SimpleSchedule> {

    @XmlElementWrapper(name = "schedules")
    @XmlElement(name = "schedule")
    private List<SimpleSchedule> entities;

    /**
     * Default constructor.
     */
    public ScheduleWrapper() {
        super();
    }

    /**
     * Default constructor.
     *
     * @param entities an array of entities.
     */
    public ScheduleWrapper(SimpleSchedule... entities) {
        super(entities);
    }

    /**
     * Default constructor.
     *
     * @param entities a list of entities.
     */
    public ScheduleWrapper(List<SimpleSchedule> entities) {
        super(entities);
    }

    /**
     * Default constructor.
     *
     * @param count the number of affected entities.
     */
    public ScheduleWrapper(Integer count) {
        super(count);
    }

    @Override
    public void setEntities(List<SimpleSchedule> pEntities) {
        this.entities = pEntities;
    }

    @Override
    public List<SimpleSchedule> getEntities() {
        return this.entities;
    }
}
