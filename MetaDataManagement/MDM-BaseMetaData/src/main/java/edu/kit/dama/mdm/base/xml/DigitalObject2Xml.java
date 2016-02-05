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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.Investigation;
import edu.kit.dama.mdm.base.MetaDataSchema;
import edu.kit.dama.mdm.base.OrganizationUnit;
import edu.kit.dama.mdm.base.Participant;
import edu.kit.dama.mdm.base.Relation;
import edu.kit.dama.mdm.base.Study;
import edu.kit.dama.mdm.base.Task;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.tools.BaseMetaDataHelper;

/**
 * Transform DigitalObject to xml.
 *
 * @author hartmann-v
 */
public class DigitalObject2Xml {

  /**
   * Get the content of the digital object as xml string.
   *
   * @param digitalObject instance holding all information.
   * @return string representation of the digital object in xml format.
   */
  public static String getXmlString(DigitalObject digitalObject) {

    //<editor-fold defaultstate="collapsed" desc="set up xStream">
    // add namespaces
    QNameMap qmap = new QNameMap();
    qmap.setDefaultNamespace(BaseMetaDataHelper.DAMA_NAMESPACE_BASEMETADATA);
    qmap.setDefaultPrefix(BaseMetaDataHelper.DAMA_NAMESPACE_PREFIX);

    StaxDriver staxDriver = new StaxDriver(qmap);

    XStream xstream = new XStream(staxDriver);
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="define short tags for classes">
    xstream.alias("digitalObject", DigitalObject.class);
    xstream.omitField(DigitalObject.class, "digitalObjectId");
    xstream.alias("investigation", Investigation.class);
    xstream.addImplicitCollection(Investigation.class, "metaDataSchema");
    xstream.omitField(Investigation.class, "dataSets");
    xstream.alias("metaDataSchema", MetaDataSchema.class);
    xstream.alias("organizationUnit", OrganizationUnit.class);
    xstream.alias("participant", Participant.class);
    xstream.alias("relation", Relation.class);
    xstream.alias("study", Study.class);
    xstream.omitField(Study.class, "investigations");
    xstream.alias("task", Task.class);

    // <editor-fold defaultstate="collapsed" desc="settings for class UserData">
    xstream.alias("user", UserData.class);
    xstream.omitField(UserData.class, "userId");
    xstream.omitField(UserData.class, "firstName");
    xstream.omitField(UserData.class, "lastName");
    xstream.omitField(UserData.class, "validUntil");
    xstream.omitField(UserData.class, "validFrom");
    xstream.omitField(UserData.class, "email");
    // </editor-fold>

    //<editor-fold defaultstate="collapsed" desc="prepare digitalObject to avoid empty fields">
    // To avoid empty fields in XML they will be omitted explicitly
    if (digitalObject.getExperimenters().isEmpty()) {
      xstream.omitField(DigitalObject.class, "experimenters");
    }
    Investigation investigation = digitalObject.getInvestigation();
    if (investigation != null) {
      if (investigation.getMetaDataSchema().isEmpty()) {
        xstream.omitField(Investigation.class, "metaDataSchema");
      }
      if (investigation.getParticipants().isEmpty()) {
        xstream.omitField(Investigation.class, "participants");
      }
      Study study = investigation.getStudy();
      if (study != null) {
        if (study.getOrganizationUnits().isEmpty()) {
          xstream.omitField(Study.class, "organizationUnits");
        }
      } else {
        xstream.omitField(Investigation.class, "study");
      }
    } else {
      xstream.omitField(DigitalObject.class, "investigation");
    }
    // </editor-fold>
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="set up converters">
    // suppress metadataschema
    xstream.registerConverter(new TaskClassConverter());
    xstream.registerConverter(new MetaDataSchemaClassConverter());
    xstream.registerConverter(new DateConverter("yyyy-MM-dd'T'HH:mm:ss", new String[0]));

    // suppress references and print full information every time.
    xstream.setMode(XStream.NO_REFERENCES);
    //</editor-fold>

    return xstream.toXML(digitalObject);
  }

//  public static void main(String[] args) {
//    DigitalObject digitalObject = DigitalObject.factoryNewDigitalObject();
//    System.out.println("<!--*************************************************************************-->");
//    System.out.println(getXmlString(digitalObject));
//    digitalObject.addExperimenter(UserData.NO_USER);
//    System.out.println("<!--************************addExperimenter*************************************************-->");
//    System.out.println(getXmlString(digitalObject));
//    digitalObject.setInvestigation(Investigation.factoryNewInvestigation());
//    System.out.println("<!--*************************setInvestigation************************************************-->");
//    System.out.println(getXmlString(digitalObject));
//    digitalObject.getInvestigation().addMetaDataSchema(new MetaDataSchema("schema", "url"));
//    System.out.println("<!--***********************addMetaDataSchema**************************************************-->");
//    System.out.println(getXmlString(digitalObject));
//    digitalObject.getInvestigation().addParticipant(new Participant(UserData.NO_USER, new Task("No Task")));
//    System.out.println("<!--******************addParticipant*******************************************************-->");
//    System.out.println(getXmlString(digitalObject));
//    digitalObject.getInvestigation().setStudy(Study.factoryNewStudy());
//    System.out.println("<!--*******************setStudy******************************************************-->");
//    System.out.println(getXmlString(digitalObject));
//    OrganizationUnit organizationUnit = new OrganizationUnit();
//    organizationUnit.setOuName("ouname");
//    digitalObject.getInvestigation().getStudy().addOrganizationUnit(organizationUnit);
//    System.out.println("<!--********************addOrganizationUnit*****************************************************-->");
//    System.out.println(getXmlString(digitalObject));
//  }
}
