/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.mdm.dataorganization.impl.jpa.persistence;

import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.impl.jpa.Attribute;
import edu.kit.dama.mdm.dataorganization.impl.jpa.CollectionNode;
import edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.impl.jpa.FileNode;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.util.Constants;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author pasic
 */
final public class PersistenceFacade {

  private final static PersistenceFacade SINGLETON = new PersistenceFacade();
  private static final String DEFAULT_PU_NAME = "DataOrganizationPU";
  private String persistenceUnitName = null;
  private EntityManagerFactory entityManagerFactory = null;
  private static final String COL_STEP_NO_ARRIVED = "stepNoArrived";
  private static final String COL_STEP_NO_LEAVED = "stepNoLeaved";
  private static final String COL_DIGITAL_OBJ_ID = "digitalObjectIDStr";
  private static final String COL_NODE_DEPTH = "nodeDepth";
  private static final String COL_NODE_ID_VERSION = "idVersion";
  private static final String COL_VIEW_NAME = "viewName";

  /**
   * Gets the persistence unit name.
   *
   * @return The PU name.
   */
  public String getPersistenceUnitName() {
    return null == persistenceUnitName ? DEFAULT_PU_NAME
            : persistenceUnitName;
  }

  /**
   * Selects a persistence unit. Unit name must be present in persistence.xml.
   *
   * @param persistenceUnitName The PU name.
   */
  public void setPersistenceUnit(String persistenceUnitName) {
    this.persistenceUnitName = persistenceUnitName;
    entityManagerFactory = Persistence.createEntityManagerFactory(
            getPersistenceUnitName());
  }

  /**
   * Returns the entity manager factory corresponding to the persistence unit
   * name.
   *
   * @return The EntityManagerFactory.
   */
  public EntityManagerFactory getEntityManagerFactory() {
    if (null == entityManagerFactory) {
      entityManagerFactory = Persistence.createEntityManagerFactory(
              getPersistenceUnitName());
    }
    return entityManagerFactory;
  }

  /**
   * Default constructor
   */
  public PersistenceFacade() {
  }

  /**
   * Save the entity.
   *
   * @param entity The entity to save.
   *
   * @return The reference to the saved entity.
   */
  public Object save(Object entity) {
    EntityManager em = getEntityManagerFactory().createEntityManager();
    EntityTransaction transaction = em.getTransaction();
    transaction.begin();
    Object ret = em.merge(entity);
    transaction.commit();
    em.close();
    return ret;
  }

  /**
   * Gets a singleton instance.
   *
   * @return The singleton.
   */
  public static PersistenceFacade getInstance() {
    return SINGLETON;
  }

  public List<DataOrganizationNode> getAllNodesForTree(
          DigitalObjectId digitalObjectID) {
    return getAllNodesForTree(digitalObjectID, Constants.DEFAULT_VIEW);
  }

  /**
   * Queries the db and gets all the the nodes of a tree.
   *
   * @param digitalObjectID The id of the object to load all nodes for.
   * @param viewName The name of the view that should be loaded.
   *
   * @return All nodes of the tree.
   */
  public List<DataOrganizationNode> getAllNodesForTree(
          DigitalObjectId digitalObjectID, String viewName) {
    EntityManager em = getEntityManagerFactory().createEntityManager();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<DataOrganizationNode> query = cb.createQuery(
            DataOrganizationNode.class);

    //Query: Select * From DataOrganizationNodes Where digitalobjectid = digitalObjectID ASC stepsArrived
    Root<DataOrganizationNode> from = query.from(DataOrganizationNode.class);
    query.select(from);
    query.where(
            cb.and(
                    cb.equal(from.get(COL_DIGITAL_OBJ_ID),
                            digitalObjectID.getStringRepresentation()),
                    cb.equal(from.get(COL_VIEW_NAME),
                            viewName)));
    query.orderBy(cb.asc(from.get(COL_STEP_NO_ARRIVED)));

    TypedQuery<DataOrganizationNode> tq = em.createQuery(query);
    List<DataOrganizationNode> resultList = tq.getResultList();
    em.close();
    return resultList;
  }

  /**
   * Deletes all nodes belonging to a view of for a tree from db. If viewName is
   * null, all views are deleted.
   *
   * @param digitalObjectID tree is identified by this
   * @param viewName The data organization view containing a different
   * representation of a digital object's data.
   */
  public void deleteAllNodesForTree(DigitalObjectId digitalObjectID, String viewName) {
    EntityManager em = getEntityManagerFactory().createEntityManager();
    EntityTransaction transaction = em.getTransaction();

    TypedQuery<DataOrganizationNode> q;
    if (viewName != null) {
      q = em.createQuery(
              "SELECT do FROM DataOrganizationNode do WHERE "
              + "do.digitalObjectIDStr = :id and do.viewName = :viewName",
              DataOrganizationNode.class);
      q.setParameter("id", digitalObjectID.getStringRepresentation());
      q.setParameter("viewName", viewName);
    } else {
      q = em.createQuery(
              "SELECT do FROM DataOrganizationNode do WHERE "
              + "do.digitalObjectIDStr = :id", DataOrganizationNode.class);
      q.setParameter("id", digitalObjectID.getStringRepresentation());
    }
    transaction.begin();

    List<DataOrganizationNode> resultList = q.getResultList();
    for (DataOrganizationNode don : resultList) {
      em.remove(don);
    }

    transaction.commit();
    em.close();
  }

