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
import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.impl.staging.AttributeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileTreeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.util.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author jejkal
 */
public class Neo4jHelper {

    public static final String ROOT_LABEL = "Root";
    public static final String COLLECTION_TYPE = "Collection";
    public static final String FILE_TYPE = "File";
    public static final String UNKNOWN_TYPE = "Unknown";

    public static Long persistFileTree(IFileTree pTree, JdbcCypherExecutor exec) {
        Iterator<Map<String, Object>> result = exec.query(buildCreateRootNodeQuery(pTree), null);
        long rootId = ((Number) result.next().get("ID(n)")).longValue();
        String view = (pTree.getViewName() != null) ? pTree.getViewName() : Constants.DEFAULT_VIEW;

//        Map<String, Object> params = new HashMap<>();
//        List<IDataOrganizationNode> nodes = DataOrganizationUtils.flattenNode(pTree.getRootNode());
//        int cnt = 1;
//        int nodeId = 0;
//        for (IDataOrganizationNode node : nodes) {
//            DataOrganizationNodeImpl impl = (DataOrganizationNodeImpl) node;
//            impl.setNodeId((long) nodeId);
//            nodeCreation.append(buildCreateNodeQueryAlt(node, pTree.getDigitalObjectId(), view, params)).append(" ");
//            if (cnt % 100 == 0) {
//                exec.query(nodeCreation.toString(), null);
//                nodeCreation = new StringBuilder();
//                cnt = 0;
//            }
//            cnt++;
//            nodeId++;
//        }
//        exec.query(nodeCreation.toString(), null);
//        cnt = 1;
//        //nodeCreation = new StringBuilder();
//        for (IDataOrganizationNode node : nodes) {
//            if (node instanceof ICollectionNode) {
//                for (IDataOrganizationNode child : ((ICollectionNode) node).getChildren()) {
//                    exec.query(buildCreateRelationQueryAlt(node.getTransientNodeId().getInTreeId(), child.getTransientNodeId().getInTreeId()), null);
//This does not work as we have multiple MATCH statements that cannot be combined in one query
//                    /*  nodeCreation.append(buildCreateRelationQueryAlt(node.getTransientNodeId().getInTreeId(), child.getTransientNodeId().getInTreeId())).append(" ");
//                    if (cnt % 100 == 0) {
//                        exec.query(nodeCreation.toString(), null);
//                        nodeCreation = new StringBuilder();
//                        cnt = 0;
//                    }
//                    cnt++;*/
//                }
//            }
//        }
        pTree.getRootNode().setViewName(view);
        for (IDataOrganizationNode child : pTree.getRootNode().getChildren()) {
            persistNode(child, pTree.getDigitalObjectId(), view, rootId, exec);
        }

        return rootId;
    }

    private static void persistNode(IDataOrganizationNode pNode, DigitalObjectId oid, String viewName, long parentId, JdbcCypherExecutor exec) {
        Map<String, Object> params = new HashMap<>();

        Iterator<Map<String, Object>> result = exec.query(buildCreateNodeQuery(pNode, oid, viewName, params), params);

        Long nodeId = ((Number) result.next().get("ID(n)")).longValue();
        //build sub structure if node is a collection node
        if (pNode instanceof ICollectionNode) {
            for (IDataOrganizationNode child : ((ICollectionNode) pNode).getChildren()) {
                persistNode(child, oid, viewName, nodeId, exec);
            }
        }
        //link node to parent
        exec.query(buildCreateRelationQuery(parentId, nodeId), null);
    }

    private static String buildCreateRootNodeQuery(IFileTree pTree) {
        StringBuilder b = new StringBuilder();
        String viewArgument = pTree.getViewName() != null ? pTree.getViewName() : Constants.DEFAULT_VIEW;
        if (pTree.getDigitalObjectId() == null) {
            throw new IllegalArgumentException("The provided tree must have a digital object identifier assigned.");
        }
        b.append("CREATE (n:`" + ROOT_LABEL + "`:`").append(pTree.getDigitalObjectId().getStringRepresentation()).append("`:`").append(viewArgument).append("`) RETURN ID(n)");
        return b.toString();
    }

