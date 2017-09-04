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
package edu.kit.dama.rest.dataorganization.types;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.DataOrganizationNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class ElementPath implements Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElementPath.class);

    private String[] path;

    public ElementPath(String unparsedPath) {

        if (unparsedPath == null) {
            //nothing to do
            path = new String[]{};
        } else {
            path = unparsedPath.split("/");
        }
    }

    public String[] getPath() {
        return path;
    }

    public void setPath(String[] path) {
        this.path = path;
    }

    public IDataOrganizationNode getNodeForPath(DigitalObjectId pObjectId, String pView) throws EntityNotFoundException, InvalidNodeIdException {
        long id = -1l;
        LOGGER.debug("Obtaining data organization node for path {} in object {} and view {}", Arrays.asList(path), pObjectId, pView);
        if (path.length == 1) {
            //check for node id
            try {
                //check for node id
                LOGGER.debug("Checking for node id in first path element.");
                id = Long.parseLong(path[0]);
                LOGGER.debug("First path element is nodeId {}.", id);
                //is node id...
            } catch (NumberFormatException ex) {
                //is normal path...obtain id by path
                LOGGER.debug("First path element is no nodeId.");
            }
        }//

        LOGGER.debug("Getting DataOrganizer");
        DataOrganizer dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();

        IDataOrganizationNode result;
        if (id < 0) {
            //no id yet...obtain id by path
            LOGGER.debug("Loading file tree for object {} and view {}", pObjectId, pView);
            IFileTree tree = dataOrganizer.loadFileTree(pObjectId, pView);
            //traverse root by path
            IDataOrganizationNode rootNode = tree.getRootNode();
            List<String> paths = new ArrayList<>();
            Collections.addAll(paths, path);
            //remove empty strings
            paths.remove("");
            if (paths.isEmpty()) {
                //no children...just return root
                LOGGER.debug("No paths found, returning root node {}.", rootNode);
                return rootNode;
            }
            LOGGER.debug("Getting child of rootNode {} by path {}", rootNode, paths);
            result = (DataOrganizationNodeImpl) DataOrganizationUtils.copyNode(getChildByPath(rootNode, paths), false);
        } else {
            //use direct node id
            LOGGER.debug("Loading subtree for object {}, view {} and nodeId {}", pObjectId, pView, id);
            NodeId nodeId = new NodeId(pObjectId, id, 1, pView);
            LOGGER.debug("NodeId {} obtained. Loading node.", nodeId);
            IDataOrganizationNode thisNode = dataOrganizer.loadNode(nodeId);
            LOGGER.debug("Node {} obtained. Loading sub tree.", thisNode);
            IFileTree subtree = dataOrganizer.loadSubTree(nodeId, 100);
            if (subtree == null) {
                LOGGER.info("No subtree obtained for node. Returning node itself.");
                result = (DataOrganizationNodeImpl) DataOrganizationUtils.copyNode(thisNode, false);
            } else {
                subtree.setDigitalObjectId(pObjectId);
                result = (DataOrganizationNodeImpl) DataOrganizationUtils.copyNode(subtree.getRootNode(), false);
                result.setName(thisNode.getName());
            }
        }
        //((DataOrganizationNodeImpl) result).setDigitalObjectIDStr(pObjectId.getStringRepresentation());
        result.setViewName(pView);
        return result;
    }

    private IDataOrganizationNode getChildByPath(IDataOrganizationNode pCurrentNode, List<String> pPath) {
        if (pCurrentNode == null) {
            throw new IllegalArgumentException("Failed to traverse data organization node. Current node is 'null', remaining path is '" + pPath + "'");
        }
        if (pPath.isEmpty()) {
            return pCurrentNode;
        }
        String currentPath = pPath.remove(0);
        LOGGER.debug("Obtaining node for currentPath {}", currentPath);
        if (pCurrentNode instanceof ICollectionNode) {
            return getChildByPath(Util.getNodeByName((ICollectionNode) pCurrentNode, currentPath), pPath);
        }
        //current node is a file node, no more children are available but obviously the path has not ended yet.
        throw new IllegalArgumentException("Failed to traverse data organization node. No children for node '" + pCurrentNode + "' but sub-path '" + currentPath + "' expected.");
    }

}
