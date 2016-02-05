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
package edu.kit.dama.mdm.base.xml;

import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.commons.types.DigitalObjectId;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test class for testing XML serialization.
 *
 * @author hartmann-v
 */
public class TestXML {

  /**
   * Test method to generate an xml file.
   *
   * @param args Command line arguments.
   * @throws Exception Folder not parsable.
   */
  public static void main(String[] args) throws Exception {
    DigitalObject digitalObject = createDigitalObject();
    System.out.println(DigitalObject2Xml.getXmlString(digitalObject));
  }

  /**
   * Create a sample digital object.
   *
   * @return sample instance of a digital object.
   */
  public static DigitalObject createDigitalObject() {
    Study study = new Study();
    Investigation investigation = new Investigation();
    DigitalObject digitalObject = new DigitalObject();
    UserData user1 = new UserData();
    try {
      user1.setDistinguishedName("cn: user1");
      user1.setEmail("user@local.host");
      user1.setFirstName("firstUser");
      user1.setLastName("firstLastname");
      user1.setUserId(1l);
      user1.setValidFrom(new Date());
      Thread.sleep(1000);
      user1.setValidUntil(new Date());

      UserData user2 = new UserData();
      user2.setDistinguishedName("cn: user2");
      user2.setEmail("user2@local.host");
      user2.setFirstName("secondUser");
      user2.setLastName("secondLastname");
      user2.setUserId(2l);
      user2.setValidFrom(new Date());
      Thread.sleep(1000);
      user2.setValidUntil(new Date());

      UserData head = new UserData();
      head.setDistinguishedName("cn: head");
      head.setEmail("head@local.host");
      head.setFirstName("headUser");
      head.setLastName("headLastname");
      head.setUserId(3l);
      head.setValidFrom(new Date());
      Thread.sleep(1000);
      head.setValidUntil(new Date());

      Task taskHead = new Task("head");
      Task taskDevel = new Task("devel");

      OrganizationUnit ipe = new OrganizationUnit();
      ipe.setAddress("Hermann-Platz 1");
      ipe.setCity("Leo");
      ipe.setCity("de");
      ipe.setManager(head);
      ipe.setOuName("IPE");
      ipe.setWebsite("www.ipe.kit.edu");
      ipe.setZipCode("76344");

      digitalObject.setDigitalObjectId(new DigitalObjectId("uniqueID-0000001"));
      Date now = new Date();
      digitalObject.setEndDate(now);
      digitalObject.addExperimenter(user1);
      digitalObject.setNote("Keine Note!");
      digitalObject.setStartDate(new Date(now.getTime() - 3600000l));
      digitalObject.setUploadDate(new Date(now.getTime() - 60000));
      digitalObject.setUploader(user2);
      digitalObject.setVisible(Boolean.TRUE);

      investigation.setDescription("any desc");
      investigation.setEndDate(new Date(now.getTime() + 10000000));
      investigation.addMetaDataSchema(new MetaDataSchema("cu", "url"));
      investigation.setNote("6, setzen!");
      investigation.addParticipant(new Participant(head, taskHead));
      investigation.addParticipant(new Participant(user1, taskDevel));
      investigation.addParticipant(new Participant(user2, taskDevel));
      investigation.setStartDate(new Date(now.getTime() - 10000000));
      investigation.setStudy(study);
      investigation.setTopic("Topic");
      investigation.setVisible(true);
      investigation.addDataSet(digitalObject);

      study.addInvestigation(investigation);
      study.setLegalNote("egal");
      study.setManager(head);
      study.setNote("ausreichend");
      study.setTopic("Topic Study");
      study.addOrganizationUnit(ipe, taskHead);
      study.setVisible(true);
      study.setStartDate(new Date(now.getTime() - 15000000));
      study.setEndDate(new Date(now.getTime() + 15000000));

    } catch (InterruptedException ex) {
      Logger.getLogger(TestXML.class.getName()).log(Level.SEVERE, null, ex);
    }
    return digitalObject;
  }
}
