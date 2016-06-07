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
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.xml.DigitalObject2Xml;
import edu.kit.dama.mdm.content.impl.exceptions.MetaDataExtractionException;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileTreeImpl;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.tools.BaseMetaDataHelper;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.basemetadata.client.impl.BaseMetaDataRestClient;
import edu.kit.dama.staging.adalapi.authenticator.KITDMAuthenticator;
import edu.kit.dama.staging.adalapi.protocol.DatabaseProtocolConfigurator;
import edu.kit.dama.staging.adalapi.protocol.SimpleHttp;
import edu.kit.dama.staging.entities.AdalapiProtocolConfiguration;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.util.ProtocolSettings;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.fzk.tools.xml.JaxenUtil;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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

       Map<String, Object> custom = new HashMap<>();
        custom.put("repository.context", "empty");
        AdalapiProtocolConfiguration config = AdalapiProtocolConfiguration.factoryConfiguration(new URL("http://dreamatico.com/data_images/kitten/kitten-2.jpg"),SimpleHttp.class.getCanonicalName(), KITDMAuthenticator.class.getCanonicalName(), custom);

        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            mdm.save(config);
        } finally {
            mdm.close();
        }
        ProtocolSettings.getSingleton().setExternalProtocolConfigurator(new DatabaseProtocolConfigurator());
        
        new AbstractFile(new URL("http://dreamatico.com/data_images/kitten/kitten-2.jpg")).transfer(new AbstractFile(new File("test.json")));
        System.out.println("ONDE");
// AbstractFile fi = new AbstractFile(new URL("http://ipelsdf1.lsdf.kit.edu:8889/webdav/admin/1/data/screen.jpg"));
        //SimpleRESTContext context = new SimpleRESTContext("admin", "dama14");

        /* UserGroupWrapper groupWrapper = client1.getAllGroups(0, Integer.MAX_VALUE);
        System.out.println("The following groups were found:");
        for (UserGroup group : groupWrapper.getEntities()) {
            System.out.println(" Name: " + group.getGroupName());
            UserDataWrapper members = client1.getUsersOfGroup(group.getId(), 0, Integer.MAX_VALUE);
            System.out.println(" The group has the following members:");
            for (UserData user : members.getEntities()) {
                System.out.println("   - " + user.getFullname() + " (" + user.getDistinguishedName() + ")" + user.getUserId());
            }
        }
        //  client1.addGroup("uniqueId", "Another Custom Group", "A custom group created for testing purposes.");

        UserDataWrapper userWrapper = client1.getAllUsers(Constants.USERS_GROUP_ID, 0, Integer.MAX_VALUE);
        for (UserData user : userWrapper.getEntities()) {
            System.out.println(" - " + user.getFullname() + " (" + user.getDistinguishedName() + ")");
        }
        
        //distinguished name or id
        int modified = client1.addUserToGroup(3l, "tester").getCount();
        
        System.out.println(client1.removeUserFromGroup(3l, 421l).getCount());
         */
 /* UserDataWrapper newUser = client1.addUser(Constants.USERS_GROUP_ID, "Test", "User", "test@mail.org", newUserIdentifier);
        System.out.println(newUser.getEntities().get(0).getDistinguishedName());
        //distinguished name or id
        int modified = client1.addUserToGroup(3l, newUserIdentifier).getCount();
         */
 /*  UserGroupWrapper groupWrapper = client.getAllGroups(0, Integer.MAX_VALUE);

        for (UserGroup group : groupWrapper.getEntities()) {
            System.out.println("GName: " + group.getGroupName());
        }

        UserDataWrapper userWrapper = client.getAllUsers("eCod", 0, Integer.MAX_VALUE);
        for (UserData user : userWrapper.getEntities()) {
            System.out.println("UName: " + user.getDistinguishedName());

        }*/