    private static String buildCreateNodeQuery(IDataOrganizationNode pNode, DigitalObjectId oid, String viewName, Map<String, Object> params) {
        StringBuilder b = new StringBuilder();
        String type = (pNode instanceof ICollectionNode) ? COLLECTION_TYPE : (pNode instanceof IFileNode) ? FILE_TYPE : UNKNOWN_TYPE;

        b.append("CREATE (n:`").append(oid.getStringRepresentation()).append("`:`").append(viewName).append("`:`").append(type).append("` {");
        b.append("name : {1}");
        b.append(", type : {2}");
        params.put("1", pNode.getName());
        params.put("2", type);

        int varIterator = 2;
        if (FILE_TYPE.equals(type)) {
            varIterator = 3;
            b.append(", lfn : {3}");
            params.put("3", ((IFileNode) pNode).getLogicalFileName().asString());
        }
        Set<? extends IAttribute> attribs = pNode.getAttributes();
        if (attribs != null) {
            //add attributes
            Iterator<? extends IAttribute> iter = attribs.iterator();
            while (iter.hasNext()) {
                IAttribute attribute = iter.next();
                b.append(", ").append(attribute.getKey()).append(" : {");
                varIterator++;
                params.put(Integer.toString(varIterator), attribute.getValue());
                b.append(varIterator).append("}");
                varIterator++;
            }
        }

        b.append("})  RETURN ID(n)");
        return b.toString();
    }

///////////////
// Alternative persisting...too slow, investigate later
///////////////
//    private static void persistNodeAlt(IDataOrganizationNode pNode, DigitalObjectId oid, String viewName, StringBuilder builder) {
//        Map<String, Object> params = new HashMap<>();
//
//        builder.append(buildCreateNodeQueryAlt(pNode, oid, viewName, params)).append(" ");
//
//        //build sub structure if node is a collection node
//        if (pNode instanceof ICollectionNode) {
//            for (IDataOrganizationNode child : ((ICollectionNode) pNode).getChildren()) {
//                persistNodeAlt(child, oid, viewName, builder);
//            }
//        }
//    }
//    private static String buildCreateNodeQueryAlt(IDataOrganizationNode pNode, DigitalObjectId oid, String view, Map<String, Object> params) {
//        if (pNode.getName() == null) {
//            return "";
//        }
//        StringBuilder b = new StringBuilder();
//        String type = (pNode instanceof ICollectionNode) ? COLLECTION_TYPE : (pNode instanceof IFileNode) ? FILE_TYPE : UNKNOWN_TYPE;
//
//        b.append("CREATE (:").append(oid).append(":").append(view).append(":").append(type).append(" {");
//        b.append("name : '").append(pNode.getName().replaceAll("\'", "\\\\'")).append("'");
//
//        if (FILE_TYPE.equals(type)) {
//            b.append(", lfn : '").append(((IFileNode) pNode).getLogicalFileName().asString().replaceAll("\'", "\\\\'")).append("'");
//        }
//
//        b.append(", id : '").append(pNode.getTransientNodeId().getInTreeId()).append("'");
//
//        Set<? extends IAttribute> attribs = pNode.getAttributes();
//        if (attribs != null) {
//            //add attributes
//            Iterator<? extends IAttribute> iter = attribs.iterator();
//            while (iter.hasNext()) {
//                IAttribute attribute = iter.next();
//                b.append(", ").append(attribute.getKey()).append(" : '");
//                b.append(attribute.getValue()).append("'");
//            }
//        }
//
//        b.append("})");
//        return b.toString();
//    }
//
//    private static String buildCreateRelationQueryAlt(long parentId, long childId) {
//        StringBuilder b = new StringBuilder();
//        b.append("MATCH (a {id:'").append(parentId).append("'}),(b {id:'").append(childId).append("'}) ").append(" CREATE (a)-[p:IS_PARENT]->(b)");
//        return b.toString();
//    }
    private static String buildCreateRelationQuery(long parentId, long childId) {
        StringBuilder b = new StringBuilder();
        b.append("MATCH (a),(b) WHERE ID(a) = ").append(parentId).append("  AND ID(b) = ").append(childId).append(" CREATE (a)-[p:IS_PARENT]->(b)");
        return b.toString();
    }

    /**
     * FileTreeLoading
     */
    public static IFileTree loadFileTree(DigitalObjectId pId, JdbcCypherExecutor exec) {
        return loadFileTree(pId, Constants.DEFAULT_VIEW, exec);
    }

