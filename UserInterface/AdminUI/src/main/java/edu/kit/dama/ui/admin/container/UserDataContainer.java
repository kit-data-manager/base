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
import edu.kit.dama.mdm.base.UserData;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dx6468
 */
public class UserDataContainer extends BeanItemContainer<UserData> implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataContainer.class);

    public UserDataContainer() throws IllegalArgumentException {
        super(UserData.class);
    }

    /**
     *
     */
    public static final Object[] COLUMN_ORDER = new Object[]{
        Property.USER_ID.propertyId,
        Property.DISTINGUISHED_NAME.propertyId,
        Property.FIRST_NAME.propertyId,
        Property.LAST_NAME.propertyId,
        Property.EMAIL.propertyId,
        Property.VALID_FROM.propertyId,
        Property.VALID_UNTIL.propertyId
    };

    /**
     *
     */
    public static final String[] COLUMN_HEADERS = new String[]{
        Property.USER_ID.columnHeader,
        Property.DISTINGUISHED_NAME.columnHeader,
        Property.FIRST_NAME.columnHeader,
        Property.LAST_NAME.columnHeader,
        Property.EMAIL.columnHeader,
        Property.VALID_FROM.columnHeader,
        Property.VALID_UNTIL.columnHeader
    };

    /**
     * The first parameter of an enumeration item corresponds to the name of a
     * specific field variable defined in the entity class called
     * edu.kit.dama.mdm.base.UserData.
     * <p>
     * The second parameter of an enumeration item represents the column header
     * of the table implemented in
     * edu.kit.dama.ui.main.UserAdministrationTab.
     *
     * @see UserData
     */
    public enum Property {

        USER_ID("userId", "ID"),
        DISTINGUISHED_NAME("distinguishedName", "DISTINGUISHED NAME"),
        FIRST_NAME("firstName", "FIRST NAME"),
        LAST_NAME("lastName", "LAST NAME"),
        EMAIL("email", "E-MAIL"),
        VALID_FROM("validFrom", "VALID FROM"),
        VALID_UNTIL("validUntil", "VALID UNTIL");

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
