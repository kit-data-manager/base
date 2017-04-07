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
package edu.kit.dama.rest.sharing.services.interfaces;

import com.qmino.miredot.annotations.ReturnType;
import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.rest.base.ICommonRestInterface;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.util.Constants;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author mf6319
 */
@Path("/rest/audit")
public interface IAuditService extends ICommonRestInterface {

    /**
     * Returns a list of audit events for the resource with the provided pid. By
     * default, a call to this method returns the first 10 (or less) events
     * associated with the resource with the provided pid. Furthermore, the list
     * of events can be filtered by different criterias. Multiple criterias are
     * AND connected.
     *
     * In order to obtain audit information the caller must possess at least
     * MANAGER permissions in the group provided with the call. If not, access
     * is forbidden.
     *
     * @summary Get a list of audit events for a resource, which may be filtered
     * by different criterias.
     *
     * @param groupId The id of the group the access is authorized with.
     * @param pid The pid of the audited resource. This pid will typically be
     * the digital object id, but may also be the unique identifier of a
     * securable resource. This argument is mandatory.
     * @param eventType Only return events of the provided type. If no type is
     * provided, all types of events are returned. Valid event types are:
     *
     * <pre>
     *  CREATION
     *  INGESTION
     *  METADATA_MODIFICATION
     *  CONTENT_MODIFICATION
     *  CONTENT_REMOVAL
     *  MIGRATION
     *  REPLICATION
     *  VALIDATION
     *  DERIVATIVE_CREATION
     *  DELETION
     *  DEACCESSION
     * </pre>
     *
     * @param eventTrigger Only return events triggered by the provided trigger.
     * If no type is provided, all types of events are returned. Valid triggers
     * are:
     *
     * <pre>
     *  INTERNAL
     *  EXTERNAL
     * </pre>
     *
     * @param fromDate The start date from where on events are returned. The
     * format pattern must match yyyy-MM-dd'T'HH:mm:ss'Z' (UTC), the default
     * value is 1970-01-01T00:00:00Z
     * @param toDate The end date from where on events are returned. The format
     * pattern must match yyyy-MM-dd'T'HH:mm:ss'Z' (UTC), the default value is
     * NOW.
     * @param category The category of the event. The category format is defined
     * by the server-side configuration of the Audit service. By default, the
     * category must start with 'audit.' followed by a sub-category determining
     * which kind of ressource is affected, e.g. 'digitalObject', such that the
     * final category argument should look like 'audit.digitalObject' in order
     * to filter the correct events.
     * @param first The first index.
     * @param results The max. number of results.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of audit events.
     */
    @GET
    @Path(value = "/events/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.audit.types.AuditEvent>")
    IEntityWrapper<AuditEvent> getEvents(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @QueryParam("pid") String pid,
            @QueryParam("eventType") String eventType,
            @QueryParam("eventTrigger") String eventTrigger,
            @QueryParam("fromDate") @DefaultValue("1970-01-01T00:00:00Z") String fromDate,
            @QueryParam("toDate") String toDate,
            @QueryParam("category") String category,
            @QueryParam("first") @DefaultValue(Constants.REST_DEFAULT_MIN_INDEX) Integer first,
            @QueryParam("results") @DefaultValue(Constants.REST_DEFAULT_MAX_RESULTS) Integer results,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Returns an audit event by its id.
     *
     * In order to obtain audit information the caller must possess at least
     * MANAGER permissions in the group provided with the call. If not, access
     * is forbidden.
     *
     * @summary Get an audit event by its id.
     *
     * @param groupId The id of the group the investigation belongs to.
     * @param id The id of the audit event.
     * @param hc The HttpContext for OAuth check.
     *
     * @return A list of audit events.
     */
    @GET
    @Path(value = "/events/{id}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("edu.kit.dama.rest.base.IEntityWrapper<edu.kit.dama.mdm.audit.types.AuditEvent>")
    IEntityWrapper<AuditEvent> getEventById(
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @PathParam("id") Long id,
            @javax.ws.rs.core.Context HttpContext hc);

    /**
     * Create a new event using the provider properties. Events added using the
     * REST endpoint will have the trigger set to AuditEvent.TRIGGER.EXTERNAL.
     *
     * @summary Add a new audit event of a specific type and with the provided
     * category.
     *
     * @param groupId The id of the group the investigation belongs to.
     * @param pid The pid of the entity the audit event belongs to.
     * @param eventType The event type. Valid event types are:
     *
     * <pre>
     *  CREATION
     *  INGESTION
     *  METADATA_MODIFICATION
     *  CONTENT_MODIFICATION
     *  CONTENT_REMOVAL
     *  MIGRATION
     *  REPLICATION
     *  VALIDATION
     *  DERIVATIVE_CREATION
     *  DELETION
     *  DEACCESSION
     * </pre>
     *
     * @param category The event category. Be aware that the category format is
     * defined by the server-side configuration of the Audit service. By
     * default, the category must start with 'audit.' followed by a sub-category
     * determining which kind of ressource is affected, e.g. 'digitalObject',
     * such that the final category argument should look like
     * 'audit.digitalObject' in order to be able to consumer the event properly.
     * @param details A JSON serialized array of event details looking as
     * follows:
     *
     * <pre>
     * "details":[{
     *   "detailType":"ARGUMENT",
     *   "dataType":"java.lang.String",
     *   "name":"groupId",
     *   "value":"USERS"
     * },{
     *   "detailType":"COMMENT",
     *   "value":"This is a comment."
     * }]
     * </pre>
     *
     * @param hc The HttpContext for OAuth check.
     *
     * @return The HTTP response.
     */
    @POST
    @Path(value = "/events/")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ReturnType("java.lang.Void")
    Response addEvent(
            @FormParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @FormParam("pid") String pid,
            @FormParam("eventType") String eventType,
            @FormParam("category") String category,
            @FormParam("details") String details,
            @javax.ws.rs.core.Context HttpContext hc);

}
