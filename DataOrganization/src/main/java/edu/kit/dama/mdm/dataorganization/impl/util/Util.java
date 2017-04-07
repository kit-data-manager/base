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
package edu.kit.dama.mdm.dataorganization.impl.util;

import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNodeVisitor;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.impl.staging.AttributeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.DataOrganizationNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileTreeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.util.Constants;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Utility methods for the implementation.
 *
 * @author pasic
 */
public final class Util {

    private static String DO_SCHEMA = "http://datamanager.kit.edu/dama/dataorganization/2015-08/dataorganization.xsd";
    private static String DO_NS = "http://datamanager.kit.edu/dama/dataorganization";

    /**
     * Hidden constructor.
     */
    private Util() {
    }

    /**
     * Walks a subtree.
     *
     * @param node The node to start with.
     * @param preorderAction preorderAction.action(thisNode) will be called
     * before processing the children of thisNode
     * @param postorderAction postorder.action(thisNode) is called after
     * processing the children of thisNode (postorderAction if not null)
     */
    public static void walkSubtree(IDataOrganizationNode node,
            IDataOrganizationNodeVisitor preorderAction,
            IDataOrganizationNodeVisitor postorderAction) {
        if (null != preorderAction) {
            preorderAction.action(node);
        }
        if (node instanceof ICollectionNode) {
            ICollectionNode coll = (ICollectionNode) node;
            for (IDataOrganizationNode n : coll.getChildren()) {
                walkSubtree(n, preorderAction, postorderAction);
            }
        }
        if (null != postorderAction) {
            postorderAction.action(node);
        }
    }

    /**
     * Makes a deep copy of the subtree.
     *
     * @param don Root node of the subtree.
     * @param parent The parent. Null at beginning.
     *
     * @throws CloneNotSupportedException Clone is not supported.
     */
    public static void walkAndClone(IDataOrganizationNode don, ICollectionNode parent) throws CloneNotSupportedException {
        if (null != parent) {
            parent.addChild(don.clone());
        }
        if (don instanceof ICollectionNode) {
            for (IDataOrganizationNode child : ((ICollectionNode) don).getChildren()) {
                walkAndClone(child, (ICollectionNode) don);
            }
        }
    }

    /**
     * Gets the a first node with the provided name from the provided subtree.
     *
     * @param subtree The subtree to search.
     * @param name The node name to search for.
     *
     * @return The found node or null if no node was found.
     */
    public static IDataOrganizationNode getNodeByName(ICollectionNode subtree, String name) {
        SearchVisitor searchVisitor = new SearchVisitor(name);
        try {
            walkSubtree(subtree, searchVisitor, null);
        } catch (SearchVisitor.NodeFoundException e) {
            return e.getNode();
        }
        return null;
    }

    /**
     * Produces a JSON representation of the provided file tree representing the
     * view with the provided name. The file tree must consist of collection
     * nodes, that may or may not be already persisted, and file nodes that MUST
     * be already persisted for the digital object the file tree will be
     * associated with. If a JSON element provides a nodeId it means the the
     * content of the node equals the existing node. This applies to all file
     * nodes. If the node defines a name this means that it is a new node.
     *
     * A sample result object may looks as follows:
     * <pre>
     * {
     *"objectId":"1234-5678-abcd",
     *"viewName":"custom",
     *"children":[
     *    {
     *       "nodeId":100,
     *       "type":"CollectionNode"
     *       "attributes":[
     *          {
     *              "childAttrib":"customNewAttribute"
     *          }
     *      ]
     *   },{
     *      "name":"newCollectionNode",
     *      "type":"CollectionNode"
     *      "children":[
     *          {
     *              "nodeId":200,
     *              "type":"FileNode",
     *              "attributes":[
     *                  {
     *                      "subChildAttrib":"myValue"
     *                  }
     *              ]
     *          },
     *          {
     *             "nodeId":300,
     *             "type":"FileNode"
     *          }
     *      ]
     *    }
     * ]
     * }
     * </pre>
     *
     * @param pTree The file tree to convert to JSON
     *
     * @return A JSONObject representing pTree.
     */
    public static JSONObject fileTreeToJsonView(IFileTree pTree) {
        if (pTree == null) {
            throw new IllegalArgumentException("Argument pTree must not be null.");
        }
        if (pTree.getDigitalObjectId() == null || pTree.getViewName() == null) {
            throw new IllegalArgumentException("Neither objectId nor view name of pTree must be null.");
        }

        JSONObject jsonTree = new JSONObject();
        jsonTree.putOnce("objectId", pTree.getDigitalObjectId().toString());
        jsonTree.putOnce("viewName", pTree.getViewName());

        for (IDataOrganizationNode child : ((ICollectionNode) pTree.getRootNode()).getChildren()) {
            jsonTree.append("children", nodeToJson(child));
        }
        return jsonTree;
    }