    public static IFileTree loadFileTree(DigitalObjectId pId, String pView, JdbcCypherExecutor exec) {
        Iterator<Map<String, Object>> result = exec.query("MATCH (n: `" + pId.getStringRepresentation() + "`: `" + pView + "`) RETURN ID(n)", null);

        if (!result.hasNext()) {
            return null;
        }
        Integer rootId = (Integer) result.next().get("ID(n)");
        FileTreeImpl tree = new FileTreeImpl();
        tree.setNodeId(Long.valueOf(rootId));
        tree.setDigitalObjectId(pId);
        tree.setViewName(pView);
        Iterator<Map<String, Object>> children = exec.query("MATCH (n:`" + pId.getStringRepresentation() + "`:`" + pView + "`)-[r:IS_PARENT*1..]->(t) WITH startNode(r[length(r)-1]) AS start, endNode(r[length(r)-1]) AS end RETURN ID(start), start, ID(end),end", null);

        List<Map<String, Object>> nodeStack = new ArrayList<>();
        while (children.hasNext()) {
            nodeStack.add(nodeStack.size(), children.next());
        }
        buildTree(tree, nodeStack);
        return tree;
    }

    public static IFileTree loadSubTree(NodeId pNodeId, int maxDepth, JdbcCypherExecutor exec) throws InvalidNodeIdException {
        Iterator<Map<String, Object>> result = exec.query("MATCH (n) WHERE ID(n)=" + pNodeId.getInTreeId() + " RETURN ID(n), n", null);

        if (!result.hasNext()) {
            throw new InvalidNodeIdException("No node with id '" + pNodeId + "' found.");
        }

        LinkedHashMap resultMap = (LinkedHashMap) result.next();
        Integer rootId = (Integer) resultMap.get("ID(n)");
        Map<String, Object> node = (Map<String, Object>) resultMap.get("n");
        FileTreeImpl tree = new FileTreeImpl();
        tree.setNodeId(Long.valueOf(rootId));
        tree.setName((String) node.get("name"));
        Iterator<Map<String, Object>> children = exec.query("MATCH (n)-[r:IS_PARENT*1.." + maxDepth + "]->(t) WHERE ID(n)=" + pNodeId.getInTreeId() + " WITH startNode(r[length(r)-1]) AS start, endNode(r[length(r)-1]) AS end RETURN ID(start), start, ID(end),end", null);

        List<Map<String, Object>> nodeStack = new ArrayList<>();
        while (children.hasNext()) {
            nodeStack.add(nodeStack.size(), children.next());
        }
        buildTree(tree, nodeStack);
        return tree;
    }

    private static void buildTree(IFileTree tree, List<Map<String, Object>> relationStack) {
        while (!relationStack.isEmpty()) {
            Map<String, Object> relation = relationStack.remove(0);
            Integer endId = (Integer) relation.get("ID(end)");
            LinkedHashMap endProperties = (LinkedHashMap) relation.get("end");
            buildNode(endId, endProperties, tree.getRootNode(), relationStack);
        }
    }

    public static void buildNode(Integer nodeId, Map pProperties, ICollectionNode pParent, List<Map<String, Object>> relationStack) {
        Set propertyKeys;
        if (null != (String) pProperties.get("type")) {
            switch ((String) pProperties.get("type")) {
                case FILE_TYPE:
                    final FileNodeImpl fileNode = new FileNodeImpl(null);
                    fileNode.setNodeId(Long.valueOf(nodeId));

                    propertyKeys = pProperties.keySet();
                    for (Object o : propertyKeys) {
                        String key = (String) o;
                        if (null != key) {
                            switch (key) {
                                case "name":
                                    fileNode.setName((String) pProperties.get(key));
                                    break;
                                case "lfn":
                                    fileNode.setLogicalFileName(new LFNImpl((String) pProperties.get(key)));
                                    break;
                                default:
                                    fileNode.addAttribute(new AttributeImpl(key, (String) pProperties.get(key)));
                                    break;
                            }
                        }
                    }

                    pParent.addChild(fileNode);
                    break;
                case COLLECTION_TYPE:
                    final CollectionNodeImpl collectionNode = new CollectionNodeImpl();
                    collectionNode.setNodeId(Long.valueOf(nodeId));
                    propertyKeys = pProperties.keySet();
                    for (Object o : propertyKeys) {
                        String key = (String) o;
                        if (null != key) {
                            if ("name".equals(key)) {
                                collectionNode.setName((String) key);
                            } else {
                                collectionNode.addAttribute(new AttributeImpl(key, (String) pProperties.get(key)));
                            }
                        }
                    }
                    while (!relationStack.isEmpty()) {
                        Map<String, Object> nextRelation = relationStack.get(0);
                        Integer startId = (Integer) nextRelation.get("ID(start)");
                        if (!Objects.equals(startId, nodeId)) {
                            break;
                        }
                        nextRelation = relationStack.remove(0);
                        Integer endId = (Integer) nextRelation.get("ID(end)");
                        LinkedHashMap endProperties = (LinkedHashMap) nextRelation.get("end");
                        buildNode(endId, endProperties, collectionNode, relationStack);
                    }
                    pParent.addChild(collectionNode);
                    break;

                default:
                    //unknown node type
                    break;
            }
        }
    }

