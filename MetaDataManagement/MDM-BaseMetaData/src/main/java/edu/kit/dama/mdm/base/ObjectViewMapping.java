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
package edu.kit.dama.mdm.base;

import edu.kit.dama.mdm.base.interfaces.IDefaultObjectViewMapping;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @author mf6319
 */
@Entity
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("id")
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                //id is not in because it is not needed
                @XmlNamedAttributeNode(value = "digitalObject", subgraph = "simple"),
                @XmlNamedAttributeNode("viewName")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "ObjectViewMapping.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")}),
    @NamedEntityGraph(
            name = "ObjectViewMapping.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("viewName"),
                @NamedAttributeNode(value = "digitalObject", subgraph = "ObjectViewMapping.default.DigitalObject.simple")},
            subgraphs = {
                @NamedSubgraph(
                        name = "ObjectViewMapping.default.DigitalObject.simple",
                        attributeNodes = {
                            @NamedAttributeNode("baseId")}
                )
            })
})
public class ObjectViewMapping implements Serializable, IDefaultObjectViewMapping, FetchGroupTracker {

    private static final long serialVersionUID = -6157665960655560658L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    private DigitalObject digitalObject;
    private String viewName;

    /**
     * Default constructor.
     */
    public ObjectViewMapping() {
    }

    /**
     * Default constructor.
     *
     * @param digitalObject The digital object.
     * @param viewName The view name.
     */
    public ObjectViewMapping(DigitalObject digitalObject, String viewName) {
        this.digitalObject = digitalObject;
        this.viewName = viewName;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public DigitalObject getDigitalObject() {
        return digitalObject;
    }

    /**
     * Set the digital object.
     *
     * @param digitalObject The digital object.
     */
    public void setDigitalObject(DigitalObject digitalObject) {
        this.digitalObject = digitalObject;
    }

    @Override
    public String getViewName() {
        return viewName;
    }

    /**
     * Set the view name.
     *
     * @param viewName The view name.
     */
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public String toString() {
        return getDigitalObject().getDigitalObjectIdentifier() + " / " + getViewName();
    }
    private transient org.eclipse.persistence.queries.FetchGroup fg;
    private transient Session sn;

    @Override
    public org.eclipse.persistence.queries.FetchGroup _persistence_getFetchGroup() {
        return this.fg;
    }

    @Override
    public void _persistence_setFetchGroup(org.eclipse.persistence.queries.FetchGroup fg) {
        this.fg = fg;
    }

    @Override
    public boolean _persistence_isAttributeFetched(String string) {
        return true;
    }

    @Override
    public void _persistence_resetFetchGroup() {
    }

    @Override
    public boolean _persistence_shouldRefreshFetchGroup() {
        return false;
    }

    @Override
    public void _persistence_setShouldRefreshFetchGroup(boolean bln) {

    }

    @Override
    public Session _persistence_getSession() {

        return sn;
    }

    @Override
    public void _persistence_setSession(Session sn) {
        this.sn = sn;

    }
}