    /**
     * Transform the provided data organization node to a JSON representation.
     * If pNode is a CollectionNode, the method is called recursively for all of
     * its child nodes. If pNode is a FileNode and has a nodeId assigned, only
     * the node id is added to the JSONObject. For FileNodes without nodeId the
     * name and the lfn is added.
     *
     * @param pNode The node to transform.
     *
     * @return The JSONObject repesenting the node.
     */
    private static JSONObject nodeToJson(IDataOrganizationNode pNode) {
        JSONObject object = new JSONObject();
        if (pNode instanceof ICollectionNode) {
            if (pNode.getTransientNodeId() != null) {
                object.putOnce("nodeId", pNode.getTransientNodeId().getInTreeId());
            } else {
                object.putOnce("name", pNode.getName());
            }
            object.putOnce("type", "CollectionNode");
            for (IDataOrganizationNode child : ((ICollectionNode) pNode).getChildren()) {
                object.append("children", nodeToJson(child));
            }
        } else if (pNode instanceof IFileNode) {
            if (pNode.getTransientNodeId() != null) {
                // throw new IllegalArgumentException("Only FileNodes with a nodeId are supported.");
                object.putOnce("nodeId", pNode.getTransientNodeId().getInTreeId());
                object.putOnce("type", "FileNode");
            } else {
                object.putOnce("name", pNode.getName());
                object.putOnce("lfn", ((IFileNode) pNode).getLogicalFileName().asString());
                object.putOnce("type", "FileNode");
            }
        }

        if (!pNode.getAttributes().isEmpty()) {
            for (IAttribute attribute : pNode.getAttributes()) {
                JSONObject attrib = new JSONObject();
                attrib.putOnce(attribute.getKey(), attribute.getValue());
                object.append("attributes", attrib);
            }
        }
        return object;
    }

    public static IFileTree jsonViewToFileTree(JSONObject pJsonTree, boolean preserveAttributes) throws InvalidNodeIdException {
        return jsonViewToFileTree(pJsonTree, preserveAttributes, true);
    }

    public static IFileTree jsonViewToFileTree(JSONObject pJsonTree, boolean preserveAttributes, boolean pStrict) throws InvalidNodeIdException {
        IFileTree tree = new FileTreeImpl();
        String objectId = pJsonTree.optString("objectId", null);
        String viewName = pJsonTree.getString("viewName");
        DigitalObjectId oid = (objectId == null) ? null : new DigitalObjectId(objectId);
        tree.setDigitalObjectId(oid);
        tree.setViewName(viewName);
        JSONArray children = pJsonTree.getJSONArray("children");
        for (int i = 0; i < children.length(); i++) {
            tree.getRootNode().addChild(jsonToNode((JSONObject) children.get(i), oid, preserveAttributes, pStrict));
        }

        return tree;
    }