    public static void loadNode(Map pNode, ICollectionNode pParent, JdbcCypherExecutor exec, int maxDepth, int currentDepth) {
        Integer nodeId = (Integer) pNode.get("id");
        Map<String, Object> nodePoperties;
        if (pNode.get("data") == null) {
            LinkedHashMap map = (LinkedHashMap) exec.query("MATCH (p) WHERE ID(p)=" + nodeId + " RETURN { id : id(c),  data: c} as node", null);
            nodePoperties = (Map<String, Object>) map.get("data");
        } else {
            nodePoperties = (Map<String, Object>) pNode.get("data");
        }
        Set propertyKeys;
        if (null != (String) nodePoperties.get("type")) {
            switch ((String) nodePoperties.get("type")) {
                case FILE_TYPE:
                    final FileNodeImpl fileNode = new FileNodeImpl(null);
                    fileNode.setNodeId(Long.valueOf(nodeId));

                    propertyKeys = nodePoperties.keySet();
                    for (Object o : propertyKeys) {
                        String key = (String) o;
                        if (null != key) {
                            switch (key) {
                                case "name":
                                    fileNode.setName((String) nodePoperties.get(key));
                                    break;
                                case "lfn":
                                    fileNode.setLogicalFileName(new LFNImpl((String) nodePoperties.get(key)));
                                    break;
                                default:
                                    fileNode.addAttribute(new AttributeImpl(key, (String) nodePoperties.get(key)));
                                    break;
                            }
                        }
                    }

                    pParent.addChild(fileNode);
                    break;
                case COLLECTION_TYPE:
                    final CollectionNodeImpl collectionNode = new CollectionNodeImpl();
                    collectionNode.setNodeId(Long.valueOf(nodeId));
                    propertyKeys = nodePoperties.keySet();
                    for (Object o : propertyKeys) {
                        String key = (String) o;
                        if (null != key) {
                            if ("name".equals(key)) {
                                collectionNode.setName((String) key);
                            } else {
                                collectionNode.addAttribute(new AttributeImpl(key, (String) nodePoperties.get(key)));
                            }
                        }
                    }
                    Iterator<Map<String, Object>> children = exec.query("MATCH (p)-[r:IS_PARENT]->(c) WHERE ID(p)=" + nodeId + " RETURN { id : id(c),  data: c} as node", null);
                    while (children.hasNext()) {
                        // Integer nextId = (Integer) children.next().get("ID(c)");
                        LinkedHashMap node = (LinkedHashMap) children.next().get("node");
                        if (currentDepth < maxDepth) {
                            loadNode(node, collectionNode, exec, maxDepth, currentDepth++);
                        }
                    }
                    pParent.addChild(collectionNode);
                    break;
                default:
                    //unknown node type
                    break;
            }
        }
        //}
    }

