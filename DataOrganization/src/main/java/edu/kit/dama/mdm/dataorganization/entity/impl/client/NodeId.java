/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
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
package edu.kit.dama.mdm.dataorganization.entity.impl.client;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.util.Constants;

/**
 *
 * @author pasic
 */
public class NodeId {

    /**
     * The digital object id.
     */
    private DigitalObjectId digitalObjectId;
    /**
     * The id of the node withing the tree.
     */
    private long inTreeId;
    /**
     * The id version, by default 1.
     */
    private int inTreeIdVersion = 1;

    /**
     * view/representation name
     */
    private String viewName;

    /**
     * Default constructor.
     */
    public NodeId() {
    }

    /**
     * Default constructor.
     *
     * @param digitalObjectId The id of the digital object associated with this
     * node.
     * @param inTreeId The node id within the tree.
     * @param inTreeIdVersion The id version of this node. (default : 1)
     * @param viewName The name of the view this node is associated with.
     */
    public NodeId(DigitalObjectId digitalObjectId, long inTreeId,
            int inTreeIdVersion, String viewName) {
        this.digitalObjectId = digitalObjectId;
        this.inTreeId = inTreeId;
        this.inTreeIdVersion = inTreeIdVersion;
        this.viewName = viewName;
    }

    /**
     * Default constructor.
     *
     * @param digitalObjectId The id of the digital object associated with this
     * node.
     * @param inTreeId The node id within the tree.
     * @param inTreeIdVersion The id version of this node. (default : 1)
     */
    public NodeId(DigitalObjectId digitalObjectId, long inTreeId,
            int inTreeIdVersion) {
        this.digitalObjectId = digitalObjectId;
        this.inTreeId = inTreeId;
        this.inTreeIdVersion = inTreeIdVersion;
        this.viewName = Constants.DEFAULT_VIEW;
    }

    /**
     * Returns the digital object id.
     *
     * @return The digital object id.
     */
    public final DigitalObjectId getDigitalObjectId() {
        return digitalObjectId;
    }

    /**
     * Set the digital object id.
     *
     * @param digitalObjectId The digital object id.
     */
    public final void setDigitalObjectId(DigitalObjectId digitalObjectId) {
        this.digitalObjectId = digitalObjectId;
    }

    /**
     * Returns the node id.
     *
     * @return The node id.
     */
    public final long getInTreeId() {
        return inTreeId;
    }

    /**
     * Set the node id.
     *
     * @param inTreeId The node id.
     */
    public final void setInTreeId(long inTreeId) {
        this.inTreeId = inTreeId;
    }

    /**
     * Get the id version.
     *
     * @return The id version.
     */
    public final int getInTreeIdVersion() {
        return inTreeIdVersion;
    }

    /**
     * Set the id version.
     *
     * @param inTreeIdVersion The id version.
     */
    public final void setInTreeIdVersion(int inTreeIdVersion) {
        this.inTreeIdVersion = inTreeIdVersion;
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
    public String toString() {
        return ((getDigitalObjectId() != null) ? getDigitalObjectId().getStringRepresentation() : "NULL") + ":" + getViewName() + ":" + getInTreeId();
    }

}
