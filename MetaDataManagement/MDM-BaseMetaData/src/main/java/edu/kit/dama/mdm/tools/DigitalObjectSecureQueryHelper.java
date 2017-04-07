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
package edu.kit.dama.mdm.tools;

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.util.AuthorizationUtil;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.mdm.core.tools.AbstractSecureQueryHelper;
import java.util.List;

/**
 *
 * @author jejkal
 */
public final class DigitalObjectSecureQueryHelper extends AbstractSecureQueryHelper<DigitalObject> {

    /**
     * Get the number of readable digital objects in the provided study.
     *
     * @param pStudy The study where the objects belong to.
     * @param pDefailedQuery A detailed query string or null if no detailed
     * query is needed.
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context. Attention: User context needed.
     * System context is not applicable here!
     *
     * @return The number of digital objects.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any digital object in the provided
     * study.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public Number getObjectCountInStudy(Study pStudy, String pDefailedQuery, IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pDefailedQuery != null) {
            return getReadableResourceCount(pMetaDataManager,
                    pDefailedQuery
                    + (AuthorizationUtil.isAdminContext(pContext)
                    ? " AND o.investigation.study.studyId='" + pStudy.getStudyId() + "'"
                    : " AND o.visible='TRUE' AND o.investigation.study.studyId='" + pStudy.getStudyId() + "'"),
                    pContext);
        }
        return getReadableResourceCount(pMetaDataManager,
                AuthorizationUtil.isAdminContext(pContext)
                ? "o.investigation.study.studyId='" + pStudy.getStudyId() + "'"
                : "o.visible='TRUE' AND o.investigation.study.studyId='" + pStudy.getStudyId() + "'",
                pContext);
    }

    /**
     * Get a list of readable digital objects in the provided investigation or
     * study. If pInvestigation is 'null', all objects in pStudy are returned.
     * Otherwise, max. pMaxResult objects of pInvestigation will be in the
     * result list.
     *
     * @param pStudy The study where the objects belong to.
     * @param pInvestigation The investigation where the objects belong to.
     * @param pDetailedQuery A detailed query string or null if no detailed
     * query is needed.
     * @param pFirstResult The index of the first result.
     * @param pMaxResults The max. number of results.
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context.
     *
     * @return The number of digital objects.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any digital object in the provided
     * investigation or study.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public List<DigitalObject> getObjectsByParent(Study pStudy, Investigation pInvestigation, String pDetailedQuery, IMetaDataManager pMetaDataManager, int pFirstResult, int pMaxResults, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pInvestigation != null) {
            return getObjectsInInvestigation(pInvestigation, pDetailedQuery, pMetaDataManager, pFirstResult, pMaxResults, pContext);
        } else if (pStudy != null) {
            return getObjectsInStudy(pStudy, pDetailedQuery, pMetaDataManager, pFirstResult, pMaxResults, pContext);
        }

        if (pDetailedQuery != null) {
            return getReadableResources(pMetaDataManager,
                    pDetailedQuery
                    + (AuthorizationUtil.isAdminContext(pContext)
                    ? ""
                    : " AND o.visible='TRUE'"),
                    ORDER.ASC,
                    pFirstResult,
                    pMaxResults,
                    pContext
            );
        }
        return getReadableResources(pMetaDataManager,
                AuthorizationUtil.isAdminContext(pContext)
                ? null
                : "o.visible='TRUE'", ORDER.ASC, pFirstResult, pMaxResults, pContext);
    }

    /**
     * Get the number of readable digital objects in the provided investigation
     * or study. If pInvestigation is 'null', all objects in pStudy are
     * returned. Otherwise, max. pMaxResult objects of pInvestigation will be in
     * the result list.
     *
     * @param pStudy The study where the objects belong to.
     * @param pInvestigation The investigation where the objects belong to.
     * @param pDetailedQuery A detailed query string or null if no detailed
     * query is needed.
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context.
     *
     * @return The number of digital objects.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any digital object in the provided
     * investigation or study.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public Number getObjectCountByParent(Study pStudy, Investigation pInvestigation, String pDetailedQuery, IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pInvestigation != null) {
            return getObjectCountInInvestigation(pInvestigation, pDetailedQuery, pMetaDataManager, pContext);
        } else if (pStudy != null) {
            return getObjectCountInStudy(pStudy, pDetailedQuery, pMetaDataManager, pContext);
        }
        return getReadableResourceCount(pMetaDataManager,
                AuthorizationUtil.isAdminContext(pContext)
                ? pDetailedQuery
                : (pDetailedQuery != null) ? pDetailedQuery + " AND o.visible='TRUE'" : "o.visible='TRUE'",
                pContext);
    }

    /**
     * Get a list of readable digital objects in the provided study.
     *
     * @param pStudy The study where the objects belong to.
     * @param pDetailedQuery A detailed query string or null if no detailed
     * query is needed.
     * @param pFirstResult The index of the first result.
     * @param pMaxResults The max. number of results.
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context.
     *
     * @return The number of digital objects.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any digital object in the provided
     * study.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public List<DigitalObject> getObjectsInStudy(Study pStudy, String pDetailedQuery, IMetaDataManager pMetaDataManager, int pFirstResult, int pMaxResults, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pDetailedQuery != null) {
            return getReadableResources(pMetaDataManager,
                    pDetailedQuery
                    + (AuthorizationUtil.isAdminContext(pContext)
                    ? " AND o.investigation.study.studyId='" + pStudy.getStudyId() + "'"
                    : " AND o.visible='TRUE' AND o.investigation.study.studyId='" + pStudy.getStudyId() + "'"),
                    ORDER.ASC,
                    pFirstResult,
                    pMaxResults,
                    pContext);
        }
        return getReadableResources(pMetaDataManager,
                AuthorizationUtil.isAdminContext(pContext)
                ? "o.investigation.study.studyId='" + pStudy.getStudyId() + "'"
                : "o.visible='TRUE' AND o.investigation.study.studyId='" + pStudy.getStudyId() + "'",
                ORDER.ASC,
                pFirstResult,
                pMaxResults,
                pContext);
    }

    /**
     * Get the number of readable digital objects in the provided investigation.
     *
     * @param pInvestigation The investigation where the objects belong to.
     * @param pDetailedQuery A detailed query string or null if no detailed
     * query is needed.
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context.
     *
     * @return The number of digital objects.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any digital object in the provided
     * investigation.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public Number getObjectCountInInvestigation(Investigation pInvestigation, String pDetailedQuery, IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        if (pDetailedQuery != null) {
            return getReadableResourceCount(pMetaDataManager,
                    pDetailedQuery
                    + (AuthorizationUtil.isAdminContext(pContext)
                    ? " AND o.investigation.investigationId='" + pInvestigation.getInvestigationId() + "'"
                    : " AND o.visible='TRUE' AND o.investigation.investigationId='" + pInvestigation.getInvestigationId() + "'"),
                    pContext);
        }
        return getReadableResourceCount(pMetaDataManager,
                AuthorizationUtil.isAdminContext(pContext)
                ? "o.investigation.investigationId='" + pInvestigation.getInvestigationId() + "'"
                : "o.visible='TRUE' AND o.investigation.investigationId='" + pInvestigation.getInvestigationId() + "'",
                pContext);
    }

    /**
     * Get a list number of readable digital objects in the provided
     * investigation.
     *
     * @param pInvestigation The investigation where the objects belong to.
     * @param pDetailedQuery A detailed query string or null if no detailed
     * query is needed.
     * @param pFirstResult The index of the first result.
     * @param pMaxResults The max. number of results.
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context.
     *
     * @return A list of digital objects.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any digital object in the provided
     * investigation.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public List<DigitalObject> getObjectsInInvestigation(Investigation pInvestigation, String pDetailedQuery, IMetaDataManager pMetaDataManager, int pFirstResult, int pMaxResults, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        try {
            pMetaDataManager.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
            if (pDetailedQuery != null) {
                return getReadableResources(pMetaDataManager,
                        pDetailedQuery
                        + (AuthorizationUtil.isAdminContext(pContext)
                        ? " AND o.investigation.investigationId='" + pInvestigation.getInvestigationId() + "'"
                        : " AND o.visible='TRUE' AND o.investigation.investigationId='" + pInvestigation.getInvestigationId() + "'"),
                        ORDER.ASC,
                        pFirstResult,
                        pMaxResults,
                        pContext);
            }

            return getReadableResources(pMetaDataManager,
                    AuthorizationUtil.isAdminContext(pContext)
                    ? "o.investigation.investigationId='" + pInvestigation.getInvestigationId() + "'"
                    : "o.visible='TRUE' AND o.investigation.investigationId='" + pInvestigation.getInvestigationId() + "'",
                    ORDER.ASC,
                    pFirstResult,
                    pMaxResults,
                    pContext);
        } finally {
            pMetaDataManager.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
        }
    }

    /**
     * Get a digital object by its identifier.
     *
     * @param pDigitalObjectId The digital object id.
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context.
     *
     * @return The digital object.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any digital object.
     * @throws EntityNotFoundException If no object was found during the query
     * for securable resources.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public DigitalObject getObjectByIdentifier(String pDigitalObjectId, IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        try {
            pMetaDataManager.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
            DigitalObject result = getReadableResource(pMetaDataManager, null, pDigitalObjectId, pContext);
            if (result == null) {
                throw new EntityNotFoundException("No entity found/accessible with object id " + pDigitalObjectId);
            } else {
                return result;
            }
        } finally {
            pMetaDataManager.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
        }
    }

    /**
     * Perform a quick check whether a digital object for the provided
     * identifier exists. The check includes a query for the according securable
     * resource followed by a query for count of digital objects for the
     * provided identifier. If this count is not 0, 'TRUE' is returned.
     *
     * @param pDigitalObjectId The digital object id.
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context.
     *
     * @return TRUE if the object exists, FALSE otherwise.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any digital object.
     * @throws EntityNotFoundException If no object was found during the query
     * for securable resources.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public boolean objectByIdentifierExists(String pDigitalObjectId, IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        return getReadableResourceCount(pMetaDataManager,
                (AuthorizationUtil.isAdminContext(pContext)
                ? "o.digitalObjectIdentifier='" + pDigitalObjectId + "'"
                : "o.visible='TRUE' AND o.digitalObjectIdentifier='" + pDigitalObjectId + "'"),
                pContext) > 0;
    }

    /**
     * Get a digital object by its base id.
     *
     * @param pBaseId The base id.
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context.
     *
     * @return The digital object.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any digital object.
     * @throws EntityNotFoundException If no object was found.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public DigitalObject getObjectByBaseId(Long pBaseId, IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException, EntityNotFoundException {
        try {
            pMetaDataManager.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
            List<DigitalObject> result = getReadableResources(pMetaDataManager,
                    AuthorizationUtil.isAdminContext(pContext)
                    ? "o.baseId='" + pBaseId + "'"
                    : "o.visible='TRUE' AND o.baseId='" + pBaseId + "'",
                    ORDER.ASC,
                    0,
                    1,
                    pContext);
            if (result.isEmpty()) {
                throw new EntityNotFoundException("No entity found/accessible with base id " + pBaseId);
            } else {
                return result.get(0);
            }
        } finally {
            pMetaDataManager.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
        }
    }
}
