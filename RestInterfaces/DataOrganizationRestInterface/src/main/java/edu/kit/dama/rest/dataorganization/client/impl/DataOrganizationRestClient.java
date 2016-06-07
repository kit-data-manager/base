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

import com.sun.jersey.api.client.ClientResponse;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.util.Constants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import javax.ws.rs.core.MultivaluedMap;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
public class DataOrganizationRestClient extends AbstractRestClient {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DataOrganizationRestClient.class);

    private static final String QUERY_PARAMETER_VIEW_NAME = "viewName";
    private static final String FORM_PARAMETER_VIEW_DATA = "viewData";
    private static final String FORM_PARAMETER_PRESERVE_ATTRIBUTES = "preserveAttributes";

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
    private static final String ROOT_NODE = "/organization/{0}";
    /**
     * Download root node.
     */
    private static final String DOWNLOAD_ROOT_NODE = "/organization/download/{0}";
    /**
     * Download root node including path component.
     */
    private static final String DOWNLOAD_PATH = "/organization/download/{0}/{1}";

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
     * Default constructor.
     *
     * @param rootUrl The service root URL, e.g.
     * http://localhost:8080/KITDM/dataorganization.
     * @param pContext The context used to access the service.
     */
    public DataOrganizationRestClient(String rootUrl, SimpleRESTContext pContext) {
        super(rootUrl, pContext);
    }
