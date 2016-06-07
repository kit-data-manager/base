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

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.InitializationError;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.entity.impl.client.NodeId;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.commons.interfaces.IConfigurableAdapter;
import edu.kit.dama.util.Constants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class DataOrganizerImpl implements DataOrganizer, IConfigurableAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataOrganizerImpl.class);

    private JdbcCypherExecutor exec = null;

    /**
     * Default constructor.
     */
    public DataOrganizerImpl() {
    }

    private void checkExecutor() {
        if (exec == null) {
            throw new InitializationError("This DataOrganizerImpl is not initialized properly. Cypher executor is null.");
        }
    }

    @Override
    public IFileTree loadFileTree(DigitalObjectId digitalObjectId, String viewName) throws EntityNotFoundException {
        checkExecutor();
        return Neo4jHelper.loadFileTree(digitalObjectId, viewName, exec);
    }

    @Override
    public IFileTree loadFileTree(DigitalObjectId digitalObjectId) throws EntityNotFoundException {
        checkExecutor();
        return Neo4jHelper.loadFileTree(digitalObjectId, exec);
    }

    @Override
    public void createFileTree(IFileTree fileTree) throws EntityExistsException {
        checkExecutor();
        Neo4jHelper.persistFileTree(fileTree, exec);
    }

    @Override
    public List<? extends IDataOrganizationNode> getChildren(NodeId nodeId, int firstResult, int maxResult) throws InvalidNodeIdException {
        checkExecutor();
        Iterator<Map<String, Object>> result = exec.query("MATCH s-[r:IS_PARENT]->t WHERE ID(s) = " + nodeId.getInTreeId() + " RETURN ID(t) SKIP " + firstResult + " LIMIT " + maxResult, null);
        CollectionNodeImpl cn = new CollectionNodeImpl();

        while (result.hasNext()) {
            Integer val = (Integer) result.next().get("ID(t)");
            Neo4jHelper.loadNode(val, cn, exec);
        }
        return new ArrayList<>(cn.getChildren());
    }

    @Override
    public Long getChildCount(NodeId nodeId) throws InvalidNodeIdException {
        checkExecutor();
        return ((Integer) exec.query("MATCH s-[r:IS_PARENT]->t WHERE ID(s) = " + nodeId.getInTreeId() + "  RETURN count(t)", null).next().get("count(t)")).longValue();
    }

    @Override
    public IFileTree loadSubTree(NodeId relativeRoot, int relativeDepth) throws InvalidNodeIdException {
        checkExecutor();
        return Neo4jHelper.loadSubTree(relativeRoot, relativeDepth, exec);
    }

    @Override
    public NodeId getRootNodeId(DigitalObjectId digitalObjectId) throws EntityNotFoundException {
        checkExecutor();
        return getRootNodeId(digitalObjectId, Constants.DEFAULT_VIEW);
    }

    @Override
    public NodeId getRootNodeId(DigitalObjectId digitalObjectId, String viewName) throws EntityNotFoundException {
        Iterator<Map<String, Object>> result = (Iterator<Map<String, Object>>) exec.query("MATCH (s:`Root`:`" + digitalObjectId.getStringRepresentation() + "`:`" + viewName + "`) RETURN ID(s)", null);

        if (!result.hasNext()) {
            //not found
            throw new EntityNotFoundException("No node labeled as 'Root' was found for digital object '" + digitalObjectId + "' and view '" + viewName + "'.");
        }
        Integer id = (Integer) result.next().get("ID(s)");
        return new NodeId(digitalObjectId, id, 1);
    }

    @Override
    public void updateNodeData(NodeId nodeId, IDataOrganizationNode newData) throws InvalidNodeIdException {
        checkExecutor();
        Neo4jHelper.updateNode(nodeId, newData, exec);
    }

    @Override
    public IDataOrganizationNode loadNode(NodeId nodeId) throws InvalidNodeIdException {
        checkExecutor();
        CollectionNodeImpl dummyParent = new CollectionNodeImpl();
        Neo4jHelper.loadNode((int) nodeId.getInTreeId(), dummyParent, exec);
        if (dummyParent.getChildren().isEmpty()) {
            //should not happen as this is handled before...but in case...
            throw new InvalidNodeIdException("No node with id '" + nodeId + "' found.");
        }

        return dummyParent.getChildren().get(0);
    }

    @Override
    public List<String> getViews(DigitalObjectId doid) {
        checkExecutor();
        return Neo4jHelper.getViews(doid, exec);
    }

    public final boolean configure(String pNeo4jUrl, String pUsername, String pPassword) {
        if (pNeo4jUrl == null || pUsername == null || pPassword == null) {
            throw new IllegalArgumentException("None of the arguments 'pNeo4jUrl', pUsername' and 'pPassword' must be null.");
        }
        boolean result = false;
        LOGGER.debug("Initializing Neo4J JDBC adapter for URL '{}', user '{}' and password '{}'", pNeo4jUrl, pUsername, pPassword);
        try {
            exec = new JdbcCypherExecutor(pNeo4jUrl, pUsername, pPassword);
        } catch (Throwable t) {
            LOGGER.error("Failed to connect to " + pNeo4jUrl + " using username '" + pUsername + "' and password '" + pPassword + "'", t);
            return result;
        }
        LOGGER.debug("Trying to get all nodes labeled as 'Root'");
        Iterator<Map<String, Object>> results = exec.query("MATCH(n:`Root`) RETURN n", null);
        if (results != null) {
            //success
            LOGGER.debug("Test query succeeded. Neo4j DataOrganizer successfully configured.");
            result = true;
        } else {
            LOGGER.warn("Test query returned a null result, which should not happen. Failed to configure Neo4j DataOrganizer.");
        }

        return result;
    }

    @Override
    public final boolean configure(Configuration pConfig) throws ConfigurationException {
        String neo4jUrl = pConfig.getString("neo4jUrl"); //http://localhost:7474
        String neo4jUser = pConfig.getString("neo4jUser");//neo4j
        String neo4jPassword = pConfig.getString("neo4jPassword");//neo4j

        return configure(neo4jUrl, neo4jUser, neo4jPassword);

    }

}
