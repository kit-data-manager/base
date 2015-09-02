/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.rest.dataorganization.client.impl;

import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.util.Constants;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author mf6319
 */
public class DataOrganizationRestClient extends AbstractRestClient {

  private static final String QUERY_PARAMETER_VIEW_NAME = "viewName";

  // <editor-fold defaultstate="collapsed" desc="URL components">
  /**
   * 'url' for count.
   */
  private static final String COUNT = "/count";
  /**
   * 'url' for children.
   */
  private static final String CHILDREN = "/children";

  /**
   * Root node.
   */
  private static final String ROOT_NODE = "/{0}";

  /**
   * 'url' for root children count.
   */
  private static final String ROOT_COUNT = ROOT_NODE + COUNT;
  /**
   * Get all root children.
   */
  private static final String ROOT_CHILDREN = ROOT_NODE + CHILDREN;
  /**
   * Get all views.
   */
  private static final String VIEWS = ROOT_NODE + "/views";
  /**
   * Get the number of all views.
   */
  private static final String VIEWS_COUNT = VIEWS + COUNT;
  /**
   * Get a child node.
   */
  private static final String NODE = ROOT_NODE + "/{1}";

  /**
   * Get children of a node.
   */
  private static final String NODE_CHILDREN = NODE + CHILDREN;

  /**
   * Node children count.
   */
  private static final String NODE_CHILDREN_COUNT = NODE + COUNT;

// </editor-fold>
  /**
   *
   * @param rootUrl
   * @param pContext
   */
  public DataOrganizationRestClient(String rootUrl, SimpleRESTContext pContext) {
    super(rootUrl, pContext);
  }
// <editor-fold defaultstate="collapsed" desc="Generic Rest methods (GET, PUT, POST, DELETE)">

  /**
   * Perform a get for DataOrganizationNode.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   *
   * @return DataOrganizationNodeWrapper.
   */
  private DataOrganizationNodeWrapper performDataOrganizationNodeGet(
          String pPath, MultivaluedMap pQueryParams) {
    return RestClientUtils.performGet(DataOrganizationNodeWrapper.class,
            getWebResource(pPath), pQueryParams);
  }

  /**
   * Perform a get for DataOrganizationView.
   *
   * @param pPath url
   * @param pQueryParams url parameters
   *
   * @return DataOrganizationView.
   */
  private DataOrganizationViewWrapper performDataOrganizationViewGet(
          String pPath, MultivaluedMap pQueryParams) {
    return RestClientUtils.performGet(DataOrganizationViewWrapper.class,
            getWebResource(pPath), pQueryParams);
  }
  // </editor-fold>