  /**
   * Deletes all nodes and all views for a tree from db.
   *
   * @param digitalObjectID tree is identified by this.
   */
  public void deleteAllNodesForTree(DigitalObjectId digitalObjectID) {
    deleteAllNodesForTree(digitalObjectID, null);
  }

  /**
   * Get all child nodes.
   *
   * @param relativeRoot The node for which the children should be obtained.
   * @param firstResult The index of the first result.
   * @param maxResults The max. number of results.
   *
   * @return A list of child nodes (max. <i>maxResults</i>)
   */
  public List<DataOrganizationNode> getChildNodes(NodeId relativeRoot,
          int firstResult, int maxResults) {
    EntityManager em = getEntityManagerFactory().createEntityManager();

    DataOrganizationNode rrNode = findNodeByNodeId(relativeRoot, em);

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<DataOrganizationNode> query = cb.createQuery(
            DataOrganizationNode.class);
    Root<DataOrganizationNode> from = query.from(DataOrganizationNode.class);
    query = cb.createQuery(DataOrganizationNode.class);
    query.select(from);
    query.where(
            cb.and(
                    cb.equal(from.get(COL_DIGITAL_OBJ_ID), relativeRoot.
                            getDigitalObjectId().getStringRepresentation()),
                    cb.equal(from.get(COL_VIEW_NAME),
                            relativeRoot.getViewName()),
                    cb.ge(from.<Long>get(COL_STEP_NO_ARRIVED), rrNode.
                            getStepNoArrived()),
                    cb.le(from.<Long>get(COL_STEP_NO_LEAVED), rrNode.
                            getStepNoLeaved()),
                    cb.equal(from.<Integer>get(COL_NODE_DEPTH), rrNode.
                            getNodeDepth() + 1)));

    query.orderBy(cb.asc(from.get(COL_STEP_NO_ARRIVED)));
    TypedQuery<DataOrganizationNode> tq = em.createQuery(query);

    tq.setMaxResults(maxResults);
    tq.setFirstResult(firstResult);

    List<DataOrganizationNode> resultList = tq.getResultList();
    em.close();
    return resultList;
  }

  /**
   * Get the number of all child nodes.
   *
   * @param relativeRoot The node for which the child count should be obtained.
   *
   * @return The child count.
   */
  public Long getChildNodeCount(NodeId relativeRoot) {
    EntityManager em = getEntityManagerFactory().createEntityManager();

    DataOrganizationNode rrNode = findNodeByNodeId(relativeRoot, em);

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> query = cb.createQuery(Long.class);
    Root<DataOrganizationNode> from = query.from(DataOrganizationNode.class);
    query.select(cb.count(from));
    query.where(
            cb.and(
                    cb.equal(from.get(COL_DIGITAL_OBJ_ID), relativeRoot.
                            getDigitalObjectId().getStringRepresentation()),
                    cb.equal(from.get(COL_VIEW_NAME),
                            relativeRoot.getViewName()),
                    cb.ge(from.<Long>get(COL_STEP_NO_ARRIVED), rrNode.
                            getStepNoArrived()),
                    cb.le(from.<Long>get(COL_STEP_NO_LEAVED), rrNode.
                            getStepNoLeaved()),
                    cb.equal(from.<Integer>get(COL_NODE_DEPTH), rrNode.
                            getNodeDepth() + 1)));

    //query.orderBy(cb.asc(from.get(COL_STEP_NO_ARRIVED)));
    Long result = em.createQuery(query).getSingleResult();
    em.close();
    return result;
  }

  /**
   * Get all child nodes.
   *
   * @param relativeRoot The node for which the children should be obtained.
   * @param relativeDepth The depth.
   *
   * @return The list of child nodes.
   */
  public List<DataOrganizationNode> getAllNodes(NodeId relativeRoot,
          int relativeDepth) {
    EntityManager em = getEntityManagerFactory().createEntityManager();

    DataOrganizationNode rrNode = findNodeByNodeId(relativeRoot, em);

    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<DataOrganizationNode> query = cb.createQuery(
            DataOrganizationNode.class);
    Root<DataOrganizationNode> from = query.from(DataOrganizationNode.class);
    query = cb.createQuery(DataOrganizationNode.class);
    query.select(from);
    query.where(
            cb.and(
                    cb.equal(from.get(COL_DIGITAL_OBJ_ID), relativeRoot.
                            getDigitalObjectId().getStringRepresentation()),
                    cb.equal(from.get(COL_VIEW_NAME),
                            relativeRoot.getViewName()),
                    cb.ge(from.<Long>get(COL_STEP_NO_ARRIVED), rrNode.
                            getStepNoArrived()),
                    cb.le(from.<Long>get(COL_STEP_NO_LEAVED), rrNode.
                            getStepNoLeaved()),
                    cb.le(from.<Integer>get(COL_NODE_DEPTH), rrNode.
                            getNodeDepth() + relativeDepth)));

    query.orderBy(cb.asc(from.get(COL_STEP_NO_ARRIVED)));
    TypedQuery<DataOrganizationNode> tq = em.createQuery(query);

    List<DataOrganizationNode> resultList = tq.getResultList();
    em.close();

    return resultList;
  }

