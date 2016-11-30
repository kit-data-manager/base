/*
 * Copyright 2015 Karlsruhe Institute of Technology.
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
package edu.kit.dama.audit.test;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.rest.audit.types.AuditEventWrapper;
import edu.kit.dama.rest.base.IEntityWrapper;
import edu.kit.dama.rest.base.types.CheckServiceResponse;
import edu.kit.dama.rest.base.types.ServiceStatus;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import edu.kit.dama.rest.sharing.services.interfaces.IAuditService;

/**
 *
 * @author dapp
 */
@Path("/AuditTest")
public class AuditTestService implements IAuditService {

    @Override
    public Response checkService() {
        return Response.status(200).entity(new CheckServiceResponse("SharingTest", ServiceStatus.OK)).build();
    }

    public AuditEvent factoryAuditEvent(long id, String pid, AuditEvent.TYPE eventType, AuditEvent.TRIGGER eventTrigger, String category, String details) {
        AuditEvent event = AuditEvent.factoryAuditEvent(AuditEvent.TYPE.CREATION, category);
        event.setId(id);
        event.setPid(pid);
        event.setEventType(eventType);
        event.setEventTrigger(eventTrigger);
        event.setDetails(details);
        return event;
    }

    @Override
    public IEntityWrapper<AuditEvent> getEvents(String groupId, String pid, String eventType, String eventTrigger, String fromDate, String toDate, String category, Integer first, Integer results, HttpContext hc) {
        return new AuditEventWrapper(factoryAuditEvent(1, pid, AuditEvent.TYPE.valueOf((eventType != null) ? eventType : "CREATION"), AuditEvent.TRIGGER.valueOf((eventTrigger != null) ? eventTrigger : "INTERNAL"), category, null));
    }

    @Override
    public IEntityWrapper<AuditEvent> getEventById(String groupId, Long id, HttpContext hc) {
        return new AuditEventWrapper(factoryAuditEvent(1, "test-1234", AuditEvent.TYPE.CREATION, AuditEvent.TRIGGER.INTERNAL, "audit.digitalObject", null));
    }

    @Override
    public Response addEvent(String groupId, String pid, String eventType, String category, String details, HttpContext hc) {
        return Response.ok().build();
    }

}