  /**
   * Get the root node of the data organization associated with the provided
   * object id and the provided view identifier. The arguments pFirst and
   * pResults could be ignored in most cases as typically only one root node
   * should be available.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pFirstIndex The first index, which can be ignored.
   * @param pResults The number of results, which can be ignored.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getRootNode(String pGroupId, Long pId,
          Integer pFirstIndex, Integer pResults, String pViewName) {

    return getRootNode(pGroupId, pId, pFirstIndex, pResults, pViewName, null);
  }

  /**
   * Get the root node of the data organization associated with the provided
   * object id and the provided view identifier. The arguments pFirst and
   * pResults could be ignored in most cases as typically only one root node
   * should be available.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pFirstIndex The first index, which can be ignored.
   * @param pResults The number of results, which can be ignored.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   * @param pSecurityContext The security context used to access the REST
   * interface.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getRootNode(String pGroupId, Long pId,
          Integer pFirstIndex, Integer pResults, String pViewName,
          SimpleRESTContext pSecurityContext) {

    DataOrganizationNodeWrapper returnValue;
    MultivaluedMap queryParams;
    setFilterFromContext(pSecurityContext);
    queryParams = new MultivaluedMapImpl();

    if (pId == null) {
      throw new IllegalArgumentException(
              "Digital object ID may not be null");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    if (pViewName != null) {
      queryParams.add(QUERY_PARAMETER_VIEW_NAME, pViewName);
    }
    if (pFirstIndex != null) {
      queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
              pFirstIndex));
    }
    if (pResults != null) {
      queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
              pResults));
    }

    returnValue = performDataOrganizationNodeGet(RestClientUtils.encodeUrl(
            ROOT_NODE, pId), queryParams);
    return returnValue;
  }

  /**
   * Get the number of root nodes within the data organization associated with
   * the provided object id and the provided view identifier.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getRootNodeCount(String pGroupId,
          Long pId, String pViewName) {

    return getRootNodeCount(pGroupId, pId, pViewName, null);
  }

  /**
   * Get the number of root nodes within the data organization associated with
   * the provided object id and the provided view identifier.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   * @param pSecurityContext The security context used to access the REST
   * interface.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getRootNodeCount(String pGroupId,
          Long pId, String pViewName, SimpleRESTContext pSecurityContext) {

    DataOrganizationNodeWrapper returnValue;
    MultivaluedMap queryParams;
    setFilterFromContext(pSecurityContext);
    queryParams = new MultivaluedMapImpl();

    if (pId == null) {
      throw new IllegalArgumentException(
              "Digital object ID may not be null");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    if (pViewName != null) {
      queryParams.add(QUERY_PARAMETER_VIEW_NAME, pViewName);
    }

    returnValue = performDataOrganizationNodeGet(RestClientUtils.encodeUrl(
            ROOT_COUNT, pId), queryParams);
    return returnValue;
  }

  /**
   * Get the list of children of the root node within the data organization
   * associated with the provided object id and the provided view identifier.
   * This method is a shortcut for calling {@link #getChildren(java.lang.String, java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.Integer, java.lang.String, edu.kit.dama.rest.SimpleRESTContext) using the root node id a third argument.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pFirstIndex The first node index which is returned.
   * @param pResults The max. number of results.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getRootNodeChildren(String pGroupId,
          Long pId, Integer pFirstIndex, Integer pResults, String pViewName) {

    return getRootNodeChildren(pGroupId, pId, pFirstIndex, pResults,
            pViewName, null);
  }

  /**
   * Get the list of children of the root node within the data organization
   * associated with the provided object id and the provided view identifier.
   * This method is a shortcut for calling {@link #getChildren(java.lang.String, java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.Integer, java.lang.String, edu.kit.dama.rest.SimpleRESTContext) using the root node id a third argument.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pFirstIndex The first node index which is returned.
   * @param pResults The max. number of results.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   * @param pSecurityContext The security context used to access the REST
   * interface.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getRootNodeChildren(String pGroupId,
          Long pId, Integer pFirstIndex, Integer pResults, String pViewName,
          SimpleRESTContext pSecurityContext) {

    DataOrganizationNodeWrapper returnValue;
    MultivaluedMap queryParams;
    setFilterFromContext(pSecurityContext);
    queryParams = new MultivaluedMapImpl();

    if (pId == null) {
      throw new IllegalArgumentException(
              "Digital object ID may not be null");
    }

    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    if (pViewName != null) {
      queryParams.add(QUERY_PARAMETER_VIEW_NAME, pViewName);
    }
    if (pFirstIndex != null) {
      queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
              pFirstIndex));
    }
    if (pResults != null) {
      queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
              pResults));
    }

    returnValue = performDataOrganizationNodeGet(RestClientUtils.encodeUrl(
            ROOT_CHILDREN, pId), queryParams);
    return returnValue;
  }

  /**
   * Get detailed node information of the node with the provided id. The
   * detailed information also contains navigation information (e.g. parent) and
   * attributes.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pNodeId The id of the node to which the details will be obtained.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getNodeInformation(String pGroupId,
          Long pId, Long pNodeId, String pViewName) {

    return getNodeInformation(pGroupId, pId, pNodeId, pViewName, null);
  }

  /**
   * Get detailed node information of the node with the provided id. The
   * detailed information also contains navigation information (e.g. parent) and
   * attributes.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pNodeId The id of the node to which the details will be obtained.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   * @param pSecurityContext The security context used to access the REST
   * interface.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getNodeInformation(String pGroupId,
          Long pId, Long pNodeId, String pViewName,
          SimpleRESTContext pSecurityContext) {

    DataOrganizationNodeWrapper returnValue;
    MultivaluedMap queryParams;
    setFilterFromContext(pSecurityContext);
    queryParams = new MultivaluedMapImpl();

    if (pId == null) {
      throw new IllegalArgumentException(
              "Digital object ID may not be null");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    if (pViewName != null) {
      queryParams.add(QUERY_PARAMETER_VIEW_NAME, pViewName);
    }

    returnValue = performDataOrganizationNodeGet(RestClientUtils.encodeUrl(
            NODE, pId, pNodeId), queryParams);
    return returnValue;
  }

  /**
   * Get a list of child nodes for the node with the provided id belonging the
   * the DataOrganization of the digital object with the provided id.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pNodeId The id of the node to which the children will be obtained.
   * @param pFirstIndex The first node index which is returned.
   * @param pResults The max. number of results.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getChildren(String pGroupId, Long pId,
          Long pNodeId, Integer pFirstIndex, Integer pResults, String pViewName) {

    return getChildren(pGroupId, pId, pNodeId, pFirstIndex, pResults,
            pViewName, null);
  }

  /**
   * Get a list of child nodes for the node with the provided id belonging the
   * the DataOrganization of the digital object with the provided id.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pNodeId The id of the node to which the children will be obtained.
   * @param pFirstIndex The first node index which is returned.
   * @param pResults The max. number of results.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   * @param pSecurityContext The security context used to access the REST
   * interface.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getChildren(String pGroupId, Long pId,
          Long pNodeId, Integer pFirstIndex, Integer pResults, String pViewName,
          SimpleRESTContext pSecurityContext) {

    DataOrganizationNodeWrapper returnValue;
    MultivaluedMap queryParams;
    setFilterFromContext(pSecurityContext);
    queryParams = new MultivaluedMapImpl();

    if (pId == null) {
      throw new IllegalArgumentException(
              "Digital object ID may not be null");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    if (pViewName != null) {
      queryParams.add(QUERY_PARAMETER_VIEW_NAME, pViewName);
    }
    if (pFirstIndex != null) {
      queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(
              pFirstIndex));
    }
    if (pResults != null) {
      queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(
              pResults));
    }

    returnValue = performDataOrganizationNodeGet(RestClientUtils.encodeUrl(
            NODE_CHILDREN, pId, pNodeId), queryParams);
    return returnValue;
  }

  /**
   * Get the number of children of the node with the provided id belonging the
   * the DataOrganization of the digital object with the provided id.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pNodeId The id of the node to which the number of children will be
   * obtained.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getChildCount(String pGroupId, Long pId,
          Long pNodeId, String pViewName) {

    return getChildCount(pGroupId, pId, pNodeId, pViewName, null);
  }

  /**
   * Get the number of children of the node with the provided id belonging the
   * the DataOrganization of the digital object with the provided id.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pNodeId The id of the node to which the number of children will be
   * obtained.
   * @param pViewName The identifier of the view. Typically, 'default' can be
   * used.
   * @param pSecurityContext The security context used to access the REST
   * interface.
   *
   * @return A DataOrganizationNodeWrapper entity containing the result.
   */
  public DataOrganizationNodeWrapper getChildCount(String pGroupId, Long pId,
          Long pNodeId, String pViewName, SimpleRESTContext pSecurityContext) {

    DataOrganizationNodeWrapper returnValue;
    MultivaluedMap queryParams;
    setFilterFromContext(pSecurityContext);
    queryParams = new MultivaluedMapImpl();

    if (pId == null) {
      throw new IllegalArgumentException(
              "Digital object ID may not be null");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }
    if (pViewName != null) {
      queryParams.add(QUERY_PARAMETER_VIEW_NAME, pViewName);
    }

    returnValue = performDataOrganizationNodeGet(RestClientUtils.encodeUrl(
            NODE_CHILDREN_COUNT, pId, pNodeId), queryParams);
    return returnValue;
  }

