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
package edu.kit.dama.rest.dataorganization.services.impl.util;

import edu.kit.dama.mdm.base.DigitalObject;
import edu.kit.dama.mdm.dataorganization.entity.core.ICollectionNode;
import edu.kit.dama.mdm.dataorganization.entity.core.IDataOrganizationNode;
import edu.kit.dama.mdm.dataorganization.service.core.DataOrganizerFactory;
import edu.kit.dama.mdm.dataorganization.service.exception.EntityNotFoundException;
import edu.kit.dama.mdm.dataorganization.service.exception.InvalidNodeIdException;
import edu.kit.dama.rest.dataorganization.services.impl.DataOrganizationRestServiceImpl;
import edu.kit.dama.rest.dataorganization.types.ElementPath;
import edu.kit.dama.staging.util.DataOrganizationUtils;
import edu.kit.dama.util.Constants;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
public class PublicDownloadHandler implements IDownloadHandler<DigitalObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicDownloadHandler.class);

    @Override
    public Response prepareStream(DigitalObject object) throws IOException {
        //Check which data organization view will be returned.
        List<String> views = DataOrganizerFactory.getInstance().getDataOrganizer().getViews(object.getDigitalObjectId());
        //View 'public' is default result, check if this view exists.
        String viewName = Constants.PUBLIC_VIEW;

        if (views.contains(viewName)) {
            LOGGER.info("View 'public' found for object with id '{}'.", object);
        } else {
            LOGGER.info("No view 'public' found for object with id '{}'. Using view 'default'.", object);
            viewName = Constants.DEFAULT_VIEW;
        }

        try {
            LOGGER.debug("Obtaining root node for view '{}'", viewName);
            ElementPath p = new ElementPath("/");
            final IDataOrganizationNode node = p.getNodeForPath(object.getDigitalObjectId(), viewName);

            LOGGER.debug("Root node obtained. Establishing piped streams.");
            PipedInputStream in = new PipedInputStream();
            final PipedOutputStream out = new PipedOutputStream(in);
            LOGGER.debug("Starting streaming thread.");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        LOGGER.debug("Start zipping operation to piped output stream.");
                        DataOrganizationUtils.zip((ICollectionNode) node, out);
                        LOGGER.debug("Zipping operation finshed.");
                    } catch (IOException ex) {
                        LOGGER.error("Failed to zip node content to output stream.", ex);
                    } finally {
                        if (out != null) {
                            try {
                                out.flush();
                                out.close();
                            } catch (IOException ex) {
                                //ignore
                            }
                        }
                    }
                }
            }
            ).start();
            String resultName = object.getDigitalObjectIdentifier() + ".zip";
            LOGGER.debug("Returning response file named '{}' in stream linked to piped zip stream.", resultName);
            return Response.ok(in, MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition",
                    "attachment; filename=" + resultName).build();
        } catch (EntityNotFoundException | InvalidNodeIdException ex) {
            LOGGER.error("Failed to load root node for Data organization view '" + Constants.PUBLIC_VIEW + "' of object with id '" + object.getDigitalObjectIdentifier() + "'.", ex);
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (IOException ex) {
            LOGGER.error("Failed to perform published data download.", ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

    }

}
