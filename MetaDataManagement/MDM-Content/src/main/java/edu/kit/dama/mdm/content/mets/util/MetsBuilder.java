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
package edu.kit.dama.mdm.content.mets.util;

import au.edu.apsr.mtk.base.Agent;
import au.edu.apsr.mtk.base.AmdSec;
import au.edu.apsr.mtk.base.DigiprovMD;
import au.edu.apsr.mtk.base.Div;
import au.edu.apsr.mtk.base.DmdSec;
import au.edu.apsr.mtk.base.FLocat;
import au.edu.apsr.mtk.base.File;
import au.edu.apsr.mtk.base.FileGrp;
import au.edu.apsr.mtk.base.FileSec;
import au.edu.apsr.mtk.base.Fptr;
import au.edu.apsr.mtk.base.MDTYPE;
import au.edu.apsr.mtk.base.METS;
import au.edu.apsr.mtk.base.METSException;
import au.edu.apsr.mtk.base.METSWrapper;
import au.edu.apsr.mtk.base.MdWrap;
import au.edu.apsr.mtk.base.MetsHdr;
import au.edu.apsr.mtk.base.SourceMD;
import au.edu.apsr.mtk.base.StructMap;
import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.base.UserData;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.util.DataManagerSettings;
import edu.kit.dama.mdm.content.util.DublinCoreHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static edu.kit.dama.mdm.content.mets.util.MetsNamespaceDefinition.*;

/**
 * Helper class building METS document.
 * @author jejkal
 */
