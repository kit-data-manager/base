/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology (support@kitdatamanager.net)
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
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.rest.dataorganization.services.interfaces.IDataOrganizationRestService;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.staging.entities.CollectionNodeImpl;
import edu.kit.dama.staging.entities.DataOrganizationNodeImpl;
import edu.kit.dama.util.Constants;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

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
  public StreamingOutput getRootNode(String groupId, Long id, Integer first,
          Integer results, String viewName, HttpContext hc) {

    CollectionNodeImpl rnd = new CollectionNodeImpl();

    rnd.setName(tree.getRootNode().getName());
    rnd.setNodeId(100l);

    IDataOrganizationNode nd1 = tree.getNodeByName("child 1");
    IDataOrganizationNode nd2 = tree.getNodeByName("child 2");

    rnd.addChild(nd1);
    rnd.addChild(nd2);

    return createObjectGraphStream(DataOrganizationNodeWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH,
            new DataOrganizationNodeWrapper(rnd));
  }

  @Override
  public StreamingOutput getRootNodeCount(String groupId, Long id,
          String viewName, HttpContext hc) {
    return createObjectGraphStream(DataOrganizationNodeWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper(1));
  }

  @Override
  public StreamingOutput getRootNodeChildren(String groupId, Long id,
          Integer first, Integer results, String viewName, HttpContext hc) {
    ICollectionNode rootNode = tree.getRootNode();
    List<? extends IDataOrganizationNode> children = rootNode.getChildren();
    List<DataOrganizationNodeImpl> childrenImpls = new LinkedList<>();

    for (IDataOrganizationNode nd : children) {
      if (nd instanceof DataOrganizationNodeImpl) {
        childrenImpls.add((DataOrganizationNodeImpl) nd);
      }
    }

    return createObjectGraphStream(DataOrganizationNodeWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper(
                    childrenImpls));
  }

  @Override
  public StreamingOutput getNodeInformation(String groupId, Long id,
          Long nodeId, String viewName, HttpContext hc) {

    IDataOrganizationNode nd = tree.getNodeByName("child " + nodeId);

    if (nd instanceof DataOrganizationNodeImpl) {
      DataOrganizationNodeImpl dnd = new DataOrganizationNodeImpl();
      dnd.setName(nd.getName());
      dnd.setViewName(nd.getViewName());
      return createObjectGraphStream(DataOrganizationNodeWrapper.class,
              Constants.REST_SIMPLE_OBJECT_GRAPH,
              new DataOrganizationNodeWrapper(dnd));
    } else {
      CollectionNodeImpl cnd = new CollectionNodeImpl();
      cnd.setName(nd.getName());
      cnd.setViewName(nd.getViewName());
      return createObjectGraphStream(DataOrganizationNodeWrapper.class,
              Constants.REST_SIMPLE_OBJECT_GRAPH,
              new DataOrganizationNodeWrapper(cnd));
    }
  }

  @Override
  public StreamingOutput getChildren(String groupId, Long id, Long nodeId,
          Integer first, Integer results, String viewName, HttpContext hc) {

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

    return createObjectGraphStream(DataOrganizationNodeWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH,
            new DataOrganizationNodeWrapper(l));
  }

  @Override
  public StreamingOutput getChildCount(String groupId, Long id, Long nodeId,
          String viewName, HttpContext hc) {

    IDataOrganizationNode nd;

    if (nodeId == 100l) {
      nd = tree.getNodeByName("root");
    } else {
      nd = tree.getNodeByName("child " + id.toString());
    }

    if (nd instanceof ICollectionNode) {
      int sz = ((ICollectionNode) nd).getChildren().size();

      return createObjectGraphStream(DataOrganizationNodeWrapper.class,
              Constants.REST_SIMPLE_OBJECT_GRAPH,
              new DataOrganizationNodeWrapper(sz));
    }

    return createObjectGraphStream(DataOrganizationNodeWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper(0));
  }

  @Override
  public StreamingOutput getViews(String groupId, Long id, HttpContext hc) {

    return createObjectGraphStream(DataOrganizationViewWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationViewWrapper(
                    views));
  }

  @Override
  public StreamingOutput getViewCount(String groupId, Long id, HttpContext hc) {

    return createObjectGraphStream(DataOrganizationViewWrapper.class,
            Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationViewWrapper(
                    views.size()));
  }

  @Override
  public StreamingOutput getTransformOutput(String groupId, Long id,
          Long nodeId, String viewName, HttpContext hc) {

    return createObjectGraphStream(DataOrganizationNodeWrapper.class,
            Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper(1));
  }

  /* @Override
   public StreamingOutput getMetadata(String groupId, Long id, Long nodeId,
   HttpContext hc) {

   return createObjectGraphStream(DataOrganizationNodeWrapper.class,
   Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper(1));
   }*/
  @Override
  public Response checkService() {
    return Response.status(200).entity(new CheckServiceResponse("DataOrganizationTest", ServiceStatus.OK)).build();
  }

}
