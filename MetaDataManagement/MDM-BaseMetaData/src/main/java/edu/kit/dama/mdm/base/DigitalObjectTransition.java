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

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.mdm.base.interfaces.IDefaultDigitalObjectTransition;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;

/**
 * An basic definition of a transition between one or more input digital objects
 * and one or more output digital objects. Basically, the transition in between
 * is defined by a transition type and an identifier that can by used to obtain
 * the entity decribing the transition, e.g. a computing task or a content
 * preservation operation. Obtaining this entity is then task of the concrete
 * implementation as the entity content might be stored in external systems,
 * files or somewhere else.
 *
 * @author mf6319
 * @param <C> The type of the linked entity.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TRANSITION_TYPE")
@Table(name = "DIGITALOBJECTTRANSITION")
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("id")
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("id"),
                @XmlNamedAttributeNode("transitionEntityId"),
                @XmlNamedAttributeNode(value = "inputObjectViewMappings", subgraph = "default"),
                @XmlNamedAttributeNode(value = "outputObjects", subgraph = "simple"),
                @XmlNamedAttributeNode("creationTimestamp")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "DigitalObjectTransition.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id")}),
    @NamedEntityGraph(
            name = "DigitalObjectTransition.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("id"),
                @NamedAttributeNode("transitionEntityId"),
                @NamedAttributeNode(value = "inputObjectViewMappings", subgraph = "DigitalObjectTransition.default.InputObjectViewMappings.simple"),
                @NamedAttributeNode(value = "outputObjects", subgraph = "DigitalObjectTransition.default.OutputObjects.simple"),
                @NamedAttributeNode("creationTimestamp")
            },
            subgraphs = {
                @NamedSubgraph(
                        name = "DigitalObjectTransition.default.InputObjectViewMappings.simple",
                        attributeNodes = {
                            @NamedAttributeNode("viewName"),
                            @NamedAttributeNode(value = "digitalObject", subgraph = "DigitalObjectTransition.default.InputObjectViewMappings.simple.DigitalObject.simple")
                        }
                ),
                @NamedSubgraph(
                        name = "DigitalObjectTransition.default.InputObjectViewMappings.simple.DigitalObject.simple",
                        attributeNodes = {
                            @NamedAttributeNode("baseId")
                        }
                ),
                @NamedSubgraph(
                        name = "DigitalObjectTransition.default.OutputObjects.simple",
                        attributeNodes = {
                            @NamedAttributeNode("baseId")
                        }
                )
            })
})
public class DigitalObjectTransition<C> implements Serializable, IDefaultDigitalObjectTransition, FetchGroupTracker {

    private static final long serialVersionUID = -3251059153836839236L;

    /**
     * Id of the transition. Has to be unique.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The id of the metadata entity describing the transition. It can be used
     * by concrete implementations of this class to obtain additional
     * information from external sources.
     */
    private String transitionEntityId;

    /* @ElementCollection
     @CollectionTable(name = "inputObjects_transition")
     @MapKeyJoinColumn(name = "inputobject_baseid")
     @Column(name = "viewId")
     @BatchFetch(BatchFetchType.EXISTS)
     @XmlPath(".")
     @XmlJavaTypeAdapter(DigitalObjectViewMapAdapter.class)*/
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "transition_inputObjects")
    @BatchFetch(BatchFetchType.EXISTS)
    private Set<ObjectViewMapping> inputObjectViewMappings;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "transition_outputObjects")
    @BatchFetch(BatchFetchType.EXISTS)
    private Set<DigitalObject> outputObjects;
    /**
     * The timestamp when this transition was created.
     */
    private Long creationTimestamp = 0l;

    public DigitalObjectTransition() {
        outputObjects = new HashSet<>();
        inputObjectViewMappings = new HashSet<>();
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * Set the id of the transition.
     *
     * @param pId The id.
     */
    public void setId(Long pId) {
        this.id = pId;
    }

    @Override
    public Long getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * Set the creation timestamp of this transition.
     *
     * @param pCreationTimestamp The creation timestamp.
     */
    public void setCreationTimestamp(Long pCreationTimestamp) {
        this.creationTimestamp = pCreationTimestamp;
    }

    @Override
    public Set<DigitalObject> getOutputObjects() {
        return outputObjects;
    }

    /**
     * Set the set of output objects that are result of this transition. In
     * typical use cases only one output object should be produced, but in
     * special cases also multiple outputs are imaginable.
     *
     * @param pOutputObjects The set of output objects.
     */
    public void setOutputObjects(Set<DigitalObject> pOutputObjects) {
        this.outputObjects = pOutputObjects;
    }

    /**
     * Add a single output object to the list of output objects.
     *
     * @param pObject The output object to add.
     */
    public void addOutputObject(DigitalObject pObject) {
        outputObjects.add(pObject);
    }

    @Override
    public Set<ObjectViewMapping> getInputObjectViewMappings() {
        return inputObjectViewMappings;
    }

    /**
     * Set the list of input objects and their according views that are inputs
     * of this transition.
     *
     * @param pInputObjectViewMappings The list of input objects and their
     * views.
     */
    public void setInputObjectViewMappings(Set<ObjectViewMapping> pInputObjectViewMappings) {
        this.inputObjectViewMappings = pInputObjectViewMappings;
    }

    /**
     * Add the single object-view input mapping.
     *
     * @param pObject The object part of the mapping.
     * @param pView The view part of the mapping.
     */
    public void addInputMapping(final DigitalObject pObject, String pView) {
        if (pObject == null || pObject.getDigitalObjectId() == null) {
            return;
        }
        ObjectViewMapping existing = (ObjectViewMapping) CollectionUtils.find(inputObjectViewMappings, new Predicate() {

            @Override
            public boolean evaluate(Object o) {
                return pObject.getDigitalObjectId().equals(((ObjectViewMapping) o).getDigitalObject().getDigitalObjectId());
            }
        });
        if (existing == null) {
            inputObjectViewMappings.add(new ObjectViewMapping(pObject, pView));
        }
    }

    /**
     * Get the type of this transition. By default, TransitionType.NONE is
     * returned. All available transition types are defined by the enum
     * {@link TransitionType}. The appropriate enum must be returned by
     * implementations extending this basic transition.
     *
     * @return The transition type.
     */
    public TransitionType getTransitionType() {
        return TransitionType.NONE;
    }

    /**
     * Set the entity id that can be used to obtain the entity linked to this
     * transition. For flexibility reasons the datatype of the id is
     * {@link String} and the implementation class is responsible for an
     * appropriate interpretation, e.g. parsing a long value as a primary key in
     * a relational database.
     *
     * @param pTransitionEntityId The string representation of the linked entity
     * id.
     */
    public void setTransitionEntityId(String pTransitionEntityId) {
        this.transitionEntityId = pTransitionEntityId;
    }

    /**
     * Get the id of the entity linked to this transition. Typically, this
     * method should not be used externally but by the implementation class to
     * obtain the linked entity when calling {@link #getTransitionEntity(edu.kit.dama.authorization.entities.IAuthorizationContext)
     * }. In this case the implementation class is resposible for an appropriate
     * interpretation of the result of this method call.
     *
     * @return The string representation of the linked entity id.
     */
    public String getTransitionEntityId() {
        return transitionEntityId;
    }

    /**
     * Get the entity linked to this transition. Therefor, {@link #getTransitionEntityId()
     * } and the provided AuthorizationContext are be used to query the
     * according data source. The internal call may also fail. In that case,
     * 'null' might be returned by this method.
     *
     * @param pContext The authorization context used to authorize the access to
     * the transition entity, if required.
     *
     * @return The entity linked to this transtion or null if the entity could
     * not be retrieved.
     */
    public C getTransitionEntity(IAuthorizationContext pContext) {
        return null;
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
//  public static void main(String[] args) throws Exception {
//    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
//    mdm.setAuthorizationContext(new AuthorizationContext(new UserId("admin"), new GroupId("USERS"), Role.MANAGER));
//
//    DigitalObject o1 = DigitalObject.factoryNewDigitalObject();
//    o1.setLabel("O1");
//    DigitalObject o2 = DigitalObject.factoryNewDigitalObject();
//    o2.setLabel("O2");
//    DigitalObject o3 = DigitalObject.factoryNewDigitalObject();
//    o2.setLabel("O3");
//    mdm.save(o1);
//    mdm.save(o2);
//    mdm.save(o3);
//    DataWorkflowTransition t = new DataWorkflowTransition();
//    t.setTransitionEntityId(Long.toString(1l));
//    t.addInputMapping(o1, Constants.DEFAULT_VIEW);
//    t.addInputMapping(o2, "reduced");
//    t.addOutputObject(o3);
//    t.setCreationTimestamp(new Date().getTime());
//    mdm.save(t);
//    mdm.close();
//  }
}
