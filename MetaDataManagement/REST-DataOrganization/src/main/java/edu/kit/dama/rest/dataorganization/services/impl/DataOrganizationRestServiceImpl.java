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
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizerImpl;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationNodeWrapper;
import edu.kit.dama.rest.dataorganization.types.DataOrganizationViewWrapper;
import edu.kit.dama.rest.dataorganization.services.interfaces.IDataOrganizationRestService;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.util.RestUtils;
import static edu.kit.dama.rest.util.RestUtils.createObjectGraphStream;
import edu.kit.dama.staging.entities.AttributeImpl;
import edu.kit.dama.staging.entities.CollectionNodeImpl;
import edu.kit.dama.staging.entities.DataOrganizationNodeImpl;
import edu.kit.dama.staging.entities.FileNodeImpl;
import edu.kit.dama.staging.entities.LFNImpl;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.util.Constants;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  @Override
  public StreamingOutput getRootNode(String groupId, Long id, Integer first, Integer results, String viewName, HttpContext hc) {
    try {
      IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
      LOGGER.debug("Getting root for id {} with context {}", new Object[]{id, ctx});
      IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
      LOGGER.debug("Obtaining digital object for id {}", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        throw new WebApplicationException(new Exception("Object for id " + id + " not found"), 404);
      }
      DataOrganizerImpl dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();
      LOGGER.debug("Getting root node id for digital object id {}", object.getDigitalObjectId());
      NodeId rootId = dataOrganizer.getRootNodeId(object.getDigitalObjectId(), viewName);
      LOGGER.debug("Loading node for node id {}", rootId);
      IDataOrganizationNode rootNode = dataOrganizer.loadNode(rootId);
      LOGGER.debug("Writing node to output stream");
      return createObjectGraphStream(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(rootNode, false)));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get root node.", ex);
      throw new WebApplicationException(401);
    }
  }

  @Override
  public StreamingOutput getRootNodeCount(String groupId, Long id, String viewName, HttpContext hc) {
    try {
      IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
      LOGGER.debug("Getting root for id {} with context {}", new Object[]{id, ctx});
      IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
      LOGGER.debug("Obtaining digital object for id {}", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        throw new WebApplicationException(new Exception("Object for id " + id + " not found"), 404);
      }
      DataOrganizerImpl dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();

      LOGGER.debug("Getting root node id for digital object id {}", object.getDigitalObjectId());
      NodeId rootId = dataOrganizer.getRootNodeId(object.getDigitalObjectId(), viewName);

      return createObjectGraphStream(IMPL_CLASSES, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper((rootId != null) ? 1 : 0));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get node count.", ex);
      throw new WebApplicationException(401);
    }
  }

  @Override
  public StreamingOutput getChildren(String groupId, Long id, Long nodeId, Integer first, Integer results, String viewName, HttpContext hc) {
    try {
      IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
      LOGGER.debug("Getting root for id {} with context {}", new Object[]{id, ctx});
      IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
      LOGGER.debug("Obtaining digital object for id {}", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        throw new WebApplicationException(new Exception("Object for id " + id + " not found"), 404);
      }

      DataOrganizerImpl dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();
      NodeId nid = new NodeId(object.getDigitalObjectId(), nodeId, inTreeIdVersion, viewName);
      List<? extends IDataOrganizationNode> l = dataOrganizer.getChildren(nid, first, results);
      List<DataOrganizationNodeImpl> children = new LinkedList();

      LOGGER.debug("Converting children to serializable format.");
      for (IDataOrganizationNode aChild : l) {
        if (aChild instanceof DataOrganizationNodeImpl) {
          //add child directly
          children.add((DataOrganizationNodeImpl) aChild);
        } else {
          //reload child and copy content
          children.add(DataOrganizationUtils.copyNode(dataOrganizer.loadNode(aChild.getTransientNodeId()), false));
        }
      }

      return createObjectGraphStream(IMPL_CLASSES, Constants.REST_SIMPLE_OBJECT_GRAPH, new DataOrganizationNodeWrapper(children));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get children.", ex);
      throw new WebApplicationException(401);
    }
  }

  @Override
  public StreamingOutput getNodeInformation(String groupId, Long id, Long nodeId, String viewName, HttpContext hc) {
    try {
      IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
      LOGGER.debug("Getting root for id {} with context {}", new Object[]{id, ctx});
      IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
      LOGGER.debug("Obtaining digital object for id {}", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        throw new WebApplicationException(new Exception("Object for id " + id + " not found"), 404);
      }
      DataOrganizerImpl dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();

      NodeId nid = new NodeId(object.getDigitalObjectId(), nodeId, inTreeIdVersion, viewName);
      IDataOrganizationNode node = dataOrganizer.loadNode(nid);

      return createObjectGraphStream(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(DataOrganizationUtils.copyNode(node, false)));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get node information.", ex);
      throw new WebApplicationException(401);
    }
  }

  @Override
  public StreamingOutput getChildCount(String groupId, Long id, Long nodeId, String viewName, HttpContext hc) {
    try {
      IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
      LOGGER.debug("Getting root for id {} with context {}", new Object[]{id, ctx});
      IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
      LOGGER.debug("Obtaining digital object for id {}", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        throw new WebApplicationException(new Exception("Object for id " + id + " not found"), 404);
      }
      DataOrganizerImpl dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();
      NodeId nid = new NodeId(object.getDigitalObjectId(), nodeId, inTreeIdVersion, viewName);
      //return max. 100
      int childCount = (int) dataOrganizer.getChildCount(nid).longValue();

      return createObjectGraphStream(IMPL_CLASSES, Constants.REST_DEFAULT_OBJECT_GRAPH, new DataOrganizationNodeWrapper(childCount));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get child count.", ex);
      throw new WebApplicationException(401);
    }
  }

  @Override
  public StreamingOutput getTransformOutput(String groupId, Long id, Long nodeId, String viewName, HttpContext hc) {
    LOGGER.warn("getTransformOutput() is currently not supported.");
    throw new WebApplicationException(405);
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
  public StreamingOutput getRootNodeChildren(String groupId, Long id, Integer first, Integer results, String viewName, HttpContext hc) {
    return getRootNode(groupId, id, first, results, viewName, hc);
  }

  @Override
  public StreamingOutput getViews(String groupId, Long id, HttpContext hc) {
    try {
      IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
      LOGGER.debug("Getting root for id {} with context {}", new Object[]{id, ctx});
      IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
      LOGGER.debug("Obtaining digital object for id {}", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        throw new WebApplicationException(new Exception("Object for id " + id + " not found"), 404);
      }
      DataOrganizerImpl dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();
      List<String> lv = dataOrganizer.getViews(object.getDigitalObjectId());

      return createObjectGraphStream(DataOrganizationViewWrapper.class,
              Constants.REST_DEFAULT_OBJECT_GRAPH,
              new DataOrganizationViewWrapper(lv));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get views.", ex);
      throw new WebApplicationException(401);
    }
  }

  @Override
  public StreamingOutput getViewCount(String groupId, Long id, HttpContext hc) {
    try {
      IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
      LOGGER.debug("Getting root for id {} with context {}", new Object[]{id, ctx});
      IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
      LOGGER.debug("Obtaining digital object for id {}", id);
      DigitalObject object = mdm.find(DigitalObject.class, id);
      if (object == null) {
        throw new WebApplicationException(new Exception("Object for id " + id + " not found"), 404);
      }
      DataOrganizerImpl dataOrganizer = DataOrganizerFactory.getInstance().getDataOrganizer();
      List<String> lv = dataOrganizer.getViews(object.getDigitalObjectId());

      return createObjectGraphStream(DataOrganizationViewWrapper.class,
              Constants.REST_DEFAULT_OBJECT_GRAPH,
              new DataOrganizationViewWrapper(lv.size()));
    } catch (UnauthorizedAccessAttemptException ex) {
      LOGGER.error("Failed to get view count.", ex);
      throw new WebApplicationException(401);
    }
  }

}
