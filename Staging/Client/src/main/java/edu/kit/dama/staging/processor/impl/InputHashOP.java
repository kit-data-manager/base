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
package edu.kit.dama.staging.processor.impl;

import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.lsdf.adalapi.exception.AdalapiException;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.staging.util.StagingUtils;
import edu.kit.dama.util.Constants;
import edu.kit.tools.url.URLCreator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
//Only for demonstration purposes, should not be used in production.
@Deprecated
public class InputHashOP extends AbstractStagingProcessor {

    /**
     * The logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(InputHashOP.class);
    /**
     * The hash type configuration property
     */
    private static final String HASH_TYPE_PROPERTY = "hash";
    /**
     * The currently used hash type
     */
    private HASH_TYPE hashType = HASH_TYPE.MD5;

    /**
     * Enum for supported hash types
     */
    public enum HASH_TYPE {

        MD5, SHA, SHA256, SHA384, SHA512;
    }
    /**
     * The map which contains all hashes.
     */
    private final Map<String, String> hashes = new HashMap<String, String>();

    /**
     * Default constructor
     *
     * @param pUniqueIdentifier The unique identifier of this processor. This
     * identifier should be used to name generated output files associated with
     * this processor.
     */
    public InputHashOP(String pUniqueIdentifier) {
        super(pUniqueIdentifier);
    }

    @Override
    public final String getName() {
        return "MD5Hash";
    }

    @Override
    public final void configure(Properties pProperties) throws PropertyValidationException, ConfigurationException {
        String hashValue = pProperties.getProperty(HASH_TYPE_PROPERTY, HASH_TYPE.MD5.toString());
        try {
            hashType = HASH_TYPE.valueOf(hashValue);
        } catch (IllegalArgumentException iae) {
            LOGGER.warn("Failed to parse hash type from property value {}. Using default value MD5", hashValue);
            hashType = HASH_TYPE.MD5;
        }
    }

    @Override
    public String[] getInternalPropertyKeys() {
        return new String[]{HASH_TYPE_PROPERTY};
    }

    @Override
    public String getInternalPropertyDescription(String pKey) {
        if (HASH_TYPE_PROPERTY.equals(pKey)) {
            return "The hash algorithm which is used to generate the hashes. Possible values are: " + Arrays.toString(HASH_TYPE.values());
        }
        return null;
    }

    @Override
    public String[] getUserPropertyKeys() {
        return new String[]{};
    }

    @Override
    public String getUserPropertyDescription(String pKey) {
        return null;
    }

    @Override
    public void validateProperties(Properties pProperties) throws PropertyValidationException {
        if (pProperties == null) {
            throw new IllegalArgumentException("Argument pProperties must not be null");
        }
        String selectedHashType = pProperties.getProperty(HASH_TYPE_PROPERTY);
        try {
            if (selectedHashType == null) {
                //no hash type set
                throw new IllegalArgumentException();
            }
            //check hash type ... produces IllegalArgumentException in case of an error
            LOGGER.debug("Validated hash type to: {}", HASH_TYPE.valueOf(selectedHashType));
        } catch (IllegalArgumentException iae) {
            throw new PropertyValidationException("Failed to parse hash type from property value " + selectedHashType + ". Value not part of " + Arrays.toString(HASH_TYPE.values()), iae);
        }
    }

    @Override
    public final void performPreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        //create list of hashes for input directory
        ICollectionNode root = pContainer.getFileTree().getRootNode();
        IDataOrganizationNode dataSubTree = Util.getNodeByName(root, Constants.STAGING_DATA_FOLDER_NAME);

        if (!(dataSubTree instanceof ICollectionNode)) {
            throw new StagingProcessorException("Data node does not implement ICollectionNode");
        }

        List<IDataOrganizationNode> dataNodes = DataOrganizationUtils.flattenNode((ICollectionNode) dataSubTree);

