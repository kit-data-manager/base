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
package edu.kit.dama.mdm.core.jpa;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.persistence.Entity;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.Metamodel;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the MetaDataManagement interface for a JPA implementation.
 * The IMetaDataManager interface closely follows the JPA interface.
 *
 * @author hartmann-v
 */
public class MetaDataManagerJpa implements IMetaDataManager {

    /**
     * For logging purposes.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataManagerJpa.class);
    public static final String JAVAX_PERSISTENCE_FETCHGRAPH = "javax.persistence.fetchgraph";

    /**
     * JPA entity manager for selected type.
     */
    private final EntityManager entityManager;
    private volatile boolean entityManagerClosed = false;
    private Map<String, Object> properties;

    /**
     * Constructor.
     *
     * @param entityManager entity manager implementation.
     */
    MetaDataManagerJpa(final EntityManager entityManager) {
        if (entityManager == null) {
            throw new IllegalArgumentException("Argument entityManager must not be null");
        }
        this.entityManager = entityManager;
        properties = new TreeMap<>();
    }

    @Override
    public void addProperty(String key, Object value) {
        if (JAVAX_PERSISTENCE_FETCHGRAPH.equals(key) && value instanceof String) {
            properties.put(key, entityManager.getEntityGraph((String) value));
        } else {
            properties.put(key, value);
        }
    }

    @Override
    public void removeProperty(String key) {
        properties.remove(key);
    }

