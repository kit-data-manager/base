/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 * (support@kitdatamanager.net)
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
package edu.kit.dama.mdm.core.authorization;

import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.FilterOutput;
import edu.kit.dama.authorization.annotations.SecuredArgument;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.ISecurableResource;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.AuthorizationException;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.ResourceServiceLocal;
import edu.kit.dama.authorization.services.base.PlainAuthorizerLocal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for all Implementations of database connections. Each instance is
 * linked with one entity manager which has explicitly closed at the end.
 * <code>metaDataManager.close();</code> <b>Attention: </b>All entities
 * implementing ISecurableInterface need an authorizationContext.
 *
 * @author hartmann-v
 */
public final class SecureMetaDataManager implements IMetaDataManager {

  /**
   * For logging purposes.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SecureMetaDataManager.class);
  /**
   * Authorization Context for secure Access.
   */
  private IAuthorizationContext authCtx = null;
  /**
   * Implementation of MetaDataManager.
   */
  IMetaDataManager impl = null;

  /**
   * Factory a new instance of a SecureMetaDataManager using the provided
   * context.
   *
   * @param ctx The context.
   *
   * @return The metadata manager.
   */
  public static IMetaDataManager factorySecureMetaDataManager(IAuthorizationContext ctx) {
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(ctx);
    return mdm;
  }

  /**
   * Default constructor.
   *
   * @param implementation The meta data manager implementation.
   */
  public SecureMetaDataManager(IMetaDataManager implementation) {
    impl = implementation;
  }

  @Override
  public void setAuthorizationContext(IAuthorizationContext authorizationContext) {
    authCtx = authorizationContext;
  }

