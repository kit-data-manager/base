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
package edu.kit.dama.mdm.base.interfaces;

import java.util.Date;

/**
 *
 * @author jejkal
 */
public interface IDefaultUserData extends ISimpleUserData {

    /**
     * Get first name.
     *
     * @return the firstName
     */
    String getFirstName();

    /**
     * Get last name.
     *
     * @return the lastName
     */
    String getLastName();

    /**
     * Get email.
     *
     * @return the email
     */
    String getEmail();

    /**
     * Get distinguished name.
     *
     * @return the distributedName
     */
    String getDistinguishedName();

    /**
     * Get property 'validUntil'.
     *
     * @return the validUntil
     */
    Date getValidUntil();

    /**
     * Get property 'validFrom'.
     *
     * @return the validFrom
     */
    Date getValidFrom();
}
