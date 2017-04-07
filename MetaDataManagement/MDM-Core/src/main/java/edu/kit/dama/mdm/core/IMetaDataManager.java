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
package edu.kit.dama.mdm.core;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import java.util.List;
import java.util.Map;

/**
 * Interface defining all methods needed to access MetaDataRepository. Each
 * instance is linked with one entity manager which has explicitly closed at the
 * end. metaDataManager.close();
 *
 * @author hartmann-v
 */
public interface IMetaDataManager {

    /**
     * Set authorization context which is used to authorize secured access and
     * filtering of secured resources. When using security, for each method of
     * this interface a secured version has to be implemented.
     *
     * Example remove():
     *
     * <pre>
     * &#64;Override
     * public &lt;T&gt; void remove(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
     *  if (entity instanceof ISecurableResource) {
     *    //use secured implementation
     *    removeSecured(entity, authCtx);
     *  } else {
     *     //use insecure implementation
     *     remove(entity);
     *  }
     * }
     *
     * &#64;SecuredMethod(roleRequired = Role.MANAGER)
     * private &lt;T&gt; void removeSecured(&#64;SecuredArgument T entity, &#64;Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
     *  //method will only be entered if authorizationContext satisfies Role.MANAGER
     *  ISecurableResource securableResource = (ISecurableResource) entity;
     *  //perform access checks and remove entity if allowed, throw UnauthorizedAccessAttemptException if not allowed
     *  remove(entity);
     * }
     * </pre>
     *
     * In this example, <i>authCtx</i> was set before and is used when an
     * operation is performed. The authorization context may change between
     * different calls, e.g. for temporary using system permissions.
     *
     * @param authorizationContext authorization context.
     */
    void setAuthorizationContext(IAuthorizationContext authorizationContext);

    /**
     * Get authorization context which is used to authorize secured access and
     * reiltering of secured resources.
     *
     * @return authorizationContext authorization context.
     */
    IAuthorizationContext getAuthorizationContext();

