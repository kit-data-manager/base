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
package edu.kit.dama.rest.dataorganization.services.interfaces;

import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.util.Constants;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.StreamingOutput;

/**
 *
 * @author mf6319
 */
public interface IDataOrganizationRestService extends ICommonRestInterface {

  /**
   * Returns the DataOrganization root for the digital object with the provided
   * id. By default only one root node should be available for each digital
   * object. Therefore, arguments <i>first</i> and <i>results</i> can be ignored
   * in most cases.
   *
  
   * @summary Returns the DataOrganization root for the digital object with the
   * provided id.
   *
   * @param groupId The id of the group the node belongs to.
   * @param id The id of the digital object for which the DataOrganization root
   * node should be obtained.
   * @param first The first index. In typical environments the default value
   * should be fine.
   * @param results The max. number of results. In typical environments the
   * default value should be fine.
   * @param viewName The data organization view that will be returned.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A list of root nodes (typically exactly one) of the
   * DataOrganization stored for the provided digital object. All returned
   * entities are serialized using the <b>simple</b> object graph of
   * DataOrganizationNodeWrapper, which removes all attributes but the id from
   * the returned entities.
   *
   * @see
   * edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
   */
  @GET
  @Path(value = "/organization/{id}")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper")
  StreamingOutput getRootNode(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("id") Long id,
          @QueryParam("first") @DefaultValue("0") Integer first,
          @QueryParam("results") @DefaultValue("10") Integer results,
          @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Returns the number of root nodes in the DataOrganization for the digital
   * object with the provided id. By default only one root node should be
   * available for each digital object.
   *
   * @summary Returns the number of root nodes in the DataOrganization for the
   * digital object with the provided id.
   *
   * @param groupId The id of the group the node belongs to.
   * @param id The id of the digital object for which the DataOrganization root
   * node should be obtained.
   * @param viewName The data organization view that will be returned.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The number of root nodes (typically exactly one) wrapped by an
   * instance of DataOrganizationNodeWrapper.
   *
   * @see
   * edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
   */
  @GET
  @Path(value = "/organization/{id}/count/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper")
  StreamingOutput getRootNodeCount(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("id") Long id,
          @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Get a list of all children of the root node associated with the digital
   * object with the provided id. The resulting list will contain only the ids
   * of the children. Further querying is necessary to obtain details. For
   * performance reasons, the number of children per query can be controlled by
   * arguments <i>first</i>
   * and * <i>results</i>. By default, the first 10 child nodes (if available)
   * are returned.
   *
   * @summary Returns a list of children of the node with the provided id.
   *
   * @param groupId The id of the group the node belongs to.
   * @param id The id of the digital object the DataOrganization node is
   * associated with.
   * @param first First index of the child nodes, if available.
   * @param results Max. number of returned child nodes, if available.
   * @param viewName The data organization view that will be returned.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A list of child DataOrganization nodes associated with the root
   * node of the provided digital object. The returned entity is serialized
   * using the
   * <b>default</b> object graph of DataOrganizationNodeWrapper, which contains
   * all attributes but complex types. For complex attributes, e.g. the parent
   * node, the ids of the entity are returned and can be used for additional
   * queries.
   *
   * @see
   * edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
   */
  @GET
  @Path(value = "/organization/{id}/children")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper")
  StreamingOutput getRootNodeChildren(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("id") Long id,
          @QueryParam("first") @DefaultValue("0") Integer first,
          @QueryParam("results") @DefaultValue("10") Integer results,
          @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Returns information about the DataOrganization node with the provided node
   * id. The node must be part of the DataOrganization of the digital object
   * with the provided id. The result contains all information about the node
   * itself including information for navigation, e.g. parent node or children,
   * if available. These information can be used for further queries to walk the
   * entire DataOrganization.
   *
   * @summary Returns information about the DataOrganization node with the
   * provided id.
   *
   * @param groupId The id of the group the node belongs to.
   * @param id The id of the digital object the DataOrganization node is
   * associated with.
   * @param nodeId The id of the DataOrganization node the information is
   * requested for.
   * @param viewName The data organization view that will be returned.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The DataOrganization node with the provided nodeId associated with
   * the provided digital object. The returned entity is serialized using the
   * <b>default</b> object graph of DataOrganizationNodeWrapper, which contains
   * all attributes but complex types. For complex attributes, e.g. the parent
   * node, the ids of the entity are returned and can be used for additional
   * queries.
   *
   * @see edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
   */
  @GET
  @Path(value = "/organization/{id}/{nodeId}/")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper")
  StreamingOutput getNodeInformation(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("id") Long id,
          @PathParam("nodeId") Long nodeId,
          @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Get a list of all children of the node with the provided id. The resulting
   * list will contain only the ids of the children. Further querying is
   * necessary to obtain details. For performance reasons, the number of
   * children per query can be controlled by arguments <i>first</i>
   * and * <i>results</i>. By default, the first 10 child nodes (if available)
   * are returned.
   *
   * @summary Returns a list of children of the node with the provided id.
   *
   * @param groupId The id of the group the node belongs to.
   * @param id The id of the digital object the DataOrganization node is
   * associated with.
   * @param nodeId The id of the DataOrganization node the information is
   * requested for.
   * @param first First index of the child nodes, if available.
   * @param results Max. number of returned child nodes, if available.
   * @param viewName The data organization view that will be returned.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A list of DataOrganization nodes which are children of the node
   * with the provided nodeId associated with the provided digital object. The
   * returned entity is serialized using the
   * <b>default</b> object graph of DataOrganizationNodeWrapper, which contains
   * all attributes but complex types. For complex attributes, e.g. the parent
   * node, the ids of the entity are returned and can be used for additional
   * queries.
   *
   * @see
   * edu.kit.dama.mdm.dataorganization.types.DataOrganizationNodeWrapper
   */
  @GET
  @Path(value = "/organization/{id}/{nodeId}/children")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper")
  StreamingOutput getChildren(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("id") Long id,
          @PathParam("nodeId") Long nodeId,
          @QueryParam("first") @DefaultValue("0") Integer first,
          @QueryParam("results") @DefaultValue("100") Integer results,
          @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Returns the number of children of the node with the provided id for the
   * DataOrganization of the digital object with the provided id. For
   * performance reasons currently the max. count of children returned is 100.
   *
   * @summary Returns the number of children of the node with the provided id.
   *
   * @param groupId The id of the group the node belongs to.
   * @param id The id of the digital object the DataOrganization node is
   * associated with.
   * @param nodeId The id of the DataOrganization node the information is
   * requested for.
   * @param viewName The data organization view that will be returned.
   * @param hc The HttpContext for OAuth check.
   *
   * @return The number of children of the DataOrganization node with the
   * provided nodeId associated with the provided digital object. The result is
   * wrapped by an instance of DataOrganizationNodeWrapper.
   *
   * @see
   * edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
   */
  @GET
  @Path(value = "/organization/{id}/{nodeId}/count")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper")
  StreamingOutput getChildCount(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("id") Long id,
          @PathParam("nodeId") Long nodeId,
          @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * List available views for digital object
   *
   * @param groupId The id of the group the views belongs to.
   * @param id The id of the digital object the DataOrganization node is
   * associated with.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A DataOrganizationViewWrapper containing the views.
   */
  @GET
  @Path(value = "/organization/{id}/views")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper")
  StreamingOutput getViews(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("id") Long id,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Get the number of available views for digital object
   *
   * @param groupId The id of the group the views belongs to.
   * @param id The id of the digital object the DataOrganization node is
   * associated with.
   * @param hc The HttpContext for OAuth check.
   *
   * @return A DataOrganizationViewWrapper containing the views count.
   */
  @GET
  @Path(value = "/organization/{id}/views/count")
  @Produces("application/xml")
  @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper")
  StreamingOutput getViewCount(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("id") Long id,
          @javax.ws.rs.core.Context HttpContext hc);

  /**
   * Temporary test
   *
   * @param groupId
   * @param id The id of the digital object the DataOrganization node is
   * associated with.
   * @param nodeId
   * @param viewName
   * @param hc
   * @return
   */
  @GET
  @Path(value = "/organization/{id}/{nodeId}/transform")
  @Produces("application/octet-stream")
  StreamingOutput getTransformOutput(
          @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
          @PathParam("id") Long id,
          @PathParam("nodeId") Long nodeId,
          @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
          @javax.ws.rs.core.Context HttpContext hc);
}
