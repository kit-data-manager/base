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
package edu.kit.dama.mdm.dataorganization.test;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.jpa.Attribute;
import edu.kit.dama.mdm.dataorganization.impl.jpa.CollectionNode;
import edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.impl.jpa.FileTree;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import java.util.List;
import javax.persistence.EntityManager;

/**
 *
 * @author pasic
 */
public class TestUtil {

    static void clearDB() {
        EntityManager em = PersistenceFacade.getInstance().
                getEntityManagerFactory().createEntityManager();
        em.getTransaction().begin();
        List<DataOrganizationNode> nodes = em.createQuery(
                "SELECT m FROM DataOrganizationNode m",
                DataOrganizationNode.class).getResultList();
        for (DataOrganizationNode node : nodes) {
            em.remove(node);
        }

        em.flush();
        em.getTransaction().commit();
        em.close();
    }

    public static IFileTree createBasicTestTree() {
        IFileTree tree
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.FileTree();
        DigitalObjectId digitalObjectID = new DigitalObjectId("Dummy");
        tree.setDigitalObjectId(digitalObjectID);
        ICollectionNode cnp;
        ICollectionNode cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("child 1");
        cnp = tree.getRootNode();
        tree.getRootNode().setName("root");
        cnp.addChild(cnc);
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        IAttribute attr
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.Attribute();
        attr.setKey("dummy");
        attr.setValue("attribute");
        cnc.addAttribute(attr);
        cnc.setName("child 2");
        cnp.addChild(cnc);

        cnp = cnc;
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("cnc 2.1");
        cnp.addChild(cnc);
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("cnc 2.2");
        cnp.addChild(cnc);
        ICollectionNode cnp22 = cnc;
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("cnc 2.3");
        cnp.addChild(cnc);

        cnp = cnc;
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("cnc 2.3.1");
        cnp.addChild(cnc);
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("cnc 2.3.2");
        cnp.addChild(cnc);
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("cnc 2.3.3");
        cnp.addChild(cnc);

        cnp = cnp22;
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("cnc 2.2.1");
        cnp.addChild(cnc);
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("cnc 2.2.2");
        cnp.addChild(cnc);
        cnc
                = new edu.kit.dama.mdm.dataorganization.entity.impl.client.CollectionNode();
        cnc.setName("cnc 2.2.3");
        cnp.addChild(cnc);

        tree.setViewName("default");

        return tree;
    }

    public static FileTree createBasicJPATestTree() {
        FileTree tree = new FileTree();
        DigitalObjectId digitalObjectID = new DigitalObjectId("Dummy");
        tree.setDigitalObjectId(digitalObjectID);
        CollectionNode cnp;
        CollectionNode cnc = new CollectionNode();
        tree.setName("root");
        cnc.setName("child 1");
        cnp = (CollectionNode) tree;
        cnp.addChild(cnc);
        cnc = new CollectionNode();
        Attribute attr = new Attribute();
        attr.setKey("dummy");
        attr.setValue("attribute");
        cnc.addAttribute(attr);
        cnc.setName("child 2");
        cnp.addChild(cnc);

        cnp = cnc;
        cnc = new CollectionNode();
        cnc.setName("cnc 2.1");
        cnp.addChild(cnc);
        cnc = new CollectionNode();
        cnc.setName("cnc 2.2");
        cnp.addChild(cnc);
        CollectionNode cnp22 = cnc;
        cnc = new CollectionNode();
        cnc.setName("cnc 2.3");
        cnp.addChild(cnc);

        cnp = cnc;
        cnc = new CollectionNode();
        cnc.setName("cnc 2.3.1");
        cnp.addChild(cnc);
        cnc = new CollectionNode();
        cnc.setName("cnc 2.3.2");
        cnp.addChild(cnc);
        cnc = new CollectionNode();
        cnc.setName("cnc 2.3.3");
        cnp.addChild(cnc);

        cnp = cnp22;
        cnc = new CollectionNode();
        cnc.setName("cnc 2.2.1");
        cnp.addChild(cnc);
        cnc = new CollectionNode();
        cnc.setName("cnc 2.2.2");
        cnp.addChild(cnc);
        cnc = new CollectionNode();
        cnc.setName("cnc 2.2.3");
        cnp.addChild(cnc);

        return tree;
    }
}