  /**
   * Check whether instance of object is managed by the given RepositoryManager.
   *
   * @param <T> any class supported by the chosen model.
   * @param object instance of an object.
   * @param authorizationContext authorization context.
   *
   * @return Managed or not.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   * @throws EntityNotFoundException entity is not available
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  public <T> boolean contains(@SecuredArgument T object, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    return impl.contains(object);
  }

  /**
   * Performs a native query which is expected to return a list of entities of
   * the provided entity class.
   *
   * @param <T> Generic result type.
   * @param queryString The plain SQL query string.
   * @param entityClass The entity class.
   * @param authorizationContext authorization context.
   *
   * @return The result list.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  @FilterOutput(roleRequired = Role.GUEST)
  <T> List<T> findResultList(String queryString, Class<T> entityClass, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return impl.findResultList(queryString, entityClass);
  }

  /**
   * Performs a native query which is expected to return a list of entities of
   * the provided entity class.
   *
   * @param <T> Generic result type.
   * @param queryString The plain SQL query string.
   * @param pParameters Parameters used in queryString.
   * @param entityClass The entity class.
   * @param authorizationContext authorization context.
   *
   * @return The result list.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  @FilterOutput(roleRequired = Role.GUEST)
  <T> List<T> findResultList(String queryString, Object[] pParameters, Class<T> entityClass, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return impl.findResultList(queryString, pParameters, entityClass);
  }

  /**
   * Performs a native query which is expected to return a list of entities of
   * the provided entity class. The result list has a fixed maximum size defined
   * by pFirstIdx and pResultCount.
   *
   * @param <T> Generic result type.
   * @param queryString The plain SQL query string.
   * @param entityClass The entity class.
   * @param pFirstIdx The first entity index withing the result list.
   * @param pResultCount The maximum number of results returned by one call.
   * @param authorizationContext authorization context.
   *
   * @return The result list.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  @FilterOutput(roleRequired = Role.GUEST)
  <T> List<T> findResultList(String queryString, Class<T> entityClass, int pFirstIdx, int pResultCount, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, null, entityClass, pFirstIdx, pResultCount, authorizationContext);
  }

  /**
   * Performs a native query which is expected to return a list of entities of
   * the provided entity class. The result list has a fixed maximum size defined
   * by pFirstIdx and pResultCount.
   *
   * @param <T> Generic result type.
   * @param queryString The plain SQL query string.
   * @param pParameters Parameters used in queryString.
   * @param entityClass The entity class.
   * @param pFirstIdx The first entity index withing the result list.
   * @param pResultCount The maximum number of results returned by one call.
   * @param authorizationContext authorization context.
   *
   * @return The result list.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  @FilterOutput(roleRequired = Role.GUEST)
  <T> List<T> findResultList(String queryString, Object[] pParameters, Class<T> entityClass, int pFirstIdx, int pResultCount, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return impl.findResultList(queryString, pParameters, entityClass, pFirstIdx, pResultCount);
  }

  /**
   * Performs a native query which is expected to return a single entity of the
   * provided entity class.
   *
   * @param <T> Generic result type.
   * @param queryString The plain SQL query string.
   * @param entityClass The entity class.
   * @param authorizationContext authorization context.
   *
   * @return The result.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  <T> T findSingleResult(String queryString, Class<T> entityClass, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return findSingleResult(queryString, null, entityClass, authorizationContext);
  }

  /**
   * Performs a native query which is expected to return a single entity of the
   * provided entity class.
   *
   * @param <T> Generic result type.
   * @param queryString The plain SQL query string.
   * @param pParameters Parameters used in queryString.
   * @param entityClass The entity class.
   * @param authorizationContext authorization context.
   *
   * @return The result.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  <T> T findSingleResult(String queryString, Object[] pParameters, Class<T> entityClass, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    T entity;
    entity = impl.findSingleResult(queryString, pParameters, entityClass);
    entity = filterEntity(entity, authorizationContext);
    return entity;
  }

  /**
   * Performs a native query which is expected to return a single entity. The
   * type of the returned entity is undefined and has to be determined later by
   * 'instanceof' check or by an appropriate typecast.
   *
   * @param queryString The plain SQL query string.
   * @param authorizationContext authorization context.
   *
   * @return The result object.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  Object findSingleResult(String queryString, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return findSingleResult(queryString, (Object[]) null, authorizationContext);
  }

  /**
   * Performs a native query which is expected to return a single entity. The
   * type of the returned entity is undefined and has to be determined later by
   * 'instanceof' check or by an appropriate typecast.
   *
   * @param queryString The plain SQL query string.
   * @param pParameters Parameters used in queryString
   * @param authorizationContext authorization context.
   *
   * @return The result object.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  Object findSingleResult(String queryString, Object[] pParameters, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    Object entity;
    entity = impl.findSingleResult(queryString, pParameters);
    entity = filterEntity(entity, authorizationContext);
    return entity;
  }

  /**
   * Performs a native query which is expected to return a list of entities. The
   * type of the returned entities is undefined and has to be determined later
   * by 'instanceof' check or by an appropriate typecast.
   *
   * @param queryString The plain SQL query string.
   * @param authorizationContext authorization context.
   *
   * @return The result list.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  List findResultList(String queryString, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, (Object[]) null, authorizationContext);
  }

  /**
   * Performs a native query which is expected to return a list of entities. The
   * type of the returned entities is undefined and has to be determined later
   * by 'instanceof' check or by an appropriate typecast.
   *
   * @param queryString The plain SQL query string.
   * @param pParameters Parameters used in queryString.
   * @param authorizationContext authorization context.
   *
   * @return The result list.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  List findResultList(String queryString, Object[] pParameters, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    List entityList;
    entityList = impl.findResultList(queryString, pParameters);
    return filterList(entityList, authorizationContext);
  }

  /**
   * Performs a native query which is expected to return a list of entities. The
   * type of the returned entities is undefined and has to be determined later
   * by 'instanceof' check or by an appropriate typecast. The result list has a
   * fixed maximum size defined by pFirstIdx and pResultCount.
   *
   * @param queryString The plain SQL query string.
   * @param pFirstIdx The first entity index within the result list.
   * @param pResultCount The maximum number of results returned by one call.
   * @param authorizationContext authorization context.
   *
   * @return The result list.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  List findResultList(String queryString, int pFirstIdx, int pResultCount, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, (Object[]) null, pFirstIdx, pResultCount, authorizationContext);
  }

  /**
   * Performs a native query which is expected to return a list of entities. The
   * type of the returned entities is undefined and has to be determined later
   * by 'instanceof' check or by an appropriate typecast. The result list has a
   * fixed maximum size defined by pFirstIdx and pResultCount.
   *
   * @param queryString The plain SQL query string.
   * @param pParameters Parameters used in queryString.
   * @param pFirstIdx The first entity index within the result list.
   * @param pResultCount The maximum number of results returned by one call.
   * @param authorizationContext authorization context.
   *
   * @return The result list.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  List findResultList(String queryString, Object[] pParameters, int pFirstIdx, int pResultCount, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    List entityList;
    entityList = impl.findResultList(queryString, pParameters, pFirstIdx, pResultCount);
    return filterList(entityList, authorizationContext);
  }

  /**
   * Performs a native update which is expected to return the number of affected
   * rows.
   *
   * @param udpateString The plain SQL update string.
   * @param pParameters Parameters used in udpateString
   * @param authorizationContext authorization context.
   *
   * @return The number of affected entities..
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.MEMBER)
  Integer performUpdate(String udpateString, Object[] pParameters, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return impl.performUpdate(udpateString, pParameters);
  }

  /**
   * Filter a list of objects. All entities which implement the
   * ISecurableResource interface will be checked for access rights (Role.GUEST)
   *
   * @param entityList List with all entities.
   * @param authorizationContext authorization context
   *
   * @return The filtered list.
   */
  private List filterList(List entityList, IAuthorizationContext authorizationContext) {
    List resultList = new ArrayList();
    for (Object entity : entityList) {
      entity = filterEntity(entity, authorizationContext);
      if (entity != null) {
        resultList.add(entity);
      }
    }
    return resultList;
  }