    /**
     * Transform the provided JSON object back to a DataOrganization node. For
     * the transformation there are some rules. If pObject is of type FileNode
     * it must provide a nodeId referring to an existing file node in the
     * 'default' view of the DataOrganization of the object with the object id
     * 'oid'. The content of this node (LFN, name and optionally the attributes)
     * are copied to the result node. If pObject is of type CollectionNode there
     * may or may not be a nodeId provided. If a nodeId is provided it has to
     * refer to abn existing collection node. The content of this node is then
     * copied to the result node, including name, all children and optionally
     * the attributes. If pObject provides additional children they are added
     * afterwards in addition to the existing children to the result node.
     *
     * @param pObject The JSON object to transform.
     * @param oid The digital object id of the digital object providing the data
     * organization the resulting node should be associated with.
     * @param preserveAttributes If TRUE all attributes of an existing nodes
     * will be copied to the resulting node. Otherwise, the resulting node will
     * just have the attributes assigned that are defined in pObject.
     * @param pStrict If TRUE it will be checked that nodes referenced by id
     * have the same type as defined in the JSONObject. Furthermore, it will be
     * checked that file nodes are provided by reference only. If FALSE, also
     * file nodes with custom LFNs may be provided. (default: true)
     *
     * @return The transformed DataOrganizationNode.
     *
     * @throws InvalidNodeIdException If pObject or any child provides a nodeId
     * that does not refer to an existing node.
     */
    private static IDataOrganizationNode jsonToNode(JSONObject pObject, DigitalObjectId oid, boolean preserveAttributes, boolean pStrict) throws InvalidNodeIdException {
        IDataOrganizationNode node;
        String type = pObject.getString("type");
        Long nodeId = pObject.optLong("nodeId", -1l);
        DataOrganizer org = DataOrganizerFactory.getInstance().getDataOrganizer();
        IDataOrganizationNode defaultNode = null;
        NodeId nid = null;
        if (nodeId >= 0) {
            if (oid == null) {
                throw new IllegalArgumentException("ObjectId must not be null if requesting to load nodes from json by id.");
            }
            nid = new NodeId(oid, nodeId, 1, "default");
            defaultNode = org.loadNode(nid);
        }
        if ("CollectionNode".equals(type)) {
            if (pStrict && (nodeId >= 0 && !(defaultNode instanceof ICollectionNode))) {
                throw new IllegalArgumentException("Type conflict. The provided JSON node is of type CollectionNode but the DataOrganization node with node id " + nodeId + " is not.");
            }
            node = new CollectionNodeImpl();
            if (defaultNode != null) {
                //existing collection node, 
                node.setName(defaultNode.getName());
                IFileTree subTree = org.loadSubTree(nid, 12345);
                for (IDataOrganizationNode child : subTree.getRootNode().getChildren()) {
                    ((ICollectionNode) node).addChild(copyNode(child, preserveAttributes));
                }
            } else {
                //new node...apply name and attributes
                String name = pObject.getString("name");
                node.setName(name);
            }
            //handling children
            JSONArray children = pObject.optJSONArray("children");
            if (children != null) {
                for (int i = 0; i < children.length(); i++) {
                    ((ICollectionNode) node).addChild(jsonToNode((JSONObject) children.get(i), oid, preserveAttributes, pStrict));
                }
            }
        } else if ("FileNode".equals(type)) {
            if (pStrict && defaultNode == null) {
                throw new IllegalArgumentException("The provided JSON node is of type FileNode but has no nodeId assigned.");
            }

            if (pStrict && !(defaultNode instanceof IFileNode)) {
                throw new IllegalArgumentException("Type conflict. The provided JSON node is of type FileNode but the DataOrganization node with node id " + nodeId + " is not.");
            }
            if (defaultNode != null) {
                node = copyNode(defaultNode, preserveAttributes);
            } else {
                String name = pObject.getString("name");
                String lfn = pObject.getString("lfn");
                LFNImpl lfnImpl = new LFNImpl(lfn);
                node = new FileNodeImpl(lfnImpl);
                node.setName(name);
            }
        } else {
            throw new IllegalArgumentException("Nodes of type '" + type + "' are currently not supported.");
        }

        JSONArray attributes = pObject.optJSONArray("attributes");
        if (attributes != null) {
            for (int i = 0; i < attributes.length(); i++) {
                JSONObject attribute = (JSONObject) attributes.get(i);
                Iterator keys = attribute.keys();
                if (keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = (String) attribute.getString(key);
                    node.addAttribute(new AttributeImpl(key, value));
                }
            }
        }

        return node;
    }

