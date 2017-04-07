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
package edu.kit.dama.mdm.tools;

import edu.kit.dama.authorization.annotations.Context;
import edu.kit.dama.authorization.annotations.SecuredMethod;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.util.AuthorizationUtil;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.mdm.core.tools.AbstractSecureQueryHelper;
import java.util.List;

/**
 *
 * @author jejkal
 */
public final class StudySecureQueryHelper extends AbstractSecureQueryHelper<Study> {

    /**
     * Get the number of readable studies.
     *
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pContext The security context.
     *
     * @return The number of studies.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any study.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public Number getStudyCount(IMetaDataManager pMetaDataManager, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        return getReadableResourceCount(pMetaDataManager, AuthorizationUtil.isAdminContext(pContext) ? null : "o.visible='TRUE'", pContext);
    }

    /**
     * Get a list of readable studies.
     *
     * @param pMetaDataManager The MetaDataManager used to perform the query.
     * @param pFirstIdx The index of the first result.
     * @param pMaxEntries The max. number of results.
     * @param pContext The security context.
     *
     * @return A list of studies.
     *
     * @throws UnauthorizedAccessAttemptException If the provided context is not
     * allowed to access this method or any study.
     */
    @SecuredMethod(roleRequired = Role.GUEST)
    public List<Study> getReadableStudies(IMetaDataManager pMetaDataManager, int pFirstIdx, int pMaxEntries, @Context IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        pMetaDataManager.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Study.default");
        try {
            return getReadableResources(pMetaDataManager, AuthorizationUtil.isAdminContext(pContext) ? null : "o.visible='TRUE'", ORDER.ASC, pFirstIdx, pMaxEntries, pContext);
        } finally {
            pMetaDataManager.removeProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH);
        }
    }
}
