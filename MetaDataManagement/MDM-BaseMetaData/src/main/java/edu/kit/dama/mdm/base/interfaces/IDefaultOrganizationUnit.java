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

/**
 *
 * @author jejkal
 */
public interface IDefaultOrganizationUnit extends ISimpleOrganizationUnit {

    /**
     * Get name of ou.
     *
     * @return the ouName
     */
    String getOuName();

    /**
     * Get address of ou.
     *
     * @return the address
     */
    String getAddress();

    /**
     * Get zip-code of city.
     *
     * @return the zipCode
     */
    String getZipCode();

    /**
     * Get city of ou.
     *
     * @return the city
     */
    String getCity();

    /**
     * Get country of ou.
     *
     * @return the country
     */
    String getCountry();

    /**
     * Set web site of the ou.
     *
     * @return the website
     */
    String getWebsite();

    /**
     * Get the manager of organization unit.
     *
     * @return the manager
     */
    ISimpleUserData getManager();
}