public class MetsBuilder {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MetsBuilder.class);

  
  private final METS mets;
  private final METSWrapper mw;
  private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
  private final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
  private final DigitalObject theObject;
  private int fileId = 0;
  private int structId = 0;
  private final String metsProfile;

  /**
   * Hidden default constructor.
   *
   * @param object The object to write as METS document.
   */
  MetsBuilder(DigitalObject object, String profile) throws METSException {
    mw = new METSWrapper();
    mets = mw.getMETSObject();
    theObject = object;
    metsProfile = (profile != null) ? profile : KITDM_PROFILE;
  }

  /**
   * Initialize the METS builder for the default KIT DM mets profile.
   *
   * @param object The object to write as METS.
   *
   * @return The builder instance.
   *
   * @throws METSException If something goes wrong.
   */
  public static MetsBuilder init(DigitalObject object) throws METSException {
    return new MetsBuilder(object, null);
  }

  /**
   * Initialize the METS builder for a custom mets profile.
   *
   * @param object The object to write as METS.
   * @param profile The linked mets profile.
   *
   * @return The builder instance.
   *
   * @throws METSException If something goes wrong.
   */
  public static MetsBuilder init(DigitalObject object, String profile) throws METSException {
    return new MetsBuilder(object, profile);
  }

  /**
   * Create all elements of a minimal METS document fulfilling the KIT DM METS
   * profile. This documents contains a content metadata section with Dublin
   * Core metadata, two adminsitrative metadata sections containing base
   * metadata and data organization documents and a file and structure section
   * reflecting the data organization of the digital object.
   *
   * @param creator The creator stored in the METS header as agent.
   *
   * @return The builder instance.
   *
   * @throws Exception If anything goes wrong.
   */
  public MetsBuilder createMinimalMetsDocument(UserData creator) throws Exception {
    LOGGER.debug("Creating minimal mets document for object with base id {} and creator {}", theObject.getBaseId(), creator);
    final long baseId = theObject.getBaseId();
    Function<IDataOrganizationNode, String> defaultObjectNodeResolver = (IDataOrganizationNode node) -> {
      return DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_BASE_URL_ID, "http://localhost:8080/KITDM")
              + "/rest/dataorganization/organization/download/" + Long.toString(baseId) + "/" + ((node.getName() != null) ? node.getName() : "");
    };
    return this.createMetsHeader(creator).
            createDCSection(creator).
            createBMDSection(true).
            createDOSection(defaultObjectNodeResolver).
            createStructureSection(defaultObjectNodeResolver);
  }

  /**
   * Create the default Dublin Core section as descriptive metadata. The
   * provided creator argument is used in the Dublin Core document as author and
   * publisher. If no creator is provided, the uploader of the digital object is
   * used if available.
   *
   * @param creator The creator.
   *
   * @return The builder instance.
   *
   * @throws METSException If creating the METS structure failed.
   * @throws ParserConfigurationException If creating the Dublin Core document
   * failed.
   */
  public MetsBuilder createDCSection(UserData creator) throws METSException, ParserConfigurationException {
    return addCustomDmdSection("DUBLIN-CORE", MDTYPE.OTHER, "OAI-DUBLIN-CORE", DublinCoreHelper.createDublinCoreDocument(theObject, creator));
  }

  /**
   * Create the default base metadata section as administrative metadata.
   *
   * @param compact Use the compact version of the base metadata omitting all
   * information of study and investigation but their ids.
   *
   * @return The builder instance.
   *
   * @throws Exception If anything goes wrong.
   */
  public MetsBuilder createBMDSection(boolean compact) throws Exception {
    return addCustomAmdSourceSection("KIT-DM-AMD", "KIT-DM-BASEMETADATA", MDTYPE.OTHER, "KIT-DM-BASEMETADATA", createBaseMetadataDocument(compact));
  }

  /**
   * Create the default data organization section as administrative metadata.
   * The provided objectNodeResolver allows to resolve the contained data
   * organization node's LFNs to publicly accessible URLs. If no resolver is
   * provided, the internally stored LFNs are used.
   *
   * @param objectNodeResolver Resolver function or null.
   *
   * @return The builder instance.
   *
   * @throws Exception If anything goes wrong.
   */
  public MetsBuilder createDOSection(Function<IDataOrganizationNode, String> objectNodeResolver) throws Exception {
    return addCustomAmdSourceSection("KIT-DM-AMD", "KIT-DM-DATAORGANIZATION", MDTYPE.OTHER, "KIT-DM-DATAORGANIZATION", createDataOrganizationDocument(objectNodeResolver));
  }

  /**
   * Create the default provenance section as administrative metadata. (Not
   * implement, yet)
   *
   * @return The builder instance.
   *
   * @throws Exception If anything goes wrong.
   */
  public MetsBuilder createProvSection() throws Exception {
    throw new UnsupportedOperationException("Not implemented, yet.");
    //  return addCustomAmdDigiProvSection("DIGITAL-OBJECT-0", "KIT-DM-PREMIS-0", "MDTYPE.PREMIS_OBJECT, null, createProvenanceDocument());
  }

  /**
   * Create the default structure section containing fileSec and structMap METS
   * elements. The provided objectNodeResolver allows to resolve the contained
   * data organization node's LFNs to publicly accessible URLs. If no resolver
   * is provided, the internally stored LFNs are used.
   *
   * @param objectNodeResolver Resolver function or null.
   *
   * @return The builder instance.
   *
   * @throws Exception If anything goes wrong.
   */
  public MetsBuilder createStructureSection(Function<IDataOrganizationNode, String> objectNodeResolver) throws Exception {
    LOGGER.debug("Creating structure section.");
    //String[] views = new String[]{"default", "dummy"};
    Map<String, Map<String, String>> viewFileMap = new HashMap<>();
    List<StructMap> structs = new ArrayList<>();
    DataOrganizer org = DataOrganizerFactory.getInstance().getDataOrganizer();

    List<String> views = org.getViews(theObject.getDigitalObjectId());
    for (String view : views) {
      LOGGER.debug("Obtaining file tree of data organization view {}.", view);
      IFileTree tree = org.loadFileTree(theObject.getDigitalObjectId(), view);
      //IFileTree tree = DataOrganizationUtils.createTreeFromFile(theObject.getDigitalObjectIdentifier(), new AbstractFile(new java.io.File("src")), false);
      tree.setViewName(view);
      Map<String, String> fileMap = new HashMap<>();
      LOGGER.debug("Creating structure map for view {}.", view);
      structs.add(createStructMapForTree(tree, objectNodeResolver, fileMap));
      LOGGER.debug("Storing structure map for view {}.", view);
      viewFileMap.put(view, fileMap);
    }

    FileSec fsec = mets.newFileSec();
    FileGrp allFiles = fsec.newFileGrp();
    allFiles.setID("KIT-DM-FILE-GROUP");
    for (String view : views) {
      LOGGER.debug("Adding files of view {} to file group.", view);
      Map<String, String> fileMap = viewFileMap.get(view);
      Set<String> keys = fileMap.keySet();
      for (String key : keys) {
        File file = allFiles.newFile();
        file.setID(key);
        FLocat flocat = file.newFLocat();
        flocat.setLocType("URL");
        flocat.setHref(fileMap.get(key));
        file.addFLocat(flocat);
        allFiles.addFile(file);
      }
    }
    LOGGER.debug("Adding file group KIT-DM-FILE-GROUP to file section.");
    fsec.addFileGrp(allFiles);

    LOGGER.debug("Putting file section to mets document.");
    mets.setFileSec(fsec);

    LOGGER.debug("Putting struct maps to mets document.");
    structs.stream().forEach((struct) -> {
      mets.addStructMap(struct);
    });

    return this;
  }

  /**
   * Add a custom provenance section with a defined mdType as administrative
   * metadata .
   *
   * @param amdId The administrative metadata id this section belongs to.
   * @param digiProvId The id of this digital provenance section.
   * @param mdType The contained metadata type identifier, e.g. PREMIS.
   * @param xmlContent The XML document containing the provenance information
   * integrated as xmlData.
   *
   * @return The builder instance.
   *
   * @throws METSException If creating the METS structure failed.
   * @throws ParserConfigurationException If creating the Dublin Core document
   * failed.
   */
  public MetsBuilder addCustomAmdDigiProvSection(String amdId, String digiProvId, MDTYPE mdType, Document xmlContent) throws METSException, ParserConfigurationException {
    return addCustomAmdDigiProvSection(amdId, digiProvId, mdType, null, xmlContent);
  }

  /**
   * Add a custom provenance section as administrative metadata.
   *
   * @param amdId The administrative metadata id this section belongs to.
   * @param digiProvId The id of this digital provenance section.
   * @param mdType The contained metadata type identifier, e.g. OTHER
   * @param otherMdType The metadata type if OTHER is provided as type.
   * @param xmlContent The XML document containing the provenance information
   * integrated as xmlData.
   *
   * @return The builder instance.
   *
   * @throws METSException If creating the METS structure failed.
   * @throws ParserConfigurationException If creating the Dublin Core document
   * failed.
   */
  public MetsBuilder addCustomAmdDigiProvSection(String amdId, String digiProvId, MDTYPE mdType, String otherMdType, Document xmlContent) throws METSException, ParserConfigurationException {
    LOGGER.debug("Adding digi prov section with id {} to amd section with id {}. ", digiProvId, amdId);
    AmdSec amd = mets.getAmdSec(amdId);
    if (amd == null) {
      amd = mets.newAmdSec();
    }
    amd.setID(amdId);
    DigiprovMD dpmd = amd.newDigiprovMD();
    dpmd.setID(digiProvId);
    MdWrap dpw = dpmd.newMdWrap();
    if (mdType == null) {
      LOGGER.debug("Setting undefined MDType to {}.", MDTYPE.OTHER);
      dpw.setMDType(MDTYPE.OTHER.toString());
    } else {
      LOGGER.debug("Setting MDType to {}.", mdType);
      dpw.setMDType(mdType.toString());
    }
    if (otherMdType != null) {
      LOGGER.debug("Setting OtherMD-Type to {}.", otherMdType);
      dpw.setOtherMDType(otherMdType);
    } else {
      LOGGER.debug("No OtherMD-Type provided.");
    }
    LOGGER.debug("Setting provided XML data.");
    dpw.setMIMEType("text/xml");
    dpw.setXmlData(xmlContent.getDocumentElement());
    dpmd.setMdWrap(dpw);
    LOGGER.debug("Adding digi prov section to mets document.");
    amd.addDigiprovMD(dpmd);
    mets.addAmdSec(amd);
    return this;
  }

  /**
   * Add a basic mets header section.
   *
   * @param creator The creator used as agent in the header.
   *
   * @return The builder instance.
   *
   * @throws METSException If creating the METS structure failed.
   */
  public MetsBuilder createMetsHeader(UserData creator) throws METSException {
    LOGGER.debug("Creating mets header.");
    mets.setObjID(theObject.getDigitalObjectIdentifier());
    LOGGER.debug("Setting linked profile to {}. ", metsProfile);
    mets.setProfile(metsProfile);
    mets.setType(theObject.getClass().getName());
    MetsHdr mh = mets.newMetsHdr();
    String currentTime = df.format(cal.getTime());
    mh.setCreateDate(currentTime);
    mh.setLastModDate(currentTime);
    if (creator != null) {
      LOGGER.debug("Setting creator to {}.", creator);
      Agent a = mh.newAgent();
      a.setRole("CREATOR");
      a.setType("OTHER");
      a.setName(creator.getFullname());
      mh.addAgent(a);
    }
    LOGGER.debug("Setting mets header.");
    mets.setMetsHdr(mh);
    return this;
  }

  /**
   * Add a custom descriptive metadata section with a defined md type.
   *
   * @param dmdId The descriptive metadata id of this section.
   * @param mdType The contained metadata type identifier, e.g. OTHER
   * @param xmlContent The XML document containing the provenance information
   * integrated as xmlData.
   *
   * @return The builder instance.
   *
   * @throws METSException If creating the METS structure failed.
   * @throws ParserConfigurationException If creating the Dublin Core document
   * failed.
   */
  public MetsBuilder addCustomDmdSection(String dmdId, MDTYPE mdType, Document xmlContent) throws METSException, ParserConfigurationException {
    return addCustomDmdSection(dmdId, mdType, null, xmlContent);
  }

  /**
   * Add a custom descriptive metadata section.
   *
   * @param dmdId The descriptive metadata id of this section.
   * @param mdType The contained metadata type identifier, e.g. OTHER
   * @param otherMdType The metadata type if OTHER is provided as type.
   * @param xmlContent The XML document containing the provenance information
   * integrated as xmlData.
   *
   * @return The builder instance.
   *
   * @throws METSException If creating the METS structure failed.
   * @throws ParserConfigurationException If creating the Dublin Core document
   * failed.
   */
  public MetsBuilder addCustomDmdSection(String dmdId, MDTYPE mdType, String otherMdType, Document xmlContent) throws METSException, ParserConfigurationException {
    LOGGER.debug("Adding dmd section with id {}", dmdId);
    DmdSec dmd = mets.getDmdSec(dmdId);
    if (dmd == null) {
      dmd = mets.newDmdSec();
    }
    dmd.setID(dmdId);
    MdWrap mdw = dmd.newMdWrap();
    if (mdType == null) {
      LOGGER.debug("Setting undefined MDType to {}.", MDTYPE.OTHER);
      mdw.setMDType(MDTYPE.OTHER.toString());
    } else {
      LOGGER.debug("Setting MDType to {}.", mdType);
      mdw.setMDType(mdType.toString());
    }
    if (otherMdType != null) {
      LOGGER.debug("Setting OtherMd-Type to {}.", otherMdType);
      mdw.setOtherMDType(otherMdType);
    } else {
      LOGGER.debug("No OtherMD-Type provided.");
    }

    LOGGER.debug("Setting provided XML data.");
    mdw.setMIMEType("text/xml");
    mdw.setXmlData(xmlContent.getDocumentElement());
    dmd.setMdWrap(mdw);
    LOGGER.debug("Adding dmd section to mets document.");
    mets.addDmdSec(dmd);
    return this;
  }

  /**
   * Add a custom source metadata section with a defined md type as
   * administrative metadata.
   *
   * @param amdId The administrative metadata id this section belongs to.
   * @param sourceId The id of this source section.
   * @param mdType The contained metadata type identifier, e.g. OTHER
   * @param xmlContent The XML document containing the provenance information
   * integrated as xmlData.
   *
   * @return The builder instance.
   *
   * @throws METSException If creating the METS structure failed.
   * @throws ParserConfigurationException If creating the Dublin Core document
   * failed.
   */
  public MetsBuilder addCustomAmdSourceSection(String amdId, String sourceId, MDTYPE mdType, Document xmlContent) throws METSException, ParserConfigurationException {
    return addCustomAmdSourceSection(amdId, sourceId, mdType, null, xmlContent);
  }

  /**
   * Add a custom source metadata section as administrative metadata.
   *
   * @param amdId The administrative metadata id this section belongs to.
   * @param sourceId The id of this source section.
   * @param mdType The contained metadata type identifier, e.g. OTHER
   * @param otherMdType The metadata type if OTHER is provided as type.
   * @param xmlContent The XML document containing the provenance information
   * integrated as xmlData.
   *
   * @return The builder instance.
   *
   * @throws METSException If creating the METS structure failed.
   * @throws ParserConfigurationException If creating the Dublin Core document
   * failed.
   */
  public MetsBuilder addCustomAmdSourceSection(String amdId, String sourceId, MDTYPE mdType, String otherMdType, Document xmlContent) throws METSException, ParserConfigurationException {
    LOGGER.debug("Adding amd section with id {}", amdId);
    AmdSec amd = mets.getAmdSec(amdId);
    if (amd == null) {
      amd = mets.newAmdSec();
    }
    amd.setID(amdId);
    if (amd.hasSourceMD(sourceId)) {
      LOGGER.info("Replace existing AMD section with id '{}'.", sourceId);
      amd.removeSourceMD(sourceId);
    }
    SourceMD smd = amd.newSourceMD();
    smd.setID(sourceId);
    MdWrap smdw = smd.newMdWrap();
    if (mdType == null) {
      LOGGER.debug("Setting undefined MDType to {}.", MDTYPE.OTHER);
      smdw.setMDType(MDTYPE.OTHER.toString());
    } else {
      LOGGER.debug("Setting MDType to {}.", mdType);
      smdw.setMDType(mdType.toString());
    }
    if (otherMdType != null) {
      LOGGER.debug("Setting OtherMd-Type to {}.", otherMdType);
      smdw.setOtherMDType(otherMdType);
    } else {
      LOGGER.debug("No OtherMD-Type provided.");
    }
    LOGGER.debug("Setting provided XML data.");
    smdw.setMIMEType("text/xml");
    smdw.setXmlData(xmlContent.getDocumentElement());
    smd.setMdWrap(smdw);
    amd.addSourceMD(smd);
    LOGGER.debug("Adding amd section to mets document.");
    mets.addAmdSec(amd);
    return this;
  }

  /**
   * Write the current METS document to the provided output stream, e.g. for
   * debugging.
   *
   * @param out The output stream.
   *
   * @return The builder instance.
   */
  public MetsBuilder write(OutputStream out) {
    mw.write(out);
    return this;
  }

  /**
   * Create the base metadata document.
   *
   * @param compact Use the compact version of the base metadata omitting all
   * information of study and investigation but their ids.
   *
   * @return The base metadata Document.
   *
   * @throws Exception If anything goes wrong.
   */
  private Document createBaseMetadataDocument(boolean compact) throws Exception {
    LOGGER.debug("Creating base metadata document. Mode is: {}", (compact) ? "compact" : "detailed");

    LOGGER.debug("Marshalling digital object to XML.");
    Marshaller marshaller = org.eclipse.persistence.jaxb.JAXBContext.newInstance(DigitalObject.class).createMarshaller();
    if (compact) {
      marshaller.setProperty(MarshallerProperties.OBJECT_GRAPH, "default");
    }

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    marshaller.marshal(theObject, bout);

    LOGGER.debug("Creating document from XML.");
    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
    fac.setNamespaceAware(true);
    DocumentBuilder builder = fac.newDocumentBuilder();
    LOGGER.trace(new String(bout.toByteArray()));
    Document doc = builder.parse(new ByteArrayInputStream(bout.toByteArray()));
    LOGGER.debug("Creating empty basemetadata document.");
    Document bmdDoc = builder.newDocument();
    LOGGER.debug("Creating basemetadata root node.");
    Element root = bmdDoc.createElementNS(DAMA_NAMESPACE_BASEMETADATA, BASEMETADATA_ROOT_ELEMENT);
    root.setAttribute(XSI_NAMESPACE_PREFIX, NAMESPACE_XSI); 
    root.setAttribute(SCHEMALOCATION, DAMA_NAMESPACE_BASEMETADATA + " " + BASEMETADATA_XSD); 
    LOGGER.debug("Appending root node to document.");
    bmdDoc.appendChild(root);
    LOGGER.debug("Adopting marshalled root node by basemetadata document.");
    Node adopted = bmdDoc.adoptNode(doc.getDocumentElement());
    LOGGER.debug("Appending adopted node to basemetadata document.");
    root.appendChild(adopted);
    LOGGER.debug("Updating namespaces.");
    renameNamespaceRecursive(bmdDoc, adopted, DAMA_NAMESPACE_BASEMETADATA);
    LOGGER.debug("Returning basemetadata document.");
    return bmdDoc;
  }

  /**
   * Create the structure map section for the provided data organization tree.
   * The provided objectNodeResolver allows to resolve the contained data
   * organization node's LFNs to publicly accessible URLs. If no resolver is
   * provided, the internally stored LFNs are used.
   *
   * All file nodes that where found in the provided tree are stored in the
   * provided file map. As soon as all data organization views are handled, this
   * file map is used to add the fileSec section to the METS document before the
   * struct map sections are written to the document. This is due to the fact,
   * that file fileSec entries are references by the structMap sections and must
   * be located in the METS document before the strutMaps start.
   *
   * @param tree The file tree to create a structure map section for.
   * @param objectNodeResolver Resolver function or null.
   * @param fileMap A map of the files contained in the provided tree.
   *
   * @return The structure map section.
   *
   * @throws METSException If creating the METS structure failed.
   */
  private StructMap createStructMapForTree(IFileTree tree, Function<IDataOrganizationNode, String> objectNodeResolver, Map<String, String> fileMap) throws METSException {
    LOGGER.debug("Creating struct map for data organization tree.");
    StructMap map = mets.newStructMap();
    LOGGER.debug("Setting struct map label to {}.", tree.getViewName());
    map.setLabel(tree.getViewName());
    if ("default".equals(map.getLabel())) {
      LOGGER.debug("Setting id KIT-DM-FILE-VIEW to struct map with label 'default'");
      map.setID("KIT-DM-FILE-VIEW");
    } else {
      LOGGER.debug("Setting id KIT-DM-FILE-VIEW-" + structId + " to struct map with label 'default'");
      map.setID("KIT-DM-FILE-VIEW-" + structId);
      structId++;
    }
    LOGGER.debug("Creating new div with label 'root'");
    LOGGER.debug("Adding tree nodes recursively.");
    Div rootDiv = addNodeToStructMap(tree.getRootNode(), map, null, objectNodeResolver, fileMap);
    LOGGER.debug("Adding root div to struct map.");
    map.addDiv(rootDiv);
    return map;
  }

  /**
   * Add a single data organization node to the struct map at the currentDiv
   * position. This method is called by {@link #createStructMapForTree(edu.kit.dama.mdm.dataorganization.entity.core.IFileTree, java.util.function.Function, java.util.Map)
   * } and calls itself recursively for each node of the tree. This method also
   * creates a unique node id for each node and stored this id, to be used later
   * on in the fileSec section, in the file map together with the LFN or the
   * publicly accessible node URL obtained from the provided objectNodeResolver.
   *
   * @param node The current node.
   * @param map The parent struct map.
   * @param currentDiv The parent div section of this node within the struct
   * map.
   * @param objectNodeResolver Resolver function or null.
   * @param fileMap A map of the files contained in the parent tree.
   *
   * @throws METSException If creating the METS structure failed.
   */
  private Div addNodeToStructMap(IDataOrganizationNode node, StructMap map, Div currentDiv, Function<IDataOrganizationNode, String> objectNodeResolver, Map<String, String> fileMap) throws METSException {
    LOGGER.debug("Adding data organiztion node to struct map.");
    String nodeName = node.getName();
    if (node instanceof IFileNode) {
      String fid = "FILE-" + this.fileId;
      LOGGER.debug("Adding new file node with id {}. Resolving node url.", fid);
      String nodeUrl = (objectNodeResolver != null) ? objectNodeResolver.apply(node) : ((IFileNode) node).getLogicalFileName().getStringRepresentation();
      LOGGER.debug("Node url is resolved to {}", nodeUrl);
      fileMap.put(fid, nodeUrl);

      Div divToAdd = currentDiv;
      if (divToAdd == null) {
        LOGGER.warn("Detected file nodes in no collection, creating 'root' div.");
        divToAdd = map.newDiv();
        divToAdd.setType("folder");
        divToAdd.setLabel("root");
      }

      Fptr fpts = divToAdd.newFptr();
      fpts.setFileID(fid);
      LOGGER.debug("Adding filePtr to current div section.");
      divToAdd.addFptr(fpts);
      this.fileId++;
      return divToAdd;
    } else if (node instanceof ICollectionNode) {
      LOGGER.debug("Adding new collection node. Sorting children to handle collection nodes first.");
      List<? extends IDataOrganizationNode> children = ((ICollectionNode) node).getChildren();
      children.sort((IDataOrganizationNode o1, IDataOrganizationNode o2) -> {
        int i1 = 0;
        int i2 = 0;
        if (o1 instanceof ICollectionNode) {
          i1 = 1;
        }
        if (o2 instanceof ICollectionNode) {
          i2 = 1;
        }
        return Integer.compare(i1, i2);
      });

      LOGGER.debug("Creating new collection node div.");
      Div newDiv = map.newDiv();
      newDiv.setType("folder");

      if (nodeName != null) {
        LOGGER.debug("Setting div label to {}.", nodeName);
        newDiv.setLabel(nodeName);
      } else {
        newDiv.setLabel("root");
      }
      LOGGER.debug("Adding node's children recursively.");
      for (IDataOrganizationNode child : children) {
        addNodeToStructMap(child, map, newDiv, objectNodeResolver, fileMap);
      }
      LOGGER.debug("Adding collection node div to parent div.");
      if (currentDiv != null) {
        currentDiv.addDiv(newDiv);
      } else {
        return newDiv;
      }
    }
    return currentDiv;
  }

  /**
   * Create the provenance document. (Not implemented, yet)
   *
   * @return The provenance Document.
   *
   * @throws ParserConfigurationException If creating the Dublin Core document
   * failed.
   */
  private Document createProvenanceDocument() throws ParserConfigurationException {
    //@TODO
    return null;
  }

  /**
   * Create the data organization document for the administrative metadata
   * section. The provided objectNodeResolver allows to resolve the contained
   * data organization node's LFNs to publicly accessible URLs. If no resolver
   * is provided, the internally stored LFNs are used.
   *
   * @param objectNodeResolver Resolver function or null.
   *
   * @return The document.
   *
   * @throws Exception If anything goes wrong.
   */
  public Document createDataOrganizationDocument(Function<IDataOrganizationNode, String> objectNodeResolver) throws Exception {
    return Util.dataOrganizationToXml(theObject.getDigitalObjectId(), objectNodeResolver);
    /*IFileTree ttree = DataOrganizationUtils.createTreeFromFile("1234-abcd-efgh-5678", new AbstractFile(new java.io.File("/Users/jejkal/NetBeansProjects/KITDM/trunk/Utils/src/main/java/edu/kit/dama/util/jaxb")), true);
        ttree.setViewName("default");
        return Util.fileTreeToXml(ttree);*/
  }

  /**
   * Helper method to rename a node's namespace recursively.
   *
   * @param doc The owner document.
   * @param node The node.
   * @param namespace The new namespace.
   */
  private void renameNamespaceRecursive(Document doc, Node node, String namespace) {
    if (node.getNodeType() == Node.ELEMENT_NODE) {
      doc.renameNode(node, namespace, node.getNodeName());
    }

    NodeList list = node.getChildNodes();
    for (int i = 0; i < list.getLength(); ++i) {
      renameNamespaceRecursive(doc, list.item(i), namespace);
    }
  }

}