    /**
     * Check whether instance of object is managed by the manager backend. If
     * 'true' is returned, <i>object</i> can be obtained via methods like
     * find(). If 'false' is returned, <i>object</i> has to be persisted first.
     *
     * This method does not change <i>object</i> and won't try to achieve a
     * status where 'true' is returned, e.g. by persisting <i>object</i>
     * implicitly.
     *
     * @param <T> any class supported by the chosen model
     * @param object instance of an object
     *
     * @return TRUE = managed, FALSE = not managed.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> boolean contains(T object) throws UnauthorizedAccessAttemptException;

    /**
     * Find all instances of the given class.
     *
     * @param <T> any class supported by the chosen model
     * @param entityClass reference class
     *
     * @return List with all instances of &lt;T&gt; or an empty list.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> List<T> find(Class<T> entityClass) throws UnauthorizedAccessAttemptException;

    /**
     * Find instance by primary key. For backends using relational databases
     * (e.g. JPA), the primary key could be an instance of 'Long' stored in a
     * field annotated by <i>@Id</i>.
     *
     * @param <T> any class supported by the chosen model
     * @param entityClass class of entity.
     * @param primaryKey primary key of entityClass
     *
     * @return the found entity instance or null if the entity does not exist
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> T find(Class<T> entityClass, Object primaryKey) throws UnauthorizedAccessAttemptException;

    /**
     * Find instances withing the given boundaries defined by the two instances.
     * However, there are <b>no rules</b> on how detailed the result should be.
     * It is <i>recommended</i> to cover at least fields of primitive types,
     * which are directly located within class T. If T representes a complex
     * object containing child elements, the implementation of IMetaDataManager
     * may or may not include these childrens into the search.
     *
     * <b>Attention:</b> Neither <i>first</i> nor <i>last</i> can be expected to
     * be a valid entities. Their only purpose is to define search boundary.
     * Therefore, none of these arguments must be written to or should be
     * checked against the backend.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param <T> any class supported by the chosen model
     * @param first lower bounds
     * @param last upper bounds
     *
     * @return List with all instances fulfilling the criteria. (List may be
     * empty.)
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> List<T> find(T first, T last) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a list of entities of
     * the provided entity class fulfilling <i>queryString</i>. The syntax of
     * <i>queryString</i> depends on the concrete implementation of
     * IMetaDataManager and might be JPQL, SQL or another query language syntax.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param <T> any class supported by the chosen model
     * @param queryString The plain query string
     * @param entityClass The entity class
     *
     * @return The result list which might be empty if no entity matches
     * <i>queryString</i>.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> List<T> findResultList(String queryString, Class<T> entityClass) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a list of entities of
     * the provided entity class fulfilling <i>queryString</i>. The syntax of
     * <i>queryString</i> depends on the concrete implementation of
     * IMetaDataManager and might be JPQL, SQL or another query language syntax.
     * In contrast to {@link #findResultList(java.lang.String, java.lang.Class)
     * } this method is intended to be used if the query contains parameters. It
     * should be guaranteed that parameters are handled properly, e.g. by
     * escaping them to avoid SQL-injections or similar vulnerabilities. The
     * paramters are added to the query by providing variables according to
     * their position in the parameters array beginning with 1. Therefore, the
     * first parameter in the parameter array replaces the variable named
     * <b>?1</b> in the query.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param <T> any class supported by the chosen model
     * @param queryString The plain query string
     * @param pParameters The list of parameters used in queryString.
     * @param entityClass The entity class
     *
     * @return The result list which might be empty if no entity matches
     * <i>queryString</i>.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> List<T> findResultList(String queryString, Object[] pParameters, Class<T> entityClass) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a list of entities of
     * the provided entity class. The method behaves like findResultList(String,
     * Class), but the result list has a fixed maximum size defined by pFirstIdx
     * and pResultCount. This method is intended be used for performance reasons
     * to avoid reading all entities at once if only a subset is needed.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param <T> any class supported by the chosen model
     * @param queryString The plain query string
     * @param entityClass The entity class
     * @param pFirstIdx The first entity index withing the result list. This
     * index must be &gt;= 0. Values smaller than 0 should be set 0. The result
     * for the case, where <i>pFirstIdx</i> is larger than the max. amount of
     * entries is not defined!
     * @param pResultCount The maximum number of results returned by one call.
     * This value should be &gt;= 1. If the value is smaller than 1, the value
     * has to be <b>ignored</b> by the implementation.
     *
     * @return The result list which might be empty.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> List<T> findResultList(String queryString, Class<T> entityClass, int pFirstIdx, int pResultCount) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a list of entities of
     * the provided entity class. The method behaves like findResultList(String,
     * Class), but the result list has a fixed maximum size defined by pFirstIdx
     * and pResultCount. This method is intended be used for performance reasons
     * to avoid reading all entities at once if only a subset is needed. In
     * contrast to {@link #findResultList(java.lang.String, java.lang.Class)
     * } this method is intended to be used if the query contains parameters. It
     * should be guaranteed that parameters are handled properly, e.g. by
     * escaping them to avoid SQL-injections or similar vulnerabilities. The
     * paramters are added to the query by providing variables according to
     * their position in the parameters array beginning with 1. Therefore, the
     * first parameter in the parameter array replaces the variable named
     * <b>?1</b> in the query.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param <T> any class supported by the chosen model
     * @param queryString The plain query string
     * @param pParameters The list of parameters used in queryString.
     * @param entityClass The entity class
     * @param pFirstIdx The first entity index withing the result list. This
     * index must be &gt;= 0. Values smaller than 0 should be set 0. The result
     * for the case, where <i>pFirstIdx</i> is larger than the max. amount of
     * entries is not defined!
     * @param pResultCount The maximum number of results returned by one call.
     * This value should be &gt;= 1. If the value is smaller than 1, the value
     * has to be <b>ignored</b> by the implementation.
     *
     * @return The result list which might be empty.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> List<T> findResultList(String queryString, Object[] pParameters, Class<T> entityClass, int pFirstIdx, int pResultCount) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a single entity of
     * the provided entity class. If the query is correct, this method should
     * succeed. If no result was found, <i>null</i> should be returned.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param <T> any class supported by the chosen model
     * @param queryString The plain SQL query string
     * @param entityClass The entity class
     *
     * @return The result or <i>null</i> if no result was found.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> T findSingleResult(String queryString, Class<T> entityClass) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a single entity of
     * the provided entity class. If the query is correct, this method should
     * succeed. If no result was found, <i>null</i> should be returned. In
     * contrast to {@link #findSingleResult(java.lang.String, java.lang.Class)
     * } this method is intended to be used if the query contains parameters. It
     * should be guaranteed that parameters are handled properly, e.g. by
     * escaping them to avoid SQL-injections or similar vulnerabilities. The
     * paramters are added to the query by providing variables according to
     * their position in the parameters array beginning with 1. Therefore, the
     * first parameter in the parameter array replaces the variable named
     * <b>?1</b> in the query.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param <T> any class supported by the chosen model
     * @param queryString The plain SQL query string
     * @param pParameters The parameter array used to replace parameters in the
     * query string.
     * @param entityClass The entity class
     *
     * @return The result or <i>null</i> if no result was found.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> T findSingleResult(String queryString, Object[] pParameters, Class<T> entityClass) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a single object. The
     * type of the returned entity is undefined and has to be determined later
     * by 'instanceof' check or by an appropriate typecast. This method is
     * intended to be used for queries returning no entity, e.g. a count query
     * in JPQL/SQL, but should be applicable also for queries for entities.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param queryString The plain SQL query string
     *
     * @return The result object or null if no result was obtained.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    Object findSingleResult(String queryString) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a single object. The
     * type of the returned entity is undefined and has to be determined later
     * by 'instanceof' check or by an appropriate typecast. This method is
     * intended to be used for queries returning no entity, e.g. a count query
     * in JPQL/SQL, but should be applicable also for queries for entities. In
     * contrast to {@link #findSingleResult(java.lang.String)
     * } this method is intended to be used if the query contains parameters. It
     * should be guaranteed that parameters are handled properly, e.g. by
     * escaping them to avoid SQL-injections or similar vulnerabilities. The
     * paramters are added to the query by providing variables according to
     * their position in the parameters array beginning with 1. Therefore, the
     * first parameter in the parameter array replaces the variable named
     * <b>?1</b> in the query.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param queryString The plain SQL query string
     * @param pParameters The parameter array used to replace parameters in the
     * query string.
     *
     * @return The result object or null if no result was obtained.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    Object findSingleResult(String queryString, Object[] pParameters) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a list of entities.
     * The type of the returned entities is undefined and has to be determined
     * later by 'instanceof' check or by an appropriate typecast.
     *
     * <b>Attention</b>: It is recommended to use the typed version of this
     * method where possible as the untyped query has huge disadvantages
     * regarding performance.
     *
     * @param queryString The plain SQL query string
     *
     * @return The result list which may be empty if no entity fits the query.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    List findResultList(String queryString) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a list of entities.
     * The type of the returned entities is undefined and has to be determined
     * later by 'instanceof' check or by an appropriate typecast. In contrast to {@link #findResultList(java.lang.String)
     * } this method is intended to be used if the query contains parameters. It
     * should be guaranteed that parameters are handled properly, e.g. by
     * escaping them to avoid SQL-injections or similar vulnerabilities. The
     * paramters are added to the query by providing variables according to
     * their position in the parameters array beginning with 1. Therefore, the
     * first parameter in the parameter array replaces the variable named
     * <b>?1</b> in the query.
     *
     * <b>Attention</b>: It is recommended to use the typed version of this
     * method where possible as the untyped query has huge disadvantages
     * regarding performance.
     *
     * @param queryString The plain SQL query string
     * @param pParameters The parameter array used to replace parameters in the
     * query string.
     *
     * @return The result list which may be empty if no entity fits the query.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    List findResultList(String queryString, Object[] pParameters) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a list of entities.
     * The type of the returned entities is undefined and has to be determined
     * later by 'instanceof' check or by an appropriate typecast. The result
     * list has a fixed maximum size defined by pFirstIdx and pResultCount.
     *
     * <b>Attention</b>: It is recommended to use the typed version of this
     * method where possible as the untyped query has huge disadvantages
     * regarding performance.
     *
     * @param queryString The plain SQL query string
     * @param pFirstIdx The first entity index withing the result list. This
     * index must be &gt;= 0. Values smaller than 0 should be set 0. The result
     * for the case, where <i>pFirstIdx</i> is larger than the max. amount of
     * entries is not defined!
     * @param pResultCount The maximum number of results returned by one call.
     * This value should be &gt;= 1. If the value is smaller than 1, the value
     * has to be <b>ignored</b> by the implementation.
     *
     * @return The result list with the size pResultCount or an empty list if
     * not entity fits the query.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    List findResultList(String queryString, int pFirstIdx, int pResultCount) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native query which is expected to return a list of entities.
     * The type of the returned entities is undefined and has to be determined
     * later by 'instanceof' check or by an appropriate typecast. The result
     * list has a fixed maximum size defined by pFirstIdx and pResultCount. In
     * contrast to {@link #findResultList(java.lang.String, int, int)
     * } this method is intended to be used if the query contains parameters. It
     * should be guaranteed that parameters are handled properly, e.g. by
     * escaping them to avoid SQL-injections or similar vulnerabilities. The
     * paramters are added to the query by providing variables according to
     * their position in the parameters array beginning with 1. Therefore, the
     * first parameter in the parameter array replaces the variable named
     * <b>?1</b> in the query.
     *
     * <b>Attention</b>: It is recommended to use the typed version of this
     * method where possible as the untyped query has huge disadvantages
     * regarding performance.
     *
     * @param queryString The plain SQL query string
     * @param pParameters The parameter array used to replace parameters in the
     * query string.
     * @param pFirstIdx The first entity index withing the result list. This
     * index must be &gt;= 0. Values smaller than 0 should be set 0. The result
     * for the case, where <i>pFirstIdx</i> is larger than the max. amount of
     * entries is not defined!
     * @param pResultCount The maximum number of results returned by one call.
     * This value should be &gt;= 1. If the value is smaller than 1, the value
     * has to be <b>ignored</b> by the implementation.
     *
     * @return The result list with the size pResultCount or an empty list if
     * not entity fits the query.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    List findResultList(String queryString, Object[] pParameters, int pFirstIdx, int pResultCount) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native update which is expected to return the number of
     * affected entities. This method is intended to be used for
     * high-performance updates on single fields of an entity. Compared to
     * {@link #update(java.lang.Object)} this method will not have to check the
     * entire object graph but will directly modify the entity. This leads to a
     * huge performance advantage, especially for very big object graphs with
     * many entities.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param updateString The plain SQL update string.
     *
     * @return The number of affected entities.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    Integer performUpdate(String updateString) throws UnauthorizedAccessAttemptException;

    /**
     * Performs a native update which is expected to return the number of
     * affected entities. This method is intended to be used for
     * high-performance updates on single fields of an entity. Compared to
     * {@link #update(java.lang.Object)} this method will not have to check the
     * entire object graph but will directly modify the entity. This leads to a
     * huge performance advantage, especially for very big object graphs with
     * many entities. In contrast to {@link #performUpdate(java.lang.String)
     * } this method is intended to be used if the update contains parameters.
     * It should be guaranteed that parameters are handled properly, e.g. by
     * escaping them to avoid SQL-injections or similar vulnerabilities. The
     * paramters are added to the update query by providing variables according
     * to their position in the parameters array beginning with 1. Therefore,
     * the first parameter in the parameter array replaces the variable named
     * <b>?1</b> in the query.
     *
     * Implementation specific runtime exceptions which may occur should be
     * logged as trace messages.
     *
     * @param updateString The plain SQL update string.
     * @param pParameters The parameter array used to replace parameters in the
     * update string.
     *
     * @return The number of affected entities.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    Integer performUpdate(String updateString, Object[] pParameters) throws UnauthorizedAccessAttemptException;

    /**
     * Save the provided entity. This method may be used for saving entities for
     * the first time (perform an initial persist) or for saving changed
     * entities which were already persisted. Therefore, this method should
     * check if the entity is known, first, e.g. via <i>contains(entity)</i>. If
     * this method returns an entity, this should be the representation of the
     * entity at the backend. If the operation fails, a RuntimeException should
     * occur.
     *
     * If the entity does not exist yet, it should be persisted.
     *
     * During the save-process the entity may be changed by the implementation
     * (e.g. a unique id is assigned by JPA) or its reference changes.
     * Therefore, the argument <i>entity</i> should not be used after this
     * method call anymore. Instead, the returned entity is the actual, managed
     * entity which should be used for subsequent accesses.
     *
     * @param <T> any class supported by the chosen model Make a persist or
     * update.
     * @param entity entity to save.
     *
     * @return The saved entity, which may or may not different to the provided
     * argument <i>entity</i>.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> T save(T entity) throws UnauthorizedAccessAttemptException;

    /**
     * Persist the provided entity. This method is intended to be used for
     * initial persist operations. It has to check, wheather the entity already
     * exists or not. If it exists, an exception will be thrown. If not, the new
     * entity will be persisted. If the operation fails, a RuntimeException
     * should occur.
     *
     * During the persist-process the entity may be changed by the
     * implementation (e.g. a unique id is assigned by JPA) or its reference
     * changes. Therefore, the argument <i>entity</i> should not be used after
     * this method call anymore. Instead, the returned entity is the actual,
     * managed entity which should be used for subsequent accesses.
     *
     * @param <T> any class supported by the chosen model
     * @param entity instance to persist.
     *
     * @return The persisted entity.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     */
    <T> T persist(T entity) throws UnauthorizedAccessAttemptException;