    /**
     * Copy a single node including all children and attributes.
     *
     * @param <C> An instance of DataOrganizationNodeImpl.
     * @param pNode The node to copy.
     * @param preserveAttributes If TRUE all attributes of pNode and optional
     * children are also copied.
     *
     * @return An exact copy of (all selected nodes below) pNode.
     */
    public static <C extends DataOrganizationNodeImpl> C copyNode(IDataOrganizationNode pNode, boolean preserveAttributes) {
        IDataOrganizationNode result = null;
        if (pNode == null) {
            throw new IllegalArgumentException("Argument pNode must not be null.");
        }

        if (pNode instanceof IFileNode) {
            result = new FileNodeImpl(((IFileNode) pNode).getLogicalFileName());
        } else if (pNode instanceof IFileTree) {
            result = new FileTreeImpl();
            ((IFileTree) result).setDigitalObjectId(((IFileTree) pNode).getDigitalObjectId());
            for (IDataOrganizationNode child : ((IFileTree) pNode).getRootNode().getChildren()) {
                ((FileTreeImpl) result).getRootNode().addChild(copyNode(child, preserveAttributes));
            }
        } else if (pNode instanceof ICollectionNode) {
            result = new CollectionNodeImpl();
            ICollectionNode colNode = (ICollectionNode) pNode;
            for (IDataOrganizationNode child : colNode.getChildren()) {
                ((CollectionNodeImpl) result).addChild(copyNode(child, preserveAttributes));
            }
        }

        if (result == null) {
            throw new IllegalArgumentException("Argument 'pNode' must be an instance of IFileTree, ICollectionNode or IFileNode.");
        }

        result.setName(pNode.getName());
        if (preserveAttributes) {
            result.setDescription(pNode.getDescription());
            Set<? extends IAttribute> attributes = pNode.getAttributes();
            Set<AttributeImpl> attributeCopy = new HashSet<>();
            for (IAttribute a : attributes) {
                attributeCopy.add(new AttributeImpl(a.getKey(), a.getValue()));
            }
            result.setAttributes(attributeCopy);
        }
        return (C) result;
    }

    /**
     * Check if the provided view name is reserved by the system or if it can be
     * used for custom views. Reserved view names are for example 'data',
     * 'default' and 'generated'.
     *
     * @param pName The view name to check.
     *
     * @return TRUE if the name is reserved and is not allowed to be used by
     * custom views.
     */
    public static boolean isReservedViewName(String pName) {
        return Constants.DATA_VIEW.equals(pName) || Constants.GENERATED_VIEW.equals(pName) || Constants.DEFAULT_VIEW.equals(pName);
    }

    /**
     * Get all nodes which filenames match the regular expression from the
     * provided subtree.
     *
     * @param pSubtree The subtree to search.
     * @param pRegex The regular expression to search for.
     * @see SearchPatternVisitor
     * @return A set with the found nodes or empty set if no node was found.
     */
    public static Set<IDataOrganizationNode> getNodesByRegex(ICollectionNode pSubtree, String pRegex) {
        SearchPatternVisitor searchPatternVisitor = new SearchPatternVisitor(pRegex);
        walkSubtree(pSubtree, searchPatternVisitor, null);
        return searchPatternVisitor.getAllNodes();
    }

    public static Document fileTreeToXml(IFileTree tree) throws Exception {
        return fileTreeToXml(tree, null);
    }

