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
package edu.kit.dama.mdm.dataorganization.test.performance;

import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.DataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.FileTree;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.ext.FSParser;
import edu.kit.dama.commons.types.DigitalObjectId;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author pasic
 */
public class BurningStickTest {

  DigitalObjectId doid;

  public BurningStickTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    PersistenceFacade.getInstance().setPersistenceUnit(
            "DataOrganizationPU");
  }

  @After
  public void tearDown() {
  }

  @Test
  @Ignore
  public void save() throws IOException, EntityExistsException {

    FSParser fsp = new FSParser();

    doid = new DigitalObjectId("10001337");

    System.out.println("ParsingFS!");
    File file = new File("/localhome/tmp/Beethoven/");
    DataOrganizationNode parseDONodes = fsp.parseDONodes(file, 6);
    System.out.println("ParsingFS FINISHED!");
    FileTree fileTree = new FileTree();
    fileTree.setDigitalObjectId(doid);
    fileTree.addChild(parseDONodes);
    fileTree.setViewName("default");

    DataOrganizer dor = DataOrganizerFactory.getInstance().getDataOrganizer();
    long start = System.currentTimeMillis();
    dor.createFileTree(fileTree);
    System.out.println("Save took " + (double) (System.currentTimeMillis() - start) / 1000 + " sec.");

    //doid = new DigitalObjectId("10001338");
    System.out.println("ParsingFS!");
    file = new File("/localhome/tmp/Music/");
    parseDONodes = fsp.parseDONodes(file, 6);
    System.out.println("ParsingFS FINISHED!");
    fileTree = new FileTree();
    fileTree.setDigitalObjectId(doid);
    fileTree.addChild(parseDONodes);
    fileTree.setViewName("fooview");

    dor = DataOrganizerFactory.getInstance().
            getDataOrganizer();
    start = System.currentTimeMillis();
    dor.createFileTree(fileTree);
    System.out.println("Save took " + (double) (System.currentTimeMillis()
            - start) / 1000 + " sec.");
  }

  @Test
  @Ignore
  public void load() throws EntityNotFoundException {
    DataOrganizer dor = DataOrganizerFactory.getInstance().
            getDataOrganizer();
    long start = System.currentTimeMillis();
    IFileTree tree = dor.loadFileTree(doid);
    System.out.println("Load took " + (double) (System.currentTimeMillis()
            - start) / 1000 + " sec.");
  }
}