    /**
     * Update the provided entity. This method is used to update an existing
     * entity. If the entity does not exist (contains(entity) returns 'false'),
     * an EntityNotFoundException should be thrown.
     *
     * If the entity exists, the new state should be persisted and the updated
     * entity is returned.
     *
     * During the update-process the entity may be changed by the implementation
     * or its reference changes. Therefore, the argument <i>entity</i> should
     * not be used after this method call anymore. Instead, the returned entity
     * is the actual, managed entity which should be used for subsequent
     * accesses.
     *
     * @param <T> any class supported by the chosen model Make a persist or
     * update.
     * @param entity entity to persist.
     *
     * @return The updated entity.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     * @throws EntityNotFoundException If the entity was not persisted before.
     */
    <T> T update(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException;

    /**
     * Reloads the state of the provided entity from the backend. This method is
     * used to withdraw changes made to the entity after the entity was read the
     * last time from the backend, e.g. via find- or update-operation.
     *
     * This only works for entities which were persisted before. For new
     * entities, an EntityNotFoundException should be thrown.
     *
     * During the update-process the entity may be changed by the implementation
     * or its reference changes. Therefore, the argument <i>entity</i> should
     * not be used after this method call anymore. Instead, the returned entity
     * is the actual, managed entity which should be used for subsequent
     * accesses.
     *
     * Attention: As this method reads the current state of the entity from the
     * backend and the state might have changed in the backend since the last
     * access, the state of the result entity might be different from the last
     * known state, e.g. if another user has changed the entity in the meantime.
     *
     * @param <T> any class supported by the chosen model
     * @param entity instance to persist.
     *
     * @return Entity which reflects the state of the entity within the backend.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     * @throws EntityNotFoundException entity does not exist
     */
    <T> T refresh(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException;

    /**
     * Remove the provided entity. This method removes the entity from the
     * backend. After a call to <i>remove(entity)</i>, a call to
     * <i>contains(entity)</i> should return 'false'. A subsequent call to
     * <i>update(entity)</i> or <i>refresh(entity)</i> should result in an
     * EntityNotFoundException. A call so <i>save(entity)</i> should persist the
     * entity again, but this may change the entity (e.g. by a new unique entity
     * id in case of using JPA)
     *
     * @param <T> any class supported by the chosen model
     * @param entity Entity to remove.
     *
     * @throws UnauthorizedAccessAttemptException Access not allowed for this
     * context.
     * @throws EntityNotFoundException entity is not available
     */
    <T> void remove(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException;

    /**
     * <b>Attention:</b> Entity manager has to be closed at the end of the
     * 'session'. Not explicitly closing entity manager will cause persistence
     * errors. Close entity manager.
     */
    void close();

    void addProperty(String key, Object value);

    void removeProperty(String key);

    void removeAllProperties();

    Map<String, Object> getProperties();
}
