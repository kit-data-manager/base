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
public interface IDefaultStudy extends ISimpleStudy {

    /**
     * Get topic of study.
     *
     * @return the topic
     */
    String getTopic();

    /**
     * Get note.
     *
     * @return the note.
     */
    String getNote();

    /**
     * Get legal note.
     *
     * @return the legal note.
     */
    String getLegalNote();

    /**
     * Set the manager of this topic.
     *
     * @return the manager
     */
    ISimpleUserData getManager();

    /**
     * Get the involved organization units.
     *
     * @return the organizationUnits
     */
    Set<? extends ISimpleRelation> getOrganizationUnits();

    /**
     * Get investigations of this investigation.
     *
     * @return the investigations
     */
    Set<? extends ISimpleInvestigation> getInvestigations();

    /**
     * Get start date of study.
     *
     * @return the startDate
     */
    Date getStartDate();

    /**
     * Get end date of study.
     *
     * @return the endDate
     */
    Date getEndDate();

    /**
     * Get visibility of the study.
     *
     * @return visibility of study
     */
    Boolean isVisible();

}