    public static Document fileTreeToXml(IFileTree tree, Function<IDataOrganizationNode, String> objectNodeResolver) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new URL(DO_SCHEMA));
        factory.setSchema(schema);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElementNS(DO_NS, "dataOrganization");
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                "xs:schemaLocation", DO_NS + " " + DO_SCHEMA
        );

        doc.appendChild(root);
        Element doidElement = doc.createElementNS(DO_NS, "digitalObjectId");
        doidElement.setTextContent(tree.getDigitalObjectId().getStringRepresentation());
        root.appendChild(doidElement);

        root.appendChild(createViewElement(tree, doc, objectNodeResolver));
        return doc;
    }

    public static Document dataOrganizationToXml(DigitalObjectId pDigitalObjectId) throws Exception {
        return dataOrganizationToXml(pDigitalObjectId, null);
    }

    public static Document dataOrganizationToXml(DigitalObjectId pDigitalObjectId, Function<IDataOrganizationNode, String> objectNodeResolver) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(true);

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new URL(DO_SCHEMA));
        factory.setSchema(schema);

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElementNS(DO_NS, "dataOrganization");
        root.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance",
                "xs:schemaLocation", DO_NS + " " + DO_SCHEMA
        );

        doc.appendChild(root);
        Element doidElement = doc.createElementNS(DO_NS, "digitalObjectId");
        doidElement.setTextContent(pDigitalObjectId.getStringRepresentation());
        root.appendChild(doidElement);

        /*    String nodeUrl = (objectNodeResolver != null)? objectNodeResolver.apply(t, Long.MIN_VALUE);
        if (addExternalReferences) {
            baseUrl = DataManagerSettings.getSingleton().getStringProperty(DataManagerSettings.GENERAL_BASE_URL_ID, "http://localhost:8080");
            baseUrl += "/rest/dataorganization/organization/download/" + Long.toString(baseId) + "/";
        }
         */
        DataOrganizer org = DataOrganizerFactory.getInstance().getDataOrganizer();
        List<String> views = org.getViews(pDigitalObjectId);
        for (String view : views) {
            //load tree for view
            IFileTree tree = org.loadFileTree(pDigitalObjectId, view);
            root.appendChild(createViewElement(tree, doc, objectNodeResolver));
        }

        return doc;
    }

    private static Element createViewElement(IFileTree viewTree, Document doc, Function<IDataOrganizationNode, String> objectNodeResolver) throws Exception {
        Element result = doc.createElementNS(DO_NS, "view");
        result.setAttributeNS(DO_NS, "name", viewTree.getViewName());
        Element rootElement = doc.createElementNS(DO_NS, "root");
        addNodeToElement(viewTree.getRootNode(), rootElement, doc, objectNodeResolver);
        result.appendChild(rootElement);
        return result;
    }

    private static void addNodeToElement(IDataOrganizationNode node, Element parent, Document doc, Function<IDataOrganizationNode, String> objectNodeResolver) {
        Element nameElement = doc.createElementNS(DO_NS, "name");
        nameElement.setTextContent(node.getName());
        String nodeUrl = (objectNodeResolver != null) ? objectNodeResolver.apply(node) : null;

        boolean hasAttrib = false;
        Element attribElement = doc.createElementNS(DO_NS, "attributes");
        for (IAttribute attribute : node.getAttributes()) {
            Element attributeElement = doc.createElementNS(DO_NS, "attribute");
            Element keyElement = doc.createElementNS(DO_NS, "key");
            keyElement.setTextContent(attribute.getKey());
            Element valueElement = doc.createElementNS(DO_NS, "value");
            valueElement.setTextContent(attribute.getValue());
            attributeElement.appendChild(keyElement);
            attributeElement.appendChild(valueElement);
            attribElement.appendChild(attributeElement);
            hasAttrib = true;
        }

        if (node instanceof IFileNode) {
            Element lfnElement = doc.createElementNS(DO_NS, "logicalFileName");
            if (nodeUrl != null) {
                lfnElement.setTextContent(nodeUrl);
            } else {
                lfnElement.setTextContent(((IFileNode) node).getLogicalFileName().getStringRepresentation());
            }
            parent.appendChild(nameElement);
            parent.appendChild(lfnElement);
            if (hasAttrib) {
                parent.appendChild(attribElement);
            }
        } else if (node instanceof ICollectionNode) {
            parent.appendChild(nameElement);
            if (nodeUrl != null) {
                Element lfnElement = doc.createElementNS(DO_NS, "logicalFileName");
                lfnElement.setTextContent(nodeUrl);
                parent.appendChild(lfnElement);
            }
            if (hasAttrib) {
                parent.appendChild(attribElement);
            }
            Element childrenElement = doc.createElementNS(DO_NS, "children");
            for (IDataOrganizationNode child : ((ICollectionNode) node).getChildren()) {
                Element childElement = doc.createElementNS(DO_NS, "child");
                addNodeToElement(child, childElement, doc, objectNodeResolver);
                childrenElement.appendChild(childElement);
            }
            parent.appendChild(childrenElement);
        }
    }

}

/**
 * Utility class for search.
 *
 * @author pasic
 */
class SearchVisitor implements IDataOrganizationNodeVisitor {

    /**
     * Logger for the class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchVisitor.class);

    class NodeFoundException extends RuntimeException {

        /**
         * The node.
         */
        private final IDataOrganizationNode node;

        /**
         * Default constructor.
         *
         * @param node The node to return.
         */
        public NodeFoundException(IDataOrganizationNode node) {
            this.node = node;
        }

        /**
         * Returns the node.
         *
         * @return The node.
         */
        public IDataOrganizationNode getNode() {
            return node;
        }
    }

    /**
     * The node name.
     */
    private final String name;

    /**
     * Default constructor.
     *
     * @param name The name.
     */
    public SearchVisitor(String name) {
        this.name = name;
    }

    @Override
    public void action(IDataOrganizationNode node) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Testing node '{}' for name '{}'", node.getName(), name);
        }
        if (name.equals(node.getName())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Success! Found node with name '{}'.", node.getName());
            }
            throw new NodeFoundException(node);
        }
    }
}