  /**
   * Get a list of supported views for the data organization of the digital
   * object with the provided id.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   *
   * @return A DataOrganizationViewWrapper entity containing the result.
   */
  public DataOrganizationViewWrapper getViews(String pGroupId, Long pId) {
    return getViews(pGroupId, pId, null);
  }

  /**
   * Get a list of supported views for the data organization of the digital
   * object with the provided id.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pSecurityContext The security context used to access the REST
   * interface.
   *
   * @return A DataOrganizationViewWrapper entity containing the result.
   */
  public DataOrganizationViewWrapper getViews(String pGroupId, Long pId,
          SimpleRESTContext pSecurityContext) {

    DataOrganizationViewWrapper returnValue;
    MultivaluedMap queryParams;
    setFilterFromContext(pSecurityContext);
    queryParams = new MultivaluedMapImpl();

    if (pId == null) {
      throw new IllegalArgumentException(
              "Digital object ID may not be null");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }

    returnValue = performDataOrganizationViewGet(RestClientUtils.encodeUrl(
            VIEWS, pId), queryParams);
    return returnValue;
  }

  /**
   * Get the number of supported views for the data organization of the digital
   * object with the provided id.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   *
   * @return A DataOrganizationViewWrapper entity containing the result.
   */
  public DataOrganizationViewWrapper getViewCount(String pGroupId, Long pId) {
    return getViewCount(pGroupId, pId, null);
  }

