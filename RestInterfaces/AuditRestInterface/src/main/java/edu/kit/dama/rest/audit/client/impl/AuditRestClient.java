/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
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
package edu.kit.dama.rest.audit.client.impl;

import com.sun.jersey.api.client.ClientResponse;
import edu.kit.dama.rest.audit.types.AuditEventWrapper;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import edu.kit.dama.mdm.audit.types.AuditDetail;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.rest.AbstractRestClient;
import edu.kit.dama.rest.SimpleRESTContext;
import edu.kit.dama.rest.util.RestClientUtils;
import edu.kit.dama.util.Constants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import javax.ws.rs.core.MultivaluedMap;

/**
 * Rest client for Sharing service.
 *
 * @author mf6319
 */
public final class AuditRestClient extends AbstractRestClient {

    /**
     * The logger
     */
    //private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AuditRestClient.class);
    //<editor-fold defaultstate="collapsed" desc="Parameter names">
    /**
     * PID parameter
     */
    protected static final String PARAMETER_PID = "pid";
    /**
     * Event type parameter
     */
    protected static final String PARAMETER_EVENT_TYPE = "eventType";
    /**
     * Event trigger parameter
     */
    protected static final String PARAMETER_EVENT_TRIGGER = "eventTrigger";
    /**
     * From date parameter
     */
    protected static final String PARAMETER_FROM_DATE = "fromDate";
    /**
     * To date parameter
     */
    protected static final String PARAMETER_TO_DATE = "toDate";
    /**
     * Category parameter
     */
    protected static final String PARAMETER_CATEGORY = "category";
    /**
     * Details parameter
     */
    protected static final String PARAMETER_DETAILS = "details";

//</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="URL components">
    /**
     * Events part.
     */
    private static final String EVENTS_URL = "/events";

    /**
     * Get event by given id.
     */
    private static final String EVENT_BY_ID = EVENTS_URL + "/{0}";

    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
// </editor-fold>

