/*
 * Copyright 2015 Karlsruhe Institute of Technology.
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
package edu.kit.dama.util;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.SecurableResourceId;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.authorization.services.administration.ResourceServiceLocal;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.admin.UserGroup;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import edu.kit.dama.mdm.tools.TransitionQueryHelper;
import org.slf4j.LoggerFactory;

/**
 * BaseMetaDataCleaningHelper implements methods for cleaning up BaseMetaData
 * entities. This helper is not intended to be used on a regular basis but to
 * cleanup test instances or remove entities during curation workflows.
 *
 * @author mf6319
 */
public final class BaseMetaDataCleaningHelper {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BaseMetaDataCleaningHelper.class);

  /**
   * Delete pInvestigation from the database. The removal consists of the
   * following operations:
   *
   * <ul>
   * <li>Remove all objects.</li>
   * <li>Remove the investigation itself.</li>
   * </ul>
   *
   * @param pInvestigation The investigation to remove.
   * @param pContext The context used to authorize the operation.
   *
   * @throws UnauthorizedAccessAttemptException if the operation could not be
   * authorized using pContext.
   */
  public void deleteInvestigation(Investigation pInvestigation, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Deleting investigation with identifier {}", pInvestigation.getUniqueIdentifier());
    LOGGER.debug(" - Removing digital objects.");

    for (DigitalObject o : pInvestigation.getDataSets()) {
      LOGGER.debug("Deleting digital object with id {} from investigation with id {}", o.getBaseId(), pInvestigation.getInvestigationId());
      deleteDigitalObject(o, pContext);
    }
    LOGGER.debug(" - Removing investigation.");
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pContext);
    try {
      mdm.remove(pInvestigation);
      LOGGER.debug("Investigation with identifier {} removed.", pInvestigation.getUniqueIdentifier());
    } catch (EntityNotFoundException ex) {
      LOGGER.warn("Investigation with identifier " + pInvestigation.getUniqueIdentifier() + " not found in database. Removal skipped.", ex);
    } finally {
      mdm.close();
    }
  }

  /**
   * Delete pStudy from the database. The removal consists of the following
   * operations:
   *
   * <ul>
   * <li>Remove all investigations.</li>
   * <li>Remove the study itself.</li>
   * </ul>
   *
   * @param pStudy The study to remove.
   * @param pContext The context used to authorize the operation.
   *
   * @throws UnauthorizedAccessAttemptException if the operation could not be
   * authorized using pContext.
   */
  public void deleteStudy(Study pStudy, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Deleting study with identifier {}", pStudy.getUniqueIdentifier());
    LOGGER.debug(" - Removing investigations.");

    for (Investigation investigation : pStudy.getInvestigations()) {
      LOGGER.debug("Deleting investigation with id {} from study with id {}", investigation.getInvestigationId(), pStudy.getStudyId());
      deleteInvestigation(investigation, pContext);
    }
    LOGGER.debug(" - Removing study.");
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pContext);
    try {
      mdm.remove(pStudy);
      LOGGER.debug("Study with identifier {} removed.", pStudy.getUniqueIdentifier());
    } catch (EntityNotFoundException ex) {
      LOGGER.warn("Study with identifier " + pStudy.getUniqueIdentifier() + " not found in database. Removal skipped.", ex);
    } finally {
      mdm.close();
    }
  }

  /**
   * Delete pObject from the database. The removal consists of the following
   * operations:
   *
   * <ul>
   * <li>Remove all shares (revoke grants and delete references).</li>
   * <li>Remove all transitions associated with pObject</li>
   * <li>Remove the object itself.</li>
   * </ul>
   *
   * @param pObject The digital object to remove.
   * @param pContext The context used to authorize the operation.
   *
   * @throws UnauthorizedAccessAttemptException if the operation could not be
   * authorized using pContext.
   */
  public void deleteDigitalObject(DigitalObject pObject, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Deleting object with identifier {}", pObject.getDigitalObjectIdentifier());
    LOGGER.debug(" - Revoking shares.");
    revokeAllShares(pObject.getSecurableResourceId(), pContext);
    LOGGER.debug(" - Removing transitions.");
    TransitionQueryHelper.removeTransitionsByObject(pObject, pContext);
    LOGGER.debug(" - Removing data organization.");
    PersistenceFacade.getInstance().deleteAllNodesForTree(pObject.getDigitalObjectId());
    LOGGER.debug(" - Removing object.");
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pContext);
    try {
      mdm.remove(pObject);
      LOGGER.debug("Object with identifier {} removed.", pObject.getDigitalObjectIdentifier());
    } catch (EntityNotFoundException ex) {
      LOGGER.warn("DigitalObject with identifier " + pObject.getDigitalObjectIdentifier() + " not found in database. Removal skipped.", ex);
    } finally {
      mdm.close();
    }
  }

  /**
   * Revoke all shares for the resource pResourceId and finally removed the
   * resource id. This includes grants and resource references.
   *
   * @param pResourceId The resource Id for which the shares should be removed.
   * @param pContext The context used to authorize the operation.
   *
   * @throws UnauthorizedAccessAttemptException if the operation could not be
   * authorized using pContext.
   */
  private static void revokeAllShares(SecurableResourceId pResourceId, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Revoking all grants.");
    try {
      ResourceServiceLocal.getSingleton().revokeAllAndDisallowGrants(pResourceId, pContext);
    } catch (EntityNotFoundException ex) {
      LOGGER.debug("No resource grants found for resource {}", pResourceId);
    }
    LOGGER.debug("Removing securable resource id.");
    try {
      ResourceServiceLocal.getSingleton().remove(pResourceId, pContext);
    } catch (EntityNotFoundException ex) {
      LOGGER.debug("No securable resource Id found for resource {}", pResourceId);
    }
  }

  /**
   * Delete the group with the provided group id. The deletion covers removing
   * the group metadata from the UserGroup table, the revokation of all
   * memberships and associated resources and the removal of the group from the
   * Groups table itself.
   *
   * @param pGroup The group Id of the group to remove.
   * @param pContext The context used to authorize the operation.
   *
   * @throws UnauthorizedAccessAttemptException if the operation could not be
   * authorized using pContext.
   */
  public void deleteGroup(GroupId pGroup, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
    LOGGER.debug("Deleting group with identifier {}", pGroup.getStringRepresentation());
    LOGGER.debug(" - Removing UserGroup metadata.");
    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
    mdm.setAuthorizationContext(pContext);
    try {
      LOGGER.debug(" - Querying for group metadata for id {}", pGroup);
      UserGroup group = mdm.findSingleResult("SELECT g FROM UserGroup g WHERE g.groupId='" + pGroup.getStringRepresentation() + "'", UserGroup.class);
      if (group != null) {
        LOGGER.debug(" - Removing group metadata.");
        mdm.remove(group);
      } else {
        LOGGER.info(" - Group metadata is null. Skip removal.");
      }
    } catch (EntityNotFoundException ex) {
      LOGGER.warn("Group with identifier " + pGroup.getStringRepresentation() + " not found in database. Removal skipped.", ex);
    } finally {
      mdm.close();
    }

    LOGGER.debug(" - Removing group entry including all memberships.");
    try {
      GroupServiceLocal.getSingleton().remove(pGroup, pContext);
    } catch (EntityNotFoundException ex) {
      LOGGER.warn("No group entry found. Skipping removal.", ex);
    }
    LOGGER.debug("Group successfully removed.");
  }

  public static void main(String[] args) throws Exception {
    //new BaseMetaDataCleaningHelper().deleteGroup(new GroupId("80773"), AuthorizationContext.factorySystemContext());
    PersistenceFacade.getInstance().deleteAllNodesForTree(new DigitalObjectId("4b82aa47-31dc-42e3-bcbb-d100049fafae"));
  }
}
