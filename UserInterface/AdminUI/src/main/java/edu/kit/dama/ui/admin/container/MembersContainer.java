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
package edu.kit.dama.ui.admin.container;

import com.vaadin.data.util.BeanItemContainer;
import edu.kit.dama.authorization.entities.impl.Membership;
import edu.kit.dama.ui.admin.exception.EnumParameterNotFoundException;

/**
 *
 * @author dx6468
 */
public class MembersContainer extends BeanItemContainer<Membership> {

    /**
     *
     */
    public static final Object[] COLUMN_ORDER = new Object[]{
        MembersContainer.Property.USER_ID.propertyId,
        MembersContainer.Property.ROLE.propertyId
    };

    /**
     *
     */
    public static final String[] COLUMN_HEADERS = new String[]{
        MembersContainer.Property.USER_ID.columnHeader,
        MembersContainer.Property.ROLE.columnHeader
    };

    public MembersContainer() throws IllegalArgumentException {
        super(Membership.class);
        this.addNestedContainerProperty(
                MembersContainer.Property.USER_ID.propertyId);
    }

    public enum Property {

        USER_ID("user.userId", "USER ID"),
        ROLE("role", "ROLE");

        public final String propertyId;
        public final String columnHeader;

        private Property(String propertyId, String columnHeader) {
            this.propertyId = propertyId;
            this.columnHeader = columnHeader;
        }

        /**
         *
         * @param propertyId
         * @return
         * @throws EnumParameterNotFoundException
         */
        public static Property getEnumByKey(String propertyId) throws EnumParameterNotFoundException {
            for (Property property : Property.values()) {
                if (property.propertyId.equals(propertyId)) {
                    return property;
                }
            }
            throw new EnumParameterNotFoundException("Requested container property not found. "
                    + "Passed key '" + propertyId + "' is not a defined enum parameter.");
        }

        /**
         *
         * @param columnHeader
         * @return
         * @throws EnumParameterNotFoundException
         */
        public static Property getEnumByColumnHeader(String columnHeader) throws EnumParameterNotFoundException {
            for (Property property : Property.values()) {
                if (property.columnHeader.equals(columnHeader)) {
                    return property;
                }
            }
            throw new EnumParameterNotFoundException("Requested container property not found. "
                    + "Passed columnHeader '" + columnHeader + "' is not a defined enum parameter.");
        }
    }
}