  /**
   * Get the number of supported views for the data organization of the digital
   * object with the provided id.
   *
   * @param pGroupId The id of the group to which the associated digital object
   * belongs.
   * @param pId The id of the digital object.
   * @param pSecurityContext The security context used to access the REST
   * interface.
   *
   * @return A DataOrganizationViewWrapper entity containing the result.
   */
  public DataOrganizationViewWrapper getViewCount(String pGroupId, Long pId,
          SimpleRESTContext pSecurityContext) {

    DataOrganizationViewWrapper returnValue;
    MultivaluedMap queryParams;
    setFilterFromContext(pSecurityContext);
    queryParams = new MultivaluedMapImpl();

    if (pId == null) {
      throw new IllegalArgumentException(
              "Digital object ID may not be null");
    }
    if (pGroupId != null) {
      queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
    }

    returnValue = performDataOrganizationViewGet(RestClientUtils.encodeUrl(
            VIEWS_COUNT, pId), queryParams);
    return returnValue;
  }

  /**
   * Not implemented, yet.
   *
   * @param groupId Not implemented, yet.
   * @param id Not implemented, yet.
   * @param nodeId Not implemented, yet.
   * @param viewName Not implemented, yet.
   * @param hc Not implemented, yet.
   *
   * @return Not implemented, yet.
   */
  public StreamingOutput getTransformOutput(String groupId, Long id,
          Long nodeId, String viewName, HttpContext hc) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   * Simple testing.
   *
   * @param args Command line arguments.
   */
  public static void main(String[] args) {
    SimpleRESTContext ctx = new SimpleRESTContext("t3Qyl4dFJ3HS4GzQ", "Bl9pcHHBgE6WPH7H");
    DataOrganizationRestClient client = new DataOrganizationRestClient("http://localhost:8089/KITDM/rest/dataorganization/organization", ctx);
    System.out.println("Getting root node");
    System.out.println(client.getRootNode(Constants.USERS_GROUP_ID, 4l, 0, 1, Constants.DEFAULT_VIEW, ctx).getCount());
    System.out.println("Getting children");
    System.out.println(client.getChildren(Constants.USERS_GROUP_ID, 4l, 100l, 0, 10, Constants.DEFAULT_VIEW, ctx).getEntities());

    /*System.out.println("Get child count");
     System.out.println(client.getChildCount("USERS", 4l, 100l, "default", ctx).getCount());

     System.out.println("Get views");
     System.out.println(client.getViews("USERS", 4l, ctx).getEntities());*/
  }
}
