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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.dama.mdm.dataorganization.impl.jpa;

import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import edu.kit.dama.mdm.dataorganization.service.exception.DataOrganizationError;

/**
 * Utility functions for the JPA implementation.
 *
 * @author pasic
 */
public final class JPAImplUtil {

    /**
     * Hidden constructor.
     */
    private JPAImplUtil() {
    }

    /**
     * Converts some IDataOrganizationNode to JPA data organization node.
     *
     * @param pDataOrganizationNode the node to be converted.
     *
     * @return a corresponding JPA data organization node.
     */
    public static DataOrganizationNode convertDataOrganizationNode(IDataOrganizationNode pDataOrganizationNode) {
        DataOrganizationNode newDon = null;
        if (null == pDataOrganizationNode) {
            return null;
        }
        if (pDataOrganizationNode instanceof DataOrganizationNode) {
            if (pDataOrganizationNode instanceof FileTree) {
                newDon = new CollectionNode((ICollectionNode) pDataOrganizationNode);
            } else {
                newDon = (DataOrganizationNode) pDataOrganizationNode;
            }
        } else if (pDataOrganizationNode instanceof ICollectionNode) {
            newDon = new CollectionNode((ICollectionNode) pDataOrganizationNode);
        } else if (pDataOrganizationNode instanceof IFileNode) {
            newDon = new FileNode((IFileNode) pDataOrganizationNode);
        }
        if (null == newDon) {
            throw new DataOrganizationError("node:IDataOrganizationNode should be either"
                    + " instance of IFileNode or instance of ICollectionNode");
        }
        return newDon;
    }

    public static int renameView(DigitalObjectId pObjectId, String pOldName, String pNewName, IAuthorizationContext pContext) throws UnauthorizedAccessAttemptException {
        String pu = PersistenceFacade.getInstance().getPersistenceUnitName();
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(pu);
        mdm.setAuthorizationContext(pContext);
        try {
            System.out.println("Update Attrib");
            //  int attribChanges = mdm.performUpdate("UPDATE Attribute a SET a.viewName=?1 WHERE a.annotatedNode.digitalObjectIDStr=?2 AND a.annotatedNode.viewName=?3", new Object[]{pNewName, pObjectId.getStringRepresentation(), pOldName});
            //   System.out.println("CHANGE " + attribChanges);
            System.out.println("UPDATE NODE");
            return mdm.performUpdate("UPDATE DataOrganizationNode n SET n.viewName=?1 WHERE  n.digitalObjectIDStr=?2 AND n.viewName=?3", new Object[]{pNewName, pObjectId.getStringRepresentation(), pOldName});

        } finally {
            mdm.close();
        }
    }
}