    /**
     * Create a REST client with a predefined context.
     *
     * @param rootUrl root url of the staging service. (e.g.:
     * "http://dama.lsdf.kit.edu/KITDM/rest/UserGroupService")
     * @param pContext initial context
     */
    public AuditRestClient(String rootUrl, SimpleRESTContext pContext) {
        super(rootUrl, pContext);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    // <editor-fold defaultstate="collapsed" desc="Generic Rest methods (GET, PUT, POST, DELETE)">
    /**
     * Perform a get for an AuditEvent.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     *
     * @return AuditEventWrapper.
     */
    private AuditEventWrapper performAuditEventGet(String pPath, MultivaluedMap pQueryParams) {
        return RestClientUtils.performGet(AuditEventWrapper.class, getWebResource(pPath), pQueryParams);
    }

    /**
     * Perform a post of an AuditEvent.
     *
     * @param pPath url
     * @param pQueryParams url parameters
     * @param pFormParams Form parameters
     *
     * @return AuditEventWrapper.
     */
    private boolean performAuditEventPost(String pPath, MultivaluedMap pQueryParams, MultivaluedMap pFormParams) {
        return ((ClientResponse) RestClientUtils.performPost(null, getWebResource(pPath), pQueryParams, pFormParams)).getStatus() == 200;
    }

    // </editor-fold>
    //<editor-fold defaultstate="collapsed" desc="get[Events|EventById]">
    /**
     * Get all audit events associated with the resource with the provided pid.
     * The list of events can be filtered by a number of criterias, e.g. to
     * return only events of a specific type.
     *
     * @param pid The pid of the audited resource. This pid will typically be
     * the digital object id, but may also be the unique identifier of a
     * securable resource. This argument is mandatory.
     * @param eventType Only return events of the provided type. If no type is
     * provided, all types of events are returned.
     * @param eventTrigger Only return events triggered by the provided trigger.
     * If no type is provided, all types of events are returned.
     * @param fromDate The start date from where on events are returned. The
     * format pattern must match yyyy-MM-dd'T'HH:mm:ss'Z' (UTC), the default
     * value is 1970-01-01T00:00:00Z
     * @param toDate The end date from where on events are returned. The format
     * pattern must match yyyy-MM-dd'T'HH:mm:ss'Z' (UTC), the default value is
     * NOW.
     * @param category The category of the event.
     * @param first The first index.
     * @param results The max. number of results.
     * @param pGroupId The id of the group in whose name the users will be
     * queried.
     * @param pSecurityContext The SecurityContext for OAuth check.
     *
     * @return The list of audit events wrapped by an AuditEventWrapper.
     *
     * @see edu.kit.dama.rest.audit.types.AuditEventWrapper
     */
    public AuditEventWrapper getEvents(
            String pid,
            AuditEvent.TYPE eventType,
            AuditEvent.TRIGGER eventTrigger,
            String fromDate,
            String toDate,
            String category,
            int first,
            int results,
            String pGroupId,
            SimpleRESTContext pSecurityContext) {
        AuditEventWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams;
        queryParams = new MultivaluedMapImpl();
        if (pid == null) {
            throw new IllegalArgumentException("Argument 'pid' must be null.");
        }

        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        queryParams.add(PARAMETER_PID, pid);

        if (eventType != null) {
            queryParams.add(PARAMETER_EVENT_TYPE, eventType.toString());
        }
        if (eventTrigger != null) {
            queryParams.add(PARAMETER_EVENT_TRIGGER, eventTrigger.toString());
        }
        if (fromDate != null) {
            queryParams.add(PARAMETER_FROM_DATE, df.format(fromDate));
        }
        if (toDate != null) {
            queryParams.add(PARAMETER_TO_DATE, df.format(toDate));
        }
        if (category != null) {
            queryParams.add(PARAMETER_CATEGORY, category);
        }

        queryParams.add(Constants.REST_PARAMETER_FIRST, Integer.toString(first));
        queryParams.add(Constants.REST_PARAMETER_RESULT, Integer.toString(results));

        returnValue = performAuditEventGet(EVENTS_URL, queryParams);
        return returnValue;
    }

    /**
     * Get all audit events associated with the resource with the provided pid.
     * The list of events can be filtered by a number of criterias, e.g. to
     * return only events of a specific type.
     *
     * @param pid The pid of the audited resource. This pid will typically be
     * the digital object id, but may also be the unique identifier of a
     * securable resource. This argument is mandatory.
     * @param eventType Only return events of the provided type. If no type is
     * provided, all types of events are returned.
     * @param eventTrigger Only return events triggered by the provided trigger.
     * If no type is provided, all types of events are returned.
     * @param fromDate The start date from where on events are returned. The
     * format pattern must match yyyy-MM-dd'T'HH:mm:ss'Z' (UTC), the default
     * value is 1970-01-01T00:00:00Z
     * @param toDate The end date from where on events are returned. The format
     * pattern must match yyyy-MM-dd'T'HH:mm:ss'Z' (UTC), the default value is
     * NOW.
     * @param category The category of the event.
     * @param first The first index.
     * @param results The max. number of results.
     * @param pGroupId The id of the group in whose name the users will be
     * queried.
     *
     * @return The list of audit events wrapped by an AuditEventWrapper.
     *
     * @see edu.kit.dama.rest.audit.types.AuditEventWrapper
     */
    public AuditEventWrapper getEvents(
            String pid,
            AuditEvent.TYPE eventType,
            AuditEvent.TRIGGER eventTrigger,
            String fromDate,
            String toDate,
            String category,
            int first,
            int results,
            String pGroupId) {
        return getEvents(pid, eventType, eventTrigger, fromDate, toDate, category, first, results, pGroupId, null);
    }

    /**
     * Get all audit events associated with the resource with the provided pid.
     *
     * @param pid The pid of the audited resource. This pid will typically be
     * the digital object id, but may also be the unique identifier of a
     * securable resource. This argument is mandatory.
     *
     * @param first The first index.
     * @param results The max. number of results.
     * @param pGroupId The id of the group in whose name the audit events will
     * be queried.
     *
     * @return The list of audit events wrapped by an AuditEventWrapper.
     *
     * @see edu.kit.dama.rest.audit.types.AuditEventWrapper
     */
    public AuditEventWrapper getEvents(
            String pid,
            int first,
            int results,
            String pGroupId) {
        return getEvents(pid, null, null, null, null, null, first, results, pGroupId);
    }

    /**
     * Get a single audit event by its id.
     *
     * @param id The id of the audit event.
     * @param pGroupId The id of the group in whose name the audit event will be
     * queried.
     * @param pSecurityContext The SimpleRESTContext for OAuth check.
     *
     * @return One audit event wrapped by an AuditEventWrapper.
     *
     * @see edu.kit.dama.rest.audit.types.AuditEventWrapper
     */
    public AuditEventWrapper getEventById(
            long id,
            String pGroupId,
            SimpleRESTContext pSecurityContext) {
        AuditEventWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        MultivaluedMap queryParams;
        queryParams = new MultivaluedMapImpl();
        if (pGroupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, pGroupId);
        }

        returnValue = performAuditEventGet(RestClientUtils.encodeUrl(EVENT_BY_ID, id), queryParams);
        return returnValue;
    }

