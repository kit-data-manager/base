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
package edu.kit.dama.staging.util;

import com.thoughtworks.xstream.XStream;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.lsdf.adalapi.AbstractFile;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.mdm.dataorganization.entity.core.IAttribute;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.jpa.persistence.PersistenceFacade;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.MetaDataManagement;
import edu.kit.dama.mdm.dataorganization.impl.staging.AttributeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.CollectionNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.DataOrganizationNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileNodeImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.FileTreeImpl;
import edu.kit.dama.staging.entities.StagingFile;
import edu.kit.dama.mdm.dataorganization.impl.staging.LFNImpl;
import edu.kit.dama.mdm.dataorganization.impl.staging.ISelectable;
import edu.kit.dama.util.Constants;
import edu.kit.tools.url.URLCreator;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides some utilities for a better data organization service
 * handling and debugging, e.g. writing a file tree to a PrintStream.
 *
 * @author jejkal
 */
public class DataOrganizationUtils {

    /**
     * The logger instance
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataOrganizationUtils.class);
    public final static String DIRECTORY_KEY = "directory";
    public final static String LAST_MODIFIED_KEY = "lastModified";
    public final static String CHILDREN_KEY = "children";
    public final static String SIZE_KEY = "size";
    public final static String EXISTS = "exists";
    public final static String TRANSFERRED = "transferred";

    /**
     * A IDataOrganizationNode comparater based on the node name which can be
     * used for file listings. The ordering prefers directories and sorts files
     * alphabetically and case sensitive.
     */
    public static final class NAME_COMPARATOR implements Comparator<IDataOrganizationNode> {