  /**
   * Filter a single object. If no access allowed null will returned.
   *
   * @param <T> The generic type.
   * @param entity Any entity.
   * @param authorizationContext authorization context.
   *
   * @return Entity if access allowed null otherwise.
   */
  private <T> T filterEntity(T entity, IAuthorizationContext authorizationContext) {
    T result = entity;
    if (entity instanceof ISecurableResource) {
      ISecurableResource secureEntity = (ISecurableResource) entity;
      try {
        // Check access rights for entity!
        PlainAuthorizerLocal.authorize(authorizationContext, secureEntity.getSecurableResourceId(), Role.GUEST);
      } catch (AuthorizationException ex) {
        result = null;
        LOGGER.error(null, ex);
      }
    }
    return result;
  }

  /**
   * Find all instances of the given class.
   *
   * @param <T> Any class supported by the chosen model.
   * @param entityClass reference class.
   * @param authorizationContext authorization context.
   *
   * @return List with all instances of <T>.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  @FilterOutput(roleRequired = Role.GUEST)
  <T> List<T> find(Class<T> entityClass, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return impl.find(entityClass);
  }

//  /**
//   * Find all visible instances of the given class. (depends on role)
//   * @param <U extends ISecurableResource> any class supported security by the chosen model
//   * @param entityClass  reference class
//   * @return List with all instances of <T>
//   */
//   @FilteredMethod(roleRequired = Role.GUEST)
//   <U extends ISecurableResource> List<U> find(Class<U> entityClass, @Context IAuthorizationContext ctx);
  /**
   * Find instance by primary key. Primary key should always an instance of
   * 'Long'.
   *
   * @param <T> any class supported by the chosen model.
   * @param entityClass class of instance.
   * @param primaryKey primary key of instance.
   * @param authorizationContext authorization context.
   *
   * @return the found entity instance or null if the entity does not exist.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   * @throws EntityNotFoundException If no entity was found for the provided
   * primary key.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  <T> T find(Class<T> entityClass, Object primaryKey, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    T entity = impl.find(entityClass, primaryKey);
    ISecurableResource secureEntity = (ISecurableResource) entity;
    if (secureEntity != null) {
      // Check access rights for entity!
      PlainAuthorizerLocal.authorize(authorizationContext, secureEntity.getSecurableResourceId(), Role.GUEST);
    } else {
      throw new EntityNotFoundException("Data base for Entity " + entityClass + " has no entry with PK '" + primaryKey.toString() + "'!");
    }

    return entity;
  }

  /**
   * Find instances between the given boundaries defined by the two instances.
   *
   * @param <T> any class supported by the chosen model
   * @param first Lower bound.
   * @param last Upper bound.
   * @param authorizationContext Authorization context.
   *
   * @return List with all instances fulfilling the criteria. (List may be
   * empty.)
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  @FilterOutput(roleRequired = Role.GUEST)
  <T> List<T> find(T first, T last, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    return impl.find(first, last);
  }

  /**
   * Save the instance.
   *
   * @param <T> any class supported by the chosen model Make a persist or
   * update.
   * @param entity instance to persist.
   * @param authorizationContext authorization context.
   *
   * @return The saved entity.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   * @throws EntityNotFoundException If the entity was not persisted before.
   */
  @SecuredMethod(roleRequired = Role.MEMBER)
  <T> T save(@SecuredArgument T entity, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    return impl.save(entity);
  }