    @Override
    public void removeAllProperties() {
        properties.clear();
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public final <T> boolean contains(final T object) throws UnauthorizedAccessAttemptException {
        //contains()
        //
        //1. @ID pruefen (wenn NULL dann ist Entity neu, return false; )
        //2. Wenn @ID gesetzt, return true;

        return EntityManagerHelper.getIdOfEntity(object.getClass(), object) != null;
    }

    private void applyProperties(Query pQuery) {
        Set<Entry<String, Object>> entries = properties.entrySet();
        for (Entry<String, Object> entry : entries) {
            pQuery.setHint(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public final <T> List<T> find(final Class<T> entityClass) throws UnauthorizedAccessAttemptException {
        List<T> resultList = new ArrayList();
        String queryString = "SELECT e FROM " + EntityManagerHelper.getEntityTableName(entityClass) + " e";
        LOGGER.debug("Query string for all entities of one class: {}", queryString);
        try {
            //EntityGraph graph = entityManager.getEntityGraph("DigitalObjectTransition.simple");
            Query query = entityManager.createQuery(queryString, entityClass);

            // query.setHint("javax.persistence.fetchgraph", graph);
            applyProperties(query);
            resultList = query.getResultList();
        } catch (RuntimeException re) {
            LOGGER.trace("Failed to find entity list for query: " + queryString, re);
        } finally {
            finalizeEntityManagerAccess("find", null, entityClass);
        }
        return resultList;
    }

    @Override
    public final <T> T find(final Class<T> entityClass, final Object primaryKey) throws UnauthorizedAccessAttemptException {
        if (primaryKey == null) {
            throw new IllegalArgumentException("Argument 'primaryKey' must not be null");
        }

        T result = null;
        try {
            //Map<String, Object> hints = new HashMap<>();
            //hints.put("javax.persistence.fetchgraph", entityManager.getEntityGraph("fetchInvestigations"));
            //result = entityManager.find(entityClass, primaryKey, hints);
            //Provide -javaagent:D:/.m2/repository2/org/eclipse/persistence/eclipselink/2.5.1/eclipselink-2.5.1.jar 
            //or implement FetchGroupTracker as it is currently done
            result = entityManager.find(entityClass, primaryKey, properties);
        } catch (RuntimeException re) {
            LOGGER.trace("Failed to find typed single result by primary key '" + primaryKey + "'", re);
        } finally {
            finalizeEntityManagerAccess("find with PK '" + primaryKey.toString() + "'", null, entityClass);
        }
        return result;
    }

    @Override
    public final <T> List<T> findResultList(String queryString, Class<T> entityClass) throws UnauthorizedAccessAttemptException {
        return findResultList(queryString, null, entityClass);
    }

    @Override
    public final <T> List<T> findResultList(String queryString, Object[] pParameters, Class<T> entityClass) throws UnauthorizedAccessAttemptException {
        return findResultList(queryString, pParameters, entityClass, 0, Integer.MAX_VALUE);
    }

    @Override
    public final <T> List<T> findResultList(String queryString, Class<T> entityClass, int pFirstIdx, int pMaxResults) throws UnauthorizedAccessAttemptException {
        return findResultList(queryString, null, entityClass, pFirstIdx, pMaxResults);
    }

    @Override
    public final <T> List<T> findResultList(String queryString, Object[] pParameters, Class<T> entityClass, int pFirstIdx, int pMaxResults) throws UnauthorizedAccessAttemptException {
        List<T> result = new ArrayList();
        try {
            TypedQuery<T> q = entityManager.createQuery(queryString, entityClass);
            applyProperties(q);
            if (pParameters != null && pParameters.length != 0) {
                for (int i = 0; i < pParameters.length; i++) {
                    q.setParameter(i + 1, pParameters[i]);
                }
            }
            q.setFirstResult((pFirstIdx >= 0) ? pFirstIdx : 0);
            if (pMaxResults > 0) {
                q.setMaxResults(pMaxResults);
            }
            result = q.getResultList();
        } catch (RuntimeException re) {
            LOGGER.trace("Failed to obtain typed query result list", re);
        } finally {
            finalizeEntityManagerAccess("find result list with plain SQL '" + queryString + "'", null, entityClass);
        }
        return result;
    }

    @Override
    public final List findResultList(String queryString) throws UnauthorizedAccessAttemptException {
        return findResultList(queryString, (Object[]) null, 0, Integer.MAX_VALUE);
    }

    @Override
    public final List findResultList(String queryString, Object[] pParameters) throws UnauthorizedAccessAttemptException {
        return findResultList(queryString, pParameters, 0, Integer.MAX_VALUE);
    }

    @Override
    public final List findResultList(String queryString, int pFirstIdx, int pMaxResults) throws UnauthorizedAccessAttemptException {
        return findResultList(queryString, (Object[]) null, pFirstIdx, pMaxResults);
    }

    @Override
    public final List findResultList(String queryString, Object[] pParameters, int pFirstIdx, int pMaxResults) throws UnauthorizedAccessAttemptException {
        List result = new ArrayList();
        try {
            Query q = entityManager.createQuery(queryString);
            applyProperties(q);
            if (pParameters != null && pParameters.length != 0) {
                for (int i = 0; i < pParameters.length; i++) {
                    q.setParameter(i + 1, pParameters[i]);
                }
            }
            q.setFirstResult((pFirstIdx >= 0) ? pFirstIdx : 0);
            if (pMaxResults > 0) {
                q.setMaxResults(pMaxResults);
            }
            result = q.getResultList();
        } catch (RuntimeException re) {
            LOGGER.trace("Failed to obtain generic query result list", re);
        } finally {
            finalizeEntityManagerAccess("find result list with plain SQL '" + queryString + "'", null, List.class);
        }
        return result;
    }

    @Override
    public final <T> T findSingleResult(String queryString, Class<T> entityClass) throws UnauthorizedAccessAttemptException {
        return findSingleResult(queryString, (Object[]) null, entityClass);
    }

    @Override
    public final <T> T findSingleResult(String queryString, Object[] pParameters, Class<T> entityClass) throws UnauthorizedAccessAttemptException {
        T result = null;
        try {
            LOGGER.debug("Building typed query");
            TypedQuery<T> q = entityManager.createQuery(queryString, entityClass);
            applyProperties(q);
            if (pParameters != null && pParameters.length != 0) {
                LOGGER.debug("Adding {} parameters to query", pParameters.length);
                for (int i = 0; i < pParameters.length; i++) {
                    q.setParameter(i + 1, pParameters[i]);
                }
            }
            LOGGER.debug("Executing query for single result.");
            result = q.getSingleResult();
            LOGGER.debug("Query returned.");
        } catch (RuntimeException re) {
            LOGGER.trace("Failed to obtain typed single query result", re);
        } finally {
            finalizeEntityManagerAccess("find single result with plain SQL '" + queryString + "'", null, entityClass);
        }
        LOGGER.debug("Returning result.");
        return result;
    }

    @Override
    public final Object findSingleResult(String queryString) throws UnauthorizedAccessAttemptException {
        return findSingleResult(queryString, (Object[]) null);
    }

    @Override
    public final Object findSingleResult(String queryString, Object[] pParameters) throws UnauthorizedAccessAttemptException {
        Object result = null;
        try {
            Query q = entityManager.createQuery(queryString);
            applyProperties(q);
            if (pParameters != null && pParameters.length != 0) {
                for (int i = 0; i < pParameters.length; i++) {
                    q.setParameter(i + 1, pParameters[i]);
                }
            }
            result = q.getSingleResult();
        } catch (RuntimeException re) {
            LOGGER.trace("Failed to obtain generic single query result", re);
        } finally {
            finalizeEntityManagerAccess("find single result with plain SQL '" + queryString + "'", null, Object.class);
        }
        return result;
    }

    @Override
    public final Integer performUpdate(String queryString) throws UnauthorizedAccessAttemptException {
        return performUpdate(queryString, (Object[]) null);
    }

    @Override
    public final Integer performUpdate(String queryString, Object[] pParameters) throws UnauthorizedAccessAttemptException {
        Integer result = null;
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            Query q = entityManager.createQuery(queryString);
            applyProperties(q);
            if (pParameters != null && pParameters.length != 0) {
                for (int i = 0; i < pParameters.length; i++) {
                    q.setParameter(i + 1, pParameters[i]);
                }
            }
            transaction.begin();
            result = q.executeUpdate();
        } catch (RuntimeException re) {
            LOGGER.trace("Failed to obtain generic update result", re);
        } finally {
            finalizeEntityManagerAccess("update with plain SQL '" + queryString + "'", transaction, Object.class);
        }
        return result;
    }

    @Override
    public final <T> List<T> find(final T first, final T last) throws UnauthorizedAccessAttemptException {
        List<T> resultList = new ArrayList();
        boolean firstCondition = true;
        // Maybe one of the arguments could be null.
        // Test for the instance which is not null.
        T argumentNotNull = null;
        if (first != null) {
            argumentNotNull = first;
        } else if (last != null) {
            argumentNotNull = last;
        }
        if (argumentNotNull == null) {
            // both instances are null nothing to do.
            return resultList;
        }
        StringBuilder queryString = new StringBuilder("SELECT e FROM ")
                .append(EntityManagerHelper.getEntityTableName(argumentNotNull.getClass()))
                .append(" e");
        try {
            Metamodel metamodel = entityManager.getMetamodel();

            for (Object attribute : metamodel.entity(argumentNotNull.getClass()).getAttributes()) {
                Attribute myAttribute = ((Attribute) attribute);
                LOGGER.trace("Attribute: {}\nName: {}\n ",
                        attribute,
                        myAttribute.getName());
                // Only basic types where tested. 
                if (myAttribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.BASIC) {
                    String propertyName = myAttribute.getName();
                    String firstValue = null;
                    String lastValue = null;
                    if (first != null) {
                        firstValue = BeanUtils.getProperty(first, propertyName);
                    }
                    if (last != null) {
                        lastValue = BeanUtils.getProperty(last, propertyName);
                    }
                    if ((firstValue != null) || (lastValue != null)) {
                        LOGGER.trace("At least one property is set!");
                        if (!firstCondition) {
                            queryString.append(" AND ");
                        } else {
                            queryString.append(" WHERE");
                            firstCondition = false;
                        }
                        queryString.append(" e.").append(propertyName);
                        if ((firstValue != null) && (lastValue != null)) {
                            queryString.append(" BETWEEN '").append(firstValue).append("' AND '").append(lastValue).append("'");
                        } else if (firstValue != null) {
                            queryString.append(" >= '").append(firstValue).append("'");
                        } else {
                            // lastValue != null
                            queryString.append(" <= '").append(lastValue).append("'");
                        }
                    }
                } else {
                    LOGGER.trace("****************************************"
                            + "*****************************\nAttribute skipped: {}",
                            myAttribute.getDeclaringType().getClass());
                }
            }
            LOGGER.debug(queryString.toString());
            Query q = entityManager.createQuery(queryString.toString());
            applyProperties(q);
            resultList = (List<T>) q.getResultList();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.trace("Failed to obtain result in find-method for query: " + queryString.toString(), e);
        } finally {
            finalizeEntityManagerAccess("find(first,last)", null, argumentNotNull);
        }
        return resultList;
    }

    @Override
    public final <T> T save(final T entity) throws UnauthorizedAccessAttemptException {
        //1. persist(Entity) falls EntityExistException
        //2. impl.merge(E')
        T savedEntity = entity;
        try {
            persist(entity);
        } catch (EntityExistsException eee) {
            EntityTransaction transaction = entityManager.getTransaction();
            try {
                transaction.begin();
                savedEntity = entityManager.merge(entity);
            } catch (RuntimeException re) {
                LOGGER.error("Failed to save entity", re);
                throw re;
            } finally {
                finalizeEntityManagerAccess("save -> merge", transaction, entity);
            }
        }
        return savedEntity;
    }

    @Override
    public final <T> T refresh(final T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
//refresh()
//
//1. Pruefen ob em.contains(Entity) -> falls ja, em.refresh(Entity), return Entity
//3. Wenn @ID gesetzt, dann E' = find(Entity, Key) / em.refresh(E'); / return E'
//2. @ID pruefen (wenn NULL dann ist Entity neu, EntityNotFoundException werfen)
//
//
        T entityManaged = entity;
        if (entityManager.contains(entityManaged)) {
            try {
                entityManager.refresh(entityManaged, properties);
            } catch (RuntimeException re) {
                LOGGER.error("Failed to refresh entity", re);
                throw re;
            } finally {
                finalizeEntityManagerAccess("refresh", null, entity);
            }
        } else if (contains(entity)) {
            entityManaged = EntityManagerHelper.obtainManagedEntity(entityManager, entity);
            try {
                entityManager.refresh(entityManaged, properties);
            } catch (RuntimeException re) {
                LOGGER.error("Failed to refresh entity", re);
                throw re;
            } finally {
                finalizeEntityManagerAccess("refresh", null, entityManaged);
            }
        } else {
            throw new EntityNotFoundException("Cannot refresh non existing entity: " + entity);
        }
        return entityManaged;
    }

    @Override
    public final <T> void remove(final T entity) throws UnauthorizedAccessAttemptException {
        T managedEntity = entity;
        if (!entityManager.contains(managedEntity)) {
            managedEntity = EntityManagerHelper.obtainManagedEntity(entityManager, entity);
        }
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.remove(managedEntity);
        } catch (RuntimeException re) {
            LOGGER.trace("Failed to remove entity.", re);
        } finally {
            finalizeEntityManagerAccess("remove", transaction, entity);
        }
        setIdOfEntity2Null(entity.getClass(), entity);
    }

//  /**
//   * Make the provided entity managed. This is needed for some JPA operations,
//   * e.g. update or reload. Normally, the managed entity represented by
//   * pUnmanagedEntity will be returned. If not unmanaged entity is not an entity
//   * or the Id of the unmanaged entity cannot be obtained, an
//   * UnauthorizedAccessAttemptException is thrown.
//   *
//   * @param <T> Generic type.
//   * @param pUnmanagedEntity The unmanaged entity.
//   *
//   * @return The managed entity.
//   *
//   * @throws UnauthorizedAccessAttemptException If the Id field of the unmanaged
//   * entity could not be accessed.
//   */
//  private <T> T obtainManagedEntity(T pUnmanagedEntity) throws UnauthorizedAccessAttemptException {
//    return entityManager.find((Class<T>) pUnmanagedEntity.getClass(), getIdOfEntity(pUnmanagedEntity.getClass(), pUnmanagedEntity));
//  }
    /**
     * Finalize any operation which affected the EntityManager. This method has
     * the following tasks:
     *
     * <ul>
     * <li>Commit the provided transaction</li>
     * <li>If the commit fails, rollback the transaction</li>
     * <li>Clear the cache of the EntityManager (make all entities
     * unmanaged)</li>
     * </ul>
     *
     * If no transaction is provided (after an operation which did not affect
     * the data backend, e.g. find()), only the cache is cleared.
     *
     * @param <T> Generic entity type.
     * @param methodName Description of the method which was performed (for
     * debugging).
     * @param transaction The transaction to finalize.
     * @param entity Affected entity or entity class (for debugging).
     */
    private <T> void finalizeEntityManagerAccess(String methodName, EntityTransaction transaction, T entity) {
        if (transaction != null) {
            try {
                LOGGER.debug("Flushing entityManager");
                entityManager.flush();
                LOGGER.debug("Committing current transaction");
                transaction.commit();
            } catch (Exception e) {
                LOGGER.warn("Failed to commit transaction for operation '" + methodName + "'", e);
            } finally {
                if (transaction.isActive()) {
                    LOGGER.debug("Transaction is still active. Performing rollback.");
                    transaction.rollback();
                    LOGGER.error("Method '" + methodName + "' fails for entity/class '" + entity + "'");
                }
                LOGGER.debug("Clearing entityManager cache");
                entityManager.clear();
                LOGGER.debug("Cache cleared.");
            }
        } else {
            LOGGER.debug("Clearing entityManager cache");
            entityManager.clear();
            LOGGER.debug("Cache cleared.");
        }
    }

    @Override
    public final <T> T persist(T entity) throws UnauthorizedAccessAttemptException {
        //persist()
        //auf internes contains() pruefen, falls "TRUE" throw EntityExistsException() 
        if (contains(entity)) {
            throw new EntityExistsException("Cannot persist existing entity");
        }
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            entityManager.persist(entity);
        } catch (RuntimeException re) {
            LOGGER.error("Failed to persist entity", re);
            throw re;
        } finally {
            finalizeEntityManagerAccess("persist", transaction, entity);
        }
        return entity;
    }

    @Override
    public final <T> T update(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        //1. @ID pruefen (wenn NULL dann ist Entity neu, EntityNotFoundException werfen)
        //    setID() als protected in allen Entities um manuelles setzen zu vermeiden!!
        //2. Wenn @ID gesetzt, dann "return merge(Entity)"
        if (!contains(entity)) {
            throw new EntityNotFoundException("Can't update entity '" + entity + "'! Entity is not in database.");
        }
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            return entityManager.merge(entity);
        } catch (RuntimeException re) {
            LOGGER.error("Failed to update entity", re);
            throw re;
        } finally {
            finalizeEntityManagerAccess("update", transaction, entity);
        }
    }

