/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.rest.dataorganization.test;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileTreeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.util.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author jejkal
 */
public class DummyDataOrganizer implements edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer {

    @Override
    public IFileTree loadFileTree(DigitalObjectId digitalObjectId, String viewName) throws EntityNotFoundException {
        return new FileTreeImpl();
    }

    @Override
    public IFileTree loadFileTree(DigitalObjectId digitalObjectId) throws EntityNotFoundException {
        return new FileTreeImpl();
    }

    @Override
    public void createFileTree(IFileTree fileTree) throws EntityExistsException {
    }

    @Override
    public List<? extends IDataOrganizationNode> getChildren(NodeId nodeId, int firstResult, int maxResult) throws InvalidNodeIdException {
        return new ArrayList<IDataOrganizationNode>();
    }

    @Override
    public Long getChildCount(NodeId nodeId) throws InvalidNodeIdException {
        return 0l;
    }

    @Override
    public IFileTree loadSubTree(NodeId relativeRoot, int relativeDepth) throws InvalidNodeIdException {
        return new FileTreeImpl();
    }

    @Override
    public NodeId getRootNodeId(DigitalObjectId digitalObjectId) throws EntityNotFoundException {
        return new NodeId(digitalObjectId, 1, 1);
    }

    @Override
    public NodeId getRootNodeId(DigitalObjectId digitalObjectId, String viewName) throws EntityNotFoundException {
        return new NodeId(digitalObjectId, 1, 1);
    }

    @Override
    public void updateNodeData(NodeId nodeId, IDataOrganizationNode newData) throws InvalidNodeIdException {
    }

    @Override
    public IDataOrganizationNode loadNode(NodeId nodeId) throws InvalidNodeIdException {
        FileNodeImpl node = new FileNodeImpl(new LFNImpl("file:///tmp/dummy.txt"));
        node.setNodeId(nodeId.getInTreeId());
        return node;
    }

    @Override
    public List<String> getViews(DigitalObjectId doid) {
        return Arrays.asList(Constants.DEFAULT_VIEW);
    }

    @Override
    public boolean configure(Configuration pConfig) throws ConfigurationException {
        return true;
    }

}
