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
package edu.kit.dama.mdm.content.oaipmh.service;

import edu.kit.dama.mdm.content.oaipmh.impl.SimpleOAIPMHRepository;
import edu.kit.dama.mdm.content.oaipmh.util.OAIPMHBuilder;
import java.text.ParseException;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.openarchives.oai._2.OAIPMHerrorcodeType;
import org.openarchives.oai._2.VerbType;
import edu.kit.dama.mdm.content.oaipmh.AbstractOAIPMHRepository;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jejkal
 */
@Path("/oaipmh")
public class OAIPMHServiceImpl {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OAIPMHServiceImpl.class);

    private final AbstractOAIPMHRepository repository = new SimpleOAIPMHRepository();

    @GET
    @Produces("application/xml")
    public Response getOaiPmh(@QueryParam("verb") String verb, @QueryParam("from") String from, @QueryParam("until") String until, @QueryParam("set") String set, @QueryParam("identifier") String identifier, @QueryParam("metadataPrefix") String metadataPrefix, @QueryParam("resumptionToken") String resumptionToken) {
        VerbType verbType = null;
        LOGGER.debug("Checking provided OAI-PMH verb {}", verb);
        try {
            verbType = VerbType.fromValue(verb);
            LOGGER.debug("Verb is valid.");
        } catch (IllegalArgumentException ex) {
            //wrong verb...OAIPMHBuilder will handle this
            LOGGER.warn("Verb '" + verb + "' is invalid. OAI-PMH error will be returned.", ex);
        }

        Date fromDate = null;
        Date untilDate = null;
        boolean wrongDateFormat = false;
        try {
            LOGGER.debug("Checking 'from' and 'until' dates.");
            if (from != null) {
                LOGGER.debug("Checking 'from' value {}", from);
                fromDate = repository.getDateFormat().parse(from);
                LOGGER.debug("Successfully parse 'from' value.");
            }
            if (until != null) {
                LOGGER.debug("Checking 'until' value {}", until);
                untilDate = repository.getDateFormat().parse(until);
                LOGGER.debug("Successfully parse 'until' value.");

            }
        } catch (ParseException ex) {
            LOGGER.warn("'from' and/or 'until' date are in an invalid format.  OAI-PMH error will be returned.", ex);
            wrongDateFormat = true;
        }

        OAIPMHBuilder builder = OAIPMHBuilder.init(repository, verbType, metadataPrefix, identifier, fromDate, untilDate, resumptionToken);
        if (!builder.isError()) {
            if (wrongDateFormat) {
                LOGGER.debug("Returning BAD_ARGUMENT error due to wrong date format.");
                //date format is wrong
                builder.addError(OAIPMHerrorcodeType.BAD_ARGUMENT, "Either from and/or until date are in the wrong format.");
            } else if (!(metadataPrefix == null || repository.isPrefixSupported(metadataPrefix))) {
                //prefix is not null and not supported
                LOGGER.debug("Returning CANNOT_DISSEMINATE_FORMAT error due to unsupported metadata prefix '{}'.", metadataPrefix);
                builder.addError(OAIPMHerrorcodeType.CANNOT_DISSEMINATE_FORMAT, "Metadata prefix " + metadataPrefix + " not supported by repository.");
            } else {
                LOGGER.debug("Handling request by repository implementation.");
                //if request is wrong, error is set already at this point...if no error, continue
                repository.handleRequest(builder);
            }
        }

        //build the result and return it.
        LOGGER.debug("Building and returning OAI-PMH response.");
        return Response.ok(builder.build()).build();
    }
}
