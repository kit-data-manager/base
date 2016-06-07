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
package edu.kit.dama.mdm.dataorganization.impl.staging;

import edu.kit.dama.commons.types.ILFN;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;

/**
 *
 * @author jejkal
 */
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("nodeId")
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("nodeId"),
                @XmlNamedAttributeNode("name"),
                @XmlNamedAttributeNode("logicalFileName"),
                @XmlNamedAttributeNode("description"),
                @XmlNamedAttributeNode(value = "attributes", subgraph = "default")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
public final class FileNodeImpl extends DataOrganizationNodeImpl implements IFileNode {

    @XmlElement(name = "logicalFileName")
    private ILFN logicalFileName;

    /**
     * Default constructor.
     */
    protected FileNodeImpl() {
    }

    /**
     * Default constructor.
     *
     * @param logicalFileName The logical filename.
     */
    public FileNodeImpl(ILFN logicalFileName) {
        setLogicalFileName(logicalFileName);
    }

    @Override
    public ILFN getLogicalFileName() {
        return logicalFileName;
    }

    @Override
    public void setLogicalFileName(ILFN logicalFileName) {
        this.logicalFileName = logicalFileName;
    }

    @Override
    public final String toString() {
        return "(F) " + getName() + " (" + hashCode() + ") - " + getLogicalFileName();
    }

}
