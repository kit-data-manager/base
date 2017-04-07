/*
 * Copyright 2014 Karlsruhe Institute of Technology.
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
package edu.kit.dama.rest.audit.services.impl;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.IAuthorizationContext;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.mdm.audit.types.AuditDetail;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.mdm.audit.util.AuditUtils;
import edu.kit.dama.mdm.core.IMetaDataManager;
import edu.kit.dama.mdm.core.authorization.SecureMetaDataManager;
import edu.kit.dama.rest.audit.types.AuditEventWrapper;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import edu.kit.dama.rest.sharing.services.interfaces.IAuditService;
import edu.kit.dama.rest.util.RestUtils;
import edu.kit.dama.util.Constants;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mf6319
 */
@Path("/")
public final class AuditRestServiceImpl implements IAuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditRestServiceImpl.class);

    @Override
    public IEntityWrapper<AuditEvent> getEvents(String groupId, String pid, String eventType, String eventTrigger, String fromDate, String toDate, String category, Integer first, Integer results, HttpContext hc) {
        if (pid == null) {
            LOGGER.error("Argument pid must not be null.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        if (results > Constants.REST_MAX_PAGE_SIZE) {
            LOGGER.error("BAD_REQUEST. Result count {} is larger than max. page size {}", results, Constants.REST_MAX_PAGE_SIZE);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

        if (ctx.getRoleRestriction().atLeast(Role.MANAGER)) {
            LOGGER.error("Insufficient role for obtaining audit information. Role is {}, but MANAGER is needed.", ctx.getRoleRestriction());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Building audit event query.");
            StringBuilder query = new StringBuilder();
            List<Object> arguments = new ArrayList<>();
            int argumentIndex = 1;
            query.append("SELECT e FROM AuditEvent e WHERE e.pid=?").append(argumentIndex);
            arguments.add(pid);
            argumentIndex++;

            if (eventType != null) {
                LOGGER.debug(" - Appending filter by type {}", eventType);
                query.append(" AND e.eventType=?").append(argumentIndex);
                arguments.add(eventType);
                argumentIndex++;
            }
            if (eventTrigger != null) {
                LOGGER.debug(" - Appending filter by trigger {}", eventTrigger);
                query.append(" AND e.eventTrigger=?").append(argumentIndex);
                arguments.add(eventTrigger);
                argumentIndex++;
            }

            if (category != null) {
                LOGGER.debug(" - Appending filter by category {}", category);
                query.append(" AND e.category LIKE %?").append(argumentIndex).append("%");
                arguments.add(category);
                argumentIndex++;
            }

            if (fromDate != null) {
                LOGGER.debug(" - Appending filter by event date >= {}", fromDate);
                query.append(" AND e.eventDate >= ?").append(argumentIndex);
                arguments.add(df.parse(fromDate));
                argumentIndex++;
            }

            if (toDate != null) {
                LOGGER.debug(" - Appending filter by event date <= {}", toDate);
                query.append(" AND e.eventDate <= ?").append(argumentIndex);
                arguments.add(df.parse(toDate));
                argumentIndex++;
            }

            LOGGER.debug("Sending query {} for audit events.", query.toString());
            List<AuditEvent> resultList = mdm.findResultList(query.toString(), arguments.toArray(), AuditEvent.class, first, results);
            LOGGER.debug("Returning {} result(s)", resultList.size());
            return new AuditEventWrapper(resultList);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to obtain events for pid " + pid, ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } catch (ParseException ex) {
            LOGGER.error("Date argument(s) do not match pattern yyyy-MM-dd'T'HH:mm:ss'Z'", ex);
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } finally {
            mdm.close();
        }
    }

    @Override
    public IEntityWrapper<AuditEvent> getEventById(String groupId, Long id, HttpContext hc) {
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));

        if (ctx.getRoleRestriction().atLeast(Role.MANAGER)) {
            LOGGER.error("Insufficient role for obtaining audit information. Role is {}, but MANAGER is needed.", ctx.getRoleRestriction());
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        IMetaDataManager mdm = SecureMetaDataManager.factorySecureMetaDataManager(ctx);
        try {
            LOGGER.debug("Getting audit event by id {}", id);

            AuditEvent result = mdm.find(AuditEvent.class, id);
            if (result == null) {
                LOGGER.error("No audit event found for id {}", id);
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
            return new AuditEventWrapper(result);
        } catch (UnauthorizedAccessAttemptException ex) {
            LOGGER.error("Not authorized to obtain events for id " + id, ex);
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        } finally {
            mdm.close();
        }
    }

    @Override
    public Response addEvent(String groupId, String pid, String eventType, String category, String details, HttpContext hc) {
        if (eventType == null || category == null || pid == null) {
            LOGGER.error("Arguments pid, eventType and category must not be null.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        IAuthorizationContext ctx = RestUtils.authorize(hc, new GroupId(groupId));
        List<AuditDetail> detailsList = new ArrayList<>();
        if (details != null) {
            LOGGER.debug("Parsing audit details from string {}", details);
            JSONArray detailsArray = new JSONArray(details);
            for (int i = 0; i < detailsArray.length(); i++) {
                JSONObject jsonDetail = detailsArray.getJSONObject(i);
                detailsList.add(AuditDetail.initFromJson(jsonDetail));
            }
        } else {
            LOGGER.debug("No details provided. Skip parsing audit details.");
        }

        AuditUtils.audit(pid,
                ctx.getUserId().toString(),
                hc.getRequest().getAbsolutePath().toString(),
                hc.getRequest().getHeaderValue("User-Agent"),
                category,
                AuditEvent.TYPE.valueOf(eventType),
                AuditEvent.TRIGGER.EXTERNAL,
                detailsList.toArray(new AuditDetail[]{}));

        return Response.ok().build();
    }

    @Override
    public Response checkService() {
        ServiceStatus status = ServiceStatus.UNKNOWN;
        return Response.status(200).entity(new CheckServiceResponse("Audit", status)).build();
    }

}
