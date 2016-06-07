/*
 * Copyright 2016 Karlsruhe Institute of Technology.
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
package edu.kit.dama.dataorganization.impl.neo4j;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import java.util.Iterator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author jejkal
 */
public class Neo4jTest {

    public static void printRecursive(org.neo4j.graphdb.Node node) {
        String ln = "NOde";
        String space = "";
        if (node == null) {
            return;
        }
        if (node.getLabels() != null) {
            Iterator<Label> labels = node.getLabels().iterator();
            String label = "test"; //labels.next().name();
            space = label.startsWith("Collection") ? "   " : "      ";
            ln = space + "Label: " + label;
        }

        System.out.println(ln);
        System.out.println(space + "Props: " + node.getAllProperties());

        Iterator<Relationship> r = node.getRelationships(Direction.OUTGOING, new RelationshipType() {
            @Override
            public String name() {
                return "IS_PARENT";
            }
        }).iterator();

        while (r.hasNext()) {
            Relationship s = r.next();
            printRecursive(s.getEndNode());
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("IUnit tree");

        DataOrganizer dor = DataOrganizerFactory.getInstance().getDataOrganizer();
        //dor.configure("http://localhost:7474", "neo4j", "test");
        // edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizerImpl dor = new edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizerImpl();

        // IFileTree tree = DataOrganizationUtils.createTreeFromFile("Large4", new AbstractFile(new File("/Users/jejkal/NetBeansProjects/KITDM/trunk")), true);
        //edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizerImpl dor = new edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizerImpl();       
        // System.out.println("Create tree");
        //  dor.createFileTree(tree);
        System.out.println("DONE");
        long s = System.currentTimeMillis();

        System.out.println(dor.getViews(new DigitalObjectId("Large4")));
        //dor.createFileTree(tree);
        System.out.println("R " + (System.currentTimeMillis() - s));

        /* // tree  = new edu.kit.dama.mdm.dataorganization.impl.jpa.DataOrganizerImpl().loadFileTree(new DigitalObjectId("abcd-12345"));
        long s = System.currentTimeMillis();
        System.out.println("Load tree");
        IFileTree tree = dor.loadFileTree(new DigitalObjectId("Large"));
        System.out.println("R " + (System.currentTimeMillis() - s));

        DataOrganizerImpl dor1 = new DataOrganizerImpl();
        s = System.currentTimeMillis();
        tree = dor1.loadFileTree(new DigitalObjectId("Large"));
        System.out.println("R " + (System.currentTimeMillis() - s));
         */
 /*  s = System.currentTimeMillis();
         tree = dor.loadFileTree(new DigitalObjectId("abcd-12345"));
                 System.out.println("R " + (System.currentTimeMillis() - s));
                 s = System.currentTimeMillis();
         tree = dor.loadFileTree(new DigitalObjectId("abcd-12345"));
        System.out.println("R " + (System.currentTimeMillis() - s));*/
        //  IFileTree tree = DataOrganizerImpl.getSingleton().loadFileTree(new DigitalObjectId("abcd-12345"));//loadSubTree(new NodeId(null, 9l, 0),99);
        //DataOrganizationUtils.printTree(tree.getRootNode(), true);
        // System.out.println(DataOrganizerImpl.getSingleton().getChildCount(new NodeId(null, 75513l, 0)));
        // System.out.println("T " + (System.currentTimeMillis() - t));
        // t = System.currentTimeMillis();
        // DataOrganizationUtils.printTree(DataOrganizerImpl.getSingleton().loadFileTree(new DigitalObjectId("1234567"), "default").getRootNode(), true);
        // System.out.println("T " + (System.currentTimeMillis() - t));
    }
}
