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
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.xml.DigitalObject2Xml;
import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.core.jpa.MetaDataManagerJpa;
import edu.kit.dama.mdm.tools.BaseMetaDataHelper;
import edu.kit.dama.rest.basemetadata.types.DigitalObjectWrapper;
import edu.kit.dama.rest.util.RestUtils;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.StringWriter;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.fzk.tools.xml.JaxenUtil;
import org.quartz.JobDataMap;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import uk.co.jemos.podam.api.AbstractRandomDataProviderStrategy;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

/**
 *
 * @author mf6319
 */
public class LocalAccessTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(BaseMetaDataCleaningHelper.class);

    private static String getStringFromDocument(Document doc) throws TransformerException {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    public static void main(String[] args) throws Exception {
        DigitalObject o = DigitalObject.factoryNewDigitalObject();
        o.setBaseId(1l);
        o.setLabel("Test");
        o.setNote("hello!");
        System.out.println(RestUtils.transformObject(new Class[]{DigitalObjectWrapper.class}, "default", new DigitalObjectWrapper(o)));

        if (true) {
            return;
        }
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        AuthorizationContext ctx = new AuthorizationContext(new UserId("admin"), new GroupId("eCod"), Role.ADMINISTRATOR);
        mdm.setAuthorizationContext(ctx);
        long s = System.currentTimeMillis();
        //System.out.println(new DigitalObjectSecureQueryHelper().getReadableResources(mdm, 0, 100, ctx).size());
        System.out.println("Time: " + (System.currentTimeMillis() - s));
        s = System.currentTimeMillis();
        mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
        System.out.println(mdm.findResultList("SELECT o FROM DigitalObject o", DigitalObject.class, 0, 100).size());
        System.out.println("Time2: " + (System.currentTimeMillis() - s));
        if (true) {
            return;
        }

//        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
//        //mdm.setAuthorizationContext(new AuthorizationContext(new UserId("admin"), new GroupId("eCod"), Role.MANAGER));
//        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
//        mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
//        long s = System.currentTimeMillis();
//        DigitalObject result = mdm.find(DigitalObject.class, 1000l);
//        System.out.println(result.getBaseId());
//        System.out.println(result.getLabel());
//        System.out.println(result.getInvestigation().getInvestigationId());
//        System.out.println(result.getInvestigation().getTopic());
//        /* System.out.println(result.getStudyId());
//         System.out.println(result.getTopic());*/
//        System.out.println("TIME: " + (System.currentTimeMillis() - s));
//        // System.out.println(result.getInvestigations().size());

        /* System.out.println(result.getBaseId());
         System.out.println(result.getLabel());
         System.out.println(result.getNote());
         System.out.println("--------");
         mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
         result = mdm.save(result);
         System.out.println(result.getBaseId());
         System.out.println(result.getLabel());
         System.out.println(result.getNote());
         System.out.println("--------");
         result = mdm.find(DigitalObject.class).get(0);
         System.out.println(result.getBaseId());
         System.out.println(result.getLabel());
         System.out.println(result.getNote());*/

 /* System.out.println("TOPIC " + result.get(0).getTopic());
         System.out.println("NOT " + result.get(0).getNote());
         System.out.println("INV " + result.get(0).getInvestigations());
         System.out.println("DO " + ((Investigation) result.get(0).getInvestigations().toArray()[0]).getDataSets());*/
        if (true) {
            return;
        }

        long t = System.currentTimeMillis();

        AbstractRandomDataProviderStrategy stra = new AbstractRandomDataProviderStrategy() {
        };
        stra.setDefaultNumberOfCollectionElements(1);
        PodamFactory factory = new PodamFactoryImpl(stra);
        DigitalObject a = factory.manufacturePojo(DigitalObject.class);
        DigitalObject a2 = factory.manufacturePojoWithFullData(DigitalObject.class);

        //for (int i = 0; i < 10; i++) {
        //  long s = System.currentTimeMillis();
        Marshaller marshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(DigitalObject.class).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("eclipselink.media-type", "application/json");
        marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, "default");
        marshaller.marshal(a, System.out);
        marshaller.marshal(a2, System.out);
        //  t += System.currentTimeMillis() - s;
        // }
        System.out.println("D " + (System.currentTimeMillis() - t));
        if (true) {
            return;
        }

        Document docDigitalObject = null;
        String completeXml = null;

        String baseXML = DigitalObject2Xml.getXmlString(DigitalObject.factoryNewDigitalObject());

        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(baseXML.getBytes());
            docDigitalObject = JaxenUtil.getW3CDocument(bin);//XMLTools.parseDOM(baseXML);
        } catch (Exception exc) {
            throw new MetaDataExtractionException("Failed to transform DigitalObject XML.", exc);
        }

        Element digitalObjectElement = docDigitalObject.getDocumentElement();

        Document completeDocument = null;
        try {
            completeDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            throw new MetaDataExtractionException("Failed to generate target document.", ex);
        }
        Element rootElement = completeDocument.createElement("test");

        Element root = completeDocument.createElementNS(BaseMetaDataHelper.DAMA_NAMESPACE_BASEMETADATA, BaseMetaDataHelper.DAMA_NAMESPACE_PREFIX);
        root.appendChild(completeDocument.importNode(digitalObjectElement, true));
        Node csmdRoot = root.appendChild(completeDocument.createElementNS(BaseMetaDataHelper.DAMA_NAMESPACE_METADATA, BaseMetaDataHelper.CSMD_NAMESPACE_PREFIX));
        csmdRoot.appendChild(completeDocument.importNode(rootElement, true));

        completeDocument.appendChild(root);
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation", BaseMetaDataHelper.DAMA_NAMESPACE_METADATA + " " + BaseMetaDataHelper.DAMA_NAMESPACE_BASEMETADATA + "/MetaData.xsd");
        // convert tweaked DOM back to XML string
        try {
            completeXml = getStringFromDocument(completeDocument);//XMLTools.getXML(completeDocument);
        } catch (Exception exc) {
            throw new MetaDataExtractionException("Internal XML conversion error.", exc);
        }
        System.out.println(completeXml);

