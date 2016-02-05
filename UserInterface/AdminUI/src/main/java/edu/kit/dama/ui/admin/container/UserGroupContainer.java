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
import edu.kit.dama.ui.admin.exception.EnumParameterNotFoundException;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.base.UserData;
import java.io.Serializable;

/**
 *
 * @author dx6468
 */
public class UserGroupContainer extends BeanItemContainer<UserGroup>
        implements Serializable {

    /**
     *
     */
    public static final Object[] COLUMN_ORDER = new Object[]{
        Property.ID.propertyId,
        Property.GROUP_ID.propertyId,
        Property.GROUP_NAME.propertyId,
        Property.DESCRIPTION.propertyId
    };
    /**
     *
     */
    public static final String[] COLUMN_HEADERS = new String[]{
        Property.ID.columnHeader,
        Property.GROUP_ID.columnHeader,
        Property.GROUP_NAME.columnHeader,
        Property.DESCRIPTION.columnHeader
    };

    public UserGroupContainer() throws IllegalArgumentException {
        super(UserGroup.class);
    }    

    /**
     * The first parameter of an enumeration item corresponds to the name of a
     * specific field variable defined in the entity class called
     * edu.kit.dama.mdm.base.UserData.
     * <p>
     * The second parameter of an enumeration item represents the column header
     * of the table implemented in
     * edu.kit.dama.ui.main.GroupAdministrationTab.
     *
     * @see UserData
     */
    public enum Property {

        ID("id", "ID"),
        GROUP_ID("groupId", "GROUP ID"),
        GROUP_NAME("groupName", "GROUP NAME"),
        DESCRIPTION("description", "DESCRIPTION");

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
        public static Property getEnumByColumnHeader(String columnHeader)
                throws EnumParameterNotFoundException {
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