//        AbstractRandomDataProviderStrategy stra = new AbstractRandomDataProviderStrategy() {
//            @Override
//            public Long getLong(AttributeMetadata attributeMetadata) {
//                if (attributeMetadata.getAttributeName().toLowerCase().contains("id")) {
//                    return 0l;
//                }
//                return super.getLong(attributeMetadata); //To change body of generated methods, choose Tools | Templates.
//            }
//
//            @Override
//            public Object getMemoizedObject(AttributeMetadata attributeMetadata) {
//                if (attributeMetadata != null && attributeMetadata.getAttributeName() != null) {
//                    switch (attributeMetadata.getAttributeName()) {
//                        case "validFrom":
//                            return new Date(0);
//                        case "startDate":
//                            return new Date(0);
//                        case "validUntil":
//                            return new Date(System.currentTimeMillis());
//                        case "endDate":
//                            return new Date(System.currentTimeMillis());
//                        case "uploadDate":
//                            return new Date(System.currentTimeMillis());
//                    }
//                }
//                return super.getMemoizedObject(attributeMetadata);
//            }
//
//        };
//        stra.setDefaultNumberOfCollectionElements(1);       
//        PodamFactory factory = new PodamFactoryImpl(stra);
//
//        DigitalObject a2 = factory.manufacturePojo(DigitalObject.class);
//
//        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
//        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
//        a2 = mdm.save(a2);
//
//        System.out.println(a2);
        if (true) {
            return;
        }

        /*String content = org.apache.commons.io.FileUtils.readFileToString(new File("/Users/jejkal/Software/GenericRestClient-1.2/bin/data/default_view.json"));
        JSONObject viewObject = new JSONObject(content);
        IFileTree tree1 = Util.jsonViewToFileTree(viewObject, true, false);
        DataOrganizationUtils.printTree(tree1.getRootNode(), true);*/
 /*AdalapiProtocolConfiguration config = AdalapiProtocolConfiguration.factoryConfiguration(new URL("http://localhost:8080"), "edu.kit.lsdf.adalapi.protocols.WebDav", "edu.kit.dama.staging.adalapi.authenticator.KITDMAuthenticator", null);
        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        System.out.println("RESULT " + mdm.save(config));

        DatabaseProtocolConfigurator config1 = new DatabaseProtocolConfigurator();
        ProtocolSettings.getSingleton().setExternalProtocolConfigurator(config1);

        Configuration configuration = config1.getConfiguration(new URL("http://localhost:8080"));
        Iterator keys = configuration.getKeys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            System.out.println(key + " - " + configuration.getString(key));
        }*/
        //SardineImpl impl = new SardineImpl("webdav", "webdav");
        //impl.enablePreemptiveAuthentication(new URL("http://127.0.0.1:8080/webdav/admin/1/data/index.html"));
        //impl.setCredentials("webdav", "webdav");
        //System.out.println(impl.get("http://webdav@127.0.0.1:8080/webdav/admin/1/data/index.html"));
        //System.out.println(impl.exists("http://webdav@ipesuco1.ipe.kit.edu:10000/"));
        /*Configuration config = new DatabaseProtocolConfigurator().getConfiguration(new URL("http://localhost:8080/webdav/admin/1/data/index.html"));
        config.addProperty("username", "webdav");
        config.addProperty("password", "webdav");
        
        AbstractFile file = new AbstractFile(new URL("http://localhost:8080/webdav/admin/1/data/index.html"));
        System.out.println(file.exists());
        
       /* Configuration config1 = new DatabaseProtocolConfigurator().getConfiguration(new URL("http://localhost:8080/webdav/admin/1/data/index.html"));
        config1.addProperty("username", "webdav1");
        config1.addProperty("password", "webdav1");
         */
 /* AbstractFile file2 = new AbstractFile(new URL("http://localhost:8080/webdav/admin/1/data/index.html"));
        System.out.println(file2.exists());*/
        // createDestination("1", new AuthorizationContext(new UserId("admin"), new GroupId("USERS"), Role.ADMINISTRATOR));
        //  System.out.println(file.exists());
        ///////////