    /**
     * Get a single audit event by its id.
     *
     * @param id The id of the audit event.
     * @param pGroupId The id of the group in whose name the audit event will be
     * queried.
     *
     * @return One audit event wrapped by an AuditEventWrapper.
     *
     * @see edu.kit.dama.rest.audit.types.AuditEventWrapper
     */
    public AuditEventWrapper getEventById(long id, String pGroupId) {
        return getEventById(id, pGroupId, null);
    }
//</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="create[Event]">

    /**
     * Create a new external audit event with the provided properties.
     *
     * @param pid The pid of the resource the event is assigned to.
     * @param eventType The event type.
     * @param category The event category.
     * @param details The event details.
     * @param groupId The id of the group in whose name the event will be
     * created.
     * @param pSecurityContext The SimpleRESTContext for OAuth check.
     *
     * @return TRUE if the event was published. Be aware that the fact that the
     * event has been published does not imply that the event has also been
     * consumed by any of the configured consumers.
     *
     * @see edu.kit.dama.rest.audit.types.AuditEventWrapper
     */
    public boolean createEvent(
            String pid,
            AuditEvent.TYPE eventType,
            String category,
            List<AuditDetail> details,
            String groupId,
            SimpleRESTContext pSecurityContext) {
        AuditEventWrapper returnValue;
        setFilterFromContext(pSecurityContext);
        MultivaluedMap formParams;
        formParams = new MultivaluedMapImpl();
        if (pid == null || eventType == null || category == null) {
            throw new IllegalArgumentException("Neither pid nor eventType or category must be null.");
        }

        formParams.add(PARAMETER_PID, pid);
        formParams.add(PARAMETER_EVENT_TYPE, eventType.toString());
        formParams.add(PARAMETER_CATEGORY, category);
        if (details != null && !details.isEmpty()) {
            formParams.add(PARAMETER_DETAILS, AuditEvent.detailsToJson(details));
        }
        MultivaluedMap queryParams;
        queryParams = new MultivaluedMapImpl();
        if (groupId != null) {
            queryParams.add(Constants.REST_PARAMETER_GROUP_ID, groupId);
        }
        return performAuditEventPost(EVENTS_URL, queryParams, formParams);
    }

    /**
     * Create a new external audit event with the provided properties.
     *
     * @param pid The pid of the resource the event is assigned to.
     * @param eventType The event type.
     * @param category The event category.
     * @param details The event details.
     * @param groupId The id of the group in whose name the event will be
     * created.
     *
     * @return TRUE if the event was added.
     *
     * @see edu.kit.dama.rest.audit.types.AuditEventWrapper
     */
    public boolean createEvent(
            String pid,
            AuditEvent.TYPE eventType,
            String category,
            List<AuditDetail> details,
            String groupId) {
        return createEvent(pid, eventType, category, details, groupId, null);
    }

    /**
     * Create a new external audit event with the provided properties.
     *
     * @param pid The pid of the resource the event is assigned to.
     * @param eventType The event type.
     * @param category The event category.
     * @param groupId The id of the group in whose name the event will be
     * created.
     *
     * @return TRUE if the event was added.
     *
     * @see edu.kit.dama.rest.audit.types.AuditEventWrapper
     */
    public boolean createEvent(
            String pid,
            AuditEvent.TYPE eventType,
            String category,
            String groupId) {
        return createEvent(pid, eventType, category, null, groupId, null);
    }
//</editor-fold>   
}
