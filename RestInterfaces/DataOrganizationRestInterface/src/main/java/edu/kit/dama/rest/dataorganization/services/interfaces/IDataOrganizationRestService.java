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
package edu.kit.dama.rest.dataorganization.services.interfaces;

import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.ResourceConfig;
import edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.util.Constants;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author mf6319
 */
@Path("/rest/dataorganization")
public interface IDataOrganizationRestService extends ICommonRestInterface {

    /**
     * Returns the DataOrganization root for the digital object with the
     * provided id. By default only one root node should be available for each
     * digital object. Therefore, arguments <i>first</i> and <i>results</i> can
     * be ignored in most cases.
     *
     *
     * @summary Returns the DataOrganization root for the digital object with
     * the provided id.
     *
     * @param groupId The id of the group the node belongs to.
     * @param baseId The baseId of the digital object for which the
     * DataOrganization root node should be obtained.
     * @param first The first index. In typical environments the default value
     * should be fine.
     * @param results The max. number of results. In typical environments the
     * default value should be fine.
     * @param viewName The data organization view that will be returned.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of root nodes (typically exactly one) of the
     * DataOrganization stored for the provided digital object.
     *
     * @see edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
     */
    @GET
    @Path(value = "/organization/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode>")
    IEntityWrapper<? extends IDefaultDataOrganizationNode> getRootNode(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long baseId,
            @QueryParam("first") @DefaultValue("0") Integer first,
            @QueryParam("results") @DefaultValue("10") Integer results,
            @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Add a new DataOrganization view to the DigitalObject with the provided
     * id. This can be used to provide an alternative DataOrganization for a
     * digital object and to re-structure the DataOrganization of a digital
     * object. The provided view must not be equal to the 'default' view but may
     * use nodes of the default view. Existing nodes are identified by their
     * node id. If an existing node (either file or collection node) is
     * referenced, its contents (and child nodes) are be duplicated to the new
     * view. Furthermore, it is possible to provide attributes for each node.
     * Their handling dependes on the 'preserveAttributes' flag. If the flag is
     * 'TRUE' existing attributes of existing nodes are preserved and provided
     * attributes are added. If the flag is set to 'FALSE' existing attributes
     * are ignored and the only attributed in the JSON structure are added to
     * the elements.
     *
     * The format of viewData must be:
     * <pre>
     * {
     *"objectId":"1234-5678-abcd",
     *"viewName":"custom",
     *"children":[
     *    {
     *       "nodeId":100,
     *       "type":"CollectionNode"
     *       "attributes":[
     *          {
     *              "childAttrib":"customNewAttribute"
     *          }
     *      ]
     *   },{
     *      "name":"newCollectionNode",
     *      "type":"CollectionNode"
     *      "children":[
     *          {
     *              "nodeId":200,
     *              "type":"FileNode",
     *              "attributes":[
     *                  {
     *                      "subChildAttrib":"myValue"
     *                  }
     *              ]
     *          },
     *          {
     *             "nodeId":300,
     *             "type":"FileNode"
     *          }
     *      ]
     *    }
     * ]
     * }
     * </pre>
     *
     * @summary Create a new DataOrganization view for the object with the
     * provided baseId.
     *
     * @param groupId The id of the group the digital object belongs to.
     * @param baseId The baseId of the digital object for which a
     * DataOrganization view should be posted.
     * @param viewData The data structure defining the data organization view.
     * @param preserveAttributes If TRUE all attributes of existing nodes are
     * preserved meaning they are copied to the according node in the new view.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The root node of the created DataOrganization view stored for the
     * provided digital object.
     *
     * @see edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
     */
    @POST
    @Path(value = "/organization/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode>")
    IEntityWrapper<? extends IDefaultDataOrganizationNode> addDataOrganizationView(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long baseId,
            @FormParam("viewData") String viewData,
            @FormParam("preserveAttributes") Boolean preserveAttributes,
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
     * @param baseId The baseId of the digital object for which the
     * DataOrganization root node should be obtained.
     * @param viewName The data organization view that will be returned.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of root nodes (typically exactly one) wrapped by an
     * instance of DataOrganizationNodeWrapper.
     *
     * @see edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
     */
    @GET
    @Path(value = "/organization/{id}/count/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode>")
    IEntityWrapper<? extends IDefaultDataOrganizationNode> getRootNodeCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long baseId,
            @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a list of all children of the root node associated with the digital
     * object with the provided id. The resulting list will contain only the ids
     * of the children. Further querying is necessary to obtain details. For
     * performance reasons, the number of children per query can be controlled
     * by arguments <i>first</i>
     * and <i>results</i>. By default, the first 10 child nodes (if available)
     * are returned.
     *
     * @summary Returns a list of children of the node with the provided id.
     *
     * @param groupId The id of the group the node belongs to.
     * @param baseId The baseId of the digital object the DataOrganization node
     * is associated with.
     * @param first First index of the child nodes, if available.
     * @param results Max. number of returned child nodes, if available.
     * @param viewName The data organization view that will be returned.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of child DataOrganization nodes associated with the root
     * node of the provided digital object.
     *
     * @see edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
     */
    @GET
    @Path(value = "/organization/{id}/children")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode>")
    IEntityWrapper<? extends IDefaultDataOrganizationNode> getRootNodeChildren(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long baseId,
            @QueryParam("first") @DefaultValue("0") Integer first,
            @QueryParam("results") @DefaultValue("10") Integer results,
            @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Returns information about the DataOrganization node with the provided
     * node id. The node must be part of the DataOrganization of the digital
     * object with the provided id. The result contains all information about
     * the node itself including information for navigation, e.g. parent node or
     * children, if available. These information can be used for further queries
     * to walk the entire DataOrganization.
     *
     * @summary Returns information about the DataOrganization node with the
     * provided id.
     *
     * @param groupId The id of the group the node belongs to.
     * @param baseId The baseId of the digital object the DataOrganization node
     * is associated with.
     * @param nodeId The id of the DataOrganization node the information is
     * requested for.
     * @param viewName The data organization view that will be returned.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The DataOrganization node with the provided nodeId associated
     * with the provided digital object.
     *
     * @see edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
     */
    @GET
    @Path(value = "/organization/{id}/{nodeId}/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode>")
    IEntityWrapper<? extends IDefaultDataOrganizationNode> getNodeInformation(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long baseId,
            @PathParam("nodeId") Long nodeId,
            @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get a list of all children of the node with the provided id. The
     * resulting list will contain only the ids of the children. Further
     * querying is necessary to obtain details. For performance reasons, the
     * number of children per query can be controlled by arguments <i>first</i>
     * and <i>results</i>. By default, the first 10 child nodes (if available)
     * are returned.
     *
     * @summary Returns a list of children of the node with the provided id.
     *
     * @param groupId The id of the group the node belongs to.
     * @param baseId The baseId of the digital object the DataOrganization node
     * is associated with.
     * @param nodeId The id of the DataOrganization node the information is
     * requested for.
     * @param first First index of the child nodes, if available.
     * @param results Max. number of returned child nodes, if available.
     * @param viewName The data organization view that will be returned.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of DataOrganization nodes which are children of the node
     * with the provided nodeId associated with the provided digital object.
     *
     * @see edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
     */
    @GET
    @Path(value = "/organization/{id}/{nodeId}/children")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode>")
    IEntityWrapper<? extends IDefaultDataOrganizationNode> getChildren(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long baseId,
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
     * @param baseId The baseId of the digital object the DataOrganization node
     * is associated with.
     * @param nodeId The id of the DataOrganization node the information is
     * requested for.
     * @param viewName The data organization view that will be returned.
     * @param hc The HttpContext for OAuth check.
     *
     * @return The number of children of the DataOrganization node with the
     * provided nodeId associated with the provided digital object.
     *
     * @see edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper
     */
    @GET
    @Path(value = "/organization/{id}/{nodeId}/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode>")
    IEntityWrapper<? extends IDefaultDataOrganizationNode> getChildCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long baseId,
            @PathParam("nodeId") Long nodeId,
            @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * List available views for digital object
     *
     * @param groupId The id of the group the views belongs to.
     * @param baseId The baseId of the digital object the DataOrganization node
     * is associated with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A DataOrganizationViewWrapper containing the views.
     */
    @GET
    @Path(value = "/organization/{id}/views")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper")
    DataOrganizationViewWrapper getViews(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long baseId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Get the number of available views for digital object.
     *
     * @param groupId The id of the group the views belongs to.
     * @param baseId The baseId of the digital object the DataOrganization node
     * is associated with.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A DataOrganizationViewWrapper containing the views count.
     */
    @GET
    @Path(value = "/organization/{id}/views/count")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper")
    DataOrganizationViewWrapper getViewCount(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long baseId,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Download content associated to the data organization of a digital object.
     * There are three ways that should be supported to deliver content:
     *
     * <ul>
     * <li>Use the 'path' potion of the URL to provide a path pointing to a file
     * node of the data organization. This will deliver the data of the
     * according file if it is currently accessible. Otherwise status 501 (Not
     * Implemented) should be returned. 404 should only be used if accessing an
     * invalid resource,e.g. a node that does not exist. </li>
     * <li>Use the 'path' potion of the URL to provide a path pointing to a
     * collection node of the data organization. This will deliver all data
     * located below the according node if it is currently accessible, e.g. as
     * zip file. Otherwise status 501 (Not Implemented) should be returned. 404
     * should only be used if accessing an invalid resource,e.g. a node that
     * does not exist. </li>
     * <li>Use the 'path' potion of the URL to provide ONE id of an according
     * data organization node in the tree associated with the provided object
     * and view. As all ids are unique inside the tree, only the id of the node
     * has to be provided. Depending on the node type the case should deliver
     * the same result as scenario 1 and 2. </li>
     * </ul>
     *
     * This methods supports all kinds of media types. If the type can be
     * determined from the delivered file it should be provided to the client.
     * Otherwise, application/octet-stream is recommended to be used.
     *
     * To support the client, a header field 'Content-Disposition' looking as
     * follows should be delivered:
     *
     * <i>attachment; filename=&lt;FILENAME.EXT&gt;</i>
     *
     * @param groupId The id of the group used to authorize the access.
     * @param baseId The baseId of the digital object the DataOrganization is
     * associated with.
     * @param viewName The data organization view that will be accessed.
     * @param path The path in the data organization view to access.
     * @param hc The HttpContext for OAuth check.
     * @param context The context for init param check.
     *
     * @return A response object providing a stream to the according data
     * provided by the call.
     */
    @GET
    @Path(value = "/organization/download/{objectId}/{path: .*}")
    @Produces(MediaType.WILDCARD)
    Response downloadContent(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("objectId") Long baseId,
            @QueryParam("viewName") @DefaultValue(Constants.DEFAULT_VIEW) String viewName,
            @PathParam("path") String path,
            @javax.ws.rs.core.Context HttpContext hc,
            @javax.ws.rs.core.Context ResourceConfig context);

}