//    IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
//    IAuthorizationContext context = new AuthorizationContext(new UserId("admin"), new GroupId(Constants.USERS_GROUP_ID), Role.ADMINISTRATOR);//AuthorizationContext.factorySystemContext();
//    mdm.setAuthorizationContext(context);
//    try {
//      //TransferClientProperties props = new TransferClientProperties();
//      //props.setStagingAccessPointId("0000-0000-0000-0000");
//      //IngestInformationServiceLocal.getSingleton().prepareIngest(new DigitalObjectId("c98408fc-36d0-4cc0-8197-340873d6698e"), props, context);
//
//      //System.out.println(StagingService.getSingleton().finalizeIngest(new DigitalObjectId("c98408fc-36d0-4cc0-8197-340873d6698e"), context));
//      System.out.println(MetadataIndexingHelper.getSingleton().performIndexing("KITDataManager", "dc", new GroupId("USERS"), 10, context));
//    } finally {
//      mdm.close();
//    }
    }

    //<editor-fold defaultstate="collapsed" desc="PerformenceTestCode">
//  IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager("MDM-Core");
//    IAuthorizationContext context = AuthorizationContext.factorySystemContext();//new AuthorizationContext(new UserId("admin"), new GroupId("eCod"), Role.MANAGER);//AuthorizationContext.factorySystemContext();
//    mdm.setAuthorizationContext(context);
//    try {
//      long sum = 0;
//      for (int i = 0; i < 10; i++) {
//        long t = System.currentTimeMillis();
//        List<DigitalObject> result = mdm.findResultList("SELECT o FROM DigitalObject o", DigitalObject.class, 0, 1000);
//        //List<DigitalObject> result = new DigitalObjectSecureQueryHelper().getReadableResources(md, 0, 1000, context);
//        //DigitalObject result = new DigitalObjectSecureQueryHelper().objectByIdentifierExists("907b23bc-f2f4-42b4-9bf0-c621c2033175", md, context);
//        //DigitalObject result = new DigitalObjectSecureQueryHelper().getObjectByIdentifier("907b23bc-f2f4-42b4-9bf0-c621c2033175", md, context);
//        //DigitalObject result = md.findSingleResult("SELECT o FROM DigitalObject o WHERE o.digitalObjectIdentifier='" + "907b23bc-f2f4-42b4-9bf0-c621c2033175" + "'", DigitalObject.class);
//        sum += (System.currentTimeMillis() - t);
//      }
//      System.out.println("D " + (sum / 10));
//
//    } finally {
//      mdm.close();
//    }
//</editor-fold>
}
