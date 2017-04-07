/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.mdm.core.jpa;

import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;

/**
 *
 * @author mf6319
 */
public final class EntityManagerHelper {

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
    public static <T> Object getIdOfEntity(Class entityClass, T entity) throws UnauthorizedAccessAttemptException {
        Object returnValue = null;
        boolean idFound = false; // only one Id per class hierarchie.

        if (entityClass.isAnnotationPresent(Entity.class) || entityClass.isAnnotationPresent(MappedSuperclass.class)) {
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                Id entityId = field.getAnnotation(Id.class);
                if (entityId != null) {
                    idFound = true;
                    field.setAccessible(true);
                    try {
                        returnValue = field.get(entity);
                    } catch (IllegalAccessException iae) {
                        throw new UnauthorizedAccessAttemptException("Failed to access value of field '" + field.getName() + "' annotated by 'Id", iae);
                    } finally {
                        field.setAccessible(false);
                        // exit loop
                        break;
                    }
                }
            }

            if (!idFound) {
                returnValue = getIdOfEntity(entityClass.getSuperclass(), entity);
            }
        } else {
            throw new IllegalArgumentException(entity.getClass().toString() + " is not an Entity!");
        }
        return returnValue;
    }

    /**
     * Returns the field name annotated with <code>@Id</code> of the provided
     * entity. If no annotated field was found, an IllegalArgumentException is
     * thrown.
     *
     * @param entityClass The entity class.
     *
     * @return The name of the field annotated with <code>@Id</code>
     *
     * @throws UnauthorizedAccessAttemptException If the Id field cannot be
     * accessed.
     */
    public static String getIdFieldName(Class entityClass) throws UnauthorizedAccessAttemptException {
        String returnValue = null;
        boolean idFound = false; // only one Id per class hierarchie.

        if (entityClass.isAnnotationPresent(Entity.class) || entityClass.isAnnotationPresent(MappedSuperclass.class)) {
            Field[] fields = entityClass.getDeclaredFields();
            for (Field field : fields) {
                Id entityId = field.getAnnotation(Id.class);
                if (entityId != null) {
                    idFound = true;
                    returnValue = field.getName();
                }
            }

            if (!idFound) {
                returnValue = getIdFieldName(entityClass.getSuperclass());
            }
        } else {
            throw new IllegalArgumentException(entityClass.toString() + " is not an Entity!");
        }
        return returnValue;
    }

    /**
     * Returns the entity table name obtained from the 'Table' annotation of the
     * entity class or one of its super classes. If no such annotation is found,
     * the class name of the entity class is returned.
     *
     * @param entityClass The entity class.
     *
     * @return The value of the <code>@Table</code> annotation.
     */
    public static String getEntityTableName(Class entityClass) {
        String returnValue = null;
        if ((entityClass.isAnnotationPresent(Entity.class) || entityClass.isAnnotationPresent(MappedSuperclass.class)) && entityClass.isAnnotationPresent(Table.class)) {
            Annotation table = entityClass.getAnnotation(Table.class);
            try {
                Method m = Table.class.getMethod("name");
                returnValue = (String) m.invoke(table, (Object[]) null);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                //not possible...returnValue stays null and the super type will be checked
            }
        }

        //no return Value from current class...check super class or return simple class name
        if (returnValue == null && entityClass.getSuperclass() != null && (entityClass.getSuperclass().isAnnotationPresent(Entity.class) || entityClass.getSuperclass().isAnnotationPresent(MappedSuperclass.class))) {
            return getEntityTableName(entityClass.getSuperclass());
        } else if (returnValue == null) {
            //no annotation available, return simple class name
            returnValue = entityClass.getSimpleName();
        }
        return returnValue;
    }

    /**
     * Make the provided entity managed. This is needed for some JPA operations,
     * e.g. update or reload. Normally, the managed entity represented by
     * pUnmanagedEntity will be returned. If not unmanaged entity is not an
     * entity or the Id of the unmanaged entity cannot be obtained, an
     * UnauthorizedAccessAttemptException is thrown.
     *
     * @param <T> Generic type.
     * @param pEntityManager The entity manager used to perform the query.
     * @param pUnmanagedEntity The unmanaged entity.
     *
     * @return The managed entity.
     *
     * @throws UnauthorizedAccessAttemptException If the Id field of the
     * unmanaged entity could not be accessed.
     */
    public static <T> T obtainManagedEntity(EntityManager pEntityManager, T pUnmanagedEntity) throws UnauthorizedAccessAttemptException {
        return pEntityManager.find((Class<T>) pUnmanagedEntity.getClass(), getIdOfEntity(pUnmanagedEntity.getClass(), pUnmanagedEntity));
    }
}