        for (IDataOrganizationNode node : dataNodes) {
            //hash file nodes
            if (node instanceof IFileNode) {
                hashInputFile((IFileNode) node);
            }
        }
    }

    @Override
    public final void performPostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        ICollectionNode root = pContainer.getFileTree().getRootNode();
        //initialize hashes list
        LOGGER.debug("Preparing server-side hash validation");
        //obtain "generated" folder node
        IDataOrganizationNode generatedSubTree = Util.getNodeByName(root, "generated");
        //obtain preprocessing results
        IFileNode prepProcessingOutputNode = (IFileNode) Util.getNodeByName((ICollectionNode) generatedSubTree, getUniqueIdentifier() + ".proc");

        BufferedReader bin = null;
        String lfnString = prepProcessingOutputNode.getLogicalFileName().asString();
        try {
            URL u = new URL(lfnString);
            LOGGER.debug("Checking hash input file {}", u);
            File inputFile = new File(u.toURI());
            if (!inputFile.exists()) {
                throw new StagingProcessorException("Hash input file " + u + " does not exist");
            }
            LOGGER.debug("Reading hash input file");
            bin = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
            //read header
            String line = bin.readLine();
            while (line != null) {
                String[] lineSplit = line.split("\t");
                if (lineSplit.length == 2 && lineSplit[0] != null && lineSplit[1] != null) {
                    hashes.put(lineSplit[0].trim(), lineSplit[1].trim());
                }
                line = bin.readLine();
            }
        } catch (FileNotFoundException ex) {
            throw new StagingProcessorException("Failed to access hash input file " + lfnString, ex);
        } catch (IOException ex) {
            throw new StagingProcessorException("Failed to read hash input file " + lfnString, ex);
        } catch (URISyntaxException ex) {
            throw new StagingProcessorException("LFN of hash input file is not in a valid URI format " + lfnString, ex);
        } finally {
            if (bin != null) {
                try {
                    bin.close();
                } catch (IOException ex) {
                }
            }
        }

        //check hashes for all data nodes
        IDataOrganizationNode dataSubTree = Util.getNodeByName(root, Constants.STAGING_DATA_FOLDER_NAME);
        if (!(dataSubTree instanceof ICollectionNode)) {
            throw new StagingProcessorException("Data node does not implement ICollectionNode");
        }

        List<IDataOrganizationNode> dataNodes = DataOrganizationUtils.flattenNode((ICollectionNode) dataSubTree);

        for (IDataOrganizationNode node : dataNodes) {
            //hash file nodes
            if (node instanceof IFileNode) {
                validateOutputFile((IFileNode) node);
            }
        }
    }

    /**
     * Creates a hash for pNode and stores it in the internally managed map. At
     * the end, the map is serialized to a file and submitted to the destination
     * as 'generated' file.
     *
     * @param pNode The file node to add. The LFN associated with this node must
     * be mappable to a file URI.
     *
     * @throws StagingProcessorException If there was an error while generating
     * the hash.
     */
    private void hashInputFile(IFileNode pNode) throws StagingProcessorException {
        try {
            File currentFile = new File(new URL(pNode.getLogicalFileName().asString()).toURI());
            //get the path of pNode 
            List<ICollectionNode> parents = DataOrganizationUtils.walkParents(pNode);
            StringBuilder path = new StringBuilder();
            for (ICollectionNode parent : parents) {
                String subElementName = parent.getName();
                if (subElementName != null) {
                    path.append(subElementName);
                }
            }
            hashes.put(path.toString(), hashFile(currentFile));
        } catch (URISyntaxException use) {
            throw new StagingProcessorException("Failed to obtain file from URL", use);
        } catch (IOException ioe) {
            throw new StagingProcessorException("Failed to read from file", ioe);
        }
    }

    /**
     * Validate a hash for pNode using the internal map of hashes.
     *
     * @param pNode The file node to add. The LFN associated with this node must
     * be mappable to a file URI.
     *
     * @throws StagingProcessorException If there was an error while generating
     * the hash.
     */
    private void validateOutputFile(IFileNode pNode) throws StagingProcessorException {
        try {
            File currentFile = new File(new URL(pNode.getLogicalFileName().asString()).toURI());
            //get the path of pNode 
            List<ICollectionNode> parents = DataOrganizationUtils.walkParents(pNode);
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < parents.size(); i++) {
                String subElementName = parents.get(i).getName();
                if (subElementName != null) {
                    path.append(subElementName);
                }
            }
            String inputHash = hashes.get(path.toString());
            String outputHash = hashFile(currentFile);

            if (inputHash == null || !inputHash.equals(outputHash)) {
                throw new StagingProcessorException("Stored hash '" + inputHash + "' for path '" + path + "' is not equal server-side hash '" + outputHash + "'");
            }
        } catch (URISyntaxException use) {
            throw new StagingProcessorException("Failed to obtain file from URL", use);
        } catch (IOException ioe) {
            throw new StagingProcessorException("Failed to read from file", ioe);
        }
    }

    /**
     * Hash a single file using the internally defined hash algorithm.
     *
     * @param pFile The file to hash
     *
     * @return The hash value
     *
     * @throws IOException If pFile cannot be read
     */
    private String hashFile(File pFile) throws IOException {
        LOGGER.debug("Hashing file {}", pFile.getAbsolutePath());
        InputStream is = null;
        try {
            is = new FileInputStream(pFile);
            String hash;
            switch (hashType) {
                case SHA:
                    hash = DigestUtils.sha1Hex(is);
                    break;
                case SHA256:
                    hash = DigestUtils.sha256Hex(is);
                    break;
                case SHA384:
                    hash = DigestUtils.sha384Hex(is);
                    break;
                case SHA512:
                    hash = DigestUtils.sha512Hex(is);
                    break;
                default:
                    hash = DigestUtils.md5Hex(is);
            }
            return hash;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ioe) {
                    //ignore
                }
            }
        }
    }

    @Override
    public void finalizePreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        FileOutputStream hashOutput = null;
        try {

            File tmpDir = new File(StagingUtils.getTempDir(pContainer));
            String outputFilename = getUniqueIdentifier() + ".proc";
            File localFile = new File(URLCreator.appendToURL(tmpDir.toURI().toURL(), outputFilename).toURI());

            hashOutput = new FileOutputStream(localFile);
            hashOutput.write(("#Digest Algorithm: " + hashType.toString() + "\n").getBytes());
            Iterator<Entry<String, String>> fileIterator = hashes.entrySet().iterator();

            while (fileIterator.hasNext()) {
                Entry<String, String> next = fileIterator.next();
                String nextFile = next.getKey();
                String hash = next.getValue();
                hashOutput.write((nextFile + "\t" + hash + "\n").getBytes());
            }
            hashOutput.flush();
            //add generated file to container
            pContainer.addGeneratedFile(localFile);
        } catch (URISyntaxException use) {
            throw new StagingProcessorException("Failed to create file from URL", use);
        } catch (MalformedURLException mux) {
            throw new StagingProcessorException("Failed to add generated file to transfer task container", mux);
        } catch (AdalapiException aex) {
            throw new StagingProcessorException("Failed to add generated file to transfer task container", aex);
        } catch (FileNotFoundException fnfe) {
            throw new StagingProcessorException("Failed to open file output stream", fnfe);
        } catch (IOException ioe) {
            throw new StagingProcessorException("Failed to write data to output stream", ioe);
        } finally {
            if (hashOutput != null) {
                try {
                    hashOutput.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    @Override
    public void finalizePostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        //nothing to do here
    }
}