//          long investigationId = -1l;
//        int first = 0;
//        int results = 10;
//         List<DigitalObject> objects;
//            if (investigationId <= 0) {
//                //no investigationId provided...get all objects
//                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
//                objects = mdm.findResultList("SELECT o FROM DigitalObject o", DigitalObject.class, first, results);
//            } else {
//                //first, obtain investigation for id
//                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "Investigation.simple");
//                Investigation investigation = mdm.find(Investigation.class, investigationId);
//                if (investigation == null) {
//                    LOGGER.error("Investigation for id {} not found.", investigationId);
//                    throw new WebApplicationException(Response.Status.NOT_FOUND);
//                }
//                //try to get objects in investigation
//                mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
//                objects = mdm.findResultList("SELECT o FROM DigitalObject o WHERE o.investigation.investigationId=" + investigationId, DigitalObject.class, first, results);
//            }
        ////////
        /* BaseMetaDataRestClient cl = new BaseMetaDataRestClient("http://ipesuco1.ipe.kit.edu:8080/KITDM/rest/basemetadata/", new SimpleRESTContext("admin", "dama14"));
        DigitalObjectWrapper w = cl.getAllDigitalObjects(63l, 0, 100, "eCod");
        
        System.out.println("Time2: " + (System.currentTimeMillis() - s));
         */
        //DataOrganizationRestClient doClient = new DataOrganizationRestClient("http://localhost:8080/KITDM/rest/dataorganization/", new SimpleRESTContext("admin", "dama14"));
        /* DataOrganizer org = DataOrganizerFactory.getInstance().getDataOrganizer();
        IFileTree tree1 = org.loadFileTree(new DigitalObjectId("ef16c1e5-d9b3-44b5-ba77-b9877082d02c"), "default");
        
        IFileTree sub = org.loadSubTree(new NodeId(new DigitalObjectId("ef16c1e5-d9b3-44b5-ba77-b9877082d02c"), 400l,1), 0);
        
        DataOrganizationUtils.printTree(sub.getRootNode(), true);*/

 /* FileTreeImpl newTree = new FileTreeImpl();
        newTree.setDigitalObjectId(new DigitalObjectId("ef16c1e5-d9b3-44b5-ba77-b9877082d02c"));
        newTree.setViewName("custom");
        CollectionNodeImpl images = new CollectionNodeImpl();
        images.setNodeId(400l);

        CollectionNodeImpl documents = new CollectionNodeImpl();
        documents.setName("documents");
        FileNodeImpl fDocumentation = new FileNodeImpl(null);
        fDocumentation.setNodeId(200l);
        documents.addChild(fDocumentation);
        newTree.getRootNode().addChild(images);
        newTree.getRootNode().addChild(documents);

        doClient.postView("USERS", 31l, newTree, Boolean.TRUE, new SimpleRESTContext("admin", "dama14"));
         */
 /* DataOrganizer org = DataOrganizerFactory.getInstance().getDataOrganizer();
        IFileTree tree1 = org.loadFileTree(new DigitalObjectId("ef16c1e5-d9b3-44b5-ba77-b9877082d02c"), "custom");

        DataOrganizationUtils.printTree(tree1.getRootNode(), true);*/
        if (true) {
            return;
        }

        FileTreeImpl tree = new FileTreeImpl();
        tree.setDigitalObjectId(new DigitalObjectId("12345"));
        tree.setViewName("test");
        CollectionNodeImpl c = new CollectionNodeImpl();
        c.setName("newNode");
        FileNodeImpl f = new FileNodeImpl(null);
        f.setNodeId(2l);
        FileNodeImpl f2 = new FileNodeImpl(null);
        f2.setNodeId(3l);
        c.addChild(f);
        c.addChild(f2);
        tree.getRootNode().addChild(c);
        System.out.println(Util.fileTreeToJsonView(tree).toString(1));
        if (true) {
            return;
        }

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

        if (true) {
            return;
        }

        AuthorizationContext ctx1 = new AuthorizationContext(new UserId("admin"), new GroupId("eCod"), Role.ADMINISTRATOR);

        DigitalObject pObject1 = new DigitalObject();
        DigitalObject pObject2 = new DigitalObject();

        String groupID = "USERS";
        SimpleRESTContext context = new SimpleRESTContext("admin", "dama14");
        BaseMetaDataRestClient client = new BaseMetaDataRestClient("http://localhost:8080/KITDM/rest/basemetadata/", context);

        pObject1.setBaseId(31l);
        pObject2.setBaseId(32l);
        System.out.println(client.addTransitionToDigitalObject(pObject1, pObject2, groupID));
        pObject1.setBaseId(31l);
        pObject2.setBaseId(33l);
        System.out.println(client.addTransitionToDigitalObject(pObject1, pObject2, groupID));
        pObject1.setBaseId(31l);
        pObject2.setBaseId(31l);
        System.out.println(client.addTransitionToDigitalObject(pObject1, pObject2, groupID));
        //  IMetaDataManager mdm1 = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        // mdm1.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.simple");
        //   AuthorizationContext ctx1 = new AuthorizationContext(new UserId("admin"), new GroupId("eCod"), Role.ADMINISTRATOR);
        //   mdm1.setAuthorizationContext(ctx1);*/
        /* StringBuilder query = new StringBuilder();
        IMetaDataManager mdm1 = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
        mdm1.setAuthorizationContext(ctx1);
        String domain = SecurableEntityHelper.getSecurableResourceDomain(DigitalObject.class);
        String uniqueField = SecurableEntityHelper.getDomainUniqueFieldName(DigitalObject.class);

        query.append("SELECT o FROM FilterHelper f, ").
                append("DigitalObject").append(" o WHERE ");

        query.append("f.userId='").append(ctx1.getUserId().getStringRepresentation());
        query.append("' AND ");

        query.append("f.groupId='").append(ctx1.getGroupId().getStringRepresentation()).append("' AND ")
                .append("f.domainId='").append(domain).
                append("' AND f.roleAllowed>=").append(Role.GUEST.ordinal()).
                append(" AND f.domainUniqueId=o.").append(uniqueField);

        long s1 = System.currentTimeMillis();

        List<DigitalObject> result2 = mdm1.findResultList(query.toString(), DigitalObject.class, 0, 100);
        System.out.println("D: " + (System.currentTimeMillis() - s1));*/
 /*for(int i=0;i<100;i++){
    if(result1.get(i).getDigitalObjectId().equals(result2.get(i).getDigitalObjectId())){
        System.out.println("ERROR ");
        System.out.println(result1.get(i));
        System.out.println("=====================");
        System.out.println(result2.get(i));
        System.out.println("===================");
        System.out.println(i);
    }
}*/
        if (true) {
            return;
        }