  /**
   * Find a node by its id.
   *
   * @param nodeId The id to search for.
   * @param em The entityManager used to search.
   *
   * @return The node with the provided id.
   */
  public DataOrganizationNode findNodeByNodeId(NodeId nodeId, EntityManager em) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<DataOrganizationNode> query = cb.createQuery(DataOrganizationNode.class);
    Root<DataOrganizationNode> from = query.from(DataOrganizationNode.class);
    query.select(from);
    query.where(
            cb.and(
                    cb.equal(from.get(COL_DIGITAL_OBJ_ID), nodeId.
                            getDigitalObjectId().getStringRepresentation()),
                    cb.equal(from.get(COL_VIEW_NAME), nodeId.getViewName()),
                    cb.equal(from.get(COL_STEP_NO_ARRIVED), nodeId.
                            getInTreeId()),
                    cb.equal(from.get(COL_NODE_ID_VERSION), nodeId.
                            getInTreeIdVersion())));
    TypedQuery<DataOrganizationNode> tq = em.createQuery(query);
    return tq.getSingleResult();

  }

  public DataOrganizationNode getRootNode(DigitalObjectId digitalObjectId,
          EntityManager em) {
    return getRootNode(digitalObjectId, em, Constants.DEFAULT_VIEW);
  }

  /**
   * Get the DataOrganization Root node for the object with the provided id.
   *
   * @param digitalObjectId The object id.
   * @param em The entityManager used to search.
   * @param viewName The name of the view.
   *
   * @return The root node.
   */
  public DataOrganizationNode getRootNode(DigitalObjectId digitalObjectId,
          EntityManager em, String viewName) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<DataOrganizationNode> query = cb.createQuery(
            DataOrganizationNode.class);
    Root<DataOrganizationNode> from = query.from(DataOrganizationNode.class);
    query.select(from);
    query.where(
            cb.and(
                    cb.and(
                            cb.equal(from.get(COL_DIGITAL_OBJ_ID),
                                    digitalObjectId.
                                    getStringRepresentation()),
                            cb.equal(from.get(COL_VIEW_NAME), viewName),
                            cb.equal(from.get(COL_NODE_DEPTH), 0)),
                    cb.equal(from.get(COL_VIEW_NAME), viewName)));
    TypedQuery<DataOrganizationNode> tq = em.createQuery(query);
    return tq.getSingleResult();

  }

  /**
   * Update the node with the provided id. This method updated only the direct
   * content (name, attributes, logical filename), not the childrens of a
   * CollectionNode.
   *
   * @param id The id of the node to update.
   * @param newData The new content of the node.
   */
  public void updateDataOrganizationNodeData(NodeId id,
          IDataOrganizationNode newData) {
    EntityManager em = getEntityManagerFactory().createEntityManager();
    em.getTransaction().begin();
    DataOrganizationNode node = findNodeByNodeId(id, em);
    if (!((newData instanceof IFileNode && node instanceof FileNode)
            || (newData instanceof ICollectionNode
            && node instanceof CollectionNode))) {
      // the parmeter entity and the server side entity have different types
      // which is a sign of corruption
      throw new IllegalStateException(
              "Node data update inpossible due to type mismatch");
    }

    if (newData instanceof IFileNode && node instanceof FileNode) {
      // FileNode specific updates
      FileNode fileNode = (FileNode) node;
      IFileNode fnewData = (IFileNode) newData;
      fileNode.setLogicalFileName(fnewData.getLogicalFileName());
    }

    //updates common for collections and files
    node.setName(newData.getName());
    node.setDescription(newData.getDescription());
    Set<Attribute> attributesOld = node.getAttributes();
    Set<? extends IAttribute> attributesNew = newData.getAttributes();

    for (Attribute attr : attributesOld) {
      if (!attributesNew.contains(attr)) {
        attributesOld.remove(attr);
      }
    }

    for (IAttribute attr : attributesNew) {
      Attribute nattr = new Attribute(attr);
      if (!attributesOld.contains(nattr)) {
        node.addAttribute(nattr);
      }
    }

    em.getTransaction().commit();
  }

  public List<String> getViewsForDoid(DigitalObjectId doid) {
    EntityManager em = getEntityManagerFactory().createEntityManager();
    Query q = em.createQuery(
            "select distinct d.viewName from DataOrganizationNode d");
    //doid.getStringRepresentation());
    List<String> l = q.getResultList();
    List<String> r = new LinkedList<>();
    for (String le : l) {
      if (le == null) {
        if (!r.contains("default")) {
          r.add("default");
        }
      } else {
        if (!r.contains(le)) {
          r.add(le);
        }
      }
    }

    return r;
  }
}
