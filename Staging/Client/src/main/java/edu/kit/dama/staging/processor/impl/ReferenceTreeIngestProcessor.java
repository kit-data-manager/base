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
package edu.kit.dama.staging.processor.impl;

import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.commons.exceptions.ConfigurationException;
import edu.kit.dama.commons.exceptions.PropertyValidationException;
import edu.kit.dama.commons.types.DigitalObjectId;
import edu.kit.dama.commons.types.ILFN;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IFileTree;
import edu.kit.dama.mdm.dataorganization.impl.jpa.JPAImplUtil;
import edu.kit.dama.mdm.dataorganization.impl.util.Util;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizer;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityExistsException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.rest.staging.types.TransferTaskContainer;
import edu.kit.dama.staging.exceptions.StagingProcessorException;
import edu.kit.dama.staging.processor.AbstractStagingProcessor;
import edu.kit.dama.util.Constants;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class ReferenceTreeIngestProcessor extends AbstractStagingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceTreeIngestProcessor.class);

    /**
     * Default constructor
     *
     * @param pUniqueIdentifier The unique identifier of this processor. This
     * identifier should be used to name generated output files associated with
     * this processor.
     */
    public ReferenceTreeIngestProcessor(String pUniqueIdentifier) {
        super(pUniqueIdentifier);
    }

    @Override
    public String getName() {
        return "ReferenceTreeIngestProcessor";
    }

    @Override
    public void performPreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {

    }

    @Override
    public void finalizePreTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    }

    @Override
    public void performPostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
        LOGGER.debug("Starting {} by getting data node.", getName());
        ICollectionNode dataNode = (ICollectionNode) Util.getNodeByName(pContainer.getFileTree().getRootNode(), Constants.STAGING_DATA_FOLDER_NAME);
        Pattern p = Pattern.compile("(.*)_view\\.json");
        LOGGER.debug("Getting data organizer instance.");
        DataOrganizer organizer = DataOrganizerFactory.getInstance().getDataOrganizer();
        DigitalObjectId doid = new DigitalObjectId(pContainer.getTransferInformation().getDigitalObjectId());
        LOGGER.debug("Getting views for object with id {}.", doid);
        List<String> views = organizer.getViews(doid);

        for (IDataOrganizationNode node : dataNode.getChildren()) {
            LOGGER.debug("Processing next child or root node. Child name is: '{}", node.getName());
            if (node instanceof IFileNode) {
                LOGGER.debug("Checking child node with name '{}' for view description.", node.getName());
                String name = node.getName();
                Matcher m = p.matcher(name);
                if (m.matches()) {
                    LOGGER.debug("File node name '{}' matches pattern. Trying to extract view name.", name);
                    //found view definition
                    String viewName = m.group(1);
                    //replacing the default view is supported for this processor.
                    boolean defaultViewReplacement = Constants.DEFAULT_VIEW.equals(viewName);

                    if (!defaultViewReplacement) {
                        if (views.contains(viewName) || Util.isReservedViewName(viewName)) {
                            throw new StagingProcessorException(MessageFormat.format("Failed to register view with name '{0}'. "
                                    + "A view for this name already exists for object '{1}' or view name is reserved.", viewName, doid));
                        } else {
                            //add view name to list
                            views.add(viewName);
                        }
                    } else {
                        LOGGER.debug("Detected replacement request for view 'default'.");
                    }
                    LOGGER.debug("Extracted view name '{}', procesing with reading JSON description.", viewName);
                    ILFN lfn = ((IFileNode) node).getLogicalFileName();
                    LOGGER.debug("Checking protocol of LFN '{}' (should be 'file').", lfn);
                    String sLfn = lfn.asString();
                    if (!sLfn.startsWith("file:/")) {
                        //not possible, yet
                        throw new StagingProcessorException(MessageFormat.format("Cannot extract view definition from LFN {0}. Remote access not implemented.", sLfn));
                    }
                    LOGGER.debug("Reading view description from URL {}", sLfn);
                    try {
                        String content = FileUtils.readFileToString(new File(new URL(sLfn).toURI()));
                        LOGGER.debug("Creating JSON object from file content.");
                        JSONObject viewObject = new JSONObject(content);
                        LOGGER.debug("JSON object successfully read. Extracting view information.");
                        IFileTree tree = Util.jsonViewToFileTree(viewObject, true, false);
                        LOGGER.debug("Successfully read file tree from JSON object. Validating view name.");
                        if (!viewName.equals(tree.getViewName())) {
                            throw new StagingProcessorException(MessageFormat.format("Validation failed. View name '{0}' provided by JSON description does not fit view name extracted from node name '{1}'.", tree.getViewName(), viewName));
                        }
                        LOGGER.debug("View description successfully validated. Registering view in DataOrganization.");
                        if (defaultViewReplacement) {
                            LOGGER.debug("Preparing default view replacement. Renaming current view 'default' to 'data'.");
                            int changedNodes = JPAImplUtil.renameView(doid, Constants.DEFAULT_VIEW, Constants.DATA_VIEW, AuthorizationContext.factorySystemContext());
                            LOGGER.debug("View successfully renamed. {} data organization nodes updated.", changedNodes);
                        }
                        LOGGER.debug("Storing file tree for view '{}' in database for object with id {}.", viewName, doid);
                        tree.setDigitalObjectId(doid);
                        organizer.createFileTree(tree);
                        LOGGER.debug("File tree for view '{}' successfully registered. Checking for additional view definitions.", viewName);
                    } catch (MalformedURLException | URISyntaxException ex) {
                        throw new StagingProcessorException(MessageFormat.format("Failed to parse URL from LFN {0}.", sLfn), ex);
                    } catch (IOException ex) {
                        throw new StagingProcessorException(MessageFormat.format("Failed to read view data from file with URL {0}.", sLfn), ex);
                    } catch (EntityExistsException ex) {
                        throw new StagingProcessorException("Failed to register file tree using data organizer. An according tree seems to exist already.", ex);
                    } catch (UnauthorizedAccessAttemptException ex) {
                        throw new StagingProcessorException("Failed to rename 'default' view to 'data'.", ex);
                    } catch (InvalidNodeIdException ex) {
                        //this should never happen, as the provided tree should not contain any id references to existing nodes as they where not known before executing this processor.
                        throw new StagingProcessorException("Illegal file tree provided. Ingested file tree description must not contain any node references.", ex);
                    }
                }
            }
        }
        LOGGER.debug("All child nodes of root are processed. Processor '{}' has finished.", getName());
    }

    @Override
    public void finalizePostTransferProcessing(TransferTaskContainer pContainer) throws StagingProcessorException {
    }

    @Override
    public String[] getInternalPropertyKeys() {
        return new String[]{};
    }

    @Override
    public String getInternalPropertyDescription(String pKey
    ) {
        return "";
    }

    @Override
    public String[] getUserPropertyKeys() {
        return new String[]{};
    }

    @Override
    public String getUserPropertyDescription(String pKey
    ) {
        return "";
    }

    @Override
    public void validateProperties(Properties pProperties) throws PropertyValidationException {
    }

    @Override
    public void configure(Properties pProperties) throws PropertyValidationException, ConfigurationException {
    }

}
