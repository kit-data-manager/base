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
package edu.kit.dama.rest.dataorganization.services.impl;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.rest.dataorganization.services.interfaces.IDataOrganizationRestService;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDefaultDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.ISimpleDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import edu.kit.dama.mdm.dataorganization.impl.staging.AttributeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.DataOrganizationNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.dataorganization.types.ElementPath;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.rest.util.RestUtils;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.util.Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author mf6319
 */
@Path("/")
public final class DataOrganizationRestServiceImpl implements IDataOrganizationRestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataOrganizationRestServiceImpl.class);

    private static final Class[] IMPL_CLASSES = new Class[]{DataOrganizationNodeWrapper.class,
        LFNImpl.class,
        URL.class,
        DataOrganizationNodeImpl.class,
        FileNodeImpl.class,
        CollectionNodeImpl.class,
        AttributeImpl.class
    };

    // inTreeIdVersion is currently fixed here and in DataOrganization. This may change in future.
    int inTreeIdVersion = 1;

    /**
     * Get the digital object id for the provided base id. If No object for
     * pBaseId exists, a WebApplicationException with error code 404 ist thrown.
     * If the accessing context is not authorized to access the object a
     * WebApplicationException with HTTP 401 is thrown.
     *
     * @param pBaseId The base id.
     * @param pGroupId The group if for authorization.
     * @param hc The HttpContext for authorization.
     *
     * @return The digital object id.
     */
    private DigitalObjectId getDigitalObjectId(Long pBaseId, String pGroupId, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(pGroupId));
        LOGGER.debug("Getting root for id {} with context {}", new Object[]{pBaseId, ctx});
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Obtaining digital object for id {}", pBaseId);
            mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
            DigitalObject object = mdm.find(DigitalObject.class, pBaseId);
            if (object == null) {
                throw new WebApplicationException(new Exception("Object for id " + pBaseId + " not found"), 404);
            }
            return object.getDigitalObjectId();
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Failed to obtain object for baseId " + pBaseId + ".", ex);
            throw new WebApplicationException(401);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<? extends ISimpleDataOrganizationNode> getRootNode(String groupId, Long id, Integer first, Integer results, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting root node id for digital object id {}", objectId);
        NodeId rootId = DataOrganizerFactory.getInstance().getDataOrganizer().getRootNodeId(objectId, viewName);
        LOGGER.debug("Loading node for node id {}", rootId);
        IDataOrganizationNode rootNode = DataOrganizerFactory.getInstance().getDataOrganizer().loadNode(rootId);
        LOGGER.debug("Writing node to output stream");
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(rootNode, false)));
    }

    @Override
    public IEntityWrapper<? extends ISimpleDataOrganizationNode> getRootNodeCount(String groupId, Long id, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting root node id for digital object id {}", objectId);
        NodeId rootId = DataOrganizerFactory.getInstance().getDataOrganizer().getRootNodeId(objectId, viewName);
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper((rootId != null) ? 1 : 0));

    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getChildren(String groupId, Long id, Long nodeId, Integer first, Integer results, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        NodeId nid = new NodeId(objectId, nodeId, inTreeIdVersion, viewName);
        List<? extends IDataOrganizationNode> l = DataOrganizerFactory.getInstance().getDataOrganizer().getChildren(nid, first, results);
        List<DataOrganizationNodeImpl> children = new LinkedList();

        LOGGER.debug("Converting children to serializable format.");
        for (IDataOrganizationNode aChild : l) {
            if (aChild instanceof DataOrganizationNodeImpl) {
                //add child directly
                children.add((DataOrganizationNodeImpl) aChild);
            } else {
                //reload child and copy content
                children.add(DataOrganizationUtils.copyNode(DataOrganizerFactory.getInstance().getDataOrganizer().loadNode(aChild.getTransientNodeId()), false));
            }
        }
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(children));
    }

    @Override
    public IEntityWrapper<? extends IDefaultDataOrganizationNode> getNodeInformation(String groupId, Long id, Long nodeId, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        NodeId nid = new NodeId(objectId, nodeId, inTreeIdVersion, viewName);
        IDataOrganizationNode node = DataOrganizerFactory.getInstance().getDataOrganizer().loadNode(nid);
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(node, false)));
    }

    @Override
    public IEntityWrapper<? extends ISimpleDataOrganizationNode> getChildCount(String groupId, Long id, Long nodeId, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting child count for object with id {} and nodeId {}", objectId, nodeId);
        NodeId nid = new NodeId(objectId, nodeId, inTreeIdVersion, viewName);
        LOGGER.debug("Returning child count for node {}", nid);
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper(DataOrganizerFactory.getInstance().getDataOrganizer().getChildCount(nid).intValue()));
    }

    @Override
    public Response checkService() {
        IMetaDataManager doMdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(PersistenceFacade.getInstance().getPersistenceUnitName());
        doMdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        ServiceStatus status = ServiceStatus.UNKNOWN;
        try {
            LOGGER.debug("Doing service check by obtaining data organization node count");
            Number result = (Number) doMdm.findSingleResult("SELECT COUNT(a) FROM DataOrganizationNode a");
            LOGGER.debug("Service check using node count returned {}", result);
            status = ServiceStatus.OK;
        } catch (Throwable t) {
            LOGGER.error("Obtaining node count returned an error. Service status is set to ERROR", t);
            status = ServiceStatus.ERROR;
        } finally {
            doMdm.close();
        }
        return Response.status(200).entity(new CheckServiceResponse("DataOrganization", status)).build();
    }

    @Override
    public IEntityWrapper<? extends ISimpleDataOrganizationNode> getRootNodeChildren(String groupId, Long id, Integer first, Integer results, String viewName, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting root node id for digital object id {}", objectId);
        NodeId rootId = DataOrganizerFactory.getInstance().getDataOrganizer().getRootNodeId(objectId, viewName);
        LOGGER.debug("Loading node for node id {}", rootId);
        IDataOrganizationNode rootNode = DataOrganizerFactory.getInstance().getDataOrganizer().loadNode(rootId);
        LOGGER.debug("Converting result to DataOrganizationNodeImpl list.");

        List<DataOrganizationNodeImpl> implList = new ArrayList<>();
        for (IDataOrganizationNode node : ((ICollectionNode) rootNode).getChildren()) {
            implList.add(DataOrganizationUtils.copyNode(node, false));
        }
        return RestUtils.transformObject(IMPL_CLASSES, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper(implList));

    }

    @Override
    public DataOrganizationViewWrapper getViews(String groupId, Long id, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting views for object with id {}", objectId);
        return new DataOrganizationViewWrapper(DataOrganizerFactory.getInstance().getDataOrganizer().getViews(objectId));
    }

    @Override
    public DataOrganizationViewWrapper getViewCount(String groupId, Long id, HttpContext hc) {
        DigitalObjectId objectId = getDigitalObjectId(id, groupId, hc);
        LOGGER.debug("Getting view count for object with id {}", objectId);
        return new DataOrganizationViewWrapper(DataOrganizerFactory.getInstance().getDataOrganizer().getViews(objectId).size());

    }

    @Override
    public Response downloadContent(String pGroupId, Long pBaseId, String pViewName, String pPath, HttpContext hc) {
        //check path whether it is a single id...in that case access a node directly
        //if path is a slash-separated string, obtain the according node
        //return node value depending on the media type stored in the request (either XML or octet-stream)
        InputStream is = null;
        try {
            DigitalObjectId objectId = getDigitalObjectId(pBaseId, pGroupId, hc);

            LOGGER.error("Getting data organization node for object {}, view {} and path {}", objectId, pViewName, pPath);
            final IDataOrganizationNode node = new ElementPath(pPath).getNodeForPath(objectId, pViewName);
            if (node instanceof IFileNode) {
                String lfn = ((IFileNode) node).getLogicalFileName().asString();
                URL u = new URL(lfn);
                File fileToDownload = null;
                try {
                    if ("file".equals(u.getProtocol().toLowerCase())) {
                        fileToDownload = new File(u.toURI());
                        is = new FileInputStream(fileToDownload);
                    } else {
                        LOGGER.error("Direct data access is only available for data locally accessible. The accessed entry is available only via '{}'.", u.getProtocol());
                        //not implemented for this protocol type
                        throw new WebApplicationException(501);
                    }
                } catch (URISyntaxException use) {
                    LOGGER.error("Direct data access is not available for LFNs in the form of '{}'", u);
                    //not implemented for this LFN type
                    throw new WebApplicationException(501);
                }

                LOGGER.debug("Trying to determine content type of file {}", fileToDownload);
                Metadata metadata = new Metadata();
                ParseContext context = new ParseContext();
                AutoDetectParser parser = new AutoDetectParser();
                context.set(Parser.class, parser);
                ContentHandler handler = new BodyContentHandler();
                // actually extract the metadata via Tika
                parser.parse(is, handler, metadata, context);
                String contentType = metadata.get("Content-Type");
                LOGGER.debug("Using content type '{}' to stream file to client.", contentType);
                return Response.ok(new FileInputStream(fileToDownload), contentType).header("Content-Disposition",
                        "attachment; filename=" + node.getName()).build();
            } else {
                LOGGER.debug("Establishing piped zip streams.");
                PipedInputStream in = new PipedInputStream();
                final PipedOutputStream out = new PipedOutputStream(in);
                LOGGER.debug("Starting streaming thread.");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            LOGGER.debug("Start zipping operation to piped output stream.");
                            DataOrganizationUtils.zip((ICollectionNode) node, out);
                            LOGGER.debug("Zipping operation finshed.");
                        } catch (IOException ex) {
                            LOGGER.error("Failed to zip node content to output stream.", ex);
                        } finally {
                            if (out != null) {
                                try {
                                    out.flush();
                                    out.close();
                                } catch (IOException ex) {
                                    //ignore
                                }
                            }
                        }
                    }
                }
                ).start();
                String resultName = objectId + "_" + pViewName + ((node.getName() != null) ? "_" + node.getName() : "");
                LOGGER.debug("Returning response file named '{}' in stream linked to piped zip stream.", resultName);

                return Response.ok(in, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
                        "attachment; filename=" + resultName + ".zip").build();
            }
        } catch (IOException | SAXException | TikaException ex) {
            LOGGER.error("Failed to get view count.", ex);
            throw new WebApplicationException(500);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
