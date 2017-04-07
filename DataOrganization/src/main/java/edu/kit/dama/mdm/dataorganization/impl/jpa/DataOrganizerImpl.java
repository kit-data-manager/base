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

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNodeVisitor;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.BulkPersist;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.util.Constants;
import java.util.List;
import java.util.Stack;
import javax.persistence.EntityManager;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pasic
 */
final public class DataOrganizerImpl implements edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer, IConfigurableAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataOrganizerImpl.class);

    public static final Integer VER_START = 1;

    /**
     * Build a tree for a digital object id from a list of DataOrganizationNode
     * obtained from a data source.
     *
     * @param nodes The list of nodes.
     * @param digitalObjectID The digital object id the tree is associated with.
     *
     * @return The tree.
     */
    private FileTree buildTree(List<DataOrganizationNode> nodes,
            DigitalObjectId digitalObjectID) {

        FileTree tree = null;
        if (!nodes.isEmpty()) {
            Stack<DataOrganizationNode> stack = new Stack<>();
            DataOrganizationNode node = nodes.get(0);
            if (!(node instanceof CollectionNode)) {
                return null;
            }
            tree = new FileTree((CollectionNode) node);
            tree.setDigitalObjectId(digitalObjectID);
            node = tree;
            stack.push(node);
            DataOrganizationNode nextNode;
            for (int i = 1; i < nodes.size(); ++i) {
                nextNode = nodes.get(i);
                while (stack.peek().getStepNoLeaved() < nextNode.getStepNoLeaved()) {
                    stack.pop();
                }
                node = stack.peek();
                ((CollectionNode) node).addChild(nextNode);
                stack.push(nextNode);
            }
        }
        return tree;
    }

    @Override
    public FileTree loadFileTree(DigitalObjectId digitalObjectId) {
        return loadFileTree(digitalObjectId, Constants.DEFAULT_VIEW);
    }

    @Override
    public FileTree loadFileTree(DigitalObjectId digitalObjectID, String viewName) {
        return buildTree(PersistenceFacade.getInstance().getAllNodesForTree(digitalObjectID, viewName), digitalObjectID);
    }

    @Override
    public void createFileTree(IFileTree fileTree) {
        if (null == fileTree) {
            throw new IllegalArgumentException(
                    "The reference to a FileTree to save must not be null!");
        }

        // Delete all existing nodes with same digitalObjectId and viewName
        PersistenceFacade.getInstance().deleteAllNodesForTree(fileTree.
                getDigitalObjectId(), fileTree.getViewName());

        SaveVisitor saveVisitor = new SaveVisitor();
        BulkPersist bulkPersist = new BulkPersist();
        saveVisitor.setBulkPersist(bulkPersist);
        saveVisitor.setDigitalObjectIDStr(fileTree.getDigitalObjectId().getStringRepresentation());
        saveVisitor.setViewName(fileTree.getViewName());
        bulkPersist.startBulkIngest(4000);
        fileTree.walkTree(saveVisitor.getPreorderVisitor(), saveVisitor.getPostorderVisitor());
        bulkPersist.finish();
    }

    @Override
    public List<? extends IDataOrganizationNode> getChildren(NodeId relativeRoot,
            int firstResult, int maxResult) {
        return PersistenceFacade.getInstance().getChildNodes(relativeRoot,
                firstResult, maxResult);
    }

    @Override
    public Long getChildCount(NodeId nodeId) {
        return PersistenceFacade.getInstance().getChildNodeCount(nodeId);
    }

    @Override
    public IFileTree loadSubTree(NodeId relativeRoot, int relativeDepth) {
        return buildTree(
                PersistenceFacade.getInstance().getAllNodes(relativeRoot,
                        relativeDepth), relativeRoot.getDigitalObjectId());
    }

    @Override
    public NodeId getRootNodeId(DigitalObjectId digitalObjectId) {
        return getRootNodeId(digitalObjectId, Constants.DEFAULT_VIEW);
    }

    @Override
    public NodeId getRootNodeId(DigitalObjectId digitalObjectId, String viewName) {
        PersistenceFacade pf = PersistenceFacade.getInstance();
        EntityManager em = pf.getEntityManagerFactory().createEntityManager();
        DataOrganizationNode root = pf.getRootNode(digitalObjectId, em, viewName);
        em.close();
        return root.getTransientNodeId();
    }

    @Override
    public void updateNodeData(NodeId id, IDataOrganizationNode newData) {
        PersistenceFacade.getInstance().updateDataOrganizationNodeData(id, newData);
    }

    @Override
    public IDataOrganizationNode loadNode(NodeId nodeId) {
        PersistenceFacade pf = PersistenceFacade.getInstance();
        EntityManager em = pf.getEntityManagerFactory().createEntityManager();
        DataOrganizationNode node = PersistenceFacade.getInstance().findNodeByNodeId(nodeId, em);
        em.close();
        return node;
    }

    @Override
    public List<String> getViews(DigitalObjectId doid) {
        PersistenceFacade pf = PersistenceFacade.getInstance();
        return pf.getViewsForDoid(doid);
    }

    @Override
    public boolean configure(Configuration pConfig) throws ConfigurationException {
        return true;
    }

    static class SaveVisitor {

        private long count = 0L;
        private long increment = 100L;
        private int depth = 0;
        private String digitalObjectIDStr;
        private Stack<DataOrganizationNode> implNodes = new Stack<DataOrganizationNode>();

        private String viewName;

        private BulkPersist bulkPersist;
        /**
         * Create preorder visitor.
         */
        private final IDataOrganizationNodeVisitor preorderVisitor
                = new IDataOrganizationNodeVisitor() {
            @Override
            public void action(IDataOrganizationNode node) {
                DataOrganizationNode implNode = JPAImplUtil.
                        convertDataOrganizationNode(node);

                implNodes.push(implNode);
                count += increment;
                ++depth;
                implNode.setStepNoArrived(count);
                implNode.setViewName(node.getViewName());
            }
        };
        /**
         * Create postorder visitor.
         */
        private final IDataOrganizationNodeVisitor postorderVisitor = new IDataOrganizationNodeVisitor() {
            @Override
            public void action(IDataOrganizationNode node) {
                DataOrganizationNode implNode = implNodes.pop();
                count += increment;
                --depth;
                implNode.setStepNoLeaved(count);
                implNode.setIdVersion(VER_START);
                implNode.setDigitalObjectIDStr(digitalObjectIDStr);
                implNode.setNodeDepth(depth);
                implNode.setViewName(viewName);
                bulkPersist.persist(implNode);
            }
        };

        /**
         * Default constructor.
         */
        public SaveVisitor() {
        }

        /**
         * Get the postorder visitor.
         *
         * @return The postorder visitor.
         */
        public IDataOrganizationNodeVisitor getPostorderVisitor() {
            return postorderVisitor;
        }

        /**
         * Get the preorder visitor.
         *
         * @return The preorder visitor.
         */
        public IDataOrganizationNodeVisitor getPreorderVisitor() {
            return preorderVisitor;
        }

        /**
         * Set bulk persist implementation.
         *
         * @param bulkPersist Bulk persist implementation.
         */
        public void setBulkPersist(BulkPersist bulkPersist) {
            this.bulkPersist = bulkPersist;
        }

        /**
         * Set digital object id string.
         *
         * @param digitalObjectID Digital object id string.
         */
        public void setDigitalObjectIDStr(String digitalObjectID) {
            this.digitalObjectIDStr = digitalObjectID;
        }

        /**
         * Set increment.
         *
         * @param increment The increment.
         */
        public void setIncrement(long increment) {
            this.increment = increment;
        }

        public void setViewName(String viewName) {
            this.viewName = viewName;
        }

    }
}
