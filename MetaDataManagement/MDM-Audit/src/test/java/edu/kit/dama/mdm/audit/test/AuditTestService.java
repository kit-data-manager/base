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
package edu.kit.dama.mdm.audit.test;

import com.sun.jersey.api.core.HttpContext;
import edu.kit.dama.mdm.audit.types.AuditDetail;
import edu.kit.dama.mdm.audit.types.AuditEvent;
import edu.kit.dama.mdm.audit.util.AuditUtils;
import edu.kit.dama.util.Constants;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
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
@Path("/audit")
public class AuditTestService {

    @GET
    @Path(value = "/digitalObjects/{baseId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getDigitalObjects(
            @PathParam("baseId") Long baseId,
            @QueryParam("groupId") @DefaultValue(Constants.USERS_GROUP_ID) String groupId,
            @javax.ws.rs.core.Context HttpContext hc) {
        AuditUtils.audit("test1234", "admin", hc.getRequest().getHeaderValue("User-Agent"), hc.getRequest().getAbsolutePath().toString(), "audit.digitalObject", AuditEvent.TYPE.CREATION, AuditEvent.TRIGGER.INTERNAL, AuditDetail.factoryArgumentDetail("java.lang.String", "groupId", groupId));
        return Response.ok().build();
    }

}