        @Override
        public int compare(IDataOrganizationNode o1,
                IDataOrganizationNode o2) {
            if ((o1 instanceof ICollectionNode && o2 instanceof ICollectionNode) || (o1 instanceof IFileNode && o2 instanceof IFileNode)) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            } else if (o1 instanceof ICollectionNode && o2 instanceof IFileNode) {
                return -1;
            }
            return 1;
        }
    }

    /**
     * Hidden constructor.
     */
    DataOrganizationUtils() {
    }

    /**
     * Create a FileTree object for the provided abstract file. The URLs to each
     * node of the tree will be relative to pFile. The root node of the FileTree
     * will be linked to the provided digital object ID.
     *
     * @param pDigitalObjectId The digital object ID the tree is associated
     * with.
     * @param pFile The abstract file which will be reflected by the tree.
     * @param pIncludeAttributes If TRUE obtainable file attributes (e.g.
     * filesize, directory/file flag, lastModified) are included into the tree.
     *
     * @return A FileTree which contains call files from pLocalPath in relation
     * to pBaseURL.
     *
     * @throws IOException If pFile cannot be accessed (AdalapiException) or
     * pFile has an invalid URL (MalformedURLException).
     */
    public static IFileTree createTreeFromFile(String pDigitalObjectId, AbstractFile pFile, boolean pIncludeAttributes) throws IOException {
        return createTreeFromFile(pDigitalObjectId, pFile, pFile.getUrl(), pIncludeAttributes);
    }

    /**
     * Create a FileTree object for the provided abstract file. The URLs to each
     * node of the tree will be build by using the node's relative path and the
     * provided base URL. The root node of the FileTree will be linked to the
     * provided digital object ID.
     *
     * @param pDigitalObjectId The digital object ID the tree is associated
     * with.
     * @param pFile The abstract file which will be reflected by the tree
     * @param pBaseUrl The base URL which will be the root of the tree.
     * @param pIncludeAttributes If TRUE obtainable file attributes (e.g.
     * filesize, directory/file flag, lastModified) are included into the tree.
     *
     * @return A FileTree which contains call files from pLocalPath in relation
     * to pBaseURL.
     *
     * @throws AdalapiException If pFile cannot be accessed.
     * @throws MalformedURLException If pFile or pBaseUrl are invalid URLs.
     */
    public static IFileTree createTreeFromFile(String pDigitalObjectId, AbstractFile pFile, URL pBaseUrl, boolean pIncludeAttributes) throws AdalapiException, MalformedURLException {
        LOGGER.debug("Creating file tree for object {} from local path {} with base URL {}", new Object[]{pDigitalObjectId, pFile.getPath(), pBaseUrl.toString()});
        IDataOrganizationNode node = createNodeFromFile(pDigitalObjectId, pFile, pFile.getUrl().toString(), pBaseUrl, pIncludeAttributes);
        IFileTree fileTree = new FileTreeImpl();
        fileTree.setDigitalObjectId(new DigitalObjectId(pDigitalObjectId));
        if (node instanceof ICollectionNode) {
            //should always be the case as pLocalPath is the root path/root node
            for (IDataOrganizationNode child : ((ICollectionNode) node).getChildren()) {
                fileTree.getRootNode().addChild(child);
            }
            ((IDataOrganizationNode) fileTree).setAttributes(node.getAttributes());
        } else {
            fileTree.getRootNode().addChild(node);
            ((IDataOrganizationNode) fileTree).setAttributes(node.getAttributes());
        }

        return fileTree;
    }

    /**
     * Creates a DataOrganizationNode from a local path. This method will take
     * pLocalFile, removed pLocalBasePath from the file's path and creates a new
     * DataOrganizationNode consisting of pNewBaseURL and the relative path of
     * pLocalFile. Directories are included by iterative calls until the end of
     * the directory structure was reached.
     *
     * @param pDigitalObjectId The digital object ID used for the root node
     * @param pAbstractFile The file/directory forming the DataOrganizationNode
     * @param pBaseUrl The base URL which will be removed from all LFNs in order
     * to get its relative path.
     * @param pNewBaseUrl The base URL to which the relative path of pFile is
     * appended to form a DataOrganizationNode. It is expected that this URL is
     * a directory.
     * @param pIncludeAttributes If TRUE obtainable file attributes (e.g.
     * filesize, directory/file flag, lastModified) are included into the tree.
     *
     * @return An IDataOrganizationNode object representing the entire file
     * structure below pLocalFile.
     *
     * @throws AdalapiException If the access to pFile fails.
     * @throws MalformedURLException If pBaseUrl is invalid.
     */
    private static IDataOrganizationNode createNodeFromFile(String pDigitalObjectId, AbstractFile pAbstractFile, String pBaseUrl, URL pNewBaseUrl, boolean pIncludeAttributes) throws AdalapiException, MalformedURLException {
        String sBaseUrl = pNewBaseUrl.toString();
        if (!sBaseUrl.endsWith("/")) {//add slash to base URL as this should be a directory
            sBaseUrl += "/";
        }
        LOGGER.debug("Creating node from file {} with baseUrl {} for new baseUrl {}", pAbstractFile, pBaseUrl, pNewBaseUrl);
        DataOrganizationNodeImpl ret;
        String fromPath = FilenameUtils.normalize(pAbstractFile.getUrl().getPath(), true);
        if (pAbstractFile.isDirectory()) {//local file is a directory, proceed accordingly in a recursive way
            CollectionNodeImpl cn = new CollectionNodeImpl(new LFNImpl(sBaseUrl));
            if (fromPath.equals(pBaseUrl)) {//we have the root path, so use the digital object ID as node identifier
                cn.setName(pDigitalObjectId);
            } else {//we have another path, so use the directory name as node identifier
                cn.setName(pAbstractFile.getName());
            }

            //list all files and add them to the current node
            Collection<AbstractFile> ls = pAbstractFile.list();
            long dirSize = 0;
            if (ls != null) {
                for (AbstractFile child : ls) {//add all children recursively
                    if (child.isLocal()) {//perform symlink check
                        try {
                            if (Files.isSymbolicLink(Paths.get(pAbstractFile.getUrl().toURI()))) {
                                LOGGER.warn("Symbolic link detected in child in local file at {}. Ignoring it.", child);
                                //do nothing with this child
                            } else {
                                //no symlink, add child
                                IDataOrganizationNode childNode = createNodeFromFile(pDigitalObjectId, child, pBaseUrl, pNewBaseUrl, pIncludeAttributes);
                                cn.addChild(childNode);
                                for (IAttribute attribute : childNode.getAttributes()) {
                                    if (attribute.getKey().equals(SIZE_KEY)) {
                                        dirSize += Long.parseLong(attribute.getValue());
                                    }
                                }
                            }
                        } catch (URISyntaxException ex) {
                            //ignored
                        }
                    } else {//for remote files we cannot perform symlink checks
                        IDataOrganizationNode childNode = createNodeFromFile(pDigitalObjectId, child, pBaseUrl, pNewBaseUrl, pIncludeAttributes);
                        cn.addChild(childNode);
                        for (IAttribute attribute : childNode.getAttributes()) {
                            if (attribute.getKey().equals(SIZE_KEY)) {
                                dirSize += Long.parseLong(attribute.getValue());
                            }
                        }
                    }
                }
            }

            if (pIncludeAttributes) {
                cn.addAttribute(new AttributeImpl(DIRECTORY_KEY, Boolean.TRUE.toString()));
                cn.addAttribute(new AttributeImpl(LAST_MODIFIED_KEY, Long.toString(pAbstractFile.lastModified())));
                cn.addAttribute(new AttributeImpl(SIZE_KEY, Long.toString(dirSize)));
                cn.addAttribute(new AttributeImpl(CHILDREN_KEY, Integer.toString(pAbstractFile.list().size())));
            }

            ret = cn;
        } else {//current file is no directory, just append it as filenode
            ret = new FileNodeImpl(new LFNImpl(pAbstractFile.getUrl()));
            ret.setName(pAbstractFile.getName());
            if (pIncludeAttributes) {
                ret.addAttribute(new AttributeImpl(DIRECTORY_KEY, Boolean.FALSE.toString()));
                ret.addAttribute(new AttributeImpl(LAST_MODIFIED_KEY, Long.toString(pAbstractFile.lastModified())));
                ret.addAttribute(new AttributeImpl(SIZE_KEY, Long.toString(pAbstractFile.getSize())));
            }
        }
        return ret;
    }

    /**
     * Print a provided file tree into a string builder.
     *
     * @param pTree The tree to print.
     * @param pPadding The passing string.
     * @param pOutputHolder The destination string builder.
     * @param pDoubleLine TRUE = each node fills two lines.
     * @param pIncludeURL TRUE = Include the node URLs into the tree.
     */
    private static void printTreeInternal(ICollectionNode pTree, String pPadding, StringBuilder pOutputHolder, boolean pDoubleLine, boolean pIncludeURL) {
        String name = pTree.getName();
        String sPadding = pPadding;
        if (name == null) {
            name = "<null>";
        }
        pOutputHolder.append(sPadding).append("+-").append(name).append("/\n");
        sPadding = sPadding + ' ';

        IDataOrganizationNode[] files;
        files = pTree.getChildren().toArray(new IDataOrganizationNode[pTree.getChildren().size()]);
        Arrays.sort(files, new Comparator<IDataOrganizationNode>() {
            @Override
            public int compare(IDataOrganizationNode o1, IDataOrganizationNode o2) {
                if (o1 instanceof ICollectionNode && !(o2 instanceof ICollectionNode)) {
                    return -1;
                } else if (!(o1 instanceof ICollectionNode) && o2 instanceof ICollectionNode) {
                    return 1;
                }

                //set name != null to allow comparison
                if (o1.getName() == null) {
                    o1.setName("");
                }

                if (o2.getName() == null) {
                    o2.setName("");
                }

                return o1.getName().compareTo(o2.getName());
            }
        });
        int count = 0;
        for (IDataOrganizationNode file : files) {
            count += 1;
            if (pDoubleLine) {
                pOutputHolder.append(sPadding).append("|\n");
            }
            if (file instanceof ICollectionNode) {
                if (count == files.length) {
                    printTreeInternal((ICollectionNode) file, sPadding + ' ', pOutputHolder, pDoubleLine, pIncludeURL);
                } else {
                    printTreeInternal((ICollectionNode) file, sPadding + "|", pOutputHolder, pDoubleLine, pIncludeURL);
                }
            } else {
                pOutputHolder.append(sPadding).append("+-").append(file.getName());
                if (pIncludeURL) {
                    pOutputHolder.append(" (").append(((IFileNode) file).getLogicalFileName().asString()).append(")");
                }
                pOutputHolder.append("\n");
            }
        }
    }

    /**
     * Print the provided FileTree to Std.out. This method is intended to be
     * used for debugging.
     *
     * @param pTree The FileTree to print.
     * @param pIncludeURL Include URL after filenames.
     */
    public static void printTree(ICollectionNode pTree, boolean pIncludeURL) {
        printTree(pTree, pIncludeURL, System.out);
    }

    /**
     * Print the provided FileTree into the provided PrintStream. This method is
     * intended to be used for debugging.
     *
     * @param pTree The FileTree to print.
     * @param pIncludeURL Include URL after filenames.
     * @param pOut The PrintStream to which the output is written, e.g.
     * System.out.
     */
    public static void printTree(ICollectionNode pTree, boolean pIncludeURL, PrintStream pOut) {
        if (pOut == null) {
            throw new IllegalArgumentException("Argument pOut must not be null");
        }
        StringBuilder b = new StringBuilder();
        printTreeInternal(pTree, "", b, false, pIncludeURL);
        pOut.println(b.toString());
    }

    /**
     * Serialize the provided tree as XML into the provided file.
     *
     * @param pTree The tree to write.
     * @param pDestination The file to write into.
     */
    public static void writeTreeToFile(IFileTree pTree, File pDestination) {
        if (pTree == null) {
            throw new IllegalArgumentException("Argument 'pTree' must not be 'null'");
        }

        if (pDestination == null) {
            throw new IllegalArgumentException("Argument 'pDestination' must not be 'null'");
        }

        XStream x = new XStream();
        x.alias("fileTree", IFileTree.class);
        LOGGER.debug("Writing data organization tree to file {}", pDestination.getPath());
        FileWriter w = null;
        try {
            w = new FileWriter(pDestination);
            x.toXML(pTree, w);
            w.flush();
        } catch (IOException ioe) {
            LOGGER.error("Failed to write data organization tree", ioe);
        } finally {
            try {
                if (w != null) {
                    w.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Deserialize the provided from the provided file in which the tree was
     * written before using writeTreeToFile().
     *
     * @param pSource The file to read from.
     *
     * @return The tree or null if deserialization fails.
     */
    public static IFileTree readTreeFromFile(File pSource) {
        if (pSource == null) {
            throw new IllegalArgumentException("Argument 'pSource' must not be 'null'");
        }

        XStream x = new XStream();
        x.alias("fileTree", IFileTree.class);
        LOGGER.debug("Loading data organization tree to file {}", pSource.getPath());
        FileReader r = null;
        IFileTree result = null;
        try {
            r = new FileReader(pSource);
            result = (IFileTree) x.fromXML(r);
        } catch (FileNotFoundException fnfe) {
            LOGGER.error("Failed to read data organization tree.", fnfe);
        } finally {
            try {
                if (r != null) {
                    r.close();
                }
            } catch (IOException ignored) {
            }
        }
        return result;
    }

    /**
     * Restore the tree structure indicated by pTree at pDestination. All single
     * files which are inside pTree are added to pContainedFileMappings as key.
     * Created destination folders places under pDestination are put into
     * pContainedFileMappings as value. Afterwards, all key entries in
     * pContainedFileMappings can be transferred to the value entries.
     *
     * @param pTree The tree strucuture to restore.
     * @param pDestination The destination where to restore pTree.
     * @param pOpenTransfers A map of files which have to be transferred.
     *
     * @throws IOException If pTree contains files with an unknown URL or if any
     * subfolder cannot be created.
     */
    public static void restoreTreeStructure(IFileTree pTree, AbstractFile pDestination, Map<StagingFile, StagingFile> pOpenTransfers) throws IOException {
        if (pTree == null) {
            throw new IllegalArgumentException("Argument pTree must not be 'null'");
        }

        if (pDestination == null) {
            throw new IllegalArgumentException("Argument pDestination must not be 'null'");
        }

        LOGGER.debug("Restoring file tree for object {} at {}", new Object[]{pTree.getDigitalObjectId(), pDestination.getPath()});
        if (!pDestination.exists()) {
            throw new IOException("Destination folder at " + pDestination.getUrl() + " does not exist.");
        }
        ICollectionNode root = pTree.getRootNode();
        restoreFolder(root, pDestination.getUrl(), pOpenTransfers);
        LOGGER.debug("File tree restored, {} single file mappings found", pOpenTransfers.size());
    }

    /**
     * Restore a folder indicated by pNode at pBaseUrl recursively. This method
     * is internally used by restoreTreeStructure to process a subfolder.
     * Directories at the destination location are created directly. Necessary
     * transfers are only stored in the provided map and have to be performated
     * later.
     *
     * @param pNode The collection node to restore.
     * @param pBaseUrl The base URL where the folder should be restored. This
     * URL is used to build the destination paths.
     * @param pOpenTransfers A map of transfers which are finally necessary to
     * restore the folder structure. The transfers have to be carried out in a
     * separate step.
     *
     * @throws IOException If directory check/creation fails.
     */
    private static void restoreFolder(ICollectionNode pNode, URL pBaseUrl, Map<StagingFile, StagingFile> pOpenTransfers) throws IOException {
        String nodeName = pNode.getName();
        if (nodeName == null) {
            //obviously an entire tree should be restored...as it has no name, take empty string
            nodeName = "";
        }
        LOGGER.debug("Building restore path from base URL {} and node name {}", new Object[]{pBaseUrl, nodeName});
        URL newUrl = URLCreator.appendToURL(pBaseUrl, nodeName);

        LOGGER.debug("Restoring folder to {}", newUrl);
        AbstractFile baseFile = new AbstractFile(pBaseUrl);
        AbstractFile newFile = new AbstractFile(newUrl);

        //check for EXISTS attribute
        if (!directoryExists(pNode)) {
            //attribute not found, check using protocol access
            if (!newFile.exists()) {
                //file does not exist...create it.
                newFile = baseFile.createDirectory(nodeName);
                //set exists attribute after successful creation
                pNode.addAttribute(new AttributeImpl(EXISTS, Boolean.TRUE.toString()));
            } else {
                //set exists attribute as directory already exists
                pNode.addAttribute(new AttributeImpl(EXISTS, Boolean.TRUE.toString()));
            }
        }
        //folder exists...check subfolder
        for (IDataOrganizationNode child : pNode.getChildren()) {
            if (child instanceof ICollectionNode) {
                restoreFolder((ICollectionNode) child, newFile.getUrl(), pOpenTransfers);
            } else if (child instanceof IFileNode && !isFileTransferred((IFileNode) child)) {
                //file still has to be transferred
                //source LFN is obtained from the file node
                StagingFile sourceLfn = new StagingFile(((IFileNode) child).getLogicalFileName());
                //target LFN is a local file
                AbstractFile targetFile = new AbstractFile(URLCreator.appendToURL(newFile.getUrl(), child.getName()));
                StagingFile targetLfn = new StagingFile(targetFile);
                pOpenTransfers.put(sourceLfn, targetLfn);
            }
        }
    }

    /**
     * Check if the EXISTS flag is set as attribute of the provided node.
     *
     * @param pNode The node to check.
     *
     * @return TRUE = EXISTS is set TRUE.
     */
    public static boolean directoryExists(ICollectionNode pNode) {
        IAttribute attribute = (IAttribute) CollectionUtils.find(pNode.getAttributes(), new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return ((IAttribute) o).getKey().equals(EXISTS);
            }
        });

        return (attribute != null && Boolean.parseBoolean(attribute.getValue()));
    }

    /**
     * Check if the TRANSFERRED flag is set as attribute of the provided node.
     *
     * @param pNode The node to check.
     *
     * @return TRUE = TRANSFERRED is set TRUE.
     */
    public static boolean isFileTransferred(IFileNode pNode) {
        IAttribute attribute = (IAttribute) CollectionUtils.find(pNode.getAttributes(), new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return ((IAttribute) o).getKey().equals(TRANSFERRED);
            }
        });

        return (attribute != null && Boolean.parseBoolean(attribute.getValue()));
    }

    /**
     * Mark the provided IFileNode as TRANSFERRED. Basically, an attribute
     * TRANSFERRED with the value 'TRUE' is added. This feature is needed for
     * the KIT Data Manager data transfer.
     *
     * @param pNode The tree node which should be marked as TRANSFERRED.
     */
    public static void markFileTransferred(IFileNode pNode) {
        IAttribute attribute = (IAttribute) CollectionUtils.find(pNode.getAttributes(), new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return ((IAttribute) o).getKey().equals(TRANSFERRED);
            }
        });

        if (attribute == null) {
            pNode.addAttribute(new AttributeImpl(TRANSFERRED, Boolean.TRUE.toString()));
        } else {
            attribute.setValue(Boolean.TRUE.toString());
        }
    }

    /**
     * Copy a single node including all children and attributes. Optionally,
     * only selected nodes can be copied by setting pSelectedOnly 'true'. In
     * this case, each node must implement the interface ISelectable.
     * Otherwhise, an IllegalArgumentException will be thrown.
     *
     * @param <C> An instance of DataOrganizationNodeImpl.
     * @param pNode The node to copy.
     * @param pSelectedOnly Copy only node which are marked as selected via the
     * ISelectable interface.
     *
     * @return An exact copy of (all selected nodes below) pNode.
     */
    public static <C extends DataOrganizationNodeImpl> C copyNode(IDataOrganizationNode pNode, boolean pSelectedOnly) {
        IDataOrganizationNode result = null;
        if (pNode != null) {
            LOGGER.debug("Copying node {}", pNode.getName());
        } else {
            throw new IllegalArgumentException("Argument pNode must not be null.");
        }

        if (pNode instanceof IFileNode) {
            LOGGER.debug("Copying node as FileNode.");
            result = new FileNodeImpl(((IFileNode) pNode).getLogicalFileName());
        } else if (pNode instanceof IFileTree) {
            LOGGER.debug("Copying node as TreeNode.");
            result = new FileTreeImpl();
            ((IFileTree) result).setDigitalObjectId(((IFileTree) pNode).getDigitalObjectId());
            for (IDataOrganizationNode child : ((IFileTree) pNode).getRootNode().getChildren()) {
                if (!pSelectedOnly || ((ISelectable) child).isSelected()) {
                    ((FileTreeImpl) result).getRootNode().addChild(copyNode(child, pSelectedOnly));
                }
            }
        } else if (pNode instanceof ICollectionNode) {
            LOGGER.debug("Copying node as CollectionNode.");
            result = new CollectionNodeImpl();
            ICollectionNode colNode = (ICollectionNode) pNode;
            for (IDataOrganizationNode child : colNode.getChildren()) {
                if (!pSelectedOnly || ((ISelectable) child).isSelected()) {
                    ((CollectionNodeImpl) result).addChild(copyNode(child, pSelectedOnly));
                }
            }
        }

        if (result == null) {
            throw new IllegalArgumentException("Argument 'pNode' must be an instance of IFileTree, ICollectionNode or IFileNode.");
        }
        LOGGER.debug("Getting node id.");
        if (pNode.getTransientNodeId() != null) {
            LOGGER.debug("Setting node id");
            ((DataOrganizationNodeImpl) result).setNodeId(pNode.getTransientNodeId().getInTreeId());
        } else {
            LOGGER.debug("Node id is null. Skip setting it.");
        }
        result.setName(pNode.getName());
        result.setViewName(pNode.getViewName());
        result.setDescription(pNode.getDescription());
        LOGGER.debug("Copying attributes.");
        Set<? extends IAttribute> attributes = pNode.getAttributes();
        Set<AttributeImpl> attributeCopy = new HashSet<>();
        for (IAttribute a : attributes) {
            attributeCopy.add(new AttributeImpl(a.getKey(), a.getValue()));
        }
        result.setAttributes(attributeCopy);
        return (C) result;
    }

    /**
     * Copy the provided tree and return the result. Optionally, only selected
     * tree nodes can be copied by setting pSelectedOnly 'true'. In this case,
     * each tree node must implement the interface ISelectable. Otherwhise, an
     * IllegalArgumentException will be thrown.
     *
     * @param <C> An instance of IFileTree.
     * @param pTree The tree to copy.
     * @param pSelectedOnly Only copy selected nodes. If 'true', all nodes of
     * pTree must implement ISelectable.
     *
     * @return A copy of (all selected nodes of) pTree.
     */
    public static <C extends IFileTree> C copyTree(IFileTree pTree, boolean pSelectedOnly) {
        if (pTree == null) {
            throw new IllegalArgumentException("Argument 'pNode' must not be null");
        }

        if (pSelectedOnly) {
            if (!(pTree instanceof ISelectable)) {
                throw new IllegalArgumentException("'pNode' is no instance of ISelectable");
            }

            if (!((ISelectable) ((IFileTree) pTree).getRootNode()).isSelected()) {
                //nothing is selected
                return null;
            }
        }

        IFileTree result = new FileTreeImpl();
        result.setDigitalObjectId(((IFileTree) pTree).getDigitalObjectId());

        for (IDataOrganizationNode child : ((IFileTree) pTree).getRootNode().getChildren()) {
            if (!pSelectedOnly || ((ISelectable) child).isSelected()) {
                result.getRootNode().addChild(copyNode(child, pSelectedOnly));
            }
        }

        ICollectionNode resultRoot = result.getRootNode();
        ICollectionNode pNodeRoot = pTree.getRootNode();

        if (resultRoot != null && pNodeRoot != null) {
            resultRoot.setName(pNodeRoot.getName());
            resultRoot.setDescription(pNodeRoot.getDescription());

            Set<? extends IAttribute> pNodeAttributes = pNodeRoot.getAttributes();
            if (pNodeAttributes != null) {
                Set<AttributeImpl> attributeCopy = new HashSet<AttributeImpl>();
                for (IAttribute a : pNodeAttributes) {
                    attributeCopy.add(new AttributeImpl(a.getKey(), a.getValue()));
                }
                resultRoot.setAttributes(attributeCopy);
            }
        }
        return (C) result;
    }

    /**
     * Copy an entire tree including all children and attributes
     *
     * @param <C> An instance of FileTreeImpl.
     * @param pNode The tree to copy
     *
     * @return An exact copy of pNode
     */
    public static <C extends FileTreeImpl> C copyTree(IFileTree pNode) {
        return (C) copyTree(pNode, false);
    }

    /**
     * Walk all parents of the provided node and put them into pParents. This
     * method is used to reflect the location of a specific node. After this
     * call, pParents will contain the path to pCurrentNode beginning from the
     * tree root and ending with the parent of pCurrentNode.
     *
     * @param pCurrentNode The node from which we want to obtain all parent
     * nodes.
     *
     * @return The list of parents of pCurrentNode.
     */
    public static List<ICollectionNode> walkParents(IDataOrganizationNode pCurrentNode) {
        return walkParents(pCurrentNode, new ArrayList<ICollectionNode>());
    }

    /**
     * Walk all parents of the provided node and put them into pParents. This
     * method is used to reflect the location of a specific node. After this
     * call, pParents will contain the path to pCurrentNode beginning from the
     * tree root and ending with the parent of pCurrentNode.
     *
     * @param pCurrentNode The node from which we want to obtain all parent
     * nodes.
     * @param pParents The target list where to put the parents of pCurrentNode.
     * If the argument is null, a new list will be created and returned.
     *
     * @return The list of parents of pCurrentNode.
     */
    public static List<ICollectionNode> walkParents(IDataOrganizationNode pCurrentNode, List<ICollectionNode> pParents) {
        List<ICollectionNode> parents = pParents;
        if (parents == null) {
            parents = new ArrayList<>();
        }
        ICollectionNode currentParent = pCurrentNode.getParent();

        if (currentParent != null) {
            pParents.add(0, currentParent);
            return walkParents(currentParent, pParents);
        }
        return parents;
    }

    /**
     * Merge pNodeList into pNode. All nodes in pNodeList are placed under the
     * path provided by pPath.
     *
     * @param pNode The node to which the merge should be applied.
     * @param pPath The path of pTheNode under pNode.
     * @param pTheNodeList The list of nodes which are finally added.
     */
    public static void merge(ICollectionNode pNode, List<ICollectionNode> pPath, final IDataOrganizationNode... pTheNodeList) {
        List<ICollectionNode> newParents = new ArrayList<>();

        ICollectionNode currentNode = pNode;

        for (ICollectionNode node : pPath) {//check all parents and create them if needed
            boolean needCreation = true;
            for (IDataOrganizationNode child : currentNode.getChildren()) {//check all current children for beeing parents
                if (child instanceof ICollectionNode && child.getName().equals(node.getName())) {//parent found, don't create
                    needCreation = false;
                    //set new parent
                    currentNode = (ICollectionNode) child;
                    newParents.add(currentNode);
                    break;
                }
            }

            if (needCreation) {//parent does not exist, create it
                ICollectionNode newNode = new CollectionNodeImpl();
                newNode.setName(node.getName());
                newParents.add(newNode);
                currentNode.addChild(newNode);
                //set new parent
                currentNode = newNode;
            }
        }

        //put new parents into list to expand them later
        pPath.clear();
        Collections.addAll(pPath, newParents.toArray(new ICollectionNode[newParents.size()]));
        //try to add the new node and overwrite an existing node if needed
        for (IDataOrganizationNode node : pTheNodeList) {
            addNode(currentNode, node);
        }
    }

    /**
     * Add pNodeToAdd to pNode. Before the node is added it is checked if there
     * is already a child of pNode with the name of pNodeToAdd. If there is a
     * IFileNode with the same name, pNodeToAdd is not added. If there is a
     * ICollectionNode with the same name, all children of pNodeToAdd are added
     * recursively using this method. The mentioned checks are then performed
     * for each node.
     *
     * @param pNode To node to which a node is added.
     * @param pNodeToAdd The node to add.
     */
    public static void addNode(ICollectionNode pNode, final IDataOrganizationNode pNodeToAdd) {
        IDataOrganizationNode result = (IDataOrganizationNode) CollectionUtils.find(pNode.getChildren(), new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                IDataOrganizationNode node = (IDataOrganizationNode) o;
                if (o instanceof ICollectionNode && pNodeToAdd instanceof IFileNode) {
                    return false;
                } else if (o instanceof IFileNode && pNodeToAdd instanceof ICollectionNode) {
                    return false;
                } else {//node types are equal...check them
                    if (node.getName() != null) {
                        return node.getName().equals(pNodeToAdd.getName());
                    } else if (pNodeToAdd.getName() != null) {
                        return pNodeToAdd.getName().equals(node.getName());
                    }
                    //both are null
                    return true;
                }
            }
        });

        if (result == null) {
            //no child with same name, just add the node
            pNode.addChild(pNodeToAdd);
        } else if (result instanceof ICollectionNode && pNodeToAdd instanceof ICollectionNode) {
            //child with same name found
            for (IDataOrganizationNode child : ((ICollectionNode) pNodeToAdd).getChildren()) {
                addNode((ICollectionNode) result, child);
            }
        }
    }

    /**
     * Merge all children of pSecondTree into pFirstTree.
     *
     * @param pFirstTree The output tree.
     * @param pSecondTree The input tree.
     */
    public static void merge(IFileTree pFirstTree, IFileTree pSecondTree) {
        for (IDataOrganizationNode node : pSecondTree.getRootNode().getChildren()) {
            merge(pFirstTree.getRootNode(), new ArrayList<ICollectionNode>(), node);
        }
    }

    /**
     * Flattens pNode and its children to a list which is returned.
     *
     * @param pNode The node to flatten.
     *
     * @return The list of pNode and all child nodes.
     */
    public static List<IDataOrganizationNode> flattenNode(ICollectionNode pNode) {
        List<IDataOrganizationNode> nodes = new ArrayList<>();
        nodes.add(pNode);
        return flattenNode(pNode, nodes);
    }

    /**
     * Flattens pNode and its children to a list which is returned. If
     * pResultList is null, a new list will be created an returned. Otherwise,
     * pResultList will be modified and returned.
     *
     * @param pNode The node to flatten.
     * @param pResultList The result list used for the recursive call. This list
     * is also returned at the end.
     *
     * @return The list of pNode and all child nodes.
     */
    public static List<IDataOrganizationNode> flattenNode(ICollectionNode pNode, List<IDataOrganizationNode> pResultList) {
        List<IDataOrganizationNode> nodes = pResultList;
        if (nodes == null) {
            nodes = new LinkedList<>();
        }

        for (IDataOrganizationNode node : pNode.getChildren()) {
            if (node instanceof ICollectionNode) {
                nodes.add(node);
                flattenNode((ICollectionNode) node, nodes);
            } else {
                nodes.add(node);
            }
        }
        return nodes;
    }

    /**
     * Generate a map of zip entries that have to be considered when zipping
     * pNode. This method calls itself recursively to traverse through
     * sub-directories. The provided map finally contains all files and folders
     * that have to be in the zip file. For files, the value of a map entry is
     * defined, for (empty) folders the value is null. The returned value is the
     * summed size of all files and can be used to decide whether or not the zip
     * should be finally created.
     *
     * @param pNode The node that should be handled. Filenodes will be directly
     * added to pMap, CollectionNodes will be traversed.
     * @param pPath The current part that is accessed.
     * @param pMap The map of resulting entries.
     *
     * @return The overall amount of data.
     */
    private static long generateZipEntries(IDataOrganizationNode pNode, String pPath, Map<String, File> pMap) {
        long size = 0;
        String basePath = (pPath == null) ? pNode.getName() : pPath + "/" + pNode.getName();
        if (pNode instanceof ICollectionNode) {
            ICollectionNode node = (ICollectionNode) pNode;
            if (!node.getChildren().isEmpty()) {
                for (IDataOrganizationNode childNode : node.getChildren()) {
                    size += generateZipEntries(childNode, basePath, pMap);
                }
            } else {
                //empty node
                pMap.put(basePath, null);
            }
        } else if (pNode instanceof IFileNode) {
            IFileNode node = (IFileNode) pNode;
            File f = null;
            String lfn = node.getLogicalFileName().asString();
            try {
                URL lfnUrl = new URL(lfn);
                if ("file".equals(lfnUrl.getProtocol())) {
                    f = new File(lfnUrl.toURI());
                    size += f.length();
                } else {
                    throw new MalformedURLException("Protocol " + lfnUrl.getProtocol() + " currently not supported.");
                }
                pMap.put(basePath, f);
            } catch (MalformedURLException | URISyntaxException ex) {
                LOGGER.warn("Unsupported LFN " + lfn + ". Only LFNs refering to locally accessible files are supported.");
            }
        } else {
            throw new IllegalArgumentException("Argument " + pNode + " is not a supported argument. Only nodes of type ICollectionNode or IFileNode are supported.");
        }
        return size;
    }

    /**
     * Zip the content of the provided collection node and write the result to
     * the provided output stream.
     *
     * @param pNode The node whose content is zipped.
     * @param pOutputStream The output stream the content is zipped to.
     *
     * @throws java.io.IOException If writing the data to the output stream
     * fails.
     */
    public static void zip(ICollectionNode pNode, OutputStream pOutputStream) throws IOException {
        zip(pNode, pOutputStream, -1l);
    }

    /**
     * Zip the content of the provided collection node and write the result to
     * the provided output stream.
     *
     * @param pNode The node whose content is zipped.
     * @param pOutputStream The output stream to which the zipped data will
     * send.
     * @param pSizeLimit The max. size in bytes that will be streamed.
     * Otherwise, an IOException will occur. Provide a value smaller 0 for no
     * limit.
     * @throws java.io.IOException If the size limit has been exceeded or
     * streaming fails.
     */
    public static void zip(ICollectionNode pNode, OutputStream pOutputStream, long pSizeLimit) throws IOException {
        byte[] buf = new byte[1024];

        HashMap<String, File> map = new HashMap<>();
        long size = DataOrganizationUtils.generateZipEntries(pNode, null, map);
        if (pSizeLimit > 0 && size > pSizeLimit) {
            throw new IOException("Size limit of " + pSizeLimit + " bytes exceeded. Zip operation aborted.");
        }
        Set<Entry<String, File>> entries = map.entrySet();
        try (ZipOutputStream zipOut = new ZipOutputStream(pOutputStream)) {
            for (Entry<String, File> entry : entries) {
                zipOut.putNextEntry(new ZipEntry(entry.getKey()));
                if (entry.getValue() != null) {
                    try (final FileInputStream in = new FileInputStream(entry.getValue())) {
                        // Add ZIP entry to output stream.
                        // Transfer bytes from the file to the ZIP file
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            zipOut.write(buf, 0, len);
                        }
                    }
                }
                zipOut.closeEntry();
            }
            zipOut.finish();
            zipOut.flush();
        }
    }

    /**
     * Return the amount of data in bytes associated with the provided object
     * id. This method implies, that for each object there is an attribute
     * SIZE_KEY where the attribute value is the amount of contained bytes as
     * long. The method will query for the root node of pObjectId (nodeDepth=0),
     * obtains the attributes' value and returns its long representation. If
     * nothing is found or something fails, 0 is returned.
     *
     * @param pObjectId The id of the object to query for.
     *
     * @return The amount of bytes associated with pObjectId or 0 if no size
     * could be obtained.
     */
    public static Long getAssociatedDataSize(DigitalObjectId pObjectId) {
        IMetaDataManager doMdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(PersistenceFacade.getInstance().getPersistenceUnitName());
        doMdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        String objectSelection;
        if (pObjectId != null) {
            objectSelection = "a.annotatedNode.digitalObjectIDStr='" + pObjectId.getStringRepresentation() + "' AND ";
        } else {
            objectSelection = "";
        }

        try {
            List<Object> result = doMdm.findResultList("SELECT a.value FROM Attribute a WHERE " + objectSelection + "a.key='" + SIZE_KEY + "' AND a.annotatedNode.nodeDepth=0");
            if (result.isEmpty()) {
                return 0l;
            } else if (result.size() == 1) {
                return Long.parseLong((String) result.get(0));
            } else {
                long sum = 0;
                for (Object entry : result) {
                    sum += Long.parseLong((String) entry);
                }
                return sum;
            }
        } catch (UnauthorizedAccessAttemptException ex) {
            return 0l;
        } finally {
            doMdm.close();
        }
    }

    /**
     * Return the amount of data in bytes of all digital objects.
     *
     * @return The amount of bytes associated with pObjectId or 0 if no size
     * could be obtained.
     */
    public static Long getAssociatedDataSize() {
        return getAssociatedDataSize(null);
    }

    /**
     * Return the number of files which are part of the data organization of the
     * digital object with the provided id. Only the FileNodes in the view
     * 'default' are counted.
     *
     * @param pObjectId The id of the object to query for.
     *
     * @return The number of FileNodes associated with pObjectId or 0 if no
     * amount could be obtained.
     */
    public static Long getAssociatedFileCount(DigitalObjectId pObjectId) {
        IMetaDataManager doMdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(PersistenceFacade.getInstance().getPersistenceUnitName());
        doMdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            Number result = (Number) doMdm.findSingleResult("SELECT COUNT(a) FROM DataOrganizationNode a WHERE a.digitalObjectIDStr='" + pObjectId.getStringRepresentation() + "' AND a.viewName='" + Constants.DEFAULT_VIEW + "' AND TYPE(a)=FileNode");
            return (result != null) ? result.longValue() : 0l;
        } catch (UnauthorizedAccessAttemptException ex) {
            return 0l;
        } finally {
            doMdm.close();
        }
    }

    /**
     * Return the number of files which are part the current KIT Data Manager
     * instance. Only the FileNodes in the view 'default' are counted.
     *
     * @return The number of all FileNodes aor 0 if no amount could be obtained.
     */
    public static Long getAssociatedFileCount() {
        IMetaDataManager doMdm = MetaDataManagement.getMetaDataManagement().getMetaDataManager(PersistenceFacade.getInstance().getPersistenceUnitName());
        doMdm.setAuthorizationContext(AuthorizationContext.factorySystemContext());
        try {
            Number result = (Number) doMdm.findSingleResult("SELECT COUNT(a) FROM DataOrganizationNode a WHERE a.viewName='" + Constants.DEFAULT_VIEW + "' AND TYPE(a)=FileNode");
            return (result != null) ? result.longValue() : 0l;
        } catch (UnauthorizedAccessAttemptException ex) {
            return 0l;
        } finally {
            doMdm.close();
        }
    }
}