    public static void loadNode(Integer pNodeId, ICollectionNode pParent, JdbcCypherExecutor exec) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", pNodeId);
        loadNode(map, pParent, exec, Integer.MAX_VALUE, 0);
    }

    public static void updateNode(NodeId pNodeId, final IDataOrganizationNode pNewData, JdbcCypherExecutor exec) throws InvalidNodeIdException {

        Iterator<Map<String, Object>> result = (Iterator<Map<String, Object>>) exec.query("MATCH (s:`" + pNodeId.getDigitalObjectId() + "`:`" + pNodeId.getViewName() + "`) WHERE ID(s)=" + pNodeId.getInTreeId() + " RETURN s", null);
        if (!result.hasNext()) {
            throw new InvalidNodeIdException("No node with id '" + pNodeId + "' found.");
        }
        Map<String, Object> pProperties = (Map<String, Object>) result.next().get("s");

        if (null != (String) pProperties.get("type")) {
            switch ((String) pProperties.get("type")) {
                case FILE_TYPE:
                    if (pNewData instanceof IFileNode) {
                        //same type...
                    } else {
                        throw new IllegalArgumentException("Type conflict. The node identified by id " + pNodeId + " is a file node but pNewData does not implement IFileNode.");
                    }
                    break;
                case COLLECTION_TYPE:
                    if (pNewData instanceof ICollectionNode) {
                        //same type...
                    } else {
                        throw new IllegalArgumentException("Type conflict. The node identified by id " + pNodeId + " is a collection node but pNewData does not implement ICollectionNode.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Type conflict. The node identified by id " + pNodeId + " has the unknown type " + pProperties.get("type"));
            }
        }

        final StringBuilder b = new StringBuilder();
        b.append("SET ");

        //restructure new attributes for better handling
        Set<? extends IAttribute> attributes = pNewData.getAttributes();
        final Map<String, String> attributeMap = new HashMap<>();
        for (IAttribute attribute : attributes) {
            attributeMap.put(attribute.getKey(), attribute.getValue());
        }
        //check existing properties for changes/removals
        Set propertyKeys = pProperties.keySet();
        for (Object o : propertyKeys) {
            String key = (String) o;
            String value = (String) pProperties.get(key);
            if (null != key) {

                switch (key) {
                    case "name":
                        String oldName = value;
                        if (!Objects.equals(pNewData.getName(), oldName)) {
                            if (b.length() > 4) {
                                b.append(", ");
                            }
                            b.append("s.name='").append(pNewData.getName()).append("' ");
                        }
                        break;
                    case "lfn":
                        String oldLfn = value;
                        if (pNewData instanceof IFileNode) {
                            String newLfn = ((IFileNode) pNewData).getLogicalFileName().asString();
                            if (!Objects.equals(newLfn, oldLfn)) {
                                if (b.length() > 4) {
                                    b.append(", ");
                                }
                                b.append("s.lfn='").append(newLfn).append("' ");
                            }
                        }
                        break;
                    default:
                        if (attributeMap.containsKey(key)) {
                            if (!Objects.equals(attributeMap.get(key), value)) {
                                if (b.length() > 4) {
                                    b.append(", ");
                                }
                                b.append("s.").append(key).append("='").append(value).append("' ");
                                //prop has been handled so remove it.
                                attributeMap.remove(key);
                            }
                        } else {
                            if (b.length() > 4) {
                                b.append(", ");
                            }
                            b.append("s.").append(key).append("=NULL ");
                            //prop has been handled so remove it.
                            attributeMap.remove(key);
                        }
                        break;
                }
            }
        }
        //add unhandled attributes
        propertyKeys = attributeMap.keySet();
        for (Object o : propertyKeys) {
            String key = (String) o;
            String value = (String) pProperties.get(key);
            if (null != key) {
                if (b.length() > 4) {
                    b.append(", ");
                }
                b.append("s.").append(key).append("='").append((String) value).append("' ");
            }
        }

        //all changes collected, perform update if there is any change
        if (b.length() > 4) {
            exec.query("MATCH (s) WHERE ID(s)=" + pNodeId.getInTreeId() + " " + b.toString(), null);
        } else {
            //nothing to do
        }
    }

    public static List<String> getViews(DigitalObjectId doi, JdbcCypherExecutor exec) {
        Iterator<Map<String, Object>> result = exec.query("MATCH (p:`" + ROOT_LABEL + "`:`" + doi.getStringRepresentation() + "`) RETURN labels(p)", null);
        List<String> resultList = (List<String>) result.next().get("labels(p)");

        //remove non-view labels
        resultList.remove(ROOT_LABEL);
        resultList.remove(doi.getStringRepresentation());
        return resultList;
    }

}
