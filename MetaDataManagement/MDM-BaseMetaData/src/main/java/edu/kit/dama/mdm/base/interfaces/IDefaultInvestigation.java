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
public interface IDefaultInvestigation extends ISimpleInvestigation {

    /**
     * Get topic.
     *
     * @return the topic
     */
    String getTopic();

    /**
     * Get note.
     *
     * @return the note
     */
    String getNote();

    /**
     * Get description of the investigation.
     *
     * @return the description
     */
    String getDescription();

    /**
     * Get the study initiating this investigation.
     *
     * @return the study
     */
    ISimpleStudy getStudy();

    /**
     * Get type of meta data linked with this investigation.
     *
     * @return the set of all supported metaDataSchemas
     */
    Set<? extends ISimpleMetaDataSchema> getMetaDataSchema();

    /**
     * Get participants of this investigation.
     *
     * @return the participants
     */
    Set<? extends ISimpleParticipant> getParticipants();

    /**
     * Get set of data linked to this investigation.
     *
     * @return the dataSets
     */
    Set<? extends ISimpleDigitalObject> getDataSets();

    /**
     * Get start date of investigation.
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
     * Get visibility of investigation.
     *
     * @return visibility of investigation
     */
    Boolean isVisible();
}