//        IMetaDataManager mdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager();
//        AuthorizationContext ctx = new AuthorizationContext(new UserId("admin"), new GroupId("eCod"), Role.ADMINISTRATOR);
//        mdm.setAuthorizationContext(ctx);
//        long s = System.currentTimeMillis();
//        //System.out.println(new DigitalObjectSecureQueryHelper().getReadableResources(mdm, 0, 100, ctx).size());
//        System.out.println("Time: " + (System.currentTimeMillis() - s));
//        s = System.currentTimeMillis();
//        mdm.addProperty(MetaDataManagerJpa.JAVAX_PERSISTENCE_FETCHGRAPH, "DigitalObject.default");
//        System.out.println(mdm.findResultList("SELECT o FROM DigitalObject o", DigitalObject.class, 0, 100).size());
//        System.out.println("Time2: " + (System.currentTimeMillis() - s));
//        if (true) {
//            return;
//        }

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

//        long t = System.currentTimeMillis();
//
//        AbstractRandomDataProviderStrategy stra = new AbstractRandomDataProviderStrategy() {
//        };
//        stra.setDefaultNumberOfCollectionElements(1);
//        PodamFactory factory = new PodamFactoryImpl(stra);
//        DigitalObject a = factory.manufacturePojo(DigitalObject.class);
//        DigitalObject a2 = factory.manufacturePojoWithFullData(DigitalObject.class);
//
//        //for (int i = 0; i < 10; i++) {
//        //  long s = System.currentTimeMillis();
//        Marshaller marshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(DigitalObject.class).createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        marshaller.setProperty("eclipselink.media-type", "application/json");
//        marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, "default");
//        marshaller.marshal(a, System.out);
//        marshaller.marshal(a2, System.out);
//        //  t += System.currentTimeMillis() - s;
//        // }
//        System.out.println("D " + (System.currentTimeMillis() - t));
//        if (true) {
//            return;
//        }
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
