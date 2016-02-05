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
import java.util.Set;

/**
 *
 * @author jejkal
 */
public interface IDefaultDigitalObject extends ISimpleDigitalObject {

    /**
     * Get the label of this object.
     *
     * @return The label of the object.
     */
    String getLabel();

    /**
     * Get note for data.
     *
     * @return the note
     */
    String getNote();

    /**
     * Set investigation.
     *
     * @return the investigation
     */
    ISimpleInvestigation getInvestigation();

    /**
     * Get the user who uploads data.
     *
     * @return the user who uploads the data
     */
    ISimpleUserData getUploader();

    /**
     * Get a set of experimenters.
     *
     * @return the experimenters
     */
    Set<? extends ISimpleUserData> getExperimenters();

    /**
     * Get start date of measurement.
     *
     * @return the startDate
     */
    Date getStartDate();

    /**
     * Get end date of measurement.
     *
     * @return the endDate
     */
    Date getEndDate();

    /**
     * Get upload date.
     *
     * @return the uploadDate
     */
    Date getUploadDate();

    /**
     * Get visibility of the DigitalObject.
     *
     * @return visibility of DigitalObject
     */
    Boolean isVisible();

}