// <editor-fold defaultstate="collapsed" desc="Generic Rest methods (GET, PUT, POST, DELETE)">

    /**
     * Perform a GET for DataOrganizationNode.
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
     * Perform a POST for DataOrganizationNode.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams The form params
     *
     * @return DataOrganizationNodeWrapper.
     */
    private DataOrganizationNodeWrapper performDataOrganizationNodePost(
            String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return RestClientUtils.performPost(DataOrganizationNodeWrapper.class,
                getWebResource(pPath), pQueryParams, pFormParams);
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * This method is a shortcut for calling {@link #getChildren(java.lang.String, java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.Integer, java.lang.String, edu.kit.dama.rest.SimpleRESTContext)
     * } using the root node id a third argument.
     *
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * This method is a shortcut for calling
     * {@link #getChildren(java.lang.String, java.lang.Long, java.lang.Long, java.lang.Integer, java.lang.Integer, java.lang.String, edu.kit.dama.rest.SimpleRESTContext)}
     * using the root node id a third argument.
     *
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * detailed information also contains navigation information (e.g. parent)
     * and attributes.
     *
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * detailed information also contains navigation information (e.g. parent)
     * and attributes.
     *
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * Get the number of supported views for the data organization of the
     * digital object with the provided id.
     *
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
     * @param pId The id of the digital object.
     *
     * @return A DataOrganizationViewWrapper entity containing the result.
     */
    public DataOrganizationViewWrapper getViewCount(String pGroupId, Long pId) {
        return getViewCount(pGroupId, pId, null);
    }

    /**
     * Get the number of supported views for the data organization of the
     * digital object with the provided id.
     *
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
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
     * Download the data located at the provided data organization path for the
     * object with the provided id in the view with the provided name. The path
     * must be the absolute path inside the data organization tree, e.g.
     * /results/images/ResultImage.png or /results/images/. If pPath refers to a
     * file this file is downloaded, if it referes to a collection of files, a
     * zip file containing the collection is downloaded.
     *
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
     * @param pId The id of the digital object.
     * @param pViewName The view name from which the data will be obtained.
     * @param pDataOrganizationPath The path inside the view that will be
     * downloaded.
     * @param pDestination The destination folder where to store the downloaded
     * file.
     * @param pFilename The destination filename.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @throws IOException if downloading the data fails.
     */
    public void downloadData(String pGroupId, Long pId, String pViewName, String pDataOrganizationPath, File pDestination, String pFilename, SimpleRESTContext pSecurityContext) throws IOException {
        if (pId == null) {
            throw new IllegalArgumentException(
                    "Digital object ID may not be null");
        }

        LOGGER.debug("Obtaining download path.");
        String path = (pDataOrganizationPath == null) ? "/" : pDataOrganizationPath;
        LOGGER.debug("Downloading data from absolut path '{}'", path);
        String resourceUrl = RestClientUtils.encodeUrl(DOWNLOAD_PATH, pId, ((!path.startsWith("/")) ? "/" + path : path));
        LOGGER.debug("Resource for path is: '{}'", resourceUrl);

        MultivaluedMap queryParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        LOGGER.debug("Requesting download stream.");
        ClientResponse response = getWebResource(resourceUrl).queryParams(queryParams).get(ClientResponse.class);

        String filename;
        LOGGER.debug("Determining download filename.");
        if (pFilename == null) {
            LOGGER.debug("Evaluating Content-Disposition header.");
            String content = response.getHeaders().getFirst("Content-Disposition");
            if (content != null) {
                LOGGER.debug("Content disposition header with value {} found. Trying to obtain filename.", content);
                filename = content.substring(content.indexOf("=") + 1);
                if (filename.isEmpty()) {
                    throw new IllegalArgumentException("Invalid Content-Disposition header '" + content + "' results in empty filename.");
                }
                LOGGER.debug("Using filename '{}' from Content-Disposition header.", pFilename);
            } else {
                throw new IllegalArgumentException("Argument pFilename must not be 'null' if server does not return a proper Content-Disposition header.");
            }
        } else {
            LOGGER.debug("Using user-provided filename '{}'", pFilename);
            filename = pFilename;
        }

        LOGGER.debug("Reading input stream and writing content to file.");
        InputStream in = response.getEntityInputStream();
        FileOutputStream fout = new FileOutputStream(new File(pDestination, filename));
        try {
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            while (len != -1) {
                fout.write(buffer, 0, len);
                len = in.read(buffer);
            }
        } finally {
            LOGGER.debug("Closing streams.");
            fout.flush();
            fout.close();
            in.close();
        }
    }

    /**
     * Get the root node of the data organization associated with the provided
     * object id and the provided view identifier. The arguments pFirst and
     * pResults could be ignored in most cases as typically only one root node
     * should be available.
     *
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
     * @param pId The id of the digital object.
     * @param pView The file tree representing the view to create.
     * @param pPreserveAttributes If TRUE all data organization node attributes
     * of existing nodes are preserved in the new view.
     *
     * @return A DataOrganizationNodeWrapper entity containing the root node of
     * the new view.
     */
    public DataOrganizationNodeWrapper postView(String pGroupId, Long pId,
            IFileTree pView, Boolean pPreserveAttributes) {
        return postView(pGroupId, pId, pView, pPreserveAttributes, null);
    }

    /**
     * Get the root node of the data organization associated with the provided
     * object id and the provided view identifier. The arguments pFirst and
     * pResults could be ignored in most cases as typically only one root node
     * should be available.
     *
     * @param pGroupId The id of the group to which the associated digital
     * object belongs.
     * @param pId The id of the digital object.
     * @param pView The file tree representing the view to create.
     * @param pPreserveAttributes If TRUE all data organization node attributes
     * of existing nodes are preserved in the new view.
     * @param pSecurityContext The security context used to access the REST
     * interface.
     *
     * @return A DataOrganizationNodeWrapper entity containing the root node of
     * the new view.
     */
    public DataOrganizationNodeWrapper postView(String pGroupId, Long pId,
            IFileTree pView, Boolean pPreserveAttributes, SimpleRESTContext pSecurityContext) {

        MultivaluedMap queryParams;
        MultivaluedMap formParams;
        setFilterFromContext(pSecurityContext);
        queryParams = new MultivaluedMapImpl();
        formParams = new MultivaluedMapImpl();

        if (pId == null) {
            throw new IllegalArgumentException(
                    "Digital object ID must not be null");
        }

        if (pView == null) {
            throw new IllegalArgumentException(
                    "Argument pView must not be null");
        }

        if (Util.isReservedViewName(pView.getViewName())) {
            throw new IllegalArgumentException(MessageFormat.format("The name '{0}' of the provided view is a reserved name and my not be used for custom views.", pView.getViewName()));
        }

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }
        JSONObject viewData = Util.fileTreeToJsonView(pView);

        formParams.add(FORM_PARAMETER_VIEW_DATA, viewData.toString());
        if (pPreserveAttributes != null) {
            formParams.add(FORM_PARAMETER_PRESERVE_ATTRIBUTES, pPreserveAttributes.toString());
        }
        return performDataOrganizationNodePost(RestClientUtils.encodeUrl(
                ROOT_NODE, pId), queryParams, formParams);
    }

    /**
     * Simple testing.
     *
     * @param args Command line arguments.
     *
     * @throws Exception If something fails.
     */
    public static void main(String[] args) throws Exception {
        SimpleRESTContext ctx = new SimpleRESTContext("admin", "dama14");
        DataOrganizationRestClient client = new DataOrganizationRestClient("http://localhost:8080/KITDM/rest/dataorganization", ctx);
        System.out.println("Getting root node");
        client.downloadData(Constants.USERS_GROUP_ID, 34l, "default", "/", new File("."), null, ctx);

        /*System.out.println("Get child count");
         System.out.println(client.getChildCount("USERS", 4l, 100l, "default", ctx).getCount());

         System.out.println("Get views");
         System.out.println(client.getViews("USERS", 4l, ctx).getEntities());*/
    }
}