    /**
     * Returns the field annotated with <code>@Id</code> of the provided entity.
     * If no annotated field was found, an IllegalArgumentException is thrown.
     * If the field cannot be accessed, an UnauthorizedAccessAttemptException is
     * thrown. Otherwise, the value of the ID field is returned.
     *
     * @param <T> Generic entity type.
     * @param entityClass The entity class.
     * @param entity The entity.
     *
     * @return The value of the field annotated with <code>@Id</code>
     *
     * @throws UnauthorizedAccessAttemptException If the Id field cannot be
     * accessed.
     */
//  private <T> Object getIdOfEntity(Class entityClass, T entity) throws UnauthorizedAccessAttemptException {
//    Object returnValue = null;
//    boolean idFound = false; // only one Id per class hierarchie.
//
//    if (entityClass.isAnnotationPresent(Entity.class)) {
//      Field[] fields = entityClass.getDeclaredFields();
//      for (Field field : fields) {
//        Id entityId = field.getAnnotation(Id.class);
//        if (entityId != null) {
//          idFound = true;
//          field.setAccessible(true);
//          try {
//            returnValue = field.get(entity);
//          } catch (IllegalAccessException iae) {
//            throw new UnauthorizedAccessAttemptException("Failed to access value of field '" + field.getName() + "' annotated by 'Id", iae);
//          } finally {
//            field.setAccessible(false);
//            // exit loop
//            break;
//          }
//        }
//      }
//      if (!idFound) {
//        returnValue = getIdOfEntity(entityClass.getSuperclass(), entity);
//      }
//    } else {
//      throw new IllegalArgumentException(entity.getClass().toString() + " is not an Entity!");
//    }
//    return returnValue;
//  }
    /**
     * Set the field annotated with <code>@Id</code> of the provided entity to
     * null. This method is used to 'mark' an entity as 'removed'. If the field
     * cannot be accessed, an UnauthorizedAccessAttemptException is thrown.
     * Otherwise, true is returned.
     *
     * @param <T> Generic entity type.
     * @param entityClass The entity class.
     * @param entity The entity.
     *
     * @return TRUE if the Id field could be set to null or if the provided
     * entity is not a JPA entity.
     *
     * @throws UnauthorizedAccessAttemptException If the Id field cannot be
     * accessed.
     */
    private <T> boolean setIdOfEntity2Null(Class entityClass, T entity) throws UnauthorizedAccessAttemptException {
        boolean success = false;
        if (entityClass.isAnnotationPresent(Entity.class)) {
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                Id entityId = field.getAnnotation(Id.class);
                if (entityId != null) {
                    field.setAccessible(true);
                    try {
                        field.set(entity, null);
                    } catch (IllegalAccessException iae) {
                        throw new UnauthorizedAccessAttemptException("Failed to access value of field '" + field.getName() + "' annotated by 'Id", iae);
                    } finally {
                        field.setAccessible(false);
                        // exit loop
                        break;
                    }
                }
            }
            if (!success) {
                success = setIdOfEntity2Null(entityClass.getSuperclass(), entity);
            }
        } else {
            success = true;
        }
        return success;
    }

    @Override
    public final void close() {
        if (!entityManagerClosed) {
            entityManagerClosed = true;
            entityManager.close();
        }
    }

    @Override
    @SuppressWarnings(value = "FinalizeDeclaration")
    protected final void finalize() throws Throwable {
        if (!entityManagerClosed) {
            LOGGER.warn("EntityManager should be closed directly after use! I'm doing it for finalization.");
            close();
        }
        super.finalize();
    }

    @Override
    public void setAuthorizationContext(IAuthorizationContext authorizationContext) {
        //nothing is done here as this metadata manager is not secured.
    }

    @Override
    public IAuthorizationContext getAuthorizationContext() {
        return null;
    }
}