  /**
   * Persist the instance. Make the instance available for the persistence
   * framework. This method is called internally only for resources implementing
   * the ISecurableResource interface.
   *
   * @param <T> any class supported by the chosen model.
   * @param entity instance to persist.
   * @param authorizationContext authorization context.
   *
   * @return The persisted entity.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   */
  @SecuredMethod(roleRequired = Role.MEMBER)
  <T> T persist(T entity, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException {
    T returnValue = impl.persist(entity);
    LOGGER.debug("Persist successful. Registering resource.");
    ISecurableResource resource = (ISecurableResource) returnValue;
    // Register resource for group with MANAGER access as default.
    // Only manager of group is allowed to remove this resource.
    try {
      LOGGER.debug("Registering resource id {}", resource.getSecurableResourceId());
      ResourceServiceLocal.getSingleton().registerResource(resource.getSecurableResourceId(),
              authorizationContext.getGroupId(),
              Role.MANAGER,
              AuthorizationContext.factorySystemContext());
    } catch (EntityNotFoundException ex) {
      LOGGER.error("Failed to register resource. Probably, the provided groupId provided in the caller's context (" + authorizationContext.getGroupId() + ") does not exist.", ex);
    }
    return returnValue;
  }

  /**
   * Update the instance. Throws exception if instance was not persisted before.
   *
   * @param <T> any class supported by the chosen model Make a persist or
   * update.
   * @param entity instance to persist.
   * @param authorizationContext authorization context.
   *
   * @return entity attached to database.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   * @throws EntityNotFoundException entity is not available
   */
  @SecuredMethod(roleRequired = Role.MEMBER)
  public <T> T update(@SecuredArgument T entity, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    return impl.update(entity);
  }

  /**
   * Refresh the state of the instance from the database, overwriting changes
   * made to the entity, if any.
   *
   * @param <T> any class supported by the chosen model.
   * @param entity instance to persist.
   * @param authorizationContext authorization context.
   *
   * @return managed entity of the entity.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   * @throws EntityNotFoundException Entity is not available.
   */
  @SecuredMethod(roleRequired = Role.GUEST)
  public <T> T refresh(@SecuredArgument T entity, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    return refreshImpl(entity);
  }

  /**
   * Implementation of refresh.
   *
   * @param <T> Any entity.
   * @param entity entity to refresh.
   *
   * @return Entity with 'old' values.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this
   * context.
   * @throws EntityNotFoundException Entity is not available.
   */
  private <T> T refreshImpl(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    return impl.refresh(entity);
  }

  /**
   * Remove the instance from the database.
   *
   * @param <T> any class supported by the chosen model.
   * @param entity instance to remove.
   * @param authorizationContext authorization context.
   *
   * @throws UnauthorizedAccessAttemptException Access not allowed for this.
   * context.
   * @throws EntityNotFoundException entity is not available.
   */
  @SecuredMethod(roleRequired = Role.MANAGER)
  public <T> void remove(@SecuredArgument T entity, @Context IAuthorizationContext authorizationContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    ISecurableResource securableResource = (ISecurableResource) entity;
    // Remove all registered Users/Groups
    try {
      ResourceServiceLocal.getSingleton().remove(securableResource.getSecurableResourceId(), authorizationContext);
    } catch (EntityNotFoundException enfe) {
      // ignore
    }

    impl.remove(entity);
  }

  @Override
  public void close() {
    impl.close();
  }

  @Override
  public <T> boolean contains(T object) throws UnauthorizedAccessAttemptException {
    if ((authCtx != null) && (object instanceof ISecurableResource)) {
      boolean exists;
      try {
        exists = contains(object, authCtx);
      } catch (EntityNotFoundException enfe) {
        // entity doesn't exist
        exists = false;
      }
      return exists;
    } else {
      return impl.contains(object);
    }
  }

  @Override
  public <T> List<T> find(Class<T> entityClass) throws UnauthorizedAccessAttemptException {
    if (ISecurableResource.class.isAssignableFrom(entityClass)) {
      return find(entityClass, authCtx);
    } else {
      return impl.find(entityClass);
    }
  }

  @Override
  public <T> T find(Class<T> entityClass, Object primaryKey) throws UnauthorizedAccessAttemptException {
    T returnValue = null;
    if (ISecurableResource.class.isAssignableFrom(entityClass)) {
      try {
        return find(entityClass, primaryKey, authCtx);
      } catch (EntityNotFoundException ex) {
        LOGGER.error("Failed to find secured entity. Returning 'null'.", ex);

      }
    } else {
      returnValue = impl.find(entityClass, primaryKey);
    }
    return returnValue;
  }

  @Override
  public <T> List<T> find(T first, T last) throws UnauthorizedAccessAttemptException {
    boolean secure = false;
    if (first != null && first instanceof ISecurableResource) {
      secure = true;
    }
    if (last != null && last instanceof ISecurableResource) {
      secure = true;
    }
    if (secure) {
      return find(first, last, authCtx);
    } else {
      return impl.find(first, last);
    }
  }

  @Override
  public <T> T save(T entity) throws UnauthorizedAccessAttemptException {
    T returnValue = entity;
    if (entity instanceof ISecurableResource) {
      if (!impl.contains(entity)) {
        //entity is not persisted
        //this check is needed for SYS_ADMIN access!!
        persist(entity, authCtx);
      } else {
        //entity is persisted, perform a normal save for normal users
        try {
          returnValue = update(entity, authCtx);
        } catch (EntityNotFoundException ex) {
          //this should never happen! ... yes, never ever! Really! :-D
          throw new UnauthorizedAccessAttemptException("Failed to save entity. The entity does either not exist or is not properly registered by the authorization.", ex);
        }
      }
    } else {
      returnValue = impl.save(entity);
    }
    return returnValue;
  }

  @Override
  public <T> T persist(T entity) throws UnauthorizedAccessAttemptException {
    T returnValue;
    if ((authCtx != null) && (entity instanceof ISecurableResource)) {
      returnValue = persist(entity, authCtx);
    } else {
      returnValue = impl.persist(entity);
    }
    return returnValue;
  }

  @Override
  public <T> T update(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    if ((authCtx != null) && (entity instanceof ISecurableResource)) {
      return update(entity, authCtx);
    } else {
      return impl.update(entity);
    }
  }

  @Override
  public <T> T refresh(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    T returnValue;
    if (entity instanceof ISecurableResource) {
      returnValue = refresh(entity, authCtx);
    } else {
      returnValue = refreshImpl(entity);
    }
    return returnValue;
  }

  @Override
  public <T> void remove(T entity) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
    if (entity instanceof ISecurableResource) {
      remove(entity, authCtx);
    } else {
      if (impl.contains(entity)) {
        impl.remove(entity);
      } else {
        throw new EntityNotFoundException("Entity '" + entity + "' doesn't exist! Can't remove it.");
      }
    }
  }

