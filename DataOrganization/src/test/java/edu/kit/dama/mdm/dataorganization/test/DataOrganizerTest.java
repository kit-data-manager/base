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
package edu.kit.dama.mdm.dataorganization.test;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.FileNode;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author pasic
 */
public class DataOrganizerTest {

    DigitalObjectId digitalObjectID = new DigitalObjectId("DigitalObjectIdDummy");
    static DataOrganizer dataOrganizer = null;

    @Before
    public void setUp() {
        TestUtil.clearDB();
    }

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("datamanager.config", "datamanager.xml");
        PersistenceFacade.getInstance().setPersistenceUnit("DataOrganizationPU-Test");
        dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();
    }

    /**
     * Test save and load
     */
    @Test
    public void testSaveAndLoadFileTree() throws EntityNotFoundException, EntityExistsException {
        IFileTree testTree = TestUtil.createBasicTestTree();
        testTree.setDigitalObjectId(digitalObjectID);
        System.out.println("CREATE");
        dataOrganizer.createFileTree(testTree);
        System.out.println("LOAD ");
        IFileTree lf = DataOrganizerFactory.getInstance().getDataOrganizer().
                loadFileTree(testTree.getDigitalObjectId());
        System.out.println("DONE");
        assert (testTree.equals(lf));
    }

    @Test
    public void testLoadNotExistent() throws EntityNotFoundException {
        assert (null == dataOrganizer.loadFileTree(digitalObjectID));
    }

    @Test
    public void testDetached() throws EntityExistsException, EntityNotFoundException {
        IFileTree testTree = TestUtil.createBasicTestTree();
        testTree.setDigitalObjectId(digitalObjectID);

        dataOrganizer.createFileTree(testTree);

        IFileTree lf1 = dataOrganizer.loadFileTree(testTree.getDigitalObjectId());
        assert (testTree.equals(lf1));
        ICollectionNode newNode = new CollectionNode();
        newNode.setName("newNode");
        testTree.getRootNode().addChild(newNode);

        IFileTree lf2 = dataOrganizer.
                loadFileTree(testTree.getDigitalObjectId());

        assert (!testTree.equals(lf2));
    }

    @Test
    public void testLoadAndSaveWithFileNode() throws EntityExistsException, EntityNotFoundException {
        final String fileNodeName = "XyZNa31";
        IFileTree testTree = TestUtil.createBasicTestTree();
        testTree.setDigitalObjectId(digitalObjectID);
        ICollectionNode coll = (ICollectionNode) testTree.getNodeByName(
                "cnc 2.2.3");

        StringLFN lfn = new StringLFN("XyZ");

        IFileNode fileNode = new FileNode(lfn);
        fileNode.setName(fileNodeName);
        coll.addChild(fileNode);

        dataOrganizer.createFileTree(testTree);

        IFileTree l1 = dataOrganizer.loadFileTree(testTree.getDigitalObjectId());

        IFileNode lfileNode = (IFileNode) l1.getNodeByName(fileNodeName);

        assertTrue(fileNode.getName().equals(lfileNode.getName()));

    }

    @Test
    public void testHeterogeneLFN() throws EntityExistsException, EntityNotFoundException {
        final long lfnLVal = 1000;
        final String nameL = "Lnode";
        final String lfnSVal = "XxLfN";
        final String nameS = "Snode";

        IFileTree testTree = TestUtil.createBasicTestTree();
        testTree.setDigitalObjectId(digitalObjectID);
        testTree.setViewName("default");

        StringLFN lfnStr = new StringLFN(lfnSVal);
        IFileNode fileNode = new FileNode(lfnStr);
        fileNode.setName(nameS);
        ICollectionNode coll = (ICollectionNode) testTree.getNodeByName(
                "cnc 2.2.3");
        coll.addChild(fileNode);

        LongLFN lfnLong = new LongLFN(lfnLVal);
        fileNode = new FileNode(lfnLong);
        fileNode.setName(nameL);
        coll = (ICollectionNode) testTree.getNodeByName("cnc 2.3.1");
        coll.addChild(fileNode);

        dataOrganizer.createFileTree(testTree);

        IFileTree l1 = dataOrganizer.loadFileTree(digitalObjectID);
        IFileNode fSl1 = (IFileNode) l1.getNodeByName(nameS);
        IFileNode fLl1 = (IFileNode) l1.getNodeByName(nameL);

        assertTrue(lfnSVal.equals(fSl1.getLogicalFileName().asString()));
        assertTrue(lfnLong.asString().equals(fLl1.getLogicalFileName().
                asString()));

    }

    @Test
    public void testGetChildren() throws EntityExistsException, EntityNotFoundException, InvalidNodeIdException {
        IFileTree testTree = TestUtil.createBasicTestTree();
        testTree.setDigitalObjectId(digitalObjectID);
        dataOrganizer.createFileTree(testTree);
        NodeId rootNodeId = dataOrganizer.getRootNodeId(digitalObjectID);
        List<? extends IDataOrganizationNode> children = dataOrganizer.
                getChildren(rootNodeId, 0, 3);
        for (IDataOrganizationNode node : children) {
            List<? extends IDataOrganizationNode> children1 = dataOrganizer.
                    getChildren(node.getTransientNodeId(), 0, 3);
            for (IDataOrganizationNode node1 : children1) {
                System.out.println(node1.getName());
            }
        }
    }

    @Test
    public void testGetSubtree() throws EntityExistsException, EntityNotFoundException, InvalidNodeIdException {
        IFileTree testTree = TestUtil.createBasicTestTree();
        testTree.setDigitalObjectId(digitalObjectID);
        dataOrganizer.createFileTree(testTree);
        NodeId rootNodeId = dataOrganizer.getRootNodeId(digitalObjectID);
        IFileTree subTree = dataOrganizer.loadSubTree(rootNodeId, 2);
        printTree(subTree.getRootNode(), 0);
    }

    @Test
    public void testUpdateNode() throws EntityExistsException, EntityNotFoundException, InvalidNodeIdException {
        IFileTree testTree = TestUtil.createBasicTestTree();
        testTree.setDigitalObjectId(digitalObjectID);
        dataOrganizer.createFileTree(testTree);
        NodeId rootNodeId = dataOrganizer.getRootNodeId(digitalObjectID);
        IFileTree subTree = dataOrganizer.loadSubTree(rootNodeId, 2);
        String newDescription = "new description 312334167";
        String newName = "new name 312334167";
        IDataOrganizationNode nodeToUpdate
                = subTree.getRootNode().getChildren().get(0);
        nodeToUpdate.setDescription(newDescription);
        nodeToUpdate.setName(newName);
        NodeId transientNodeId = nodeToUpdate.getTransientNodeId();
        StringLFN newLfn = new StringLFN("test://no-file-there");
        if (nodeToUpdate instanceof IFileNode) {
            IFileNode fNode2Update = (IFileNode) nodeToUpdate;
            fNode2Update.setLogicalFileName(newLfn);
        }
        dataOrganizer.updateNodeData(transientNodeId, nodeToUpdate);
        IDataOrganizationNode updatedNode = dataOrganizer.loadNode(
                transientNodeId);
        assertEquals(updatedNode.getDescription(), newDescription);
        assertEquals(updatedNode.getName(), newName);
        if (updatedNode instanceof IFileNode) {
            assertEquals(newLfn, ((IFileNode) updatedNode).getLogicalFileName());
        }

    }

    public void printTree(IDataOrganizationNode node, int level) {
        for (int i = 0; i < level; ++i) {
            System.out.print("   ");
        }
        System.out.println(node.getName());
        if (node instanceof ICollectionNode) {
            List<? extends IDataOrganizationNode> children
                    = ((ICollectionNode) node).getChildren();
            for (IDataOrganizationNode n : children) {
                printTree(n, level + 1);
            }
        }
    }
}
