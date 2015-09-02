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
package edu.kit.dama.authorization.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for entity fields holding the domain unique securable
 * resource id. This annotation allows to define a domainName which will be used
 * to build the SecurableResourceId together with the domain unique id read from
 * the field.
 *
 * This field is needed to link the entity representing a SecurableResource to
 * the KIT Data Manager Authorization building up a view (FilterHelper) holding all
 * permissions. As this view can only be linked to the actual entity table by
 * the domainUniqueId, this field has to represented by a column within the
 * entity table. For this purpose, the field must be also annotated by
 * <i>javax.persistence.Column</i> to check, wheather all requirements towards
 * this field are fulfilled.
 *
 * This field and the column annotation must satisfy the following conditions:
 *
 * <ul>
 *
 * <li>The type of the field has to be String/the type of the column will be
 * varchar</li>
 *
 * <li>The value must be unique within the column and not nullable &gt;
 * &#64;Column(unique=true, nullable=false)</li>
 *
 * <li>The column may be named different than the field, e.g. when the field
 * name is a reserved term of the underlying database system. For this purpose,
 * the 'name' argument of <i>javax.persistence.Column</i> is used &gt;
 * &#64;Column(name="myName", unique=true, nullable=false)</li>
 *
 * </ul>
 *
 * If one of these requirements is not fulfilled, the entity may not be used
 * together with the KIT Data Manager Authorization.
 *
 * @author jejkal
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface SecurableResourceIdField {

  /**
   * Get the domain name.
   *
   * @return The domain name.
   */
  String domainName();
}