  @Override
  public <T> List<T> findResultList(String queryString, Class<T> entityClass) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, null, entityClass);
  }

  @Override
  public <T> List<T> findResultList(String queryString, Object[] pParameters, Class<T> entityClass) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, pParameters, entityClass, 0, Integer.MAX_VALUE);
  }

  @Override
  public <T> List<T> findResultList(String queryString, Class<T> entityClass, int pFirstIdx, int pResultCount) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, null, entityClass, pFirstIdx, pResultCount);
  }

  @Override
  public <T> List<T> findResultList(String queryString, Object[] pParameters, Class<T> entityClass, int pFirstIdx, int pResultCount) throws UnauthorizedAccessAttemptException {
    if (ISecurableResource.class.isAssignableFrom(entityClass)) {
      return findResultList(queryString, pParameters, entityClass, pFirstIdx, pResultCount, authCtx);
    } else {
      return impl.findResultList(queryString, pParameters, entityClass, pFirstIdx, pResultCount);
    }
  }

  @Override
  public <T> T findSingleResult(String queryString, Class<T> entityClass) throws UnauthorizedAccessAttemptException {
    return findSingleResult(queryString, null, entityClass);
  }

  @Override
  public <T> T findSingleResult(String queryString, Object[] pParameters, Class<T> entityClass) throws UnauthorizedAccessAttemptException {
    if (ISecurableResource.class.isAssignableFrom(entityClass)) {
      return findSingleResult(queryString, pParameters, entityClass, authCtx);
    } else {
      return impl.findSingleResult(queryString, pParameters, entityClass);
    }
  }

  @Override
  public Object findSingleResult(String queryString) throws UnauthorizedAccessAttemptException {
    return findSingleResult(queryString, (Object[]) null);
  }

  @Override
  public Object findSingleResult(String queryString, Object[] pParameters) throws UnauthorizedAccessAttemptException {
    return findSingleResult(queryString, pParameters, authCtx);
  }

  @Override
  public List findResultList(String queryString) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, (Object[]) null);
  }

  @Override
  public List findResultList(String queryString, Object[] pParameters) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, pParameters, authCtx);
  }

  @Override
  public List findResultList(String queryString, int pFirstIdx, int pResultCount) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, (Object[]) null, pFirstIdx, pResultCount, authCtx);
  }

  @Override
  public List findResultList(String queryString, Object[] pParameters, int pFirstIdx, int pResultCount) throws UnauthorizedAccessAttemptException {
    return findResultList(queryString, pParameters, pFirstIdx, pResultCount, authCtx);
  }

  @Override
  public Integer performUpdate(String queryString) throws UnauthorizedAccessAttemptException {
    return performUpdate(queryString, (Object[]) null, authCtx);
  }

  @Override
  public Integer performUpdate(String queryString, Object[] pParameters) throws UnauthorizedAccessAttemptException {
    return performUpdate(queryString, pParameters, authCtx);
  }

}
