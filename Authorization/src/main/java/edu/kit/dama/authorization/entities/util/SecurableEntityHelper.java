/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
package edu.kit.dama.authorization.entities.util;

import edu.kit.dama.authorization.annotations.SecurableResourceIdField;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.exceptions.SecurableResourceAnnotationException;
import java.lang.reflect.Field;
import javax.persistence.Column;

/**
 *
 * @author jejkal
 */
public final class SecurableEntityHelper {

  /**
   * Hidden constructor.
   */
  private SecurableEntityHelper() {
  }

  /**
   * Get the securable resource id for the provided object or a
   * SecurableResourceAnnotationException if an error occurs. This method
   * searches for a field annotated with ISecurableResourceIdField. If a field
   * is found it is checked, if the field is annotated as
   * javax.persistence.Column, is unique and not nullable. If all these
   * requirements are fulfilled, the value of the field is extracted and
   * returned together with the namespace definition of the provided
   * ISecurableResourceIdField.
   *
   * Example:
   *
   * <tt> @ISecurableResourceIdField(domainName =
   * "edu.kit.dama.mdm.base.DigitalObject")
   *
   * @Column(nullable = false, unique = true) private String
   * digitalObjectIdentifier;</tt>
   *
   * @param pEntity The entity of which the securable resource id should be
   * obtained.
   *
   * @return The securable resource id.
   */
  public static SecurableResourceId getSecurableResourceId(Object pEntity) {
    Field[] fields = pEntity.getClass().getDeclaredFields();
    for (Field field : fields) {
      SecurableResourceIdField securableResourceAnnotation = field.getAnnotation(SecurableResourceIdField.class);
      if (securableResourceAnnotation != null) {
        //parse annotations
        String namespace = securableResourceAnnotation.domainName();
        Column columnAnnotation = field.getAnnotation(Column.class);

        if (!columnAnnotation.unique() || columnAnnotation.nullable()) {
          throw new SecurableResourceAnnotationException("The annotation javax.persistence.Column does not satisfy the requirements of a SecurableResource (unique and not nullable)");
        }

        if (String.class.isAssignableFrom(field.getType())) {
          field.setAccessible(true);
          try {
            return new SecurableResourceId(namespace, (String) field.get(pEntity));
          } catch (IllegalAccessException iae) {
            throw new SecurableResourceAnnotationException("Failed to access value of field '" + field.getName() + "' annotated by 'ISecurableResourceIdField'", iae);
          } finally {
            field.setAccessible(false);
          }
        } else {
          throw new SecurableResourceAnnotationException("The type of the field annotated by 'SecurableResourceIdField' must be java.lang.String");
        }
      }
    }
    throw new SecurableResourceAnnotationException("Failed to obtain field annotated by edu.kit.dama.authorization.annotations.ISecurableResourceIdField");
  }

  /**
   * Get the securable resource domain of the provided entity class.
   *
   * @param pEntityClass The entity class.
   *
   * @return The securable resource domain.
   */
  public static String getSecurableResourceDomain(Class pEntityClass) {
    Field[] fields = pEntityClass.getDeclaredFields();
    for (Field field : fields) {
      SecurableResourceIdField securableResourceAnnotation = field.getAnnotation(SecurableResourceIdField.class);
      if (securableResourceAnnotation != null) {
        //parse annotations
        return securableResourceAnnotation.domainName();
      }
    }
    throw new SecurableResourceAnnotationException("Failed to obtain domain from field annotated by edu.kit.dama.authorization.annotations.ISecurableResourceIdField");
  }

  /**
   * Get the domain unique field name.
   *
   * @param pEntityClass The entity class.
   *
   * @return The domain unique field name.
   */
  public static String getDomainUniqueFieldName(Class pEntityClass) {
    String result = null;
    Field[] fields = pEntityClass.getDeclaredFields();

    for (Field field : fields) {
      SecurableResourceIdField securableResourceAnnotation = field.getAnnotation(SecurableResourceIdField.class);
      if (securableResourceAnnotation != null) {
        //parse annotations
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null) {
          //check column annotation
          if (columnAnnotation.unique() && !columnAnnotation.nullable()) {
            //if annotated with "column", the field MUST be unique and not nullable -> here it is valid 
            result = columnAnnotation.name();
            if (result == null || result.length() < 1) {
              //no explicit column name defined...use variable name
              result = field.getName();
            }
            //already valid...use existing value from annotation
          } else {
            //field is annotated by "column" but the annotation's attributes are invalid
            throw new SecurableResourceAnnotationException("The annotation javax.persistence.Column does not satisfy the requirements of a SecurableResource (unique and not nullable)");
          }
        } else {
          //ignore column annotation, simply take field name
          result = field.getName();
        }
      }
    }

    //check result
    if (result == null) {
      //error!
      throw new SecurableResourceAnnotationException("Failed to obtain field annotated by edu.kit.dama.authorization.annotations.ISecurableResourceIdField");
    }
    return result;
  }
}
