/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology 
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
package edu.kit.dama.mdm.dataorganization.impl.jpa;

import java.io.Serializable;

/**
 *
 * @author pasic
 */
public class DataOrganizationNodeId implements Serializable {

    private static final long serialVersionUID = 7526472295622776127L;
    /**
     * The digital object id in a string representation.
     */
    private String digitalObjectIDStr;
    /**
     * The step number.
     */
    private Long stepNoArrived;

    /**
     * view/representation namel
     */
    private String viewName;

    /**
     * Set the digital object id.
     *
     * @param digitalObjectIDStr The digital object id.
     */
    public void setDigitalObjectIDStr(String digitalObjectIDStr) {
        this.digitalObjectIDStr = digitalObjectIDStr;
    }

    /**
     * Get the digital object id.
     *
     * @return The digital object id.
     */
    public String getDigitalObjectIDStr() {
        return digitalObjectIDStr;
    }

    /**
     * Set the arrived step number.
     *
     * @param stepNoArrived The step number.
     */
    public void setStepNoArrived(Long stepNoArrived) {
        this.stepNoArrived = stepNoArrived;
    }

    /**
     * Get the arrived step number.
     *
     * @return The step number.
     */
    public Long getStepNoArrived() {
        return stepNoArrived;
    }

    /**
     * @return the viewName
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * @param viewName the viewName to set
     */
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof DataOrganizationNodeId)) {
            return false;
        }

        DataOrganizationNodeId other = (DataOrganizationNodeId) obj;

        if (digitalObjectIDStr == null) {
            return other.digitalObjectIDStr == null && stepNoArrived == null &&
                    other.stepNoArrived == null && viewName == null && other.viewName == null;
        }

        return digitalObjectIDStr.equals(other.digitalObjectIDStr) &&
                stepNoArrived.equals(other.stepNoArrived) &&
                viewName.equals(other.viewName);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.digitalObjectIDStr != null ?
                this.digitalObjectIDStr.hashCode() : 0);
        hash = 97 * hash + (this.stepNoArrived != null ? this.stepNoArrived.
                hashCode() : 0);
        return hash;
    }
}
