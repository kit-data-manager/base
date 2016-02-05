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
package edu.kit.dama.rest.dataorganization.test;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.mdm.dataorganization.ext.TestUtil;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.core.ISimpleDataOrganizationNode;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.rest.dataorganization.services.interfaces.IDataOrganizationRestService;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.DataOrganizationNodeImpl;
import edu.kit.dama.rest.base.IEntityWrapper;
import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author dapp
 */
@Path("/DataOrganizationTest")
public class DataOrganizationTestService implements IDataOrganizationRestService {

    IFileTree tree;
    DataOrganizer dor;
    List<String> views;

    void createRootNode() {

    }

    public DataOrganizationTestService() {
        tree = TestUtil.createBasicTestTree();
        views = new LinkedList<>();
        views.add("Default");
        views.add("View 1");
        views.add("View 2");
    }

    @Override
    public IEntityWrapper<? extends ISimpleDataOrganizationNode> getRootNode(String groupId, Long id, Integer first, Integer results, String viewName, HttpContext hc) {
        CollectionNodeImpl rnd = new CollectionNodeImpl();

        rnd.setName(tree.getRootNode().getName());
        rnd.setNodeId(100l);

        IDataOrganizationNode nd1 = tree.getNodeByName("child 1");
        IDataOrganizationNode nd2 = tree.getNodeByName("child 2");

        rnd.addChild(nd1);
        rnd.addChild(nd2);

        return new DataOrganizationNodeWrapper(rnd);
    }

    @Override
    public IEntityWrapper<? extends ISimpleDataOrganizationNode> getRootNodeCount(String groupId, Long id, String viewName, HttpContext hc) {
        return new DataOrganizationNodeWrapper(1);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getRootNodeChildren(String groupId, Long id, Integer first, Integer results, String viewName, HttpContext hc) {
        ICollectionNode rootNode = tree.getRootNode();
        List<? extends IDataOrganizationNode> children = rootNode.getChildren();
        List<DataOrganizationNodeImpl> childrenImpls = new LinkedList<>();

        for (IDataOrganizationNode nd : children) {
            if (nd instanceof DataOrganizationNodeImpl) {
                childrenImpls.add((DataOrganizationNodeImpl) nd);
            }
        }

        return new DataOrganizationNodeWrapper(childrenImpls);
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getNodeInformation(String groupId, Long id, Long nodeId, String viewName, HttpContext hc) {
        IDataOrganizationNode nd = tree.getNodeByName("child " + nodeId);

        if (nd instanceof DataOrganizationNodeImpl) {
            DataOrganizationNodeImpl dnd = new DataOrganizationNodeImpl();
            dnd.setName(nd.getName());
            dnd.setViewName(nd.getViewName());
            return new DataOrganizationNodeWrapper(dnd);
        } else {
            CollectionNodeImpl cnd = new CollectionNodeImpl();
            cnd.setName(nd.getName());
            cnd.setViewName(nd.getViewName());
            return new DataOrganizationNodeWrapper(cnd);
        }
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getChildren(String groupId, Long id, Long nodeId, Integer first, Integer results, String viewName, HttpContext hc) {
        List<DataOrganizationNodeImpl> l = new LinkedList<>();
        if (nodeId == 100l) {
            DataOrganizationNodeImpl nd = new DataOrganizationNodeImpl();
            IDataOrganizationNode nd2 = tree.getNodeByName("child 1");
            nd.setName(nd2.getName());
            nd.setViewName(nd2.getViewName());
            l.add(nd);
            nd2 = tree.getNodeByName("child 2");
            nd = new DataOrganizationNodeImpl();
            nd.setName(nd2.getName());
            nd.setViewName(nd2.getViewName());
            l.add(nd);
        } else if (nodeId == 200l) {
            DataOrganizationNodeImpl nd = new DataOrganizationNodeImpl();
            IDataOrganizationNode nd2 = tree.getNodeByName("child 2.1");
            nd.setName(nd2.getName());
            nd.setViewName(nd2.getViewName());
            l.add(nd);
            nd = new DataOrganizationNodeImpl();
            nd2 = tree.getNodeByName("child 2.2");
            nd.setName(nd2.getName());
            nd.setViewName(nd2.getViewName());
            l.add(nd);
            nd = new DataOrganizationNodeImpl();
            nd2 = tree.getNodeByName("child 2.3");
            nd.setName(nd2.getName());
            nd.setViewName(nd2.getViewName());
            l.add(nd);
        }
        return new DataOrganizationNodeWrapper(l);
    }

    @Override
    public IEntityWrapper<? extends ISimpleDataOrganizationNode> getChildCount(String groupId, Long id, Long nodeId, String viewName, HttpContext hc) {
        IDataOrganizationNode nd;

        if (nodeId == 100l) {
            nd = tree.getNodeByName("root");
        } else {
            nd = tree.getNodeByName("child " + id.toString());
        }

        if (nd instanceof ICollectionNode) {
            int sz = ((ICollectionNode) nd).getChildren().size();

            return new DataOrganizationNodeWrapper(sz);
        }

        return new DataOrganizationNodeWrapper(0);
    }

    @Override
    public DataOrganizationViewWrapper getViews(String groupId, Long id, HttpContext hc) {
        return new DataOrganizationViewWrapper(views);
    }

    @Override
    public DataOrganizationViewWrapper getViewCount(String groupId, Long id, HttpContext hc) {
        return new DataOrganizationViewWrapper(
                views.size());
    }

    @Override
    public Response checkService() {
        return Response.status(200).entity(new CheckServiceResponse("DataOrganizationTest", ServiceStatus.OK)).build();
    }

    @Override
    public Response downloadContent(
            String groupId,
            Long objectId,
            String viewName,
            String path,
            @javax.ws.rs.core.Context HttpContext hc) {
        ByteArrayInputStream bin = new ByteArrayInputStream((groupId + "/" + objectId + "/" + viewName + "/" + path).getBytes());
        return Response.ok(bin, MediaType.TEXT_PLAIN).build();
    }
}
