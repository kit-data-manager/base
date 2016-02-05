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
package edu.kit.dama.mdm.dataorganization.service.core;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.impl.jpa.FileTree;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.util.Constants;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides access to the data organization service (which is specified by the {@link
 * IDataOrganizationService} interface) via singleton.
 *
 * @author pasic
 */
public final class DataOrganizationServiceLocal implements
        IDataOrganizationService<IAuthorizationContext> {

  private static DataOrganizationServiceLocal singleton = null;

  /**
   * Returns the DataOrganizationServiceLocal instance
   *
   * @return The singleton instance of the DataOrganizationServiceLocal
   */
  public static synchronized DataOrganizationServiceLocal getSingleton() {
    if (singleton == null) {
      singleton = new DataOrganizationServiceLocal();
    }
    return singleton;
  }

  /**
   * Hidden constructor.
   */
  protected DataOrganizationServiceLocal() {
  }

  @Override
  public IFileTree loadFileTree(DigitalObjectId digitalObjectId,
          String viewName, IAuthorizationContext ctx)
          throws EntityNotFoundException {
    FileTree result = DataOrganizerFactory.getInstance().getDataOrganizer().
            loadFileTree(digitalObjectId, viewName);
    if (result == null) {
      //no file tree found for the provided object ID
      throw new EntityNotFoundException(
              "No data organization tree was found for digital object '"
              + digitalObjectId + "'");
    }
    return result;
  }

  @Override
  public IFileTree loadFileTree(DigitalObjectId digitalObjectId,
          IAuthorizationContext ctx)
          throws EntityNotFoundException {
    return loadFileTree(digitalObjectId, Constants.DEFAULT_VIEW, ctx);
  }

  @Override
  public void createFileTree(IFileTree fileTree, IAuthorizationContext ctx)
          throws EntityExistsException {
    DataOrganizerFactory.getInstance().getDataOrganizer().createFileTree(
            fileTree);
  }

  @Override
  public List<? extends IDataOrganizationNode> getChildren(NodeId nodeId,
          int firstResult, int maxResult, IAuthorizationContext ctx)
          throws InvalidNodeIdException {
    return DataOrganizerFactory.getInstance().getDataOrganizer().
            getChildren(nodeId, firstResult, maxResult);
  }

  @Override
  public IFileTree loadSubTree(NodeId relativeRoot, int relativeDepth,
          IAuthorizationContext ctx)
          throws InvalidNodeIdException {
    return DataOrganizerFactory.getInstance().getDataOrganizer().
            loadSubTree(relativeRoot, relativeDepth);
  }

  @Override
  public NodeId getRootNodeId(DigitalObjectId digitalObjectId,
          IAuthorizationContext ctx)
          throws EntityNotFoundException {
    return getRootNodeId(digitalObjectId, Constants.DEFAULT_VIEW, ctx);
  }

  @Override
  public NodeId getRootNodeId(DigitalObjectId digitalObjectId, String viewName,
          IAuthorizationContext ctx)
          throws EntityNotFoundException {
    return DataOrganizerFactory.getInstance().getDataOrganizer().getRootNodeId(digitalObjectId, viewName);
  }

  @Override
  public void updateNodeData(NodeId nodeId, IDataOrganizationNode newData,
          IAuthorizationContext ctx)
          throws InvalidNodeIdException {
    DataOrganizerFactory.getInstance().getDataOrganizer().updateNodeData(nodeId, newData);
  }

  @Override
  public IDataOrganizationNode loadNode(NodeId nodeId,
          IAuthorizationContext ctx)
          throws InvalidNodeIdException {
    return DataOrganizerFactory.getInstance().getDataOrganizer().loadNode(
            nodeId);
  }

  /**
   * Returns all associated NodeIds for the provided digital object id. This
   * method might be used later for high-performance lazy loading.
   *
   * @param pId The digital object id.
   * @param pMetaDataManager The meta data manager used to perform the query.
   * Keep in mind, that you have to provide a meta data manager for the
   * persistence unit used by the data organization. Otherwise the query will
   * fail.
   *
   * @return A list of associated NodeIds.
   *
   * @throws UnauthorizedAccessAttemptException if anything goes wrong.
   */
  public List<NodeId> getNodeIds(DigitalObjectId pId,
          IMetaDataManager pMetaDataManager)
          throws UnauthorizedAccessAttemptException {
    List<Object> objects = pMetaDataManager.findResultList(
            "SELECT n.stepNoArrived FROM DataOrganizationNode n WHERE n.digitalObjectIDStr='"
            + pId.getStringRepresentation() + "'");
    List<NodeId> ids = new LinkedList<NodeId>();

    for (Object o : objects) {
      ids.add(new NodeId(pId, (Long) o, 1));
    }
    return ids;
  }
}
